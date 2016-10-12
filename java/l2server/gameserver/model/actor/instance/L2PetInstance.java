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

package l2server.gameserver.model.actor.instance;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.datatables.PetDataTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.handler.ItemHandler;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.instancemanager.CursedWeaponsManager;
import l2server.gameserver.instancemanager.ItemsOnGroundManager;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.stat.PetStat;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.model.itemcontainer.PetInventory;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.taskmanager.DecayTaskManager;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.item.L2EtcItemType;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.templates.item.L2Weapon;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.logging.Level;

/**
 * This class ...
 *
 * @version $Revision: 1.15.2.10.2.16 $ $Date: 2005/04/06 16:13:40 $
 */
public class L2PetInstance extends L2Summon
{
	private int _curFed;
	private PetInventory _inventory;
	private final int _controlObjectId;
	private boolean _respawned;
	private boolean _mountable;
	private Future<?> _feedTask;
	private L2PetData _data;
	private L2PetLevelData _leveldata;

	/**
	 * The Experience before the last Death Penalty
	 */
	private long _expBeforeDeath = 0;
	private int _curWeightPenalty = 0;

	private static final int PET_DECAY_DELAY = 86400000; // 24 hours

	public final L2PetLevelData getPetLevelData()
	{
		if (_leveldata == null)
		{
			_leveldata = PetDataTable.getInstance().getPetLevelData(getTemplate().NpcId, getStat().getLevel());
		}

		return _leveldata;
	}

	public final L2PetData getPetData()
	{
		if (_data == null)
		{
			_data = PetDataTable.getInstance().getPetData(getTemplate().NpcId);
		}

		return _data;
	}

	public final void setPetData(L2PetLevelData value)
	{
		_leveldata = value;
	}

	/**
	 * Manage Feeding Task.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR>
	 * <li>Feed or kill the pet depending on hunger level</li>
	 * <li>If pet has food in inventory and feed level drops below 55% then consume food from inventory</li>
	 * <li>Send a broadcastStatusUpdate packet for this L2PetInstance</li><BR><BR>
	 */

	class FeedTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (getOwner() == null || getOwner().getPet() == null ||
						getOwner().getPet().getObjectId() != getObjectId())
				{
					stopFeed();
					return;
				}
				else if (getCurrentFed() > getFeedConsume())
				{
					// eat
					setCurrentFed(getCurrentFed() - getFeedConsume());
				}
				else
				{
					setCurrentFed(0);
				}

				broadcastStatusUpdate();

				int[] foodIds = getPetData().getFood();
				if (foodIds.length == 0)
				{
					if (getCurrentFed() == 0)
					{
						// Owl Monk remove PK
						if (getTemplate().NpcId == 16050 && getOwner() != null)
						{
							getOwner().setPkKills(Math.max(0, getOwner().getPkKills() - Rnd.get(1, 6)));
						}
						getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_HELPER_PET_LEAVING));
						deleteMe(getOwner());
					}
					else if (isHungry())
					{
						getOwner().sendPacket(SystemMessage
								.getSystemMessage(SystemMessageId.THERE_NOT_MUCH_TIME_REMAINING_UNTIL_HELPER_LEAVES));
					}
					return;
				}
				L2ItemInstance food = null;
				for (int id : foodIds)
				{
					food = getInventory().getItemByItemId(id);
					if (food != null)
					{
						break;
					}
				}
				if (isRunning() && isHungry())
				{
					setWalking();
				}
				else if (!isHungry() && !isRunning())
				{
					setRunning();
				}
				if (food != null && isHungry())
				{
					IItemHandler handler = ItemHandler.getInstance().getItemHandler(food.getEtcItem());
					if (handler != null)
					{
						SystemMessage sm =
								SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY);
						sm.addItemName(food.getItemId());
						getOwner().sendPacket(sm);
						handler.useItem(L2PetInstance.this, food, false);
					}
				}
				else
				{
					if (getCurrentFed() == 0)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOUR_PET_IS_VERY_HUNGRY);
						getOwner().sendPacket(sm);
						if (Rnd.get(100) < 30)
						{
							stopFeed();
							sm = SystemMessage
									.getSystemMessage(SystemMessageId.STARVING_GRUMPY_AND_FED_UP_YOUR_PET_HAS_LEFT);
							getOwner().sendPacket(sm);
							Log.info("Hungry pet [" + getTemplate().getName() + "][" + getLevel() +
									"] deleted for player: " + getOwner() + " Control Item Id :" +
									getControlObjectId());
							deleteMe(getOwner());
						}
					}
					else if (getCurrentFed() < 0.10 * getPetLevelData().getPetMaxFeed())
					{
						SystemMessage sm = SystemMessage
								.getSystemMessage(SystemMessageId.PET_CAN_RUN_AWAY_WHEN_HUNGER_BELOW_10_PERCENT);
						getOwner().sendPacket(sm);
						if (Rnd.get(100) < 3)
						{
							stopFeed();
							sm = SystemMessage
									.getSystemMessage(SystemMessageId.STARVING_GRUMPY_AND_FED_UP_YOUR_PET_HAS_LEFT);
							getOwner().sendPacket(sm);
							Log.info("Hungry pet [" + getTemplate().getName() + "][" + getLevel() +
									"] deleted for player: " + getOwner() + " Control Item Id :" +
									getControlObjectId());
							deleteMe(getOwner());
						}
					}
				}
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "Pet [ObjectId: " + getObjectId() + "] a feed task error has occurred", e);
			}
		}

		/**
		 * @return
		 */
		private int getFeedConsume()
		{
			// if pet is attacking
			if (isAttackingNow())
			{
				return getPetLevelData().getPetFeedBattle();
			}
			else
			{
				return getPetLevelData().getPetFeedNormal();
			}
		}
	}

	public synchronized static L2PetInstance spawnPet(L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control)
	{
		if (L2World.getInstance().getPet(owner.getObjectId()) != null)
		{
			return null; // owner has a pet listed in world
		}

		L2PetInstance pet = restore(control, template, owner);
		// add the pet instance to world
		if (pet != null)
		{
			pet.setTitle(owner.getName());
			L2World.getInstance().addPet(owner.getObjectId(), pet);
		}

		return pet;
	}

	/**
	 * Constructor for new pet
	 *
	 * @param objectId
	 * @param template
	 * @param owner
	 * @param control
	 */
	public L2PetInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control)
	{
		this(objectId, template, owner, control,
				(byte) (template.TemplateId == 12564 ? owner.getLevel() : template.Level));
	}

	/**
	 * Constructor for restored pet
	 *
	 * @param objectId
	 * @param template
	 * @param owner
	 * @param control
	 * @param level
	 */
	public L2PetInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control, byte level)
	{
		super(objectId, template, owner);
		setInstanceType(InstanceType.L2PetInstance);

		_controlObjectId = control.getObjectId();

		getStat().setLevel((byte) Math.max(level, PetDataTable.getInstance().getPetMinLevel(template.NpcId)));

		_inventory = new PetInventory(this);
		_inventory.restore();

		int npcId = template.NpcId;
		_mountable = PetDataTable.isMountable(npcId);
		getPetData();
		getPetLevelData();
	}

	@Override
	public PetStat getStat()
	{
		return (PetStat) super.getStat();
	}

	@Override
	public void initCharStat()
	{
		setStat(new PetStat(this));
	}

	public boolean isRespawned()
	{
		return _respawned;
	}

	@Override
	public int getSummonType()
	{
		return 2;
	}

	@Override
	public int getControlObjectId()
	{
		return _controlObjectId;
	}

	public L2ItemInstance getControlItem()
	{
		return getOwner().getInventory().getItemByObjectId(_controlObjectId);
	}

	public int getCurrentFed()
	{
		return _curFed;
	}

	public void setCurrentFed(int num)
	{
		_curFed = num > getMaxFed() ? getMaxFed() : num;
	}

	/**
	 * Returns the pet's currently equipped weapon instance (if any).
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		for (L2ItemInstance item : getInventory().getItems())
		{
			if (item.getLocation() == L2ItemInstance.ItemLocation.PET_EQUIP &&
					item.getItem().getBodyPart() == L2Item.SLOT_R_HAND)
			{
				return item;
			}
		}

		return null;
	}

	/**
	 * Returns the pet's currently equipped weapon (if any).
	 */
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		L2ItemInstance weapon = getActiveWeaponInstance();

		if (weapon == null)
		{
			return null;
		}

		return (L2Weapon) weapon.getItem();
	}

	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		// temporary? unavailable
		return null;
	}

	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		// temporary? unavailable
		return null;
	}

	@Override
	public PetInventory getInventory()
	{
		return _inventory;
	}

	/**
	 * Destroys item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 *
	 * @param process     : String Identifier of process triggering this action
	 * @param objectId    : int Item Instance identifier of the item to be destroyed
	 * @param count       : int Quantity of items to be destroyed
	 * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	@Override
	public boolean destroyItem(String process, int objectId, long count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = _inventory.destroyItem(process, objectId, count, getOwner(), reference);
		if (item == null)
		{
			if (sendMessage)
			{
				getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
			}

			return false;
		}

		// Send Pet inventory update packet
		PetInventoryUpdate petIU = new PetInventoryUpdate();
		petIU.addItem(item);
		getOwner().sendPacket(petIU);

		if (sendMessage)
		{
			if (count > 1)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
				sm.addItemName(item.getItemId());
				sm.addItemNumber(count);
				getOwner().sendPacket(sm);
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
				sm.addItemName(item.getItemId());
				getOwner().sendPacket(sm);
			}
		}
		return true;
	}

	/**
	 * Destroy item from inventory by using its <B>itemId</B> and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 *
	 * @param process     : String Identifier of process triggering this action
	 * @param itemId      : int Item identifier of the item to be destroyed
	 * @param count       : int Quantity of items to be destroyed
	 * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	@Override
	public boolean destroyItemByItemId(String process, int itemId, long count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = _inventory.destroyItemByItemId(process, itemId, count, getOwner(), reference);

		if (item == null)
		{
			if (sendMessage)
			{
				getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
			}
			return false;
		}

		// Send Pet inventory update packet
		PetInventoryUpdate petIU = new PetInventoryUpdate();
		petIU.addItem(item);
		getOwner().sendPacket(petIU);

		if (sendMessage)
		{
			if (count > 1)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
				sm.addItemName(item.getItemId());
				sm.addItemNumber(count);
				getOwner().sendPacket(sm);
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
				sm.addItemName(item.getItemId());
				getOwner().sendPacket(sm);
			}
		}

		return true;
	}

	@Override
	protected void doPickupItem(L2Object object)
	{
		boolean follow = getFollowStatus();
		if (isDead())
		{
			return;
		}

		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		StopMove sm = new StopMove(getObjectId(), getX(), getY(), getZ(), getHeading());

		if (Config.DEBUG)
		{
			Log.fine("Pet pickup pos: " + object.getX() + " " + object.getY() + " " + object.getZ());
		}

		broadcastPacket(sm);

		if (!(object instanceof L2ItemInstance))
		{
			// dont try to pickup anything that is not an item :)
			Log.warning(this + " trying to pickup wrong target." + object);
			getOwner().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2ItemInstance target = (L2ItemInstance) object;

		// Cursed weapons
		if (CursedWeaponsManager.getInstance().isCursed(target.getItemId()))
		{
			SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
			smsg.addItemName(target.getItemId());
			getOwner().sendPacket(smsg);
			return;
		}

		synchronized (target)
		{
			if (!target.isVisible())
			{
				getOwner().sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (!_inventory.validateCapacity(target))
			{
				getOwner().sendPacket(
						SystemMessage.getSystemMessage(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS));
				return;
			}

			if (!_inventory.validateWeight(target, target.getCount()))
			{
				getOwner().sendPacket(SystemMessage
						.getSystemMessage(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED));
				return;
			}

			if (target.getOwnerId() != 0 && target.getOwnerId() != getOwner().getObjectId() &&
					!getOwner().isInLooterParty(target.getOwnerId()))
			{
				getOwner().sendPacket(ActionFailed.STATIC_PACKET);

				if (target.getItemId() == 57)
				{
					SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA);
					smsg.addItemNumber(target.getCount());
					getOwner().sendPacket(smsg);
				}
				else if (target.getCount() > 1)
				{
					SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S);
					smsg.addItemName(target.getItemId());
					smsg.addItemNumber(target.getCount());
					getOwner().sendPacket(smsg);
				}
				else
				{
					SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
					smsg.addItemName(target.getItemId());
					getOwner().sendPacket(smsg);
				}

				return;
			}
			if (target.getItemLootShedule() != null && (target.getOwnerId() == getOwner().getObjectId() ||
					getOwner().isInLooterParty(target.getOwnerId())))
			{
				target.resetOwnerTimer();
			}

			target.pickupMe(this);

			if (Config.SAVE_DROPPED_ITEM) // item must be removed from ItemsOnGroundManager if is active
			{
				ItemsOnGroundManager.getInstance().removeObject(target);
			}
		}

		// Herbs
		if (target.getItemType() == L2EtcItemType.HERB)
		{
			IItemHandler handler = ItemHandler.getInstance().getItemHandler(target.getEtcItem());
			if (handler == null)
			{
				Log.fine("No item handler registered for item ID " + target.getItemId() + ".");
			}
			else
			{
				handler.useItem(this, target, false);
			}

			ItemTable.getInstance().destroyItem("Consume", target, getOwner(), null);

			broadcastStatusUpdate();
		}
		else
		{
			if (target.getItemId() == 57)
			{
				SystemMessage sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1_ADENA);
				sm2.addItemNumber(target.getCount());
				getOwner().sendPacket(sm2);
			}
			else if (target.getEnchantLevel() > 0)
			{
				SystemMessage sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1_S2);
				sm2.addNumber(target.getEnchantLevel());
				sm2.addString(target.getName());
				getOwner().sendPacket(sm2);
			}
			else if (target.getCount() > 1)
			{
				SystemMessage sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S2_S1_S);
				sm2.addItemNumber(target.getCount());
				sm2.addString(target.getName());
				getOwner().sendPacket(sm2);
			}
			else
			{
				SystemMessage sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1);
				sm2.addString(target.getName());
				getOwner().sendPacket(sm2);
			}
			getInventory().addItem("Pickup", target, getOwner(), this);
			//FIXME Just send the updates if possible (old way wasn't working though)
			PetItemList iu = new PetItemList(this);
			getOwner().sendPacket(iu);
		}

		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

		if (follow)
		{
			followOwner();
		}
	}

	@Override
	public void deleteMe(L2PcInstance owner)
	{
		getInventory().transferItemsToOwner();
		super.deleteMe(owner);
		destroyControlItem(owner, false); //this should also delete the pet from the db
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer, true))
		{
			return false;
		}
		stopFeed();
		getOwner().sendPacket(
				SystemMessage.getSystemMessage(SystemMessageId.MAKE_SURE_YOU_RESSURECT_YOUR_PET_WITHIN_24_HOURS));
		DecayTaskManager.getInstance().addDecayTask(this, PET_DECAY_DELAY);
		// do not decrease exp if is in duel, arena
		L2PcInstance owner = getOwner();
		if (owner != null && !owner.isInDuel() && (!isInsideZone(ZONE_PVP) || isInsideZone(ZONE_SIEGE)))
		{
			deathPenalty();
		}
		return true;
	}

	@Override
	public void doRevive()
	{
		getOwner().removeReviving();

		super.doRevive();

		// stopDecay
		DecayTaskManager.getInstance().cancelDecayTask(this);
		startFeed();
		if (!isHungry())
		{
			setRunning();
		}
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
	}

	@Override
	public void doRevive(double revivePower)
	{
		// Restore the pet's lost experience,
		// depending on the % return of the skill used (based on its power).
		restoreExp(revivePower);
		doRevive();
	}

	/**
	 * Transfers item to another inventory
	 *
	 * @param process   : String Identifier of process triggering this action
	 * @param count     : int Quantity of items to be transfered
	 * @param actor     : L2PcInstance Player requesting the item transfer
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public L2ItemInstance transferItem(String process, int objectId, long count, Inventory target, L2PcInstance actor, L2Object reference)
	{
		if (target == null)
		{
			return null;
		}

		L2ItemInstance oldItem = getInventory().getItemByObjectId(objectId);
		L2ItemInstance playerOldItem = target.getItemByItemId(oldItem.getItemId());
		L2ItemInstance newItem = getInventory().transferItem(process, objectId, count, target, actor, reference);

		if (newItem == null)
		{
			return null;
		}

		// Send inventory update packet
		PetInventoryUpdate petIU = new PetInventoryUpdate();
		if (oldItem.getCount() > 0 && oldItem != newItem)
		{
			petIU.addModifiedItem(oldItem);
		}
		else
		{
			petIU.addRemovedItem(oldItem);
		}
		getOwner().sendPacket(petIU);

		// Send target update packet
		if (!newItem.isStackable())
		{
			InventoryUpdate iu = new InventoryUpdate();
			iu.addNewItem(newItem);
			getOwner().sendPacket(iu);
		}
		else if (playerOldItem != null && newItem.isStackable())
		{
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(newItem);
			getOwner().sendPacket(iu);
		}

		return newItem;
	}

	/**
	 * Remove the Pet from DB and its associated item from the player inventory
	 *
	 * @param owner The owner from whose invenory we should delete the item
	 */
	public void destroyControlItem(L2PcInstance owner, boolean evolve)
	{
		// remove the pet instance from world
		L2World.getInstance().removePet(owner.getObjectId());

		// delete from inventory
		try
		{
			L2ItemInstance removedItem;
			if (evolve)
			{
				removedItem = owner.getInventory().destroyItem("Evolve", getControlObjectId(), 1, getOwner(), this);
			}
			else
			{
				removedItem = owner.getInventory().destroyItem("PetDestroy", getControlObjectId(), 1, getOwner(), this);
				if (removedItem != null)
				{
					owner.sendPacket(
							SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(removedItem));
				}
			}

			if (removedItem == null)
			{
				Log.warning("Couldn't destroy pet control item for " + owner + " pet: " + this + " evolve: " + evolve);
			}
			else
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addRemovedItem(removedItem);

				owner.sendPacket(iu);

				StatusUpdate su = new StatusUpdate(owner);
				su.addAttribute(StatusUpdate.CUR_LOAD, owner.getCurrentLoad());
				owner.sendPacket(su);

				owner.broadcastUserInfo();

				L2World.getInstance().removeObject(removedItem);
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Error while destroying control item: " + e.getMessage(), e);
		}

		// pet control item no longer exists, delete the pet from the db
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
			statement.setInt(1, getControlObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed to delete Pet [ObjectId: " + getObjectId() + "]", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void dropAllItems()
	{
		try
		{
			for (L2ItemInstance item : getInventory().getItems())
			{
				dropItemHere(item);
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Pet Drop Error: " + e.getMessage(), e);
		}
	}

	public void dropItemHere(L2ItemInstance dropit)
	{
		dropit = getInventory().dropItem("Drop", dropit.getObjectId(), dropit.getCount(), getOwner(), this);

		if (dropit != null)
		{
			Log.finer("Item id to drop: " + dropit.getItemId() + " amount: " + dropit.getCount());
			dropit.dropMe(this, getX(), getY(), getZ() + 100);
		}
	}

	/**
	 * @return Returns the mount able.
	 */
	@Override
	public boolean isMountable()
	{
		return _mountable;
	}

	private static L2PetInstance restore(L2ItemInstance control, L2NpcTemplate template, L2PcInstance owner)
	{
		Connection con = null;
		try
		{
			L2PetInstance pet;
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"SELECT item_obj_id, name, level, curHp, curMp, exp, sp, fed FROM pets WHERE item_obj_id=?");
			statement.setInt(1, control.getObjectId());
			ResultSet rset = statement.executeQuery();
			if (!rset.next())
			{
				if (template.Type.compareToIgnoreCase("L2BabyPet") == 0)
				{
					pet = new L2BabyPetInstance(IdFactory.getInstance().getNextId(), template, owner, control);
				}
				else
				{
					pet = new L2PetInstance(IdFactory.getInstance().getNextId(), template, owner, control);
				}

				rset.close();
				statement.close();
				return pet;
			}

			if (template.Type.compareToIgnoreCase("L2BabyPet") == 0)
			{
				pet = new L2BabyPetInstance(IdFactory.getInstance().getNextId(), template, owner, control,
						rset.getByte("level"));
			}
			else
			{
				pet = new L2PetInstance(IdFactory.getInstance().getNextId(), template, owner, control,
						rset.getByte("level"));
			}

			pet._respawned = true;
			pet.setName(rset.getString("name"));

			long exp = rset.getLong("exp");
			L2PetLevelData info = PetDataTable.getInstance().getPetLevelData(pet.getNpcId(), pet.getLevel());
			// DS: update experience based by level
			// Avoiding pet delevels due to exp per level values changed.
			if (info != null && exp < info.getPetMaxExp())
			{
				exp = info.getPetMaxExp();
			}

			pet.getStat().setExp(exp);
			pet.getStat().setSp(rset.getLong("sp"));

			pet.getStatus().setCurrentHp(rset.getDouble("curHp"));
			pet.getStatus().setCurrentMp(rset.getDouble("curMp"));
			pet.getStatus().setCurrentCp(pet.getMaxCp());
			if (rset.getDouble("curHp") < 0.5)
			{
				pet.setIsDead(true);
				pet.stopHpMpRegeneration();
			}

			pet.setCurrentFed(rset.getInt("fed"));

			rset.close();
			statement.close();
			return pet;
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Could not restore pet data for owner: " + owner + " - " + e.getMessage(), e);
			return null;
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	@Override
	public void store()
	{
		if (getControlObjectId() == 0)
		{
			// this is a summon, not a pet, don't store anything
			return;
		}

		String req;
		if (!isRespawned())
		{
			req = "INSERT INTO pets (name,level,curHp,curMp,exp,sp,fed,item_obj_id) " + "VALUES (?,?,?,?,?,?,?,?)";
		}
		else
		{
			req = "UPDATE pets SET name=?,level=?,curHp=?,curMp=?,exp=?,sp=?,fed=? " + "WHERE item_obj_id = ?";
		}
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(req);
			statement.setString(1, getName());
			statement.setInt(2, getStat().getLevel());
			statement.setDouble(3, getStatus().getCurrentHp());
			statement.setDouble(4, getStatus().getCurrentMp());
			statement.setLong(5, getStat().getExp());
			statement.setLong(6, getStat().getSp());
			statement.setInt(7, getCurrentFed());
			statement.setInt(8, getControlObjectId());
			statement.executeUpdate();
			statement.close();
			_respawned = true;
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed to store Pet [ObjectId: " + getObjectId() + "] data", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		L2ItemInstance itemInst = getControlItem();
		if (itemInst != null && itemInst.getEnchantLevel() != getStat().getLevel())
		{
			itemInst.setEnchantLevel(getStat().getLevel());
			itemInst.updateDatabase();
		}
	}

	public synchronized void stopFeed()
	{
		if (_feedTask != null)
		{
			_feedTask.cancel(false);
			_feedTask = null;
			if (Config.DEBUG)
			{
				Log.fine("Pet [#" + getObjectId() + "] feed task stop");
			}
		}
	}

	public synchronized void startFeed()
	{
		// stop feeding task if its active

		stopFeed();
		if (!isDead() && getOwner().getPet() == this)
		{
			_feedTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FeedTask(), 10000, 10000);
		}
	}

	@Override
	public synchronized void unSummon(L2PcInstance owner)
	{
		stopFeed();
		stopHpMpRegeneration();
		super.unSummon(owner);

		if (!isDead())
		{
			if (getInventory() != null && owner.getPetInv() == null)
			{
				getInventory().deleteMe();
			}

			L2World.getInstance().removePet(owner.getObjectId());
		}
	}

	/**
	 * Restore the specified % of experience this L2PetInstance has lost.<BR><BR>
	 */
	public void restoreExp(double restorePercent)
	{
		if (_expBeforeDeath > 0)
		{
			// Restore the specified % of lost experience.
			getStat().addExp(Math.round((_expBeforeDeath - getStat().getExp()) * restorePercent / 100));
			_expBeforeDeath = 0;
		}
	}

	private void deathPenalty()
	{
		// TODO Need Correct Penalty

		int lvl = getStat().getLevel();
		double percentLost = -0.07 * lvl + 6.5;

		long levelExp = getStat().getExpForLevel(lvl);
		long nextLevelExp = getStat().getExpForLevel(lvl + 1);
		// Make sure the lost exp is positive
		if (nextLevelExp < levelExp)
		{
			nextLevelExp = levelExp;
		}
		// Calculate the Experience loss
		long lostExp = Math.round((nextLevelExp - levelExp) * percentLost / 100);

		// Get the Experience before applying penalty
		_expBeforeDeath = getStat().getExp();
		if (lostExp > _expBeforeDeath - levelExp)
		{
			lostExp = _expBeforeDeath - levelExp;
		}

		// Set the new Experience value of the L2PetInstance
		getStat().removeExp(lostExp);
	}

	@Override
	public void addExpAndSp(long addToExp, long addToSp)
	{
		if (getNpcId() == 12564) //SinEater
		{
			getStat().addExpAndSp(Math.round(addToExp * Config.SINEATER_XP_RATE), addToSp);
		}
		else
		{
			getStat().addExpAndSp(Math.round(addToExp * Config.PET_XP_RATE), addToSp);
		}
	}

	@Override
	public long getExpForThisLevel()
	{
		return getStat().getExpForLevel(getLevel());
	}

	@Override
	public long getExpForNextLevel()
	{
		return getStat().getExpForLevel(getLevel() + 1);
	}

	@Override
	public final int getLevel()
	{
		return getStat().getLevel();
	}

	public int getMaxFed()
	{
		return getStat().getMaxFeed();
	}

	@Override
	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getCriticalHit(target, skill);
	}

	@Override
	public int getMAtk(L2Character target, L2Skill skill)
	{
		return getStat().getMAtk(target, skill);
	}

	@Override
	public int getMDef(L2Character target, L2Skill skill)
	{
		return getStat().getMDef(target, skill);
	}

	@Override
	public final int getSkillLevelHash(int skillId)
	{
		if (getKnownSkill(skillId) == null)
		{
			return -1;
		}

		final int lvl = getLevel();
		return lvl > 70 ? 7 + (lvl - 70) / 5 : lvl / 10;
	}

	public void updateRefOwner(L2PcInstance owner)
	{
		int oldOwnerId = getOwner().getObjectId();

		setOwner(owner);
		L2World.getInstance().removePet(oldOwnerId);
		L2World.getInstance().addPet(oldOwnerId, this);
	}

	public int getCurrentLoad()
	{
		return _inventory.getTotalWeight();
	}

	@Override
	public final int getMaxLoad()
	{
		return getPetData().getLoad();
	}

	public int getInventoryLimit()
	{
		return Config.INVENTORY_MAXIMUM_PET;
	}

	public void refreshOverloaded()
	{
		int maxLoad = getMaxLoad();
		if (maxLoad > 0)
		{
			int weightproc = getCurrentLoad() * 1000 / maxLoad;
			int newWeightPenalty;
			if (weightproc < 500 || getOwner().getDietMode())
			{
				newWeightPenalty = 0;
			}
			else if (weightproc < 666)
			{
				newWeightPenalty = 1;
			}
			else if (weightproc < 800)
			{
				newWeightPenalty = 2;
			}
			else if (weightproc < 1000)
			{
				newWeightPenalty = 3;
			}
			else
			{
				newWeightPenalty = 4;
			}

			if (_curWeightPenalty != newWeightPenalty)
			{
				_curWeightPenalty = newWeightPenalty;
				if (newWeightPenalty > 0)
				{
					addSkill(SkillTable.getInstance().getInfo(4270, newWeightPenalty));
					setIsOverloaded(getCurrentLoad() >= maxLoad);
				}
				else
				{
					super.removeSkill(getKnownSkill(4270));
					setIsOverloaded(false);
				}
			}
		}
	}

	@Override
	public void updateAndBroadcastStatus(int val)
	{
		refreshOverloaded();
		super.updateAndBroadcastStatus(val);
	}

	@Override
	public final boolean isHungry()
	{
		return getCurrentFed() < getPetData().getHungry_limit() / 100f * getPetLevelData().getPetMaxFeed();
	}

	@Override
	public final int getWeapon()
	{
		L2ItemInstance weapon = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (weapon != null)
		{
			return weapon.getItemId();
		}
		return 0;
	}

	@Override
	public final int getArmor()
	{
		L2ItemInstance weapon = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		if (weapon != null)
		{
			return weapon.getItemId();
		}
		return 0;
	}

	public final int getJewel()
	{
		L2ItemInstance weapon = getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK);
		if (weapon != null)
		{
			return weapon.getItemId();
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.model.actor.L2Summon#getSoulShotsPerHit()
	 */
	@Override
	public short getSoulShotsPerHit()
	{
		return getPetLevelData().getPetSoulShot();
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.model.actor.L2Summon#getSpiritShotsPerHit()
	 */
	@Override
	public short getSpiritShotsPerHit()
	{
		return getPetLevelData().getPetSpiritShot();
	}

	@Override
	public void setName(String name)
	{
		L2ItemInstance controlItem = getControlItem();
		if (controlItem != null && controlItem.getCustomType2() == (name == null ? 1 : 0))
		{
			// name not set yet
			controlItem.setCustomType2(name != null ? 1 : 0);
			controlItem.updateDatabase();
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(controlItem);
			getOwner().sendPacket(iu);
		}
		super.setName(name);
	}

	@Override
	protected void broadcastModifiedStats(ArrayList<Stats> stats)
	{
		// check for initialization
		if (getInstanceType() == InstanceType.L2PetInstance)
		{
			super.broadcastModifiedStats(stats);
		}
	}

	public boolean canEatFoodId(int itemId)
	{
		return Util.contains(_data.getFood(), itemId);
	}
}
