package events.Halloween;

import l2server.gameserver.Announcements;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.GuardInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.olympiad.OlympiadManager;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.templates.skills.EffectType;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.List;

/**
 * Halloween global thematic event.
 *
 * @author LasTravel
 * <p>
 * Sometimes, my imagination it's unstoppable, lol.
 */

public class Halloween extends Quest {
	private static final boolean Debug = false;
	private static final String qn = "Halloween";
	
	//Ids
	private static final int instanceTemplateId = 504;
	private static final int doorGuildCoin = 37045;
	private static final int pumpkinGhostId = 13135;
	private static final int mountedRaidId = 80331;
	private static final int finalRaidId = 80330;
	private static final int passiveSkillId = 90003;
	private static final int[] minionIds = {80318, 80319, 80320, 80356, 80322, 80323, 80324, 80355};
	
	//Helpers
	private static final int kegorId = 80332;
	private static final int jiniaId = 80333;
	
	//Vars
	private static Npc boss = null;
	private static Npc dummy = null;
	
	private static Npc kegor = null;
	private static Npc jinia = null;
	
	private static int eventState = 0;
	private static int instanceId = 0;
	private static int round = 0;
	
	//Others
	private static List<Npc> allMinions = new ArrayList<Npc>();
	private static List<Npc> townBuffers = new ArrayList<Npc>();
	private static final Skill blessingOfHalloween = SkillTable.getInstance().getInfo(21129, 1);
	
	public Halloween(int questId, String name, String descr) {
		super(questId, name, descr);
		
		addTalkId(pumpkinGhostId);
		addFirstTalkId(pumpkinGhostId);
		addStartNpc(pumpkinGhostId);
		addAttackId(mountedRaidId);
		addAttackId(mountedRaidId);
		addKillId(finalRaidId);
		
		for (int a : minionIds) {
			addKillId(a);
		}
		
		//Start Npc
		addSpawn(pumpkinGhostId, 83798, 148625, -3391, 31701, false, 0);
		
		//Just Dummy
		addSpawn(80347, 83672, 148860, -3409, 32219, false, 0);
		addSpawn(80347, 83681, 147989, -3409, 33093, false, 0);
		addSpawn(80347, 83684, 149253, -3409, 32767, false, 0);
		addSpawn(80348, 83850, 148562, -3397, 32381, false, 0);
		addSpawn(80348, 83853, 148683, -3397, 32420, false, 0);
		
		townBuffers.add(addSpawn(80349, 83273, 148627, -3409, 32073, false, 0));
		townBuffers.add(addSpawn(80349, 83259, 148168, -3409, 32233, false, 0));
		townBuffers.add(addSpawn(80349, 83275, 149078, -3409, 32010, false, 0));
		
		startQuestTimer("stage_all_buff_players", 30 * 60000, null, null, true);
	}
	
	@Override
	public final String onFirstTalk(Npc npc, Player player) {
		if (Debug) {
			log.warn(getName() + ": onFirstTalk: " + player.getName());
		}
		
		if (npc.getNpcId() == pumpkinGhostId) {
			if (player.isGM()) {
				return "gmEventPanel.html";
			}
			
			return qn + ".html";
		}
		
		return super.onFirstTalk(npc, player);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player) {
		if (Debug) {
			log.warn(getName() + ": onAdvEvent: " + event);
		}
		
		if (event.equalsIgnoreCase("stage_all_buff_players")) {
			for (Npc buffers : townBuffers) {
				if (buffers == null) {
					continue;
				}
				
				for (Player players : buffers.getKnownList().getKnownPlayersInRadius(15000)) {
					if (players == null) {
						continue;
					}
					
					if (players.getPrivateStoreType() != 0 || players.getClient().isDetached() ||
							players.getFirstEffect(blessingOfHalloween) != null) {
						continue;
					}
					
					blessingOfHalloween.getEffects(players, players);
				}
			}
		} else if (event.equalsIgnoreCase("launch_event")) {
			if (player.isGM() && eventState == 0) {
				notifyEvent("stage_0_prepare_event", npc, player);
			}
		} else if (event.equalsIgnoreCase("stop_event")) {
			if (eventState != 0) {
				cancelQuestTimers("stop_event");
				boss = null;
				jinia = null;
				kegor = null;
				eventState = 0;
				instanceId = 0;
				allMinions.clear();
				round = 0;
				dummy = null;
				InstanceManager.getInstance().destroyInstance(instanceId);
				
				if (player != null) {
					player.sendMessage("Vars restarted!");
				}
			}
		} else if (event.equalsIgnoreCase("stage_0_prepare_event")) {
			instanceId = InstanceManager.getInstance().createDynamicInstance(qn + ".xml");
			
			InstanceWorld world = null;
			world = new InstanceWorld();
			world.instanceId = instanceId;
			world.templateId = instanceTemplateId;
			
			InstanceManager.getInstance().addWorld(world);
			InstanceManager.getInstance().getInstance(instanceId).setPvPInstance(false);
			InstanceManager.getInstance().getInstance(instanceId).setPeaceInstance(true);
			
			dummy = addSpawn(80346, 153570, 142072, -12741, 39237, false, 0, false, instanceId);
			
			Announcements.getInstance().announceToAll("ssh");
			Announcements.getInstance().announceToAll("The instance will start on 10 minutes!");
			Announcements.getInstance().announceToAll("When started, none will be able to enter!");
			
			eventState = 1;
			
			startQuestTimer("stop_event", 130 * 60000, null, null);
			startQuestTimer("stage_1_start_event", Debug ? 60000 : 1 * 60000, null, null);
		} else if (event.equalsIgnoreCase("stage_1_start_event")) {
			eventState = 2;
			
			InstanceManager.getInstance()
					.sendPacket(instanceId, new ExShowScreenMessage(1, 0, 2, 0, 0, 0, 0, true, 5 * 1000, 0, "You won't leave this place alive!"));
			InstanceManager.getInstance().sendPacket(instanceId, new CreatureSay(0, 10, "", "The event process will start in one minute!"));
			InstanceManager.getInstance()
					.sendPacket(instanceId, new CreatureSay(0, 10, "", "If you die and you press to village you wont be able to come back!"));
			InstanceManager.getInstance().sendPacket(instanceId, new CreatureSay(0, 10, "", "Prepare to the hell!"));
			
			startQuestTimer("stage_all_spawn_round", 60000, null, null);
		} else if (event.equalsIgnoreCase("stage_all_spawn_round")) {
			round++;
			
			InstanceManager.getInstance().sendPacket(instanceId, new Earthquake(153581, 142081, -12741, 8, 10));
			InstanceManager.getInstance().sendPacket(instanceId, new ExShowScreenMessage(1, 0, 2, 0, 0, 0, 0, true, 5 * 1000, 0, "Round: " + round));
			
			int minionId = minionIds[Rnd.get(minionIds.length)];
			
			Skill passiveSkill = SkillTable.getInstance().getInfo(passiveSkillId, round);
			if (round < 20) {
				for (int i = 0; i < 60; i++) {
					int x = (int) (600 * Math.cos(i * 0.618));
					int y = (int) (600 * Math.sin(i * 0.618));
					
					Npc minion = addSpawn(minionId, 153569 + x, 142075 + y, -12742 + 20, -1, false, 0, false, instanceId);
					minion.addSkill(passiveSkill);
					minion.setCurrentHpMp(minion.getMaxHp(), minion.getMaxMp());
					
					allMinions.add(minion);
				}
			} else if (round == 20) {
				//BossTime
				boss = addSpawn(mountedRaidId, 153572, 142074, -12741, 52867, false, 0, false, instanceId);
				
				InstanceManager.getInstance()
						.sendPacket(instanceId, new CreatureSay(boss.getObjectId(), 1, boss.getName(), "It's the time to end that shit!"));
			}
		}
		//Help process
		else if (event.equalsIgnoreCase("stage_all_start_help_process")) {
			//Special Effects
			InstanceManager.getInstance().sendPacket(instanceId, new Earthquake(153581, 142081, -12741, 8, 10));
			
			//Paralyze the whole instance
			for (Player players : World.getInstance().getAllPlayersArray()) {
				if (players == null || players.getInstanceId() != instanceId) {
					continue;
				}
				
				players.stopEffects(EffectType.PHOENIX_BLESSING);
				players.reduceCurrentHp(players.getMaxHp(), boss, null);
				players.setIsImmobilized(true);
			}
			
			boss.setIsImmobilized(true);
			
			dummy.broadcastPacket(new SpecialCamera(dummy.getObjectId(), 700, -45, 160, 10000, 15200, 0, 0, 1, 0));
			
			//Chats
			InstanceManager.getInstance().sendPacket(instanceId, new CreatureSay(boss.getObjectId(), 1, boss.getName(), "Poor heros..!"));
			
			startQuestTimer("stage_all_help_process_gm_entrance_effect", 5000, null, null);
		} else if (event.equalsIgnoreCase("stage_all_help_process_gm_entrance_effect")) {
			//Door Effect 1
			dummy.broadcastPacket(new MagicSkillUse(dummy, dummy, 6798, 1, 500, 500, 500, instanceId, 0));//6783
			
			InstanceManager.getInstance()
					.sendDelayedPacketToInstance(instanceId,
							3,
							new CreatureSay(boss.getObjectId(), 1, boss.getName(), "What the hell is that sh1t!"));
			
			startQuestTimer("stage_all_help_process_gm_appear", 8000, null, null);
		} else if (event.equalsIgnoreCase("stage_all_help_process_gm_appear")) {
			//Door Effect 2
			dummy.broadcastPacket(new MagicSkillUse(dummy, dummy, 6799, 1, 500, 500, 500, 0, 0));
			
			//Supports
			kegor = addSpawn(kegorId, 153511, 142101, -12741, 62606, false, 0, false, instanceId);
			((GuardInstance) kegor).setCanReturnToSpawnPoint(false);
			kegor.setIsInvul(true);
			kegor.setIsMortal(false);
			kegor.setIsImmobilized(true);
			
			jinia = addSpawn(jiniaId, 153610, 142129, -12741, 40159, false, 0, false, instanceId);
			((GuardInstance) jinia).setCanReturnToSpawnPoint(false);
			jinia.setIsInvul(true);
			jinia.setIsMortal(false);
			jinia.setIsImmobilized(true);
			
			kegor.broadcastPacket(new MagicSkillUse(kegor, kegor, 6463, 1, 500, 500, 500, 0, 0));
			
			jinia.broadcastPacket(new MagicSkillUse(jinia, jinia, 6463, 1, 500, 500, 500, 0, 0));
			
			InstanceManager.getInstance()
					.sendDelayedPacketToInstance(instanceId, 5, new CreatureSay(kegor.getObjectId(), 1, kegor.getName(), "Hi b1tches!"));
			InstanceManager.getInstance()
					.sendDelayedPacketToInstance(instanceId,
							9,
							new CreatureSay(jinia.getObjectId(), 1, jinia.getName(), "Seems that these noobs need some help, don't you think?"));
			InstanceManager.getInstance()
					.sendDelayedPacketToInstance(instanceId,
							13,
							new CreatureSay(boss.getObjectId(), 1, boss.getName(), "Fuck it!! I'll crush your fucking heads with my sword!"));
			InstanceManager.getInstance()
					.sendDelayedPacketToInstance(instanceId,
							17,
							new CreatureSay(jinia.getObjectId(), 1, jinia.getName(), "Mad Bambi! We will put your sword on your ass!"));
			InstanceManager.getInstance()
					.sendDelayedPacketToInstance(instanceId,
							21,
							new CreatureSay(kegor.getObjectId(), 1, kegor.getName(), "There are a lot of dead players around!"));
			InstanceManager.getInstance()
					.sendDelayedPacketToInstance(instanceId, 25, new CreatureSay(jinia.getObjectId(), 1, jinia.getName(), "Leet me do the work!"));
			
			startQuestTimer("stage_all_help_process_players_back", 27000, null, null);
		} else if (event.equalsIgnoreCase("stage_all_help_process_players_back")) {
			jinia.broadcastPacket(new MagicSkillUse(jinia, jinia, 6176, 1, 500, 500, 500, 0, 0));
			
			startQuestTimer("stage_all_help_process_players_res", 7000, null, null);
		} else if (event.equalsIgnoreCase("stage_all_help_process_players_res")) {
			for (Player players : World.getInstance().getAllPlayersArray()) {
				if (players == null || players.getInstanceId() != instanceId) {
					continue;
				}
				
				players.doRevive();
				players.setIsImmobilized(false);
			}
			
			InstanceManager.getInstance()
					.sendPacket(instanceId, new CreatureSay(kegor.getObjectId(), 1, kegor.getName(), "Let's own this chicken guys!"));
			InstanceManager.getInstance()
					.sendDelayedPacketToInstance(instanceId, 2, new CreatureSay(jinia.getObjectId(), 1, jinia.getName(), "You lead!"));
			
			startQuestTimer("stage_all_help_process_attack_boss", 2000, null, null);
		} else if (event.equalsIgnoreCase("stage_all_help_process_attack_boss")) {
			boss.setIsImmobilized(false);
			
			kegor.setTarget(boss);
			
			((Attackable) kegor).addDamageHate(boss, 500, 99999);
			kegor.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, boss, null);
			kegor.setIsRunning(true);
			
			jinia.setTarget(boss);
			
			((Attackable) jinia).addDamageHate(boss, 500, 99999);
			jinia.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, boss, null);
			jinia.setIsRunning(true);
			jinia.setIsImmobilized(false);
			
			kegor.setIsImmobilized(false);
		} else if (event.equalsIgnoreCase("stage_last_boss_change")) {
			int x = boss.getX();
			int y = boss.getY();
			int z = boss.getZ();
			
			boss.decayMe();
			boss = addSpawn(finalRaidId, x, y, z, 31701, false, 0, false, instanceId);
			boss.addSkill(SkillTable.getInstance().getInfo(passiveSkillId, 20));
			boss.setCurrentHpMp(boss.getMaxHp(), boss.getMaxMp());
			
			startQuestTimer("stage_all_start_help_process", 1, null, null);
		} else if (event.equalsIgnoreCase("enter_to_instance")) {
			if (!checkConditions(player)) {
				return "";
			}
			
			player.setInstanceId(instanceId);
			player.teleToLocation(153569, 142075, -12732, true);
			player.stopAllEffectsExceptThoseThatLastThroughDeath();
		} else if (event.equalsIgnoreCase("leave_instance")) {
			player.setInstanceId(0);
			player.teleToLocation(82731, 148655, -3464, true);
		}
		
		return "";
	}
	
	@Override
	public final String onAttack(Npc npc, Player attacker, int damage, boolean isPet) {
		if (Debug) {
			log.warn(getName() + ": onAttack: " + npc.getName());
		}
		
		if (npc.getNpcId() == mountedRaidId) {
			if (npc.getCurrentHp() < npc.getMaxHp() * 0.50) //50%
			{
				if (!npc.isInvul() && eventState == 2) {
					eventState = 3;
					
					npc.setIsInvul(true);
					npc.setIsImmobilized(true);
					
					//Do cast some skill
					boss.broadcastPacket(new MagicSkillUse(boss, boss, 6796, 1, 500, 500, 500, 0, 0));
					
					startQuestTimer("stage_last_boss_change", 2000, null, null);
				}
			}
		}
		
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet) {
		if (Debug) {
			log.warn(getName() + ": onKill: " + npc.getName());
		}
		
		if (allMinions.contains(npc)) {
			allMinions.remove(npc);
			if (allMinions.isEmpty()) {
				InstanceManager.getInstance()
						.sendPacket(instanceId,
								new ExShowScreenMessage(1, 0, 2, 0, 0, 0, 0, true, 5 * 1000, 0, "Next round will start in 15 seconds!"));
				
				startQuestTimer("stage_all_spawn_round", 15000, null, null);
			}
			
			//Drop
			int count = Rnd.get(10, 20);
			if (round > 15) {
				count *= 2;
			} else {
				count += round;
			}
			
			Item item = null;
			for (int a = 1; a <= count; a++) {
				item = ItemTable.getInstance().createItem(qn, doorGuildCoin, 1, null, npc);
				item.setInstanceId(instanceId);
				item.dropMe(npc, npc.getX() + Rnd.get(100), npc.getY() + Rnd.get(100), npc.getZ() + 50);
			}
		}
		
		if (npc.getNpcId() == finalRaidId) {
			cancelQuestTimers("stop_event");
			
			Item item = null;
			for (int a = 1; a <= 2000; a++) {
				item = ItemTable.getInstance().createItem(qn, doorGuildCoin, 1, null, npc);
				item.dropMe(npc, npc.getX() + Rnd.get(500), npc.getY() + Rnd.get(500), npc.getZ() + 50);
			}
			
			InstanceManager.getInstance().finishInstance(instanceId, true);
			
			startQuestTimer("stop_event", 5 * 60000, null, null);
		}
		
		return "";
	}
	
	private static boolean checkConditions(Player player) {
		if (player == null) {
			return false;
		}
		
		if (OlympiadManager.getInstance().isRegisteredInComp(player) || player.isInOlympiadMode() || player.isCursedWeaponEquipped() ||
				player.getEvent() != null) {
			player.sendMessage("You can't enter while in other event!");
			return false;
		}
		
		if (eventState == 0) {
			player.sendMessage("The event don't started yet!");
			return false;
		} else if (eventState > 1) {
			player.sendMessage("You can't enter while the event is already started!");
			return false;
		}
		
		//Ip checks
		for (Player players : World.getInstance().getAllPlayersArray()) {
			if (players == null || players.getClient() != null && players.getClient().isDetached()) {
				continue;
			}
			
			if (players.getInstanceId() == instanceId) {
				if (players.getExternalIP().equalsIgnoreCase(player.getExternalIP())) {
					if (players.getInternalIP().equalsIgnoreCase(player.getInternalIP())) {
						player.sendMessage("Dual box is not allowed here!");
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public static void main(String[] args) {
		new Halloween(-1, qn, "events");
	}
}
