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

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.util.Rnd;

import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 *         <p>
 *         Spicula Scout Golem AI
 */

public class SpiculaScoutGolem extends L2AttackableAIScript
{
	private static final int _spiculaScoutGolem = 23268;
	private static final int _golemGenerator = 19296;
	private static final int _battleGolem = 23269;

	public SpiculaScoutGolem(int id, String name, String descr)
	{
		super(id, name, descr);

		addKillId(_spiculaScoutGolem);
		addFirstTalkId(_golemGenerator);
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		npc.deleteMe();

		for (int a = 0; a < Rnd.get(2, 3); a++)
		{
			L2Npc golem = addSpawn(_battleGolem, npc.getX(), npc.getY(), npc.getZ(), 0, true, 120000);

			golem.setIsRunning(true);

			golem.setTarget(player);

			((L2MonsterInstance) golem).addDamageHate(player, 500, 99999);

			golem.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}

		return super.onFirstTalk(npc, player);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (Rnd.get(10) > 7)
		{
			addSpawn(_golemGenerator, npc.getX(), npc.getY(), npc.getZ(), 0, false, 120000, true);
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
		new SpiculaScoutGolem(-1, "SpiculaScoutGolem", "ai");
	}
}
