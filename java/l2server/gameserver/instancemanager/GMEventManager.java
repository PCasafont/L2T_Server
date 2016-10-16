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
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.communitybbs.Manager.CustomCommunityBoard;
import l2server.gameserver.datatables.*;
import l2server.gameserver.datatables.MapRegionTable.TeleportWhereType;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2ColosseumFence;
import l2server.gameserver.model.actor.L2ColosseumFence.FenceState;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2DoorInstance;
import l2server.gameserver.model.actor.instance.L2NpcInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2StaticObjectInstance;
import l2server.gameserver.model.entity.Message;
import l2server.gameserver.model.itemcontainer.Mail;
import l2server.gameserver.model.olympiad.OlympiadManager;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.model.zone.type.L2ArenaZone;
import l2server.gameserver.model.zone.type.L2PeaceZone;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.gameserver.network.serverpackets.SocialAction;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;
import l2server.gameserver.util.NpcUtil;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author LasTravel
 */

public class GMEventManager
{
	private static final int bufferNpcId = 8508;
	private static final int dummyArenaSignNpcId = 35608;
	private static final int rewardCoinId = 14720;
	private Map<String, Event> predefinedEvents = new HashMap<>();
	private static Map<Integer, CurrencyInfo> currencies = new LinkedHashMap<>();
	private static Map<String, SubEvent> subEvents = new HashMap<>();
	private static Event currentEvent;

	public String getCustomEventPanel(L2PcInstance player, int pageToShow)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<table width=750 border=0>");

		if (player.isGM())
		{
			if (this.currentEvent == null || !this.currentEvent.isStarted())
			{
				sb.append("<tr><td><table width=750 border=1>");
				if (this.currentEvent == null || !this.currentEvent.isStarted())
				{
					String subEvents = "";
					for (Entry<String, SubEvent> event : this.subEvents.entrySet())
					{
						subEvents += event.getKey() + ";";
					}

					if (!subEvents.isEmpty())
					{
						sb.append(
								"<tr><td>Start sub event:</td><td><combobox width=100 height=17 var=\"subEvent\" list=" +
										subEvents +
										"></td><td><button action=\"bypass this.bbscustom;action;gEvent;startSubEvent; $subEvent ;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td></tr>");
					}

					String predefinedEvents = "";
					for (Entry<String, Event> event : this.predefinedEvents.entrySet())
					{
						predefinedEvents += event.getValue().getName() + ";";
					}

					if (!predefinedEvents.isEmpty())
					{
						sb.append(
								"<tr><td>Load predefined event:</td><td><combobox width=100 height=17 var=\"loadEvent\" list=" +
										predefinedEvents +
										"></td><td><button action=\"bypass this.bbscustom;action;gEvent;loadEvent; $loadEvent ;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td></tr>");
					}

					String eventName =
							"<td FIXWIDTH=200><edit var=\"eName\" width=100 length=25></td><td><button action=\"bypass this.bbscustom;action;gEvent;setName; $eName ;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td>";
					if (this.currentEvent != null && this.currentEvent.getName() != null)
					{
						eventName = "<td FIXWIDTH=200>" + this.currentEvent.getName() +
								"</td><td><button action=\"bypass this.bbscustom;action;gEvent;delName;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td>";
					}

					String eventDescription =
							"<td><edit var=\"eDesc\" width=100 length=25></td><td><button action=\"bypass this.bbscustom;action;gEvent;setDesc; $eDesc ; \" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td>";
					if (this.currentEvent != null && this.currentEvent.getDescription() != null)
					{
						eventDescription = "<td>" + this.currentEvent.getDescription() +
								"</td><td><button action=\"bypass this.bbscustom;action;gEvent;delDesc;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td>";
					}

					String eventLocation =
							"<td><edit var=\"eLoc\" width=100 length=25></td><td><button action=\"bypass this.bbscustom;action;gEvent;setLoc; $eLoc ; \" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td>";
					if (this.currentEvent != null && this.currentEvent.getLocation() != null)
					{
						eventLocation = "<td>" + this.currentEvent.getLocation() +
								"</td><td><button action=\"bypass this.bbscustom;action;gEvent;delLoc;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td>";
					}

					String arenaZoneName = "";
					if (this.currentEvent != null && this.currentEvent.getArenaZones() != null)
					{
						for (L2ArenaZone zone : this.currentEvent.getArenaZones())
						{
							if (zone == null)
							{
								continue;
							}
							arenaZoneName += zone.getName() + "<br1> ";
						}
					}

					String peaceZoneName = "";
					if (this.currentEvent != null && this.currentEvent.getPeaceZones() != null)
					{
						for (L2PeaceZone zone : this.currentEvent.getPeaceZones())
						{
							if (zone == null)
							{
								continue;
							}
							peaceZoneName += zone.getName() + "<br1> ";
						}
					}

					String teamOneSpawn =
							"<td><button action=\"bypass this.bbscustom;action;gEvent;setTeamOneSpawn;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td>";
					if (this.currentEvent != null && this.currentEvent.getTeamOneSpawn() != null)
					{
						teamOneSpawn = "<td>" + this.currentEvent.getTeamOneSpawn().getX() + ", " +
								this.currentEvent.getTeamOneSpawn().getY() + ", " + this.currentEvent.getTeamOneSpawn().getZ() +
								"</td><td><button action=\"bypass this.bbscustom;action;gEvent;delTeamOneSpawn;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td>";
					}

					String teamTwoSpawn =
							"<td><button action=\"bypass this.bbscustom;action;gEvent;setTeamTwoSpawn;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td>";
					if (this.currentEvent != null && this.currentEvent.getTeamTwoSpawn() != null)
					{
						teamTwoSpawn = "<td>" + this.currentEvent.getTeamTwoSpawn().getX() + ", " +
								this.currentEvent.getTeamTwoSpawn().getY() + ", " + this.currentEvent.getTeamTwoSpawn().getZ() +
								"</td><td><button action=\"bypass this.bbscustom;action;gEvent;delTeamTwoSpawn;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td>";
					}

					String spawnBufferOne =
							"<td><button action=\"bypass this.bbscustom;action;gEvent;setSpawnBufferOne;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td>";
					if (this.currentEvent != null && this.currentEvent.getSpawnBufferOne() != null)
					{
						spawnBufferOne = "<td>" + this.currentEvent.getSpawnBufferOne().getX() + ", " +
								this.currentEvent.getSpawnBufferOne().getY() + ", " +
								this.currentEvent.getSpawnBufferOne().getZ() +
								"</td><td><button action=\"bypass this.bbscustom;action;gEvent;delSpawnBufferOne;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td>";
					}

					String spawnBufferTwo =
							"<td><button action=\"bypass this.bbscustom;action;gEvent;setSpawnBufferTwo;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td>";
					if (this.currentEvent != null && this.currentEvent.getSpawnBufferTwo() != null)
					{
						spawnBufferTwo = "<td>" + this.currentEvent.getSpawnBufferTwo().getX() + ", " +
								this.currentEvent.getSpawnBufferTwo().getY() + ", " +
								this.currentEvent.getSpawnBufferTwo().getZ() +
								"</td><td><button action=\"bypass this.bbscustom;action;gEvent;delSpawnBufferTwo;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td>";
					}

					String spawnLoc =
							"<td><button action=\"bypass this.bbscustom;action;gEvent;setSpawnLoc;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td>";
					if (this.currentEvent != null && this.currentEvent.getTeleportLocation() != null)
					{
						spawnLoc = "<td>" + this.currentEvent.getTeleportLocation().getX() + ", " +
								this.currentEvent.getTeleportLocation().getY() + ", " +
								this.currentEvent.getTeleportLocation().getZ() +
								"</td><td><button action=\"bypass this.bbscustom;action;gEvent;delSpawnLoc;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td>";
					}

					String doors = "";
					if (this.currentEvent != null && this.currentEvent.getDoors() != null)
					{
						for (int i : this.currentEvent.getDoors())
						{
							doors += i + "<br>";
						}
					}

					String arenaSign = "";
					if (this.currentEvent != null && this.currentEvent.getArenaSignIds() != null)
					{
						for (int id : this.currentEvent.getArenaSignIds())
						{
							arenaSign += id + "<br1> ";
						}
					}

					String arenaSignSpawns = "";
					if (this.currentEvent != null && this.currentEvent.getArenaSignSpawns() != null)
					{
						for (Location loc : this.currentEvent.getArenaSignSpawns())
						{
							arenaSignSpawns += loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + "<br1> ";
						}
					}

					sb.append("<tr><td FIXWIDTH=200>Enter the Event Name:</td>" + eventName + "</tr>");
					sb.append("<tr><td>Enter the Event Description:</td>" + eventDescription + "</tr>");
					sb.append("<tr><td>Enter the Event Location:</td>" + eventLocation + "</tr>");
					sb.append("<tr><td>Set Arena Zone:</td><td>" + arenaZoneName +
							"</td><td><button action=\"bypass this.bbscustom;action;gEvent;setArena;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td><td><button action=\"bypass this.bbscustom;action;gEvent;delArena;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td></tr>");
					sb.append("<tr><td>Set Peace Zone:</td><td>" + peaceZoneName +
							"</td><td><button action=\"bypass this.bbscustom;action;gEvent;setPeace;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td><td><button action=\"bypass this.bbscustom;action;gEvent;delPeace;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td></tr>");
					sb.append("<tr><td>Team One Spawn:</td>" + teamOneSpawn + "</tr>");
					sb.append("<tr><td>Team Two Spawn:</td>" + teamTwoSpawn + "</tr>");
					sb.append("<tr><td>Team One Buffer Spawn:</td>" + spawnBufferOne + "</tr>");
					sb.append("<tr><td>Team Two Buffer Spawn:</td>" + spawnBufferTwo + "</tr>");
					sb.append("<tr><td>Event Spawn:</td>" + spawnLoc + "</tr>");
					sb.append("<tr><td>Doors:</td><td>" + doors +
							"</td><td><td><button action=\"bypass this.bbscustom;action;gEvent;setDoor;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td><td><button action=\"bypass this.bbscustom;action;gEvent;delDoor;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td></tr>");
					sb.append("<tr><td>Arena Signs:</td><td>" + arenaSign +
							"</td><td><td><button action=\"bypass this.bbscustom;action;gEvent;setArenaSign;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td><td><button action=\"bypass this.bbscustom;action;gEvent;delArenaSign;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td></tr>");
					sb.append("<tr><td>Arena Sign Spawns:</td><td>" + arenaSignSpawns +
							"</td><td><td><button action=\"bypass this.bbscustom;action;gEvent;addSignSpawn;\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></button></td><td><button action=\"bypass this.bbscustom;action;gEvent;delSignSpawn;\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td></tr>");
					sb.append(
							"<tr><td><button value=\"Start\" width=200 height=24 action=\"bypass this.bbscustom;action;gEvent;startEvent;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button></td><td><button value=\"Restart Config\" width=200 height=24 action=\"bypass this.bbscustom;action;gEvent;restartConfig;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button></td></tr>");
				}
				sb.append("</table></td></tr>");
			}
			else
			{
				String reAnnounce =
						"<button value=\"Re Announce\" width=100 height=24 action=\"bypass this.bbscustom;action;gEvent;reAnnounce;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>";

				String manageDoors = "";
				if (this.currentEvent != null && this.currentEvent.isStarted() && !this.currentEvent.getDoors().isEmpty())
				{
					manageDoors =
							"<button value=\"Manage Doors\" width=100 height=24 action=\"bypass this.bbscustom;action;gEvent;manageDoors;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>";
				}

				String manageFight =
						"<button value=\"Start Fight\" width=100 height=24 action=\"bypass this.bbscustom;action;gEvent;startFight;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>";
				if (this.currentEvent != null && this.currentEvent.isFightStarted())
				{
					manageFight =
							"<button value=\"Stop Fight\" width=100 height=24 action=\"bypass this.bbscustom;action;gEvent;stopFight;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>";
				}

				String manageFences =
						"<button value=\"Add Fences\" width=100 height=24 action=\"bypass this.bbscustom;action;gEvent;addFences;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>";
				if (this.currentEvent != null && !this.currentEvent.getFences().isEmpty())
				{
					manageFences =
							"<button value=\"Delete Fences\" width=100 height=24 action=\"bypass this.bbscustom;action;gEvent;delFences;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>";
				}

				String manageBets =
						"<button value=\"Open Bets\" width=100 height=24 action=\"bypass this.bbscustom;action;gEvent;addBets;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>";
				if (this.currentEvent != null && this.currentEvent.getAllowBets())
				{
					manageBets =
							"<button value=\"Close Bets\" width=100 height=24 action=\"bypass this.bbscustom;action;gEvent;delBets;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>";
				}

				String returnBets = "";
				if (this.currentEvent != null && this.currentEvent.hasBets())
				{
					returnBets =
							"<button value=\"Return Bets\" width=100 height=24 action=\"bypass this.bbscustom;action;gEvent;returnBets;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>";
				}

				boolean hasBets = this.currentEvent != null && this.currentEvent.hasBets();

				sb.append("<tr><td><table width=750 border=0><tr><td>" + reAnnounce + "</td><td>" + manageDoors +
						"</td><td>" + manageFences + "</td><td>" + manageBets + "</td><td>" + returnBets + "</td><td>" +
						manageFight +
						"</td><td><button value=\"Restart Fight\" width=100 height=24 action=\"bypass this.bbscustom;action;gEvent;restartFight;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button></td><td><button value=\"Stop Event\" width=100 height=24 action=\"bypass this.bbscustom;action;gEvent;stopEvent;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button></td></tr></table></td></tr>");
				sb.append(
						"<tr><td><table width=750 bgcolor=999999 border=0><tr><td FIXWIDTH=150>Team One Players</td><td FIXWIDTH=50><combobox width=100 height=17 var=\"rCount1\" list=1;2;3;4;5></td><td FIXWIDTH=350><button action=\"bypass this.bbscustom;action;gEvent;giveReward;1; $rCount1 ;\" value=\" \" width=16 height=16 back=L2UI_CH3.joypad_r_hold fore=L2UI_CH3.joypad_r_over></button></td><td FIXWIDTH=200>" +
								(hasBets ?
										"<button value=\"Reward Bets\" width=100 height=24 action=\"bypass this.bbscustom;action;gEvent;giveBetRewards;1\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>" :
										"") + "</td></tr></table></td></tr>");
				sb.append("<tr><td><table width=750>");
				for (Entry<Integer, Integer> i : this.currentEvent.getParticipants().entrySet())
				{
					if (i == null)
					{
						continue;
					}

					L2PcInstance pl = L2World.getInstance().getPlayer(i.getKey());
					if (pl == null)
					{
						continue;
					}

					if (i.getValue() == 1)
					{
						sb.append("<tr><td FIXWIDTH=200>" + pl.getName() +
								"</td><td FIXWIDTH=550><button action=\"bypass this.bbscustom;action;gEvent;delPlayer;" +
								pl.getObjectId() +
								"\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td></tr>");
					}
				}
				sb.append("</table></td></tr>");

				sb.append(
						"<tr><td><table width=750 bgcolor=999999 border=0><tr><td FIXWIDTH=150>Team Two Players</td><td FIXWIDTH=50><combobox width=100 height=17 var=\"rCount2\" list=1;2;3;4;5></td><td FIXWIDTH=350><button action=\"bypass this.bbscustom;action;gEvent;giveReward;2; $rCount2 ;\" value=\" \" width=16 height=16 back=L2UI_CH3.joypad_r_hold fore=L2UI_CH3.joypad_r_over></button></td><td FIXWIDTH=200>" +
								(hasBets ?
										"<button value=\"Reward Bets\" width=100 height=24 action=\"bypass this.bbscustom;action;gEvent;giveBetRewards;2\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button>" :
										"") + "</td></tr></table></td></tr>");
				sb.append("<tr><td><table width=750>");
				for (Entry<Integer, Integer> i : this.currentEvent.getParticipants().entrySet())
				{
					if (i == null)
					{
						continue;
					}

					L2PcInstance pl = L2World.getInstance().getPlayer(i.getKey());
					if (pl == null)
					{
						continue;
					}

					if (i.getValue() == 2)
					{
						sb.append("<tr><td FIXWIDTH=200>" + pl.getName() +
								"</td><td FIXWIDTH=550><button action=\"bypass this.bbscustom;action;gEvent;delPlayer;" +
								pl.getObjectId() +
								"\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td></tr>");
					}
				}
				sb.append("</table></td></tr>");

				sb.append(
						"<tr><td><table width=850 bgcolor=999999 border=0><tr><td FIXWIDTH=750>Banned Players</td></tr></table></td></tr>");
				sb.append("<tr><td><table width=750>");
				for (Entry<String, String> i : this.currentEvent.getBannedIpsFromEvent().entrySet())
				{
					if (i == null)
					{
						continue;
					}

					sb.append("<tr><td FIXWIDTH=200>" + i.getValue() +
							"</td><td FIXWIDTH=550><button action=\"bypass this.bbscustom;action;gEvent;delBan;" +
							i.getKey() +
							"\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td></tr>");
				}
				sb.append("</table></td></tr>");

				sb.append(
						"<tr><td><table width=850 bgcolor=999999 border=0><tr><td FIXWIDTH=750>Waiting Players</td></tr></table></td></tr>");
				sb.append("<tr><td>");
				List<L2PcInstance> allPlayers = new ArrayList<>();
				for (L2PeaceZone zone : this.currentEvent.getPeaceZones())
				{
					if (zone == null)
					{
						continue;
					}

					for (L2PcInstance pl : zone.getPlayersInside())
					{
						if (pl == null || allPlayers.contains(pl))
						{
							continue;
						}
						allPlayers.add(pl);
					}
				}

				int maxWaitingPLayersPerPage = 10;
				int auctionsSize = allPlayers.size();
				int maxPages = auctionsSize / maxWaitingPLayersPerPage;
				if (auctionsSize > maxWaitingPLayersPerPage * maxPages)
				{
					maxPages++;
				}
				if (pageToShow > maxPages)
				{
					pageToShow = maxPages;
				}
				int pageStart = maxWaitingPLayersPerPage * pageToShow;
				int pageEnd = auctionsSize;
				if (pageEnd - pageStart > maxWaitingPLayersPerPage)
				{
					pageEnd = pageStart + maxWaitingPLayersPerPage;
				}

				if (maxPages > 1)
				{
					sb.append("<center>" + CustomCommunityBoard.getInstance()
							.createPages(pageToShow, maxPages, "_bbscustom;gmEvent;", ";") + "</center>");
				}

				int x = 0;
				for (int i = pageStart; i < pageEnd; i++)
				{
					L2PcInstance pl = allPlayers.get(i);
					if (pl == null)
					{
						continue;
					}

					int participatedTimes = currentEvent
							.getParticipatedTimes(pl.getObjectId(), pl.getExternalIP(), pl.getInternalIP());
					sb.append("<table width=750 " + (x % 2 == 1 ? "bgcolor=131210" : "") + "><tr><td FIXWIDTH=159>" +
							pl.getName() + " " + (participatedTimes > 0 ? "(" + participatedTimes + ")" : "") +
							"</td><td FIXWIDTH=159>" +
							PlayerClassTable.getInstance().getClassNameById(pl.getClassId()) + " (Lv. " +
							pl.getLevel() + ")</td><td FIXWIDTH=159>" +
							(pl.getClan() != null ? pl.getClan().getName() : "NoClan") +
							"</td><td FIXWIDTH=120><button value=\"Add to Team One\" width=120 height=20 action=\"bypass this.bbscustom;action;gEvent;addPlayer;" +
							pl.getObjectId() +
							";1\" back=L2UI_CT1.Button_DF_Calculator_Over fore=L2UI_CT1.Button_DF_Calculator></button></td><td FIXWIDTH=120><button value=\"Add to Team Two\" width=120 height=20 action=\"bypass this.bbscustom;action;gEvent;addPlayer;" +
							pl.getObjectId() +
							";2\" back=L2UI_CT1.Button_DF_Calculator_Over fore=L2UI_CT1.Button_DF_Calculator></button></td><td FIXWIDTH=16><button action=\"bypass this.bbscustom;action;gEvent;kickPlayer;" +
							pl.getObjectId() +
							"\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></button></td><td FIXWIDTH=16><button action=\"bypass this.bbscustom;action;gEvent;banPlayer;" +
							pl.getObjectId() +
							"\" value=\" \" width=16 height=16 back=L2UI_CT1.SellingAgencyWnd_df_HelpBtn_down fore=L2UI_CT1.SellingAgencyWnd_df_HelpBtn_down></button></td></tr></table>");
					x++;
				}
				sb.append("</td></tr>");
			}
		}
		else
		{
			if (this.currentEvent == null || !this.currentEvent.isStarted())
			{
				sb.append("<tr><td align=center><font color=LEVEL>There are no GM event right now!</font></td></tr>");
			}
			else
			{
				boolean isInsidePeaceZone = false;
				for (L2PeaceZone zone : this.currentEvent.getPeaceZones())
				{
					if (zone == null)
					{
						continue;
					}

					if (zone.isCharacterInZone(player))
					{
						isInsidePeaceZone = true;
						break;
					}
				}

				if (!isInsidePeaceZone)
				{
					sb.append("<tr><td align=center><table width=500 background=L2UI_CH3.refinewnd_back_Pattern>");
					sb.append("<tr><td FIXWIDTH=30>&nbsp;</td><td><br><br></td></tr>");
					sb.append("<tr><td FIXWIDTH=30>&nbsp;</td><td>Event Name:</td><td>" + this.currentEvent.getName() +
							"</td></tr>");
					sb.append("<tr><td FIXWIDTH=30>&nbsp;</td><td>Event Description:</td><td>" +
							this.currentEvent.getDescription() + "</td></tr>");
					sb.append("<tr><td FIXWIDTH=30>&nbsp;</td><td>Event Location:</td><td>" +
							this.currentEvent.getLocation() + "</td></tr>");
					sb.append("<tr><td FIXWIDTH=30>&nbsp;</td><td>Powered By:</td><td>" + this.currentEvent.getGMName() +
							"</td></tr>");
					sb.append("<tr><td FIXWIDTH=30>&nbsp;</td><td><br><br></td></tr>");
					sb.append("</table></td></tr>");

					sb.append("<tr><td align=center><table>");
					sb.append("<tr><td><button value=\"Take me to " + this.currentEvent.getLocation() +
							"!\" width=530 height=24 action=\"bypass this.bbscustom;action;gEvent;teleToEvent;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button></td></tr>");
					sb.append("</table></td></tr>");
				}
				else
				{
					if (this.currentEvent.getAllowBets() && this.currentEvent.getCanBetNow() &&
							!this.currentEvent.playerHasBet(player.getObjectId()) &&
							!this.currentEvent.isParticipant(player.getObjectId()))
					{
						String options = "";
						for (Entry<Integer, CurrencyInfo> b : this.currencies.entrySet())
						{
							options += b.getValue().getName() + ";";
						}

						sb.append("<tr><td align=center><table width=500 border=0>");
						sb.append(
								"<tr><td>Select your bet currency:</td><td><combobox width=100 height=17 var=\"bCoin\" list=" +
										options + "></td></tr>");
						sb.append(
								"<tr><td FIXWIDTH=100>Introduce your bet:</td><td><edit var=\"bet\" type=number width=100 length=25></td></tr>");
						sb.append(
								"<tr><td>Select the team you want to bet for:</td><td><combobox width=100 height=17 var=\"betTeam\" list=blue;red></td></tr>");
						sb.append(
								"</table></td></tr><tr><td align=center><table><tr><td><button value=\"Bet!\" width=500 height=24 action=\"bypass this.bbscustom;action;gEvent;doBet; $bCoin ; $bet ; $betTeam ;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></button></td></tr></table></td></tr>");
					}
					else
					{
						sb.append(
								"<tr><td align=center><font color=LEVEL>You already did a bet or bets are already closed!</font></td></tr>");
					}
				}
			}
		}

		sb.append("</table>");
		return sb.toString();
	}

	private int getCurrencyId(String coinName)
	{
		for (Entry<Integer, CurrencyInfo> i : this.currencies.entrySet())
		{
			if (i.getValue().getName().equalsIgnoreCase(coinName))
			{
				return i.getValue().getId();
			}
		}
		return 57;
	}

	private class CurrencyInfo
	{
		private String name;
		private int id;

		private CurrencyInfo(int id)
		{
			this.id = id;
			this.name = ItemTable.getInstance().getTemplate(id).getName().replace(" ", "");
		}

		private String getName()
		{
			return this.name;
		}

		private int getId()
		{
			return this.id;
		}
	}

	private class SubEvent
	{
		@SuppressWarnings("unused")
		private String eventName;
		private String startBypass;
		private String endBypass;

		private SubEvent(String eventName, String startBypass, String endBypass)
		{
			this.eventName = eventName;
			this.startBypass = startBypass;
			this.endBypass = endBypass;
		}

		private String getStartBypass()
		{
			return this.startBypass;
		}

		@SuppressWarnings("unused")
		private String getEndBypass()
		{
			return this.endBypass;
		}
	}

	private class Bets
	{
		private int playerId;
		private int itemId;
		private Long betAmount;
		private int teamId;

		private Bets(int playerId, int itemId, long betAmount, int teamId)
		{
			this.playerId = playerId;
			this.itemId = itemId;
			this.betAmount = betAmount;
			this.teamId = teamId;
		}

		private int getPlayerId()
		{
			return this.playerId;
		}

		private int getItemId()
		{
			return this.itemId;
		}

		private Long getBetAmount()
		{
			return this.betAmount;
		}

		private int getTeamId()
		{
			return this.teamId;
		}
	}

	public void handleEventCommand(L2PcInstance player, String command)
	{
		if (player == null)
		{
			return;
		}

		StringTokenizer st = new StringTokenizer(command, ";");
		st.nextToken();
		st.nextToken();
		st.nextToken();

		if (player.isGM())
		{
			switch (String.valueOf(st.nextToken()))
			{
				case "loadEvent":
					this.currentEvent = new Event(this.predefinedEvents.get(st.nextToken().trim()));
					break;

				case "startSubEvent":
				{
					String eventName = st.nextToken().trim();
					Quest subEvent = QuestManager.getInstance().getQuest(eventName);
					if (subEvent != null)
					{
						subEvent.notifyEvent(this.subEvents.get(eventName).getStartBypass(), null, null);
					}
					break;
				}

				case "setName":
					if (this.currentEvent == null)//new event design
					{
						this.currentEvent = new Event(st.nextToken());
					}
					else
					{
						this.currentEvent.setName(st.nextToken());
					}
					break;

				case "setDesc":
					this.currentEvent.setDescription(st.nextToken());
					break;

				case "setLoc":
					this.currentEvent.setLocation(st.nextToken());
					break;

				case "setPeace":
					L2PeaceZone peace = ZoneManager.getInstance().getZone(player, L2PeaceZone.class);
					if (peace != null)
					{
						this.currentEvent.setPeaceZone(peace);
					}
					break;

				case "setArena":
					L2ArenaZone arena = ZoneManager.getInstance().getArena(player);
					if (arena != null)
					{
						this.currentEvent.setArenaZone(arena);
					}
					break;

				case "setTeamOneSpawn":
					this.currentEvent.setTeamOneSpawn(new Location(player.getX(), player.getY(), player.getZ() + 10));
					break;

				case "setTeamTwoSpawn":
					this.currentEvent.setTeamTwoSpawn(new Location(player.getX(), player.getY(), player.getZ() + 10));
					break;

				case "setSpawnBufferOne":
					this.currentEvent.setSpawnBufferOne(
							new Location(player.getX(), player.getY(), player.getZ() + 10, player.getHeading()));
					break;

				case "setSpawnBufferTwo":
					this.currentEvent.setSpawnBufferTwo(
							new Location(player.getX(), player.getY(), player.getZ() + 10, player.getHeading()));
					break;

				case "setSpawnLoc":
					this.currentEvent.setTeleportLocation(new Location(player.getX(), player.getY(), player.getZ() + 10));
					break;

				case "setArenaSign":
					L2StaticObjectInstance arenaSign = null;
					L2Object target = player.getTarget();
					if (target != null && target instanceof L2StaticObjectInstance)
					{
						arenaSign = (L2StaticObjectInstance) target;
					}

					int staticId = 0;
					if (arenaSign != null)
					{
						staticId = arenaSign.getStaticObjectId();
						this.currentEvent.addArenaSignNpc(staticId);
					}
					break;

				case "delArenaSign":
					this.currentEvent.addArenaSignNpc(0);
					break;

				case "addSignSpawn":
					this.currentEvent.addArenaSignSpawn(new Location(player.getX(), player.getY(), player.getZ() + 10));
					break;

				case "addDoor":
					this.currentEvent.addDoor(((L2DoorInstance) player.getTarget()).getDoorId());
					break;

				case "delDoor":
					this.currentEvent.addDoor(0);
					break;

				case "delSignSpawn":
					this.currentEvent.addArenaSignSpawn(null);
					break;

				case "delName":
					this.currentEvent.setName(null);
					break;

				case "delDesc":
					this.currentEvent.setDescription(null);
					break;

				case "delLoc":
					this.currentEvent.setLocation(null);
					break;

				case "delPeace":
					this.currentEvent.setPeaceZone(null);
					break;

				case "delArena":
					this.currentEvent.setArenaZone(null);
					break;

				case "delTeamOneSpawn":
					this.currentEvent.setTeamOneSpawn(null);
					break;

				case "delTeamTwoSpawn":
					this.currentEvent.setTeamTwoSpawn(null);
					break;

				case "delSpawnBufferOne":
					this.currentEvent.setSpawnBufferOne(null);
					break;

				case "delSpawnBufferTwo":
					this.currentEvent.setSpawnBufferTwo(null);
					break;

				case "delSpawnLoc":
					this.currentEvent.setTeleportLocation(null);
					break;

				case "startEvent":
					this.currentEvent.startEvent(player.getName());
					break;

				case "restartConfig":
					this.currentEvent = null;
					break;

				case "startFight":
					this.currentEvent.startFight();
					break;

				case "stopFight":
					this.currentEvent.stopFight();
					break;

				case "restartFight":
					this.currentEvent.restartFight();
					break;

				case "stopEvent":
					ArrayList<Integer> copyParticipants = new ArrayList<>(this.currentEvent.getParticipants().keySet());
					for (int i : copyParticipants)
					{
						this.currentEvent.removeParticipant(i);
					}

					this.currentEvent.deleteArenaSigns();
					this.currentEvent.removeFences();

					this.currentEvent = null;
					Announcements.getInstance()
							.announceToAll("The GM Event has ended, thanks everyone for participate!");
					break;

				case "addPlayer":
				{
					int playerId = Integer.valueOf(st.nextToken());
					int teamId = Integer.valueOf(st.nextToken());

					this.currentEvent.addParticipant(playerId, teamId);
					break;
				}
				case "delPlayer":
				{
					int playerId = Integer.valueOf(st.nextToken());

					this.currentEvent.removeParticipant(playerId);
					break;
				}
				case "kickPlayer":
				{
					int playerId = Integer.valueOf(st.nextToken());
					this.currentEvent.kickPlayer(playerId);
					break;
				}
				case "banPlayer":
				{
					int playerId = Integer.valueOf(st.nextToken());
					this.currentEvent.addBannedIp(playerId);
					break;
				}
				case "delBan":
				{
					String ip = st.nextToken();
					this.currentEvent.removeBannedIp(ip);
					break;
				}

				case "reAnnounce":
					this.currentEvent.reAnnounce();
					break;

				case "manageDoors":
					for (int i : this.currentEvent.getDoors())
					{
						L2DoorInstance door = DoorTable.getInstance().getDoor(i);
						if (door == null)
						{
							continue;
						}

						if (door.getOpen())
						{
							door.closeMe();
						}
						else
						{
							door.openMe();
						}
					}
					break;

				case "addFences":
					this.currentEvent.addFeances();
					break;

				case "delFences":
					this.currentEvent.removeFences();
					break;

				case "addBets":
					this.currentEvent.setAllowBets(true);
					break;

				case "delBets":
					this.currentEvent.setAllowBets(false);
					break;

				case "returnBets":
					this.currentEvent.returnBets();
					break;

				case "giveBetRewards":
					this.currentEvent.giveBetRewards(Integer.valueOf(st.nextToken()));
					break;

				case "giveReward":
					this.currentEvent.giveReward(Integer.valueOf(st.nextToken()), Integer.valueOf(st.nextToken().trim()));
					break;
			}
			CustomCommunityBoard.getInstance().parseCmd("_bbscustom;gmEvent;0", player);
		}
		else
		{
			switch (String.valueOf(st.nextToken()))
			{
				case "teleToEvent":
					if (this.currentEvent != null && this.currentEvent.isStarted() && teleportConditions(player))
					{
						player.teleToLocation(this.currentEvent.getTeleportLocation(), true);
					}
					break;

				case "acceptRules":
					if (this.currentEvent != null && this.currentEvent.isStarted())
					{
						this.currentEvent.acceptRules(player.getObjectId());
						player.sendPacket(new ExShowScreenMessage("You're now allowed to enter! ", 3000));
					}
					break;

				case "getBuff":
					if (player.getIsInsideGMEvent() && this.currentEvent != null && this.currentEvent.isStarted() &&
							this.currentEvent.isParticipant(player.getObjectId()))
					{
						if (this.currentEvent.canUseMoreBuffs(player.getObjectId()))
						{
							this.currentEvent.addUsedBuff(player.getObjectId());

							L2Skill buff = SkillTable.getInstance().getInfo(Integer.valueOf(st.nextToken()), 1);
							if (buff != null)
							{
								buff.getEffects(player, player);
							}
						}
					}
					break;

				case "doBet":
					if (this.currentEvent == null || !this.currentEvent.getCanBetNow() || !this.currentEvent.getAllowBets() ||
							this.currentEvent.playerHasBet(player.getObjectId()) &&
									!this.currentEvent.isParticipant(player.getObjectId()))
					{
						return;
					}

					int teamId = 0;
					int itemId = 0;
					long betAmount = 0;

					if (st.hasMoreTokens())
					{
						itemId = getCurrencyId(st.nextToken().trim());
					}

					if (this.currencies.get(itemId) == null)
					{
						return; //client hack
					}

					if (st.hasMoreTokens())
					{
						betAmount = Long.valueOf(st.nextToken().trim());
					}

					if (st.hasMoreTokens())
					{
						if (st.nextToken().trim().equalsIgnoreCase("blue"))
						{
							teamId = 1;
						}
						else
						{
							teamId = 2;
						}
					}

					if (teamId == 0 || betAmount == 0 || itemId == 0)
					{
						return;
					}

					if (!player.getClient().getFloodProtectors().getTransaction().tryPerformAction("buy"))
					{
						return;
					}

					if (!player.destroyItemByItemId("GM Event", itemId, betAmount, null, true))
					{
						return;
					}

					this.currentEvent.addBet(player.getObjectId(), itemId, betAmount, teamId);

					CustomCommunityBoard.getInstance().parseCmd("_bbscustom;gmEvent;0", player);
					break;
			}
		}
	}

	private boolean teleportConditions(L2PcInstance player)
	{
		if (player == null)
		{
			return false;
		}

		return !(player.getLevel() < 99 || player.isInCombat() || player.getPvpFlag() > 0 ||
				player.getInstanceId() != 0 || player.isInDuel() || player.isFakeDeath() || player.isOutOfControl() ||
				player.isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(player) ||
				AttackStanceTaskManager.getInstance().getAttackStanceTask(player) || player.getIsInsideGMEvent() ||
				player.getEvent() != null);

	}

	private class ParticipateRegister
	{
		private int playerId;
		private String externalIp;
		private String internalIp;
		private int participatedTimes;

		private ParticipateRegister(int playerId, String externalIp, String internalIp)
		{
			this.playerId = playerId;
			this.externalIp = externalIp;
			this.internalIp = internalIp;
			this.participatedTimes = 1;
		}

		@SuppressWarnings("unused")
		private int getPlayerId()
		{
			return this.playerId;
		}

		private boolean matchIp(String externalIp, String internalIp)
		{
			return this.externalIp.equalsIgnoreCase(externalIp) && this.internalIp.equalsIgnoreCase(internalIp);
		}

		private int getParticipatedTimes()
		{
			return this.participatedTimes;
		}

		private void increaseParticipatedTimes()
		{
			this.participatedTimes++;
		}
	}

	private class Event
	{
		private String gmName;
		private String eventName;
		private String description;
		private String location;
		private List<Integer> doors;
		private List<L2ArenaZone> arenaZones;
		private List<L2Npc> buffers;
		private List<L2Npc> arenaSigns;
		private List<Integer> rewardedPlayers;
		private List<L2PeaceZone> peaceZones;
		private List<Integer> arenaSignIds;
		private List<Location> arenaSignSpawns;
		private List<L2ColosseumFence> areaFences;
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

		private Event(String eventName)
		{
			this.eventName = eventName;
			this.doors = new ArrayList<>();
			this.arenaZones = new ArrayList<>();
			this.peaceZones = new ArrayList<>();
			this.participants = new HashMap<>();
			this.areaFences = new ArrayList<>();
			this.bannedIpsFromTheEvent = new HashMap<>();
			this.arenaSignSpawns = new ArrayList<>();
			this.arenaSignIds = new ArrayList<>();
			this.acceptedEventRules = new HashMap<>();
			this.participatedRegister = new HashMap<>();
			this.usedBuffs = new HashMap<>();
			this.buffers = new ArrayList<>();
			this.arenaSigns = new ArrayList<>();
			this.rewardedPlayers = new ArrayList<>();
			this.bets = new HashMap<>();
			this.allowBets = false;
		}

		private Event(String eventName, String description, String locationName, List<Integer> doors, List<L2ArenaZone> arenaZones, List<L2PeaceZone> peaceZones, Location teamOneSpawn, Location teamTwoSpawn, Location bufferTeamOneSpawn, Location bufferTeamTwoSpawn, Location eventSpawn, List<Location> arenaSignSpawns, List<Integer> arenaSigns)
		{
			this.eventName = eventName;
			this.description = description;
			this.location = locationName;
			this.doors = doors;
			this.arenaZones = arenaZones;
			this.peaceZones = peaceZones;
			this.spawnOne = teamOneSpawn;
			this.spawnTwo = teamTwoSpawn;
			this.bufferSpawnOne = bufferTeamOneSpawn;
			this.bufferSpawnTwo = bufferTeamTwoSpawn;
			this.eventTeleport = eventSpawn;
			this.arenaSignSpawns = arenaSignSpawns;
			this.arenaSignIds = arenaSigns;
			this.participants = new HashMap<>();
			this.areaFences = new ArrayList<>();
			this.bannedIpsFromTheEvent = new HashMap<>();
			this.acceptedEventRules = new HashMap<>();
			this.participatedRegister = new HashMap<>();
			this.usedBuffs = new HashMap<>();
			this.buffers = new ArrayList<>();
			this.arenaSigns = new ArrayList<>();
			this.rewardedPlayers = new ArrayList<>();
			this.bets = new HashMap<>();
			this.allowBets = false;
		}

		private Event(Event event)
		{
			this.eventName = event.getName();
			this.description = event.getDescription();
			this.location = event.getLocation();
			this.doors = event.getDoors();
			this.arenaZones = event.getArenaZones();
			this.peaceZones = event.getPeaceZones();
			this.spawnOne = event.getTeamOneSpawn();
			this.spawnTwo = event.getTeamTwoSpawn();
			this.bufferSpawnOne = event.getSpawnBufferOne();
			this.bufferSpawnTwo = event.getSpawnBufferTwo();
			this.eventTeleport = event.getTeleportLocation();
			this.arenaSignSpawns = event.getArenaSignSpawns();
			this.arenaSignIds = event.getArenaSignIds();
			this.arenaSignSpawns = event.getArenaSignSpawns();
			this.participants = new HashMap<>();
			this.areaFences = new ArrayList<>();
			this.bannedIpsFromTheEvent = new HashMap<>();
			this.acceptedEventRules = new HashMap<>();
			this.participatedRegister = new HashMap<>();
			this.usedBuffs = new HashMap<>();
			this.buffers = new ArrayList<>();
			this.arenaSigns = new ArrayList<>();
			this.rewardedPlayers = new ArrayList<>();
			this.bets = new HashMap<>();
			this.allowBets = false;
		}

		private String getName()
		{
			return this.eventName;
		}

		private void setName(String n)
		{
			this.eventName = n;
		}

		private String getGMName()
		{
			return this.gmName;
		}

		private void setDescription(String d)
		{
			this.description = d;
		}

		private String getDescription()
		{
			return this.description;
		}

		private void setLocation(String b)
		{
			this.location = b;
		}

		private String getLocation()
		{
			return this.location;
		}

		private boolean isStarted()
		{
			return this.isStarted;
		}

		private boolean isFightStarted()
		{
			return this.isFightStarted;
		}

		private List<L2ColosseumFence> getFences()
		{
			return this.areaFences;
		}

		private List<Integer> getDoors()
		{
			return this.doors;
		}

		private List<L2ArenaZone> getArenaZones()
		{
			return this.arenaZones;
		}

		private List<L2PeaceZone> getPeaceZones()
		{
			return this.peaceZones;
		}

		private void setArenaZone(L2ArenaZone z)
		{
			if (z == null)
			{
				this.arenaZones.clear();
			}
			else if (!this.arenaZones.contains(z))
			{
				this.arenaZones.add(z);
			}
		}

		private void setPeaceZone(L2PeaceZone z)
		{
			if (z == null)
			{
				this.peaceZones.clear();
			}
			else if (!this.peaceZones.contains(z))
			{
				this.peaceZones.add(z);
			}
		}

		private void setTeleportLocation(Location l)
		{
			this.eventTeleport = l;
		}

		private Location getTeleportLocation()
		{
			return this.eventTeleport;
		}

		private void setTeamOneSpawn(Location loc)
		{
			this.spawnOne = loc;
		}

		private void setTeamTwoSpawn(Location loc)
		{
			this.spawnTwo = loc;
		}

		private boolean isParticipant(int playerId)
		{
			return this.participants.containsKey(playerId);
		}

		private Map<Integer, Integer> getParticipants()
		{
			return this.participants;
		}

		private void setSpawnBufferOne(Location l)
		{
			this.bufferSpawnOne = l;
		}

		private void setSpawnBufferTwo(Location l)
		{
			this.bufferSpawnTwo = l;
		}

		private Location getSpawnBufferOne()
		{
			return this.bufferSpawnOne;
		}

		private Location getSpawnBufferTwo()
		{
			return this.bufferSpawnTwo;
		}

		private boolean canUseMoreBuffs(int playerId)
		{
			return !this.usedBuffs.containsKey(playerId) || this.usedBuffs.get(playerId) < 5;
		}

		private void addUsedBuff(int playerId)
		{
			if (this.usedBuffs.containsKey(playerId))
			{
				this.usedBuffs.put(playerId, this.usedBuffs.get(playerId) + 1);
			}
			else
			{
				this.usedBuffs.put(playerId, 1);
			}
		}

		private Location getTeamOneSpawn()
		{
			return this.spawnOne;
		}

		private Location getTeamTwoSpawn()
		{
			return this.spawnTwo;
		}

		private void addDoor(int door)
		{
			if (door == 0)
			{
				this.doors.clear();
			}
			else
			{
				this.doors.add(door);
			}
		}

		private void addArenaSignSpawn(Location loc)
		{
			if (loc == null)
			{
				this.arenaSignSpawns.clear();
			}
			else
			{
				this.arenaSignSpawns.add(loc);
			}
		}

		private List<Location> getArenaSignSpawns()
		{
			return this.arenaSignSpawns;
		}

		private List<Integer> getArenaSignIds()
		{
			return this.arenaSignIds;
		}

		private void addArenaSignNpc(int id)
		{
			if (id == 0)
			{
				this.arenaSignIds.clear();
			}
			else
			{
				this.arenaSignIds.add(id);
			}
		}

		private int getParticipatedTimes(int playerId, String externalIp, String internalIp)
		{
			int times = 0;
			for (Entry<Integer, ParticipateRegister> reg : this.participatedRegister.entrySet())
			{
				ParticipateRegister i = reg.getValue();
				if (i == null)
				{
					continue;
				}

				if (i.matchIp(externalIp, internalIp))
				{
					times += i.getParticipatedTimes();
				}
			}
			return times;
		}

		private Map<String, String> getBannedIpsFromEvent()
		{
			return this.bannedIpsFromTheEvent;
		}

		private boolean isBannedIp(String eIp)
		{
			return this.bannedIpsFromTheEvent.containsKey(eIp);
		}

		private void addBannedIp(int playerId)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(playerId);
			if (player == null)
			{
				return;
			}

			this.bannedIpsFromTheEvent.put(player.getExternalIP(), player.getName());

			kickPlayer(playerId);

			//Revalidate the zone to kick dualbox
			for (L2PeaceZone zone : this.peaceZones)
			{
				if (zone == null)
				{
					continue;
				}

				for (L2PcInstance pl : zone.getPlayersInside())
				{
					if (pl == null)
					{
						continue;
					}
					onEnterZone(pl, zone);
				}
			}
		}

		private void removeBannedIp(String b)
		{
			this.bannedIpsFromTheEvent.remove(b);
		}

		private boolean hasBets()
		{
			return !this.bets.isEmpty();
		}

		private void setCanBetNow(boolean b)
		{
			this.canBetNow = b;

			if (this.allowBets)
			{
				sendPacketToWaitingPlayers(
						new CreatureSay(1, 15, "", "The bets are now " + (this.canBetNow ? "open" : "closed") + "! :"));
			}
		}

		private boolean getCanBetNow()
		{
			return this.canBetNow;
		}

		private void setAllowBets(boolean b)
		{
			this.allowBets = b;
		}

		private boolean getAllowBets()
		{
			return this.allowBets;
		}

		private void addBet(int playerId, int itemId, long betAmount, int teamId)
		{
			synchronized (this.bets)
			{
				this.bets.put(playerId, new Bets(playerId, itemId, betAmount, teamId));

				String charName = CharNameTable.getInstance().getNameById(playerId);
				Util.logToFile(charName + " did a bet " + currencies.get(itemId).getName() + "(" + betAmount +
						") for the team " + (teamId == 1 ? "blue" : "red"), "GMEvents", true);
			}
		}

		private boolean playerHasBet(int playerId)
		{
			return this.bets.containsKey(playerId);
		}

		private void giveBetRewards(int winnerTeam)
		{
			for (Entry<Integer, CurrencyInfo> currency : currencies.entrySet())
			{
				CurrencyInfo coin = currency.getValue();
				if (coin == null)
				{
					continue;
				}

				int winnerTeamSize = 0;
				for (Entry<Integer, Integer> i : this.participants.entrySet())
				{
					if (i.getValue() == winnerTeam)
					{
						winnerTeamSize += 1;
					}
				}

				long totalBet = 0;
				long fighterTotalBet = 0;
				for (Entry<Integer, Bets> bets : this.bets.entrySet())
				{
					Bets bet = bets.getValue();
					if (bet == null)
					{
						continue;
					}

					if (bet.getTeamId() == winnerTeam && bet.getItemId() == coin.getId())
					{
						fighterTotalBet += bet.getBetAmount();
					}

					if (bet.getItemId() == coin.getId())
					{
						totalBet += bet.getBetAmount();
					}
				}

				//There are no bets with this coin..
				if (totalBet == 0)
				{
					continue;
				}

				//Reward the fighter players
				long winnerBetReward = Math.round(totalBet * 0.1f / winnerTeamSize);
				for (Entry<Integer, Integer> i : this.participants.entrySet())
				{
					if (i.getValue() == winnerTeam)
					{
						sendRewardMail(i.getKey(), coin.getId(), winnerBetReward,
								"Congratulations, this is your bet percentage!");
					}
				}

				//Reward the winner waiting players
				for (Entry<Integer, Bets> bets : this.bets.entrySet())
				{
					Bets bet = bets.getValue();
					if (bet == null)
					{
						continue;
					}

					if (bet.getTeamId() == winnerTeam && bet.getItemId() == coin.getId())
					{
						long reward = Math.round(totalBet * 0.9f * bet.getBetAmount() / fighterTotalBet);
						sendRewardMail(bet.getPlayerId(), coin.getId(), reward,
								"Congratulations, your bet has been successful!");
					}
				}
			}
			this.bets.clear();
		}

		private void reAnnounce()
		{
			Announcements.getInstance()
					.announceToAll(this.eventName + " at " + this.location + " join with ALT + B > GM EVENT!");
		}

		private void returnBets()
		{
			this.canBetNow = false;

			for (Entry<Integer, Bets> bets : this.bets.entrySet())
			{
				Bets bet = bets.getValue();
				if (bet == null)
				{
					continue;
				}

				sendRewardMail(bet.getPlayerId(), bet.getItemId(), bet.getBetAmount(), "Bet Refound!");
			}
			this.bets.clear();
		}

		private boolean hasAcceptedRules(int playerId)
		{
			return this.acceptedEventRules.containsKey(playerId) && this.acceptedEventRules.get(playerId);
		}

		private void acceptRules(int charId)
		{
			this.acceptedEventRules.put(charId, true);
		}

		private void addFeances()
		{
			if (!this.arenaZones.isEmpty() && this.arenaZones.size() == 1)
			{
				L2ColosseumFence feance =
						addDynamic(this.arenaZones.get(0).getZone().getCenterX(), this.arenaZones.get(0).getZone().getCenterY(),
								-3775, -3775 + 100, -3775 - 100, 1100, 1100);
				if (feance != null)
				{
					this.areaFences.add(feance);
				}
			}
		}

		private void removeFences()
		{
			for (L2ColosseumFence fence : this.areaFences)
			{
				if (fence == null)
				{
					continue;
				}

				fence.decayMe();
				fence.getKnownList().removeAllKnownObjects();
			}
			this.areaFences.clear();
		}

		private void startFight()
		{
			this.isFightStarted = true;

			spawnBuffers();

			setCanBetNow(true);

			sendPacketToWaitingPlayers(new ExShowScreenMessage("The fight will start in 40 seconds!", 5000));
			sendPacketToFighterPlayers(
					new ExShowScreenMessage("Take buffs! The fight will start in 40 seconds!", 5000));

			ThreadPoolManager.getInstance().scheduleGeneral(() ->
			{
				setCanBetNow(false);

				deleteBuffers();

				for (Entry<Integer, Integer> i : currentEvent.getParticipants().entrySet())
				{
					L2PcInstance player = L2World.getInstance().getPlayer(i.getKey());
					if (player != null)
					{
						player.heal();
					}
				}

				sendPacketToWaitingPlayers(new ExShowScreenMessage("The fight has started!", 2000));
				sendPacketToFighterPlayers(new ExShowScreenMessage("Start the fight! GO! GO! GO!", 5000));
			}, 40000);
		}

		private void spawnBuffers()
		{
			L2Npc bufferOne = NpcUtil.addSpawn(bufferNpcId, this.bufferSpawnOne.getX(), this.bufferSpawnOne.getY(),
					this.bufferSpawnOne.getZ(), this.bufferSpawnOne.getHeading(), false, 0, false, 0);
			this.buffers.add(bufferOne);

			L2Npc bufferTwo = NpcUtil.addSpawn(bufferNpcId, this.bufferSpawnTwo.getX(), this.bufferSpawnTwo.getY(),
					this.bufferSpawnTwo.getZ(), this.bufferSpawnTwo.getHeading(), false, 0, false, 0);
			this.buffers.add(bufferTwo);
		}

		private void spawnArenaSigns()
		{
			if (this.arenaSignIds.isEmpty())
			{
				for (Location loc : this.arenaSignSpawns)
				{
					L2Npc npc =
							NpcUtil.addSpawn(dummyArenaSignNpcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(),
									false, 0, false, 0);
					this.arenaSigns.add(npc);
				}
			}
		}

		private void deleteArenaSigns()
		{
			if (this.arenaSignIds.isEmpty())
			{
				for (L2Npc arenaSign : this.arenaSigns)
				{
					arenaSign.deleteMe();
				}
			}
		}

		private void deleteBuffers()
		{
			for (L2Npc buffer : this.buffers)
			{
				buffer.deleteMe();
			}
		}

		private void sendPacketToFighterPlayers(L2GameServerPacket p)
		{
			for (L2ArenaZone zone : this.arenaZones)
			{
				if (zone == null)
				{
					continue;
				}
				zone.broadcastPacket(p);
			}
		}

		private void kickPlayersFromPeaceZones()
		{
			for (L2PeaceZone zone : this.peaceZones)
			{
				if (zone == null)
				{
					continue;
				}
				zone.oustAllPlayers();
			}
		}

		private void kickPlayersFromArenaZones()
		{
			for (L2ArenaZone zone : this.arenaZones)
			{
				if (zone == null)
				{
					continue;
				}
				zone.oustAllPlayers();
			}
		}

		private void sendPacketToWaitingPlayers(L2GameServerPacket p)
		{
			for (L2PeaceZone zone : this.peaceZones)
			{
				if (zone == null)
				{
					continue;
				}
				zone.broadcastPacket(p);
			}
		}

		private void stopFight()
		{
			this.isFightStarted = false;

			ArrayList<Integer> copyParticipants = new ArrayList<>(currentEvent.getParticipants().keySet());
			for (int i : copyParticipants)
			{
				removeParticipant(i);
			}

			this.usedBuffs.clear();
			this.rewardedPlayers.clear();
		}

		private void restartFight()
		{
			this.isFightStarted = false;

			for (Entry<Integer, Integer> i : this.participants.entrySet())
			{
				L2PcInstance player = L2World.getInstance().getPlayer(i.getKey());
				if (player == null)
				{
					continue;
				}

				if (player.isDead())
				{
					player.doRevive();
				}

				//Heal
				player.heal();

				//Reuse
				player.removeSkillReuse(true);

				//Spawn
				if (i.getValue() == 1)
				{
					player.teleToLocation(this.spawnOne, false);
				}
				else
				{
					player.teleToLocation(this.spawnTwo, false);
				}

				player.broadcastUserInfo();
			}

			this.usedBuffs.clear();
			this.rewardedPlayers.clear();
		}

		private void startEvent(String gmName)
		{
			this.isStarted = true;

			this.gmName = gmName;

			Announcements.getInstance().announceToAll("GM Event: " + this.eventName +
					" has started (Giran Arena), check the community board for more information!");

			//Kick already-inside players
			kickPlayersFromPeaceZones();
			kickPlayersFromArenaZones();

			//If the aren't arenaSigns by default we will spawn our ones
			spawnArenaSigns();
		}

		private void addParticipant(int playerId, int team)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(playerId);
			if (player == null)
			{
				return;
			}

			if (player.isInParty())
			{
				player.leaveParty();
			}

			if (player.isSitting())
			{
				player.standUp();
			}

			player.stopAllEffectsExceptThoseThatLastThroughDeath();
			player.removeSkillReuse(true);
			player.setTeam(team);
			player.setIsInsideGMEvent(true);
			player.teleToLocation(team == 1 ? this.spawnOne : this.spawnTwo, false);

			this.participants.put(playerId, team);

			sendPacketToWaitingPlayers(
					new ExShowScreenMessage(player.getName() + " joined the " + (team == 1 ? "blue" : "red") + " side!",
							2000));

			player.sendPacket(new ExShowScreenMessage("Please don't do any action, just wait!", 5000));
		}

		private void removeParticipant(int playerId)
		{
			if (isParticipant(playerId))
			{
				this.participants.remove(playerId);

				L2PcInstance player = L2World.getInstance().getPlayer(playerId);
				if (player != null)
				{
					if (player.isDead())
					{
						player.doRevive();
					}

					player.heal();

					player.setTeam(0);
					player.setIsInsideGMEvent(false);
					player.teleToLocation(this.eventTeleport, true);

					ParticipateRegister register = this.participatedRegister.get(playerId);
					if (register != null)
					{
						register.increaseParticipatedTimes();
					}
					else
					{
						this.participatedRegister.put(playerId,
								new ParticipateRegister(playerId, player.getExternalIP(), player.getInternalIP()));
					}
				}
			}
		}

		private void kickPlayer(int playerId)
		{
			if (hasAcceptedRules(playerId))
			{
				this.acceptedEventRules.remove(playerId);
			}

			L2PcInstance toKick = L2World.getInstance().getPlayer(playerId);
			if (toKick != null)
			{
				toKick.teleToLocation(TeleportWhereType.Town);
			}
		}

		private void giveReward(int team, int count)
		{
			synchronized (this.rewardedPlayers)
			{
				for (Entry<Integer, Integer> i : this.participants.entrySet())
				{
					if (i.getValue() != team)
					{
						continue;
					}

					L2PcInstance player = L2World.getInstance().getPlayer(i.getKey());
					if (player == null)
					{
						continue;
					}

					if (this.rewardedPlayers.contains(player.getObjectId()))
					{
						continue;
					}

					this.rewardedPlayers.add(player.getObjectId());

					player.addItem("GMEvent", rewardCoinId, count, player, true);

					GmListTable.broadcastMessageToGMs(
							"GMEvent: " + player.getName() + " has been rewarded with " + count + " Apigas!");
					Util.logToFile(this.gmName + ": " + player.getName() + " has been rewarded with " + count + " Apigas!",
							"GMEvents", true);
				}
			}
		}
	}

	private void sendRewardMail(int playerId, int itemId, long amount, String message)
	{
		Message msg = new Message(-1, playerId, false, "GMEvent", message, 0);

		Mail attachments = msg.createAttachments();
		attachments.addItem("GMEvent", itemId, amount, null, null);

		MailManager.getInstance().sendMessage(msg);

		String playerName = CharNameTable.getInstance().getNameById(playerId);
		if (message.contains("Refound"))
		{
			Util.logToFile("GMEvent Bet: " + playerName + " get his bet refounded " + amount + " " +
					this.currencies.get(itemId).getName() + "", "GMEvents", true);
		}
		else
		{
			Util.logToFile("GMEvent Bet: " + playerName + " has been rewarded with " + amount + " " +
					this.currencies.get(itemId).getName() + "", "GMEvents", true);
		}
	}

	private L2ColosseumFence addDynamic(int x, int y, int z, int minZ, int maxZ, int width, int height)
	{
		L2ColosseumFence fence = new L2ColosseumFence(0, x, y, z, minZ, maxZ, width, height, FenceState.CLOSED);
		fence.spawnMe();
		return fence;
	}

	public void onKill(L2Character killer, L2Character killed)
	{
		if (killer == null || killed == null || this.currentEvent == null)
		{
			return;
		}

		if (this.currentEvent.isStarted() && this.currentEvent.isFightStarted())
		{
			if (killer.getActingPlayer() != null && killed.getActingPlayer() != null)
			{
				this.currentEvent.sendPacketToWaitingPlayers(new ExShowScreenMessage(
						killed.getActingPlayer().getName() + " has been killed by " +
								killer.getActingPlayer().getName() + "!", 2000));

				//Winner animation only if is 1vs1
				if (this.currentEvent.getParticipants().size() == 2)
				{
					killer.broadcastPacket(new SocialAction(killer.getActingPlayer().getObjectId(), 22));
				}
			}
		}
	}

	//Lets auto control who enter to the zone instead of kick every one who illegally enter, also banned ppl
	public boolean onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (character == null)
		{
			return true;
		}

		L2PcInstance player = character.getActingPlayer();
		if (player == null)
		{
			return true;
		}

		if (this.currentEvent != null && this.currentEvent.isStarted() && !player.isGM())
		{
			if (zone instanceof L2ArenaZone && this.currentEvent.getArenaZones().contains(zone))
			{
				if (!player.getIsInsideGMEvent())
				{
					this.currentEvent.kickPlayer(character.getObjectId());
					return false;
				}
			}
			else if (zone instanceof L2PeaceZone && this.currentEvent.getPeaceZones().contains(zone))
			{
				if (this.currentEvent.isBannedIp(player.getExternalIP()))
				{
					player.sendPacket(new ExShowScreenMessage(" ", 5000));
					this.currentEvent.kickPlayer(character.getObjectId());
					return false;
				}

				if (!this.currentEvent.hasAcceptedRules(player.getObjectId()))
				{
					player.sendPacket(new ExShowScreenMessage(
							"You should accept the Event rules (on the Arena Sign) before enter to the zone! ", 5000));
					player.teleToLocation(this.currentEvent.getTeleportLocation(), true);
					return false;
				}
			}
		}

		return true;
	}

	public void onNpcTalk(L2Object obj, L2PcInstance player)
	{
		if (obj == null || player == null || this.currentEvent == null)
		{
			return;
		}

		if (this.currentEvent.isStarted())
		{
			if (obj instanceof L2StaticObjectInstance &&
					this.currentEvent.getArenaSignIds().contains(((L2StaticObjectInstance) obj).getStaticObjectId()) ||
					obj instanceof L2NpcInstance && ((L2NpcInstance) obj).getNpcId() == this.dummyArenaSignNpcId)
			{
				CustomCommunityBoard.getInstance().parseCmd("bypass this.bbscustom;info;gmEventRules", player);
			}
		}
	}

	public boolean canAttack(L2PcInstance attacker, L2Object target)
	{
		if (attacker == null || target == null)
		{
			return false;
		}

		L2PcInstance tgt = null;
		if (target instanceof L2Summon)
		{
			tgt = ((L2Summon) target).getOwner();
		}
		else if (target instanceof L2PcInstance)
		{
			tgt = (L2PcInstance) target;
		}

		if (attacker == tgt)
		{
			return true;
		}

		if (tgt != null)
		{
			return tgt.getTeam() != attacker.getTeam();
		}

		return false;
	}

	private void loadPredefinedEvents()
	{
		File file = new File(Config.DATAPACK_ROOT, "data_" + Config.SERVER_NAME + "/predefinedGMEvents.xml");
		if (!file.exists())
		{
			return;
		}

		XmlDocument doc = new XmlDocument(file);
		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				for (XmlNode d : n.getChildren())
				{
					if (d.getName().equalsIgnoreCase("currency"))
					{
						int itemId = d.getInt("itemId");
						this.currencies.put(itemId, new CurrencyInfo(itemId));
					}
					else if (d.getName().equalsIgnoreCase("event"))
					{
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
						String[] arenaSignsIds =
								d.hasAttribute("arenaSigns") ? d.getString("arenaSigns").split(",") : null;
						String[] arenaSignSpawnOne =
								d.hasAttribute("arenaSignSpawnOne") ? d.getString("arenaSignSpawnOne").split(",") :
										null;
						String[] arenaSignSpawnTwo =
								d.hasAttribute("arenaSignSpawnTwo") ? d.getString("arenaSignSpawnTwo").split(",") :
										null;

						List<Integer> doorList = new ArrayList<>();
						List<L2ArenaZone> arenaZones = new ArrayList<>();
						List<L2PeaceZone> peaceZones = new ArrayList<>();
						List<Integer> arenaSignList = new ArrayList<>();
						List<Location> arenaSignSpawnList = new ArrayList<>();

						if (doors != null)
						{
							for (String door : doors)
							{
								doorList.add(Integer.valueOf(door));
							}
						}

						for (String zone : arenaZoneNames)
						{
							arenaZones.add(ZoneManager.getInstance().getZoneByName(zone, L2ArenaZone.class));
						}

						for (String zone : peaceZoneNames)
						{
							peaceZones.add(ZoneManager.getInstance().getZoneByName(zone, L2PeaceZone.class));
						}

						if (arenaSignsIds != null)
						{
							for (String sign : arenaSignsIds)
							{
								arenaSignList.add(Integer.valueOf(sign));
							}
						}

						if (arenaSignSpawnOne != null)
						{
							arenaSignSpawnList.add(new Location(Integer.valueOf(arenaSignSpawnOne[0]),
									Integer.valueOf(arenaSignSpawnOne[1]), Integer.valueOf(arenaSignSpawnOne[2])));
						}
						if (arenaSignSpawnTwo != null)
						{
							arenaSignSpawnList.add(new Location(Integer.valueOf(arenaSignSpawnTwo[0]),
									Integer.valueOf(arenaSignSpawnTwo[1]), Integer.valueOf(arenaSignSpawnTwo[2])));
						}

						Location spawnTeamOne = new Location(Integer.valueOf(spawnTeamOneCords[0]),
								Integer.valueOf(spawnTeamOneCords[1]), Integer.valueOf(spawnTeamOneCords[2]),
								Integer.valueOf(spawnTeamOneCords[3]));
						Location spawnTeamTwo = new Location(Integer.valueOf(spawnTeamTwoCords[0]),
								Integer.valueOf(spawnTeamTwoCords[1]), Integer.valueOf(spawnTeamTwoCords[2]),
								Integer.valueOf(spawnTeamTwoCords[3]));
						Location bufferSpawnTeamOne = new Location(Integer.valueOf(spawnBufferTeamOneCords[0]),
								Integer.valueOf(spawnBufferTeamOneCords[1]),
								Integer.valueOf(spawnBufferTeamOneCords[2]),
								Integer.valueOf(spawnBufferTeamOneCords[3]));
						Location bufferSpawnTeamTwo = new Location(Integer.valueOf(spawnBufferTeamTwoCords[0]),
								Integer.valueOf(spawnBufferTeamTwoCords[1]),
								Integer.valueOf(spawnBufferTeamTwoCords[2]),
								Integer.valueOf(spawnBufferTeamTwoCords[3]));
						Location eventSpawmn =
								new Location(Integer.valueOf(eventSpawnCords[0]), Integer.valueOf(eventSpawnCords[1]),
										Integer.valueOf(eventSpawnCords[2]), Integer.valueOf(eventSpawnCords[3]));

						this.predefinedEvents.put(name,
								new Event(name, description, locationName, doorList, arenaZones, peaceZones,
										spawnTeamOne, spawnTeamTwo, bufferSpawnTeamOne, bufferSpawnTeamTwo, eventSpawmn,
										arenaSignSpawnList, arenaSignList));
					}
					else if (d.getName().equalsIgnoreCase("subEvent"))
					{
						String eventName = d.getString("eventName");
						String startBypass = d.getString("startBypass");
						String endBypass = d.getString("endBypass");
						this.subEvents.put(eventName, new SubEvent(eventName, startBypass, endBypass));
					}
				}
			}
		}

		Log.info("GMEventManager: loaded " + this.predefinedEvents.size() + " predefinied events " + this.currencies.size() +
				" bet currencies and " + this.subEvents.size() + " sub events!");
	}

	private GMEventManager()
	{
		loadPredefinedEvents();
	}

	public static GMEventManager getInstance()
	{
		return SingletonHolder.instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final GMEventManager instance = new GMEventManager();
	}
}
