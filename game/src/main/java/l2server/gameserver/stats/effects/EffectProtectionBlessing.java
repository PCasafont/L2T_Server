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
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectTemplate;
import l2server.gameserver.templates.skills.EffectType;

/**
 * @author kerberos_20
 */
public class EffectProtectionBlessing extends L2Effect {
	public EffectProtectionBlessing(Env env, EffectTemplate template) {
		super(env, template);
	}

	@Override
	public EffectType getEffectType() {
		return EffectType.PROTECTION_BLESSING;
	}

	@Override
	public AbnormalType getAbnormalType() {
		return AbnormalType.BUFF;
	}

	/**
	 * Notify started
	 */
	@Override
	public boolean onStart() {
		return false;
	}

	/**
	 * Notify exited
	 */
	@Override
	public void onExit() {
		((Playable) getEffected()).stopProtectionBlessing(getAbnormal());
	}

	@Override
	public boolean onActionTime() {
		// just stop this effect
		return false;
	}
}
