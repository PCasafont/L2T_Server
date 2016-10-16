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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.entity.Message;
import l2server.gameserver.model.itemcontainer.ItemContainer;
import l2server.log.Log;

/**
 * @author Pere, DS
 */
public class ExReplySentPost extends L2ItemListPacket
{

	private Message msg;
	private L2ItemInstance[] items = null;

	public ExReplySentPost(Message msg)
	{
		this.msg = msg;
		if (msg.hasAttachments())
		{
			final ItemContainer attachments = msg.getAttachments();
			if (attachments != null && attachments.getSize() > 0)
			{
				items = attachments.getItems();
			}
			else
			{
				Log.warning("Message " + msg.getId() + " has attachments but itemcontainer is empty (" +
						msg.getSenderName() + " > " + msg.getReceiverName() + ").");
			}
		}
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(msg.getSendBySystem());
		writeD(msg.getId());
		writeD(msg.isLocked() ? 1 : 0);
		writeS(msg.getReceiverName());
		writeS(msg.getSubject());
		writeS(msg.getContent());

		if (items != null && items.length > 0)
		{
			writeD(items.length);
			for (L2ItemInstance item : items)
			{
				writeItem(item);
			}
			writeQ(msg.getReqAdena());
			writeD(msg.hasAttachments() ? 1 : 0);
			writeD(msg.getSendBySystem() > 0 ? 0x00 : 0x01);
			writeD(msg.getReceiverId());
		}
		else
		{
			writeD(0x00);
		}

		items = null;
		msg = null;
	}
}
