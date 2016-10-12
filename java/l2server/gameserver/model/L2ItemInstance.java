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
import l2server.gameserver.GeoData;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.EnsoulDataTable;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.instancemanager.ItemsOnGroundManager;
import l2server.gameserver.instancemanager.MercTicketManager;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.knownlist.NullKnownList;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.network.serverpackets.L2ItemListPacket.ItemInstanceInfo;
import l2server.gameserver.stats.funcs.Func;
import l2server.gameserver.templates.item.*;
import l2server.gameserver.util.GMAudit;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import static l2server.gameserver.model.itemcontainer.PcInventory.ADENA_ID;
import static l2server.gameserver.model.itemcontainer.PcInventory.MAX_ADENA;

/**
 * This class manages items.
 *
 * @version $Revision: 1.4.2.1.2.11 $ $Date: 2005/03/31 16:07:50 $
 */
public final class L2ItemInstance extends L2Object implements ItemInstanceInfo
{
	/**
	 * Enumeration of locations for item
	 */
	public enum ItemLocation
	{
		VOID, INVENTORY, PAPERDOLL, WAREHOUSE, CLANWH, PET, PET_EQUIP, LEASE, REFUND, MAIL, AUCTION
	}

	/**
	 * ID of the owner
	 */
	private int _ownerId;

	/**
	 * ID of who dropped the item last, used for knownlist
	 */
	private int _dropperObjectId = 0;

	/**
	 * Quantity of the item
	 */
	private long _count;
	/**
	 * Initial Quantity of the item
	 */
	private long _initCount;
	/**
	 * Remaining time (in miliseconds)
	 */
	private long _time;
	/**
	 * Quantity of the item can decrease
	 */
	private boolean _decrease = false;

	/**
	 * ID of the item
	 */
	private final int _itemId;

	/**
	 * Object L2Item associated to the item
	 */
	private final L2Item _item;

	/**
	 * Location of the item : Inventory, PaperDoll, WareHouse
	 */
	private ItemLocation _loc;

	/**
	 * Slot where item is stored : Paperdoll slot, inventory order ...
	 */
	private int _locData;

	/**
	 * Level of enchantment of the item
	 */
	private int _enchantLevel;

	/**
	 * Wear Item
	 */
	private boolean _wear;

	/**
	 * Soul Crystal Enhancements
	 */
	private EnsoulEffect[] _ensoulEffects = new EnsoulEffect[3];

	/**
	 * Augmented Item
	 */
	private L2Augmentation _augmentation = null;

	/**
	 * Shadow item
	 */
	private int _mana = -1;
	private boolean _consumingMana = false;
	private static final int MANA_CONSUMPTION_RATE = 60000;

	/**
	 * Custom item types (used loto, race tickets)
	 */
	private int _type1;
	private int _type2;

	private long _dropTime;

	private boolean _published = false;

	public static final double CHARGED_NONE = 1.0;
	public static final double CHARGED_SOULSHOT = 2.0;
	public static final double CHARGED_SPIRITSHOT = 2.0;
	public static final double CHARGED_BLESSED_SPIRITSHOT = 4.0;

	/**
	 * Item charged with SoulShot (type of SoulShot)
	 */
	private double _chargedSoulshot = CHARGED_NONE;
	/**
	 * Item charged with SpiritShot (type of SpiritShot)
	 */
	private double _chargedSpiritshot = CHARGED_NONE;

	private boolean _chargedFishtshot = false;

	private boolean _protected;

	public static final int UNCHANGED = 0;
	public static final int ADDED = 1;
	public static final int REMOVED = 3;
	public static final int MODIFIED = 2;
	private int _lastChange = 2; //1 ??, 2 modified, 3 removed
	private boolean _existsInDb; // if a record exists in DB.
	private boolean _storedInDb; // if DB data is up-to-date.

	private final ReentrantLock _dbLock = new ReentrantLock();

	private Elementals[] _elementals = null;

	private ScheduledFuture<?> itemLootShedule = null;
	public ScheduledFuture<?> _lifeTimeTask;

	private int _mobId = 0;

	/**
	 * Constructor of the L2ItemInstance from the objectId and the itemId.
	 *
	 * @param objectId : int designating the ID of the object in the world
	 * @param itemId   : int designating the ID of the item
	 */
	public L2ItemInstance(int objectId, int itemId)
	{
		super(objectId);
		setInstanceType(InstanceType.L2ItemInstance);
		_itemId = itemId;
		_item = ItemTable.getInstance().getTemplate(itemId);
		if (_itemId == 0 || _item == null)
		{
			throw new IllegalArgumentException();
		}
		super.setName(_item.getName());
		setCount(1);
		_loc = ItemLocation.VOID;
		_type1 = 0;
		_type2 = 0;
		_dropTime = 0;
		_mana = _item.getDuration();
		_time = _item.getTime() == -1 ? -1 : System.currentTimeMillis() + (long) _item.getTime() * 60 * 1000;
		scheduleLifeTimeTask();
	}

	/**
	 * Constructor of the L2ItemInstance from the objetId and the description of the item given by the L2Item.
	 *
	 * @param objectId : int designating the ID of the object in the world
	 * @param item     : L2Item containing informations of the item
	 */
	public L2ItemInstance(int objectId, L2Item item, long time)
	{
		super(objectId);
		setInstanceType(InstanceType.L2ItemInstance);
		_itemId = item.getItemId();
		_item = item;
		if (_itemId == 0)
		{
			throw new IllegalArgumentException();
		}
		super.setName(_item.getName());
		setCount(1);
		_loc = ItemLocation.VOID;
		_mana = _item.getDuration();

		if (time != -1)
		{
			_time = time;
		}
		else
		{
			_time = _item.getTime() == -1 ? -1 : System.currentTimeMillis() + (long) _item.getTime() * 60 * 1000;
		}

		scheduleLifeTimeTask();
	}

	public L2ItemInstance(int objectId, L2Item item)
	{
		super(objectId);
		setInstanceType(InstanceType.L2ItemInstance);
		_itemId = item.getItemId();
		_item = item;
		if (_itemId == 0)
		{
			throw new IllegalArgumentException();
		}
		super.setName(_item.getName());
		setCount(1);
		_loc = ItemLocation.VOID;
		_mana = _item.getDuration();
		_time = _item.getTime() == -1 ? -1 : System.currentTimeMillis() + (long) _item.getTime() * 60 * 1000;
		scheduleLifeTimeTask();
	}

	@Override
	public void initKnownList()
	{
		setKnownList(new NullKnownList(this));
	}

	/**
	 * Remove a L2ItemInstance from the world and send server->client GetItem packets.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client Packet GetItem to player that pick up and its _knowPlayers member </li>
	 * <li>Remove the L2Object from the world</li><BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World </B></FONT><BR><BR>
	 * <p>
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> this instanceof L2ItemInstance</li>
	 * <li> _worldRegion != null <I>(L2Object is visible at the beginning)</I></li><BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Do Pickup Item : PCInstance and Pet</li><BR><BR>
	 *
	 * @param player Player that pick up the item
	 */
	public final void pickupMe(L2Character player)
	{
		assert getPosition().getWorldRegion() != null;

		L2WorldRegion oldregion = getPosition().getWorldRegion();

		// Create a server->client GetItem packet to pick up the L2ItemInstance
		GetItem gi = new GetItem(this, player.getObjectId());
		player.broadcastPacket(gi);

		synchronized (this)
		{
			setIsVisible(false);
			getPosition().setWorldRegion(null);
		}

		// if this item is a mercenary ticket, remove the spawns!
		int itemId = getItemId();

		if (MercTicketManager.getInstance().getTicketCastleId(itemId) > 0)
		{
			MercTicketManager.getInstance().removeTicket(this);
			ItemsOnGroundManager.getInstance().removeObject(this);
		}

		if (!Config.DISABLE_TUTORIAL && (itemId == 57 || itemId == 6353))
		{
			L2PcInstance actor = player.getActingPlayer();
			if (actor != null)
			{
				QuestState qs = actor.getQuestState("Q255_Tutorial");
				if (qs != null)
				{
					qs.getQuest().notifyEvent("CE" + itemId + "", null, actor);
				}
			}
		}
		// outside of synchronized to avoid deadlocks
		// Remove the L2ItemInstance from the world
		L2World.getInstance().removeVisibleObject(this, oldregion);
	}

	/**
	 * Sets the ownerID of the item
	 *
	 * @param process   : String Identifier of process triggering this action
	 * @param owner_id  : int designating the ID of the owner
	 * @param creator   : L2PcInstance Player requesting the item creation
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void setOwnerId(String process, int owner_id, L2PcInstance creator, Object reference)
	{
		setOwnerId(owner_id);

		if (Config.LOG_ITEMS && !process.contains("Consume"))
		{
			if (!Config.LOG_ITEMS_SMALL_LOG || Config.LOG_ITEMS_SMALL_LOG &&
					(getItem().isEquipable() || getItem().getItemId() == ADENA_ID || _item.getItemId() == 4037 ||
							_item.getItemId() == 4355 || _item.getItemId() == 4356))
			{
				logItem(getItemId(), getObjectId(), getCount(), owner_id, process);
			}
		}

		if (creator != null && creator.isGM())
		{
			String referenceName = "no-reference";
			if (reference instanceof L2Object)
			{
				referenceName = ((L2Object) reference).getName() != null ? ((L2Object) reference).getName() : "no-name";
			}
			else if (reference instanceof String)
			{
				referenceName = (String) reference;
			}
			String targetName = creator.getTarget() != null ? creator.getTarget().getName() : "no-target";
			if (Config.GMAUDIT)
			{
				GMAudit.auditGMAction(creator.getName(),
						process + " (obj id: " + getObjectId() + " receipt id: " + owner_id + " id: " + getItemId() +
								" count: " + getCount() + " name: " + getName() + ")", targetName,
						"L2Object referencing this action is: " + referenceName);
			}
		}
	}

	/**
	 * Sets the ownerID of the item
	 *
	 * @param owner_id : int designating the ID of the owner
	 */
	public void setOwnerId(int owner_id)
	{
		if (owner_id == _ownerId)
		{
			return;
		}

		_ownerId = owner_id;
		_storedInDb = false;
	}

	/**
	 * Returns the ownerID of the item
	 *
	 * @return int : ownerID of the item
	 */
	public int getOwnerId()
	{
		return _ownerId;
	}

	/**
	 * Sets the location of the item
	 *
	 * @param loc : ItemLocation (enumeration)
	 */
	public void setLocation(ItemLocation loc)
	{
		setLocation(loc, 0);
	}

	/**
	 * Sets the location of the item.<BR><BR>
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 *
	 * @param loc      : ItemLocation (enumeration)
	 * @param loc_data : int designating the slot where the item is stored or the village for freights
	 */
	public void setLocation(ItemLocation loc, int loc_data)
	{
		if (loc == _loc && loc_data == _locData)
		{
			return;
		}
		_loc = loc;
		_locData = loc_data;
		_storedInDb = false;
	}

	public ItemLocation getLocation()
	{
		return _loc;
	}

	/**
	 * Sets the quantity of the item.<BR><BR>
	 *
	 * @param count the new count to set
	 */
	public void setCount(long count)
	{
		if (getCount() == count)
		{
			return;
		}

		_count = count >= -1 ? count : 0;
		_storedInDb = false;
	}

	/**
	 * @return Returns the count.
	 */
	@Override
	public long getCount()
	{
		return _count;
	}

	public static void logItem(int itemId, int objectId, long count, int ownerId, String process)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"INSERT IGNORE INTO log_items(owner_id, item_id, item_object_id, count, process, time) VALUES(?,?,?,?,?,?)");
			statement.setInt(1, ownerId);
			statement.setInt(2, itemId);
			statement.setInt(3, objectId);
			statement.setLong(4, count);
			statement.setString(5, process);
			statement.setLong(6, System.currentTimeMillis());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Sets the quantity of the item.<BR><BR>
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 *
	 * @param process   : String Identifier of process triggering this action
	 * @param count     : int
	 * @param creator   : L2PcInstance Player requesting the item creation
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void changeCount(String process, long count, L2PcInstance creator, Object reference)
	{
		if (count == 0)
		{
			return;
		}
		long old = getCount();
		//long max = getItemId() == ADENA_ID ? MAX_ADENA : Integer.MAX_VALUE;
		long max = MAX_ADENA;

		if (count > 0 && getCount() > max - count)
		{
			setCount(max);
		}
		else
		{
			setCount(getCount() + count);
		}

		if (getCount() < 0)
		{
			setCount(0);
		}

		_storedInDb = false;

		if (Config.LOG_ITEMS && process != null && !process.contains("Consume"))
		{
			if (!Config.LOG_ITEMS_SMALL_LOG || Config.LOG_ITEMS_SMALL_LOG &&
					(_item.isEquipable() || _item.getItemId() == ADENA_ID || _item.getItemId() == 4037 ||
							_item.getItemId() == 4355 || _item.getItemId() == 4356))
			{
				logItem(getItemId(), getObjectId(), count, creator != null ? creator.getObjectId() : 0,
						process + " (" + old + "->" + getCount() + ")");
			}
		}

		if (creator != null)
		{
			if (getOwnerId() != creator.getObjectId())
			{
				//Broadcast.toGameMasters("Found " + getName() + " with diff oid, " + getOwnerId() + " VS " + creator.getObjectId());
			}

			if (creator.isGM())
			{
				String referenceName = "no-reference";
				if (reference instanceof L2Object)
				{
					referenceName =
							((L2Object) reference).getName() != null ? ((L2Object) reference).getName() : "no-name";
				}
				else if (reference instanceof String)
				{
					referenceName = (String) reference;
				}
				String targetName = creator.getTarget() != null ? creator.getTarget().getName() : "no-target";
				if (Config.GMAUDIT)
				{
					GMAudit.auditGMAction(creator.getName(),
							process + " (id: " + getItemId() + " objId: " + getObjectId() + " name: " + getName() +
									" count: " + count + ")", targetName,
							"L2Object referencing this action is: " + referenceName);
				}
			}
		}
	}

	// No logging (function designed for shots only)
	public void changeCountWithoutTrace(int count, L2PcInstance creator, Object reference)
	{
		changeCount(null, count, creator, reference);
	}

	/**
	 * Returns if item is equipable
	 *
	 * @return boolean
	 */
	public boolean isEquipable()
	{
		return !(_item.getBodyPart() == 0 || _item.getItemType() == L2EtcItemType.LURE);
	}

	/**
	 * Returns if item is equipped
	 *
	 * @return boolean
	 */
	@Override
	public boolean isEquipped()
	{
		return _loc == ItemLocation.PAPERDOLL || _loc == ItemLocation.PET_EQUIP;
	}

	/**
	 * Returns the slot where the item is stored
	 *
	 * @return int
	 */
	@Override
	public int getLocationSlot()
	{
		assert _loc == ItemLocation.PAPERDOLL || _loc == ItemLocation.PET_EQUIP || _loc == ItemLocation.INVENTORY ||
				_loc == ItemLocation.MAIL;
		return _locData;
	}

	/**
	 * Returns the characteristics of the item
	 *
	 * @return L2Item
	 */
	@Override
	public L2Item getItem()
	{
		return _item;
	}

	public int getCustomType1()
	{
		return _type1;
	}

	public int getCustomType2()
	{
		return _type2;
	}

	public void setCustomType1(int newtype)
	{
		_type1 = newtype;
	}

	public void setCustomType2(int newtype)
	{
		_type2 = newtype;
	}

	public void setDropTime(long time)
	{
		_dropTime = time;
	}

	public long getDropTime()
	{
		return _dropTime;
	}

	/**
	 * Returns the type of item
	 *
	 * @return Enum
	 */
	public L2ItemType getItemType()
	{
		return _item.getItemType();
	}

	/**
	 * Returns the ID of the item
	 *
	 * @return int
	 */
	public int getItemId()
	{
		return _itemId;
	}

	/**
	 * Returns true if item is an EtcItem
	 *
	 * @return boolean
	 */
	public boolean isEtcItem()
	{
		return _item instanceof L2EtcItem;
	}

	/**
	 * Returns true if item is a Weapon/Shield
	 *
	 * @return boolean
	 */
	public boolean isWeapon()
	{
		return _item instanceof L2Weapon;
	}

	/**
	 * Returns true if item is an Armor
	 *
	 * @return boolean
	 */
	public boolean isArmor()
	{
		return _item instanceof L2Armor;
	}

	/**
	 * Returns the characteristics of the L2EtcItem
	 *
	 * @return L2EtcItem
	 */
	public L2EtcItem getEtcItem()
	{
		if (_item instanceof L2EtcItem)
		{
			return (L2EtcItem) _item;
		}
		return null;
	}

	/**
	 * Returns the characteristics of the L2Weapon
	 *
	 * @return L2Weapon
	 */
	public L2Weapon getWeaponItem()
	{
		if (_item instanceof L2Weapon)
		{
			return (L2Weapon) _item;
		}
		return null;
	}

	/**
	 * Returns the characteristics of the L2Armor
	 *
	 * @return L2Armor
	 */
	public L2Armor getArmorItem()
	{
		if (_item instanceof L2Armor)
		{
			return (L2Armor) _item;
		}
		return null;
	}

	/**
	 * Returns the quantity of crystals for crystallization
	 *
	 * @return int
	 */
	public final int getCrystalCount()
	{
		return _item.getCrystalCount(_enchantLevel);
	}

	/**
	 * Returns the reference price of the item
	 *
	 * @return int
	 */
	public int getReferencePrice()
	{
		return _item.getReferencePrice();
	}

	/**
	 * Returns the name of the item
	 *
	 * @return String
	 */
	public String getItemName()
	{
		return _item.getName();
	}

	/**
	 * Returns the last change of the item
	 *
	 * @return int
	 */
	public int getLastChange()
	{
		return _lastChange;
	}

	/**
	 * Sets the last change of the item
	 *
	 * @param lastChange : int
	 */
	public void setLastChange(int lastChange)
	{
		_lastChange = lastChange;
	}

	/**
	 * Returns if item is stackable
	 *
	 * @return boolean
	 */
	public boolean isStackable()
	{
		return _item.isStackable();
	}

	/**
	 * Returns if item is dropable
	 *
	 * @return boolean
	 */
	public boolean isDropable()
	{
		return !isAugmented() && _item.isDropable();
	}

	/**
	 * Returns if item is destroyable
	 *
	 * @return boolean
	 */
	public boolean isDestroyable()
	{
		if (getTime() != -1)
		{
			return false;
		}

		return _item.isDestroyable();
	}

	/**
	 * Returns if item is tradeable
	 *
	 * @return boolean
	 */
	public boolean isTradeable()
	{
		if (getTime() != -1)
		{
			return false;
		}

		return !isAugmented() && _item.isTradeable();
	}

	/**
	 * Returns if item is sellable
	 *
	 * @return boolean
	 */
	public boolean isSellable()
	{
		if (getTime() != -1)
		{
			return false;
		}

		return !isAugmented() && _item.isSellable();
	}

	/**
	 * Returns if item can be deposited in warehouse or freight
	 *
	 * @return boolean
	 */
	public boolean isDepositable(boolean isPrivateWareHouse)
	{
		if (getTime() != -1)
		{
			return false;
		}

		// equipped, hero and quest items
		if (isEquipped() || !_item.isDepositable())
		{
			return false;
		}
		if (!isPrivateWareHouse)
		{
			// augmented not tradeable
			if (!isTradeable() || isShadowItem())
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns if item is consumable
	 *
	 * @return boolean
	 */
	public boolean isConsumable()
	{
		return _item.isConsumable();
	}

	public boolean isPotion()
	{
		return _item.isPotion();
	}

	public boolean isElixir()
	{
		return _item.isElixir();
	}

	public boolean isHeroItem()
	{
		return _item.isHeroItem();
	}

	public boolean isCommonItem()
	{
		return _item.isCommon();
	}

	/**
	 * Returns whether this item is pvp or not
	 *
	 * @return boolean
	 */
	public boolean isPvp()
	{
		return _item.isPvpItem();
	}

	/**
	 * Returns if item is available for manipulation
	 *
	 * @return boolean
	 */
	public boolean isAvailable(L2PcInstance player, boolean allowAdena, boolean allowNonTradeable)
	{
		return !isEquipped() // Not equipped
				&& getItem().getType2() != L2Item.TYPE2_QUEST // Not Quest Item
				&& (getItem().getType2() != L2Item.TYPE2_MONEY || getItem().getType1() != L2Item.TYPE1_SHIELD_ARMOR)
				// not money, not shield
				&& (player.getPet() == null || getObjectId() != player.getPet().getControlObjectId())
				// Not Control item of currently summoned pet
				&& player.getActiveEnchantItem() != this // Not momentarily used enchant scroll
				&& (allowAdena || getItemId() != 57) // Not adena
				&& (player.getCurrentSkill() == null ||
				player.getCurrentSkill().getSkill().getItemConsumeId() != getItemId()) &&
				(!player.isCastingSimultaneouslyNow() || player.getLastSimultaneousSkillCast() == null ||
						player.getLastSimultaneousSkillCast().getItemConsumeId() != getItemId()) &&
				(allowNonTradeable || isTradeable() &&
						!(getItem().getItemType() == L2EtcItemType.PET_COLLAR && player.havePetInvItems()));
	}

	/**
	 * Returns the level of enchantment of the item
	 *
	 * @return int
	 */
	@Override
	public int getEnchantLevel()
	{
		return _enchantLevel;
	}

	/**
	 * Sets the level of enchantment of the item
	 */
	public void setEnchantLevel(int enchantLevel)
	{
		if (_enchantLevel == enchantLevel)
		{
			return;
		}
		_enchantLevel = enchantLevel;
		_storedInDb = false;
	}

	/**
	 * Returns whether this item is augmented or not
	 *
	 * @return true if augmented
	 */
	@Override
	public boolean isSoulEnhanced()
	{
		for (EnsoulEffect e : _ensoulEffects)
		{
			if (e != null)
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns the augmentation object for this item
	 *
	 * @return augmentation
	 */
	public EnsoulEffect[] getEnsoulEffects()
	{
		return _ensoulEffects;
	}

	/**
	 * Returns the augmentation object for this item
	 *
	 * @return augmentation
	 */
	@Override
	public int[] getEnsoulEffectIds()
	{
		int effectCount = 0;
		if (_ensoulEffects[0] != null)
		{
			effectCount++;
		}
		if (_ensoulEffects[1] != null)
		{
			effectCount++;
		}

		int[] effects = new int[effectCount];
		int index = 0;
		if (_ensoulEffects[0] != null)
		{
			effects[index++] = _ensoulEffects[0].getId();
		}
		if (_ensoulEffects[1] != null)
		{
			effects[index++] = _ensoulEffects[1].getId();
		}

		return effects;
	}

	/**
	 * Returns the augmentation object for this item
	 *
	 * @return augmentation
	 */
	@Override
	public int[] getEnsoulSpecialEffectIds()
	{
		if (_ensoulEffects[2] == null)
		{
			return new int[]{};
		}

		return new int[]{_ensoulEffects[2].getId()};
	}

	/**
	 * Sets a new ensoul effect
	 *
	 * @param effect
	 * @return return true if sucessful
	 */
	public boolean setEnsoulEffect(int index, EnsoulEffect effect)
	{
		// there shall be no previous effect..?
		//if (_ensoulEffects[index] != null)
		//	return false;

		_ensoulEffects[index] = effect;
		updateItemEnsoulEffects(null);
		return true;
	}

	/**
	 * Remove the ensoul effect
	 */
	public void removeEnsoulEffects()
	{
		if (_ensoulEffects == null)
		{
			return;
		}

		_ensoulEffects = new EnsoulEffect[3];
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = null;
			// Remove the entry
			statement = con.prepareStatement("DELETE FROM item_ensoul_effects WHERE itemId = ?");

			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Could not remove ensoul effect for item: " + this + " from DB:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Returns whether this item is augmented or not
	 *
	 * @return true if augmented
	 */
	@Override
	public boolean isAugmented()
	{
		return _augmentation != null;
	}

	/**
	 * Returns the augmentation object for this item
	 *
	 * @return augmentation
	 */
	public L2Augmentation getAugmentation()
	{
		return _augmentation;
	}

	/**
	 * Returns the augmentation bonus for this item
	 *
	 * @return augmentation id
	 */
	@Override
	public long getAugmentationBonus()
	{
		return _augmentation.getId();
	}

	/**
	 * Sets a new augmentation
	 *
	 * @param augmentation
	 * @return return true if sucessfull
	 */
	public boolean setAugmentation(L2Augmentation augmentation)
	{
		// there shall be no previous augmentation..
		if (_augmentation != null)
		{
			return false;
		}

		_augmentation = augmentation;
		updateItemAttributes(null);
		return true;
	}

	/**
	 * Remove the augmentation
	 */
	public void removeAugmentation()
	{
		if (_augmentation == null)
		{
			return;
		}

		_augmentation = null;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = null;
			// Remove the entry
			statement = con.prepareStatement("DELETE FROM item_attributes WHERE itemId = ?");

			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Could not remove augmentation for item: " + this + " from DB:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void restoreAttributes()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("SELECT effectIndex, effectId FROM item_ensoul_effects WHERE itemId=?");
			statement.setInt(1, getObjectId());
			ResultSet rs = statement.executeQuery();
			while (rs.next())
			{
				int effectIndex = rs.getInt(1);
				int effectId = rs.getInt(2);
				if (effectIndex >= 0 && effectIndex < _ensoulEffects.length && effectId > 0)
				{
					_ensoulEffects[effectIndex] = EnsoulDataTable.getInstance().getEffect(effectId);
				}
			}
			rs.close();
			statement.close();

			statement = con.prepareStatement("SELECT augAttributes FROM item_attributes WHERE itemId=?");
			statement.setInt(1, getObjectId());
			rs = statement.executeQuery();
			if (rs.next())
			{
				long augAttributes = rs.getLong(1);
				if (augAttributes > 0)
				{
					_augmentation = new L2Augmentation(augAttributes);
				}
			}
			rs.close();
			statement.close();

			statement = con.prepareStatement("SELECT elemType,elemValue FROM item_elementals WHERE itemId=?");
			statement.setInt(1, getObjectId());
			rs = statement.executeQuery();
			while (rs.next())
			{
				byte elem_type = rs.getByte(1);
				int elem_value = rs.getInt(2);
				if (elem_type != -1 && elem_value != -1)
				{
					applyAttribute(elem_type, elem_value);
				}
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Could not restore augmentation and elemental data for item " + this + " from DB: " +
					e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void updateItemEnsoulEffects(Connection pooledCon)
	{
		Connection con = null;
		try
		{
			con = pooledCon == null ? L2DatabaseFactory.getInstance().getConnection() : pooledCon;
			PreparedStatement statement = con.prepareStatement("REPLACE INTO item_ensoul_effects VALUES(?,?,?)");
			for (int i = 0; i < _ensoulEffects.length; i++)
			{
				statement.setInt(1, getObjectId());
				statement.setLong(2, i);
				statement.setLong(3, _ensoulEffects[i] != null ? _ensoulEffects[i].getId() : 0);
				statement.executeUpdate();
			}

			statement.close();
		}
		catch (SQLException e)
		{
			Log.log(Level.SEVERE, "Could not update ensoul effects for item: " + this + " from DB:", e);
		}
		finally
		{
			if (pooledCon == null)
			{
				L2DatabaseFactory.close(con);
			}
		}
	}

	private void updateItemAttributes(Connection pooledCon)
	{
		Connection con = null;
		try
		{
			con = pooledCon == null ? L2DatabaseFactory.getInstance().getConnection() : pooledCon;
			PreparedStatement statement = con.prepareStatement("REPLACE INTO item_attributes VALUES(?,?)");
			statement.setInt(1, getObjectId());
			statement.setLong(2, _augmentation != null ? _augmentation.getId() : 0);
			statement.executeUpdate();
			statement.close();
		}
		catch (SQLException e)
		{
			Log.log(Level.SEVERE, "Could not update atributes for item: " + this + " from DB:", e);
		}
		finally
		{
			if (pooledCon == null)
			{
				L2DatabaseFactory.close(con);
			}
		}
	}

	private void updateItemElements(Connection pooledCon)
	{
		Connection con = null;
		try
		{
			con = pooledCon == null ? L2DatabaseFactory.getInstance().getConnection() : pooledCon;
			PreparedStatement statement = con.prepareStatement("DELETE FROM item_elementals WHERE itemId = ?");
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			statement.close();

			if (_elementals == null)
			{
				return;
			}

			statement = con.prepareStatement("INSERT INTO item_elementals VALUES(?,?,?)");

			for (Elementals elm : _elementals)
			{
				statement.setInt(1, getObjectId());
				statement.setByte(2, elm.getElement());
				statement.setInt(3, elm.getValue());
				statement.executeUpdate();
				statement.clearParameters();
			}

			statement.close();
		}
		catch (SQLException e)
		{
			Log.log(Level.SEVERE, "Could not update elementals for item: " + this + " from DB:", e);
		}
		finally
		{
			if (pooledCon == null)
			{
				L2DatabaseFactory.close(con);
			}
		}
	}

	public Elementals[] getElementals()
	{
		return _elementals;
	}

	@Override
	public boolean isElementEnchanted()
	{
		return _elementals != null || getItem().getElementals() != null;
	}

	public Elementals getElemental(byte attribute)
	{
		if (_elementals == null)
		{
			return null;
		}
		for (Elementals elm : _elementals)
		{
			if (elm.getElement() == attribute)
			{
				return elm;
			}
		}
		return null;
	}

	@Override
	public byte getAttackElementType()
	{
		if (!isWeapon())
		{
			return -2;
		}
		else if (getItem().getElementals() != null)
		{
			return getItem().getElementals()[0].getElement();
		}
		else if (_elementals != null)
		{
			return _elementals[0].getElement();
		}
		return -2;
	}

	@Override
	public int getAttackElementPower()
	{
		if (!isWeapon())
		{
			return 0;
		}
		else if (getItem().getElementals() != null)
		{
			return getItem().getElementals()[0].getValue();
		}
		else if (_elementals != null)
		{
			return _elementals[0].getValue();
		}
		return 0;
	}

	@Override
	public int getElementDefAttr(byte element)
	{
		if (!isArmor())
		{
			return 0;
		}
		else if (getItem().getElementals() != null)
		{
			Elementals elm = getItem().getElemental(element);
			if (elm != null)
			{
				return elm.getValue();
			}
		}
		else if (_elementals != null)
		{
			Elementals elm = getElemental(element);
			if (elm != null)
			{
				return elm.getValue();
			}
		}
		return 0;
	}

	private void applyAttribute(byte element, int value)
	{
		if (_elementals == null)
		{
			_elementals = new Elementals[1];
			_elementals[0] = new Elementals(element, value);
		}
		else
		{
			Elementals elm = getElemental(element);
			if (elm != null)
			{
				elm.setValue(value);
			}
			else
			{
				elm = new Elementals(element, value);
				Elementals[] array = new Elementals[_elementals.length + 1];
				System.arraycopy(_elementals, 0, array, 0, _elementals.length);
				array[_elementals.length] = elm;
				_elementals = array;
			}
		}
	}

	public void changeAttribute(byte element, int value)
	{
		if (_elementals == null)
		{
			_elementals = new Elementals[1];
			_elementals[0] = new Elementals(element, value);
		}
		else
		{
			Elementals elm = getElemental(element);
			if (elm != null)
			{
				elm.setValue(value);
			}
			else
			{
				_elementals = new Elementals[1];
				_elementals[0] = new Elementals(element, value);
			}
		}
		updateItemElements(null);
	}

	/**
	 * Add elemental attribute to item and save to db
	 *
	 * @param element
	 * @param value
	 */
	public void setElementAttr(byte element, int value)
	{
		applyAttribute(element, value);
		updateItemElements(null);
	}

	/**
	 * Remove elemental from item
	 *
	 * @param element byte element to remove, -1 for all elementals remove
	 */
	public void clearElementAttr(byte element)
	{
		if (getElemental(element) == null && element != -1)
		{
			return;
		}

		Elementals[] array = null;
		if (element != -1 && _elementals != null && _elementals.length > 1)
		{
			array = new Elementals[_elementals.length - 1];
			int i = 0;
			for (Elementals elm : _elementals)
			{
				if (elm.getElement() != element)
				{
					array[i++] = elm;
				}
			}
		}
		_elementals = array;

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = null;
			if (element != -1)
			{
				//Item can have still others
				statement = con.prepareStatement("DELETE FROM item_elementals WHERE itemId = ? AND elemType = ?");
				statement.setInt(2, element);
			}
			else
			{
				// Remove the entries
				statement = con.prepareStatement("DELETE FROM item_elementals WHERE itemId = ?");
			}

			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Could not remove elemental enchant for item: " + this + " from DB:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Used to decrease mana
	 * (mana means life time for shadow items)
	 */
	public static class ScheduleConsumeManaTask implements Runnable
	{
		private final L2ItemInstance _shadowItem;

		public ScheduleConsumeManaTask(L2ItemInstance item)
		{
			_shadowItem = item;
		}

		@Override
		public void run()
		{
			try
			{
				// decrease mana
				if (_shadowItem != null)
				{
					_shadowItem.decreaseMana(true);
				}
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "", e);
			}
		}
	}

	/**
	 * Returns true if this item is a shadow item
	 * Shadow items have a limited life-time
	 *
	 * @return
	 */
	public boolean isShadowItem()
	{
		return _mana >= 0;
	}

	/**
	 * Returns the remaining mana of this shadow item
	 *
	 * @return lifeTime
	 */
	@Override
	public int getMana()
	{
		return _mana;
	}

	/**
	 * Decreases the mana of this shadow item,
	 * sends a inventory update
	 * schedules a new consumption task if non is running
	 * optionally one could force a new task
	 *
	 * @param resetConsumingMana a new consumption task if item is equipped
	 */
	public void decreaseMana(boolean resetConsumingMana)
	{
		decreaseMana(resetConsumingMana, 1);
	}

	/**
	 * Decreases the mana of this shadow item,
	 * sends a inventory update
	 * schedules a new consumption task if non is running
	 * optionally one could force a new task
	 *
	 * @param resetConsumingMana a new consumption task if item is equipped
	 * @param count              how much mana decrease
	 */
	public void decreaseMana(boolean resetConsumingMana, int count)
	{
		if (!isShadowItem())
		{
			return;
		}

		if (_mana - count >= 0)
		{
			_mana -= count;
		}
		else
		{
			_mana = 0;
		}

		if (_storedInDb)
		{
			_storedInDb = false;
		}
		if (resetConsumingMana)
		{
			_consumingMana = false;
		}

		final L2PcInstance player = L2World.getInstance().getPlayer(getOwnerId());
		if (player != null)
		{
			SystemMessage sm;
			switch (_mana)
			{
				case 10:
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_10);
					sm.addItemName(_item);
					player.sendPacket(sm);
					break;
				case 5:
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_5);
					sm.addItemName(_item);
					player.sendPacket(sm);
					break;
				case 1:
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_1);
					sm.addItemName(_item);
					player.sendPacket(sm);
					break;
			}

			if (_mana == 0) // The life time has expired
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_0);
				sm.addItemName(_item);
				player.sendPacket(sm);

				// unequip
				if (isEquipped())
				{
					L2ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(getLocationSlot());
					InventoryUpdate iu = new InventoryUpdate();
					for (L2ItemInstance item : unequiped)
					{
						player.checkSShotsMatch(null, item);
						iu.addModifiedItem(item);
					}
					player.sendPacket(iu);
					player.broadcastUserInfo();
				}

				if (getLocation() != ItemLocation.WAREHOUSE)
				{
					// destroy
					player.getInventory().destroyItem("L2ItemInstance", this, player, null);

					// send update
					InventoryUpdate iu = new InventoryUpdate();
					iu.addRemovedItem(this);
					player.sendPacket(iu);

					StatusUpdate su = new StatusUpdate(player);
					su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
					player.sendPacket(su);
				}
				else
				{
					player.getWarehouse().destroyItem("L2ItemInstance", this, player, null);
				}

				// delete from world
				L2World.getInstance().removeObject(this);
			}
			else
			{
				// Reschedule if still equipped
				if (!_consumingMana && isEquipped())
				{
					scheduleConsumeManaTask();
				}
				if (getLocation() != ItemLocation.WAREHOUSE)
				{
					InventoryUpdate iu = new InventoryUpdate();
					iu.addModifiedItem(this);
					player.sendPacket(iu);
				}
			}
		}
	}

	public void scheduleConsumeManaTask()
	{
		if (_consumingMana)
		{
			return;
		}
		_consumingMana = true;
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleConsumeManaTask(this), MANA_CONSUMPTION_RATE);
	}

	private int _appearance;

	public void setAppearance(int appearance)
	{
		_appearance = appearance;
		_storedInDb = false;
	}

	@Override
	public int getAppearance()
	{
		return _appearance;
	}

	/**
	 * Returns false cause item can't be attacked
	 *
	 * @return boolean false
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	/**
	 * Returns the type of charge with SoulShot of the item.
	 *
	 * @return int (CHARGED_NONE, CHARGED_SOULSHOT)
	 */
	public double getChargedSoulShot()
	{
		return _chargedSoulshot;
	}

	/**
	 * Returns the type of charge with SpiritShot of the item
	 *
	 * @return int (CHARGED_NONE, CHARGED_SPIRITSHOT, CHARGED_BLESSED_SPIRITSHOT)
	 */
	public double getChargedSpiritShot()
	{
		return _chargedSpiritshot;
	}

	public boolean getChargedFishshot()
	{
		return _chargedFishtshot;
	}

	/**
	 * Sets the type of charge with SoulShot of the item
	 *
	 * @param type : int (CHARGED_NONE, CHARGED_SOULSHOT)
	 */
	public void setChargedSoulShot(double type)
	{
		_chargedSoulshot = type;
	}

	/**
	 * Sets the type of charge with SpiritShot of the item
	 *
	 * @param type : int (CHARGED_NONE, CHARGED_SPIRITSHOT, CHARGED_BLESSED_SPIRITSHOT)
	 */
	public void setChargedSpiritShot(double type)
	{
		_chargedSpiritshot = type;
	}

	public void setChargedFishshot(boolean type)
	{
		_chargedFishtshot = type;
	}

	/**
	 * This function basically returns a set of functions from
	 * L2Item/L2Armor/L2Weapon, but may add additional
	 * functions, if this particular item instance is enhanched
	 * for a particular player.
	 *
	 * @return Func[]
	 */
	public Func[] getStatFuncs()
	{
		return getItem().getStatFuncs(this);
	}

	/**
	 * Updates the database.<BR>
	 */
	public void updateDatabase()
	{
		updateDatabase(false);
	}

	/**
	 * Updates the database.<BR>
	 *
	 * @param force if the update should necessarilly be done.
	 */
	public void updateDatabase(boolean force)
	{
		_dbLock.lock();

		try
		{
			if (_existsInDb)
			{
				if (_ownerId == 0 || _loc == ItemLocation.VOID || _loc == ItemLocation.REFUND ||
						getCount() == 0 && _loc != ItemLocation.LEASE)
				{
					removeFromDb();

					/*
					if (getCount() != 0)
					{
						Broadcast.toGameMasters("(1) Deleted " + getCount() + " " + getName() + " from DB because... ");

						if (_ownerId == 0)
							Broadcast.toGameMasters("OwnerId = 0");
						if (_loc == ItemLocation.VOID || _loc == ItemLocation.REFUND)
							Broadcast.toGameMasters("Location = " + _loc);
						if (getCount() == 0 && _loc != ItemLocation.LEASE)
							Broadcast.toGameMasters("Count = 0 & Loc != LEASE");
					}*/
				}
				else if (!Config.LAZY_ITEMS_UPDATE || force)
				{
					updateInDb();
				}
			}
			else
			{
				if (_ownerId == 0 || _loc == ItemLocation.VOID || _loc == ItemLocation.REFUND ||
						getCount() == 0 && _loc != ItemLocation.LEASE)
				{
                    /*
					if (getCount() != 0)
					{
						Broadcast.toGameMasters("(2) Deleted " + getCount() + " " + getName() + " from DB because... ");

						if (_ownerId == 0)
							Broadcast.toGameMasters("OwnerId = 0");
						if (_loc == ItemLocation.VOID || _loc == ItemLocation.REFUND)
							Broadcast.toGameMasters("Location = " + _loc);
						if (getCount() == 0 && _loc != ItemLocation.LEASE)
							Broadcast.toGameMasters("Count = 0 & Loc != LEASE");
					}*/

					return;
				}
				insertIntoDb();
			}
		}
		finally
		{
			_dbLock.unlock();
		}
	}

	/**
	 * Returns a L2ItemInstance stored in database from its objectID
	 *
	 * @return L2ItemInstance
	 */
	public static L2ItemInstance restoreFromDb(int ownerId, ResultSet rs)
	{
		L2ItemInstance inst = null;
		int objectId, item_id, loc_data, enchant_level, custom_type1, custom_type2, manaLeft, appearance, mobId;
		long time, count;
		ItemLocation loc;
		try
		{
			objectId = rs.getInt(1);
			item_id = rs.getInt("item_id");
			count = rs.getLong("count");
			loc = ItemLocation.valueOf(rs.getString("loc"));
			loc_data = rs.getInt("loc_data");
			enchant_level = rs.getInt("enchant_level");
			custom_type1 = rs.getInt("custom_type1");
			custom_type2 = rs.getInt("custom_type2");
			manaLeft = rs.getInt("mana_left");
			time = rs.getLong("time");
			appearance = rs.getInt("appearance");
			mobId = rs.getInt("mob_id");

			L2Object temp = L2World.getInstance().findObject(objectId);
			if (temp instanceof L2ItemInstance)
			{
				L2World.getInstance().removeObject(temp);

				return (L2ItemInstance) temp;
			}
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Could not restore an item owned by " + ownerId + " from DB:", e);
			return null;
		}
		L2Item item = ItemTable.getInstance().getTemplate(item_id);
		if (item == null)
		{
			Log.severe("Item item_id=" + item_id + " not known, object_id=" + objectId);
			return null;
		}
		inst = new L2ItemInstance(objectId, item, time);
		inst._ownerId = ownerId;
		inst.setCount(count);
		inst._enchantLevel = enchant_level;
		inst._type1 = custom_type1;
		inst._type2 = custom_type2;
		inst._loc = loc;
		inst._locData = loc_data;
		inst._existsInDb = true;
		inst._storedInDb = true;

		// Setup life time for shadow weapons
		inst._mana = manaLeft;

		inst.setAppearance(appearance);

		//load augmentation and elemental enchant
		if (inst.isEquipable())
		{
			inst.restoreAttributes();
		}

		inst.setMobId(mobId);

		return inst;
	}

	/**
	 * Init a dropped L2ItemInstance and add it in the world as a visible object.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the x,y,z position of the L2ItemInstance dropped and update its _worldregion </li>
	 * <li>Add the L2ItemInstance dropped to _visibleObjects of its L2WorldRegion</li>
	 * <li>Add the L2ItemInstance dropped in the world as a <B>visible</B> object</li><BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object to _allObjects of L2World </B></FONT><BR><BR>
	 * <p>
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> _worldRegion == null <I>(L2Object is invisible at the beginning)</I></li><BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Drop item</li>
	 * <li> Call Pet</li><BR>
	 */
	public class ItemDropTask implements Runnable
	{
		private int _x, _y, _z;
		private final L2Character _dropper;
		private final L2ItemInstance _itm;

		public ItemDropTask(L2ItemInstance item, L2Character dropper, int x, int y, int z)
		{
			_x = x;
			_y = y;
			_z = z;
			_dropper = dropper;
			_itm = item;
		}

		@Override
		public final void run()
		{
			assert _itm.getPosition().getWorldRegion() == null;

			if (Config.GEODATA > 0 && _dropper != null)
			{
				Location dropDest = GeoData.getInstance()
						.moveCheck(_dropper.getX(), _dropper.getY(), _dropper.getZ(), _x, _y, _z,
								_dropper.getInstanceId());
				_x = dropDest.getX();
				_y = dropDest.getY();
				_z = dropDest.getZ();
			}

			if (_dropper != null)
			{
				setInstanceId(_dropper.getInstanceId()); // Inherit instancezone when dropped in visible world
			}
			else
			{
				setInstanceId(0); // No dropper? Make it a global item...
			}

			synchronized (_itm)
			{
				// Set the x,y,z position of the L2ItemInstance dropped and update its _worldregion
				_itm.setIsVisible(true);
				_itm.getPosition().setWorldPosition(_x, _y, _z);
				_itm.getPosition().setWorldRegion(L2World.getInstance().getRegion(getPosition().getWorldPosition()));

				// Add the L2ItemInstance dropped to _visibleObjects of its L2WorldRegion
			}

			// this can synchronize on others instancies, so it's out of
			// synchronized, to avoid deadlocks
			_itm.getPosition().getWorldRegion().addVisibleObject(_itm);
			_itm.setDropTime(System.currentTimeMillis());
			_itm.setDropperObjectId(_dropper != null ? _dropper.getObjectId() :
					0); //Set the dropper Id for the knownlist packets in sendInfo

			// Add the L2ItemInstance dropped in the world as a visible object
			L2World.getInstance().addVisibleObject(_itm, _itm.getPosition().getWorldRegion());
			if (Config.SAVE_DROPPED_ITEM)
			{
				ItemsOnGroundManager.getInstance().save(_itm);
			}
			//_itm.setDropperObjectId(0); //Set the dropper Id back to 0 so it no longer shows the drop packet
		}
	}

	public final void dropMe(L2Character dropper, int x, int y, int z)
	{
		ThreadPoolManager.getInstance().executeTask(new ItemDropTask(this, dropper, x, y, z));
	}

	/**
	 * Update the database with values of the item
	 */
	private void updateInDb()
	{
		assert _existsInDb;

		if (_wear)
		{
			return;
		}

		if (_storedInDb)
		{
			return;
		}

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(
					"UPDATE items SET owner_id=?,count=?,loc=?,loc_data=?,enchant_level=?,custom_type1=?,custom_type2=?,mana_left=?,time=?,appearance=?,mob_id=? " +
							"WHERE object_id = ?");
			statement.setInt(1, _ownerId);
			statement.setLong(2, getCount());
			statement.setString(3, _loc.name());
			statement.setInt(4, _locData);
			statement.setInt(5, getEnchantLevel());
			statement.setInt(6, getCustomType1());
			statement.setInt(7, getCustomType2());
			statement.setInt(8, getMana());
			statement.setLong(9, getTime());
			statement.setInt(10, getAppearance());
			statement.setInt(11, getMobId());
			statement.setInt(12, getObjectId());
			statement.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE,
					"Could not update item " + this + " (owner id " + _ownerId + ") in DB: Reason: " + e.getMessage(),
					e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Insert the item in database
	 */
	private void insertIntoDb()
	{
		assert !_existsInDb && getObjectId() != 0;

		if (_wear)
		{
			return;
		}

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(
					"INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,object_id,custom_type1,custom_type2,mana_left,time,appearance,mob_id) " +
							"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, _ownerId);
			statement.setInt(2, _itemId);
			statement.setLong(3, getCount());
			statement.setString(4, _loc.name());
			statement.setInt(5, _locData);
			statement.setInt(6, getEnchantLevel());
			statement.setInt(7, getObjectId());
			statement.setInt(8, _type1);
			statement.setInt(9, _type2);
			statement.setInt(10, getMana());
			statement.setLong(11, getTime());
			statement.setInt(12, getAppearance());
			statement.setInt(13, getMobId());

			statement.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
			statement.close();

			if (isSoulEnhanced())
			{
				updateItemEnsoulEffects(con);
			}
			if (_augmentation != null)
			{
				updateItemAttributes(con);
			}
			if (_elementals != null)
			{
				updateItemElements(con);
			}
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Could not insert item " + this + " into DB: Reason: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Delete item from database
	 */
	private void removeFromDb()
	{
		assert _existsInDb;

		if (_wear)
		{
			return;
		}

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("DELETE FROM items WHERE object_id=?");
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			_existsInDb = false;
			_storedInDb = false;
			statement.close();

			statement = con.prepareStatement("DELETE FROM item_attributes WHERE itemId = ?");
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			statement.close();

			statement = con.prepareStatement("DELETE FROM item_elementals WHERE itemId = ?");
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Could not delete item " + this + " in DB: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Returns the item in String format
	 *
	 * @return String
	 */
	@Override
	public String toString()
	{
		return _item + "[" + getObjectId() + "]";
	}

	public void resetOwnerTimer()
	{
		if (itemLootShedule != null)
		{
			itemLootShedule.cancel(true);
		}
		itemLootShedule = null;
	}

	public void setItemLootShedule(ScheduledFuture<?> sf)
	{
		itemLootShedule = sf;
	}

	public ScheduledFuture<?> getItemLootShedule()
	{
		return itemLootShedule;
	}

	public void setProtected(boolean is_protected)
	{
		_protected = is_protected;
	}

	public boolean isProtected()
	{
		return _protected;
	}

	public boolean isNightLure()
	{
		return _itemId >= 8505 && _itemId <= 8513 || _itemId == 8485;
	}

	public void setCountDecrease(boolean decrease)
	{
		_decrease = decrease;
	}

	public boolean getCountDecrease()
	{
		return _decrease;
	}

	public void setInitCount(int InitCount)
	{
		_initCount = InitCount;
	}

	public long getInitCount()
	{
		return _initCount;
	}

	public void restoreInitCount()
	{
		if (_decrease)
		{
			setCount(_initCount);
		}
	}

	public boolean isTimeLimitedItem()
	{
		return _time > 0;
	}

	/**
	 * Returns (current system time + time) of this time limited item
	 *
	 * @return Time
	 */
	public long getTime()
	{
		return _time;
	}

	public final void setTime(final int time)
	{
		_time = System.currentTimeMillis() + (long) time * 60 * 1000;

		scheduleLifeTimeTask();

		_storedInDb = false;
	}

	@Override
	public int getRemainingTime()
	{
		long remTime = _time - System.currentTimeMillis();
		return isTimeLimitedItem() ? (int) (remTime / 1000) : -9999;
	}

	public void endOfLife()
	{
		L2PcInstance player = L2World.getInstance().getPlayer(getOwnerId());
		if (player != null)
		{
			if (isEquipped())
			{
				L2ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(getLocationSlot());
				InventoryUpdate iu = new InventoryUpdate();
				for (L2ItemInstance item : unequiped)
				{
					player.checkSShotsMatch(null, item);
					iu.addModifiedItem(item);
				}
				player.sendPacket(iu);
				player.broadcastUserInfo();
			}

			if (getLocation() != ItemLocation.WAREHOUSE)
			{
				// destroy
				player.getInventory().destroyItem("L2ItemInstance", this, player, null);

				// send update
				InventoryUpdate iu = new InventoryUpdate();
				iu.addRemovedItem(this);
				player.sendPacket(iu);

				StatusUpdate su = new StatusUpdate(player);
				su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
				player.sendPacket(su);
			}
			else
			{
				player.getWarehouse().destroyItem("L2ItemInstance", this, player, null);
			}
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TIME_LIMITED_ITEM_DELETED));
			// delete from world
			L2World.getInstance().removeObject(this);
		}
	}

	public void scheduleLifeTimeTask()
	{
		if (!isTimeLimitedItem())
		{
			return;
		}

		if (getRemainingTime() <= 0)
		{
			endOfLife();
		}
		else
		{
			if (_lifeTimeTask != null)
			{
				_lifeTimeTask.cancel(false);
			}
			_lifeTimeTask = ThreadPoolManager.getInstance()
					.scheduleGeneral(new ScheduleLifeTimeTask(this), getRemainingTime() * 1000L);
		}
	}

	public static class ScheduleLifeTimeTask implements Runnable
	{
		private final L2ItemInstance _limitedItem;

		public ScheduleLifeTimeTask(L2ItemInstance item)
		{
			_limitedItem = item;
		}

		@Override
		public void run()
		{
			try
			{
				if (_limitedItem != null)
				{
					_limitedItem.endOfLife();
				}
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "", e);
			}
		}
	}

	public void updateElementAttrBonus(L2PcInstance player)
	{
		if (_elementals == null)
		{
			return;
		}
		for (Elementals elm : _elementals)
		{
			elm.updateBonus(player, isArmor());
		}
	}

	public void removeElementAttrBonus(L2PcInstance player)
	{
		if (_elementals == null)
		{
			return;
		}
		for (Elementals elm : _elementals)
		{
			elm.removeBonus(player);
		}
	}

	public void setDropperObjectId(int id)
	{
		_dropperObjectId = id;
	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if (_dropperObjectId != 0)
		{
			activeChar.sendPacket(new DropItem(this, _dropperObjectId));
		}
		else
		{
			activeChar.sendPacket(new SpawnItem(this));
		}
	}

	public boolean isPublished()
	{
		return _published;
	}

	public void publish()
	{
		_published = true;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.model.L2Object#decayMe()
	 */
	@Override
	public void decayMe()
	{
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().removeObject(this);
		}
		super.decayMe();
	}

	public boolean isQuestItem()
	{
		return getItem().isQuestItem();
	}

	public void setMobId(int mobId)
	{
		_mobId = mobId;
		updateDatabase(true);
	}

	public int getMobId()
	{
		return _mobId;
	}

	public int getMaxEnchantLevel()
	{
		switch (getItem().getType2())
		{
			case L2Item.TYPE2_WEAPON:
				return Config.ENCHANT_MAX_WEAPON;
			case L2Item.TYPE2_SHIELD_ARMOR:
				return Config.ENCHANT_MAX_ARMOR;
			case L2Item.TYPE2_ACCESSORY:
				return Config.ENCHANT_MAX_JEWELRY;
		}
		return 0;
	}

	public boolean isValuable()
	{
		return getItem().getItemGrade() >= L2Item.CRYSTAL_S || (isAugmented() || isPvp());
	}

	private boolean _isEventDrop;

	public boolean isEventDrop()
	{
		return _isEventDrop;
	}

	public void isEventDrop(boolean mode)
	{
		_isEventDrop = mode;
	}

	public int getStoneType()
	{
		if (getName().contains("Weapon") || getItem().getStandardItem() > -1 &&
				ItemTable.getInstance().getTemplate(getItem().getStandardItem()) instanceof L2Weapon)
		{
			return L2Item.TYPE2_WEAPON;
		}
		if (getName().contains("Armor"))
		{
			return L2Item.TYPE2_SHIELD_ARMOR;
		}
		if (getName().contains("Accessory"))
		{
			return L2Item.TYPE2_ACCESSORY;
		}
		return -1;
	}
}
