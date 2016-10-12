package l2server.gameserver.events.instanced;

import l2server.Config;
import l2server.gameserver.Announcements;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.communitybbs.Manager.CustomCommunityBoard;
import l2server.gameserver.events.instanced.EventInstance.EventState;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.olympiad.OlympiadManager;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Pere
 */
public class EventsMatchMaker
{
	public static EventsMatchMaker _instance = null;

	private MatchMakingTask _pvpTask;
	private MatchMakingTask _specialTask;
	public ConcurrentHashMap<Integer, EventInstance> Instances = new ConcurrentHashMap<>();
	private int _nextInstanceId = 1;

	public static EventsMatchMaker getInstance()
	{
		if (_instance == null)
		{
			_instance = new EventsMatchMaker();
		}
		return _instance;
	}

	public void start()
	{
		if (Config.INSTANCED_EVENT_ENABLED)
		{
			_pvpTask = new MatchMakingTask(true);
			_specialTask = new MatchMakingTask(false);
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(_pvpTask, 60000L, 10000L);
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(_specialTask, 60000L, 10000L);
			Log.info("Instanced Events scheduled.");
		}
		else
		{
			Log.info("Instanced Events are disabled.");
		}
	}

	public MatchMakingTask getPvpTask()
	{
		return _pvpTask;
	}

	public MatchMakingTask getSpecialTask()
	{
		return _specialTask;
	}

	class MatchMakingTask implements Runnable
	{
		private boolean _pvp = true;
		private EventConfig _currentConfig = null;
		private Map<Integer, L2PcInstance> _registeredPlayers = new HashMap<>();
		private int _prepareAttempts = 0;
		private int _fillProgress = 0;

		MatchMakingTask(boolean pvp)
		{
			_pvp = pvp;
			_currentConfig = new EventConfig(_pvp);
		}

		@Override
		public void run()
		{
			List<Integer> toRemove = new ArrayList<>();
			try
			{
				for (EventInstance event : Instances.values())
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
				Instances.clear();
			}

			for (int eventId : toRemove)
			{
				Instances.remove(eventId);
			}

			// Prepare an instance
			while (prepare())
			{
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

		public int getFillProgress()
		{
			return _fillProgress;
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

			// Next divide all the registered players in groups, depending on their strenght
			int[] group = null;
			i = 0;
			int bestFillProgress = 0;
			while (i < sorted.length)
			{
				group = new int[_currentConfig.getLocation().getMaxPlayers()];
				int points = sorted[i][1];
				int j = 0;
				while (i + j < sorted.length && (j < _currentConfig.getMinPlayers() ||
						j < _currentConfig.getLocation().getMaxPlayers() &&
								(points - sorted[i + j][1] < 15000 || _currentConfig.hasNoLevelLimits())))
				{
					group[j] = sorted[i + j][0];
					j++;
				}

				int minPlayers = _currentConfig.getMinPlayers();
				if (_prepareAttempts < 100)
				{
					minPlayers += (_currentConfig.getLocation().getMaxPlayers() - _currentConfig.getMinPlayers()) *
							(100 - _prepareAttempts) / 100;
				}

				if (j >= minPlayers)
				{
					bestFillProgress = 100;
					break;
				}

				if (100 * j / minPlayers > bestFillProgress)
				{
					bestFillProgress = 100 * j / minPlayers;
				}

				group = null;
				i += j;
			}

			_fillProgress = bestFillProgress;

			// And finally create the event instances according to the generated groups
			if (group != null)
			{
				EventInstance ei = createInstance(_nextInstanceId++, group, _currentConfig);
				if (ei != null)
				{
					Instances.put(ei.getId(), ei);
					_currentConfig = new EventConfig(_pvp);
					_prepareAttempts = 0;
					_fillProgress = 0;
					String type = _pvp ? "PvP" : "special";
					Announcements.getInstance().announceToAll(
							"The " + type + " event has been set up to start. The next event will be a " +
									_currentConfig.getEventString() + ". Type .event to join.");

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

			_prepareAttempts++;

			if (_prepareAttempts > 200)
			{
				_currentConfig = new EventConfig(_pvp);
				_prepareAttempts = 0;
				_fillProgress = 0;
				String type = _pvp ? "PvP" : "special";
				Announcements.getInstance().announceToAll(
						"The " + type + " event couldn't start after almost an hour waiting, shuffling configuration.");
				Announcements.getInstance().announceToAll(
						"The new " + type + " event will be a " + _currentConfig.getEventString() +
								". Type .event to join.");
			}

			return false;
		}
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
	}

	public void join(L2PcInstance playerInstance, boolean pvp)
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
		else if (playerInstance.isInDuel() || playerInstance.isDead())
		{
			npcHtmlMessage.setHtml(
					"<html><head><title>Instanced Events</title></head><body>You can't join one event at this moment!</body></html>");
		}
		else
		{
			if (addParticipant(playerInstance, pvp))
			{
				CustomCommunityBoard.getInstance().parseCmd("_bbscustom;currentEvent", playerInstance);
			}

			return;
		}

		playerInstance.sendPacket(npcHtmlMessage);
	}

	public synchronized boolean addParticipant(L2PcInstance playerInstance, boolean pvp)
	{
		// Check for nullpoitner
		if (playerInstance == null)
		{
			return false;
		}

		if (pvp)
		{
			_pvpTask.getRegisteredPlayers().put(playerInstance.getObjectId(), playerInstance);
		}
		else
		{
			_specialTask.getRegisteredPlayers().put(playerInstance.getObjectId(), playerInstance);
		}

		return true;
	}

	public void leave(L2PcInstance playerInstance)
	{
		if (!isPlayerParticipant(playerInstance.getObjectId()))
		{
			return;
		}

		//LasTravel: If the event is started the player shouldn't be allowed to leave
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
		if (_pvpTask.getRegisteredPlayers().remove(playerObjectId) != null ||
				_specialTask.getRegisteredPlayers().remove(playerObjectId) != null)
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
		if (!player.getFloodProtectors().getEventBypass().tryPerformAction("Event Info"))
		{
			return "";
		}

		String a = null;

		if (player.getEvent() != null && player.getEvent().isState(EventState.STARTED))
		{
			a = HtmCache.getInstance().getHtm(null, "CommunityBoard/runningEvent.htm");

			a = a.replace("%runningEventInfo%", player.getEvent().getInfo(player));

			return a;
		}
		else
		{
			a = HtmCache.getInstance().getHtm(null, "CommunityBoard/joinEvents.htm");
		}

		//PvP Event
		a = a.replace("%pvpEventName%", getInstance().getPvpTask().getCurrentConfig().getEventName());
		a = a.replace("%pvpEventLocation%", getInstance().getPvpTask().getCurrentConfig().getEventLocationName());
		a = a.replace("%pvpStartProgress%", getInstance().getPvpTask().getFillProgress() + "%");
		a = a.replace("%pvpEventId%", String.valueOf(getInstance().getPvpTask().getCurrentConfig().getEventImageId()));
		a = a.replace("%pvpInfoLink%", String.valueOf(getInstance().getPvpTask().getCurrentConfig().getType()));

		if (_pvpTask.getRegisteredPlayers().isEmpty())
		{
			a = a.replace("%pvpEventPlayers%", "");
			a = a.replace("Registred Players At PvP Event", "");
		}
		else
		{
			a = a.replace("%pvpEventPlayers%", getPvPEventRegistredPlayers(player));
		}

		//Special Event
		a = a.replace("%specialStartProgress%", getInstance().getSpecialTask().getFillProgress() + "%");
		a = a.replace("%specialEventLocation%",
				getInstance().getSpecialTask().getCurrentConfig().getEventLocationName());
		a = a.replace("%specialEventName%", getInstance().getSpecialTask().getCurrentConfig().getEventName());
		a = a.replace("%specialEventId%",
				String.valueOf(getInstance().getSpecialTask().getCurrentConfig().getEventImageId()));
		a = a.replace("%specialInfoLink%", String.valueOf(getInstance().getSpecialTask().getCurrentConfig().getType()));

		if (_specialTask.getRegisteredPlayers().isEmpty())
		{
			a = a.replace("%specialEventPlayers%", "");
			a = a.replace("Registred Players At Special Event", "");
		}
		else
		{
			a = a.replace("%specialEventPlayers%", getSpecialEventRegistredPlayers(player));
		}

		//Both events
		if (isPlayerParticipant(player.getObjectId()))
		{
			a = a.replace("%pvpEventJoinButton%", "");
			a = a.replace("%specialEventJoinButton%", "");
			a = a.replace("%leaveButton%",
					"<button value=\"Leave Match making\" action=\"bypass -h TenkaiEventLeave\" width=630 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}
		else
		{
			a = a.replace("%leaveButton%", "");
			if (player.getLevel() < 99 || player.getOnlineTime() < 5 * 3600)
			{
				a = a.replace("%pvpEventJoinButton%",
						"<font color=FF0000>You can't join a PvP event until you get stronger!</font>");
			}
			else
			{
				a = a.replace("%pvpEventJoinButton%",
						"<button value=\"Join Match making (PvP)\" action=\"bypass -h TenkaiEventJoin true\" width=255 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			}
			a = a.replace("%specialEventJoinButton%",
					"<button value=\"Join Match making (Special)\" action=\"bypass -h TenkaiEventJoin false\" width=255 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}

		//Observe part?
		if (Instances.isEmpty())
		{
			a = a.replace("Current Observable Events", "");
			a = a.replace("%observeEvents%", "");
		}
		else
		{
			int b = Instances.size();

			int d = 1;

			int e = 1;

			String c = "";

			for (EventInstance event : Instances.values())
			{
				if (event.isState(EventState.STARTED))
				{
					if (d == 1)
					{
						c += "<tr>";
					}

					c += "<td><button value=\"" + event.getType().toString() + " #" + e +
							"\" action=\"bypass -h TenkaiEventObserve " + event.getId() +
							"\" width=90 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>";

					d++;

					b--;

					e++;

					if (d == 6 || b == 0)
					{
						d = 1;

						c += "</tr>";
					}
				}
			}

			a = a.replace("%observeEvents%", c);
			a += "<br><br><br><br>";
		}

		return a;
	}

	private String getPvPEventRegistredPlayers(L2PcInstance player)
	{
		String a = "";

		if (_pvpTask.getRegisteredPlayers().isEmpty())
		{
			return a;
		}

		for (L2PcInstance participant : _pvpTask.getRegisteredPlayers().values())
		{
			if (participant == null)
			{
				continue;
			}

			a += getPlayerString(participant, player) + ", ";
		}

		a = a.substring(0, a.length() - 2);

		a += ".";

		return a;
	}

	private String getSpecialEventRegistredPlayers(L2PcInstance player)
	{
		String a = "";

		if (_specialTask.getRegisteredPlayers().isEmpty())
		{
			return a;
		}

		for (L2PcInstance participant : _specialTask.getRegisteredPlayers().values())
		{
			if (participant == null)
			{
				continue;
			}

			a += getPlayerString(participant, player) + ", ";
		}

		a = a.substring(0, a.length() - 2);

		a += ".";

		return a;
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
		for (EventInstance event : Instances.values())
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
		for (EventInstance event : Instances.values())
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
		if (_pvpTask.getRegisteredPlayers().containsKey(playerObjectId) ||
				_specialTask.getRegisteredPlayers().containsKey(playerObjectId))
		{
			return true;
		}

		for (EventInstance event : Instances.values())
		{
			if (event.isPlayerParticipant(playerObjectId))
			{
				return true;
			}
		}
		return false;
	}

	public EventInstance createInstance(int id, int[] group, EventConfig config)
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

		for (L2PcInstance registered : _pvpTask.getRegisteredPlayers().values())
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

		for (L2PcInstance registered : _specialTask.getRegisteredPlayers().values())
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
	public void handleEventBypass(L2PcInstance activeChar, String _command)
	{
		if (activeChar == null)
		{
			return;
		}

		if (_command.startsWith("TenkaiEventJoin"))
		{
			join(activeChar, Boolean.parseBoolean(_command.split(" ", 0)[1]));
		}
		else if (_command.equals("TenkaiEventLeave"))
		{
			leave(activeChar);
		}
		else if (_command.startsWith("TenkaiEventObserve"))
		{
			int eventId = Integer.valueOf(_command.substring(19));

			if (Instances.get(eventId) != null)
			{
				Instances.get(eventId).observe(activeChar);
			}
		}

		/*else if (_command.startsWith("TenkaiEventParticipation"))
		{
			int eventId = Integer.valueOf(_command.substring(25));
			if (Events.getInstance().Events.getInstance().get(eventId) != null)
				Events.getInstance().Events.getInstance().get(eventId).join(activeChar);
		}
		else if (_command.startsWith("TenkaiEventStatus"))
		{
			int eventId = Integer.valueOf(_command.substring(18));
			if (Events.getInstance().Events.getInstance().get(eventId) != null)
				Events.getInstance().Events.getInstance().get(eventId).eventInfo(activeChar);
		}*/
	}
}
