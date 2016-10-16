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
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.InventoryUpdate;
import l2server.gameserver.network.serverpackets.ItemList;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.item.L2EtcItemType;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.util.GMAudit;
import l2server.gameserver.util.Util;
import l2server.log.Log;

/**
 * This class ...
 *
 * @version $Revision: 1.11.2.1.2.7 $ $Date: 2005/04/02 21:25:21 $
 */
public final class RequestDropItem extends L2GameClientPacket
{

	private int objectId;
	private long count;
	private int x;
	private int y;
	private int z;

	@Override
	protected void readImpl()
	{
		objectId = readD();
		count = readQ();
		x = readD();
		y = readD();
		z = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null || activeChar.isDead())
		{
			return;
		}
		// Flood protect drop to avoid packet lag
		if (!getClient().getFloodProtectors().getDropItem().tryPerformAction("drop item"))
		{
			return;
		}

		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(objectId);

		if (item == null || count == 0 || !activeChar.validateItemManipulation(objectId, "drop") ||
				!Config.ALLOW_DISCARDITEM && !activeChar.isGM() ||
				!item.isDropable() && !(activeChar.isGM() && Config.GM_TRADE_RESTRICTED_ITEMS) ||
				item.getItemType() == L2EtcItemType.PET_COLLAR && activeChar.havePetInvItems() ||
				activeChar.isInsideZone(L2Character.ZONE_NOITEMDROP))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
			return;
		}

		if (item.isQuestItem() && !(activeChar.isGM() && Config.GM_TRADE_RESTRICTED_ITEMS))
		{
			return;
		}

		if (count > item.getCount())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
			return;
		}

		if (Config.PLAYER_SPAWN_PROTECTION > 0 && activeChar.isInvul() && !activeChar.isGM())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
			return;
		}

		if (count < 0)
		{
			Util.handleIllegalPlayerAction(activeChar,
					"[RequestDropItem] Character " + activeChar.getName() + " of account " +
							activeChar.getAccountName() + " tried to drop item with oid " + objectId +
							" but has count < 0!", Config.DEFAULT_PUNISH);
			return;
		}

		if (!item.isStackable() && count > 1)
		{
			Util.handleIllegalPlayerAction(activeChar,
					"[RequestDropItem] Character " + activeChar.getName() + " of account " +
							activeChar.getAccountName() + " tried to drop non-stackable item with oid " + objectId +
							" but has count > 1!", Config.DEFAULT_PUNISH);
			return;
		}

		if (Config.JAIL_DISABLE_TRANSACTION && activeChar.isInJail())
		{
			activeChar.sendMessage("You cannot drop items in Jail.");
			return;
		}

		if (!activeChar.getAccessLevel().allowTransaction())
		{
			activeChar.sendMessage("Transactions are disable for your Access Level");
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOTHING_HAPPENED));
			return;
		}

		if (activeChar.isProcessingTransaction() || activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			return;
		}
		if (activeChar.isFishing())
		{
			//You can't mount, dismount, break and drop items while fishing
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DO_WHILE_FISHING_2));
			return;
		}
		if (activeChar.isFlying())
		{
			return;
		}

		// Cannot discard item that the skill is consuming
		if (activeChar.isCastingNow())
		{
			if (activeChar.getCurrentSkill() != null &&
					activeChar.getCurrentSkill().getSkill().getItemConsumeId() == item.getItemId())
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
				return;
			}
		}

		// Cannot discard item that the skill is consuming
		if (activeChar.isCastingSimultaneouslyNow())
		{
			if (activeChar.getLastSimultaneousSkillCast() != null &&
					activeChar.getLastSimultaneousSkillCast().getItemConsumeId() == item.getItemId())
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
				return;
			}
		}

		if (L2Item.TYPE2_QUEST == item.getItem().getType2() && !activeChar.isGM())
		{
			if (Config.DEBUG)
			{
				Log.finest(activeChar.getObjectId() + ":player tried to drop quest item");
			}
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISCARD_EXCHANGE_ITEM));
			return;
		}

		if (!activeChar.isInsideRadius(x, y, 150, false) || Math.abs(z - activeChar.getZ()) > 50)
		{
			if (Config.DEBUG)
			{
				Log.finest(activeChar.getObjectId() + ": trying to drop too far away");
			}
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISCARD_DISTANCE_TOO_FAR));
			return;
		}

		if (!activeChar.getInventory().canManipulateWithItemId(item.getItemId()))
		{
			activeChar.sendMessage("Cannot use this item.");
			return;
		}

		if (Config.DEBUG)
		{
			Log.fine("requested drop item " + objectId + " (" + item.getCount() + ") at " + x + "/" + y + "/" +
					z);
		}

		if (item.isEquipped())
		{
			L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance itm : unequiped)
			{
				activeChar.checkSShotsMatch(null, itm);

				iu.addModifiedItem(itm);
			}
			activeChar.sendPacket(iu);
			activeChar.broadcastUserInfo();

			ItemList il = new ItemList(activeChar, true);
			activeChar.sendPacket(il);
		}

		L2ItemInstance dropedItem = activeChar.dropItem("Drop", objectId, count, x, y, z, null, false);

		if (Config.DEBUG)
		{
			Log.fine("dropping " + objectId + " item(" + count + ") at: " + x + " " + y + " " + z);
		}

		// activeChar.broadcastUserInfo();

		if (activeChar.isGM())
		{
			String target = activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target";
			GMAudit.auditGMAction(activeChar.getName(), "Drop", target,
					"(id: " + dropedItem.getItemId() + " name: " + dropedItem.getItemName() + " objId: " +
							dropedItem.getObjectId() + " x: " + activeChar.getX() + " y: " + activeChar.getY() +
							" z: " + activeChar.getZ() + ")");
		}

		/*if (dropedItem != null && dropedItem.getItemId() == 57 && dropedItem.getCount() >= 1000000)
		{
			String msg = "Character (" + activeChar.getName() + ") has dropped (" + dropedItem.getCount() + ")adena at (" + this.x + "," + this.y + "," + this.z + ")";
			Log.warning(msg);
			GmListTable.broadcastMessageToGMs(msg);
		}*/
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
