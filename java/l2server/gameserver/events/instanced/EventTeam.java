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
	private String name;
	/**
	 * The team spot coordinated<br>
	 */
	private Point3D coordinates;
	/**
	 * The points of the team<br>
	 */
	private short points;
	/**
	 * Name and instance of all participated players in HashMap<br>
	 */
	private Map<Integer, L2PcInstance> participatedPlayers = new LinkedHashMap<>();

	private int flagId = 44004;
	private L2Spawn flagSpawn;
	private int golemId = 44003;
	private L2Spawn golemSpawn;

	private L2PcInstance VIP;

	/**
	 * C'tor initialize the team<br><br>
	 *
	 * @param name        as String<br>
	 * @param coordinates as int[]<br>
	 */
	public EventTeam(int id, String name, Point3D coordinates)
	{
		flagId = 44004 + id;
		this.name = name;
		this.coordinates = coordinates;
		points = 0;
		flagSpawn = null;
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

		synchronized (participatedPlayers)
		{
			participatedPlayers.put(playerInstance.getObjectId(), playerInstance);
		}

		return true;
	}

	/**
	 * Removes a player from the team<br><br>
	 */
	public void removePlayer(int playerObjectId)
	{
		synchronized (participatedPlayers)
		{
			/*if (!EventsManager.getInstance().isType(EventType.DM)
                    && !EventsManager.getInstance().isType(EventType.SS)
					&& !EventsManager.getInstance().isType(EventType.SS2))
				this.participatedPlayers.get(playerObjectId).setEvent(null);*/
			participatedPlayers.remove(playerObjectId);
		}
	}

	/**
	 * Increases the points of the team<br>
	 */
	public void increasePoints()
	{
		++points;
	}

	public void decreasePoints()
	{
		--points;
	}

	/**
	 * Cleanup the team and make it ready for adding players again<br>
	 */
	public void cleanMe()
	{
		participatedPlayers.clear();
		participatedPlayers = new HashMap<>();
		points = 0;
	}

	public void onEventNotStarted()
	{
		for (L2PcInstance player : participatedPlayers.values())
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

		synchronized (participatedPlayers)
		{
			containsPlayer = participatedPlayers.containsKey(playerObjectId);
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
		return name;
	}

	/**
	 * Returns the coordinates of the team spot<br><br>
	 *
	 * @return int[]: team coordinates<br>
	 */
	public Point3D getCoords()
	{
		return coordinates;
	}

	/**
	 * Returns the points of the team<br><br>
	 *
	 * @return short: team points<br>
	 */
	public short getPoints()
	{
		return points;
	}

	/**
	 * Returns name and instance of all participated players in HashMap<br><br>
	 *
	 * @return Map<String, L2PcInstance>: map of players in this team<br>
	 */
	public Map<Integer, L2PcInstance> getParticipatedPlayers()
	{
		Map<Integer, L2PcInstance> participatedPlayers = null;

		synchronized (this.participatedPlayers)
		{
			participatedPlayers = this.participatedPlayers;
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

		synchronized (participatedPlayers)
		{
			participatedPlayerCount = participatedPlayers.size();
		}

		return participatedPlayerCount;
	}

	public int getAlivePlayerCount()
	{
		int alivePlayerCount = 0;

		ArrayList<L2PcInstance> toIterate = new ArrayList<>(participatedPlayers.values());
		for (L2PcInstance player : toIterate)
		{
			if (!player.isOnline() || player.getClient() == null || player.getEvent() == null)
			{
				participatedPlayers.remove(player.getObjectId());
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

		for (L2PcInstance player : participatedPlayers.values())
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
		int rnd = Rnd.get(participatedPlayers.size());
		int i = 0;
		for (L2PcInstance participant : participatedPlayers.values())
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
		this.name = name;
	}

	public void setCoords(Point3D coords)
	{
		coordinates = coords;
	}

	public L2Spawn getFlagSpawn()
	{
		return flagSpawn;
	}

	public void setFlagSpawn(L2Spawn spawn)
	{
		if (flagSpawn != null && flagSpawn.getNpc() != null)
		{
			((L2EventFlagInstance) flagSpawn.getNpc()).shouldBeDeleted();
			flagSpawn.getNpc().deleteMe();
			flagSpawn.stopRespawn();
			SpawnTable.getInstance().deleteSpawn(flagSpawn, false);
		}

		flagSpawn = spawn;
	}

	public int getFlagId()
	{
		return flagId;
	}

	public void setVIP(L2PcInstance character)
	{
		VIP = character;
	}

	public L2PcInstance getVIP()
	{
		return VIP;
	}

	public boolean isAlive()
	{
		return getAlivePlayerCount() > 0;
	}

	public L2Spawn getGolemSpawn()
	{
		return golemSpawn;
	}

	public void setGolemSpawn(L2Spawn spawn)
	{
		if (golemSpawn != null && golemSpawn.getNpc() != null)
		{
			golemSpawn.getNpc().deleteMe();
		}
		golemSpawn = spawn;
	}

	public int getGolemId()
	{
		return golemId;
	}
}
