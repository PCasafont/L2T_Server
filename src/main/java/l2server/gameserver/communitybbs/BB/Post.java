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
import l2server.gameserver.communitybbs.Manager.PostBBSManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Maktakien
 */
public class Post {
	private static Logger log = LoggerFactory.getLogger(Post.class.getName());



	public static class CPost {
		public int postId;
		public String postOwner;
		public int postOwnerId;
		public long postDate;
		public int postTopicId;
		public int postForumId;
		public String postTxt;
	}

	private List<CPost> post;

	//public enum ConstructorType {REPLY, CREATE };
	public Post(String PostOwner, int PostOwnerID, long date, int tid, int PostForumID, String txt) {
		post = new ArrayList<>();
		CPost cp = new CPost();
		cp.postId = 0;
		cp.postOwner = PostOwner;
		cp.postOwnerId = PostOwnerID;
		cp.postDate = date;
		cp.postTopicId = tid;
		cp.postForumId = PostForumID;
		cp.postTxt = txt;
		post.add(cp);
		insertindb(cp);
	}

	public void insertindb(CPost cp) {
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"INSERT INTO posts (post_id,post_owner_name,post_ownerid,post_date,post_topic_id,post_forum_id,post_txt) VALUES (?,?,?,?,?,?,?)");
			statement.setInt(1, cp.postId);
			statement.setString(2, cp.postOwner);
			statement.setInt(3, cp.postOwnerId);
			statement.setLong(4, cp.postDate);
			statement.setInt(5, cp.postTopicId);
			statement.setInt(6, cp.postForumId);
			statement.setString(7, cp.postTxt);
			statement.execute();
			statement.close();
		} catch (Exception e) {
			log.warn("Error while saving new Post to db " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	public Post(Topic t) {
		post = new ArrayList<>();
		load(t);
	}

	public CPost getCPost(int id) {
		int i = 0;
		for (CPost cp : post) {
			if (i++ == id) {
				return cp;
			}
		}
		return null;
	}

	public void deleteme(Topic t) {
		PostBBSManager.getInstance().delPostByTopic(t);
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM posts WHERE post_forum_id=? AND post_topic_id=?");
			statement.setInt(1, t.getForumID());
			statement.setInt(2, t.getID());
			statement.execute();
			statement.close();
		} catch (Exception e) {
			log.warn("Error while deleting post: " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	private void load(Topic t) {
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM posts WHERE post_forum_id=? AND post_topic_id=? ORDER BY post_id ASC");
			statement.setInt(1, t.getForumID());
			statement.setInt(2, t.getID());
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				CPost cp = new CPost();
				cp.postId = result.getInt("post_id");
				cp.postOwner = result.getString("post_owner_name");
				cp.postOwnerId = result.getInt("post_ownerid");
				cp.postDate = result.getLong("post_date");
				cp.postTopicId = result.getInt("post_topic_id");
				cp.postForumId = result.getInt("post_forum_id");
				cp.postTxt = result.getString("post_txt");
				post.add(cp);
			}
			result.close();
			statement.close();
		} catch (Exception e) {
			log.warn("Data error on Post " + t.getForumID() + "/" + t.getID() + " : " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	public void updatetxt(int i) {
		Connection con = null;
		try {
			CPost cp = getCPost(i);
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE posts SET post_txt=? WHERE post_id=? AND post_topic_id=? AND post_forum_id=?");
			statement.setString(1, cp.postTxt);
			statement.setInt(2, cp.postId);
			statement.setInt(3, cp.postTopicId);
			statement.setInt(4, cp.postForumId);
			statement.execute();
			statement.close();
		} catch (Exception e) {
			log.warn("Error while saving new Post to db " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}
}
