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

import l2server.gameserver.model.actor.instance.AirShipInstance;

public class ExAirShipInfo extends L2GameServerPacket {
	
	// store some parameters, because they can be changed during broadcast
	private final AirShipInstance ship;
	private final int x, y, z, heading, moveSpeed, rotationSpeed, captain, helm;
	
	public ExAirShipInfo(AirShipInstance ship) {
		this.ship = ship;
		x = ship.getX();
		y = ship.getY();
		z = ship.getZ();
		heading = ship.getHeading();
		moveSpeed = (int) ship.getStat().getMoveSpeed();
		rotationSpeed = ship.getStat().getRotationSpeed();
		captain = ship.getCaptainId();
		helm = ship.getHelmObjectId();
	}
	
	@Override
	protected final void writeImpl() {
		writeD(ship.getObjectId());
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(heading);
		
		writeD(captain);
		writeD(moveSpeed);
		writeD(rotationSpeed);
		writeD(helm);
		if (helm != 0) {
			writeD(0x16e); // Controller X
			writeD(0x00); // Controller Y
			writeD(0x6b); // Controller Z
			writeD(0x15c); // Captain X
			writeD(0x00); // Captain Y
			writeD(0x69); // Captain Z
		} else {
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
		}
		
		writeD(ship.getFuel());
		writeD(ship.getMaxFuel());
	}
}
