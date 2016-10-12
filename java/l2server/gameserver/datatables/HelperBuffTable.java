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
import l2server.gameserver.templates.L2HelperBuff;
import l2server.gameserver.templates.StatsSet;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the Newbie Helper Buff list
 * <p>
 * Author: Ayor
 */

public class HelperBuffTable
{
	/**
	 * The table containing all Buff of the Newbie Helper
	 */
	private List<L2HelperBuff> _helperBuff;

	/**
	 * The player level since Newbie Helper can give the fisrt buff <BR>
	 * Used to generate message : "Come back here when you have reached level ...")
	 */
	private int _magicClassLowestLevel = 100;
	private int _physicClassLowestLevel = 100;

	/**
	 * The player level above which Newbie Helper won't give any buff <BR>
	 * Used to generate message : "Only novice character of level ... or less can receive my support magic.")
	 */
	private int _magicClassHighestLevel = 1;
	private int _physicClassHighestLevel = 1;

	private int _servitorLowestLevel = 100;

	private int _servitorHighestLevel = 1;

	public static HelperBuffTable getInstance()
	{
		return SingletonHolder._instance;
	}

	/**
	 * Create and Load the Newbie Helper Buff list from SQL Table helper_buff_list
	 */
	private HelperBuffTable()
	{
		_helperBuff = new ArrayList<>();
		restoreHelperBuffData();
	}

	/**
	 * Read and Load the Newbie Helper Buff list from SQL Table helper_buff_list
	 */
	private void restoreHelperBuffData()
	{
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "helperBuffTable.xml");
		XmlDocument doc = new XmlDocument(file);

		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				for (XmlNode d : n.getChildren())
				{
					if (d.getName().equalsIgnoreCase("buff"))
					{
						StatsSet helperBuffDat = new StatsSet();
						helperBuffDat.set("skillID", d.getInt("skillId"));
						helperBuffDat.set("skillLevel", d.getInt("skillLevel"));
						int lowerLevel = d.getInt("lowerLevel");
						helperBuffDat.set("lowerLevel", lowerLevel);
						int upperLevel = d.getInt("upperLevel");
						helperBuffDat.set("upperLevel", upperLevel);
						boolean isMagicClass = d.getBool("isMagic");
						helperBuffDat.set("isMagicClass", isMagicClass);
						boolean forSummon = d.getBool("forSummon");
						helperBuffDat.set("forSummon", forSummon);

						if (!isMagicClass)
						{
							if (lowerLevel < _physicClassLowestLevel)
							{
								_physicClassLowestLevel = lowerLevel;
							}

							if (upperLevel > _physicClassHighestLevel)
							{
								_physicClassHighestLevel = upperLevel;
							}
						}
						else
						{
							if (lowerLevel < _magicClassLowestLevel)
							{
								_magicClassLowestLevel = lowerLevel;
							}

							if (upperLevel > _magicClassHighestLevel)
							{
								_magicClassHighestLevel = upperLevel;
							}
						}
						if (forSummon)
						{
							if (lowerLevel < _servitorLowestLevel)
							{
								_servitorLowestLevel = lowerLevel;
							}

							if (upperLevel > _servitorHighestLevel)
							{
								_servitorHighestLevel = upperLevel;
							}
						}
						L2HelperBuff template = new L2HelperBuff(helperBuffDat);
						_helperBuff.add(template);
					}
				}
			}
		}
		Log.info("HelperBuffTable: Loaded: " + _helperBuff.size() + " buffs!");
	}

	/**
	 * Return the Helper Buff List
	 */
	public List<L2HelperBuff> getHelperBuffTable()
	{
		return _helperBuff;
	}

	/**
	 * @return Returns the magicClassHighestLevel.
	 */
	public int getMagicClassHighestLevel()
	{
		return _magicClassHighestLevel;
	}

	/**
	 * @return Returns the magicClassLowestLevel.
	 */
	public int getMagicClassLowestLevel()
	{
		return _magicClassLowestLevel;
	}

	/**
	 * @return Returns the physicClassHighestLevel.
	 */
	public int getPhysicClassHighestLevel()
	{
		return _physicClassHighestLevel;
	}

	/**
	 * @return Returns the physicClassLowestLevel.
	 */
	public int getPhysicClassLowestLevel()
	{
		return _physicClassLowestLevel;
	}

	/**
	 * @return Returns the servitorLowestLevel.
	 */
	public int getServitorLowestLevel()
	{
		return _servitorLowestLevel;
	}

	/**
	 * @return Returns the servitorHighestLevel.
	 */
	public int getServitorHighestLevel()
	{
		return _servitorHighestLevel;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final HelperBuffTable _instance = new HelperBuffTable();
	}
}
