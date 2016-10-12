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
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.PledgeInfo;
import l2server.log.Log;

import java.util.logging.Level;

/**
 * This class ...
 *
 * @version $Revision: 1.5.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestPledgeInfo extends L2GameClientPacket
{

	private int _clanId;

	@Override
	protected void readImpl()
	{
		_clanId = readD();
	}

	@Override
	protected void runImpl()
	{
		if (Config.DEBUG)
		{
			Log.log(Level.FINE, "Info for clan " + _clanId + " requested");
		}

		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
		{
			return;
		}

		L2Clan clan = ClanTable.getInstance().getClan(_clanId);
		if (clan == null)
		{
			if (Config.DEBUG)
			{
				Log.warning("Clan data for clanId " + _clanId + " is missing for player " + activeChar.getName());
			}
			return; // we have no clan data ?!? should not happen
		}

		PledgeInfo pc = new PledgeInfo(clan);
		activeChar.sendPacket(pc);
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
