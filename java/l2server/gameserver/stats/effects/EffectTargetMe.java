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
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2SiegeSummonInstance;
import l2server.gameserver.network.serverpackets.MyTargetSelected;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectTemplate;

/**
 * @author -Nemesiss-
 */
public class EffectTargetMe extends L2Effect
{
	public EffectTargetMe(Env env, L2EffectTemplate template)
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
		if (getEffected() instanceof L2Playable)
		{
			if (getEffected() instanceof L2SiegeSummonInstance)
			{
				return false;
			}

			if (getEffected() instanceof L2PcInstance && ((L2PcInstance) getEffected()).isCastingProtected())
			{
				return false;
			}

			if (getEffected().getTarget() != getEffector())
			{
				// Target is different
				getEffected().abortAttack();
				getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				getEffected().setTarget(getEffector());
				if (getEffected() instanceof L2PcInstance)
				{
					getEffected().sendPacket(new MyTargetSelected(getEffector().getObjectId(), 0));
				}
			}

			if (getAbnormal().getTemplate().duration > 0)
			{
				((L2Playable) getEffected()).setLockedTarget(getEffector());
			}

			return true;
		}
		else if (getEffected() instanceof L2Attackable && !getEffected().isRaid())
		{
			return true;
		}

		return false;
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
