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

package quests.Q10324_FindingMagisterGallint;

import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.GlobalQuest;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.quest.State;
import l2server.gameserver.network.serverpackets.TutorialShowHtml;

/**
 * @author Pere
 */
public class Q10324_FindingMagisterGallint extends Quest
{
	// Quest
	public static String qn = "Q10324_FindingMagisterGallint";

	// NPC
	private int shannon = 32974;
	private int gallint = 32980;

	public Q10324_FindingMagisterGallint(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(shannon);
		addTalkId(shannon);
		addTalkId(gallint);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);

		if (st == null)
		{
			return htmltext;
		}

		if (npc.getNpcId() == shannon && event.equalsIgnoreCase("32974-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if (npc.getNpcId() == gallint && event.equalsIgnoreCase("32980-02.htm") && st.getInt("cond") == 1)
		{
			st.unset("cond");
			st.giveItems(57, 11000);
			st.addExpAndSp(1700, 2000);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
			player.sendPacket(new TutorialShowHtml(2, "..\\L2text\\QT_004_skill_01.htm"));

			// Main quests state
			player.setGlobalQuestFlag(GlobalQuest.STARTING, 5);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}

		if (npc.getNpcId() == shannon)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (canStart(player))
					{
						htmltext = "32974-01.htm";
					}
					else
					{
						htmltext = "32974-00.htm"; // TODO
					}
					break;
				case State.STARTED:
					htmltext = "32974-04.htm"; // TODO
					break;
				case State.COMPLETED:
					htmltext = "32974-05.htm"; // TODO
					break;
			}
		}
		else if (npc.getNpcId() == gallint && st.getInt("cond") == 1)
		{
			htmltext = "32980-01.htm";
		}
		return htmltext;
	}

	@Override
	public boolean canStart(L2PcInstance player)
	{
		return player.getGlobalQuestFlag(GlobalQuest.STARTING, 4) && player.getLevel() <= 20;
	}

	public static void main(String[] args)
	{
		new Q10324_FindingMagisterGallint(10324, qn, "Moving from Basic Training Ground to Admin Office.");
	}
}
