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
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2EffectTemplate;

/**
 * @author DS
 *         <p>
 *         Effect will generate charges for L2PcInstance targets
 *         Number of charges in "value", maximum number in "count" effect variables
 */
public class EffectIncreaseCharges extends L2Effect
{
	public EffectIncreaseCharges(Env env, L2EffectTemplate template)
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

		if (!(getEffected() instanceof L2PcInstance))
		{
			return false;
		}

		int count = getAbnormal().getCount();
		if (count == 15)
		{
			if (((L2PcInstance) getEffected()).getClassId() < 140)
			{
				count = 2;
			}
			else if (((L2PcInstance) getEffected()).getClassId() != 152 &&
					((L2PcInstance) getEffected()).getClassId() != 155)
			{
				count = 10;
			}
		}

		((L2PcInstance) getEffected()).increaseCharges((int) calc(), count);

		return true;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		return false; // abort effect even if count > 1
	}
}
