package l2server.gameserver.events;

import l2server.L2DatabaseFactory;
import l2server.gameserver.Announcements;
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.datatables.PlayerClassTable;
import l2server.gameserver.instancemanager.MailManager;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Message;
import l2server.gameserver.model.itemcontainer.Mail;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author LasTravel
 */

public class DMGManager
{

	// Config
	private static final String eventname = "Damage Manager";

	// Other
	private static Map<Integer, dmginfo> dmgIinfo = new HashMap<>();

	public static String getRankingInfo()
	{
		StringBuilder sb = new StringBuilder();

		sb.append(
				"<table bgcolor=999999 width=750><tr><td></td><td FIXWIDTH=200>Class</td><td FIXWIDTH=250>Actual Record</td><td FIXWIDTH=250>Actual Owner</td></tr></table>");

		for (Entry<Integer, dmginfo> info : dmgIinfo.entrySet())
		{
			String className = PlayerClassTable.getInstance().getClassNameById(info.getKey());

			int actualrecord = info.getValue().getNewDmg();

			String actualowner = info.getValue().getNewName();

			if (actualowner.equals(""))
			{
				actualowner = "none";
			}

			sb.append(
					"<table cellspacing=0 cellpadding=2 width=750 height=17><tr><td><img src=\"L2UI_CT1.PlayerStatusWnd_ClassMark_" +
							info.getKey() + "_Big\" width=32 height=32></td><td FIXWIDTH=200>" + className +
							"</td><td FIXWIDTH=250>" + actualrecord + "</td><td FIXWIDTH=250>" + actualowner +
							"</td></tr></table>");

			sb.append("<table><tr><td><img src=\"L2UI.Squaregray\" width=750 height=1></td></tr></table>");
		}

		return sb.toString();
	}

	public static class dmginfo
	{
		private int Classid = 0;

		private int NewDmg = 0;

		private String NewName = null;

		private int PlayerId = 0;

		private String externalIP = null;

		private String internalIP = null;

		public void setdmg(int classid, int newdmg, String newname, int playerid, String externalip, String internalip)
		{
			Classid = classid;

			NewDmg = newdmg;

			NewName = newname;

			PlayerId = playerid;

			externalIP = externalip;

			internalIP = internalip;
		}

		public void setDmg(int dmg)
		{
			NewDmg = dmg;
		}

		public void setName(String name)
		{
			NewName = name;
		}

		public void setExternalIP(String ip)
		{
			externalIP = ip;
		}

		public int getClassId()
		{
			return Classid;
		}

		public int getNewDmg()
		{
			return NewDmg;
		}

		public String getNewName()
		{
			return NewName;
		}

		public int getPlayerId()
		{
			return PlayerId;
		}

		public String getExternalIP()
		{
			return externalIP;
		}

		public String getInternalIP()
		{
			return internalIP;
		}

		public void setNewData(int dmg, L2PcInstance pl)
		{
			pl.sendPacket(new CreatureSay(36610, 2, eventname, "Congrats, you raised the $1 record with $2!"
					.replace("$1", PlayerClassTable.getInstance().getClassNameById(pl.getClassId()))
					.replace("$2", String.valueOf(dmg))));

			NewDmg = dmg;

			NewName = pl.getName();

			PlayerId = pl.getObjectId();

			externalIP = pl.getExternalIP();

			internalIP = pl.getInternalIP();
		}
	}

	public static void giveDamage(L2PcInstance pl, int dmg)
	{
		if (pl == null)
		{
			return;
		}

		dmginfo info = dmgIinfo.get(pl.getClassId());

		if (info == null)
		{
			return;
		}

		if (dmg > info.getNewDmg())
		{
			if (playerMatch(pl.getExternalIP(), pl.getInternalIP(), pl.getClassId()))
			{
				pl.sendMessage("You already have other record, only it's allowed one per person");

				return;
			}

			info.setNewData(dmg, pl);
		}
	}

	private static boolean playerMatch(String externalIP, String internalIP, int playerClassId)
	{
		for (Entry<Integer, dmginfo> info : dmgIinfo.entrySet())
		{
			if (info.getValue().getExternalIP().equalsIgnoreCase(externalIP))
			{
				if (info.getValue().getInternalIP().equalsIgnoreCase(internalIP))
				{
					return info.getValue().getClassId() != playerClassId;
				}
			}
		}

		return false;
	}

	public static void saveData()
	{
		Log.info(eventname + ": Saving information..!");

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = null;

			for (Entry<Integer, dmginfo> info : dmgIinfo.entrySet())
			{
				if (info == null)
				{
					continue;
				}

				statement = con.prepareStatement(
						"UPDATE `dmg_data` SET `newdmg`=?, `newname`=?, `playerid`=?, `externalIP`=?, `internalIP`=? WHERE `classid`=?");

				statement.setInt(1, info.getValue().getNewDmg());

				statement.setString(2, info.getValue().getNewName());

				statement.setInt(3, info.getValue().getPlayerId());

				statement.setString(4, info.getValue().getExternalIP());

				statement.setString(5, info.getValue().getInternalIP());

				statement.setInt(6, info.getValue().getClassId());

				statement.executeUpdate();

				statement.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public static void giveRewardsAndReset()
	{
		if (dmgIinfo.isEmpty())
		{
			return;
		}

		// Give Rewards (Memory)
		for (Entry<Integer, dmginfo> info : dmgIinfo.entrySet())
		{
			if (info.getValue().getNewName() != null && !info.getValue().getNewName().equals(""))
			{
				int charid = CharNameTable.getInstance().getIdByName(info.getValue().getNewName());

				Message msg = new Message(-1, charid, false, eventname,
						"Congratulations, you was classified on Damage Ranking, here is your reward!", 0);

				Mail attachments = msg.createAttachments();

				attachments.addItem(eventname, 4357, 30000, null, null);

				MailManager.getInstance().sendMessage(msg);

				Log.warning(eventname + ": Player: " + info.getValue().getNewName() + " rewarded!");
			}
		}

		// Restart The Ranking (BD)
		truncateTable();

		// Restart info from memmory
		for (Entry<Integer, dmginfo> info : dmgIinfo.entrySet())
		{
			if (info.getValue().getNewDmg() > 0)
			{
				info.getValue().setDmg(0);

				info.getValue().setName("");
			}
		}

		// Announce to all online
		Announcements.getInstance().announceToAll(
				"All players classified on Damage Ranking were rewarded and the ranking was restarted, more rewards the next week!");
	}

	private static void truncateTable()
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = null;

			statement = con.prepareStatement(
					"UPDATE dmg_data SET newdmg=0, playerid=0, newname=0, externalIP=0, internalIP=0 WHERE newdmg > 0");

			statement.executeUpdate();

			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public static void updateIP(L2PcInstance pl)
	{
		if (pl == null)
		{
			return;
		}

		for (Entry<Integer, dmginfo> info : dmgIinfo.entrySet())
		{
			if (info == null)
			{
				continue;
			}

			if (info.getValue().getNewName().equalsIgnoreCase(pl.getName()) &&
					!info.getValue().getExternalIP().equalsIgnoreCase(pl.getExternalIP()))
			{
				info.getValue().setExternalIP(pl.getExternalIP());
			}
		}
	}

	public static void load()
	{
		Log.info(eventname + ": Loading DMG Ranking information..!");

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement(
					"SELECT `classid`, `newdmg`, `newname`, `playerid`, `externalIP`, `internalIP` FROM `dmg_data`");

			ResultSet rs = statement.executeQuery();

			while (rs.next())
			{
				dmginfo info = new dmginfo();

				info.setdmg(rs.getInt("classid"), rs.getInt("newdmg"), rs.getString("newname"), rs.getInt("playerid"),
						rs.getString("externalIP"), rs.getString("internalIP"));

				dmgIinfo.put(info.getClassId(), info);
			}

			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
}
