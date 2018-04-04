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

package l2server.gameserver.model;

import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.RadarControl;

import java.util.ArrayList;

/**
 * @author dalrond
 */
public final class L2Radar {
	private Player player;
	private ArrayList<RadarMarker> markers;
	
	public L2Radar(Player player) {
		this.player = player;
		markers = new ArrayList<>();
	}
	
	// Add a marker to player's radar
	public void addMarker(int x, int y, int z) {
		RadarMarker newMarker = new RadarMarker(x, y, z);
		
		markers.add(newMarker);
		player.sendPacket(new RadarControl(2, 2, x, y, z));
		player.sendPacket(new RadarControl(0, 1, x, y, z));
	}
	
	// Remove a marker from player's radar
	public void removeMarker(int x, int y, int z) {
		RadarMarker newMarker = new RadarMarker(x, y, z);
		
		markers.remove(newMarker);
		player.sendPacket(new RadarControl(1, 1, x, y, z));
	}
	
	public void removeAllMarkers() {
		for (RadarMarker tempMarker : markers) {
			player.sendPacket(new RadarControl(2, 2, tempMarker.x, tempMarker.y, tempMarker.z));
		}
		
		markers.clear();
	}
	
	public void loadMarkers() {
		player.sendPacket(new RadarControl(2, 2, player.getX(), player.getY(), player.getZ()));
		for (RadarMarker tempMarker : markers) {
			player.sendPacket(new RadarControl(0, 1, tempMarker.x, tempMarker.y, tempMarker.z));
		}
	}
	
	public static class RadarMarker {
		// Simple class to model radar points.
		public int type, x, y, z;
		
		public RadarMarker(int type, int x, int y, int z) {
			this.type = type;
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public RadarMarker(int x, int y, int z) {
			type = 1;
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		/**
		 * @see Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + type;
			result = prime * result + x;
			result = prime * result + y;
			result = prime * result + z;
			return result;
		}
		
		/**
		 * @see Object#equals(Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof RadarMarker)) {
				return false;
			}
			final RadarMarker other = (RadarMarker) obj;
			if (type != other.type) {
				return false;
			}
			if (x != other.x) {
				return false;
			}
			if (y != other.y) {
				return false;
			}
			return z == other.z;
		}
	}
}
