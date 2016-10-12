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

import l2server.L2DatabaseFactory;
import l2server.gameserver.InstanceListManager;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2ClanMember;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Castle;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class CastleManager implements InstanceListManager
{

	public static CastleManager getInstance()
	{
		return SingletonHolder._instance;
	}

	// =========================================================

	// =========================================================
	// Data Field
	private List<Castle> _castles;

	// =========================================================
	// Constructor
	private static final int _castleCirclets[] = {0, 6838, 6835, 6839, 6837, 6840, 6834, 6836, 8182, 8183};

	private CastleManager()
	{
	}

	// =========================================================
	// Method - Public

	public final int findNearestCastleIndex(L2Object obj)
	{
		return findNearestCastleIndex(obj, Long.MAX_VALUE);
	}

	public final int findNearestCastleIndex(L2Object obj, long maxDistance)
	{
		int index = getCastleIndex(obj);
		if (index < 0)
		{
			double distance;
			Castle castle;
			for (int i = 0; i < getCastles().size(); i++)
			{
				castle = getCastles().get(i);
				if (castle == null)
				{
					continue;
				}
				distance = castle.getDistance(obj);
				if (maxDistance > distance)
				{
					maxDistance = (long) distance;
					index = i;
				}
			}
		}
		return index;
	}

	// =========================================================
	// Method - Public

	public Castle findNearestCastle(L2Object obj)
	{
		Castle castle = null;
		double closestDistance = 99999999;
		double distance;
		Castle c;
		for (int i = 0; i < getCastles().size(); i++)
		{
			c = getCastles().get(i);
			if (c == null)
			{
				continue;
			}
			distance = c.getDistance(obj);
			if (closestDistance > distance)
			{
				closestDistance = distance;
				castle = c;
			}
		}
		return castle;
	}

	// =========================================================
	// Property - Public

	public final Castle getCastleById(int castleId)
	{
		for (Castle temp : getCastles())
		{
			if (temp.getCastleId() == castleId)
			{
				return temp;
			}
		}
		return null;
	}

	public final Castle getCastleByOwner(L2Clan clan)
	{
		for (Castle temp : getCastles())
		{
			if (temp.getOwnerId() == clan.getClanId())
			{
				return temp;
			}
		}
		return null;
	}

	public final Castle getCastle(String name)
	{
		for (Castle temp : getCastles())
		{
			if (temp.getName().equalsIgnoreCase(name.trim()))
			{
				return temp;
			}
		}
		return null;
	}

	public final Castle getCastle(int x, int y, int z)
	{
		for (Castle temp : getCastles())
		{
			if (temp.checkIfInZone(x, y, z))
			{
				return temp;
			}
		}
		return null;
	}

	public final Castle getCastle(L2Object activeObject)
	{
		return getCastle(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public final int getCastleIndex(int castleId)
	{
		Castle castle;
		for (int i = 0; i < getCastles().size(); i++)
		{
			castle = getCastles().get(i);
			if (castle != null && castle.getCastleId() == castleId)
			{
				return i;
			}
		}
		return -1;
	}

	public final int getCastleIndex(L2Object activeObject)
	{
		return getCastleIndex(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public final int getCastleIndex(int x, int y, int z)
	{
		Castle castle;
		for (int i = 0; i < getCastles().size(); i++)
		{
			castle = getCastles().get(i);
			if (castle != null && castle.checkIfInZone(x, y, z))
			{
				return i;
			}
		}
		return -1;
	}

	public final List<Castle> getCastles()
	{
		if (_castles == null)
		{
			_castles = new ArrayList<>();
		}
		return _castles;
	}

	public final void validateTaxes()
	{
		for (Castle castle : _castles)
		{
			int maxTax = castle.getTendency() == 2 ? 30 : 0;
			if (castle.getTaxPercent() > maxTax)
			{
				castle.setTaxPercent(maxTax);
			}
		}
	}

	int _castleId = 1; // from this castle

	public int getCirclet()
	{
		return getCircletByCastleId(_castleId);
	}

	public int getCircletByCastleId(int castleId)
	{
		if (castleId > 0 && castleId < 10)
		{
			return _castleCirclets[castleId];
		}

		return 0;
	}

	// remove this castle's circlets from the clan
	public void removeCirclet(L2Clan clan, int castleId)
	{
		for (L2ClanMember member : clan.getMembers())
		{
			removeCirclet(member, castleId);
		}
	}

	public void removeCirclet(L2ClanMember member, int castleId)
	{
		if (member == null)
		{
			return;
		}
		L2PcInstance player = member.getPlayerInstance();
		int circletId = getCircletByCastleId(castleId);

		if (circletId != 0)
		{
			// online-player circlet removal
			if (player != null)
			{
				try
				{
					L2ItemInstance circlet = player.getInventory().getItemByItemId(circletId);
					if (circlet != null)
					{
						if (circlet.isEquipped())
						{
							player.getInventory().unEquipItemInSlot(circlet.getLocationSlot());
						}
						player.destroyItemByItemId("CastleCircletRemoval", circletId, 1, player, true);
					}
					return;
				}
				catch (NullPointerException e)
				{
					// continue removing offline
				}
			}
			// else offline-player circlet removal
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement =
						con.prepareStatement("DELETE FROM items WHERE owner_id = ? and item_id = ?");
				statement.setInt(1, member.getObjectId());
				statement.setInt(2, circletId);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING,
						"Failed to remove castle circlets offline for player " + member.getName() + ": " +
								e.getMessage(), e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}

	@Override
	public void load()
	{
		Log.info("Initializing CastleManager");
		Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("SELECT id FROM castle ORDER BY id");
			rs = statement.executeQuery();

			while (rs.next())
			{
				Castle castle = new Castle(rs.getInt("id"));
				getCastles().add(castle);
				castle.load();
			}

			statement.close();

			Log.info("Loaded: " + getCastles().size() + " castles");
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Exception: loadCastleData(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	@Override
	public void updateReferences()
	{
	}

	@Override
	public void activateInstances()
	{
		_castles.forEach(Castle::activateInstance);
	}

	public void spawnCastleTendencyNPCs()
	{
		getCastles().forEach(Castle::manageTendencyChangeSpawns);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CastleManager _instance = new CastleManager();
	}
}
