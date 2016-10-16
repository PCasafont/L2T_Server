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

package ai.group_template;

import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.SocialAction;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LasTravel
 */

public class TalkingIslandGuards extends L2AttackableAIScript
{
	private static final int generalId = 33007;
	private static final int guardId = 33018;
	private static int action = 0;
	private List<L2Npc> generals = new ArrayList<L2Npc>();
	private List<L2Npc> guards = new ArrayList<L2Npc>();

	public TalkingIslandGuards(int questId, String name, String descr)
	{
		super(questId, name, descr);

		findNpcs();
	}

	public void findNpcs()
	{
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable())
		{
			if (spawn != null)
			{
				if (spawn.getNpcId() == generalId)
				{
					generals.add(spawn.getNpc());
				}
				else if (spawn.getNpcId() == guardId)
				{
					guards.add(spawn.getNpc());
				}
			}
		}

		if (!guards.isEmpty() && !generals.isEmpty())
		{
			startQuestTimer("socialgeneral", 5000, null, null);
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.startsWith("socialgeneral"))
		{
			action = Rnd.get(12);

			for (L2Npc general : generals)
			{
				general.broadcastPacket(new SocialAction(general.getObjectId(), action));
			}

			startQuestTimer("socialguards", 2000, null, null);
		}
		else if (event.startsWith("socialguards"))
		{
			for (L2Npc guard : guards)
			{
				guard.broadcastPacket(new SocialAction(guard.getObjectId(), action));
			}

			startQuestTimer("socialgeneral", 8000, null, null);
		}

		return super.onAdvEvent(event, npc, player);
	}

	public static void main(String[] args)
	{
		new TalkingIslandGuards(-1, "TalkingIslandGuards", "ai");
	}
}
