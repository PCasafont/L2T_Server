/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.idfactory;

import gnu.trove.TIntArrayList;
import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.ThreadPoolManager;
import l2server.log.Log;

import java.sql.*;

/**
 * This class ...
 *
 * @version $Revision: 1.3.2.1.2.7 $ $Date: 2005/04/11 10:06:12 $
 */
public abstract class IdFactory
{

	protected static final String[] ID_CHECKS = {
			"SELECT owner_id	FROM items				 WHERE object_id >= ?   AND object_id < ?",
			"SELECT object_id   FROM items				 WHERE object_id >= ?   AND object_id < ?",
			"SELECT charId	 FROM character_quests	  WHERE charId >= ?	 AND charId < ?",
			"SELECT charId     FROM character_contacts    WHERE charId >= ?     AND charId < ?",
			"SELECT contactId  FROM character_contacts    WHERE contactId >= ?  AND contactId < ?",
			"SELECT charId	 FROM character_friends	 WHERE charId >= ?	 AND charId < ?",
			"SELECT charId	 FROM character_friends	 WHERE friendId >= ?   AND friendId < ?",
			"SELECT charId	 FROM character_hennas	  WHERE charId >= ? AND charId < ?",
			"SELECT charId	 FROM character_recipebook  WHERE charId >= ?	 AND charId < ?",
			"SELECT charId	 FROM character_recipeshoplist  WHERE charId >= ?	 AND charId < ?",
			"SELECT charId	 FROM character_shortcuts   WHERE charId >= ? AND charId < ?",
			"SELECT charId	 FROM character_macroses	WHERE charId >= ? AND charId < ?",
			"SELECT charId	 FROM character_skills	  WHERE charId >= ? AND charId < ?",
			"SELECT charId	 FROM character_skills_save WHERE charId >= ? AND charId < ?",
			"SELECT charId	 FROM character_subclasses  WHERE charId >= ? AND charId < ?",
			"SELECT charId	 FROM character_ui_actions  WHERE charId >= ? AND charId < ?",
			"SELECT charId	 FROM character_ui_categories  WHERE charId >= ? AND charId < ?",
			"SELECT charId	  FROM characters			WHERE charId >= ?	  AND charId < ?",
			"SELECT clanid	  FROM characters			WHERE clanid >= ?	  AND clanid < ?",
			"SELECT clan_id	 FROM clan_data			 WHERE clan_id >= ?	 AND clan_id < ?",
			"SELECT clan_id	 FROM siege_clans		   WHERE clan_id >= ?	 AND clan_id < ?",
			"SELECT ally_id	 FROM clan_data			 WHERE ally_id >= ?	 AND ally_id < ?",
			"SELECT leader_id   FROM clan_data			 WHERE leader_id >= ?   AND leader_id < ?",
			"SELECT item_obj_id FROM pets				  WHERE item_obj_id >= ? AND item_obj_id < ?",
			"SELECT object_id   FROM itemsonground		WHERE object_id >= ?   AND object_id < ?"
	};

	private static final String[] TIMESTAMPS_CLEAN = {
			"DELETE FROM character_instance_time WHERE time <= ?",
			"DELETE FROM character_skills_save WHERE restore_type = 1 AND systime <= ?"
	};

	protected boolean _initialized;

	public static final int FIRST_OID = 0x10000000;
	public static final int LAST_OID = 0x7FFFFFFF;
	public static final int FREE_OBJECT_ID_SIZE = LAST_OID - FIRST_OID;

	protected static final IdFactory _instance;

	protected IdFactory()
	{
		ThreadPoolManager.getInstance().executeTask(this::setAllCharacterOffline);

		cleanUpDB();
		cleanUpTimeStamps();
	}

	static
	{
		switch (Config.IDFACTORY_TYPE)
		{
			case Compaction:
				throw new UnsupportedOperationException("Compaction IdFactory is disabled.");
				//_instance = new CompactionIDFactory();
				//break;
			case BitSet:
				_instance = new BitSetIDFactory();
				break;
			case Stack:
				_instance = new StackIDFactory();
				break;
			default:
				_instance = null;
				break;
		}
	}

	/**
	 * Sets all character offline
	 */
	private void setAllCharacterOffline()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			Statement statement = con.createStatement();
			statement.executeUpdate("UPDATE characters SET online = 0");
			statement.close();

			Log.info("Updated characters online status.");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Cleans up Database
	 */
	private void cleanUpDB()
	{
		Connection con = null;
		Statement stmt = null;
		try
		{
			long cleanupStart = System.currentTimeMillis();
			int cleanCount = 0;
			con = L2DatabaseFactory.getInstance().getConnection();
			stmt = con.createStatement();
			// Misc/Account Related
			// Please read the descriptions above each before uncommenting them. If you are still
			// unsure of what exactly it does, leave it commented out. This is for those who know
			// what they are doing. :)

			// Deletes only accounts that HAVE been logged into and have no characters associated
			// with the account.
			// cleanCount +=
			// stmt.executeUpdate("DELETE FROM accounts WHERE accounts.lastactive > 0 AND accounts.login NOT IN (SELECT account_name FROM characters);");

			// Deletes any accounts that don't have characters. Whether or not the player has ever
			// logged into the account.
			// cleanCount +=
			// stmt.executeUpdate("DELETE FROM accounts WHERE accounts.login NOT IN (SELECT account_name FROM characters);");

			// Deletes banned accounts that have not been logged into for xx amount of days
			// (specified at the end of the script, default is set to 90 days). This prevents
			// accounts from being deleted that were accidentally or temporarily banned.
			// cleanCount +=
			// stmt.executeUpdate("DELETE FROM accounts WHERE accounts.accessLevel < 0 AND DATEDIFF(CURRENT_DATE( ) , FROM_UNIXTIME(`lastactive`/1000)) > 90;");
			// cleanCount +=
			// stmt.executeUpdate("DELETE FROM characters WHERE characters.account_name NOT IN (SELECT login FROM accounts);");

			// If the clan does not exist...
			cleanCount += stmt.executeUpdate(
					"DELETE FROM clanhall_functions WHERE clanhall_functions.hall_id NOT IN (SELECT id FROM clanhall WHERE ownerId <> 0);");
			// Untested, leaving commented out until confirmation that it's safe/works properly. Was
			// initially removed because of a bug. Search for idfactory.java changes in the trac for
			// further info.
			// cleanCount +=
			// stmt.executeUpdate("DELETE FROM clanhall_auction WHERE auction.id IN (SELECT id FROM clanhall WHERE ownerId <> 0) AND auction.sellerId=0;");
			// cleanCount +=
			// stmt.executeUpdate("DELETE FROM clanhall_auction_bid WHERE auctionId NOT IN (SELECT id FROM auction)");

			// Forum Related
			cleanCount += stmt.executeUpdate(
					"DELETE FROM forums WHERE forums.forum_owner_id NOT IN (SELECT clan_id FROM clan_data) AND forums.forum_parent=2;");
			cleanCount += stmt.executeUpdate(
					"DELETE FROM forums WHERE forums.forum_owner_id NOT IN (SELECT charId FROM characters) AND forums.forum_parent=3;");
			cleanCount += stmt.executeUpdate(
					"DELETE FROM posts WHERE posts.post_forum_id NOT IN (SELECT forum_id FROM forums);");
			cleanCount += stmt.executeUpdate(
					"DELETE FROM topic WHERE topic.topic_forum_id NOT IN (SELECT forum_id FROM forums);");

			// Update needed items after cleaning has taken place.
			stmt.executeUpdate(
					"UPDATE clan_data SET auction_bid_at = 0 WHERE auction_bid_at NOT IN (SELECT auctionId FROM clanhall_auction_bid);");
			stmt.executeUpdate(
					"UPDATE clan_subpledges SET leader_id=0 WHERE clan_subpledges.leader_id NOT IN (SELECT charId FROM characters) AND leader_id > 0;");
			stmt.executeUpdate(
					"UPDATE castle SET taxpercent=0 WHERE castle.id NOT IN (SELECT hasCastle FROM clan_data);");
			stmt.executeUpdate(
					"UPDATE characters SET clanid=0, clan_privs=0, wantspeace=0, subpledge=0, lvl_joined_academy=0, apprentice=0, sponsor=0, clan_join_expiry_time=0, clan_create_expiry_time=0 WHERE characters.clanid > 0 AND characters.clanid NOT IN (SELECT clan_id FROM clan_data);");
			stmt.executeUpdate(
					"UPDATE clanhall SET ownerId=0, paidUntil=0, paid=0 WHERE clanhall.ownerId NOT IN (SELECT clan_id FROM clan_data);");
			stmt.executeUpdate("UPDATE fort SET owner=0 WHERE owner NOT IN (SELECT clan_id FROM clan_data);");

			// FIXME: This query takes long when there are many clans
			stmt.executeUpdate(
					"UPDATE clanhall set ownerId=0, paidUntil=0, paid=0 WHERE ownerId IN (SELECT clanid FROM characters WHERE DATEDIFF(CURRENT_DATE( ) , FROM_UNIXTIME(`lastaccess`/1000)) >= 60 AND charId IN (SELECT leader_id FROM clan_data));");

			long time = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L;
			cleanCount += stmt.executeUpdate("DELETE FROM log_chat WHERE time <= " + time);
			cleanCount += stmt.executeUpdate("DELETE FROM log_enchant_skills WHERE time <= " + time);
			cleanCount += stmt.executeUpdate("DELETE FROM log_enchants WHERE time <= " + time);
			cleanCount += stmt.executeUpdate("DELETE FROM log_items WHERE time <= " + time);
			cleanCount += stmt.executeUpdate("DELETE FROM log_olys WHERE time <= " + time);
			//cleanCount += stmt.executeUpdate("DELETE FROM log_damage WHERE time <= " + time);
			cleanCount += stmt.executeUpdate("DELETE FROM gm_audit WHERE time <= " + time / 1000);
			Log.info("Cleaned " + cleanCount + " elements from database in " +
					(System.currentTimeMillis() - cleanupStart) / 1000 + " s");
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
	}

	private void cleanUpTimeStamps()
	{
		Connection con = null;
		PreparedStatement stmt = null;
		try
		{
			int cleanCount = 0;
			con = L2DatabaseFactory.getInstance().getConnection();
			for (String line : TIMESTAMPS_CLEAN)
			{
				stmt = con.prepareStatement(line);
				stmt.setLong(1, System.currentTimeMillis());
				cleanCount += stmt.executeUpdate();
				stmt.close();
			}

			Log.info("Cleaned " + cleanCount + " expired timestamps from database.");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	protected final int[] extractUsedObjectIDTable() throws Exception
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			Statement statement = null;
			ResultSet rset = null;

			statement = con.createStatement();
			final TIntArrayList temp = new TIntArrayList();

			rset = statement.executeQuery("SELECT COUNT(*) FROM characters");
			rset.next();
			temp.ensureCapacity(rset.getInt(1));
			rset = statement.executeQuery("SELECT charId FROM characters");
			while (rset.next())
			{
				temp.add(rset.getInt(1));
			}

			rset = statement.executeQuery("SELECT COUNT(*) FROM items");
			rset.next();
			temp.ensureCapacity(temp.size() + rset.getInt(1));
			rset = statement.executeQuery("SELECT object_id FROM items");
			while (rset.next())
			{
				temp.add(rset.getInt(1));
			}

			rset = statement.executeQuery("SELECT COUNT(*) FROM clan_data");
			rset.next();
			temp.ensureCapacity(temp.size() + rset.getInt(1));
			rset = statement.executeQuery("SELECT clan_id FROM clan_data");
			while (rset.next())
			{
				temp.add(rset.getInt(1));
			}

			rset = statement.executeQuery("SELECT COUNT(*) FROM itemsonground");
			rset.next();
			temp.ensureCapacity(temp.size() + rset.getInt(1));
			rset = statement.executeQuery("SELECT object_id FROM itemsonground");
			while (rset.next())
			{
				temp.add(rset.getInt(1));
			}

			rset = statement.executeQuery("SELECT COUNT(*) FROM messages");
			rset.next();
			temp.ensureCapacity(temp.size() + rset.getInt(1));
			rset = statement.executeQuery("SELECT messageId FROM messages");
			while (rset.next())
			{
				temp.add(rset.getInt(1));
			}

			rset = statement.executeQuery("SELECT COUNT(*) FROM custom_auctions");
			rset.next();
			temp.ensureCapacity(temp.size() + rset.getInt(1));
			rset = statement.executeQuery("SELECT id FROM custom_auctions");
			while (rset.next())
			{
				temp.add(rset.getInt(1));
			}

			temp.sort();

			return temp.toNativeArray();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public boolean isInitialized()
	{
		return _initialized;
	}

	public static IdFactory getInstance()
	{
		return _instance;
	}

	public abstract int getNextId();

	/**
	 * return a used Object ID back to the pool
	 */
	public abstract void releaseId(int id);

	public abstract int size();
}
