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
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectTemplate;
import l2server.gameserver.templates.skills.EffectType;

/**
 * @author mkizub
 */
public class EffectRoot extends L2Effect {
	public EffectRoot(Env env, EffectTemplate template) {
		super(env, template);
	}

	@Override
	public EffectType getEffectType() {
		return EffectType.ROOT;
	}

	@Override
	public AbnormalType getAbnormalType() {
		return AbnormalType.HOLD;
	}

	@Override
	public boolean onStart() {
		getEffected().startRooted();
		return true;
	}

	@Override
	public void onExit() {
		getEffected().stopRooting(false);
	}

	@Override
	public boolean onActionTime() {
		return true;
	}
}
