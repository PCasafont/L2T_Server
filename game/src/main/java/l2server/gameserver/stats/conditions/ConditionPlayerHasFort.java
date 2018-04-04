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

import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.stats.Env;

/**
 * The Class ConditionPlayerHasFort.
 *
 * @author MrPoke
 */
public final class ConditionPlayerHasFort extends Condition {
	
	private final int fort;
	
	/**
	 * Instantiates a new condition player has fort.
	 *
	 * @param fort the fort
	 */
	public ConditionPlayerHasFort(int fort) {
		this.fort = fort;
	}
	
	/**
	 * Test impl.
	 *
	 * @param env the env
	 * @return true, if successful
	 * @see Condition#testImpl(Env)
	 */
	@Override
	public boolean testImpl(Env env) {
		if (!(env.player instanceof Player)) {
			return false;
		}
		
		L2Clan clan = ((Player) env.player).getClan();
		if (clan == null) {
			return fort == 0;
		}
		
		// Any fortress
		if (fort == -1) {
			return clan.getHasFort() > 0;
		}
		
		return clan.getHasFort() == fort;
	}
}
