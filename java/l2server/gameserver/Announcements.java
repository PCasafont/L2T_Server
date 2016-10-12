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

package l2server.gameserver;

import l2server.Config;
import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.script.DateRange;
import l2server.gameserver.util.Broadcast;
import l2server.log.Log;
import l2server.util.StringUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

/**
 * This class ...
 *
 * @version $Revision: 1.5.2.1.2.7 $ $Date: 2005/03/29 23:15:14 $
 */
public class Announcements
{

	private List<String> _announcements = new ArrayList<>();
	private List<List<Object>> _eventAnnouncements = new ArrayList<>();

	private Announcements()
	{
		loadAnnouncements();
	}

	public static Announcements getInstance()
	{
		return SingletonHolder._instance;
	}

	public void loadAnnouncements()
	{
		_announcements.clear();

		File file = new File(Config.DATAPACK_ROOT, "data_" + Config.SERVER_NAME + "/announcements.txt");
		if (file.exists())
		{
			readFromDisk(file);
		}
		else
		{
			file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "announcements.txt");
			if (file.exists())
			{
				readFromDisk(file);
			}
			else
			{
				Log.warning(Config.DATA_FOLDER + "announcements.txt doesn't exist");
			}
		}
	}

	public void showAnnouncements(L2PcInstance activeChar)
	{
		for (String _announcement : _announcements)
		{
			CreatureSay cs = new CreatureSay(0, Say2.ANNOUNCEMENT, activeChar.getName(), _announcement);
			activeChar.sendPacket(cs);
		}

		for (List<Object> entry : _eventAnnouncements)
		{
			DateRange validDateRange = (DateRange) entry.get(0);
			String[] msg = (String[]) entry.get(1);
			Date currentDate = new Date();

			if (!validDateRange.isValid() || validDateRange.isWithinRange(currentDate))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
				for (String element : msg)
				{
					sm.addString(element);
				}
				activeChar.sendPacket(sm);
			}
		}
	}

	public void addEventAnnouncement(DateRange validDateRange, String[] msg)
	{
		List<Object> entry = new ArrayList<>();
		entry.add(validDateRange);
		entry.add(msg);
		_eventAnnouncements.add(entry);
	}

	public void listAnnouncements(L2PcInstance activeChar)
	{
		String content = HtmCache.getInstance().getHtmForce(activeChar.getHtmlPrefix(), "admin/announce.htm");
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(content);
		final StringBuilder replyMSG = StringUtil.startAppend(500, "<br>");
		for (int i = 0; i < _announcements.size(); i++)
		{
			StringUtil.append(replyMSG, "<table width=260><tr><td width=220>", _announcements.get(i),
					"</td><td width=40>" + "<button value=\"Delete\" action=\"bypass -h admin_del_announcement ",
					String.valueOf(i),
					"\" width=60 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>");
		}
		adminReply.replace("%announces%", replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	public void addAnnouncement(String text)
	{
		_announcements.add(text);
		saveToDisk();
	}

	public void delAnnouncement(int line)
	{
		_announcements.remove(line);
		saveToDisk();
	}

	private void readFromDisk(File file)
	{
		LineNumberReader lnr = null;
		try
		{
			int i = 0;
			String line = null;
			lnr = new LineNumberReader(new FileReader(file));
			while ((line = lnr.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(line, "\n\r");
				if (st.hasMoreTokens())
				{
					String announcement = st.nextToken();
					_announcements.add(announcement);

					i++;
				}
			}

			if (Config.DEBUG)
			{
				Log.info("Announcements: Loaded " + i + " Announcements.");
			}
		}
		catch (IOException e1)
		{
			Log.log(Level.SEVERE, "Error reading announcements: ", e1);
		}
		finally
		{
			try
			{
				lnr.close();
			}
			catch (Exception e2)
			{
				// nothing
			}
		}
	}

	private void saveToDisk()
	{
		File file = new File("data_" + Config.SERVER_NAME + "/announcements.txt");
		FileWriter save = null;

		try
		{
			save = new FileWriter(file);
			for (String _announcement : _announcements)
			{
				save.write(_announcement);
				save.write("\r\n");
			}
		}
		catch (IOException e)
		{
			Log.log(Level.SEVERE, "Saving to the announcements file has failed: ", e);
		}
		finally
		{
			try
			{
				save.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void announceToAll(String text)
	{
		Broadcast.announceToOnlinePlayers(text);
	}

	public void announceToAll(SystemMessage sm)
	{
		Broadcast.toAllOnlinePlayers(sm);
	}

	public void announceToInstance(SystemMessage sm, int instanceId)
	{
		Broadcast.toPlayersInInstance(sm, instanceId);
	}

	// Method for handling announcements from admin
	public void handleAnnounce(String command, int lengthToTrim)
	{
		try
		{
			// Announce string to everyone on server
			String text = command.substring(lengthToTrim);
			SingletonHolder._instance.announceToAll(text);
		}

		// No body cares!
		catch (StringIndexOutOfBoundsException e)
		{
			// empty message.. ignore
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final Announcements _instance = new Announcements();
	}
}
