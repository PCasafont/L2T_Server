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

package quests.Q10362_CertificationOfTheSeeker;

import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.GlobalQuest;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.quest.State;
import l2server.gameserver.network.serverpackets.ExQuestNpcLogList;

/**
 * @author Pere
 */
public class Q10362_CertificationOfTheSeeker extends Quest
{
	// Quest
	public static String qn = "Q10362_CertificationOfTheSeeker";

	// NPC
	private int _chesha = 33449;
	private int _nagel = 33450;
	private int _mob1 = 22992;
	private int _mob2 = 22991;

	public Q10362_CertificationOfTheSeeker(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(_chesha);
		addTalkId(_chesha);
		addTalkId(_nagel);
		addKillId(_mob1);
		addKillId(_mob2);
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

		if (npc.getNpcId() == _chesha && event.equalsIgnoreCase("33449-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if (npc.getNpcId() == _nagel && event.equalsIgnoreCase("33450-03.htm") && st.getInt("cond") == 3)
		{
			st.unset("cond");
			st.giveItems(1060, 50);
			st.giveItems(57, 43000);
			st.giveItems(49, 1);
			st.addExpAndSp(50000, 7000);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);

			// Main quests state
			player.setGlobalQuestFlag(GlobalQuest.YE_SAGIRA, 11);
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

		if (npc.getNpcId() == _chesha)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (canStart(player))
					{
						htmltext = "33449-01.htm";
					}
					else
					{
						htmltext = "33449-00.htm";
					}
					break;
				case State.STARTED:
					if (st.getInt("cond") == 1)
					{
						htmltext = "33449-04.htm"; // TODO
					}
					else if (st.getInt("cond") == 2)
					{
						htmltext = "33449-05.htm";
						st.set("cond", "3");
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						htmltext = "33449-06.htm"; // TODO
					}
					break;
				case State.COMPLETED:
					htmltext = "33449-07.htm"; // TODO
					break;
			}
		}
		else if (npc.getNpcId() == _nagel && st.getInt("cond") == 3)
		{
			htmltext = "33450-01.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null || st.getInt("cond") != 1)
		{
			return null;
		}

		if (npc.getNpcId() == _mob1 && st.getNpcLog(_mob1) < 10)
		{
			st.increaseNpcLog(_mob1);
			st.playSound("ItemSound.quest_itemget");
			player.sendPacket(new ExQuestNpcLogList(st));
		}
		else if (npc.getNpcId() == _mob2 && st.getNpcLog(_mob2) < 5)
		{
			st.increaseNpcLog(_mob2);
			st.playSound("ItemSound.quest_itemget");
			player.sendPacket(new ExQuestNpcLogList(st));
		}

		if (st.getNpcLog(_mob1) == 10 && st.getNpcLog(_mob2) == 5)
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}

		return null;
	}

	@Override
	public boolean canStart(L2PcInstance player)
	{
		return player.getLevel() >= 10 && player.getLevel() <= 20 &&
				player.getGlobalQuestFlag(GlobalQuest.YE_SAGIRA, 10);
	}

	public static void main(String[] args)
	{
		new Q10362_CertificationOfTheSeeker(10362, qn,
				"Eliminating monsters in the Ye Sagira Ruins. Opportunity to obtain no-Grade armor.");
	}
}
