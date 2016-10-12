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
	int _timeLeft;
	int _bluePoints;
	int _redPoints;
	boolean _isRedTeam;
	L2PcInstance _player;
	int _playerPoints;

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
		_timeLeft = timeLeft;
		_bluePoints = bluePoints;
		_redPoints = redPoints;
		_isRedTeam = isRedTeam;
		_player = player;
		_playerPoints = playerPoints;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(_timeLeft);
		writeD(_bluePoints);
		writeD(_redPoints);

		writeD(_isRedTeam ? 0x01 : 0x00);
		writeD(_player.getObjectId());
		writeD(_playerPoints);
	}
}
