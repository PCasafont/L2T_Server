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

import l2server.gameserver.model.L2Effect;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.VisualEffect;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectTemplate;
import l2server.gameserver.templates.skills.L2EffectType;

public class EffectParalyze extends L2Effect
{
	public EffectParalyze(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	// Special constructor to steal this effect
	public EffectParalyze(Env env, L2Effect effect)
	{
		super(env, effect);
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#effectCanBeStolen()
	 */
	@Override
	protected boolean effectCanBeStolen()
	{
		return true;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#getType()
	 */
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.PARALYZE;
	}

	@Override
	public L2AbnormalType getAbnormalType()
	{
		return L2AbnormalType.PARALYZE;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		if (getAbnormal() != null)
		{
			if (getAbnormal().getTemplate().visualEffect != null &&
					getAbnormal().getTemplate().visualEffect.length == 0)
			{
				getEffected().startVisualEffect(VisualEffect.HOLD_1);
			}
		}

		getEffected().startParalyze();
		return super.onStart();
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onExit()
	 */
	@Override
	public void onExit()
	{
		if (getAbnormal() != null)
		{
			if (getAbnormal().getTemplate().visualEffect != null &&
					getAbnormal().getTemplate().visualEffect.length == 0)
			{
				getEffected().stopVisualEffect(VisualEffect.HOLD_1);
			}
		}

		getEffected().stopParalyze(false);
		super.onExit();
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		return true;
	}
}
