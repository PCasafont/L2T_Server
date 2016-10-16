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
public final class NpcHtmlMessage extends L2GameServerPacket
{
	// d S
	// d is usually 0, S is the html text starting with <html> and ending with </html>
	//

	private int npcObjId;
	private String html;
	private int itemId = 0;
	private boolean isFirstTalk = false;
	private boolean validate = true;

	/**
	 * @param npcObjId
	 * @param itemId
	 */
	public NpcHtmlMessage(int npcObjId, int itemId)
	{
		this.npcObjId = npcObjId;
		this.itemId = itemId;
	}

	/**
	 */
	public NpcHtmlMessage(int npcObjId, String text)
	{
		this.npcObjId = npcObjId;
		setHtml(text);
	}

	public NpcHtmlMessage(int npcObjId)
	{
		this.npcObjId = npcObjId;
	}

	public void isFirstTalk()
	{
		this.isFirstTalk = true;
	}

	/**
	 * disable building bypass validation cache for this packet
	 */
	public void disableValidation()
	{
		this.validate = false;
	}

	@Override
	public void runImpl()
	{
		if (Config.BYPASS_VALIDATION && this.validate)
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

		this.html = text;
	}

	public boolean setFile(String prefix, String path)
	{
		String content = HtmCache.getInstance().getHtm(prefix, path);

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
		this.html = this.html.replaceAll(pattern, value.replaceAll("\\$", "\\\\\\$"));
	}

	private void buildBypassCache(L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return;
		}

		activeChar.clearBypass();
		int len = this.html.length();
		for (int i = 0; i < len; i++)
		{
			int start = this.html.indexOf("\"bypass ", i);
			int finish = this.html.indexOf("\"", start + 1);
			if (start < 0 || finish < 0)
			{
				break;
			}

			if (this.html.substring(start + 8, start + 10).equals("-h"))
			{
				start += 11;
			}
			else
			{
				start += 8;
			}

			i = finish;
			int finish2 = this.html.indexOf("$", start);
			if (finish2 < finish && finish2 > 0)
			{
				activeChar.addBypass2(this.html.substring(start, finish2).trim());
			}
			else
			{
				activeChar.addBypass(this.html.substring(start, finish).trim());
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(this.npcObjId);
		writeS(this.html);
		writeD(this.itemId);
		writeD(this.isFirstTalk ? 0x00 : 0x01);
	}
}
