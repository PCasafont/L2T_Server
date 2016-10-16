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

import l2server.Config;
import l2server.gameserver.model.L2PremiumItem;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExGetPremiumItemList;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Util;

/**
 * * @author Gnacik
 */
public final class RequestWithDrawPremiumItem extends L2GameClientPacket
{

	private int itemNum;
	private int charId;
	private long itemcount;

	@Override
	protected void readImpl()
	{
		itemNum = readD();
		charId = readD();
		itemcount = readQ();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
		{
			return;
		}
		if (itemcount <= 0)
		{
			return;
		}

		if (activeChar.getObjectId() != charId)
		{
			Util.handleIllegalPlayerAction(activeChar,
					"[RequestWithDrawPremiumItem] Incorrect owner, Player: " + activeChar.getName(),
					Config.DEFAULT_PUNISH);
			return;
		}
		if (activeChar.getPremiumItemList().isEmpty())
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestWithDrawPremiumItem] Player: " + activeChar.getName() +
					" try to get item with empty list!", Config.DEFAULT_PUNISH);
			return;
		}
		if (activeChar.getWeightPenalty() >= 3 || !activeChar.isInventoryUnder90(false))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_RECEIVE_THE_VITAMIN_ITEM));
			return;
		}
		if (activeChar.isProcessingTransaction())
		{
			activeChar.sendPacket(SystemMessage
					.getSystemMessage(SystemMessageId.YOU_CANNOT_RECEIVE_A_VITAMIN_ITEM_DURING_AN_EXCHANGE));
			return;
		}

		L2PremiumItem item = activeChar.getPremiumItemList().get(itemNum);

		if (item == null)
		{
			return;
		}
		if (item.getCount() < itemcount)
		{
			return;
		}

		activeChar.addItem("PremiumItem", item.getItemId(), itemcount, null, true);

		if (itemcount < item.getCount())
		{
			activeChar.getPremiumItemList().get(itemNum).updateCount(item.getCount() - itemcount);
			activeChar.updatePremiumItem(itemNum, item.getCount() - itemcount);
		}
		else
		{
			activeChar.getPremiumItemList().remove(itemNum);
			activeChar.deletePremiumItem(itemNum);
		}

		if (activeChar.getPremiumItemList().isEmpty())
		{
			activeChar.sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.THERE_ARE_NO_MORE_VITAMIN_ITEMS_TO_BE_FOUND));
		}
		else
		{
			activeChar.sendPacket(new ExGetPremiumItemList(activeChar));
		}
	}
}
