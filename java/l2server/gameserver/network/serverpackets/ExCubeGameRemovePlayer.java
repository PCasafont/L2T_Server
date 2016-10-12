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
 * Format: (chd) ddd
 * d: Always -1
 * d: Player Team
 * d: Player Object ID
 *
 * @author mrTJO
 */
public class ExCubeGameRemovePlayer extends L2GameServerPacket
{
	L2PcInstance _player;
	boolean _isRedTeam;

	/**
	 * Remove Player from Minigame Waiting List
	 *
	 * @param player:    Player to Remove
	 * @param isRedTeam: Is Player from Red Team?
	 */
	public ExCubeGameRemovePlayer(L2PcInstance player, boolean isRedTeam)
	{
		_player = player;
		_isRedTeam = isRedTeam;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(0xffffffff);

		writeD(_isRedTeam ? 0x01 : 0x00);
		writeD(_player.getObjectId());
	}
}
