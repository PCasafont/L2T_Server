/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.instancemanager;

import l2server.Config;
import l2server.gameserver.Announcements;
import l2server.gameserver.GmListTable;
import l2server.gameserver.Ranked1v1;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.communitybbs.Manager.CustomCommunityBoard;
import l2server.gameserver.datatables.*;
import l2server.gameserver.datatables.MapRegionTable.TeleportWhereType;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.World;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.ColosseumFence;
import l2server.gameserver.model.actor.ColosseumFence.FenceState;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.DoorInstance;
import l2server.gameserver.model.actor.instance.NpcInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.StaticObjectInstance;
import l2server.gameserver.model.entity.Message;
import l2server.gameserver.model.itemcontainer.Mail;
import l2server.gameserver.model.olympiad.OlympiadManager;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.zone.ZoneType;
import l2server.gameserver.model.zone.type.ArenaZone;
import l2server.gameserver.model.zone.type.PeaceZone;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.gameserver.network.serverpackets.SocialAction;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;
import l2server.gameserver.util.NpcUtil;
import l2server.gameserver.util.Util;
import l2server.util.loader.annotations.Load;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author LasTravel
 */

public class GMEventManager {
	private static Logger log = LoggerFactory.getLogger(GMEventManager.class.getName());


	private static final int bufferNpcId = 8508;
	private static final int dummyArenaSignNpcId = 50013;
	private static final int rewardCoinId = 37559;
	private Map<String, Event> predefinedEvents = new HashMap<>();
	private static Map<Integer, CurrencyInfo> currencies = new LinkedHashMap<>();
	private static Map<String, SubEvent> subEvents = new HashMap<>();
	private static Event currentEvent;

	public String getCustomEventPanel(Player player, int pageToShow) {
		StringBuilder sb = new StringBuilder();
		if (player.isGM()) {
			sb.append("<table><tr>" + "<td>" +
					"<button value=\"Creature Invasion\" width=140 height=24 action=\"bypass _bbscustom;action;gEvent;startSubEvent; CreatureInvasion\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>" +
					"</td>" + "<td>" +
					"<button value=\"Cows Invasion\" width=140 height=24 action=\"bypass _bbscustom;action;gEvent;startSubEvent; Christmas\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>" +
					"</td>" + "<td>" +
					"<button value=\"Chests\" width=140 height=24 action=\"bypass _bbscustom;action;gEvent;startSubEvent; SurpriseEvent\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>" +
					"</td>" + "</tr></table>");

			sb.append("<table width=750 border=0>");
		}
		if (player.isGM()) {
			if (currentEvent == null || !currentEvent.isStarted()) {

				sb.append("<tr><td><table width=750 border=1>");
				if (currentEvent == null || !currentEvent.isStarted()) {
					String subEvents = "";
					for (Entry<String, SubEvent> event : this.subEvents.entrySet()) {
						subEvents += event.getKey() + ";";
					}

					if (!subEvents.isEmpty()) {
						sb.append("<tr><td>Start sub event:</td><td><combobox width=100 height=17 var=\"subEvent\" list=" + subEvents +
								"></td><td><button action=\"bypass _bbscustom;action;gEvent;startSubEvent; $subEvent ;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td></tr>");
					}

					String predefinedEvents = "";
					for (Entry<String, Event> event : this.predefinedEvents.entrySet()) {
						predefinedEvents += event.getValue().getName() + ";";
					}

					if (!predefinedEvents.isEmpty()) {
						sb.append("<tr><td>Load predefined event:</td><td><combobox width=100 height=17 var=\"loadEvent\" list=" + predefinedEvents +
								"></td><td><button action=\"bypass _bbscustom;action;gEvent;loadEvent; $loadEvent ;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td></tr>");
					}

					String eventName =
							"<td FIXWIDTH=200><edit var=\"eName\" width=100 length=25></td><td><button action=\"bypass _bbscustom;action;gEvent;setName; $eName ;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td>";
					if (currentEvent != null && currentEvent.getName() != null) {
						eventName = "<td FIXWIDTH=200>" + currentEvent.getName() +
								"</td><td><button action=\"bypass _bbscustom;action;gEvent;delName;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td>";
					}

					String eventDescription =
							"<td><edit var=\"eDesc\" width=100 length=25></td><td><button action=\"bypass _bbscustom;action;gEvent;setDesc; $eDesc ; \" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td>";
					if (currentEvent != null && currentEvent.getDescription() != null) {
						eventDescription = "<td>" + currentEvent.getDescription() +
								"</td><td><button action=\"bypass _bbscustom;action;gEvent;delDesc;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td>";
					}

					String eventLocation =
							"<td><edit var=\"eLoc\" width=100 length=25></td><td><button action=\"bypass _bbscustom;action;gEvent;setLoc; $eLoc ; \" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td>";
					if (currentEvent != null && currentEvent.getLocation() != null) {
						eventLocation = "<td>" + currentEvent.getLocation() +
								"</td><td><button action=\"bypass _bbscustom;action;gEvent;delLoc;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td>";
					}

					String arenaZoneName = "";
					if (currentEvent != null && currentEvent.getArenaZones() != null) {
						for (ArenaZone zone : currentEvent.getArenaZones()) {
							if (zone == null) {
								continue;
							}
							arenaZoneName += zone.getName() + "<br1> ";
						}
					}

					String peaceZoneName = "";
					if (currentEvent != null && currentEvent.getPeaceZones() != null) {
						for (PeaceZone zone : currentEvent.getPeaceZones()) {
							if (zone == null) {
								continue;
							}
							peaceZoneName += zone.getName() + "<br1> ";
						}
					}

					String teamOneSpawn =
							"<td><button action=\"bypass _bbscustom;action;gEvent;setTeamOneSpawn;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td>";
					if (currentEvent != null && currentEvent.getTeamOneSpawn() != null) {
						teamOneSpawn = "<td>" + currentEvent.getTeamOneSpawn().getX() + ", " + currentEvent.getTeamOneSpawn().getY() + ", " +
								currentEvent.getTeamOneSpawn().getZ() +
								"</td><td><button action=\"bypass _bbscustom;action;gEvent;delTeamOneSpawn;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td>";
					}

					String teamTwoSpawn =
							"<td><button action=\"bypass _bbscustom;action;gEvent;setTeamTwoSpawn;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td>";
					if (currentEvent != null && currentEvent.getTeamTwoSpawn() != null) {
						teamTwoSpawn = "<td>" + currentEvent.getTeamTwoSpawn().getX() + ", " + currentEvent.getTeamTwoSpawn().getY() + ", " +
								currentEvent.getTeamTwoSpawn().getZ() +
								"</td><td><button action=\"bypass _bbscustom;action;gEvent;delTeamTwoSpawn;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td>";
					}

					String spawnBufferOne =
							"<td><button action=\"bypass _bbscustom;action;gEvent;setSpawnBufferOne;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td>";
					if (currentEvent != null && currentEvent.getSpawnBufferOne() != null) {
						spawnBufferOne = "<td>" + currentEvent.getSpawnBufferOne().getX() + ", " + currentEvent.getSpawnBufferOne().getY() + ", " +
								currentEvent.getSpawnBufferOne().getZ() +
								"</td><td><button action=\"bypass _bbscustom;action;gEvent;delSpawnBufferOne;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td>";
					}

					String spawnBufferTwo =
							"<td><button action=\"bypass _bbscustom;action;gEvent;setSpawnBufferTwo;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td>";
					if (currentEvent != null && currentEvent.getSpawnBufferTwo() != null) {
						spawnBufferTwo = "<td>" + currentEvent.getSpawnBufferTwo().getX() + ", " + currentEvent.getSpawnBufferTwo().getY() + ", " +
								currentEvent.getSpawnBufferTwo().getZ() +
								"</td><td><button action=\"bypass _bbscustom;action;gEvent;delSpawnBufferTwo;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td>";
					}

					String spawnLoc =
							"<td><button action=\"bypass _bbscustom;action;gEvent;setSpawnLoc;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td>";
					if (currentEvent != null && currentEvent.getTeleportLocation() != null) {
						spawnLoc = "<td>" + currentEvent.getTeleportLocation().getX() + ", " + currentEvent.getTeleportLocation().getY() + ", " +
								currentEvent.getTeleportLocation().getZ() +
								"</td><td><button action=\"bypass _bbscustom;action;gEvent;delSpawnLoc;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td>";
					}

					String doors = "";
					if (currentEvent != null && currentEvent.getDoors() != null) {
						for (int i : currentEvent.getDoors()) {
							doors += i + "<br>";
						}
					}

					String arenaSign = "";
					if (currentEvent != null && currentEvent.getArenaSignIds() != null) {
						for (int id : currentEvent.getArenaSignIds()) {
							arenaSign += id + "<br1> ";
						}
					}

					String arenaSignSpawns = "";
					if (currentEvent != null && currentEvent.getArenaSignSpawns() != null) {
						for (Location loc : currentEvent.getArenaSignSpawns()) {
							arenaSignSpawns += loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + "<br1> ";
						}
					}

					sb.append("<tr><td FIXWIDTH=200>Enter the Event Name:</td>" + eventName + "</tr>");
					sb.append("<tr><td>Enter the Event Description:</td>" + eventDescription + "</tr>");
					sb.append("<tr><td>Enter the Event Location:</td>" + eventLocation + "</tr>");
					sb.append("<tr><td>Set Arena Zone:</td><td>" + arenaZoneName +
							"</td><td><button action=\"bypass _bbscustom;action;gEvent;setArena;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td><td><button action=\"bypass _bbscustom;action;gEvent;delArena;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td></tr>");
					sb.append("<tr><td>Set Peace Zone:</td><td>" + peaceZoneName +
							"</td><td><button action=\"bypass _bbscustom;action;gEvent;setPeace;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td><td><button action=\"bypass _bbscustom;action;gEvent;delPeace;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td></tr>");
					sb.append("<tr><td>Team One Spawn:</td>" + teamOneSpawn + "</tr>");
					sb.append("<tr><td>Team Two Spawn:</td>" + teamTwoSpawn + "</tr>");
					sb.append("<tr><td>Team One Buffer Spawn:</td>" + spawnBufferOne + "</tr>");
					sb.append("<tr><td>Team Two Buffer Spawn:</td>" + spawnBufferTwo + "</tr>");
					sb.append("<tr><td>Event Spawn:</td>" + spawnLoc + "</tr>");
					sb.append("<tr><td>Doors:</td><td>" + doors +
							"</td><td><td><button action=\"bypass _bbscustom;action;gEvent;setDoor;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td><td><button action=\"bypass _bbscustom;action;gEvent;delDoor;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td></tr>");
					sb.append("<tr><td>Arena Signs:</td><td>" + arenaSign +
							"</td><td><td><button action=\"bypass _bbscustom;action;gEvent;setArenaSign;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td><td><button action=\"bypass _bbscustom;action;gEvent;delArenaSign;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td></tr>");
					sb.append("<tr><td>Arena Sign Spawns:</td><td>" + arenaSignSpawns +
							"</td><td><td><button action=\"bypass _bbscustom;action;gEvent;addSignSpawn;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td><td><button action=\"bypass _bbscustom;action;gEvent;delSignSpawn;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td></tr>");
					sb.append(
							"<tr><td><button value=\"Start\" width=200 height=24 action=\"bypass _bbscustom;action;gEvent;startEvent;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button></td><td><button value=\"Restart Config\" width=200 height=24 action=\"bypass _bbscustom;action;gEvent;restartConfig;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button></td></tr>");
				}
				sb.append("</table></td></tr>");
			} else {
				String reAnnounce =
						"<button value=\"Re Announce\" width=100 height=24 action=\"bypass _bbscustom;action;gEvent;reAnnounce;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>";

				String manageDoors = "";
				if (currentEvent != null && currentEvent.isStarted() && !currentEvent.getDoors().isEmpty()) {
					manageDoors =
							"<button value=\"Manage Doors\" width=100 height=24 action=\"bypass _bbscustom;action;gEvent;manageDoors;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>";
				}

				String manageFight =
						"<button value=\"Start Fight\" width=100 height=24 action=\"bypass _bbscustom;action;gEvent;startFight;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>";
				if (currentEvent != null && currentEvent.isFightStarted()) {
					manageFight =
							"<button value=\"Stop Fight\" width=100 height=24 action=\"bypass _bbscustom;action;gEvent;stopFight;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>";
				}

				String manageFences =
						"<button value=\"Refresh\" width=100 height=24 action=\"bypass _bbscustom;action;gEvent;addFences;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>";
				if (currentEvent != null && !currentEvent.getFences().isEmpty()) {
					manageFences =
							"<button value=\"Refresh\" width=100 height=24 action=\"bypass _bbscustom;action;gEvent;delFences;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>";
				}

				String manageBets =
						"<button value=\"Open Bets\" width=100 height=24 action=\"bypass _bbscustom;action;gEvent;addBets;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>";
				if (currentEvent != null && currentEvent.getAllowBets()) {
					manageBets =
							"<button value=\"Close Bets\" width=100 height=24 action=\"bypass _bbscustom;action;gEvent;delBets;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>";
				}

				String returnBets = "";
				String reload =
						"<button value=\"Reload\" width=100 height=24 action=\"bypass _bbscustom;gmEvent;1;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>";

				if (currentEvent != null && currentEvent.hasBets()) {
					returnBets =
							"<button value=\"Return Bets\" width=100 height=24 action=\"bypass _bbscustom;action;gEvent;returnBets;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>";
				}

				boolean hasBets = currentEvent != null && currentEvent.hasBets();

				sb.append("<tr><td><table width=750 border=0><tr><td>" + reAnnounce + "</td><td>" + manageDoors + "</td><td>" + manageFences +
						"</td><td>" + manageBets + "</td><td>" + returnBets + "</td><td>" + manageFight +
						"</td><td><button value=\"Restart Fight\" width=100 height=24 action=\"bypass _bbscustom;action;gEvent;restartFight;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button></td><td><button value=\"Stop Event\" width=100 height=24 action=\"bypass _bbscustom;action;gEvent;stopEvent;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button></td></tr></table></td></tr>");
				sb.append(
						"<tr><td><table width=750 bgcolor=999999 border=0><tr><td FIXWIDTH=150>Team One Players</td><td FIXWIDTH=50><combobox width=100 height=17 var=\"rCount1\" list=1;2;3;4;5;10;15></td><td FIXWIDTH=350><button action=\"bypass _bbscustom;action;gEvent;giveReward;1; $rCount1 ;\" value=\" \" width=16 height=16 back=L2UI_CH3.joypad_r_hold fore=L2UI_CH3.joypad_r_over></button></td><td FIXWIDTH=200>" +
								(hasBets ?
										"<button value=\"Reward Bets\" width=100 height=24 action=\"bypass _bbscustom;action;gEvent;giveBetRewards;1\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>" :
										"") + "</td></tr></table></td></tr>");
				sb.append("<tr><td><table width=750>");
				for (Entry<Integer, Integer> i : currentEvent.getParticipants().entrySet()) {
					if (i == null) {
						continue;
					}

					Player pl = World.getInstance().getPlayer(i.getKey());
					if (pl == null) {
						continue;
					}

					if (i.getValue() == 1) {
						sb.append("<tr><td FIXWIDTH=200>" + pl.getName() +
								"</td><td FIXWIDTH=550><button action=\"bypass _bbscustom;action;gEvent;delPlayer;" + pl.getObjectId() +
								"\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td></tr>");
					}
				}
				sb.append("</table></td></tr>");

				sb.append(
						"<tr><td><table width=750 bgcolor=999999 border=0><tr><td FIXWIDTH=150>Team Two Players</td><td FIXWIDTH=50><combobox width=100 height=17 var=\"rCount2\" list=1;2;3;4;5></td><td FIXWIDTH=350><button action=\"bypass _bbscustom;action;gEvent;giveReward;2; $rCount2 ;\" value=\" \" width=16 height=16 back=L2UI_CH3.joypad_r_hold fore=L2UI_CH3.joypad_r_over></button></td><td FIXWIDTH=200>" +
								(hasBets ?
										"<button value=\"Reward Bets\" width=100 height=24 action=\"bypass _bbscustom;action;gEvent;giveBetRewards;2\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>" :
										"") + "</td></tr></table></td></tr>");
				sb.append("<tr><td><table width=750>");
				for (Entry<Integer, Integer> i : currentEvent.getParticipants().entrySet()) {
					if (i == null) {
						continue;
					}

					Player pl = World.getInstance().getPlayer(i.getKey());
					if (pl == null) {
						continue;
					}

					if (i.getValue() == 2) {
						sb.append("<tr><td FIXWIDTH=200>" + pl.getName() +
								"</td><td FIXWIDTH=550><button action=\"bypass _bbscustom;action;gEvent;delPlayer;" + pl.getObjectId() +
								"\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td></tr>");
					}
				}
				sb.append("</table></td></tr>");

				sb.append("<tr><td><table width=850 bgcolor=999999 border=0><tr><td FIXWIDTH=750>Banned Players</td></tr></table></td></tr>");
				sb.append("<tr><td><table width=750>");
				for (Entry<String, String> i : currentEvent.getBannedIpsFromEvent().entrySet()) {
					if (i == null) {
						continue;
					}

					sb.append("<tr><td FIXWIDTH=200>" + i.getValue() +
							"</td><td FIXWIDTH=550><button action=\"bypass _bbscustom;action;gEvent;delBan;" + i.getKey() +
							"\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td></tr>");
				}
				sb.append("</table></td></tr>");

				sb.append("<tr><td><table width=850 bgcolor=999999 border=0><tr><td FIXWIDTH=750>Waiting Players</td></tr></table></td></tr>");
				sb.append("<tr><td>");
				List<Player> allPlayers = new ArrayList<>();
				for (PeaceZone zone : currentEvent.getPeaceZones()) {
					if (zone == null) {
						continue;
					}

					for (Player pl : zone.getPlayersInside()) {
						if (pl == null || allPlayers.contains(pl)) {
							continue;
						}
						allPlayers.add(pl);
					}
				}

				int maxWaitingPLayersPerPage = 10;
				int auctionsSize = allPlayers.size();
				int maxPages = auctionsSize / maxWaitingPLayersPerPage;
				if (auctionsSize > maxWaitingPLayersPerPage * maxPages) {
					maxPages++;
				}
				if (pageToShow > maxPages) {
					pageToShow = maxPages;
				}
				int pageStart = maxWaitingPLayersPerPage * pageToShow;
				int pageEnd = auctionsSize;
				if (pageEnd - pageStart > maxWaitingPLayersPerPage) {
					pageEnd = pageStart + maxWaitingPLayersPerPage;
				}

				if (maxPages > 1) {
					sb.append("<center>" + CustomCommunityBoard.getInstance().createPages(pageToShow, maxPages, "_bbscustom;gmEvent;", ";") +
							"</center>");
				}

				int x = 0;
				for (int i = pageStart; i < pageEnd; i++) {
					Player pl = allPlayers.get(i);
					if (pl == null) {
						continue;
					}

					int participatedTimes = currentEvent.getParticipatedTimes(pl.getObjectId(), pl.getExternalIP(), pl.getInternalIP());
					sb.append("<table width=750 " + (x % 2 == 1 ? "bgcolor=131210" : "") + "><tr><td FIXWIDTH=159>" + pl.getName() + " " +
							(participatedTimes > 0 ? "(" + participatedTimes + ")" : "") + "</td><td FIXWIDTH=159>" +
							PlayerClassTable.getInstance().getClassNameById(pl.getClassId()) + " (Lv. " + pl.getLevel() + ")</td><td FIXWIDTH=159>" +
							(pl.getClan() != null ? pl.getClan().getName() : "NoClan") +
							"</td><td FIXWIDTH=120><button value=\"Add to Team One\" width=120 height=20 action=\"bypass _bbscustom;action;gEvent;addPlayer;" +
							pl.getObjectId() +
							";1\" back=L2UI_CT1.Button_DF_Calculator_Over fore=L2UI_CT1.Button_DF_Calculator></button></td><td FIXWIDTH=120><button value=\"Add to Team Two\" width=120 height=20 action=\"bypass _bbscustom;action;gEvent;addPlayer;" +
							pl.getObjectId() +
							";2\" back=L2UI_CT1.Button_DF_Calculator_Over fore=L2UI_CT1.Button_DF_Calculator></button></td><td FIXWIDTH=16><button action=\"bypass _bbscustom;action;gEvent;kickPlayer;" +
							pl.getObjectId() +
							"\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td><td FIXWIDTH=16><button action=\"bypass _bbscustom;action;gEvent;banPlayer;" +
							pl.getObjectId() +
							"\" value=\" \" width=16 height=16 back=L2UI_CT1.SellingAgencyWnd_df_HelpBtn_down fore=L2UI_CT1.SellingAgencyWnd_df_HelpBtn_down></button></td></tr></table>");
					x++;
				}
				sb.append("</td></tr>");
			}
		} else {
			if (currentEvent == null || !currentEvent.isStarted()) {
				sb.append("<tr><td align=center><font color=LEVEL>There are no GM event right now!</font></td></tr>");
			} else {
				boolean isInsidePeaceZone = false;
				for (PeaceZone zone : currentEvent.getPeaceZones()) {
					if (zone == null) {
						continue;
					}

					if (zone.isCharacterInZone(player)) {
						isInsidePeaceZone = true;
						break;
					}
				}

				if (!isInsidePeaceZone) {
					sb.append("<tr><td align=center><table width=500 background=L2UI_CH3.refinewnd_back_Pattern>");
					sb.append("<tr><td FIXWIDTH=30>&nbsp;</td><td><br><br></td></tr>");
					sb.append("<tr><td FIXWIDTH=30>&nbsp;</td><td>Event Name:</td><td>" + currentEvent.getName() + "</td></tr>");
					sb.append("<tr><td FIXWIDTH=30>&nbsp;</td><td>Event Description:</td><td>" + currentEvent.getDescription() + "</td></tr>");
					sb.append("<tr><td FIXWIDTH=30>&nbsp;</td><td>Event Location:</td><td>" + currentEvent.getLocation() + "</td></tr>");
					sb.append("<tr><td FIXWIDTH=30>&nbsp;</td><td>Powered By:</td><td>" + currentEvent.getGMName() + "</td></tr>");
					sb.append("<tr><td FIXWIDTH=30>&nbsp;</td><td><br><br></td></tr>");
					sb.append("</table></td></tr>");

					sb.append("<tr><td align=center><table>");
					sb.append("<tr><td><button value=\"Take me to " + currentEvent.getLocation() +
							"!\" width=530 height=24 action=\"bypass _bbscustom;action;gEvent;teleToEvent;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button></td></tr>");
					sb.append("</table></td></tr>");
				} else {
					if (currentEvent.getAllowBets() && currentEvent.getCanBetNow() && !currentEvent.playerHasBet(player.getObjectId()) &&
							!currentEvent.isParticipant(player.getObjectId())) {
						String options = "";
						for (Entry<Integer, CurrencyInfo> b : currencies.entrySet()) {
							options += b.getValue().getName() + ";";
						}

						sb.append("<tr><td align=center><table width=500 border=0>");
						sb.append("<tr><td>Select your bet currency:</td><td><combobox width=100 height=17 var=\"bCoin\" list=" + options +
								"></td></tr>");
						sb.append("<tr><td FIXWIDTH=100>Introduce your bet:</td><td><edit var=\"bet\" type=number width=100 length=25></td></tr>");
						sb.append(
								"<tr><td>Select the team you want to bet for:</td><td><combobox width=100 height=17 var=\"betTeam\" list=blue;red></td></tr>");
						sb.append(
								"</table></td></tr><tr><td align=center><table><tr><td><button value=\"Bet!\" width=500 height=24 action=\"bypass _bbscustom;action;gEvent;doBet; $bCoin ; $bet ; $betTeam ;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button></td></tr></table></td></tr>");
					} else {
						sb.append("<tr><td align=center><font color=LEVEL>You already did a bet or bets are already closed!</font></td></tr>");
					}
				}
			}
		}

		sb.append("</table>");
		return sb.toString();
	}

	private int getCurrencyId(String coinName) {
		for (Entry<Integer, CurrencyInfo> i : currencies.entrySet()) {
			if (i.getValue().getName().equalsIgnoreCase(coinName)) {
				return i.getValue().getId();
			}
		}
		return 57;
	}

	private class CurrencyInfo {
		private String name;
		private int id;

		private CurrencyInfo(int id) {
			this.id = id;
			name = ItemTable.getInstance().getTemplate(id).getName().replace(" ", "");
		}

		private String getName() {
			return name;
		}

		private int getId() {
			return id;
		}
	}

	private class SubEvent {
		@SuppressWarnings("unused")
		private String eventName;
		private String startBypass;
		private String endBypass;

		private SubEvent(String eventName, String startBypass, String endBypass) {
			this.eventName = eventName;
			this.startBypass = startBypass;
			this.endBypass = endBypass;
		}

		private String getStartBypass() {
			return startBypass;
		}

		@SuppressWarnings("unused")
		private String getEndBypass() {
			return endBypass;
		}
	}

	private class Bets {
		private int playerId;
		private int itemId;
		private Long betAmount;
		private int teamId;

		private Bets(int playerId, int itemId, long betAmount, int teamId) {
			this.playerId = playerId;
			this.itemId = itemId;
			this.betAmount = betAmount;
			this.teamId = teamId;
		}

		private int getPlayerId() {
			return playerId;
		}

		private int getItemId() {
			return itemId;
		}

		private Long getBetAmount() {
			return betAmount;
		}

		private int getTeamId() {
			return teamId;
		}
	}

	public void handleEventCommand(Player player, String command) {
		if (player == null) {
			return;
		}

		StringTokenizer st = new StringTokenizer(command, ";");
		st.nextToken();
		st.nextToken();
		st.nextToken();

		if (player.isGM()) {
			switch (String.valueOf(st.nextToken())) {
				case "loadEvent":
					currentEvent = new Event(predefinedEvents.get(st.nextToken().trim()));
					break;
				case "reduce": {
					int playerId = Integer.valueOf(st.nextToken());
					player.sendMessage("- 10 to " + playerId);
					Ranked1v1.getInstance().reduce(playerId);

					break;
				}
				case "startSubEvent": {
					String eventName = st.nextToken().trim();
					Quest subEvent = QuestManager.getInstance().getQuest(eventName);
					if (subEvent != null) {
						subEvent.notifyEvent(subEvents.get(eventName).getStartBypass(), null, null);
					}
					break;
				}

				case "setName":
					if (currentEvent == null)//new event design
					{
						currentEvent = new Event(st.nextToken());
					} else {
						currentEvent.setName(st.nextToken());
					}
					break;

				case "setDesc":
					currentEvent.setDescription(st.nextToken());
					break;

				case "setLoc":
					currentEvent.setLocation(st.nextToken());
					break;

				case "setPeace":
					PeaceZone peace = ZoneManager.getInstance().getZone(player, PeaceZone.class);
					if (peace != null) {
						currentEvent.setPeaceZone(peace);
					}
					break;

				case "setArena":
					ArenaZone arena = ZoneManager.getInstance().getArena(player);
					if (arena != null) {
						currentEvent.setArenaZone(arena);
					}
					break;

				case "setTeamOneSpawn":
					currentEvent.setTeamOneSpawn(new Location(player.getX(), player.getY(), player.getZ() + 10));
					break;

				case "setTeamTwoSpawn":
					currentEvent.setTeamTwoSpawn(new Location(player.getX(), player.getY(), player.getZ() + 10));
					break;

				case "setSpawnBufferOne":
					currentEvent.setSpawnBufferOne(new Location(player.getX(), player.getY(), player.getZ() + 10, player.getHeading()));
					break;

				case "setSpawnBufferTwo":
					currentEvent.setSpawnBufferTwo(new Location(player.getX(), player.getY(), player.getZ() + 10, player.getHeading()));
					break;

				case "setSpawnLoc":
					currentEvent.setTeleportLocation(new Location(player.getX(), player.getY(), player.getZ() + 10));
					break;

				case "setArenaSign":
					StaticObjectInstance arenaSign = null;
					WorldObject target = player.getTarget();
					if (target != null && target instanceof StaticObjectInstance) {
						arenaSign = (StaticObjectInstance) target;
					}

					int staticId = 0;
					if (arenaSign != null) {
						staticId = arenaSign.getStaticObjectId();
						currentEvent.addArenaSignNpc(staticId);
					}
					break;

				case "delArenaSign":
					currentEvent.addArenaSignNpc(0);
					break;

				case "addSignSpawn":
					currentEvent.addArenaSignSpawn(new Location(player.getX(), player.getY(), player.getZ() + 10));
					break;

				case "addDoor":
					currentEvent.addDoor(((DoorInstance) player.getTarget()).getDoorId());
					break;

				case "delDoor":
					currentEvent.addDoor(0);
					break;

				case "delSignSpawn":
					currentEvent.addArenaSignSpawn(null);
					break;

				case "delName":
					currentEvent.setName(null);
					break;

				case "delDesc":
					currentEvent.setDescription(null);
					break;

				case "delLoc":
					currentEvent.setLocation(null);
					break;

				case "delPeace":
					currentEvent.setPeaceZone(null);
					break;

				case "delArena":
					currentEvent.setArenaZone(null);
					break;

				case "delTeamOneSpawn":
					currentEvent.setTeamOneSpawn(null);
					break;

				case "delTeamTwoSpawn":
					currentEvent.setTeamTwoSpawn(null);
					break;

				case "delSpawnBufferOne":
					currentEvent.setSpawnBufferOne(null);
					break;

				case "delSpawnBufferTwo":
					currentEvent.setSpawnBufferTwo(null);
					break;

				case "delSpawnLoc":
					currentEvent.setTeleportLocation(null);
					break;

				case "startEvent":
					currentEvent.startEvent(player.getName());
					break;

				case "restartConfig":
					currentEvent = null;
					break;

				case "startFight":
					currentEvent.startFight();
					break;

				case "stopFight":
					currentEvent.stopFight();
					break;

				case "restartFight":
					currentEvent.restartFight();
					break;

				case "stopEvent":
					ArrayList<Integer> copyParticipants = new ArrayList<>(currentEvent.getParticipants().keySet());
					for (int i : copyParticipants) {
						currentEvent.removeParticipant(i);
					}

					currentEvent.deleteArenaSigns();
					currentEvent.removeFences();

					currentEvent = null;
					Announcements.getInstance().announceToAll("The GM Event has ended, thanks everyone for participate!");
					break;

				case "addPlayer": {
					int playerId = Integer.valueOf(st.nextToken());
					int teamId = Integer.valueOf(st.nextToken());

					currentEvent.addParticipant(playerId, teamId);
					break;
				}
				case "delPlayer": {
					int playerId = Integer.valueOf(st.nextToken());

					currentEvent.removeParticipant(playerId);
					break;
				}
				case "kickPlayer": {
					int playerId = Integer.valueOf(st.nextToken());
					currentEvent.kickPlayer(playerId);
					break;
				}
				case "banPlayer": {
					int playerId = Integer.valueOf(st.nextToken());
					currentEvent.addBannedIp(playerId);
					break;
				}
				case "delBan": {
					String ip = st.nextToken();
					currentEvent.removeBannedIp(ip);
					break;
				}

				case "reAnnounce":
					currentEvent.reAnnounce();
					break;

				case "manageDoors":
					for (int i : currentEvent.getDoors()) {
						DoorInstance door = DoorTable.getInstance().getDoor(i);
						if (door == null) {
							continue;
						}

						if (door.getOpen()) {
							door.closeMe();
						} else {
							door.openMe();
						}
					}
					break;

				case "addFences":
					currentEvent.addFeances();
					break;

				case "delFences":
					currentEvent.removeFences();
					break;

				case "addBets":
					currentEvent.setAllowBets(true);
					break;

				case "delBets":
					currentEvent.setAllowBets(false);
					break;

				case "returnBets":
					currentEvent.returnBets();
					break;

				case "giveBetRewards":
					currentEvent.giveBetRewards(Integer.valueOf(st.nextToken()));
					break;

				case "giveReward":
					currentEvent.giveReward(Integer.valueOf(st.nextToken()), Integer.valueOf(st.nextToken().trim()));
					break;
			}
			CustomCommunityBoard.getInstance().parseCmd("_bbscustom;gmEvent;0", player);
		} else {
			switch (String.valueOf(st.nextToken())) {
				case "teleToEvent":
					if (currentEvent != null && currentEvent.isStarted() && teleportConditions(player)) {
						player.teleToLocation(currentEvent.getTeleportLocation(), true);
					}
					break;

				case "acceptRules":
					if (currentEvent != null && currentEvent.isStarted()) {
						currentEvent.acceptRules(player.getObjectId());
						player.sendPacket(new ExShowScreenMessage("You're now allowed to enter! ", 3000));
					}
					break;

				case "getBuff":
					if (player.getIsInsideGMEvent() && currentEvent != null && currentEvent.isStarted() &&
							currentEvent.isParticipant(player.getObjectId())) {
						if (currentEvent.canUseMoreBuffs(player.getObjectId())) {
							currentEvent.addUsedBuff(player.getObjectId());

							Skill buff = SkillTable.getInstance().getInfo(Integer.valueOf(st.nextToken()), 1);
							if (buff != null) {
								buff.getEffects(player, player);
							}
						}
					}
					break;

				case "doBet":
					if (currentEvent == null || !currentEvent.getCanBetNow() || !currentEvent.getAllowBets() ||
							currentEvent.playerHasBet(player.getObjectId()) && !currentEvent.isParticipant(player.getObjectId())) {
						return;
					}

					int teamId = 0;
					int itemId = 0;
					long betAmount = 0;

					if (st.hasMoreTokens()) {
						itemId = getCurrencyId(st.nextToken().trim());
					}

					if (currencies.get(itemId) == null) {
						return; //client hack
					}

					if (st.hasMoreTokens()) {
						betAmount = Long.valueOf(st.nextToken().trim());
					}

					if (st.hasMoreTokens()) {
						if (st.nextToken().trim().equalsIgnoreCase("blue")) {
							teamId = 1;
						} else {
							teamId = 2;
						}
					}

					if (teamId == 0 || betAmount == 0 || itemId == 0) {
						return;
					}

					if (!player.getClient().getFloodProtectors().getTransaction().tryPerformAction("buy")) {
						return;
					}

					if (!player.destroyItemByItemId("GM Event", itemId, betAmount, null, true)) {
						return;
					}

					currentEvent.addBet(player.getObjectId(), itemId, betAmount, teamId);

					CustomCommunityBoard.getInstance().parseCmd("_bbscustom;gmEvent;0", player);
					break;
			}
		}
	}

	private boolean teleportConditions(Player player) {
		if (player == null) {
			return false;
		}

		return !(player.getLevel() < 99 || player.isInCombat() || player.getPvpFlag() > 0 || player.getInstanceId() != 0 || player.isInDuel() ||
				player.isFakeDeath() || player.isOutOfControl() || player.isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(player) ||
				AttackStanceTaskManager.getInstance().getAttackStanceTask(player) || player.getIsInsideGMEvent() || player.getEvent() != null);
	}

	private class ParticipateRegister {
		private int playerId;
		private String externalIp;
		private String internalIp;
		private int participatedTimes;

		private ParticipateRegister(int playerId, String externalIp, String internalIp) {
			this.playerId = playerId;
			this.externalIp = externalIp;
			this.internalIp = internalIp;
			participatedTimes = 1;
		}

		@SuppressWarnings("unused")
		private int getPlayerId() {
			return playerId;
		}

		private boolean matchIp(String externalIp, String internalIp) {
			return externalIp.equalsIgnoreCase(externalIp) && internalIp.equalsIgnoreCase(internalIp);
		}

		private int getParticipatedTimes() {
			return participatedTimes;
		}

		private void increaseParticipatedTimes() {
			participatedTimes++;
		}
	}

	private class Event {
		private String gmName;
		private String eventName;
		private String description;
		private String location;
		private List<Integer> doors;
		private List<ArenaZone> arenaZones;
		private List<Npc> buffers;
		private List<Npc> arenaSigns;
		private List<Integer> rewardedPlayers;
		private List<PeaceZone> peaceZones;
		private List<Integer> arenaSignIds;
		private List<Location> arenaSignSpawns;
		private List<ColosseumFence> areaFences;
		private Map<Integer, Bets> bets;
		private Map<Integer, Integer> usedBuffs;
		private Map<Integer, Integer> participants;
		private Map<String, String> bannedIpsFromTheEvent;
		private Map<Integer, ParticipateRegister> participatedRegister;
		private Map<Integer, Boolean> acceptedEventRules;
		private Location eventTeleport;
		private Location spawnOne;
		private Location spawnTwo;
		private Location bufferSpawnOne;
		private Location bufferSpawnTwo;
		private boolean isStarted;
		private boolean isFightStarted;
		private boolean allowBets; //The gm can turn it on/off at each combat
		private boolean canBetNow;

		private Event(String eventName) {
			this.eventName = eventName;
			doors = new ArrayList<>();
			arenaZones = new ArrayList<>();
			peaceZones = new ArrayList<>();
			participants = new HashMap<>();
			areaFences = new ArrayList<>();
			bannedIpsFromTheEvent = new HashMap<>();
			arenaSignSpawns = new ArrayList<>();
			arenaSignIds = new ArrayList<>();
			acceptedEventRules = new HashMap<>();
			participatedRegister = new HashMap<>();
			usedBuffs = new HashMap<>();
			buffers = new ArrayList<>();
			arenaSigns = new ArrayList<>();
			rewardedPlayers = new ArrayList<>();
			bets = new HashMap<>();
			allowBets = false;
		}

		private Event(String eventName,
		              String description,
		              String locationName,
		              List<Integer> doors,
		              List<ArenaZone> arenaZones,
		              List<PeaceZone> peaceZones,
		              Location teamOneSpawn,
		              Location teamTwoSpawn,
		              Location bufferTeamOneSpawn,
		              Location bufferTeamTwoSpawn,
		              Location eventSpawn,
		              List<Location> arenaSignSpawns,
		              List<Integer> arenaSigns) {
			this.eventName = eventName;
			this.description = description;
			location = locationName;
			this.doors = doors;
			this.arenaZones = arenaZones;
			this.peaceZones = peaceZones;
			spawnOne = teamOneSpawn;
			spawnTwo = teamTwoSpawn;
			bufferSpawnOne = bufferTeamOneSpawn;
			bufferSpawnTwo = bufferTeamTwoSpawn;
			eventTeleport = eventSpawn;
			this.arenaSignSpawns = arenaSignSpawns;
			arenaSignIds = arenaSigns;
			participants = new HashMap<>();
			areaFences = new ArrayList<>();
			bannedIpsFromTheEvent = new HashMap<>();
			acceptedEventRules = new HashMap<>();
			participatedRegister = new HashMap<>();
			usedBuffs = new HashMap<>();
			buffers = new ArrayList<>();
			arenaSigns = new ArrayList<>();
			rewardedPlayers = new ArrayList<>();
			bets = new HashMap<>();
			allowBets = false;
		}

		private Event(Event event) {
			eventName = event.getName();
			description = event.getDescription();
			location = event.getLocation();
			doors = event.getDoors();
			arenaZones = event.getArenaZones();
			peaceZones = event.getPeaceZones();
			spawnOne = event.getTeamOneSpawn();
			spawnTwo = event.getTeamTwoSpawn();
			bufferSpawnOne = event.getSpawnBufferOne();
			bufferSpawnTwo = event.getSpawnBufferTwo();
			eventTeleport = event.getTeleportLocation();
			arenaSignSpawns = event.getArenaSignSpawns();
			arenaSignIds = event.getArenaSignIds();
			arenaSignSpawns = event.getArenaSignSpawns();
			participants = new HashMap<>();
			areaFences = new ArrayList<>();
			bannedIpsFromTheEvent = new HashMap<>();
			acceptedEventRules = new HashMap<>();
			participatedRegister = new HashMap<>();
			usedBuffs = new HashMap<>();
			buffers = new ArrayList<>();
			arenaSigns = new ArrayList<>();
			rewardedPlayers = new ArrayList<>();
			bets = new HashMap<>();
			allowBets = false;
		}

		private String getName() {
			return eventName;
		}

		private void setName(String n) {
			eventName = n;
		}

		private String getGMName() {
			return gmName;
		}

		private void setDescription(String d) {
			description = d;
		}

		private String getDescription() {
			return description;
		}

		private void setLocation(String b) {
			location = b;
		}

		private String getLocation() {
			return location;
		}

		private boolean isStarted() {
			return isStarted;
		}

		private boolean isFightStarted() {
			return isFightStarted;
		}

		private List<ColosseumFence> getFences() {
			return areaFences;
		}

		private List<Integer> getDoors() {
			return doors;
		}

		private List<ArenaZone> getArenaZones() {
			return arenaZones;
		}

		private List<PeaceZone> getPeaceZones() {
			return peaceZones;
		}

		private void setArenaZone(ArenaZone z) {
			if (z == null) {
				arenaZones.clear();
			} else if (!arenaZones.contains(z)) {
				arenaZones.add(z);
			}
		}

		private void setPeaceZone(PeaceZone z) {
			if (z == null) {
				peaceZones.clear();
			} else if (!peaceZones.contains(z)) {
				peaceZones.add(z);
			}
		}

		private void setTeleportLocation(Location l) {
			eventTeleport = l;
		}

		private Location getTeleportLocation() {
			return eventTeleport;
		}

		private void setTeamOneSpawn(Location loc) {
			spawnOne = loc;
		}

		private void setTeamTwoSpawn(Location loc) {
			spawnTwo = loc;
		}

		private boolean isParticipant(int playerId) {
			return participants.containsKey(playerId);
		}

		private Map<Integer, Integer> getParticipants() {
			return participants;
		}

		private void setSpawnBufferOne(Location l) {
			bufferSpawnOne = l;
		}

		private void setSpawnBufferTwo(Location l) {
			bufferSpawnTwo = l;
		}

		private Location getSpawnBufferOne() {
			return bufferSpawnOne;
		}

		private Location getSpawnBufferTwo() {
			return bufferSpawnTwo;
		}

		private boolean canUseMoreBuffs(int playerId) {
			return !usedBuffs.containsKey(playerId) || usedBuffs.get(playerId) < 5;
		}

		private void addUsedBuff(int playerId) {
			if (usedBuffs.containsKey(playerId)) {
				usedBuffs.put(playerId, usedBuffs.get(playerId) + 1);
			} else {
				usedBuffs.put(playerId, 1);
			}
		}

		private Location getTeamOneSpawn() {
			return spawnOne;
		}

		private Location getTeamTwoSpawn() {
			return spawnTwo;
		}

		private void addDoor(int door) {
			if (door == 0) {
				doors.clear();
			} else {
				doors.add(door);
			}
		}

		private void addArenaSignSpawn(Location loc) {
			if (loc == null) {
				arenaSignSpawns.clear();
			} else {
				arenaSignSpawns.add(loc);
			}
		}

		private List<Location> getArenaSignSpawns() {
			return arenaSignSpawns;
		}

		private List<Integer> getArenaSignIds() {
			return arenaSignIds;
		}

		private void addArenaSignNpc(int id) {
			if (id == 0) {
				arenaSignIds.clear();
			} else {
				arenaSignIds.add(id);
			}
		}

		private int getParticipatedTimes(int playerId, String externalIp, String internalIp) {
			int times = 0;
			for (Entry<Integer, ParticipateRegister> reg : participatedRegister.entrySet()) {
				ParticipateRegister i = reg.getValue();
				if (i == null) {
					continue;
				}

				if (i.matchIp(externalIp, internalIp)) {
					times += i.getParticipatedTimes();
				}
			}
			return times;
		}

		private Map<String, String> getBannedIpsFromEvent() {
			return bannedIpsFromTheEvent;
		}

		private boolean isBannedIp(String eIp) {
			return bannedIpsFromTheEvent.containsKey(eIp);
		}

		private void addBannedIp(int playerId) {
			Player player = World.getInstance().getPlayer(playerId);
			if (player == null) {
				return;
			}

			bannedIpsFromTheEvent.put(player.getExternalIP(), player.getName());

			kickPlayer(playerId);

			//Revalidate the zone to kick dualbox
			for (PeaceZone zone : peaceZones) {
				if (zone == null) {
					continue;
				}

				for (Player pl : zone.getPlayersInside()) {
					if (pl == null) {
						continue;
					}
					onEnterZone(pl, zone);
				}
			}
		}

		private void removeBannedIp(String b) {
			bannedIpsFromTheEvent.remove(b);
		}

		private boolean hasBets() {
			return !bets.isEmpty();
		}

		private void setCanBetNow(boolean b) {
			canBetNow = b;

			if (allowBets) {
				sendPacketToWaitingPlayers(new CreatureSay(1, 15, "", "The bets are now " + (canBetNow ? "open" : "closed") + "! :"));
			}
		}

		private boolean getCanBetNow() {
			return canBetNow;
		}

		private void setAllowBets(boolean b) {
			allowBets = b;
		}

		private boolean getAllowBets() {
			return allowBets;
		}

		private void addBet(int playerId, int itemId, long betAmount, int teamId) {
			synchronized (bets) {
				bets.put(playerId, new Bets(playerId, itemId, betAmount, teamId));

				String charName = CharNameTable.getInstance().getNameById(playerId);
				Util.logToFile(charName + " did a bet " + currencies.get(itemId).getName() + "(" + betAmount + ") for the team " +
						(teamId == 1 ? "blue" : "red"), "GMEvents", true);
			}
		}

		private boolean playerHasBet(int playerId) {
			return bets.containsKey(playerId);
		}

		private void giveBetRewards(int winnerTeam) {
			for (Entry<Integer, CurrencyInfo> currency : currencies.entrySet()) {
				CurrencyInfo coin = currency.getValue();
				if (coin == null) {
					continue;
				}

				int winnerTeamSize = 0;
				for (Entry<Integer, Integer> i : participants.entrySet()) {
					if (i.getValue() == winnerTeam) {
						winnerTeamSize += 1;
					}
				}

				long totalBet = 0;
				long fighterTotalBet = 0;
				for (Entry<Integer, Bets> bets : bets.entrySet()) {
					Bets bet = bets.getValue();
					if (bet == null) {
						continue;
					}

					if (bet.getTeamId() == winnerTeam && bet.getItemId() == coin.getId()) {
						fighterTotalBet += bet.getBetAmount();
					}

					if (bet.getItemId() == coin.getId()) {
						totalBet += bet.getBetAmount();
					}
				}

				//There are no bets with this coin..
				if (totalBet == 0) {
					continue;
				}

				//Reward the Fighter players
				long winnerBetReward = Math.round(totalBet * 0.1f / winnerTeamSize);
				for (Entry<Integer, Integer> i : participants.entrySet()) {
					if (i.getValue() == winnerTeam) {
						sendRewardMail(i.getKey(), coin.getId(), winnerBetReward, "Congratulations, this is your bet percentage!");
					}
				}

				//Reward the winner waiting players
				for (Entry<Integer, Bets> bets : bets.entrySet()) {
					Bets bet = bets.getValue();
					if (bet == null) {
						continue;
					}

					if (bet.getTeamId() == winnerTeam && bet.getItemId() == coin.getId()) {
						long reward = Math.round(totalBet * 1.2f * bet.getBetAmount() / fighterTotalBet);
						sendRewardMail(bet.getPlayerId(), coin.getId(), reward, "Congratulations, your bet has been successful!");
					}
				}
			}
			bets.clear();
		}

		private void reAnnounce() {
			Announcements.getInstance().announceToAll(eventName + " at " + location + " join with ALT + B > GM EVENT!");
		}

		private void returnBets() {
			canBetNow = false;

			for (Entry<Integer, Bets> bets : bets.entrySet()) {
				Bets bet = bets.getValue();
				if (bet == null) {
					continue;
				}

				sendRewardMail(bet.getPlayerId(), bet.getItemId(), bet.getBetAmount(), "Bet Refound!");
			}
			bets.clear();
		}

		private boolean hasAcceptedRules(int playerId) {
			return acceptedEventRules.containsKey(playerId) && acceptedEventRules.get(playerId);
		}

		private void acceptRules(int charId) {
			acceptedEventRules.put(charId, true);
		}

		private void addFeances() {
			if (!arenaZones.isEmpty() && arenaZones.size() == 1) {
				ColosseumFence feance = addDynamic(arenaZones.get(0).getZone().getCenterX(),
						arenaZones.get(0).getZone().getCenterY(),
						-3775,
						-3775 + 100,
						-3775 - 100,
						1100,
						1100);
				if (feance != null) {
					areaFences.add(feance);
				}
			}
		}

		private void removeFences() {
			for (ColosseumFence fence : areaFences) {
				if (fence == null) {
					continue;
				}

				fence.decayMe();
				fence.getKnownList().removeAllKnownObjects();
			}
			areaFences.clear();
		}

		private void startFight() {
			isFightStarted = true;

			spawnBuffers();

			setCanBetNow(true);

			sendPacketToWaitingPlayers(new ExShowScreenMessage("The Fight will start in 40 seconds!", 5000));
			sendPacketToFighterPlayers(new ExShowScreenMessage("Take buffs! The Fight will start in 40 seconds!", 5000));

			ThreadPoolManager.getInstance().scheduleGeneral(() -> {
				setCanBetNow(false);

				deleteBuffers();

				for (Entry<Integer, Integer> i : currentEvent.getParticipants().entrySet()) {
					Player player = World.getInstance().getPlayer(i.getKey());
					if (player != null) {
						player.heal();
					}
				}

				sendPacketToWaitingPlayers(new ExShowScreenMessage("The Fight has started!", 2000));
				sendPacketToFighterPlayers(new ExShowScreenMessage("Start the Fight! GO! GO! GO!", 5000));
			}, 40000);
		}

		private void spawnBuffers() {
			Npc bufferOne = NpcUtil.addSpawn(bufferNpcId,
					bufferSpawnOne.getX(),
					bufferSpawnOne.getY(),
					bufferSpawnOne.getZ(),
					bufferSpawnOne.getHeading(),
					false,
					0,
					false,
					0);
			buffers.add(bufferOne);

			Npc bufferTwo = NpcUtil.addSpawn(bufferNpcId,
					bufferSpawnTwo.getX(),
					bufferSpawnTwo.getY(),
					bufferSpawnTwo.getZ(),
					bufferSpawnTwo.getHeading(),
					false,
					0,
					false,
					0);
			buffers.add(bufferTwo);
		}

		private void spawnArenaSigns() {
			if (arenaSignIds.isEmpty()) {
				for (Location loc : arenaSignSpawns) {
					Npc npc = NpcUtil.addSpawn(dummyArenaSignNpcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), false, 0, false, 0);
					arenaSigns.add(npc);
				}
			}
		}

		private void deleteArenaSigns() {
			if (arenaSignIds.isEmpty()) {
				for (Npc arenaSign : arenaSigns) {
					arenaSign.deleteMe();
				}
			}
		}

		private void deleteBuffers() {
			for (Npc buffer : buffers) {
				buffer.deleteMe();
			}
		}

		private void sendPacketToFighterPlayers(L2GameServerPacket p) {
			for (ArenaZone zone : arenaZones) {
				if (zone == null) {
					continue;
				}
				zone.broadcastPacket(p);
			}
		}

		private void kickPlayersFromPeaceZones() {
			for (PeaceZone zone : peaceZones) {
				if (zone == null) {
					continue;
				}
				zone.oustAllPlayers();
			}
		}

		private void kickPlayersFromArenaZones() {
			for (ArenaZone zone : arenaZones) {
				if (zone == null) {
					continue;
				}
				zone.oustAllPlayers();
			}
		}

		private void sendPacketToWaitingPlayers(L2GameServerPacket p) {
			for (PeaceZone zone : peaceZones) {
				if (zone == null) {
					continue;
				}
				zone.broadcastPacket(p);
			}
		}

		private void stopFight() {
			isFightStarted = false;

			ArrayList<Integer> copyParticipants = new ArrayList<>(currentEvent.getParticipants().keySet());
			for (int i : copyParticipants) {
				removeParticipant(i);
			}

			usedBuffs.clear();
			rewardedPlayers.clear();
		}

		private void restartFight() {
			isFightStarted = false;

			for (Entry<Integer, Integer> i : participants.entrySet()) {
				Player player = World.getInstance().getPlayer(i.getKey());
				if (player == null) {
					continue;
				}

				if (player.isDead()) {
					player.doRevive();
				}

				//Heal
				player.heal();

				//Reuse
				player.removeSkillReuse(true);

				//Spawn
				if (i.getValue() == 1) {
					player.teleToLocation(spawnOne, false);
				} else {
					player.teleToLocation(spawnTwo, false);
				}

				player.broadcastUserInfo();
			}

			usedBuffs.clear();
			rewardedPlayers.clear();
		}

		private void startEvent(String gmName) {
			isStarted = true;

			this.gmName = gmName;

			Announcements.getInstance()
					.announceToAll("GM Event: " + eventName + " has started (Giran Arena), check the community board for more information!");

			//Kick already-inside players
			kickPlayersFromPeaceZones();
			kickPlayersFromArenaZones();

			//If the aren't arenaSigns by default we will spawn our ones
			spawnArenaSigns();
		}

		private void addParticipant(int playerId, int team) {
			Player player = World.getInstance().getPlayer(playerId);
			if (player == null) {
				return;
			}

			if (player.isInParty()) {
				player.leaveParty();
			}

			if (player.isSitting()) {
				player.standUp();
			}

			player.stopAllEffectsExceptThoseThatLastThroughDeath();
			player.removeSkillReuse(true);
			player.setTeam(team);
			player.setIsInsideGMEvent(true);
			player.teleToLocation(team == 1 ? spawnOne : spawnTwo, false);

			participants.put(playerId, team);

			sendPacketToWaitingPlayers(new ExShowScreenMessage(player.getName() + " joined the " + (team == 1 ? "blue" : "red") + " side!", 2000));

			player.sendPacket(new ExShowScreenMessage("Please don't do any action, just wait!", 5000));
		}

		private void removeParticipant(int playerId) {
			if (isParticipant(playerId)) {
				participants.remove(playerId);

				Player player = World.getInstance().getPlayer(playerId);
				if (player != null) {
					if (player.isDead()) {
						player.doRevive();
					}

					player.heal();

					player.setTeam(0);
					player.setIsInsideGMEvent(false);
					player.teleToLocation(eventTeleport, true);

					ParticipateRegister register = participatedRegister.get(playerId);
					if (register != null) {
						register.increaseParticipatedTimes();
					} else {
						participatedRegister.put(playerId, new ParticipateRegister(playerId, player.getExternalIP(), player.getInternalIP()));
					}
				}
			}
		}

		private void kickPlayer(int playerId) {
			if (hasAcceptedRules(playerId)) {
				acceptedEventRules.remove(playerId);
			}

			Player toKick = World.getInstance().getPlayer(playerId);
			if (toKick != null) {
				toKick.teleToLocation(TeleportWhereType.Town);
			}
		}

		private void giveReward(int team, int count) {
			synchronized (rewardedPlayers) {
				for (Entry<Integer, Integer> i : participants.entrySet()) {
					if (i.getValue() != team) {
						continue;
					}

					Player player = World.getInstance().getPlayer(i.getKey());
					if (player == null) {
						continue;
					}

					if (rewardedPlayers.contains(player.getObjectId())) {
						continue;
					}

					rewardedPlayers.add(player.getObjectId());

					player.addItem("GMEvent", rewardCoinId, count, player, true);

					GmListTable.broadcastMessageToGMs("GMEvent: " + player.getName() + " has been rewarded with " + count + " Apigas!");
					Util.logToFile(gmName + ": " + player.getName() + " has been rewarded with " + count + " Apigas!", "GMEvents", true);
				}
			}
		}
	}

	private void sendRewardMail(int playerId, int itemId, long amount, String message) {
		Message msg = new Message(-1, playerId, false, "GMEvent", message, 0);

		Mail attachments = msg.createAttachments();
		attachments.addItem("GMEvent", itemId, amount, null, null);

		MailManager.getInstance().sendMessage(msg);

		String playerName = CharNameTable.getInstance().getNameById(playerId);
		if (message.contains("Refound")) {
			Util.logToFile("GMEvent Bet: " + playerName + " get his bet refounded " + amount + " " + currencies.get(itemId).getName() + "",
					"GMEvents",
					true);
		} else {
			Util.logToFile("GMEvent Bet: " + playerName + " has been rewarded with " + amount + " " + currencies.get(itemId).getName() + "",
					"GMEvents",
					true);
		}
	}

	private ColosseumFence addDynamic(int x, int y, int z, int minZ, int maxZ, int width, int height) {
		ColosseumFence fence = new ColosseumFence(0, x, y, z, minZ, maxZ, width, height, FenceState.CLOSED);
		fence.spawnMe();
		return fence;
	}

	public void onKill(Creature killer, Creature killed) {
		if (killer == null || killed == null || currentEvent == null) {
			return;
		}

		if (currentEvent.isStarted() && currentEvent.isFightStarted()) {
			if (killer.getActingPlayer() != null && killed.getActingPlayer() != null) {
				currentEvent.sendPacketToWaitingPlayers(new ExShowScreenMessage(
						killed.getActingPlayer().getName() + " has been killed by " + killer.getActingPlayer().getName() + "!", 2000));

				//Winner animation only if is 1vs1
				if (currentEvent.getParticipants().size() == 2) {
					killer.broadcastPacket(new SocialAction(killer.getActingPlayer().getObjectId(), 22));
				}
			}
		}
	}

	//Lets auto control who enter to the zone instead of kick every one who illegally enter, also banned ppl
	public boolean onEnterZone(Creature character, ZoneType zone) {
		if (character == null) {
			return true;
		}

		Player player = character.getActingPlayer();
		if (player == null) {
			return true;
		}

		if (currentEvent != null && currentEvent.isStarted() && !player.isGM()) {
			if (zone instanceof ArenaZone && currentEvent.getArenaZones().contains(zone)) {
				if (!player.getIsInsideGMEvent()) {
					currentEvent.kickPlayer(character.getObjectId());
					return false;
				}
			} else if (zone instanceof PeaceZone && currentEvent.getPeaceZones().contains(zone)) {
				if (currentEvent.isBannedIp(player.getExternalIP())) {
					player.sendPacket(new ExShowScreenMessage(" ", 5000));
					currentEvent.kickPlayer(character.getObjectId());
					return false;
				}

				if (!currentEvent.hasAcceptedRules(player.getObjectId())) {
					player.sendPacket(new ExShowScreenMessage("You should accept the Event Rule (on the Arena Sign) before enter to the zone! ",
							5000));
					player.teleToLocation(currentEvent.getTeleportLocation(), true);
					return false;
				}
			}
		}

		return true;
	}

	public void onNpcTalk(WorldObject obj, Player player) {
		if (obj == null || player == null || currentEvent == null) {
			return;
		}

		if (currentEvent.isStarted()) {
			if (obj instanceof StaticObjectInstance &&
					currentEvent.getArenaSignIds().contains(((StaticObjectInstance) obj).getStaticObjectId()) ||
					obj instanceof NpcInstance && ((NpcInstance) obj).getNpcId() == dummyArenaSignNpcId) {
				CustomCommunityBoard.getInstance().parseCmd("bypass _bbscustom;info;gmEventRules", player);
			}
		}
	}

	public boolean canAttack(Player attacker, WorldObject target) {
		if (attacker == null || target == null) {
			return false;
		}

		Player tgt = null;
		if (target instanceof Summon) {
			tgt = ((Summon) target).getOwner();
		} else if (target instanceof Player) {
			tgt = (Player) target;
		}

		if (attacker == tgt) {
			return true;
		}

		if (tgt != null) {
			return tgt.getTeam() != attacker.getTeam();
		}

		return false;
	}

	@Load(dependencies = {ItemTable.class, ZoneManager.class})
	public void loadPredefinedEvents() {
		File file = new File(Config.DATAPACK_ROOT, "data_" + Config.SERVER_NAME + "/predefinedGMEvents.xml");
		if (!file.exists()) {
			return;
		}

		XmlDocument doc = new XmlDocument(file);
		for (XmlNode d : doc.getChildren()) {
			if (d.getName().equalsIgnoreCase("currency")) {
				int itemId = d.getInt("itemId");
				currencies.put(itemId, new CurrencyInfo(itemId));
			} else if (d.getName().equalsIgnoreCase("event")) {
				String name = d.getString("name");
				String description = d.getString("description");
				String locationName = d.getString("location");
				String[] doors = d.hasAttribute("doors") ? d.getString("doors").split(",") : null;
				String[] arenaZoneNames = d.getString("arenaZones").split(",");
				String[] peaceZoneNames = d.getString("peaceZones").split(",");
				String[] spawnTeamOneCords = d.getString("spawnTeamOne").split(",");
				String[] spawnTeamTwoCords = d.getString("spawnTeamTwo").split(",");
				String[] spawnBufferTeamOneCords = d.getString("spawnBufferTeamOne").split(",");
				String[] spawnBufferTeamTwoCords = d.getString("spawnBufferTeamTwo").split(",");
				String[] eventSpawnCords = d.getString("eventSpawn").split(",");
				String[] arenaSignsIds = d.hasAttribute("arenaSigns") ? d.getString("arenaSigns").split(",") : null;
				String[] arenaSignSpawnOne = d.hasAttribute("arenaSignSpawnOne") ? d.getString("arenaSignSpawnOne").split(",") : null;
				String[] arenaSignSpawnTwo = d.hasAttribute("arenaSignSpawnTwo") ? d.getString("arenaSignSpawnTwo").split(",") : null;

				List<Integer> doorList = new ArrayList<>();
				List<ArenaZone> arenaZones = new ArrayList<>();
				List<PeaceZone> peaceZones = new ArrayList<>();
				List<Integer> arenaSignList = new ArrayList<>();
				List<Location> arenaSignSpawnList = new ArrayList<>();

				if (doors != null) {
					for (String door : doors) {
						doorList.add(Integer.valueOf(door));
					}
				}

				for (String zone : arenaZoneNames) {
					arenaZones.add(ZoneManager.getInstance().getZoneByName(zone, ArenaZone.class));
				}

				for (String zone : peaceZoneNames) {
					peaceZones.add(ZoneManager.getInstance().getZoneByName(zone, PeaceZone.class));
				}

				if (arenaSignsIds != null) {
					for (String sign : arenaSignsIds) {
						arenaSignList.add(Integer.valueOf(sign));
					}
				}

				if (arenaSignSpawnOne != null) {
					arenaSignSpawnList.add(new Location(Integer.valueOf(arenaSignSpawnOne[0]),
							Integer.valueOf(arenaSignSpawnOne[1]),
							Integer.valueOf(arenaSignSpawnOne[2])));
				}
				if (arenaSignSpawnTwo != null) {
					arenaSignSpawnList.add(new Location(Integer.valueOf(arenaSignSpawnTwo[0]),
							Integer.valueOf(arenaSignSpawnTwo[1]),
							Integer.valueOf(arenaSignSpawnTwo[2])));
				}

				Location spawnTeamOne = new Location(Integer.valueOf(spawnTeamOneCords[0]),
						Integer.valueOf(spawnTeamOneCords[1]),
						Integer.valueOf(spawnTeamOneCords[2]),
						Integer.valueOf(spawnTeamOneCords[3]));
				Location spawnTeamTwo = new Location(Integer.valueOf(spawnTeamTwoCords[0]),
						Integer.valueOf(spawnTeamTwoCords[1]),
						Integer.valueOf(spawnTeamTwoCords[2]),
						Integer.valueOf(spawnTeamTwoCords[3]));
				Location bufferSpawnTeamOne = new Location(Integer.valueOf(spawnBufferTeamOneCords[0]),
						Integer.valueOf(spawnBufferTeamOneCords[1]),
						Integer.valueOf(spawnBufferTeamOneCords[2]),
						Integer.valueOf(spawnBufferTeamOneCords[3]));
				Location bufferSpawnTeamTwo = new Location(Integer.valueOf(spawnBufferTeamTwoCords[0]),
						Integer.valueOf(spawnBufferTeamTwoCords[1]),
						Integer.valueOf(spawnBufferTeamTwoCords[2]),
						Integer.valueOf(spawnBufferTeamTwoCords[3]));
				Location eventSpawmn = new Location(Integer.valueOf(eventSpawnCords[0]),
						Integer.valueOf(eventSpawnCords[1]),
						Integer.valueOf(eventSpawnCords[2]),
						Integer.valueOf(eventSpawnCords[3]));

				predefinedEvents.put(name,
						new Event(name,
								description,
								locationName,
								doorList,
								arenaZones,
								peaceZones,
								spawnTeamOne,
								spawnTeamTwo,
								bufferSpawnTeamOne,
								bufferSpawnTeamTwo,
								eventSpawmn,
								arenaSignSpawnList,
								arenaSignList));
			} else if (d.getName().equalsIgnoreCase("subEvent")) {
				String eventName = d.getString("eventName");
				String startBypass = d.getString("startBypass");
				String endBypass = d.getString("endBypass");
				subEvents.put(eventName, new SubEvent(eventName, startBypass, endBypass));
			}
		}

		log.info("GMEventManager: loaded " + predefinedEvents.size() + " predefinied events " + currencies.size() + " bet currencies and " +
				subEvents.size() + " sub events!");
	}

	private GMEventManager() {
	}

	public static GMEventManager getInstance() {
		return SingletonHolder.instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final GMEventManager instance = new GMEventManager();
	}
}
