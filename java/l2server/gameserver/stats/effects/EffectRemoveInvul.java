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

import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectTemplate;
import l2server.gameserver.templates.skills.L2EffectType;

public class EffectRemoveInvul extends L2Effect
{
	public EffectRemoveInvul(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2AbnormalType getAbnormalType()
	{
		return L2AbnormalType.DEBUFF;
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BLOCK_INVUL;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		if (!(getEffected() instanceof L2Playable))
		{
			return false;
		}

		if (getEffected().isInvul(getEffector()))
		{
			for (L2Abnormal e : getEffected().getAllEffects())
			{
				if (e == null)
				{
					continue;
				}

				for (L2Effect eff : e.getEffects())
				{
					if (eff == null)
					{
						continue;
					}

					if (eff.getEffectType() == L2EffectType.INVINCIBLE)
					{
						getEffected().onExitChanceEffect(e.getSkill(), e.getSkill().getElement());
						e.exit();
						break;
					}
				}
			}
		}

		return true;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		// Simply stop the effect
		return false;
	}
}
