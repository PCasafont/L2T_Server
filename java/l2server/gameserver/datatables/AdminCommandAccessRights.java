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

import l2server.Config;
import l2server.gameserver.model.L2AccessLevel;
import l2server.gameserver.model.L2AdminCommandAccessRight;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author FBIagent<br>
 */
public class AdminCommandAccessRights
{
	/**
	 * The logger<br>
	 */

	private Map<String, L2AdminCommandAccessRight> _adminCommandAccessRights;

	/**
	 * Returns the one and only instance of this class<br><br>
	 *
	 * @return AdminCommandAccessRights: the one and only instance of this class<br>
	 */
	public static AdminCommandAccessRights getInstance()
	{
		return SingletonHolder._instance;
	}

	/**
	 * The access rights<br>
	 */
	private AdminCommandAccessRights()
	{
		loadAdminCommandAccessRights();
	}

	/**
	 * Loads admin command access rights from database<br>
	 */
	private void loadAdminCommandAccessRights()
	{
		_adminCommandAccessRights = new HashMap<>();
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "adminCommands.xml");

		XmlDocument doc = new XmlDocument(file);
		doc.getChildren().stream().filter(n -> n.getName().equalsIgnoreCase("list")).forEachOrdered(
				n -> n.getChildren().stream().filter(d -> d.getName().equalsIgnoreCase("command")).forEachOrdered(d ->
				{
					String adminCommand = d.getString("name");
					String accessLevels = d.getString("accessLevels");
					boolean confirm = d.getBool("configmDlg", false);
					_adminCommandAccessRights
							.put(adminCommand, new L2AdminCommandAccessRight(adminCommand, accessLevels, confirm));
				}));

		Log.info("AdminCommandAccessRights: Loaded " + _adminCommandAccessRights.size() + " from xml.");
	}

	public boolean hasAccess(String adminCommand, L2AccessLevel accessLevel)
	{
		if (accessLevel.getLevel() == AccessLevels._masterAccessLevelNum)
		{
			return true;
		}

		L2AdminCommandAccessRight acar = _adminCommandAccessRights.get(adminCommand);

		if (acar == null)
		{
			Log.info("AdminCommandAccessRights: No rights defined for admin command " + adminCommand + ".");
			return false;
		}

		return acar.hasAccess(accessLevel);
	}

	public boolean requireConfirm(String command)
	{
		L2AdminCommandAccessRight acar = _adminCommandAccessRights.get(command);
		if (acar == null)
		{
			Log.info("AdminCommandAccessRights: No rights defined for admin command " + command + ".");
			return false;
		}
		return _adminCommandAccessRights.get(command).getRequireConfirm();
	}

	public void reloadAdminCommandAccessRights()
	{
		loadAdminCommandAccessRights();
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final AdminCommandAccessRights _instance = new AdminCommandAccessRights();
	}
}
