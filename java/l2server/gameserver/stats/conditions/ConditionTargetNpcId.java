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

import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2DoorInstance;
import l2server.gameserver.stats.Env;

import java.util.ArrayList;

/**
 * The Class ConditionTargetNpcId.
 */
public class ConditionTargetNpcId extends Condition
{
	private final ArrayList<Integer> _npcIds;

	/**
	 * Instantiates a new condition target npc id.
	 *
	 * @param npcIds the npc ids
	 */
	public ConditionTargetNpcId(ArrayList<Integer> npcIds)
	{
		_npcIds = npcIds;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.stats.conditions.Condition#testImpl(l2server.gameserver.stats.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		if (env.target instanceof L2Npc)
		{
			return _npcIds.contains(((L2Npc) env.target).getNpcId());
		}

		if (env.target instanceof L2DoorInstance)
		{
			return _npcIds.contains(((L2DoorInstance) env.target).getDoorId());
		}

		return false;
	}
}
