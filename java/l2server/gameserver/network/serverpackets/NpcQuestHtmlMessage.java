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

import l2server.Config;
import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.log.Log;

/**
 * the HTML parser in the client knowns these standard and non-standard tags and attributes
 * VOLUMN
 * UNKNOWN
 * UL
 * U
 * TT
 * TR
 * TITLE
 * TEXTCODE
 * TEXTAREA
 * TD
 * TABLE
 * SUP
 * SUB
 * STRIKE
 * SPIN
 * SELECT
 * RIGHT
 * PRE
 * P
 * OPTION
 * OL
 * MULTIEDIT
 * LI
 * LEFT
 * INPUT
 * IMG
 * I
 * HTML
 * H7
 * H6
 * H5
 * H4
 * H3
 * H2
 * H1
 * FONT
 * EXTEND
 * EDIT
 * COMMENT
 * COMBOBOX
 * CENTER
 * BUTTON
 * BR
 * BR1
 * BODY
 * BAR
 * ADDRESS
 * A
 * SEL
 * LIST
 * VAR
 * FORE
 * READONL
 * ROWS
 * VALIGN
 * FIXWIDTH
 * BORDERCOLORLI
 * BORDERCOLORDA
 * BORDERCOLOR
 * BORDER
 * BGCOLOR
 * BACKGROUND
 * ALIGN
 * VALU
 * READONLY
 * MULTIPLE
 * SELECTED
 * TYP
 * TYPE
 * MAXLENGTH
 * CHECKED
 * SRC
 * Y
 * X
 * QUERYDELAY
 * NOSCROLLBAR
 * IMGSRC
 * B
 * FG
 * SIZE
 * FACE
 * COLOR
 * DEFFON
 * DEFFIXEDFONT
 * WIDTH
 * VALUE
 * TOOLTIP
 * NAME
 * MIN
 * MAX
 * HEIGHT
 * DISABLED
 * ALIGN
 * MSG
 * LINK
 * HREF
 * ACTION
 *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public final class NpcQuestHtmlMessage extends L2GameServerPacket
{

	private int _npcObjId;
	private String _html;
	private int _questId = 0;

	/**
	 * @param npcObjId
	 * @param questId
	 */
	public NpcQuestHtmlMessage(int npcObjId, int questId)
	{
		_npcObjId = npcObjId;
		_questId = questId;
	}

	@Override
	public void runImpl()
	{
		if (Config.BYPASS_VALIDATION)
		{
			buildBypassCache(getClient().getActiveChar());
		}
	}

	public void setHtml(String text)
	{
		if (!text.contains("<html>"))
		{
			text = "<html><body>" + text + "</body></html>";
		}

		_html = text;
	}

	public boolean setFile(String path)
	{
		String content = HtmCache.getInstance().getHtm(getClient().getActiveChar().getHtmlPrefix(), path);

		if (content == null)
		{
			setHtml("<html><body>My Text is missing:<br>" + path + "</body></html>");
			Log.warning("missing html page " + path);
			return false;
		}

		setHtml(content);
		return true;
	}

	public void replace(String pattern, String value)
	{
		_html = _html.replaceAll(pattern, value);
	}

	private void buildBypassCache(L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return;
		}

		activeChar.clearBypass();
		int len = _html.length();
		for (int i = 0; i < len; i++)
		{
			int start = _html.indexOf("bypass -h", i);
			int finish = _html.indexOf("\"", start);

			if (start < 0 || finish < 0)
			{
				break;
			}

			start += 10;
			i = finish;
			int finish2 = _html.indexOf("$", start);
			if (finish2 < finish && finish2 > 0)
			{
				activeChar.addBypass2(_html.substring(start, finish2).trim());
			}
			else
			{
				activeChar.addBypass(_html.substring(start, finish).trim());
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_npcObjId);
		writeS(_html);
		writeD(_questId);
	}
}
