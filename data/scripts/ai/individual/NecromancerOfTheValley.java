package ai.individual;

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.util.Rnd;
import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 */

public class NecromancerOfTheValley extends L2AttackableAIScript
{
    private static final int necromancerOfTheValley = 22858;

    public NecromancerOfTheValley(int questId, String name, String descr)
    {
        super(questId, name, descr);

        addKillId(necromancerOfTheValley);
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
    {
        if (Rnd.get(100) < 30)
        {
            L2Character attacker = isPet ? killer.getPet() : killer;

            if (attacker == null)
            {
                return super.onKill(npc, killer, isPet);
            }

            for (int a = 22818; a < 22820; a++)
            {
                L2Attackable minion = (L2Attackable) addSpawn(a, npc.getX(), npc.getY(), npc.getZ() + 10, npc
                        .getHeading(), false, 0, true);

                minion.setIsRunning(true);

                minion.addDamageHate(attacker, 0, 500);

                minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
            }
        }

        return super.onKill(npc, killer, isPet);
    }

    public static void main(String[] args)
    {
        new NecromancerOfTheValley(-1, "NecromancerOfTheValley", "ai");
    }
}
