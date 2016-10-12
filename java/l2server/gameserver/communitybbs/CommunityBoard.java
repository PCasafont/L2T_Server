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

package l2server.gameserver.communitybbs;

import l2server.Config;
import l2server.gameserver.communitybbs.Manager.*;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.L2GameClient;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ShowBoard;

public class CommunityBoard
{
	public static CommunityBoard getInstance()
	{
		return SingletonHolder._instance;
	}

	public void handleCommands(L2GameClient client, String command)
	{
		L2PcInstance activeChar = client.getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		int communityType = Config.COMMUNITY_TYPE;

		if (activeChar.getName().equals("Ghost"))
		{
			communityType = 2;
		}

		switch (communityType)
		{
			default:
			case 0: // disabled
				activeChar.sendPacket(SystemMessageId.CB_OFFLINE);
				break;
			case 1: // old
				RegionBBSManager.getInstance().parsecmd(command, activeChar);
				break;
			case 2: // new
				if (command.startsWith("_bbsclan"))
				{
					ClanBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if (command.startsWith("_bbsmemo"))
				{
					TopicBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if (command.startsWith("_bbstopics"))
				{
					TopicBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if (command.startsWith("_bbsposts"))
				{
					PostBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if (command.startsWith("_bbstop"))
				{
					TopBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if (command.startsWith("_bbshome"))
				{
					TopBBSManager.getInstance().parsecmd(command, activeChar);
				}
				else if (command.startsWith("_bbsloc"))
				{
					RegionBBSManager.getInstance().parsecmd(command, activeChar);
				}
				// LasTravel
				else if (command.startsWith("_bbscustom"))
				{
					CustomCommunityBoard.getInstance().parseCmd(command, activeChar);
				}
				else
				{
					ShowBoard sb = new ShowBoard("<html><body><br><br><center></center><br><br></body></html>", "101");
					activeChar.sendPacket(sb);
					activeChar.sendPacket(new ShowBoard(null, "102"));
					activeChar.sendPacket(new ShowBoard(null, "103"));
				}
				break;
		}
	}

	/**
	 * @param client
	 * @param url
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 */
	public void handleWriteCommands(L2GameClient client, String url, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		L2PcInstance activeChar = client.getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		switch (Config.COMMUNITY_TYPE)
		{
			case 2:
				switch (url)
				{
					case "Topic":
						TopicBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
						break;
					case "Post":
						PostBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
						break;
					case "Region":
						RegionBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
						break;
					case "Notice":
						ClanBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
						break;
					default:
						ShowBoard sb =
								new ShowBoard("<html><body><br><br><center></center><br><br></body></html>", "101");
						activeChar.sendPacket(sb);
						activeChar.sendPacket(new ShowBoard(null, "102"));
						activeChar.sendPacket(new ShowBoard(null, "103"));
						break;
				}
				break;
			case 1:
				RegionBBSManager.getInstance().parsewrite(arg1, arg2, arg3, arg4, arg5, activeChar);
				break;
			default:
			case 0:
				ShowBoard sb = new ShowBoard(
						"<html><body><br><br><center>The Community board is currently disabled</center><br><br></body></html>",
						"101");
				activeChar.sendPacket(sb);
				activeChar.sendPacket(new ShowBoard(null, "102"));
				activeChar.sendPacket(new ShowBoard(null, "103"));
				break;
		}
	}

	private static class SingletonHolder
	{
		protected static final CommunityBoard _instance = new CommunityBoard();
	}
}
