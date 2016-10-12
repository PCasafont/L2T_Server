/*
 * Copyright (C) 2004-2013 L2J Server
 *
 * This file is part of L2J Server.
 *
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.communitybbs.Manager;

import l2server.L2DatabaseFactory;
import l2server.gameserver.communitybbs.BB.Forum;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ForumsBBSManager extends BaseBBSManager
{

	private final List<Forum> _table;
	private int _lastid = 1;

	/**
	 * Instantiates a new forums bbs manager.
	 */
	private ForumsBBSManager()
	{
		_table = new ArrayList<>();

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT forum_id FROM forums WHERE forum_type=0");
			ResultSet result = statement.executeQuery();
			while (result.next())
			{
				int forumId = result.getInt("forum_id");
				Forum f = new Forum(forumId, null);
				addForum(f);
			}
			result.close();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Data error on Forum (root): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Inits the root.
	 */
	public void initRoot()
	{
		List<Forum> copy = new ArrayList<>(_table);
		for (Forum f : copy)
		{
			f.vload();
		}
		Log.info("Loaded " + _table.size() + " forums. Last forum id used: " + _lastid);
	}

	/**
	 * Adds the forum.
	 *
	 * @param ff the forum
	 */
	public void addForum(Forum ff)
	{
		if (ff == null)
		{
			return;
		}

		_table.add(ff);

		if (ff.getID() > _lastid)
		{
			_lastid = ff.getID();
		}
	}

	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
	}

	/**
	 * Gets the forum by name.
	 *
	 * @param name the forum name
	 * @return the forum by name
	 */
	public Forum getForumByName(String name)
	{
		for (Forum f : _table)
		{
			if (f.getName().equals(name))
			{
				return f;
			}
		}
		return null;
	}

	/**
	 * Creates the new forum.
	 *
	 * @param name   the forum name
	 * @param parent the parent forum
	 * @param type   the forum type
	 * @param perm   the perm
	 * @param oid    the oid
	 * @return the new forum
	 */
	public Forum createNewForum(String name, Forum parent, int type, int perm, int oid)
	{
		Forum forum = new Forum(name, parent, type, perm, oid);
		forum.insertIntoDb();
		return forum;
	}

	/**
	 * Gets the a new Id.
	 *
	 * @return the a new Id
	 */
	public int getANewID()
	{
		return ++_lastid;
	}

	/**
	 * Gets the forum by Id.
	 *
	 * @param idf the the forum Id
	 * @return the forum by Id
	 */
	public Forum getForumByID(int idf)
	{
		for (Forum f : _table)
		{
			if (f.getID() == idf)
			{
				return f;
			}
		}
		return null;
	}

	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{

	}

	/**
	 * Gets the single instance of ForumsBBSManager.
	 *
	 * @return single instance of ForumsBBSManager
	 */
	public static ForumsBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final ForumsBBSManager _instance = new ForumsBBSManager();
	}
}
