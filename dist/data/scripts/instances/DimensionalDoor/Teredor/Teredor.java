package instances.DimensionalDoor.Teredor;

import ai.group_template.L2AttackableAIScript;
import instances.DimensionalDoor.DimensionalDoor;
import l2server.Config;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.ai.L2AttackableAI;
import l2server.gameserver.ai.L2NpcWalkerAI;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.L2NpcWalkerNode;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LasTravel
 * <p>
 * Terador Boss - Default mode
 * <p>
 * Source:
 * - http://www.youtube.com/watch?v=fFERpRhW52E
 * - http://www.youtube.com/watch?v=iNMNTe2L3qU
 * - http://www.lineage-realm.com/community/lineage-2-talk/quest-walkthroughs/Trajan-Instance-Guide-killing-teredor
 */

public class Teredor extends L2AttackableAIScript {
	//Quest
	private static final boolean debug = false;
	private static final String qn = "Teredor";

	//Id's
	private static final int instanceTemplateId = 160;
	private static final int teredor = 25785;
	private static final int egg1 = 19023;
	private static final int egg2 = 18996;
	private static final int eliteMillipede = 19015;
	private static final int teredorTransparent1 = 18998;
	private static final int adventureGuildsman = 33385;
	private static final int[] eggMinions = {18993, 19016, 19000, 18995, 18994};
	private static final int[] allMobs = {18993, 19016, 19000, 18995, 18994, 19023, 25785, 18996, 19015, 19024};

	//Skills
	private static final L2Skill teredorFluInfection = SkillTable.getInstance().getInfo(14113, 1);

	//Spawns
	private static final int[] adventureSpawn = {177228, -186305, -3800, 339};

	//Others
	private static List<L2NpcWalkerNode> route = new ArrayList<L2NpcWalkerNode>();

	//Cords
	private static final Location[] playerEnter =
			{new Location(186933, -173534, -3878), new Location(186787, -173618, -3878), new Location(186907, -173708, -3878),
					new Location(187048, -173699, -3878), new Location(186998, -173579, -3878)};

	private static final int[][] walkRoutes =
			{{177127, -185282, -3804, 19828}, {177138, -184701, -3804, 16417}, {176616, -184448, -3796, 29126}, {176067, -184240, -3804, 28990},
					{176038, -184806, -3804, 48098}, {175494, -185097, -3804, 37891}, {175347, -185711, -3804, 46649},
					{175912, -186311, -3804, 57030}, {176449, -186195, -3804, 1787}};

	private class TeredorWorld extends InstanceWorld {
		private L2Npc Teredor;
		private boolean bossIsReady;
		private boolean bossIsInPause;
		private L2NpcWalkerAI teredorWalkAI;
		private L2AttackableAI teredorAttackAI;
		private ArrayList<L2PcInstance> rewardedPlayers;

		public TeredorWorld() {
			bossIsReady = true;
			bossIsInPause = true;
			rewardedPlayers = new ArrayList<L2PcInstance>();
		}
	}

	public Teredor(int questId, String name, String descr) {
		super(questId, name, descr);

		addTalkId(DimensionalDoor.getNpcManagerId());
		addStartNpc(DimensionalDoor.getNpcManagerId());

		addTalkId(adventureGuildsman);
		addSpellFinishedId(teredor);
		addAggroRangeEnterId(egg1);
		addAggroRangeEnterId(egg2);
		addAggroRangeEnterId(teredorTransparent1);

		for (int id : allMobs) {
			addKillId(id);
			addAttackId(id);
		}

		for (int[] coord : walkRoutes) {
			route.add(new L2NpcWalkerNode(coord[0], coord[1], coord[2], 0, "", true));
		}
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill) {
		if (debug) {
			Log.warning(getName() + ": onSpellFinished: " + skill.getName());
		}

		InstanceWorld wrld = null;
		if (npc != null) {
			wrld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		} else if (player != null) {
			wrld = InstanceManager.getInstance().getPlayerWorld(player);
		} else {
			Log.warning(getName() + ": onAdvEvent: Unable to get world.");
			return null;
		}

		if (wrld != null && wrld instanceof TeredorWorld) {
			TeredorWorld world = (TeredorWorld) wrld;
			if (npc.getNpcId() == teredor && skill.getId() == 14112) //Teredor Poison
			{
				for (L2PcInstance players : L2World.getInstance().getAllPlayers().values()) {
					if (players != null && players.getInstanceId() == world.instanceId) {
						addSpawn(teredorTransparent1, players.getX(), players.getY(), players.getZ(), 0, false, 0, true, world.instanceId);
					}
				}
			}
		}

		return super.onSpellFinished(npc, player, skill);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player) {
		if (debug) {
			Log.warning(getName() + ": onAdvEvent: " + event);
		}

		InstanceWorld wrld = null;
		if (npc != null) {
			wrld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		} else if (player != null) {
			wrld = InstanceManager.getInstance().getPlayerWorld(player);
		} else {
			Log.warning(getName() + ": onAdvEvent: Unable to get world.");
			return null;
		}

		if (wrld != null && wrld instanceof TeredorWorld) {
			TeredorWorld world = (TeredorWorld) wrld;
			if (event.equalsIgnoreCase("stage_1_spawn_boss")) {
				world.Teredor = addSpawn(teredor, 177228, -186305, -3800, 59352, false, 0, false, world.instanceId);

				startBossWalk(world);
			} else if (event.equalsIgnoreCase("stage_all_attack_again")) {
				world.bossIsReady = true;
			} else if (event.equalsIgnoreCase("stage_all_egg")) {
				if (npc != null && !npc.isDead() && npc.getTarget() != null) {
					spawnMinions(world, npc, npc.getTarget().getActingPlayer(), eggMinions[Rnd.get(eggMinions.length)], 1);
				}
			}
		} else if (event.equalsIgnoreCase("enterToInstance")) {
			try {
				enterInstance(player);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	@Override
	public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet) {
		if (debug) {
			Log.warning(getName() + ": onAttack: " + attacker.getName());
		}

		final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpWorld instanceof TeredorWorld) {
			final TeredorWorld world = (TeredorWorld) tmpWorld;
			if (npc.getNpcId() == teredor) {
				if (world.bossIsInPause && world.bossIsReady) {
					world.bossIsInPause = false;

					stopBossWalk(world);

					spawnMinions(world, npc, attacker, eliteMillipede, 3);
				} else if (!world.bossIsInPause && world.bossIsReady && (world.status == 0 && npc.getCurrentHp() < npc.getMaxHp() * 0.85 ||
						world.status == 1 && npc.getCurrentHp() < npc.getMaxHp() * 0.50)) {
					world.status++;
					world.bossIsInPause = true;
					world.bossIsReady = false;

					startBossWalk(world);

					startQuestTimer("stage_all_attack_again", 60000, npc, null);
				}
			}
		}

		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet) {
		if (debug) {
			Log.warning(getName() + ": onAggroRangeEnter: " + player.getName());
		}

		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof TeredorWorld) {
			if ((npc.getNpcId() == egg1 || npc.getNpcId() == egg2) && npc.getDisplayEffect() == 0) {
				if (npc.getNpcId() == egg1) {
					npc.setDisplayEffect(3);
				} else if (npc.getNpcId() == egg2) {
					npc.setDisplayEffect(2);
				}

				npc.setTarget(player);

				//Custom but funny
				if (Rnd.get(100) > 30) {
					npc.doCast(teredorFluInfection);
				}

				startQuestTimer("stage_all_egg", 5000, npc, null); // 5sec?
			} else if (npc.getNpcId() == teredorTransparent1) {
				npc.setTarget(player);
				npc.doCast(teredorFluInfection);
			}
		}

		return super.onAggroRangeEnter(npc, player, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet) {
		if (debug) {
			Log.warning(getName() + ": onKill: " + npc.getName());
		}

		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof TeredorWorld) {
			TeredorWorld world = (TeredorWorld) tmpworld;
			if (npc.getNpcId() == teredor) {
				addSpawn(adventureGuildsman,
						adventureSpawn[0],
						adventureSpawn[1],
						adventureSpawn[2],
						adventureSpawn[3],
						false,
						0,
						false,
						world.instanceId);

				if (player.isInParty()) {
					for (L2PcInstance pMember : player.getParty().getPartyMembers()) {
						if (pMember == null || pMember.getInstanceId() != world.instanceId) {
							continue;
						}

						//Check if any char is moving if yes spawn random millipede's
						if (pMember.isMoving()) {
							spawnMinions(world, npc, pMember, eliteMillipede, Rnd.get(1, 2));
						}

						//Reward
						if (InstanceManager.getInstance().canGetUniqueReward(pMember, world.rewardedPlayers)) {
							world.rewardedPlayers.add(pMember);
							pMember.addItem(qn, DimensionalDoor.getDimensionalDoorRewardId(), 20, player, true);
						} else {
							pMember.sendMessage("Nice attempt, but you already got a reward!");
						}
					}
				}

				InstanceManager.getInstance().setInstanceReuse(world.instanceId, instanceTemplateId, 6, 30);
				InstanceManager.getInstance().finishInstance(world.instanceId, false);
			}
		}

		return super.onKill(npc, player, isPet);
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player) {
		if (debug) {
			Log.warning(getName() + ": onTalk: " + player.getName());
		}

		if (npc.getNpcId() == DimensionalDoor.getNpcManagerId()) {
			return qn + ".html";
		} else if (npc.getNpcId() == adventureGuildsman) {
			player.setInstanceId(0);
			player.teleToLocation(85636, -142530, -1336, true);
		}

		return super.onTalk(npc, player);
	}

	private void stopBossWalk(TeredorWorld world) {
		world.Teredor.stopMove(null);
		world.teredorWalkAI.cancelTask();
		world.Teredor.setIsInvul(false);
		world.teredorAttackAI = new L2AttackableAI(world.Teredor);
		world.Teredor.setAI(world.teredorAttackAI);
	}

	private void startBossWalk(TeredorWorld world) {
		if (world.Teredor.isCastingNow()) {
			world.Teredor.abortCast();
		}

		world.Teredor.setIsInvul(true);
		world.Teredor.setIsRunning(true);
		world.teredorWalkAI = new L2NpcWalkerAI(world.Teredor);
		world.Teredor.setAI(world.teredorWalkAI);
		world.teredorWalkAI.initializeRoute(route, null);
		world.teredorWalkAI.walkToLocation();
	}

	private void spawnMinions(TeredorWorld world, L2Npc npc, L2Character target, int npcId, int count) {
		if (!Util.checkIfInRange(700, npc, target, true)) {
			return;
		}

		for (int id = 0; id < count; id++) {
			L2Attackable minion =
					(L2Attackable) addSpawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, false, world.instanceId);
			minion.setIsRunning(true);

			if (target != null) {
				minion.addDamageHate(target, 0, 500);
				minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
	}

	private final synchronized void enterInstance(L2PcInstance player) {
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null) {
			if (!(world instanceof TeredorWorld)) {
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}

			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if (inst != null) {
				if (inst.getInstanceEndTime() > 300600 && world.allowed.contains(player.getObjectId())) {
					player.setInstanceId(world.instanceId);
					player.teleToLocation(186933, -173534, -3878, true);
				}
			}
			return;
		} else {
			if (!debug && !InstanceManager.getInstance().checkInstanceConditions(player, instanceTemplateId, 7, 7, 92, Config.MAX_LEVEL)) {
				return;
			}

			final int instanceId = InstanceManager.getInstance().createDynamicInstance(qn + ".xml");

			world = new TeredorWorld();
			world.instanceId = instanceId;
			world.templateId = instanceTemplateId;
			world.status = 0;

			InstanceManager.getInstance().addWorld(world);

			List<L2PcInstance> allPlayers = new ArrayList<L2PcInstance>();
			if (debug) {
				allPlayers.add(player);
			} else {
				allPlayers.addAll(player.getParty().getPartyMembers());
			}

			for (L2PcInstance enterPlayer : allPlayers) {
				if (enterPlayer == null) {
					continue;
				}

				world.allowed.add(enterPlayer.getObjectId());

				enterPlayer.stopAllEffectsExceptThoseThatLastThroughDeath();
				enterPlayer.setInstanceId(instanceId);
				enterPlayer.teleToLocation(playerEnter[Rnd.get(0, playerEnter.length - 1)], true);
			}

			startQuestTimer("stage_1_spawn_boss", 5000, null, player);

			Log.fine(getName() + ":  instance started: " + instanceId + " created by player: " + player.getName());
			return;
		}
	}

	public static void main(String[] args) {
		new Teredor(-1, qn, "instances/DimensionalDoor");
	}
}
