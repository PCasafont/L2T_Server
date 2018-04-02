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
import l2server.gameserver.model.Item;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.model.itemcontainer.PcInventory;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.templates.item.ItemTemplate;
import l2server.gameserver.templates.item.WeaponTemplate;
import l2server.gameserver.util.Broadcast;

/**
 * This class ...
 *
 * @version $Revision: 1.2.4.4 $ $Date: 2005/03/27 15:30:07 $
 */

public class SoulShots implements IItemHandler {
	/**
	 * @see l2server.gameserver.handler.IItemHandler#useItem(Playable, Item, boolean)
	 */
	@Override
	public void useItem(Playable playable, Item item, boolean forceUse) {
		if (!(playable instanceof Player)) {
			return;
		}

		Player activeChar = (Player) playable;
		Item weaponInst = activeChar.getActiveWeaponInstance();
		WeaponTemplate weaponItem = activeChar.getActiveWeaponItem();
		int itemId = item.getItemId();

		// Check if Soul shot can be used
		if (weaponInst == null || weaponItem == null || weaponItem.getSoulShotCount() == 0) {
			if (!activeChar.hasAutoSoulShot(item)) {
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_USE_SOULSHOTS));
			}
			return;
		}

		final int weaponGrade = weaponItem.getCrystalType();
		boolean gradeCheck = true;

		switch (weaponGrade) {
			case ItemTemplate.CRYSTAL_NONE:
				if (itemId != 5789 && itemId != 1835) {
					gradeCheck = false;
				}
				break;
			case ItemTemplate.CRYSTAL_D:
				if (itemId != 1463 && itemId != 22082) {
					gradeCheck = false;
				}
				break;
			case ItemTemplate.CRYSTAL_C:
				if (itemId != 1464 && itemId != 22083) {
					gradeCheck = false;
				}
				break;
			case ItemTemplate.CRYSTAL_B:
				if (itemId != 1465 && itemId != 22084) {
					gradeCheck = false;
				}
				break;
			case ItemTemplate.CRYSTAL_A:
				if (itemId != 1466 && itemId != 22085) {
					gradeCheck = false;
				}
				break;
			case ItemTemplate.CRYSTAL_S:
			case ItemTemplate.CRYSTAL_S80:
			case ItemTemplate.CRYSTAL_S84:
				if (itemId != 1467 && itemId != 22086) {
					gradeCheck = false;
				}
				break;
			case ItemTemplate.CRYSTAL_R:
			case ItemTemplate.CRYSTAL_R95:
			case ItemTemplate.CRYSTAL_R99:
				if (itemId != 17754 && itemId != 22433) {
					gradeCheck = false;
				}
				break;
		}

		if (!gradeCheck) {
			if (!activeChar.hasAutoSoulShot(item)) {
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SOULSHOTS_GRADE_MISMATCH));
			}
			return;
		}

		int rubyLvl = 0;
		PcInventory playerInventory = activeChar.getInventory();
		for (int i = Inventory.PAPERDOLL_JEWELRY1; i < Inventory.PAPERDOLL_JEWELRY1 + playerInventory.getMaxJewelryCount(); i++) {
			Item jewel = playerInventory.getPaperdollItem(i);
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
						break;
				}
			}
		}

		int skillId = 0;
		int skillLvl = 1;
		double rubyMul = 1.0;
		switch (rubyLvl) {
			case 0:
				switch (itemId) {
					case 1835:
					case 5789:
						skillId = 2039;
						break;
					case 1463:
						skillId = 2150;
						break;
					case 1464:
						skillId = 2151;
						break;
					case 1465:
						skillId = 2152;
						break;
					case 1466:
						skillId = 2153;
						break;
					case 1467:
						skillId = 2154;
						break;
					case 22082:
						skillId = 26060;
						break;
					case 22083:
						skillId = 26061;
						break;
					case 22084:
						skillId = 26062;
						break;
					case 22085:
						skillId = 26063;
						break;
					case 22086:
						skillId = 26064;
						break;
					case 17754:
					case 22433:
						skillId = 9193;
						break;
				}
				break;
			case 1:
				skillId = 17814;
				rubyMul = 1.01;
				break;
			case 2:
				skillId = 17814;
				skillLvl = 2;
				rubyMul = 1.035;
				break;
			case 3:
				skillId = 17815;
				rubyMul = 1.075;
				break;
			case 4:
				skillId = 17816;
				rubyMul = 1.125;
				break;
			case 5:
				skillId = 17817;
				rubyMul = 1.2;
				break;
			case 6:
				skillId = 18715;
				rubyMul = 1.2;
				break;
		}

		activeChar.consumableLock.lock();
		try {
			// Check if Soul shot is already active
			if (weaponInst.getChargedSoulShot() != Item.CHARGED_NONE) {
				return;
			}

			// Consume Soul shots if player has enough of them
			int saSSCount = (int) activeChar.getStat().calcStat(Stats.SOULSHOT_COUNT, 0, null, null);
			int SSCount = saSSCount == 0 ? weaponItem.getSoulShotCount() : saSSCount;

			if (!activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), SSCount, null, false)) {
				if (!activeChar.disableAutoShot(item)) {
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_SOULSHOTS));
				}
				return;
			}

			// Charge soul shot
			weaponInst.setChargedSoulShot(Item.CHARGED_SOULSHOT * rubyMul);
		} finally {
			activeChar.consumableLock.unlock();
		}

		// Send message to client
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ENABLED_SOULSHOT));
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, activeChar, skillId, skillLvl, 0, 0, 0), 360000);
	}
}
