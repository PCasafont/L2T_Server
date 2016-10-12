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
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2GrandBossInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;
import l2server.log.Log;
import l2server.util.Point3D;
import l2server.util.Rnd;

import java.util.concurrent.Future;
import java.util.logging.Level;

import static l2server.gameserver.ai.CtrlIntention.*;

/**
 * Mother class of all objects AI in the world.<BR><BR>
 * <p>
 * AbastractAI :<BR><BR>
 * <li>L2CharacterAI</li><BR><BR>
 */
abstract class AbstractAI implements Ctrl
{

	class FollowTask implements Runnable
	{
		protected int _range = 70;

		public FollowTask()
		{
		}

		public FollowTask(int range)
		{
			_range = range;
		}

		@Override
		public void run()
		{
			try
			{
				if (_followTask == null)
				{
					return;
				}

				L2Character followTarget = _followTarget; // copy to prevent NPE
				if (followTarget == null)
				{
					if (_actor instanceof L2Summon)
					{
						((L2Summon) _actor).setFollowStatus(false);
					}
					setIntention(AI_INTENTION_IDLE);
					return;
				}

				if (!_actor.isInsideRadius(followTarget, _range, true, false))
				{
					if (!_actor.isInsideRadius(followTarget, 3000, true, false))
					{
						// if the target is too far (maybe also teleported)
						if (_actor instanceof L2Summon)
						{
							((L2Summon) _actor).setFollowStatus(false);
						}

						setIntention(AI_INTENTION_IDLE);
						return;
					}

					moveToPawn(followTarget, _range);
				}
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING, "", e);
			}
		}
	}

	/**
	 * The character that this AI manages
	 */
	protected final L2Character _actor;

	/**
	 * An accessor for private methods of the actor
	 */
	protected final L2Character.AIAccessor _accessor;

	/**
	 * Current long-term intention
	 */
	protected CtrlIntention _intention = AI_INTENTION_IDLE;
	/**
	 * Current long-term intention parameter
	 */
	protected Object _intentionArg0 = null;
	/**
	 * Current long-term intention parameter
	 */
	protected Object _intentionArg1 = null;

	/**
	 * Flags about client's state, in order to know which messages to send
	 */
	protected volatile boolean _clientMoving;
	/**
	 * Flags about client's state, in order to know which messages to send
	 */
	protected volatile boolean _clientAutoAttacking;
	/**
	 * Flags about client's state, in order to know which messages to send
	 */
	protected int _clientMovingToPawnOffset;

	/**
	 * Different targets this AI maintains
	 */
	private L2Object _target;
	private L2Character _castTarget;
	protected L2Character _attackTarget;
	protected L2Character _followTarget;

	/**
	 * The skill we are currently casting by INTENTION_CAST
	 */
	L2Skill _skill;

	/**
	 * Different internal state flags
	 */
	private int _moveToPawnTimeout;

	protected Future<?> _followTask = null;
	private static final int FOLLOW_INTERVAL = 1000;
	private static final int ATTACK_FOLLOW_INTERVAL = 500;

	/**
	 * Constructor of AbstractAI.<BR><BR>
	 *
	 * @param accessor The AI accessor of the L2Character
	 */
	protected AbstractAI(L2Character.AIAccessor accessor)
	{
		_accessor = accessor;

		// Get the L2Character managed by this Accessor AI
		_actor = accessor.getActor();
	}

	/**
	 * Return the L2Character managed by this Accessor AI.<BR><BR>
	 */
	@Override
	public L2Character getActor()
	{
		return _actor;
	}

	/**
	 * Return the current Intention.<BR><BR>
	 */
	@Override
	public CtrlIntention getIntention()
	{
		return _intention;
	}

	protected void setCastTarget(L2Character target)
	{
		_castTarget = target;
	}

	/**
	 * Return the current cast target.<BR><BR>
	 */
	public L2Character getCastTarget()
	{
		return _castTarget;
	}

	protected void setAttackTarget(L2Character target)
	{
		_attackTarget = target;
	}

	/**
	 * Return current attack target.<BR><BR>
	 */
	@Override
	public L2Character getAttackTarget()
	{
		return _attackTarget;
	}

	/**
	 * Set the Intention of this AbstractAI.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is USED by AI classes</B></FONT><BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> : </B><BR>
	 * <B>L2AttackableAI</B> : Create an AI Task executed every 1s (if necessary)<BR>
	 * <B>L2PlayerAI</B> : Stores the current AI intention parameters to later restore it if necessary<BR><BR>
	 *
	 * @param intention The new Intention to set to the AI
	 * @param arg0      The first parameter of the Intention
	 * @param arg1      The second parameter of the Intention
	 */
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		/*
         if (Config.DEBUG)
		 Logozo.warning("AbstractAI: changeIntention -> " + intention + " " + arg0 + " " + arg1);
		 */

		_intention = intention;
		_intentionArg0 = arg0;
		_intentionArg1 = arg1;
	}

	/**
	 * Launch the L2CharacterAI onIntention method corresponding to the new Intention.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Stop the FOLLOW mode if necessary</B></FONT><BR><BR>
	 *
	 * @param intention The new Intention to set to the AI
	 */
	@Override
	public final void setIntention(CtrlIntention intention)
	{
		setIntention(intention, null, null);
	}

	/**
	 * Launch the L2CharacterAI onIntention method corresponding to the new Intention.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Stop the FOLLOW mode if necessary</B></FONT><BR><BR>
	 *
	 * @param intention The new Intention to set to the AI
	 * @param arg0      The first parameter of the Intention (optional target)
	 */
	@Override
	public final void setIntention(CtrlIntention intention, Object arg0)
	{
		setIntention(intention, arg0, null);
	}

	/**
	 * Launch the L2CharacterAI onIntention method corresponding to the new Intention.<BR><BR>
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
		private AbstractAI _ai;
		private CtrlIntention _intent;
		private Object _arg0;
		public InformAIMsg(AbstractAI ai, CtrlIntention intention, Object arg0) {
			_ai=ai;
			_intent = intention;
			_arg0 = arg0;
		}
		public final void run() {
			_ai.setIntention(_intent, _arg0, null);
		}
	}
	 */
	@Override
	public final void setIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		/*
		 if (Config.DEBUG)
		 Logozo.warning("AbstractAI: setIntention -> " + intention + " " + arg0 + " " + arg1);
		 */

		// Stop the follow mode if necessary
		if (intention != AI_INTENTION_FOLLOW && intention != AI_INTENTION_ATTACK)
		{
			stopFollow();
		}

		// Launch the onIntention method of the L2CharacterAI corresponding to the new Intention
		switch (intention)
		{
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
				onIntentionAttack((L2Character) arg0);
				break;
			case AI_INTENTION_CAST:
				onIntentionCast((L2Skill) arg0, (L2Object) arg1);
				break;
			case AI_INTENTION_MOVE_TO:
				onIntentionMoveTo((L2CharPosition) arg0);
				break;
			case AI_INTENTION_FOLLOW:
				onIntentionFollow((L2Character) arg0);
				break;
			case AI_INTENTION_PICK_UP:
				onIntentionPickUp((L2Object) arg0);
				break;
			case AI_INTENTION_INTERACT:
				onIntentionInteract((L2Object) arg0);
				break;
		}
	}

	/**
	 * Launch the L2CharacterAI onEvt method corresponding to the Event.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current general intention won't be change
	 * (ex : If the character attack and is stunned, he will attack again after the stunned period)</B></FONT><BR><BR>
	 *
	 * @param evt The event whose the AI must be notified
	 */
	@Override
	public final void notifyEvent(CtrlEvent evt)
	{
		notifyEvent(evt, null, null);
	}

	/**
	 * Launch the L2CharacterAI onEvt method corresponding to the Event.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current general intention won't be change
	 * (ex : If the character attack and is stunned, he will attack again after the stunned period)</B></FONT><BR><BR>
	 *
	 * @param evt  The event whose the AI must be notified
	 * @param arg0 The first parameter of the Event (optional target)
	 */
	@Override
	public final void notifyEvent(CtrlEvent evt, Object arg0)
	{
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
		private AbstractAI _ai;
		private CtrlEvent _evt;
		private Object _arg0, _arg1;

		public InformAIEvent(AbstractAI ai, CtrlEvent evt, Object arg0, Object arg1) {
			_ai=ai;
			_evt = evt;
			_arg0 = arg0;
			_arg1 = arg1;
		}

		public final void run() {
			_ai.notifyEvent(_evt, _arg0, _arg1);
		}
	}
	 */

	/**
	 * Launch the L2CharacterAI onEvt method corresponding to the Event.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current general intention won't be change
	 * (ex : If the character attack and is stunned, he will attack again after the stunned period)</B></FONT><BR><BR>
	 *
	 * @param evt  The event whose the AI must be notified
	 * @param arg0 The first parameter of the Event (optional target)
	 * @param arg1 The second parameter of the Event (optional target)
	 */
	@Override
	public final void notifyEvent(CtrlEvent evt, Object arg0, Object arg1)
	{
		if (!_actor.isVisible() && !_actor.isTeleporting() || !_actor.hasAI())
		{
			//return;
		}
		
		/*
		 if (Config.DEBUG)
		 Logozo.warning("AbstractAI: notifyEvent -> " + evt + " " + arg0 + " " + arg1);
		 */

		switch (evt)
		{
			case EVT_THINK:
				onEvtThink();
				break;
			case EVT_ATTACKED:
				onEvtAttacked((L2Character) arg0);
				break;
			case EVT_AGGRESSION:
				onEvtAggression((L2Character) arg0, ((Number) arg1).intValue());
				break;
			case EVT_STUNNED:
				onEvtStunned((L2Character) arg0);
				break;
			case EVT_PARALYZED:
				onEvtParalyzed((L2Character) arg0);
				break;
			case EVT_SLEEPING:
				onEvtSleeping((L2Character) arg0);
				break;
			case EVT_ROOTED:
				onEvtRooted((L2Character) arg0);
				break;
			case EVT_CONFUSED:
				onEvtConfused((L2Character) arg0);
				break;
			case EVT_MUTED:
				onEvtMuted((L2Character) arg0);
				break;
			case EVT_EVADED:
				onEvtEvaded((L2Character) arg0);
				break;
			case EVT_READY_TO_ACT:
				if (!_actor.isCastingNow() && !_actor.isCastingSimultaneouslyNow())
				{
					onEvtReadyToAct();
				}
				break;
			case EVT_USER_CMD:
				onEvtUserCmd(arg0, arg1);
				break;
			case EVT_ARRIVED:
				// happens e.g. from stopmove but we don't process it if we're casting
				if (!_actor.isCastingNow() && !_actor.isCastingSimultaneouslyNow())
				{
					onEvtArrived();
				}
				break;
			case EVT_ARRIVED_REVALIDATE:
				// this is disregarded if the char is not moving any more
				if (_actor.isMoving())
				{
					onEvtArrivedRevalidate();
				}
				break;
			case EVT_ARRIVED_BLOCKED:
				onEvtArrivedBlocked((L2CharPosition) arg0);
				break;
			case EVT_FORGET_OBJECT:
				onEvtForgetObject((L2Object) arg0);
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

	protected abstract void onIntentionAttack(L2Character target);

	protected abstract void onIntentionCast(L2Skill skill, L2Object target);

	protected abstract void onIntentionMoveTo(L2CharPosition destination);

	protected abstract void onIntentionFollow(L2Character target);

	protected abstract void onIntentionPickUp(L2Object item);

	protected abstract void onIntentionInteract(L2Object object);

	protected abstract void onEvtThink();

	protected abstract void onEvtAttacked(L2Character attacker);

	protected abstract void onEvtAggression(L2Character target, int aggro);

	protected abstract void onEvtStunned(L2Character attacker);

	protected abstract void onEvtParalyzed(L2Character attacker);

	protected abstract void onEvtSleeping(L2Character attacker);

	protected abstract void onEvtRooted(L2Character attacker);

	protected abstract void onEvtConfused(L2Character attacker);

	protected abstract void onEvtMuted(L2Character attacker);

	protected abstract void onEvtEvaded(L2Character attacker);

	protected abstract void onEvtReadyToAct();

	protected abstract void onEvtUserCmd(Object arg0, Object arg1);

	protected abstract void onEvtArrived();

	protected abstract void onEvtArrivedRevalidate();

	protected abstract void onEvtArrivedBlocked(L2CharPosition blocked_at_pos);

	protected abstract void onEvtForgetObject(L2Object object);

	protected abstract void onEvtCancel();

	protected abstract void onEvtDead();

	protected abstract void onEvtFakeDeath();

	protected abstract void onEvtFinishCasting();

	/**
	 * Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR><BR>
	 */
	protected void clientActionFailed()
	{
		if (_actor instanceof L2PcInstance)
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	/**
	 * Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn <I>(broadcast)</I>.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR><BR>
	 */
	protected void moveToPawn(L2Object pawn, int offset)
	{
		// Check if actor can move
		if (!_actor.isMovementDisabled())
		{
			if (offset < 10)
			{
				offset = 10;
			}

			// prevent possible extra calls to this function (there is none?),
			// also don't send movetopawn packets too often
			boolean sendPacket = true;
			if (_clientMoving && _target == pawn)
			{
				if (_clientMovingToPawnOffset == offset)
				{
					if (TimeController.getGameTicks() < _moveToPawnTimeout)
					{
						return;
					}
					sendPacket = false;
				}
				else if (_actor.isOnGeodataPath())
				{
					// minimum time to calculate new route is 2 seconds
					if (TimeController.getGameTicks() < _moveToPawnTimeout + 10)
					{
						return;
					}
				}
			}

			// Set AI movement data
			_clientMoving = true;
			_clientMovingToPawnOffset = offset;
			_target = pawn;
			_moveToPawnTimeout = TimeController.getGameTicks();
			_moveToPawnTimeout += 1000 / TimeController.MILLIS_IN_TICK;

			if (pawn == null || _accessor == null)
			{
				return;
			}

			// Calculate movement data for a move to location action and add the actor to movingObjects of GameTimeController
			if (pawn instanceof L2Character && !(_actor instanceof L2PcInstance) &&
					!(_actor instanceof L2GrandBossInstance) && !(_actor instanceof L2Npc))
			{
				if (offset == 50)
				{
					offset = 30;
				}
				else if (offset == 70)
				{
					offset = 50;
				}
				// Make the NPCs move around their target, not silly-following
				int tries = 10;
				Point3D position = new Point3D(pawn.getX() + Rnd.get(offset * 2) - offset,
						pawn.getY() + Rnd.get(offset * 2) - offset, pawn.getZ());

				while (!GeoData.getInstance().canSeeTarget(_actor, position) && tries-- > 0)
				{
					position.setXYZ(pawn.getX() + Rnd.get(offset * 2) - offset,
							pawn.getY() + Rnd.get(offset * 2) - offset, pawn.getZ());
				}

				if (tries > 0)
				{
					_accessor.moveTo(position.getX(), position.getY(), position.getZ(), 0);
				}
				else
				{
					_accessor.moveTo(pawn.getX(), pawn.getY(), pawn.getZ(), offset);
				}
			}
			else
			{
				_accessor.moveTo(pawn.getX(), pawn.getY(), pawn.getZ(), offset);
			}

			if (!_actor.isMoving())
			{
				_actor.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			// Send a Server->Client packet MoveToPawn/CharMoveToLocation to the actor and all L2PcInstance in its _knownPlayers
			if (pawn instanceof L2Character)
			{
				if (!(_actor instanceof L2PcInstance) || _actor.isOnGeodataPath())
				{
					_actor.broadcastPacket(new MoveToLocation(_actor));
					_clientMovingToPawnOffset = 0;
				}
				else if (sendPacket) // don't repeat unnecessarily
				{
					_actor.broadcastPacket(new MoveToPawn(_actor, (L2Character) pawn, offset));
				}
			}
			else
			{
				_actor.broadcastPacket(new MoveToLocation(_actor));
			}
		}
		else
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	/**
	 * Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation <I>(broadcast)</I>.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR><BR>
	 */
	protected void moveTo(int x, int y, int z)
	{
		// Chek if actor can move
		if (!_actor.isMovementDisabled())
		{
			// Set AI movement data
			_clientMoving = true;
			_clientMovingToPawnOffset = 0;

			// Calculate movement data for a move to location action and add the actor to movingObjects of GameTimeController
			_accessor.moveTo(x, y, z);

			// Send a Server->Client packet CharMoveToLocation to the actor and all L2PcInstance in its _knownPlayers
			MoveToLocation msg = new MoveToLocation(_actor);
			_actor.broadcastPacket(msg);
		}
		else
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	/**
	 * Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation <I>(broadcast)</I>.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR><BR>
	 */
	protected void clientStopMoving(L2CharPosition pos)
	{
		/*
		 if (Config.DEBUG)
		 Logozo.warning("clientStopMoving();");
		 */

		// Stop movement of the L2Character
		if (_actor.isMoving())
		{
			_accessor.stopMove(pos);
		}

		_clientMovingToPawnOffset = 0;

		if (_clientMoving || pos != null)
		{
			_clientMoving = false;

			// Send a Server->Client packet StopMove to the actor and all L2PcInstance in its _knownPlayers
			StopMove msg = new StopMove(_actor);
			_actor.broadcastPacket(msg);

			if (pos != null)
			{
				// Send a Server->Client packet StopRotation to the actor and all L2PcInstance in its _knownPlayers
				StopRotation sr = new StopRotation(_actor.getObjectId(), pos.heading, 0);
				_actor.sendPacket(sr);
				_actor.broadcastPacket(sr);
			}
		}
	}

	// Client has already arrived to target, no need to force StopMove packet
	protected void clientStoppedMoving()
	{
		if (_clientMovingToPawnOffset > 0) // movetoPawn needs to be stopped
		{
			_clientMovingToPawnOffset = 0;
			StopMove msg = new StopMove(_actor);
			_actor.broadcastPacket(msg);
		}
		_clientMoving = false;
	}

	public boolean isAutoAttacking()
	{
		return _clientAutoAttacking;
	}

	public void setAutoAttacking(boolean isAutoAttacking)
	{
		if (_actor instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) _actor;
			if (summon.getOwner() != null)
			{
				summon.getOwner().getAI().setAutoAttacking(isAutoAttacking);
			}
			return;
		}
		_clientAutoAttacking = isAutoAttacking;
	}

	/**
	 * Start the actor Auto Attack client side by sending Server->Client packet AutoAttackStart <I>(broadcast)</I>.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR><BR>
	 */
	public void clientStartAutoAttack()
	{
		if (_actor instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) _actor;
			if (summon.getOwner() != null)
			{
				summon.getOwner().getAI().clientStartAutoAttack();
			}
			return;
		}

		if (!isAutoAttacking())
		{
			if (_actor instanceof L2PcInstance)
			{
				if (((L2PcInstance) _actor).getPet() != null)
				{
					((L2PcInstance) _actor).getPet()
							.broadcastPacket(new AutoAttackStart(((L2PcInstance) _actor).getPet().getObjectId()));
				}
				for (L2SummonInstance summon : ((L2PcInstance) _actor).getSummons())
				{
					summon.broadcastPacket(new AutoAttackStart(summon.getObjectId()));
				}
			}
			// Send a Server->Client packet AutoAttackStart to the actor and all L2PcInstance in its _knownPlayers
			_actor.broadcastPacket(new AutoAttackStart(_actor.getObjectId()));
			setAutoAttacking(true);
		}

		AttackStanceTaskManager.getInstance().addAttackStanceTask(_actor);
	}

	/**
	 * Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop <I>(broadcast)</I>.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR><BR>
	 */
	public void clientStopAutoAttack()
	{
		if (_actor instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) _actor;
			if (summon.getOwner() != null)
			{
				summon.getOwner().getAI().clientStopAutoAttack();
			}
			return;
		}
		if (_actor instanceof L2PcInstance)
		{
			if (!AttackStanceTaskManager.getInstance().getAttackStanceTask(_actor) && isAutoAttacking())
			{
				AttackStanceTaskManager.getInstance().addAttackStanceTask(_actor);
			}
		}
		else if (isAutoAttacking())
		{
			_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
			setAutoAttacking(false);
		}
	}

	/**
	 * Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die <I>(broadcast)</I>.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR><BR>
	 */
	protected void clientNotifyDead()
	{
		// Send a Server->Client packet Die to the actor and all L2PcInstance in its _knownPlayers
		Die msg = new Die(_actor);
		_actor.broadcastPacket(msg);

		// Init AI
		_intention = AI_INTENTION_IDLE;
		_target = null;
		_castTarget = null;
		_attackTarget = null;

		// Cancel the follow task if necessary
		stopFollow();
	}

	/**
	 * Update the state of this actor client side by sending Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance player.<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR><BR>
	 *
	 * @param player The L2PcIstance to notify with state of this L2Character
	 */
	public void describeStateToPlayer(L2PcInstance player)
	{
		if (_clientMoving)
		{
			if (_clientMovingToPawnOffset != 0 && _followTarget != null)
			{
				// Send a Server->Client packet MoveToPawn to the actor and all L2PcInstance in its _knownPlayers
				MoveToPawn msg = new MoveToPawn(_actor, _followTarget, _clientMovingToPawnOffset);
				player.sendPacket(msg);
			}
			else
			{
				// Send a Server->Client packet CharMoveToLocation to the actor and all L2PcInstance in its _knownPlayers
				MoveToLocation msg = new MoveToLocation(_actor);
				player.sendPacket(msg);
			}
		}
	}

	/**
	 * Create and Launch an AI Follow Task to execute every 1s.<BR><BR>
	 *
	 * @param target The L2Character to follow
	 */
	public synchronized void startFollow(L2Character target)
	{
		if (_followTask != null)
		{
			_followTask.cancel(false);
			_followTask = null;
		}

		// Create and Launch an AI Follow Task to execute every 1s
		_followTarget = target;
		_followTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FollowTask(), 5, FOLLOW_INTERVAL);
	}

	/**
	 * Create and Launch an AI Follow Task to execute every 0.5s, following at specified range.<BR><BR>
	 *
	 * @param target The L2Character to follow
	 */
	public synchronized void startFollow(L2Character target, int range)
	{
		if (_followTask != null)
		{
			_followTask.cancel(false);
			_followTask = null;
		}

		_followTarget = target;
		_followTask =
				ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FollowTask(range), 5, ATTACK_FOLLOW_INTERVAL);
	}

	/**
	 * Stop an AI Follow Task.<BR><BR>
	 */
	public synchronized void stopFollow()
	{
		if (_followTask != null)
		{
			// Stop the Follow Task
			_followTask.cancel(false);
			_followTask = null;
		}
		_followTarget = null;
	}

	protected L2Character getFollowTarget()
	{
		return _followTarget;
	}

	protected L2Object getTarget()
	{
		return _target;
	}

	protected void setTarget(L2Object target)
	{
		_target = target;
	}

	/**
	 * Stop all Ai tasks and futures.
	 */
	public void stopAITask()
	{
		stopFollow();
	}

	@Override
	public String toString()
	{
		if (_actor == null)
		{
			return "Actor: null";
		}
		else
		{
			return "Actor: " + _actor;
		}
	}
}
