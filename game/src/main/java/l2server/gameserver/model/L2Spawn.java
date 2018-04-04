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
import l2server.gameserver.GameApplication;
import l2server.gameserver.GeoData;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.instancemanager.SpawnDataManager;
import l2server.gameserver.instancemanager.SpawnDataManager.DbSpawnData;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.ArmyMonsterInstance;
import l2server.gameserver.model.actor.instance.ChessPieceInstance;
import l2server.gameserver.model.actor.instance.EventGolemInstance;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class manages the spawn and respawn of a NpcInstance.
 * <p>
 * <B><U> Concept</U> :</B><BR><BR>
 * NpcInstance can be spawned either in a random position into a location area (if Lox=0 and Locy=0), either at an exact position.
 * The heading of the NpcInstance can be a random heading if not defined (value= -1) or an exact heading (ex : merchant...).<BR><BR>
 *
 * @author Nightmare
 * @version $Revision: 1.9.2.3.2.8 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2Spawn {
	private static Logger log = LoggerFactory.getLogger(GameApplication.class.getName());
	/**
	 * The link on the NpcTemplate object containing generic and static properties of this spawn (ex : RewardExp, RewardSP, AggroRange...)
	 */
	private final Npc npc;

	/**
	 * The location area where NpcInstance can be spawned
	 */
	private SpawnGroup group;

	/**
	 * Is it spawned currently?
	 */
	private boolean spawned = false;

	/**
	 * Is it scheduled for respawn?
	 */
	protected boolean scheduled = false;

	/**
	 * The X position of the spwan point
	 */
	private int locX;

	/**
	 * The Y position of the spwan point
	 */
	private int locY;

	/**
	 * The Z position of the spwan point
	 */
	private int locZ;

	/**
	 * The heading of NpcInstance when they are spawned
	 */
	private int heading;

	/**
	 * The possible coords in which to spawn [x, y, z, chance]
	 */
	List<int[]> randomCoords;

	/**
	 * The delay between a NpcInstance remove and its re-spawn
	 */
	private int respawnDelay;

	/**
	 * Random delay RaidBoss
	 */
	private int randomRespawnDelay;

	private int instanceId = 0;
	@SuppressWarnings("unused")
	private int dimensionId = 0;

	/**
	 * If True a NpcInstance is respawned each time that another is killed
	 */
	private boolean doRespawn = false;

	/**
	 * If it has a value, the npc data will be saved
	 */
	private String dbName = null;

	private long nextRespawn = 0L;

	private static final List<SpawnListener> spawnListeners = new ArrayList<>();

	/**
	 * The task launching the function doSpawn()
	 */
	class SpawnTask implements Runnable {
		@Override
		public void run() {
			try {
				respawnNpc();
			} catch (Exception e) {
				log.warn("", e);
			}

			scheduled = false;
		}
	}

	public L2Spawn(NpcTemplate template)
			throws SecurityException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		if (template == null) {
			throw new IllegalArgumentException("Null template!");
		}

		// Create the generic constructor of NpcInstance managed by this L2Spawn
		Class<?>[] parameters = {int.class, Class.forName("l2server.gameserver.templates.chars.NpcTemplate")};

		Class<?> instanceClass = getClassFor(template.Type, false);

		// Couldn't get the NPC class from default NpcTypes. Must be custom.
		if (instanceClass == null) {
			instanceClass = getClassFor(template.Type, true);
		}

		Constructor<?> constructor = instanceClass.getConstructor(parameters);

		// Get NpcInstance Init parameters and its generate an Identifier
		Object[] params = {IdFactory.getInstance().getNextId(), template};

		// Call the constructor of the NpcInstance
		// (can be a ArtefactInstance, FriendlyMobInstance, GuardInstance, MonsterInstance, L2SiegeGuardInstance, L2BoxInstance,
		// FeedableBeastInstance, TamedBeastInstance, L2FolkInstance or L2InstancedEventNpcInstance)
		Object tmp = constructor.newInstance(params);
		// Check if the Instance is a NpcInstance
		if (!(tmp instanceof Npc)) {
			throw new IllegalArgumentException("Trying to create a spawn of a non NPC object!");
		}

		npc = (Npc) tmp;

		// Link the NpcInstance to this L2Spawn
		npc.setSpawn(this);
		npc.setIsDead(true);

		template.onSpawn(this);
	}

	/**
	 * Decrease the current number of NpcInstance of this L2Spawn and if necessary create a SpawnTask to launch after the respawn Delay.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Decrease the current number of NpcInstance of this L2Spawn </li>
	 * <li>Check if respawn is possible to prevent multiple respawning caused by lag </li>
	 * <li>Update the current number of SpawnTask in progress or stand by of this L2Spawn </li>
	 * <li>Create a new SpawnTask to launch after the respawn Delay </li><BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : A respawn is possible ONLY if doRespawn=True and scheduledCount + currentCount < maximumCount</B></FONT><BR><BR>
	 */
	public void onDecay(Npc oldNpc) {
		// sanity check
		if (!spawned) {
			return;
		}

		// Mark the spawn as not spawned
		spawned = false;

		// Check if respawn is possible to prevent multiple respawning caused by lag
		if (doRespawn && !scheduled) {
			// Update the current number of SpawnTask in progress or stand by of this L2Spawn
			scheduled = true;

			int respawnDelay = this.respawnDelay + Rnd.get(randomRespawnDelay);
			// Create a new SpawnTask to launch after the respawn Delay
			ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(), respawnDelay);
			nextRespawn = System.currentTimeMillis() + respawnDelay;

			if (dbName != null && !dbName.isEmpty()) {
				SpawnDataManager.getInstance().updateDbSpawnData(this);
			}
		} else {
			npc.getTemplate().onUnSpawn(this);
		}
	}

	/**
	 * Return true if respawn enabled
	 */
	public boolean isRespawnEnabled() {
		return doRespawn;
	}

	/**
	 * Set doRespawn to False to stop respawn in thios L2Spawn.<BR><BR>
	 */
	public void stopRespawn() {
		doRespawn = false;
	}

	/**
	 * Set doRespawn to True to start or restart respawn in this L2Spawn.<BR><BR>
	 */
	public void startRespawn() {
		doRespawn = true;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getDbName() {
		return dbName;
	}

	public boolean doSpawn() {
		return doSpawn(false);
	}

	public boolean doSpawn(boolean isSummonSpawn) {
		if (spawned) {
			return false;
		}

		boolean temp = npc.isShowSummonAnimation();
		npc.setShowSummonAnimation(isSummonSpawn);

		boolean handled = false;
		if (dbName != null && !dbName.isEmpty()) {
			DbSpawnData dbsd = SpawnDataManager.getInstance().popDbSpawnData(dbName);
			if (dbsd != null) {
				long respawnTime = dbsd.respawnTime;
				long time = System.currentTimeMillis();
				if (respawnTime > time) {
					long spawnTime = respawnTime - time;
					ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(), spawnTime);
					nextRespawn = System.currentTimeMillis() + spawnTime;
				} else {
					initializeNpc();
					if (respawnTime == 0) {
						npc.setCurrentHp(dbsd.currentHp);
						npc.setCurrentMp(dbsd.currentMp);
					}
				}

				handled = true;
			}
		}

		if (!handled) {
			initializeNpc();
		}

		npc.setShowSummonAnimation(temp);
		spawned = true;
		return true;
	}

	private void initializeNpc() {
		int newlocx, newlocy, newlocz;

		// If Locx=0 and Locy=0, the NpcInstance must be spawned in an area defined by location
		if (getGroup() != null) {
			// Calculate the random position in the location area
			int p[] = getGroup().getRandomPoint();

			// Set the calculated position of the NpcInstance
			newlocx = p[0];
			newlocy = p[1];
			newlocz = GeoData.getInstance().getSpawnHeight(newlocx, newlocy, p[2], p[3], this);

			// Just for the cuteness
			setX(newlocx);
			setY(newlocy);
			setZ(newlocz);
		} else {
			if (randomCoords != null) {
				int rnd = Rnd.get(100);
				int cumul = 0;
				for (int[] coord : randomCoords) {
					cumul += coord[4];
					if (cumul > rnd) {
						setX(coord[0]);
						setY(coord[1]);
						setZ(coord[2]);
						setHeading(coord[3]);
						break;
					}
				}
			}
			// The NpcInstance is spawned at the exact position (Lox, Locy, Locz)
			newlocx = getX();
			newlocy = getY();
			if (Config.GEODATA > 0) {
				newlocz = GeoData.getInstance().getSpawnHeight(newlocx, newlocy, getZ(), getZ(), this);
			} else {
				newlocz = getZ();
			}
		}

		npc.stopAllEffects();

		npc.setInstanceId(instanceId);
		npc.setIsDead(false);
		// Reset decay info
		npc.setDecayed(false);
		// Set the HP and MP of the NpcInstance to the max
		npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());

		// Set the heading of the NpcInstance (random heading if not defined)
		if (getHeading() == -1) {
			npc.setHeading(Rnd.nextInt(61794));
		} else {
			npc.setHeading(getHeading());
		}

		if (npc instanceof Attackable) {
			((Attackable) npc).setChampion(false);
		}

		if (Config.L2JMOD_CHAMPION_ENABLE) {
			// Set champion on next spawn
			if (npc instanceof MonsterInstance && !getTemplate().isQuestMonster && getTemplate().canBeChampion && !npc.isRaid() &&
					!npc.isRaidMinion() && !(npc instanceof ArmyMonsterInstance) && !(npc instanceof ChessPieceInstance) &&
					!(npc instanceof EventGolemInstance) && getNpcId() != 44000 && Config.L2JMOD_CHAMPION_FREQUENCY > 0 &&
					npc.getLevel() >= Config.L2JMOD_CHAMP_MIN_LVL && npc.getLevel() <= Config.L2JMOD_CHAMP_MAX_LVL &&
					(Config.L2JMOD_CHAMPION_ENABLE_IN_INSTANCES || getInstanceId() == 0)) {
				int random = Rnd.get(100);

				if (random < Config.L2JMOD_CHAMPION_FREQUENCY) {
					((Attackable) npc).setChampion(true);
				}
			}
		}

		// Init other values of the NpcInstance (ex : from its CreatureTemplate for INT, STR, DEX...) and add it in the world as a visible object
		npc.spawnMe(newlocx, newlocy, newlocz);

		L2Spawn.notifyNpcSpawned(npc);

		if (Config.DEBUG) {
			log.trace("spawned Mob ID: " + npc.getNpcId() + " ,at: " + npc.getX() + " x, " + npc.getY() + " y, " + npc.getZ() + " z");
		}
	}

	private Class<?> getClassFor(final String templateType, final boolean custom) {
		Class<?> instanceClass = null;

		try {
			instanceClass = Class.forName("l2server.gameserver.model.actor.instance." + (custom ? "custom." : "") + templateType + "Instance");
		} catch (ClassNotFoundException e) {
			// Let us know only once we tried both default/custom...
			if (custom) {
				e.printStackTrace();
			}
		}

		return instanceClass;
	}

	/**
	 * Return the Identifier of the location area where NpcInstance can be spwaned.<BR><BR>
	 */
	public SpawnGroup getGroup() {
		return group;
	}

	/**
	 * Return the X position of the spwan point.<BR><BR>
	 */
	public int getX() {
		return locX;
	}

	/**
	 * Return the Y position of the spwan point.<BR><BR>
	 */
	public int getY() {
		return locY;
	}

	/**
	 * Return the Z position of the spwan point.<BR><BR>
	 */
	public int getZ() {
		return locZ;
	}

	/**
	 * Return the Itdentifier of the NpcInstance manage by this L2Spwan contained in the NpcTemplate.<BR><BR>
	 */
	public int getNpcId() {
		return npc.getNpcId();
	}

	/**
	 * Return the heading of NpcInstance when they are spawned.<BR><BR>
	 */
	public int getHeading() {
		return heading;
	}

	/**
	 * Return the delay between a NpcInstance remove and its re-spawn.<BR><BR>
	 */
	public int getRespawnDelay() {
		return respawnDelay;
	}

	/**
	 * Return Random RaidBoss Spawn delay.<BR><BR>
	 */
	public int getRandomRespawnDelay() {
		return randomRespawnDelay;
	}

	/**
	 * Set the Identifier of the location area where NpcInstance can be spwaned.<BR><BR>
	 */
	public void setGroup(SpawnGroup group) {
		this.group = group;
	}

	/**
	 * Set the X position of the spwan point.<BR><BR>
	 */
	public void setX(int locx) {
		this.locX = locx;
	}

	/**
	 * Set the Y position of the spwan point.<BR><BR>
	 */
	public void setY(int locy) {
		this.locY = locy;
	}

	/**
	 * Set the Z position of the spwan point.<BR><BR>
	 */
	public void setZ(int locz) {
		this.locZ = locz;
	}

	/**
	 * Set the heading of NpcInstance when they are spawned.<BR><BR>
	 */
	public void setHeading(int heading) {
		this.heading = heading;
	}

	public void setRandomCoords(List<int[]> coords) {
		randomCoords = coords;
	}

	public static void addSpawnListener(SpawnListener listener) {
		synchronized (spawnListeners) {
			spawnListeners.add(listener);
		}
	}

	public static void removeSpawnListener(SpawnListener listener) {
		synchronized (spawnListeners) {
			spawnListeners.remove(listener);
		}
	}

	public static void notifyNpcSpawned(Npc npc) {
		synchronized (spawnListeners) {
			for (SpawnListener listener : spawnListeners) {
				listener.npcSpawned(npc);
			}
		}
	}

	/**
	 * @param i delay in seconds
	 */
	public void setRespawnDelay(int i) {
		if (i < 0) {
			log.warn("respawn delay is negative for spawn:" + this);
		}

		if (i < 10) {
			i = 10;
		}

		respawnDelay = i * 1000;
	}

	/**
	 * @param i delay in seconds
	 */
	public void setRandomRespawnDelay(int i) {
		if (i < 0) {
			log.warn("respawn random delay is negative for spawn:" + this);
		}

		if (i < 10) {
			i = 10;
		}

		randomRespawnDelay = i * 1000;
	}

	public Npc getNpc() {
		return npc;
	}

	private void respawnNpc() {
		if (doRespawn) {
			npc.refreshID();
			initializeNpc();
			spawned = true;
		}
	}

	public NpcTemplate getTemplate() {
		return npc.getTemplate();
	}

	public int getInstanceId() {
		return instanceId;
	}

	public boolean isSpawned() {
		return spawned;
	}

	public void setInstanceId(int instanceId) {
		this.instanceId = instanceId;
	}

	public long getNextRespawn() {
		return nextRespawn;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "L2Spawn [_template=" + getNpcId() + ", _locX=" + locX + ", _locY=" + locY + ", _locZ=" + locZ + ", _heading=" + heading + "]";
	}
}
