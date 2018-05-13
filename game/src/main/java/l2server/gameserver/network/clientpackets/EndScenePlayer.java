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

import l2server.gameserver.model.actor.instance.Player;

/**
 * @author JIV
 */
public final class EndScenePlayer extends L2GameClientPacket {

	private int movieId;

	@Override
	protected void readImpl() {
		movieId = readD();
	}

	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}
		if (movieId == 0) {
			return;
		}
		if (activeChar.getMovieId() != movieId) {
			log.warn("Player " + getClient() + " sent EndScenePlayer with wrong movie id: " + movieId);
			return;
		}
		activeChar.setMovieId(0);
		/* L2j guarrineitorada, we'll see if it explodes but it won't most probably
		 * activeChar.setTeleporting(true, false); // avoid to get player removed from World
		 * activeChar.decayMe();
		 * activeChar.spawnMe(activeChar.getPosition().getX(), activeChar.getPosition().getY(), activeChar.getPosition().getZ());
		 * activeChar.setTeleporting(false, false);
		 */
	}
}
