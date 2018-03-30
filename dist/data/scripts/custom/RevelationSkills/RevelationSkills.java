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

package custom.RevelationSkills;

import l2server.Config;
import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LasTravel
 */

public class RevelationSkills extends Quest {
	private static final String qn = "RevelationSkills";
	
	private static final int monkOfChaos = 33880;
	private static final int chaoticPomander = 37374;
	private static final int chaosPomanderDualClass = 37375;
	private static final long resetPrice = 100000000L;
	private static final int[] revelationSkills = {1904, 1907, 1912, 1914, 1917, 1920, 1922, 1925, 1996, 1997};
	
	public RevelationSkills(int questId, String name, String descr) {
		super(questId, name, descr);
		
		addStartNpc(monkOfChaos);
		addTalkId(monkOfChaos);
		
		if (Config.isServer(Config.TENKAI_LEGACY)) {
			addStartNpc(40005);
			addTalkId(40005);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player) {
		if (event.equalsIgnoreCase("show_skills")) {
			if (getRevelationCount(player) < 2) {
				String skillList = "<table>";
				
				L2Skill skillInfo = null;
				for (int id : revelationSkills) {
					if (player.getSkillLevelHash(id) != -1) {
						continue;
					}
					
					skillInfo = SkillTable.getInstance().getInfo(id, 1);
					if (skillInfo != null && skillInfo.getSkillType() != L2SkillType.NOTDONE) {
						skillList += "<tr><td><a action=\"bypass -h Quest RevelationSkills " + skillInfo.getId() + "\">" + skillInfo.getName() +
								"</a></td></tr>";
					}
				}
				skillList += "</table>";
				
				return HtmCache.getInstance()
						.getHtm(null, Config.DATA_FOLDER + "scripts/custom/RevelationSkills/skillList.html")
						.replace("%skillList%", skillList);
			}
			return HtmCache.getInstance()
					.getHtm(null, Config.DATA_FOLDER + "scripts/custom/RevelationSkills/skillList.html")
					.replace("%skillList%", "You can't learn more skills!");
		} else if (Util.isDigit(event)) {
			int skillId = Integer.valueOf(event);
			if (player.getSkillLevelHash(skillId) > -1) {
				player.sendMessage(
						"ERROR: Please contact with the server administrator and inform about this message: " + player.getObjectId() + 500);
				return "";
			}

			/*long promanderCount = player.getInventory().getInventoryItemCount(chaoticPomander, 0);

			long promanderDualClassCount = player.getInventory().getInventoryItemCount(chaosPomanderDualClass, 0);

			if (promanderCount > 0)
			{
				player.destroyItemByItemId(qn, chaoticPomander, 1, npc, true);
			}
			else if (promanderDualClassCount > 0)
			{
				player.destroyItemByItemId(qn, chaosPomanderDualClass, 1, npc, true);
			}
			else
			{
				player.sendMessage("You don't have chaos promander!");

				return "";
			}
			player.broadcastPacket(new InventoryUpdate());
			 */
			
			//Anti exploit
			if (getRevelationCount(player) < 2) {
				L2Skill newSkill = SkillTable.getInstance().getInfo(skillId, 1);
				
				//Msg
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.LEARNED_SKILL_S1);
				sm.addSkillName(newSkill);
				player.sendPacket(sm);
				
				//Add the skill
				player.addSkill(newSkill, true);
				player.sendSkillList();
				
				notifyEvent("show_skills", npc, player);
			}
		} else if (event.equalsIgnoreCase("reset_revelation")) {
			if (player.getAdena() >= resetPrice) {
				player.reduceAdena(qn, resetPrice, player, true);
				for (L2Skill skill : player.getAllSkills()) {
					if (skill == null) {
						continue;
					}
					
					if (Util.contains(revelationSkills, skill.getId())) {
						player.removeSkill(skill, true, true);
					}
				}
				
				long promanderCount = player.getInventory().getInventoryItemCount(chaoticPomander, 0);
				long promanderDualClassCount = player.getInventory().getInventoryItemCount(chaosPomanderDualClass, 0);
				if (promanderCount > 0) {
					player.destroyItemByItemId(qn, chaoticPomander, 1, npc, true);
				} else if (promanderDualClassCount > 0) {
					player.destroyItemByItemId(qn, chaosPomanderDualClass, 1, npc, true);
				}
			} else {
				player.sendMessage("You don't have enough adena!");
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player) {
		if (player.getCurrentClass().getLevel() < 85 && player.getCurrentClass().getParent() == null) {
			return "no.html";
		}
		
		return "main.html";
	}
	
	private int getRevelationCount(L2PcInstance player) {
		int skillCount = 0;
		
		List<Integer> haveSkills = new ArrayList<Integer>();
		for (L2Skill skill : player.getAllSkills()) {
			if (skill == null) {
				continue;
			}
			
			if (Util.contains(revelationSkills, skill.getId())) {
				haveSkills.add(skill.getId());
				skillCount++;
			}
		}
		return skillCount;
	}
	
	public static void main(String[] args) {
		new RevelationSkills(-1, qn, "custom");
	}
}
