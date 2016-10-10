package instances.DimensionalDoor.LabyrinthOfBelis;

import instances.DimensionalDoor.DimensionalDoor;

import java.util.ArrayList;
import java.util.List;

import l2server.Config;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.ScenePlayerDataTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2GuardInstance;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2NpcBufferInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.NpcSay;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;
import l2server.util.Rnd;
import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 *         <p>
 *         Source:
 *         - http://www.youtube.com/watch?v=XIX2i6n1i8U
 */

public class LabyrinthOfBelis extends L2AttackableAIScript
{
    private static final String _qn = "LabyrinthOfBelis";

    //Config
    private static final boolean _debug = false;
    private static final int _reuseMinutes = 1440;

    //Ids
    private static final int _instanceTemplateId = 178;
    private static final int _generatorId = 80312;
    private static final int _operativeId = 80313;
    private static final int _handymanId = 80314;
    private static final int _combatOfficer = 80310;
    private static final int _markOfBelis = 17615;
    private static final int _belisVerificationSystem = 80311;
    private static final int _bossId = 80315;
    private static final int[][] _operativeSpawns = {
            {-118589, 210903, -8592, 59724},
            {-118095, 211293, -8592, 4477},
            {-118125, 210983, -8592, 16358},
            {-118586, 211547, -8592, 33149},
            {-118273, 210870, -8592, 52342},
            {-118186, 211547, -8592, 42822},
            {-118427, 211322, -8592, 3497},
            {-118236, 211452, -8592, 39798}
    };

    public LabyrinthOfBelis(int questId, String name, String descr)
    {
        super(questId, name, descr);

        addTalkId(DimensionalDoor.getNpcManagerId());
        addStartNpc(DimensionalDoor.getNpcManagerId());
        addFirstTalkId(_combatOfficer);
        addTalkId(_combatOfficer);
        addStartNpc(_combatOfficer);
        addFirstTalkId(_belisVerificationSystem);
        addTalkId(_belisVerificationSystem);
        addStartNpc(_belisVerificationSystem);
        addKillId(_operativeId);
        addKillId(_handymanId);
        addAttackId(_operativeId);
        addAttackId(_handymanId);
        addKillId(_generatorId);
        addKillId(_bossId);
        addAggroRangeEnterId(_generatorId);
    }

    private class LabyrinthOfBelisWorld extends InstanceWorld
    {
        private L2PcInstance instancePlayer;
        private L2GuardInstance officer;
        private L2Npc generator;
        private L2Npc walkingGuard;
        private List<L2Npc> operativeList;
        private boolean isOfficerWalking;
        private boolean isGuardAttacked;

        private LabyrinthOfBelisWorld()
        {
            operativeList = new ArrayList<L2Npc>();
            isOfficerWalking = false;
            isGuardAttacked = false;
        }
    }

    private static void moveTo(L2Npc walker, int x, int y, int z, int h)
    {
        if (walker == null)
        {
            return;
        }
        walker.setIsRunning(true);
        walker.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(x, y, z, h));
    }

    @Override
    public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onAggroRangeEnter: " + player.getName());
        }

        InstanceWorld wrld = null;
        if (npc != null)
        {
            wrld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        }
        else
        {
            wrld = InstanceManager.getInstance().getPlayerWorld(player);
        }

        if (wrld != null && wrld instanceof LabyrinthOfBelisWorld)
        {
            LabyrinthOfBelisWorld world = (LabyrinthOfBelisWorld) wrld;
            if (npc.getNpcId() == _generatorId)
            {
                player.doDie(world.generator);
            }
        }
        return super.onAggroRangeEnter(npc, player, isPet);
    }

    @Override
    public final String onFirstTalk(L2Npc npc, L2PcInstance player)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onFirstTalk: " + player.getName());
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

        if (wrld != null && wrld instanceof LabyrinthOfBelisWorld)
        {
            LabyrinthOfBelisWorld world = (LabyrinthOfBelisWorld) wrld;
            if (npc.getNpcId() == _combatOfficer)
            {
                switch (world.status)
                {
                    case 0:
                        return "1.html";
                    case 2:
                        return "2.html";
                    case 4:
                        return "3.html";
                    case 6:
                        return "4.html";
                }
            }
            else if (npc.getNpcId() == _belisVerificationSystem)
            {
                return "33215.html";
            }
        }
        return super.onFirstTalk(npc, player);
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

        if (wrld != null && wrld instanceof LabyrinthOfBelisWorld)
        {
            LabyrinthOfBelisWorld world = (LabyrinthOfBelisWorld) wrld;
            if (event.equalsIgnoreCase("stage_1_start"))
            {
                world.instancePlayer = player;

                InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240001).openMe();

                world.generator =
                        addSpawn(_generatorId, -118253, 214706, -8584, 57541, false, 0, false, world.instanceId);
                world.generator.setIsMortal(false);

                world.officer = (L2GuardInstance) addSpawn(_combatOfficer, -119061, 211151, -8592, 142, false, 0, false,
                        world.instanceId);
                world.officer.setIsInvul(true);
                world.officer.setIsMortal(false);
                world.officer.setCanReturnToSpawnPoint(false);

                for (int[] spawn : _operativeSpawns)
                {
                    L2Npc operative = addSpawn(_operativeId, spawn[0], spawn[1], spawn[2], spawn[3], false, 0, false,
                            world.instanceId);
                    synchronized (world.operativeList)
                    {
                        world.operativeList.add(operative);
                    }
                }
            }
            else if (event.equalsIgnoreCase("stage_1_open_door"))
            {
                world.status = 1;

                InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240002).openMe();

                startQuestTimer("stage_all_officer_process", 2 * 1000, npc, null);
            }
            else if (event.equalsIgnoreCase("stage_1_end"))
            {
                world.status = 2;

                InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240003).openMe();
            }
            else if (event.equalsIgnoreCase("stage_2_start"))
            {
                world.isOfficerWalking = false;

                world.status = 3;

                InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240004).openMe();

                world.instancePlayer.sendPacket(new ExShowScreenMessage(1811199, 0, true, 5000));
            }
            else if (event.equalsIgnoreCase("stage_2_check_belis"))
            {
                long belisCount = player.getInventory().getInventoryItemCount(_markOfBelis, 0);
                if (belisCount >= 3)
                {
                    world.status = 4;

                    player.destroyItemByItemId(_qn, _markOfBelis, belisCount, player, true);

                    InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240005).openMe();
                }
            }
            else if (event.equalsIgnoreCase("stage_3_start"))
            {
                world.isOfficerWalking = false;

                world.status = 5;

                world.generator.setDisplayEffect(1);

                InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240006).openMe();

                world.instancePlayer.sendPacket(new ExShowScreenMessage(1811197, 0, true, 3000));

                world.officer.broadcastPacket(new NpcSay(world.officer.getObjectId(), 0, world.officer
                        .getTemplate().TemplateId, 1811217));
                world.officer.broadcastPacket(new NpcSay(world.officer.getObjectId(), 0, world.officer
                        .getTemplate().TemplateId, 1600025));
                world.officer.setIsInvul(false);
                world.officer.setIsMortal(true);

                startQuestTimer("stage_3_spawn_guard", 3000, npc, null);
                startQuestTimer("stage_3_generator_die", 1 * 60000, npc, null);
            }
            else if (event.equalsIgnoreCase("stage_3_spawn_guard"))
            {
                world.isGuardAttacked = false;

                world.walkingGuard = null;

                int guardId = 0;

                if (Rnd.get(2) == 0)
                {
                    guardId = _operativeId;
                }
                else
                {
                    guardId = _handymanId;
                }

                world.walkingGuard =
                        addSpawn(guardId, -116772, 213344, -8599, 24341, false, 0, false, world.instanceId);
                world.walkingGuard.broadcastPacket(new NpcSay(world.walkingGuard.getObjectId(), 0, world.walkingGuard
                        .getTemplate().TemplateId, guardId == _operativeId ? 1811196 : 1811195));

                world.instancePlayer
                        .sendPacket(
                                new ExShowScreenMessage(guardId == _operativeId ? 1811194 : 1811194, 0, true, 5000));

                startQuestTimer("stage_3_guard_attack", 1000, world.walkingGuard, null);
            }
            else if (event.equalsIgnoreCase("stage_3_guard_attack"))
            {
                if (world.generator != null && world.walkingGuard.getDistanceSq(world.generator) >= 100)
                {
                    if (world.status < 6 && !world.isGuardAttacked)
                    {
                        moveTo(world.walkingGuard, world.generator.getX(), world.generator.getY(), world.generator
                                .getZ(), world.generator.getHeading());
                    }

                    startQuestTimer("stage_3_guard_attack", 3000, world.walkingGuard, null);
                }
                else
                {
                    world.walkingGuard.setTarget(world.officer);
                    ((L2Attackable) world.walkingGuard).addDamageHate(world.officer, 500, 99999);
                    world.walkingGuard.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world.officer);
                }
            }
            else if (event.equalsIgnoreCase("stage_3_generator_die"))
            {
                world.generator.doDie(world.instancePlayer);
            }
            else if (event.equalsIgnoreCase("stage_last_start"))
            {
                world.status = 7;

                InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240008).openMe();

                world.instancePlayer.showQuestMovie(43);

                startQuestTimer("stage_last_spawn_boss", ScenePlayerDataTable.getInstance()
                        .getVideoDuration(43), npc, null);
            }
            else if (event.equalsIgnoreCase("stage_last_spawn_boss"))
            {
                addSpawn(_bossId, -118337, 212976, -8679, 24463, false, 0, false, world.instanceId);
            }
            else if (event.equalsIgnoreCase("stage_all_officer_process"))
            {
                //TODO Not good, but manage it is very hard
                if (world.status > 6)
                {
                    return "";
                }

                //Instance fail
                if (world.officer == null || world.officer.isDead())
                {
                    InstanceManager.getInstance().finishInstance(world.instanceId, true);
                    return "";
                }

                if (world.instancePlayer != null && !world.instancePlayer.isDead())
                {
                    switch (world.status)
                    {
                        case 1:
                        case 3:
                            if (!world.isOfficerWalking)
                            {
                                L2Object target = world.instancePlayer.getTarget();
                                if (target == null || !(target instanceof L2MonsterInstance) ||
                                        target instanceof L2MonsterInstance && ((L2MonsterInstance) target)
                                                .isDead())
                                {
                                    if (world.officer.getAI().getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)
                                    {
                                        world.officer.setIsRunning(true);
                                        world.officer.setTarget(world.instancePlayer);
                                        world.officer.getAI()
                                                .setIntention(CtrlIntention.AI_INTENTION_FOLLOW, world.instancePlayer);
                                    }
                                }
                                else
                                {
                                    if (target instanceof L2MonsterInstance)
                                    {
                                        if (!((L2MonsterInstance) target)
                                                .isInsideRadius(world.officer, 300, false, false))
                                        {
                                            world.officer.getAI()
                                                    .setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
                                                            new L2CharPosition(target
                                                                    .getX(), target.getY(), target.getZ(), 0));
                                        }
                                        else
                                        {
                                            world.officer.setTarget(target);
                                            ((L2Attackable) world.officer)
                                                    .addDamageHate((L2MonsterInstance) target, 500, 99999);
                                            world.officer.getAI()
                                                    .setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
                                        }
                                    }
                                }
                            }
                            break;

                        case 2:
                            moveTo(world.officer, -117069, 212520, -8592, 41214);
                            break;

                        case 4:
                            moveTo(world.officer, -117869, 214231, -8592, 57052);
                            break;

                        case 5:
                            if (world.officer.getDistanceSq(world.generator) >= 100)
                            {
                                moveTo(world.officer, world.generator.getX(), world.generator.getY(), world.generator
                                        .getZ(), world.generator.getHeading());
                            }
                            else
                            {
                                world.officer.setTarget(world.generator);
                                ((L2Attackable) world.officer).addDamageHate(world.generator, 500, 99999);
                                world.officer.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world.generator);
                            }
                            break;

                        case 6:
                            moveTo(world.officer, -119242, 213768, -8592, 24575);
                            break;
                    }
                }
                startQuestTimer("stage_all_officer_process", 2 * 1000, npc, null);
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
    public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onAttack: " + npc.getName());
        }

        InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpworld instanceof LabyrinthOfBelisWorld)
        {
            LabyrinthOfBelisWorld world = (LabyrinthOfBelisWorld) tmpworld;
            if (world.status == 5 && (npc.getNpcId() == _operativeId || npc.getNpcId() == _handymanId))
            {
                if (!world.isGuardAttacked)
                {
                    world.isGuardAttacked = true;
                    if (world.walkingGuard != null && !world.walkingGuard.isDead())
                    {
                        world.walkingGuard.stopMove(null);
                    }
                }
            }
        }
        return super.onAttack(npc, attacker, damage, isPet);
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onKill: " + npc.getName());
        }

        InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpworld instanceof LabyrinthOfBelisWorld)
        {
            LabyrinthOfBelisWorld world = (LabyrinthOfBelisWorld) tmpworld;
            switch (npc.getNpcId())
            {
                case _operativeId:

                    if (world.status == 1)
                    {
                        synchronized (world.operativeList)
                        {
                            if (world.operativeList.contains(npc))
                            {
                                world.operativeList.remove(npc);
                            }

                            if (world.operativeList.isEmpty())
                            {
                                startQuestTimer("stage_1_end", 1000, null, player);
                            }
                        }
                    }
                    else if (world.status == 5)
                    {
                        startQuestTimer("stage_3_spawn_guard", 1000, npc, null);
                    }
                    break;

                case _handymanId:
                    if (world.status == 3)
                    {
                        world.instancePlayer.sendPacket(new ExShowScreenMessage(1811199, 0, true, 5000));
                        if (Rnd.get(10) > 6)
                        {
                            ((L2MonsterInstance) npc).dropItem(player, _markOfBelis, 1);
                        }
                    }
                    else if (world.status == 5)
                    {
                        startQuestTimer("stage_3_spawn_guard", 1000, npc, null);
                    }
                    break;

                case _generatorId:
                    world.isOfficerWalking = false;

                    world.status = 6;

                    world.instancePlayer.sendPacket(new ExShowScreenMessage(1811198, 0, true, 5000));

                    InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240007).openMe();
                    break;

                case _bossId:
                    world.instancePlayer.showQuestMovie(44);

                    InstanceManager.getInstance()
                            .setInstanceReuse(world.instanceId, _instanceTemplateId, _reuseMinutes);
                    InstanceManager.getInstance().finishInstance(world.instanceId, true);

                    player.addItem(_qn, DimensionalDoor.getDimensionalDoorRewardId(), Rnd
                            .get(3 * DimensionalDoor.getDimensionalDoorRewardRate(), 6 * DimensionalDoor
                                    .getDimensionalDoorRewardRate()), player, true);
                    break;
            }
        }
        return "";
    }

    private final synchronized void enterInstance(L2PcInstance player)
    {
        InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
        if (world != null)
        {
            if (!(world instanceof LabyrinthOfBelisWorld))
            {
                player.sendPacket(SystemMessage
                        .getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
                return;
            }

            Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
            if (inst != null)
            {
                if (inst.getInstanceEndTime() > 300600 && world.allowed.contains(player.getObjectId()))
                {
                    player.deleteAllItemsById(_markOfBelis);
                    player.setInstanceId(world.instanceId);
                    player.teleToLocation(-119941, 211146, -8590, true);

                    L2NpcBufferInstance.giveBasicBuffs(player);
                }
            }
            return;
        }
        else
        {
            if (!_debug && !InstanceManager.getInstance()
                    .checkInstanceConditions(player, _instanceTemplateId, 1, 1, 99, Config.MAX_LEVEL))
            {
                return;
            }

            final int instanceId = InstanceManager.getInstance().createDynamicInstance(_qn + ".xml");
            world = new LabyrinthOfBelisWorld();
            world.instanceId = instanceId;
            world.status = 0;

            InstanceManager.getInstance().addWorld(world);

            world.allowed.add(player.getObjectId());

            player.stopAllEffectsExceptThoseThatLastThroughDeath();
            player.deleteAllItemsById(_markOfBelis);
            player.setInstanceId(instanceId);
            player.teleToLocation(-119941, 211146, -8590, true);

            L2NpcBufferInstance.giveBasicBuffs(player);

            startQuestTimer("stage_1_start", 4000, null, player);

            Log.fine(getName() + ": instance started: " + instanceId + " created by player: " + player.getName());
            return;
        }
    }

    public static void main(String[] args)
    {
        new LabyrinthOfBelis(-1, _qn, "instances/DimensionalDoor");
    }
}
