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

package l2server.gameserver.network.clientpackets;

import l2server.gameserver.datatables.LifeStoneTable;
import l2server.gameserver.datatables.LifeStoneTable.LifeStone;
import l2server.gameserver.model.L2Augmentation;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExVariationResult;
import l2server.gameserver.network.serverpackets.InventoryUpdate;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * Format:(ch) dddd
 *
 * @author -Wooden-
 */
public final class RequestRefine extends L2GameClientPacket
{
	private int _targetItemObjId;
	private int _refinerItemObjId;
	private int _gemStoneItemObjId;
	private long _gemStoneCount;

	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
		_gemStoneItemObjId = readD();
		_gemStoneCount = readQ();
	}

	/**
	 */
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		if (targetItem == null)
		{
			return;
		}
		L2ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		if (refinerItem == null)
		{
			return;
		}
		L2ItemInstance gemStoneItem = activeChar.getInventory().getItemByObjectId(_gemStoneItemObjId);
		if (gemStoneItem == null)
		{
			return;
		}

		if (!LifeStoneTable.getInstance().isValid(activeChar, targetItem, refinerItem, gemStoneItem))
		{
			activeChar.sendPacket(new ExVariationResult(0, 0, 0));
			activeChar.sendPacket(SystemMessage
					.getSystemMessage(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS));
			return;
		}

		final LifeStone ls = LifeStoneTable.getInstance().getLifeStone(refinerItem.getItemId());
		if (ls == null)
		{
			return;
		}

		if (_gemStoneCount != LifeStoneTable.getGemStoneCount(targetItem.getItem().getItemGrade(), ls.getGrade()))
		{
			activeChar.sendPacket(new ExVariationResult(0, 0, 0));
			activeChar.sendPacket(SystemMessage
					.getSystemMessage(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS));
			return;
		}

		// unequip item
		if (targetItem.isEquipped())
		{
			L2ItemInstance[] unequiped =
					activeChar.getInventory().unEquipItemInSlotAndRecord(targetItem.getLocationSlot());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance itm : unequiped)
			{
				iu.addModifiedItem(itm);
			}

			activeChar.sendPacket(iu);
			activeChar.broadcastUserInfo();
		}

		// consume the life stone
		if (!activeChar.destroyItem("RequestRefine", refinerItem, 1, null, false))
		{
			return;
		}

		// consume the gemstones
		if (!activeChar.destroyItem("RequestRefine", gemStoneItem, _gemStoneCount, null, false))
		{
			return;
		}

		final L2Augmentation aug = LifeStoneTable.getInstance().generateRandomAugmentation(ls, targetItem);
		targetItem.setAugmentation(aug);

		final int stat12 = aug.getAugment1().getId();
		final int stat34 = aug.getAugment2().getId();
		activeChar.sendPacket(new ExVariationResult(stat12, stat34, 1));

		InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(targetItem);
		activeChar.sendPacket(iu);

		StatusUpdate su = new StatusUpdate(activeChar);
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);
	}
}
