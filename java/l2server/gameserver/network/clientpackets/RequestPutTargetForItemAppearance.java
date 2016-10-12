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

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExPutTargetResultForItemAppearance;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.item.L2Item;

/**
 * @author Pere
 */
public final class RequestPutTargetForItemAppearance extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	/**
	 */
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}

		L2ItemInstance stone = player.getActiveAppearanceStone();
		if (stone == null)
		{
			return;
		}

		L2ItemInstance item = player.getInventory().getItemByObjectId(_objectId);
		if (item == null)
		{
			return;
		}

		if (!item.getItem().canBeUsedAsApp() || item.getTime() != -1)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ITEM_CANNOT_APPEARENCE_WEAPON));
			return;
		}

		int type = stone.getStoneType();
		int itemType = item.getItem().getType2();
		if (item.getItem().getBodyPart() == L2Item.SLOT_BACK)
		{
			itemType = L2Item.TYPE2_SHIELD_ARMOR;
		}

		switch (item.getItem().getBodyPart())
		{
			case L2Item.SLOT_HAIR:
			case L2Item.SLOT_HAIR2:
			case L2Item.SLOT_HAIRALL:
			case L2Item.SLOT_BACK:
				break;
			default:
			{
				boolean isCorrectGrade = item.getItem().getItemGradePlain() == stone.getItem().getItemGradePlain();
				boolean isCorrectType = type == -1 || itemType == type;
				if (!isCorrectGrade || !isCorrectType)
				{
					sendPacket(new ExPutTargetResultForItemAppearance(0, 0));
					return;
				}
				break;
			}
		}

		// TODO adena price
		sendPacket(new ExPutTargetResultForItemAppearance(1, 30));

		// TODO fix that
		sendPacket(new ExShowScreenMessage(
				"Warning: adding the appearance item will start the transformation right away...", 3000));
		ThreadPoolManager.getInstance().scheduleGeneral(
				() -> sendPacket(new ExShowScreenMessage("...destroying the stone and the appearance item!", 3000)),
				3000);
	}
}
