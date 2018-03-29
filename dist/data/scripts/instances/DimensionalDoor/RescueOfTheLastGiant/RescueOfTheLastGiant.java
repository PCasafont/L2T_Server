package instances.DimensionalDoor.RescueOfTheLastGiant;

import ai.group_template.L2AttackableAIScript;
import instances.DimensionalDoor.DimensionalDoor;
import l2server.Config;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.ScenePlayerDataTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2NpcBufferInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LasTravel
 *         <p>
 *         Source: http://www.youtube.com/watch?v=K8SSsVOYwRk
 */

public class RescueOfTheLastGiant extends L2AttackableAIScript
{
    private static final String qn = "RescueOfTheLastGiant";

    //Config
    private static final boolean debug = false;
    private static final int reuseMinutes = 1440;

    //Ids
    private static final int mob1 = 80294;
    private static final int mob2 = 80293;
    private static final int mob3 = 80296;
    private static final int mob4 = 80297;
    private static final int mob5 = 80298;
    private static final int mob6 = 80295;
    private static final int mob7 = 80299;
    private static final int mob8 = 80292;
    private static final int dummyNpc = 13310;
    private static final int controlDevice = 80290;
    private static final int bossId = 80291;
    private static final int instanceTemplateId = 503;
    private static final int[] herbs = {8605, 8602};
    private static final int[] allmobs = {mob1, mob2, mob3, mob3, mob4, mob5, mob6, mob7, mob8, bossId};
    private static final L2Skill powerHealSkill = SkillTable.getInstance().getInfo(14625, 1);

    private static final int[][] secondRoom1 = {{-107776, 209248}, {-108096, 209248}, {-107926, 209248}};

    private static final int[][] secondRoom2 = {
            {-108096, 209248},
            {-108314, 208699},
            {-107541, 208697},
            {-107776, 209248},
            {-108314, 209022},
            {-107542, 209024},
            {-108314, 208855},
            {-107926, 209248},
            {-108206, 209134},
            {-107541, 208857},
            {-107650, 209142}
    };

    private static final int[][] lastGuardSpawns =
            {{-108500, 211232}, {-108500, 211596}, {-107349, 211596}, {-107349, 211232}};

    private class RescueOfTheLastGiantWorld extends InstanceWorld
    {
        private L2PcInstance instancedPlayer;
        private int mobToAttack;
        private int specificMobClass;
        private boolean playerFail;
        private boolean deviceSpawned;
        private L2Npc dummyNpc;
        private L2Npc lastPower;
        private List<L2Npc> mobs;

        private RescueOfTheLastGiantWorld()
        {
            playerFail = false;
            deviceSpawned = false;
            mobs = new ArrayList<L2Npc>();
        }
    }

    public RescueOfTheLastGiant(int questId, String name, String descr)
    {
        super(questId, name, descr);

        addTalkId(DimensionalDoor.getNpcManagerId());
        addStartNpc(DimensionalDoor.getNpcManagerId());
        addFirstTalkId(controlDevice);
        addTalkId(controlDevice);
        addStartNpc(controlDevice);
        addAggroRangeEnterId(dummyNpc);

        for (int id : allmobs)
        {
            addKillId(id);
            addAttackId(id);
        }
    }

    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player)
    {
        if (debug)
        {
            Log.warning(getName() + ": onFirstTalk: " + player);
        }

        if (npc.getNpcId() == controlDevice)
        {
            return "sealDevice.html";
        }

        return super.onFirstTalk(npc, player);
    }

    @Override
    public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (debug)
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

        if (wrld != null && wrld instanceof RescueOfTheLastGiantWorld)
        {
            RescueOfTheLastGiantWorld world = (RescueOfTheLastGiantWorld) wrld;
            if (npc.getNpcId() == dummyNpc)
            {
                if (world.status == 0)
                {
                    world.status = 1;

                    startQuestTimer("stage_2_start", 2000, npc, null);
                }
                else if (world.status == 4)
                {
                    world.status = 5;
                    world.dummyNpc.deleteMe();
                    world.instancedPlayer.showQuestMovie(46);

                    InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240102).closeMe();

                    startQuestTimer("stage_3_spawns", ScenePlayerDataTable.getInstance().getVideoDuration(46) + 2000,
                            npc, null);
                }
            }
        }

        return super.onAggroRangeEnter(npc, player, isPet);
    }

    @Override
    public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        if (debug)
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

        if (wrld != null && wrld instanceof RescueOfTheLastGiantWorld)
        {
            RescueOfTheLastGiantWorld world = (RescueOfTheLastGiantWorld) wrld;
            if (event.equalsIgnoreCase("stage_1_start"))
            {
                world.instancedPlayer = player;

                //Screen msg
                InstanceManager.getInstance().sendDelayedPacketToPlayer(world.instancedPlayer, 5, world.instanceId,
                        new ExShowScreenMessage(10338012, 0, true, 4000));
                InstanceManager.getInstance().sendDelayedPacketToPlayer(world.instancedPlayer, 10, world.instanceId,
                        new ExShowScreenMessage(10338013, 0, true, 4000));

                world.mobToAttack = mob1;

                //Send the mob
                sendToCenter(world.instanceId, mob1, -107930, 206328, -10872, 49317);
                sendText(world.instanceId, mob1, 0, 10338014, 10338015);
            }
            else if (event.equalsIgnoreCase("stage_1_ends"))
            {
                world.mobToAttack = 0;

                //Open the door
                InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240100).openMe();

                //Spawn the powers
                addSpawn(80287, -107827, 206882, -10872, 46759, false, 10700, false, world.instanceId);
                addSpawn(80285, -107937, 206882, -10872, 49558, false, 10700, false, world.instanceId);
                addSpawn(80286, -108030, 206882, -10872, 60370, false, 10700, false, world.instanceId);

                //Cast npc chat
                sendText(world.instanceId, 80287, 2, 10338036, 10338037);

                //Cast screen message
                world.instancedPlayer.sendPacket(new ExShowScreenMessage(1811213, 0, true, 10000));
                world.instancedPlayer.sendPacket(new ExShowScreenMessage(1811214, 0, true, 10000));

                //Cast the skill to the player
                for (L2Npc power : InstanceManager.getInstance().getInstance(world.instanceId).getNpcs())
                {
                    if (power != null && power.getNpcId() >= 80285 && power.getNpcId() <= 80287)
                    {
                        power.setTarget(world.instancedPlayer);
                        power.doCast(powerHealSkill);
                    }
                }

                //Spawn the dummy npc
                world.dummyNpc = addSpawn(13310, -107929, 208860, -10877, 49151, false, 0, false, world.instanceId);
                world.dummyNpc.disableCoreAI(true);
                world.dummyNpc.setIsImmobilized(true);
            }
            else if (event.equalsIgnoreCase("stage_2_start"))
            {
                world.dummyNpc.deleteMe();

                InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240100).closeMe();

                world.instancedPlayer.sendPacket(new ExShowScreenMessage(10338030, 0, true, 4000));
                world.instancedPlayer.sendPacket(new ExShowScreenMessage(10338031, 0, true, 4000));
                world.instancedPlayer.sendPacket(new ExShowScreenMessage(10338032, 0, true, 4000));
                world.specificMobClass = 80292; //Aeore

                String className = world.instancedPlayer.getCurrentClass().getName();
                if (className.contains("Tyrr"))
                {
                    world.specificMobClass = 80293;
                }
                else if (className.contains("Sigel"))
                {
                    world.specificMobClass = 80294;
                }
                else if (className.contains("Iss"))
                {
                    world.specificMobClass = 80295;
                }
                else if (className.contains("Othell"))
                {
                    world.specificMobClass = 80296;
                }
                else if (className.contains("Yul"))
                {
                    world.specificMobClass = 80297;
                }
                else if (className.contains("Feoh"))
                {
                    world.specificMobClass = 80298;
                }
                else if (className.contains("Wynn"))
                {
                    world.specificMobClass = 80299;
                }

                addSpawn(world.specificMobClass, -107926, 209248, -10872, 49536, false, 0, false, world.instanceId);

                sendToCenter(world.instanceId, world.specificMobClass, -107930, 208861, -10872, 49536);
                sendText(world.instanceId, world.specificMobClass, 0, 10338028, 10338029);
            }
            else if (event.equalsIgnoreCase("stage_2_round_2"))
            {
                world.status = 3;
                world.instancedPlayer.sendPacket(new ExShowScreenMessage(1811222, 0, true, 4000));

                //The power
                L2Skill powerSkill = SkillTable.getInstance().getInfo(14700, 1);

                L2Npc power = addSpawn(80287, -107925, 208869, -10877, 8191, false, 10000, false, world.instanceId);
                power.setTarget(world.instancedPlayer);
                power.doCast(powerSkill);

                //11minions
                synchronized (world.mobs)
                {
                    for (int[] spawns : secondRoom2)
                    {
                        L2Npc mob =
                                addSpawn(world.specificMobClass, spawns[0], spawns[1], -10872, 49536, false, 0, false,
                                        world.instanceId);
                        world.mobs.add(mob);
                    }

                    //Send to attack
                    for (L2Npc mobs : world.mobs)
                    {
                        if (mobs != null && mobs.getNpcId() == world.specificMobClass)
                        {
                            sendToAttack(world.instancedPlayer, mobs);
                        }
                    }
                }
            }
            else if (event.equalsIgnoreCase("stage_2_round_3"))
            {
                world.status = 4;

                L2Npc newNpc = addSpawn(world.specificMobClass, -107926, 209248, -10872, 49536, false, 0, false,
                        world.instanceId);

                sendToAttack(world.instancedPlayer, newNpc);
            }
            else if (event.equalsIgnoreCase("stage_3_spawns"))
            {
                //Spawn the boss
                addSpawn(bossId, -107929, 211416, -10872, 49536, false, 0, false, world.instanceId);

                //Spawn the power
                world.lastPower = addSpawn(80287, -107926, 210899, -10872, 16686, false, 0, false, world.instanceId);

                //Start the heal
                startQuestTimer("stage_last_last_power", 6000, world.lastPower, null);

                //Spawn FlyNpc
                L2Npc flyNpc = addSpawn(80289, -107926, 212489, -10824, 49536, false, 0, false, world.instanceId);

                //flyNpc effect
                flyNpc.setDisplayEffect(1);

                //Delay here the flyNpc messages
                InstanceManager.getInstance().sendDelayedPacketToPlayer(world.instancedPlayer, 5, world.instanceId,
                        new ExShowScreenMessage(10338006, 0, true, 4000));
                InstanceManager.getInstance().sendDelayedPacketToPlayer(world.instancedPlayer, 15, world.instanceId,
                        new ExShowScreenMessage(10338007, 0, true, 4000));
                InstanceManager.getInstance().sendDelayedPacketToPlayer(world.instancedPlayer, 20, world.instanceId,
                        new ExShowScreenMessage(10338008, 0, true, 4000));
                InstanceManager.getInstance().sendDelayedPacketToPlayer(world.instancedPlayer, 25, world.instanceId,
                        new ExShowScreenMessage(10338009, 0, true, 4000));
            }
            else if (event.equalsIgnoreCase("stage_last_last_power"))
            {
                if (world.lastPower != null)
                {
                    if (!world.instancedPlayer.isDead())
                    {
                        world.lastPower.setTarget(world.instancedPlayer);
                        world.lastPower.doCast(powerHealSkill);
                    }

                    startQuestTimer("stage_last_last_power", 8000, world.lastPower, null);
                }
            }
            else if (event.equalsIgnoreCase("releaseSeal"))
            {
                if (npc.getDisplayEffect() != 0)
                {
                    return "";
                }

                if (world.status == 5)
                {
                    world.status = 6;
                }
                else if (world.status == 6)
                {
                    //Ended correctly
                    world.status = 7;

                    InstanceManager.getInstance()
                            .setInstanceReuse(world.instanceId, instanceTemplateId, reuseMinutes);
                    InstanceManager.getInstance().finishInstance(world.instanceId, true);

                    world.instancedPlayer.sendPacket(new ExSendUIEventRemove());
                    world.instancedPlayer.showQuestMovie(47);
                    world.instancedPlayer.addItem(qn, DimensionalDoor.getDimensionalDoorRewardId(),
                            10, player, true);
                }
                npc.setDisplayEffect(1);
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
        if (debug)
        {
            Log.warning(getName() + ": onAttack: " + npc.getName());
        }

        final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpWorld instanceof RescueOfTheLastGiantWorld)
        {
            RescueOfTheLastGiantWorld world = (RescueOfTheLastGiantWorld) tmpWorld;
            if (world.mobToAttack != 0)
            {
                if (!world.playerFail && world.mobToAttack != npc.getNpcId())
                {
                    world.playerFail = true;

                    //If player attack one mob without kill first the boss (status = 1), send all mobs to attack he
                    synchronized (world.mobs)
                    {
                        for (L2Npc mob : InstanceManager.getInstance().getInstance(world.instanceId).getNpcs())
                        {
                            if (mob != null && !mob.isDead() && mob.getNpcId() >= mob8 && mob.getNpcId() <= mob7)
                            {
                                world.mobs.add(mob);

                                sendToAttack(attacker, mob);
                            }
                        }
                    }
                }
            }

            if (npc.getNpcId() == bossId)
            {
                if (npc.getCurrentHp() < npc.getMaxHp() * 0.05 && !world.deviceSpawned) //5%
                {
                    world.deviceSpawned = true;
                    world.instancedPlayer.sendPacket(new ExSendUIEvent(1, 0, 60, 0, 1811302));

                    //Spawn device
                    L2Npc device = addSpawn(controlDevice, -107790, 211409, -10872, 32768, false, 60000, false,
                            world.instanceId);
                    device.sendPacket(new NpcSay(device.getObjectId(), 0, device.getTemplate().TemplateId, 1300171));

                    addSpawn(controlDevice, -108046, 211409, -10872, 32768, false, 60000, false, world.instanceId);

                    //Spawn guards
                    for (int[] spawn : lastGuardSpawns)
                    {
                        L2Npc mob = addSpawn(world.specificMobClass, spawn[0], spawn[1], -10872, 49536, false, 0, false,
                                world.instanceId);

                        sendToAttack(world.instancedPlayer, mob);
                    }
                    //Message
                    world.instancedPlayer.broadcastPacket(new ExShowScreenMessage(1811223, 0, true, 4000));
                }
            }
        }
        return super.onAttack(npc, attacker, damage, isPet);
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (debug)
        {
            Log.warning(getName() + ": onKill: " + npc.getName());
        }

        InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpworld instanceof RescueOfTheLastGiantWorld)
        {
            RescueOfTheLastGiantWorld world = (RescueOfTheLastGiantWorld) tmpworld;
            if (world.status == 0)
            {
                if (world.playerFail)
                {
                    synchronized (world.mobs)
                    {
                        if (world.mobs.contains(npc))
                        {
                            world.mobs.remove(npc);
                        }

                        if (world.mobs.isEmpty())
                        {
                            notifyEvent("stage_1_ends", npc, null);
                        }
                        return "";
                    }
                }

                switch (npc.getNpcId())
                {
                    case mob1:
                        world.mobToAttack = mob2;

                        sendToCenter(world.instanceId, mob2, -107930, 206328, -10872, 49317);
                        sendText(world.instanceId, mob2, 0, 10338016, 10338017);
                        break;

                    case mob2:
                        world.mobToAttack = mob3;

                        sendToCenter(world.instanceId, mob3, -107930, 206328, -10872, 49317);
                        sendText(world.instanceId, mob3, 0, 10338018, 10338019);
                        break;

                    case mob3:
                        world.mobToAttack = mob4;

                        sendToCenter(world.instanceId, mob4, -107930, 206328, -10872, 49317);
                        sendText(world.instanceId, mob4, 0, 10338028, 10338029);
                        break;

                    case mob4:
                        world.mobToAttack = mob5;

                        sendToCenter(world.instanceId, mob5, -107930, 206328, -10872, 49317);
                        sendText(world.instanceId, mob5, 0, 10338022, 10338023);
                        break;

                    case mob5:
                        world.mobToAttack = mob6;

                        sendToCenter(world.instanceId, mob6, -107930, 206328, -10872, 49317);
                        sendText(world.instanceId, mob6, 0, 10338020, 10338020);
                        break;

                    case mob6:
                        world.mobToAttack = mob7;

                        sendToCenter(world.instanceId, mob7, -107930, 206328, -10872, 49317);
                        sendText(world.instanceId, mob7, 0, 10338024, 10338025);
                        break;

                    case mob7:
                        world.mobToAttack = mob8;

                        sendToCenter(world.instanceId, mob8, -107930, 206328, -10872, 49317);
                        sendText(world.instanceId, mob8, 0, 10338026, 10338027);
                        break;

                    case mob8:
                        notifyEvent("stage_1_ends", npc, null);
                        break;
                }
            }
            else if (world.status == 1) //Second room
            {
                world.status = 2;
                synchronized (world.mobs)
                {
                    for (int[] spawn : secondRoom1)
                    {
                        L2Npc mob = addSpawn(world.specificMobClass, spawn[0], spawn[1], -10872, 49536, false, 0, false,
                                world.instanceId);
                        world.mobs.add(mob);
                    }

                    //Send to attack
                    for (L2Npc mobs : world.mobs)
                    {
                        if (mobs != null && mobs.getNpcId() == world.specificMobClass)
                        {
                            sendToAttack(world.instancedPlayer, mobs);
                        }
                    }
                }
            }
            else if (world.status == 2)
            {
                synchronized (world.mobs)
                {
                    if (world.mobs.contains(npc))
                    {
                        world.mobs.remove(npc);
                    }

                    if (!world.mobs.isEmpty())
                    {
                        return "";
                    }

                    startQuestTimer("stage_2_round_2", 4000, npc, null);
                }
            }
            else if (world.status == 3)
            {
                synchronized (world.mobs)
                {
                    if (world.mobs.contains(npc))
                    {
                        world.mobs.remove(npc);
                    }

                    if (!world.mobs.isEmpty())
                    {
                        return "";
                    }

                    startQuestTimer("stage_2_round_3", 4000, npc, null);
                }
            }
            else if (world.status == 4)
            {
                InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16240102).openMe();

                world.dummyNpc = addSpawn(dummyNpc, -107925, 211414, -10877, 49151, false, 0, false, world.instanceId);
            }
            else if (world.status > 4)
            {
                if (npc.getNpcId() == bossId)
                {
                    world.instancedPlayer.addItem(qn, DimensionalDoor.getDimensionalDoorRewardId(),
                            DimensionalDoor.getDimensionalDoorRewardRate(), player, true);
                    InstanceManager.getInstance().finishInstance(world.instanceId, true);
                    InstanceManager.getInstance().showVidToInstance(48, world.instanceId);
                }
            }

            //Herbs drop
            if (world.status < 4)
            {
                dropHerb(npc, player);
            }
        }

        return "";
    }

    private static void sendText(int inst, int npcid, int chatId, int npcstring1, int npcstring2)
    {
        for (L2Npc mob : InstanceManager.getInstance().getInstance(inst).getNpcs())
        {
            if (mob != null && mob.getNpcId() == npcid)
            {
                if (npcstring1 != 0)
                {
                    mob.broadcastPacket(
                            new NpcSay(mob.getObjectId(), chatId, mob.getTemplate().TemplateId, npcstring1));
                }

                if (npcstring2 != 0)
                {
                    mob.broadcastPacket(
                            new NpcSay(mob.getObjectId(), chatId, mob.getTemplate().TemplateId, npcstring2));
                }
            }
        }
    }

    private static void sendToCenter(int inst, int npcid, int x, int y, int z, int head)
    {
        for (L2Npc mob : InstanceManager.getInstance().getInstance(inst).getNpcs())
        {
            if (mob != null && mob.getNpcId() == npcid)
            {
                mob.setIsRunning(true);
                mob.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(x, y, z, head));
                mob.getSpawn().setX(x);
                mob.getSpawn().setY(y);
                mob.getSpawn().setZ(z);
            }
        }
    }

    private static void sendToAttack(L2PcInstance player, L2Npc npc)
    {
        if (player == null || npc == null)
        {
            return;
        }

        npc.setIsRunning(true);
        npc.setTarget(player);
        ((L2Attackable) npc).addDamageHate(player, 500, 99999);
        npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player, null);
    }

    private static void dropHerb(L2Npc mob, L2PcInstance player)
    {
        for (int i = 0; i < 2; i++)
        {
            ((L2MonsterInstance) mob).dropItem(player, herbs[i], 1);
        }
    }

    @Override
    public final String onTalk(L2Npc npc, L2PcInstance player)
    {
        if (debug)
        {
            Log.warning(getName() + ": onTalk: " + player.getName());
        }

        if (npc.getNpcId() == DimensionalDoor.getNpcManagerId())
        {
            return qn + ".html";
        }

        return super.onTalk(npc, player);
    }

    private final synchronized void enterInstance(L2PcInstance player)
    {
        InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
        if (world != null)
        {
            if (!(world instanceof RescueOfTheLastGiantWorld))
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
                    player.teleToLocation(-107917, 205824, -10872, true);

                    L2NpcBufferInstance.giveBasicBuffs(player);
                }
            }
            return;
        }
        else
        {
            if (!debug && !InstanceManager.getInstance()
                    .checkInstanceConditions(player, instanceTemplateId, 1, 1, 99, Config.MAX_LEVEL))
            {
                return;
            }

            final int instanceId = InstanceManager.getInstance().createDynamicInstance(qn + ".xml");
            world = new RescueOfTheLastGiantWorld();
            world.instanceId = instanceId;
            world.status = 0;

            InstanceManager.getInstance().addWorld(world);

            world.allowed.add(player.getObjectId());

            player.stopAllEffectsExceptThoseThatLastThroughDeath();
            player.setInstanceId(instanceId);
            player.teleToLocation(-107917, 205824, -10872, true);

            L2NpcBufferInstance.giveBasicBuffs(player);

            startQuestTimer("stage_1_start", 4000, null, player);

            Log.fine(getName() + ": instance started: " + instanceId + " created by player: " + player.getName());
            return;
        }
    }

    public static void main(String[] args)
    {
        new RescueOfTheLastGiant(-1, qn, "instances/DimensionalDoor");
    }
}
