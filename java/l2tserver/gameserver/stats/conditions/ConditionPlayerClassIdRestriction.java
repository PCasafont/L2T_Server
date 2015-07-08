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

import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.stats.Env;

/**
 * The Class ConditionPlayerClassIdRestriction.
 */
public class ConditionPlayerClassIdRestriction extends Condition
{
	private final ArrayList<Integer> _classIds;
	
	/**
	 * Instantiates a new condition player class id restriction.
	 *
	 * @param classId the class id
	 */
	public ConditionPlayerClassIdRestriction(ArrayList<Integer> classId)
	{
		_classIds = classId;
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.stats.conditions.Condition#testImpl(l2tserver.gameserver.stats.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.player instanceof L2PcInstance))
			return false;
		return (_classIds.contains(((L2PcInstance)env.player).getCurrentClass().getId()));
	}
}
