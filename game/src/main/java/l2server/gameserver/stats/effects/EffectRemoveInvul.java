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
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectTemplate;
import l2server.gameserver.templates.skills.EffectType;

public class EffectRemoveInvul extends L2Effect {
	public EffectRemoveInvul(Env env, EffectTemplate template) {
		super(env, template);
	}
	
	@Override
	public AbnormalType getAbnormalType() {
		return AbnormalType.DEBUFF;
	}
	
	@Override
	public EffectType getEffectType() {
		return EffectType.BLOCK_INVUL;
	}
	
	/**
	 * @see Abnormal#onStart()
	 */
	@Override
	public boolean onStart() {
		if (!(getEffected() instanceof Playable)) {
			return false;
		}
		
		if (getEffected().isInvul(getEffector())) {
			for (Abnormal e : getEffected().getAllEffects()) {
				if (e == null) {
					continue;
				}
				
				for (L2Effect eff : e.getEffects()) {
					if (eff == null) {
						continue;
					}
					
					if (eff.getEffectType() == EffectType.INVINCIBLE) {
						getEffected().onExitChanceEffect(e.getSkill(), e.getSkill().getElement());
						e.exit();
						break;
					}
				}
			}
		}
		
		return true;
	}
	
	/**
	 * @see Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime() {
		// Simply stop the effect
		return false;
	}
}
