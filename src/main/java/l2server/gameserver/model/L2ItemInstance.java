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
public final class L2ItemInstance extends L2Object implements ItemInstanceInfo {
	/**
	 * Enumeration of locations for item
	 */
	public enum ItemLocation {
		VOID,
		INVENTORY,
		PAPERDOLL,
		WAREHOUSE,
		CLANWH,
		PET,
		PET_EQUIP,
		LEASE,
		REFUND,
		MAIL,
		AUCTION
	}
	
	/**
	 * ID of the owner
	 */
	private int ownerId;
	
	/**
	 * ID of who dropped the item last, used for knownlist
	 */
	private int dropperObjectId = 0;
	
	/**
	 * Quantity of the item
	 */
	private long count;
	/**
	 * Initial Quantity of the item
	 */
	private long initCount;
	/**
	 * Remaining time (in miliseconds)
	 */
	private long time;
	/**
	 * Quantity of the item can decrease
	 */
	private boolean decrease = false;
	
	/**
	 * ID of the item
	 */
	private final int itemId;
	
	/**
	 * Object L2Item associated to the item
	 */
	private final L2Item item;
	
	/**
	 * Location of the item : Inventory, PaperDoll, WareHouse
	 */
	private ItemLocation loc;
	
	/**
	 * Slot where item is stored : Paperdoll slot, inventory order ...
	 */
	private int locData;
	
	/**
	 * Level of enchantment of the item
	 */
	private int enchantLevel;
	
	/**
	 * Wear Item
	 */
	private boolean wear;
	
	/**
	 * Soul Crystal Enhancements
	 */
	private EnsoulEffect[] ensoulEffects = new EnsoulEffect[3];
	
	/**
	 * Augmented Item
	 */
	private L2Augmentation augmentation = null;
	
	/**
	 * Shadow item
	 */
	private int mana = -1;
	private boolean consumingMana = false;
	private static final int MANA_CONSUMPTION_RATE = 60000;
	
	/**
	 * Custom item types (used loto, race tickets)
	 */
	private int type1;
	private int type2;
	
	private long dropTime;
	
	private boolean published = false;
	
	public static final double CHARGED_NONE = 1.0;
	public static final double CHARGED_SOULSHOT = 2.0;
	public static final double CHARGED_SPIRITSHOT = 2.0;
	public static final double CHARGED_BLESSED_SPIRITSHOT = 4.0;
	
	/**
	 * Item charged with SoulShot (type of SoulShot)
	 */
	private double chargedSoulshot = CHARGED_NONE;
	/**
	 * Item charged with SpiritShot (type of SpiritShot)
	 */
	private double chargedSpiritshot = CHARGED_NONE;
	
	private boolean chargedFishtshot = false;
	
	private boolean isProtected;
	
	public static final int UNCHANGED = 0;
	public static final int ADDED = 1;
	public static final int REMOVED = 3;
	public static final int MODIFIED = 2;
	private int lastChange = 2; //1 ??, 2 modified, 3 removed
	private boolean existsInDb; // if a record exists in DB.
	private boolean storedInDb; // if DB data is up-to-date.
	
	private final ReentrantLock dbLock = new ReentrantLock();
	
	private Elementals[] elementals = null;
	
	private ScheduledFuture<?> itemLootShedule = null;
	public ScheduledFuture<?> lifeTimeTask;
	
	private int mobId = 0;
	
	/**
	 * Constructor of the L2ItemInstance from the objectId and the itemId.
	 *
	 * @param objectId : int designating the ID of the object in the world
	 * @param itemId   : int designating the ID of the item
	 */
	public L2ItemInstance(int objectId, int itemId) {
		super(objectId);
		setInstanceType(InstanceType.L2ItemInstance);
		this.itemId = itemId;
		item = ItemTable.getInstance().getTemplate(itemId);
		if (itemId == 0 || item == null) {
			throw new IllegalArgumentException();
		}
		super.setName(item.getName());
		setCount(1);
		loc = ItemLocation.VOID;
		type1 = 0;
		type2 = 0;
		dropTime = 0;
		mana = item.getDuration();
		time = item.getTime() == -1 ? -1 : System.currentTimeMillis() + (long) item.getTime() * 60 * 1000;
		scheduleLifeTimeTask();
	}
	
	/**
	 * Constructor of the L2ItemInstance from the objetId and the description of the item given by the L2Item.
	 *
	 * @param objectId : int designating the ID of the object in the world
	 * @param item     : L2Item containing informations of the item
	 */
	public L2ItemInstance(int objectId, L2Item item, long time) {
		super(objectId);
		setInstanceType(InstanceType.L2ItemInstance);
		itemId = item.getItemId();
		this.item = item;
		if (itemId == 0) {
			throw new IllegalArgumentException();
		}
		super.setName(item.getName());
		setCount(1);
		loc = ItemLocation.VOID;
		mana = item.getDuration();
		
		if (time != -1) {
			this.time = time;
		} else {
			this.time = item.getTime() == -1 ? -1 : System.currentTimeMillis() + (long) item.getTime() * 60 * 1000;
		}
		
		scheduleLifeTimeTask();
	}
	
	public L2ItemInstance(int objectId, L2Item item) {
		super(objectId);
		setInstanceType(InstanceType.L2ItemInstance);
		itemId = item.getItemId();
		this.item = item;
		if (itemId == 0) {
			throw new IllegalArgumentException();
		}
		super.setName(item.getName());
		setCount(1);
		loc = ItemLocation.VOID;
		mana = item.getDuration();
		time = item.getTime() == -1 ? -1 : System.currentTimeMillis() + (long) item.getTime() * 60 * 1000;
		scheduleLifeTimeTask();
	}
	
	@Override
	public void initKnownList() {
		setKnownList(new NullKnownList(this));
	}
	
	/**
	 * Remove a L2ItemInstance from the world and send server->client GetItem packets.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client Packet GetItem to player that pick up and its knowPlayers member </li>
	 * <li>Remove the L2Object from the world</li><BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from allObjects of L2World </B></FONT><BR><BR>
	 * <p>
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> this instanceof L2ItemInstance</li>
	 * <li> worldRegion != null <I>(L2Object is visible at the beginning)</I></li><BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Do Pickup Item : PCInstance and Pet</li><BR><BR>
	 *
	 * @param player Player that pick up the item
	 */
	public final void pickupMe(L2Character player) {
		assert getPosition().getWorldRegion() != null;
		
		L2WorldRegion oldregion = getPosition().getWorldRegion();
		
		// Create a server->client GetItem packet to pick up the L2ItemInstance
		GetItem gi = new GetItem(this, player.getObjectId());
		player.broadcastPacket(gi);
		
		synchronized (this) {
			setIsVisible(false);
			getPosition().setWorldRegion(null);
		}
		
		// if this item is a mercenary ticket, remove the spawns!
		int itemId = getItemId();
		
		if (MercTicketManager.getInstance().getTicketCastleId(itemId) > 0) {
			MercTicketManager.getInstance().removeTicket(this);
			ItemsOnGroundManager.getInstance().removeObject(this);
		}
		
		if (!Config.DISABLE_TUTORIAL && (itemId == 57 || itemId == 6353)) {
			L2PcInstance actor = player.getActingPlayer();
			if (actor != null) {
				QuestState qs = actor.getQuestState("Q255_Tutorial");
				if (qs != null) {
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
	public void setOwnerId(String process, int owner_id, L2PcInstance creator, Object reference) {
		setOwnerId(owner_id);
		
		if (Config.LOG_ITEMS && !process.contains("Consume")) {
			if (!Config.LOG_ITEMS_SMALL_LOG || Config.LOG_ITEMS_SMALL_LOG &&
					(getItem().isEquipable() || getItem().getItemId() == ADENA_ID || item.getItemId() == 4037 || item.getItemId() == 4355 ||
							item.getItemId() == 4356)) {
				logItem(getItemId(), getObjectId(), getCount(), owner_id, process);
			}
		}
		
		if (creator != null && creator.isGM()) {
			String referenceName = "no-reference";
			if (reference instanceof L2Object) {
				referenceName = ((L2Object) reference).getName() != null ? ((L2Object) reference).getName() : "no-name";
			} else if (reference instanceof String) {
				referenceName = (String) reference;
			}
			String targetName = creator.getTarget() != null ? creator.getTarget().getName() : "no-target";
			if (Config.GMAUDIT) {
				GMAudit.auditGMAction(creator.getName(),
						process + " (obj id: " + getObjectId() + " receipt id: " + owner_id + " id: " + getItemId() + " count: " + getCount() +
								" name: " + getName() + ")",
						targetName,
						"L2Object referencing this action is: " + referenceName);
			}
		}
	}
	
	/**
	 * Sets the ownerID of the item
	 *
	 * @param owner_id : int designating the ID of the owner
	 */
	public void setOwnerId(int owner_id) {
		if (owner_id == ownerId) {
			return;
		}
		
		ownerId = owner_id;
		storedInDb = false;
	}
	
	/**
	 * Returns the ownerID of the item
	 *
	 * @return int : ownerID of the item
	 */
	public int getOwnerId() {
		return ownerId;
	}
	
	/**
	 * Sets the location of the item
	 *
	 * @param loc : ItemLocation (enumeration)
	 */
	public void setLocation(ItemLocation loc) {
		setLocation(loc, 0);
	}
	
	/**
	 * Sets the location of the item.<BR><BR>
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 *
	 * @param loc      : ItemLocation (enumeration)
	 * @param loc_data : int designating the slot where the item is stored or the village for freights
	 */
	public void setLocation(ItemLocation loc, int loc_data) {
		if (loc == loc && loc_data == locData) {
			return;
		}
		this.loc = loc;
		locData = loc_data;
		storedInDb = false;
	}
	
	public ItemLocation getLocation() {
		return loc;
	}
	
	/**
	 * Sets the quantity of the item.<BR><BR>
	 *
	 * @param count the new count to set
	 */
	public void setCount(long count) {
		if (getCount() == count) {
			return;
		}
		
		this.count = count >= -1 ? count : 0;
		storedInDb = false;
	}
	
	/**
	 * @return Returns the count.
	 */
	@Override
	public long getCount() {
		return count;
	}
	
	public static void logItem(int itemId, int objectId, long count, int ownerId, String process) {
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("INSERT IGNORE INTO log_items(owner_id, item_id, item_object_id, count, process, time) VALUES(?,?,?,?,?,?)");
			statement.setInt(1, ownerId);
			statement.setInt(2, itemId);
			statement.setInt(3, objectId);
			statement.setLong(4, count);
			statement.setString(5, process);
			statement.setLong(6, System.currentTimeMillis());
			statement.execute();
			statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
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
	public void changeCount(String process, long count, L2PcInstance creator, Object reference) {
		if (count == 0) {
			return;
		}
		long old = getCount();
		//long max = getItemId() == ADENA_ID ? MAX_ADENA : Integer.MAX_VALUE;
		long max = MAX_ADENA;
		
		if (count > 0 && getCount() > max - count) {
			setCount(max);
		} else {
			setCount(getCount() + count);
		}
		
		if (getCount() < 0) {
			setCount(0);
		}
		
		storedInDb = false;
		
		if (Config.LOG_ITEMS && process != null && !process.contains("Consume")) {
			if (!Config.LOG_ITEMS_SMALL_LOG || Config.LOG_ITEMS_SMALL_LOG &&
					(item.isEquipable() || item.getItemId() == ADENA_ID || item.getItemId() == 4037 || item.getItemId() == 4355 ||
							item.getItemId() == 4356)) {
				logItem(getItemId(),
						getObjectId(),
						count,
						creator != null ? creator.getObjectId() : 0,
						process + " (" + old + "->" + getCount() + ")");
			}
		}
		
		if (creator != null) {
			if (getOwnerId() != creator.getObjectId()) {
				//Broadcast.toGameMasters("Found " + getName() + " with diff oid, " + getOwnerId() + " VS " + creator.getObjectId());
			}
			
			if (creator.isGM()) {
				String referenceName = "no-reference";
				if (reference instanceof L2Object) {
					referenceName = ((L2Object) reference).getName() != null ? ((L2Object) reference).getName() : "no-name";
				} else if (reference instanceof String) {
					referenceName = (String) reference;
				}
				String targetName = creator.getTarget() != null ? creator.getTarget().getName() : "no-target";
				if (Config.GMAUDIT) {
					GMAudit.auditGMAction(creator.getName(),
							process + " (id: " + getItemId() + " objId: " + getObjectId() + " name: " + getName() + " count: " + count + ")",
							targetName,
							"L2Object referencing this action is: " + referenceName);
				}
			}
		}
	}
	
	// No logging (function designed for shots only)
	public void changeCountWithoutTrace(int count, L2PcInstance creator, Object reference) {
		changeCount(null, count, creator, reference);
	}
	
	/**
	 * Returns if item is equipable
	 *
	 * @return boolean
	 */
	public boolean isEquipable() {
		return !(item.getBodyPart() == 0 || item.getItemType() == L2EtcItemType.LURE);
	}
	
	/**
	 * Returns if item is equipped
	 *
	 * @return boolean
	 */
	@Override
	public boolean isEquipped() {
		return loc == ItemLocation.PAPERDOLL || loc == ItemLocation.PET_EQUIP;
	}
	
	/**
	 * Returns the slot where the item is stored
	 *
	 * @return int
	 */
	@Override
	public int getLocationSlot() {
		return locData;
	}
	
	/**
	 * Returns the characteristics of the item
	 *
	 * @return L2Item
	 */
	@Override
	public L2Item getItem() {
		return item;
	}
	
	public int getCustomType1() {
		return type1;
	}
	
	public int getCustomType2() {
		return type2;
	}
	
	public void setCustomType1(int newtype) {
		type1 = newtype;
	}
	
	public void setCustomType2(int newtype) {
		type2 = newtype;
	}
	
	public void setDropTime(long time) {
		dropTime = time;
	}
	
	public long getDropTime() {
		return dropTime;
	}
	
	/**
	 * Returns the type of item
	 *
	 * @return Enum
	 */
	public L2ItemType getItemType() {
		return item.getItemType();
	}
	
	/**
	 * Returns the ID of the item
	 *
	 * @return int
	 */
	public int getItemId() {
		return itemId;
	}
	
	/**
	 * Returns true if item is an EtcItem
	 *
	 * @return boolean
	 */
	public boolean isEtcItem() {
		return item instanceof L2EtcItem;
	}
	
	/**
	 * Returns true if item is a Weapon/Shield
	 *
	 * @return boolean
	 */
	public boolean isWeapon() {
		return item instanceof L2Weapon;
	}
	
	/**
	 * Returns true if item is an Armor
	 *
	 * @return boolean
	 */
	public boolean isArmor() {
		return item instanceof L2Armor;
	}
	
	/**
	 * Returns the characteristics of the L2EtcItem
	 *
	 * @return L2EtcItem
	 */
	public L2EtcItem getEtcItem() {
		if (item instanceof L2EtcItem) {
			return (L2EtcItem) item;
		}
		return null;
	}
	
	/**
	 * Returns the characteristics of the L2Weapon
	 *
	 * @return L2Weapon
	 */
	public L2Weapon getWeaponItem() {
		if (item instanceof L2Weapon) {
			return (L2Weapon) item;
		}
		return null;
	}
	
	/**
	 * Returns the characteristics of the L2Armor
	 *
	 * @return L2Armor
	 */
	public L2Armor getArmorItem() {
		if (item instanceof L2Armor) {
			return (L2Armor) item;
		}
		return null;
	}
	
	/**
	 * Returns the quantity of crystals for crystallization
	 *
	 * @return int
	 */
	public final int getCrystalCount() {
		return item.getCrystalCount(enchantLevel);
	}
	
	/**
	 * Returns the reference price of the item
	 *
	 * @return int
	 */
	public int getReferencePrice() {
		return item.getReferencePrice();
	}
	
	/**
	 * Returns the name of the item
	 *
	 * @return String
	 */
	public String getItemName() {
		return item.getName();
	}
	
	/**
	 * Returns the last change of the item
	 *
	 * @return int
	 */
	public int getLastChange() {
		return lastChange;
	}
	
	/**
	 * Sets the last change of the item
	 *
	 * @param lastChange : int
	 */
	public void setLastChange(int lastChange) {
		this.lastChange = lastChange;
	}
	
	/**
	 * Returns if item is stackable
	 *
	 * @return boolean
	 */
	public boolean isStackable() {
		return item.isStackable();
	}
	
	/**
	 * Returns if item is dropable
	 *
	 * @return boolean
	 */
	public boolean isDropable() {
		return !isAugmented() && item.isDropable();
	}
	
	/**
	 * Returns if item is destroyable
	 *
	 * @return boolean
	 */
	public boolean isDestroyable() {
		if (getTime() != -1) {
			return false;
		}
		
		return item.isDestroyable();
	}
	
	/**
	 * Returns if item is tradeable
	 *
	 * @return boolean
	 */
	public boolean isTradeable() {
		if (getTime() != -1) {
			return false;
		}
		
		return !isAugmented() && item.isTradeable();
	}
	
	/**
	 * Returns if item is sellable
	 *
	 * @return boolean
	 */
	public boolean isSellable() {
		if (getTime() != -1) {
			return false;
		}
		
		return !isAugmented() && item.isSellable();
	}
	
	/**
	 * Returns if item can be deposited in warehouse or freight
	 *
	 * @return boolean
	 */
	public boolean isDepositable(boolean isPrivateWareHouse) {
		if (getTime() != -1) {
			return false;
		}
		
		// equipped, hero and quest items
		if (isEquipped() || !item.isDepositable()) {
			return false;
		}
		if (!isPrivateWareHouse) {
			// augmented not tradeable
			if (!isTradeable() || isShadowItem()) {
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
	public boolean isConsumable() {
		return item.isConsumable();
	}
	
	public boolean isPotion() {
		return item.isPotion();
	}
	
	public boolean isElixir() {
		return item.isElixir();
	}
	
	public boolean isHeroItem() {
		return item.isHeroItem();
	}
	
	public boolean isCommonItem() {
		return item.isCommon();
	}
	
	/**
	 * Returns whether this item is pvp or not
	 *
	 * @return boolean
	 */
	public boolean isPvp() {
		return item.isPvpItem();
	}
	
	/**
	 * Returns if item is available for manipulation
	 *
	 * @return boolean
	 */
	public boolean isAvailable(L2PcInstance player, boolean allowAdena, boolean allowNonTradeable) {
		return !isEquipped() // Not equipped
				&& getItem().getType2() != L2Item.TYPE2_QUEST // Not Quest Item
				&& (getItem().getType2() != L2Item.TYPE2_MONEY || getItem().getType1() != L2Item.TYPE1_SHIELD_ARMOR)
				// not money, not shield
				&& (player.getPet() == null || getObjectId() != player.getPet().getControlObjectId())
				// Not Control item of currently summoned pet
				&& player.getActiveEnchantItem() != this // Not momentarily used enchant scroll
				&& (allowAdena || getItemId() != 57) // Not adena
				&& (player.getCurrentSkill() == null || player.getCurrentSkill().getSkill().getItemConsumeId() != getItemId()) &&
				(!player.isCastingSimultaneouslyNow() || player.getLastSimultaneousSkillCast() == null ||
						player.getLastSimultaneousSkillCast().getItemConsumeId() != getItemId()) &&
				(allowNonTradeable || isTradeable() && !(getItem().getItemType() == L2EtcItemType.PET_COLLAR && player.havePetInvItems()));
	}
	
	/**
	 * Returns the level of enchantment of the item
	 *
	 * @return int
	 */
	@Override
	public int getEnchantLevel() {
		return enchantLevel;
	}
	
	/**
	 * Sets the level of enchantment of the item
	 */
	public void setEnchantLevel(int enchantLevel) {
		if (enchantLevel == enchantLevel) {
			return;
		}
		this.enchantLevel = enchantLevel;
		storedInDb = false;
	}
	
	/**
	 * Returns whether this item is augmented or not
	 *
	 * @return true if augmented
	 */
	@Override
	public boolean isSoulEnhanced() {
		for (EnsoulEffect e : ensoulEffects) {
			if (e != null) {
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
	public EnsoulEffect[] getEnsoulEffects() {
		return ensoulEffects;
	}
	
	/**
	 * Returns the augmentation object for this item
	 *
	 * @return augmentation
	 */
	@Override
	public int[] getEnsoulEffectIds() {
		int effectCount = 0;
		if (ensoulEffects[0] != null) {
			effectCount++;
		}
		if (ensoulEffects[1] != null) {
			effectCount++;
		}
		
		int[] effects = new int[effectCount];
		int index = 0;
		if (ensoulEffects[0] != null) {
			effects[index++] = ensoulEffects[0].getId();
		}
		if (ensoulEffects[1] != null) {
			effects[index++] = ensoulEffects[1].getId();
		}
		
		return effects;
	}
	
	/**
	 * Returns the augmentation object for this item
	 *
	 * @return augmentation
	 */
	@Override
	public int[] getEnsoulSpecialEffectIds() {
		if (ensoulEffects[2] == null) {
			return new int[]{};
		}
		
		return new int[]{ensoulEffects[2].getId()};
	}
	
	/**
	 * Sets a new ensoul effect
	 *
	 * @param effect
	 * @return return true if sucessful
	 */
	public boolean setEnsoulEffect(int index, EnsoulEffect effect) {
		// there shall be no previous effect..?
		//if (ensoulEffects[index] != null)
		//	return false;
		
		ensoulEffects[index] = effect;
		updateItemEnsoulEffects(null);
		return true;
	}
	
	/**
	 * Remove the ensoul effect
	 */
	public void removeEnsoulEffects() {
		if (ensoulEffects == null) {
			return;
		}
		
		ensoulEffects = new EnsoulEffect[3];
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = null;
			// Remove the entry
			statement = con.prepareStatement("DELETE FROM item_ensoul_effects WHERE itemId = ?");
			
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			statement.close();
		} catch (Exception e) {
			Log.log(Level.SEVERE, "Could not remove ensoul effect for item: " + this + " from DB:", e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}
	
	/**
	 * Returns whether this item is augmented or not
	 *
	 * @return true if augmented
	 */
	@Override
	public boolean isAugmented() {
		return augmentation != null;
	}
	
	/**
	 * Returns the augmentation object for this item
	 *
	 * @return augmentation
	 */
	public L2Augmentation getAugmentation() {
		return augmentation;
	}
	
	/**
	 * Returns the augmentation bonus for this item
	 *
	 * @return augmentation id
	 */
	@Override
	public long getAugmentationBonus() {
		return augmentation.getId();
	}
	
	/**
	 * Sets a new augmentation
	 *
	 * @param augmentation
	 * @return return true if sucessfull
	 */
	public boolean setAugmentation(L2Augmentation augmentation) {
		// there shall be no previous augmentation..
		if (augmentation != null) {
			return false;
		}
		
		this.augmentation = augmentation;
		updateItemAttributes(null);
		return true;
	}
	
	/**
	 * Remove the augmentation
	 */
	public void removeAugmentation() {
		if (augmentation == null) {
			return;
		}
		
		augmentation = null;
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = null;
			// Remove the entry
			statement = con.prepareStatement("DELETE FROM item_attributes WHERE itemId = ?");
			
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			statement.close();
		} catch (Exception e) {
			Log.log(Level.SEVERE, "Could not remove augmentation for item: " + this + " from DB:", e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}
	
	public void restoreAttributes() {
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT effectIndex, effectId FROM item_ensoul_effects WHERE itemId=?");
			statement.setInt(1, getObjectId());
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				int effectIndex = rs.getInt(1);
				int effectId = rs.getInt(2);
				if (effectIndex >= 0 && effectIndex < ensoulEffects.length && effectId > 0) {
					ensoulEffects[effectIndex] = EnsoulDataTable.getInstance().getEffect(effectId);
				}
			}
			rs.close();
			statement.close();
			
			statement = con.prepareStatement("SELECT augAttributes FROM item_attributes WHERE itemId=?");
			statement.setInt(1, getObjectId());
			rs = statement.executeQuery();
			if (rs.next()) {
				long augAttributes = rs.getLong(1);
				if (augAttributes > 0) {
					augmentation = new L2Augmentation(augAttributes);
				}
			}
			rs.close();
			statement.close();
			
			statement = con.prepareStatement("SELECT elemType,elemValue FROM item_elementals WHERE itemId=?");
			statement.setInt(1, getObjectId());
			rs = statement.executeQuery();
			while (rs.next()) {
				byte elem_type = rs.getByte(1);
				int elem_value = rs.getInt(2);
				if (elem_type != -1 && elem_value != -1) {
					applyAttribute(elem_type, elem_value);
				}
			}
			rs.close();
			statement.close();
		} catch (Exception e) {
			Log.log(Level.SEVERE, "Could not restore augmentation and elemental data for item " + this + " from DB: " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}
	
	private void updateItemEnsoulEffects(Connection pooledCon) {
		Connection con = null;
		try {
			con = pooledCon == null ? L2DatabaseFactory.getInstance().getConnection() : pooledCon;
			PreparedStatement statement = con.prepareStatement("REPLACE INTO item_ensoul_effects VALUES(?,?,?)");
			for (int i = 0; i < ensoulEffects.length; i++) {
				statement.setInt(1, getObjectId());
				statement.setLong(2, i);
				statement.setLong(3, ensoulEffects[i] != null ? ensoulEffects[i].getId() : 0);
				statement.executeUpdate();
			}
			
			statement.close();
		} catch (SQLException e) {
			Log.log(Level.SEVERE, "Could not update ensoul effects for item: " + this + " from DB:", e);
		} finally {
			if (pooledCon == null) {
				L2DatabaseFactory.close(con);
			}
		}
	}
	
	private void updateItemAttributes(Connection pooledCon) {
		Connection con = null;
		try {
			con = pooledCon == null ? L2DatabaseFactory.getInstance().getConnection() : pooledCon;
			PreparedStatement statement = con.prepareStatement("REPLACE INTO item_attributes VALUES(?,?)");
			statement.setInt(1, getObjectId());
			statement.setLong(2, augmentation != null ? augmentation.getId() : 0);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			Log.log(Level.SEVERE, "Could not update atributes for item: " + this + " from DB:", e);
		} finally {
			if (pooledCon == null) {
				L2DatabaseFactory.close(con);
			}
		}
	}
	
	private void updateItemElements(Connection pooledCon) {
		Connection con = null;
		try {
			con = pooledCon == null ? L2DatabaseFactory.getInstance().getConnection() : pooledCon;
			PreparedStatement statement = con.prepareStatement("DELETE FROM item_elementals WHERE itemId = ?");
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			statement.close();
			
			if (elementals == null) {
				return;
			}
			
			statement = con.prepareStatement("INSERT INTO item_elementals VALUES(?,?,?)");
			
			for (Elementals elm : elementals) {
				statement.setInt(1, getObjectId());
				statement.setByte(2, elm.getElement());
				statement.setInt(3, elm.getValue());
				statement.executeUpdate();
				statement.clearParameters();
			}
			
			statement.close();
		} catch (SQLException e) {
			Log.log(Level.SEVERE, "Could not update elementals for item: " + this + " from DB:", e);
		} finally {
			if (pooledCon == null) {
				L2DatabaseFactory.close(con);
			}
		}
	}
	
	public Elementals[] getElementals() {
		return elementals;
	}
	
	@Override
	public boolean isElementEnchanted() {
		return elementals != null || getItem().getElementals() != null;
	}
	
	public Elementals getElemental(byte attribute) {
		if (elementals == null) {
			return null;
		}
		for (Elementals elm : elementals) {
			if (elm.getElement() == attribute) {
				return elm;
			}
		}
		return null;
	}
	
	@Override
	public byte getAttackElementType() {
		if (!isWeapon()) {
			return -2;
		} else if (getItem().getElementals() != null) {
			return getItem().getElementals()[0].getElement();
		} else if (elementals != null) {
			return elementals[0].getElement();
		}
		return -2;
	}
	
	@Override
	public int getAttackElementPower() {
		if (!isWeapon()) {
			return 0;
		} else if (getItem().getElementals() != null) {
			return getItem().getElementals()[0].getValue();
		} else if (elementals != null) {
			return elementals[0].getValue();
		}
		return 0;
	}
	
	@Override
	public int getElementDefAttr(byte element) {
		if (!isArmor()) {
			return 0;
		} else if (getItem().getElementals() != null) {
			Elementals elm = getItem().getElemental(element);
			if (elm != null) {
				return elm.getValue();
			}
		} else if (elementals != null) {
			Elementals elm = getElemental(element);
			if (elm != null) {
				return elm.getValue();
			}
		}
		return 0;
	}
	
	private void applyAttribute(byte element, int value) {
		if (elementals == null) {
			elementals = new Elementals[1];
			elementals[0] = new Elementals(element, value);
		} else {
			Elementals elm = getElemental(element);
			if (elm != null) {
				elm.setValue(value);
			} else {
				elm = new Elementals(element, value);
				Elementals[] array = new Elementals[elementals.length + 1];
				System.arraycopy(elementals, 0, array, 0, elementals.length);
				array[elementals.length] = elm;
				elementals = array;
			}
		}
	}
	
	public void changeAttribute(byte element, int value) {
		if (elementals == null) {
			elementals = new Elementals[1];
			elementals[0] = new Elementals(element, value);
		} else {
			Elementals elm = getElemental(element);
			if (elm != null) {
				elm.setValue(value);
			} else {
				elementals = new Elementals[1];
				elementals[0] = new Elementals(element, value);
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
	public void setElementAttr(byte element, int value) {
		applyAttribute(element, value);
		updateItemElements(null);
	}
	
	/**
	 * Remove elemental from item
	 *
	 * @param element byte element to remove, -1 for all elementals remove
	 */
	public void clearElementAttr(byte element) {
		if (getElemental(element) == null && element != -1) {
			return;
		}
		
		Elementals[] array = null;
		if (element != -1 && elementals != null && elementals.length > 1) {
			array = new Elementals[elementals.length - 1];
			int i = 0;
			for (Elementals elm : elementals) {
				if (elm.getElement() != element) {
					array[i++] = elm;
				}
			}
		}
		elementals = array;
		
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = null;
			if (element != -1) {
				//Item can have still others
				statement = con.prepareStatement("DELETE FROM item_elementals WHERE itemId = ? AND elemType = ?");
				statement.setInt(2, element);
			} else {
				// Remove the entries
				statement = con.prepareStatement("DELETE FROM item_elementals WHERE itemId = ?");
			}
			
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			statement.close();
		} catch (Exception e) {
			Log.log(Level.SEVERE, "Could not remove elemental enchant for item: " + this + " from DB:", e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}
	
	/**
	 * Used to decrease mana
	 * (mana means life time for shadow items)
	 */
	public static class ScheduleConsumeManaTask implements Runnable {
		private final L2ItemInstance shadowItem;
		
		public ScheduleConsumeManaTask(L2ItemInstance item) {
			shadowItem = item;
		}
		
		@Override
		public void run() {
			try {
				// decrease mana
				if (shadowItem != null) {
					shadowItem.decreaseMana(true);
				}
			} catch (Exception e) {
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
	public boolean isShadowItem() {
		return mana >= 0;
	}
	
	/**
	 * Returns the remaining mana of this shadow item
	 *
	 * @return lifeTime
	 */
	@Override
	public int getMana() {
		return mana;
	}
	
	/**
	 * Decreases the mana of this shadow item,
	 * sends a inventory update
	 * schedules a new consumption task if non is running
	 * optionally one could force a new task
	 *
	 * @param resetConsumingMana a new consumption task if item is equipped
	 */
	public void decreaseMana(boolean resetConsumingMana) {
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
	public void decreaseMana(boolean resetConsumingMana, int count) {
		if (!isShadowItem()) {
			return;
		}
		
		if (mana - count >= 0) {
			mana -= count;
		} else {
			mana = 0;
		}
		
		if (storedInDb) {
			storedInDb = false;
		}
		if (resetConsumingMana) {
			consumingMana = false;
		}
		
		final L2PcInstance player = L2World.getInstance().getPlayer(getOwnerId());
		if (player != null) {
			SystemMessage sm;
			switch (mana) {
				case 10:
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_10);
					sm.addItemName(item);
					player.sendPacket(sm);
					break;
				case 5:
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_5);
					sm.addItemName(item);
					player.sendPacket(sm);
					break;
				case 1:
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_1);
					sm.addItemName(item);
					player.sendPacket(sm);
					break;
			}
			
			if (mana == 0) // The life time has expired
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_0);
				sm.addItemName(item);
				player.sendPacket(sm);
				
				// unequip
				if (isEquipped()) {
					L2ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(getLocationSlot());
					InventoryUpdate iu = new InventoryUpdate();
					for (L2ItemInstance item : unequiped) {
						player.checkSShotsMatch(null, item);
						iu.addModifiedItem(item);
					}
					player.sendPacket(iu);
					player.broadcastUserInfo();
				}
				
				if (getLocation() != ItemLocation.WAREHOUSE) {
					// destroy
					player.getInventory().destroyItem("L2ItemInstance", this, player, null);
					
					// send update
					InventoryUpdate iu = new InventoryUpdate();
					iu.addRemovedItem(this);
					player.sendPacket(iu);
					
					StatusUpdate su = new StatusUpdate(player);
					su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
					player.sendPacket(su);
				} else {
					player.getWarehouse().destroyItem("L2ItemInstance", this, player, null);
				}
				
				// delete from world
				L2World.getInstance().removeObject(this);
			} else {
				// Reschedule if still equipped
				if (!consumingMana && isEquipped()) {
					scheduleConsumeManaTask();
				}
				if (getLocation() != ItemLocation.WAREHOUSE) {
					InventoryUpdate iu = new InventoryUpdate();
					iu.addModifiedItem(this);
					player.sendPacket(iu);
				}
			}
		}
	}
	
	public void scheduleConsumeManaTask() {
		if (consumingMana) {
			return;
		}
		consumingMana = true;
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleConsumeManaTask(this), MANA_CONSUMPTION_RATE);
	}
	
	private int appearance;
	
	public void setAppearance(int appearance) {
		this.appearance = appearance;
		storedInDb = false;
	}
	
	@Override
	public int getAppearance() {
		return appearance;
	}
	
	/**
	 * Returns false cause item can't be attacked
	 *
	 * @return boolean false
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker) {
		return false;
	}
	
	/**
	 * Returns the type of charge with SoulShot of the item.
	 *
	 * @return int (CHARGED_NONE, CHARGED_SOULSHOT)
	 */
	public double getChargedSoulShot() {
		return chargedSoulshot;
	}
	
	/**
	 * Returns the type of charge with SpiritShot of the item
	 *
	 * @return int (CHARGED_NONE, CHARGED_SPIRITSHOT, CHARGED_BLESSED_SPIRITSHOT)
	 */
	public double getChargedSpiritShot() {
		return chargedSpiritshot;
	}
	
	public boolean getChargedFishshot() {
		return chargedFishtshot;
	}
	
	/**
	 * Sets the type of charge with SoulShot of the item
	 *
	 * @param type : int (CHARGED_NONE, CHARGED_SOULSHOT)
	 */
	public void setChargedSoulShot(double type) {
		chargedSoulshot = type;
	}
	
	/**
	 * Sets the type of charge with SpiritShot of the item
	 *
	 * @param type : int (CHARGED_NONE, CHARGED_SPIRITSHOT, CHARGED_BLESSED_SPIRITSHOT)
	 */
	public void setChargedSpiritShot(double type) {
		chargedSpiritshot = type;
	}
	
	public void setChargedFishshot(boolean type) {
		chargedFishtshot = type;
	}
	
	/**
	 * This function basically returns a set of functions from
	 * L2Item/L2Armor/L2Weapon, but may add additional
	 * functions, if this particular item instance is enhanched
	 * for a particular player.
	 *
	 * @return Func[]
	 */
	public Func[] getStatFuncs() {
		return getItem().getStatFuncs(this);
	}
	
	/**
	 * Updates the database.<BR>
	 */
	public void updateDatabase() {
		updateDatabase(false);
	}
	
	/**
	 * Updates the database.<BR>
	 *
	 * @param force if the update should necessarilly be done.
	 */
	public void updateDatabase(boolean force) {
		dbLock.lock();
		
		try {
			if (existsInDb) {
				if (ownerId == 0 || loc == ItemLocation.VOID || loc == ItemLocation.REFUND || getCount() == 0 && loc != ItemLocation.LEASE) {
					removeFromDb();

					/*
					if (getCount() != 0)
					{
						Broadcast.toGameMasters("(1) Deleted " + getCount() + " " + getName() + " from DB because... ");

						if (ownerId == 0)
							Broadcast.toGameMasters("OwnerId = 0");
						if (loc == ItemLocation.VOID || loc == ItemLocation.REFUND)
							Broadcast.toGameMasters("Location = " + loc);
						if (getCount() == 0 && loc != ItemLocation.LEASE)
							Broadcast.toGameMasters("Count = 0 & Loc != LEASE");
					}*/
				} else if (!Config.LAZY_ITEMS_UPDATE || force) {
					updateInDb();
				}
			} else {
				if (ownerId == 0 || loc == ItemLocation.VOID || loc == ItemLocation.REFUND || getCount() == 0 && loc != ItemLocation.LEASE) {
                    /*
					if (getCount() != 0)
					{
						Broadcast.toGameMasters("(2) Deleted " + getCount() + " " + getName() + " from DB because... ");

						if (ownerId == 0)
							Broadcast.toGameMasters("OwnerId = 0");
						if (loc == ItemLocation.VOID || loc == ItemLocation.REFUND)
							Broadcast.toGameMasters("Location = " + loc);
						if (getCount() == 0 && loc != ItemLocation.LEASE)
							Broadcast.toGameMasters("Count = 0 & Loc != LEASE");
					}*/
					
					return;
				}
				insertIntoDb();
			}
		} finally {
			dbLock.unlock();
		}
	}
	
	/**
	 * Returns a L2ItemInstance stored in database from its objectID
	 *
	 * @return L2ItemInstance
	 */
	public static L2ItemInstance restoreFromDb(int ownerId, ResultSet rs) {
		L2ItemInstance inst = null;
		int objectId, item_id, loc_data, enchant_level, custom_type1, custom_type2, manaLeft, appearance, mobId;
		long time, count;
		ItemLocation loc;
		try {
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
			if (temp instanceof L2ItemInstance) {
				L2World.getInstance().removeObject(temp);
				
				return (L2ItemInstance) temp;
			}
		} catch (Exception e) {
			Log.log(Level.SEVERE, "Could not restore an item owned by " + ownerId + " from DB:", e);
			return null;
		}
		L2Item item = ItemTable.getInstance().getTemplate(item_id);
		if (item == null) {
			Log.severe("Item item_id=" + item_id + " not known, object_id=" + objectId);
			return null;
		}
		inst = new L2ItemInstance(objectId, item, time);
		inst.ownerId = ownerId;
		inst.setCount(count);
		inst.enchantLevel = enchant_level;
		inst.type1 = custom_type1;
		inst.type2 = custom_type2;
		inst.loc = loc;
		inst.locData = loc_data;
		inst.existsInDb = true;
		inst.storedInDb = true;
		
		// Setup life time for shadow weapons
		inst.mana = manaLeft;
		
		inst.setAppearance(appearance);
		
		//load augmentation and elemental enchant
		if (inst.isEquipable()) {
			inst.restoreAttributes();
		}
		
		inst.setMobId(mobId);
		
		return inst;
	}
	
	/**
	 * Init a dropped L2ItemInstance and add it in the world as a visible object.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the x,y,z position of the L2ItemInstance dropped and update its worldregion </li>
	 * <li>Add the L2ItemInstance dropped to visibleObjects of its L2WorldRegion</li>
	 * <li>Add the L2ItemInstance dropped in the world as a <B>visible</B> object</li><BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object to allObjects of L2World </B></FONT><BR><BR>
	 * <p>
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> worldRegion == null <I>(L2Object is invisible at the beginning)</I></li><BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Drop item</li>
	 * <li> Call Pet</li><BR>
	 */
	public class ItemDropTask implements Runnable {
		private int x, y, z;
		private final L2Character dropper;
		private final L2ItemInstance itm;
		
		public ItemDropTask(L2ItemInstance item, L2Character dropper, int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.dropper = dropper;
			itm = item;
		}
		
		@Override
		public final void run() {
			assert itm.getPosition().getWorldRegion() == null;
			
			if (Config.GEODATA > 0 && dropper != null) {
				Location dropDest = GeoData.getInstance().moveCheck(dropper.getX(), dropper.getY(), dropper.getZ(), x, y, z, dropper.getInstanceId());
				x = dropDest.getX();
				y = dropDest.getY();
				z = dropDest.getZ();
			}
			
			if (dropper != null) {
				setInstanceId(dropper.getInstanceId()); // Inherit instancezone when dropped in visible world
			} else {
				setInstanceId(0); // No dropper? Make it a global item...
			}
			
			synchronized (itm) {
				// Set the x,y,z position of the L2ItemInstance dropped and update its worldregion
				itm.setIsVisible(true);
				itm.getPosition().setWorldPosition(x, y, z);
				itm.getPosition().setWorldRegion(L2World.getInstance().getRegion(getPosition().getWorldPosition()));
				
				// Add the L2ItemInstance dropped to visibleObjects of its L2WorldRegion
			}
			
			// this can synchronize on others instancies, so it's out of
			// synchronized, to avoid deadlocks
			itm.getPosition().getWorldRegion().addVisibleObject(itm);
			itm.setDropTime(System.currentTimeMillis());
			itm.setDropperObjectId(dropper != null ? dropper.getObjectId() : 0); //Set the dropper Id for the knownlist packets in sendInfo
			
			// Add the L2ItemInstance dropped in the world as a visible object
			L2World.getInstance().addVisibleObject(itm, itm.getPosition().getWorldRegion());
			if (Config.SAVE_DROPPED_ITEM) {
				ItemsOnGroundManager.getInstance().save(itm);
			}
			//itm.setDropperObjectId(0); //Set the dropper Id back to 0 so it no longer shows the drop packet
		}
	}
	
	public final void dropMe(L2Character dropper, int x, int y, int z) {
		ThreadPoolManager.getInstance().executeTask(new ItemDropTask(this, dropper, x, y, z));
	}
	
	/**
	 * Update the database with values of the item
	 */
	private void updateInDb() {
		assert existsInDb;
		
		if (wear) {
			return;
		}
		
		if (storedInDb) {
			return;
		}
		
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement(
					"UPDATE items SET owner_id=?,count=?,loc=?,loc_data=?,enchant_level=?,custom_type1=?,custom_type2=?,mana_left=?,time=?,appearance=?,mob_id=? " +
							"WHERE object_id = ?");
			statement.setInt(1, ownerId);
			statement.setLong(2, getCount());
			statement.setString(3, loc.name());
			statement.setInt(4, locData);
			statement.setInt(5, getEnchantLevel());
			statement.setInt(6, getCustomType1());
			statement.setInt(7, getCustomType2());
			statement.setInt(8, getMana());
			statement.setLong(9, getTime());
			statement.setInt(10, getAppearance());
			statement.setInt(11, getMobId());
			statement.setInt(12, getObjectId());
			statement.executeUpdate();
			existsInDb = true;
			storedInDb = true;
			statement.close();
		} catch (Exception e) {
			Log.log(Level.SEVERE, "Could not update item " + this + " (owner id " + ownerId + ") in DB: Reason: " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}
	
	/**
	 * Insert the item in database
	 */
	private void insertIntoDb() {
		assert !existsInDb && getObjectId() != 0;
		
		if (wear) {
			return;
		}
		
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement(
					"INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,object_id,custom_type1,custom_type2,mana_left,time,appearance,mob_id) " +
							"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, ownerId);
			statement.setInt(2, itemId);
			statement.setLong(3, getCount());
			statement.setString(4, loc.name());
			statement.setInt(5, locData);
			statement.setInt(6, getEnchantLevel());
			statement.setInt(7, getObjectId());
			statement.setInt(8, type1);
			statement.setInt(9, type2);
			statement.setInt(10, getMana());
			statement.setLong(11, getTime());
			statement.setInt(12, getAppearance());
			statement.setInt(13, getMobId());
			
			statement.executeUpdate();
			existsInDb = true;
			storedInDb = true;
			statement.close();
			
			if (isSoulEnhanced()) {
				updateItemEnsoulEffects(con);
			}
			if (augmentation != null) {
				updateItemAttributes(con);
			}
			if (elementals != null) {
				updateItemElements(con);
			}
		} catch (Exception e) {
			Log.log(Level.SEVERE, "Could not insert item " + this + " into DB: Reason: " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}
	
	/**
	 * Delete item from database
	 */
	private void removeFromDb() {
		assert existsInDb;
		
		if (wear) {
			return;
		}
		
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement("DELETE FROM items WHERE object_id=?");
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			existsInDb = false;
			storedInDb = false;
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM item_attributes WHERE itemId = ?");
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM item_elementals WHERE itemId = ?");
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			statement.close();
		} catch (Exception e) {
			Log.log(Level.SEVERE, "Could not delete item " + this + " in DB: " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}
	
	/**
	 * Returns the item in String format
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		return item + "[" + getObjectId() + "]";
	}
	
	public void resetOwnerTimer() {
		if (itemLootShedule != null) {
			itemLootShedule.cancel(true);
		}
		itemLootShedule = null;
	}
	
	public void setItemLootShedule(ScheduledFuture<?> sf) {
		itemLootShedule = sf;
	}
	
	public ScheduledFuture<?> getItemLootShedule() {
		return itemLootShedule;
	}
	
	public void setProtected(boolean is_protected) {
		isProtected = is_protected;
	}
	
	public boolean isProtected() {
		return isProtected;
	}
	
	public boolean isNightLure() {
		return itemId >= 8505 && itemId <= 8513 || itemId == 8485;
	}
	
	public void setCountDecrease(boolean decrease) {
		this.decrease = decrease;
	}
	
	public boolean getCountDecrease() {
		return decrease;
	}
	
	public void setInitCount(int InitCount) {
		this.initCount = InitCount;
	}
	
	public long getInitCount() {
		return initCount;
	}
	
	public void restoreInitCount() {
		if (decrease) {
			setCount(initCount);
		}
	}
	
	public boolean isTimeLimitedItem() {
		return time > 0;
	}
	
	/**
	 * Returns (current system time + time) of this time limited item
	 *
	 * @return Time
	 */
	public long getTime() {
		return time;
	}
	
	public final void setTime(final int time) {
		this.time = System.currentTimeMillis() + (long) time * 60 * 1000;
		
		scheduleLifeTimeTask();
		
		storedInDb = false;
	}
	
	@Override
	public int getRemainingTime() {
		long remTime = time - System.currentTimeMillis();
		return isTimeLimitedItem() ? (int) (remTime / 1000) : -9999;
	}
	
	public void endOfLife() {
		L2PcInstance player = L2World.getInstance().getPlayer(getOwnerId());
		if (player != null) {
			if (isEquipped()) {
				L2ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(getLocationSlot());
				InventoryUpdate iu = new InventoryUpdate();
				for (L2ItemInstance item : unequiped) {
					player.checkSShotsMatch(null, item);
					iu.addModifiedItem(item);
				}
				player.sendPacket(iu);
				player.broadcastUserInfo();
			}
			
			if (getLocation() != ItemLocation.WAREHOUSE) {
				// destroy
				player.getInventory().destroyItem("L2ItemInstance", this, player, null);
				
				// send update
				InventoryUpdate iu = new InventoryUpdate();
				iu.addRemovedItem(this);
				player.sendPacket(iu);
				
				StatusUpdate su = new StatusUpdate(player);
				su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
				player.sendPacket(su);
			} else {
				player.getWarehouse().destroyItem("L2ItemInstance", this, player, null);
			}
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TIME_LIMITED_ITEM_DELETED));
			// delete from world
			L2World.getInstance().removeObject(this);
		}
	}
	
	public void scheduleLifeTimeTask() {
		if (!isTimeLimitedItem()) {
			return;
		}
		
		if (getRemainingTime() <= 0) {
			endOfLife();
		} else {
			if (lifeTimeTask != null) {
				lifeTimeTask.cancel(false);
			}
			lifeTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleLifeTimeTask(this), getRemainingTime() * 1000L);
		}
	}
	
	public static class ScheduleLifeTimeTask implements Runnable {
		private final L2ItemInstance limitedItem;
		
		public ScheduleLifeTimeTask(L2ItemInstance item) {
			limitedItem = item;
		}
		
		@Override
		public void run() {
			try {
				if (limitedItem != null) {
					limitedItem.endOfLife();
				}
			} catch (Exception e) {
				Log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	public void updateElementAttrBonus(L2PcInstance player) {
		if (elementals == null) {
			return;
		}
		for (Elementals elm : elementals) {
			elm.updateBonus(player, isArmor());
		}
	}
	
	public void removeElementAttrBonus(L2PcInstance player) {
		if (elementals == null) {
			return;
		}
		for (Elementals elm : elementals) {
			elm.removeBonus(player);
		}
	}
	
	public void setDropperObjectId(int id) {
		dropperObjectId = id;
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar) {
		if (dropperObjectId != 0) {
			activeChar.sendPacket(new DropItem(this, dropperObjectId));
		} else {
			activeChar.sendPacket(new SpawnItem(this));
		}
	}
	
	public boolean isPublished() {
		return published;
	}
	
	public void publish() {
		published = true;
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.model.L2Object#decayMe()
	 */
	@Override
	public void decayMe() {
		if (Config.SAVE_DROPPED_ITEM) {
			ItemsOnGroundManager.getInstance().removeObject(this);
		}
		super.decayMe();
	}
	
	public boolean isQuestItem() {
		return getItem().isQuestItem();
	}
	
	public void setMobId(int mobId) {
		this.mobId = mobId;
		updateDatabase(true);
	}
	
	public int getMobId() {
		return mobId;
	}
	
	public int getMaxEnchantLevel() {
		switch (getItem().getType2()) {
			case L2Item.TYPE2_WEAPON:
				return Config.ENCHANT_MAX_WEAPON;
			case L2Item.TYPE2_SHIELD_ARMOR:
				return Config.ENCHANT_MAX_ARMOR;
			case L2Item.TYPE2_ACCESSORY:
				return Config.ENCHANT_MAX_JEWELRY;
		}
		return 0;
	}
	
	public boolean isValuable() {
		return getItem().getItemGrade() >= L2Item.CRYSTAL_S || (isAugmented() || isPvp());
	}
	
	private boolean isEventDrop;
	
	public boolean isEventDrop() {
		return isEventDrop;
	}
	
	public void isEventDrop(boolean mode) {
		isEventDrop = mode;
	}
	
	public int getStoneType() {
		if (getName().contains("Weapon") ||
				getItem().getStandardItem() > -1 && ItemTable.getInstance().getTemplate(getItem().getStandardItem()) instanceof L2Weapon) {
			return L2Item.TYPE2_WEAPON;
		}
		if (getName().contains("Armor")) {
			return L2Item.TYPE2_SHIELD_ARMOR;
		}
		if (getName().contains("Accessory")) {
			return L2Item.TYPE2_ACCESSORY;
		}
		return -1;
	}
}
