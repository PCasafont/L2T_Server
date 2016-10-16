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
public class ExReplyReceivedPost extends L2ItemListPacket
{

	private Message msg;
	private L2ItemInstance[] items = null;

	public ExReplyReceivedPost(Message msg)
	{
		this.msg = msg;
		if (msg.hasAttachments())
		{
			final ItemContainer attachments = msg.getAttachments();
			if (attachments != null && attachments.getSize() > 0)
			{
				this.items = attachments.getItems();
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
		writeD(this.msg.getSendBySystem());
		if (this.msg.getSendBySystem() == Message.SendBySystem.SYSTEM.ordinal())
		{
			writeD(0x00);// unknown1
			writeD(0x00);// unknown2
			writeD(0x00);// unknown3
			writeD(0x00);// unknown4
			writeD(0x00);// unknown5
			writeD(0x00);// unknown6
			writeD(0x00);// unknown7
			writeD(0x00);// unknown8
			writeD(this.msg.getSystemMessage1());
			writeD(this.msg.getSystemMessage2());
		}

		writeD(this.msg.getId());
		writeD(this.msg.isLocked() ? 1 : 0);
		writeD(0x00); //Unknown
		writeS(this.msg.getSenderName());
		writeS(this.msg.getSubject());
		writeS(this.msg.getContent());

		if (this.items != null && this.items.length > 0)
		{
			writeD(this.items.length);
			for (L2ItemInstance item : this.items)
			{
				writeItem(item);
				writeD(item.getObjectId());
			}
			this.items = null;
		}
		else
		{
			writeD(0x00);
		}

		writeQ(this.msg.getReqAdena());
		writeD(this.msg.hasAttachments() ? 1 : 0);
		writeD(this.msg.getSendBySystem() > 0 ? 0x00 : 0x01);
		writeD(this.msg.getReceiverId());

		this.msg = null;
	}
}
