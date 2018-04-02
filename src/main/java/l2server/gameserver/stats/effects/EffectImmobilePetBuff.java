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
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectTemplate;

/**
 * @author demonia
 */
public class EffectImmobilePetBuff extends L2Effect {
	private Summon pet;

	public EffectImmobilePetBuff(Env env, EffectTemplate template) {
		super(env, template);
	}

	@Override
	public AbnormalType getAbnormalType() {
		return AbnormalType.BUFF;
	}

	@Override
	public boolean onStart() {
		pet = null;

		if (getEffected() instanceof Summon && getEffector() instanceof Player && ((Summon) getEffected()).getOwner() == getEffector()) {
			pet = (Summon) getEffected();
			pet.setIsImmobilized(true);
			return true;
		}
		return false;
	}

	@Override
	public void onExit() {
		pet.setIsImmobilized(false);
	}

	@Override
	public boolean onActionTime() {
		// just stop this effect
		return false;
	}
}
