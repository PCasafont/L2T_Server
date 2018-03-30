/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package handlers.itemhandlers;

import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.model.itemcontainer.PcInventory;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Broadcast;

/**
 * Beast SoulShot Handler
 *
 * @author Tempy
 */
public class BeastSoulShot implements IItemHandler {
	/**
	 * @see l2server.gameserver.handler.IItemHandler#useItem(l2server.gameserver.model.actor.L2Playable, l2server.gameserver.model.L2ItemInstance, boolean)
	 */
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean forceUse) {
		if (playable == null) {
			return;
		}

		L2PcInstance summoner = playable.getActingPlayer();

		if (playable instanceof L2Summon) {
			summoner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_CANNOT_USE_ITEM));
			return;
		}

		if (summoner.getPet() != null) {
			useShot(summoner, summoner.getPet(), item);
		}
		for (L2SummonInstance summon : summoner.getSummons()) {
			useShot(summoner, summon, item);
		}
	}

	public void useShot(L2PcInstance summoner, L2Summon summon, L2ItemInstance item) {
		if (summon == null) {
			summoner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PETS_ARE_NOT_AVAILABLE_AT_THIS_TIME));
			return;
		}

		if (summon.isDead()) {
			summoner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET));
			return;
		}

		int itemId = item.getItemId();
		short shotConsumption = summon.getSoulShotsPerHit();
		long shotCount = item.getCount();

		if (!(shotCount > shotConsumption)) {
			// Not enough Soulshots to use.
			if (!summoner.disableAutoShot(item)) {
				summoner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET));
			}
			return;
		}

		int rubyLvl = 0;
		PcInventory playerInventory = summoner.getInventory();
		for (int i = Inventory.PAPERDOLL_JEWELRY1; i < Inventory.PAPERDOLL_JEWELRY1 + playerInventory.getMaxJewelryCount(); i++) {
			L2ItemInstance jewel = playerInventory.getPaperdollItem(i);
			if (jewel != null) {
				//Ruby
				switch (jewel.getItemId()) {
					case 38855:
						rubyLvl = 1;
						break;
					case 38856:
						rubyLvl = 2;
						break;
					case 38857:
						rubyLvl = 3;
						break;
					case 38858:
						rubyLvl = 4;
						break;
					case 38859:
						rubyLvl = 5;
						break;
					case 47688:
						rubyLvl = 6;
				}
			}
		}

		int skillId = 0;
		int skillLvl = 1;
		double rubyMul = 1.0;
		switch (rubyLvl) {
			case 0:
				switch (itemId) {
					case 6645:
						skillId = 17817;
						break;
					default:
						skillId = 22036;
						break;
				}
				break;
			case 1:
				skillId = 17888;
				rubyMul = 1.01;
				break;
			case 2:
				skillId = 17888;
				skillLvl = 2;
				rubyMul = 1.035;
				break;
			case 3:
				skillId = 17889;
				rubyMul = 1.075;
				break;
			case 4:
				skillId = 17890;
				rubyMul = 1.125;
				break;
			case 5:
				skillId = 17891;
				rubyMul = 1.2;
				break;
			case 6:
				skillId = 18716;
				rubyMul = 1.2;
				break;
		}

		L2ItemInstance weaponInst = null;
		if (summon instanceof L2PetInstance) {
			weaponInst = ((L2PetInstance) summon).getActiveWeaponInstance();
		}

		if (weaponInst == null) {
			if (summon.getChargedSoulShot() != L2ItemInstance.CHARGED_NONE) {
				return;
			}

			summon.setChargedSoulShot(L2ItemInstance.CHARGED_SOULSHOT * rubyMul);
		} else {
			if (weaponInst.getChargedSoulShot() != L2ItemInstance.CHARGED_NONE) {
				// SoulShots are already active.
				return;
			}
			weaponInst.setChargedSoulShot(L2ItemInstance.CHARGED_SOULSHOT * rubyMul);
		}

		// If the player doesn't have enough beast soulshot remaining, remove any auto soulshot task.
		if (!summoner.destroyItemWithoutTrace("Consume", item.getObjectId(), shotConsumption, null, false)) {
			if (!summoner.disableAutoShot(item)) {
				summoner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET));
			}
			return;
		}

		// Pet uses the power of spirit.
		summoner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_USE_SPIRITSHOT));

		Broadcast.toSelfAndKnownPlayersInRadius(summoner, new MagicSkillUse(summon, summon, skillId, skillLvl, 0, 0, 0), 360000/*600*/);
	}
}
