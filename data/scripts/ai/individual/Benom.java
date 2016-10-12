/* This program is free software: you can redistribute it and/or modify it under
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

package ai.individual;

import ai.group_template.L2AttackableAIScript;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.DoorTable;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.instancemanager.GrandBossManager;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.L2SiegeClan;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.NpcSay;
import l2server.gameserver.network.serverpackets.SocialAction;
import l2server.gameserver.network.serverpackets.SpecialCamera;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.List;

/**
 * @author theOne
 */
public class Benom extends L2AttackableAIScript
{
    private L2Npc benom = null;
    private static final int benomId = 29054;
    private static final int dungeonGk = 35506;

    private static final int ALIVE = 0;
    private static final int DEAD = 1;

    private static final int runeCastleId = 8;

    private static int benomIsSpawned = 0;
    private static int benomWalkRouteStep = 0;

    private static final String[] benomSpeak = {
            "You should have finished me when you had the chance!!!",
            "I will crush all of you!!!",
            "I am not finished here, come face me!!!",
            "You cowards!!! I will torture each and everyone of you!!!"
    };

    private static final int[] walkInterval = {
            18000,
            17000,
            4500,
            16000,
            22000,
            14000,
            10500,
            14000,
            9500,
            12500,
            20500,
            14500,
            17000,
            20000,
            22000,
            11000,
            11000,
            20000,
            8000,
            5500,
            20000,
            18000,
            25000,
            28000,
            25000,
            25000,
            25000,
            25000,
            10000,
            24000,
            7000,
            12000,
            20000
    };

    private static final int[][] benomWalkRoutes = {
            {12565, -49739, -547},
            {11242, -49689, -33},
            {10751, -49702, 83},
            {10824, -50808, 316},
            {9084, -50786, 972},
            {9095, -49787, 1252},
            {8371, -49711, 1252},
            {8423, -48545, 1252},
            {9105, -48474, 1252},
            {9085, -47488, 972},
            {10858, -47527, 316},
            {10842, -48626, 75},
            {12171, -48464, -547},
            {13565, -49145, -535},
            {15653, -49159, -1059},
            {15423, -48402, -839},
            {15066, -47438, -419},
            {13990, -46843, -292},
            {13685, -47371, -163},
            {13384, -47470, -163},
            {14609, -48608, 346},
            {13878, -47449, 747},
            {12894, -49109, 980},
            {10135, -49150, 996},
            {12894, -49109, 980},
            {13738, -50894, 747},
            {14579, -49698, 347},
            {12896, -51135, -166},
            {12971, -52046, -292,},
            {15140, -50781, -442,},
            {15328, -50406, -603},
            {15594, -49192, -1059},
            {13175, -49153, -537}
    };

    public Benom(int questId, String name, String descr)
    {
        super(questId, name, descr);
        addTalkId(dungeonGk);
        addAggroRangeEnterId(benomId);
        addKillId(benomId);
        addSpawnId(benomId);
        addStartNpc(dungeonGk);
        data();
    }

    private void data()
    {
        int ownerId = CastleManager.getInstance().getCastleById(runeCastleId).getOwnerId();
        List<L2SiegeClan> attackerClans =
                CastleManager.getInstance().getCastleById(runeCastleId).getSiege().getAttackerClans();
        long siegeDate = CastleManager.getInstance().getCastleById(runeCastleId).getSiegeDate().getTimeInMillis();
        long benomRaidRoomSpawn = siegeDate - System.currentTimeMillis() - 86400000;
        long benomRaidSiegeSpawn = siegeDate - System.currentTimeMillis();

        long benomRaidInit = siegeDate - System.currentTimeMillis() + 90000000;
        startQuestTimer("BenomBossInit", benomRaidInit, null, null);

        if (ownerId != 0)
        {
            if (benomRaidSiegeSpawn < 0)
            {
                benomRaidSiegeSpawn = 1;
            }

            if (siegeDate - System.currentTimeMillis() > 0)
            {
                startQuestTimer("BenomRaidRoomSpawn", benomRaidRoomSpawn, null, null);
            }

            if (attackerClans.size() != 0)
            {
                startQuestTimer("BenomRaidSiegeSpawn", benomRaidSiegeSpawn, null, null);
            }
        }
    }

    @Override
    public String onTalk(L2Npc npc, L2PcInstance player)
    {
        if (npc.getCastle().getSiege().getIsInProgress())
        {
            npc.showChatWindow(player, 1);
        }
        else
        {
            if (benomIsSpawned == 1)
            {
                player.teleToLocation(12589, -49044, -3008);
            }
            else
            {
                player.sendMessage("Dungeon is available only 24 hours before a siege war!");
            }
        }
        return null;
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        int benomStatus = GrandBossManager.getInstance().getBossStatus(benomId);

        if (event.equals("BenomRaidRoomSpawn"))
        {
            if (benomIsSpawned == 0 && benomStatus == ALIVE)
            {
                benom = addSpawn(benomId, 12047, -49211, -3009, 0, false, 0);
            }

            benomIsSpawned = 1;

            startQuestTimer("BenomBossDespawn", 93600000, npc, null);
        }
        else if (event.equals("BenomRaidSiegeSpawn"))
        {
            if (benomStatus == ALIVE)
            {
                if (benomIsSpawned == 0)
                {
                    benom = addSpawn(benomId, 11025, -49152, -537, 0, false, 0);
                    benomIsSpawned = 1;
                }
                else if (benomIsSpawned == 1)
                {
                    benom.teleToLocation(11025, -49152, -537);
                }

                startQuestTimer("BenomSpawnEffect", 100, npc, null);
            }
        }
        else if (event.equals("BenomSpawnEffect"))
        {
            npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
            npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 200, 0, 150, 0, 5000));
            npc.broadcastPacket(new SocialAction(npc.getObjectId(), 3));

            startQuestTimer("BenomWalk", 5000, npc, null);
            benomWalkRouteStep = 0;
        }
        else if (event.equals("Attacking"))
        {
            ArrayList<L2PcInstance> numPlayers = new ArrayList<>();
            for (L2PcInstance plr : npc.getKnownList().getKnownPlayers().values())
            {
                numPlayers.add(plr);
            }

            if (numPlayers.size() > 0)
            {
                L2PcInstance target = numPlayers.get(Rnd.get(numPlayers.size()));
                ((L2Attackable) npc).addDamageHate(target, 0, 999);
                npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);

                startQuestTimer("Attacking", 2000, npc, player);
            }
            else if (numPlayers.size() == 0)
            {
                startQuestTimer("BenomWalkFinish", 2000, npc, null);
            }
        }
        else if (event.equals("BenomWalk"))
        {
            if (benomWalkRouteStep == 33)
            {
                benomWalkRouteStep = 0;
                startQuestTimer("BenomWalk", 100, npc, null);
            }
            else
            {
                startQuestTimer("Talk", 100, npc, null);
                if (benomWalkRouteStep == 14)
                {
                    startQuestTimer("DoorOpen", 15000, null, null);
                    startQuestTimer("DoorClose", 23000, null, null);
                }
                if (benomWalkRouteStep == 32)
                {
                    startQuestTimer("DoorOpen", 500, null, null);
                    startQuestTimer("DoorClose", 4000, null, null);
                }

                npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

                int time = walkInterval[benomWalkRouteStep];
                int x = benomWalkRoutes[benomWalkRouteStep][0];
                int y = benomWalkRoutes[benomWalkRouteStep][1];
                int z = benomWalkRoutes[benomWalkRouteStep][2];

                npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(x, y, z, 0));
                benomWalkRouteStep++;
                startQuestTimer("BenomWalk", time, npc, null);
            }
        }
        else if (event.equals("BenomWalkFinish"))
        {
            if (npc.getCastle().getSiege().getIsInProgress())
            {
                cancelQuestTimer("Attacking", npc, player);
            }

            int x = benomWalkRoutes[benomWalkRouteStep][0];
            int y = benomWalkRoutes[benomWalkRouteStep][1];
            int z = benomWalkRoutes[benomWalkRouteStep][2];

            npc.teleToLocation(x, y, z);
            npc.setWalking();
            benomWalkRouteStep = 0;

            startQuestTimer("BenomWalk", 2200, npc, null);
        }
        else if (event.equals("Talk"))
        {
            if (Rnd.get(100) < 40)
            {
                npc.broadcastPacket(
                        new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), benomSpeak[Rnd.get(benomSpeak.length)]));
            }
        }
        else if (event.equals("BenomBossDespawn"))
        {
            GrandBossManager.getInstance().setBossStatus(benomId, ALIVE);
            benomIsSpawned = 0;
            npc.deleteMe();
        }
        else if (event.equals("BenomBossInit"))
        {
            GrandBossManager.getInstance().setBossStatus(benomId, ALIVE);
            benomIsSpawned = 0;
            data();
        }
        else if (event.equals("DoorOpen"))
        {
            DoorTable.getInstance().getDoor(20160005).openMe();
        }
        else if (event.equals("DoorClose"))
        {
            DoorTable.getInstance().getDoor(20160005).closeMe();
        }

        return super.onAdvEvent(event, npc, player);
    }

    @Override
    public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (npc.getCastle().getSiege().getIsInProgress())
        {
            cancelQuestTimer("BenomWalk", npc, null);
            cancelQuestTimer("BenomWalkFinish", npc, null);
            startQuestTimer("Attacking", 100, npc, player);
        }
        else if (((L2Attackable) npc).getMostHated() == null)
        {
            return null;
        }

        return super.onAggroRangeEnter(npc, player, isPet);
    }

    public String onKill(L2Npc npc, L2PcInstance player, Boolean isPet)
    {
        GrandBossManager.getInstance().setBossStatus(benomId, DEAD);

        cancelQuestTimer("BenomWalk", npc, null);
        cancelQuestTimer("BenomWalkFinish", npc, null);
        cancelQuestTimer("BenomBossDespawn", npc, null);
        cancelQuestTimer("Talk", npc, null);
        cancelQuestTimer("Attacking", npc, null);

        return super.onKill(npc, player, isPet);
    }

    @Override
    public String onSpawn(L2Npc npc)
    {
        npc.broadcastPacket(new NpcSay(npc.getObjectId(), 1, npc.getNpcId(),
                "Who dared to pretend on our castle? Go away or you will pay with your blood for it!"));

        return super.onSpawn(npc);
    }

    public static void main(String[] args)
    {
        new Benom(-1, "Benom", "ai");
    }
}
