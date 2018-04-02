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
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.IAdminCommandHandler;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.PledgeSkillList;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.util.StringUtil;

import java.util.StringTokenizer;

/**
 * This class handles following admin commands:
 * - show_skills
 * - remove_skills
 * - skill_list
 * - skill_index
 * - add_skill
 * - remove_skill
 * - get_skills
 * - reset_skills
 * - give_all_skills
 * - remove_all_skills
 * - add_clan_skills
 *
 * @version $Revision: 1.2.4.7 $ $Date: 2005/04/11 10:06:02 $
 * Small fixes by Zoey76 24/02/2011
 */
public class AdminSkill implements IAdminCommandHandler {

	private static final String[] ADMIN_COMMANDS =
			{"admin_show_skills", "admin_remove_skills", "admin_skill_list", "admin_skill_index", "admin_add_skill", "admin_remove_skill",
					"admin_get_skills", "admin_reset_skills", "admin_give_all_skills", "admin_remove_all_skills", "admin_add_clan_skill",
					"admin_setskill"};

	private static Skill[] adminSkills;

	@Override
	public boolean useAdminCommand(String command, Player activeChar) {
		if (command.equals("admin_show_skills")) {
			showMainPage(activeChar);
		} else if (command.startsWith("admin_remove_skills")) {
			try {
				String val = command.substring(20);
				removeSkillsPage(activeChar, Integer.parseInt(val));
			} catch (StringIndexOutOfBoundsException e) {
			}
		} else if (command.startsWith("admin_skill_list")) {
			AdminHelpPage.showHelpPage(activeChar, "skills.htm");
		} else if (command.startsWith("admin_skill_index")) {
			try {
				String val = command.substring(18);
				AdminHelpPage.showHelpPage(activeChar, "skills/" + val + ".htm");
			} catch (StringIndexOutOfBoundsException e) {
			}
		} else if (command.startsWith("admin_add_skill")) {
			try {
				String val = command.substring(15);
				adminAddSkill(activeChar, val);
			} catch (Exception e) {
				activeChar.sendMessage("Usage: //add_skill <skill_id> <level>");
			}
		} else if (command.startsWith("admin_remove_skill")) {
			try {
				String id = command.substring(19);
				int idval = Integer.parseInt(id);
				adminRemoveSkill(activeChar, idval);
			} catch (Exception e) {
				activeChar.sendMessage("Usage: //remove_skill <skill_id>");
			}
		} else if (command.equals("admin_get_skills")) {
			adminGetSkills(activeChar);
		} else if (command.equals("admin_reset_skills")) {
			adminResetSkills(activeChar);
		} else if (command.equals("admin_give_all_skills")) {
			adminGiveAllSkills(activeChar);
		} else if (command.equals("admin_remove_all_skills")) {
			if (activeChar.getTarget() instanceof Player) {
				Player player = (Player) activeChar.getTarget();
				for (Skill skill : player.getAllSkills()) {
					player.removeSkill(skill);
				}
				activeChar.sendMessage("You removed all skills from " + player.getName());
				player.sendMessage("Admin removed all skills from you.");
				player.sendSkillList();
				player.broadcastUserInfo();
			}
		} else if (command.startsWith("admin_add_clan_skill")) {
			try {
				String[] val = command.split(" ");
				adminAddClanSkill(activeChar, Integer.parseInt(val[1]), Integer.parseInt(val[2]));
			} catch (Exception e) {
				activeChar.sendMessage("Usage: //add_clan_skill <skill_id> <level>");
			}
		} else if (command.startsWith("admin_setskill")) {
			String[] split = command.split(" ");
			int id = Integer.parseInt(split[1]);
			int lvl = Integer.parseInt(split[2]);
			Skill skill = SkillTable.getInstance().getInfo(id, lvl);
			activeChar.addSkill(skill);
			activeChar.sendSkillList();
			activeChar.sendMessage("You added yourself skill " + skill.getName() + "(" + id + ") level " + lvl);
		}
		return true;
	}

	/**
	 * This function will give all the skills that the target can learn at his/her level
	 *
	 * @param activeChar: the gm char
	 */
	private void adminGiveAllSkills(Player activeChar) {
		WorldObject target = activeChar.getTarget();
		Player player = null;
		if (target instanceof Player) {
			player = (Player) target;
		} else {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		//Notify player and admin
		activeChar.sendMessage("You gave " + player.giveAvailableSkills(true) + " skills to " + player.getName());
		player.sendSkillList();
	}

	@Override
	public String[] getAdminCommandList() {
		return ADMIN_COMMANDS;
	}

	private void removeSkillsPage(Player activeChar, int page) { //TODO: Externalize HTML
		WorldObject target = activeChar.getTarget();
		Player player = null;
		if (target instanceof Player) {
			player = (Player) target;
		} else {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return;
		}

		Skill[] skills = player.getAllSkills();

		int maxSkillsPerPage = 10;
		int maxPages = skills.length / maxSkillsPerPage;
		if (skills.length > maxSkillsPerPage * maxPages) {
			maxPages++;
		}

		if (page > maxPages) {
			page = maxPages;
		}

		int skillsStart = maxSkillsPerPage * page;
		int skillsEnd = skills.length;
		if (skillsEnd - skillsStart > maxSkillsPerPage) {
			skillsEnd = skillsStart + maxSkillsPerPage;
		}

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = StringUtil.startAppend(500 + maxPages * 50 + (skillsEnd - skillsStart + 1) * 50,
				"<html><body>" + "<table width=260><tr>" +
						"<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" +
						"<td width=180><center>Character Selection Menu</center></td>" +
						"<td width=40><button value=\"Back\" action=\"bypass -h admin_show_skills\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" +
						"</tr></table>" + "<br><br>" + "<center>Editing <font color=\"LEVEL\">",
				player.getName(),
				"</font></center>" + "<br><table width=270><tr><td>Lv: ",
				String.valueOf(player.getLevel()),
				" ",
				player.getCurrentClass().getName(),
				"</td></tr></table>" + "<br><table width=270><tr><td>Note: Dont forget that modifying players skills can</td></tr>" +
						"<tr><td>ruin the game...</td></tr></table>" + "<br><center>Click on the skill you wish to remove:</center>" + "<br>" +
						"<center><table width=270><tr>");

		for (int x = 0; x < maxPages; x++) {
			int pagenr = x + 1;
			StringUtil.append(replyMSG,
					"<td><a action=\"bypass -h admin_remove_skills ",
					String.valueOf(x),
					"\">Page ",
					String.valueOf(pagenr),
					"</a></td>");
		}

		replyMSG.append(
				"</tr></table></center>" + "<br><table width=270>" + "<tr><td width=80>Name:</td><td width=60>Level:</td><td width=40>Id:</td></tr>");

		for (int i = skillsStart; i < skillsEnd; i++) {
			StringUtil.append(replyMSG,
					"<tr><td width=80><a action=\"bypass -h admin_remove_skill ",
					String.valueOf(skills[i].getId()),
					"\">",
					skills[i].getName(),
					"</a></td><td width=60>",
					String.valueOf(skills[i].getLevel()),
					"</td><td width=40>",
					String.valueOf(skills[i].getId()),
					"</td></tr>");
		}

		replyMSG.append("</table>" + "<br><center><table>" + "Remove skill by ID :" + "<tr><td>Id: </td>" +
				"<td><edit var=\"id_to_remove\" width=110></td></tr>" + "</table></center>" +
				"<center><button value=\"Remove skill\" action=\"bypass -h admin_remove_skill $id_to_remove\" width=110 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>" +
				"<br><center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>" +
				"</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void showMainPage(Player activeChar) {
		WorldObject target = activeChar.getTarget();
		Player player = null;
		if (target instanceof Player) {
			player = (Player) target;
		} else {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getHtmlPrefix(), "admin/charskills.htm");
		adminReply.replace("%name%", player.getName());
		adminReply.replace("%level%", String.valueOf(player.getLevel()));
		adminReply.replace("%class%", player.getCurrentClass().getName());
		activeChar.sendPacket(adminReply);
	}

	private void adminGetSkills(Player activeChar) {
		WorldObject target = activeChar.getTarget();
		Player player = null;
		if (target instanceof Player) {
			player = (Player) target;
		} else {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		if (player.getName().equals(activeChar.getName())) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_USE_ON_YOURSELF));
		} else {
			Skill[] skills = player.getAllSkills();
			adminSkills = activeChar.getAllSkills();
			for (Skill skill : adminSkills) {
				activeChar.removeSkill(skill);
			}
			for (Skill skill : skills) {
				activeChar.addSkill(skill, true);
			}
			activeChar.sendMessage("You now have all the skills of " + player.getName() + ".");
			activeChar.sendSkillList();
		}
		showMainPage(activeChar);
	}

	private void adminResetSkills(Player activeChar) {
		WorldObject target = activeChar.getTarget();
		Player player = null;
		if (target instanceof Player) {
			player = (Player) target;
		} else {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		if (adminSkills == null) {
			activeChar.sendMessage("You must get the skills of someone in order to do this.");
		} else {
			Skill[] skills = player.getAllSkills();
			for (Skill skill : skills) {
				player.removeSkill(skill);
			}
			for (Skill skill : activeChar.getAllSkills()) {
				player.addSkill(skill, true);
			}
			for (Skill skill : skills) {
				activeChar.removeSkill(skill);
			}
			for (Skill skill : adminSkills) {
				activeChar.addSkill(skill, true);
			}
			player.sendMessage("[GM]" + activeChar.getName() + " updated your skills.");
			activeChar.sendMessage("You now have all your skills back.");
			adminSkills = null;
			activeChar.sendSkillList();
			player.sendSkillList();
		}
		showMainPage(activeChar);
	}

	private void adminAddSkill(Player activeChar, String val) {
		WorldObject target = activeChar.getTarget();
		Player player = null;
		if (target instanceof Player) {
			player = (Player) target;
		} else {
			showMainPage(activeChar);
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		StringTokenizer st = new StringTokenizer(val);
		if (st.countTokens() != 2) {
			showMainPage(activeChar);
		} else {
			Skill skill = null;
			try {
				String id = st.nextToken();
				String level = st.nextToken();
				int idval = Integer.parseInt(id);
				int levelval = Integer.parseInt(level);
				skill = SkillTable.getInstance().getInfo(idval, levelval);
			} catch (Exception e) {
				log.warn("", e);
			}
			if (skill != null) {
				String name = skill.getName();
				// Player's info.
				player.sendMessage("Admin gave you the skill " + name + ".");
				player.addSkill(skill, true);
				player.sendSkillList();
				// Admin info.
				activeChar.sendMessage("You gave the skill " + name + " to " + player.getName() + ".");
				if (Config.DEBUG) {
					log.debug("[GM]" + activeChar.getName() + " gave skill " + name + " to " + player.getName() + ".");
				}
				activeChar.sendSkillList();
			} else {
				activeChar.sendMessage("Error: there is no such skill.");
			}
			showMainPage(activeChar); //Back to start
		}
	}

	private void adminRemoveSkill(Player activeChar, int idval) {
		WorldObject target = activeChar.getTarget();
		Player player = null;
		if (target instanceof Player) {
			player = (Player) target;
		} else {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		Skill skill = SkillTable.getInstance().getInfo(idval, player.getSkillLevelHash(idval));
		if (skill != null) {
			String skillname = skill.getName();
			player.sendMessage("Admin removed the skill " + skillname + " from your skills list.");
			player.removeSkill(skill);
			//Admin information
			activeChar.sendMessage("You removed the skill " + skillname + " from " + player.getName() + ".");
			if (Config.DEBUG) {
				log.debug("[GM]" + activeChar.getName() + " removed skill " + skillname + " from " + player.getName() + ".");
			}
			activeChar.sendSkillList();
		} else {
			activeChar.sendMessage("Error: there is no such skill.");
		}
		removeSkillsPage(activeChar, 0); //Back to previous page
	}

	private void adminAddClanSkill(Player activeChar, int id, int level) {
		WorldObject target = activeChar.getTarget();
		Player player = null;
		if (target instanceof Player) {
			player = (Player) target;
		} else {
			showMainPage(activeChar);
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		if (!player.isClanLeader()) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER).addString(player.getName()));
			showMainPage(activeChar);
			return;
		}
		if (id < 370 || id > 391 || level < 1 || level > 3) {
			activeChar.sendMessage("Usage: //add_clan_skill <skill_id> <level>");
			showMainPage(activeChar);
			return;
		} else {
			Skill skill = SkillTable.getInstance().getInfo(id, level);
			if (skill != null) {
				String skillname = skill.getName();
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_SKILL_S1_ADDED);
				sm.addSkillName(skill);
				player.sendPacket(sm);
				final L2Clan clan = player.getClan();
				clan.broadcastToOnlineMembers(sm);
				clan.addNewSkill(skill);
				activeChar.sendMessage("You gave the Clan Skill: " + skillname + " to the clan " + clan.getName() + ".");

				clan.broadcastToOnlineMembers(new PledgeSkillList(clan));
				for (Player member : clan.getOnlineMembers(0)) {
					member.sendSkillList();
				}

				showMainPage(activeChar);
				return;
			} else {
				activeChar.sendMessage("Error: there is no such skill.");
				return;
			}
		}
	}
}
