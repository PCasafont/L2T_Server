package events.SurpriseEvent;

import l2server.gameserver.Announcements;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.NpcSay;
import l2server.gameserver.templates.item.WeaponTemplate;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Inia
 * <p>
 * Little custom SurpriseEvent event
 */

public class SurpriseEvent extends Quest {
	//Config
	private static final boolean exChangeOnly = false;
	private static final int startInvasionEach = 48; //Hours
	private static final int timeToEndInvasion = 10; //Minutes
	private static final int rewardRandomPlayerEach = 48; //Hours
	private static final int santaTalksEach = 12; //Hours
	private static final int santaId = 91000;
	private static final int secondSantaId = 104;
	private static final int[] invaderIds = {91001};
	public int nb_box = 3;
	public int save = nb_box;

	//Vars
	private static Long nextInvasion;
	private static Long nextSantaReward;
	private static String lastSantaRewardedName;
	private static Player player;
	private static Npc santa;
	private static boolean isUnderInvasion = false;
	private Map<Integer, invaderInfo> attackInfo = new HashMap<Integer, invaderInfo>();
	private ArrayList<Creature> invaders = new ArrayList<Creature>();
	private ArrayList<String> rewardedPlayers = new ArrayList<String>(); //IP based
	private static final int[][] randomRewards = {
			//Item Id, ammount
			{57, 250000000}, // Adena
			{36414, 10}, // Dragon Claw
			{37559, 50}, //Shiny Coin
			{4037, 2}, //Coin Of Luck
			{36515, 500}, //Elcyum
			{20770, 3}, //Apple Basket
			{21235, 100} //Milk
	};
	private static final String[][] _reward_name = {{"Adena"}, {"Dragon Claw"}, {"Shiny Coin"}, {"Coin Of Luck"}};
	private static final int loc[][] =
			{{-115528, 252701, -1497}, {-116654, 253371, -1500}, {-117617, 253961, -1534}, {-118697, 255202, -1430}, {-117632, 256107, -1327},
					{-117437, 256836, -1422}, {-114606, 257740, -1201}, {-112181, 256530, -1431}, {-111860, 257352, -1472}, {-113256, 254961, -1498},
					{-113063, 253622, -1517}, {-113383, 252115, -1517}};

	public SurpriseEvent(int id, String name, String descr) {
		super(id, name, descr);

		addStartNpc(santaId);
		addTalkId(santaId);
		addFirstTalkId(santaId);

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
	public String onAttack(Npc npc, Player player, int damage, boolean isPet, Skill skill) {

		synchronized (attackInfo) {
			invaderInfo info = attackInfo.get(npc.getObjectId()); //Get the attack info from this npc

			for (Map.Entry<Integer, invaderInfo> entry : attackInfo.entrySet()) {
				if (entry == null) {
					continue;
				}

				invaderInfo i = entry.getValue();
				if (i == null) {
					continue;
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
		WeaponTemplate playerWeapon = player.getActiveWeaponItem();
		if (playerWeapon != null) {
			player.sendPacket(new NpcSay(npc.getObjectId(), 2, npc.getTemplate().TemplateId, player.getName() + " You should use your hands!"));
			return "";
		} else {
			attackInfo.remove(npc.getObjectId()); //Delete the stored info for this npc
			int random = Rnd.get(randomRewards.length);
			player.addItem("Surpise Event", randomRewards[random][0], randomRewards[random][1], player, true);
			Creature chara = (Creature) player.getTarget();
			chara.deleteMe();
			nb_box--;
			Announcements.getInstance().announceToAll("A box has been found by " + player.getName() + "!");
			//for(int k=0; k<_reward_name[random].length; k++)
			//	Announcements.getInstance().announceToAll( player.getName() + " found : " + randomRewards[random][1] + " " +  _reward_name[random][k]);
			Announcements.getInstance().announceToAll("Boxes left :  " + nb_box + "!");
			if (nb_box <= 0) {
				for (Creature charaa : invaders) {
					if (charaa == null) {
						continue;
					}
					charaa.deleteMe();
				}
				invaders.clear();
				attackInfo.clear();
				isUnderInvasion = false;
				Announcements.getInstance().announceToAll("All the boxes have been found!");
				nb_box = 3;
			}
			return super.onKill(npc, player, isPet);
		}
	}

	@Override
	public String onKill(Npc npc, Player player, boolean isPet) {

		synchronized (attackInfo) {
			invaderInfo info = attackInfo.get(npc.getObjectId()); //Get the attack info
			if (info != null) {
				attackInfo.remove(npc.getObjectId()); //Delete the stored info for this npc
			}

			return super.onKill(npc, player, isPet);
		}
	}

	@Override
	public String onFirstTalk(Npc npc, Player player) {
		StringBuilder tb = new StringBuilder();
		tb.append("<html><center><font color=\"3D81A8\">MEUUUH!</font></center><br1>Hohohoh! Hi " + player.getName() +
				" If you give me some <font color=LEVEL>Milk</font> I can give you some gifts! Take all what you can!<br>");

		if (exChangeOnly) {
			tb.append(
					"This event is currently working in exchange mode, there are no more invasions or free gifts, you can only exchange your Milk.<br>");
		} else {
			tb.append(
					"You can get <font color=LEVEL>Milk</font> while participating in the <font color=LEVEL>Cow invasion</font> each <font color=LEVEL>" +
							startInvasionEach + "</font> hours.<br>");
			tb.append("<font color=LEVEL>King of the cows</font> will also visit <font color=LEVEL>randomly</font> each <font color=LEVEL>" +
					rewardRandomPlayerEach + "</font> hours an <font color=LEVEL>active</font> player and will give special random gifts!<br>");

			tb.append("<font color=LEVEL>Last random player rewarded: " + (lastSantaRewardedName == null ? "None Yet" : lastSantaRewardedName) +
					"</font><br>");
		}

		tb.append("<br>");
		tb.append("<font color=\"3D81A8\">Available Gifts:</font><br1>");
		tb.append("<a action=\"bypass -h npc_" + npc.getObjectId() +
				"_multisell SurpriseEvent_event_shop\"><font color=c2dceb>View the event shop.</font></a><br1>");
		tb.append("<br>");

		/*if (!exChangeOnly)
		{
			tb.append("<font color=\"3D81A8\">Free Effects:</font><br1>");
			tb.append(
					"<a action=\"bypass -h Quest SurpriseEvent eventEffect 16419\"><font color=c2dceb>Receive the Stocking Fairy's Blessingt buff!</font></a><br1>");
			tb.append(
					"<a action=\"bypass -h Quest SurpriseEvent eventEffect 16420\"><font color=c2dceb>Receive the Tree Fairy's Blessing buff!</font></a><br1>");
			tb.append(
					"<a action=\"bypass -h Quest SurpriseEvent eventEffect 16421\"><font color=c2dceb>Receive the Snowman Fairy's Blessing buff!</font></a><br1>");
			tb.append("<br>");
			tb.append("<font color=\"3D81A8\">SurpriseEvent Music:</font><br1>");
			tb.append(
					"<a action=\"bypass -h Quest SurpriseEvent eventMusic\"><font color=c2dceb>Play some music!</font></a><br1>");
			tb.append("<br>");
		}*/

		//GMPart
		if (player.isGM()) {
			tb.append("<center>~~~~ For GM's Only ~~~~</center> <br1>");
			tb.append("<a action=\"bypass -h Quest SurpriseEvent start_invasion_gm\">Start new Invasion</a><br1>");
			tb.append("<a action=\"bypass -h Quest SurpriseEvent end_invasion_gm_force\">Force Stop Invasion</a><br1>");
			tb.append("<a action=\"bypass -h Quest SurpriseEvent santas_random_player_reward_gm\">Give random reward to a player</a>");
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

		if (event.startsWith("start_invasion")) {
			if (isUnderInvasion) {
				return "";
			}

			isUnderInvasion = true;

			nb_box = 3;

			for (int i = 0; i < nb_box; i++) {
				int r = Rnd.get(loc.length);
				Npc inv = addSpawn(invaderIds[Rnd.get(invaderIds.length)], loc[r][0], loc[r][1], loc[r][2] + 20, -1, false, 0);
				inv.setImmobilized(true);
				inv.setMortal(false);
				invaders.add(inv);
			}

			Announcements.getInstance().announceToAll(nb_box + " box(es) has spawned in Talking Island !");
			Announcements.getInstance().announceToAll("You will have : " + timeToEndInvasion + " minutes to find them !");
			Announcements.getInstance().announceToAll("Hit the box with your HANDS to win !");
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

			Announcements.getInstance().announceToAll("");

			//Only schedule the next invasion if is not started by a GM
			if (!event.startsWith("end_invasion_gm")) {
				startQuestTimer("start_invasion", startInvasionEach * 3600000, null, null);
				nextInvasion = System.currentTimeMillis() + startInvasionEach * 3600000;
			}
		}
		return "";
	}

	@Override
	public int getOnKillDelay(int npcId) {
		return 0;
	}

	public static void main(String[] args) {
		new SurpriseEvent(-1, "SurpriseEvent", "events");
	}
}
