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

import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author l3x
 */
public class L2CastleWarehouseInstance extends L2WarehouseInstance
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;

	/**
	 * @param template
	 */
	public L2CastleWarehouseInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2CastleWarehouseInstance);
	}

	@Override
	public boolean isWarehouse()
	{
		return true;
	}

	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "castlewarehouse/castlewarehouse-no.htm";

		int condition = validateCondition(player);

		if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			{
				filename = "castlewarehouse/castlewarehouse-busy.htm";
			}
			else if (condition == COND_OWNER)
			{
				if (val == 0)
				{
					filename = "castlewarehouse/castlewarehouse.htm";
				}
				else
				{
					filename = "castlewarehouse/castlewarehouse-" + val + ".htm";
				}
			}
		}
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	protected int validateCondition(L2PcInstance player)
	{
		if (player.isGM())
		{
			return COND_OWNER;
		}

		if (getCastle() != null && getCastle().getCastleId() > 0)
		{
			if (player.getClan() != null)
			{
				if (getCastle().getZone().isActive())
				{
					return COND_BUSY_BECAUSE_OF_SIEGE;
				}
				else if (getCastle().getOwnerId() == player.getClanId())
				{
					return COND_OWNER;
				}
			}
		}
		return COND_ALL_FALSE;
	}
}
