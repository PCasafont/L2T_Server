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
public final class ExUserCubics extends L2GameServerPacket
{
	private L2PcInstance _player;

	public ExUserCubics(L2PcInstance character)
	{
		_player = character;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_player.getObjectId());
		writeH(_player.getCubics().size());
		for (int id : _player.getCubics().keySet())
		{
			writeH(id);
		}
		writeD(_player.getAgathionId());
	}
}
