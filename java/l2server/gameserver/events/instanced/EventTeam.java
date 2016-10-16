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
		this.flagId = 44004 + id;
		this.name = name;
		this.coordinates = coordinates;
		this.points = 0;
		this.flagSpawn = null;
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

		synchronized (this.participatedPlayers)
		{
			this.participatedPlayers.put(playerInstance.getObjectId(), playerInstance);
		}

		return true;
	}

	/**
	 * Removes a player from the team<br><br>
	 */
	public void removePlayer(int playerObjectId)
	{
		synchronized (this.participatedPlayers)
		{
			/*if (!EventsManager.getInstance().isType(EventType.DM)
                    && !EventsManager.getInstance().isType(EventType.SS)
					&& !EventsManager.getInstance().isType(EventType.SS2))
				this.participatedPlayers.get(playerObjectId).setEvent(null);*/
			this.participatedPlayers.remove(playerObjectId);
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
		this.participatedPlayers.clear();
		this.participatedPlayers = new HashMap<>();
		this.points = 0;
	}

	public void onEventNotStarted()
	{
		for (L2PcInstance player : this.participatedPlayers.values())
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

		synchronized (this.participatedPlayers)
		{
			containsPlayer = this.participatedPlayers.containsKey(playerObjectId);
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
		return this.name;
	}

	/**
	 * Returns the coordinates of the team spot<br><br>
	 *
	 * @return int[]: team coordinates<br>
	 */
	public Point3D getCoords()
	{
		return this.coordinates;
	}

	/**
	 * Returns the points of the team<br><br>
	 *
	 * @return short: team points<br>
	 */
	public short getPoints()
	{
		return this.points;
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

		synchronized (this.participatedPlayers)
		{
			participatedPlayerCount = this.participatedPlayers.size();
		}

		return participatedPlayerCount;
	}

	public int getAlivePlayerCount()
	{
		int alivePlayerCount = 0;

		ArrayList<L2PcInstance> toIterate = new ArrayList<>(this.participatedPlayers.values());
		for (L2PcInstance player : toIterate)
		{
			if (!player.isOnline() || player.getClient() == null || player.getEvent() == null)
			{
				this.participatedPlayers.remove(player.getObjectId());
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

		for (L2PcInstance player : this.participatedPlayers.values())
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
		int rnd = Rnd.get(this.participatedPlayers.size());
		int i = 0;
		for (L2PcInstance participant : this.participatedPlayers.values())
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
		this.coordinates = coords;
	}

	public L2Spawn getFlagSpawn()
	{
		return this.flagSpawn;
	}

	public void setFlagSpawn(L2Spawn spawn)
	{
		if (this.flagSpawn != null && this.flagSpawn.getNpc() != null)
		{
			((L2EventFlagInstance) this.flagSpawn.getNpc()).shouldBeDeleted();
			this.flagSpawn.getNpc().deleteMe();
			this.flagSpawn.stopRespawn();
			SpawnTable.getInstance().deleteSpawn(this.flagSpawn, false);
		}

		this.flagSpawn = spawn;
	}

	public int getFlagId()
	{
		return this.flagId;
	}

	public void setVIP(L2PcInstance character)
	{
		this.VIP = character;
	}

	public L2PcInstance getVIP()
	{
		return this.VIP;
	}

	public boolean isAlive()
	{
		return getAlivePlayerCount() > 0;
	}

	public L2Spawn getGolemSpawn()
	{
		return this.golemSpawn;
	}

	public void setGolemSpawn(L2Spawn spawn)
	{
		if (this.golemSpawn != null && this.golemSpawn.getNpc() != null)
		{
			this.golemSpawn.getNpc().deleteMe();
		}
		this.golemSpawn = spawn;
	}

	public int getGolemId()
	{
		return this.golemId;
	}
}
