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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import l2server.Config;
import l2server.gameserver.Announcements;
import l2server.gameserver.GmListTable;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.bots.BotMode;
import l2server.gameserver.bots.BotsManager;
import l2server.gameserver.communitybbs.Manager.CustomCommunityBoard;
import l2server.gameserver.datatables.HeroSkillTable;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.datatables.PlayerClassTable;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2NpcBufferInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.olympiad.OlympiadManager;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.Earthquake;
import l2server.gameserver.network.serverpackets.ExSendUIEvent;
import l2server.gameserver.network.serverpackets.ExSendUIEventRemove;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;
import l2server.gameserver.util.Broadcast;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

/**
 * @author LasTravel
 */
public class CustomWarAreas
{
	private static Map<Integer, Map<Integer, Assist>> _playerAssists 	= new HashMap<Integer, Map<Integer, Assist>>();
	private static Map<Integer, Location> _playerPositions 				= new HashMap<Integer, Location>();
	private static List<ZoneInfo> _zoneInfo 							= new ArrayList<ZoneInfo>();
	private static WarZoneState _zoneState 								= WarZoneState.IDLE;
	private static WarZone _warZone;
	
	private static long _lastStartedTime;
	private static long _nextStartTime;
	
	//Configs
	private static final boolean BOTS_ENABLED = false;
	private static final int AMOUNT_OF_BOTS = 30;
	private static final String BOT_USAGE_NAME = "WarAreas";
	private static final int ADD_BOT_DELAY = 4;//Minutes
	
	private static final boolean ALLOW_DUAL_BOX = false;
	private static final boolean ASSIST_SYSTEM = true;
	private static final int MIN_ASSIST_DAMAGE_TO_GET_REWARD = 800;
	private static final int ASSIST_TIME_TO_GET_REWARD = 5;	//Seconds
	private static final int FIRST_EVENT_START = 90;//Minutes
	private static final int RESPAWN_DELAY = 3;//Seconds
	private static final int EVENT_DURATION = 20; //Minutes
	private static final int EVENT_DELAY = 90; //Minutes
	private static final int BALANCE_DELAY = 3;	//Minutes
	
	private enum WarZoneState
	{
		IDLE,
		OPEN,
	}
	
	private enum BalanceType
	{
		ON_ENTER,
		ON_KILL
	}
	
	private class Assist
	{
		private L2PcInstance _player;
		private long _damageDone;
		private long _lastAttackTime;
		
		private Assist(L2PcInstance player, long initialDamage)
		{
			_player = player;
			_damageDone = initialDamage;
		}
		
		private long getDamageDone()
		{
			return _damageDone;
		}
		
		private long getLastAttackTime()
		{
			return _lastAttackTime;
		}
		
		private void increaseDamage(long damage)
		{
			_damageDone += damage;
			_lastAttackTime = System.currentTimeMillis();
		}
		
		private L2PcInstance getPlayer()
		{
			return _player;
		}
	}
	
	private class PlayerStats
	{
		private int _playerKills;
		private int _playerDeaths;
		private int _playerAssists;
		
		private PlayerStats(int playerKills, int playerDeaths, int playerAssists)
		{
			_playerKills = playerKills;
			_playerDeaths = playerDeaths;
			_playerAssists = playerAssists;
		}
		
		private void increasePlayerKills()
		{
			_playerKills ++;
		}
		
		private void increasePlayerAssists()
		{
			_playerAssists ++;
		}
		
		private void increasePlayerDeaths()
		{
			_playerDeaths ++;
		}
		
		private int getPlayerDeaths()
		{
			return _playerDeaths;
		}
		
		private int getPlayerAssists()
		{
			return _playerAssists;
		}
		
		private int getPlayerKills()
		{
			return _playerKills;
		}
	}
	
	private class ZoneInfo
	{
		private String _zoneName;
		private String _instanceTemplate;
		private int _zoneImage;
		private List<Location> _redTeamSpawns;
		private List<Location> _blueTeamSpawns;
		
		private ZoneInfo(String zoneName, String instanceTemplate, int zoneImage, List<Location> redTeamSpawns, List<Location> blueTeamSpawns)
		{
			_zoneName = zoneName;
			_instanceTemplate = instanceTemplate;
			_zoneImage = zoneImage;
			_redTeamSpawns = redTeamSpawns;
			_blueTeamSpawns = blueTeamSpawns;
		}
		
		private String getName()
		{
			return _zoneName;
		}
		
		private String getInstanceTemplate()
		{
			return _instanceTemplate;
		}
		
		private int getImageId()
		{
			return _zoneImage;
		}
		
		private Location getRandomSpawn(int team)
		{
			return getTeamSpawns(team).get(Rnd.get(getTeamSpawns(team).size()));
		}
		
		private List<Location> getTeamSpawns(int team)
		{
			if (team == 1)
				return _blueTeamSpawns;
			return _redTeamSpawns;
		}
	}
	
	private class WarZone
	{
		private long _lastBalancedTime;
		private ZoneInfo _zone;
		private int _instanceId;
		private int _redPoints;
		private int _bluePoints;
		private List<L2PcInstance> _balanceQueue;
		private Map<Integer, PlayerStats> _playerPoints;
		private List<L2PcInstance> _allPlayers;
		private List<L2PcInstance> _redTeam;
		private List<L2PcInstance> _blueTeam;

		private WarZone()
		{
			_zone 			= _zoneInfo.get(Rnd.get(_zoneInfo.size()));
			_allPlayers		= new ArrayList<L2PcInstance>();
			_redTeam 		= new ArrayList<L2PcInstance>();
			_blueTeam 		= new ArrayList<L2PcInstance>();
			_playerPoints 	= new HashMap<Integer, PlayerStats>();
			_balanceQueue 	= new ArrayList<L2PcInstance>();
			_lastBalancedTime = System.currentTimeMillis();
		}
		
		private void prepareInstance()
		{
			_instanceId = InstanceManager.getInstance().createDynamicInstance(_zone.getInstanceTemplate());
			if (_instanceId != 0)
				InstanceManager.getInstance().getInstance(_instanceId).setPvPInstance(true);
		}

		private void addPlayer(L2PcInstance player, int teamToGo)
		{
			if (player == null)
				return;
			
			player.setTeam(teamToGo);
			
			if (teamToGo == 1)
			{
				synchronized(_blueTeam)
				{
					_blueTeam.add(player);
				}
			}
			else
			{
				synchronized(_redTeam)
				{
					_redTeam.add(player);
				}
			}

			synchronized(_allPlayers)
			{
				_allPlayers.add(player);
			}
		}
		
		private void setLastBalancedTime(long time)
		{
			_lastBalancedTime = time;
		}
		
		private int teamToBalance(BalanceType balanceType)
		{
			int teamToGo = 0;
			int blueTeamSize = _blueTeam.size();
			int redTeamSize = _redTeam.size();
			
			if (balanceType == BalanceType.ON_KILL)
			{
				//Balance delay
				if (_lastBalancedTime + BALANCE_DELAY * 60000 > System.currentTimeMillis())
					return 0;
				
				//Already balancing..
				synchronized(_balanceQueue)
				{
					if (!_balanceQueue.isEmpty())
						return 0;
				}
			}
			
			if (balanceType == BalanceType.ON_ENTER)
			{
				//Team with less players, otherwise go to the team with less points
				if (blueTeamSize > redTeamSize)
					teamToGo = 2;
				else if (blueTeamSize < redTeamSize)
					teamToGo = 1;
				else if (blueTeamSize == redTeamSize)
				{
					if (_bluePoints > _redPoints)
						teamToGo = 2;
					else if (_redPoints > _bluePoints)
						teamToGo = 1;
					else
						teamToGo = Rnd.get(1, 2);
				}
			}
			
			if (balanceType == BalanceType.ON_KILL)
			{
				//Team with less players but with enough players on its team
				if (blueTeamSize > 1 && blueTeamSize > redTeamSize)
					teamToGo =  2;
				else if (redTeamSize > 1 && redTeamSize > blueTeamSize)
					teamToGo =  1;
			}
			
			return teamToGo;
		}
		
		private void teleToRandomPoints(L2PcInstance player)
		{
			if (player == null)
				return;
			
			Location randomSpawn = _warZone.getZone().getRandomSpawn(player.getTeam());
			if (randomSpawn == null)
				return;
			
			if (player.isDead())
				player.doRevive();
			
			player.setProtection(true);
			player.teleToLocation(randomSpawn, true);
			player.heal();
			
			//Cancel debuffs
			for (L2Abnormal eff : player.getAllDebuffs())
			{
				if (eff == null)
					continue;
				eff.exit();
			}
		}
		
		private void removePlayer(L2PcInstance player, boolean playerLeave)
		{
			if (player == null)
				return;
			
			if (player.getTeam() == 1)
			{
				synchronized(_blueTeam)
				{
					_blueTeam.remove(player);
				}
			}
			else
			{
				synchronized(_blueTeam)
				{
					_redTeam.remove(player);
				}
			}
	
			synchronized(_allPlayers)
			{
				_allPlayers.remove(player);
			}
			
			if (playerLeave)
			{
				synchronized(_playerPoints)
				{
					if (_playerPoints.containsKey(player.getObjectId()))
						_playerPoints.remove(player.getObjectId());
				}
			}
			
			synchronized(_balanceQueue)
			{
				if(_balanceQueue.contains(player))
					_balanceQueue.remove(player);
			}
			player.setTeam(0);
		}
		
		private void increaseTeamPoints(int team)
		{
			if (team == 1)
				_bluePoints++;
			else
				_redPoints++;
		}
		
		private void balancePlayer(L2PcInstance player, int balanceTeam, boolean forceUpdate)
		{
			if (player == null)
				return;

			synchronized(_balanceQueue)
			{
				_balanceQueue.add(player);
			}
			
			InstanceManager.getInstance().sendPacket(_warZone.getInstanceId(), new ExShowScreenMessage("Balancing the teams...", 5000));
			
			player.sendPacket(new CreatureSay(0, 2, "Balance System: ", "You will be moved to the other team...!"));
			
			ThreadPoolManager.getInstance().scheduleGeneral(new balancePlayer(player, balanceTeam, forceUpdate), RESPAWN_DELAY * 1000);
		}
		
		private void increasePlayerPoints(L2PcInstance player, boolean isKill, boolean isDeath, boolean isAssist)
		{
			if (player == null)
				return;
			
			synchronized(_playerPoints)
			{
				if (_playerPoints.containsKey(player.getObjectId()))
				{
					if (isKill)
						_playerPoints.get(player.getObjectId()).increasePlayerKills();
					
					if (isDeath)
						_playerPoints.get(player.getObjectId()).increasePlayerDeaths();
					
					if (isAssist)
						_playerPoints.get(player.getObjectId()).increasePlayerAssists();
				}
				else
				{
					if (isKill)
						_playerPoints.put(player.getObjectId(), new PlayerStats(1, 0, 1));
					
					if (isDeath)
						_playerPoints.put(player.getObjectId(), new PlayerStats(0, 1, 0));
					
					if (isAssist)
						_playerPoints.put(player.getObjectId(), new PlayerStats(0, 0, 1));	
				}
			}	
		}
		
		private int getInstanceId()
		{
			return _instanceId;
		}
		
		private int getPlayerPoints(int objId, boolean isKill, boolean isDeath, boolean isAssist)
		{
			int points = 0;
			synchronized(_playerPoints)
			{
				if (_playerPoints.containsKey(objId))
				{
					if (isKill)
						return _playerPoints.get(objId).getPlayerKills();
					
					if (isDeath)
						return _playerPoints.get(objId).getPlayerDeaths();
					
					return _playerPoints.get(objId).getPlayerAssists();
				}
			}
			return points;
		}
		
		private int getTeamPoints(int team)
		{
			if (team == 1)
				return _bluePoints;
			return _redPoints;
		}
		
		private List<L2PcInstance> getAllPlayers()
		{
			return _allPlayers;
		}
		
		private ZoneInfo getZone()
		{
			return _zone;
		}

		private boolean isBalancingPlayer(L2PcInstance _player)
		{
			synchronized(_balanceQueue)
			{
				return _balanceQueue.contains(_player);
			}
		}
		
		private void onEnterPlayer(L2PcInstance player)
		{
			if (player == null)
				return;
			
			//We choose the team with less members
			int teamToGo = teamToBalance(BalanceType.ON_ENTER);
			
			Location position = new Location(player.getX(), player.getY(), player.getZ());
			if (player.isBot())
				position = new Location(-113436, -244342, -15540);
			
			if (teamToGo != 0)
			{
				//Save the player location
				synchronized(_playerPositions)
				{
					_playerPositions.put(player.getObjectId(), position);
				}
				
				if (player.isInParty())
					player.leaveParty();
				
				player.setIsInsideWarZone(true);
				player.setInstanceId(_warZone.getInstanceId());
				
				//Delete the clan skills and etc
				if (player.getClan() != null)
				{
					player.getClan().removeSkillEffects(player);
					if (player.getClan().getHasCastle() > 0)
						CastleManager.getInstance().getCastleByOwner(player.getClan()).removeResidentialSkills(player);
					if (player.getClan().getHasFort() > 0)
						FortManager.getInstance().getFortByOwner(player.getClan()).removeResidentialSkills(player);
				}
				player.sendSkillList();
				
				addPlayer(player, teamToGo);
				teleToRandomPoints(player);
				
				player.sendPacket(new ExSendUIEvent(0, 0, (int)(TimeUnit.MILLISECONDS.toSeconds((_lastStartedTime + EVENT_DURATION * 60000) - System.currentTimeMillis())) , 0, "Remaining time..."));
			}
		}
	}
	
	public String getCustomWarAreas(L2PcInstance player, int pageToShow)
	{
		StringBuilder sb = new StringBuilder();
		int blueSize = String.valueOf(_warZone.getTeamPoints(1)).length();
		int redSize = String.valueOf(_warZone.getTeamPoints(2)).length();
		int size = 32 * Math.max(redSize, blueSize);
		sb.append("<table width=600><tr><td FIXWIDTH="+size+"><br>"+generatePointsTable(2)+"</td><td align=center FIXWIDTH=512><img src=\"Crest.crest_%serverId%_"+_warZone.getZone().getImageId()+"\" width=512 height=128></td><td FIXWIDTH="+size+"><br>"+generatePointsTable(1)+"</td></tr></table>");
		sb.append("<br>");
		
		if (_zoneState == WarZoneState.OPEN)
		{
			if (player.getIsInsideWarZone())
				sb.append("<button action=\"bypass _bbscustom;action;WarArea;leave\" value=Leave! width=512 height=20 back=L2UI_ct1.button_df fore=L2UI_ct1.button_df></button>");
			else
				sb.append("<button action=\"bypass _bbscustom;action;WarArea;join\" value=\"Join "+_warZone.getZone().getName()+"!\" width=512 height=20 back=L2UI_ct1.button_df fore=L2UI_ct1.button_df></button>");
		}
		
		if (_zoneState == WarZoneState.IDLE)
		{
			long time = _nextStartTime - System.currentTimeMillis();
			int seconds = (int) (time / 1000) % 60 ;
			int minutes = (int) ((time / (1000*60)) % 60);
			int hours   = (int) ((time / (1000*60*60)) % 24);
			
			sb.append("<br><table width=600><tr><td FIXWIDTH=600 align=center><font name=hs12>Next start in: "+hours+":"+minutes+":"+seconds+"</font></td></tr></table>");
			return sb.toString();
		}
		
		sb.append("<br>");
		
		int maxPlayersPerPage = 20;
		int playersSize = _warZone.getAllPlayers().size();
		int maxPages = playersSize / maxPlayersPerPage;
		if (playersSize > (maxPlayersPerPage * maxPages))
			maxPages++;
		if (pageToShow > maxPages)
			pageToShow = maxPages;
		int pageStart = maxPlayersPerPage * pageToShow;
		int pageEnd = playersSize;
		if ((pageEnd - pageStart) > maxPlayersPerPage)
			pageEnd = pageStart + maxPlayersPerPage;
		
		if (maxPages > 1)
			sb.append(CustomCommunityBoard.getInstance().createPages(pageToShow, maxPages, "_bbscustom;warZone;", ""));
		
		sb.append("<br><table width=600 bgcolor=999999><tr><td FIXWIDTH=200>Name</td><td FIXWIDTH=200>Class</td><td FIXWIDTH=100 align=center>Kills</td><td FIXWIDTH=100 align=center>Deaths</td<td FIXWIDTH=100 align=center>Assists</td>><td FIXWIDTH=100 align=center>Team</td></tr></table>");
		
		boolean isGM = player.isGM();
		for (int i = pageStart; i < pageEnd; i++)
		{
			L2PcInstance pl = _warZone.getAllPlayers().get(i);
			if (pl == null)
				continue;
			boolean haveColor = pl.getName().equalsIgnoreCase(player.getName());
			String team = pl.getTeam() == 1 ? "<font color=3366FF>Blue</font>" : "<font color=FF0000>Red</font>";
			String name = haveColor || isGM ? pl.getName() : "NoName";
			if (isGM)
			{
				if (pl.isBot())
					name +="*";
			}
			sb.append("<table width=600><tr><td FIXWIDTH=200>"+(haveColor ? "<font color=LEVEL>" : "")+""+name+"</td><td FIXWIDTH=200>"+PlayerClassTable.getInstance().getClassNameById(pl.getClassId())+"</td><td FIXWIDTH=100 align=center>"+_warZone.getPlayerPoints(pl.getObjectId(), true, false, false)+"</td><td FIXWIDTH=100 align=center>"+_warZone.getPlayerPoints(pl.getObjectId(), false, true, false)+"</td><td FIXWIDTH=100 align=center>"+_warZone.getPlayerPoints(pl.getObjectId(), false, false, true)+" "+(haveColor ? "</font>" : "")+"</td><td FIXWIDTH=100 align=center>"+team+"</td></tr></table>");
			sb.append("<img src=\"L2UI.Squaregray\" width=600 height=1>");
		}
		return sb.toString();
	}
	
	private String generatePointsTable(int team)
	{
		int pointLengh = String.valueOf(_warZone.getTeamPoints(team)).length();
		String pointTable = "";
		pointTable += "<table background=L2UI_CH3.refinewnd_back_Pattern width=100 height=100><tr><td><br>";
		String pointType = team == 1 ? "<font name=hs12 color=3366FF>Blue</font>" : "<font name=hs12 color=FF0000>Red</font>";
		pointTable += "<center><table width=60><tr><td align=center>"+pointType+"</td></tr></table></center>";
		pointTable += "<table><tr><td><table><tr><td FIXWIDTH="+(32 * pointLengh)+" align=center>";
		pointTable += "<table><tr>";
		for (String i : String.valueOf(_warZone.getTeamPoints(team)).split(""))
		{
			if (i.isEmpty())
				continue;
			pointTable += "<td align=center><img src=\"L2UI_CT1.MiniGame_DF_Text_Score_"+i+"\" width=16 height=32/></td>";
		}
		pointTable += "</tr></table>";
		pointTable += "</td></tr></table></td></tr></table>";
		pointTable +="</td></tr></table>";
		return pointTable;
	}
	
	public void onEnterZone(L2PcInstance player)
	{
		if (player == null)
			return;
		
		if (!checkEnterConditions(player))
			return;
		
		_warZone.onEnterPlayer(player);
	}
	
	public void onKill(L2PcInstance killer, L2PcInstance killed)
	{
		if (_warZone.isBalancingPlayer(killer) || _warZone.isBalancingPlayer(killed))
			return;
		
		//Points
		_warZone.increaseTeamPoints(killer.getTeam());
		_warZone.increasePlayerPoints(killer, true, false, false);
		_warZone.increasePlayerPoints(killed, false, true, false);
		
		//Increase the PvP points
		killer.increasePvpKills(killed);
		
		//Manage the balance
		int balanceTeam = _warZone.teamToBalance(BalanceType.ON_KILL);
		if (balanceTeam != 0)
			_warZone.balancePlayer(killer, balanceTeam, true);
		
		//Manage rewards
		boolean canGiveReward = _warZone.getAllPlayers().size() / 2 > 3;
		if (ASSIST_SYSTEM)
		{
			synchronized(_playerAssists)
			{
				if (_playerAssists.containsKey(killed.getObjectId()))
				{
					for (Entry<Integer, Assist> i : _playerAssists.get(killed.getObjectId()).entrySet())
					{
						Assist assist = i.getValue();
						L2PcInstance assistPlayer = assist.getPlayer();
						if (assistPlayer == null)
							continue;
						
						if (assist.getDamageDone() < MIN_ASSIST_DAMAGE_TO_GET_REWARD || killer.getObjectId() == assistPlayer.getObjectId() || !assistPlayer.getIsInsideWarZone() ||
								killed.getTeam() == assistPlayer.getTeam() || !Util.checkIfInRange(1600, killed, assistPlayer, false))
							continue;
						
						if ((System.currentTimeMillis() - ASSIST_TIME_TO_GET_REWARD * 1000) < assist.getLastAttackTime())
						{
							int rnd = Rnd.get(100);
							if (rnd < 5 && canGiveReward)
							{
								int cardId = Rnd.get(38074, 38085);
								assistPlayer.addItem("CustomWarAreas", cardId, 1, null, true);
								
								GmListTable.broadcastMessageToGMs("CustomWarAreas: " + assistPlayer.getName() + " get one card!");
								Util.logToFile(assistPlayer.getName()+"->"+killed.getName()+" assist rewarded with "+ ItemTable.getInstance().getTemplate(cardId).getName(), "LegendaryCards", true);
							}
							_warZone.increasePlayerPoints(assist.getPlayer(), false, false, true);
						}
					}
				}
			}
		}
		
		//37586 hidden card
		
		//Killer reward
		if (killed != killer)
		{	
			int rnd = Rnd.get(100);
			if (rnd < 10 && canGiveReward)
			{
				int cardId = Rnd.get(38074, 38085);
				killer.addItem("CustomWarAreas", cardId, 1, null, true);
				
				GmListTable.broadcastMessageToGMs("CustomWarAreas: " + killer.getName() + " get one card!");
				Util.logToFile(killer.getName()+"->"+killed.getName()+" kill rewarded with "+ ItemTable.getInstance().getTemplate(cardId).getName(), "LegendaryCards", true);
			}
		}
		_playerAssists.remove(killed.getObjectId());
		
		//Respawn
		ThreadPoolManager.getInstance().scheduleGeneral(new respawnPlayer(killed), RESPAWN_DELAY * 1000);
	}
	
	private class balancePlayer implements Runnable
	{
		private L2PcInstance _player;
		private boolean _forceUpdate;
		private int _teamToGo;
	
		private balancePlayer(L2PcInstance player, int teamToGo, boolean forceUpdate)
		{
			_player = player;
			_teamToGo = teamToGo;
			_forceUpdate = forceUpdate;
		}

		public void run()
		{
			if (_player != null && _player.getIsInsideWarZone() && _warZone.isBalancingPlayer(_player) && _zoneState == WarZoneState.OPEN)
			{
				if (_player.isInParty())
					_player.leaveParty();
				
				_warZone.removePlayer(_player, false);
				_warZone.addPlayer(_player, _teamToGo);
				_warZone.teleToRandomPoints(_player);
				
				//We only will update it if the balance doesn't come from the leave way
				if (_forceUpdate)
					_warZone.setLastBalancedTime(System.currentTimeMillis());
			}
		}
	}
	
	private class respawnPlayer implements Runnable
	{
		private L2PcInstance _player;
	
		private respawnPlayer(L2PcInstance player)
		{
			_player = player;
		}

		public void run()
		{
			if (_player != null && _player.getIsInsideWarZone() && _zoneState == WarZoneState.OPEN)
				_warZone.teleToRandomPoints(_player);		
		}	
	}
	
	public void onExitZone(L2PcInstance player, boolean force)
	{
		if (player == null)
			return;
		
		if (!force && !checkLeaveConditions(player))
			return;
		
		_warZone.removePlayer(player, true);
		
		player.setIsInsideWarZone(false);
		player.setInstanceId(0);
		
		Location loc = new Location(83497, 148639, -3409);	//Giran
		synchronized(_playerPositions)
		{
			if (_playerPositions.containsKey(player.getObjectId()))
				loc = _playerPositions.get(player.getObjectId());
		}
		
		
		// Add Clan Skills
		if (player.getClan() != null)
		{
			player.getClan().addSkillEffects(player);
			if (player.getClan().getHasCastle() > 0)
				CastleManager.getInstance().getCastleByOwner(player.getClan()).giveResidentialSkills(player);
			if (player.getClan().getHasFort() > 0)
				FortManager.getInstance().getFortByOwner(player.getClan()).giveResidentialSkills(player);
		}
		
		// Add Hero Skills
		if (player.isHero() && player.getClassId() == player.getBaseClass())
		{
			for (L2Skill skill : HeroSkillTable.getHeroSkills())
				player.addSkill(skill, false);
		}
		
		player.sendSkillList();
		
		if (player.isBot())
			player.getBotController().stopController();
			
		if (player.isDead())
			player.doRevive();
			
		player.heal();
		
		if (!player.isOnline())
			player.setXYZInvisible(loc.getX(), loc.getY(), loc.getZ());
		else
			player.teleToLocation(loc, true);
			
		player.sendPacket(new ExSendUIEventRemove());
	}	
	
	private boolean checkLeaveConditions (L2PcInstance player)
	{
		if (player == null)
			return false;
		
		if (_zoneState == WarZoneState.IDLE)
			return true;
		
		if (_warZone.isBalancingPlayer(player) || player.isInCombat() || player.getInstanceId() == 0 || !player.getIsInsideWarZone() || player.isOutOfControl() ||
				AttackStanceTaskManager.getInstance().getAttackStanceTask(player))
			return false;
		return true;
	}
	
	private boolean checkEnterConditions(L2PcInstance player)
	{
		if (player == null)
			return false;
		
		if (_zoneState != WarZoneState.OPEN)
			return false;
		
		if (player.isBot())
			return true;
		
		if (player.getLevel() < 99 || player.isInCombat() || player.getPvpFlag() > 0 || player.getInstanceId() != 0 || player.isInDuel() ||
				player.isFakeDeath() || player.isOutOfControl() || player.isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(player)|
				player.getIsInsideWarZone() || AttackStanceTaskManager.getInstance().getAttackStanceTask(player) || player.getEvent() != null)
			return false;
		
		//DualBox Checks
		if (!ALLOW_DUAL_BOX)
		{
			for (L2PcInstance pl : _warZone.getAllPlayers())
			{
				if (pl == null)
					continue;
				if (pl.getExternalIP().equalsIgnoreCase(player.getExternalIP()) && pl.getInternalIP().equalsIgnoreCase(player.getInternalIP()))
					return false;
			}
		}
		return true;
	}
	
	private void load()
	{
		File file = new File(Config.DATAPACK_ROOT, "data_" + Config.SERVER_NAME + "/WarAreas.xml");
		XmlDocument doc = new XmlDocument(file);
		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				for (XmlNode d : n.getChildren())
				{
					if (d.getName().equalsIgnoreCase("warZone"))
					{
						String name = d.getString("name");
						String instanceTemplate = d.getString("instanceTemplate");
						int imageId = d.getInt("imageId");
						
						List<Location> redTeamSpawns = new ArrayList<Location>();
						List<Location> blueTeamSpawns = new ArrayList<Location>();
						for (XmlNode a : d.getChildren())
						{
							if (a.getName().equalsIgnoreCase("blueTeamSpawns") || a.getName().equalsIgnoreCase("redTeamSpawns"))
							{
								for (XmlNode b : a.getChildren())
								{
									if (b.getName().equalsIgnoreCase("spawn"))
									{	
										if (a.getName().equalsIgnoreCase("redTeamSpawns"))
											redTeamSpawns.add(new Location(b.getInt("x"), b.getInt("y"), b.getInt("z")));
										else
											blueTeamSpawns.add(new Location(b.getInt("x"), b.getInt("y"), b.getInt("z")));
									}
								}
							}
						}
						_zoneInfo.add(new ZoneInfo(name, instanceTemplate, imageId, redTeamSpawns, blueTeamSpawns));
					}
				}
			}
		}
		Log.info("WarAreas: Loaded: " + _zoneInfo.size() + " war zones!");
	}
	
	//Will be used to know if any player can attack other one inside the zone or if
	//the effects should affect the target
	public boolean canAttack(L2PcInstance attacker, L2Object target, L2Skill skill)
	{
		if (attacker == null || target == null)
			return false;
		
		L2PcInstance tgt = null;
		if (target instanceof L2Summon)
			tgt = ((L2Summon)target).getOwner();
		else if (target instanceof L2PcInstance)
			tgt = ((L2PcInstance)target);
		
		if (attacker == tgt)
			return true;
		
		if (tgt != null)
		{
			if (skill != null && !skill.isOffensive())
				return tgt.getTeam() == attacker.getTeam();
			return tgt.getTeam() != attacker.getTeam();
		}
		return false;
	}
	
	
	public void onHeal(L2PcInstance player, L2Character target, int damage)
	{
		if (player == null || target == null)
			return;
		
		L2PcInstance playerTarget = null;
		if (target instanceof L2PcInstance)
			playerTarget = (L2PcInstance)target;
		else if (target instanceof L2Summon)
			playerTarget = ((L2Summon)target).getOwner();
		
		if (playerTarget == null)
			return;
		
		if (player == playerTarget)
			return;
		
		//player is the caster
		//target is in theory your team mate
		//need know what players are attacking your team mate
		if (ASSIST_SYSTEM)
		{
		}
	}
	
	public void onDamage(L2PcInstance player, L2Character target, int damage)
	{
		if (player == null || target == null)
			return;
		
		L2PcInstance playerTarget = null;
		if (target instanceof L2PcInstance)
			playerTarget = (L2PcInstance)target;
		else if (target instanceof L2Summon)
			playerTarget = ((L2Summon)target).getOwner();
		
		if (playerTarget == null)
			return;
		
		if (ASSIST_SYSTEM)
		{
			synchronized(_playerAssists)
			{
				if (_playerAssists.containsKey(playerTarget.getObjectId()))
				{
					Map<Integer, Assist> info = _playerAssists.get(playerTarget.getObjectId());
					if (info.containsKey(player.getObjectId()))
					{
						//Update his information
						_playerAssists.get(playerTarget.getObjectId()).get(player.getObjectId()).increaseDamage(damage);
					}
					else
					{
						//This character don't have any record yet
						_playerAssists.get(playerTarget.getObjectId()).put(player.getObjectId(), new Assist(player, damage));
					}
				}
				else
				{
					Map<Integer, Assist> info = new HashMap<Integer, Assist>();
					info.put(player.getObjectId(), new Assist(player, damage));
					_playerAssists.put(playerTarget.getObjectId(), info);
				}
			}
		}
	}
	
	private class WarZoneTask implements Runnable
	{
		public void run()
		{
			if (_zoneState == WarZoneState.IDLE)
			{
				if (ThreadPoolManager.getInstance().isShutdown())
					return;
				
				_warZone = new WarZone();
				_warZone.prepareInstance();
				
				_zoneState = WarZoneState.OPEN;
				
				_lastStartedTime = System.currentTimeMillis();
				
				Announcements.getInstance().announceToAll("War Zones: The access to the war zone is now open (ALT + B > War Zone)!");
				
				Broadcast.toAllOnlinePlayers(new Earthquake(1, 1, 1, 10, 3));
				
				if (BOTS_ENABLED)
				{
					if (BotsManager.getInstance().getBots(BOT_USAGE_NAME) != null)
					{
						//Get the list..
						List<L2PcInstance> preparedBots = BotsManager.getInstance().getBots(BOT_USAGE_NAME);
						Collections.shuffle(preparedBots);
						
						if (!preparedBots.isEmpty())
						{
							//Be sure we use random amount of bots
							int botsToUseCount = 0;
							int randomBotsToUse = Rnd.get(AMOUNT_OF_BOTS / 2, AMOUNT_OF_BOTS);
							for (final L2PcInstance pl : preparedBots)
							{
								if (pl == null)
									continue;
								
								if (botsToUseCount == randomBotsToUse)
									break;
								
								L2NpcBufferInstance.giveBasicBuffs(pl);
								
								ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
								{
									public void run()
									{
										if (_zoneState == WarZoneState.OPEN)
										{
											CustomCommunityBoard.getInstance().parseCmd("_bbscustom;action;WarArea;join", pl);
											pl.getBotController().startController();
										}
									}
								}, Rnd.get(ADD_BOT_DELAY / 2, ADD_BOT_DELAY) * 60000);
								
								botsToUseCount++;
							}
							Log.info("CustomWarAreas: We will use " + randomBotsToUse + " bots on this round!");
						}
					}
				}
				ThreadPoolManager.getInstance().scheduleGeneral(new WarZoneTask(), EVENT_DURATION * 60000);
			}
			else if (_zoneState == WarZoneState.OPEN)
			{
				_zoneState = WarZoneState.IDLE;
				
				//kick all players
				List<L2PcInstance> allPlayers = new ArrayList<>(_warZone.getAllPlayers());
				for (L2PcInstance pl : allPlayers)
				{
					if (pl == null)
						continue;
					onExitZone(pl, true);
				}
				Announcements.getInstance().announceToAll("War Zones: The access to the war zone is now closed!");
				
				//Delete stored info
				_playerPositions.clear();
				_playerAssists.clear();
				
				//Destroy the instance
				InstanceManager.getInstance().destroyInstance(_warZone.getInstanceId());
				
				_nextStartTime = System.currentTimeMillis() + EVENT_DELAY * 60000;
				
				//Delay the next start
				ThreadPoolManager.getInstance().scheduleGeneral(new WarZoneTask(), EVENT_DELAY * 60000);
			}
		}
	}
	
	public boolean isActive()
	{
		return _zoneState == WarZoneState.OPEN;
	}
	
	private CustomWarAreas()
	{
		load();
		
		if (BOTS_ENABLED)
			BotsManager.getInstance().prepareBots(AMOUNT_OF_BOTS, 99, BOT_USAGE_NAME, BotMode.WARZONE);
		
		_warZone = new WarZone();
		
		_nextStartTime = System.currentTimeMillis() + FIRST_EVENT_START * 60000;
		
		ThreadPoolManager.getInstance().scheduleGeneral(new WarZoneTask(), FIRST_EVENT_START * 60000);
	}
	
	public static final CustomWarAreas getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CustomWarAreas _instance = new CustomWarAreas();
	}
}
