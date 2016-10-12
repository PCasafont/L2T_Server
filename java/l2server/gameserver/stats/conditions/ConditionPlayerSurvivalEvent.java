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

import l2server.gameserver.events.instanced.EventInstance.EventType;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.Env;

public class ConditionPlayerSurvivalEvent extends Condition
{
	private final boolean _val;

	public ConditionPlayerSurvivalEvent(boolean val)
	{
		_val = val;
	}

	@Override
	public boolean testImpl(Env env)
	{
		final L2PcInstance player = env.player.getActingPlayer();
		if (player == null || !player.isPlayingEvent() ||
				!(player.getEvent().isType(EventType.Survival) && player.getEvent().isType(EventType.TeamSurvival)))
		{
			return !_val;
		}

		return _val;
	}
}
