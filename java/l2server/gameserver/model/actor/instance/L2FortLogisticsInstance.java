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

import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.templates.chars.L2NpcTemplate;

import java.util.StringTokenizer;

/**
 * @author Vice
 */
public class L2FortLogisticsInstance extends L2MerchantInstance
{
	private static final int BLOOD_OATH = 9910;
	private static final int[] SUPPLY_BOX_IDS = {
			35665,
			35697,
			35734,
			35766,
			35803,
			35834,
			35866,
			35903,
			35935,
			35973,
			36010,
			36042,
			36080,
			36117,
			36148,
			36180,
			36218,
			36256,
			36293,
			36325,
			36363
	};

	public L2FortLogisticsInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
		setInstanceType(InstanceType.L2FortLogisticsInstance);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		// BypassValidation Exploit plug.
		if (player.getLastFolkNPC().getObjectId() != getObjectId())
		{
			return;
		}

		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command

		String par = "";
		if (st.countTokens() >= 1)
		{
			par = st.nextToken();
		}

		if (actualCommand.equalsIgnoreCase("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(par);
			}
			catch (IndexOutOfBoundsException | NumberFormatException ignored)
			{
			}

			showMessageWindow(player, val);
		}
		else if (actualCommand.equalsIgnoreCase("rewards"))
		{
			if (player.getClan() != null && getFort().getOwnerClan() != null &&
					player.getClan() == getFort().getOwnerClan() && player.isClanLeader())
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player.getHtmlPrefix(), "fortress/logistics-rewards.htm");
				int blood = getFort().getBloodOathReward();
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%bloodoath%", String.valueOf(blood));
				player.sendPacket(html);
			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player.getHtmlPrefix(), "fortress/logistics-noprivs.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
			}
		}
		else if (actualCommand.equalsIgnoreCase("blood"))
		{
			if (player.getClan() != null && getFort().getOwnerClan() != null &&
					player.getClan() == getFort().getOwnerClan() && player.isClanLeader())
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				int blood = getFort().getBloodOathReward();
				if (blood > 0)
				{
					html.setFile(player.getHtmlPrefix(), "fortress/logistics-blood.htm");
					player.addItem("Quest", BLOOD_OATH, blood, this, true);
					getFort().setBloodOathReward(0);
					getFort().saveFortVariables();
				}
				else
				{
					html.setFile(player.getHtmlPrefix(), "fortress/logistics-noblood.htm");
				}
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player.getHtmlPrefix(), "fortress/logistics-noprivs.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
			}
		}
		else if (actualCommand.equalsIgnoreCase("supplylvl"))
		{
			if (player.getClan() != null && getFort().getOwnerClan() != null &&
					player.getClan() == getFort().getOwnerClan() && getFort().getFortState() == 2)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				if (player.isClanLeader())
				{
					html.setFile(player.getHtmlPrefix(), "fortress/logistics-supplylvl.htm");
					html.replace("%supplylvl%", String.valueOf(getFort().getSupplyLvL()));
				}
				else
				{
					html.setFile(player.getHtmlPrefix(), "fortress/logistics-noprivs.htm");
				}
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player.getHtmlPrefix(), "fortress/logistics-1.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
			}
		}
		else if (actualCommand.equalsIgnoreCase("supply"))
		{
			if (player.getClan() != null && getFort().getOwnerClan() != null &&
					player.getClan() == getFort().getOwnerClan() && player.isClanLeader())
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				if (getFort().getSiege().getIsInProgress())
				{
					html.setFile(player.getHtmlPrefix(), "fortress/logistics-siege.htm");
				}
				else
				{
					int level = getFort().getSupplyLvL();
					if (level > 0)
					{
						html.setFile(player.getHtmlPrefix(), "fortress/logistics-supply.htm");
						// spawn box
						L2NpcTemplate BoxTemplate = NpcTable.getInstance().getTemplate(SUPPLY_BOX_IDS[level - 1]);
						L2MonsterInstance box = new L2MonsterInstance(IdFactory.getInstance().getNextId(), BoxTemplate);
						box.setCurrentHp(box.getMaxHp());
						box.setCurrentMp(box.getMaxMp());
						box.setHeading(0);
						//L2World.getInstance().storeObject(box);
						box.spawnMe(getX() - 23, getY() + 41, getZ());

						getFort().setSupplyLvL(0);
						getFort().saveFortVariables();
					}
					else
					{
						html.setFile(player.getHtmlPrefix(), "fortress/logistics-nosupply.htm");
					}
				}
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player.getHtmlPrefix(), "fortress/logistics-noprivs.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	@Override
	public void showChatWindow(L2PcInstance player)
	{
		showMessageWindow(player, 0);
	}

	private void showMessageWindow(L2PcInstance player, int val)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);

		String filename;

		if (val == 0)
		{
			filename = "fortress/logistics.htm";
		}
		else
		{
			filename = "fortress/logistics-" + val + ".htm";
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
