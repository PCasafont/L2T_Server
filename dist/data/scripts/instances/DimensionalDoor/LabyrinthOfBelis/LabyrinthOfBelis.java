package instances.DimensionalDoor.LabyrinthOfBelis;

import ai.group_template.L2AttackableAIScript;
import instances.DimensionalDoor.DimensionalDoor;
import l2server.Config;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.ScenePlayerDataTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.GuardInstance;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.model.actor.instance.NpcBufferInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.NpcSay;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LasTravel
 * <p>
 * Source:
 * - http://www.youtube.com/watch?v=XIX2i6n1i8U
 */

public class LabyrinthOfBelis extends L2AttackableAIScript {
	private static final String qn = "LabyrinthOfBelis";

	//Config
	private static final boolean debug = false;
	private static final int reuseMinutes = 1440;

	//Ids
	private static final int instanceTemplateId = 178;
	private static final int generatorId = 80312;
	private static final int operativeId = 80313;
	private static final int handymanId = 80314;
	private static final int combatOfficer = 80310;
	private static final int markOfBelis = 17615;
	private static final int belisVerificationSystem = 80311;
	private static final int bossId = 80315;
	private static final int[][] operativeSpawns =
			{{-118589, 210903, -8592, 59724}, {-118095, 211293, -8592, 4477}, {-118125, 210983, -8592, 16358}, {-118586, 211547, -8592, 33149},
					{-118273, 210870, -8592, 52342}, {-118186, 211547, -8592, 42822}, {-118427, 211322, -8592, 3497},
					{-118236, 211452, -8592, 39798}};

	public LabyrinthOfBelis(int questId, String name, String descr) {
		super(questId, name, descr);

		addTalkId(DimensionalDoor.getNpcManagerId());
		addStartNpc(DimensionalDoor.getNpcManagerId());
		addFirstTalkId(combatOfficer);
		addTalkId(combatOfficer);
		addStartNpc(combatOfficer);
		addFirstTalkId(belisVerificationSystem);
		addTalkId(belisVerificationSystem);
		addStartNpc(belisVerificationSystem);
		addKillId(operativeId);
		addKillId(handymanId);
		addAttackId(operativeId);
		addAttackId(handymanId);
		addKillId(generatorId);
		addKillId(bossId);
		addAggroRangeEnterId(generatorId);
	}

	private class LabyrinthOfBelisWorld extends InstanceWorld {
		private Player instancePlayer;
		private GuardInstance officer;
		private Npc generator;
		private Npc walkingGuard;
		private List<Npc> operativeList;
		private boolean isOfficerWalking;
		private boolean isGuardAttacked;

		private LabyrinthOfBelisWorld() {
			operativeList = new ArrayList<Npc>();
			isOfficerWalking = false;
			isGuardAttacked = false;
		}
	}

	private static void moveTo(Npc walker, int x, int y, int z, int h) {
		if (walker == null) {
			return;
		}
		walker.setRunning(true);
		walker.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(x, y, z, h));
	}

	@Override
	public String onAggroRangeEnter(Npc npc, Player player, boolean isPet) {
		if (debug) {
			log.warn(getName() + ": onAggroRangeEnter: " + player.getName());
		}

		InstanceWorld wrld = null;
		if (npc != null) {
			wrld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		} else {
			wrld = InstanceManager.getInstance().getPlayerWorld(player);
		}

		if (wrld != null && wrld instanceof LabyrinthOfBelisWorld) {
			LabyrinthOfBelisWorld world = (LabyrinthOfBelisWorld) wrld;
			if (npc.getNpcId() == generatorId) {
				player.doDie(world.generator);
			}
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}

	@Override
	public final String onFirstTalk(Npc npc, Player player) {
		if (debug) {
			log.warn(getName() + ": onFirstTalk: " + player.getName());
		}

		InstanceWorld wrld = null;
		if (npc != null) {
			wrld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		} else if (player != null) {
			wrld = InstanceManager.getInstance().getPlayerWorld(player);
		}

		if (wrld != null && wrld instanceof LabyrinthOfBelisWorld) {
			LabyrinthOfBelisWorld world = (LabyrinthOfBelisWorld) wrld;
			if (npc.getNpcId() == combatOfficer) {
				switch (world.status) {
					case 0:
						return "1.html";
					case 2:
						return "2.html";
					case 4:
						return "3.html";
					case 6:
						return "4.html";
				}
			} else if (npc.getNpcId() == belisVerificationSystem) {
				return "33215.html";
			}
		}
		return super.onFirstTalk(npc, player);
	}

	@Override
	public final String onTalk(Npc npc, Player player) {
		if (debug) {
			log.warn(getName() + ": onTalk: " + player.getName());
		}

		if (npc.getNpcId() == DimensionalDoor.getNpcManagerId()) {
			return qn + ".html";
		}

		return super.onTalk(npc, player);
	}

	@Override
	public final String onAdvEvent(String event, Npc npc, Player player) {
		if (debug) {
			log.warn(getName() + ": onAdvEvent: " + event);
		}

		InstanceWorld wrld = null;
		if (npc != null) {
			wrld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		} else if (player != null) {
			wrld = InstanceManager.getInstance().getPlayerWorld(player);
		} else {
			log.warn(getName() + ": onAdvEvent: Unable to get world.");
			return null;
		}

		if (wrld != null && wrld instanceof LabyrinthOfBelisWorld) {
			LabyrinthOfBelisWorld world = (LabyrinthOfBelisWorld) wrld;
			if (event.equalsIgnoreCase("stage_1_start")) {
				world.instancePlayer = player;

				InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240001).openMe();

				world.generator = addSpawn(generatorId, -118253, 214706, -8584, 57541, false, 0, false, world.instanceId);
				world.generator.setMortal(false);

				world.officer = (GuardInstance) addSpawn(combatOfficer, -119061, 211151, -8592, 142, false, 0, false, world.instanceId);
				world.officer.setInvul(true);
				world.officer.setMortal(false);
				world.officer.setCanReturnToSpawnPoint(false);

				for (int[] spawn : operativeSpawns) {
					Npc operative = addSpawn(operativeId, spawn[0], spawn[1], spawn[2], spawn[3], false, 0, false, world.instanceId);
					synchronized (world.operativeList) {
						world.operativeList.add(operative);
					}
				}
			} else if (event.equalsIgnoreCase("stage_1_open_door")) {
				world.status = 1;

				InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240002).openMe();

				startQuestTimer("stage_all_officer_process", 2 * 1000, npc, null);
			} else if (event.equalsIgnoreCase("stage_1_end")) {
				world.status = 2;

				InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240003).openMe();
			} else if (event.equalsIgnoreCase("stage_2_start")) {
				world.isOfficerWalking = false;

				world.status = 3;

				InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240004).openMe();

				world.instancePlayer.sendPacket(new ExShowScreenMessage(1811199, 0, true, 5000));
			} else if (event.equalsIgnoreCase("stage_2_check_belis")) {
				long belisCount = player.getInventory().getInventoryItemCount(markOfBelis, 0);
				if (belisCount >= 3) {
					world.status = 4;

					player.destroyItemByItemId(qn, markOfBelis, belisCount, player, true);

					InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240005).openMe();
				}
			} else if (event.equalsIgnoreCase("stage_3_start")) {
				world.isOfficerWalking = false;

				world.status = 5;

				world.generator.setDisplayEffect(1);

				InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240006).openMe();

				world.instancePlayer.sendPacket(new ExShowScreenMessage(1811197, 0, true, 3000));

				world.officer.broadcastPacket(new NpcSay(world.officer.getObjectId(), 0, world.officer.getTemplate().TemplateId, 1811217));
				world.officer.broadcastPacket(new NpcSay(world.officer.getObjectId(), 0, world.officer.getTemplate().TemplateId, 1600025));
				world.officer.setInvul(false);
				world.officer.setMortal(true);

				startQuestTimer("stage_3_spawn_guard", 3000, npc, null);
				startQuestTimer("stage_3_generator_die", 60000, npc, null);
			} else if (event.equalsIgnoreCase("stage_3_spawn_guard")) {
				world.isGuardAttacked = false;

				world.walkingGuard = null;

				int guardId = 0;

				if (Rnd.get(2) == 0) {
					guardId = operativeId;
				} else {
					guardId = handymanId;
				}

				world.walkingGuard = addSpawn(guardId, -116772, 213344, -8599, 24341, false, 0, false, world.instanceId);
				world.walkingGuard.broadcastPacket(new NpcSay(world.walkingGuard.getObjectId(),
						0,
						world.walkingGuard.getTemplate().TemplateId,
						guardId == operativeId ? 1811196 : 1811195));

				world.instancePlayer.sendPacket(new ExShowScreenMessage(guardId == operativeId ? 1811194 : 1811194, 0, true, 5000));

				startQuestTimer("stage_3_guard_attack", 1000, world.walkingGuard, null);
			} else if (event.equalsIgnoreCase("stage_3_guard_attack")) {
				if (world.generator != null && world.walkingGuard.getDistanceSq(world.generator) >= 100) {
					if (world.status < 6 && !world.isGuardAttacked) {
						moveTo(world.walkingGuard,
								world.generator.getX(),
								world.generator.getY(),
								world.generator.getZ(),
								world.generator.getHeading());
					}

					startQuestTimer("stage_3_guard_attack", 3000, world.walkingGuard, null);
				} else {
					world.walkingGuard.setTarget(world.officer);
					((Attackable) world.walkingGuard).addDamageHate(world.officer, 500, 99999);
					world.walkingGuard.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world.officer);
				}
			} else if (event.equalsIgnoreCase("stage_3_generator_die")) {
				world.generator.doDie(world.instancePlayer);
			} else if (event.equalsIgnoreCase("stage_last_start")) {
				world.status = 7;

				InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240008).openMe();

				world.instancePlayer.showQuestMovie(43);

				startQuestTimer("stage_last_spawn_boss", ScenePlayerDataTable.getInstance().getVideoDuration(43), npc, null);
			} else if (event.equalsIgnoreCase("stage_last_spawn_boss")) {
				addSpawn(bossId, -118337, 212976, -8679, 24463, false, 0, false, world.instanceId);
			} else if (event.equalsIgnoreCase("stage_all_officer_process")) {
				//TODO Not good, but manage it is very hard
				if (world.status > 6) {
					return "";
				}

				//Instance fail
				if (world.officer == null || world.officer.isDead()) {
					InstanceManager.getInstance().finishInstance(world.instanceId, true);
					return "";
				}

				if (world.instancePlayer != null && !world.instancePlayer.isDead()) {
					switch (world.status) {
						case 1:
						case 3:
							if (!world.isOfficerWalking) {
								WorldObject target = world.instancePlayer.getTarget();
								if (target == null || !(target instanceof MonsterInstance) ||
										target instanceof MonsterInstance && ((MonsterInstance) target).isDead()) {
									if (world.officer.getAI().getIntention() != CtrlIntention.AI_INTENTION_FOLLOW) {
										world.officer.setRunning(true);
										world.officer.setTarget(world.instancePlayer);
										world.officer.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, world.instancePlayer);
									}
								} else {
									if (target instanceof MonsterInstance) {
										if (!((MonsterInstance) target).isInsideRadius(world.officer, 300, false, false)) {
											world.officer.getAI()
													.setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
															new L2CharPosition(target.getX(), target.getY(), target.getZ(), 0));
										} else {
											world.officer.setTarget(target);
											((Attackable) world.officer).addDamageHate((MonsterInstance) target, 500, 99999);
											world.officer.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
										}
									}
								}
							}
							break;

						case 2:
							moveTo(world.officer, -117069, 212520, -8592, 41214);
							break;

						case 4:
							moveTo(world.officer, -117869, 214231, -8592, 57052);
							break;

						case 5:
							if (world.officer.getDistanceSq(world.generator) >= 100) {
								moveTo(world.officer,
										world.generator.getX(),
										world.generator.getY(),
										world.generator.getZ(),
										world.generator.getHeading());
							} else {
								world.officer.setTarget(world.generator);
								((Attackable) world.officer).addDamageHate(world.generator, 500, 99999);
								world.officer.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world.generator);
							}
							break;

						case 6:
							moveTo(world.officer, -119242, 213768, -8592, 24575);
							break;
					}
				}
				startQuestTimer("stage_all_officer_process", 2 * 1000, npc, null);
			}
		}

		if (event.equalsIgnoreCase("enterToInstance")) {
			try {
				enterInstance(player);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	@Override
	public final String onAttack(Npc npc, Player attacker, int damage, boolean isPet) {
		if (debug) {
			log.warn(getName() + ": onAttack: " + npc.getName());
		}

		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof LabyrinthOfBelisWorld) {
			LabyrinthOfBelisWorld world = (LabyrinthOfBelisWorld) tmpworld;
			if (world.status == 5 && (npc.getNpcId() == operativeId || npc.getNpcId() == handymanId)) {
				if (!world.isGuardAttacked) {
					world.isGuardAttacked = true;
					if (world.walkingGuard != null && !world.walkingGuard.isDead()) {
						world.walkingGuard.stopMove(null);
					}
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(Npc npc, Player player, boolean isPet) {
		if (debug) {
			log.warn(getName() + ": onKill: " + npc.getName());
		}

		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof LabyrinthOfBelisWorld) {
			LabyrinthOfBelisWorld world = (LabyrinthOfBelisWorld) tmpworld;
			switch (npc.getNpcId()) {
				case operativeId:

					if (world.status == 1) {
						synchronized (world.operativeList) {
							if (world.operativeList.contains(npc)) {
								world.operativeList.remove(npc);
							}

							if (world.operativeList.isEmpty()) {
								startQuestTimer("stage_1_end", 1000, null, player);
							}
						}
					} else if (world.status == 5) {
						startQuestTimer("stage_3_spawn_guard", 1000, npc, null);
					}
					break;

				case handymanId:
					if (world.status == 3) {
						world.instancePlayer.sendPacket(new ExShowScreenMessage(1811199, 0, true, 5000));
						if (Rnd.get(10) > 6) {
							((MonsterInstance) npc).dropItem(player, markOfBelis, 1);
						}
					} else if (world.status == 5) {
						startQuestTimer("stage_3_spawn_guard", 1000, npc, null);
					}
					break;

				case generatorId:
					world.isOfficerWalking = false;

					world.status = 6;

					world.instancePlayer.sendPacket(new ExShowScreenMessage(1811198, 0, true, 5000));

					InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240007).openMe();
					break;

				case bossId:
					world.instancePlayer.showQuestMovie(44);

					InstanceManager.getInstance().setInstanceReuse(world.instanceId, instanceTemplateId, reuseMinutes);
					InstanceManager.getInstance().finishInstance(world.instanceId, true);

					player.addItem(qn, DimensionalDoor.getDimensionalDoorRewardId(), 10, player, true);
					break;
			}
		}
		return "";
	}

	private final synchronized void enterInstance(Player player) {
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null) {
			if (!(world instanceof LabyrinthOfBelisWorld)) {
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}

			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if (inst != null) {
				if (inst.getInstanceEndTime() > 300600 && world.allowed.contains(player.getObjectId())) {
					player.deleteAllItemsById(markOfBelis);
					player.setInstanceId(world.instanceId);
					player.teleToLocation(-119941, 211146, -8590, true);

					NpcBufferInstance.giveBasicBuffs(player);
				}
			}
			return;
		} else {
			if (!debug && !InstanceManager.getInstance().checkInstanceConditions(player, instanceTemplateId, 1, 1, 99, Config.MAX_LEVEL)) {
				return;
			}

			final int instanceId = InstanceManager.getInstance().createDynamicInstance(qn + ".xml");
			world = new LabyrinthOfBelisWorld();
			world.instanceId = instanceId;
			world.status = 0;

			InstanceManager.getInstance().addWorld(world);

			world.allowed.add(player.getObjectId());

			player.stopAllEffectsExceptThoseThatLastThroughDeath();
			player.deleteAllItemsById(markOfBelis);
			player.setInstanceId(instanceId);
			player.teleToLocation(-119941, 211146, -8590, true);

			NpcBufferInstance.giveBasicBuffs(player);

			startQuestTimer("stage_1_start", 4000, null, player);

			log.debug(getName() + ": instance started: " + instanceId + " created by player: " + player.getName());
			return;
		}
	}

	public static void main(String[] args) {
		new LabyrinthOfBelis(-1, qn, "instances/DimensionalDoor");
	}
}
