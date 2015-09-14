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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import l2server.Config;
import l2server.gameserver.model.L2Augmentation;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.stats.funcs.Func;
import l2server.gameserver.stats.funcs.FuncAdd;
import l2server.gameserver.stats.funcs.FuncAddPercent;
import l2server.gameserver.stats.funcs.FuncBaseAdd;
import l2server.gameserver.stats.funcs.LambdaConst;
import l2server.gameserver.templates.item.L2Armor;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.templates.item.L2Weapon;
import l2server.log.Log;
import l2server.util.Rnd;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

/**
 * @author Pere
 */
public class AugmentationData
{
	public static final class Augment
	{
		private final int _id;
		private final int _rarity;
		private final int _slot;
		
		private int _skillId = 0;
		private int _skillLevel = 0;
		private final List<Func> _funcs = new ArrayList<Func>();
		
		public Augment(int id, int rarity, int slot)
		{
			_id = id;
			_rarity = rarity;
			_slot = slot;
		}

		public void setSkill(int skillId, int skillLevel)
		{
			_skillId = skillId;
			_skillLevel = skillLevel;
		}

		public void addFunc(Func func)
		{
			_funcs.add(func);
		}
		
		public int getId()
		{
			return _id;
		}
		
		public int getRarity()
		{
			return _rarity;
		}
		
		public int getSlot()
		{
			return _slot;
		}
		
		public L2Skill getSkill()
		{
			if (_skillId == 0)
				return null;
			
			return SkillTable.getInstance().getInfo(_skillId, _skillLevel);
		}
		
		public void applyBonus(L2PcInstance player)
		{
			for (Func f : _funcs)
				player.addStatFunc(f);
		}
		
		public void removeBonus(L2PcInstance player)
		{
			player.removeStatsOwner(this);
		}
	}
	
	public static final class AugmentSet
	{
		private final List<Augment> _augments;
		private final float _chance;
		
		public AugmentSet(List<Augment> augments, float chance)
		{
			_augments = augments;
			_chance = chance;
		}
		
		public final Augment getRandomAugment()
		{
			return _augments.get(Rnd.get(_augments.size()));
		}
		
		public final float getChance()
		{
			return _chance;
		}
	}
	
	public static final class AugmentGroup
	{
		private final List<AugmentSet> _augments;
		
		public AugmentGroup(List<AugmentSet> augments)
		{
			_augments = augments;
		}
		
		public final Augment getRandomAugment()
		{
			float random = Rnd.get(10000) / 100.0f;
			float current = 0.0f;
			for (AugmentSet set : _augments)
			{
				if (random < current + set.getChance())
					return set.getRandomAugment();
				
				current += set.getChance();
			}
			
			return _augments.get(0).getRandomAugment();
		}
	}
	
	public static final class LifeStone
	{
		// lifestone level to player level table
		private static final int[] LEVELS = {46, 49, 52, 55, 58, 61, 64, 67, 70, 76, 80, 82, 84, 85, 95, 99};
		
		private final int _grade;
		private final int _level;
		private final Map<String, AugmentGroup[]> _augments = new HashMap<String, AugmentGroup[]>();
		
		public LifeStone(int grade, int level)
		{
			_grade = grade;
			_level = level;
		}
		
		public final int getLevel()
		{
			return _level;
		}
		
		public final int getGrade()
		{
			return _grade;
		}
		
		public final int getPlayerLevel()
		{
			return LEVELS[_level];
		}
		
		public final void setAugmentGroup(String type, int order, AugmentGroup group)
		{
			AugmentGroup[] augments = _augments.get(type);
			if (augments == null)
			{
				augments = new AugmentGroup[2];
				_augments.put(type, augments);
			}
			
			augments[order] = group;
		}
		
		public final Augment getRandomAugment(String type, int order)
		{
			AugmentGroup[] augments = _augments.get(type);
			if (augments == null || augments[order] == null)
			{
				Log.warning("Null augment: " + type + ", " + order);
				return null;
			}
			
			return augments[order].getRandomAugment();
		}
	}
	
	public static final AugmentationData getInstance()
	{
		return SingletonHolder._instance;
	}

	public static final int GRADE_NONE = 0;
	public static final int GRADE_MID = 1;
	public static final int GRADE_HIGH = 2;
	public static final int GRADE_TOP = 3;
	public static final int GRADE_LEG = 4;
	public static final int GRADE_ACC = 5; // Accessory LS
	public static final int GRADE_ARIA = 6; // Aria's LS
	
	protected static final int GEMSTONE_D = 2130;
	protected static final int GEMSTONE_C = 2131;
	protected static final int GEMSTONE_B = 2132;
	protected static final int GEMSTONE_A = 2133;
	protected static final int GEMSTONE_R = 19440;

	private final Map<Integer, Augment> _augments = new HashMap<Integer, Augment>();
	private final Map<Integer, LifeStone> _lifeStones = new HashMap<Integer, LifeStone>();
	
	// =========================================================
	// Constructor
	private AugmentationData()
	{
		load();
	}
	
	private final void load()
	{
		// Load the skillmap
		// Note: the skillmap data is only used when generating new augmentations
		// the client expects a different id in order to display the skill in the
		// items description...
		try
		{
			File file = new File(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "augments/augments.xml");
			if (!file.exists())
			{
				if (Config.DEBUG)
					Log.info("The augments file is missing.");
				return;
			}
			
			XmlDocument doc = new XmlDocument(file);
			
			for (XmlNode n : doc.getChildren())
			{
				if (!n.getName().equalsIgnoreCase("list"))
					continue;
				
				for (XmlNode augmentNode : n.getChildren())
				{
					if (!augmentNode.getName().equalsIgnoreCase("augment"))
						continue;
					
					int id = augmentNode.getInt("id");
					int rarity = augmentNode.getInt("rarity");
					int slot = augmentNode.getInt("slot");
					Augment augment = new Augment(id, rarity, slot);
					
					for (XmlNode effectNode : augmentNode.getChildren())
					{
						if (effectNode.getName().equalsIgnoreCase("skill"))
						{
							int skillId = effectNode.getInt("id");
							int skillLevel = effectNode.getInt("level");
							augment.setSkill(skillId, skillLevel);
							continue;
						}
						
						String stat = effectNode.getString("stat", "");
						double val = effectNode.getDouble("val", 0.0);
						Func func = null;
						if (effectNode.getName().equalsIgnoreCase("add"))
							func = new FuncAdd(Stats.fromString(stat), augment, new LambdaConst(val));
						else if (effectNode.getName().equalsIgnoreCase("baseAdd"))
							func = new FuncBaseAdd(Stats.fromString(stat), augment, new LambdaConst(val));
						else if (effectNode.getName().equalsIgnoreCase("addPercent"))
							func = new FuncAddPercent(Stats.fromString(stat), augment, new LambdaConst(val));
						
						if (func != null)
							augment.addFunc(func);
					}
					
					_augments.put(id, augment);
				}
			}

			Log.info("AugmentationData: Loaded " + _augments.size() + " augments.");

			file = new File(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "augments/lifeStones.xml");
			if (!file.exists())
			{
				if (Config.DEBUG)
					Log.info("The life stones file is missing.");
				return;
			}
			
			doc = new XmlDocument(file);
			
			for (XmlNode n : doc.getChildren())
			{
				if (!n.getName().equalsIgnoreCase("list"))
					continue;
				
				for (XmlNode stoneNode : n.getChildren())
				{
					if (!stoneNode.getName().equalsIgnoreCase("lifeStone"))
						continue;
					
					int id = stoneNode.getInt("id");
					int grade = stoneNode.getInt("grade");
					int level = stoneNode.getInt("level");
					LifeStone lifeStone = new LifeStone(grade, level);
					
					for (XmlNode groupNode : stoneNode.getChildren())
					{
						if (!groupNode.getName().equalsIgnoreCase("augmentGroup"))
							continue;
						
						String[] weaponTypes = groupNode.getString("weaponType").split(",");
						int order = groupNode.getInt("order");
						
						List<AugmentSet> sets = new ArrayList<AugmentSet>();
						for (XmlNode setNode : groupNode.getChildren())
						{
							if (!setNode.getName().equalsIgnoreCase("augments"))
								continue;
							
							String[] ids = setNode.getString("ids").split(",");
							float chance = setNode.getFloat("chance");
							List<Augment> augments = new ArrayList<Augment>();
							for (String idRange : ids)
							{
								if (idRange.contains("-"))
								{
									int start = Integer.parseInt(idRange.substring(0, idRange.indexOf("-")));
									int end = Integer.parseInt(idRange.substring(idRange.indexOf("-") + 1));
									for (int augmentId = start; augmentId <= end; augmentId++)
										augments.add(_augments.get(augmentId));
								}
								else
									augments.add(_augments.get(Integer.parseInt(idRange)));
							}
							
							sets.add(new AugmentSet(augments, chance));
						}
						
						for (String weaponType : weaponTypes)
							lifeStone.setAugmentGroup(weaponType, order, new AugmentGroup(sets));
					}
					
					_lifeStones.put(id, lifeStone);
				}
			}

			Log.info("AugmentationData: Loaded " + _lifeStones.size() + " life stones.");
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error loading augmentation data", e);
			return;
		}
	}
	
	/**
	 * Generate a new random augmentation
	 * @param item
	 * @param lifeStoneLevel
	 * @param lifeSoneGrade
	 * @param bodyPart
	 * @return L2Augmentation
	 */
	public L2Augmentation generateRandomAugmentation(LifeStone lifeStone, L2ItemInstance targetItem)
	{
		switch (targetItem.getItem().getBodyPart())
		{
			case L2Item.SLOT_LR_FINGER:
				return generateRandomAugmentation(lifeStone, "ring");
			case L2Item.SLOT_LR_EAR:
				return generateRandomAugmentation(lifeStone, "earring");
			case L2Item.SLOT_NECK:
				return generateRandomAugmentation(lifeStone, "necklace");
			default:
				return generateRandomAugmentation(lifeStone, targetItem.getWeaponItem().isMagicWeapon() ? "mage" : "warrior");
		}
	}
	
	private L2Augmentation generateRandomAugmentation(LifeStone lifeStone, String weaponType)
	{
		Augment augment1 = lifeStone.getRandomAugment(weaponType, 0);
		Augment augment2 = lifeStone.getRandomAugment(weaponType, 1);
		if (augment1 == null)
			return null;
		
		return new L2Augmentation(augment1, augment2);
	}

	public final Augment getAugment(int id)
	{
		return _augments.get(id);
	}

	public final LifeStone getLifeStone(int itemId)
	{
		return _lifeStones.get(itemId);
	}
	
	/*
	 * Checks player, source item, lifestone and gemstone validity for augmentation process
	 */
	public final boolean isValid(L2PcInstance player, L2ItemInstance item, L2ItemInstance refinerItem, L2ItemInstance gemStones)
	{
		if (!isValid(player, item, refinerItem))
			return false;
		
		// GemStones must belong to owner
		if (gemStones.getOwnerId() != player.getObjectId())
			return false;
		// .. and located in inventory
		if (gemStones.getLocation() != L2ItemInstance.ItemLocation.INVENTORY)
			return false;
		
		final int grade = item.getItem().getItemGrade();
		final LifeStone ls = getLifeStone(refinerItem.getItemId());
		
		// Check for item id
		if (getGemStoneId(grade, ls.getGrade()) != gemStones.getItemId())
			return false;
		// Count must be greater or equal of required number
		if (getGemStoneCount(grade, ls.getGrade()) > gemStones.getCount())
			return false;
		
		return true;
	}
	
	/*
	 * Checks player, source item and lifestone validity for augmentation process
	 */
	public final boolean isValid(L2PcInstance player, L2ItemInstance item, L2ItemInstance refinerItem)
	{
		if (!isValid(player, item))
			return false;
		
		// Item must belong to owner
		if (refinerItem.getOwnerId() != player.getObjectId())
			return false;
		// Lifestone must be located in inventory
		if (refinerItem.getLocation() != L2ItemInstance.ItemLocation.INVENTORY)
			return false;
		
		final LifeStone ls = getLifeStone(refinerItem.getItemId());
		if (ls == null)
			return false;
		
		if (item.getItem().isEpic() && ls.getGrade() != GRADE_ARIA)
			return false;
		
		// weapons can't be augmented with accessory ls
		if (item.getItem() instanceof L2Weapon && (ls.getGrade() == GRADE_ACC || ls.getGrade() == GRADE_ARIA))
			return false;
		// and accessory can't be augmented with weapon ls
		if (item.getItem() instanceof L2Armor && ls.getGrade() < GRADE_ACC)
			return false;
		// check for level of the lifestone
		if (player.getLevel() < ls.getPlayerLevel())
			return false;
		
		return true;
	}
	
	/*
	 * Check both player and source item conditions for augmentation process
	 */
	public static final boolean isValid(L2PcInstance player, L2ItemInstance item)
	{
		if (!isValid(player))
			return false;
		
		// Item must belong to owner
		if (item.getOwnerId() != player.getObjectId())
			return false;
		if (item.isAugmented())
			return false;
		if (item.isHeroItem() && !item.getItem().isAugmentable())
			return false;
		if (item.isShadowItem())
			return false;
		if (item.isCommonItem())
			return false;
		if (item.isEtcItem())
			return false;
		if (item.isTimeLimitedItem())
			return false;
		//if (item.isPvp())
		//	return false;
		if (item.getItem().getCrystalType() < L2Item.CRYSTAL_C)
			return false;
		
		// Source item can be equipped or in inventory
		switch (item.getLocation())
		{
			case INVENTORY:
			case PAPERDOLL:
				break;
			default:
				return false;
		}
		
		if (item.getItem() instanceof L2Weapon)
		{
			switch (((L2Weapon)item.getItem()).getItemType())
			{
				case NONE:
				case FISHINGROD:
					return false;
				default:
					break;
			}
		}
		else if (item.getItem() instanceof L2Armor)
		{
			// only accessories can be augmented
			switch (item.getItem().getBodyPart())
			{
				case L2Item.SLOT_LR_FINGER:
				case L2Item.SLOT_LR_EAR:
				case L2Item.SLOT_NECK:
					break;
				default:
					return false;
			}
		}
		else
			return false; // neither weapon nor armor ?
		
		// blacklist check
		if (!item.getItem().isAugmentable())
			return false;
		
		return true;
	}
	
	/*
	 * Check if player's conditions valid for augmentation process
	 */
	public static final boolean isValid(L2PcInstance player)
	{
		if (player.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION));
			return false;
		}
		if (player.getActiveTradeList() != null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_TRADING));
			return false;
		}
		if (player.isDead())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD));
			return false;
		}
		if (player.isParalyzed())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED));
			return false;
		}
		if (player.isFishing())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING));
			return false;
		}
		if (player.isSitting())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN));
			return false;
		}
		if (player.isCursedWeaponEquipped())
			return false;
		if (player.isEnchanting() || player.isProcessingTransaction())
			return false;
		
		return true;
	}
	
	/*
	 * Returns GemStone itemId based on item grade
	 */
	public static final int getGemStoneId(int itemGrade, int lifeStoneGrade)
	{
		if (lifeStoneGrade == GRADE_ARIA)
			return GEMSTONE_R;
		
		switch (itemGrade)
		{
			case L2Item.CRYSTAL_C:
			case L2Item.CRYSTAL_B:
				return GEMSTONE_D;
			case L2Item.CRYSTAL_A:
			case L2Item.CRYSTAL_S:
				return GEMSTONE_C;
			case L2Item.CRYSTAL_S80:
			case L2Item.CRYSTAL_S84:
				return GEMSTONE_B;
			case L2Item.CRYSTAL_R:
			case L2Item.CRYSTAL_R95:
			case L2Item.CRYSTAL_R99:
				return GEMSTONE_A;
			default:
				return 0;
		}
	}
	
	/*
	 * Returns GemStone count based on item grade and lifestone grade
	 * (different for weapon and accessory augmentation)
	 */
	public static final int getGemStoneCount(int itemGrade, int lifeStoneGrade)
	{
		switch (lifeStoneGrade)
		{
			case GRADE_ARIA:
				return 100;
			case GRADE_ACC:
				switch (itemGrade)
				{
					case L2Item.CRYSTAL_C:
						return 200;
					case L2Item.CRYSTAL_B:
						return 300;
					case L2Item.CRYSTAL_A:
						return 200;
					case L2Item.CRYSTAL_S:
						return 250;
					case L2Item.CRYSTAL_S80:
						return 360;
					case L2Item.CRYSTAL_S84:
						return 480;
					case L2Item.CRYSTAL_R:
						return 26;
					case L2Item.CRYSTAL_R95:
						return 90;
					case L2Item.CRYSTAL_R99:
						return 236;
					default:
						return 0;
				}
			default:
				switch (itemGrade)
				{
					case L2Item.CRYSTAL_C:
						return 20;
					case L2Item.CRYSTAL_B:
						return 30;
					case L2Item.CRYSTAL_A:
						return 20;
					case L2Item.CRYSTAL_S:
						return 25;
					case L2Item.CRYSTAL_S80:
						return 36;
					case L2Item.CRYSTAL_S84:
						return 48;
					case L2Item.CRYSTAL_R:
						return 13;
					case L2Item.CRYSTAL_R95:
						return 45;
					case L2Item.CRYSTAL_R99:
						return 118;
					default:
						return 0;
				}
		}
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final AugmentationData _instance = new AugmentationData();
	}
}
