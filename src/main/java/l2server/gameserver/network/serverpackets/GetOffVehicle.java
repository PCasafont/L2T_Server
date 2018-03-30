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

/**
 * @author Maktakien
 */
public class GetOffVehicle extends L2GameServerPacket {
	private int charObjId, boatObjId, x, y, z;
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 */
	public GetOffVehicle(int charObjId, int boatObjId, int x, int y, int z) {
		this.charObjId = charObjId;
		this.boatObjId = boatObjId;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	protected final void writeImpl() {
		writeD(charObjId);
		writeD(boatObjId);
		writeD(x);
		writeD(y);
		writeD(z);
	}
}
