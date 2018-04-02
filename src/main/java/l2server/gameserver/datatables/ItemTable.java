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

package l2server.gameserver.datatables;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.GameApplication;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.Item.ItemLocation;
import l2server.gameserver.model.World;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.instance.GrandBossInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.stats.ItemParser;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.stats.funcs.FuncTemplate;
import l2server.gameserver.stats.funcs.LambdaConst;
import l2server.gameserver.templates.item.*;
import l2server.gameserver.util.GMAudit;
import l2server.util.loader.annotations.Load;
import l2server.util.loader.annotations.Reload;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

import static l2server.gameserver.model.itemcontainer.PcInventory.ADENA_ID;

/**
 * This class ...
 *
 * @version $Revision: 1.9.2.6.2.9 $ $Date: 2005/04/02 15:57:34 $
 */
public class ItemTable {
	
	private static Logger log = LoggerFactory.getLogger(GameApplication.class.getName());
	
	public static final Map<String, Integer> crystalTypes = new HashMap<>();
	public static final Map<String, Integer> slots = new HashMap<>();
	public static final Map<String, WeaponType> weaponTypes = new HashMap<>();
	public static final Map<String, ArmorType> armorTypes = new HashMap<>();
	
	private ItemTemplate[] allTemplates;
	private Map<Integer, EtcItemTemplate> etcItems = new HashMap<>();
	private Map<Integer, ArmorTemplate> armors = new HashMap<>();
	private Map<Integer, WeaponTemplate> weapons = new HashMap<>();
	
	static {
		crystalTypes.put("r99", ItemTemplate.CRYSTAL_R99);
		crystalTypes.put("r95", ItemTemplate.CRYSTAL_R95);
		crystalTypes.put("r", ItemTemplate.CRYSTAL_R);
		crystalTypes.put("s84", ItemTemplate.CRYSTAL_S84);
		crystalTypes.put("s80", ItemTemplate.CRYSTAL_S80);
		crystalTypes.put("s", ItemTemplate.CRYSTAL_S);
		crystalTypes.put("a", ItemTemplate.CRYSTAL_A);
		crystalTypes.put("b", ItemTemplate.CRYSTAL_B);
		crystalTypes.put("c", ItemTemplate.CRYSTAL_C);
		crystalTypes.put("d", ItemTemplate.CRYSTAL_D);
		crystalTypes.put("none", ItemTemplate.CRYSTAL_NONE);
		
		// weapon types
		for (WeaponType type : WeaponType.values()) {
			weaponTypes.put(type.toString(), type);
		}
		
		// armor types
		for (ArmorType type : ArmorType.values()) {
			armorTypes.put(type.toString(), type);
		}
		
		slots.put("shirt", ItemTemplate.SLOT_UNDERWEAR);
		slots.put("lbracelet", ItemTemplate.SLOT_L_BRACELET);
		slots.put("rbracelet", ItemTemplate.SLOT_R_BRACELET);
		slots.put("talisman", ItemTemplate.SLOT_DECO);
		slots.put("chest", ItemTemplate.SLOT_CHEST);
		slots.put("fullarmor", ItemTemplate.SLOT_FULL_ARMOR);
		slots.put("head", ItemTemplate.SLOT_HEAD);
		slots.put("hair", ItemTemplate.SLOT_HAIR);
		slots.put("hairall", ItemTemplate.SLOT_HAIRALL);
		slots.put("underwear", ItemTemplate.SLOT_UNDERWEAR);
		slots.put("back", ItemTemplate.SLOT_BACK);
		slots.put("neck", ItemTemplate.SLOT_NECK);
		slots.put("legs", ItemTemplate.SLOT_LEGS);
		slots.put("feet", ItemTemplate.SLOT_FEET);
		slots.put("gloves", ItemTemplate.SLOT_GLOVES);
		slots.put("chest,legs", ItemTemplate.SLOT_CHEST | ItemTemplate.SLOT_LEGS);
		slots.put("belt", ItemTemplate.SLOT_BELT);
		slots.put("rhand", ItemTemplate.SLOT_R_HAND);
		slots.put("lhand", ItemTemplate.SLOT_L_HAND);
		slots.put("lrhand", ItemTemplate.SLOT_LR_HAND);
		slots.put("rear;lear", ItemTemplate.SLOT_R_EAR | ItemTemplate.SLOT_L_EAR);
		slots.put("rfinger;lfinger", ItemTemplate.SLOT_R_FINGER | ItemTemplate.SLOT_L_FINGER);
		slots.put("wolf", ItemTemplate.SLOT_WOLF);
		slots.put("greatwolf", ItemTemplate.SLOT_GREATWOLF);
		slots.put("hatchling", ItemTemplate.SLOT_HATCHLING);
		slots.put("strider", ItemTemplate.SLOT_STRIDER);
		slots.put("babypet", ItemTemplate.SLOT_BABYPET);
		slots.put("brooch", ItemTemplate.SLOT_BROOCH);
		slots.put("jewel", ItemTemplate.SLOT_JEWELRY);
		slots.put("none", ItemTemplate.SLOT_NONE);
		
		//retail compatibility
		slots.put("onepiece", ItemTemplate.SLOT_FULL_ARMOR);
		slots.put("hair2", ItemTemplate.SLOT_HAIR2);
		slots.put("dhair", ItemTemplate.SLOT_HAIRALL);
		slots.put("alldress", ItemTemplate.SLOT_ALLDRESS);
		slots.put("deco1", ItemTemplate.SLOT_DECO);
		slots.put("waist", ItemTemplate.SLOT_BELT);
	}
	
	/**
	 * Returns instance of ItemTable
	 *
	 * @return ItemTable
	 */
	public static ItemTable getInstance() {
		return SingletonHolder.instance;
	}
	
	/**
	 * Constructor.
	 */
	private ItemTable() {
	}
	
	@Reload("items")
	@Load
	public void load() {
		int highest = 0;
		armors.clear();
		etcItems.clear();
		weapons.clear();
		
		File dir = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "items");
		if (!dir.exists()) {
			log.warn("Dir " + dir.getAbsolutePath() + " does not exist");
			return;
		}
		List<File> validFiles = new ArrayList<>();
		File[] files = dir.listFiles();
		for (File f : files) {
			if (f.getName().endsWith(".xml") && !f.getName().startsWith("custom")) {
				validFiles.add(f);
			}
		}
		File customfile = new File(Config.DATAPACK_ROOT, "data_" + Config.SERVER_NAME + "/items.xml");
		if (customfile.exists()) {
			validFiles.add(customfile);
		}
		
		Map<Integer, ItemParser> items = new HashMap<>();
		for (File f : validFiles) {
			XmlDocument doc = new XmlDocument(f);
			for (XmlNode d : doc.getChildren()) {
				if (d.getName().equalsIgnoreCase("item")) {
					ItemParser item = new ItemParser(d);
					try {
						ItemParser original = items.get(item.getId());
						if (original != null) {
							item.parse(original);
						} else {
							item.parse();
						}
						
						if (Config.isServer(Config.TENKAI) && item.getItem() instanceof WeaponTemplate &&
								(item.getName().contains("Antharas") || item.getName().contains("Valakas") || item.getName().contains("Lindvior"))) {
							item.getItem().attach(new FuncTemplate(null, "SubPercent", Stats.PHYS_ATTACK, new LambdaConst(50.0)));
							item.getItem().attach(new FuncTemplate(null, "SubPercent", Stats.MAGIC_ATTACK, new LambdaConst(30.0)));
						}
						
						items.put(item.getId(), item);
					} catch (Exception e) {
						log.warn("Cannot create item " + item.getId(), e);
					}
				}
			}
		}
		
		for (ItemParser item : items.values()) {
			if (highest < item.getItem().getItemId()) {
				highest = item.getItem().getItemId();
			}
			if (item.getItem() instanceof EtcItemTemplate) {
				etcItems.put(item.getId(), (EtcItemTemplate) item.getItem());
			} else if (item.getItem() instanceof ArmorTemplate) {
				armors.put(item.getId(), (ArmorTemplate) item.getItem());
			} else {
				weapons.put(item.getId(), (WeaponTemplate) item.getItem());
			}
		}
		buildFastLookupTable(highest);
	}
	
	/**
	 * Builds a variable in which all items are putting in in function of their ID.
	 */
	private void buildFastLookupTable(int size) {
		// Create a FastLookUp Table called allTemplates of size : value of the highest item ID
		log.info("Highest item id used: " + size);
		allTemplates = new ItemTemplate[size + 1];
		
		// Insert armor item in Fast Look Up Table
		for (ArmorTemplate item : armors.values()) {
			allTemplates[item.getItemId()] = item;
		}
		
		// Insert weapon item in Fast Look Up Table
		for (WeaponTemplate item : weapons.values()) {
			allTemplates[item.getItemId()] = item;
		}
		
		// Insert etcItem item in Fast Look Up Table
		for (EtcItemTemplate item : etcItems.values()) {
			allTemplates[item.getItemId()] = item;
		}
	}
	
	/**
	 * Returns the item corresponding to the item ID
	 *
	 * @param id : int designating the item
	 * @return ItemTemplate
	 */
	public ItemTemplate getTemplate(int id) {
		if (id >= allTemplates.length) {
			return null;
		} else {
			return allTemplates[id];
		}
	}
	
	/**
	 * Create the Item corresponding to the Item Identifier and quantitiy add logs the activity.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Create and Init the Item corresponding to the Item Identifier and quantity </li>
	 * <li>Add the Item object to allObjects of L2world </li>
	 * <li>Logs Item creation according to log settings</li><BR><BR>
	 *
	 * @param process   : String Identifier of process triggering this action
	 * @param itemId    : int Item Identifier of the item to be created
	 * @param count     : int Quantity of items to be created for stackable items
	 * @param actor     : Player Player requesting the item creation
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the new item
	 */
	public Item createItem(String process, int itemId, long count, Player actor, Object reference) {
		// Create and Init the Item corresponding to the Item Identifier
		Item item = new Item(IdFactory.getInstance().getNextId(), itemId);
		
		if (process.equalsIgnoreCase("loot")) {
			ScheduledFuture<?> itemLootShedule;
			if (reference instanceof Attackable && ((Attackable) reference).isRaid()) // loot privilege for raids
			{
				Attackable raid = (Attackable) reference;
				boolean protectDrop = true;
				if (Config.isServer(Config.TENKAI)) {
					protectDrop = !(raid instanceof GrandBossInstance) || raid.getInstanceId() != 0;
				}
				
				// if in CommandChannel and was killing a World/RaidBoss
				if (!Config.AUTO_LOOT_RAIDS && protectDrop) {
					if (raid.getFirstCommandChannelAttacked() != null) {
						item.setOwnerId(raid.getFirstCommandChannelAttacked().getChannelLeader().getObjectId());
					} else {
						item.setOwnerId(actor.getObjectId());
					}
					itemLootShedule =
							ThreadPoolManager.getInstance().scheduleGeneral(new ResetOwner(item), Config.LOOT_RAIDS_PRIVILEGE_INTERVAL * 1000);
					item.setItemLootShedule(itemLootShedule);
				}
			} else if (!Config.AUTO_LOOT) {
				item.setOwnerId(actor.getObjectId());
				itemLootShedule = ThreadPoolManager.getInstance().scheduleGeneral(new ResetOwner(item), 15000);
				item.setItemLootShedule(itemLootShedule);
			}
		}
		
		if (Config.DEBUG) {
			log.debug("ItemTable: Item created  oid:" + item.getObjectId() + " itemid:" + itemId);
		}
		
		// Add the Item object to allObjects of L2world
		World.getInstance().storeObject(item);
		
		// Set Item parameters
		if (item.isStackable() && count > 1) {
			item.setCount(count);
		}
		
		if (Config.LOG_ITEMS && !process.equals("Reset") && !process.contains("Consume")) {
			if (!Config.LOG_ITEMS_SMALL_LOG || Config.LOG_ITEMS_SMALL_LOG &&
					(item.isEquipable() || item.getItemId() == ADENA_ID || item.getItemId() == 4037 || item.getItemId() == 4355 ||
							item.getItemId() == 4356)) {
				Item.logItem(item.getItemId(), item.getObjectId(), item.getCount(), item.getOwnerId(), process);
			}
		}
		
		if (actor != null) {
			if (actor.isGM()) {
				String referenceName = "no-reference";
				if (reference instanceof WorldObject) {
					referenceName = ((WorldObject) reference).getName() != null ? ((WorldObject) reference).getName() : "no-name";
				} else if (reference instanceof String) {
					referenceName = (String) reference;
				}
				String targetName = actor.getTarget() != null ? actor.getTarget().getName() : "no-target";
				if (Config.GMAUDIT) {
					GMAudit.auditGMAction(actor.getName(),
							process + " (id: " + itemId + " count: " + count + " name: " + item.getItemName() + " objId: " + item.getObjectId() + ")",
							targetName,
							"WorldObject referencing this action is: " + referenceName);
				}
			}
		}
		
		return item;
	}
	
	public Item createItem(String process, int itemId, int count, Player actor) {
		return createItem(process, itemId, count, actor, null);
	}
	
	/**
	 * Returns a dummy (fr = factice) item.<BR><BR>
	 * <U><I>Concept :</I></U><BR>
	 * Dummy item is created by setting the ID of the object in the world at null value
	 *
	 * @param itemId : int designating the item
	 * @return Item designating the dummy item created
	 */
	public Item createDummyItem(int itemId) {
		ItemTemplate item = getTemplate(itemId);
		if (item == null) {
			return null;
		}
		return new Item(0, item);
	}
	
	/**
	 * Destroys the Item.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Sets Item parameters to be unusable </li>
	 * <li>Removes the Item object to allObjects of L2world </li>
	 * <li>Logs Item delettion according to log settings</li><BR><BR>
	 *
	 * @param process   : String Identifier of process triggering this action
	 * @param item      : int Item Identifier of the item to be created
	 * @param actor     : Player Player requesting the item destroy
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void destroyItem(String process, Item item, Player actor, Object reference) {
		if (Config.LOG_ITEMS && !process.contains("Consume")) {
			if (!Config.LOG_ITEMS_SMALL_LOG || Config.LOG_ITEMS_SMALL_LOG &&
					(item.isEquipable() || item.getItemId() == ADENA_ID || item.getItemId() == 4037 || item.getItemId() == 4355 ||
							item.getItemId() == 4356)) {
				Item.logItem(item.getItemId(), item.getObjectId(), item.getCount(), item.getOwnerId(), process);
			}
		}
		
		synchronized (item) {
			item.setCount(0);
			item.setOwnerId(0);
			item.setLocation(ItemLocation.VOID);
			item.setLastChange(Item.REMOVED);
			
			World.getInstance().removeObject(item);
			IdFactory.getInstance().releaseId(item.getObjectId());
			
			if (Config.LOG_ITEMS && !process.contains("Consume")) {
				if (!Config.LOG_ITEMS_SMALL_LOG || Config.LOG_ITEMS_SMALL_LOG && (item.isEquipable() || item.getItemId() == ADENA_ID)) {
					Item.logItem(item.getItemId(), item.getObjectId(), item.getCount(), item.getOwnerId(), process);
				}
			}
			
			if (actor != null) {
				if (actor.isGM()) {
					String referenceName = "no-reference";
					if (reference instanceof WorldObject) {
						referenceName = ((WorldObject) reference).getName() != null ? ((WorldObject) reference).getName() : "no-name";
					} else if (reference instanceof String) {
						referenceName = (String) reference;
					}
					String targetName = actor.getTarget() != null ? actor.getTarget().getName() : "no-target";
					if (Config.GMAUDIT) {
						GMAudit.auditGMAction(actor.getName(),
								process + " (id: " + item.getItemId() + " count: " + item.getCount() + " itemObjId: " + item.getObjectId() + ")",
								targetName,
								"WorldObject referencing this action is: " + referenceName);
					}
				}
			}
			
			// if it's a pet control item, delete the pet as well
			if (PetDataTable.isPetItem(item.getItemId())) {
				Connection con = null;
				try {
					// Delete the pet in db
					con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
					statement.setInt(1, item.getObjectId());
					statement.execute();
					statement.close();
				} catch (Exception e) {
					log.warn("could not delete pet objectid:", e);
				} finally {
					L2DatabaseFactory.close(con);
				}
			}
		}
	}
	
	protected static class ResetOwner implements Runnable {
		Item item;
		
		public ResetOwner(Item item) {
			this.item = item;
		}
		
		@Override
		public void run() {
			item.setOwnerId(0);
			item.setItemLootShedule(null);
		}
	}
	
	public Set<Integer> getAllArmorsId() {
		return armors.keySet();
	}
	
	public Set<Integer> getAllWeaponsId() {
		return weapons.keySet();
	}
	
	public ItemTemplate[] getAllItems() {
		return allTemplates;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final ItemTable instance = new ItemTable();
	}
}
