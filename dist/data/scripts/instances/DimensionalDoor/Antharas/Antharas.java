package instances.DimensionalDoor.Antharas;

import ai.group_template.L2AttackableAIScript;
import instances.DimensionalDoor.DimensionalDoor;
import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2GuardInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.NpcSay;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LasTravel
 * <p>
 * Antharas - Default Mode
 * <p>
 * Source:
 * - http://www.youtube.com/watch?v=N-e2MPrsjJ4&feature=player_embedded
 * - http://www.youtube.com/watch?v=GJYsQKOH9Jg
 * - http://www.lineage2news.com/2011/08/antaras-raid-tips-and-information.html
 */

public class Antharas extends L2AttackableAIScript {
	//Quests
	private static final boolean debug = false;
	private static final String qn = "Antharas";

	//Id's
	private static final int[] allGuards = {19129, 19128, 19133, 19130, 19136, 19137, 19127};
	private static final int antharasId = 29223;
	private static final int behemothId = 29224;
	private static final int tarrasqueId = 29225;
	private static final int treaterId = 29230;
	private static final int instanceTemplateId = 183;
	private static final int rashId = 19131;
	private static final int ateldId = 19129;
	private static final int fellowId = 19128;
	private static final int commandoId = 19127;
	private static final int dragonBomberId = 29226;

	//Skills
	private static final L2Skill sacrifice = SkillTable.getInstance().getInfo(14477, 1);

	//Cords
	private static final Location enterLoc = new Location(175111, 114924, -7710);

	//Spawns
	private static final int[][] rashArmy = {
			//Polermans
			{19133, 178983, 114767, -7712, 453}, {19133, 178980, 114817, -7712, 453}, {19133, 178978, 114867, -7712, 453},
			{19133, 178976, 114917, -7712, 453}, {19133, 178974, 114966, -7712, 453}, {19133, 178972, 115016, -7712, 453},
			{19133, 178970, 115066, -7712, 453}, {19133, 178967, 115116, -7712, 453}, {19133, 179032, 114769, -7712, 453},
			{19133, 179029, 114819, -7712, 453}, {19133, 179027, 114869, -7712, 453}, {19133, 179025, 114919, -7710, 453},
			{19133, 179023, 114968, -7712, 453}, {19133, 179021, 115018, -7712, 453}, {19133, 179019, 115068, -7712, 453},
			{19133, 179016, 115118, -7712, 453},
			//Archers
			{19136, 178933, 114769, -7712, 319}, {19136, 178932, 114819, -7712, 319}, {19136, 178930, 114869, -7712, 319},
			{19136, 178929, 114919, -7712, 319}, {19136, 178927, 114968, -7712, 319}, {19136, 178926, 115018, -7712, 319},
			{19136, 178924, 115068, -7712, 319}, {19136, 178923, 115118, -7712, 319},
			//Archers
			{19137, 178882, 114767, -7712, 351}, {19137, 178881, 114817, -7712, 351}, {19137, 178879, 114867, -7712, 351},
			{19137, 178877, 114917, -7712, 351}, {19137, 178876, 114966, -7712, 351}, {19137, 178874, 115016, -7712, 351},
			{19137, 178872, 115066, -7712, 351}, {19137, 178871, 115116, -7712, 351}};

	private class AntharasWorld extends InstanceWorld {
		private L2Npc antharas;
		private L2Npc rash;
		private L2Npc fellow;
		private L2Npc ateld;
		private L2Npc commando;
		private ArrayList<L2Npc> minions;
		private ArrayList<L2Npc> army;
		private ArrayList<L2PcInstance> rewardedPlayers;

		public AntharasWorld() {
			minions = new ArrayList<L2Npc>();
			army = new ArrayList<L2Npc>();
			rewardedPlayers = new ArrayList<L2PcInstance>();
		}
	}

	public Antharas(int questId, String name, String descr) {
		super(questId, name, descr);

		addStartNpc(DimensionalDoor.getNpcManagerId());
		addTalkId(DimensionalDoor.getNpcManagerId());

		for (int a : allGuards) {
			addSpawnId(a);
		}

		addAttackId(antharasId);
		addSpellFinishedId(antharasId);
		addStartNpc(rashId);
		addTalkId(rashId);
		addFirstTalkId(rashId);
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player) {
		if (debug) {
			Log.warning(getName() + ": onFirstTalk: " + player.getName());
		}

		InstanceWorld wrld = null;
		if (npc != null) {
			wrld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		} else {
			wrld = InstanceManager.getInstance().getPlayerWorld(player);
		}

		if (wrld != null && wrld instanceof AntharasWorld) {
			AntharasWorld world = (AntharasWorld) wrld;
			if (npc == world.rash) {
				return "Rash.html";
			}
		}

		return super.onFirstTalk(npc, player);
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
			Log.warning(getName() + ": onSpellFinished: Unable to get world.");
			return null;
		}

		if (wrld != null && wrld instanceof AntharasWorld) {
			AntharasWorld world = (AntharasWorld) wrld;
			if (npc.getNpcId() == antharasId) {
				switch (skill.getId()) {
					case 14385: //Front Foot Strike"
						InstanceManager.getInstance()
								.sendPacket(world.instanceId,
										new ExShowScreenMessage(17178303,
												0,
												true,
												5000)); //Behemoth and Tarrasque, Rise with the powers of the ground and help me.
						if (Rnd.get(5) > 3) {
							//Spawn Behemoth and Tarrasque?
							L2Npc behemoth = addSpawn(behemothId,
									world.antharas.getX(),
									world.antharas.getY(),
									world.antharas.getZ(),
									world.antharas.getHeading(),
									true,
									0,
									false,
									world.instanceId);
							behemoth.setIsRunning(true);

							L2Npc tarrasque = addSpawn(tarrasqueId,
									world.antharas.getX(),
									world.antharas.getY(),
									world.antharas.getZ(),
									world.antharas.getHeading(),
									true,
									0,
									false,
									world.instanceId);
							tarrasque.setIsRunning(true);
						}
						break;

					case 14526: //Roar of Death
						InstanceManager.getInstance()
								.sendPacket(world.instanceId, new ExShowScreenMessage(17178300, 0, true, 5000)); //Shoot fire at the imbecile!
						if (!world.minions.isEmpty() && !world.army.isEmpty()) {
							L2Skill suicideSKill = SkillTable.getInstance().getInfo(14390, 1); //Dragon Bomber Explosion
							for (L2Npc _npc : world.army) {
								if (_npc == null || _npc.isDead()) {
									continue;
								}

								_npc.setIsInvul(false);
								_npc.doDie(world.antharas);
							}

							for (L2Npc _npc : world.minions) {
								if (_npc == null || _npc.isDead()) {
									continue;
								}

								_npc.setIsInvul(false);
								_npc.doCast(suicideSKill);
							}

							world.minions.clear();
							world.army.clear();

							//Antharas can now can be attacked
							world.antharas.disableCoreAI(false);
							world.antharas.setIsInvul(false);
							world.antharas.getStatus().startHpMpRegeneration();

							if (world.status == 4) {
								world.status = 5;
							}
						}
						break;

					case 14383: //Antharas Meteor
						L2Object antharasTarget = world.antharas.getTarget();
						if (antharasTarget != null) {
							InstanceManager.getInstance()
									.sendPacket(world.instanceId,
											new ExShowScreenMessage("Wrath of the ground will fall from the sky on $s1!".replace("$s1",
													antharasTarget.getName()), 5000)); //Wrath of the ground will fall from the sky on $s1!
						}
						break;

					case 14380: //Antharas Tail Strike

						if (world.status == 1) {
							world.status = 2;
						}

						world.antharas.setXYZ(world.antharas.getX(), world.antharas.getY(), -7664);

						//Move to inner cave
						startQuestTimer("stage_all_move_antharas", 1000, world.antharas, null);
						startQuestTimer("stage_1_antharas_move_to_zone", 60000, world.antharas, null);
						break;
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

		if (wrld != null && wrld instanceof AntharasWorld) {
			final AntharasWorld world = (AntharasWorld) wrld;
			if (event.equalsIgnoreCase("stage_1_start")) {
				//Spawn Antharas
				world.antharas = addSpawn(antharasId, 177480, 114887, -7710, 32675, false, 0, false, world.instanceId);
				world.antharas.setCurrentHp(world.antharas.getMaxHp() / 2);
				world.antharas.getStatus().stopHpMpRegeneration();
				world.antharas.setIsInvul(true);
				world.antharas.setIsImmobilized(true);
				world.antharas.setIsMortal(false); //Antharas can't die

				//Skills?
				for (L2Skill sk : world.antharas.getAllSkills()) {
					world.antharas.disableSkill(sk, 213000);
				}

				((L2Attackable) world.antharas).setCanReturnToSpawnPoint(false);

				//Spawn Fellow
				world.fellow = addSpawn(fellowId, 175324, 114864, -7710, 64957, false, 0, false, world.instanceId);
				//Spawn Ateld
				world.ateld = addSpawn(ateldId, 175323, 114936, -7710, 64122, false, 0, false, world.instanceId);
				//Comando
				world.commando = addSpawn(commandoId, 175309, 114004, -7710, 1245, false, 0, false, world.instanceId);

				//Delay messages...
				sendMessage(world, world.fellow, 17178318, 15000); //I think we hurt him good. We can defeat him!
				sendMessage(world, world.ateld, 17178319, 20000); //You want more losses?!
				sendMessage(world, world.fellow, 17178320, 25000); //Watch your words!
				sendMessage(world, world.fellow, 17178321, 30000); //Everyone listen!
				sendMessage(world, world.fellow, 17178322, 35000); //This is their limit!
				sendMessage(world, world.fellow, 17178323, 40000); //Do your best for those who died for us!!
				sendMessage(world, world.fellow, 17178324, 45000); //Charge!!!!
				sendMessage(world, world.ateld, 17178325, 50000); //Damn it! Will we end up dead here...
				sendMessage(world, world.commando, 17178326, 52000); //Whoaaaaaa!!!!

				startQuestTimer("stage_1_charge", 45000, world.antharas, null);

				//Store the Army
				for (L2Npc _npc : InstanceManager.getInstance().getInstance(world.instanceId).getNpcs()) {
					if (_npc == null || !(_npc instanceof L2GuardInstance)) {
						continue;
					}

					world.army.add(_npc);
				}
			} else if (event.equalsIgnoreCase("stage_1_charge")) {
				//Send army's to attack Antharas
				for (L2Npc chara : world.army) {
					if (chara == null) {
						continue;
					}

					chara.setIsImmobilized(false);
					chara.setTarget(world.antharas);

					((L2Attackable) chara).addDamageHate(world.antharas, 500, 99999);

					chara.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world.antharas, null);
				}

				world.antharas.setIsImmobilized(false);

				//Force Antharas to attack a target
				ThreadPoolManager.getInstance().scheduleAi(new Runnable() {
					@Override
					public void run() {
						for (L2Npc chara : world.army) {
							if (chara == null) {
								continue;
							}

							if (world.antharas.isInsideRadius(chara, 1200, false, false)) {
								world.antharas.setTarget(chara);
								((L2Attackable) world.antharas).addDamageHate(chara, 500, 99999);
								world.antharas.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, chara, null);

								if (debug) {
									Log.warning(getName() + ": found a target: " + chara.getName());
								}
								break;
							}
						}
					}
				}, 8000);

				sendMessage(world, null, 17178327, 5000); //How stubborn... Squirming 'til the last minute

				startQuestTimer("stage_1_antharas_move_to_cave", 45000, world.antharas, null);
			} else if (event.equalsIgnoreCase("stage_1_antharas_move_to_cave")) {
				InstanceManager.getInstance()
						.sendPacket(world.instanceId,
								new ExShowScreenMessage(17178306,
										0,
										true,
										5000)); //It's stronger than expected! I didn't think I'd be hurt this much...

				world.antharas.disableCoreAI(true);

				((L2Attackable) world.antharas).getAggroList().clear();

				//Move to entrance
				startQuestTimer("stage_all_move_antharas", 1000, world.antharas, null);

				//Stop the army, looks bad if follow the boss :S
				for (L2Npc _npc : world.army) {
					if (_npc == null || _npc.isDead()) {
						continue;
					}

					_npc.setIsImmobilized(true);
				}

				//TODO NOT SURE
				sendMessage(world, world.fellow, 17178328, 2000); //I can't die like this! I will get backup from the Kingdom!

				startQuestTimer("stage_1_antharas_move_entrance_arrived", 20000, world.antharas, null);
				startQuestTimer("stage_1_antharas_minions", 35000, world.antharas, null);
			} else if (event.equalsIgnoreCase("stage_1_antharas_move_entrance_arrived")) {
				world.status = 1;

				world.antharas.stopMove(null);

				L2Skill _skill = SkillTable.getInstance().getInfo(14380, 1); //Antharas Tail Strike
				world.antharas.doCast(_skill);
			} else if (event.equalsIgnoreCase("stage_1_antharas_minions")) {
				InstanceManager.getInstance()
						.sendPacket(world.instanceId,
								new ExShowScreenMessage(17178305, 0, true, 5000)); //Children. With noble your sacrifice, give them pain!

				//Army back..
				for (L2Npc _npc : world.army) {
					if (_npc == null || _npc.isDead()) {
						continue;
					}

					_npc.setIsImmobilized(false);
				}

				L2ZoneType zone = ZoneManager.getInstance().getZoneById(12001);
				for (int a = 0; a < 150; a++) {
					int[] _point = zone.getZone().getRandomPoint();
					L2Npc minion = addSpawn(dragonBomberId, _point[0], _point[1], _point[2], 64122, true, 0, false, world.instanceId);
					minion.setIsRunning(true);
					world.minions.add(minion);
				}

				//TODO NOT SURE
				sendMessage(world, world.fellow, 17178329, 2000); //Mercenaries! I think they're here to support us!
				//TODO NOT SURE
				sendMessage(world, world.fellow, 17178330, 7000); //Mercenaries!! Everyone charge! We can't let them win it for us!
			} else if (event.equalsIgnoreCase("stage_1_antharas_move_to_zone")) {
				world.status = 3;

				InstanceManager.getInstance()
						.sendPacket(world.instanceId, new ExShowScreenMessage(17178304, 0, true, 5000)); //Not enough... I will have to go myself.

				//Move out to out of the cave
				startQuestTimer("stage_all_move_antharas", 1000, world.antharas, null);
				startQuestTimer("stage_1_antharas_move_out", 45000, world.antharas, null);
			} else if (event.equalsIgnoreCase("stage_1_antharas_move_out")) {
				world.status = 4;

				world.antharas.stopMove(null);

				L2Skill skill = SkillTable.getInstance().getInfo(14526, 1); //Roar of Death
				world.antharas.doCast(skill);
			} else if (event.equalsIgnoreCase("stage_all_move_antharas")) {
				if (world.status != 1 && world.status <= 3) {
					world.antharas.setIsRunning(true);
					switch (world.status) {
						case 0:
							world.antharas.getAI()
									.setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
											new L2CharPosition(180242, 114877, -7710, 130)); //Move to entrance
							break;

						case 2:
							world.antharas.getAI()
									.setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
											new L2CharPosition(185009, 114777, -8222, 65457)); //Move to inner cave
							break;

						case 3:
							world.antharas.getAI()
									.setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
											new L2CharPosition(179595, 114762, -7710, 32854)); //Move out to out of the cave
							break;
					}
					startQuestTimer("stage_all_move_antharas", 3000, world.antharas, null);
				}
			} else if (event.equalsIgnoreCase("tryGetReward")) {
				synchronized (world.rewardedPlayers) {
					if (InstanceManager.getInstance().canGetUniqueReward(player, world.rewardedPlayers)) {
						world.rewardedPlayers.add(player);

						player.addItem(qn, DimensionalDoor.getDimensionalDoorRewardId(), 50, player, true);
					} else {
						player.sendMessage("Nice attempt, but you already got a reward!");
					}
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
			Log.warning(getName() + ": onAttack: " + npc.getName());
		}

		final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpWorld instanceof AntharasWorld) {
			final AntharasWorld world = (AntharasWorld) tmpWorld;
			if (npc.getNpcId() == antharasId) {
				if (world.status < 5) {
					L2Attackable antharas = (L2Attackable) npc;
					antharas.clearAggroList();
				} else if (world.status == 5 && npc.getCurrentHp() < npc.getMaxHp() * 0.25) //25%
				{
					world.status = 6;

					InstanceManager.getInstance()
							.sendPacket(world.instanceId,
									new ExShowScreenMessage(17178307, 0, true, 5000)); //Children. Heal me with your noble sacrifice.

					//Spawn treater's
					for (int a = 0; a < 7; a++) {
						L2Npc treater = addSpawn(treaterId,
								world.antharas.getX(),
								world.antharas.getY(),
								world.antharas.getZ(),
								world.antharas.getHeading(),
								true,
								0,
								false,
								world.instanceId);
						treater.setIsRunning(true);
						treater.setTarget(world.antharas);
						treater.doCast(sacrifice);
					}

					sendMessage(world, null, 17178308, 10000); //Your sacrifices will become a new rescue...
				}

				if (world.status == 6 && npc.getCurrentHp() < npc.getMaxHp() * 0.20) //20%
				{
					world.status = 7;

					InstanceManager.getInstance()
							.sendPacket(world.instanceId, new ExShowScreenMessage(17178309, 0, true, 5000)); //Children. Give everything you've got!

					//Spawn treater's
					for (int a = 0; a < 7; a++) {
						L2Npc treater = addSpawn(treaterId,
								world.antharas.getX(),
								world.antharas.getY(),
								world.antharas.getZ(),
								world.antharas.getHeading(),
								true,
								0,
								false,
								world.instanceId);
						treater.setIsRunning(true);
						treater.setTarget(world.antharas);
						treater.doCast(sacrifice);
					}

					sendMessage(world, null, 17178310, 10000); //Lowly beings! Can you handle my wrath!
					sendMessage(world, null, 17178311, 15000); //This is the end of your desperate measures!
				}

				if (world.status == 7 && npc.getCurrentHp() < npc.getMaxHp() * 0.05) //5%)
				{
					world.status = 8;

					InstanceManager.getInstance()
							.sendPacket(world.instanceId, new ExShowScreenMessage(17178331, 0, true, 5000)); //Be happy that I'm backing off today.
				}

				if (world.status == 8 && npc.getCurrentHp() < npc.getMaxHp() * 0.03) //3%
				{
					world.status = 9;

					InstanceManager.getInstance()
							.sendPacket(world.instanceId,
									new ExShowScreenMessage(17178332, 0, true, 5000)); //Imbeciles...you'll disappear on the day of destruction...

					world.antharas.deleteMe();

					//Spawn Rash Army
					world.rash = addSpawn(rashId, 179059, 114955, -7712, 64337, false, 0, false, world.instanceId);

					for (int[] a : rashArmy) {
						addSpawn(a[0], a[1], a[2], a[3], a[4], false, 0, false, world.instanceId);
					}

					sendMessage(world, world.rash, 17178333, 5000); //Ah.. Did the backup get wiped out... Looks like we're late.
					sendMessage(world, world.rash, 17178334, 10000); //You guys are the mercenaries.
					sendMessage(world, world.rash, 17178335, 15000); //He's quiet again. Thanks.
					sendMessage(world, world.rash, 17178336, 20000); //This.. We brought this to support the backup, but we could give these to you.
					sendMessage(world,
							world.rash,
							17178337,
							25000); //Courageous ones who supported Antharas force, come and take the Kingdom's reward.
					sendMessage(world, world.rash, 17178338, 30000); //Are there those who didn't receive the rewards yet? Come and get it from me.

					//Finish world
					InstanceManager.getInstance().setInstanceReuse(world.instanceId, instanceTemplateId, 6, 30);
					InstanceManager.getInstance().finishInstance(world.instanceId, false);
				}
			}
		}

		return super.onAttack(npc, attacker, damage, isPet);
	}

	private void sendMessage(final AntharasWorld world, final L2Npc npc, final int msgId, int delay) {
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
			@Override
			public void run() {
				if (npc != null) {
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), msgId));
				} else {
					InstanceManager.getInstance().sendPacket(world.instanceId, new ExShowScreenMessage(msgId, 0, true, 5000));
				}
			}
		}, delay);
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player) {
		if (debug) {
			Log.warning(getName() + ": onTalk: " + player.getName());
		}

		int npcId = npc.getNpcId();
		if (npcId == DimensionalDoor.getNpcManagerId()) {
			return qn + ".html";
		}

		return super.onTalk(npc, player);
	}

	@Override
	public String onSpawn(L2Npc npc) {
		if (debug) {
			Log.warning(getName() + ": onSpawn: " + npc.getName());
		}

		if (npc instanceof L2GuardInstance) {
			npc.setIsImmobilized(true);
			npc.setIsInvul(true);
			npc.setIsRunning(true);
		}

		return super.onSpawn(npc);
	}

	private final synchronized void enterInstance(L2PcInstance player) {
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null) {
			if (!(world instanceof AntharasWorld)) {
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}

			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if (inst != null) {
				if (inst.getInstanceEndTime() > 300600 && world.allowed.contains(player.getObjectId())) {
					player.setInstanceId(world.instanceId);
					player.teleToLocation(175111, 114924, -7710);
				}
			}

			return;
		} else {
			if (!debug && !InstanceManager.getInstance().checkInstanceConditions(player, instanceTemplateId, 7, 7, 92, Config.MAX_LEVEL)) {
				return;
			}

			final int instanceId = InstanceManager.getInstance().createDynamicInstance(qn + ".xml");

			world = new AntharasWorld();
			world.instanceId = instanceId;
			world.templateId = instanceTemplateId;

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
				enterPlayer.teleToLocation(enterLoc, true);
			}

			startQuestTimer("stage_1_start", 2000, null, player);

			Log.fine(getName() + ":  instance started: " + instanceId + " created by player: " + player.getName());
			return;
		}
	}

	public static void main(String[] args) {
		new Antharas(-1, qn, "instances/DimensionalDoor");
	}
}
