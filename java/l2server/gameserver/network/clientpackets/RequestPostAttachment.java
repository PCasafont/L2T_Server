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
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.instancemanager.MailManager;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2ItemInstance.ItemLocation;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Message;
import l2server.gameserver.model.itemcontainer.ItemContainer;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.util.Util;

import static l2server.gameserver.model.actor.L2Character.ZONE_PEACE;
import static l2server.gameserver.model.itemcontainer.PcInventory.ADENA_ID;

/**
 * @author Pere, DS
 */
public final class RequestPostAttachment extends L2GameClientPacket
{

	private int _msgId;

	@Override
	protected void readImpl()
	{
		_msgId = readD();
	}

	@Override
	public void runImpl()
	{
		if (!Config.ALLOW_MAIL || !Config.ALLOW_ATTACHMENTS)
		{
			return;
		}

		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("getattach"))
		{
			return;
		}

		if (!activeChar.getAccessLevel().allowTransaction())
		{
			activeChar.sendMessage("Transactions are disabled for your Access Level");
			return;
		}

		if (activeChar.getEvent() != null)
		{
			activeChar.sendMessage("You can't use this feature when involved in an event!");
			return;
		}

		if (activeChar.getOlympiadGameId() > -1)
		{
			activeChar.sendMessage("You can't use this feature when involved in the Grand Olympiad!");
			return;
		}

		if (!activeChar.isInsideZone(ZONE_PEACE))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_RECEIVE_NOT_IN_PEACE_ZONE));
			return;
		}

		if (activeChar.getActiveTradeList() != null)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_RECEIVE_DURING_EXCHANGE));
			return;
		}

		if (activeChar.isEnchanting())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_RECEIVE_DURING_ENCHANT));
			return;
		}

		if (activeChar.getPrivateStoreType() > 0)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_RECEIVE_PRIVATE_STORE));
			return;
		}

		final Message msg = MailManager.getInstance().getMessage(_msgId);
		if (msg == null)
		{
			return;
		}

		if (msg.getReceiverId() != activeChar.getObjectId())
		{
			Util.handleIllegalPlayerAction(activeChar,
					"Player " + activeChar.getName() + " tried to get not own attachment!", Config.DEFAULT_PUNISH);
			return;
		}

		if (!msg.hasAttachments())
		{
			return;
		}

		final ItemContainer attachments = msg.getAttachments();
		if (attachments == null)
		{
			return;
		}

		int weight = 0;
		int slots = 0;

		for (L2ItemInstance item : attachments.getItems())
		{
			if (item == null)
			{
				continue;
			}

			// Calculate needed slots
			if (msg.getSenderId() > -1 && item.getOwnerId() != msg.getSenderId())
			{
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() +
						" tried to get wrong item (ownerId != senderId) from attachment!", Config.DEFAULT_PUNISH);
				return;
			}

			if (!item.getLocation().equals(ItemLocation.MAIL))
			{
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() +
						" tried to get wrong item (Location != MAIL) from attachment!", Config.DEFAULT_PUNISH);
				return;
			}

			if (item.getLocationSlot() != msg.getId())
			{
				Util.handleIllegalPlayerAction(activeChar,
						"Player " + activeChar.getName() + " tried to get items from different attachment!",
						Config.DEFAULT_PUNISH);
				return;
			}

			weight += item.getCount() * item.getItem().getWeight();
			if (!item.isStackable())
			{
				slots += item.getCount();
			}
			else if (activeChar.getInventory().getItemByItemId(item.getItemId()) == null)
			{
				slots++;
			}
		}

		// Item Max Limit Check
		if (!activeChar.getInventory().validateCapacity(slots))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_RECEIVE_INVENTORY_FULL));
			return;
		}

		// Weight limit Check
		if (!activeChar.getInventory().validateWeight(weight))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_RECEIVE_INVENTORY_FULL));
			return;
		}

		long adena = msg.getReqAdena();
		if (adena > 0 && !activeChar.reduceAdena("PayMail", adena, null, true))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_RECEIVE_NO_ADENA));
			return;
		}

		Util.logToFile(activeChar.getName() + " is retrieving items from Mail[" + msg.getId() + "].",
				"Logs/Mails/" + activeChar.getName() + "_Retrieve_Mails", "txt", true, true);

		// Proceed to the transfer
		InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (L2ItemInstance item : attachments.getItems())
		{
			if (item == null)
			{
				continue;
			}

			if (msg.getSenderId() > -1 && item.getOwnerId() != msg.getSenderId())
			{
				Util.logToFile(
						"- " + activeChar.getName() + " could not retrieve " + item.getName() + " [" + item.getCount() +
								"] - MailSenderId[" + msg.getSenderId() + "] differs from ItemOwnerId[" +
								item.getOwnerId() + "].", "Logs/Mails/" + activeChar.getName() + "_Retrieve_Mails",
						"txt", true, false);

				Util.handleIllegalPlayerAction(activeChar,
						"Player " + activeChar.getName() + " tried to get items with owner != sender !",
						Config.DEFAULT_PUNISH);
				return;
			}

			long count = item.getCount();
			final L2ItemInstance newItem = attachments
					.transferItem(attachments.getName() + " from " + msg.getSenderName(), item.getObjectId(),
							item.getCount(), activeChar.getInventory(), activeChar, null);
			if (newItem == null)
			{
				Util.logToFile(
						"- " + activeChar.getName() + " could not retrieve " + item.getName() + " [" + item.getCount() +
								"] - Item was NULL after transfer.",
						"Logs/Mails/" + activeChar.getName() + "_Retrieve_Mails", "txt", true, false);
				return;
			}

			if (playerIU != null)
			{
				if (newItem.getCount() > count)
				{
					playerIU.addModifiedItem(newItem);
				}
				else
				{
					playerIU.addNewItem(newItem);
				}
			}
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_ACQUIRED_S2_S1);
			sm.addItemName(item.getItemId());
			sm.addItemNumber(count);
			activeChar.sendPacket(sm);

			Util.logToFile(
					"- " + activeChar.getName() + " retrieved " + item.getName() + "[" + newItem.getCount() + "].",
					"Logs/Mails/" + activeChar.getName() + "_Retrieve_Mails", "txt", true, false);
		}

		// Send updated item list to the player
		if (playerIU != null)
		{
			activeChar.sendPacket(playerIU);
		}
		else
		{
			activeChar.sendPacket(new ItemList(activeChar, false));
		}

		msg.removeAttachments();

		// Update current load status on player
		StatusUpdate su = new StatusUpdate(activeChar);
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);

		SystemMessage sm;
		final L2PcInstance sender = L2World.getInstance().getPlayer(msg.getSenderId());
		if (adena > 0)
		{
			if (sender != null)
			{
				sender.addAdena("PayMail", adena, activeChar, false);
				sm = SystemMessage.getSystemMessage(SystemMessageId.PAYMENT_OF_S1_ADENA_COMPLETED_BY_S2);
				sm.addItemNumber(adena);
				sm.addCharName(activeChar);
				sender.sendPacket(sm);
			}
			else
			{
				L2ItemInstance paidAdena =
						ItemTable.getInstance().createItem("PayMail", ADENA_ID, adena, activeChar, null);
				paidAdena.setOwnerId(msg.getSenderId());
				paidAdena.setLocation(ItemLocation.INVENTORY);
				paidAdena.updateDatabase(true);
				L2World.getInstance().removeObject(paidAdena);
			}
		}
		else if (sender != null)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ACQUIRED_ATTACHED_ITEM);
			sm.addCharName(activeChar);
			sender.sendPacket(sm);
		}

		activeChar.sendPacket(new ExChangePostState(true, _msgId, Message.READED));
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MAIL_SUCCESSFULLY_RECEIVED));
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
