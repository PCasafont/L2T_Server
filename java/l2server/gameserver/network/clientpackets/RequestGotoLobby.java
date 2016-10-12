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

import l2server.gameserver.network.L2GameClient;
import l2server.gameserver.network.serverpackets.CharSelectionInfo;

/**
 * (ch)
 *
 * @author KenM
 */
public class RequestGotoLobby extends L2GameClientPacket
{
	/**
	 * @see l2server.gameserver.network.clientpackets.L2GameClientPacket#readImpl()
	 */
	@Override
	protected void readImpl()
	{
		// trigger
	}

	/**
	 * @see l2server.gameserver.network.clientpackets.L2GameClientPacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2GameClient client = getClient();
		client.sendPacket(new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1));
	}
}
