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
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.GrandBossInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.model.zone.type.BossZone;
import l2server.gameserver.network.serverpackets.NpcSay;
import l2server.gameserver.network.serverpackets.PlaySound;
import l2server.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Inia
 * <p>
 * helios AI
 */

public class Helios extends L2AttackableAIScript {
	private static Logger log = LoggerFactory.getLogger(Helios.class.getName());


	//Quest
	private static final boolean debug = false;
	private static final String qn = "Helios";

	//Id's
	private static final int heliosId = 29303;
	private static final int[] textIds = {1000028, 1000029, 1000030, 1000031};
	private static final Skill paralysis = SkillTable.getInstance().getInfo(4064, 1);
	private static final BossZone bossZone = GrandBossManager.getInstance().getZone(43728, 17220, -4342);
	private static final Location[] heliosLocs =
			{new Location(43728, 17220, -4342), new Location(55024, 17368, -5412), new Location(53504, 21248, -5486),
					new Location(53248, 24576, -5262)};

	//Others
	private Npc heliosBoss;
	private static long LastAction;
	private boolean isTeleported;

	public Helios(int id, String name, String descr) {
		super(id, name, descr);

		addAttackId(heliosId);
		addKillId(heliosId);
		addSkillSeeId(heliosId);

		//Unlock
		startQuestTimer("unlock_helios", GrandBossManager.getInstance().getUnlockTime(heliosId), null, null);
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player) {
		if (debug) {
			log.warn(getName() + ": onAdvEvent: " + event);
		}

		if (event.equalsIgnoreCase("unlock_helios")) {
			int rnd = Rnd.get(10);
			Location heliosLoc = null;

			if (rnd < 4) {
				heliosLoc = heliosLocs[1];
			} else if (rnd < 7) {
				heliosLoc = heliosLocs[2];
			} else {
				heliosLoc = heliosLocs[3];
			}

			heliosBoss = addSpawn(heliosId, heliosLoc.getX(), heliosLoc.getY(), heliosLoc.getZ(), 0, false, 0);

			GrandBossManager.getInstance().addBoss((GrandBossInstance) heliosBoss);

			GrandBossManager.getInstance().setBossStatus(heliosId, GrandBossManager.getInstance().ALIVE);

			heliosBoss.broadcastPacket(new PlaySound(1,
					"BS01_A",
					1,
					heliosBoss.getObjectId(),
					heliosBoss.getX(),
					heliosBoss.getY(),
					heliosBoss.getZ()));
		} else if (event.equalsIgnoreCase("check_helios_location")) {
			//Check the boss location and minion loc
			if (isTeleported && heliosBoss.getCurrentHp() > heliosBoss.getMaxHp() * 0.95 || !bossZone.isInsideZone(heliosBoss) && !isTeleported) {
				setSpawnPoint(Rnd.get(3) + 1);
				isTeleported = false;
			} else if (isTeleported && !bossZone.isInsideZone(heliosBoss)) {
				setSpawnPoint(0);
			}
		} else if (event.equalsIgnoreCase("check_activity_task")) {
			if (!GrandBossManager.getInstance().isActive(heliosId, LastAction)) {
				notifyEvent("end_helios", null, null);
			}
		} else if (event.equalsIgnoreCase("end_helios")) {
			QuestTimer activityTimer = getQuestTimer("check_activity_task", null, null);
			if (activityTimer != null) {
				activityTimer.cancel();
			}

			QuestTimer checkHeliosLoc = getQuestTimer("check_helios_location", null, null);
			if (checkHeliosLoc != null) {
				checkHeliosLoc.cancel();
			}

			if (GrandBossManager.getInstance().getBossStatus(heliosId) != GrandBossManager.getInstance().DEAD) {
				GrandBossManager.getInstance().setBossStatus(heliosId, GrandBossManager.getInstance().ALIVE);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	public void setSpawnPoint(int index) {
		Location loc = heliosLocs[index];

		((Attackable) heliosBoss).clearAggroList();

		heliosBoss.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);

		L2Spawn spawn = heliosBoss.getSpawn();
		spawn.setX(loc.getX());
		spawn.setY(loc.getY());
		spawn.setZ(loc.getZ());

		heliosBoss.teleToLocation(loc.getX(), loc.getY(), loc.getZ());
	}

	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet) {
		if (debug) {
			log.warn(getName() + ": onAttack: " + npc.getName());
		}

		LastAction = System.currentTimeMillis();

		if (GrandBossManager.getInstance().getBossStatus(heliosId) == GrandBossManager.getInstance().ALIVE) {
			GrandBossManager.getInstance().setBossStatus(heliosId, GrandBossManager.getInstance().FIGHTING);

			startQuestTimer("check_activity_task", 60000, null, null, true);

			startQuestTimer("check_helios_location", 10000, null, null, true);
		}

		if (npc.getNpcId() == heliosId) {
			if (!isTeleported && npc.getCurrentHp() - damage < npc.getMaxHp() / 2) {
				isTeleported = true;
				setSpawnPoint(0);
			} else if (npc.isInsideRadius(attacker, 1000, false, false) && !npc.isInsideRadius(attacker, 300, false, false) && Rnd.get(10) == 0) {
				attacker.teleToLocation(npc.getX(), npc.getY(), npc.getZ());

				NpcSay packet = new NpcSay(npc.getObjectId(), 0, heliosId, textIds[Rnd.get(3)]);
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

		if (npc.getNpcId() == heliosId) {
			heliosBoss.broadcastPacket(new PlaySound(1,
					"BS02_D",
					1,
					heliosBoss.getObjectId(),
					heliosBoss.getX(),
					heliosBoss.getY(),
					heliosBoss.getZ()));

			GrandBossManager.getInstance().notifyBossKilled(heliosId);

			notifyEvent("end_helios", null, null);

			startQuestTimer("unlock_helios", GrandBossManager.getInstance().getUnlockTime(heliosId), null, null);
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSkillSee(Npc npc, Player caster, Skill skill, WorldObject[] targets, boolean isPet) {
		if (npc.getNpcId() == heliosId) {
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
		new Helios(-1, qn, "ai/individual/GrandBosses");
	}
}
