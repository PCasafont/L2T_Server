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
import l2server.gameserver.network.serverpackets.PledgeReceiveMemberInfo;

/**
 * Format: (ch) dS
 *
 * @author -Wooden-
 */
public final class RequestPledgeMemberInfo extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _unk1;
	private String _player;

	@Override
	protected void readImpl()
	{
		_unk1 = readD();
		_player = readS();
	}

	/**
	 */
	@Override
	protected void runImpl()
	{
		//Logozo.info("C5: RequestPledgeMemberInfo d:"+_unk1);
		//Logozo.info("C5: RequestPledgeMemberInfo S:"+_player);
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		//do we need powers to do that??
		L2Clan clan = activeChar.getClan();
		if (clan == null)
		{
			return;
		}
		L2ClanMember member = clan.getClanMember(_player);
		if (member == null)
		{
			return;
		}
		activeChar.sendPacket(new PledgeReceiveMemberInfo(member));
	}
}
