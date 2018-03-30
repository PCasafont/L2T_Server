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
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.model.itemcontainer.PcInventory;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.templates.item.L2Weapon;
import l2server.gameserver.util.Broadcast;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.1.2.5 $ $Date: 2005/03/27 15:30:07 $
 */

public class BlessedSpiritShot implements IItemHandler {
	/**
	 * @see IItemHandler#useItem(L2Playable, L2ItemInstance, boolean)
	 */
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean forceUse) {
		if (!(playable instanceof L2PcInstance)) {
			return;
		}

		L2PcInstance activeChar = (L2PcInstance) playable;
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		L2Weapon weaponItem = activeChar.getActiveWeaponItem();
		int itemId = item.getItemId();

		// Check if Blessed SpiritShot can be used
		if (weaponInst == null || weaponItem == null || weaponItem.getSpiritShotCount() == 0) {
			if (!activeChar.hasAutoSoulShot(item)) {
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_USE_SPIRITSHOTS));
			}
			return;
		}

		// Check if Blessed SpiritShot is already active (it can be charged over SpiritShot)
		if (weaponInst.getChargedSpiritShot() != L2ItemInstance.CHARGED_NONE) {
			return;
		}

		// Check for correct grade
		final int weaponGrade = weaponItem.getCrystalType();
		boolean gradeCheck = true;

		switch (weaponGrade) {
			case L2Item.CRYSTAL_NONE:
				if (itemId != 3947) {
					gradeCheck = false;
				}
				break;
			case L2Item.CRYSTAL_D:
				if (itemId != 3948 && itemId != 22072) {
					gradeCheck = false;
				}
				break;
			case L2Item.CRYSTAL_C:
				if (itemId != 3949 && itemId != 22073) {
					gradeCheck = false;
				}
				break;
			case L2Item.CRYSTAL_B:
				if (itemId != 3950 && itemId != 22074) {
					gradeCheck = false;
				}
				break;
			case L2Item.CRYSTAL_A:
				if (itemId != 3951 && itemId != 22075) {
					gradeCheck = false;
				}
				break;
			case L2Item.CRYSTAL_S:
			case L2Item.CRYSTAL_S80:
			case L2Item.CRYSTAL_S84:
				if (itemId != 3952 && itemId != 22076) {
					gradeCheck = false;
				}
				break;
			case L2Item.CRYSTAL_R:
			case L2Item.CRYSTAL_R95:
			case L2Item.CRYSTAL_R99:
				if (itemId != 19442 && itemId != 22434) {
					gradeCheck = false;
				}
				break;
		}

		if (!gradeCheck) {
			if (!activeChar.hasAutoSoulShot(item)) {
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH));
			}

			return;
		}

		int sapphireLvl = 0;
		PcInventory playerInventory = activeChar.getInventory();
		for (int i = Inventory.PAPERDOLL_JEWELRY1; i < Inventory.PAPERDOLL_JEWELRY1 + playerInventory.getMaxJewelryCount(); i++) {
			L2ItemInstance jewel = playerInventory.getPaperdollItem(i);
			if (jewel != null) {
				//Sapphire
				switch (jewel.getItemId()) {
					case 38927:
						sapphireLvl = 1;
						break;
					case 38928:
						sapphireLvl = 2;
						break;
					case 38929:
						sapphireLvl = 3;
						break;
					case 38930:
						sapphireLvl = 4;
						break;
					case 38931:
						sapphireLvl = 5;
						break;
					case 47689:
						sapphireLvl = 6;
						break;
				}
			}
		}

		int skillId = 0;
		int skillLvl = 1;
		double sapphireMul = 1.0;
		switch (sapphireLvl) {
			case 0:
				switch (itemId) {
					case 3948:
						skillId = 2160;
						break;
					case 3949:
						skillId = 2161;
						break;
					case 3950:
						skillId = 2162;
						break;
					case 3951:
						skillId = 2163;
						break;
					case 3952:
						skillId = 2164;
						break;
					case 22072:
						skillId = 26050;
						break;
					case 22073:
						skillId = 26051;
						break;
					case 22074:
						skillId = 26052;
						break;
					case 22075:
						skillId = 26053;
						break;
					case 22076:
						skillId = 26054;
					case 22434:
						skillId = 9195;
						break;
				}
				break;
			case 1:
				skillId = 17818;
				skillLvl = 3;
				sapphireMul = 1.01;
				break;
			case 2:
				skillId = 17818;
				skillLvl = 4;
				sapphireMul = 1.035;
				break;
			case 3:
				skillId = 17819;
				skillLvl = 2;
				sapphireMul = 1.075;
				break;
			case 4:
				skillId = 17820;
				skillLvl = 2;
				sapphireMul = 1.125;
				break;
			case 5:
				skillId = 17821;
				skillLvl = 2;
				sapphireMul = 1.2;
				break;
			case 6:
				skillId = 18718;
				sapphireMul = 1.2;
				break;
		}

		activeChar.consumableLock.lock();
		try {
			// Check if Soul shot is already active
			if (weaponInst.getChargedSpiritShot() != L2ItemInstance.CHARGED_NONE) {
				return;
			}

			// Consume Blessed SpiritShot if player has enough of them
			if (!activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), weaponItem.getSpiritShotCount(), null, false)) {
				if (!activeChar.disableAutoShot(item)) {
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS));
				}
				return;
			}

			// Charge Blessed SpiritShot
			weaponInst.setChargedSpiritShot(L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT * sapphireMul);
		} finally {
			activeChar.consumableLock.unlock();
		}

		// Send message to client
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ENABLED_SPIRITSHOT));
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, activeChar, skillId, skillLvl, 0, 0, 0), 360000);
	}
}
