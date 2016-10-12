/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.taskmanager;

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.util.Broadcast;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;

/**
 * @author nBd
 */
public class AutoAnnounceTaskManager
{
	public static AutoAnnounceTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private AutoAnnounceTaskManager()
	{
		load();
	}

	private void load()
	{
		File file = new File(Config.DATAPACK_ROOT, "data_" + Config.SERVER_NAME + "/autoAnnouncements.xml");
		if (!file.exists())
		{
			return;
		}
		int count = 0;
		XmlDocument doc = new XmlDocument(file);
		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				for (XmlNode d : n.getChildren())
				{
					if (d.getName().equalsIgnoreCase("announce"))
					{
						String text = d.getString("text");
						int initial = d.getInt("initial");
						int reuse = d.getInt("reuse");

						ThreadPoolManager.getInstance()
								.scheduleGeneralAtFixedRate(new AutoAnnouncement(text), initial * 60000, reuse * 60000);
						count++;
					}
				}
			}
		}
		Log.info("AutoAnnouncements: Loaded: " + count + " auto announcements!");
	}

	private class AutoAnnouncement implements Runnable
	{
		private String _text;

		private AutoAnnouncement(String text)
		{
			_text = text;
		}

		@Override
		public void run()
		{
			Broadcast.announceToOnlinePlayers(_text);
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final AutoAnnounceTaskManager _instance = new AutoAnnounceTaskManager();
	}
}
