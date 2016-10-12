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

package l2server.gameserver.model.actor.instance;

import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author NightMarez
 * @version $Revision: 1.3.2.2.2.5 $ $Date: 2005/03/27 15:29:32 $
 */
public final class L2ObservationInstance extends L2Npc
{
	public L2ObservationInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2ObservationInstance);
	}

	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		String filename = null;

		if (isInsideRadius(-79884, 86529, 50, true) || isInsideRadius(-78858, 111358, 50, true) ||
				isInsideRadius(-76973, 87136, 50, true) || isInsideRadius(-75850, 111968, 50, true))
		{
			if (val == 0)
			{
				filename = "observation/" + getNpcId() + "-Oracle.htm";
			}
			else
			{
				filename = "observation/" + getNpcId() + "-Oracle-" + val + ".htm";
			}
		}
		else
		{
			if (val == 0)
			{
				filename = "observation/" + getNpcId() + ".htm";
			}
			else
			{
				filename = "observation/" + getNpcId() + "-" + val + ".htm";
			}
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
}
