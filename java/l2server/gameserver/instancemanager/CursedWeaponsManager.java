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

package l2server.gameserver.instancemanager;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.model.CursedWeapon;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.*;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Broadcast;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author Micht
 */
public class CursedWeaponsManager
{

	public static CursedWeaponsManager getInstance()
	{
		return SingletonHolder._instance;
	}

	// =========================================================
	// Data Field
	private Map<Integer, CursedWeapon> _cursedWeapons;

	// =========================================================
	// Constructor
	private CursedWeaponsManager()
	{
		init();
	}

	private void init()
	{
		Log.info("Initializing CursedWeaponsManager");
		_cursedWeapons = new HashMap<>();

		if (!Config.ALLOW_CURSED_WEAPONS)
		{
			return;
		}

		load();
		restore();
		controlPlayers();
		Log.info("Loaded : " + _cursedWeapons.size() + " cursed weapon(s).");
	}

	// =========================================================
	// Method - Private
	public final void reload()
	{
		init();
	}

	private void load()
	{
		if (Config.DEBUG)
		{
			Log.info("  Parsing ... ");
		}
		try
		{
			File file = new File(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "cursedWeapons.xml");
			if (!file.exists())
			{
				if (Config.DEBUG)
				{
					Log.info("NO FILE");
				}
				return;
			}

			XmlDocument doc = new XmlDocument(file);
			for (XmlNode n : doc.getChildren())
			{
				if (n.getName().equalsIgnoreCase("list"))
				{
					for (XmlNode d : n.getChildren())
					{
						if (d.getName().equalsIgnoreCase("item"))
						{
							int id = d.getInt("id");
							int skillId = d.getInt("skillId");
							String name = d.getString("name");

							CursedWeapon cw = new CursedWeapon(id, skillId, name);

							int val;
							for (XmlNode cd : d.getChildren())
							{
								if (cd.getName().equalsIgnoreCase("dropRate"))
								{
									val = cd.getInt("val");
									cw.setDropRate(val);
								}
								else if (cd.getName().equalsIgnoreCase("duration"))
								{
									val = cd.getInt("val");
									cw.setDuration(val);
								}
								else if (cd.getName().equalsIgnoreCase("durationLost"))
								{
									val = cd.getInt("val");
									cw.setDurationLost(val);
								}
								else if (cd.getName().equalsIgnoreCase("disapearChance"))
								{
									val = cd.getInt("val");
									cw.setDisapearChance(val);
								}
								else if (cd.getName().equalsIgnoreCase("stageKills"))
								{
									val = cd.getInt("val");
									cw.setStageKills(val);
								}
							}

							// Store cursed weapon
							_cursedWeapons.put(id, cw);
						}
					}
				}
			}

			if (Config.DEBUG)
			{
				Log.info("OK");
			}
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error parsing cursed weapons file.", e);

		}
	}

	private void restore()
	{
		if (Config.DEBUG)
		{
			Log.info("  Restoring ... ");
		}
		Connection con = null;
		try
		{
			// Retrieve the L2PcInstance from the characters table of the database
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement(
					"SELECT itemId, charId, playerKarma, playerPkKills, nbKills, endTime FROM cursed_weapons");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				int itemId = rset.getInt("itemId");
				int playerId = rset.getInt("charId");
				int playerKarma = rset.getInt("playerKarma");
				int playerPkKills = rset.getInt("playerPkKills");
				int nbKills = rset.getInt("nbKills");
				long endTime = rset.getLong("endTime");

				CursedWeapon cw = _cursedWeapons.get(itemId);
				cw.setPlayerId(playerId);
				cw.setPlayerKarma(playerKarma);
				cw.setPlayerPkKills(playerPkKills);
				cw.setNbKills(nbKills);
				cw.setEndTime(endTime);
				cw.reActivate();
			}

			rset.close();
			statement.close();

			if (Config.DEBUG)
			{
				Log.info("OK");
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Could not restore CursedWeapons data: " + e.getMessage(), e);

		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void controlPlayers()
	{
		if (Config.DEBUG)
		{
			Log.info("  Checking players ... ");
		}

		Connection con = null;
		try
		{
			// Retrieve the L2PcInstance from the characters table of the database
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = null;
			ResultSet rset = null;

			// TODO: See comments below...
			// This entire for loop should NOT be necessary, since it is already handled by
			// CursedWeapon.endOfLife().  However, if we indeed *need* to duplicate it for safety,
			// then we'd better make sure that it FULLY cleans up inactive cursed weapons!
			// Undesired effects result otherwise, such as player with no zariche but with karma
			// or a lost-child entry in the cursedweapons table, without a corresponding one in items...
			for (CursedWeapon cw : _cursedWeapons.values())
			{
				if (cw.isActivated())
				{
					continue;
				}

				// Do an item check to be sure that the cursed weapon isn't hold by someone
				int itemId = cw.getItemId();
				try
				{
					statement = con.prepareStatement("SELECT owner_id FROM items WHERE item_id=?");
					statement.setInt(1, itemId);
					rset = statement.executeQuery();

					if (rset.next())
					{
						// A player has the cursed weapon in his inventory ...
						int playerId = rset.getInt("owner_id");
						Log.info("PROBLEM : Player " + playerId + " owns the cursed weapon " + itemId +
								" but he shouldn't.");

						// Delete the item
						statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
						statement.setInt(1, playerId);
						statement.setInt(2, itemId);
						if (statement.executeUpdate() != 1)
						{
							Log.warning("Error while deleting cursed weapon " + itemId + " from userId " + playerId);
						}
						statement.close();

						// Delete the skill
						/*
                        statement = con.prepareStatement("DELETE FROM character_skills WHERE charId=? AND skill_id=");
						statement.setInt(1, playerId);
						statement.setInt(2, cw.getSkillId());
						if (statement.executeUpdate() != 1)
						{
							Logozo.warning("Error while deleting cursed weapon "+itemId+" skill from userId "+playerId);
						}
						 */
						// Restore the player's old karma and pk count
						statement = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE charId=?");
						statement.setInt(1, cw.getPlayerKarma());
						statement.setInt(2, cw.getPlayerPkKills());
						statement.setInt(3, playerId);
						if (statement.executeUpdate() != 1)
						{
							Log.warning("Error while updating karma & pkkills for userId " + cw.getPlayerId());
						}
						// clean up the cursedweapons table.
						removeFromDb(itemId);
					}
					rset.close();
					statement.close();
				}
				catch (SQLException ignored)
				{
				}
			}
		}
		catch (Exception e)
		{
			if (Config.DEBUG)
			{
				Log.log(Level.WARNING, "Could not check CursedWeapons data: " + e.getMessage(), e);
			}
			return;
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		if (Config.DEBUG)
		{
			Log.info("DONE");
		}
	}

	// =========================================================
	// Properties - Public
	public synchronized void checkDrop(L2Attackable attackable, L2PcInstance player)
	{
		if (attackable instanceof L2DefenderInstance || attackable instanceof L2GuardInstance ||
				attackable instanceof L2GrandBossInstance || attackable instanceof L2FeedableBeastInstance ||
				attackable instanceof L2FortCommanderInstance)
		{
			return;
		}

		for (CursedWeapon cw : _cursedWeapons.values())
		{
			if (cw.isActive())
			{
				continue;
			}

			if (cw.checkDrop(attackable, player))
			{
				break;
			}
		}
	}

	public void activate(L2PcInstance player, L2ItemInstance item)
	{
		CursedWeapon cw = _cursedWeapons.get(item.getItemId());
		if (player.isCursedWeaponEquipped()) // cannot own 2 cursed swords
		{
			CursedWeapon cw2 = _cursedWeapons.get(player.getCursedWeaponEquippedId());
            /* TODO: give the bonus level in a more appropriate manner.
			 *  The following code adds "_stageKills" levels.  This will also show in the char status.
			 * I do not have enough info to know if the bonus should be shown in the pk count, or if it
			 * should be a full "_stageKills" bonus or just the remaining from the current count till the
			 * of the current stage...
			 * This code is a TEMP fix, so that the cursed weapon's bonus level can be observed with as
			 * little change in the code as possible, until proper info arises.
			 */
			cw2.setNbKills(cw2.getStageKills() - 1);
			cw2.increaseKills();

			// erase the newly obtained cursed weapon
			cw.setPlayer(player); // NECESSARY in order to find which inventory the weapon is in!
			cw.endOfLife(); // expire the weapon and clean up.
		}
		else
		{
			cw.activate(player, item);
		}
	}

	public void drop(int itemId, L2Character killer)
	{
		CursedWeapon cw = _cursedWeapons.get(itemId);

		cw.dropIt(killer);
	}

	public void increaseKills(int itemId)
	{
		CursedWeapon cw = _cursedWeapons.get(itemId);

		cw.increaseKills();
	}

	public int getLevel(int itemId)
	{
		CursedWeapon cw = _cursedWeapons.get(itemId);

		return cw.getLevel();
	}

	public static void announce(SystemMessage sm)
	{
		Broadcast.toAllOnlinePlayers(sm);
	}

	public void checkPlayer(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}

		for (CursedWeapon cw : _cursedWeapons.values())
		{
			if (cw.isActivated() && player.getObjectId() == cw.getPlayerId())
			{
				cw.setPlayer(player);
				cw.setItem(player.getInventory().getItemByItemId(cw.getItemId()));
				cw.giveSkill();
				player.setCursedWeaponEquippedId(cw.getItemId());

				SystemMessage sm =
						SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1);
				sm.addString(cw.getName());
				//sm.addItemName(cw.getItemId());
				sm.addNumber((int) ((cw.getEndTime() - System.currentTimeMillis()) / 60000));
				player.sendPacket(sm);
			}
		}
	}

	public int checkOwnsWeaponId(int ownerId)
	{
		for (CursedWeapon cw : _cursedWeapons.values())
		{
			if (cw.isActivated() && ownerId == cw.getPlayerId())
			{
				return cw.getItemId();
			}
		}
		return -1;
	}

	public static void removeFromDb(int itemId)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			// Delete datas
			PreparedStatement statement = con.prepareStatement("DELETE FROM cursed_weapons WHERE itemId = ?");
			statement.setInt(1, itemId);
			statement.executeUpdate();

			statement.close();
		}
		catch (SQLException e)
		{
			Log.log(Level.SEVERE, "CursedWeaponsManager: Failed to remove data: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void saveData()
	{
		for (CursedWeapon cw : _cursedWeapons.values())
		{
			cw.saveData();
		}
	}

	// =========================================================
	public boolean isCursed(int itemId)
	{
		return _cursedWeapons.containsKey(itemId);
	}

	public Collection<CursedWeapon> getCursedWeapons()
	{
		return _cursedWeapons.values();
	}

	public Set<Integer> getCursedWeaponsIds()
	{
		return _cursedWeapons.keySet();
	}

	public CursedWeapon getCursedWeapon(int itemId)
	{
		return _cursedWeapons.get(itemId);
	}

	public void givePassive(int itemId)
	{
		try
		{
			_cursedWeapons.get(itemId).giveSkill();
		}
		catch (Exception e)
		{
            /**/
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CursedWeaponsManager _instance = new CursedWeaponsManager();
	}
}
