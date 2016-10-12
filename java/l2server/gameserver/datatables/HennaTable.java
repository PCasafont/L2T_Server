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
import l2server.gameserver.Reloadable;
import l2server.gameserver.ReloadableManager;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.item.L2Henna;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;

/**
 * This class ...
 *
 * @version $Revision$ $Date$
 */
public class HennaTable implements Reloadable
{
	private TIntObjectHashMap<L2Henna> _henna;

	public static HennaTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private HennaTable()
	{
		_henna = new TIntObjectHashMap<>();
		if (!Config.IS_CLASSIC)
		{
			restoreHennaData();
			ReloadableManager.getInstance().register("henna", this);
		}
	}

	private void restoreHennaData()
	{
		if (Config.IS_CLASSIC)
		{
			return;
		}

		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "henna.xml");
		XmlDocument doc = new XmlDocument(file);

		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				for (XmlNode hennaNode : n.getChildren())
				{
					if (hennaNode.getName().equalsIgnoreCase("henna"))
					{
						StatsSet hennaDat = new StatsSet();
						int id = hennaNode.getInt("symbolId");

						hennaDat.set("symbolId", id);
						hennaDat.set("dyeId", hennaNode.getInt("dyeId"));
						hennaDat.set("name", hennaNode.getString("name"));
						if (hennaNode.hasAttribute("price"))
						{
							hennaDat.set("price", hennaNode.getLong("price"));
						}
						if (hennaNode.hasAttribute("STR"))
						{
							hennaDat.set("STR", hennaNode.getInt("STR"));
						}
						if (hennaNode.hasAttribute("CON"))
						{
							hennaDat.set("CON", hennaNode.getInt("CON"));
						}
						if (hennaNode.hasAttribute("DEX"))
						{
							hennaDat.set("DEX", hennaNode.getInt("DEX"));
						}
						if (hennaNode.hasAttribute("INT"))
						{
							hennaDat.set("INT", hennaNode.getInt("INT"));
						}
						if (hennaNode.hasAttribute("WIT"))
						{
							hennaDat.set("WIT", hennaNode.getInt("WIT"));
						}
						if (hennaNode.hasAttribute("MEN"))
						{
							hennaDat.set("MEN", hennaNode.getInt("MEN"));
						}
						if (hennaNode.hasAttribute("LUC"))
						{
							hennaDat.set("LUC", hennaNode.getInt("LUC"));
						}
						if (hennaNode.hasAttribute("CHA"))
						{
							hennaDat.set("CHA", hennaNode.getInt("CHA"));
						}
						if (hennaNode.hasAttribute("elemId"))
						{
							hennaDat.set("elemId", hennaNode.getInt("elemId"));
						}
						if (hennaNode.hasAttribute("elemVal"))
						{
							hennaDat.set("elemVal", hennaNode.getInt("elemVal"));
						}

						if (hennaNode.hasAttribute("time"))
						{
							hennaDat.set("time", hennaNode.getLong("time"));
						}
						if (hennaNode.hasAttribute("fourthSlot"))
						{
							hennaDat.set("fourthSlot", hennaNode.getBool("fourthSlot"));
						}
						if (hennaNode.hasAttribute("skills"))
						{
							hennaDat.set("skills", hennaNode.getString("skills"));
						}

						L2Henna henna = new L2Henna(hennaDat);

						for (XmlNode allowedClassNode : hennaNode.getChildren())
						{
							if (allowedClassNode.getName().equalsIgnoreCase("allowedClass"))
							{
								int classId = allowedClassNode.getInt("id");
								PlayerClassTable.getInstance().getClassById(classId).addAllowedDye(henna);
							}
						}

						_henna.put(id, henna);
					}
				}
			}
		}
		Log.info("HennaTable: Loaded " + _henna.size() + " Templates.");
	}

	public L2Henna getTemplate(int id)
	{
		return _henna.get(id);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final HennaTable _instance = new HennaTable();
	}

	@Override
	public boolean reload()
	{
		_henna.clear();
		restoreHennaData();
		return true;
	}

	@Override
	public String getReloadMessage(boolean success)
	{
		return "Henna reloaded";
	}
}
