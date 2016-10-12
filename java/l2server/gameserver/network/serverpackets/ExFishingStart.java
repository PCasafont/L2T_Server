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

import l2server.gameserver.model.actor.L2Character;

/**
 * Format (ch)ddddd
 *
 * @author -Wooden-
 */
public class ExFishingStart extends L2GameServerPacket
{
	private L2Character _activeChar;
	private int _x, _y, _z, _fishType;
	private boolean _isNightLure;

	public ExFishingStart(L2Character character, int fishType, int x, int y, int z, boolean isNightLure)
	{
		_activeChar = character;
		_fishType = fishType;
		_x = x;
		_y = y;
		_z = z;
		_isNightLure = isNightLure;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(_activeChar.getObjectId());
		writeC(_fishType); // fish type
		writeD(_x); // x position
		writeD(_y); // y position
		writeD(_z); // z position
		writeC(_isNightLure ? 0x01 : 0x00); // night lure
	}
}
