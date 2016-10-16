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

package l2server.gameserver.communitybbs.BB;

import l2server.L2DatabaseFactory;
import l2server.gameserver.communitybbs.Manager.TopicBBSManager;
import l2server.log.Log;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;

public class Topic
{

	public static final int MORMAL = 0;
	public static final int MEMO = 1;

	private int id;
	private int forumId;
	private String topicName;
	@Getter private long date;
	@Getter private String ownerName;
	private int ownerId;
	private int type;
	private int cReply;

	/**
	 */
	public Topic(ConstructorType ct, int id, int fid, String name, long date, String oname, int oid, int type, int Creply)
	{
		this.id = id;
		forumId = fid;
		topicName = name;
		this.date = date;
		ownerName = oname;
		ownerId = oid;
		this.type = type;
		cReply = Creply;
		TopicBBSManager.getInstance().addTopic(this);

		if (ct == ConstructorType.CREATE)
		{

			insertindb();
		}
	}

	/**
	 *
	 */
	public void insertindb()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"INSERT INTO topic (topic_id,topic_forum_id,topic_name,topic_date,topic_ownername,topic_ownerid,topic_type,topic_reply) values (?,?,?,?,?,?,?,?)");
			statement.setInt(1, id);
			statement.setInt(2, forumId);
			statement.setString(3, topicName);
			statement.setLong(4, date);
			statement.setString(5, ownerName);
			statement.setInt(6, ownerId);
			statement.setInt(7, type);
			statement.setInt(8, cReply);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Error while saving new Topic to db " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public enum ConstructorType
	{
		RESTORE, CREATE
	}

	/**
	 * @return
	 */
	public int getID()
	{
		return id;
	}

	public int getForumID()
	{
		return forumId;
	}

	/**
	 * @return
	 */
	public String getName()
	{
		return topicName;
	}


	/**
	 *
	 */
	public void deleteme(Forum f)
	{
		TopicBBSManager.getInstance().delTopic(this);
		f.rmTopicByID(getID());
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("DELETE FROM topic WHERE topic_id=? AND topic_forum_id=?");
			statement.setInt(1, getID());
			statement.setInt(2, f.getID());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Error while deleting topic: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * @return
	 */
}
