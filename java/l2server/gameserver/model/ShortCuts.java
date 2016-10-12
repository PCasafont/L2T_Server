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

package l2server.gameserver.model;

import l2server.L2DatabaseFactory;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ShortCutInit;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:33 $
 */
public class ShortCuts
{
	private L2PcInstance _owner;
	private Map<Integer, Map<Integer, Map<Integer, L2ShortCut>>> _shortCuts = new TreeMap<>();

	private boolean _hasPresetForCurrentLevel;

	public ShortCuts(L2PcInstance owner)
	{
		_owner = owner;
	}

	public L2ShortCut[] getAllShortCuts()
	{
		if (!_shortCuts.containsKey(_owner.getClassIndex()))
		{
			_shortCuts.put(_owner.getClassIndex(), new HashMap<>());
		}

		if (!_shortCuts.get(_owner.getClassIndex()).containsKey(_owner.getGearGradeForCurrentLevel()))
		{
			_shortCuts.get(_owner.getClassIndex()).put(_owner.getGearGradeForCurrentLevel(), new HashMap<>());
		}

		Map<Integer, L2ShortCut> allShortcuts =
				_shortCuts.get(_owner.getClassIndex()).get(_owner.getGearGradeForCurrentLevel());
		return allShortcuts.values().toArray(new L2ShortCut[allShortcuts.size()]);
	}

	public L2ShortCut getShortCut(int slot, int page)
	{
		L2ShortCut sc =
				_shortCuts.get(_owner.getClassIndex()).get(_owner.getGearGradeForCurrentLevel()).get(slot + page * 12);

		// verify shortcut
		if (sc != null && sc.getType() == L2ShortCut.TYPE_ITEM)
		{
			if (_owner.getInventory().getItemByObjectId(sc.getId()) == null)
			{
				deleteShortCut(sc.getSlot(), sc.getPage());
				sc = null;
			}
		}

		return sc;
	}

	public synchronized void registerShortCut(L2ShortCut shortcut)
	{
		// verify shortcut
		if (shortcut.getType() == L2ShortCut.TYPE_ITEM)
		{
			L2ItemInstance item = _owner.getInventory().getItemByObjectId(shortcut.getId());
			if (item == null)
			{
				return;
			}
			if (item.isEtcItem())
			{
				shortcut.setSharedReuseGroup(item.getEtcItem().getSharedReuseGroup());
			}
		}
		if (shortcut.getType() == L2ShortCut.TYPE_SKILL)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(shortcut.getId(), shortcut.getLevel());
			shortcut.setSharedReuseGroup(skill.getReuseHashCode());
		}

		if (!_shortCuts.containsKey(_owner.getClassIndex()))
		{
			//_owner.sendMessage("Adding classIndex");
			_shortCuts.put(_owner.getClassIndex(), new HashMap<>());
		}

		if (!_shortCuts.get(_owner.getClassIndex()).containsKey(_owner.getGearGradeForCurrentLevel()))
		{
			//_owner.sendMessage("Adding levelRange");
			_shortCuts.get(_owner.getClassIndex()).put(_owner.getGearGradeForCurrentLevel(), new HashMap<>());
		}

		L2ShortCut oldShortCut = _shortCuts.get(_owner.getClassIndex()).get(_owner.getGearGradeForCurrentLevel())
				.put(shortcut.getSlot() + 12 * shortcut.getPage(), shortcut);

		registerShortCutInDb(shortcut, oldShortCut);
	}

	public void registerShortCutInDb(L2ShortCut shortcut, L2ShortCut oldShortCut)
	{
		registerShortCutInDb(shortcut, oldShortCut, -1);
	}

	public void registerShortCutInDb(L2ShortCut shortcut, L2ShortCut oldShortCut, int levelRange)
	{
		if (oldShortCut != null)
		{
			deleteShortCutFromDb(oldShortCut);
		}

		Connection con = null;

		if (levelRange == -1)
		{
			boolean hasPresetForCurrentLevel = false;

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(
						"SELECT charId FROM character_shortcuts WHERE charId=? AND class_index=? AND levelRange = ?");
				statement.setInt(1, _owner.getObjectId());
				statement.setInt(2, _owner.getClassIndex());
				statement.setInt(3, _owner.getGearGradeForCurrentLevel());

				ResultSet rset = statement.executeQuery();

				if (rset.next())
				{
					hasPresetForCurrentLevel = true;
				}

				rset.close();
				statement.close();
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING,
						"Could not check if the character had preset for current level " + e.getMessage(), e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}

			if (!hasPresetForCurrentLevel)
			{
				for (L2ShortCut sh : getAllShortCuts())
				{
					if (sh == null)
					{
						continue;
					}

					L2ShortCut shortcutCopy =
							new L2ShortCut(sh.getSlot(), sh.getPage(), sh.getType(), sh.getId(), sh.getLevel(),
									sh.getCharacterType());

					registerShortCutInDb(shortcutCopy, null, _owner.getGearGradeForCurrentLevel());
				}
			}
		}

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement(
					"REPLACE INTO character_shortcuts (charId,slot,page,type,shortcut_id,level,class_index,levelRange) values(?,?,?,?,?,?,?,?)");
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, shortcut.getSlot());
			statement.setInt(3, shortcut.getPage());
			statement.setInt(4, shortcut.getType());
			statement.setInt(5, shortcut.getId());
			statement.setInt(6, shortcut.getLevel());
			statement.setInt(7, _owner.getClassIndex());
			statement.setInt(8, _owner.getGearGradeForCurrentLevel());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Could not store character shortcut: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		if (!_shortCuts.containsKey(_owner.getClassIndex()))
		{
			_shortCuts.put(_owner.getClassIndex(), new HashMap<>());

			_owner.sendSysMessage("Adding classIndex");
		}

		if (!_shortCuts.get(_owner.getClassIndex()).containsKey(_owner.getGearGradeForCurrentLevel()))
		{
			_shortCuts.get(_owner.getClassIndex()).put(_owner.getGearGradeForCurrentLevel(), new HashMap<>());

			_owner.sendSysMessage("Adding LevelRange");
		}

		_shortCuts.get(_owner.getClassIndex()).get(_owner.getGearGradeForCurrentLevel())
				.put(shortcut.getSlot() + shortcut.getPage() * 12, shortcut);
	}

	/**
	 * @param slot
	 */
	public synchronized void deleteShortCut(int slot, int page)
	{
		if (_shortCuts.get(_owner.getClassIndex()) == null ||
				_shortCuts.get(_owner.getClassIndex()).get(_owner.getGearGradeForCurrentLevel()) == null)
		{
			return;
		}

		L2ShortCut old = _shortCuts.get(_owner.getClassIndex()).get(_owner.getGearGradeForCurrentLevel())
				.remove(slot + page * 12);

		_owner.sendSysMessage("Old Shortcut = " + old);
		if (old == null || _owner == null)
		{
			return;
		}

		deleteShortCutFromDb(old);
		_owner.sendPacket(new ShortCutInit(_owner));
	}

	public synchronized void deleteShortCutByObjectId(int objectId)
	{
		if (_shortCuts.get(_owner.getClassIndex()) == null ||
				_shortCuts.get(_owner.getClassIndex()).get(_owner.getGearGradeForCurrentLevel()) == null)
		{
			return;
		}

		try
		{
			L2ShortCut toRemove = null;

			for (L2ShortCut shortcut : _shortCuts.get(_owner.getClassIndex()).get(_owner.getGearGradeForCurrentLevel())
					.values())
			{
				if (shortcut == null)
				{
					continue;
				}
				if (shortcut.getType() == L2ShortCut.TYPE_ITEM && shortcut.getId() == objectId)
				{
					toRemove = shortcut;
					break;
				}
			}

			if (toRemove != null)
			{
				deleteShortCut(toRemove.getSlot(), toRemove.getPage());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @param shortcut
	 */
	private void deleteShortCutFromDb(L2ShortCut shortcut)
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement(
					"DELETE FROM character_shortcuts WHERE charId=? AND slot=? AND page=? AND class_index=? AND levelRange=? ");
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, shortcut.getSlot());
			statement.setInt(3, shortcut.getPage());
			statement.setInt(4, _owner.getClassIndex());
			statement.setInt(5, _owner.getGearGradeForCurrentLevel());

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Could not delete character shortcut: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void restore()
	{
		restore(_owner.getClassIndex(), _owner.getGearGradeForCurrentLevel(), true);
	}

	public void restore(final int classIndex, final int levelRange, boolean loadDefault)
	{
		_hasPresetForCurrentLevel =
				_shortCuts.containsKey(classIndex) && _shortCuts.get(classIndex).containsKey(levelRange) &&
						_shortCuts.get(classIndex).get(levelRange).values().size() != 0;

		//if (_hasPresetForCurrentLevel)
		//	_shortCuts.get(_owner.getClassIndex()).get(_owner.getGearGradeForCurrentLevel()).clear();
		//System.out.println("Shortcuts Size = " +  _shortCuts.get(0).get(8).values().size());
		//System.out.println("Shortcuts Size = " +  _shortCuts.get(0).get(10).values().size());

		Connection con = null;

		if (!_hasPresetForCurrentLevel)
		{
			//_owner.sendMessage("Loading Shortcuts from DB.");
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(
						"SELECT charId FROM character_shortcuts WHERE charId=? AND class_index=? AND levelRange = ?");
				statement.setInt(1, _owner.getObjectId());
				statement.setInt(2, classIndex);
				statement.setInt(3, levelRange);

				ResultSet rset = statement.executeQuery();

				if (rset.next())
				{
					_hasPresetForCurrentLevel = true;
				}

				if (!_shortCuts.containsKey(classIndex))
				{
					_shortCuts.put(classIndex, new HashMap<>());
				}

				if (!_shortCuts.get(classIndex).containsKey(levelRange))
				{
					_shortCuts.get(classIndex).put(levelRange, new HashMap<>());
				}

				rset.close();
				statement.close();
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING,
						"Could not check if the character had preset for current level " + e.getMessage(), e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}

			if (loadDefault || _hasPresetForCurrentLevel)
			{
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();

					String query =
							"SELECT charId, slot, page, type, shortcut_id, level FROM character_shortcuts WHERE charId=? AND class_index=?";

					if (_hasPresetForCurrentLevel)
					{
						query += " AND levelRange = ?";
					}

					PreparedStatement statement = con.prepareStatement(query);
					statement.setInt(1, _owner.getObjectId());
					statement.setInt(2, classIndex);

					if (_hasPresetForCurrentLevel)
					{
						statement.setInt(3, levelRange);
					}

					ResultSet rset = statement.executeQuery();

					while (rset.next())
					{
						int slot = rset.getInt("slot");
						int page = rset.getInt("page");
						int type = rset.getInt("type");
						int id = rset.getInt("shortcut_id");
						int level = rset.getInt("level");

						L2ShortCut sc = new L2ShortCut(slot, page, type, id, level, 1);

						_shortCuts.get(classIndex).get(levelRange).put(slot + page * 12, sc);
					}

					//System.out.println("Shortcuts Size = " +  _shortCuts.get(classIndex).get(levelRange).values().size());

					rset.close();
					statement.close();
				}
				catch (Exception e)
				{
					Log.log(Level.WARNING, "Could not restore character shortcuts: " + e.getMessage(), e);
				}
				finally
				{
					L2DatabaseFactory.close(con);
				}
			}
		}

		if (_hasPresetForCurrentLevel)
		{
			_owner.sendMessage("Loaded the previously saved preset for this level range.");
		}
		else
		{
			_owner.sendMessage("You do not have any shortcut preset for this level range.");
		}

		// verify shortcuts
		for (L2ShortCut sc : getAllShortCuts())
		{
			if (sc == null)
			{
				continue;
			}

			if (sc.getType() == L2ShortCut.TYPE_ITEM)
			{
				L2ItemInstance item = _owner.getInventory().getItemByObjectId(sc.getId());
				if (item == null)
				{
					deleteShortCut(sc.getSlot(), sc.getPage());
				}
				else if (item.isEtcItem())
				{
					sc.setSharedReuseGroup(item.getEtcItem().getSharedReuseGroup());
				}
			}
			if (sc.getType() == L2ShortCut.TYPE_SKILL)
			{
				L2Skill skill = SkillTable.getInstance().getInfo(sc.getId(), sc.getLevel());
				sc.setSharedReuseGroup(skill.getReuseHashCode());
			}
		}

		//_owner.sendMessage("Shortcuts verified.");
	}

	public final boolean hasPresetFor(int classIndex, int levelRange)
	{
		if (!_shortCuts.containsKey(classIndex) || !_shortCuts.get(classIndex).containsKey(levelRange))
		{
			restore(classIndex, levelRange, false);
		}

		return _shortCuts.get(classIndex).get(levelRange).values().size() != 0;
	}
}
