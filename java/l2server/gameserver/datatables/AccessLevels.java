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

import gnu.trove.TIntObjectHashMap;
import l2server.Config;
import l2server.gameserver.model.L2AccessLevel;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;

/**
 * @author FBIagent<br>
 */
public class AccessLevels
{
	/* The logger<br> */

	/**
	 * Reserved master access level<br>
	 */
	public static final int _masterAccessLevelNum = Config.MASTERACCESS_LEVEL;
	/**
	 * The master access level which can use everything<br>
	 */
	public static L2AccessLevel _masterAccessLevel =
			new L2AccessLevel(_masterAccessLevelNum, "Master Access", Config.MASTERACCESS_NAME_COLOR,
					Config.MASTERACCESS_TITLE_COLOR, null, true, true, true, true, true, true, true, true);
	/**
	 * Reserved user access level<br>
	 */
	public static final int _userAccessLevelNum = 0;
	/**
	 * The user access level which can do no administrative tasks<br>
	 */
	public static L2AccessLevel _userAccessLevel =
			new L2AccessLevel(_userAccessLevelNum, "User", -1, -1, null, false, false, false, true, false, true, true,
					true);
	/**
	 * HashMap of access levels defined in database<br>
	 */
	private final TIntObjectHashMap<L2AccessLevel> _accessLevels = new TIntObjectHashMap<>();

	/**
	 * Returns the one and only instance of this class<br><br>
	 *
	 * @return AccessLevels: the one and only instance of this class<br>
	 */
	public static AccessLevels getInstance()
	{
		return SingletonHolder._instance;
	}

	private AccessLevels()
	{
		loadAccessLevels();
		_accessLevels.put(_userAccessLevelNum, _userAccessLevel);
	}

	/**
	 * Loads the access levels from database<br>
	 */
	private void loadAccessLevels()
	{
		_accessLevels.clear();

		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "accessLevels.xml");
		XmlDocument doc = new XmlDocument(file);
		for (XmlNode n : doc.getFirstChild().getChildren())
		{
			if (n.getName().equalsIgnoreCase("accessLevel"))
			{
				int accessLevel = n.getInt("id");
				String name = n.getString("name");
				if (accessLevel == _userAccessLevelNum)
				{
					Log.warning(
							"AccessLevels: Access level with name " + name + " is using reserved user access level " +
									_userAccessLevelNum + ". Ignoring it!");
					continue;
				}
				else if (accessLevel == _masterAccessLevelNum)
				{
					Log.warning(
							"AccessLevels: Access level with name " + name + " is using reserved master access level " +
									_masterAccessLevelNum + ". Ignoring it!");
					continue;
				}
				else if (accessLevel < 0)
				{
					Log.warning("AccessLevels: Access level with name " + name +
							" is using banned access level state(below 0). Ignoring it!");
					continue;
				}

				int nameColor = 0;
				try
				{
					nameColor = Integer.decode("0x" + n.getString("nameColor"));
				}
				catch (NumberFormatException nfe)
				{
					nfe.printStackTrace();
				}

				int titleColor = 0;
				try
				{
					titleColor = Integer.decode("0x" + n.getString("titleColor"));
				}
				catch (NumberFormatException nfe)
				{
					nfe.printStackTrace();
				}

				String childs = n.getString("childAccess");
				boolean isGm = n.getBool("isGm");
				boolean allowPeaceAttack = n.getBool("allowPeaceAttack");
				boolean allowFixedRes = n.getBool("allowFixedRes");
				boolean allowTransaction = n.getBool("allowTransaction");
				boolean allowAltG = n.getBool("allowAltG");
				boolean giveDamage = n.getBool("giveDamage");
				boolean takeAggro = n.getBool("takeAggro");
				boolean gainExp = n.getBool("gainExp");

				_accessLevels.put(accessLevel,
						new L2AccessLevel(accessLevel, name, nameColor, titleColor, childs.isEmpty() ? null : childs,
								isGm, allowPeaceAttack, allowFixedRes, allowTransaction, allowAltG, giveDamage,
								takeAggro, gainExp));
			}
		}

		Log.info("AccessLevels: Loaded " + _accessLevels.size() + " access levels.");
	}

	/**
	 * Returns the access level by characterAccessLevel<br><br>
	 *
	 * @param accessLevelNum as int<br><br>
	 * @return AccessLevel: AccessLevel instance by char access level<br>
	 */
	public L2AccessLevel getAccessLevel(int accessLevelNum)
	{
		L2AccessLevel accessLevel = null;

		synchronized (_accessLevels)
		{
			accessLevel = _accessLevels.get(accessLevelNum);
		}
		return accessLevel;
	}

	public void addBanAccessLevel(int accessLevel)
	{
		synchronized (_accessLevels)
		{
			if (accessLevel > -1)
			{
				return;
			}

			_accessLevels.put(accessLevel,
					new L2AccessLevel(accessLevel, "Banned", -1, -1, null, false, false, false, false, false, false,
							false, false));
		}
	}

	public void reload()
	{
		loadAccessLevels();
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final AccessLevels _instance = new AccessLevels();
	}
}
