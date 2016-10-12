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
import l2server.L2DatabaseFactory;
import l2server.gameserver.datatables.EnchantItemTable;
import l2server.gameserver.datatables.EnchantItemTable.EnchantScroll;
import l2server.gameserver.datatables.EnchantItemTable.EnchantSupportItem;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.templates.item.L2Armor;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Logger;

public final class RequestEnchantItem extends L2GameClientPacket
{
	protected static final Logger _logEnchant = Logger.getLogger("enchant");

	private int _objectId = 0;
	private int _supportId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_supportId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null || _objectId == 0)
		{
			return;
		}

		if (!activeChar.isOnline() || getClient().isDetached())
		{
			activeChar.setActiveEnchantItem(null);
			return;
		}

		if (activeChar.isProcessingTransaction() || activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_ENCHANT_WHILE_STORE));
			activeChar.setActiveEnchantItem(null);
			return;
		}

		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		L2ItemInstance scroll = activeChar.getActiveEnchantItem();
		L2ItemInstance support = activeChar.getActiveEnchantSupportItem();

		if (item == null || scroll == null)
		{
			activeChar.setActiveEnchantItem(null);
			return;
		}

		// template for scroll
		EnchantScroll scrollTemplate = EnchantItemTable.getInstance().getEnchantScroll(scroll);

		// scroll not found in list
		if (scrollTemplate == null)
		{
			return;
		}

		// template for support item, if exist
		EnchantSupportItem supportTemplate = null;
		if (support != null)
		{
			if (support.getObjectId() != _supportId)
			{
				activeChar.setActiveEnchantItem(null);
				return;
			}
			supportTemplate = EnchantItemTable.getInstance().getSupportItem(support);
		}

		// first validation check
		if (!scrollTemplate.isValid(item, supportTemplate) || !EnchantItemTable.isEnchantable(item))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
			activeChar.setActiveEnchantItem(null);
			activeChar.sendPacket(new EnchantResult(2, 0, 0, 0));
			return;
		}

		// fast auto-enchant cheat check
		if (activeChar.getActiveEnchantTimestamp() == 0 ||
				System.currentTimeMillis() - activeChar.getActiveEnchantTimestamp() < 1000)
		{
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " use autoenchant program ",
					Config.DEFAULT_PUNISH);
			activeChar.setActiveEnchantItem(null);
			activeChar.sendPacket(new EnchantResult(2, 0, 0, 0));
			return;
		}

		// attempting to destroy scroll
		scroll = activeChar.getInventory().destroyItem("Enchant", scroll.getObjectId(), 1, activeChar, item);
		if (scroll == null)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
			Util.handleIllegalPlayerAction(activeChar,
					"Player " + activeChar.getName() + " tried to enchant with a scroll he doesn't have",
					Config.DEFAULT_PUNISH);
			activeChar.setActiveEnchantItem(null);
			activeChar.sendPacket(new EnchantResult(2, 0, 0, 0));
			return;
		}

		// attempting to destroy support if exist
		if (support != null)
		{
			support = activeChar.getInventory().destroyItem("Enchant", support.getObjectId(), 1, activeChar, item);
			if (support == null)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
				Util.handleIllegalPlayerAction(activeChar,
						"Player " + activeChar.getName() + " tried to enchant with a support item he doesn't have",
						Config.DEFAULT_PUNISH);
				activeChar.setActiveEnchantItem(null);
				activeChar.sendPacket(new EnchantResult(2, 0, 0, 0));
				return;
			}
		}

		synchronized (item)
		{
			float chance = scrollTemplate.getChance(item, supportTemplate);

			int rnd = Rnd.get(100);
			boolean success = rnd < chance;

			// last validation check
			L2Item it = item.getItem();
			if (item.getOwnerId() != activeChar.getObjectId() || !EnchantItemTable.isEnchantable(item) || chance < 0)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
				activeChar.setActiveEnchantItem(null);
				activeChar.sendPacket(new EnchantResult(2, 0, 0, 0));
				return;
			}

			// Lastravel, LUC second chance
			rnd = Rnd.get(10000);
			if (!success && (item.getEnchantLevel() < 10 && rnd < activeChar.getLUC() ||
					item.getEnchantLevel() >= 10 && rnd < activeChar.getLUC() / 2))
			{
				//System.out.println("Enchant luck effect " + activeChar.getName() + " enchanted to " + (item.getEnchantLevel() + 1 + " (Luck: " + activeChar.getLUC() + ")"));
				success = true;

				//LUC animation
				L2Skill skill = SkillTable.FrequentSkill.LUCKY_CLOVER.getSkill();
				if (skill != null)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LADY_LUCK_SMILES_UPON_YOU));
					activeChar.broadcastPacket(
							new MagicSkillUse(activeChar, activeChar, skill.getId(), skill.getLevel(),
									skill.getHitTime(), skill.getReuseDelay(), skill.getReuseHashCode(), 0, 0));
				}
			}

			if (success)
			{
				// success
				int newEnchantLevel = item.getEnchantLevel() + 1;
				if (Config.isServer(Config.TENKAI_ESTHUS) && newEnchantLevel < 16)
				{
					newEnchantLevel = 16;
				}

				item.setEnchantLevel(newEnchantLevel);
				item.updateDatabase();
				activeChar.sendPacket(new EnchantResult(0, 0, 0, item.getEnchantLevel()));

				if (Config.LOG_ITEM_ENCHANTS)
				{
					Connection con = null;
					try
					{
						con = L2DatabaseFactory.getInstance().getConnection();
						PreparedStatement statement = con.prepareStatement(
								"INSERT INTO log_enchants(player_id, item_id, item_object_id, scroll, support_id, chance, time) VALUES(?,?,?,?,?,?,?)");
						statement.setInt(1, activeChar.getObjectId());
						statement.setInt(2, item.getItemId());
						statement.setInt(3, item.getObjectId());
						statement.setInt(4, scroll.getItemId());
						statement.setInt(5, support == null ? 0 : support.getObjectId());
						statement.setInt(6, (int) chance);
						statement.setLong(7, System.currentTimeMillis());
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

				// announce the success
				int minEnchantAnnounce = item.isArmor() ? 6 : 7;
				int maxEnchantAnnounce = item.isArmor() ? 0 : 15;
				if (!Config.isServer(Config.TENKAI_ESTHUS) &&
						(item.getEnchantLevel() == minEnchantAnnounce || item.getEnchantLevel() == maxEnchantAnnounce))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_SUCCESSFULY_ENCHANTED_A_S2_S3);
					sm.addCharName(activeChar);
					sm.addNumber(item.getEnchantLevel());
					sm.addItemName(item);
					activeChar.broadcastPacket(sm);

					L2Skill skill = SkillTable.FrequentSkill.FIREWORK.getSkill();
					if (skill != null)
					{
						activeChar.broadcastPacket(
								new MagicSkillUse(activeChar, activeChar, skill.getId(), skill.getLevelHash(),
										skill.getHitTime(), skill.getReuseDelay(), skill.getReuseHashCode(), 0, 0));
					}
				}

				if (it instanceof L2Armor &&
						activeChar.getInventory().getItemByObjectId(item.getObjectId()).isEquipped())
				{
					for (int enchant = 1; enchant <= L2Armor.MAX_ENCHANT_SKILL; enchant++)
					{
						L2Skill enchantSkill = ((L2Armor) it).getEnchantSkill(enchant);
						if (enchantSkill != null && item.getEnchantLevel() == enchant)
						{
							// add skills bestowed from +X armor
							activeChar.addSkill(enchantSkill, false);
							activeChar.sendSkillList();
						}
					}
				}
			}
			else
			{
				if (scrollTemplate.isSafe())
				{
					// safe enchant - remain old value
					// need retail message
					activeChar.sendPacket(new EnchantResult(5, 0, 0, item.getEnchantLevel()));

					if (Config.LOG_ITEM_ENCHANTS)
					{
						Connection con = null;
						try
						{
							con = L2DatabaseFactory.getInstance().getConnection();
							PreparedStatement statement = con.prepareStatement(
									"INSERT INTO log_enchants(player_id, item_id, item_object_id, scroll, support_id, chance, time) VALUES(?,?,?,?,?,?,?)");
							statement.setInt(1, activeChar.getObjectId());
							statement.setInt(2, item.getItemId());
							statement.setInt(3, item.getObjectId());
							statement.setInt(4, scroll.getItemId());
							statement.setInt(5, support == null ? 0 : support.getObjectId());
							statement.setInt(6, (int) chance);
							statement.setLong(7, System.currentTimeMillis());
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
				}
				else
				{
					// unequip item on enchant failure to avoid item skills stack
					if (item.isEquipped())
					{
						if (item.getEnchantLevel() > 0)
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
							sm.addNumber(item.getEnchantLevel());
							sm.addItemName(item);
							activeChar.sendPacket(sm);
						}
						else
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
							sm.addItemName(item);
							activeChar.sendPacket(sm);
						}

						L2ItemInstance[] unequiped =
								activeChar.getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
						InventoryUpdate iu = new InventoryUpdate();
						for (L2ItemInstance itm : unequiped)
						{
							iu.addModifiedItem(itm);
						}

						activeChar.sendPacket(iu);
						activeChar.broadcastUserInfo();
					}

					if (scrollTemplate.isBlessed())
					{
						// blessed enchant - clear enchant value
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BLESSED_ENCHANT_FAILED));

						item.setEnchantLevel(0);
						item.updateDatabase();
						activeChar.sendPacket(new EnchantResult(3, 0, 0, 0));

						if (Config.LOG_ITEM_ENCHANTS)
						{
							Connection con = null;
							try
							{
								con = L2DatabaseFactory.getInstance().getConnection();
								PreparedStatement statement = con.prepareStatement(
										"INSERT INTO log_enchants(player_id, item_id, item_object_id, scroll, support_id, chance, time) VALUES(?,?,?,?,?,?,?)");
								statement.setInt(1, activeChar.getObjectId());
								statement.setInt(2, item.getItemId());
								statement.setInt(3, item.getObjectId());
								statement.setInt(4, scroll.getItemId());
								statement.setInt(5, support == null ? 0 : support.getObjectId());
								statement.setInt(6, (int) chance);
								statement.setLong(7, System.currentTimeMillis());
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
					}
					else
					{
						// enchant failed, destroy item
						int crystalId = item.getItem().getCrystalItemId();
						int count = item.getCrystalCount() - (item.getItem().getCrystalCount() + 1) / 2;
						if (count < 1)
						{
							count = 1;
						}

						L2ItemInstance destroyItem =
								activeChar.getInventory().destroyItem("Enchant", item, activeChar, null);
						if (destroyItem == null)
						{
							// unable to destroy item, cheater ?
							Util.handleIllegalPlayerAction(activeChar,
									"Unable to delete item on enchant failure from player " + activeChar.getName() +
											", possible cheater !", Config.DEFAULT_PUNISH);
							activeChar.setActiveEnchantItem(null);
							activeChar.sendPacket(new EnchantResult(2, 0, 0, 0));

							if (Config.LOG_ITEM_ENCHANTS)
							{
								Connection con = null;
								try
								{
									con = L2DatabaseFactory.getInstance().getConnection();
									PreparedStatement statement = con.prepareStatement(
											"INSERT INTO log_enchants(player_id, item_id, item_object_id, scroll, support_id, chance, time) VALUES(?,?,?,?,?,?,?)");
									statement.setInt(1, activeChar.getObjectId());
									statement.setInt(2, item.getItemId());
									statement.setInt(3, item.getObjectId());
									statement.setInt(4, scroll.getItemId());
									statement.setInt(5, support == null ? 0 : support.getObjectId());
									statement.setInt(6, (int) chance);
									statement.setLong(7, System.currentTimeMillis());
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
							return;
						}

						L2ItemInstance crystals = null;
						if (crystalId != 0)
						{
							crystals = activeChar.getInventory()
									.addItem("Enchant", crystalId, count, activeChar, destroyItem);

							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
							sm.addItemName(crystals);
							sm.addItemNumber(count);
							activeChar.sendPacket(sm);
						}

						if (!Config.FORCE_INVENTORY_UPDATE)
						{
							InventoryUpdate iu = new InventoryUpdate();
							if (destroyItem.getCount() == 0)
							{
								iu.addRemovedItem(destroyItem);
							}
							else
							{
								iu.addModifiedItem(destroyItem);
							}

							if (crystals != null)
							{
								iu.addItem(crystals);
							}

							activeChar.sendPacket(iu);
						}
						else
						{
							activeChar.sendPacket(new ItemList(activeChar, true));
						}

						L2World world = L2World.getInstance();
						world.removeObject(destroyItem);
						if (crystalId == 0)
						{
							activeChar.sendPacket(new EnchantResult(4, 0, 0, 0));
						}
						else
						{
							activeChar.sendPacket(new EnchantResult(1, crystalId, count, 0));
						}

						if (Config.LOG_ITEM_ENCHANTS)
						{
							Connection con = null;
							try
							{
								con = L2DatabaseFactory.getInstance().getConnection();
								PreparedStatement statement = con.prepareStatement(
										"INSERT INTO log_enchants(player_id, item_id, item_object_id, scroll, support_id, chance, time) VALUES(?,?,?,?,?,?,?)");
								statement.setInt(1, activeChar.getObjectId());
								statement.setInt(2, item.getItemId());
								statement.setInt(3, item.getObjectId());
								statement.setInt(4, scroll.getItemId());
								statement.setInt(5, support == null ? 0 : support.getObjectId());
								statement.setInt(6, (int) chance);
								statement.setLong(7, System.currentTimeMillis());
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
					}
				}
			}

			StatusUpdate su = new StatusUpdate(activeChar);
			su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
			activeChar.sendPacket(su);

			activeChar.sendPacket(new ItemList(activeChar, false));
			activeChar.broadcastUserInfo();

			activeChar.setActiveEnchantSupportItem(null);
			activeChar.setActiveEnchantTimestamp(0);
			activeChar.setIsEnchanting(false);
		}
	}
}
