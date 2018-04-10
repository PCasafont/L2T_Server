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

import l2server.DatabasePool;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.ShortCutInit;
import l2server.gameserver.network.serverpackets.ShortCutRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:33 $
 */
public class ShortCuts {
	private static Logger log = LoggerFactory.getLogger(ShortCuts.class.getName());

	private Player owner;
	private Map<Integer, Map<Integer, Map<Integer, L2ShortCut>>> shortCuts = new TreeMap<>();

	private boolean hasPresetForCurrentLevel;

	public ShortCuts(Player owner) {
		this.owner = owner;
	}

	public L2ShortCut[] getAllShortCuts() {
		if (!shortCuts.containsKey(owner.getClassIndex())) {
			shortCuts.put(owner.getClassIndex(), new HashMap<>());
		}

		if (!shortCuts.get(owner.getClassIndex()).containsKey(owner.getGearGradeForCurrentLevel())) {
			shortCuts.get(owner.getClassIndex()).put(owner.getGearGradeForCurrentLevel(), new HashMap<>());
		}

		Map<Integer, L2ShortCut> allShortcuts = shortCuts.get(owner.getClassIndex()).get(owner.getGearGradeForCurrentLevel());
		return allShortcuts.values().toArray(new L2ShortCut[allShortcuts.size()]);
	}

	public L2ShortCut getShortCut(int slot, int page) {
		L2ShortCut sc = shortCuts.get(owner.getClassIndex()).get(owner.getGearGradeForCurrentLevel()).get(slot + page * 12);

		// verify shortcut
		if (sc != null && sc.getType() == L2ShortCut.TYPE_ITEM) {
			if (owner.getInventory().getItemByObjectId(sc.getId()) == null) {
				deleteShortCut(sc.getSlot(), sc.getPage());
				sc = null;
			}
		}

		return sc;
	}

	public synchronized void registerShortCut(L2ShortCut shortcut) {
		// verify shortcut
		if (shortcut.getType() == L2ShortCut.TYPE_ITEM) {
			Item item = owner.getInventory().getItemByObjectId(shortcut.getId());
			if (item == null) {
				return;
			}
			if (item.isEtcItem()) {
				shortcut.setSharedReuseGroup(item.getEtcItem().getSharedReuseGroup());
			}
		}
		if (shortcut.getType() == L2ShortCut.TYPE_SKILL) {
			Skill skill = SkillTable.getInstance().getInfo(shortcut.getId(), shortcut.getLevel());
			shortcut.setSharedReuseGroup(skill.getReuseHashCode());
		}

		if (!shortCuts.containsKey(owner.getClassIndex())) {
			//owner.sendMessage("Adding classIndex");
			shortCuts.put(owner.getClassIndex(), new HashMap<>());
		}

		if (!shortCuts.get(owner.getClassIndex()).containsKey(owner.getGearGradeForCurrentLevel())) {
			//owner.sendMessage("Adding levelRange");
			shortCuts.get(owner.getClassIndex()).put(owner.getGearGradeForCurrentLevel(), new HashMap<>());
		}

		L2ShortCut oldShortCut = shortCuts.get(owner.getClassIndex())
				.get(owner.getGearGradeForCurrentLevel())
				.put(shortcut.getSlot() + 12 * shortcut.getPage(), shortcut);

		registerShortCutInDb(shortcut, oldShortCut);
	}

	public void registerShortCutInDb(L2ShortCut shortcut, L2ShortCut oldShortCut) {
		registerShortCutInDb(shortcut, oldShortCut, -1);
	}

	public void registerShortCutInDb(L2ShortCut shortcut, L2ShortCut oldShortCut, int levelRange) {
		if (oldShortCut != null) {
			deleteShortCutFromDb(oldShortCut);
		}

		Connection con = null;

		if (levelRange == -1) {
			boolean hasPresetForCurrentLevel = false;

			try {
				con = DatabasePool.getInstance().getConnection();
				PreparedStatement statement =
						con.prepareStatement("SELECT charId FROM character_shortcuts WHERE charId=? AND class_index=? AND levelRange = ?");
				statement.setInt(1, owner.getObjectId());
				statement.setInt(2, owner.getClassIndex());
				statement.setInt(3, owner.getGearGradeForCurrentLevel());

				ResultSet rset = statement.executeQuery();

				if (rset.next()) {
					hasPresetForCurrentLevel = true;
				}

				rset.close();
				statement.close();
			} catch (Exception e) {
				log.warn("Could not check if the character had preset for current level " + e.getMessage(), e);
			} finally {
				DatabasePool.close(con);
			}

			if (!hasPresetForCurrentLevel) {
				for (L2ShortCut sh : getAllShortCuts()) {
					if (sh == null) {
						continue;
					}

					L2ShortCut shortcutCopy =
							new L2ShortCut(sh.getSlot(), sh.getPage(), sh.getType(), sh.getId(), sh.getLevel(), sh.getCharacterType());

					registerShortCutInDb(shortcutCopy, null, owner.getGearGradeForCurrentLevel());
				}
			}
		}

		try {
			con = DatabasePool.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement(
					"REPLACE INTO character_shortcuts (charId,slot,page,type,shortcut_id,level,class_index,levelRange) VALUES(?,?,?,?,?,?,?,?)");
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, shortcut.getSlot());
			statement.setInt(3, shortcut.getPage());
			statement.setInt(4, shortcut.getType());
			statement.setInt(5, shortcut.getId());
			statement.setInt(6, shortcut.getLevel());
			statement.setInt(7, owner.getClassIndex());
			statement.setInt(8, owner.getGearGradeForCurrentLevel());
			statement.execute();
			statement.close();
		} catch (Exception e) {
			log.warn("Could not store character shortcut: " + e.getMessage(), e);
		} finally {
			DatabasePool.close(con);
		}

		if (!shortCuts.containsKey(owner.getClassIndex())) {
			shortCuts.put(owner.getClassIndex(), new HashMap<>());

			owner.sendSysMessage("Adding classIndex");
		}

		if (!shortCuts.get(owner.getClassIndex()).containsKey(owner.getGearGradeForCurrentLevel())) {
			shortCuts.get(owner.getClassIndex()).put(owner.getGearGradeForCurrentLevel(), new HashMap<>());

			owner.sendSysMessage("Adding LevelRange");
		}

		shortCuts.get(owner.getClassIndex()).get(owner.getGearGradeForCurrentLevel()).put(shortcut.getSlot() + shortcut.getPage() * 12, shortcut);
	}

	public synchronized void deleteShortCut(int slot, int page) {
		if (shortCuts.get(owner.getClassIndex()) == null || shortCuts.get(owner.getClassIndex()).get(owner.getGearGradeForCurrentLevel()) == null) {
			return;
		}

		L2ShortCut old = shortCuts.get(owner.getClassIndex()).get(owner.getGearGradeForCurrentLevel()).remove(slot + page * 12);

		owner.sendSysMessage("Old Shortcut = " + old);
		if (old == null || owner == null) {
			return;
		}

		deleteShortCutFromDb(old);
		owner.sendPacket(new ShortCutInit(owner));
	}

	public synchronized void deleteShortCutByObjectId(int objectId) {
		if (shortCuts.get(owner.getClassIndex()) == null || shortCuts.get(owner.getClassIndex()).get(owner.getGearGradeForCurrentLevel()) == null) {
			return;
		}

		try {
			L2ShortCut toRemove = null;

			for (L2ShortCut shortcut : shortCuts.get(owner.getClassIndex()).get(owner.getGearGradeForCurrentLevel()).values()) {
				if (shortcut == null) {
					continue;
				}
				if (shortcut.getType() == L2ShortCut.TYPE_ITEM && shortcut.getId() == objectId) {
					toRemove = shortcut;
					break;
				}
			}

			if (toRemove != null) {
				deleteShortCut(toRemove.getSlot(), toRemove.getPage());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteShortCutFromDb(L2ShortCut shortcut) {
		Connection con = null;

		try {
			con = DatabasePool.getInstance().getConnection();

			PreparedStatement statement =
					con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=? AND slot=? AND page=? AND class_index=? AND levelRange=? ");
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, shortcut.getSlot());
			statement.setInt(3, shortcut.getPage());
			statement.setInt(4, owner.getClassIndex());
			statement.setInt(5, owner.getGearGradeForCurrentLevel());

			statement.execute();
			statement.close();
		} catch (Exception e) {
			log.warn("Could not delete character shortcut: " + e.getMessage(), e);
		} finally {
			DatabasePool.close(con);
		}
	}

	public void restore() {
		restore(owner.getClassIndex(), owner.getGearGradeForCurrentLevel(), true);
	}

	public void restore(final int classIndex, final int levelRange, boolean loadDefault) {
		hasPresetForCurrentLevel = shortCuts.containsKey(classIndex) && shortCuts.get(classIndex).containsKey(levelRange) &&
				shortCuts.get(classIndex).get(levelRange).values().size() != 0;

		//if (hasPresetForCurrentLevel)
		//	shortCuts.get(owner.getClassIndex()).get(owner.getGearGradeForCurrentLevel()).clear();
		//System.out.println("Shortcuts Size = " +  shortCuts.get(0).get(8).values().size());
		//System.out.println("Shortcuts Size = " +  shortCuts.get(0).get(10).values().size());

		Connection con = null;

		if (!hasPresetForCurrentLevel) {
			//owner.sendMessage("Loading Shortcuts from DB.");
			try {
				con = DatabasePool.getInstance().getConnection();
				PreparedStatement statement =
						con.prepareStatement("SELECT charId FROM character_shortcuts WHERE charId=? AND class_index=? AND levelRange = ?");
				statement.setInt(1, owner.getObjectId());
				statement.setInt(2, classIndex);
				statement.setInt(3, levelRange);

				ResultSet rset = statement.executeQuery();

				if (rset.next()) {
					hasPresetForCurrentLevel = true;
				}

				if (!shortCuts.containsKey(classIndex)) {
					shortCuts.put(classIndex, new HashMap<>());
				}

				if (!shortCuts.get(classIndex).containsKey(levelRange)) {
					shortCuts.get(classIndex).put(levelRange, new HashMap<>());
				}

				rset.close();
				statement.close();
			} catch (Exception e) {
				log.warn("Could not check if the character had preset for current level " + e.getMessage(), e);
			} finally {
				DatabasePool.close(con);
			}

			if (loadDefault || hasPresetForCurrentLevel) {
				try {
					con = DatabasePool.getInstance().getConnection();

					String query = "SELECT charId, slot, page, type, shortcut_id, level FROM character_shortcuts WHERE charId=? AND class_index=?";

					if (hasPresetForCurrentLevel) {
						query += " AND levelRange = ?";
					}

					PreparedStatement statement = con.prepareStatement(query);
					statement.setInt(1, owner.getObjectId());
					statement.setInt(2, classIndex);

					if (hasPresetForCurrentLevel) {
						statement.setInt(3, levelRange);
					}

					ResultSet rset = statement.executeQuery();

					while (rset.next()) {
						int slot = rset.getInt("slot");
						int page = rset.getInt("page");
						int type = rset.getInt("type");
						int id = rset.getInt("shortcut_id");
						int level = rset.getInt("level");

						L2ShortCut sc = new L2ShortCut(slot, page, type, id, level, 1);

						shortCuts.get(classIndex).get(levelRange).put(slot + page * 12, sc);
					}

					//System.out.println("Shortcuts Size = " +  shortCuts.get(classIndex).get(levelRange).values().size());

					rset.close();
					statement.close();
				} catch (Exception e) {
					log.warn("Could not restore character shortcuts: " + e.getMessage(), e);
				} finally {
					DatabasePool.close(con);
				}
			}
		}

		if (hasPresetForCurrentLevel) {
			owner.sendMessage("Loaded the previously saved preset for this level range.");
		} else {
			owner.sendMessage("You do not have any shortcut preset for this level range.");
		}

		// verify shortcuts
		for (L2ShortCut sc : getAllShortCuts()) {
			if (sc == null) {
				continue;
			}

			if (sc.getType() == L2ShortCut.TYPE_ITEM) {
				Item item = owner.getInventory().getItemByObjectId(sc.getId());
				if (item == null) {
					deleteShortCut(sc.getSlot(), sc.getPage());
				} else if (item.isEtcItem()) {
					sc.setSharedReuseGroup(item.getEtcItem().getSharedReuseGroup());
				}
			}
			if (sc.getType() == L2ShortCut.TYPE_SKILL) {
				Skill skill = SkillTable.getInstance().getInfo(sc.getId(), sc.getLevel());
				sc.setSharedReuseGroup(skill.getReuseHashCode());
			}
		}

		//owner.sendMessage("Shortcuts verified.");
	}

	public final boolean hasPresetFor(int classIndex, int levelRange) {
		if (!shortCuts.containsKey(classIndex) || !shortCuts.get(classIndex).containsKey(levelRange)) {
			restore(classIndex, levelRange, false);
		}

		return shortCuts.get(classIndex).get(levelRange).values().size() != 0;
	}
	
	/**
	 * Updates the shortcut bars with the new skill.
	 *
	 * @param skillId    the skill Id to search and update.
	 * @param skillLevel the skill level to update.
	 */
	public synchronized void updateSkillShortcuts(int skillId, int skillLevel) {
		// Update all the shortcuts for this skill
		for (L2ShortCut sc : getAllShortCuts()) {
			if ((sc.getId() == skillId) && (sc.getType() == L2ShortCut.TYPE_SKILL)) {
				final L2ShortCut newsc = new L2ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), skillLevel, 1);
				owner.sendPacket(new ShortCutRegister(newsc));
				owner.registerShortCut(newsc);
			}
		}
	}
	
	/**
	 * Updates the shortcut bars with the new skill.
	 *
	 * @param objId the item objectId to search and update.
	 * @param type  the short cut type to update.
	 */
	public synchronized void updateItemShortcuts(int objId) {
		for (L2ShortCut sc : getAllShortCuts()) {
			if ((sc.getId() == objId) && (sc.getType() == L2ShortCut.TYPE_ITEM)) {
				final L2ShortCut newsc = new L2ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), sc.getLevel(), sc.getCharacterType());
				owner.sendPacket(new ShortCutRegister(newsc));
				owner.registerShortCut(newsc);
			}
		}
	}
}
