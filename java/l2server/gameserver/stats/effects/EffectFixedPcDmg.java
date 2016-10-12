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
import l2server.gameserver.stats.Stats;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectTemplate;

public class EffectFixedPcDmg extends L2Effect
{
	public EffectFixedPcDmg(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2AbnormalType getAbnormalType()
	{
		return L2AbnormalType.DEBUFF;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onStart()
	{
		if (getEffected().isDead() || !(getEffected() instanceof L2PcInstance) || getEffected().getLevel() < 85 ||
				getEffected().isInvul())
		{
			return false;
		}

		double damage = calc();
		damage = getEffected().calcStat(Stats.FIXED_DMG_VULN, damage, getEffector(), getSkill());
		((L2PcInstance) getEffected()).getStatus()
				.reduceHp(damage, getEffector(), true, false, false, true, getSkill().ignoreImmunity());
		getEffector().sendDamageMessage(getEffected(), (int) damage, false, false, false);
		return true;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
