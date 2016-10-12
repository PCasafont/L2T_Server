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
import l2server.gameserver.Reloadable;
import l2server.gameserver.ReloadableManager;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LasTravel
 */

public class ScenePlayerDataTable implements Reloadable
{

	private Map<Integer, Integer> _sceneDataTable;

	@Override
	public boolean reload()
	{
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "scenePlayerData.xml");
		XmlDocument doc = new XmlDocument(file);

		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				for (XmlNode d : n.getChildren())
				{
					if (d.getName().equalsIgnoreCase("scene"))
					{
						int id = d.getInt("id");

						int time = d.getInt("time");

						_sceneDataTable.put(id, time);
					}
				}

				Log.info("ScenePlayerTable: Loaded: " + _sceneDataTable.size() + " scenes!");
			}
		}

		return false;
	}

	public static ScenePlayerDataTable getInstance()
	{
		return SingletonHolder._instance;
	}

	public Map<Integer, Integer> getSceneTable()
	{
		return _sceneDataTable;
	}

	public int getVideoDuration(int vidId)
	{
		return _sceneDataTable.get(vidId);
	}

	private ScenePlayerDataTable()
	{
		_sceneDataTable = new HashMap<>();

		reload();

		ReloadableManager.getInstance().register("scenes", this);
	}

	@Override
	public String getReloadMessage(boolean success)
	{
		return "Scene Data Table reloaded";
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ScenePlayerDataTable _instance = new ScenePlayerDataTable();
	}
}
