package ai.individual.AltarOfTalking;

import l2server.gameserver.Announcements;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author NO ONE
 */

public class AltarOfTalking extends Quest
{
    private static final int margueriteId = 80245;
    private static final String qn = "AltarOfTalking";
    private static Map<Integer, Boolean> spawnInfo = new HashMap<Integer, Boolean>(3);
    private static final int[] raidIds = {80248, 80247, 80246};
    private static final int[] stoneIds = {20770, 20771, 20772};
    private static final int altarOfTalkingId = 80245;

    public AltarOfTalking(int questId, String name, String descr)
    {
        super(questId, name, descr);

        addSpawn(margueriteId, -119771, 246318, -1237, 843, false, 0);


        addFirstTalkId(altarOfTalkingId);
        addStartNpc(altarOfTalkingId);
        addTalkId(altarOfTalkingId);

        for (int i : raidIds)
        {
            addKillId(i);
            spawnInfo.put(i, false);
        }
    }

    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player)
    {
        return "AltarOfTalking.html";
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        if (event.startsWith("trySpawnBoss"))
        {
            int bossId = Integer.valueOf(event.split(" ")[1]);
            int stoneId = 0;
            if (bossId == raidIds[0])
            {
                stoneId = stoneIds[0];
            }
            else if (bossId == raidIds[1])
            {
                stoneId = stoneIds[1];
            }
            else
            {
                stoneId = stoneIds[2];
            }

            if (stoneId == 0) //Cheating?
            {
                return null;
            }

            synchronized (spawnInfo)
            {
                if (!spawnInfo.get(bossId))
                {
                    if (!player.destroyItemByItemId(qn, stoneId, 1, player, true))
                    {
                        return stoneId + "-no.html";
                    }
                    Announcements.getInstance().announceToAll("Marguerite: Mooooooh!");
                    spawnInfo.put(bossId, true); //Boss is spawned


                    L2Attackable boss =
                            (L2Attackable) addSpawn(bossId, npc.getX(), npc.getY() + 200, npc.getZ(), 0, false, 0,
                                    true);
                    boss.setIsRunning(true);
                    boss.setTarget(player);
                    boss.addDamageHate(player, 9999, 9999);
                    boss.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
                }
            }
        }

        return super.onAdvEvent(event, npc, player);
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        synchronized (spawnInfo)
        {
            spawnInfo.put(npc.getNpcId(), false);

        }
        Announcements.getInstance().announceToAll("Marguerite: MOOOOOOOOOH!");
        return super.onKill(npc, player, isPet);
    }

    public static void main(String[] args)
    {
        new AltarOfTalking(-1, qn, "ai/individual");
    }
}
