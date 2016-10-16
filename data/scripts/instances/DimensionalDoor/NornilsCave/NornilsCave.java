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

package instances.DimensionalDoor.NornilsCave;

import java.util.ArrayList;
import java.util.List;

import ai.group_template.L2AttackableAIScript;
import instances.DimensionalDoor.DimensionalDoor;
import l2server.Config;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

/**
 * @author LasTravel
 *         <p>
 *         Source: -https://www.youtube.com/watch?v=3BFgaYv3ibQ
 *         -http://l2wiki.com/Nornils_Garden
 *         -http://boards.lineage2.com/showthread.php?t=247492
 */

public class NornilsCave extends L2AttackableAIScript
{
    private static final String qn = "NornilsCave";

    //Config
    private static final boolean debug = false;

    //Ids
    private static final int instanceTemplateId = 231;
    private static final int bozId = 19298;
    private static final int spiculaZeroId = 25901;
    private static final int[] doorIds = {16200014, 16200015, 16200016, 16200201};
    private static final int[] caveMobs = {19300, 19301, 19302, 19303, 19304};
    private static final int[][][] bozSpawns = {
            {{-114849, 87180, -12686, 0}}, {

            {-115202, 87421, -12792, 40807},
            {-115689, 87421, -12792, 57124},
            {-115692, 86938, -12792, 8798},
            {-115209, 86937, -12792, 25130}
    }, {{-117340, 87179, -12697, 0}}, {{-119541, 86658, -12601, 15849}, {-119543, 87706, -12600, 49027}}
    };

    private class NornilsCaveWorld extends InstanceWorld
    {
        private boolean firstBossAttack;
        private List<L2Npc> bozMobs;
        private List<L2Npc> roomMobs;
        private ArrayList<L2PcInstance> rewardedPlayers;

        private NornilsCaveWorld()
        {
            this.firstBossAttack = false;
            this.roomMobs = new ArrayList<L2Npc>();
            this.bozMobs = new ArrayList<L2Npc>();
            rewardedPlayers = new ArrayList<L2PcInstance>();
        }
    }

    public NornilsCave(int questId, String name, String descr)
    {
        super(questId, name, descr);

        addTalkId(DimensionalDoor.getNpcManagerId());
        addStartNpc(DimensionalDoor.getNpcManagerId());

        for (int i : this.caveMobs)
        {
            addKillId(i);
        }

        addAttackId(this.spiculaZeroId);
        addKillId(this.spiculaZeroId);

        addKillId(this.bozId);
        addAttackId(this.bozId);
    }

    @Override
    public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        if (this.debug)
        {
            Log.warning(getName() + ": onAdvEvent: " + event);
        }

        InstanceWorld wrld = null;
        if (npc != null)
        {
            wrld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        }
        else if (player != null)
        {
            wrld = InstanceManager.getInstance().getPlayerWorld(player);
        }
        else
        {
            Log.warning(getName() + ": onAdvEvent: Unable to get world.");
            return null;
        }

        if (wrld != null && wrld instanceof NornilsCaveWorld)
        {
            NornilsCaveWorld world = (NornilsCaveWorld) wrld;
            if (event.equalsIgnoreCase("stage_1_start"))
            {
                world.status = 1;

                InstanceManager.getInstance().getInstance(world.instanceId).getDoor(this.doorIds[0]).openMe();

                for (int[] i : this.bozSpawns[0])
                {
                    L2Npc boz = addSpawn(this.bozId, i[0], i[1], i[2], i[3], false, 0, false, world.instanceId);
                    world.bozMobs.add(boz);
                    boz.setIsParalyzed(true);
                    boz.setIsInvul(true);
                    notifyEvent("stage_all_spicula_spawns", boz, null);
                }
            }
            else if (event.equalsIgnoreCase("stage_all_spicula_spawns"))
            {
                if (!npc.isDead())
                {
                    if (!npc.isInvul())
                    {
                        npc.setIsInvul(true);
                    }

                    if (world.roomMobs.size() < 20)
                    {
                        for (int i = 0; i < Rnd.get(5, 8); i++)
                        {
                            L2Npc roomMob = addSpawn(this.caveMobs[Rnd.get(this.caveMobs.length)], npc.getX() + Rnd.get(50),
                                    npc.getY() + Rnd.get(50), npc.getZ(), npc.getHeading(), false, 0, true,
                                    world.instanceId);
                            world.roomMobs.add(roomMob);
                        }
                    }

                    int nextTime = 30000;
                    if (world.status == 2)
                    {
                        nextTime = 80000;
                    }

                    startQuestTimer("stage_all_spicula_spawns", nextTime, npc, null);
                }
            }
            else if (event.equalsIgnoreCase("stage_2_start"))
            {
                for (int[] i : this.bozSpawns[1])
                {
                    L2Npc boz = addSpawn(this.bozId, i[0], i[1], i[2], i[3], false, 0, true, world.instanceId);
                    world.bozMobs.add(boz);
                    boz.setIsParalyzed(true);
                    boz.setIsInvul(true);
                    notifyEvent("stage_all_spicula_spawns", boz, null);
                }
            }
            else if (event.equalsIgnoreCase("stage_3_start"))
            {
                for (int[] i : this.bozSpawns[2])
                {
                    L2Npc boz = addSpawn(this.bozId, i[0], i[1], i[2], i[3], false, 0, true, world.instanceId);
                    world.bozMobs.add(boz);
                    boz.setIsParalyzed(true);
                    boz.setIsInvul(true);
                    notifyEvent("stage_all_spicula_spawns", boz, null);
                }
            }
            else if (event.equalsIgnoreCase("stage_4_start"))
            {
                addSpawn(this.spiculaZeroId, -119994, 87179, -12601, 64277, false, 0, false, world.instanceId);
                for (int[] i : this.bozSpawns[3])
                {
                    L2Npc boz = addSpawn(this.bozId, i[0], i[1], i[2], i[3], false, 0, false, world.instanceId);
                    world.bozMobs.add(boz);
                    boz.setIsParalyzed(true);
                    boz.setIsInvul(true);
                    notifyEvent("stage_all_spicula_spawns", boz, null);
                }
            }
        }

        if (event.equalsIgnoreCase("enterToInstance"))
        {
            try
            {
                enterInstance(player);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return "";
    }

    @Override
    public final String onTalk(L2Npc npc, L2PcInstance player)
    {
        if (this.debug)
        {
            Log.warning(getName() + ": onTalk: " + player.getName());
        }

        if (npc.getNpcId() == DimensionalDoor.getNpcManagerId())
        {
            return this.qn + ".html";
        }

        return super.onTalk(npc, player);
    }

    @Override
    public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
    {
        if (this.debug)
        {
            Log.warning(getName() + ": onAttack: " + npc.getName());
        }

        final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpWorld instanceof NornilsCaveWorld)
        {
            NornilsCaveWorld world = (NornilsCaveWorld) tmpWorld;
            if (npc.getNpcId() == this.spiculaZeroId)
            {
                if (!world.firstBossAttack)
                {
                    world.firstBossAttack = true;
                    InstanceManager.getInstance().getInstance(world.instanceId).getDoor(this.doorIds[3]).closeMe();
                }
            }
        }
        return "";
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (this.debug)
        {
            Log.warning(getName() + ": onKill: " + npc.getName());
        }

        InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpworld instanceof NornilsCaveWorld)
        {
            NornilsCaveWorld world = (NornilsCaveWorld) tmpworld;
            if (Util.contains(this.caveMobs, npc.getNpcId()))
            {
                synchronized (world.roomMobs)
                {
                    if (world.roomMobs.contains(npc))
                    {
                        world.roomMobs.remove(npc);
                    }

                    if (world.roomMobs.isEmpty())
                    {
                        for (L2Npc boz : world.bozMobs)
                        {
                            boz.setIsInvul(false);
                        }
                    }
                }
            }
            else if (npc.getNpcId() == this.bozId)
            {
                QuestTimer activityTimer = getQuestTimer("stage_all_spicula_spawns", npc, null);
                if (activityTimer != null)
                {
                    activityTimer.cancel();
                }

                synchronized (world.bozMobs)
                {
                    if (world.bozMobs.contains(npc))
                    {
                        world.bozMobs.remove(npc);
                    }

                    if (world.bozMobs.isEmpty())
                    {
                        if (world.status == 1)
                        {
                            world.status = 2;

                            InstanceManager.getInstance().getInstance(world.instanceId).getDoor(this.doorIds[0]).closeMe();
                            InstanceManager.getInstance().getInstance(world.instanceId).getDoor(this.doorIds[1]).openMe();

                            notifyEvent("stage_2_start", npc, null);
                        }
                        else if (world.status == 2)
                        {
                            world.status = 3;

                            InstanceManager.getInstance().getInstance(world.instanceId).getDoor(this.doorIds[1]).closeMe();
                            InstanceManager.getInstance().getInstance(world.instanceId).getDoor(this.doorIds[2]).openMe();

                            notifyEvent("stage_3_start", npc, null);
                        }
                        else if (world.status == 3)
                        {
                            world.status = 4;

                            InstanceManager.getInstance().getInstance(world.instanceId).getDoor(this.doorIds[2]).closeMe();
                            InstanceManager.getInstance().getInstance(world.instanceId).getDoor(this.doorIds[3]).openMe();

                            notifyEvent("stage_4_start", npc, null);
                        }
                    }
                }
            }
            else if (npc.getNpcId() == this.spiculaZeroId)
            {
                if (player.isInParty())
                {
                    for (L2PcInstance pMember : player.getParty().getPartyMembers())
                    {
                        if (pMember == null || pMember.getInstanceId() != world.instanceId)
                        {
                            continue;
                        }

                        if (InstanceManager.getInstance().canGetUniqueReward(pMember, world.rewardedPlayers))
                        {
                            world.rewardedPlayers.add(pMember);
                            pMember.addItem(this.qn, DimensionalDoor.getDimensionalDoorRewardId(),
                                    Rnd.get(7 * DimensionalDoor.getDimensionalDoorRewardRate(),
                                            10 * DimensionalDoor.getDimensionalDoorRewardRate()), player, true);
                        }
                        else
                        {
                            pMember.sendMessage("Nice attempt, but you already got a reward!");
                        }
                    }
                }
                InstanceManager.getInstance().setInstanceReuse(world.instanceId, instanceTemplateId, 6, 30);
                InstanceManager.getInstance().finishInstance(world.instanceId, true);
            }
        }
        return "";
    }

    private final synchronized void enterInstance(L2PcInstance player)
    {
        InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
        if (world != null)
        {
            if (!(world instanceof NornilsCaveWorld))
            {
                player.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
                return;
            }

            Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
            if (inst != null)
            {
                if (inst.getInstanceEndTime() > 300600 && world.allowed.contains(player.getObjectId()))
                {
                    player.setInstanceId(world.instanceId);
                    player.teleToLocation(-111818, 87871, -13020, 867, true);
                }
            }
            return;
        }
        else
        {
            if (!this.debug && !InstanceManager.getInstance()
                    .checkInstanceConditions(player, instanceTemplateId, 7, 7, 99, Config.MAX_LEVEL))
            {
                return;
            }

            final int instanceId = InstanceManager.getInstance().createDynamicInstance(this.qn + ".xml");
            world = new NornilsCaveWorld();
            world.instanceId = instanceId;
            world.status = 0;

            InstanceManager.getInstance().addWorld(world);

            List<L2PcInstance> allPlayers = new ArrayList<L2PcInstance>();
            if (this.debug)
            {
                allPlayers.add(player);
            }
            else
            {
                allPlayers.addAll(player.getParty().getPartyMembers());
            }

            for (L2PcInstance enterPlayer : allPlayers)
            {
                if (enterPlayer == null)
                {
                    continue;
                }

                world.allowed.add(enterPlayer.getObjectId());

                enterPlayer.stopAllEffectsExceptThoseThatLastThroughDeath();
                enterPlayer.setInstanceId(instanceId);
                enterPlayer.teleToLocation(-111818, 87871, -13020, 867, true);
            }

            startQuestTimer("stage_1_start", 1000, null, player);

            Log.fine(getName() + ": instance started: " + instanceId + " created by player: " + player.getName());
            return;
        }
    }

    @Override
    public int getOnKillDelay(int npcId)
    {
        return 0;
    }

    public static void main(String[] args)
    {
        new NornilsCave(-1, qn, "instances/DimensionalDoor");
    }
}
