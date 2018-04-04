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
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectTemplate;

public class EffectManaHealOverTime extends L2Effect {
	public EffectManaHealOverTime(Env env, EffectTemplate template) {
		super(env, template);
	}

	// Special constructor to steal this effect
	public EffectManaHealOverTime(Env env, L2Effect effect) {
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
		return AbnormalType.BUFF;
	}

	/**
	 * @see Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime() {
		if (getEffected().isDead() || getEffected() instanceof Player && ((Player) getEffected()).getCurrentClass().getId() == 146) {
			return false;
		}

		if (getEffected().calcStat(Stats.MANA_SHIELD_PERCENT, 0, getEffected(), null) > 0) {
			return false;
		}

		double mp = getEffected().getCurrentMp();
		double maxmp = getEffected().getMaxMp();
		mp += calc();
		if (mp > maxmp) {
			mp = maxmp;
		}

		getEffected().setCurrentMp(mp);
		StatusUpdate sump = new StatusUpdate(getEffected());
		sump.addAttribute(StatusUpdate.CUR_MP, (int) mp);
		getEffected().sendPacket(sump);
		return true;
	}
}
