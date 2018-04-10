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

import l2server.DatabasePool;
import l2server.gameserver.communitybbs.Manager.TopicBBSManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class Topic {
	private static Logger log = LoggerFactory.getLogger(Topic.class.getName());

	public static final int MORMAL = 0;
	public static final int MEMO = 1;

	private int id;
	private int forumId;
	private String topicName;
	private long date;
	private String ownerName;
	private int ownerId;
	private int type;
	private int cReply;

	public Topic(ConstructorType ct, int id, int fid, String name, long date, String oname, int oid, int type, int Creply) {
		this.id = id;
		forumId = fid;
		topicName = name;
		this.date = date;
		ownerName = oname;
		ownerId = oid;
		this.type = type;
		cReply = Creply;
		TopicBBSManager.getInstance().addTopic(this);

		if (ct == ConstructorType.CREATE) {

			insertindb();
		}
	}

	/**
	 *
	 */
	public void insertindb() {
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"INSERT INTO topic (topic_id,topic_forum_id,topic_name,topic_date,topic_ownername,topic_ownerid,topic_type,topic_reply) VALUES (?,?,?,?,?,?,?,?)");
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
		} catch (Exception e) {
			log.warn("Error while saving new Topic to db " + e.getMessage(), e);
		} finally {
			DatabasePool.close(con);
		}
	}

	public enum ConstructorType {
		RESTORE,
		CREATE
	}

	public int getID() {
		return id;
	}

	public int getForumID() {
		return forumId;
	}

	public String getName() {
		return topicName;
	}

	public String getOwnerName() {
		return ownerName;
	}

	/**
	 *
	 */
	public void deleteme(Forum f) {
		TopicBBSManager.getInstance().delTopic(this);
		f.rmTopicByID(getID());
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM topic WHERE topic_id=? AND topic_forum_id=?");
			statement.setInt(1, getID());
			statement.setInt(2, f.getID());
			statement.execute();
			statement.close();
		} catch (Exception e) {
			log.warn("Error while deleting topic: " + e.getMessage(), e);
		} finally {
			DatabasePool.close(con);
		}
	}

	public long getDate() {
		return date;
	}
}
