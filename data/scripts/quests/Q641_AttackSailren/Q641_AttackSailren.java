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

package quests.Q641_AttackSailren;

import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.quest.State;
import l2server.util.Rnd;

/**
 * @author UnAfraid
 */
public class Q641_AttackSailren extends Quest
{
	// Quest
	public static String qn = "641_AttackSailren";
	public static boolean DEBUG = false;
	public static int DROP_CHANCE = 40;

	// NPC
	public static int statue = 32109;
	public static int[] mobs = {22196, 22197, 22198, 22218, 22223, 22199};

	// Quest Items
	public static int GAZKH_FRAGMENT = 8782;
	public static int GAZKH = 8784;

	public Q641_AttackSailren(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addTalkId(this.statue);
		addStartNpc(this.statue);
		for (int npcId : this.mobs)
		{
			addKillId(npcId);
		}
	}

	public static String getNoQuestMsg(L2PcInstance player)
	{
		String DEFAULT_NO_QUEST_MSG =
				"<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
		final String result = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "noquest.htm");
		if (result != null && result.length() > 0)
		{
			return result;
		}

		return DEFAULT_NO_QUEST_MSG;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (DEBUG)
		{
			player.sendMessage("onAdvEvent: " + event + " npcId: " + npc.getNpcId());
		}

		String htmltext = getNoQuestMsg(player);

		QuestState st = player.getQuestState(qn);
		if (st != null)
		{
			if (event.equalsIgnoreCase("32109-1.htm"))
			{
				htmltext = "32109-2.htm";
			}
			else if (event.equalsIgnoreCase("32109-2.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
				htmltext = "32109-3.htm";
			}
			else if (event.equalsIgnoreCase("32109-5.htm"))
			{
				st.giveItems(GAZKH, 1);
				st.set("cond", "3");
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(true);
				htmltext = "32109-5.htm";
			}
		}

		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (DEBUG)
		{
			player.sendMessage("onTalk: " + npc.getNpcId());
		}

		String htmltext = getNoQuestMsg(player);
		if (npc.getNpcId() == this.statue)
		{
			QuestState st = player.getQuestState(qn);
			if (st == null)
			{
				st = newQuestState(player);
				st.set("cond", "0");
			}

			try
			{
				byte id = st.getState();
				String cond = st.get("cond");
				if (cond == null || cond == "0")
				{
					QuestState prevSt = player.getQuestState("126_TheNameOfEvil2");
					if (prevSt != null)
					{
						byte prevId = prevSt.getState();
						if (prevId != State.COMPLETED)
						{
							htmltext =
									"<html><body>You have to complete quest The Name of Evil 2 in order to begin this one!</body></html>";
						}
						else if (id == State.COMPLETED && st.getQuestItemsCount(GAZKH) == 1)
						{
							htmltext = "<html><body>This quest has already been completed.</body></html>";
						}
						else

						{
							htmltext = "32109-1.htm";
						}
					}
					else
					{
						htmltext =
								"<html><body>You have to complete quest The Name of Evil 2 in order to begin this one!</body></html>";
					}
				}
				else if (cond == "1")
				{
					if (st.getQuestItemsCount(GAZKH_FRAGMENT) >= 30)
					{
						st.takeItems(GAZKH_FRAGMENT, 30);
						st.set("cond", "2");
						st.playSound("ItemSound.quest_middle");
						htmltext = "32109-4.htm";
					}
					else
					{
						htmltext = "<html><body> Please come back once you have 30 Gazkh Fragments. </body></html>";
					}
				}
				else if (cond == "2")
				{
					startQuestTimer("32109-5.htm", 0, npc, player);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (DEBUG)
		{
			player.sendMessage("onKill: " + npc.getNpcId());
		}

		for (int npcId : this.mobs)
		{
			if (npc.getNpcId() == npcId)
			{
				QuestState st = player.getQuestState(qn);
				if (st == null)
				{
					return null;
				}
				else
				{
					try
					{
						int chance = Rnd.get(100);
						int cond = Integer.parseInt(st.get("cond"));
						if (cond == 1 && DROP_CHANCE >= chance)
						{
							st.giveItems(GAZKH_FRAGMENT, 1);
							st.playSound("ItemSound.quest_itemget");
						}
					}
					catch (NumberFormatException nfe)
					{
					}
				}
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new Q641_AttackSailren(641, qn, "Attack Sailren");
	}
}
