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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.RecipeController;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2RecipeList;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.item.L2EtcItemType;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.templates.item.L2WarehouseItem;
import l2server.log.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 0x42 WarehouseWithdrawalList  dh (h dddhh dhhh d)
 *
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/29 23:15:10 $
 */

public class SortedWareHouseWithdrawalList extends L2ItemListPacket {
	public static final int PRIVATE = 1;
	public static final int CLAN = 2;
	public static final int CASTLE = 3; //not sure
	public static final int FREIGHT = 4; //not sure
	
	private L2PcInstance activeChar;
	private long playerAdena;
	private List<L2WarehouseItem> objects = new ArrayList<>();
	private int whType;
	private byte sortorder;
	private WarehouseListType itemtype;
	
	public enum WarehouseListType {
		WEAPON,
		ARMOR,
		ETCITEM,
		RECIPE,
		AMULETT,
		SPELLBOOK,
		SHOT,
		SCROLL,
		CONSUMABLE,
		SEED,
		POTION,
		QUEST,
		PET,
		OTHER,
		ALL
	}
	
	/**
	 * sort order A..Z
	 */
	public static final byte A2Z = 1;
	/**
	 * sort order Z..A
	 */
	public static final byte Z2A = -1;
	/**
	 * sort order Grade non..S
	 */
	public static final byte GRADE = 2;
	/**
	 * sort order Recipe Level 1..9
	 */
	public static final byte LEVEL = 3;
	/**
	 * sort order body part (wearing)
	 */
	public static final byte WEAR = 4;
	/**
	 * Maximum Items to put into list
	 */
	public static final int MAX_SORT_LIST_ITEMS = 300;
	
	/**
	 * This will instantiate the Warehouselist the Player asked for
	 *
	 * @param player    who calls for the itemlist
	 * @param type      is the Warehouse Type
	 * @param itemtype  is the Itemtype to sort for
	 * @param sortorder is the integer Sortorder like 1 for A..Z (use public constant)
	 */
	public SortedWareHouseWithdrawalList(L2PcInstance player, int type, WarehouseListType itemtype, byte sortorder) {
		activeChar = player;
		whType = type;
		this.itemtype = itemtype;
		this.sortorder = sortorder;
		
		playerAdena = activeChar.getAdena();
		if (activeChar.getActiveWarehouse() == null) {
			// Something went wrong!
			Log.warning("error while sending withdraw request to: " + activeChar.getName());
			return;
		}
		
		switch (itemtype) {
			case WEAPON:
				objects = createWeaponList(activeChar.getActiveWarehouse().getItems());
				break;
			case ARMOR:
				objects = createArmorList(activeChar.getActiveWarehouse().getItems());
				break;
			case ETCITEM:
				objects = createEtcItemList(activeChar.getActiveWarehouse().getItems());
				break;
			case RECIPE:
				objects = createRecipeList(activeChar.getActiveWarehouse().getItems());
				break;
			case AMULETT:
				objects = createAmulettList(activeChar.getActiveWarehouse().getItems());
				break;
			case SPELLBOOK:
				objects = createSpellbookList(activeChar.getActiveWarehouse().getItems());
				break;
			case CONSUMABLE:
				objects = createConsumableList(activeChar.getActiveWarehouse().getItems());
				break;
			case SHOT:
				objects = createShotList(activeChar.getActiveWarehouse().getItems());
				break;
			case SCROLL:
				objects = createScrollList(activeChar.getActiveWarehouse().getItems());
				break;
			case SEED:
				objects = createSeedList(activeChar.getActiveWarehouse().getItems());
				break;
			case OTHER:
				objects = createOtherList(activeChar.getActiveWarehouse().getItems());
				break;
			case ALL:
			default:
				objects = createAllList(activeChar.getActiveWarehouse().getItems());
				break;
		}
		
		try {
			switch (sortorder) {
				case A2Z:
				case Z2A:
					Collections.sort(objects, new WarehouseItemNameComparator(sortorder));
					break;
				case GRADE:
					if (itemtype == WarehouseListType.ARMOR || itemtype == WarehouseListType.WEAPON) {
						Collections.sort(objects, new WarehouseItemNameComparator(A2Z));
						Collections.sort(objects, new WarehouseItemGradeComparator(A2Z));
					}
					break;
				case LEVEL:
					if (itemtype == WarehouseListType.RECIPE) {
						Collections.sort(objects, new WarehouseItemNameComparator(A2Z));
						Collections.sort(objects, new WarehouseItemRecipeComparator(A2Z));
					}
					break;
				case WEAR:
					if (itemtype == WarehouseListType.ARMOR) {
						Collections.sort(objects, new WarehouseItemNameComparator(A2Z));
						Collections.sort(objects, new WarehouseItemBodypartComparator(A2Z));
					}
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This public method return the integer of the Sortorder by its name.
	 * If you want to have another, add the Comparator and the Constant.
	 *
	 * @param order
	 * @return the integer of the sortorder or 1 as default value
	 */
	public static byte getOrder(String order) {
		if (order == null) {
			return A2Z;
		} else if (order.startsWith("A2Z")) {
			return A2Z;
		} else if (order.startsWith("Z2A")) {
			return Z2A;
		} else if (order.startsWith("GRADE")) {
			return GRADE;
		} else if (order.startsWith("WEAR")) {
			return WEAR;
		} else {
			try {
				return Byte.parseByte(order);
			} catch (NumberFormatException ex) {
				return A2Z;
			}
		}
	}
	
	/**
	 * This is the common Comparator to sort the items by Name
	 */
	private static class WarehouseItemNameComparator implements Comparator<L2WarehouseItem> {
		private byte order = 0;
		
		protected WarehouseItemNameComparator(byte sortOrder) {
			order = sortOrder;
		}
		
		@Override
		public int compare(L2WarehouseItem o1, L2WarehouseItem o2) {
			if (o1.getType2() == L2Item.TYPE2_MONEY && o2.getType2() != L2Item.TYPE2_MONEY) {
				return order == A2Z ? Z2A : A2Z;
			}
			if (o2.getType2() == L2Item.TYPE2_MONEY && o1.getType2() != L2Item.TYPE2_MONEY) {
				return order == A2Z ? A2Z : Z2A;
			}
			String s1 = o1.getItemName();
			String s2 = o2.getItemName();
			return order == A2Z ? s1.compareTo(s2) : s2.compareTo(s1);
		}
	}
	
	/**
	 * This Comparator is used to sort by Recipe Level
	 */
	private static class WarehouseItemRecipeComparator implements Comparator<L2WarehouseItem> {
		private int order = 0;
		
		private RecipeController rc = null;
		
		protected WarehouseItemRecipeComparator(int sortOrder) {
			order = sortOrder;
			rc = RecipeController.getInstance();
		}
		
		@Override
		public int compare(L2WarehouseItem o1, L2WarehouseItem o2) {
			if (o1.getType2() == L2Item.TYPE2_MONEY && o2.getType2() != L2Item.TYPE2_MONEY) {
				return order == A2Z ? Z2A : A2Z;
			}
			if (o2.getType2() == L2Item.TYPE2_MONEY && o1.getType2() != L2Item.TYPE2_MONEY) {
				return order == A2Z ? A2Z : Z2A;
			}
			if (o1.isEtcItem() && o1.getItemType() == L2EtcItemType.RECIPE && o2.isEtcItem() && o2.getItemType() == L2EtcItemType.RECIPE) {
				try {
					L2RecipeList rp1 = rc.getRecipeByItemId(o1.getItemId());
					L2RecipeList rp2 = rc.getRecipeByItemId(o2.getItemId());
					
					if (rp1 == null) {
						return order == A2Z ? A2Z : Z2A;
					}
					if (rp2 == null) {
						return order == A2Z ? Z2A : A2Z;
					}
					
					Integer i1 = rp1.getLevel();
					Integer i2 = rp2.getLevel();
					
					return order == A2Z ? i1.compareTo(i2) : i2.compareTo(i1);
				} catch (Exception e) {
					return 0;
				}
			} else {
				String s1 = o1.getItemName();
				String s2 = o2.getItemName();
				return order == A2Z ? s1.compareTo(s2) : s2.compareTo(s1);
			}
		}
	}
	
	/**
	 * This Comparator is used to sort the Items by BodyPart
	 */
	private static class WarehouseItemBodypartComparator implements Comparator<L2WarehouseItem> {
		private byte order = 0;
		
		protected WarehouseItemBodypartComparator(byte sortOrder) {
			order = sortOrder;
		}
		
		@Override
		public int compare(L2WarehouseItem o1, L2WarehouseItem o2) {
			if (o1.getType2() == L2Item.TYPE2_MONEY && o2.getType2() != L2Item.TYPE2_MONEY) {
				return order == A2Z ? Z2A : A2Z;
			}
			if (o2.getType2() == L2Item.TYPE2_MONEY && o1.getType2() != L2Item.TYPE2_MONEY) {
				return order == A2Z ? A2Z : Z2A;
			}
			Integer i1 = o1.getBodyPart();
			Integer i2 = o2.getBodyPart();
			return order == A2Z ? i1.compareTo(i2) : i2.compareTo(i1);
		}
	}
	
	/**
	 * This Comparator is used to sort by the Item Grade (e.g. Non..S-Grade)
	 */
	private static class WarehouseItemGradeComparator implements Comparator<L2WarehouseItem> {
		byte order = 0;
		
		protected WarehouseItemGradeComparator(byte sortOrder) {
			order = sortOrder;
		}
		
		@Override
		public int compare(L2WarehouseItem o1, L2WarehouseItem o2) {
			if (o1.getType2() == L2Item.TYPE2_MONEY && o2.getType2() != L2Item.TYPE2_MONEY) {
				return order == A2Z ? Z2A : A2Z;
			}
			if (o2.getType2() == L2Item.TYPE2_MONEY && o1.getType2() != L2Item.TYPE2_MONEY) {
				return order == A2Z ? A2Z : Z2A;
			}
			Integer i1 = o1.getItemGrade();
			Integer i2 = o2.getItemGrade();
			return order == A2Z ? i1.compareTo(i2) : i2.compareTo(i1);
		}
	}
	
	// ========================================================================
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Weapon</li>
	 * <li>Arrow</li>
	 * <li>Money</li>
	 *
	 * @param items complete Warehouse List
	 * @return limited Item List
	 */
	private List<L2WarehouseItem> createWeaponList(L2ItemInstance[] items) {
		List<L2WarehouseItem> list = new ArrayList<>();
		for (L2ItemInstance item : items) {
			if (item.isWeapon() || item.getItem().getType2() == L2Item.TYPE2_WEAPON ||
					item.isEtcItem() && item.getItemType() == L2EtcItemType.ARROW || item.getItem().getType2() == L2Item.TYPE2_MONEY) {
				if (list.size() < MAX_SORT_LIST_ITEMS) {
					list.add(new L2WarehouseItem(item));
				} else {
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Armor</li>
	 * <li>Money</li>
	 *
	 * @param items complete Warehouse List
	 * @return limited Item List
	 */
	private List<L2WarehouseItem> createArmorList(L2ItemInstance[] items) {
		List<L2WarehouseItem> list = new ArrayList<>();
		for (L2ItemInstance item : items) {
			if (item.isArmor() || item.getItem().getType2() == L2Item.TYPE2_MONEY) {
				if (list.size() < MAX_SORT_LIST_ITEMS) {
					list.add(new L2WarehouseItem(item));
				} else {
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Everything which is no Weapon/Armor</li>
	 * <li>Money</li>
	 *
	 * @param items complete Warehouse List
	 * @return limited Item List
	 */
	private List<L2WarehouseItem> createEtcItemList(L2ItemInstance[] items) {
		List<L2WarehouseItem> list = new ArrayList<>();
		for (L2ItemInstance item : items) {
			if (item.isEtcItem() || item.getItem().getType2() == L2Item.TYPE2_MONEY) {
				if (list.size() < MAX_SORT_LIST_ITEMS) {
					list.add(new L2WarehouseItem(item));
				} else {
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Recipes</li>
	 * <li>Money</li>
	 *
	 * @param items complete Warehouse List
	 * @return limited Item List
	 */
	private List<L2WarehouseItem> createRecipeList(L2ItemInstance[] items) {
		List<L2WarehouseItem> list = new ArrayList<>();
		for (L2ItemInstance item : items) {
			if (item.isEtcItem() && item.getEtcItem().getItemType() == L2EtcItemType.RECIPE || item.getItem().getType2() == L2Item.TYPE2_MONEY) {
				if (list.size() < MAX_SORT_LIST_ITEMS) {
					list.add(new L2WarehouseItem(item));
				} else {
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Amulett</li>
	 * <li>Money</li>
	 *
	 * @param items complete Warehouse List
	 * @return limited Item List
	 */
	private List<L2WarehouseItem> createAmulettList(L2ItemInstance[] items) {
		List<L2WarehouseItem> list = new ArrayList<>();
		for (L2ItemInstance item : items) {
			if (item.isEtcItem() && item.getItemName().toUpperCase().startsWith("AMULET") || item.getItem().getType2() == L2Item.TYPE2_MONEY) {
				if (list.size() < MAX_SORT_LIST_ITEMS) {
					list.add(new L2WarehouseItem(item));
				} else {
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Spellbook & Dwarven Drafts</li>
	 * <li>Money</li>
	 *
	 * @param items complete Warehouse List
	 * @return limited Item List
	 */
	private List<L2WarehouseItem> createSpellbookList(L2ItemInstance[] items) {
		List<L2WarehouseItem> list = new ArrayList<>();
		for (L2ItemInstance item : items) {
			if (item.isEtcItem() && !item.getItemName().toUpperCase().startsWith("AMULET") || item.getItem().getType2() == L2Item.TYPE2_MONEY) {
				if (list.size() < MAX_SORT_LIST_ITEMS) {
					list.add(new L2WarehouseItem(item));
				} else {
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Consumables (Potions, Shots, ...)</li>
	 * <li>Money</li>
	 *
	 * @param items complete Warehouse List
	 * @return limited Item List
	 */
	private List<L2WarehouseItem> createConsumableList(L2ItemInstance[] items) {
		List<L2WarehouseItem> list = new ArrayList<>();
		for (L2ItemInstance item : items) {
			if (item.isEtcItem() &&
					(item.getEtcItem().getItemType() == L2EtcItemType.SCROLL || item.getEtcItem().getItemType() == L2EtcItemType.SHOT) ||
					item.getItem().getType2() == L2Item.TYPE2_MONEY) {
				if (list.size() < MAX_SORT_LIST_ITEMS) {
					list.add(new L2WarehouseItem(item));
				} else {
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Shots</li>
	 * <li>Money</li>
	 *
	 * @param items complete Warehouse List
	 * @return limited Item List
	 */
	private List<L2WarehouseItem> createShotList(L2ItemInstance[] items) {
		List<L2WarehouseItem> list = new ArrayList<>();
		for (L2ItemInstance item : items) {
			if (item.isEtcItem() && item.getEtcItem().getItemType() == L2EtcItemType.SHOT || item.getItem().getType2() == L2Item.TYPE2_MONEY) {
				if (list.size() < MAX_SORT_LIST_ITEMS) {
					list.add(new L2WarehouseItem(item));
				} else {
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Scrolls/Potions</li>
	 * <li>Money</li>
	 *
	 * @param items complete Warehouse List
	 * @return limited Item List
	 */
	private List<L2WarehouseItem> createScrollList(L2ItemInstance[] items) {
		List<L2WarehouseItem> list = new ArrayList<>();
		for (L2ItemInstance item : items) {
			if (item.isEtcItem() && item.getEtcItem().getItemType() == L2EtcItemType.SCROLL || item.getItem().getType2() == L2Item.TYPE2_MONEY) {
				if (list.size() < MAX_SORT_LIST_ITEMS) {
					list.add(new L2WarehouseItem(item));
				} else {
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Seeds</li>
	 * <li>Money</li>
	 *
	 * @param items complete Warehouse List
	 * @return limited Item List
	 */
	private List<L2WarehouseItem> createSeedList(L2ItemInstance[] items) {
		List<L2WarehouseItem> list = new ArrayList<>();
		for (L2ItemInstance item : items) {
			if (item.isEtcItem() && item.getEtcItem().getItemType() == L2EtcItemType.SEED || item.getItem().getType2() == L2Item.TYPE2_MONEY) {
				if (list.size() < MAX_SORT_LIST_ITEMS) {
					list.add(new L2WarehouseItem(item));
				} else {
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>Everything which is no Weapon/Armor, Material, Recipe, Spellbook, Scroll or Shot</li>
	 * <li>Money</li>
	 *
	 * @param items complete Warehouse List
	 * @return limited Item List
	 */
	private List<L2WarehouseItem> createOtherList(L2ItemInstance[] items) {
		List<L2WarehouseItem> list = new ArrayList<>();
		for (L2ItemInstance item : items) {
			if (item.isEtcItem() && item.getEtcItem().getItemType() != L2EtcItemType.MATERIAL &&
					item.getEtcItem().getItemType() != L2EtcItemType.RECIPE && item.getEtcItem().getItemType() != L2EtcItemType.SCROLL &&
					item.getEtcItem().getItemType() != L2EtcItemType.SHOT || item.getItem().getType2() == L2Item.TYPE2_MONEY) {
				if (list.size() < MAX_SORT_LIST_ITEMS) {
					list.add(new L2WarehouseItem(item));
				} else {
				}
			}
		}
		return list;
	}
	
	/**
	 * This method is used to limit the given Warehouse List to:
	 * <li>no limit</li>
	 * This may sound strange but we return the given Array as a List<L2WarehouseItem>
	 *
	 * @param items complete Warehouse List
	 * @return limited Item List
	 */
	private List<L2WarehouseItem> createAllList(L2ItemInstance[] items) {
		List<L2WarehouseItem> list = new ArrayList<>();
		for (L2ItemInstance item : items) {
			if (list.size() < MAX_SORT_LIST_ITEMS) {
				list.add(new L2WarehouseItem(item));
			} else {
			}
		}
		return list;
	}
	
	@Override
	protected final void writeImpl() {
		/* 0x01-Private Warehouse
		 * 0x02-Clan Warehouse
		 * 0x03-Castle Warehouse
		 * 0x04-Warehouse */
		writeH(whType);
		writeQ(playerAdena);
		writeH(objects.size());
		writeH(0x00); // GoD ???
		writeD(0x00); // TODO: Amount of already deposited items
		
		for (L2WarehouseItem item : objects) {
			writeItem(item);
			
			writeD(item.getObjectId());
			
			writeD(0x00); // GoD ???
			writeD(0x00); // GoD ???
		}
	}
	
	@Override
	protected final Class<?> getOpCodeClass() {
		return WareHouseWithdrawalList.class;
	}
}
