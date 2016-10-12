/*
 * $Header: BlockList.java, 21/11/2005 14:53:53 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 21/11/2005 14:53:53 $
 * $Revision: 1 $
 * $Log: BlockList.java,v $
 * Revision 1  21/11/2005 14:53:53  luisantonioa
 * Added copyright notice
 *
 *
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

import l2server.L2DatabaseFactory;
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.BlockListPacket;
import l2server.gameserver.network.serverpackets.ExBlockAddResult;
import l2server.gameserver.network.serverpackets.ExBlockRemoveResult;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * This class ...
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class BlockList
{

	private static Map<Integer, List<Integer>> _offlineList = new HashMap<>();

	private final L2PcInstance _owner;
	private List<Integer> _blockList;

	public BlockList(L2PcInstance owner)
	{
		_owner = owner;
		_blockList = _offlineList.get(owner.getObjectId());
		if (_blockList == null)
		{
			_blockList = loadList(_owner.getObjectId());
		}
	}

	private synchronized void addToBlockList(int target)
	{
		_blockList.add(target);
		updateInDB(target, true);
		_owner.sendPacket(new ExBlockAddResult(target));
	}

	private synchronized void removeFromBlockList(int target)
	{
		_blockList.remove(Integer.valueOf(target));
		updateInDB(target, false);
		_owner.sendPacket(new ExBlockRemoveResult(target));
	}

	public void playerLogout()
	{
		_offlineList.put(_owner.getObjectId(), _blockList);
	}

	private static List<Integer> loadList(int ObjId)
	{
		Connection con = null;
		List<Integer> list = new ArrayList<>();

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("SELECT friendId, memo FROM character_friends WHERE charId=? AND relation=1");
			statement.setInt(1, ObjId);
			ResultSet rset = statement.executeQuery();

			int friendId;
			while (rset.next())
			{
				friendId = rset.getInt("friendId");
				if (friendId == ObjId)
				{
					continue;
				}
				list.add(friendId);
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Error found in " + ObjId + " FriendList while loading BlockList: " + e.getMessage(),
					e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return list;
	}

	private void updateInDB(int targetId, boolean state)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			if (state) //add
			{
				statement = con.prepareStatement(
						"INSERT INTO character_friends (charId, friendId, relation) VALUES (?, ?, 1)");
				statement.setInt(1, _owner.getObjectId());
				statement.setInt(2, targetId);
			}
			else
			//remove
			{
				statement = con.prepareStatement(
						"DELETE FROM character_friends WHERE charId=? AND friendId=? AND relation=1");
				statement.setInt(1, _owner.getObjectId());
				statement.setInt(2, targetId);
			}
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Could not add block player: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public boolean isInBlockList(L2PcInstance target)
	{
		return _blockList.contains(target.getObjectId());
	}

	public boolean isInBlockList(int targetId)
	{
		return _blockList.contains(targetId);
	}

	private boolean isBlockAll()
	{
		return _owner.getMessageRefusal();
	}

	public static boolean isBlocked(L2PcInstance listOwner, L2PcInstance target)
	{
		BlockList blockList = listOwner.getBlockList();
		return blockList.isBlockAll() || blockList.isInBlockList(target);
	}

	public static boolean isBlocked(L2PcInstance listOwner, int targetId)
	{
		BlockList blockList = listOwner.getBlockList();
		return blockList.isBlockAll() || blockList.isInBlockList(targetId);
	}

	private void setBlockAll(boolean state)
	{
		_owner.setMessageRefusal(state);
	}

	public List<Integer> getBlockList()
	{
		return _blockList;
	}

	public static void addToBlockList(L2PcInstance listOwner, int targetId)
	{
		if (listOwner == null)
		{
			return;
		}

		String charName = CharNameTable.getInstance().getNameById(targetId);

		if (listOwner.getFriendList().contains(targetId))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST);
			sm.addString(charName);
			listOwner.sendPacket(sm);
			return;
		}

		if (listOwner.getBlockList().getBlockList().contains(targetId))
		{
			listOwner.sendMessage("Already in ignore list.");
			return;
		}

		listOwner.getBlockList().addToBlockList(targetId);

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_WAS_ADDED_TO_YOUR_IGNORE_LIST);
		sm.addString(charName);
		listOwner.sendPacket(sm);
		listOwner.sendPacket(new ExBlockAddResult(targetId));
		listOwner.sendPacket(new BlockListPacket(listOwner));

		L2PcInstance player = L2World.getInstance().getPlayer(targetId);

		if (player != null)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
			sm.addString(listOwner.getName());
			player.sendPacket(sm);
		}
	}

	public static void removeFromBlockList(L2PcInstance listOwner, int targetId)
	{
		if (listOwner == null)
		{
			return;
		}

		SystemMessage sm;

		String charName = CharNameTable.getInstance().getNameById(targetId);

		if (!listOwner.getBlockList().getBlockList().contains(targetId))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT);
			listOwner.sendPacket(sm);
			return;
		}

		listOwner.getBlockList().removeFromBlockList(targetId);
		listOwner.sendPacket(new ExBlockRemoveResult(targetId));
		listOwner.sendPacket(new BlockListPacket(listOwner));

		sm = SystemMessage.getSystemMessage(SystemMessageId.S1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST);
		sm.addString(charName);
		listOwner.sendPacket(sm);
	}

	public static boolean isInBlockList(L2PcInstance listOwner, L2PcInstance target)
	{
		return listOwner.getBlockList().isInBlockList(target);
	}

	public boolean isBlockAll(L2PcInstance listOwner)
	{
		return listOwner.getBlockList().isBlockAll();
	}

	public static void setBlockAll(L2PcInstance listOwner, boolean newValue)
	{
		listOwner.getBlockList().setBlockAll(newValue);
	}

	/**
	 * @param ownerId  object id of owner block list
	 * @param targetId object id of potential blocked player
	 * @return true if blocked
	 */
	public static boolean isInBlockList(int ownerId, int targetId)
	{
		L2PcInstance player = L2World.getInstance().getPlayer(ownerId);
		if (player != null)
		{
			return BlockList.isBlocked(player, targetId);
		}
		if (!_offlineList.containsKey(ownerId))
		{
			_offlineList.put(ownerId, loadList(ownerId));
		}
		return _offlineList.get(ownerId).contains(targetId);
	}
}
