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

package l2server.gameserver.ai;

import l2server.Config;
import l2server.gameserver.GeoData;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.TimeController;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.*;
import l2server.gameserver.model.actor.Attackable.AggroInfo;
import l2server.gameserver.model.actor.instance.*;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.gameserver.templates.chars.NpcTemplate.AIType;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectType;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.gameserver.templates.skills.SkillType;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import static l2server.gameserver.ai.CtrlIntention.*;

/**
 * This class manages AI of Attackable.<BR><BR>
 */
public class AttackableAI extends CreatureAI implements Runnable {
	private static Logger log = LoggerFactory.getLogger(AttackableAI.class.getName());


	//
	private static final int RANDOM_WALK_RATE = 30; // confirmed
	// private static final int MAX_DRIFT_RANGE = 300;
	private static final int MAX_ATTACK_TIMEOUT = 1200; // int ticks, i.e. 2min

	/**
	 * The Attackable AI task executed every 1s (call onEvtThink method)
	 */
	private Future<?> aiTask;

	/**
	 * The delay after which the attacked is stopped
	 */
	private int attackTimeout;

	/**
	 * The Attackable aggro counter
	 */
	private int globalAggro;

	/**
	 * The flag used to indicate that a thinking action is in progress
	 */
	private boolean thinking; // to prevent recursive thinking

	private int timepass = 0;
	private int chaostime = 0;
	private NpcTemplate skillrender;
	int lastBuffTick;

	/**
	 * Constructor of AttackableAI.<BR><BR>
	 *
	 */
	public AttackableAI(Creature creature) {
		super(creature);
		if (getActiveChar().getClonedPlayer() == null) {
			skillrender = NpcTable.getInstance().getTemplate(getActiveChar().getTemplate().NpcId);
		} else {
			skillrender = getActiveChar().getTemplate();
		}
		//selfAnalysis.doSpawn();
		attackTimeout = Integer.MAX_VALUE;
		globalAggro = -10; // 10 seconds timeout of ATTACK after respawn
	}

	@Override
	public void run() {
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}

	/**
	 * Return True if the target is autoattackable (depends on the actor type).<BR><BR>
	 * <p>
	 * <B><U> Actor is a GuardInstance</U> :</B><BR><BR>
	 * <li>The target isn't a Folk or a Door</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The Player target has karma (=PK)</li>
	 * <li>The MonsterInstance target is aggressive</li><BR><BR>
	 * <p>
	 * <B><U> Actor is a L2SiegeGuardInstance</U> :</B><BR><BR>
	 * <li>The target isn't a Folk or a Door</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>A siege is in progress</li>
	 * <li>The Player target isn't a Defender</li><BR><BR>
	 * <p>
	 * <B><U> Actor is a FriendlyMobInstance</U> :</B><BR><BR>
	 * <li>The target isn't a Folk, a Door or another Npc</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The Player target has karma (=PK)</li><BR><BR>
	 * <p>
	 * <B><U> Actor is a MonsterInstance</U> :</B><BR><BR>
	 * <li>The target isn't a Folk, a Door or another Npc</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The actor is Aggressive</li><BR><BR>
	 *
	 * @param target The targeted WorldObject
	 */
	private boolean autoAttackCondition(Creature target) {
		if (target == null || getActiveChar() == null) {
			return false;
		}

		Attackable me = getActiveChar();

		// Check if the target isn't invulnerable
		if (target.isInvul(me)) {
			// However EffectInvincible requires to check GMs specially
			if (target instanceof Player && target.isGM()) {
				return false;
			}
			if (target instanceof Summon && ((Summon) target).getOwner().isGM()) {
				return false;
			}
		}

		// Check if the target isn't a Folk or a Door
		if (target instanceof DoorInstance) {
			return false;
		}

		// Check if the target isn't dead, is in the Aggro range and is at the same height
		if (target.isAlikeDead() || target instanceof Playable && !me.isInsideRadius(target, me.getAggroRange(), true, false)) {
			return false;
		}

		// Check if the target is a L2PlayableInstance
		if (target instanceof Playable) {
			// Check if the AI isn't a Raid Boss, can See Silent Moving players and the target isn't in silent move mode
			if (!me.isRaid() && !me.canSeeThroughSilentMove() && ((Playable) target).isSilentMoving()) {
				return false;
			}
		}

		// Check if the target is a Player
		if (target instanceof Player) {
			// Don't take the aggro if the GM has the access level below or equal to GM_DONT_TAKE_AGGRO
			if (target.isGM() && !((Player) target).getAccessLevel().canTakeAggro()) {
				return false;
			}

			// TODO: Ideally, autoattack condition should be called from the AI script.  In that case,
			// it should only implement the basic behaviors while the script will add more specific
			// behaviors (like varka/ketra alliance, etc).  Once implemented, remove specialized stuff
			// from this location.  (Fulminus)

			// Check if player is an ally (comparing mem addr)
			if ("varka_silenos_clan".equals(me.getFactionId()) && ((Player) target).isAlliedWithVarka()) {
				return false;
			}
			if ("ketra_orc_clan".equals(me.getFactionId()) && ((Player) target).isAlliedWithKetra()) {
				return false;
			}
			// check if the target is within the grace period for JUST getting up from fake death
			if (((Player) target).isRecentFakeDeath()) {
				return false;
			}

			//if (selfAnalysis.cannotMoveOnLand && !target.isInsideZone(Creature.ZONE_WATER))
			//	return false;

			if (((Player) target).isPlayingEvent()) {
				return false;
			}

			if (me.getClonedPlayer() != null && (target.getLevel() < me.getLevel() || target.getLevel() > me.getLevel() + 5)) {
				return false;
			}
		}

		// Check if the target is a Summon
		if (target instanceof Summon) {
			Player owner = ((Summon) target).getOwner();
			if (owner != null) {
				// Don't take the aggro if the GM has the access level below or equal to GM_DONT_TAKE_AGGRO
				if (owner.isGM() && (owner.isInvul(me) || !owner.getAccessLevel().canTakeAggro())) {
					return false;
				}
				// Check if player is an ally (comparing mem addr)
				if ("varka_silenos_clan".equals(me.getFactionId()) && owner.isAlliedWithVarka()) {
					return false;
				}
				if ("ketra_orc_clan".equals(me.getFactionId()) && owner.isAlliedWithKetra()) {
					return false;
				}
			}
		}
		// Check if the actor is a GuardInstance
		if (getActiveChar() instanceof GuardInstance) {
			// Check if the Player target has karma (=PK)
			if (target instanceof Player && ((Player) target).getReputation() < 0)
			// Los Check
			{
				return GeoData.getInstance().canSeeTarget(me, target);
			}

			//if (target instanceof Summon)
			//	return ((Summon)target).getKarma() > 0;

			// Check if the MonsterInstance target is aggressive
			if (target instanceof MonsterInstance && Config.GUARD_ATTACK_AGGRO_MOB && target.getAI().getAttackTarget() != null) {
				return GeoData.getInstance().canSeeTarget(me, target);
			}

			if (!(target instanceof Npc) || getActiveChar().getEnemyClan() == null || ((Npc) target).getClan() == null) {
				return false;
			}

			if (getActiveChar().getEnemyClan().equals(((Npc) target).getClan())) {
				if (getActiveChar().isInsideRadius(target, getActiveChar().getEnemyRange(), false, false)) {
					return GeoData.getInstance().canSeeTarget(getActiveChar(), target);
				} else {
					return false;
				}
			}

			return false;
		} else if (getActiveChar() instanceof FriendlyMobInstance) { // the actor is a FriendlyMobInstance

			// Check if the target isn't another Npc
			if (target instanceof Npc) {
				return false;
			}

			// Check if the Player target has karma (=PK)
			if (target instanceof Player && ((Player) target).getReputation() < 0) {
				return GeoData.getInstance().canSeeTarget(me, target); // Los Check
			} else {
				return false;
			}
		} else {
			if (target instanceof Attackable) {
				if (getActiveChar().getEnemyClan() == null || ((Attackable) target).getClan() == null) {
					return false;
				}

				if (!target.isAutoAttackable(getActiveChar())) {
					return false;
				}

				if (getActiveChar().getEnemyClan().equals(((Attackable) target).getClan())) {
					if (getActiveChar().isInsideRadius(target, getActiveChar().getEnemyRange(), false, false)) {
						return GeoData.getInstance().canSeeTarget(getActiveChar(), target);
					} else {
						return false;
					}
				}
				if (getActiveChar().getIsChaos() > 0 && me.isInsideRadius(target, getActiveChar().getIsChaos(), false, false)) {
					if (getActiveChar().getFactionId() != null && getActiveChar().getFactionId().equals(((Attackable) target).getFactionId())) {
						return false;
					}
					// Los Check
					return GeoData.getInstance().canSeeTarget(me, target);
				}
			}

			if (target instanceof Attackable || target instanceof Npc) {
				return false;
			}

			// depending on config, do not allow mobs to attack _new_ players in peacezones,
			// unless they are already following those players from outside the peacezone.
			if (!Config.ALT_MOB_AGRO_IN_PEACEZONE && target.isInsideZone(Creature.ZONE_PEACE)) {
				return false;
			}

			if (me.isChampion() && Config.L2JMOD_CHAMPION_PASSIVE) {
				return false;
			}

			// Check if the actor is Aggressive
			return me.isAggressive() && GeoData.getInstance().canSeeTarget(me, target);
		}
	}

	public void startAITask() {
		// If not idle - create an AI task (schedule onEvtThink repeatedly)
		if (aiTask == null) {
			aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 1000);
		}
	}

	@Override
	public void stopAITask() {
		if (aiTask != null) {
			aiTask.cancel(false);
			aiTask = null;
		}
		super.stopAITask();
	}

	/**
	 * Set the Intention of this CreatureAI and create an  AI Task executed every 1s (call onEvtThink method) for this Attackable.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If actor knowPlayer isn't EMPTY, AI_INTENTION_IDLE will be change in AI_INTENTION_ACTIVE</B></FONT><BR><BR>
	 *
	 * @param intention The new Intention to set to the AI
	 * @param arg0      The first parameter of the Intention
	 * @param arg1      The second parameter of the Intention
	 */
	@Override
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1) {
		if (intention == AI_INTENTION_IDLE || intention == AI_INTENTION_ACTIVE) {
			// Check if actor is not dead
			Attackable npc = getActiveChar();
			if (!npc.isAlikeDead()) {
				// If its knownPlayer isn't empty set the Intention to AI_INTENTION_ACTIVE
				if (!npc.getKnownList().getKnownPlayers().isEmpty()) {
					intention = AI_INTENTION_ACTIVE;
				} else {
					if (npc.getSpawn() != null) {
						final int range = Config.MAX_DRIFT_RANGE;
						if (!npc.isInsideRadius(npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ(), range + range, true, false)) {
							intention = AI_INTENTION_ACTIVE;
						}
					}
				}
			}

			if (intention == AI_INTENTION_IDLE) {
				// Set the Intention of this AttackableAI to AI_INTENTION_IDLE
				super.changeIntention(AI_INTENTION_IDLE, null, null);

				// Stop AI task and detach AI from NPC
				if (aiTask != null) {
					aiTask.cancel(true);
					aiTask = null;
				}

				// Cancel the AI
				actor.detachAI();

				return;
			}
		}

		// Set the Intention of this AttackableAI to intention
		super.changeIntention(intention, arg0, arg1);

		// If not idle - create an AI task (schedule onEvtThink repeatedly)
		startAITask();
	}

	/**
	 * Manage the Attack Intention : Stop current Attack (if necessary), Calculate attack timeout, Start a new Attack and Launch Think Event.<BR><BR>
	 *
	 * @param target The Creature to attack
	 */
	@Override
	protected void onIntentionAttack(Creature target) {
		// Calculate the attack timeout
		attackTimeout = MAX_ATTACK_TIMEOUT + TimeController.getGameTicks();

		// self and buffs

		if (lastBuffTick + 30 < TimeController.getGameTicks()) {
			if (skillrender.hasBuffSkill()) {
				for (Skill sk : skillrender.aiSkills[NpcTemplate.AIST_BUFF]) {
					if (cast(sk)) {
						break;
					}
				}
			}

			lastBuffTick = TimeController.getGameTicks();
		}

		// Manage the Attack Intention : Stop current Attack (if necessary), Start a new Attack and Launch Think Event
		super.onIntentionAttack(target);
	}

	private void thinkCast() {
		if (checkTargetLost(getCastTarget())) {
			setCastTarget(null);
			return;
		}
		if (maybeMoveToPawn(getCastTarget(), actor.getMagicalAttackRange(skill))) {
			return;
		}
		clientStopMoving(null);
		setIntention(AI_INTENTION_ACTIVE);
		actor.doCast(skill, false);
	}

	/**
	 * Manage AI standard thinks of a Attackable (called by onEvtThink).<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Update every 1s the globalAggro counter to come close to 0</li>
	 * <li>If the actor is Aggressive and can attack, add all autoAttackable Creature in its Aggro Range to its aggroList, chose a target and order to attack it</li>
	 * <li>If the actor is a GuardInstance that can't attack, order to it to return to its home location</li>
	 * <li>If the actor is a MonsterInstance that can't attack, order to it to random walk (1/100)</li><BR><BR>
	 */
	private void thinkActive() {
		Attackable npc = getActiveChar();

		// Update every 1s the globalAggro counter to come close to 0
		if (globalAggro != 0) {
			if (globalAggro < 0) {
				globalAggro++;
			} else {
				globalAggro--;
			}
		}

		// Add all autoAttackable Creature in Attackable Aggro Range to its aggroList with 0 damage and 1 hate
		// A Attackable isn't aggressive during 10s after its spawn because globalAggro is set to -10
		if (globalAggro >= 0) {
			// Get all visible objects inside its Aggro Range
			Collection<WorldObject> objs = npc.getKnownList().getKnownObjects().values();
			//synchronized (npc.getKnownList().getKnownObjects())
			{
				for (WorldObject obj : objs) {
					if (!(obj instanceof Creature)) {
						continue;
					}
					Creature target = (Creature) obj;

					// TODO: The AI Script ought to handle aggro behaviors in onSee.  Once implemented, aggro behaviors ought
					// to be removed from here.  (Fulminus)
					// For each Creature check if the target is autoattackable
					if (autoAttackCondition(target)) // check aggression
					{
						// Get the hate level of the Attackable against this Creature target contained in aggroList
						int hating = npc.getHating(target);

						// Add the attacker to the Attackable aggroList with 0 damage and 0 hate
						if (hating == 0) {
							npc.addDamageHate(target, 0, 0);
						}
					}
				}
			}

			// Chose a target from its aggroList
			Creature hated;
			if (npc.isConfused()) {
				hated = getAttackTarget(); // effect handles selection
			} else {
				hated = npc.getMostHated();
			}

			// Order to the Attackable to attack the target
			if (hated != null && !npc.isCoreAIDisabled()) {
				// Get the hate level of the Attackable against this Creature target contained in aggroList
				int aggro = npc.getHating(hated);

				if (aggro + globalAggro > 0) {
					// Set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others Player
					if (!npc.isRunning()) {
						npc.setRunning();
					}

					// Set the AI Intention to AI_INTENTION_ATTACK
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated);
				}

				return;
			}
		}

		// Chance to forget attackers after some time
		if (npc.getCurrentHp() == npc.getMaxHp() && npc.getCurrentMp() == npc.getMaxMp() && !npc.getAttackByList().isEmpty() &&
				Rnd.nextInt(500) == 0) {
			npc.clearAggroList();
			npc.getAttackByList().clear();
			if (npc instanceof MonsterInstance) {
				if (((MonsterInstance) npc).hasMinions()) {
					((MonsterInstance) npc).getMinionList().deleteReusedMinions();
				}
			}
		}

		// Check if the mob should not return to spawn point
		if (!npc.canReturnToSpawnPoint()) {
			return;
		}

		// Check if the actor is a GuardInstance
		if (npc instanceof GuardInstance) {
			// Order to the GuardInstance to return to its home location because there's no target to attack
			npc.returnHome();
		}

		// Minions following leader
		final Creature leader = npc.getLeader();
		if (leader != null && !leader.isAlikeDead()) {
			final int offset;
			final int minRadius = 30;

			if (npc.isRaidMinion()) {
				offset = 500; // for Raids - need correction
			} else {
				offset = 200; // for normal minions - need correction :)
			}

			if (leader.isRunning()) {
				npc.setRunning();
			} else {
				npc.setWalking();
			}

			if (npc.getPlanDistanceSq(leader) > offset * offset) {
				int x1, y1, z1;
				x1 = Rnd.get(minRadius * 2, offset * 2); // x
				y1 = Rnd.get(x1, offset * 2); // distance
				y1 = (int) Math.sqrt(y1 * y1 - x1 * x1); // y
				if (x1 > offset + minRadius) {
					x1 = leader.getX() + x1 - offset;
				} else {
					x1 = leader.getX() - x1 + minRadius;
				}
				if (y1 > offset + minRadius) {
					y1 = leader.getY() + y1 - offset;
				} else {
					y1 = leader.getY() - y1 + minRadius;
				}

				z1 = leader.getZ();
				// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
				moveTo(x1, y1, z1);
			} else if (Rnd.nextInt(RANDOM_WALK_RATE) == 0) {
				if (skillrender.hasBuffSkill()) {
					for (Skill sk : skillrender.aiSkills[NpcTemplate.AIST_BUFF]) {
						if (cast(sk)) {
							return;
						}
					}
				}
			}
		}
		// Order to the MonsterInstance to random walk (1/100)
		else if (npc.getSpawn() != null && Rnd.nextInt(RANDOM_WALK_RATE) == 0) {
			int x1, y1, z1;
			final int range = Config.MAX_DRIFT_RANGE;

			if (skillrender != null && skillrender.hasBuffSkill()) {
				for (Skill sk : skillrender.aiSkills[NpcTemplate.AIST_BUFF]) {
					if (cast(sk)) {
						return;
					}
				}
			}

			// If NPC with random coord in territory
			if (npc.getSpawn().getGroup() != null) {
				// Calculate a destination point in the spawn area
				int p[] = npc.getSpawn().getGroup().getRandomPoint();
				x1 = p[0];
				y1 = p[1];

				// Calculate the distance between the current position of the Creature and the target (x,y)
				double distance2 = npc.getPlanDistanceSq(x1, y1);
				if (distance2 > (range + range) * (range + range)) {
					npc.setisReturningToSpawnPoint(true);
					float delay = (float) Math.sqrt(distance2) / range;
					x1 = npc.getX() + (int) ((x1 - npc.getX()) / delay);
					y1 = npc.getY() + (int) ((y1 - npc.getY()) / delay);
				}

				z1 = GeoData.getInstance().getHeight(x1, y1, npc.getZ());
			} else {
				// If NPC with fixed coord
				x1 = npc.getSpawn().getX();
				y1 = npc.getSpawn().getY();
				z1 = npc.getSpawn().getZ();

				if (!npc.isInsideRadius(x1, y1, range, false)) {
					npc.setisReturningToSpawnPoint(true);
				} else if (npc.isRndWalk()) {
					x1 = Rnd.nextInt(range * 2); // x
					y1 = Rnd.get(x1, range * 2); // distance
					y1 = (int) Math.sqrt(y1 * y1 - x1 * x1); // y
					x1 += npc.getSpawn().getX() - range;
					y1 += npc.getSpawn().getY() - range;
					z1 = GeoData.getInstance().getHeight(x1, y1, npc.getZ());
				} else {
					return;
				}
			}

			//Logozo.debug("Current pos ("+getX()+", "+getY()+"), moving to ("+x1+", "+y1+").");
			// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
			moveTo(x1, y1, z1);
		}
	}

	/**
	 * Manage AI attack thinks of a Attackable (called by onEvtThink).<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Update the attack timeout if actor is running</li>
	 * <li>If target is dead or timeout is expired, stop this attack and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>Call all WorldObject of its Faction inside the Faction Range</li>
	 * <li>Chose a target and order to attack it with magic skill or physical attack</li><BR><BR>
	 * <p>
	 * TODO: Manage casting Rule to healer mobs (like Ant Nurses)
	 */
	private boolean callingFaction = false;

	private void thinkAttack() {
		final Attackable npc = getActiveChar();
		if (npc.isCastingNow()) {
			return;
		}

		Creature originalAttackTarget = getAttackTarget();

		// Check if target is dead or if timeout is expired to stop this attack
		if (originalAttackTarget == null || originalAttackTarget.isAlikeDead() || attackTimeout < TimeController.getGameTicks() ||
				!npc.isRaid() && originalAttackTarget.isAffected(EffectType.UNTARGETABLE.getMask())) {
			if (attackTimeout < TimeController.getGameTicks() && originalAttackTarget instanceof Npc &&
					((Npc) originalAttackTarget).getClan() != null &&
					((Npc) originalAttackTarget).getClan().equalsIgnoreCase(npc.getEnemyClan())) {
				attackTimeout = MAX_ATTACK_TIMEOUT + TimeController.getGameTicks();
				return;
			}
			// Stop hating this target after the attack timeout or if target is dead
			if (originalAttackTarget != null) {
				npc.stopHating(originalAttackTarget);
			}

			// Set the AI Intention to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);

			npc.setWalking();
			return;
		}

		final int collision = npc.getTemplate().collisionRadius;

		// Handle all WorldObject of its Faction inside the Faction Range

		String faction_id = getActiveChar().getFactionId();
		if (faction_id != null && !faction_id.isEmpty() && Thread.currentThread().getStackTrace().length < 50) // Mega ugly check, but...
		{
			callingFaction = true;
			int factionRange = npc.getClanRange() + collision;
			// Go through all WorldObject that belong to its faction
			Collection<WorldObject> objs = npc.getKnownList().getKnownObjects().values();
			//synchronized (actor.getKnownList().getKnownObjects())
			try {
				for (WorldObject obj : objs) {
					if (obj instanceof Npc) {
						Npc called = (Npc) obj;

						//Handle SevenSigns mob Factions
						final String npcfaction = called.getFactionId();
						if (npcfaction == null || npcfaction.isEmpty()) {
							continue;
						}

						if (!faction_id.equals(npcfaction)) {
							continue;
						}

						// Check if the WorldObject is inside the Faction Range of
						// the actor
						if (npc.isInsideRadius(called, factionRange, true, false) && called.hasAI()) {
							if (Math.abs(originalAttackTarget.getZ() - called.getZ()) < 600 && npc.getAttackByList().contains(originalAttackTarget) &&
									(called.getAI().intention == CtrlIntention.AI_INTENTION_IDLE ||
											called.getAI().intention == CtrlIntention.AI_INTENTION_ACTIVE) &&
									called.getInstanceId() == npc.getInstanceId() &&
									!(called instanceof Attackable && ((AttackableAI) called.getAI()).callingFaction))
							//									&& GeoData.getInstance().canSeeTarget(called, npc))
							{
								if (originalAttackTarget instanceof Playable) {
									Quest[] quests = called.getTemplate().getEventQuests(Quest.QuestEventType.ON_FACTION_CALL);
									if (quests != null) {
										Player player = originalAttackTarget.getActingPlayer();
										boolean isSummon = originalAttackTarget instanceof Summon;
										for (Quest quest : quests) {
											quest.notifyFactionCall(called, getActiveChar(), player, isSummon);
										}
									}
								} else if (called instanceof Attackable && getAttackTarget() != null &&
										called.getAI().intention != CtrlIntention.AI_INTENTION_ATTACK) {
									((Attackable) called).addDamageHate(getAttackTarget(), 0, npc.getHating(getAttackTarget()));
									called.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getAttackTarget());
								}
							}
						}
					}
				}
			} catch (NullPointerException e) {
				log.warn("AttackableAI: thinkAttack() faction call failed: " + e.getMessage(), e);
			}

			callingFaction = false;
		}

		if (npc.isCoreAIDisabled()) {
			return;
		}

		/*
		if (actor.getTarget() == null || this.getAttackTarget() == null || this.getAttackTarget().isDead() || ctarget == actor)
			AggroReconsider();
		 */

		//----------------------------------------------------------------

		//------------------------------------------------------------------------------
		//Initialize data
		Creature mostHate = npc.getMostHated();
		if (mostHate == null) {
			setIntention(AI_INTENTION_ACTIVE);
			return;
		}

		setAttackTarget(mostHate);
		npc.setTarget(mostHate);

		final int combinedCollision = collision + mostHate.getTemplate().collisionRadius;

		//------------------------------------------------------
		// In case many mobs are trying to hit from same place, move a bit,
		// circling around the target
		// Note from Gnacik:
		// On l2js because of that sometimes mobs don't attack player only running
		// around player without any sense, so decrease chance for now
		if (!npc.isMovementDisabled() && Rnd.nextInt(100) <= 3) {
			for (WorldObject nearby : npc.getKnownList().getKnownObjects().values()) {
				if (nearby instanceof Attackable && npc.isInsideRadius(nearby, collision, false, false) && nearby != mostHate) {
					int newX = combinedCollision + Rnd.get(40);
					if (Rnd.nextBoolean()) {
						newX = mostHate.getX() + newX;
					} else {
						newX = mostHate.getX() - newX;
					}
					int newY = combinedCollision + Rnd.get(40);
					if (Rnd.nextBoolean()) {
						newY = mostHate.getY() + newY;
					} else {
						newY = mostHate.getY() - newY;
					}

					if (!npc.isInsideRadius(newX, newY, collision, false)) {
						int newZ = npc.getZ() + 30;
						if (Config.GEODATA == 0 || GeoData.getInstance()
								.canMoveFromToTarget(npc.getX(), npc.getY(), npc.getZ(), newX, newY, newZ, npc.getInstanceId())) {
							moveTo(newX, newY, newZ);
						}
					}
					return;
				}
			}
		}
		//Dodge if its needed
		if (!npc.isMovementDisabled() && npc.getCanDodge() > 0) {
			if (Rnd.get(100) <= npc.getCanDodge()) {
				// Micht: kepping this one otherwise we should do 2 sqrt
				double distance2 = npc.getPlanDistanceSq(mostHate.getX(), mostHate.getY());
				if (Math.sqrt(distance2) <= 60 + combinedCollision) {
					int posX = npc.getX();
					int posY = npc.getY();
					int posZ = npc.getZ() + 30;

					if (Rnd.nextBoolean()) {
						posX = posX + Rnd.get(100);
					} else {
						posX = posX - Rnd.get(100);
					}

					if (Rnd.nextBoolean()) {
						posY = posY + Rnd.get(100);
					} else {
						posY = posY - Rnd.get(100);
					}

					if (Config.GEODATA == 0 ||
							GeoData.getInstance().canMoveFromToTarget(npc.getX(), npc.getY(), npc.getZ(), posX, posY, posZ, npc.getInstanceId())) {
						setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(posX, posY, posZ, 0));
					}
					return;
				}
			}
		}

		//------------------------------------------------------------------------------
		// BOSS/Raid Minion Target Reconsider
		if (npc.isRaid() || npc.isRaidMinion()) {
			chaostime++;
			if (Config.isServer(Config.TENKAI)) {
				if (npc instanceof RaidBossInstance) {
					if (!((MonsterInstance) npc).hasMinions()) {
						if (chaostime > Config.RAID_CHAOS_TIME) {
							if (Rnd.get(100) <= 100 - npc.getCurrentHp() * 100 / npc.getMaxHp()) {
								aggroReconsider();
								chaostime = 0;
								return;
							}
						}
					} else {
						if (chaostime > Config.RAID_CHAOS_TIME) {
							if (Rnd.get(100) <= 100 - npc.getCurrentHp() * 200 / npc.getMaxHp()) {
								aggroReconsider();
								chaostime = 0;
								return;
							}
						}
					}
				} else if (npc instanceof GrandBossInstance) {
					if (chaostime > Config.GRAND_CHAOS_TIME) {
						double chaosRate = 100 - npc.getCurrentHp() * 300 / npc.getMaxHp();
						if (chaosRate <= 10 && Rnd.get(100) <= 10 || chaosRate > 10 && Rnd.get(100) <= chaosRate) {
							aggroReconsider();
							chaostime = 0;
							return;
						}
					}
				} else {
					if (chaostime > Config.MINION_CHAOS_TIME) {
						if (Rnd.get(100) <= 100 - npc.getCurrentHp() * 200 / npc.getMaxHp()) {
							aggroReconsider();
							chaostime = 0;
							return;
						}
					}
				}
			}
		}

		if (skillrender.hasSkill()) {
			//-------------------------------------------------------------------------------
			//Heal Condition
			if (skillrender.hasHealSkill() && skillrender.aiSkills[NpcTemplate.AIST_HEAL] != null) {
				double percentage = npc.getCurrentHp() / npc.getMaxHp() * 100;
				if (npc.isMinion()) {
					Creature leader = npc.getLeader();
					if (leader != null && !leader.isDead() && Rnd.get(100) > leader.getCurrentHp() / leader.getMaxHp() * 100) {
						for (Skill sk : skillrender.aiSkills[NpcTemplate.AIST_HEAL]) {
							if (sk.getTargetType() == SkillTargetType.TARGET_SELF) {
								continue;
							}
							if (sk.getMpConsume() >= npc.getCurrentMp() || npc.isSkillDisabled(sk) || sk.isMagic() && npc.isMuted() ||
									!sk.isMagic() && npc.isPhysicalMuted()) {
								continue;
							}
							if (!Util.checkIfInRange(sk.getCastRange() + collision + leader.getTemplate().collisionRadius, npc, leader, false) &&
									!isParty(sk) && !npc.isMovementDisabled()) {
								moveToPawn(leader, sk.getCastRange() + collision + leader.getTemplate().collisionRadius);
								return;
							}
							if (GeoData.getInstance().canSeeTarget(npc, leader)) {
								clientStopMoving(null);
								npc.setTarget(leader);
								clientStopMoving(null);
								npc.doCast(sk);
								return;
							}
						}
					}
				}
				if (percentage < 60) {
					for (Skill sk : skillrender.aiSkills[NpcTemplate.AIST_HEAL]) {
						if (sk.getMpConsume() >= npc.getCurrentMp() || npc.isSkillDisabled(sk) || sk.isMagic() && npc.isMuted() ||
								!sk.isMagic() && npc.isPhysicalMuted()) {
							continue;
						}
						clientStopMoving(null);
						npc.setTarget(npc);
						npc.doCast(sk);
						return;
					}
				}
				for (Skill sk : skillrender.aiSkills[NpcTemplate.AIST_HEAL]) {
					if (sk.getMpConsume() >= npc.getCurrentMp() || npc.isSkillDisabled(sk) || sk.isMagic() && npc.isMuted() ||
							!sk.isMagic() && npc.isPhysicalMuted()) {
						continue;
					}

					int allies = 0;
					for (Creature obj : npc.getKnownList().getKnownCharactersInRadius(sk.getCastRange() + collision)) {
						if (!(obj instanceof Attackable) || obj.isDead()) {
							continue;
						}

						Attackable targets = (Attackable) obj;
						if (npc.getFactionId() != null && !npc.getFactionId().equals(targets.getFactionId())) {
							continue;
						}
						percentage = targets.getCurrentHp() / targets.getMaxHp() * 100;
						if (percentage < 70 && sk.getTargetType() == SkillTargetType.TARGET_ONE) {
							if (GeoData.getInstance().canSeeTarget(npc, targets)) {
								clientStopMoving(null);
								npc.setTarget(obj);
								npc.doCast(sk);
								return;
							}
						}

						allies++;
					}

					if (allies > 0 && isParty(sk)) {
						clientStopMoving(null);
						npc.doCast(sk);
						return;
					}
				}
			}
			//-------------------------------------------------------------------------------
			//Res Skill Condition
			if (skillrender.hasResSkill()) {
				if (npc.isMinion()) {
					Creature leader = npc.getLeader();
					if (leader != null && leader.isDead()) {
						for (Skill sk : skillrender.aiSkills[NpcTemplate.AIST_RES]) {
							if (sk.getTargetType() == SkillTargetType.TARGET_SELF) {
								continue;
							}
							if (sk.getMpConsume() >= npc.getCurrentMp() || npc.isSkillDisabled(sk) || sk.isMagic() && npc.isMuted() ||
									!sk.isMagic() && npc.isPhysicalMuted()) {
								continue;
							}
							if (!Util.checkIfInRange(sk.getCastRange() + collision + leader.getTemplate().collisionRadius, npc, leader, false) &&
									!isParty(sk) && !npc.isMovementDisabled()) {
								moveToPawn(leader, sk.getCastRange() + collision + leader.getTemplate().collisionRadius);
								return;
							}
							if (GeoData.getInstance().canSeeTarget(npc, leader)) {
								clientStopMoving(null);
								npc.setTarget(leader);
								npc.doCast(sk);
								return;
							}
						}
					}
				}
				if (skillrender.aiSkills[NpcTemplate.AIST_RES] != null) {
					for (Skill sk : skillrender.aiSkills[NpcTemplate.AIST_RES]) {
						if (sk.getMpConsume() >= npc.getCurrentMp() || npc.isSkillDisabled(sk) || sk.isMagic() && npc.isMuted() ||
								!sk.isMagic() && npc.isPhysicalMuted()) {
							continue;
						}
						if (sk.getTargetType() == SkillTargetType.TARGET_ONE) {
							for (Creature obj : npc.getKnownList().getKnownCharactersInRadius(sk.getCastRange() + collision)) {
								if (!(obj instanceof Attackable) || !obj.isDead()) {
									continue;
								}

								Attackable targets = (Attackable) obj;
								if (npc.getFactionId() != null && !npc.getFactionId().equals(targets.getFactionId())) {
									continue;
								}
								if (Rnd.get(100) < 10) {
									if (GeoData.getInstance().canSeeTarget(npc, targets)) {
										clientStopMoving(null);
										npc.setTarget(obj);
										npc.doCast(sk);
										return;
									}
								}
							}
						}
						if (isParty(sk)) {
							clientStopMoving(null);
							WorldObject target = getAttackTarget();
							npc.setTarget(npc);
							npc.doCast(sk);
							npc.setTarget(target);
							return;
						}
					}
				}
			}
		}

		double dist = Math.sqrt(npc.getPlanDistanceSq(mostHate.getX(), mostHate.getY()));
		int dist2 = (int) dist - collision;
		int range = npc.getPhysicalAttackRange() + combinedCollision;
		if (mostHate.isMoving()) {
			range = range + 50;
			if (npc.isMoving()) {
				range = range + 50;
			}
		}

		//-------------------------------------------------------------------------------
		//Immobilize Condition
		if (npc.isMovementDisabled() && (dist > range || mostHate.isMoving()) || dist > range && mostHate.isMoving()) {
			movementDisable();
			return;
		}
		setTimepass(0);
		//--------------------------------------------------------------------------------
		//Skill Use
		if (skillrender.hasSkill()) {
			if (Rnd.get(100) <= npc.getSkillChance()) {
				Skill skills =
						skillrender.aiSkills[NpcTemplate.AIST_GENERAL].get(Rnd.nextInt(skillrender.aiSkills[NpcTemplate.AIST_GENERAL].size()));
				if (cast(skills)) {
					return;
				}
				for (Skill sk : skillrender.aiSkills[NpcTemplate.AIST_GENERAL]) {
					if (cast(sk)) {
						return;
					}
				}
			}

			//--------------------------------------------------------------------------------
			//Long/Short Range skill Usage
			if (npc.hasLSkill() || npc.hasSSkill()) {
				if (npc.hasSSkill() && dist2 <= 150 && Rnd.get(100) <= npc.getSSkillChance()) {
					sSkillRender();
					if (skillrender.aiSkills[NpcTemplate.AIST_SHORT_RANGE] != null) {
						Skill skills =
								skillrender.aiSkills[NpcTemplate.AIST_SHORT_RANGE].get(Rnd.nextInt(skillrender.aiSkills[NpcTemplate.AIST_SHORT_RANGE]
										.size()));
						if (cast(skills)) {
							return;
						}
						for (Skill sk : skillrender.aiSkills[NpcTemplate.AIST_SHORT_RANGE]) {
							if (cast(sk)) {
								return;
							}
						}
					}
				}
				if (npc.hasLSkill() && dist2 > 150 && Rnd.get(100) <= npc.getLSkillChance()) {
					lSkillRender();
					if (skillrender.aiSkills[NpcTemplate.AIST_LONG_RANGE] != null) {
						Skill skills =
								skillrender.aiSkills[NpcTemplate.AIST_LONG_RANGE].get(Rnd.nextInt(skillrender.aiSkills[NpcTemplate.AIST_LONG_RANGE]
										.size()));
						if (cast(skills)) {
							return;
						}
						for (Skill sk : skillrender.aiSkills[NpcTemplate.AIST_LONG_RANGE]) {
							if (cast(sk)) {
								return;
							}
						}
					}
				}
			}
		}

		//--------------------------------------------------------------------------------
		// Starts Melee or Primary Skill
		if (dist2 > range || !GeoData.getInstance().canSeeTarget(npc, mostHate)) {
			if (npc.isMovementDisabled()) {
				targetReconsider();
			} else {
				if (getAttackTarget() == null) {
					return;
				}
				if (getAttackTarget().isMoving()) {
					range -= 100;
				}
				if (range < 5) {
					range = 5;
				}
				moveToPawn(getAttackTarget(), range);
			}
		} else {
			melee(npc.getPrimaryAttack());
		}
	}

	private void melee(int type) {
		if (type != 0) {
			switch (type) {
				case -1: {
					if (skillrender.aiSkills[NpcTemplate.AIST_GENERAL] != null) {
						Skill s =
								skillrender.aiSkills[NpcTemplate.AIST_GENERAL].get(Rnd.nextInt(skillrender.aiSkills[NpcTemplate.AIST_GENERAL].size()));
						if (cast(s)) {
							return;
						}
						for (Skill sk : skillrender.aiSkills[NpcTemplate.AIST_GENERAL]) {
							if (cast(sk)) {
								return;
							}
						}
					}
					break;
				}
				case 1: {
					if (skillrender.hasAtkSkill()) {
						Skill s =
								skillrender.aiSkills[NpcTemplate.AIST_ATK].get(Rnd.nextInt(skillrender.aiSkills[NpcTemplate.AIST_ATK].size()));
						if (cast(s)) {
							return;
						}
						for (Skill sk : skillrender.aiSkills[NpcTemplate.AIST_ATK]) {
							if (cast(sk)) {
								return;
							}
						}
					}
					break;
				}
				default: {
					if (skillrender.aiSkills[NpcTemplate.AIST_GENERAL] != null) {
						for (Skill sk : skillrender.aiSkills[NpcTemplate.AIST_GENERAL]) {
							if (sk.getId() == getActiveChar().getPrimaryAttack() && cast(sk)) {
								return;
							}
						}
					}
				}
				break;
			}
		}

		actor.doAttack(getAttackTarget());
	}

	private boolean cast(Skill sk) {
		if (sk == null) {
			return false;
		}

		final Attackable caster = getActiveChar();

		if (caster.isCastingNow() && !sk.isSimultaneousCast()) {
			return false;
		}

		if (sk.getMpConsume() >= caster.getCurrentMp() || caster.isSkillDisabled(sk) || sk.isMagic() && caster.isMuted() ||
				!sk.isMagic() && caster.isPhysicalMuted()) {
			return false;
		}
		if (getAttackTarget() == null && caster.getMostHated() != null) {
			setAttackTarget(caster.getMostHated());
		}
		Creature attackTarget = getAttackTarget();
		if (attackTarget == null) {
			return false;
		}
		double dist = Math.sqrt(caster.getPlanDistanceSq(attackTarget.getX(), attackTarget.getY()));
		double dist2 = dist - attackTarget.getTemplate().collisionRadius;
		double srange = sk.getCastRange() + caster.getTemplate().collisionRadius;
		if (attackTarget.isMoving()) {
			dist2 = dist2 - 30;
		}

		switch (sk.getSkillType()) {

			case BUFF: {
				if (caster.getFirstEffect(sk) == null) {
					clientStopMoving(null);
					//WorldObject target = attackTarget;
					caster.setTarget(caster);
					caster.doCast(sk);
					//actor.setTarget(target);
					return true;
				}
				//----------------------------------------
				//If actor already have buff, start looking at others same faction mob to cast
				if (sk.getTargetType() == SkillTargetType.TARGET_SELF) {
					return false;
				}
				if (sk.getTargetType() == SkillTargetType.TARGET_ONE || sk.getTargetType() == SkillTargetType.TARGET_SINGLE) {
					Creature target = effectTargetReconsider(sk, true);
					if (target != null) {
						clientStopMoving(null);
						caster.setTarget(target);
						caster.doCast(sk);
						caster.setTarget(attackTarget);
						return true;
					}
				}
				if (canParty(sk)) {
					clientStopMoving(null);
					caster.setTarget(caster);
					caster.doCast(sk);
					caster.setTarget(attackTarget);
					return true;
				}
				break;
			}
			case HEAL:
			case HEAL_PERCENT:
			case HEAL_STATIC:
			case BALANCE_LIFE: {
				double percentage = caster.getCurrentHp() / caster.getMaxHp() * 100;
				if (caster.isMinion() && sk.getTargetType() != SkillTargetType.TARGET_SELF) {
					Creature leader = caster.getLeader();
					if (leader != null && !leader.isDead() && Rnd.get(100) > leader.getCurrentHp() / leader.getMaxHp() * 100) {
						if (!Util.checkIfInRange(sk.getCastRange() + caster.getTemplate().collisionRadius + leader.getTemplate().collisionRadius,
								caster,
								leader,
								false) && !isParty(sk) && !caster.isMovementDisabled()) {
							moveToPawn(leader, sk.getCastRange() + caster.getTemplate().collisionRadius + leader.getTemplate().collisionRadius);
						}
						if (GeoData.getInstance().canSeeTarget(caster, leader)) {
							clientStopMoving(null);
							caster.setTarget(leader);
							caster.doCast(sk);
							return true;
						}
					}
				}
				if (Rnd.get(100) < (100 - percentage) / 3) {
					clientStopMoving(null);
					caster.setTarget(caster);
					caster.doCast(sk);
					return true;
				}

				if (sk.getTargetType() == SkillTargetType.TARGET_ONE) {
					for (Creature obj : caster.getKnownList()
							.getKnownCharactersInRadius(sk.getCastRange() + caster.getTemplate().collisionRadius)) {
						if (!(obj instanceof Attackable) || obj.isDead()) {
							continue;
						}

						Attackable targets = (Attackable) obj;
						if (caster.getFactionId() != null && !caster.getFactionId().equals(targets.getFactionId())) {
							continue;
						}
						percentage = targets.getCurrentHp() / targets.getMaxHp() * 100;
						if (Rnd.get(100) < (100 - percentage) / 10) {
							if (GeoData.getInstance().canSeeTarget(caster, targets)) {
								clientStopMoving(null);
								caster.setTarget(obj);
								caster.doCast(sk);
								return true;
							}
						}
					}
				}
				if (isParty(sk)) {
					for (Creature obj : caster.getKnownList()
							.getKnownCharactersInRadius(sk.getSkillRadius() + caster.getTemplate().collisionRadius)) {
						if (!(obj instanceof Attackable)) {
							continue;
						}
						Npc targets = (Npc) obj;
						if (caster.getFactionId() != null && targets.getFactionId().equals(caster.getFactionId())) {
							if (obj.getCurrentHp() < obj.getMaxHp() && Rnd.get(100) <= 20) {
								clientStopMoving(null);
								caster.setTarget(caster);
								caster.doCast(sk);
								return true;
							}
						}
					}
				}
				break;
			}
			case RESURRECT: {
				if (!isParty(sk)) {
					if (caster.isMinion() && sk.getTargetType() != SkillTargetType.TARGET_SELF) {
						Creature leader = caster.getLeader();
						if (leader != null && leader.isDead()) {
							if (!Util.checkIfInRange(sk.getCastRange() + caster.getTemplate().collisionRadius + leader.getTemplate().collisionRadius,
									caster,
									leader,
									false) && !isParty(sk) && !caster.isMovementDisabled()) {
								moveToPawn(leader, sk.getCastRange() + caster.getTemplate().collisionRadius + leader.getTemplate().collisionRadius);
							}
						}
						if (GeoData.getInstance().canSeeTarget(caster, leader)) {
							clientStopMoving(null);
							caster.setTarget(leader);
							caster.doCast(sk);
							return true;
						}
					}

					for (Creature obj : caster.getKnownList()
							.getKnownCharactersInRadius(sk.getCastRange() + caster.getTemplate().collisionRadius)) {
						if (!(obj instanceof Attackable) || !obj.isDead()) {
							continue;
						}

						Attackable targets = (Attackable) obj;
						if (caster.getFactionId() != null && !caster.getFactionId().equals(targets.getFactionId())) {
							continue;
						}
						if (Rnd.get(100) < 10) {
							if (GeoData.getInstance().canSeeTarget(caster, targets)) {
								clientStopMoving(null);
								caster.setTarget(obj);
								caster.doCast(sk);
								return true;
							}
						}
					}
				} else if (isParty(sk)) {
					for (Creature obj : caster.getKnownList()
							.getKnownCharactersInRadius(sk.getSkillRadius() + caster.getTemplate().collisionRadius)) {
						if (!(obj instanceof Attackable)) {
							continue;
						}
						Npc targets = (Npc) obj;
						if (caster.getFactionId() != null && caster.getFactionId().equals(targets.getFactionId())) {
							if (obj.getCurrentHp() < obj.getMaxHp() && Rnd.get(100) <= 20) {
								clientStopMoving(null);
								caster.setTarget(caster);
								caster.doCast(sk);
								return true;
							}
						}
					}
				}
				break;
			}
			case DEBUFF: {
				if (GeoData.getInstance().canSeeTarget(caster, attackTarget) && !canAOE(sk) && !attackTarget.isDead() && dist2 <= srange) {
					if (attackTarget.getFirstEffect(sk) == null) {
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				} else if (canAOE(sk)) {
					if (sk.getTargetType() == SkillTargetType.TARGET_AURA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA ||
							sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA) {
						clientStopMoving(null);
						//WorldObject target = attackTarget;
						//actor.setTarget(actor);
						caster.doCast(sk);
						//actor.setTarget(target);
						return true;
					}
					if ((sk.getTargetType() == SkillTargetType.TARGET_AREA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AREA ||
							sk.getTargetType() == SkillTargetType.TARGET_FRONT_AREA) && GeoData.getInstance().canSeeTarget(caster, attackTarget) &&
							!attackTarget.isDead() && dist2 <= srange) {
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				} else if (sk.getTargetType() == SkillTargetType.TARGET_ONE) {
					Creature target = effectTargetReconsider(sk, false);
					if (target != null) {
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			}
			case CANCEL:
			case NEGATE: {
				// decrease cancel probability
				if (Rnd.get(50) != 0) {
					return true;
				}

				if (sk.getTargetType() == SkillTargetType.TARGET_ONE) {
					if (attackTarget.getFirstEffect(AbnormalType.BUFF) != null && GeoData.getInstance().canSeeTarget(caster, attackTarget) &&
							!attackTarget.isDead() && dist2 <= srange) {
						clientStopMoving(null);
						//WorldObject target = attackTarget;
						//actor.setTarget(actor);
						caster.doCast(sk);
						//actor.setTarget(target);
						return true;
					}
					Creature target = effectTargetReconsider(sk, false);
					if (target != null) {
						clientStopMoving(null);
						caster.setTarget(target);
						caster.doCast(sk);
						caster.setTarget(attackTarget);
						return true;
					}
				} else if (canAOE(sk)) {
					if ((sk.getTargetType() == SkillTargetType.TARGET_AURA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA ||
							sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA) && GeoData.getInstance().canSeeTarget(caster, attackTarget))

					{
						clientStopMoving(null);
						//WorldObject target = attackTarget;
						//actor.setTarget(actor);
						caster.doCast(sk);
						//actor.setTarget(target);
						return true;
					} else if ((sk.getTargetType() == SkillTargetType.TARGET_AREA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AREA ||
							sk.getTargetType() == SkillTargetType.TARGET_FRONT_AREA) && GeoData.getInstance().canSeeTarget(caster, attackTarget) &&
							!attackTarget.isDead() && dist2 <= srange) {
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			}
			case PDAM:
			case MDAM:
			case BLOW:
			case DRAIN:
			case CHARGEDAM:
			case FATAL:
			case DEATHLINK:
			case CPDAM:
			case MANADAM:
			case CPDAMPERCENT:
			case MAXHPDAMPERCENT: {
				if (!canAura(sk)) {
					if (GeoData.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && dist2 <= srange) {
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					} else {
						Creature target = skillTargetReconsider(sk);
						if (target != null) {
							clientStopMoving(null);
							caster.setTarget(target);
							caster.doCast(sk);
							caster.setTarget(attackTarget);
							return true;
						}
					}
				} else {
					clientStopMoving(null);
					caster.doCast(sk);
					return true;
				}
				break;
			}
			default: {
				if (!canAura(sk)) {

					if (GeoData.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && dist2 <= srange) {
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					} else {
						Creature target = skillTargetReconsider(sk);
						if (target != null) {
							clientStopMoving(null);
							caster.setTarget(target);
							caster.doCast(sk);
							caster.setTarget(attackTarget);
							return true;
						}
					}
				} else {
					clientStopMoving(null);
					//WorldObject targets = attackTarget;
					//actor.setTarget(actor);
					caster.doCast(sk);
					//actor.setTarget(targets);
					return true;
				}
			}
			break;
		}

		return false;
	}

	/**
	 * This AI task will start when ACTOR cannot move and attack range larger than distance
	 */
	private void movementDisable() {
		Creature attackTarget = getAttackTarget();
		if (attackTarget == null) {
			return;
		}

		final Attackable npc = getActiveChar();
		double dist = 0;
		double dist2 = 0;
		int range = 0;
		try {
			if (npc.getTarget() == null) {
				npc.setTarget(attackTarget);
			}
			dist = Math.sqrt(npc.getPlanDistanceSq(attackTarget.getX(), attackTarget.getY()));
			dist2 = dist - npc.getTemplate().collisionRadius;
			range = npc.getPhysicalAttackRange() + npc.getTemplate().collisionRadius + attackTarget.getTemplate().collisionRadius;
			if (attackTarget.isMoving()) {
				dist = dist - 30;
				if (npc.isMoving()) {
					dist = dist - 50;
				}
			}

			//Check if activeChar has any skill
			if (skillrender.hasSkill()) {
				//-------------------------------------------------------------
				//Try to stop the target or disable the target as priority
				int random = Rnd.get(100);
				if (skillrender.hasImmobiliseSkill() && !attackTarget.isImmobilized() && random < 2) {
					for (Skill sk : skillrender.aiSkills[NpcTemplate.AIST_IMMOBILIZE]) {
						if (sk.getMpConsume() >= npc.getCurrentMp() || npc.isSkillDisabled(sk) ||
								sk.getCastRange() + npc.getTemplate().collisionRadius + attackTarget.getTemplate().collisionRadius <= dist2 &&
										!canAura(sk) || sk.isMagic() && npc.isMuted() || !sk.isMagic() && npc.isPhysicalMuted()) {
							continue;
						}
						if (!GeoData.getInstance().canSeeTarget(npc, attackTarget)) {
							continue;
						}
						if (attackTarget.getFirstEffect(sk) == null) {
							clientStopMoving(null);
							//WorldObject target = attackTarget;
							//actor.setTarget(actor);
							npc.doCast(sk);
							//actor.setTarget(target);
							return;
						}
					}
				}
				//-------------------------------------------------------------
				//Same as Above, but with Mute/FEAR etc....
				if (skillrender.hasCOTSkill() && random < 5) {
					for (Skill sk : skillrender.aiSkills[NpcTemplate.AIST_COT]) {
						if (sk.getMpConsume() >= npc.getCurrentMp() || npc.isSkillDisabled(sk) ||
								sk.getCastRange() + npc.getTemplate().collisionRadius + attackTarget.getTemplate().collisionRadius <= dist2 &&
										!canAura(sk) || sk.isMagic() && npc.isMuted() || !sk.isMagic() && npc.isPhysicalMuted()) {
							continue;
						}
						if (!GeoData.getInstance().canSeeTarget(npc, attackTarget)) {
							continue;
						}
						if (attackTarget.getFirstEffect(sk) == null) {
							clientStopMoving(null);
							//WorldObject target = attackTarget;
							//actor.setTarget(actor);
							npc.doCast(sk);
							//actor.setTarget(target);
							return;
						}
					}
				}
				//-------------------------------------------------------------
				if (skillrender.hasDebuffSkill() && random < 8) {
					for (Skill sk : skillrender.aiSkills[NpcTemplate.AIST_DEBUFF]) {
						if (sk.getMpConsume() >= npc.getCurrentMp() || npc.isSkillDisabled(sk) ||
								sk.getCastRange() + npc.getTemplate().collisionRadius + attackTarget.getTemplate().collisionRadius <= dist2 &&
										!canAura(sk) || sk.isMagic() && npc.isMuted() || !sk.isMagic() && npc.isPhysicalMuted()) {
							continue;
						}
						if (!GeoData.getInstance().canSeeTarget(npc, attackTarget)) {
							continue;
						}
						if (attackTarget.getFirstEffect(sk) == null) {
							clientStopMoving(null);
							//WorldObject target = attackTarget;
							//actor.setTarget(actor);
							npc.doCast(sk);
							//actor.setTarget(target);
							return;
						}
					}
				}
				//-------------------------------------------------------------
				//Some side effect skill like CANCEL or NEGATE
				if (skillrender.hasNegativeSkill() && random < 9) {
					for (Skill sk : skillrender.aiSkills[NpcTemplate.AIST_NEGATIVE]) {
						if (sk.getMpConsume() >= npc.getCurrentMp() || npc.isSkillDisabled(sk) ||
								sk.getCastRange() + npc.getTemplate().collisionRadius + attackTarget.getTemplate().collisionRadius <= dist2 &&
										!canAura(sk) || sk.isMagic() && npc.isMuted() || !sk.isMagic() && npc.isPhysicalMuted()) {
							continue;
						}
						if (!GeoData.getInstance().canSeeTarget(npc, attackTarget)) {
							continue;
						}
						if (attackTarget.getFirstEffect(AbnormalType.BUFF) != null) {
							clientStopMoving(null);
							//WorldObject target = attackTarget;
							//actor.setTarget(actor);
							npc.doCast(sk);
							//actor.setTarget(target);
							return;
						}
					}
				}
				//-------------------------------------------------------------
				//Start ATK SKILL when nothing can be done
				if (skillrender.hasAtkSkill() && (npc.isMovementDisabled() || npc.getAiType() == AIType.MAGE || npc.getAiType() == AIType.HEALER)) {
					for (Skill sk : skillrender.aiSkills[NpcTemplate.AIST_ATK]) {
						if (sk.getMpConsume() >= npc.getCurrentMp() || npc.isSkillDisabled(sk) ||
								sk.getCastRange() + npc.getTemplate().collisionRadius + attackTarget.getTemplate().collisionRadius <= dist2 &&
										!canAura(sk) || sk.isMagic() && npc.isMuted() || !sk.isMagic() && npc.isPhysicalMuted()) {
							continue;
						}
						if (!GeoData.getInstance().canSeeTarget(npc, attackTarget)) {
							continue;
						}
						clientStopMoving(null);
						//WorldObject target = attackTarget;
						//actor.setTarget(actor);
						npc.doCast(sk);
						//actor.setTarget(target);
						return;
					}
				}
				//-------------------------------------------------------------
				//if there is no ATK skill to use, then try Universal skill
                /*
				if (skillrender.hasUniversalSkill())
				{
					for (Skill sk:skillrender.universalskills)
					{
						if (sk.getMpConsume()>=actor.getCurrentMp()
								|| actor.isSkillDisabled(sk.getId())
								||(sk.getCastRange()+ actor.getTemplate().collisionRadius + attackTarget.getTemplate().collisionRadius <= dist2 && !canAura(sk))
								||(sk.isMagic()&&actor.isMuted())
								||(!sk.isMagic()&&actor.isPhysicalMuted()))
						{
							continue;
						}
						if (!GeoData.getInstance().canSeeTarget(actor,attackTarget))
							continue;
						clientStopMoving(null);
						WorldObject target = attackTarget;
						//actor.setTarget(actor);
						actor.doCast(sk);
						//actor.setTarget(target);
						return;
					}
				}

				 */
			}
			//timepass = timepass + 1;
			if (npc.isMovementDisabled()) {
				//timepass = 0;
				targetReconsider();

				return;
			}
			//else if (timepass>=5)
			//{
			//	timepass = 0;
			//	AggroReconsider();
			//	return;
			//}

			if (dist > range || !GeoData.getInstance().canSeeTarget(npc, attackTarget)) {
				if (attackTarget.isMoving()) {
					range -= 100;
				}
				if (range < 5) {
					range = 5;
				}
				moveToPawn(attackTarget, range);
				return;
			}

			melee(npc.getPrimaryAttack());
		} catch (NullPointerException e) {
			setIntention(AI_INTENTION_ACTIVE);
			log.warn(this + " - failed executing movementDisable(): " + e.getMessage(), e);
		}
	}

	private Creature effectTargetReconsider(Skill sk, boolean positive) {
		if (sk == null) {
			return null;
		}
		Attackable actor = getActiveChar();
		if (sk.getSkillType() != SkillType.NEGATE || sk.getSkillType() != SkillType.CANCEL) {
			if (!positive) {
				double dist = 0;
				double dist2 = 0;
				int range = 0;

				for (Creature obj : actor.getAttackByList()) {
					if (obj == null || obj.isDead() || !GeoData.getInstance().canSeeTarget(actor, obj) || obj == getAttackTarget()) {
						continue;
					}
					try {
						actor.setTarget(getAttackTarget());
						dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
						dist2 = dist - actor.getTemplate().collisionRadius;
						range = sk.getCastRange() + actor.getTemplate().collisionRadius + obj.getTemplate().collisionRadius;
						if (obj.isMoving()) {
							dist2 = dist2 - 70;
						}
					} catch (NullPointerException e) {
						continue;
					}
					if (dist2 <= range) {
						if (getAttackTarget().getFirstEffect(sk) == null) {
							return obj;
						}
					}
				}

				//----------------------------------------------------------------------
				//If there is nearby Target with aggro, start going on random target that is attackable
				for (Creature obj : actor.getKnownList().getKnownCharactersInRadius(range)) {
					if (obj.isDead() || !GeoData.getInstance().canSeeTarget(actor, obj)) {
						continue;
					}
					try {
						actor.setTarget(getAttackTarget());
						dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
						dist2 = dist;
						range = sk.getCastRange() + actor.getTemplate().collisionRadius + obj.getTemplate().collisionRadius;
						if (obj.isMoving()) {
							dist2 = dist2 - 70;
						}
					} catch (NullPointerException e) {
						continue;
					}
					if (obj instanceof Attackable) {
						if (actor.getEnemyClan() != null && actor.getEnemyClan().equals(((Attackable) obj).getClan())) {
							if (dist2 <= range) {
								if (getAttackTarget().getFirstEffect(sk) == null) {
									return obj;
								}
							}
						}
					}
					if (obj instanceof Player || obj instanceof Summon) {
						if (dist2 <= range) {
							if (getAttackTarget().getFirstEffect(sk) == null) {
								return obj;
							}
						}
					}
				}
			} else if (positive) {
				double dist = 0;
				double dist2 = 0;
				int range = 0;
				for (Creature obj : actor.getKnownList().getKnownCharactersInRadius(range)) {
					if (!(obj instanceof Attackable) || obj.isDead() || !GeoData.getInstance().canSeeTarget(actor, obj)) {
						continue;
					}

					Attackable targets = (Attackable) obj;
					if (actor.getFactionId() != null && !actor.getFactionId().equals(targets.getFactionId())) {
						continue;
					}

					try {
						actor.setTarget(getAttackTarget());
						dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
						dist2 = dist - actor.getTemplate().collisionRadius;
						range = sk.getCastRange() + actor.getTemplate().collisionRadius + obj.getTemplate().collisionRadius;
						if (obj.isMoving()) {
							dist2 = dist2 - 70;
						}
					} catch (NullPointerException e) {
						continue;
					}
					if (dist2 <= range) {
						if (obj.getFirstEffect(sk) == null) {
							return obj;
						}
					}
				}
			}
			return null;
		} else {
			double dist = 0;
			double dist2 = 0;
			int range = 0;
			range = sk.getCastRange() + actor.getTemplate().collisionRadius + getAttackTarget().getTemplate().collisionRadius;
			for (Creature obj : actor.getKnownList().getKnownCharactersInRadius(range)) {
				if (obj == null || obj.isDead() || !GeoData.getInstance().canSeeTarget(actor, obj)) {
					continue;
				}
				try {
					actor.setTarget(getAttackTarget());
					dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
					dist2 = dist - actor.getTemplate().collisionRadius;
					range = sk.getCastRange() + actor.getTemplate().collisionRadius + obj.getTemplate().collisionRadius;
					if (obj.isMoving()) {
						dist2 = dist2 - 70;
					}
				} catch (NullPointerException e) {
					continue;
				}
				if (obj instanceof Attackable) {
					if (actor.getEnemyClan() != null && actor.getEnemyClan().equals(((Attackable) obj).getClan())) {
						if (dist2 <= range) {
							if (getAttackTarget().getFirstEffect(AbnormalType.BUFF) != null) {
								return obj;
							}
						}
					}
				}
				if (obj instanceof Player || obj instanceof Summon) {

					if (dist2 <= range) {
						if (getAttackTarget().getFirstEffect(AbnormalType.BUFF) != null) {
							return obj;
						}
					}
				}
			}
			return null;
		}
	}

	private Creature skillTargetReconsider(Skill sk) {
		double dist = 0;
		double dist2 = 0;
		int range = 0;
		Attackable actor = getActiveChar();
		List<Creature> hateList = actor.getHateList();
		if (hateList != null) {
			for (Creature obj : hateList) {
				if (obj == null || !GeoData.getInstance().canSeeTarget(actor, obj) || obj.isDead()) {
					continue;
				}
				try {
					actor.setTarget(getAttackTarget());
					dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
					dist2 = dist - actor.getTemplate().collisionRadius;
					range = sk.getCastRange() + actor.getTemplate().collisionRadius + getAttackTarget().getTemplate().collisionRadius;
					//if (obj.isMoving())
					//	dist2 = dist2 - 40;
				} catch (NullPointerException e) {
					continue;
				}
				if (dist2 <= range) {
					return obj;
				}
			}
		}

		if (!(actor instanceof GuardInstance)) {
			Collection<WorldObject> objs = actor.getKnownList().getKnownObjects().values();
			for (WorldObject target : objs) {
				try {
					actor.setTarget(getAttackTarget());
					dist = Math.sqrt(actor.getPlanDistanceSq(target.getX(), target.getY()));
					dist2 = dist;
					range = sk.getCastRange() + actor.getTemplate().collisionRadius + getAttackTarget().getTemplate().collisionRadius;
					//if (obj.isMoving())
					//	dist2 = dist2 - 40;
				} catch (NullPointerException e) {
					continue;
				}
				Creature obj = null;
				if (target instanceof Creature) {
					obj = (Creature) target;
				}
				if (obj == null || !GeoData.getInstance().canSeeTarget(actor, obj) || dist2 > range) {
					continue;
				}
				if (obj instanceof Player) {
					return obj;
				}
				if (obj instanceof Attackable) {
					if (actor.getEnemyClan() != null && actor.getEnemyClan().equals(((Attackable) obj).getClan())) {
						return obj;
					}
					if (actor.getIsChaos() != 0) {
						if (((Attackable) obj).getFactionId() != null && ((Attackable) obj).getFactionId().equals(actor.getFactionId())) {
							continue;
						} else {
							return obj;
						}
					}
				}
				if (obj instanceof Summon) {
					return obj;
				}
			}
		}
		return null;
	}

	private void targetReconsider() {
		double dist = 0;
		double dist2 = 0;
		int range = 0;
		Attackable actor = getActiveChar();
		Creature mostHate = actor.getMostHated();
		List<Creature> hateList = actor.getHateList();
		if (hateList != null) {
			for (Creature obj : hateList) {
				if (obj == null || !GeoData.getInstance().canSeeTarget(actor, obj) || obj.isDead() || obj != mostHate || obj == actor) {
					continue;
				}
				try {
					dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
					dist2 = dist - actor.getTemplate().collisionRadius;
					range = actor.getPhysicalAttackRange() + actor.getTemplate().collisionRadius + obj.getTemplate().collisionRadius;
					if (obj.isMoving()) {
						dist2 = dist2 - 70;
					}
				} catch (NullPointerException e) {
					continue;
				}

				if (dist2 <= range) {
					if (mostHate != null) {
						actor.addDamageHate(obj, 0, actor.getHating(mostHate));
					} else {
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
					return;
				}
			}
		}
		if (!(actor instanceof GuardInstance)) {
			Collection<WorldObject> objs = actor.getKnownList().getKnownObjects().values();
			for (WorldObject target : objs) {
				Creature obj = null;
				if (target instanceof Creature) {
					obj = (Creature) target;
				}

				if (obj == null || !GeoData.getInstance().canSeeTarget(actor, obj) || obj.isDead() || obj != mostHate || obj == actor ||
						obj == getAttackTarget()) {
					continue;
				}
				if (obj instanceof Player) {
					if (mostHate != null) {
						actor.addDamageHate(obj, 0, actor.getHating(mostHate));
					} else {
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
				} else if (obj instanceof Attackable) {
					if (actor.getEnemyClan() != null && actor.getEnemyClan().equals(((Attackable) obj).getClan())) {
						actor.addDamageHate(obj, 0, actor.getHating(mostHate));
						actor.setTarget(obj);
					}
					if (actor.getIsChaos() != 0) {
						if (((Attackable) obj).getFactionId() != null && ((Attackable) obj).getFactionId().equals(actor.getFactionId())) {
						} else {
							if (mostHate != null) {
								actor.addDamageHate(obj, 0, actor.getHating(mostHate));
							} else {
								actor.addDamageHate(obj, 0, 2000);
							}
							actor.setTarget(obj);
							setAttackTarget(obj);
						}
					}
				} else if (obj instanceof Summon) {
					if (mostHate != null) {
						actor.addDamageHate(obj, 0, actor.getHating(mostHate));
					} else {
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
				}
			}
		}
	}

	@SuppressWarnings("null")
	private void aggroReconsider() {
		Attackable actor = getActiveChar();
		Creature MostHate = actor.getMostHated();

		List<Creature> hateList = actor.getHateList();
		if (hateList != null && !hateList.isEmpty()) {
			int rand = Rnd.get(hateList.size());
			int count = 0;
			for (Creature obj : hateList) {
				if (count < rand) {
					count++;
					continue;
				}

				if (obj == null || !GeoData.getInstance().canSeeTarget(actor, obj) || obj.isDead() || obj == getAttackTarget() || obj == actor) {
					continue;
				}

				try {
					actor.setTarget(getAttackTarget());
				} catch (NullPointerException e) {
					continue;
				}
				if (MostHate != null) {
					actor.addDamageHate(obj, 0, actor.getHating(MostHate));
				} else {
					actor.addDamageHate(obj, 0, 2000);
				}
				actor.setTarget(obj);
				setAttackTarget(obj);
				return;
			}
		}

		if (!(actor instanceof GuardInstance)) {
			Collection<WorldObject> objs = actor.getKnownList().getKnownObjects().values();
			for (WorldObject target : objs) {
				Creature obj = null;
				if (target instanceof Creature) {
					obj = (Creature) target;
				} else {
					continue;
				}
				if (obj == null || !GeoData.getInstance().canSeeTarget(actor, obj) || obj.isDead() || obj != MostHate || obj == actor) {
					continue;
				}
				if (obj instanceof Player) {
					if (MostHate != null || !MostHate.isDead()) {
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
					} else {
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
				} else if (obj instanceof Attackable) {
					if (actor.getEnemyClan() != null && actor.getEnemyClan().equals(((Attackable) obj).getClan())) {
						if (MostHate != null) {
							actor.addDamageHate(obj, 0, actor.getHating(MostHate));
						} else {
							actor.addDamageHate(obj, 0, 2000);
						}
						actor.setTarget(obj);
					}
					if (actor.getIsChaos() != 0) {
						if (((Attackable) obj).getFactionId() != null && ((Attackable) obj).getFactionId().equals(actor.getFactionId())) {
						} else {
							if (MostHate != null) {
								actor.addDamageHate(obj, 0, actor.getHating(MostHate));
							} else {
								actor.addDamageHate(obj, 0, 2000);
							}
							actor.setTarget(obj);
							setAttackTarget(obj);
						}
					}
				} else if (obj instanceof Summon) {
					if (MostHate != null) {
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
					} else {
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
				}
			}
		} else {
			Collection<WorldObject> objs = actor.getKnownList().getKnownObjects().values();
			for (WorldObject target : objs) {
				Creature obj = null;
				if (target instanceof Creature) {
					obj = (Creature) target;
				} else {
					continue;
				}
				if (obj == null || !GeoData.getInstance().canSeeTarget(actor, obj) || obj.isDead() || obj != MostHate || obj == actor) {
					continue;
				}
				if (obj instanceof Npc && actor.getEnemyClan() != null && actor.getEnemyClan().equals(((Npc) obj).getClan())) {
					if (MostHate != null) {
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
					} else {
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
				}
			}
		}
	}

	private void lSkillRender() {
		if (skillrender.aiSkills[NpcTemplate.AIST_LONG_RANGE] == null) {
			skillrender.aiSkills[NpcTemplate.AIST_LONG_RANGE] = getActiveChar().getLrangeSkill();
		}
	}

	private void sSkillRender() {
		if (skillrender.aiSkills[NpcTemplate.AIST_SHORT_RANGE] == null) {
			skillrender.aiSkills[NpcTemplate.AIST_SHORT_RANGE] = getActiveChar().getSrangeSkill();
		}
	}

	/**
	 * Manage AI thinking actions of a Attackable.<BR><BR>
	 */
	@Override
	protected void onEvtThink() {
		// Check if the actor can't use skills and if a thinking action isn't already in progress
		if (thinking || getActiveChar().isAllSkillsDisabled()) {
			return;
		}

		// Start thinking action
		thinking = true;

		try {
			// Manage AI thinks of a Attackable
			switch (getIntention()) {
				case AI_INTENTION_ACTIVE:
					thinkActive();
					break;
				case AI_INTENTION_ATTACK:
					thinkAttack();
					break;
				case AI_INTENTION_CAST:
					thinkCast();
					break;
			}
		} catch (Exception e) {
			log.warn(this + " -  onEvtThink() failed: " + e.getMessage(), e);
		} finally {
			// Stop thinking action
			thinking = false;
		}
	}

	/**
	 * Launch actions corresponding to the Event Attacked.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Init the attack : Calculate the attack timeout, Set the globalAggro to 0, Add the attacker to the actor aggroList</li>
	 * <li>Set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others Player</li>
	 * <li>Set the Intention to AI_INTENTION_ATTACK</li><BR><BR>
	 *
	 * @param attacker The Creature that attacks the actor
	 */
	@Override
	protected void onEvtAttacked(Creature attacker) {
		if (attacker instanceof Player) {
			Player player = (Player) attacker;
			if (player.isGM() && !player.getAccessLevel().canGiveDamage()) {
				return;
			}

			if (actor instanceof EventGolemInstance ||
					actor instanceof GuardInstance && ((GuardInstance) actor).getNpcId() == 40009 && attacker instanceof Player &&
							((Player) attacker).getPvpFlag() > 0 && ((Player) attacker).getReputation() >= 0) {
				return;
			}
		}

		Attackable me = getActiveChar();

		// Calculate the attack timeout
		attackTimeout = MAX_ATTACK_TIMEOUT + TimeController.getGameTicks();

		// Set the globalAggro to 0 to permit attack even just after spawn
		if (globalAggro < 0) {
			globalAggro = 0;
		}

		// Add the attacker to the aggroList of the actor
		me.addDamageHate(attacker, 0, 1);

		// Set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others Player
		if (!me.isRunning()) {
			me.setRunning();
		}

		// Set the Intention to AI_INTENTION_ATTACK
		if (getIntention() != AI_INTENTION_ATTACK) {
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		} else if (me.getMostHated() != getAttackTarget()) {
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}

		if (me instanceof MonsterInstance) {
			MonsterInstance master = (MonsterInstance) me;

			if (master.hasMinions()) {
				master.getMinionList().onAssist(me, attacker);
			}

			master = master.getLeader();
			if (master != null && master.hasMinions()) {
				master.getMinionList().onAssist(me, attacker);
			}
		}

		super.onEvtAttacked(attacker);
	}

	/**
	 * Launch actions corresponding to the Event Aggression.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Add the target to the actor aggroList or update hate if already present </li>
	 * <li>Set the actor Intention to AI_INTENTION_ATTACK (if actor is GuardInstance check if it isn't too far from its home location)</li><BR><BR>
	 *
	 * @param aggro The value of hate to add to the actor against the target
	 */
	@Override
	protected void onEvtAggression(Creature target, int aggro) {
		Attackable me = getActiveChar();

		if (target != null) {
			// CUSTOM: clear the aggro so that the aggression is always effective
			if (Config.isServer(Config.TENKAI) && aggro > 1) {
				for (AggroInfo aggroInfo : me.getAggroList().values()) {
					aggroInfo.stopHate();
				}
			}

			// Add the target to the actor aggroList or update hate if already present
			me.addDamageHate(target, 0, aggro);

			// Set the actor AI Intention to AI_INTENTION_ATTACK
			if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK) {
				// Set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others Player
				if (!me.isRunning()) {
					me.setRunning();
				}

				setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}

			if (me instanceof MonsterInstance) {
				MonsterInstance master = (MonsterInstance) me;

				if (master.hasMinions()) {
					master.getMinionList().onAssist(me, target);
				}

				master = master.getLeader();
				if (master != null && master.hasMinions()) {
					master.getMinionList().onAssist(me, target);
				}
			}
		}
	}

	@Override
	protected void onIntentionActive() {
		// Cancel attack timeout
		attackTimeout = Integer.MAX_VALUE;
		super.onIntentionActive();
	}

	public void setGlobalAggro(int value) {
		globalAggro = value;
	}

	public void setTimepass(int TP) {
		timepass = TP;
	}

	/**
	 * @return Returns the timepass.
	 */
	public int getTimepass() {
		return timepass;
	}

	public Attackable getActiveChar() {
		return (Attackable) actor;
	}
}
