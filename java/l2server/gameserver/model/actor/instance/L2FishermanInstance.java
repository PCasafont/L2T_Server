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

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.datatables.SkillTreeTable;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2SkillLearn;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.ExAcquireSkillList;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.chars.L2NpcTemplate;

public class L2FishermanInstance extends L2MerchantInstance
{
	/**
	 * @param objectId
	 * @param template
	 */
	public L2FishermanInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2FishermanInstance);
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

		return "fisherman/" + pom + ".htm";
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

		return "fisherman/" + pom + ".htm";
	}

	public static void showFishSkillList(L2PcInstance player)
	{
		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player);
		ExAcquireSkillList asl = new ExAcquireSkillList(ExAcquireSkillList.SkillType.Fishing);

		int counts = 0;

		for (L2SkillLearn s : skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

			if (sk == null)
			{
				continue;
			}

			counts++;
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getSpCost(), 1);
		}

		if (counts == 0)
		{
			int minlevel = SkillTreeTable.getInstance().getMinLevelForNewSkill(player);

			if (minlevel > 0)
			{
				// No more skills to learn, come back when you level.
				SystemMessage sm =
						SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1);
				sm.addNumber(minlevel);
				player.sendPacket(sm);
			}
			else
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_MORE_SKILLS_TO_LEARN));
			}
		}
		else
		{
			player.sendPacket(asl);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
