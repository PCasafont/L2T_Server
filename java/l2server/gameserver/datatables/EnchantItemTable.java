package l2server.gameserver.datatables;

import gnu.trove.TIntObjectHashMap;
import l2server.Config;
import l2server.gameserver.Reloadable;
import l2server.gameserver.ReloadableManager;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.templates.item.L2WeaponType;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;

public class EnchantItemTable implements Reloadable
{
	public enum EnchantTargetType
	{
		WEAPON, ARMOR, HAIR_ACCESSORY, ELEMENTAL_SHIRT
	}

	public class EnchantSupportItem
	{
		protected final EnchantTargetType targetType;
		protected final int grade;
		protected final int maxEnchantLevel;
		protected final int chanceAdd;
		protected final int itemId;

		public EnchantSupportItem(EnchantTargetType targetType, int type, int level, int chance, int items)
		{
			this.targetType = targetType;
			grade = type;
			maxEnchantLevel = level;
			chanceAdd = chance;
			itemId = items;
		}

		/*
		 * Return true if support item can be used for this item
		 */
		public final boolean isValid(L2ItemInstance enchantItem)
		{
			if (enchantItem == null)
			{
				return false;
			}

			int type2 = enchantItem.getItem().getType2();

			// checking scroll type and configured maximum enchant level
			switch (type2)
			{
				// weapon scrolls can enchant only weapons
				case L2Item.TYPE2_WEAPON:
					if (targetType != EnchantTargetType.WEAPON ||
							Config.ENCHANT_MAX_WEAPON > 0 && enchantItem.getEnchantLevel() >= Config.ENCHANT_MAX_WEAPON)
					{
						return false;
					}
					break;
				// armor scrolls can enchant only accessory and armors
				case L2Item.TYPE2_SHIELD_ARMOR:
					if (targetType != EnchantTargetType.ELEMENTAL_SHIRT && (targetType != EnchantTargetType.ARMOR ||
							Config.ENCHANT_MAX_ARMOR > 0 && enchantItem.getEnchantLevel() >= Config.ENCHANT_MAX_ARMOR))
					{
						return false;
					}
					break;
				case L2Item.TYPE2_ACCESSORY:
					if ((enchantItem.getItem().getBodyPart() &
							(L2Item.SLOT_HAIR | L2Item.SLOT_HAIR2 | L2Item.SLOT_HAIRALL)) > 0)
					{
						if (targetType != EnchantTargetType.HAIR_ACCESSORY || Config.ENCHANT_MAX_JEWELRY > 0 &&
								enchantItem.getEnchantLevel() >= Config.ENCHANT_MAX_JEWELRY)
						{
							return false;
						}
						break;
					}
					if (targetType != EnchantTargetType.ARMOR || Config.ENCHANT_MAX_JEWELRY > 0 &&
							enchantItem.getEnchantLevel() >= Config.ENCHANT_MAX_JEWELRY)
					{
						return false;
					}
					break;
				default:
					return false;
			}

			// check for crystal types
			if (grade != L2Item.CRYSTAL_NONE && grade != enchantItem.getItem().getItemGradePlain())
			{
				return false;
			}

			// check for maximum enchant level
			if (maxEnchantLevel != 0 && enchantItem.getEnchantLevel() >= maxEnchantLevel)
			{
				return false;
			}

			return !(itemId > 0 && enchantItem.getItemId() != itemId);
		}

		/*
		 * return chance increase
		 */
		public final int getChanceAdd()
		{
			return chanceAdd;
		}
	}

	public final class EnchantScroll extends EnchantSupportItem
	{
		private final boolean isBlessed;
		private final boolean isCrystal;
		private final boolean isSafe;

		public EnchantScroll(EnchantTargetType targetType, boolean bless, boolean crystal, boolean safe, int type, int level, int chance, int item)
		{
			super(targetType, type, level, chance, item);

			isBlessed = bless;
			isCrystal = crystal;
			isSafe = safe;
		}

		/*
		 * Return true for blessed scrolls
		 */
		public final boolean isBlessed()
		{
			return isBlessed;
		}

		/*
		 * Return true for crystal scrolls
		 */
		public final boolean isCrystal()
		{
			return isCrystal;
		}

		/*
		 * Return true for safe-enchant scrolls (enchant level will remain on failure)
		 */
		public final boolean isSafe()
		{
			if (Config.ENCHANT_ALWAYS_SAFE)
			{
				return true;
			}

			return isSafe;
		}

		public final boolean isValid(L2ItemInstance enchantItem, EnchantSupportItem supportItem)
		{
			// blessed scrolls can't use support items
			if (supportItem != null && (!supportItem.isValid(enchantItem) || isBlessed()))
			{
				return false;
			}

			return isValid(enchantItem);
		}

		public final float getChance(L2ItemInstance enchantItem, EnchantSupportItem supportItem)
		{
			if (!isValid(enchantItem, supportItem))
			{
				return -1;
			}

			boolean fullBody = enchantItem.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR;
			if (enchantItem.getEnchantLevel() < Config.ENCHANT_SAFE_MAX ||
					fullBody && enchantItem.getEnchantLevel() < Config.ENCHANT_SAFE_MAX_FULL)
			{
				return 100;
			}

			boolean isAccessory = enchantItem.getItem().getType2() == L2Item.TYPE2_ACCESSORY;

			if (Config.ENCHANT_CHANCE_PER_LEVEL.length > 0)
			{
				if (enchantItem.getEnchantLevel() >= Config.ENCHANT_CHANCE_PER_LEVEL.length)
				{
					return 0;
				}

				if (isBlessed)
				{
					return Config.BLESSED_ENCHANT_CHANCE_PER_LEVEL[enchantItem.getEnchantLevel()];
				}
				else
				{
					return Config.ENCHANT_CHANCE_PER_LEVEL[enchantItem.getEnchantLevel()];
				}
			}

			float chance = 0;
			if (isBlessed)
			{
				// blessed scrolls does not use support items
				if (supportItem != null)
				{
					return -1;
				}

				if (targetType == EnchantTargetType.WEAPON)
				{
					chance = Config.BLESSED_ENCHANT_CHANCE_WEAPON;
				}
				else if (isAccessory)
				{
					chance = Config.BLESSED_ENCHANT_CHANCE_JEWELRY;
				}
				else
				{
					chance = Config.BLESSED_ENCHANT_CHANCE_ARMOR;
				}
			}
			else
			{
				if (targetType == EnchantTargetType.WEAPON)
				{
					chance = Config.ENCHANT_CHANCE_WEAPON;
				}
				else if (isAccessory)
				{
					chance = Config.ENCHANT_CHANCE_JEWELRY;
				}
				else
				{
					chance = Config.ENCHANT_CHANCE_ARMOR;
				}
			}

			chance += chanceAdd;

			if (supportItem != null)
			{
				chance += supportItem.getChanceAdd();
			}

			return chance;
		}
	}

	private final TIntObjectHashMap<EnchantScroll> scrolls = new TIntObjectHashMap<>();
	private final TIntObjectHashMap<EnchantSupportItem> supports = new TIntObjectHashMap<>();

	private static EnchantItemTable instance;

	public static EnchantItemTable getInstance()
	{
		if (instance == null)
		{
			instance = new EnchantItemTable();
		}

		return instance;
	}

	private EnchantItemTable()
	{
		reload();

		ReloadableManager.getInstance().register("globaldrops", this);
	}

	@Override
	public boolean reload()
	{
		scrolls.clear();
		supports.clear();
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "enchantItems.xml");
		XmlDocument doc = new XmlDocument(file);

		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				for (XmlNode d : n.getChildren())
				{
					if (d.getName().equalsIgnoreCase("enchantScroll"))
					{
						int id = d.getInt("id");
						EnchantTargetType targetType = EnchantTargetType.valueOf(d.getString("targetType"));
						int grade = ItemTable.crystalTypes.get(d.getString("grade", "none"));
						boolean isBlessed = d.getBool("isBlessed", false);
						boolean isCrystal = d.getBool("isCrystal", false);
						boolean isSafe = d.getBool("isSafe", false);
						int maxLevel = d.getInt("maxLevel", 0);
						int extraChance = d.getInt("extraChance", 0);
						int onlyOnItem = d.getInt("onlyOnItem", 0);

						scrolls.put(id, new EnchantScroll(targetType, isBlessed, isCrystal, isSafe, grade, maxLevel,
								extraChance, onlyOnItem));
					}
					else if (d.getName().equalsIgnoreCase("enchantSupportItem"))
					{
						int id = d.getInt("id");
						EnchantTargetType targetType = EnchantTargetType.valueOf(d.getString("targetType"));
						int grade = ItemTable.crystalTypes.get(d.getString("grade", "none"));
						int maxLevel = d.getInt("maxLevel", 0);
						int extraChance = d.getInt("extraChance", 0);
						int onlyOnItem = d.getInt("onlyOnItem", 0);

						supports.put(id, new EnchantSupportItem(targetType, grade, maxLevel, extraChance, onlyOnItem));
					}
				}
			}
		}

		return true;
	}

	@Override
	public String getReloadMessage(boolean success)
	{
		return "Enchant Items reloaded";
	}

	/**
	 * Return enchant template for scroll
	 */
	public final EnchantScroll getEnchantScroll(L2ItemInstance scroll)
	{
		return scrolls.get(scroll.getItemId());
	}

	/**
	 * Return enchant template for support item
	 */
	public final EnchantSupportItem getSupportItem(L2ItemInstance item)
	{
		return supports.get(item.getItemId());
	}

	/**
	 * Return true if item can be enchanted
	 */
	public static boolean isEnchantable(L2ItemInstance item)
	{
		if (item.isHeroItem() && !item.getItem().isEnchantable())
		{
			return false;
		}
		if (item.isShadowItem())
		{
			return false;
		}
		if (item.isCommonItem())
		{
			return false;
		}
		if (item.isEtcItem())
		{
			return false;
		}
		if (item.isTimeLimitedItem())
		{
			return false;
		}
		// rods
		if (item.getItem().getItemType() == L2WeaponType.FISHINGROD)
		{
			return false;
		}
		// bracelets
		if (item.getItem().getBodyPart() == L2Item.SLOT_L_BRACELET)
		{
			return false;
		}
		if (item.getItem().getBodyPart() == L2Item.SLOT_R_BRACELET)
		{
			return false;
		}
		if (item.getItem().getBodyPart() == L2Item.SLOT_BACK)
		{
			return false;
		}
		if (item.getItem().getBodyPart() == L2Item.SLOT_BROOCH)
		{
			return false;
		}
		// blacklist check
		if (!item.getItem().isEnchantable())
		{
			return false;
		}
		// only items in inventory and equipped can be enchanted
		if (item.getLocation() != L2ItemInstance.ItemLocation.INVENTORY &&
				item.getLocation() != L2ItemInstance.ItemLocation.PAPERDOLL)
		{
			return false;
		}
		return !item.getName().startsWith("Common");
	}
}
