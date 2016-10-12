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

package quests.Q10323_GoingIntoARealWar;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.L2NpcWalkerAI;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.L2NpcWalkerNode;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.GlobalQuest;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.quest.State;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.NpcSay;
import l2server.gameserver.network.serverpackets.TutorialShowHtml;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class Q10323_GoingIntoARealWar extends Quest
{
    // Quest
    public static String qn = "Q10323_GoingIntoARealWar";

    // Items
    private int _key = 17574;

    // NPCs
    private int _yibein = 33464;
    private int _holden = 33194;
    private int _shannon = 32974;

    private int _guideId = 33006;
    private List<L2NpcWalkerNode> _guideRoute = new ArrayList<>();
    private int _guideFirstChatId = 1032304;
    private int _guideWaitChatId = 1032305;
    private int _guideLastChatId = 1032306;

    private int _trainerGuard = 33021;
    private int _teleporterGuard = 33193;
    private int _trainingGuard = 19141;
    private int _monster1 = 23113;
    private int _monster2 = 23114;

    public Q10323_GoingIntoARealWar(int questId, String name, String descr)
    {
        super(questId, name, descr);
        addStartNpc(_yibein);
        addTalkId(_yibein);
        addTalkId(_holden);
        addTalkId(_shannon);
        addFirstTalkId(_trainerGuard);
        addTalkId(_trainerGuard);
        addTalkId(_teleporterGuard);
        addKillId(_monster1);
        addKillId(_monster2);

        addEventId(_guideId, QuestEventType.ON_ARRIVED);
        addEventId(_guideId, QuestEventType.ON_PLAYER_ARRIVED);

        _guideRoute.add(new L2NpcWalkerNode(-110596, 253644, -1784, 0, "", true));
        _guideRoute.add(new L2NpcWalkerNode(-110486, 253523, -1776, 0, "", true));
        _guideRoute.add(new L2NpcWalkerNode(-110381, 253406, -1776, 0, "", true));
        _guideRoute.add(new L2NpcWalkerNode(-110308, 253249, -1800, 0, "", true));
        _guideRoute.add(new L2NpcWalkerNode(-110199, 252963, -1856, 0, "", true));
        _guideRoute.add(new L2NpcWalkerNode(-110127, 252685, -1960, 0, "", true));
        _guideRoute.add(new L2NpcWalkerNode(-110172, 252528, -1984, 0, "", true));
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, final L2PcInstance player)
    {
        String htmltext = event;
        QuestState st = player.getQuestState(qn);

        if (st == null)
        {
            return htmltext;
        }

        if (npc.getNpcId() == _yibein && event.equalsIgnoreCase("33464-03.htm"))
        {
            st.setState(State.STARTED);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");

            final L2Npc guide = addSpawn(_guideId, -110596, 253644, -1784, 0, false, 600000);
            L2NpcWalkerAI guideAI = new L2NpcWalkerAI(guide.new AIAccessor());
            guide.setAI(guideAI);
            guideAI.initializeRoute(_guideRoute, player);
            guideAI.setWaiting(true);

            NpcSay ns = new NpcSay(guide.getObjectId(), Say2.ALL_NOT_RECORDED, guide.getNpcId(), _guideFirstChatId);
            ns.addStringParameter(player.getName());
            guide.broadcastPacket(ns);

            // Delete in 1 min
            ThreadPoolManager.getInstance().scheduleAi(new Runnable()
            {
                @Override
                public void run()
                {
                    if (guide.isDecayed())
                    {
                        return;
                    }
                    if (!player.isInsideRadius(guide, 1000, false, false))
                    {
                        guide.deleteMe();
                    }
                    else
                    {
                        ThreadPoolManager.getInstance().scheduleAi(this, 60000);
                    }
                }
            }, 60000);

            st.giveItems(_key, 1);
        }
        else if (npc.getNpcId() == _shannon && event.equalsIgnoreCase("32974-02.htm") && st.getInt("cond") == 8)
        {
            st.unset("cond");
            st.takeItems(_key, -1);
            st.giveItems(57, 9000);
            st.addExpAndSp(300, 1500);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);

            // Main quests state
            player.setGlobalQuestFlag(GlobalQuest.STARTING, 4);
        }
        return htmltext;
    }

    @Override
    public String onFirstTalk(L2Npc npc, final L2PcInstance player)
    {
        String htmltext = getNoQuestMsg(player);
        final QuestState st = player.getQuestState(qn);

        if (st == null)
        {
            return htmltext;
        }

        if (npc.getNpcId() == _trainerGuard)
        {
            if (st.getInt("cond") == 3)
            {
                st.set("cond", "4");
                st.playSound("ItemSound.quest_middle");

                player.sendPacket(new ExShowScreenMessage(1032349, 0, true, 5000));

                if (player.isMageClass())
                {
                    st.giveItems(2509, 500);
                    htmltext = "33021-02.htm";
                }
                else
                {
                    st.giveItems(1835, 500);
                    htmltext = "33021-01.htm";
                }

                // Show this in 5 sec
                ThreadPoolManager.getInstance().scheduleAi(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        st.set("cond", "5");
                        //st.playSound("ItemSound.quest_middle");
                        player.sendPacket(new ExShowScreenMessage(1032350, 0, true, 5000));
                        player.sendPacket(new TutorialShowHtml(2, "..\\L2text\\QT_003_bullet_01.htm"));
                    }
                }, 5000);
            }
            else if (st.getInt("cond") == 4 || st.getInt("cond") == 5)
            {
                st.set("cond", "7");
                st.playSound("ItemSound.quest_middle");
                addSpawn(_monster1, -114047, 248425, -7872, 0, false, 0, false, player.getObjectId());
                addSpawn(_monster1, -114999, 248279, -7872, 0, false, 0, false, player.getObjectId());
                addSpawn(_monster1, -114974, 247961, -7872, 0, false, 0, false, player.getObjectId());
                addSpawn(_monster1, -114532, 248503, -7872, 0, false, 0, false, player.getObjectId());

                if (player.isMageClass())
                {
                    htmltext = "33021-06.htm";
                }
                else
                {
                    htmltext = "33021-05.htm";
                }
            }
        }

        return htmltext;
    }

    @Override
    public String onTalk(L2Npc npc, final L2PcInstance player)
    {
        String htmltext = getNoQuestMsg(player);
        QuestState st = player.getQuestState(qn);

        if (st == null)
        {
            return htmltext;
        }

        if (npc.getNpcId() == _yibein)
        {
            switch (st.getState())
            {
                case State.CREATED:
                    if (canStart(player))
                    {
                        htmltext = "33464-01.htm";
                    }
                    else
                    {
                        htmltext = "33464-00.htm"; // TODO
                    }
                    break;
                case State.STARTED:
                    htmltext = "33464-04.htm"; // TODO
                    break;
                case State.COMPLETED:
                    htmltext = "33464-05.htm"; // TODO
                    break;
            }
        }
        else if (npc.getNpcId() == _holden && st.getInt("cond") >= 1 && st.getInt("cond") <= 7)
        {
            if (st.getInt("cond") == 1)
            {
                st.set("cond", "2");
                st.playSound("ItemSound.quest_middle");
            }

            InstanceManager.getInstance().createInstance(player.getObjectId());
            addSpawn(_trainerGuard, -114875, 248336, -7872, 62000, false, 0, false, player.getObjectId());
            addSpawn(_teleporterGuard, -114014, 247680, -7872, 13665, false, 0, false, player.getObjectId());
            addSpawn(_trainingGuard, -114998, 248222, -7872, 0, false, 0, false, player.getObjectId());
            addSpawn(_trainingGuard, -114974, 247888, -7872, 0, false, 0, false, player.getObjectId());
            addSpawn(_trainingGuard, -114101, 248388, -7872, 0, false, 0, false, player.getObjectId());
            addSpawn(_trainingGuard, -114541, 248601, -7872, 0, false, 0, false, player.getObjectId());
            addSpawn(_monster1, -114047, 248425, -7872, 0, false, 0, false, player.getObjectId());
            addSpawn(_monster1, -114999, 248279, -7872, 0, false, 0, false, player.getObjectId());
            addSpawn(_monster1, -114974, 247961, -7872, 0, false, 0, false, player.getObjectId());
            addSpawn(_monster1, -114532, 248503, -7872, 0, false, 0, false, player.getObjectId());

            player.setInstanceId(player.getObjectId());
            player.teleToLocation(-113814, 247731, -7872, false);
            return null;
        }
        else if (npc.getNpcId() == _teleporterGuard && st.getInt("cond") == 8)
        {
            player.teleToLocation(-110415, 252423, -1992, false);
            player.setInstanceId(0);
            InstanceManager.getInstance().destroyInstance(player.getObjectId());
            return null;
        }
        else if (npc.getNpcId() == _shannon && st.getInt("cond") == 8)
        {
            htmltext = "32974-01.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        QuestState st = player.getQuestState(qn);
        if (st == null)
        {
            return null;
        }

        if (npc.getNpcId() == _monster1 || npc.getNpcId() == _monster2)
        {
            if (st.getInt("cond") == 2)
            {
                boolean allDead = true;
                for (L2Npc iNpc : InstanceManager.getInstance().getInstance(player.getObjectId()).getNpcs())
                {
                    if (iNpc instanceof L2MonsterInstance && iNpc.getObjectId() != npc.getObjectId() && !iNpc.isDead())
                    {
                        allDead = false;
                    }
                }
                if (allDead)
                {
                    st.set("cond", "3");
                    st.playSound("ItemSound.quest_middle");
                }
            }
            else if (st.getInt("cond") == 7)
            {
                boolean allDead = true;
                for (L2Npc iNpc : InstanceManager.getInstance().getInstance(player.getObjectId()).getNpcs())
                {
                    if (iNpc instanceof L2MonsterInstance && iNpc.getObjectId() != npc.getObjectId() && !iNpc.isDead())
                    {
                        allDead = false;
                    }
                }
                if (allDead)
                {
                    st.set("cond", "8");
                    st.playSound("ItemSound.quest_middle");
                }
            }
        }
        return null;
    }

    @Override
    public String onArrived(final L2NpcWalkerAI guideAI)
    {
        if (!guideAI.getActor().isInsideRadius(guideAI.getGuided(), guideAI.getWaitRadius() + 50, false, false) ||
                guideAI.getCurrentPos() == _guideRoute.size() - 1)
        {
            if (guideAI.getCurrentPos() == 1)
            {
                guideAI.setWaiting(true);
                return null;
            }
            int chatId = _guideLastChatId;
            if (guideAI.getCurrentPos() != _guideRoute.size() - 1)
            {
                guideAI.walkToGuided(40);
                chatId = _guideWaitChatId;
            }
            NpcSay ns =
                    new NpcSay(guideAI.getActor().getObjectId(), Say2.ALL_NOT_RECORDED, guideAI.getActor().getNpcId(),
                            chatId);
            ns.addStringParameter(guideAI.getGuided().getName());
            guideAI.getActor().broadcastPacket(ns);
            guideAI.setWaiting(true);
            return null;
        }

        guideAI.walkToLocation();
        guideAI.setWaiting(false);
        return null;
    }

    @Override
    public String onPlayerArrived(final L2NpcWalkerAI guideAI)
    {
        if (guideAI.getCurrentPos() == _guideRoute.size() - 1)
        {
            // Delete in 5 sec
            ThreadPoolManager.getInstance().scheduleAi(new Runnable()
            {
                @Override
                public void run()
                {
                    if (!guideAI.getActor().isDecayed())
                    {
                        guideAI.getActor().deleteMe();
                    }
                }
            }, 5000);
            return null;
        }
        guideAI.walkToLocation();
        guideAI.setWaiting(false);
        return null;
    }

    @Override
    public int getOnKillDelay(int npcId)
    {
        if (npcId == _monster1 || npcId == _monster2)
        {
            return 0;
        }
        return super.getOnKillDelay(npcId);
    }

    @Override
    public boolean canStart(L2PcInstance player)
    {
        return player.getGlobalQuestFlag(GlobalQuest.STARTING, 3) && player.getLevel() <= 20;
    }

    public static void main(String[] args)
    {
        new Q10323_GoingIntoARealWar(10323, qn, "Using soulshots and spiritshots to fight monsters.");
    }
}
