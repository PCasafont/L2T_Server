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
package l2tserver.gameserver.network.clientpackets;

import l2tserver.gameserver.model.L2ItemInstance;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.serverpackets.ExChangeAttributeInfo;
import l2tserver.gameserver.network.serverpackets.ExChangeAttributeItemList;

/**
 *
 * @author Erlandys
 */
public class SendChangeAttributeTargetItem extends L2GameClientPacket
{

	private static final String _C__D0_B7_SENDCHANGEATTRIBUTETARGETITEM = "[C] D0:B7 SendChangeAttributeTargetItem";

	private int _attributeOID;
	private int _itemOID;

	@Override
	protected void readImpl()
	{
		_attributeOID = readD();
		_itemOID = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		int attributes = -1;
		L2ItemInstance item = player.getInventory().getItemByObjectId(_itemOID);

		if (!item.isWeapon())
		{
			player.sendPacket(new ExChangeAttributeItemList(player, _attributeOID));
			return;
		}

		switch (item.getAttackElementType())
		{
			case 0:
				attributes = 2 | 4 | 8 | 16 | 32;
				break;
			case 1:
				attributes = 1 | 4 | 8 | 16 | 32;
				break;
			case 2:
				attributes = 1 | 2 | 8 | 16 | 32;
				break;
			case 3:
				attributes = 1 | 2 | 4 | 16 | 32;
				break;
			case 4:
				attributes = 1 | 2 | 4 | 8 | 32;
				break;
			case 5:
				attributes = 1 | 2 | 4 | 8 | 16;
				break;
		}

		if (attributes == -1)
		{
			player.sendPacket(new ExChangeAttributeItemList(player, _attributeOID));
			return;
		}

		player.sendPacket(new ExChangeAttributeInfo(_attributeOID, _itemOID, attributes));
	}

	@Override
	public String getType()
	{
		return _C__D0_B7_SENDCHANGEATTRIBUTETARGETITEM;
	}
}