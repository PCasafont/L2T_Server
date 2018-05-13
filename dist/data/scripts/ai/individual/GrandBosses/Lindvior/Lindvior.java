package ai.individual.GrandBosses.Lindvior;

import ai.group_template.L2AttackableAIScript;
import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.ScenePlayerDataTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.instancemanager.GrandBossManager;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.GrandBossInstance;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.model.zone.type.BossZone;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.util.Broadcast;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author LasTravel
 * @author Pere
 * <p>
 * Lindvior Boss - Normal Mode
 * <p>
 * Source:
 * - http://www.youtube.com/watch?v=QlHvI54oyJo (Retail part 1)
 * - http://www.youtube.com/watch?v=9GMk6q4rjys (Retail part 2)
 * - http://www.youtube.com/watch?v=sVFNT8tdagA (Retail part3)
 * - http://www.youtube.com/watch?v=vKwf8Jx_Qtc (Retail failed boss)
 * - http://www.youtube.com/watch?v=dG9OMGGg1ao
 */

public class Lindvior extends L2AttackableAIScript {
	//Quest
	private static final boolean debug = false;
	private static final String qn = "Lindvior";

	//Id's
	private static final int npcEnterId = 33881;
	private static final int generatorId = 19426;
	private static final int generatorGuard = 19479;
	private static final int giantCycloneId = 19427;
	private static final int cycloneId = 25898;
	private static final int firstFloorLindvior = 25899;
	private static final int secondFloorLindvior = 29240;
	private static final int flyLindvior = 19424;
	private static final int[] lindviorIds = {firstFloorLindvior, secondFloorLindvior, flyLindvior};
	private static final int[] lynDracoIds = {25895, 25896, 25897, 29241, 29242, 29243};
	private static final int[] allMinionIds =
			{lynDracoIds[0], lynDracoIds[1], lynDracoIds[2], lynDracoIds[3], lynDracoIds[4], lynDracoIds[5], cycloneId, giantCycloneId};
	private static final int flyingLindviorAroundZoneId = 19423;
	private static final int lindviorCameraId = 19428;
	//private static final int redCircle					= 19391;
	//private static final int blueCircle					= 19392;
	private static final double maxGeneratorDamage = 1500000;
	private static final BossZone bossZone = GrandBossManager.getInstance().getZone(45697, -26269, -1409);

	//Effects
	private static final int allGeneratorsConnectedEffect = 21170110;
	private static final int redTowerEffect = 21170112;
	private static final int shieldTowerEffect = 21170100;
	private static final int _generatorEffect_1 = 21170104;
	private static final int _generatorEffect_2 = 21170102;
	private static final int _generatorEffect_3 = 21170106;
	private static final int _generatorEffect_4 = 21170108;
	private static final int redZoneEffect = 21170120;

	//Cords
	private static final Location enterCords = new Location(46931, -28813, -1406);

	//Skills
	//private static final Skill rechargePossible 		= SkillTable.getInstance().getInfo(15605, 1);
	//private static final Skill recharge 				= SkillTable.getInstance().getInfo(15606, 1);
	private static final Skill takeOff = SkillTable.getInstance().getInfo(15596, 1);

	//Vars
	private Npc dummyLindvior;
	private Npc lindviorBoss;
	private Location bossLocation;
	private static long LastAction;
	private int bossStage;
	private Map<Npc, Double> manageGenerators = new HashMap<Npc, Double>();

	public Lindvior(int questId, String name, String descr) {
		super(questId, name, descr);

		addTalkId(npcEnterId);
		addStartNpc(npcEnterId);
		addSpawnId(generatorGuard);
		addFirstTalkId(generatorId);
		addTalkId(generatorId);
		addStartNpc(generatorId);
		addSpawnId(generatorId);
		addAttackId(generatorId);

		for (int a : allMinionIds) {
			addAttackId(a);
			addKillId(a);
		}

		for (int a : lindviorIds) {
			addSpellFinishedId(a);
			addAttackId(a);
			addKillId(a);
		}

		//Unlock
		startQuestTimer("unlock_lindvior", GrandBossManager.getInstance().getUnlockTime(secondFloorLindvior), null, null);
	}

	@Override
	public String onSpawn(Npc npc) {
		if (debug) {
			log.warn(getName() + ": onSpawn: " + npc.getName());
		}

		if (npc.getNpcId() == generatorId) {
			npc.disableCoreAI(true);
			npc.setDisplayEffect(1);
			npc.setMortal(false);
			npc.setInvul(true); //Can't get damage now
		}

		if (npc.getNpcId() == generatorGuard) {
			npc.setInvul(true);
		}

		return super.onSpawn(npc);
	}

	@Override
	public String onFirstTalk(Npc npc, Player player) {
		if (debug) {
			log.warn(getName() + ": onFirstTalk: " + player.getName());
		}

		if (npc.getNpcId() == generatorId) {
			if (bossStage == 1) {
				return npc.getDisplayEffect() == 1 ? "Generator.html" : "GeneratorDone.html";
			} else {
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, npc);
			}
		}

		return super.onFirstTalk(npc, player);
	}

	@Override
	public final String onTalk(Npc npc, Player player) {
		if (debug) {
			log.warn(getName() + ": onTalk: " + player.getName());
		}

		int npcId = npc.getNpcId();
		if (npcId == npcEnterId) {
			int lindStatus = GrandBossManager.getInstance().getBossStatus(secondFloorLindvior);

			final List<Player> allPlayers = new ArrayList<Player>();
			if (lindStatus == GrandBossManager.getInstance().DEAD) {
				return "33881-01.html";
			} else {
				if (!debug) {
					if (lindStatus == GrandBossManager.getInstance().ALIVE && !InstanceManager.getInstance()
							.checkInstanceConditions(player, -1, Config.LINDVIOR_MIN_PLAYERS, 200, 95, Config.MAX_LEVEL)) {
						return null;
					} else if (lindStatus == GrandBossManager.getInstance().WAITING && !InstanceManager.getInstance()
							.checkInstanceConditions(player, -1, Config.LINDVIOR_MIN_PLAYERS, 200, 95, Config.MAX_LEVEL)) {
						return null;
					} else if (lindStatus == GrandBossManager.getInstance().FIGHTING) {
						return null;
					}
				}
			}

			if (lindStatus == GrandBossManager.getInstance().ALIVE) {
				GrandBossManager.getInstance().setBossStatus(secondFloorLindvior, GrandBossManager.getInstance().WAITING);

				startQuestTimer("stage_1_start", 1000, null, null);
			}

			if (debug) {
				allPlayers.add(player);
			} else {
				allPlayers.addAll(Config.LINDVIOR_MIN_PLAYERS > Config.MAX_MEMBERS_IN_PARTY || player.getParty().isInCommandChannel() ?
						player.getParty().getCommandChannel().getMembers() : player.getParty().getPartyMembers());
			}

			for (Player enterPlayer : allPlayers) {
				if (enterPlayer == null) {
					continue;
				}

				bossZone.allowPlayerEntry(enterPlayer, 7200);

				enterPlayer.sendPacket(new EventTrigger(redTowerEffect, true));
				enterPlayer.teleToLocation(enterCords, true);
			}
		}

		return "";
	}

	@Override
	public final String onAdvEvent(String event, Npc npc, Player player) {
		if (debug) {
			log.warn(getName() + ": onAdvEvent: " + event);
		}

		if (event.equalsIgnoreCase("unlock_lindvior")) {
			Npc lindvior = addSpawn(secondFloorLindvior, -105200, -253104, -15264, 32768, false, 0);

			GrandBossManager.getInstance().addBoss((GrandBossInstance) lindvior);
			GrandBossManager.getInstance().setBossStatus(secondFloorLindvior, GrandBossManager.getInstance().ALIVE);

			Broadcast.toAllOnlinePlayers(new Earthquake(45697, -26269, -1409, 20, 10));
		} else if (event.equalsIgnoreCase("check_activity_task")) {
			if (!GrandBossManager.getInstance().isActive(secondFloorLindvior, LastAction)) {
				notifyEvent("end_lindvior", null, null);
			}
		} else if (event.equalsIgnoreCase("stage_1_start")) {
			SpawnTable.getInstance().spawnSpecificTable("lindvior_boss");

			bossStage = 1;

			bossZone.sendDelayedPacketToZone(5000, new ExShowScreenMessage(14211701, 0, true, 5000)); //You must activate the 4 Generators.

			//Generator guards should broadcast npcStringId 1802366

			dummyLindvior = addSpawn(lindviorCameraId, 45259, -27115, -638, 41325, false, 0, false);

			LastAction = System.currentTimeMillis();

			startQuestTimer("check_activity_task", 60000, null, null, true);
		} else if (event.equalsIgnoreCase("stage_1_activate_generator")) {
			if (bossStage == 1) {
				if (npc.getDisplayEffect() == 1) //Orange
				{
					npc.setDisplayEffect(2); //Blue

					int generatorEffect = _generatorEffect_1;
					if (Util.checkIfInRange(500, npc.getX(), npc.getY(), npc.getZ(), 45283, -30372, -1405, false)) {
						generatorEffect = _generatorEffect_2;
					} else if (Util.checkIfInRange(500, npc.getX(), npc.getY(), npc.getZ(), 45283, -23967, -1405, false)) {
						generatorEffect = _generatorEffect_3;
					} else if (Util.checkIfInRange(500, npc.getX(), npc.getY(), npc.getZ(), 42086, -27179, -1405, false)) {
						generatorEffect = _generatorEffect_4;
					}

					bossZone.broadcastPacket(new EventTrigger(generatorEffect, true));

					bossZone.broadcastPacket(new Earthquake(npc.getX(), npc.getY(), npc.getZ(), 10, 10));

					synchronized (manageGenerators) {
						if (manageGenerators.size() < 3) {
							bossZone.broadcastPacket(new ExShowScreenMessage(14211701, 0, true, 5000)); //You must activate the 4 Generators.
						}

						if (!manageGenerators.containsKey(npc)) {
							manageGenerators.put(npc, (double) 0);
						}

						if (manageGenerators.size() == 4) {
							//All generators are active now
							//Here the center shield should opens (missing) and Lindvior should appear
							bossStage = 2;

							//Spawn the dummy cyclone
							for (Npc generator : manageGenerators.keySet()) {
								if (generator == null) {
									continue;
								}

								//Display & invul maybe should be done before the intro movie
								generator.setDisplayEffect(1);
								generator.setInvul(false); //generator now can get damage
							}

							bossZone.broadcastPacket(new EventTrigger(allGeneratorsConnectedEffect, true));

							bossZone.broadcastPacket(new SocialAction(dummyLindvior.getObjectId(), 1));

							bossZone.broadcastPacket(new EventTrigger(shieldTowerEffect, true));
							bossZone.broadcastPacket(new EventTrigger(redTowerEffect, false));

							startQuestTimer("stage_2_intro_start", 6500, npc, null);
						}
					}
				}
			}
		} else if (event.equalsIgnoreCase("stage_2_intro_start")) {
			bossZone.showVidToZone(76);

			startQuestTimer("stage_2_start", ScenePlayerDataTable.getInstance().getVideoDuration(76) + 200, npc, null);
		} else if (event.equalsIgnoreCase("stage_2_start")) {
			dummyLindvior.deleteMe();
			dummyLindvior = addSpawn(flyingLindviorAroundZoneId, 45259, -27115, -638, 41325, false, 0, false);

			bossZone.broadcastPacket(new ExShowScreenMessage(14211702, 0, true, 5000)); //Protect the Generator!
		} else if (event.equalsIgnoreCase("stage_2_end")) {
			bossStage = 5;

			bossLocation = new Location(lindviorBoss.getX(), lindviorBoss.getY(), lindviorBoss.getZ(), lindviorBoss.getHeading());

			lindviorBoss.deleteMe();

			startQuestTimer("stage_3_start", 5000, npc, null);
		} else if (event.equalsIgnoreCase("stage_3_start")) {
			landingLindvior();
		} else if (event.equalsIgnoreCase("stage_3_end")) {
			bossStage = 7;

			bossLocation = new Location(lindviorBoss.getX(), lindviorBoss.getY(), lindviorBoss.getZ(), lindviorBoss.getHeading());

			lindviorBoss.deleteMe();

			startQuestTimer("stage_4_start", 5000, npc, null);
		} else if (event.equalsIgnoreCase("stage_4_start")) {
			landingLindvior();
		} else if (event.equalsIgnoreCase("stage_4_end")) {
			bossLocation = new Location(lindviorBoss.getX(), lindviorBoss.getY(), lindviorBoss.getZ(), lindviorBoss.getHeading());

			lindviorBoss.deleteMe();

			startQuestTimer("stage_5_start", 5000, npc, null);
		} else if (event.equalsIgnoreCase("stage_5_start")) {
			landingLindvior();
		} else if (event.equalsIgnoreCase("stage_5_end")) {
			bossLocation = new Location(lindviorBoss.getX(), lindviorBoss.getY(), lindviorBoss.getZ(), lindviorBoss.getHeading());

			lindviorBoss.deleteMe();

			startQuestTimer("stage_6_start", 5000, npc, null);
		} else if (event.equalsIgnoreCase("stage_6_start")) {
			landingLindvior();
		} else if (event.equalsIgnoreCase("spawn_minion_task")) {
			if (lindviorBoss != null && !lindviorBoss.isDead()) {
				if (Rnd.get(100) > 30) {
					if (bossStage < 9) //Can spawn Lyns at all stages, less at the last one
					{
						spawnMinions(lindviorBoss, Rnd.get(100) > 50 ? cycloneId : -1, 1000, 20);
					} else {
						spawnMinions(lindviorBoss, cycloneId, 1000, 20); //Only Cyclones
					}
				}

				//Big Cyclone only at last stage, always
				if (bossStage >= 9) {
					spawnMinions(lindviorBoss, giantCycloneId, 1, 2);
				}

				//Individual Player Cyclone always
				for (Player players : bossZone.getPlayersInside()) {
					if (players == null) {
						continue;
					}

					if (Rnd.get(100) > 40) {
						spawnMinions(players, cycloneId, 1, 1);
					}
				}
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
		} else if (event.equalsIgnoreCase("end_lindvior")) {
			notifyEvent("cancel_timers", null, null);

			bossZone.oustAllPlayers();

			manageGenerators.clear();

			bossStage = 0;

			SpawnTable.getInstance().despawnSpecificTable("lindvior_boss");

			for (Npc mob : bossZone.getNpcsInside()) {
				if (mob == null) {
					continue;
				}

				mob.getSpawn().stopRespawn();
				mob.deleteMe();
			}

			if (GrandBossManager.getInstance().getBossStatus(secondFloorLindvior) != GrandBossManager.getInstance().DEAD) {
				GrandBossManager.getInstance().setBossStatus(secondFloorLindvior, GrandBossManager.getInstance().ALIVE);
			}
		}

		return super.onAdvEvent(event, npc, player);
	}

	private void spawnMinions(Creature npc, int minionId, int rad, int amount) {
		int radius = rad;
		int mobCount = bossZone.getNpcsInside().size();
		if (mobCount < 70) {
			for (int i = 0; i < amount; i++) {
				int x = (int) (radius * Math.cos(i * 0.618));
				int y = (int) (radius * Math.sin(i * 0.618));

				MonsterInstance
						minion = (MonsterInstance) addSpawn(minionId == -1 ? lynDracoIds[Rnd.get(lynDracoIds.length)] : minionId,
						npc.getX() + x,
						npc.getY() + y,
						npc.getZ() + 20,
						-1,
						false,
						0,
						true,
						npc.getInstanceId());
				minion.setRunning(true);

				//To be sure
				if (!bossZone.isInsideZone(minion)) {
					minion.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
				}
			}
		}
	}

	@Override
	public final String onAttack(Npc npc, Player attacker, int damage, boolean isPet, Skill skill) {
		if (debug) {
			log.warn(getName() + ": onAttack: " + npc.getName());
		}

		LastAction = System.currentTimeMillis();

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
			int[] randPoint = bossZone.getZone().getRandomPoint();
			npc.teleToLocation(randPoint[0], randPoint[1], randPoint[2]);

			if (debug) {
				log.warn(getName() + ": Character: " + attacker.getName() + " attacked: " + npc.getName() + " wich is out of the boss zone!");
			}
		}

		if (npc.getNpcId() == generatorId) {
			if (bossStage == 2) {
				if (npc.getDisplayEffect() == 1)//Charge is Possible
				{
					synchronized (manageGenerators) {
						if (manageGenerators.containsKey(npc)) {
							if (manageGenerators.get(npc) == 0) //First attack
							{
								manageGenerators.put(npc, (double) damage);

								bossZone.broadcastPacket(new ExShowScreenMessage(14211702, 0, true, 5000)); //Protect the Generator!

								//Spawn Lyn Dracos
								spawnMinions(npc, -1, 600, 7);
							} else {
								//This should be done with one special skill but we will do it with damage....
								double generatorDamage = manageGenerators.get(npc); //Get the current damage from this generator
								double calcDamage = generatorDamage + damage;

								if (generatorDamage == maxGeneratorDamage) {
									return super.onAttack(npc, attacker, damage, isPet);
								} else {
									if (calcDamage >= maxGeneratorDamage) {
										manageGenerators.put(npc, maxGeneratorDamage);

										npc.broadcastPacket(new ExSendUIEvent(5, 120, 120, 16211701), 1200); //bar with the 100%

										bossZone.broadcastPacket(new ExShowScreenMessage("$s1 has charged the cannon!".replace("$s1",
												attacker.getName()), 5000)); //$s1 has charged the cannon!

										int count = 0;

										for (Entry<Npc, Double> generator : manageGenerators.entrySet()) {
											if (generator.getValue() == maxGeneratorDamage) {
												count++;
											}
										}

										if (count == 4) {
											//All generators are charged here
											bossStage = 3;

											GrandBossManager.getInstance()
													.setBossStatus(secondFloorLindvior, GrandBossManager.getInstance().FIGHTING);

											dummyLindvior.deleteMe(); //Delete the flying Lindvior

											//Lindvior fall to the scenario here
											bossZone.broadcastPacket(new Earthquake(npc.getX(), npc.getY(), npc.getZ(), 10, 10));

											landingLindvior();

											bossZone.sendDelayedPacketToZone(5000,
													new ExShowScreenMessage(14211708, 0, true, 5000)); //Lindvior has fallen from the sky!

											//Start the minion task
											startQuestTimer("spawn_minion_task", 3 * 60000, null, null, true);

											//At this point all the start instance npcs are deleted
											for (Creature chara : bossZone.getCharactersInside().values()) {
												if (chara == null || !(chara instanceof Npc)) {
													continue;
												}

												if (((Npc) chara).getNpcId() != firstFloorLindvior) {
													chara.deleteMe();
												}
											}

											//kick dual box
											bossZone.kickDualBoxes();
										}
									} else {
										manageGenerators.put(npc, calcDamage);

										double calc = generatorDamage / maxGeneratorDamage * 120;

										npc.broadcastPacket(new ExSendUIEvent(5, (int) calc, 120, 16211701), 1200);
									}
								}
							}
						}
					}
				}
			}
		} else if (npc.getNpcId() == firstFloorLindvior) {
			if (bossStage == 3) {
				if (npc.getCurrentHp() < npc.getMaxHp() * 0.50) //50%
				{
					bossStage = 4;

					takeOffLindvior();

					bossZone.broadcastPacket(new EventTrigger(redZoneEffect, true));
					bossZone.sendDelayedPacketToZone(15000, new EventTrigger(redZoneEffect, false));
					bossZone.broadcastPacket(new ExShowScreenMessage(14211705, 0, true, 5000)); //A fearsome power emanates from Lindvior!

					startQuestTimer("stage_2_end", 4000, npc, null); //TODO
				}
			}
		} else if (npc.getNpcId() == secondFloorLindvior) {
			if (bossStage == 7) {
				if (npc.getCurrentHp() < npc.getMaxHp() * 0.20) //20%
				{
					bossStage = 8;

					takeOffLindvior();

					startQuestTimer("stage_4_end", 5600, npc, null);
				}
			}
		} else if (npc.getNpcId() == flyLindvior) {
			if (bossStage == 5) {
				if (npc.getCurrentHp() < npc.getMaxHp() * 0.30) //30%
				{
					bossStage = 6;

					takeOffLindvior();

					startQuestTimer("stage_3_end", 5600, npc, null);
				}
			} else if (bossStage == 8) {
				if (npc.getCurrentHp() < npc.getMaxHp() * 0.20) //20%
				{
					bossStage = 9;

					takeOffLindvior();

					startQuestTimer("stage_5_end", 5600, npc, null);
				}
			}
		}

		return super.onAttack(npc, attacker, damage, isPet, skill);
	}

	private void takeOffLindvior() {
		switch (bossStage) {
			case 4:
			case 6:
			case 8:
			case 9:
				lindviorBoss.disableCoreAI(true);
				lindviorBoss.setInvul(true);
				lindviorBoss.setTarget(lindviorBoss);

				if (lindviorBoss.isCastingNow()) {
					lindviorBoss.abortCast();
				}

				lindviorBoss.doCast(takeOff);
				break;
		}
	}

	private void landingLindvior() {
		switch (bossStage) {
			case 3:
				lindviorBoss = addSpawn(firstFloorLindvior, 47180, -26122, -1407, 48490, false, 0, true);
				break;

			case 5:
			case 8:
				lindviorBoss = addSpawn(flyLindvior,
						bossLocation.getX(),
						bossLocation.getY(),
						bossLocation.getZ(),
						bossLocation.getHeading(),
						false,
						0,
						true);
				lindviorBoss.setDisplayEffect(1);
				lindviorBoss.setCurrentHp(lindviorBoss.getMaxHp() * (bossStage == 5 ? 0.85 : 0.40));
				break;

			case 7:
			case 9:
				lindviorBoss = addSpawn(secondFloorLindvior,
						bossLocation.getX(),
						bossLocation.getY(),
						bossLocation.getZ(),
						bossLocation.getHeading(),
						false,
						0,
						true);
				lindviorBoss.setCurrentHp(lindviorBoss.getMaxHp() * (bossStage == 7 ? 0.70 : 0.20));
				break;
		}

		lindviorBoss.setShowSummonAnimation(false);
	}

	@Override
	public String onKill(Npc npc, Player player, boolean isPet) {
		if (debug) {
			log.warn(getName() + ": onKill: " + npc.getName());
		}

		if (npc.getNpcId() == secondFloorLindvior) {
			GrandBossManager.getInstance().notifyBossKilled(secondFloorLindvior);

			notifyEvent("cancel_timers", null, null);

			bossZone.stopWholeZone();

			// Start the zone when the cameras ends
			ThreadPoolManager.getInstance().scheduleAi(new Runnable() {
				@Override
				public void run() {
					bossZone.startWholeZone();
				}
			}, 14000);

			startQuestTimer("unlock_lindvior", GrandBossManager.getInstance().getUnlockTime(secondFloorLindvior), null, null);
			startQuestTimer("end_lindvior", 1800000, null, null);
		}

		return super.onKill(npc, player, isPet);
	}

	@Override
	public int getOnKillDelay(int npcId) {
		return 0;
	}

	public static void main(String[] args) {
		new Lindvior(-1, qn, "ai/individual/GrandBosses");
	}
}
