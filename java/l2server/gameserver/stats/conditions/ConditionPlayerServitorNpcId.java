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

import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.stats.Env;

import java.util.ArrayList;

/**
 * The Class ConditionPlayerServitorNpcId.
 */
public class ConditionPlayerServitorNpcId extends Condition
{
	private final ArrayList<Integer> _npcIds;

	/**
	 * Instantiates a new condition player servitor npc id.
	 *
	 * @param npcIds the npc ids
	 */
	public ConditionPlayerServitorNpcId(ArrayList<Integer> npcIds)
	{
		if (npcIds.size() == 1 && npcIds.get(0) == 0)
		{
			_npcIds = null;
		}
		else
		{
			_npcIds = npcIds;
		}
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.stats.conditions.Condition#testImpl(l2server.gameserver.stats.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.player instanceof L2PcInstance))
		{
			return false;
		}

		L2PcInstance player = (L2PcInstance) env.player;

		if (player.getPet() == null)
		{
			return false;
		}

		boolean hasInSummons = false;
		if (_npcIds != null)
		{
			for (L2SummonInstance summon : player.getSummons())
			{
				if (!summon.isDead() && _npcIds.contains(summon.getNpcId()))
				{
					hasInSummons = true;
				}
			}
		}

		return _npcIds == null || _npcIds.contains(player.getPet().getNpcId()) || hasInSummons;
	}
}
