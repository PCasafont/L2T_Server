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

import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.ShowBoard;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseBBSManager {
	public abstract void parsecmd(String command, Player activeChar);

	public abstract void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, Player activeChar);

	protected void separateAndSend(String html, Player acha) {
		if (html == null) {
			return;
		}
		if (html.length() < 4096) {
			acha.sendPacket(new ShowBoard(html, "101"));
			acha.sendPacket(new ShowBoard(null, "102"));
			acha.sendPacket(new ShowBoard(null, "103"));
		} else if (html.length() < 8192) {
			acha.sendPacket(new ShowBoard(html.substring(0, 4096), "101"));
			acha.sendPacket(new ShowBoard(html.substring(4096), "102"));
			acha.sendPacket(new ShowBoard(null, "103"));
		} else if (html.length() < 16384) {
			acha.sendPacket(new ShowBoard(html.substring(0, 4096), "101"));
			acha.sendPacket(new ShowBoard(html.substring(4096, 8192), "102"));
			acha.sendPacket(new ShowBoard(html.substring(8192), "103"));
		}
	}

	/**
	 * @param html
	 * @param acha
	 */
	protected void send1001(String html, Player acha) {
		if (html.length() < 8192) {
			acha.sendPacket(new ShowBoard(html, "1001"));
		}
	}

	/**
	 * @param acha
	 */
	protected void send1002(Player acha) {
		send1002(acha, " ", " ", "0");
	}

	/**
	 * @param activeChar
	 * @param string
	 * @param string2
	 * @param string3
	 */
	protected void send1002(Player activeChar, String string, String string2, String string3) {
		List<String> arg = new ArrayList<>();
		arg.add("0");
		arg.add("0");
		arg.add("0");
		arg.add("0");
		arg.add("0");
		arg.add("0");
		arg.add(activeChar.getName());
		arg.add(Integer.toString(activeChar.getObjectId()));
		arg.add(activeChar.getAccountName());
		arg.add("9");
		arg.add(string2);
		arg.add(string2);
		arg.add(string);
		arg.add(string3);
		arg.add(string3);
		arg.add("0");
		arg.add("0");
		activeChar.sendPacket(new ShowBoard(arg));
	}
}
