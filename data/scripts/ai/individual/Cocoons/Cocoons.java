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

package ai.individual.Cocoons;

import ai.group_template.L2AttackableAIScript;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.util.Rnd;

/**
 * @author LasTravel
 *         <p>
 *         Cocoons AI
 *         <p>
 *         Source:
 *         - http://l2wiki.com/Large_Cocoon
 */

public class Cocoons extends L2AttackableAIScript
{
	private static final int cocoon = 32919;
	private static final int largeCocoon = 32920;
	private static final int contaminatedLargeCocoon = 19394;
	private static final int contaminatedCocoon = 19393;

	private static final int[] normalMobs = {22863, 22879, 22903, 22895, 22887, 22871};
	private static final int[] wickedMobs = {22864, 22880, 22904, 22896, 22888, 22872};
	private static final int[] violentMobs = {22867, 22883, 22907, 22899, 22891, 22875};
	private static final int[] brutalMobs = {22868, 22884, 22908, 22900, 22892, 22876};
	private static final int[] slightlyMobs = {22870, 22886, 22910, 22902, 22894, 22878};

	public Cocoons(int id, String name, String descr)
	{
		super(id, name, descr);

		for (int a = cocoon; a <= largeCocoon; a++)
		{
			addAttackId(a);
			addStartNpc(a);
			addTalkId(a);
			addFirstTalkId(a);
			addSpawnId(a);
		}

		addSpawnId(contaminatedCocoon);

		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable())
		{
			if (spawn == null)
			{
				continue;
			}

			if (spawn.getNpcId() == cocoon || spawn.getNpcId() == largeCocoon)
			{
				notifySpawn(spawn.getNpc());
			}
		}
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		npc.setIsImmobilized(true);

		return super.onSpawn(npc);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		int mobs[] = null;
		if (event.equalsIgnoreCase("normalAttack"))
		{
			if (!npc.isDead() && !npc.isDecayed())
			{
				npc.doDie(null);
				if (npc.getNpcId() == cocoon)
				{
					if (Rnd.get(10) > 7)
					{
						addSpawn(contaminatedCocoon, npc.getX(), npc.getY(), npc.getZ(), 0, false, 0);
						mobs = wickedMobs;
					}
					else
					{
						mobs = normalMobs;
					}
				}
				else
				{
					if (Rnd.get(10) > 7)
					{
						addSpawn(contaminatedLargeCocoon, npc.getX(), npc.getY(), npc.getZ(), 0, false, 0);
						mobs = brutalMobs;
					}
					else
					{
						mobs = violentMobs;
					}
				}
			}
		}
		else if (event.equalsIgnoreCase("strongAttack"))
		{
			if (!npc.isDead() && !npc.isDecayed())
			{
				npc.doDie(null);
				if (npc.getNpcId() == cocoon)
				{
					mobs = wickedMobs;
				}
				else
				{
					mobs = slightlyMobs;
				}
			}
		}

		if (mobs != null)
		{
			for (int a = 0; a <= 3; a++)
			{
				L2Npc mob = addSpawn(mobs[Rnd.get(mobs.length)], npc.getX(), npc.getY(), npc.getZ(), 0, false, 180000,
						true); //3 min self-despawn
				mob.setIsRunning(true);
				mob.setTarget(player);
				((L2MonsterInstance) mob).addDamageHate(player, 500, 99999);
				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);

				if (event.equalsIgnoreCase("strongAttack"))
				{
					mob.setCurrentHp(mob.getMaxHp() * 0.90);
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "cocoon.html";
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		if (skill == null)
		{
			notifyEvent("normalAttack", npc, attacker);
		}
		else
		{
			notifyEvent("strongAttack", npc, attacker);
		}

		return super.onAttack(npc, attacker, damage, isPet, skill);
	}

	public static void main(String[] args)
	{
		new Cocoons(-1, "Cocoons", "ai/individual");
	}
}
