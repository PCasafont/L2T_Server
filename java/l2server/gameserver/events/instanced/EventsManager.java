package l2server.gameserver.events.instanced;

import l2server.Config;
import l2server.gameserver.Announcements;
import l2server.gameserver.Reloadable;
import l2server.gameserver.ReloadableManager;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.communitybbs.Manager.CustomCommunityBoard;
import l2server.gameserver.events.instanced.EventInstance.EventState;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.olympiad.OlympiadManager;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.util.Broadcast;
import l2server.log.Log;
import l2server.util.Rnd;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Pere
 */
public class EventsManager implements Reloadable
{
	public static EventsManager _instance = null;

	private HashMap<Integer, EventLocation> _locations = new HashMap<>();

	private EventManagerTask _task;
	private ConcurrentHashMap<Integer, EventInstance> _instances = new ConcurrentHashMap<>();
	private int _nextInstanceId = 1;

	private EventConfig _currentConfig = null;
	private Map<Integer, L2PcInstance> _registeredPlayers = new HashMap<>();

	public static EventsManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new EventsManager();
		}
		return _instance;
	}

	public void start()
	{
		if (!Config.INSTANCED_EVENT_ENABLED)
		{
			Log.info("Instanced Events are disabled.");
			return;
		}

		// Load the configuration
		loadConfig();
		ReloadableManager.getInstance().register("eventLocations", this);

		_task = new EventManagerTask();
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(_task, 10000L, 60000L);

		_currentConfig = new EventConfig();

		Log.info("Instanced Events started.");
	}

	public void loadConfig()
	{
		_locations.clear();

		XmlDocument doc = new XmlDocument(new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "eventsConfig.xml"));
		if (doc.getFirstChild() == null)
		{
			Log.warning("An error occured while loading the Event Locations.");
			return;
		}

		int locCount = 0;
		for (XmlNode n : doc.getChildren())
		{
			if (!n.getName().equals("list"))
			{
				continue;
			}

			for (XmlNode node : n.getChildren())
			{
				if (node.getName().equals("location"))
				{
					EventLocation loc = new EventLocation(node);
					_locations.put(loc.getId(), loc);

					locCount++;
				}
			}
		}

		Log.info("Events Manager: loaded " + locCount + " locations");
	}

	public EventLocation getRandomLocation()
	{
		EventLocation loc = _locations.get(Rnd.get(100));
		while (loc == null)
		{
			loc = _locations.get(Rnd.get(100));
		}
		return loc;
	}

	public EventLocation getLocation(int id)
	{
		return _locations.get(id);
	}

	public EventManagerTask getTask()
	{
		return _task;
	}

	class EventManagerTask implements Runnable
	{
		private int _minutesToStart;

		public EventManagerTask()
		{
			_minutesToStart = Config.INSTANCED_EVENT_INTERVAL;
		}

		@Override
		public void run()
		{
			List<Integer> toRemove = new ArrayList<>();
			try
			{
				for (EventInstance event : _instances.values())
				{
					if (event == null)
					{
						continue;
					}

					if (event.isState(EventState.INACTIVE))
					{
						toRemove.add(event.getId());
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				_instances.clear();
			}

			for (int eventId : toRemove)
			{
				_instances.remove(eventId);
			}

			_minutesToStart--;

			if (_minutesToStart <= 0)
			{
				// Prepare an instance
				if (!prepare())
				{
					Announcements.getInstance()
							.announceToAll("The event could not start because it lacked registered players.");
				}

				_currentConfig = new EventConfig();
				_minutesToStart = Config.INSTANCED_EVENT_INTERVAL;
			}
			else if (_minutesToStart == 10 || _minutesToStart == 5 || _minutesToStart == 2 || _minutesToStart == 1)
			{
				// Auto join!
				/*if (_minutesToStart == 1)
                {
					for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
					{
						if (player == null || player.isGM())
							continue;
						if (player.isOnline() && !isPlayerParticipant(player.getObjectId()))
							join(player);
					}
				}*/

				Broadcast.toAllOnlinePlayers(new ExShowScreenMessage(
						"The " + _currentConfig.getEventName() + " will start in " + _minutesToStart + " minute" +
								(_minutesToStart > 1 ? "s" : "") + ".", 5000));
				ThreadPoolManager.getInstance().scheduleGeneral(() -> Broadcast.toAllOnlinePlayers(
						new ExShowScreenMessage("Use the Community Board's (ALT+B) \"Join Events\" menu to join.",
								5000)), 5000L);
			}
		}

		public int getMinutesToStart()
		{
			return _minutesToStart;
		}
	}

	public EventConfig getCurrentConfig()
	{
		return _currentConfig;
	}

	public Map<Integer, L2PcInstance> getRegisteredPlayers()
	{
		return _registeredPlayers;
	}

	private boolean prepare()
	{
		if (_registeredPlayers.isEmpty())
		{
			return false;
		}

		// First sort the registered players
		int[][] sorted = new int[_registeredPlayers.size()][2];
		int i = 0;
		for (L2PcInstance player : _registeredPlayers.values())
		{
			if (player == null || OlympiadManager.getInstance().isRegisteredInComp(player) ||
					player.isInOlympiadMode() || player.isOlympiadStart() || player.isFlyingMounted() ||
					player.inObserverMode())
			{
				continue;
			}

			int objId = player.getObjectId();
			int strPoints = player.getStrenghtPoints(false);
			// Find the index of where the current player should be put
			int j = 0;
			while (j < i && strPoints < sorted[j][1])
			{
				j++;
			}
			// Move the rest
			for (int k = i; k > j; k--)
			{
				int temp1 = sorted[k][0];
				int temp2 = sorted[k][1];
				sorted[k][0] = sorted[k - 1][0];
				sorted[k][1] = sorted[k - 1][1];
				sorted[k - 1][0] = temp1;
				sorted[k - 1][1] = temp2;
			}
			// And put the current player in the blank space
			sorted[j][0] = objId;
			sorted[j][1] = strPoints;

			i++;
		}

		// Next divide all the registered players in groups, depending on the location's maximum room
		List<List<Integer>> groups = new ArrayList<>();
		i = 0;
		while (i < sorted.length)
		{
			List<Integer> group = new ArrayList<>();
			int j = 0;
			while (i + j < sorted.length)
			{
				group.add(sorted[i + j][0]);

				//if (Config.isServer(Config.TENKAI) && j >= _currentConfig.getLocation().getMaxPlayers())
				//	break;

				j++;
			}

			if (j < _currentConfig.getMinPlayers())
			{
				if (!groups.isEmpty())
				{
					groups.get(groups.size() - 1).addAll(group);
				}

				break;
			}

			groups.add(group);
			i += j;
		}

		// And finally create the event instances according to the generated groups
		for (List<Integer> group : groups)
		{
			EventInstance ei = createInstance(_nextInstanceId++, group, _currentConfig);
			if (ei != null)
			{
				_instances.put(ei.getId(), ei);
				Announcements.getInstance().announceToAll(
						"Event registrations closed.");// The next event will be a " + _currentConfig.getEventString() + ". Type .event to join.");

				for (EventTeam team : ei.getTeams())
				{
					for (int memberId : team.getParticipatedPlayers().keySet())
					{
						_registeredPlayers.remove(memberId);
					}
				}
			}

			return true;
		}

		return false;
	}

	public void onLogin(L2PcInstance playerInstance)
	{
		if (playerInstance != null && isPlayerParticipant(playerInstance.getObjectId()))
		{
			removeParticipant(playerInstance.getObjectId());
			if (playerInstance.getEvent() != null)
			{
				for (L2Abnormal effect : playerInstance.getAllEffects())
				{
					if (effect != null)
					{
						effect.exit();
					}
				}
				playerInstance.eventRestoreBuffs();
				playerInstance.getEvent().onLogin(playerInstance);
			}
		}
	}

	public void onLogout(L2PcInstance playerInstance)
	{
		if (playerInstance != null && isPlayerParticipant(playerInstance.getObjectId()))
		{
			if (playerInstance.getEvent() != null)
			{
				for (L2Abnormal effect : playerInstance.getAllEffects())
				{
					if (effect != null)
					{
						effect.exit();
					}
				}
				playerInstance.eventRestoreBuffs();
				playerInstance.getEvent().onLogout(playerInstance);
			}

			removeParticipant(playerInstance.getObjectId());
		}

		playerInstance.setEvent(null);
	}

	public void join(L2PcInstance playerInstance)
	{
		if (isPlayerParticipant(playerInstance.getObjectId()))
		{
			return;
		}

		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);

		if (playerInstance.isCursedWeaponEquipped())
		{
			npcHtmlMessage.setHtml(
					"<html><head><title>Instanced Events</title></head><body>Cursed weapon owners are not allowed to participate.</body></html>");
		}
		else if (OlympiadManager.getInstance().isRegisteredInComp(playerInstance))
		{
			npcHtmlMessage.setHtml(
					"<html><head><title>Instanced Events</title></head><body>You can not participate when registered for Olympiad.</body></html>");
		}
		else if (playerInstance.getReputation() < 0)
		{
			npcHtmlMessage.setHtml(
					"<html><head><title>Instanced Events</title></head><body>Chaotic players are not allowed to participate.</body></html>");
		}
		else if (playerInstance.isInJail())
		{
			npcHtmlMessage.setHtml(
					"<html><head><title>Instanced Events</title></head><body>You cannot participate, you must wait your jail time.</body></html>");
		}
		else if (playerInstance.isCastingNow())
		{
			npcHtmlMessage.setHtml(
					"<html><head><title>Instanced Events</title></head><body>You can't register while casting a skill.</body></html>");
		}
		else if (checkDualBox(playerInstance))
		{
			npcHtmlMessage.setHtml(
					"<html><head><title>Instanced Events</title></head><body>You have another character already registered for this event!</body></html>");
		}
		else if (playerInstance.getInstanceId() != 0)
		{
			npcHtmlMessage.setHtml(
					"<html><head><title>Instanced Events</title></head><body>You can't join one event while in other instance!</body></html>");
		}
		else if (playerInstance.isInDuel() || playerInstance.isDead() || playerInstance.getIsInsideGMEvent())
		{
			npcHtmlMessage.setHtml(
					"<html><head><title>Instanced Events</title></head><body>You can't join one event at this moment!</body></html>");
		}
		else
		{
			if (addParticipant(playerInstance))
			{
				CustomCommunityBoard.getInstance().parseCmd("_bbscustom;currentEvent", playerInstance);
			}

			return;
		}

		playerInstance.sendPacket(npcHtmlMessage);
	}

	public synchronized boolean addParticipant(L2PcInstance playerInstance)
	{
		// Check for nullpoitner
		if (playerInstance == null)
		{
			return false;
		}

		_registeredPlayers.put(playerInstance.getObjectId(), playerInstance);

		return true;
	}

	public void leave(L2PcInstance playerInstance)
	{
		if (!isPlayerParticipant(playerInstance.getObjectId()))
		{
			return;
		}

		// If the event is started the player shouldn't be allowed to leave
		if (playerInstance.getEvent() != null && playerInstance.getEvent().isState(EventState.STARTED))
		{
			return;
		}

		if (removeParticipant(playerInstance.getObjectId()))
		{
			CustomCommunityBoard.getInstance().parseCmd("_bbscustom;currentEvent", playerInstance);
		}
	}

	public boolean removeParticipant(int playerObjectId)
	{
		if (_registeredPlayers.remove(playerObjectId) != null)
		{
			return true;
		}

		EventInstance event = getParticipantEvent(playerObjectId);
		if (event != null)
		{
			return event.removeParticipant(playerObjectId);
		}

		return false;
	}

	public String getEventInfoPage(L2PcInstance player)
	{
		if (!Config.INSTANCED_EVENT_ENABLED)
		{
			return "";
		}

		if (!player.getFloodProtectors().getEventBypass().tryPerformAction("Event Info"))
		{
			return "";
		}

		String result = null;
		if (player.getEvent() != null && player.getEvent().isState(EventState.STARTED))
		{
			result = HtmCache.getInstance().getHtm(null, "CommunityBoard/runningEvent.htm");
			result = result.replace("%runningEventInfo%", player.getEvent().getInfo(player));
			return result;
		}
		else
		{
			result = HtmCache.getInstance().getHtm(null, "CommunityBoard/joinEvents.htm");
		}

		//PvP Event
		result = result.replace("%pvpEventName%", _currentConfig.getEventName());
		result = result.replace("%pvpEventLocation%", _currentConfig.getEventLocationName());
		result = result.replace("%pvpEventTime%",
				_task.getMinutesToStart() + " minute" + (_task.getMinutesToStart() > 1 ? "s" : ""));
		result = result.replace("%pvpEventId%", String.valueOf(_currentConfig.getEventImageId()));
		result = result.replace("%pvpInfoLink%", String.valueOf(_currentConfig.getType()));

		if (_registeredPlayers.isEmpty())
		{
			result = result.replace("%pvpEventPlayers%", "");
			result = result.replace("Registered Players for the Event", "");
		}
		else
		{
			result = result.replace("%pvpEventPlayers%", getRegisteredPlayers(player));
		}

		//Both events
		if (isPlayerParticipant(player.getObjectId()))
		{
			result = result.replace("%leaveButton%",
					"<button value=\"Leave Match making\" action=\"bypass -h InstancedEventLeave\" width=255 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			result = result.replace("%pvpEventJoinButton%", "");
		}
		else
		{
			result = result.replace("%pvpEventJoinButton%",
					"<button value=\"Join Match making\" action=\"bypass -h InstancedEventJoin true\" width=255 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			result = result.replace("%leaveButton%", "");
		}

		//Observe part?
		if (_instances.isEmpty())
		{
			result = result.replace("Current Observable Events", "");
			result = result.replace("%observeEvents%", "");
		}
		else
		{
			int remaining = _instances.size();
			int pageCheck = 1;
			int total = 1;
			String eventString = "";

			for (EventInstance event : _instances.values())
			{
				if (!event.isState(EventState.STARTED))
				{
					remaining--;
					if (!eventString.isEmpty() && (pageCheck == 6 || remaining == 0))
					{
						pageCheck = 1;
						eventString += "</tr>";
					}

					continue;
				}

				if (pageCheck == 1)
				{
					eventString += "<tr>";
				}

				eventString += "<td align=center><button value=\"" + event.getConfig().getEventName() + " #" + total +
						"\" action=\"bypass -h InstancedEventObserve " + event.getId() +
						"\" width=110 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>";

				// temp fix
				event.getInfo(player);

				pageCheck++;
				remaining--;
				total++;

				if (pageCheck == 6 || remaining == 0)
				{
					pageCheck = 1;
					eventString += "</tr>";
				}
			}

			result = result.replace("%observeEvents%", eventString);
			//result += "<br><br><br><br>";
		}

		return result;
	}

	private String getRegisteredPlayers(L2PcInstance player)
	{
		String result = "";

		if (_registeredPlayers.isEmpty())
		{
			return result;
		}

		for (L2PcInstance participant : _registeredPlayers.values())
		{
			if (participant == null)
			{
				continue;
			}

			result += getPlayerString(participant, player) + ", ";
		}

		if (!result.isEmpty())
		{
			result = result.substring(0, result.length() - 2);
			result += ".";
		}

		return result;
	}

	public String getPlayerString(L2PcInstance player, L2PcInstance reader)
	{
		String color = "FFFFFF";
		if (player == reader)
		{
			color = "FFFF00";
		}
		else if (player.getFriendList().contains(reader.getObjectId()))
		{
			color = "00FFFF";
		}
		else if (reader.getParty() != null && reader.getParty() == player.getParty())
		{
			color = "00FF00";
		}
		else if (reader.getClan() != null)
		{
			if (reader.getClanId() > 0 && reader.getClanId() == player.getClanId())
			{
				color = "8888FF";
			}
			else if (reader.getAllyId() > 0 && reader.getAllyId() == player.getAllyId())
			{
				color = "88FF88";
			}
			else if (reader.getClan().isAtWarWith(player.getClanId()))
			{
				color = "CC0000";
			}
		}
		return "<font color=\"" + color + "\">" + player.getName() + "</font>";
	}

	public int getParticipantEventId(int playerObjectId)
	{
		for (EventInstance event : _instances.values())
		{
			if (event.isPlayerParticipant(playerObjectId))
			{
				return event.getId();
			}
		}
		return -1;
	}

	public EventInstance getParticipantEvent(int playerObjectId)
	{
		for (EventInstance event : _instances.values())
		{
			if (event.isPlayerParticipant(playerObjectId))
			{
				return event;
			}
		}
		return null;
	}

	public byte getParticipantTeamId(int playerObjectId)
	{
		EventInstance event = getParticipantEvent(playerObjectId);
		if (event == null)
		{
			return -1;
		}
		return event.getParticipantTeamId(playerObjectId);
	}

	public EventTeam getParticipantTeam(int playerObjectId)
	{
		EventInstance event = getParticipantEvent(playerObjectId);
		if (event == null)
		{
			return null;
		}
		return getParticipantEvent(playerObjectId).getParticipantTeam(playerObjectId);
	}

	public EventTeam getParticipantEnemyTeam(int playerObjectId)
	{
		EventInstance event = getParticipantEvent(playerObjectId);
		if (event == null)
		{
			return null;
		}
		return getParticipantEvent(playerObjectId).getParticipantEnemyTeam(playerObjectId);
	}

	public boolean isPlayerParticipant(int playerObjectId)
	{
		if (_registeredPlayers.containsKey(playerObjectId))
		{
			return true;
		}

		for (EventInstance event : _instances.values())
		{
			if (event.isPlayerParticipant(playerObjectId))
			{
				return true;
			}
		}
		return false;
	}

	public EventInstance createInstance(int id, List<Integer> group, EventConfig config)
	{
		// A map of lists to access the players sorted by class
		Map<Integer, List<L2PcInstance>> playersByClass = new HashMap<>();
		// Classify the players according to their class
		for (int playerId : group)
		{
			if (playerId == 0)
			{
				continue;
			}

			L2PcInstance player = L2World.getInstance().getPlayer(playerId);
			int classId = player.getCurrentClass().getAwakeningClassId();
			if (classId == -1)
			{
				classId = 147;
			}

			List<L2PcInstance> players = playersByClass.get(classId);
			if (players == null)
			{
				players = new ArrayList<>();
				playersByClass.put(classId, players);
			}

			players.add(player);
		}

		// If we found none, don't do anything
		if (playersByClass.isEmpty())
		{
			return null;
		}

		// Create the event and fill it with the players, in class order
		EventInstance event = config.createInstance(id);
		for (int classId = 139; classId <= 147; classId++)
		{
			List<L2PcInstance> players = playersByClass.get(classId);
			if (players == null)
			{
				continue;
			}

			for (L2PcInstance player : players)
			{
				event.addParticipant(player);
			}
		}

		return event;
	}

	private boolean checkDualBox(L2PcInstance player)
	{
		if (player == null)
		{
			return false;
		}

		for (L2PcInstance registered : _registeredPlayers.values())
		{
			if (registered == null)
			{
				continue;
			}

			if (player.getExternalIP().equalsIgnoreCase(registered.getExternalIP()) &&
					player.getInternalIP().equalsIgnoreCase(registered.getInternalIP()))
			{
				return true;
			}
		}

		return false;

		//TODO LasTravel: Hwid check don't work if we don't have LG
        /*String hwId = player.getClient().getHWId();
		for (L2PcInstance registered : _registeredPlayers.values())
		{
			if (registered.getClient() != null
					&& registered.getClient().getHWId() != null
					&& registered.getClient().getHWId().equals(hwId))
				return true;
		}
		return false;*/
	}

	/**
	 * @param activeChar
	 * @param _command
	 */
	public void handleBypass(L2PcInstance activeChar, String _command)
	{
		if (activeChar == null)
		{
			return;
		}

		if (_command.startsWith("InstancedEventJoin"))
		{
			join(activeChar);
		}
		else if (_command.equals("InstancedEventLeave"))
		{
			leave(activeChar);
		}
		else if (_command.startsWith("InstancedEventObserve"))
		{
			int eventId = Integer.valueOf(_command.substring(22));
			if (_instances.get(eventId) != null)
			{
				_instances.get(eventId).observe(activeChar);
			}
		}
		
		/*else if (_command.startsWith("InstancedEventParticipation"))
		{
			int eventId = Integer.valueOf(_command.substring(25));
			if (Events.getInstance().Events.getInstance().get(eventId) != null)
				Events.getInstance().Events.getInstance().get(eventId).join(activeChar);
		}
		else if (_command.startsWith("InstancedEventStatus"))
		{
			int eventId = Integer.valueOf(_command.substring(18));
			if (Events.getInstance().Events.getInstance().get(eventId) != null)
				Events.getInstance().Events.getInstance().get(eventId).eventInfo(activeChar);
		}*/
	}

	@Override
	public boolean reload()
	{
		_locations.clear();
		loadConfig();
		return true;
	}

	@Override
	public String getReloadMessage(boolean success)
	{
		return "Event configurations reloaded";
	}
}
