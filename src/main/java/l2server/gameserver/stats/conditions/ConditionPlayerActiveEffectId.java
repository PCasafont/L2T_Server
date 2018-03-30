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
 * The Class ConditionPlayerActiveEffectId.
 */
public class ConditionPlayerActiveEffectId extends Condition {

	private final int effectId;
	private final int effectLvl;

	/**
	 * Instantiates a new condition player active effect id.
	 *
	 * @param effectId the effect id
	 */
	public ConditionPlayerActiveEffectId(int effectId) {
		this.effectId = effectId;
		effectLvl = -1;
	}

	/**
	 * Instantiates a new condition player active effect id.
	 *
	 * @param effectId    the effect id
	 * @param effectLevel the effect level
	 */
	public ConditionPlayerActiveEffectId(int effectId, int effectLevel) {
		this.effectId = effectId;
		effectLvl = effectLevel;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.stats.conditions.Condition#testImpl(l2server.gameserver.stats.Env)
	 */
	@Override
	public boolean testImpl(Env env) {
		final L2Abnormal e = env.player.getFirstEffect(effectId);
		return e != null && (effectLvl == -1 || effectLvl <= e.getSkill().getLevel());
	}
}
