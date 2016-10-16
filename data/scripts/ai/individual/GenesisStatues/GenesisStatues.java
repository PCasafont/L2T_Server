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

package ai.individual.GenesisStatues;

import java.util.HashMap;
import java.util.Map;

import ai.group_template.L2AttackableAIScript;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author LasTravel
 *         <p>
 *         Genesis Statues AI
 */

public class GenesisStatues extends L2AttackableAIScript
{
    private static final int[] statues = {33138, 33139, 33140};
    private static final int[] keepers = {23038, 23039, 23040};
    private static final L2Skill blessingOfGarden = SkillTable.getInstance().getInfo(14200, 1);
    private static Map<Integer, Long> spawns = new HashMap<Integer, Long>(3);

    public GenesisStatues(int id, String name, String descr)
    {
        super(id, name, descr);

        for (int statues : this.statues)
        {
            addTalkId(statues);
            addStartNpc(statues);

            this.spawns.put(statues, 0L);
        }

        for (int keepers : this.keepers)
        {
            addKillId(keepers);
        }
    }

    @Override
    public String onTalk(L2Npc npc, L2PcInstance player)
    {
        long currentTime = System.currentTimeMillis();
        if (!this.spawns.containsKey(npc.getNpcId()) || this.spawns.get(npc.getNpcId()) + 3600000 > currentTime)
        {
            //final SimpleDateFormat dateFormatter = new SimpleDateFormat("[EEEE d MMMMMMM] @ k:m:s: ");

            //player.sendMessage("Magic will happen again on the " + dateFormatter.format(this.spawns.get(npc.getNpcId()) + 3600000) + ".");
            return npc.getNpcId() + "-no.html";
        }
        else
        {
            this.spawns.put(npc.getNpcId(), currentTime);

            L2MonsterInstance angelStatue =
                    (L2MonsterInstance) addSpawn(npc.getNpcId() - 10100, player.getX(), player.getY(), player.getZ(), 0,
                            false, 0, false);
            angelStatue.setTarget(player);
            angelStatue.addDamageHate(player, 500, 99999);
            angelStatue.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
        }
        return super.onTalk(npc, player);
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
    {
        this.blessingOfGarden.getEffects(killer, killer);

        return super.onKill(npc, killer, isPet);
    }

    public static void main(String[] args)
    {
        new GenesisStatues(-1, "GenesisStatues", "ai/individual");
    }
}
