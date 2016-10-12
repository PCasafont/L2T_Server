package l2server.gameserver.events.instanced;

import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.instance.L2EventFlagInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.util.Point3D;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Pere
 */
public class EventTeam
{
	/**
	 * The name of the team<br>
	 */
	private String _name;
	/**
	 * The team spot coordinated<br>
	 */
	private Point3D _coordinates;
	/**
	 * The points of the team<br>
	 */
	private short _points;
	/**
	 * Name and instance of all participated players in HashMap<br>
	 */
	private Map<Integer, L2PcInstance> _participatedPlayers = new LinkedHashMap<>();

	private int _flagId = 44004;
	private L2Spawn _flagSpawn;
	private int _golemId = 44003;
	private L2Spawn _golemSpawn;

	private L2PcInstance _VIP;

	/**
	 * C'tor initialize the team<br><br>
	 *
	 * @param name        as String<br>
	 * @param coordinates as int[]<br>
	 */
	public EventTeam(int id, String name, Point3D coordinates)
	{
		_flagId = 44004 + id;
		_name = name;
		_coordinates = coordinates;
		_points = 0;
		_flagSpawn = null;
	}

	/**
	 * Adds a player to the team<br><br>
	 *
	 * @param playerInstance as L2PcInstance<br>
	 * @return boolean: true if success, otherwise false<br>
	 */
	public boolean addPlayer(L2PcInstance playerInstance)
	{
		if (playerInstance == null)
		{
			return false;
		}

		synchronized (_participatedPlayers)
		{
			_participatedPlayers.put(playerInstance.getObjectId(), playerInstance);
		}

		return true;
	}

	/**
	 * Removes a player from the team<br><br>
	 */
	public void removePlayer(int playerObjectId)
	{
		synchronized (_participatedPlayers)
		{
			/*if (!EventsManager.getInstance().isType(EventType.DM)
                    && !EventsManager.getInstance().isType(EventType.SS)
					&& !EventsManager.getInstance().isType(EventType.SS2))
				_participatedPlayers.get(playerObjectId).setEvent(null);*/
			_participatedPlayers.remove(playerObjectId);
		}
	}

	/**
	 * Increases the points of the team<br>
	 */
	public void increasePoints()
	{
		++_points;
	}

	public void decreasePoints()
	{
		--_points;
	}

	/**
	 * Cleanup the team and make it ready for adding players again<br>
	 */
	public void cleanMe()
	{
		_participatedPlayers.clear();
		_participatedPlayers = new HashMap<>();
		_points = 0;
	}

	public void onEventNotStarted()
	{
		for (L2PcInstance player : _participatedPlayers.values())
		{
			if (player != null)
			{
				player.setEvent(null);
			}
		}
		cleanMe();
	}

	/**
	 * Is given player in this team?<br><br>
	 *
	 * @return boolean: true if player is in this team, otherwise false<br>
	 */
	public boolean containsPlayer(int playerObjectId)
	{
		boolean containsPlayer;

		synchronized (_participatedPlayers)
		{
			containsPlayer = _participatedPlayers.containsKey(playerObjectId);
		}

		return containsPlayer;
	}

	/**
	 * Returns the name of the team<br><br>
	 *
	 * @return String: name of the team<br>
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * Returns the coordinates of the team spot<br><br>
	 *
	 * @return int[]: team coordinates<br>
	 */
	public Point3D getCoords()
	{
		return _coordinates;
	}

	/**
	 * Returns the points of the team<br><br>
	 *
	 * @return short: team points<br>
	 */
	public short getPoints()
	{
		return _points;
	}

	/**
	 * Returns name and instance of all participated players in HashMap<br><br>
	 *
	 * @return Map<String, L2PcInstance>: map of players in this team<br>
	 */
	public Map<Integer, L2PcInstance> getParticipatedPlayers()
	{
		Map<Integer, L2PcInstance> participatedPlayers = null;

		synchronized (_participatedPlayers)
		{
			participatedPlayers = _participatedPlayers;
		}

		return participatedPlayers;
	}

	/**
	 * Returns player count of this team<br><br>
	 *
	 * @return int: number of players in team<br>
	 */
	public int getParticipatedPlayerCount()
	{
		int participatedPlayerCount;

		synchronized (_participatedPlayers)
		{
			participatedPlayerCount = _participatedPlayers.size();
		}

		return participatedPlayerCount;
	}

	public int getAlivePlayerCount()
	{
		int alivePlayerCount = 0;

		ArrayList<L2PcInstance> toIterate = new ArrayList<>(_participatedPlayers.values());
		for (L2PcInstance player : toIterate)
		{
			if (!player.isOnline() || player.getClient() == null || player.getEvent() == null)
			{
				_participatedPlayers.remove(player.getObjectId());
			}
			if (!player.isDead())
			{
				alivePlayerCount++;
			}
		}
		return alivePlayerCount;
	}

	public int getHealersCount()
	{
		int count = 0;

		for (L2PcInstance player : _participatedPlayers.values())
		{
			if (player.getCurrentClass().getId() == 146)
			{
				count++;
			}
		}
		return count;
	}

	public L2PcInstance selectRandomParticipant()
	{
		int rnd = Rnd.get(_participatedPlayers.size());
		int i = 0;
		for (L2PcInstance participant : _participatedPlayers.values())
		{
			if (i == rnd)
			{
				return participant;
			}
			i++;
		}
		return null;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public void setCoords(Point3D coords)
	{
		_coordinates = coords;
	}

	public L2Spawn getFlagSpawn()
	{
		return _flagSpawn;
	}

	public void setFlagSpawn(L2Spawn spawn)
	{
		if (_flagSpawn != null && _flagSpawn.getNpc() != null)
		{
			((L2EventFlagInstance) _flagSpawn.getNpc()).shouldBeDeleted();
			_flagSpawn.getNpc().deleteMe();
			_flagSpawn.stopRespawn();
			SpawnTable.getInstance().deleteSpawn(_flagSpawn, false);
		}

		_flagSpawn = spawn;
	}

	public int getFlagId()
	{
		return _flagId;
	}

	public void setVIP(L2PcInstance character)
	{
		_VIP = character;
	}

	public L2PcInstance getVIP()
	{
		return _VIP;
	}

	public boolean isAlive()
	{
		return getAlivePlayerCount() > 0;
	}

	public L2Spawn getGolemSpawn()
	{
		return _golemSpawn;
	}

	public void setGolemSpawn(L2Spawn spawn)
	{
		if (_golemSpawn != null && _golemSpawn.getNpc() != null)
		{
			_golemSpawn.getNpc().deleteMe();
		}
		_golemSpawn = spawn;
	}

	public int getGolemId()
	{
		return _golemId;
	}
}
