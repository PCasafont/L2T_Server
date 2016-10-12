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
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2EffectTemplate;

public class EffectUnSummon extends L2Effect
{
	public EffectUnSummon(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		if (getEffected() == null)
		{
			return false;
		}

		L2Character target = getEffected();
		if (target instanceof L2PcInstance)
		{
			target = ((L2PcInstance) target).getSummon(0);
		}

		if (!(target instanceof L2SummonInstance))
		{
			return false;
		}

		L2SummonInstance summon = (L2SummonInstance) target;
		if (summon.isDead())
		{
			return false;
		}

		summon.unSummon((L2PcInstance) getEffector());

		return true;
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
