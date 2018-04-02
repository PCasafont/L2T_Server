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

package ai.individual.GrandBosses.AntharasOpenWorld;

import ai.group_template.L2AttackableAIScript;
import l2server.Config;
import l2server.gameserver.GeoData;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.instancemanager.GrandBossManager;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.GrandBossInstance;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.model.zone.ZoneType;
import l2server.gameserver.model.zone.type.BossZone;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.util.Broadcast;
import l2server.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author LasTravel
 * <p>
 * Open World Antharas AI (Based on SANDMAN work)
 */

public class AntharasOpenWorld extends L2AttackableAIScript {
	private static Logger log = LoggerFactory.getLogger(AntharasOpenWorld.class.getName());


	//Quest
	private static final boolean debug = false;
	private static final String qn = "AntharasOpenWorld";

	//Id's
	private static final int maxMinions = 30;
	private static final int behemothDragon = 29069;
	private static final int taraskDragon = 29225;
	private static final int[] dragonBombers = {29070, 29076};
	private static final int antharasId = 29068;
	private static final int[] allMobIds = {behemothDragon, taraskDragon, dragonBombers[0], dragonBombers[1], antharasId};
	private static final int heartOfWarding = 13001;
	private static final int teleportCubic = 31859;
	private static final BossZone bossZone = GrandBossManager.getInstance().getZone(179700, 113800, -7709);

	//Others
	private static final List<Npc> allMonsters = new ArrayList<Npc>();
	private static Npc antharasBoss;
	private static long LastAction;

	public AntharasOpenWorld(int id, String name, String descr) {
		super(id, name, descr);

		addTalkId(teleportCubic);
		addStartNpc(teleportCubic);

		addTalkId(heartOfWarding);
		addStartNpc(heartOfWarding);

		for (int i : allMobIds) {
			addKillId(i);
			addAttackId(i);
		}

		addEnterZoneId(bossZone.getId());
		addExitZoneId(bossZone.getId());

		//Unlock
		startQuestTimer("unlock_antharas", GrandBossManager.getInstance().getUnlockTime(antharasId), null, null);
	}

	@Override
	public String onTalk(Npc npc, Player player) {
		if (debug) {
			log.warn(getName() + ": onTalk: " + player.getName());
		}

		if (npc.getNpcId() == heartOfWarding) {
			int anthyStatus = GrandBossManager.getInstance().getBossStatus(antharasId);

			final List<Player> allPlayers = new ArrayList<Player>();

			if (anthyStatus == GrandBossManager.getInstance().DEAD) {
				return "13001-01.html";
			} else {
				if (!debug) {
					if (anthyStatus == GrandBossManager.getInstance().ALIVE && !InstanceManager.getInstance()
							.checkInstanceConditions(player, -1, Config.ANTHARAS_MIN_PLAYERS, 200, 95, Config.MAX_LEVEL)) {
						return null;
					} else if (anthyStatus == GrandBossManager.getInstance().WAITING && !InstanceManager.getInstance()
							.checkInstanceConditions(player, -1, Config.ANTHARAS_MIN_PLAYERS, 200, 95, Config.MAX_LEVEL)) {
						return null;
					} else if (anthyStatus == GrandBossManager.getInstance().FIGHTING) {
						return "13001-02.html";
					}
				}
			}

			if (anthyStatus == GrandBossManager.getInstance().ALIVE) {
				GrandBossManager.getInstance().setBossStatus(antharasId, GrandBossManager.getInstance().WAITING);

				LastAction = System.currentTimeMillis();

				startQuestTimer("antharas_spawn_task_1", debug ? 60000 : Config.ANTHARAS_WAIT_TIME * 60000, null, null);
			}

			if (debug) {
				allPlayers.add(player);
			} else {
				allPlayers.addAll(Config.ANTHARAS_MIN_PLAYERS > Config.MAX_MEMBERS_IN_PARTY || player.getParty().isInCommandChannel() ?
						player.getParty().getCommandChannel().getMembers() : player.getParty().getPartyMembers());
			}

			for (Player enterPlayer : allPlayers) {
				if (enterPlayer == null) {
					continue;
				}

				bossZone.allowPlayerEntry(enterPlayer, 7200);

				enterPlayer.teleToLocation(179700 + Rnd.get(700), 113800 + Rnd.get(2100), -7709);
			}
		} else if (npc.getNpcId() == teleportCubic) {
			player.teleToLocation(79800 + Rnd.get(600), 151200 + Rnd.get(1100), -3534);
		}
		return super.onTalk(npc, player);
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player) {
		if (debug) {
			log.warn(getName() + ": onAdvEvent: " + event);
		}

		if (event.equalsIgnoreCase("unlock_antharas")) {
			GrandBossManager.getInstance().setBossStatus(antharasId, GrandBossManager.getInstance().ALIVE);

			Broadcast.toAllOnlinePlayers(new Earthquake(185708, 114298, -8221, 20, 10));
		} else if (event.equalsIgnoreCase("antharas_spawn_task_1")) {
			//Block all players
			bossZone.stopWholeZone();

			antharasBoss = addSpawn(antharasId, 181323, 114850, -7623, 32542, false, 120 * 2 * 60000);

			allMonsters.add(antharasBoss);

			GrandBossManager.getInstance().addBoss((GrandBossInstance) antharasBoss);

			antharasBoss.setIsImmobilized(true);

			GrandBossManager.getInstance().setBossStatus(antharasId, GrandBossManager.getInstance().FIGHTING);

			LastAction = System.currentTimeMillis();

			//Cameras
			bossZone.sendDelayedPacketToZone(16, new SpecialCamera(antharasBoss.getObjectId(), 700, 13, -19, 0, 20000, 0, 0, 1, 0));
			bossZone.sendDelayedPacketToZone(3016, new SpecialCamera(antharasBoss.getObjectId(), 700, 13, 0, 6000, 20000, 0, 0, 1, 0));
			bossZone.sendDelayedPacketToZone(13016, new SpecialCamera(antharasBoss.getObjectId(), 3700, 0, -3, 0, 10000, 0, 0, 1, 0));
			bossZone.sendDelayedPacketToZone(13216, new SpecialCamera(antharasBoss.getObjectId(), 1100, 0, -3, 22000, 30000, 0, 0, 1, 0));
			bossZone.sendDelayedPacketToZone(24016, new SpecialCamera(antharasBoss.getObjectId(), 1100, 0, -3, 300, 7000, 0, 0, 1, 0));

			startQuestTimer("antharas_spawn_task_7", 25916, null, null);
			startQuestTimer("antharas_last_spawn_task", 25916, null, null);
			startQuestTimer("check_activity_task", 60000, null, null, true);
			startQuestTimer("spawn_minion_task", 5 * 60000, null, null, true);
		} else if (event.equalsIgnoreCase("antharas_spawn_task_7")) {
			antharasBoss.abortCast();

			antharasBoss.setIsImmobilized(false);

			startQuestTimer("antharas_move_random", 500, null, null);
		} else if (event.equalsIgnoreCase("antharas_move_random")) {
			//UnBlock all players
			bossZone.startWholeZone();

			L2CharPosition pos = new L2CharPosition(Rnd.get(175000, 178500), Rnd.get(112400, 116000), -7707, 0);

			antharasBoss.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);

			//kick dual box
			bossZone.kickDualBoxes();
		} else if (event.equalsIgnoreCase("check_activity_task")) {
			if (!GrandBossManager.getInstance().isActive(antharasId, LastAction)) {
				notifyEvent("end_antharas", null, null);
			}
		} else if (event.equalsIgnoreCase("cancel_timers")) {
			QuestTimer activityTimer = getQuestTimer("check_activity_task", null, null);
			if (activityTimer != null) {
				activityTimer.cancel();
			}

			QuestTimer spawnMinionTask = getQuestTimer("spawn_minion_task", null, null);
			if (spawnMinionTask != null) {
				spawnMinionTask.cancel();
			}
		} else if (event.equalsIgnoreCase("end_antharas")) {
			notifyEvent("cancel_timers", null, null);

			bossZone.oustAllPlayers();

			for (Npc mob : allMonsters) {
				mob.getSpawn().stopRespawn();
				mob.deleteMe();
			}
			allMonsters.clear();

			if (GrandBossManager.getInstance().getBossStatus(antharasId) != GrandBossManager.getInstance().DEAD) {
				GrandBossManager.getInstance().setBossStatus(antharasId, GrandBossManager.getInstance().ALIVE);
			}
		} else if (event.equalsIgnoreCase("spawn_minion_task")) {
			if (antharasBoss != null && !antharasBoss.isDead()) {
				List<Integer> minionsToSpawn = new ArrayList<Integer>();
				for (int i = 1; i <= 5; i++) {
					if (allMonsters.size() < maxMinions) {
						minionsToSpawn.add(dragonBombers[Rnd.get(dragonBombers.length)]);
					}
				}

				if (allMonsters.size() < maxMinions) {
					minionsToSpawn.add(taraskDragon);
				}

				if (allMonsters.size() < maxMinions) {
					minionsToSpawn.add(behemothDragon);
				}

				for (int i = 0; i < minionsToSpawn.size(); i++) {
					int tried = 0;
					boolean notFound = true;
					int x = 175000;
					int y = 112400;
					int dt = (antharasBoss.getX() - x) * (antharasBoss.getX() - x) + (antharasBoss.getY() - y) * (antharasBoss.getY() - y);

					while (tried++ < 25 && notFound) {
						int rx = Rnd.get(175000, 179900);
						int ry = Rnd.get(112400, 116000);
						int rdt = (antharasBoss.getX() - rx) * (antharasBoss.getX() - rx) + (antharasBoss.getY() - ry) * (antharasBoss.getY() - ry);

						if (GeoData.getInstance().canSeeTarget(antharasBoss.getX(), antharasBoss.getY(), -7704, rx, ry, -7704)) {
							if (rdt < dt) {
								x = rx;
								y = ry;
								dt = rdt;

								if (rdt <= 900000) {
									notFound = false;
								}
							}
						}
					}

					Npc minion = addSpawn(minionsToSpawn.get(i), x, y, -7704, 0, true, 120 * 2 * 60000);
					minion.setIsRunning(true);
					allMonsters.add(minion);
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet) {
		if (debug) {
			log.warn(getName() + ": onAttack: " + npc.getName());
		}

		if (npc.getNpcId() == antharasId) {
			LastAction = System.currentTimeMillis();

			if (GrandBossManager.getInstance().getBossStatus(antharasId) != GrandBossManager.getInstance().FIGHTING) {
				bossZone.oustAllPlayers();
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(Npc npc, Player killer, boolean isPet) {
		if (debug) {
			log.warn(getName() + ": onKill: " + npc.getName());
		}

		if (npc.getNpcId() == antharasId) {
			GrandBossManager.getInstance().notifyBossKilled(antharasId);

			notifyEvent("cancel_timers", null, null);

			bossZone.broadcastPacket(new PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));

			addSpawn(teleportCubic, 177615, 114941, -7709, 0, false, 600000); //10min

			startQuestTimer("unlock_antharas", GrandBossManager.getInstance().getUnlockTime(antharasId), null, null);

			startQuestTimer("end_antharas", 900000, null, null);
		} else if (npc.getNpcId() == behemothDragon) {
			int countHPHerb = Rnd.get(6, 18);
			int countMPHerb = Rnd.get(6, 18);

			for (int i = 0; i < countHPHerb; i++) {
				((MonsterInstance) npc).dropItem(killer, 8602, 1);
			}

			for (int i = 0; i < countMPHerb; i++) {
				((MonsterInstance) npc).dropItem(killer, 8605, 1);
			}
		}

		if (allMonsters.contains(npc)) {
			allMonsters.remove(npc);
		}

		return super.onKill(npc, killer, isPet);
	}

	@Override
	public final String onEnterZone(Creature character, ZoneType zone) {
		if (character instanceof Player) {
			if (GrandBossManager.getInstance().getBossStatus(antharasId) == GrandBossManager.getInstance().WAITING) {
				character.sendPacket(new ExSendUIEvent(0,
						0,
						(int) TimeUnit.MILLISECONDS.toSeconds(LastAction + Config.ANTHARAS_WAIT_TIME * 60000 - System.currentTimeMillis()),
						0,
						"Antharas is coming..."));
			}
		}
		return null;
	}

	@Override
	public final String onExitZone(Creature character, ZoneType zone) {
		if (character instanceof Player) {
			if (GrandBossManager.getInstance().getBossStatus(antharasId) == GrandBossManager.getInstance().WAITING) {
				character.sendPacket(new ExSendUIEventRemove());
			}
		}
		return null;
	}

	public static void main(String[] args) {
		new AntharasOpenWorld(-1, qn, "ai/individual/GrandBosses");
	}
}
