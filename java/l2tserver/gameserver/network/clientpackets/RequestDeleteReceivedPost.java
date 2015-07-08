/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2tserver.gameserver.network.clientpackets;

import static l2tserver.gameserver.model.actor.L2Character.ZONE_PEACE;
import l2tserver.Config;
import l2tserver.gameserver.instancemanager.MailManager;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.model.entity.Message;
import l2tserver.gameserver.network.SystemMessageId;
import l2tserver.gameserver.network.serverpackets.ExChangePostState;
import l2tserver.gameserver.network.serverpackets.SystemMessage;
import l2tserver.gameserver.util.Util;

/**
 * @author Pere, DS
 */
public final class RequestDeleteReceivedPost extends L2GameClientPacket
{
	private static final String _C__D0_68_REQUESTDELETERECEIVEDPOST = "[C] D0:68 RequestDeleteReceivedPost";
	
	private static final int BATCH_LENGTH = 4; // length of the one item
	
	int[] _msgIds = null;
	
	@Override
	protected void readImpl()
	{
		int count = readD();
		if (count <= 0
				|| count > Config.MAX_ITEM_IN_PACKET
				|| count * BATCH_LENGTH != _buf.remaining())
			return;
		
		_msgIds = new int[count];
		for (int i = 0; i < count; i++)
			_msgIds[i] = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null || _msgIds == null || !Config.ALLOW_MAIL)
			return;
		
		if (!activeChar.isInsideZone(ZONE_PEACE))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_USE_MAIL_OUTSIDE_PEACE_ZONE));
			return;
		}
		
		for (int msgId : _msgIds)
		{
			Message msg = MailManager.getInstance().getMessage(msgId);
			if (msg == null)
				continue;
			if (msg.getReceiverId() != activeChar.getObjectId())
			{
				Util.handleIllegalPlayerAction(activeChar,
						"Player "+activeChar.getName()+" tried to delete not own post!", Config.DEFAULT_PUNISH);
				return;
			}
			
			if (msg.hasAttachments() || msg.isDeletedByReceiver())
				return;
			
			msg.setDeletedByReceiver();
		}
		activeChar.sendPacket(new ExChangePostState(true, _msgIds, Message.DELETED));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_68_REQUESTDELETERECEIVEDPOST;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}