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

import l2server.gameserver.model.actor.instance.L2DoorInstance;
import l2server.gameserver.util.Util;

/*
 * @author SYS @date 10/9/2007
 */

public class EventTrigger extends L2GameServerPacket
{
	private boolean _active;
	private int _emitterId;

	private static final int[] _reverse_doors = {16200023, 16200024, 16200025};

	public EventTrigger(L2DoorInstance door, boolean opened)
	{
		_emitterId = door.getEmitter();

		if (Util.contains(_reverse_doors, door.getDoorId()))
		{
			_active = !opened;
		}
		else
		{
			_active = opened;
		}
	}

	public EventTrigger(int trapId, boolean active)
	{
		_emitterId = trapId;
		_active = active;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_emitterId); // trap object id
		writeC(_active ? 1 : 0); // trap activity 1 or 0
	}
}
