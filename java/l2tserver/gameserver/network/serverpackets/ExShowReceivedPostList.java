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
package l2tserver.gameserver.network.serverpackets;

import java.util.List;

import l2tserver.gameserver.instancemanager.MailManager;
import l2tserver.gameserver.model.entity.Message;

/**
 * @author Pere, DS
 */
public class ExShowReceivedPostList extends L2GameServerPacket
{
	private static final String _S__FE_AA_EXSHOWRECEIVEDPOSTLIST = "[S] FE:AA ExShowReceivedPostList";
	
	private List<Message> _inbox;
	
	public ExShowReceivedPostList(int objectId)
	{
		_inbox = MailManager.getInstance().getInbox(objectId);
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0xab);
		writeD((int)(System.currentTimeMillis() / 1000));
		if (_inbox != null && _inbox.size() > 0)
		{
			writeD(_inbox.size());
			for (Message msg : _inbox)
			{
				writeD(msg.getSendBySystem());
				if (msg.getSendBySystem() == Message.SendBySystem.SYSTEM.ordinal())
					writeD(msg.getSystemMessage1());
				writeD(msg.getId());
				writeS(msg.getSubject());
				writeS(msg.getSenderName());
				writeD(msg.isLocked() ? 0x01 : 0x00);
				writeD(msg.getExpirationSeconds());
				writeD(msg.isUnread() ? 0x01 : 0x00);
				writeD(msg.getSendBySystem() == 0 ? 0x00 : 0x01); // Is returnable...
				writeD(msg.hasAttachments() ? 0x01 : 0x00);
				writeD(msg.isReturned() ? 0x01 : 0x00);
				writeD(msg.getSendBySystem());
			}
			writeD(2902007);
			writeD(100);
			writeD(1000);
		}
		else
		{
			writeD(0x00);
		}
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_AA_EXSHOWRECEIVEDPOSTLIST;
	}
}
