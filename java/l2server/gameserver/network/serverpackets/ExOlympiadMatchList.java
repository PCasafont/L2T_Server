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

import l2server.gameserver.model.olympiad.OlympiadGameClassed;
import l2server.gameserver.model.olympiad.OlympiadGameNonClassed;
import l2server.gameserver.model.olympiad.OlympiadGameTask;

import java.util.List;

/**
 * Format: (chd) ddd[dddS]
 * d: number of matches
 * d: unknown (always 0)
 * [
 * d: arena
 * d: match type
 * d: status
 * S: player 1 name
 * S: player 2 name
 * ]
 *
 * @author mrTJO
 */
public class ExOlympiadMatchList extends L2GameServerPacket
{
	private final List<OlympiadGameTask> _games;

	/**
	 * @param games: competitions list
	 */
	public ExOlympiadMatchList(List<OlympiadGameTask> games)
	{
		_games = games;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_games.size());
		writeD(0x00);

		for (OlympiadGameTask curGame : _games)
		{
			writeD(curGame.getGame().getGameId()); // Stadium Id (Arena 1 = 0)

			if (curGame.getGame() instanceof OlympiadGameNonClassed)
			{
				writeD(1);
			}
			else if (curGame.getGame() instanceof OlympiadGameClassed)
			{
				writeD(2);
			}
			else
			{
				writeD(0);
			}

			writeD(curGame.isRunning() ? 0x02 : 0x01); // (1 = Standby, 2 = Playing)
			writeS(curGame.getGame().getPlayerNames()[0]); // Player 1 Name
			writeS(curGame.getGame().getPlayerNames()[1]); // Player 2 Name
		}
	}
}
