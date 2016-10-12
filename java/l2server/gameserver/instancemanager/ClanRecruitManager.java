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

import l2server.L2DatabaseFactory;
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.datatables.PlayerClassTable;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.base.PlayerClass;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.logging.Level;

/**
 * @author Pere
 */
public class ClanRecruitManager
{
	public class ClanRecruitData
	{
		public L2Clan clan = null;
		public int karma = 0;
		public String introduction = "";
		public String largeIntroduction = "";
		public Map<Integer, ClanRecruitWaitingUser> applicants = new HashMap<>();
	}

	public class ClanRecruitWaitingUser
	{
		public int id;
		public String name = "";
		public int classId = 0;
		public int level = 1;
		public int karma = 0;
		public String application = "";
		public ClanRecruitData recruitData = null;
	}

	private Map<Integer, ClanRecruitData> _recruitData = new HashMap<>();
	private Map<Integer, ClanRecruitWaitingUser> _allApplicants = new HashMap<>();
	private Map<Integer, ClanRecruitWaitingUser> _waitingUsers = new HashMap<>();

	public static ClanRecruitManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private ClanRecruitManager()
	{
		load();
	}

	private void load()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"SELECT clan_id, karma, introduction, large_introduction FROM clan_recruit_data");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				L2Clan clan = ClanTable.getInstance().getClan(rset.getInt("clan_id"));
				if (clan == null)
				{
					continue;
				}
				ClanRecruitData data = new ClanRecruitData();
				data.clan = clan;
				data.karma = rset.getInt("karma");
				data.introduction = rset.getString("introduction");
				data.largeIntroduction = rset.getString("large_introduction");
				_recruitData.put(clan.getClanId(), data);
			}
			rset.close();
			statement.close();

			statement = con.prepareStatement(
					"SELECT applicant_id, clan_id, karma, application FROM clan_recruit_applicants");
			rset = statement.executeQuery();
			while (rset.next())
			{
				int applicantId = rset.getInt("applicant_id");
				ClanRecruitData data = _recruitData.get(rset.getInt("clan_id"));

				Connection con2 = null;
				try
				{
					con2 = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement2 = con2.prepareStatement(
							"SELECT char_name, classid, level, clanid FROM characters WHERE charId = ?");
					statement2.setInt(1, applicantId);
					ResultSet rset2 = statement2.executeQuery();
					if (rset2.next() && rset2.getInt("clanid") == 0)
					{
						ClanRecruitWaitingUser applicant = new ClanRecruitWaitingUser();
						applicant.id = applicantId;
						applicant.name = rset2.getString("char_name");
						applicant.classId = rset2.getInt("classid");
						applicant.level = rset2.getInt("level");
						applicant.karma = rset.getInt("karma");
						applicant.application = rset.getString("application");
						applicant.recruitData = data;

						if (data != null)
						{
							data.applicants.put(applicantId, applicant);
							_allApplicants.put(applicantId, applicant);
						}
						else
						{
							_waitingUsers.put(applicantId, applicant);
						}
					}
					rset2.close();
					statement2.close();
				}
				catch (Exception e)
				{
					Log.log(Level.SEVERE, "Error restoring the clan recruit manager.", e);
				}
				finally
				{
					L2DatabaseFactory.close(con2);
				}
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error restoring the clan recruit manager.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		Log.info("Clan recruit manager loaded.");
	}

	public boolean addClan(L2Clan clan, int karma, String introduction, String largeIntroduction)
	{
		if (_recruitData.containsKey(clan.getClanId()))
		{
			return false;
		}

		ClanRecruitData data = new ClanRecruitData();
		data.clan = clan;
		data.karma = karma;
		data.introduction = introduction;
		data.largeIntroduction = largeIntroduction;
		_recruitData.put(clan.getClanId(), data);

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"INSERT INTO clan_recruit_data (clan_id, karma, introduction, large_introduction) VALUES (?, ?, ?, ?)");
			statement.setInt(1, clan.getClanId());
			statement.setInt(2, karma);
			statement.setString(3, introduction);
			statement.setString(4, largeIntroduction);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error storing clan recruit data.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return true;
	}

	public void updateClan(L2Clan clan, int karma, String introduction, String largeIntroduction)
	{
		ClanRecruitData data = _recruitData.get(clan.getClanId());
		if (data == null)
		{
			return;
		}

		data.karma = karma;
		data.introduction = introduction;
		data.largeIntroduction = largeIntroduction;

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"UPDATE clan_recruit_data SET karma = ?, introduction = ?, large_introduction = ? WHERE clan_id = ?");
			statement.setInt(1, karma);
			statement.setString(2, introduction);
			statement.setString(3, largeIntroduction);
			statement.setInt(4, clan.getClanId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error updating clan recruit data.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public boolean removeClan(L2Clan clan)
	{
		ClanRecruitData data = _recruitData.get(clan.getClanId());
		if (data == null)
		{
			return false;
		}

		List<ClanRecruitWaitingUser> toIterate = new ArrayList<>(data.applicants.values());
		for (ClanRecruitWaitingUser applicant : toIterate)
		{
			removeApplicant(applicant.id);
		}

		_recruitData.remove(clan.getClanId());

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM clan_recruit_data WHERE clan_id = ?");
			statement.setInt(1, clan.getClanId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error deleting clan recruit data.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return true;
	}

	public Map<Integer, ClanRecruitData> getRecruitData()
	{
		return _recruitData;
	}

	public ClanRecruitData getRecruitData(int clanId)
	{
		return _recruitData.get(clanId);
	}

	public List<ClanRecruitData> getRecruitData(int level, int karma, boolean clanName, String name, final int sortBy, final boolean desc)
	{
		name = name.toLowerCase();
		List<ClanRecruitData> list = new ArrayList<>();

		for (ClanRecruitData data : _recruitData.values())
		{
			if (level > -1 && data.clan.getLevel() != level || karma > -1 && data.karma != karma)
			{
				continue;
			}

			if (!name.isEmpty())
			{
				if (!clanName && !data.clan.getLeaderName().toLowerCase().contains(name) ||
						clanName && !data.clan.getName().toLowerCase().contains(name))
				{
					continue;
				}
			}

			list.add(data);
		}

		if (sortBy > 0)
		{
			Collections.sort(list, (d1, d2) ->
			{
				try
				{
					int result = d1.clan.getClanId() < d2.clan.getClanId() ? 1 : -1;
					switch (sortBy)
					{
						case 1:
							result = d1.clan.getName().compareTo(d2.clan.getName());
							break;
						case 2:
							result = d1.clan.getLeaderName().compareTo(d2.clan.getLeaderName());
							break;
						case 3:
							result = d1.clan.getLevel() < d2.clan.getLevel() ? 1 :
									d1.clan.getLevel() > d2.clan.getLevel() ? -1 : 0;
							break;
						case 5:
							result = d1.karma < d2.karma ? 1 : d1.karma > d2.karma ? -1 : 0;
							break;
					}

					if (desc)
					{
						result *= -1;
					}

					return result;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				return 1;
			});
		}

		return list;
	}

	public boolean addApplicant(L2PcInstance player, int clanId, String application)
	{
		if (_allApplicants.containsKey(player.getObjectId()))
		{
			return false;
		}

		ClanRecruitData data = _recruitData.get(clanId);
		if (data == null)
		{
			return false;
		}

		ClanRecruitWaitingUser applicant = new ClanRecruitWaitingUser();
		applicant.id = player.getObjectId();
		applicant.name = player.getName();
		applicant.classId = player.getClassId();
		applicant.level = player.getLevel();
		applicant.application = application;
		applicant.recruitData = data;

		data.applicants.put(player.getObjectId(), applicant);
		_allApplicants.put(player.getObjectId(), applicant);

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"INSERT INTO clan_recruit_applicants (applicant_id, clan_id, application) VALUES (?, ?, ?)");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, clanId);
			statement.setString(3, application);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error storing clan application data.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return true;
	}

	public boolean removeApplicant(int playerId)
	{
		ClanRecruitWaitingUser applicant = _allApplicants.get(playerId);
		if (applicant == null)
		{
			return false;
		}

		applicant.recruitData.applicants.remove(playerId);
		_allApplicants.remove(playerId);

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("DELETE FROM clan_recruit_applicants WHERE applicant_id = ?");
			statement.setInt(1, playerId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error deleting clan application data.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return true;
	}

	public ClanRecruitWaitingUser getApplicant(int playerId)
	{
		return _allApplicants.get(playerId);
	}

	public boolean addWaitingUser(L2PcInstance player, int karma)
	{
		if (_waitingUsers.containsKey(player.getObjectId()) || _allApplicants.containsKey(player.getObjectId()))
		{
			return false;
		}

		ClanRecruitWaitingUser waitingUser = new ClanRecruitWaitingUser();
		waitingUser.id = player.getObjectId();
		waitingUser.name = player.getName();
		waitingUser.classId = player.getClassId();
		waitingUser.level = player.getLevel();
		waitingUser.karma = karma;

		_waitingUsers.put(player.getObjectId(), waitingUser);

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("INSERT INTO clan_recruit_applicants (applicant_id, karma) VALUES (?, ?)");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, karma);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error storing clan waiting user data.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return true;
	}

	public boolean removeWaitingUser(L2PcInstance player)
	{
		ClanRecruitWaitingUser waitingUser = _waitingUsers.get(player.getObjectId());
		if (waitingUser == null)
		{
			return false;
		}

		_waitingUsers.remove(waitingUser.id);

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("DELETE FROM clan_recruit_applicants WHERE applicant_id = ?");
			statement.setInt(1, player.getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error deleting clan application data.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return true;
	}

	public ClanRecruitWaitingUser getWaitingUser(int playerId)
	{
		return _waitingUsers.get(playerId);
	}

	public List<ClanRecruitWaitingUser> getWaitingUsers(int minLevel, int maxLevel, int role, final int sortBy, final boolean desc, String name)
	{
		name = name.toLowerCase();
		List<ClanRecruitWaitingUser> result = new ArrayList<>();
		for (ClanRecruitWaitingUser user : _waitingUsers.values())
		{
			if (user.level < minLevel || user.level > maxLevel)
			{
				continue;
			}

			PlayerClass pc = PlayerClassTable.getInstance().getClassById(user.classId);
			int awakening = pc.getAwakeningClassId();
			if (awakening == -1 && pc.getParent() != null)
			{
				awakening = pc.getParent().getAwakeningClassId();
			}
			switch (role)
			{
				case 0: // All
					break;
				case 1: // Warrior
					if (pc.isMage())
					{
						continue;
					}
					break;
				case 2: // Wizard
					if (!pc.isMage())
					{
						continue;
					}
					break;
				case 3: // Close-Range Fighter
					if (awakening != 140)
					{
						continue;
					}
					break;
				case 4: // Close-Range Assassin
					if (awakening != 141)
					{
						continue;
					}
					break;
				case 5: // Long-Range Fighter
					if (awakening != 142)
					{
						continue;
					}
					break;
				case 6: // Defensive Fighter
					if (awakening != 139)
					{
						continue;
					}
					break;
				case 7: // Song/Dance Assist Fighter
					if (awakening != 144)
					{
						continue;
					}
					break;
				case 8: // Wizard
					if (awakening != 143)
					{
						continue;
					}
					break;
				case 9: // Healer/Buffer
					if (awakening != 146)
					{
						continue;
					}
					break;
				case 10: // Summoner
					if (awakening != 145)
					{
						continue;
					}
					break;
			}

			if (!name.isEmpty() && !user.name.toLowerCase().contains(name))
			{
				continue;
			}

			result.add(user);
		}

		if (sortBy > 0)
		{
			Collections.sort(result, (wu1, wu2) ->
			{
				int result1 = wu1.id < wu2.id ? 1 : -1;
				switch (sortBy)
				{
					case 1:
						result1 = wu1.name.compareTo(wu2.name);
						break;
					case 2:
						result1 = wu1.karma < wu2.karma ? 1 : -1;
						break;
					case 3:
						result1 = wu1.classId < wu2.classId ? 1 : -1;
						break;
					case 4:
						result1 = wu1.level < wu2.level ? 1 : -1;
						break;
				}

				if (desc)
				{
					result1 *= -1;
				}

				return result1;
			});
		}

		return result;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ClanRecruitManager _instance = new ClanRecruitManager();
	}
}
