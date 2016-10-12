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

import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.stats.Env;

/**
 * @author UnAfraid
 */
public class ConditionPlayerRangeFromNpc extends Condition
{
	private final int _npcId;
	private final int _radius;

	public ConditionPlayerRangeFromNpc(int npcId, int radius)
	{
		_npcId = npcId;
		_radius = radius;
	}

	@Override
	boolean testImpl(Env env)
	{
		if (_npcId == 0 || _radius == 0)
		{
			return false;
		}

		for (L2Character target : env.player.getKnownList().getKnownCharactersInRadius(_radius))
		{
			if (target instanceof L2Npc)
			{
				if (((L2Npc) target).getNpcId() == _npcId)
				{
					return true;
				}
			}
		}

		return false;
	}
}
