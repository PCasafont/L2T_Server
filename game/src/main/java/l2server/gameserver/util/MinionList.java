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

package l2server.gameserver.util;

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.L2MinionData;
import l2server.gameserver.model.L2RandomMinionData;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author luisantonioa, DS
 */
public class MinionList {
	private static Logger log = LoggerFactory.getLogger(MinionList.class.getName());

	private final MonsterInstance master;
	/**
	 * List containing the current spawned minions
	 */
	private final List<MonsterInstance> minionReferences;
	/**
	 * List containing the cached deleted minions for reuse
	 */
	private List<MonsterInstance> reusedMinionReferences = null;

	private int spawnedMinions;

	public MinionList(MonsterInstance pMaster) {
		if (pMaster == null) {
			throw new NullPointerException("MinionList: master is null");
		}

		master = pMaster;
		minionReferences = new CopyOnWriteArrayList<>();
	}

	/**
	 * Returns list of the spawned (alive) minions.
	 */
	public List<MonsterInstance> getSpawnedMinions() {
		return minionReferences;
	}

	/**
	 * Manage the spawn of Minions.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the Minion data of all Minions that must be spawn </li>
	 * <li>For each Minion type, spawn the amount of Minion needed </li><BR><BR>
	 */
	public final void spawnMinions() {
		if (master.isAlikeDead()) {
			return;
		}

		int minionCount, minionId, minionsToSpawn;

		L2RandomMinionData randomMinions = master.getTemplate().getRandomMinionData();
		if (randomMinions != null) {
			List<Integer> lastMinions = randomMinions.getLastSpawnedMinionIds();
			boolean isFirstSpawn = true;
			for (int i = 0; i < randomMinions.getAmount(); i++) {
				if (isFirstSpawn) {
					int randMinion = randomMinions.getMinionIds().get(Rnd.get(randomMinions.getMinionIds().size()));
					spawnMinion(randMinion);
					randomMinions.addLastSpawnedMinionId(randMinion);
				} else {
					int randMinion = lastMinions.get(i);
					spawnMinion(randMinion);
				}
			}

			// remove non-needed minions
			deleteReusedMinions();
		}

		List<L2MinionData> minions = master.getTemplate().getMinionData();

		if (minions != null) {
			for (L2MinionData minion : minions) {
				minionCount = minion.getAmount();
				minionId = minion.getMinionId();

				minionsToSpawn = minionCount - countSpawnedMinionsById(minionId);
				if (minionsToSpawn > 0) {
					for (int i = 0; i < minionsToSpawn; i++) {
						spawnMinion(minionId);
					}
				}
			}
			// remove non-needed minions
			deleteReusedMinions();
		}
	}

	/**
	 * Delete all spawned minions and try to reuse them.
	 */
	public void deleteSpawnedMinions() {
		//Broadcast.toGameMasters("Deleting Spawned Minions.. ");
		if (!minionReferences.isEmpty()) {
			for (MonsterInstance minion : minionReferences) {
				if (minion != null) {
					//Broadcast.toGameMasters("Deleting Spawned Minion... " + minion.getName());
					if (!minion.isDead()) {
						minion.setLeader(null);
						minion.abortAttack();
						minion.abortCast();
						minion.deleteMe();
					}

					if (reusedMinionReferences != null) {
						reusedMinionReferences.add(minion);
					}
				}
			}
			minionReferences.clear();
		}

		spawnedMinions = 0;
	}

	/**
	 * Delete all reused minions to prevent memory leaks.
	 */
	public void deleteReusedMinions() {
		if (reusedMinionReferences != null) {
			reusedMinionReferences.clear();
		}
	}

	// hooks

	/**
	 * Called on the master spawn
	 * Old minions (from previous spawn) are deleted.
	 * If master can respawn - enabled reuse of the killed minions.
	 */
	public void onMasterSpawn() {
		deleteSpawnedMinions();

		// if master has spawn and can respawn - try to reuse minions
		if (reusedMinionReferences == null && (master.getTemplate().getMinionData() != null || master.getTemplate().getRandomMinionData() != null) &&
				master.getSpawn() != null && master.getSpawn().isRespawnEnabled()) {
			reusedMinionReferences = new ArrayList<>();
		}
	}

	/**
	 * Called on the minion spawn
	 * and added them in the list of the spawned minions.
	 */
	public void onMinionSpawn(MonsterInstance minion) {
		minionReferences.add(minion);
	}

	/**
	 * Called on the master death/delete.
	 *
	 * @param force if true - force delete of the spawned minions
	 *              By default minions deleted only for raidbosses
	 */
	public void onMasterDie(boolean force) {
		//Broadcast.toGameMasters("On master die... " + force);
		if (master.isRaid() || force) {
			deleteSpawnedMinions();
		}
	}

	/**
	 * Called on the minion death/delete.
	 * Removed minion from the list of the spawned minions and reuse if possible.
	 *
	 * @param respawnTime (ms) enable respawning of this minion while master is alive.
	 *                    -1 - use default value: 0 (disable) for mobs and config value for raids.
	 */
	public void onMinionDie(MonsterInstance minion, int respawnTime) {
		minion.setLeader(null); // prevent memory leaks
		minionReferences.remove(minion);
		if (reusedMinionReferences != null) {
			reusedMinionReferences.add(minion);
		}

		int time = respawnTime < 0 ? master.isRaid() ? (int) Config.RAID_MINION_RESPAWN_TIMER : 0 : respawnTime;

		L2MinionData minionData = null;

		if (master.getTemplate().getMinionData() != null) {
			minionData = master.getTemplate().getMinionData(minion.getNpcId());
		}

		//Broadcast.toGameMasters("OnMinionDie....");
		//Broadcast.toGameMasters(minionData + ", " + (minionData != null ? minionData.getRespawnTime() : ""));
		if (minionData != null && minionData.getRespawnTime() != 0) {
			time = minionData.getRespawnTime() * 1000;
			//Broadcast.toGameMasters("Will respawn in ...." + time);
		}

		if (time > 0 && !master.isAlikeDead() && minionData != null) {
			if (minionData.getMaxRespawn() == 0 || ++spawnedMinions < minionData.getMaxRespawn()) {
				ThreadPoolManager.getInstance().scheduleGeneral(new MinionRespawnTask(minion), time);
			}
		}
	}

	/**
	 * Called if master/minion was attacked.
	 * Master and all free minions receive aggro against attacker.
	 */
	public void onAssist(Creature caller, Creature attacker) {
		if (attacker == null) {
			return;
		}

		if (!master.isAlikeDead() && !master.isInCombat()) {
			master.addDamageHate(attacker, 0, 1);
		}

		final boolean callerIsMaster = caller == master;
		int aggro = callerIsMaster ? 10 : 1;
		if (master.isRaid()) {
			aggro *= 10;
		}

		for (MonsterInstance minion : minionReferences) {
			if (minion != null && !minion.isDead() && (callerIsMaster || !minion.isInCombat())) {
				minion.addDamageHate(attacker, 0, aggro);
			}
		}
	}

	/**
	 * Called from onTeleported() of the master
	 * Alive and able to move minions teleported to master.
	 */
	public void onMasterTeleported() {
		final int offset = 200;
		final int minRadius = (int) master.getCollisionRadius() + 30;

		for (MonsterInstance minion : minionReferences) {
			if (minion != null && !minion.isDead() && !minion.isMovementDisabled()) {
				int newX = Rnd.get(minRadius * 2, offset * 2); // x
				int newY = Rnd.get(newX, offset * 2); // distance
				newY = (int) Math.sqrt(newY * newY - newX * newX); // y
				if (newX > offset + minRadius) {
					newX = master.getX() + newX - offset;
				} else {
					newX = master.getX() - newX + minRadius;
				}
				if (newY > offset + minRadius) {
					newY = master.getY() + newY - offset;
				} else {
					newY = master.getY() - newY + minRadius;
				}

				minion.teleToLocation(newX, newY, master.getZ());
			}
		}
	}

	private void spawnMinion(int minionId) {
		if (minionId == 0) {
			return;
		}

		// searching in reused minions
		if (reusedMinionReferences != null && !reusedMinionReferences.isEmpty()) {
			MonsterInstance minion;
			Iterator<MonsterInstance> iter = reusedMinionReferences.iterator();
			while (iter.hasNext()) {
				minion = iter.next();
				if (minion != null && minion.getNpcId() == minionId) {
					iter.remove();
					minion.refreshID();
					initializeMinion(master, minion);
					return;
				}
			}
		}
		// not found in cache
		spawnMinion(master, minionId);
	}

	private final class MinionRespawnTask implements Runnable {
		private final MonsterInstance minion;

		public MinionRespawnTask(MonsterInstance minion) {
			this.minion = minion;
		}

		@Override
		public void run() {
			if (!master.isAlikeDead() && master.isVisible()) {
				// minion can be already spawned or deleted
				if (!minion.isVisible()) {
					if (reusedMinionReferences != null) {
						reusedMinionReferences.remove(minion);
					}

					minion.refreshID();
					initializeMinion(master, minion);
				}
			}
		}
	}

	/**
	 * Init a Minion and add it in the world as a visible object.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the template of the Minion to spawn </li>
	 * <li>Create and Init the Minion and generate its Identifier </li>
	 * <li>Set the Minion HP, MP and Heading </li>
	 * <li>Set the Minion leader to this RaidBoss </li>
	 * <li>Init the position of the Minion and add it in the world as a visible object </li><BR><BR>
	 *
	 * @param master   MonsterInstance used as master for this minion
	 * @param minionId The NpcTemplate Identifier of the Minion to spawn
	 */
	public static MonsterInstance spawnMinion(MonsterInstance master, int minionId) {
		// Get the template of the Minion to spawn
		NpcTemplate minionTemplate = NpcTable.getInstance().getTemplate(minionId);
		if (minionTemplate == null) {
			return null;
		}

		if (Config.isServer(Config.TENKAI_LEGACY) && minionTemplate.Level + 5 < master.getLevel()) {
			minionTemplate.Level = (byte) master.getLevel();
		}

		// Create and Init the Minion and generate its Identifier
		MonsterInstance minion = new MonsterInstance(IdFactory.getInstance().getNextId(), minionTemplate);
		return initializeMinion(master, minion);
	}

	private static MonsterInstance initializeMinion(MonsterInstance master, MonsterInstance minion) {
		minion.stopAllEffects();
		minion.setIsDead(false);
		minion.setDecayed(false);

		// Set the Minion HP, MP and Heading
		minion.setCurrentHpMp(minion.getMaxHp(), minion.getMaxMp());
		minion.setHeading(master.getHeading());

		// Set the Minion leader to this RaidBoss
		minion.setLeader(master);

		//move monster to masters instance
		minion.setInstanceId(master.getInstanceId());

		// Init the position of the Minion and add it in the world as a visible object
		final int offset = 200;
		final int minRadius = (int) master.getCollisionRadius() + 30;

		int newX = Rnd.get(minRadius * 2, offset * 2); // x
		int newY = Rnd.get(newX, offset * 2); // distance
		newY = (int) Math.sqrt(newY * newY - newX * newX); // y
		if (newX > offset + minRadius) {
			newX = master.getX() + newX - offset;
		} else {
			newX = master.getX() - newX + minRadius;
		}
		if (newY > offset + minRadius) {
			newY = master.getY() + newY - offset;
		} else {
			newY = master.getY() - newY + minRadius;
		}

		minion.spawnMe(newX, newY, master.getZ());

		if (Config.DEBUG) {
			log.debug("Spawned minion template " + minion.getNpcId() + " with objid: " + minion.getObjectId() + " to boss " + master.getObjectId() +
					" ,at: " + minion.getX() + " x, " + minion.getY() + " y, " + minion.getZ() + " z");
		}

		return minion;
	}

	// Statistics part

	private int countSpawnedMinionsById(int minionId) {
		int count = 0;
		for (MonsterInstance minion : minionReferences) {
			if (minion != null && minion.getNpcId() == minionId) {
				count++;
			}
		}
		return count;
	}

	public final int countSpawnedMinions() {
		return minionReferences.size();
	}

	public final int lazyCountSpawnedMinionsGroups() {
		Set<Integer> seenGroups = new HashSet<>();
		for (MonsterInstance minion : minionReferences) {
			if (minion == null) {
				continue;
			}

			seenGroups.add(minion.getNpcId());
		}
		return seenGroups.size();
	}
}
