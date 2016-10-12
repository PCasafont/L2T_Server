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

import l2server.L2DatabaseFactory;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2ItemInstance.ItemLocation;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

/**
 * @author DS
 */
public class Mail extends ItemContainer
{
	private final int _ownerId;
	private int _messageId;

	public Mail(int objectId, int messageId)
	{
		_ownerId = objectId;
		_messageId = messageId;
	}

	@Override
	public String getName()
	{
		return "Mail";
	}

	@Override
	public L2PcInstance getOwner()
	{
		return null;
	}

	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.MAIL;
	}

	public int getMessageId()
	{
		return _messageId;
	}

	public void setNewMessageId(int messageId)
	{
		_messageId = messageId;
		for (L2ItemInstance item : _items.values())
		{
			if (item == null)
			{
				continue;
			}

			item.setLocation(getBaseLocation(), messageId);
		}

		updateDatabase();
	}

	public void returnToWh(ItemContainer wh)
	{
		for (L2ItemInstance item : _items.values())
		{
			if (item == null)
			{
				continue;
			}
			if (wh == null)
			{
				item.setLocation(ItemLocation.WAREHOUSE);
			}
			else
			{
				transferItem("Expire", item.getObjectId(), item.getCount(), wh, null, null);
			}
		}
	}

	@Override
	protected void addItem(L2ItemInstance item)
	{
		super.addItem(item);
		item.setLocation(getBaseLocation(), _messageId);
	}

	/*
	 * Allow saving of the items without owner
	 */
	@Override
	public void updateDatabase()
	{
		for (L2ItemInstance item : _items.values())
		{
			if (item != null)
			{
				item.updateDatabase(true);
			}
		}
	}

	@Override
	public void restore()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(
					"SELECT object_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, mana_left, time, appearance, mob_id FROM items WHERE owner_id=? AND loc=? AND loc_data=?");
			statement.setInt(1, getOwnerId());
			statement.setString(2, getBaseLocation().name());
			statement.setInt(3, getMessageId());
			ResultSet inv = statement.executeQuery();

			L2ItemInstance item;
			while (inv.next())
			{
				item = L2ItemInstance.restoreFromDb(getOwnerId(), inv);
				if (item == null)
				{
					continue;
				}

				L2World.getInstance().storeObject(item);

				// If stackable item is found just add to current quantity
				if (item.isStackable() && getItemByItemId(item.getItemId()) != null)
				{
					addItem("Restore", item, null, null);
				}
				else
				{
					addItem(item);
				}
			}
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "could not restore container:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	@Override
	public int getOwnerId()
	{
		return _ownerId;
	}
}
