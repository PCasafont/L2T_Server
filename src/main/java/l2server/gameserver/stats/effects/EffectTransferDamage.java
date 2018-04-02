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
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.EffectTemplate;

/**
 * @author UnAfraid
 */

public class EffectTransferDamage extends L2Effect {
	public EffectTransferDamage(Env env, EffectTemplate template) {
		super(env, template);
	}
	
	public EffectTransferDamage(Env env, L2Effect effect) {
		super(env, effect);
	}
	
	/**
	 * @see Abnormal#onStart()
	 */
	@Override
	public boolean onStart() {
		if (getEffected() instanceof Playable && getEffector() instanceof Player) {
			((Playable) getEffected()).setTransferDamageTo((Player) getEffector());
		}
		return true;
	}
	
	/**
	 * @see Abnormal#onExit()
	 */
	@Override
	public void onExit() {
		if (getEffected() instanceof Playable && getEffector() instanceof Player) {
			((Playable) getEffected()).setTransferDamageTo(null);
		}
	}
	
	/**
	 * @see Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime() {
		return false;
	}
}
