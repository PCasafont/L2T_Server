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

import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ShowBoard;

public class AdminBBSManager extends BaseBBSManager
{
	/**
	 * Gets the single instance of AdminBBSManager.
	 *
	 * @return single instance of AdminBBSManager
	 */
	public static AdminBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}

	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (!activeChar.isGM())
		{
			return;
		}
		if (command.startsWith("admin_bbs"))
		{
			separateAndSend("<html><body><br><br><center>This Page is only an exemple :)<br><br>command=" + command +
					"</center></body></html>", activeChar);
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
		if (!activeChar.isGM())
		{
		}
	}

	private static class SingletonHolder
	{
		protected static final AdminBBSManager _instance = new AdminBBSManager();
	}
}
