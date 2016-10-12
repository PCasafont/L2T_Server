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
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.VisualEffect;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectTemplate;

/**
 * @author mkizub
 */
public class EffectLiftHold extends L2Effect
{
	public EffectLiftHold(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2AbnormalType getAbnormalType()
	{
		return L2AbnormalType.AERIAL_YOKE;
	}

	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2Attackable && getEffected().isImmobilized() ||
				getEffected().isRaid())
		{
			return false;
		}

		getEffected().setIsParalyzed(true);
		getEffected().startParalyze();
		getEffected().startVisualEffect(VisualEffect.S_LIFT_HOLD);
		return true;
	}

	@Override
	public void onExit()
	{
		getEffected().setIsParalyzed(false);
		getEffected().stopParalyze(false);
		getEffected().stopVisualEffect(VisualEffect.S_LIFT_HOLD);
	}

	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return true;
	}
}
