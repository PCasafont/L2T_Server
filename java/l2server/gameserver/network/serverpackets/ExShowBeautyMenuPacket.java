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
 * @author Pere
 */
public final class ExShowBeautyMenuPacket extends L2GameServerPacket
{
	private boolean _isRestore;
	private L2PcInstance _player;

	public ExShowBeautyMenuPacket(boolean isRestore, L2PcInstance player)
	{
		_isRestore = isRestore;
		_player = player;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_isRestore ? 1 : 0); //0 add 1 remove
		writeD(_player.getAppearance().getHairStyle());
		writeD(_player.getAppearance().getHairColor());
		writeD(_player.getAppearance().getFace());
	}
}
