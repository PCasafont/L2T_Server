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
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author Vice
 */
public class L2FortSiegeNpcInstance extends L2Npc
{
	public L2FortSiegeNpcInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
		setInstanceType(InstanceType.L2FortSiegeNpcInstance);
	}

	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);

		String filename;

		if (val == 0)
		{
			filename = "fortress/merchant.htm";
		}
		else
		{
			filename = "fortress/merchant-" + val + ".htm";
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		if (getFort().getOwnerClan() != null)
		{
			html.replace("%clanname%", getFort().getOwnerClan().getName());
		}
		else
		{
			html.replace("%clanname%", "NPC");
		}
		player.sendPacket(html);
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
}
