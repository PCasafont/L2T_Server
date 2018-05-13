package instances.RaidBosses.Balok;

import ai.group_template.L2AttackableAIScript;
import l2server.Config;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.ScenePlayerDataTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LasTravel
 * <p>
 * Balok Boss - Normal Mode
 * <p>
 * Source:
 * - http://www.youtube.com/watch?v=H9XZAEj48tc&feature=player_embedded
 */

public class Balok extends L2AttackableAIScript {
	//Quest
	private static final boolean debug = false;
	private static final String qn = "Balok";

	//Ids
	private static final int instanceTemplateId = 167;
	private static final int prisonKey = 10015;
	private static final int crystalPortal = 33523;
	private static final int minionId = 23123;
	private static final int balokId = 29218;
	private static final Location enterCords = new Location(153573, 142867, -12737);
	private static final Skill darknessDrain = SkillTable.getInstance().getInfo(14367, 1);
	private static final Skill invincibilityActivation = SkillTable.getInstance().getInfo(14190, 1);
	private static final int[][] minionSpawns =
			{{154592, 141488, -12738, 26941}, {154759, 142073, -12738, 32333}, {154158, 143112, -12738, 43737}, {152963, 143102, -12738, 53988},
					{152360, 142067, -12740, 0}, {152530, 141457, -12740, 7246}, {153571, 140878, -12738, 16756}, {154174, 141057, -12738, 22165}};
	private static final int[][] prisonsSpawns =
			{{154428, 140551, -12712}, {155061, 141204, -12704}, {155268, 142097, -12712}, {154438, 143581, -12712}, {152695, 143560, -12704},
					{151819, 142063, -12712}, {152055, 141231, -12712}, {153608, 140371, -12712}};

	public Balok(int questId, String name, String descr) {
		super(questId, name, descr);

		addTalkId(crystalPortal);
		addStartNpc(crystalPortal);
		addKillId(minionId);
		addKillId(balokId);
		addAttackId(balokId);
		addSpellFinishedId(balokId);
		addKillId(minionId);
	}

	private class CrystalPrisonWorld extends InstanceWorld {
		private List<Npc> minionList;
		private Npc balok;
		private Npc currentMinion;

		private CrystalPrisonWorld() {
			minionList = new ArrayList<Npc>();
		}
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
			log.warn(getName() + ": onSpellFinished: Unable to get world.");
			return null;
		}

		if (wrld != null && wrld instanceof CrystalPrisonWorld) {
			CrystalPrisonWorld world = (CrystalPrisonWorld) wrld;
			if (skill == darknessDrain) {
				if (!world.currentMinion.isDead()) {
					world.balok.setCurrentHp(world.balok.getCurrentHp() + world.currentMinion.getMaxHp());
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

		if (wrld != null && wrld instanceof CrystalPrisonWorld) {
			CrystalPrisonWorld world = (CrystalPrisonWorld) wrld;
			if (event.equalsIgnoreCase("stage_1_start")) {
				InstanceManager.getInstance().showVidToInstance(106, world.instanceId); //Balok history :D

				startQuestTimer("stage_1_balok_intro", ScenePlayerDataTable.getInstance().getVideoDuration(106) + 5000, null, player);
			} else if (event.equalsIgnoreCase("stage_1_balok_intro")) {
				InstanceManager.getInstance().showVidToInstance(105, world.instanceId); //Balok intro

				startQuestTimer("stage_1_spawn_balok", ScenePlayerDataTable.getInstance().getVideoDuration(105) + 2000, null, player);
			} else if (event.equalsIgnoreCase("stage_1_spawn_balok")) {
				world.balok = addSpawn(balokId, 153573, 142071, -12738, 16565, false, 0, false, world.instanceId);
			} else if (event.equalsIgnoreCase("stage_last_send_minions")) {
				synchronized (world.minionList) {
					Npc minion = world.minionList.get(Rnd.get(world.minionList.size()));
					if (minion != null) {
						minion.setRunning(true);
						((Attackable) minion).setCanReturnToSpawnPoint(false);

						world.currentMinion = minion;

						startQuestTimer("stage_last_minion_walk", 2000, minion, null);
					}
				}
			} else if (event.equalsIgnoreCase("stage_last_minion_walk")) {
				if (npc.getDistanceSq(world.balok) > 113125) {
					npc.getAI()
							.setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
									new L2CharPosition(world.balok.getX() + 100,
											world.balok.getY() + 50,
											world.balok.getZ(),
											world.balok.getHeading()));
					startQuestTimer("stage_last_minion_walk", 2000, npc, null);
				} else {
					Abnormal invul = npc.getFirstEffect(invincibilityActivation);
					if (invul != null) {
						invul.exit();
					}

					world.balok.setTarget(npc);
					world.balok.doCast(darknessDrain);
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
	public final String onAttack(Npc npc, Player attacker, int damage, boolean isPet) {
		if (debug) {
			log.warn(getName() + ": onAttack: " + npc.getName());
		}

		final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpWorld instanceof CrystalPrisonWorld) {
			CrystalPrisonWorld world = (CrystalPrisonWorld) tmpWorld;
			if (npc == world.balok) {
				if (npc.getCurrentHp() < npc.getMaxHp() * 0.85 && world.status == 0) {
					world.status = 1;

					for (int[] a : minionSpawns) {
						Npc minion = addSpawn(minionId, a[0], a[1], a[2], a[3], false, 0, false, world.instanceId);
						world.minionList.add(minion);

						invincibilityActivation.getEffects(minion, minion);
					}
				} else if (npc.getCurrentHp() < npc.getMaxHp() * 0.25 && world.status == 1) {
					world.status = 2;

					invincibilityActivation.getEffects(world.balok, world.balok);

					//Jail random players?
					for (Player instPlayer : world.balok.getKnownList().getKnownPlayers().values()) {
						if (instPlayer == null || Rnd.get(100) > 40) {
							continue;
						}

						int[] randomJail = prisonsSpawns[Rnd.get(prisonsSpawns.length)]; //Random jail

						instPlayer.teleToLocation(randomJail[0], randomJail[1], randomJail[2]);

						InstanceManager.getInstance()
								.sendPacket(world.instanceId,
										new ExShowScreenMessage("$s1, locked away in the prison.".replace("$s1", instPlayer.getName()), 5000));
					}
					startQuestTimer("stage_last_send_minions", 2000, npc, null);
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
		if (tmpworld instanceof CrystalPrisonWorld) {
			CrystalPrisonWorld world = (CrystalPrisonWorld) tmpworld;

			if (npc == world.balok) {
				InstanceManager.getInstance().setInstanceReuse(world.instanceId, instanceTemplateId, false);
				InstanceManager.getInstance().finishInstance(world.instanceId, true);
			} else if (npc == world.currentMinion) {
				synchronized (world.minionList) {
					if (world.minionList.contains(npc)) {
						world.minionList.remove(npc);

						if (world.minionList.size() > 0) {
							startQuestTimer("stage_last_send_minions", 2000, npc, null);
						} else {
							world.balok.getFirstEffect(invincibilityActivation).exit();
						}
					}
				}
			}
		}

		return "";
	}

	@Override
	public final String onTalk(Npc npc, Player player) {
		if (debug) {
			log.warn(getName() + ": onTalk: " + player.getName());
		}

		int npcId = npc.getNpcId();

		if (npcId == crystalPortal) {
			return "EntrancePortal.html";
		}

		return "";
	}

	private final synchronized void enterInstance(Player player) {
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null) {
			if (!(world instanceof CrystalPrisonWorld)) {
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}

			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if (inst != null) {
				if (inst.getInstanceEndTime() > 300600 && world.allowed.contains(player.getObjectId())) {
					player.setInstanceId(world.instanceId);
					player.teleToLocation(153573, 142867, -12737);
				}
			}

			return;
		} else {
			if (!debug && !InstanceManager.getInstance().checkInstanceConditions(player, instanceTemplateId, Config.BALOK_MIN_PLAYERS, 21, 92, 99)) {
				return;
			}

			final int instanceId = InstanceManager.getInstance().createDynamicInstance(qn + ".xml");
			world = new CrystalPrisonWorld();
			world.instanceId = instanceId;
			world.status = 0;

			InstanceManager.getInstance().addWorld(world);

			List<Player> allPlayers = new ArrayList<Player>();
			if (debug) {
				allPlayers.add(player);
			} else {
				allPlayers.addAll(Config.BALOK_MIN_PLAYERS > Config.MAX_MEMBERS_IN_PARTY ? player.getParty().getCommandChannel().getMembers() :
						player.getParty().getPartyMembers());
			}

			for (Player enterPlayer : allPlayers) {
				if (enterPlayer == null) {
					continue;
				}

				enterPlayer.deleteAllItemsById(prisonKey);

				world.allowed.add(enterPlayer.getObjectId());

				enterPlayer.stopAllEffectsExceptThoseThatLastThroughDeath();
				enterPlayer.setInstanceId(instanceId);
				enterPlayer.teleToLocation(enterCords, true);
			}

			startQuestTimer("stage_1_start", 60000, null, player);

			log.debug(getName() + ":  instance started: " + instanceId + " created by player: " + player.getName());

			return;
		}
	}

	public static void main(String[] args) {
		new Balok(-1, qn, "instances/RaidBosses");
	}
}
