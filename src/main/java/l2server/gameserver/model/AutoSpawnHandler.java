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

package l2server.gameserver.model;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.log.Log;
import l2server.util.Rnd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Auto Spawn Handler
 * <p>
 * Allows spawning of a NPC object based on a timer. (From the official idea
 * used for the Merchant and Blacksmith of Mammon)
 * <p>
 * General Usage: - Call registerSpawn() with the parameters listed below. int
 * npcId int[][] spawnPoints or specify NULL to add points later. int
 * initialDelay (If < 0 = default value) int respawnDelay (If < 0 = default
 * value) int despawnDelay (If < 0 = default value or if = 0, function disabled)
 * <p>
 * spawnPoints is a standard two-dimensional int array containing X,Y and Z
 * coordinates. The default respawn/despawn delays are currently every hour (as
 * for Mammon on official servers).
 * - The resulting AutoSpawnInstance object represents the newly added spawn
 * index. - The interal methods of this object can be used to adjust random
 * spawning, for instance a call to setRandomSpawn(1, true); would set the spawn
 * at index 1 to be randomly rather than sequentially-based. - Also they can be
 * used to specify the number of NPC instances to spawn using setSpawnCount(),
 * and broadcast a message to all users using setBroadcast().
 * <p>
 * Random Spawning = OFF by default Broadcasting = OFF by default
 *
 * @author Tempy
 */
public class AutoSpawnHandler {
	
	private static final int DEFAULT_INITIAL_SPAWN = 30000; // 30 seconds after registration
	private static final int DEFAULT_RESPAWN = 3600000; // 1 hour in millisecs
	private static final int DEFAULT_DESPAWN = 3600000; // 1 hour in millisecs
	
	protected Map<Integer, AutoSpawnInstance> registeredSpawns;
	protected Map<Integer, ScheduledFuture<?>> runningSpawns;
	
	protected boolean activeState = true;
	
	private AutoSpawnHandler() {
		registeredSpawns = new HashMap<>();
		runningSpawns = new HashMap<>();
		
		restoreSpawnData();
	}
	
	public static AutoSpawnHandler getInstance() {
		return SingletonHolder.instance;
	}
	
	public final int size() {
		return registeredSpawns.size();
	}
	
	public void reload() {
		// stop all timers
		runningSpawns.values().stream().filter(sf -> sf != null).forEach(sf -> sf.cancel(true));
		// unregister all registered spawns
		registeredSpawns.values().stream().filter(asi -> asi != null).forEach(this::removeSpawn);
		
		// create clean list
		registeredSpawns = new HashMap<>();
		runningSpawns = new HashMap<>();
		
		// load
		restoreSpawnData();
	}
	
	private void restoreSpawnData() {
		int numLoaded = 0;
		Connection con = null;
		
		try {
			PreparedStatement statement = null;
			PreparedStatement statement2 = null;
			ResultSet rs = null;
			ResultSet rs2 = null;
			
			con = L2DatabaseFactory.getInstance().getConnection();
			
			// Restore spawn group data, then the location data.
			statement = con.prepareStatement("SELECT * FROM random_spawn ORDER BY groupId ASC");
			rs = statement.executeQuery();
			
			statement2 = con.prepareStatement("SELECT * FROM random_spawn_loc WHERE groupId=?");
			while (rs.next()) {
				// Register random spawn group, set various options on the
				// created spawn instance.
				AutoSpawnInstance spawnInst =
						registerSpawn(rs.getInt("npcId"), rs.getInt("initialDelay"), rs.getInt("respawnDelay"), rs.getInt("despawnDelay"));
				
				spawnInst.setBroadcast(rs.getBoolean("broadcastSpawn"));
				spawnInst.setRandomSpawn(rs.getBoolean("randomSpawn"));
				numLoaded++;
				
				// Restore the spawn locations for this spawn group/instance.
				statement2.setInt(1, rs.getInt("groupId"));
				rs2 = statement2.executeQuery();
				statement2.clearParameters();
				
				while (rs2.next()) {
					// Add each location to the spawn group/instance.
					spawnInst.addSpawnLocation(rs2.getInt("x"), rs2.getInt("y"), rs2.getInt("z"), rs2.getInt("heading"));
				}
				rs2.close();
			}
			statement2.close();
			rs.close();
			statement.close();
			
			if (Config.DEBUG) {
				Log.info("AutoSpawnHandler: Loaded " + numLoaded + " spawn group(s) from the database.");
			}
		} catch (Exception e) {
			Log.log(Level.WARNING, "AutoSpawnHandler: Could not restore spawn data: " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}
	
	/**
	 * Registers a spawn with the given parameters with the spawner, and marks
	 * it as active. Returns a AutoSpawnInstance containing info about the
	 * spawn.
	 *
	 * @return AutoSpawnInstance spawnInst
	 */
	public AutoSpawnInstance registerSpawn(int npcId, int[][] spawnPoints, int initialDelay, int respawnDelay, int despawnDelay) {
		if (initialDelay < 0) {
			initialDelay = DEFAULT_INITIAL_SPAWN;
		}
		
		if (respawnDelay < 0) {
			respawnDelay = DEFAULT_RESPAWN;
		}
		
		if (despawnDelay < 0) {
			despawnDelay = DEFAULT_DESPAWN;
		}
		
		AutoSpawnInstance newSpawn = new AutoSpawnInstance(npcId, initialDelay, respawnDelay, despawnDelay);
		
		if (spawnPoints != null) {
			for (int[] spawnPoint : spawnPoints) {
				newSpawn.addSpawnLocation(spawnPoint);
			}
		}
		
		int newId = IdFactory.getInstance().getNextId();
		newSpawn.objectId = newId;
		registeredSpawns.put(newId, newSpawn);
		
		setSpawnActive(newSpawn, true);
		
		if (Config.DEBUG) {
			Log.info("AutoSpawnHandler: Registered auto spawn for NPC ID " + npcId + " (Object ID = " + newId + ").");
		}
		
		return newSpawn;
	}
	
	/**
	 * Registers a spawn with the given parameters with the spawner, and marks
	 * it as active. Returns a AutoSpawnInstance containing info about the
	 * spawn. <BR>
	 * <B>Warning:</B> Spawn locations must be specified separately using
	 * addSpawnLocation().
	 *
	 * @return AutoSpawnInstance spawnInst
	 */
	public AutoSpawnInstance registerSpawn(int npcId, int initialDelay, int respawnDelay, int despawnDelay) {
		return registerSpawn(npcId, null, initialDelay, respawnDelay, despawnDelay);
	}
	
	/**
	 * Remove a registered spawn from the list, specified by the given spawn
	 * instance.
	 *
	 * @return boolean removedSuccessfully
	 */
	public boolean removeSpawn(AutoSpawnInstance spawnInst) {
		if (!isSpawnRegistered(spawnInst)) {
			return false;
		}
		
		try {
			// Try to remove from the list of registered spawns if it exists.
			registeredSpawns.remove(spawnInst.getNpcId());
			
			// Cancel the currently associated running scheduled task.
			ScheduledFuture<?> respawnTask = runningSpawns.remove(spawnInst.objectId);
			respawnTask.cancel(false);
			
			if (Config.DEBUG) {
				Log.info("AutoSpawnHandler: Removed auto spawn for NPC ID " + spawnInst.npcId + " (Object ID = " + spawnInst.objectId + ").");
			}
		} catch (Exception e) {
			Log.log(Level.WARNING,
					"AutoSpawnHandler: Could not auto spawn for NPC ID " + spawnInst.npcId + " (Object ID = " + spawnInst.objectId + "): " +
							e.getMessage(),
					e);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Remove a registered spawn from the list, specified by the given spawn
	 * object ID.
	 *
	 * @return boolean removedSuccessfully
	 */
	public void removeSpawn(int objectId) {
		removeSpawn(registeredSpawns.get(objectId));
	}
	
	/**
	 * Sets the active state of the specified spawn.
	 */
	public void setSpawnActive(AutoSpawnInstance spawnInst, boolean isActive) {
		if (spawnInst == null) {
			return;
		}
		
		int objectId = spawnInst.objectId;
		
		if (isSpawnRegistered(objectId)) {
			ScheduledFuture<?> spawnTask = null;
			
			if (isActive) {
				AutoSpawner rs = new AutoSpawner(objectId);
				
				if (spawnInst.desDelay > 0) {
					spawnTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(rs, spawnInst.initDelay, spawnInst.resDelay);
				} else {
					spawnTask = ThreadPoolManager.getInstance().scheduleEffect(rs, spawnInst.initDelay);
				}
				
				runningSpawns.put(objectId, spawnTask);
			} else {
				AutoDespawner rd = new AutoDespawner(objectId);
				spawnTask = runningSpawns.remove(objectId);
				
				if (spawnTask != null) {
					spawnTask.cancel(false);
				}
				
				ThreadPoolManager.getInstance().scheduleEffect(rd, 0);
			}
			
			spawnInst.setSpawnActive(isActive);
		}
	}
	
	/**
	 * Sets the active state of all auto spawn instances to that specified, and
	 * cancels the scheduled spawn task if necessary.
	 */
	public void setAllActive(boolean isActive) {
		if (activeState == isActive) {
			return;
		}
		
		for (AutoSpawnInstance spawnInst : registeredSpawns.values()) {
			setSpawnActive(spawnInst, isActive);
		}
		
		activeState = isActive;
	}
	
	/**
	 * Returns the number of milliseconds until the next occurrance of the given
	 * spawn.
	 */
	public final long getTimeToNextSpawn(AutoSpawnInstance spawnInst) {
		int objectId = spawnInst.getObjectId();
		
		if (!isSpawnRegistered(objectId)) {
			return -1;
		}
		
		return runningSpawns.get(objectId).getDelay(TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Attempts to return the AutoSpawnInstance associated with the given NPC or
	 * Object ID type. <BR>
	 * Note: If isObjectId == false, returns first instance for the specified
	 * NPC ID.
	 *
	 * @return AutoSpawnInstance spawnInst
	 */
	public final AutoSpawnInstance getAutoSpawnInstance(int id, boolean isObjectId) {
		if (isObjectId) {
			if (isSpawnRegistered(id)) {
				return registeredSpawns.get(id);
			}
		} else {
			for (AutoSpawnInstance spawnInst : registeredSpawns.values()) {
				if (spawnInst.getNpcId() == id) {
					return spawnInst;
				}
			}
		}
		return null;
	}
	
	public Map<Integer, AutoSpawnInstance> getAutoSpawnInstances(int npcId) {
		Map<Integer, AutoSpawnInstance> spawnInstList = new HashMap<>();
		
		registeredSpawns.values()
				.stream()
				.filter(spawnInst -> spawnInst.getNpcId() == npcId)
				.forEach(spawnInst -> spawnInstList.put(spawnInst.getObjectId(), spawnInst));
		
		return spawnInstList;
	}
	
	/**
	 * Tests if the specified object ID is assigned to an auto spawn.
	 *
	 * @return boolean isAssigned
	 */
	public final boolean isSpawnRegistered(int objectId) {
		return registeredSpawns.containsKey(objectId);
	}
	
	/**
	 * Tests if the specified spawn instance is assigned to an auto spawn.
	 *
	 * @return boolean isAssigned
	 */
	public final boolean isSpawnRegistered(AutoSpawnInstance spawnInst) {
		return registeredSpawns.containsValue(spawnInst);
	}
	
	/**
	 * AutoSpawner Class <BR>
	 * <BR>
	 * This handles the main spawn task for an auto spawn instance, and
	 * initializes a despawner if required.
	 *
	 * @author Tempy
	 */
	private class AutoSpawner implements Runnable {
		private int objectId;
		
		protected AutoSpawner(int objectId) {
			this.objectId = objectId;
		}
		
		@Override
		public void run() {
			try {
				// Retrieve the required spawn instance for this spawn task.
				AutoSpawnInstance spawnInst = registeredSpawns.get(objectId);
				
				// If the spawn is not scheduled to be active, cancel the spawn
				// task.
				if (!spawnInst.isSpawnActive()) {
					return;
				}
				
				Location[] locationList = spawnInst.getLocationList();
				
				// If there are no set co-ordinates, cancel the spawn task.
				if (locationList.length == 0) {
					Log.info("AutoSpawnHandler: No location co-ords specified for spawn instance (Object ID = " + objectId + ").");
					return;
				}
				
				int locationCount = locationList.length;
				int locationIndex = Rnd.nextInt(locationCount);
				
				/*
				 * If random spawning is disabled, the spawn at the next set of
				 * co-ordinates after the last. If the index is greater than the
				 * number of possible spawns, reset the counter to zero.
				 */
				if (!spawnInst.isRandomSpawn()) {
					locationIndex = spawnInst.lastLocIndex + 1;
					
					if (locationIndex == locationCount) {
						locationIndex = 0;
					}
					
					spawnInst.lastLocIndex = locationIndex;
				}
				
				// Set the X, Y and Z co-ordinates, where this spawn will take
				// place.
				final int x = locationList[locationIndex].getX();
				final int y = locationList[locationIndex].getY();
				final int z = locationList[locationIndex].getZ();
				final int heading = locationList[locationIndex].getHeading();
				
				// Fetch the template for this NPC ID and create a new spawn.
				L2NpcTemplate npcTemp = NpcTable.getInstance().getTemplate(spawnInst.getNpcId());
				if (npcTemp == null) {
					Log.warning("Couldn't find NPC #" + spawnInst.getNpcId() + ". Try to update your DP");
					return;
				}
				L2Spawn newSpawn = new L2Spawn(npcTemp);
				
				newSpawn.setX(x);
				newSpawn.setY(y);
				newSpawn.setZ(z);
				if (heading != -1) {
					newSpawn.setHeading(heading);
				}
				if (spawnInst.desDelay == 0) {
					newSpawn.setRespawnDelay(spawnInst.resDelay);
				}
				
				// Add the new spawn information to the spawn table, but do not
				// store it.
				SpawnTable.getInstance().addNewSpawn(newSpawn, false);
				L2Npc npcInst = newSpawn.getNpc();
				newSpawn.doSpawn();
				spawnInst.addNpcInstance(npcInst);
				
				String nearestTown = MapRegionTable.getInstance().getClosestTownName(npcInst);
				
				if (Config.DEBUG) {
					Log.info("AutoSpawnHandler: Spawned NPC ID " + spawnInst.getNpcId() + " at " + x + ", " + y + ", " + z + " (Near " + nearestTown +
							") for " + spawnInst.getRespawnDelay() / 60000 + " minute(s).");
				}
				
				// If there is no despawn time, do not create a despawn task.
				if (spawnInst.getDespawnDelay() > 0) {
					AutoDespawner rd = new AutoDespawner(objectId);
					ThreadPoolManager.getInstance().scheduleAi(rd, spawnInst.getDespawnDelay() - 1000);
				}
			} catch (Exception e) {
				Log.log(Level.WARNING,
						"AutoSpawnHandler: An error occurred while initializing spawn instance (Object ID = " + objectId + "): " + e.getMessage(),
						e);
			}
		}
	}
	
	/**
	 * AutoDespawner Class <BR>
	 * <BR>
	 * Simply used as a secondary class for despawning an auto spawn instance.
	 *
	 * @author Tempy
	 */
	private class AutoDespawner implements Runnable {
		private int objectId;
		
		protected AutoDespawner(int objectId) {
			this.objectId = objectId;
		}
		
		@Override
		public void run() {
			try {
				AutoSpawnInstance spawnInst = registeredSpawns.get(objectId);
				
				if (spawnInst == null) {
					Log.info("AutoSpawnHandler: No spawn registered for object ID = " + objectId + ".");
					return;
				}
				
				for (L2Npc npcInst : spawnInst.getNPCInstanceList()) {
					if (npcInst == null) {
						continue;
					}
					
					npcInst.deleteMe();
					SpawnTable.getInstance().deleteSpawn(npcInst.getSpawn(), false);
					spawnInst.removeNpcInstance(npcInst);
					
					if (Config.DEBUG) {
						Log.info("AutoSpawnHandler: Spawns removed for spawn instance (Object ID = " + objectId + ").");
					}
				}
			} catch (Exception e) {
				Log.log(Level.WARNING,
						"AutoSpawnHandler: An error occurred while despawning spawn (Object ID = " + objectId + "): " + e.getMessage(),
						e);
			}
		}
	}
	
	/**
	 * AutoSpawnInstance Class <BR>
	 * <BR>
	 * Stores information about a registered auto spawn.
	 *
	 * @author Tempy
	 */
	public static class AutoSpawnInstance {
		protected int objectId;
		
		protected int spawnIndex;
		
		protected int npcId;
		
		protected int initDelay;
		
		protected int resDelay;
		
		protected int desDelay;
		
		protected int lastLocIndex = -1;
		
		private List<L2Npc> npcList = new Vector<>();
		
		private List<Location> locList = new Vector<>();
		
		private boolean spawnActive;
		
		private boolean randomSpawn = false;
		
		private boolean broadcastAnnouncement = false;
		
		protected AutoSpawnInstance(int npcId, int initDelay, int respawnDelay, int despawnDelay) {
			this.npcId = npcId;
			this.initDelay = initDelay;
			resDelay = respawnDelay;
			desDelay = despawnDelay;
		}
		
		protected void setSpawnActive(boolean activeValue) {
			spawnActive = activeValue;
		}
		
		protected boolean addNpcInstance(L2Npc npcInst) {
			return npcList.add(npcInst);
		}
		
		protected boolean removeNpcInstance(L2Npc npcInst) {
			return npcList.remove(npcInst);
		}
		
		public int getObjectId() {
			return objectId;
		}
		
		public int getInitialDelay() {
			return initDelay;
		}
		
		public int getRespawnDelay() {
			return resDelay;
		}
		
		public int getDespawnDelay() {
			return desDelay;
		}
		
		public int getNpcId() {
			return npcId;
		}
		
		public Location[] getLocationList() {
			return locList.toArray(new Location[locList.size()]);
		}
		
		public L2Npc[] getNPCInstanceList() {
			L2Npc[] ret;
			synchronized (npcList) {
				ret = new L2Npc[npcList.size()];
				npcList.toArray(ret);
			}
			
			return ret;
		}
		
		public L2Spawn[] getSpawns() {
			List<L2Spawn> npcSpawns = npcList.stream().map(L2Npc::getSpawn).collect(Collectors.toList());
			
			return npcSpawns.toArray(new L2Spawn[npcSpawns.size()]);
		}
		
		public void setRandomSpawn(boolean randValue) {
			randomSpawn = randValue;
		}
		
		public void setBroadcast(boolean broadcastValue) {
			broadcastAnnouncement = broadcastValue;
		}
		
		public boolean isSpawnActive() {
			return spawnActive;
		}
		
		public boolean isRandomSpawn() {
			return randomSpawn;
		}
		
		public boolean isBroadcasting() {
			return broadcastAnnouncement;
		}
		
		public boolean addSpawnLocation(int x, int y, int z, int heading) {
			return locList.add(new Location(x, y, z, heading));
		}
		
		public boolean addSpawnLocation(int[] spawnLoc) {
			if (spawnLoc.length != 3) {
				return false;
			}
			
			return addSpawnLocation(spawnLoc[0], spawnLoc[1], spawnLoc[2], -1);
		}
		
		public Location removeSpawnLocation(int locIndex) {
			try {
				return locList.remove(locIndex);
			} catch (IndexOutOfBoundsException e) {
				return null;
			}
		}
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final AutoSpawnHandler instance = new AutoSpawnHandler();
	}
}
