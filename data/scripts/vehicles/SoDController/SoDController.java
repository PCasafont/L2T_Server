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

package vehicles.SoDController;

import l2server.gameserver.model.Location;
import l2server.gameserver.model.VehiclePathPoint;
import vehicles.AirShipController;

public class SoDController extends AirShipController
{
	private static final int DOCK_ZONE = 50601;
	private static final int LOCATION = 102;
	private static final int CONTROLLER_ID = 32605;

	private static final VehiclePathPoint[] ARRIVAL = {new VehiclePathPoint(-246445, 252331, 4359, 280, 2000),};

	private static final VehiclePathPoint[] DEPART = {new VehiclePathPoint(-245245, 251040, 4359, 280, 2000)};

	private static final VehiclePathPoint[][] TELEPORTS = {
			{
					new VehiclePathPoint(-245245, 251040, 4359, 280, 2000),
					new VehiclePathPoint(-235693, 248843, 5100, 0, 0)
			},
			{new VehiclePathPoint(-245245, 251040, 4359, 280, 2000), new VehiclePathPoint(-195357, 233430, 2500, 0, 0)}
	};

	private static final int[] FUEL = {0, 100};

	public SoDController(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(CONTROLLER_ID);
		addFirstTalkId(CONTROLLER_ID);
		addTalkId(CONTROLLER_ID);

		this.dockZone = DOCK_ZONE;
		addEnterZoneId(DOCK_ZONE);
		addExitZoneId(DOCK_ZONE);

		this.shipSpawnX = -247702;
		this.shipSpawnY = 253631;
		this.shipSpawnZ = 4359;

		this.oustLoc = new Location(-247746, 251079, 4328);

		this.locationId = LOCATION;
		this.arrivalPath = ARRIVAL;
		this.departPath = DEPART;
		this.teleportsTable = TELEPORTS;
		this.fuelTable = FUEL;

		this.movieId = 1003;

		validityCheck();
	}

	public static void main(String[] args)
	{
		new SoDController(-1, SoDController.class.getSimpleName(), "vehicles");
	}
}
