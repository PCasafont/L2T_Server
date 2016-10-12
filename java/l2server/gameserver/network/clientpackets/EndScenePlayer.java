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
import l2server.log.Log;

/**
 * @author JIV
 */
public final class EndScenePlayer extends L2GameClientPacket
{

	private int _movieId;

	@Override
	protected void readImpl()
	{
		_movieId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		if (_movieId == 0)
		{
			return;
		}
		if (activeChar.getMovieId() != _movieId)
		{
			Log.warning("Player " + getClient() + " sent EndScenePlayer with wrong movie id: " + _movieId);
			return;
		}
		activeChar.setMovieId(0);
		/* L2j guarrineitorada, we'll see if it explodes but it won't most probably
         * activeChar.setIsTeleporting(true, false); // avoid to get player removed from L2World
		 * activeChar.decayMe();
		 * activeChar.spawnMe(activeChar.getPosition().getX(), activeChar.getPosition().getY(), activeChar.getPosition().getZ());
		 * activeChar.setIsTeleporting(false, false);
		 */
	}
}
