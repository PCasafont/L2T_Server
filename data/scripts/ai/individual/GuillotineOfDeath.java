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
import l2server.gameserver.instancemanager.BossManager;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2RaidBossInstance;
import l2server.gameserver.network.serverpackets.ExShowUsmPacket;
import l2server.log.Log;
import l2server.util.Rnd;

/**
 * @author LasTravel
 *         <p>
 *         Source:
 *         - http://www.youtube.com/watch?v=L_ydM4ya6Rc
 */

public class GuillotineOfDeath extends L2AttackableAIScript
{
	private static final int firstBoss = 25888; //Execution Grounds Watchman Guillotine
	private static final int secondBoss = 25885; //Guillotine of Death
	private static final int thirdBoss = 25892; //Guillotine of Death
	private static final int strainId = 25893; //Strain minion
	//private static final int tumorId		= 0;	//Missing atm :/
	private static int bossStage = 0;
	private static L2RaidBossInstance firstBossInstance = null;

	private static final int[][] strainSpawns = {
			{46160, 155298, -1078, 25394},
			{45957, 156871, -1072, 38057},
			{44380, 157191, -1072, 53056},
			{43571, 155798, -1072, 64719},
			{44650, 154540, -1078, 13862}
	};

	public GuillotineOfDeath(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addSpawnId(firstBoss);
		addSpawnId(secondBoss);
		addAttackId(firstBoss);
		addAttackId(secondBoss);
		addAttackId(thirdBoss);
		addKillId(thirdBoss);

		L2RaidBossInstance boss = BossManager.getInstance().getBoss(firstBoss);

		if (boss != null) //boss is spawned
		{
			boss.setIsMortal(false);
		}
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (bossStage == 3 && npc.getNpcId() == thirdBoss)
		{
			//Update the first boss to killed
			firstBossInstance.doDie(killer);

			bossStage = 0;

			firstBossInstance = null;

			Log.info("GuillotineOfDeath AI: " + npc.getName() + ", has been killed by: " + killer.getName() + " at: " +
					System.currentTimeMillis());
		}

		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getNpcId() == firstBoss)
		{
			if (bossStage == 0 && npc.getCurrentHp() < npc.getMaxHp() * 0.05)
			{
				bossStage = 1;

				firstBossInstance = (L2RaidBossInstance) npc;

				//Spawns tumors here

				//Cast some skill to the boss

				npc.setIsInvul(true);

				npc.deleteMe();

				addSpawn(secondBoss, npc.getX(), npc.getY(), npc.getZ(), 0, false, 0, true);
			}
		}
		else if (npc.getNpcId() == secondBoss)
		{
			if (bossStage == 1 && npc.getCurrentHp() < npc.getMaxHp() * 0.05)
			{
				bossStage = 2;

				npc.setIsInvul(true);

				npc.deleteMe();

				addSpawn(thirdBoss, npc.getX(), npc.getY(), npc.getZ(), 0, false, 0, true);
			}
		}
		else if (npc.getNpcId() == thirdBoss)
		{
			if (bossStage == 2 && npc.getCurrentHp() < npc.getMaxHp() * 0.50)
			{
				bossStage = 3;

				npc.broadcastPacket(new ExShowUsmPacket(12));

				int[] rnd = null;

				for (int a = 0; a <= 50; a++)
				{
					rnd = strainSpawns[Rnd.get(strainSpawns.length)];

					addSpawn(strainId, rnd[0], rnd[1], rnd[2], rnd[3], false, 0, true);
				}
			}
		}

		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsMortal(false);

		return super.onSpawn(npc);
	}

	public static void main(String[] args)
	{
		new GuillotineOfDeath(-1, "GuillotineOfDeath", "ai");
	}
}
