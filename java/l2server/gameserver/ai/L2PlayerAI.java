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
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Character.AIAccessor;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2StaticObjectInstance;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.log.Log;

import static l2server.gameserver.ai.CtrlIntention.*;

public class L2PlayerAI extends L2PlayableAI
{

	private boolean _thinking; // to prevent recursive thinking

	IntentionCommand _nextIntention = null;

	public L2PlayerAI(AIAccessor accessor)
	{
		super(accessor);
	}

	void saveNextIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		_nextIntention = new IntentionCommand(intention, arg0, arg1);
	}

	@Override
	public IntentionCommand getNextIntention()
	{
		return _nextIntention;
	}

	/**
	 * Saves the current Intention for this L2PlayerAI if necessary and calls changeIntention in AbstractAI.<BR><BR>
	 *
	 * @param intention The new Intention to set to the AI
	 * @param arg0      The first parameter of the Intention
	 * @param arg1      The second parameter of the Intention
	 */
	@Override
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		/*
         if (Config.DEBUG)
		 Logozo.warning("L2PlayerAI: changeIntention -> " + intention + " " + arg0 + " " + arg1);
		 */

		// do nothing unless CAST intention
		// however, forget interrupted actions when starting to use an offensive skill
		if (intention != AI_INTENTION_CAST || arg0 != null && ((L2Skill) arg0).isOffensive())
		{
			_nextIntention = null;
			super.changeIntention(intention, arg0, arg1);
			return;
		}

		// do nothing if next intention is same as current one.
		if (intention == _intention && arg0 == _intentionArg0 && arg1 == _intentionArg1)
		{
			super.changeIntention(intention, arg0, arg1);
			return;
		}

		// save current intention so it can be used after cast
		saveNextIntention(_intention, _intentionArg0, _intentionArg1);
		super.changeIntention(intention, arg0, arg1);
	}

	/**
	 * Launch actions corresponding to the Event ReadyToAct.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Launch actions corresponding to the Event Think</li><BR><BR>
	 */
	@Override
	protected void onEvtReadyToAct()
	{
		// Launch actions corresponding to the Event Think
		if (_nextIntention != null)
		{
			setIntention(_nextIntention._crtlIntention, _nextIntention._arg0, _nextIntention._arg1);
			_nextIntention = null;
		}
		super.onEvtReadyToAct();
	}

	/**
	 * Launch actions corresponding to the Event Cancel.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop an AI Follow Task</li>
	 * <li>Launch actions corresponding to the Event Think</li><BR><BR>
	 */
	@Override
	protected void onEvtCancel()
	{
		_nextIntention = null;
		super.onEvtCancel();
	}

	/**
	 * Finalize the casting of a skill. This method overrides L2CharacterAI method.<BR><BR>
	 * <p>
	 * <B>What it does:</B>
	 * Check if actual intention is set to CAST and, if so, retrieves latest intention
	 * before the actual CAST and set it as the current intention for the player
	 */
	@Override
	protected void onEvtFinishCasting()
	{
		if (getIntention() == AI_INTENTION_CAST)
		{
			// run interrupted or next intention

			IntentionCommand nextIntention = _nextIntention;
			if (nextIntention != null)
			{
				if (nextIntention._crtlIntention != AI_INTENTION_CAST) // previous state shouldn't be casting
				{
					setIntention(nextIntention._crtlIntention, nextIntention._arg0, nextIntention._arg1);
				}
				else
				{
					setIntention(AI_INTENTION_IDLE);
				}
			}
			else
			{
                /*
				 if (Config.DEBUG)
				 Logozo.warning("L2PlayerAI: no previous intention set... Setting it to IDLE");
				 */
				// set intention to idle if skill doesn't change intention.
				setIntention(AI_INTENTION_IDLE);
			}
		}
	}

	@Override
	protected void onIntentionRest()
	{
		if (getIntention() != AI_INTENTION_REST)
		{
			changeIntention(AI_INTENTION_REST, null, null);
			setTarget(null);
			if (getAttackTarget() != null)
			{
				setAttackTarget(null);
			}
			clientStopMoving(null);
		}
	}

	@Override
	protected void onIntentionActive()
	{
		setIntention(AI_INTENTION_IDLE);
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
	protected void onIntentionMoveTo(L2CharPosition pos)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}

		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAttackingNow())
		{
			clientActionFailed();
			saveNextIntention(AI_INTENTION_MOVE_TO, pos, null);
			return;
		}

		// Set the Intention of this AbstractAI to AI_INTENTION_MOVE_TO
		changeIntention(AI_INTENTION_MOVE_TO, pos, null);

		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();

		// Abort the attack of the L2Character and send Server->Client ActionFailed packet
		_actor.abortAttack();

		// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
		moveTo(pos.x, pos.y, pos.z);
	}

	@Override
	protected void clientNotifyDead()
	{
		_clientMovingToPawnOffset = 0;
		_clientMoving = false;

		super.clientNotifyDead();
	}

	private void thinkAttack()
	{
		L2Character target = getAttackTarget();
		if (target == null)
		{
			return;
		}
		if (checkTargetLostOrDead(target))
		{
			// Notify the target
			setAttackTarget(null);
			return;
		}
		if (maybeMoveToPawn(target, _actor.getPhysicalAttackRange()))
		{
			return;
		}

		_accessor.doAttack(target);
	}

	private void thinkCast()
	{
		L2Character target = getCastTarget();
		if (Config.DEBUG)
		{
			Log.warning("L2PlayerAI: thinkCast -> Start");
		}

		if ((_skill.getTargetType() == L2SkillTargetType.TARGET_GROUND ||
				_skill.getTargetType() == L2SkillTargetType.TARGET_GROUND_AREA) && _actor instanceof L2PcInstance)
		{
			if (maybeMoveToPosition(_actor.getSkillCastPosition(), _actor.getMagicalAttackRange(_skill)))
			{
				_actor.setIsCastingNow(false);
				_actor.setIsCastingNow2(false);
				return;
			}
		}
		else
		{
			if (checkTargetLost(target))
			{
				if (_skill.isOffensive() && getAttackTarget() != null)
				{
					//Notify the target
					setCastTarget(null);
				}
				_actor.setIsCastingNow(false);
				_actor.setIsCastingNow2(false);
				return;
			}
			if (target != null && maybeMoveToPawn(target, _actor.getMagicalAttackRange(_skill)))
			{
				_actor.setIsCastingNow(false);
				_actor.setIsCastingNow2(false);
				return;
			}
		}

		if (_skill.getHitTime() > 50 && !_skill.isSimultaneousCast())
		{
			clientStopMoving(null);
		}

		L2Object oldTarget = _actor.getTarget();
		if (oldTarget != null && target != null && oldTarget != target)
		{
			// Replace the current target by the cast target
			_actor.setTarget(getCastTarget());
			// Launch the Cast of the skill
			_accessor.doCast(_skill, _actor.canDoubleCast() && !_actor.wasLastCast1());
			// Restore the initial target
			_actor.setTarget(oldTarget);
		}
		else
		{
			_accessor.doCast(_skill, _actor.canDoubleCast() && !_actor.wasLastCast1());
		}
	}

	private void thinkPickUp()
	{
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			return;
		}
		L2Object target = getTarget();
		if (checkTargetLost(target))
		{
			return;
		}
		if (maybeMoveToPawn(target, 36))
		{
			return;
		}
		setIntention(AI_INTENTION_IDLE);
		((L2PcInstance.AIAccessor) _accessor).doPickupItem(target);
	}

	private void thinkInteract()
	{
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			return;
		}
		L2Object target = getTarget();
		if (checkTargetLost(target))
		{
			return;
		}
		if (maybeMoveToPawn(target, 36))
		{
			return;
		}
		if (!(target instanceof L2StaticObjectInstance))
		{
			((L2PcInstance.AIAccessor) _accessor).doInteract((L2Character) target);
		}
		setIntention(AI_INTENTION_IDLE);
	}

	@Override
	protected void onEvtThink()
	{
		if (_thinking && getIntention() != AI_INTENTION_CAST) // casting must always continue
		{
			return;
		}
		
		/*
		 if (Config.DEBUG)
		 Logozo.warning("L2PlayerAI: onEvtThink -> Check intention");
		 */

		_thinking = true;
		try
		{
			if (getIntention() == AI_INTENTION_ATTACK)
			{
				thinkAttack();
			}
			else if (getIntention() == AI_INTENTION_CAST)
			{
				thinkCast();
			}
			else if (getIntention() == AI_INTENTION_PICK_UP)
			{
				thinkPickUp();
			}
			else if (getIntention() == AI_INTENTION_INTERACT)
			{
				thinkInteract();
			}
		}
		finally
		{
			_thinking = false;
		}
	}
}
