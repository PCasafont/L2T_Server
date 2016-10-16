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

package custom.SquadSkills;

import l2server.Config;
import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2Clan.SubPledge;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author LasTravel
 *         <p>
 *         Noob way to learn squad skills, we're tired about the issues caused by the retail system
 *         <p>
 *         All the script looks a bit bad, ugly and whatever you want, but works fine.
 */

public class SquadSkills extends Quest
{
	private static final String qn = "SquadSkills";

	private static final int[] courtWizzards = {35648, 35649, 35650, 35651, 35652, 35653, 35654, 35655, 35656};
	private static final int[] supportUnitCaptain = {
			36382,
			36360,
			36344,
			36322,
			36308,
			36290,
			36275,
			36253,
			36237,
			36215,
			36199,
			36177,
			36163,
			36145,
			36132,
			36114,
			36099,
			36077,
			36061,
			36039,
			36025,
			36007,
			35992,
			35970,
			35954,
			35932,
			35918,
			35900,
			35885,
			35863,
			35849,
			35831,
			35818,
			35800,
			35785,
			35763,
			35749,
			35731,
			35716,
			35694,
			35680,
			35662
	};
	private static final List<SkillInfo> skillInfo = new ArrayList<SkillInfo>();

	public SquadSkills(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int i : courtWizzards)
		{
			addStartNpc(i);
			addTalkId(i);
		}

		for (int i : supportUnitCaptain)
		{
			addStartNpc(i);
			addTalkId(i);
		}

		loadSkillInfo();
	}

	private void loadSkillInfo()
	{
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "skillTrees/subPledgeSkillTree.xml");

		XmlDocument doc = new XmlDocument(file);
		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("skill_tree"))
			{
				for (XmlNode d : n.getChildren())
				{
					if (d.getName().equalsIgnoreCase("skill"))
					{
						int id = d.getInt("id");
						int level = d.getInt("level");
						int clanLevel = d.getInt("clanLevel");
						int reputation = d.getInt("reputation");
						int itemId = d.getInt("itemId");
						int count = d.getInt("count");

						skillInfo.add(new SkillInfo(id, level, clanLevel, reputation, itemId, count));
					}
				}
			}
		}
	}

	private class SkillInfo
	{
		private int skillId;
		private int level;
		private int clanLevel;
		private int reputation;
		private int itemId;
		private int count;

		private SkillInfo(int skillId, int level, int clanLevel, int reputation, int itemId, int count)
		{
			this.skillId = skillId;
			this.level = level;
			this.clanLevel = clanLevel;
			this.reputation = reputation;
			this.itemId = itemId;
			this.count = count;
		}

		private int getSkillid()
		{
			return skillId;
		}

		private int getSkillLevel()
		{
			return level;
		}

		private int getClanLevel()
		{
			return clanLevel;
		}

		private int getReputation()
		{
			return reputation;
		}

		private int getItemId()
		{
			return itemId;
		}

		private int getCount()
		{
			return count;
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		L2Clan playerClan = player.getClan();

		if (playerClan == null || !player.isClanLeader() || playerClan.getLeaderId() != player.getObjectId())
		{
			return null;
		}

		if (event.equalsIgnoreCase("show_subPledges")) //show all the available subPledges for learn skills
		{
			String pledgeInfo = "<table width=300>";

			//Add the main clan
			pledgeInfo +=
					"<tr><td><a action=\"bypass -h Quest " + qn + " show_available_skills_0\">" + playerClan.getName() +
							" (Main Clan)</a></td></tr>";

			//SubPledges
			SubPledge[] subPledges = playerClan.getAllSubPledges();

			for (SubPledge pledge : subPledges)
			{
				if (pledge == null || pledge.getId() == L2Clan.SUBUNIT_ACADEMY) //Don't show academy
				{
					continue;
				}
				pledgeInfo += "<tr><td><a action=\"bypass -h Quest " + qn + " show_available_skills_" + pledge.getId() +
						"\">" + pledge.getName() + "</a></td></tr>";
			}

			pledgeInfo += "</table>";

			return HtmCache.getInstance()
					.getHtm(null, Config.DATA_FOLDER + "scripts/custom/SquadSkills/subPledgeList.html")
					.replace("%subPledgeList%", pledgeInfo);
		}
		else if (event.startsWith("show_available_skills_")) //Show available skills for that pledge
		{
			int pledgeType = Integer.valueOf(event.replace("show_available_skills_", ""));

			String skillInfo = "<table width=300>";

			Map<Integer, Integer> skillToLearn = getAvailableLearnSkills(playerClan, pledgeType);

			if (skillToLearn.isEmpty())
			{
				skillInfo += "<tr><td><font color=LEVEL>There are no skill to learn at this moment!</font></td></tr>";
			}
			else
			{
				for (Entry<Integer, Integer> toLearn : skillToLearn.entrySet())
				{
					if (toLearn == null)
					{
						continue;
					}
					skillInfo +=
							"<tr><td><a action=\"bypass -h Quest " + qn + " try_learn_skill_" + toLearn.getKey() + "_" +
									toLearn.getValue() + "_" + pledgeType + "\">" +
									SkillTable.getInstance().getInfo(toLearn.getKey(), 1).getName() + " (Level: " +
									toLearn.getValue() + ")</a></td></tr>";
				}
			}
			skillInfo += "</table>";

			skillInfo += "<br><br> <a action=\"bypass -h Quest " + qn + " show_subPledges\">Back</a>";

			return HtmCache.getInstance().getHtm(null, Config.DATA_FOLDER + "scripts/custom/SquadSkills/skillList.html")
					.replace("%skillList%", skillInfo);
		}
		else if (event.startsWith("try_learn_skill_"))
		{
			String[] split = event.split("_");

			int skillId = Integer.valueOf(split[3]);
			int level = Integer.valueOf(split[4]);
			int pledgeType = Integer.valueOf(split[5]);

			//Be sure it's a valid skill
			Map<Integer, Integer> skillToLearn = getAvailableLearnSkills(playerClan, pledgeType);

			if (skillToLearn.get(skillId) == null || skillToLearn.get(skillId) != level)
			{
				return null; //cheating
			}

			L2Skill newSkill = SkillTable.getInstance().getInfo(skillId, level);

			if (newSkill == null)
			{
				return null;
			}

			SkillInfo info = null;

			for (SkillInfo skill : skillInfo)
			{
				if (skill.getSkillid() == skillId && skill.getSkillLevel() == level)
				{
					info = skill;

					if (skill.getReputation() > playerClan.getReputationScore())
					{
						player.sendPacket(SystemMessage
								.getSystemMessage(SystemMessageId.ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE));
						return null;
					}

					if (!player.destroyItemByItemId(qn, skill.getItemId(), skill.getCount(), player, false))
					{
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL));
						return null;
					}

					break;
				}
			}

			if (info == null)
			{
				return null;
			}

			//Take rep
			playerClan.takeReputationScore(info.getReputation(), true);
			SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
			cr.addNumber(info.getReputation());
			player.sendPacket(cr);

			playerClan.addNewSkill(newSkill, pledgeType);

			notifyEvent("show_available_skills_" + pledgeType, npc, player);
		}

		return super.onAdvEvent(event, npc, player);
	}

	private static Map<Integer, Integer> getAvailableLearnSkills(L2Clan clan, int pledgeType)
	{
		Map<Integer, Integer> availableSkills = new HashMap<Integer, Integer>();

		Map<Integer, Integer> pledgeSkills = new HashMap<Integer, Integer>();

		if (pledgeType == 0)
		{
			for (Entry<Integer, L2Skill> skill : clan.getMainClanSubSkills().entrySet())
			{
				pledgeSkills.put(skill.getKey(), skill.getValue().getLevelHash());
			}
		}
		else
		{
			for (L2Skill skill : clan.getSubPledge(pledgeType).getSkills())
			{
				pledgeSkills.put(skill.getId(), skill.getLevelHash());
			}
		}

		for (SkillInfo skillInfo : SquadSkills.skillInfo)
		{
			if (skillInfo == null)
			{
				continue;
			}

			if (skillInfo.getClanLevel() > clan.getLevel())
			{
				continue;
			}

			if (pledgeSkills.get(skillInfo.getSkillid()) == null) //Don't have this skill at any level
			{
				if (skillInfo.getSkillLevel() == 1)
				{
					availableSkills.put(skillInfo.getSkillid(), skillInfo.getSkillLevel());
				}
			}
			else
			{
				int currentLevel = pledgeSkills.get(skillInfo.getSkillid());
				if (currentLevel == 3)
				{
					continue;
				}
				availableSkills.put(skillInfo.getSkillid(), currentLevel + 1);
			}
		}

		return availableSkills;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		notifyEvent("show_subPledges", npc, player);
		return null;
	}

	public static void main(String[] args)
	{
		new SquadSkills(-1, qn, "custom");
	}
}
