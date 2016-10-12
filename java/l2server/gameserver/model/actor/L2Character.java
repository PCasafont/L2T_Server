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

package l2server.gameserver.model.actor;

import l2server.Config;
import l2server.gameserver.GeoData;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.TimeController;
import l2server.gameserver.ai.CtrlEvent;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.ai.L2AttackableAI;
import l2server.gameserver.ai.L2CharacterAI;
import l2server.gameserver.datatables.DoorTable;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.datatables.MapRegionTable.TeleportWhereType;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.events.Curfew;
import l2server.gameserver.events.instanced.EventInstance;
import l2server.gameserver.events.instanced.EventInstance.EventType;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.handler.SkillHandler;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.PlayerAssistsManager;
import l2server.gameserver.instancemanager.SiegeManager;
import l2server.gameserver.instancemanager.TownManager;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.instance.*;
import l2server.gameserver.model.actor.instance.L2PcInstance.SkillDat;
import l2server.gameserver.model.actor.knownlist.CharKnownList;
import l2server.gameserver.model.actor.position.CharPosition;
import l2server.gameserver.model.actor.stat.CharStat;
import l2server.gameserver.model.actor.status.CharStatus;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.entity.Fort;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.model.entity.Siege;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.network.serverpackets.FlyToLocation.FlyType;
import l2server.gameserver.network.serverpackets.StatusUpdate.StatusUpdateDisplay;
import l2server.gameserver.pathfinding.AbstractNodeLoc;
import l2server.gameserver.pathfinding.PathFinding;
import l2server.gameserver.stats.Calculator;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.stats.VisualEffect;
import l2server.gameserver.stats.effects.EffectChanceSkillTrigger;
import l2server.gameserver.stats.effects.EffectSpatialTrap;
import l2server.gameserver.stats.funcs.Func;
import l2server.gameserver.stats.skills.L2SkillAgathion;
import l2server.gameserver.stats.skills.L2SkillMount;
import l2server.gameserver.stats.skills.L2SkillSummon;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;
import l2server.gameserver.templates.chars.L2CharTemplate;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.templates.item.L2Weapon;
import l2server.gameserver.templates.item.L2WeaponType;
import l2server.gameserver.templates.skills.*;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Point3D;
import l2server.util.Rnd;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.logging.Level;

import static l2server.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

/**
 * Mother class of all character objects of the world (PC, NPC...)<BR><BR>
 * <p>
 * L2Character :<BR><BR>
 * <li>L2CastleGuardInstance</li>
 * <li>L2DoorInstance</li>
 * <li>L2NpcInstance</li>
 * <li>L2PlayableInstance </li><BR><BR>
 * <p>
 * <p>
 * <B><U> Concept of L2CharTemplate</U> :</B><BR><BR>
 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...).
 * All of those properties are stored in a different template for each type of L2Character.
 * Each template is loaded once in the server cache memory (reduce memory use).
 * When a new instance of L2Character is spawned, server just create a link between the instance and the template.
 * This link is stored in <B>_template</B><BR><BR>
 *
 * @version $Revision: 1.53.2.45.2.34 $ $Date: 2005/04/11 10:06:08 $
 */
public abstract class L2Character extends L2Object
{
	// =========================================================
	// Data Field
	private Set<L2Character> _attackByList;
	private volatile boolean _isCastingNow = false;
	private volatile boolean _isCastingNow2 = false;
	private volatile boolean _isCastingSimultaneouslyNow = false;
	private L2Skill _lastSkillCast;
	private L2Skill _lastSimultaneousSkillCast;

	private boolean _isDead = false;
	private boolean _isImmobilized = false;
	private boolean _isOverloaded = false; // the char is carrying too much
	private boolean _isParalyzed = false;
	private boolean _isPendingRevive = false;
	private boolean _isRunning = false;
	private boolean _refuseBuffs = false; // Tenkai custom - refuse buffs from out of party
	protected boolean _showSummonAnimation = false;
	protected boolean _isTeleporting = false;
	protected boolean _isInvul = false;
	private boolean _isMortal = true; // Char will die when HP decreased to 0
	private boolean _isFlying = false;

	private CharStat _stat;
	private CharStatus _status;
	private L2CharTemplate _template;
	// The link on the L2CharTemplate object containing generic and static properties of this L2Character type (ex : Max HP, Speed...)
	private String _title;
	private double _hpUpdateIncCheck = .0;
	private double _hpUpdateDecCheck = .0;
	private double _hpUpdateInterval = .0;

	/**
	 * Table of Calculators containing all used calculator
	 */
	private Calculator[] _calculators;

	/**
	 * HashMap(Integer, L2Skill) containing all skills of the L2Character
	 */
	protected final Map<Integer, L2Skill> _skills;
	/**
	 * HashMap containing the active chance skills on this character
	 */
	private ChanceSkillList _chanceSkills;

	/**
	 * Current force buff this caster is casting to a target
	 */
	protected FusionSkill _fusionSkill;

	protected Point3D _skillCastPosition;

	/**
	 * Zone system
	 */
	public static final byte ZONE_PVP = 0;
	public static final byte ZONE_PEACE = 1;
	public static final byte ZONE_SIEGE = 2;
	public static final byte ZONE_MOTHERTREE = 3;
	public static final byte ZONE_CLANHALL = 4;
	public static final byte ZONE_LANDING = 5;
	public static final byte ZONE_NOLANDING = 6;
	public static final byte ZONE_WATER = 7;
	public static final byte ZONE_JAIL = 8;
	public static final byte ZONE_MONSTERTRACK = 9;
	public static final byte ZONE_CASTLE = 10;
	public static final byte ZONE_SWAMP = 11;
	public static final byte ZONE_NOSUMMONFRIEND = 12;
	public static final byte ZONE_FORT = 13;
	public static final byte ZONE_NOSTORE = 14;
	public static final byte ZONE_TOWN = 15;
	public static final byte ZONE_SCRIPT = 16;
	public static final byte ZONE_NOHQ = 17;
	public static final byte ZONE_DANGERAREA = 18;
	public static final byte ZONE_ALTERED = 19;
	public static final byte ZONE_NOBOOKMARK = 20;
	public static final byte ZONE_NOITEMDROP = 21;
	public static final byte ZONE_NORESTART = 22;
	public static final byte ZONE_FLY = 23;

	private final byte[] _zones = new byte[25];
	protected byte _zoneValidateCounter = 4;

	protected L2Character _debugger = null;

	/**
	 * @return True if debugging is enabled for this L2Character
	 */
	public boolean isDebug()
	{
		return _debugger != null;
	}

	/**
	 * Sets L2Character instance, to which debug packets will be send
	 *
	 * @param d
	 */
	public void setDebug(L2Character d)
	{
		_debugger = d;
	}

	/**
	 * Send debug packet.
	 *
	 * @param pkt
	 */
	public void sendDebugPacket(L2GameServerPacket pkt)
	{
		if (_debugger != null)
		{
			_debugger.sendPacket(pkt);
		}
	}

	/**
	 * Send debug text string
	 *
	 * @param msg The message to send
	 */
	public void sendDebugMessage(String msg)
	{
		if (_debugger != null)
		{
			_debugger.sendMessage(msg);
		}
	}

	/**
	 * Returns character inventory, default null, overridden in L2Playable types and in L2NPcInstance
	 */
	public Inventory getInventory()
	{
		return null;
	}

	public boolean destroyItemByItemId(String process, int itemId, long count, L2Object reference, boolean sendMessage)
	{
		// Default: NPCs consume virtual items for their skills
		// TODO: should be logged if even happens.. should be false
		return true;
	}

	public boolean destroyItem(String process, int objectId, long count, L2Object reference, boolean sendMessage)
	{
		// Default: NPCs consume virtual items for their skills
		// TODO: should be logged if even happens.. should be false
		return true;
	}

	/**
	 * @param zone
	 * @return
	 */
	public final boolean isInsideZone(final byte zone)
	{
		Instance instance = InstanceManager.getInstance().getInstance(getInstanceId());
		switch (zone)
		{
			case ZONE_PVP:
				if (instance != null && instance.isPvPInstance() || Curfew.getInstance().getOnlyPeaceTown() != -1 ||
						getActingPlayer() != null && getActingPlayer().isPlayingEvent())
				{
					return true; //_zones[ZONE_PEACE] == 0;
				}
				return _zones[ZONE_PVP] > 0 && _zones[ZONE_PEACE] == 0;
			case ZONE_PEACE:
				if (instance != null && instance.isPvPInstance() ||
						getActingPlayer() != null && getActingPlayer().isPlayingEvent())
				{
					return false;
				}
		}
		return _zones[zone] > 0;
	}

	/**
	 * @param zone
	 * @param state
	 */
	public final void setInsideZone(final byte zone, final boolean state)
	{
		if (state)
		{
			_zones[zone]++;
		}
		else
		{
			_zones[zone]--;
			if (_zones[zone] < 0)
			{
				_zones[zone] = 0;
			}
		}
	}

	/**
	 * This will return true if the player is transformed,<br>
	 * but if the player is not transformed it will return false.
	 *
	 * @return transformation status
	 */
	public boolean isTransformed()
	{
		return false;
	}

	/**
	 * This will untransform a player if they are an instance of L2Pcinstance
	 * and if they are transformed.
	 *
	 * @return untransform
	 */
	public void unTransform(boolean removeEffects)
	{
		// Just a place holder
	}

	/**
	 * This will return true if the player is GM,<br>
	 * but if the player is not GM it will return false.
	 *
	 * @return GM status
	 */
	public boolean isGM()
	{
		return false;
	}

	// =========================================================
	// Constructor

	/**
	 * Constructor of L2Character.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...).
	 * All of those properties are stored in a different template for each type of L2Character.
	 * Each template is loaded once in the server cache memory (reduce memory use).
	 * When a new instance of L2Character is spawned, server just create a link between the instance and the template
	 * This link is stored in <B>_template</B><BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the _template of the L2Character </li>
	 * <li>Set _overloaded to false (the charcater can take more items)</li><BR><BR>
	 * <p>
	 * <li>If L2Character is a L2NPCInstance, copy skills from template to object</li>
	 * <li>If L2Character is a L2NPCInstance, link _calculators to NPC_STD_CALCULATOR</li><BR><BR>
	 * <p>
	 * <li>If L2Character is NOT a L2NPCInstance, create an empty _skills slot</li>
	 * <li>If L2Character is a L2PcInstance or L2Summon, copy basic Calculator set to object</li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2CharTemplate to apply to the object
	 */
	public L2Character(int objectId, L2CharTemplate template)
	{
		super(objectId);
		setInstanceType(InstanceType.L2Character);
		initCharStat();
		initCharStatus();

		// Set its template to the new L2Character
		_template = template;

		if (this instanceof L2DoorInstance || this instanceof L2Npc)
		{
			// Copy the Standard Calcultors of the L2NPCInstance in _calculators
			_calculators = NPC_STD_CALCULATOR;
		}
		else
		{
			// If L2Character is a L2PcInstance or a L2Summon, create the basic calculator set
			_calculators = new Calculator[Stats.NUM_STATS];
			Formulas.addFuncsToNewCharacter(this);
		}

		_skills = new ConcurrentHashMap<>();
		if (template != null && (this instanceof L2Npc || this instanceof L2Summon))
		{
			// Copy the skills of the NPC from its template to the L2Character Instance
			// The skills list can be affected by spell effects so it's necessary to make a copy
			// to avoid that a spell affecting a L2NPCInstance, affects others L2NPCInstance of the same type too.
			if (((L2NpcTemplate) template).getSkills() != null)
			{
				for (L2Skill skill : ((L2NpcTemplate) template).getSkills().values())
				{
					addSkill(skill);
				}
			}
		}

		setIsInvul(true);
	}

	protected void initCharStatusUpdateValues()
	{
		_hpUpdateIncCheck = getMaxVisibleHp();
		_hpUpdateInterval = _hpUpdateIncCheck / 352.0; // MAX_HP div MAX_HP_BAR_PX
		_hpUpdateDecCheck = _hpUpdateIncCheck - _hpUpdateInterval;
	}

	// =========================================================
	// Event - Public

	/**
	 * Remove the L2Character from the world when the decay task is launched.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World </B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR><BR>
	 */
	public void onDecay()
	{
		L2WorldRegion reg = getWorldRegion();
		decayMe();
		if (reg != null)
		{
			reg.removeFromZones(this);
		}
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		revalidateZone(true);
	}

	public void onTeleported()
	{
		if (!isTeleporting())
		{
			return;
		}

		spawnMe(getPosition().getX(), getPosition().getY(), getPosition().getZ());

		setIsTeleporting(false);

		if (_isPendingRevive)
		{
			doRevive();
		}
	}

	// =========================================================
	// Method - Public

	/**
	 * Add L2Character instance that is attacking to the attacker list.<BR><BR>
	 *
	 * @param player The L2Character that attacks this one
	 *               <p>
	 *               <B><U> Overridden in </U> :</B><BR><BR>
	 *               <li> L2Attackable : Add to list only for attackables, not all other NPCs</li><BR><BR>
	 */
	public void addAttackerToAttackByList(L2Character player)
	{
		// DS: moved to L2Attackable
	}

	/**
	 * Send a packet to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
	 */
	public void broadcastPacket(L2GameServerPacket mov)
	{
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		//synchronized (getKnownList().getKnownPlayers())
		{
			for (L2PcInstance player : plrs)
			{
				if (player != null)
				{
					player.sendPacket(mov);
				}
			}
		}
	}

	/**
	 * Send a packet to the L2Character AND to all L2PcInstance in the radius (max knownlist radius) from the L2Character.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
	 */
	public void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist)
	{
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		//synchronized (getKnownList().getKnownPlayers())
		{
			for (L2PcInstance player : plrs)
			{
				if (player != null && isInsideRadius(player, radiusInKnownlist, false, false))
				{
					player.sendPacket(mov);
				}
			}
		}
	}

	/**
	 * Returns true if hp update should be done, false if not
	 *
	 * @return boolean
	 */
	protected boolean needHpUpdate(int barPixels)
	{
		double currentHp = getCurrentHp();
		double maxHp = getMaxVisibleHp();

		if (currentHp <= 1.0 || maxHp < barPixels)
		{
			return true;
		}

		if (currentHp <= _hpUpdateDecCheck || currentHp >= _hpUpdateIncCheck)
		{
			if (currentHp == maxHp)
			{
				_hpUpdateIncCheck = currentHp + 1;
				_hpUpdateDecCheck = currentHp - _hpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentHp / _hpUpdateInterval;
				int intMulti = (int) doubleMulti;

				_hpUpdateDecCheck = _hpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_hpUpdateIncCheck = _hpUpdateDecCheck + _hpUpdateInterval;
			}

			return true;
		}

		return false;
	}

	public void broadcastStatusUpdate()
	{
		broadcastStatusUpdate(null, StatusUpdateDisplay.NONE);
	}

	/**
	 * Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Create the Server->Client packet StatusUpdate with current HP and MP </li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all
	 * L2Character called _statusListener that must be informed of HP/MP updates of this L2Character </li><BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND CP information</B></FONT><BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Send current HP,MP and CP to the L2PcInstance and only current HP, MP and Level to all other L2PcInstance of the Party</li><BR><BR>
	 */
	public void broadcastStatusUpdate(L2Character causer, StatusUpdateDisplay display)
	{
		if (this instanceof L2Summon)
		{
			return;
		}

		if (getStatus().getStatusListener().isEmpty() && getKnownList().getKnownPlayersInRadius(200).isEmpty())
		{
			return;
		}

		if (display == StatusUpdateDisplay.NONE && !needHpUpdate(352))
		{
			return;
		}

		if (Config.DEBUG)
		{
			Log.fine("Broadcast Status Update for " + getObjectId() + " (" + getName() + "). HP: " + getCurrentHp());
		}

		// Create the Server->Client packet StatusUpdate with current HP
		StatusUpdate su = new StatusUpdate(this, causer, display);
		su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());

		// Go through the StatusListener
		// Send the Server->Client packet StatusUpdate with current HP and MP
		if (this instanceof L2Attackable)
		{
			for (L2PcInstance temp : getKnownList().getKnownPlayersInRadius(600))
			{
				if (temp != null)
				{
					temp.sendPacket(su);
				}
			}
		}

		for (L2Character temp : getStatus().getStatusListener())
		{
			if (temp != null && !temp.isInsideRadius(this, 600, false, false))
			{
				temp.sendPacket(su);
			}
		}
	}

	public void broadcastAbnormalStatusUpdate()
	{
		if (getStatus().getStatusListener().isEmpty())
		{
			return;
		}

		// Create the Server->Client packet AbnormalStatusUpdate
		AbnormalStatusUpdateFromTarget asu = new AbnormalStatusUpdateFromTarget(this);

		// Go through the StatusListener
		// Send the Server->Client packet StatusUpdate with current HP and MP

		for (L2Character temp : getStatus().getStatusListener())
		{
			if (temp != null)
			{
				temp.sendPacket(asu);
			}
		}
	}

	/**
	 * Not Implemented.<BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 */
	public void sendMessage(String text)
	{
		// default implementation
	}

	/**
	 * Teleport a L2Character and its pet if necessary.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop the movement of the L2Character</li>
	 * <li>Set the x,y,z position of the L2Object and if necessary modify its _worldRegion</li>
	 * <li>Send a Server->Client packet TeleportToLocationt to the L2Character AND to all L2PcInstance in its _KnownPlayers</li>
	 * <li>Modify the position of the pet if necessary</li><BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 */
	public void teleToLocation(int x, int y, int z, int heading, boolean allowRandomOffset)
	{
		// Stop movement
		stopMove(null, false);
		abortAttack();
		abortCast();

		setIsTeleporting(true);
		setTarget(null);

		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

		if (Config.OFFSET_ON_TELEPORT_ENABLED && allowRandomOffset)
		{
			x += Rnd.get(-Config.MAX_OFFSET_ON_TELEPORT, Config.MAX_OFFSET_ON_TELEPORT);
			y += Rnd.get(-Config.MAX_OFFSET_ON_TELEPORT, Config.MAX_OFFSET_ON_TELEPORT);
		}

		z += 5;

		if (Config.DEBUG)
		{
			Log.fine("Teleporting to: " + x + ", " + y + ", " + z);
		}

		// Send a Server->Client packet TeleportToLocationt to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		broadcastPacket(new TeleportToLocation(this, x, y, z, heading));
		sendPacket(new ExTeleportToLocationActivate(getObjectId(), x, y, z, heading));

		// remove the object from its old location
		decayMe();

		// Set the x,y,z position of the L2Object and if necessary modify its _worldRegion
		getPosition().setXYZ(x, y, z);

		// temporary fix for heading on teleports
		if (heading != 0)
		{
			getPosition().setHeading(heading);
		}

		// allow recall of the detached characters
		if (!(this instanceof L2PcInstance) ||
				((L2PcInstance) this).getClient() != null && ((L2PcInstance) this).getClient().isDetached() ||
				this instanceof L2ApInstance)
		{
			onTeleported();
		}

		revalidateZone(true);
	}

	public void teleToLocation(int x, int y, int z)
	{
		teleToLocation(x, y, z, getHeading(), false);
	}

	public void teleToLocation(int x, int y, int z, boolean allowRandomOffset)
	{
		teleToLocation(x, y, z, getHeading(), allowRandomOffset);
	}

	public void teleToLocation(Location loc, boolean allowRandomOffset)
	{
		int x = loc.getX();
		int y = loc.getY();
		int z = loc.getZ();
		teleToLocation(x, y, z, getHeading(), allowRandomOffset);
	}

	public void teleToLocation(TeleportWhereType teleportWhere)
	{
		teleToLocation(MapRegionTable.getInstance().getTeleToLocation(this, teleportWhere), true);
	}

	// =========================================================
	// Method - Private

	/**
	 * Launch a physical attack against a target (Simple, Bow, Pole or Dual).<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the active weapon (always equiped in the right hand) </li><BR><BR>
	 * <li>If weapon is a bow, check for arrows, MP and bow re-use delay (if necessary, equip the L2PcInstance with arrows in left hand)</li>
	 * <li>If weapon is a bow, consume MP and set the new period of bow non re-use </li><BR><BR>
	 * <li>Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack) </li>
	 * <li>Select the type of attack to start (Simple, Bow, Pole or Dual) and verify if SoulShot are charged then start calculation</li>
	 * <li>If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character</li>
	 * <li>Notify AI with EVT_READY_TO_ACT</li><BR><BR>
	 *
	 * @param target The L2Character targeted
	 */
	protected void doAttack(L2Character target)
	{
		if (Config.DEBUG)
		{
			Log.fine(getName() + " doAttack: target=" + target);
		}

		if (!isAlikeDead() && target != null)
		{
			if (this instanceof L2Npc && target.isAlikeDead() || !getKnownList().knowsObject(target))
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			else if (this instanceof L2PcInstance)
			{
				if (target.isDead())
				{
					getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}

				L2PcInstance actor = (L2PcInstance) this;
				/*
                 * Players riding wyvern or with special (flying) transformations can't do melee attacks, only with skills
				 */
				if (actor.isMounted() && actor.getMountNpcId() == 12621 ||
						actor.isTransformed() && !actor.getTransformation().canDoMeleeAttack())
				{
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}

		if (isAttackingDisabled())
		{
			return;
		}

		if (this instanceof L2PcInstance)
		{
			final L2PcInstance player = (L2PcInstance) this;

			if (player.inObserverMode())
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE));
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (target.getActingPlayer() != null && player.getSiegeState() > 0 &&
					isInsideZone(L2Character.ZONE_SIEGE) &&
					target.getActingPlayer().getSiegeState() == player.getSiegeState() &&
					target.getActingPlayer() != this &&
					target.getActingPlayer().getSiegeSide() == player.getSiegeSide() && !Config.isServer(Config.TENKAI))
			{
				sendPacket(SystemMessage.getSystemMessage(
						SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS));
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			// Checking if target has moved to peace zone
			if (target.isInsidePeaceZone((L2PcInstance) this))
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			// TODO: unhardcode this to support boolean if with that weapon u can attack or not (for ex transform weapons)
			if (player.getActiveWeaponItem() != null && player.getActiveWeaponItem().getItemId() == 9819)
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THAT_WEAPON_CANT_ATTACK));
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		else if (isInsidePeaceZone(this, target))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		stopEffectsOnAction(null);

		// Get the active weapon instance (always equiped in the right hand)
		L2ItemInstance weaponInst = getActiveWeaponInstance();

		// Get the active weapon item corresponding to the active weapon instance (always equiped in the right hand)
		L2Weapon weaponItem = getActiveWeaponItem();

		if (weaponItem != null && weaponItem.getItemType() == L2WeaponType.FISHINGROD)
		{
			//	You can't make an attack with a fishing pole.
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_ATTACK_WITH_FISHING_POLE));
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// GeoData Los Check here (or dz > 1000)
		if (!GeoData.getInstance().canSeeTarget(this, target))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SEE_TARGET));
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// BOW and CROSSBOW checks
		if (weaponItem != null && !isTransformed())
		{
			if (weaponItem.getItemType() == L2WeaponType.BOW)
			{
				//Check for arrows and MP
				if (this instanceof L2PcInstance)
				{
					// Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True
					if (!checkAndEquipArrows())
					{
						// Cancel the action because the L2PcInstance have no arrow
						getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						sendPacket(ActionFailed.STATIC_PACKET);
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ARROWS));
						return;
					}

					// Verify if the bow can be use
					if (_disableBowAttackEndTime <= TimeController.getGameTicks())
					{
						// Verify if L2PcInstance owns enough MP
						int saMpConsume = (int) getStat().calcStat(Stats.MP_CONSUME, 0, null, null);
						int mpConsume = saMpConsume == 0 ? weaponItem.getMpConsume() : saMpConsume;
						mpConsume = (int) calcStat(Stats.BOW_MP_CONSUME_RATE, mpConsume, null, null);

						if (getCurrentMp() < mpConsume)
						{
							// If L2PcInstance doesn't have enough MP, stop the attack
							ThreadPoolManager.getInstance()
									.scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
							sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_MP));
							sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
						// If L2PcInstance have enough MP, the bow consumes it
						if (mpConsume > 0)
						{
							getStatus().reduceMp(mpConsume);
						}

						// Set the period of bow no re-use
						_disableBowAttackEndTime = 5 * TimeController.TICKS_PER_SECOND + TimeController.getGameTicks();
					}
					else
					{
						// Cancel the action because the bow can't be re-use at this moment
						ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);

						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
				}
			}
			if (weaponItem.getItemType() == L2WeaponType.CROSSBOW || weaponItem.getItemType() == L2WeaponType.CROSSBOWK)
			{
				//Check for bolts
				if (this instanceof L2PcInstance)
				{
					// Checking if target has moved to peace zone - only for player-crossbow attacks at the moment
					// Other melee is checked in movement code and for offensive spells a check is done every time
					if (target.isInsidePeaceZone((L2PcInstance) this))
					{
						getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}

					// Equip bolts needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True
					if (!checkAndEquipBolts())
					{
						// Cancel the action because the L2PcInstance have no arrow
						getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						sendPacket(ActionFailed.STATIC_PACKET);
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_BOLTS));
						return;
					}

					// Verify if the crossbow can be use
					if (_disableCrossBowAttackEndTime <= TimeController.getGameTicks())
					{
						// Set the period of crossbow no re-use
						_disableCrossBowAttackEndTime =
								5 * TimeController.TICKS_PER_SECOND + TimeController.getGameTicks();
					}
					else
					{
						// Cancel the action because the crossbow can't be re-use at this moment
						ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
				}
				else if (this instanceof L2Npc)
				{
					if (_disableCrossBowAttackEndTime > TimeController.getGameTicks())
					{
						return;
					}
				}
			}
		}

		// Add the L2PcInstance to _knownObjects and _knownPlayer of the target
		target.getKnownList().addKnownObject(this);

		// Reduce the current CP if TIREDNESS configuration is activated
		if (Config.ALT_GAME_TIREDNESS)
		{
			setCurrentCp(getCurrentCp() - 10);
		}

		if (this instanceof L2PcInstance)
		{
			final L2PcInstance player = (L2PcInstance) this;

			// Recharge any active auto soulshot tasks for player (or player's summon if one exists).
			player.rechargeAutoSoulShot(true, false, false);
		}
		else if (this instanceof L2Summon)
		{
			((L2Summon) this).getOwner().rechargeAutoSoulShot(true, false, true);
		}

		// Verify if soulshots are charged.
		double ssCharge = L2ItemInstance.CHARGED_NONE;
		if (weaponInst != null)
		{
			ssCharge = weaponInst.getChargedSoulShot();
		}
		else if (this instanceof L2Summon && !(this instanceof L2PetInstance))
		{
			ssCharge = ((L2Summon) this).getChargedSoulShot();
		}

		if (this instanceof L2Attackable)
		{
			if (((L2Npc) this).useSoulShot())
			{
				ssCharge = L2ItemInstance.CHARGED_SOULSHOT;
			}
		}

		// Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack)
		int timeAtk = calculateTimeBetweenAttacks(target, weaponItem);
		// the hit is calculated to happen halfway to the animation - might need further tuning e.g. in bow case
		int timeToHit = timeAtk / 2;
		_attackEndTime = TimeController.getGameTicks();
		_attackEndTime += timeAtk / TimeController.MILLIS_IN_TICK;
		_attackEndTime -= 1;

		int ssGrade = 0;
		if (weaponItem != null)
		{
			ssGrade = weaponItem.getItemGradePlain();
		}

		// Create a Server->Client packet Attack
		Attack attack = new Attack(this, target, ssCharge, ssGrade);

		// Set the Attacking Body part to CHEST
		setAttackingBodypart();
		// Make sure that char is facing selected target
		// also works: setHeading(Util.convertDegreeToClientHeading(Util.calculateAngleFrom(this, target)));
		setHeading(Util.calculateHeadingFrom(this, target));

		// Get the Attack Reuse Delay of the L2Weapon
		int reuse = calculateReuseTime(target, weaponItem);
		boolean hitted;
		// Select the type of attack to start
		if (weaponItem == null || isTransformed())
		{
			hitted = doAttackHitSimple(attack, target, timeToHit);
		}
		else if (calcStat(Stats.ATTACK_COUNT_MAX, 1, null, null) - 1 > 0 ||
				weaponItem.getItemType() == L2WeaponType.POLE)
		{
			hitted = doAttackHitByPole(attack, target, timeToHit);
		}
		else if (weaponItem.getItemType() == L2WeaponType.BOW)
		{
			hitted = doAttackHitByBow(attack, target, timeAtk, reuse);
		}
		else if (weaponItem.getItemType() == L2WeaponType.CROSSBOW ||
				weaponItem.getItemType() == L2WeaponType.CROSSBOWK)
		{
			hitted = doAttackHitByCrossBow(attack, target, timeAtk, reuse);
		}
		else if (isUsingDualWeapon())
		{
			hitted = doAttackHitByDual(attack, target, timeToHit);
		}
		else
		{
			hitted = doAttackHitSimple(attack, target, timeToHit);
		}

		// Flag the attacker if it's a L2PcInstance outside a PvP area
		L2PcInstance player = getActingPlayer();

		if (player != null)
		{
			AttackStanceTaskManager.getInstance().addAttackStanceTask(player);

			boolean targetIsSummon = false;
			for (L2SummonInstance summon : player.getSummons())
			{
				if (summon == target)
				{
					targetIsSummon = true;
				}
			}
			if (!targetIsSummon && player.getPet() != target)
			{
				player.updatePvPStatus(target);
			}
		}
		// Check if hit isn't missed
		if (!hitted)
		// Abort the attack of the L2Character and send Server->Client ActionFailed packet
		{
			abortAttack();
		}
		else
		{
            /* ADDED BY nexus - 2006-08-17
			 *
			 * As soon as we know that our hit landed, we must discharge any active soulshots.
			 * This must be done so to avoid unwanted soulshot consumption.
			 */

			// If we didn't miss the hit, discharge the shoulshots, if any
			if (this instanceof L2Summon && !(this instanceof L2PetInstance && weaponInst != null))
			{
				((L2Summon) this).setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
			}
			else if (weaponInst != null)
			{
				weaponInst.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
			}

			if (player != null)
			{
				if (player.isCursedWeaponEquipped())
				{
					// If hitted by a cursed weapon, Cp is reduced to 0
					if (!target.isInvul(this))
					{
						target.setCurrentCp(0);
					}
				}
				else if (player.isHero())
				{
					if (target instanceof L2PcInstance && ((L2PcInstance) target).isCursedWeaponEquipped())
					// If a cursed weapon is hitted by a Hero, Cp is reduced to 0
					{
						target.setCurrentCp(0);
					}
				}
			}
		}

		// If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack
		// to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		if (attack.hasHits())
		{
			broadcastPacket(attack);
		}

		// Notify AI with EVT_READY_TO_ACT
		ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), timeAtk + reuse);
	}

	/**
	 * Launch a Bow attack.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate if hit is missed or not </li>
	 * <li>Consume arrows </li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient </li>
	 * <li>If hit isn't missed, calculate if hit is critical </li>
	 * <li>If hit isn't missed, calculate physical damages </li>
	 * <li>If the L2Character is a L2PcInstance, Send a Server->Client packet SetupGauge </li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Calculate and set the disable delay of the bow in function of the Attack Speed</li>
	 * <li>Add this hit to the Server-Client packet Attack </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 * @param sAtk   The Attack Speed of the attacker
	 * @return True if the hit isn't missed
	 */
	private boolean doAttackHitByBow(Attack attack, L2Character target, int sAtk, int reuse)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;

		// Calculate if hit is missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);

		// Consume arrows
		reduceArrowCount(false);

		_move = null;

		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target);

			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null), target);

			// Calculate physical damages
			damage1 = (int) Formulas.calcPhysDam(this, target, shld1, crit1, false, attack.soulshotCharge);
		}

		// Check if the L2Character is a L2PcInstance
		if (this instanceof L2PcInstance)
		{
			// Send a system message
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GETTING_READY_TO_SHOOT_AN_ARROW));

			// Send a Server->Client packet SetupGauge
			SetupGauge sg = new SetupGauge(SetupGauge.BLUE, sAtk + reuse);
			sendPacket(sg);
		}

		// Create a new hit task with Medium priority
		ThreadPoolManager.getInstance()
				.scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshotCharge, shld1), sAtk);

		// Calculate and set the disable delay of the bow in function of the Attack Speed
		_disableBowAttackEndTime = (sAtk + reuse) / TimeController.MILLIS_IN_TICK + TimeController.getGameTicks();

		// Add this hit to the Server-Client packet Attack
		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1));

		// Return true if hit isn't missed
		return !miss1;
	}

	/**
	 * Launch a CrossBow attack.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate if hit is missed or not </li>
	 * <li>Consume bolts </li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient </li>
	 * <li>If hit isn't missed, calculate if hit is critical </li>
	 * <li>If hit isn't missed, calculate physical damages </li>
	 * <li>If the L2Character is a L2PcInstance, Send a Server->Client packet SetupGauge </li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Calculate and set the disable delay of the crossbow in function of the Attack Speed</li>
	 * <li>Add this hit to the Server-Client packet Attack </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 * @param sAtk   The Attack Speed of the attacker
	 * @return True if the hit isn't missed
	 */
	private boolean doAttackHitByCrossBow(Attack attack, L2Character target, int sAtk, int reuse)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;

		// Calculate if hit is missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);

		// Consume bolts
		reduceArrowCount(true);

		_move = null;

		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target);

			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null), target);

			// Calculate physical damages
			damage1 = (int) Formulas.calcPhysDam(this, target, shld1, crit1, false, attack.soulshotCharge);
		}

		// Check if the L2Character is a L2PcInstance
		if (this instanceof L2PcInstance)
		{
			// Send a system message
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CROSSBOW_PREPARING_TO_FIRE));

			// Send a Server->Client packet SetupGauge
			SetupGauge sg = new SetupGauge(SetupGauge.BLUE, sAtk + reuse);
			sendPacket(sg);
		}

		// Create a new hit task with Medium priority
		if (this instanceof L2Attackable)
		{
			if (((L2Attackable) this)._soulshotcharged)
			{
				// Create a new hit task with Medium priority
				ThreadPoolManager.getInstance()
						.scheduleAi(new HitTask(target, damage1, crit1, miss1, L2ItemInstance.CHARGED_SOULSHOT, shld1),
								sAtk);
			}
			else
			{
				ThreadPoolManager.getInstance()
						.scheduleAi(new HitTask(target, damage1, crit1, miss1, L2ItemInstance.CHARGED_NONE, shld1),
								sAtk);
			}
		}
		else
		{
			ThreadPoolManager.getInstance()
					.scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshotCharge, shld1), sAtk);
		}

		// Calculate and set the disable delay of the bow in function of the Attack Speed
		_disableCrossBowAttackEndTime = (sAtk + reuse) / TimeController.MILLIS_IN_TICK + TimeController.getGameTicks();

		// Add this hit to the Server-Client packet Attack
		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1));

		// Return true if hit isn't missed
		return !miss1;
	}

	/**
	 * Launch a Dual attack.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate if hits are missed or not </li>
	 * <li>If hits aren't missed, calculate if shield defense is efficient </li>
	 * <li>If hits aren't missed, calculate if hit is critical </li>
	 * <li>If hits aren't missed, calculate physical damages </li>
	 * <li>Create 2 new hit tasks with Medium priority</li>
	 * <li>Add those hits to the Server-Client packet Attack </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 * @return True if hit 1 or hit 2 isn't missed
	 */
	private boolean doAttackHitByDual(Attack attack, L2Character target, int sAtk)
	{
		int damage1 = 0;
		int damage2 = 0;
		byte shld1 = 0;
		byte shld2 = 0;
		boolean crit1 = false;
		boolean crit2 = false;

		// Calculate if hits are missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);
		boolean miss2 = Formulas.calcHitMiss(this, target);

		// Check if hit 1 isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient against hit 1
			shld1 = Formulas.calcShldUse(this, target);

			// Calculate if hit 1 is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null), target);

			// Calculate physical damages of hit 1
			damage1 = (int) Formulas.calcPhysDam(this, target, shld1, crit1, true, attack.soulshotCharge);
			damage1 /= 2;
		}

		// Check if hit 2 isn't missed
		if (!miss2)
		{
			// Calculate if shield defense is efficient against hit 2
			shld2 = Formulas.calcShldUse(this, target);

			// Calculate if hit 2 is critical
			crit2 = Formulas.calcCrit(getStat().getCriticalHit(target, null), target);

			// Calculate physical damages of hit 2
			damage2 = (int) Formulas.calcPhysDam(this, target, shld2, crit2, true, attack.soulshotCharge);
			damage2 /= 2;
		}

		if (this instanceof L2Attackable)
		{
			if (((L2Attackable) this)._soulshotcharged)
			{

				// Create a new hit task with Medium priority for hit 1
				ThreadPoolManager.getInstance()
						.scheduleAi(new HitTask(target, damage1, crit1, miss1, L2ItemInstance.CHARGED_SOULSHOT, shld1),
								sAtk / 2);

				// Create a new hit task with Medium priority for hit 2 with a higher delay
				ThreadPoolManager.getInstance()
						.scheduleAi(new HitTask(target, damage2, crit2, miss2, L2ItemInstance.CHARGED_SOULSHOT, shld2),
								sAtk);
			}
			else
			{
				ThreadPoolManager.getInstance()
						.scheduleAi(new HitTask(target, damage1, crit1, miss1, L2ItemInstance.CHARGED_NONE, shld1),
								sAtk / 2);

				// Create a new hit task with Medium priority for hit 2 with a higher delay
				ThreadPoolManager.getInstance()
						.scheduleAi(new HitTask(target, damage2, crit2, miss2, L2ItemInstance.CHARGED_NONE, shld2),
								sAtk);
			}
		}
		else
		{
			// Create a new hit task with Medium priority for hit 1
			ThreadPoolManager.getInstance()
					.scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshotCharge, shld1), sAtk / 2);

			// Create a new hit task with Medium priority for hit 2 with a higher delay
			ThreadPoolManager.getInstance()
					.scheduleAi(new HitTask(target, damage2, crit2, miss2, attack.soulshotCharge, shld2), sAtk);
		}

		// Add those hits to the Server-Client packet Attack
		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1),
				attack.createHit(target, damage2, miss2, crit2, shld2));

		// Return true if hit 1 or hit 2 isn't missed
		return !miss1 || !miss2;
	}

	/**
	 * Launch a Pole attack.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get all visible objects in a spherical area near the L2Character to obtain possible targets </li>
	 * <li>If possible target is the L2Character targeted, launch a simple attack against it </li>
	 * <li>If possible target isn't the L2Character targeted but is attackable, launch a simple attack against it </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @return True if one hit isn't missed
	 */
	private boolean doAttackHitByPole(Attack attack, L2Character target, int sAtk)
	{
		//double angleChar;
		int maxRadius = getPhysicalAttackRange();
		int maxAngleDiff = (int) getStat().calcStat(Stats.POWER_ATTACK_ANGLE, 120, null, null);

		if (Config.DEBUG)
		{
			Log.info("doAttackHitByPole: Max radius = " + maxRadius);
			Log.info("doAttackHitByPole: Max angle = " + maxAngleDiff);
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
		int attackRandomCountMax = (int) getStat().calcStat(Stats.ATTACK_COUNT_MAX, 1, null, null) - 1;
		int attackcount = 0;

		/*if (angleChar <= 0)
			angleChar += 360;*/
		// ===========================================================

		boolean hitted = doAttackHitSimple(attack, target, 100, sAtk);
		int percentLostPerTarget = 15;
		if (Config.isServer(Config.TENKAI_ESTHUS) && target instanceof L2MonsterInstance)
		{
			percentLostPerTarget = 50;
		}
		double attackpercent = 100 - percentLostPerTarget;
		L2Character temp;
		Collection<L2Object> objs = getKnownList().getKnownObjects().values();
		//synchronized (getKnownList().getKnownObjects())
		{
			for (L2Object obj : objs)
			{
				if (obj == target)
				{
					continue; // do not hit twice
				}
				// Check if the L2Object is a L2Character
				if (obj instanceof L2Character)
				{
					if (obj instanceof L2PetInstance && this instanceof L2PcInstance &&
							((L2PetInstance) obj).getOwner() == this)
					{
						continue;
					}

					if (!Util.checkIfInRange(maxRadius, this, obj, false))
					{
						continue;
					}

					// otherwise hit too high/low. 650 because mob z coord
					// sometimes wrong on hills
					if (Math.abs(obj.getZ() - getZ()) > 650)
					{
						continue;
					}
					if (!isFacing(obj, maxAngleDiff))
					{
						continue;
					}

					if (this instanceof L2Attackable && obj instanceof L2PcInstance &&
							getTarget() instanceof L2Attackable)
					{
						continue;
					}

					if (this instanceof L2Attackable && obj instanceof L2Attackable &&
							((L2Attackable) this).getEnemyClan() == null && ((L2Attackable) this).getIsChaos() == 0)
					{
						continue;
					}

					if (this instanceof L2Attackable && obj instanceof L2Attackable &&
							!((L2Attackable) this).getEnemyClan().equals(((L2Attackable) obj).getClan()) &&
							((L2Attackable) this).getIsChaos() == 0)
					{
						continue;
					}

					if (this instanceof L2Playable && obj instanceof L2Playable)
					{
						final L2PcInstance activeChar = getActingPlayer();
						final L2PcInstance targetedPlayer = obj.getActingPlayer();

						if (targetedPlayer.getPvpFlag() == 0)
						{
							if (activeChar.hasAwakaned())
							{
								if (!targetedPlayer.hasAwakaned())
								{
									continue;
								}
							}
							else if (targetedPlayer.hasAwakaned())
							{
								continue;
							}

							if (targetedPlayer.getLevel() + 9 <= activeChar.getLevel())
							{
								continue;
							}
							else if (activeChar.getLevel() + 9 <= targetedPlayer.getLevel())
							{
								continue;
							}
						}
					}

					temp = (L2Character) obj;

					// Launch a simple attack against the L2Character targeted
					if (!temp.isAlikeDead())
					{
						if (temp == getAI().getAttackTarget() || temp.isAutoAttackable(this))
						{
							hitted |= doAttackHitSimple(attack, temp, attackpercent, sAtk);
							attackpercent *= 1 - percentLostPerTarget / 100.0;

							attackcount++;
							if (attackcount > attackRandomCountMax)
							{
								break;
							}
						}
					}
				}
			}
		}

		// Return true if one hit isn't missed
		return hitted;
	}

	/**
	 * Launch a simple attack.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate if hit is missed or not </li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient </li>
	 * <li>If hit isn't missed, calculate if hit is critical </li>
	 * <li>If hit isn't missed, calculate physical damages </li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Add this hit to the Server-Client packet Attack </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 * @return True if the hit isn't missed
	 */
	private boolean doAttackHitSimple(Attack attack, L2Character target, int sAtk)
	{
		return doAttackHitSimple(attack, target, 100, sAtk);
	}

	private boolean doAttackHitSimple(Attack attack, L2Character target, double attackpercent, int sAtk)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;

		// Calculate if hit is missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);

		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target);

			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null), target);

			// Calculate physical damages
			damage1 = (int) Formulas.calcPhysDam(this, target, shld1, crit1, false, attack.soulshotCharge);

			if (attackpercent != 100)
			{
				damage1 = (int) (damage1 * attackpercent / 100);
			}
		}

		// Create a new hit task with Medium priority
		if (this instanceof L2Attackable)
		{
			if (((L2Attackable) this)._soulshotcharged)
			{
				ThreadPoolManager.getInstance()
						.scheduleAi(new HitTask(target, damage1, crit1, miss1, L2ItemInstance.CHARGED_SOULSHOT, shld1),
								sAtk);
			}
			else
			{
				ThreadPoolManager.getInstance()
						.scheduleAi(new HitTask(target, damage1, crit1, miss1, L2ItemInstance.CHARGED_NONE, shld1),
								sAtk);
			}
		}
		else
		{
			ThreadPoolManager.getInstance()
					.scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshotCharge, shld1), sAtk);
		}

		// Add this hit to the Server-Client packet Attack
		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1));

		// Return true if hit isn't missed
		return !miss1;
	}

	/**
	 * Manage the casting task (casting and interrupt time, re-use delay...) and display the casting bar and animation on client.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Verify the possibilty of the the cast : skill is a spell, caster isn't muted... </li>
	 * <li>Get the list of all targets (ex : area effects) and define the L2Charcater targeted (its stats will be used in calculation)</li>
	 * <li>Calculate the casting time (base + modifier of MAtkSpd), interrupt time and re-use delay</li>
	 * <li>Send a Server->Client packet MagicSkillUse (to diplay casting animation), a packet SetupGauge (to display casting bar) and a system message </li>
	 * <li>Disable all skills during the casting time (create a task EnableAllSkills)</li>
	 * <li>Disable the skill during the re-use delay (create a task EnableSkill)</li>
	 * <li>Create a task MagicUseTask (that will call method onMagicUseTimer) to launch the Magic Skill at the end of the casting time</li><BR><BR>
	 *
	 * @param skill The L2Skill to use
	 */
	public void doCast(L2Skill skill, boolean second)
	{
		beginCast(skill, false, second);
	}

	public void doCast(L2Skill skill)
	{
		beginCast(skill, false, false);
	}

	public void doSimultaneousCast(L2Skill skill)
	{
		beginCast(skill, true, false);
	}

	public void doCast(L2Skill skill, L2Character target, L2Object[] targets)
	{
		if (!checkDoCastConditions(skill))
		{
			setIsCastingNow(false);
			return;
		}

		//if (this instanceof L2PcInstance && !((L2PcInstance) this).onCast(target, skill))
		//	return;

		// Override casting type
		if (skill.isSimultaneousCast())
		{
			doSimultaneousCast(skill, target, targets);
			return;
		}

		// Recharge AutoSoulShot
		// this method should not used with L2Playable

		beginCast(skill, false, false, target, targets);
	}

	public void doSimultaneousCast(L2Skill skill, L2Character target, L2Object[] targets)
	{
		if (!checkDoCastConditions(skill))// || this instanceof L2PcInstance)
		{
			setIsCastingSimultaneouslyNow(false);
			return;
		}

		// Recharge AutoSoulShot
		// this method should not used with L2Playable

		beginCast(skill, true, false, target, targets);
	}

	private void beginCast(L2Skill skill, boolean simultaneously, boolean second)
	{
		if (!checkDoCastConditions(skill))
		{
			if (simultaneously)
			{
				setIsCastingSimultaneouslyNow(false);
			}
			else if (second)
			{
				setIsCastingNow2(false);
			}
			else
			{
				setIsCastingNow(false);
			}
			if (this instanceof L2PcInstance)
			{
				getAI().setIntention(AI_INTENTION_ACTIVE);
			}
			return;
		}
		// Override casting type
		if (skill.isSimultaneousCast() && !simultaneously)
		{
			simultaneously = true;
		}

		//Recharge AutoSoulShot
		if (skill.useSoulShot())
		{
			if (this instanceof L2PcInstance)
			{
				((L2PcInstance) this).rechargeAutoSoulShot(true, false, false);
			}
			else if (this instanceof L2Summon)
			{
				((L2Summon) this).getOwner().rechargeAutoSoulShot(true, false, true);
			}
		}
		else if (skill.useSpiritShot())
		{
			if (this instanceof L2PcInstance)
			{
				((L2PcInstance) this).rechargeAutoSoulShot(false, true, false);
			}
			else if (this instanceof L2Summon)
			{
				((L2Summon) this).getOwner().rechargeAutoSoulShot(false, true, true);
			}
		}

		// Set the target of the skill in function of Skill Type and Target Type
		L2Character target = null;
		// Get all possible targets of the skill in a table in function of the skill target type
		L2Object[] targets = skill.getTargetList(this);

		// AURA skills should always be using caster as target
		if (skill.getTargetType() == L2SkillTargetType.TARGET_AURA ||
				skill.getTargetType() == L2SkillTargetType.TARGET_FRONT_AURA ||
				skill.getTargetType() == L2SkillTargetType.TARGET_BEHIND_AURA ||
				skill.getTargetType() == L2SkillTargetType.TARGET_GROUND || skill.isUseableWithoutTarget())
		{
			target = this;
		}
		else
		{
			if ((targets == null || targets.length == 0) && !skill.isUseableWithoutTarget())
			{
				if (simultaneously)
				{
					setIsCastingSimultaneouslyNow(false);
				}
				else
				{
					setIsCastingNow(false);
				}

				return;
			}

			switch (skill.getSkillType())
			{
				case BUFF:
				case HEAL:
				case COMBATPOINTHEAL:
				case MANAHEAL:
				case HPMPHEAL_PERCENT:
				case HPCPHEAL_PERCENT:
				case HPMPCPHEAL_PERCENT:
				{
					target = (L2Character) targets[0];
					break;
				}

				default:
				{
					switch (skill.getTargetType())
					{
						case TARGET_SELF:
						case TARGET_PET:
						case TARGET_MY_SUMMON:
						case TARGET_SUMMON:
						case TARGET_PARTY:
						case TARGET_CLAN:
						case TARGET_CLANPARTY:
						case TARGET_ALLY:
						case TARGET_FRIEND_NOTME:
						{
							target = (L2Character) targets[0];
							break;
						}

						default:
						{
							target = (L2Character) getTarget();
							break;
						}
					}
				}
			}
		}

		beginCast(skill, simultaneously, second, target, targets);
	}

	private void beginCast(L2Skill skill, boolean simultaneously, boolean second, L2Character target, L2Object[] targets)
	{
		stopEffectsOnAction(skill);

		if (target == null)
		{
			if (simultaneously)
			{
				setIsCastingSimultaneouslyNow(false);
			}
			else if (second)
			{
				setIsCastingNow2(false);
			}
			else
			{
				setIsCastingNow(false);
			}
			if (this instanceof L2PcInstance)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				getAI().setIntention(AI_INTENTION_ACTIVE);
			}
			return;
		}

		//TODO LasTravel, TEMP fix, IDK why L2Monsters don't check skill conditions
		if (this instanceof L2MonsterInstance)
		{
			if (!skill.checkCondition(this, target, false))
			{
				return;
			}
		}

		if (skill.getSkillType() == L2SkillType.RESURRECT)
		{
			if (isResurrectionBlocked() || target.isResurrectionBlocked())
			{
				sendPacket(SystemMessage.getSystemMessage(356)); // Reject resurrection
				target.sendPacket(SystemMessage.getSystemMessage(356)); // Reject resurrection
				if (simultaneously)
				{
					setIsCastingSimultaneouslyNow(false);
				}
				else if (second)
				{
					setIsCastingNow2(false);
				}
				else
				{
					setIsCastingNow(false);
				}

				if (this instanceof L2PcInstance)
				{
					getAI().setIntention(AI_INTENTION_ACTIVE);
					sendPacket(ActionFailed.STATIC_PACKET);
				}
				return;
			}
		}

		// Get the Identifier of the skill
		int magicId = skill.getId();

		// Get the Display Identifier for a skill that client can't display
		int displayId = skill.getDisplayId();

		// Get the level of the skill
		int level = skill.getLevelHash();

		if (level < 1)
		{
			level = 1;
		}

		// Get the casting time of the skill (base)
		int hitTime = skill.getHitTime();
		int coolTime = skill.getCoolTime();

		boolean effectWhileCasting = skill.getSkillType() == L2SkillType.CONTINUOUS_DEBUFF ||
				skill.getSkillType() == L2SkillType.CONTINUOUS_DRAIN ||
				skill.getSkillType() == L2SkillType.CONTINUOUS_CASTS || skill.getSkillType() == L2SkillType.FUSION ||
				skill.getSkillType() == L2SkillType.SIGNET_CASTTIME;

		// Calculate the casting time of the skill (base + modifier of MAtkSpd)
		// Don't modify the skill time for FORCE_BUFF skills. The skill time for those skills represent the buff time.
		if (!effectWhileCasting)
		{
			hitTime = Formulas.calcAtkSpd(this, skill, hitTime);
			if (coolTime > 0)
			{
				coolTime = Formulas.calcAtkSpd(this, skill, coolTime);
			}
		}

		double shotSave = L2ItemInstance.CHARGED_NONE;

		// Calculate altered Cast Speed due to BSpS/SpS
		L2ItemInstance weaponInst = getActiveWeaponInstance();
		if (weaponInst != null)
		{
			if (skill.isMagic() && !effectWhileCasting && !skill.isPotion())
			{
				if (weaponInst.getChargedSpiritShot() > L2ItemInstance.CHARGED_NONE)
				{
					//Only takes 70% of the time to cast a BSpS/SpS cast
					hitTime = (int) (0.70 * hitTime);
					coolTime = (int) (0.70 * coolTime);

					//Because the following are magic skills that do not actively 'eat' BSpS/SpS,
					//I must 'eat' them here so players don't take advantage of infinite speed increase
					switch (skill.getSkillType())
					{
						case BUFF:
						case MANAHEAL:
						case MANARECHARGE:
						case MANA_BY_LEVEL:
						case RESURRECT:
						case RECALL:
						case DRAIN:
							weaponInst.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
							break;
					}
				}
			}

			// Save shots value for repeats
			if (skill.useSoulShot())
			{
				shotSave = weaponInst.getChargedSoulShot();
			}
			else if (skill.useSpiritShot())
			{
				shotSave = weaponInst.getChargedSpiritShot();
			}
		}

		if (this instanceof L2Npc)
		{
			if (((L2Npc) this).useSpiritShot())
			{
				hitTime = (int) (0.70 * hitTime);
				coolTime = (int) (0.70 * coolTime);
			}
		}

		// Don't modify skills HitTime if staticHitTime is specified for skill in datapack.
		if (skill.isStaticHitTime())
		{
			hitTime = skill.getHitTime();
			coolTime = skill.getCoolTime();
		}
		// if basic hitTime is higher than 500 than the min hitTime is 500
		else if (skill.getHitTime() >= 500 && hitTime < 500)
		{
			hitTime = 500;
		}

		// queue herbs and potions
		if (isCastingSimultaneouslyNow() && simultaneously)
		{
			ThreadPoolManager.getInstance().scheduleAi(new UsePotionTask(this, skill), 100);
			return;
		}

		// Set the _castInterruptTime and casting status (L2PcInstance already has this true)
		if (simultaneously)
		{
			setIsCastingSimultaneouslyNow(true);
		}
		else if (second)
		{
			setIsCastingNow2(true);
		}
		else
		{
			setIsCastingNow(true);
		}

		//setLastCast1(!second);

		// Note: _castEndTime = GameTimeController.getGameTicks() + (coolTime + hitTime) / GameTimeController.MILLIS_IN_TICK;
		if (!simultaneously)
		{
			_castInterruptTime = -2 + TimeController.getGameTicks() + hitTime / TimeController.MILLIS_IN_TICK;
			setLastSkillCast(skill);
		}
		else
		{
			setLastSimultaneousSkillCast(skill);
		}

		// Init the reuse time of the skill
		int reuseDelay;
		if (skill.isStaticReuse())
		{
			reuseDelay = skill.getReuseDelay();
		}
		else
		{
			if (skill.isMagic())
			{
				reuseDelay = (int) (skill.getReuseDelay() * getStat().getMReuseRate(skill));
			}
			else
			{
				reuseDelay = (int) (skill.getReuseDelay() * getStat().getPReuseRate(skill));
			}
		}

		boolean skillMastery = Formulas.calcSkillMastery(this, skill);

		//Buffs and debuffs and get reset duration
		if (skillMastery && (skill.getSkillType() == L2SkillType.BUFF || skill.getSkillType() == L2SkillType.DEBUFF) ||
				skill.isPotion() || skill.isToggle())
		{
			skillMastery = false;
		}
		if (skill.hasEffects() && skillMastery)
		{
			for (L2AbnormalTemplate ab : skill.getEffectTemplates())
			{
				if (!skillMastery)
				{
					break;
				}

				if (ab.effectType == L2AbnormalType.DEBUFF || ab.effectType == L2AbnormalType.BUFF)
				{
					skillMastery = false;
				}
			}
		}

		// Skill reuse check
		if (reuseDelay > 30000 && !skillMastery)
		{
			addTimeStamp(skill, reuseDelay);
		}

		// Check if this skill consume mp on start casting
		int initmpcons = getStat().getMpInitialConsume(skill);
		if (initmpcons > 0)
		{
			getStatus().reduceMp(initmpcons);
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			sendPacket(su);
		}

		// Disable the skill during the re-use delay and create a task EnableSkill with Medium priority to enable it at the end of the re-use delay
		if (reuseDelay > 10)
		{
			if (skillMastery || isGM())
			{
				reuseDelay = 100;

				if (getActingPlayer() != null)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SKILL_READY_TO_USE_AGAIN);
					getActingPlayer().sendPacket(sm);
					sm = null;
				}
			}

			disableSkill(skill, reuseDelay);
		}

		// Make sure that char is facing selected target
		if (target != this)
		{
			setHeading(Util.calculateHeadingFrom(this, target));
		}

		// For force buff skills, start the effect as long as the player is casting.
		if (effectWhileCasting)
		{
			// Consume Items if necessary and Send the Server->Client packet InventoryUpdate with Item modification to all the L2Character
			if (skill.getItemConsumeId() > 0 && skill.getItemConsume() > 0)
			{
				if (!destroyItemByItemId("Consume", skill.getItemConsumeId(), skill.getItemConsume(), null, true))
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					if (simultaneously)
					{
						setIsCastingSimultaneouslyNow(false);
					}
					else if (second)
					{
						setIsCastingNow2(false);
					}
					else
					{
						setIsCastingNow(false);
					}

					if (this instanceof L2PcInstance)
					{
						getAI().setIntention(AI_INTENTION_ACTIVE);
					}
					return;
				}
			}

			// Consume Souls if necessary
			if (skill.getSoulConsumeCount() > 0 || skill.getMaxSoulConsumeCount() > 0)
			{
				if (this instanceof L2PcInstance)
				{
					if (!((L2PcInstance) this).decreaseSouls(skill.getSoulConsumeCount(), skill))
					{
						if (simultaneously)
						{
							setIsCastingSimultaneouslyNow(false);
						}
						else if (second)
						{
							setIsCastingNow2(false);
						}
						else
						{
							setIsCastingNow(false);
						}
						return;
					}
				}
			}

			if (this instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) this;
				if (skill.getFameConsume() > 0)
				{
					if (player.getFame() >= skill.getFameConsume())
					{
						player.setFame(player.getFame() - skill.getFameConsume());
						player.sendPacket(new UserInfo(player));
					}
					else
					{
						if (simultaneously)
						{
							setIsCastingSimultaneouslyNow(false);
						}
						else if (second)
						{
							setIsCastingNow2(false);
						}
						else
						{
							setIsCastingNow(false);
						}
						return;
					}
				}
				if (skill.getClanRepConsume() > 0)
				{
					if (player.getClan() != null && player.getClan().getReputationScore() >= skill.getClanRepConsume())
					{
						player.getClan().takeReputationScore(skill.getClanRepConsume(), false);
						SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
						smsg.addItemNumber(skill.getClanRepConsume());
						player.sendPacket(smsg);
					}
					else
					{
						if (simultaneously)
						{
							setIsCastingSimultaneouslyNow(false);
						}
						else if (second)
						{
							setIsCastingNow2(false);
						}
						else
						{
							setIsCastingNow(false);
						}
						return;
					}
				}
			}

			if (skill.getSkillType() == L2SkillType.CONTINUOUS_DEBUFF)
			{
				setContinuousDebuffTargets(targets);
			}

			if (skill.getSkillType() == L2SkillType.FUSION)
			{
				startFusionSkill(target, skill);
			}
			else
			{
				callSkill(skill, targets);
			}
		}

		// Send a Server->Client packet MagicSkillUser with target, displayId, level, skillTime, reuseDelay
		// to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		if (!skill.isToggle()) // Toggles should not display animations upon cast
		{
			broadcastPacket(
					new MagicSkillUse(this, target, displayId, level, hitTime, reuseDelay, skill.getReuseHashCode(),
							second ? 1 : 0, skill.getTargetType() == L2SkillTargetType.TARGET_GROUND ||
							skill.getTargetType() == L2SkillTargetType.TARGET_GROUND_AREA, skill.getActionId()));
		}

		// Send a system message USE_S1 to the L2Character
		if (this instanceof L2PcInstance && magicId != 1312)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.USE_S1);
			sm.addSkillName(skill);
			sendPacket(sm);
		}

		if (this instanceof L2Playable)
		{
			if (!effectWhileCasting)
			{
				L2PcInstance player = getActingPlayer();
				if (skill.getItemConsumeId() > 0 && skill.getItemConsume() != 0)
				{
					if (!destroyItemByItemId("Consume", skill.getItemConsumeId(), skill.getItemConsume(), null, true))
					{
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
						abortCast();
						return;
					}
				}

				if (skill.getFameConsume() > 0)
				{
					if (player.getFame() >= skill.getFameConsume())
					{
						player.setFame(player.getFame() - skill.getFameConsume());
						player.sendPacket(new UserInfo(player));
					}
					else
					{
						getActingPlayer()
								.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_FAME_POINTS));
						abortCast();
						return;
					}
				}
				if (skill.getClanRepConsume() > 0)
				{
					if (player.getClan() != null && player.getClan().getReputationScore() >= skill.getClanRepConsume())
					{
						player.getClan().takeReputationScore(skill.getClanRepConsume(), false);
						SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
						smsg.addItemNumber(skill.getClanRepConsume());
						player.sendPacket(smsg);
					}
					else
					{
						getActingPlayer().sendPacket(
								SystemMessage.getSystemMessage(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW));
						abortCast();
						return;
					}
				}
			}

			//reduce talisman mana on skill use
			if (skill.getReferenceItemId() > 0 &&
					ItemTable.getInstance().getTemplate(skill.getReferenceItemId()).getBodyPart() == L2Item.SLOT_DECO)
			{
				for (L2ItemInstance item : getInventory().getItemsByItemId(skill.getReferenceItemId()))
				{
					if (item.isEquipped())
					{
						item.decreaseMana(false, skill.getReuseDelay() / 60000);
						break;
					}
				}
			}
		}

		// Before start AI Cast Broadcast Fly Effect is Need
		if (skill.getFlyType() != null/* && (this instanceof L2PcInstance)*/)
		{
			FlyType flyType = FlyType.valueOf(skill.getFlyType());
			int radius = skill.getFlyRadius();
			int x, y, z;

			if (radius != 0)
			{
				double angle = Util.convertHeadingToDegree(getHeading());
				double radian = Math.toRadians(angle);
				double course = Math.toRadians(skill.getFlyCourse());

				float x1 = (float) Math.cos(Math.PI + radian + course);
				float y1 = (float) Math.sin(Math.PI + radian + course);

				if (skill.getTargetType() == L2SkillTargetType.TARGET_SINGLE)
				{
					x = target.getX() + (int) (x1 * radius);
					y = target.getY() + (int) (y1 * radius);
					z = target.getZ();
				}
				else
				{
					x = getX() + (int) (x1 * radius);
					y = getY() + (int) (y1 * radius);
					z = getZ() + 100;
				}

				if (Config.GEODATA > 0)
				{
					Location destiny =
							GeoData.getInstance().moveCheck(getX(), getY(), getZ(), x, y, z, getInstanceId());
					if (destiny.getX() != x || destiny.getY() != y)
					{
						x = destiny.getX() - (int) (x1 * 10);
						y = destiny.getY() - (int) (y1 * 10);
						z = destiny.getZ();
					}
				}

				broadcastPacket(new FlyToLocation(this, x, y, z, flyType));
			}
			else
			{
				x = target.getX();
				y = target.getY();
				z = target.getZ();
				broadcastPacket(new FlyToLocation(this, target, flyType));
			}

			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			abortAttack();

			setXYZ(x, y, z);
			ThreadPoolManager.getInstance().scheduleEffect(new FlyToLocationTask(this, x, y, z), 400);
		}

		MagicUseTask mut = new MagicUseTask(targets, skill, hitTime, coolTime, simultaneously, shotSave, second);

		// launch the magic in hitTime milliseconds
		if (hitTime > 410)
		{
			// Send a Server->Client packet SetupGauge with the color of the gauge and the casting time
			if (this instanceof L2PcInstance && !effectWhileCasting)
			{
				sendPacket(new SetupGauge(SetupGauge.BLUE, hitTime));
			}

			if (skill.getHitCounts() > 0)
			{
				hitTime = hitTime * skill.getHitTimings()[0] / 100;

				if (hitTime < 410)
				{
					hitTime = 410;
				}
			}

			if (effectWhileCasting)
			{
				mut.phase = 2;
			}

			if (simultaneously)
			{
				Future<?> future = _simultSkillCast;
				if (future != null)
				{
					future.cancel(true);
					_simultSkillCast = null;
				}

				// Create a task MagicUseTask to launch the MagicSkill at the end of the casting time (hitTime)
				// For client animation reasons (party buffs especially) 400 ms before!
				_simultSkillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, hitTime - 400);
			}
			else if (second)
			{
				Future<?> future = _skillCast2;
				if (future != null)
				{
					future.cancel(true);
					_skillCast2 = null;
				}

				// Create a task MagicUseTask to launch the MagicSkill at the end of the casting time (hitTime)
				// For client animation reasons (party buffs especially) 400 ms before!
				_skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(mut, hitTime - 400);
			}
			else
			{
				Future<?> future = _skillCast;
				if (future != null)
				{
					future.cancel(true);
					_skillCast = null;
				}

				// Create a task MagicUseTask to launch the MagicSkill at the end of the casting time (hitTime)
				// For client animation reasons (party buffs especially) 400 ms before!
				_skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, hitTime - 400);
			}
		}
		else
		{
			mut.hitTime = 0;
			onMagicLaunchedTimer(mut);
		}
	}

	/**
	 * Check if casting of skill is possible
	 *
	 * @param skill
	 * @return True if casting is possible
	 */
	protected boolean checkDoCastConditions(L2Skill skill)
	{
		if (skill == null || isSkillDisabled(skill) ||
				(skill.getFlyRadius() > 0 || skill.getFlyType() != null) && isMovementDisabled())
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		boolean canCastWhileStun = false;
		switch (skill.getId())
		{
			case 30008: // Wind Blend
			case 19227: // Wind Blend Trigger
			case 30009: // Deceptive Blink
			{
				canCastWhileStun = true;
				break;
			}
			default:
			{
				break;
			}
		}

		if (isSkillDisabled(skill) ||
				(skill.getFlyRadius() > 0 || skill.getFlyType() != null) && isMovementDisabled() && !canCastWhileStun)
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// Check if the caster has enough MP
		if (getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
		{
			// Send a System Message to the caster
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_MP));

			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// Check if the caster has enough HP
		if (getCurrentHp() <= skill.getHpConsume())
		{
			// Send a System Message to the caster
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_HP));

			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// Disallow players to use hp consuming skills while waiting for an oly match to start
		if (skill.getHpConsume() > 0 && this instanceof L2PcInstance && ((L2PcInstance) this).isInOlympiadMode() &&
				!((L2PcInstance) this).isOlympiadStart())
		{
			// Send a System Message to the caster
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));

			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if (!skill.isPotion() && !skill.canBeUsedWhenDisabled() && !canCastWhileStun)
		{
			// Check if the skill is a magic spell and if the L2Character is not muted
			if (skill.isMagic())
			{
				if (isMuted())
				{
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
			else
			{
				// Check if the skill is physical and if the L2Character is not physical_muted
				if (isPhysicalMuted())
				{
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
				else if (isPhysicalAttackMuted()) // Prevent use attack
				{
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
		}

		// prevent casting signets to peace zone
		if (skill.getSkillType() == L2SkillType.SIGNET || skill.getSkillType() == L2SkillType.SIGNET_CASTTIME)
		{
			L2WorldRegion region = getWorldRegion();
			if (region == null)
			{
				return false;
			}
			boolean canCast = true;
			if (skill.getTargetType() == L2SkillTargetType.TARGET_GROUND && this instanceof L2PcInstance)
			{
				Point3D wp = getSkillCastPosition();
				if (!region.checkEffectRangeInsidePeaceZone(skill, wp.getX(), wp.getY(), wp.getZ()))
				{
					canCast = false;
				}
			}
			else if (!region.checkEffectRangeInsidePeaceZone(skill, getX(), getY(), getZ()))
			{
				canCast = false;
			}
			if (!canCast)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addSkillName(skill);
				sendPacket(sm);
				return false;
			}
		}

		// Check if the caster owns the weapon needed
		if (!skill.getWeaponDependancy(this))
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// Check if the spell consumes an Item
		// TODO: combine check and consume
		if (skill.getItemConsumeId() > 0 && skill.getItemConsume() != 0 && getInventory() != null)
		{
			// Get the L2ItemInstance consumed by the spell
			L2ItemInstance requiredItems = getInventory().getItemByItemId(skill.getItemConsumeId());

			// Check if the caster owns enough consumed Item to cast
			if (requiredItems == null || requiredItems.getCount() < skill.getItemConsume())
			{
				// Checked: when a summon skill failed, server show required consume item count
				if (skill.getSkillType() == L2SkillType.SUMMON)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SUMMONING_SERVITOR_COSTS_S2_S1);
					sm.addItemName(skill.getItemConsumeId());
					sm.addNumber(skill.getItemConsume());
					sendPacket(sm);
					return false;
				}
				else
				{
					// Send a System Message to the caster
					sendPacket(SystemMessage
							.getSystemMessage(SystemMessageId.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL));
					return false;
				}
			}
		}

		if (getActingPlayer() != null)
		{
			L2PcInstance player = getActingPlayer();
			if (skill.getFameConsume() > 0)
			{
				if (player.getFame() < skill.getFameConsume())
				{
					// Send a System Message to the caster
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_FAME_POINTS));
					return false;
				}
			}
			if (skill.getClanRepConsume() > 0)
			{
				if (player.getClan() == null || player.getClan().getReputationScore() < skill.getClanRepConsume())
				{
					// Send a System Message to the caster
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW));
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Index according to skill id the current timestamp of use.<br><br>
	 *
	 * @param skill id
	 * @param reuse delay
	 *              <BR><B>Overridden in :</B>  (L2PcInstance)
	 */
	public void addTimeStamp(L2Skill skill, long reuse)
	{
        /**/
	}

	public void startFusionSkill(L2Character target, L2Skill skill)
	{
		if (skill.getSkillType() != L2SkillType.FUSION)
		{
			return;
		}

		if (_fusionSkill == null)
		{
			_fusionSkill = new FusionSkill(this, target, skill);
		}
	}

	public void setSkillCastPosition(Point3D position)
	{
		_skillCastPosition = position;
	}

	public Point3D getSkillCastPosition()
	{
		return _skillCastPosition;
	}

	/**
	 * Kill the L2Character.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set target to null and cancel Attack or Cast </li>
	 * <li>Stop movement </li>
	 * <li>Stop HP/MP/CP Regeneration task </li>
	 * <li>Stop all active skills effects in progress on the L2Character </li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform </li>
	 * <li>Notify L2Character AI </li><BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2NpcInstance : Create a DecayTask to remove the corpse of the L2NpcInstance after 7 seconds </li>
	 * <li> L2Attackable : Distribute rewards (EXP, SP, Drops...) and notify Quest Engine </li>
	 * <li> L2PcInstance : Apply Death Penalty, Manage gain/loss Karma and Item Drop </li><BR><BR>
	 *
	 * @param killer The L2Character who killed it
	 */
	public boolean doDie(L2Character killer)
	{
		// killing is only possible one time
		synchronized (this)
		{
			if (isDead())
			{
				return false;
			}
			// now reset currentHp to zero
			setCurrentHp(0);
			setIsDead(true);
		}

		// Set target to null and cancel Attack or Cast
		setTarget(null);

		// Stop movement
		stopMove(null);

		// Stop HP/MP/CP Regeneration task
		getStatus().stopHpMpRegeneration();

		// Stop all active skills effects in progress on the L2Character,
		// if the Character isn't affected by Soul of The Phoenix or Salvation
		if (this instanceof L2Playable && ((L2Playable) this).isPhoenixBlessed())
		{
			if (((L2Playable) this).getCharmOfLuck()) //remove Lucky Charm if player has SoulOfThePhoenix/Salvation buff
			{
				((L2Playable) this).stopCharmOfLuck(null);
			}
			if (((L2Playable) this).isNoblesseBlessed())
			{
				((L2Playable) this).stopNoblesseBlessing(null);
			}
		}
		// Same thing if the Character isn't a Noblesse Blessed L2PlayableInstance
		else if (this instanceof L2Playable && ((L2Playable) this).isNoblesseBlessed())
		{
			((L2Playable) this).stopNoblesseBlessing(null);

			if (((L2Playable) this).getCharmOfLuck()) //remove Lucky Charm if player have Nobless blessing buff
			{
				((L2Playable) this).stopCharmOfLuck(null);
			}
		}
		else
		{
			stopAllEffectsExceptThoseThatLastThroughDeath();
		}

		if (this instanceof L2PcInstance && ((L2PcInstance) this).getAgathionId() != 0)
		{
			((L2PcInstance) this).setAgathionId(0);
		}
		calculateRewards(killer);

		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		broadcastStatusUpdate();

		// Notify L2Character AI
		if (hasAI())
		{
			getAI().notifyEvent(CtrlEvent.EVT_DEAD);
		}

		if (getWorldRegion() != null)
		{
			getWorldRegion().onDeath(this, killer);
		}

		getAttackByList().clear();
		// If character is PhoenixBlessed
		// or has charm of courage inside siege battlefield (exact operation to be confirmed)
		// a resurrection popup will show up
		if (this instanceof L2Summon)
		{
			if (((L2Summon) this).isPhoenixBlessed() && ((L2Summon) this).getOwner() != null)
			{
				((L2Summon) this).getOwner().reviveRequest(((L2Summon) this).getOwner(), null, true);
			}
		}
		if (this instanceof L2PcInstance)
		{
			if (((L2Playable) this).isPhoenixBlessed())
			{
				((L2PcInstance) this).reviveRequest((L2PcInstance) this, null, false);
			}
			else if (isAffected(L2EffectType.CHARMOFCOURAGE.getMask()) && ((L2PcInstance) this).isInSiege())
			{
				((L2PcInstance) this).reviveRequest((L2PcInstance) this, null, false);
			}
		}
		try
		{
			if (_fusionSkill != null || _continuousDebuffTargets != null)
			{
				abortCast();
			}

			for (L2Character character : getKnownList().getKnownCharacters())
			{
				if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this ||
						character.getTarget() == this && character.getLastSkillCast() != null &&
								character.getLastSkillCast().getTargetType() == L2SkillTargetType.TARGET_SINGLE &&
								(character.getLastSkillCast().getSkillType() == L2SkillType.CONTINUOUS_DEBUFF ||
										character.getLastSkillCast().getSkillType() == L2SkillType.CONTINUOUS_DRAIN ||
										character.getLastSkillCast().getSkillType() == L2SkillType.CONTINUOUS_CASTS))
				{
					character.abortCast();
				}
			}
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "deleteMe()", e);
		}
		return true;
	}

	public void deleteMe()
	{
		setDebug(null);

		if (hasAI())
		{
			getAI().stopAITask();
		}
	}

	protected void calculateRewards(L2Character killer)
	{
	}

	/**
	 * Sets HP, MP and CP and revives the L2Character.
	 */
	public void doRevive()
	{
		if (!isDead())
		{
			return;
		}
		if (!isTeleporting())
		{
			setIsPendingRevive(false);
			setIsDead(false);
			boolean restorefull = false;

			if (this instanceof L2Playable && ((L2Playable) this).isPhoenixBlessed())
			{
				restorefull = true;
				((L2Playable) this).stopPhoenixBlessing(null);
			}
			if (restorefull)
			{
				_status.setCurrentCp(getCurrentCp()); //this is not confirmed, so just trigger regeneration
				_status.setCurrentHp(getMaxHp(), true); //confirmed
				_status.setCurrentMp(getMaxMp(), true); //and also confirmed
			}
			else
			{
				_status.setCurrentHp(getMaxHp() * Config.RESPAWN_RESTORE_HP, true);
			}
			_status.setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);
			_status.setCurrentMp(getMaxMp() * Config.RESPAWN_RESTORE_MP);

			// Start broadcast status
			broadcastPacket(new Revive(this));
			if (getWorldRegion() != null)
			{
				getWorldRegion().onRevive(this);
			}
		}
		else
		{
			setIsPendingRevive(true);
		}
	}

	/**
	 * Revives the L2Character using skill.
	 */
	public void doRevive(double revivePower)
	{
		doRevive();
	}

	// =========================================================
	// Property - Public

	/**
	 * Return the L2CharacterAI of the L2Character and if its null create a new one.
	 */
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai; // copy handle
		if (ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					_ai = new L2CharacterAI(new AIAccessor());
				}
				return _ai;
			}
		}
		return ai;
	}

	public void setAI(L2CharacterAI newAI)
	{
		L2CharacterAI oldAI = getAI();
		if (oldAI != null && oldAI != newAI && oldAI instanceof L2AttackableAI)
		{
			oldAI.stopAITask();
		}
		_ai = newAI;
	}

	/**
	 * Return True if the L2Character has a L2CharacterAI.
	 */
	public boolean hasAI()
	{
		return _ai != null;
	}

	/**
	 * Return True if the L2Character is RaidBoss or his minion.
	 */
	public boolean isRaid()
	{
		return false;
	}

	/**
	 * Return True if the L2Character is minion.
	 */
	public boolean isMinion()
	{
		return false;
	}

	/**
	 * Return True if the L2Character is minion of RaidBoss.
	 */
	public boolean isRaidMinion()
	{
		return false;
	}

	/**
	 * Return a list of L2Character that attacked.
	 */
	public final Set<L2Character> getAttackByList()
	{
		if (_attackByList != null)
		{
			return _attackByList;
		}

		synchronized (this)
		{
			if (_attackByList == null)
			{
				_attackByList = new CopyOnWriteArraySet<>();
			}
		}
		return _attackByList;
	}

	public final L2Skill getLastSimultaneousSkillCast()
	{
		return _lastSimultaneousSkillCast;
	}

	public void setLastSimultaneousSkillCast(L2Skill skill)
	{
		_lastSimultaneousSkillCast = skill;
	}

	public final L2Skill getLastSkillCast()
	{
		return _lastSkillCast;
	}

	public void setLastSkillCast(L2Skill skill)
	{
		_lastSkillCast = skill;
	}

	public final boolean isAfraid()
	{
		return isAffected(L2EffectType.FEAR.getMask());
	}

	public final boolean isInLove()
	{
		return isAffected(L2EffectType.LOVE.getMask());
	}

	/**
	 * Return True if the L2Character can't use its skills (ex : stun, sleep...).
	 */
	public final boolean isAllSkillsDisabled()
	{
		return _allSkillsDisabled || isStunned() || isSleeping() || isParalyzed();
	}

	/**
	 * Return True if the L2Character can't attack (stun, sleep, attackEndTime, fakeDeath, paralyse, attackMute).
	 */
	public boolean isAttackingDisabled()
	{
		return isFlying() || isStunned() || isSleeping() || _attackEndTime > TimeController.getGameTicks() ||
				isAlikeDead() || isParalyzed() || isPhysicalAttackMuted() || isCoreAIDisabled();
	}

	public final Calculator[] getCalculators()
	{
		return _calculators;
	}

	public final boolean isConfused()
	{
		return isAffected(L2EffectType.CONFUSION.getMask());
	}

	/**
	 * Return True if the L2Character is dead or use fake death.
	 */
	public boolean isAlikeDead()
	{
		return _isDead;
	}

	/**
	 * Return True if the L2Character is dead.
	 */
	public final boolean isDead()
	{
		return _isDead;
	}

	public final void setIsDead(boolean value)
	{
		_isDead = value;
	}

	public boolean isImmobilized()
	{
		return _isImmobilized;
	}

	public void setIsImmobilized(boolean value)
	{
		_isImmobilized = value;
	}

	public final boolean isMuted()
	{
		return isAffected(L2EffectType.MUTE.getMask());
	}

	public final boolean isPhysicalMuted()
	{
		return isAffected(L2EffectType.PHYSICAL_MUTE.getMask());
	}

	public final boolean isPhysicalAttackMuted()
	{
		return isAffected(L2EffectType.PHYSICAL_ATTACK_MUTE.getMask());
	}

	/**
	 * Return True if the L2Character can't move (stun, root, sleep, overload, paralyzed).
	 */
	public boolean isMovementDisabled()
	{
		// check for isTeleporting to prevent teleport cheating (if appear packet not received)
		return isStunned() || isRooted() || isSleeping() || isOverloaded() || isParalyzed() || isImmobilized() ||
				isAlikeDead() || isTeleporting();
	}

	/**
	 * Return True if the L2Character can not be controlled by the player (confused, afraid).
	 */
	public final boolean isOutOfControl()
	{
		return isConfused() || isAfraid() || isInLove();
	}

	public final boolean isOverloaded()
	{
		return _isOverloaded;
	}

	/**
	 * Set the overloaded status of the L2Character is overloaded (if True, the L2PcInstance can't take more item).
	 */
	public final void setIsOverloaded(boolean value)
	{
		_isOverloaded = value;
	}

	public final boolean isParalyzed()
	{
		return _isParalyzed || isAffected(L2EffectType.PARALYZE.getMask()) ||
				isAffected(L2EffectType.PETRIFY.getMask());
	}

	public final void setIsParalyzed(boolean value)
	{
		_isParalyzed = value;
	}

	public final boolean isPendingRevive()
	{
		return isDead() && _isPendingRevive;
	}

	public final void setIsPendingRevive(boolean value)
	{
		_isPendingRevive = value;
	}

	public boolean isDisarmed()
	{
		return isAffected(L2EffectType.DISARM.getMask());
	}

	public boolean isArmorDisarmed()
	{
		return isAffected(L2EffectType.DISARM_ARMOR.getMask());
	}

	public final boolean isRooted()
	{
		return isAffected(L2EffectType.ROOT.getMask());
	}

	/**
	 * Return True if the L2Character is running.
	 */
	public boolean isRunning()
	{
		return _isRunning;
	}

	public final void setIsRunning(boolean value)
	{
		if (isInvul() && this instanceof L2ArmyMonsterInstance)
		{
			return;
		}

		_isRunning = value;

		if (this instanceof L2Npc && ((L2Npc) this).getIsInvisible())
		{
			return;
		}

		if (getRunSpeed() != 0)
		{
			broadcastPacket(new ChangeMoveType(this));
		}

		if (this instanceof L2PcInstance)
		{
			((L2PcInstance) this).broadcastUserInfo();
		}
		else if (this instanceof L2Summon)
		{
			this.broadcastStatusUpdate();
		}
		else if (this instanceof L2Npc)
		{
			Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
			//synchronized (character.getKnownList().getKnownPlayers())
			{
				for (L2PcInstance player : plrs)
				{
					if (player == null)
					{
						continue;
					}

					if (getRunSpeed() == 0)
					{
						player.sendPacket(new ServerObjectInfo((L2Npc) this, player));
					}
					else
					{
						player.sendPacket(new NpcInfo((L2Npc) this, player));
					}
				}
			}
		}
	}

	/**
	 * Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance.
	 */
	public final void setRunning()
	{
		if (!isRunning())
		{
			setIsRunning(true);
		}
	}

	public final boolean isSleeping()
	{
		return isAffected(L2EffectType.SLEEP.getMask());
	}

	public final boolean isStunned()
	{
		return isAffected(L2EffectType.STUN.getMask());
	}

	public final boolean isBetrayed()
	{
		return isAffected(L2EffectType.BETRAY.getMask());
	}

	public final boolean isInSpatialTrap()
	{
		return isAffected(L2EffectType.SPATIAL_TRAP.getMask());
	}

	public final boolean isTeleporting()
	{
		return _isTeleporting;
	}

	public void setIsTeleporting(boolean value)
	{
		_isTeleporting = value;
	}

	public void setIsInvul(boolean b)
	{
		_isInvul = b;
	}

	public boolean isInvul()
	{
		return _isInvul || _isTeleporting || isAffected(L2EffectType.INVINCIBLE.getMask());
	}

	public void setIsMortal(boolean b)
	{
		_isMortal = b;
	}

	public boolean isMortal()
	{
		return _isMortal;
	}

	public boolean isUndead()
	{
		return false;
	}

	public boolean isResurrectionBlocked()
	{
		return isAffected(L2EffectType.BLOCK_RESURRECTION.getMask());
	}

	public final boolean isFlying()
	{
		return _isFlying;
	}

	public final void setIsFlying(boolean mode)
	{
		_isFlying = mode;
	}

	public boolean isInvul(L2Character attacker)
	{
		if (attacker != null)
		{
			// If the attacker is farther than our ranged invul, his attacks won't affect us
			int radius = (int) Math.round(calcStat(Stats.INVUL_RADIUS, 0, attacker, null));
			if (radius > 0 && !Util.checkIfInRange(radius, this, attacker, false))
			{
				return true;
			}
		}

		return isInvul();
	}

	@Override
	public CharKnownList getKnownList()
	{
		return (CharKnownList) super.getKnownList();
	}

	@Override
	public void initKnownList()
	{
		setKnownList(new CharKnownList(this));
	}

	public CharStat getStat()
	{
		return _stat;
	}

	/**
	 * Initializes the CharStat class of the L2Object,
	 * is overwritten in classes that require a different CharStat Type.
	 * <p>
	 * Removes the need for instanceof checks.
	 */
	public void initCharStat()
	{
		_stat = new CharStat(this);
	}

	public final void setStat(CharStat value)
	{
		_stat = value;
	}

	public CharStatus getStatus()
	{
		return _status;
	}

	/**
	 * Initializes the CharStatus class of the L2Object,
	 * is overwritten in classes that require a different CharStatus Type.
	 * <p>
	 * Removes the need for instanceof checks.
	 */
	public void initCharStatus()
	{
		_status = new CharStatus(this);
	}

	public final void setStatus(CharStatus value)
	{
		_status = value;
	}

	@Override
	public CharPosition getPosition()
	{
		return (CharPosition) super.getPosition();
	}

	@Override
	public void initPosition()
	{
		setObjectPosition(new CharPosition(this));
	}

	public L2CharTemplate getTemplate()
	{
		return _template;
	}

	/**
	 * Set the template of the L2Character.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...).
	 * All of those properties are stored in a different template for each type of L2Character.
	 * Each template is loaded once in the server cache memory (reduce memory use).
	 * When a new instance of L2Character is spawned, server just create a link between the instance and the template
	 * This link is stored in <B>_template</B><BR><BR>
	 * <p>
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> this instanceof L2Character</li><BR><BR
	 */
	public final void setTemplate(L2CharTemplate template)
	{
		_template = template;
	}

	/**
	 * Return the Title of the L2Character.
	 */
	public final String getTitle()
	{
		return _title;
	}

	/**
	 * Set the Title of the L2Character.
	 */
	public final void setTitle(String value)
	{
		if (value == null)
		{
			_title = "";
		}
		else
		{
			_title = value.length() > 16 ? value.substring(0, 15) : value;
		}
	}

	/**
	 * Set the L2Character movement type to walk and send Server->Client packet ChangeMoveType to all others L2PcInstance.
	 */
	public final void setWalking()
	{
		if (isRunning())
		{
			setIsRunning(false);
		}
	}

	/**
	 * Task lauching the function onHitTimer().<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance </li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary </li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...) </li><BR><BR>
	 */
	public class HitTask implements Runnable
	{
		L2Character _hitTarget;
		int _damage;
		boolean _crit;
		boolean _miss;
		byte _shld;
		double _soulshot;

		public HitTask(L2Character target, int damage, boolean crit, boolean miss, double soulshot, byte shld)
		{
			_hitTarget = target;
			_damage = damage;
			_crit = crit;
			_shld = shld;
			_miss = miss;
			_soulshot = soulshot;
		}

		@Override
		public void run()
		{
			try
			{
				onHitTimer(_hitTarget, _damage, _crit, _miss, _soulshot, _shld, false);
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "Failed executing HitTask. Hit target: " + _hitTarget);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Task lauching the magic skill phases
	 */
	class MagicUseTask implements Runnable
	{
		L2Object[] targets;
		L2Skill skill;
		int count;
		int hitTime;
		int coolTime;
		int phase;
		boolean simultaneously;
		double shots;
		boolean second;

		public MagicUseTask(L2Object[] tgts, L2Skill s, int hit, int coolT, boolean simultaneous, double shot, boolean sec)
		{
			targets = tgts;
			skill = s;
			count = 0;
			phase = 1;
			hitTime = hit;
			coolTime = coolT;
			simultaneously = simultaneous;
			shots = shot;
			second = sec;
			//Log.info("START " + System.currentTimeMillis() + " (" + skill.getName() + ") " + hit + " | " + coolT);
		}

		@Override
		public void run()
		{
			try
			{
				switch (phase)
				{
					case 1:
						onMagicLaunchedTimer(this);
						//Log.info("LAUNC " + System.currentTimeMillis() + " (" + skill.getName() + ")");
						break;
					case 2:
						onMagicHitTimer(this);
						//Log.info("HIT   " + System.currentTimeMillis() + " (" + skill.getName() + ")");
						break;
					case 3:
						onMagicFinalizer(this);
						//Log.info("ENDED " + System.currentTimeMillis() + " (" + skill.getName() + ")");
						break;
					default:
						break;
				}
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "Failed executing MagicUseTask.", e);
				if (simultaneously)
				{
					setIsCastingSimultaneouslyNow(false);
				}
				else if (second)
				{
					setIsCastingNow2(false);
				}
				else
				{
					setIsCastingNow(false);
				}
			}
		}
	}

	/**
	 * Task launching the function useMagic()
	 */
	private static class QueuedMagicUseTask implements Runnable
	{
		L2PcInstance _currPlayer;
		L2Skill _queuedSkill;
		boolean _isCtrlPressed;
		boolean _isShiftPressed;

		public QueuedMagicUseTask(L2PcInstance currPlayer, L2Skill queuedSkill, boolean isCtrlPressed, boolean isShiftPressed)
		{
			_currPlayer = currPlayer;
			_queuedSkill = queuedSkill;
			_isCtrlPressed = isCtrlPressed;
			_isShiftPressed = isShiftPressed;
		}

		@Override
		public void run()
		{
			try
			{
				_currPlayer.useMagic(_queuedSkill, _isCtrlPressed, _isShiftPressed);
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "Failed executing QueuedMagicUseTask.", e);
			}
		}
	}

	/**
	 * Task of AI notification
	 */
	public class NotifyAITask implements Runnable
	{
		private final CtrlEvent _evt;

		NotifyAITask(CtrlEvent evt)
		{
			_evt = evt;
		}

		@Override
		public void run()
		{
			try
			{
				getAI().notifyEvent(_evt, null);
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING, "NotifyAITask failed. " + e.getMessage() + " Actor " + L2Character.this, e);
			}
		}
	}

	// =========================================================

	/**
	 * Task lauching the magic skill phases
	 */
	class FlyToLocationTask implements Runnable
	{
		private final L2Character _actor;
		@SuppressWarnings("unused")
		private final int _x;
		@SuppressWarnings("unused")
		private final int _y;
		@SuppressWarnings("unused")
		private final int _z;

		public FlyToLocationTask(L2Character actor, int x, int y, int z)
		{
			_actor = actor;
			_x = x;
			_y = y;
			_z = z;
		}

		@Override
		public void run()
		{
			try
			{
				//_actor.setXYZ(_x, _y, _z);
				broadcastPacket(new ValidateLocation(_actor));

				// Dirty fix for... summons not attacking targets automatically after jumping.
				if (_actor instanceof L2Summon)
				{
					_actor.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _actor.getTarget());
					_actor.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _actor.getTarget());
				}
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "Failed executing FlyToLocationTask.", e);
			}
		}
	}

	private final Set<Integer> _abnormalEffects = new CopyOnWriteArraySet<>();

	protected CharEffectList _effects = new CharEffectList(this);

	// Method - Public

	/**
	 * Launch and add L2Effect (including Stack Group management) to L2Character and update client magic icon.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 * <p>
	 * Several same effect can't be used on a L2Character at the same time.
	 * Indeed, effects are not stackable and the last cast will replace the previous in progress.
	 * More, some effects belong to the same Stack Group (ex WindWald and Haste Potion).
	 * If 2 effects of a same group are used at the same time on a L2Character, only the more efficient (identified by its priority order) will be preserve.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Add the L2Effect to the L2Character _effects</li>
	 * <li>If this effect doesn't belong to a Stack Group, add its Funcs to the Calculator set of the L2Character (remove the old one if necessary)</li>
	 * <li>If this effect has higher priority in its Stack Group, add its Funcs to the Calculator set of the L2Character (remove previous stacked effect Funcs if necessary)</li>
	 * <li>If this effect has NOT higher priority in its Stack Group, set the effect to Not In Use</li>
	 * <li>Update active skills in progress icons on player client</li><BR>
	 */
	public void addEffect(L2Abnormal newEffect)
	{
		// Make sure it doesn't crash if buff comes from NPC buffer or something like that
		if (newEffect.getEffector() instanceof L2PcInstance)
		{
			// Player characters who used custom command to refuse buffs, will only receive from party
			if (this instanceof L2PcInstance && _refuseBuffs && newEffect.getEffector() != newEffect.getEffected() &&
					newEffect.getSkill().getSkillType() == L2SkillType.BUFF)
			{
				//if (Config.isServer(Config.TENKAI))
				//	return;

				// Effector or effected have no party, so refuse
				if (!isInParty() || !newEffect.getEffector().isInParty())
				{
					return;
				}

				// One of both has party, but not same, so refuse
				if (!newEffect.getEffector().getParty().getPartyMembers().contains(this))
				{
					return;
				}
			}
		}

		_effects.queueEffect(newEffect, false);
	}

	/**
	 * Stop and remove L2Effect (including Stack Group management) from L2Character and update client magic icon.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 * <p>
	 * Several same effect can't be used on a L2Character at the same time.
	 * Indeed, effects are not stackable and the last cast will replace the previous in progress.
	 * More, some effects belong to the same Stack Group (ex WindWald and Haste Potion).
	 * If 2 effects of a same group are used at the same time on a L2Character, only the more efficient (identified by its priority order) will be preserve.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove Func added by this effect from the L2Character Calculator (Stop L2Effect)</li>
	 * <li>If the L2Effect belongs to a not empty Stack Group, replace theses Funcs by next stacked effect Funcs</li>
	 * <li>Remove the L2Effect from _effects of the L2Character</li>
	 * <li>Update active skills in progress icons on player client</li><BR>
	 */
	public final void removeEffect(L2Abnormal effect)
	{
		_effects.queueEffect(effect, true);
	}

	/**
	 * Active abnormal effects flags in the binary mask and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startVisualEffect(VisualEffect effect)
	{
		_abnormalEffects.add(effect.getId());
		updateAbnormalEffect();
	}

	public final void startVisualEffect(int effectId)
	{
		_abnormalEffects.add(effectId);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Confused flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startConfused()
	{
		getAI().notifyEvent(CtrlEvent.EVT_CONFUSED);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Fake Death flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startFakeDeath()
	{
		if (!(this instanceof L2PcInstance))
		{
			return;
		}

		for (L2Character target : getKnownList().getKnownCharacters())
		{
			if (target != null && target != this && target.getTarget() == this)
			{
				target.setTarget(null);
				target.abortAttack();
				//target.abortCast();
				target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
		}

		((L2PcInstance) this).setIsFakeDeath(true);
		/* Aborts any attacks/casts if fake dead */
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH);
		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_START_FAKEDEATH));
	}

	/**
	 * Active the abnormal effect Fear flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startFear()
	{
		getAI().notifyEvent(CtrlEvent.EVT_AFRAID);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Fear flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startLove()
	{
		getAI().notifyEvent(CtrlEvent.EVT_INLOVE);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Muted flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startMuted()
	{
		/* Aborts any casts if muted */
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Psychical_Muted flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startPsychicalMuted()
	{
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Root flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startRooted()
	{
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_ROOTED);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Sleep flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startSleeping()
	{
		/* Aborts any attacks/casts if sleeped */
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_SLEEPING);
		updateAbnormalEffect();
	}

	/**
	 * Launch a Stun Abnormal Effect on the L2Character.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate the success rate of the Stun Abnormal Effect on this L2Character</li>
	 * <li>If Stun succeed, active the abnormal effect Stun flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet</li>
	 * <li>If Stun NOT succeed, send a system message Failed to the L2PcInstance attacker</li><BR><BR>
	 */
	public final void startStunning()
	{
		/* Aborts any attacks/casts if stunned */
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_STUNNED);
		if (!(this instanceof L2Summon))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		updateAbnormalEffect();
	}

	public final void startParalyze()
	{
		/* Aborts any attacks/casts if paralyzed */
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_PARALYZED);
	}

	/**
	 * Modify the abnormal effect map according to the mask.<BR><BR>
	 */
	public final void stopVisualEffect(VisualEffect effect)
	{
		_abnormalEffects.remove(effect.getId());
		updateAbnormalEffect();
	}

	public final void stopVisualEffect(int effectId)
	{
		_abnormalEffects.remove(effectId);
		updateAbnormalEffect();
	}

	/**
	 * Stop all active skills effects in progress on the L2Character.<BR><BR>
	 */
	public void stopAllEffects()
	{
		_effects.stopAllEffects();
	}

	public void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		_effects.stopAllEffectsExceptThoseThatLastThroughDeath();
	}

	/**
	 * Stop a specified/all Confused abnormal L2Effect.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Confused abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _confused to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 */
	public final void stopConfused(L2Abnormal effect)
	{
		if (effect == null)
		{
			stopEffects(L2EffectType.CONFUSION);
		}
		else
		{
			removeEffect(effect);
		}
		if (!(this instanceof L2PcInstance))
		{
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
		//updateAbnormalEffect();
	}

	/**
	 * Stop and remove the L2Effects corresponding to the L2Skill Identifier and update client magic icon.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 *
	 * @param skillId The L2Skill Identifier of the L2Effect to remove from _effects
	 */
	public final void stopSkillEffects(int skillId)
	{
		_effects.stopSkillEffects(skillId);
	}

	/**
	 * Stop and remove all L2Effect of the selected type (ex : BUFF, DMG_OVER_TIME...) from the L2Character and update client magic icon.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove Func added by this effect from the L2Character Calculator (Stop L2Effect)</li>
	 * <li>Remove the L2Effect from _effects of the L2Character</li>
	 * <li>Update active skills in progress icons on player client</li><BR><BR>
	 *
	 * @param type The type of effect to stop ((ex : BUFF, DMG_OVER_TIME...)
	 */
	public final void stopEffects(L2AbnormalType type)
	{
		_effects.stopEffects(type);
	}

	public final void stopEffects(L2EffectType type)
	{
		_effects.stopEffects(type);
	}

	/**
	 * Exits all buffs effects of the skills with "removedOnAnyAction" set.
	 * Called on any action except movement (attack, cast).
	 */
	public final void stopEffectsOnAction(L2Skill skill)
	{
		_effects.stopEffectsOnAction(skill);
	}

	/**
	 * Exits all buffs effects of the skills with "removedOnDamage" set.
	 * Called on decreasing HP and mana burn.
	 */
	public final void stopEffectsOnDamage(boolean awake, int damage)
	{
		_effects.stopEffectsOnDamage(awake, damage);
	}

	/**
	 * Exits all buffs effects of the skills with "removedOnDebuffBlock" set.
	 * Called on debuffs blocked by a debuff immunity stat
	 */
	public final void stopEffectsOnDebuffBlock()
	{
		_effects.stopEffectsOnDebuffBlock();
	}

	/**
	 * Stop a specified/all Fake Death abnormal L2Effect.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Fake Death abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _fake_death to False </li>
	 * <li>Notify the L2Character AI</li><BR><BR>
	 */
	public final void stopFakeDeath(boolean removeEffects)
	{
		if (removeEffects)
		{
			stopEffects(L2EffectType.FAKE_DEATH);
		}

		// if this is a player instance, start the grace period for this character (grace from mobs only)!
		if (this instanceof L2PcInstance)
		{
			((L2PcInstance) this).setIsFakeDeath(false);
			((L2PcInstance) this).setRecentFakeDeath(true);
		}

		ChangeWaitType revive = new ChangeWaitType(this, ChangeWaitType.WT_STOP_FAKEDEATH);
		broadcastPacket(revive);
		//TODO: Temp hack: players see FD on ppl that are moving: Teleport to someone who uses FD - if he gets up he will fall down again for that client -
		// even tho he is actually standing... Probably bad info in CharInfo packet?
		broadcastPacket(new Revive(this));
	}

	/**
	 * Stop a specified/all Fear abnormal L2Effect.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Fear abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _affraid to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 */
	public final void stopFear(boolean removeEffects)
	{
		if (removeEffects)
		{
			stopEffects(L2EffectType.FEAR);
		}
		updateAbnormalEffect();
	}

	public final void stopLove(boolean removeEffects)
	{
		if (removeEffects)
		{
			stopEffects(L2EffectType.LOVE);
		}
		updateAbnormalEffect();
	}

	/**
	 * Stop a specified/all Muted abnormal L2Effect.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Muted abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _muted to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 */
	public final void stopMuted(boolean removeEffects)
	{
		if (removeEffects)
		{
			stopEffects(L2EffectType.MUTE);
		}

		updateAbnormalEffect();
	}

	public final void stopPsychicalMuted(boolean removeEffects)
	{
		if (removeEffects)
		{
			stopEffects(L2EffectType.PHYSICAL_MUTE);
		}

		updateAbnormalEffect();
	}

	/**
	 * Stop a specified/all Root abnormal L2Effect.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Root abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _rooted to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 */
	public final void stopRooting(boolean removeEffects)
	{
		if (removeEffects)
		{
			stopEffects(L2EffectType.ROOT);
		}

		if (!(this instanceof L2PcInstance))
		{
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
		updateAbnormalEffect();
	}

	/**
	 * Stop a specified/all Sleep abnormal L2Effect.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Sleep abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _sleeping to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 */
	public final void stopSleeping(boolean removeEffects)
	{
		if (removeEffects)
		{
			stopEffects(L2EffectType.SLEEP);
		}

		if (!(this instanceof L2PcInstance))
		{
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
		updateAbnormalEffect();
	}

	/**
	 * Stop a specified/all Stun abnormal L2Effect.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Stun abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _stuned to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 */
	public final void stopStunning(boolean removeEffects)
	{
		if (removeEffects)
		{
			stopEffects(L2EffectType.STUN);
		}

		if (!(this instanceof L2PcInstance))
		{
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
		updateAbnormalEffect();
	}

	public final void stopParalyze(boolean removeEffects)
	{
		if (removeEffects)
		{
			stopEffects(L2EffectType.PARALYZE);
		}

		if (!(this instanceof L2PcInstance))
		{
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
	}

	/**
	 * Stop L2Effect: Transformation<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove Transformation Effect</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 */
	public final void stopTransformation(boolean removeEffects)
	{
		if (removeEffects)
		{
			stopEffects(L2AbnormalType.MUTATE);
		}

		// if this is a player instance, then untransform, also set the transform_id column equal to 0 if not cursed.
		if (this instanceof L2PcInstance)
		{
			if (((L2PcInstance) this).getTransformation() != null)
			{
				this.unTransform(removeEffects);
			}
		}

		if (!(this instanceof L2PcInstance))
		{
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
		updateAbnormalEffect();
	}

	/**
	 * Not Implemented.<BR><BR>
	 * <p>
	 * <B><U> Overridden in</U> :</B><BR><BR>
	 * <li>L2NPCInstance</li>
	 * <li>L2PcInstance</li>
	 * <li>L2Summon</li>
	 * <li>L2DoorInstance</li><BR><BR>
	 */
	public abstract void updateAbnormalEffect();

	/**
	 * Update active skills in progress (In Use and Not In Use because stacked) icons on client.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress (In Use and Not In Use because stacked) are represented by an icon on the client.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method ONLY UPDATE the client of the player and not clients of all players in the party.</B></FONT><BR><BR>
	 */
	public final void updateEffectIcons()
	{
		updateEffectIcons(false);
	}

	/**
	 * Updates Effect Icons for this character(palyer/summon) and his party if any<BR>
	 * <p>
	 * Overridden in:<BR>
	 * L2PcInstance<BR>
	 * L2Summon<BR>
	 *
	 * @param partyOnly
	 */
	public void updateEffectIcons(boolean partyOnly)
	{
		// overridden
	}

	// Property - Public

	/**
	 * Return a map of 16 bits (0x0000) containing all abnormal effect in progress for this L2Character.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * In Server->Client packet, each effect is represented by 1 bit of the map (ex : BLEEDING = 0x0001 (bit 1), SLEEP = 0x0080 (bit 8)...).
	 * The map is calculated by applying a BINARY OR operation on each effect.<BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Server Packet : CharInfo, NpcInfo, NpcInfoPoly, UserInfo...</li><BR><BR>
	 */
	public Set<Integer> getAbnormalEffect()
	{
		Set<Integer> result = new HashSet<>();
		synchronized (_abnormalEffects)
		{
			result.addAll(_abnormalEffects);
		}

		return result;
	}

	/**
	 * Return all active skills effects in progress on the L2Character.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the effect.<BR><BR>
	 *
	 * @return A table containing all active skills effect in progress on the L2Character
	 */
	public final L2Abnormal[] getAllEffects()
	{
		return _effects.getAllEffects();
	}

	public final L2Abnormal[] getAllDebuffs()
	{
		return _effects.getAllDebuffs();
	}

	/**
	 * Return L2Effect in progress on the L2Character corresponding to the L2Skill Identifier.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 *
	 * @param skillId The L2Skill Identifier of the L2Effect to return from the _effects
	 * @return The L2Effect corresponding to the L2Skill Identifier
	 */
	public final L2Abnormal getFirstEffect(int skillId)
	{
		return _effects.getFirstEffect(skillId);
	}

	public final L2Abnormal getFirstEffect(final String stackType)
	{
		return _effects.getFirstEffect(stackType);
	}

	public L2Abnormal getFirstEffectByName(String _effectName)
	{
		return _effects.getFirstEffectByName(_effectName);
	}

	/**
	 * Return the first L2Effect in progress on the L2Character created by the L2Skill.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 *
	 * @param skill The L2Skill whose effect must be returned
	 * @return The first L2Effect created by the L2Skill
	 */
	public final L2Abnormal getFirstEffect(L2Skill skill)
	{
		return _effects.getFirstEffect(skill);
	}

	/**
	 * Return the first L2Effect in progress on the L2Character corresponding to the Effect Type (ex : BUFF, STUN, ROOT...).<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 *
	 * @param tp The Effect Type of skills whose effect must be returned
	 * @return The first L2Effect corresponding to the Effect Type
	 */
	public final L2Abnormal getFirstEffect(L2AbnormalType tp)
	{
		return _effects.getFirstEffect(tp);
	}

	// =========================================================
	// =========================================================
	// NEED TO ORGANIZE AND MOVE TO PROPER PLACE

	/**
	 * This class permit to the L2Character AI to obtain informations and uses L2Character method
	 */
	public class AIAccessor
	{
		public AIAccessor()
		{
		}

		/**
		 * Return the L2Character managed by this Accessor AI.<BR><BR>
		 */
		public L2Character getActor()
		{
			return L2Character.this;
		}

		/**
		 * Accessor to L2Character moveToLocation() method with an interaction area.<BR><BR>
		 */
		public void moveTo(int x, int y, int z, int offset)
		{
			moveToLocation(x, y, z, offset);
		}

		/**
		 * Accessor to L2Character moveToLocation() method without interaction area.<BR><BR>
		 */
		public void moveTo(int x, int y, int z)
		{
			moveToLocation(x, y, z, 0);
		}

		/**
		 * Accessor to L2Character stopMove() method.<BR><BR>
		 */
		public void stopMove(L2CharPosition pos)
		{
			L2Character.this.stopMove(pos);
		}

		/**
		 * Accessor to L2Character doAttack() method.<BR><BR>
		 */
		public void doAttack(L2Character target)
		{
			L2Character.this.doAttack(target);
		}

		/**
		 * Accessor to L2Character doCast() method.<BR><BR>
		 */
		public void doCast(L2Skill skill, boolean second)
		{
			L2Character.this.doCast(skill, second);
		}

		/**
		 * Create a NotifyAITask.<BR><BR>
		 */
		public NotifyAITask newNotifyTask(CtrlEvent evt)
		{
			return new NotifyAITask(evt);
		}

		/**
		 * Cancel the AI.<BR><BR>
		 */
		public void detachAI()
		{
			_ai = null;
		}
	}

	/**
	 * This class group all mouvement data.<BR><BR>
	 * <p>
	 * <B><U> Data</U> :</B><BR><BR>
	 * <li>_moveTimestamp : Last time position update</li>
	 * <li>_xDestination, _yDestination, _zDestination : Position of the destination</li>
	 * <li>_xMoveFrom, _yMoveFrom, _zMoveFrom  : Position of the origin</li>
	 * <li>_moveStartTime : Start time of the movement</li>
	 * <li>_ticksToMove : Nb of ticks between the start and the destination</li>
	 * <li>_xSpeedTicks, _ySpeedTicks : Speed in unit/ticks</li><BR><BR>
	 */
	public static class MoveData
	{
		// when we retrieve x/y/z we use GameTimeControl.getGameTicks()
		// if we are moving, but move timestamp==gameticks, we don't need
		// to recalculate position
		public int _moveStartTime;
		public int _moveTimestamp; // last update
		public int _xDestination;
		public int _yDestination;
		public int _zDestination;
		public double _xAccurate; // otherwise there would be rounding errors
		public double _yAccurate;
		public double _zAccurate;
		public int _heading;

		public boolean disregardingGeodata;
		public int onGeodataPathIndex;
		public List<AbstractNodeLoc> geoPath;
		public int geoPathAccurateTx;
		public int geoPathAccurateTy;
		public int geoPathGtx;
		public int geoPathGty;
	}

	/**
	 * Table containing all skillId that are disabled
	 */
	protected Map<Integer, Long> _disabledSkills;
	private boolean _allSkillsDisabled;

	//	private int _flyingRunSpeed;
	//	private int _floatingWalkSpeed;
	//	private int _flyingWalkSpeed;
	//	private int _floatingRunSpeed;

	/**
	 * Movement data of this L2Character
	 */
	protected MoveData _move;

	/**
	 * Orientation of the L2Character
	 */
	private int _heading;

	/**
	 * L2Charcater targeted by the L2Character
	 */
	private L2Object _target;

	// set by the start of attack, in game ticks
	private int _attackEndTime;
	private int _attacking;
	private int _disableBowAttackEndTime;
	private int _disableCrossBowAttackEndTime;

	private int _castInterruptTime;

	/**
	 * Table of calculators containing all standard NPC calculator (ex : ACCURACY_COMBAT, EVASION_RATE
	 */
	private static final Calculator[] NPC_STD_CALCULATOR;

	static
	{
		NPC_STD_CALCULATOR = Formulas.getStdNPCCalculators();
	}

	protected L2CharacterAI _ai;

	/**
	 * Future Skill Cast
	 */
	protected Future<?> _skillCast;
	protected Future<?> _skillCast2;
	protected Future<?> _simultSkillCast;

	/**
	 * Add a Func to the Calculator set of the L2Character.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR><BR>
	 * <p>
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR
	 * must be create in its _calculators before addind new Func object.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If _calculators is linked to NPC_STD_CALCULATOR, create a copy of NPC_STD_CALCULATOR in _calculators</li>
	 * <li>Add the Func object to _calculators</li><BR><BR>
	 *
	 * @param f The Func object to add to the Calculator corresponding to the state affected
	 */
	public final void addStatFunc(Func f)
	{
		if (f == null)
		{
			return;
		}

		synchronized (_calculators)
		{
			// Check if Calculator set is linked to the standard Calculator set of NPC
			if (_calculators == NPC_STD_CALCULATOR)
			{
				// Create a copy of the standard NPC Calculator set
				_calculators = new Calculator[Stats.NUM_STATS];

				for (int i = 0; i < Stats.NUM_STATS; i++)
				{
					if (NPC_STD_CALCULATOR[i] != null)
					{
						_calculators[i] = new Calculator(NPC_STD_CALCULATOR[i]);
					}
				}
			}

			// Select the Calculator of the affected state in the Calculator set
			int stat = f.stat.ordinal();

			if (_calculators[stat] == null)
			{
				_calculators[stat] = new Calculator();
			}

			// Add the Func to the calculator corresponding to the state
			_calculators[stat].addFunc(f);
		}
	}

	/**
	 * Add a list of Funcs to the Calculator set of the L2Character.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). <BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is ONLY for L2PcInstance</B></FONT><BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Equip an item from inventory</li>
	 * <li> Learn a new passive skill</li>
	 * <li> Use an active skill</li><BR><BR>
	 *
	 * @param funcs The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	public final void addStatFuncs(Func[] funcs)
	{

		ArrayList<Stats> modifiedStats = new ArrayList<>();

		for (Func f : funcs)
		{
			modifiedStats.add(f.stat);
			addStatFunc(f);
		}
		broadcastModifiedStats(modifiedStats);
	}

	/**
	 * Remove a Func from the Calculator set of the L2Character.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR><BR>
	 * <p>
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR
	 * must be create in its _calculators before addind new Func object.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the Func object from _calculators</li><BR><BR>
	 * <li>If L2Character is a L2NPCInstance and _calculators is equal to NPC_STD_CALCULATOR,
	 * free cache memory and just create a link on NPC_STD_CALCULATOR in _calculators</li><BR><BR>
	 *
	 * @param f The Func object to remove from the Calculator corresponding to the state affected
	 */
	public final void removeStatFunc(Func f)
	{
		if (f == null)
		{
			return;
		}

		// Select the Calculator of the affected state in the Calculator set
		int stat = f.stat.ordinal();

		synchronized (_calculators)
		{
			if (_calculators[stat] == null)
			{
				return;
			}

			// Remove the Func object from the Calculator
			_calculators[stat].removeFunc(f);

			if (_calculators[stat].size() == 0)
			{
				_calculators[stat] = null;
			}

			// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
			if (this instanceof L2Npc)
			{
				int i = 0;
				for (; i < Stats.NUM_STATS; i++)
				{
					if (!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
					{
						break;
					}
				}

				if (i >= Stats.NUM_STATS)
				{
					_calculators = NPC_STD_CALCULATOR;
				}
			}
		}
	}

	/**
	 * Remove a list of Funcs from the Calculator set of the L2PcInstance.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). <BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is ONLY for L2PcInstance</B></FONT><BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Unequip an item from inventory</li>
	 * <li> Stop an active skill</li><BR><BR>
	 *
	 * @param funcs The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	public final void removeStatFuncs(Func[] funcs)
	{

		ArrayList<Stats> modifiedStats = new ArrayList<>();

		for (Func f : funcs)
		{
			modifiedStats.add(f.stat);
			removeStatFunc(f);
		}

		broadcastModifiedStats(modifiedStats);
	}

	/**
	 * Remove all Func objects with the selected owner from the Calculator set of the L2Character.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR><BR>
	 * <p>
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR
	 * must be create in its _calculators before addind new Func object.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove all Func objects of the selected owner from _calculators</li><BR><BR>
	 * <li>If L2Character is a L2NPCInstance and _calculators is equal to NPC_STD_CALCULATOR,
	 * free cache memory and just create a link on NPC_STD_CALCULATOR in _calculators</li><BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Unequip an item from inventory</li>
	 * <li> Stop an active skill</li><BR><BR>
	 *
	 * @param owner The Object(Skill, Item...) that has created the effect
	 */
	public final void removeStatsOwner(Object owner)
	{

		ArrayList<Stats> modifiedStats = null;

		int i = 0;
		// Go through the Calculator set
		synchronized (_calculators)
		{
			for (Calculator calc : _calculators)
			{
				if (calc != null)
				{
					// Delete all Func objects of the selected owner
					if (modifiedStats != null)
					{
						modifiedStats.addAll(calc.removeOwner(owner));
					}
					else
					{
						modifiedStats = calc.removeOwner(owner);
					}

					if (calc.size() == 0)
					{
						_calculators[i] = null;
					}
				}
				i++;
			}

			// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
			if (this instanceof L2Npc)
			{
				i = 0;
				for (; i < Stats.NUM_STATS; i++)
				{
					if (!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
					{
						break;
					}
				}

				if (i >= Stats.NUM_STATS)
				{
					_calculators = NPC_STD_CALCULATOR;
				}
			}

			if (owner instanceof L2Abnormal)
			{
				if (!((L2Abnormal) owner).preventExitUpdate)
				{
					broadcastModifiedStats(modifiedStats);
				}
			}
			else
			{
				broadcastModifiedStats(modifiedStats);
			}
		}
	}

	protected void broadcastModifiedStats(ArrayList<Stats> stats)
	{
		if (stats == null || stats.isEmpty())
		{
			return;
		}

		boolean broadcastFull = false;
		StatusUpdate su = null;

		for (Stats stat : stats)
		{
			if (this instanceof L2Summon && ((L2Summon) this).getOwner() != null)
			{
				((L2Summon) this).updateAndBroadcastStatus(1);
				break;
			}
			else if (stat == Stats.POWER_ATTACK_SPEED)
			{
				if (su == null)
				{
					su = new StatusUpdate(this);
				}
				su.addAttribute(StatusUpdate.ATK_SPD, getPAtkSpd());
			}
			else if (stat == Stats.MAGIC_ATTACK_SPEED)
			{
				if (su == null)
				{
					su = new StatusUpdate(this);
				}
				su.addAttribute(StatusUpdate.CAST_SPD, getMAtkSpd());
			}
			else if (stat == Stats.MAX_HP && this instanceof L2Attackable)
			{
				if (su == null)
				{
					su = new StatusUpdate(this);
				}
				su.addAttribute(StatusUpdate.MAX_HP, getMaxVisibleHp());
			}
			else if (stat == Stats.LIMIT_HP)
			{
				getStatus().setCurrentHp(getCurrentHp()); // start regeneration if needed
			}
			/*else if (stat == Stats.MAX_CP)
			{
				if (this instanceof L2PcInstance)
				{
					if (su == null) su = new StatusUpdate(getObjectId());
					su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
				}
			}*/
			//else if (stat==Stats.MAX_MP)
			//{
			//	if (su == null) su = new StatusUpdate(getObjectId());
			//	su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
			//}
			else if (stat == Stats.RUN_SPEED)
			{
				broadcastFull = true;
			}
		}

		if (this instanceof L2PcInstance)
		{
			final L2PcInstance player = getActingPlayer();

			if (!player.isUpdateLocked())
			{
				if (broadcastFull)
				{
					((L2PcInstance) this).updateAndBroadcastStatus(2);
				}
				else
				{
					((L2PcInstance) this).updateAndBroadcastStatus(1);
					if (su != null)
					{
						broadcastPacket(su);
					}
				}
			}
		}
		else if (this instanceof L2Npc)
		{
			if (((L2Npc) this).getIsInvisible())
			{
				return;
			}

			if (broadcastFull)
			{
				Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
				//synchronized (getKnownList().getKnownPlayers())
				{
					for (L2PcInstance player : plrs)
					{
						if (player == null)
						{
							continue;
						}

						if (getRunSpeed() == 0)
						{
							player.sendPacket(new ServerObjectInfo((L2Npc) this, player));
						}
						else
						{
							player.sendPacket(new NpcInfo((L2Npc) this, player));
						}
						player.sendPacket(new ExNpcSpeedInfo((L2Npc) this));
					}
				}
			}
			else if (su != null)
			{
				broadcastPacket(su);
			}
		}
		else if (su != null)
		{
			broadcastPacket(su);
		}
	}

	/**
	 * Return the orientation of the L2Character.<BR><BR>
	 */
	public final int getHeading()
	{
		return _heading;
	}

	/**
	 * Set the orientation of the L2Character.<BR><BR>
	 */
	public final void setHeading(int heading)
	{
		_heading = heading;
	}

	public final int getXdestination()
	{
		MoveData m = _move;

		if (m != null)
		{
			return m._xDestination;
		}

		return getX();
	}

	/**
	 * Return the Y destination of the L2Character or the Y position if not in movement.<BR><BR>
	 */
	public final int getYdestination()
	{
		MoveData m = _move;

		if (m != null)
		{
			return m._yDestination;
		}

		return getY();
	}

	/**
	 * Return the Z destination of the L2Character or the Z position if not in movement.<BR><BR>
	 */
	public final int getZdestination()
	{
		MoveData m = _move;

		if (m != null)
		{
			return m._zDestination;
		}

		return getZ();
	}

	/**
	 * Return True if the L2Character is in combat.<BR><BR>
	 */
	public boolean isInCombat()
	{
		return hasAI() && (getAI().getAttackTarget() != null || getAI().isAutoAttacking());
	}

	/**
	 * Return True if the L2Character is moving.<BR><BR>
	 */
	public final boolean isMoving()
	{
		return _move != null;
	}

	/**
	 * Return True if the L2Character is travelling a calculated path.<BR><BR>
	 */
	public final boolean isOnGeodataPath()
	{
		MoveData m = _move;
		if (m == null)
		{
			return false;
		}
		if (m.onGeodataPathIndex == -1)
		{
			return false;
		}
		return m.onGeodataPathIndex != m.geoPath.size() - 1;
	}

	/**
	 * Return True if the L2Character is casting.<BR><BR>
	 */
	public final boolean isCastingNow()
	{
		if (canDoubleCast())
		{
			return _isCastingNow || _isCastingNow2;
		}

		return _isCastingNow;
	}

	public boolean isCastingNow1()
	{
		return _isCastingNow;
	}

	public final boolean canCastNow(L2Skill skill)
	{
		// Horrible hotfix
		/*if (!canDoubleCast() && _isCastingNow
				&& (_skillCast == null || _skillCast.isDone()))
		{
			System.out.println(getName() + "'s cast fixed by the horrible hotfix");
			System.out.println("The fixing cast is " + skill);
			System.out.println("His last cast was " + _skillCast);
			if (_skillCast != null)
				System.out.println("Cancelled " + _skillCast.isCancelled());
			//_isCastingNow = false;
		}*/

		if (canDoubleCast() && skill.isElemental())
		{
			return !_isCastingNow || !_isCastingNow2;
		}

		return !_isCastingNow;
	}

	private boolean _lastCast1;

	public final boolean wasLastCast1()
	{
		return _lastCast1;
	}

	public void setIsCastingNow(boolean value)
	{
		_isCastingNow = value;
		_lastCast1 = true;
	}

	public void setIsCastingNow2(boolean value)
	{
		_isCastingNow2 = value;
		_lastCast1 = !value;
	}

	public final boolean isCastingSimultaneouslyNow()
	{
		return _isCastingSimultaneouslyNow;
	}

	public void setIsCastingSimultaneouslyNow(boolean value)
	{
		_isCastingSimultaneouslyNow = value;
	}

	/**
	 * Return True if the cast of the L2Character can be aborted.<BR><BR>
	 */
	public final boolean canAbortCast()
	{
		return _castInterruptTime > TimeController.getGameTicks();
	}

	public int getCastInterruptTime()
	{
		return _castInterruptTime;
	}

	public boolean canDoubleCast()
	{
		return false;
	}

	/**
	 * Return True if the L2Character is attacking.<BR><BR>
	 */
	public boolean isAttackingNow()
	{
		return _attackEndTime > TimeController.getGameTicks();
	}

	/**
	 * Return True if the L2Character has aborted its attack.<BR><BR>
	 */
	public final boolean isAttackAborted()
	{
		return _attacking <= 0;
	}

	/**
	 * Abort the attack of the L2Character and send Server->Client ActionFailed packet.<BR><BR>
	 */
	public final void abortAttack()
	{
		if (isAttackingNow())
		{
			_attacking = 0;
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	/**
	 * Returns body part (paperdoll slot) we are targeting right now
	 */
	public final int getAttackingBodyPart()
	{
		return _attacking;
	}

	/**
	 * Abort the cast of the L2Character and send Server->Client MagicSkillCanceld/ActionFailed packet.<BR><BR>
	 */
	public final void abortCast()
	{
		if (isCastingNow() || isCastingSimultaneouslyNow())
		{
			Future<?> future = _skillCast;
			// cancels the skill hit scheduled task
			if (future != null)
			{
				future.cancel(true);
				_skillCast = null;
			}
			future = _skillCast2;
			if (future != null)
			{
				future.cancel(true);
				_skillCast2 = null;
			}
			future = _simultSkillCast;
			if (future != null)
			{
				future.cancel(true);
				_simultSkillCast = null;
			}

			if (getFusionSkill() != null)
			{
				getFusionSkill().onCastAbort();
			}

			if (getContinuousDebuffTargets() != null)
			{
				abortContinuousDebuff(getLastSkillCast());
			}

			L2Abnormal mog = getFirstEffect(L2AbnormalType.SIGNET_GROUND);
			if (mog != null)
			{
				mog.exit();
			}

			if (_allSkillsDisabled)
			{
				enableAllSkills(); // this remains for forced skill use, e.g. scroll of escape
			}
			setIsCastingNow(false);
			setIsCastingNow2(false);
			setIsCastingSimultaneouslyNow(false);
			// safeguard for cannot be interrupt any more
			_castInterruptTime = 0;
			if (this instanceof L2PcInstance)
			{
				getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING); // setting back previous intention
			}

			broadcastPacket(new MagicSkillCancelled(getObjectId())); // broadcast packet to stop animations client-side
			sendPacket(ActionFailed.STATIC_PACKET); // send an "action failed" packet to the caster
		}
	}

	/**
	 * Update the position of the L2Character during a movement and return True if the movement is finished.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>_move</B> of the L2Character.
	 * The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR><BR>
	 * <p>
	 * When the movement is started (ex : by MovetoLocation), this method will be called each 0.1 sec to estimate and update the L2Character position on the server.
	 * Note, that the current server position can differe from the current client position even if each movement is straight foward.
	 * That's why, client send regularly a Client->Server ValidatePosition packet to eventually correct the gap on the server.
	 * But, it's always the server position that is used in range calculation.<BR><BR>
	 * <p>
	 * At the end of the estimated movement time, the L2Character position is automatically set to the destination position even if the movement is not finished.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current Z position is obtained FROM THE CLIENT by the Client->Server ValidatePosition Packet.
	 * But x and y positions must be calculated to avoid that players try to modify their movement speed.</B></FONT><BR><BR>
	 *
	 * @param gameTicks Nb of ticks since the server start
	 * @return True if the movement is finished
	 */
	public boolean updatePosition(int gameTicks)
	{
		// Get movement data
		MoveData m = _move;

		if (m == null)
		{
			return true;
		}

		if (!isVisible())
		{
			_move = null;
			return true;
		}

		// Check if this is the first update
		if (m._moveTimestamp == 0)
		{
			m._moveTimestamp = m._moveStartTime;
			m._xAccurate = getX();
			m._yAccurate = getY();
		}

		// Check if the position has already been calculated
		if (m._moveTimestamp >= gameTicks)
		{
			return false;
		}

		int xPrev = getX();
		int yPrev = getY();
		int zPrev = getZ(); // the z coordinate may be modified by coordinate synchronizations

		double dx, dy, dz;
		if (Config.COORD_SYNCHRONIZE == 1)
		// the only method that can modify x,y while moving (otherwise _move would/should be set null)
		{
			dx = m._xDestination - xPrev;
			dy = m._yDestination - yPrev;
		}
		else
		// otherwise we need saved temporary values to avoid rounding errors
		{
			dx = m._xDestination - m._xAccurate;
			dy = m._yDestination - m._yAccurate;
		}

		final boolean isFloating = isFlying() || isInsideZone(L2Character.ZONE_WATER);

		// Z coordinate will follow geodata or client values
		if (Config.GEODATA > 0 && Config.COORD_SYNCHRONIZE == 2 && !isFloating && !m.disregardingGeodata &&
				TimeController.getGameTicks() % 10 == 0 && GeoData.getInstance().hasGeo(xPrev, yPrev))
		{
			short geoHeight = GeoData.getInstance().getSpawnHeight(xPrev, yPrev, zPrev - 30, zPrev + 30, null);
			dz = m._zDestination - geoHeight;
			// quite a big difference, compare to validatePosition packet
			if (this instanceof L2PcInstance && Math.abs(((L2PcInstance) this).getClientZ() - geoHeight) > 200 &&
					Math.abs(((L2PcInstance) this).getClientZ() - geoHeight) < 1500)
			{
				dz = m._zDestination - zPrev; // allow diff
			}
			else if (isInCombat() && Math.abs(dz) > 200 &&
					dx * dx + dy * dy < 40000) // allow mob to climb up to pcinstance
			{
				dz = m._zDestination - zPrev; // climbing
			}
			else
			{
				zPrev = geoHeight;
			}
		}
		else
		{
			dz = m._zDestination - zPrev;
		}

		double delta = dx * dx + dy * dy;
		if (delta < 10000 && dz * dz > 2500
				// close enough, allows error between client and server geodata if it cannot be avoided
				&& !isFloating) // should not be applied on vertical movements in water or during flight
		{
			delta = Math.sqrt(delta);
		}
		else
		{
			delta = Math.sqrt(delta + dz * dz);
		}

		double distFraction = Double.MAX_VALUE;
		if (delta > 1)
		{
			final double distPassed =
					getStat().getMoveSpeed() * (gameTicks - m._moveTimestamp) / TimeController.TICKS_PER_SECOND;
			distFraction = distPassed / delta;
		}

		// if (Config.DEVELOPER) Logozo.warning("Move Ticks:" + (gameTicks - m._moveTimestamp) + ", distPassed:" + distPassed + ", distFraction:" + distFraction);

		if (distFraction > 1) // already there
		{
			// Set the position of the L2Character to the destination
			super.getPosition().setXYZ(m._xDestination, m._yDestination, m._zDestination);
			//broadcastPacket(new ValidateLocation(this));
		}
		else
		{
			m._xAccurate += dx * distFraction;
			m._yAccurate += dy * distFraction;

			// Set the position of the L2Character to estimated after parcial move
			super.getPosition().setXYZ((int) m._xAccurate, (int) m._yAccurate, zPrev + (int) (dz * distFraction + 0.5));
		}
		revalidateZone(false);

		// Set the timer of last position update to now
		m._moveTimestamp = gameTicks;

		return distFraction > 1;
	}

	public void revalidateZone(boolean force)
	{
		if (getWorldRegion() == null)
		{
			return;
		}

		// This function is called too often from movement code
		if (force)
		{
			_zoneValidateCounter = 4;
		}
		else
		{
			_zoneValidateCounter--;
			if (_zoneValidateCounter < 0)
			{
				_zoneValidateCounter = 4;
			}
			else
			{
				return;
			}
		}

		getWorldRegion().revalidateZones(this);
	}

	/**
	 * Stop movement of the L2Character (Called by AI Accessor only).<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete movement data of the L2Character </li>
	 * <li>Set the current position (x,y,z), its current L2WorldRegion if necessary and its heading </li>
	 * <li>Remove the L2Object object from _gmList** of GmListTable </li>
	 * <li>Remove object from _knownObjects and _knownPlayer* of all surrounding L2WorldRegion L2Characters </li><BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T send Server->Client packet StopMove/StopRotation </B></FONT><BR><BR>
	 */
	public void stopMove(L2CharPosition pos)
	{
		stopMove(pos, false);
	}

	public void stopMove(L2CharPosition pos, boolean updateKnownObjects)
	{
		// Delete movement data of the L2Character
		_move = null;

		//if (getAI() != null)
		//  getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

		// Set the current position (x,y,z), its current L2WorldRegion if necessary and its heading
		// All data are contained in a L2CharPosition object
		if (pos != null)
		{
			getPosition().setXYZ(pos.x, pos.y, pos.z);
			setHeading(pos.heading);
			revalidateZone(true);
		}
		broadcastPacket(new StopMove(this));
		if (Config.MOVE_BASED_KNOWNLIST && updateKnownObjects)
		{
			getKnownList().findObjects();
		}
	}

	/**
	 * @return Returns the showSummonAnimation.
	 */
	public boolean isShowSummonAnimation()
	{
		return _showSummonAnimation;
	}

	/**
	 * @param showSummonAnimation The showSummonAnimation to set.
	 */
	public void setShowSummonAnimation(boolean showSummonAnimation)
	{
		_showSummonAnimation = showSummonAnimation;
	}

	/**
	 * Target a L2Object (add the target to the L2Character _target, _knownObject and L2Character to _KnownObject of the L2Object).<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * The L2Object (including L2Character) targeted is identified in <B>_target</B> of the L2Character<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the _target of L2Character to L2Object </li>
	 * <li>If necessary, add L2Object to _knownObject of the L2Character </li>
	 * <li>If necessary, add L2Character to _KnownObject of the L2Object </li>
	 * <li>If object==null, cancel Attak or Cast </li><BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Remove the L2PcInstance from the old target _statusListener and add it to the new target if it was a L2Character</li><BR><BR>
	 *
	 * @param object L2object to target
	 */
	public void setTarget(L2Object object)
	{
		if (object != null && !object.isVisible())
		{
			object = null;
		}

		if (object != null && object != _target)
		{
			getKnownList().addKnownObject(object);
			object.getKnownList().addKnownObject(this);
		}

		_target = object;
	}

	/**
	 * Return the identifier of the L2Object targeted or -1.<BR><BR>
	 */
	public final int getTargetId()
	{
		if (_target != null)
		{
			return _target.getObjectId();
		}

		return -1;
	}

	/**
	 * Return the L2Object targeted or null.<BR><BR>
	 */
	public final L2Object getTarget()
	{
		return _target;
	}

	// called from AIAccessor only

	/**
	 * Calculate movement data for a move to location action and add the L2Character to movingObjects of GameTimeController (only called by AI Accessor).<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>_move</B> of the L2Character.
	 * The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR><BR>
	 * All L2Character in movement are identified in <B>movingObjects</B> of GameTimeController that will call the updatePosition method of those L2Character each 0.1s.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get current position of the L2Character </li>
	 * <li>Calculate distance (dx,dy) between current position and destination including offset </li>
	 * <li>Create and Init a MoveData object </li>
	 * <li>Set the L2Character _move object to MoveData object </li>
	 * <li>Add the L2Character to movingObjects of the GameTimeController </li>
	 * <li>Create a task to notify the AI that L2Character arrives at a check point of the movement </li><BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T send Server->Client packet MoveToPawn/CharMoveToLocation </B></FONT><BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> AI : onIntentionMoveTo(L2CharPosition), onIntentionPickUp(L2Object), onIntentionInteract(L2Object) </li>
	 * <li> FollowTask </li><BR><BR>
	 *
	 * @param x      The X position of the destination
	 * @param y      The Y position of the destination
	 * @param z      The Y position of the destination
	 * @param offset The size of the interaction area of the L2Character targeted
	 */
	protected void moveToLocation(int x, int y, int z, int offset)
	{
		// Get the Move Speed of the L2Charcater
		float speed = getStat().getMoveSpeed();
		if (speed <= 0 || isMovementDisabled())
		{
			return;
		}

		if (getFirstEffect(L2AbnormalType.SPATIAL_TRAP) != null)
		{
			// We're expecting the first effect in the array to be the SpatialTrap effect... F. IT.
			EffectSpatialTrap st = (EffectSpatialTrap) getFirstEffect(L2AbnormalType.SPATIAL_TRAP).getEffects()[0];

			float vecX = x - st.getTrapX();
			float vecY = y - st.getTrapY();

			double dist = Math.sqrt(vecX * vecX + vecY * vecY);

			vecX /= dist;
			vecY /= dist;

			if (dist > 175 * 0.9f)
			{
				x = (int) (st.getTrapX() + vecX * 175 * 0.9f);
				y = (int) (st.getTrapY() + vecY * 175 * 0.9f);
			}
		}
		else if (isTransformed() && this instanceof L2PcInstance && getFirstEffect(11580) != null ||
				getFirstEffect(11537) != null)
		{
			x = getX() + Rnd.get(-250, 250);
			y = getY() + Rnd.get(-250, 250);
		}

		// Get current position of the L2Character
		final int curX = super.getX();
		final int curY = super.getY();
		final int curZ = super.getZ();

		// Calculate distance (dx,dy) between current position and destination
		// TODO: improve Z axis move/follow support when dx,dy are small compared to dz
		double dx = x - curX;
		double dy = y - curY;
		double dz = z - curZ;
		double distance = Math.sqrt(dx * dx + dy * dy);

		final boolean verticalMovementOnly = isFlying() && distance == 0 && dz != 0;
		if (verticalMovementOnly)
		{
			distance = Math.abs(dz);
		}

		// make water move short and use no geodata checks for swimming chars
		// distance in a click can easily be over 3000
		if (Config.GEODATA > 0 && isInsideZone(ZONE_WATER) && distance > 700)
		{
			double divider = 700 / distance;
			x = curX + (int) (divider * dx);
			y = curY + (int) (divider * dy);
			z = curZ + (int) (divider * dz);
			dx = x - curX;
			dy = y - curY;
			dz = z - curZ;
			distance = Math.sqrt(dx * dx + dy * dy);
		}

		if (Config.DEBUG)
		{
			Log.fine("distance to target:" + distance);
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

		double cos;
		double sin;

		// Check if a movement offset is defined or no distance to go through
		if (offset > 0 || distance < 1)
		{
			// approximation for moving closer when z coordinates are different
			// TODO: handle Z axis movement better
			offset -= Math.abs(dz);
			if (offset < 5)
			{
				offset = 5;
			}

			// If no distance to go through, the movement is canceled
			if (distance < 1 || distance - offset <= 0)
			{
				if (Config.DEBUG)
				{
					Log.fine("already in range, no movement needed.");
				}

				// Notify the AI that the L2Character is arrived at destination
				getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);

				return;
			}
			// Calculate movement angles needed
			sin = dy / distance;
			cos = dx / distance;

			distance -= offset - 5; // due to rounding error, we have to move a bit closer to be in range

			// Calculate the new destination with offset included
			x = curX + (int) (distance * cos);
			y = curY + (int) (distance * sin);
		}
		else
		{
			// Calculate movement angles needed
			sin = dy / distance;
			cos = dx / distance;
		}

		// Create and Init a MoveData object
		MoveData m = new MoveData();

		// GEODATA MOVEMENT CHECKS AND PATHFINDING
		m.onGeodataPathIndex = -1; // Initialize not on geodata path
		m.disregardingGeodata = false;

		if (Config.GEODATA > 0 && !isFlying() // flying chars not checked - even canSeeTarget doesn't work yet
				&& (!isInsideZone(ZONE_WATER) ||
				isInsideZone(ZONE_SIEGE))) // swimming also not checked unless in siege zone - but distance is limited
		//&& !(this instanceof L2NpcWalkerInstance)) // npc walkers not checked
		{
			final boolean isInVehicle = this instanceof L2PcInstance && ((L2PcInstance) this).getVehicle() != null;
			if (isInVehicle)
			{
				m.disregardingGeodata = true;
			}

			double originalDistance = distance;
			int originalX = x;
			int originalY = y;
			int originalZ = z;
			int gtx = originalX - L2World.MAP_MIN_X >> 4;
			int gty = originalY - L2World.MAP_MIN_Y >> 4;

			// Movement checks:
			// when geodata == 2, for all characters except mobs returning home (could be changed later to teleport if pathfinding fails)
			// when geodata == 1, for l2playableinstance and l2riftinstance only
			if (Config.GEODATA == 2 &&
					!(this instanceof L2Attackable && ((L2Attackable) this).isReturningToSpawnPoint()) ||
					this instanceof L2PcInstance && !(isInVehicle && distance > 1500)
					//|| (this instanceof L2Summon && !(getAI().getIntention() == AI_INTENTION_FOLLOW)) // assuming intention_follow only when following owner
					|| isAfraid() || isInLove())
			{
				if (isOnGeodataPath())
				{
					try
					{
						if (gtx == _move.geoPathGtx && gty == _move.geoPathGty)
						{
							return;
						}
						else
						{
							_move.onGeodataPathIndex = -1; // Set not on geodata path
						}
					}
					catch (NullPointerException e)
					{
						// nothing
					}
				}

				if (curX < L2World.MAP_MIN_X || curX > L2World.MAP_MAX_X || curY < L2World.MAP_MIN_Y ||
						curY > L2World.MAP_MAX_Y)
				{
					// Temporary fix for character outside world region errors
					Log.warning(
							"Character " + getName() + " outside world area, in coordinates x:" + curX + " y:" + curY);
					getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					if (this instanceof L2PcInstance)
					{
						((L2PcInstance) this).logout();
					}
					else if (this instanceof L2Summon)
					{
						return; // preventation when summon get out of world coords, player will not loose him, unsummon handled from pcinstance
					}
					else
					{
						onDecay();
					}
					return;
				}
				Location destiny = GeoData.getInstance().moveCheck(curX, curY, curZ, x, y, z, getInstanceId());
				// location different if destination wasn't reached (or just z coord is different)
				x = destiny.getX();
				y = destiny.getY();
				z = destiny.getZ();
				dx = x - curX;
				dy = y - curY;
				dz = z - curZ;
				distance = verticalMovementOnly ? Math.abs(dz * dz) : Math.sqrt(dx * dx + dy * dy);
			}

			// Pathfinding checks. Only when geodata setting is 2, the LoS check gives shorter result
			// than the original movement was and the LoS gives a shorter distance than 2000
			// This way of detecting need for pathfinding could be changed.
			if (Config.GEODATA == 2 && originalDistance - distance > 30 && distance < 2000 && !isAfraid())
			{
				// Path calculation
				// Overrides previous movement check
				if (this instanceof L2Playable && !isInVehicle || isMinion() || isInCombat() ||
						this instanceof L2GuardInstance && getInstanceId() != 0) //TODO LasTravel
				{
					m.geoPath = PathFinding.getInstance()
							.findPath(curX, curY, curZ, originalX, originalY, originalZ, getInstanceId(),
									this instanceof L2Playable);
					if (m.geoPath == null || m.geoPath.size() < 2) // No path found
					{
						// * Even though there's no path found (remember geonodes aren't perfect),
						// the mob is attacking and right now we set it so that the mob will go
						// after target anyway, is dz is small enough.
						// * With cellpathfinding this approach could be changed but would require taking
						// off the geonodes and some more checks.
						// * Summons will follow their masters no matter what.
						// * Currently minions also must move freely since L2AttackableAI commands
						// them to move along with their leader
						if (this instanceof L2PcInstance ||
								!(this instanceof L2Playable) && !isMinion() && Math.abs(z - curZ) > 140 ||
								this instanceof L2Summon && !((L2Summon) this).getFollowStatus())
						{
							getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
							return;
						}
						else
						{
							m.disregardingGeodata = true;
							x = originalX;
							y = originalY;
							z = originalZ;
							distance = originalDistance;
						}
					}
					else
					{
						m.onGeodataPathIndex = 0; // on first segment
						m.geoPathGtx = gtx;
						m.geoPathGty = gty;
						m.geoPathAccurateTx = originalX;
						m.geoPathAccurateTy = originalY;

						x = m.geoPath.get(m.onGeodataPathIndex).getX();
						y = m.geoPath.get(m.onGeodataPathIndex).getY();
						z = m.geoPath.get(m.onGeodataPathIndex).getZ();

						// check for doors in the route
						if (DoorTable.getInstance().checkIfDoorsBetween(curX, curY, curZ, x, y, z, getInstanceId()))
						{
							m.geoPath = null;
							getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
							return;
						}
						for (int i = 0; i < m.geoPath.size() - 1; i++)
						{
							if (DoorTable.getInstance()
									.checkIfDoorsBetween(m.geoPath.get(i), m.geoPath.get(i + 1), getInstanceId()))
							{
								m.geoPath = null;
								getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
								return;
							}
						}

						dx = x - curX;
						dy = y - curY;
						dz = z - curZ;
						distance = verticalMovementOnly ? Math.abs(dz * dz) : Math.sqrt(dx * dx + dy * dy);
						sin = dy / distance;
						cos = dx / distance;
					}
				}
			}
			// If no distance to go through, the movement is canceled
			if (distance < 1 && (Config.GEODATA == 2 || this instanceof L2Playable || isAfraid()))
			{
				if (this instanceof L2Summon)
				{
					((L2Summon) this).setFollowStatus(false);
				}
				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				return;
			}
		}

		// Apply Z distance for flying or swimming for correct timing calculations
		if ((isFlying() || isInsideZone(ZONE_WATER)) && !verticalMovementOnly)
		{
			distance = Math.sqrt(distance * distance + dz * dz);
		}

		// Caclulate the Nb of ticks between the current position and the destination
		// One tick added for rounding reasons
		int ticksToMove = 1 + (int) (TimeController.TICKS_PER_SECOND * distance / speed);
		m._xDestination = x;
		m._yDestination = y;
		m._zDestination = z; // this is what was requested from client

		// Calculate and set the heading of the L2Character
		m._heading = 0; // initial value for coordinate sync
		// Does not broke heading on vertical movements
		if (!verticalMovementOnly)
		{
			setHeading(Util.calculateHeadingFrom(cos, sin));
		}

		if (Config.DEBUG)
		{
			Log.fine("dist:" + distance + "speed:" + speed + " ttt:" + ticksToMove + " heading:" + getHeading());
		}

		m._moveStartTime = TimeController.getGameTicks();

		// Set the L2Character _move object to MoveData object
		_move = m;

		// Adding 2 ticks to fight ping a bit
		if (isOnGeodataPath())
		{
			m._moveStartTime += 2;
		}

		// Add the L2Character to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		TimeController.getInstance().registerMovingObject(this);

		// Create a task to notify the AI that L2Character arrives at a check point of the movement
		if (ticksToMove * TimeController.MILLIS_IN_TICK > 3000)
		{
			ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);
		}

		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive
		// to destination by GameTimeController
	}

	public boolean moveToNextRoutePoint()
	{
		if (!isOnGeodataPath())
		{
			// Cancel the move action
			_move = null;
			return false;
		}

		// Get the Move Speed of the L2Charcater
		float speed = getStat().getMoveSpeed();
		if (speed <= 0 || isMovementDisabled())
		{
			// Cancel the move action
			_move = null;
			return false;
		}

		MoveData md = _move;
		if (md == null)
		{
			return false;
		}

		// Create and Init a MoveData object
		MoveData m = new MoveData();

		// Update MoveData object
		m.onGeodataPathIndex = md.onGeodataPathIndex + 1; // next segment
		m.geoPath = md.geoPath;
		m.geoPathGtx = md.geoPathGtx;
		m.geoPathGty = md.geoPathGty;
		m.geoPathAccurateTx = md.geoPathAccurateTx;
		m.geoPathAccurateTy = md.geoPathAccurateTy;

		if (md.onGeodataPathIndex == md.geoPath.size() - 2)
		{
			m._xDestination = md.geoPathAccurateTx;
			m._yDestination = md.geoPathAccurateTy;
			m._zDestination = md.geoPath.get(m.onGeodataPathIndex).getZ();
		}
		else
		{
			m._xDestination = md.geoPath.get(m.onGeodataPathIndex).getX();
			m._yDestination = md.geoPath.get(m.onGeodataPathIndex).getY();
			m._zDestination = md.geoPath.get(m.onGeodataPathIndex).getZ();
		}
		double dx = m._xDestination - super.getX();
		double dy = m._yDestination - super.getY();
		double distance = Math.sqrt(dx * dx + dy * dy);
		// Calculate and set the heading of the L2Character
		if (distance != 0)
		{
			setHeading(Util.calculateHeadingFrom(getX(), getY(), m._xDestination, m._yDestination));
		}

		// Caclulate the Nb of ticks between the current position and the destination
		// One tick added for rounding reasons
		int ticksToMove = 1 + (int) (TimeController.TICKS_PER_SECOND * distance / speed);

		m._heading = 0; // initial value for coordinate sync

		m._moveStartTime = TimeController.getGameTicks();

		if (Config.DEBUG)
		{
			Log.fine("time to target:" + ticksToMove);
		}

		// Set the L2Character _move object to MoveData object
		_move = m;

		// Add the L2Character to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		TimeController.getInstance().registerMovingObject(this);

		// Create a task to notify the AI that L2Character arrives at a check point of the movement
		if (ticksToMove * TimeController.MILLIS_IN_TICK > 3000)
		{
			ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);
		}

		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive
		// to destination by GameTimeController

		// Send a Server->Client packet CharMoveToLocation to the actor and all L2PcInstance in its _knownPlayers
		MoveToLocation msg = new MoveToLocation(this);
		broadcastPacket(msg);

		return true;
	}

	public boolean validateMovementHeading(int heading)
	{
		MoveData m = _move;

		if (m == null)
		{
			return true;
		}

		boolean result = true;
		if (m._heading != heading)
		{
			result = m._heading == 0; // initial value or false
			m._heading = heading;
		}

		return result;
	}

	/**
	 * Return the distance between the current position of the L2Character and the target (x,y).<BR><BR>
	 *
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @return the plan distance
	 * @deprecated use getPlanDistanceSq(int x, int y, int z)
	 */
	@Deprecated
	public final double getDistance(int x, int y)
	{
		double dx = x - getX();
		double dy = y - getY();

		return Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * Return the distance between the current position of the L2Character and the target (x,y).<BR><BR>
	 *
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @return the plan distance
	 * @deprecated use getPlanDistanceSq(int x, int y, int z)
	 */
	@Deprecated
	public final double getDistance(int x, int y, int z)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();

		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/**
	 * Return the squared distance between the current position of the L2Character and the given object.<BR><BR>
	 *
	 * @param object L2Object
	 * @return the squared distance
	 */
	public final double getDistanceSq(L2Object object)
	{
		return getDistanceSq(object.getX(), object.getY(), object.getZ());
	}

	/**
	 * Return the squared distance between the current position of the L2Character and the given x, y, z.<BR><BR>
	 *
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @param z Z position of the target
	 * @return the squared distance
	 */
	public final double getDistanceSq(int x, int y, int z)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();

		return dx * dx + dy * dy + dz * dz;
	}

	/**
	 * Return the squared plan distance between the current position of the L2Character and the given object.<BR>
	 * (check only x and y, not z)<BR><BR>
	 *
	 * @param object L2Object
	 * @return the squared plan distance
	 */
	public final double getPlanDistanceSq(L2Object object)
	{
		return getPlanDistanceSq(object.getX(), object.getY());
	}

	/**
	 * Return the squared plan distance between the current position of the L2Character and the given x, y, z.<BR>
	 * (check only x and y, not z)<BR><BR>
	 *
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @return the squared plan distance
	 */
	public final double getPlanDistanceSq(int x, int y)
	{
		double dx = x - getX();
		double dy = y - getY();

		return dx * dx + dy * dy;
	}

	/**
	 * Check if this object is inside the given radius around the given object. Warning: doesn't cover collision radius!<BR><BR>
	 *
	 * @param object      the target
	 * @param radius      the radius around the target
	 * @param checkZ      should we check Z axis also
	 * @param strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return true is the L2Character is inside the radius.
	 * @see l2server.gameserver.model.actor.L2Character#isInsideRadius(int x, int y, int z, int radius, boolean checkZ, boolean strictCheck)
	 */
	public final boolean isInsideRadius(L2Object object, int radius, boolean checkZ, boolean strictCheck)
	{
		return isInsideRadius(object.getX(), object.getY(), object.getZ(), radius, checkZ, strictCheck);
	}

	/**
	 * Check if this object is inside the given plan radius around the given point. Warning: doesn't cover collision radius!<BR><BR>
	 *
	 * @param x           X position of the target
	 * @param y           Y position of the target
	 * @param radius      the radius around the target
	 * @param strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return true is the L2Character is inside the radius.
	 */
	public final boolean isInsideRadius(int x, int y, int radius, boolean strictCheck)
	{
		return isInsideRadius(x, y, 0, radius, false, strictCheck);
	}

	/**
	 * Check if this object is inside the given radius around the given point.<BR><BR>
	 *
	 * @param x           X position of the target
	 * @param y           Y position of the target
	 * @param z           Z position of the target
	 * @param radius      the radius around the target
	 * @param checkZ      should we check Z axis also
	 * @param strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return true is the L2Character is inside the radius.
	 */
	public final boolean isInsideRadius(int x, int y, int z, int radius, boolean checkZ, boolean strictCheck)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();

		if (strictCheck)
		{
			if (checkZ)
			{
				return dx * dx + dy * dy + dz * dz < radius * radius;
			}
			else
			{
				return dx * dx + dy * dy < radius * radius;
			}
		}
		else
		{
			if (checkZ)
			{
				return dx * dx + dy * dy + dz * dz <= radius * radius;
			}
			else
			{
				return dx * dx + dy * dy <= radius * radius;
			}
		}
	}

	//	/**
	//	* event that is called when the destination coordinates are reached
	//	*/
	//	public void onTargetReached()
	//	{
	//	L2Character pawn = getPawnTarget();
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
	//	if (_currentAttackTask == null )//&& !isInCombat())
	//	{
	//	_currentAttackTask = ThreadPoolManager.getInstance().scheduleMed(new AttackTask(), 0);
	//	}
	//	else
	//	{
	//	Logozo.info("multiple attacks want to start in parallel. prevented.");
	//	}
	//	}
	//

	/**
	 * Set _attacking corresponding to Attacking Body part to CHEST.<BR><BR>
	 */
	public void setAttackingBodypart()
	{
		_attacking = Inventory.PAPERDOLL_CHEST;
	}

	/**
	 * Retun True if arrows are available.<BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 */
	protected boolean checkAndEquipArrows()
	{
		return true;
	}

	/**
	 * Retun True if bolts are available.<BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 */
	protected boolean checkAndEquipBolts()
	{
		return true;
	}

	/**
	 * Add Exp and Sp to the L2Character.<BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li>
	 * <li> L2PetInstance</li><BR><BR>
	 */
	public void addExpAndSp(long addToExp, long addToSp)
	{
		// Dummy method (overridden by players and pets)
	}

	/**
	 * Return the active weapon instance (always equiped in the right hand).<BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 */
	public abstract L2ItemInstance getActiveWeaponInstance();

	/**
	 * Return the active weapon item (always equiped in the right hand).<BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 */
	public abstract L2Weapon getActiveWeaponItem();

	/**
	 * Return the secondary weapon instance (always equiped in the left hand).<BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 */
	public abstract L2ItemInstance getSecondaryWeaponInstance();

	/**
	 * Return the secondary {@link L2Item} item (always equiped in the left hand).<BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 */
	public abstract L2Item getSecondaryWeaponItem();

	/**
	 * Manage hit process (called by Hit Task).<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance </li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary </li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...) </li><BR><BR>
	 *
	 * @param target   The L2Character targeted
	 * @param damage   Nb of HP to reduce
	 * @param crit     True if hit is critical
	 * @param miss     True if hit is missed
	 * @param soulshot True if SoulShot are charged
	 * @param shld     True if shield is efficient
	 */
	public void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, double soulshot, byte shld, boolean wasHeavyPunch)
	{
		// If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL
		// and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)
		if (target == null || isAlikeDead())
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}

		if (target.getFirstEffect(L2AbnormalType.SPALLATION) != null && !Util.checkIfInRange(130, this, target, false))
		{
			sendMessage("Your attack has been blocked.");

			target.sendMessage("You blocked an attack.");
			return;
		}

		if (this instanceof L2Npc && target.isAlikeDead() || target.isDead() ||
				!getKnownList().knowsObject(target) && !(this instanceof L2DoorInstance))
		{
			//getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);

			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (miss)
		{
			// Notify target AI
			if (target.hasAI())
			{
				target.getAI().notifyEvent(CtrlEvent.EVT_EVADED, this);
			}

			// ON_EVADED_HIT
			if (target.getChanceSkills() != null)
			{
				target.getChanceSkills().onEvadedHit(this);
			}
		}

		// Send message about damage/crit or miss
		sendDamageMessage(target, damage, false, crit, miss);

		// If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance
		if (!isAttackAborted())
		{
			// Check Raidboss attack
			// Character will be petrified if attacking a raid that's more
			// than 8 levels lower
			if (target.isRaid() && target.giveRaidCurse() && !Config.RAID_DISABLE_CURSE)
			{
				if (getLevel() > target.getLevel() + 8)
				{
					L2Skill skill = SkillTable.FrequentSkill.RAID_CURSE2.getSkill();

					if (skill != null)
					{
						abortAttack();
						abortCast();
						getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						skill.getEffects(target, this);
					}
					else
					{
						Log.warning("Skill 4515 at level 1 is missing in DP.");
					}

					damage = 0; // prevents messing up drop calculation
				}
			}

			// If L2Character target is a L2PcInstance, send a system message
			if (target instanceof L2PcInstance)
			{
				L2PcInstance enemy = (L2PcInstance) target;
				enemy.getAI().clientStartAutoAttack();

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

			if (!miss && damage > 0)
			{
				int reflectedDamage = 0;

				if (!target.isInvul(this)) // Do not reflect if target is invulnerable
				{
					// quick fix for no drop from raid if boss attack high-level char with damage reflection
					if (!target.isRaid() || getActingPlayer() == null ||
							getActingPlayer().getLevel() <= target.getLevel() + 8)
					{
						// Reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
						double reflectPercent = target.getStat().calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0, null, null);
						reflectPercent = getStat().calcStat(Stats.REFLECT_VULN, reflectPercent, null, null);

						if (reflectPercent > 0)
						{
							reflectedDamage = (int) (reflectPercent / 100. * Math.min(target.getCurrentHp(), damage));

							// Half the reflected damage for bows
							/*L2Weapon weaponItem = getActiveWeaponItem();
							if (weaponItem != null && (weaponItem.getItemType() == L2WeaponType.BOW
									 || weaponItem.getItemType() == L2WeaponType.CROSSBOW))
								reflectedDamage *= 0.5f;*/

							boolean defLimitReflects = true;

							if (target.getFirstEffect(10021) != null || target.getFirstEffect(10017) != null ||
									target.getSkillLevelHash(13524) != 0)
							{
								defLimitReflects = false;
							}

							if (defLimitReflects && reflectedDamage > target.getPDef(this))
							{
								reflectedDamage = target.getPDef(this);
							}

							int totalHealth = (int) (target.getCurrentHp() + target.getCurrentCp());

							if (totalHealth - damage <= 0)
							{
								reflectedDamage = 0;
							}

							//damage -= reflectedDamage;
						}
					}
				}

				// reduce targets HP
				target.reduceCurrentHp(damage, this, null);

				if (!wasHeavyPunch && !crit && this instanceof L2PcInstance)
				{
					final L2PcInstance player = (L2PcInstance) this;

					player.setLastPhysicalDamages(damage);
				}

				if (reflectedDamage > 0 && !(this instanceof L2PcInstance && ((L2PcInstance) this).isPlayingEvent() &&
						((L2PcInstance) this).getEvent().isType(EventType.StalkedSalkers))) //SS Events check
				{
					reduceCurrentHp(reflectedDamage, target, true, false, null);

					// Custom messages - nice but also more network load
					if (target instanceof L2PcInstance)
					{
						target.sendMessage("You reflected " + reflectedDamage + " damage.");
					}
					else if (target instanceof L2Summon)
					{
						((L2Summon) target).getOwner().sendMessage("Summon reflected " + reflectedDamage + " damage.");
					}

					if (this instanceof L2PcInstance)
					{
						this.sendMessage("Target reflected to you " + reflectedDamage + " damage.");
					}
					else if (this instanceof L2Summon)
					{
						((L2Summon) this).getOwner()
								.sendMessage("Target reflected to your summon " + reflectedDamage + " damage.");
					}
				}

				if (Rnd.get(100) < 20) // Absorb now acts as "trigger". Let's hardcode a 20% chance
				{
					// Absorb HP from the damage inflicted
					double absorbPercent = getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null);

					if (absorbPercent > 0)
					{
						int maxCanAbsorb = (int) (getMaxHp() - getCurrentHp());
						int absorbDamage = (int) (absorbPercent / 100. * damage);

						if (absorbDamage > maxCanAbsorb)
						{
							absorbDamage = maxCanAbsorb; // Can't absorb more than max hp
						}

						if (absorbDamage > 0)
						{
							getStatus().setCurrentHp(getCurrentHp() + absorbDamage, true, target,
									StatusUpdateDisplay.NORMAL);
							sendMessage("You absorbed " + absorbDamage + " HP from " + target.getName() + ".");
						}
					}

					// Absorb MP from the damage inflicted
					absorbPercent = getStat().calcStat(Stats.ABSORB_MANA_DAMAGE_PERCENT, 0, null, null);

					if (absorbPercent > 0)
					{
						int maxCanAbsorb = (int) (getMaxMp() - getCurrentMp());
						int absorbDamage = (int) (absorbPercent / 100. * damage);

						if (absorbDamage > maxCanAbsorb)
						{
							absorbDamage = maxCanAbsorb; // Can't absord more than max hp
						}

						if (absorbDamage > 0)
						{
							setCurrentMp(getCurrentMp() + absorbDamage);
						}
					}
				}

				// Notify AI with EVT_ATTACKED
				if (target.hasAI())
				{
					target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
				}
				getAI().clientStartAutoAttack();

				if (target.isStunned() && Rnd.get(100) < (crit ? 75 : 10))
				{
					target.stopStunning(true);
				}

				//Summon part
				if (target instanceof L2PcInstance)
				{
					if (!((L2PcInstance) target).getSummons().isEmpty())
					{
						for (L2Summon _summon : ((L2PcInstance) target).getSummons())
						{
							if (_summon == null)
							{
								continue;
							}

							_summon.onOwnerGotAttacked(this);
						}
					}
				}

				if (target instanceof L2Summon)
				{
					if (((L2Summon) target).getOwner() != null)
					{
						if (!((L2Summon) target).getOwner().getSummons().isEmpty())
						{
							for (L2SummonInstance _summon : ((L2Summon) target).getOwner().getSummons())
							{
								if (_summon == null)
								{
									continue;
								}

								_summon.onOwnerGotAttacked(this);
							}
						}
					}
				}

				if (this instanceof L2Summon)
				{
					L2PcInstance owner = ((L2Summon) this).getOwner();
					if (owner != null)
					{
						owner.getAI().clientStartAutoAttack();
					}
				}

				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}

				// Maybe launch chance skills on us
				if (_chanceSkills != null && !wasHeavyPunch)
				{
					_chanceSkills.onHit(target, damage, false, false, crit);
					// Reflect triggers onHit
					if (reflectedDamage > 0)
					{
						_chanceSkills.onHit(target, damage, true, false, false);
					}
				}

				// Maybe launch chance skills on target
				if (target.getChanceSkills() != null)
				{
					target.getChanceSkills().onHit(this, damage, true, false, crit);
				}

				if (this instanceof L2SummonInstance &&
						((L2SummonInstance) this).getOwner().getChanceSkills() != null && reflectedDamage > 0)
				{
					((L2SummonInstance) this).getOwner().getChanceSkills().onHit(target, damage, true, true, false);
				}

				if (target instanceof L2SummonInstance &&
						((L2SummonInstance) target).getOwner().getChanceSkills() != null)
				{
					((L2SummonInstance) target).getOwner().getChanceSkills().onHit(this, damage, true, true, crit);
				}
			}

			// Launch weapon Special ability effect if available
			L2Weapon activeWeapon = getActiveWeaponItem();

			if (activeWeapon != null)
			{
				activeWeapon.getSkillEffects(this, target, crit);
			}

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
			L2ItemInstance weapon = getActiveWeaponInstance();

			if (!miss)
			{
				if (this instanceof L2Summon && !(this instanceof L2PetInstance))
				{
					if (((L2Summon)this).getChargedSoulShot() != L2ItemInstance.CHARGED_NONE)
						((L2Summon)this).setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
				}
				else
				{
					if (weapon != null && weapon.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE)
						weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
				}
			}
			 */

			return;
		}

		if (!isCastingNow() && !isCastingSimultaneouslyNow())
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
		}
	}

	/**
	 * Break an attack and send Server->Client ActionFailed packet and a System Message to the L2Character.<BR><BR>
	 */
	public void breakAttack()
	{
		if (isAttackingNow())
		{
			// Abort the attack of the L2Character and send Server->Client ActionFailed packet
			abortAttack();

			if (this instanceof L2PcInstance)
			{
				//TODO Remove sendPacket because it's always done in abortAttack
				sendPacket(ActionFailed.STATIC_PACKET);

				// Send a system message
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED));
			}
		}
	}

	/**
	 * Break a cast and send Server->Client ActionFailed packet and a System Message to the L2Character.<BR><BR>
	 */
	public void breakCast()
	{
		// damage can only cancel magical skills
		if (isCastingNow() && canAbortCast() && getLastSkillCast() != null && getLastSkillCast().isMagic())
		{
			// Abort the cast of the L2Character and send Server->Client MagicSkillCanceld/ActionFailed packet.
			abortCast();

			if (this instanceof L2PcInstance)
			{
				// Send a system message
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CASTING_INTERRUPTED));
			}
		}
	}

	/**
	 * Reduce the arrow number of the L2Character.<BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 */
	protected void reduceArrowCount(boolean bolts)
	{
		// default is to do nothing
	}

	/**
	 * Manage Forced attack (shift + select target).<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If L2Character or target is in a town area, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed </li>
	 * <li>If target is confused, send a Server->Client packet ActionFailed </li>
	 * <li>If L2Character is a L2ArtefactInstance, send a Server->Client packet ActionFailed </li>
	 * <li>Send a Server->Client packet MyTargetSelected to start attack and Notify AI with AI_INTENTION_ATTACK </li><BR><BR>
	 *
	 * @param player The L2PcInstance that attacks this character
	 */
	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		if (Config.isServer(Config.TENKAI) && getLevel() < player.getLevel() - 5 && this instanceof L2PcInstance &&
				((L2PcInstance) this).getPvpFlag() == 0 && !((L2PcInstance) this).isCombatFlagEquipped())
		{
			player.sendMessage("You can't attack lower level players.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (isInsidePeaceZone(player) && !player.isInDuel())
		{
			// If L2Character or target is in a peace zone, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (player.getEvent() != null && player.getEvent().onForcedAttack(player, getObjectId()))
		{
			player.sendMessage("You can't attack your team mates.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (player.isInOlympiadMode() && player.getTarget() != null && player.getTarget() instanceof L2Playable)
		{
			L2PcInstance target;
			if (player.getTarget() instanceof L2Summon)
			{
				target = ((L2Summon) player.getTarget()).getOwner();
			}
			else
			{
				target = (L2PcInstance) player.getTarget();
			}

			if (target == null || target.isInOlympiadMode() &&
					(!player.isOlympiadStart() || player.getOlympiadGameId() != target.getOlympiadGameId()))
			{
				// if L2PcInstance is in Olympiad and the match isn't already start, send a Server->Client packet ActionFailed
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}

		if (player.getTarget() != null && !player.getTarget().isAttackable() &&
				!player.getAccessLevel().allowPeaceAttack() && !(player.getTarget() instanceof L2NpcInstance))
		{
			// If target is not attackable, send a Server->Client packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (player.isConfused())
		{
			// If target is confused, send a Server->Client packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		// GeoData Los Check or dz > 1000
		if (!GeoData.getInstance().canSeeTarget(player, this))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SEE_TARGET));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (player.getBlockCheckerArena() != -1)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		// Notify AI with AI_INTENTION_ATTACK
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
	}

	/**
	 * Return True if inside peace zone.<BR><BR>
	 */
	public boolean isInsidePeaceZone(L2PcInstance attacker)
	{
		return isInsidePeaceZone(attacker, this);
	}

	public boolean isInsidePeaceZone(L2PcInstance attacker, L2Object target)
	{
		return !attacker.getAccessLevel().allowPeaceAttack() && isInsidePeaceZone((L2Object) attacker, target);
	}

	public boolean isInsidePeaceZone(L2Object attacker, L2Object target)
	{
		if (target == null)
		{
			return false;
		}

		if (this instanceof L2Playable && target instanceof L2Playable)
		{
			final L2PcInstance player = this.getActingPlayer();
			final L2PcInstance targetedPlayer = target.getActingPlayer();

			if (player.getDuelId() != 0 && player.getDuelId() == targetedPlayer.getDuelId())
			{
				return false;
			}
		}

		if (!(target instanceof L2Playable && attacker instanceof L2Playable))
		{
			return false;
		}

		if (getInstanceId() > 0)
		{
			Instance instance = InstanceManager.getInstance().getInstance(getInstanceId());

			if (instance != null)
			{
				if (instance.isPvPInstance())
				{
					return false;
				}

				if (instance.isPeaceInstance())
				{
					return true;
				}
			}
		}

		if (target instanceof L2PcInstance)
		{
			final L2PcInstance targetedPlayer = (L2PcInstance) target;
			if (targetedPlayer.isPlayingEvent())
			{
				return false;
			}
		}

		if (Config.ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE)
		{
			// allows red to be attacked and red to attack flagged players
			if (target.getActingPlayer() != null && target.getActingPlayer().getReputation() < 0)
			{
				return false;
			}
			if (attacker.getActingPlayer() != null && attacker.getActingPlayer().getReputation() < 0 &&
					target.getActingPlayer() != null && target.getActingPlayer().getPvpFlag() > 0)
			{
				return false;
			}

			if (attacker instanceof L2Character && target instanceof L2Character)
			{
				return ((L2Character) target).isInsideZone(ZONE_PEACE) ||
						((L2Character) attacker).isInsideZone(ZONE_PEACE);
			}
			if (attacker instanceof L2Character)
			{
				return TownManager.getTown(target.getX(), target.getY(), target.getZ()) != null ||
						((L2Character) attacker).isInsideZone(ZONE_PEACE);
			}
		}

		if (attacker instanceof L2Character && target instanceof L2Character)
		{
			return ((L2Character) target).isInsideZone(ZONE_PEACE) || ((L2Character) attacker).isInsideZone(ZONE_PEACE);
		}
		if (attacker instanceof L2Character)
		{
			return TownManager.getTown(target.getX(), target.getY(), target.getZ()) != null ||
					((L2Character) attacker).isInsideZone(ZONE_PEACE);
		}
		return TownManager.getTown(target.getX(), target.getY(), target.getZ()) != null ||
				TownManager.getTown(attacker.getX(), attacker.getY(), attacker.getZ()) != null;
	}

	/**
	 * return true if this character is inside an active grid.
	 */
	public boolean isInActiveRegion()
	{
		L2WorldRegion region = getWorldRegion();
		return region != null && region.isActive();
	}

	/**
	 * Return True if the L2Character has a Party in progress.<BR><BR>
	 */
	public boolean isInParty()
	{
		return false;
	}

	/**
	 * Return the L2Party object of the L2Character.<BR><BR>
	 */
	public L2Party getParty()
	{
		return null;
	}

	/**
	 * Return the Attack Speed of the L2Character (delay (in milliseconds) before next attack).<BR><BR>
	 */
	public int calculateTimeBetweenAttacks(L2Character target, L2Weapon weapon)
	{
		double atkSpd = 0;
		if (weapon != null && !isTransformed())
		{
			switch (weapon.getItemType())
			{
				case BOW:
					atkSpd = getStat().getPAtkSpd();
					return (int) (1500 * 345 / atkSpd);
				case CROSSBOW:
				case CROSSBOWK:
					atkSpd = getStat().getPAtkSpd();
					return (int) (1200 * 345 / atkSpd);
				case DAGGER:
					atkSpd = getStat().getPAtkSpd();
					//atkSpd /= 1.15;
					break;
				default:
					atkSpd = getStat().getPAtkSpd();
			}
		}
		else
		{
			atkSpd = getPAtkSpd();
		}

		return Formulas.calcPAtkSpd(this, target, atkSpd);
	}

	public int calculateReuseTime(L2Character target, L2Weapon weapon)
	{
		if (weapon == null || isTransformed())
		{
			return 0;
		}

		int reuse = weapon.getReuseDelay();
		// only bows should continue for now
		if (reuse == 0)
		{
			return 0;
		}

		reuse *= getStat().getWeaponReuseModifier(target);
		double atkSpd = getStat().getPAtkSpd();
		switch (weapon.getItemType())
		{
			case BOW:
			case CROSSBOW:
			case CROSSBOWK:
				return (int) (reuse * 345 / atkSpd);
			default:
				return (int) (reuse * 312 / atkSpd);
		}
	}

	/**
	 * Return True if the L2Character use a dual weapon.<BR><BR>
	 */
	public boolean isUsingDualWeapon()
	{
		return false;
	}

	/**
	 * Add a skill to the L2Character _skills and its Func objects to the calculator set of the L2Character.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Replace oldSkill by newSkill or Add the newSkill </li>
	 * <li>If an old skill has been replaced, remove all its Func objects of L2Character calculator set</li>
	 * <li>Add Func objects of newSkill to the calculator set of the L2Character </li><BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Save update in the character_skills table of the database</li><BR><BR>
	 *
	 * @param newSkill The L2Skill to add to the L2Character
	 * @return The L2Skill replaced or null if just added a new L2Skill
	 */
	public L2Skill addSkill(L2Skill newSkill)
	{
		L2Skill oldSkill = null;

		if (newSkill != null)
		{
			// Replace oldSkill by newSkill or Add the newSkill
			oldSkill = getSkills().put(newSkill.getId(), newSkill);

			// If an old skill has been replaced, remove all its Func objects
			if (oldSkill != null)
			{
				// if skill came with another one, we should delete the other one too.
				if (oldSkill.triggerAnotherSkill())
				{
					removeSkill(oldSkill.getTriggeredId(), true);
				}
				removeStatsOwner(oldSkill);
			}
			// Add Func objects of newSkill to the calculator set of the L2Character
			addStatFuncs(newSkill.getStatFuncs(this));

			if (oldSkill != null && _chanceSkills != null)
			{
				removeChanceSkill(oldSkill.getId());
			}
			if (newSkill.isChance())
			{
				addChanceTrigger(newSkill);
			}

			/*if (!newSkill.isChance() && newSkill.triggerAnotherSkill() )
			{
				L2Skill bestowed = SkillTable.getInstance().getInfo(newSkill.getTriggeredId(), newSkill.getTriggeredLevel());
				addSkill(bestowed);
				//bestowed skills are invisible for player. Visible for gm's looking thru gm window.
				//those skills should always be chance or passive, to prevent hlapex.
			}

			if (newSkill.isChance() && newSkill.triggerAnotherSkill())
			{
				L2Skill triggeredSkill = SkillTable.getInstance().getInfo(newSkill.getTriggeredId(),newSkill.getTriggeredLevel());
				addSkill(triggeredSkill);
			}*/
		}

		return oldSkill;
	}

	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the skill from the L2Character _skills </li>
	 * <li>Remove all its Func objects from the L2Character calculator set</li><BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Save update in the character_skills table of the database</li><BR><BR>
	 *
	 * @param skill The L2Skill to remove from the L2Character
	 * @return The L2Skill removed
	 */
	public L2Skill removeSkill(L2Skill skill)
	{
		if (skill == null)
		{
			return null;
		}

		return removeSkill(skill.getId(), true);
	}

	public L2Skill removeSkill(L2Skill skill, boolean cancelEffect)
	{
		if (skill == null)
		{
			return null;
		}

		// Remove the skill from the L2Character _skills
		return removeSkill(skill.getId(), cancelEffect);
	}

	public L2Skill removeSkill(int skillId)
	{
		return removeSkill(skillId, true);
	}

	public L2Skill removeSkill(int skillId, boolean cancelEffect)
	{
		// Remove the skill from the L2Character _skills
		L2Skill oldSkill = getSkills().remove(skillId);
		// Remove all its Func objects from the L2Character calculator set
		if (oldSkill != null)
		{
			//this is just a fail-safe againts buggers and gm dummies...
			if (oldSkill.triggerAnotherSkill() && oldSkill.getTriggeredId() > 0)
			{
				removeSkill(oldSkill.getTriggeredId(), true);
			}

			// does not abort casting of the transformation dispell
			if (oldSkill.getSkillType() != L2SkillType.TRANSFORMDISPEL)
			{
				// Stop casting if this skill is used right now
				if (getLastSkillCast() != null && isCastingNow())
				{
					if (oldSkill.getId() == getLastSkillCast().getId())
					{
						abortCast();
					}
				}
				if (getLastSimultaneousSkillCast() != null && isCastingSimultaneouslyNow())
				{
					if (oldSkill.getId() == getLastSimultaneousSkillCast().getId())
					{
						abortCast();
					}
				}
			}

			if (cancelEffect || oldSkill.isToggle())
			{
				// for now, to support transformations, we have to let their
				// effects stay when skill is removed
				L2Abnormal e = getFirstEffect(oldSkill);
				if (e == null || e.getType() != L2AbnormalType.MUTATE)
				{
					removeStatsOwner(oldSkill);
					stopSkillEffects(oldSkill.getId());
				}
			}

			if (oldSkill instanceof L2SkillAgathion && this instanceof L2PcInstance &&
					((L2PcInstance) this).getAgathionId() > 0)
			{
				((L2PcInstance) this).setAgathionId(0);
				((L2PcInstance) this).broadcastUserInfo();
			}

			if (oldSkill instanceof L2SkillMount && this instanceof L2PcInstance && ((L2PcInstance) this).isMounted())
			{
				((L2PcInstance) this).dismount();
			}

			if (oldSkill.isChance() && _chanceSkills != null)
			{
				removeChanceSkill(oldSkill.getId());
			}
			if (oldSkill instanceof L2SkillSummon && oldSkill.getId() == 710 && this instanceof L2PcInstance)
			{
				for (L2SummonInstance summon : ((L2PcInstance) this).getSummons())
				{
					if (summon.getNpcId() == 14870)
					{
						summon.unSummon((L2PcInstance) this);
					}
				}
			}
		}

		return oldSkill;
	}

	public void removeChanceSkill(int id)
	{
		if (_chanceSkills == null)
		{
			return;
		}

		synchronized (_chanceSkills)
		{
			for (IChanceSkillTrigger trigger : _chanceSkills.keySet())
			{
				if (!(trigger instanceof L2Skill))
				{
					continue;
				}
				if (((L2Skill) trigger).getId() == id)
				{
					_chanceSkills.remove(trigger);
				}
			}
		}
	}

	public void addChanceTrigger(IChanceSkillTrigger trigger)
	{
		if (_chanceSkills == null)
		{
			synchronized (this)
			{
				if (_chanceSkills == null)
				{
					_chanceSkills = new ChanceSkillList(this);
				}
			}
		}
		_chanceSkills.put(trigger, trigger.getTriggeredChanceCondition());
	}

	public void removeChanceEffect(EffectChanceSkillTrigger effect)
	{
		if (_chanceSkills == null)
		{
			return;
		}

		_chanceSkills.remove(effect);
	}

	public void onStartChanceEffect(L2Skill skill, byte element)
	{
		if (_chanceSkills == null)
		{
			return;
		}

		_chanceSkills.onStart(skill, element);
	}

	public void onActionTimeChanceEffect(L2Skill skill, byte element)
	{
		if (_chanceSkills == null)
		{
			return;
		}

		_chanceSkills.onActionTime(skill, element);
	}

	public void onExitChanceEffect(L2Skill skill, byte element)
	{
		if (_chanceSkills == null)
		{
			return;
		}

		_chanceSkills.onExit(skill, element);
	}

	/**
	 * Return all skills own by the L2Character in a table of L2Skill.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2Character are identified in <B>_skills</B> the L2Character <BR><BR>
	 */
	public L2Skill[] getAllSkills()
	{
		if (_skills == null)
		{
			return new L2Skill[0];
		}

		return _skills.values().toArray(new L2Skill[_skills.values().size()]);
	}

	public ChanceSkillList getChanceSkills()
	{
		return _chanceSkills;
	}

	/**
	 * Return the level of a skill owned by the L2Character.<BR><BR>
	 *
	 * @param skillId The identifier of the L2Skill whose level must be returned
	 * @return The level of the L2Skill identified by skillId
	 */
	public int getSkillLevelHash(int skillId)
	{
		if (skillId >= 1566 && skillId <= 1569 || skillId == 17192)
		{
			return 1;
		}

		final L2Skill skill = getKnownSkill(skillId);
		if (skill == null)
		{
			return -1;
		}

		return skill.getLevelHash();
	}

	public int getSkillLevel(int skillId)
	{
		final L2Skill skill = getKnownSkill(skillId);
		if (skill == null)
		{
			return -1;
		}

		return skill.getLevel();
	}

	/**
	 * Return True if the skill is known by the L2Character.<BR><BR>
	 *
	 * @param skillId The identifier of the L2Skill to check the knowledge
	 */
	public L2Skill getKnownSkill(int skillId)
	{
		if (_skills == null)
		{
			return null;
		}

		return _skills.get(skillId);
	}

	/**
	 * Return the number of buffs affecting this L2Character.<BR><BR>
	 *
	 * @return The number of Buffs affecting this L2Character
	 */
	public int getBuffCount()
	{
		return _effects.getBuffCount();
	}

	public int getDanceCount()
	{
		return _effects.getDanceCount();
	}

	/**
	 * Manage the magic skill launching task (MP, HP, Item consummation...) and display the magic skill animation on client.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet MagicSkillLaunched (to display magic skill animation) to all L2PcInstance of L2Charcater _knownPlayers</li>
	 * <li>Consumme MP, HP and Item if necessary</li>
	 * <li>Send a Server->Client packet StatusUpdate with MP modification to the L2PcInstance</li>
	 * <li>Launch the magic skill in order to calculate its effects</li>
	 * <li>If the skill type is PDAM, notify the AI of the target with AI_INTENTION_ATTACK</li>
	 * <li>Notify the AI of the L2Character with EVT_FINISH_CASTING</li><BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : A magic skill casting MUST BE in progress</B></FONT><BR><BR>
	 *
	 * @param mut The L2Skill to use
	 */
	public void onMagicLaunchedTimer(MagicUseTask mut)
	{
		final L2Skill skill = mut.skill;
		L2Object[] targets = mut.targets;

		if (skill == null || targets == null)
		{
			abortCast();
			return;
		}

		/*if (calcStat(Stats.SKILL_FAILURE_RATE, 0.0, null, skill) > Rnd.get(100))
		{
			abortCast();
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CASTING_INTERRUPTED));
			return;
		}*/

		if (targets.length == 0)
		{
			switch (skill.getTargetType())
			{
				// only AURA-type skills can be cast without target
				case TARGET_AURA:
				case TARGET_FRONT_AURA:
				case TARGET_BEHIND_AURA:
				case TARGET_GROUND_AREA:
					break;
				default:
				{
					if (!skill.isUseableWithoutTarget())
					{
						abortCast();
						return;
					}
				}
			}
		}

		// Escaping from under skill's radius and peace zone check. First version, not perfect in AoE skills.
		int escapeRange = 0;
		if (skill.getEffectRange() > escapeRange)
		{
			escapeRange = skill.getEffectRange();
		}
		else if (skill.getCastRange() < 0 && skill.getSkillRadius() > 80)
		{
			escapeRange = skill.getSkillRadius();
		}

		if (targets.length > 0 && escapeRange > 0)
		{
			int _skiprange = 0;
			int _skipgeo = 0;
			int _skippeace = 0;
			List<L2Character> targetList = new ArrayList<>(targets.length);
			for (L2Object target : targets)
			{
				if (target instanceof L2Character)
				{
					if (skill.getTargetDirection() != L2SkillTargetDirection.CHAIN_HEAL)
					{
						if (!Util.checkIfInRange(escapeRange, this, target, true))
						{
							_skiprange++;
							continue;
						}
						if (skill.getSkillRadius() > 0 && skill.isOffensive() && Config.GEODATA > 0 &&
								!GeoData.getInstance().canSeeTarget(this, target))
						{
							_skipgeo++;
							continue;
						}
						if (skill.isOffensive() && !skill.isNeutral())
						{
							if (this instanceof L2PcInstance)
							{
								if (((L2Character) target).isInsidePeaceZone((L2PcInstance) this))
								{
									_skippeace++;
									continue;
								}
							}
							else
							{
								if (((L2Character) target).isInsidePeaceZone(this, target))
								{
									_skippeace++;
									continue;
								}
							}
						}
					}
					targetList.add((L2Character) target);
				}
				//else
				//{
				//	if (Config.DEBUG)
				//		Logozo.warning("Class cast bad: "+targets[i].getClass().toString());
				//}
			}

			if (targetList.isEmpty())
			{
				if (this instanceof L2PcInstance)
				{
					if (_skiprange > 0)
					{
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED));
					}
					else if (_skipgeo > 0)
					{
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SEE_TARGET));
					}
					else if (_skippeace > 0)
					{
						sendPacket(SystemMessage
								.getSystemMessage(SystemMessageId.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_PEACE_ZONE));
					}
				}
				abortCast();
				return;
			}
			mut.targets = targetList.toArray(new L2Character[targetList.size()]);
		}

		// Ensure that a cast is in progress
		// Check if player is using fake death.
		// Potions can be used while faking death.
		if (mut.simultaneously && !isCastingSimultaneouslyNow() || !mut.simultaneously && !isCastingNow() ||
				isAlikeDead() && !skill.isPotion())
		{
			// now cancels both, simultaneous and normal
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}

		// Get the display identifier of the skill
		int magicId = skill.getDisplayId();

		// Get the level of the skill
		int level = getSkillLevelHash(skill.getId());

		if (level < 1)
		{
			level = 1;
		}

		// Send a Server->Client packet MagicSkillLaunched to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		if (!skill.isPotion())
		{
			broadcastPacket(new MagicSkillLaunched(this, magicId, level, targets));
		}

		mut.phase = 2;
		if (mut.hitTime == 0)
		{
			onMagicHitTimer(mut);
		}
		else if (mut.second)
		{
			_skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(mut, 400);
		}
		else
		{
			_skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, 400);
		}
	}

	/*
	 * Runs in the end of skill casting
	 */
	public void onMagicHitTimer(MagicUseTask mut)
	{
		final L2Skill skill = mut.skill;
		final L2Object[] targets = mut.targets;

		if (skill == null || !skill.isAuraAttack() && (targets == null || targets.length <= 0))
		{
			abortCast();
			return;
		}

		if (getFusionSkill() != null || mut.skill.getSkillType() == L2SkillType.CONTINUOUS_DEBUFF ||
				mut.skill.getSkillType() == L2SkillType.CONTINUOUS_DRAIN ||
				mut.skill.getSkillType() == L2SkillType.CONTINUOUS_CASTS)
		{
			if (mut.simultaneously)
			{
				_simultSkillCast = null;
				setIsCastingSimultaneouslyNow(false);
			}
			else if (mut.second)
			{
				_skillCast2 = null;
				setIsCastingNow2(false);
			}
			else
			{
				_skillCast = null;
				setIsCastingNow(false);
			}
			if (getFusionSkill() != null)
			{
				getFusionSkill().onCastAbort();
			}

			if (targets.length > 0)
			{
				notifyQuestEventSkillFinished(skill, targets[0]);
			}

			if (mut.skill.getSkillType() == L2SkillType.CONTINUOUS_DEBUFF)
			{
				abortContinuousDebuff(mut.skill);
			}

			return;
		}
		L2Abnormal mog = getFirstEffect(L2AbnormalType.SIGNET_GROUND);
		if (mog != null)
		{
			if (mut.simultaneously)
			{
				_simultSkillCast = null;
				setIsCastingSimultaneouslyNow(false);
			}
			else if (mut.second)
			{
				_skillCast2 = null;
				setIsCastingNow2(false);
			}
			else
			{
				_skillCast = null;
				setIsCastingNow(false);
			}
			mog.exit();
			notifyQuestEventSkillFinished(skill, targets[0]);
			return;
		}

		try
		{
			// Go through targets table
			for (L2Object tgt : targets)
			{
				if (tgt instanceof L2Playable)
				{
					L2Character target = (L2Character) tgt;

					if (skill.getSkillType() == L2SkillType.BUFF)
					{
						SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						smsg.addSkillName(skill);
						target.sendPacket(smsg);
					}

					if (this instanceof L2PcInstance && target instanceof L2Summon)
					{
						((L2Summon) target).updateAndBroadcastStatus(1);
					}
				}
			}

			StatusUpdate su = new StatusUpdate(this);
			boolean isSendStatus = false;

			// Consume MP of the L2Character and Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
			double mpConsume = getStat().getMpConsume(skill);

			if (mpConsume > 0)
			{
				if (mpConsume > getCurrentMp())
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_MP));
					abortCast();
					return;
				}

				getStatus().reduceMp(mpConsume);
				su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
				isSendStatus = true;
			}

			// Consume HP if necessary and Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
			if (skill.getHpConsume() > 0)
			{
				double consumeHp;

				consumeHp = calcStat(Stats.HP_CONSUME_RATE, skill.getHpConsume(), null, null);
				if (consumeHp + 1 >= getCurrentHp())
				{
					consumeHp = getCurrentHp() - 1.0;
				}

				getStatus().reduceHp(consumeHp, this, true);

				su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
				isSendStatus = true;
			}

			// Consume CP if necessary and Send the Server->Client packet StatusUpdate with current CP/HP and MP to all other L2PcInstance to inform
			if (skill.getCpConsume() > 0)
			{
				double consumeCp;

				consumeCp = skill.getCpConsume();
				if (consumeCp + 1 >= getCurrentHp())
				{
					consumeCp = getCurrentHp() - 1.0;
				}

				getStatus().reduceCp((int) consumeCp);
				su.addAttribute(StatusUpdate.CUR_CP, (int) getCurrentCp());
				isSendStatus = true;
			}

			// Send a Server->Client packet StatusUpdate with MP modification to the L2PcInstance
			if (isSendStatus)
			{
				sendPacket(su);
			}

			if (this instanceof L2PcInstance)
			{
				int charges = ((L2PcInstance) this).getCharges();
				// check for charges
				if (skill.getMaxCharges() == 0 && charges < skill.getNumCharges())
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
					sm.addSkillName(skill);
					sendPacket(sm);
					abortCast();
					return;
				}
				// generate charges if any
				if (skill.getNumCharges() > 0)
				{
					int maxCharges = skill.getMaxCharges();
					if (maxCharges == 15 && ((L2PcInstance) this).getClassId() != 152 &&
							((L2PcInstance) this).getClassId() != 155)
					{
						maxCharges = 10;
					}

					if (maxCharges > 0)
					{
						((L2PcInstance) this).increaseCharges(skill.getNumCharges(), maxCharges);
					}
					else
					{
						((L2PcInstance) this).decreaseCharges(skill.getNumCharges());
					}
				}

				// Consume Souls if necessary
				if (skill.getSoulConsumeCount() > 0 || skill.getMaxSoulConsumeCount() > 0)
				{
					if (!((L2PcInstance) this).decreaseSouls(
							skill.getSoulConsumeCount() > 0 ? skill.getSoulConsumeCount() :
									skill.getMaxSoulConsumeCount(), skill))
					{
						abortCast();
						return;
					}
				}
			}

			// On each repeat restore shots before cast
			if (mut.count > 0)
			{
				final L2ItemInstance weaponInst = getActiveWeaponInstance();
				if (weaponInst != null)
				{
					if (mut.skill.useSoulShot())
					{
						weaponInst.setChargedSoulShot(mut.shots);
					}
					else if (mut.skill.useSpiritShot())
					{
						weaponInst.setChargedSpiritShot(mut.shots);
					}
				}
			}

			// Launch the magic skill in order to calculate its effects
			callSkill(mut.skill, mut.targets);
		}
		catch (NullPointerException e)
		{
			Log.log(Level.WARNING, "", e);
		}

		if (mut.hitTime > 0)
		{
			mut.count++;
			if (mut.count < skill.getHitCounts())
			{
				int hitTime = mut.hitTime * skill.getHitTimings()[mut.count] / 100;
				if (mut.simultaneously)
				{
					_simultSkillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, hitTime);
				}
				else if (mut.second)
				{
					_skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(mut, hitTime);
				}
				else
				{
					_skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, hitTime);
				}
				return;
			}
		}

		mut.phase = 3;
		if (mut.hitTime == 0 || mut.coolTime == 0)
		{
			onMagicFinalizer(mut);
		}
		else
		{
			if (mut.simultaneously)
			{
				_simultSkillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, mut.coolTime);
			}
			else if (mut.second)
			{
				_skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(mut, mut.coolTime);
			}
			else
			{
				_skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, mut.coolTime);
			}
		}
	}

	/*
	 * Runs after skill hitTime+coolTime
	 */
	public void onMagicFinalizer(MagicUseTask mut)
	{
		if (mut.simultaneously)
		{
			_simultSkillCast = null;
			setIsCastingSimultaneouslyNow(false);
			return;
		}
		else if (mut.second)
		{
			_skillCast2 = null;
			setIsCastingNow2(false);
			_castInterruptTime = 0;
		}
		else
		{
			_skillCast = null;
			setIsCastingNow(false);
			_castInterruptTime = 0;
		}

		final L2Skill skill = mut.skill;
		final L2Object target = mut.targets.length > 0 ? mut.targets[0] : null;

		// Attack target after skill use
		if ((skill.nextActionIsAttack() || skill.nextActionIsAttackMob() && getTarget() instanceof L2Attackable) &&
				getTarget() != this && getTarget() == target)
		{
			if (getAI() == null || getAI().getNextIntention() == null ||
					getAI().getNextIntention().getCtrlIntention() != CtrlIntention.AI_INTENTION_MOVE_TO ||
					getAI().getNextIntention().getCtrlIntention() != CtrlIntention.AI_INTENTION_IDLE)
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
		if (skill.isOffensive() && !skill.isNeutral() && !(skill.getSkillType() == L2SkillType.UNLOCK) &&
				!(skill.getSkillType() == L2SkillType.DELUXE_KEY_UNLOCK))
		{
			getAI().clientStartAutoAttack();
		}

		// Notify the AI of the L2Character with EVT_FINISH_CASTING
		getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);

		notifyQuestEventSkillFinished(skill, target);

		/*
		 * If character is a player, then wipe their current cast state and
		 * check if a skill is queued.
		 *
		 * If there is a queued skill, launch it and wipe the queue.
		 */
		if (this instanceof L2PcInstance)
		{
			L2PcInstance currPlayer = (L2PcInstance) this;
			SkillDat queuedSkill = currPlayer.getQueuedSkill();

			currPlayer.setCurrentSkill(null, false, false);
			if (queuedSkill != null)
			{
				currPlayer.setQueuedSkill(null, false, false);

				// DON'T USE : Recursive call to useMagic() method
				// currPlayer.useMagic(queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed());
				ThreadPoolManager.getInstance().executeTask(
						new QueuedMagicUseTask(currPlayer, queuedSkill.getSkill(), queuedSkill.isCtrlPressed(),
								queuedSkill.isShiftPressed()));
			}
		}
	}

	// Quest event ON_SPELL_FNISHED
	protected void notifyQuestEventSkillFinished(L2Skill skill, L2Object target)
	{

	}

	public Map<Integer, Long> getDisabledSkills()
	{
		return _disabledSkills;
	}

	/**
	 * Enable a skill (remove it from _disabledSkills of the L2Character).<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR><BR>
	 *
	 * @param skill The L2Skill to enable
	 */
	public void enableSkill(L2Skill skill)
	{
		if (skill == null || _disabledSkills == null)
		{
			return;
		}

		_disabledSkills.remove(skill.getReuseHashCode());
	}

	/**
	 * Disable this skill id for the duration of the delay in milliseconds.
	 *
	 * @param skill
	 * @param delay (seconds * 1000)
	 */
	public void disableSkill(L2Skill skill, long delay)
	{
		if (skill == null)
		{
			return;
		}

		if (_disabledSkills == null)
		{
			_disabledSkills = Collections.synchronizedMap(new HashMap<Integer, Long>());
		}

		_disabledSkills.put(skill.getReuseHashCode(), delay > 10 ? System.currentTimeMillis() + delay : Long.MAX_VALUE);
	}

	/**
	 * Check if a skill is disabled.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills disabled are identified by their reuse hashcodes in <B>_disabledSkills</B> of the L2Character <BR><BR>
	 *
	 * @param skill The L2Skill to check
	 */
	public boolean isSkillDisabled(L2Skill skill)
	{
		if (skill == null)
		{
			return true;
		}

		boolean canCastWhileStun = false;

		switch (skill.getId())
		{
			case 30008: // Wind Blend
			case 19227: // Wind Blend Trigger
			case 30009: // Deceptive Blink
				canCastWhileStun = true;
				break;
			default:
				break;
		}

		if (!canCastWhileStun)
		{
			if (isAllSkillsDisabled() && !skill.canBeUsedWhenDisabled())
			{
				return true;
			}
		}

		return isSkillDisabled(skill.getReuseHashCode());
	}

	/**
	 * Check if a skill is disabled.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills disabled are identified by their reuse hashcodes in <B>_disabledSkills</B> of the L2Character <BR><BR>
	 *
	 * @param reuseHashcode The reuse hashcode of the skillId/level to check
	 */
	public boolean isSkillDisabled(int reuseHashcode)
	{
		if (_disabledSkills == null)
		{
			return false;
		}

		final Long timeStamp = _disabledSkills.get(reuseHashcode);
		if (timeStamp == null)
		{
			return false;
		}

		if (timeStamp < System.currentTimeMillis())
		{
			_disabledSkills.remove(reuseHashcode);
			return false;
		}

		return true;
	}

	/**
	 * Disable all skills (set _allSkillsDisabled to True).<BR><BR>
	 */
	public void disableAllSkills()
	{
		if (Config.DEBUG)
		{
			Log.fine("all skills disabled");
		}
		_allSkillsDisabled = true;
	}

	/**
	 * Enable all skills (set _allSkillsDisabled to False).<BR><BR>
	 */
	public void enableAllSkills()
	{
		if (Config.DEBUG)
		{
			Log.fine("all skills enabled");
		}
		_allSkillsDisabled = false;
	}

	/**
	 * Launch the magic skill and calculate its effects on each target contained in the targets table.<BR><BR>
	 *
	 * @param skill   The L2Skill to use
	 * @param targets The table of L2Object targets
	 */
	public void callSkill(L2Skill skill, L2Object[] targets)
	{
		try
		{
			// Get the skill handler corresponding to the skill type (PDAM, MDAM, SWEEP...) started in gameserver
			ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
			L2Weapon activeWeapon = getActiveWeaponItem();

			// Check if the toggle skill effects are already in progress on the L2Character
			if (skill.isToggle() && getFirstEffect(skill.getId()) != null)
			{
				return;
			}

			// Initial checks
			for (L2Object trg : targets)
			{
				if (trg instanceof L2Character)
				{
					// Set some values inside target's instance for later use
					L2Character target = (L2Character) trg;

					// Check Raidboss attack and
					// check buffing chars who attack raidboss. Results in mute.
					L2Character targetsAttackTarget = null;
					L2Character targetsCastTarget = null;
					if (target.hasAI())
					{
						targetsAttackTarget = target.getAI().getAttackTarget();
						targetsCastTarget = target.getAI().getCastTarget();
					}
					if (!Config.RAID_DISABLE_CURSE &&
							(target.isRaid() && target.giveRaidCurse() && getLevel() > target.getLevel() + 8 ||
									!skill.isOffensive() && targetsAttackTarget != null &&
											targetsAttackTarget.isRaid() && targetsAttackTarget.giveRaidCurse() &&
											targetsAttackTarget.getAttackByList().contains(target) // has attacked raid
											&& getLevel() > targetsAttackTarget.getLevel() + 8 ||
									!skill.isOffensive() && targetsCastTarget != null && targetsCastTarget.isRaid() &&
											targetsCastTarget.giveRaidCurse() &&
											targetsCastTarget.getAttackByList().contains(target) // has attacked raid
											&& getLevel() > targetsCastTarget.getLevel() + 8))
					{
						if (skill.isMagic())
						{
							L2Skill tempSkill = SkillTable.FrequentSkill.RAID_CURSE.getSkill();
							if (tempSkill != null)
							{
								abortAttack();
								abortCast();
								getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
								tempSkill.getEffects(target, this);
							}
							else if (Log.isLoggable(Level.WARNING))
							{
								Log.log(Level.WARNING, "Skill 4215 at level 1 is missing in DP.");
							}
						}
						else
						{
							L2Skill tempSkill = SkillTable.FrequentSkill.RAID_CURSE2.getSkill();
							if (tempSkill != null)
							{
								abortAttack();
								abortCast();
								getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
								tempSkill.getEffects(target, this);
							}
							else if (Log.isLoggable(Level.WARNING))
							{
								Log.log(Level.WARNING, "Skill 4515 at level 1 is missing in DP.");
							}
						}
						return;
					}

					// Check if over-hit is possible
					if (skill.isOverhit())
					{
						if (target instanceof L2Attackable)
						{
							((L2Attackable) target).overhitEnabled(true);
						}
					}

					// crafting does not trigger any chance skills
					// possibly should be unhardcoded
					switch (skill.getSkillType())
					{
						case COMMON_CRAFT:
						case DWARVEN_CRAFT:
							break;
						default:
							// Launch weapon Special ability skill effect if available
							if (activeWeapon != null && !target.isDead())
							{
								if (activeWeapon.getSkillEffects(this, target, skill).length > 0 &&
										this instanceof L2PcInstance)
								{
									SystemMessage sm =
											SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ACTIVATED);
									sm.addSkillName(skill);
									sendPacket(sm);
								}
							}

							// Maybe launch chance skills on us
							if (_chanceSkills != null)
							{
								_chanceSkills.onSkillHit(target, skill, false, false);
							}
							// Maybe launch chance skills on target
							if (target.getChanceSkills() != null)
							{
								target.getChanceSkills().onSkillHit(this, skill, false, true);
							}

							if (target instanceof L2SummonInstance &&
									((L2SummonInstance) target).getOwner().getChanceSkills() != null)
							{
								((L2SummonInstance) target).getOwner().getChanceSkills()
										.onSkillHit(this, skill, true, true);
							}
					}
				}
			}

			// Launch the magic skill and calculate its effects
			if (handler != null)
			{
				handler.useSkill(this, skill, targets);
			}
			else
			{
				skill.useSkill(this, targets);
			}

			L2PcInstance player = getActingPlayer();
			if (player != null)
			{
				for (L2Object target : targets)
				{
					// EVT_ATTACKED and PvPStatus
					if (target instanceof L2Character)
					{
						if (skill.isNeutral())
						{
							// no flags
						}
						else if (skill.isOffensive())
						{
							if (target instanceof L2PcInstance || target instanceof L2Summon ||
									target instanceof L2Trap)
							{
								// Signets are a special case, casted on target_self but don't harm self
								if (skill.getSkillType() != L2SkillType.SIGNET &&
										skill.getSkillType() != L2SkillType.SIGNET_CASTTIME)
								{
									if (target instanceof L2PcInstance)
									{
										((L2PcInstance) target).getAI().clientStartAutoAttack();
									}
									else if (target instanceof L2Summon && ((L2Character) target).hasAI())
									{
										L2PcInstance owner = ((L2Summon) target).getOwner();
										if (owner != null)
										{
											owner.getAI().clientStartAutoAttack();
										}
									}
									// attack of the own pet does not flag player
									// triggering trap not flag trap owner
									if (player.getPet() != target && !player.getSummons().contains(target) &&
											!(this instanceof L2Trap) && !(this instanceof L2MonsterInstance))
									{
										player.updatePvPStatus((L2Character) target);
									}
								}
							}
							else if (target instanceof L2Attackable)
							{
								switch (skill.getId())
								{
									case 51: // Lure
									case 511: // Temptation
										break;
									default:
										// add attacker into list
										((L2Character) target).addAttackerToAttackByList(this);
								}
							}
							// notify target AI about the attack
							if (((L2Character) target).hasAI())
							{
								switch (skill.getSkillType())
								{
									case AGGREDUCE:
									case AGGREDUCE_CHAR:
									case AGGREMOVE:
										break;
									default:
										((L2Character) target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
								}
							}
						}
						else
						{
							if (target instanceof L2PcInstance)
							{
								// Casting non offensive skill on player with pvp flag set or with karma
								if (!(target.equals(this) || target.equals(player)) &&
										(((L2PcInstance) target).getPvpFlag() > 0 ||
												((L2PcInstance) target).getReputation() < 0))
								{
									player.updatePvPStatus();
								}

								PlayerAssistsManager.getInstance().updateHelpTimer(player, (L2PcInstance) target);
							}
							else if (target instanceof L2Attackable)
							{
								switch (skill.getSkillType())
								{
									case SUMMON:
									case BEAST_FEED:
									case UNLOCK:
									case DELUXE_KEY_UNLOCK:
									case UNLOCK_SPECIAL:
										break;
									default:
										player.updatePvPStatus();
								}
							}
						}
					}
				}

				// Mobs in range 1000 see spell
				Collection<L2Object> objs = player.getKnownList().getKnownObjects().values();
				//synchronized (player.getKnownList().getKnownObjects())
				{
					for (L2Object spMob : objs)
					{
						if (spMob instanceof L2Npc)
						{
							L2Npc npcMob = (L2Npc) spMob;

							if (npcMob.isInsideRadius(player, 1000, true, true) &&
									npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE) != null)
							{
								for (Quest quest : npcMob.getTemplate()
										.getEventQuests(Quest.QuestEventType.ON_SKILL_SEE))
								{
									quest.notifySkillSee(npcMob, player, skill, targets, this instanceof L2Summon);
								}
							}
						}
					}
				}
			}
			// Notify AI
			if (skill.isOffensive())
			{
				switch (skill.getSkillType())
				{
					case AGGREDUCE:
					case AGGREDUCE_CHAR:
					case AGGREMOVE:
						break;
					default:
						for (L2Object target : targets)
						{
							if (target instanceof L2Character && ((L2Character) target).hasAI())
							{
								// notify target AI about the attack
								((L2Character) target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
							}
						}
						break;
				}
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, getClass().getSimpleName() + ": callSkill() failed.");
			e.printStackTrace();
		}
	}

	/**
	 * Return True if the L2Character is behind the target and can't be seen.<BR><BR>
	 */
	public boolean isBehind(L2Object target)
	{
		double angleChar, angleTarget, angleDiff, maxAngleDiff = 60;

		if (target == null)
		{
			return false;
		}

		if (target instanceof L2Character)
		{
			L2Character target1 = (L2Character) target;
			angleChar = Util.calculateAngleFrom(this, target1);
			angleTarget = Util.convertHeadingToDegree(target1.getHeading());
			angleDiff = angleChar - angleTarget;
			if (angleDiff <= -360 + maxAngleDiff)
			{
				angleDiff += 360;
			}
			if (angleDiff >= 360 - maxAngleDiff)
			{
				angleDiff -= 360;
			}
			if (Math.abs(angleDiff) <= maxAngleDiff)
			{
				if (Config.DEBUG)
				{
					Log.info("Char " + getName() + " is behind " + target.getName());
				}
				return true;
			}
		}
		else
		{
			Log.fine("isBehindTarget's target not an L2 Character.");
		}
		return false;
	}

	public boolean isBehindTarget()
	{
		if (calcStat(Stats.IS_BEHIND, 0, this, null) >
				0)//TODO TEST LasTravel permanent at behind stats while under Distortion Ertheia Buff
		{
			return true;
		}
		return isBehind(getTarget());
	}

	/**
	 * Return True if the target is facing the L2Character.<BR><BR>
	 */
	public boolean isInFrontOf(L2Character target)
	{
		double angleChar, angleTarget, angleDiff, maxAngleDiff = 60;
		if (target == null)
		{
			return false;
		}

		angleTarget = Util.calculateAngleFrom(target, this);
		angleChar = Util.convertHeadingToDegree(target.getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= -360 + maxAngleDiff)
		{
			angleDiff += 360;
		}
		if (angleDiff >= 360 - maxAngleDiff)
		{
			angleDiff -= 360;
		}
		return Math.abs(angleDiff) <= maxAngleDiff;
	}

	/**
	 * Returns true if target is in front of L2Character (shield def etc)
	 */
	public boolean isFacing(L2Object target, int maxAngle)
	{
		double angleChar, angleTarget, angleDiff, maxAngleDiff;
		if (target == null)
		{
			return false;
		}

		maxAngleDiff = maxAngle / 2;
		angleTarget = Util.calculateAngleFrom(this, target);
		angleChar = Util.convertHeadingToDegree(getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= -360 + maxAngleDiff)
		{
			angleDiff += 360;
		}
		if (angleDiff >= 360 - maxAngleDiff)
		{
			angleDiff -= 360;
		}
		return Math.abs(angleDiff) <= maxAngleDiff;
	}

	public boolean isInFrontOfTarget()
	{
		L2Object target = getTarget();
		if (target instanceof L2Character)
		{
			return isInFrontOf((L2Character) target);
		}
		else
		{
			return false;
		}
	}

	public double getLevelMod()
	{
		if (getLevel() > 99)
		{
			return (89.0 + getLevel() + 4.0 * (getLevel() - 99.0)) / 100.0;
		}

		return (89.0 + getLevel()) / 100.0;
	}

	public final void setSkillCast(Future<?> newSkillCast)
	{
		_skillCast = newSkillCast;
	}

	/**
	 * Sets _isCastingNow to true and _castInterruptTime is calculated from end time (ticks)
	 */
	public final void forceIsCasting(int newSkillCastEndTick)
	{
		setIsCastingNow(true);
		// for interrupt -400 ms
		_castInterruptTime = newSkillCastEndTick - 4;
	}

	private boolean _AIdisabled = false;

	public void updatePvPFlag(int value)
	{
		// Overridden in L2PcInstance
	}

	/**
	 * Return a multiplier based on weapon random damage<BR><BR>
	 */
	public final double getRandomDamageMultiplier()
	{
		L2Weapon activeWeapon = getActiveWeaponItem();
		int random;

		if (activeWeapon != null)
		{
			random = activeWeapon.getRandomDamage();
		}
		else
		{
			random = 5 + (int) Math.sqrt(getLevel());
		}

		return 1 + (double) Rnd.get(-random, random) / 100;
	}

	public int getAttackEndTime()
	{
		return _attackEndTime;
	}

	/**
	 * Not Implemented.<BR><BR>
	 */
	public abstract int getLevel();

	// =========================================================

	// =========================================================
	// Stat - NEED TO REMOVE ONCE L2CHARSTAT IS COMPLETE
	// Property - Public
	public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill)
	{
		return getStat().calcStat(stat, init, target, skill);
	}

	// Property - Public
	public int getAccuracy()
	{
		return getStat().getAccuracy();
	}

	public int getMAccuracy()
	{
		return getStat().getMAccuracy();
	}

	public final float getAttackSpeedMultiplier()
	{
		return getStat().getAttackSpeedMultiplier();
	}

	public int getCON()
	{
		return getStat().getCON();
	}

	public int getDEX()
	{
		return getStat().getDEX();
	}

	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getCriticalHit(target, skill);
	}

	public double getPCriticalDamage(L2Character target, double damage, L2Skill skill)
	{
		return getStat().getPCriticalDamage(target, damage, skill);
	}

	public int getEvasionRate(L2Character target)
	{
		return getStat().getEvasionRate(target);
	}

	public int getMEvasionRate(L2Character target)
	{
		return getStat().getMEvasionRate(target);
	}

	public int getINT()
	{
		return getStat().getINT();
	}

	public final int getMagicalAttackRange(L2Skill skill)
	{
		return getStat().getMagicalAttackRange(skill);
	}

	public final int getMaxCp()
	{
		return getStat().getMaxCp();
	}

	public int getMAtk(L2Character target, L2Skill skill)
	{
		return getStat().getMAtk(target, skill);
	}

	public int getMAtkSpd()
	{
		return getStat().getMAtkSpd();
	}

	public int getMaxMp()
	{
		return getStat().getMaxMp();
	}

	public int getMaxHp()
	{
		return getStat().getMaxHp();
	}

	public final int getMCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getMCriticalHit(target, skill);
	}

	public int getMDef(L2Character target, L2Skill skill)
	{
		return getStat().getMDef(target, skill);
	}

	public int getMEN()
	{
		return getStat().getMEN();
	}

	public int getLUC()
	{
		return getStat().getLUC();
	}

	public int getCHA()
	{
		return getStat().getCHA();
	}

	public double getMReuseRate(L2Skill skill)
	{
		return getStat().getMReuseRate(skill);
	}

	public float getMovementSpeedMultiplier()
	{
		return getStat().getMovementSpeedMultiplier();
	}

	public int getPAtk(L2Character target)
	{
		return getStat().getPAtk(target);
	}

	public double getSkillMastery()
	{
		return getStat().getSkillMastery();
	}

	public double getPAtkAnimals(L2Character target)
	{
		return getStat().getPAtkAnimals(target);
	}

	public double getPAtkDragons(L2Character target)
	{
		return getStat().getPAtkDragons(target);
	}

	public double getPAtkInsects(L2Character target)
	{
		return getStat().getPAtkInsects(target);
	}

	public double getPAtkMonsters(L2Character target)
	{
		return getStat().getPAtkMonsters(target);
	}

	public double getPAtkPlants(L2Character target)
	{
		return getStat().getPAtkPlants(target);
	}

	public double getPAtkGiants(L2Character target)
	{
		return getStat().getPAtkGiants(target);
	}

	public double getPAtkMagicCreatures(L2Character target)
	{
		return getStat().getPAtkMagicCreatures(target);
	}

	public double getPDefAnimals(L2Character target)
	{
		return getStat().getPDefAnimals(target);
	}

	public double getPDefDragons(L2Character target)
	{
		return getStat().getPDefDragons(target);
	}

	public double getPDefInsects(L2Character target)
	{
		return getStat().getPDefInsects(target);
	}

	public double getPDefMonsters(L2Character target)
	{
		return getStat().getPDefMonsters(target);
	}

	public double getPDefPlants(L2Character target)
	{
		return getStat().getPDefPlants(target);
	}

	public double getPDefGiants(L2Character target)
	{
		return getStat().getPDefGiants(target);
	}

	public double getPDefMagicCreatures(L2Character target)
	{
		return getStat().getPDefMagicCreatures(target);
	}

	//PvP Bonus
	public double getPvPPhysicalDamage(L2Character target)
	{
		return getStat().getPvPPhysicalDamage(target);
	}

	public double getPvPPhysicalDefense(L2Character attacker)
	{
		return getStat().getPvPPhysicalDefense(attacker);
	}

	public double getPvPPhysicalSkillDamage(L2Character target)
	{
		return getStat().getPvPPhysicalSkillDamage(target);
	}

	public double getPvPPhysicalSkillDefense(L2Character attacker)
	{
		return getStat().getPvPPhysicalSkillDefense(attacker);
	}

	public double getPvPMagicDamage(L2Character target)
	{
		return getStat().getPvPMagicDamage(target);
	}

	public double getPvPMagicDefense(L2Character attacker)
	{
		return getStat().getPvPMagicDefense(attacker);
	}

	//PvE Bonus
	public double getPvEPhysicalDamage(L2Character target)
	{
		return getStat().getPvEPhysicalDamage(target);
	}

	public double getPvEPhysicalDefense(L2Character attacker)
	{
		return getStat().getPvEPhysicalDefense(attacker);
	}

	public double getPvEPhysicalSkillDamage(L2Character target)
	{
		return getStat().getPvEPhysicalSkillDamage(target);
	}

	public double getPvEPhysicalSkillDefense(L2Character attacker)
	{
		return getStat().getPvEPhysicalSkillDefense(attacker);
	}

	public double getPvEMagicDamage(L2Character target)
	{
		return getStat().getPvEMagicDamage(target);
	}

	public double getPvEMagicDefense(L2Character attacker)
	{
		return getStat().getPvEMagicDefense(attacker);
	}

	/**
	 * Return max visible HP for display purpose.
	 * Calculated by applying non-visible HP limit
	 * getMaxHp() = getMaxVisibleHp() * limitHp
	 */
	public int getMaxVisibleHp()
	{
		return getStat().getMaxVisibleHp();
	}

	public int getPAtkSpd()
	{
		return getStat().getPAtkSpd();
	}

	public int getPDef(L2Character target)
	{
		return getStat().getPDef(target);
	}

	public final int getPhysicalAttackRange()
	{
		return getStat().getPhysicalAttackRange();
	}

	public int getRunSpeed()
	{
		return getStat().getRunSpeed();
	}

	public final int getShldDef()
	{
		return getStat().getShldDef();
	}

	public int getSTR()
	{
		return getStat().getSTR();
	}

	public final int getWalkSpeed()
	{
		return getStat().getWalkSpeed();
	}

	public int getWIT()
	{
		return getStat().getWIT();
	}

	// =========================================================

	// =========================================================
	// Status - NEED TO REMOVE ONCE L2CHARTATUS IS COMPLETE
	// Method - Public
	public void addStatusListener(L2Character object)
	{
		getStatus().addStatusListener(object);
	}

	public void reduceCurrentHp(double i, L2Character attacker, L2Skill skill)
	{
		reduceCurrentHp(i, attacker, true, false, skill);
	}

	public void reduceCurrentHpByDOT(double i, L2Character attacker, L2Skill skill)
	{
		reduceCurrentHp(i, attacker, !skill.isToggle(), true, skill);
	}

	public void reduceCurrentHp(double i, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if (i == -1)
		{
			return;
		}

		if (Config.L2JMOD_CHAMPION_ENABLE && isChampion() && Config.L2JMOD_CHAMPION_HP != 0)
		{
			getStatus().reduceHp(i / Config.L2JMOD_CHAMPION_HP, attacker, awake, isDOT, false);
		}
		else
		{
			getStatus().reduceHp(i, attacker, awake, isDOT, false);
		}
	}

	public void reduceCurrentMp(double i)
	{
		getStatus().reduceMp(i);
	}

	public void removeStatusListener(L2Character object)
	{
		getStatus().removeStatusListener(object);
	}

	protected void stopHpMpRegeneration()
	{
		getStatus().stopHpMpRegeneration();
	}

	// Property - Public
	public final double getCurrentCp()
	{
		return getStatus().getCurrentCp();
	}

	public final void setCurrentCp(Double newCp)
	{
		setCurrentCp((double) newCp);
	}

	public final void setCurrentCp(double newCp)
	{
		getStatus().setCurrentCp(newCp);
	}

	public final double getCurrentHp()
	{
		return getStatus().getCurrentHp();
	}

	public final void setCurrentHp(double newHp)
	{
		if (!(this instanceof L2EventGolemInstance))
		{
			getStatus().setCurrentHp(newHp);
		}
	}

	public final void setCurrentHpMp(double newHp, double newMp)
	{
		getStatus().setCurrentHpMp(newHp, newMp);
	}

	public final double getCurrentMp()
	{
		return getStatus().getCurrentMp();
	}

	public final void setCurrentMp(Double newMp)
	{
		setCurrentMp((double) newMp);
	}

	public final void setCurrentMp(double newMp)
	{
		getStatus().setCurrentMp(newMp);
	}

	// =========================================================

	public boolean isChampion()
	{
		return false;
	}

	/**
	 * Check player max buff count
	 *
	 * @return max buff count
	 */
	public int getMaxBuffCount()
	{
		int maxBuffs = Config.BUFFS_MAX_AMOUNT + Math.max(0, getSkillLevelHash(L2Skill.SKILL_DIVINE_INSPIRATION) +
				Math.max(0, getSkillLevelHash(L2Skill.SKILL_DIVINE_EXPANSION))); //TODO MOVE TO STAT?

		if (Config.isServer(Config.TENKAI) && this instanceof L2Summon)
		{
			maxBuffs += 8;
		}

		return maxBuffs;
	}

	/**
	 * Send system message about damage.<BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance
	 * <li> L2SummonInstance
	 * <li> L2PetInstance</li><BR><BR>
	 */
	public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
	}

	public FusionSkill getFusionSkill()
	{
		return _fusionSkill;
	}

	public void setFusionSkill(FusionSkill fb)
	{
		_fusionSkill = fb;
	}

	protected L2Object[] _continuousDebuffTargets = null;

	public L2Object[] getContinuousDebuffTargets()
	{
		return _continuousDebuffTargets;
	}

	public void setContinuousDebuffTargets(L2Object[] targets)
	{
		_continuousDebuffTargets = targets;
	}

	public void abortContinuousDebuff(L2Skill skill)
	{
		if (_continuousDebuffTargets == null || skill == null)
		{
			return;
		}

		for (L2Object obj : _continuousDebuffTargets)
		{
			if (!(obj instanceof L2Character))
			{
				continue;
			}

			L2Character target = (L2Character) obj;
			for (L2Abnormal abnormal : target.getAllEffects())
			{
				if (abnormal.getSkill() == skill)
				{
					abnormal.exit();
				}
			}
		}

		_continuousDebuffTargets = null;
	}

	public byte getAttackElement()
	{
		return getStat().getAttackElement();
	}

	public int getAttackElementValue(byte attackAttribute)
	{
		return getStat().getAttackElementValue(attackAttribute);
	}

	public byte getDefenseElement()
	{
		return getStat().getDefenseElement();
	}

	public int getDefenseElementValue(byte defenseAttribute)
	{
		return getStat().getDefenseElementValue(defenseAttribute);
	}

	public final void startPhysicalAttackMuted()
	{
		abortAttack();
	}

	public final void stopPhysicalAttackMuted(L2Abnormal effect)
	{
		if (effect == null)
		{
			stopEffects(L2EffectType.PHYSICAL_ATTACK_MUTE);
		}
		else
		{
			removeEffect(effect);
		}
	}

	public void disableCoreAI(boolean val)
	{
		_AIdisabled = val;
	}

	public boolean isCoreAIDisabled()
	{
		return _AIdisabled;
	}

	/**
	 * Task for potion and herb queue
	 */
	private static class UsePotionTask implements Runnable
	{
		private L2Character _activeChar;
		private L2Skill _skill;

		UsePotionTask(L2Character activeChar, L2Skill skill)
		{
			_activeChar = activeChar;
			_skill = skill;
		}

		@Override
		public void run()
		{
			try
			{
				_activeChar.doSimultaneousCast(_skill);
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING, "", e);
			}
		}
	}

	/**
	 * @return true
	 */
	public boolean giveRaidCurse()
	{
		return true;
	}

	/**
	 * Check if target is affected with special buff
	 *
	 * @param flag long
	 * @return boolean
	 * @see CharEffectList#isAffected(long)
	 */
	public boolean isAffected(long flag)
	{
		return _effects.isAffected(flag);
	}

	public void setRefuseBuffs(boolean refuses)
	{
		_refuseBuffs = refuses;
	}

	public boolean isRefusingBuffs()
	{
		return _refuseBuffs;
	}

	public float getMezMod(int type)
	{
		return 1.0f;
	}

	public void increaseMezResist(final int type)
	{
	}

	public int getMezType(L2SkillType type)
	{
		return -1;
	}

	public int getMezType(L2AbnormalType type)
	{
		return -1;
	}

	private L2Character _faceoffTarget = null;

	public void setFaceoffTarget(L2Character faceoffTarget)
	{
		_faceoffTarget = faceoffTarget;
	}

	public L2Character getFaceoffTarget()
	{
		return _faceoffTarget;
	}

	public void addSkillEffect(L2Skill newSkill)
	{
		if (newSkill != null)
		{
			// Add Func objects of newSkill to the calculator set of the L2Character
			addStatFuncs(newSkill.getStatFuncs(this));

			if (newSkill.isChance())
			{
				addChanceTrigger(newSkill);
			}
		}
	}

	public L2Skill removeSkillEffect(int skillId, boolean removeActiveEffect)
	{
		// Remove the skill from the L2Character _skills
		L2Skill oldSkill = getSkills().get(skillId);
		// Remove all its Func objects from the L2Character calculator set
		if (oldSkill != null)
		{
			//this is just a fail-safe againts buggers and gm dummies...
			if (oldSkill.triggerAnotherSkill() && oldSkill.getTriggeredId() > 0)
			{
				removeSkill(oldSkill.getTriggeredId(), true);
			}

			// does not abort casting of the transformation dispell
			if (oldSkill.getSkillType() != L2SkillType.TRANSFORMDISPEL)
			{
				// Stop casting if this skill is used right now
				if (getLastSkillCast() != null && isCastingNow())
				{
					if (oldSkill.getId() == getLastSkillCast().getId())
					{
						abortCast();
					}
				}
				if (getLastSimultaneousSkillCast() != null && isCastingSimultaneouslyNow())
				{
					if (oldSkill.getId() == getLastSimultaneousSkillCast().getId())
					{
						abortCast();
					}
				}
			}

			// for now, to support transformations, we have to let their
			// effects stay when skill is removed
			if (removeActiveEffect)
			{
				L2Abnormal e = getFirstEffect(oldSkill);
				if (e == null || e.getType() != L2AbnormalType.MUTATE)
				{
					removeStatsOwner(oldSkill);
					stopSkillEffects(oldSkill.getId());
				}
			}

			if (oldSkill instanceof L2SkillAgathion && this instanceof L2PcInstance &&
					((L2PcInstance) this).getAgathionId() > 0)
			{
				((L2PcInstance) this).setAgathionId(0);
				((L2PcInstance) this).broadcastUserInfo();
			}

			if (oldSkill instanceof L2SkillMount && this instanceof L2PcInstance && ((L2PcInstance) this).isMounted())
			{
				((L2PcInstance) this).dismount();
			}

			if (oldSkill.isChance() && _chanceSkills != null)
			{
				removeChanceSkill(oldSkill.getId());
			}
			if (oldSkill instanceof L2SkillSummon && oldSkill.getId() == 710 && this instanceof L2PcInstance)
			{
				for (L2SummonInstance summon : ((L2PcInstance) this).getSummons())
				{
					if (summon.getNpcId() == 14870)
					{
						summon.unSummon((L2PcInstance) this);
					}
				}
			}
		}

		return oldSkill;
	}

	public Map<Integer, L2Skill> getSkills()
	{
		return _skills;
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
	 * @return
	 */
	public boolean isAbleToCastOnTarget(L2Object obj, L2Skill skill, final boolean isMassiveCheck)
	{
		if (this instanceof L2Playable || this instanceof L2DecoyInstance || this instanceof L2TrapInstance)
		{
			L2PcInstance activeChar = null;
			boolean isPressingCtrl = false;

			if (this instanceof L2DecoyInstance)
			{
				activeChar = ((L2DecoyInstance) this).getOwner();
			}
			else if (this instanceof L2TrapInstance)
			{
				activeChar = ((L2TrapInstance) this).getOwner();
			}
			else
			{
				activeChar = getActingPlayer();
			}

			if (activeChar.getCurrentSkill() != null && activeChar.getCurrentSkill().isCtrlPressed())
			{
				isPressingCtrl = true;
			}

			final boolean isInsideSiegeZone = activeChar.isInsideZone(L2Character.ZONE_SIEGE);

			if (obj instanceof L2Playable)
			{
				final L2PcInstance target = obj.getActingPlayer();
				if (activeChar.isPlayingEvent() && !target.isPlayingEvent() ||
						!activeChar.isPlayingEvent() && target.isPlayingEvent())
				{
					return false;
				}

				// Do not check anything if its a chance skill, just fucking cast it motherfucker!
				if (skill.isChance())
				{
					return true;
				}

				switch (skill.getSkillBehavior())
				{
					case FRIENDLY:
					{
						if (((L2Playable) obj).isDead() && !skill.isUseableOnDead())
						{
							return false;
						}
						else if (!((L2Playable) obj).isDead() && skill.isUseableOnDead())
						{
							return false;
						}

						if (activeChar == target)
						{
							return true;
						}

						// Massive friendly skills affects only the caster during Olympiads.
						else if (isMassiveCheck && activeChar.isInOlympiadMode())
						{
							if (!target.isInOlympiadMode())
							{
								return false;
							}

							if (activeChar.getParty() != null && target.getParty() != null &&
									activeChar.getParty() != target.getParty())
							{
								return false;
							}
						}

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

						if (activeChar.isPlayingEvent())
						{
							EventInstance event = activeChar.getEvent();
							if (event != target.getEvent())
							{
								return false;
							}

							if (event.getConfig().isAllVsAll())
							{
								return false;
							}

							return event.getParticipantTeam(activeChar.getObjectId()) ==
									event.getParticipantTeam(target.getObjectId());

						}

						if (isInsideSiegeZone)
						{
							// Using resurrection skills is impossible, except in Fortress.
							if (skill.getSkillType() == L2SkillType.RESURRECT)
							{
								if (activeChar.isInsideZone(L2Character.ZONE_CASTLE))
								{
									if (activeChar.getSiegeState() == 2 && target.getSiegeState() == 2)
									{
										final Siege s = SiegeManager.getInstance().getSiege(getX(), getY(), getZ());

										if (s != null)
										{
											if (s.getControlTowerCount() > 0)
											{
												return true;
											}
										}
									}

									return false;
								}
								else if (!activeChar.isInsideZone(L2Character.ZONE_FORT))
								{
									return false;
								}
							}
						}

						if (isPressingCtrl)
						{
							return true;
						}
						else
						{
							// You can't use friendly skills without ctrl if you are in duel but target isn't, or if target isn't in duel nor in the same duel as yours...
							if (activeChar.isInDuel() && !target.isInDuel() || activeChar.isInSameDuel(target))
							{
								return false;
							}
							// You can use friendly skills without ctrl if target is in same party/command channel...
							if (activeChar.isInSameParty(target) || activeChar.isInSameChannel(target))
							{
								return true;
							}
							// You can use friendly skills without ctrl on clan mates...
							if (activeChar.isInSameClan(target))
							{
								return true;
							}
							// You can use friendly skills without ctrl on ally mates...
							if (activeChar.isInSameAlly(target))
							{
								return true;
							}
							if (isInsideSiegeZone)
							{
								if (activeChar.isInSiege())
								{
									if (activeChar.isInSameSiegeSide(target))
									{
										return true;
									}
								}
								else
								{
									if (target.isInSiege())
									{
										return false;
									}
								}
							}
							// You can't use friendly skills without ctrl while in an arena or a duel...
							if (target.isInsideZone(L2Character.ZONE_PVP) || target.isInDuel() ||
									target.isInOlympiadMode())
							{
								return false;
							}
							// You can't use friendly skills without ctrl on clan wars...
							else if (activeChar.isInSameClanWar(target))
							{
								return false;
							}
							// You can't use friendly skills without ctrl if the target is in combat mode...
							if (target.isAvailableForCombat())
							{
								return false;
							}
						}

						break;
					}
					case UNFRIENDLY:
					{
						// You can't debuff your summon at all, even while pressing CTRL.
						if (activeChar == target)
						{
							return false;
						}

						if (activeChar.isPlayingEvent())
						{
							EventInstance event = activeChar.getEvent();
							if (event != target.getEvent())
							{
								return false;
							}

							if (event.getConfig().isAllVsAll())
							{
								return true;
							}

							return event.getParticipantTeam(activeChar.getObjectId()) !=
									event.getParticipantTeam(target.getObjectId());

						}

						if (activeChar.isInDuel())
						{
							if (!target.isInDuel())
							{
								return false;
							}
							else if (activeChar.isInSameDuel(target))
							{
								return true;
							}
						}

						if (!target.isInOlympiadMode() && target.getPvpFlag() == 0)
						{
							if (activeChar.hasAwakaned())
							{
								if (!target.hasAwakaned())
								{
									return false;
								}
							}
							else if (target.hasAwakaned())
							{
								return false;
							}

							if (target.getLevel() + 9 <= activeChar.getLevel())
							{
								return false;
							}
							else if (activeChar.getLevel() + 9 <= target.getLevel())
							{
								return false;
							}
						}

						if (activeChar.isInOlympiadMode())
						{
							if (!target.isInOlympiadMode())
							{
								return false;
							}
							else if (activeChar.isInSameOlympiadGame(target))
							{
								return true;
							}
						}
						else if (target.isInOlympiadMode())
						{
							return false;
						}

						// On retail, you can't debuff party members at all unless you're in duel.
						if (activeChar.isInSameParty(target) || activeChar.isInSameChannel(target))
						{
							return false;
						}

						// During Fortress/Castle Sieges, they can't debuff eachothers if they are in the same side.
						if (isInsideSiegeZone && activeChar.isInSiege() && activeChar.isInSameSiegeSide(target))
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(
									SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS));
							return false;
						}

						// You can debuff anyone except party members while in an arena...
						if (!isInsideSiegeZone && isInsideZone(L2Character.ZONE_PVP) &&
								target.isInsideZone(L2Character.ZONE_PVP))
						{
							return true;
						}

						// You can debuff anyone except party members while in clan war...
						if (activeChar.isInSameClanWar(target))
						{
							return true;
						}

						if (!isPressingCtrl)
						{
                            /* On retail, it's not possible to debuff a clan member, except in Arena's. */
							if (activeChar.isInSameClan(target))
							{
								return false;
							}
                            /* On retail, it's not possible to debuff an aly member, except in Arena's. */
							else if (activeChar.isInSameAlly(target))
							{
								return false;
							}
						}

						if (isInsideZone(L2Character.ZONE_PVP) && target.isInsideZone(L2Character.ZONE_PVP))
						{
							return true;
						}

						if (target.isInsideZone(L2Character.ZONE_PEACE))
						{
							return false;
						}

                        /* On retail, it is impossible to debuff a "peaceful" player. */
						if (!target.isAvailableForCombat())
						{
							return false;
						}

						// TODO:
						// Skip if both players are in the same siege side...

						break;
					}
					case ATTACK:
					{
                        /* On retail, non-massive and massives attacks has different behaviors.*/
						if (isMassiveCheck)
						{
							// Checks for massives attacks...
							if (activeChar == target)
							{
								return false;
							}

							if (activeChar.isPlayingEvent())
							{
								EventInstance event = activeChar.getEvent();
								if (event != target.getEvent())
								{
									return false;
								}

								if (event.getConfig().isAllVsAll())
								{
									return true;
								}

								return event.getParticipantTeam(activeChar.getObjectId()) !=
										event.getParticipantTeam(target.getObjectId());

							}

							if (activeChar.isInDuel())
							{
								if (!target.isInDuel())
								{
									return false;
								}
								else if (activeChar.isInSameDuel(target))
								{
									return true;
								}
							}

							if (!target.isInOlympiadMode() && target.getPvpFlag() == 0)
							{
								if (activeChar.hasAwakaned())
								{
									if (!target.hasAwakaned())
									{
										return false;
									}
								}
								else if (target.hasAwakaned())
								{
									return false;
								}

								if (target.getLevel() + 9 <= activeChar.getLevel())
								{
									return false;
								}
								else if (activeChar.getLevel() + 9 <= target.getLevel())
								{
									return false;
								}
							}

							if (target.isInsideZone(L2Character.ZONE_PEACE))
							{
								return false;
							}

							if (activeChar.isInSameParty(target) || activeChar.isInSameChannel(target))
							{
								return false;
							}

							if (activeChar.isInOlympiadMode())
							{
								if (!target.isInOlympiadMode())
								{
									return false;
								}
								else if (activeChar.isInSameOlympiadGame(target))
								{
									return true;
								}
							}
							else if (target.isInOlympiadMode())
							{
								return false;
							}

							// During Fortress/Castle Sieges, they can't attack eachothers with massive attacks if they are in the same side.
							// TODO: Needs to be verified on retail.
							if (isInsideSiegeZone && activeChar.isInSiege() && activeChar.isInSameSiegeSide(target))
							{
								return false;
							}

							if (!isInsideSiegeZone && isInsideZone(L2Character.ZONE_PVP) &&
									target.isInsideZone(L2Character.ZONE_PVP))
							{
								return true;
							}

                            /*
                              On retail, there's no way to affect a clan member with a massive attack.
                              Unless you are in an arena.
                             */
							if (activeChar.isInSameClan(target))
							{
								return false;
							}
                            /*
                              On retail, there's no way to affect an ally member with a massive attack.
                              Unless you are in an arena.
                             */
							else if (activeChar.isInSameAlly(target))
							{
								return false;
							}

							if (isInsideZone(L2Character.ZONE_PVP) && target.isInsideZone(L2Character.ZONE_PVP))
							{
								return true;
							}

							if (activeChar.isInSameClanWar(target))
							{
								return true;
							}

                            /*
                              On retail, there's no way to affect a "peaceful" player with massive attacks.
                              Even if CTRL is pressed...
                             */
							if (!target.isAvailableForCombat())
							{
								return false;
							}
						}
						else
						{
							// Checks for non-massives attacks...
                            /* It is impossible to affect a player that isn't participating in the same duel.**/
							if (activeChar.isInDuel())
							{
								// On retail, you can attack a target that's not in your duel with single target skills attacks unless you press CTRL.
								if (!target.isInDuel() && !isPressingCtrl)
								{
									return false;
								}
								// If both are in the same duel, don't check any more condition - return true.
								else if (activeChar.isInSameDuel(target))
								{
									return true;
								}
							}

							if (activeChar.isPlayingEvent())
							{
								EventInstance event = activeChar.getEvent();
								if (event != target.getEvent())
								{
									return false;
								}

								if (event.getConfig().isAllVsAll())
								{
									return true;
								}

								return event.getParticipantTeam(activeChar.getObjectId()) !=
										event.getParticipantTeam(target.getObjectId());

							}

							if (isPressingCtrl)
							{
								if (target == activeChar)
								{
									return true;
								}
							}

							if (target.isInsideZone(L2Character.ZONE_PEACE))
							{
								return false;
							}

							if (!isPressingCtrl)
							{
								if (activeChar == target)
								{
									return false;
								}
								else if (activeChar.isInSameParty(target) || activeChar.isInSameChannel(target))
								{
									return false;
								}
								if (isInsideSiegeZone && activeChar.isInSiege() && activeChar.isInSameSiegeSide(target))
								{
									activeChar.sendPacket(SystemMessage.getSystemMessage(
											SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS));
									return false;
								}
								if (target.isInsideZone(L2Character.ZONE_PVP))
								{
									return true;
								}
								else if (activeChar.isInSameClan(target) || activeChar.isInSameAlly(target))
								{
									return false;
								}
								else if (activeChar.isInSameClanWar(target))
								{
									return true;
								}
								if (!target.isAvailableForCombat())
								{
									return false;
								}
							}
						}
					}
				}
			}
			else if (obj instanceof L2DoorInstance)
			{
				final L2DoorInstance door = (L2DoorInstance) obj;
				final Castle cDoor = door.getCastle();
				final Fort fDoor = door.getFort();

				switch (skill.getSkillBehavior())
				{
					case FRIENDLY:
						return true;
					case UNFRIENDLY:
						return false;
					case ATTACK:
					{
						if (door.isInsideZone(L2Character.ZONE_PEACE))
						{
							return false;
						}
						if (cDoor != null)
						{
                            /* Maybe we need checks to see if the character is attacker? **/
							return cDoor.getSiege().getIsInProgress();
						}
						else if (fDoor != null)
						{
                            /* Maybe we need checks to see if the character is attacker? **/
							/*if (fDoor.getSiege().getIsInProgress() && door.getIsCommanderDoor()) FIXME
								return true;
							else
								return false;*/
						}
						else
						{
							return false;
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
			else if (obj instanceof L2MonsterInstance)
			{
				// Impossible to interact on monsters while in a duel...
				if (activeChar.getDuelId() != 0 && !isPressingCtrl)
				{
					return false;
				}

				if (skill.getSkillBehavior() == L2SkillBehaviorType.FRIENDLY)
				{
					if (!isPressingCtrl)
					{
						return false;
					}
				}

				// TODO: Maybe monsters friendly check here? Like Ketra/Varka Alliance...
				// I think they can't be attacked/debuffed unless you press CTRL on Retail.
			}
			else if (obj instanceof L2NpcInstance || obj instanceof L2GuardInstance)
			{
				// Impossible to interact on npcs/guards while in a duel...
				if (activeChar.getDuelId() != 0 && !isPressingCtrl)
				{
					return false;
				}

				L2SkillBehaviorType skillBehavior = skill.getSkillBehavior();

				switch (skillBehavior)
				{
					// On retail, you can't debuff npcs at all.
					case UNFRIENDLY:
						return false;
					case ATTACK:
					{
						if (isMassiveCheck)
						{
							return false;
						}
						else
						{
							if (!isPressingCtrl)
							{
								return false;
							}
						}
					}
				}
			}
		}
		else if (this instanceof L2Attackable)
		{
			if (!(obj instanceof L2Playable))
			{
				return false;
			}
		}

		return true;
	}
}
