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
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Summon;
import l2server.util.Rnd;

import java.util.concurrent.Future;

import static l2server.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static l2server.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

public class SummonAI extends PlayableAI implements Runnable {
	private static final int AVOID_RADIUS = 70;

	private volatile boolean thinking; // to prevent recursive thinking
	private volatile boolean startFollow = ((Summon) actor).getFollowStatus();
	@SuppressWarnings("unused")
	private Creature lastAttack = null;

	private volatile boolean startAvoid = false;
	private Future<?> avoidTask = null;

	public SummonAI(Creature creature) {
		super(creature);
	}

	@Override
	protected void onIntentionIdle() {
		stopFollow();
		startFollow = false;
		onIntentionActive();
	}

	@Override
	protected void onIntentionActive() {
		Summon summon = (Summon) actor;
		if (startFollow) {
			setIntention(AI_INTENTION_FOLLOW, summon.getOwner());
		} else {
			super.onIntentionActive();
		}
	}

	@Override
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1) {
		switch (intention) {
			case AI_INTENTION_ACTIVE:
			case AI_INTENTION_FOLLOW:
				startAvoidTask();
				break;
			default:
				stopAvoidTask();
		}

		super.changeIntention(intention, arg0, arg1);
	}

	private void thinkAttack() {
		if (checkTargetLostOrDead(getAttackTarget())) {
			setAttackTarget(null);
			return;
		}
		if (maybeMoveToPawn(getAttackTarget(), actor.getPhysicalAttackRange())) {
			return;
		}
		clientStopMoving(null);
		actor.doAttack(getAttackTarget());
	}

	private void thinkCast() {
		Summon summon = (Summon) actor;
		if (checkTargetLost(getCastTarget())) {
			setCastTarget(null);
			return;
		}
		boolean val = startFollow;
		if (maybeMoveToPawn(getCastTarget(), actor.getMagicalAttackRange(skill))) {
			return;
		}
		clientStopMoving(null);
		summon.setFollowStatus(false);
		setIntention(AI_INTENTION_IDLE);
		startFollow = val;
		actor.doCast(skill, false);
	}

	private void thinkPickUp() {
		if (checkTargetLost(getTarget())) {
			return;
		}
		if (maybeMoveToPawn(getTarget(), 36)) {
			return;
		}
		setIntention(AI_INTENTION_IDLE);
		((Summon) actor).doPickupItem(getTarget());
	}

	private void thinkInteract() {
		if (checkTargetLost(getTarget())) {
			return;
		}
		if (maybeMoveToPawn(getTarget(), 36)) {
			return;
		}
		setIntention(AI_INTENTION_IDLE);
	}

	@Override
	protected void onEvtThink() {
		if (thinking || actor.isCastingNow() || actor.isAllSkillsDisabled()) {
			return;
		}

		thinking = true;
		try {
			switch (getIntention()) {
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
		} finally {
			thinking = false;
		}
	}

	@Override
	protected void onEvtFinishCasting() {
		boolean shouldFollow = attackTarget == null || !attackTarget.isAutoAttackable(((Summon) actor).getOwner());

		if (!actor.isMoving() && !actor.isAttackingNow()) {
			shouldFollow = true;
		}

		if (shouldFollow) {
			((Summon) actor).setFollowStatus(startFollow);
		} else {
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attackTarget);
		}
	}

	@Override
	protected void onEvtAttacked(Creature attacker) {
		super.onEvtAttacked(attacker);

		avoidAttack(attacker);
	}

	@Override
	protected void onEvtEvaded(Creature attacker) {
		super.onEvtEvaded(attacker);

		avoidAttack(attacker);
	}

	private void avoidAttack(Creature attacker) {
		// trying to avoid if summon near owner
		if (((Summon) actor).getOwner() != null && ((Summon) actor).getOwner() != attacker &&
				((Summon) actor).getOwner().isInsideRadius(actor, 2 * AVOID_RADIUS, true, false)) {
			startAvoid = true;
		}
	}

	@Override
	public void run() {
		if (startAvoid) {
			startAvoid = false;

			if (!clientMoving && !actor.isDead() && !actor.isMovementDisabled()) {
				final int ownerX = ((Summon) actor).getOwner().getX();
				final int ownerY = ((Summon) actor).getOwner().getY();
				final double angle = Math.toRadians(Rnd.get(-90, 90)) + Math.atan2(ownerY - actor.getY(), ownerX - actor.getX());

				final int targetX = ownerX + (int) (AVOID_RADIUS * Math.cos(angle));
				final int targetY = ownerY + (int) (AVOID_RADIUS * Math.sin(angle));
				if (Config.GEODATA == 0 || GeoData.getInstance()
						.canMoveFromToTarget(actor.getX(), actor.getY(), actor.getZ(), targetX, targetY, actor.getZ(), actor.getInstanceId())) {
					moveTo(targetX, targetY, actor.getZ());
				}
			}
		}
	}

	public void notifyFollowStatusChange() {
		switch (getIntention()) {
			case AI_INTENTION_ACTIVE:
			case AI_INTENTION_FOLLOW:
			case AI_INTENTION_IDLE:
			case AI_INTENTION_MOVE_TO:
			case AI_INTENTION_PICK_UP:
				((Summon) actor).setFollowStatus(startFollow);
		}
	}

	public void setStartFollowController(boolean val) {
		startFollow = val;
	}

	public boolean getStartFollowController() {
		return startFollow;
	}

	@Override
	protected void onIntentionCast(Skill skill, WorldObject target) {
		if (target instanceof Creature && target.isAutoAttackable(((Summon) actor).getOwner())) {
			attackTarget = (Creature) actor.getTarget();
		}

		super.onIntentionCast(skill, target);
	}

	private void startAvoidTask() {
		if (avoidTask == null) {
			avoidTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 100, 100);
		}
	}

	private void stopAvoidTask() {
		if (avoidTask != null) {
			avoidTask.cancel(false);
			avoidTask = null;
		}
	}

	@Override
	public void stopAITask() {
		stopAvoidTask();
		super.stopAITask();
	}
}
