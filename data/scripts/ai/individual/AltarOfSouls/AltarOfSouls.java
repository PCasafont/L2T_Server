package ai.individual.AltarOfSouls;

import java.util.HashMap;
import java.util.Map;

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;

/**
 * @author LasTravel
 */

public class AltarOfSouls extends Quest
{
    private static final String qn = "AltarOfSouls";
    private static Map<Integer, Boolean> spawnInfo = new HashMap<Integer, Boolean>(3);
    private static final int[] raidIds = {25944, 25943, 25942};
    private static final int[] stoneIds = {38572, 38573, 38574};
    private static final int altarOfSoulsId = 33920;

    public AltarOfSouls(int questId, String name, String descr)
    {
        super(questId, name, descr);

        addFirstTalkId(this.altarOfSoulsId);
        addStartNpc(this.altarOfSoulsId);
        addTalkId(this.altarOfSoulsId);

        for (int i : this.raidIds)
        {
            addKillId(i);
            this.spawnInfo.put(i, false);
        }
    }

    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player)
    {
        return "AltarOfSouls.html";
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        if (event.startsWith("trySpawnBoss"))
        {
            int bossId = Integer.valueOf(event.split(" ")[1]);
            int stoneId = 0;
            if (bossId == this.raidIds[0])
            {
                stoneId = this.stoneIds[0];
            }
            else if (bossId == this.raidIds[1])
            {
                stoneId = this.stoneIds[1];
            }
            else
            {
                stoneId = this.stoneIds[2];
            }

            if (stoneId == 0) //Cheating?
            {
                return null;
            }

            synchronized (this.spawnInfo)
            {
                if (!this.spawnInfo.get(bossId))
                {
                    if (!player.destroyItemByItemId(this.qn, stoneId, 1, player, true))
                    {
                        return stoneId + "-no.html";
                    }

                    this.spawnInfo.put(bossId, true); //Boss is spawned

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
        synchronized (this.spawnInfo)
        {
            this.spawnInfo.put(npc.getNpcId(), false);
        }

        return super.onKill(npc, player, isPet);
    }

    public static void main(String[] args)
    {
        new AltarOfSouls(-1, qn, "ai/individual");
    }
}
