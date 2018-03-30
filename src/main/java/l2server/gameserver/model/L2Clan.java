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

package l2server.gameserver.model;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.cache.CrestCache;
import l2server.gameserver.communitybbs.BB.Forum;
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.instancemanager.ClanRecruitManager;
import l2server.gameserver.instancemanager.FortManager;
import l2server.gameserver.instancemanager.SiegeManager;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance.TimeStamp;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.entity.ClanWarManager.ClanWar;
import l2server.gameserver.model.entity.ClanWarManager.ClanWar.WarState;
import l2server.gameserver.model.itemcontainer.ClanWarehouse;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.network.serverpackets.PledgeSkillList.SubPledgeSkill;
import l2server.gameserver.util.Broadcast;
import l2server.gameserver.util.Util;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

/**
 * This class ...
 *
 * @version $Revision: 1.7.2.4.2.7 $ $Date: 2005/04/06 16:13:41 $
 */
public class L2Clan {
	private String name;
	private int clanId;
	private L2ClanMember leader;
	private Map<Integer, L2ClanMember> members = new HashMap<>();

	private String allyName;
	private int allyId;
	private int level;
	private int hasCastle;
	private int hasFort;
	private int hasHideout;
	private int hiredGuards;
	private int crestId;
	private int crestLargeId;
	private int allyCrestId;
	private int auctionBiddedAt = 0;
	private long allyPenaltyExpiryTime;
	private int allyPenaltyType;
	private long charPenaltyExpiryTime;
	private long dissolvingExpiryTime;
	// Ally Penalty Types
	/**
	 * Clan leaved ally
	 */
	public static final int PENALTY_TYPE_CLAN_LEAVED = 1;
	/**
	 * Clan was dismissed from ally
	 */
	public static final int PENALTY_TYPE_CLAN_DISMISSED = 2;
	/**
	 * Leader clan dismiss clan from ally
	 */
	public static final int PENALTY_TYPE_DISMISS_CLAN = 3;
	/**
	 * Leader clan dissolve ally
	 */
	public static final int PENALTY_TYPE_DISSOLVE_ALLY = 4;

	private ClanWarehouse warehouse = null;

	private List<ClanWar> wars = new ArrayList<>();

	@SuppressWarnings("unused")
	private Forum forum;

	//  Clan Privileges
	/**
	 * No privilege to manage any clan activity
	 */
	public static final int CP_NOTHING = 0;
	/**
	 * Privilege to join clan
	 */
	public static final int CP_CL_JOIN_CLAN = 2;
	/**
	 * Privilege to give a title
	 */
	public static final int CP_CL_GIVE_TITLE = 4;
	/**
	 * Privilege to view warehouse content
	 */
	public static final int CP_CL_VIEW_WAREHOUSE = 8;
	/**
	 * Privilege to manage clan ranks
	 */
	public static final int CP_CL_MANAGE_RANKS = 16;
	public static final int CP_CL_PLEDGE_WAR = 32;
	public static final int CP_CL_DISMISS = 64;
	/**
	 * Privilege to register clan crest
	 */
	public static final int CP_CL_REGISTER_CREST = 128;
	public static final int CP_CL_APPRENTICE = 256;
	public static final int CP_CL_TROOPS_FAME = 512;
	public static final int CP_CL_SUMMON_AIRSHIP = 1024;
	/**
	 * Privilege to open a door
	 */
	public static final int CP_CH_OPEN_DOOR = 2048;
	public static final int CP_CH_OTHER_RIGHTS = 4096;
	public static final int CP_CH_AUCTION = 8192;
	public static final int CP_CH_DISMISS = 16384;
	public static final int CP_CH_SET_FUNCTIONS = 32768;
	public static final int CP_CS_OPEN_DOOR = 65536;
	public static final int CP_CS_MANOR_ADMIN = 131072;
	public static final int CP_CS_MANAGE_SIEGE = 262144;
	public static final int CP_CS_USE_FUNCTIONS = 524288;
	public static final int CP_CS_DISMISS = 1048576;
	public static final int CP_CS_TAXES = 2097152;
	public static final int CP_CS_MERCENARIES = 4194304;
	public static final int CP_CS_SET_FUNCTIONS = 8388608;
	/**
	 * Privilege to manage all clan activity
	 */
	public static final int CP_ALL = 16777215;

	// Sub-unit types
	/**
	 * Clan subunit type of Academy
	 */
	public static final int SUBUNIT_ACADEMY = -1;
	/**
	 * Clan subunit type of Royal Guard A
	 */
	public static final int SUBUNIT_ROYAL1 = 100;
	/**
	 * Clan subunit type of Royal Guard B
	 */
	public static final int SUBUNIT_ROYAL2 = 200;
	/**
	 * Clan subunit type of Order of Knights A-1
	 */
	public static final int SUBUNIT_KNIGHT1 = 1001;
	/**
	 * Clan subunit type of Order of Knights A-2
	 */
	public static final int SUBUNIT_KNIGHT2 = 1002;
	/**
	 * Clan subunit type of Order of Knights B-1
	 */
	public static final int SUBUNIT_KNIGHT3 = 2001;
	/**
	 * Clan subunit type of Order of Knights B-2
	 */
	public static final int SUBUNIT_KNIGHT4 = 2002;

	/**
	 * HashMap(Integer, L2Skill) containing all skills of the L2Clan
	 */
	private final Map<Integer, L2Skill> skills = new HashMap<>();
	private final Map<Integer, RankPrivs> privs = new HashMap<>();
	private final Map<Integer, SubPledge> subPledges = new HashMap<>();
	private final Map<Integer, L2Skill> subPledgeSkills = new HashMap<>();

	private int reputationScore = 0;
	private int rank = 0;

	private String notice;
	private boolean noticeEnabled = false;
	private static final int MAX_NOTICE_LENGTH = 8192;

	/**
	 * Called if a clan is referenced only by id.
	 * In this case all other data needs to be fetched from db
	 *
	 * @param clanId A valid clan Id to create and restore
	 */
	public L2Clan(int clanId) {
		this.clanId = clanId;
		initializePrivs();
		restore();
	}

	/**
	 * Called only if a new clan is created
	 *
	 * @param clanId   A valid clan Id to create
	 * @param clanName A valid clan name
	 */
	public L2Clan(int clanId, String clanName) {
		this.clanId = clanId;
		name = clanName;
		initializePrivs();
	}

	/**
	 * @return Returns the clanId.
	 */
	public int getClanId() {
		return clanId;
	}

	/**
	 * @param clanId The clanId to set.
	 */
	public void setClanId(int clanId) {
		this.clanId = clanId;
	}

	/**
	 * @return Returns the leaderId.
	 */
	public int getLeaderId() {
		return leader != null ? leader.getObjectId() : 0;
	}

	/**
	 * @return L2ClanMember of clan leader.
	 */
	public L2ClanMember getLeader() {
		return leader;
	}

	/**
	 */
	public void setLeader(L2ClanMember leader) {
		this.leader = leader;
		members.put(leader.getObjectId(), leader);
	}

	public void setNewLeader(L2ClanMember member) {
		if (!getLeader().isOnline()) {
			return;
		}

		if (member == null) {
			return;
		}

		if (!member.isOnline()) {
			return;
		}

		// Avoid the lazy warehouse to load before the leader is changed
		getWarehouse();

		L2PcInstance exLeader = getLeader().getPlayerInstance();
		SiegeManager.getInstance().removeSiegeSkills(exLeader);
		exLeader.setClan(this);
		exLeader.setClanPrivileges(L2Clan.CP_NOTHING);
		exLeader.broadcastUserInfo();

		setLeader(member);
		updateClanInDB();

		exLeader.setPledgeClass(exLeader.getClan().getClanMember(exLeader.getObjectId()).calculatePledgeClass(exLeader));
		exLeader.broadcastUserInfo();
		exLeader.checkItemRestriction();
		L2PcInstance newLeader = member.getPlayerInstance();
		newLeader.setClan(this);
		newLeader.setPledgeClass(member.calculatePledgeClass(newLeader));
		newLeader.setClanPrivileges(L2Clan.CP_ALL);
		if (getLevel() >= SiegeManager.getInstance().getSiegeClanMinLevel()) {
			SiegeManager.getInstance().addSiegeSkills(newLeader);

			// Transferring siege skills TimeStamps from old leader to new leader to prevent unlimited headquarters
			if (!exLeader.getReuseTimeStamp().isEmpty()) {
				for (L2Skill sk : SkillTable.getInstance().getSiegeSkills(newLeader.isNoble(), getHasCastle() > 0)) {
					if (exLeader.getReuseTimeStamp().containsKey(sk.getReuseHashCode())) {
						TimeStamp t = exLeader.getReuseTimeStamp().get(sk.getReuseHashCode());
						newLeader.addTimeStamp(sk, t.getReuse(), t.getStamp());
					}
				}
				newLeader.sendPacket(new SkillCoolTime(newLeader));
			}
		}
		newLeader.broadcastUserInfo();

		broadcastClanStatus();

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_LEADER_PRIVILEGES_HAVE_BEEN_TRANSFERRED_TO_C1);
		sm.addString(newLeader.getName());
		broadcastToOnlineMembers(sm);
		sm = null;

		getWarehouse().updateItemsOwnerId();
		getWarehouse().updateDatabase();
	}

	/**
	 * @return Returns the leaderName.
	 */
	public String getLeaderName() {
		String leader = "Unknown";
		if (this.leader != null) {
			leader = this.leader.getName();
		}

		return leader;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	private void addClanMember(L2ClanMember member) {
		members.put(member.getObjectId(), member);
	}

	public void addClanMember(L2PcInstance player) {
		// Using a different constructor, to make it easier to read
		// L2ClanMember(L2Clan, L2PcInstance)
		// L2ClanMember member = new L2ClanMember(this,player.getName(), player.getLevel(), player.getCurrentClass().getId(), player.getObjectId(), player.getPledgeType(), player.getPowerGrade(), player.getTitle(), player.getAppearance().getSex(), player.getRace().ordinal());
		L2ClanMember member = new L2ClanMember(this, player);
		// store in memory
		//
		addClanMember(member);
		member.setPlayerInstance(player);
		player.setClan(this);
		player.setPledgeClass(member.calculatePledgeClass(player));
		player.sendPacket(new PledgeShowMemberListUpdate(player));
		player.sendPacket(new PledgeSkillList(this));
		addSkillEffects(player);

		ClanRecruitManager.getInstance().removeApplicant(player.getObjectId());
	}

	public void updateClanMember(L2PcInstance player) {
		L2ClanMember member = new L2ClanMember(player);
		if (player.isClanLeader()) {
			setLeader(member);
		}

		addClanMember(member);
		// notify CB server about the change
	}

	public L2ClanMember getClanMember(String name) {
		for (L2ClanMember temp : members.values()) {
			if (temp.getName().equals(name)) {
				return temp;
			}
		}
		return null;
	}

	public L2ClanMember getClanMember(int objectID) {
		return members.get(objectID);
	}

	public void removeClanMember(int objectId, long clanJoinExpiryTime) {
		L2ClanMember exMember = members.remove(objectId);
		if (exMember == null) {
			Log.warning("Member Object ID: " + objectId + " not found in clan while trying to remove");
			return;
		}
		int leadssubpledge = getLeaderSubPledge(objectId);
		if (leadssubpledge != 0) {
			// Sub-unit leader withdraws, position becomes vacant and leader
			// should appoint new via NPC
			getSubPledge(leadssubpledge).setLeaderId(0);
			updateSubPledgeInDB(leadssubpledge);
		}

		if (exMember.getApprentice() != 0) {
			L2ClanMember apprentice = getClanMember(exMember.getApprentice());
			if (apprentice != null) {
				if (apprentice.getPlayerInstance() != null) {
					apprentice.getPlayerInstance().setSponsor(0);
				} else {
					apprentice.initApprenticeAndSponsor(0, 0);
				}

				apprentice.saveApprenticeAndSponsor(0, 0);
			}
		}
		if (exMember.getSponsor() != 0) {
			L2ClanMember sponsor = getClanMember(exMember.getSponsor());
			if (sponsor != null) {
				if (sponsor.getPlayerInstance() != null) {
					sponsor.getPlayerInstance().setApprentice(0);
				} else {
					sponsor.initApprenticeAndSponsor(0, 0);
				}

				sponsor.saveApprenticeAndSponsor(0, 0);
			}
		}
		exMember.saveApprenticeAndSponsor(0, 0);
		if (Config.REMOVE_CASTLE_CIRCLETS) {
			CastleManager.getInstance().removeCirclet(exMember, getHasCastle());
		}
		if (exMember.isOnline()) {
			L2PcInstance player = exMember.getPlayerInstance();
			player.setTitle("");
			player.setApprentice(0);
			player.setSponsor(0);

			if (player.isClanLeader()) {
				SiegeManager.getInstance().removeSiegeSkills(player);
				player.setClanCreateExpiryTime(System.currentTimeMillis() + Config.ALT_CLAN_CREATE_DAYS * 86400000L); //24*60*60*1000 = 86400000
			}
			// remove Clanskills from Player
			removeSkillEffects(player);

			// remove Residential skills
			if (player.getClan().getHasCastle() > 0) {
				CastleManager.getInstance().getCastleByOwner(player.getClan()).removeResidentialSkills(player);
			}
			if (player.getClan().getHasFort() > 0) {
				FortManager.getInstance().getFortByOwner(player.getClan()).removeResidentialSkills(player);
			}
			player.sendSkillList();

			player.setClan(null);

			// players leaving from clan academy have no penalty
			if (exMember.getPledgeType() != -1) {
				player.setClanJoinExpiryTime(clanJoinExpiryTime);
			}

			player.setPledgeClass(exMember.calculatePledgeClass(player));
			player.broadcastUserInfo();
			// disable clan tab
			player.sendPacket(new PledgeShowMemberListDeleteAll());
		} else {
			removeMemberInDatabase(exMember,
					clanJoinExpiryTime,
					getLeaderId() == objectId ? System.currentTimeMillis() + Config.ALT_CLAN_CREATE_DAYS * 86400000L : 0);
		}
	}

	public L2ClanMember[] getMembers() {
		return members.values().toArray(new L2ClanMember[members.size()]);
	}

	public int getMembersCount() {
		return members.size();
	}

	public int getSubPledgeMembersCount(int subpl) {
		int result = 0;
		for (L2ClanMember temp : members.values()) {
			if (temp.getPledgeType() == subpl) {
				result++;
			}
		}
		return result;
	}

	public int getMaxNrOfMembers(int pledgetype) {
		int limit = 0;

		switch (pledgetype) {
			case 0:
				switch (getLevel()) {
					case 3:
						limit = 30;
						break;
					case 2:
						limit = 20;
						break;
					case 1:
						limit = 15;
						break;
					case 0:
						limit = 10;
						break;
					default:
						limit = 40;
						break;
				}
				break;
			case -1:
				limit = 20;
				break;
			case 100:
			case 200:
				switch (getLevel()) {
					case 11:
						limit = 30;
						break;
					default:
						limit = 20;
						break;
				}
				break;
			case 1001:
			case 1002:
			case 2001:
			case 2002:
				switch (getLevel()) {
					case 9:
					case 10:
					case 11:
						limit = 25;
						break;
					default:
						limit = 10;
						break;
				}
				break;
			default:
				break;
		}

		return limit;
	}

	public L2PcInstance[] getOnlineMembers(int exclude) {
		ArrayList<L2PcInstance> list = new ArrayList<>();
		for (L2ClanMember temp : members.values()) {
			if (temp != null && temp.isOnline() && !(temp.getObjectId() == exclude)) {
				list.add(temp.getPlayerInstance());
			}
		}

		return list.toArray(new L2PcInstance[list.size()]);
	}

	public int getOnlineMembersCount() {
		int count = 0;
		for (L2ClanMember temp : members.values()) {
			if (temp == null || !temp.isOnline()) {
				continue;
			}

			count++;
		}

		return count;
	}

	/**
	 * @return
	 */
	public int getAllyId() {
		return allyId;
	}

	/**
	 * @return
	 */
	public String getAllyName() {
		return allyName;
	}

	public void setAllyCrestId(int allyCrestId) {
		this.allyCrestId = allyCrestId;
	}

	/**
	 * @return
	 */
	public int getAllyCrestId() {
		return allyCrestId;
	}

	/**
	 * @return
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @return
	 */
	public int getHasCastle() {
		return hasCastle;
	}

	/**
	 * @return
	 */
	public int getHasFort() {
		return hasFort;
	}

	/**
	 * @return
	 */
	public int getHasHideout() {
		return hasHideout;
	}

	/**
	 * @param crestId The id of pledge crest.
	 */
	public void setCrestId(int crestId) {
		this.crestId = crestId;
	}

	/**
	 * @return Returns the clanCrestId.
	 */
	public int getCrestId() {
		return crestId;
	}

	/**
	 * @param crestLargeId The id of pledge LargeCrest.
	 */
	public void setLargeCrestId(int crestLargeId) {
		this.crestLargeId = crestLargeId;
	}

	/**
	 * @return Returns the clan CrestLargeId
	 */
	public int getLargeCrestId() {
		return crestLargeId;
	}

	/**
	 * @param allyId The allyId to set.
	 */
	public void setAllyId(int allyId) {
		this.allyId = allyId;
	}

	/**
	 * @param allyName The allyName to set.
	 */
	public void setAllyName(String allyName) {
		this.allyName = allyName;
	}

	/**
	 * @param hasCastle The hasCastle to set.
	 */
	public void setHasCastle(int hasCastle) {
		this.hasCastle = hasCastle;
	}

	/**
	 * @param hasFort The hasFort to set.
	 */
	public void setHasFort(int hasFort) {
		this.hasFort = hasFort;
	}

	/**
	 * @param hasHideout The hasHideout to set.
	 */
	public void setHasHideout(int hasHideout) {
		this.hasHideout = hasHideout;
	}

	/**
	 * @param level The level to set.
	 */
	public void setLevel(int level) {
		this.level = level;
		/*if (level >= 2 && forum == null && Config.COMMUNITY_TYPE > 0)
        {

			Forum forum = ForumsBBSManager.getInstance().getForumByName("ClanRoot");

			if (forum != null)
			{
				forum = forum.getChildByName(name);

				if (forum == null)
				{
					forum = ForumsBBSManager.getInstance().createNewForum(name, ForumsBBSManager.getInstance().getForumByName("ClanRoot"), Forum.CLAN, Forum.CLANMEMBERONLY, getClanId());
				}
			}
		}*/
	}

	/**
	 * @param id Player id
	 * @return
	 */
	public boolean isMember(int id) {
		return id != 0 && members.containsKey(id);
	}

	public void updateClanScoreInDB() {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET reputation_score=? WHERE clan_id=?");
			statement.setInt(1, getReputationScore());
			statement.setInt(2, getClanId());
			statement.execute();
		} catch (Exception e) {
			Log.log(Level.WARNING, "Exception on updateClanScoreInDb(): " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	public void updateClanInDB() {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(
					"UPDATE clan_data SET clan_name=?,leader_id=?,ally_id=?,ally_name=?,reputation_score=?,ally_penalty_expiry_time=?,ally_penalty_type=?,char_penalty_expiry_time=?,dissolving_expiry_time=? WHERE clan_id=?");
			statement.setString(1, getName());
			statement.setInt(2, getLeaderId());
			statement.setInt(3, getAllyId());
			statement.setString(4, getAllyName());
			statement.setInt(5, getReputationScore());
			statement.setLong(6, getAllyPenaltyExpiryTime());
			statement.setInt(7, getAllyPenaltyType());
			statement.setLong(8, getCharPenaltyExpiryTime());
			statement.setLong(9, getDissolvingExpiryTime());
			statement.setInt(10, getClanId());
			statement.execute();
			if (Config.DEBUG) {
				Log.fine("New clan leader saved in db: " + getClanId());
			}
		} catch (Exception e) {
			Log.log(Level.SEVERE, "Error saving clan: " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	public void store() {
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"INSERT INTO clan_data (clan_id,clan_name,clan_level,hasCastle,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id) VALUES (?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, getClanId());
			statement.setString(2, getName());
			statement.setInt(3, getLevel());
			statement.setInt(4, getHasCastle());
			statement.setInt(5, getAllyId());
			statement.setString(6, getAllyName());
			statement.setInt(7, getLeaderId());
			statement.setInt(8, getCrestId());
			statement.setInt(9, getLargeCrestId());
			statement.setInt(10, getAllyCrestId());
			statement.execute();
			statement.close();

			if (Config.DEBUG) {
				Log.fine("New clan saved in db: " + getClanId());
			}
		} catch (Exception e) {
			Log.log(Level.SEVERE, "Error saving new clan: " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	private void removeMemberInDatabase(L2ClanMember member, long clanJoinExpiryTime, long clanCreateExpiryTime) {
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"UPDATE characters SET clanid=0, title=?, clan_join_expiry_time=?, clan_create_expiry_time=?, clan_privs=0, wantspeace=0, subpledge=0, lvl_joined_academy=0, apprentice=0, sponsor=0 WHERE charId=?");
			statement.setString(1, "");
			statement.setLong(2, clanJoinExpiryTime);
			statement.setLong(3, clanCreateExpiryTime);
			statement.setInt(4, member.getObjectId());
			statement.execute();
			statement.close();
			if (Config.DEBUG) {
				Log.fine("clan member removed in db: " + getClanId());
			}

			statement = con.prepareStatement("UPDATE characters SET apprentice=0 WHERE apprentice=?");
			statement.setInt(1, member.getObjectId());
			statement.execute();
			statement.close();

			statement = con.prepareStatement("UPDATE characters SET sponsor=0 WHERE sponsor=?");
			statement.setInt(1, member.getObjectId());
			statement.execute();
			statement.close();
		} catch (Exception e) {
			Log.log(Level.SEVERE, "Error removing clan member: " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	private void restore() {
		Connection con = null;
		try {
			L2ClanMember member;

			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"SELECT clan_name,clan_level,hasCastle,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id,reputation_score,auction_bid_at,ally_penalty_expiry_time,ally_penalty_type,char_penalty_expiry_time,dissolving_expiry_time FROM clan_data WHERE clan_id=?");
			statement.setInt(1, getClanId());
			ResultSet clanData = statement.executeQuery();

			if (clanData.next()) {
				setName(clanData.getString("clan_name"));
				setLevel(clanData.getInt("clan_level"));
				setHasCastle(clanData.getInt("hasCastle"));
				setAllyId(clanData.getInt("ally_id"));
				setAllyName(clanData.getString("ally_name"));
				setAllyPenaltyExpiryTime(clanData.getLong("ally_penalty_expiry_time"), clanData.getInt("ally_penalty_type"));
				if (getAllyPenaltyExpiryTime() < System.currentTimeMillis()) {
					setAllyPenaltyExpiryTime(0, 0);
				}
				setCharPenaltyExpiryTime(clanData.getLong("char_penalty_expiry_time"));
				if (getCharPenaltyExpiryTime() + Config.ALT_CLAN_JOIN_DAYS * 86400000L < System.currentTimeMillis()) //24*60*60*1000 = 86400000
				{
					setCharPenaltyExpiryTime(0);
				}
				setDissolvingExpiryTime(clanData.getLong("dissolving_expiry_time"));

				setCrestId(clanData.getInt("crest_id"));
				setLargeCrestId(clanData.getInt("crest_large_id"));
				setAllyCrestId(clanData.getInt("ally_crest_id"));

				setReputationScore(clanData.getInt("reputation_score"), false);
				setAuctionBiddedAt(clanData.getInt("auction_bid_at"), false);

				int leaderId = clanData.getInt("leader_id");

				PreparedStatement statement2 = con.prepareStatement(
						"SELECT char_name,level,classid,charId,title,power_grade,subpledge,apprentice,sponsor,sex,race FROM characters WHERE clanid=?");
				statement2.setInt(1, getClanId());
				ResultSet clanMembers = statement2.executeQuery();

				while (clanMembers.next()) {
					member = new L2ClanMember(this,
							clanMembers.getString("char_name"),
							clanMembers.getInt("level"),
							clanMembers.getInt("classid"),
							clanMembers.getInt("charId"),
							clanMembers.getInt("subpledge"),
							clanMembers.getInt("power_grade"),
							clanMembers.getString("title"),
							clanMembers.getInt("sex") != 0,
							clanMembers.getInt("race"));
					if (member.getObjectId() == leaderId) {
						setLeader(member);
					} else {
						addClanMember(member);
					}
					member.initApprenticeAndSponsor(clanMembers.getInt("apprentice"), clanMembers.getInt("sponsor"));
				}
				clanMembers.close();
				statement2.close();
			}

			clanData.close();
			statement.close();

			if (Config.DEBUG && getName() != null) {
				Log.info("Restored clan data for \"" + getName() + "\" from database.");
			}
			restoreSubPledges();
			restoreRankPrivs();
			restoreSkills();
			restoreNotice();
			checkCrests();
		} catch (Exception e) {
			Log.log(Level.SEVERE, "Error restoring clan data: " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	private void restoreNotice() {
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT enabled,notice FROM clan_notices WHERE clan_id=?");
			statement.setInt(1, getClanId());
			ResultSet noticeData = statement.executeQuery();

			while (noticeData.next()) {
				noticeEnabled = noticeData.getBoolean("enabled");
				notice = noticeData.getString("notice");
			}

			noticeData.close();
			statement.close();
		} catch (Exception e) {
			Log.log(Level.SEVERE, "Error restoring clan notice: " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	private void storeNotice(String notice, boolean enabled) {
		if (notice == null) {
			notice = "";
		}

		if (notice.length() > MAX_NOTICE_LENGTH) {
			notice = notice.substring(0, MAX_NOTICE_LENGTH - 1);
		}

		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("INSERT INTO clan_notices (clan_id,notice,enabled) VALUES (?,?,?) ON DUPLICATE KEY UPDATE notice=?,enabled=?");
			statement.setInt(1, getClanId());
			statement.setString(2, notice);
			if (enabled) {
				statement.setString(3, "true");
			} else {
				statement.setString(3, "false");
			}
			statement.setString(4, notice);
			if (enabled) {
				statement.setString(5, "true");
			} else {
				statement.setString(5, "false");
			}
			statement.execute();
			statement.close();
		} catch (Exception e) {
			Log.log(Level.WARNING, "Error could not store clan notice: " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}

		this.notice = notice;
		noticeEnabled = enabled;
	}

	public void setNoticeEnabled(boolean enabled) {
		storeNotice(notice, enabled);
	}

	public void setNotice(String notice) {
		storeNotice(notice, noticeEnabled);
	}

	public boolean isNoticeEnabled() {
		return noticeEnabled;
	}

	public String getNotice() {
		if (notice == null) {
			return "";
		}
		return notice;
	}

	private void restoreSkills() {
		Connection con = null;

		try {
			// Retrieve all skills of this L2PcInstance from the database
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT skill_id,skill_level,sub_pledge_id FROM clan_skills WHERE clan_id=?");
			statement.setInt(1, getClanId());

			ResultSet rset = statement.executeQuery();

			// Go though the recordset of this SQL query
			while (rset.next()) {
				int id = rset.getInt("skill_id");
				int level = rset.getInt("skill_level");
				// Create a L2Skill object for each record
				L2Skill skill = SkillTable.getInstance().getInfo(id, level);
				// Add the L2Skill object to the L2Clan skills
				int subType = rset.getInt("sub_pledge_id");

				if (subType == -2) {
					skills.put(skill.getId(), skill);
				} else if (subType == 0) {
					subPledgeSkills.put(skill.getId(), skill);
				} else {
					SubPledge subunit = subPledges.get(subType);
					if (subunit != null) {
						subunit.addNewSkill(skill);
					} else {
						Log.info("Missing subpledge " + subType + " for clan " + this + ", skill skipped.");
					}
				}
			}

			rset.close();
			statement.close();
		} catch (Exception e) {
			Log.log(Level.SEVERE, "Error restoring clan skills: " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * used to retrieve all skills
	 */
	public final L2Skill[] getAllSkills() {
		if (skills == null) {
			return new L2Skill[0];
		}

		return skills.values().toArray(new L2Skill[skills.values().size()]);
	}

	public Map<Integer, L2Skill> getMainClanSubSkills() {
		return subPledgeSkills;
	}

	/**
	 * used to add a skill to skill list of this L2Clan
	 */
	public L2Skill addSkill(L2Skill newSkill) {
		L2Skill oldSkill = null;

		if (newSkill != null) {
			// Replace oldSkill by newSkill or Add the newSkill
			oldSkill = skills.put(newSkill.getId(), newSkill);
		}

		return oldSkill;
	}

	public L2Skill addNewSkill(L2Skill newSkill) {
		return addNewSkill(newSkill, -2);
	}

	/**
	 * used to add a new skill to the list, send a packet to all online clan members, update their stats and store it in db
	 */
	public L2Skill addNewSkill(L2Skill newSkill, int subType) {
		L2Skill oldSkill = null;
		Connection con = null;

		if (newSkill != null) {

			if (subType == -2) // regular clan skill
			{
				oldSkill = skills.put(newSkill.getId(), newSkill);
			} else if (subType == 0) // main clan sub skill
			{
				oldSkill = subPledgeSkills.put(newSkill.getId(), newSkill);
			} else {
				SubPledge subunit = getSubPledge(subType);
				if (subunit != null) {
					oldSkill = subunit.addNewSkill(newSkill);
				} else {
					Log.log(Level.WARNING, "Subpledge " + subType + " does not exist for clan " + this);
					return oldSkill;
				}
			}

			try {
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement;

				if (oldSkill != null) {
					statement = con.prepareStatement("UPDATE clan_skills SET skill_level=? WHERE skill_id=? AND clan_id=?");
					statement.setInt(1, newSkill.getLevelHash());
					statement.setInt(2, oldSkill.getId());
					statement.setInt(3, getClanId());
					statement.execute();
					statement.close();
				} else {
					statement =
							con.prepareStatement("INSERT INTO clan_skills (clan_id,skill_id,skill_level,skill_name,sub_pledge_id) VALUES (?,?,?,?,?)");
					statement.setInt(1, getClanId());
					statement.setInt(2, newSkill.getId());
					statement.setInt(3, newSkill.getLevelHash());
					statement.setString(4, newSkill.getName());
					statement.setInt(5, subType);
					statement.execute();
					statement.close();
				}
			} catch (Exception e) {
				Log.log(Level.WARNING, "Error could not store clan skills: " + e.getMessage(), e);
			} finally {
				L2DatabaseFactory.close(con);
			}

			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_SKILL_S1_ADDED);
			sm.addSkillName(newSkill.getId());

			for (L2ClanMember temp : members.values()) {
				if (temp != null && temp.getPlayerInstance() != null && temp.isOnline()) {
					if (subType == -2) {
						temp.getPlayerInstance()
								.sendSysMessage("Minimum Pledge Class for " + newSkill.getName() + " = " + newSkill.getMinPledgeClass() +
										". Your Pledge Class = " + temp.getPlayerInstance().getPledgeClass());
						if (newSkill.getMinPledgeClass() <= temp.getPlayerInstance().getPledgeClass()) {
							temp.getPlayerInstance().addSkill(newSkill, false); // Skill is not saved to player DB
							temp.getPlayerInstance().sendPacket(new PledgeSkillListAdd(newSkill.getId(), newSkill.getLevelHash()));
							temp.getPlayerInstance().sendPacket(sm);
							temp.getPlayerInstance().sendSkillList();
						}
					} else {
						temp.getPlayerInstance()
								.sendSysMessage(newSkill.getName() + " Pledge = " + subType + ", Your Pledge = " + temp.getPledgeType() + ".");
						if (temp.getPledgeType() == subType) {
							temp.getPlayerInstance().addSkill(newSkill, false); // Skill is not saved to player DB
							temp.getPlayerInstance().sendPacket(new ExSubPledgeSkillAdd(subType, newSkill.getId(), newSkill.getLevelHash()));
							temp.getPlayerInstance().sendPacket(sm);
							temp.getPlayerInstance().sendSkillList();
						}
					}
				}
			}
		}

		return oldSkill;
	}

	public void addSkillEffects() {
		for (L2Skill skill : skills.values()) {
			for (L2ClanMember temp : members.values()) {
				try {
					if (temp != null && temp.isOnline()) {
						if (skill.getMinPledgeClass() <= temp.getPlayerInstance().getPledgeClass()) {
							temp.getPlayerInstance().addSkill(skill, false); // Skill is not saved to player DB
						}
					}
				} catch (NullPointerException e) {
					Log.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
	}

	public void addSkillEffects(L2PcInstance player) {
		addSkillEffects(player, false);
	}

	public void addSkillEffects(L2PcInstance player, boolean activesOnly) {
		if (player == null || reputationScore < 0) {
			return;
		}

		for (L2Skill skill : skills.values()) {
			if (skill.isPassive() && activesOnly) {
				continue;
			}

			if (skill.getMinPledgeClass() <= player.getPledgeClass()) {
				player.addSkill(skill, false); // Skill is not saved to player DB
			}
		}

		if (player.getPledgeType() == 0) {
			for (L2Skill skill : subPledgeSkills.values()) {
				if (skill.isPassive() && activesOnly) {
					continue;
				}

				player.addSkill(skill, false); // Skill is not saved to player DB
			}
		} else {
			SubPledge subunit = getSubPledge(player.getPledgeType());
			if (subunit == null) {
				return;
			}
			for (L2Skill skill : subunit.getSkills()) {
				if (skill.isPassive() && activesOnly) {
					continue;
				}

				player.addSkill(skill, false); // Skill is not saved to player DB
			}
		}
	}

	public void removeSkillEffects(L2PcInstance player) {
		removeSkillEffects(player, false);
	}

	public void removeSkillEffects(L2PcInstance player, boolean activesOnly) {
		if (player == null) {
			return;
		}

		for (L2Skill skill : skills.values()) {
			if (skill.isPassive() && activesOnly) {
				continue;
			}

			player.removeSkill(skill, false); // Skill is not saved to player DB
		}

		if (player.getPledgeType() == 0) {
			for (L2Skill skill : subPledgeSkills.values()) {
				if (skill.isPassive() && activesOnly) {
					continue;
				}

				player.removeSkill(skill, false); // Skill is not saved to player DB
			}
		} else {
			SubPledge subunit = getSubPledge(player.getPledgeType());
			if (subunit == null) {
				return;
			}
			for (L2Skill skill : subunit.getSkills()) {
				if (skill.isPassive() && activesOnly) {
					continue;
				}

				player.removeSkill(skill, false); // Skill is not saved to player DB
			}
		}
	}

	public void broadcastToOnlineAllyMembers(L2GameServerPacket packet) {
		if (getAllyId() == 0) {
			return;
		}
		for (L2Clan clan : ClanTable.getInstance().getClans()) {
			if (clan.getAllyId() == getAllyId()) {
				clan.broadcastToOnlineMembers(packet);
			}
		}
	}

	public void broadcastToOnlineMembers(L2GameServerPacket packet) {
		for (L2ClanMember member : members.values()) {
			if (member != null && member.isOnline()) {
				member.getPlayerInstance().sendPacket(packet);
			}
		}
	}

	public void broadcastCSToOnlineMembers(CreatureSay packet, L2PcInstance broadcaster) {
		for (L2ClanMember member : members.values()) {
			if (member != null && member.isOnline() && !BlockList.isBlocked(member.getPlayerInstance(), broadcaster)) {
				member.getPlayerInstance().sendPacket(packet);
			}
		}
	}

	public void broadcastToOtherOnlineMembers(L2GameServerPacket packet, L2PcInstance player) {
		for (L2ClanMember member : members.values()) {
			if (member != null && member.isOnline() && member.getPlayerInstance() != player) {
				member.getPlayerInstance().sendPacket(packet);
			}
		}
	}

	public void broadcastMessageToOnlineMembers(String message) {
		for (L2ClanMember member : members.values()) {
			if (member != null && member.isOnline()) {
				member.getPlayerInstance().sendMessage(message);
			}
		}
	}

	@Override
	public String toString() {
		return getName() + "[" + getClanId() + "]";
	}

	public synchronized ClanWarehouse getWarehouse() {
		if (warehouse == null) {
			warehouse = new ClanWarehouse(this);
			warehouse.restore();
		}

		return warehouse;
	}

	public List<ClanWar> getWars() {
		return wars;
	}

	public boolean isAtWarWith(Integer id) {
		if (!wars.isEmpty()) {
			for (ClanWar war : wars) {
				if (war.getState() == WarState.STARTED && (war.getClan1().getClanId() == id || war.getClan2().getClanId() == id)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isAtWarWith(L2Clan clan) {
		if (!wars.isEmpty()) {
			for (ClanWar war : wars) {
				if (war.getState() == WarState.STARTED && (war.getClan1() == clan || war.getClan2() == clan)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isAtWarAttacker(Integer id) {
		if (!wars.isEmpty()) {
			for (ClanWar war : wars) {
				if (war.getState() == WarState.STARTED && war.getClan1().getClanId() == id) {
					return true;
				}
			}
		}
		return false;
	}

	public List<L2Clan> getClansAtWarRepose() {
		List<L2Clan> clanList = new ArrayList<>();
		for (ClanWar war : wars) {
			if (war.getState() == WarState.REPOSE) {
				clanList.add(this == war.getClan1() ? war.getClan2() : war.getClan1());
			}
		}
		return clanList;
	}

	public List<L2Clan> getClansAtWarQueue() {
		List<L2Clan> clanList = new ArrayList<>();
		for (ClanWar war : wars) {
			if (war.getState() == WarState.DECLARED) {
				clanList.add(this == war.getClan1() ? war.getClan2() : war.getClan1());
			}
		}
		return clanList;
	}

	public List<L2Clan> getEnemiesQueue() {
		List<L2Clan> clanList = new ArrayList<>();
		for (ClanWar war : wars) {
			if (war.getState() == WarState.DECLARED &&
					this == war.getClan1()) // If is on queue & this clan == attacker (Allways 0 is the clan who declared the war first)
			{
				clanList.add(war.getClan2());
			}
		}
		return clanList;
	}

	public void addWar(ClanWar war) {
		if (!wars.contains(war)) {
			wars.add(war);
		}
	}

	public void removeWar(ClanWar war) {
		if (wars.contains(war)) {
			wars.remove(war);
		}
	}

	public boolean isOnTheEnemiesQueue(L2Clan clan) {
		for (ClanWar war : wars) {
			if (war.getState() == WarState.DECLARED && war.getClan2() == clan) {
				return true;
			}
		}
		return false;
	}

	public List<L2Clan> getAttackersQueue() {
		List<L2Clan> clanList = new ArrayList<>();
		for (ClanWar war : wars) {
			if (war.getState() == WarState.DECLARED &&
					this != war.getClan1()) // If is on queue & this clan != attacker (Allways 0 is the clan who declared the war first)
			{
				clanList.add(war.getClan1());
			}
		}
		return clanList;
	}

	public boolean isOnTheAttackersQueue(L2Clan clan) {
		for (ClanWar war : wars) {
			if (war.getState() == WarState.DECLARED && war.getClan1() == clan) {
				return true;
			}
		}
		return false;
	}

	public boolean isOnWarRepose(L2Clan clan) {
		for (ClanWar war : wars) {
			if (war.getState() == WarState.REPOSE && (war.getClan1() == clan || war.getClan2() == clan)) {
				return true;
			}
		}
		return false;
	}

	public int getHiredGuards() {
		return hiredGuards;
	}

	public void incrementHiredGuards() {
		hiredGuards++;
	}

	public boolean isAtWar() {
		if (!wars.isEmpty()) {
			for (ClanWar war : wars) {
				if (war.getState() == WarState.STARTED) {
					return true;
				}
			}
		}
		return false;
	}

	public List<L2Clan> getWarList() {
		List<L2Clan> clanList = new ArrayList<>();
		for (ClanWar war : wars) {
			if (war.getState() == WarState.STARTED &&
					this == war.getClan1()) // If is on queue & this clan == attacker (Allways 0 is the clan who declared the war first)
			{
				clanList.add(war.getClan2());
			}
		}

		return clanList;
	}

	public List<L2Clan> getAttackerList() {
		List<L2Clan> clanList = new ArrayList<>();
		for (ClanWar war : wars) {
			if (war.getState() == WarState.STARTED &&
					this != war.getClan1()) // If is on queue & this clan != attacker (Allways 0 is the clan who declared the war first)
			{
				clanList.add(war.getClan1());
			}
		}
		return clanList;
	}

	public List<L2Clan> getDeclaredWars() {
		List<L2Clan> clanList = new ArrayList<>();
		for (ClanWar war : wars) {
			if (war.getState() != WarState.REPOSE && this == war.getClan1()) {
				clanList.add(war.getClan2());
			}
		}

		return clanList;
	}

	public List<L2Clan> getUnderAttackWars() {
		List<L2Clan> clanList = new ArrayList<>();
		for (ClanWar war : wars) {
			if (war.getState() != WarState.REPOSE && this != war.getClan1()) {
				clanList.add(war.getClan1());
			}
		}

		return clanList;
	}

	public List<L2Clan> getClanWars() {
		List<L2Clan> clanList = new ArrayList<>();
		for (ClanWar war : wars) {
			clanList.add(this == war.getClan1() ? war.getClan2() : war.getClan1());
		}

		return clanList;
	}

	public List<L2Clan> getStartedWarList() {
		List<L2Clan> clanList = new ArrayList<>();
		for (ClanWar war : wars) {
			if (war.getState() == WarState.STARTED) {
				clanList.add(this == war.getClan1() ? war.getClan2() : war.getClan1());
			}
		}
		return clanList;
	}

	public void broadcastClanStatus() {
		for (L2PcInstance member : getOnlineMembers(0)) {
			member.sendPacket(new PledgeShowMemberListDeleteAll());
			member.sendPacket(new PledgeShowMemberListAll(this, member));
            /*
			member.sendPacket(new PledgeStatusChanged(this));
			member.sendPacket(new PledgeShowMemberListUpdate(member));*/
			member.sendPacket(new UserInfo(member));
			member.sendPacket(new PledgeSkillList(this));
		}
	}

	public int getWarDeclarator(L2Clan clan) {
		for (ClanWar war : wars) {
			if (war.getClan1() == clan) {
				return war.getDeclarator2();
			}
			if (war.getClan2() == clan) {
				return war.getDeclarator1();
			}
		}
		return 0;
	}

	public static class SubPledge {
		private int id;
		private String subPledgeName;
		private int leaderId;
		private final Map<Integer, L2Skill> subPledgeSkills = new HashMap<>();

		public SubPledge(int id, String name, int leaderId) {
			this.id = id;
			subPledgeName = name;
			this.leaderId = leaderId;
		}

		public int getId() {
			return id;
		}

		public String getName() {
			return subPledgeName;
		}

		public void setName(String name) {
			subPledgeName = name;
		}

		public int getLeaderId() {
			return leaderId;
		}

		public void setLeaderId(int leaderId) {
			this.leaderId = leaderId;
		}

		public L2Skill addNewSkill(L2Skill skill) {
			return subPledgeSkills.put(skill.getId(), skill);
		}

		public Collection<L2Skill> getSkills() {
			return subPledgeSkills.values();
		}
	}

	public static class RankPrivs {
		private int rankId;
		private int party;// TODO find out what this stuff means and implement it
		private int rankPrivs;

		public RankPrivs(int rank, int party, int privs) {
			rankId = rank;
			this.party = party;
			rankPrivs = privs;
		}

		public int getRank() {
			return rankId;
		}

		public int getParty() {
			return party;
		}

		public int getPrivs() {
			return rankPrivs;
		}

		public void setPrivs(int privs) {
			rankPrivs = privs;
		}
	}

	private void restoreSubPledges() {
		Connection con = null;

		try {
			// Retrieve all subpledges of this clan from the database
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT sub_pledge_id,name,leader_id FROM clan_subpledges WHERE clan_id=?");
			statement.setInt(1, getClanId());
			ResultSet rset = statement.executeQuery();

			while (rset.next()) {
				int id = rset.getInt("sub_pledge_id");
				String name = rset.getString("name");
				int leaderId = rset.getInt("leader_id");
				// Create a SubPledge object for each record
				SubPledge pledge = new SubPledge(id, name, leaderId);
				subPledges.put(id, pledge);
			}

			rset.close();
			statement.close();
		} catch (Exception e) {
			Log.log(Level.WARNING, "Could not restore clan sub-units: " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * used to retrieve subPledge by type
	 */
	public final SubPledge getSubPledge(int pledgeType) {
		if (subPledges == null) {
			return null;
		}

		return subPledges.get(pledgeType);
	}

	/**
	 * used to retrieve subPledge by type
	 */
	public final SubPledge getSubPledge(String pledgeName) {
		if (subPledges == null) {
			return null;
		}

		for (SubPledge sp : subPledges.values()) {
			if (sp.getName().equalsIgnoreCase(pledgeName)) {
				return sp;
			}
		}
		return null;
	}

	/**
	 * used to retrieve all subPledges
	 */
	public final SubPledge[] getAllSubPledges() {
		if (subPledges == null) {
			return new SubPledge[0];
		}

		return subPledges.values().toArray(new SubPledge[subPledges.values().size()]);
	}

	public SubPledge createSubPledge(L2PcInstance player, int pledgeType, int leaderId, String subPledgeName) {
		SubPledge subPledge = null;
		pledgeType = getAvailablePledgeTypes(pledgeType);
		if (pledgeType == 0) {
			if (pledgeType == L2Clan.SUBUNIT_ACADEMY) {
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY));
			} else {
				player.sendMessage("You can't create any more sub-units of this type");
			}
			return null;
		}
		if (leader.getObjectId() == leaderId) {
			player.sendMessage("Leader is not correct");
			return null;
		}

		// Royal Guard 5000 points per each
		// Order of Knights 10000 points per each
		if (pledgeType != -1 && (getReputationScore() < 5000 && pledgeType < L2Clan.SUBUNIT_KNIGHT1 ||
				getReputationScore() < 10000 && pledgeType > L2Clan.SUBUNIT_ROYAL2)) {
			SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
			player.sendPacket(sp);
			return null;
		} else {
			Connection con = null;
			try {
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement =
						con.prepareStatement("INSERT INTO clan_subpledges (clan_id,sub_pledge_id,name,leader_id) VALUES (?,?,?,?)");
				statement.setInt(1, getClanId());
				statement.setInt(2, pledgeType);
				statement.setString(3, subPledgeName);
				if (pledgeType != -1) {
					statement.setInt(4, leaderId);
				} else {
					statement.setInt(4, 0);
				}
				statement.execute();
				statement.close();

				subPledge = new SubPledge(pledgeType, subPledgeName, leaderId);
				subPledges.put(pledgeType, subPledge);

				if (pledgeType != -1) {
					// Royal Guard 5000 points per each
					// Order of Knights 10000 points per each
					if (pledgeType < L2Clan.SUBUNIT_KNIGHT1) {
						setReputationScore(getReputationScore() - Config.ROYAL_GUARD_COST, true);
					} else {
						setReputationScore(getReputationScore() - Config.KNIGHT_UNIT_COST, true);
					}
					//TODO: clan lvl9 or more can reinforce knights cheaper if first knight unit already created, use Config.KNIGHT_REINFORCE_COST
				}

				if (Config.DEBUG) {
					Log.fine("New sub_clan saved in db: " + getClanId() + "; " + pledgeType);
				}
			} catch (Exception e) {
				Log.log(Level.SEVERE, "Error saving sub clan data: " + e.getMessage(), e);
			} finally {
				L2DatabaseFactory.close(con);
			}
		}
		broadcastToOnlineMembers(new PledgeShowInfoUpdate(leader.getClan()));
		broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(subPledge, leader.getClan()));
		return subPledge;
	}

	public int getAvailablePledgeTypes(int pledgeType) {
		if (subPledges.get(pledgeType) != null) {
			//Logozo.warning("found sub-unit with id: "+pledgeType);
			switch (pledgeType) {
				case SUBUNIT_ACADEMY:
					return 0;
				case SUBUNIT_ROYAL1:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_ROYAL2);
					break;
				case SUBUNIT_ROYAL2:
					return 0;
				case SUBUNIT_KNIGHT1:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT2);
					break;
				case SUBUNIT_KNIGHT2:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT3);
					break;
				case SUBUNIT_KNIGHT3:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT4);
					break;
				case SUBUNIT_KNIGHT4:
					return 0;
			}
		}
		return pledgeType;
	}

	public void updateSubPledgeInDB(int pledgeType) {
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE clan_subpledges SET leader_id=?, name=? WHERE clan_id=? AND sub_pledge_id=?");
			statement.setInt(1, getSubPledge(pledgeType).getLeaderId());
			statement.setString(2, getSubPledge(pledgeType).getName());
			statement.setInt(3, getClanId());
			statement.setInt(4, pledgeType);
			statement.execute();
			statement.close();
			if (Config.DEBUG) {
				Log.fine("Subpledge updated in db: " + getClanId());
			}
		} catch (Exception e) {
			Log.log(Level.SEVERE, "Error updating subpledge: " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	private void restoreRankPrivs() {
		Connection con = null;

		try {
			// Retrieve all skills of this L2PcInstance from the database
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT privs,rank,party FROM clan_privs WHERE clan_id=?");
			statement.setInt(1, getClanId());
			//Logozo.warning("clanPrivs restore for ClanId : "+getClanId());
			ResultSet rset = statement.executeQuery();

			// Go though the recordset of this SQL query
			while (rset.next()) {
				int rank = rset.getInt("rank");
				//int party = rset.getInt("party");
				int privileges = rset.getInt("privs");
				// Create a SubPledge object for each record
				if (rank == -1) {
					continue;
				}

				if (privs.get(rank) == null) {
					continue;
				}

				privs.get(rank).setPrivs(privileges);
			}

			rset.close();
			statement.close();
		} catch (Exception e) {
			Log.log(Level.SEVERE, "Error restoring clan privs by rank: " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	public void initializePrivs() {
		RankPrivs privs;
		for (int i = 1; i < 10; i++) {
			privs = new RankPrivs(i, 0, CP_NOTHING);
			this.privs.put(i, privs);
		}
	}

	public int getRankPrivs(int rank) {
		if (privs.get(rank) != null) {
			return privs.get(rank).getPrivs();
		} else {
			return CP_NOTHING;
		}
	}

	public void setRankPrivs(int rank, int privs) {
		if (this.privs.get(rank) != null) {
			this.privs.get(rank).setPrivs(privs);

			Connection con = null;

			try {
				//Logozo.warning("requested store clan privs in db for rank: "+rank+", privs: "+privs);
				// Retrieve all skills of this L2PcInstance from the database
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement =
						con.prepareStatement("INSERT INTO clan_privs (clan_id,rank,party,privs) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE privs = ?");
				statement.setInt(1, getClanId());
				statement.setInt(2, rank);
				statement.setInt(3, 0);
				statement.setInt(4, privs);
				statement.setInt(5, privs);

				statement.execute();
				statement.close();
			} catch (Exception e) {
				Log.log(Level.WARNING, "Could not store clan privs for rank: " + e.getMessage(), e);
			} finally {
				L2DatabaseFactory.close(con);
			}
			for (L2ClanMember cm : getMembers()) {
				if (cm.isOnline()) {
					if (cm.getPowerGrade() == rank) {
						if (cm.getPlayerInstance() != null) {
							cm.getPlayerInstance().setClanPrivileges(privs);
							cm.getPlayerInstance().sendPacket(new UserInfo(cm.getPlayerInstance()));
						}
					}
				}
			}
		} else {
			this.privs.put(rank, new RankPrivs(rank, 0, privs));

			Connection con = null;

			try {
				//Logozo.warning("requested store clan new privs in db for rank: "+rank);
				// Retrieve all skills of this L2PcInstance from the database
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("INSERT INTO clan_privs (clan_id,rank,party,privs) VALUES (?,?,?,?)");
				statement.setInt(1, getClanId());
				statement.setInt(2, rank);
				statement.setInt(3, 0);
				statement.setInt(4, privs);
				statement.execute();
				statement.close();
			} catch (Exception e) {
				Log.log(Level.WARNING, "Could not create new rank and store clan privs for rank: " + e.getMessage(), e);
			} finally {
				L2DatabaseFactory.close(con);
			}
		}

		broadcastClanStatus();
	}

	/**
	 * used to retrieve all RankPrivs
	 */
	public final RankPrivs[] getAllRankPrivs() {
		if (privs == null) {
			return new RankPrivs[0];
		}

		return privs.values().toArray(new RankPrivs[privs.values().size()]);
	}

	public int getLeaderSubPledge(int leaderId) {
		int id = 0;
		for (SubPledge sp : subPledges.values()) {
			if (sp.getLeaderId() == 0) {
				continue;
			}
			if (sp.getLeaderId() == leaderId) {
				id = sp.getId();
			}
		}
		return id;
	}

	public synchronized void addReputationScore(int value, boolean save) {
		setReputationScore(getReputationScore() + value, save);
	}

	public synchronized void takeReputationScore(int value, boolean save) {
		setReputationScore(getReputationScore() - value, save);
	}

	private void setReputationScore(int value, boolean save) {
		if (reputationScore >= 0 && value < 0) {
			broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.REPUTATION_POINTS_0_OR_LOWER_CLAN_SKILLS_DEACTIVATED));
			for (L2ClanMember member : members.values()) {
				if (member.isOnline() && member.getPlayerInstance() != null) {
					removeSkillEffects(member.getPlayerInstance());
				}
			}
		} else if (reputationScore < 0 && value >= 0) {
			broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_SKILLS_WILL_BE_ACTIVATED_SINCE_REPUTATION_IS_0_OR_HIGHER));
			for (L2ClanMember member : members.values()) {
				if (member.isOnline() && member.getPlayerInstance() != null) {
					addSkillEffects(member.getPlayerInstance());
				}
			}
		}

		reputationScore = value;
		if (reputationScore > 100000000) {
			reputationScore = 100000000;
		}
		if (reputationScore < -100000000) {
			reputationScore = -100000000;
		}

		broadcastToOnlineMembers(new PledgeShowInfoUpdate(this));
		if (save) {
			updateClanScoreInDB();
		}
	}

	public int getReputationScore() {
		return reputationScore;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getRank() {
		return rank;
	}

	public int getAuctionBiddedAt() {
		return auctionBiddedAt;
	}

	public void setAuctionBiddedAt(int id, boolean storeInDb) {
		auctionBiddedAt = id;

		if (storeInDb) {
			Connection con = null;
			try {
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET auction_bid_at=? WHERE clan_id=?");
				statement.setInt(1, id);
				statement.setInt(2, getClanId());
				statement.execute();
				statement.close();
			} catch (Exception e) {
				Log.log(Level.WARNING, "Could not store auction for clan: " + e.getMessage(), e);
			} finally {
				L2DatabaseFactory.close(con);
			}
		}
	}

	/**
	 * Checks if activeChar and target meet various conditions to join a clan
	 *
	 * @param activeChar
	 * @param target
	 * @param pledgeType
	 * @return
	 */
	public boolean checkClanJoinCondition(L2PcInstance activeChar, L2PcInstance target, int pledgeType) {
		if (activeChar == null) {
			return false;
		}
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_JOIN_CLAN) != L2Clan.CP_CL_JOIN_CLAN) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			return false;
		}
		if (target == null) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET));
			return false;
		}
		if (activeChar.getObjectId() == target.getObjectId()) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_INVITE_YOURSELF));
			return false;
		}
		if (getCharPenaltyExpiryTime() > System.currentTimeMillis()) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER));
			return false;
		}
		if (target.getClanId() != 0) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_WORKING_WITH_ANOTHER_CLAN);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			sm = null;
			return false;
		}
		if (!activeChar.isGM() && target.getClanJoinExpiryTime() > System.currentTimeMillis()) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			sm = null;
			return false;
		}
		if ((target.getLevel() > 75 || target.getCurrentClass().level() > 40) && pledgeType == -1) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DOESNOT_MEET_REQUIREMENTS_TO_JOIN_ACADEMY);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			sm = null;
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ACADEMY_REQUIREMENTS));
			return false;
		}
		if (getSubPledgeMembersCount(pledgeType) >= getMaxNrOfMembers(pledgeType)) {
			if (pledgeType == 0) {
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_IS_FULL);
				sm.addString(getName());
				activeChar.sendPacket(sm);
				sm = null;
			} else {
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SUBCLAN_IS_FULL));
			}
			return false;
		}

		return !(Config.isServer(Config.TENKAI) && !checkClanBalance(activeChar, activeChar.getClan()));
	}

	/**
	 * Tenkai custom - check whether the clan requestion a user to join is not too full of chars
	 * already, compared to the other clans on the server. This hopefully improves the balance
	 * and decreases the chance of zerging.
	 *
	 * @return <b>false</b> if the requesting clan is too big, compared to the other clans. Else <b>true</b>.
	 */
	public boolean checkClanBalance(L2PcInstance requester, L2Clan clanOfRequester) {
		L2Clan[] topClans = ClanTable.getInstance().getTopTenClansByMemberCount();

		if (topClans == null || topClans.length <= 10) {
			return true;
		}

		if (clanOfRequester.getMembersCount() > topClans[1].getMembersCount() + 10) {
			requester.sendMessage(
					"Your clan is already too big compared to the clan right after yours. It would harm the server balance to let more characters join your clan.");
			return false;
		}

		return true;
	}

	/**
	 * Checks if activeChar and target meet various conditions to join a clan
	 *
	 * @param activeChar
	 * @param target
	 * @return
	 */
	public boolean checkAllyJoinCondition(L2PcInstance activeChar, L2PcInstance target) {
		if (activeChar == null) {
			return false;
		}
		if (activeChar.getAllyId() == 0 || !activeChar.isClanLeader() || activeChar.getClanId() != activeChar.getAllyId()) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER));
			return false;
		}
		L2Clan leaderClan = activeChar.getClan();
		if (leaderClan.getAllyPenaltyExpiryTime() > System.currentTimeMillis()) {
			if (leaderClan.getAllyPenaltyType() == PENALTY_TYPE_DISMISS_CLAN) {
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_INVITE_CLAN_WITHIN_1_DAY));
				return false;
			}
		}
		if (target == null) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET));
			return false;
		}
		if (activeChar.getObjectId() == target.getObjectId()) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_INVITE_YOURSELF));
			return false;
		}
		if (target.getClan() == null) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_MUST_BE_IN_CLAN));
			return false;
		}
		if (!target.isClanLeader()) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			sm = null;
			return false;
		}
		L2Clan targetClan = target.getClan();
		if (target.getAllyId() != 0) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_ALREADY_MEMBER_OF_S2_ALLIANCE);
			sm.addString(targetClan.getName());
			sm.addString(targetClan.getAllyName());
			activeChar.sendPacket(sm);
			sm = null;
			return false;
		}
		if (targetClan.getAllyPenaltyExpiryTime() > System.currentTimeMillis()) {
			if (targetClan.getAllyPenaltyType() == PENALTY_TYPE_CLAN_LEAVED) {
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANT_ENTER_ALLIANCE_WITHIN_1_DAY);
				sm.addString(target.getClan().getName());
				//sm.addString(target.getClan().getAllyName());
				activeChar.sendPacket(sm);
				sm = null;
				return false;
			}
			if (targetClan.getAllyPenaltyType() == PENALTY_TYPE_CLAN_DISMISSED) {
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_ENTER_ALLIANCE_WITHIN_1_DAY));
				return false;
			}
		}
		if (activeChar.isInsideZone(L2Character.ZONE_SIEGE) && target.isInsideZone(L2Character.ZONE_SIEGE)) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OPPOSING_CLAN_IS_PARTICIPATING_IN_SIEGE));
			return false;
		}
		if (leaderClan.isAtWarWith(targetClan.getClanId())) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MAY_NOT_ALLY_CLAN_BATTLE));
			return false;
		}

		int numOfClansInAlly = 0;
		for (L2Clan clan : ClanTable.getInstance().getClans()) {
			if (clan.getAllyId() == activeChar.getAllyId()) {
				++numOfClansInAlly;
			}
		}
		if (numOfClansInAlly >= Config.ALT_MAX_NUM_OF_CLANS_IN_ALLY) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_THE_LIMIT));
			return false;
		}

		return true;
	}

	public long getAllyPenaltyExpiryTime() {
		return allyPenaltyExpiryTime;
	}

	public int getAllyPenaltyType() {
		return allyPenaltyType;
	}

	public void setAllyPenaltyExpiryTime(long expiryTime, int penaltyType) {
		allyPenaltyExpiryTime = expiryTime;
		allyPenaltyType = penaltyType;
	}

	public long getCharPenaltyExpiryTime() {
		return charPenaltyExpiryTime;
	}

	public void setCharPenaltyExpiryTime(long time) {
		charPenaltyExpiryTime = time;
	}

	public long getDissolvingExpiryTime() {
		return dissolvingExpiryTime;
	}

	public void setDissolvingExpiryTime(long time) {
		dissolvingExpiryTime = time;
	}

	public void createAlly(L2PcInstance player, String allyName) {
		if (null == player) {
			return;
		}

		if (Config.DEBUG) {
			Log.fine(player.getObjectId() + " (" + player.getName() + ") requested ally creation from ");
		}

		if (!player.isClanLeader()) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_CLAN_LEADER_CREATE_ALLIANCE));
			return;
		}
		if (getAllyId() != 0) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_JOINED_ALLIANCE));
			return;
		}
		if (getLevel() < 5) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TO_CREATE_AN_ALLY_YOU_CLAN_MUST_BE_LEVEL_5_OR_HIGHER));
			return;
		}
		if (getAllyPenaltyExpiryTime() > System.currentTimeMillis()) {
			if (getAllyPenaltyType() == L2Clan.PENALTY_TYPE_DISSOLVE_ALLY) {
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_CREATE_ALLIANCE_10_DAYS_DISOLUTION));
				return;
			}
		}
		if (getDissolvingExpiryTime() > System.currentTimeMillis()) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_MAY_NOT_CREATE_ALLY_WHILE_DISSOLVING));
			return;
		}
		if (!Util.isAlphaNumeric(allyName)) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_ALLIANCE_NAME));
			return;
		}
		if (allyName.length() > 16 || allyName.length() < 2) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_ALLIANCE_NAME_LENGTH));
			return;
		}
		if (ClanTable.getInstance().isAllyExists(allyName)) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_ALREADY_EXISTS));
			return;
		}

		setAllyId(getClanId());
		setAllyName(allyName.trim());
		setAllyPenaltyExpiryTime(0, 0);
		updateClanInDB();

		player.sendPacket(new UserInfo(player));

		//TODO: Need correct message id
		player.sendMessage("Alliance " + allyName + " has been created.");
		// notify CB server about the change
	}

	public void dissolveAlly(L2PcInstance player) {
		if (getAllyId() == 0) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_CURRENT_ALLIANCES));
			return;
		}
		if (!player.isClanLeader() || getClanId() != getAllyId()) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER));
			return;
		}
		if (player.isInsideZone(L2Character.ZONE_SIEGE)) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISSOLVE_ALLY_WHILE_IN_SIEGE));
			return;
		}

		broadcastToOnlineAllyMembers(SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_DISOLVED));

		long currentTime = System.currentTimeMillis();
		for (L2Clan clan : ClanTable.getInstance().getClans()) {
			if (clan.getAllyId() == getAllyId() && clan.getClanId() != getClanId()) {
				clan.setAllyId(0);
				clan.setAllyName(null);
				clan.setAllyPenaltyExpiryTime(0, 0);
				clan.updateClanInDB();
				// notify CB server about the change
			}
		}

		setAllyId(0);
		setAllyName(null);
		changeAllyCrest(0, false);
		setAllyPenaltyExpiryTime(currentTime + Config.ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED * 86400000L,
				L2Clan.PENALTY_TYPE_DISSOLVE_ALLY); //24*60*60*1000 = 86400000
		updateClanInDB();

		// The clan leader should take the XP penalty of a full death.
		player.deathPenalty(false, false, false, false);
		// notify CB server about the change
	}

	public boolean levelUpClan(L2PcInstance player) {
		if (!player.isClanLeader()) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			return false;
		}
		if (System.currentTimeMillis() < getDissolvingExpiryTime()) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_RISE_LEVEL_WHILE_DISSOLUTION_IN_PROGRESS));
			return false;
		}

		int requiredMembers = 0;
		int requiredReputation = 0;
		boolean increaseClanLevel = false;

		switch (getLevel()) {
			case 0: {
				// Upgrade to 1
				if (player.getSp() >= 2000 && player.getAdena() >= 650000) {
					if (player.reduceAdena("ClanLvl", 650000, player.getTarget(), true)) {
						player.setSp(player.getSp() - 2000);
						SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(2000);
						player.sendPacket(sp);
						sp = null;
						increaseClanLevel = true;
					}
				}
				break;
			}
			case 1: {
				// Upgrade to 2
				if (player.getSp() >= 10000 && player.getAdena() >= 2500000) {
					if (player.reduceAdena("ClanLvl", 2500000, player.getTarget(), true)) {
						player.setSp(player.getSp() - 10000);
						SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(10000);
						player.sendPacket(sp);
						sp = null;
						increaseClanLevel = true;
					}
				}
				break;
			}
			case 2: {
				// Upgrade to 3
				if (player.getSp() >= 35000 && player.getInventory().getItemByItemId(1419) != null) {
					// itemId 1419 == Blood Mark
					if (player.destroyItemByItemId("ClanLvl", 1419, 1, player.getTarget(), false)) {
						player.setSp(player.getSp() - 35000);
						SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(35000);
						player.sendPacket(sp);
						sp = null;
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
						sm.addItemName(1419);
						player.sendPacket(sm);
						sm = null;
						increaseClanLevel = true;
					}
				}
				break;
			}
			case 3: {
				// Upgrade to 4
				if (player.getSp() >= 100000 && player.getInventory().getItemByItemId(3874) != null) {
					// itemId 3874 == Alliance Manifesto
					if (player.destroyItemByItemId("ClanLvl", 3874, 1, player.getTarget(), false)) {
						player.setSp(player.getSp() - 100000);
						SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(100000);
						player.sendPacket(sp);
						sp = null;
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
						sm.addItemName(3874);
						player.sendPacket(sm);
						sm = null;
						increaseClanLevel = true;
					}
				}
				break;
			}
			case 4: {
				// Upgrade to 5
				if (player.getSp() >= 250000 && player.getInventory().getItemByItemId(3870) != null) {
					// itemId 3870 == Seal of Aspiration
					if (player.destroyItemByItemId("ClanLvl", 3870, 1, player.getTarget(), false)) {
						player.setSp(player.getSp() - 250000);
						SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(250000);
						player.sendPacket(sp);
						sp = null;
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
						sm.addItemName(3870);
						player.sendPacket(sm);
						sm = null;
						increaseClanLevel = true;
					}
				}
				break;
			}
			case 5:
				// Upgrade to 6
				if (getReputationScore() >= Config.CLAN_LEVEL_6_COST && getMembersCount() >= Config.CLAN_LEVEL_6_REQUIREMENT) {
					setReputationScore(getReputationScore() - Config.CLAN_LEVEL_6_COST, true);
					SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(Config.CLAN_LEVEL_6_COST);
					player.sendPacket(cr);
					cr = null;
					increaseClanLevel = true;
				} else if (getReputationScore() >= 20000) {
					setReputationScore(getReputationScore() - 20000, true);
					SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(20000);
					player.sendPacket(cr);
					cr = null;
					increaseClanLevel = true;
				}
				requiredMembers = Config.CLAN_LEVEL_6_REQUIREMENT;
				requiredReputation = Config.CLAN_LEVEL_6_COST;
				break;

			case 6:
				// Upgrade to 7
				if (getReputationScore() >= Config.CLAN_LEVEL_7_COST && getMembersCount() >= Config.CLAN_LEVEL_7_REQUIREMENT) {
					setReputationScore(getReputationScore() - Config.CLAN_LEVEL_7_COST, true);
					SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(Config.CLAN_LEVEL_7_COST);
					player.sendPacket(cr);
					cr = null;
					increaseClanLevel = true;
				} else if (getReputationScore() >= 30000) {
					setReputationScore(getReputationScore() - 30000, true);
					SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(30000);
					player.sendPacket(cr);
					cr = null;
					increaseClanLevel = true;
				}
				requiredMembers = Config.CLAN_LEVEL_7_REQUIREMENT;
				requiredReputation = Config.CLAN_LEVEL_7_COST;
				break;
			case 7:
				// Upgrade to 8
				if (getReputationScore() >= Config.CLAN_LEVEL_8_COST && getMembersCount() >= Config.CLAN_LEVEL_8_REQUIREMENT) {
					setReputationScore(getReputationScore() - Config.CLAN_LEVEL_8_COST, true);
					SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(Config.CLAN_LEVEL_8_COST);
					player.sendPacket(cr);
					cr = null;
					increaseClanLevel = true;
				} else if (getReputationScore() >= 50000) {
					setReputationScore(getReputationScore() - 50000, true);
					SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(50000);
					player.sendPacket(cr);
					cr = null;
					increaseClanLevel = true;
				}
				requiredMembers = Config.CLAN_LEVEL_8_REQUIREMENT;
				requiredReputation = Config.CLAN_LEVEL_8_COST;
				break;
			case 8:
				// Upgrade to 9
				if (getReputationScore() >= Config.CLAN_LEVEL_9_COST && player.getInventory().getItemByItemId(9910) != null &&
						getMembersCount() >= Config.CLAN_LEVEL_9_REQUIREMENT) {
					// itemId 9910 == Blood Oath
					if (player.destroyItemByItemId("ClanLvl", 9910, 150, player.getTarget(), false)) {
						setReputationScore(getReputationScore() - Config.CLAN_LEVEL_9_COST, true);
						SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
						cr.addNumber(Config.CLAN_LEVEL_9_COST);
						player.sendPacket(cr);
						cr = null;
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
						sm.addItemName(9910);
						sm.addItemNumber(150);
						player.sendPacket(sm);
						increaseClanLevel = true;
					}
				}
				requiredMembers = Config.CLAN_LEVEL_9_REQUIREMENT;
				requiredReputation = Config.CLAN_LEVEL_9_COST;
				break;
			case 9:
				// Upgrade to 10
				if (getReputationScore() >= Config.CLAN_LEVEL_10_COST && player.getInventory().getItemByItemId(9911) != null &&
						getMembersCount() >= Config.CLAN_LEVEL_10_REQUIREMENT) {
					// itemId 9911 == Blood Alliance
					if (player.destroyItemByItemId("ClanLvl", 9911, 5, player.getTarget(), false)) {
						setReputationScore(getReputationScore() - Config.CLAN_LEVEL_10_COST, true);
						SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
						cr.addNumber(Config.CLAN_LEVEL_10_COST);
						player.sendPacket(cr);
						cr = null;
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
						sm.addItemName(9911);
						sm.addItemNumber(5);
						player.sendPacket(sm);
						increaseClanLevel = true;
					}
				}
				requiredMembers = Config.CLAN_LEVEL_10_REQUIREMENT;
				requiredReputation = Config.CLAN_LEVEL_10_COST;
				break;
			case 10:
				Broadcast.toGameMasters(player.getName() + " is trying to level up his clan " + getName() + " to 11.");
				Broadcast.toGameMasters("Reputation = " + getReputationScore());
				Broadcast.toGameMasters("Members = " + getMembersCount());
				// Upgrade to 11
				if (getReputationScore() >= Config.CLAN_LEVEL_11_COST && getMembersCount() >= Config.CLAN_LEVEL_11_REQUIREMENT &&
						(player.getInventory().getInventoryItemCount(34996, 0, true) > 0)) {
					setReputationScore(getReputationScore() - Config.CLAN_LEVEL_11_COST, true);
					SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(Config.CLAN_LEVEL_11_COST);
					player.sendPacket(cr);
					cr = null;
					increaseClanLevel = true;
				}
				requiredMembers = Config.CLAN_LEVEL_11_REQUIREMENT;
				requiredReputation = Config.CLAN_LEVEL_11_COST;
				break;
			default:
				return false;
		}

		if (!increaseClanLevel) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_INCREASE_CLAN_LEVEL);
			player.sendPacket(sm);
			if (getMembersCount() < requiredMembers) {
				player.sendMessage("You need " + requiredMembers + " members in order to level up your clan.");
			}
			if (getReputationScore() < requiredReputation) {
				player.sendMessage("You need " + requiredReputation + " reputation in order to level up your clan.");
			}
			return false;
		}

		// the player should know that he has less sp now :p
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.SP, (int) player.getSp());
		player.sendPacket(su);

		ItemList il = new ItemList(player, false);
		player.sendPacket(il);

		changeLevel(getLevel() + 1);
		return true;
	}

	public void changeLevel(int level) {
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET clan_level = ? WHERE clan_id = ?");
			statement.setInt(1, level);
			statement.setInt(2, getClanId());
			statement.execute();
			statement.close();
		} catch (Exception e) {
			Log.log(Level.WARNING, "could not increase clan level:" + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}

		setLevel(level);

		if (getLeader().isOnline()) {
			L2PcInstance leader = getLeader().getPlayerInstance();
			if (4 < level) {
				SiegeManager.getInstance().addSiegeSkills(leader);
				leader.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS));
			} else if (5 > level) {
				SiegeManager.getInstance().removeSiegeSkills(leader);
			}
		}

		// notify all the members about it
		broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_LEVEL_INCREASED));
		broadcastToOnlineMembers(new PledgeShowInfoUpdate(this));
		/*
		 * Micht :
		 * 	- use PledgeShowInfoUpdate instead of PledgeStatusChanged
		 * 		to update clan level ingame
		 * 	- remove broadcastClanStatus() to avoid members duplication
		 */
		//clan.broadcastToOnlineMembers(new PledgeStatusChanged(clan));
		//clan.broadcastClanStatus();

		// notify CB server about the change
	}

	/**
	 * Change the clan crest. If crest id is 0, crest is removed. New crest id is saved to database.
	 *
	 * @param crestId if 0, crest is removed, else new crest id is set and saved to database
	 */
	public void changeClanCrest(int crestId) {
		if (getCrestId() != 0) {
			CrestCache.getInstance().removePledgeCrest(getCrestId());
		}

		setCrestId(crestId);
		broadcastToOnlineMembers(new PledgeShowInfoUpdate(this));
		broadcastToOnlineMembers(new PledgeStatusChanged(this));

		Connection con = null;

		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET crest_id = ? WHERE clan_id = ?");
			statement.setInt(1, crestId);
			statement.setInt(2, getClanId());
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			Log.log(Level.WARNING, "Could not update crest for clan " + getName() + " [" + getClanId() + "] : " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}

		for (L2PcInstance member : getOnlineMembers(0)) {
			member.broadcastUserInfo();
		}
	}

	/**
	 * Change the ally crest. If crest id is 0, crest is removed. New crest id is saved to database.
	 *
	 * @param crestId if 0, crest is removed, else new crest id is set and saved to database
	 */
	public void changeAllyCrest(int crestId, boolean onlyThisClan) {
		String sqlStatement = "UPDATE clan_data SET ally_crest_id = ? WHERE clan_id = ?";
		int allyId = getClanId();
		if (!onlyThisClan) {
			if (getAllyCrestId() != 0) {
				CrestCache.getInstance().removeAllyCrest(getAllyCrestId());
			}
			sqlStatement = "UPDATE clan_data SET ally_crest_id = ? WHERE ally_id = ?";
			allyId = getAllyId();
		}

		Connection con = null;

		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(sqlStatement);
			statement.setInt(1, crestId);
			statement.setInt(2, allyId);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			Log.log(Level.WARNING, "Could not update ally crest for ally/clan id " + allyId + " : " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}

		if (onlyThisClan) {
			setAllyCrestId(crestId);
			for (L2PcInstance member : getOnlineMembers(0)) {
				member.broadcastUserInfo();
			}
		} else {
			for (L2Clan clan : ClanTable.getInstance().getClans()) {
				if (clan.getAllyId() == getAllyId()) {
					clan.setAllyCrestId(crestId);
					for (L2PcInstance member : clan.getOnlineMembers(0)) {
						member.broadcastUserInfo();
					}
				}
			}
		}
	}

	private int tempLargeCrestId = 0;

	public int getTempLargeCrestId() {
		return tempLargeCrestId;
	}

	public void setTempLargeCrestId(int id) {
		tempLargeCrestId = id;
	}

	/**
	 * Change the large crest. If crest id is 0, crest is removed. New crest id is saved to database.
	 *
	 * @param crestId if 0, crest is removed, else new crest id is set and saved to database
	 */
	public void changeLargeCrest(int crestId) {
		if (getLargeCrestId() != 0) {
			CrestCache.getInstance().removePledgeCrestLarge(getLargeCrestId());
		}

		setLargeCrestId(crestId);

		Connection con = null;

		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET crest_large_id = ? WHERE clan_id = ?");
			statement.setInt(1, crestId);
			statement.setInt(2, getClanId());
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			Log.log(Level.WARNING, "Could not update large crest for clan " + getName() + " [" + getClanId() + "] : " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}

		for (L2PcInstance member : getOnlineMembers(0)) {
			member.broadcastUserInfo();
		}
	}

	/**
	 * Check if clan learn this squad skill
	 *
	 * @param skill
	 * @return true if can be added
	 */
	public boolean isLearnableSubSkill(L2Skill skill) {
		int id = skill.getId();
		L2Skill current = subPledgeSkills.get(id);
		// is next level?
		if (current != null && current.getLevel() + 1 == skill.getLevel()) {
			return true;
		}
		// is first level?
		if (current == null && skill.getLevel() == 1) {
			return true;
		}
		// other subpledges
		for (SubPledge subunit : subPledges.values()) {
			//disable academy
			if (subunit.id == -1) {
				continue;
			}
			current = subunit.subPledgeSkills.get(id);
			// is next level?
			if (current != null && current.getLevel() + 1 == skill.getLevel()) {
				return true;
			}
			// is first level?
			if (current == null && skill.getLevel() == 1) {
				return true;
			}
		}
		return false;
	}

	public boolean isLearnableSubSkill(L2Skill skill, int subType) {
		//academy
		if (subType == -1) {
			return false;
		}

		int id = skill.getId();
		L2Skill current;
		if (subType == 0) {
			current = subPledgeSkills.get(id);
		} else {
			current = subPledges.get(subType).subPledgeSkills.get(id);
		}
		// is next level?
		if (current != null && current.getLevel() + 1 == skill.getLevel()) {
			return true;
		}
		// is first level?
		return current == null && skill.getLevel() == 1;
	}

	public SubPledgeSkill[] getAllSubSkills() {
		ArrayList<SubPledgeSkill> list = new ArrayList<>();
		for (L2Skill skill : subPledgeSkills.values()) {
			list.add(new SubPledgeSkill(0, skill.getId(), skill.getLevelHash()));
		}
		for (SubPledge subunit : subPledges.values()) {
			for (L2Skill skill : subunit.getSkills()) {
				list.add(new SubPledgeSkill(subunit.id, skill.getId(), skill.getLevelHash()));
			}
		}
		return list.toArray(new SubPledgeSkill[list.size()]);
	}

	private void checkCrests() {
		if (getCrestId() != 0) {
			if (CrestCache.getInstance().getPledgeCrest(getCrestId()) == null) {
				Log.info("Removing non-existent crest for clan " + getName() + " [" + getClanId() + "], crestId:" + getCrestId());
				setCrestId(0);
				changeClanCrest(0);
			}
		}
		if (getLargeCrestId() != 0) {
			if (CrestCache.getInstance().getPledgeCrestLarge(getLargeCrestId()) == null) {
				Log.info("Removing non-existent large crest for clan " + getName() + " [" + getClanId() + "], crestLargeId:" + getLargeCrestId());
				setLargeCrestId(0);
				changeLargeCrest(0);
			}
		}
		if (getAllyCrestId() != 0) {
			if (CrestCache.getInstance().getAllyCrest(getAllyCrestId()) == null) {
				Log.info("Removing non-existent ally crest for clan " + getName() + " [" + getClanId() + "], allyCrestId:" + getAllyCrestId());
				setAllyCrestId(0);
				changeAllyCrest(0, true);
			}
		}
	}

	public void checkTendency() {
		for (L2ClanMember temp : members.values()) {
			if (temp != null && temp.getPlayerInstance() != null && temp.isOnline()) {
				removeSkillEffects(temp.getPlayerInstance());
			}
		}

		skills.remove(19032);
		skills.remove(19033);
		Castle castle = CastleManager.getInstance().getCastleByOwner(this);
		if (castle != null) {
			L2Skill skill = null;
			if (castle.getTendency() == Castle.TENDENCY_LIGHT) {
				skill = SkillTable.getInstance().getInfo(19032, 1);
			} else if (castle.getTendency() == Castle.TENDENCY_DARKNESS) {
				skill = SkillTable.getInstance().getInfo(19033, 1);
			}

			if (skill != null) {
				addSkill(skill);
			}
		}

		for (L2ClanMember temp : members.values()) {
			if (temp != null && temp.getPlayerInstance() != null && temp.isOnline()) {
				addSkillEffects(temp.getPlayerInstance());
			}
		}
	}
}
