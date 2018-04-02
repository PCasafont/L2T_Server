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
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.EffectTemplate;
import l2server.gameserver.templates.skills.SkillTargetType;

public class EffectAggroReduce extends L2Effect {
	public EffectAggroReduce(Env env, EffectTemplate template) {
		super(env, template);
	}
	
	/**
	 * @see Abnormal#onStart()
	 */
	@Override
	public boolean onStart() {
		//Works only on mobz
		if (!(getEffected() instanceof Attackable)) {
			return false;
		}
		
		if (getSkill().getTargetType() != SkillTargetType.TARGET_UNDEAD || getEffected().isUndead()) {
			((Attackable) getEffected()).reduceHate(null, ((Attackable) getEffected()).getHating(((Attackable) getEffected()).getMostHated()));
		}
		
		return true;
	}
	
	/**
	 * @see Abnormal#onExit()
	 */
	@Override
	public void onExit() {
	
	}
	
	/**
	 * @see Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime() {
		// nothing
		return false;
	}
}
