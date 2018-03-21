package l2server.gameserver.events.Faction;

import l2server.L2DatabaseFactory;
import l2server.gameserver.Announcements;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.util.Broadcast;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

import static java.lang.Math.abs;

/**
 * @author Inia
 * 			Little faction engine
 *
 */

public class FactionManager
{
	public int getPlayerFaction(int char_id)			/* Return the faction of the player */
	{
		Connection get = null;

		try
		{
			get = L2DatabaseFactory.getInstance().getConnection();



			PreparedStatement statement = get.prepareStatement(
					"SELECT faction_id FROM characters WHERE charId = ?");
			statement.setInt(1, char_id);
			ResultSet rset = statement.executeQuery();

			if (rset.next())
			{
				int id = rset.getInt("faction_id");
				return (id);
			}
			rset.close();
			statement.close();
		}

		catch (Exception e)
		{
			Log.log(Level.WARNING, "Couldn't get current ranked points : " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(get);
		}
		return 0;
	}

	public int getFactionPoints(int id) 				/* Return the points of the faction */
	{
		Connection get = null;

		try
		{
			get = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = get.prepareStatement(
					"SELECT points FROM factions WHERE id = ?");
			statement.setInt(1, id);
			ResultSet rset = statement.executeQuery();

			if (rset.next())
			{
				int currentPoints = rset.getInt("points");
				return (currentPoints);
			}
			rset.close();
			statement.close();
		}

		catch (Exception e)
		{
			Log.log(Level.WARNING, "Couldn't get current ranked points : " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(get);
		}
		return 0;
	}

	public String getFactionName(int id)				/* Return the faction's name */
	{
		Connection get = null;

		try
		{
			get = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = get.prepareStatement(
					"SELECT name FROM factions WHERE id = ?");
			statement.setInt(1, id);
			ResultSet rset = statement.executeQuery();

			if (rset.next())
			{
				String factionName = rset.getString("name");
				return (factionName);
			}
			rset.close();
			statement.close();
		}

		catch (Exception e)
		{
			Log.log(Level.WARNING, "Couldn't get current faction name : " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(get);
		}
		return "";
	}

	public String getFactionDesc(int id)				/* Return the faction's desc */
	{
		Connection get = null;

		try
		{
			get = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = get.prepareStatement("SELECT desc FROM factions WHERE id = ?");
			statement.setInt(1, id);
			ResultSet rset = statement.executeQuery();

			if (rset.next())
			{
				String factionDesc = rset.getString("desc");
				return (factionDesc);
			}
			rset.close();
			statement.close();
		}

		catch (Exception e)
		{
			Log.log(Level.WARNING, "Couldn't get current faction desc : " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(get);
		}
		return "";
	}

	public int getFactionMembers(int id)				/* Return the amount of players in the faction */
	{
		Connection get = null;

		try
		{
			get = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = get.prepareStatement(
					"SELECT members FROM factions WHERE id = ?");
			statement.setInt(1, id);
			ResultSet rset = statement.executeQuery();

			if (rset.next())
			{
				int currentPoints = rset.getInt("members");
				return (currentPoints);
			}
			rset.close();
			statement.close();
		}

		catch (Exception e)
		{
			Log.log(Level.WARNING, "Couldn't get current ranked points : " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(get);
		}
		return 0;
	}

	public int getFactionKills(int id)					/* Return the amount of kills of the faction */
	{
		Connection get = null;

		try
		{
			get = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = get.prepareStatement(
					"SELECT kills FROM factions WHERE id = ?");
			statement.setInt(1, id);
			ResultSet rset = statement.executeQuery();

			if (rset.next())
			{
				int currentPoints = rset.getInt("kills");
				return (currentPoints);
			}
			rset.close();
			statement.close();
		}

		catch (Exception e)
		{
			Log.log(Level.WARNING, "Couldn't get current ranked kills : " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(get);
		}
		return 0;
	}

	public int getFactionDeaths(int id)					/* Return the amount of deaths of the faction */
	{
		Connection get = null;

		try
		{
			get = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = get.prepareStatement(
					"SELECT deaths FROM factions WHERE id = ?");
			statement.setInt(1, id);
			ResultSet rset = statement.executeQuery();

			if (rset.next())
			{
				int currentPoints = rset.getInt("deaths");
				return (currentPoints);
			}
			rset.close();
			statement.close();
		}

		catch (Exception e)
		{
			Log.log(Level.WARNING, "Couldn't get current ranked points : " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(get);
		}
		return 0;
	}

	public int getFactionTeamKills(int id)					/* Return the amount of team kills of the faction */
	{
		Connection get = null;

		try
		{
			get = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = get.prepareStatement(
					"SELECT teamKill FROM factions WHERE id = ?");
			statement.setInt(1, id);
			ResultSet rset = statement.executeQuery();

			if (rset.next())
			{
				int currentPoints = rset.getInt("teamKill");
				return (currentPoints);
			}
			rset.close();
			statement.close();
		}

		catch (Exception e)
		{
			Log.log(Level.WARNING, "Couldn't get current teamKill  : " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(get);
		}
		return 0;
	}

	public int getFactionMinLvl(int id)					/* Return the level minimum to join the faction */
	{
		Connection get = null;

		try
		{
			get = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = get.prepareStatement(
					"SELECT minLvl FROM factions WHERE id = ?");
			statement.setInt(1, id);
			ResultSet rset = statement.executeQuery();

			if (rset.next())
			{
				int currentPoints = rset.getInt("minLvl");
				return (currentPoints);
			}
			rset.close();
			statement.close();
		}

		catch (Exception e)
		{
			Log.log(Level.WARNING, "Couldn't get current ranked minLvl : " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(get);
		}
		return 0;
	}

	public int getFactionMinPvp(int id)					/* Return the minimum pvp to join the faction */
	{
		Connection get = null;

		try
		{
			get = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = get.prepareStatement(
					"SELECT minPvp FROM factions WHERE id = ?");
			statement.setInt(1, id);
			ResultSet rset = statement.executeQuery();

			if (rset.next())
			{
				int currentPoints = rset.getInt("minPvp");
				return (currentPoints);
			}
			rset.close();
			statement.close();
		}

		catch (Exception e)
		{
			Log.log(Level.WARNING, "Couldn't get current ranked minPvp : " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(get);
		}
		return 0;
	}

	public int addFactionMember(int id, int amount)		/* Add a player to a faction /Don't Use */
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement =
					con.prepareStatement("UPDATE factions SET members=? WHERE id=?");
			statement.setInt(1, getFactionMembers(id) + amount);
			statement.setInt(2, id);

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed updating Faction Points", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return (0);
	}

	public int subFactionMember(int id, int amount)		/* Remove a player from a faction /Don't Use */
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement =
					con.prepareStatement("UPDATE factions SET members=? WHERE id=?");
			statement.setInt(1, getFactionMembers(id) - amount);
			statement.setInt(2, id);

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed updating Faction Points", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return (0);
	}

	public void setFactionKills(int id, int points)		/* Change the kills of a faction */
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement =
					con.prepareStatement("UPDATE factions SET kills=? WHERE id=?");
			statement.setInt(1, points);
			statement.setInt(2, id);

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed updating Faction kills", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public int addFactionKills(int id, int amount)		/* Add kills to a faction */
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement =
					con.prepareStatement("UPDATE factions SET kills=? WHERE id=?");
			statement.setInt(1, getFactionKills(id) + amount);
			statement.setInt(2, id);

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed updating Faction Points", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return (0);
	}

	public int subFactionKills(int id, int amount)		/* Remove kills from a faction */
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement =
					con.prepareStatement("UPDATE factions SET kills=? WHERE id=?");
			statement.setInt(1, getFactionKills(id) - amount);
			statement.setInt(2, id);

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed updating Faction Points", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return (0);
	}

	public void setFactionDeaths(int id, int points)	/* Change the deaths of a faction */
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement =
					con.prepareStatement("UPDATE factions SET deaths=? WHERE id=?");
			statement.setInt(1, points);
			statement.setInt(2, id);

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed updating Faction Points", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public int addFactionDeaths(int id, int amount)		/* Add deaths to a faction */
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement =
					con.prepareStatement("UPDATE factions SET deaths=? WHERE id=?");
			statement.setInt(1, getFactionDeaths(id) + amount);
			statement.setInt(2, id);

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed updating Faction Points", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return (0);
	}

	public int subFactionTeamKill(int id, int amount)		/* Remove deaths from a faction */
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement =
					con.prepareStatement("UPDATE factions SET teamKill =? WHERE id=?");
			statement.setInt(1, getFactionTeamKills(id) - amount);
			statement.setInt(2, id);

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed updating Faction teamKill", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return (0);
	}

	public void setFactionTeamKill(int id, int points)	/* Change the deaths of a faction */
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement =
					con.prepareStatement("UPDATE factions SET teamKill=? WHERE id=?");
			statement.setInt(1, points);
			statement.setInt(2, id);

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed updating Faction teamKill", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public int addFactionTeamKill(int id, int amount)		/* Add deaths to a faction */
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement =
					con.prepareStatement("UPDATE factions SET teamKill=? WHERE id=?");
			statement.setInt(1, getFactionTeamKills(id) + amount);
			statement.setInt(2, id);

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed updating Faction Points", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return (0);
	}

	public int subFactionDeaths(int id, int amount)		/* Remove deaths from a faction */
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement =
					con.prepareStatement("UPDATE factions SET deaths =? WHERE id=?");
			statement.setInt(1, getFactionDeaths(id) - amount);
			statement.setInt(2, id);

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed updating Faction Points", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return (0);
	}
	public int addFactionPoints(int id, int amount)		/* Add points to a faction */
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement =
					con.prepareStatement("UPDATE factions SET points=? WHERE id=?");
			statement.setInt(1, getFactionPoints(id) + amount);
			statement.setInt(2, id);

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed updating Faction Points", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return (0);
	}

	public int subFactionPoints(int id, int amount)		/* Remove points from a faction */
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement =
					con.prepareStatement("UPDATE factions SET points=? WHERE id=?");
			statement.setInt(1, getFactionPoints(id) - amount);
			statement.setInt(2, id);

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed updating Faction Points", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return (0);
	}

	public void setFactionPlayerPoints(int char_id, int points)		/* Change the faction points of a player */
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement =
					con.prepareStatement("UPDATE characters SET faction_points=? WHERE charId=?");
			statement.setInt(1, points);
			statement.setInt(2, char_id);

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed updating Faction Points", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void setFactionPoints(int id, int points)	/* Change the faction points of a faction */
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement =
					con.prepareStatement("UPDATE factions SET points=? WHERE id=?");
			statement.setInt(1, points);
			statement.setInt(2, id);

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed updating Faction Points", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void setPlayerToFaction(int id, L2PcInstance player)		/* Add player to a faction */
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			if (player.getFactionId() != 0)
				FactionManager.getInstance().subFactionMember(player.getFactionId(), 1);
			FactionManager.getInstance().addFactionMember(id, 1);
			PreparedStatement statement =
					con.prepareStatement("UPDATE characters SET faction_id=? WHERE charId =?");
			statement.setInt(1, id);
			statement.setInt(2, player.getInstanceId());

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed updating Faction id", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public int getFactionAmount(int factionId)			/* Do not use */
	{
		Connection get = null;
		int i = 0;

		Broadcast.toGameMasters("oui");
		try
		{
			get = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = get.prepareStatement(
					"SELECT  charId FROM characters WHERE faction_id = 1");

			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{

				i++;
				Announcements.getInstance().announceToAll(" " + i);
			}

			rset.close();
			statement.close();
			return i;
		}

		catch (Exception e)
		{
			Log.log(Level.WARNING, "Couldn't get current ranked points : " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(get);
		}
		return 0;
	}

	public void	onKillFaction(L2PcInstance killer, L2PcInstance killed) /* Kill */
	{
		int	killer_faction = killer.getFactionId();
		int	killed_faction = killed.getFactionId();

		/* Little verification */
		if ((killed.getInternalIP() == killer.getInternalIP()) ||
				(killed.getExternalIP() == killer.getExternalIP()) ||
				(killed.getHWID() == killer.getHWID()) ||
				(killer.isInOlympiadMode()) == true)
		{
			return;
		}

		if (killed_faction == killer_faction) /* Same Faction */
		{
			subFactionPoints(killed_faction, abs(killer.getLevel() - killed.getLevel()) + 2);
			return;
		}

		addFactionKills(killer_faction, 1);	/* Add 1 kill to the faction */
		addFactionDeaths(killed_faction, 1);	/* Add 1 death to the faction */

		int	reward = ((killed.getFactionPoints() / 20) * (abs(getFactionPoints(killed_faction) - getFactionPoints(killer_faction)) / 5 )) + abs(killer.getLevel() - 105);

		if (reward > 100)
			reward = (int) (100 + Math.pow(reward - 100, 0.85));
		int	lose = reward / 2;

		killer.addFactionPoints(reward);
		killed.subFactionPoints(lose);

		addFactionPoints(killer_faction, 1);
		addFactionPoints(killer_faction ,reward);
		subFactionPoints(killed_faction ,lose);

		return;
	}

	protected FactionManager()
	{
	}

	public static FactionManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final FactionManager _instance = new FactionManager();
	}
}