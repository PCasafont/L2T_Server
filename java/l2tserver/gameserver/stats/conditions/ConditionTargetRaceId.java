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
package l2tserver.gameserver.stats.conditions;

import java.util.ArrayList;

import l2tserver.gameserver.model.actor.L2Npc;
import l2tserver.gameserver.stats.Env;

/**
 * The Class ConditionTargetRaceId.
 *
 * @author nBd
 */

public class ConditionTargetRaceId extends Condition
{
	private final ArrayList<Integer> _raceIds;
	
	/**
	 * Instantiates a new condition target race id.
	 *
	 * @param raceId the race id
	 */
	public ConditionTargetRaceId(ArrayList<Integer> raceId)
	{
		_raceIds = raceId;
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.stats.conditions.Condition#testImpl(l2tserver.gameserver.stats.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.target instanceof L2Npc))
			return false;
		return (_raceIds.contains(((L2Npc)env.target).getTemplate().getRace().ordinal()+1));
	}
}
