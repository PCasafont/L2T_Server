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

package l2server.gameserver;

import l2server.Config;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.instance.Player;
import l2server.util.Point3D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author -Nemesiss-
 */
public class GeoData {
	private static Logger log = LoggerFactory.getLogger(GeoData.class.getName());

	protected GeoData() {
	}

	private GeoData(final boolean disabled) {
		if (disabled) {
			log.info("Geodata Engine: Disabled.");
		}
	}

	public static GeoData getInstance() {
		return SingletonHolder.instance;
	}

	// Public Methods

	/**
	 * @return Geo Block Type
	 */
	public short getType(int x, int y) {
		return 0;
	}

	/**
	 * @return Nearles Z
	 */
	public short getHeight(int x, int y, int z) {
		return (short) z;
	}

	public short getSpawnHeight(int x, int y, int zmin, int zmax, L2Spawn spawn) {
		return (short) zmin;
	}

	public String geoPosition(int x, int y) {
		return "";
	}

	/**
	 * @return True if cha can see target (LOS)
	 */
	public boolean canSeeTarget(WorldObject cha, WorldObject target) {
		//If geo is off do simple check :]
		//Don't allow casting on players on different dungeon lvls etc
		return Math.abs(target.getZ() - cha.getZ()) < 1000;
	}

	public boolean canSeeTarget(WorldObject cha, Point3D worldPosition) {
		//If geo is off do simple check :]
		//Don't allow casting on players on different dungeon lvls etc
		return Math.abs(worldPosition.getZ() - cha.getZ()) < 1000;
	}

	public boolean canSeeTarget(int x, int y, int z, int tx, int ty, int tz) {
		// If geo is off do simple check :]
		// Don't allow casting on players on different dungeon lvls etc
		return Math.abs(z - tz) < 1000;
	}

	/**
	 * @return True if cha can see target (LOS) and send usful info to PC
	 */
	public boolean canSeeTargetDebug(Player gm, WorldObject target) {
		return true;
	}

	/**
	 * @return Geo NSWE (0-15)
	 */
	public short getNSWE(int x, int y, int z) {
		return 15;
	}

	public short getHeightAndNSWE(int x, int y, int z) {
		return (short) (z << 1 | 15);
	}

	/**
	 * @return Last Location (x,y,z) where player can walk - just before wall
	 */
	public Location moveCheck(int x, int y, int z, int tx, int ty, int tz, int instanceId) {
		return new Location(tx, ty, tz);
	}

	public boolean canMoveFromToTarget(int x, int y, int z, int tx, int ty, int tz, int instanceId) {
		return true;
	}

	public void addGeoDataBug(Player gm, String comment) {
		//Do Nothing
	}

	public static void unloadGeodata(byte rx, byte ry) {

	}

	public static boolean loadGeodataFile(byte rx, byte ry) {
		return false;
	}

	public boolean hasGeo(int x, int y) {
		return false;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final GeoData instance = Config.GEODATA > 0 ? GeoEngine.getInstance() : new GeoData(true);
	}
}
