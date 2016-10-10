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

package quests.Q10364_ObligationsOfTheSeeker;

import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.GlobalQuest;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.quest.State;
import l2server.util.Rnd;

/**
 * @author Pere
 */
public class Q10364_ObligationsOfTheSeeker extends Quest
{
    // Quest
    public static String qn = "Q10364_ObligationsOfTheSeeker";

    // NPC
    private int _celin = 33451;
    private int _walter = 33452;
    private int _dep = 33453;
    private int _mob = 22994;

    // Item
    private int _paper = 17578; //TODO mob/s?

    public Q10364_ObligationsOfTheSeeker(int questId, String name, String descr)
    {
        super(questId, name, descr);
        addStartNpc(_celin);
        addTalkId(_celin);
        addTalkId(_walter);
        addTalkId(_dep);
        addKillId(_mob);
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

        if (npc.getNpcId() == _celin && event.equalsIgnoreCase("33451-03.htm"))
        {
            st.setState(State.STARTED);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        if (npc.getNpcId() == _walter && event.equalsIgnoreCase("33452-04.htm"))
        {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
        }
        else if (npc.getNpcId() == _dep && event.equalsIgnoreCase("33453-03.htm") && st.getInt("cond") == 3)
        {
            st.unset("cond");
            st.takeItems(_paper, -1);
            st.giveItems(1060, 50);
            st.giveItems(57, 55000);
            st.giveItems(19, 1);
            st.addExpAndSp(95000, 10000);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);

            // Main quests state
            player.setGlobalQuestFlag(GlobalQuest.YE_SAGIRA, 13);
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

        if (npc.getNpcId() == _celin)
        {
            switch (st.getState())
            {
                case State.CREATED:
                    if (canStart(player))
                    {
                        htmltext = "33451-01.htm";
                    }
                    else
                    {
                        htmltext = "33451-00.htm";
                    }
                    break;
                case State.STARTED:
                    htmltext = "33451-04.htm"; // TODO
                    break;
                case State.COMPLETED:
                    htmltext = "33451-07.htm"; // TODO
                    break;
            }
        }
        else if (npc.getNpcId() == _walter && st.getInt("cond") == 1)
        {
            htmltext = "33452-01.htm";
        }
        else if (npc.getNpcId() == _dep && st.getInt("cond") == 3)
        {
            htmltext = "33453-01.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        QuestState st = player.getQuestState(qn);
        if (st == null || st.getInt("cond") != 2 || Rnd.get(100) < 0) //TODO chance?
        {
            return null;
        }

        if (npc.getNpcId() == _mob && st.getQuestItemsCount(_paper) < 5)
        {
            st.giveItems(_paper, 1);
            st.playSound("ItemSound.quest_itemget");
        }

        if (st.getQuestItemsCount(_paper) == 5)
        {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
        }

        return null;
    }

    @Override
    public boolean canStart(L2PcInstance player)
    {
        return player.getLevel() >= 14 && player.getLevel() <= 25 && player
                .getGlobalQuestFlag(GlobalQuest.YE_SAGIRA, 12);
    }

    public static void main(String[] args)
    {
        new Q10364_ObligationsOfTheSeeker(10364, qn,
                "Collecting items from monsters in the Ye Sagira Ruins. Opportunity to obtain no-Grade armor.");
    }
}
