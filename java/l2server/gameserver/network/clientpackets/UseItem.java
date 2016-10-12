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

package l2server.gameserver.network.clientpackets;

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.TimeController;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.handler.IVoicedCommandHandler;
import l2server.gameserver.handler.ItemHandler;
import l2server.gameserver.handler.VoicedCommandHandler;
import l2server.gameserver.instancemanager.FortSiegeManager;
import l2server.gameserver.model.Elementals;
import l2server.gameserver.model.EnsoulEffect;
import l2server.gameserver.model.L2Augmentation;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.base.Race;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.templates.item.*;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

/**
 * This class ...
 *
 * @version $Revision: 1.18.2.7.2.9 $ $Date: 2005/03/27 15:29:30 $
 */
public final class UseItem extends L2GameClientPacket
{
	private int _objectId;
	private boolean _ctrlPressed;
	private int _itemId;

	/**
	 * Weapon Equip Task
	 */
	public static class WeaponEquipTask implements Runnable
	{
		L2ItemInstance item;
		L2PcInstance activeChar;

		public WeaponEquipTask(L2ItemInstance it, L2PcInstance character)
		{
			item = it;
			activeChar = character;
		}

		@Override
		public void run()
		{
			//If character is still engaged in strike we should not change weapon
			if (activeChar.isAttackingNow())
			{
				return;
			}
			// Equip or unEquip
			activeChar.useEquippableItem(item, false);
		}
	}

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_ctrlPressed = readD() != 0;
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		// Flood protect UseItem
		if (!getClient().getFloodProtectors().getUseItem().tryPerformAction("use item"))
		{
			return;
		}

		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		if (item == null)
		{
			return;
		}
		else if (!item.isEquipable() && !getClient().getFloodProtectors().getUseItem().tryPerformAction("use item"))
		{
			return;
		}

		if (activeChar.getPrivateStoreType() != 0 && item.getItemId() != 1373)
		{
			activeChar.sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.getActiveTradeList() != null)
		{
			activeChar.cancelActiveTrade();
		}

		// cannot use items during Fear (possible more abnormal states?)
		if (activeChar.isAfraid() || activeChar.isInLove())
		{
			// no sysmsg
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// NOTE: disabled due to deadlocks
		// synchronized (activeChar.getInventory())
		// 	{

		if (activeChar.isGM())
		{
			activeChar.sendSysMessage("Using Item[" + item.getItemId() + "]");
		}

		if (item.getItem().isVitality())
		{
			if (activeChar.getVitalityItemsUsed() >= 5)
			{
				activeChar.sendMessage("You can not use more than 5 vitality items on a week.");
				return;
			}
			else
			{
				activeChar.increaseVitalityItemsUsed();
			}
		}

		if (activeChar.hasIdentityCrisis() && !item.isPotion())
		{
			activeChar.sendMessage("While identity crisis is active you can only use potions.");
			return;
		}

		// Tenkai customization - .itemid command for showing players object id of items
		if (activeChar.isItemId())
		{
			String itemName = item.toString();
			if (item.getEnchantLevel() > 0)
			{
				itemName += " +" + item.getEnchantLevel();
			}

			activeChar.sendMessage("Your " + itemName + "'s id is " + item.getObjectId() + ".");

			activeChar.setIsItemId(false);
			return;
		}

		/*if (activeChar.getActiveEnchantAttrItem() != null)
		{
			RequestExEnchantItemAttribute.enchantItemAttribute(activeChar, item.getObjectId());
			return;
		}*/

		if (activeChar.getCurrentUnbindScroll() != null)
		{
			if (!item.isEquipable() || !item.getName().contains("Bound") || item.getItem().getStandardItem() == -1)
			{
				activeChar.sendMessage("This item is not bound!");
				activeChar.setCurrentUnbindScroll(null);
				return;
			}

			L2Item scroll = activeChar.getCurrentUnbindScroll().getItem();
			if (item.getItem().getCrystalType() != scroll.getCrystalType())
			{
				activeChar.sendMessage("Invalid item grade.");
				activeChar.setCurrentUnbindScroll(null);
				return;
			}

			int productId = item.getItem().getStandardItem();
			if (item.getItem().getBlessedItem() != -1 && Rnd.get(100) < 10)
			{
				productId = item.getItem().getBlessedItem();
			}

			int enchantLevel = item.getEnchantLevel();
			int appId = item.getAppearance();

			EnsoulEffect[] ensoulEffects = null;
			L2Augmentation augmentation = null;
			Elementals[] elementals = null;
			if (item.isSoulEnhanced())
			{
				ensoulEffects = item.getEnsoulEffects();
			}
			if (item.isAugmented())
			{
				augmentation = item.getAugmentation();
			}
			if (item.getElementals() != null)
			{
				elementals = item.getElementals();
			}

			if (!activeChar.destroyItem("Unbind", item.getObjectId(), 1, activeChar, true) || !activeChar
					.destroyItem("Unbind", activeChar.getCurrentUnbindScroll().getObjectId(), 1, activeChar, true))
			{
				activeChar.setCurrentUnbindScroll(null);
				return;
			}

			L2ItemInstance product = activeChar.addItem("Unbind", productId, 1, activeChar, true);
			product.setEnchantLevel(enchantLevel);
			if (ensoulEffects != null)
			{
				for (int i = 0; i < ensoulEffects.length; i++)
				{
					product.setEnsoulEffect(i, ensoulEffects[i]);
				}
			}
			if (augmentation != null)
			{
				product.setAugmentation(new L2Augmentation(augmentation.getAugment1(), augmentation.getAugment2()));
			}
			if (elementals != null)
			{
				for (Elementals elm : elementals)
				{
					product.setElementAttr(elm.getElement(), elm.getValue());
				}
			}
			if (appId != 0)
			{
				product.setAppearance(appId);
			}
			activeChar.setCurrentUnbindScroll(null);
			return;
		}

		if (activeChar.getCurrentBlessingScroll() != null)
		{
			if (!item.isEquipable() || item.getName().contains("Bound") || item.getName().contains("Blessed") ||
					item.getItem().getBlessedItem() == -1)
			{
				activeChar.sendMessage("This item cannot be blessed!");
				activeChar.setCurrentBlessingScroll(null);
				return;
			}

			if (item.isEquipped())
			{
				activeChar.sendMessage("You can't use it on an item that is equipped!");
				return;
			}

			L2Item scroll = activeChar.getCurrentBlessingScroll().getItem();
			if (item.getItem().getCrystalType() != scroll.getCrystalType())
			{
				activeChar.sendMessage("Invalid item grade.");
				activeChar.setCurrentBlessingScroll(null);
				return;
			}

			int productId = item.getItem().getBlessedItem();
			int app = item.getAppearance();
			int enchantLevel = item.getEnchantLevel();
			EnsoulEffect[] ensoulEffects = null;
			L2Augmentation augmentation = null;
			Elementals[] elementals = null;
			if (item.isSoulEnhanced())
			{
				ensoulEffects = item.getEnsoulEffects();
			}
			if (item.isAugmented())
			{
				augmentation = item.getAugmentation();
			}
			if (item.getElementals() != null)
			{
				elementals = item.getElementals();
			}

			if (!activeChar.destroyItem("Blessing", item.getObjectId(), 1, activeChar, true) || !activeChar
					.destroyItem("Blessing", activeChar.getCurrentBlessingScroll().getObjectId(), 1, activeChar, true))
			{
				activeChar.setCurrentBlessingScroll(null);
				return;
			}

			L2ItemInstance product = activeChar.addItem("Blessing", productId, 1, activeChar, true);
			product.setEnchantLevel(enchantLevel);
			if (ensoulEffects != null)
			{
				for (int i = 0; i < ensoulEffects.length; i++)
				{
					product.setEnsoulEffect(i, ensoulEffects[i]);
				}
			}
			if (augmentation != null)
			{
				product.setAugmentation(new L2Augmentation(augmentation.getAugment1(), augmentation.getAugment2()));
			}
			if (elementals != null)
			{
				for (Elementals elm : elementals)
				{
					product.setElementAttr(elm.getElement(), elm.getValue());
				}
			}
			if (app > 0)
			{
				product.setAppearance(app);
			}
			activeChar.broadcastUserInfo();
			activeChar.setCurrentBlessingScroll(null);
			return;
		}

		if (activeChar.getActiveAppearanceStone() != null)
		{
			L2ItemInstance stone = activeChar.getActiveAppearanceStone();

			if (stone.getName().contains("Restor"))
			{
				boolean isCorrectStone = stone.getName().contains("Weapon") && item.getItem() instanceof L2Weapon ||
						stone.getName().contains("Armor") && item.getItem() instanceof L2Armor ||
						stone.getName().contains("Equipment");

				@SuppressWarnings("unused") int type = stone.getStoneType();

				//activeChar.sendMessage("Stone Type = " + type);
				//activeChar.sendMessage("Item Type = " + item.getItem().getType2());
				if (item.getItem().getItemGradePlain() == stone.getItem().getItemGradePlain() && isCorrectStone)
				{
					Util.logToFile(activeChar.getName() + " is removing appearance on his " + item.getName() + ".",
							"Appearances", "txt", true, true);

					if (!activeChar.destroyItem("Appearance", stone.getObjectId(), 1, activeChar, true))
					{
						activeChar.setActiveAppearanceStone(null);
						return;
					}

					item.setAppearance(0);
					activeChar.sendMessage("Your " + item.getName() + "'s appearance has been restored.");
					InventoryUpdate iu = new InventoryUpdate();
					iu.addModifiedItem(item);
					activeChar.sendPacket(iu);
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("This is an incorrect item.");
				}
			}
			else if (stone.getItem().getStandardItem() >
					0) // The stones hold their appearance item template id in their standard item
			{
				L2Item template = ItemTable.getInstance().getTemplate(stone.getItem().getStandardItem());

				if (item.getItem().canBeUsedAsApp() && (item.getItem().getBodyPart() == template.getBodyPart() ||
						item.getItem().getBodyPart() == L2Item.SLOT_CHEST &&
								template.getBodyPart() == L2Item.SLOT_FULL_ARMOR) &&
						(item.isWeapon() && item.getItem().getItemType() == template.getItemType() || item.isArmor()) ||
						(item.getItem().getBodyPart() == L2Item.SLOT_CHEST ||
								item.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR) &&
								template.getBodyPart() == L2Item.SLOT_ALLDRESS)
				{
					Util.logToFile(
							activeChar.getName() + " is applying " + stone.getName() + " on his " + item.getName() +
									".", "Appearances", "txt", true, true);
					if (!activeChar.destroyItem("Appearance", stone.getObjectId(), 1, activeChar, true))
					{
						activeChar.setActiveAppearanceStone(null);
						return;
					}

					item.setAppearance(template.getItemId());
					activeChar.sendMessage("Your " + item.getName() + "'s appearance has been modified.");
					InventoryUpdate iu = new InventoryUpdate();
					iu.addModifiedItem(item);
					activeChar.sendPacket(iu);
					activeChar.broadcastUserInfo();
				}
				else
				{
					activeChar.sendMessage("This is an incorrect item.");
				}
			}

			activeChar.setActiveAppearanceStone(null);
			return;
		}

		// Tenkai customization - .sell command
		if (activeChar.isAddSellItem())
		{
			IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler("sell");
			if (vch != null)
			{
				vch.useVoicedCommand("sell", activeChar, "addItem " + item.getObjectId());
			}

			activeChar.setIsAddSellItem(false);
			return;
		}
		else if (activeChar.getAddSellPrice() > -1)
		{
			IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler("sell");
			if (vch != null)
			{
				vch.useVoicedCommand("sell", activeChar,
						"addPrice " + activeChar.getAddSellPrice() + " " + item.getItemId());
			}

			activeChar.setAddSellPrice(-1);
			return;
		}

		if (item.getItem().getType2() == L2Item.TYPE2_QUEST)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CANNOT_USE_QUEST_ITEMS);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}

		_itemId = item.getItemId();
        /*
		 * Alt game - Karma punishment // SOE
		 * 736  	Scroll of Escape
		 * 1538  	Blessed Scroll of Escape
		 * 1829  	Scroll of Escape: Clan Hall
		 * 1830  	Scroll of Escape: Castle
		 * 3958  	L2Day - Blessed Scroll of Escape
		 * 5858  	Blessed Scroll of Escape: Clan Hall
		 * 5859  	Blessed Scroll of Escape: Castle
		 * 6663  	Scroll of Escape: Orc Village
		 * 6664  	Scroll of Escape: Silenos Village
		 * 7117  	Scroll of Escape to Talking Island
		 * 7118  	Scroll of Escape to Elven Village
		 * 7119  	Scroll of Escape to Dark Elf Village
		 * 7120  	Scroll of Escape to Orc Village
		 * 7121  	Scroll of Escape to Dwarven Village
		 * 7122  	Scroll of Escape to Gludin Village
		 * 7123  	Scroll of Escape to the Town of Gludio
		 * 7124  	Scroll of Escape to the Town of Dion
		 * 7125  	Scroll of Escape to Floran
		 * 7126  	Scroll of Escape to Giran Castle Town
		 * 7127  	Scroll of Escape to Hardin's Private Academy
		 * 7128  	Scroll of Escape to Heine
		 * 7129  	Scroll of Escape to the Town of Oren
		 * 7130  	Scroll of Escape to Ivory Tower
		 * 7131  	Scroll of Escape to Hunters Village
		 * 7132  	Scroll of Escape to Aden Castle Town
		 * 7133  	Scroll of Escape to the Town of Goddard
		 * 7134  	Scroll of Escape to the Rune Township
		 * 7135  	Scroll of Escape to the Town of Schuttgart.
		 * 7554  	Scroll of Escape to Talking Island
		 * 7555  	Scroll of Escape to Elven Village
		 * 7556  	Scroll of Escape to Dark Elf Village
		 * 7557  	Scroll of Escape to Orc Village
		 * 7558  	Scroll of Escape to Dwarven Village
		 * 7559  	Scroll of Escape to Giran Castle Town
		 * 7618  	Scroll of Escape - Ketra Orc Village
		 * 7619  	Scroll of Escape - Varka Silenos Village
		 * 10129	Scroll of Escape : Fortress
		 * 10130	Blessed Scroll of Escape : Fortress
		 */
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && activeChar.getReputation() < 0)
		{
			switch (_itemId)
			{
				case 736:
				case 1538:
				case 1829:
				case 1830:
				case 3958:
				case 5858:
				case 5859:
				case 6663:
				case 6664:
				case 7554:
				case 7555:
				case 7556:
				case 7557:
				case 7558:
				case 7559:
				case 7618:
				case 7619:
				case 10129:
				case 10130:
					return;
			}

			if (_itemId >= 7117 && _itemId <= 7135)
			{
				return;
			}
		}

		if (activeChar.isFishing() && (_itemId < 6535 || _itemId > 6540))
		{
			// You cannot do anything else while fishing
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			getClient().getActiveChar().sendPacket(sm);
			sm = null;
			return;
		}

		// Char cannot use item when dead
		if (activeChar.isDead())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addItemName(item);
			getClient().getActiveChar().sendPacket(sm);
			sm = null;
			return;
		}

		// No UseItem is allowed while the player is in special conditions
		if (activeChar.isStunned() || activeChar.isSleeping() || activeChar.isParalyzed() || activeChar.isAlikeDead() ||
				activeChar.isAfraid() || activeChar.isCastingNow() && !(item.isPotion() || item.isElixir()))
		{
			return;
		}

		// Char cannot use pet items
		/*if ((item.getItem() instanceof L2Armor && item.getItem().getItemType() == L2ArmorType.PET)
				|| (item.getItem() instanceof L2Weapon && item.getItem().getItemType() == L2WeaponType.PET) )
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CANNOT_EQUIP_PET_ITEM); // You cannot equip a pet item.
			sm.addItemName(item);
			getClient().getActiveChar().sendPacket(sm);
			sm = null;
			return;
		}*/

		if (!activeChar.getInventory().canManipulateWithItemId(item.getItemId()))
		{
			activeChar.sendMessage("Cannot use this item.");
			return;
		}

		if (Config.DEBUG)
		{
			Log.finest(activeChar.getObjectId() + ": use item " + _objectId);
		}

		if (!item.isEquipped() && !item.getItem().checkCondition(activeChar, activeChar, true))
		{
			return;
		}

		if (item.isEquipable())
		{
			// Don't allow hero equipment and restricted items during Olympiad
			if (activeChar.isInOlympiadMode() && (item.isHeroItem() || item.getItem().isOlyRestricted()))
			{
				activeChar.sendPacket(SystemMessage
						.getSystemMessage(SystemMessageId.THIS_ITEM_CANT_BE_EQUIPPED_FOR_THE_OLYMPIAD_EVENT));
				return;
			}

			switch (item.getItem().getBodyPart())
			{
				case L2Item.SLOT_LR_HAND:
				case L2Item.SLOT_L_HAND:
				case L2Item.SLOT_R_HAND:
				{
					// prevent players to equip weapon while wearing combat flag
					if (activeChar.getActiveWeaponItem() != null &&
							activeChar.getActiveWeaponItem().getItemId() == 9819)
					{
						activeChar.sendPacket(
								SystemMessage.getSystemMessage(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION));
						return;
					}
					// Prevent player to remove the weapon on special conditions
					if (activeChar.isCastingNow() || activeChar.isCastingSimultaneouslyNow())
					{
						activeChar.sendPacket(
								SystemMessage.getSystemMessage(SystemMessageId.CANNOT_USE_ITEM_WHILE_USING_MAGIC));
						return;
					}
					if (activeChar.isMounted())
					{
						activeChar.sendPacket(
								SystemMessage.getSystemMessage(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION));
						return;
					}
					if (activeChar.isDisarmed())
					{
						activeChar.sendPacket(
								SystemMessage.getSystemMessage(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION));
						return;
					}

					// Don't allow weapon/shield equipment if a cursed weapon is equiped
					if (activeChar.isCursedWeaponEquipped())
					{
						return;
					}

					boolean isErtheiaMage = false;
					switch (activeChar.getClassId())
					{
						case 183:
						case 185:
						case 187:
						case 189:
							isErtheiaMage = true;
					}

					// Don't allow other Race to Wear Kamael exclusive Weapons.
					if (!item.isEquipped() && !activeChar.isGM())
					{
						if (item.getItem() instanceof L2Weapon)
						{
							L2Weapon wpn = (L2Weapon) item.getItem();
							if (item.getItem().getCrystalType() < L2Item.CRYSTAL_R)
							{
								switch (activeChar.getRace())
								{
									case Human:
									case Dwarf:
									case Elf:
									case DarkElf:
									case Orc:
									{
										switch (wpn.getItemType())
										{
											case RAPIER:
											case CROSSBOWK:
											case ANCIENTSWORD:
												activeChar.sendPacket(SystemMessage.getSystemMessage(
														SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION));
												return;
										}
										break;
									}
								}
							}
							if (isErtheiaMage && wpn.getItemType() == L2WeaponType.NONE)
							{
								activeChar.sendPacket(SystemMessage
										.getSystemMessage(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION));
								return;
							}
						}
						else if (item.getItem() instanceof L2Armor)
						{
							L2Armor armor = (L2Armor) item.getItem();
							if (isErtheiaMage && (armor.getItemType() == L2ArmorType.SHIELD ||
									armor.getItemType() == L2ArmorType.SIGIL))
							{
								activeChar.sendPacket(SystemMessage
										.getSystemMessage(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION));
								return;
							}
						}
					}
					break;
				}
				case L2Item.SLOT_CHEST:
					if (activeChar.isArmorDisarmed())
					{
						activeChar.sendPacket(
								SystemMessage.getSystemMessage(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION));
						return;
					}
				case L2Item.SLOT_BACK:
				case L2Item.SLOT_GLOVES:
				case L2Item.SLOT_FEET:
				case L2Item.SLOT_HEAD:
				case L2Item.SLOT_FULL_ARMOR:
				case L2Item.SLOT_LEGS:
				{
					if (activeChar.getRace() == Race.Kamael && item.getItem().getCrystalType() < L2Item.CRYSTAL_S &&
							item.getItem().getItemType() == L2ArmorType.HEAVY)
					{
						activeChar.sendPacket(
								SystemMessage.getSystemMessage(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION));
						return;
					}
					break;
				}
				case L2Item.SLOT_DECO:
				{
					if (!item.isEquipped() && activeChar.getInventory().getMaxTalismanCount() == 0)
					{
						activeChar.sendPacket(
								SystemMessage.getSystemMessage(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION));
						return;
					}
					break;
				}
				case L2Item.SLOT_BROOCH:
				{
					if (activeChar.getLevel() <= 85)
					{
						activeChar.sendPacket(
								SystemMessage.getSystemMessage(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION));
						return;
					}
					break;
				}
				case L2Item.SLOT_JEWELRY:
				{
					if (activeChar.getLevel() <= 85)
					{
						activeChar.sendPacket(
								SystemMessage.getSystemMessage(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION));
						return;
					}

					if (!item.isEquipped() && activeChar.getInventory().getMaxJewelryCount() == 0)
					{
						activeChar.sendPacket(
								SystemMessage.getSystemMessage(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION));
						return;
					}
					break;
				}
			}

			if (activeChar.isCursedWeaponEquipped() && _itemId == 6408) // Don't allow to put formal wear
			{
				return;
			}

			if (activeChar.isAttackingNow())
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new WeaponEquipTask(item, activeChar),
						(activeChar.getAttackEndTime() - TimeController.getGameTicks()) *
								TimeController.MILLIS_IN_TICK);
				return;
			}
			// Equip or unEquip
			if (FortSiegeManager.getInstance().isCombat(item.getItemId()))
			{
				return; //no message
			}
			else if (activeChar.isCombatFlagEquipped())
			{
				return;
			}

			activeChar.useEquippableItem(item, true);
		}
		else
		{
			if (activeChar.isInOlympiadMode() && _itemId == 37041)
			{
				return;
			}

			L2Weapon weaponItem = activeChar.getActiveWeaponItem();
			int itemid = item.getItemId();
			if (itemid == 4393)
			{
				activeChar.sendPacket(new ShowCalculator(4393));
			}
			else if (weaponItem != null && weaponItem.getItemType() == L2WeaponType.FISHINGROD &&
					(itemid >= 6519 && itemid <= 6527 || itemid >= 7610 && itemid <= 7613 ||
							itemid >= 7807 && itemid <= 7809 || itemid >= 8484 && itemid <= 8486 ||
							itemid >= 8505 && itemid <= 8513))
			{
				activeChar.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, item);
				activeChar.broadcastUserInfo();
				// Send a Server->Client packet ItemList to this L2PcINstance to update left hand equipement
				ItemList il = new ItemList(activeChar, false);
				sendPacket(il);
			}
			else
			{
				IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getEtcItem());
				if (handler == null)
				{
					if (Config.DEBUG)
					{
						Log.warning("No item handler registered for item ID " + item.getItemId() + ".");
					}
				}
				else
				{
					handler.useItem(activeChar, item, _ctrlPressed);
				}
			}
		}
		//		}
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return !Config.SPAWN_PROTECTION_ALLOWED_ITEMS.contains(_itemId);
	}
}
