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

package handlers.bypasshandlers;

import l2server.Config;
import l2server.gameserver.handler.IBypassHandler;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.base.Experience;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;

public class Delevel implements IBypassHandler
{
	private static final String[] COMMANDS = {"Delevel"};

	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Npc target)
	{
		if (target == null || !Config.isServer(Config.TENKAI))
		{
			return false;
		}

		if (command.equalsIgnoreCase("Delevel"))
		{
			int minLevel = activeChar.getCurrentClass().level() > 1 ? 40 : 1;
			for (L2Skill skill : activeChar.getAllSkills())
			{
				if (skill.getEnchantLevel() > 0)
				{
					minLevel = activeChar.getLevel() + 1;
					break;
				}
			}
			String html =
					"<html>" + "<title>Tenkai</title>" + "<body>" + "<center><br><tr><td>Change Level</tr></td><br>" +
							"<br>" + "What level do you wish to be?<br>" + "<table width=300>";
			for (int i = minLevel; i < activeChar.getLevel(); i += 10)
			{
				html += "<tr>";
				for (int j = i; j < i + 10 && j < activeChar.getLevel(); j++)
				{
					html += "<td fixwidth=\"15\"><a action=\"bypass -h npc_%objectId%_Delevel " + j + "\">" + j +
							"</a></td>";
				}
				html += "</tr>";
			}
			html += "</table></center></body></html>";
			NpcHtmlMessage packet = new NpcHtmlMessage(target.getObjectId());
			packet.setHtml(html);
			packet.replace("%objectId%", String.valueOf(target.getObjectId()));
			activeChar.sendPacket(packet);
		}
		else
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(8));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			byte lvl = (byte) val;
			if (lvl >= 1 && lvl <= Config.MAX_LEVEL + 1)
			{
				long pXp = activeChar.getExp();
				long tXp = Experience.getAbsoluteExp(lvl);

				int rep = activeChar.getReputation();
				if (pXp > tXp)
				{
					activeChar.removeExpAndSp(pXp - tXp, 0);
				}
				else if (pXp < tXp)
				{
					activeChar.addExpAndSp(tXp - pXp, 0);
				}
				activeChar.setReputation(rep);
			}
			else
			{
				activeChar.sendMessage("There was an error while changing your level.");
				return false;
			}
		}

		return true;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
