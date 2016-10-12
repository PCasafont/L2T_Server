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

import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.PledgeReceiveWarList;

/**
 * Format: (ch) dd
 *
 * @author -Wooden-
 */
public final class RequestPledgeWarList extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _unk1;
	private int _tab;

	@Override
	protected void readImpl()
	{
		_unk1 = readD();
		_tab = readD();
	}

	/**
	 */
	@Override
	protected void runImpl()
	{
		//Logozo.info("C5: RequestPledgeWarList d:"+_unk1);
		//Logozo.info("C5: RequestPledgeWarList d:"+_tab);
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		if (activeChar.getClan() == null)
		{
			return;
		}

		//do we need powers to do that??
		activeChar.sendPacket(new PledgeReceiveWarList(activeChar.getClan(), _tab));
	}
}
