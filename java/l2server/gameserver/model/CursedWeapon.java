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

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.CursedWeaponsManager;
import l2server.gameserver.instancemanager.TransformationManager;
import l2server.gameserver.model.L2Party.messageType;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.util.Broadcast;
import l2server.log.Log;
import l2server.util.Point3D;
import l2server.util.Rnd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;

public class CursedWeapon
{

	// this.name is the name of the cursed weapon associated with its ID.
	private final String name;
	// this.itemId is the Item ID of the cursed weapon.
	private final int itemId;
	// this.skillId is the skills ID.
	private final int skillId;
	private final int skillMaxLevel;
	private int dropRate;
	private int duration;
	private int durationLost;
	private int disapearChance;
	private int stageKills;

	// this should be false unless if the cursed weapon is dropped, in that case it would be true.
	private boolean isDropped = false;
	// this sets the cursed weapon status to true only if a player has the cursed weapon, otherwise this should be false.
	private boolean isActivated = false;
	private ScheduledFuture<?> removeTask;

	private int nbKills = 0;
	private long endTime = 0;

	private int playerId = 0;
	protected L2PcInstance player = null;
	private L2ItemInstance item = null;
	private int playerKarma = 0;
	private int playerPkKills = 0;
	protected int transformationId = 0;

	private static final int[] TRANSFORM_IDS = new int[]{3630, 3631};

	// =========================================================
	// Constructor
	public CursedWeapon(int itemId, int skillId, String name)
	{
		this.name = name;
		this.itemId = itemId;
		this.skillId = skillId;
		this.skillMaxLevel = SkillTable.getInstance().getMaxLevel(this.skillId);
	}

	// =========================================================
	// Private
	public void endOfLife()
	{
		if (this.isActivated)
		{
			if (this.player != null && this.player.isOnline())
			{
				// Remove from player
				Log.info(this.name + " being removed online.");

				this.player.abortAttack();

				this.player.setReputation(this.playerKarma);
				this.player.setPkKills(this.playerPkKills);
				this.player.setCursedWeaponEquippedId(0);
				removeSkill();

				// Remove
				this.player.getInventory().unEquipItemInBodySlot(L2Item.SLOT_LR_HAND);
				this.player.store();

				// Destroy
				L2ItemInstance removedItem = this.player.getInventory().destroyItemByItemId("", this.itemId, 1, this.player, null);
				if (removedItem != null && !Config.FORCE_INVENTORY_UPDATE)
				{
					InventoryUpdate iu = new InventoryUpdate();
					if (removedItem.getCount() == 0)
					{
						iu.addRemovedItem(removedItem);
					}
					else
					{
						iu.addModifiedItem(removedItem);
					}

					this.player.sendPacket(iu);
				}
				else
				{
					this.player.sendPacket(new ItemList(this.player, true));
				}

				this.player.broadcastUserInfo();
			}
			else
			{
				// Remove from Db
				Log.info(this.name + " being removed offline.");

				Connection con = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();

					// Delete the item
					PreparedStatement statement =
							con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
					statement.setInt(1, this.playerId);
					statement.setInt(2, this.itemId);
					if (statement.executeUpdate() != 1)
					{
						Log.warning("Error while deleting itemId " + this.itemId + " from userId " + this.playerId);
					}
					statement.close();
					/* Yesod: Skill is not stored into database any more.
                    // Delete the skill
					statement = con.prepareStatement("DELETE FROM character_skills WHERE charId=? AND skill_id=?");
					statement.setInt(1, this.playerId);
					statement.setInt(2, this.skillId);
					if (statement.executeUpdate() != 1)
					{
						Logozo.warning("Error while deleting skillId "+ this.skillId +" from userId "+_playerId);
					}
					 */
					// Restore the karma
					statement = con.prepareStatement("UPDATE characters SET reputation=?, pkkills=? WHERE charId=?");
					statement.setInt(1, this.playerKarma);
					statement.setInt(2, this.playerPkKills);
					statement.setInt(3, this.playerId);
					if (statement.executeUpdate() != 1)
					{
						Log.warning("Error while updating karma & pkkills for userId " + this.playerId);
					}

					statement.close();
				}
				catch (Exception e)
				{
					Log.log(Level.WARNING, "Could not delete : " + e.getMessage(), e);
				}
				finally
				{
					L2DatabaseFactory.close(con);
				}
			}
		}
		else
		{
			// either this cursed weapon is in the inventory of someone who has another cursed weapon equipped,
			// OR this cursed weapon is on the ground.
			if (this.player != null && this.player.getInventory().getItemByItemId(this.itemId) != null)
			{
				// Destroy
				L2ItemInstance removedItem = this.player.getInventory().destroyItemByItemId("", this.itemId, 1, this.player, null);
				if (!Config.FORCE_INVENTORY_UPDATE)
				{
					InventoryUpdate iu = new InventoryUpdate();
					if (removedItem.getCount() == 0)
					{
						iu.addRemovedItem(removedItem);
					}
					else
					{
						iu.addModifiedItem(removedItem);
					}

					this.player.sendPacket(iu);
				}
				else
				{
					this.player.sendPacket(new ItemList(this.player, true));
				}

				this.player.broadcastUserInfo();
			}
			//  is dropped on the ground
			else if (this.item != null)
			{
				this.item.decayMe();
				L2World.getInstance().removeObject(this.item);
				Log.info(this.name + " item has been removed from World.");
			}
		}

		// Delete infos from table if any
		CursedWeaponsManager.removeFromDb(this.itemId);

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
		sm.addItemName(this.itemId);
		CursedWeaponsManager.announce(sm);

		// Reset  state
		cancelTask();
		this.isActivated = false;
		this.isDropped = false;
		this.endTime = 0;
		this.player = null;
		this.playerId = 0;
		this.playerKarma = 0;
		this.playerPkKills = 0;
		this.item = null;
		this.nbKills = 0;
	}

	private void cancelTask()
	{
		if (this.removeTask != null)
		{
			this.removeTask.cancel(true);
			this.removeTask = null;
		}
	}

	private class RemoveTask implements Runnable
	{
		protected RemoveTask()
		{
		}

		@Override
		public void run()
		{
			if (System.currentTimeMillis() >= getEndTime())
			{
				endOfLife();
			}
		}
	}

	private void dropIt(L2Attackable attackable, L2PcInstance player)
	{
		dropIt(attackable, player, null, true);
	}

	private void dropIt(L2Attackable attackable, L2PcInstance player, L2Character killer, boolean fromMonster)
	{
		this.isActivated = false;

		if (fromMonster)
		{
			this.item = attackable.dropItem(player, this.itemId, 1);
			this.item.setDropTime(0); // Prevent item from being removed by ItemsAutoDestroy

			// RedSky and Earthquake
			ExRedSky packet = new ExRedSky(10);
			Earthquake eq = new Earthquake(player.getX(), player.getY(), player.getZ(), 14, 3);
			Broadcast.toAllOnlinePlayers(packet);
			Broadcast.toAllOnlinePlayers(eq);
		}
		else
		{
			this.item = this.player.getInventory().getItemByItemId(this.itemId);
			this.player.dropItem("DieDrop", this.item, killer, true);
			this.player.setReputation(this.playerKarma);
			this.player.setPkKills(this.playerPkKills);
			this.player.setCursedWeaponEquippedId(0);
			removeSkill();
			this.player.abortAttack();
			//L2ItemInstance item = this.player.getInventory().getItemByItemId(this.itemId);
			//_player.getInventory().dropItem("DieDrop", item, this.player, null);
			//_player.getInventory().getItemByItemId(this.itemId).dropMe(this.player, this.player.getX(), this.player.getY(), this.player.getZ());
		}
		this.isDropped = true;
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_WAS_DROPPED_IN_THE_S1_REGION);
		if (player != null)
		{
			sm.addZoneName(player.getX(), player.getY(), player.getZ()); // Region Name
		}
		else if (this.player != null)
		{
			sm.addZoneName(this.player.getX(), this.player.getY(), this.player.getZ()); // Region Name
		}
		else
		{
			sm.addZoneName(killer.getX(), killer.getY(), killer.getZ()); // Region Name
		}
		sm.addItemName(this.itemId);
		CursedWeaponsManager.announce(sm); // in the Hot Spring region
	}

	public void cursedOnLogin()
	{
		doTransform();
		giveSkill();

		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S2_OWNER_HAS_LOGGED_INTO_THE_S1_REGION);
		msg.addZoneName(this.player.getX(), this.player.getY(), this.player.getZ());
		msg.addItemName(this.player.getCursedWeaponEquippedId());
		CursedWeaponsManager.announce(msg);

		CursedWeapon cw = CursedWeaponsManager.getInstance().getCursedWeapon(this.player.getCursedWeaponEquippedId());
		SystemMessage msg2 = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1);
		int timeLeft = (int) (cw.getTimeLeft() / 60000);
		msg2.addItemName(this.player.getCursedWeaponEquippedId());
		msg2.addNumber(timeLeft);
		this.player.sendPacket(msg2);
	}

	/**
	 * Yesod:<br>
	 * Rebind the passive skill belonging to the CursedWeapon. Invoke this
	 * method if the weapon owner switches to a subclass.
	 */
	public void giveSkill()
	{
		int level = 1 + this.nbKills / this.stageKills;
		if (level > skillMaxLevel)
		{
			level = this.skillMaxLevel;
		}

		L2Skill skill = SkillTable.getInstance().getInfo(this.skillId, level);
		// Yesod:
		// To properly support subclasses this skill can not be stored.
		this.player.addSkill(skill, false);

		// Void Burst, Void Flow
		skill = SkillTable.FrequentSkill.VOID_BURST.getSkill();
		this.player.addSkill(skill, false);
		skill = SkillTable.FrequentSkill.VOID_FLOW.getSkill();
		this.player.addSkill(skill, false);
		this.player.setTransformAllowedSkills(TRANSFORM_IDS);
		if (Config.DEBUG)
		{
			Log.info("Player " + this.player.getName() + " has been awarded with skill " + skill);
		}
		this.player.sendSkillList();
	}

	public void doTransform()
	{
		if (this.itemId == 8689)
		{
			transformationId = 302;
		}
		else if (this.itemId == 8190)
		{
			transformationId = 301;
		}

		if (this.player.isTransformed() || this.player.isInStance())
		{
			this.player.stopTransformation(true);

			ThreadPoolManager.getInstance().scheduleGeneral(
					() -> TransformationManager.getInstance().transformPlayer(transformationId, this.player), 500);
		}
		else
		{
			TransformationManager.getInstance().transformPlayer(transformationId, this.player);
		}
	}

	public void removeSkill()
	{
		this.player.removeSkill(this.skillId);
		this.player.removeSkill(SkillTable.FrequentSkill.VOID_BURST.getSkill().getId());
		this.player.removeSkill(SkillTable.FrequentSkill.VOID_FLOW.getSkill().getId());
		this.player.unTransform(true);
		this.player.sendSkillList();
	}

	// =========================================================
	// Public
	public void reActivate()
	{
		this.isActivated = true;
		if (this.endTime - System.currentTimeMillis() <= 0)
		{
			endOfLife();
		}
		else
		{
			this.removeTask = ThreadPoolManager.getInstance()
					.scheduleGeneralAtFixedRate(new RemoveTask(), this.durationLost * 12000L, this.durationLost * 12000L);
		}
	}

	public boolean checkDrop(L2Attackable attackable, L2PcInstance player)
	{
		if (Rnd.get(100000) < this.dropRate)
		{
			// Drop the item
			dropIt(attackable, player);

			// Start the Life Task
			this.endTime = System.currentTimeMillis() + this.duration * 60000L;
			this.removeTask = ThreadPoolManager.getInstance()
					.scheduleGeneralAtFixedRate(new RemoveTask(), this.durationLost * 12000L, this.durationLost * 12000L);

			return true;
		}

		return false;
	}

	public void activate(L2PcInstance player, L2ItemInstance item)
	{
		// if the player is mounted, attempt to unmount first.  Only allow picking up
		// the zariche if unmounting is successful.
		if (player.isMounted())
		{
			if (!player.dismount())
			{
				// TODO: correct this custom message.
				player.sendMessage("You may not pick up this item while riding in this territory");
				player.dropItem("InvDrop", item, null, true);
				return;
			}
		}

		this.isActivated = true;

		// Player holding it data
		this.player = player;
		this.playerId = this.player.getObjectId();
		this.playerKarma = this.player.getReputation();
		this.playerPkKills = this.player.getPkKills();
		saveData();

		// Change player stats
		this.player.setCursedWeaponEquippedId(this.itemId);
		this.player.setReputation(9999999);
		this.player.setPkKills(0);
		if (this.player.isInParty())
		{
			this.player.getParty().removePartyMember(this.player, messageType.Expelled);
		}

		// Disable All Skills
		// Do Transform
		doTransform();
		// Add skill
		giveSkill();

		// Equip with the weapon
		this.item = item;
		//L2ItemInstance[] items =
		this.player.getInventory().equipItem(this.item);
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED);
		sm.addItemName(this.item);
		this.player.sendPacket(sm);

		// Fully heal player
		this.player.setCurrentHpMp(this.player.getMaxHp(), this.player.getMaxMp());
		this.player.setCurrentCp(this.player.getMaxCp());

		// Refresh inventory
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(this.item);
			//iu.addItems(Arrays.asList(items));
			this.player.sendPacket(iu);
		}
		else
		{
			this.player.sendPacket(new ItemList(this.player, false));
		}

		// Refresh player stats
		this.player.broadcastUserInfo();

		SocialAction atk = new SocialAction(this.player.getObjectId(), 17);

		this.player.broadcastPacket(atk);

		sm = SystemMessage.getSystemMessage(SystemMessageId.THE_OWNER_OF_S2_HAS_APPEARED_IN_THE_S1_REGION);
		sm.addZoneName(this.player.getX(), this.player.getY(), this.player.getZ()); // Region Name
		sm.addItemName(this.item);
		CursedWeaponsManager.announce(sm);
	}

	public void saveData()
	{
		if (Config.DEBUG)
		{
			Log.info("CursedWeapon: Saving data to disk.");
		}

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			// Delete previous datas
			PreparedStatement statement = con.prepareStatement("DELETE FROM cursed_weapons WHERE itemId = ?");
			statement.setInt(1, this.itemId);
			statement.executeUpdate();

			if (this.isActivated)
			{
				statement = con.prepareStatement(
						"INSERT INTO cursed_weapons (itemId, charId, playerKarma, playerPkKills, nbKills, endTime) VALUES (?, ?, ?, ?, ?, ?)");
				statement.setInt(1, this.itemId);
				statement.setInt(2, this.playerId);
				statement.setInt(3, this.playerKarma);
				statement.setInt(4, this.playerPkKills);
				statement.setInt(5, this.nbKills);
				statement.setLong(6, this.endTime);
				statement.executeUpdate();
				statement.close();
			}
		}
		catch (SQLException e)
		{
			Log.log(Level.SEVERE, "CursedWeapon: Failed to save data.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void dropIt(L2Character killer)
	{
		if (Rnd.get(100) <= this.disapearChance)
		{
			// Remove it
			endOfLife();
		}
		else
		{
			// Unequip & Drop
			dropIt(null, null, killer, false);
			// Reset player stats
			this.player.setReputation(this.playerKarma);
			this.player.setPkKills(this.playerPkKills);
			this.player.setCursedWeaponEquippedId(0);
			removeSkill();

			this.player.abortAttack();

			this.player.broadcastUserInfo();
		}
	}

	public void increaseKills()
	{
		this.nbKills++;

		if (this.player != null && this.player.isOnline())
		{
			this.player.setPkKills(this.nbKills);
			this.player.sendPacket(new UserInfo(this.player));

			if (this.nbKills % this.stageKills == 0 && this.nbKills <= this.stageKills * (this.skillMaxLevel - 1))
			{
				giveSkill();
			}
		}
		// Reduce time-to-live
		this.endTime -= this.durationLost * 60000L;
		saveData();
	}

	// =========================================================
	// Setter
	public void setDisapearChance(int disapearChance)
	{
		this.disapearChance = disapearChance;
	}

	public void setDropRate(int dropRate)
	{
		this.dropRate = dropRate;
	}

	public void setDuration(int duration)
	{
		this.duration = duration;
	}

	public void setDurationLost(int durationLost)
	{
		this.durationLost = durationLost;
	}

	public void setStageKills(int stageKills)
	{
		this.stageKills = stageKills;
	}

	public void setNbKills(int nbKills)
	{
		this.nbKills = nbKills;
	}

	public void setPlayerId(int playerId)
	{
		this.playerId = playerId;
	}

	public void setPlayerKarma(int playerKarma)
	{
		this.playerKarma = playerKarma;
	}

	public void setPlayerPkKills(int playerPkKills)
	{
		this.playerPkKills = playerPkKills;
	}

	public void setActivated(boolean isActivated)
	{
		this.isActivated = isActivated;
	}

	public void setDropped(boolean isDropped)
	{
		this.isDropped = isDropped;
	}

	public void setEndTime(long endTime)
	{
		this.endTime = endTime;
	}

	public void setPlayer(L2PcInstance player)
	{
		this.player = player;
	}

	public void setItem(L2ItemInstance item)
	{
		this.item = item;
	}

	// =========================================================
	// Getter
	public boolean isActivated()
	{
		return this.isActivated;
	}

	public boolean isDropped()
	{
		return this.isDropped;
	}

	public long getEndTime()
	{
		return this.endTime;
	}

	public String getName()
	{
		return this.name;
	}

	public int getItemId()
	{
		return this.itemId;
	}

	public int getSkillId()
	{
		return this.skillId;
	}

	public int getPlayerId()
	{
		return this.playerId;
	}

	public L2PcInstance getPlayer()
	{
		return this.player;
	}

	public int getPlayerKarma()
	{
		return this.playerKarma;
	}

	public int getPlayerPkKills()
	{
		return this.playerPkKills;
	}

	public int getNbKills()
	{
		return this.nbKills;
	}

	public int getStageKills()
	{
		return this.stageKills;
	}

	public boolean isActive()
	{
		return this.isActivated || this.isDropped;
	}

	public int getLevel()
	{
		if (this.nbKills > stageKills * this.skillMaxLevel)
		{
			return this.skillMaxLevel;
		}
		else
		{
			return this.nbKills / this.stageKills;
		}
	}

	public long getTimeLeft()
	{
		return this.endTime - System.currentTimeMillis();
	}

	public void goTo(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}

		if (this.isActivated && this.player != null)
		{
			// Go to player holding the weapon
			player.teleToLocation(this.player.getX(), this.player.getY(), this.player.getZ() + 20, true);
		}
		else if (this.isDropped && this.item != null)
		{
			// Go to item on the ground
			player.teleToLocation(this.item.getX(), this.item.getY(), this.item.getZ() + 20, true);
		}
		else
		{
			player.sendMessage(this.name + " isn't in the World.");
		}
	}

	public Point3D getWorldPosition()
	{
		if (this.isActivated && this.player != null)
		{
			return this.player.getPosition().getWorldPosition();
		}

		if (this.isDropped && this.item != null)
		{
			return this.item.getPosition().getWorldPosition();
		}

		return null;
	}

	public long getDuration()
	{
		return this.duration;
	}
}
