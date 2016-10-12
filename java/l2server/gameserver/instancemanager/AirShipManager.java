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

package l2server.gameserver.instancemanager;

import gnu.trove.TIntObjectHashMap;
import l2server.L2DatabaseFactory;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.VehiclePathPoint;
import l2server.gameserver.model.actor.instance.L2AirShipInstance;
import l2server.gameserver.model.actor.instance.L2ControllableAirShipInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ExAirShipTeleportList;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.chars.L2CharTemplate;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class AirShipManager
{

	private static final String LOAD_DB = "SELECT * FROM airships";
	private static final String ADD_DB = "INSERT INTO airships (owner_id,fuel) VALUES (?,?)";
	private static final String UPDATE_DB = "UPDATE airships SET fuel=? WHERE owner_id=?";

	private L2CharTemplate _airShipTemplate = null;
	private TIntObjectHashMap<StatsSet> _airShipsInfo = new TIntObjectHashMap<>();
	private TIntObjectHashMap<L2AirShipInstance> _airShips = new TIntObjectHashMap<>();
	private TIntObjectHashMap<AirShipTeleportList> _teleports = new TIntObjectHashMap<>();

	public static AirShipManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private AirShipManager()
	{
		StatsSet npcDat = new StatsSet();
		npcDat.set("npcId", 9);
		npcDat.set("level", 0);
		npcDat.set("jClass", "boat");

		npcDat.set("STR", 0);
		npcDat.set("CON", 0);
		npcDat.set("DEX", 0);
		npcDat.set("INT", 0);
		npcDat.set("WIT", 0);
		npcDat.set("MEN", 0);

		npcDat.set("collisionRadius", 0);
		npcDat.set("collisionHeight", 0);
		npcDat.set("sex", "male");
		npcDat.set("type", "");
		npcDat.set("atkRange", 0);
		npcDat.set("mpMax", 0);
		npcDat.set("cpMax", 0);
		npcDat.set("rewardExp", 0);
		npcDat.set("rewardSp", 0);
		npcDat.set("pAtk", 0);
		npcDat.set("mAtk", 0);
		npcDat.set("pAtkSpd", 0);
		npcDat.set("aggroRange", 0);
		npcDat.set("mAtkSpd", 0);
		npcDat.set("rhand", 0);
		npcDat.set("lhand", 0);
		npcDat.set("armor", 0);
		npcDat.set("walkSpd", 0);
		npcDat.set("runSpd", 0);
		npcDat.set("name", "AirShip");
		npcDat.set("hpMax", 50000);
		npcDat.set("hpReg", 3.e-3f);
		npcDat.set("mpReg", 3.e-3f);
		npcDat.set("pDef", 100);
		npcDat.set("mDef", 100);
		_airShipTemplate = new L2CharTemplate(npcDat);

		load();
	}

	public L2AirShipInstance getNewAirShip(int x, int y, int z, int heading)
	{
		final L2AirShipInstance airShip = new L2AirShipInstance(IdFactory.getInstance().getNextId(), _airShipTemplate);

		airShip.setHeading(heading);
		airShip.setXYZInvisible(x, y, z);
		airShip.spawnMe();
		airShip.getStat().setMoveSpeed(280);
		airShip.getStat().setRotationSpeed(2000);
		return airShip;
	}

	public L2AirShipInstance getNewAirShip(int x, int y, int z, int heading, int ownerId)
	{
		final StatsSet info = _airShipsInfo.get(ownerId);
		if (info == null)
		{
			return null;
		}

		final L2AirShipInstance airShip;
		if (_airShips.containsKey(ownerId))
		{
			airShip = _airShips.get(ownerId);
			airShip.refreshID();
		}
		else
		{
			airShip = new L2ControllableAirShipInstance(IdFactory.getInstance().getNextId(), _airShipTemplate, ownerId);
			_airShips.put(ownerId, airShip);

			airShip.setMaxFuel(600);
			airShip.setFuel(info.getInteger("fuel"));
			airShip.getStat().setMoveSpeed(280);
			airShip.getStat().setRotationSpeed(2000);
		}

		airShip.setHeading(heading);
		airShip.setXYZInvisible(x, y, z);
		airShip.spawnMe();
		return airShip;
	}

	public void removeAirShip(L2AirShipInstance ship)
	{
		if (ship.getOwnerId() != 0)
		{
			storeInDb(ship.getOwnerId());
			final StatsSet info = _airShipsInfo.get(ship.getOwnerId());
			if (info != null)
			{
				info.set("fuel", ship.getFuel());
			}
		}
	}

	public boolean hasAirShipLicense(int ownerId)
	{
		return _airShipsInfo.contains(ownerId);
	}

	public void registerLicense(int ownerId)
	{
		if (!_airShipsInfo.contains(ownerId))
		{
			final StatsSet info = new StatsSet();
			info.set("fuel", 600);

			_airShipsInfo.put(ownerId, info);

			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();

				PreparedStatement statement = con.prepareStatement(ADD_DB);
				statement.setInt(1, ownerId);
				statement.setInt(2, info.getInteger("fuel"));
				statement.executeUpdate();
				statement.close();
			}
			catch (SQLException e)
			{
				Log.log(Level.WARNING,
						getClass().getSimpleName() + ": Could not add new airship license: " + e.getMessage(), e);
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING, getClass().getSimpleName() + ": Error while initializing: " + e.getMessage(), e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}

	public boolean hasAirShip(int ownerId)
	{
		final L2AirShipInstance ship = _airShips.get(ownerId);
		return !(ship == null || !(ship.isVisible() || ship.isTeleporting()));

	}

	public void registerAirShipTeleportList(int dockId, int locationId, VehiclePathPoint[][] tp, int[] fuelConsumption)
	{
		if (tp.length != fuelConsumption.length)
		{
			return;
		}

		_teleports.put(dockId, new AirShipTeleportList(locationId, fuelConsumption, tp));
	}

	public void sendAirShipTeleportList(L2PcInstance player)
	{
		if (player == null || !player.isInAirShip())
		{
			return;
		}

		final L2AirShipInstance ship = player.getAirShip();
		if (!ship.isCaptain(player) || !ship.isInDock() || ship.isMoving())
		{
			return;
		}

		int dockId = ship.getDockId();
		if (!_teleports.contains(dockId))
		{
			return;
		}

		final AirShipTeleportList all = _teleports.get(dockId);
		player.sendPacket(new ExAirShipTeleportList(all.location, all.routes, all.fuel));
	}

	public VehiclePathPoint[] getTeleportDestination(int dockId, int index)
	{
		final AirShipTeleportList all = _teleports.get(dockId);
		if (all == null)
		{
			return null;
		}

		if (index < -1 || index >= all.routes.length)
		{
			return null;
		}

		return all.routes[index + 1];
	}

	public int getFuelConsumption(int dockId, int index)
	{
		final AirShipTeleportList all = _teleports.get(dockId);
		if (all == null)
		{
			return 0;
		}

		if (index < -1 || index >= all.fuel.length)
		{
			return 0;
		}

		return all.fuel[index + 1];
	}

	private void load()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement(LOAD_DB);
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				StatsSet info = new StatsSet();
				info.set("fuel", rset.getInt("fuel"));

				_airShipsInfo.put(rset.getInt("owner_id"), info);
				info = null;
			}

			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			Log.log(Level.WARNING, getClass().getSimpleName() + ": Could not load airships table: " + e.getMessage(),
					e);
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, getClass().getSimpleName() + ": Error while initializing: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		Log.info(getClass().getSimpleName() + ": Loaded " + _airShipsInfo.size() + " private airships");
	}

	private void storeInDb(int ownerId)
	{
		StatsSet info = _airShipsInfo.get(ownerId);
		if (info == null)
		{
			return;
		}

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement(UPDATE_DB);
			statement.setInt(1, info.getInteger("fuel"));
			statement.setInt(2, ownerId);
			statement.executeUpdate();
			statement.close();
		}
		catch (SQLException e)
		{
			Log.log(Level.WARNING, getClass().getSimpleName() + ": Could not update airships table: " + e.getMessage(),
					e);
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, getClass().getSimpleName() + ": Error while save: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private static class AirShipTeleportList
	{
		public int location;
		public int[] fuel;
		public VehiclePathPoint[][] routes;

		public AirShipTeleportList(int loc, int[] f, VehiclePathPoint[][] r)
		{
			location = loc;
			fuel = f;
			routes = r;
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final AirShipManager _instance = new AirShipManager();
	}
}
