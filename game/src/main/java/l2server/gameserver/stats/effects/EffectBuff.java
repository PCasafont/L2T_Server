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
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectTemplate;

/**
 * @author mkizub
 */
public class EffectBuff extends L2Effect {
	public EffectBuff(Env env, EffectTemplate template) {
		super(env, template);
	}
	
	// Special constructor to steal this effect
	public EffectBuff(Env env, L2Effect effect) {
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
	
	@Override
	public boolean onActionTime() {
		// just stop this effect
		return false;
	}
	
	/**
	 * @see Abnormal#onExit()
	 */
	@Override
	public void onExit() {
		super.onExit();
		/*if (getEffector() != null && getEffector() instanceof MonsterInstance)
        {
			if (!getEffector().isInCombat())
				getEffector().doCast(getSkill());
		}*/
	}
}
