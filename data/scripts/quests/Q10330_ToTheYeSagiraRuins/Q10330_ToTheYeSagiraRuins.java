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

package quests.Q10330_ToTheYeSagiraRuins;

import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.GlobalQuest;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.quest.State;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * @author Pere
 */
public class Q10330_ToTheYeSagiraRuins extends Quest
{
	// Quest
	public static String qn = "Q10330_ToTheYeSagiraRuins";

	// NPC
	private int atran = 33448;
	private int lakcis = 32977;

	public Q10330_ToTheYeSagiraRuins(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(atran);
		addTalkId(atran);
		addTalkId(lakcis);
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

		if (npc.getNpcId() == atran && event.equalsIgnoreCase("33448-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if (npc.getNpcId() == lakcis && event.equalsIgnoreCase("32977-03.htm") && st.getInt("cond") == 1)
		{
			st.unset("cond");
			st.giveItems(22, 1);
			st.giveItems(29, 1);
			st.giveItems(57, 28000);
			st.addExpAndSp(23000, 5800);
			st.playSound("ItemSound.quest_finish");
			player.sendPacket(new ExShowScreenMessage(11022202, 1, false, 10000));
			st.exitQuest(false);

			// Main quests state
			player.setGlobalQuestFlag(GlobalQuest.STARTING, 11);
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

		if (npc.getNpcId() == atran)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (canStart(player))
					{
						htmltext = "33448-01.htm";
					}
					else
					{
						htmltext = "33448-00.htm";
					}
					break;
				case State.STARTED:
					htmltext = "33448-04.htm"; // TODO
					break;
				case State.COMPLETED:
					htmltext = "33448-05.htm"; // TODO
					break;
			}
		}
		else if (npc.getNpcId() == lakcis && st.getInt("cond") == 1)
		{
			htmltext = "32977-01.htm";
		}
		return htmltext;
	}

	@Override
	public boolean canStart(L2PcInstance player)
	{
		return player.getLevel() >= 8 && player.getLevel() <= 20;
	}

	public static void main(String[] args)
	{
		new Q10330_ToTheYeSagiraRuins(10330, qn, "Going to the Ye Sagira Ruins. Opportunity to obtain no-Grade armor.");
	}
}
