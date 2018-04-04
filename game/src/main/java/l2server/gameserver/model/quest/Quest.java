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

package l2server.gameserver.model.quest;

import l2server.Config;
import l2server.DatabasePool;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.NpcWalkerAI;
import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.instancemanager.QuestManager;
import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.L2Party;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.Trap;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.TrapInstance;
import l2server.gameserver.model.olympiad.CompetitionType;
import l2server.gameserver.model.zone.ZoneType;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.NpcQuestHtmlMessage;
import l2server.gameserver.scripting.ManagedScript;
import l2server.gameserver.scripting.ScriptManager;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.gameserver.templates.item.ItemTemplate;
import l2server.gameserver.util.MinionList;
import l2server.util.Rnd;
import l2server.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Luis Arias
 */
public class Quest extends ManagedScript {
	
	protected static Logger log = LoggerFactory.getLogger(Quest.class.getName());
	
	/**
	 * HashMap containing events from String value of the event
	 */
	private static Map<String, Quest> allEventsS = new HashMap<>();
	/**
	 * HashMap containing lists of timers from the name of the timer
	 */
	private Map<String, List<QuestTimer>> allEventTimers = new ConcurrentHashMap<>();
	
	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
	
	private final int questId;
	private final String name;
	private final String descr;
	private final byte initialState = State.CREATED;
	protected boolean onEnterWorld = false;
	// NOTE: questItemIds will be overridden by child classes.  Ideally, it should be
	// protected instead of public.  However, quest scripts written in Jython will
	// have trouble with protected, as Jython only knows private and public...
	// In fact, protected will typically be considered private thus breaking the scripts.
	// Leave this as public as a workaround.
	public int[] questItemIds = null;
	
	private static final String DEFAULT_NO_QUEST_MSG =
			"<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
	private static final String DEFAULT_ALREADY_COMPLETED_MSG = "<html><body>This quest has already been completed.</body></html>";
	
	/**
	 * Return collection view of the values contains in the allEventS
	 *
	 * @return Collection<Quest>
	 */
	public static Collection<Quest> findAllEvents() {
		return allEventsS.values();
	}
	
	/**
	 * (Constructor)Add values to class variables and put the quest in HashMaps.
	 *
	 * @param questId : int pointing out the ID of the quest
	 * @param name    : String corresponding to the name of the quest
	 * @param descr   : String for the description of the quest
	 */
	public Quest(int questId, String name, String descr) {
		this.questId = questId;
		this.name = name;
		this.descr = descr;
		
		if (questId != 0) {
			QuestManager.getInstance().addQuest(Quest.this);
		} else {
			allEventsS.put(name, this);
		}
		init_LoadGlobalData();
	}
	
	/**
	 * The function init_LoadGlobalData is, by default, called by the constructor of all quests.
	 * Children of this class can implement this function in order to define what variables
	 * to load and what structures to save them in.  By default, nothing is loaded.
	 */
	protected void init_LoadGlobalData() {
	
	}
	
	/**
	 * The function saveGlobalData is, by default, called at shutdown, for all quests, by the QuestManager.
	 * Children of this class can implement this function in order to convert their structures
	 * into <var, value> tuples and make calls to save them to the database, if needed.
	 * By default, nothing is saved.
	 */
	public void saveGlobalData() {
	
	}
	
	public enum TrapAction {
		TRAP_TRIGGERED,
		TRAP_DETECTED,
		TRAP_DISARMED
	}
	
	public enum QuestEventType {
		ON_FIRST_TALK(false),
		// control the first dialog shown by NPCs when they are clicked (some quests must override the default npc action)
		QUEST_START(true),
		// onTalk action from start npcs
		ON_TALK(true),
		// onTalk action from npcs participating in a quest
		ON_ATTACK(true),
		// onAttack action triggered when a mob gets attacked by someone
		ON_KILL(true),
		// onKill action triggered when a mob gets killed.
		ON_SPAWN(true),
		// onSpawn action triggered when an NPC is spawned or respawned.
		ON_SKILL_SEE(true),
		// NPC or Mob saw a person casting a skill (regardless what the target is).
		ON_FACTION_CALL(true),
		ON_AGGRO_RANGE_ENTER(true),
		// a person came within the Npc/Mob's range
		ON_SPELL_FINISHED(true),
		// on spell finished action when npc finish casting skill
		ON_SKILL_LEARN(false),
		// control the AcquireSkill dialog from quest script
		ON_ENTER_ZONE(true),
		// on zone enter
		ON_EXIT_ZONE(true),
		// on zone exit
		ON_DIE_ZONE(true),
		//on die zone
		ON_TRAP_ACTION(true),
		// on zone exit
		ON_SOCIAL_ACTION(true),
		// when a companion npc arrives at a point of a route
		ON_OLYMPIAD_COMBAT(true),
		// on olympiad combat
		ON_ARRIVED(true),
		// when a companion npc arrives at a point of a route
		ON_PLAYER_ARRIVED(true),
		// when the player reaches the companion npc while it was waiting for him
		ON_CREATURE_SEE(true); //when a npc see a creature
		
		// control whether this event type is allowed for the same npc template in multiple quests
		// or if the npc must be registered in at most one quest for the specified event
		private boolean allowMultipleRegistration;
		
		QuestEventType(boolean allowMultipleRegistration) {
			this.allowMultipleRegistration = allowMultipleRegistration;
		}
		
		public boolean isMultipleRegistrationAllowed() {
			return allowMultipleRegistration;
		}
		
	}
	
	/**
	 * Return ID of the quest
	 *
	 * @return int
	 */
	public int getQuestIntId() {
		return questId;
	}
	
	/**
	 * Add a new QuestState to the database and return it.
	 *
	 * @return QuestState : QuestState created
	 */
	public QuestState newQuestState(Player player) {
		return new QuestState(this, player, getInitialState());
	}
	
	/**
	 * Return initial state of the quest
	 *
	 * @return State
	 */
	public byte getInitialState() {
		return initialState;
	}
	
	/**
	 * Return name of the quest
	 *
	 * @return String
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Return description of the quest
	 *
	 * @return String
	 */
	public String getDescr() {
		return descr;
	}
	
	/**
	 * Add a timer to the quest, if it doesn't exist already
	 *
	 * @param name:   name of the timer (also passed back as "event" in onAdvEvent)
	 * @param time:   time in ms for when to fire the timer
	 * @param npc:    npc associated with this timer (can be null)
	 * @param player: player associated with this timer (can be null)
	 */
	public void startQuestTimer(String name, long time, Npc npc, Player player) {
		startQuestTimer(name, time, npc, player, false);
	}
	
	/**
	 * Add a timer to the quest, if it doesn't exist already.  If the timer is repeatable,
	 * it will auto-fire automatically, at a fixed rate, until explicitly canceled.
	 *
	 * @param name:   name of the timer (also passed back as "event" in onAdvEvent)
	 * @param time:   time in ms for when to fire the timer
	 * @param npc:    npc associated with this timer (can be null)
	 * @param player: player associated with this timer (can be null)
	 */
	public void startQuestTimer(String name, long time, Npc npc, Player player, boolean repeating) {
		if (name == null) {
			return;
		}
		
		// Add quest timer if timer doesn't already exist
		List<QuestTimer> timers = getQuestTimers(name);
		// no timer exists with the same name, at all
		if (timers == null) {
			timers = new CopyOnWriteArrayList<>();
			timers.add(new QuestTimer(this, name, time, npc, player, repeating));
			allEventTimers.put(name, timers);
		}
		// a timer with this name exists, but may not be for the same set of npc and player
		else {
			// if there exists a timer with this name, allow the timer only if the [npc, player] set is unique
			// nulls act as wildcards
			if (getQuestTimer(name, npc, player) == null) {
				try {
					rwLock.writeLock().lock();
					timers.add(new QuestTimer(this, name, time, npc, player, repeating));
				} finally {
					rwLock.writeLock().unlock();
				}
			}
		}
	}
	
	public QuestTimer getQuestTimer(String name, Npc npc, Player player) {
		List<QuestTimer> qt = getQuestTimers(name);
		
		if (qt == null || qt.isEmpty()) {
			return null;
		}
		try {
			rwLock.readLock().lock();
			for (QuestTimer timer : qt) {
				if (timer != null) {
					if (timer.isMatch(this, name, npc, player)) {
						return timer;
					}
				}
			}
		} finally {
			rwLock.readLock().unlock();
		}
		return null;
	}
	
	private List<QuestTimer> getQuestTimers(String name) {
		return allEventTimers.get(name);
	}
	
	public void cancelQuestTimers(String name) {
		List<QuestTimer> timers = getQuestTimers(name);
		if (timers == null) {
			return;
		}
		try {
			rwLock.writeLock().lock();
			for (QuestTimer timer : timers) {
				if (timer != null) {
					timer.cancel();
				}
			}
		} finally {
			rwLock.writeLock().unlock();
		}
	}
	
	public void cancelQuestTimer(String name, Npc npc, Player player) {
		QuestTimer timer = getQuestTimer(name, npc, player);
		if (timer != null) {
			timer.cancel();
		}
	}
	
	public void removeQuestTimer(QuestTimer timer) {
		if (timer == null) {
			return;
		}
		
		List<QuestTimer> timers = getQuestTimers(timer.getName());
		if (timers == null) {
			return;
		}
		try {
			rwLock.writeLock().lock();
			timers.remove(timer);
		} finally {
			rwLock.writeLock().unlock();
		}
	}
	
	// these are methods to call from java
	public final boolean notifyAttack(Npc npc, Player attacker, int damage, boolean isPet, Skill skill) {
		String res = null;
		try {
			res = onAttack(npc, attacker, damage, isPet, skill);
		} catch (Exception e) {
			return showError(attacker, e);
		}
		return showResult(attacker, res);
	}
	
	public final boolean notifyCreatureSee(Npc npc, Player creature, boolean isPet) {
		String res = null;
		try {
			res = onCreatureSee(npc, creature, isPet);
		} catch (Exception e) {
			return showError(creature, e);
		}
		return showResult(creature, res);
	}
	
	public final boolean notifyDeath(Creature killer, Creature victim, QuestState qs) {
		String res = null;
		try {
			res = onDeath(killer, victim, qs);
		} catch (Exception e) {
			return showError(qs.getPlayer(), e);
		}
		return showResult(qs.getPlayer(), res);
	}
	
	public final boolean notifySpellFinished(Npc instance, Player player, Skill skill) {
		String res = null;
		try {
			res = onSpellFinished(instance, player, skill);
		} catch (Exception e) {
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	/**
	 * Notify quest script when something happens with a trap
	 *
	 * @param trap:    the trap instance which triggers the notification
	 * @param trigger: the character which makes effect on the trap
	 * @param action:  0: trap casting its skill. 1: trigger detects the trap. 2: trigger removes the trap
	 */
	public final boolean notifyTrapAction(Trap trap, Creature trigger, TrapAction action) {
		String res = null;
		try {
			res = onTrapAction(trap, trigger, action);
		} catch (Exception e) {
			if (trigger.getActingPlayer() != null) {
				return showError(trigger.getActingPlayer(), e);
			}
			log.warn("Exception on onTrapAction() in notifyTrapAction(): " + e.getMessage(), e);
			return true;
		}
		if (trigger.getActingPlayer() != null) {
			return showResult(trigger.getActingPlayer(), res);
		}
		return false;
	}
	
	public final boolean notifySpawn(Npc npc) {
		try {
			onSpawn(npc);
		} catch (Exception e) {
			log.warn("Exception on onSpawn() in notifySpawn(): " + e.getMessage(), e);
			return true;
		}
		return false;
	}
	
	public final boolean notifySpawn(Summon npc) {
		try {
			onSpawn(npc);
		} catch (Exception e) {
			log.warn("Exception on onSpawn() in notifySpawn(): " + e.getMessage(), e);
			return true;
		}
		return false;
	}
	
	public final boolean notifyEvent(String event, Npc npc, Player player) {
		String res = null;
		try {
			res = onAdvEvent(event, npc, player);
		} catch (Exception e) {
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public final boolean notifyEnterWorld(Player player) {
		String res = null;
		try {
			res = onEnterWorld(player);
		} catch (Exception e) {
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public final boolean notifyKill(Npc npc, Player killer, boolean isPet) {
		String res = null;
		try {
			res = onKill(npc, killer, isPet);
		} catch (Exception e) {
			return showError(killer, e);
		}
		return showResult(killer, res);
	}
	
	public final boolean notifyTalk(Npc npc, QuestState qs) {
		String res = null;
		try {
			res = onTalk(npc, qs.getPlayer());
		} catch (Exception e) {
			return showError(qs.getPlayer(), e);
		}
		qs.getPlayer().setLastQuestNpcObject(npc.getObjectId());
		return showResult(qs.getPlayer(), res);
	}
	
	// override the default NPC dialogs when a quest defines this for the given NPC
	public final boolean notifyFirstTalk(Npc npc, Player player) {
		String res = null;
		try {
			res = onFirstTalk(npc, player);
		} catch (Exception e) {
			return showError(player, e);
		}
		// if the quest returns text to display, display it.
		if (res != null && res.length() > 0) {
			return showResult(player, res);
		}
		// else tell the player that
		else {
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		// note: if the default html for this npc needs to be shown, onFirstTalk should
		// call npc.showChatWindow(player) and then return null.
		return true;
	}
	
	public final boolean notifyAcquireSkillList(Npc npc, Player player) {
		String res = null;
		try {
			res = onAcquireSkillList(npc, player);
		} catch (Exception e) {
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public final boolean notifyAcquireSkillInfo(Npc npc, Player player, Skill skill) {
		String res = null;
		try {
			res = onAcquireSkillInfo(npc, player, skill);
		} catch (Exception e) {
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public final boolean notifyAcquireSkill(Npc npc, Player player, Skill skill) {
		String res = null;
		try {
			res = onAcquireSkill(npc, player, skill);
			if (Objects.equals(res, "true")) {
				return true;
			} else if (Objects.equals(res, "false")) {
				return false;
			}
		} catch (Exception e) {
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public class TmpOnSkillSee implements Runnable {
		private Npc npc;
		private Player caster;
		private Skill skill;
		private WorldObject[] targets;
		private boolean isPet;
		
		public TmpOnSkillSee(Npc npc, Player caster, Skill skill, WorldObject[] targets, boolean isPet) {
			this.npc = npc;
			this.caster = caster;
			this.skill = skill;
			this.targets = targets;
			this.isPet = isPet;
		}
		
		@Override
		public void run() {
			String res = null;
			try {
				res = onSkillSee(npc, caster, skill, targets, isPet);
			} catch (Exception e) {
				showError(caster, e);
			}
			showResult(caster, res);
		}
	}
	
	public final boolean notifySkillSee(Npc npc, Player caster, Skill skill, WorldObject[] targets, boolean isPet) {
		ThreadPoolManager.getInstance().executeAi(new TmpOnSkillSee(npc, caster, skill, targets, isPet));
		return true;
	}
	
	public final boolean notifyFactionCall(Npc npc, Npc caller, Player attacker, boolean isPet) {
		String res = null;
		try {
			res = onFactionCall(npc, caller, attacker, isPet);
		} catch (Exception e) {
			return showError(attacker, e);
		}
		return showResult(attacker, res);
	}
	
	public class TmpOnAggroEnter implements Runnable {
		private Npc npc;
		private Player pc;
		private boolean isPet;
		
		public TmpOnAggroEnter(Npc npc, Player pc, boolean isPet) {
			this.npc = npc;
			this.pc = pc;
			this.isPet = isPet;
		}
		
		@Override
		public void run() {
			String res = null;
			try {
				res = onAggroRangeEnter(npc, pc, isPet);
			} catch (Exception e) {
				showError(pc, e);
			}
			showResult(pc, res);
		}
	}
	
	public final boolean notifyAggroRangeEnter(Npc npc, Player player, boolean isPet) {
		ThreadPoolManager.getInstance().executeAi(new TmpOnAggroEnter(npc, player, isPet));
		return true;
	}
	
	public final boolean notifyDieZone(Creature character, Creature killer, ZoneType zone) {
		Player player = character.getActingPlayer();
		String res = null;
		try {
			res = onDieZone(character, killer, zone);
		} catch (Exception e) {
			if (player != null) {
				return showError(player, e);
			}
		}
		if (player != null) {
			return showResult(player, res);
		}
		return true;
	}
	
	public final boolean notifyEnterZone(Creature character, ZoneType zone) {
		Player player = character.getActingPlayer();
		String res = null;
		try {
			res = onEnterZone(character, zone);
		} catch (Exception e) {
			if (player != null) {
				return showError(player, e);
			}
		}
		if (player != null) {
			return showResult(player, res);
		}
		return true;
	}
	
	public final boolean notifyExitZone(Creature character, ZoneType zone) {
		Player player = character.getActingPlayer();
		String res = null;
		try {
			res = onExitZone(character, zone);
		} catch (Exception e) {
			if (player != null) {
				return showError(player, e);
			}
		}
		if (player != null) {
			return showResult(player, res);
		}
		return true;
	}
	
	public final boolean notifyItemUse(Player player, ItemTemplate item) {
		String res = null;
		try {
			res = onItemUse(player, item);
		} catch (Exception e) {
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public final boolean notifySocialAction(Npc npc, Player player, int actionId) {
		String res = null;
		try {
			res = onSocialAction(npc, player, actionId);
		} catch (Exception e) {
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public final boolean notifyArrived(NpcWalkerAI ai) {
		String res = null;
		try {
			res = onArrived(ai);
		} catch (Exception e) {
			if (ai.getGuided() != null) {
				return showError(ai.getGuided(), e);
			}
		}
		if (ai.getGuided() != null) {
			return showResult(ai.getGuided(), res);
		}
		return true;
	}
	
	public final boolean notifyPlayerArrived(NpcWalkerAI ai) {
		String res = null;
		try {
			res = onPlayerArrived(ai);
		} catch (Exception e) {
			if (ai.getGuided() != null) {
				return showError(ai.getGuided(), e);
			}
		}
		if (ai.getGuided() != null) {
			return showResult(ai.getGuided(), res);
		}
		return true;
	}
	
	public final boolean notifyOlympiadCombat(Player player, CompetitionType type, boolean hasWon) {
		String res = null;
		try {
			res = onOlympiadCombat(player, type, hasWon);
		} catch (Exception e) {
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	// these are methods that java calls to invoke scripts
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet) {
		return null;
	}
	
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet, Skill skill) {
		return onAttack(npc, attacker, damage, isPet);
	}
	
	public String onCreatureSee(Npc npc, Player creature, boolean isPet) {
		return null;
	}
	
	public String onDeath(Creature killer, Creature victim, QuestState qs) {
		if (killer instanceof Npc) {
			return onAdvEvent("", (Npc) killer, qs.getPlayer());
		} else {
			return onAdvEvent("", null, qs.getPlayer());
		}
	}
	
	public String onAdvEvent(String event, Npc npc, Player player) {
		// if not overridden by a subclass, then default to the returned value of the simpler (and older) onEvent override
		// if the player has a state, use it as parameter in the next call, else return null
		QuestState qs = player.getQuestState(getName());
		if (qs != null) {
			return onEvent(event, qs);
		}
		
		return null;
	}
	
	public String onEvent(String event, QuestState qs) {
		return null;
	}
	
	public String onKill(Npc npc, Player killer, boolean isPet) {
		return null;
	}
	
	public String onTalk(Npc npc, Player talker) {
		return null;
	}
	
	public String onFirstTalk(Npc npc, Player player) {
		return null;
	}
	
	public String onAcquireSkillList(Npc npc, Player player) {
		return null;
	}
	
	public String onAcquireSkillInfo(Npc npc, Player player, Skill skill) {
		return null;
	}
	
	public String onAcquireSkill(Npc npc, Player player, Skill skill) {
		return null;
	}
	
	public String onSkillSee(Npc npc, Player caster, Skill skill, WorldObject[] targets, boolean isPet) {
		return null;
	}
	
	public String onSpellFinished(Npc npc, Player player, Skill skill) {
		return null;
	}
	
	public String onTrapAction(Trap trap, Creature trigger, TrapAction action) {
		return null;
	}
	
	public String onSpawn(Npc npc) {
		return null;
	}
	
	public String onSpawn(Summon npc) {
		return null;
	}
	
	public String onFactionCall(Npc npc, Npc caller, Player attacker, boolean isPet) {
		return null;
	}
	
	public String onAggroRangeEnter(Npc npc, Player player, boolean isPet) {
		return null;
	}
	
	public String onEnterWorld(Player player) {
		return null;
	}
	
	public String onEnterZone(Creature character, ZoneType zone) {
		return null;
	}
	
	public String onDieZone(Creature character, Creature killer, ZoneType zone) {
		return null;
	}
	
	public String onExitZone(Creature character, ZoneType zone) {
		return null;
	}
	
	public String onItemUse(Player player, ItemTemplate item) {
		return null;
	}
	
	public String onSocialAction(Npc npc, Player player, int actionId) {
		return null;
	}
	
	public String onArrived(NpcWalkerAI npc) {
		return null;
	}
	
	public String onPlayerArrived(NpcWalkerAI npc) {
		return null;
	}
	
	public String onOlympiadCombat(Player player, CompetitionType type, boolean hasWon) {
		return null;
	}
	
	/**
	 * Show message error to player who has an access level greater than 0
	 *
	 * @param player : Player
	 * @param t      : Throwable
	 * @return boolean
	 */
	public boolean showError(Player player, Throwable t) {
		log.warn(getScriptFile().getAbsolutePath(), t);
		if (t.getMessage() == null) {
			t.printStackTrace();
		}
		if (player != null && player.getAccessLevel().isGm()) {
			String res = "<html><body><title>Script error</title>" + Util.getStackTrace(t) + "</body></html>";
			return showResult(player, res);
		}
		return false;
	}
	
	/**
	 * Show a message to player.<BR><BR>
	 * <U><I>Concept : </I></U><BR>
	 * 3 cases are managed according to the value of the parameter "res" :<BR>
	 * <LI><U>"res" ends with string ".html" :</U> an HTML is opened in order to be shown in a dialog box</LI>
	 * <LI><U>"res" starts with "<html>" :</U> the message hold in "res" is shown in a dialog box</LI>
	 * <LI><U>otherwise :</U> the message held in "res" is shown in chat box</LI>
	 *
	 * @param res : String pointing out the message to show at the player
	 * @return boolean
	 */
	public boolean showResult(Player player, String res) {
		if (res == null || res.isEmpty() || player == null) {
			return true;
		}
		
		if (res.endsWith(".htm") || res.endsWith(".html")) {
			showHtmlFile(player, res);
		} else if (res.startsWith("<html>")) {
			NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
			npcReply.setHtml(res);
			npcReply.replace("%playername%", player.getName());
			player.sendPacket(npcReply);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		} else {
			player.sendMessage(res);
		}
		return false;
	}
	
	/**
	 * Add quests to the L2PCInstance of the player.<BR><BR>
	 * <U><I>Action : </U></I><BR>
	 * Add state of quests, drops and variables for quests in the HashMap quest of Player
	 *
	 * @param player : Player who is entering the world
	 */
	public static void playerEnter(Player player) {
		
		Connection con = null;
		try {
			// Get list of quests owned by the player from database
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement;
			
			PreparedStatement invalidQuestData = con.prepareStatement("DELETE FROM character_quests WHERE charId=? AND name=?");
			PreparedStatement invalidQuestDataVar = con.prepareStatement("DELETE FROM character_quests WHERE charId=? AND name=? AND var=?");
			
			statement = con.prepareStatement("SELECT name,value FROM character_quests WHERE charId=? AND var=?");
			statement.setInt(1, player.getObjectId());
			statement.setString(2, "<state>");
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				
				// Get ID of the quest and ID of its state
				String questId = rs.getString("name");
				String statename = rs.getString("value");
				
				// Search quest associated with the ID
				Quest q = QuestManager.getInstance().getQuest(questId);
				if (q == null) {
					log.trace("Unknown quest " + questId + " for player " + player.getName());
					if (Config.AUTODELETE_INVALID_QUEST_DATA) {
						invalidQuestData.setInt(1, player.getObjectId());
						invalidQuestData.setString(2, questId);
						invalidQuestData.executeUpdate();
					}
					continue;
				}
				
				// Create a new QuestState for the player that will be added to the player's list of quests
				new QuestState(q, player, State.getStateId(statename));
			}
			rs.close();
			invalidQuestData.close();
			statement.close();
			
			// Get list of quests owned by the player from the DB in order to add variables used in the quest.
			statement = con.prepareStatement("SELECT name,var,value FROM character_quests WHERE charId=? AND var<>?");
			statement.setInt(1, player.getObjectId());
			statement.setString(2, "<state>");
			rs = statement.executeQuery();
			while (rs.next()) {
				String questId = rs.getString("name");
				String var = rs.getString("var");
				String value = rs.getString("value");
				// Get the QuestState saved in the loop before
				QuestState qs = player.getQuestState(questId);
				if (qs == null) {
					log.trace("Lost variable " + var + " in quest " + questId + " for player " + player.getName());
					if (Config.AUTODELETE_INVALID_QUEST_DATA) {
						invalidQuestDataVar.setInt(1, player.getObjectId());
						invalidQuestDataVar.setString(2, questId);
						invalidQuestDataVar.setString(3, var);
						invalidQuestDataVar.executeUpdate();
					}
					continue;
				}
				// Add parameter to the quest
				qs.setInternal(var, value);
			}
			rs.close();
			invalidQuestDataVar.close();
			statement.close();
		} catch (Exception e) {
			log.warn("could not insert char quest:", e);
		} finally {
			DatabasePool.close(con);
		}
		
		// events
		for (String name : allEventsS.keySet()) {
			player.processQuestEvent(name, "enter");
		}
	}
	
	/**
	 * Insert (or Update) in the database variables that need to stay persistant for this quest after a reboot.
	 * This function is for storage of values that do not related to a specific player but are
	 * global for all characters.  For example, if we need to disable a quest-gatekeeper until
	 * a certain time (as is done with some grand-boss gatekeepers), we can save that time in the DB.
	 *
	 * @param var   : String designating the name of the variable for the quest
	 * @param value : String designating the value of the variable for the quest
	 */
	public final void saveGlobalQuestVar(String var, String value) {
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("REPLACE INTO quest_global_data (quest_name,var,value) VALUES (?,?,?)");
			statement.setString(1, getName());
			statement.setString(2, var);
			statement.setString(3, value);
			statement.executeUpdate();
			statement.close();
		} catch (Exception e) {
			log.warn("could not insert global quest variable:", e);
		} finally {
			DatabasePool.close(con);
		}
	}
	
	/**
	 * Read from the database a previously saved variable for this quest.
	 * Due to performance considerations, this function should best be used only when the quest is first loaded.
	 * Subclasses of this class can define structures into which these loaded values can be saved.
	 * However, on-demand usage of this function throughout the script is not prohibited, only not recommended.
	 * Values read from this function were entered by calls to "saveGlobalQuestVar"
	 *
	 * @param var : String designating the name of the variable for the quest
	 * @return String : String representing the loaded value for the passed var, or an empty string if the var was invalid
	 */
	public final String loadGlobalQuestVar(String var) {
		String result = "";
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("SELECT value FROM quest_global_data WHERE quest_name = ? AND var = ?");
			statement.setString(1, getName());
			statement.setString(2, var);
			ResultSet rs = statement.executeQuery();
			if (rs.first()) {
				result = rs.getString(1);
			}
			rs.close();
			statement.close();
		} catch (Exception e) {
			log.warn("could not load global quest variable:", e);
		} finally {
			DatabasePool.close(con);
		}
		return result;
	}
	
	/**
	 * Permanently delete from the database a global quest variable that was previously saved for this quest.
	 *
	 * @param var : String designating the name of the variable for the quest
	 */
	public final void deleteGlobalQuestVar(String var) {
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM quest_global_data WHERE quest_name = ? AND var = ?");
			statement.setString(1, getName());
			statement.setString(2, var);
			statement.executeUpdate();
			statement.close();
		} catch (Exception e) {
			log.warn("could not delete global quest variable:", e);
		} finally {
			DatabasePool.close(con);
		}
	}
	
	/**
	 * Permanently delete from the database all global quest variables that was previously saved for this quest.
	 */
	public final void deleteAllGlobalQuestVars() {
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM quest_global_data WHERE quest_name = ?");
			statement.setString(1, getName());
			statement.executeUpdate();
			statement.close();
		} catch (Exception e) {
			log.warn("could not delete global quest variables:", e);
		} finally {
			DatabasePool.close(con);
		}
	}
	
	/**
	 * Insert in the database the quest for the player.
	 *
	 * @param qs    : QuestState pointing out the state of the quest
	 * @param var   : String designating the name of the variable for the quest
	 * @param value : String designating the value of the variable for the quest
	 */
	public static void createQuestVarInDb(QuestState qs, String var, String value) {
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("INSERT INTO character_quests (charId,name,var,value) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE value=?");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.setString(3, var);
			statement.setString(4, value);
			statement.setString(5, value);
			statement.executeUpdate();
			statement.close();
		} catch (Exception e) {
			log.warn("could not insert char quest:", e);
		} finally {
			DatabasePool.close(con);
		}
	}
	
	/**
	 * Update the value of the variable "var" for the quest.<BR><BR>
	 * <U><I>Actions :</I></U><BR>
	 * The selection of the right record is made with :
	 * <LI>charId = qs.getPlayer().getObjectID()</LI>
	 * <LI>name = qs.getQuest().getName()</LI>
	 * <LI>var = var</LI>
	 * <BR><BR>
	 * The modification made is :
	 * <LI>value = parameter value</LI>
	 *
	 * @param qs    : Quest State
	 * @param var   : String designating the name of the variable for quest
	 * @param value : String designating the value of the variable for quest
	 */
	public static void updateQuestVarInDb(QuestState qs, String var, String value) {
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("UPDATE character_quests SET value=? WHERE charId=? AND name=? AND var = ?");
			statement.setString(1, value);
			statement.setInt(2, qs.getPlayer().getObjectId());
			statement.setString(3, qs.getQuestName());
			statement.setString(4, var);
			statement.executeUpdate();
			statement.close();
		} catch (Exception e) {
			log.warn("could not update char quest:", e);
		} finally {
			DatabasePool.close(con);
		}
	}
	
	/**
	 * Delete a variable of player's quest from the database.
	 *
	 * @param qs  : object QuestState pointing out the player's quest
	 * @param var : String designating the variable characterizing the quest
	 */
	public static void deleteQuestVarInDb(QuestState qs, String var) {
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM character_quests WHERE charId=? AND name=? AND var=?");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.setString(3, var);
			statement.executeUpdate();
			statement.close();
		} catch (Exception e) {
			log.warn("could not delete char quest:", e);
		} finally {
			DatabasePool.close(con);
		}
	}
	
	/**
	 * Delete the player's quest from database.
	 *
	 * @param qs : QuestState pointing out the player's quest
	 */
	public static void deleteQuestInDb(QuestState qs) {
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM character_quests WHERE charId=? AND name=?");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.executeUpdate();
			statement.close();
		} catch (Exception e) {
			log.warn("could not delete char quest:", e);
		} finally {
			DatabasePool.close(con);
		}
	}
	
	/**
	 * Create a record in database for quest.<BR><BR>
	 * <U><I>Actions :</I></U><BR>
	 * Use fucntion createQuestVarInDb() with following parameters :<BR>
	 * <LI>QuestState : parameter sq that puts in fields of database :
	 * <UL type="square">
	 * <LI>charId : ID of the player</LI>
	 * <LI>name : name of the quest</LI>
	 * </UL>
	 * </LI>
	 * <LI>var : string "&lt;state&gt;" as the name of the variable for the quest</LI>
	 * <LI>val : string corresponding at the ID of the state (in fact, initial state)</LI>
	 *
	 * @param qs : QuestState
	 */
	public static void createQuestInDb(QuestState qs) {
		createQuestVarInDb(qs, "<state>", State.getStateName(qs.getState()));
	}
	
	/**
	 * Update informations regarding quest in database.<BR>
	 * <U><I>Actions :</I></U><BR>
	 * <LI>Get ID state of the quest recorded in object qs</LI>
	 * <LI>Test if quest is completed. If true, add a star (*) before the ID state</LI>
	 * <LI>Save in database the ID state (with or without the star) for the variable called "&lt;state&gt;" of the quest</LI>
	 *
	 * @param qs : QuestState
	 */
	public static void updateQuestInDb(QuestState qs) {
		String val = State.getStateName(qs.getState());
		updateQuestVarInDb(qs, "<state>", val);
	}
	
	/**
	 * Return default html page "You are either not on a quest that involves this NPC.."
	 */
	public static String getNoQuestMsg(Player player) {
		final String result = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "noquest.htm");
		if (result != null && result.length() > 0) {
			return result;
		}
		
		return DEFAULT_NO_QUEST_MSG;
	}
	
	/**
	 * Return default html page "This quest has already been completed."
	 */
	public static String getAlreadyCompletedMsg(Player player) {
		final String result = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "alreadycompleted.htm");
		if (result != null && result.length() > 0) {
			return result;
		}
		
		return DEFAULT_ALREADY_COMPLETED_MSG;
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for the specified Event type.<BR><BR>
	 *
	 * @param npcId     : id of the NPC to register
	 * @param eventType : type of event being registered
	 * @return NpcTemplate : Npc Template corresponding to the npcId, or null if the id is invalid
	 */
	public NpcTemplate addEventId(int npcId, QuestEventType eventType) {
		try {
			NpcTemplate t = NpcTable.getInstance().getTemplate(npcId);
			if (t != null) {
				t.addQuestEvent(eventType, this);
			}
			
			return t;
		} catch (Exception e) {
			log.warn("Exception on addEventId(): " + e.getMessage(), e);
			return null;
		}
	}
	
	/**
	 * Add the quest to the NPC's startQuest
	 *
	 * @return NpcTemplate : Start NPC
	 */
	public NpcTemplate addStartNpc(int npcId) {
		return addEventId(npcId, QuestEventType.QUEST_START);
	}
	
	/**
	 * Add the quest to the NPC's first-talk (default action dialog)
	 *
	 * @return NpcTemplate : Start NPC
	 */
	public NpcTemplate addFirstTalkId(int npcId) {
		return addEventId(npcId, QuestEventType.ON_FIRST_TALK);
	}
	
	/**
	 * Add the NPC to the AcquireSkill dialog
	 *
	 * @return NpcTemplate : NPC
	 */
	public NpcTemplate addAcquireSkillId(int npcId) {
		return addEventId(npcId, QuestEventType.ON_SKILL_LEARN);
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for Attack Events.<BR><BR>
	 *
	 * @return int : attackId
	 */
	public NpcTemplate addAttackId(int attackId) {
		return addEventId(attackId, QuestEventType.ON_ATTACK);
	}
	
	public NpcTemplate addCreatureSeeId(int npcId) {
		return addEventId(npcId, QuestEventType.ON_CREATURE_SEE);
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for Kill Events.<BR><BR>
	 *
	 * @return int : killId
	 */
	public NpcTemplate addKillId(int killId) {
		return addEventId(killId, QuestEventType.ON_KILL);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Talk Events.<BR><BR>
	 *
	 * @param talkId : ID of the NPC
	 * @return int : ID of the NPC
	 */
	public NpcTemplate addTalkId(int talkId) {
		return addEventId(talkId, QuestEventType.ON_TALK);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Spawn Events.<BR><BR>
	 *
	 * @return int : ID of the NPC
	 */
	public NpcTemplate addSpawnId(int npcId) {
		return addEventId(npcId, QuestEventType.ON_SPAWN);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Skill-See Events.<BR><BR>
	 *
	 * @return int : ID of the NPC
	 */
	public NpcTemplate addSkillSeeId(int npcId) {
		return addEventId(npcId, QuestEventType.ON_SKILL_SEE);
	}
	
	public NpcTemplate addSpellFinishedId(int npcId) {
		return addEventId(npcId, QuestEventType.ON_SPELL_FINISHED);
	}
	
	public NpcTemplate addTrapActionId(int trapId) {
		return addEventId(trapId, QuestEventType.ON_TRAP_ACTION);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Faction Call Events.<BR><BR>
	 *
	 * @return int : ID of the NPC
	 */
	public NpcTemplate addFactionCallId(int npcId) {
		return addEventId(npcId, QuestEventType.ON_FACTION_CALL);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Character See Events.<BR><BR>
	 *
	 * @return int : ID of the NPC
	 */
	public NpcTemplate addAggroRangeEnterId(int npcId) {
		return addEventId(npcId, QuestEventType.ON_AGGRO_RANGE_ENTER);
	}
	
	public ZoneType addEnterZoneId(int zoneId) {
		try {
			ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
			if (zone != null) {
				zone.addQuestEvent(QuestEventType.ON_ENTER_ZONE, this);
			}
			return zone;
		} catch (Exception e) {
			log.warn("Exception on addEnterZoneId(): " + e.getMessage(), e);
			return null;
		}
	}
	
	public ZoneType addDieZoneId(int zoneId) {
		try {
			ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
			if (zone != null) {
				zone.addQuestEvent(QuestEventType.ON_DIE_ZONE, this);
			}
			return zone;
		} catch (Exception e) {
			log.warn("Exception on addEnterZoneId(): " + e.getMessage(), e);
			return null;
		}
	}
	
	public ZoneType addExitZoneId(int zoneId) {
		try {
			ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
			if (zone != null) {
				zone.addQuestEvent(QuestEventType.ON_EXIT_ZONE, this);
			}
			return zone;
		} catch (Exception e) {
			log.warn("Exception on addExitZoneId(): " + e.getMessage(), e);
			return null;
		}
	}
	
	// returns a random party member's Player for the passed player's party
	// returns the passed player if he has no party.
	public Player getRandomPartyMember(Player player) {
		// NPE prevention.  If the player is null, there is nothing to return
		if (player == null) {
			return null;
		}
		if (player.getParty() == null || player.getParty().getPartyMembers().isEmpty()) {
			return player;
		}
		L2Party party = player.getParty();
		return party.getPartyMembers().get(Rnd.get(party.getPartyMembers().size()));
	}
	
	/**
	 * Auxilary function for party quests.
	 * Note: This function is only here because of how commonly it may be used by quest developers.
	 * For any variations on this function, the quest script can always handle things on its own
	 *
	 * @param player: the instance of a player whose party is to be searched
	 * @param value:  the value of the "cond" variable that must be matched
	 * @return Player: Player for a random party member that matches the specified
	 * condition, or null if no match.
	 */
	public Player getRandomPartyMember(Player player, String value) {
		return getRandomPartyMember(player, "cond", value);
	}
	
	/**
	 * Auxilary function for party quests.
	 * Note: This function is only here because of how commonly it may be used by quest developers.
	 * For any variations on this function, the quest script can always handle things on its own
	 *
	 * @param player:    the instance of a player whose party is to be searched
	 * @param var/value: a tuple specifying a quest condition that must be satisfied for
	 *                   a party member to be considered.
	 * @return Player: Player for a random party member that matches the specified
	 * condition, or null if no match.  If the var is null, any random party
	 * member is returned (i.e. no condition is applied).
	 * The party member must be within 1500 distance from the target of the reference
	 * player, or if no target exists, 1500 distance from the player itself.
	 */
	public Player getRandomPartyMember(Player player, String var, String value) {
		// if no valid player instance is passed, there is nothing to check...
		if (player == null) {
			return null;
		}
		
		// for null var condition, return any random party member.
		if (var == null) {
			return getRandomPartyMember(player);
		}
		
		// normal cases...if the player is not in a party, check the player's state
		QuestState temp = null;
		L2Party party = player.getParty();
		// if this player is not in a party, just check if this player instance matches the conditions itself
		if (party == null || party.getPartyMembers().isEmpty()) {
			temp = player.getQuestState(getName());
			if (temp != null && temp.get(var) != null && temp.get(var).equalsIgnoreCase(value)) {
				return player; // match
			}
			
			return null; // no match
		}
		
		// if the player is in a party, gather a list of all matching party members (possibly
		// including this player)
		ArrayList<Player> candidates = new ArrayList<>();
		
		// get the target for enforcing distance limitations.
		WorldObject target = player.getTarget();
		if (target == null) {
			target = player;
		}
		
		for (Player partyMember : party.getPartyMembers()) {
			if (partyMember == null) {
				continue;
			}
			temp = partyMember.getQuestState(getName());
			if (temp != null && temp.get(var) != null && temp.get(var).equalsIgnoreCase(value) &&
					partyMember.isInsideRadius(target, 1500, true, false)) {
				candidates.add(partyMember);
			}
		}
		// if there was no match, return null...
		if (candidates.isEmpty()) {
			return null;
		}
		
		// if a match was found from the party, return one of them at random.
		return candidates.get(Rnd.get(candidates.size()));
	}
	
	/**
	 * Auxilary function for party quests.
	 * Note: This function is only here because of how commonly it may be used by quest developers.
	 * For any variations on this function, the quest script can always handle things on its own
	 *
	 * @param player: the instance of a player whose party is to be searched
	 * @param state:  the state in which the party member's queststate must be in order to be considered.
	 * @return Player: Player for a random party member that matches the specified
	 * condition, or null if no match.  If the var is null, any random party
	 * member is returned (i.e. no condition is applied).
	 */
	public Player getRandomPartyMemberState(Player player, byte state) {
		// if no valid player instance is passed, there is nothing to check...
		if (player == null) {
			return null;
		}
		
		// normal cases...if the player is not in a partym check the player's state
		QuestState temp = null;
		L2Party party = player.getParty();
		// if this player is not in a party, just check if this player instance matches the conditions itself
		if (party == null || party.getPartyMembers().isEmpty()) {
			temp = player.getQuestState(getName());
			if (temp != null && temp.getState() == state) {
				return player; // match
			}
			
			return null; // no match
		}
		
		// if the player is in a party, gather a list of all matching party members (possibly
		// including this player)
		ArrayList<Player> candidates = new ArrayList<>();
		
		// get the target for enforcing distance limitations.
		WorldObject target = player.getTarget();
		if (target == null) {
			target = player;
		}
		
		for (Player partyMember : party.getPartyMembers()) {
			if (partyMember == null) {
				continue;
			}
			temp = partyMember.getQuestState(getName());
			if (temp != null && temp.getState() == state && partyMember.isInsideRadius(target, 1500, true, false)) {
				candidates.add(partyMember);
			}
		}
		// if there was no match, return null...
		if (candidates.isEmpty()) {
			return null;
		}
		
		// if a match was found from the party, return one of them at random.
		return candidates.get(Rnd.get(candidates.size()));
	}
	
	/**
	 * Show HTML file to client
	 *
	 * @return String : message sent to client
	 */
	public String showHtmlFile(Player player, String fileName) {
		boolean questwindow = true;
		if (fileName.endsWith(".html") || player.getQuestState(name) != null && player.getQuestState(name).getState() >= State.STARTED) {
			questwindow = false;
		}
		int questId = getQuestIntId();
		//Create handler to file linked to the quest
		String content = getHtm(player.getHtmlPrefix(), fileName);
		
		if (player.getTarget() != null) {
			content = content.replaceAll("%objectId%", String.valueOf(player.getTarget().getObjectId()));
		}
		
		//Send message to client if message not empty
		if (content != null) {
			if (questwindow /*&& questId > 0 && questId < 20000 && questId != 999*/) {
				NpcQuestHtmlMessage npcReply = new NpcQuestHtmlMessage(5, questId);
				npcReply.setHtml(content);
				npcReply.replace("%playername%", player.getName());
				player.sendPacket(npcReply);
			} else {
				NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
				npcReply.setHtml(content);
				npcReply.replace("%playername%", player.getName());
				player.sendPacket(npcReply);
			}
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		
		return content;
	}
	
	/**
	 * Return HTML file contents
	 */
	public String getHtm(String prefix, String fileName) {
		String content =
				HtmCache.getInstance().getHtm(prefix, Config.DATA_FOLDER + "scripts/" + getDescr().toLowerCase() + "/" + getName() + "/" + fileName);
		if (content == null) {
			content = HtmCache.getInstance().getHtm(prefix, Config.DATA_FOLDER + "scripts/quests/Q" + getName() + "/" + fileName);
			if (content == null) {
				content = HtmCache.getInstance().getHtmForce(prefix, Config.DATA_FOLDER + "scripts/quests/" + getName() + "/" + fileName);
			}
		}
		
		return content;
	}
	
	// Method - Public
	
	/**
	 * Add a temporary (quest) spawn
	 * Return instance of newly spawned npc
	 */
	public Npc addSpawn(int npcId, Creature cha) {
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), false, 0, false);
	}
	
	/**
	 * Add a temporary (quest) spawn
	 * Return instance of newly spawned npc
	 * with summon animation
	 */
	public Npc addSpawn(int npcId, Creature cha, boolean isSummonSpawn) {
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), false, 0, isSummonSpawn);
	}
	
	public Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffSet, long despawnDelay) {
		return addSpawn(npcId, x, y, z, heading, randomOffSet, despawnDelay, false);
	}
	
	public Npc addSpawn(int npcId, int[] XYZ, int heading, boolean randomOffSet, int despawnDelay) {
		return addSpawn(npcId, XYZ[0], XYZ[1], XYZ[2], heading, randomOffSet, despawnDelay, false);
	}
	
	public Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay, boolean isSummonSpawn) {
		return addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, isSummonSpawn, 0);
	}
	
	public Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay, boolean isSummonSpawn, int instanceId) {
		Npc result = null;
		try {
			NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
			if (template != null) {
				// Sometimes, even if the quest script specifies some xyz (for example npc.getX() etc) by the time the code
				// reaches here, xyz have become 0!  Also, a questdev might have purposely set xy to 0,0...however,
				// the spawn code is coded such that if x=y=0, it looks into location for the spawn loc!  This will NOT work
				// with quest spawns!  For both of the above cases, we need a fail-safe spawn.  For this, we use the
				// default spawn location, which is at the player's loc.
				if (x == 0 && y == 0) {
					log.error("Failed to adjust bad coords for quest spawn! Spawn aborted!");
					return null;
				}
				if (randomOffset) {
					int offset;
					
					offset = Rnd.get(2); // Get the direction of the offset
					if (offset == 0) {
						offset = -1;
					} // make offset negative
					offset *= Rnd.get(50, 100);
					x += offset;
					
					offset = Rnd.get(2); // Get the direction of the offset
					if (offset == 0) {
						offset = -1;
					} // make offset negative
					offset *= Rnd.get(50, 100);
					y += offset;
				}
				L2Spawn spawn = new L2Spawn(template);
				spawn.setInstanceId(instanceId);
				spawn.setHeading(heading);
				spawn.setX(x);
				spawn.setY(y);
				spawn.setZ(z + 20);
				spawn.stopRespawn();
				spawn.doSpawn(isSummonSpawn);
				result = spawn.getNpc();
				
				if (despawnDelay > 0) {
					result.scheduleDespawn(despawnDelay);
				}
				
				return result;
			}
		} catch (Exception e1) {
			log.warn("Could not spawn Npc " + npcId);
		}
		
		return null;
	}
	
	public Trap addTrap(int trapId, int x, int y, int z, int heading, Skill skill, int instanceId) {
		NpcTemplate TrapTemplate = NpcTable.getInstance().getTemplate(trapId);
		Trap trap = new TrapInstance(IdFactory.getInstance().getNextId(), TrapTemplate, instanceId, -1, skill);
		trap.setCurrentHp(trap.getMaxHp());
		trap.setCurrentMp(trap.getMaxMp());
		trap.setIsInvul(true);
		trap.setHeading(heading);
		//World.getInstance().storeObject(trap);
		trap.spawnMe(x, y, z);
		
		return trap;
	}
	
	public Npc addMinion(MonsterInstance master, int minionId) {
		return MinionList.spawnMinion(master, minionId);
	}
	
	public int[] getRegisteredItemIds() {
		return questItemIds;
	}
	
	@Override
	public String getScriptName() {
		return getName();
	}
	
	@Override
	public void setActive(boolean status) {
		// TODO implement me
	}
	
	@Override
	public boolean reload() {
		unload();
		return super.reload();
	}
	
	@Override
	public boolean unload() {
		return unload(true);
	}
	
	public boolean unload(boolean removeFromList) {
		saveGlobalData();
		// cancel all pending timers before reloading.
		// if timers ought to be restarted, the quest can take care of it
		// with its code (example: save global data indicating what timer must
		// be restarted).
		for (List<QuestTimer> timers : allEventTimers.values()) {
			for (QuestTimer timer : timers) {
				timer.cancel();
			}
		}
		allEventTimers.clear();
		if (removeFromList) {
			return QuestManager.getInstance().removeQuest(this);
		} else {
			return true;
		}
	}
	
	@Override
	public ScriptManager<?> getScriptManager() {
		return QuestManager.getInstance();
	}
	
	public void setOnEnterWorld(boolean val) {
		onEnterWorld = val;
	}
	
	public boolean getOnEnterWorld() {
		return onEnterWorld;
	}
	
	public int getOnKillDelay(int npcId) {
		return 10000;
	}
	
	public boolean canStart(Player player) {
		return true;
	}
}
