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

/*
  @author godson
 */

package l2server.gameserver.model.olympiad;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.datatables.PlayerClassTable;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.model.olympiad.HeroInfo.DiaryEntry;
import l2server.gameserver.model.olympiad.HeroInfo.FightInfo;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.InventoryUpdate;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.network.serverpackets.UserInfo;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.log.Log;
import l2server.util.StringUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

public class HeroesManager
{
	private static final String GET_HEROES =
			"SELECT heroes.charId, characters.char_name, heroes.class_id, heroes.count, heroes.played FROM heroes, characters WHERE characters.charId = heroes.charId AND heroes.played = 1";
	private static final String GET_ALL_HEROES =
			"SELECT heroes.charId, characters.char_name, heroes.class_id, heroes.count, heroes.played FROM heroes, characters WHERE characters.charId = heroes.charId";
	private static final String UPDATE_ALL = "UPDATE heroes SET played = 0";
	private static final String INSERT_HERO = "INSERT INTO heroes (charId, class_id, count, played) VALUES (?,?,?,?)";
	private static final String UPDATE_HERO = "UPDATE heroes SET count = ?, played = ? WHERE charId = ?";
	private static final String GET_CLAN_ALLY =
			"SELECT characters.clanid AS clanid, coalesce(clan_data.ally_Id, 0) AS allyId FROM characters LEFT JOIN clan_data ON clan_data.clan_id = characters.clanid WHERE characters.charId = ?";
	private static final String GET_CLAN_NAME =
			"SELECT clan_name FROM clan_data WHERE clan_id = (SELECT clanid FROM characters WHERE charId = ?)";
	// delete hero items
	private static final String DELETE_ITEMS =
			"DELETE FROM items WHERE item_id IN (6842, 6611, 6612, 6613, 6614, 6615, 6616, 6617, 6618, 6619, 6620, 6621, 9388, 9389, 9390) AND owner_id NOT IN (SELECT charId FROM characters WHERE accesslevel > 0)";
	private static final String DELETE_HERO = "DELETE FROM heroes WHERE charId = ?";

	private static Map<Integer, HeroInfo> _heroes = new HashMap<>();
	private static Map<Integer, HeroInfo> _pastAndCurrentHeroes = new HashMap<>();

	public static final String COUNT = "count";
	public static final String PLAYED = "played";
	public static final String CLAN_NAME = "clan_name";
	public static final String CLAN_CREST = "clan_crest";
	public static final String ALLY_NAME = "ally_name";
	public static final String ALLY_CREST = "ally_crest";

	public static final int ACTION_RAID_KILLED = 1;
	public static final int ACTION_HERO_GAINED = 2;
	public static final int ACTION_CASTLE_TAKEN = 3;

	public static HeroesManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private HeroesManager()
	{
		init();
	}

	private void init()
	{
		if (Config.IS_CLASSIC)
		{
			return;
		}

		Connection con = null;

		PreparedStatement statement;
		PreparedStatement statement2;

		ResultSet rset;
		ResultSet rset2;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(GET_HEROES);
			rset = statement.executeQuery();

			while (rset.next())
			{
				int charId = rset.getInt(Olympiad.CHAR_ID);
				HeroInfo hero = new HeroInfo(charId, rset.getString(Olympiad.CHAR_NAME), rset.getInt(Olympiad.CLASS_ID),
						rset.getInt(COUNT), rset.getBoolean(PLAYED));

				loadFights(hero);
				loadDiary(hero);
				loadMessage(hero);

				statement2 = con.prepareStatement(GET_CLAN_ALLY);
				statement2.setInt(1, charId);
				rset2 = statement2.executeQuery();

				if (rset2.next())
				{
					int clanId = rset2.getInt("clanid");
					int allyId = rset2.getInt("allyId");

					String clanName = "";
					String allyName = "";
					int clanCrest = 0;
					int allyCrest = 0;

					if (clanId > 0)
					{
						clanName = ClanTable.getInstance().getClan(clanId).getName();
						clanCrest = ClanTable.getInstance().getClan(clanId).getCrestId();

						if (allyId > 0)
						{
							allyName = ClanTable.getInstance().getClan(clanId).getAllyName();
							allyCrest = ClanTable.getInstance().getClan(clanId).getAllyCrestId();
						}
					}

					hero.setClanCrest(clanCrest);
					hero.setClanName(clanName);
					hero.setAllyCrest(allyCrest);
					hero.setAllyName(allyName);
				}

				rset2.close();
				statement2.close();

				_heroes.put(charId, hero);
			}

			rset.close();
			statement.close();

			statement = con.prepareStatement(GET_ALL_HEROES);
			rset = statement.executeQuery();

			while (rset.next())
			{
				int charId = rset.getInt(Olympiad.CHAR_ID);
				HeroInfo hero = new HeroInfo(charId, rset.getString(Olympiad.CHAR_NAME), rset.getInt(Olympiad.CLASS_ID),
						rset.getInt(COUNT), rset.getBoolean(PLAYED));

				statement2 = con.prepareStatement(GET_CLAN_ALLY);
				statement2.setInt(1, charId);
				rset2 = statement2.executeQuery();

				if (rset2.next())
				{
					int clanId = rset2.getInt("clanid");
					int allyId = rset2.getInt("allyId");

					String clanName = "";
					String allyName = "";
					int clanCrest = 0;
					int allyCrest = 0;

					if (clanId > 0)
					{
						clanName = ClanTable.getInstance().getClan(clanId).getName();
						clanCrest = ClanTable.getInstance().getClan(clanId).getCrestId();

						if (allyId > 0)
						{
							allyName = ClanTable.getInstance().getClan(clanId).getAllyName();
							allyCrest = ClanTable.getInstance().getClan(clanId).getAllyCrestId();
						}
					}

					hero.setClanCrest(clanCrest);
					hero.setClanName(clanName);
					hero.setAllyCrest(allyCrest);
					hero.setAllyName(allyName);
				}

				rset2.close();
				statement2.close();

				_pastAndCurrentHeroes.put(charId, hero);
			}

			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			Log.warning("Hero System: Couldnt load Heroes");
			if (Config.DEBUG)
			{
				Log.log(Level.WARNING, "", e);
			}
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		Log.info("Hero System: Loaded " + _heroes.size() + " Heroes.");
		Log.info("Hero System: Loaded " + _pastAndCurrentHeroes.size() + " all time Heroes.");
	}

	private String calcFightDuration(long FightTime)
	{
		String format = String.format("%%0%dd", 2);
		FightTime = FightTime / 1000;
		String seconds = String.format(format, FightTime % 60);
		String minutes = String.format(format, FightTime % 3600 / 60);
		return minutes + ":" + seconds;
	}

	/**
	 * Restore hero message from Db.
	 */
	public void loadMessage(HeroInfo hero)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT message FROM heroes WHERE charId=?");
			statement.setInt(1, hero.getId());
			ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				hero.setMessage(rset.getString("message"));
			}
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			Log.log(Level.WARNING, "Hero System: Couldn't load Hero Message for hero: " + hero.getName(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void loadDiary(HeroInfo hero)
	{
		int diaryentries = 0;
		Connection con = null;
		PreparedStatement statement;
		ResultSet rset;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM  heroes_diary WHERE charId=? ORDER BY time ASC");
			statement.setInt(1, hero.getId());
			rset = statement.executeQuery();

			while (rset.next())
			{
				DiaryEntry diaryentry = new DiaryEntry();
				diaryentry.time = rset.getLong("time");
				int action = rset.getInt("action");
				int param = rset.getInt("param");
				switch (action)
				{
					case ACTION_RAID_KILLED:
						L2NpcTemplate template = NpcTable.getInstance().getTemplate(param);
						if (template != null)
						{
							diaryentry.action = template.getName() + " was defeated";
						}
						break;
					case ACTION_HERO_GAINED:
						diaryentry.action = "Gained Hero status";
						break;
					case ACTION_CASTLE_TAKEN:
						Castle castle = CastleManager.getInstance().getCastleById(param);
						if (castle != null)
						{
							diaryentry.action = castle.getName() + " Castle was successfuly taken";
						}
						break;
				}

				hero.addDiaryEntry(diaryentry);
				diaryentries++;
			}
			rset.close();
			statement.close();

			Log.fine("Hero System: Loaded " + diaryentries + " diary entries for hero: " + hero.getName());
		}
		catch (Exception e)
		{
			Log.warning("Hero System: Couldnt load Hero Diary for hero: " + hero.getName());
			if (Config.DEBUG)
			{
				Log.log(Level.WARNING, "", e);
			}
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void loadFights(HeroInfo hero)
	{
		Calendar date = Calendar.getInstance();
		date.set(Calendar.DAY_OF_MONTH, 1);
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.MILLISECOND, 0);

		long from = date.getTimeInMillis();
		int numberOfFights = 0;
		int victories = 0;
		int defeats = 0;
		int draws = 0;

		Connection con = null;
		PreparedStatement statement;
		ResultSet rset;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(
					"SELECT * FROM olympiad_fights WHERE (charOneId=? OR charTwoId=?) AND start<? ORDER BY start ASC");
			statement.setInt(1, hero.getId());
			statement.setInt(2, hero.getId());
			statement.setLong(3, from);
			rset = statement.executeQuery();

			while (rset.next())
			{
				int charOneId = rset.getInt("charOneId");
				int charOneClass = rset.getInt("charOneClass");
				int charTwoId = rset.getInt("charTwoId");
				int charTwoClass = rset.getInt("charTwoClass");
				int winner = rset.getInt("winner");
				long start = rset.getLong("start");
				int time = rset.getInt("time");
				int classed = rset.getInt("classed");

				if (hero.getId() == charOneId)
				{
					String name = CharNameTable.getInstance().getNameById(charTwoId);
					String cls = PlayerClassTable.getInstance().getClassNameById(charTwoClass);
					if (name != null && cls != null)
					{
						FightInfo fight = new FightInfo();
						fight.opponent = name;
						fight.opponentClass = cls;

						fight.duration = calcFightDuration(time);
						fight.startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(start));

						fight.classed = classed == 1;
						if (winner == 1)
						{
							fight.result = "<font color=\"00ff00\">victory</font>";
							victories++;
						}
						else if (winner == 2)
						{
							fight.result = "<font color=\"ff0000\">loss</font>";
							defeats++;
						}
						else if (winner == 0)
						{
							fight.result = "<font color=\"ffff00\">draw</font>";
							draws++;
						}

						hero.addFight(fight);

						numberOfFights++;
					}
				}
				else if (hero.getId() == charTwoId)
				{
					String name = CharNameTable.getInstance().getNameById(charOneId);
					String cls = PlayerClassTable.getInstance().getClassNameById(charOneClass);
					if (name != null && cls != null)
					{
						FightInfo fight = new FightInfo();
						fight.opponent = name;
						fight.opponentClass = cls;

						fight.duration = calcFightDuration(time);
						fight.startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(start));

						fight.classed = classed == 1;
						if (winner == 1)
						{
							fight.result = "<font color=\"00ff00\">victory</font>";
							victories++;
						}
						else if (winner == 2)
						{
							fight.result = "<font color=\"ff0000\">loss</font>";
							defeats++;
						}
						else if (winner == 0)
						{
							fight.result = "<font color=\"ffff00\">draw</font>";
							draws++;
						}

						hero.addFight(fight);

						numberOfFights++;
					}
				}
			}
			rset.close();
			statement.close();

			hero.setVictories(victories);
			hero.setDefeats(defeats);
			hero.setDraws(draws);

			Log.fine("Hero System: Loaded " + numberOfFights + " fights for hero: " + hero.getName());
		}
		catch (SQLException e)
		{
			Log.warning("Hero System: Couldnt load Hero fights history for hero: " + hero.getName());
			if (Config.DEBUG)
			{
				Log.log(Level.WARNING, "", e);
			}
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public Map<Integer, HeroInfo> getHeroes()
	{
		return _heroes;
	}

	public int getHeroByClass(int classid)
	{
		for (HeroInfo hero : _heroes.values())
		{
			if (hero.getClassId() == classid)
			{
				return hero.getId();
			}
		}

		return 0;
	}

	public void showHeroDiary(L2PcInstance activeChar, int heroclass, int charId, int page)
	{
		final int perpage = 10;

		if (_heroes.containsKey(charId))
		{
			HeroInfo hero = _heroes.get(charId);
			NpcHtmlMessage diaryReply = new NpcHtmlMessage(5);
			final String htmContent =
					HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "olympiad/herodiary.htm");
			if (htmContent != null)
			{
				diaryReply.setHtml(htmContent);
				diaryReply.replace("%heroname%", CharNameTable.getInstance().getNameById(charId));
				diaryReply.replace("%message%", hero.getMessage());
				diaryReply.disableValidation();

				if (!hero.getDiary().isEmpty())
				{
					ArrayList<DiaryEntry> list = new ArrayList<>();
					list.addAll(hero.getDiary());
					Collections.reverse(list);

					boolean color = true;
					final StringBuilder fList = new StringBuilder(500);
					int counter = 0;
					int breakat = 0;
					for (int i = (page - 1) * perpage; i < list.size(); i++)
					{
						breakat = i;
						DiaryEntry diaryEntry = list.get(i);
						StringUtil.append(fList, "<tr><td>");
						if (color)
						{
							StringUtil.append(fList, "<table width=270 bgcolor=\"131210\">");
						}
						else
						{
							StringUtil.append(fList, "<table width=270>");
						}
						StringUtil.append(fList, "<tr><td width=270><font color=\"LEVEL\">" +
								new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(diaryEntry.time)) +
								"</font></td></tr>");
						StringUtil.append(fList, "<tr><td width=270>" + diaryEntry.action + "</td></tr>");
						StringUtil.append(fList, "<tr><td>&nbsp;</td></tr></table>");
						StringUtil.append(fList, "</td></tr>");
						color = !color;
						counter++;
						if (counter >= perpage)
						{
							break;
						}
					}

					if (breakat < list.size() - 1)
					{
						diaryReply.replace("%buttprev%",
								"<button value=\"Prev\" action=\"bypass _diary?class=" + heroclass + "&page=" +
										(page + 1) +
										"\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
					}
					else
					{
						diaryReply.replace("%buttprev%", "");
					}

					if (page > 1)
					{
						diaryReply.replace("%buttnext%",
								"<button value=\"Next\" action=\"bypass _diary?class=" + heroclass + "&page=" +
										(page - 1) +
										"\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
					}
					else
					{
						diaryReply.replace("%buttnext%", "");
					}

					diaryReply.replace("%list%", fList.toString());
				}
				else
				{
					diaryReply.replace("%list%", "");
					diaryReply.replace("%buttprev%", "");
					diaryReply.replace("%buttnext%", "");
				}

				activeChar.sendPacket(diaryReply);
			}
		}
	}

	public void showHeroFights(L2PcInstance activeChar, int heroclass, int charid, int page)
	{
		final int perpage = 20;
		if (_heroes.containsKey(charid))
		{
			HeroInfo hero = _heroes.get(charid);
			List<FightInfo> list = hero.getFights();

			NpcHtmlMessage FightReply = new NpcHtmlMessage(5);
			final String htmContent =
					HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "olympiad/herohistory.htm");
			if (htmContent != null)
			{
				FightReply.setHtml(htmContent);
				FightReply.replace("%heroname%", CharNameTable.getInstance().getNameById(charid));
				FightReply.disableValidation();

				if (!list.isEmpty())
				{
					boolean color = true;
					final StringBuilder fList = new StringBuilder(500);
					int counter = 0;
					int breakat = 0;
					for (int i = (page - 1) * perpage; i < list.size(); i++)
					{
						breakat = i;
						FightInfo fight = list.get(i);
						StringUtil.append(fList, "<tr><td>");
						if (color)
						{
							StringUtil.append(fList, "<table width=270 bgcolor=\"131210\">");
						}
						else
						{
							StringUtil.append(fList, "<table width=270>");
						}
						StringUtil.append(fList,
								"<tr><td width=220><font color=\"LEVEL\">" + fight.startTime + "</font>&nbsp;&nbsp;" +
										fight.result + "</td><td width=50 align=right>" +
										(fight.classed ? "<font color=\"FFFF99\">cls</font>" :
												"<font color=\"999999\">non-cls<font>") + "</td></tr>");
						StringUtil.append(fList, "<tr><td width=220>vs " + fight.opponent + " (" + fight.opponentClass +
								")</td><td width=50 align=right>(" + fight.duration + ")</td></tr>");
						StringUtil.append(fList, "<tr><td colspan=2>&nbsp;</td></tr></table>");
						StringUtil.append(fList, "</td></tr>");
						color = !color;
						counter++;
						if (counter >= perpage)
						{
							break;
						}
					}

					if (breakat < list.size() - 1)
					{
						FightReply.replace("%buttprev%",
								"<button value=\"Prev\" action=\"bypass _match?class=" + heroclass + "&page=" +
										(page + 1) +
										"\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
					}
					else
					{
						FightReply.replace("%buttprev%", "");
					}

					if (page > 1)
					{
						FightReply.replace("%buttnext%",
								"<button value=\"Next\" action=\"bypass _match?class=" + heroclass + "&page=" +
										(page - 1) +
										"\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
					}
					else
					{
						FightReply.replace("%buttnext%", "");
					}

					FightReply.replace("%list%", fList.toString());
				}
				else
				{
					FightReply.replace("%list%", "");
					FightReply.replace("%buttprev%", "");
					FightReply.replace("%buttnext%", "");
				}

				FightReply.replace("%win%", String.valueOf(hero.getVictories()));
				FightReply.replace("%draw%", String.valueOf(hero.getDraws()));
				FightReply.replace("%loos%", String.valueOf(hero.getDefeats()));

				activeChar.sendPacket(FightReply);
			}
		}
	}

	public synchronized void computeNewHeroes(Map<Integer, OlympiadNobleInfo> newHeroes)
	{
		updateHeroes(true);

		if (!_heroes.isEmpty())
		{
			for (HeroInfo hero : _heroes.values())
			{
				L2PcInstance player = L2World.getInstance().getPlayer(hero.getId());
				if (player == null)
				{
					continue;
				}

				try
				{
					player.setHero(false);

					for (int i = 0; i < Inventory.PAPERDOLL_TOTALSLOTS; i++)
					{
						L2ItemInstance equippedItem = player.getInventory().getPaperdollItem(i);
						if (equippedItem != null && equippedItem.isHeroItem())
						{
							player.getInventory().unEquipItemInSlot(i);
						}
					}

					for (L2ItemInstance item : player.getInventory().getAvailableItems(false, false))
					{
						if (item != null && item.isHeroItem())
						{
							player.destroyItem("Hero", item, null, true);
							InventoryUpdate iu = new InventoryUpdate();
							iu.addRemovedItem(item);
							player.sendPacket(iu);
						}
					}

					player.broadcastUserInfo();
				}
				catch (NullPointerException e)
				{
					e.printStackTrace();
				}
			}
		}

		if (newHeroes.isEmpty())
		{
			_heroes.clear();
			return;
		}

		Map<Integer, HeroInfo> heroes = new LinkedHashMap<>();

		for (int classId : newHeroes.keySet())
		{
			OlympiadNobleInfo heroNobleInfo = newHeroes.get(classId);
			int charId = heroNobleInfo.getId();

			if (_pastAndCurrentHeroes != null && _pastAndCurrentHeroes.containsKey(charId))
			{
				HeroInfo oldHero = _pastAndCurrentHeroes.get(charId);
				oldHero.increaseCount();
				oldHero.setPlayed(true);
				oldHero.setVictories(heroNobleInfo.getVictories());
				oldHero.setDefeats(heroNobleInfo.getDefeats());
				oldHero.setDraws(heroNobleInfo.getDraws());

				heroes.put(charId, oldHero);
			}
			else
			{
				heroes.put(charId, new HeroInfo(heroNobleInfo));
			}
		}

		deleteItemsInDb();

		_heroes.clear();
		_heroes.putAll(heroes);

		heroes.clear();

		updateHeroes(false);

		for (HeroInfo hero : _heroes.values())
		{
			// Set Gained hero and reload data
			setHeroGained(hero.getId());
			loadFights(hero);
			loadDiary(hero);
			hero.setMessage("");

			L2PcInstance player = L2World.getInstance().getPlayer(hero.getId());

			if (player != null)
			{
				player.setHero(true);
				L2Clan clan = player.getClan();
				if (clan != null)
				{
					clan.addReputationScore(Config.HERO_POINTS, true);
					SystemMessage sm = SystemMessage.getSystemMessage(
							SystemMessageId.CLAN_MEMBER_C1_BECAME_HERO_AND_GAINED_S2_REPUTATION_POINTS);
					sm.addString(CharNameTable.getInstance().getNameById(hero.getId()));
					sm.addNumber(Config.HERO_POINTS);
					clan.broadcastToOnlineMembers(sm);
				}
				player.sendPacket(new UserInfo(player));
				player.broadcastUserInfo();
			}
			else
			{
				Connection con = null;

				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement(GET_CLAN_NAME);
					statement.setInt(1, hero.getId());
					ResultSet rset = statement.executeQuery();
					if (rset.next())
					{
						String clanName = rset.getString("clan_name");
						if (clanName != null)
						{
							L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
							if (clan != null)
							{
								clan.addReputationScore(Config.HERO_POINTS, true);
								SystemMessage sm = SystemMessage.getSystemMessage(
										SystemMessageId.CLAN_MEMBER_C1_BECAME_HERO_AND_GAINED_S2_REPUTATION_POINTS);
								sm.addString(CharNameTable.getInstance().getNameById(hero.getId()));
								sm.addNumber(Config.HERO_POINTS);
								clan.broadcastToOnlineMembers(sm);
							}
						}
					}

					rset.close();
					statement.close();
				}
				catch (Exception e)
				{
					Log.warning("could not get clan name of player with objectId:" + hero.getId() + ": " + e);
				}
				finally
				{
					L2DatabaseFactory.close(con);
				}
			}
		}
	}

	public void updateHeroes(boolean setDefault)
	{
		//_herofights = new HashMap<Integer, List<StatsSet>>();
		//_herocounts = new HashMap<Integer, StatsSet>();

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			if (setDefault)
			{
				PreparedStatement statement = con.prepareStatement(UPDATE_ALL);
				statement.execute();
				statement.close();
			}
			else
			{
				PreparedStatement statement;

				for (HeroInfo hero : _heroes.values())
				{
					if (_pastAndCurrentHeroes == null || !_pastAndCurrentHeroes.containsKey(hero.getId()))
					{
						statement = con.prepareStatement(INSERT_HERO);
						statement.setInt(1, hero.getId());
						statement.setInt(2, hero.getClassId());
						statement.setInt(3, hero.getCount());
						statement.setBoolean(4, hero.getPlayed());
						statement.execute();

						Connection con2 = L2DatabaseFactory.getInstance().getConnection();
						PreparedStatement statement2 = con2.prepareStatement(GET_CLAN_ALLY);
						statement2.setInt(1, hero.getId());
						ResultSet rset2 = statement2.executeQuery();

						if (rset2.next())
						{
							int clanId = rset2.getInt("clanid");
							int allyId = rset2.getInt("allyId");

							String clanName = "";
							String allyName = "";
							int clanCrest = 0;
							int allyCrest = 0;

							if (clanId > 0)
							{
								clanName = ClanTable.getInstance().getClan(clanId).getName();
								clanCrest = ClanTable.getInstance().getClan(clanId).getCrestId();

								if (allyId > 0)
								{
									allyName = ClanTable.getInstance().getClan(clanId).getAllyName();
									allyCrest = ClanTable.getInstance().getClan(clanId).getAllyCrestId();
								}
							}

							hero.setClanCrest(clanCrest);
							hero.setClanName(clanName);
							hero.setAllyCrest(allyCrest);
							hero.setAllyName(allyName);
						}

						rset2.close();
						statement2.close();
						con2.close();

						_pastAndCurrentHeroes.put(hero.getId(), hero);
					}
					else
					{
						statement = con.prepareStatement(UPDATE_HERO);
						statement.setInt(1, hero.getCount());
						statement.setBoolean(2, hero.getPlayed());
						statement.setInt(3, hero.getId());
						statement.execute();
					}

					statement.close();
				}
			}
		}
		catch (SQLException e)
		{
			Log.warning("Hero System: Couldnt update Heroes");
			if (Config.DEBUG)
			{
				Log.log(Level.WARNING, "", e);
			}
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void setHeroGained(int charId)
	{
		if (_heroes.containsKey(charId))
		{
			DiaryEntry diaryentry = new DiaryEntry();
			diaryentry.time = System.currentTimeMillis();
			diaryentry.action = "Gained Hero status";

			_heroes.get(charId).addDiaryEntry(diaryentry);
			saveDiaryData(charId, ACTION_HERO_GAINED, 0);
		}
	}

	public void setRBkilled(int charId, int npcId)
	{
		L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
		if (_heroes.containsKey(charId) && template != null)
		{
			DiaryEntry diaryentry = new DiaryEntry();
			diaryentry.time = System.currentTimeMillis();
			diaryentry.action = template.getName() + " was defeated";

			_heroes.get(charId).addDiaryEntry(diaryentry);
			saveDiaryData(charId, ACTION_RAID_KILLED, npcId);
		}
	}

	public void setCastleTaken(int charId, int castleId)
	{
		Castle castle = CastleManager.getInstance().getCastleById(castleId);
		if (_heroes.containsKey(charId) && castle != null)
		{
			DiaryEntry diaryentry = new DiaryEntry();
			diaryentry.time = System.currentTimeMillis();
			diaryentry.action = castle.getName() + " Castle was successfuly taken";

			_heroes.get(charId).addDiaryEntry(diaryentry);
			saveDiaryData(charId, ACTION_CASTLE_TAKEN, castleId);
		}
	}

	public void saveDiaryData(int charId, int action, int param)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("INSERT INTO heroes_diary (charId, time, action, param) values(?,?,?,?)");
			statement.setInt(1, charId);
			statement.setLong(2, System.currentTimeMillis());
			statement.setInt(3, action);
			statement.setInt(4, param);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			if (Log.isLoggable(Level.SEVERE))
			{
				Log.log(Level.SEVERE, "SQL exception while saving DiaryData.", e);
			}
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void setHeroMessage(L2PcInstance player, String heroWords)
	{
		if (!_heroes.containsKey(player.getObjectId()))
		{
			return;
		}

		_heroes.get(player.getObjectId()).setMessage(heroWords);
	}

	/**
	 * Update hero message in database
	 *
	 * @param charId character objid
	 */
	public void saveHeroMessage(int charId)
	{
		if (!_heroes.containsKey(charId))
		{
			return;
		}

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE heroes SET message=? WHERE charId=?;");
			statement.setString(1, _heroes.get(charId).getMessage());
			statement.setInt(2, charId);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			Log.log(Level.SEVERE, "SQL exception while saving HeroMessage.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void deleteItemsInDb()
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_ITEMS);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			Log.log(Level.WARNING, "", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void removeHero(int heroId)
	{
		Connection con = null;

		_heroes.remove(heroId);
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_HERO);
			statement.setInt(1, heroId);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			Log.log(Level.WARNING, "", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Saving task for {@link HeroesManager}<BR>
	 * Save all hero messages to DB.
	 */
	public void shutdown()
	{
		for (int charId : _heroes.keySet())
		{
			saveHeroMessage(charId);
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final HeroesManager _instance = new HeroesManager();
	}
}
