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

import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.EffectTemplate;

/**
 * @author DS
 * <p>
 * Effect will generate charges for Player targets
 * Number of charges in "value", maximum number in "count" effect variables
 */
public class EffectIncreaseCharges extends L2Effect {
	public EffectIncreaseCharges(Env env, EffectTemplate template) {
		super(env, template);
	}

	/**
	 * @see Abnormal#onStart()
	 */
	@Override
	public boolean onStart() {
		if (getEffected() == null) {
			return false;
		}

		if (!(getEffected() instanceof Player)) {
			return false;
		}

		int count = getAbnormal().getCount();
		if (count == 15) {
			if (((Player) getEffected()).getClassId() < 140) {
				count = 2;
			} else if (((Player) getEffected()).getClassId() != 152 && ((Player) getEffected()).getClassId() != 155) {
				count = 10;
			}
		}

		((Player) getEffected()).increaseCharges((int) calc(), count);

		return true;
	}

	/**
	 * @see Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime() {
		return false; // abort effect even if count > 1
	}
}
