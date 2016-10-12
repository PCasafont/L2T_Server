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

import l2server.gameserver.datatables.PlayerClassTable;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.ExChangeToAwakenedClass;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author Erlandys
 */
public final class L2AwakeNpcInstance extends L2Npc
{
	public L2AwakeNpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2AwakeNpcInstance);
	}

	@Override
	public void showChatWindow(L2PcInstance player)
	{
		int npcId = getTemplate().NpcId;

		String iHaveNothing =
				"<html><body>I have nothing to say to you<br>" + "<a action=\"bypass -h npc_" + getObjectId() +
						"_Quest\">Quest</a>" + "</body></html>";

		String mainText[] = {iHaveNothing, iHaveNothing};

		if (npcId > 33396 && npcId < 33405)
		{
			mainText[0] = getMainText(npcId - 33258, true);
			mainText[1] = getMainText(npcId - 33258, false);
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		if (!mainText.equals(""))
		{
			if (player.getLevel() >= 85 && player.getCurrentClass().getAwakeningClassId() != -1)
			{
				html.setHtml(mainText[0]);
			}
			else
			{
				html.setHtml(mainText[1]);
			}
		}
		else
		{
			html.setFile(player.getHtmlPrefix(), "npcdefault.htm");
		}

		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);

		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	private String getMainText(int classId, boolean canAwake)
	{
		String className[] = {
				"Sigel Knight",
				"Tyrr Warrior",
				"Othell Rogue",
				"Yul Archer",
				"Feoh Wizard",
				"Iss Enchanter",
				"Wynn Summoner",
				"Aeore Healer"
		};
		String ancientHero[] =
				{"Abelius", "Spyros", "Ashagen", "Cranigg", "Leister", "Soltkrieg", "Nabiarov", "Lakcis"};
		String htmlText = "";
		htmlText +=
				"<html><body scroll=\"no\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"292\" height=\"358\" background=\"L2UI_CH3.refinewnd_back_Pattern\"><tr><td>";
		htmlText +=
				"<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"292\" background=\"L2UI_CT1.HtmlWnd_DF_Texture" +
						className[classId - 139].split(" ")[1] + "\"><tr><td align=\"center\">";
		htmlText +=
				"<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"272\" height=\"338\"><tr><td height=\"90\"></td></tr>";
		if (canAwake)
		{
			htmlText +=
					"<tr><td align=\"center\">You are brave to take this road. Will you accept the help of the ancient hero " +
							ancientHero[classId - 139] + " and Awaken as an " + className[classId - 139] + "</td></tr>";
			htmlText += "<tr><td align=\"center\"><button value=\"Awaken\" action=\"bypass -h npc_" + getObjectId() +
					"_Awake " + classId +
					"\" width=200 height=30 back=\"L2UI_CT1.HtmlWnd_DF_Awake_Down\" fore=\"L2UI_CT1.HtmlWnd_DF_Awake\"></td></tr>";
		}
		else
		{
			htmlText +=
					"<tr><td align=\"center\">It is not possible for you to Awaken as an " + className[classId - 139] +
							". <br>";
			htmlText +=
					"<font color=\"af9878\">(Only characters level 85 or above who have completed their 3rd class transfer and possess the Scroll of Afterlife may Awaken.)</font></td></tr>";
		}
		htmlText += "</table></td></tr></table></td></tr></table></body></html>";
		return htmlText;
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("Awake"))
		{
			if (player.getWeightPenalty() >= 3)
			{
				player.sendPacket(
						SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_AWAKEN_DUE_TO_WEIGHT_LIMITS));
				return;
			}

			if (player.isMounted() || player.isTransformed())
			{
				player.sendPacket(SystemMessage
						.getSystemMessage(SystemMessageId.YOU_CANNOT_AWAKEN_WHILE_YOURE_TRANSFORMED_OR_RIDING));
				return;
			}

			int classId = PlayerClassTable.getInstance().getAwakening(player.getCurrentClass().getId());
			int statueClass = Integer.parseInt(command.split(" ")[1]);
			if (statueClass != player.getCurrentClass().getAwakeningClassId())
			{
				return;
			}

			player.setLastCheckedAwakeningClassId(classId);
			player.sendPacket(new ExChangeToAwakenedClass(classId));
		}
	}
}
