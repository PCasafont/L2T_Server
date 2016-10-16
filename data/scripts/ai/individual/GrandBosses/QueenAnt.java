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

package ai.individual.GrandBosses;

import ai.group_template.L2AttackableAIScript;
import l2server.Config;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.MapRegionTable.TeleportWhereType;
import l2server.gameserver.instancemanager.GrandBossManager;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.*;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.model.zone.type.L2BossZone;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.network.serverpackets.PlaySound;
import l2server.gameserver.stats.SkillHolder;
import l2server.log.Log;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LasTravel
 *         <p>
 *         Queen Ant AI (Based on Emperorc work)
 *         <p>
 *         Source:
 *         - https://www.youtube.com/watch?v=hLI0R-qSa3w
 */

public class QueenAnt extends L2AttackableAIScript
{
	//Quest
	private static final boolean debug = false;
	private static final String qn = "QueenAnt";

	//Id's
	private static final int queenAntId = 29001;
	private static final int larvaId = 29002;
	private static final int nurseId = 29003;
	private static final int guardId = 29004;
	private static final int royalId = 29005;
	private static final int[] allMobs = {queenAntId, larvaId, nurseId, guardId, royalId};
	private static final L2BossZone bossZone = GrandBossManager.getInstance().getZone(-21610, 181594, -5734);

	//Skills
	private static final SkillHolder HEAL1 = new SkillHolder(4020, 1);
	private static final SkillHolder HEAL2 = new SkillHolder(4024, 1);
	private static final SkillHolder CURSE = new SkillHolder(4215, 1);

	//Others
	private List<L2MonsterInstance> nurses = new ArrayList<L2MonsterInstance>(5);
	private static long LastAction;
	private static L2Npc queenAnt;
	private static L2Npc larvaAnt;

	public QueenAnt(int id, String name, String descr)
	{
		super(id, name, descr);

		for (int i : allMobs)
		{
			addAttackId(i);
			addSpawnId(i);
			addKillId(i);
			addAggroRangeEnterId(i);
		}

		addEnterZoneId(bossZone.getId());
		addFactionCallId(nurseId);

		//Unlock
		startQuestTimer("unlock_queen_ant", GrandBossManager.getInstance().getUnlockTime(queenAntId), null, null);
	}

	@Override
	public final String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (debug)
		{
			Log.warning(getName() + ": onEnterZone: " + character.getName());
		}

		if (!character.isGM())
		{
			L2PcInstance player = null;

			if (character instanceof L2PcInstance)
			{
				player = (L2PcInstance) character;
			}
			if (character instanceof L2SummonInstance)
			{
				player = ((L2SummonInstance) character).getOwner();
			}
			else if (character instanceof L2PetInstance)
			{
				player = ((L2PetInstance) character).getOwner();
			}
			else if (character instanceof L2BabyPetInstance)
			{
				player = ((L2BabyPetInstance) character).getOwner();
			}
			else if (character instanceof L2MobSummonInstance)
			{
				player = ((L2MobSummonInstance) character).getOwner();
			}

			if (player != null)
			{
				if (player.getLevel() > 48 && !player.isGM() && !Config.isServer(Config.TENKAI_ESTHUS))
				{
					if (getQuestTimer("player_kick", null, player) == null)
					{
						startQuestTimer("player_kick", 3000, null, player);
					}
				}
			}
		}

		return super.onEnterZone(character, zone);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (debug)
		{
			Log.warning(getName() + ": onAdvEvent: " + event);
		}

		if (event.equalsIgnoreCase("unlock_queen_ant"))
		{
			queenAnt = addSpawn(queenAntId, -21610, 181594, -5734, 0, false, 0);

			GrandBossManager.getInstance().addBoss((L2GrandBossInstance) queenAnt);

			GrandBossManager.getInstance().setBossStatus(queenAntId, GrandBossManager.getInstance().ALIVE);

			queenAnt.broadcastPacket(
					new PlaySound(1, "BS02_D", 1, queenAnt.getObjectId(), queenAnt.getX(), queenAnt.getY(),
							queenAnt.getZ()));

			larvaAnt = addSpawn(larvaId, -21600, 179482, -5846, Rnd.get(360), false, 0);

			if (Rnd.get(100) < 33)
			{
				bossZone.movePlayersTo(-19480, 187344, -5600);
			}
			else if (Rnd.get(100) < 50)
			{
				bossZone.movePlayersTo(-17928, 180912, -5520);
			}
			else
			{
				bossZone.movePlayersTo(-23808, 182368, -5600);
			}

			startQuestTimer("queen_ant_heal_process", 1000, null, null, true);
		}
		else if (event.equalsIgnoreCase("check_activity_task"))
		{
			if (!GrandBossManager.getInstance().isActive(queenAntId, LastAction))
			{
				notifyEvent("end_queenAnt", null, null);
			}
		}
		else if (event.equalsIgnoreCase("end_queenAnt"))
		{
			QuestTimer activityTimer = getQuestTimer("check_activity_task", null, null);
			if (activityTimer != null)
			{
				activityTimer.cancel();
			}

			if (GrandBossManager.getInstance().getBossStatus(queenAntId) != GrandBossManager.getInstance().DEAD)
			{
				GrandBossManager.getInstance().setBossStatus(queenAntId, GrandBossManager.getInstance().ALIVE);
			}
		}
		else if (event.equalsIgnoreCase("player_kick"))
		{
			player.teleToLocation(TeleportWhereType.Town);
		}
		else if (event.equalsIgnoreCase("queen_ant_heal_process"))
		{
			boolean notCasting;
			final boolean larvaNeedHeal = larvaAnt != null && larvaAnt.getCurrentHp() < larvaAnt.getMaxHp();
			final boolean queenNeedHeal = queenAnt != null && queenAnt.getCurrentHp() < queenAnt.getMaxHp();

			List<L2MonsterInstance> toIterate = new ArrayList<L2MonsterInstance>(nurses);
			for (L2MonsterInstance nurse : toIterate)
			{
				if (nurse == null || nurse.isDead() || nurse.isCastingNow())
				{
					continue;
				}

				notCasting = nurse.getAI().getIntention() != CtrlIntention.AI_INTENTION_CAST;

				if (larvaNeedHeal)
				{
					if (nurse.getTarget() != larvaAnt || notCasting)
					{
						nurse.setTarget(larvaAnt);
						nurse.useMagic(Rnd.nextBoolean() ? HEAL1.getSkill() : HEAL2.getSkill());
					}
					continue;
				}

				if (queenNeedHeal)
				{
					if (nurse.getLeader() == larvaAnt) // skip larva's minions
					{
						continue;
					}

					if (nurse.getTarget() != queenAnt || notCasting)
					{
						nurse.setTarget(queenAnt);
						nurse.useMagic(HEAL1.getSkill());
					}
					continue;
				}

				// if nurse not casting - remove target
				if (notCasting && nurse.getTarget() != null)
				{
					nurse.setTarget(null);
				}
			}
		}

		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (debug)
		{
			Log.warning(getName() + ": onAttack: " + npc.getName());
		}

		LastAction = System.currentTimeMillis();

		if (GrandBossManager.getInstance().getBossStatus(queenAntId) == GrandBossManager.getInstance().ALIVE)
		{
			GrandBossManager.getInstance().setBossStatus(queenAntId, GrandBossManager.getInstance().FIGHTING);

			startQuestTimer("check_activity_task", 60000, null, null, true);
		}

		//Anti BUGGERS
		if (!bossZone.isInsideZone(attacker)) //Character attacking out of zone
		{
			attacker.doDie(null);

			if (debug)
			{
				Log.warning(getName() + ": Character: " + attacker.getName() + " attacked: " + npc.getName() +
						" out of the boss zone!");
			}
		}

		if (!bossZone.isInsideZone(npc)) //Npc moved out of the zone
		{
			L2Spawn spawn = npc.getSpawn();

			if (spawn != null)
			{
				npc.teleToLocation(spawn.getX(), spawn.getY(), spawn.getZ());
			}

			if (debug)
			{
				Log.warning(getName() + ": Character: " + attacker.getName() + " attacked: " + npc.getName() +
						" wich is out of the boss zone!");
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (debug)
		{
			Log.warning(getName() + ": onKill: " + npc.getName());
		}

		if (npc.getNpcId() == queenAntId)
		{
			GrandBossManager.getInstance().notifyBossKilled(queenAntId);

			notifyEvent("end_queenAnt", null, null);

			queenAnt.broadcastPacket(
					new PlaySound(1, "BS02_D", 1, queenAnt.getObjectId(), queenAnt.getX(), queenAnt.getY(),
							queenAnt.getZ()));

			startQuestTimer("unlock_queen_ant", GrandBossManager.getInstance().getUnlockTime(queenAntId), null, null);

			nurses.clear();
			larvaAnt.deleteMe();
			larvaAnt = null;
			queenAnt = null;
		}
		else if (queenAnt != null && !queenAnt.isAlikeDead())
		{
			if (npc.getNpcId() == royalId)
			{
				L2MonsterInstance mob = (L2MonsterInstance) npc;
				if (mob.getLeader() != null)
				{
					mob.getLeader().getMinionList().onMinionDie(mob, (280 + Rnd.get(40)) * 1000);
				}
			}
			else if (npc.getNpcId() == nurseId)
			{
				L2MonsterInstance mob = (L2MonsterInstance) npc;
				nurses.remove(mob);
				if (mob.getLeader() != null)
				{
					mob.getLeader().getMinionList().onMinionDie(mob, 10000);
				}
			}
		}

		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		final L2MonsterInstance mob = (L2MonsterInstance) npc;
		switch (npc.getNpcId())
		{
			case larvaId:
				mob.setIsImmobilized(true);
				mob.setIsMortal(false);
				mob.setIsRaidMinion(true);
				break;

			case nurseId:
				mob.disableCoreAI(true);
				mob.setIsRaidMinion(true);
				nurses.add(mob);
				break;

			case royalId:
			case guardId:
				mob.setIsRaidMinion(true);
				break;
		}

		return super.onSpawn(npc);
	}

	@Override
	public String onFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isPet)
	{
		if (caller == null || npc == null)
		{
			return super.onFactionCall(npc, caller, attacker, isPet);
		}

		if (!npc.isCastingNow() && npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_CAST)
		{
			if (caller.getCurrentHp() < caller.getMaxHp())
			{
				npc.setTarget(caller);
				((L2Attackable) npc).useMagic(HEAL1.getSkill());
			}
		}
		return null;
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (npc == null)
		{
			return null;
		}

		if (!Config.isServer(Config.TENKAI_ESTHUS) && player.getLevel() - npc.getLevel() > 8)
		{
			npc.broadcastPacket(new MagicSkillUse(npc, player, CURSE.getSkillId(), CURSE.getSkillLvl(), 300, 0, 0));

			CURSE.getSkill().getEffects(npc, player);

			((L2Attackable) npc).stopHating(player); // for calling again

			return null;
		}

		return super.onAggroRangeEnter(npc, player, isPet);
	}

	public static void main(String[] args)
	{
		new QueenAnt(-1, qn, "ai/individual/GrandBosses");
	}
}
