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

import l2server.gameserver.model.WorldObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KenM
 */
public final class ExShowTrace extends L2GameServerPacket {
	private final List<Trace> traces = new ArrayList<>();
	
	public void addTrace(int x, int y, int z, int time) {
		traces.add(new Trace(x, y, z, time));
	}
	
	public void addTrace(WorldObject obj, int time) {
		this.addTrace(obj.getX(), obj.getY(), obj.getZ(), time);
	}
	
	static final class Trace {
		public final int x;
		public final int y;
		public final int z;
		public final int time;
		
		public Trace(int x, int y, int z, int time) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.time = time;
		}
	}

    /*
	  @see l2server.gameserver.network.serverpackets.L2GameServerPacket#getType()
     */
	
	/**
	 * @see l2server.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl() {
		writeH(traces.size());
		for (Trace t : traces) {
			writeD(t.x);
			writeD(t.y);
			writeD(t.z);
			writeH(t.time);
		}
	}
}
