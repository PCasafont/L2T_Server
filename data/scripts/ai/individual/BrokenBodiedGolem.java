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
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author LasTravel
 *         <p>
 *         Broken-bodied Golem AI
 */

public class BrokenBodiedGolem extends L2AttackableAIScript
{
	private static final int brokenGolem = 23259;
	private static final int summonedGolem = 23260;

	public BrokenBodiedGolem(int id, String name, String descr)
	{
		super(id, name, descr);

		addKillId(brokenGolem);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		for (int a = 0; a < 2; a++)
		{
			L2Npc minion = addSpawn(summonedGolem, killer.getX(), killer.getY(), killer.getZ(), 0, true, 60000, true);
			minion.setIsRunning(true);
			minion.setTarget(killer);
			((L2MonsterInstance) minion).addDamageHate(killer, 500, 99999);
			minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public int getOnKillDelay(int npcId)
	{
		return 0;
	}

	public static void main(String[] args)
	{
		new BrokenBodiedGolem(-1, "BrokenBodiedGolem", "ai");
	}
}
