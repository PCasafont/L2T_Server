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

import java.util.ArrayList;
import java.util.List;

/**
 * @author LasTravel
 *         <p>
 *         Source: -https://www.youtube.com/watch?v=3BFgaYv3ibQ
 *         -http://l2wiki.com/Nornils_Garden
 *         -http://boards.lineage2.com/showthread.php?t=247492
 */

public class NornilsCave extends L2AttackableAIScript
{
    private static final String _qn = "NornilsCave";

    //Config
    private static final boolean _debug = false;

    //Ids
    private static final int _instanceTemplateId = 231;
    private static final int _bozId = 19298;
    private static final int _spiculaZeroId = 25901;
    private static final int[] _doorIds = {16200014, 16200015, 16200016, 16200201};
    private static final int[] _caveMobs = {19300, 19301, 19302, 19303, 19304};
    private static final int[][][] _bozSpawns = {
            {{-114849, 87180, -12686, 0}}, {

            {-115202, 87421, -12792, 40807},
            {-115689, 87421, -12792, 57124},
            {-115692, 86938, -12792, 8798},
            {-115209, 86937, -12792, 25130}
    }, {{-117340, 87179, -12697, 0}}, {{-119541, 86658, -12601, 15849}, {-119543, 87706, -12600, 49027}}
    };

    private class NornilsCaveWorld extends InstanceWorld
    {
        private boolean _firstBossAttack;
        private List<L2Npc> _bozMobs;
        private List<L2Npc> _roomMobs;
        private ArrayList<L2PcInstance> rewardedPlayers;

        private NornilsCaveWorld()
        {
            _firstBossAttack = false;
            _roomMobs = new ArrayList<L2Npc>();
            _bozMobs = new ArrayList<L2Npc>();
            rewardedPlayers = new ArrayList<L2PcInstance>();
        }
    }

    public NornilsCave(int questId, String name, String descr)
    {
        super(questId, name, descr);

        addTalkId(DimensionalDoor.getNpcManagerId());
        addStartNpc(DimensionalDoor.getNpcManagerId());

        for (int i : _caveMobs)
        {
            addKillId(i);
        }

        addAttackId(_spiculaZeroId);
        addKillId(_spiculaZeroId);

        addKillId(_bozId);
        addAttackId(_bozId);
    }

    @Override
    public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        if (_debug)
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

                InstanceManager.getInstance().getInstance(world.instanceId).getDoor(_doorIds[0]).openMe();

                for (int[] i : _bozSpawns[0])
                {
                    L2Npc boz = addSpawn(_bozId, i[0], i[1], i[2], i[3], false, 0, false, world.instanceId);
                    world._bozMobs.add(boz);
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

                    if (world._roomMobs.size() < 20)
                    {
                        for (int i = 0; i < Rnd.get(5, 8); i++)
                        {
                            L2Npc roomMob = addSpawn(_caveMobs[Rnd.get(_caveMobs.length)], npc.getX() + Rnd.get(50),
                                    npc.getY() + Rnd.get(50), npc.getZ(), npc.getHeading(), false, 0, true,
                                    world.instanceId);
                            world._roomMobs.add(roomMob);
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
                for (int[] i : _bozSpawns[1])
                {
                    L2Npc boz = addSpawn(_bozId, i[0], i[1], i[2], i[3], false, 0, true, world.instanceId);
                    world._bozMobs.add(boz);
                    boz.setIsParalyzed(true);
                    boz.setIsInvul(true);
                    notifyEvent("stage_all_spicula_spawns", boz, null);
                }
            }
            else if (event.equalsIgnoreCase("stage_3_start"))
            {
                for (int[] i : _bozSpawns[2])
                {
                    L2Npc boz = addSpawn(_bozId, i[0], i[1], i[2], i[3], false, 0, true, world.instanceId);
                    world._bozMobs.add(boz);
                    boz.setIsParalyzed(true);
                    boz.setIsInvul(true);
                    notifyEvent("stage_all_spicula_spawns", boz, null);
                }
            }
            else if (event.equalsIgnoreCase("stage_4_start"))
            {
                addSpawn(_spiculaZeroId, -119994, 87179, -12601, 64277, false, 0, false, world.instanceId);
                for (int[] i : _bozSpawns[3])
                {
                    L2Npc boz = addSpawn(_bozId, i[0], i[1], i[2], i[3], false, 0, false, world.instanceId);
                    world._bozMobs.add(boz);
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
        if (_debug)
        {
            Log.warning(getName() + ": onTalk: " + player.getName());
        }

        if (npc.getNpcId() == DimensionalDoor.getNpcManagerId())
        {
            return _qn + ".html";
        }

        return super.onTalk(npc, player);
    }

    @Override
    public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onAttack: " + npc.getName());
        }

        final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpWorld instanceof NornilsCaveWorld)
        {
            NornilsCaveWorld world = (NornilsCaveWorld) tmpWorld;
            if (npc.getNpcId() == _spiculaZeroId)
            {
                if (!world._firstBossAttack)
                {
                    world._firstBossAttack = true;
                    InstanceManager.getInstance().getInstance(world.instanceId).getDoor(_doorIds[3]).closeMe();
                }
            }
        }
        return "";
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onKill: " + npc.getName());
        }

        InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpworld instanceof NornilsCaveWorld)
        {
            NornilsCaveWorld world = (NornilsCaveWorld) tmpworld;
            if (Util.contains(_caveMobs, npc.getNpcId()))
            {
                synchronized (world._roomMobs)
                {
                    if (world._roomMobs.contains(npc))
                    {
                        world._roomMobs.remove(npc);
                    }

                    if (world._roomMobs.isEmpty())
                    {
                        for (L2Npc boz : world._bozMobs)
                        {
                            boz.setIsInvul(false);
                        }
                    }
                }
            }
            else if (npc.getNpcId() == _bozId)
            {
                QuestTimer activityTimer = getQuestTimer("stage_all_spicula_spawns", npc, null);
                if (activityTimer != null)
                {
                    activityTimer.cancel();
                }

                synchronized (world._bozMobs)
                {
                    if (world._bozMobs.contains(npc))
                    {
                        world._bozMobs.remove(npc);
                    }

                    if (world._bozMobs.isEmpty())
                    {
                        if (world.status == 1)
                        {
                            world.status = 2;

                            InstanceManager.getInstance().getInstance(world.instanceId).getDoor(_doorIds[0]).closeMe();
                            InstanceManager.getInstance().getInstance(world.instanceId).getDoor(_doorIds[1]).openMe();

                            notifyEvent("stage_2_start", npc, null);
                        }
                        else if (world.status == 2)
                        {
                            world.status = 3;

                            InstanceManager.getInstance().getInstance(world.instanceId).getDoor(_doorIds[1]).closeMe();
                            InstanceManager.getInstance().getInstance(world.instanceId).getDoor(_doorIds[2]).openMe();

                            notifyEvent("stage_3_start", npc, null);
                        }
                        else if (world.status == 3)
                        {
                            world.status = 4;

                            InstanceManager.getInstance().getInstance(world.instanceId).getDoor(_doorIds[2]).closeMe();
                            InstanceManager.getInstance().getInstance(world.instanceId).getDoor(_doorIds[3]).openMe();

                            notifyEvent("stage_4_start", npc, null);
                        }
                    }
                }
            }
            else if (npc.getNpcId() == _spiculaZeroId)
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
                            pMember.addItem(_qn, DimensionalDoor.getDimensionalDoorRewardId(),
                                    15, player, true);
                        }
                        else
                        {
                            pMember.sendMessage("Nice attempt, but you already got a reward!");
                        }
                    }
                }
                InstanceManager.getInstance().setInstanceReuse(world.instanceId, _instanceTemplateId, 6, 30);
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
            if (!_debug && !InstanceManager.getInstance()
                    .checkInstanceConditions(player, _instanceTemplateId, 7, 7, 99, Config.MAX_LEVEL))
            {
                return;
            }

            final int instanceId = InstanceManager.getInstance().createDynamicInstance(_qn + ".xml");
            world = new NornilsCaveWorld();
            world.instanceId = instanceId;
            world.status = 0;

            InstanceManager.getInstance().addWorld(world);

            List<L2PcInstance> allPlayers = new ArrayList<L2PcInstance>();
            if (_debug)
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
        new NornilsCave(-1, _qn, "instances/DimensionalDoor");
    }
}
