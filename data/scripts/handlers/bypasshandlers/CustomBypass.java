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

import l2tserver.Config;
import l2tserver.gameserver.datatables.CharNameTable;
import l2tserver.gameserver.datatables.ClanTable;
import l2tserver.gameserver.datatables.ItemTable;
import l2tserver.gameserver.datatables.PledgeSkillTree;
import l2tserver.gameserver.datatables.SkillTable;
import l2tserver.gameserver.handler.IBypassHandler;
import l2tserver.gameserver.instancemanager.QuestManager;
import l2tserver.gameserver.model.L2Clan;
import l2tserver.gameserver.model.L2PledgeSkillLearn;
import l2tserver.gameserver.model.L2Skill;
import l2tserver.gameserver.model.actor.L2Npc;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.model.base.Race;
import l2tserver.gameserver.model.itemcontainer.PcInventory;
import l2tserver.gameserver.model.quest.Quest;
import l2tserver.gameserver.model.quest.QuestState;
import l2tserver.gameserver.network.clientpackets.Say2;
import l2tserver.gameserver.network.serverpackets.CreatureSay;
import l2tserver.gameserver.network.serverpackets.NpcHtmlMessage;
import l2tserver.gameserver.network.serverpackets.PartySmallWindowAll;
import l2tserver.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import l2tserver.gameserver.util.Util;

public class CustomBypass implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"titlecolor",
		"changesex",
		"changeclanname",
		"changecharname",
		"namecolor",
		"increaseclanlevel",
		"removeClanPenalty",
		"removePlayerClanPenalty"
	};
	
	public boolean useBypass(String command, L2PcInstance player, L2Npc target)
	{
		if (target == null || !Config.isServer(Config.TENKAI))
			return false;
		
		if (command.startsWith("titlecolor") || command.startsWith("namecolor"))
		{
			boolean isTitleColor = command.startsWith("titlecolor");
			int priceId = isTitleColor ? 4357 : Config.DONATION_COIN_ID;
			int priceAmount = isTitleColor ? 1000000 : Config.CHANGE_NAME_COLOR_PRICE;
			String whatChange = isTitleColor ? "Title" : "Name";
			String coinName = isTitleColor ? "Silver Shilen" : ItemTable.getInstance().getTemplate(Config.DONATION_COIN_ID).getName();
			
			String val = command.split(" ")[1];
			PcInventory inv = player.getInventory();
			
			if (((isTitleColor && !val.equalsIgnoreCase("FFFF77")) || (!isTitleColor && !val.equalsIgnoreCase("FFFFFF"))) && (inv.getItemByItemId(priceId) == null || inv.getItemByItemId(priceId).getCount() < priceAmount))
			{	
				player.sendPacket(new CreatureSay(target.getObjectId(), Say2.TELL, target.getName(), "You have not enough "+coinName+"..."));
				return false;
			}	
			
			if ((isTitleColor && !val.equalsIgnoreCase("FFFF77")) || (!isTitleColor && !val.equalsIgnoreCase("FFFFFF")))
				player.destroyItemByItemId("Change Title Color", priceId, priceAmount, player, true);
			
			if (isTitleColor)
				player.setTitleColor(val);
			else
			{
				Quest quest = QuestManager.getInstance().getQuest("CustomColorName");
				QuestState st = player.getQuestState("CustomColorName");
				if (st == null)
					st  = new QuestState(quest, player, (byte)0);
				
				if (val.equalsIgnoreCase("FFFFFF"))	//restore
				{
					if (st.getGlobalQuestVar("CustomColorName").length() > 0)
						st.deleteGlobalQuestVar("CustomColorName");
				}
				else
					st.saveGlobalQuestVar("CustomColorName", val);
				
				player.getAppearance().setNameColor(Integer.decode("0x" + val));	
			}	
			player.broadcastUserInfo();
			player.sendMessage("Special Services: " + whatChange + " color changed!");
			
		}
		else if (command.equalsIgnoreCase("removeClanPenalty"))	//From a clan
		{
			L2Clan clan = player.getClan();
			if (clan == null)
				return false;
			
			if (!player.destroyItemByItemId("SpecialServices", Config.DONATION_COIN_ID, Config.REMOVE_CLAN_PENALTY_FROM_CLAN_PRICE, player, true))
				return false;
			
			clan.setCharPenaltyExpiryTime(0);
			player.sendMessage("Special Services: Your clan penalty has been removed!");
		}
		else if (command.equalsIgnoreCase("removePlayerClanPenalty"))	//From a player
		{
			L2Clan clan = player.getClan();
			if (clan != null || player.getClanJoinExpiryTime() <= 0)
			{
				player.sendMessage("SpecialServices: You don't have any penalty!");
				return false;
			}
			
			if (!player.destroyItemByItemId("SpecialServices", Config.DONATION_COIN_ID, Config.REMOVE_CLAN_PENALTY_FROM_CLAN_PRICE, player, true))
				return false;
			
			player.setClanJoinExpiryTime(0);
			player.sendMessage("Special Services: Your personal clan penalty has been removed!");
		}
		else if (command.equalsIgnoreCase("changesex"))
		{
			if (player.getRace() == Race.Kamael || player.getRace() == Race.Ertheia)
			{
				player.sendMessage("Special Services: Sorry but I can't change your class sex!");
				return false;
			}
			
			if (!player.destroyItemByItemId("SpecialServices", Config.DONATION_COIN_ID, Config.CHANGE_SEX_PRICE, player, true))
				return false;
			
			player.getAppearance().setSex(player.getAppearance().getSex() ? false : true);
			player.broadcastUserInfo();
			
			player.sendMessage("Special Services: You changed your sex successfully!");
		}
		else if (command.startsWith("changeclanname") || command.startsWith("changecharname"))
		{
			boolean isCharName = command.startsWith("changecharname");
			int price = isCharName ? Config.CHANGE_CHAR_NAME_PRICE : Config.CHANGE_CLAN_NAME_PRICE;
			String changeWhat = isCharName ? "Character Name" : "Clan Name";
			
			if (command.equalsIgnoreCase("changeclanname") || command.equalsIgnoreCase("changecharname"))
			{	
				StringBuilder sb = new StringBuilder();
				sb.append("<html><body><center><br><tr><td>Change " + changeWhat+"</tr></td><br><br>");
				sb.append("Tired of your current "+changeWhat+"? Have in mind that this is a very big privilege, the name change has always been denied by the administrators!<br>");
				sb.append("But I can help you. This is not a recommended option, I would suggest you to create another "+changeWhat.split(" ")[0]+", but if you insist that much... it will be " +price+" " + ItemTable.getInstance().getTemplate(Config.DONATION_COIN_ID).getName() + ".<br>");
				sb.append("<center><tr><td><edit var=text width=130 height=11 length=26><br>");
				sb.append("<button value=\"Done\" action=\"bypass -h npc_"+target.getObjectId()+"_"+command + " $text\" back=\"l2ui_ct1.button_df\" width=65 height=20 fore=\"l2ui_ct1.button_df\"></button></td></tr><br>");
				sb.append("</center><br><Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h npc_"+target.getObjectId()+"_Chat title_color\">Back</Button></body></html>");
				player.sendPacket(new NpcHtmlMessage(0, sb.toString()));
			}
			else
			{
				String newName = command.substring(15);
				if (newName.isEmpty())
					return false;
				
				if (!isCharName)
				{
					if (player.getClan() == null || !player.isClanLeader())
					{
						player.sendMessage("You can't use this function!");
						return false;
					}
					
					if (!ClanTable.getInstance().setClanNameConditions(player, newName))
						return false;
				}
				else
				{
					if (!CharNameTable.getInstance().setCharNameConditions(player, newName))
						return false;
				}
				
				if (!player.destroyItemByItemId(changeWhat, Config.DONATION_COIN_ID, price, target, true))
					return false;
				
				String oldName = isCharName ? player.getName() : player.getClan().getName();
				
				if (isCharName)
				{
					player.setName(newName);
					player.store();
					CharNameTable.getInstance().addName(player);
					player.broadcastUserInfo();
					
					if (player.isInParty())
					{
						player.getParty().broadcastToPartyMembers(player, new PartySmallWindowDeleteAll());
						for (L2PcInstance member : player.getParty().getPartyMembers())
						{
							if (member == null)
								continue;
							if (member != player)
								member.sendPacket(new PartySmallWindowAll(member, player.getParty()));
						}
					}
				}
				else
				{
					player.getClan().setName(newName);
					player.getClan().updateClanInDB();
				}
				
				if (player.getClan() != null)
					player.getClan().broadcastClanStatus();
				
				player.sendMessage("Special Services: Your "+changeWhat+" has been changed.");
				
				//Log
				String log = (isCharName ? oldName : player.getName()) + " changed his "+changeWhat+": " + oldName + " > " + newName;
				Util.logToFile(log, "changeNamesLog", true);
			}
		}
		else if (command.equalsIgnoreCase("increaseclanlevel"))
		{
			L2Clan playerClan = player.getClan();
			if (playerClan == null || !player.isClanLeader())
			{
				player.sendMessage("Special Services: You can't use this function!");
				return false;
			}
			
			if (!player.destroyItemByItemId("SpecialServices", Config.DONATION_COIN_ID, Config.INCREASE_CLAN_LEVEL_PRICE, target, true))
				return false;
			
			//Now increase the clan level and give the skills..
			if (playerClan.getLevel() < 10)
				playerClan.changeLevel(10);
			
			//Add the skills
			while (PledgeSkillTree.getInstance().getAvailableSkills(player).length != 0)
			{
				L2PledgeSkillLearn[] skills = PledgeSkillTree.getInstance().getAvailableSkills(player);
				if (skills != null)
				{
					for (L2PledgeSkillLearn sk : skills)
					{
						L2Skill s = SkillTable.getInstance().getInfo(sk.getId(), sk.getLevel());
						if (s != null)
							playerClan.addNewSkill(s);
					}
				}
			}
			player.sendMessage("Special Services: Your clan level has been increased!");
		}
		return true;
	}
	
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}