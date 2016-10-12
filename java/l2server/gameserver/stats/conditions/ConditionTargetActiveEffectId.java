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
public class ConditionTargetActiveEffectId extends Condition
{

	private final int _effectId;
	private final int _effectLvl;

	/**
	 * Instantiates a new condition target active effect id.
	 *
	 * @param effectId the effect id
	 */
	public ConditionTargetActiveEffectId(int effectId)
	{
		_effectId = effectId;
		_effectLvl = -1;
	}

	/**
	 * Instantiates a new condition target active effect id.
	 *
	 * @param effectId    the effect id
	 * @param effectLevel the effect level
	 */
	public ConditionTargetActiveEffectId(int effectId, int effectLevel)
	{
		_effectId = effectId;
		_effectLvl = effectLevel;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.stats.conditions.Condition#testImpl(l2server.gameserver.stats.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		final L2Abnormal e = env.target.getFirstEffect(_effectId);
		return e != null && (_effectLvl == -1 || _effectLvl <= e.getSkill().getLevel());

	}
}
