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
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Character.AIAccessor;
import l2server.gameserver.model.actor.L2Summon;
import l2server.util.Rnd;

import java.util.concurrent.Future;

import static l2server.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static l2server.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

public class L2SummonAI extends L2PlayableAI implements Runnable
{
	private static final int AVOID_RADIUS = 70;

	private volatile boolean thinking; // to prevent recursive thinking
	private volatile boolean startFollow = ((L2Summon) actor).getFollowStatus();
	@SuppressWarnings("unused")
	private L2Character lastAttack = null;

	private volatile boolean startAvoid = false;
	private Future<?> avoidTask = null;

	public L2SummonAI(AIAccessor accessor)
	{
		super(accessor);
	}

	@Override
	protected void onIntentionIdle()
	{
		stopFollow();
		startFollow = false;
		onIntentionActive();
	}

	@Override
	protected void onIntentionActive()
	{
		L2Summon summon = (L2Summon) actor;
		if (startFollow)
		{
			setIntention(AI_INTENTION_FOLLOW, summon.getOwner());
		}
		else
		{
			super.onIntentionActive();
		}
	}

	@Override
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		switch (intention)
		{
			case AI_INTENTION_ACTIVE:
			case AI_INTENTION_FOLLOW:
				startAvoidTask();
				break;
			default:
				stopAvoidTask();
		}

		super.changeIntention(intention, arg0, arg1);
	}

	private void thinkAttack()
	{
		if (checkTargetLostOrDead(getAttackTarget()))
		{
			setAttackTarget(null);
			return;
		}
		if (maybeMoveToPawn(getAttackTarget(), actor.getPhysicalAttackRange()))
		{
			return;
		}
		clientStopMoving(null);
		accessor.doAttack(getAttackTarget());
	}

	private void thinkCast()
	{
		L2Summon summon = (L2Summon) actor;
		if (checkTargetLost(getCastTarget()))
		{
			setCastTarget(null);
			return;
		}
		boolean val = startFollow;
		if (maybeMoveToPawn(getCastTarget(), actor.getMagicalAttackRange(skill)))
		{
			return;
		}
		clientStopMoving(null);
		summon.setFollowStatus(false);
		setIntention(AI_INTENTION_IDLE);
		startFollow = val;
		accessor.doCast(skill, false);
	}

	private void thinkPickUp()
	{
		if (checkTargetLost(getTarget()))
		{
			return;
		}
		if (maybeMoveToPawn(getTarget(), 36))
		{
			return;
		}
		setIntention(AI_INTENTION_IDLE);
		((L2Summon.AIAccessor) accessor).doPickupItem(getTarget());
	}

	private void thinkInteract()
	{
		if (checkTargetLost(getTarget()))
		{
			return;
		}
		if (maybeMoveToPawn(getTarget(), 36))
		{
			return;
		}
		setIntention(AI_INTENTION_IDLE);
	}

	@Override
	protected void onEvtThink()
	{
		if (thinking || actor.isCastingNow() || actor.isAllSkillsDisabled())
		{
			return;
		}

		thinking = true;
		try
		{
			switch (getIntention())
			{
				case AI_INTENTION_ATTACK:
					thinkAttack();
					break;
				case AI_INTENTION_CAST:
					thinkCast();
					break;
				case AI_INTENTION_PICK_UP:
					thinkPickUp();
					break;
				case AI_INTENTION_INTERACT:
					thinkInteract();
					break;
			}
		}
		finally
		{
			thinking = false;
		}
	}

	@Override
	protected void onEvtFinishCasting()
	{
		boolean shouldFollow = attackTarget == null || !attackTarget.isAutoAttackable(((L2Summon) actor).getOwner());

		if (!actor.isMoving() && !actor.isAttackingNow())
		{
			shouldFollow = true;
		}

		if (shouldFollow)
		{
			((L2Summon) actor).setFollowStatus(startFollow);
		}
		else
		{
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attackTarget);
		}
	}

	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		super.onEvtAttacked(attacker);

		avoidAttack(attacker);
	}

	@Override
	protected void onEvtEvaded(L2Character attacker)
	{
		super.onEvtEvaded(attacker);

		avoidAttack(attacker);
	}

	private void avoidAttack(L2Character attacker)
	{
		// trying to avoid if summon near owner
		if (((L2Summon) actor).getOwner() != null && ((L2Summon) actor).getOwner() != attacker &&
				((L2Summon) actor).getOwner().isInsideRadius(actor, 2 * AVOID_RADIUS, true, false))
		{
			startAvoid = true;
		}
	}

	@Override
	public void run()
	{
		if (startAvoid)
		{
			startAvoid = false;

			if (!clientMoving && !actor.isDead() && !actor.isMovementDisabled())
			{
				final int ownerX = ((L2Summon) actor).getOwner().getX();
				final int ownerY = ((L2Summon) actor).getOwner().getY();
				final double angle =
						Math.toRadians(Rnd.get(-90, 90)) + Math.atan2(ownerY - actor.getY(), ownerX - actor.getX());

				final int targetX = ownerX + (int) (AVOID_RADIUS * Math.cos(angle));
				final int targetY = ownerY + (int) (AVOID_RADIUS * Math.sin(angle));
				if (Config.GEODATA == 0 || GeoData.getInstance()
						.canMoveFromToTarget(actor.getX(), actor.getY(), actor.getZ(), targetX, targetY,
								actor.getZ(), actor.getInstanceId()))
				{
					moveTo(targetX, targetY, actor.getZ());
				}
			}
		}
	}

	public void notifyFollowStatusChange()
	{
		switch (getIntention())
		{
			case AI_INTENTION_ACTIVE:
			case AI_INTENTION_FOLLOW:
			case AI_INTENTION_IDLE:
			case AI_INTENTION_MOVE_TO:
			case AI_INTENTION_PICK_UP:
				((L2Summon) actor).setFollowStatus(startFollow);
		}
	}

	public void setStartFollowController(boolean val)
	{
		startFollow = val;
	}

	public boolean getStartFollowController()
	{
		return startFollow;
	}

	@Override
	protected void onIntentionCast(L2Skill skill, L2Object target)
	{
		if (target instanceof L2Character && target.isAutoAttackable(((L2Summon) actor).getOwner()))
		{
			attackTarget = (L2Character) actor.getTarget();
		}

		super.onIntentionCast(skill, target);
	}

	private void startAvoidTask()
	{
		if (avoidTask == null)
		{
			avoidTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 100, 100);
		}
	}

	private void stopAvoidTask()
	{
		if (avoidTask != null)
		{
			avoidTask.cancel(false);
			avoidTask = null;
		}
	}

	@Override
	public void stopAITask()
	{
		stopAvoidTask();
		super.stopAITask();
	}
}
