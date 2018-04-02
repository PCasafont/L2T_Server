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

package l2server.gameserver.stats.conditions;

import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.SummonInstance;
import l2server.gameserver.stats.Env;

import java.util.ArrayList;

/**
 * The Class ConditionPlayerServitorNpcId.
 */
public class ConditionPlayerServitorNpcId extends Condition {
	private final ArrayList<Integer> npcIds;

	/**
	 * Instantiates a new condition player servitor npc id.
	 *
	 * @param npcIds the npc ids
	 */
	public ConditionPlayerServitorNpcId(ArrayList<Integer> npcIds) {
		if (npcIds.size() == 1 && npcIds.get(0) == 0) {
			this.npcIds = null;
		} else {
			this.npcIds = npcIds;
		}
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.stats.conditions.Condition#testImpl(l2server.gameserver.stats.Env)
	 */
	@Override
	public boolean testImpl(Env env) {
		if (!(env.player instanceof Player)) {
			return false;
		}

		Player player = (Player) env.player;

		if (player.getPet() == null) {
			return false;
		}

		boolean hasInSummons = false;
		if (npcIds != null) {
			for (SummonInstance summon : player.getSummons()) {
				if (!summon.isDead() && npcIds.contains(summon.getNpcId())) {
					hasInSummons = true;
				}
			}
		}

		return npcIds == null || npcIds.contains(player.getPet().getNpcId()) || hasInSummons;
	}
}
