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

import l2server.Config;
import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.datatables.TeleportLocationTable;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.instancemanager.SiegeManager;
import l2server.gameserver.instancemanager.TownManager;
import l2server.gameserver.model.L2TeleportLocation;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.olympiad.OlympiadManager;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.log.Log;

import java.util.Calendar;
import java.util.StringTokenizer;

/**
 * @author NightMarez
 * @version $Revision: 1.3.2.2.2.5 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2TeleporterInstance extends L2Npc
{
	private static final int COND_ALL_FALSE = 0;
	private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	private static final int COND_OWNER = 2;
	private static final int COND_REGULAR = 3;

	/**
	 * @param template
	 */
	public L2TeleporterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2TeleporterInstance);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);

		int condition = validateCondition(player);

		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command

		if (player.getFirstEffect(6201) != null || player.getFirstEffect(6202) != null ||
				player.getFirstEffect(6203) != null)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());

			String filename = "teleporter/epictransformed.htm";

			html.setFile(player.getHtmlPrefix(), filename);
			html.replace("%objectId%", String.valueOf(getObjectId()));
			html.replace("%npcname%", getName());
			player.sendPacket(html);
			return;
		}
		else if (actualCommand.equalsIgnoreCase("goto"))
		{
			int npcId = getNpcId();

			switch (npcId)
			{
				case 32534: // Seed of Infinity
				case 32539:
					if (player.isFlyingMounted())
					{
						player.sendPacket(SystemMessage
								.getSystemMessage(SystemMessageId.YOU_CANNOT_ENTER_SEED_IN_FLYING_TRANSFORM));
						return;
					}
					break;
			}

			if (st.countTokens() <= 0)
			{
				return;
			}

			int whereTo = Integer.parseInt(st.nextToken());
			if (condition == COND_REGULAR)
			{
				doTeleport(player, whereTo);
				return;
			}
			else if (condition == COND_OWNER)
			{
				int minPrivilegeLevel = 0; // NOTE: Replace 0 with highest level when privilege level is implemented
				if (st.countTokens() >= 1)
				{
					minPrivilegeLevel = Integer.parseInt(st.nextToken());
				}

				if (10 >= minPrivilegeLevel) // NOTE: Replace 10 with privilege level of player
				{
					doTeleport(player, whereTo);
				}
				else
				{
					player.sendMessage("You don't have the sufficient access level to teleport there.");
				}
				return;
			}
		}
		else if (command.startsWith("Chat"))
		{
			Calendar cal = Calendar.getInstance();
			String val = command.substring(5);

			if (!Config.IS_CLASSIC)
			{
				if (val.equalsIgnoreCase("1") && player.getLevel() < 41)
				{
					showNewbieHtml(player);
					return;
				}
				else if (val.equalsIgnoreCase("1") && cal.get(Calendar.HOUR_OF_DAY) >= 20 &&
						cal.get(Calendar.HOUR_OF_DAY) <= 23 &&
						(cal.get(Calendar.DAY_OF_WEEK) == 1 || cal.get(Calendar.DAY_OF_WEEK) == 7))
				{
					showHalfPriceHtml(player);
					return;
				}
			}
			showChatWindow(player, val);

			return;
		}

		super.onBypassFeedback(player, command);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}

		return "teleporter/" + pom + ".htm";
	}

	@Override
	public String getHtmlPath(int npcId, String val)
	{
		String pom = "";
		if (val.isEmpty() || val.equals("0"))
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}

		return "teleporter/" + pom + ".htm";
	}

	private void showNewbieHtml(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());

		String filename = "teleporter/free/" + getTemplate().NpcId + ".htm";
		if (!HtmCache.getInstance().isLoadable(filename))
		{
			filename = "teleporter/" + getTemplate().NpcId + "-1.htm";
		}

		html.setFile(player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	private void showHalfPriceHtml(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());

		String filename = "teleporter/half/" + getNpcId() + ".htm";
		if (!HtmCache.getInstance().isLoadable(filename))
		{
			filename = "teleporter/" + getNpcId() + "-1.htm";
		}

		html.setFile(player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	@Override
	public void showChatWindow(L2PcInstance player)
	{
		String filename = "teleporter/castleteleporter-no.htm";

		int condition = validateCondition(player);
		if (condition == COND_REGULAR)
		{
			super.showChatWindow(player);
			return;
		}
		else if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			{
				filename = "teleporter/castleteleporter-busy.htm"; // Busy because of siege
			}
			else if (condition == COND_OWNER) // Clan owns castle
			{
				filename = getHtmlPath(getNpcId(), 0); // Owner message window
			}
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	protected void doTeleport(L2PcInstance player, int val)
	{
		L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
		if (list != null)
		{
			//you cannot teleport to village that is in siege
			if (SiegeManager.getInstance().getSiege(list.getLocX(), list.getLocY(), list.getLocZ()) != null)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE));
				return;
			}
			else if (TownManager.townHasCastleInSiege(list.getLocX(), list.getLocY()) &&
					(isInsideZone(L2Character.ZONE_TOWN) || getNpcId() > 40000))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE));
				return;
			}
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && player.getReputation() < 0) //karma
			{
				player.sendMessage("Go away, you're not welcome here.");
				return;
			}
			else if (Config.isServer(Config.TENKAI) && getNpcId() == 40001 && player.getPvpFlag() > 0 &&
					player.getCurrentHp() * 100 / player.getMaxHp() < 60) //pvping?
			{
				player.sendPacket(new NpcHtmlMessage(getObjectId(),
						"<html><body>I don't stand there for cowards to escape from battle!</body></html>"));
				return;
			}
			else if (OlympiadManager.getInstance().isRegisteredInComp(player))
			{
				player.sendMessage("You can't teleport while registred in Olympiad Games!");
				return;
			}
			else if (player.isCombatFlagEquipped())
			{
				player.sendPacket(SystemMessage
						.getSystemMessage(SystemMessageId.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD));
				return;
			}
			else if (list.getIsForNoble() && !player.isNoble())
			{
				String filename = "teleporter/nobleteleporter-no.htm";
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player.getHtmlPrefix(), filename);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				player.sendPacket(html);
				return;
			}
			else if (player.isAlikeDead())
			{
				return;
			}

			Calendar cal = Calendar.getInstance();
			int price = list.getPrice();

			if (player.getLevel() < 41)
			{
				price = 0;
			}
			else if (!list.getIsForNoble())
			{
				if (cal.get(Calendar.HOUR_OF_DAY) >= 20 && cal.get(Calendar.HOUR_OF_DAY) <= 23 &&
						(cal.get(Calendar.DAY_OF_WEEK) == 1 || cal.get(Calendar.DAY_OF_WEEK) == 7))
				{
					price /= 2;
				}
			}

			if (Config.ALT_GAME_FREE_TELEPORT ||
					player.destroyItemByItemId("Teleport " + (list.getIsForNoble() ? " nobless" : ""), list.getItemId(),
							price, this, true))
			{
				if (Config.DEBUG)
				{
					Log.fine("Teleporting player " + player.getName() + " to new location: " + list.getLocX() + ":" +
							list.getLocY() + ":" + list.getLocZ());
				}

				player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), true);
			}
		}
		else
		{
			Log.warning("No teleport destination with id:" + val);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	private int validateCondition(L2PcInstance player)
	{
		if (getNpcId() > 40000 ||
				CastleManager.getInstance().getCastleIndex(this) < 0) // Teleporter isn't on castle ground
		{
			return COND_REGULAR; // Regular access
		}
		else if (getCastle().getSiege().getIsInProgress()) // Teleporter is on castle ground and siege is in progress
		{
			return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
		}
		else if (player.getClan() != null) // Teleporter is on castle ground and player is in a clan
		{
			if (getCastle().getOwnerId() == player.getClanId()) // Clan owns castle
			{
				return COND_OWNER; // Owner
			}
		}

		return COND_ALL_FALSE;
	}
}
