package events.Christmas;

import l2server.gameserver.Announcements;
import l2server.gameserver.GmListTable;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.network.NpcStringId;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.NpcSay;
import l2server.gameserver.network.serverpackets.PlaySound;
import l2server.gameserver.network.serverpackets.SocialAction;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LasTravel
 * <p>
 * Little custom Christmas event
 */

public class Christmas extends Quest {
	//Config
	private static final boolean onGoing = false;
	private static final boolean exChangeOnly = true;
	private static final int startInvasionEach = 3; //Hours
	private static final int timeToEndInvasion = 15; //Minutes
	private static final int rewardRandomPlayerEach = 2; //Hours
	private static final int santaTalksEach = 3; //Hours
	private static final int santaId = 33885;
	private static final int secondSantaId = 104;
	private static final int[] invaderIds = {80198, 80199};

	//Vars
	private static Long nextInvasion;
	private static Long nextSantaReward;
	private static String lastSantaRewardedName;
	private static Npc santa;
	private static boolean isUnderInvasion = false;
	private Map<Integer, invaderInfo> attackInfo = new HashMap<Integer, invaderInfo>();
	private ArrayList<Creature> invaders = new ArrayList<Creature>();
	private ArrayList<String> rewardedPlayers = new ArrayList<String>(); //IP based

	private static final int[][] randomRewards = {
			//Item Id, ammount
			{36513, 1000}, //Elcyum Powder
			{36514, 100}, //Elcyum Crystal
			{36515, 10} //Elcyum
	};

	public Christmas(int id, String name, String descr) {
		super(id, name, descr);

		if (!onGoing) {
			return;
		}

		//Spawn Santa's
		addSpawn(santaId, 83453, 148642, -3405, 32659, false, 0);
		addSpawn(santaId, 147709, -55308, -2735, 49609, false, 0);
		addSpawn(santaId, 18456, 145205, -3103, 8291, false, 0);
		addSpawn(santaId, -12661, 122568, -3121, 15716, false, 0);
		addSpawn(santaId, 87360, -143376, -1293, 15917, false, 0);
		addSpawn(santaId, 117066, 77063, -2694, 38717, false, 0);
		addSpawn(santaId, 147463, 25632, -2013, 15704, false, 0);
		addSpawn(santaId, 43903, -47733, -797, 49285, false, 0);
		addSpawn(santaId, 82916, 53098, -1496, 16552, false, 0);
		addSpawn(santaId, -80920, 149744, -3044, 16304, false, 0);
		addSpawn(santaId, 111380, 218701, -3466, 17021, false, 0);
		addSpawn(santaId, -59011, -56895, -2042, 31470, false, 0);
		addSpawn(santaId, -78307, 247921, -3303, 24266, false, 0);

		if (!exChangeOnly) {
			//Small Tree
			addSpawn(13006, 83276, 149323, -3409, 0, false, 0);
			addSpawn(13006, 83680, 149248, -3409, 31644, false, 0);
			addSpawn(13006, 83680, 147988, -3409, 33115, false, 0);
			addSpawn(13006, 83271, 147908, -3409, 14420, false, 0);
			addSpawn(13006, 83258, 148361, -3409, 289, false, 0);
			addSpawn(13006, 83249, 148799, -3409, 1722, false, 0);
			addSpawn(13006, 83076, 149323, -3473, 508, false, 0);
			addSpawn(13006, 83066, 148841, -3473, 32767, false, 0);
			addSpawn(13006, 83067, 148396, -3473, 31613, false, 0);
			addSpawn(13006, 83066, 147911, -3473, 32408, false, 0);
			addSpawn(13006, -59316, -56895, -2042, 33975, false, 0);
			addSpawn(13006, -59428, -56607, -2042, 44389, false, 0);
			addSpawn(13006, -59721, -56487, -2042, 48539, false, 0);
			addSpawn(13006, -60016, -56599, -2042, 57046, false, 0);
			addSpawn(13006, -60127, -56895, -2042, 386, false, 0);
			addSpawn(13006, -60010, -57188, -2042, 7845, false, 0);
			addSpawn(13006, -59719, -57301, -2042, 16338, false, 0);
			addSpawn(13006, -59426, -57187, -2042, 23351, false, 0);

			//Big Tree
			addSpawn(34009, 82595, 148617, -3476, 63602, false, 0);
		}

		addStartNpc(santaId);
		addTalkId(santaId);
		addFirstTalkId(santaId);

		for (int mob : invaderIds) {
			addAttackId(mob);
			addKillId(mob);
		}

		addFirstTalkId(secondSantaId);

		startQuestTimer("santas_talks", santaTalksEach * 3600000, null, null, true);

		if (!exChangeOnly) {
			nextInvasion = System.currentTimeMillis() + startInvasionEach * 3600000;
			nextSantaReward = System.currentTimeMillis() + rewardRandomPlayerEach * 3600000;

			startQuestTimer("start_invasion", startInvasionEach * 3600000, null, null);
			startQuestTimer("santas_random_player_reward", rewardRandomPlayerEach * 3600000, null, null);
		}
	}

	private class invaderInfo {
		private Long attackedTime;
		private int playerId;
		private String externalIP;
		private String internalIP;

		private invaderInfo(int playerId, String externalIP, String internalIP) {
			this.playerId = playerId;
			this.externalIP = externalIP;
			this.internalIP = internalIP;
			setAttackedTime();
		}

		private long getAttackedTime() {
			return attackedTime;
		}

		private void setAttackedTime() {
			attackedTime = System.currentTimeMillis();
		}

		private int getPlayerId() {
			return playerId;
		}

		private String getExternalIP() {
			return externalIP;
		}

		private String getInternalIP() {
			return internalIP;
		}

		private void updateInfo(int playerId, String externalIP, String internalIP) {
			this.playerId = playerId;
			this.externalIP = externalIP;
			this.internalIP = internalIP;
			setAttackedTime();
		}
	}

	@Override
	public String onAttack(Npc npc, Player player, int damage, boolean isPet, Skill skill) {
		if (!isUnderInvasion) {
			player.doDie(npc);
			return "";
		}

		synchronized (attackInfo) {
			invaderInfo info = attackInfo.get(npc.getObjectId()); //Get the attack info from this npc

			int sameIPs = 0;
			int underAttack = 0;

			for (Map.Entry<Integer, invaderInfo> entry : attackInfo.entrySet()) {
				if (entry == null) {
					continue;
				}

				invaderInfo i = entry.getValue();
				if (i == null) {
					continue;
				}

				if (System.currentTimeMillis() < i.getAttackedTime() + 5000) {
					if (i.getPlayerId() == player.getObjectId()) {
						underAttack++;
					}
					if (i.getExternalIP().equalsIgnoreCase(player.getExternalIP()) && i.getInternalIP().equalsIgnoreCase(player.getInternalIP())) {
						sameIPs++;
					}
					if (underAttack > 1 || sameIPs > 1) {
						player.doDie(npc);
						if (underAttack > 1) {
							npc.broadcastPacket(new NpcSay(npc.getObjectId(),
									0,
									npc.getTemplate().TemplateId,
									player.getName() + " you cant attack more than one mob at same time!"));
						}
						if (sameIPs > 1) {
							npc.broadcastPacket(new NpcSay(npc.getObjectId(),
									0,
									npc.getTemplate().TemplateId,
									player.getName() + " dualbox is not allowed here!"));
						}
						return "";
					}
				}
			}

			if (info == null) //Don't exist any info from this npc
			{
				//Add the correct info
				info = new invaderInfo(player.getObjectId(), player.getExternalIP(), player.getInternalIP());
				//Insert to the map
				attackInfo.put(npc.getObjectId(), info);
			} else {
				//Already exists information for this NPC
				//Check if the attacker is the same as the stored
				if (info.getPlayerId() != player.getObjectId()) {
					//The attacker is not same
					//If the last attacked stored info +10 seconds is bigger than the current time, this mob is currently attacked by someone
					if (info.getAttackedTime() + 5000 > System.currentTimeMillis()) {
						player.doDie(npc);
						npc.broadcastPacket(new NpcSay(npc.getObjectId(),
								0,
								npc.getTemplate().TemplateId,
								player.getName() + " don't attack mobs from other players!"));
						return "";
					} else {
						//Add new information, none is currently attacking this NPC
						info.updateInfo(player.getObjectId(), player.getExternalIP(), player.getInternalIP());
					}
				} else {
					//player id is the same, update the attack time
					info.setAttackedTime();
				}
			}
		}
		return super.onAttack(npc, player, damage, isPet, skill);
	}

	@Override
	public String onKill(Npc npc, Player player, boolean isPet) {
		synchronized (attackInfo) {
			invaderInfo info = attackInfo.get(npc.getObjectId()); //Get the attack info
			if (info != null) {
				attackInfo.remove(npc.getObjectId()); //Delete the stored info for this npc
			}
		}

		if (isUnderInvasion) {
			Npc inv =
					addSpawn(invaderIds[Rnd.get(invaderIds.length)], npc.getX() + Rnd.get(100), npc.getY() + Rnd.get(100), npc.getZ(), 0, false, 0);
			invaders.add(inv);
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onFirstTalk(Npc npc, Player player) {
		StringBuilder tb = new StringBuilder();
		tb.append("<html><center><font color=\"3D81A8\">Merry Christmas!</font></center><br1>Hohohoh! Hi " + player.getName() +
				" If you give me some <font color=LEVEL>Star Ornament's</font> I can give you some gifts! Take all what you can!<br>");

		if (exChangeOnly) {
			tb.append(
					"This event is currently working in exchange mode, there are no more invasions or free gifts, you can only exchange your Star Ornaments.<br>");
		} else {
			tb.append(
					"You can get <font color=LEVEL>Star Ornament</font> while participating in the <font color=LEVEL>Snowman's invasion</font> each <font color=LEVEL>" +
							startInvasionEach + "</font> hours.<br>");
			tb.append("<font color=LEVEL>Santa</font> will also visit <font color=LEVEL>randomly</font> each <font color=LEVEL>" +
					rewardRandomPlayerEach + "</font> hours an <font color=LEVEL>active</font> player and will give special random gifts!<br>");
			tb.append(getNextInvasionTime() + "<br1>");
			tb.append(getNextSantaRewardTime() + "<br1>");
			tb.append("<font color=LEVEL>Last random player rewarded: " + (lastSantaRewardedName == null ? "None Yet" : lastSantaRewardedName) +
					"</font><br>");
			if (isUnderInvasion) {
				tb.append("<font color=\"3D81A8\">Available Actons:</font><br>");
				tb.append("<a action=\"bypass -h Quest Christmas teleport_to_fantasy\"><font color=c2dceb>Teleport to Fantasy Island.</font></a><br1>");
			}
		}

		tb.append("<br>");
		tb.append("<font color=\"3D81A8\">Available Gifts:</font><br1>");
		tb.append("<a action=\"bypass -h npc_" + npc.getObjectId() +
				"_multisell christmas_event_shop\"><font color=c2dceb>View the event shop.</font></a><br1>");
		tb.append("<br>");

		if (!exChangeOnly) {
			tb.append("<font color=\"3D81A8\">Free Effects:</font><br1>");
			tb.append(
					"<a action=\"bypass -h Quest Christmas eventEffect 16419\"><font color=c2dceb>Receive the Stocking Fairy's Blessingt buff!</font></a><br1>");
			tb.append(
					"<a action=\"bypass -h Quest Christmas eventEffect 16420\"><font color=c2dceb>Receive the Tree Fairy's Blessing buff!</font></a><br1>");
			tb.append(
					"<a action=\"bypass -h Quest Christmas eventEffect 16421\"><font color=c2dceb>Receive the Snowman Fairy's Blessing buff!</font></a><br1>");
			tb.append("<br>");
			tb.append("<font color=\"3D81A8\">Christmas Music:</font><br1>");
			tb.append("<a action=\"bypass -h Quest Christmas eventMusic\"><font color=c2dceb>Play some music!</font></a><br1>");
			tb.append("<br>");
		}

		//GMPart
		if (player.isGM()) {
			tb.append("<center>~~~~ For GM's Only ~~~~</center> <br1>");
			tb.append("<a action=\"bypass -h Quest Christmas start_invasion_gm\">Start new Invasion</a><br1>");
			tb.append("<a action=\"bypass -h Quest Christmas end_invasion_gm_force\">Force Stop Invasion</a><br1>");
			tb.append("<a action=\"bypass -h Quest Christmas santas_random_player_reward_gm\">Give random reward to a player</a>");
		}

		tb.append("</body></html>");

		NpcHtmlMessage msg = new NpcHtmlMessage(santaId);
		msg.setHtml(tb.toString());
		player.sendPacket(msg);

		return "";
	}

	@SuppressWarnings("unused")
	@Override
	public String onAdvEvent(String event, Npc npc, Player player) {
		if (event.equalsIgnoreCase("santas_talks")) {
			if (exChangeOnly) {
				Announcements.getInstance()
						.announceToAll(
								"Santa's Girl: Hohoho! I need tons of Star Ornaments!!! You have only few days more for get some gifts before I go to my home! Hohoho! Hohoho!");
			} else {
				Announcements.getInstance()
						.announceToAll(
								"Santa's Girl: Hohoho! I need tons of Star Ornaments!!! Please collect them in Snowman's invasion for me!! You can found me at any Town! Hohoho! Hohoho!");
			}
		} else if (event.equalsIgnoreCase("teleport_to_fantasy")) {
			player.teleToLocation(-59004, -56889, -2032, true);
		} else if (event.startsWith("eventEffect")) {
			int effect = Integer.valueOf(event.replace("eventEffect", "").trim());
			if (effect == 16419 || effect == 16420 || effect == 16421) {
				SkillTable.getInstance().getInfo(effect, 1).getEffects(player, player);
			}
		} else if (event.equalsIgnoreCase("eventMusic")) {
			int rnd = Rnd.get(4) + 1;
			player.sendPacket(new PlaySound(1, "CC_0" + rnd, 0, 0, 0, 0, 0));
		} else if (event.startsWith("santas_random_player_reward")) {
			if (exChangeOnly) {
				return "";
			}

			List<Player> playerList = new ArrayList<Player>();
			for (Player pl : World.getInstance().getAllPlayersArray()) {
				if (pl != null && pl.isOnline() && pl.isInCombat() && !pl.isInsideZone(Creature.ZONE_PEACE) && !pl.isFlyingMounted() &&
						pl.getClient() != null && !pl.getClient().isDetached()) {
					if (rewardedPlayers.contains(pl.getExternalIP())) {
						continue;
					}

					playerList.add(pl);
				}
			}

			if (!playerList.isEmpty()) {
				player = playerList.get(Rnd.get(playerList.size()));
				if (player != null) {
					rewardedPlayers.add(player.getExternalIP());
					lastSantaRewardedName = player.getName();

					int locx = (int) (player.getX() + Math.pow(-1, Rnd.get(1, 2)) * 50);
					int locy = (int) (player.getY() + Math.pow(-1, Rnd.get(1, 2)) * 50);
					int heading = Util.calculateHeadingFrom(locx, locy, player.getX(), player.getY());

					santa = addSpawn(secondSantaId, locx, locy, player.getZ(), heading, false, 30000);

					startQuestTimer("santas_reward_1", 5000, null, null);

					if (!event.equalsIgnoreCase("santas_random_player_reward_gm")) {
						startQuestTimer("santas_random_player_reward", rewardRandomPlayerEach * 3600000, null, null);
						nextSantaReward = System.currentTimeMillis() + rewardRandomPlayerEach * 3600000;
					}
					//System.out.println("CHRISTMAS REWARDING: " + player.getName());
					GmListTable.broadcastMessageToGMs("Christmas: Rewarding: " + player.getName() + " IP: " + player.getExternalIP());
				}
			}
		} else if (event.equalsIgnoreCase("santas_reward_1")) {
			final NpcSay msg = new NpcSay(santa.getObjectId(), 0, santa.getNpcId(), NpcStringId.I_HAVE_A_GIFT_FOR_S1);
			msg.addStringParameter(player.getName());

			santa.broadcastPacket(msg);

			startQuestTimer("santas_reward_2", 5000, null, null);
		} else if (event.equalsIgnoreCase("santas_reward_2")) {
			//Select random reward
			int reward[] = randomRewards[Rnd.get(randomRewards.length)];
			santa.broadcastPacket(new SocialAction(santa.getObjectId(), 2));
			santa.broadcastPacket(new NpcSay(santa.getObjectId(),
					0,
					santa.getNpcId(),
					NpcStringId.TAKE_A_LOOK_AT_THE_INVENTORY_I_HOPE_YOU_LIKE_THE_GIFT_I_GAVE_YOU));

			int rndCount = Rnd.get(1, reward[1]);
			player.addItem("Christmas", reward[0], rndCount, player, true);

			GmListTable.broadcastMessageToGMs("Christmas: Player: " + player.getName() + " rewarded with: " + rndCount + " " +
					ItemTable.getInstance().getTemplate(reward[0]).getName());

			player = null;
		} else if (event.startsWith("start_invasion")) {
			if (isUnderInvasion || exChangeOnly) {
				return "";
			}

			isUnderInvasion = true;

			int radius = 1000;
			for (int a = 0; a < 2; a++) {
				for (int i = 0; i < 50; i++) {
					int x = (int) (radius * Math.cos(i * 0.618));
					int y = (int) (radius * Math.sin(i * 0.618));

					Npc
							inv = addSpawn(invaderIds[Rnd.get(invaderIds.length)], -59718 + x, -56909 + y, -2029 + 20, -1, false, 0, false, 0);
					invaders.add(inv);
				}
				radius += 300;
			}

			Announcements.getInstance().announceToAll("Fantasy Island is under Snowmen's invasion!");
			Announcements.getInstance().announceToAll("Don't attack mobs from other players!");
			Announcements.getInstance().announceToAll("Dualbox is not allowed on the event!");
			Announcements.getInstance().announceToAll("The invasion will lasts for: " + timeToEndInvasion + " minute(s)!");

			startQuestTimer(event.equalsIgnoreCase("start_invasion") ? "end_invasion" : "end_invasion_gm", timeToEndInvasion * 60000, null, null);
		} else if (event.startsWith("end_invasion")) {
			isUnderInvasion = false;

			if (event.equalsIgnoreCase("end_invasion_gm_force")) {
				QuestTimer timer = getQuestTimer("end_invasion_gm", null, null);
				if (timer != null) {
					timer.cancel();
				}
			}

			for (Creature chara : invaders) {
				if (chara == null) {
					continue;
				}
				chara.deleteMe();
			}

			invaders.clear();
			attackInfo.clear();

			Announcements.getInstance().announceToAll("The invasion has been ended!");

			//Only schedule the next invasion if is not started by a GM
			if (!event.startsWith("end_invasion_gm")) {
				startQuestTimer("start_invasion", startInvasionEach * 3600000, null, null);
				nextInvasion = System.currentTimeMillis() + startInvasionEach * 3600000;
			}
		}
		return "";
	}

	private static String getNextInvasionTime() {
		Long remainingTime = (nextInvasion - System.currentTimeMillis()) / 1000;
		int hours = (int) (remainingTime / 3600);
		int minutes = (int) (remainingTime % 3600 / 60);

		if (minutes < 0) {
			return "<font color=LEVEL>Next Invasion in: Currently under invasion!</font>";
		}

		return "<font color=LEVEL>Next Invasion in: " + hours + " hours and " + minutes + " minutes!</font>";
	}

	private static String getNextSantaRewardTime() {
		Long remainingTime = (nextSantaReward - System.currentTimeMillis()) / 1000;
		int hours = (int) (remainingTime / 3600);
		int minutes = (int) (remainingTime % 3600 / 60);

		if (minutes < 0) {
			return "<font color=LEVEL>Next Santa Reward in: Currently rewarding a player!</font>";
		}

		return "<font color=LEVEL>Next Santa Reward: " + hours + " hours and " + minutes + " minutes!</font>";
	}

	@Override
	public int getOnKillDelay(int npcId) {
		return 0;
	}

	public static void main(String[] args) {
		new Christmas(-1, "Christmas", "events");
	}
}
