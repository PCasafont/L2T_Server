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

package quests.Q10328_RequestOfSealedEvilFragments;

import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.GlobalQuest;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.quest.State;

/**
 * @author Pere
 */
public class Q10328_RequestOfSealedEvilFragments extends Quest
{
	// Quest
	public static String qn = "Q10328_RequestOfSealedEvilFragments";
	
	// NPC
	private int _pantheon = 32972;
	private int _kakai = 30565;
	
	private int _evilFragment = 17577;
	
	public Q10328_RequestOfSealedEvilFragments(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(_pantheon);
		addTalkId(_pantheon);
		addTalkId(_kakai);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		if ((npc.getNpcId() == _pantheon) && event.equalsIgnoreCase("32972-04.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.giveItems(_evilFragment, 1);
			st.playSound("ItemSound.quest_accept");
		}
		else if ((npc.getNpcId() == _kakai) && (st.getInt("cond") == 1))
		{
			if (event.equalsIgnoreCase("30565-02.htm"))
			{
				st.takeItems(_evilFragment, -1);
			}
			else if (event.equalsIgnoreCase("30565-03.htm"))
			{
				st.unset("cond");
				st.takeItems(_evilFragment, -1);
				st.giveItems(57, 20000);
				st.addExpAndSp(13000, 4000);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
				
				// Main quests state
				player.setGlobalQuestFlag(GlobalQuest.STARTING, 9);
			}
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
		
		if (npc.getNpcId() == _pantheon)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (canStart(player))
						htmltext = "32972-01.htm";
					else
						htmltext = "32972-00.htm"; // TODO
					break;
				case State.STARTED:
					htmltext = "32972-05.htm"; // TODO
					break;
				case State.COMPLETED:
					htmltext = "32972-06.htm"; // TODO
					break;
			}
		}
		else if ((npc.getNpcId() == _kakai) && (st.getInt("cond") == 1))
			htmltext = "30565-01.htm";
		return htmltext;
	}
	
	@Override
	public boolean canStart(L2PcInstance player)
	{
		return player.getGlobalQuestFlag(GlobalQuest.STARTING, 8) && (player.getLevel() <= 20);
	}
	
	public static void main(String[] args)
	{
		new Q10328_RequestOfSealedEvilFragments(10328, qn, "Moving from the Museum to Admin Office.");
	}
}
