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

/**
 * Format: (chd) dddddd
 * d: Time Left
 * d: Blue Points
 * d: Red Points
 * d: Player Team
 * d: Player Object ID
 * d: Player Points
 *
 * @author mrTJO
 */
public class ExCubeGameExtendedChangePoints extends L2GameServerPacket
{
	int timeLeft;
	int bluePoints;
	int redPoints;
	boolean isRedTeam;
	L2PcInstance player;
	int playerPoints;

	/**
	 * Update a Secret Point Counter (used by client when receive ExCubeGameEnd)
	 *
	 * @param timeLeft     Time Left before Minigame's End
	 * @param bluePoints   Current Blue Team Points
	 * @param redPoints    Current Blue Team points
	 * @param isRedTeam    Is Player from Red Team?
	 * @param player       Player Instance
	 * @param playerPoints Current Player Points
	 */
	public ExCubeGameExtendedChangePoints(int timeLeft, int bluePoints, int redPoints, boolean isRedTeam, L2PcInstance player, int playerPoints)
	{
		this.timeLeft = timeLeft;
		this.bluePoints = bluePoints;
		this.redPoints = redPoints;
		this.isRedTeam = isRedTeam;
		this.player = player;
		this.playerPoints = playerPoints;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(this.timeLeft);
		writeD(this.bluePoints);
		writeD(this.redPoints);

		writeD(this.isRedTeam ? 0x01 : 0x00);
		writeD(this.player.getObjectId());
		writeD(this.playerPoints);
	}
}
