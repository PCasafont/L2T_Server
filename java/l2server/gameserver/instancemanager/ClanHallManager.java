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
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.entity.Auction;
import l2server.gameserver.model.entity.ClanHall;
import l2server.gameserver.model.zone.type.L2ClanHallZone;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Steuf
 */
public class ClanHallManager
{

	private Map<Integer, ClanHall> clanHall;
	private Map<Integer, ClanHall> freeClanHall;
	private Map<Integer, ClanHall> allClanHalls;
	private boolean loaded = false;

	public static ClanHallManager getInstance()
	{
		return SingletonHolder.instance;
	}

	public boolean loaded()
	{
		return this.loaded;
	}

	private ClanHallManager()
	{
		Log.info("Initializing ClanHallManager");
		this.clanHall = new HashMap<>();
		this.freeClanHall = new HashMap<>();
		this.allClanHalls = new HashMap<>();
		load();
	}

    /* Reload All Clan Hall */
	/*	public final void reload() Cant reload atm - would loose zone info
        {
			this.clanHall.clear();
			this.freeClanHall.clear();
			load();
		}
	 */

	/**
	 * Load All Clan Hall
	 */
	private void load()
	{
		Connection con = null;
		try
		{
			int id, ownerId, grade = 0;
			String Name, Desc, Location;
			long lease, paidUntil = 0;
			boolean paid = false;
			PreparedStatement statement;
			ResultSet rs;
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM clanhall ORDER BY id");
			rs = statement.executeQuery();
			while (rs.next())
			{
				id = rs.getInt("id");
				Name = rs.getString("name");
				ownerId = rs.getInt("ownerId");
				lease = rs.getLong("lease");
				Desc = rs.getString("desc");
				Location = rs.getString("location");
				paidUntil = rs.getLong("paidUntil");
				grade = rs.getInt("Grade");
				paid = rs.getBoolean("paid");

				ClanHall ch = new ClanHall(id, Name, ownerId, lease, Desc, Location, paidUntil, grade, paid);
				this.allClanHalls.put(id, ch);

				if (ownerId > 0)
				{
					final L2Clan owner = ClanTable.getInstance().getClan(ownerId);
					if (owner != null)
					{
						this.clanHall.put(id, ch);
						owner.setHasHideout(id);
						continue;
					}
					else
					{
						ch.free();
					}
				}
				this.freeClanHall.put(id, ch);

				Auction auc = ClanHallAuctionManager.getInstance().getAuction(id);
				if (auc == null && lease > 0)
				{
					ClanHallAuctionManager.getInstance().initNPC(id);
				}
			}

			statement.close();
			Log.info("Loaded: " + getClanHalls().size() + " clan halls");
			Log.info("Loaded: " + getFreeClanHalls().size() + " free clan halls");
			this.loaded = true;
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Exception: ClanHallManager.load(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Get Map with all FreeClanHalls
	 */
	public final Map<Integer, ClanHall> getFreeClanHalls()
	{
		return this.freeClanHall;
	}

	/**
	 * Get Map with all ClanHalls that have owner
	 */
	public final Map<Integer, ClanHall> getClanHalls()
	{
		return this.clanHall;
	}

	/**
	 * Get Map with all ClanHalls
	 */
	public final Map<Integer, ClanHall> getAllClanHalls()
	{
		return this.allClanHalls;
	}

	/**
	 * Check is free ClanHall
	 */
	public final boolean isFree(int chId)
	{
		return this.freeClanHall.containsKey(chId);
	}

	/**
	 * Free a ClanHall
	 */
	public final synchronized void setFree(int chId)
	{
		this.freeClanHall.put(chId, this.clanHall.get(chId));
		ClanTable.getInstance().getClan(this.freeClanHall.get(chId).getOwnerId()).setHasHideout(0);
		this.freeClanHall.get(chId).free();
		this.clanHall.remove(chId);
	}

	/**
	 * Set ClanHallOwner
	 */
	public final synchronized void setOwner(int chId, L2Clan clan)
	{
		if (!this.clanHall.containsKey(chId))
		{
			this.clanHall.put(chId, this.freeClanHall.get(chId));
			this.freeClanHall.remove(chId);
		}
		else
		{
			this.clanHall.get(chId).free();
		}
		ClanTable.getInstance().getClan(clan.getClanId()).setHasHideout(chId);
		this.clanHall.get(chId).setOwner(clan);
	}

	/**
	 * Get Clan Hall by Id
	 */
	public final ClanHall getClanHallById(int clanHallId)
	{
		if (this.clanHall.containsKey(clanHallId))
		{
			return this.clanHall.get(clanHallId);
		}
		if (this.freeClanHall.containsKey(clanHallId))
		{
			return this.freeClanHall.get(clanHallId);
		}
		Log.warning("Clan hall id " + clanHallId + " not found in clanhall table!");
		return null;
	}

	/**
	 * Get Clan Hall by x,y,z
	 */
    /*
		public final ClanHall getClanHall(int x, int y, int z)
		{
			for (Map.Entry<Integer, ClanHall> ch : this.clanHall.entrySet())
				if (ch.getValue().getZone().isInsideZone(x, y, z)) return ch.getValue();

			for (Map.Entry<Integer, ClanHall> ch : this.freeClanHall.entrySet())
				if (ch.getValue().getZone().isInsideZone(x, y, z)) return ch.getValue();

			return null;
		}*/
	public final ClanHall getNearbyClanHall(int x, int y, int maxDist)
	{
		L2ClanHallZone zone = null;

		for (Map.Entry<Integer, ClanHall> ch : this.clanHall.entrySet())
		{
			zone = ch.getValue().getZone();
			if (zone != null && zone.getDistanceToZone(x, y) < maxDist)
			{
				return ch.getValue();
			}
		}
		for (Map.Entry<Integer, ClanHall> ch : this.freeClanHall.entrySet())
		{
			zone = ch.getValue().getZone();
			if (zone != null && zone.getDistanceToZone(x, y) < maxDist)
			{
				return ch.getValue();
			}
		}
		return null;
	}

	/**
	 * Get Clan Hall by Owner
	 */
	public final ClanHall getClanHallByOwner(L2Clan clan)
	{
		for (Map.Entry<Integer, ClanHall> ch : this.clanHall.entrySet())
		{
			if (clan.getClanId() == ch.getValue().getOwnerId())
			{
				return ch.getValue();
			}
		}
		return null;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ClanHallManager instance = new ClanHallManager();
	}
}
