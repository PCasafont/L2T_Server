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

package l2server.gameserver.model.actor.instance;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.util.Rnd;

import java.util.concurrent.ScheduledFuture;

/**
 * A clone.
 *
 * @author ZaKaX
 */
public class L2CloneInstance extends L2SummonInstance {
	private ScheduledFuture<?> cloneTask;

	public L2CloneInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2Skill skill) {
		super(objectId, template, owner, skill);

		setInstanceType(InstanceType.L2CloneInstance);
	}

	@Override
	public void onSpawn() {
		super.onSpawn();

		// Schedule the party look-up task every 60seconds.
		if (cloneTask == null) {
			cloneTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CloneTask(this), 1000, 1000);
		}
	}

	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill) {
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}

	@Override
	public boolean doDie(L2Character killer) {
		if (!super.doDie(killer)) {
			return false;
		}

		// Cancel the attackers-check task.
		if (cloneTask != null) {
			cloneTask.cancel(true);
			cloneTask = null;
		}

		return true;
	}

	@Override
	public void unSummon(L2PcInstance owner) {
		if (cloneTask != null) {
			cloneTask.cancel(true);
			cloneTask = null;
		}

		super.unSummon(owner);
	}

	private static final class CloneTask implements Runnable {
		private final L2CloneInstance clone;

		CloneTask(final L2CloneInstance clone) {
			this.clone = clone;
		}

		@Override
		public final void run() {
			final L2PcInstance owner = clone.getOwner();

			if (!clone.isVisible()) {
				return;
			}

			if (owner.isAttackingNow()) {
				if (owner.getTarget() != clone.getTarget() || !clone.isAttackingNow() && !clone.isCastingNow()) {
					clone.setTarget(owner.getTarget());

					if (clone.getTarget() != null && clone.getTarget().isAutoAttackable(owner)) {
						clone.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, clone.getTarget());
					}
				}
			} else if (!clone.isAttackingNow() && !clone.isCastingNow()) {
				clone.followOwner();
			}

			for (L2Skill skill : clone.getAllSkills()) {
				if (Rnd.nextBoolean()) {
					continue;
				} else if (clone.isSkillDisabled(skill.getReuseHashCode())) {
					continue;
				}

				int reuseDelay = skill.getReuseDelay();

				if (Rnd.nextBoolean()) {
					reuseDelay *= 2;
				}

				clone.useMagic(skill, false, false);

				clone.disableSkill(skill, reuseDelay);
			}
		}
	}
}
