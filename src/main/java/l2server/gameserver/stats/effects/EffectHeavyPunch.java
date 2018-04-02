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
import l2server.gameserver.model.Item;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectTemplate;

/**
 * @author ZaKaX - nBd
 */
public class EffectHeavyPunch extends L2Effect {
	public EffectHeavyPunch(Env env, EffectTemplate template) {
		super(env, template);
	}

	public EffectHeavyPunch(Env env, L2Effect effect) {
		super(env, effect);
	}

	@Override
	public AbnormalType getAbnormalType() {
		return AbnormalType.HEAVY_PUNCH;
	}

	/**
	 * @see Abnormal#onStart()
	 */
	@Override
	public boolean onStart() {
		if (!(getEffector() instanceof Player)) {
			return false;
		}

		Player attacker = (Player) getEffector();
		Creature target = getEffected();

		int lastPhysicalDamages = attacker.getLastPhysicalDamages();

		int minDamageNeeded = attacker.getFirstEffect(30520) != null ? 300 : 150;

		if (lastPhysicalDamages < minDamageNeeded) {
			return false;
		}

		attacker.sendMessage("Heavy Punch is acting up.");

		double multiplier = 17.5;

		multiplier = 17;

		int damage = (int) (attacker.getLastPhysicalDamages() * multiplier * attacker.calcStat(Stats.PHYSICAL_SKILL_POWER, 1, target, null));

		if (damage > 10000 && target.getActingPlayer() != null) {
			damage = 10000 + (int) Math.pow(damage - 10000, 0.9);
		}

		attacker.onHitTimer(target, damage, false, false, Item.CHARGED_SOULSHOT, (byte) 0, true);

		return true;
	}

	/**
	 * @see Abnormal#onExit()
	 */
	@Override
	public void onExit() {
		super.onExit();
	}

	/**
	 * @see Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime() {

		return true;
	}
}
