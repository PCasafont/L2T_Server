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
import l2server.gameserver.Reloadable;
import l2server.gameserver.ReloadableManager;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2ItemInstance.ItemLocation;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.instance.L2GrandBossInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.ItemParser;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.stats.funcs.FuncTemplate;
import l2server.gameserver.stats.funcs.LambdaConst;
import l2server.gameserver.templates.item.*;
import l2server.gameserver.util.GMAudit;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;

import static l2server.gameserver.model.itemcontainer.PcInventory.ADENA_ID;

/**
 * This class ...
 *
 * @version $Revision: 1.9.2.6.2.9 $ $Date: 2005/04/02 15:57:34 $
 */
public class ItemTable implements Reloadable
{

	public static final Map<String, Integer> _crystalTypes = new HashMap<>();
	public static final Map<String, Integer> _slots = new HashMap<>();
	public static final Map<String, L2WeaponType> _weaponTypes = new HashMap<>();
	public static final Map<String, L2ArmorType> _armorTypes = new HashMap<>();

	private L2Item[] _allTemplates;
	private Map<Integer, L2EtcItem> _etcItems;
	private Map<Integer, L2Armor> _armors;
	private Map<Integer, L2Weapon> _weapons;

	static
	{
		_crystalTypes.put("r99", L2Item.CRYSTAL_R99);
		_crystalTypes.put("r95", L2Item.CRYSTAL_R95);
		_crystalTypes.put("r", L2Item.CRYSTAL_R);
		_crystalTypes.put("s84", L2Item.CRYSTAL_S84);
		_crystalTypes.put("s80", L2Item.CRYSTAL_S80);
		_crystalTypes.put("s", L2Item.CRYSTAL_S);
		_crystalTypes.put("a", L2Item.CRYSTAL_A);
		_crystalTypes.put("b", L2Item.CRYSTAL_B);
		_crystalTypes.put("c", L2Item.CRYSTAL_C);
		_crystalTypes.put("d", L2Item.CRYSTAL_D);
		_crystalTypes.put("none", L2Item.CRYSTAL_NONE);

		// weapon types
		for (L2WeaponType type : L2WeaponType.values())
		{
			_weaponTypes.put(type.toString(), type);
		}

		// armor types
		for (L2ArmorType type : L2ArmorType.values())
		{
			_armorTypes.put(type.toString(), type);
		}

		_slots.put("shirt", L2Item.SLOT_UNDERWEAR);
		_slots.put("lbracelet", L2Item.SLOT_L_BRACELET);
		_slots.put("rbracelet", L2Item.SLOT_R_BRACELET);
		_slots.put("talisman", L2Item.SLOT_DECO);
		_slots.put("chest", L2Item.SLOT_CHEST);
		_slots.put("fullarmor", L2Item.SLOT_FULL_ARMOR);
		_slots.put("head", L2Item.SLOT_HEAD);
		_slots.put("hair", L2Item.SLOT_HAIR);
		_slots.put("hairall", L2Item.SLOT_HAIRALL);
		_slots.put("underwear", L2Item.SLOT_UNDERWEAR);
		_slots.put("back", L2Item.SLOT_BACK);
		_slots.put("neck", L2Item.SLOT_NECK);
		_slots.put("legs", L2Item.SLOT_LEGS);
		_slots.put("feet", L2Item.SLOT_FEET);
		_slots.put("gloves", L2Item.SLOT_GLOVES);
		_slots.put("chest,legs", L2Item.SLOT_CHEST | L2Item.SLOT_LEGS);
		_slots.put("belt", L2Item.SLOT_BELT);
		_slots.put("rhand", L2Item.SLOT_R_HAND);
		_slots.put("lhand", L2Item.SLOT_L_HAND);
		_slots.put("lrhand", L2Item.SLOT_LR_HAND);
		_slots.put("rear;lear", L2Item.SLOT_R_EAR | L2Item.SLOT_L_EAR);
		_slots.put("rfinger;lfinger", L2Item.SLOT_R_FINGER | L2Item.SLOT_L_FINGER);
		_slots.put("wolf", L2Item.SLOT_WOLF);
		_slots.put("greatwolf", L2Item.SLOT_GREATWOLF);
		_slots.put("hatchling", L2Item.SLOT_HATCHLING);
		_slots.put("strider", L2Item.SLOT_STRIDER);
		_slots.put("babypet", L2Item.SLOT_BABYPET);
		_slots.put("brooch", L2Item.SLOT_BROOCH);
		_slots.put("jewel", L2Item.SLOT_JEWELRY);
		_slots.put("none", L2Item.SLOT_NONE);

		//retail compatibility
		_slots.put("onepiece", L2Item.SLOT_FULL_ARMOR);
		_slots.put("hair2", L2Item.SLOT_HAIR2);
		_slots.put("dhair", L2Item.SLOT_HAIRALL);
		_slots.put("alldress", L2Item.SLOT_ALLDRESS);
		_slots.put("deco1", L2Item.SLOT_DECO);
		_slots.put("waist", L2Item.SLOT_BELT);
	}

	/**
	 * Returns instance of ItemTable
	 *
	 * @return ItemTable
	 */
	public static ItemTable getInstance()
	{
		return SingletonHolder._instance;
	}

	/**
	 * Constructor.
	 */
	private ItemTable()
	{
		_etcItems = new HashMap<>();
		_armors = new HashMap<>();
		_weapons = new HashMap<>();
		load();

		ReloadableManager.getInstance().register("items", this);
	}

	private void load()
	{
		int highest = 0;
		_armors.clear();
		_etcItems.clear();
		_weapons.clear();

		File dir = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "items");
		if (!dir.exists())
		{
			Log.warning("Dir " + dir.getAbsolutePath() + " does not exist");
			return;
		}
		List<File> validFiles = new ArrayList<>();
		File[] files = dir.listFiles();
		for (File f : files)
		{
			if (f.getName().endsWith(".xml") && !f.getName().startsWith("custom"))
			{
				validFiles.add(f);
			}
		}
		File customfile = new File(Config.DATAPACK_ROOT, "data_" + Config.SERVER_NAME + "/items.xml");
		if (customfile.exists())
		{
			validFiles.add(customfile);
		}

		Map<Integer, ItemParser> items = new HashMap<>();
		for (File f : validFiles)
		{
			XmlDocument doc = new XmlDocument(f);
			for (XmlNode n : doc.getChildren())
			{
				if (n.getName().equalsIgnoreCase("list"))
				{
					for (XmlNode d : n.getChildren())
					{
						if (d.getName().equalsIgnoreCase("item"))
						{
							ItemParser item = new ItemParser(d);
							try
							{
								ItemParser original = items.get(item.getId());
								if (original != null)
								{
									item.parse(original);
								}
								else
								{
									item.parse();
								}

								if (Config.isServer(Config.TENKAI) && item.getItem() instanceof L2Weapon &&
										(item.getName().contains("Antharas") || item.getName().contains("Valakas") ||
												item.getName().contains("Lindvior")))
								{
									item.getItem().attach(new FuncTemplate(null, "SubPercent", Stats.PHYS_ATTACK,
											new LambdaConst(50.0)));
									item.getItem().attach(new FuncTemplate(null, "SubPercent", Stats.MAGIC_ATTACK,
											new LambdaConst(30.0)));
								}

								items.put(item.getId(), item);
							}
							catch (Exception e)
							{
								Log.log(Level.WARNING, "Cannot create item " + item.getId(), e);
							}
						}
					}
				}
			}
		}

		for (ItemParser item : items.values())
		{
			if (highest < item.getItem().getItemId())
			{
				highest = item.getItem().getItemId();
			}
			if (item.getItem() instanceof L2EtcItem)
			{
				_etcItems.put(item.getId(), (L2EtcItem) item.getItem());
			}
			else if (item.getItem() instanceof L2Armor)
			{
				_armors.put(item.getId(), (L2Armor) item.getItem());
			}
			else
			{
				_weapons.put(item.getId(), (L2Weapon) item.getItem());
			}
		}
		buildFastLookupTable(highest);
	}

	/**
	 * Builds a variable in which all items are putting in in function of their ID.
	 */
	private void buildFastLookupTable(int size)
	{
		// Create a FastLookUp Table called _allTemplates of size : value of the highest item ID
		Log.info("Highest item id used:" + size);
		_allTemplates = new L2Item[size + 1];

		// Insert armor item in Fast Look Up Table
		for (L2Armor item : _armors.values())
		{
			_allTemplates[item.getItemId()] = item;
		}

		// Insert weapon item in Fast Look Up Table
		for (L2Weapon item : _weapons.values())
		{
			_allTemplates[item.getItemId()] = item;
		}

		// Insert etcItem item in Fast Look Up Table
		for (L2EtcItem item : _etcItems.values())
		{
			_allTemplates[item.getItemId()] = item;
		}
	}

	/**
	 * Returns the item corresponding to the item ID
	 *
	 * @param id : int designating the item
	 * @return L2Item
	 */
	public L2Item getTemplate(int id)
	{
		if (id >= _allTemplates.length)
		{
			return null;
		}
		else
		{
			return _allTemplates[id];
		}
	}

	/**
	 * Create the L2ItemInstance corresponding to the Item Identifier and quantitiy add logs the activity.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Create and Init the L2ItemInstance corresponding to the Item Identifier and quantity </li>
	 * <li>Add the L2ItemInstance object to _allObjects of L2world </li>
	 * <li>Logs Item creation according to log settings</li><BR><BR>
	 *
	 * @param process   : String Identifier of process triggering this action
	 * @param itemId    : int Item Identifier of the item to be created
	 * @param count     : int Quantity of items to be created for stackable items
	 * @param actor     : L2PcInstance Player requesting the item creation
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the new item
	 */
	public L2ItemInstance createItem(String process, int itemId, long count, L2PcInstance actor, Object reference)
	{
		// Create and Init the L2ItemInstance corresponding to the Item Identifier
		L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);

		if (process.equalsIgnoreCase("loot"))
		{
			ScheduledFuture<?> itemLootShedule;
			if (reference instanceof L2Attackable && ((L2Attackable) reference).isRaid()) // loot privilege for raids
			{
				L2Attackable raid = (L2Attackable) reference;
				boolean protectDrop = true;
				if (Config.isServer(Config.TENKAI))
				{
					protectDrop = !(raid instanceof L2GrandBossInstance) || raid.getInstanceId() != 0;
				}

				// if in CommandChannel and was killing a World/RaidBoss
				if (!Config.AUTO_LOOT_RAIDS && protectDrop)
				{
					if (raid.getFirstCommandChannelAttacked() != null)
					{
						item.setOwnerId(raid.getFirstCommandChannelAttacked().getChannelLeader().getObjectId());
					}
					else
					{
						item.setOwnerId(actor.getObjectId());
					}
					itemLootShedule = ThreadPoolManager.getInstance()
							.scheduleGeneral(new ResetOwner(item), Config.LOOT_RAIDS_PRIVILEGE_INTERVAL * 1000);
					item.setItemLootShedule(itemLootShedule);
				}
			}
			else if (!Config.AUTO_LOOT)
			{
				item.setOwnerId(actor.getObjectId());
				itemLootShedule = ThreadPoolManager.getInstance().scheduleGeneral(new ResetOwner(item), 15000);
				item.setItemLootShedule(itemLootShedule);
			}
		}

		if (Config.DEBUG)
		{
			Log.fine("ItemTable: Item created  oid:" + item.getObjectId() + " itemid:" + itemId);
		}

		// Add the L2ItemInstance object to _allObjects of L2world
		L2World.getInstance().storeObject(item);

		// Set Item parameters
		if (item.isStackable() && count > 1)
		{
			item.setCount(count);
		}

		if (Config.LOG_ITEMS && !process.equals("Reset") && !process.contains("Consume"))
		{
			if (!Config.LOG_ITEMS_SMALL_LOG || Config.LOG_ITEMS_SMALL_LOG &&
					(item.isEquipable() || item.getItemId() == ADENA_ID || item.getItemId() == 4037 ||
							item.getItemId() == 4355 || item.getItemId() == 4356))
			{
				L2ItemInstance
						.logItem(item.getItemId(), item.getObjectId(), item.getCount(), item.getOwnerId(), process);
			}
		}

		if (actor != null)
		{
			if (actor.isGM())
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
				String targetName = actor.getTarget() != null ? actor.getTarget().getName() : "no-target";
				if (Config.GMAUDIT)
				{
					GMAudit.auditGMAction(actor.getName(),
							process + " (id: " + itemId + " count: " + count + " name: " + item.getItemName() +
									" objId: " + item.getObjectId() + ")", targetName,
							"L2Object referencing this action is: " + referenceName);
				}
			}
		}

		return item;
	}

	public L2ItemInstance createItem(String process, int itemId, int count, L2PcInstance actor)
	{
		return createItem(process, itemId, count, actor, null);
	}

	/**
	 * Returns a dummy (fr = factice) item.<BR><BR>
	 * <U><I>Concept :</I></U><BR>
	 * Dummy item is created by setting the ID of the object in the world at null value
	 *
	 * @param itemId : int designating the item
	 * @return L2ItemInstance designating the dummy item created
	 */
	public L2ItemInstance createDummyItem(int itemId)
	{
		L2Item item = getTemplate(itemId);
		if (item == null)
		{
			return null;
		}
		return new L2ItemInstance(0, item);
	}

	/**
	 * Destroys the L2ItemInstance.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Sets L2ItemInstance parameters to be unusable </li>
	 * <li>Removes the L2ItemInstance object to _allObjects of L2world </li>
	 * <li>Logs Item delettion according to log settings</li><BR><BR>
	 *
	 * @param process   : String Identifier of process triggering this action
	 * @param item      : int Item Identifier of the item to be created
	 * @param actor     : L2PcInstance Player requesting the item destroy
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void destroyItem(String process, L2ItemInstance item, L2PcInstance actor, Object reference)
	{
		if (Config.LOG_ITEMS && !process.contains("Consume"))
		{
			if (!Config.LOG_ITEMS_SMALL_LOG || Config.LOG_ITEMS_SMALL_LOG &&
					(item.isEquipable() || item.getItemId() == ADENA_ID || item.getItemId() == 4037 ||
							item.getItemId() == 4355 || item.getItemId() == 4356))
			{
				L2ItemInstance
						.logItem(item.getItemId(), item.getObjectId(), item.getCount(), item.getOwnerId(), process);
			}
		}

		synchronized (item)
		{
			item.setCount(0);
			item.setOwnerId(0);
			item.setLocation(ItemLocation.VOID);
			item.setLastChange(L2ItemInstance.REMOVED);

			L2World.getInstance().removeObject(item);
			IdFactory.getInstance().releaseId(item.getObjectId());

			if (Config.LOG_ITEMS && !process.contains("Consume"))
			{
				if (!Config.LOG_ITEMS_SMALL_LOG ||
						Config.LOG_ITEMS_SMALL_LOG && (item.isEquipable() || item.getItemId() == ADENA_ID))
				{
					L2ItemInstance
							.logItem(item.getItemId(), item.getObjectId(), item.getCount(), item.getOwnerId(), process);
				}
			}

			if (actor != null)
			{
				if (actor.isGM())
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
					String targetName = actor.getTarget() != null ? actor.getTarget().getName() : "no-target";
					if (Config.GMAUDIT)
					{
						GMAudit.auditGMAction(actor.getName(),
								process + " (id: " + item.getItemId() + " count: " + item.getCount() + " itemObjId: " +
										item.getObjectId() + ")", targetName,
								"L2Object referencing this action is: " + referenceName);
					}
				}
			}

			// if it's a pet control item, delete the pet as well
			if (PetDataTable.isPetItem(item.getItemId()))
			{
				Connection con = null;
				try
				{
					// Delete the pet in db
					con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
					statement.setInt(1, item.getObjectId());
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					Log.log(Level.WARNING, "could not delete pet objectid:", e);
				}
				finally
				{
					L2DatabaseFactory.close(con);
				}
			}
		}
	}

	@Override
	public boolean reload()
	{
		load();
		EnchantHPBonusData.getInstance().reload();

		return true;
	}

	@Override
	public String getReloadMessage(boolean success)
	{
		return "Item Templates have been reloaded";
	}

	protected static class ResetOwner implements Runnable
	{
		L2ItemInstance _item;

		public ResetOwner(L2ItemInstance item)
		{
			_item = item;
		}

		@Override
		public void run()
		{
			_item.setOwnerId(0);
			_item.setItemLootShedule(null);
		}
	}

	public Set<Integer> getAllArmorsId()
	{
		return _armors.keySet();
	}

	public Set<Integer> getAllWeaponsId()
	{
		return _weapons.keySet();
	}

	public L2Item[] getAllItems()
	{
		return _allTemplates;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ItemTable _instance = new ItemTable();
	}
}
