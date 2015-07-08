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
package l2tserver.gameserver.datatables;

import gnu.trove.TIntObjectHashMap;

import java.io.File;

import l2tserver.Config;
import l2tserver.gameserver.Reloadable;
import l2tserver.gameserver.ReloadableManager;
import l2tserver.gameserver.model.L2TeleportLocation;
import l2tserver.log.Log;
import l2tserver.util.xml.XmlDocument;
import l2tserver.util.xml.XmlNode;

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
	
	public boolean reload()
	{
		_teleports = new TIntObjectHashMap<L2TeleportLocation>();
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
	
	public String getReloadMessage(boolean success)
	{
		if (success)
			return "Teleport Locations have been reloaded";
		return "There was an error while reloading Teleport Locations";
	}
	
	/**
	 * @param template id
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
