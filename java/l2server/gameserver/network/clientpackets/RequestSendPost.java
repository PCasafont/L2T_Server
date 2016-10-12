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
import l2server.gameserver.datatables.AccessLevels;
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.instancemanager.MailManager;
import l2server.gameserver.model.BlockList;
import l2server.gameserver.model.L2AccessLevel;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Message;
import l2server.gameserver.model.itemcontainer.Mail;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.StringUtil;

import static l2server.gameserver.model.actor.L2Character.ZONE_PEACE;
import static l2server.gameserver.model.itemcontainer.PcInventory.ADENA_ID;
import static l2server.gameserver.model.itemcontainer.PcInventory.MAX_ADENA;

/**
 * @author Pere, DS
 */
public final class RequestSendPost extends L2GameClientPacket
{

	private static final int BATCH_LENGTH = 12; // length of the one item

	private static final int MAX_RECV_LENGTH = 16;
	private static final int MAX_SUBJ_LENGTH = 128;
	private static final int MAX_TEXT_LENGTH = 512;
	private static final int MAX_ATTACHMENTS = 8;
	private static final int INBOX_SIZE = 240;
	private static final int OUTBOX_SIZE = 240;

	private static final int MESSAGE_FEE = 100;
	private static final int MESSAGE_FEE_PER_SLOT = 1000; // 100 adena message fee + 1000 per each item slot

	private String _receiver;
	private boolean _isCod;
	private String _subject;
	private String _text;
	private AttachmentItem _items[] = null;
	private long _reqAdena;

	public RequestSendPost()
	{
	}

	@Override
	protected void readImpl()
	{
		_receiver = readS();
		_isCod = readD() != 0;
		_subject = readS();
		_text = readS();

		int attachCount = readD();
		if (attachCount < 0 || attachCount > Config.MAX_ITEM_IN_PACKET ||
				attachCount * BATCH_LENGTH + 8 != _buf.remaining())
		{
			return;
		}

		if (attachCount > 0)
		{
			_items = new AttachmentItem[attachCount];
			for (int i = 0; i < attachCount; i++)
			{
				int objectId = readD();
				long count = readQ();
				if (objectId < 1 || count < 0)
				{
					_items = null;
					return;
				}
				_items[i] = new AttachmentItem(objectId, count);
			}
		}

		_reqAdena = readQ();
	}

	@Override
	public void runImpl()
	{
		if (!Config.ALLOW_MAIL)
		{
			return;
		}

		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		if (!Config.ALLOW_ATTACHMENTS)
		{
			_items = null;
			_isCod = false;
			_reqAdena = 0;
		}

		if (!activeChar.getAccessLevel().allowTransaction())
		{
			activeChar.sendMessage("Transactions are disable for your Access Level.");
			return;
		}

		if (!activeChar.isInsideZone(ZONE_PEACE) && _items != null)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_FORWARD_NOT_IN_PEACE_ZONE));
			return;
		}

		if (activeChar.getActiveTradeList() != null)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_FORWARD_DURING_EXCHANGE));
			return;
		}

		if (activeChar.isEnchanting())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_FORWARD_DURING_ENCHANT));
			return;
		}

		if (activeChar.getPrivateStoreType() > 0)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_FORWARD_PRIVATE_STORE));
			return;
		}

		if (_receiver.length() > MAX_RECV_LENGTH)
		{
			activeChar
					.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALLOWED_LENGTH_FOR_RECIPIENT_EXCEEDED));
			return;
		}

		if (_subject.length() > MAX_SUBJ_LENGTH)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALLOWED_LENGTH_FOR_TITLE_EXCEEDED));
			return;
		}

		if (_text.length() > MAX_TEXT_LENGTH)
		{
			// not found message for this
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALLOWED_LENGTH_FOR_TITLE_EXCEEDED));
			return;
		}

		if (_items != null && _items.length > MAX_ATTACHMENTS)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ITEM_SELECTION_POSSIBLE_UP_TO_8));
			return;
		}

		if (_reqAdena < 0 || _reqAdena > MAX_ADENA)
		{
			return;
		}

		if (_isCod)
		{
			if (_reqAdena == 0)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PAYMENT_AMOUNT_NOT_ENTERED));
				return;
			}
			if (_items == null || _items.length == 0)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PAYMENT_REQUEST_NO_ITEM));
				return;
			}
		}

		final int receiverId = CharNameTable.getInstance().getIdByName(_receiver);
		if (receiverId <= 0)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.RECIPIENT_NOT_EXIST));
			return;
		}

		if (receiverId == activeChar.getObjectId())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANT_SEND_MAIL_TO_YOURSELF));
			return;
		}

		L2AccessLevel accessLevel;
		final int level = CharNameTable.getInstance().getAccessLevelById(receiverId);
		if (level == AccessLevels._masterAccessLevelNum)
		{
			accessLevel = AccessLevels._masterAccessLevel;
		}
		else if (level == AccessLevels._userAccessLevelNum)
		{
			accessLevel = AccessLevels._userAccessLevel;
		}
		else
		{
			accessLevel = AccessLevels.getInstance().getAccessLevel(level);
			if (accessLevel == null)
			{
				accessLevel = AccessLevels._userAccessLevel;
			}
		}

		if (accessLevel.isGm() && !activeChar.getAccessLevel().isGm())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CANNOT_MAIL_GM_C1);
			sm.addString(_receiver);
			activeChar.sendPacket(sm);
			return;
		}

		if (activeChar.isInJail() && (Config.JAIL_DISABLE_TRANSACTION && _items != null || Config.JAIL_DISABLE_CHAT))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_FORWARD_NOT_IN_PEACE_ZONE));
			return;
		}

		if (BlockList.isInBlockList(receiverId, activeChar.getObjectId()))
		{
			activeChar.sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.C1_BLOCKED_YOU_CANNOT_MAIL).addString(_receiver));
			return;
		}

		if (MailManager.getInstance().getOutboxSize(activeChar.getObjectId()) >= OUTBOX_SIZE)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_FORWARD_MAIL_LIMIT_EXCEEDED));
			return;
		}

		if (MailManager.getInstance().getInboxSize(receiverId) >= INBOX_SIZE)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_FORWARD_MAIL_LIMIT_EXCEEDED));
			return;
		}

		if (!getClient().getFloodProtectors().getSendMail().tryPerformAction("sendmail"))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_FORWARD_LESS_THAN_MINUTE));
			return;
		}

		Message msg = new Message(activeChar.getObjectId(), receiverId, _isCod, _subject, _text, _reqAdena);

		Util.logToFile(activeChar.getName() + " is sending a Mail[" + msg.getId() + "] to " + _receiver + ".",
				"Logs/Mails/" + activeChar.getName() + "_Sent_Mails", "txt", true, true);

		if (removeItems(activeChar, msg))
		{
			MailManager.getInstance().sendMessage(msg);
			activeChar.sendPacket(ExNoticePostSent.valueOf(true));
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MAIL_SUCCESSFULLY_SENT));
		}
	}

	private boolean removeItems(L2PcInstance player, Message msg)
	{
		long currentAdena = player.getAdena();
		long fee = MESSAGE_FEE;

		if (_items != null)
		{
			for (AttachmentItem i : _items)
			{
				// Check validity of requested item
				L2ItemInstance item = player.checkItemManipulation(i.getObjectId(), i.getCount(), "attach");
				if (item == null || !item.isTradeable() || item.isEquipped())
				{
					Util.logToFile(
							"- Could not Attach " + (item == null ? i.getObjectId() : item.getName()) + ". Aborting.",
							"Logs/Mails/" + player.getName() + "_Sent_Mails", "txt", true, false);
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_FORWARD_BAD_ITEM));
					return false;
				}

				fee += MESSAGE_FEE_PER_SLOT;

				if (item.getItemId() == ADENA_ID)
				{
					currentAdena -= i.getCount();
				}
			}
		}

		// Check if enough adena and charge the fee
		if (currentAdena < fee || !player.reduceAdena("MailFee", fee, null, false))
		{
			Util.logToFile("- Couldn't take fees. Aborting.", "Logs/Mails/" + player.getName() + "_Sent_Mails", "txt",
					true, false);

			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_FORWARD_NO_ADENA));
			return false;
		}

		if (_items == null)
		{
			Util.logToFile("- Mail has no attachments. Sending.", "Logs/Mails/" + player.getName() + "_Sent_Mails",
					"txt", true, false);

			return true;
		}

		Mail attachments = msg.createAttachments();

		// message already has attachments ? oO
		if (attachments == null)
		{
			Util.logToFile("- Attachments were null. Aborting.", "Logs/Mails/" + player.getName() + "_Sent_Mails",
					"txt", true, false);

			return false;
		}

		final StringBuilder recv = new StringBuilder(32);
		StringUtil.append(recv, msg.getReceiverName(), "[", String.valueOf(msg.getReceiverId()), "]");
		final String receiver = recv.toString();

		// Proceed to the transfer
		InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (AttachmentItem i : _items)
		{
			// Check validity of requested item
			L2ItemInstance oldItem = player.checkItemManipulation(i.getObjectId(), i.getCount(), "attach");
			if (oldItem == null || !oldItem.isTradeable() || oldItem.isEquipped())
			{
				Log.warning("Error adding attachment for char " + player.getName() + " (olditem == null)");

				Util.logToFile(
						"- Could not delete old item " + (oldItem == null ? i.getObjectId() : oldItem.getName()) +
								". Aborting.", "Logs/Mails/" + player.getName() + "_Sent_Mails", "txt", true, false);

				return false;
			}

			final L2ItemInstance newItem = player.getInventory()
					.transferItem("send mail to " + receiver, i.getObjectId(), i.getCount(), attachments, player,
							receiver);
			if (newItem == null)
			{
				Log.warning("Error adding attachment for char " + player.getName() + " (newitem == null)");

				Util.logToFile("- Could not transfer " + oldItem.getName() + ". Aborting.",
						"Logs/Mails/" + player.getName() + "_Sent_Mails", "txt", true, false);

				continue;
			}

			newItem.setLocation(newItem.getLocation(), msg.getId());

			if (playerIU != null)
			{
				if (oldItem.getCount() > 0 && oldItem != newItem)
				{
					playerIU.addModifiedItem(oldItem);
				}
				else
				{
					playerIU.addRemovedItem(oldItem);
				}
			}

			Util.logToFile("- Attached " + newItem.getName() + ", Count = " + newItem.getCount() + ".",
					"Logs/Mails/" + player.getName() + "_Sent_Mails", "txt", true, false);
		}

		// Send updated item list to the player
		if (playerIU != null)
		{
			player.sendPacket(playerIU);
		}
		else
		{
			player.sendPacket(new ItemList(player, false));
		}

		// Update current load status on player
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);

		return true;
	}

	private static class AttachmentItem
	{
		private final int _objectId;
		private final long _count;

		public AttachmentItem(int id, long num)
		{
			_objectId = id;
			_count = num;
		}

		public int getObjectId()
		{
			return _objectId;
		}

		public long getCount()
		{
			return _count;
		}
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
