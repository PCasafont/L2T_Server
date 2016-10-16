package ai.group_template;

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.util.Util;

/**
 * @author LasTravel
 *         <p>
 *         Source:
 *         - http://l2wiki.com/Guillotine_Fortress
 */

public class GuillotineFortress extends L2AttackableAIScript
{
	private static final int[] npcIds = {
			23199,
			23200,
			23201,
			23245,
			23244,
			23243,
			23202,
			23203,
			23204,
			23205,
			23206,
			23207,
			23208,
			23209,
			23212,
			23242
	};
	private static final L2Skill chaosShield = SkillTable.getInstance().getInfo(15090, 1);

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skill)
	{
		L2Abnormal chaosShield = npc.getFirstEffect(15090); //Chaos Shield

		if (chaosShield != null)
		{
			if (npc.getCurrentHp() < npc.getMaxHp() * 0.85 || skill != null && skill.getId() == 10258)
			{
				chaosShield.exit();

				for (L2Character attacker : npc.getAttackByList())
				{
					if (attacker == null || !(attacker instanceof L2PcInstance) ||
							!attacker.isInsideRadius(npc, 1600, false, false))
					{
						continue;
					}

					attacker.broadcastPacket(new ExShowScreenMessage(1801773, 7, 10000));
				}
			}
		}

		return super.onAttack(npc, player, damage, isPet, skill);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.doCast(chaosShield);

		return super.onSpawn(npc);
	}

	public GuillotineFortress(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int id : npcIds)
		{
			addAttackId(id);

			addSpawnId(id);
		}

		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable())
		{
			if (spawn == null)
			{
				continue;
			}

			if (Util.contains(npcIds, spawn.getNpcId()))
			{
				notifySpawn(spawn.getNpc());
			}
		}
	}

	public static void main(String[] args)
	{
		new GuillotineFortress(-1, "GuillotineFortress", "ai");
	}
}
