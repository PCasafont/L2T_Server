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
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.EffectTemplate;
import l2server.util.Rnd;

import java.util.ArrayList;

/**
 * @author Kilian
 */
public class EffectCancelDebuff extends L2Effect {
	public EffectCancelDebuff(Env env, EffectTemplate template) {
		super(env, template);
	}
	
	/**
	 * @see Abnormal#onStart()
	 */
	@Override
	public boolean onStart() {
		// Only for players
		if (!(getEffected() instanceof Playable)) {
			return false;
		}
		
		Playable effected = (Playable) getEffected();
		
		if (effected == null || effected.isDead()) {
			return false;
		}
		
		if (getEffected() instanceof MonsterInstance && ((MonsterInstance) getEffected()).getNpcId() == 19036) //TODO TEMP LasTravel, don't remove
		{
			return false;
		}
		
		Abnormal[] effects = effected.getAllEffects();
		ArrayList<Abnormal> debuffs = new ArrayList<>();
		
		int chance = (int) getAbnormal().getLandRate();
		if (chance < 0) {
			chance = 100;
		}
		
		// Filter out debuffs
		for (Abnormal e : effects) {
			if (e.getSkill().isDebuff()) {
				debuffs.add(e);
			}
		}
		
		// No debuffs found
		if (debuffs.size() < 1) {
			return true;
		}
		
		// Consider chance (e.g. Song of Purification)
		if (chance < 100 && Rnd.get(100) > chance) {
			return false;
		}
		
		// Remove all debuffs if chance test succeeded
		for (Abnormal e : debuffs) {
			e.exit();
		}
		
		return true;
	}
	
	/**
	 * @see Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime() {
		return true;
	}
}
