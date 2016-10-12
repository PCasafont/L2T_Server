package l2server.gameserver.communitybbs.Manager;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.datatables.NpcTable.DropChances;
import l2server.gameserver.events.DamageManager;
import l2server.gameserver.events.LotterySystem;
import l2server.gameserver.events.instanced.EventsManager;
import l2server.gameserver.handler.IVoicedCommandHandler;
import l2server.gameserver.handler.VoicedCommandHandler;
import l2server.gameserver.instancemanager.*;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2RaidBossInstance;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.entity.ClanWarManager.ClanWar;
import l2server.gameserver.model.olympiad.Olympiad;
import l2server.gameserver.model.zone.type.L2PeaceZone;
import l2server.gameserver.model.zone.type.L2SiegeZone;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.ShowBoard;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author LasTravel
 */
public class CustomCommunityBoard
{
	private static String newsInfo = "";

	// Other
	private static List<cmboard> cmboard_info = new ArrayList<>();
	private static List<Object> _raidIds = new ArrayList<>();
	private static List<Object> _bossIds = new ArrayList<>();

	private class cmboard
	{
		private String _postDate;
		private String _postTitle;
		private String _postUrl;

		private String getDate()
		{
			long timestamp = Long.parseLong(_postDate);
			return new SimpleDateFormat("dd/MM/yyyy").format(new Date(timestamp * 1000));
		}

		private String getUrl()
		{
			return _postUrl;
		}

		private String getTitle()
		{
			if (_postTitle.length() > 40)
			{
				_postTitle = _postTitle.substring(0, 40) + "...";
			}
			return _postTitle.replace("&#33;", "!");
		}

		private cmboard(String date, String title, String postUrl)
		{
			_postDate = date;
			_postTitle = title;
			_postUrl = postUrl;
		}
	}

	private String getCustomNewsBoard()
	{
		if (newsInfo.isEmpty())
		{
			loadForumNews();
		}

		if (newsInfo.isEmpty())
		{
			return "<html><body>%menu%<center><br><table width=610><tr><td width=610 align=center><font color=\"LEVEL\">Can't show this page at this moment.</font></td></tr></table></center></body></html>";
		}

		return newsInfo;
	}

	private void loadForumNews()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement("SELECT * FROM " + Config.FORUM_DB_NAME +
					".forums_topics WHERE forum_id = 4 AND pinned = 0 AND approved = 1 ORDER BY start_date DESC LIMIT 0, 70");
			ResultSet rs = st.executeQuery();
			while (rs.next())
			{
				String tid = String.valueOf(rs.getInt("tid"));
				String titleSeo = rs.getString("title_seo");

				PreparedStatement st2 =
						con.prepareStatement("SELECT * FROM " + Config.FORUM_DB_NAME + ".forums_posts WHERE pid = ?");
				st2.setInt(1, rs.getInt("topic_firstpost"));

				ResultSet rs2 = st2.executeQuery();
				rs2.next();

				String postUrl = "url http://www." + Config.FORUM_DB_NAME.replace("_board", "") +
						".com/board/index.php?/topic/" + tid + "-" + titleSeo + "/";
				cmboard_info.add(new cmboard(rs2.getString("post_date"), rs.getString("title").replace("&#39;", "'"),
						postUrl));

				rs2.close();
				st2.close();
			}
			rs.close();
			st.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		buildCmPage();
	}

	private void buildCmPage()
	{
		if (cmboard_info.isEmpty())
		{
			return;
		}

		String htmltext = "<html><body>" + getCommunityPage("mainMenu") + "<br>";
		htmltext +=
				"<center><table><tr><td><img src=\"icon.etc_alphabet_n_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_e_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_w_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_s_i00\" width=32 height=32></td></tr></table></center>";
		htmltext += "<br><br><br>";
		htmltext += "<center><table border=0 width=750 cellspacing=2 cellpadding=2>";

		int count = 0;
		int newsDone = 0;
		for (cmboard info : cmboard_info)
		{
			if (count == 0)
			{
				htmltext += "<tr>";
			}

			htmltext += "<td><table width=375><tr><td><font color=\"LEVEL\"><a action=\"" + info.getUrl() + "\">" +
					info.getTitle() + "</a></font> (" + info.getDate() + ")</td></tr></table></td>";
			if (cmboard_info.size() > newsDone)
			{
				count++;
				newsDone++;
				if (count == 2)
				{
					htmltext += "</tr>";
					count = 0;
				}
				else if (cmboard_info.size() == newsDone)
				{
					htmltext += "</tr>";
				}
			}
		}

		htmltext += "</table></center>";
		htmltext += "<br><br></body></html>";

		newsInfo = htmltext;
		cmboard_info.clear();
	}

	private String getCustomCastleInfoBoard()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(
				"<html><body>%menu%<br><center><table><tr><td><img src=icon.etc_alphabet_c_i00 width=32 height=32></td><td><img src=icon.etc_alphabet_a_i00 width=32 height=32></td><td><img src=icon.etc_alphabet_s_i00 width=32 height=32></td><td><img src=icon.etc_alphabet_t_i00 width=32 height=32></td><td><img src=icon.etc_alphabet_l_i00 width=32 height=32></td><td><img src=icon.etc_alphabet_e_i00 width=32 height=32></td><td></td><td></td><td></td><td></td><td><img src=icon.etc_alphabet_i_i00 width=32 height=32></td><td><img src=icon.etc_alphabet_n_i00 width=32 height=32></td><td><img src=icon.etc_alphabet_f_i00 width=32 height=32></td><td><img src=icon.etc_alphabet_o_i00 width=32 height=32></td></tr></table><br><br><br>");

		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			L2Clan clan = ClanTable.getInstance().getClan(castle.getOwnerId());
			List<L2SiegeClan> attackerClanList = castle.getSiege().getAttackerClans();
			List<L2SiegeClan> defenderClanList = castle.getSiege().getDefenderClans();

			String tax = Integer.toString(castle.getTaxPercent());
			String siegeDate = castle.getSiegeDate().getTime().toString().replace("2015", "");

			sb.append("<table width=710 border=0 bgcolor=999999><tr><td align=center FIXWIDTH=710>" + castle.getName() +
					"</td></tr></table>");
			sb.append("<table width=710 height=160 border=0><tr><td><table><tr><td><img src=\"Crest.pledge_crest_" +
					Config.SERVER_ID + "_" + castle.getCastleId() + "\" width=256 height=128></td></tr></table></td>");
			sb.append("<td FIXWIDTH=450><table width=450 border=0>");
			if (clan != null)
			{
				sb.append("<tr><td FIXWIDTH=110>Owner Clan:</td><td FIXWIDTH=360> " + clan.getName() + "</td></tr>");
				sb.append("<tr><td FIXWIDTH=110>Clan Leader:</td><td FIXWIDTH=360> " + clan.getLeaderName() +
						"</td></tr>");
			}
			else
			{
				sb.append("<tr><td FIXWIDTH=110>Owner Clan:</td><td FIXWIDTH=360>NPC</td></tr>");
			}
			sb.append("<tr><td>Tax Rate:</td><td> " + tax + "%</td></tr>");
			sb.append("<tr><td>Siege Date:</td><td> " + siegeDate + "</td></tr>");

			// Defenders list part
			int defenderListSize = defenderClanList.size();
			if (defenderListSize > 0)
			{
				String defenderClans = "";
				for (L2SiegeClan siegeClan : defenderClanList)
				{
					if (siegeClan == null)
					{
						continue;
					}

					defenderClans += ClanTable.getInstance().getClan(siegeClan.getClanId()).getName() +
							(defenderListSize > 1 ? ", " : "");
					defenderListSize--;
				}
				sb.append("<tr><td>Defenders:</td><td> " + defenderClans + "</td></tr>");
			}
			else
			{
				sb.append("<tr><td>Defenders:</td><td>NPC</td></tr>");
			}

			// Attacker list part
			int attackerListSize = attackerClanList.size();
			if (attackerListSize > 0)
			{
				String attackerClans = "";
				for (L2SiegeClan siegeClan : attackerClanList)
				{
					if (siegeClan == null)
					{
						continue;
					}

					attackerClans += ClanTable.getInstance().getClan(siegeClan.getClanId()).getName() +
							(attackerListSize > 1 ? ", " : "");
					attackerListSize--;
				}
				sb.append("<tr><td>Attackers:</td><td>" + attackerClans + "</td></tr>");
			}
			else
			{
				sb.append("<tr><td>Attackers:</td><td>No Attackers</td></tr>");
			}

			sb.append("<tr><td>Tendency:</td><td>" + (castle.getTendency() == Castle.TENDENCY_DARKNESS ? "Darkness" :
					castle.getTendency() == Castle.TENDENCY_LIGHT ? "Light" : "None") + "</td></tr>");

			if (castle.getSiege() != null && castle.getSiege().getIsInProgress())
			{
				sb.append("<tr><td>Is under Siege:</td><td>Yes</td></tr>");
			}
			else
			{
				sb.append("<tr><td>Is under Siege:</td><td>No</td></tr>");
			}

			sb.append("</table></td>");
			sb.append("</tr></table>");
		}
		sb.append("</center></body></html>");

		return sb.toString();
	}

	public String createPages(int pageToShow, int maxPages, String url, String extraUrl)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<table border=0 cellspacing=0 cellpadding=0 background=\"L2UI_CT1.Tab_DF_Tab_Selected\"><tr>");

		for (int x = 0; x < maxPages; x++)
		{
			int pagenr = x + 1;
			if (pageToShow == x)
			{
				sb.append("<td><button value=" + String.valueOf(pagenr) + " width=30 height=30 action=\"bypass " + url +
						x + extraUrl +
						"\" fore=\"L2UI_CT1.Tab_DF_Tab_Selected\" back=\"L2UI_CT1.Tab_DF_Tab_Selected\"></td>");
			}
			else
			{
				sb.append("<td><button value=" + String.valueOf(pagenr) + " width=30 height=30 action=\"bypass " + url +
						x + extraUrl +
						"\" fore=\"L2UI_CT1.Tab_DF_Tab_Unselected\" back=\"L2UI_CT1.Tab_DF_Tab_Selected\"></td>");
			}
		}

		sb.append("</tr></table>");
		return sb.toString();
	}

	private void loadRaidData()
	{
		Map<Object, Long> raidIds = new HashMap<>();
		for (L2RaidBossInstance raid : BossManager.getInstance().getBosses().values())
		{
			if (raid != null && raid.getSpawn().getRespawnDelay() >= 3600)
			{
				raidIds.put(raid.getNpcId(), (long) raid.getMaxHp() * (raid.getPDef(null) + raid.getMDef(null, null)));
			}
		}

		raidIds = sortByValue(raidIds, false);
		_raidIds.addAll(raidIds.keySet());
		Map<Object, Long> bossIds = new HashMap<>();

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement stmt = con.prepareStatement("SELECT `boss_id` FROM `grandboss_data`");
			ResultSet rset = stmt.executeQuery();

			while (rset.next())
			{
				L2NpcTemplate boss = NpcTable.getInstance().getTemplate(rset.getInt("boss_id"));
				if (boss != null)
				{
					bossIds.put(rset.getInt("boss_id"),
							(long) NpcTable.getInstance().getTemplate(rset.getInt("boss_id")).Level);
				}
			}

			rset.close();
			stmt.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		bossIds = sortByValue(bossIds, true);
		_bossIds.addAll(bossIds.keySet());
	}

	public String getCustomGrandBossInfo()
	{
		StringBuilder sb = new StringBuilder();

		sb.append(
				"<html><body>%menu%<br><center><table><tr><td><img src=\"icon.etc_alphabet_e_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_p_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_i_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_c_i00\" width=32 height=32></td><td></td><td></td><td></td><td></td><td><img src=\"icon.etc_alphabet_b_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_o_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_s_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_s_i00\" width=32 height=32></td><td></td><td></td><td></td><td></td><td><img src=\"icon.etc_alphabet_i_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_n_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_f_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_o_i00\" width=32 height=32></td></tr></table></center><br><br>");
		sb.append("<center>");
		sb.append("<table width=100%><tr><td align=center>Open World Bosses</td></tr></table>");
		sb.append(
				"<table width=750 bgcolor=999999><tr><td FIXWIDTH=20>#</td><td FIXWIDTH=50>Name</td><td FIXWIDTH=30>Level</td><td FIXWIDTH=150>Respawn</td><td FIXWIDTH=120>Status</td><td FIXWIDTH=20>Drop</td></tr></table>");

		L2NpcTemplate boss = null;

		int pos = 0;
		int spawnTime = 0;
		int randomSpawnTime = 0;

		for (Object id : _bossIds)
		{
			int bossId = (Integer) id;
			if (bossId == 29054 || bossId == 29065)
			{
				continue;
			}

			boss = NpcTable.getInstance().getTemplate(bossId);
			if (boss == null)
			{
				continue;
			}

			int status = GrandBossManager.getInstance().getBossStatus(bossId);
			String bossStatus = "<font color=99FF00>Alive</font>";
			StatsSet bossInfo = GrandBossManager.getInstance().getStatsSet(boss.NpcId);
			if (bossInfo == null)
			{
				continue;
			}

			if (status == GrandBossManager.getInstance().FIGHTING)
			{
				bossStatus = "<font color=LEVEL>Under Attack</font>";
			}
			else if (status == GrandBossManager.getInstance().WAITING)
			{
				bossStatus = "<font color=00FF00>Waiting</font>";
			}
			else if (status == GrandBossManager.getInstance().DEAD)
			{
				long respawnTime = bossInfo.getLong("respawn_time");
				int diff = (int) ((respawnTime - System.currentTimeMillis()) / 1000);
				int days = diff / (3600 * 24);
				double gradient = Math.min(diff / (24.0 * 3600), 1.0);
				String remainingTime = "";
				if (days > 0)
				{
					remainingTime = "around " + days + " day";
					if (days > 1)
					{
						remainingTime += "s";
					}
				}
				else
				{
					int hours = diff / 3600 % 24;
					if (hours > 0)
					{
						remainingTime = "around " + hours + " hour";
						if (hours > 1)
						{
							remainingTime += "s";
						}
					}
					else if (diff % 3600 >= 20 * 60)
					{
						remainingTime = "less than 1 hour";
					}
					else
					{
						remainingTime = "less than 20 minutes";
					}
				}
				String color =
						String.format("%02x%02x%02x", (int) ((1 - gradient) * 0x80) + 0x7f, (int) (gradient * 0x80),
								(int) (gradient * 0x80));
				bossStatus = "<font color=" + color + ">Respawns in " + remainingTime + "</font>";
			}

			spawnTime = GrandBossManager.getInstance().getRespawnTime(boss.NpcId);
			randomSpawnTime = GrandBossManager.getInstance().getRandomRespawnTime(boss.NpcId);

			int spawnTimeDays = spawnTime / (3600000 * 24);
			int spawnTimeHours = spawnTime / 3600000 % 24;
			String spawnTime1 = spawnTimeDays + " day";
			if (spawnTimeDays > 1)
			{
				spawnTime1 += "s";
			}
			if (spawnTimeHours > 0)
			{
				spawnTime1 += " and " + spawnTimeHours + " hours";
			}

			spawnTimeDays = (spawnTime + randomSpawnTime) / (3600000 * 24);
			spawnTimeHours = (spawnTime + randomSpawnTime) / 3600000 % 24;
			String spawnTime2 = spawnTimeDays + " days";
			if (spawnTimeHours > 0)
			{
				spawnTime2 += " and " + spawnTimeHours + " hours";
			}
			String bossRespawn = spawnTime1 + ", up to " + spawnTime2;

			//Special respawn cases
			if (bossId == 25286) //Anakim
			{
				bossRespawn = "Tuesday (21:00) and Saturday (16:00)";
			}
			else if (bossId == 25283) //Lilith
			{
				bossRespawn = "Thursday (21:00) and Saturday (14:00)";
			}

			sb.append(
					"<table border=0 cellspacing=0 cellpadding=2 width=750 height=17><tr><td FIXWIDTH=20>" + (pos + 1) +
							"</td><td FIXWIDTH=50>" + boss.getName() + "</td><td FIXWIDTH=30>" + boss.Level +
							"</td><td FIXWIDTH=150>" + bossRespawn + "</td><td FIXWIDTH=120>" + bossStatus +
							"</td><td FIXWIDTH=20><button value=\" \" width=16 height=16 action=\"bypass _bbscustom;info;drop;" +
							boss.NpcId +
							";1\" fore=L2UI_CH3.aboutotpicon back=L2UI_CH3.aboutotpicon></td></tr></table>");
			sb.append("<img src=\"L2UI.Squaregray\" width=740 height=1>");

			pos++;
		}

		if (!Config.IS_CLASSIC)
		{
			sb.append(getCommunityPage("instanceIdGrandbosses")); //Temp
		}

		sb.append("</center>");
		sb.append("<br><br></body></html>");

		return sb.toString();
	}

	public String getCustomRBInfo(int pageToShow, boolean isGM)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(
				"<html><body>%menu%<br><center><table><tr><td><img src=\"icon.etc_alphabet_r_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_a_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_i_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_d_i00\" width=32 height=32></td><td></td><td></td><td></td><td></td><td><img src=\"icon.etc_alphabet_i_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_n_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_f_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_o_i00\" width=32 height=32></td></tr></table></center><br><br>");

		int maxBossesPerPage = 15;
		int bossSize = _raidIds.size();
		int maxPages = bossSize / maxBossesPerPage;
		if (bossSize > maxBossesPerPage * maxPages)
		{
			maxPages++;
		}
		if (pageToShow > maxPages)
		{
			pageToShow = maxPages;
		}
		int pageStart = maxBossesPerPage * pageToShow;
		int pageEnd = bossSize;
		if (pageEnd - pageStart > maxBossesPerPage)
		{
			pageEnd = pageStart + maxBossesPerPage;
		}

		sb.append("<center>" + createPages(pageToShow, maxPages, "_bbscustom;raids;", "") + "</center><br>");
		sb.append("<center>");
		sb.append(
				"<table width=750 bgcolor=999999><tr><td FIXWIDTH=20>#</td><td FIXWIDTH=140>Name</td><td FIXWIDTH=40>Level</td><td FIXWIDTH=50>Respawn</td><td FIXWIDTH=120>Status</td><td FIXWIDTH=20>Drop</td></tr></table>");

		L2NpcTemplate npc = null; // Get the npc template
		L2RaidBossInstance boss = null; // Get the current npc
		Long respawn = null; // Just the respawn time
		for (int i = pageStart; i < pageEnd; i++)
		{
			npc = NpcTable.getInstance().getTemplate((Integer) _raidIds.get(i));
			if (npc == null)
			{
				continue;
			}

			boolean isInCombat = false;
			boolean isAggro = false;
			boss = BossManager.getInstance().getBoss(npc.NpcId);

			isInCombat = boss.isInCombat();
			isAggro = boss.isAggressive();
			respawn = boss.getSpawn().getNextRespawn();

			String status = "<font color=99FF00>Alive</font>";
			if (isInCombat)
			{
				status = "<font color=LEVEL>Under Attack</font>";
			}
			else if (boss.isDead())
			{
				int diff = (int) ((respawn - System.currentTimeMillis()) / 1000);
				double gradient = Math.min(diff / (24.0 * 3600), 1.0);
				String remainingTime = "";
				int hours = diff / 3600;
				if (hours > 0)
				{
					remainingTime = "around " + hours + " hour";
					if (hours > 1)
					{
						remainingTime += "s";
					}
				}
				else if (diff % 3600 >= 20 * 60)
				{
					remainingTime = "less than 1 hour";
				}
				else
				{
					remainingTime = "less than 20 minutes";
				}
				String color =
						String.format("%02x%02x%02x", (int) ((1 - gradient) * 0x80) + 0x7f, (int) (gradient * 0x80),
								(int) (gradient * 0x80));
				status = "<font color=" + color + ">Respawns in " + remainingTime + "</font>";
			}
			String bossIsAggro = isAggro ? "<font color=FF0000>*</font>" : "";
			String nameString =
					"<a action=\"" + (isGM ? "bypass -h admin_move_to " : "bypass _bbscustom;action;showRadar; ") + "" +
							boss.getSpawn().getX() + " " + boss.getSpawn().getY() + " " + boss.getSpawn().getZ() +
							"\">" + npc.getName() + "</a>";

			sb.append("<table border=0 cellspacing=0 cellpadding=2 width=750 height=17><tr><td FIXWIDTH=20>" + (i + 1) +
					"</td>" + "<td FIXWIDTH=140>" + nameString + bossIsAggro + "</td>" + "<td FIXWIDTH=40>" +
					npc.Level + "</td>" + "<td FIXWIDTH=50>" + boss.getSpawn().getRespawnDelay() / 3600000 + "-" +
					(boss.getSpawn().getRespawnDelay() + boss.getSpawn().getRandomRespawnDelay()) / 3600000 +
					" Hours</td>" + "<td FIXWIDTH=120>" + status + "</td>" +
					"<td FIXWIDTH=20><button value=\" \" width=16 height=16 action=\"bypass _bbscustom;info;drop;" +
					npc.NpcId + ";1\" fore=L2UI_CH3.aboutotpicon back=L2UI_CH3.aboutotpicon></td>" + "</tr></table>");
			sb.append("<img src=\"L2UI.Squaregray\" width=740 height=1>");
		}

		sb.append("</center>");
		sb.append("</body></html>");

		return sb.toString();
	}

	public void parseCmd(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command, ";");
		st.nextToken();

		String val = st.nextToken();
		switch (val)
		{
			case "action":
				switch (String.valueOf(st.nextToken()))
				{
					case "gEvent":
					{
						GMEventManager.getInstance().handleEventCommand(activeChar, command);
						break;
					}

					case "showRadar":
						activeChar.getRadar().addMarker(Integer.valueOf(command.split(" ")[1]),
								Integer.valueOf(command.split(" ")[2]), Integer.valueOf(command.split(" ")[3]));
						break;

					case "trade":
						L2PcInstance target = L2World.getInstance().getPlayer(st.nextToken());
						if (target == null)
						{
							return;
						}
						activeChar.doInteract(target);
						break;

					case "searchDrop":
						String itemName = "";
						if (st.hasMoreTokens())
						{
							itemName = st.nextToken().trim();
						}
						else
						{
							break;
						}

						if (itemName.isEmpty())
						{
							return;
						}

						boolean isSpoil = st.nextToken().trim().equalsIgnoreCase("spoil");
						if (Util.isDigit(itemName))
						{
							int page = 1;
							if (st.hasMoreTokens())
							{
								page = Integer.valueOf(st.nextToken());
							}
							sendCommunityBoardPage(getCommunityPage("searchDrop").replace("%result%",
									SearchDropManager.getInstance()
											.getDrops(activeChar, Integer.valueOf(itemName), isSpoil, page)),
									activeChar);
						}
						else
						{
							sendCommunityBoardPage(getCommunityPage("searchDrop").replace("%result%",
									SearchDropManager.getInstance()
											.searchPossiblesResults(activeChar, itemName, isSpoil)), activeChar);
						}
						break;

					case "worldBuff":
						switch (String.valueOf(st.nextToken()))
						{
							case "addCoin":
							{
								String coin = "";
								if (st.hasMoreTokens())
								{
									coin = st.nextToken().trim();
								}

								CustomOfflineBuffersManager.getInstance().changeCurrencyId(activeChar, coin);
								break;
							}

							case "delCoin":
							{
								CustomOfflineBuffersManager.getInstance().changeCurrencyId(activeChar, null);
								break;
							}

							case "addBuff":
							{
								int skillId = 0;
								Long price = 0L;
								if (st.hasMoreTokens())
								{
									skillId = Integer.valueOf(st.nextToken().trim());
								}
								if (st.hasMoreTokens())
								{
									price = Long.valueOf(st.nextToken().trim());
								}

								if (skillId != 0)
								{
									CustomOfflineBuffersManager.getInstance()
											.addBuffToBuffer(activeChar, skillId, price);
								}
								break;
							}

							case "bufferInfo":
							{
								int playerId = 0;
								if (st.hasMoreTokens())
								{
									playerId = Integer.valueOf(st.nextToken());
								}
								if (playerId != 0)
								{
									CustomOfflineBuffersManager.getInstance()
											.getSpecificBufferInfo(activeChar, playerId);
								}
								break;
							}

							case "getBuff":
							{
								int playerId = 0;
								int skillId = 0;
								if (st.hasMoreTokens())
								{
									playerId = Integer.valueOf(st.nextToken());
								}
								if (st.hasMoreTokens())
								{
									skillId = Integer.valueOf(st.nextToken());
								}
								if (playerId != 0 && skillId != 0)
								{
									CustomOfflineBuffersManager.getInstance()
											.getBuffFromBuffer(activeChar, playerId, skillId);
								}

								break;
							}
							case "delBuff":
								int skillId = 0;
								if (st.hasMoreTokens())
								{
									skillId = Integer.valueOf(st.nextToken());
								}

								if (skillId != 0)
								{
									CustomOfflineBuffersManager.getInstance().delBuffToBuffer(activeChar, skillId);
								}
								break;

							case "addDesc":
								String description = "";
								if (st.hasMoreTokens())
								{
									description = st.nextToken();
								}
								CustomOfflineBuffersManager.getInstance().addDescription(activeChar, description);
								break;

							case "delDesc":
								CustomOfflineBuffersManager.getInstance().addDescription(activeChar, null);
								break;
						}
						break;

					case "buyNumber":
						LotterySystem.getInstance().buyNumber(activeChar, Integer.valueOf(st.nextToken()));
						break;

					case "voice":
						String voicedCommand = st.nextToken();

						IVoicedCommandHandler handler =
								VoicedCommandHandler.getInstance().getVoicedCommandHandler(voicedCommand);
						if (handler == null)
						{
							return;
						}
						handler.useVoicedCommand(voicedCommand, activeChar, voicedCommand);

						parseCmd("_bbscustom;playerPanel", activeChar);
						break;

					case "bid":
						int bidId = 0;
						long bidAmount = 0;
						String coin = "";

						if (st.hasMoreTokens())
						{
							bidId = Integer.valueOf(st.nextToken());
						}
						if (st.hasMoreTokens())
						{
							String amount = st.nextToken().trim();
							if (amount.isEmpty())
							{
								break;
							}
							bidAmount = Long.valueOf(amount);
						}
						if (st.hasMoreTokens())
						{
							coin = st.nextToken().trim();
						}

						if (bidId != 0 && bidAmount != 0)
						{
							TenkaiAuctionManager.getInstance().tryToBid(activeChar, bidId, bidAmount, coin);
						}
						break;

					default:
						Log.warning("CustomCommunityBoard: Wrong command in parseCmd void, command: " + command);
						break;
				}
				break;

			case "main":
				sendCommunityBoardPage(getCommunityPage("index"), activeChar);
				break;

			case "serverNews":
				sendCommunityBoardPage(getCustomNewsBoard(), activeChar);
				break;

			case "castles":
				sendCommunityBoardPage(getCustomCastleInfoBoard(), activeChar);
				break;

			case "worldAltars":
				sendCommunityBoardPage(getCommunityPage("WorldAltars")
								.replace("%altars%", CustomWorldAltars.getInstance().getAltarsInfo(activeChar.isGM())),
						activeChar);
				break;

			case "gainak":
				sendCommunityBoardPage(getCommunityPage("Gainak")
						.replace("%gainak%", getGainakStatus(activeChar, Integer.valueOf(st.nextToken()))), activeChar);
				break;

			case "gmEvent":
				sendCommunityBoardPage(getCommunityPage("GMEvent").replace("%info%",
						GMEventManager.getInstance().getCustomEventPanel(activeChar, Integer.valueOf(st.nextToken()))),
						activeChar);
				break;

			case "worldBuffers":
				sendCommunityBoardPage(getCommunityPage("WorldBuffers").replace("%buffers%",
						CustomOfflineBuffersManager.getInstance()
								.getOfflineBuffersPage(Integer.valueOf(st.nextToken()))), activeChar);
				break;

			case "raids":
				sendCommunityBoardPage(getCustomRBInfo(Integer.valueOf(st.nextToken()), activeChar.isGM()), activeChar);
				break;

			case "grandbosses":
				sendCommunityBoardPage(getCustomGrandBossInfo(), activeChar);
				break;

			case "itemAuction":
				sendCommunityBoardPage(getCommunityPage("itemAuction").replace("%auctionInfo%",
						TenkaiAuctionManager.getInstance()
								.getAuctionInfo(activeChar.getObjectId(), Integer.valueOf(st.nextToken()))),
						activeChar);
				break;

			case "searchDrop":
				sendCommunityBoardPage(getCommunityPage("searchDrop").replace("%result%", ""), activeChar);
				break;

			case "info":
				String toSend = st.nextToken();
				switch (toSend)
				{
					case "drop": // player, npcId, page
						sendDropPage(activeChar, Integer.valueOf(st.nextToken()), Integer.valueOf(st.nextToken()),
								null);
						break;

					default:
						sendNormalChatWindow(activeChar, "/info/" + toSend + ".htm");
						break;
				}
				break;

			case "currentEvent":
				sendCommunityBoardPage(EventsManager.getInstance().getEventInfoPage(activeChar), activeChar);
				break;

			case "playerPanel":
				sendCommunityBoardPage(getCustomPlayerPanelInfo(activeChar), activeChar);
				break;

			case "rankings":
				String rankingType = st.nextToken();

				sendCommunityBoardPage(
						getCommunityPage("rankings").replaceFirst("%ranking%", getRankingInfo(rankingType, activeChar)),
						activeChar);
				break;

			case "lottery":
				sendNormalChatWindow(activeChar, "customLottery.htm");
				break;

			case "buyPanel":
				sendCommunityBoardPage(
						getCustomBuyPage(Integer.valueOf(st.nextToken()), Integer.valueOf(st.nextToken())), activeChar);
				break;

			default:
				Log.warning("CustomCommunityBoard: Wrong command in parseCmd void, command: " + command);
				break;
		}
	}

	private String getGainakStatus(L2PcInstance pl, int pageToShow)
	{
		StringBuilder sb = new StringBuilder();

		//Easy way to check if it's in war mode...
		L2SiegeZone gainakSiegeZone = ZoneManager.getInstance().getZone(15547, -114380, -240, L2SiegeZone.class);
		L2PeaceZone gainakSafeZone = ZoneManager.getInstance().getZone(15547, -114380, -240, L2PeaceZone.class);
		if (gainakSiegeZone != null)
		{
			sb.append("<table><tr><td><img src=\"Crest.pledge_crest_%serverId%_" +
					(gainakSiegeZone.isActive() ? 51 : 50) + "\"  width=512 height=128></td></tr></table>");
			sb.append("<br><br>");

			if (gainakSiegeZone.isActive())
			{
				List<L2PcInstance> gainakPlayers = gainakSiegeZone.getPlayersInside();
				if (gainakSafeZone != null)
				{
					for (L2PcInstance zonePl : gainakSafeZone.getPlayersInside())
					{
						if (zonePl == null || gainakPlayers.contains(zonePl))
						{
							continue;
						}
						gainakPlayers.add(zonePl);
					}
				}

				int maxPlayersPerPage = 20;
				int playersSize = gainakPlayers.size();
				int maxPages = playersSize / maxPlayersPerPage;
				if (playersSize > maxPlayersPerPage * maxPages)
				{
					maxPages++;
				}
				if (pageToShow > maxPages)
				{
					pageToShow = maxPages;
				}
				int pageStart = maxPlayersPerPage * pageToShow;
				int pageEnd = playersSize;
				if (pageEnd - pageStart > maxPlayersPerPage)
				{
					pageEnd = pageStart + maxPlayersPerPage;
				}

				if (maxPages > 1)
				{
					sb.append(
							"<center>" + createPages(pageToShow, maxPages, "_bbscustom;gainak;", "") + "</center><br>");
				}
				sb.append(
						"<table width=600 bgcolor=999999><tr><td FIXWIDTH=100 align=center>Name</td><td FIXWIDTH=100 align=center>Clan</td><td FIXWIDTH=100 align=center>Ally</td></tr></table>");

				for (int i = pageStart; i < pageEnd; i++)
				{
					L2PcInstance player = gainakPlayers.get(i);
					if (player == null || player.isGM())
					{
						continue;
					}

					String color = ""; //no color by default
					String clanName = "";
					String allyName = "";
					if (player.getClan() != null)
					{
						if (player.getClan() == pl.getClan())
						{
							color = "<font color=LEVEL>";
						}

						clanName = player.getClan().getName();
						if (player.getClan().getAllyId() != 0)
						{
							allyName = player.getClan().getAllyName();
						}
					}
					sb.append("" + color + "<table width=600><tr><td FIXWIDTH=100 align=center> " + player.getName() +
							" </td><td FIXWIDTH=100 align=center>" + clanName + "</td><td FIXWIDTH=100 align=center>" +
							allyName + "</td></tr></table>" + (color.length() > 0 ? "</font>" : color) + "");
					sb.append(
							"<table width=600><tr><td><img src=\"L2UI.Squaregray\" width=600 height=1></td></tr></table>");
				}
			}
			else
			{
				sb.append(
						"<table width=600><tr><td align=center><font color=LEVEL>Gainak is in peace!</font></td></tr></table>");
			}
		}
		return sb.toString();
	}

	public void sendDropPage(L2PcInstance pl, int npcId, int page, L2Npc npc)
	{
		if (pl == null)
		{
			return;
		}

		//- There are some cases where we should replace the boss ids, for example the Guillotine of death have 3 transformations, only the first one appear on the
		//community page but only the last one have drops so...
		//- There are other cases where the players can detect the fake raids checking the drops with deck, on that cases we will change it for the real raid id

		//Execution Grounds Watchman Guillotine
		if (npcId == 25888 || npcId == 25885)
		{
			npcId = 25892;
		}

		//Beleth Clones
		if (npcId == 29119)
		{
			npcId = 29118;
		}
		if (npcId == 80217)
		{
			npcId = 80216;
		}

		//Spezions Clones
		if (npcId == 25868)
		{
			npcId = 25867;
		}

		L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
		if (template == null)
		{
			return;
		}

		DecimalFormat chanceFormat = new DecimalFormat("#.###");

		String replyMSG =
				"<html>" + (template.getName().length() > 0 ? "<title>" + template.getName() + "</title>" : "") +
						"<body>";

		int myPage = 1;
		int i = 0;
		int shown = 0;
		boolean hasMore = false;

		Map<L2DropData, Float> drops = new HashMap<>();
		Map<L2DropData, Float> spoilDrops = new HashMap<>();

		if (template.getMultiDropData() != null)
		{
			for (L2DropCategory catDrop : template.getMultiDropData())
			{
				if (catDrop == null)
				{
					continue;
				}

				if (catDrop.getAllDrops() != null)
				{
					for (L2DropData drop : catDrop.getAllDrops())
					{
						drops.put(drop, catDrop.getChance());
					}
				}
			}
		}

		if (template.getDropData() != null)
		{
			for (L2DropData drop : template.getDropData())
			{
				drops.put(drop, 100.0f);
			}
		}

		if (template.getSpoilData() != null)
		{
			for (L2DropData drop : template.getSpoilData())
			{
				spoilDrops.put(drop, 100.0f);
			}
		}

		if (!drops.isEmpty())
		{
			replyMSG += "<center><font color=LEVEL>General drops:</font></center>";
		}

		for (Entry<L2DropData, Float> drop : drops.entrySet())
		{
			if (shown == 20)
			{
				hasMore = true;
				break;
			}

			if (myPage != page)
			{
				i++;
				if (i == 20)
				{
					myPage++;
					i = 0;
				}
				continue;
			}

			DropChances chances = NpcTable.getInstance()
					.calculateRewardChances(template, pl, drop.getKey(), drop.getValue(), 0, false, npc);

			replyMSG += "<table><tr><td width=40><img src=\"" + chances.icon +
					"\" width=32 height=32></td><td width=220><table><tr><td width=220>" + chances.itemName;
			if (chances.max > 1)
			{
				if (chances.min == chances.max)
				{
					replyMSG += " (" + chances.min + ")";
				}
				else
				{
					replyMSG += " (" + chances.min + "-" + chances.max + ")";
				}
			}
			replyMSG += "</td></tr>";
			if (chances.chance < 100.0)
			{
				replyMSG += "<tr><td width=220>Chance: " + chanceFormat.format(chances.chance) + "%</td></tr>";
			}
			replyMSG += "</table></td></tr></table>";
			replyMSG += "</td></tr></table></td></tr></table>";

			shown++;
		}

		if (!spoilDrops.isEmpty())
		{
			replyMSG += "<br><br><center><font color=LEVEL>Spoil drops:</font></center>";
			for (Entry<L2DropData, Float> drop : spoilDrops.entrySet())
			{
				if (shown == 20)
				{
					hasMore = true;
					break;
				}

				if (myPage != page)
				{
					i++;
					if (i == 20)
					{
						myPage++;
						i = 0;
					}
					continue;
				}

				DropChances chances = NpcTable.getInstance()
						.calculateRewardChances(template, pl, drop.getKey(), drop.getValue(), 0, true, npc);

				replyMSG += "<table><tr><td width=40><img src=\"" + chances.icon +
						"\" width=32 height=32></td><td width=220><table><tr><td width=220>" + chances.itemName;
				if (chances.max > 1)
				{
					if (chances.min == chances.max)
					{
						replyMSG += " (" + chances.min + ")";
					}
					else
					{
						replyMSG += " (" + chances.min + "-" + chances.max + ")";
					}
				}
				replyMSG += "</td></tr>";
				if (chances.chance < 100.0)
				{
					replyMSG += "<tr><td width=220>Chance: " + chanceFormat.format(chances.chance) + "%</td></tr>";
				}
				replyMSG += "</table></td></tr></table>";
				replyMSG += "</td></tr></table></td></tr></table>";

				shown++;
			}
		}

		replyMSG += "<br><center><table width=250><tr>";

		if (page > 1)
		{
			replyMSG += "<td width=120><a action=\"bypass _bbscustom;info;drop;" + npcId + ";" + (page - 1) +
					"\">Prev Page</a></td>";

			if (!hasMore)
			{
				replyMSG += "<td width=100>Page " + page + "</td><td width=70></td>";
			}
		}
		if (hasMore)
		{
			if (page <= 1)
			{
				replyMSG += "<td width=120></td>";
			}

			replyMSG += "<td width=100>Page " + page + "</td><td width=70><a action=\"bypass _bbscustom;info;drop;" +
					npcId + ";" + (page + 1) + "\">Next Page</a></td>";
		}

		replyMSG += "</tr></table></center>";

		replyMSG += "</body></html>";

		pl.sendPacket(new NpcHtmlMessage(0, replyMSG));
	}

	private String getCustomPlayerPanelInfo(L2PcInstance pl)
	{
		String a = getCommunityPage("playerPanel");
		String isNoExp = pl.isNoExp() ? "Disable" : "Enable";
		String isRefusingBuffs = pl.isRefusingBuffs() ? "Disable" : "Enable";
		String isRefusingRequests = pl.getIsRefusingRequests() ? "Disable" : "Enable";
		String isRefusalPms = pl.getMessageRefusal() ? "Disable" : "Enable";
		String isRefusalKillInfo = pl.getIsRefusalKillInfo() ? "Disable" : "Enable";
		String isLandRates = pl.isLandRates() ? "Disable" : "Enable";
		String isStabs = pl.isShowingStabs() ? "Disable" : "Enable";
		String isWeaponGlowDisabled = pl.getIsWeaponGlowDisabled() ? "Disable" : "Enable";
		String isArmorGlowDisabled = pl.getIsArmorGlowDisabled() ? "Disable" : "Enable";
		String isNickNameWingsDisabled = pl.isNickNameWingsDisabled() ? "Disable" : "Enable";

		a = a.replace("%refuseXp%", "<button value=" + isNoExp +
				" width=90 height=24 action=\"bypass _bbscustom;action;voice;noexp\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over>");
		a = a.replace("%blockrequests%", "<button value=" + isRefusingRequests +
				" width=90 height=24 action=\"bypass_bbscustom;action;voice;blockrequests;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over>");
		a = a.replace("%refuseBuffs%", "<button value=" + isRefusingBuffs +
				" width=90 height=24 action=\"bypass_bbscustom;action;voice;refusebuff;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over>");
		a = a.replace("%blockpms%", "<button value=" + isRefusalPms +
				" width=90 height=24 action=\"bypass_bbscustom;action;voice;blockpms;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over>");
		a = a.replace("%refuseKillInfo%", "<button value=" + isRefusalKillInfo +
				" width=90 height=24 action=\"bypass_bbscustom;action;voice;refusekillinfo;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over>");
		a = a.replace("%landRates%", "<button value=" + isLandRates +
				" width=90 height=24 action=\"bypass_bbscustom;action;voice;landrates;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over>");
		a = a.replace("%stabs%", "<button value=" + isStabs +
				" width=90 height=24 action=\"bypass_bbscustom;action;voice;stabs;\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over>");
		a = a.replace("%disableWeaponGlow%", "<button value=" + isWeaponGlowDisabled +
				" width=90 height=24 action=\"bypass _bbscustom;action;voice;disableweaponglow\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over>");
		a = a.replace("%disableArmorGlow%", "<button value=" + isArmorGlowDisabled +
				" width=90 height=24 action=\"bypass _bbscustom;action;voice;disablearmorglow\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over>");
		a = a.replace("%disableWings%", "<button value=" + isNickNameWingsDisabled +
				" width=90 height=24 action=\"bypass _bbscustom;action;voice;disablenicknamewings\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over>");
		return a;
	}

	private String getCustomBuyPage(int pageToShow, int type)
	{
		StringBuilder sb = new StringBuilder();

		sb.append(
				"<html><body>%menu%<br><center><table><tr><td><img src=\"icon.etc_alphabet_P_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_l_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_a_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_y_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_e_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_r_i00\" width=32 height=32></td><td></td><td></td><td></td><td></td><td><img src=\"icon.etc_alphabet_S_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_h_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_o_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_p_i00\" width=32 height=32></td><td><img src=\"icon.etc_alphabet_s_i00\" width=32 height=32></td></tr></table></center><br><br>");

		List<L2PcInstance> _shops = L2World.getInstance().getAllPlayerShops();

		int maxPlayersPerPage = 20;
		int playersSize = _shops.size();
		int maxPages = playersSize / maxPlayersPerPage;
		if (playersSize > maxPlayersPerPage * maxPages)
		{
			maxPages++;
		}
		if (pageToShow > maxPages)
		{
			pageToShow = maxPages;
		}
		int pageStart = maxPlayersPerPage * pageToShow;
		int pageEnd = playersSize;
		if (pageEnd - pageStart > maxPlayersPerPage)
		{
			pageEnd = pageStart + maxPlayersPerPage;
		}

		sb.append(
				"<table width=750><tr><td FIXWIDTH=53>Show: &nbsp;<a action=\"bypass _bbscustom;buyPanel;0;0\">All</a>,&nbsp; <a action=\"bypass _bbscustom;buyPanel;0;1\">Buy</a>,&nbsp; <a action=\"bypass _bbscustom;buyPanel;0;2\">Sell</a>,&nbsp; <a action=\"bypass _bbscustom;buyPanel;0;3\">Craft</a>,&nbsp; <a action=\"bypass _bbscustom;buyPanel;0;10\">Custom Sell</a></td></tr></table><center><table width=750><tr><td FIXWIDTH=50 align=center>" +
						(maxPages > 1 ? createPages(pageToShow, maxPages, "_bbscustom;buyPanel;", ";" + type) : "") +
						"</td></tr></table></center><br>");
		sb.append("<center>");
		sb.append(
				"<table width=750 bgcolor=999999><tr><td FIXWIDTH=25>#</td><td FIXWIDTH=150>Name</td><td FIXWIDTH=90>Shop Type</td><td FIXWIDTH=110>Message</td></tr></table>");

		for (int i = pageStart; i < pageEnd; i++)
		{
			L2PcInstance shop = _shops.get(i);
			if (type == 1 && shop.getPrivateStoreType() != 3 || type == 10 && shop.getPrivateStoreType() != 10 ||
					type == 2 && shop.getPrivateStoreType() != 1 || type == 3 && shop.getPrivateStoreType() != 5)
			{
				continue;
			}

			String title = shop.getShopMessage();
			if (title == null)
			{
				title = "No Message";
			}

			sb.append("<table border=0 cellspacing=0 cellpadding=2 width=750 height=17><tr><td FIXWIDTH=25>" + i +
					"</td><td FIXWIDTH=150><a action=\"bypass _bbscustom;action;trade;" + shop.getName() + "\">" +
					shop.getName() + "</a></td><td FIXWIDTH=90>" + shop.getShopNameType() + "</td><td FIXWIDTH=110>" +
					title + "</td></tr></table>");
			sb.append("<img src=\"L2UI.Squaregray\" width=740 height=1>");
		}

		sb.append("</center>");
		sb.append("<br><br></body></html>");

		return sb.toString();
	}

	public String getCommunityPage(String pageName)
	{
		return HtmCache.getInstance().getHtm(null, "CommunityBoard/" + pageName + ".htm");
	}

	private void sendNormalChatWindow(L2PcInstance pl, String path)
	{
		if (pl == null)
		{
			return;
		}

		NpcHtmlMessage htmlPage = new NpcHtmlMessage(0);
		htmlPage.setFile(null, "CommunityBoard/" + path + "");
		htmlPage.replace("%serverId%", String.valueOf(Config.SERVER_ID));

		if (path.contains("Lottery"))
		{
			htmlPage.replace("%ticketPrice%", String.valueOf(Config.CUSTOM_LOTTERY_PRICE_AMOUNT));
			htmlPage.replace("%lotteryMultiplier%", Config.CUSTOM_LOTTERY_REWARD_MULTIPLIER > 1 ?
					"x" + String.valueOf(Config.CUSTOM_LOTTERY_REWARD_MULTIPLIER) : "");
			htmlPage.replace("%totalCoins%", String.valueOf(LotterySystem.getInstance().getTotalCoins()));
			htmlPage.replace("%totalPrize%", String.valueOf(LotterySystem.getInstance().getTotalPrize()));
			htmlPage.replace("%numbers%", LotterySystem.getInstance().getAvailableNumbers(pl));
		}

		if (path.contains("DamageDealer"))
		{
			htmlPage.replace("%damageDealerReward%", String.valueOf(Config.CUSTOM_DAMAGE_MANAGER_REWARD_AMOUNT));
		}

		//Little glitch  to integrate the farm info to the GK npc with back button...
		if (path.contains("Farm"))
		{
			int targetObjId = 0;
			L2Npc target = null;
			if (pl.getTarget() != null && pl.getTarget() instanceof L2Npc)
			{
				target = (L2Npc) pl.getTarget();
			}
			if (target != null && target.getInstanceId() == pl.getObjectId() && target.getNpcId() == 40001)
			{
				targetObjId = pl.getTarget().getObjectId();
				htmlPage.replace("%backButton%",
						"<table width=120 align=right><tr><td align=left width=20><img src=\"L2UI.bbs_reply\" width=16 height=16></td><td align=left width=104><font color=82a0b1><a action=\"bypass -h npc_" +
								targetObjId +
								"_Chat farm_zones\"><font color=999999>Back</font></a></font></td></tr></table>");
			}
			else
			{
				htmlPage.replace("%backButton%", "");
			}
		}

		pl.sendPacket(htmlPage);
	}

	private void sendCommunityBoardPage(String html, L2PcInstance player)
	{
		if (html == null)
		{
			return;
		}

		html = html.replaceFirst("%menu%", getCommunityPage("mainMenu"));
		html = html.replaceAll("%serverId%", String.valueOf(Config.SERVER_ID));

		//html = html.replace("%onlineCount%", String.valueOf(getPlayerCount()));

		if (html.length() < 8180)
		{
			player.sendPacket(new ShowBoard(html, "101"));
			player.sendPacket(new ShowBoard(null, "102"));
			player.sendPacket(new ShowBoard(null, "103"));
		}
		else if (html.length() < 8180 * 2)
		{
			player.sendPacket(new ShowBoard(html.substring(0, 8180), "101"));
			player.sendPacket(new ShowBoard(html.substring(8180, html.length()), "102"));
			player.sendPacket(new ShowBoard(null, "103"));
		}
		else if (html.length() < 8180 * 3)
		{
			player.sendPacket(new ShowBoard(html.substring(0, 8180), "101"));
			player.sendPacket(new ShowBoard(html.substring(8180, 8180 * 2), "102"));
			player.sendPacket(new ShowBoard(html.substring(8180 * 2, html.length()), "103"));
		}
		else
		{
			Log.warning(
					"CustomCommunityBoard: sendCommunityBoardPage this html exceeds the max html size supported by the client, requestor: " +
							player.getName() + " html size: " + html.length());
		}
	}

	@SuppressWarnings("unused")
	private int getPlayerCount()
	{
		double multiplier = 2.0 - ((float) (System.currentTimeMillis() / 1000) - 1401565000) * 0.0000001;
		/*if (multiplier > 2.5f)
            multiplier = 2.5f - (multiplier - 2.5f);
		if (multiplier < 1)
			multiplier = 1;*/

		return (int) Math.round(L2World.getInstance().getAllPlayersCount() * multiplier + Rnd.get(1));
	}

	private String getRankingInfo(String rankingType, L2PcInstance player)
	{
		if (rankingType.equalsIgnoreCase("damageDealer"))
		{
			return DamageManager.getInstance().getRankingInfo();
		}
		else if (rankingType.equalsIgnoreCase("main"))
		{
			return "<table width=750><tr><td FIXWIDTH=750></td></tr></table>";
		}

		boolean isOlympiadPoints = rankingType.equalsIgnoreCase("olympiadPoints");
		boolean isClanWarPoints = rankingType.equalsIgnoreCase("clanWarPoints");
		boolean isRaidPoints = rankingType.equalsIgnoreCase("raidPoints");
		boolean isClanReputation = rankingType.equalsIgnoreCase("clanReputation");
		boolean isReputation = rankingType.equalsIgnoreCase("reputation");
		boolean isFameRanking = rankingType.equalsIgnoreCase("fame");
		boolean isPvPRanking = rankingType.equalsIgnoreCase("pvp");
		String whatCheckInTop =
				isClanReputation || isClanWarPoints ? player.getClan() != null ? player.getClan().getName() : "" :
						player.getName();
		boolean playerIsInTop = false;

		Map<Object, Long> allPlayers = new HashMap<>();
		for (L2PcInstance pl : L2World.getInstance().getAllPlayers().values())
		{
			if (pl == null || pl.isInStoreMode() || pl.isGM())
			{
				continue;
			}

			if (isClanReputation &&
					(pl.getClan() == null || pl.getClan() != null && allPlayers.containsKey(pl.getClan().getName())))
			{
				continue;
			}

			if (isClanWarPoints && (pl.getClan() == null || pl.getClan() != null &&
					(pl.getClan().getWars().isEmpty() || allPlayers.containsKey(pl.getClan().getName()))))
			{
				continue;
			}

			if (isOlympiadPoints && Olympiad.getInstance().getNobleInfo(pl.getObjectId()) == null)
			{
				continue;
			}

			allPlayers.put(isClanReputation || isClanWarPoints ? pl.getClan().getName() : pl.getName(),
					(long) (isOlympiadPoints ? Olympiad.getInstance().getNobleInfo(pl.getObjectId()).getPoints() :
							isClanWarPoints ? getWarPoints(pl.getClan()) : isRaidPoints ?
									RaidBossPointsManager.getInstance().getPointsByOwnerId(pl.getObjectId()) :
									isClanReputation ? pl.getClan().getReputationScore() :
											isReputation ? pl.getReputation() : isFameRanking ? pl.getFame() :
													isPvPRanking ? pl.getPvpKills() : pl.getPkKills()));
		}

		allPlayers = sortByValue(allPlayers, false);

		StringBuilder sb = new StringBuilder();
		sb.append(
				"<table bgcolor=999999 width=750><tr><td FIXWIDTH=40>Position #</td><td FIXWIDTH=100>Name</td><td FIXWIDTH=70>Count</td></tr></table>");

		String name = null;
		int counter = 1;
		for (Entry<Object, Long> info : allPlayers.entrySet())
		{
			name = (String) info.getKey();

			if (counter <= 20)
			{
				if (!playerIsInTop && name.equalsIgnoreCase(whatCheckInTop))
				{
					playerIsInTop = true;
					sb.append("<table width=750><tr><td FIXWIDTH=40><font color=LEVEL>" + counter +
							"</font></td><td FIXWIDTH=100><font color=LEVEL>" + name +
							"</font></td><td FIXWIDTH=70><font color=LEVEL>" + info.getValue() +
							"</font></td></tr></table>");
				}
				else
				{
					sb.append("<table width=750><tr><td FIXWIDTH=40>" + counter + "</td><td FIXWIDTH=100>" + name +
							"</td><td FIXWIDTH=70>" + info.getValue() + "</td></tr></table>");
				}

				sb.append(
						"<table width=750><tr><td><img src=\"L2UI.Squaregray\" width=750 height=1></td></tr></table>");
			}
			else if (playerIsInTop)
			{
				break;
			}
			else
			{
				if (name.equalsIgnoreCase(whatCheckInTop))
				{
					break;
				}
			}
			counter++;
		}

		if (!playerIsInTop)
		{
			sb.append("<br><table width=750><tr><td>Your position:</td></tr></table>");
			sb.append("<table width=750><tr><td><img src=\"L2UI.Squaregray\" width=750 height=1></td></tr></table>");
			if (counter <= allPlayers.size()) //Player are on the list
			{
				sb.append("<table width=750><tr><td FIXWIDTH=40><font color=LEVEL>" + counter +
						"</font></td><td FIXWIDTH=100><font color=LEVEL>" + name +
						"</font></td><td FIXWIDTH=70><font color=LEVEL>" + allPlayers.get(name) +
						"</font></td></tr></table>");
			}
			else
			{
				sb.append("<table width=750><tr><td>You're not on the ranking!</td><td></td><td></td></tr></table>");
			}
			sb.append("<table width=750><tr><td><img src=\"L2UI.Squaregray\" width=750 height=1></td></tr></table>");
		}
		return sb.toString();
	}

	private int getWarPoints(L2Clan clan)
	{
		int total = 0;
		for (ClanWar war : clan.getWars())
		{
			if (war == null)
			{
				continue;
			}

			if (clan == war.getClan1())
			{
				total += war.getClan1Scores();
			}
			else if (clan == war.getClan2())
			{
				total += war.getClan2Scores();
			}
		}
		return total;
	}

	private Map<Object, Long> sortByValue(Map<Object, Long> unsortMap, final boolean ascending)
	{
		final List<Entry<Object, Long>> list = new LinkedList<>(unsortMap.entrySet());
		Collections.sort(list, (e1, e2) ->
		{
			if (ascending)
			{
				return e1.getValue().compareTo(e2.getValue());
			}
			else
			{
				return e2.getValue().compareTo(e1.getValue());
			}
		});

		final Map<Object, Long> sortedMap = new LinkedHashMap<>();
		for (Entry<Object, Long> entry : list)
		{
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	private CustomCommunityBoard()
	{
		loadRaidData();
	}

	public static CustomCommunityBoard getInstance()
	{
		return SingletonHolder._instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CustomCommunityBoard _instance = new CustomCommunityBoard();
	}
}
