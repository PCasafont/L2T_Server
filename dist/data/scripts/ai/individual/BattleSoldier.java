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

import l2server.gameserver.ai.L2NpcWalkerAI;
import l2server.gameserver.model.L2NpcWalkerNode;
import l2server.gameserver.model.actor.L2Npc;

import ai.group_template.L2AttackableAIScript;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LasTravel
 */

public class BattleSoldier extends L2AttackableAIScript
{
	private static final int BATTLE_SOLDIER = 33119;

	private static final int[][] WALK_ROUTE = {
			{-115692, 235946, -3091}, {-115692, 235946, -3091}, //1
			{-114140, 235556, -3091}, //2
			{-113477, 236672, -3045}, //3
			{-113600, 237167, -3045} //4
	};

	private static List<L2NpcWalkerNode> _route = new ArrayList<L2NpcWalkerNode>();

	public BattleSoldier(int id, String name, String descr)
	{
		super(id, name, descr);

		addSpawnId(BATTLE_SOLDIER);

		addEventId(BATTLE_SOLDIER, QuestEventType.ON_ARRIVED);

		for (int[] coord : WALK_ROUTE)
		{
			_route.add(new L2NpcWalkerNode(coord[0], coord[1], coord[2], 0, "", true));
		}

		addSpawn(BATTLE_SOLDIER, -115199, 237369, -3088, 0, true, 0);
	}

	@Override
	public String onArrived(final L2NpcWalkerAI guideAI)
	{
		if (guideAI.getCurrentPos() == 4)
		{
			guideAI.getActor().decayMe();
			addSpawn(BATTLE_SOLDIER, -115199, 237369, -3088, 0, true, 0);
		}
		else
		{
			guideAI.walkToLocation();
			guideAI.setWaiting(false);
		}
		return null;
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		L2NpcWalkerAI _battleSoldierAI = new L2NpcWalkerAI(npc);

		npc.setAI(_battleSoldierAI);

		npc.setIsInvul(true);

		_battleSoldierAI.initializeRoute(_route, null);

		_battleSoldierAI.walkToLocation();

		return super.onSpawn(npc);
	}

	public static void main(String[] args)
	{
		new BattleSoldier(-1, "BattleSoldier", "ai");
	}
}
