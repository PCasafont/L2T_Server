package l2server.gameserver.model.entity;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.entity.ClanWarManager.ClanWar.WarState;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;

/**
 * @author Xavi
 */
public class ClanWarManager
{
	private static ClanWarManager _instance = null;

	private List<ClanWar> _wars = new ArrayList<>();

	private ClanWarManager()
	{
		load();
		Log.info("Clan war manager started.");
	}

	public static ClanWarManager getInstance()
	{
		return _instance == null ? (_instance = new ClanWarManager()) : _instance;
	}

	public final void load()
	{
		List<ClanWar> wars = new ArrayList<>();
		for (ClanWar war : _wars)
		{
			wars.add(war);
		}
		_wars.clear();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement(
					"SELECT clan1, clan2, start_time, end_time, delete_time, clan1_score, clan2_score, clan1_war_declarator, clan2_war_declarator, clan1_deaths_for_war, clan1_shown_score, clan2_shown_score, loserId, winnerId FROM clan_wars");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				if (ClanTable.getInstance().getClan(rset.getInt("clan1")) == null ||
						ClanTable.getInstance().getClan(rset.getInt("clan2")) == null)
				{
					continue;
				}

				WarState warstate = null;

				if (rset.getLong("delete_time") != 0 && System.currentTimeMillis() > rset.getLong("delete_time"))
				{
					deleteWar(rset.getInt("clan1"), rset.getInt("clan2"));
				}
				else if (rset.getLong("delete_time") != 0)
				{
					warstate = WarState.REPOSE;
				}
				else if (rset.getLong("end_time") != 0 && System.currentTimeMillis() > rset.getLong("end_time"))
				{
					warstate = WarState.REPOSE;
				}
				else if (rset.getLong("end_time") != 0 && System.currentTimeMillis() < rset.getLong("end_time"))
				{
					warstate = WarState.STARTED;
				}
				else if (rset.getInt("clan1_deaths_for_war") >= 5)
				{
					warstate = WarState.STARTED;
				}
				else if (System.currentTimeMillis() >= rset.getLong("start_time"))
				{
					deleteWar(rset.getInt("clan1"), rset.getInt("clan2"));
				}
				else
				{
					warstate = WarState.DECLARED;
				}

				if (warstate != null)
				{
					for (ClanWar war : wars)
					{
						if (war.getClan1() == ClanTable.getInstance().getClan(rset.getInt("clan1")) &&
								war.getClan2() == ClanTable.getInstance().getClan(rset.getInt("clan2")))
						{
							ClanTable.getInstance().getClan(rset.getInt("clan1")).removeWar(war);
							ClanTable.getInstance().getClan(rset.getInt("clan2")).removeWar(war);
						}
					}
					ClanWar war = new ClanWar(rset.getInt("clan1"), rset.getInt("clan2"), rset.getInt("clan1_score"),
							rset.getInt("clan2_score"), rset.getInt("clan1_war_declarator"),
							rset.getInt("clan2_war_declarator"), rset.getInt("clan1_deaths_for_war"),
							rset.getInt("clan1_shown_score"), rset.getInt("clan2_shown_score"), warstate,
							rset.getInt("loserId"), rset.getInt("winnerId"), rset.getLong("start_time"),
							rset.getLong("end_time"), rset.getLong("delete_time"));
					_wars.add(war);
					ClanTable.getInstance().getClan(rset.getInt("clan1")).addWar(war);
					ClanTable.getInstance().getClan(rset.getInt("clan2")).addWar(war);
				}
			}
			statement.close();
			Log.info("Loaded " + _wars.size() + " clan wars.");
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error restoring clan wars data.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void deleteWar(int clanId1, int clanId2)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? AND clan2=?");
			statement.setInt(1, clanId1);
			statement.setInt(2, clanId2);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error updating clan war time.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void storeClansWars(int clanId1, int clanId2, int declaratorCharId)
	{
		L2Clan clan1 = ClanTable.getInstance().getClan(clanId1);
		L2Clan clan2 = ClanTable.getInstance().getClan(clanId2);

		for (ClanWar war : _wars)
		{
			if (war.getClan1() == clan1 && war.getClan2() == clan2 ||
					war.getClan2() == clan1 && war.getClan1() == clan2)
			{
				if (war.getState() != WarState.DECLARED)
				{
					clan1.broadcastMessageToOnlineMembers("You can't declare a war against this clan.");
					return;
				}

				if (war.getClan2() == clan1 && war.getClan1() == clan2)
				{
					if (war.getElapsedTime() >= Config.PREPARE_NORMAL_WAR_PERIOD * 3600)
					{
						// The war is already at Blood Declaration, so it can't be accepted now.
						clan1.broadcastMessageToOnlineMembers(
								"War is already in Blood Declaration, so it can't be accepted now!");
						return;
					}
					war.setWarDeclarator(declaratorCharId);
					war.start();
					return;
				}
			}
		}

		long startTime = 0;

		Connection con = null;
		try
		{
			startTime = System.currentTimeMillis() + Config.PREPARE_MUTUAL_WAR_PERIOD * 3600000L;
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement(
					"REPLACE INTO clan_wars (clan1, clan2, start_time, end_time, delete_time, clan1_score, clan2_score, clan1_war_declarator, clan2_war_declarator, clan1_deaths_for_war, clan1_shown_score, clan2_shown_score) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, clanId1);
			statement.setInt(2, clanId2);
			statement.setLong(3, startTime);
			statement.setLong(4, 0);
			statement.setLong(5, 0);
			statement.setInt(6, 0);
			statement.setInt(7, 0);
			statement.setInt(8, declaratorCharId);
			statement.setInt(9, 0);
			statement.setInt(10, 0);
			statement.setInt(11, 0);
			statement.setInt(12, 0);
			statement.execute();
			statement.close();

			ClanWar war = new ClanWar(clanId1, clanId2, 0, 0, declaratorCharId, 0, 0, 0, 0, WarState.DECLARED, 0, 0,
					startTime, 0, 0);

			_wars.add(war);

			clan1.addWar(war);
			clan2.addWar(war);
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error storing clan wars data.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		SystemMessage msg =
				SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAR_DECLARED_AGAINST_S1_IF_KILLED_LOSE_LOW_EXP);
		msg.addString(clan2.getName());
		clan1.broadcastToOnlineMembers(msg);

		msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_DECLARED_WAR);
		msg.addString(clan1.getName());
		clan2.broadcastToOnlineMembers(msg);
	}

	public void checkSurrender(L2Clan clan1, L2Clan clan2)
	{
		//TODO: To review
		/*int count = 0;
        for (L2ClanMember player : clan1.getMembers())
		{
			if (player != null && player.getPlayerInstance().getWantsPeace() == 1)
				count++;
		}
		if (count == clan1.getMembers().length - 1)
		{
			clan1.deleteEnemyClan(clan2);
			clan2.deleteEnemyClan(clan1);
			deleteClansWars(clan1.getClanId(), clan2.getClanId());
		}*/
	}

	public ClanWar getWar(L2Clan clan1, L2Clan clan2)
	{
		for (ClanWar war : _wars)
		{
			if (war.getClan1() == clan1 && war.getClan2() == clan2 ||
					war.getClan1() == clan2 && war.getClan2() == clan1)
			{
				return war;
			}
		}
		return null;
	}

	public void storeWarData()
	{
		for (ClanWar war : _wars)
		{
			war.saveData();
		}

		Log.log(Level.INFO, "Saved " + _wars.size() + " wars data.");
	}

	public void removeWar(ClanWar war)
	{
		_wars.remove(war);
	}

	public static class ClanWar
	{
		public enum WarState
		{
			DECLARED, STARTED, REPOSE
		}

		public enum WarSituation
		{
			DOMINATING, SUPERIOR, EVENLYMATCHED, INFERIOR, OVERWHELMED
		}

		private L2Clan _clan1;
		private L2Clan _clan2;
		private int _score1;
		private int _score2;
		private int _declarator1;
		private int _declarator2;
		private WarState _warState;
		private long _startTime;
		private long _endTime;
		private long _deleteTime;
		private WarSituation _situation1;
		private WarSituation _situation2;
		private int _clan1DeathsForWar;
		private int _clan1Score;
		private int _clan2Score;
		private L2Clan _loser;
		private L2Clan _winner;
		private boolean _tie = false;
		private ScheduledFuture<?> _task = null;

		public ClanWar(int clanId1, int clanId2, int clanScore1, int clanScore2, int clanWarDeclarator1, int clanWarDeclarator2, int clan1DeathsForWar, int clan1ShownScore, int clan2ShownScore, WarState warState, int loserId, int winnerId, long start_time, long end_time, long delete_time)
		{
			_clan1 = ClanTable.getInstance().getClan(clanId1);
			_clan2 = ClanTable.getInstance().getClan(clanId2);
			_score1 = clanScore1;
			_score2 = clanScore2;
			_declarator1 = clanWarDeclarator1;
			_declarator2 = clanWarDeclarator2;
			_warState = warState;
			_startTime = start_time;
			_endTime = end_time;
			_deleteTime = delete_time;
			_clan1DeathsForWar = clan1DeathsForWar;
			_clan1Score = clan1ShownScore;
			_clan2Score = clan2ShownScore;
			if (loserId != 0 && winnerId != 0)
			{
				_loser = ClanTable.getInstance().getClan(loserId);
				_winner = ClanTable.getInstance().getClan(winnerId);
			}
			else
			{
				_tie = true;
			}
			calculatePosition1();
			calculatePosition2();
			switch (_warState)
			{
				case DECLARED:
					scheduleStart();
					break;
				case STARTED:
					scheduleStop();
					break;
				case REPOSE: // When war is loaded and if it's in repose state, it should be waiting for deletion from the list.
					scheduleDelete();
					break;
			}
		}

		private void scheduleStart()
		{
			if (_task != null && !_task.isDone())
			{
				_task.cancel(true);
			}
			_task = ThreadPoolManager.getInstance().scheduleGeneral(() ->
			{
				if (_warState == WarState.DECLARED)
				{
					delete();
					_clan1.broadcastMessageToOnlineMembers("Clan war against " + _clan2.getName() +
							" has been stopped. Didn't accept declaration!"); // TODO: System message.
				}
			}, _startTime - System.currentTimeMillis());
		}

		private void scheduleStop()
		{
			if (_task != null && !_task.isDone())
			{
				_task.cancel(true);
			}
			_task = ThreadPoolManager.getInstance().scheduleGeneral(() ->
			{
				if (_warState == WarState.STARTED)
				{
					stop();
				}
			}, _endTime - System.currentTimeMillis());
		}

		private void scheduleDelete()
		{
			if (_task != null && !_task.isDone())
			{
				_task.cancel(true);
			}
			_task = ThreadPoolManager.getInstance().scheduleGeneral(() ->
			{
				if (_warState == WarState.REPOSE)
				{
					delete();
				}
			}, _deleteTime - System.currentTimeMillis());
		}

		public void declare(L2Clan declarator)
		{
			Connection con = null;
			try
			{
				if (declarator != _clan1)
				{
					_clan2 = _clan1;
					_clan1 = declarator;

					int temp = _score1;
					_score1 = _score2;
					_score2 = temp;

					temp = _declarator1;
					_declarator1 = _declarator2;
					_declarator2 = temp;

					temp = _clan1Score;
					_clan1Score = _clan2Score;
					_clan2Score = temp;

					WarSituation tempSit = _situation1;
					_situation1 = _situation2;
					_situation2 = tempSit;
				}
				_startTime = System.currentTimeMillis() + Config.PREPARE_MUTUAL_WAR_PERIOD * 3600000L;
				_endTime = 0;
				_deleteTime = 0;
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement;
				statement = con.prepareStatement(
						"UPDATE clan_wars SET start_time=?, end_time=?, delete_time=? WHERE clan1=? AND clan2=?");
				statement.setLong(1, _startTime);
				statement.setLong(2, _endTime);
				statement.setLong(3, _deleteTime);
				statement.setInt(4, _clan1.getClanId());
				statement.setInt(5, _clan2.getClanId());
				statement.execute();
				statement.close();

				_warState = WarState.DECLARED;
				_clan1DeathsForWar = 0;

				_clan1.broadcastClanStatus();
				_clan2.broadcastClanStatus();

				scheduleStart();
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "Error updating clan war time.", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}

			_clan1.broadcastMessageToOnlineMembers("Clan war against " + _clan2.getName() + " has been aborted.");
			_clan2.broadcastMessageToOnlineMembers("Clan war against " + _clan1.getName() + " has been aborted.");
		}

		public void start()
		{
			Connection con = null;
			try
			{
				_startTime = System.currentTimeMillis();
				long endTime = _startTime + Config.BATTLE_WAR_PERIOD * 3600000L;
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement;
				statement =
						con.prepareStatement("UPDATE clan_wars SET start_time=?, end_time=? WHERE clan1=? AND clan2=?");
				statement.setLong(1, _startTime);
				statement.setLong(2, endTime);
				statement.setInt(3, _clan1.getClanId());
				statement.setInt(4, _clan2.getClanId());
				statement.execute();
				statement.close();

				_warState = WarState.STARTED;

				_endTime = endTime;

				_clan1.broadcastClanStatus();
				_clan2.broadcastClanStatus();

				scheduleStop();
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "Error updating clan war time.", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_DECLARED_WAR);
			msg.addString(_clan2.getName());
			_clan1.broadcastToOnlineMembers(msg);

			msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAR_DECLARED_AGAINST_S1_IF_KILLED_LOSE_LOW_EXP);
			msg.addString(_clan1.getName());
			_clan2.broadcastToOnlineMembers(msg);
		}

		public void stop()
		{
			Connection con = null;
			try
			{
				_endTime = System.currentTimeMillis();
				long deleteTime = _endTime + Config.EXPIRE_NORMAL_WAR_PERIOD * 3600000L;
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement;
				statement = con.prepareStatement(
						"UPDATE clan_wars SET end_time=?, delete_time=? WHERE clan1=? AND clan2=?");
				statement.setLong(1, _endTime);
				statement.setLong(2, deleteTime);
				statement.setInt(3, _clan1.getClanId());
				statement.setInt(4, _clan2.getClanId());
				statement.execute();
				statement.close();

				_warState = WarState.REPOSE;

				_deleteTime = deleteTime;

				_clan1.broadcastClanStatus();
				_clan2.broadcastClanStatus();

				scheduleDelete();
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "Error updating clan war time.", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}

			calculateOutcome();

			_clan1.broadcastMessageToOnlineMembers("Clan war against " + _clan2.getName() + " has ended.");
			_clan2.broadcastMessageToOnlineMembers("Clan war against " + _clan1.getName() + " has ended.");
		}

		public void delete()
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? AND clan2=?");
				statement.setInt(1, _clan1.getClanId());
				statement.setInt(2, _clan2.getClanId());
				statement.execute();
				statement.close();

				_clan1.removeWar(this);
				_clan2.removeWar(this);

				ClanWarManager.getInstance().removeWar(this);
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "Error deleting clan war time.", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}

		public void saveData()
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(
						"UPDATE clan_wars SET clan1_score=?, clan2_score=?, clan1_deaths_for_war=?, clan1_shown_score=?, clan2_shown_score=? WHERE clan1=? AND clan2=?");
				statement.setInt(1, _score1);
				statement.setInt(2, _score2);
				statement.setInt(3, _clan1DeathsForWar);
				statement.setInt(4, _clan1Score);
				statement.setInt(5, _clan2Score);
				statement.setInt(6, _clan1.getClanId());
				statement.setInt(7, _clan2.getClanId());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "Error updating clan war info.", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}

		public void setWarDeclarator(int charId)
		{
			_declarator2 = charId;
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement =
						con.prepareStatement("UPDATE clan_wars set clan2_war_declarator=? WHERE clan1=? AND clan2=?");
				statement.setInt(1, charId);
				statement.setInt(2, _clan1.getClanId());
				statement.setInt(3, _clan2.getClanId());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				Log.log(Level.INFO, "Cannot update the second war declarator.", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}

		private void calculateOutcome()
		{
			if (getLoser() == null || getWinner() == null)
			{
				if (getClan1Scores() > getClan2Scores())
				{
					setWinner(getClan1());
					setLoser(getClan2());
				}
				else if (getClan1Scores() < getClan2Scores())
				{
					setLoser(getClan1());
					setWinner(getClan2());
				}
				else
				{
					setTie();
				}
			}

			if (!getTie())
			{
				Connection con = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement(
							"UPDATE clan_wars set loserId=?, winnerId=? WHERE clan1=? AND clan2=?");
					statement.setInt(1, getLoser().getClanId());
					statement.setInt(2, getWinner().getClanId());
					statement.setInt(3, _clan1.getClanId());
					statement.setInt(4, _clan2.getClanId());
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					Log.log(Level.INFO, "Cannot update the second war declarator.", e);
				}
				finally
				{
					L2DatabaseFactory.close(con);
				}
			}

			saveData();
		}

		private void calculatePosition1()
		{
			int maxResult = _score1 + _score2;
			if (maxResult == 0)
			{
				_situation1 = WarSituation.EVENLYMATCHED;
				return;
			}

			int percent = _score1 * 100 / maxResult;
			if (percent >= 85)
			{
				_situation1 = WarSituation.DOMINATING;
			}
			else if (percent >= 65 && percent < 85)
			{
				_situation1 = WarSituation.SUPERIOR;
			}
			else if (percent >= 35 && percent < 65)
			{
				_situation1 = WarSituation.EVENLYMATCHED;
			}
			else if (percent >= 15 && percent < 35)
			{
				_situation1 = WarSituation.INFERIOR;
			}
			else
			{
				_situation1 = WarSituation.OVERWHELMED;
			}
		}

		private void calculatePosition2()
		{
			int maxResult = _score1 + _score2;
			if (maxResult == 0)
			{
				_situation2 = WarSituation.EVENLYMATCHED;
				return;
			}
			int percent = _score2 * 100 / maxResult;
			if (percent >= 85)
			{
				_situation2 = WarSituation.DOMINATING;
			}
			else if (percent >= 65 && percent < 85)
			{
				_situation2 = WarSituation.SUPERIOR;
			}
			else if (percent >= 35 && percent < 65)
			{
				_situation2 = WarSituation.EVENLYMATCHED;
			}
			else if (percent >= 15 && percent < 35)
			{
				_situation2 = WarSituation.INFERIOR;
			}
			else
			{
				_situation2 = WarSituation.OVERWHELMED;
			}
		}

		public L2Clan getClan1()
		{
			return _clan1;
		}

		public L2Clan getClan2()
		{
			return _clan2;
		}

		public WarState getState()
		{
			return _warState;
		}

		public int getDeclarator1()
		{
			return _declarator1;
		}

		public int getDeclarator2()
		{
			return _declarator2;
		}

		public long getStartTimeInMilis()
		{
			return _startTime;
		}

		public long getEndTimeInMilis()
		{
			return _endTime;
		}

		public long getExpirationTimeInMilis()
		{
			return _deleteTime;
		}

		public int getElapsedTime()
		{
			switch (_warState)
			{
				case DECLARED:
					return Config.PREPARE_MUTUAL_WAR_PERIOD * 3600 -
							(int) ((_startTime - System.currentTimeMillis()) / 1000);
				case STARTED:
					return Config.BATTLE_WAR_PERIOD * 3600 - (int) ((_endTime - System.currentTimeMillis()) / 1000);
			}
			return 0;
		}

		public int getClan1Score()
		{
			return _score1;
		}

		public int getclan2Score()
		{
			return _score2;
		}

		public WarSituation getSituation1()
		{
			return _situation1;
		}

		public WarSituation getSituation2()
		{
			return _situation2;
		}

		public void raiseClan1Score()
		{
			_score1++;
		}

		public void raisClan2Score()
		{
			_score2++;
		}

		public void increaseClan1DeathsForClanWar()
		{
			_clan1DeathsForWar++;
		}

		public int getClan1DeathsForClanWar()
		{
			return _clan1DeathsForWar;
		}

		public void increaseClan1Score()
		{
			_clan1Score++;
		}

		public void decreaseClan1Score()
		{
			_clan1Score--;
		}

		public int getClan1Scores()
		{
			return _clan1Score;
		}

		public void increaseClan2Score()
		{
			_clan2Score++;
		}

		public void decreaseClan2Score()
		{
			_clan2Score--;
		}

		public int getClan2Scores()
		{
			return _clan2Score;
		}

		public void setLoser(L2Clan clan)
		{
			_loser = clan;
		}

		public L2Clan getLoser()
		{
			return _loser;
		}

		public void setWinner(L2Clan clan)
		{
			_winner = clan;
		}

		public L2Clan getWinner()
		{
			return _winner;
		}

		public void setTie()
		{
			_tie = true;
		}

		public boolean getTie()
		{
			return _tie;
		}

		public boolean isMutual()
		{
			return getClan1DeathsForClanWar() >= 5;
		}
	}
}
