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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.List;

/**
 * Format: (chd) ddd[dS]d[dS]
 * d: unknown
 * d: always -1
 * d: blue players number
 * [
 * d: player object id
 * S: player name
 * ]
 * d: blue players number
 * [
 * d: player object id
 * S: player name
 * ]
 *
 * @author mrTJO
 */
public class ExCubeGameTeamList extends L2GameServerPacket
{

	// Players Lists
	List<L2PcInstance> bluePlayers;
	List<L2PcInstance> redPlayers;

	// Common Values
	int roomNumber;

	/**
	 * Show Minigame Waiting List to Player
	 *
	 * @param redPlayers  Red Players List
	 * @param bluePlayers Blue Players List
	 * @param roomNumber  Arena/Room ID
	 */
	public ExCubeGameTeamList(List<L2PcInstance> redPlayers, List<L2PcInstance> bluePlayers, int roomNumber)
	{
		this.redPlayers = redPlayers;
		this.bluePlayers = bluePlayers;
        this.roomNumber = roomNumber - 1;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(roomNumber);
		writeD(0xffffffff);

		writeD(bluePlayers.size());
		for (L2PcInstance player : bluePlayers)
		{
			writeD(player.getObjectId());
			writeS(player.getName());
		}
		writeD(redPlayers.size());
		for (L2PcInstance player : redPlayers)
		{
			writeD(player.getObjectId());
			writeS(player.getName());
		}
	}
}
