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

package l2server.gameserver.network.serverpackets;

import l2server.util.StringUtil;

import java.util.List;

public class ShowBoard extends L2GameServerPacket
{

	private StringBuilder _htmlCode;

	public ShowBoard(String htmlCode, String id)
	{
		_htmlCode = StringUtil.startAppend(500, id, "\u0008", htmlCode);
	}

	public ShowBoard(List<String> arg)
	{
		_htmlCode = StringUtil.startAppend(500, "1002\u0008");
		for (String str : arg)
		{
			StringUtil.append(_htmlCode, str, " \u0008");
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x01); //c4 1 to show community 00 to hide
		writeS("bypass _bbshome"); // top
		writeS("bypass _bbsgetfav"); // favorite
		writeS("bypass _bbsloc"); // region
		writeS("bypass _bbsclan"); // clan
		writeS("bypass _bbsmemo"); // memo
		writeS("bypass _bbsmail"); // mail
		writeS("bypass _bbsfriends"); // friends
		writeS("bypass bbs_add_fav"); // add fav.
		if (_htmlCode.length() < 8192)
		{
			writeS(_htmlCode.toString());
		}
		else
		{
			writeS("<html><body>Html is too long!</body></html>");
		}
	}
}
