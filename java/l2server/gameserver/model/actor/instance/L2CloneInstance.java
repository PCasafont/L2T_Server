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
public class L2CloneInstance extends L2SummonInstance
{
	private ScheduledFuture<?> _cloneTask;

	public L2CloneInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2Skill skill)
	{
		super(objectId, template, owner, skill);

		setInstanceType(InstanceType.L2CloneInstance);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();

		// Schedule the party look-up task every 60seconds.
		if (_cloneTask == null)
		{
			_cloneTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CloneTask(this), 1000, 1000);
		}
	}

	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}

		// Cancel the attackers-check task.
		if (_cloneTask != null)
		{
			_cloneTask.cancel(true);
			_cloneTask = null;
		}

		return true;
	}

	@Override
	public void unSummon(L2PcInstance owner)
	{
		if (_cloneTask != null)
		{
			_cloneTask.cancel(true);
			_cloneTask = null;
		}

		super.unSummon(owner);
	}

	private static final class CloneTask implements Runnable
	{
		private final L2CloneInstance _clone;

		CloneTask(final L2CloneInstance clone)
		{
			_clone = clone;
		}

		@Override
		public final void run()
		{
			final L2PcInstance owner = _clone.getOwner();

			if (!_clone.isVisible())
			{
				return;
			}

			if (owner.isAttackingNow())
			{
				if (owner.getTarget() != _clone.getTarget() || !_clone.isAttackingNow() && !_clone.isCastingNow())
				{
					_clone.setTarget(owner.getTarget());

					if (_clone.getTarget() != null && _clone.getTarget().isAutoAttackable(owner))
					{
						_clone.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _clone.getTarget());
					}
				}
			}
			else if (!_clone.isAttackingNow() && !_clone.isCastingNow())
			{
				_clone.followOwner();
			}

			for (L2Skill skill : _clone.getAllSkills())
			{
				if (Rnd.nextBoolean())
				{
					continue;
				}
				else if (_clone.isSkillDisabled(skill.getReuseHashCode()))
				{
					continue;
				}

				int reuseDelay = skill.getReuseDelay();

				if (Rnd.nextBoolean())
				{
					reuseDelay *= 2;
				}

				_clone.useMagic(skill, false, false);

				_clone.disableSkill(skill, reuseDelay);
			}
		}
	}
}
