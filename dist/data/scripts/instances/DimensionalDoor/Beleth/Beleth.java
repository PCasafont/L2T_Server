package instances.DimensionalDoor.Beleth;

import l2server.Config;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.network.serverpackets.PlaySound;
import l2server.gameserver.network.serverpackets.SocialAction;
import l2server.gameserver.network.serverpackets.SpecialCamera;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

import ai.group_template.L2AttackableAIScript;

import java.util.ArrayList;
import java.util.List;

import instances.DimensionalDoor.DimensionalDoor;

/**
 * @author LasTravel (Based on Abyss script)
 *         <p>
 *         Source:
 *         - http://www.youtube.com/watch?v=XC57OBGMqgo
 *         - http://www.youtube.com/watch?v=QYrtMywPuHI&feature=related
 */

public class Beleth extends L2AttackableAIScript
{
    //Quest
    private static final boolean debug = false;
    private static final String qn = "Beleth";

    //Id's
    private static final int stoneId = 32470;
    private static final int priestId = 29128;
    private static final int realBelethId = 80216;
    private static final int fakeBelethId = 80217;
    private static final int _door_1 = 20240001;
    private static final int _door_2 = 20240002;
    private static final int _door_3 = 20240003;
    private static final int instanceTemplateId = 500;
    private static final int _camera_1_id = 29120;
    private static final int _camera_2_id = 29121;
    private static final int _camera_3_id = 29122;
    private static final int _camera_4_id = 29123;
    private static final int _camera_6_id = 29125;

    //Vars
    private static int[] cloneX = new int[32];
    private static int[] cloneY = new int[32];
    private static int[] cloneH = new int[32];

    //Cords
    private static final Location[] enterCords = {
            new Location(16311, 209100, -9360),
            new Location(16002, 209388, -9360),
            new Location(16572, 209655, -9360),
            new Location(16514, 209942, -9360),
            new Location(16364, 209348, -9360)
    };

    public Beleth(int questId, String name, String descr)
    {
        super(questId, name, descr);

        addTalkId(DimensionalDoor.getNpcManagerId());
        addStartNpc(DimensionalDoor.getNpcManagerId());
        addFirstTalkId(stoneId);
        addFirstTalkId(priestId);
        addKillId(realBelethId);
        addKillId(fakeBelethId);
        addAggroRangeEnterId(realBelethId);
        addAggroRangeEnterId(fakeBelethId);

        //Calculate shits
        double angle = 22.5;
        int innerRad;
        int outerRad;
        for (int i = 0; i < 16; i++)
        {
            if (i % 2 == 0)
            {
                innerRad = 650;
                outerRad = 1200;
            }
            else
            {
                innerRad = 700;
                outerRad = 1250;
            }

            cloneX[i] = 16327;
            cloneX[i] += (int) (innerRad * Math.sin(i * Math.toRadians(angle)));
            cloneY[i] = 213135;
            cloneY[i] += (int) (innerRad * Math.cos(i * Math.toRadians(angle)));
            cloneH[i] = Util.convertDegreeToClientHeading(270 - i * angle);
            cloneX[i + 16] = 16327;
            cloneX[i + 16] += (int) (outerRad * Math.sin(i * Math.toRadians(angle)));
            cloneY[i + 16] = 213135;
            cloneY[i + 16] += (int) (outerRad * Math.cos(i * Math.toRadians(angle)));
            cloneH[i + 16] = Util.convertDegreeToClientHeading(90 - i * angle);
        }
    }

    private class belethWorld extends InstanceWorld
    {
        private ArrayList<L2PcInstance> rewardedPlayers;
        private ArrayList<L2Npc> minions;
        private L2Npc belethBoss;
        private L2Npc priest;
        private L2Npc camera1;
        private L2Npc camera2;
        private L2Npc camera3;
        private L2Npc camera4;

        public belethWorld()
        {
            minions = new ArrayList<L2Npc>();
            rewardedPlayers = new ArrayList<L2PcInstance>();
        }
    }

    @Override
    public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (debug)
        {
            Log.warning(getName() + ": onAggroRangeEnter: " + player);
        }

        if (npc.getNpcId() == realBelethId || npc.getNpcId() == fakeBelethId)
        {
            if (isPet)
            {
                if (player.getPet() != null)
                {
                    ((L2Attackable) npc).addDamageHate(player.getPet(), 0, 999);
                    npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player.getPet());
                }
                else if (player.getSummons() != null)
                {
                    for (L2SummonInstance summon : player.getSummons())
                    {
                        if (summon == null)
                        {
                            continue;
                        }

                        ((L2Attackable) npc).addDamageHate(summon, 0, 999);
                        npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, summon);
                    }
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

        if (wrld != null && wrld instanceof belethWorld)
        {
            belethWorld world = (belethWorld) wrld;
            if (event.equalsIgnoreCase("stage_1_open_door"))
            {
                InstanceManager.getInstance().getInstance(world.instanceId).getDoor(_door_1).openMe();

                startQuestTimer("stage_1_intro_1", debug ? 20000 : 5 * 60000, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1"))
            {
                InstanceManager.getInstance().stopWholeInstance(world.instanceId);

                world.camera1 = addSpawn(_camera_1_id, 16323, 213142, -9357, 0, false, 0, false, world.instanceId);
                world.camera2 = addSpawn(_camera_2_id, 16323, 210741, -9357, 0, false, 0, false, world.instanceId);
                world.camera3 = addSpawn(_camera_3_id, 16323, 213170, -9357, 0, false, 0, false, world.instanceId);
                world.camera4 = addSpawn(_camera_4_id, 16323, 214917, -9356, 0, false, 0, false, world.instanceId);

                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new PlaySound(1, "BS07_A", 1, world.camera1.getObjectId(), world.camera1.getX(),
                                world.camera1.getY(), world.camera1.getZ()));
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera1.getObjectId(), 400, 75, 5, 0, 2500, 0, 0, 1, 0));

                startQuestTimer("stage_1_intro_1_2", 2300, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_2"))
            {
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera1.getObjectId(), 1800, -45, 10, 5000, 5000, 0, 0, 1, 0));

                startQuestTimer("stage_1_intro_1_3", 4900, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_3"))
            {
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera1.getObjectId(), 1900, -45, 10, 5000, 5000, 0, 0, 1, 0));

                startQuestTimer("stage_1_intro_1_4", 4900, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_4"))
            {
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera2.getObjectId(), 2200, 130, 0, 0, 1500, -20, 15, 1, 0));

                startQuestTimer("stage_1_intro_1_5", 1400, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_5"))
            {
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera2.getObjectId(), 2300, 100, 0, 2000, 4500, 0, 10, 1, 0));

                startQuestTimer("stage_1_intro_1_6", 2500, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_6"))
            {
                InstanceManager.getInstance().getInstance(world.instanceId).getDoor(_door_1).closeMe();

                startQuestTimer("stage_1_intro_1_7", 1700, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_7"))
            {
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera4.getObjectId(), 1500, 210, 0, 0, 1500, 0, 0, 1, 0));
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera4.getObjectId(), 900, 255, 0, 5000, 6500, 0, 10, 1, 0));

                startQuestTimer("stage_1_intro_1_8", 6000, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_8"))
            {
                addSpawn(_camera_6_id, 16323, 214917, -9356, 0, false, 0, false, world.instanceId); //Camera 6

                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera4.getObjectId(), 900, 255, 0, 0, 1500, 0, 10, 1, 0));

                startQuestTimer("stage_1_intro_1_9", 1000, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_9"))
            {
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera4.getObjectId(), 1000, 255, 0, 7000, 17000, 0, 25, 1, 0));

                startQuestTimer("stage_1_intro_1_10", 3000, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_10"))
            {
                world.belethBoss =
                        addSpawn(realBelethId, 16321, 214211, -9352, 49369, false, 0, false, world.instanceId);

                startQuestTimer("stage_1_intro_1_11", 200, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_11"))
            {
                InstanceManager.getInstance()
                        .sendPacket(world.instanceId, new SocialAction(world.belethBoss.getObjectId(), 1));
                for (int i = 0; i < 6; i++)
                {
                    int x = (int) (150 * Math.cos(i * 1.046666667) + 16323);
                    int y = (int) (150 * Math.sin(i * 1.046666667) + 213059);

                    L2Npc minion = addSpawn(fakeBelethId, x, y, -9357, 49152, false, 0, false, world.instanceId);
                    minion.setShowSummonAnimation(true);
                    minion.decayMe();
                    world.minions.add(minion);
                }
                startQuestTimer("stage_1_intro_1_12", 6800, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_12"))
            {
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.belethBoss.getObjectId(), 0, 270, -5, 0, 4000, 0, 0, 1, 0));

                startQuestTimer("stage_1_intro_1_13", 3500, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_13"))
            {
                InstanceManager.getInstance()
                        .sendPacket(world.instanceId, new SocialAction(world.belethBoss.getObjectId(), 4));
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new MagicSkillUse(world.belethBoss, world.belethBoss, 5531, 1, 2000, 0, 0));
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.belethBoss.getObjectId(), 800, 270, 10, 3000, 6000, 0, 0, 1, 0));

                startQuestTimer("stage_1_intro_1_14", 5000, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_14"))
            {
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera3.getObjectId(), 100, 270, 15, 0, 5000, 0, 0, 1, 0));
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera3.getObjectId(), 100, 270, 15, 0, 5000, 0, 0, 1, 0));

                startQuestTimer("stage_1_intro_1_15", 100, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_15"))
            {
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera3.getObjectId(), 100, 270, 15, 3000, 6000, 0, 5, 1, 0));

                startQuestTimer("stage_1_intro_1_16", 1400, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_16"))
            {
                world.belethBoss.deleteMe();
                world.belethBoss =
                        addSpawn(realBelethId, 16323, 213059, -9357, 49152, false, 0, false, world.instanceId);
                world.belethBoss.setIsImmobilized(true);

                startQuestTimer("stage_1_intro_1_17", 200, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_17"))
            {
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new MagicSkillUse(world.belethBoss, world.belethBoss, 5532, 1, 2000, 0, 0));

                startQuestTimer("stage_1_intro_1_18", 2000, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_18"))
            {
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera3.getObjectId(), 700, 270, 20, 1500, 4000, 0, 0, 1, 0));

                startQuestTimer("stage_1_intro_1_19", 4000, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_19"))
            {
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera3.getObjectId(), 40, 260, 0, 0, 4000, 0, 0, 1, 0));
                for (L2Npc blth : world.minions)
                {
                    blth.spawnMe();
                    blth.setIsImmobilized(true);//3000
                }
                startQuestTimer("stage_1_intro_1_20", 3000, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_20"))
            {
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera3.getObjectId(), 40, 280, 0, 0, 4000, 5, 0, 1, 0));

                L2Npc minion = addSpawn(fakeBelethId, 16253, 213144, -9357, 49152, false, 0, false, world.instanceId);
                minion.setShowSummonAnimation(true);
                minion.decayMe();
                world.minions.add(minion);
                minion.spawnMe();
                minion.setIsImmobilized(true);

                startQuestTimer("stage_1_intro_1_21", 3000, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_21"))
            {
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera3.getObjectId(), 5, 250, 5, 0, 13000, 20, 15, 1, 0));

                startQuestTimer("stage_1_intro_1_22", 1000, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_22"))
            {
                InstanceManager.getInstance()
                        .sendPacket(world.instanceId, new SocialAction(world.belethBoss.getObjectId(), 3));

                startQuestTimer("stage_1_intro_1_23", 4000, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_23"))
            {
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new MagicSkillUse(world.belethBoss, world.belethBoss, 5533, 1, 2000, 0, 0));

                startQuestTimer("stage_1_intro_1_24", 2000, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_24"))
            {
                world.belethBoss.deleteMe();

                startQuestTimer("stage_1_intro_1_25", 1000, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_25"))
            {
                for (L2Npc bel : world.minions)
                {
                    bel.deleteMe();
                }

                world.minions.clear();

                startQuestTimer("stage_1_intro_1_26", 3000, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_26"))
            {
                world.camera1.deleteMe();
                world.camera2.deleteMe();
                world.camera3.deleteMe();
                world.camera4.deleteMe();

                startQuestTimer("stage_1_intro_1_27", 1, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro_1_27"))
            {
                spawnBeleths(world);

                InstanceManager.getInstance().startWholeInstance(world.instanceId);
            }
            else if (event.equalsIgnoreCase("stage_final_1"))
            {
                world.belethBoss.doDie(null);

                world.camera3 = addSpawn(_camera_3_id, 16323, 213170, -9357, 0, false, 0, false, world.instanceId);

                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new PlaySound(1, "BS07_D", 1, world.camera3.getObjectId(), world.camera3.getX(),
                                world.camera3.getY(), world.camera3.getZ()));
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera3.getObjectId(), 400, 290, 25, 0, 10000, 0, 0, 1, 0));
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera3.getObjectId(), 400, 290, 25, 0, 10000, 0, 0, 1, 0));
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera3.getObjectId(), 400, 110, 25, 4000, 10000, 0, 0, 1, 0));
                InstanceManager.getInstance()
                        .sendPacket(world.instanceId, new SocialAction(world.belethBoss.getObjectId(), 5));

                startQuestTimer("stage_final_2", 4000, null, player);
            }
            else if (event.equalsIgnoreCase("stage_final_2"))
            {
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera1.getObjectId(), 400, 295, 25, 4000, 5000, 0, 0, 1, 0));

                startQuestTimer("stage_final_3", 4500, null, player);
            }
            else if (event.equalsIgnoreCase("stage_final_3"))
            {
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera1.getObjectId(), 400, 295, 10, 4000, 11000, 0, 25, 1, 0));

                startQuestTimer("stage_final_4", 9000, null, player);
            }
            else if (event.equalsIgnoreCase("stage_final_4"))
            {
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera1.getObjectId(), 250, 90, 25, 0, 1000, 0, 0, 1, 0));
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera1.getObjectId(), 250, 90, 25, 0, 10000, 0, 0, 1, 0));

                startQuestTimer("stage_final_5", 2000, null, player);
            }
            else if (event.equalsIgnoreCase("stage_final_5"))
            {
                world.priest.spawnMe();

                world.belethBoss.deleteMe();

                world.camera2 = addSpawn(_camera_2_id, 14056, 213170, -9357, 0, false, 0, false, world.instanceId);

                startQuestTimer("stage_final_6", 3500, null, player);
            }
            else if (event.equalsIgnoreCase("stage_final_6"))
            {
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera2.getObjectId(), 800, 180, 0, 0, 4000, 0, 10, 1, 0));
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new SpecialCamera(world.camera2.getObjectId(), 800, 180, 0, 0, 4000, 0, 10, 1, 0));
                InstanceManager.getInstance().getInstance(world.instanceId).getDoor(_door_2).openMe();
                InstanceManager.getInstance().getInstance(world.instanceId).getDoor(_door_3).openMe();

                startQuestTimer("stage_final_7", 4000, null, player);
            }
            else if (event.equalsIgnoreCase("stage_final_7"))
            {
                world.camera1.deleteMe();
                world.camera2.deleteMe();

                InstanceManager.getInstance().setInstanceReuse(world.instanceId, instanceTemplateId, 6, 30);
                InstanceManager.getInstance().finishInstance(world.instanceId, false);
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
        return null;
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (debug)
        {
            Log.warning(getName() + ": onKill: " + npc.getName());
        }

        InstanceWorld wrld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (wrld != null && wrld instanceof belethWorld)
        {
            belethWorld world = (belethWorld) wrld;
            if (npc.getNpcId() == realBelethId)
            {
                despawnAll(world);

                world.belethBoss.deleteMe();
                world.belethBoss =
                        addSpawn(realBelethId, 16323, 213170, -9357, 49152, false, 0, false, world.instanceId);
                world.belethBoss.setIsInvul(true);
                world.belethBoss.setIsImmobilized(true);
                world.belethBoss.disableAllSkills();

                world.priest = addSpawn(priestId, 16323, 213170, -9357, 49152, false, 0, false, world.instanceId);
                world.priest.setShowSummonAnimation(true);
                world.priest.decayMe();

                addSpawn(stoneId, 12470, 215607, -9381, 49152, false, 0, false, world.instanceId); //Stone

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
                            pMember.addItem(qn, DimensionalDoor.getDimensionalDoorRewardId(),
                                    40, player, true);
                        }
                        else
                        {
                            pMember.sendMessage("Nice attempt, but you already got a reward!");
                        }
                    }
                }
                startQuestTimer("stage_final_1", 1000, null, player);
            }
            else if (npc.getNpcId() == fakeBelethId)
            {
                npc.abortCast();
                npc.setTarget(null);
                npc.deleteMe();
            }
        }
        return super.onKill(npc, player, isPet);
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

    private void spawnBeleths(belethWorld world)
    {
        int realbeleth = Rnd.get(32);

        L2Npc npc;

        for (int i = 0; i < 32; i++)
        {
            if (i == realbeleth)
            {
                npc = addSpawn(realBelethId, cloneX[i], cloneY[i], -9353, cloneH[i], false, 0, false,
                        world.instanceId);
            }
            else
            {
                npc = addSpawn(fakeBelethId, cloneX[i], cloneY[i], -9353, cloneH[i], false, 0, false,
                        world.instanceId);
            }

            npc.getAI().setIntention(CtrlIntention.AI_INTENTION_REST);

            world.minions.add(npc);
        }
    }

    private void despawnAll(belethWorld world)
    {
        for (L2Npc npc : world.minions)
        {
            npc.getSpawn().stopRespawn();
            npc.deleteMe();
        }
        world.minions.clear();
    }

    private final synchronized void enterInstance(L2PcInstance player)
    {
        InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
        if (world != null)
        {
            if (!(world instanceof belethWorld))
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
                    player.teleToLocation(16323, 211588, -9359);
                }
            }

            return;
        }
        else
        {
            if (!debug && !InstanceManager.getInstance()
                    .checkInstanceConditions(player, instanceTemplateId, 7, 7, 92, Config.MAX_LEVEL))
            {
                return;
            }

            final int instanceId = InstanceManager.getInstance().createDynamicInstance(qn + ".xml");
            world = new belethWorld();
            world.instanceId = instanceId;
            world.templateId = instanceTemplateId;
            world.status = 0;

            InstanceManager.getInstance().addWorld(world);

            List<L2PcInstance> allPlayers = new ArrayList<L2PcInstance>();
            if (debug)
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
                enterPlayer.teleToLocation(enterCords[Rnd.get(0, enterCords.length - 1)], true);
            }

            startQuestTimer("stage_1_open_door", 3000, null, player);

            Log.fine(getName() + ": [" + instanceTemplateId + "] instance started: " + instanceId +
                    " created by player: " + player.getName());
            return;
        }
    }

    public static void main(String[] args)
    {
        new Beleth(-1, qn, "instances/DimensionalDoor");
    }
}
