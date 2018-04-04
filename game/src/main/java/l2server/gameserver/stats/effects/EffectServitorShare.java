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

public class EffectServitorShare extends L2Effect {
	public EffectServitorShare(Env env, EffectTemplate template) {
		super(env, template);
	}
	
	@Override
	public AbnormalType getAbnormalType() {
		return AbnormalType.BUFF;
	}
	
	@Override
	public boolean onStart() {
		if (!(getEffected() instanceof Player)) {
			return false;
		}
		
		Player player = (Player) getEffected();
		for (Summon summon : player.getSummons()) {
			summon.updateAndBroadcastStatus(1);
		}
		
		return true;
	}
	
	@Override
	public boolean onActionTime() {
		return false;
	}
	
	@Override
	public void onExit() {
		if (!(getEffected() instanceof Player)) {
			return;
		}
		
		Player player = (Player) getEffected();
		if (player.getSummons().size() != 0) {
			for (Summon summon : player.getSummons()) {
				summon.updateAndBroadcastStatus(1);
			}
		}
	}
}
