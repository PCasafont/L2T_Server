package instances.DimensionalDoor.Spezion;

import ai.group_template.L2AttackableAIScript;
import instances.DimensionalDoor.DimensionalDoor;
import l2server.Config;
import l2server.gameserver.GeoData;
import l2server.gameserver.datatables.ScenePlayerDataTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.NicknameChanged;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.VisualEffect;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LasTravel
 * <p>
 * Spezion Boss - Normal/Extreme Mode
 * <p>
 * Source:
 * - http://www.youtube.com/watch?v=RausqeXJ_rs&feature=player_embedded
 * - http://www.youtube.com/watch?v=nLVx8PPcXy8&feature=player_embedded
 */

public class Spezion extends L2AttackableAIScript {
	//Quest
	private static final String qn = "Spezion";
	private static final boolean debug = false;

	//Ids
	private static final int giantCannonball = 17611;
	private static final int fakeSpezion = 25868;
	private static final int[] cannonIds = {32939, 32940, 32941, 32942};
	private static final int[] allMobs = {25779, 25780, 25781, 25782, 25867, 25872, 25873, 25874};
	private static final Location enterCords = new Location(175475, 145044, -11897);
	private static final Skill cannonBlast = SkillTable.getInstance().getInfo(14175, 1);

	public Spezion(int questId, String name, String descr) {
		super(questId, name, descr);

		addTalkId(DimensionalDoor.getNpcManagerId());
		addStartNpc(DimensionalDoor.getNpcManagerId());

		for (int a : cannonIds) {
			addTalkId(a);
			addStartNpc(a);
			addSpellFinishedId(a);
		}

		for (int a : allMobs) {
			addAttackId(a);
			addKillId(a);
			addSpawnId(a);
		}
	}

	private class PrisonOfDarknessWorld extends InstanceWorld {
		private boolean isHardMode;
		private Npc spezionBoss;
		private int spezionId;
		private int[] spezionGuards;
		private List<Npc> minions;
		private List<Npc> fakeMonsters;
		private ArrayList<Player> rewardedPlayers;

		private PrisonOfDarknessWorld() {
			isHardMode = false;
			spezionGuards = new int[3];
			minions = new ArrayList<Npc>();
			fakeMonsters = new ArrayList<Npc>();
			rewardedPlayers = new ArrayList<Player>();
		}
	}

	@Override
	public String onKill(Npc npc, Player player, boolean isPet) {
		if (debug) {
			log.warn(getName() + ": onKill: " + npc.getName());
		}

		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof PrisonOfDarknessWorld) {
			PrisonOfDarknessWorld world = (PrisonOfDarknessWorld) tmpworld;
			if (Util.contains(world.spezionGuards, npc.getNpcId())) {
				synchronized (world.minions) {
					if (world.minions.contains(npc)) {
						world.minions.remove(npc);
					}
				}
			} else if (npc == world.spezionBoss) {
				int maxReward = world.isHardMode ? 20 : 10;
				if (player.isInParty()) {
					for (Player pMember : player.getParty().getPartyMembers()) {
						if (pMember == null || pMember.getInstanceId() != world.instanceId) {
							continue;
						}

						//Reward
						if (InstanceManager.getInstance().canGetUniqueReward(pMember, world.rewardedPlayers)) {
							world.rewardedPlayers.add(pMember);
							pMember.addItem(qn, DimensionalDoor.getDimensionalDoorRewardId(), 35, player, true);
						} else {
							pMember.sendMessage("Nice attempt, but you already got a reward!");
						}
					}
				}

				InstanceManager.getInstance().setInstanceReuse(world.instanceId, world.isHardMode ? 196 : 159, 6, 30);
				InstanceManager.getInstance().finishInstance(world.instanceId, true);
				InstanceManager.getInstance().showVidToInstance(54, world.instanceId);
			}
		}

		return super.onKill(npc, player, isPet);
	}

	@Override
	public final String onTalk(Npc npc, Player player) {
		if (debug) {
			log.warn(getName() + ": onTalk: " + player.getName());
		}

		int npcId = npc.getNpcId();
		if (npcId == DimensionalDoor.getNpcManagerId()) {
			return qn + ".html";
		} else if (Util.contains(cannonIds, npc.getNpcId())) {
			if (npc.getInstanceId() != 0) {
				notifyEvent("stage_all_cannon", npc, player);
			}
		}

		return "";
	}

	@Override
	public String onSpellFinished(Npc npc, Player player, Skill skill) {
		if (debug) {
			log.warn(getName() + ": onSpellFinished: " + skill.getName());
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

		if (wrld != null && wrld instanceof PrisonOfDarknessWorld) {
			PrisonOfDarknessWorld world = (PrisonOfDarknessWorld) wrld;
			if (Util.contains(cannonIds, npc.getNpcId())) {
				if (skill == cannonBlast) {
					npc.setTitle("Empty Cannon");
					npc.broadcastPacket(new NicknameChanged(npc));

					world.spezionBoss.setDisplayEffect(2);
					world.spezionBoss.setInvul(false);
					world.spezionBoss.stopVisualEffect(VisualEffect.S_INVINCIBLE);

					if (world.isHardMode) //Delete the fake shits
					{
						for (Npc fakeMonster : world.fakeMonsters) {
							fakeMonster.deleteMe();
						}
						world.fakeMonsters.clear();
					}
					startQuestTimer("stage_all_spezion_back", 60000, npc, null);
				}
			}
		}

		return super.onSpellFinished(npc, player, skill);
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

		if (wrld != null && wrld instanceof PrisonOfDarknessWorld) {
			PrisonOfDarknessWorld world = (PrisonOfDarknessWorld) wrld;
			if (event.equalsIgnoreCase("stage_1_start")) {
				InstanceManager.getInstance().showVidToInstance(53, world.instanceId);

				startQuestTimer("stage_1_spawns", ScenePlayerDataTable.getInstance().getVideoDuration(53) + 2000, null, player);
			} else if (event.equalsIgnoreCase("stage_1_spawns")) {
				world.spezionBoss = addSpawn(world.spezionId, 175474, 143502, -11769, 15397, true, 0, false, world.instanceId);
				world.spezionBoss.setInvul(true);
				world.spezionBoss.startVisualEffect(VisualEffect.S_INVINCIBLE);

				if (world.isHardMode) {
					notifyEvent("stage_all_spawn_fake_spezions", world.spezionBoss, null);
				}

				InstanceManager.getInstance().sendPacket(world.instanceId, new ExShowScreenMessage(1811153, 5000));

				startQuestTimer("stage_all_spawn_minions", 10000, world.spezionBoss, null);
			} else if (event.equalsIgnoreCase("stage_all_spawn_fake_spezions")) {
				//Fake Invul Spezions
				for (int i = 0; i < 3; i++) {
					Npc fakeSpezion = addSpawn(this.fakeSpezion,
							world.spezionBoss.getX(),
							world.spezionBoss.getY(),
							world.spezionBoss.getZ(),
							0,
							true,
							0,
							false,
							world.instanceId);
					world.fakeMonsters.add(fakeSpezion);
					fakeSpezion.setInvul(true);
					fakeSpezion.startVisualEffect(VisualEffect.S_INVINCIBLE);
				}

				//It's Invul minions
				List<Npc> toIterate = new ArrayList<Npc>(world.fakeMonsters);
				for (Npc fakeSpezion : toIterate) {
					for (int a = 1; a < 3; a++) {
						for (int b : world.spezionGuards) {
							Npc newGuard = addSpawn(b,
									fakeSpezion.getX(),
									fakeSpezion.getY(),
									fakeSpezion.getZ(),
									fakeSpezion.getHeading(),
									true,
									0,
									false,
									world.instanceId);
							world.fakeMonsters.add(newGuard);
							((MonsterInstance) newGuard).setIsRaidMinion(true);
							newGuard.setRunning(true);
							newGuard.setInvul(true);
						}
					}
				}
			} else if (event.equalsIgnoreCase("stage_all_spawn_minions")) {
				if (world.spezionBoss != null && !world.spezionBoss.isDead()) {
					if (world.spezionBoss.getDisplayEffect() != 2) {
						synchronized (world.minions) {
							if (world.minions.isEmpty()) {
								for (int a = 1; a < 3; a++) {
									for (int b : world.spezionGuards) {
										Npc newGuard = addSpawn(b,
												world.spezionBoss.getX(),
												world.spezionBoss.getY(),
												world.spezionBoss.getZ(),
												world.spezionBoss.getHeading(),
												true,
												0,
												false,
												world.instanceId);
										world.minions.add(newGuard);
										((MonsterInstance) newGuard).setIsRaidMinion(true);
										newGuard.setRunning(true);
									}
								}
							}
						}
					}

					startQuestTimer("stage_all_spawn_minions", 5000, world.spezionBoss, null);
				}
			} else if (event.equalsIgnoreCase("stage_all_spezion_back")) {
				if (world.spezionBoss != null && !world.spezionBoss.isDead()) {
					world.spezionBoss.setDisplayEffect(3);
					world.spezionBoss.setInvul(true);
					world.spezionBoss.startVisualEffect(VisualEffect.S_INVINCIBLE);

					if (world.isHardMode) {
						notifyEvent("stage_all_spawn_fake_spezions", world.spezionBoss, null);
					}
				}
			} else if (event.equalsIgnoreCase("stage_all_cannon")) {
				if (world.spezionBoss == null || world.spezionBoss.isDead() || world.spezionBoss.getDisplayEffect() == 2) {
					return "";
				}

				long cannonBallCount = player.getInventory().getInventoryItemCount(giantCannonball, 0);
				if (cannonBallCount == 0) {
					return npc.getTemplate().TemplateId + "-1.html";
				}

				if (!Util.checkIfInRange(800, npc, world.spezionBoss, false) && Util.checkIfInRange(1300, npc, world.spezionBoss, false) &&
						GeoData.getInstance().canSeeTarget(npc, world.spezionBoss)) {
					player.destroyItemByItemId(qn, giantCannonball, 1, npc, true);
					player.getInventory().destroyItem(qn, giantCannonball, 1, player, player);

					npc.setTitle("Loading Cannon");
					npc.broadcastPacket(new NicknameChanged(npc));
					npc.setTarget(world.spezionBoss);
					npc.doCast(cannonBlast);
				}

				if (debug) {
					log.warn(getName() + ": Range: " +
							Util.calculateDistance(npc.getX(), npc.getY(), world.spezionBoss.getX(), world.spezionBoss.getY()));
				}
			}
		} else if (event.startsWith("enterToInstance_")) {
			try {
				enterInstance(player, Integer.valueOf(event.replace("enterToInstance_", "")));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	private final synchronized void enterInstance(Player player, int template_id) {
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null) {
			if (!(world instanceof PrisonOfDarknessWorld)) {
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}

			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if (inst != null) {
				if (inst.getInstanceEndTime() > 300600 && world.allowed.contains(player.getObjectId())) {
					player.deleteAllItemsById(giantCannonball);
					player.setInstanceId(world.instanceId);
					player.teleToLocation(175373, 144292, -11818);
				}
			}

			return;
		} else {
			if (!debug && !InstanceManager.getInstance().checkInstanceConditions(player, template_id, 7, 7, 92, Config.MAX_LEVEL)) {
				return;
			}

			final int instanceId = InstanceManager.getInstance().createDynamicInstance(qn + ".xml");
			world = new PrisonOfDarknessWorld();
			world.instanceId = instanceId;
			world.status = 0;

			InstanceManager.getInstance().addWorld(world);

			setupIDs((PrisonOfDarknessWorld) world, template_id);

			List<Player> allPlayers = new ArrayList<Player>();
			if (debug) {
				allPlayers.add(player);
			} else {
				allPlayers.addAll(player.getParty().getPartyMembers());
			}

			for (Player enterPlayer : allPlayers) {
				if (enterPlayer == null) {
					continue;
				}

				world.allowed.add(enterPlayer.getObjectId());

				enterPlayer.stopAllEffectsExceptThoseThatLastThroughDeath();

				enterPlayer.deleteAllItemsById(giantCannonball);
				enterPlayer.setInstanceId(instanceId);
				enterPlayer.teleToLocation(enterCords, true);
			}

			startQuestTimer("stage_1_start", 60000, null, player);

			log.debug(getName() + ":  instance started: " + instanceId + " created by player: " + player.getName());
			return;
		}
	}

	private void setupIDs(PrisonOfDarknessWorld world, int template_id) {
		if (template_id == 159) //Easy Mode
		{
			world.spezionId = 25779;
			for (int a = 1; a < 4; a++) {
				world.spezionGuards[a - 1] = world.spezionId + a;
			}
		} else
		//196 Extreme
		{
			world.isHardMode = true;
			world.spezionId = 25867;
			for (int a = 1; a < 4; a++) {
				world.spezionGuards[a - 1] = world.spezionId + 4 + a;
			}
		}
	}

	public static void main(String[] args) {
		new Spezion(-1, qn, "instances/DimensionalDoor");
	}
}
