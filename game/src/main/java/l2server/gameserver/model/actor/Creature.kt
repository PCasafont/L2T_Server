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

package l2server.gameserver.model.actor

import l2server.Config
import l2server.gameserver.GeoData
import l2server.gameserver.ThreadPoolManager
import l2server.gameserver.TimeController
import l2server.gameserver.ai.AttackableAI
import l2server.gameserver.ai.CreatureAI
import l2server.gameserver.ai.CtrlEvent
import l2server.gameserver.ai.CtrlIntention
import l2server.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE
import l2server.gameserver.datatables.DoorTable
import l2server.gameserver.datatables.ItemTable
import l2server.gameserver.datatables.MapRegionTable
import l2server.gameserver.datatables.MapRegionTable.TeleportWhereType
import l2server.gameserver.datatables.SkillTable
import l2server.gameserver.events.Curfew
import l2server.gameserver.events.instanced.EventInstance.EventType
import l2server.gameserver.handler.SkillHandler
import l2server.gameserver.instancemanager.*
import l2server.gameserver.model.*
import l2server.gameserver.model.actor.instance.*
import l2server.gameserver.model.actor.knownlist.CharKnownList
import l2server.gameserver.model.actor.position.CharPosition
import l2server.gameserver.model.actor.stat.CharStat
import l2server.gameserver.model.actor.status.CharStatus
import l2server.gameserver.model.itemcontainer.Inventory
import l2server.gameserver.model.quest.Quest
import l2server.gameserver.network.SystemMessageId
import l2server.gameserver.network.serverpackets.*
import l2server.gameserver.network.serverpackets.FlyToLocation.FlyType
import l2server.gameserver.network.serverpackets.StatusUpdate.StatusUpdateDisplay
import l2server.gameserver.pathfinding.AbstractNodeLoc
import l2server.gameserver.pathfinding.PathFinding
import l2server.gameserver.stats.Calculator
import l2server.gameserver.stats.Formulas
import l2server.gameserver.stats.Stats
import l2server.gameserver.stats.VisualEffect
import l2server.gameserver.stats.effects.EffectChanceSkillTrigger
import l2server.gameserver.stats.effects.EffectSpatialTrap
import l2server.gameserver.stats.funcs.Func
import l2server.gameserver.stats.skills.SkillAgathion
import l2server.gameserver.stats.skills.SkillMount
import l2server.gameserver.stats.skills.SkillSummon
import l2server.gameserver.taskmanager.AttackStanceTaskManager
import l2server.gameserver.templates.chars.CreatureTemplate
import l2server.gameserver.templates.chars.NpcTemplate
import l2server.gameserver.templates.item.ItemTemplate
import l2server.gameserver.templates.item.WeaponTemplate
import l2server.gameserver.templates.item.WeaponType
import l2server.gameserver.templates.skills.*
import l2server.gameserver.util.Util
import l2server.util.Point3D
import l2server.util.Rnd
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Future

/**
 * Mother class of all character objects of the world (PC, NPC...)<BR></BR><BR></BR>
 *
 *
 * Creature :<BR></BR><BR></BR>
 *  * L2CastleGuardInstance
 *  * DoorInstance
 *  * NpcInstance
 *  * L2PlayableInstance <BR></BR><BR></BR>
 *
 *
 *
 *
 * <B><U> Concept of CreatureTemplate</U> :</B><BR></BR><BR></BR>
 * Each Creature owns generic and static properties (ex : all Keltir have the same number of HP...).
 * All of those properties are stored in a different template for each type of Creature.
 * Each template is loaded once in the server cache memory (reduce memory use).
 * When a new instance of Creature is spawned, server just create a link between the instance and the template.
 * This link is stored in <B>template</B><BR></BR><BR></BR>
 *
 * @version $Revision: 1.53.2.45.2.34 $ $Date: 2005/04/11 10:06:08 $
 */
abstract class Creature(objectId: Int, template: CreatureTemplate?): WorldObject(objectId) {

	private var attackByList: MutableSet<Creature>? = null
	@Volatile
	var isCastingNow1 = false
		private set
	@Volatile
	private var isCastingNow2 = false
	@Volatile
	var isCastingSimultaneouslyNow = false
	var lastSkillCast: Skill? = null
	var lastSimultaneousSkillCast: Skill? = null

	/**
	 * Return True if the Creature is dead or use fake death.
	 */
	open var isAlikeDead = false
		protected set
	open var isImmobilized = false
	/**
	 * Set the overloaded status of the Creature is overloaded (if True, the Player can't take more item).
	 */
	var isOverloaded = false // the char is carrying too much
	var isParalyzed = false
		get() = field || isAffected(EffectType.PARALYZE.mask) || isAffected(EffectType.PETRIFY.mask)
	private var isPendingRevive = false
	/**
	 * Return True if the Creature is running.
	 */
	//synchronized (character.getKnownList().getKnownPlayers())
	var isRunning = false
		set(value) {
			if (isInvul && this is ArmyMonsterInstance) {
				return
			}

			field = value

			if (this is Npc && this.isInvisible) {
				return
			}

			if (runSpeed != 0) {
				broadcastPacket(ChangeMoveType(this))
			}

			if (this is Player) {
				broadcastUserInfo()
			} else if (this is Summon) {
				broadcastStatusUpdate()
			} else if (this is Npc) {
				val plrs = knownList.knownPlayers.values
				run {
					for (player in plrs) {
						if (player == null) {
							continue
						}

						if (runSpeed == 0) {
							player.sendPacket(ServerObjectInfo(this as Npc, player))
						} else {
							player.sendPacket(NpcInfo(this as Npc, player))
						}
					}
				}
			}
		}
	var isRefusingBuffs = false
		private set // Tenkai custom - refuse buffs from out of party
	/**
	 * @return Returns the showSummonAnimation.
	 */
	/**
	 * @param showSummonAnimation The showSummonAnimation to set.
	 */
	var isShowSummonAnimation = false
	open var isTeleporting = false
	open var isInvul = false
		get() = field || isTeleporting || isAffected(EffectType.INVINCIBLE.mask)
	var isMortal = true // Char will die when HP decreased to 0
	var isFlying = false

	open var stat: CharStat? = null
	open var status: CharStatus? = null
	// The link on the CreatureTemplate object containing generic and static properties of this Creature type (ex : Max HP, Speed...)
	/**
	 * Return the Title of the Creature.
	 */
	/**
	 * Set the Title of the Creature.
	 */
	var title: String? = null
		set(value) = if (value == null) {
			field = ""
		} else {
			field = if (value.length > 16) value.substring(0, 15) else value
		}
	private var hpUpdateIncCheck = .0
	private var hpUpdateDecCheck = .0
	private var hpUpdateInterval = .0

	/**
	 * Table of Calculators containing all used calculator
	 */
	var calculators: Array<Calculator?>? = null
		private set

	/**
	 * HashMap(Integer, Skill) containing all skills of the Creature
	 */
	open var skills: MutableMap<Int, Skill>? = null
		protected set
	/**
	 * HashMap containing the active chance skills on this character
	 */
	var chanceSkills: ChanceSkillList? = null
		private set

	/**
	 * Current force buff this caster is casting to a target
	 */
	var fusionSkill: FusionSkill? = null

	var skillCastPosition: Point3D? = null

	private val zones = ByteArray(25)
	protected var zoneValidateCounter: Byte = 4

	protected var debugger: Creature? = null

	/**
	 * @return True if debugging is enabled for this Creature
	 */
	val isDebug: Boolean
		get() = debugger != null

	/**
	 * Returns character inventory, default null, overridden in Playable types and in L2NPcInstance
	 */
	open val inventory: Inventory?
		get() = null

	/**
	 * This will return true if the player is transformed,<br></br>
	 * but if the player is not transformed it will return false.
	 *
	 * @return transformation status
	 */
	open val isTransformed: Boolean
		get() = false

	/**
	 * This will return true if the player is GM,<br></br>
	 * but if the player is not GM it will return false.
	 *
	 * @return GM status
	 */
	open val isGM: Boolean
		get() = false

	/**
	 * Return True if the Creature is RaidBoss or his minion.
	 */
	open val isRaid: Boolean
		get() = false

	/**
	 * Return True if the Creature is minion.
	 */
	open val isMinion: Boolean
		get() = false

	/**
	 * Return True if the Creature is minion of RaidBoss.
	 */
	open val isRaidMinion: Boolean
		get() = false

	val isAfraid: Boolean
		get() = isAffected(EffectType.FEAR.mask)

	val isInLove: Boolean
		get() = isAffected(EffectType.LOVE.mask)

	/**
	 * Return True if the Creature can't use its skills (ex : stun, sleep...).
	 */
	val isAllSkillsDisabled: Boolean
		get() = allSkillsDisabled || isStunned || isSleeping || isParalyzed

	/**
	 * Return True if the Creature can't attack (stun, sleep, attackEndTime, fakeDeath, paralyse, attackMute).
	 */
	open val isAttackingDisabled: Boolean
		get() = isFlying || isStunned || isSleeping || attackEndTime > TimeController.getGameTicks() || isAlikeDead || isParalyzed ||
				isPhysicalAttackMuted || isCoreAIDisabled

	val isConfused: Boolean
		get() = isAffected(EffectType.CONFUSION.mask)

	val isMuted: Boolean
		get() = isAffected(EffectType.MUTE.mask)

	val isPhysicalMuted: Boolean
		get() = isAffected(EffectType.PHYSICAL_MUTE.mask)

	val isPhysicalAttackMuted: Boolean
		get() = isAffected(EffectType.PHYSICAL_ATTACK_MUTE.mask)

	/**
	 * Return True if the Creature can't move (stun, root, sleep, overload, paralyzed).
	 */
	// check for isTeleporting to prevent teleport cheating (if appear packet not received)
	open val isMovementDisabled: Boolean
		get() = isStunned || isRooted || isSleeping || isOverloaded || isParalyzed || isImmobilized || isAlikeDead || isTeleporting

	/**
	 * Return True if the Creature can not be controlled by the player (confused, afraid).
	 */
	val isOutOfControl: Boolean
		get() = isConfused || isAfraid || isInLove

	open val isDisarmed: Boolean
		get() = isAffected(EffectType.DISARM.mask)

	val isArmorDisarmed: Boolean
		get() = isAffected(EffectType.DISARM_ARMOR.mask)

	val isRooted: Boolean
		get() = isAffected(EffectType.ROOT.mask)

	val isSleeping: Boolean
		get() = isAffected(EffectType.SLEEP.mask)

	val isStunned: Boolean
		get() = isAffected(EffectType.STUN.mask)

	val isBetrayed: Boolean
		get() = isAffected(EffectType.BETRAY.mask)

	val isInSpatialTrap: Boolean
		get() = isAffected(EffectType.SPATIAL_TRAP.mask)

	open val isUndead: Boolean
		get() = false

	val isResurrectionBlocked: Boolean
		get() = isAffected(EffectType.BLOCK_RESURRECTION.mask)

	override val knownList: CharKnownList
		get() = super.knownList as CharKnownList

	override val position: CharPosition
		get() = super.position as CharPosition

	private val abnormalEffects = CopyOnWriteArraySet<Int>()

	protected var effects = CharEffectList(this)

	// Property - Public

	/**
	 * Return a map of 16 bits (0x0000) containing all abnormal effect in progress for this Creature.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * In Server->Client packet, each effect is represented by 1 bit of the map (ex : BLEEDING = 0x0001 (bit 1), SLEEP = 0x0080 (bit 8)...).
	 * The map is calculated by applying a BINARY OR operation on each effect.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Example of use </U> :</B><BR></BR><BR></BR>
	 *  *  Server Packet : CharInfo, NpcInfo, NpcInfoPoly, UserInfo...<BR></BR><BR></BR>
	 */
	val abnormalEffect: Set<Int>
		get() {
			val result = HashSet<Int>()
			synchronized(abnormalEffects) {
				result.addAll(abnormalEffects)
			}

			return result
		}

	/**
	 * Return all active skills effects in progress on the Creature.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * All active skills effects in progress on the Creature are identified in <B>effects</B>.
	 * The Integer key of effects is the Skill Identifier that has created the effect.<BR></BR><BR></BR>
	 *
	 * @return A table containing all active skills effect in progress on the Creature
	 */
	val allEffects: Array<Abnormal>
		get() = effects.allEffects

	val allDebuffs: Array<Abnormal>
		get() = effects.allDebuffs

	/**
	 * Table containing all skillId that are disabled
	 */
	var disabledSkills: MutableMap<Int, Long>? = null
		protected set
	private var allSkillsDisabled: Boolean = false

	//	private int flyingRunSpeed;
	//	private int floatingWalkSpeed;
	//	private int flyingWalkSpeed;
	//	private int floatingRunSpeed;

	/**
	 * Movement data of this Creature
	 */
	protected var move: MoveData? = null

	/**
	 * Orientation of the Creature
	 */
	/**
	 * Return the orientation of the Creature.<BR></BR><BR></BR>
	 */
	/**
	 * Set the orientation of the Creature.<BR></BR><BR></BR>
	 */
	var heading: Int = 0

	/**
	 * L2Charcater targeted by the Creature
	 */
	/**
	 * Return the WorldObject targeted or null.<BR></BR><BR></BR>
	 */
	/**
	 * Target a WorldObject (add the target to the Creature target, knownObject and Creature to KnownObject of the WorldObject).<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * The WorldObject (including Creature) targeted is identified in <B>target</B> of the Creature<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Set the target of Creature to WorldObject
	 *  * If necessary, add WorldObject to knownObject of the Creature
	 *  * If necessary, add Creature to KnownObject of the WorldObject
	 *  * If object==null, cancel Attak or Cast <BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Overridden in </U> :</B><BR></BR><BR></BR>
	 *  *  Player : Remove the Player from the old target statusListener and add it to the new target if it was a Creature<BR></BR><BR></BR>
	 *
	 * @param object L2object to target
	 */
	open var target: WorldObject? = null
		set(`object`) {
			var `object` = `object`
			if (`object` != null && !`object`.isVisible()) {
				`object` = null
			}

			if (`object` != null && `object` !== this.target) {
				knownList.addKnownObject(`object`)
				`object`.knownList.addKnownObject(this)
			}

			field = `object`
		}

	// set by the start of attack, in game ticks
	var attackEndTime: Int = 0
		private set
	/**
	 * Returns body part (paperdoll slot) we are targeting right now
	 */
	var attackingBodyPart: Int = 0
		private set
	private var disableBowAttackEndTime: Int = 0
	private var disableCrossBowAttackEndTime: Int = 0

	var castInterruptTime: Int = 0
		private set

	protected var ai: CreatureAI? = null

	/**
	 * Future Skill Cast
	 */
	var skillCast: Future<*>? = null
	protected var skillCast2: Future<*>? = null
	protected var simultSkillCast: Future<*>? = null

	val xdestination: Int
		get() {
			val m = move

			return m?.xDestination ?: x

		}

	/**
	 * Return the Y destination of the Creature or the Y position if not in movement.<BR></BR><BR></BR>
	 */
	val ydestination: Int
		get() {
			val m = move

			return m?.yDestination ?: y

		}

	/**
	 * Return the Z destination of the Creature or the Z position if not in movement.<BR></BR><BR></BR>
	 */
	val zdestination: Int
		get() {
			val m = move

			return m?.zDestination ?: z

		}

	/**
	 * Return True if the Creature is in combat.<BR></BR><BR></BR>
	 */
	open val isInCombat: Boolean
		get() = hasAI() && (getAI()!!.attackTarget != null || getAI()!!.isAutoAttacking)

	/**
	 * Return True if the Creature is moving.<BR></BR><BR></BR>
	 */
	val isMoving: Boolean
		get() = move != null

	/**
	 * Return True if the Creature is travelling a calculated path.<BR></BR><BR></BR>
	 */
	val isOnGeodataPath: Boolean
		get() {
			val m = move ?: return false
			return if (m.onGeodataPathIndex == -1) {
				false
			} else m.onGeodataPathIndex != m.geoPath!!.size - 1
		}

	/**
	 * Return True if the Creature is casting.<BR></BR><BR></BR>
	 */
	var isCastingNow: Boolean
		get() = if (canDoubleCast()) {
			isCastingNow1 || isCastingNow2
		} else isCastingNow1
		set(value) {
			isCastingNow1 = value
			lastCast1 = true
		}

	private var lastCast1: Boolean = false

	/**
	 * Return True if the Creature is attacking.<BR></BR><BR></BR>
	 */
	open val isAttackingNow: Boolean
		get() = attackEndTime > TimeController.getGameTicks()

	/**
	 * Return True if the Creature has aborted its attack.<BR></BR><BR></BR>
	 */
	val isAttackAborted: Boolean
		get() = attackingBodyPart <= 0

	/**
	 * Return the identifier of the WorldObject targeted or -1.<BR></BR><BR></BR>
	 */
	val targetId: Int
		get() = if (this.target != null) {
			this.target!!.objectId
		} else -1

	/**
	 * Return the active weapon instance (always equiped in the right hand).<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Overridden in </U> :</B><BR></BR><BR></BR>
	 *  *  Player<BR></BR><BR></BR>
	 */
	abstract val activeWeaponInstance: Item?

	/**
	 * Return the active weapon item (always equiped in the right hand).<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Overridden in </U> :</B><BR></BR><BR></BR>
	 *  *  Player<BR></BR><BR></BR>
	 */
	abstract val activeWeaponItem: WeaponTemplate?

	/**
	 * Return the secondary weapon instance (always equiped in the left hand).<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Overridden in </U> :</B><BR></BR><BR></BR>
	 *  *  Player<BR></BR><BR></BR>
	 */
	abstract val secondaryWeaponInstance: Item

	/**
	 * Return the secondary [ItemTemplate] item (always equiped in the left hand).<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Overridden in </U> :</B><BR></BR><BR></BR>
	 *  *  Player<BR></BR><BR></BR>
	 */
	abstract val secondaryWeaponItem: ItemTemplate

	/**
	 * return true if this character is inside an active grid.
	 */
	val isInActiveRegion: Boolean
		get() {
			val region = worldRegion
			return region != null && region.isActive
		}

	/**
	 * Return True if the Creature has a Party in progress.<BR></BR><BR></BR>
	 */
	open val isInParty: Boolean
		get() = false

	/**
	 * Return the L2Party object of the Creature.<BR></BR><BR></BR>
	 */
	open val party: L2Party?
		get() = null

	/**
	 * Return True if the Creature use a dual weapon.<BR></BR><BR></BR>
	 */
	open val isUsingDualWeapon: Boolean
		get() = false

	/**
	 * Return all skills own by the Creature in a table of Skill.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * All skills own by a Creature are identified in <B>skills</B> the Creature <BR></BR><BR></BR>
	 */
	open val allSkills: Array<Skill>
		get() = skills?.values?.toTypedArray() ?: emptyArray()

	/**
	 * Return the number of buffs affecting this Creature.<BR></BR><BR></BR>
	 *
	 * @return The number of Buffs affecting this Creature
	 */
	val buffCount: Int
		get() = effects.buffCount

	val danceCount: Int
		get() = effects.danceCount

	//TODO TEST LasTravel permanent at behind stats while under Distortion Ertheia Buff
	val isBehindTarget: Boolean
		get() = if (calcStat(Stats.IS_BEHIND, 0.0, this, null) > 0) {
			true
		} else isBehind(target)

	val isInFrontOfTarget: Boolean
		get() {
			val target = target
			return if (target is Creature) {
				isInFrontOf(target as Creature?)
			} else {
				false
			}
		}

	val levelMod: Double
		get() = if (level > 99) {
			(89.0 + level.toDouble() + 4.0 * (level - 99.0)) / 100.0
		} else (89.0 + level) / 100.0

	var isCoreAIDisabled = false
		private set

	/**
	 * Return a multiplier based on weapon random damage<BR></BR><BR></BR>
	 */
	val randomDamageMultiplier: Double
		get() {
			val activeWeapon = activeWeaponItem
			val random: Int

			if (activeWeapon != null) {
				random = activeWeapon.randomDamage
			} else {
				random = 5 + Math.sqrt(level.toDouble()).toInt()
			}

			return 1 + Rnd.get(-random, random).toDouble() / 100
		}

	/**
	 * Not Implemented.<BR></BR><BR></BR>
	 */
	abstract val level: Int

	// Property - Public
	val accuracy: Int
		get() = stat!!.accuracy

	val mAccuracy: Int
		get() = stat!!.mAccuracy

	val attackSpeedMultiplier: Float
		get() = stat!!.attackSpeedMultiplier

	val CON: Int
		get() = stat!!.con

	val DEX: Int
		get() = stat!!.dex

	val INT: Int
		get() = stat!!.int

	val maxCp: Int
		get() = stat!!.maxCp

	val mAtkSpd: Int
		get() = stat!!.mAtkSpd

	open val maxMp: Int
		get() = stat!!.maxMp

	open val maxHp: Int
		get() = stat!!.maxHp

	val MEN: Int
		get() = stat!!.men

	val LUC: Int
		get() = stat!!.luc

	val CHA: Int
		get() = stat!!.cha

	val movementSpeedMultiplier: Float
		get() = stat!!.movementSpeedMultiplier

	val skillMastery: Double
		get() = stat!!.skillMastery

	/**
	 * Return max visible HP for display purpose.
	 * Calculated by applying non-visible HP limit
	 * getMaxHp() = getMaxVisibleHp() * limitHp
	 */
	val maxVisibleHp: Int
		get() = stat!!.maxVisibleHp

	val pAtkSpd: Int
		get() = stat!!.pAtkSpd

	val physicalAttackRange: Int
		get() = stat!!.physicalAttackRange

	val runSpeed: Int
		get() = stat!!.runSpeed

	val shldDef: Int
		get() = stat!!.shldDef

	val STR: Int
		get() = stat!!.str

	val walkSpeed: Int
		get() = stat!!.walkSpeed

	val WIT: Int
		get() = stat!!.wit

	// Property - Public
	val currentCp: Double
		get() = status!!.currentCp

	var currentHp: Double
		get() = status!!.currentHp
		set(newHp) {
			if (this !is EventGolemInstance) {
				status!!.currentHp = newHp
			}
		}

	val currentMp: Double
		get() = status!!.currentMp

	open val isChampion: Boolean
		get() = false

	/**
	 * Check player max buff count
	 *
	 * @return max buff count
	 */
	//TODO MOVE TO STAT?
	val maxBuffCount: Int
		get() {
			var maxBuffs = Config.BUFFS_MAX_AMOUNT + Math.max(0,
					getSkillLevelHash(Skill.SKILL_DIVINE_INSPIRATION) + Math.max(0, getSkillLevelHash(Skill.SKILL_DIVINE_EXPANSION)))

			if (Config.isServer(Config.TENKAI) && this is Summon) {
				maxBuffs += 8
			}

			return maxBuffs
		}

	var continuousDebuffTargets: Array<WorldObject>? = null

	open val attackElement: Byte
		get() = stat!!.attackElement

	val defenseElement: Byte
		get() = stat!!.defenseElement

	var faceoffTarget: Creature? = null

	/**
	 * Sets Creature instance, to which debug packets will be send
	 *
	 */
	fun setDebug(d: Creature?) {
		debugger = d
	}

	/**
	 * Send debug packet.
	 *
	 */
	fun sendDebugPacket(pkt: L2GameServerPacket) {
		if (debugger != null) {
			debugger!!.sendPacket(pkt)
		}
	}

	/**
	 * Send debug text string
	 *
	 * @param msg The message to send
	 */
	fun sendDebugMessage(msg: String) {
		if (debugger != null) {
			debugger!!.sendMessage(msg)
		}
	}

	open fun destroyItemByItemId(process: String, itemId: Int, count: Long, reference: WorldObject?, sendMessage: Boolean): Boolean {
		// Default: NPCs consume virtual items for their skills
		// TODO: should be logged if even happens.. should be false
		return true
	}

	open fun destroyItem(process: String, objectId: Int, count: Long, reference: WorldObject, sendMessage: Boolean): Boolean {
		// Default: NPCs consume virtual items for their skills
		// TODO: should be logged if even happens.. should be false
		return true
	}

	fun isInsideZone(zone: Byte): Boolean {
		val instance = InstanceManager.getInstance().getInstance(instanceId)
		when (zone) {
			CreatureZone.ZONE_PVP -> {
				return if (instance != null && instance.isPvPInstance || Curfew.getInstance().onlyPeaceTown != -1 ||
						actingPlayer != null && actingPlayer!!.isPlayingEvent) {
					true //zones[ZONE_PEACE] == 0;
				} else zones[CreatureZone.ZONE_PVP.toInt()] > 0 && zones[CreatureZone.ZONE_PEACE.toInt()].toInt() == 0
			}
			CreatureZone.ZONE_PEACE -> if (instance != null && instance.isPvPInstance || actingPlayer != null && actingPlayer!!.isPlayingEvent) {
				return false
			}
		}
		return zones[zone.toInt()] > 0
	}

	fun setInsideZone(zone: Byte, state: Boolean) {
		if (state) {
			zones[zone.toInt()]++
		} else {
			zones[zone.toInt()]--
			if (zones[zone.toInt()] < 0) {
				zones[zone.toInt()] = 0
			}
		}
	}

	/**
	 * This will untransform a player if they are an instance of L2Pcinstance
	 * and if they are transformed.
	 *
	 * @return untransform
	 */
	open fun unTransform(removeEffects: Boolean) {
		// Just a place holder
	}

	open var template: CreatureTemplate? = null

	init {
		this.template = template
		instanceType = InstanceType.L2Character
		initCharStat()
		initCharStatus()

		if (this is DoorInstance || this is Npc) {
			// Copy the Standard Calcultors of the L2NPCInstance in calculators
			calculators = NPC_STD_CALCULATOR
		} else {
			// If Creature is a Player or a Summon, create the basic calculator set
			calculators = arrayOfNulls(Stats.NUM_STATS)
			Formulas.addFuncsToNewCharacter(this)
		}

		skills = ConcurrentHashMap()
		if (template != null && (this is Npc || this is Summon)) {
			// Copy the skills of the NPC from its template to the Creature Instance
			// The skills list can be affected by spell effects so it's necessary to make a copy
			// to avoid that a spell affecting a L2NPCInstance, affects others L2NPCInstance of the same type too.
			if ((template as NpcTemplate).skills != null) {
				for (skill in (template as NpcTemplate).skills.values) {
					addSkill(skill)
				}
			}
		}

		isInvul = true
	}// Set its template to the new Creature

	protected fun initCharStatusUpdateValues() {
		hpUpdateIncCheck = maxVisibleHp.toDouble()
		hpUpdateInterval = hpUpdateIncCheck / 352.0 // MAX_HP div MAX_HP_BAR_PX
		hpUpdateDecCheck = hpUpdateIncCheck - hpUpdateInterval
	}

	/**
	 * Remove the Creature from the world when the decay task is launched.<BR></BR><BR></BR>
	 *
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from allObjects of World </B></FONT><BR></BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR></BR><BR></BR>
	 */
	open fun onDecay() {
		val reg = worldRegion
		decayMe()
		reg?.removeFromZones(this)
	}

	override fun onSpawn() {
		super.onSpawn()
		revalidateZone(true)
	}

	open fun onTeleported() {
		if (!isTeleporting) {
			return
		}

		spawnMe(position.x, position.y, position.z)

		isTeleporting = false

		if (isPendingRevive) {
			doRevive()
		}
	}

	/**
	 * Add Creature instance that is attacking to the attacker list.<BR></BR><BR></BR>
	 *
	 * @param player The Creature that attacks this one
	 *
	 *
	 * <B><U> Overridden in </U> :</B><BR></BR><BR></BR>
	 *  *  Attackable : Add to list only for attackables, not all other NPCs<BR></BR><BR></BR>
	 */
	open fun addAttackerToAttackByList(player: Creature) {
		// DS: moved to Attackable
	}

	/**
	 * Send a packet to the Creature AND to all Player in the KnownPlayers of the Creature.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * Player in the detection area of the Creature are identified in <B>knownPlayers</B>.
	 * In order to inform other players of state modification on the Creature, server just need to go through knownPlayers to send Server->Client Packet<BR></BR><BR></BR>
	 */
	open fun broadcastPacket(mov: L2GameServerPacket) {
		val plrs = knownList.knownPlayers.values
		//synchronized (getKnownList().getKnownPlayers())
		run {
			for (player in plrs) {
				player?.sendPacket(mov)
			}
		}
	}

	/**
	 * Send a packet to the Creature AND to all Player in the radius (max knownlist radius) from the Creature.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * Player in the detection area of the Creature are identified in <B>knownPlayers</B>.
	 * In order to inform other players of state modification on the Creature, server just need to go through knownPlayers to send Server->Client Packet<BR></BR><BR></BR>
	 */
	open fun broadcastPacket(mov: L2GameServerPacket, radiusInKnownlist: Int) {
		val plrs = knownList.knownPlayers.values
		//synchronized (getKnownList().getKnownPlayers())
		run {
			for (player in plrs) {
				if (player != null && isInsideRadius(player, radiusInKnownlist, false, false)) {
					player.sendPacket(mov)
				}
			}
		}
	}

	/**
	 * Returns true if hp update should be done, false if not
	 *
	 * @return boolean
	 */
	protected fun needHpUpdate(barPixels: Int): Boolean {
		val currentHp = currentHp
		val maxHp = maxVisibleHp.toDouble()

		if (currentHp <= 1.0 || maxHp < barPixels) {
			return true
		}

		if (currentHp <= hpUpdateDecCheck || currentHp >= hpUpdateIncCheck) {
			if (currentHp == maxHp) {
				hpUpdateIncCheck = currentHp + 1
				hpUpdateDecCheck = currentHp - hpUpdateInterval
			} else {
				val doubleMulti = currentHp / hpUpdateInterval
				var intMulti = doubleMulti.toInt()

				hpUpdateDecCheck = hpUpdateInterval * if (doubleMulti < intMulti) intMulti-- else intMulti
				hpUpdateIncCheck = hpUpdateDecCheck + hpUpdateInterval
			}

			return true
		}

		return false
	}

	open fun broadcastStatusUpdate() {
		broadcastStatusUpdate(null, StatusUpdateDisplay.NONE)
	}

	/**
	 * Send the Server->Client packet StatusUpdate with current HP and MP to all other Player to inform.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Create the Server->Client packet StatusUpdate with current HP and MP
	 *  * Send the Server->Client packet StatusUpdate with current HP and MP to all
	 * Creature called statusListener that must be informed of HP/MP updates of this Creature <BR></BR><BR></BR>
	 *
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND CP information</B></FONT><BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Overridden in </U> :</B><BR></BR><BR></BR>
	 *  *  Player : Send current HP,MP and CP to the Player and only current HP, MP and Level to all other Player of the Party<BR></BR><BR></BR>
	 */
	open fun broadcastStatusUpdate(causer: Creature?, display: StatusUpdateDisplay) {
		if (this is Summon) {
			return
		}

		if (status!!.statusListener.isEmpty() && knownList.getKnownPlayersInRadius(200).isEmpty()) {
			return
		}

		if (display == StatusUpdateDisplay.NONE && !needHpUpdate(352)) {
			return
		}

		if (Config.DEBUG) {
			log.debug("Broadcast Status Update for $objectId ($name). HP: $currentHp")
		}

		// Create the Server->Client packet StatusUpdate with current HP
		val su = StatusUpdate(this, causer, display)
		su.addAttribute(StatusUpdate.CUR_HP, currentHp.toInt())

		// Go through the StatusListener
		// Send the Server->Client packet StatusUpdate with current HP and MP
		if (this is Attackable) {
			for (temp in knownList.getKnownPlayersInRadius(600)) {
				temp?.sendPacket(su)
			}
		}

		for (temp in status!!.statusListener) {
			if (temp != null && !temp.isInsideRadius(this, 600, false, false)) {
				temp.sendPacket(su)
			}
		}
	}

	fun broadcastAbnormalStatusUpdate() {
		if (status!!.statusListener.isEmpty()) {
			return
		}

		// Create the Server->Client packet AbnormalStatusUpdate
		val asu = AbnormalStatusUpdateFromTarget(this)

		// Go through the StatusListener
		// Send the Server->Client packet StatusUpdate with current HP and MP

		for (temp in status!!.statusListener) {
			temp?.sendPacket(asu)
		}
	}

	/**
	 * Not Implemented.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Overridden in </U> :</B><BR></BR><BR></BR>
	 *  *  Player<BR></BR><BR></BR>
	 */
	open fun sendMessage(text: String) {
		// default implementation
	}

	/**
	 * Teleport a Creature and its pet if necessary.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Stop the movement of the Creature
	 *  * Set the x,y,z position of the WorldObject and if necessary modify its worldRegion
	 *  * Send a Server->Client packet TeleportToLocationt to the Creature AND to all Player in its KnownPlayers
	 *  * Modify the position of the pet if necessary<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Overridden in </U> :</B><BR></BR><BR></BR>
	 *  *  Player<BR></BR><BR></BR>
	 */
	open fun teleToLocation(x: Int, y: Int, z: Int, heading: Int, allowRandomOffset: Boolean) {
		var x = x
		var y = y
		var z = z
		// Stop movement
		stopMove(null, false)
		abortAttack()
		abortCast()

		isTeleporting = true
		target = null

		getAI()!!.intention = CtrlIntention.AI_INTENTION_ACTIVE

		if (Config.OFFSET_ON_TELEPORT_ENABLED && allowRandomOffset) {
			x += Rnd.get(-Config.MAX_OFFSET_ON_TELEPORT, Config.MAX_OFFSET_ON_TELEPORT)
			y += Rnd.get(-Config.MAX_OFFSET_ON_TELEPORT, Config.MAX_OFFSET_ON_TELEPORT)
		}

		z += 5

		if (Config.DEBUG) {
			log.debug("Teleporting to: $x, $y, $z")
		}

		// Send a Server->Client packet TeleportToLocationt to the Creature AND to all Player in the KnownPlayers of the Creature
		broadcastPacket(TeleportToLocation(this, x, y, z, heading))
		sendPacket(ExTeleportToLocationActivate(objectId, x, y, z, heading))

		// remove the object from its old location
		decayMe()

		// Set the x,y,z position of the WorldObject and if necessary modify its worldRegion
		position.setXYZ(x, y, z)

		// temporary fix for heading on teleports
		if (heading != 0) {
			position.heading = heading
		}

		// allow recall of the detached characters
		if (this !is Player || this.client != null && this.client.isDetached ||
				this is ApInstance) {
			onTeleported()
		}

		revalidateZone(true)
	}

	fun teleToLocation(x: Int, y: Int, z: Int) {
		teleToLocation(x, y, z, heading, false)
	}

	fun teleToLocation(x: Int, y: Int, z: Int, allowRandomOffset: Boolean) {
		teleToLocation(x, y, z, heading, allowRandomOffset)
	}

	fun teleToLocation(loc: Location, allowRandomOffset: Boolean) {
		val x = loc.x
		val y = loc.y
		val z = loc.z
		teleToLocation(x, y, z, heading, allowRandomOffset)
	}

	fun teleToLocation(teleportWhere: TeleportWhereType) {
		teleToLocation(MapRegionTable.getInstance().getTeleToLocation(this, teleportWhere), true)
	}

	/**
	 * Launch a physical attack against a target (Simple, Bow, Pole or Dual).<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Get the active weapon (always equiped in the right hand) <BR></BR><BR></BR>
	 *  * If weapon is a bow, check for arrows, MP and bow re-use delay (if necessary, equip the Player with arrows in left hand)
	 *  * If weapon is a bow, consume MP and set the new period of bow non re-use <BR></BR><BR></BR>
	 *  * Get the Attack Speed of the Creature (delay (in milliseconds) before next attack)
	 *  * Select the type of attack to start (Simple, Bow, Pole or Dual) and verify if SoulShot are charged then start calculation
	 *  * If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack to the Creature AND to all Player in the KnownPlayers of the Creature
	 *  * Notify AI with EVT_READY_TO_ACT<BR></BR><BR></BR>
	 *
	 * @param target The Creature targeted
	 */
	fun doAttack(target: Creature?) {
		if (Config.DEBUG) {
			log.debug("$name doAttack: target=$target")
		}

		if (!isAlikeDead && target != null) {
			if (this is Npc && target.isAlikeDead || !knownList.knowsObject(target)) {
				getAI()!!.intention = CtrlIntention.AI_INTENTION_ACTIVE
				sendPacket(ActionFailed.STATIC_PACKET)
				return
			} else if (this is Player) {
				if (target.isDead()) {
					getAI()!!.intention = CtrlIntention.AI_INTENTION_ACTIVE
					sendPacket(ActionFailed.STATIC_PACKET)
					return
				}

				val actor = this
				/*
				 * Players riding wyvern or with special (flying) transformations can't do melee attacks, only with skills
				 */
				if (actor.isMounted && actor.mountNpcId == 12621 || actor.isTransformed && !actor.transformation.canDoMeleeAttack()) {
					sendPacket(ActionFailed.STATIC_PACKET)
					return
				}
			}
		}

		if (isAttackingDisabled) {
			return
		}

		if (this is Player) {
			val player = this

			if (player.inObserverMode()) {
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE))
				sendPacket(ActionFailed.STATIC_PACKET)
				return
			}

			if (target!!.actingPlayer != null && player.siegeState > 0 && isInsideZone(CreatureZone.ZONE_SIEGE) &&
					target.actingPlayer!!.siegeState == player.siegeState && target.actingPlayer !== this &&
					target.actingPlayer!!.siegeSide == player.siegeSide && !Config.isServer(Config.TENKAI)) {
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS))
				sendPacket(ActionFailed.STATIC_PACKET)
				return
			}

			// Checking if target has moved to peace zone
			if (target.isInsidePeaceZone(this)) {
				getAI()!!.intention = CtrlIntention.AI_INTENTION_ACTIVE
				sendPacket(ActionFailed.STATIC_PACKET)
				return
			}
			// TODO: unhardcode this to support boolean if with that weapon u can attack or not (for ex transform weapons)
			if (player.activeWeaponItem != null && player.activeWeaponItem!!.itemId == 9819) {
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THAT_WEAPON_CANT_ATTACK))
				sendPacket(ActionFailed.STATIC_PACKET)
				return
			}
		} else if (isInsidePeaceZone(this, target)) {
			getAI()!!.intention = CtrlIntention.AI_INTENTION_ACTIVE
			sendPacket(ActionFailed.STATIC_PACKET)
			return
		}

		stopEffectsOnAction(null)

		// Get the active weapon instance (always equiped in the right hand)
		val weaponInst = activeWeaponInstance

		// Get the active weapon item corresponding to the active weapon instance (always equiped in the right hand)
		val weaponItem = activeWeaponItem

		if (weaponItem != null && weaponItem.itemType == WeaponType.FISHINGROD) {
			//	You can't make an attack with a fishing pole.
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_ATTACK_WITH_FISHING_POLE))
			getAI()!!.intention = CtrlIntention.AI_INTENTION_IDLE

			sendPacket(ActionFailed.STATIC_PACKET)
			return
		}

		// GeoData Los Check here (or dz > 1000)
		if (!GeoData.getInstance().canSeeTarget(this, target)) {
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SEE_TARGET))
			getAI()!!.intention = CtrlIntention.AI_INTENTION_ACTIVE
			sendPacket(ActionFailed.STATIC_PACKET)
			return
		}

		// BOW and CROSSBOW checks
		if (weaponItem != null && !isTransformed) {
			if (weaponItem.itemType == WeaponType.BOW) {
				//Check for arrows and MP
				if (this is Player) {
					// Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True
					if (!checkAndEquipArrows()) {
						// Cancel the action because the Player have no arrow
						getAI()!!.intention = CtrlIntention.AI_INTENTION_IDLE
						sendPacket(ActionFailed.STATIC_PACKET)
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ARROWS))
						return
					}

					// Verify if the bow can be use
					if (disableBowAttackEndTime <= TimeController.getGameTicks()) {
						// Verify if Player owns enough MP
						val saMpConsume = stat!!.calcStat(Stats.MP_CONSUME, 0.0, null, null).toInt()
						var mpConsume = if (saMpConsume == 0) weaponItem.mpConsume else saMpConsume
						mpConsume = calcStat(Stats.BOW_MP_CONSUME_RATE, mpConsume.toDouble(), null, null).toInt()

						if (currentMp < mpConsume) {
							// If Player doesn't have enough MP, stop the attack
							ThreadPoolManager.getInstance().scheduleAi(NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000)
							sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_MP))
							sendPacket(ActionFailed.STATIC_PACKET)
							return
						}
						// If Player have enough MP, the bow consumes it
						if (mpConsume > 0) {
							status!!.reduceMp(mpConsume.toDouble())
						}

						// Set the period of bow no re-use
						disableBowAttackEndTime = 5 * TimeController.TICKS_PER_SECOND + TimeController.getGameTicks()
					} else {
						// Cancel the action because the bow can't be re-use at this moment
						ThreadPoolManager.getInstance().scheduleAi(NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000)

						sendPacket(ActionFailed.STATIC_PACKET)
						return
					}
				}
			}
			if (weaponItem.itemType == WeaponType.CROSSBOW || weaponItem.itemType == WeaponType.CROSSBOWK) {
				//Check for bolts
				if (this is Player) {
					// Checking if target has moved to peace zone - only for player-crossbow attacks at the moment
					// Other melee is checked in movement code and for offensive spells a check is done every time
					if (target!!.isInsidePeaceZone(this)) {
						getAI()!!.intention = CtrlIntention.AI_INTENTION_ACTIVE
						sendPacket(ActionFailed.STATIC_PACKET)
						return
					}

					// Equip bolts needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True
					if (!checkAndEquipBolts()) {
						// Cancel the action because the Player have no arrow
						getAI()!!.intention = CtrlIntention.AI_INTENTION_IDLE
						sendPacket(ActionFailed.STATIC_PACKET)
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_BOLTS))
						return
					}

					// Verify if the crossbow can be use
					if (disableCrossBowAttackEndTime <= TimeController.getGameTicks()) {
						// Set the period of crossbow no re-use
						disableCrossBowAttackEndTime = 5 * TimeController.TICKS_PER_SECOND + TimeController.getGameTicks()
					} else {
						// Cancel the action because the crossbow can't be re-use at this moment
						ThreadPoolManager.getInstance().scheduleAi(NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000)
						sendPacket(ActionFailed.STATIC_PACKET)
						return
					}
				} else if (this is Npc) {
					if (disableCrossBowAttackEndTime > TimeController.getGameTicks()) {
						return
					}
				}
			}
		}

		// Add the Player to knownObjects and knownPlayer of the target
		target!!.knownList.addKnownObject(this)

		// Reduce the current CP if TIREDNESS configuration is activated
		if (Config.ALT_GAME_TIREDNESS) {
			setCurrentCp(currentCp - 10)
		}

		if (this is Player) {

			// Recharge any active auto soulshot tasks for player (or player's summon if one exists).
			this.rechargeAutoSoulShot(true, false, false)
		} else if (this is Summon) {
			this.getOwner().rechargeAutoSoulShot(true, false, true)
		}

		// Verify if soulshots are charged.
		var ssCharge = Item.CHARGED_NONE
		if (weaponInst != null) {
			ssCharge = weaponInst.chargedSoulShot
		} else if (this is Summon && this !is PetInstance) {
			ssCharge = this.chargedSoulShot
		}

		if (this is Attackable) {
			if ((this as Npc).useSoulShot()) {
				ssCharge = Item.CHARGED_SOULSHOT
			}
		}

		// Get the Attack Speed of the Creature (delay (in milliseconds) before next attack)
		val timeAtk = calculateTimeBetweenAttacks(target, weaponItem)
		// the hit is calculated to happen halfway to the animation - might need further tuning e.g. in bow case
		val timeToHit = timeAtk / 2
		attackEndTime = TimeController.getGameTicks()
		attackEndTime += timeAtk / TimeController.MILLIS_IN_TICK
		attackEndTime -= 1

		var ssGrade = 0
		if (weaponItem != null) {
			ssGrade = weaponItem.itemGradePlain
		}

		// Create a Server->Client packet Attack
		val attack = Attack(this, target, ssCharge, ssGrade)

		// Set the Attacking Body part to CHEST
		setAttackingBodypart()
		// Make sure that char is facing selected target
		// also works: setHeading(Util.convertDegreeToClientHeading(Util.calculateAngleFrom(this, target)));
		heading = Util.calculateHeadingFrom(this, target)

		// Get the Attack Reuse Delay of the WeaponTemplate
		val reuse = calculateReuseTime(target, weaponItem)
		val hitted: Boolean
		// Select the type of attack to start
		if (weaponItem == null || isTransformed) {
			hitted = doAttackHitSimple(attack, target, timeToHit)
		} else if (calcStat(Stats.ATTACK_COUNT_MAX, 1.0, null, null) - 1 > 0 || weaponItem.itemType == WeaponType.POLE) {
			hitted = doAttackHitByPole(attack, target, timeToHit)
		} else if (weaponItem.itemType == WeaponType.BOW) {
			hitted = doAttackHitByBow(attack, target, timeAtk, reuse)
		} else if (weaponItem.itemType == WeaponType.CROSSBOW || weaponItem.itemType == WeaponType.CROSSBOWK) {
			hitted = doAttackHitByCrossBow(attack, target, timeAtk, reuse)
		} else if (isUsingDualWeapon) {
			hitted = doAttackHitByDual(attack, target, timeToHit)
		} else {
			hitted = doAttackHitSimple(attack, target, timeToHit)
		}

		// Flag the attacker if it's a Player outside a PvP area
		val player = actingPlayer

		if (player != null) {
			AttackStanceTaskManager.getInstance().addAttackStanceTask(player)

			var targetIsSummon = false
			for (summon in player.summons) {
				if (summon === target) {
					targetIsSummon = true
				}
			}
			if (!targetIsSummon && player.pet !== target) {
				player.updatePvPStatus(target)
			}
		}
		// Check if hit isn't missed
		if (!hitted)
		// Abort the attack of the Creature and send Server->Client ActionFailed packet
		{
			abortAttack()
		} else {
			/* ADDED BY nexus - 2006-08-17
			 *
			 * As soon as we know that our hit landed, we must discharge any active soulshots.
			 * This must be done so to avoid unwanted soulshot consumption.
			 */

			if (ArenaManager.getInstance().isInFight(player)) {
				val fight = ArenaManager.getInstance().getFight(player)
				val attacker = ArenaManager.getInstance().getFighter(player)
				if (fight == null || attacker == null) {
					return
				}
				val victim = ArenaManager.getInstance().getFighter(player)
				attacker.onHit(target as Player?)
			}

			// If we didn't miss the hit, discharge the shoulshots, if any
			if (this is Summon && !(this is PetInstance && weaponInst != null)) {
				this.chargedSoulShot = Item.CHARGED_NONE
			} else if (weaponInst != null) {
				weaponInst.chargedSoulShot = Item.CHARGED_NONE
			}

			if (player != null) {
				if (player.isCursedWeaponEquipped) {
					// If hitted by a cursed weapon, Cp is reduced to 0
					if (!target.isInvul(this)) {
						target.setCurrentCp(0.0)
					}
				} else if (player.isHero) {
					if (target is Player && target.isCursedWeaponEquipped)
					// If a cursed weapon is hitted by a Hero, Cp is reduced to 0
					{
						target.setCurrentCp(0.0)
					}
				}
			}
		}

		// If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack
		// to the Creature AND to all Player in the KnownPlayers of the Creature
		if (attack.hasHits()) {
			broadcastPacket(attack)
		}

		// Notify AI with EVT_READY_TO_ACT
		ThreadPoolManager.getInstance().scheduleAi(NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), (timeAtk + reuse).toLong())
	}

	/**
	 * Launch a Bow attack.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Calculate if hit is missed or not
	 *  * Consume arrows
	 *  * If hit isn't missed, calculate if shield defense is efficient
	 *  * If hit isn't missed, calculate if hit is critical
	 *  * If hit isn't missed, calculate physical damages
	 *  * If the Creature is a Player, Send a Server->Client packet SetupGauge
	 *  * Create a new hit task with Medium priority
	 *  * Calculate and set the disable delay of the bow in function of the Attack Speed
	 *  * Add this hit to the Server-Client packet Attack <BR></BR><BR></BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The Creature targeted
	 * @param sAtk   The Attack Speed of the attacker
	 * @return True if the hit isn't missed
	 */
	private fun doAttackHitByBow(attack: Attack, target: Creature?, sAtk: Int, reuse: Int): Boolean {
		var damage1 = 0
		var shld1: Byte = 0
		var crit1 = false

		// Calculate if hit is missed or not
		val miss1 = Formulas.calcHitMiss(this, target!!)

		// Consume arrows
		reduceArrowCount(false)

		move = null

		// Check if hit isn't missed
		if (!miss1) {
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target)

			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(stat!!.getCriticalHit(target, null).toDouble(), target)

			// Calculate physical damages
			damage1 = Formulas.calcPhysDam(this, target, shld1, crit1, false, attack.soulshotCharge).toInt()
		}

		// Check if the Creature is a Player
		if (this is Player) {
			// Send a system message
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GETTING_READY_TO_SHOOT_AN_ARROW))

			// Send a Server->Client packet SetupGauge
			val sg = SetupGauge(SetupGauge.BLUE, sAtk + reuse)
			sendPacket(sg)
		}

		// Create a new hit task with Medium priority
		ThreadPoolManager.getInstance().scheduleAi(HitTask(target, damage1, crit1, miss1, attack.soulshotCharge, shld1), sAtk.toLong())

		// Calculate and set the disable delay of the bow in function of the Attack Speed
		disableBowAttackEndTime = (sAtk + reuse) / TimeController.MILLIS_IN_TICK + TimeController.getGameTicks()

		// Add this hit to the Server-Client packet Attack
		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1))

		// Return true if hit isn't missed
		return !miss1
	}

	/**
	 * Launch a CrossBow attack.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Calculate if hit is missed or not
	 *  * Consume bolts
	 *  * If hit isn't missed, calculate if shield defense is efficient
	 *  * If hit isn't missed, calculate if hit is critical
	 *  * If hit isn't missed, calculate physical damages
	 *  * If the Creature is a Player, Send a Server->Client packet SetupGauge
	 *  * Create a new hit task with Medium priority
	 *  * Calculate and set the disable delay of the crossbow in function of the Attack Speed
	 *  * Add this hit to the Server-Client packet Attack <BR></BR><BR></BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The Creature targeted
	 * @param sAtk   The Attack Speed of the attacker
	 * @return True if the hit isn't missed
	 */
	private fun doAttackHitByCrossBow(attack: Attack, target: Creature?, sAtk: Int, reuse: Int): Boolean {
		var damage1 = 0
		var shld1: Byte = 0
		var crit1 = false

		// Calculate if hit is missed or not
		val miss1 = Formulas.calcHitMiss(this, target!!)

		// Consume bolts
		reduceArrowCount(true)

		move = null

		// Check if hit isn't missed
		if (!miss1) {
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target)

			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(stat!!.getCriticalHit(target, null).toDouble(), target)

			// Calculate physical damages
			damage1 = Formulas.calcPhysDam(this, target, shld1, crit1, false, attack.soulshotCharge).toInt()
		}

		// Check if the Creature is a Player
		if (this is Player) {
			// Send a system message
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CROSSBOW_PREPARING_TO_FIRE))

			// Send a Server->Client packet SetupGauge
			val sg = SetupGauge(SetupGauge.BLUE, sAtk + reuse)
			sendPacket(sg)
		}

		// Create a new hit task with Medium priority
		if (this is Attackable) {
			if (this.soulshotcharged) {
				// Create a new hit task with Medium priority
				ThreadPoolManager.getInstance().scheduleAi(HitTask(target, damage1, crit1, miss1, Item.CHARGED_SOULSHOT, shld1), sAtk.toLong())
			} else {
				ThreadPoolManager.getInstance().scheduleAi(HitTask(target, damage1, crit1, miss1, Item.CHARGED_NONE, shld1), sAtk.toLong())
			}
		} else {
			ThreadPoolManager.getInstance().scheduleAi(HitTask(target, damage1, crit1, miss1, attack.soulshotCharge, shld1), sAtk.toLong())
		}

		// Calculate and set the disable delay of the bow in function of the Attack Speed
		disableCrossBowAttackEndTime = (sAtk + reuse) / TimeController.MILLIS_IN_TICK + TimeController.getGameTicks()

		// Add this hit to the Server-Client packet Attack
		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1))

		// Return true if hit isn't missed
		return !miss1
	}

	/**
	 * Launch a Dual attack.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Calculate if hits are missed or not
	 *  * If hits aren't missed, calculate if shield defense is efficient
	 *  * If hits aren't missed, calculate if hit is critical
	 *  * If hits aren't missed, calculate physical damages
	 *  * Create 2 new hit tasks with Medium priority
	 *  * Add those hits to the Server-Client packet Attack <BR></BR><BR></BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The Creature targeted
	 * @return True if hit 1 or hit 2 isn't missed
	 */
	private fun doAttackHitByDual(attack: Attack, target: Creature?, sAtk: Int): Boolean {
		var damage1 = 0
		var damage2 = 0
		var shld1: Byte = 0
		var shld2: Byte = 0
		var crit1 = false
		var crit2 = false

		// Calculate if hits are missed or not
		val miss1 = Formulas.calcHitMiss(this, target!!)
		val miss2 = Formulas.calcHitMiss(this, target)

		// Check if hit 1 isn't missed
		if (!miss1) {
			// Calculate if shield defense is efficient against hit 1
			shld1 = Formulas.calcShldUse(this, target)

			// Calculate if hit 1 is critical
			crit1 = Formulas.calcCrit(stat!!.getCriticalHit(target, null).toDouble(), target)

			// Calculate physical damages of hit 1
			damage1 = Formulas.calcPhysDam(this, target, shld1, crit1, true, attack.soulshotCharge).toInt()
			damage1 /= 2
		}

		// Check if hit 2 isn't missed
		if (!miss2) {
			// Calculate if shield defense is efficient against hit 2
			shld2 = Formulas.calcShldUse(this, target)

			// Calculate if hit 2 is critical
			crit2 = Formulas.calcCrit(stat!!.getCriticalHit(target, null).toDouble(), target)

			// Calculate physical damages of hit 2
			damage2 = Formulas.calcPhysDam(this, target, shld2, crit2, true, attack.soulshotCharge).toInt()
			damage2 /= 2
		}

		if (this is Attackable) {
			if (this.soulshotcharged) {

				// Create a new hit task with Medium priority for hit 1
				ThreadPoolManager.getInstance()
						.scheduleAi(HitTask(target, damage1, crit1, miss1, Item.CHARGED_SOULSHOT, shld1), (sAtk / 2).toLong())

				// Create a new hit task with Medium priority for hit 2 with a higher delay
				ThreadPoolManager.getInstance().scheduleAi(HitTask(target, damage2, crit2, miss2, Item.CHARGED_SOULSHOT, shld2), sAtk.toLong())
			} else {
				ThreadPoolManager.getInstance().scheduleAi(HitTask(target, damage1, crit1, miss1, Item.CHARGED_NONE, shld1), (sAtk / 2).toLong())

				// Create a new hit task with Medium priority for hit 2 with a higher delay
				ThreadPoolManager.getInstance().scheduleAi(HitTask(target, damage2, crit2, miss2, Item.CHARGED_NONE, shld2), sAtk.toLong())
			}
		} else {
			// Create a new hit task with Medium priority for hit 1
			ThreadPoolManager.getInstance().scheduleAi(HitTask(target, damage1, crit1, miss1, attack.soulshotCharge, shld1), (sAtk / 2).toLong())

			// Create a new hit task with Medium priority for hit 2 with a higher delay
			ThreadPoolManager.getInstance().scheduleAi(HitTask(target, damage2, crit2, miss2, attack.soulshotCharge, shld2), sAtk.toLong())
		}

		// Add those hits to the Server-Client packet Attack
		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1), attack.createHit(target, damage2, miss2, crit2, shld2))

		// Return true if hit 1 or hit 2 isn't missed
		return !miss1 || !miss2
	}

	/**
	 * Launch a Pole attack.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Get all visible objects in a spherical area near the Creature to obtain possible targets
	 *  * If possible target is the Creature targeted, launch a simple attack against it
	 *  * If possible target isn't the Creature targeted but is attackable, launch a simple attack against it <BR></BR><BR></BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @return True if one hit isn't missed
	 */
	private fun doAttackHitByPole(attack: Attack, target: Creature?, sAtk: Int): Boolean {
		//double angleChar;
		val maxRadius = physicalAttackRange
		val maxAngleDiff = stat!!.calcStat(Stats.POWER_ATTACK_ANGLE, 120.0, null, null).toInt()

		if (Config.DEBUG) {
			log.info("doAttackHitByPole: Max radius = $maxRadius")
			log.info("doAttackHitByPole: Max angle = $maxAngleDiff")
		}

		// o1 x: 83420 y: 148158 (Giran)
		// o2 x: 83379 y: 148081 (Giran)
		// dx = -41
		// dy = -77
		// distance between o1 and o2 = 87.24
		// arctan2 = -120 (240) degree (excel arctan2(dx, dy); java arctan2(dy, dx))
		//
		// o2
		//
		//		  o1 ----- (heading)
		// In the diagram above:
		// o1 has a heading of 0/360 degree from horizontal (facing East)
		// Degree of o2 in respect to o1 = -120 (240) degree
		//
		// o2		  / (heading)
		//			/
		//		  o1
		// In the diagram above
		// o1 has a heading of -80 (280) degree from horizontal (facing north east)
		// Degree of o2 in respect to 01 = -40 (320) degree

		// Get char's heading degree
		// angleChar = Util.convertHeadingToDegree(getHeading());
		// In H5 ATTACK_COUNT_MAX 1 is by default and 2 was in skill 3599, total 3.
		val attackRandomCountMax = stat!!.calcStat(Stats.ATTACK_COUNT_MAX, 1.0, null, null).toInt() - 1
		var attackcount = 0

		/*if (angleChar <= 0)
			angleChar += 360;*/

		var hitted = doAttackHitSimple(attack, target, 100.0, sAtk)
		var percentLostPerTarget = 15
		if (Config.isServer(Config.TENKAI_LEGACY) && target is MonsterInstance) {
			percentLostPerTarget = 50
		}
		var attackpercent = (100 - percentLostPerTarget).toDouble()
		var temp: Creature
		val objs = knownList.knownObjects.values
		//synchronized (getKnownList().getKnownObjects())
		run {
			for (obj in objs) {
				if (obj === target) {
					continue // do not hit twice
				}
				// Check if the WorldObject is a Creature
				if (obj is Creature) {
					if (obj is PetInstance && this is Player && obj.getOwner() === this) {
						continue
					}

					if (!Util.checkIfInRange(maxRadius, this, obj, false)) {
						continue
					}

					// otherwise hit too high/low. 650 because mob z coord
					// sometimes wrong on hills
					if (Math.abs(obj.z - z) > 650) {
						continue
					}
					if (!isFacing(obj, maxAngleDiff)) {
						continue
					}

					if (this is Attackable && obj is Player && target is Attackable) {
						continue
					}

					if (this is Attackable && obj is Attackable && this.enemyClan == null &&
							this.isChaos == 0) {
						continue
					}

					if (this is Attackable && obj is Attackable &&
							this.enemyClan != obj.clan && this.isChaos == 0) {
						continue
					}

					if (this is Playable && obj is Playable) {
						val activeChar = actingPlayer
						val targetedPlayer = obj.actingPlayer

						if (targetedPlayer!!.pvpFlag.toInt() == 0) {
							if (activeChar!!.hasAwakaned()) {
								if (!targetedPlayer.hasAwakaned()) {
									continue
								}
							} else if (targetedPlayer.hasAwakaned()) {
								continue
							}

							if (targetedPlayer.level + 9 <= activeChar.level) {
								continue
							} else if (activeChar.level + 9 <= targetedPlayer.level) {
								continue
							}
						}
					}

					temp = obj

					// Launch a simple attack against the Creature targeted
					if (!temp.isAlikeDead) {
						if (temp === getAI()!!.attackTarget || temp.isAutoAttackable(this)) {
							hitted = hitted or doAttackHitSimple(attack, temp, attackpercent, sAtk)
							attackpercent *= 1 - percentLostPerTarget / 100.0

							attackcount++
							if (attackcount > attackRandomCountMax) {
								break
							}
						}
					}
				}
			}
		}

		// Return true if one hit isn't missed
		return hitted
	}

	/**
	 * Launch a simple attack.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Calculate if hit is missed or not
	 *  * If hit isn't missed, calculate if shield defense is efficient
	 *  * If hit isn't missed, calculate if hit is critical
	 *  * If hit isn't missed, calculate physical damages
	 *  * Create a new hit task with Medium priority
	 *  * Add this hit to the Server-Client packet Attack <BR></BR><BR></BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The Creature targeted
	 * @return True if the hit isn't missed
	 */
	private fun doAttackHitSimple(attack: Attack, target: Creature?, sAtk: Int): Boolean {
		return doAttackHitSimple(attack, target, 100.0, sAtk)
	}

	private fun doAttackHitSimple(attack: Attack, target: Creature?, attackpercent: Double, sAtk: Int): Boolean {
		var damage1 = 0
		var shld1: Byte = 0
		var crit1 = false

		// Calculate if hit is missed or not
		val miss1 = Formulas.calcHitMiss(this, target!!)

		// Check if hit isn't missed
		if (!miss1) {
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target)

			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(stat!!.getCriticalHit(target, null).toDouble(), target)

			// Calculate physical damages
			damage1 = Formulas.calcPhysDam(this, target, shld1, crit1, false, attack.soulshotCharge).toInt()

			if (attackpercent != 100.0) {
				damage1 = (damage1 * attackpercent / 100).toInt()
			}
		}

		// Create a new hit task with Medium priority
		if (this is Attackable) {
			if (this.soulshotcharged) {
				ThreadPoolManager.getInstance().scheduleAi(HitTask(target, damage1, crit1, miss1, Item.CHARGED_SOULSHOT, shld1), sAtk.toLong())
			} else {
				ThreadPoolManager.getInstance().scheduleAi(HitTask(target, damage1, crit1, miss1, Item.CHARGED_NONE, shld1), sAtk.toLong())
			}
		} else {
			ThreadPoolManager.getInstance().scheduleAi(HitTask(target, damage1, crit1, miss1, attack.soulshotCharge, shld1), sAtk.toLong())
		}

		// Add this hit to the Server-Client packet Attack
		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1))

		// Return true if hit isn't missed
		return !miss1
	}

	/**
	 * Manage the casting task (casting and interrupt time, re-use delay...) and display the casting bar and animation on client.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Verify the possibilty of the the cast : skill is a spell, caster isn't muted...
	 *  * Get the list of all targets (ex : area effects) and define the L2Charcater targeted (its stats will be used in calculation)
	 *  * Calculate the casting time (base + modifier of MAtkSpd), interrupt time and re-use delay
	 *  * Send a Server->Client packet MagicSkillUse (to diplay casting animation), a packet SetupGauge (to display casting bar) and a system message
	 *  * Disable all skills during the casting time (create a task EnableAllSkills)
	 *  * Disable the skill during the re-use delay (create a task EnableSkill)
	 *  * Create a task MagicUseTask (that will call method onMagicUseTimer) to launch the Magic Skill at the end of the casting time<BR></BR><BR></BR>
	 *
	 * @param skill The Skill to use
	 */
	fun doCast(skill: Skill, second: Boolean) {
		beginCast(skill, false, second)
	}

	open fun doCast(skill: Skill) {
		beginCast(skill, false, false)
	}

	fun doSimultaneousCast(skill: Skill) {
		beginCast(skill, true, false)
	}

	fun doCast(skill: Skill, target: Creature, targets: Array<WorldObject>) {
		if (!checkDoCastConditions(skill)) {
			isCastingNow = false
			return
		}

		//if (this instanceof Player && !((Player) this).onCast(target, skill))
		//	return;

		// Override casting type
		if (skill.isSimultaneousCast) {
			doSimultaneousCast(skill, target, targets)
			return
		}

		// Recharge AutoSoulShot
		// this method should not used with Playable

		beginCast(skill, false, false, target, targets)
	}

	fun doSimultaneousCast(skill: Skill, target: Creature, targets: Array<WorldObject>) {
		if (!checkDoCastConditions(skill))
		// || this instanceof Player)
		{
			isCastingSimultaneouslyNow = false
			return
		}

		// Recharge AutoSoulShot
		// this method should not used with Playable

		beginCast(skill, true, false, target, targets)
	}

	private fun beginCast(skill: Skill, simultaneously: Boolean, second: Boolean) {
		var simultaneously = simultaneously
		if (!checkDoCastConditions(skill)) {
			if (simultaneously) {
				isCastingSimultaneouslyNow = false
			} else if (second) {
				setCastingNow2(false)
			} else {
				isCastingNow = false
			}
			if (this is Player) {
				getAI()!!.intention = AI_INTENTION_ACTIVE
			}
			return
		}
		// Override casting type
		if (skill.isSimultaneousCast && !simultaneously) {
			simultaneously = true
		}

		//Recharge AutoSoulShot
		if (skill.useSoulShot()) {
			if (this is Player) {
				this.rechargeAutoSoulShot(true, false, false)
			} else if (this is Summon) {
				this.getOwner().rechargeAutoSoulShot(true, false, true)
			}
		} else if (skill.useSpiritShot()) {
			if (this is Player) {
				this.rechargeAutoSoulShot(false, true, false)
			} else if (this is Summon) {
				this.getOwner().rechargeAutoSoulShot(false, true, true)
			}
		}

		// Set the target of the skill in function of Skill Type and Target Type
		var target: Creature? = null
		// Get all possible targets of the skill in a table in function of the skill target type
		val targets = skill.getTargetList(this)

		// AURA skills should always be using caster as target
		if (skill.targetType === SkillTargetType.TARGET_AURA || skill.targetType === SkillTargetType.TARGET_FRONT_AURA ||
				skill.targetType === SkillTargetType.TARGET_BEHIND_AURA || skill.targetType === SkillTargetType.TARGET_GROUND ||
				skill.isUseableWithoutTarget) {
			target = this
		} else {
			if ((targets == null || targets.size == 0) && !skill.isUseableWithoutTarget) {
				if (simultaneously) {
					isCastingSimultaneouslyNow = false
				} else {
					isCastingNow = false
				}

				return
			}

			when (skill.skillType) {
				SkillType.BUFF, SkillType.HEAL, SkillType.COMBATPOINTHEAL, SkillType.MANAHEAL, SkillType.HPMPHEAL_PERCENT, SkillType.HPCPHEAL_PERCENT, SkillType.HPMPCPHEAL_PERCENT -> {
					target = targets!![0] as Creature
				}

				else -> {
					when (skill.targetType) {
						SkillTargetType.TARGET_SELF, SkillTargetType.TARGET_PET, SkillTargetType.TARGET_MY_SUMMON, SkillTargetType.TARGET_SUMMON, SkillTargetType.TARGET_PARTY, SkillTargetType.TARGET_CLAN, SkillTargetType.TARGET_CLANPARTY, SkillTargetType.TARGET_ALLY, SkillTargetType.TARGET_FRIEND_NOTME -> {
							target = targets!![0] as Creature
						}

						else -> {
							target = this.target as Creature
						}
					}
				}
			}
		}

		beginCast(skill, simultaneously, second, target, targets)
	}

	private fun beginCast(skill: Skill, simultaneously: Boolean, second: Boolean, target: Creature?, targets: Array<WorldObject>?) {
		stopEffectsOnAction(skill)

		if (target == null) {
			if (simultaneously) {
				isCastingSimultaneouslyNow = false
			} else if (second) {
				setCastingNow2(false)
			} else {
				isCastingNow = false
			}
			if (this is Player) {
				sendPacket(ActionFailed.STATIC_PACKET)
				getAI()!!.intention = AI_INTENTION_ACTIVE
			}
			return
		}

		//TODO LasTravel, TEMP fix, IDK why L2Monsters don't check skill conditions
		if (this is MonsterInstance) {
			if (!skill.checkCondition(this, target, false)) {
				return
			}
		}

		if (skill.skillType === SkillType.RESURRECT) {
			if (isResurrectionBlocked || target.isResurrectionBlocked) {
				sendPacket(SystemMessage.getSystemMessage(356)) // Reject resurrection
				target.sendPacket(SystemMessage.getSystemMessage(356)) // Reject resurrection
				if (simultaneously) {
					isCastingSimultaneouslyNow = false
				} else if (second) {
					setCastingNow2(false)
				} else {
					isCastingNow = false
				}

				if (this is Player) {
					getAI()!!.intention = AI_INTENTION_ACTIVE
					sendPacket(ActionFailed.STATIC_PACKET)
				}
				return
			}
		}

		// Get the Identifier of the skill
		val magicId = skill.id

		// Get the Display Identifier for a skill that client can't display
		val displayId = skill.displayId

		// Get the level of the skill
		var level = skill.levelHash

		if (level < 1) {
			level = 1
		}

		// Get the casting time of the skill (base)
		var hitTime = skill.hitTime
		var coolTime = skill.coolTime

		val effectWhileCasting = skill.skillType === SkillType.CONTINUOUS_DEBUFF || skill.skillType === SkillType.CONTINUOUS_DRAIN ||
				skill.skillType === SkillType.CONTINUOUS_CASTS || skill.skillType === SkillType.FUSION ||
				skill.skillType === SkillType.SIGNET_CASTTIME

		// Calculate the casting time of the skill (base + modifier of MAtkSpd)
		// Don't modify the skill time for FORCE_BUFF skills. The skill time for those skills represent the buff time.
		if (!effectWhileCasting) {
			hitTime = Formulas.calcAtkSpd(this, skill, hitTime.toDouble())
			if (coolTime > 0) {
				coolTime = Formulas.calcAtkSpd(this, skill, coolTime.toDouble())
			}
		}

		var shotSave = Item.CHARGED_NONE

		// Calculate altered Cast Speed due to BSpS/SpS
		val weaponInst = activeWeaponInstance
		if (weaponInst != null) {
			if (skill.isMagic && !effectWhileCasting && !skill.isPotion) {
				if (weaponInst.chargedSpiritShot > Item.CHARGED_NONE) {
					//Only takes 70% of the time to cast a BSpS/SpS cast
					hitTime = (0.70 * hitTime).toInt()
					coolTime = (0.70 * coolTime).toInt()

					//Because the following are magic skills that do not actively 'eat' BSpS/SpS,
					//I must 'eat' them here so players don't take advantage of infinite speed increase
					when (skill.skillType) {
						SkillType.BUFF, SkillType.MANAHEAL, SkillType.MANARECHARGE, SkillType.MANA_BY_LEVEL, SkillType.RESURRECT, SkillType.RECALL, SkillType.DRAIN -> weaponInst.chargedSpiritShot = Item.CHARGED_NONE
					}
				}
			}

			// Save shots value for repeats
			if (skill.useSoulShot()) {
				shotSave = weaponInst.chargedSoulShot
			} else if (skill.useSpiritShot()) {
				shotSave = weaponInst.chargedSpiritShot
			}
		}

		if (this is Npc) {
			if (this.useSpiritShot()) {
				hitTime = (0.70 * hitTime).toInt()
				coolTime = (0.70 * coolTime).toInt()
			}
		}

		// Don't modify skills HitTime if staticHitTime is specified for skill in datapack.
		if (skill.isStaticHitTime) {
			hitTime = skill.hitTime
			coolTime = skill.coolTime
		} else if (skill.hitTime >= 500 && hitTime < 500) {
			hitTime = 500
		}// if basic hitTime is higher than 500 than the min hitTime is 500

		// queue herbs and potions
		if (isCastingSimultaneouslyNow && simultaneously) {
			ThreadPoolManager.getInstance().scheduleAi(UsePotionTask(this, skill), 100)
			return
		}

		// Set the castInterruptTime and casting status (Player already has this true)
		if (simultaneously) {
			isCastingSimultaneouslyNow = true
		} else if (second) {
			setCastingNow2(true)
		} else {
			isCastingNow = true
		}

		//setLastCast1(!second);

		// Note: castEndTime = GameTimeController.getGameTicks() + (coolTime + hitTime) / GameTimeController.MILLIS_IN_TICK;
		if (!simultaneously) {
			castInterruptTime = -2 + TimeController.getGameTicks() + hitTime / TimeController.MILLIS_IN_TICK
			lastSkillCast = skill
		} else {
			lastSimultaneousSkillCast = skill
		}

		// Init the reuse time of the skill
		var reuseDelay: Int
		if (skill.isStaticReuse) {
			reuseDelay = skill.reuseDelay
		} else {
			if (skill.isMagic) {
				reuseDelay = (skill.reuseDelay * stat!!.getMReuseRate(skill)).toInt()
			} else {
				reuseDelay = (skill.reuseDelay * stat!!.getPReuseRate(skill)).toInt()
			}
		}

		var skillMastery = Formulas.calcSkillMastery(this, skill)

		//Buffs and debuffs and get reset duration
		if (skillMastery && (skill.skillType === SkillType.BUFF || skill.skillType === SkillType.DEBUFF) || skill.isPotion ||
				skill.isToggle) {
			skillMastery = false
		}
		if (skill.hasEffects() && skillMastery) {
			for (ab in skill.effectTemplates) {
				if (!skillMastery) {
					break
				}

				if (ab.effectType === AbnormalType.DEBUFF || ab.effectType === AbnormalType.BUFF) {
					skillMastery = false
				}
			}
		}

		// Skill reuse check
		if (reuseDelay > 30000 && !skillMastery) {
			addTimeStamp(skill, reuseDelay.toLong())
		}

		// Check if this skill consume mp on start casting
		val initmpcons = stat!!.getMpInitialConsume(skill)
		if (initmpcons > 0) {
			status!!.reduceMp(initmpcons.toDouble())
			val su = StatusUpdate(this)
			su.addAttribute(StatusUpdate.CUR_MP, currentMp.toInt())
			sendPacket(su)
		}

		// Disable the skill during the re-use delay and create a task EnableSkill with Medium priority to enable it at the end of the re-use delay
		if (reuseDelay > 10) {
			if (skillMastery || isGM) {
				reuseDelay = 100

				if (actingPlayer != null) {
					val sm = SystemMessage.getSystemMessage(SystemMessageId.SKILL_READY_TO_USE_AGAIN)
					actingPlayer!!.sendPacket(sm)
				}
			}

			disableSkill(skill, reuseDelay.toLong())
		}

		// Make sure that char is facing selected target
		if (target !== this) {
			heading = Util.calculateHeadingFrom(this, target)
		}

		// For force buff skills, start the effect as long as the player is casting.
		if (effectWhileCasting) {
			// Consume Items if necessary and Send the Server->Client packet InventoryUpdate with Item modification to all the Creature
			if (skill.itemConsumeId > 0 && skill.itemConsume > 0) {
				if (!destroyItemByItemId("Consume", skill.itemConsumeId, skill.itemConsume.toLong(), null, true)) {
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS))
					if (simultaneously) {
						isCastingSimultaneouslyNow = false
					} else if (second) {
						setCastingNow2(false)
					} else {
						isCastingNow = false
					}

					if (this is Player) {
						getAI()!!.intention = AI_INTENTION_ACTIVE
					}
					return
				}
			}

			// Consume Souls if necessary
			if (skill.soulConsumeCount > 0 || skill.maxSoulConsumeCount > 0) {
				if (this is Player) {
					if (!this.decreaseSouls(skill.soulConsumeCount, skill)) {
						if (simultaneously) {
							isCastingSimultaneouslyNow = false
						} else if (second) {
							setCastingNow2(false)
						} else {
							isCastingNow = false
						}
						return
					}
				}
			}

			if (this is Player) {
				val player = this
				if (skill.fameConsume > 0) {
					if (player.fame >= skill.fameConsume) {
						player.fame = player.fame - skill.fameConsume
						player.sendPacket(UserInfo(player))
					} else {
						if (simultaneously) {
							isCastingSimultaneouslyNow = false
						} else if (second) {
							setCastingNow2(false)
						} else {
							isCastingNow = false
						}
						return
					}
				}
				if (skill.clanRepConsume > 0) {
					if (player.clan != null && player.clan.reputationScore >= skill.clanRepConsume) {
						player.clan.takeReputationScore(skill.clanRepConsume, false)
						val smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP)
						smsg.addItemNumber(skill.clanRepConsume.toLong())
						player.sendPacket(smsg)
					} else {
						if (simultaneously) {
							isCastingSimultaneouslyNow = false
						} else if (second) {
							setCastingNow2(false)
						} else {
							isCastingNow = false
						}
						return
					}
				}
			}

			if (skill.skillType === SkillType.CONTINUOUS_DEBUFF) {
				continuousDebuffTargets = targets
			}

			if (skill.skillType === SkillType.FUSION) {
				startFusionSkill(target, skill)
			} else {
				callSkill(skill, targets)
			}
		}

		// Send a Server->Client packet MagicSkillUser with target, displayId, level, skillTime, reuseDelay
		// to the Creature AND to all Player in the KnownPlayers of the Creature
		if (!skill.isToggle)
		// Toggles should not display animations upon cast
		{
			broadcastPacket(MagicSkillUse(this,
					target,
					displayId,
					level,
					hitTime,
					reuseDelay,
					skill.reuseHashCode,
					if (second) 1 else 0,
					skill.targetType === SkillTargetType.TARGET_GROUND || skill.targetType === SkillTargetType.TARGET_GROUND_AREA,
					skill.actionId))
		}

		// Send a system message USE_S1 to the Creature
		if (this is Player && magicId != 1312) {
			val sm = SystemMessage.getSystemMessage(SystemMessageId.USE_S1)
			sm.addSkillName(skill)
			sendPacket(sm)
		}

		if (this is Playable) {
			if (!effectWhileCasting) {
				val player = actingPlayer
				if (skill.itemConsumeId > 0 && skill.itemConsume != 0) {
					if (!destroyItemByItemId("Consume", skill.itemConsumeId, skill.itemConsume.toLong(), null, true)) {
						player!!.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS))
						abortCast()
						return
					}
				}

				if (skill.fameConsume > 0) {
					if (player!!.fame >= skill.fameConsume) {
						player.fame = player.fame - skill.fameConsume
						player.sendPacket(UserInfo(player))
					} else {
						actingPlayer!!.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_FAME_POINTS))
						abortCast()
						return
					}
				}
				if (skill.clanRepConsume > 0) {
					if (player!!.clan != null && player.clan.reputationScore >= skill.clanRepConsume) {
						player.clan.takeReputationScore(skill.clanRepConsume, false)
						val smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP)
						smsg.addItemNumber(skill.clanRepConsume.toLong())
						player.sendPacket(smsg)
					} else {
						actingPlayer!!.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW))
						abortCast()
						return
					}
				}
			}

			//reduce talisman mana on skill use
			if (skill.referenceItemId > 0 && ItemTable.getInstance().getTemplate(skill.referenceItemId)!!.bodyPart == ItemTemplate.SLOT_DECO) {
				for (item in inventory!!.getItemsByItemId(skill.referenceItemId)) {
					if (item.isEquipped) {
						item.decreaseMana(false, skill.reuseDelay / 60000)
						break
					}
				}
			}
		}

		// Before start AI Cast Broadcast Fly Effect is Need
		if (skill.flyType != null/* && (this instanceof Player)*/) {
			val flyType = FlyType.valueOf(skill.flyType)
			val radius = skill.flyRadius
			var x: Int
			var y: Int
			var z: Int

			if (radius != 0) {
				val angle = Util.convertHeadingToDegree(heading)
				val radian = Math.toRadians(angle)
				val course = Math.toRadians(skill.flyCourse.toDouble())

				val x1 = Math.cos(Math.PI + radian + course).toFloat()
				val y1 = Math.sin(Math.PI + radian + course).toFloat()

				if (skill.targetType === SkillTargetType.TARGET_SINGLE) {
					x = target.x + (x1 * radius).toInt()
					y = target.y + (y1 * radius).toInt()
					z = target.z
				} else {
					x = this.x + (x1 * radius).toInt()
					y = this.y + (y1 * radius).toInt()
					z = this.z + 100
				}

				if (Config.GEODATA > 0) {
					val destiny = GeoData.getInstance().moveCheck(x, y, z, x, y, z, instanceId)
					if (destiny.x != x || destiny.y != y) {
						x = destiny.x - (x1 * 10).toInt()
						y = destiny.y - (y1 * 10).toInt()
						z = destiny.z
					}
				}

				broadcastPacket(FlyToLocation(this, x, y, z, flyType))
			} else {
				x = target.x
				y = target.y
				z = target.z
				broadcastPacket(FlyToLocation(this, target, flyType))
			}

			getAI()!!.intention = CtrlIntention.AI_INTENTION_IDLE
			abortAttack()

			setXYZ(x, y, z)
			ThreadPoolManager.getInstance().scheduleEffect(FlyToLocationTask(this, x, y, z), 400)
		}

		val mut = MagicUseTask(targets, skill, hitTime, coolTime, simultaneously, shotSave, second)

		// launch the magic in hitTime milliseconds
		if (hitTime > 410) {
			// Send a Server->Client packet SetupGauge with the color of the gauge and the casting time
			if (this is Player && !effectWhileCasting) {
				sendPacket(SetupGauge(SetupGauge.BLUE, hitTime))
			}

			if (skill.hitCounts > 0) {
				hitTime = hitTime * skill.hitTimings[0] / 100

				if (hitTime < 410) {
					hitTime = 410
				}
			}

			if (effectWhileCasting) {
				mut.phase = 2
			}

			if (simultaneously) {
				val future = simultSkillCast
				if (future != null) {
					future.cancel(true)
					simultSkillCast = null
				}

				// Create a task MagicUseTask to launch the MagicSkill at the end of the casting time (hitTime)
				// For client animation reasons (party buffs especially) 400 ms before!
				simultSkillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, (hitTime - 400).toLong())
			} else if (second) {
				val future = skillCast2
				if (future != null) {
					future.cancel(true)
					skillCast2 = null
				}

				// Create a task MagicUseTask to launch the MagicSkill at the end of the casting time (hitTime)
				// For client animation reasons (party buffs especially) 400 ms before!
				skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(mut, (hitTime - 400).toLong())
			} else {
				val future = skillCast
				if (future != null) {
					future.cancel(true)
					skillCast = null
				}

				// Create a task MagicUseTask to launch the MagicSkill at the end of the casting time (hitTime)
				// For client animation reasons (party buffs especially) 400 ms before!
				skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, (hitTime - 400).toLong())
			}
		} else {
			mut.hitTime = 0
			onMagicLaunchedTimer(mut)
		}
	}

	/**
	 * Check if casting of skill is possible
	 *
	 * @return True if casting is possible
	 */
	protected open fun checkDoCastConditions(skill: Skill?): Boolean {
		if (skill == null || isSkillDisabled(skill) || (skill.flyRadius > 0 || skill.flyType != null) && isMovementDisabled) {
			// Send a Server->Client packet ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET)
			return false
		}

		var canCastWhileStun = false
		when (skill.id) {
			30008 // Wind Blend
				, 19227 // Wind Blend Trigger
				, 30009 // Deceptive Blink
			-> {
				canCastWhileStun = true
			}
			else -> {
			}
		}

		if (isSkillDisabled(skill) || (skill.flyRadius > 0 || skill.flyType != null) && isMovementDisabled && !canCastWhileStun) {
			// Send a Server->Client packet ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET)
			return false
		}

		// Check if the caster has enough MP
		if (currentMp < stat!!.getMpConsume(skill) + stat!!.getMpInitialConsume(skill)) {
			// Send a System Message to the caster
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_MP))

			// Send a Server->Client packet ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET)
			return false
		}

		// Check if the caster has enough HP
		if (currentHp <= skill.hpConsume) {
			// Send a System Message to the caster
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_HP))

			// Send a Server->Client packet ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET)
			return false
		}

		// Disallow players to use hp consuming skills while waiting for an oly match to start
		if (skill.hpConsume > 0 && this is Player && this.isInOlympiadMode &&
				!this.isOlympiadStart) {
			// Send a System Message to the caster
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET))

			// Send a Server->Client packet ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET)
			return false
		}

		if (!skill.isPotion && !skill.canBeUsedWhenDisabled() && !canCastWhileStun) {
			// Check if the skill is a magic spell and if the Creature is not muted
			if (skill.isMagic) {
				if (isMuted) {
					// Send a Server->Client packet ActionFailed to the Player
					sendPacket(ActionFailed.STATIC_PACKET)
					return false
				}
			} else {
				// Check if the skill is physical and if the Creature is not physical_muted
				if (isPhysicalMuted) {
					// Send a Server->Client packet ActionFailed to the Player
					sendPacket(ActionFailed.STATIC_PACKET)
					return false
				} else if (isPhysicalAttackMuted)
				// Prevent use attack
				{
					// Send a Server->Client packet ActionFailed to the Player
					sendPacket(ActionFailed.STATIC_PACKET)
					return false
				}
			}
		}

		// prevent casting signets to peace zone
		if (skill.skillType === SkillType.SIGNET || skill.skillType === SkillType.SIGNET_CASTTIME) {
			val region = worldRegion ?: return false
			var canCast = true
			if (skill.targetType === SkillTargetType.TARGET_GROUND && this is Player) {
				val wp = skillCastPosition!!
				if (!region.checkEffectRangeInsidePeaceZone(skill, wp.x, wp.y, wp.z)) {
					canCast = false
				}
			} else if (!region.checkEffectRangeInsidePeaceZone(skill, x, y, z)) {
				canCast = false
			}
			if (!canCast) {
				val sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED)
				sm.addSkillName(skill)
				sendPacket(sm)
				return false
			}
		}

		// Check if the caster owns the weapon needed
		if (!skill.getWeaponDependancy(this)) {
			// Send a Server->Client packet ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET)
			return false
		}

		// Check if the spell consumes an Item
		// TODO: combine check and consume
		if (skill.itemConsumeId > 0 && skill.itemConsume != 0 && inventory != null) {
			// Get the Item consumed by the spell
			val requiredItems = inventory!!.getItemByItemId(skill.itemConsumeId)

			// Check if the caster owns enough consumed Item to cast
			if (requiredItems == null || requiredItems.count < skill.itemConsume) {
				// Checked: when a summon skill failed, server show required consume item count
				if (skill.skillType === SkillType.SUMMON) {
					val sm = SystemMessage.getSystemMessage(SystemMessageId.SUMMONING_SERVITOR_COSTS_S2_S1)
					sm.addItemName(skill.itemConsumeId)
					sm.addNumber(skill.itemConsume.toLong())
					sendPacket(sm)
					return false
				} else {
					// Send a System Message to the caster
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL))
					return false
				}
			}
		}

		if (actingPlayer != null) {
			val player = actingPlayer
			if (skill.fameConsume > 0) {
				if (player!!.fame < skill.fameConsume) {
					// Send a System Message to the caster
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_FAME_POINTS))
					return false
				}
			}
			if (skill.clanRepConsume > 0) {
				if (player!!.clan == null || player.clan.reputationScore < skill.clanRepConsume) {
					// Send a System Message to the caster
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW))
					return false
				}
			}
		}

		return true
	}

	/**
	 * Index according to skill id the current timestamp of use.<br></br><br></br>
	 *
	 * @param skill id
	 * @param reuse delay
	 * <BR></BR><B>Overridden in :</B>  (Player)
	 */
	open fun addTimeStamp(skill: Skill, reuse: Long) {
		/**/
	}

	fun startFusionSkill(target: Creature?, skill: Skill) {
		if (skill.skillType !== SkillType.FUSION) {
			return
		}

		if (fusionSkill == null) {
			fusionSkill = FusionSkill(this, target!!, skill)
		}
	}

	/**
	 * Kill the Creature.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Set target to null and cancel Attack or Cast
	 *  * Stop movement
	 *  * Stop HP/MP/CP Regeneration task
	 *  * Stop all active skills effects in progress on the Creature
	 *  * Send the Server->Client packet StatusUpdate with current HP and MP to all other Player to inform
	 *  * Notify Creature AI <BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Overridden in </U> :</B><BR></BR><BR></BR>
	 *  *  NpcInstance : Create a DecayTask to remove the corpse of the NpcInstance after 7 seconds
	 *  *  Attackable : Distribute rewards (EXP, SP, Drops...) and notify Quest Engine
	 *  *  Player : Apply Death Penalty, Manage gain/loss Karma and Item Drop <BR></BR><BR></BR>
	 *
	 * @param killer The Creature who killed it
	 */
	open fun doDie(killer: Creature): Boolean {
		// killing is only possible one time
		synchronized(this) {
			if (isDead()) {
				return false
			}
			// now reset currentHp to zero
			currentHp = 0.0
			setIsDead(true)
		}

		// Set target to null and cancel Attack or Cast
		target = null

		// Stop movement
		stopMove(null)

		// Stop HP/MP/CP Regeneration task
		status!!.stopHpMpRegeneration()

		// Stop all active skills effects in progress on the Creature,
		// if the Character isn't affected by Soul of The Phoenix or Salvation
		if (this is Playable && this.isPhoenixBlessed) {
			if (this.charmOfLuck)
			//remove Lucky Charm if player has SoulOfThePhoenix/Salvation buff
			{
				this.stopCharmOfLuck(null)
			}
			if (this.isNoblesseBlessed) {
				this.stopNoblesseBlessing(null)
			}
		} else if (this is Playable && this.isNoblesseBlessed) {
			this.stopNoblesseBlessing(null)

			if (this.charmOfLuck)
			//remove Lucky Charm if player have Nobless blessing buff
			{
				this.stopCharmOfLuck(null)
			}
		} else {
			stopAllEffectsExceptThoseThatLastThroughDeath()
		}// Same thing if the Character isn't a Noblesse Blessed L2PlayableInstance

		if (this is Player && this.agathionId != 0) {
			this.agathionId = 0
		}
		calculateRewards(killer)

		// Send the Server->Client packet StatusUpdate with current HP and MP to all other Player to inform
		broadcastStatusUpdate()

		// Notify Creature AI
		if (hasAI()) {
			getAI()!!.notifyEvent(CtrlEvent.EVT_DEAD)
		}

		if (worldRegion != null) {
			worldRegion!!.onDeath(this, killer)
		}

		getAttackByList()!!.clear()
		// If character is PhoenixBlessed
		// or has charm of courage inside siege battlefield (exact operation to be confirmed)
		// a resurrection popup will show up
		if (this is Summon) {
			if (this.isPhoenixBlessed && this.getOwner() != null) {
				this.getOwner().reviveRequest(this.getOwner(), null, true)
			}
		}
		if (this is Player) {
			if ((this as Playable).isPhoenixBlessed) {
				this.reviveRequest(this, null, false)
			} else if (isAffected(EffectType.CHARMOFCOURAGE.mask) && this.isInSiege) {
				this.reviveRequest(this, null, false)
			}
		}
		try {
			if (fusionSkill != null || continuousDebuffTargets != null) {
				abortCast()
			}

			for (character in knownList.knownCharacters) {
				if (character.fusionSkill != null && character.fusionSkill!!.target === this || character.target === this && character.lastSkillCast != null &&
						character.lastSkillCast!!.targetType === SkillTargetType.TARGET_SINGLE &&
						(character.lastSkillCast!!.skillType === SkillType.CONTINUOUS_DEBUFF ||
								character.lastSkillCast!!.skillType === SkillType.CONTINUOUS_DRAIN ||
								character.lastSkillCast!!.skillType === SkillType.CONTINUOUS_CASTS)) {
					character.abortCast()
				}
			}
		} catch (e: Exception) {
			log.error("deleteMe()", e)
		}

		return true
	}

	open fun deleteMe() {
		setDebug(null)

		if (hasAI()) {
			getAI()!!.stopAITask()
		}
	}

	protected open fun calculateRewards(killer: Creature) {}

	/**
	 * Sets HP, MP and CP and revives the Creature.
	 */
	open fun doRevive() {
		if (!isDead()) {
			return
		}
		if (!isTeleporting) {
			setIsPendingRevive(false)
			setIsDead(false)
			var restorefull = false

			if (this is Playable && this.isPhoenixBlessed) {
				restorefull = true
				this.stopPhoenixBlessing(null)
			}
			if (restorefull) {
				status!!.currentCp = currentCp //this is not confirmed, so just trigger regeneration
				status!!.setCurrentHp(maxHp.toDouble(), true) //confirmed
				status!!.setCurrentMp(maxMp.toDouble(), true) //and also confirmed
			} else {
				status!!.setCurrentHp(maxHp * Config.RESPAWN_RESTORE_HP, true)
			}
			status!!.currentCp = maxCp * Config.RESPAWN_RESTORE_CP
			status!!.currentMp = maxMp * Config.RESPAWN_RESTORE_MP

			// Start broadcast status
			broadcastPacket(Revive(this))
			if (worldRegion != null) {
				worldRegion!!.onRevive(this)
			}
		} else {
			setIsPendingRevive(true)
		}
	}

	/**
	 * Revives the Creature using skill.
	 */
	open fun doRevive(revivePower: Double) {
		doRevive()
	}

	/**
	 * Gets this creature's AI.
	 *
	 * @return the AI
	 */
	fun getAI(): CreatureAI? {
		if (ai == null) {
			synchronized(this) {
				if (ai == null) {
					// Return the new AI within the synchronized block
					// to avoid being nulled by other threads
					ai = initAI()
					return ai
				}
			}
		}
		return ai
	}

	/**
	 * Initialize this creature's AI.<br></br>
	 * OOP approach to be overridden in child classes.
	 *
	 * @return the new AI
	 */
	protected open fun initAI(): CreatureAI {
		return CreatureAI(this)
	}

	open fun setAI(newAI: CreatureAI?) {
		val oldAI = ai
		if (oldAI != null && oldAI !== newAI && oldAI is AttackableAI) {
			oldAI.stopAITask()
		}
		ai = newAI
	}

	/**
	 * Verifies if this creature has an AI,
	 *
	 * @return `true` if this creature has an AI, `false` otherwise
	 */
	open fun hasAI(): Boolean {
		return ai != null
	}

	fun detachAI() {
		setAI(null)
	}

	/**
	 * Return a list of Creature that attacked.
	 */
	fun getAttackByList(): MutableSet<Creature>? {
		if (attackByList != null) {
			return attackByList
		}

		synchronized(this) {
			if (attackByList == null) {
				attackByList = CopyOnWriteArraySet()
			}
		}
		return attackByList
	}

	/**
	 * Return True if the Creature is dead.
	 */
	fun isDead(): Boolean {
		return isAlikeDead
	}

	fun setIsDead(value: Boolean) {
		isAlikeDead = value
	}

	fun isPendingRevive(): Boolean {
		return isDead() && isPendingRevive
	}

	fun setIsPendingRevive(value: Boolean) {
		isPendingRevive = value
	}

	/**
	 * Set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others Player.
	 */
	fun setRunning() {
		if (!isRunning) {
			isRunning = true
		}
	}

	fun isInvul(attacker: Creature?): Boolean {
		if (attacker != null) {
			// If the attacker is farther than our ranged invul, his attacks won't affect us
			val radius = Math.round(calcStat(Stats.INVUL_RADIUS, 0.0, attacker, null)).toInt()
			if (radius > 0 && !Util.checkIfInRange(radius, this, attacker, false)) {
				return true
			}
		}

		return isInvul
	}

	public override fun initialKnownList(): CharKnownList {
		return CharKnownList(this)
	}

	/**
	 * Initializes the CharStat class of the WorldObject,
	 * is overwritten in classes that require a different CharStat Type.
	 *
	 *
	 * Removes the need for instanceof checks.
	 */
	open fun initCharStat() {
		stat = CharStat(this)
	}

	/**
	 * Initializes the CharStatus class of the WorldObject,
	 * is overwritten in classes that require a different CharStatus Type.
	 *
	 *
	 * Removes the need for instanceof checks.
	 */
	open fun initCharStatus() {
		status = CharStatus(this)
	}

	public override fun initialPosition(): CharPosition {
		return CharPosition(this)
	}

	/**
	 * Set the Creature movement type to walk and send Server->Client packet ChangeMoveType to all others Player.
	 */
	fun setWalking() {
		if (isRunning) {
			isRunning = false
		}
	}

	/**
	 * Task lauching the function onHitTimer().<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a Player)
	 *  * If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are Player
	 *  * If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
	 *  * if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...) <BR></BR><BR></BR>
	 */
	inner class HitTask(internal var hitTarget: Creature, internal var damage: Int, internal var crit: Boolean, internal var miss: Boolean, internal var soulshot: Double, internal var shld: Byte) : Runnable {

		override fun run() {
			try {
				onHitTimer(hitTarget, damage, crit, miss, soulshot, shld, false)
			} catch (e: Exception) {
				log.error("Failed executing HitTask. Hit target: $hitTarget")
				e.printStackTrace()
			}

		}
	}

	/**
	 * Task lauching the magic skill phases
	 */
	inner class MagicUseTask(var targets: Array<WorldObject>?, var skill: Skill?, var hitTime: Int, var coolTime: Int, var simultaneously: Boolean, var shots: Double, var second: Boolean) : Runnable {
		var count: Int = 0
		var phase: Int = 0

		init {
			count = 0
			phase = 1
			//log.info("START " + System.currentTimeMillis() + " (" + skill.getName() + ") " + hit + " | " + coolT);
		}

		override fun run() {
			try {
				when (phase) {
					1 -> onMagicLaunchedTimer(this)
					2 -> onMagicHitTimer(this)
					3 -> onMagicFinalizer(this)
					else -> {
					}
				}//log.info("LAUNC " + System.currentTimeMillis() + " (" + skill.getName() + ")");
				//log.info("HIT   " + System.currentTimeMillis() + " (" + skill.getName() + ")");
				//log.info("ENDED " + System.currentTimeMillis() + " (" + skill.getName() + ")");
			} catch (e: Exception) {
				log.error("Failed executing MagicUseTask.", e)
				if (simultaneously) {
					isCastingSimultaneouslyNow = false
				} else if (second) {
					setCastingNow2(false)
				} else {
					isCastingNow = false
				}
			}

		}
	}

	/**
	 * Task launching the function useMagic()
	 */
	private class QueuedMagicUseTask(internal var currPlayer: Player, internal var queuedSkill: Skill, internal var isCtrlPressed: Boolean, internal var isShiftPressed: Boolean) : Runnable {

		override fun run() {
			try {
				currPlayer.useMagic(queuedSkill, isCtrlPressed, isShiftPressed)
			} catch (e: Exception) {
				log.error("Failed executing QueuedMagicUseTask.", e)
			}

		}
	}

	/**
	 * Task of AI notification
	 */
	inner class NotifyAITask internal constructor(private val evt: CtrlEvent) : Runnable {

		override fun run() {
			try {
				getAI()!!.notifyEvent(evt, null)
			} catch (e: Exception) {
				log.warn("NotifyAITask failed. " + e.message + " Actor " + this@Creature, e)
			}

		}
	}

	/**
	 * Task lauching the magic skill phases
	 */
	internal inner class FlyToLocationTask(private val actor: Creature, private val x: Int, private val y: Int, private val z: Int) : Runnable {

		override fun run() {
			try {
				//actor.setXYZ(x, y, z);
				broadcastPacket(ValidateLocation(actor))

				// Dirty fix for... summons not attacking targets automatically after jumping.
				if (actor is Summon) {
					actor.getAI()!!.setIntention(CtrlIntention.AI_INTENTION_FOLLOW, actor.target)
					actor.getAI()!!.setIntention(CtrlIntention.AI_INTENTION_ATTACK, actor.target)
				}
			} catch (e: Exception) {
				log.error("Failed executing FlyToLocationTask.", e)
			}

		}
	}

	// Method - Public

	/**
	 * Launch and add L2Effect (including Stack Group management) to Creature and update client magic icon.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * All active skills effects in progress on the Creature are identified in ConcurrentHashMap(Integer,L2Effect) <B>effects</B>.
	 * The Integer key of effects is the Skill Identifier that has created the L2Effect.<BR></BR><BR></BR>
	 *
	 *
	 * Several same effect can't be used on a Creature at the same time.
	 * Indeed, effects are not stackable and the last cast will replace the previous in progress.
	 * More, some effects belong to the same Stack Group (ex WindWald and Haste Potion).
	 * If 2 effects of a same group are used at the same time on a Creature, only the more efficient (identified by its priority order) will be preserve.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Add the L2Effect to the Creature effects
	 *  * If this effect doesn't belong to a Stack Group, add its Funcs to the Calculator set of the Creature (remove the old one if necessary)
	 *  * If this effect has higher priority in its Stack Group, add its Funcs to the Calculator set of the Creature (remove previous stacked effect Funcs if necessary)
	 *  * If this effect has NOT higher priority in its Stack Group, set the effect to Not In Use
	 *  * Update active skills in progress icons on player client<BR></BR>
	 */
	open fun addEffect(newEffect: Abnormal?) {
		// Make sure it doesn't crash if buff comes from NPC buffer or something like that
		if (newEffect!!.effector is Player) {
			// Player characters who used custom command to refuse buffs, will only receive from party
			if (this is Player && isRefusingBuffs && newEffect.effector !== newEffect.effected &&
					newEffect.skill.skillType === SkillType.BUFF) {
				//if (Config.isServer(Config.TENKAI))
				//	return;

				// Effector or effected have no party, so refuse
				if (!isInParty || !newEffect.effector.isInParty) {
					return
				}

				// One of both has party, but not same, so refuse
				if (!newEffect.effector.party!!.partyMembers.contains(this)) {
					return
				}
			}
		}

		effects.queueEffect(newEffect, false)
	}

	/**
	 * Stop and remove L2Effect (including Stack Group management) from Creature and update client magic icon.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * All active skills effects in progress on the Creature are identified in ConcurrentHashMap(Integer,L2Effect) <B>effects</B>.
	 * The Integer key of effects is the Skill Identifier that has created the L2Effect.<BR></BR><BR></BR>
	 *
	 *
	 * Several same effect can't be used on a Creature at the same time.
	 * Indeed, effects are not stackable and the last cast will replace the previous in progress.
	 * More, some effects belong to the same Stack Group (ex WindWald and Haste Potion).
	 * If 2 effects of a same group are used at the same time on a Creature, only the more efficient (identified by its priority order) will be preserve.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Remove Func added by this effect from the Creature Calculator (Stop L2Effect)
	 *  * If the L2Effect belongs to a not empty Stack Group, replace theses Funcs by next stacked effect Funcs
	 *  * Remove the L2Effect from effects of the Creature
	 *  * Update active skills in progress icons on player client<BR></BR>
	 */
	fun removeEffect(effect: Abnormal) {
		effects.queueEffect(effect, true)
	}

	/**
	 * Active abnormal effects flags in the binary mask and send Server->Client UserInfo/CharInfo packet.<BR></BR><BR></BR>
	 */
	fun startVisualEffect(effect: VisualEffect) {
		abnormalEffects.add(effect.id)
		updateAbnormalEffect()
	}

	fun startVisualEffect(effectId: Int) {
		abnormalEffects.add(effectId)
		updateAbnormalEffect()
	}

	/**
	 * Active the abnormal effect Confused flag, notify the Creature AI and send Server->Client UserInfo/CharInfo packet.<BR></BR><BR></BR>
	 */
	fun startConfused() {
		getAI()!!.notifyEvent(CtrlEvent.EVT_CONFUSED)
		updateAbnormalEffect()
	}

	/**
	 * Active the abnormal effect Fake Death flag, notify the Creature AI and send Server->Client UserInfo/CharInfo packet.<BR></BR><BR></BR>
	 */
	fun startFakeDeath() {
		if (this !is Player) {
			return
		}

		for (target in knownList.knownCharacters) {
			if (target != null && target !== this && target.target === this) {
				target.target = null
				target.abortAttack()
				//target.abortCast();
				target.getAI()!!.intention = CtrlIntention.AI_INTENTION_IDLE
			}
		}

		this.setIsFakeDeath(true)
		/* Aborts any attacks/casts if fake dead */
		abortAttack()
		abortCast()
		stopMove(null)
		getAI()!!.notifyEvent(CtrlEvent.EVT_FAKE_DEATH)
		broadcastPacket(ChangeWaitType(this, ChangeWaitType.WT_START_FAKEDEATH))
	}

	/**
	 * Active the abnormal effect Fear flag, notify the Creature AI and send Server->Client UserInfo/CharInfo packet.<BR></BR><BR></BR>
	 */
	fun startFear() {
		getAI()!!.notifyEvent(CtrlEvent.EVT_AFRAID)
		updateAbnormalEffect()
	}

	/**
	 * Active the abnormal effect Fear flag, notify the Creature AI and send Server->Client UserInfo/CharInfo packet.<BR></BR><BR></BR>
	 */
	fun startLove() {
		getAI()!!.notifyEvent(CtrlEvent.EVT_INLOVE)
		updateAbnormalEffect()
	}

	/**
	 * Active the abnormal effect Muted flag, notify the Creature AI and send Server->Client UserInfo/CharInfo packet.<BR></BR><BR></BR>
	 */
	fun startMuted() {
		/* Aborts any casts if muted */
		abortCast()
		getAI()!!.notifyEvent(CtrlEvent.EVT_MUTED)
		updateAbnormalEffect()
	}

	/**
	 * Active the abnormal effect Psychical_Muted flag, notify the Creature AI and send Server->Client UserInfo/CharInfo packet.<BR></BR><BR></BR>
	 */
	fun startPsychicalMuted() {
		getAI()!!.notifyEvent(CtrlEvent.EVT_MUTED)
		updateAbnormalEffect()
	}

	/**
	 * Active the abnormal effect Root flag, notify the Creature AI and send Server->Client UserInfo/CharInfo packet.<BR></BR><BR></BR>
	 */
	fun startRooted() {
		stopMove(null)
		getAI()!!.notifyEvent(CtrlEvent.EVT_ROOTED)
		updateAbnormalEffect()
	}

	/**
	 * Active the abnormal effect Sleep flag, notify the Creature AI and send Server->Client UserInfo/CharInfo packet.<BR></BR><BR></BR>
	 */
	fun startSleeping() {
		/* Aborts any attacks/casts if sleeped */
		abortAttack()
		abortCast()
		stopMove(null)
		getAI()!!.notifyEvent(CtrlEvent.EVT_SLEEPING)
		updateAbnormalEffect()
	}

	/**
	 * Launch a Stun Abnormal Effect on the Creature.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Calculate the success rate of the Stun Abnormal Effect on this Creature
	 *  * If Stun succeed, active the abnormal effect Stun flag, notify the Creature AI and send Server->Client UserInfo/CharInfo packet
	 *  * If Stun NOT succeed, send a system message Failed to the Player attacker<BR></BR><BR></BR>
	 */
	fun startStunning() {
		/* Aborts any attacks/casts if stunned */
		abortAttack()
		abortCast()
		stopMove(null)
		getAI()!!.notifyEvent(CtrlEvent.EVT_STUNNED)
		if (this !is Summon) {
			getAI()!!.intention = CtrlIntention.AI_INTENTION_IDLE
		}
		updateAbnormalEffect()
	}

	fun startParalyze() {
		/* Aborts any attacks/casts if paralyzed */
		abortAttack()
		abortCast()
		stopMove(null)
		getAI()!!.notifyEvent(CtrlEvent.EVT_PARALYZED)
	}

	/**
	 * Modify the abnormal effect map according to the mask.<BR></BR><BR></BR>
	 */
	fun stopVisualEffect(effect: VisualEffect) {
		abnormalEffects.remove(effect.id)
		updateAbnormalEffect()
	}

	fun stopVisualEffect(effectId: Int) {
		abnormalEffects.remove(effectId)
		updateAbnormalEffect()
	}

	/**
	 * Stop all active skills effects in progress on the Creature.<BR></BR><BR></BR>
	 */
	open fun stopAllEffects() {
		effects.stopAllEffects()
	}

	open fun stopAllEffectsExceptThoseThatLastThroughDeath() {
		effects.stopAllEffectsExceptThoseThatLastThroughDeath()
	}

	/**
	 * Stop a specified/all Confused abnormal L2Effect.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Delete a specified/all (if effect=null) Confused abnormal L2Effect from Creature and update client magic icon
	 *  * Set the abnormal effect flag confused to False
	 *  * Notify the Creature AI
	 *  * Send Server->Client UserInfo/CharInfo packet<BR></BR><BR></BR>
	 */
	fun stopConfused(effect: Abnormal?) {
		if (effect == null) {
			stopEffects(EffectType.CONFUSION)
		} else {
			removeEffect(effect)
		}
		if (this !is Player) {
			getAI()!!.notifyEvent(CtrlEvent.EVT_THINK)
		}
		//updateAbnormalEffect();
	}

	/**
	 * Stop and remove the L2Effects corresponding to the Skill Identifier and update client magic icon.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * All active skills effects in progress on the Creature are identified in ConcurrentHashMap(Integer,L2Effect) <B>effects</B>.
	 * The Integer key of effects is the Skill Identifier that has created the L2Effect.<BR></BR><BR></BR>
	 *
	 * @param skillId The Skill Identifier of the L2Effect to remove from effects
	 */
	fun stopSkillEffects(skillId: Int) {
		effects.stopSkillEffects(skillId)
	}

	/**
	 * Stop and remove all L2Effect of the selected type (ex : BUFF, DMG_OVER_TIME...) from the Creature and update client magic icon.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * All active skills effects in progress on the Creature are identified in ConcurrentHashMap(Integer,L2Effect) <B>effects</B>.
	 * The Integer key of effects is the Skill Identifier that has created the L2Effect.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Remove Func added by this effect from the Creature Calculator (Stop L2Effect)
	 *  * Remove the L2Effect from effects of the Creature
	 *  * Update active skills in progress icons on player client<BR></BR><BR></BR>
	 *
	 * @param type The type of effect to stop ((ex : BUFF, DMG_OVER_TIME...)
	 */
	fun stopEffects(type: AbnormalType) {
		effects.stopEffects(type)
	}

	fun stopEffects(type: EffectType) {
		effects.stopEffects(type)
	}

	/**
	 * Exits all buffs effects of the skills with "removedOnAnyAction" set.
	 * Called on any action except movement (attack, cast).
	 */
	fun stopEffectsOnAction(skill: Skill?) {
		effects.stopEffectsOnAction(skill)
	}

	/**
	 * Exits all buffs effects of the skills with "removedOnDamage" set.
	 * Called on decreasing HP and mana burn.
	 */
	fun stopEffectsOnDamage(awake: Boolean, damage: Int) {
		effects.stopEffectsOnDamage(awake, damage)
	}

	/**
	 * Exits all buffs effects of the skills with "removedOnDebuffBlock" set.
	 * Called on debuffs blocked by a debuff immunity stat
	 */
	fun stopEffectsOnDebuffBlock() {
		effects.stopEffectsOnDebuffBlock()
	}

	/**
	 * Stop a specified/all Fake Death abnormal L2Effect.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Delete a specified/all (if effect=null) Fake Death abnormal L2Effect from Creature and update client magic icon
	 *  * Set the abnormal effect flag _fake_death to False
	 *  * Notify the Creature AI<BR></BR><BR></BR>
	 */
	fun stopFakeDeath(removeEffects: Boolean) {
		if (removeEffects) {
			stopEffects(EffectType.FAKE_DEATH)
		}

		// if this is a player instance, start the grace period for this character (grace from mobs only)!
		if (this is Player) {
			this.setIsFakeDeath(false)
			this.isRecentFakeDeath = true
		}

		val revive = ChangeWaitType(this, ChangeWaitType.WT_STOP_FAKEDEATH)
		broadcastPacket(revive)
		//TODO: Temp hack: players see FD on ppl that are moving: Teleport to someone who uses FD - if he gets up he will fall down again for that client -
		// even tho he is actually standing... Probably bad info in CharInfo packet?
		broadcastPacket(Revive(this))
	}

	/**
	 * Stop a specified/all Fear abnormal L2Effect.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Delete a specified/all (if effect=null) Fear abnormal L2Effect from Creature and update client magic icon
	 *  * Set the abnormal effect flag affraid to False
	 *  * Notify the Creature AI
	 *  * Send Server->Client UserInfo/CharInfo packet<BR></BR><BR></BR>
	 */
	fun stopFear(removeEffects: Boolean) {
		if (removeEffects) {
			stopEffects(EffectType.FEAR)
		}
		updateAbnormalEffect()
	}

	fun stopLove(removeEffects: Boolean) {
		if (removeEffects) {
			stopEffects(EffectType.LOVE)
		}
		updateAbnormalEffect()
	}

	/**
	 * Stop a specified/all Muted abnormal L2Effect.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Delete a specified/all (if effect=null) Muted abnormal L2Effect from Creature and update client magic icon
	 *  * Set the abnormal effect flag muted to False
	 *  * Notify the Creature AI
	 *  * Send Server->Client UserInfo/CharInfo packet<BR></BR><BR></BR>
	 */
	fun stopMuted(removeEffects: Boolean) {
		if (removeEffects) {
			stopEffects(EffectType.MUTE)
		}

		updateAbnormalEffect()
	}

	fun stopPsychicalMuted(removeEffects: Boolean) {
		if (removeEffects) {
			stopEffects(EffectType.PHYSICAL_MUTE)
		}

		updateAbnormalEffect()
	}

	/**
	 * Stop a specified/all Root abnormal L2Effect.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Delete a specified/all (if effect=null) Root abnormal L2Effect from Creature and update client magic icon
	 *  * Set the abnormal effect flag rooted to False
	 *  * Notify the Creature AI
	 *  * Send Server->Client UserInfo/CharInfo packet<BR></BR><BR></BR>
	 */
	fun stopRooting(removeEffects: Boolean) {
		if (removeEffects) {
			stopEffects(EffectType.ROOT)
		}

		if (this !is Player) {
			getAI()!!.notifyEvent(CtrlEvent.EVT_THINK)
		}
		updateAbnormalEffect()
	}

	/**
	 * Stop a specified/all Sleep abnormal L2Effect.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Delete a specified/all (if effect=null) Sleep abnormal L2Effect from Creature and update client magic icon
	 *  * Set the abnormal effect flag sleeping to False
	 *  * Notify the Creature AI
	 *  * Send Server->Client UserInfo/CharInfo packet<BR></BR><BR></BR>
	 */
	fun stopSleeping(removeEffects: Boolean) {
		if (removeEffects) {
			stopEffects(EffectType.SLEEP)
		}

		if (this !is Player) {
			getAI()!!.notifyEvent(CtrlEvent.EVT_THINK)
		}
		updateAbnormalEffect()
	}

	/**
	 * Stop a specified/all Stun abnormal L2Effect.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Delete a specified/all (if effect=null) Stun abnormal L2Effect from Creature and update client magic icon
	 *  * Set the abnormal effect flag stuned to False
	 *  * Notify the Creature AI
	 *  * Send Server->Client UserInfo/CharInfo packet<BR></BR><BR></BR>
	 */
	fun stopStunning(removeEffects: Boolean) {
		if (removeEffects) {
			stopEffects(EffectType.STUN)
		}

		if (this !is Player) {
			getAI()!!.notifyEvent(CtrlEvent.EVT_THINK)
		}
		updateAbnormalEffect()
	}

	fun stopParalyze(removeEffects: Boolean) {
		if (removeEffects) {
			stopEffects(EffectType.PARALYZE)
		}

		if (this !is Player) {
			getAI()!!.notifyEvent(CtrlEvent.EVT_THINK)
		}
	}

	/**
	 * Stop L2Effect: Transformation<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Remove Transformation Effect
	 *  * Notify the Creature AI
	 *  * Send Server->Client UserInfo/CharInfo packet<BR></BR><BR></BR>
	 */
	fun stopTransformation(removeEffects: Boolean) {
		if (removeEffects) {
			stopEffects(AbnormalType.MUTATE)
		}

		// if this is a player instance, then untransform, also set the transform_id column equal to 0 if not cursed.
		if (this is Player) {
			if (this.transformation != null) {
				this.unTransform(removeEffects)
			}
		}

		if (this !is Player) {
			getAI()!!.notifyEvent(CtrlEvent.EVT_THINK)
		}
		updateAbnormalEffect()
	}

	/**
	 * Not Implemented.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Overridden in</U> :</B><BR></BR><BR></BR>
	 *  * L2NPCInstance
	 *  * Player
	 *  * Summon
	 *  * DoorInstance<BR></BR><BR></BR>
	 */
	abstract fun updateAbnormalEffect()

	/**
	 * Update active skills in progress (In Use and Not In Use because stacked) icons on client.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * All active skills effects in progress (In Use and Not In Use because stacked) are represented by an icon on the client.<BR></BR><BR></BR>
	 *
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method ONLY UPDATE the client of the player and not clients of all players in the party.</B></FONT><BR></BR><BR></BR>
	 */
	fun updateEffectIcons() {
		updateEffectIcons(false)
	}

	/**
	 * Updates Effect Icons for this character(palyer/summon) and his party if any<BR></BR>
	 *
	 *
	 * Overridden in:<BR></BR>
	 * Player<BR></BR>
	 * Summon<BR></BR>
	 *
	 */
	open fun updateEffectIcons(partyOnly: Boolean) {
		// overridden
	}

	/**
	 * Return L2Effect in progress on the Creature corresponding to the Skill Identifier.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * All active skills effects in progress on the Creature are identified in <B>effects</B>.
	 *
	 * @param skillId The Skill Identifier of the L2Effect to return from the effects
	 * @return The L2Effect corresponding to the Skill Identifier
	 */
	fun getFirstEffect(skillId: Int): Abnormal? {
		return effects.getFirstEffect(skillId)
	}

	fun getFirstEffect(stackType: String): Abnormal? {
		return effects.getFirstEffect(stackType)
	}

	fun getFirstEffectByName(effectName: String): Abnormal? {
		return effects.getFirstEffectByName(effectName)
	}

	/**
	 * Return the first L2Effect in progress on the Creature created by the Skill.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * All active skills effects in progress on the Creature are identified in <B>effects</B>.
	 *
	 * @param skill The Skill whose effect must be returned
	 * @return The first L2Effect created by the Skill
	 */
	fun getFirstEffect(skill: Skill): Abnormal? {
		return effects.getFirstEffect(skill)
	}

	/**
	 * Return the first L2Effect in progress on the Creature corresponding to the Effect Type (ex : BUFF, STUN, ROOT...).<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * All active skills effects in progress on the Creature are identified in <B>effects</B>.
	 *
	 * @param tp The Effect Type of skills whose effect must be returned
	 * @return The first L2Effect corresponding to the Effect Type
	 */
	fun getFirstEffect(tp: AbnormalType): Abnormal? {
		return effects.getFirstEffect(tp)
	}

	/**
	 * This class group all mouvement data.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Data</U> :</B><BR></BR><BR></BR>
	 *  * moveTimestamp : Last time position update
	 *  * xDestination, yDestination, zDestination : Position of the destination
	 *  * xMoveFrom, yMoveFrom, zMoveFrom  : Position of the origin
	 *  * moveStartTime : Start time of the movement
	 *  * ticksToMove : Nb of ticks between the start and the destination
	 *  * xSpeedTicks, ySpeedTicks : Speed in unit/ticks<BR></BR><BR></BR>
	 */
	class MoveData {
		// when we retrieve x/y/z we use GameTimeControl.getGameTicks()
		// if we are moving, but move timestamp==gameticks, we don't need
		// to recalculate position
		var moveStartTime: Int = 0
		var moveTimestamp: Int = 0 // last update
		var xDestination: Int = 0
		var yDestination: Int = 0
		var zDestination: Int = 0
		var xAccurate: Double = 0.toDouble() // otherwise there would be rounding errors
		var yAccurate: Double = 0.toDouble()
		var zAccurate: Double = 0.toDouble()
		var heading: Int = 0

		var disregardingGeodata: Boolean = false
		var onGeodataPathIndex: Int = 0
		var geoPath: List<AbstractNodeLoc>? = null
		var geoPathAccurateTx: Int = 0
		var geoPathAccurateTy: Int = 0
		var geoPathGtx: Int = 0
		var geoPathGty: Int = 0
	}

	/**
	 * Add a Func to the Calculator set of the Creature.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * A Creature owns a table of Calculators called <B>calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR></BR><BR></BR>
	 *
	 *
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR
	 * must be create in its calculators before addind new Func object.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * If calculators is linked to NPC_STD_CALCULATOR, create a copy of NPC_STD_CALCULATOR in calculators
	 *  * Add the Func object to calculators<BR></BR><BR></BR>
	 *
	 * @param f The Func object to add to the Calculator corresponding to the state affected
	 */
	fun addStatFunc(f: Func?) {
		if (f == null) {
			return
		}

		synchronized(calculators!!) {
			var calculators = this.calculators
			// Check if Calculator set is linked to the standard Calculator set of NPC
			if (calculators === NPC_STD_CALCULATOR) {
				// Create a copy of the standard NPC Calculator set
				calculators = arrayOfNulls(Stats.NUM_STATS)

				for (i in 0 until Stats.NUM_STATS) {
					if (NPC_STD_CALCULATOR[i] != null) {
						calculators[i] = Calculator(NPC_STD_CALCULATOR[i])
					}
				}
			}

			// Select the Calculator of the affected state in the Calculator set
			val stat = f.stat.ordinal

			if (calculators!![stat] == null) {
				calculators[stat] = Calculator()
			}

			// Add the Func to the calculator corresponding to the state
			calculators[stat]?.addFunc(f)
			this.calculators = calculators
		}
	}

	/**
	 * Add a list of Funcs to the Calculator set of the Creature.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * A Creature owns a table of Calculators called <B>calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). <BR></BR><BR></BR>
	 *
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is ONLY for Player</B></FONT><BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Example of use </U> :</B><BR></BR><BR></BR>
	 *  *  Equip an item from inventory
	 *  *  Learn a new passive skill
	 *  *  Use an active skill<BR></BR><BR></BR>
	 *
	 * @param funcs The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	fun addStatFuncs(funcs: Array<Func>) {

		val modifiedStats = ArrayList<Stats>()

		for (f in funcs) {
			modifiedStats.add(f.stat)
			addStatFunc(f)
		}
		broadcastModifiedStats(modifiedStats)
	}

	/**
	 * Remove a Func from the Calculator set of the Creature.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * A Creature owns a table of Calculators called <B>calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR></BR><BR></BR>
	 *
	 *
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR
	 * must be create in its calculators before addind new Func object.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Remove the Func object from calculators<BR></BR><BR></BR>
	 *  * If Creature is a L2NPCInstance and calculators is equal to NPC_STD_CALCULATOR,
	 * free cache memory and just create a link on NPC_STD_CALCULATOR in calculators<BR></BR><BR></BR>
	 *
	 * @param f The Func object to remove from the Calculator corresponding to the state affected
	 */
	private fun removeStatFunc(f: Func?) {
		if (f == null) {
			return
		}

		// Select the Calculator of the affected state in the Calculator set
		val stat = f.stat.ordinal

		synchronized(calculators!!) {
			var calculators = this.calculators
			if (calculators!![stat] == null) {
				return
			}

			// Remove the Func object from the Calculator
			calculators[stat]!!.removeFunc(f)

			if (calculators[stat]!!.size() == 0) {
				calculators[stat] = null
			}

			// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
			if (this is Npc) {
				var i = 0
				while (i < Stats.NUM_STATS) {
					if (!Calculator.equalsCals(calculators[i], NPC_STD_CALCULATOR[i])) {
						break
					}
					i++
				}

				if (i >= Stats.NUM_STATS) {
					calculators = NPC_STD_CALCULATOR
				}
			}
			this.calculators = calculators
		}
	}

	/**
	 * Remove a list of Funcs from the Calculator set of the Player.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * A Creature owns a table of Calculators called <B>calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). <BR></BR><BR></BR>
	 *
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is ONLY for Player</B></FONT><BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Example of use </U> :</B><BR></BR><BR></BR>
	 *  *  Unequip an item from inventory
	 *  *  Stop an active skill<BR></BR><BR></BR>
	 *
	 * @param funcs The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	fun removeStatFuncs(funcs: Array<Func>) {

		val modifiedStats = ArrayList<Stats>()

		for (f in funcs) {
			modifiedStats.add(f.stat)
			removeStatFunc(f)
		}

		broadcastModifiedStats(modifiedStats)
	}

	/**
	 * Remove all Func objects with the selected owner from the Calculator set of the Creature.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * A Creature owns a table of Calculators called <B>calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR></BR><BR></BR>
	 *
	 *
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR
	 * must be create in its calculators before addind new Func object.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Remove all Func objects of the selected owner from calculators<BR></BR><BR></BR>
	 *  * If Creature is a L2NPCInstance and calculators is equal to NPC_STD_CALCULATOR,
	 * free cache memory and just create a link on NPC_STD_CALCULATOR in calculators<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Example of use </U> :</B><BR></BR><BR></BR>
	 *  *  Unequip an item from inventory
	 *  *  Stop an active skill<BR></BR><BR></BR>
	 *
	 * @param owner The Object(Skill, Item...) that has created the effect
	 */
	fun removeStatsOwner(owner: Any) {

		var modifiedStats: ArrayList<Stats>? = null

		var i = 0
		// Go through the Calculator set
		synchronized(calculators!!) {
			var calculators = this.calculators
			for (calc in calculators!!) {
				if (calc != null) {
					// Delete all Func objects of the selected owner
					if (modifiedStats != null) {
						modifiedStats!!.addAll(calc.removeOwner(owner))
					} else {
						modifiedStats = calc.removeOwner(owner)
					}

					if (calc.size() == 0) {
						calculators[i] = null
					}
				}
				i++
			}

			// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
			if (this is Npc) {
				i = 0
				while (i < Stats.NUM_STATS) {
					if (!Calculator.equalsCals(calculators!![i], NPC_STD_CALCULATOR[i])) {
						break
					}
					i++
				}

				if (i >= Stats.NUM_STATS) {
					calculators = NPC_STD_CALCULATOR
				}
			}

			if (owner is Abnormal) {
				if (!owner.preventExitUpdate) {
					broadcastModifiedStats(modifiedStats)
				}
			} else {
				broadcastModifiedStats(modifiedStats)
			}

			this.calculators = calculators
		}
	}

	protected open fun broadcastModifiedStats(stats: ArrayList<Stats>?) {
		if (stats == null || stats.isEmpty()) {
			return
		}

		var broadcastFull = false
		var su: StatusUpdate? = null

		for (stat in stats) {
			if (this is Summon && this.getOwner() != null) {
				this.updateAndBroadcastStatus(1)
				break
			} else if (stat == Stats.POWER_ATTACK_SPEED) {
				if (su == null) {
					su = StatusUpdate(this)
				}
				su.addAttribute(StatusUpdate.ATK_SPD, pAtkSpd)
			} else if (stat == Stats.MAGIC_ATTACK_SPEED) {
				if (su == null) {
					su = StatusUpdate(this)
				}
				su.addAttribute(StatusUpdate.CAST_SPD, mAtkSpd)
			} else if (stat == Stats.MAX_HP && this is Attackable) {
				if (su == null) {
					su = StatusUpdate(this)
				}
				su.addAttribute(StatusUpdate.MAX_HP, maxVisibleHp)
			} else if (stat == Stats.LIMIT_HP) {
				status!!.currentHp = currentHp // start regeneration if needed
			} else if (stat == Stats.RUN_SPEED) {
				broadcastFull = true
			}/*else if (stat == Stats.MAX_CP)
			{
				if (this instanceof Player)
				{
					if (su == null) su = new StatusUpdate(getObjectId());
					su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
				}
			}*///else if (stat==Stats.MAX_MP)
			//{
			//	if (su == null) su = new StatusUpdate(getObjectId());
			//	su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
			//}
		}

		if (this is Player) {
			val player = actingPlayer

			if (!player!!.isUpdateLocked) {
				if (broadcastFull) {
					this.updateAndBroadcastStatus(2)
				} else {
					this.updateAndBroadcastStatus(1)
					if (su != null) {
						broadcastPacket(su)
					}
				}
			}
		} else if (this is Npc) {
			if (this.isInvisible) {
				return
			}

			if (broadcastFull) {
				val plrs = knownList.knownPlayers.values
				//synchronized (getKnownList().getKnownPlayers())
				run {
					for (player in plrs) {
						if (player == null) {
							continue
						}

						if (runSpeed == 0) {
							player.sendPacket(ServerObjectInfo(this as Npc, player))
						} else {
							player.sendPacket(NpcInfo(this as Npc, player))
						}
						player.sendPacket(ExNpcSpeedInfo(this))
					}
				}
			} else if (su != null) {
				broadcastPacket(su)
			}
		} else if (su != null) {
			broadcastPacket(su)
		}
	}

	fun canCastNow(skill: Skill): Boolean {
		// Horrible hotfix
		/*if (!canDoubleCast() && isCastingNow
				&& (skillCast == null || skillCast.isDone()))
		{
			System.out.println(getName() + "'s cast fixed by the horrible hotfix");
			System.out.println("The fixing cast is " + skill);
			System.out.println("His last cast was " + skillCast);
			if (skillCast != null)
				System.out.println("Cancelled " + skillCast.isCancelled());
			//isCastingNow = false;
		}*/

		return if (canDoubleCast() && skill.isElemental) {
			!isCastingNow1 || !isCastingNow2
		} else !isCastingNow1

	}

	fun wasLastCast1(): Boolean {
		return lastCast1
	}

	fun setCastingNow2(value: Boolean) {
		isCastingNow2 = value
		lastCast1 = !value
	}

	/**
	 * Return True if the cast of the Creature can be aborted.<BR></BR><BR></BR>
	 */
	fun canAbortCast(): Boolean {
		return castInterruptTime > TimeController.getGameTicks()
	}

	open fun canDoubleCast(): Boolean {
		return false
	}

	/**
	 * Abort the attack of the Creature and send Server->Client ActionFailed packet.<BR></BR><BR></BR>
	 */
	fun abortAttack() {
		if (isAttackingNow) {
			attackingBodyPart = 0
			sendPacket(ActionFailed.STATIC_PACKET)
		}
	}

	/**
	 * Abort the cast of the Creature and send Server->Client MagicSkillCanceld/ActionFailed packet.<BR></BR><BR></BR>
	 */
	fun abortCast() {
		if (isCastingNow || isCastingSimultaneouslyNow) {
			var future = skillCast
			// cancels the skill hit scheduled task
			if (future != null) {
				future.cancel(true)
				skillCast = null
			}
			future = skillCast2
			if (future != null) {
				future.cancel(true)
				skillCast2 = null
			}
			future = simultSkillCast
			if (future != null) {
				future.cancel(true)
				simultSkillCast = null
			}

			if (fusionSkill != null) {
				fusionSkill!!.onCastAbort()
			}

			if (continuousDebuffTargets != null) {
				abortContinuousDebuff(lastSkillCast)
			}

			val mog = getFirstEffect(AbnormalType.SIGNET_GROUND)
			mog?.exit()

			if (allSkillsDisabled) {
				enableAllSkills() // this remains for forced skill use, e.g. scroll of escape
			}
			isCastingNow = false
			setCastingNow2(false)
			isCastingSimultaneouslyNow = false
			// safeguard for cannot be interrupt any more
			castInterruptTime = 0
			if (this is Player) {
				getAI()!!.notifyEvent(CtrlEvent.EVT_FINISH_CASTING) // setting back previous intention
			}

			broadcastPacket(MagicSkillCancelled(objectId)) // broadcast packet to stop animations client-side
			sendPacket(ActionFailed.STATIC_PACKET) // send an "action failed" packet to the caster
		}
	}

	/**
	 * Update the position of the Creature during a movement and return True if the movement is finished.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>move</B> of the Creature.
	 * The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR></BR><BR></BR>
	 *
	 *
	 * When the movement is started (ex : by MovetoLocation), this method will be called each 0.1 sec to estimate and update the Creature position on the server.
	 * Note, that the current server position can differe from the current client position even if each movement is straight foward.
	 * That's why, client send regularly a Client->Server ValidatePosition packet to eventually correct the gap on the server.
	 * But, it's always the server position that is used in range calculation.<BR></BR><BR></BR>
	 *
	 *
	 * At the end of the estimated movement time, the Creature position is automatically set to the destination position even if the movement is not finished.<BR></BR><BR></BR>
	 *
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current Z position is obtained FROM THE CLIENT by the Client->Server ValidatePosition Packet.
	 * But x and y positions must be calculated to avoid that players try to modify their movement speed.</B></FONT><BR></BR><BR></BR>
	 *
	 * @param gameTicks Nb of ticks since the server start
	 * @return True if the movement is finished
	 */
	open fun updatePosition(gameTicks: Int): Boolean {
		// Get movement data
		val m = move ?: return true

		if (!isVisible()) {
			move = null
			return true
		}

		// Check if this is the first update
		if (m.moveTimestamp == 0) {
			m.moveTimestamp = m.moveStartTime
			m.xAccurate = x.toDouble()
			m.yAccurate = y.toDouble()
		}

		// Check if the position has already been calculated
		if (m.moveTimestamp >= gameTicks) {
			return false
		}

		val xPrev = x
		val yPrev = y
		var zPrev = z // the z coordinate may be modified by coordinate synchronizations

		val dx: Double
		val dy: Double
		var dz: Double
		if (Config.COORD_SYNCHRONIZE == 1)
		// the only method that can modify x,y while moving (otherwise move would/should be set null)
		{
			dx = (m.xDestination - xPrev).toDouble()
			dy = (m.yDestination - yPrev).toDouble()
		} else
		// otherwise we need saved temporary values to avoid rounding errors
		{
			dx = m.xDestination - m.xAccurate
			dy = m.yDestination - m.yAccurate
		}

		val isFloating = isFlying || isInsideZone(CreatureZone.ZONE_WATER)

		// Z coordinate will follow geodata or client values
		if (Config.GEODATA > 0 && Config.COORD_SYNCHRONIZE == 2 && !isFloating && !m.disregardingGeodata && TimeController.getGameTicks() % 10 == 0 &&
				GeoData.getInstance().hasGeo(xPrev, yPrev)) {
			val geoHeight = GeoData.getInstance().getSpawnHeight(xPrev, yPrev, zPrev - 30, zPrev + 30, null)
			dz = (m.zDestination - geoHeight).toDouble()
			// quite a big difference, compare to validatePosition packet
			if (this is Player && Math.abs(this.clientZ - geoHeight) > 200 &&
					Math.abs(this.clientZ - geoHeight) < 1500) {
				dz = (m.zDestination - zPrev).toDouble() // allow diff
			} else if (isInCombat && Math.abs(dz) > 200 && dx * dx + dy * dy < 40000)
			// allow mob to climb up to pcinstance
			{
				dz = (m.zDestination - zPrev).toDouble() // climbing
			} else {
				zPrev = geoHeight.toInt()
			}
		} else {
			dz = (m.zDestination - zPrev).toDouble()
		}

		var delta = dx * dx + dy * dy
		if (delta < 10000 && dz * dz > 2500
				// close enough, allows error between client and server geodata if it cannot be avoided
				&& !isFloating)
		// should not be applied on vertical movements in water or during flight
		{
			delta = Math.sqrt(delta)
		} else {
			delta = Math.sqrt(delta + dz * dz)
		}

		var distFraction = java.lang.Double.MAX_VALUE
		if (delta > 1) {
			val distPassed = (stat!!.moveSpeed * (gameTicks - m.moveTimestamp) / TimeController.TICKS_PER_SECOND).toDouble()
			distFraction = distPassed / delta
		}

		// if (Config.DEVELOPER) Logozo.warning("Move Ticks:" + (gameTicks - m.moveTimestamp) + ", distPassed:" + distPassed + ", distFraction:" + distFraction);

		if (distFraction > 1)
		// already there
		{
			// Set the position of the Creature to the destination
			super.position.setXYZ(m.xDestination, m.yDestination, m.zDestination)
			//broadcastPacket(new ValidateLocation(this));
		} else {
			m.xAccurate += dx * distFraction
			m.yAccurate += dy * distFraction

			// Set the position of the Creature to estimated after parcial move
			super.position.setXYZ(m.xAccurate.toInt(), m.yAccurate.toInt(), zPrev + (dz * distFraction + 0.5).toInt())
		}
		revalidateZone(false)

		// Set the timer of last position update to now
		m.moveTimestamp = gameTicks

		return distFraction > 1
	}

	open fun revalidateZone(force: Boolean) {
		if (worldRegion == null) {
			return
		}

		// This function is called too often from movement code
		if (force) {
			zoneValidateCounter = 4
		} else {
			zoneValidateCounter--
			if (zoneValidateCounter < 0) {
				zoneValidateCounter = 4
			} else {
				return
			}
		}

		worldRegion!!.revalidateZones(this)
	}

	/**
	 * Stop movement of the Creature (Called by AI Accessor only).<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Delete movement data of the Creature
	 *  * Set the current position (x,y,z), its current WorldRegion if necessary and its heading
	 *  * Remove the WorldObject object from gmList** of GmListTable
	 *  * Remove object from knownObjects and knownPlayer* of all surrounding WorldRegion L2Characters <BR></BR><BR></BR>
	 *
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T send Server->Client packet StopMove/StopRotation </B></FONT><BR></BR><BR></BR>
	 */
	fun stopMove(pos: L2CharPosition?) {
		stopMove(pos, false)
	}

	open fun stopMove(pos: L2CharPosition?, updateKnownObjects: Boolean) {
		// Delete movement data of the Creature
		move = null

		//if (getAI() != null)
		//  getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

		// Set the current position (x,y,z), its current WorldRegion if necessary and its heading
		// All data are contained in a L2CharPosition object
		if (pos != null) {
			position.setXYZ(pos.x, pos.y, pos.z)
			heading = pos.heading
			revalidateZone(true)
		}
		broadcastPacket(StopMove(this))
		if (Config.MOVE_BASED_KNOWNLIST && updateKnownObjects) {
			knownList.findObjects()
		}
	}

	// called from AIAccessor only

	/**
	 * Calculate movement data for a move to location action and add the Creature to movingObjects of GameTimeController (only called by AI Accessor).<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>move</B> of the Creature.
	 * The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR></BR><BR></BR>
	 * All Creature in movement are identified in <B>movingObjects</B> of GameTimeController that will call the updatePosition method of those Creature each 0.1s.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Get current position of the Creature
	 *  * Calculate distance (dx,dy) between current position and destination including offset
	 *  * Create and Init a MoveData object
	 *  * Set the Creature move object to MoveData object
	 *  * Add the Creature to movingObjects of the GameTimeController
	 *  * Create a task to notify the AI that Creature arrives at a check point of the movement <BR></BR><BR></BR>
	 *
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T send Server->Client packet MoveToPawn/CharMoveToLocation </B></FONT><BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Example of use </U> :</B><BR></BR><BR></BR>
	 *  *  AI : onIntentionMoveTo(L2CharPosition), onIntentionPickUp(WorldObject), onIntentionInteract(WorldObject)
	 *  *  FollowTask <BR></BR><BR></BR>
	 *
	 * @param x      The X position of the destination
	 * @param y      The Y position of the destination
	 * @param z      The Y position of the destination
	 * @param offset The size of the interaction area of the Creature targeted
	 */
	fun moveToLocation(x: Int, y: Int, z: Int, offset: Int) {
		var x = x
		var y = y
		var z = z
		var offset = offset
		// Get the Move Speed of the L2Charcater
		val speed = stat!!.moveSpeed
		if (speed <= 0 || isMovementDisabled) {
			return
		}

		if (getFirstEffect(AbnormalType.SPATIAL_TRAP) != null) {
			// We're expecting the first effect in the array to be the SpatialTrap effect... F. IT.
			val st = getFirstEffect(AbnormalType.SPATIAL_TRAP)!!.effects[0] as EffectSpatialTrap

			var vecX = (x - st.trapX).toFloat()
			var vecY = (y - st.trapY).toFloat()

			val dist = Math.sqrt((vecX * vecX + vecY * vecY).toDouble())

			vecX /= dist.toFloat()
			vecY /= dist.toFloat()

			if (dist > 175 * 0.9f) {
				x = (st.trapX + vecX * 175f * 0.9f).toInt()
				y = (st.trapY + vecY * 175f * 0.9f).toInt()
			}
		} else if (isTransformed && this is Player && getFirstEffect(11580) != null || getFirstEffect(11537) != null) {
			x = x + Rnd.get(-250, 250)
			y = y + Rnd.get(-250, 250)
		}

		// Get current position of the Creature
		val curX = super.x
		val curY = super.y
		val curZ = super.z

		// Calculate distance (dx,dy) between current position and destination
		// TODO: improve Z axis move/follow support when dx,dy are small compared to dz
		var dx = (x - curX).toDouble()
		var dy = (y - curY).toDouble()
		var dz = (z - curZ).toDouble()
		var distance = Math.sqrt(dx * dx + dy * dy)

		val verticalMovementOnly = isFlying && distance == 0.0 && dz != 0.0
		if (verticalMovementOnly) {
			distance = Math.abs(dz)
		}

		// make water move short and use no geodata checks for swimming chars
		// distance in a click can easily be over 3000
		if (Config.GEODATA > 0 && isInsideZone(CreatureZone.ZONE_WATER) && distance > 700) {
			val divider = 700 / distance
			x = curX + (divider * dx).toInt()
			y = curY + (divider * dy).toInt()
			z = curZ + (divider * dz).toInt()
			dx = (x - curX).toDouble()
			dy = (y - curY).toDouble()
			dz = (z - curZ).toDouble()
			distance = Math.sqrt(dx * dx + dy * dy)
		}

		if (Config.DEBUG) {
			log.debug("distance to target:$distance")
		}

		// Define movement angles needed
		// ^
		// |	 X (x,y)
		// |   /
		// |  /distance
		// | /
		// |/ angle
		// X ---------->
		// (curx,cury)

		var cos: Double
		var sin: Double

		// Check if a movement offset is defined or no distance to go through
		if (offset > 0 || distance < 1) {
			// approximation for moving closer when z coordinates are different
			// TODO: handle Z axis movement better
			offset -= Math.abs(dz).toInt()
			if (offset < 5) {
				offset = 5
			}

			// If no distance to go through, the movement is canceled
			if (distance < 1 || distance - offset <= 0) {
				if (Config.DEBUG) {
					log.debug("already in range, no movement needed.")
				}

				// Notify the AI that the Creature is arrived at destination
				getAI()!!.notifyEvent(CtrlEvent.EVT_ARRIVED)

				return
			}
			// Calculate movement angles needed
			sin = dy / distance
			cos = dx / distance

			distance -= (offset - 5).toDouble() // due to rounding error, we have to move a bit closer to be in range

			// Calculate the new destination with offset included
			x = curX + (distance * cos).toInt()
			y = curY + (distance * sin).toInt()
		} else {
			// Calculate movement angles needed
			sin = dy / distance
			cos = dx / distance
		}

		// Create and Init a MoveData object
		val m = MoveData()

		// GEODATA MOVEMENT CHECKS AND PATHFINDING
		m.onGeodataPathIndex = -1 // Initialize not on geodata path
		m.disregardingGeodata = false

		if (Config.GEODATA > 0 && !isFlying // flying chars not checked - even canSeeTarget doesn't work yet

				&&
				(!isInsideZone(CreatureZone.ZONE_WATER) || isInsideZone(CreatureZone.ZONE_SIEGE)))
		// swimming also not checked unless in siege zone - but distance is limited
		//&& !(this instanceof L2NpcWalkerInstance)) // npc walkers not checked
		{
			val isInVehicle = this is Player && this.vehicle != null
			if (isInVehicle) {
				m.disregardingGeodata = true
			}

			val originalDistance = distance
			val originalX = x
			val originalY = y
			val originalZ = z
			val gtx = originalX - World.MAP_MIN_X shr 4
			val gty = originalY - World.MAP_MIN_Y shr 4

			// Movement checks:
			// when geodata == 2, for all characters except mobs returning home (could be changed later to teleport if pathfinding fails)
			// when geodata == 1, for l2playableinstance and l2riftinstance only
			if (Config.GEODATA == 2 && !(this is Attackable && this.isReturningToSpawnPoint) ||
					this is Player && !(isInVehicle && distance > 1500)
					//|| (this instanceof Summon && !(getAI().getIntention() == AI_INTENTION_FOLLOW)) // assuming intention_follow only when following owner
					|| isAfraid || isInLove) {
				if (isOnGeodataPath) {
					try {
						if (gtx == move!!.geoPathGtx && gty == move!!.geoPathGty) {
							return
						} else {
							move!!.onGeodataPathIndex = -1 // Set not on geodata path
						}
					} catch (e: NullPointerException) {
						// nothing
					}

				}

				if (curX < World.MAP_MIN_X || curX > World.MAP_MAX_X || curY < World.MAP_MIN_Y || curY > World.MAP_MAX_Y) {
					// Temporary fix for character outside world region errors
					log.warn("Character $name outside world area, in coordinates x:$curX y:$curY")
					getAI()!!.intention = CtrlIntention.AI_INTENTION_IDLE
					if (this is Player) {
						this.logout()
					} else if (this is Summon) {
						return  // preventation when summon get out of world coords, player will not loose him, unsummon handled from pcinstance
					} else {
						onDecay()
					}
					return
				}
				val destiny = GeoData.getInstance().moveCheck(curX, curY, curZ, x, y, z, instanceId)
				// location different if destination wasn't reached (or just z coord is different)
				x = destiny.x
				y = destiny.y
				z = destiny.z
				dx = (x - curX).toDouble()
				dy = (y - curY).toDouble()
				dz = (z - curZ).toDouble()
				distance = if (verticalMovementOnly) Math.abs(dz * dz) else Math.sqrt(dx * dx + dy * dy)
			}

			// Pathfinding checks. Only when geodata setting is 2, the LoS check gives shorter result
			// than the original movement was and the LoS gives a shorter distance than 2000
			// This way of detecting need for pathfinding could be changed.
			if (Config.GEODATA == 2 && originalDistance - distance > 30 && distance < 2000 && !isAfraid) {
				// Path calculation
				// Overrides previous movement check
				if (this is Playable && !isInVehicle || isMinion || isInCombat ||
						this is GuardInstance && instanceId != 0)
				//TODO LasTravel
				{
					m.geoPath = PathFinding.getInstance()
							.findPath(curX, curY, curZ, originalX, originalY, originalZ, instanceId, this is Playable)
					if (m.geoPath == null || m.geoPath!!.size < 2)
					// No path found
					{
						// * Even though there's no path found (remember geonodes aren't perfect),
						// the mob is attacking and right now we set it so that the mob will go
						// after target anyway, is dz is small enough.
						// * With cellpathfinding this approach could be changed but would require taking
						// off the geonodes and some more checks.
						// * Summons will follow their masters no matter what.
						// * Currently minions also must move freely since AttackableAI commands
						// them to move along with their leader
						if (this is Player || this !is Playable && !isMinion && Math.abs(z - curZ) > 140 ||
								this is Summon && !this.followStatus) {
							getAI()!!.intention = CtrlIntention.AI_INTENTION_IDLE
							return
						} else {
							m.disregardingGeodata = true
							x = originalX
							y = originalY
							z = originalZ
							distance = originalDistance
						}
					} else {
						m.onGeodataPathIndex = 0 // on first segment
						m.geoPathGtx = gtx
						m.geoPathGty = gty
						m.geoPathAccurateTx = originalX
						m.geoPathAccurateTy = originalY

						x = m.geoPath!![m.onGeodataPathIndex].x
						y = m.geoPath!![m.onGeodataPathIndex].y
						z = m.geoPath!![m.onGeodataPathIndex].z.toInt()

						// check for doors in the route
						if (DoorTable.getInstance().checkIfDoorsBetween(curX, curY, curZ, x, y, z, instanceId)) {
							m.geoPath = null
							getAI()!!.intention = CtrlIntention.AI_INTENTION_IDLE
							return
						}
						for (i in 0 until m.geoPath!!.size - 1) {
							if (DoorTable.getInstance().checkIfDoorsBetween(m.geoPath!![i], m.geoPath!![i + 1], instanceId)) {
								m.geoPath = null
								getAI()!!.intention = CtrlIntention.AI_INTENTION_IDLE
								return
							}
						}

						dx = (x - curX).toDouble()
						dy = (y - curY).toDouble()
						dz = (z - curZ).toDouble()
						distance = if (verticalMovementOnly) Math.abs(dz * dz) else Math.sqrt(dx * dx + dy * dy)
						sin = dy / distance
						cos = dx / distance
					}
				}
			}
			// If no distance to go through, the movement is canceled
			if (distance < 1 && (Config.GEODATA == 2 || this is Playable || isAfraid)) {
				if (this is Summon) {
					this.followStatus = false
				}
				getAI()!!.intention = CtrlIntention.AI_INTENTION_IDLE
				return
			}
		}

		// Apply Z distance for flying or swimming for correct timing calculations
		if ((isFlying || isInsideZone(CreatureZone.ZONE_WATER)) && !verticalMovementOnly) {
			distance = Math.sqrt(distance * distance + dz * dz)
		}

		// Caclulate the Nb of ticks between the current position and the destination
		// One tick added for rounding reasons
		val ticksToMove = 1 + (TimeController.TICKS_PER_SECOND * distance / speed).toInt()
		m.xDestination = x
		m.yDestination = y
		m.zDestination = z // this is what was requested from client

		// Calculate and set the heading of the Creature
		m.heading = 0 // initial value for coordinate sync
		// Does not broke heading on vertical movements
		if (!verticalMovementOnly) {
			heading = Util.calculateHeadingFrom(cos, sin)
		}

		if (Config.DEBUG) {
			log.debug("dist:" + distance + "speed:" + speed + " ttt:" + ticksToMove + " heading:" + heading)
		}

		m.moveStartTime = TimeController.getGameTicks()

		// Set the Creature move object to MoveData object
		move = m

		// Adding 2 ticks to Fight ping a bit
		if (isOnGeodataPath) {
			m.moveStartTime += 2
		}

		// Add the Creature to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		TimeController.getInstance().registerMovingObject(this)

		// Create a task to notify the AI that Creature arrives at a check point of the movement
		if (ticksToMove * TimeController.MILLIS_IN_TICK > 3000) {
			ThreadPoolManager.getInstance().scheduleAi(NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000)
		}

		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive
		// to destination by GameTimeController
	}

	open fun moveToNextRoutePoint(): Boolean {
		if (!isOnGeodataPath) {
			// Cancel the move action
			move = null
			return false
		}

		// Get the Move Speed of the L2Charcater
		val speed = stat!!.moveSpeed
		if (speed <= 0 || isMovementDisabled) {
			// Cancel the move action
			move = null
			return false
		}

		val md = move ?: return false

		// Create and Init a MoveData object
		val m = MoveData()

		// Update MoveData object
		m.onGeodataPathIndex = md.onGeodataPathIndex + 1 // next segment
		m.geoPath = md.geoPath
		m.geoPathGtx = md.geoPathGtx
		m.geoPathGty = md.geoPathGty
		m.geoPathAccurateTx = md.geoPathAccurateTx
		m.geoPathAccurateTy = md.geoPathAccurateTy

		if (md.onGeodataPathIndex == md.geoPath!!.size - 2) {
			m.xDestination = md.geoPathAccurateTx
			m.yDestination = md.geoPathAccurateTy
			m.zDestination = md.geoPath!![m.onGeodataPathIndex].z.toInt()
		} else {
			m.xDestination = md.geoPath!![m.onGeodataPathIndex].x
			m.yDestination = md.geoPath!![m.onGeodataPathIndex].y
			m.zDestination = md.geoPath!![m.onGeodataPathIndex].z.toInt()
		}
		val dx = (m.xDestination - super.x).toDouble()
		val dy = (m.yDestination - super.y).toDouble()
		val distance = Math.sqrt(dx * dx + dy * dy)
		// Calculate and set the heading of the Creature
		if (distance != 0.0) {
			heading = Util.calculateHeadingFrom(x, y, m.xDestination, m.yDestination)
		}

		// Caclulate the Nb of ticks between the current position and the destination
		// One tick added for rounding reasons
		val ticksToMove = 1 + (TimeController.TICKS_PER_SECOND * distance / speed).toInt()

		m.heading = 0 // initial value for coordinate sync

		m.moveStartTime = TimeController.getGameTicks()

		if (Config.DEBUG) {
			log.debug("time to target:$ticksToMove")
		}

		// Set the Creature move object to MoveData object
		move = m

		// Add the Creature to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		TimeController.getInstance().registerMovingObject(this)

		// Create a task to notify the AI that Creature arrives at a check point of the movement
		if (ticksToMove * TimeController.MILLIS_IN_TICK > 3000) {
			ThreadPoolManager.getInstance().scheduleAi(NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000)
		}

		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive
		// to destination by GameTimeController

		// Send a Server->Client packet CharMoveToLocation to the actor and all Player in its knownPlayers
		val msg = MoveToLocation(this)
		broadcastPacket(msg)

		return true
	}

	fun validateMovementHeading(heading: Int): Boolean {
		val m = move ?: return true

		var result = true
		if (m.heading != heading) {
			result = m.heading == 0 // initial value or false
			m.heading = heading
		}

		return result
	}

	/**
	 * Return the distance between the current position of the Creature and the target (x,y).<BR></BR><BR></BR>
	 *
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @return the plan distance
	 */
	@Deprecated("use getPlanDistanceSq(int x, int y, int z)")
	fun getDistance(x: Int, y: Int): Double {
		val dx = (x - x).toDouble()
		val dy = (y - y).toDouble()

		return Math.sqrt(dx * dx + dy * dy)
	}

	/**
	 * Return the distance between the current position of the Creature and the target (x,y).<BR></BR><BR></BR>
	 *
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @return the plan distance
	 */
	@Deprecated("use getPlanDistanceSq(int x, int y, int z)")
	fun getDistance(x: Int, y: Int, z: Int): Double {
		val dx = (x - x).toDouble()
		val dy = (y - y).toDouble()
		val dz = (z - z).toDouble()

		return Math.sqrt(dx * dx + dy * dy + dz * dz)
	}

	/**
	 * Return the squared distance between the current position of the Creature and the given object.<BR></BR><BR></BR>
	 *
	 * @param object WorldObject
	 * @return the squared distance
	 */
	fun getDistanceSq(`object`: WorldObject): Double {
		return getDistanceSq(`object`.x, `object`.y, `object`.z)
	}

	/**
	 * Return the squared distance between the current position of the Creature and the given x, y, z.<BR></BR><BR></BR>
	 *
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @param z Z position of the target
	 * @return the squared distance
	 */
	fun getDistanceSq(x: Int, y: Int, z: Int): Double {
		val dx = (x - x).toDouble()
		val dy = (y - y).toDouble()
		val dz = (z - z).toDouble()

		return dx * dx + dy * dy + dz * dz
	}

	/**
	 * Return the squared plan distance between the current position of the Creature and the given object.<BR></BR>
	 * (check only x and y, not z)<BR></BR><BR></BR>
	 *
	 * @param object WorldObject
	 * @return the squared plan distance
	 */
	fun getPlanDistanceSq(`object`: WorldObject): Double {
		return getPlanDistanceSq(`object`.x, `object`.y)
	}

	/**
	 * Return the squared plan distance between the current position of the Creature and the given x, y, z.<BR></BR>
	 * (check only x and y, not z)<BR></BR><BR></BR>
	 *
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @return the squared plan distance
	 */
	fun getPlanDistanceSq(x: Int, y: Int): Double {
		val dx = (x - x).toDouble()
		val dy = (y - y).toDouble()

		return dx * dx + dy * dy
	}

	/**
	 * Check if this object is inside the given radius around the given object. Warning: doesn't cover collision radius!<BR></BR><BR></BR>
	 *
	 * @param object      the target
	 * @param radius      the radius around the target
	 * @param checkZ      should we check Z axis also
	 * @param strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return true is the Creature is inside the radius.
	 * @see Creature.isInsideRadius
	 */
	fun isInsideRadius(`object`: WorldObject, radius: Int, checkZ: Boolean, strictCheck: Boolean): Boolean {
		return isInsideRadius(`object`.x, `object`.y, `object`.z, radius, checkZ, strictCheck)
	}

	/**
	 * Check if this object is inside the given plan radius around the given point. Warning: doesn't cover collision radius!<BR></BR><BR></BR>
	 *
	 * @param x           X position of the target
	 * @param y           Y position of the target
	 * @param radius      the radius around the target
	 * @param strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return true is the Creature is inside the radius.
	 */
	fun isInsideRadius(x: Int, y: Int, radius: Int, strictCheck: Boolean): Boolean {
		return isInsideRadius(x, y, 0, radius, false, strictCheck)
	}

	/**
	 * Check if this object is inside the given radius around the given point.<BR></BR><BR></BR>
	 *
	 * @param x           X position of the target
	 * @param y           Y position of the target
	 * @param z           Z position of the target
	 * @param radius      the radius around the target
	 * @param checkZ      should we check Z axis also
	 * @param strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return true is the Creature is inside the radius.
	 */
	fun isInsideRadius(x: Int, y: Int, z: Int, radius: Int, checkZ: Boolean, strictCheck: Boolean): Boolean {
		val dx = (x - x).toDouble()
		val dy = (y - y).toDouble()
		val dz = (z - z).toDouble()

		return if (strictCheck) {
			if (checkZ) {
				dx * dx + dy * dy + dz * dz < radius * radius
			} else {
				dx * dx + dy * dy < radius * radius
			}
		} else {
			if (checkZ) {
				dx * dx + dy * dy + dz * dz <= radius * radius
			} else {
				dx * dx + dy * dy <= radius * radius
			}
		}
	}

	//	/**
	//	* event that is called when the destination coordinates are reached
	//	*/
	//	public void onTargetReached()
	//	{
	//	Creature pawn = getPawnTarget();
	//
	//	if (pawn != null)
	//	{
	//	int x = pawn.getX(), y=pawn.getY(),z = pawn.getZ();
	//
	//	double distance = getDistance(x,y);
	//	if (getCurrentState() == STATE_FOLLOW)
	//	{
	//	calculateMovement(x,y,z,distance);
	//	return;
	//	}
	//
	//	//		  takes care of moving away but distance is 0 so i won't follow problem
	//
	//
	//	if (((distance > getAttackRange()) && (getCurrentState() == STATE_ATTACKING)) || (pawn.isMoving() && getCurrentState() != STATE_ATTACKING))
	//	{
	//	calculateMovement(x,y,z,distance);
	//	return;
	//	}
	//
	//	}
	//	//	   update x,y,z with the current calculated position
	//	stopMove();
	//
	//	if (Config.DEBUG)
	//	Logozo.fine(getName() +":: target reached at: x "+getX()+" y "+getY()+ " z:" + getZ());
	//
	//	if (getPawnTarget() != null)
	//	{
	//
	//	setPawnTarget(null);
	//	setMovingToPawn(false);
	//	}
	//	}
	//
	//	public void setTo(int x, int y, int z, int heading)
	//	{
	//	setX(x);
	//	setY(y);
	//	setZ(z);
	//	setHeading(heading);
	//	updateCurrentWorldRegion(); //TODO: maybe not needed here
	//	if (isMoving())
	//	{
	//	setCurrentState(STATE_IDLE);
	//	StopMove setto = new StopMove(this);
	//	broadcastPacket(setto);
	//	}
	//	else
	//	{
	//	ValidateLocation setto = new ValidateLocation(this);
	//	broadcastPacket(setto);
	//	}
	//
	//	FinishRotation fr = new FinishRotation(this);
	//	broadcastPacket(fr);
	//	}

	//	protected void startCombat()
	//	{
	//	if (currentAttackTask == null )//&& !isInCombat())
	//	{
	//	currentAttackTask = ThreadPoolManager.getInstance().scheduleMed(new AttackTask(), 0);
	//	}
	//	else
	//	{
	//	Logozo.info("multiple attacks want to start in parallel. prevented.");
	//	}
	//	}
	//

	/**
	 * Set attacking corresponding to Attacking Body part to CHEST.<BR></BR><BR></BR>
	 */
	fun setAttackingBodypart() {
		attackingBodyPart = Inventory.PAPERDOLL_CHEST
	}

	/**
	 * Retun True if arrows are available.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Overridden in </U> :</B><BR></BR><BR></BR>
	 *  *  Player<BR></BR><BR></BR>
	 */
	protected open fun checkAndEquipArrows(): Boolean {
		return true
	}

	/**
	 * Retun True if bolts are available.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Overridden in </U> :</B><BR></BR><BR></BR>
	 *  *  Player<BR></BR><BR></BR>
	 */
	protected open fun checkAndEquipBolts(): Boolean {
		return true
	}

	/**
	 * Add Exp and Sp to the Creature.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Overridden in </U> :</B><BR></BR><BR></BR>
	 *  *  Player
	 *  *  PetInstance<BR></BR><BR></BR>
	 */
	open fun addExpAndSp(addToExp: Long, addToSp: Long) {
		// Dummy method (overridden by players and pets)
	}

	/**
	 * Manage hit process (called by Hit Task).<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a Player)
	 *  * If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are Player
	 *  * If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
	 *  * if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...) <BR></BR><BR></BR>
	 *
	 * @param target   The Creature targeted
	 * @param damage   Nb of HP to reduce
	 * @param crit     True if hit is critical
	 * @param miss     True if hit is missed
	 * @param soulshot True if SoulShot are charged
	 * @param shld     True if shield is efficient
	 */
	fun onHitTimer(target: Creature?, damage: Int, crit: Boolean, miss: Boolean, soulshot: Double, shld: Byte, wasHeavyPunch: Boolean) {
		var damage = damage
		// If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL
		// and send a Server->Client packet ActionFailed (if attacker is a Player)
		if (target == null || isAlikeDead) {
			getAI()!!.notifyEvent(CtrlEvent.EVT_CANCEL)
			return
		}

		if (target.getFirstEffect(AbnormalType.SPALLATION) != null && !Util.checkIfInRange(130, this, target, false)) {
			sendMessage("Your attack has been blocked.")

			target.sendMessage("You blocked an attack.")
			return
		}

		if (this is Npc && target.isAlikeDead || target.isDead() ||
				!knownList.knowsObject(target) && this !is DoorInstance) {
			//getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			getAI()!!.notifyEvent(CtrlEvent.EVT_CANCEL)

			sendPacket(ActionFailed.STATIC_PACKET)
			return
		}

		if (miss) {
			// Notify target AI
			if (target.hasAI()) {
				target.getAI()!!.notifyEvent(CtrlEvent.EVT_EVADED, this)
			}

			// ON_EVADED_HIT
			if (target.chanceSkills != null) {
				target.chanceSkills!!.onEvadedHit(this)
			}
		}

		// Send message about damage/crit or miss
		sendDamageMessage(target, damage, false, crit, miss)

		// If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are Player
		if (!isAttackAborted) {
			// Check Raidboss attack
			// Character will be petrified if attacking a raid that's more
			// than 8 levels lower
			if (target.isRaid && target.giveRaidCurse() && !Config.RAID_DISABLE_CURSE) {
				if (level > target.level + 8) {
					val skill = SkillTable.FrequentSkill.RAID_CURSE2.skill

					if (skill != null) {
						abortAttack()
						abortCast()
						getAI()!!.intention = CtrlIntention.AI_INTENTION_IDLE
						skill.getEffects(target, this)
					} else {
						log.warn("Skill 4515 at level 1 is missing in DP.")
					}

					damage = 0 // prevents messing up drop calculation
				}
			}

			// If Creature target is a Player, send a system message
			if (target is Player) {
				val enemy = target as Player?
				enemy!!.getAI()!!.clientStartAutoAttack()

				/*if (shld && 100 - Config.ALT_PERFECT_SHLD_BLOCK < Rnd.get(100))
				{
				   if (100 - Config.ALT_PERFECT_SHLD_BLOCK < Rnd.get(100))
				   {
						 damage = 1;
						 enemy.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS)); //SHIELD_DEFENCE faultless
				   }
					else
					  enemy.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SHIELD_DEFENCE_SUCCESSFULL));
				}*/
			}

			if (!miss && damage > 0) {
				var reflectedDamage = 0

				if (!target.isInvul(this))
				// Do not reflect if target is invulnerable
				{
					// quick fix for no drop from raid if boss attack high-level char with damage reflection
					if (!target.isRaid || actingPlayer == null || actingPlayer!!.level <= target.level + 8) {
						// Reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
						var reflectPercent = target.stat!!.calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0.0, null, null)
						reflectPercent = stat!!.calcStat(Stats.REFLECT_VULN, reflectPercent, null, null)

						if (reflectPercent > 0) {
							reflectedDamage = (reflectPercent / 100.0 * Math.min(target.currentHp, damage.toDouble())).toInt()

							// Half the reflected damage for bows
							/*WeaponTemplate weaponItem = getActiveWeaponItem();
							if (weaponItem != null && (weaponItem.getItemType() == WeaponType.BOW
									 || weaponItem.getItemType() == WeaponType.CROSSBOW))
								reflectedDamage *= 0.5f;*/

							var defLimitReflects = true

							if (target.getFirstEffect(10021) != null || target.getFirstEffect(10017) != null ||
									target.getSkillLevelHash(13524) != 0) {
								defLimitReflects = false
							}

							if (defLimitReflects && reflectedDamage > target.getPDef(this)) {
								reflectedDamage = target.getPDef(this)
							}

							val totalHealth = (target.currentHp + target.currentCp).toInt()

							if (totalHealth - damage <= 0) {
								reflectedDamage = 0
							}

							//damage -= reflectedDamage;
						}
					}
				}

				// reduce targets HP
				target.reduceCurrentHp(damage.toDouble(), this, null)

				if (!wasHeavyPunch && !crit && this is Player) {

					this.lastPhysicalDamages = damage
				}

				if (reflectedDamage > 0 && !(this is Player && this.isPlayingEvent &&
								this.event.isType(EventType.StalkedSalkers)))
				//SS Events check
				{
					reduceCurrentHp(reflectedDamage.toDouble(), target, true, false, null)

					// Custom messages - nice but also more network load
					if (target is Player) {
						target.sendMessage("You reflected $reflectedDamage damage.")
					} else if (target is Summon) {
						target.getOwner().sendMessage("Summon reflected $reflectedDamage damage.")
					}

					if (this is Player) {
						this.sendMessage("Target reflected to you $reflectedDamage damage.")
					} else if (this is Summon) {
						this.getOwner().sendMessage("Target reflected to your summon $reflectedDamage damage.")
					}
				}

				if (Rnd.get(100) < 20)
				// Absorb now acts as "trigger". Let's hardcode a 20% chance
				{
					// Absorb HP from the damage inflicted
					var absorbPercent = stat!!.calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0.0, null, null)

					if (absorbPercent > 0) {
						val maxCanAbsorb = (maxHp - currentHp).toInt()
						var absorbDamage = (absorbPercent / 100.0 * damage).toInt()

						if (absorbDamage > maxCanAbsorb) {
							absorbDamage = maxCanAbsorb // Can't absorb more than max hp
						}

						if (absorbDamage > 0) {
							status!!.setCurrentHp(currentHp + absorbDamage, true, target, StatusUpdateDisplay.NORMAL)
							sendMessage("You absorbed " + absorbDamage + " HP from " + target.name + ".")
						}
					}

					// Absorb MP from the damage inflicted
					absorbPercent = stat!!.calcStat(Stats.ABSORB_MANA_DAMAGE_PERCENT, 0.0, null, null)

					if (absorbPercent > 0) {
						val maxCanAbsorb = (maxMp - currentMp).toInt()
						var absorbDamage = (absorbPercent / 100.0 * damage).toInt()

						if (absorbDamage > maxCanAbsorb) {
							absorbDamage = maxCanAbsorb // Can't absord more than max hp
						}

						if (absorbDamage > 0) {
							setCurrentMp(currentMp + absorbDamage)
						}
					}
				}

				// Notify AI with EVT_ATTACKED
				if (target.hasAI()) {
					target.getAI()!!.notifyEvent(CtrlEvent.EVT_ATTACKED, this)
				}
				getAI()!!.clientStartAutoAttack()

				if (target.isStunned && Rnd.get(100) < (if (crit) 75 else 10)) {
					target.stopStunning(true)
				}

				//Summon part
				if (target is Player) {
					if (!target.summons.isEmpty()) {
						for (summon in target.summons) {
							if (summon == null) {
								continue
							}

							summon.onOwnerGotAttacked(this)
						}
					}
				}

				if (target is Summon) {
					if (target.getOwner() != null) {
						if (!target.getOwner().summons.isEmpty()) {
							for (summon in target.getOwner().summons) {
								if (summon == null) {
									continue
								}

								summon.onOwnerGotAttacked(this)
							}
						}
					}
				}

				if (this is Summon) {
					val owner = this.getOwner()
					if (owner != null) {
						owner.getAI()!!.clientStartAutoAttack()
					}
				}

				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid && Formulas.calcAtkBreak(target, damage.toDouble())) {
					target.breakAttack()
					target.breakCast()
				}

				// Maybe launch chance skills on us
				if (chanceSkills != null && !wasHeavyPunch) {
					chanceSkills!!.onHit(target, damage, false, false, crit)
					// Reflect triggers onHit
					if (reflectedDamage > 0) {
						chanceSkills!!.onHit(target, damage, true, false, false)
					}
				}

				// Maybe launch chance skills on target
				if (target.chanceSkills != null) {
					target.chanceSkills!!.onHit(this, damage, true, false, crit)
				}

				if (this is SummonInstance && this.getOwner().chanceSkills != null && reflectedDamage > 0) {
					this.getOwner().chanceSkills!!.onHit(target, damage, true, true, false)
				}

				if (target is SummonInstance && target.getOwner().chanceSkills != null) {
					target.getOwner().chanceSkills!!.onHit(this, damage, true, true, crit)
				}
			}

			// Launch weapon Special ability effect if available
			val activeWeapon = activeWeaponItem

			activeWeapon?.getSkillEffects(this, target, crit)

			/* COMMENTED OUT BY nexus - 2006-08-17
			 *
			 * We must not discharge the soulshouts at the onHitTimer method,
			 * as this can cause unwanted soulshout consumption if the attacker
			 * recharges the soulshot right after an attack request but before
			 * his hit actually lands on the target.
			 *
			 * The soulshot discharging has been moved to the doAttack method:
			 * As soon as we know that we didn't missed the hit there, then we
			 * must discharge any charged soulshots.
			 */
			/*
			Item weapon = getActiveWeaponInstance();

			if (!miss)
			{
				if (this instanceof Summon && !(this instanceof PetInstance))
				{
					if (((Summon)this).getChargedSoulShot() != Item.CHARGED_NONE)
						((Summon)this).setChargedSoulShot(Item.CHARGED_NONE);
				}
				else
				{
					if (weapon != null && weapon.getChargedSoulshot() != Item.CHARGED_NONE)
						weapon.setChargedSoulshot(Item.CHARGED_NONE);
				}
			}
			 */

			return
		}

		if (!isCastingNow && !isCastingSimultaneouslyNow) {
			getAI()!!.notifyEvent(CtrlEvent.EVT_CANCEL)
		}
	}

	/**
	 * Break an attack and send Server->Client ActionFailed packet and a System Message to the Creature.<BR></BR><BR></BR>
	 */
	fun breakAttack() {
		if (isAttackingNow) {
			// Abort the attack of the Creature and send Server->Client ActionFailed packet
			abortAttack()

			if (this is Player) {
				//TODO Remove sendPacket because it's always done in abortAttack
				sendPacket(ActionFailed.STATIC_PACKET)

				// Send a system message
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED))
			}
		}
	}

	/**
	 * Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature.<BR></BR><BR></BR>
	 */
	fun breakCast() {
		// damage can only cancel magical skills
		if (isCastingNow && canAbortCast() && lastSkillCast != null && lastSkillCast!!.isMagic) {
			// Abort the cast of the Creature and send Server->Client MagicSkillCanceld/ActionFailed packet.
			abortCast()

			(this as? Player)?.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CASTING_INTERRUPTED))
		}
	}

	/**
	 * Reduce the arrow number of the Creature.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Overridden in </U> :</B><BR></BR><BR></BR>
	 *  *  Player<BR></BR><BR></BR>
	 */
	protected open fun reduceArrowCount(bolts: Boolean) {
		// default is to do nothing
	}

	/**
	 * Manage Forced attack (shift + select target).<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * If Creature or target is in a town area, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed
	 *  * If target is confused, send a Server->Client packet ActionFailed
	 *  * If Creature is a ArtefactInstance, send a Server->Client packet ActionFailed
	 *  * Send a Server->Client packet MyTargetSelected to start attack and Notify AI with AI_INTENTION_ATTACK <BR></BR><BR></BR>
	 *
	 * @param player The Player that attacks this character
	 */
	override fun onForcedAttack(player: Player) {
		if (Config.isServer(Config.TENKAI) && level < player.level - 5 && this is Player &&
				this.pvpFlag.toInt() == 0 && !this.isCombatFlagEquipped) {
			player.sendMessage("You can't attack lower level players.")
			player.sendPacket(ActionFailed.STATIC_PACKET)
			return
		}

		if (isInsidePeaceZone(player) && !player.isInDuel) {
			// If Creature or target is in a peace zone, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IN_PEACEZONE))
			player.sendPacket(ActionFailed.STATIC_PACKET)
			return
		}

		if (player.event != null && player.event.onForcedAttack(player, objectId)) {
			player.sendMessage("You can't attack your team mates.")
			player.sendPacket(ActionFailed.STATIC_PACKET)
			return
		}

		if (player.isInOlympiadMode && player.target != null && player.target is Playable) {
			val target: Player?
			if (player.target is Summon) {
				target = (player.target as Summon).getOwner()
			} else {
				target = player.target as Player
			}

			if (target == null || target.isInOlympiadMode && (!player.isOlympiadStart || player.olympiadGameId != target.olympiadGameId)) {
				// if Player is in Olympiad and the match isn't already start, send a Server->Client packet ActionFailed
				player.sendPacket(ActionFailed.STATIC_PACKET)
				return
			}
		}

		if (player.target != null && !player.target!!.isAttackable && !player.accessLevel.allowPeaceAttack() &&
				player.target !is NpcInstance) {
			// If target is not attackable, send a Server->Client packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET)
			return
		}
		if (player.isConfused) {
			// If target is confused, send a Server->Client packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET)
			return
		}
		// GeoData Los Check or dz > 1000
		if (!GeoData.getInstance().canSeeTarget(player, this)) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SEE_TARGET))
			player.sendPacket(ActionFailed.STATIC_PACKET)
			return
		}
		if (player.blockCheckerArena != -1) {
			player.sendPacket(ActionFailed.STATIC_PACKET)
			return
		}
		// Notify AI with AI_INTENTION_ATTACK
		player.getAI()!!.setIntention(CtrlIntention.AI_INTENTION_ATTACK, this)
	}

	@JvmOverloads
	fun isInsidePeaceZone(attacker: Player, target: WorldObject = this): Boolean {
		return !attacker.accessLevel.allowPeaceAttack() && isInsidePeaceZone(attacker as WorldObject, target)
	}

	fun isInsidePeaceZone(attacker: WorldObject, target: WorldObject?): Boolean {
		if (target == null) {
			return false
		}

		if (this is Playable && target is Playable) {
			val player = this.actingPlayer
			val targetedPlayer = target.actingPlayer

			if (player!!.duelId != 0 && player.duelId == targetedPlayer!!.duelId) {
				return false
			}
		}

		if (!(target is Playable && attacker is Playable)) {
			return false
		}

		if (instanceId > 0) {
			val instance = InstanceManager.getInstance().getInstance(instanceId)

			if (instance != null) {
				if (instance.isPvPInstance) {
					return false
				}

				if (instance.isPeaceInstance) {
					return true
				}
			}
		}

		if (target is Player) {
			val targetedPlayer = target as Player?
			if (targetedPlayer!!.isPlayingEvent) {
				return false
			}
		}

		if (Config.ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE) {
			// allows red to be attacked and red to attack flagged players
			if (target.actingPlayer != null && target.actingPlayer!!.reputation < 0) {
				return false
			}
			if (attacker.actingPlayer != null && attacker.actingPlayer!!.reputation < 0 && target.actingPlayer != null &&
					target.actingPlayer!!.pvpFlag > 0) {
				return false
			}

			if (attacker is Creature && target is Creature) {
				return (target as Creature).isInsideZone(CreatureZone.ZONE_PEACE) || (attacker as Creature).isInsideZone(CreatureZone.ZONE_PEACE)
			}
			if (attacker is Creature) {
				return TownManager.getTown(target.x, target.y, target.z) != null || (attacker as Creature).isInsideZone(CreatureZone.ZONE_PEACE)
			}
		}

		if (attacker is Creature && target is Creature) {
			return (target as Creature).isInsideZone(CreatureZone.ZONE_PEACE) || (attacker as Creature).isInsideZone(CreatureZone.ZONE_PEACE)
		}
		return if (attacker is Creature) {
			TownManager.getTown(target.x, target.y, target.z) != null || (attacker as Creature).isInsideZone(CreatureZone.ZONE_PEACE)
		} else TownManager.getTown(target.x, target.y, target.z) != null || TownManager.getTown(attacker.x, attacker.y, attacker.z) != null
	}

	/**
	 * Return the Attack Speed of the Creature (delay (in milliseconds) before next attack).<BR></BR><BR></BR>
	 */
	fun calculateTimeBetweenAttacks(target: Creature?, weapon: WeaponTemplate?): Int {
		var atkSpd = 0.0
		if (weapon != null && !isTransformed) {
			when (weapon.itemType) {
				WeaponType.BOW -> {
					atkSpd = stat!!.pAtkSpd.toDouble()
					return (1500 * 345 / atkSpd).toInt()
				}
				WeaponType.CROSSBOW, WeaponType.CROSSBOWK -> {
					atkSpd = stat!!.pAtkSpd.toDouble()
					return (1200 * 345 / atkSpd).toInt()
				}
				WeaponType.DAGGER -> atkSpd = stat!!.pAtkSpd.toDouble()
				else -> atkSpd = stat!!.pAtkSpd.toDouble()
			}//atkSpd /= 1.15;
		} else {
			atkSpd = pAtkSpd.toDouble()
		}

		return Formulas.calcPAtkSpd(this, target, atkSpd)
	}

	fun calculateReuseTime(target: Creature?, weapon: WeaponTemplate?): Int {
		if (weapon == null || isTransformed) {
			return 0
		}

		var reuse = weapon.reuseDelay
		// only bows should continue for now
		if (reuse == 0) {
			return 0
		}

		reuse *= stat!!.getWeaponReuseModifier(target).toInt()
		val atkSpd = stat!!.pAtkSpd.toDouble()
		when (weapon.itemType) {
			WeaponType.BOW, WeaponType.CROSSBOW, WeaponType.CROSSBOWK -> return (reuse * 345 / atkSpd).toInt()
			else -> return (reuse * 312 / atkSpd).toInt()
		}
	}

	/**
	 * Add a skill to the Creature skills and its Func objects to the calculator set of the Creature.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * All skills own by a Creature are identified in <B>skills</B><BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Replace oldSkill by newSkill or Add the newSkill
	 *  * If an old skill has been replaced, remove all its Func objects of Creature calculator set
	 *  * Add Func objects of newSkill to the calculator set of the Creature <BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Overridden in </U> :</B><BR></BR><BR></BR>
	 *  *  Player : Save update in the character_skills table of the database<BR></BR><BR></BR>
	 *
	 * @param newSkill The Skill to add to the Creature
	 * @return The Skill replaced or null if just added a new Skill
	 */
	fun addSkill(newSkill: Skill?): Skill? {
		var oldSkill: Skill? = null

		if (newSkill != null) {
			// Replace oldSkill by newSkill or Add the newSkill
			oldSkill = skills!!.put(newSkill.id, newSkill)

			// If an old skill has been replaced, remove all its Func objects
			if (oldSkill != null) {
				// if skill came with another one, we should delete the other one too.
				if (oldSkill.triggerAnotherSkill()) {
					removeSkill(oldSkill.triggeredId, true)
				}
				removeStatsOwner(oldSkill)
			}
			// Add Func objects of newSkill to the calculator set of the Creature
			addStatFuncs(newSkill.getStatFuncs(this))

			if (oldSkill != null && chanceSkills != null) {
				removeChanceSkill(oldSkill.id)
			}
			if (newSkill.isChance) {
				addChanceTrigger(newSkill)
			}

			/*if (!newSkill.isChance() && newSkill.triggerAnotherSkill() )
			{
				Skill bestowed = SkillTable.getInstance().getInfo(newSkill.getTriggeredId(), newSkill.getTriggeredLevel());
				addSkill(bestowed);
				//bestowed skills are invisible for player. Visible for gm's looking thru gm window.
				//those skills should always be chance or passive, to prevent hlapex.
			}

			if (newSkill.isChance() && newSkill.triggerAnotherSkill())
			{
				Skill triggeredSkill = SkillTable.getInstance().getInfo(newSkill.getTriggeredId(),newSkill.getTriggeredLevel());
				addSkill(triggeredSkill);
			}*/
		}

		return oldSkill
	}

	/**
	 * Remove a skill from the Creature and its Func objects from calculator set of the Creature.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * All skills own by a Creature are identified in <B>skills</B><BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Remove the skill from the Creature skills
	 *  * Remove all its Func objects from the Creature calculator set<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Overridden in </U> :</B><BR></BR><BR></BR>
	 *  *  Player : Save update in the character_skills table of the database<BR></BR><BR></BR>
	 *
	 * @param skill The Skill to remove from the Creature
	 * @return The Skill removed
	 */
	open fun removeSkill(skill: Skill?): Skill? {
		return if (skill == null) {
			null
		} else removeSkill(skill.id, true)

	}

	open fun removeSkill(skill: Skill?, cancelEffect: Boolean): Skill? {
		return if (skill == null) {
			null
		} else removeSkill(skill.id, cancelEffect)

		// Remove the skill from the Creature skills
	}

	@JvmOverloads
	fun removeSkill(skillId: Int, cancelEffect: Boolean = true): Skill? {
		// Remove the skill from the Creature skills
		val oldSkill = skills!!.remove(skillId)
		// Remove all its Func objects from the Creature calculator set
		if (oldSkill != null) {
			//this is just a fail-safe againts buggers and gm dummies...
			if (oldSkill.triggerAnotherSkill() && oldSkill.triggeredId > 0) {
				removeSkill(oldSkill.triggeredId, true)
			}

			// does not abort casting of the transformation dispell
			if (oldSkill.skillType !== SkillType.TRANSFORMDISPEL) {
				// Stop casting if this skill is used right now
				if (lastSkillCast != null && isCastingNow) {
					if (oldSkill.id == lastSkillCast!!.id) {
						abortCast()
					}
				}
				if (lastSimultaneousSkillCast != null && isCastingSimultaneouslyNow) {
					if (oldSkill.id == lastSimultaneousSkillCast!!.id) {
						abortCast()
					}
				}
			}

			if (cancelEffect || oldSkill.isToggle) {
				// for now, to support transformations, we have to let their
				// effects stay when skill is removed
				val e = getFirstEffect(oldSkill)
				if (e == null || e.type !== AbnormalType.MUTATE) {
					removeStatsOwner(oldSkill)
					stopSkillEffects(oldSkill.id)
				}
			}

			if (oldSkill is SkillAgathion && this is Player && this.agathionId > 0) {
				this.agathionId = 0
				this.broadcastUserInfo()
			}

			if (oldSkill is SkillMount && this is Player && this.isMounted) {
				this.dismount()
			}

			if (oldSkill.isChance && chanceSkills != null) {
				removeChanceSkill(oldSkill.id)
			}
			if (oldSkill is SkillSummon && oldSkill.id == 710 && this is Player) {
				for (summon in this.summons) {
					if (summon.npcId == 14870) {
						summon.unSummon(this)
					}
				}
			}
		}

		return oldSkill
	}

	fun removeChanceSkill(id: Int) {
		if (chanceSkills == null) {
			return
		}

		synchronized(chanceSkills!!) {
			for (trigger in chanceSkills!!.keys) {
				if (trigger !is Skill) {
					continue
				}
				if (trigger.id == id) {
					chanceSkills!!.remove(trigger)
				}
			}
		}
	}

	fun addChanceTrigger(trigger: IChanceSkillTrigger) {
		if (chanceSkills == null) {
			synchronized(this) {
				if (chanceSkills == null) {
					chanceSkills = ChanceSkillList(this)
				}
			}
		}
		chanceSkills!![trigger] = trigger.triggeredChanceCondition
	}

	fun removeChanceEffect(effect: EffectChanceSkillTrigger) {
		if (chanceSkills == null) {
			return
		}

		chanceSkills!!.remove(effect)
	}

	fun onStartChanceEffect(skill: Skill, element: Byte) {
		if (chanceSkills == null) {
			return
		}

		chanceSkills!!.onStart(skill, element)
	}

	fun onActionTimeChanceEffect(skill: Skill, element: Byte) {
		if (chanceSkills == null) {
			return
		}

		chanceSkills!!.onActionTime(skill, element)
	}

	fun onExitChanceEffect(skill: Skill, element: Byte) {
		if (chanceSkills == null) {
			return
		}

		chanceSkills!!.onExit(skill, element)
	}

	/**
	 * Return the level of a skill owned by the Creature.<BR></BR><BR></BR>
	 *
	 * @param skillId The identifier of the Skill whose level must be returned
	 * @return The level of the Skill identified by skillId
	 */
	open fun getSkillLevelHash(skillId: Int): Int {
		if (skillId >= 1566 && skillId <= 1569 || skillId == 17192) {
			return 1
		}

		val skill = getKnownSkill(skillId) ?: return -1

		return skill.levelHash
	}

	fun getSkillLevel(skillId: Int): Int {
		val skill = getKnownSkill(skillId) ?: return -1

		return skill.level
	}

	/**
	 * Return True if the skill is known by the Creature.<BR></BR><BR></BR>
	 *
	 * @param skillId The identifier of the Skill to check the knowledge
	 */
	open fun getKnownSkill(skillId: Int): Skill? {
		return if (skills == null) {
			null
		} else skills!![skillId]

	}

	/**
	 * Manage the magic skill launching task (MP, HP, Item consummation...) and display the magic skill animation on client.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Actions</U> :</B><BR></BR><BR></BR>
	 *  * Send a Server->Client packet MagicSkillLaunched (to display magic skill animation) to all Player of L2Charcater knownPlayers
	 *  * Consumme MP, HP and Item if necessary
	 *  * Send a Server->Client packet StatusUpdate with MP modification to the Player
	 *  * Launch the magic skill in order to calculate its effects
	 *  * If the skill type is PDAM, notify the AI of the target with AI_INTENTION_ATTACK
	 *  * Notify the AI of the Creature with EVT_FINISH_CASTING<BR></BR><BR></BR>
	 *
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : A magic skill casting MUST BE in progress</B></FONT><BR></BR><BR></BR>
	 *
	 * @param mut The Skill to use
	 */
	fun onMagicLaunchedTimer(mut: MagicUseTask) {
		val skill = mut.skill
		val targets = mut.targets

		if (skill == null || targets == null) {
			abortCast()
			return
		}

		/*if (calcStat(Stats.SKILL_FAILURE_RATE, 0.0, null, skill) > Rnd.get(100))
		{
			abortCast();
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CASTING_INTERRUPTED));
			return;
		}*/

		if (targets.size == 0) {
			when (skill.targetType) {
			// only AURA-type skills can be cast without target
				SkillTargetType.TARGET_AURA, SkillTargetType.TARGET_FRONT_AURA, SkillTargetType.TARGET_BEHIND_AURA, SkillTargetType.TARGET_GROUND_AREA -> {
				}
				else -> {
					if (!skill.isUseableWithoutTarget) {
						abortCast()
						return
					}
				}
			}
		}

		// Escaping from under skill's radius and peace zone check. First version, not perfect in AoE skills.
		var escapeRange = 0
		if (skill.effectRange > escapeRange) {
			escapeRange = skill.effectRange
		} else if (skill.castRange < 0 && skill.skillRadius > 80) {
			escapeRange = skill.skillRadius
		}

		if (targets.size > 0 && escapeRange > 0) {
			var skiprange = 0
			var skipgeo = 0
			var skippeace = 0
			val targetList = ArrayList<Creature>(targets.size)
			for (target in targets) {
				if (target is Creature) {
					if (skill.targetDirection !== SkillTargetDirection.CHAIN_HEAL) {
						if (!Util.checkIfInRange(escapeRange, this, target, true)) {
							skiprange++
							continue
						}
						if (skill.skillRadius > 0 && skill.isOffensive && Config.GEODATA > 0 &&
								!GeoData.getInstance().canSeeTarget(this, target)) {
							skipgeo++
							continue
						}
						if (skill.isOffensive && !skill.isNeutral) {
							if (this is Player) {
								if (target.isInsidePeaceZone(this)) {
									skippeace++
									continue
								}
							} else {
								if (target.isInsidePeaceZone(this, target)) {
									skippeace++
									continue
								}
							}
						}
					}
					targetList.add(target)
				}
				//else
				//{
				//	if (Config.DEBUG)
				//		Logozo.warning("Class cast bad: "+targets[i].getClass().toString());
				//}
			}

			if (targetList.isEmpty()) {
				if (this is Player) {
					if (skiprange > 0) {
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED))
					} else if (skipgeo > 0) {
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SEE_TARGET))
					} else if (skippeace > 0) {
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_PEACE_ZONE))
					}
				}
				abortCast()
				return
			}
			mut.targets = targetList.toTypedArray()
		}

		// Ensure that a cast is in progress
		// Check if player is using fake death.
		// Potions can be used while faking death.
		if (mut.simultaneously && !isCastingSimultaneouslyNow || !mut.simultaneously && !isCastingNow || isAlikeDead && !skill.isPotion) {
			// now cancels both, simultaneous and normal
			getAI()!!.notifyEvent(CtrlEvent.EVT_CANCEL)
			return
		}

		// Get the display identifier of the skill
		val magicId = skill.displayId

		// Get the level of the skill
		var level = getSkillLevelHash(skill.id)

		if (level < 1) {
			level = 1
		}

		// Send a Server->Client packet MagicSkillLaunched to the Creature AND to all Player in the KnownPlayers of the Creature
		if (!skill.isPotion) {
			broadcastPacket(MagicSkillLaunched(this, magicId, level, targets))
		}

		mut.phase = 2
		if (mut.hitTime == 0) {
			onMagicHitTimer(mut)
		} else if (mut.second) {
			skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(mut, 400)
		} else {
			skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, 400)
		}
	}

	/*
	 * Runs in the end of skill casting
	 */
	fun onMagicHitTimer(mut: MagicUseTask) {
		val skill = mut.skill
		val targets = mut.targets

		if (skill == null || !skill.isAuraAttack && (targets == null || targets.size <= 0)) {
			abortCast()
			return
		}

		if (fusionSkill != null || mut.skill!!.skillType === SkillType.CONTINUOUS_DEBUFF ||
				mut.skill!!.skillType === SkillType.CONTINUOUS_DRAIN || mut.skill!!.skillType === SkillType.CONTINUOUS_CASTS) {
			if (mut.simultaneously) {
				simultSkillCast = null
				isCastingSimultaneouslyNow = false
			} else if (mut.second) {
				skillCast2 = null
				setCastingNow2(false)
			} else {
				skillCast = null
				isCastingNow = false
			}
			if (fusionSkill != null) {
				fusionSkill!!.onCastAbort()
			}

			if (targets!!.size > 0) {
				notifyQuestEventSkillFinished(skill, targets[0])
			}

			if (mut.skill!!.skillType === SkillType.CONTINUOUS_DEBUFF) {
				abortContinuousDebuff(mut.skill)
			}

			return
		}
		val mog = getFirstEffect(AbnormalType.SIGNET_GROUND)
		if (mog != null) {
			if (mut.simultaneously) {
				simultSkillCast = null
				isCastingSimultaneouslyNow = false
			} else if (mut.second) {
				skillCast2 = null
				setCastingNow2(false)
			} else {
				skillCast = null
				isCastingNow = false
			}
			mog.exit()
			notifyQuestEventSkillFinished(skill, targets!![0])
			return
		}

		try {
			// Go through targets table
			for (tgt in targets!!) {
				if (tgt is Playable) {
					val target = tgt as Creature

					if (skill.skillType === SkillType.BUFF) {
						val smsg = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT)
						smsg.addSkillName(skill)
						target.sendPacket(smsg)
					}

					if (this is Player && target is Summon) {
						target.updateAndBroadcastStatus(1)
					}
				}
			}

			val su = StatusUpdate(this)
			var isSendStatus = false

			// Consume MP of the Creature and Send the Server->Client packet StatusUpdate with current HP and MP to all other Player to inform
			val mpConsume = stat!!.getMpConsume(skill).toDouble()

			if (mpConsume > 0) {
				if (mpConsume > currentMp) {
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_MP))
					abortCast()
					return
				}

				status!!.reduceMp(mpConsume)
				su.addAttribute(StatusUpdate.CUR_MP, currentMp.toInt())
				isSendStatus = true
			}

			// Consume HP if necessary and Send the Server->Client packet StatusUpdate with current HP and MP to all other Player to inform
			if (skill.hpConsume > 0) {
				var consumeHp: Double

				consumeHp = calcStat(Stats.HP_CONSUME_RATE, skill.hpConsume.toDouble(), null, null)
				if (consumeHp + 1 >= currentHp) {
					consumeHp = currentHp - 1.0
				}

				status!!.reduceHp(consumeHp, this, true)

				su.addAttribute(StatusUpdate.CUR_HP, currentHp.toInt())
				isSendStatus = true
			}

			// Consume CP if necessary and Send the Server->Client packet StatusUpdate with current CP/HP and MP to all other Player to inform
			if (skill.cpConsume > 0) {
				var consumeCp: Double

				consumeCp = skill.cpConsume.toDouble()
				if (consumeCp + 1 >= currentHp) {
					consumeCp = currentHp - 1.0
				}

				status!!.reduceCp(consumeCp.toInt())
				su.addAttribute(StatusUpdate.CUR_CP, currentCp.toInt())
				isSendStatus = true
			}

			// Send a Server->Client packet StatusUpdate with MP modification to the Player
			if (isSendStatus) {
				sendPacket(su)
			}

			if (this is Player) {
				val charges = this.charges
				// check for charges
				if (skill.maxCharges == 0 && charges < skill.numCharges) {
					val sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED)
					sm.addSkillName(skill)
					sendPacket(sm)
					abortCast()
					return
				}
				// generate charges if any
				if (skill.numCharges > 0) {
					var maxCharges = skill.maxCharges
					if (maxCharges == 15 && this.classId != 152 && this.classId != 155) {
						maxCharges = 10
					}

					if (maxCharges > 0) {
						this.increaseCharges(skill.numCharges, maxCharges)
					} else {
						this.decreaseCharges(skill.numCharges)
					}
				}

				// Consume Souls if necessary
				if (skill.soulConsumeCount > 0 || skill.maxSoulConsumeCount > 0) {
					if (!this.decreaseSouls(
									if (skill.soulConsumeCount > 0) skill.soulConsumeCount else skill.maxSoulConsumeCount, skill)) {
						abortCast()
						return
					}
				}
			}

			// On each repeat restore shots before cast
			if (mut.count > 0) {
				val weaponInst = activeWeaponInstance
				if (weaponInst != null) {
					if (mut.skill!!.useSoulShot()) {
						weaponInst.chargedSoulShot = mut.shots
					} else if (mut.skill!!.useSpiritShot()) {
						weaponInst.chargedSpiritShot = mut.shots
					}
				}
			}

			// Launch the magic skill in order to calculate its effects
			callSkill(mut.skill, mut.targets)
		} catch (e: NullPointerException) {
			log.warn("", e)
		}

		if (mut.hitTime > 0) {
			mut.count++
			if (mut.count < skill.hitCounts) {
				val hitTime = mut.hitTime * skill.hitTimings[mut.count] / 100
				if (mut.simultaneously) {
					simultSkillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, hitTime.toLong())
				} else if (mut.second) {
					skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(mut, hitTime.toLong())
				} else {
					skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, hitTime.toLong())
				}
				return
			}
		}

		mut.phase = 3
		if (mut.hitTime == 0 || mut.coolTime == 0) {
			onMagicFinalizer(mut)
		} else {
			if (mut.simultaneously) {
				simultSkillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, mut.coolTime.toLong())
			} else if (mut.second) {
				skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(mut, mut.coolTime.toLong())
			} else {
				skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, mut.coolTime.toLong())
			}
		}
	}

	/*
	 * Runs after skill hitTime+coolTime
	 */
	fun onMagicFinalizer(mut: MagicUseTask) {
		if (mut.simultaneously) {
			simultSkillCast = null
			isCastingSimultaneouslyNow = false
			return
		} else if (mut.second) {
			skillCast2 = null
			setCastingNow2(false)
			castInterruptTime = 0
		} else {
			skillCast = null
			isCastingNow = false
			castInterruptTime = 0
		}

		val skill = mut.skill
		val target = if (mut.targets!!.size > 0) mut.targets!![0] else null

		// Attack target after skill use
		if ((skill!!.nextActionIsAttack() || skill.nextActionIsAttackMob() && target is Attackable) && target !== this &&
				target === target) {
			if (getAI() == null || getAI()!!.nextIntention == null ||
					getAI()!!.nextIntention!!.ctrlIntention != CtrlIntention.AI_INTENTION_MOVE_TO ||
					getAI()!!.nextIntention!!.ctrlIntention != CtrlIntention.AI_INTENTION_IDLE) {
				getAI()!!.setIntention(CtrlIntention.AI_INTENTION_ATTACK, target)
			}
		}
		if (skill.isOffensive && !skill.isNeutral && !(skill.skillType === SkillType.UNLOCK) &&
				!(skill.skillType === SkillType.DELUXE_KEY_UNLOCK)) {
			getAI()!!.clientStartAutoAttack()
		}

		// Notify the AI of the Creature with EVT_FINISH_CASTING
		getAI()!!.notifyEvent(CtrlEvent.EVT_FINISH_CASTING)

		notifyQuestEventSkillFinished(skill, target)

		/*
		 * If character is a player, then wipe their current cast state and
		 * check if a skill is queued.
		 *
		 * If there is a queued skill, launch it and wipe the queue.
		 */
		if (this is Player) {
			val currPlayer = this
			val queuedSkill = currPlayer.queuedSkill

			currPlayer.setCurrentSkill(null, false, false)
			if (queuedSkill != null) {
				currPlayer.setQueuedSkill(null, false, false)

				// DON'T USE : Recursive call to useMagic() method
				// currPlayer.useMagic(queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed());
				ThreadPoolManager.getInstance()
						.executeTask(QueuedMagicUseTask(currPlayer,
								queuedSkill.skill,
								queuedSkill.isCtrlPressed,
								queuedSkill.isShiftPressed))
			}
		}
	}

	// Quest event ON_SPELL_FNISHED
	protected open fun notifyQuestEventSkillFinished(skill: Skill, target: WorldObject?) {

	}

	/**
	 * Enable a skill (remove it from disabledSkills of the Creature).<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * All skills disabled are identified by their skillId in <B>disabledSkills</B> of the Creature <BR></BR><BR></BR>
	 *
	 * @param skill The Skill to enable
	 */
	open fun enableSkill(skill: Skill?) {
		if (skill == null || disabledSkills == null) {
			return
		}

		disabledSkills!!.remove(skill.reuseHashCode)
	}

	/**
	 * Disable this skill id for the duration of the delay in milliseconds.
	 *
	 * @param delay (seconds * 1000)
	 */
	fun disableSkill(skill: Skill?, delay: Long) {
		if (skill == null) {
			return
		}

		if (disabledSkills == null) {
			disabledSkills = Collections.synchronizedMap(HashMap())
		}

		disabledSkills!![skill.reuseHashCode] = if (delay > 10) System.currentTimeMillis() + delay else java.lang.Long.MAX_VALUE
	}

	/**
	 * Check if a skill is disabled.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * All skills disabled are identified by their reuse hashcodes in <B>disabledSkills</B> of the Creature <BR></BR><BR></BR>
	 *
	 * @param skill The Skill to check
	 */
	fun isSkillDisabled(skill: Skill?): Boolean {
		if (skill == null) {
			return true
		}

		var canCastWhileStun = false

		when (skill.id) {
			30008 // Wind Blend
				, 19227 // Wind Blend Trigger
				, 30009 // Deceptive Blink
			-> canCastWhileStun = true
			else -> {
			}
		}

		if (!canCastWhileStun) {
			if (isAllSkillsDisabled && !skill.canBeUsedWhenDisabled()) {
				return true
			}
		}

		return isSkillDisabled(skill.reuseHashCode)
	}

	/**
	 * Check if a skill is disabled.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Concept</U> :</B><BR></BR><BR></BR>
	 * All skills disabled are identified by their reuse hashcodes in <B>disabledSkills</B> of the Creature <BR></BR><BR></BR>
	 *
	 * @param reuseHashcode The reuse hashcode of the skillId/level to check
	 */
	fun isSkillDisabled(reuseHashcode: Int): Boolean {
		if (disabledSkills == null) {
			return false
		}

		val timeStamp = disabledSkills!![reuseHashcode] ?: return false

		if (timeStamp < System.currentTimeMillis()) {
			disabledSkills!!.remove(reuseHashcode)
			return false
		}

		return true
	}

	/**
	 * Disable all skills (set allSkillsDisabled to True).<BR></BR><BR></BR>
	 */
	fun disableAllSkills() {
		if (Config.DEBUG) {
			log.debug("all skills disabled")
		}
		allSkillsDisabled = true
	}

	/**
	 * Enable all skills (set allSkillsDisabled to False).<BR></BR><BR></BR>
	 */
	fun enableAllSkills() {
		if (Config.DEBUG) {
			log.debug("all skills enabled")
		}
		allSkillsDisabled = false
	}

	/**
	 * Launch the magic skill and calculate its effects on each target contained in the targets table.<BR></BR><BR></BR>
	 *
	 * @param skill   The Skill to use
	 * @param targets The table of WorldObject targets
	 */
	fun callSkill(skill: Skill?, targets: Array<WorldObject>?) {
		try {
			// Get the skill handler corresponding to the skill type (PDAM, MDAM, SWEEP...) started in gameserver
			val handler = SkillHandler.getInstance().getSkillHandler(skill!!.skillType)
			val activeWeapon = activeWeaponItem

			// Check if the toggle skill effects are already in progress on the Creature
			if (skill.isToggle && getFirstEffect(skill.id) != null) {
				return
			}

			// Initial checks
			for (trg in targets!!) {
				if (trg is Creature) {
					// Set some values inside target's instance for later use

					// Check Raidboss attack and
					// check buffing chars who attack raidboss. Results in mute.
					var targetsAttackTarget: Creature? = null
					var targetsCastTarget: Creature? = null
					if (trg.hasAI()) {
						targetsAttackTarget = trg.getAI()!!.attackTarget
						targetsCastTarget = trg.getAI()!!.castTarget
					}
					if (!Config.RAID_DISABLE_CURSE && (trg.isRaid && trg.giveRaidCurse() && level > trg.level + 8 ||
									(!skill.isOffensive && targetsAttackTarget != null && targetsAttackTarget.isRaid &&
											targetsAttackTarget.giveRaidCurse() && targetsAttackTarget.getAttackByList()!!.contains(trg) // has attacked raid

											&& level > targetsAttackTarget.level + 8) ||
									(!skill.isOffensive && targetsCastTarget != null && targetsCastTarget.isRaid && targetsCastTarget.giveRaidCurse() &&
											targetsCastTarget.getAttackByList()!!.contains(trg) // has attacked raid

											&& level > targetsCastTarget.level + 8))) {
						if (skill.isMagic) {
							val tempSkill = SkillTable.FrequentSkill.RAID_CURSE.skill
							if (tempSkill != null) {
								abortAttack()
								abortCast()
								getAI()!!.intention = CtrlIntention.AI_INTENTION_IDLE
								tempSkill.getEffects(trg, this)
							} else {
								log.warn("Skill 4215 at level 1 is missing in DP.")
							}
						} else {
							val tempSkill = SkillTable.FrequentSkill.RAID_CURSE2.skill
							if (tempSkill != null) {
								abortAttack()
								abortCast()
								getAI()!!.intention = CtrlIntention.AI_INTENTION_IDLE
								tempSkill.getEffects(trg, this)
							} else {
								log.warn("Skill 4515 at level 1 is missing in DP.")
							}
						}
						return
					}

					// Check if over-hit is possible
					if (skill.isOverhit) {
						if (trg is Attackable) {
							trg.overhitEnabled(true)
						}
					}

					// crafting does not trigger any chance skills
					// possibly should be unhardcoded
					when (skill.skillType) {
						SkillType.COMMON_CRAFT, SkillType.DWARVEN_CRAFT -> {
						}
						else -> {
							// Launch weapon Special ability skill effect if available
							if (activeWeapon != null && !trg.isDead()) {
								if (activeWeapon.getSkillEffects(this, trg, skill).size > 0 && this is Player) {
									val sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ACTIVATED)
									sm.addSkillName(skill)
									sendPacket(sm)
								}
							}

							// Maybe launch chance skills on us
							if (chanceSkills != null) {
								chanceSkills!!.onSkillHit(trg, skill, false, false)
							}
							// Maybe launch chance skills on target
							if (trg.chanceSkills != null) {
								trg.chanceSkills!!.onSkillHit(this, skill, false, true)
							}

							if (trg is SummonInstance && trg.getOwner().chanceSkills != null) {
								trg.getOwner().chanceSkills!!.onSkillHit(this, skill, true, true)
							}
						}
					}
				}
			}

			// Launch the magic skill and calculate its effects
			if (handler != null) {
				handler.useSkill(this, skill, targets)
			} else {
				skill.useSkill(this, targets)
			}

			val player = actingPlayer
			if (player != null) {
				for (target in targets) {
					// EVT_ATTACKED and PvPStatus
					if (target is Creature) {
						if (skill.isNeutral) {
							// no flags
						} else if (skill.isOffensive) {
							if (target is Player || target is Summon || target is Trap) {
								// Signets are a special case, casted on target_self but don't harm self
								if (skill.skillType !== SkillType.SIGNET && skill.skillType !== SkillType.SIGNET_CASTTIME) {
									if (target is Player) {
										target.getAI()!!.clientStartAutoAttack()
									} else if (target is Summon && (target as Creature).hasAI()) {
										val owner = target.getOwner()
										if (owner != null) {
											owner.getAI()!!.clientStartAutoAttack()
										}
									}
									// attack of the own pet does not flag player
									// triggering trap not flag trap owner
									if (player.pet !== target && !player.summons.contains(target) && this !is Trap &&
											this !is MonsterInstance) {
										player.updatePvPStatus(target)
									}
								}
							} else if (target is Attackable) {
								when (skill.id) {
									51 // Lure
										, 511 // Temptation
									-> {
									}
									else ->
										// add attacker into list
										(target as Creature).addAttackerToAttackByList(this)
								}
							}
							// notify target AI about the attack
							if (target.hasAI()) {
								when (skill.skillType) {
									SkillType.AGGREDUCE, SkillType.AGGREDUCE_CHAR, SkillType.AGGREMOVE -> {
									}
									else -> target.getAI()!!.notifyEvent(CtrlEvent.EVT_ATTACKED, this)
								}
							}
						} else {
							if (target is Player) {
								// Casting non offensive skill on player with pvp flag set or with karma
								if (!(target == this || target == player) && (target.pvpFlag > 0 || target.reputation < 0)) {
									player.updatePvPStatus()
								}

								PlayerAssistsManager.getInstance().updateHelpTimer(player, target)
							} else if (target is Attackable) {
								when (skill.skillType) {
									SkillType.SUMMON, SkillType.BEAST_FEED, SkillType.UNLOCK, SkillType.DELUXE_KEY_UNLOCK, SkillType.UNLOCK_SPECIAL -> {
									}
									else -> player.updatePvPStatus()
								}
							}
						}
					}
				}

				// Mobs in range 1000 see spell
				val objs = player.knownList.getKnownObjects().values
				//synchronized (player.getKnownList().getKnownObjects())
				run {
					for (spMob in objs) {
						if (spMob is Npc) {
							val npcMob = spMob

							if (npcMob.isInsideRadius(player, 1000, true, true)
									&& (npcMob.template!! as NpcTemplate).getEventQuests(Quest.QuestEventType.ON_SKILL_SEE) != null) {
								for (quest in (npcMob.template!! as NpcTemplate).getEventQuests(Quest.QuestEventType.ON_SKILL_SEE)!!) {
									quest.notifySkillSee(npcMob, player, skill, targets, this is Summon)
								}
							}
						}
					}
				}
			}
			// Notify AI
			if (skill.isOffensive) {
				when (skill.skillType) {
					SkillType.AGGREDUCE, SkillType.AGGREDUCE_CHAR, SkillType.AGGREMOVE -> {
					}
					else -> for (target in targets) {
						if (target is Creature && target.hasAI()) {
							// notify target AI about the attack
							target.getAI()!!.notifyEvent(CtrlEvent.EVT_ATTACKED, this)
						}
					}
				}
			}
		} catch (e: Exception) {
			log.warn(javaClass.simpleName + ": callSkill() failed.")
			e.printStackTrace()
		}

	}

	/**
	 * Return True if the Creature is behind the target and can't be seen.<BR></BR><BR></BR>
	 */
	fun isBehind(target: WorldObject?): Boolean {
		val angleChar: Double
		val angleTarget: Double
		var angleDiff: Double
		val maxAngleDiff = 60.0

		if (target == null) {
			return false
		}

		if (target is Creature) {
			val target1 = target as Creature?
			angleChar = Util.calculateAngleFrom(this, target1!!)
			angleTarget = Util.convertHeadingToDegree(target1.heading)
			angleDiff = angleChar - angleTarget
			if (angleDiff <= -360 + maxAngleDiff) {
				angleDiff += 360.0
			}
			if (angleDiff >= 360 - maxAngleDiff) {
				angleDiff -= 360.0
			}
			if (Math.abs(angleDiff) <= maxAngleDiff) {
				if (Config.DEBUG) {
					log.info("Char " + name + " is behind " + target.name)
				}
				return true
			}
		} else {
			log.debug("isBehindTarget's target not an L2 Character.")
		}
		return false
	}

	/**
	 * Return True if the target is facing the Creature.<BR></BR><BR></BR>
	 */
	fun isInFrontOf(target: Creature?): Boolean {
		val angleChar: Double
		val angleTarget: Double
		var angleDiff: Double
		val maxAngleDiff = 60.0
		if (target == null) {
			return false
		}

		angleTarget = Util.calculateAngleFrom(target, this)
		angleChar = Util.convertHeadingToDegree(target.heading)
		angleDiff = angleChar - angleTarget
		if (angleDiff <= -360 + maxAngleDiff) {
			angleDiff += 360.0
		}
		if (angleDiff >= 360 - maxAngleDiff) {
			angleDiff -= 360.0
		}
		return Math.abs(angleDiff) <= maxAngleDiff
	}

	/**
	 * Returns true if target is in front of Creature (shield def etc)
	 */
	fun isFacing(target: WorldObject?, maxAngle: Int): Boolean {
		val angleChar: Double
		val angleTarget: Double
		var angleDiff: Double
		val maxAngleDiff: Double
		if (target == null) {
			return false
		}

		maxAngleDiff = (maxAngle / 2).toDouble()
		angleTarget = Util.calculateAngleFrom(this, target)
		angleChar = Util.convertHeadingToDegree(heading)
		angleDiff = angleChar - angleTarget
		if (angleDiff <= -360 + maxAngleDiff) {
			angleDiff += 360.0
		}
		if (angleDiff >= 360 - maxAngleDiff) {
			angleDiff -= 360.0
		}
		return Math.abs(angleDiff) <= maxAngleDiff
	}

	/**
	 * Sets isCastingNow to true and castInterruptTime is calculated from end time (ticks)
	 */
	fun forceIsCasting(newSkillCastEndTick: Int) {
		isCastingNow = true
		// for interrupt -400 ms
		castInterruptTime = newSkillCastEndTick - 4
	}

	open fun updatePvPFlag(value: Int) {
		// Overridden in Player
	}

	// Stat - NEED TO REMOVE ONCE L2CHARSTAT IS COMPLETE
	// Property - Public
	fun calcStat(stat: Stats, init: Double, target: Creature?, skill: Skill?): Double {
		return this.stat!!.calcStat(stat, init, target, skill)
	}

	open fun getCriticalHit(target: Creature?, skill: Skill?): Int {
		return stat!!.getCriticalHit(target, skill)
	}

	fun getPCriticalDamage(target: Creature?, damage: Double, skill: Skill?): Double {
		return stat!!.getPCriticalDamage(target, damage, skill)
	}

	fun getEvasionRate(target: Creature?): Int {
		return stat!!.getEvasionRate(target)
	}

	fun getMEvasionRate(target: Creature?): Int {
		return stat!!.getMEvasionRate(target)
	}

	fun getMagicalAttackRange(skill: Skill?): Int {
		return stat!!.getMagicalAttackRange(skill)
	}

	open fun getMAtk(target: Creature?, skill: Skill?): Int {
		return stat!!.getMAtk(target, skill)
	}

	fun getMCriticalHit(target: Creature?, skill: Skill?): Int {
		return stat!!.getMCriticalHit(target, skill)
	}

	open fun getMDef(target: Creature?, skill: Skill?): Int {
		return stat!!.getMDef(target, skill)
	}

	fun getMReuseRate(skill: Skill?): Double {
		return stat!!.getMReuseRate(skill)
	}

	fun getPAtk(target: Creature?): Int {
		return stat!!.getPAtk(target)
	}

	fun getPAtkAnimals(target: Creature?): Double {
		return stat!!.getPAtkAnimals(target)
	}

	fun getPAtkDragons(target: Creature?): Double {
		return stat!!.getPAtkDragons(target)
	}

	fun getPAtkInsects(target: Creature?): Double {
		return stat!!.getPAtkInsects(target)
	}

	fun getPAtkMonsters(target: Creature?): Double {
		return stat!!.getPAtkMonsters(target)
	}

	fun getPAtkPlants(target: Creature?): Double {
		return stat!!.getPAtkPlants(target)
	}

	fun getPAtkGiants(target: Creature?): Double {
		return stat!!.getPAtkGiants(target)
	}

	fun getPAtkMagicCreatures(target: Creature?): Double {
		return stat!!.getPAtkMagicCreatures(target)
	}

	fun getPDefAnimals(target: Creature?): Double {
		return stat!!.getPDefAnimals(target)
	}

	fun getPDefDragons(target: Creature?): Double {
		return stat!!.getPDefDragons(target)
	}

	fun getPDefInsects(target: Creature?): Double {
		return stat!!.getPDefInsects(target)
	}

	fun getPDefMonsters(target: Creature?): Double {
		return stat!!.getPDefMonsters(target)
	}

	fun getPDefPlants(target: Creature?): Double {
		return stat!!.getPDefPlants(target)
	}

	fun getPDefGiants(target: Creature?): Double {
		return stat!!.getPDefGiants(target)
	}

	fun getPDefMagicCreatures(target: Creature?): Double {
		return stat!!.getPDefMagicCreatures(target)
	}

	//PvP Bonus
	fun getPvPPhysicalDamage(target: Creature?): Double {
		return stat!!.getPvPPhysicalDamage(target)
	}

	fun getPvPPhysicalDefense(attacker: Creature?): Double {
		return stat!!.getPvPPhysicalDefense(attacker)
	}

	fun getPvPPhysicalSkillDamage(target: Creature?): Double {
		return stat!!.getPvPPhysicalSkillDamage(target)
	}

	fun getPvPPhysicalSkillDefense(attacker: Creature?): Double {
		return stat!!.getPvPPhysicalSkillDefense(attacker)
	}

	fun getPvPMagicDamage(target: Creature?): Double {
		return stat!!.getPvPMagicDamage(target)
	}

	fun getPvPMagicDefense(attacker: Creature?): Double {
		return stat!!.getPvPMagicDefense(attacker)
	}

	//PvE Bonus
	fun getPvEPhysicalDamage(target: Creature?): Double {
		return stat!!.getPvEPhysicalDamage(target)
	}

	fun getPvEPhysicalDefense(attacker: Creature?): Double {
		return stat!!.getPvEPhysicalDefense(attacker)
	}

	fun getPvEPhysicalSkillDamage(target: Creature?): Double {
		return stat!!.getPvEPhysicalSkillDamage(target)
	}

	fun getPvEPhysicalSkillDefense(attacker: Creature?): Double {
		return stat!!.getPvEPhysicalSkillDefense(attacker)
	}

	fun getPvEMagicDamage(target: Creature?): Double {
		return stat!!.getPvEMagicDamage(target)
	}

	fun getPvEMagicDefense(attacker: Creature?): Double {
		return stat!!.getPvEMagicDefense(attacker)
	}

	fun getPDef(target: Creature?): Int {
		return stat!!.getPDef(target)
	}

	// Status - NEED TO REMOVE ONCE L2CHARTATUS IS COMPLETE
	// Method - Public
	fun addStatusListener(`object`: Creature?) {
		status!!.addStatusListener(`object`)
	}

	open fun reduceCurrentHp(i: Double, attacker: Creature?, skill: Skill?) {
		reduceCurrentHp(i, attacker, true, false, skill)
	}

	open fun reduceCurrentHpByDOT(i: Double, attacker: Creature?, skill: Skill) {
		reduceCurrentHp(i, attacker, !skill.isToggle, true, skill)
	}

	open fun reduceCurrentHp(i: Double, attacker: Creature?, awake: Boolean, isDOT: Boolean, skill: Skill?) {
		if (i == -1.0) {
			return
		}

		if (Config.L2JMOD_CHAMPION_ENABLE && isChampion && Config.L2JMOD_CHAMPION_HP != 0) {
			status!!.reduceHp(i / Config.L2JMOD_CHAMPION_HP, attacker, awake, isDOT, false)
		} else {
			status!!.reduceHp(i, attacker, awake, isDOT, false)
		}
	}

	fun reduceCurrentMp(i: Double) {
		status!!.reduceMp(i)
	}

	fun removeStatusListener(`object`: Creature) {
		status!!.removeStatusListener(`object`)
	}

	protected fun stopHpMpRegeneration() {
		status!!.stopHpMpRegeneration()
	}

	fun setCurrentCp(newCp: Double?) {
		setCurrentCp(newCp as Double)
	}

	fun setCurrentCp(newCp: Double) {
		status!!.currentCp = newCp
	}

	fun setCurrentHpMp(newHp: Double, newMp: Double) {
		status!!.setCurrentHpMp(newHp, newMp)
	}

	fun setCurrentMp(newMp: Double?) {
		setCurrentMp(newMp as Double)
	}

	fun setCurrentMp(newMp: Double) {
		status!!.currentMp = newMp
	}

	/**
	 * Send system message about damage.<BR></BR><BR></BR>
	 *
	 *
	 * <B><U> Overridden in </U> :</B><BR></BR><BR></BR>
	 *  *  Player
	 *  *  SummonInstance
	 *  *  PetInstance<BR></BR><BR></BR>
	 */
	open fun sendDamageMessage(target: Creature?, damage: Int, mcrit: Boolean, pcrit: Boolean, miss: Boolean) {}

	fun abortContinuousDebuff(skill: Skill?) {
		if (continuousDebuffTargets == null || skill == null) {
			return
		}

		for (obj in continuousDebuffTargets!!) {
			if (obj !is Creature) {
				continue
			}

			for (abnormal in obj.allEffects) {
				if (abnormal.skill === skill) {
					abnormal.exit()
				}
			}
		}

		continuousDebuffTargets = null
	}

	open fun getAttackElementValue(attackAttribute: Byte): Int {
		return stat!!.getAttackElementValue(attackAttribute)
	}

	open fun getDefenseElementValue(defenseAttribute: Byte): Int {
		return stat!!.getDefenseElementValue(defenseAttribute)
	}

	fun startPhysicalAttackMuted() {
		abortAttack()
	}

	fun stopPhysicalAttackMuted(effect: Abnormal?) {
		if (effect == null) {
			stopEffects(EffectType.PHYSICAL_ATTACK_MUTE)
		} else {
			removeEffect(effect)
		}
	}

	fun disableCoreAI(`val`: Boolean) {
		isCoreAIDisabled = `val`
	}

	/**
	 * Task for potion and herb queue
	 */
	private class UsePotionTask internal constructor(private val activeChar: Creature, private val skill: Skill) : Runnable {

		override fun run() {
			try {
				activeChar.doSimultaneousCast(skill)
			} catch (e: Exception) {
				log.warn("", e)
			}

		}
	}

	/**
	 * @return true
	 */
	open fun giveRaidCurse(): Boolean {
		return true
	}

	/**
	 * Check if target is affected with special buff
	 *
	 * @param flag long
	 * @return boolean
	 * @see CharEffectList.isAffected
	 */
	fun isAffected(flag: Long): Boolean {
		return effects.isAffected(flag)
	}

	fun setRefuseBuffs(refuses: Boolean) {
		isRefusingBuffs = refuses
	}

	open fun getMezMod(type: Int): Float {
		return 1.0f
	}

	open fun increaseMezResist(type: Int) {}

	fun getMezType(type: SkillType): Int {
		return -1
	}

	open fun getMezType(type: AbnormalType): Int {
		return -1
	}

	fun addSkillEffect(newSkill: Skill?) {
		if (newSkill != null) {
			// Add Func objects of newSkill to the calculator set of the Creature
			addStatFuncs(newSkill.getStatFuncs(this))

			if (newSkill.isChance) {
				addChanceTrigger(newSkill)
			}
		}
	}

	fun removeSkillEffect(skillId: Int, removeActiveEffect: Boolean): Skill? {
		// Remove the skill from the Creature skills
		val oldSkill = skills!![skillId]
		// Remove all its Func objects from the Creature calculator set
		if (oldSkill != null) {
			//this is just a fail-safe againts buggers and gm dummies...
			if (oldSkill.triggerAnotherSkill() && oldSkill.triggeredId > 0) {
				removeSkill(oldSkill.triggeredId, true)
			}

			// does not abort casting of the transformation dispell
			if (oldSkill.skillType !== SkillType.TRANSFORMDISPEL) {
				// Stop casting if this skill is used right now
				if (lastSkillCast != null && isCastingNow) {
					if (oldSkill.id == lastSkillCast!!.id) {
						abortCast()
					}
				}
				if (lastSimultaneousSkillCast != null && isCastingSimultaneouslyNow) {
					if (oldSkill.id == lastSimultaneousSkillCast!!.id) {
						abortCast()
					}
				}
			}

			// for now, to support transformations, we have to let their
			// effects stay when skill is removed
			if (removeActiveEffect) {
				val e = getFirstEffect(oldSkill)
				if (e == null || e.type !== AbnormalType.MUTATE) {
					removeStatsOwner(oldSkill)
					stopSkillEffects(oldSkill.id)
				}
			}

			if (oldSkill is SkillAgathion && this is Player && this.agathionId > 0) {
				this.agathionId = 0
				this.broadcastUserInfo()
			}

			if (oldSkill is SkillMount && this is Player && this.isMounted) {
				this.dismount()
			}

			if (oldSkill.isChance && chanceSkills != null) {
				removeChanceSkill(oldSkill.id)
			}
			if (oldSkill is SkillSummon && oldSkill.id == 710 && this is Player) {
				for (summon in this.summons) {
					if (summon.npcId == 14870) {
						summon.unSummon(this)
					}
				}
			}
		}

		return oldSkill
	}

	/**
	 * Note (ZaKaX)
	 * What's done:
	 * Party/Clan/Alliance/Arena/General/Sieges/Olympiads/Duels/Clan Wars FRIENDLY/UNFRIENDLY/ATTACK + CTRL Checks...
	 * TODO: Suck my d0ng & L2SiegeGuardInstance Checks.
	 *
	 * @param obj            (The targeted object)
	 * @param skill          (The used skill)
	 * @param isMassiveCheck (Define either this is supposed to be a massive or non massive attack)
	 */
	fun isAbleToCastOnTarget(obj: WorldObject, skill: Skill, isMassiveCheck: Boolean): Boolean {
		if (this is Playable || this is DecoyInstance || this is TrapInstance) {
			var activeChar: Player? = null
			var isPressingCtrl = false

			if (this is DecoyInstance) {
				activeChar = this.owner
			} else if (this is TrapInstance) {
				activeChar = this.owner
			} else {
				activeChar = actingPlayer
			}

			if (activeChar!!.currentSkill != null && activeChar.currentSkill.isCtrlPressed) {
				isPressingCtrl = true
			}

			val isInsideSiegeZone = activeChar.isInsideZone(CreatureZone.ZONE_SIEGE)

			if (obj is Playable) {
				val target = obj.actingPlayer
				if (activeChar.isPlayingEvent && !target!!.isPlayingEvent || !activeChar.isPlayingEvent && target!!.isPlayingEvent) {
					return false
				}

				// Do not check anything if its a chance skill, just fucking cast it motherfucker!
				if (skill.isChance) {
					return true
				}

				when (skill.skillBehavior) {
					SkillBehaviorType.FRIENDLY -> {
						if (obj.isDead() && !skill.isUseableOnDead) {
							return false
						} else if (!obj.isDead() && skill.isUseableOnDead) {
							return false
						}

						if (activeChar === target) {
							return true
						} else if (isMassiveCheck && activeChar.isInOlympiadMode) {
							if (!target!!.isInOlympiadMode) {
								return false
							}

							if (activeChar.party != null && target.party != null && activeChar.party !== target.party) {
								return false
							}
						}// Massive friendly skills affects only the caster during Olympiads.

						// TODO: InC & Exodus Events Checks...
						/*
						if (activeChar.isDreaming())
						{
							if (activeChar.getTeamId() != 0)
							{
								if (activeChar.getTeamId() != target.getTeamId())
									return false;
							}
							else if (activeChar != target)
								return false;

							return true;
						}*/

						if (activeChar.isPlayingEvent) {
							val event = activeChar.event
							if (event !== target!!.event) {
								return false
							}

							return if (event.config.isAllVsAll) {
								false
							} else event.getParticipantTeam(activeChar.objectId) === event.getParticipantTeam(target!!.objectId)

						}

						if (isInsideSiegeZone) {
							// Using resurrection skills is impossible, except in Fortress.
							if (skill.skillType === SkillType.RESURRECT) {
								if (activeChar.isInsideZone(CreatureZone.ZONE_CASTLE)) {
									if (activeChar.siegeState.toInt() == 2 && target!!.siegeState.toInt() == 2) {
										val s = CastleSiegeManager.getInstance().getSiege(x, y, z)

										if (s != null) {
											if (s.controlTowerCount > 0) {
												return true
											}
										}
									}

									return false
								} else if (!activeChar.isInsideZone(CreatureZone.ZONE_FORT)) {
									return false
								}
							}
						}

						if (isPressingCtrl) {
							return true
						} else {
							// You can't use friendly skills without ctrl if you are in duel but target isn't, or if target isn't in duel nor in the same duel as yours...
							if (activeChar.isInDuel && !target!!.isInDuel || activeChar.isInSameDuel(target)) {
								return false
							}
							// You can use friendly skills without ctrl if target is in same party/command channel...
							if (activeChar.isInSameParty(target) || activeChar.isInSameChannel(target!!)) {
								return true
							}
							// You can use friendly skills without ctrl on clan mates...
							if (activeChar.isInSameClan(target)) {
								return true
							}
							// You can use friendly skills without ctrl on ally mates...
							if (activeChar.isInSameAlly(target)) {
								return true
							}
							if (isInsideSiegeZone) {
								if (activeChar.isInSiege) {
									if (activeChar.isInSameSiegeSide(target)) {
										return true
									}
								} else {
									if (target.isInSiege) {
										return false
									}
								}
							}
							// You can't use friendly skills without ctrl while in an arena or a duel...
							if (target.isInsideZone(CreatureZone.ZONE_PVP) || target.isInDuel || target.isInOlympiadMode) {
								return false
							} else if (activeChar.isInSameClanWar(target)) {
								return false
							}// You can't use friendly skills without ctrl on clan wars...
							// You can't use friendly skills without ctrl if the target is in combat mode...
							if (target.isAvailableForCombat) {
								return false
							}
						}
					}
					SkillBehaviorType.UNFRIENDLY -> {
						// You can't debuff your summon at all, even while pressing CTRL.
						if (activeChar === target) {
							return false
						}

						if (activeChar.isPlayingEvent) {
							val event = activeChar.event
							if (event !== target!!.event) {
								return false
							}

							return if (event.config.isAllVsAll) {
								true
							} else event.getParticipantTeam(activeChar.objectId) !== event.getParticipantTeam(target!!.objectId)

						}

						if (activeChar.isInDuel) {
							if (!target!!.isInDuel) {
								return false
							} else if (activeChar.isInSameDuel(target)) {
								return true
							}
						}

						if (!target!!.isInOlympiadMode && target.pvpFlag.toInt() == 0) {
							if (activeChar.hasAwakaned()) {
								if (!target.hasAwakaned()) {
									return false
								}
							} else if (target.hasAwakaned()) {
								return false
							}

							if (target.level + 9 <= activeChar.level) {
								return false
							} else if (activeChar.level + 9 <= target.level) {
								return false
							}
						}

						if (activeChar.isInOlympiadMode) {
							if (!target.isInOlympiadMode) {
								return false
							} else if (activeChar.isInSameOlympiadGame(target)) {
								return true
							}
						} else if (target.isInOlympiadMode) {
							return false
						}

						// On retail, you can't debuff party members at all unless you're in duel.
						if (activeChar.isInSameParty(target) || activeChar.isInSameChannel(target)) {
							return false
						}

						// During Fortress/Castle Sieges, they can't debuff eachothers if they are in the same side.
						if (isInsideSiegeZone && activeChar.isInSiege && activeChar.isInSameSiegeSide(target)) {
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS))
							return false
						}

						// You can debuff anyone except party members while in an arena...
						if (!isInsideSiegeZone && isInsideZone(CreatureZone.ZONE_PVP) && target.isInsideZone(CreatureZone.ZONE_PVP)) {
							return true
						}

						// You can debuff anyone except party members while in clan war...
						if (activeChar.isInSameClanWar(target)) {
							return true
						}

						if (!isPressingCtrl) {
							/* On retail, it's not possible to debuff a clan member, except in Arena's. */
							if (activeChar.isInSameClan(target)) {
								return false
							} else if (activeChar.isInSameAlly(target)) {
								return false
							}/* On retail, it's not possible to debuff an aly member, except in Arena's. */
						}

						if (isInsideZone(CreatureZone.ZONE_PVP) && target.isInsideZone(CreatureZone.ZONE_PVP)) {
							return true
						}

						if (target.isInsideZone(CreatureZone.ZONE_PEACE)) {
							return false
						}

						/* On retail, it is impossible to debuff a "peaceful" player. */
						if (!target.isAvailableForCombat) {
							return false
						}
					}// TODO:
				// Skip if both players are in the same siege side...
					SkillBehaviorType.ATTACK -> {
						/* On retail, non-massive and massives attacks has different behaviors.*/
						if (isMassiveCheck) {
							// Checks for massives attacks...
							if (activeChar === target) {
								return false
							}

							if (activeChar.isPlayingEvent) {
								val event = activeChar.event
								if (event !== target!!.event) {
									return false
								}

								return if (event.config.isAllVsAll) {
									true
								} else event.getParticipantTeam(activeChar.objectId) !== event.getParticipantTeam(target!!.objectId)

							}

							if (activeChar.isInDuel) {
								if (!target!!.isInDuel) {
									return false
								} else if (activeChar.isInSameDuel(target)) {
									return true
								}
							}

							if (!target!!.isInOlympiadMode && target.pvpFlag.toInt() == 0) {
								if (activeChar.hasAwakaned()) {
									if (!target.hasAwakaned()) {
										return false
									}
								} else if (target.hasAwakaned()) {
									return false
								}

								if (target.level + 9 <= activeChar.level) {
									return false
								} else if (activeChar.level + 9 <= target.level) {
									return false
								}
							}

							if (target.isInsideZone(CreatureZone.ZONE_PEACE)) {
								return false
							}

							if (activeChar.isInSameParty(target) || activeChar.isInSameChannel(target)) {
								return false
							}

							if (activeChar.isInOlympiadMode) {
								if (!target.isInOlympiadMode) {
									return false
								} else if (activeChar.isInSameOlympiadGame(target)) {
									return true
								}
							} else if (target.isInOlympiadMode) {
								return false
							}

							// During Fortress/Castle Sieges, they can't attack eachothers with massive attacks if they are in the same side.
							// TODO: Needs to be verified on retail.
							if (isInsideSiegeZone && activeChar.isInSiege && activeChar.isInSameSiegeSide(target)) {
								return false
							}

							if (!isInsideSiegeZone && isInsideZone(CreatureZone.ZONE_PVP) && target.isInsideZone(CreatureZone.ZONE_PVP)) {
								return true
							}

							/*
                              On retail, there's no way to affect a clan member with a massive attack.
                              Unless you are in an arena.
                             */
							if (activeChar.isInSameClan(target)) {
								return false
							} else if (activeChar.isInSameAlly(target)) {
								return false
							}/*
                              On retail, there's no way to affect an ally member with a massive attack.
                              Unless you are in an arena.
                             */

							if (isInsideZone(CreatureZone.ZONE_PVP) && target.isInsideZone(CreatureZone.ZONE_PVP)) {
								return true
							}

							if (activeChar.isInSameClanWar(target)) {
								return true
							}

							/*
                              On retail, there's no way to affect a "peaceful" player with massive attacks.
                              Even if CTRL is pressed...
                             */
							if (!target.isAvailableForCombat) {
								return false
							}
						} else {
							// Checks for non-massives attacks...
							/* It is impossible to affect a player that isn't participating in the same duel.**/
							if (activeChar.isInDuel) {
								// On retail, you can attack a target that's not in your duel with single target skills attacks unless you press CTRL.
								if (!target!!.isInDuel && !isPressingCtrl) {
									return false
								} else if (activeChar.isInSameDuel(target)) {
									return true
								}// If both are in the same duel, don't check any more condition - return true.
							}

							if (activeChar.isPlayingEvent) {
								val event = activeChar.event
								if (event !== target!!.event) {
									return false
								}

								return if (event.config.isAllVsAll) {
									true
								} else event.getParticipantTeam(activeChar.objectId) !== event.getParticipantTeam(target!!.objectId)

							}

							if (isPressingCtrl) {
								if (target === activeChar) {
									return true
								}
							}

							if (target!!.isInsideZone(CreatureZone.ZONE_PEACE)) {
								return false
							}

							if (!isPressingCtrl) {
								if (activeChar === target) {
									return false
								} else if (activeChar.isInSameParty(target) || activeChar.isInSameChannel(target)) {
									return false
								}
								if (isInsideSiegeZone && activeChar.isInSiege && activeChar.isInSameSiegeSide(target)) {
									activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS))
									return false
								}
								if (target.isInsideZone(CreatureZone.ZONE_PVP)) {
									return true
								} else if (activeChar.isInSameClan(target) || activeChar.isInSameAlly(target)) {
									return false
								} else if (activeChar.isInSameClanWar(target)) {
									return true
								}
								if (!target.isAvailableForCombat) {
									return false
								}
							}
						}
					}
				}
			} else if (obj is DoorInstance) {
				val cDoor = obj.castle
				val fDoor = obj.fort

				when (skill.skillBehavior) {
					SkillBehaviorType.FRIENDLY -> return true
					SkillBehaviorType.UNFRIENDLY -> return false
					SkillBehaviorType.ATTACK -> {
						if (obj.isInsideZone(CreatureZone.ZONE_PEACE)) {
							return false
						}
						if (cDoor != null) {
							/* Maybe we need checks to see if the character is attacker? **/
							return cDoor.siege.isInProgress
						} else if (fDoor != null) {
							/* Maybe we need checks to see if the character is attacker? **/
							/*if (fDoor.getSiege().getIsInProgress() && door.getIsCommanderDoor()) FIXME
								return true;
							else
								return false;*/
						} else {
							return false
						}
					}
				}
			}/*
				else if (obj instanceof L2TerritoryWardInstance)
				{
				final L2TerritoryWardInstance ward = ((L2TerritoryWardInstance) obj);

				if (activeChar.getSiegeSide() != 0 && TerritoryWarManager.getInstance().isAllyField(activeChar, ward.getCastle().getCastleId()))
					return false;

				return true;
				}
				else if (obj instanceof L2SiegeGuardInstance)
				{
				// final L2SiegeGuardInstance aGuard = ((L2SiegeGuardInstance) obj);
				// TODO
				}*/
			else if (obj is MonsterInstance) {
				// Impossible to interact on monsters while in a duel...
				if (activeChar.duelId != 0 && !isPressingCtrl) {
					return false
				}

				if (skill.skillBehavior === SkillBehaviorType.FRIENDLY) {
					if (!isPressingCtrl) {
						return false
					}
				}

				// TODO: Maybe monsters friendly check here? Like Ketra/Varka Alliance...
				// I think they can't be attacked/debuffed unless you press CTRL on Retail.
			} else if (obj is NpcInstance || obj is GuardInstance) {
				// Impossible to interact on npcs/guards while in a duel...
				if (activeChar.duelId != 0 && !isPressingCtrl) {
					return false
				}

				val skillBehavior = skill.skillBehavior

				when (skillBehavior) {
				// On retail, you can't debuff npcs at all.
					SkillBehaviorType.UNFRIENDLY -> return false
					SkillBehaviorType.ATTACK -> {
						if (isMassiveCheck) {
							return false
						} else {
							if (!isPressingCtrl) {
								return false
							}
						}
					}
				}
			}
		} else if (this is Attackable) {
			if (obj !is Playable) {
				return false
			}
		}

		return true
	}

	companion object {
		private val log = LoggerFactory.getLogger(Creature::class.java.name)

		/**
		 * Table of calculators containing all standard NPC calculator (ex : ACCURACY_COMBAT, EVASION_RATE
		 */
		private val NPC_STD_CALCULATOR: Array<Calculator?>

		init {
			NPC_STD_CALCULATOR = Formulas.getStdNPCCalculators()
		}
	}
}
