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
package quests.Q10321_QualificationsOfTheSeeker;

import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.GlobalQuest;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.quest.State;

/**
 * @author Pere
 */
public class Q10321_QualificationsOfTheSeeker extends Quest
{
	// Quest
	public static String qn = "Q10321_QualificationsOfTheSeeker";
	
	// NPC
	private int _theodore = 32975;
	private int _shannon = 32974;
	
	public Q10321_QualificationsOfTheSeeker(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(_theodore);
		addTalkId(_theodore);
		addTalkId(_shannon);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		if (npc.getNpcId() == _theodore && event.equalsIgnoreCase("32975-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if (npc.getNpcId() == _shannon && event.equalsIgnoreCase("32974-02.htm") && st.getInt("cond") == 1)
		{
			st.unset("cond");
			st.giveItems(57, 5000);
			st.addExpAndSp(40, 500);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
			
			// Main quests state
			player.setGlobalQuestFlag(GlobalQuest.STARTING, 2);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (npc.getNpcId() == _theodore)
		{
			switch(st.getState())
			{
				case State.CREATED:
					if (canStart(player))
						htmltext = "32975-01.htm";
					else
						htmltext = "32975-00.htm"; // TODO
					break;
				case State.STARTED:
					htmltext = "32975-04.htm"; // TODO
					break;
				case State.COMPLETED:
					htmltext = "32975-05.htm"; // TODO
					break;
			}
		}
		else if (npc.getNpcId() == _shannon && st.getInt("cond") == 1)
			htmltext = "32974-01.htm";
		return htmltext;
	}
	
	@Override
	public boolean canStart(L2PcInstance player)
	{
		return player.getGlobalQuestFlag(GlobalQuest.STARTING, 1) && player.getLevel() <= 20;
	}

	public static void main(String[] args)
	{
		new Q10321_QualificationsOfTheSeeker(10321, qn, "Going to the Training Grounds to receive combat training.");
	}
}
