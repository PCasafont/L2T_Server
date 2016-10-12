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

import l2server.gameserver.instancemanager.CastleManorManager;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author l3x
 */
public class L2CastleBlacksmithInstance extends L2NpcInstance
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;

	public L2CastleBlacksmithInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2CastleBlacksmithInstance);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (CastleManorManager.getInstance().isDisabled())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player.getHtmlPrefix(), "npcdefault.htm");
			html.replace("%objectId%", String.valueOf(getObjectId()));
			html.replace("%npcname%", getName());
			player.sendPacket(html);
			return;
		}

		int condition = validateCondition(player);
		if (condition <= COND_ALL_FALSE)
		{
		}
		else if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
		{
		}
		else if (condition == COND_OWNER)
		{
			if (command.startsWith("Chat"))
			{
				int val = 0;
				try
				{
					val = Integer.parseInt(command.substring(5));
				}
				catch (IndexOutOfBoundsException | NumberFormatException ignored)
				{
				}
				showChatWindow(player, val);
			}
			else
			{
				super.onBypassFeedback(player, command);
			}
		}
	}

	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		if (CastleManorManager.getInstance().isDisabled())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player.getHtmlPrefix(), "npcdefault.htm");
			html.replace("%objectId%", String.valueOf(getObjectId()));
			html.replace("%npcname%", getName());
			player.sendPacket(html);
			return;
		}

		String filename = "castleblacksmith/castleblacksmith-no.htm";

		int condition = validateCondition(player);
		if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			{
				filename = "castleblacksmith/castleblacksmith-busy.htm"; // Busy because of siege
			}
			else if (condition == COND_OWNER) // Clan owns castle
			{
				if (val == 0)
				{
					filename = "castleblacksmith/castleblacksmith.htm";
				}
				else
				{
					filename = "castleblacksmith/castleblacksmith-" + val + ".htm";
				}
			}
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		html.replace("%castleid%", Integer.toString(getCastle().getCastleId()));
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
					return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
				}
				else if (getCastle().getOwnerId() == player.getClanId() &&
						(player.getClanPrivileges() & L2Clan.CP_CS_MANOR_ADMIN) ==
								L2Clan.CP_CS_MANOR_ADMIN) // Leader of clan
				{
					return COND_OWNER; // Owner
				}
			}
		}

		return COND_ALL_FALSE;
	}
}
