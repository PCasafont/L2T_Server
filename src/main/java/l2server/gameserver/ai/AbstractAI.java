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

import l2server.gameserver.GeoData;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.TimeController;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.GrandBossInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.SummonInstance;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;
import l2server.util.Point3D;
import l2server.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

import static l2server.gameserver.ai.CtrlIntention.*;

/**
 * Mother class of all objects AI in the world.<BR><BR>
 * <p>
 * AbastractAI :<BR><BR>
 * <li>CreatureAI</li><BR><BR>
 */
abstract class AbstractAI implements Ctrl {
	private static Logger log = LoggerFactory.getLogger(AbstractAI.class.getName());

	class FollowTask implements Runnable {
		protected int range = 70;

		public FollowTask() {
		}

		public FollowTask(int range) {
			this.range = range;
		}

		@Override
		public void run() {
			try {
				if (followTask == null) {
					return;
				}

				Creature followTarget = AbstractAI.this.followTarget; // copy to prevent NPE
				if (followTarget == null) {
					if (actor instanceof Summon) {
						((Summon) actor).setFollowStatus(false);
					}
					setIntention(AI_INTENTION_IDLE);
					return;
				}

				if (!actor.isInsideRadius(followTarget, range, true, false)) {
					if (!actor.isInsideRadius(followTarget, 3000, true, false)) {
						// if the target is too far (maybe also teleported)
						if (actor instanceof Summon) {
							((Summon) actor).setFollowStatus(false);
						}

						setIntention(AI_INTENTION_IDLE);
						return;
					}

					moveToPawn(followTarget, range);
				}
			} catch (Exception e) {
				log.warn("", e);
			}
		}
	}

	/**
	 * The character that this AI manages
	 */
	protected final Creature actor;

	/**
	 * Current long-term intention
	 */
	protected CtrlIntention intention = AI_INTENTION_IDLE;
	/**
	 * Current long-term intention parameter
	 */
	protected Object intentionArg0 = null;
	/**
	 * Current long-term intention parameter
	 */
	protected Object intentionArg1 = null;

	/**
	 * Flags about client's state, in order to know which messages to send
	 */
	protected volatile boolean clientMoving;
	/**
	 * Flags about client's state, in order to know which messages to send
	 */
	protected volatile boolean clientAutoAttacking;
	/**
	 * Flags about client's state, in order to know which messages to send
	 */
	protected int clientMovingToPawnOffset;

	/**
	 * Different targets this AI maintains
	 */
	private WorldObject target;
	private Creature castTarget;
	protected Creature attackTarget;
	protected Creature followTarget;

	/**
	 * The skill we are currently casting by INTENTION_CAST
	 */
	Skill skill;

	/**
	 * Different internal state flags
	 */
	private int moveToPawnTimeout;

	protected Future<?> followTask = null;
	private static final int FOLLOW_INTERVAL = 1000;
	private static final int ATTACK_FOLLOW_INTERVAL = 500;

	/**
	 * Constructor of AbstractAI.
	 *
	 * @param creature the creature
	 */
	protected AbstractAI(Creature creature) {
		actor = creature;
	}

	/**
	 * Return the Creature managed by this Accessor AI.<BR><BR>
	 */
	@Override
	public Creature getActor() {
		return actor;
	}

	/**
	 * Return the current Intention.<BR><BR>
	 */
	@Override
	public CtrlIntention getIntention() {
		return intention;
	}

	protected void setCastTarget(Creature target) {
		castTarget = target;
	}

	/**
	 * Return the current cast target.<BR><BR>
	 */
	public Creature getCastTarget() {
		return castTarget;
	}

	protected void setAttackTarget(Creature target) {
		attackTarget = target;
	}

	/**
	 * Return current attack target.<BR><BR>
	 */
	@Override
	public Creature getAttackTarget() {
		return attackTarget;
	}

	/**
	 * Set the Intention of this AbstractAI.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is USED by AI classes</B></FONT><BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> : </B><BR>
	 * <B>AttackableAI</B> : Create an AI Task executed every 1s (if necessary)<BR>
	 * <B>PlayerAI</B> : Stores the current AI intention parameters to later restore it if necessary<BR><BR>
	 *
	 * @param intention The new Intention to set to the AI
	 * @param arg0      The first parameter of the Intention
	 * @param arg1      The second parameter of the Intention
	 */
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1) {
		/*
         if (Config.DEBUG)
		 Logozo.warning("AbstractAI: changeIntention -> " + intention + " " + arg0 + " " + arg1);
		 */

		this.intention = intention;
		intentionArg0 = arg0;
		intentionArg1 = arg1;
	}

	/**
	 * Launch the CreatureAI onIntention method corresponding to the new Intention.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Stop the FOLLOW mode if necessary</B></FONT><BR><BR>
	 *
	 * @param intention The new Intention to set to the AI
	 */
	@Override
	public final void setIntention(CtrlIntention intention) {
		setIntention(intention, null, null);
	}

	/**
	 * Launch the CreatureAI onIntention method corresponding to the new Intention.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Stop the FOLLOW mode if necessary</B></FONT><BR><BR>
	 *
	 * @param intention The new Intention to set to the AI
	 * @param arg0      The first parameter of the Intention (optional target)
	 */
	@Override
	public final void setIntention(CtrlIntention intention, Object arg0) {
		setIntention(intention, arg0, null);
	}

	/**
	 * Launch the CreatureAI onIntention method corresponding to the new Intention.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Stop the FOLLOW mode if necessary</B></FONT><BR><BR>
	 *
	 * @param intention The new Intention to set to the AI
	 * @param arg0      The first parameter of the Intention (optional target)
	 * @param arg1      The second parameter of the Intention (optional target)
	 */
    /*
	public final void informAIIntention(CtrlIntention intent, Object arg0) {
		ThreadPoolManager.getInstance().executeAi(new InformAIMsg(this, intent, arg0));
	}

	public final void informAIIntention(CtrlIntention intent) {
		ThreadPoolManager.getInstance().executeAi(new InformAIMsg(this, intent, null));
	}

	public class InformAIMsg implements Runnable {
		private AbstractAI ai;
		private CtrlIntention intent;
		private Object arg0;
		public InformAIMsg(AbstractAI ai, CtrlIntention intention, Object arg0) {
			ai=ai;
			intent = intention;
			this.arg0 = arg0;
		}
		public final void run() {
			ai.setIntention(intent, arg0, null);
		}
	}
	 */
	@Override
	public final void setIntention(CtrlIntention intention, Object arg0, Object arg1) {
		/*
		 if (Config.DEBUG)
		 Logozo.warning("AbstractAI: setIntention -> " + intention + " " + arg0 + " " + arg1);
		 */

		// Stop the follow mode if necessary
		if (intention != AI_INTENTION_FOLLOW && intention != AI_INTENTION_ATTACK) {
			stopFollow();
		}

		// Launch the onIntention method of the CreatureAI corresponding to the new Intention
		switch (intention) {
			case AI_INTENTION_IDLE:
				onIntentionIdle();
				break;
			case AI_INTENTION_ACTIVE:
				onIntentionActive();
				break;
			case AI_INTENTION_REST:
				onIntentionRest();
				break;
			case AI_INTENTION_ATTACK:
				onIntentionAttack((Creature) arg0);
				break;
			case AI_INTENTION_CAST:
				onIntentionCast((Skill) arg0, (WorldObject) arg1);
				break;
			case AI_INTENTION_MOVE_TO:
				onIntentionMoveTo((L2CharPosition) arg0);
				break;
			case AI_INTENTION_FOLLOW:
				onIntentionFollow((Creature) arg0);
				break;
			case AI_INTENTION_PICK_UP:
				onIntentionPickUp((WorldObject) arg0);
				break;
			case AI_INTENTION_INTERACT:
				onIntentionInteract((WorldObject) arg0);
				break;
		}
	}

	/**
	 * Launch the CreatureAI onEvt method corresponding to the Event.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current general intention won't be change
	 * (ex : If the character attack and is stunned, he will attack again after the stunned period)</B></FONT><BR><BR>
	 *
	 * @param evt The event whose the AI must be notified
	 */
	@Override
	public final void notifyEvent(CtrlEvent evt) {
		notifyEvent(evt, null, null);
	}

	/**
	 * Launch the CreatureAI onEvt method corresponding to the Event.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current general intention won't be change
	 * (ex : If the character attack and is stunned, he will attack again after the stunned period)</B></FONT><BR><BR>
	 *
	 * @param evt  The event whose the AI must be notified
	 * @param arg0 The first parameter of the Event (optional target)
	 */
	@Override
	public final void notifyEvent(CtrlEvent evt, Object arg0) {
		notifyEvent(evt, arg0, null);
	}
	
	/*
	public final void informAIEvent(CtrlEvent evt) {
		ThreadPoolManager.getInstance().executeAi(new InformAIEvent(this, evt, null, null));
	}

	public final void informAIEvent(CtrlEvent evt, Object arg0) {
		ThreadPoolManager.getInstance().executeAi(new InformAIEvent(this, evt, arg0, null));
	}

	public final void informAIEvent(CtrlEvent evt, Object arg0, Object arg1) {
		ThreadPoolManager.getInstance().executeAi(new InformAIEvent(this, evt, arg0, arg1));
	}

	public class InformAIEvent implements Runnable {
		private AbstractAI ai;
		private CtrlEvent evt;
		private Object arg0, arg1;

		public InformAIEvent(AbstractAI ai, CtrlEvent evt, Object arg0, Object arg1) {
			ai=ai;
			this.evt = evt;
			this.arg0 = arg0;
			this.arg1 = arg1;
		}

		public final void run() {
			ai.notifyEvent(evt, arg0, arg1);
		}
	}
	 */

	/**
	 * Launch the CreatureAI onEvt method corresponding to the Event.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current general intention won't be change
	 * (ex : If the character attack and is stunned, he will attack again after the stunned period)</B></FONT><BR><BR>
	 *
	 * @param evt  The event whose the AI must be notified
	 * @param arg0 The first parameter of the Event (optional target)
	 * @param arg1 The second parameter of the Event (optional target)
	 */
	@Override
	public final void notifyEvent(CtrlEvent evt, Object arg0, Object arg1) {
		if (!actor.isVisible() && !actor.isTeleporting() || !actor.hasAI()) {
			//return;
		}
		
		/*
		 if (Config.DEBUG)
		 Logozo.warning("AbstractAI: notifyEvent -> " + evt + " " + arg0 + " " + arg1);
		 */

		switch (evt) {
			case EVT_THINK:
				onEvtThink();
				break;
			case EVT_ATTACKED:
				onEvtAttacked((Creature) arg0);
				break;
			case EVT_AGGRESSION:
				onEvtAggression((Creature) arg0, ((Number) arg1).intValue());
				break;
			case EVT_STUNNED:
				onEvtStunned((Creature) arg0);
				break;
			case EVT_PARALYZED:
				onEvtParalyzed((Creature) arg0);
				break;
			case EVT_SLEEPING:
				onEvtSleeping((Creature) arg0);
				break;
			case EVT_ROOTED:
				onEvtRooted((Creature) arg0);
				break;
			case EVT_CONFUSED:
				onEvtConfused((Creature) arg0);
				break;
			case EVT_MUTED:
				onEvtMuted((Creature) arg0);
				break;
			case EVT_EVADED:
				onEvtEvaded((Creature) arg0);
				break;
			case EVT_READY_TO_ACT:
				if (!actor.isCastingNow() && !actor.isCastingSimultaneouslyNow()) {
					onEvtReadyToAct();
				}
				break;
			case EVT_USER_CMD:
				onEvtUserCmd(arg0, arg1);
				break;
			case EVT_ARRIVED:
				// happens e.g. from stopmove but we don't process it if we're casting
				if (!actor.isCastingNow() && !actor.isCastingSimultaneouslyNow()) {
					onEvtArrived();
				}
				break;
			case EVT_ARRIVED_REVALIDATE:
				// this is disregarded if the char is not moving any more
				if (actor.isMoving()) {
					onEvtArrivedRevalidate();
				}
				break;
			case EVT_ARRIVED_BLOCKED:
				onEvtArrivedBlocked((L2CharPosition) arg0);
				break;
			case EVT_FORGET_OBJECT:
				onEvtForgetObject((WorldObject) arg0);
				break;
			case EVT_CANCEL:
				onEvtCancel();
				break;
			case EVT_DEAD:
				onEvtDead();
				break;
			case EVT_FAKE_DEATH:
				onEvtFakeDeath();
				break;
			case EVT_FINISH_CASTING:
				onEvtFinishCasting();
				break;
		}
	}

	protected abstract void onIntentionIdle();

	protected abstract void onIntentionActive();

	protected abstract void onIntentionRest();

	protected abstract void onIntentionAttack(Creature target);

	protected abstract void onIntentionCast(Skill skill, WorldObject target);

	protected abstract void onIntentionMoveTo(L2CharPosition destination);

	protected abstract void onIntentionFollow(Creature target);

	protected abstract void onIntentionPickUp(WorldObject item);

	protected abstract void onIntentionInteract(WorldObject object);

	protected abstract void onEvtThink();

	protected abstract void onEvtAttacked(Creature attacker);

	protected abstract void onEvtAggression(Creature target, int aggro);

	protected abstract void onEvtStunned(Creature attacker);

	protected abstract void onEvtParalyzed(Creature attacker);

	protected abstract void onEvtSleeping(Creature attacker);

	protected abstract void onEvtRooted(Creature attacker);

	protected abstract void onEvtConfused(Creature attacker);

	protected abstract void onEvtMuted(Creature attacker);

	protected abstract void onEvtEvaded(Creature attacker);

	protected abstract void onEvtReadyToAct();

	protected abstract void onEvtUserCmd(Object arg0, Object arg1);

	protected abstract void onEvtArrived();

	protected abstract void onEvtArrivedRevalidate();

	protected abstract void onEvtArrivedBlocked(L2CharPosition blocked_at_pos);

	protected abstract void onEvtForgetObject(WorldObject object);

	protected abstract void onEvtCancel();

	protected abstract void onEvtDead();

	protected abstract void onEvtFakeDeath();

	protected abstract void onEvtFinishCasting();

	/**
	 * Cancel action client side by sending Server->Client packet ActionFailed to the Player actor.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR><BR>
	 */
	protected void clientActionFailed() {
		if (actor instanceof Player) {
			actor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	/**
	 * Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn <I>(broadcast)</I>.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR><BR>
	 */
	protected void moveToPawn(WorldObject pawn, int offset) {
		// Check if actor can move
		if (!actor.isMovementDisabled()) {
			if (offset < 10) {
				offset = 10;
			}

			// prevent possible extra calls to this function (there is none?),
			// also don't send movetopawn packets too often
			boolean sendPacket = true;
			if (clientMoving && target == pawn) {
				if (clientMovingToPawnOffset == offset) {
					if (TimeController.getGameTicks() < moveToPawnTimeout) {
						return;
					}
					sendPacket = false;
				} else if (actor.isOnGeodataPath()) {
					// minimum time to calculate new route is 2 seconds
					if (TimeController.getGameTicks() < moveToPawnTimeout + 10) {
						return;
					}
				}
			}

			// Set AI movement data
			clientMoving = true;
			clientMovingToPawnOffset = offset;
			target = pawn;
			moveToPawnTimeout = TimeController.getGameTicks();
			moveToPawnTimeout += 1000 / TimeController.MILLIS_IN_TICK;

			if (pawn == null) {
				return;
			}

			// Calculate movement data for a move to location action and add the actor to movingObjects of GameTimeController
			if (pawn instanceof Creature && !(actor instanceof Player) && !(actor instanceof GrandBossInstance) &&
					!(actor instanceof Npc)) {
				if (offset == 50) {
					offset = 30;
				} else if (offset == 70) {
					offset = 50;
				}
				// Make the NPCs move around their target, not silly-following
				int tries = 10;
				Point3D position = new Point3D(pawn.getX() + Rnd.get(offset * 2) - offset, pawn.getY() + Rnd.get(offset * 2) - offset, pawn.getZ());

				while (!GeoData.getInstance().canSeeTarget(actor, position) && tries-- > 0) {
					position.setXYZ(pawn.getX() + Rnd.get(offset * 2) - offset, pawn.getY() + Rnd.get(offset * 2) - offset, pawn.getZ());
				}

				if (tries > 0) {
					actor.moveToLocation(position.getX(), position.getY(), position.getZ(), 0);
				} else {
					actor.moveToLocation(pawn.getX(), pawn.getY(), pawn.getZ(), offset);
				}
			} else {
				actor.moveToLocation(pawn.getX(), pawn.getY(), pawn.getZ(), offset);
			}

			if (!actor.isMoving()) {
				actor.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			// Send a Server->Client packet MoveToPawn/CharMoveToLocation to the actor and all Player in its knownPlayers
			if (pawn instanceof Creature) {
				if (!(actor instanceof Player) || actor.isOnGeodataPath()) {
					actor.broadcastPacket(new MoveToLocation(actor));
					clientMovingToPawnOffset = 0;
				} else if (sendPacket) // don't repeat unnecessarily
				{
					actor.broadcastPacket(new MoveToPawn(actor, (Creature) pawn, offset));
				}
			} else {
				actor.broadcastPacket(new MoveToLocation(actor));
			}
		} else {
			actor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	/**
	 * Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation <I>(broadcast)</I>.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR><BR>
	 */
	protected void moveTo(int x, int y, int z) {
		// Chek if actor can move
		if (!actor.isMovementDisabled()) {
			// Set AI movement data
			clientMoving = true;
			clientMovingToPawnOffset = 0;

			// Calculate movement data for a move to location action and add the actor to movingObjects of GameTimeController
			actor.moveToLocation(x, y, z, 0);

			// Send a Server->Client packet CharMoveToLocation to the actor and all Player in its knownPlayers
			MoveToLocation msg = new MoveToLocation(actor);
			actor.broadcastPacket(msg);
		} else {
			actor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	/**
	 * Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation <I>(broadcast)</I>.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR><BR>
	 */
	protected void clientStopMoving(L2CharPosition pos) {
		/*
		 if (Config.DEBUG)
		 Logozo.warning("clientStopMoving();");
		 */

		// Stop movement of the Creature
		if (actor.isMoving()) {
			actor.stopMove(pos);
		}

		clientMovingToPawnOffset = 0;

		if (clientMoving || pos != null) {
			clientMoving = false;

			// Send a Server->Client packet StopMove to the actor and all Player in its knownPlayers
			StopMove msg = new StopMove(actor);
			actor.broadcastPacket(msg);

			if (pos != null) {
				// Send a Server->Client packet StopRotation to the actor and all Player in its knownPlayers
				StopRotation sr = new StopRotation(actor.getObjectId(), pos.heading, 0);
				actor.sendPacket(sr);
				actor.broadcastPacket(sr);
			}
		}
	}

	// Client has already arrived to target, no need to force StopMove packet
	protected void clientStoppedMoving() {
		if (clientMovingToPawnOffset > 0) // movetoPawn needs to be stopped
		{
			clientMovingToPawnOffset = 0;
			StopMove msg = new StopMove(actor);
			actor.broadcastPacket(msg);
		}
		clientMoving = false;
	}

	public boolean isAutoAttacking() {
		return clientAutoAttacking;
	}

	public void setAutoAttacking(boolean isAutoAttacking) {
		if (actor instanceof Summon) {
			Summon summon = (Summon) actor;
			if (summon.getOwner() != null) {
				summon.getOwner().getAI().setAutoAttacking(isAutoAttacking);
			}
			return;
		}
		clientAutoAttacking = isAutoAttacking;
	}

	/**
	 * Start the actor Auto Attack client side by sending Server->Client packet AutoAttackStart <I>(broadcast)</I>.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR><BR>
	 */
	public void clientStartAutoAttack() {
		if (actor instanceof Summon) {
			Summon summon = (Summon) actor;
			if (summon.getOwner() != null) {
				summon.getOwner().getAI().clientStartAutoAttack();
			}
			return;
		}

		if (!isAutoAttacking()) {
			if (actor instanceof Player) {
				if (((Player) actor).getPet() != null) {
					((Player) actor).getPet().broadcastPacket(new AutoAttackStart(((Player) actor).getPet().getObjectId()));
				}
				for (SummonInstance summon : ((Player) actor).getSummons()) {
					summon.broadcastPacket(new AutoAttackStart(summon.getObjectId()));
				}
			}
			// Send a Server->Client packet AutoAttackStart to the actor and all Player in its knownPlayers
			actor.broadcastPacket(new AutoAttackStart(actor.getObjectId()));
			setAutoAttacking(true);
		}

		AttackStanceTaskManager.getInstance().addAttackStanceTask(actor);
	}

	/**
	 * Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop <I>(broadcast)</I>.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR><BR>
	 */
	public void clientStopAutoAttack() {
		if (actor instanceof Summon) {
			Summon summon = (Summon) actor;
			if (summon.getOwner() != null) {
				summon.getOwner().getAI().clientStopAutoAttack();
			}
			return;
		}
		if (actor instanceof Player) {
			if (!AttackStanceTaskManager.getInstance().getAttackStanceTask(actor) && isAutoAttacking()) {
				AttackStanceTaskManager.getInstance().addAttackStanceTask(actor);
			}
		} else if (isAutoAttacking()) {
			actor.broadcastPacket(new AutoAttackStop(actor.getObjectId()));
			setAutoAttacking(false);
		}
	}

	/**
	 * Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die <I>(broadcast)</I>.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR><BR>
	 */
	protected void clientNotifyDead() {
		// Send a Server->Client packet Die to the actor and all Player in its knownPlayers
		Die msg = new Die(actor);
		actor.broadcastPacket(msg);

		// Init AI
		intention = AI_INTENTION_IDLE;
		target = null;
		castTarget = null;
		attackTarget = null;

		// Cancel the follow task if necessary
		stopFollow();
	}

	/**
	 * Update the state of this actor client side by sending Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the Player player.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR><BR>
	 *
	 * @param player The L2PcIstance to notify with state of this Creature
	 */
	public void describeStateToPlayer(Player player) {
		if (clientMoving) {
			if (clientMovingToPawnOffset != 0 && followTarget != null) {
				// Send a Server->Client packet MoveToPawn to the actor and all Player in its knownPlayers
				MoveToPawn msg = new MoveToPawn(actor, followTarget, clientMovingToPawnOffset);
				player.sendPacket(msg);
			} else {
				// Send a Server->Client packet CharMoveToLocation to the actor and all Player in its knownPlayers
				MoveToLocation msg = new MoveToLocation(actor);
				player.sendPacket(msg);
			}
		}
	}

	/**
	 * Create and Launch an AI Follow Task to execute every 1s.<BR><BR>
	 *
	 * @param target The Creature to follow
	 */
	public synchronized void startFollow(Creature target) {
		if (followTask != null) {
			followTask.cancel(false);
			followTask = null;
		}

		// Create and Launch an AI Follow Task to execute every 1s
		followTarget = target;
		followTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FollowTask(), 5, FOLLOW_INTERVAL);
	}

	/**
	 * Create and Launch an AI Follow Task to execute every 0.5s, following at specified range.<BR><BR>
	 *
	 * @param target The Creature to follow
	 */
	public synchronized void startFollow(Creature target, int range) {
		if (followTask != null) {
			followTask.cancel(false);
			followTask = null;
		}

		followTarget = target;
		followTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FollowTask(range), 5, ATTACK_FOLLOW_INTERVAL);
	}

	/**
	 * Stop an AI Follow Task.<BR><BR>
	 */
	public synchronized void stopFollow() {
		if (followTask != null) {
			// Stop the Follow Task
			followTask.cancel(false);
			followTask = null;
		}
		followTarget = null;
	}

	protected Creature getFollowTarget() {
		return followTarget;
	}

	protected WorldObject getTarget() {
		return target;
	}

	protected void setTarget(WorldObject target) {
		this.target = target;
	}

	/**
	 * Stop all Ai tasks and futures.
	 */
	public void stopAITask() {
		stopFollow();
	}

	@Override
	public String toString() {
		if (actor == null) {
			return "Actor: null";
		} else {
			return "Actor: " + actor;
		}
	}
}
