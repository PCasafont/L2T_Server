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

package l2server.gameserver.network.clientpackets;

import l2server.Config;
import l2server.gameserver.instancemanager.MailManager;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Message;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExChangePostState;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Util;

import static l2server.gameserver.model.actor.L2Character.ZONE_PEACE;

/**
 * @author Pere, DS
 */
public final class RequestDeleteSentPost extends L2GameClientPacket
{

	private static final int BATCH_LENGTH = 4; // length of the one item

	int[] _msgIds = null;

	@Override
	protected void readImpl()
	{
		int count = readD();
		if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
		{
			return;
		}

		_msgIds = new int[count];
		for (int i = 0; i < count; i++)
		{
			_msgIds[i] = readD();
		}
	}

	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null || _msgIds == null || !Config.ALLOW_MAIL)
		{
			return;
		}

		if (!activeChar.isInsideZone(ZONE_PEACE))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_USE_MAIL_OUTSIDE_PEACE_ZONE));
			return;
		}

		for (int msgId : _msgIds)
		{
			Message msg = MailManager.getInstance().getMessage(msgId);
			if (msg == null)
			{
				continue;
			}
			if (msg.getSenderId() != activeChar.getObjectId())
			{
				Util.handleIllegalPlayerAction(activeChar,
						"Player " + activeChar.getName() + " tried to delete not own post!", Config.DEFAULT_PUNISH);
				return;
			}

			if (msg.hasAttachments() || msg.isDeletedBySender())
			{
				return;
			}

			msg.setDeletedBySender();
		}
		activeChar.sendPacket(new ExChangePostState(false, _msgIds, Message.DELETED));
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
