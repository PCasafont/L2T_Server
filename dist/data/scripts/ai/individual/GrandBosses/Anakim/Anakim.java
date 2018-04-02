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

package ai.individual.GrandBosses.Anakim;

import ai.group_template.L2AttackableAIScript;
import l2server.Config;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.MapRegionTable.TeleportWhereType;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.instancemanager.GrandBossManager;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.GrandBossInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.model.zone.type.BossZone;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LasTravel
 * <p>
 * Anakim AI
 * <p>
 * Source:
 * - http://www.youtube.com/watch?v=LecymFTJQzQ
 * - https://www.youtube.com/watch?v=Vi-bf6p9H8s
 * - http://www.youtube.com/watch?v=YkinCX2ppyA
 * - http://boards.lineage2.com/showpost.php?p=3386784&postcount=6
 */

public class Anakim extends L2AttackableAIScript {
	//Quest
	private static final boolean debug = false;
	private static final String qn = "Anakim";

	//Id's
	private static final int anakimId = 25286;
	private static final int remnant = 19490;
	private static final int enterCubic = 31101;
	private static final int exitCubic = 31109;
	private static final int anakimCubic = 31111;
	private static final int[] anakimMinions = {25287, 25288, 25289};
	private static final int[] necroMobs = {21199, 21200, 21201, 21202, 21203, 21204, 21205, 21206, 21207};
	private static final Skill remantTele = SkillTable.getInstance().getInfo(23303, 1);
	private static final Location enterLoc = new Location(172420, -17602, -4906);
	private static final Location enterAnakimLoc = new Location(184569, -12134, -5499);
	private static final int[] allMobs =
			{anakimId, anakimMinions[0], anakimMinions[1], anakimMinions[2], necroMobs[0], necroMobs[1], necroMobs[2], necroMobs[3], necroMobs[4],
					necroMobs[5], necroMobs[6], necroMobs[7], necroMobs[8], remnant};
	private static final BossZone bossZone = GrandBossManager.getInstance().getZone(185084, -12598, -5499);
	private static final BossZone preAnakimZone = GrandBossManager.getInstance().getZone(172679, -17486, -4906);

	//Others
	private static List<Npc> remnants = new ArrayList<Npc>();
	private static long lastAction;
	private static Npc anakimBoss;

	public Anakim(int id, String name, String descr) {
		super(id, name, descr);

		addStartNpc(enterCubic);
		addTalkId(enterCubic);

		addStartNpc(exitCubic);
		addTalkId(exitCubic);

		addStartNpc(anakimCubic);
		addTalkId(anakimCubic);

		addSpawnId(remnant);
		addSpellFinishedId(remnant);

		for (int i : allMobs) {
			addAttackId(i);
			addKillId(i);
			addSkillSeeId(i);
		}

		//Unlock
		startQuestTimer("unlock_anakim", GrandBossManager.getInstance().getUnlockTime(anakimId), null, null);
	}

	@Override
	public String onTalk(Npc npc, Player player) {
		if (debug) {
			log.warn(getName() + ": onTalk: " + player.getName());
		}

		int npcId = npc.getNpcId();

		if (npcId == enterCubic || npcId == anakimCubic) {
			int anakimStatus = GrandBossManager.getInstance().getBossStatus(anakimId);

			final List<Player> allPlayers = new ArrayList<Player>();

			if (anakimStatus == GrandBossManager.getInstance().DEAD) {
				return "31101-01.html";
			} else {
				if (!debug) {
					if (anakimStatus == GrandBossManager.getInstance().ALIVE && !InstanceManager.getInstance()
							.checkInstanceConditions(player, -1, Config.ANAKIM_MIN_PLAYERS, 100, 99, Config.MAX_LEVEL)) {
						return null;
					} else if (anakimStatus == GrandBossManager.getInstance().WAITING && !InstanceManager.getInstance()
							.checkInstanceConditions(player, -1, Config.ANAKIM_MIN_PLAYERS, 100, 99, Config.MAX_LEVEL)) {
						return null;
					}
					if (anakimStatus == GrandBossManager.getInstance().FIGHTING) {
						return "31101-01.html";
					}
				}
			}

			if (anakimStatus == GrandBossManager.getInstance().ALIVE && npcId == enterCubic) {
				GrandBossManager.getInstance().setBossStatus(anakimId, GrandBossManager.getInstance().WAITING);

				SpawnTable.getInstance().spawnSpecificTable("pre_anakim");

				remnants.clear();

				notifyEvent("spawn_remant", null, null);

				lastAction = System.currentTimeMillis();

				startQuestTimer("check_activity_task", 60000, null, null, true);
			} else if (anakimStatus == GrandBossManager.getInstance().WAITING && npcId == anakimCubic) {
				if (!remnants.isEmpty()) {
					return "You must kill all minions before you can engage in a Fight with Anakim.";
				}

				GrandBossManager.getInstance().setBossStatus(anakimId, GrandBossManager.getInstance().FIGHTING);

				//Spawn the rb
				anakimBoss = addSpawn(anakimId, 185080, -12613, -5499, 16550, false, 0);

				GrandBossManager.getInstance().addBoss((GrandBossInstance) anakimBoss);

				startQuestTimer("end_anakim", 60 * 60000, null, null); //1h
			}

			if (debug) {
				allPlayers.add(player);
			} else {
				allPlayers.addAll(Config.ANAKIM_MIN_PLAYERS > Config.MAX_MEMBERS_IN_PARTY || player.getParty().isInCommandChannel() ?
						player.getParty().getCommandChannel().getMembers() : player.getParty().getPartyMembers());
			}

			Location enterLoc = npcId == enterCubic ? Anakim.enterLoc : enterAnakimLoc;
			for (Player enterPlayer : allPlayers) {
				if (enterPlayer == null) {
					continue;
				}

				if (npcId == anakimCubic) {
					bossZone.allowPlayerEntry(enterPlayer, 7200);
				} else {
					preAnakimZone.allowPlayerEntry(enterPlayer, 7200);
				}

				enterPlayer.teleToLocation(enterLoc, true);
			}
		} else if (npc.getNpcId() == exitCubic) {
			player.teleToLocation(TeleportWhereType.Town);
		}
		return super.onTalk(npc, player);
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player) {
		if (debug) {
			log.warn(getName() + ": onAdvEvent: " + event);
		}

		if (event.equalsIgnoreCase("unlock_anakim")) {
			GrandBossManager.getInstance().setBossStatus(anakimId, GrandBossManager.getInstance().ALIVE);
		} else if (event.equalsIgnoreCase("check_activity_task")) {
			if (!GrandBossManager.getInstance().isActive(anakimId, lastAction)) {
				notifyEvent("end_anakim", null, null);
			}
		} else if (event.equalsIgnoreCase("spawn_remant")) {
			List<L2Spawn> spawns = SpawnTable.getInstance().getSpecificSpawns("pre_anakim"); //Can be moved into a global script var, testing

			L2Spawn randomSpawn = null;

			if (npc == null) {
				for (int i = 0; i < 2; i++) {
					randomSpawn = spawns.get(Rnd.get(spawns.size()));
					if (randomSpawn != null) {
						Npc remnant = addSpawn(Anakim.remnant,
								randomSpawn.getX(),
								randomSpawn.getY(),
								randomSpawn.getZ(),
								randomSpawn.getHeading(),
								true,
								0,
								false,
								0);
						remnants.add(remnant);
					}
				}
			} else {
				randomSpawn = spawns.get(Rnd.get(spawns.size()));
				if (randomSpawn != null) {
					npc.teleToLocation(randomSpawn.getX(), randomSpawn.getY(), randomSpawn.getZ());
					npc.setSpawn(randomSpawn);
				}
			}
		} else if (event.equalsIgnoreCase("cancel_timers")) {
			QuestTimer activityTimer = getQuestTimer("check_activity_task", null, null);
			if (activityTimer != null) {
				activityTimer.cancel();
			}

			QuestTimer forceEnd = getQuestTimer("end_anakim", null, null);
			if (forceEnd != null) {
				forceEnd.cancel();
			}
		} else if (event.equalsIgnoreCase("end_anakim")) {
			notifyEvent("cancel_timers", null, null);

			if (anakimBoss != null) {
				anakimBoss.deleteMe();
			}

			bossZone.oustAllPlayers();

			preAnakimZone.oustAllPlayers();

			SpawnTable.getInstance().despawnSpecificTable("pre_anakim");

			for (Npc remnant : remnants) {
				if (remnant == null) {
					continue;
				}

				remnant.deleteMe();
			}

			if (GrandBossManager.getInstance().getBossStatus(anakimId) != GrandBossManager.getInstance().DEAD) {
				GrandBossManager.getInstance().setBossStatus(anakimId, GrandBossManager.getInstance().ALIVE);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet) {
		if (debug) {
			log.warn(getName() + ": onAttack: " + npc.getName());
		}

		lastAction = System.currentTimeMillis();

		if (npc.isMinion() || npc.isRaid())//Anakim and minions
		{
			//Anti BUGGERS
			if (!bossZone.isInsideZone(attacker)) //Character attacking out of zone
			{
				attacker.doDie(null);

				if (debug) {
					log.warn(getName() + ": Character: " + attacker.getName() + " attacked: " + npc.getName() + " out of the boss zone!");
				}
			}

			if (!bossZone.isInsideZone(npc)) //Npc moved out of the zone
			{
				L2Spawn spawn = npc.getSpawn();

				if (spawn != null) {
					npc.teleToLocation(spawn.getX(), spawn.getY(), spawn.getZ());
				}

				if (debug) {
					log.warn(getName() + ": Character: " + attacker.getName() + " attacked: " + npc.getName() + " wich is out of the boss zone!");
				}
			}
		}

		if (npc.getNpcId() == remnant) {
			if (npc.getCurrentHp() < npc.getMaxHp() * 0.30) {
				if (!npc.isCastingNow() && Rnd.get(100) > 95) {
					npc.doCast(remantTele);
				}
			}
		}

		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(Npc npc, Player killer, boolean isPet) {
		if (debug) {
			log.warn(getName() + ": onKill: " + npc.getName());
		}

		if (npc.getNpcId() == anakimId) {
			GrandBossManager.getInstance().notifyBossKilled(anakimId);

			notifyEvent("cancel_timers", null, null);

			addSpawn(exitCubic, 185082, -12606, -5499, 6133, false, 900000); //15min

			startQuestTimer("unlock_anakim", GrandBossManager.getInstance().getUnlockTime(anakimId), null, null);

			startQuestTimer("end_anakim", 900000, null, null);
		} else if (npc.getNpcId() == remnant) {
			remnants.remove(npc);

			if (remnants.isEmpty()) {
				addSpawn(anakimCubic, 183225, -11911, -4897, 32768, false, 60 * 60000, false, 0);
			}
		}

		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpellFinished(Npc npc, Player player, Skill skill) {
		if (debug) {
			log.warn(getName() + ": onSpellFinished: " + npc.getName());
		}

		if (npc.getNpcId() == remnant && preAnakimZone.isInsideZone(npc)) {
			if (skill == remantTele) {
				notifyEvent("spawn_remant", npc, null);
			}
		}
		return super.onSpellFinished(npc, player, skill);
	}

	@Override
	public String onSkillSee(Npc npc, Player caster, Skill skill, WorldObject[] targets, boolean isPet) {
		if (debug) {
			log.warn(getName() + ": onSkillSee: " + npc.getName());
		}

		if (Util.contains(anakimMinions, npc.getNpcId()) && Rnd.get(2) == 1) {
			if (skill.getSkillType().toString().contains("HEAL")) {
				if (!npc.isCastingNow() && npc.getTarget() != npc && npc.getTarget() != caster &&
						npc.getTarget() != anakimBoss) //Don't call minions if are healing Anakim
				{
					((Attackable) npc).clearAggroList();
					npc.setTarget(caster);
					((Attackable) npc).addDamageHate(caster, 500, 99999);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, caster);
				}
			}
		}

		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public String onSpawn(Npc npc) {
		if (debug) {
			log.warn(getName() + ": onSpawn: " + npc.getName() + ": " + npc.getX() + ", " + npc.getY() + ", " + npc.getZ());
		}

		return super.onSpawn(npc);
	}

	public static void main(String[] args) {
		new Anakim(-1, qn, "ai/individual/GrandBosses");
	}
}
