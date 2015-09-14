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
package quests.Q10363_RequestOfTheSeeker;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.GlobalQuest;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.quest.State;

/**
 * @author Pere
 */
public class Q10363_RequestOfTheSeeker extends Quest
{
	// Quest
	public static String qn = "Q10363_RequestOfTheSeeker";
	
	// NPC
	private int _nagel = 33450;
	private int _celin = 33451;
	private int[] _bodies = {32961, 32962, 32963, 32964};
	
	public Q10363_RequestOfTheSeeker(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(_nagel);
		addTalkId(_nagel);
		addTalkId(_celin);
		for (int npcId : _bodies)
			addEventId(npcId, QuestEventType.ON_SOCIAL_ACTION);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		if (npc.getNpcId() == _nagel && event.equalsIgnoreCase("33450-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if (npc.getNpcId() == _celin && event.equalsIgnoreCase("33451-03.htm") && st.getInt("cond") == 7)
		{
			st.unset("cond");
			st.giveItems(1060, 100);
			st.giveItems(57, 48000);
			st.giveItems(43, 1);
			st.addExpAndSp(70200, 8100);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
			
			// Main quests state
			player.setGlobalQuestFlag(GlobalQuest.YE_SAGIRA, 12);
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
		
		if (npc.getNpcId() == _nagel)
		{
			switch(st.getState())
			{
				case State.CREATED:
					if (canStart(player))
						htmltext = "33450-01.htm";
					else
						htmltext = "33450-00.htm";
					break;
				case State.STARTED:
					if (st.getInt("cond") == 1)
						htmltext = "33450-04.htm"; // TODO
					else if (st.getInt("cond") == 6)
					{
						htmltext = "33450-05.htm";
						st.set("cond", "7");
						st.playSound("ItemSound.quest_middle");
					}
					else
						htmltext = "33450-06.htm"; // TODO
					break;
				case State.COMPLETED:
					htmltext = "33450-07.htm"; // TODO
					break;
			}
		}
		else if (npc.getNpcId() == _celin && st.getInt("cond") == 7)
			htmltext = "33451-01.htm";
		return htmltext;
	}
	
	@Override
	public String onSocialAction(final L2Npc npc, final L2PcInstance player, final int actionId)
	{
		final QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;
		
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				if (actionId == 13)
				{
					boolean isCorpse = false;
					for (int id : _bodies)
					{
						if (id == npc.getNpcId())
						{
							isCorpse = true;
							break;
						}
					}
					
					if (isCorpse && st.getInt("cond") >= 1 && st.getInt("cond") <= 5
							&& npc.canInteract(player))
					{
						// TODO: Show according screen message
						st.set("cond", String.valueOf(st.getInt("cond") + 1));
						st.playSound("ItemSound.quest_middle");
						// TODO: Delete corpse and set a respawn time for it
					}
					else if (!npc.canInteract(player))
					{
						// TODO: Show according screen message
					}
				}
				else
				{
					// TODO: Show according screen message
					// TODO: Delete corpse and spawn mob in its place
				}
			}
		}, 2000L);
			
		
		return null;
	}
	
	@Override
	public boolean canStart(L2PcInstance player)
	{
		return player.getLevel() >= 12 && player.getLevel() <= 20 && player.getGlobalQuestFlag(GlobalQuest.YE_SAGIRA, 11);
	}

	public static void main(String[] args)
	{
		new Q10363_RequestOfTheSeeker(10363, qn, "Eliminating monsters in the Ye Sagira Ruins. Opportunity to obtain no-Grade armor.");
	}
}
