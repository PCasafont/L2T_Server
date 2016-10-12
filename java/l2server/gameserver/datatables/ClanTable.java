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

package l2server.gameserver.datatables;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.communitybbs.Manager.ForumsBBSManager;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.instancemanager.FortManager;
import l2server.gameserver.instancemanager.FortSiegeManager;
import l2server.gameserver.instancemanager.SiegeManager;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2ClanMember;
import l2server.gameserver.model.L2PledgeSkillLearn;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Fort;
import l2server.gameserver.model.entity.FortSiege;
import l2server.gameserver.model.entity.Siege;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.util.Util;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.logging.Level;

/**
 * Sorts by member count - descending
 */
class ClanByMemberCountComparator implements Comparator<L2Clan>
{
	@Override
	public int compare(L2Clan o1, L2Clan o2)
	{
		if (o1.getMembersCount() < o2.getMembersCount())
		{
			return -1;
		}
		else if (o1.getMembersCount() > o2.getMembersCount())
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
}

/**
 * This class ...
 *
 * @version $Revision: 1.11.2.5.2.5 $ $Date: 2005/03/27 15:29:18 $
 */
public class ClanTable
{
	private Map<Integer, L2Clan> _clans;

	// Tenkai custom - block recruiting if requesting clan is too big compared to others
	private L2Clan[] _topClansByMemberCount = new L2Clan[10];

	public L2Clan[] getTopTenClansByMemberCount()
	{
		return _topClansByMemberCount;
	}

	public static ClanTable getInstance()
	{
		return SingletonHolder._instance;
	}

	public L2Clan[] getClans()
	{
		return _clans.values().toArray(new L2Clan[_clans.size()]);
	}

	private ClanTable()
	{
		// forums has to be loaded before clan data, because of last forum id used should have also memo included
		if (Config.COMMUNITY_TYPE > 0)
		{
			ForumsBBSManager.getInstance().initRoot();
		}

		_clans = new HashMap<>();
		L2Clan clan;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("SELECT clan_id FROM clan_data ORDER BY clan_level DESC");
			ResultSet result = statement.executeQuery();

			// Count the clans
			int clanCount = 0;

			while (result.next())
			{
				int clanId = result.getInt("clan_id");
				_clans.put(clanId, new L2Clan(clanId));
				clan = getClan(clanId);
				if (clan.getDissolvingExpiryTime() != 0)
				{
					scheduleRemoveClan(clan.getClanId());
				}
				clanCount++;
			}
			result.close();
			statement.close();

			Log.info("Restored " + clanCount + " clans from the database.");
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error restoring ClanTable.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		allianceCheck();

		if (Config.isServer(Config.TENKAI))
		{
			ThreadPoolManager.getInstance()
					.scheduleGeneralAtFixedRate(this::determineTopClansByMemberCount, 0, 3600000);
		}
	}

	private synchronized void determineTopClansByMemberCount()
	{
		Comparator<L2Clan> byMemberCount = new ClanByMemberCountComparator();

		ArrayList<L2Clan> sortedClans = new ArrayList<>(_clans.values());
		Collections.sort(sortedClans, byMemberCount);

		List<L2Clan> temp = sortedClans.subList(0, Math.min(10, sortedClans.size()));
		for (int i = 0; i < _topClansByMemberCount.length && i < temp.size(); i++)
		{
			_topClansByMemberCount[i] = temp.get(i);
		}
	}

	/**
	 * @param clanId
	 * @return
	 */
	public L2Clan getClan(int clanId)
	{

		return _clans.get(clanId);
	}

	public L2Clan getClanByName(String clanName)
	{
		for (L2Clan clan : getClans())
		{
			if (clan.getName().equalsIgnoreCase(clanName))
			{
				return clan;
			}
		}

		return null;
	}

	/**
	 * Creates a new clan and store clan info to database
	 *
	 * @param player
	 * @return NULL if clan with same name already exists
	 */
	public L2Clan createClan(L2PcInstance player, String clanName)
	{
		if (null == player)
		{
			return null;
		}

		if (Config.DEBUG)
		{
			Log.fine(player.getObjectId() + " (" + player.getName() + ") requested a clan creation.");
		}

		if (10 > player.getLevel())
		{
			player.sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN));
			return null;
		}
		if (0 != player.getClanId())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_CREATE_CLAN));
			return null;
		}
		if (System.currentTimeMillis() < player.getClanCreateExpiryTime())
		{
			player.sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.YOU_MUST_WAIT_XX_DAYS_BEFORE_CREATING_A_NEW_CLAN));
			return null;
		}

		if (!setClanNameConditions(player, clanName))
		{
			return null;
		}

		L2Clan clan = new L2Clan(IdFactory.getInstance().getNextId(), clanName);
		L2ClanMember leader =
				new L2ClanMember(clan, player.getName(), player.getLevel(), player.getCurrentClass().getId(),
						player.getObjectId(), player.getPledgeType(), player.getPowerGrade(), player.getTitle(),
						player.getAppearance().getSex(), player.getRace().ordinal());
		clan.setLeader(leader);
		leader.setPlayerInstance(player);
		clan.store();
		player.setClan(clan);
		player.setPledgeClass(leader.calculatePledgeClass(player));
		player.setClanPrivileges(L2Clan.CP_ALL);

		if (Config.isServer(Config.TENKAI_ESTHUS))
		{
			clan.changeLevel(10);

			//Add the skills
			while (PledgeSkillTree.getInstance().getAvailableSkills(player).length != 0)
			{
				L2PledgeSkillLearn[] skills = PledgeSkillTree.getInstance().getAvailableSkills(player);
				if (skills != null)
				{
					for (L2PledgeSkillLearn sk : skills)
					{
						L2Skill s = SkillTable.getInstance().getInfo(sk.getId(), sk.getLevel());
						if (s != null)
						{
							clan.addNewSkill(s);
						}
					}
				}
			}
		}

		if (Config.DEBUG)
		{
			Log.fine("New clan created: " + clan.getClanId() + " " + clan.getName());
		}

		_clans.put(clan.getClanId(), clan);

		//should be update packet only
		player.sendPacket(new PledgeShowInfoUpdate(clan));
		player.sendPacket(new PledgeShowMemberListAll(clan, player));
		player.sendPacket(new UserInfo(player));
		player.sendPacket(new PledgeShowMemberListUpdate(player));
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_CREATED));
		// notify CB server that a new Clan is created
		return clan;
	}

	public boolean setClanNameConditions(L2PcInstance player, String clanName)
	{
		if (player == null)
		{
			return false;
		}

		if (!Util.isAlphaNumeric(clanName) || 2 > clanName.length())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_NAME_INCORRECT));
			return false;
		}
		if (16 < clanName.length())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_NAME_TOO_LONG));
			return false;
		}
		if (null != getClanByName(clanName))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_EXISTS);
			sm.addString(clanName);
			player.sendPacket(sm);
			return false;
		}
		return true;
	}

	public synchronized void destroyClan(int clanId)
	{
		L2Clan clan = getClan(clanId);
		if (clan == null)
		{
			return;
		}

		clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_HAS_DISPERSED));
		int castleId = clan.getHasCastle();
		if (castleId == 0)
		{
			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				siege.removeSiegeClan(clan);
			}
		}
		int fortId = clan.getHasFort();
		if (fortId == 0)
		{
			for (FortSiege siege : FortSiegeManager.getInstance().getSieges())
			{
				siege.removeSiegeClan(clan);
			}
		}
		L2ClanMember leaderMember = clan.getLeader();
		if (leaderMember == null)
		{
			clan.getWarehouse().destroyAllItems("ClanRemove", null, null);
		}
		else
		{
			clan.getWarehouse().destroyAllItems("ClanRemove", clan.getLeader().getPlayerInstance(), null);
		}

		for (L2ClanMember member : clan.getMembers())
		{
			clan.removeClanMember(member.getObjectId(), 0);
		}

		_clans.remove(clanId);
		IdFactory.getInstance().releaseId(clanId);

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM clan_privs WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM clan_skills WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM clan_subpledges WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? OR clan2=?");
			statement.setInt(1, clanId);
			statement.setInt(2, clanId);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM clan_notices WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			statement.close();

			if (castleId != 0)
			{
				statement = con.prepareStatement("UPDATE castle SET taxPercent = 0 WHERE id = ?");
				statement.setInt(1, castleId);
				statement.execute();
				statement.close();
			}
			if (fortId != 0)
			{
				Fort fort = FortManager.getInstance().getFortById(fortId);
				if (fort != null)
				{
					L2Clan owner = fort.getOwnerClan();
					if (clan == owner)
					{
						fort.removeOwner(true);
					}
				}
			}
			if (Config.DEBUG)
			{
				Log.fine("clan removed in db: " + clanId);
			}
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error removing clan from DB.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void scheduleRemoveClan(final int clanId)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			if (getClan(clanId) == null)
			{
				return;
			}
			if (getClan(clanId).getDissolvingExpiryTime() != 0)
			{
				destroyClan(clanId);
			}
		}, Math.max(getClan(clanId).getDissolvingExpiryTime() - System.currentTimeMillis(), 300000));
	}

	public boolean isAllyExists(String allyName)
	{
		for (L2Clan clan : getClans())
		{
			if (clan.getAllyName() != null && clan.getAllyName().equalsIgnoreCase(allyName))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Check for nonexistent alliances
	 */
	private void allianceCheck()
	{
		for (L2Clan clan : _clans.values())
		{
			int allyId = clan.getAllyId();
			if (allyId != 0 && clan.getClanId() != allyId)
			{
				if (!_clans.containsKey(allyId))
				{
					clan.setAllyId(0);
					clan.setAllyName(null);
					clan.changeAllyCrest(0, true);
					clan.updateClanInDB();
					Log.info(getClass().getSimpleName() + ": Removed alliance from clan: " + clan);
				}
			}
		}
	}

	public void storeClanScore()
	{
		for (L2Clan clan : _clans.values())
		{
			clan.updateClanScoreInDB();
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ClanTable _instance = new ClanTable();
	}
}
