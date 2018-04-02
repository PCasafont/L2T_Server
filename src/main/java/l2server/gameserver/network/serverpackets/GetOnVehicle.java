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

import l2server.util.Point3D;

/**
 * @author Maktakien
 */
public class GetOnVehicle extends L2GameServerPacket {
	private int charObjId;
	private int boatObjId;
	private Point3D pos;
	
	public GetOnVehicle(int charObjId, int boatObjId, Point3D pos) {
		this.charObjId = charObjId;
		this.boatObjId = boatObjId;
		this.pos = pos;
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl() {
		writeD(charObjId);
		writeD(boatObjId);
		writeD(pos.getX());
		writeD(pos.getY());
		writeD(pos.getZ());
	}
}
