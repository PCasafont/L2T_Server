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

/**
 * Format: (chd) ddd
 * d: Time Left
 * d: Blue Points
 * d: Red Points
 *
 * @author mrTJO
 */
public class ExCubeGameChangePoints extends L2GameServerPacket {
	int timeLeft;
	int bluePoints;
	int redPoints;
	
	/**
	 * Change Client Point Counter
	 *
	 * @param timeLeft   Time Left before Minigame's End
	 * @param bluePoints Current Blue Team Points
	 * @param redPoints  Current Red Team Points
	 */
	public ExCubeGameChangePoints(int timeLeft, int bluePoints, int redPoints) {
		this.timeLeft = timeLeft;
		this.bluePoints = bluePoints;
		this.redPoints = redPoints;
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl() {
		writeD(timeLeft);
		writeD(bluePoints);
		writeD(redPoints);
	}
}
