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

import l2server.DatabasePool;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.templates.chars.NpcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SiegeGuardManager {
	private static Logger log = LoggerFactory.getLogger(SiegeGuardManager.class.getName());

	// =========================================================
	// Data Field
	private Castle castle;
	private List<L2Spawn> siegeGuardSpawn = new ArrayList<>();

	// =========================================================
	// Constructor
	public SiegeGuardManager(Castle castle) {
		this.castle = castle;
	}

	// =========================================================
	// Method - Public

	/**
	 * Add guard.<BR><BR>
	 */
	public void addSiegeGuard(Player activeChar, int npcId) {
		if (activeChar == null) {
			return;
		}
		addSiegeGuard(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
	}

	/**
	 * Add guard.<BR><BR>
	 */
	public void addSiegeGuard(int x, int y, int z, int heading, int npcId) {
		saveSiegeGuard(x, y, z, heading, npcId, 0);
	}

	/**
	 * Hire merc.<BR><BR>
	 */
	public void hireMerc(Player activeChar, int npcId) {
		if (activeChar == null) {
			return;
		}
		hireMerc(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
	}

	/**
	 * Hire merc.<BR><BR>
	 */
	public void hireMerc(int x, int y, int z, int heading, int npcId) {
		saveSiegeGuard(x, y, z, heading, npcId, 1);
	}

	/**
	 * Remove a single mercenary, identified by the npcId and location.
	 * Presumably, this is used when a castle lord picks up a previously dropped ticket
	 */
	public void removeMerc(int npcId, int x, int y, int z) {
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("DELETE FROM castle_siege_guards WHERE npcId = ? AND x = ? AND y = ? AND z = ? AND isHired = 1");
			statement.setInt(1, npcId);
			statement.setInt(2, x);
			statement.setInt(3, y);
			statement.setInt(4, z);
			statement.execute();
			statement.close();
		} catch (Exception e) {
			log.warn("Error deleting hired siege guard at " + x + ',' + y + ',' + z + ": " + e.getMessage(), e);
		} finally {
			DatabasePool.close(con);
		}
	}

	/**
	 * Remove mercs.<BR><BR>
	 */
	public void removeMercs() {
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM castle_siege_guards WHERE castleId = ? AND isHired = 1");
			statement.setInt(1, getCastle().getCastleId());
			statement.execute();
			statement.close();
		} catch (Exception e) {
			log.warn("Error deleting hired siege guard for castle " + getCastle().getName() + ": " + e.getMessage(), e);
		} finally {
			DatabasePool.close(con);
		}
	}

	/**
	 * Spawn guards.<BR><BR>
	 */
	public void spawnSiegeGuard() {
		try {
			boolean isHired = getCastle().getOwnerId() > 0;
			if (isHired) {
				int hiredCount = 0, hiredMax = MercTicketManager.getInstance().getMaxAllowedMerc(castle.getCastleId());
				loadSiegeGuard();
				for (L2Spawn spawn : getSiegeGuardSpawn()) {
					if (spawn != null) {
						spawn.startRespawn();
						spawn.doSpawn();
						if (isHired) {
							spawn.stopRespawn();
							if (++hiredCount > hiredMax) {
								return;
							}
						}
					}
				}
			} else {
				SpawnTable.getInstance().spawnSpecificTable(getCastle().getName() + "_siege_guards");
			}
		} catch (Exception e) {
			log.error("Error spawning siege guards for castle " + getCastle().getName(), e);
		}
	}

	/**
	 * Unspawn guards.<BR><BR>
	 */
	public void unspawnSiegeGuard() {
		if (getCastle().getOwnerId() > 0) {
			for (L2Spawn spawn : getSiegeGuardSpawn()) {
				if (spawn == null) {
					continue;
				}

				spawn.stopRespawn();
				spawn.getNpc().doDie(spawn.getNpc());
			}

			getSiegeGuardSpawn().clear();
		}
		SpawnTable.getInstance().despawnSpecificTable(getCastle().getName() + "_siege_guards");
	}

	// =========================================================
	// Method - Private

	/**
	 * Load guards.<BR><BR>
	 */
	private void loadSiegeGuard() {
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM castle_siege_guards WHERE castleId = ? AND isHired = 1");
			statement.setInt(1, getCastle().getCastleId());
			ResultSet rs = statement.executeQuery();

			L2Spawn spawn1;
			NpcTemplate template1;

			while (rs.next()) {
				template1 = NpcTable.getInstance().getTemplate(rs.getInt("npcId"));
				if (template1 != null) {
					spawn1 = new L2Spawn(template1);
					spawn1.setX(rs.getInt("x"));
					spawn1.setY(rs.getInt("y"));
					spawn1.setZ(rs.getInt("z"));
					spawn1.setHeading(rs.getInt("heading"));
					spawn1.setRespawnDelay(rs.getInt("respawnDelay"));

					siegeGuardSpawn.add(spawn1);
				} else {
					log.warn("Missing npc data in npc table for id: " + rs.getInt("npcId"));
				}
			}
			statement.close();
		} catch (Exception e) {
			log.warn("Error loading siege guard for castle " + getCastle().getName() + ": " + e.getMessage(), e);
		} finally {
			DatabasePool.close(con);
		}
	}

	/**
	 * Save guards.<BR><BR>
	 */
	private void saveSiegeGuard(int x, int y, int z, int heading, int npcId, int isHire) {
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"INSERT INTO castle_siege_guards (castleId, npcId, x, y, z, heading, respawnDelay, isHired) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			statement.setInt(1, getCastle().getCastleId());
			statement.setInt(2, npcId);
			statement.setInt(3, x);
			statement.setInt(4, y);
			statement.setInt(5, z);
			statement.setInt(6, heading);
			if (isHire == 1) {
				statement.setInt(7, 0);
			} else {
				statement.setInt(7, 600);
			}
			statement.setInt(8, isHire);
			statement.execute();
			statement.close();
		} catch (Exception e) {
			log.warn("Error adding siege guard for castle " + getCastle().getName() + ": " + e.getMessage(), e);
		} finally {
			DatabasePool.close(con);
		}
	}

	// =========================================================
	// Proeprty

	public final Castle getCastle() {
		return castle;
	}

	public final List<L2Spawn> getSiegeGuardSpawn() {
		return siegeGuardSpawn;
	}
}
