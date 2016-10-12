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

import l2server.gameserver.GmListTable;

/**
 * This class handles RequestGmLista packet triggered by /gmlist command
 *
 * @version $Revision: 1.1.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestGMList extends L2GameClientPacket
{

	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		if (getClient().getActiveChar() == null)
		{
			return;
		}
		GmListTable.getInstance().sendListToPlayer(getClient().getActiveChar());
	}
}
