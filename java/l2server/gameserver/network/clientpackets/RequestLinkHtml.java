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

package l2server.gameserver.network.clientpackets;

import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.log.Log;

import java.util.logging.Level;

/**
 * @author zabbix
 *         Lets drink to code!
 */
public final class RequestLinkHtml extends L2GameClientPacket
{

	private String link;

	@Override
	protected void readImpl()
	{
		this.link = readS();
	}

	@Override
	public void runImpl()
	{
		L2PcInstance actor = getClient().getActiveChar();
		if (actor == null)
		{
			return;
		}

		if (this.link.contains("..") || !this.link.contains(".htm"))
		{
			Log.warning("[RequestLinkHtml] hack? link contains prohibited characters: '" + this.link + "', skipped");
			return;
		}
		try
		{
			String filename = "" + this.link;
			NpcHtmlMessage msg = new NpcHtmlMessage(0);
			msg.disableValidation();
			msg.setFile(actor.getHtmlPrefix(), filename);
			sendPacket(msg);
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Bad RequestLinkHtml: ", e);
		}
	}
}
