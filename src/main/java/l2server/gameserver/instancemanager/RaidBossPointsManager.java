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
import l2server.gameserver.model.actor.instance.Player;
import l2server.util.loader.annotations.Load;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Kerberos
 * JIV update 24.8.10
 */

public class RaidBossPointsManager {
	private static Logger log = LoggerFactory.getLogger(RaidBossPointsManager.class.getName());


	private HashMap<Integer, Map<Integer, Integer>> list;

	private final Comparator<Map.Entry<Integer, Integer>> comparator =
			(entry, entry1) -> entry.getValue().equals(entry1.getValue()) ? 0 : entry.getValue() < entry1.getValue() ? 1 : -1;

	public static RaidBossPointsManager getInstance() {
		return SingletonHolder.instance;
	}

	public RaidBossPointsManager() {
	}

	@Load
	public void init() {
		list = new HashMap<>();
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT `charId`,`boss_id`,`points` FROM `character_raid_points`");
			ResultSet rset = statement.executeQuery();
			while (rset.next()) {
				int charId = rset.getInt("charId");
				int bossId = rset.getInt("boss_id");
				int points = rset.getInt("points");
				Map<Integer, Integer> values = list.get(charId);
				if (values == null) {
					values = new HashMap<>();
					values.put(0, 0);
					list.put(charId, values);
				}
				values.put(bossId, points);
				values.put(0, values.get(0) + points);
			}
			rset.close();
			statement.close();
			log.info(getClass().getSimpleName() + ": Loaded " + list.size() + " Characters Raid Points.");
		} catch (SQLException e) {
			log.warn("RaidPointsManager: Couldnt load raid points ", e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	public final void updatePointsInDB(Player player, int raidId, int points) {
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("REPLACE INTO character_raid_points (`charId`,`boss_id`,`points`) VALUES (?,?,?)");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, raidId);
			statement.setInt(3, points);
			statement.executeUpdate();
			statement.close();
		} catch (Exception e) {
			log.warn("could not update char raid points:", e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	public final void addPoints(Player player, int bossId, int points) {
		if (player == null) {
			return;
		}

		int ownerId = player.getObjectId();
		Map<Integer, Integer> tmpPoint = list.get(ownerId);
		if (tmpPoint == null) {
			tmpPoint = new HashMap<>();
			tmpPoint.put(bossId, points);
			tmpPoint.put(0, 0);
			updatePointsInDB(player, bossId, points);
			updatePointsInDB(player, 0, points);
		} else {
			int currentPoints = tmpPoint.containsKey(bossId) ? tmpPoint.get(bossId) : 0;
			currentPoints += points;
			tmpPoint.put(bossId, currentPoints);
			tmpPoint.put(0, tmpPoint.get(0) + points);
			updatePointsInDB(player, bossId, currentPoints);
			updatePointsInDB(player, 0, tmpPoint.get(0));
		}
		list.put(ownerId, tmpPoint);
	}

	public final int getPointsByOwnerId(int ownerId) {
		Map<Integer, Integer> tmpPoint;
		tmpPoint = list.get(ownerId);

		if (tmpPoint == null || tmpPoint.isEmpty()) {
			return 0;
		}

		return tmpPoint.get(0);
	}

	public final Map<Integer, Integer> getList(Player player) {
		Map<Integer, Integer> list = this.list.get(player.getObjectId());
		if (list == null) {
			list = new HashMap<>();
			list.put(0, 0);
			this.list.put(player.getObjectId(), list);
		}
		return list;
	}

	public final void cleanUp() {
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM character_raid_points WHERE charId > 0");
			statement.executeUpdate();
			statement.close();
			list.clear();
		} catch (Exception e) {
			log.warn("could not clean raid points: ", e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	public final int calculateRanking(int playerObjId) {
		Map<Integer, Integer> rank = getRankList();
		if (rank.containsKey(playerObjId)) {
			return rank.get(playerObjId);
		}
		return 0;
	}

	public Map<Integer, Integer> getRankList() {
		Map<Integer, Integer> tmpRanking = new HashMap<>();
		Map<Integer, Integer> tmpPoints = new HashMap<>();

		for (int ownerId : list.keySet()) {
			int totalPoints = getPointsByOwnerId(ownerId);
			if (totalPoints != 0) {
				tmpPoints.put(ownerId, totalPoints);
			}
		}
		ArrayList<Entry<Integer, Integer>> list = new ArrayList<>(tmpPoints.entrySet());

		Collections.sort(list, comparator);

		int ranking = 1;
		for (Map.Entry<Integer, Integer> entry : list) {
			tmpRanking.put(entry.getKey(), ranking++);
		}

		return tmpRanking;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final RaidBossPointsManager instance = new RaidBossPointsManager();
	}
}
