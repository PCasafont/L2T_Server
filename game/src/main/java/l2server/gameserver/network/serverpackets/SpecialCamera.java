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

public class SpecialCamera extends L2GameServerPacket {
	
	private final int id;
	private final int dist;
	private final int yaw;
	private final int pitch;
	private final int time;
	private final int duration;
	private final int turn;
	private final int rise;
	private final int widescreen;
	private final int unknown;
	
	public SpecialCamera(int id, int dist, int yaw, int pitch, int time, int duration) {
		this.id = id;
		this.dist = dist;
		this.yaw = yaw;
		this.pitch = pitch;
		this.time = time;
		this.duration = duration;
		turn = 0;
		rise = 0;
		widescreen = 0;
		unknown = 0;
	}
	
	public SpecialCamera(int id, int dist, int yaw, int pitch, int time, int duration, int turn, int rise, int widescreen, int unk) {
		this.id = id;
		this.dist = dist;
		this.yaw = yaw;
		this.pitch = pitch;
		this.time = time;
		this.duration = duration;
		this.turn = turn;
		this.rise = rise;
		this.widescreen = widescreen;
		unknown = unk;
	}
	
	@Override
	protected final void writeImpl() {
		writeD(id);
		writeD(dist);
		writeD(yaw);
		writeD(pitch);
		writeD(time);
		writeD(duration);
		writeD(turn);
		writeD(rise);
		writeD(widescreen);
		writeD(unknown);
	}
}
