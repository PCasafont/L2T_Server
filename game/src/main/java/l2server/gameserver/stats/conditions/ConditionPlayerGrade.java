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

import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.stats.Env;

/**
 * The Class ConditionPlayerGrade.
 *
 * @author Gigiikun
 */

public final class ConditionPlayerGrade extends Condition {
	//	conditional values
	public static final int COND_NO_GRADE = 0x0001;
	public static final int COND_D_GRADE = 0x0002;
	public static final int COND_C_GRADE = 0x0004;
	public static final int COND_B_GRADE = 0x0008;
	public static final int COND_A_GRADE = 0x0010;
	public static final int COND_S_GRADE = 0x0020;
	public static final int COND_S80_GRADE = 0x0040;
	public static final int COND_S84_GRADE = 0x0080;

	private final int value;

	/**
	 * Instantiates a new condition player grade.
	 *
	 * @param value the value
	 */
	public ConditionPlayerGrade(int value) {
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.stats.conditions.Condition#testImpl(l2server.gameserver.stats.Env)
	 */
	@Override
	public boolean testImpl(Env env) {
		if (env.player instanceof Player) {
			byte expIndex = (byte) ((Player) env.player).getExpertiseIndex();

			return value == expIndex;
		}
		return false;
	}
}
