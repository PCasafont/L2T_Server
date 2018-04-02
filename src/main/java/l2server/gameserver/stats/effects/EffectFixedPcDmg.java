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
import l2server.gameserver.stats.Stats;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectTemplate;

public class EffectFixedPcDmg extends L2Effect {
	public EffectFixedPcDmg(Env env, EffectTemplate template) {
		super(env, template);
	}

	@Override
	public AbnormalType getAbnormalType() {
		return AbnormalType.DEBUFF;
	}

	/**
	 * @see Abnormal#onActionTime()
	 */
	@Override
	public boolean onStart() {
		if (getEffected().isDead() || !(getEffected() instanceof Player) || getEffected().getLevel() < 85 || getEffected().isInvul()) {
			return false;
		}

		double damage = calc();
		damage = getEffected().calcStat(Stats.FIXED_DMG_VULN, damage, getEffector(), getSkill());
		((Player) getEffected()).getStatus().reduceHp(damage, getEffector(), true, false, false, true, getSkill().ignoreImmunity());
		getEffector().sendDamageMessage(getEffected(), (int) damage, false, false, false);
		return true;
	}

	@Override
	public boolean onActionTime() {
		return false;
	}
}
