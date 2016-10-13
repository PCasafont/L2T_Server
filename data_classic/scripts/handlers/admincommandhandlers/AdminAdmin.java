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

package handlers.admincommandhandlers;

import l2server.Config;
import l2server.gameserver.GmListTable;
import l2server.gameserver.ReloadableManager;
import l2server.gameserver.datatables.*;
import l2server.gameserver.handler.IAdminCommandHandler;
import l2server.gameserver.instancemanager.QuestManager;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.base.Experience;
import l2server.gameserver.model.olympiad.Olympiad;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;

import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * This class handles following admin commands:
 * - admin|admin1/admin2/admin3/admin4/admin5 = slots for the 5 starting admin menus
 * - gmliston/gmlistoff = includes/excludes active character from /gmlist results
 * - silence = toggles private messages acceptance mode
 * - diet = toggles weight penalty mode
 * - tradeoff = toggles trade acceptance mode
 * - reload = reloads specified component from multisell|skill|npc|htm|item|instancemanager
 * - set/set_menu/set_mod = alters specified server setting
 * - saveolymp = saves olympiad state manually
 * - manualhero = cycles olympiad and calculate new heroes.
 *
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2007/07/28 10:06:06 $
 */
public class AdminAdmin implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminAdmin.class.getName());

	private static final String[] ADMIN_COMMANDS = {
			"admin_admin",
			"admin_admin1",
			"admin_admin2",
			"admin_admin3",
			"admin_admin4",
			"admin_admin5",
			"admin_admin6",
			"admin_admin7",
			"admin_admin8",
			"admin_gmliston",
			"admin_gmlistoff",
			"admin_silence",
			"admin_diet",
			"admin_tradeoff",
			"admin_reload",
			"admin_set",
			"admin_set_mod",
			"admin_saveolymp",
			"admin_manualhero",
			"admin_sethero",
			"admin_endolympiad",
			"admin_setconfig",
			"admin_config_server"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_admin"))
		{
			showMainPage(activeChar, command);
		}
		else if (command.equals("admin_config_server"))
		{
			showConfigPage(activeChar);
		}
		else if (command.startsWith("admin_gmliston"))
		{
			GmListTable.getInstance().showGm(activeChar);
			activeChar.sendMessage("Registered into gm list");
			//AdminHelpPage.showHelpPage(activeChar,"gm_menu.htm");
		}
		else if (command.startsWith("admin_gmlistoff"))
		{
			GmListTable.getInstance().hideGm(activeChar);
			activeChar.sendMessage("Removed from gm list");
			//AdminHelpPage.showHelpPage(activeChar,"gm_menu.htm");
		}
		else if (command.startsWith("admin_silence"))
		{
			if (activeChar.isSilenceMode()) // already in message refusal mode
			{
				activeChar.setSilenceMode(false);
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MESSAGE_ACCEPTANCE_MODE));
			}
			else
			{
				activeChar.setSilenceMode(true);
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MESSAGE_REFUSAL_MODE));
			}
			//AdminHelpPage.showHelpPage(activeChar,"gm_menu.htm");
		}
		else if (command.startsWith("admin_saveolymp"))
		{
			Olympiad.getInstance().saveOlympiadStatus();
			activeChar.sendMessage("olympiad system saved.");
		}
		else if (command.startsWith("admin_endolympiad"))
		{
			try
			{
				Olympiad.getInstance().manualSelectHeroes();
			}
			catch (Exception e)
			{
				_log.warning("An error occured while ending olympiad: " + e);
			}
			activeChar.sendMessage("Heroes formed");
		}
		else if (command.startsWith("admin_manualhero") || command.startsWith("admin_sethero"))
		{
			L2PcInstance target = null;

			if (activeChar.getTarget() instanceof L2PcInstance)
			{
				target = (L2PcInstance) activeChar.getTarget();
				target.setHero(target.isHero() ? false : true);
			}
			else
			{
				target = activeChar;
				target.setHero(target.isHero() ? false : true);
			}
			target.broadcastUserInfo();
		}
		else if (command.startsWith("admin_diet"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				if (st.nextToken().equalsIgnoreCase("on"))
				{
					activeChar.setDietMode(true);
					activeChar.sendMessage("Diet mode on");
				}
				else if (st.nextToken().equalsIgnoreCase("off"))
				{
					activeChar.setDietMode(false);
					activeChar.sendMessage("Diet mode off");
				}
			}
			catch (Exception ex)
			{
				if (activeChar.getDietMode())
				{
					activeChar.setDietMode(false);
					activeChar.sendMessage("Diet mode off");
				}
				else
				{
					activeChar.setDietMode(true);
					activeChar.sendMessage("Diet mode on");
				}
			}
			finally
			{
				activeChar.refreshOverloaded();
			}
			//AdminHelpPage.showHelpPage(activeChar,"gm_menu.htm");
		}
		else if (command.startsWith("admin_tradeoff"))
		{
			try
			{
				String mode = command.substring(15);
				if (mode.equalsIgnoreCase("on"))
				{
					activeChar.setTradeRefusal(true);
					activeChar.sendMessage("Trade refusal enabled");
				}
				else if (mode.equalsIgnoreCase("off"))
				{
					activeChar.setTradeRefusal(false);
					activeChar.sendMessage("Trade refusal disabled");
				}
			}
			catch (Exception ex)
			{
				if (activeChar.getTradeRefusal())
				{
					activeChar.setTradeRefusal(false);
					activeChar.sendMessage("Trade refusal disabled");
				}
				else
				{
					activeChar.setTradeRefusal(true);
					activeChar.sendMessage("Trade refusal enabled");
				}
			}
			//AdminHelpPage.showHelpPage(activeChar,"gm_menu.htm");
		}
		else if (command.startsWith("admin_reload"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			String type = "";
			try
			{
				type = st.nextToken();
				if (type.startsWith("config"))
				{
					Config.load();
					activeChar.sendMessage("All Config Settings have been reloaded");
				}
				else if (type.equals("npc"))
				{
					NpcTable.getInstance().reloadAllNpc();
					QuestManager.getInstance().reload();
					activeChar.sendMessage("All NPCs have been reloaded");
				}
				else if (type.startsWith("npcwalkers"))
				{
					NpcWalkersTable.getInstance().load();
					activeChar.sendMessage("NPC Walker Routes have been reloaded");
				}
				else if (type.startsWith("access"))
				{
					AccessLevels.getInstance().reload();
					AdminCommandAccessRights.getInstance().reloadAdminCommandAccessRights();
					activeChar.sendMessage("Access Rights have been reloaded");
				}
				else if (type.startsWith("quests"))
				{
					QuestManager.getInstance().reload();
					activeChar.sendMessage("All Quests have been reloaded");
				}
				else if (type.startsWith("door"))
				{
					DoorTable.getInstance().reload();
					activeChar.sendMessage("All Doors have been reloaded");
				}
				else if (type.startsWith("messages"))
				{
					CoreMessageTable.getInstance().reload();
					activeChar.sendMessage("Core Messages reloaded");
				}
				else if (type.startsWith("exp"))
				{
					Experience.reload();
					activeChar.sendMessage("Experience reloaded");
				}
				else
				{
					activeChar.sendMessage(ReloadableManager.getInstance().reload(type));
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("An error occured while reloading " + type + " !");
				_log.warning("An error occured while reloading " + type + ": ");
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void showMainPage(L2PcInstance activeChar, String command)
	{
		int mode = 0;
		String filename = null;
		try
		{
			mode = Integer.parseInt(command.substring(11));
		}
		catch (Exception e)
		{
		}
		switch (mode)
		{
			case 1:
				filename = "main";
				break;
			case 2:
				filename = "game";
				break;
			case 3:
				filename = "effects";
				break;
			case 4:
				filename = "server";
				break;
			case 5:
				filename = "mods";
				break;
			case 6:
				filename = "char";
				break;
			case 7:
				filename = "gm";
				break;
			case 8:
				filename = "old";
				break;
			default:
				if (Config.GM_ADMIN_MENU_STYLE.equals("modern"))
				{
					filename = "main";
				}
				else
				{
					filename = "classic";
				}
				break;
		}
		AdminHelpPage.showHelpPage(activeChar, filename + "_menu.htm");
	}

	public void showConfigPage(L2PcInstance activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		StringBuilder replyMSG = new StringBuilder("<html><title>L2J :: Config</title><body>");
		replyMSG.append(
				"<center><table width=270><tr><td width=60><button value=\"Main\" action=\"bypass -h admin_admin\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td width=150>Config Server Panel</td><td width=60><button value=\"Back\" action=\"bypass -h admin_admin4\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table></center><br>");
		replyMSG.append("<center><table width=260><tr><td width=140></td><td width=40></td><td width=40></td></tr>");
		replyMSG.append("<tr><td><font color=\"00AA00\">Drop:</font></td><td></td><td></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate EXP</font> = " + Config.RATE_XP +
				"</td><td><edit var=\"param1\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setconfig RateXp $param1\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate SP</font> = " + Config.RATE_SP +
				"</td><td><edit var=\"param2\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setconfig RateSp $param2\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate Drop Spoil</font> = " + Config.RATE_DROP_SPOIL +
				"</td><td><edit var=\"param4\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setconfig RateDropSpoil $param4\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td width=140></td><td width=40></td><td width=40></td></tr>");
		replyMSG.append("<tr><td><font color=\"00AA00\">Enchant:</font></td><td></td><td></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Enchant Weapon</font> = " + Config.ENCHANT_CHANCE_WEAPON +
				"</td><td><edit var=\"param5\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setconfig EnchantChanceWeapon $param5\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Enchant Armor</font> = " + Config.ENCHANT_CHANCE_ARMOR +
				"</td><td><edit var=\"param6\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setconfig EnchantChanceArmor $param6\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Enchant Jewelry</font> = " + Config.ENCHANT_CHANCE_JEWELRY +
				"</td><td><edit var=\"param7\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setconfig EnchantChanceJewelry $param7\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append(
				"<tr><td><font color=\"LEVEL\">Enchant Element Stone</font> = " + Config.ENCHANT_CHANCE_ELEMENT_STONE +
						"</td><td><edit var=\"param8\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setconfig EnchantChanceElementStone $param8\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Enchant Element Crystal</font> = " +
				Config.ENCHANT_CHANCE_ELEMENT_CRYSTAL +
				"</td><td><edit var=\"param9\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setconfig EnchantChanceElementCrystal $param9\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append(
				"<tr><td><font color=\"LEVEL\">Enchant Element Jewel</font> = " + Config.ENCHANT_CHANCE_ELEMENT_JEWEL +
						"</td><td><edit var=\"param10\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setconfig EnchantChanceElementJewel $param10\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Enchant Element Energy</font> = " +
				Config.ENCHANT_CHANCE_ELEMENT_ENERGY +
				"</td><td><edit var=\"param11\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setconfig EnchantChanceElementEnergy $param11\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");

		replyMSG.append("</table></body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
}
