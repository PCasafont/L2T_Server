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
import l2server.gameserver.instancemanager.MuseumStatistic;
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

public class SpiritShot implements IItemHandler
{
	/**
	 * 
	 * @see l2server.gameserver.handler.IItemHandler#useItem(l2server.gameserver.model.actor.L2Playable, l2server.gameserver.model.L2ItemInstance, boolean)
	 */
	public synchronized void useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		L2Weapon weaponItem = activeChar.getActiveWeaponItem();
		int itemId = item.getItemId();
		
		// Check if Spirit shot can be used
		if (weaponInst == null || weaponItem.getSpiritShotCount() == 0)
		{
			if (!activeChar.getAutoSoulShot().contains(itemId))
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_USE_SPIRITSHOTS));
			return;
		}
		
		// Check if Spirit shot is already active
		if (weaponInst.getChargedSpiritShot() != L2ItemInstance.CHARGED_NONE)
			return;
		
		final int weaponGrade = weaponItem.getCrystalType();
		int shotGrade = weaponGrade;
		boolean gradeCheck = true;
		
		switch (weaponGrade)
		{
			case L2Item.CRYSTAL_NONE:
				if (itemId != 5790 && itemId != 2509)
					gradeCheck = false;
				break;
			case L2Item.CRYSTAL_D:
				if (itemId != 2510 && itemId != 22077)
					gradeCheck = false;
				break;
			case L2Item.CRYSTAL_C:
				if (itemId != 2511 && itemId != 22078)
					gradeCheck = false;
				break;
			case L2Item.CRYSTAL_B:
				if (itemId != 2512 && itemId != 22079)
					gradeCheck = false;
				break;
			case L2Item.CRYSTAL_A:
				if (itemId != 2513 && itemId != 22080)
					gradeCheck = false;
				break;
			case L2Item.CRYSTAL_S:
			case L2Item.CRYSTAL_S80:
			case L2Item.CRYSTAL_S84:
				if (itemId != 2514 && itemId != 22081)
					gradeCheck = false;
				shotGrade = L2Item.CRYSTAL_S;
				break;
			case L2Item.CRYSTAL_R:
			case L2Item.CRYSTAL_R95:
			case L2Item.CRYSTAL_R99:
				if (itemId != 19441)
					gradeCheck = false;
				shotGrade = L2Item.CRYSTAL_R;
				break;
		}
		
		if (!gradeCheck)
		{
			if (!activeChar.getAutoSoulShot().contains(itemId))
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH));
			
			return;
		}
		
		int sapphireLvl = 0;
		PcInventory playerInventory = activeChar.getInventory();
		for (int i = Inventory.PAPERDOLL_JEWELRY1; i < Inventory.PAPERDOLL_JEWELRY1 + playerInventory.getMaxJewelryCount(); i++)
		{
			L2ItemInstance jewel = playerInventory.getPaperdollItem(i);
			if (jewel != null)
			{
				// TODO: handle better :$
				if (jewel.getName().contains("Sapphire"))
				{
					sapphireLvl = Integer.parseInt(jewel.getName().substring(13, 14));
					break;
				}
			}
		}
		
		int skillId = 0;
		int skillLvl = 1;
		double sapphireMul = 1.0;
		switch (sapphireLvl)
		{
			case 0:
				switch (itemId)
				{
					case 2509:
					case 5790:
						skillId=2061;
						break;
					case 2510:
						skillId=2155;
						break;
					case 2511:
						skillId=2156;
						break;
					case 2512:
						skillId=2157;
						break;
					case 2513:
						skillId=2158;
						break;
					case 2514:
						skillId=2159;
						break;
					case 22077:
						skillId=26055;
						break;
					case 22078:
						skillId=26056;
						break;
					case 22079:
						skillId=26057;
						break;
					case 22080:
						skillId=26058;
						break;
					case 22081:
						skillId=26059;
						break;
					case 19441:
						skillId=9194;
						break;
					case 22434:
						skillId=9195;
						break;
				}
				break;
			case 1:
				skillId = 17818;
				sapphireMul = 1.01;
				break;
			case 2:
				skillId = 17818;
				skillLvl = 2;
				sapphireMul = 1.035;
				break;
			case 3:
				skillId = 17819;
				sapphireMul = 1.075;
				break;
			case 4:
				skillId = 17820;
				sapphireMul = 1.125;
				break;
			case 5:
				skillId = 17821;
				sapphireMul = 1.2;
				break;
		}
		
		// Consume Spirit shot if player has enough of them
		if (!activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), weaponItem.getSpiritShotCount(), null, false))
		{
			if (!activeChar.disableAutoShot(itemId))
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS));
			return;
		}
		if (shotGrade != L2Item.CRYSTAL_NONE)
			activeChar.increaseStatistic(MuseumStatistic.get(14, shotGrade));
		
		// Charge Spirit shot
		weaponInst.setChargedSpiritShot(L2ItemInstance.CHARGED_SPIRITSHOT * sapphireMul);
		
		// Send message to client
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ENABLED_SPIRITSHOT));
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, activeChar, skillId, skillLvl, 0, 0), 360000);
	}
}
