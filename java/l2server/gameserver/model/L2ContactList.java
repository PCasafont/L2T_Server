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

package l2server.gameserver.model;

import l2server.L2DatabaseFactory;
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

/**
 * @author UnAfraid & mrTJO
 *         TODO: System Messages:
 *         ADD:
 *         3223: The previous name is being registered. Please try again later.
 *         END OF ADD
 *         DEL
 *         3219: $s1 was successfully deleted from your Contact List.
 *         3217: The name is not currently registered.
 *         END OF DEL
 */
public class L2ContactList
{
	private final L2PcInstance activeChar;
	private final List<String> _contacts;

	private final String QUERY_ADD = "INSERT INTO character_contacts (charId, contactId) VALUES (?, ?)";
	private final String QUERY_REMOVE = "DELETE FROM character_contacts WHERE charId = ? and contactId = ?";
	private final String QUERY_LOAD = "SELECT contactId FROM character_contacts WHERE charId = ?";

	public L2ContactList(L2PcInstance player)
	{
		activeChar = player;
		_contacts = new CopyOnWriteArrayList<>();
		restore();
	}

	public void restore()
	{
		_contacts.clear();

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(QUERY_LOAD);
			statement.setInt(1, activeChar.getObjectId());
			ResultSet rset = statement.executeQuery();

			int contactId;
			String contactName;
			while (rset.next())
			{
				contactId = rset.getInt(1);
				contactName = CharNameTable.getInstance().getNameById(contactId);
				if (contactName == null || Objects.equals(contactName, activeChar.getName()) ||
						contactId == activeChar.getObjectId())
				{
					continue;
				}

				_contacts.add(contactName);
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Error found in " + activeChar.getName() + "'s ContactsList: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public boolean add(String name)
	{
		SystemMessage sm;

		int contactId = CharNameTable.getInstance().getIdByName(name);
		if (_contacts.contains(name))
		{
			activeChar.sendPacket(SystemMessageId.NAME_ALREADY_EXIST_ON_CONTACT_LIST);
			return false;
		}
		else if (Objects.equals(activeChar.getName(), name))
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_ADD_YOUR_NAME_ON_CONTACT_LIST);
			return false;
		}
		else if (_contacts.size() >= 100)
		{
			activeChar.sendPacket(SystemMessageId.CONTACT_LIST_LIMIT_REACHED);
			return false;
		}
		else if (contactId < 1)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.NAME_S1_NOT_EXIST_TRY_ANOTHER_NAME);
			sm.addString(name);
			activeChar.sendPacket(sm);
			return false;
		}
		else
		{
			for (String contactName : _contacts)
			{
				if (contactName.equalsIgnoreCase(name))
				{
					activeChar.sendPacket(SystemMessageId.NAME_ALREADY_EXIST_ON_CONTACT_LIST);
					return false;
				}
			}
		}

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(QUERY_ADD);
			statement.setInt(1, activeChar.getObjectId());
			statement.setInt(2, contactId);
			statement.execute();
			statement.close();

			_contacts.add(name);

			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SUCCESSFULLY_ADDED_TO_CONTACT_LIST);
			sm.addString(name);
			activeChar.sendPacket(sm);
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Error found in " + activeChar.getName() + "'s ContactsList: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return true;
	}

	public void remove(String name)
	{
		int contactId = CharNameTable.getInstance().getIdByName(name);

		if (!_contacts.contains(name))
		{
			activeChar.sendPacket(SystemMessageId.NAME_NOT_REGISTERED_ON_CONTACT_LIST);
			return;
		}

		else if (contactId < 1)
		{
			//TODO: Message?
			return;
		}

		_contacts.remove(name);

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(QUERY_REMOVE);
			statement.setInt(1, activeChar.getObjectId());
			statement.setInt(2, contactId);
			statement.execute();
			statement.close();
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SUCCESFULLY_DELETED_FROM_CONTACT_LIST);
			sm.addString(name);
			activeChar.sendPacket(sm);
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Error found in " + activeChar.getName() + "'s ContactsList: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public List<String> getAllContacts()
	{
		return _contacts;
	}
}
