package events.DCEvent;

import l2server.gameserver.Announcements;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.model.zone.ZoneType;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.PlaySound;
import l2server.gameserver.util.Broadcast;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Inia
 */

public class DCEvent extends Quest {
	//Config
	private static final boolean exChangeOnly = false;
	private static int timeToEndInvasion = 1; //Minutes
	private static final int npcId = 92000;
	private static final int[] invaderIds = {92011, 92010};
	private static final int boosId = 92011;

	//Vars
	private static Long nextInvasion;
	private static Player player;
	private static boolean isUnderInvasion = false;
	private Map<Integer, invaderInfo> attackInfo = new HashMap<Integer, invaderInfo>();
	private ArrayList<Creature> invaders = new ArrayList<Creature>();
	private ArrayList<Creature> registered = new ArrayList<Creature>();
	private Map<Creature, Integer> testPlayer = new HashMap<Creature, Integer>();

	public DCEvent(int id, String name, String descr) {
		super(id, name, descr);

		addStartNpc(npcId);
		addTalkId(npcId);
		addFirstTalkId(npcId);

		for (int mob : invaderIds) {
			addAttackId(mob);
			addKillId(mob);
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
	public String onKill(Npc npc, Player player, boolean isPet) {

		Npc inv = addSpawn(invaderIds[Rnd.get(invaderIds.length)], npc.getX() + Rnd.get(100), npc.getY() + Rnd.get(100), npc.getZ(), 0, false, 0);
		invaders.add(inv);
		int points = testPlayer.get(player);
		int toAdd = 1;

		testPlayer.replace(player, points + toAdd);

		return "";
	}

	@Override
	public String onDieZone(Creature character, Creature killer, ZoneType zone) {
		if (isUnderInvasion) {
			Player player = killer.getActingPlayer();
			if (player != null) {
				player.increasePvpKills(character);
				player.addItem("coin", 4037, 1, player, true);
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(Npc npc, Player player) {
		if (isUnderInvasion) {
			StringBuilder tb = new StringBuilder();
			tb.append("<html><center><font color=\"3D81A8\">Melonis!</font></center><br1>Hi " + player.getName() +
					"<br> The event is already started.<br><Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h Quest DCEvent teleport\">Teleport to event !</Button>");
			if (player.isGM()) {
				tb.append(
						"<html><center> <br> GM Panel<br><Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h Quest DCEvent end_invasion\">Stop event</Button>");
			}
			NpcHtmlMessage msg = new NpcHtmlMessage(npcId);
			msg.setHtml(tb.toString());
			player.sendPacket(msg);
			return ("");
		}

		return "DCEventShop.html";
	}

	@SuppressWarnings("unused")
	@Override
	public String onAdvEvent(String event, Npc npc, Player player) {
		if (event.startsWith("trySpawnBoss")) {
			timeToEndInvasion = Integer.valueOf(event.split(" ")[1]);

			int price = 0;
			if (timeToEndInvasion == 5) {
				price = 5;
			} else if (timeToEndInvasion == 10) {
				price = 8;
			} else if (timeToEndInvasion == 15) {
				price = 12;
			}
			if (!player.destroyItemByItemId("coin", 4037, price, player, true)) {
				StringBuilder tb = new StringBuilder();
				tb.append("<html><center><font color=\"3D81A8\">Melonis!</font></center><br1>Sorry " + player.getName() +
						" but I need  <font color=LEVEL>" + price + "</font> Coins of luck<br>");
				NpcHtmlMessage msg = new NpcHtmlMessage(npcId);
				msg.setHtml(tb.toString());
				player.sendPacket(msg);
				return ("");
			}
			Broadcast.toAllOnlinePlayers(new ExShowScreenMessage(
					player.getName() + " started the dragon claw event for  " + timeToEndInvasion + " minutes !", 7000));

			startQuestTimer("start_invasion", 1, null, null);
		} else if (event.equalsIgnoreCase("teleport")) {
			player.teleToLocation(-54481, -69402, -3416, true);

			testPlayer.put(player, 0);
			registered.add(player);
		} else if (event.equalsIgnoreCase("teleport_back")) {
			player.teleToLocation(-114435, 253417, -1546, true);
		} else if (event.equalsIgnoreCase("eventMusic")) {
			int rnd = Rnd.get(4) + 1;
			player.sendPacket(new PlaySound(1, "CC_0" + rnd, 0, 0, 0, 0, 0));
		} else if (event.startsWith("start_talk")) {
			Announcements.getInstance().announceToAll("1 2 3!");
		} else if (event.startsWith("start_invasion")) {
			if (isUnderInvasion) {
				return "";
			}

			isUnderInvasion = true;

			addSpawn(npcId, -114358, 253164, -1541, 24266, false, timeToEndInvasion);

			int minX = -53749;
			int maxX = -55287;
			int minY = -68835;
			int maxY = -70258;
			int radius = 500;

			for (int a = 0; a < 3; a++) {
				for (int i = 0; i < 50; i++) {
					int x = Rnd.get(minX, maxX) + 1;
					int y = Rnd.get(minY, maxY) + 1;
					int x2 = (int) (radius * Math.cos(i * 0.618));
					int y2 = (int) (radius * Math.sin(i * 0.618));

					Npc inv = addSpawn(invaderIds[Rnd.get(invaderIds.length)], x, y, -3416 + 20, -1, false, 0, false, 0);

					invaders.add(inv);
				}
				radius += 300;
			}

			startQuestTimer(event.equalsIgnoreCase("start_invasion") ? "end_invasion" : "end_invasion_gm", timeToEndInvasion * 60000, null, null);
		} else if (event.startsWith("spawn_boss")) {

			startQuestTimer("delete_boss", 60000, null, null);
			Npc boss = addSpawn(boosId, -54481, -69402, -3416, 0, false, 0, true);
			invaders.add(boss);
		} else if (event.startsWith("delete_boss")) {
			for (Creature delete : invaders) {
				if (delete == null) {
					continue;
				}
				delete.deleteMe();
			}

			for (Creature test : registered) {
				if (test == null) {
					continue;
				}
				test.teleToLocation(-114435, 253417, -1546, true);
			}
			invaders.clear();
			registered.clear();
			attackInfo.clear();
			Announcements.getInstance().announceToAll("FINISHED");
			isUnderInvasion = false;
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

			for (Map.Entry<Creature, Integer> ontest : testPlayer.entrySet()) {
				if (ontest == null) {
					continue;
				}
				Creature toTp = ontest.getKey();
				int totalPoints = ontest.getValue();

				Player oui = (Player) ontest.getKey();
				Announcements.getInstance().announceToAll("Player : " + toTp.getName() + " Points 1" + totalPoints);
				oui.addItem("coin", 36414, totalPoints, oui, true);
				toTp.teleToLocation(-114435, 253417, -1546, true);
			}

			registered.clear();
			invaders.clear();
			attackInfo.clear();
			isUnderInvasion = false;
			Announcements.getInstance().announceToAll("Event finished !");

			//Only schedule the next invasion if is not started by a GM

		}
		return "";
	}

	public static void main(String[] args) {
		new DCEvent(-1, "DCEvent", "events");
	}
}
