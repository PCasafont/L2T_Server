package l2server.gameserver.events;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.Announcements;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.datatables.PlayerClassTable;
import l2server.gameserver.instancemanager.MailManager;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.entity.Message;
import l2server.gameserver.model.itemcontainer.Mail;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.util.loader.annotations.Load;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;

/**
 * @author LasTravel
 */
public class DamageManager {
	private static Logger log = LoggerFactory.getLogger(DamageManager.class.getName());


	private static Map<Integer, DamageInfo> dmgIinfo = new HashMap<>();
	protected static ScheduledFuture<?> saveTask;

	public String getRankingInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append(
				"<table bgcolor=999999 width=750><tr><td></td><td FIXWIDTH=200>Class</td><td FIXWIDTH=250>Actual Record</td><td FIXWIDTH=250>Actual Owner</td></tr></table>");

		for (Entry<Integer, DamageInfo> info : dmgIinfo.entrySet()) {
			String className = PlayerClassTable.getInstance().getClassNameById(info.getKey());
			int actualrecord = info.getValue().getCurrentDamage();
			String actualowner = info.getValue().getNewName();

			if (actualowner.equals("")) {
				actualowner = "none";
			}

			sb.append(
					"<table cellspacing=0 cellpadding=2 width=750 height=17><tr><td><img src=\"L2UI_CT1.PlayerStatusWnd_ClassMark_" + info.getKey() +
							"_Big\" width=32 height=32></td><td FIXWIDTH=200>" + className + "</td><td FIXWIDTH=250>" + actualrecord +
							"</td><td FIXWIDTH=250>" + actualowner + "</td></tr></table>");
			sb.append("<table><tr><td><img src=\"L2UI.Squaregray\" width=750 height=1></td></tr></table>");
		}
		return sb.toString();
	}

	private class DamageInfo {
		private int classId;
		private int newDamage;
		private String newName;
		private int playerId;
		private String hwId;

		private DamageInfo(int classid, int newdmg, String newname, int playerid, String hwId) {
			classId = classid;
			newDamage = newdmg;
			newName = newname;
			playerId = playerid;
			this.hwId = hwId;
		}

		private void reset() {
			newDamage = 0;
			newName = "";
			playerId = 0;
			hwId = "";
		}

		private int getClassId() {
			return classId;
		}

		private int getCurrentDamage() {
			return newDamage;
		}

		private String getNewName() {
			return newName;
		}

		private int getPlayerId() {
			return playerId;
		}

		private String gethwId() {
			return hwId;
		}

		private void setNewData(int dmg, Player pl) {
			pl.sendPacket(new CreatureSay(36610,
					2,
					"Damage Manager",
					"Congrats, you raised the $1 record with $2!".replace("$1", PlayerClassTable.getInstance().getClassNameById(pl.getClassId()))
							.replace("$2", String.valueOf(dmg))));

			newDamage = dmg;
			newName = pl.getName();
			playerId = pl.getObjectId();
			hwId = pl.getHWID();
		}
	}

	public void giveDamage(Player pl, int dmg) {
		if (pl == null) {
			return;
		}

		DamageInfo info = dmgIinfo.get(pl.getClassId());
		if (info == null) {
			return;
		}

		/*String hwId = pl.getHWID();
		if (hwId == null || hwId.equalsIgnoreCase(""))
			return;*/

		if (dmg > info.getCurrentDamage()) {
            /*if (playerMatch(pl.getHWID(), pl.getClassId()))
			{
				pl.sendMessage("You already have another record, only one per person is allowed");
				return;
			}*/
			info.setNewData(dmg, pl);
		}
	}

	@SuppressWarnings("unused")
	private boolean playerMatch(String hwId, int playerClassId) {
		for (Entry<Integer, DamageInfo> info : dmgIinfo.entrySet()) {
			if (info.getValue().gethwId().equalsIgnoreCase(hwId)) {
				return info.getValue().getClassId() != playerClassId;
			}
		}
		return false;
	}

	public void saveData() {
		log.debug("Damage Manager: Saving information..!");
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = null;
			for (Entry<Integer, DamageInfo> info : dmgIinfo.entrySet()) {
				if (info == null) {
					continue;
				}
				statement = con.prepareStatement("UPDATE `dmg_data` SET `newdmg`=?, `newname`=?, `playerid`=? WHERE `classid`=?");
				statement.setInt(1, info.getValue().getCurrentDamage());
				statement.setString(2, info.getValue().getNewName());
				statement.setInt(3, info.getValue().getPlayerId());
				statement.setString(4, info.getValue().gethwId());
				//statement.setInt(5, info.getValue().getClassId());
				statement.executeUpdate();
				statement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	public void giveRewardsAndReset() {
		if (dmgIinfo.isEmpty()) {
			return;
		}

		// Give Rewards (Memory)
		List<Integer> rewardedCharIds = new ArrayList<>();
		for (Entry<Integer, DamageInfo> damageInfo : dmgIinfo.entrySet()) {
			if (damageInfo == null) {
				continue;
			}

			DamageInfo info = damageInfo.getValue();
			if (info == null) {
				continue;
			}

			int charId = info.getPlayerId();
			String playerName = CharNameTable.getInstance().getNameById(charId);
			if (playerName == null || info.getCurrentDamage() == 0) {
				continue;
			}

			if (rewardedCharIds.contains(charId)) {
				continue;
			}

			rewardedCharIds.add(charId);

			Message msg = new Message(-1,
					charId,
					false,
					"Damage Manager",
					"Congratulations, you was classified on Damage Ranking, here is your reward!",
					0);

			Mail attachments = msg.createAttachments();
			attachments.addItem("Damage Manager", Config.CUSTOM_DAMAGE_MANAGER_REWARD_ID, Config.CUSTOM_DAMAGE_MANAGER_REWARD_AMOUNT, null, null);

			MailManager.getInstance().sendMessage(msg);
			//log.info("Damage Manager: Player: " + damageInfo.getValue().getNewName() + " rewarded!");
		}

		// Restart The Ranking (BD)
		truncateTable();

		// Restart info from memory
		for (Entry<Integer, DamageInfo> info : dmgIinfo.entrySet()) {
			if (info.getValue().getCurrentDamage() > 0) {
				info.getValue().reset();
			}
		}

		// Announce to all online
		Announcements.getInstance()
				.announceToAll("All players classified on Damage Ranking were rewarded and the ranking was restarted, more rewards the next week!");
	}

	private static void truncateTable() {
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = null;
			statement = con.prepareStatement("UPDATE dmg_data SET newdmg=0, playerid=0, newname=0, hwId=0 WHERE newdmg > 0");
			statement.executeUpdate();
			statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	@Load
	private void load() {
		log.info("Damage Manager: Loading DMG Ranking information..!");
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement("SELECT `classid`, `newdmg`, `newname`, `playerid`, '' FROM `dmg_data`");
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				DamageInfo info = new DamageInfo(rs.getInt("classid"),
						rs.getInt("newdmg"),
						rs.getString("newname"),
						rs.getInt("playerid"),
						"" /*rs.getString("hwId")*/);
				dmgIinfo.put(info.getClassId(), info);
			}
			statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			L2DatabaseFactory.close(con);
		}

		saveTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this::saveData, 3600000, 3600000);
	}

	private DamageManager() {
	}

	public static DamageManager getInstance() {
		return SingletonHolder.instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final DamageManager instance = new DamageManager();
	}
}
