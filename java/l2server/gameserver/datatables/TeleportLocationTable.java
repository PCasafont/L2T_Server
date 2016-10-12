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
import l2server.gameserver.model.L2TeleportLocation;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;

/**
 * This class ...
 *
 * @version $Revision: 1.3.2.2.2.3 $ $Date: 2005/03/27 15:29:18 $
 */
public class TeleportLocationTable implements Reloadable
{

	private TIntObjectHashMap<L2TeleportLocation> _teleports;

	public static TeleportLocationTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private TeleportLocationTable()
	{
		reload();

		ReloadableManager.getInstance().register("teleports", this);
	}

	@Override
	public boolean reload()
	{
		_teleports = new TIntObjectHashMap<>();
		boolean success = true;

		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "teleports.xml");
		XmlDocument doc = new XmlDocument(file);

		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				for (XmlNode d : n.getChildren())
				{
					if (d.getName().equalsIgnoreCase("tele"))
					{
						L2TeleportLocation teleport = new L2TeleportLocation();

						teleport.setTeleId(d.getInt("id"));
						teleport.setDescription(d.getString("description"));
						teleport.setLocX(d.getInt("x"));
						teleport.setLocY(d.getInt("y"));
						teleport.setLocZ(d.getInt("z"));

						teleport.setPrice(d.getInt("price", 0));
						if (Config.isServer(Config.TENKAI_ESTHUS))
						{
							teleport.setPrice((int) Math.sqrt(d.getInt("price", 0)));
						}
						teleport.setIsForNoble(d.getBool("fornoble", false));
						teleport.setItemId(d.getInt("itemId", 57));

						_teleports.put(teleport.getTeleId(), teleport);
					}
				}
			}
		}

		Log.info("TeleportLocationTable: Loaded " + _teleports.size() + " Teleport Location Templates.");

		return success;
	}

	@Override
	public String getReloadMessage(boolean success)
	{
		if (success)
		{
			return "Teleport Locations have been reloaded";
		}
		return "There was an error while reloading Teleport Locations";
	}

	/**
	 * @return
	 */
	public L2TeleportLocation getTemplate(int id)
	{
		return _teleports.get(id);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final TeleportLocationTable _instance = new TeleportLocationTable();
	}
}
