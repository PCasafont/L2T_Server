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

package l2server.gameserver.model

import l2server.gameserver.handler.ActionHandler
import l2server.gameserver.idfactory.IdFactory
import l2server.gameserver.instancemanager.InstanceManager
import l2server.gameserver.model.actor.Creature
import l2server.gameserver.model.actor.Npc
import l2server.gameserver.model.actor.instance.Player
import l2server.gameserver.model.actor.knownlist.ObjectKnownList
import l2server.gameserver.model.actor.poly.ObjectPoly
import l2server.gameserver.model.actor.position.ObjectPosition
import l2server.gameserver.network.serverpackets.ActionFailed
import l2server.gameserver.network.serverpackets.ExSendUIEvent
import l2server.gameserver.network.serverpackets.ExSendUIEventRemove
import l2server.gameserver.network.serverpackets.L2GameServerPacket

/**
 * Mother class of all objects in the world wich ones is it possible
 * to interact (PC, NPC, Item...)<BR></BR><BR></BR>
 *
 *
 * WorldObject :<BR></BR><BR></BR>
 *  * Creature
 *  * Item
 *  * L2Potion
 */

abstract class WorldObject(objectId: Int) {

	private var isVisible: Boolean = false // Object visibility
	open var knownList: ObjectKnownList? = null
	open var name: String? = null
	var objectId: Int = 0
		private set // Object identifier
	val poly: ObjectPoly by lazy { ObjectPoly(this) }
	open lateinit var position: ObjectPosition
		protected set

	// If we change it for visible objects, me must clear & revalidate knownlists
	// We don't want some ugly looking disappear/appear effects, so don't update
	// the knownlist here, but players usually enter instancezones through teleporting
	// and the teleport will do the revalidation for us.
	var instanceId = 0
		set(instanceId) {
			if (instanceId == field) {
				return
			}

			val oldI = InstanceManager.getInstance().getInstance(field)
			val newI = InstanceManager.getInstance().getInstance(instanceId) ?: return

			if (this is Player) {
				if (field > 0 && oldI != null) {
					oldI.removePlayer(objectId)
					if (oldI.isShowTimer) {
						sendPacket(ExSendUIEventRemove())
					}
				}
				if (instanceId > 0) {
					newI.addPlayer(objectId)
					if (newI.isShowTimer) {
						val startTime = ((System.currentTimeMillis() - newI.instanceStartTime) / 1000).toInt()
						val endTime = ((newI.instanceEndTime - newI.instanceStartTime) / 1000).toInt()

						val packet = if (newI.isTimerIncrease) {
							ExSendUIEvent(0, 1, startTime, endTime, newI.timerText)
						} else {
							ExSendUIEvent(0, 0, endTime - startTime, 0, newI.timerText)
						}

						sendPacket(packet)
					}
				}

				if (this.pet != null) {
					this.pet.instanceId = instanceId
				}
				for (summon in this.summons) {
					summon.instanceId = instanceId
				}
			} else if (this is Npc) {
				if (field > 0 && oldI != null) {
					oldI.removeNpc(this)
				}
				if (instanceId > 0) {
					newI.addNpc(this)
				}
			}

			field = instanceId
			if (isVisible && knownList != null) {
				if (this is Player) {
				} else {
					decayMe()
					spawnMe()
				}
			}
		}

	var instanceType: InstanceType
		protected set

	val x: Int
		get() {
			assert(position.worldRegion != null || isVisible)
			return position.x
		}

	val y: Int
		get() {
			assert(position.worldRegion != null || isVisible)
			return position.y
		}

	val z: Int
		get() {
			assert(position.worldRegion != null || isVisible)
			return position.z
		}

	open val isAttackable: Boolean
		get() = false

	open val isMarker: Boolean
		get() = false

	/**
	 * returns reference to region this object is in
	 */
	val worldRegion: WorldRegion
		get() = position.worldRegion

	open val actingPlayer: Player?
		get() = null

	init {
		instanceType = InstanceType.L2Object
		this.objectId = objectId

		@Suppress("LeakingThis")
		initKnownList()
		@Suppress("LeakingThis")
		initPosition()
	}

	fun isInstanceType(i: InstanceType): Boolean {
		return instanceType.isType(i)
	}

	fun isInstanceTypes(vararg i: InstanceType): Boolean {
		return instanceType.isTypes(*i)
	}

	fun onAction(player: Player) {
		onAction(player, true)
	}

	open fun onAction(player: Player, interact: Boolean) {
		val handler = ActionHandler.getInstance().getActionHandler(instanceType)
		handler?.action(player, this, interact)

		player.sendPacket(ActionFailed.STATIC_PACKET)
	}

	open fun onActionShift(player: Player) {
		val handler = ActionHandler.getInstance().getActionShiftHandler(instanceType)
		handler?.action(player, this, true)

		player.sendPacket(ActionFailed.STATIC_PACKET)
	}

	open fun onForcedAttack(player: Player) {
		player.sendPacket(ActionFailed.STATIC_PACKET)
	}

	/**
	 * Do Nothing.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Overridden in </U> :</B><BR></BR><BR></BR>
	 *  *  GuardInstance :  Set the home location of its GuardInstance
	 *  *  Attackable	:  Reset the Spoiled flag <BR></BR><BR></BR>
	 */
	open fun onSpawn() {}

	// Position - Should remove to fully move to L2ObjectPosition
	fun setXYZ(x: Int, y: Int, z: Int) {
		position.setXYZ(x, y, z)
	}

	fun setXYZInvisible(x: Int, y: Int, z: Int) {
		position.setXYZInvisible(x, y, z)
	}

	/**
	 * Remove a WorldObject from the world.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Remove the WorldObject from the world<BR></BR><BR></BR>
	 *
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Assert </U> :</B><BR></BR><BR></BR>
	 *  *  worldRegion != null <I>(WorldObject is visible at the beginning)</I><BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Example of use </U> :</B><BR></BR><BR></BR>
	 *  *  Delete NPC/PC or Unsummon<BR></BR><BR></BR>
	 */
	open fun decayMe() {
		assert(position.worldRegion != null)

		val reg = position.worldRegion

		synchronized(this) {
			isVisible = false
			position.worldRegion = null
		}

		// this can synchronize on others instancies, so it's out of
		// synchronized, to avoid deadlocks
		// Remove the WorldObject from the world
		World.getInstance().removeVisibleObject(this, reg)
		World.getInstance().removeObject(this)
	}

	open fun refreshID() {
		World.getInstance().removeObject(this)
		IdFactory.getInstance().releaseId(objectId)
		objectId = IdFactory.getInstance().nextId
	}

	/**
	 * Init the position of a WorldObject spawn and add it in the world as a visible object.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Set the x,y,z position of the WorldObject spawn and update its worldregion
	 *  * Add the WorldObject spawn in the allobjects of World
	 *  * Add the WorldObject spawn to visibleObjects of its WorldRegion
	 *  * Add the WorldObject spawn in the world as a <B>visible</B> object<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Assert </U> :</B><BR></BR><BR></BR>
	 *  *  worldRegion == null <I>(WorldObject is invisible at the beginning)</I><BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Example of use </U> :</B><BR></BR><BR></BR>
	 *  *  Create Door
	 *  *  Spawn : Monster, Minion, CTs, Summon...<BR></BR>
	 */
	fun spawnMe() {
		synchronized(this) {
			// Set the x,y,z position of the WorldObject spawn and update its worldregion
			isVisible = true
			position.worldRegion = World.getInstance().getRegion(position.worldPosition)

			// Add the WorldObject spawn in the allobjects of World
			World.getInstance().storeObject(this)

			// Add the WorldObject spawn to visibleObjects and if necessary to allplayers of its WorldRegion
			position.worldRegion.addVisibleObject(this)
		}

		// this can synchronize on others instancies, so it's out of
		// synchronized, to avoid deadlocks
		// Add the WorldObject spawn in the world as a visible object
		World.getInstance().addVisibleObject(this, position.worldRegion)

		onSpawn()
	}

	fun spawnMe(x: Int, y: Int, z: Int) {
		synchronized(this) {
			var fixedX = x
			var fixedY = y
			// Set the x,y,z position of the WorldObject spawn and update its worldregion
			isVisible = true

			if (fixedX > World.MAP_MAX_X) {
				fixedX = World.MAP_MAX_X - 5000
			}
			if (fixedX < World.MAP_MIN_X) {
				fixedX = World.MAP_MIN_X + 5000
			}
			if (fixedY > World.MAP_MAX_Y) {
				fixedY = World.MAP_MAX_Y - 5000
			}
			if (fixedY < World.MAP_MIN_Y) {
				fixedY = World.MAP_MIN_Y + 5000
			}

			position.setWorldPosition(fixedX, fixedY, z)
			position.worldRegion = World.getInstance().getRegion(position.worldPosition)
		}

		// Add the WorldObject spawn in the allobjects of World
		World.getInstance().storeObject(this)

		// these can synchronize on others instancies, so they're out of
		// synchronized, to avoid deadlocks

		// Add the WorldObject spawn to visibleObjects and if necessary to allplayers of its WorldRegion
		position.worldRegion.addVisibleObject(this)

		// Add the WorldObject spawn in the world as a visible object
		World.getInstance().addVisibleObject(this, position.worldRegion)

		onSpawn()
	}

	fun toggleVisible() {
		if (isVisible()) {
			decayMe()
		} else {
			spawnMe()
		}
	}

	abstract fun isAutoAttackable(attacker: Creature): Boolean

	/**
	 * Return the visibilty state of the WorldObject. <BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * A WorldObject is visble if <B>_IsVisible</B>=true and <B>worldregion</B>!=null <BR></BR><BR></BR>
	 */
	fun isVisible(): Boolean {
		//return getPosition().getWorldRegion() != null && IsVisible;
		return position.worldRegion != null
	}

	fun setIsVisible(value: Boolean) {
		isVisible = value
		if (!isVisible) {
			position.worldRegion = null
		}
	}

	/**
	 * Initializes the KnownList of the WorldObject,
	 * is overwritten in classes that require a different knownlist Type.
	 *
	 *
	 * Removes the need for instanceof checks.
	 */
	open fun initKnownList() {
		knownList = ObjectKnownList(this)
	}

	/**
	 * Initializes the Position class of the WorldObject,
	 * is overwritten in classes that require a different position Type.
	 *
	 *
	 * Removes the need for instanceof checks.
	 */
	open fun initPosition() {
		position = ObjectPosition(this)
	}

	fun setObjectPosition(value: ObjectPosition) {
		position = value
	}

	/**
	 * Sends the Server->Client info packet for the object.<br></br><br></br>
	 * Is Overridden in:
	 *  * AirShipInstance
	 *  * BoatInstance
	 *  * DoorInstance
	 *  * Player
	 *  * StaticObjectInstance
	 *  * L2Decoy
	 *  * Npc
	 *  * Summon
	 *  * Trap
	 *  * Item
	 */
	open fun sendInfo(activeChar: Player) {

	}

	override fun toString(): String {
		return javaClass.simpleName + ":" + name + "[" + objectId + "]"
	}

	/**
	 * Not Implemented.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Overridden in </U> :</B><BR></BR><BR></BR>
	 *  *  Player<BR></BR><BR></BR>
	 */
	open fun sendPacket(mov: L2GameServerPacket) {
		// default implementation
	}
}
