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
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.GrandBossManager;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.GrandBossInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.model.zone.type.BossZone;
import l2server.gameserver.network.serverpackets.NpcSay;
import l2server.gameserver.network.serverpackets.PlaySound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import l2server.util.Rnd;

/**
 * @author LasTravel
 * <p>
 * Orfen AI (Based on Emperorc work)
 */

public class Orfen extends L2AttackableAIScript {
	private static Logger log = LoggerFactory.getLogger(Orfen.class.getName());


	//Quest
	private static final boolean debug = false;
	private static final String qn = "Orfen";

	//Id's
	private static final int orfenId = 29014;
	private static final int[] textIds = {1000028, 1000029, 1000030, 1000031};
	private static final Skill paralysis = SkillTable.getInstance().getInfo(4064, 1);
	private static final BossZone bossZone = GrandBossManager.getInstance().getZone(43728, 17220, -4342);
	private static final Location[] orfenLocs =
			{new Location(43728, 17220, -4342), new Location(55024, 17368, -5412), new Location(53504, 21248, -5486),
					new Location(53248, 24576, -5262)};

	//Others
	private Npc orfenBoss;
	private static long LastAction;
	private boolean isTeleported;

	public Orfen(int id, String name, String descr) {
		super(id, name, descr);

		addAttackId(orfenId);
		addKillId(orfenId);
		addSkillSeeId(orfenId);

		//Unlock
		startQuestTimer("unlock_orfen", GrandBossManager.getInstance().getUnlockTime(orfenId), null, null);
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player) {
		if (debug) {
			log.warn(getName() + ": onAdvEvent: " + event);
		}

		if (event.equalsIgnoreCase("unlock_orfen")) {
			int rnd = Rnd.get(10);
			Location orfenLoc = null;

			if (rnd < 4) {
				orfenLoc = orfenLocs[1];
			} else if (rnd < 7) {
				orfenLoc = orfenLocs[2];
			} else {
				orfenLoc = orfenLocs[3];
			}

			orfenBoss = addSpawn(orfenId, orfenLoc.getX(), orfenLoc.getY(), orfenLoc.getZ(), 0, false, 0);

			GrandBossManager.getInstance().addBoss((GrandBossInstance) orfenBoss);

			GrandBossManager.getInstance().setBossStatus(orfenId, GrandBossManager.getInstance().ALIVE);

			orfenBoss.broadcastPacket(new PlaySound(1, "BS01_A", 1, orfenBoss.getObjectId(), orfenBoss.getX(), orfenBoss.getY(), orfenBoss.getZ()));
		} else if (event.equalsIgnoreCase("check_orfen_location")) {
			//Check the boss location and minion loc
			if (isTeleported && orfenBoss.getCurrentHp() > orfenBoss.getMaxHp() * 0.95 || !bossZone.isInsideZone(orfenBoss) && !isTeleported) {
				setSpawnPoint(Rnd.get(3) + 1);
				isTeleported = false;
			} else if (isTeleported && !bossZone.isInsideZone(orfenBoss)) {
				setSpawnPoint(0);
			}
		} else if (event.equalsIgnoreCase("check_activity_task")) {
			if (!GrandBossManager.getInstance().isActive(orfenId, LastAction)) {
				notifyEvent("end_orfen", null, null);
			}
		} else if (event.equalsIgnoreCase("end_orfen")) {
			QuestTimer activityTimer = getQuestTimer("check_activity_task", null, null);
			if (activityTimer != null) {
				activityTimer.cancel();
			}

			QuestTimer checkOrfenLoc = getQuestTimer("check_orfen_location", null, null);
			if (checkOrfenLoc != null) {
				checkOrfenLoc.cancel();
			}

			if (GrandBossManager.getInstance().getBossStatus(orfenId) != GrandBossManager.getInstance().DEAD) {
				GrandBossManager.getInstance().setBossStatus(orfenId, GrandBossManager.getInstance().ALIVE);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	public void setSpawnPoint(int index) {
		Location loc = orfenLocs[index];

		((Attackable) orfenBoss).clearAggroList();

		orfenBoss.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);

		L2Spawn spawn = orfenBoss.getSpawn();
		spawn.setX(loc.getX());
		spawn.setY(loc.getY());
		spawn.setZ(loc.getZ());

		orfenBoss.teleToLocation(loc.getX(), loc.getY(), loc.getZ());
	}

	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet) {
		if (debug) {
			log.warn(getName() + ": onAttack: " + npc.getName());
		}

		LastAction = System.currentTimeMillis();

		if (GrandBossManager.getInstance().getBossStatus(orfenId) == GrandBossManager.getInstance().ALIVE) {
			GrandBossManager.getInstance().setBossStatus(orfenId, GrandBossManager.getInstance().FIGHTING);

			startQuestTimer("check_activity_task", 60000, null, null, true);

			startQuestTimer("check_orfen_location", 10000, null, null, true);
		}

		if (npc.getNpcId() == orfenId) {
			if (!isTeleported && npc.getCurrentHp() - damage < npc.getMaxHp() / 2) {
				isTeleported = true;
				setSpawnPoint(0);
			} else if (npc.isInsideRadius(attacker, 1000, false, false) && !npc.isInsideRadius(attacker, 300, false, false) && Rnd.get(10) == 0) {
				attacker.teleToLocation(npc.getX(), npc.getY(), npc.getZ());

				NpcSay packet = new NpcSay(npc.getObjectId(), 0, orfenId, textIds[Rnd.get(3)]);
				packet.addStringParameter(attacker.getName().toString());
				npc.broadcastPacket(packet);

				npc.setTarget(attacker);
				npc.doCast(paralysis);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(Npc npc, Player killer, boolean isPet) {
		if (debug) {
			log.warn(getName() + ": onKill: " + npc.getName());
		}

		if (npc.getNpcId() == orfenId) {
			orfenBoss.broadcastPacket(new PlaySound(1, "BS02_D", 1, orfenBoss.getObjectId(), orfenBoss.getX(), orfenBoss.getY(), orfenBoss.getZ()));

			GrandBossManager.getInstance().notifyBossKilled(orfenId);

			notifyEvent("end_orfen", null, null);

			startQuestTimer("unlock_orfen", GrandBossManager.getInstance().getUnlockTime(orfenId), null, null);
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSkillSee(Npc npc, Player caster, Skill skill, WorldObject[] targets, boolean isPet) {
		if (npc.getNpcId() == orfenId) {
			Creature originalCaster = isPet ? caster.getPet() : caster;
			if (skill.getAggroPoints() > 0 && Rnd.get(5) == 0 && npc.isInsideRadius(originalCaster, 1000, false, false)) {
				NpcSay packet = new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), textIds[Rnd.get(textIds.length)]);
				packet.addStringParameter(caster.getName().toString());
				npc.broadcastPacket(packet);

				originalCaster.teleToLocation(npc.getX(), npc.getY(), npc.getZ());

				npc.setTarget(originalCaster);
				npc.doCast(paralysis);
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	public static void main(String[] args) {
		new Orfen(-1, qn, "ai/individual/GrandBosses");
	}
}
