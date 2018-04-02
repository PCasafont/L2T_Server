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

import l2server.gameserver.handler.ActionHandler;
import l2server.gameserver.handler.IActionHandler;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.SummonInstance;
import l2server.gameserver.model.actor.knownlist.ObjectKnownList;
import l2server.gameserver.model.actor.poly.ObjectPoly;
import l2server.gameserver.model.actor.position.ObjectPosition;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.ExSendUIEvent;
import l2server.gameserver.network.serverpackets.ExSendUIEventRemove;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * Mother class of all objects in the world wich ones is it possible
 * to interact (PC, NPC, Item...)<BR><BR>
 * <p>
 * WorldObject :<BR><BR>
 * <li>Creature</li>
 * <li>Item</li>
 * <li>L2Potion</li>
 */

public abstract class WorldObject {
	// =========================================================
	// Data Field
	private boolean isVisible; // Object visibility
	private ObjectKnownList knownList;
	private String name;
	private int objectId; // Object identifier
	private ObjectPoly poly;
	private ObjectPosition position;
	private int instanceId = 0;
	
	private InstanceType instanceType = null;
	
	// =========================================================
	// Constructor
	public WorldObject(int objectId) {
		setInstanceType(InstanceType.L2Object);
		this.objectId = objectId;
		initKnownList();
		initPosition();
	}
	
	public enum InstanceType {
		L2Object(null),
		L2ItemInstance(L2Object),
		L2Character(L2Object),
		L2Npc(L2Character),
		L2Playable(L2Character),
		L2Summon(L2Playable),
		L2Decoy(L2Character),
		L2Trap(L2Character),
		L2PcInstance(L2Playable),
		L2NpcInstance(L2Npc),
		L2MerchantInstance(L2NpcInstance),
		L2WarehouseInstance(L2NpcInstance),
		L2StaticObjectInstance(L2Character),
		L2DoorInstance(L2Character),
		L2NpcWalkerInstance(L2Npc),
		L2TerrainObjectInstance(L2Npc),
		L2EffectPointInstance(L2Npc),
		// Summons, Pets, Decoys and Traps
		L2SummonInstance(L2Summon),
		L2SiegeSummonInstance(L2SummonInstance),
		L2MerchantSummonInstance(L2SummonInstance),
		L2PetInstance(L2Summon),
		L2BabyPetInstance(L2PetInstance),
		L2DecoyInstance(L2Decoy),
		L2TrapInstance(L2Trap),
		// Attackable
		L2Attackable(L2Npc),
		L2GuardInstance(L2Attackable),
		L2QuestGuardInstance(L2GuardInstance),
		L2MonsterInstance(L2Attackable),
		L2ChestInstance(L2MonsterInstance),
		L2ControllableMobInstance(L2MonsterInstance),
		L2FeedableBeastInstance(L2MonsterInstance),
		L2TamedBeastInstance(L2FeedableBeastInstance),
		L2FriendlyMobInstance(L2Attackable),
		L2PenaltyMonsterInstance(L2MonsterInstance),
		L2RaidBossInstance(L2MonsterInstance),
		L2GrandBossInstance(L2RaidBossInstance),
		// FlyMobs
		L2FlyNpcInstance(L2NpcInstance),
		L2FlyMonsterInstance(L2MonsterInstance),
		L2FlyRaidBossInstance(L2RaidBossInstance),
		L2FlyTerrainObjectInstance(L2Npc),
		// Sepulchers
		L2SepulcherNpcInstance(L2NpcInstance),
		L2SepulcherMonsterInstance(L2MonsterInstance),
		// Vehicles
		L2Vehicle(L2Character),
		L2BoatInstance(L2Vehicle),
		L2AirShipInstance(L2Vehicle),
		L2ControllableAirShipInstance(L2AirShipInstance),
		L2ShuttleInstance(L2Vehicle),
		// Siege
		L2DefenderInstance(L2Attackable),
		L2ArtefactInstance(L2NpcInstance),
		L2ControlTowerInstance(L2Npc),
		L2FlameTowerInstance(L2Npc),
		L2SiegeFlagInstance(L2Npc),
		L2SiegeNpcInstance(L2Npc),
		// Fort Siege
		L2FortBallistaInstance(L2Npc),
		L2FortCommanderInstance(L2DefenderInstance),
		// Castle NPCs
		L2CastleBlacksmithInstance(L2NpcInstance),
		L2CastleChamberlainInstance(L2MerchantInstance),
		L2CastleMagicianInstance(L2NpcInstance),
		L2CastleTeleporterInstance(L2Npc),
		L2CastleWarehouseInstance(L2WarehouseInstance),
		L2MercManagerInstance(L2MerchantInstance),
		// Fort NPCs
		L2FortEnvoyInstance(L2Npc),
		L2FortLogisticsInstance(L2MerchantInstance),
		L2FortManagerInstance(L2MerchantInstance),
		L2FortSiegeNpcInstance(L2NpcWalkerInstance),
		L2FortSupportCaptainInstance(L2MerchantInstance),
		// City NPCs
		L2AdventurerInstance(L2NpcInstance),
		L2AuctioneerInstance(L2Npc),
		L2ClanHallManagerInstance(L2MerchantInstance),
		L2ClanTraderInstance(L2Npc),
		L2FameManagerInstance(L2Npc),
		L2FishermanInstance(L2MerchantInstance),
		L2ManorManagerInstance(L2MerchantInstance),
		L2MercenaryManagerInstance(L2Npc),
		L2ObservationInstance(L2Npc),
		L2OlympiadManagerInstance(L2Npc),
		L2PetManagerInstance(L2MerchantInstance),
		L2RaceManagerInstance(L2Npc),
		L2SymbolMakerInstance(L2Npc),
		L2TeleporterInstance(L2Npc),
		L2TownPetInstance(L2Npc),
		L2TrainerInstance(L2NpcInstance),
		L2TransformManagerInstance(L2MerchantInstance),
		L2VillageMasterInstance(L2NpcInstance),
		L2WyvernManagerInstance(L2NpcInstance),
		L2XmassTreeInstance(L2NpcInstance),
		L2AwakeNpcInstance(L2Npc),
		// Doormens
		L2DoormenInstance(L2NpcInstance),
		L2CastleDoormenInstance(L2DoormenInstance),
		L2FortDoormenInstance(L2DoormenInstance),
		L2ClanHallDoormenInstance(L2DoormenInstance),
		// Custom
		L2ClassMasterInstance(L2NpcInstance),
		L2NpcBufferInstance(L2Npc),
		L2InstancedEventNpcInstance(L2Npc),
		L2WeddingManagerInstance(L2Npc),
		L2EventMobInstance(L2Npc),
		L2StatueInstance(L2Npc),
		L2GatekeeperInstance(L2TeleporterInstance),
		L2BufferInstance(L2TeleporterInstance),
		L2CustomMerchantInstance(L2MerchantInstance),
		L2WorldManagerInstance(L2MerchantInstance),
		L2DonationManagerInstance(L2MerchantInstance),
		L2MiniRaidInstance(L2RaidBossInstance),
		L2DeluxeChestInstance(L2MonsterInstance),
		L2MiniGameManagerInstance(L2MerchantInstance),
		L2CloneInstance(L2SummonInstance);
		
		private final InstanceType parent;
		private final long typeL;
		private final long typeH;
		private final long maskL;
		private final long maskH;
		
		InstanceType(InstanceType parent) {
			this.parent = parent;
			
			final int high = ordinal() - (Long.SIZE - 1);
			if (high < 0) {
				typeL = 1L << ordinal();
				typeH = 0;
			} else {
				typeL = 0;
				typeH = 1L << high;
			}
			
			if (typeL < 0 || typeH < 0) {
				throw new Error("Too many instance types, failed to load " + name());
			}
			
			if (parent != null) {
				maskL = typeL | parent.maskL;
				maskH = typeH | parent.maskH;
			} else {
				maskL = typeL;
				maskH = typeH;
			}
		}
		
		public final InstanceType getParent() {
			return parent;
		}
		
		public final boolean isType(InstanceType it) {
			return (maskL & it.typeL) > 0 || (maskH & it.typeH) > 0;
		}
		
		public final boolean isTypes(InstanceType... it) {
			for (InstanceType i : it) {
				if (isType(i)) {
					return true;
				}
			}
			return false;
		}
	}
	
	protected final void setInstanceType(InstanceType i) {
		instanceType = i;
	}
	
	public final InstanceType getInstanceType() {
		return instanceType;
	}
	
	public final boolean isInstanceType(InstanceType i) {
		return instanceType.isType(i);
	}
	
	public final boolean isInstanceTypes(InstanceType... i) {
		return instanceType.isTypes(i);
	}
	
	// =========================================================
	// Event - Public
	public final void onAction(Player player) {
		onAction(player, true);
	}
	
	public void onAction(Player player, boolean interact) {
		IActionHandler handler = ActionHandler.getInstance().getActionHandler(getInstanceType());
		if (handler != null) {
			handler.action(player, this, interact);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void onActionShift(Player player) {
		IActionHandler handler = ActionHandler.getInstance().getActionShiftHandler(getInstanceType());
		if (handler != null) {
			handler.action(player, this, true);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void onForcedAttack(Player player) {
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Do Nothing.<BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> GuardInstance :  Set the home location of its GuardInstance </li>
	 * <li> Attackable	:  Reset the Spoiled flag </li><BR><BR>
	 */
	public void onSpawn() {
	}
	
	// =========================================================
	// Position - Should remove to fully move to L2ObjectPosition
	public final void setXYZ(int x, int y, int z) {
		getPosition().setXYZ(x, y, z);
	}
	
	public final void setXYZInvisible(int x, int y, int z) {
		getPosition().setXYZInvisible(x, y, z);
	}
	
	public final int getX() {
		assert getPosition().getWorldRegion() != null || isVisible;
		return getPosition().getX();
	}
	
	/**
	 * @return The id of the instance zone the object is in - id 0 is global
	 * since everything like dropped items, mobs, players can be in a instanciated area, it must be in l2object
	 */
	public int getInstanceId() {
		return instanceId;
	}
	
	/**
	 * @param instanceId The id of the instance zone the object is in - id 0 is global
	 */
	public void setInstanceId(int instanceId) {
		if (instanceId == instanceId) {
			return;
		}
		
		Instance oldI = InstanceManager.getInstance().getInstance(instanceId);
		Instance newI = InstanceManager.getInstance().getInstance(instanceId);
		
		if (newI == null) {
			return;
		}
		
		if (this instanceof Player) {
			if (instanceId > 0 && oldI != null) {
				oldI.removePlayer(getObjectId());
				if (oldI.isShowTimer()) {
					sendPacket(new ExSendUIEventRemove());
				}
			}
			if (instanceId > 0) {
				newI.addPlayer(getObjectId());
				if (newI.isShowTimer()) {
					int startTime = (int) ((System.currentTimeMillis() - newI.getInstanceStartTime()) / 1000);
					int endTime = (int) ((newI.getInstanceEndTime() - newI.getInstanceStartTime()) / 1000);
					
					if (newI.isTimerIncrease()) {
						sendPacket(new ExSendUIEvent(0, 1, startTime, endTime, newI.getTimerText()));
					} else {
						sendPacket(new ExSendUIEvent(0, 0, endTime - startTime, 0, newI.getTimerText()));
					}
				}
			}
			
			if (((Player) this).getPet() != null) {
				((Player) this).getPet().setInstanceId(instanceId);
			}
			for (SummonInstance summon : ((Player) this).getSummons()) {
				summon.setInstanceId(instanceId);
			}
		} else if (this instanceof Npc) {
			if (instanceId > 0 && oldI != null) {
				oldI.removeNpc((Npc) this);
			}
			if (instanceId > 0) {
				newI.addNpc((Npc) this);
			}
		}
		
		this.instanceId = instanceId;
		
		// If we change it for visible objects, me must clear & revalidate knownlists
		if (isVisible && knownList != null) {
			if (this instanceof Player) {
				
				// We don't want some ugly looking disappear/appear effects, so don't update
				// the knownlist here, but players usually enter instancezones through teleporting
				// and the teleport will do the revalidation for us.
			} else {
				decayMe();
				spawnMe();
			}
		}
	}
	
	public final int getY() {
		assert getPosition().getWorldRegion() != null || isVisible;
		return getPosition().getY();
	}
	
	public final int getZ() {
		assert getPosition().getWorldRegion() != null || isVisible;
		return getPosition().getZ();
	}
	
	// =========================================================
	// Method - Public
	
	/**
	 * Remove a WorldObject from the world.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the WorldObject from the world</li><BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR><BR>
	 * <p>
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> worldRegion != null <I>(WorldObject is visible at the beginning)</I></li><BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Delete NPC/PC or Unsummon</li><BR><BR>
	 */
	public void decayMe() {
		assert getPosition().getWorldRegion() != null;
		
		WorldRegion reg = getPosition().getWorldRegion();
		
		synchronized (this) {
			isVisible = false;
			getPosition().setWorldRegion(null);
		}
		
		// this can synchronize on others instancies, so it's out of
		// synchronized, to avoid deadlocks
		// Remove the WorldObject from the world
		World.getInstance().removeVisibleObject(this, reg);
		World.getInstance().removeObject(this);
	}
	
	public void refreshID() {
		World.getInstance().removeObject(this);
		IdFactory.getInstance().releaseId(getObjectId());
		objectId = IdFactory.getInstance().getNextId();
	}
	
	/**
	 * Init the position of a WorldObject spawn and add it in the world as a visible object.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the x,y,z position of the WorldObject spawn and update its worldregion </li>
	 * <li>Add the WorldObject spawn in the allobjects of World </li>
	 * <li>Add the WorldObject spawn to visibleObjects of its WorldRegion</li>
	 * <li>Add the WorldObject spawn in the world as a <B>visible</B> object</li><BR><BR>
	 * <p>
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> worldRegion == null <I>(WorldObject is invisible at the beginning)</I></li><BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Create Door</li>
	 * <li> Spawn : Monster, Minion, CTs, Summon...</li><BR>
	 */
	public final void spawnMe() {
		synchronized (this) {
			// Set the x,y,z position of the WorldObject spawn and update its worldregion
			isVisible = true;
			getPosition().setWorldRegion(World.getInstance().getRegion(getPosition().getWorldPosition()));
			
			// Add the WorldObject spawn in the allobjects of World
			World.getInstance().storeObject(this);
			
			// Add the WorldObject spawn to visibleObjects and if necessary to allplayers of its WorldRegion
			getPosition().getWorldRegion().addVisibleObject(this);
		}
		
		// this can synchronize on others instancies, so it's out of
		// synchronized, to avoid deadlocks
		// Add the WorldObject spawn in the world as a visible object
		World.getInstance().addVisibleObject(this, getPosition().getWorldRegion());
		
		onSpawn();
	}
	
	public final void spawnMe(int x, int y, int z) {
		synchronized (this) {
			// Set the x,y,z position of the WorldObject spawn and update its worldregion
			isVisible = true;
			
			if (x > World.MAP_MAX_X) {
				x = World.MAP_MAX_X - 5000;
			}
			if (x < World.MAP_MIN_X) {
				x = World.MAP_MIN_X + 5000;
			}
			if (y > World.MAP_MAX_Y) {
				y = World.MAP_MAX_Y - 5000;
			}
			if (y < World.MAP_MIN_Y) {
				y = World.MAP_MIN_Y + 5000;
			}
			
			getPosition().setWorldPosition(x, y, z);
			getPosition().setWorldRegion(World.getInstance().getRegion(getPosition().getWorldPosition()));
		}
		
		// Add the WorldObject spawn in the allobjects of World
		World.getInstance().storeObject(this);
		
		// these can synchronize on others instancies, so they're out of
		// synchronized, to avoid deadlocks
		
		// Add the WorldObject spawn to visibleObjects and if necessary to allplayers of its WorldRegion
		getPosition().getWorldRegion().addVisibleObject(this);
		
		// Add the WorldObject spawn in the world as a visible object
		World.getInstance().addVisibleObject(this, getPosition().getWorldRegion());
		
		onSpawn();
	}
	
	public void toggleVisible() {
		if (isVisible()) {
			decayMe();
		} else {
			spawnMe();
		}
	}
	
	// =========================================================
	// Method - Private
	
	// =========================================================
	// Property - Public
	public boolean isAttackable() {
		return false;
	}
	
	public abstract boolean isAutoAttackable(Creature attacker);
	
	public boolean isMarker() {
		return false;
	}
	
	/**
	 * Return the visibilty state of the WorldObject. <BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A WorldObject is visble if <B>_IsVisible</B>=true and <B>worldregion</B>!=null <BR><BR>
	 */
	public final boolean isVisible() {
		//return getPosition().getWorldRegion() != null && IsVisible;
		return getPosition().getWorldRegion() != null;
	}
	
	public final void setIsVisible(boolean value) {
		isVisible = value;
		if (!isVisible) {
			getPosition().setWorldRegion(null);
		}
	}
	
	public ObjectKnownList getKnownList() {
		return knownList;
	}
	
	/**
	 * Initializes the KnownList of the WorldObject,
	 * is overwritten in classes that require a different knownlist Type.
	 * <p>
	 * Removes the need for instanceof checks.
	 */
	public void initKnownList() {
		knownList = new ObjectKnownList(this);
	}
	
	public final void setKnownList(ObjectKnownList value) {
		knownList = value;
	}
	
	public final String getName() {
		return name;
	}
	
	public void setName(String value) {
		name = value;
	}
	
	public final int getObjectId() {
		return objectId;
	}
	
	public final ObjectPoly getPoly() {
		if (poly == null) {
			poly = new ObjectPoly(this);
		}
		return poly;
	}
	
	public ObjectPosition getPosition() {
		return position;
	}
	
	/**
	 * Initializes the Position class of the WorldObject,
	 * is overwritten in classes that require a different position Type.
	 * <p>
	 * Removes the need for instanceof checks.
	 */
	public void initPosition() {
		position = new ObjectPosition(this);
	}
	
	public final void setObjectPosition(ObjectPosition value) {
		position = value;
	}
	
	/**
	 * returns reference to region this object is in
	 */
	public WorldRegion getWorldRegion() {
		return getPosition().getWorldRegion();
	}
	
	public Player getActingPlayer() {
		return null;
	}
	
	/**
	 * Sends the Server->Client info packet for the object.<br><br>
	 * Is Overridden in:
	 * <li>AirShipInstance</li>
	 * <li>BoatInstance</li>
	 * <li>DoorInstance</li>
	 * <li>Player</li>
	 * <li>StaticObjectInstance</li>
	 * <li>L2Decoy</li>
	 * <li>Npc</li>
	 * <li>Summon</li>
	 * <li>Trap</li>
	 * <li>Item</li>
	 */
	public void sendInfo(Player activeChar) {
	
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + getName() + "[" + getObjectId() + "]";
	}
	
	/**
	 * Not Implemented.<BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> Player</li><BR><BR>
	 */
	public void sendPacket(L2GameServerPacket mov) {
		// default implementation
	}
}
