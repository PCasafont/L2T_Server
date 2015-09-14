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

import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExItemAppearanceResult;
import l2server.gameserver.network.serverpackets.ExPutTemplateResultForItemAppearance;
import l2server.gameserver.network.serverpackets.InventoryUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.item.L2ArmorType;
import l2server.gameserver.templates.item.L2Item;

/**
 * @author Pere
 */
public final class RequestPutTemplateForItemAppearance extends L2GameClientPacket
{
	private int _objectId1;
	private int _objectId2;
	
	@Override
	protected void readImpl()
	{
		_objectId1 = readD();
		_objectId2 = readD();
	}
	
	/**
	 * @see l2server.util.network.BaseRecievePacket.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		if (_objectId1 == _objectId2)
			return;
		
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		L2ItemInstance stone = player.getActiveAppearanceStone();
		if (stone == null)
			return;

		L2ItemInstance target = player.getInventory().getItemByObjectId(_objectId1);
		if (target == null)
			return;

		L2ItemInstance template = player.getInventory().getItemByObjectId(_objectId2);
		if (template == null)
			return;
		
		if (!template.getItem().canBeUsedAsApp())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ITEM_CANNOT_APPEARENCE_WEAPON));
			return;
		}
		
		int type = stone.getStoneType();
		int itemType = target.getItem().getType2();
		if (target.getItem().getBodyPart() == L2Item.SLOT_BACK)
			itemType = L2Item.TYPE2_SHIELD_ARMOR;
		
		if (target.getItem().getItemGradePlain() != stone.getItem().getItemGradePlain()
				|| target.getItem().getItemGrade() < template.getItem().getItemGrade()
				|| (type != -1 && itemType != type)
				|| target.getItem().getType2() != template.getItem().getType2()
				|| (target.getItem().getItemType() != template.getItem().getItemType()
						&& template.getItem().getItemType() != L2ArmorType.NONE
						&& template.getItem().getBodyPart() != L2Item.SLOT_ALLDRESS)
				|| (target.getItem().getBodyPart() != template.getItem().getBodyPart()
						&& !(template.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR
						&& target.getItem().getBodyPart() == L2Item.SLOT_CHEST)
						&& template.getItem().getBodyPart() != L2Item.SLOT_ALLDRESS))
		{
			sendPacket(new ExPutTemplateResultForItemAppearance(0));
			return;
		}

		sendPacket(new ExPutTemplateResultForItemAppearance(2));
		
		target.setAppearance(template.getItemId());
		player.destroyItem("Appearance", stone.getObjectId(), 1, player, true);
		player.destroyItem("Appearance", template, player, true);
		sendPacket(new ExItemAppearanceResult(1, target));
		
		InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(target);
		player.sendPacket(iu);
		player.broadcastUserInfo();
	}
	
	/**
	 * @see l2server.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "RequestWorldStatistics";
	}
}
