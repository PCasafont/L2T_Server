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
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectTemplate;

public class EffectReset extends L2Effect
{
	public EffectReset(Env env, L2EffectTemplate template)
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
		L2Abnormal[] effects = getEffected().getAllEffects();
		if (effects == null || effects.length == 0)
		{
			return false;
		}

		for (L2Abnormal e : effects)
		{
			if (e == null || !e.getSkill().isOffensive())
			{
				continue;
			}

			// Devil's Sway: Resets the duration of the target's paralysis, hold, silence, sleep, shock, fear, petrification, and disarm.
			if (getSkill().getId() == 11095) //Devil's Sway
			{
				switch (e.getType())
				{
					case PARALYZE:
					case HOLD:
					case SILENCE:
					case SLEEP:
					case FEAR:
					case STUN:
					case PETRIFY:
					case DISARM:
						break;
					default:
						continue;
				}
			}

			//if (e.isRemovedOnDamage(true) || e.getType() == L2AbnormalType.AERIAL_YOKE)
			//	continue;

			e.exit();
			Env env = new Env();
			env.player = getEffector();
			env.target = getEffected();
			env.skill = e.getSkill();

			L2Abnormal ef = e.getTemplate().getEffect(env);
			if (ef != null)
			{
				ef.scheduleEffect();
			}
		}

		getEffected().broadcastAbnormalStatusUpdate();

		return true;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onExit()
	 */
	@Override
	public void onExit()
	{

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
