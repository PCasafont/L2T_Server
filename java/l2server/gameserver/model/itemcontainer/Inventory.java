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

package l2server.gameserver.model.itemcontainer;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.datatables.ArmorSetsTable;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.*;
import l2server.gameserver.model.L2ItemInstance.ItemLocation;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.SkillCoolTime;
import l2server.gameserver.stats.SkillHolder;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.templates.item.*;
import l2server.log.Log;
import l2server.util.StringUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * This class manages inventory
 *
 * @version $Revision: 1.13.2.9.2.12 $ $Date: 2005/03/29 23:15:15 $
 *          rewritten 23.2.2006 by Advi
 */
public abstract class Inventory extends ItemContainer
{
	//
	public interface PaperdollListener
	{
		void notifyEquiped(int slot, L2ItemInstance inst, Inventory inventory);

		void notifyUnequiped(int slot, L2ItemInstance inst, Inventory inventory);
	}

	public static final int PAPERDOLL_UNDER = 0;
	public static final int PAPERDOLL_HEAD = 1;
	public static final int PAPERDOLL_HAIR = 2;
	public static final int PAPERDOLL_HAIR2 = 3;
	public static final int PAPERDOLL_NECK = 4;
	public static final int PAPERDOLL_RHAND = 5;
	public static final int PAPERDOLL_CHEST = 6;
	public static final int PAPERDOLL_LHAND = 7;
	public static final int PAPERDOLL_REAR = 8;
	public static final int PAPERDOLL_LEAR = 9;
	public static final int PAPERDOLL_GLOVES = 10;
	public static final int PAPERDOLL_LEGS = 11;
	public static final int PAPERDOLL_FEET = 12;
	public static final int PAPERDOLL_RFINGER = 13;
	public static final int PAPERDOLL_LFINGER = 14;
	public static final int PAPERDOLL_LBRACELET = 15;
	public static final int PAPERDOLL_RBRACELET = 16;
	public static final int PAPERDOLL_DECO1 = 17;
	public static final int PAPERDOLL_DECO2 = 18;
	public static final int PAPERDOLL_DECO3 = 19;
	public static final int PAPERDOLL_DECO4 = 20;
	public static final int PAPERDOLL_DECO5 = 21;
	public static final int PAPERDOLL_DECO6 = 22;
	public static final int PAPERDOLL_CLOAK = 23;
	public static final int PAPERDOLL_BELT = 24;
	public static final int PAPERDOLL_BROOCH = 25;
	public static final int PAPERDOLL_JEWELRY1 = 26;
	public static final int PAPERDOLL_JEWELRY2 = 27;
	public static final int PAPERDOLL_JEWELRY3 = 28;
	public static final int PAPERDOLL_JEWELRY4 = 29;
	public static final int PAPERDOLL_JEWELRY5 = 30;
	public static final int PAPERDOLL_JEWELRY6 = 31;
	public static final int PAPERDOLL_TOTALSLOTS = 32;

	//Speed percentage mods
	public static final double MAX_ARMOR_WEIGHT = 12000;

	private final L2ItemInstance[] _paperdoll;
	private final List<PaperdollListener> _paperdollListeners;

	// protected to be accessed from child classes only
	protected int _totalWeight;

	// used to quickly check for using of items of special type
	private int _wearedMask;

	// Recorder of alterations in inventory
	private static final class ChangeRecorder implements PaperdollListener
	{
		private final Inventory _inventory;
		private final List<L2ItemInstance> _changed;

		/**
		 * Constructor of the ChangeRecorder
		 *
		 * @param inventory
		 */
		ChangeRecorder(Inventory inventory)
		{
			_inventory = inventory;
			_changed = new ArrayList<>();
			_inventory.addPaperdollListener(this);
		}

		/**
		 * Add alteration in inventory when item equiped
		 */
		@Override
		public void notifyEquiped(int slot, L2ItemInstance item, Inventory inventory)
		{
			if (!_changed.contains(item))
			{
				_changed.add(item);
			}
		}

		/**
		 * Add alteration in inventory when item unequiped
		 */
		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item, Inventory inventory)
		{
			if (!_changed.contains(item))
			{
				_changed.add(item);
			}
		}

		/**
		 * Returns alterations in inventory
		 *
		 * @return L2ItemInstance[] : array of alterated items
		 */
		public L2ItemInstance[] getChangedItems()
		{
			return _changed.toArray(new L2ItemInstance[_changed.size()]);
		}
	}

	private static final class WeaponListener implements PaperdollListener
	{
		private static WeaponListener instance = new WeaponListener();

		public static WeaponListener getInstance()
		{
			return instance;
		}

		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item, Inventory inventory)
		{
			if (slot != PAPERDOLL_RHAND)
			{
				return;
			}

			if (inventory instanceof PcInventory)
			{
				((PcInventory) inventory).getOwner().checkAutoShots();
				if (Config.isServer(Config.TENKAI) &&
						(item.getName().contains("Antharas") || item.getName().contains("Valakas") ||
								item.getName().contains("Lindvior")))
				{
					((PcInventory) inventory).getOwner().onDWUnequip();
				}
			}

			if (item.getItemType() == L2WeaponType.BOW)
			{
				L2ItemInstance arrow = inventory.getPaperdollItem(PAPERDOLL_LHAND);

				if (arrow != null)
				{
					inventory.setPaperdollItem(PAPERDOLL_LHAND, null);
				}
			}
			else if (item.getItemType() == L2WeaponType.CROSSBOW || item.getItemType() == L2WeaponType.CROSSBOWK)
			{
				L2ItemInstance bolts = inventory.getPaperdollItem(PAPERDOLL_LHAND);

				if (bolts != null)
				{
					inventory.setPaperdollItem(PAPERDOLL_LHAND, null);
				}
			}
			else if (item.getItemType() == L2WeaponType.FISHINGROD)
			{
				L2ItemInstance lure = inventory.getPaperdollItem(PAPERDOLL_LHAND);

				if (lure != null)
				{
					inventory.setPaperdollItem(PAPERDOLL_LHAND, null);
				}
			}
		}

		@Override
		public void notifyEquiped(int slot, L2ItemInstance item, Inventory inventory)
		{
			if (slot != PAPERDOLL_RHAND)
			{
				return;
			}

			if (inventory instanceof PcInventory)
			{
				((PcInventory) inventory).getOwner().checkAutoShots();
				if (Config.isServer(Config.TENKAI) &&
						(item.getName().contains("Antharas") || item.getName().contains("Valakas") ||
								item.getName().contains("Lindvior")))
				{
					((PcInventory) inventory).getOwner().onDWEquip();
				}
			}

			if (item.getItemType() == L2WeaponType.BOW)
			{
				L2ItemInstance arrow = inventory.findArrowForBow(item.getItem());

				if (arrow != null)
				{
					inventory.setPaperdollItem(PAPERDOLL_LHAND, arrow);
				}
			}
			else if (item.getItemType() == L2WeaponType.CROSSBOW || item.getItemType() == L2WeaponType.CROSSBOWK)
			{
				L2ItemInstance bolts = inventory.findBoltForCrossBow(item.getItem());

				if (bolts != null)
				{
					inventory.setPaperdollItem(PAPERDOLL_LHAND, bolts);
				}
			}
		}
	}

	private static final class StatsListener implements PaperdollListener
	{
		private static StatsListener instance = new StatsListener();

		public static StatsListener getInstance()
		{
			return instance;
		}

		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item, Inventory inventory)
		{
			/*if (slot == PAPERDOLL_RHAND)
                return;*/
			inventory.getOwner().removeStatsOwner(item);
		}

		@Override
		public void notifyEquiped(int slot, L2ItemInstance item, Inventory inventory)
		{
            /*if (slot == PAPERDOLL_RHAND)
				return;*/
			inventory.getOwner().addStatFuncs(item.getStatFuncs());
		}
	}

	private static final class ItemSkillsListener implements PaperdollListener
	{
		private static ItemSkillsListener instance = new ItemSkillsListener();

		public static ItemSkillsListener getInstance()
		{
			return instance;
		}

		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item, Inventory inventory)
		{
			L2PcInstance player;

			if (inventory.getOwner() instanceof L2PcInstance)
			{
				player = (L2PcInstance) inventory.getOwner();
			}
			else
			{
				return;
			}

			L2Skill enchantSkill, itemSkill;
			L2Item it = item.getItem();
			boolean update = false;

			if (it instanceof L2Weapon)
			{
				// Remove ensoul effects on unequip
				for (EnsoulEffect e : item.getEnsoulEffects())
				{
					if (e != null)
					{
						e.removeBonus(player);
					}
				}

				// Remove augmentation bonuses on unequip
				if (item.isAugmented())
				{
					item.getAugmentation().removeBonus(player);
				}

				item.removeElementAttrBonus(player);
				// Remove skills bestowed from +4 Rapiers/Duals
				if (item.getEnchantLevel() >= 4)
				{
					enchantSkill = ((L2Weapon) it).getEnchant4Skill();

					if (enchantSkill != null)
					{
						player.removeSkill(enchantSkill, false, enchantSkill.isPassive());
						update = true;
					}
				}
			}
			else if (it instanceof L2Armor)
			{
				// Remove augmentation bonuses on unequip
				if (item.isAugmented())
				{
					item.getAugmentation().removeBonus(player);
				}

				item.removeElementAttrBonus(player);

				// Remove skills bestowed from +X armor
				for (int enchant = 1; enchant <= L2Armor.MAX_ENCHANT_SKILL; enchant++)
				{
					if (item.getEnchantLevel() >= enchant)
					{
						enchantSkill = ((L2Armor) it).getEnchantSkill(enchant);

						if (enchantSkill != null)
						{
							player.removeSkill(enchantSkill, false, enchantSkill.isPassive());
							update = true;
						}
					}
				}
			}

			final SkillHolder[] skills = it.getSkills();

			if (skills != null)
			{
				for (SkillHolder skillInfo : skills)
				{
					if (skillInfo == null)
					{
						continue;
					}

					itemSkill = skillInfo.getSkill();

					if (itemSkill != null)
					{
						player.removeSkill(itemSkill, false, itemSkill.isPassive());
						update = true;
					}
					else
					{
						Log.warning("Inventory.ItemSkillsListener.Weapon: Incorrect skill: " + skillInfo + ".");
					}
				}
			}

			if (update)
			{
				player.sendSkillList();
			}
		}

		@Override
		public void notifyEquiped(int slot, L2ItemInstance item, Inventory inventory)
		{
			L2PcInstance player;

			if (inventory.getOwner() instanceof L2PcInstance)
			{
				player = (L2PcInstance) inventory.getOwner();
			}
			else
			{
				return;
			}

			L2Skill enchantSkill, itemSkill;
			L2Item it = item.getItem();
			boolean update = false;
			boolean updateTimeStamp = false;

			if (it instanceof L2Weapon)
			{
				for (EnsoulEffect e : item.getEnsoulEffects())
				{
					if (e != null)
					{
						e.applyBonus(player);
					}
				}

				// Apply augmentation bonuses on equip
				if (item.isAugmented())
				{
					item.getAugmentation().applyBonus(player);
				}

				item.updateElementAttrBonus(player);

				// Add skills bestowed from +4 Rapiers/Duals
				if (item.getEnchantLevel() >= 4)
				{
					enchantSkill = ((L2Weapon) it).getEnchant4Skill();

					if (enchantSkill != null)
					{
						player.addSkill(enchantSkill, false);
						update = true;
					}
				}
			}
			else if (it instanceof L2Armor)
			{
				// Apply augmentation bonuses on equip
				if (item.isAugmented())
				{
					item.getAugmentation().applyBonus(player);
				}

				item.updateElementAttrBonus(player);

				// Add skills bestowed from +X armor
				for (int enchant = 1; enchant <= L2Armor.MAX_ENCHANT_SKILL; enchant++)
				{
					if (item.getEnchantLevel() >= enchant)
					{
						enchantSkill = ((L2Armor) it).getEnchantSkill(enchant);

						if (enchantSkill != null)
						{
							player.addSkill(enchantSkill, false);
							update = true;
						}
					}
				}
			}

			final SkillHolder[] skills = it.getSkills();

			if (skills != null)
			{
				for (SkillHolder skillInfo : skills)
				{
					if (skillInfo == null)
					{
						continue;
					}

					itemSkill = skillInfo.getSkill();

					if (itemSkill != null)
					{
						player.addSkill(itemSkill, false);

						if (itemSkill.isActive())
						{
							if (player.getReuseTimeStamp().isEmpty() ||
									!player.getReuseTimeStamp().containsKey(itemSkill.getReuseHashCode()))
							{
								int equipDelay = itemSkill.getEquipDelay();

								if (equipDelay > 0)
								{
									player.addTimeStamp(itemSkill, equipDelay);
									player.disableSkill(itemSkill, equipDelay);
								}
							}
							updateTimeStamp = true;
						}
						update = true;
					}
					else
					{
						Log.warning("Inventory.ItemSkillsListener.Weapon: Incorrect skill: " + skillInfo + ".");
					}
				}
			}

			if (update && !player.isUpdateLocked())
			{
				player.sendSkillList();

				if (updateTimeStamp)
				{
					player.sendPacket(new SkillCoolTime(player));
				}
			}
		}
	}

	private static final class ArmorSetListener implements PaperdollListener
	{
		private static ArmorSetListener instance = new ArmorSetListener();

		public static ArmorSetListener getInstance()
		{
			return instance;
		}

		@Override
		public void notifyEquiped(int slot, L2ItemInstance item, Inventory inventory)
		{
			if (!(inventory.getOwner() instanceof L2PcInstance))
			{
				return;
			}

			L2PcInstance player = (L2PcInstance) inventory.getOwner();

			if (item == null || item.getArmorItem() == null || item.getArmorItem().getArmorSet() == null)
			{
				return;
			}

			for (int setId : item.getArmorItem().getArmorSet())
			{
				// Checks for armorset for the equiped chest
				L2ArmorSet armorSet = ArmorSetsTable.getInstance().getSet(setId);

				if (armorSet == null)
				{
					continue;
				}

				boolean update = false;
				boolean updateTimeStamp = false;
				int missingParts = armorSet.countMissingParts(player);

				for (int skillId : armorSet.getSkills().keys())
				{
					L2Skill itemSkill;
					int skillLvl = armorSet.getSkills().get(skillId) - missingParts;
					if (skillId > 0 && skillLvl > 0)
					{
						itemSkill = SkillTable.getInstance().getInfo(skillId, skillLvl);
						if (itemSkill != null)
						{
							player.addSkill(itemSkill, false);

							if (itemSkill.isActive())
							{
								if (player.getReuseTimeStamp().isEmpty() ||
										!player.getReuseTimeStamp().containsKey(itemSkill.getReuseHashCode()))
								{
									int equipDelay = itemSkill.getEquipDelay();

									if (equipDelay > 0)
									{
										player.addTimeStamp(itemSkill, itemSkill.getEquipDelay());
										player.disableSkill(itemSkill, itemSkill.getEquipDelay());
									}
								}
								updateTimeStamp = true;
							}
							update = true;
						}
						else
						{
							Log.warning("Inventory.ArmorSetListener: Incorrect skill: " + skillId + ".");
						}
					}
				}

				// Checks if equiped item is part of set
				if (missingParts == 0)
				{
					if (armorSet.containsShield(player)) // has shield from set
					{
						final L2Skill shieldSkill = SkillTable.getInstance().getInfo(armorSet.getShieldSkillId(), 1);

						if (shieldSkill != null)
						{
							player.addSkill(shieldSkill, false);
							update = true;
						}
						else
						{
							Log.warning("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getShieldSkillId() +
									".");
						}
					}

					int enchant = armorSet.getEnchantLevel(player);
					if (enchant >= 6) // has all parts of set enchanted to 6 or more
					{
						final int skillId6 = armorSet.getEnchant6skillId();

						if (skillId6 > 0)
						{
							int maxLevel = SkillTable.getInstance().getMaxLevel(skillId6);
							int level = Math.min(maxLevel, enchant - 5);
							L2Skill skille = SkillTable.getInstance().getInfo(skillId6, level);

							if (skille != null)
							{
								player.addSkill(skille, false);
								update = true;
							}
							else
							{
								Log.warning("Inventory.ArmorSetListener: Incorrect skill: " +
										armorSet.getEnchant6skillId() + ".");
							}
						}
					}
					if (item.getItemType() == L2ArmorType.SHIELD)
					{
						final L2Skill shieldSkill = SkillTable.getInstance().getInfo(armorSet.getShieldSkillId(), 1);

						if (shieldSkill != null)
						{
							player.addSkill(shieldSkill, false);
							update = true;
						}
						else
						{
							Log.warning("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getShieldSkillId() +
									".");
						}
					}
				}

				if (update && !player.isUpdateLocked())
				{
					player.sendSkillList();

					if (updateTimeStamp)
					{
						player.sendPacket(new SkillCoolTime(player));
					}
				}
			}
		}

		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item, Inventory inventory)
		{
			if (!(inventory.getOwner() instanceof L2PcInstance))
			{
				return;
			}

			L2PcInstance player = (L2PcInstance) inventory.getOwner();

			if (item == null || item.getArmorItem() == null || item.getArmorItem().getArmorSet() == null)
			{
				return;
			}

			for (int setId : item.getArmorItem().getArmorSet())
			{
				// Checks for armorset for the equiped chest
				L2ArmorSet armorSet = ArmorSetsTable.getInstance().getSet(setId);

				if (armorSet == null)
				{
					continue;
				}

				int shieldSkill = armorSet.getShieldSkillId();
				int skillId6 = armorSet.getEnchant6skillId();
				int missingParts = armorSet.countMissingParts(player);

				for (int skillId : armorSet.getSkills().keys())
				{
					L2Skill itemSkill;
					int skillLvl = armorSet.getSkills().get(skillId) - missingParts;
					if (skillId > 0 && skillLvl > 0)
					{
						itemSkill = SkillTable.getInstance().getInfo(skillId, skillLvl);
						if (itemSkill != null)
						{
							player.addSkill(itemSkill, false);

							if (itemSkill.isActive())
							{
								if (player.getReuseTimeStamp().isEmpty() ||
										!player.getReuseTimeStamp().containsKey(itemSkill.getReuseHashCode()))
								{
									int equipDelay = itemSkill.getEquipDelay();

									if (equipDelay > 0)
									{
										player.addTimeStamp(itemSkill, itemSkill.getEquipDelay());
										player.disableSkill(itemSkill, itemSkill.getEquipDelay());
									}
								}
							}
						}
						else
						{
							Log.warning("Inventory.ArmorSetListener: Incorrect skill: " + skillId + ".");
						}
					}
					else
					{
						itemSkill = SkillTable.getInstance().getInfo(skillId, 1);
						if (itemSkill != null)
						{
							player.removeSkill(itemSkill, false, itemSkill.isPassive());
						}
						else
						{
							Log.warning("Inventory.ArmorSetListener: Incorrect skill: " + skillId + ".");
						}
					}
				}

				if (item.getItemType() == L2ArmorType.SHIELD || missingParts > 0 && shieldSkill > 0)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(shieldSkill, 1);
					if (skill != null)
					{
						player.removeSkill(skill, false, skill.isPassive());
					}
				}

				if (missingParts > 0 && skillId6 > 0)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(skillId6, 1);
					if (skill != null)
					{
						player.removeSkill(skill, false, skill.isPassive());
					}
				}

				player.checkItemRestriction();

				if (!player.isUpdateLocked())
				{
					player.sendSkillList();
				}
			}
		}
	}

	private static final class BraceletListener implements PaperdollListener
	{
		private static BraceletListener instance = new BraceletListener();

		public static BraceletListener getInstance()
		{
			return instance;
		}

		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item, Inventory inventory)
		{
			if (item.getItem().getBodyPart() == L2Item.SLOT_R_BRACELET)
			{
				inventory.unEquipItemInSlot(PAPERDOLL_DECO1);
				inventory.unEquipItemInSlot(PAPERDOLL_DECO2);
				inventory.unEquipItemInSlot(PAPERDOLL_DECO3);
				inventory.unEquipItemInSlot(PAPERDOLL_DECO4);
				inventory.unEquipItemInSlot(PAPERDOLL_DECO5);
				inventory.unEquipItemInSlot(PAPERDOLL_DECO6);
			}
		}

		@Override
		public void notifyEquiped(int slot, L2ItemInstance item, Inventory inventory)
		{
		}
	}

	private static final class BroochListener implements PaperdollListener
	{
		private static BroochListener instance = new BroochListener();

		public static BroochListener getInstance()
		{
			return instance;
		}

		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item, Inventory inventory)
		{
			if (item.getItem().getBodyPart() == L2Item.SLOT_BROOCH)
			{
				inventory.unEquipItemInSlot(PAPERDOLL_JEWELRY1);
				inventory.unEquipItemInSlot(PAPERDOLL_JEWELRY2);
				inventory.unEquipItemInSlot(PAPERDOLL_JEWELRY3);
				inventory.unEquipItemInSlot(PAPERDOLL_JEWELRY4);
				inventory.unEquipItemInSlot(PAPERDOLL_JEWELRY5);
				inventory.unEquipItemInSlot(PAPERDOLL_JEWELRY6);
			}
		}

		@Override
		public void notifyEquiped(int slot, L2ItemInstance item, Inventory inventory)
		{
		}
	}

	/**
	 * Constructor of the inventory
	 */
	protected Inventory()
	{
		_paperdoll = new L2ItemInstance[PAPERDOLL_TOTALSLOTS];
		_paperdollListeners = new ArrayList<>();

		if (this instanceof PcInventory)
		{
			addPaperdollListener(ArmorSetListener.getInstance());
			addPaperdollListener(WeaponListener.getInstance());
			addPaperdollListener(ItemSkillsListener.getInstance());
			addPaperdollListener(BraceletListener.getInstance());
			addPaperdollListener(BroochListener.getInstance());
		}

		//common
		addPaperdollListener(StatsListener.getInstance());
	}

	protected abstract ItemLocation getEquipLocation();

	/**
	 * Returns the instance of new ChangeRecorder
	 *
	 * @return ChangeRecorder
	 */
	public ChangeRecorder newRecorder()
	{
		return new ChangeRecorder(this);
	}

	/**
	 * Drop item from inventory and updates database
	 *
	 * @param process   : String Identifier of process triggering this action
	 * @param item      : L2ItemInstance to be dropped
	 * @param actor     : L2PcInstance Player requesting the item drop
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the destroyed item or the updated item in inventory
	 */
	public L2ItemInstance dropItem(String process, L2ItemInstance item, L2PcInstance actor, Object reference)
	{
		if (item == null)
		{
			return null;
		}

		synchronized (item)
		{
			if (!_items.containsKey(item.getObjectId()))
			{
				return null;
			}

			removeItem(item);
			item.setOwnerId(process, 0, actor, reference);
			item.setLocation(ItemLocation.VOID);
			item.setLastChange(L2ItemInstance.REMOVED);

			item.updateDatabase();
			refreshWeight();
		}
		return item;
	}

	/**
	 * Drop item from inventory by using its <B>objectID</B> and updates database
	 *
	 * @param process   : String Identifier of process triggering this action
	 * @param objectId  : int Item Instance identifier of the item to be dropped
	 * @param count     : int Quantity of items to be dropped
	 * @param actor     : L2PcInstance Player requesting the item drop
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the destroyed item or the updated item in inventory
	 */
	public L2ItemInstance dropItem(String process, int objectId, long count, L2PcInstance actor, Object reference)
	{
		L2ItemInstance item = getItemByObjectId(objectId);
		if (item == null)
		{
			return null;
		}

		synchronized (item)
		{
			if (!_items.containsKey(item.getObjectId()))
			{
				return null;
			}

			// Adjust item quantity and create new instance to drop
			// Directly drop entire item
			if (item.getCount() > count)
			{
				item.changeCount(process, -count, actor, reference);
				item.setLastChange(L2ItemInstance.MODIFIED);
				item.updateDatabase();

				item = ItemTable.getInstance().createItem(process, item.getItemId(), count, actor, reference);
				item.updateDatabase();
				refreshWeight();
				return item;
			}
		}
		return dropItem(process, item, actor, reference);
	}

	/**
	 * Adds item to inventory for further adjustments and Equip it if necessary (itemlocation defined)<BR><BR>
	 *
	 * @param item : L2ItemInstance to be added from inventory
	 */
	@Override
	protected void addItem(L2ItemInstance item)
	{
		super.addItem(item);
		if (item.isEquipped())
		{
			equipItem(item, false);
		}
	}

	/**
	 * Removes item from inventory for further adjustments.
	 *
	 * @param item : L2ItemInstance to be removed from inventory
	 */
	@Override
	protected boolean removeItem(L2ItemInstance item)
	{
		// Unequip item if equiped
		for (int i = 0; i < _paperdoll.length; i++)
		{
			if (_paperdoll[i] == item)
			{
				unEquipItemInSlot(i);
			}
		}
		return super.removeItem(item);
	}

	/**
	 * Returns the item in the paperdoll slot
	 *
	 * @return L2ItemInstance
	 */
	public L2ItemInstance getPaperdollItem(int slot)
	{
		return _paperdoll[slot];
	}

	public static int getPaperdollIndex(int slot)
	{
		switch (slot)
		{
			case L2Item.SLOT_UNDERWEAR:
				return PAPERDOLL_UNDER;
			case L2Item.SLOT_R_EAR:
				return PAPERDOLL_REAR;
			case L2Item.SLOT_LR_EAR:
			case L2Item.SLOT_L_EAR:
				return PAPERDOLL_LEAR;
			case L2Item.SLOT_NECK:
				return PAPERDOLL_NECK;
			case L2Item.SLOT_R_FINGER:
			case L2Item.SLOT_LR_FINGER:
				return PAPERDOLL_RFINGER;
			case L2Item.SLOT_L_FINGER:
				return PAPERDOLL_LFINGER;
			case L2Item.SLOT_HEAD:
				return PAPERDOLL_HEAD;
			case L2Item.SLOT_R_HAND:
			case L2Item.SLOT_LR_HAND:
				return PAPERDOLL_RHAND;
			case L2Item.SLOT_L_HAND:
				return PAPERDOLL_LHAND;
			case L2Item.SLOT_GLOVES:
				return PAPERDOLL_GLOVES;
			case L2Item.SLOT_CHEST:
			case L2Item.SLOT_FULL_ARMOR:
			case L2Item.SLOT_ALLDRESS:
				return PAPERDOLL_CHEST;
			case L2Item.SLOT_LEGS:
				return PAPERDOLL_LEGS;
			case L2Item.SLOT_FEET:
				return PAPERDOLL_FEET;
			case L2Item.SLOT_BACK:
				return PAPERDOLL_CLOAK;
			case L2Item.SLOT_HAIR:
			case L2Item.SLOT_HAIRALL:
				return PAPERDOLL_HAIR;
			case L2Item.SLOT_HAIR2:
				return PAPERDOLL_HAIR2;
			case L2Item.SLOT_R_BRACELET:
				return PAPERDOLL_RBRACELET;
			case L2Item.SLOT_L_BRACELET:
				return PAPERDOLL_LBRACELET;
			case L2Item.SLOT_DECO:
				return PAPERDOLL_DECO1; //return first we deal with it later
			case L2Item.SLOT_BELT:
				return PAPERDOLL_BELT;
			case L2Item.SLOT_BROOCH:
				return PAPERDOLL_BROOCH;
			case L2Item.SLOT_JEWELRY:
				return PAPERDOLL_JEWELRY1;
		}
		return -1;
	}

	/**
	 * Returns all items worn by a character
	 *
	 * @return L2ItemInstance[]
	 */
	public L2ItemInstance[] getPaperdollItems()
	{
		List<L2ItemInstance> list = new ArrayList<>();
		for (L2ItemInstance element : _paperdoll)
		{
			if (element != null)
			{
				list.add(element);
			}
		}
		return list.toArray(new L2ItemInstance[list.size()]);
	}

	/**
	 * Returns the item in the paperdoll L2Item slot
	 *
	 * @return L2ItemInstance
	 */
	public L2ItemInstance getPaperdollItemByL2ItemId(int slot)
	{
		int index = getPaperdollIndex(slot);
		if (index == -1)
		{
			return null;
		}
		return _paperdoll[index];
	}

	/**
	 * Returns the ID of the item in the paperdol slot
	 *
	 * @param slot : int designating the slot
	 * @return int designating the ID of the item
	 */
	public int getPaperdollItemId(int slot)
	{
		// Check for chest parts with full body appearance
		L2ItemInstance item = _paperdoll[slot];
		if (item != null)
		{
			return item.getItemId();
		}

		return 0;
	}

	public long getPaperdollAugmentationId(int slot)
	{
		L2ItemInstance item = _paperdoll[slot];
		if (item != null)
		{
			//Do not show augment glow on hero weapons
			if (Config.isServer(Config.TENKAI) && item.isWeapon() && item.isHeroItem())
			{
				return 0;
			}

			if (item.getAugmentation() != null)
			{
				return item.getAugmentation().getId();
			}
			else
			{
				return 0;
			}
		}
		return 0;
	}

	/**
	 * Returns the objectID associated to the item in the paperdoll slot
	 *
	 * @param slot : int pointing out the slot
	 * @return int designating the objectID
	 */
	public int getPaperdollObjectId(int slot)
	{
		L2ItemInstance item = _paperdoll[slot];
		if (item != null)
		{
			return item.getObjectId();
		}
		return 0;
	}

	/**
	 * Adds new inventory's paperdoll listener
	 */
	public synchronized void addPaperdollListener(PaperdollListener listener)
	{
		assert !_paperdollListeners.contains(listener);
		_paperdollListeners.add(listener);
	}

	/**
	 * Removes a paperdoll listener
	 */
	public synchronized void removePaperdollListener(PaperdollListener listener)
	{
		_paperdollListeners.remove(listener);
	}

	public L2ItemInstance setPaperdollItem(int slot, L2ItemInstance item)
	{
		return setPaperdollItem(slot, item, true);
	}

	/**
	 * Equips an item in the given slot of the paperdoll.
	 * <U><I>Remark :</I></U> The item <B>HAS TO BE</B> already in the inventory
	 *
	 * @param slot : int pointing out the slot of the paperdoll
	 * @param item : L2ItemInstance pointing out the item to add in slot
	 * @return L2ItemInstance designating the item placed in the slot before
	 */
	public synchronized L2ItemInstance setPaperdollItem(int slot, L2ItemInstance item, boolean updateDb)
	{
		L2ItemInstance old = _paperdoll[slot];
		if (old != item)
		{
			if (old != null)
			{
				_paperdoll[slot] = null;
				// Put old item from paperdoll slot to base location
				old.setLocation(getBaseLocation());
				old.setLastChange(L2ItemInstance.MODIFIED);
				// Get the mask for paperdoll
				int mask = 0;
				for (int i = 0; i < PAPERDOLL_TOTALSLOTS; i++)
				{
					L2ItemInstance pi = _paperdoll[i];
					if (pi != null)
					{
						mask |= pi.getItem().getItemMask();
					}
				}
				_wearedMask = mask;
				// Notify all paperdoll listener in order to unequip old item in slot
				for (PaperdollListener listener : _paperdollListeners)
				{
					if (listener == null)
					{
						continue;
					}

					listener.notifyUnequiped(slot, old, this);
				}
				old.updateDatabase();
			}
			// Add new item in slot of paperdoll
			if (item != null)
			{
				_paperdoll[slot] = item;
				item.setLocation(getEquipLocation(), slot);
				item.setLastChange(L2ItemInstance.MODIFIED);
				_wearedMask |= item.getItem().getItemMask();
				for (PaperdollListener listener : _paperdollListeners)
				{
					if (listener == null)
					{
						continue;
					}

					listener.notifyEquiped(slot, item, this);
				}
				item.updateDatabase();
			}
		}
		return old;
	}

	/**
	 * Return the mask of weared item
	 *
	 * @return int
	 */
	public int getWearedMask()
	{
		return _wearedMask;
	}

	public int getSlotFromItem(L2ItemInstance item)
	{
		int slot = -1;
		int location = item.getLocationSlot();

		switch (location)
		{
			case PAPERDOLL_UNDER:
				slot = L2Item.SLOT_UNDERWEAR;
				break;
			case PAPERDOLL_LEAR:
				slot = L2Item.SLOT_L_EAR;
				break;
			case PAPERDOLL_REAR:
				slot = L2Item.SLOT_R_EAR;
				break;
			case PAPERDOLL_NECK:
				slot = L2Item.SLOT_NECK;
				break;
			case PAPERDOLL_RFINGER:
				slot = L2Item.SLOT_R_FINGER;
				break;
			case PAPERDOLL_LFINGER:
				slot = L2Item.SLOT_L_FINGER;
				break;
			case PAPERDOLL_HAIR:
				slot = L2Item.SLOT_HAIR;
				break;
			case PAPERDOLL_HAIR2:
				slot = L2Item.SLOT_HAIR2;
				break;
			case PAPERDOLL_HEAD:
				slot = L2Item.SLOT_HEAD;
				break;
			case PAPERDOLL_RHAND:
				slot = L2Item.SLOT_R_HAND;
				break;
			case PAPERDOLL_LHAND:
				slot = L2Item.SLOT_L_HAND;
				break;
			case PAPERDOLL_GLOVES:
				slot = L2Item.SLOT_GLOVES;
				break;
			case PAPERDOLL_CHEST:
				slot = item.getItem().getBodyPart();
				break;
			case PAPERDOLL_LEGS:
				slot = L2Item.SLOT_LEGS;
				break;
			case PAPERDOLL_CLOAK:
				slot = L2Item.SLOT_BACK;
				break;
			case PAPERDOLL_FEET:
				slot = L2Item.SLOT_FEET;
				break;
			case PAPERDOLL_LBRACELET:
				slot = L2Item.SLOT_L_BRACELET;
				break;
			case PAPERDOLL_RBRACELET:
				slot = L2Item.SLOT_R_BRACELET;
				break;
			case PAPERDOLL_DECO1:
			case PAPERDOLL_DECO2:
			case PAPERDOLL_DECO3:
			case PAPERDOLL_DECO4:
			case PAPERDOLL_DECO5:
			case PAPERDOLL_DECO6:
				slot = L2Item.SLOT_DECO;
				break;
			case PAPERDOLL_BELT:
				slot = L2Item.SLOT_BELT;
				break;
			case PAPERDOLL_BROOCH:
				slot = L2Item.SLOT_BROOCH;
				break;
			case PAPERDOLL_JEWELRY1:
			case PAPERDOLL_JEWELRY2:
			case PAPERDOLL_JEWELRY3:
			case PAPERDOLL_JEWELRY4:
			case PAPERDOLL_JEWELRY5:
			case PAPERDOLL_JEWELRY6:
				slot = L2Item.SLOT_JEWELRY;
				break;
		}
		return slot;
	}

	/**
	 * Unequips item in body slot and returns alterations.<BR>
	 * <B>If you dont need return value use {@link Inventory#unEquipItemInBodySlot(int)} instead</B>
	 *
	 * @param slot : int designating the slot of the paperdoll
	 * @return L2ItemInstance[] : list of changes
	 */
	public L2ItemInstance[] unEquipItemInBodySlotAndRecord(int slot)
	{
		Inventory.ChangeRecorder recorder = newRecorder();

		try
		{
			unEquipItemInBodySlot(slot);
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}

	/**
	 * Sets item in slot of the paperdoll to null value
	 *
	 * @param pdollSlot : int designating the slot
	 * @return L2ItemInstance designating the item in slot before change
	 */
	public L2ItemInstance unEquipItemInSlot(int pdollSlot)
	{
		return setPaperdollItem(pdollSlot, null);
	}

	/**
	 * Unepquips item in slot and returns alterations<BR>
	 * <B>If you dont need return value use {@link Inventory#unEquipItemInSlot(int)} instead</B>
	 *
	 * @param slot : int designating the slot
	 * @return L2ItemInstance[] : list of items altered
	 */
	public L2ItemInstance[] unEquipItemInSlotAndRecord(int slot)
	{
		Inventory.ChangeRecorder recorder = newRecorder();

		try
		{
			unEquipItemInSlot(slot);
			if (getOwner() instanceof L2PcInstance)
			{
				((L2PcInstance) getOwner()).refreshExpertisePenalty();
			}
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}

	/**
	 * Unequips item in slot (i.e. equips with default value)
	 *
	 * @param slot : int designating the slot
	 * @return {@link L2ItemInstance} designating the item placed in the slot
	 */
	public L2ItemInstance unEquipItemInBodySlot(int slot)
	{
		if (Config.DEBUG)
		{
			Log.fine("--- unequip body slot:" + slot);
		}

		int pdollSlot = -1;

		switch (slot)
		{
			case L2Item.SLOT_L_EAR:
				pdollSlot = PAPERDOLL_LEAR;
				break;
			case L2Item.SLOT_R_EAR:
			case L2Item.SLOT_LR_EAR:
				pdollSlot = PAPERDOLL_REAR;
				break;
			case L2Item.SLOT_NECK:
				pdollSlot = PAPERDOLL_NECK;
				break;
			case L2Item.SLOT_R_FINGER:
				pdollSlot = PAPERDOLL_RFINGER;
				break;
			case L2Item.SLOT_L_FINGER:
			case L2Item.SLOT_LR_FINGER:
				pdollSlot = PAPERDOLL_LFINGER;
				break;
			case L2Item.SLOT_HAIR:
				pdollSlot = PAPERDOLL_HAIR;
				break;
			case L2Item.SLOT_HAIR2:
				pdollSlot = PAPERDOLL_HAIR2;
				break;
			case L2Item.SLOT_HAIRALL:
				setPaperdollItem(PAPERDOLL_HAIR, null);
				pdollSlot = PAPERDOLL_HAIR;
				break;
			case L2Item.SLOT_HEAD:
				pdollSlot = PAPERDOLL_HEAD;
				break;
			case L2Item.SLOT_R_HAND:
			case L2Item.SLOT_LR_HAND:
				pdollSlot = PAPERDOLL_RHAND;
				break;
			case L2Item.SLOT_L_HAND:
				pdollSlot = PAPERDOLL_LHAND;
				break;
			case L2Item.SLOT_GLOVES:
				pdollSlot = PAPERDOLL_GLOVES;
				break;
			case L2Item.SLOT_CHEST:
			case L2Item.SLOT_ALLDRESS:
			case L2Item.SLOT_FULL_ARMOR:
				pdollSlot = PAPERDOLL_CHEST;
				break;
			case L2Item.SLOT_LEGS:
				pdollSlot = PAPERDOLL_LEGS;
				break;
			case L2Item.SLOT_BACK:
				pdollSlot = PAPERDOLL_CLOAK;
				break;
			case L2Item.SLOT_FEET:
				pdollSlot = PAPERDOLL_FEET;
				break;
			case L2Item.SLOT_UNDERWEAR:
				pdollSlot = PAPERDOLL_UNDER;
				break;
			case L2Item.SLOT_L_BRACELET:
				pdollSlot = PAPERDOLL_LBRACELET;
				break;
			case L2Item.SLOT_R_BRACELET:
				pdollSlot = PAPERDOLL_RBRACELET;
				break;
			case L2Item.SLOT_DECO:
				pdollSlot = PAPERDOLL_DECO1;
				break;
			case L2Item.SLOT_BELT:
				pdollSlot = PAPERDOLL_BELT;
				break;
			case L2Item.SLOT_BROOCH:
				pdollSlot = PAPERDOLL_BROOCH;
				break;
			case L2Item.SLOT_JEWELRY:
				pdollSlot = PAPERDOLL_JEWELRY1;
				break;
			default:
				Log.info("Unhandled slot type: " + slot);
				Log.info(StringUtil.getTraceString(Thread.currentThread().getStackTrace()));
		}
		if (pdollSlot >= 0)
		{
			L2ItemInstance old = setPaperdollItem(pdollSlot, null);
			if (old != null)
			{
				if (getOwner() instanceof L2PcInstance)
				{
					((L2PcInstance) getOwner()).refreshExpertisePenalty();
				}
			}
			return old;
		}
		return null;
	}

	/**
	 * Equips item and returns list of alterations<BR>
	 * <B>If you dont need return value use {@link Inventory#equipItem(L2ItemInstance)} instead</B>
	 *
	 * @param item : L2ItemInstance corresponding to the item
	 * @return L2ItemInstance[] : list of alterations
	 */
	public L2ItemInstance[] equipItemAndRecord(L2ItemInstance item)
	{
		Inventory.ChangeRecorder recorder = newRecorder();

		try
		{
			equipItem(item);
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}

	public void equipItem(L2ItemInstance item)
	{
		equipItem(item, true);
	}

	/**
	 * Equips item in slot of paperdoll.
	 *
	 * @param item : L2ItemInstance designating the item and slot used.
	 */
	public void equipItem(L2ItemInstance item, boolean updateDb)
	{
		if (getOwner() instanceof L2PcInstance && ((L2PcInstance) getOwner()).getPrivateStoreType() != 0)
		{
			return;
		}

		if (getOwner() instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) getOwner();
			if (!player.isGM() && !player.isHero() && item.isHeroItem())
			{
				return;
			}
		}

		int targetSlot = item.getItem().getBodyPart();

		//check if player wear formal
		L2ItemInstance formal = getPaperdollItem(PAPERDOLL_CHEST);
		if (formal != null && formal.getItem().getBodyPart() == L2Item.SLOT_ALLDRESS)
		{
			switch (targetSlot)
			{
				// only chest target can pass this
				case L2Item.SLOT_LR_HAND:
				case L2Item.SLOT_L_HAND:
				case L2Item.SLOT_R_HAND:
				case L2Item.SLOT_LEGS:
				case L2Item.SLOT_FEET:
				case L2Item.SLOT_GLOVES:
				case L2Item.SLOT_HEAD:
					setPaperdollItem(PAPERDOLL_CHEST, null, updateDb);
			}
		}

		switch (targetSlot)
		{
			case L2Item.SLOT_LR_HAND:
			{
				setPaperdollItem(PAPERDOLL_LHAND, null, updateDb);
				setPaperdollItem(PAPERDOLL_RHAND, item, updateDb);
				break;
			}
			case L2Item.SLOT_L_HAND:
			{
				L2ItemInstance rh = getPaperdollItem(PAPERDOLL_RHAND);
				if (rh != null && rh.getItem().getBodyPart() == L2Item.SLOT_LR_HAND &&
						(!(rh.getItemType() == L2WeaponType.BOW && item.getItemType() == L2EtcItemType.ARROW ||
								(rh.getItemType() == L2WeaponType.CROSSBOW ||
										rh.getItemType() == L2WeaponType.CROSSBOWK) &&
										item.getItemType() == L2EtcItemType.BOLT ||
								rh.getItemType() == L2WeaponType.FISHINGROD &&
										item.getItemType() == L2EtcItemType.LURE) ||
								item.getItem().getItemGradePlain() != rh.getItem().getItemGradePlain()))
				{
					setPaperdollItem(PAPERDOLL_RHAND, null, updateDb);
				}

				setPaperdollItem(PAPERDOLL_LHAND, item, updateDb);
				break;
			}
			case L2Item.SLOT_R_HAND:
			{
				// dont care about arrows, listener will unequip them (hopefully)
				setPaperdollItem(PAPERDOLL_RHAND, item, updateDb);
				break;
			}
			case L2Item.SLOT_L_EAR:
			case L2Item.SLOT_R_EAR:
			case L2Item.SLOT_LR_EAR:
			{
				if (_paperdoll[PAPERDOLL_LEAR] == null)
				{
					setPaperdollItem(PAPERDOLL_LEAR, item, updateDb);
				}
				else if (_paperdoll[PAPERDOLL_REAR] == null)
				{
					setPaperdollItem(PAPERDOLL_REAR, item, updateDb);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_LEAR, item, updateDb);
				}
				break;
			}
			case L2Item.SLOT_L_FINGER:
			case L2Item.SLOT_R_FINGER:
			case L2Item.SLOT_LR_FINGER:
			{
				if (_paperdoll[PAPERDOLL_LFINGER] == null)
				{
					setPaperdollItem(PAPERDOLL_LFINGER, item, updateDb);
				}
				else if (_paperdoll[PAPERDOLL_RFINGER] == null)
				{
					setPaperdollItem(PAPERDOLL_RFINGER, item, updateDb);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_LFINGER, item, updateDb);
				}
				break;
			}
			case L2Item.SLOT_NECK:
				setPaperdollItem(PAPERDOLL_NECK, item, updateDb);
				break;
			case L2Item.SLOT_FULL_ARMOR:
				setPaperdollItem(PAPERDOLL_LEGS, null, updateDb);
				setPaperdollItem(PAPERDOLL_CHEST, item, updateDb);
				break;
			case L2Item.SLOT_CHEST:
				setPaperdollItem(PAPERDOLL_CHEST, item, updateDb);
				break;
			case L2Item.SLOT_LEGS:
			{
				// handle full armor
				L2ItemInstance chest = getPaperdollItem(PAPERDOLL_CHEST);
				if (chest != null && chest.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR)
				{
					setPaperdollItem(PAPERDOLL_CHEST, null, updateDb);
				}

				setPaperdollItem(PAPERDOLL_LEGS, item, updateDb);
				break;
			}
			case L2Item.SLOT_FEET:
				setPaperdollItem(PAPERDOLL_FEET, item, updateDb);
				break;
			case L2Item.SLOT_GLOVES:
				setPaperdollItem(PAPERDOLL_GLOVES, item, updateDb);
				break;
			case L2Item.SLOT_HEAD:
				setPaperdollItem(PAPERDOLL_HEAD, item, updateDb);
				break;
			case L2Item.SLOT_HAIR:
				L2ItemInstance hair = getPaperdollItem(PAPERDOLL_HAIR);
				if (hair != null && hair.getItem().getBodyPart() == L2Item.SLOT_HAIRALL)
				{
					setPaperdollItem(PAPERDOLL_HAIR2, null, updateDb);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_HAIR, null, updateDb);
				}

				setPaperdollItem(PAPERDOLL_HAIR, item, updateDb);
				break;
			case L2Item.SLOT_HAIR2:
				L2ItemInstance hair2 = getPaperdollItem(PAPERDOLL_HAIR);
				if (hair2 != null && hair2.getItem().getBodyPart() == L2Item.SLOT_HAIRALL)
				{
					setPaperdollItem(PAPERDOLL_HAIR, null, updateDb);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_HAIR2, null, updateDb);
				}

				setPaperdollItem(PAPERDOLL_HAIR2, item, updateDb);
				break;
			case L2Item.SLOT_HAIRALL:
				setPaperdollItem(PAPERDOLL_HAIR2, null, updateDb);
				setPaperdollItem(PAPERDOLL_HAIR, item, updateDb);
				break;
			case L2Item.SLOT_UNDERWEAR:
				setPaperdollItem(PAPERDOLL_UNDER, item, updateDb);
				break;
			case L2Item.SLOT_BACK:
				setPaperdollItem(PAPERDOLL_CLOAK, item, updateDb);
				break;
			case L2Item.SLOT_L_BRACELET:
				setPaperdollItem(PAPERDOLL_LBRACELET, item, updateDb);
				break;
			case L2Item.SLOT_R_BRACELET:
				setPaperdollItem(PAPERDOLL_RBRACELET, item, updateDb);
				break;
			case L2Item.SLOT_DECO:
				equipTalisman(item, updateDb);
				break;
			case L2Item.SLOT_BELT:
				setPaperdollItem(PAPERDOLL_BELT, item, updateDb);
				break;
			case L2Item.SLOT_BROOCH:
				setPaperdollItem(PAPERDOLL_BROOCH, item, updateDb);
				break;
			case L2Item.SLOT_JEWELRY:
				equipJewelry(item, updateDb);
				break;
			case L2Item.SLOT_ALLDRESS:
				// formal dress
				setPaperdollItem(PAPERDOLL_LEGS, null, updateDb);
				setPaperdollItem(PAPERDOLL_LHAND, null, updateDb);
				setPaperdollItem(PAPERDOLL_RHAND, null, updateDb);
				setPaperdollItem(PAPERDOLL_RHAND, null, updateDb);
				setPaperdollItem(PAPERDOLL_LHAND, null, updateDb);
				setPaperdollItem(PAPERDOLL_HEAD, null, updateDb);
				setPaperdollItem(PAPERDOLL_FEET, null, updateDb);
				setPaperdollItem(PAPERDOLL_GLOVES, null, updateDb);
				setPaperdollItem(PAPERDOLL_CHEST, item, updateDb);
				break;
			default:
				Log.warning("Unknown body slot " + targetSlot + " for Item ID:" + item.getItemId());
		}
	}

	/**
	 * Refresh the weight of equipment loaded
	 */
	@Override
	protected void refreshWeight()
	{
		long weight = 0;

		for (L2ItemInstance item : _items.values())
		{
			if (item != null && item.getItem() != null)
			{
				weight += item.getItem().getWeight() * item.getCount();
			}
		}
		_totalWeight = (int) Math.min(weight, Integer.MAX_VALUE);
	}

	/**
	 * Returns the totalWeight.
	 *
	 * @return int
	 */
	public int getTotalWeight()
	{
		return _totalWeight;
	}

	/**
	 * Return the L2ItemInstance of the arrows needed for this bow.<BR><BR>
	 *
	 * @param bow : L2Item designating the bow
	 * @return L2ItemInstance pointing out arrows for bow
	 */
	public L2ItemInstance findArrowForBow(L2Item bow)
	{
		if (bow == null)
		{
			return null;
		}

		L2ItemInstance arrow = null;

		for (L2ItemInstance item : getItems())
		{
			if (item == null)
			{
				continue;
			}

			if (item.getItemType() == L2EtcItemType.ARROW &&
					item.getItem().getItemGradePlain() == bow.getItemGradePlain())
			{
				arrow = item;
				break;
			}
		}

		// Get the L2ItemInstance corresponding to the item identifier and return it
		return arrow;
	}

	/**
	 * Return the L2ItemInstance of the bolts needed for this crossbow.<BR><BR>
	 *
	 * @param crossbow : L2Item designating the crossbow
	 * @return L2ItemInstance pointing out bolts for crossbow
	 */
	public L2ItemInstance findBoltForCrossBow(L2Item crossbow)
	{
		L2ItemInstance bolt = null;

		for (L2ItemInstance item : getItems())
		{
			if (item == null)
			{
				continue;
			}

			if (item.getItemType() == L2EtcItemType.BOLT &&
					item.getItem().getItemGradePlain() == crossbow.getItemGradePlain())
			{
				bolt = item;
				break;
			}
		}

		// Get the L2ItemInstance corresponding to the item identifier and return it
		return bolt;
	}

	/**
	 * Get back items in inventory from database
	 */
	@Override
	public void restore()
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"SELECT object_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, mana_left, time, appearance, mob_id FROM items WHERE owner_id=? AND (loc=? OR loc=?) ORDER BY loc_data");
			statement.setInt(1, getOwnerId());
			statement.setString(2, getBaseLocation().name());
			statement.setString(3, getEquipLocation().name());
			ResultSet inv = statement.executeQuery();

			L2ItemInstance item;
			while (inv.next())
			{
				item = L2ItemInstance.restoreFromDb(getOwnerId(), inv);

				if (item == null)
				{
					continue;
				}

				if (getOwner() instanceof L2PcInstance)
				{
					L2PcInstance player = (L2PcInstance) getOwner();

					if (!player.isGM() && !player.isHero() && item.isHeroItem())
					{
						item.setLocation(ItemLocation.INVENTORY);
					}
				}

				L2World.getInstance().storeObject(item);

				// If stackable item is found in inventory just add to current quantity
				if (item.isStackable() && getItemByItemId(item.getItemId()) != null)
				{
					addItem("Restore", item, getOwner().getActingPlayer(), null);
				}
				else
				{
					addItem(item);
				}
			}
			inv.close();
			statement.close();
			refreshWeight();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Could not restore inventory: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public int getMaxTalismanCount()
	{
		return (int) getOwner().getStat().calcStat(Stats.TALISMAN_SLOTS, 0, null, null);
	}

	private void equipTalisman(L2ItemInstance item, boolean updateDb)
	{
		if (getMaxTalismanCount() == 0)
		{
			return;
		}

		// find same (or incompatible) talisman type
		for (int i = PAPERDOLL_DECO1; i < PAPERDOLL_DECO1 + getMaxTalismanCount(); i++)
		{
			if (_paperdoll[i] != null)
			{
				if (getPaperdollItemId(i) == item.getItemId())
				{
					// overwrite
					setPaperdollItem(i, item, updateDb);
					return;
				}
			}
		}

		// free slot found - put on first free
		for (int i = PAPERDOLL_DECO1; i < PAPERDOLL_DECO1 + getMaxTalismanCount(); i++)
		{
			if (_paperdoll[i] == null)
			{
				setPaperdollItem(i, item, updateDb);
				return;
			}
		}

		// no free slots - put on first
		setPaperdollItem(PAPERDOLL_DECO1, item, updateDb);
	}

	public int getMaxJewelryCount()
	{
		return (int) getOwner().getStat().calcStat(Stats.JEWELRY_SLOTS, 0, null, null);
	}

	private void equipJewelry(L2ItemInstance item, boolean updateDb)
	{
		if (getMaxJewelryCount() == 0)
		{
			return;
		}

		// find same (or incompatible) jewel type
		for (int i = PAPERDOLL_JEWELRY1; i < PAPERDOLL_JEWELRY1 + getMaxJewelryCount(); i++)
		{
			if (_paperdoll[i] != null)
			{
				if (getPaperdollItemId(i) == item.getItemId() ||
						item.getName().startsWith(_paperdoll[i].getName().substring(0, 4)))
				{
					// overwrite
					setPaperdollItem(i, item, updateDb);
					return;
				}
			}
		}

		// free slot found - put on first free
		for (int i = PAPERDOLL_JEWELRY1; i < PAPERDOLL_JEWELRY1 + getMaxJewelryCount(); i++)
		{
			if (_paperdoll[i] == null)
			{
				setPaperdollItem(i, item, updateDb);
				return;
			}
		}

		// no free slots - put on first
		setPaperdollItem(PAPERDOLL_JEWELRY1, item, updateDb);
	}

	public int getCloakStatus()
	{
		if (getOwner() instanceof L2PcInstance && ((L2PcInstance) getOwner()).getCurrentClass() != null &&
				((L2PcInstance) getOwner()).getCurrentClass().getLevel() == 85)
		{
			return 1;
		}

		return (int) getOwner().getStat().calcStat(Stats.CLOAK_SLOT, 0, null, null);
	}

	/**
	 * Re-notify to paperdoll listeners every equipped item
	 */
	public void reloadEquippedItems()
	{
		int slot;

		for (L2ItemInstance item : _paperdoll)
		{
			if (item == null)
			{
				continue;
			}

			slot = item.getLocationSlot();

			for (PaperdollListener listener : _paperdollListeners)
			{
				if (listener == null)
				{
					continue;
				}

				listener.notifyUnequiped(slot, item, this);
				listener.notifyEquiped(slot, item, this);
			}
		}
	}
}
