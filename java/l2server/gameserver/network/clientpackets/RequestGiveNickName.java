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

import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2ClanMember;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

import java.util.logging.Logger;

/**
 * This class ...
 *
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestGiveNickName extends L2GameClientPacket
{
	static Logger _log = Logger.getLogger(RequestGiveNickName.class.getName());

	private String _target;
	private String _title;

	@Override
	protected void readImpl()
	{
		_target = readS();
		_title = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		// Noblesse can bestow a title to themselves
		if (activeChar.isNoble() && _target.matches(activeChar.getName()))
		{
			activeChar.setTitle(_title);
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.TITLE_CHANGED);
			activeChar.sendPacket(sm);
			activeChar.broadcastTitleInfo();
		}
		//Can the player change/give a title?
		else if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_GIVE_TITLE) == L2Clan.CP_CL_GIVE_TITLE)
		{
			if (activeChar.getClan().getLevel() < 3)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_LVL_3_NEEDED_TO_ENDOWE_TITLE);
				activeChar.sendPacket(sm);
				sm = null;
				return;
			}

			L2ClanMember member1 = activeChar.getClan().getClanMember(_target);
			if (member1 != null)
			{
				L2PcInstance member = member1.getPlayerInstance();
				if (member != null)
				{
					//is target from the same clan?
					member.setTitle(_title);
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.TITLE_CHANGED);
					member.sendPacket(sm);
					member.broadcastTitleInfo();
					sm = null;
				}
				else
				{
					activeChar.sendMessage("Target needs to be online to get a title");
				}
			}
			else
			{
				activeChar.sendMessage("Target does not belong to your clan");
			}
		}
	}
}
