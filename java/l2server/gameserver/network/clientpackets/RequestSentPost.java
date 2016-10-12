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
import l2server.gameserver.network.serverpackets.ExReplySentPost;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Util;

import static l2server.gameserver.model.actor.L2Character.ZONE_PEACE;

/**
 * @author Pere, DS
 */
public final class RequestSentPost extends L2GameClientPacket
{

	private int _msgId;

	@Override
	protected void readImpl()
	{
		_msgId = readD();
	}

	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null || !Config.ALLOW_MAIL)
		{
			return;
		}

		Message msg = MailManager.getInstance().getMessage(_msgId);
		if (msg == null)
		{
			return;
		}

		if (!activeChar.isInsideZone(ZONE_PEACE) && msg.hasAttachments())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_USE_MAIL_OUTSIDE_PEACE_ZONE));
			return;
		}

		if (msg.getSenderId() != activeChar.getObjectId())
		{
			Util.handleIllegalPlayerAction(activeChar,
					"Player " + activeChar.getName() + " tried to read not own post!", Config.DEFAULT_PUNISH);
			return;
		}

		if (msg.isDeletedBySender())
		{
			return;
		}

		activeChar.sendPacket(new ExReplySentPost(msg));
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
