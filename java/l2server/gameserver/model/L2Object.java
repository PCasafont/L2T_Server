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
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
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
 * L2Object :<BR><BR>
 * <li>L2Character</li>
 * <li>L2ItemInstance</li>
 * <li>L2Potion</li>
 */

public abstract class L2Object
{
	// =========================================================
	// Data Field
	private boolean _isVisible; // Object visibility
	private ObjectKnownList _knownList;
	private String _name;
	private int _objectId; // Object identifier
	private ObjectPoly _poly;
	private ObjectPosition _position;
	private int _instanceId = 0;

	private InstanceType _instanceType = null;

	// =========================================================
	// Constructor
	public L2Object(int objectId)
	{
		setInstanceType(InstanceType.L2Object);
		_objectId = objectId;
		initKnownList();
		initPosition();
	}

	public enum InstanceType
	{
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

		private final InstanceType _parent;
		private final long _typeL;
		private final long _typeH;
		private final long _maskL;
		private final long _maskH;

		InstanceType(InstanceType parent)
		{
			_parent = parent;

			final int high = ordinal() - (Long.SIZE - 1);
			if (high < 0)
			{
				_typeL = 1L << ordinal();
				_typeH = 0;
			}
			else
			{
				_typeL = 0;
				_typeH = 1L << high;
			}

			if (_typeL < 0 || _typeH < 0)
			{
				throw new Error("Too many instance types, failed to load " + name());
			}

			if (parent != null)
			{
				_maskL = _typeL | parent._maskL;
				_maskH = _typeH | parent._maskH;
			}
			else
			{
				_maskL = _typeL;
				_maskH = _typeH;
			}
		}

		public final InstanceType getParent()
		{
			return _parent;
		}

		public final boolean isType(InstanceType it)
		{
			return (_maskL & it._typeL) > 0 || (_maskH & it._typeH) > 0;
		}

		public final boolean isTypes(InstanceType... it)
		{
			for (InstanceType i : it)
			{
				if (isType(i))
				{
					return true;
				}
			}
			return false;
		}
	}

	protected final void setInstanceType(InstanceType i)
	{
		_instanceType = i;
	}

	public final InstanceType getInstanceType()
	{
		return _instanceType;
	}

	public final boolean isInstanceType(InstanceType i)
	{
		return _instanceType.isType(i);
	}

	public final boolean isInstanceTypes(InstanceType... i)
	{
		return _instanceType.isTypes(i);
	}

	// =========================================================
	// Event - Public
	public final void onAction(L2PcInstance player)
	{
		onAction(player, true);
	}

	public void onAction(L2PcInstance player, boolean interact)
	{
		IActionHandler handler = ActionHandler.getInstance().getActionHandler(getInstanceType());
		if (handler != null)
		{
			handler.action(player, this, interact);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void onActionShift(L2PcInstance player)
	{
		IActionHandler handler = ActionHandler.getInstance().getActionShiftHandler(getInstanceType());
		if (handler != null)
		{
			handler.action(player, this, true);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void onForcedAttack(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	/**
	 * Do Nothing.<BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2GuardInstance :  Set the home location of its L2GuardInstance </li>
	 * <li> L2Attackable	:  Reset the Spoiled flag </li><BR><BR>
	 */
	public void onSpawn()
	{
	}

	// =========================================================
	// Position - Should remove to fully move to L2ObjectPosition
	public final void setXYZ(int x, int y, int z)
	{
		getPosition().setXYZ(x, y, z);
	}

	public final void setXYZInvisible(int x, int y, int z)
	{
		getPosition().setXYZInvisible(x, y, z);
	}

	public final int getX()
	{
		assert getPosition().getWorldRegion() != null || _isVisible;
		return getPosition().getX();
	}

	/**
	 * @return The id of the instance zone the object is in - id 0 is global
	 * since everything like dropped items, mobs, players can be in a instanciated area, it must be in l2object
	 */
	public int getInstanceId()
	{
		return _instanceId;
	}

	/**
	 * @param instanceId The id of the instance zone the object is in - id 0 is global
	 */
	public void setInstanceId(int instanceId)
	{
		if (_instanceId == instanceId)
		{
			return;
		}

		Instance oldI = InstanceManager.getInstance().getInstance(_instanceId);
		Instance newI = InstanceManager.getInstance().getInstance(instanceId);

		if (newI == null)
		{
			return;
		}

		if (this instanceof L2PcInstance)
		{
			if (_instanceId > 0 && oldI != null)
			{
				oldI.removePlayer(getObjectId());
				if (oldI.isShowTimer())
				{
					sendPacket(new ExSendUIEventRemove());
				}
			}
			if (instanceId > 0)
			{
				newI.addPlayer(getObjectId());
				if (newI.isShowTimer())
				{
					int startTime = (int) ((System.currentTimeMillis() - newI.getInstanceStartTime()) / 1000);
					int endTime = (int) ((newI.getInstanceEndTime() - newI.getInstanceStartTime()) / 1000);

					if (newI.isTimerIncrease())
					{
						sendPacket(new ExSendUIEvent(0, 1, startTime, endTime, newI.getTimerText()));
					}
					else
					{
						sendPacket(new ExSendUIEvent(0, 0, endTime - startTime, 0, newI.getTimerText()));
					}
				}
			}

			if (((L2PcInstance) this).getPet() != null)
			{
				((L2PcInstance) this).getPet().setInstanceId(instanceId);
			}
			for (L2SummonInstance summon : ((L2PcInstance) this).getSummons())
			{
				summon.setInstanceId(instanceId);
			}
		}
		else if (this instanceof L2Npc)
		{
			if (_instanceId > 0 && oldI != null)
			{
				oldI.removeNpc((L2Npc) this);
			}
			if (instanceId > 0)
			{
				newI.addNpc((L2Npc) this);
			}
		}

		_instanceId = instanceId;

		// If we change it for visible objects, me must clear & revalidate knownlists
		if (_isVisible && _knownList != null)
		{
			if (this instanceof L2PcInstance)
			{

				// We don't want some ugly looking disappear/appear effects, so don't update
				// the knownlist here, but players usually enter instancezones through teleporting
				// and the teleport will do the revalidation for us.
			}
			else
			{
				decayMe();
				spawnMe();
			}
		}
	}

	public final int getY()
	{
		assert getPosition().getWorldRegion() != null || _isVisible;
		return getPosition().getY();
	}

	public final int getZ()
	{
		assert getPosition().getWorldRegion() != null || _isVisible;
		return getPosition().getZ();
	}

	// =========================================================
	// Method - Public

	/**
	 * Remove a L2Object from the world.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the L2Object from the world</li><BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR><BR>
	 * <p>
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> _worldRegion != null <I>(L2Object is visible at the beginning)</I></li><BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Delete NPC/PC or Unsummon</li><BR><BR>
	 */
	public void decayMe()
	{
		assert getPosition().getWorldRegion() != null;

		L2WorldRegion reg = getPosition().getWorldRegion();

		synchronized (this)
		{
			_isVisible = false;
			getPosition().setWorldRegion(null);
		}

		// this can synchronize on others instancies, so it's out of
		// synchronized, to avoid deadlocks
		// Remove the L2Object from the world
		L2World.getInstance().removeVisibleObject(this, reg);
		L2World.getInstance().removeObject(this);
	}

	public void refreshID()
	{
		L2World.getInstance().removeObject(this);
		IdFactory.getInstance().releaseId(getObjectId());
		_objectId = IdFactory.getInstance().getNextId();
	}

	/**
	 * Init the position of a L2Object spawn and add it in the world as a visible object.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the x,y,z position of the L2Object spawn and update its _worldregion </li>
	 * <li>Add the L2Object spawn in the _allobjects of L2World </li>
	 * <li>Add the L2Object spawn to _visibleObjects of its L2WorldRegion</li>
	 * <li>Add the L2Object spawn in the world as a <B>visible</B> object</li><BR><BR>
	 * <p>
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> _worldRegion == null <I>(L2Object is invisible at the beginning)</I></li><BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Create Door</li>
	 * <li> Spawn : Monster, Minion, CTs, Summon...</li><BR>
	 */
	public final void spawnMe()
	{
		assert getPosition().getWorldRegion() == null && getPosition().getWorldPosition().getX() != 0 &&
				getPosition().getWorldPosition().getY() != 0 && getPosition().getWorldPosition().getZ() != 0;

		synchronized (this)
		{
			// Set the x,y,z position of the L2Object spawn and update its _worldregion
			_isVisible = true;
			getPosition().setWorldRegion(L2World.getInstance().getRegion(getPosition().getWorldPosition()));

			// Add the L2Object spawn in the _allobjects of L2World
			L2World.getInstance().storeObject(this);

			// Add the L2Object spawn to _visibleObjects and if necessary to _allplayers of its L2WorldRegion
			getPosition().getWorldRegion().addVisibleObject(this);
		}

		// this can synchronize on others instancies, so it's out of
		// synchronized, to avoid deadlocks
		// Add the L2Object spawn in the world as a visible object
		L2World.getInstance().addVisibleObject(this, getPosition().getWorldRegion());

		onSpawn();
	}

	public final void spawnMe(int x, int y, int z)
	{
		assert getPosition().getWorldRegion() == null;

		synchronized (this)
		{
			// Set the x,y,z position of the L2Object spawn and update its _worldregion
			_isVisible = true;

			if (x > L2World.MAP_MAX_X)
			{
				x = L2World.MAP_MAX_X - 5000;
			}
			if (x < L2World.MAP_MIN_X)
			{
				x = L2World.MAP_MIN_X + 5000;
			}
			if (y > L2World.MAP_MAX_Y)
			{
				y = L2World.MAP_MAX_Y - 5000;
			}
			if (y < L2World.MAP_MIN_Y)
			{
				y = L2World.MAP_MIN_Y + 5000;
			}

			getPosition().setWorldPosition(x, y, z);
			getPosition().setWorldRegion(L2World.getInstance().getRegion(getPosition().getWorldPosition()));
		}

		// Add the L2Object spawn in the _allobjects of L2World
		L2World.getInstance().storeObject(this);

		// these can synchronize on others instancies, so they're out of
		// synchronized, to avoid deadlocks

		// Add the L2Object spawn to _visibleObjects and if necessary to _allplayers of its L2WorldRegion
		getPosition().getWorldRegion().addVisibleObject(this);

		// Add the L2Object spawn in the world as a visible object
		L2World.getInstance().addVisibleObject(this, getPosition().getWorldRegion());

		onSpawn();
	}

	public void toggleVisible()
	{
		if (isVisible())
		{
			decayMe();
		}
		else
		{
			spawnMe();
		}
	}

	// =========================================================
	// Method - Private

	// =========================================================
	// Property - Public
	public boolean isAttackable()
	{
		return false;
	}

	public abstract boolean isAutoAttackable(L2Character attacker);

	public boolean isMarker()
	{
		return false;
	}

	/**
	 * Return the visibilty state of the L2Object. <BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Object is visble if <B>__IsVisible</B>=true and <B>_worldregion</B>!=null <BR><BR>
	 */
	public final boolean isVisible()
	{
		//return getPosition().getWorldRegion() != null && _IsVisible;
		return getPosition().getWorldRegion() != null;
	}

	public final void setIsVisible(boolean value)
	{
		_isVisible = value;
		if (!_isVisible)
		{
			getPosition().setWorldRegion(null);
		}
	}

	public ObjectKnownList getKnownList()
	{
		return _knownList;
	}

	/**
	 * Initializes the KnownList of the L2Object,
	 * is overwritten in classes that require a different knownlist Type.
	 * <p>
	 * Removes the need for instanceof checks.
	 */
	public void initKnownList()
	{
		_knownList = new ObjectKnownList(this);
	}

	public final void setKnownList(ObjectKnownList value)
	{
		_knownList = value;
	}

	public final String getName()
	{
		return _name;
	}

	public void setName(String value)
	{
		_name = value;
	}

	public final int getObjectId()
	{
		return _objectId;
	}

	public final ObjectPoly getPoly()
	{
		if (_poly == null)
		{
			_poly = new ObjectPoly(this);
		}
		return _poly;
	}

	public ObjectPosition getPosition()
	{
		return _position;
	}

	/**
	 * Initializes the Position class of the L2Object,
	 * is overwritten in classes that require a different position Type.
	 * <p>
	 * Removes the need for instanceof checks.
	 */
	public void initPosition()
	{
		_position = new ObjectPosition(this);
	}

	public final void setObjectPosition(ObjectPosition value)
	{
		_position = value;
	}

	/**
	 * returns reference to region this object is in
	 */
	public L2WorldRegion getWorldRegion()
	{
		return getPosition().getWorldRegion();
	}

	public L2PcInstance getActingPlayer()
	{
		return null;
	}

	/**
	 * Sends the Server->Client info packet for the object.<br><br>
	 * Is Overridden in:
	 * <li>L2AirShipInstance</li>
	 * <li>L2BoatInstance</li>
	 * <li>L2DoorInstance</li>
	 * <li>L2PcInstance</li>
	 * <li>L2StaticObjectInstance</li>
	 * <li>L2Decoy</li>
	 * <li>L2Npc</li>
	 * <li>L2Summon</li>
	 * <li>L2Trap</li>
	 * <li>L2ItemInstance</li>
	 */
	public void sendInfo(L2PcInstance activeChar)
	{

	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ":" + getName() + "[" + getObjectId() + "]";
	}

	/**
	 * Not Implemented.<BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 */
	public void sendPacket(L2GameServerPacket mov)
	{
		// default implementation
	}
}
