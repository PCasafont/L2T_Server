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
import l2server.gameserver.model.zone.type.ClanHallZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import l2server.util.loader.annotations.Load;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Steuf
 */
public class ClanHallManager {
	private static Logger log = LoggerFactory.getLogger(ClanHallManager.class.getName());



	private Map<Integer, ClanHall> clanHall = new HashMap<>();
	private Map<Integer, ClanHall> freeClanHall = new HashMap<>();
	private Map<Integer, ClanHall> allClanHalls = new HashMap<>();
	private boolean loaded = false;

	public static ClanHallManager getInstance() {
		return SingletonHolder.instance;
	}

	public boolean loaded() {
		return loaded;
	}

	private ClanHallManager() {
	}

	/* Reload All Clan Hall */
	/*	public final void reload() Cant reload atm - would loose zone info
        {
			clanHall.clear();
			freeClanHall.clear();
			load();
		}
	 */

	/**
	 * Load All Clan Hall
	 */
	@Load(dependencies = {ClanTable.class, ClanHallAuctionManager.class})
	public void load() {
		log.info("Initializing ClanHallManager");
		Connection con = null;
		try {
			int id, ownerId, grade = 0;
			String Name, Desc, Location;
			long lease, paidUntil = 0;
			boolean paid = false;
			PreparedStatement statement;
			ResultSet rs;
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM clanhall ORDER BY id");
			rs = statement.executeQuery();
			while (rs.next()) {
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
				allClanHalls.put(id, ch);

				if (ownerId > 0) {
					final L2Clan owner = ClanTable.getInstance().getClan(ownerId);
					if (owner != null) {
						clanHall.put(id, ch);
						owner.setHasHideout(id);
						continue;
					} else {
						ch.free();
					}
				}
				freeClanHall.put(id, ch);

				Auction auc = ClanHallAuctionManager.getInstance().getAuction(id);
				if (auc == null && lease > 0) {
					ClanHallAuctionManager.getInstance().initNPC(id);
				}
			}

			statement.close();
			log.info("Loaded: " + getClanHalls().size() + " clan halls");
			log.info("Loaded: " + getFreeClanHalls().size() + " free clan halls");
			loaded = true;
		} catch (Exception e) {
			log.warn("Exception: ClanHallManager.load(): " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Get Map with all FreeClanHalls
	 */
	public final Map<Integer, ClanHall> getFreeClanHalls() {
		return freeClanHall;
	}

	/**
	 * Get Map with all ClanHalls that have owner
	 */
	public final Map<Integer, ClanHall> getClanHalls() {
		return clanHall;
	}

	/**
	 * Get Map with all ClanHalls
	 */
	public final Map<Integer, ClanHall> getAllClanHalls() {
		return allClanHalls;
	}

	/**
	 * Check is free ClanHall
	 */
	public final boolean isFree(int chId) {
		return freeClanHall.containsKey(chId);
	}

	/**
	 * Free a ClanHall
	 */
	public final synchronized void setFree(int chId) {
		freeClanHall.put(chId, clanHall.get(chId));
		ClanTable.getInstance().getClan(freeClanHall.get(chId).getOwnerId()).setHasHideout(0);
		freeClanHall.get(chId).free();
		clanHall.remove(chId);
	}

	/**
	 * Set ClanHallOwner
	 */
	public final synchronized void setOwner(int chId, L2Clan clan) {
		if (!clanHall.containsKey(chId)) {
			clanHall.put(chId, freeClanHall.get(chId));
			freeClanHall.remove(chId);
		} else {
			clanHall.get(chId).free();
		}
		ClanTable.getInstance().getClan(clan.getClanId()).setHasHideout(chId);
		clanHall.get(chId).setOwner(clan);
	}

	/**
	 * Get Clan Hall by Id
	 */
	public final ClanHall getClanHallById(int clanHallId) {
		if (clanHall.containsKey(clanHallId)) {
			return clanHall.get(clanHallId);
		}
		if (freeClanHall.containsKey(clanHallId)) {
			return freeClanHall.get(clanHallId);
		}
		log.warn("Clan hall id " + clanHallId + " not found in clanhall table!");
		return null;
	}

	/**
	 * Get Clan Hall by x,y,z
	 */
    /*
		public final ClanHall getClanHall(int x, int y, int z)
		{
			for (Map.Entry<Integer, ClanHall> ch : clanHall.entrySet())
				if (ch.getValue().getZone().isInsideZone(x, y, z)) return ch.getValue();

			for (Map.Entry<Integer, ClanHall> ch : freeClanHall.entrySet())
				if (ch.getValue().getZone().isInsideZone(x, y, z)) return ch.getValue();

			return null;
		}*/
	public final ClanHall getNearbyClanHall(int x, int y, int maxDist) {
		ClanHallZone zone = null;

		for (Map.Entry<Integer, ClanHall> ch : clanHall.entrySet()) {
			zone = ch.getValue().getZone();
			if (zone != null && zone.getDistanceToZone(x, y) < maxDist) {
				return ch.getValue();
			}
		}
		for (Map.Entry<Integer, ClanHall> ch : freeClanHall.entrySet()) {
			zone = ch.getValue().getZone();
			if (zone != null && zone.getDistanceToZone(x, y) < maxDist) {
				return ch.getValue();
			}
		}
		return null;
	}

	/**
	 * Get Clan Hall by Owner
	 */
	public final ClanHall getClanHallByOwner(L2Clan clan) {
		for (Map.Entry<Integer, ClanHall> ch : clanHall.entrySet()) {
			if (clan.getClanId() == ch.getValue().getOwnerId()) {
				return ch.getValue();
			}
		}
		return null;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final ClanHallManager instance = new ClanHallManager();
	}
}
