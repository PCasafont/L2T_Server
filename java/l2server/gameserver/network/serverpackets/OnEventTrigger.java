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

/**
 ** @author Gnacik
 **
 */
public class OnEventTrigger extends L2GameServerPacket
{
	private final int _emitterId;
	private final boolean _opened;
	
	private static final int[] _reverse_doors = { 16200023, 16200024, 16200025 };
	
	public OnEventTrigger(L2DoorInstance door, boolean opened)
	{
		_emitterId = door.getEmitter();
		
		if (Util.contains(_reverse_doors, door.getDoorId()))
			_opened = !opened;
		else
			_opened = opened;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xCF);
		writeD(_emitterId);
		writeD(_opened ? 0 : 1);
	}
	
	@Override
	public String getType()
	{
		return "[S] CF OnEventTrigger".intern();
	}
}