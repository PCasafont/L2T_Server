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

package l2server.gameserver.stats.conditions;

import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.stats.Env;

/**
 * The Class ConditionTargetActiveEffectId.
 */
public class ConditionTargetActiveEffect extends Condition
{

	private final String _effectName;

	/**
	 * Instantiates a new condition target active effect id.
	 */
	public ConditionTargetActiveEffect(String effectName)
	{
		_effectName = effectName;
	}

	@Override
	public boolean testImpl(Env env)
	{
		final L2Abnormal e = env.target.getFirstEffectByName(_effectName);
		return e != null;

	}
}
