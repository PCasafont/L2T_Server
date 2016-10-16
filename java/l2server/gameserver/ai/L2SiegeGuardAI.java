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
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.*;
import l2server.gameserver.model.actor.instance.L2DefenderInstance;
import l2server.gameserver.model.actor.instance.L2DoorInstance;
import l2server.gameserver.model.actor.instance.L2NpcInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

import java.util.concurrent.Future;

import static l2server.gameserver.ai.CtrlIntention.*;

/**
 * This class manages AI of L2Attackable.<BR><BR>
 */
public class L2SiegeGuardAI extends L2CharacterAI implements Runnable
{

	//
	private static final int MAX_ATTACK_TIMEOUT = 300; // int ticks, i.e. 30 seconds

	/**
	 * The L2Attackable AI task executed every 1s (call onEvtThink method)
	 */
	private Future<?> aiTask;

	/**
	 * For attack AI, analysis of mob and its targets
	 */
	private SelfAnalysis selfAnalysis = new SelfAnalysis();
	//private TargetAnalysis mostHatedAnalysis = new TargetAnalysis();

	/**
	 * The delay after which the attacked is stopped
	 */
	private int attackTimeout;

	/**
	 * The L2Attackable aggro counter
	 */
	private int globalAggro;

	/**
	 * The flag used to indicate that a thinking action is in progress
	 */
	private boolean thinking; // to prevent recursive thinking

	private int attackRange;

	/**
	 * Constructor of L2AttackableAI.<BR><BR>
	 *
	 * @param accessor The AI accessor of the L2Character
	 */
	public L2SiegeGuardAI(L2Character.AIAccessor accessor)
	{
		super(accessor);
		this.selfAnalysis.init();
		this.attackTimeout = Integer.MAX_VALUE;
		this.globalAggro = -10; // 10 seconds timeout of ATTACK after respawn
		this.attackRange = this.actor.getPhysicalAttackRange();
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
	 * <li>The target isn't a Folk, a Door or another L2NpcInstance</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The L2PcInstance target has karma (=PK)</li><BR><BR>
	 * <p>
	 * <B><U> Actor is a L2MonsterInstance</U> :</B><BR><BR>
	 * <li>The target isn't a Folk, a Door or another L2NpcInstance</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The actor is Aggressive</li><BR><BR>
	 *
	 * @param target The targeted L2Object
	 */
	private boolean autoAttackCondition(L2Character target)
	{
		// Check if the target isn't another guard, folk or a door
		if (target == null || target instanceof L2DefenderInstance || target instanceof L2NpcInstance ||
				target instanceof L2DoorInstance || target.isAlikeDead())
		{
			return false;
		}

		// Check if the target isn't invulnerable
		if (target.isInvul(getActor()))
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

		// Get the owner if the target is a summon
		if (target instanceof L2Summon)
		{
			L2PcInstance owner = ((L2Summon) target).getOwner();
			if (this.actor.isInsideRadius(owner, 1000, true, false))
			{
				target = owner;
			}
		}

		// Check if the target is a L2PcInstance
		if (target instanceof L2Playable)
		{
			// Check if the target isn't in silent move mode AND too far (>100)
			if (((L2Playable) target).isSilentMoving() && !this.actor.isInsideRadius(target, 250, false, false))
			{
				return false;
			}
		}
		// Los Check Here
		return this.actor.isAutoAttackable(target) && GeoData.getInstance().canSeeTarget(this.actor, target);
	}

	/**
	 * Set the Intention of this L2CharacterAI and create an  AI Task executed every 1s (call onEvtThink method) for this L2Attackable.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If actor this.knowPlayer isn't EMPTY, AI_INTENTION_IDLE will be change in AI_INTENTION_ACTIVE</B></FONT><BR><BR>
	 *
	 * @param intention The new Intention to set to the AI
	 * @param arg0      The first parameter of the Intention
	 * @param arg1      The second parameter of the Intention
	 */
	@Override
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if (Config.DEBUG)
		{
			Log.info("L2SiegeAI.changeIntention(" + intention + ", " + arg0 + ", " + arg1 + ")");
		}

		if (intention ==
				AI_INTENTION_IDLE /*|| intention == AI_INTENTION_ACTIVE*/) // active becomes idle if only a summon is present
		{
			// Check if actor is not dead
			if (!this.actor.isAlikeDead())
			{
				L2Attackable npc = (L2Attackable) this.actor;

				// If its this.knownPlayer isn't empty set the Intention to AI_INTENTION_ACTIVE
				if (!npc.getKnownList().getKnownPlayers().isEmpty())
				{
					intention = AI_INTENTION_ACTIVE;
				}
				else
				{
					intention = AI_INTENTION_IDLE;
				}
			}

			if (intention == AI_INTENTION_IDLE)
			{
				// Set the Intention of this L2AttackableAI to AI_INTENTION_IDLE
				super.changeIntention(AI_INTENTION_IDLE, null, null);

				// Stop AI task and detach AI from NPC
				if (this.aiTask != null)
				{
					this.aiTask.cancel(true);
					this.aiTask = null;
				}

				// Cancel the AI
				this.accessor.detachAI();

				return;
			}
		}

		// Set the Intention of this L2AttackableAI to intention
		super.changeIntention(intention, arg0, arg1);

		// If not idle - create an AI task (schedule onEvtThink repeatedly)
		if (this.aiTask == null)
		{
			this.aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 1000);
		}
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
		this.attackTimeout = MAX_ATTACK_TIMEOUT + TimeController.getGameTicks();

		// Manage the Attack Intention : Stop current Attack (if necessary), Start a new Attack and Launch Think Event
		//if (this.actor.getTarget() != null)
		super.onIntentionAttack(target);
	}

	/**
	 * Manage AI standard thinks of a L2Attackable (called by onEvtThink).<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Update every 1s the this.globalAggro counter to come close to 0</li>
	 * <li>If the actor is Aggressive and can attack, add all autoAttackable L2Character in its Aggro Range to its this.aggroList, chose a target and order to attack it</li>
	 * <li>If the actor  can't attack, order to it to return to its home location</li>
	 */
	private void thinkActive()
	{
		L2Attackable npc = (L2Attackable) this.actor;

		// Update every 1s the this.globalAggro counter to come close to 0
		if (this.globalAggro != 0)
		{
			if (this.globalAggro < 0)
			{
				globalAggro++;
			}
			else
			{
				globalAggro--;
			}
		}

		// Add all autoAttackable L2Character in L2Attackable Aggro Range to its this.aggroList with 0 damage and 1 hate
		// A L2Attackable isn't aggressive during 10s after its spawn because this.globalAggro is set to -10
		if (this.globalAggro >= 0)
		{
			for (L2Character target : npc.getKnownList().getKnownCharactersInRadius(this.attackRange))
			{
				if (target == null)
				{
					continue;
				}
				if (autoAttackCondition(target)) // check aggression
				{
					// Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
					int hating = npc.getHating(target);

					// Add the attacker to the L2Attackable this.aggroList with 0 damage and 1 hate
					if (hating == 0)
					{
						npc.addDamageHate(target, 0, 1);
					}
				}
			}

			// Chose a target from its aggroList
			L2Character hated;
			if (this.actor.isConfused())
			{
				hated = getAttackTarget(); // Force mobs to attack anybody if confused
			}
			else
			{
				hated = npc.getMostHated();
			}
			//_mostHatedAnalysis.Update(hated);

			// Order to the L2Attackable to attack the target
			if (hated != null)
			{
				// Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
				int aggro = npc.getHating(hated);

				if (aggro + this.globalAggro > 0)
				{
					// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
					if (!this.actor.isRunning())
					{
						this.actor.setRunning();
					}

					// Set the AI Intention to AI_INTENTION_ATTACK
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated, null);
				}

				return;
			}
		}
		// Order to the L2DefenderInstance to return to its home location because there's no target to attack
		((L2DefenderInstance) this.actor).returnHome();
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
	private void thinkAttack()
	{
		if (Config.DEBUG)
		{
			Log.info("L2SiegeGuardAI.thinkAttack(); timeout=" + (this.attackTimeout - TimeController.getGameTicks()));
		}

		if (this.attackTimeout < TimeController.getGameTicks())
		{
			// Check if the actor is running
			if (this.actor.isRunning())
			{
				// Set the actor movement type to walk and send Server->Client packet ChangeMoveType to all others L2PcInstance
				this.actor.setWalking();

				// Calculate a new attack timeout
				this.attackTimeout = MAX_ATTACK_TIMEOUT + TimeController.getGameTicks();
			}
		}

		L2Character attackTarget = getAttackTarget();
		// Check if target is dead or if timeout is expired to stop this attack
		if (attackTarget == null || attackTarget.isAlikeDead() || this.attackTimeout < TimeController.getGameTicks())
		{
			// Stop hating this target after the attack timeout or if target is dead
			if (attackTarget != null)
			{
				L2Attackable npc = (L2Attackable) this.actor;
				npc.stopHating(attackTarget);
			}

			// Cancel target and timeout
			this.attackTimeout = Integer.MAX_VALUE;
			setAttackTarget(null);

			// Set the AI Intention to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE, null, null);

			this.actor.setWalking();
			return;
		}

		factionNotifyAndSupport();
		attackPrepare();
	}

	private void factionNotifyAndSupport()
	{
		L2Character target = getAttackTarget();
		// Call all L2Object of its Faction inside the Faction Range
		if (((L2Npc) this.actor).getFactionId() == null || target == null)
		{
			return;
		}

		if (target.isInvul(this.actor))
		{
			return; // speeding it up for siege guards
		}

		String faction_id = ((L2Npc) this.actor).getFactionId();

		// Go through all L2Character that belong to its faction
		//for (L2Character cha : this.actor.getKnownList().getKnownCharactersInRadius(((L2NpcInstance) this.actor).getFactionRange()+_actor.getTemplate().collisionRadius))
		for (L2Character cha : this.actor.getKnownList().getKnownCharactersInRadius(1000))
		{
			if (cha == null)
			{
				continue;
			}

			if (!(cha instanceof L2Npc))
			{
				if (this.selfAnalysis.hasHealOrResurrect && cha instanceof L2PcInstance &&
						((L2Npc) this.actor).getCastle().getSiege().checkIsDefender(((L2PcInstance) cha).getClan()))
				{
					// heal friends
					if (!this.actor.isAttackingDisabled() && cha.getCurrentHp() < cha.getMaxHp() * 0.6 &&
							this.actor.getCurrentHp() > actor.getMaxHp() / 2 &&
							this.actor.getCurrentMp() > actor.getMaxMp() / 2 && cha.isInCombat())
					{
						for (L2Skill sk : this.selfAnalysis.healSkills)
						{
							if (this.actor.getCurrentMp() < sk.getMpConsume())
							{
								continue;
							}
							if (this.actor.isSkillDisabled(sk))
							{
								continue;
							}
							if (!Util.checkIfInRange(sk.getCastRange(), this.actor, cha, true))
							{
								continue;
							}

							int chance = 5;
							if (chance >= Rnd.get(100)) // chance
							{
								continue;
							}
							if (!GeoData.getInstance().canSeeTarget(this.actor, cha))
							{
								break;
							}

							L2Object OldTarget = this.actor.getTarget();
							this.actor.setTarget(cha);
							clientStopMoving(null);
							this.accessor.doCast(sk, false);
							this.actor.setTarget(OldTarget);
							return;
						}
					}
				}
				continue;
			}

			L2Npc npc = (L2Npc) cha;

			if (!faction_id.equals(npc.getFactionId()))
			{
				continue;
			}

			if (npc.getAI() != null) // TODO: possibly check not needed
			{
				if (!npc.isDead() && Math.abs(target.getZ() - npc.getZ()) < 600
						//&& this.actor.getAttackByList().contains(getAttackTarget())
						&& (npc.getAI().intention == CtrlIntention.AI_INTENTION_IDLE ||
						npc.getAI().intention == CtrlIntention.AI_INTENTION_ACTIVE)
						//limiting aggro for siege guards
						&& target.isInsideRadius(npc, 1500, true, false) &&
						GeoData.getInstance().canSeeTarget(npc, target))
				{
					// Notify the L2Object AI with EVT_AGGRESSION
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, getAttackTarget(), 1);
					return;
				}
				// heal friends
				if (this.selfAnalysis.hasHealOrResurrect && !this.actor.isAttackingDisabled() &&
						npc.getCurrentHp() < npc.getMaxHp() * 0.6 && this.actor.getCurrentHp() > actor.getMaxHp() / 2 &&
						this.actor.getCurrentMp() > actor.getMaxMp() / 2 && npc.isInCombat())
				{
					for (L2Skill sk : this.selfAnalysis.healSkills)
					{
						if (this.actor.getCurrentMp() < sk.getMpConsume())
						{
							continue;
						}
						if (this.actor.isSkillDisabled(sk))
						{
							continue;
						}
						if (!Util.checkIfInRange(sk.getCastRange(), this.actor, npc, true))
						{
							continue;
						}

						int chance = 4;
						if (chance >= Rnd.get(100)) // chance
						{
							continue;
						}
						if (!GeoData.getInstance().canSeeTarget(this.actor, npc))
						{
							break;
						}

						L2Object OldTarget = this.actor.getTarget();
						this.actor.setTarget(npc);
						clientStopMoving(null);
						this.accessor.doCast(sk, false);
						this.actor.setTarget(OldTarget);
						return;
					}
				}
			}
		}
	}

	private void attackPrepare()
	{
		// Get all information needed to choose between physical or magical attack
		L2Skill[] skills = null;
		double dist_2 = 0;
		int range = 0;
		L2DefenderInstance sGuard = (L2DefenderInstance) this.actor;
		L2Character attackTarget = getAttackTarget();

		try
		{
			this.actor.setTarget(attackTarget);
			skills = this.actor.getAllSkills();
			dist_2 = this.actor.getPlanDistanceSq(attackTarget.getX(), attackTarget.getY());
			range = this.actor.getPhysicalAttackRange() + this.actor.getTemplate().collisionRadius +
					attackTarget.getTemplate().collisionRadius;
			if (attackTarget.isMoving())
			{
				range += 50;
			}
		}
		catch (NullPointerException e)
		{
			//Logozo.warning("AttackableAI: Attack target is NULL.");
			this.actor.setTarget(null);
			setIntention(AI_INTENTION_IDLE, null, null);
			return;
		}

		// never attack defenders
		if (attackTarget instanceof L2PcInstance &&
				sGuard.getCastle().getSiege().checkIsDefender(((L2PcInstance) attackTarget).getClan()))
		{
			// Cancel the target
			sGuard.stopHating(attackTarget);
			this.actor.setTarget(null);
			setIntention(AI_INTENTION_IDLE, null, null);
			return;
		}

		if (!GeoData.getInstance().canSeeTarget(this.actor, attackTarget))
		{
			// Siege guards differ from normal mobs currently:
			// If target cannot seen, don't attack any more
			sGuard.stopHating(attackTarget);
			this.actor.setTarget(null);
			setIntention(AI_INTENTION_IDLE, null, null);
			return;
		}

		// Check if the actor isn't muted and if it is far from target
		if (!this.actor.isMuted() && dist_2 > range * range)
		{
			// check for long ranged skills and heal/buff skills
			for (L2Skill sk : skills)
			{
				int castRange = sk.getCastRange();

				if (dist_2 <= castRange * castRange && castRange > 70 && !this.actor.isSkillDisabled(sk) &&
						this.actor.getCurrentMp() >= this.actor.getStat().getMpConsume(sk) && !sk.isPassive())
				{

					L2Object OldTarget = this.actor.getTarget();
					if (sk.getSkillType() == L2SkillType.BUFF || sk.getSkillType() == L2SkillType.HEAL)
					{
						boolean useSkillSelf = true;
						if (sk.getSkillType() == L2SkillType.HEAL &&
								this.actor.getCurrentHp() > (int) (this.actor.getMaxHp() / 1.5))
						{
							useSkillSelf = false;
							break;
						}
						if (sk.getSkillType() == L2SkillType.BUFF)
						{
							L2Abnormal[] effects = this.actor.getAllEffects();
							for (int i = 0; effects != null && i < effects.length; i++)
							{
								L2Abnormal effect = effects[i];
								if (effect.getSkill() == sk)
								{
									useSkillSelf = false;
									break;
								}
							}
						}
						if (useSkillSelf)
						{
							this.actor.setTarget(this.actor);
						}
					}

					clientStopMoving(null);
					this.accessor.doCast(sk, false);
					this.actor.setTarget(OldTarget);
					return;
				}
			}

			// Check if the L2SiegeGuardInstance is attacking, knows the target and can't run
			if (!this.actor.isAttackingNow() && this.actor.getRunSpeed() == 0 &&
					this.actor.getKnownList().knowsObject(attackTarget))
			{
				// Cancel the target
				this.actor.getKnownList().removeKnownObject(attackTarget);
				this.actor.setTarget(null);
				setIntention(AI_INTENTION_IDLE, null, null);
			}
			else
			{
				double dx = this.actor.getX() - attackTarget.getX();
				double dy = this.actor.getY() - attackTarget.getY();
				double dz = this.actor.getZ() - attackTarget.getZ();
				double homeX = attackTarget.getX() - sGuard.getSpawn().getX();
				double homeY = attackTarget.getY() - sGuard.getSpawn().getY();

				// Check if the L2SiegeGuardInstance isn't too far from it's home location
				if (dx * dx + dy * dy > 10000 && homeX * homeX + homeY * homeY > 3240000 // 1800 * 1800
						&& this.actor.getKnownList().knowsObject(attackTarget))
				{
					// Cancel the target
					this.actor.getKnownList().removeKnownObject(attackTarget);
					this.actor.setTarget(null);
					setIntention(AI_INTENTION_IDLE, null, null);
				}
				else
				// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
				{
					// Temporary hack for preventing guards jumping off towers,
					// before replacing this with effective geodata checks and AI modification
					if (dz * dz < 170 * 170) // normally 130 if guard z coordinates correct
					{
						if (this.selfAnalysis.isHealer)
						{
							return;
						}
						if (this.selfAnalysis.isMage)
						{
							range = this.selfAnalysis.maxCastRange - 50;
						}
						if (attackTarget.isMoving())
						{
							moveToPawn(attackTarget, range - 70);
						}
						else
						{
							moveToPawn(attackTarget, range);
						}
					}
				}
			}

		}
		// Else, if the actor is muted and far from target, just "move to pawn"
		else if (this.actor.isMuted() && dist_2 > range * range && !this.selfAnalysis.isHealer)
		{
			// Temporary hack for preventing guards jumping off towers,
			// before replacing this with effective geodata checks and AI modification
			double dz = this.actor.getZ() - attackTarget.getZ();
			if (dz * dz < 170 * 170) // normally 130 if guard z coordinates correct
			{
				if (this.selfAnalysis.isMage)
				{
					range = this.selfAnalysis.maxCastRange - 50;
				}
				if (attackTarget.isMoving())
				{
					moveToPawn(attackTarget, range - 70);
				}
				else
				{
					moveToPawn(attackTarget, range);
				}
			}
		}
		// Else, if this is close enough to attack
		else if (dist_2 <= range * range)
		{
			// Force mobs to attack anybody if confused
			L2Character hated = null;
			if (this.actor.isConfused())
			{
				hated = attackTarget;
			}
			else
			{
				hated = ((L2Attackable) this.actor).getMostHated();
			}

			if (hated == null)
			{
				setIntention(AI_INTENTION_ACTIVE, null, null);
				return;
			}
			if (hated != attackTarget)
			{
				attackTarget = hated;
			}

			this.attackTimeout = MAX_ATTACK_TIMEOUT + TimeController.getGameTicks();

			// check for close combat skills && heal/buff skills
			if (!this.actor.isMuted() && Rnd.nextInt(100) <= 5)
			{
				for (L2Skill sk : skills)
				{
					int castRange = sk.getCastRange();

					if (castRange * castRange >= dist_2 && !sk.isPassive() &&
							this.actor.getCurrentMp() >= this.actor.getStat().getMpConsume(sk) && !this.actor.isSkillDisabled(sk))
					{
						L2Object OldTarget = this.actor.getTarget();
						if (sk.getSkillType() == L2SkillType.BUFF || sk.getSkillType() == L2SkillType.HEAL)
						{
							boolean useSkillSelf = true;
							if (sk.getSkillType() == L2SkillType.HEAL &&
									this.actor.getCurrentHp() > (int) (this.actor.getMaxHp() / 1.5))
							{
								useSkillSelf = false;
								break;
							}
							if (sk.getSkillType() == L2SkillType.BUFF)
							{
								L2Abnormal[] effects = this.actor.getAllEffects();
								for (int i = 0; effects != null && i < effects.length; i++)
								{
									L2Abnormal effect = effects[i];
									if (effect.getSkill() == sk)
									{
										useSkillSelf = false;
										break;
									}
								}
							}
							if (useSkillSelf)
							{
								this.actor.setTarget(this.actor);
							}
						}

						clientStopMoving(null);
						this.accessor.doCast(sk, false);
						this.actor.setTarget(OldTarget);
						return;
					}
				}
			}
			// Finally, do the physical attack itself
			if (!this.selfAnalysis.isHealer)
			{
				this.accessor.doAttack(attackTarget);
			}
		}
	}

	/**
	 * Manage AI thinking actions of a L2Attackable.<BR><BR>
	 */
	@Override
	protected void onEvtThink()
	{
		//	  if (getIntention() != AI_INTENTION_IDLE && (!this.actor.isVisible() || !this.actor.hasAI() || !this.actor.isKnownPlayers()))
		//		  setIntention(AI_INTENTION_IDLE);

		// Check if the thinking action is already in progress
		if (this.thinking || this.actor.isCastingNow() || this.actor.isAllSkillsDisabled())
		{
			return;
		}

		// Start thinking action
		this.thinking = true;

		try
		{
			// Manage AI thinks of a L2Attackable
			if (getIntention() == AI_INTENTION_ACTIVE)
			{
				thinkActive();
			}
			else if (getIntention() == AI_INTENTION_ATTACK)
			{
				thinkAttack();
			}
		}
		finally
		{
			// Stop thinking action
			this.thinking = false;
		}
	}

	/**
	 * Launch actions corresponding to the Event Attacked.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Init the attack : Calculate the attack timeout, Set the this.globalAggro to 0, Add the attacker to the actor _aggroList</li>
	 * <li>Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance</li>
	 * <li>Set the Intention to AI_INTENTION_ATTACK</li><BR><BR>
	 *
	 * @param attacker The L2Character that attacks the actor
	 */
	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		// Calculate the attack timeout
		this.attackTimeout = MAX_ATTACK_TIMEOUT + TimeController.getGameTicks();

		// Set the this.globalAggro to 0 to permit attack even just after spawn
		if (this.globalAggro < 0)
		{
			this.globalAggro = 0;
		}

		// Add the attacker to the this.aggroList of the actor
		((L2Attackable) this.actor).addDamageHate(attacker, 0, 1);

		// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
		if (!this.actor.isRunning())
		{
			this.actor.setRunning();
		}

		// Set the Intention to AI_INTENTION_ATTACK
		if (getIntention() != AI_INTENTION_ATTACK)
		{
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker, null);
		}

		super.onEvtAttacked(attacker);
	}

	/**
	 * Launch actions corresponding to the Event Aggression.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Add the target to the actor this.aggroList or update hate if already present </li>
	 * <li>Set the actor Intention to AI_INTENTION_ATTACK (if actor is L2GuardInstance check if it isn't too far from its home location)</li><BR><BR>
	 *
	 * @param aggro The value of hate to add to the actor against the target
	 */
	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
		if (this.actor == null)
		{
			return;
		}
		L2Attackable me = (L2Attackable) this.actor;

		if (target != null)
		{
			// Add the target to the actor this.aggroList or update hate if already present
			me.addDamageHate(target, 0, aggro);

			// Get the hate of the actor against the target
			aggro = me.getHating(target);

			if (aggro <= 0)
			{
				if (me.getMostHated() == null)
				{
					this.globalAggro = -25;
					me.clearAggroList();
					setIntention(AI_INTENTION_IDLE, null, null);
				}
				return;
			}

			// Set the actor AI Intention to AI_INTENTION_ATTACK
			if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
				if (!this.actor.isRunning())
				{
					this.actor.setRunning();
				}

				L2DefenderInstance sGuard = (L2DefenderInstance) this.actor;
				double homeX = target.getX() - sGuard.getSpawn().getX();
				double homeY = target.getY() - sGuard.getSpawn().getY();

				// Check if the L2SiegeGuardInstance is not too far from its home location
				if (homeX * homeX + homeY * homeY < 3240000) // 1800 * 1800
				{
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
				}
			}
		}
		else
		{
			// currently only for setting lower general aggro
			if (aggro >= 0)
			{
				return;
			}

			L2Character mostHated = me.getMostHated();
			if (mostHated == null)
			{
				this.globalAggro = -25;
				return;
			}
			else
			{
				for (L2Character aggroed : me.getAggroList().keySet())
				{
					me.addDamageHate(aggroed, 0, aggro);
				}
			}

			aggro = me.getHating(mostHated);
			if (aggro <= 0)
			{
				this.globalAggro = -25;
				me.clearAggroList();
				setIntention(AI_INTENTION_IDLE, null, null);
			}
		}
	}

	@Override
	public void stopAITask()
	{
		if (this.aiTask != null)
		{
			this.aiTask.cancel(false);
			this.aiTask = null;
		}
		this.accessor.detachAI();
		super.stopAITask();
	}
}
