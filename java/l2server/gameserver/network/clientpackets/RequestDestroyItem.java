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
import l2server.gameserver.datatables.PetDataTable;
import l2server.gameserver.instancemanager.CursedWeaponsManager;
import l2server.gameserver.model.L2CrystallizeReward;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.InventoryUpdate;
import l2server.gameserver.network.serverpackets.ItemList;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;

/**
 * This class ...
 *
 * @version $Revision: 1.7.2.4.2.6 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestDestroyItem extends L2GameClientPacket
{

	private int _objectId;
	private long _count;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readQ();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		if (_count <= 0)
		{
			if (_count < 0)
			{
				Util.handleIllegalPlayerAction(activeChar,
						"[RequestDestroyItem] Character " + activeChar.getName() + " of account " +
								activeChar.getAccountName() + " tried to destroy item with oid " + _objectId +
								" but has count < 0!", Config.DEFAULT_PUNISH);
			}
			return;
		}

		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("destroy"))
		{
			activeChar.sendMessage("You destroying items too fast.");
			return;
		}

		if (activeChar.isInJail())
		{
			return;
		}

		long count = _count;

		if (activeChar.isProcessingTransaction() || activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			return;
		}

		L2ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(_objectId);
		// if we can't find the requested item, its actually a cheat
		if (itemToRemove == null)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
			return;
		}

		// Cannot discard item that the skill is consuming
		if (activeChar.isCastingNow())
		{
			if (activeChar.getCurrentSkill() != null &&
					activeChar.getCurrentSkill().getSkill().getItemConsumeId() == itemToRemove.getItemId())
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
				return;
			}
		}
		// Cannot discard item that the skill is consuming
		if (activeChar.isCastingSimultaneouslyNow())
		{
			if (activeChar.getLastSimultaneousSkillCast() != null &&
					activeChar.getLastSimultaneousSkillCast().getItemConsumeId() == itemToRemove.getItemId())
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
				return;
			}
		}

		int itemId = itemToRemove.getItemId();

		if (!activeChar.isGM() && !itemToRemove.isDestroyable() || CursedWeaponsManager.getInstance().isCursed(itemId))
		{
			if (itemToRemove.isHeroItem())
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.HERO_WEAPONS_CANT_DESTROYED));
			}
			else
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
			}
			return;
		}

		if (!itemToRemove.isStackable() && count > 1)
		{
			Util.handleIllegalPlayerAction(activeChar,
					"[RequestDestroyItem] Character " + activeChar.getName() + " of account " +
							activeChar.getAccountName() + " tried to destroy a non-stackable item with oid " +
							_objectId + " but has count > 1!", Config.DEFAULT_PUNISH);
			return;
		}

		if (!activeChar.getInventory().canManipulateWithItemId(itemToRemove.getItemId()))
		{
			activeChar.sendMessage("Cannot use this item.");
			return;
		}

		if (_count > itemToRemove.getCount())
		{
			count = itemToRemove.getCount();
		}

		if (itemToRemove.isEquipped())
		{
			L2ItemInstance[] unequiped =
					activeChar.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getLocationSlot());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance item : unequiped)
			{
				activeChar.checkSShotsMatch(null, item);

				iu.addModifiedItem(item);
			}
			activeChar.sendPacket(iu);
			activeChar.broadcastUserInfo();
		}

		if (PetDataTable.isPetItem(itemId))
		{
			Connection con = null;
			try
			{
				if (activeChar.getPet() != null && activeChar.getPet().getControlObjectId() == _objectId)
				{
					activeChar.getPet().unSummon(activeChar);
				}

				// if it's a pet control item, delete the pet
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
				statement.setInt(1, _objectId);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING, "could not delete pet objectid: ", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
		if (itemToRemove.isTimeLimitedItem())
		{
			itemToRemove.endOfLife();
		}

		// Crystallize the item instead of destroying it, if possible
		int skillLevel = activeChar.getSkillLevelHash(L2Skill.SKILL_CRYSTALLIZE);
		boolean hasBeenCrystallized = false;
		if (skillLevel > 0 && itemToRemove.getItem().isCrystallizable() && itemToRemove.getCrystalCount() > 0 &&
				!(itemToRemove.getItem().getCrystalType() == L2Item.CRYSTAL_NONE) && !itemToRemove.isEquipped())
		{
			// Check if the char can crystallize items and return if false;
			boolean canCrystallize = true;

			switch (itemToRemove.getItem().getItemGradePlain())
			{
				case L2Item.CRYSTAL_C:
				{
					if (skillLevel <= 1)
					{
						canCrystallize = false;
					}
					break;
				}
				case L2Item.CRYSTAL_B:
				{
					if (skillLevel <= 2)
					{
						canCrystallize = false;
					}
					break;
				}
				case L2Item.CRYSTAL_A:
				{
					if (skillLevel <= 3)
					{
						canCrystallize = false;
					}
					break;
				}
				case L2Item.CRYSTAL_S:
				{
					if (skillLevel <= 4)
					{
						canCrystallize = false;
					}
					break;
				}
			}

			if (canCrystallize)
			{
				// remove from inventory
				L2ItemInstance removedItem =
						activeChar.getInventory().destroyItem("Crystalize", _objectId, _count, activeChar, null);
				if (removedItem == null)
				{
					return;
				}

				activeChar.setInCrystallize(true);

				InventoryUpdate iu = new InventoryUpdate();
				iu.addRemovedItem(removedItem);
				activeChar.sendPacket(iu);

				// add crystals
				int crystalId = itemToRemove.getItem().getCrystalItemId();
				int crystalAmount = itemToRemove.getCrystalCount();
				L2ItemInstance createditem = activeChar.getInventory()
						.addItem("Crystallize", crystalId, crystalAmount, activeChar, activeChar);

				SystemMessage sm;
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CRYSTALLIZED);
				sm.addItemName(removedItem);
				activeChar.sendPacket(sm);

				sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
				sm.addItemName(createditem);
				sm.addItemNumber(crystalAmount);
				activeChar.sendPacket(sm);

				if (Config.ENABLE_CRYSTALLIZE_REWARDS)
				{
					for (L2CrystallizeReward reward : itemToRemove.getItem().getCrystallizeRewards())
					{
						if (reward.getChance() * 1000 > Rnd.get(100000))
						{
							activeChar.addItem("Crystallize", reward.getItemId(), reward.getCount(), activeChar, true);
						}
					}
				}

				activeChar.broadcastUserInfo();

				L2World world = L2World.getInstance();
				world.removeObject(removedItem);

				hasBeenCrystallized = true;

				activeChar.setInCrystallize(false);
			}
		}

		if (hasBeenCrystallized)
		{
			return;
		}

		L2ItemInstance removedItem =
				activeChar.getInventory().destroyItem("Destroy", _objectId, count, activeChar, null);

		if (removedItem == null)
		{
			return;
		}

		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate iu = new InventoryUpdate();
			if (removedItem.getCount() == 0)
			{
				iu.addRemovedItem(removedItem);
			}
			else
			{
				iu.addModifiedItem(removedItem);
			}

			//client.getConnection().sendPacket(iu);
			activeChar.sendPacket(iu);
		}
		else
		{
			sendPacket(new ItemList(activeChar, true));
		}

		StatusUpdate su = new StatusUpdate(activeChar);
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);
	}
}
