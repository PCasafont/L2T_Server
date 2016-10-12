/*
 * Copyright (C) 2004-2013 L2J Server
 *
 * This file is part of L2J Server.
 *
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.communitybbs.Manager;

import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ShowBoard;

import java.util.StringTokenizer;

public class TopBBSManager extends BaseBBSManager
{
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.equals("_bbstop"))
		{
			String content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "CommunityBoard/index.htm");
			if (content == null)
			{
				content =
						"<html><body><br><br><center>404 :File not found: 'data/html/CommunityBoard/index.htm' </center></body></html>";
			}
			separateAndSend(content, activeChar);
		}
		else if (command.equals("_bbshome"))
		{
			CustomCommunityBoard.getInstance().parseCmd("_bbscustom;main", activeChar);
		}
		else if (command.startsWith("_bbstop;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			int idp = Integer.parseInt(st.nextToken());
			String content =
					HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "CommunityBoard/" + idp + ".htm");
			if (content == null)
			{
				content = "<html><body><br><br><center>404 :File not found: 'data/html/CommunityBoard/" + idp +
						".htm' </center></body></html>";
			}
			separateAndSend(content, activeChar);
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center></center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}

	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	}

	public static TopBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final TopBBSManager _instance = new TopBBSManager();
	}
}
