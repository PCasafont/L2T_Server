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

package ai.individual;

import ai.group_template.L2AttackableAIScript;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;

/**
 * @author LasTravel
 */

public class SpiculaCaptain extends L2AttackableAIScript
{
	private static final int _captain = 23275;

	public SpiculaCaptain(int id, String name, String descr)
	{
		super(id, name, descr);

		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable())
		{
			if (spawn == null)
			{
				continue;
			}

			if (spawn.getNpcId() == _captain)
			{
				spawn.getNpc().setShowSummonAnimation(true);

				for (L2MonsterInstance a : ((L2MonsterInstance) spawn.getNpc()).getMinionList().getSpawnedMinions())
				{
					a.setShowSummonAnimation(true);
				}

				break;
			}
		}
	}

	public static void main(String[] args)
	{
		new SpiculaCaptain(-1, "SpiculaCaptain", "ai");
	}
}
