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

package l2server.gameserver.stats.effects;

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectTemplate;
import l2server.gameserver.templates.skills.L2SkillType;

/**
 * @author -Nemesiss-
 */
public class EffectRemoveTarget extends L2Effect
{
	public EffectRemoveTarget(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2AbnormalType getAbnormalType()
	{
		return L2AbnormalType.DEBUFF;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2PcInstance && ((L2PcInstance) getEffected()).isCastingProtected())
		{
			return false;
		}

		if (getEffected().isRaid())
		{
			return false;
		}

		if (getEffected() instanceof L2MonsterInstance &&
				((L2MonsterInstance) getEffected()).getNpcId() == 19036) //TODO TEMP LasTravel, don't remove
		{
			return false;
		}

		if (getEffected() instanceof L2Playable && getEffected().isCastingNow())
		{
			//chat what is casting, if its a self skill defined as a buff return?
			L2Skill lastSkillCast = getEffected().getLastSkillCast();
			if (lastSkillCast != null)
			{
				if (lastSkillCast.getSkillType() == L2SkillType.BUFF)
				{
					return false;
				}
			}

			L2Skill lastSimultaneouSkillCast = getEffected().getLastSimultaneousSkillCast();
			if (lastSimultaneouSkillCast != null)
			{
				if (lastSimultaneouSkillCast.getSkillType() == L2SkillType.BUFF)
				{
					return false;
				}
			}
		}

		getEffected().setTarget(null);
		getEffected().abortAttack();
		// It should not cancel the cast
		//getEffected().abortCast();
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, getEffector());

		if (getEffected() instanceof L2Playable && getAbnormal().getTemplate().duration > 0 &&
				getSkill().getId() != 10265)
		{
			((L2Playable) getEffected()).setLockedTarget(getEffected());
		}

		return true;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onExit()
	 */
	@Override
	public void onExit()
	{
		if (getEffected() instanceof L2Playable)
		{
			((L2Playable) getEffected()).setLockedTarget(null);
		}
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		// nothing
		return false;
	}
}
