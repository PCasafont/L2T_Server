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
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectTemplate;

public class EffectCombatPointHealOverTime extends L2Effect {
	public EffectCombatPointHealOverTime(Env env, EffectTemplate template) {
		super(env, template);
	}

	// Special constructor to steal this effect
	public EffectCombatPointHealOverTime(Env env, L2Effect effect) {
		super(env, effect);
	}

	/**
	 * @see Abnormal#effectCanBeStolen()
	 */
	@Override
	protected boolean effectCanBeStolen() {
		return true;
	}

	@Override
	public AbnormalType getAbnormalType() {
		return AbnormalType.HEAL_OVER_TIME;
	}

	/**
	 * @see Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime() {
		if (getEffected().isDead()) {
			return false;
		}

		double cp = getEffected().getCurrentCp();
		double maxcp = getEffected().getMaxCp();
		cp += calc();
		if (cp > maxcp) {
			cp = maxcp;
		}

		getEffected().setCurrentCp(cp);
		StatusUpdate sump = new StatusUpdate(getEffected());
		sump.addAttribute(StatusUpdate.CUR_CP, (int) cp);
		getEffected().sendPacket(sump);
		return true;
	}
}
