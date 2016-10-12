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
import l2server.gameserver.templates.skills.L2EffectTemplate;

public class EffectTalismanPower extends L2Effect
{
	public EffectTalismanPower(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		switch (getSkill().getId())
		{
			case 13385:
			case 13386:
				getEffected().startVisualEffect(VisualEffect.TALISMAN_POWER1);
				break;
			case 13387:
				getEffected().startVisualEffect(VisualEffect.TALISMAN_POWER2);
				break;
			case 13388:
				getEffected().startVisualEffect(VisualEffect.TALISMAN_POWER3);
				break;
			case 13389:
				getEffected().startVisualEffect(VisualEffect.TALISMAN_POWER4);
				break;
			case 17978:
				getEffected().startVisualEffect(VisualEffect.TALISMAN_POWER5);
				break;
		}

		return true;
	}

	@Override
	public void onExit()
	{
		getEffected().stopVisualEffect(VisualEffect.TALISMAN_POWER1);
		getEffected().stopVisualEffect(VisualEffect.TALISMAN_POWER2);
		getEffected().stopVisualEffect(VisualEffect.TALISMAN_POWER3);
		getEffected().stopVisualEffect(VisualEffect.TALISMAN_POWER4);
		getEffected().stopVisualEffect(VisualEffect.TALISMAN_POWER5);
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
