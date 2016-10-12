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
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.*;
import l2server.gameserver.model.actor.L2Attackable.AggroInfo;
import l2server.gameserver.model.actor.instance.*;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.chars.L2NpcTemplate.AIType;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectType;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;

import static l2server.gameserver.ai.CtrlIntention.*;

/**
 * This class manages AI of L2Attackable.<BR><BR>
 */
public class L2AttackableAI extends L2CharacterAI implements Runnable
{
	//
	private static final int RANDOM_WALK_RATE = 30; // confirmed
	// private static final int MAX_DRIFT_RANGE = 300;
	private static final int MAX_ATTACK_TIMEOUT = 1200; // int ticks, i.e. 2min

	/**
	 * The L2Attackable AI task executed every 1s (call onEvtThink method)
	 */
	private Future<?> _aiTask;

	/**
	 * The delay after which the attacked is stopped
	 */
	private int _attackTimeout;

	/**
	 * The L2Attackable aggro counter
	 */
	private int _globalAggro;

	/**
	 * The flag used to indicate that a thinking action is in progress
	 */
	private boolean _thinking; // to prevent recursive thinking

	private int timepass = 0;
	private int chaostime = 0;
	private L2NpcTemplate _skillrender;
	int lastBuffTick;

	/**
	 * Constructor of L2AttackableAI.<BR><BR>
	 *
	 * @param accessor The AI accessor of the L2Character
	 */
	public L2AttackableAI(L2Character.AIAccessor accessor)
	{
		super(accessor);
		if (getActiveChar().getClonedPlayer() == null)
		{
			_skillrender = NpcTable.getInstance().getTemplate(getActiveChar().getTemplate().NpcId);
		}
		else
		{
			_skillrender = getActiveChar().getTemplate();
		}
		//_selfAnalysis.doSpawn();
		_attackTimeout = Integer.MAX_VALUE;
		_globalAggro = -10; // 10 seconds timeout of ATTACK after respawn
	}

	@Override
	public void run()
	{
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}

	/**
	 * Return True if the target is autoattackable (depends on the actor type).<BR><BR>
	 * <p>
	 * <B><U> Actor is a L2GuardInstance</U> :</B><BR><BR>
	 * <li>The target isn't a Folk or a Door</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The L2PcInstance target has karma (=PK)</li>
	 * <li>The L2MonsterInstance target is aggressive</li><BR><BR>
	 * <p>
	 * <B><U> Actor is a L2SiegeGuardInstance</U> :</B><BR><BR>
	 * <li>The target isn't a Folk or a Door</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>A siege is in progress</li>
	 * <li>The L2PcInstance target isn't a Defender</li><BR><BR>
	 * <p>
	 * <B><U> Actor is a L2FriendlyMobInstance</U> :</B><BR><BR>
	 * <li>The target isn't a Folk, a Door or another L2Npc</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The L2PcInstance target has karma (=PK)</li><BR><BR>
	 * <p>
	 * <B><U> Actor is a L2MonsterInstance</U> :</B><BR><BR>
	 * <li>The target isn't a Folk, a Door or another L2Npc</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The actor is Aggressive</li><BR><BR>
	 *
	 * @param target The targeted L2Object
	 */
	private boolean autoAttackCondition(L2Character target)
	{
		if (target == null || getActiveChar() == null)
		{
			return false;
		}

		L2Attackable me = getActiveChar();

		// Check if the target isn't invulnerable
		if (target.isInvul(me))
		{
			// However EffectInvincible requires to check GMs specially
			if (target instanceof L2PcInstance && target.isGM())
			{
				return false;
			}
			if (target instanceof L2Summon && ((L2Summon) target).getOwner().isGM())
			{
				return false;
			}
		}

		// Check if the target isn't a Folk or a Door
		if (target instanceof L2DoorInstance)
		{
			return false;
		}

		// Check if the target isn't dead, is in the Aggro range and is at the same height
		if (target.isAlikeDead() ||
				target instanceof L2Playable && !me.isInsideRadius(target, me.getAggroRange(), true, false))
		{
			return false;
		}

		// Check if the target is a L2PlayableInstance
		if (target instanceof L2Playable)
		{
			// Check if the AI isn't a Raid Boss, can See Silent Moving players and the target isn't in silent move mode
			if (!me.isRaid() && !me.canSeeThroughSilentMove() && ((L2Playable) target).isSilentMoving())
			{
				return false;
			}
		}

		// Check if the target is a L2PcInstance
		if (target instanceof L2PcInstance)
		{
			// Don't take the aggro if the GM has the access level below or equal to GM_DONT_TAKE_AGGRO
			if (target.isGM() && !((L2PcInstance) target).getAccessLevel().canTakeAggro())
			{
				return false;
			}

			// TODO: Ideally, autoattack condition should be called from the AI script.  In that case,
			// it should only implement the basic behaviors while the script will add more specific
			// behaviors (like varka/ketra alliance, etc).  Once implemented, remove specialized stuff
			// from this location.  (Fulminus)

			// Check if player is an ally (comparing mem addr)
			if ("varka_silenos_clan".equals(me.getFactionId()) && ((L2PcInstance) target).isAlliedWithVarka())
			{
				return false;
			}
			if ("ketra_orc_clan".equals(me.getFactionId()) && ((L2PcInstance) target).isAlliedWithKetra())
			{
				return false;
			}
			// check if the target is within the grace period for JUST getting up from fake death
			if (((L2PcInstance) target).isRecentFakeDeath())
			{
				return false;
			}

			//if (_selfAnalysis.cannotMoveOnLand && !target.isInsideZone(L2Character.ZONE_WATER))
			//	return false;

			if (((L2PcInstance) target).isPlayingEvent())
			{
				return false;
			}

			if (me.getClonedPlayer() != null &&
					(target.getLevel() < me.getLevel() || target.getLevel() > me.getLevel() + 5))
			{
				return false;
			}
		}

		// Check if the target is a L2Summon
		if (target instanceof L2Summon)
		{
			L2PcInstance owner = ((L2Summon) target).getOwner();
			if (owner != null)
			{
				// Don't take the aggro if the GM has the access level below or equal to GM_DONT_TAKE_AGGRO
				if (owner.isGM() && (owner.isInvul(me) || !owner.getAccessLevel().canTakeAggro()))
				{
					return false;
				}
				// Check if player is an ally (comparing mem addr)
				if ("varka_silenos_clan".equals(me.getFactionId()) && owner.isAlliedWithVarka())
				{
					return false;
				}
				if ("ketra_orc_clan".equals(me.getFactionId()) && owner.isAlliedWithKetra())
				{
					return false;
				}
			}
		}
		// Check if the actor is a L2GuardInstance
		if (getActiveChar() instanceof L2GuardInstance)
		{
			// Check if the L2PcInstance target has karma (=PK)
			if (target instanceof L2PcInstance && ((L2PcInstance) target).getReputation() < 0)
			// Los Check
			{
				return GeoData.getInstance().canSeeTarget(me, target);
			}

			//if (target instanceof L2Summon)
			//	return ((L2Summon)target).getKarma() > 0;

			// Check if the L2MonsterInstance target is aggressive
			if (target instanceof L2MonsterInstance && Config.GUARD_ATTACK_AGGRO_MOB &&
					target.getAI().getAttackTarget() != null)
			{
				return GeoData.getInstance().canSeeTarget(me, target);
			}

			if (!(target instanceof L2Npc) || getActiveChar().getEnemyClan() == null ||
					((L2Npc) target).getClan() == null)
			{
				return false;
			}

			if (getActiveChar().getEnemyClan().equals(((L2Npc) target).getClan()))
			{
				if (getActiveChar().isInsideRadius(target, getActiveChar().getEnemyRange(), false, false))
				{
					return GeoData.getInstance().canSeeTarget(getActiveChar(), target);
				}
				else
				{
					return false;
				}
			}

			return false;
		}
		else if (getActiveChar() instanceof L2FriendlyMobInstance)
		{ // the actor is a L2FriendlyMobInstance

			// Check if the target isn't another L2Npc
			if (target instanceof L2Npc)
			{
				return false;
			}

			// Check if the L2PcInstance target has karma (=PK)
			if (target instanceof L2PcInstance && ((L2PcInstance) target).getReputation() < 0)
			{
				return GeoData.getInstance().canSeeTarget(me, target); // Los Check
			}
			else
			{
				return false;
			}
		}
		else
		{
			if (target instanceof L2Attackable)
			{
				if (getActiveChar().getEnemyClan() == null || ((L2Attackable) target).getClan() == null)
				{
					return false;
				}

				if (!target.isAutoAttackable(getActiveChar()))
				{
					return false;
				}

				if (getActiveChar().getEnemyClan().equals(((L2Attackable) target).getClan()))
				{
					if (getActiveChar().isInsideRadius(target, getActiveChar().getEnemyRange(), false, false))
					{
						return GeoData.getInstance().canSeeTarget(getActiveChar(), target);
					}
					else
					{
						return false;
					}
				}
				if (getActiveChar().getIsChaos() > 0 &&
						me.isInsideRadius(target, getActiveChar().getIsChaos(), false, false))
				{
					if (getActiveChar().getFactionId() != null &&
							getActiveChar().getFactionId().equals(((L2Attackable) target).getFactionId()))
					{
						return false;
					}
					// Los Check
					return GeoData.getInstance().canSeeTarget(me, target);
				}
			}

			if (target instanceof L2Attackable || target instanceof L2Npc)
			{
				return false;
			}

			// depending on config, do not allow mobs to attack _new_ players in peacezones,
			// unless they are already following those players from outside the peacezone.
			if (!Config.ALT_MOB_AGRO_IN_PEACEZONE && target.isInsideZone(L2Character.ZONE_PEACE))
			{
				return false;
			}

			if (me.isChampion() && Config.L2JMOD_CHAMPION_PASSIVE)
			{
				return false;
			}

			// Check if the actor is Aggressive
			return me.isAggressive() && GeoData.getInstance().canSeeTarget(me, target);
		}
	}

	public void startAITask()
	{
		// If not idle - create an AI task (schedule onEvtThink repeatedly)
		if (_aiTask == null)
		{
			_aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 1000);
		}
	}

	@Override
	public void stopAITask()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(false);
			_aiTask = null;
		}
		super.stopAITask();
	}

	/**
	 * Set the Intention of this L2CharacterAI and create an  AI Task executed every 1s (call onEvtThink method) for this L2Attackable.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If actor _knowPlayer isn't EMPTY, AI_INTENTION_IDLE will be change in AI_INTENTION_ACTIVE</B></FONT><BR><BR>
	 *
	 * @param intention The new Intention to set to the AI
	 * @param arg0      The first parameter of the Intention
	 * @param arg1      The second parameter of the Intention
	 */
	@Override
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if (intention == AI_INTENTION_IDLE || intention == AI_INTENTION_ACTIVE)
		{
			// Check if actor is not dead
			L2Attackable npc = getActiveChar();
			if (!npc.isAlikeDead())
			{
				// If its _knownPlayer isn't empty set the Intention to AI_INTENTION_ACTIVE
				if (!npc.getKnownList().getKnownPlayers().isEmpty())
				{
					intention = AI_INTENTION_ACTIVE;
				}
				else
				{
					if (npc.getSpawn() != null)
					{
						final int range = Config.MAX_DRIFT_RANGE;
						if (!npc.isInsideRadius(npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ(),
								range + range, true, false))
						{
							intention = AI_INTENTION_ACTIVE;
						}
					}
				}
			}

			if (intention == AI_INTENTION_IDLE)
			{
				// Set the Intention of this L2AttackableAI to AI_INTENTION_IDLE
				super.changeIntention(AI_INTENTION_IDLE, null, null);

				// Stop AI task and detach AI from NPC
				if (_aiTask != null)
				{
					_aiTask.cancel(true);
					_aiTask = null;
				}

				// Cancel the AI
				_accessor.detachAI();

				return;
			}
		}

		// Set the Intention of this L2AttackableAI to intention
		super.changeIntention(intention, arg0, arg1);

		// If not idle - create an AI task (schedule onEvtThink repeatedly)
		startAITask();
	}

	/**
	 * Manage the Attack Intention : Stop current Attack (if necessary), Calculate attack timeout, Start a new Attack and Launch Think Event.<BR><BR>
	 *
	 * @param target The L2Character to attack
	 */
	@Override
	protected void onIntentionAttack(L2Character target)
	{
		// Calculate the attack timeout
		_attackTimeout = MAX_ATTACK_TIMEOUT + TimeController.getGameTicks();

		// self and buffs

		if (lastBuffTick + 30 < TimeController.getGameTicks())
		{
			if (_skillrender.hasBuffSkill())
			{
				for (L2Skill sk : _skillrender.aiSkills[L2NpcTemplate.AIST_BUFF])
				{
					if (cast(sk))
					{
						break;
					}
				}
			}

			lastBuffTick = TimeController.getGameTicks();
		}

		// Manage the Attack Intention : Stop current Attack (if necessary), Start a new Attack and Launch Think Event
		super.onIntentionAttack(target);
	}

	private void thinkCast()
	{
		if (checkTargetLost(getCastTarget()))
		{
			setCastTarget(null);
			return;
		}
		if (maybeMoveToPawn(getCastTarget(), _actor.getMagicalAttackRange(_skill)))
		{
			return;
		}
		clientStopMoving(null);
		setIntention(AI_INTENTION_ACTIVE);
		_accessor.doCast(_skill, false);
	}

	/**
	 * Manage AI standard thinks of a L2Attackable (called by onEvtThink).<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Update every 1s the _globalAggro counter to come close to 0</li>
	 * <li>If the actor is Aggressive and can attack, add all autoAttackable L2Character in its Aggro Range to its _aggroList, chose a target and order to attack it</li>
	 * <li>If the actor is a L2GuardInstance that can't attack, order to it to return to its home location</li>
	 * <li>If the actor is a L2MonsterInstance that can't attack, order to it to random walk (1/100)</li><BR><BR>
	 */
	private void thinkActive()
	{
		L2Attackable npc = getActiveChar();

		// Update every 1s the _globalAggro counter to come close to 0
		if (_globalAggro != 0)
		{
			if (_globalAggro < 0)
			{
				_globalAggro++;
			}
			else
			{
				_globalAggro--;
			}
		}

		// Add all autoAttackable L2Character in L2Attackable Aggro Range to its _aggroList with 0 damage and 1 hate
		// A L2Attackable isn't aggressive during 10s after its spawn because _globalAggro is set to -10
		if (_globalAggro >= 0)
		{
			// Get all visible objects inside its Aggro Range
			Collection<L2Object> objs = npc.getKnownList().getKnownObjects().values();
			//synchronized (npc.getKnownList().getKnownObjects())
			{
				for (L2Object obj : objs)
				{
					if (!(obj instanceof L2Character))
					{
						continue;
					}
					L2Character target = (L2Character) obj;

					// TODO: The AI Script ought to handle aggro behaviors in onSee.  Once implemented, aggro behaviors ought
					// to be removed from here.  (Fulminus)
					// For each L2Character check if the target is autoattackable
					if (autoAttackCondition(target)) // check aggression
					{
						// Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
						int hating = npc.getHating(target);

						// Add the attacker to the L2Attackable _aggroList with 0 damage and 0 hate
						if (hating == 0)
						{
							npc.addDamageHate(target, 0, 0);
						}
					}
				}
			}

			// Chose a target from its aggroList
			L2Character hated;
			if (npc.isConfused())
			{
				hated = getAttackTarget(); // effect handles selection
			}
			else
			{
				hated = npc.getMostHated();
			}

			// Order to the L2Attackable to attack the target
			if (hated != null && !npc.isCoreAIDisabled())
			{
				// Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
				int aggro = npc.getHating(hated);

				if (aggro + _globalAggro > 0)
				{
					// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
					if (!npc.isRunning())
					{
						npc.setRunning();
					}

					// Set the AI Intention to AI_INTENTION_ATTACK
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated);
				}

				return;
			}
		}

		// Chance to forget attackers after some time
		if (npc.getCurrentHp() == npc.getMaxHp() && npc.getCurrentMp() == npc.getMaxMp() &&
				!npc.getAttackByList().isEmpty() && Rnd.nextInt(500) == 0)
		{
			npc.clearAggroList();
			npc.getAttackByList().clear();
			if (npc instanceof L2MonsterInstance)
			{
				if (((L2MonsterInstance) npc).hasMinions())
				{
					((L2MonsterInstance) npc).getMinionList().deleteReusedMinions();
				}
			}
		}

		// Check if the mob should not return to spawn point
		if (!npc.canReturnToSpawnPoint())
		{
			return;
		}

		// Check if the actor is a L2GuardInstance
		if (npc instanceof L2GuardInstance)
		{
			// Order to the L2GuardInstance to return to its home location because there's no target to attack
			npc.returnHome();
		}

		// Minions following leader
		final L2Character leader = npc.getLeader();
		if (leader != null && !leader.isAlikeDead())
		{
			final int offset;
			final int minRadius = 30;

			if (npc.isRaidMinion())
			{
				offset = 500; // for Raids - need correction
			}
			else
			{
				offset = 200; // for normal minions - need correction :)
			}

			if (leader.isRunning())
			{
				npc.setRunning();
			}
			else
			{
				npc.setWalking();
			}

			if (npc.getPlanDistanceSq(leader) > offset * offset)
			{
				int x1, y1, z1;
				x1 = Rnd.get(minRadius * 2, offset * 2); // x
				y1 = Rnd.get(x1, offset * 2); // distance
				y1 = (int) Math.sqrt(y1 * y1 - x1 * x1); // y
				if (x1 > offset + minRadius)
				{
					x1 = leader.getX() + x1 - offset;
				}
				else
				{
					x1 = leader.getX() - x1 + minRadius;
				}
				if (y1 > offset + minRadius)
				{
					y1 = leader.getY() + y1 - offset;
				}
				else
				{
					y1 = leader.getY() - y1 + minRadius;
				}

				z1 = leader.getZ();
				// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
				moveTo(x1, y1, z1);
			}
			else if (Rnd.nextInt(RANDOM_WALK_RATE) == 0)
			{
				if (_skillrender.hasBuffSkill())
				{
					for (L2Skill sk : _skillrender.aiSkills[L2NpcTemplate.AIST_BUFF])
					{
						if (cast(sk))
						{
							return;
						}
					}
				}
			}
		}
		// Order to the L2MonsterInstance to random walk (1/100)
		else if (npc.getSpawn() != null && Rnd.nextInt(RANDOM_WALK_RATE) == 0)
		{
			int x1, y1, z1;
			final int range = Config.MAX_DRIFT_RANGE;

			if (_skillrender != null && _skillrender.hasBuffSkill())
			{
				for (L2Skill sk : _skillrender.aiSkills[L2NpcTemplate.AIST_BUFF])
				{
					if (cast(sk))
					{
						return;
					}
				}
			}

			// If NPC with random coord in territory
			if (npc.getSpawn().getGroup() != null)
			{
				// Calculate a destination point in the spawn area
				int p[] = npc.getSpawn().getGroup().getRandomPoint();
				x1 = p[0];
				y1 = p[1];

				// Calculate the distance between the current position of the L2Character and the target (x,y)
				double distance2 = npc.getPlanDistanceSq(x1, y1);
				if (distance2 > (range + range) * (range + range))
				{
					npc.setisReturningToSpawnPoint(true);
					float delay = (float) Math.sqrt(distance2) / range;
					x1 = npc.getX() + (int) ((x1 - npc.getX()) / delay);
					y1 = npc.getY() + (int) ((y1 - npc.getY()) / delay);
				}

				z1 = GeoData.getInstance().getHeight(x1, y1, npc.getZ());
			}
			else
			{
				// If NPC with fixed coord
				x1 = npc.getSpawn().getX();
				y1 = npc.getSpawn().getY();
				z1 = npc.getSpawn().getZ();

				if (!npc.isInsideRadius(x1, y1, range, false))
				{
					npc.setisReturningToSpawnPoint(true);
				}
				else if (npc.isRndWalk())
				{
					x1 = Rnd.nextInt(range * 2); // x
					y1 = Rnd.get(x1, range * 2); // distance
					y1 = (int) Math.sqrt(y1 * y1 - x1 * x1); // y
					x1 += npc.getSpawn().getX() - range;
					y1 += npc.getSpawn().getY() - range;
					z1 = GeoData.getInstance().getHeight(x1, y1, npc.getZ());
				}
				else
				{
					return;
				}
			}

			//Logozo.debug("Current pos ("+getX()+", "+getY()+"), moving to ("+x1+", "+y1+").");
			// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
			moveTo(x1, y1, z1);
		}
	}

	/**
	 * Manage AI attack thinks of a L2Attackable (called by onEvtThink).<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Update the attack timeout if actor is running</li>
	 * <li>If target is dead or timeout is expired, stop this attack and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>Call all L2Object of its Faction inside the Faction Range</li>
	 * <li>Chose a target and order to attack it with magic skill or physical attack</li><BR><BR>
	 * <p>
	 * TODO: Manage casting rules to healer mobs (like Ant Nurses)
	 */
	private boolean _callingFaction = false;

	private void thinkAttack()
	{
		final L2Attackable npc = getActiveChar();
		if (npc.isCastingNow())
		{
			return;
		}

		L2Character originalAttackTarget = getAttackTarget();

		// Check if target is dead or if timeout is expired to stop this attack
		if (originalAttackTarget == null || originalAttackTarget.isAlikeDead() ||
				_attackTimeout < TimeController.getGameTicks() ||
				!npc.isRaid() && originalAttackTarget.isAffected(L2EffectType.UNTARGETABLE.getMask()))
		{
			if (_attackTimeout < TimeController.getGameTicks() && originalAttackTarget instanceof L2Npc &&
					((L2Npc) originalAttackTarget).getClan() != null &&
					((L2Npc) originalAttackTarget).getClan().equalsIgnoreCase(npc.getEnemyClan()))
			{
				_attackTimeout = MAX_ATTACK_TIMEOUT + TimeController.getGameTicks();
				return;
			}
			// Stop hating this target after the attack timeout or if target is dead
			if (originalAttackTarget != null)
			{
				npc.stopHating(originalAttackTarget);
			}

			// Set the AI Intention to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);

			npc.setWalking();
			return;
		}

		final int collision = npc.getTemplate().collisionRadius;

		// Handle all L2Object of its Faction inside the Faction Range

		String faction_id = getActiveChar().getFactionId();
		if (faction_id != null && !faction_id.isEmpty() &&
				Thread.currentThread().getStackTrace().length < 50) // Mega ugly check, but...
		{
			_callingFaction = true;
			int factionRange = npc.getClanRange() + collision;
			// Go through all L2Object that belong to its faction
			Collection<L2Object> objs = npc.getKnownList().getKnownObjects().values();
			//synchronized (_actor.getKnownList().getKnownObjects())
			try
			{
				for (L2Object obj : objs)
				{
					if (obj instanceof L2Npc)
					{
						L2Npc called = (L2Npc) obj;

						//Handle SevenSigns mob Factions
						final String npcfaction = called.getFactionId();
						if (npcfaction == null || npcfaction.isEmpty())
						{
							continue;
						}

						if (!faction_id.equals(npcfaction))
						{
							continue;
						}

						// Check if the L2Object is inside the Faction Range of
						// the actor
						if (npc.isInsideRadius(called, factionRange, true, false) && called.hasAI())
						{
							if (Math.abs(originalAttackTarget.getZ() - called.getZ()) < 600 &&
									npc.getAttackByList().contains(originalAttackTarget) &&
									(called.getAI()._intention == CtrlIntention.AI_INTENTION_IDLE ||
											called.getAI()._intention == CtrlIntention.AI_INTENTION_ACTIVE) &&
									called.getInstanceId() == npc.getInstanceId() && !(called instanceof L2Attackable &&
									((L2AttackableAI) called.getAI())._callingFaction))
							//									&& GeoData.getInstance().canSeeTarget(called, npc))
							{
								if (originalAttackTarget instanceof L2Playable)
								{
									Quest[] quests =
											called.getTemplate().getEventQuests(Quest.QuestEventType.ON_FACTION_CALL);
									if (quests != null)
									{
										L2PcInstance player = originalAttackTarget.getActingPlayer();
										boolean isSummon = originalAttackTarget instanceof L2Summon;
										for (Quest quest : quests)
										{
											quest.notifyFactionCall(called, getActiveChar(), player, isSummon);
										}
									}
								}
								else if (called instanceof L2Attackable && getAttackTarget() != null &&
										called.getAI()._intention != CtrlIntention.AI_INTENTION_ATTACK)
								{
									((L2Attackable) called)
											.addDamageHate(getAttackTarget(), 0, npc.getHating(getAttackTarget()));
									called.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getAttackTarget());
								}
							}
						}
					}
				}
			}
			catch (NullPointerException e)
			{
				Log.log(Level.WARNING, "L2AttackableAI: thinkAttack() faction call failed: " + e.getMessage(), e);
			}

			_callingFaction = false;
		}

		if (npc.isCoreAIDisabled())
		{
			return;
		}

		/*
		if (_actor.getTarget() == null || this.getAttackTarget() == null || this.getAttackTarget().isDead() || ctarget == _actor)
			AggroReconsider();
		 */

		//----------------------------------------------------------------

		//------------------------------------------------------------------------------
		//Initialize data
		L2Character mostHate = npc.getMostHated();
		if (mostHate == null)
		{
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
		if (!npc.isMovementDisabled() && Rnd.nextInt(100) <= 3)
		{
			for (L2Object nearby : npc.getKnownList().getKnownObjects().values())
			{
				if (nearby instanceof L2Attackable && npc.isInsideRadius(nearby, collision, false, false) &&
						nearby != mostHate)
				{
					int newX = combinedCollision + Rnd.get(40);
					if (Rnd.nextBoolean())
					{
						newX = mostHate.getX() + newX;
					}
					else
					{
						newX = mostHate.getX() - newX;
					}
					int newY = combinedCollision + Rnd.get(40);
					if (Rnd.nextBoolean())
					{
						newY = mostHate.getY() + newY;
					}
					else
					{
						newY = mostHate.getY() - newY;
					}

					if (!npc.isInsideRadius(newX, newY, collision, false))
					{
						int newZ = npc.getZ() + 30;
						if (Config.GEODATA == 0 || GeoData.getInstance()
								.canMoveFromToTarget(npc.getX(), npc.getY(), npc.getZ(), newX, newY, newZ,
										npc.getInstanceId()))
						{
							moveTo(newX, newY, newZ);
						}
					}
					return;
				}
			}
		}
		//Dodge if its needed
		if (!npc.isMovementDisabled() && npc.getCanDodge() > 0)
		{
			if (Rnd.get(100) <= npc.getCanDodge())
			{
				// Micht: kepping this one otherwise we should do 2 sqrt
				double distance2 = npc.getPlanDistanceSq(mostHate.getX(), mostHate.getY());
				if (Math.sqrt(distance2) <= 60 + combinedCollision)
				{
					int posX = npc.getX();
					int posY = npc.getY();
					int posZ = npc.getZ() + 30;

					if (Rnd.nextBoolean())
					{
						posX = posX + Rnd.get(100);
					}
					else
					{
						posX = posX - Rnd.get(100);
					}

					if (Rnd.nextBoolean())
					{
						posY = posY + Rnd.get(100);
					}
					else
					{
						posY = posY - Rnd.get(100);
					}

					if (Config.GEODATA == 0 || GeoData.getInstance()
							.canMoveFromToTarget(npc.getX(), npc.getY(), npc.getZ(), posX, posY, posZ,
									npc.getInstanceId()))
					{
						setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(posX, posY, posZ, 0));
					}
					return;
				}
			}
		}

		//------------------------------------------------------------------------------
		// BOSS/Raid Minion Target Reconsider
		if (npc.isRaid() || npc.isRaidMinion())
		{
			chaostime++;
			if (Config.isServer(Config.TENKAI))
			{
				if (npc instanceof L2RaidBossInstance)
				{
					if (!((L2MonsterInstance) npc).hasMinions())
					{
						if (chaostime > Config.RAID_CHAOS_TIME)
						{
							if (Rnd.get(100) <= 100 - npc.getCurrentHp() * 100 / npc.getMaxHp())
							{
								aggroReconsider();
								chaostime = 0;
								return;
							}
						}
					}
					else
					{
						if (chaostime > Config.RAID_CHAOS_TIME)
						{
							if (Rnd.get(100) <= 100 - npc.getCurrentHp() * 200 / npc.getMaxHp())
							{
								aggroReconsider();
								chaostime = 0;
								return;
							}
						}
					}
				}
				else if (npc instanceof L2GrandBossInstance)
				{
					if (chaostime > Config.GRAND_CHAOS_TIME)
					{
						double chaosRate = 100 - npc.getCurrentHp() * 300 / npc.getMaxHp();
						if (chaosRate <= 10 && Rnd.get(100) <= 10 || chaosRate > 10 && Rnd.get(100) <= chaosRate)
						{
							aggroReconsider();
							chaostime = 0;
							return;
						}
					}
				}
				else
				{
					if (chaostime > Config.MINION_CHAOS_TIME)
					{
						if (Rnd.get(100) <= 100 - npc.getCurrentHp() * 200 / npc.getMaxHp())
						{
							aggroReconsider();
							chaostime = 0;
							return;
						}
					}
				}
			}
		}

		if (_skillrender.hasSkill())
		{
			//-------------------------------------------------------------------------------
			//Heal Condition
			if (_skillrender.hasHealSkill() && _skillrender.aiSkills[L2NpcTemplate.AIST_HEAL] != null)
			{
				double percentage = npc.getCurrentHp() / npc.getMaxHp() * 100;
				if (npc.isMinion())
				{
					L2Character leader = npc.getLeader();
					if (leader != null && !leader.isDead() &&
							Rnd.get(100) > leader.getCurrentHp() / leader.getMaxHp() * 100)
					{
						for (L2Skill sk : _skillrender.aiSkills[L2NpcTemplate.AIST_HEAL])
						{
							if (sk.getTargetType() == L2SkillTargetType.TARGET_SELF)
							{
								continue;
							}
							if (sk.getMpConsume() >= npc.getCurrentMp() || npc.isSkillDisabled(sk) ||
									sk.isMagic() && npc.isMuted() || !sk.isMagic() && npc.isPhysicalMuted())
							{
								continue;
							}
							if (!Util.checkIfInRange(
									sk.getCastRange() + collision + leader.getTemplate().collisionRadius, npc, leader,
									false) && !isParty(sk) && !npc.isMovementDisabled())
							{
								moveToPawn(leader,
										sk.getCastRange() + collision + leader.getTemplate().collisionRadius);
								return;
							}
							if (GeoData.getInstance().canSeeTarget(npc, leader))
							{
								clientStopMoving(null);
								npc.setTarget(leader);
								clientStopMoving(null);
								npc.doCast(sk);
								return;
							}
						}
					}
				}
				if (percentage < 60)
				{
					for (L2Skill sk : _skillrender.aiSkills[L2NpcTemplate.AIST_HEAL])
					{
						if (sk.getMpConsume() >= npc.getCurrentMp() || npc.isSkillDisabled(sk) ||
								sk.isMagic() && npc.isMuted() || !sk.isMagic() && npc.isPhysicalMuted())
						{
							continue;
						}
						clientStopMoving(null);
						npc.setTarget(npc);
						npc.doCast(sk);
						return;
					}
				}
				for (L2Skill sk : _skillrender.aiSkills[L2NpcTemplate.AIST_HEAL])
				{
					if (sk.getMpConsume() >= npc.getCurrentMp() || npc.isSkillDisabled(sk) ||
							sk.isMagic() && npc.isMuted() || !sk.isMagic() && npc.isPhysicalMuted())
					{
						continue;
					}

					int allies = 0;
					for (L2Character obj : npc.getKnownList().getKnownCharactersInRadius(sk.getCastRange() + collision))
					{
						if (!(obj instanceof L2Attackable) || obj.isDead())
						{
							continue;
						}

						L2Attackable targets = (L2Attackable) obj;
						if (npc.getFactionId() != null && !npc.getFactionId().equals(targets.getFactionId()))
						{
							continue;
						}
						percentage = targets.getCurrentHp() / targets.getMaxHp() * 100;
						if (percentage < 70 && sk.getTargetType() == L2SkillTargetType.TARGET_ONE)
						{
							if (GeoData.getInstance().canSeeTarget(npc, targets))
							{
								clientStopMoving(null);
								npc.setTarget(obj);
								npc.doCast(sk);
								return;
							}
						}

						allies++;
					}

					if (allies > 0 && isParty(sk))
					{
						clientStopMoving(null);
						npc.doCast(sk);
						return;
					}
				}
			}
			//-------------------------------------------------------------------------------
			//Res Skill Condition
			if (_skillrender.hasResSkill())
			{
				if (npc.isMinion())
				{
					L2Character leader = npc.getLeader();
					if (leader != null && leader.isDead())
					{
						for (L2Skill sk : _skillrender.aiSkills[L2NpcTemplate.AIST_RES])
						{
							if (sk.getTargetType() == L2SkillTargetType.TARGET_SELF)
							{
								continue;
							}
							if (sk.getMpConsume() >= npc.getCurrentMp() || npc.isSkillDisabled(sk) ||
									sk.isMagic() && npc.isMuted() || !sk.isMagic() && npc.isPhysicalMuted())
							{
								continue;
							}
							if (!Util.checkIfInRange(
									sk.getCastRange() + collision + leader.getTemplate().collisionRadius, npc, leader,
									false) && !isParty(sk) && !npc.isMovementDisabled())
							{
								moveToPawn(leader,
										sk.getCastRange() + collision + leader.getTemplate().collisionRadius);
								return;
							}
							if (GeoData.getInstance().canSeeTarget(npc, leader))
							{
								clientStopMoving(null);
								npc.setTarget(leader);
								npc.doCast(sk);
								return;
							}
						}
					}
				}
				if (_skillrender.aiSkills[L2NpcTemplate.AIST_RES] != null)
				{
					for (L2Skill sk : _skillrender.aiSkills[L2NpcTemplate.AIST_RES])
					{
						if (sk.getMpConsume() >= npc.getCurrentMp() || npc.isSkillDisabled(sk) ||
								sk.isMagic() && npc.isMuted() || !sk.isMagic() && npc.isPhysicalMuted())
						{
							continue;
						}
						if (sk.getTargetType() == L2SkillTargetType.TARGET_ONE)
						{
							for (L2Character obj : npc.getKnownList()
									.getKnownCharactersInRadius(sk.getCastRange() + collision))
							{
								if (!(obj instanceof L2Attackable) || !obj.isDead())
								{
									continue;
								}

								L2Attackable targets = (L2Attackable) obj;
								if (npc.getFactionId() != null && !npc.getFactionId().equals(targets.getFactionId()))
								{
									continue;
								}
								if (Rnd.get(100) < 10)
								{
									if (GeoData.getInstance().canSeeTarget(npc, targets))
									{
										clientStopMoving(null);
										npc.setTarget(obj);
										npc.doCast(sk);
										return;
									}
								}
							}
						}
						if (isParty(sk))
						{
							clientStopMoving(null);
							L2Object target = getAttackTarget();
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
		if (mostHate.isMoving())
		{
			range = range + 50;
			if (npc.isMoving())
			{
				range = range + 50;
			}
		}

		//-------------------------------------------------------------------------------
		//Immobilize Condition
		if (npc.isMovementDisabled() && (dist > range || mostHate.isMoving()) || dist > range && mostHate.isMoving())
		{
			movementDisable();
			return;
		}
		setTimepass(0);
		//--------------------------------------------------------------------------------
		//Skill Use
		if (_skillrender.hasSkill())
		{
			if (Rnd.get(100) <= npc.getSkillChance())
			{
				L2Skill skills = _skillrender.aiSkills[L2NpcTemplate.AIST_GENERAL]
						.get(Rnd.nextInt(_skillrender.aiSkills[L2NpcTemplate.AIST_GENERAL].size()));
				if (cast(skills))
				{
					return;
				}
				for (L2Skill sk : _skillrender.aiSkills[L2NpcTemplate.AIST_GENERAL])
				{
					if (cast(sk))
					{
						return;
					}
				}
			}

			//--------------------------------------------------------------------------------
			//Long/Short Range skill Usage
			if (npc.hasLSkill() || npc.hasSSkill())
			{
				if (npc.hasSSkill() && dist2 <= 150 && Rnd.get(100) <= npc.getSSkillChance())
				{
					sSkillRender();
					if (_skillrender.aiSkills[L2NpcTemplate.AIST_SHORT_RANGE] != null)
					{
						L2Skill skills = _skillrender.aiSkills[L2NpcTemplate.AIST_SHORT_RANGE]
								.get(Rnd.nextInt(_skillrender.aiSkills[L2NpcTemplate.AIST_SHORT_RANGE].size()));
						if (cast(skills))
						{
							return;
						}
						for (L2Skill sk : _skillrender.aiSkills[L2NpcTemplate.AIST_SHORT_RANGE])
						{
							if (cast(sk))
							{
								return;
							}
						}
					}
				}
				if (npc.hasLSkill() && dist2 > 150 && Rnd.get(100) <= npc.getLSkillChance())
				{
					lSkillRender();
					if (_skillrender.aiSkills[L2NpcTemplate.AIST_LONG_RANGE] != null)
					{
						L2Skill skills = _skillrender.aiSkills[L2NpcTemplate.AIST_LONG_RANGE]
								.get(Rnd.nextInt(_skillrender.aiSkills[L2NpcTemplate.AIST_LONG_RANGE].size()));
						if (cast(skills))
						{
							return;
						}
						for (L2Skill sk : _skillrender.aiSkills[L2NpcTemplate.AIST_LONG_RANGE])
						{
							if (cast(sk))
							{
								return;
							}
						}
					}
				}
			}
		}

		//--------------------------------------------------------------------------------
		// Starts Melee or Primary Skill
		if (dist2 > range || !GeoData.getInstance().canSeeTarget(npc, mostHate))
		{
			if (npc.isMovementDisabled())
			{
				targetReconsider();
			}
			else
			{
				if (getAttackTarget() == null)
				{
					return;
				}
				if (getAttackTarget().isMoving())
				{
					range -= 100;
				}
				if (range < 5)
				{
					range = 5;
				}
				moveToPawn(getAttackTarget(), range);
			}
		}
		else
		{
			melee(npc.getPrimaryAttack());
		}
	}

	private void melee(int type)
	{
		if (type != 0)
		{
			switch (type)
			{
				case -1:
				{
					if (_skillrender.aiSkills[L2NpcTemplate.AIST_GENERAL] != null)
					{
						L2Skill s = _skillrender.aiSkills[L2NpcTemplate.AIST_GENERAL]
								.get(Rnd.nextInt(_skillrender.aiSkills[L2NpcTemplate.AIST_GENERAL].size()));
						if (cast(s))
						{
							return;
						}
						for (L2Skill sk : _skillrender.aiSkills[L2NpcTemplate.AIST_GENERAL])
						{
							if (cast(sk))
							{
								return;
							}
						}
					}
					break;
				}
				case 1:
				{
					if (_skillrender.hasAtkSkill())
					{
						L2Skill s = _skillrender.aiSkills[L2NpcTemplate.AIST_ATK]
								.get(Rnd.nextInt(_skillrender.aiSkills[L2NpcTemplate.AIST_ATK].size()));
						if (cast(s))
						{
							return;
						}
						for (L2Skill sk : _skillrender.aiSkills[L2NpcTemplate.AIST_ATK])
						{
							if (cast(sk))
							{
								return;
							}
						}
					}
					break;
				}
				default:
				{
					if (_skillrender.aiSkills[L2NpcTemplate.AIST_GENERAL] != null)
					{
						for (L2Skill sk : _skillrender.aiSkills[L2NpcTemplate.AIST_GENERAL])
						{
							if (sk.getId() == getActiveChar().getPrimaryAttack() && cast(sk))
							{
								return;
							}
						}
					}
				}
				break;
			}
		}

		_accessor.doAttack(getAttackTarget());
	}

	private boolean cast(L2Skill sk)
	{
		if (sk == null)
		{
			return false;
		}

		final L2Attackable caster = getActiveChar();

		if (caster.isCastingNow() && !sk.isSimultaneousCast())
		{
			return false;
		}

		if (sk.getMpConsume() >= caster.getCurrentMp() || caster.isSkillDisabled(sk) ||
				sk.isMagic() && caster.isMuted() || !sk.isMagic() && caster.isPhysicalMuted())
		{
			return false;
		}
		if (getAttackTarget() == null && caster.getMostHated() != null)
		{
			setAttackTarget(caster.getMostHated());
		}
		L2Character attackTarget = getAttackTarget();
		if (attackTarget == null)
		{
			return false;
		}
		double dist = Math.sqrt(caster.getPlanDistanceSq(attackTarget.getX(), attackTarget.getY()));
		double dist2 = dist - attackTarget.getTemplate().collisionRadius;
		double srange = sk.getCastRange() + caster.getTemplate().collisionRadius;
		if (attackTarget.isMoving())
		{
			dist2 = dist2 - 30;
		}

		switch (sk.getSkillType())
		{

			case BUFF:
			{
				if (caster.getFirstEffect(sk) == null)
				{
					clientStopMoving(null);
					//L2Object target = attackTarget;
					caster.setTarget(caster);
					caster.doCast(sk);
					//_actor.setTarget(target);
					return true;
				}
				//----------------------------------------
				//If actor already have buff, start looking at others same faction mob to cast
				if (sk.getTargetType() == L2SkillTargetType.TARGET_SELF)
				{
					return false;
				}
				if (sk.getTargetType() == L2SkillTargetType.TARGET_ONE ||
						sk.getTargetType() == L2SkillTargetType.TARGET_SINGLE)
				{
					L2Character target = effectTargetReconsider(sk, true);
					if (target != null)
					{
						clientStopMoving(null);
						caster.setTarget(target);
						caster.doCast(sk);
						caster.setTarget(attackTarget);
						return true;
					}
				}
				if (canParty(sk))
				{
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
			case BALANCE_LIFE:
			{
				double percentage = caster.getCurrentHp() / caster.getMaxHp() * 100;
				if (caster.isMinion() && sk.getTargetType() != L2SkillTargetType.TARGET_SELF)
				{
					L2Character leader = caster.getLeader();
					if (leader != null && !leader.isDead() &&
							Rnd.get(100) > leader.getCurrentHp() / leader.getMaxHp() * 100)
					{
						if (!Util.checkIfInRange(sk.getCastRange() + caster.getTemplate().collisionRadius +
								leader.getTemplate().collisionRadius, caster, leader, false) && !isParty(sk) &&
								!caster.isMovementDisabled())
						{
							moveToPawn(leader, sk.getCastRange() + caster.getTemplate().collisionRadius +
									leader.getTemplate().collisionRadius);
						}
						if (GeoData.getInstance().canSeeTarget(caster, leader))
						{
							clientStopMoving(null);
							caster.setTarget(leader);
							caster.doCast(sk);
							return true;
						}
					}
				}
				if (Rnd.get(100) < (100 - percentage) / 3)
				{
					clientStopMoving(null);
					caster.setTarget(caster);
					caster.doCast(sk);
					return true;
				}

				if (sk.getTargetType() == L2SkillTargetType.TARGET_ONE)
				{
					for (L2Character obj : caster.getKnownList()
							.getKnownCharactersInRadius(sk.getCastRange() + caster.getTemplate().collisionRadius))
					{
						if (!(obj instanceof L2Attackable) || obj.isDead())
						{
							continue;
						}

						L2Attackable targets = (L2Attackable) obj;
						if (caster.getFactionId() != null && !caster.getFactionId().equals(targets.getFactionId()))
						{
							continue;
						}
						percentage = targets.getCurrentHp() / targets.getMaxHp() * 100;
						if (Rnd.get(100) < (100 - percentage) / 10)
						{
							if (GeoData.getInstance().canSeeTarget(caster, targets))
							{
								clientStopMoving(null);
								caster.setTarget(obj);
								caster.doCast(sk);
								return true;
							}
						}
					}
				}
				if (isParty(sk))
				{
					for (L2Character obj : caster.getKnownList()
							.getKnownCharactersInRadius(sk.getSkillRadius() + caster.getTemplate().collisionRadius))
					{
						if (!(obj instanceof L2Attackable))
						{
							continue;
						}
						L2Npc targets = (L2Npc) obj;
						if (caster.getFactionId() != null && targets.getFactionId().equals(caster.getFactionId()))
						{
							if (obj.getCurrentHp() < obj.getMaxHp() && Rnd.get(100) <= 20)
							{
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
			case RESURRECT:
			{
				if (!isParty(sk))
				{
					if (caster.isMinion() && sk.getTargetType() != L2SkillTargetType.TARGET_SELF)
					{
						L2Character leader = caster.getLeader();
						if (leader != null && leader.isDead())
						{
							if (!Util.checkIfInRange(sk.getCastRange() + caster.getTemplate().collisionRadius +
									leader.getTemplate().collisionRadius, caster, leader, false) && !isParty(sk) &&
									!caster.isMovementDisabled())
							{
								moveToPawn(leader, sk.getCastRange() + caster.getTemplate().collisionRadius +
										leader.getTemplate().collisionRadius);
							}
						}
						if (GeoData.getInstance().canSeeTarget(caster, leader))
						{
							clientStopMoving(null);
							caster.setTarget(leader);
							caster.doCast(sk);
							return true;
						}
					}

					for (L2Character obj : caster.getKnownList()
							.getKnownCharactersInRadius(sk.getCastRange() + caster.getTemplate().collisionRadius))
					{
						if (!(obj instanceof L2Attackable) || !obj.isDead())
						{
							continue;
						}

						L2Attackable targets = (L2Attackable) obj;
						if (caster.getFactionId() != null && !caster.getFactionId().equals(targets.getFactionId()))
						{
							continue;
						}
						if (Rnd.get(100) < 10)
						{
							if (GeoData.getInstance().canSeeTarget(caster, targets))
							{
								clientStopMoving(null);
								caster.setTarget(obj);
								caster.doCast(sk);
								return true;
							}
						}
					}
				}
				else if (isParty(sk))
				{
					for (L2Character obj : caster.getKnownList()
							.getKnownCharactersInRadius(sk.getSkillRadius() + caster.getTemplate().collisionRadius))
					{
						if (!(obj instanceof L2Attackable))
						{
							continue;
						}
						L2Npc targets = (L2Npc) obj;
						if (caster.getFactionId() != null && caster.getFactionId().equals(targets.getFactionId()))
						{
							if (obj.getCurrentHp() < obj.getMaxHp() && Rnd.get(100) <= 20)
							{
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
			case DEBUFF:
			{
				if (GeoData.getInstance().canSeeTarget(caster, attackTarget) && !canAOE(sk) && !attackTarget.isDead() &&
						dist2 <= srange)
				{
					if (attackTarget.getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if (sk.getTargetType() == L2SkillTargetType.TARGET_AURA ||
							sk.getTargetType() == L2SkillTargetType.TARGET_BEHIND_AURA ||
							sk.getTargetType() == L2SkillTargetType.TARGET_FRONT_AURA)
					{
						clientStopMoving(null);
						//L2Object target = attackTarget;
						//_actor.setTarget(_actor);
						caster.doCast(sk);
						//_actor.setTarget(target);
						return true;
					}
					if ((sk.getTargetType() == L2SkillTargetType.TARGET_AREA ||
							sk.getTargetType() == L2SkillTargetType.TARGET_BEHIND_AREA ||
							sk.getTargetType() == L2SkillTargetType.TARGET_FRONT_AREA) &&
							GeoData.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() &&
							dist2 <= srange)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (sk.getTargetType() == L2SkillTargetType.TARGET_ONE)
				{
					L2Character target = effectTargetReconsider(sk, false);
					if (target != null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			}
			case CANCEL:
			case NEGATE:
			{
				// decrease cancel probability
				if (Rnd.get(50) != 0)
				{
					return true;
				}

				if (sk.getTargetType() == L2SkillTargetType.TARGET_ONE)
				{
					if (attackTarget.getFirstEffect(L2AbnormalType.BUFF) != null &&
							GeoData.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() &&
							dist2 <= srange)
					{
						clientStopMoving(null);
						//L2Object target = attackTarget;
						//_actor.setTarget(_actor);
						caster.doCast(sk);
						//_actor.setTarget(target);
						return true;
					}
					L2Character target = effectTargetReconsider(sk, false);
					if (target != null)
					{
						clientStopMoving(null);
						caster.setTarget(target);
						caster.doCast(sk);
						caster.setTarget(attackTarget);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if ((sk.getTargetType() == L2SkillTargetType.TARGET_AURA ||
							sk.getTargetType() == L2SkillTargetType.TARGET_BEHIND_AURA ||
							sk.getTargetType() == L2SkillTargetType.TARGET_FRONT_AURA) &&
							GeoData.getInstance().canSeeTarget(caster, attackTarget))

					{
						clientStopMoving(null);
						//L2Object target = attackTarget;
						//_actor.setTarget(_actor);
						caster.doCast(sk);
						//_actor.setTarget(target);
						return true;
					}
					else if ((sk.getTargetType() == L2SkillTargetType.TARGET_AREA ||
							sk.getTargetType() == L2SkillTargetType.TARGET_BEHIND_AREA ||
							sk.getTargetType() == L2SkillTargetType.TARGET_FRONT_AREA) &&
							GeoData.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() &&
							dist2 <= srange)
					{
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
			case MAXHPDAMPERCENT:
			{
				if (!canAura(sk))
				{
					if (GeoData.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() &&
							dist2 <= srange)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					else
					{
						L2Character target = skillTargetReconsider(sk);
						if (target != null)
						{
							clientStopMoving(null);
							caster.setTarget(target);
							caster.doCast(sk);
							caster.setTarget(attackTarget);
							return true;
						}
					}
				}
				else
				{
					clientStopMoving(null);
					caster.doCast(sk);
					return true;
				}
				break;
			}
			default:
			{
				if (!canAura(sk))
				{

					if (GeoData.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() &&
							dist2 <= srange)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					else
					{
						L2Character target = skillTargetReconsider(sk);
						if (target != null)
						{
							clientStopMoving(null);
							caster.setTarget(target);
							caster.doCast(sk);
							caster.setTarget(attackTarget);
							return true;
						}
					}
				}
				else
				{
					clientStopMoving(null);
					//L2Object targets = attackTarget;
					//_actor.setTarget(_actor);
					caster.doCast(sk);
					//_actor.setTarget(targets);
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
	private void movementDisable()
	{
		L2Character attackTarget = getAttackTarget();
		if (attackTarget == null)
		{
			return;
		}

		final L2Attackable npc = getActiveChar();
		double dist = 0;
		double dist2 = 0;
		int range = 0;
		try
		{
			if (npc.getTarget() == null)
			{
				npc.setTarget(attackTarget);
			}
			dist = Math.sqrt(npc.getPlanDistanceSq(attackTarget.getX(), attackTarget.getY()));
			dist2 = dist - npc.getTemplate().collisionRadius;
			range = npc.getPhysicalAttackRange() + npc.getTemplate().collisionRadius +
					attackTarget.getTemplate().collisionRadius;
			if (attackTarget.isMoving())
			{
				dist = dist - 30;
				if (npc.isMoving())
				{
					dist = dist - 50;
				}
			}

			//Check if activeChar has any skill
			if (_skillrender.hasSkill())
			{
				//-------------------------------------------------------------
				//Try to stop the target or disable the target as priority
				int random = Rnd.get(100);
				if (_skillrender.hasImmobiliseSkill() && !attackTarget.isImmobilized() && random < 2)
				{
					for (L2Skill sk : _skillrender.aiSkills[L2NpcTemplate.AIST_IMMOBILIZE])
					{
						if (sk.getMpConsume() >= npc.getCurrentMp() || npc.isSkillDisabled(sk) ||
								sk.getCastRange() + npc.getTemplate().collisionRadius +
										attackTarget.getTemplate().collisionRadius <= dist2 && !canAura(sk) ||
								sk.isMagic() && npc.isMuted() || !sk.isMagic() && npc.isPhysicalMuted())
						{
							continue;
						}
						if (!GeoData.getInstance().canSeeTarget(npc, attackTarget))
						{
							continue;
						}
						if (attackTarget.getFirstEffect(sk) == null)
						{
							clientStopMoving(null);
							//L2Object target = attackTarget;
							//_actor.setTarget(_actor);
							npc.doCast(sk);
							//_actor.setTarget(target);
							return;
						}
					}
				}
				//-------------------------------------------------------------
				//Same as Above, but with Mute/FEAR etc....
				if (_skillrender.hasCOTSkill() && random < 5)
				{
					for (L2Skill sk : _skillrender.aiSkills[L2NpcTemplate.AIST_COT])
					{
						if (sk.getMpConsume() >= npc.getCurrentMp() || npc.isSkillDisabled(sk) ||
								sk.getCastRange() + npc.getTemplate().collisionRadius +
										attackTarget.getTemplate().collisionRadius <= dist2 && !canAura(sk) ||
								sk.isMagic() && npc.isMuted() || !sk.isMagic() && npc.isPhysicalMuted())
						{
							continue;
						}
						if (!GeoData.getInstance().canSeeTarget(npc, attackTarget))
						{
							continue;
						}
						if (attackTarget.getFirstEffect(sk) == null)
						{
							clientStopMoving(null);
							//L2Object target = attackTarget;
							//_actor.setTarget(_actor);
							npc.doCast(sk);
							//_actor.setTarget(target);
							return;
						}
					}
				}
				//-------------------------------------------------------------
				if (_skillrender.hasDebuffSkill() && random < 8)
				{
					for (L2Skill sk : _skillrender.aiSkills[L2NpcTemplate.AIST_DEBUFF])
					{
						if (sk.getMpConsume() >= npc.getCurrentMp() || npc.isSkillDisabled(sk) ||
								sk.getCastRange() + npc.getTemplate().collisionRadius +
										attackTarget.getTemplate().collisionRadius <= dist2 && !canAura(sk) ||
								sk.isMagic() && npc.isMuted() || !sk.isMagic() && npc.isPhysicalMuted())
						{
							continue;
						}
						if (!GeoData.getInstance().canSeeTarget(npc, attackTarget))
						{
							continue;
						}
						if (attackTarget.getFirstEffect(sk) == null)
						{
							clientStopMoving(null);
							//L2Object target = attackTarget;
							//_actor.setTarget(_actor);
							npc.doCast(sk);
							//_actor.setTarget(target);
							return;
						}
					}
				}
				//-------------------------------------------------------------
				//Some side effect skill like CANCEL or NEGATE
				if (_skillrender.hasNegativeSkill() && random < 9)
				{
					for (L2Skill sk : _skillrender.aiSkills[L2NpcTemplate.AIST_NEGATIVE])
					{
						if (sk.getMpConsume() >= npc.getCurrentMp() || npc.isSkillDisabled(sk) ||
								sk.getCastRange() + npc.getTemplate().collisionRadius +
										attackTarget.getTemplate().collisionRadius <= dist2 && !canAura(sk) ||
								sk.isMagic() && npc.isMuted() || !sk.isMagic() && npc.isPhysicalMuted())
						{
							continue;
						}
						if (!GeoData.getInstance().canSeeTarget(npc, attackTarget))
						{
							continue;
						}
						if (attackTarget.getFirstEffect(L2AbnormalType.BUFF) != null)
						{
							clientStopMoving(null);
							//L2Object target = attackTarget;
							//_actor.setTarget(_actor);
							npc.doCast(sk);
							//_actor.setTarget(target);
							return;
						}
					}
				}
				//-------------------------------------------------------------
				//Start ATK SKILL when nothing can be done
				if (_skillrender.hasAtkSkill() && (npc.isMovementDisabled() || npc.getAiType() == AIType.MAGE ||
						npc.getAiType() == AIType.HEALER))
				{
					for (L2Skill sk : _skillrender.aiSkills[L2NpcTemplate.AIST_ATK])
					{
						if (sk.getMpConsume() >= npc.getCurrentMp() || npc.isSkillDisabled(sk) ||
								sk.getCastRange() + npc.getTemplate().collisionRadius +
										attackTarget.getTemplate().collisionRadius <= dist2 && !canAura(sk) ||
								sk.isMagic() && npc.isMuted() || !sk.isMagic() && npc.isPhysicalMuted())
						{
							continue;
						}
						if (!GeoData.getInstance().canSeeTarget(npc, attackTarget))
						{
							continue;
						}
						clientStopMoving(null);
						//L2Object target = attackTarget;
						//_actor.setTarget(_actor);
						npc.doCast(sk);
						//_actor.setTarget(target);
						return;
					}
				}
				//-------------------------------------------------------------
				//if there is no ATK skill to use, then try Universal skill
                /*
				if (_skillrender.hasUniversalSkill())
				{
					for (L2Skill sk:_skillrender._universalskills)
					{
						if (sk.getMpConsume()>=_actor.getCurrentMp()
								|| _actor.isSkillDisabled(sk.getId())
								||(sk.getCastRange()+ _actor.getTemplate().collisionRadius + attackTarget.getTemplate().collisionRadius <= dist2 && !canAura(sk))
								||(sk.isMagic()&&_actor.isMuted())
								||(!sk.isMagic()&&_actor.isPhysicalMuted()))
						{
							continue;
						}
						if (!GeoData.getInstance().canSeeTarget(_actor,attackTarget))
							continue;
						clientStopMoving(null);
						L2Object target = attackTarget;
						//_actor.setTarget(_actor);
						_actor.doCast(sk);
						//_actor.setTarget(target);
						return;
					}
				}

				 */
			}
			//timepass = timepass + 1;
			if (npc.isMovementDisabled())
			{
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

			if (dist > range || !GeoData.getInstance().canSeeTarget(npc, attackTarget))
			{
				if (attackTarget.isMoving())
				{
					range -= 100;
				}
				if (range < 5)
				{
					range = 5;
				}
				moveToPawn(attackTarget, range);
				return;
			}

			melee(npc.getPrimaryAttack());
		}
		catch (NullPointerException e)
		{
			setIntention(AI_INTENTION_ACTIVE);
			Log.log(Level.WARNING, this + " - failed executing movementDisable(): " + e.getMessage(), e);
		}
	}

	private L2Character effectTargetReconsider(L2Skill sk, boolean positive)
	{
		if (sk == null)
		{
			return null;
		}
		L2Attackable actor = getActiveChar();
		if (sk.getSkillType() != L2SkillType.NEGATE || sk.getSkillType() != L2SkillType.CANCEL)
		{
			if (!positive)
			{
				double dist = 0;
				double dist2 = 0;
				int range = 0;

				for (L2Character obj : actor.getAttackByList())
				{
					if (obj == null || obj.isDead() || !GeoData.getInstance().canSeeTarget(actor, obj) ||
							obj == getAttackTarget())
					{
						continue;
					}
					try
					{
						actor.setTarget(getAttackTarget());
						dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
						dist2 = dist - actor.getTemplate().collisionRadius;
						range = sk.getCastRange() + actor.getTemplate().collisionRadius +
								obj.getTemplate().collisionRadius;
						if (obj.isMoving())
						{
							dist2 = dist2 - 70;
						}
					}
					catch (NullPointerException e)
					{
						continue;
					}
					if (dist2 <= range)
					{
						if (getAttackTarget().getFirstEffect(sk) == null)
						{
							return obj;
						}
					}
				}

				//----------------------------------------------------------------------
				//If there is nearby Target with aggro, start going on random target that is attackable
				for (L2Character obj : actor.getKnownList().getKnownCharactersInRadius(range))
				{
					if (obj.isDead() || !GeoData.getInstance().canSeeTarget(actor, obj))
					{
						continue;
					}
					try
					{
						actor.setTarget(getAttackTarget());
						dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
						dist2 = dist;
						range = sk.getCastRange() + actor.getTemplate().collisionRadius +
								obj.getTemplate().collisionRadius;
						if (obj.isMoving())
						{
							dist2 = dist2 - 70;
						}
					}
					catch (NullPointerException e)
					{
						continue;
					}
					if (obj instanceof L2Attackable)
					{
						if (actor.getEnemyClan() != null && actor.getEnemyClan().equals(((L2Attackable) obj).getClan()))
						{
							if (dist2 <= range)
							{
								if (getAttackTarget().getFirstEffect(sk) == null)
								{
									return obj;
								}
							}
						}
					}
					if (obj instanceof L2PcInstance || obj instanceof L2Summon)
					{
						if (dist2 <= range)
						{
							if (getAttackTarget().getFirstEffect(sk) == null)
							{
								return obj;
							}
						}
					}
				}
			}
			else if (positive)
			{
				double dist = 0;
				double dist2 = 0;
				int range = 0;
				for (L2Character obj : actor.getKnownList().getKnownCharactersInRadius(range))
				{
					if (!(obj instanceof L2Attackable) || obj.isDead() ||
							!GeoData.getInstance().canSeeTarget(actor, obj))
					{
						continue;
					}

					L2Attackable targets = (L2Attackable) obj;
					if (actor.getFactionId() != null && !actor.getFactionId().equals(targets.getFactionId()))
					{
						continue;
					}

					try
					{
						actor.setTarget(getAttackTarget());
						dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
						dist2 = dist - actor.getTemplate().collisionRadius;
						range = sk.getCastRange() + actor.getTemplate().collisionRadius +
								obj.getTemplate().collisionRadius;
						if (obj.isMoving())
						{
							dist2 = dist2 - 70;
						}
					}
					catch (NullPointerException e)
					{
						continue;
					}
					if (dist2 <= range)
					{
						if (obj.getFirstEffect(sk) == null)
						{
							return obj;
						}
					}
				}
			}
			return null;
		}
		else
		{
			double dist = 0;
			double dist2 = 0;
			int range = 0;
			range = sk.getCastRange() + actor.getTemplate().collisionRadius +
					getAttackTarget().getTemplate().collisionRadius;
			for (L2Character obj : actor.getKnownList().getKnownCharactersInRadius(range))
			{
				if (obj == null || obj.isDead() || !GeoData.getInstance().canSeeTarget(actor, obj))
				{
					continue;
				}
				try
				{
					actor.setTarget(getAttackTarget());
					dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
					dist2 = dist - actor.getTemplate().collisionRadius;
					range = sk.getCastRange() + actor.getTemplate().collisionRadius + obj.getTemplate().collisionRadius;
					if (obj.isMoving())
					{
						dist2 = dist2 - 70;
					}
				}
				catch (NullPointerException e)
				{
					continue;
				}
				if (obj instanceof L2Attackable)
				{
					if (actor.getEnemyClan() != null && actor.getEnemyClan().equals(((L2Attackable) obj).getClan()))
					{
						if (dist2 <= range)
						{
							if (getAttackTarget().getFirstEffect(L2AbnormalType.BUFF) != null)
							{
								return obj;
							}
						}
					}
				}
				if (obj instanceof L2PcInstance || obj instanceof L2Summon)
				{

					if (dist2 <= range)
					{
						if (getAttackTarget().getFirstEffect(L2AbnormalType.BUFF) != null)
						{
							return obj;
						}
					}
				}
			}
			return null;
		}
	}

	private L2Character skillTargetReconsider(L2Skill sk)
	{
		double dist = 0;
		double dist2 = 0;
		int range = 0;
		L2Attackable actor = getActiveChar();
		List<L2Character> hateList = actor.getHateList();
		if (hateList != null)
		{
			for (L2Character obj : hateList)
			{
				if (obj == null || !GeoData.getInstance().canSeeTarget(actor, obj) || obj.isDead())
				{
					continue;
				}
				try
				{
					actor.setTarget(getAttackTarget());
					dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
					dist2 = dist - actor.getTemplate().collisionRadius;
					range = sk.getCastRange() + actor.getTemplate().collisionRadius +
							getAttackTarget().getTemplate().collisionRadius;
					//if (obj.isMoving())
					//	dist2 = dist2 - 40;
				}
				catch (NullPointerException e)
				{
					continue;
				}
				if (dist2 <= range)
				{
					return obj;
				}
			}
		}

		if (!(actor instanceof L2GuardInstance))
		{
			Collection<L2Object> objs = actor.getKnownList().getKnownObjects().values();
			for (L2Object target : objs)
			{
				try
				{
					actor.setTarget(getAttackTarget());
					dist = Math.sqrt(actor.getPlanDistanceSq(target.getX(), target.getY()));
					dist2 = dist;
					range = sk.getCastRange() + actor.getTemplate().collisionRadius +
							getAttackTarget().getTemplate().collisionRadius;
					//if (obj.isMoving())
					//	dist2 = dist2 - 40;
				}
				catch (NullPointerException e)
				{
					continue;
				}
				L2Character obj = null;
				if (target instanceof L2Character)
				{
					obj = (L2Character) target;
				}
				if (obj == null || !GeoData.getInstance().canSeeTarget(actor, obj) || dist2 > range)
				{
					continue;
				}
				if (obj instanceof L2PcInstance)
				{
					return obj;
				}
				if (obj instanceof L2Attackable)
				{
					if (actor.getEnemyClan() != null && actor.getEnemyClan().equals(((L2Attackable) obj).getClan()))
					{
						return obj;
					}
					if (actor.getIsChaos() != 0)
					{
						if (((L2Attackable) obj).getFactionId() != null &&
								((L2Attackable) obj).getFactionId().equals(actor.getFactionId()))
						{
							continue;
						}
						else
						{
							return obj;
						}
					}
				}
				if (obj instanceof L2Summon)
				{
					return obj;
				}
			}
		}
		return null;
	}

	private void targetReconsider()
	{
		double dist = 0;
		double dist2 = 0;
		int range = 0;
		L2Attackable actor = getActiveChar();
		L2Character mostHate = actor.getMostHated();
		List<L2Character> hateList = actor.getHateList();
		if (hateList != null)
		{
			for (L2Character obj : hateList)
			{
				if (obj == null || !GeoData.getInstance().canSeeTarget(actor, obj) || obj.isDead() || obj != mostHate ||
						obj == actor)
				{
					continue;
				}
				try
				{
					dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
					dist2 = dist - actor.getTemplate().collisionRadius;
					range = actor.getPhysicalAttackRange() + actor.getTemplate().collisionRadius +
							obj.getTemplate().collisionRadius;
					if (obj.isMoving())
					{
						dist2 = dist2 - 70;
					}
				}
				catch (NullPointerException e)
				{
					continue;
				}

				if (dist2 <= range)
				{
					if (mostHate != null)
					{
						actor.addDamageHate(obj, 0, actor.getHating(mostHate));
					}
					else
					{
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
					return;
				}
			}
		}
		if (!(actor instanceof L2GuardInstance))
		{
			Collection<L2Object> objs = actor.getKnownList().getKnownObjects().values();
			for (L2Object target : objs)
			{
				L2Character obj = null;
				if (target instanceof L2Character)
				{
					obj = (L2Character) target;
				}

				if (obj == null || !GeoData.getInstance().canSeeTarget(actor, obj) || obj.isDead() || obj != mostHate ||
						obj == actor || obj == getAttackTarget())
				{
					continue;
				}
				if (obj instanceof L2PcInstance)
				{
					if (mostHate != null)
					{
						actor.addDamageHate(obj, 0, actor.getHating(mostHate));
					}
					else
					{
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
				}
				else if (obj instanceof L2Attackable)
				{
					if (actor.getEnemyClan() != null && actor.getEnemyClan().equals(((L2Attackable) obj).getClan()))
					{
						actor.addDamageHate(obj, 0, actor.getHating(mostHate));
						actor.setTarget(obj);
					}
					if (actor.getIsChaos() != 0)
					{
						if (((L2Attackable) obj).getFactionId() != null &&
								((L2Attackable) obj).getFactionId().equals(actor.getFactionId()))
						{
						}
						else
						{
							if (mostHate != null)
							{
								actor.addDamageHate(obj, 0, actor.getHating(mostHate));
							}
							else
							{
								actor.addDamageHate(obj, 0, 2000);
							}
							actor.setTarget(obj);
							setAttackTarget(obj);
						}
					}
				}
				else if (obj instanceof L2Summon)
				{
					if (mostHate != null)
					{
						actor.addDamageHate(obj, 0, actor.getHating(mostHate));
					}
					else
					{
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
				}
			}
		}
	}

	@SuppressWarnings("null")
	private void aggroReconsider()
	{
		L2Attackable actor = getActiveChar();
		L2Character MostHate = actor.getMostHated();

		List<L2Character> hateList = actor.getHateList();
		if (hateList != null && !hateList.isEmpty())
		{
			int rand = Rnd.get(hateList.size());
			int count = 0;
			for (L2Character obj : hateList)
			{
				if (count < rand)
				{
					count++;
					continue;
				}

				if (obj == null || !GeoData.getInstance().canSeeTarget(actor, obj) || obj.isDead() ||
						obj == getAttackTarget() || obj == actor)
				{
					continue;
				}

				try
				{
					actor.setTarget(getAttackTarget());
				}
				catch (NullPointerException e)
				{
					continue;
				}
				if (MostHate != null)
				{
					actor.addDamageHate(obj, 0, actor.getHating(MostHate));
				}
				else
				{
					actor.addDamageHate(obj, 0, 2000);
				}
				actor.setTarget(obj);
				setAttackTarget(obj);
				return;
			}
		}

		if (!(actor instanceof L2GuardInstance))
		{
			Collection<L2Object> objs = actor.getKnownList().getKnownObjects().values();
			for (L2Object target : objs)
			{
				L2Character obj = null;
				if (target instanceof L2Character)
				{
					obj = (L2Character) target;
				}
				else
				{
					continue;
				}
				if (obj == null || !GeoData.getInstance().canSeeTarget(actor, obj) || obj.isDead() || obj != MostHate ||
						obj == actor)
				{
					continue;
				}
				if (obj instanceof L2PcInstance)
				{
					if (MostHate != null || !MostHate.isDead())
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
					}
					else
					{
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
				}
				else if (obj instanceof L2Attackable)
				{
					if (actor.getEnemyClan() != null && actor.getEnemyClan().equals(((L2Attackable) obj).getClan()))
					{
						if (MostHate != null)
						{
							actor.addDamageHate(obj, 0, actor.getHating(MostHate));
						}
						else
						{
							actor.addDamageHate(obj, 0, 2000);
						}
						actor.setTarget(obj);
					}
					if (actor.getIsChaos() != 0)
					{
						if (((L2Attackable) obj).getFactionId() != null &&
								((L2Attackable) obj).getFactionId().equals(actor.getFactionId()))
						{
						}
						else
						{
							if (MostHate != null)
							{
								actor.addDamageHate(obj, 0, actor.getHating(MostHate));
							}
							else
							{
								actor.addDamageHate(obj, 0, 2000);
							}
							actor.setTarget(obj);
							setAttackTarget(obj);
						}
					}
				}
				else if (obj instanceof L2Summon)
				{
					if (MostHate != null)
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
					}
					else
					{
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
				}
			}
		}
		else
		{
			Collection<L2Object> objs = actor.getKnownList().getKnownObjects().values();
			for (L2Object target : objs)
			{
				L2Character obj = null;
				if (target instanceof L2Character)
				{
					obj = (L2Character) target;
				}
				else
				{
					continue;
				}
				if (obj == null || !GeoData.getInstance().canSeeTarget(actor, obj) || obj.isDead() || obj != MostHate ||
						obj == actor)
				{
					continue;
				}
				if (obj instanceof L2Npc && actor.getEnemyClan() != null &&
						actor.getEnemyClan().equals(((L2Npc) obj).getClan()))
				{
					if (MostHate != null)
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
					}
					else
					{
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
				}
			}
		}
	}

	private void lSkillRender()
	{
		if (_skillrender.aiSkills[L2NpcTemplate.AIST_LONG_RANGE] == null)
		{
			_skillrender.aiSkills[L2NpcTemplate.AIST_LONG_RANGE] = getActiveChar().getLrangeSkill();
		}
	}

	private void sSkillRender()
	{
		if (_skillrender.aiSkills[L2NpcTemplate.AIST_SHORT_RANGE] == null)
		{
			_skillrender.aiSkills[L2NpcTemplate.AIST_SHORT_RANGE] = getActiveChar().getSrangeSkill();
		}
	}

	/**
	 * Manage AI thinking actions of a L2Attackable.<BR><BR>
	 */
	@Override
	protected void onEvtThink()
	{
		// Check if the actor can't use skills and if a thinking action isn't already in progress
		if (_thinking || getActiveChar().isAllSkillsDisabled())
		{
			return;
		}

		// Start thinking action
		_thinking = true;

		try
		{
			// Manage AI thinks of a L2Attackable
			switch (getIntention())
			{
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
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, this + " -  onEvtThink() failed: " + e.getMessage(), e);
		}
		finally
		{
			// Stop thinking action
			_thinking = false;
		}
	}

	/**
	 * Launch actions corresponding to the Event Attacked.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Init the attack : Calculate the attack timeout, Set the _globalAggro to 0, Add the attacker to the actor _aggroList</li>
	 * <li>Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance</li>
	 * <li>Set the Intention to AI_INTENTION_ATTACK</li><BR><BR>
	 *
	 * @param attacker The L2Character that attacks the actor
	 */
	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		if (attacker instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) attacker;
			if (player.isGM() && !player.getAccessLevel().canGiveDamage())
			{
				return;
			}

			if (_actor instanceof L2EventGolemInstance ||
					_actor instanceof L2GuardInstance && ((L2GuardInstance) _actor).getNpcId() == 40009 &&
							attacker instanceof L2PcInstance && ((L2PcInstance) attacker).getPvpFlag() > 0 &&
							((L2PcInstance) attacker).getReputation() >= 0)
			{
				return;
			}
		}

		L2Attackable me = getActiveChar();

		// Calculate the attack timeout
		_attackTimeout = MAX_ATTACK_TIMEOUT + TimeController.getGameTicks();

		// Set the _globalAggro to 0 to permit attack even just after spawn
		if (_globalAggro < 0)
		{
			_globalAggro = 0;
		}

		// Add the attacker to the _aggroList of the actor
		me.addDamageHate(attacker, 0, 1);

		// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
		if (!me.isRunning())
		{
			me.setRunning();
		}

		// Set the Intention to AI_INTENTION_ATTACK
		if (getIntention() != AI_INTENTION_ATTACK)
		{
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}
		else if (me.getMostHated() != getAttackTarget())
		{
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}

		if (me instanceof L2MonsterInstance)
		{
			L2MonsterInstance master = (L2MonsterInstance) me;

			if (master.hasMinions())
			{
				master.getMinionList().onAssist(me, attacker);
			}

			master = master.getLeader();
			if (master != null && master.hasMinions())
			{
				master.getMinionList().onAssist(me, attacker);
			}
		}

		super.onEvtAttacked(attacker);
	}

	/**
	 * Launch actions corresponding to the Event Aggression.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Add the target to the actor _aggroList or update hate if already present </li>
	 * <li>Set the actor Intention to AI_INTENTION_ATTACK (if actor is L2GuardInstance check if it isn't too far from its home location)</li><BR><BR>
	 *
	 * @param aggro The value of hate to add to the actor against the target
	 */
	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
		L2Attackable me = getActiveChar();

		if (target != null)
		{
			// CUSTOM: clear the aggro so that the aggression is always effective
			if (Config.isServer(Config.TENKAI) && aggro > 1)
			{
				for (AggroInfo aggroInfo : me.getAggroList().values())
				{
					aggroInfo.stopHate();
				}
			}

			// Add the target to the actor _aggroList or update hate if already present
			me.addDamageHate(target, 0, aggro);

			// Set the actor AI Intention to AI_INTENTION_ATTACK
			if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
				if (!me.isRunning())
				{
					me.setRunning();
				}

				setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}

			if (me instanceof L2MonsterInstance)
			{
				L2MonsterInstance master = (L2MonsterInstance) me;

				if (master.hasMinions())
				{
					master.getMinionList().onAssist(me, target);
				}

				master = master.getLeader();
				if (master != null && master.hasMinions())
				{
					master.getMinionList().onAssist(me, target);
				}
			}
		}
	}

	@Override
	protected void onIntentionActive()
	{
		// Cancel attack timeout
		_attackTimeout = Integer.MAX_VALUE;
		super.onIntentionActive();
	}

	public void setGlobalAggro(int value)
	{
		_globalAggro = value;
	}

	/**
	 */
	public void setTimepass(int TP)
	{
		timepass = TP;
	}

	/**
	 * @return Returns the timepass.
	 */
	public int getTimepass()
	{
		return timepass;
	}

	public L2Attackable getActiveChar()
	{
		return (L2Attackable) _actor;
	}
}
