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

import l2server.Config;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectTemplate;

/**
 * @author ZaKaX - nBd
 */
public class EffectHeavyPunch extends L2Effect
{
	public EffectHeavyPunch(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	public EffectHeavyPunch(Env env, L2Effect effect)
	{
		super(env, effect);
	}

	@Override
	public L2AbnormalType getAbnormalType()
	{
		return L2AbnormalType.HEAVY_PUNCH;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		if (!(getEffector() instanceof L2PcInstance))
		{
			return false;
		}

		L2PcInstance attacker = (L2PcInstance) getEffector();
		L2Character target = getEffected();

		int lastPhysicalDamages = attacker.getLastPhysicalDamages();

		int minDamageNeeded = attacker.getFirstEffect(30520) != null ? 300 : 150;

		if (lastPhysicalDamages < minDamageNeeded)
		{
			return false;
		}

		attacker.sendMessage("Heavy Punch is acting up.");

		double multiplier = 17.5;
		if (Config.isServer(Config.TENKAI))
		{
			multiplier = 4;
		}

		int damage = (int) (attacker.getLastPhysicalDamages() * multiplier *
				attacker.calcStat(Stats.PHYSICAL_SKILL_POWER, 1, target, null));

		if (Config.isServer(Config.TENKAI) && damage > 10000 && target.getActingPlayer() != null)
		{
			damage = 10000 + (int) Math.pow(damage - 10000, 0.9);
		}

		attacker.onHitTimer(target, damage, false, false, L2ItemInstance.CHARGED_SOULSHOT, (byte) 0, true);

		return true;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onExit()
	 */
	@Override
	public void onExit()
	{
		super.onExit();
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{

		return true;
	}
}
