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
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.*;
import l2server.gameserver.model.actor.instance.DoorInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.AutoAttackStop;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.gameserver.templates.item.WeaponTemplate;
import l2server.gameserver.templates.item.WeaponType;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.gameserver.templates.skills.SkillType;
import l2server.util.Point3D;
import l2server.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static l2server.gameserver.ai.CtrlIntention.*;

/**
 * This class manages AI of Creature.<BR><BR>
 * <p>
 * CreatureAI :<BR><BR>
 * <li>L2NpcAI</li>
 * <li>DoorAI</li>
 * <li>PlayerAI</li>
 * <li>SummonAI</li><BR><BR>
 */
public class CreatureAI extends AbstractAI {
	private static Logger log = LoggerFactory.getLogger(CreatureAI.class.getName());

	public static class IntentionCommand {
		protected final CtrlIntention crtlIntention;
		protected final Object arg0, arg1;

		protected IntentionCommand(CtrlIntention pIntention, Object pArg0, Object pArg1) {
			crtlIntention = pIntention;
			arg0 = pArg0;
			arg1 = pArg1;
		}

		public CtrlIntention getCtrlIntention() {
			return crtlIntention;
		}
	}

	public CreatureAI(Creature creature) {
		super(creature);
	}

	public IntentionCommand getNextIntention() {
		return null;
	}

	@Override
	protected void onEvtAttacked(Creature attacker) {
		if (attacker instanceof Attackable && !attacker.isCoreAIDisabled()) {
			clientStartAutoAttack();
		}
	}

	/**
	 * Manage the Idle Intention : Stop Attack, Movement and Stand Up the actor.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the AI Intention to AI_INTENTION_IDLE </li>
	 * <li>Init cast and attack target </li>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast) </li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast) </li>
	 * <li>Stand up the actor server side AND client side by sending Server->Client packet ChangeWaitType (broadcast) </li><BR><BR>
	 */
	@Override
	protected void onIntentionIdle() {
		// Set the AI Intention to AI_INTENTION_IDLE
		changeIntention(AI_INTENTION_IDLE, null, null);

		// Init cast and attack target
		setCastTarget(null);
		setAttackTarget(null);

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);

		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
	}

	/**
	 * Manage the Active Intention : Stop Attack, Movement and Launch Think Event.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> : <I>if the Intention is not already Active</I></B><BR><BR>
	 * <li>Set the AI Intention to AI_INTENTION_ACTIVE </li>
	 * <li>Init cast and attack target </li>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast) </li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast) </li>
	 * <li>Launch the Think Event </li><BR><BR>
	 */
	@Override
	protected void onIntentionActive() {
		// Check if the Intention is not already Active
		if (getIntention() != AI_INTENTION_ACTIVE) {
			// Set the AI Intention to AI_INTENTION_ACTIVE
			changeIntention(AI_INTENTION_ACTIVE, null, null);

			// Init cast and attack target
			setCastTarget(null);
			setAttackTarget(null);

			// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
			clientStopMoving(null);

			// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
			clientStopAutoAttack();

			// Also enable random animations for this Creature if allowed
			// This is only for mobs - town npcs are handled in their constructor
			if (actor instanceof Attackable) {
				((Npc) actor).startRandomAnimationTimer();
			}

			// Launch the Think Event
			onEvtThink();
		}
	}

	/**
	 * Manage the Rest Intention.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Set the AI Intention to AI_INTENTION_IDLE </li><BR><BR>
	 */
	@Override
	protected void onIntentionRest() {
		// Set the AI Intention to AI_INTENTION_IDLE
		setIntention(AI_INTENTION_IDLE);
	}

	/**
	 * Manage the Attack Intention : Stop current Attack (if necessary), Start a new Attack and Launch Think Event.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast) </li>
	 * <li>Set the Intention of this AI to AI_INTENTION_ATTACK </li>
	 * <li>Set or change the AI attack target </li>
	 * <li>Start the actor Auto Attack client side by sending Server->Client packet AutoAttackStart (broadcast) </li>
	 * <li>Launch the Think Event </li><BR><BR>
	 * <p>
	 * <p>
	 * <B><U> Overridden in</U> :</B><BR><BR>
	 * <li>AttackableAI : Calculate attack timeout</li><BR><BR>
	 */
	@Override
	protected void onIntentionAttack(Creature target) {
		if (target == null) {
			clientActionFailed();
			return;
		}

		if (getIntention() == AI_INTENTION_REST) {
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}

		if (actor.isAllSkillsDisabled() || actor.isCastingNow() || actor.isAfraid() || actor.isInLove()) {
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}

		// Check if the Intention is already AI_INTENTION_ATTACK
		if (getIntention() == AI_INTENTION_ATTACK) {
			// Check if the AI already targets the Creature
			if (getAttackTarget() != target) {
				// Set the AI attack target (change target)
				setAttackTarget(target);

				stopFollow();

				// Launch the Think Event
				notifyEvent(CtrlEvent.EVT_THINK, null);
			} else {
				clientActionFailed(); // else client freezes until cancel target
			}
		} else {
			// Set the Intention of this AbstractAI to AI_INTENTION_ATTACK
			changeIntention(AI_INTENTION_ATTACK, target, null);

			// Set the AI attack target
			setAttackTarget(target);

			stopFollow();

			// Launch the Think Event
			notifyEvent(CtrlEvent.EVT_THINK, null);
		}
	}

	/**
	 * Manage the Cast Intention : Stop current Attack, Init the AI in order to cast and Launch Think Event.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Set the AI cast target </li>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast) </li>
	 * <li>Cancel action client side by sending Server->Client packet ActionFailed to the Player actor </li>
	 * <li>Set the AI skill used by INTENTION_CAST </li>
	 * <li>Set the Intention of this AI to AI_INTENTION_CAST </li>
	 * <li>Launch the Think Event </li><BR><BR>
	 */
	@Override
	protected void onIntentionCast(Skill skill, WorldObject target) {
		if (getIntention() == AI_INTENTION_REST && skill.isMagic()) {
			clientActionFailed();
			actor.setCastingNow(false);
			return;
		}

		// Set the AI cast target
		setCastTarget((Creature) target);

		// Stop actions client-side to cast the skill
		if (skill.getHitTime() > 50) {
			// Abort the attack of the Creature and send Server->Client ActionFailed packet
			actor.abortAttack();

			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			// no need for second ActionFailed packet, abortAttack() already sent it
			//clientActionFailed();
		}

		// Set the AI skill used by INTENTION_CAST
		this.skill = skill;

		// Change the Intention of this AbstractAI to AI_INTENTION_CAST
		changeIntention(AI_INTENTION_CAST, skill, target);

		// Launch the Think Event
		notifyEvent(CtrlEvent.EVT_THINK, null);
	}

	/**
	 * Manage the Move To Intention : Stop current Attack and Launch a Move to Location Task.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Stop the actor auto-attack server side AND client side by sending Server->Client packet AutoAttackStop (broadcast) </li>
	 * <li>Set the Intention of this AI to AI_INTENTION_MOVE_TO </li>
	 * <li>Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast) </li><BR><BR>
	 */
	@Override
	protected void onIntentionMoveTo(L2CharPosition pos) {
		if (getIntention() == AI_INTENTION_REST) {
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}

		if (actor.isAllSkillsDisabled() || actor.isCastingNow()) {
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}

		// Set the Intention of this AbstractAI to AI_INTENTION_MOVE_TO
		changeIntention(AI_INTENTION_MOVE_TO, pos, null);

		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();

		// Abort the attack of the Creature and send Server->Client ActionFailed packet
		actor.abortAttack();

		// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
		moveTo(pos.x, pos.y, pos.z);
	}

	/**
	 * Manage the Follow Intention : Stop current Attack and Launch a Follow Task.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Stop the actor auto-attack server side AND client side by sending Server->Client packet AutoAttackStop (broadcast) </li>
	 * <li>Set the Intention of this AI to AI_INTENTION_FOLLOW </li>
	 * <li>Create and Launch an AI Follow Task to execute every 1s </li><BR><BR>
	 */
	@Override
	protected void onIntentionFollow(Creature target) {
		if (getIntention() == AI_INTENTION_REST) {
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}

		if (actor.isAllSkillsDisabled() || actor.isCastingNow()) {
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}

		if (actor.isMovementDisabled()) {
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}

		// Dead actors can`t follow
		if (actor.isDead()) {
			clientActionFailed();
			return;
		}

		// do not follow yourself
		if (actor == target) {
			clientActionFailed();
			return;
		}

		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();

		// Set the Intention of this AbstractAI to AI_INTENTION_FOLLOW
		changeIntention(AI_INTENTION_FOLLOW, target, null);

		// Create and Launch an AI Follow Task to execute every 1s
		startFollow(target);
	}

	/**
	 * Manage the PickUp Intention : Set the pick up target and Launch a Move To Pawn Task (offset=20).<BR><BR>
	 * <p>
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Set the AI pick up target </li>
	 * <li>Set the Intention of this AI to AI_INTENTION_PICK_UP </li>
	 * <li>Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast) </li><BR><BR>
	 */
	@Override
	protected void onIntentionPickUp(WorldObject object) {
		if (getIntention() == AI_INTENTION_REST) {
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}

		if (actor.isAllSkillsDisabled() || actor.isCastingNow()) {
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}

		if (actor instanceof Player && !((Player) actor).getFloodProtectors().getPickUpItem().tryPerformAction("PickUpItem")) {
			if (((Player) actor).getClient() != null) {
				clientActionFailed();
			}
			return;
		}

		//All kind of summons, pets
		if (actor instanceof Summon && !((Summon) actor).getOwner().getFloodProtectors().getPickUpItem().tryPerformAction("PickUpItem")) {
			return;
		}

		if (actor.isImmobilized() || actor.isAllSkillsDisabled()) {
			if (actor instanceof Player) {
				clientActionFailed();
			}
			return;
		}

		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();

		// Set the Intention of this AbstractAI to AI_INTENTION_PICK_UP
		changeIntention(AI_INTENTION_PICK_UP, object, null);

		// Set the AI pick up target
		setTarget(object);
		if (object.getX() == 0 && object.getY() == 0) // TODO: Find the drop&spawn bug
		{
			log.warn("Object in coords 0,0 - using a temporary fix");
			object.setXYZ(getActor().getX(), getActor().getY(), getActor().getZ() + 5);
		}

		// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
		moveToPawn(object, 20);
	}

	/**
	 * Manage the Interact Intention : Set the interact target and Launch a Move To Pawn Task (offset=60).<BR><BR>
	 * <p>
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast) </li>
	 * <li>Set the AI interact target </li>
	 * <li>Set the Intention of this AI to AI_INTENTION_INTERACT </li>
	 * <li>Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast) </li><BR><BR>
	 */
	@Override
	protected void onIntentionInteract(WorldObject object) {
		if (getIntention() == AI_INTENTION_REST) {
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}

		if (actor.isAllSkillsDisabled() || actor.isCastingNow()) {
			// Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
			clientActionFailed();
			return;
		}

		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();

		if (getIntention() != AI_INTENTION_INTERACT) {
			// Set the Intention of this AbstractAI to AI_INTENTION_INTERACT
			changeIntention(AI_INTENTION_INTERACT, object, null);

			// Set the AI interact target
			setTarget(object);

			// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
			moveToPawn(object, 60);
		}
	}

	/**
	 * Do nothing.<BR><BR>
	 */
	@Override
	protected void onEvtThink() {
		// do nothing
	}

	/**
	 * Do nothing.<BR><BR>
	 */
	@Override
	protected void onEvtAggression(Creature target, int aggro) {
		// do nothing
	}

	/**
	 * Launch actions corresponding to the Event Stunned then onAttacked Event.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Break an attack and send Server->Client ActionFailed packet and a System Message to the Creature </li>
	 * <li>Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature </li>
	 * <li>Launch actions corresponding to the Event onAttacked (only for AttackableAI after the stunning periode) </li><BR><BR>
	 */
	@Override
	protected void onEvtStunned(Creature attacker) {
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		actor.broadcastPacket(new AutoAttackStop(actor.getObjectId()));
		if (AttackStanceTaskManager.getInstance().getAttackStanceTask(actor)) {
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(actor);
		}

		// Stop Server AutoAttack also
		setAutoAttacking(false);

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);

		// Launch actions corresponding to the Event onAttacked (only for AttackableAI after the stunning periode)
		onEvtAttacked(attacker);
	}

	@Override
	protected void onEvtParalyzed(Creature attacker) {
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		actor.broadcastPacket(new AutoAttackStop(actor.getObjectId()));
		if (AttackStanceTaskManager.getInstance().getAttackStanceTask(actor)) {
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(actor);
		}

		// Stop Server AutoAttack also
		setAutoAttacking(false);

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);

		// Launch actions corresponding to the Event onAttacked (only for AttackableAI after the stunning periode)
		onEvtAttacked(attacker);
	}

	/**
	 * Launch actions corresponding to the Event Sleeping.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Break an attack and send Server->Client ActionFailed packet and a System Message to the Creature </li>
	 * <li>Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature </li><BR><BR>
	 */
	@Override
	protected void onEvtSleeping(Creature attacker) {
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		actor.broadcastPacket(new AutoAttackStop(actor.getObjectId()));
		if (AttackStanceTaskManager.getInstance().getAttackStanceTask(actor)) {
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(actor);
		}

		// stop Server AutoAttack also
		setAutoAttacking(false);

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
	}

	/**
	 * Launch actions corresponding to the Event Rooted.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Launch actions corresponding to the Event onAttacked</li><BR><BR>
	 */
	@Override
	protected void onEvtRooted(Creature attacker) {
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		//actor.broadcastPacket(new AutoAttackStop(actor.getObjectId()));
		//if (AttackStanceTaskManager.getInstance().getAttackStanceTask(actor))
		//	AttackStanceTaskManager.getInstance().removeAttackStanceTask(actor);

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);

		// Launch actions corresponding to the Event onAttacked
		onEvtAttacked(attacker);
	}

	/**
	 * Launch actions corresponding to the Event Confused.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Launch actions corresponding to the Event onAttacked</li><BR><BR>
	 */
	@Override
	protected void onEvtConfused(Creature attacker) {
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);

		// Launch actions corresponding to the Event onAttacked
		onEvtAttacked(attacker);
	}

	/**
	 * Launch actions corresponding to the Event Muted.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature </li><BR><BR>
	 */
	@Override
	protected void onEvtMuted(Creature attacker) {
		// Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature
		onEvtAttacked(attacker);
	}

	/**
	 * Do nothing.<BR><BR>
	 */
	@Override
	protected void onEvtEvaded(Creature attacker) {
		// do nothing
	}

	/**
	 * Launch actions corresponding to the Event ReadyToAct.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Launch actions corresponding to the Event Think</li><BR><BR>
	 */
	@Override
	protected void onEvtReadyToAct() {
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}

	/**
	 * Do nothing.<BR><BR>
	 */
	@Override
	protected void onEvtUserCmd(Object arg0, Object arg1) {
		// do nothing
	}

	/**
	 * Launch actions corresponding to the Event Arrived.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the Intention was AI_INTENTION_MOVE_TO, set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>Launch actions corresponding to the Event Think</li><BR><BR>
	 */
	@Override
	protected void onEvtArrived() {
		actor.revalidateZone(true);

		if (actor.moveToNextRoutePoint()) {
			return;
		}

		if (actor instanceof Attackable) {
			((Attackable) actor).setisReturningToSpawnPoint(false);
		}
		clientStoppedMoving();

		// If the Intention was AI_INTENTION_MOVE_TO, set the Intention to AI_INTENTION_ACTIVE
		if (getIntention() == AI_INTENTION_MOVE_TO) {
			setIntention(AI_INTENTION_ACTIVE);
		}

		// Launch actions corresponding to the Event Think
		onEvtThink();
	}

	/**
	 * Launch actions corresponding to the Event ArrivedRevalidate.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Launch actions corresponding to the Event Think</li><BR><BR>
	 */
	@Override
	protected void onEvtArrivedRevalidate() {
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}

	/**
	 * Launch actions corresponding to the Event ArrivedBlocked.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>If the Intention was AI_INTENTION_MOVE_TO, set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>Launch actions corresponding to the Event Think</li><BR><BR>
	 */
	@Override
	protected void onEvtArrivedBlocked(L2CharPosition blocked_at_pos) {
		// If the Intention was AI_INTENTION_MOVE_TO, set the Intention to AI_INTENTION_ACTIVE
		if (getIntention() == AI_INTENTION_MOVE_TO || getIntention() == AI_INTENTION_CAST) {
			setIntention(AI_INTENTION_ACTIVE);
		}

		if (getIntention() == AI_INTENTION_ATTACK) {
			setAttackTarget(null);
			return;
		}

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(blocked_at_pos);

		/*if (Config.ACTIVATE_POSITION_RECORDER && Universe.getInstance().shouldLog(actor.getObjectId()))
		{
			if (!actor.isFlying())
				Universe.getInstance().registerObstacle(blocked_at_pos.x, blocked_at_pos.y, blocked_at_pos.z);
			if (actor instanceof Player)
				((Player) actor).explore();
		}*/

		// Launch actions corresponding to the Event Think
		onEvtThink();
	}

	/**
	 * Launch actions corresponding to the Event ForgetObject.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the object was targeted  and the Intention was AI_INTENTION_INTERACT or AI_INTENTION_PICK_UP, set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>If the object was targeted to attack, stop the auto-attack, cancel target and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>If the object was targeted to cast, cancel target and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>If the object was targeted to follow, stop the movement, cancel AI Follow Task and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>If the targeted object was the actor , cancel AI target, stop AI Follow Task, stop the movement and set the Intention to AI_INTENTION_IDLE </li><BR><BR>
	 */
	@Override
	protected void onEvtForgetObject(WorldObject object) {
		// If the object was targeted  and the Intention was AI_INTENTION_INTERACT or AI_INTENTION_PICK_UP, set the Intention to AI_INTENTION_ACTIVE
		if (getTarget() == object) {
			setTarget(null);

			if (getIntention() == AI_INTENTION_INTERACT) {
				setIntention(AI_INTENTION_ACTIVE);
			} else if (getIntention() == AI_INTENTION_PICK_UP) {
				setIntention(AI_INTENTION_ACTIVE);
			}
		}

		// Check if the object was targeted to attack
		if (getAttackTarget() == object) {
			// Cancel attack target
			setAttackTarget(null);

			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
		}

		// Check if the object was targeted to cast
		if (getCastTarget() == object) {
			// Cancel cast target
			setCastTarget(null);

			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
		}

		// Check if the object was targeted to follow
		if (getFollowTarget() == object) {
			// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
			clientStopMoving(null);

			// Stop an AI Follow Task
			stopFollow();

			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
		}

		// Check if the targeted object was the actor
		if (actor == object) {
			// Cancel AI target
			setTarget(null);
			setAttackTarget(null);
			setCastTarget(null);

			// Stop an AI Follow Task
			stopFollow();

			// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
			clientStopMoving(null);

			// Set the Intention of this AbstractAI to AI_INTENTION_IDLE
			changeIntention(AI_INTENTION_IDLE, null, null);
		}
	}

	/**
	 * Launch actions corresponding to the Event Cancel.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop an AI Follow Task</li>
	 * <li>Launch actions corresponding to the Event Think</li><BR><BR>
	 */
	@Override
	protected void onEvtCancel() {
		actor.abortCast();

		// Stop an AI Follow Task
		stopFollow();

		if (!AttackStanceTaskManager.getInstance().getAttackStanceTask(actor)) {
			actor.broadcastPacket(new AutoAttackStop(actor.getObjectId()));
		}

		// Launch actions corresponding to the Event Think
		onEvtThink();
	}

	/**
	 * Launch actions corresponding to the Event Dead.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop an AI Follow Task</li>
	 * <li>Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die (broadcast)</li><BR><BR>
	 */
	@Override
	protected void onEvtDead() {
		// Stop an AI Tasks
		stopAITask();

		// Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die (broadcast)
		clientNotifyDead();

		if (!(actor instanceof Playable)) {
			actor.setWalking();
		}
	}

	/**
	 * Launch actions corresponding to the Event Fake Death.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop an AI Follow Task</li>
	 */
	@Override
	protected void onEvtFakeDeath() {
		// Stop an AI Follow Task
		stopFollow();

		// Stop the actor movement and send Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);

		// Init AI
		intention = AI_INTENTION_IDLE;
		setTarget(null);
		setCastTarget(null);
		setAttackTarget(null);
	}

	/**
	 * Do nothing.<BR><BR>
	 */
	@Override
	protected void onEvtFinishCasting() {
		// do nothing
	}

	protected boolean maybeMoveToPosition(Point3D worldPosition, int offset) {
		if (worldPosition == null) {
			log.warn("maybeMoveToPosition: worldPosition == NULL!");
			return false;
		}

		if (offset < 0) {
			return false; // skill radius -1
		}

		if (!actor.isInsideRadius(worldPosition.getX(), worldPosition.getY(), offset + actor.getTemplate().getCollisionRadius(), false)) {
			if (actor.isMovementDisabled()) {
				return true;
			}

			if (!actor.isRunning() && !(this instanceof PlayerAI) && !(this instanceof SummonAI)) {
				actor.setRunning();
			}

			stopFollow();

			int x = actor.getX();
			int y = actor.getY();

			double dx = worldPosition.getX() - x;
			double dy = worldPosition.getY() - y;

			double dist = Math.sqrt(dx * dx + dy * dy);

			double sin = dy / dist;
			double cos = dx / dist;

			dist -= offset - 5;

			x += (int) (dist * cos);
			y += (int) (dist * sin);

			moveTo(x, y, worldPosition.getZ());
			return true;
		}

		if (getFollowTarget() != null) {
			stopFollow();
		}

		return false;
	}

	/**
	 * Manage the Move to Pawn action in function of the distance and of the Interact area.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the distance between the current position of the Creature and the target (x,y)</li>
	 * <li>If the distance > offset+20, move the actor (by running) to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)</li>
	 * <li>If the distance <= offset+20, Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li><BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> L2PLayerAI, SummonAI</li><BR><BR>
	 *
	 * @param target The targeted WorldObject
	 * @param offset The Interact area radius
	 * @return True if a movement must be done
	 */
	protected boolean maybeMoveToPawn(WorldObject target, int offset) {
		// Get the distance between the current position of the Creature and the target (x,y)
		if (target == null) {
			log.warn("maybeMoveToPawn: target == NULL!");
			return false;
		}
		if (offset < 0) {
			return false; // skill radius -1
		}

		offset += actor.getTemplate().getCollisionRadius();
		if (target instanceof Creature) {
			offset += ((Creature) target).getTemplate().getCollisionRadius();
		}

		if (!actor.isInsideRadius(target, offset, false, false)) {
			// Caller should be Playable and thinkAttack/thinkCast/thinkInteract/thinkPickUp
			if (getFollowTarget() != null) {

				// allow larger hit range when the target is moving (check is run only once per second)
				if (!actor.isInsideRadius(target, offset + 100, false, false)) {
					return true;
				}
				stopFollow();
				return false;
			}

			if (actor.isMovementDisabled()) {
				// If player is trying attack target but he cannot move to attack target
				// change his intention to idle
				if (actor.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK) {
					actor.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				}

				return true;
			}

			// while flying there is no move to cast
			if (actor.getAI().getIntention() == CtrlIntention.AI_INTENTION_CAST && actor instanceof Player && actor.isTransformed()) {
				if (!((Player) actor).getTransformation().canStartFollowToCast()) {
					actor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED));
					actor.sendPacket(ActionFailed.STATIC_PACKET);

					return true;
				}
			}

			// If not running, set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others Player
			if (!actor.isRunning() && !(this instanceof PlayerAI) && !(this instanceof SummonAI)) {
				actor.setRunning();
			}

			stopFollow();
			if (target instanceof Creature && !(target instanceof DoorInstance)) {
				if (((Creature) target).isMoving()) {
					offset -= 100;
				}
				if (offset < 5) {
					offset = 5;
				}

				startFollow((Creature) target, offset);
			} else {
				// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
				moveToPawn(target, offset);
			}
			return true;
		}

		if (getFollowTarget() != null) {
			stopFollow();
		}

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		// clientStopMoving(null);
		return false;
	}

	/**
	 * Modify current Intention and actions if the target is lost or dead.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> : <I>If the target is lost or dead</I></B><BR><BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE</li><BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> L2PLayerAI, SummonAI</li><BR><BR>
	 *
	 * @param target The targeted WorldObject
	 * @return True if the target is lost or dead (false if fakedeath)
	 */
	protected boolean checkTargetLostOrDead(Creature target) {
		if (target == null || target.isAlikeDead()) {
			//check if player is fakedeath
			if (target instanceof Player && ((Player) target).isFakeDeath()) {
				target.stopFakeDeath(true);
				return false;
			}

			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
			return true;
		}
		return false;
	}

	/**
	 * Modify current Intention and actions if the target is lost.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> : <I>If the target is lost</I></B><BR><BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE</li><BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> L2PLayerAI, SummonAI</li><BR><BR>
	 *
	 * @param target The targeted WorldObject
	 * @return True if the target is lost
	 */
	protected boolean checkTargetLost(WorldObject target) {
		// check if player is fakedeath
		if (target instanceof Player) {
			Player target2 = (Player) target; //convert object to chara

			if (target2.isFakeDeath()) {
				target2.stopFakeDeath(true);
				return false;
			}
		}
		if (target == null) {
			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
			return true;
		}
		if (actor != null && skill != null && skill.isOffensive() && skill.getSkillRadius() > 0 && Config.GEODATA > 0 &&
				!GeoData.getInstance().canSeeTarget(actor, target)) {
			setIntention(AI_INTENTION_ACTIVE);
			return true;
		}
		return false;
	}

	protected class SelfAnalysis {
		public boolean isMage = false;
		public boolean isBalanced;
		public boolean isArcher = false;
		public boolean isHealer = false;
		public boolean isFighter = false;
		public boolean cannotMoveOnLand = false;
		public List<Skill> generalSkills = new ArrayList<>();
		public List<Skill> buffSkills = new ArrayList<>();
		public int lastBuffTick = 0;
		public List<Skill> debuffSkills = new ArrayList<>();
		public int lastDebuffTick = 0;
		public List<Skill> cancelSkills = new ArrayList<>();
		public List<Skill> healSkills = new ArrayList<>();
		//public List<Skill> trickSkills = new ArrayList<Skill>();
		public List<Skill> generalDisablers = new ArrayList<>();
		public List<Skill> sleepSkills = new ArrayList<>();
		public List<Skill> rootSkills = new ArrayList<>();
		public List<Skill> muteSkills = new ArrayList<>();
		public List<Skill> resurrectSkills = new ArrayList<>();
		public boolean hasHealOrResurrect = false;
		public boolean hasLongRangeSkills = false;
		public boolean hasLongRangeDamageSkills = false;
		public int maxCastRange = 0;

		public SelfAnalysis() {
		}

		public void init() {
			switch (((NpcTemplate) actor.getTemplate()).getAIData().getAiType()) {
				case FIGHTER:
					isFighter = true;
					break;
				case MAGE:
					isMage = true;
					break;
				case CORPSE:
				case BALANCED:
					isBalanced = true;
					break;
				case ARCHER:
					isArcher = true;
					break;
				case HEALER:
					isHealer = true;
					break;
				default:
					isFighter = true;
					break;
			}
			// water movement analysis
			if (actor instanceof Npc) {
				int npcId = ((Npc) actor).getNpcId();

				switch (npcId) {
					case 20314: // great white shark
					case 20849: // Light Worm
						cannotMoveOnLand = true;
						break;
					default:
						cannotMoveOnLand = false;
						break;
				}
			}
			// skill analysis
			for (Skill sk : actor.getAllSkills()) {
				if (sk.isPassive()) {
					continue;
				}
				int castRange = sk.getCastRange();
				boolean hasLongRangeDamageSkill = false;
				switch (sk.getSkillType()) {
					case HEAL:
					case HEAL_PERCENT:
					case HEAL_STATIC:
					case BALANCE_LIFE:
						//case HOT:
						healSkills.add(sk);
						hasHealOrResurrect = true;
						continue; // won't be considered something for fighting
					case BUFF:
						buffSkills.add(sk);
						continue; // won't be considered something for fighting
                        /*case PARALYZE:
						case STUN:
							// hardcoding petrification until improvements are made to
							// EffectTemplate... petrification is totally different for
							// AI than paralyze
							switch (sk.getId())
							{
								case 367:
								case 4111:
								case 4383:
								case 4616:
								case 4578:
									sleepSkills.add(sk);
									break;
								default:
									generalDisablers.add(sk);
									break;
							}
							break;
						case MUTE:
							muteSkills.add(sk);
							break;
						case SLEEP:
							sleepSkills.add(sk);
							break;
						case ROOT:
							rootSkills.add(sk);
							break;
						case FEAR: // could be used as an alternative for healing?
						case CONFUSION:
							//  trickSkills.add(sk);*/
					case DEBUFF:
						debuffSkills.add(sk);
						break;
					case CANCEL:
					case NEGATE:
						cancelSkills.add(sk);
						break;
					case RESURRECT:
						resurrectSkills.add(sk);
						hasHealOrResurrect = true;
						break;
					case NOTDONE:
					case COREDONE:
						continue; // won't be considered something for fighting
					default:
						generalSkills.add(sk);
						hasLongRangeDamageSkill = true;
						break;
				}
				if (castRange > 70) {
					hasLongRangeSkills = true;
					if (hasLongRangeDamageSkill) {
						hasLongRangeDamageSkills = true;
					}
				}
				if (castRange > maxCastRange) {
					maxCastRange = castRange;
				}
			}
			// Because of missing skills, some mages/balanced cannot play like mages
			if (!hasLongRangeDamageSkills && isMage) {
				isBalanced = true;
				isMage = false;
				isFighter = false;
			}
			if (!hasLongRangeSkills && (isMage || isBalanced)) {
				isBalanced = false;
				isMage = false;
				isFighter = true;
			}
			if (generalSkills.isEmpty() && isMage) {
				isBalanced = true;
				isMage = false;
			}
		}
	}

	protected class TargetAnalysis {
		public Creature character;
		public boolean isMage;
		public boolean isBalanced;
		public boolean isArcher;
		public boolean isFighter;
		public boolean isCanceled;
		public boolean isSlower;
		public boolean isMagicResistant;

		public TargetAnalysis() {
		}

		public void update(Creature target) {
			// update status once in 4 seconds
			if (target == character && Rnd.nextInt(100) > 25) {
				return;
			}
			character = target;
			if (target == null) {
				return;
			}
			isMage = false;
			isBalanced = false;
			isArcher = false;
			isFighter = false;
			isCanceled = false;

			if (target.getMAtk(null, null) > 1.5 * target.getPAtk(null)) {
				isMage = true;
			} else if (target.getPAtk(null) * 0.8 < target.getMAtk(null, null) || target.getMAtk(null, null) * 0.8 > target.getPAtk(null)) {
				isBalanced = true;
			} else {
				WeaponTemplate weapon = target.getActiveWeaponItem();
				if (weapon != null && (weapon.getItemType() == WeaponType.BOW || weapon.getItemType() == WeaponType.CROSSBOW ||
						weapon.getItemType() == WeaponType.CROSSBOWK)) {
					isArcher = true;
				} else {
					isFighter = true;
				}
			}
			isSlower = target.getRunSpeed() < actor.getRunSpeed() - 3;
			isMagicResistant = target.getMDef(null, null) * 1.2 > actor.getMAtk(null, null);
			if (target.getBuffCount() < 4) {
				isCanceled = true;
			}
		}
	}

	public boolean canAura(Skill sk) {
		if (sk.getTargetType() == SkillTargetType.TARGET_AURA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA ||
				sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA) {
			for (WorldObject target : actor.getKnownList().getKnownCharactersInRadius(sk.getSkillRadius())) {
				if (target == getAttackTarget()) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean canAOE(Skill sk) {
		if (sk.getSkillType() != SkillType.NEGATE || sk.getSkillType() != SkillType.CANCEL) {
			if (sk.getTargetType() == SkillTargetType.TARGET_AURA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA ||
					sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA) {
				boolean cancast = true;
				for (Creature target : actor.getKnownList().getKnownCharactersInRadius(sk.getSkillRadius())) {
					if (!GeoData.getInstance().canSeeTarget(actor, target)) {
						continue;
					}
					if (target instanceof Attackable) {
						Npc targets = (Npc) target;
						Npc actors = (Npc) actor;

						if (targets.getEnemyClan() == null || actors.getClan() == null || !targets.getEnemyClan().equals(actors.getClan()) ||
								actors.getClan() == null && actors.getIsChaos() == 0) {
							continue;
						}
					}
					Abnormal[] effects = target.getAllEffects();
					for (int i = 0; effects != null && i < effects.length; i++) {
						Abnormal effect = effects[i];
						if (effect.getSkill() == sk) {
							cancast = false;
							break;
						}
					}
				}
				if (cancast) {
					return true;
				}
			} else if (sk.getTargetType() == SkillTargetType.TARGET_AREA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AREA ||
					sk.getTargetType() == SkillTargetType.TARGET_FRONT_AREA) {
				boolean cancast = true;
				for (Creature target : getAttackTarget().getKnownList().getKnownCharactersInRadius(sk.getSkillRadius())) {
					if (!GeoData.getInstance().canSeeTarget(actor, target) || target == null) {
						continue;
					}
					if (target instanceof Attackable) {
						Npc targets = (Npc) target;
						Npc actors = (Npc) actor;
						if (targets.getEnemyClan() == null || actors.getClan() == null || !targets.getEnemyClan().equals(actors.getClan()) ||
								actors.getClan() == null && actors.getIsChaos() == 0) {
							continue;
						}
					}
					Abnormal[] effects = target.getAllEffects();
					if (effects.length > 0) {
						cancast = true;
					}
				}
				if (cancast) {
					return true;
				}
			}
		} else {
			if (sk.getTargetType() == SkillTargetType.TARGET_AURA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA ||
					sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA) {
				boolean cancast = false;
				for (Creature target : actor.getKnownList().getKnownCharactersInRadius(sk.getSkillRadius())) {
					if (!GeoData.getInstance().canSeeTarget(actor, target)) {
						continue;
					}
					if (target instanceof Attackable) {
						Npc targets = (Npc) target;
						Npc actors = (Npc) actor;
						if (targets.getEnemyClan() == null || actors.getClan() == null || !targets.getEnemyClan().equals(actors.getClan()) ||
								actors.getClan() == null && actors.getIsChaos() == 0) {
							continue;
						}
					}
					Abnormal[] effects = target.getAllEffects();
					if (effects.length > 0) {
						cancast = true;
					}
				}
				if (cancast) {
					return true;
				}
			} else if (sk.getTargetType() == SkillTargetType.TARGET_AREA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AREA ||
					sk.getTargetType() == SkillTargetType.TARGET_FRONT_AREA) {
				boolean cancast = true;
				for (Creature target : getAttackTarget().getKnownList().getKnownCharactersInRadius(sk.getSkillRadius())) {
					if (!GeoData.getInstance().canSeeTarget(actor, target)) {
						continue;
					}
					if (target instanceof Attackable) {
						Npc targets = (Npc) target;
						Npc actors = (Npc) actor;
						if (targets.getEnemyClan() == null || actors.getClan() == null || !targets.getEnemyClan().equals(actors.getClan()) ||
								actors.getClan() == null && actors.getIsChaos() == 0) {
							continue;
						}
					}
					Abnormal[] effects = target.getAllEffects();
					for (int i = 0; effects != null && i < effects.length; i++) {
						Abnormal effect = effects[i];
						if (effect.getSkill() == sk) {
							cancast = false;
							break;
						}
					}
				}
				if (cancast) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean canParty(Skill sk) {
		if (sk.getTargetType() == SkillTargetType.TARGET_PARTY) {
			int count = 0;
			int ccount = 0;
			for (Creature target : actor.getKnownList().getKnownCharactersInRadius(sk.getSkillRadius())) {
				if (!(target instanceof Attackable) || !GeoData.getInstance().canSeeTarget(actor, target)) {
					continue;
				}
				Npc targets = (Npc) target;
				Npc actors = (Npc) actor;
				if (actors.getFactionId() != null && targets.getFactionId().equals(actors.getFactionId())) {
					count++;
					Abnormal[] effects = target.getAllEffects();
					for (int i = 0; effects != null && i < effects.length; i++) {

						Abnormal effect = effects[i];
						if (effect.getSkill() == sk) {
							ccount++;
							break;
						}
					}
				}
			}
			if (ccount < count) {
				return true;
			}
		}
		return false;
	}

	public boolean isParty(Skill sk) {
		return sk.getTargetType() == SkillTargetType.TARGET_PARTY;
	}
}
