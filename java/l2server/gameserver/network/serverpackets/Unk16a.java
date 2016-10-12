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
public final class Unk16a extends L2GameServerPacket
{
	private int _objectId;
	private int _unk1;
	private int _unk2;

	public Unk16a(L2PcInstance character)
	{
		_objectId = character.getObjectId();

		_unk1 = 9259;
		_unk2 = 69483;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		writeD(_unk1);
		writeD(_unk2);
	}
}
