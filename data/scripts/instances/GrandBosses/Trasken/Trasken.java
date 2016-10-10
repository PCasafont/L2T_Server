package instances.GrandBosses.Trasken;

import java.util.ArrayList;
import java.util.List;

import l2server.Config;
import l2server.gameserver.ai.L2AttackableAI;
import l2server.gameserver.ai.L2NpcWalkerAI;
import l2server.gameserver.datatables.ScenePlayerDataTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.L2NpcWalkerNode;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.Earthquake;
import l2server.gameserver.network.serverpackets.ExSendUIEvent;
import l2server.gameserver.network.serverpackets.ExSendUIEventRemove;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;
import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 *         <p>
 *         Trasken Boss - Normal
 *         <p>
 *         Source:
 *         - http://forums.goha.ru/showthread_0_0_t653488
 *         - http://power.plaync.co.kr/lineage2/%EC%96%B4%EC%8A%A4%EC%9B%9C
 *         - http://www.youtube.com/watch?v=0Vyu7GJvuBo (Finally retail like video, very different from what I made :()
 */

public class Trasken extends L2AttackableAIScript
{
    //Quest
    private static final boolean _debug = false;
    private static final String _qn = "Trasken";

    //Ids
    private static final int[] _allMobs = {19081, 29200, 19159, 29198, 29205, 25801, 29206};
    private static final int _enterNpc = 30537; //Daichir
    private static final int _teleDevice = 33513;
    private static final int _earthWyrmHeart = 19081;
    private static final int _earthWyrmTail = 29200;
    private static final int _despawnEach = 60000; //MS
    private static final int _earthWyrmTrasken = 19159; //Head-final
    private static final int _tentacleSmall = 29198;
    private static final int _tentacleMedium = 29205;
    private static final int _stomachGland = 29206;
    private static final int[] _larvas = {29207, 29208, 29220, 29221};
    private static final int _tentaclesToKill = 20;
    private static final int _teredorId = 25801;
    private static final int _template = 138;
    private static final int _zoneDoor = 22120001;

    //Skills
    private static final L2Skill _tailHiding = SkillTable.getInstance().getInfo(14343, 1);
    private static final L2Skill _tailHaunting = SkillTable.getInstance().getInfo(14342, 1);
    private static final L2Skill _traskenHaunting = SkillTable.getInstance().getInfo(14505, 1);

    //Cords
    private static final Location _traskenInterior = new Location(88604, -173907, -15989, 32324);
    private static final Location _caveCords = new Location(80048, -182107, -9898);
    private static final Location _enterCords = new Location(75445, -182112, -9880);
    private static final Location _enterCaveCords = new Location(78721, -182239, -9905);

    //Others
    private static List<L2NpcWalkerNode> _route = new ArrayList<L2NpcWalkerNode>();

    //Spawns
    private static final int[][] _larvaSpawns = {{88798, -173756, -15981, 32767}, {87763, -173760, -15980, 65170}};

    private static final int[][] _walkRoutes = {
            {80800, -182273, -9880, 16544},
            {81844, -181768, -9897, 4951},
            {82457, -181777, -9899, 64543},
            {82962, -182286, -9899, 52926},
            {82957, -183324, -9897, 47472},
            {82627, -183946, -9897, 40673},
            {81887, -183622, -9895, 24724},
            {80992, -181181, -9892, 14214},
            {79823, -180464, -9887, 31456},
            {79127, -181424, -9879, 37761},
            {78644, -182568, -9898, 45995},
            {79045, -183289, -9899, 54458},
            {80491, -182208, -9886, 14486}
    };

    private static final int[][] _earthWyrmTailSpawns = {
            {81181, -180408, -9872, 39299},
            {81211, -182104, -9901, 39299},
            {82984, -182071, -9905, 39299},
            {79560, -182129, -9905, 39299},
            {80988, -183857, -9902, 39299}
    };

    private class TraskenWorld extends InstanceWorld
    {
        private ArrayList<L2Npc> _teredors;
        private L2Npc TraskenHeart;
        private L2Npc Trasken;
        private int mobCount;
        private double tailHP;
        private boolean isTeredorTime;
        private boolean isTraskenTime;
        private long lastTailSpawned;
        private L2NpcWalkerAI teredor_WalkAI;
        private L2AttackableAI teredor_AttackAI;

        public TraskenWorld()
        {
            _teredors = new ArrayList<L2Npc>();
            isTeredorTime = false;
            isTraskenTime = false;
        }
    }

    public Trasken(int questId, String name, String descr)
    {
        super(questId, name, descr);

        addStartNpc(_enterNpc);
        addTalkId(_enterNpc);
        addTalkId(_teleDevice);
        addSpawnId(_tentacleSmall);
        addSpawnId(_tentacleMedium);
        addSpawnId(_stomachGland);
        addSpawnId(_earthWyrmHeart);

        for (int id : _allMobs)
        {
            addAttackId(id);
            addKillId(id);
        }

        for (int[] coord : _walkRoutes)
        {
            _route.add(new L2NpcWalkerNode(coord[0], coord[1], coord[2], 0, "", true));
        }
    }

    private void stopTeredorWalk(TraskenWorld world, L2Npc npc)
    {
        for (L2Npc teredor : world._teredors)
        {
            if (teredor == null || teredor != npc)
            {
                continue;
            }

            teredor.stopMove(null);
            world.teredor_WalkAI.cancelTask();
            teredor.setIsInvul(false);
            world.teredor_AttackAI = new L2AttackableAI(teredor.new AIAccessor());
            teredor.setAI(world.teredor_AttackAI);
        }
    }

    private void startTeredorWalk(TraskenWorld world)
    {
        for (L2Npc teredor : world._teredors)
        {
            if (teredor == null)
            {
                continue;
            }

            if (teredor.isCastingNow())
            {
                teredor.abortCast();
            }

            teredor.setIsInvul(true);
            teredor.setIsRunning(true);
            world.teredor_WalkAI = new L2NpcWalkerAI(teredor.new AIAccessor());
            teredor.setAI(world.teredor_WalkAI);
            world.teredor_WalkAI.initializeRoute(_route, null);
            world.teredor_WalkAI.walkToLocation();
        }
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

        if (wrld != null && wrld instanceof TraskenWorld)
        {
            final TraskenWorld world = (TraskenWorld) wrld;
            if (event.equalsIgnoreCase("stage_1_start"))
            {
                world.status = 1;

                //Open the door
                InstanceManager.getInstance().getInstance(world.instanceId).getDoor(_zoneDoor).openMe();

                //Teredors
                for (int i = 0; i < Rnd.get(1, 6); i++)
                {
                    L2Npc teredor =
                            addSpawn(_teredorId, 82366, -183533, -9888, 56178, false, 0, false, world.instanceId);
                    teredor.setIsInvul(true);
                    world._teredors.add(teredor);
                }

                startTeredorWalk(world);

                //Spawn the tail
                world.Trasken = addSpawn(_earthWyrmTail, _earthWyrmTailSpawns[0][0], _earthWyrmTailSpawns[0][1],
                        _earthWyrmTailSpawns[0][2], _earthWyrmTailSpawns[0][3], false, 0, false, world.instanceId);
                world.Trasken.setIsInvul(true);
                world.Trasken.setIsImmobilized(true);
                world.Trasken.setIsMortal(false);

                //Cast the earthquake
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new Earthquake(world.Trasken.getX(), world.Trasken.getY(), world.Trasken.getZ(), 5, 5));
                //Custom Message
                InstanceManager.getInstance().sendPacket(world.instanceId, new ExShowScreenMessage(
                        "You should decrease the number of Tentacles in order to weak Earth Wyrm Trasken", 10000));
            }
            else if (event.equalsIgnoreCase("stage_all_raid_despawn"))
            {
                //Cast the earthquake
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new Earthquake(world.Trasken.getX(), world.Trasken.getY(), world.Trasken.getZ(), 5, 5));

                //set the tail invul
                world.Trasken.setIsInvul(true);
                //Hiding
                world.Trasken.doCast(_tailHiding);

                startQuestTimer("stage_all_hiding_casted", 10000 - 2000, npc, null);
            }
            else if (event.equalsIgnoreCase("stage_all_raid_spawn"))
            {
                //Cast the earthquake
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new Earthquake(world.Trasken.getX(), world.Trasken.getY(), world.Trasken.getZ(), 5, 5));

                world.Trasken.setIsInvisible(false);
                world.Trasken.doCast(world.isTraskenTime ? _traskenHaunting : _tailHaunting);
                world.lastTailSpawned = System.currentTimeMillis();

                startQuestTimer("stage_all_haunting_casted", 10000, npc, null);
            }
            else if (event.equalsIgnoreCase("stage_all_hiding_casted"))
            {
                world.tailHP = world.Trasken.getCurrentHp();
                world.Trasken.deleteMe();

                if (world.isTraskenTime)
                {
                    //Close the door
                    InstanceManager.getInstance().getInstance(world.instanceId).getDoor(_zoneDoor).closeMe();
                    //TRASKEN
                    world.Trasken = addSpawn(_earthWyrmTrasken, 82383, -183527, -9892, 26533, false, 0, false,
                            world.instanceId);
                }
                else
                {
                    //TAIL
                    int[] tailSpawn = _earthWyrmTailSpawns[Rnd.get(_earthWyrmTailSpawns.length)];
                    world.Trasken =
                            addSpawn(_earthWyrmTail, tailSpawn[0], tailSpawn[1], tailSpawn[2], tailSpawn[3], false, 0,
                                    false, world.instanceId);
                    world.Trasken.setCurrentHp(world.tailHP);
                }
                world.Trasken.setIsInvisible(true);
                world.Trasken.setIsInvul(true);
                world.Trasken.setIsImmobilized(true);
                world.Trasken.setIsMortal(false);

                //Start the spawn
                startQuestTimer("stage_all_raid_spawn", 15000 + Rnd.get(5000, 7000), npc, null);
            }
            else if (event.equalsIgnoreCase("stage_all_haunting_casted"))
            {
                if (world.Trasken.isInvul())
                {
                    world.Trasken.setIsInvul(false);
                }

                if (world.isTraskenTime)
                {
                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new ExShowScreenMessage("Focus all your power to Earth Wyrm trasken!",
                                    10000)); //Custom Message
                }
            }
            else if (event.equalsIgnoreCase("stage_last_spawn_larvas"))
            {
                for (int[] a : _larvaSpawns)
                {
                    L2Npc larva = addSpawn(_larvas[Rnd.get(_larvas.length)], a[0], a[1], a[2], a[3], false, 0, false,
                            world.instanceId);
                    larva.setIsRunning(true);
                }
            }
            else if (event.equalsIgnoreCase("stage_last_stomach_glands_message"))
            {
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new ExShowScreenMessage(1620001, 0, true, 5000)); //Acid is secreting from the Stomach Glands
            }
            else if (event.equalsIgnoreCase("stage_last_foes_message"))
            {
                //Larvas?
                startQuestTimer("stage_last_spawn_larvas", 7000, world.Trasken, null, true);

                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new ExShowScreenMessage(1620003, 0, true, 5000)); //The heart is being protected by foes

                startQuestTimer("stage_last_eliminate_larvas_message", 6000, npc, null);
            }
            else if (event.equalsIgnoreCase("stage_last_eliminate_larvas_message"))
            {
                InstanceManager.getInstance().sendPacket(world.instanceId, new ExShowScreenMessage(1620006, 0, true,
                        5000)); //Eliminate those who protect the heart of the Earth Wyrm

                startQuestTimer("stage_last_heart_invul", 30000, npc, null);
            }
            else if (event.equalsIgnoreCase("stage_last_heart_invul"))
            {
                world.TraskenHeart.setIsInvul(false); //Now the heart can get damage
            }
            else if (event.equalsIgnoreCase("stage_last_time_elapsed"))
            {
                QuestTimer timer = getQuestTimer("stage_last_spawn_larvas", world.Trasken, null);
                if (timer != null)
                {
                    timer.cancel();
                }

                world.TraskenHeart.deleteMe();
                world.Trasken.deleteMe();

                //Timer off
                InstanceManager.getInstance().sendPacket(world.instanceId, new ExSendUIEventRemove());
                //At this point the raid failed
                InstanceManager.getInstance().finishInstance(world.instanceId, true);
            }
            else if (event.equalsIgnoreCase("stage_last_teleport_back"))
            {
                world.status = 3;

                //Teleport back the players for take the drop?
                for (L2PcInstance pl : InstanceManager.getInstance().getPlayers(world.instanceId))
                {
                    if (pl == null)
                    {
                        continue;
                    }

                    pl.teleToLocation(_caveCords, true);
                }
            }
        }

        if (npc != null && npc.getNpcId() == _enterNpc && Util.isDigit(event) && Integer.valueOf(event) == _template)
        {
            try
            {
                enterInstance(player, Integer.valueOf(event));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }
        return null;
    }

    @Override
    public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onAttack: " + npc.getName());
        }

        final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpWorld instanceof TraskenWorld)
        {
            final TraskenWorld world = (TraskenWorld) tmpWorld;

            switch (npc.getNpcId())
            {
                case _teredorId:
                    if (world.isTeredorTime && npc.isInvul())
                    {
                        stopTeredorWalk(world, npc);
                    }
                    break;

                case _earthWyrmTail:
                    if (!npc.isInvul() && npc.getCurrentHp() < npc.getMaxHp() * 0.05)
                    {
                        world.isTraskenTime = true;

                        startQuestTimer("stage_all_raid_despawn", 1, npc, null);

                        return "";
                    }

                    if (System.currentTimeMillis() > world.lastTailSpawned + _despawnEach)
                    {
                        startQuestTimer("stage_all_raid_despawn", 1, npc, null);
                    }
                    break;

                case _earthWyrmTrasken:
                    if (world.status == 1 && !npc.isInvul() && npc.getCurrentHp() < npc.getMaxHp() * 0.05)
                    {
                        world.status = 2;

                        world.Trasken.setIsInvul(true);
                        //Spawn Earth Wyrm Heart
                        world.TraskenHeart = addSpawn(_earthWyrmHeart, 88285, -173758, -15965, 49151, false, 0, false,
                                world.instanceId);
                        world.TraskenHeart.setIsInvul(true);

                        //Teleport players to Heart
                        for (L2PcInstance pl : InstanceManager.getInstance().getPlayers(world.instanceId))
                        {
                            if (pl == null)
                            {
                                continue;
                            }
                            pl.teleToLocation(_traskenInterior, true);
                        }

                        InstanceManager.getInstance().sendPacket(world.instanceId,
                                new ExShowScreenMessage(1620005, 0, true, 5000)); //You were ingested by the Earth Wyrm

                        startQuestTimer("stage_last_stomach_glands_message", 6000, npc, null);

                        //Send the 10min packet time
                        InstanceManager.getInstance()
                                .sendPacket(world.instanceId, new ExSendUIEvent(0, 0, 600, 0, 1811302));

                        //Start the end task check
                        startQuestTimer("stage_last_time_elapsed", 600000, npc, null); //10 Min
                    }
                    break;
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
        if (tmpworld instanceof TraskenWorld)
        {
            TraskenWorld world = (TraskenWorld) tmpworld;
            switch (npc.getNpcId())
            {
                case _tentacleSmall:
                case _tentacleMedium:
                    if (world.mobCount < _tentaclesToKill)
                    {
                        world.mobCount++;
                    }

                    if (_debug)
                    {
                        Log.warning(getName() + ": mobCount: " + world.mobCount);
                    }

                    if (world.mobCount == _tentaclesToKill && !world.isTeredorTime)
                    {
                        world.isTeredorTime = true;
                        //Custom Message
                        InstanceManager.getInstance().sendPacket(world.instanceId, new ExShowScreenMessage(
                                "It's the time to kill Teredor's, search and destroy all of them!", 10000));
                    }
                    break;

                case _teredorId:
                    world._teredors.remove(npc);
                    if (world._teredors.isEmpty())
                    {
                        world.Trasken.setIsInvul(false);
                    }
                    break;

                case _stomachGland:
                    for (L2Npc gland : InstanceManager.getInstance().getInstance(world.instanceId).getNpcs())
                    {
                        if (gland == null || gland.getNpcId() != _stomachGland)
                        {
                            continue;
                        }

                        if (!gland.isDead())
                        {
                            return "";
                        }
                    }

                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new ExShowScreenMessage(1620002, 0, true, 5000)); //All Stomach Glands have been destroyed

                    startQuestTimer("stage_last_foes_message", 6000, npc, null);
                    break;

                case _earthWyrmHeart:
                    //Cancel the end task
                    QuestTimer timer = getQuestTimer("stage_last_time_elapsed", world.Trasken, null);
                    if (timer != null)
                    {
                        timer.cancel();
                    }

                    timer = getQuestTimer("stage_last_spawn_larvas", world.Trasken, null);
                    if (timer != null)
                    {
                        timer.cancel();
                    }

                    world.Trasken.setIsMortal(true);
                    world.Trasken.setIsInvul(false);

                    //System.out.println(getName() + " heart killer is: " + (player == null ? "NULL" : player.getName()));

                    ((L2Attackable) world.Trasken).addDamageHate(player, 99999, 99999);

                    world.Trasken.reduceCurrentHp(world.Trasken.getMaxHp(), player, null); //Kill trasken

                    //Hide the quest timer
                    InstanceManager.getInstance().sendPacket(world.instanceId, new ExSendUIEventRemove());
                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new ExShowScreenMessage(1620011, 0, true, 5000)); //Heart of Earth Wyrm has been destroyed
                    InstanceManager.getInstance().broadcastMovie(49, world.instanceId);
                    InstanceManager.getInstance().setInstanceReuse(world.instanceId, _template, true);
                    InstanceManager.getInstance().finishInstance(world.instanceId, true);
                    startQuestTimer("stage_last_teleport_back", ScenePlayerDataTable.getInstance().getVideoDuration(49),
                            npc, null);

                    break;
            }
        }
        return super.onKill(npc, player, isPet);
    }

    @Override
    public final String onTalk(L2Npc npc, L2PcInstance player)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onTalk: " + player.getName());
        }

        int npcId = npc.getNpcId();
        if (npcId == _enterNpc)
        {
            return "Daichir.html";
        }
        else if (npcId == _teleDevice)
        {
            player.setInstanceId(0);
            player.teleToLocation(87608, -143201, -1295, true);
        }
        return super.onTalk(npc, player);
    }

    @Override
    public String onSpawn(L2Npc npc)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onSpawn: " + npc.getName());
        }

        switch (npc.getNpcId())
        {
            case _earthWyrmHeart:
            case _tentacleSmall:
            case _tentacleMedium:
            case _stomachGland:
                npc.setIsImmobilized(true);
                break;
        }
        return super.onSpawn(npc);
    }

    private final synchronized void enterInstance(L2PcInstance player, int template_id)
    {
        InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
        if (world != null)
        {
            if (!(world instanceof TraskenWorld))
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
                    switch (world.status)
                    {
                        case 0:
                            player.teleToLocation(_enterCords, true);
                            break;

                        case 1:
                            player.teleToLocation(_enterCaveCords, true);
                            break;

                        case 2:
                            player.teleToLocation(_traskenInterior, true);
                            break;

                        case 3:
                            player.teleToLocation(_caveCords, true);
                            break;
                    }
                }
            }
            return;
        }
        else
        {
            if (!_debug && !InstanceManager.getInstance()
                    .checkInstanceConditions(player, template_id, Config.TRASKEN_MIN_PLAYERS, 100, 92,
                            Config.MAX_LEVEL))
            {
                return;
            }

            final int instanceId = InstanceManager.getInstance().createDynamicInstance(_qn + ".xml");
            world = new TraskenWorld();
            world.instanceId = instanceId;
            world.templateId = template_id;
            world.status = 0;

            InstanceManager.getInstance().addWorld(world);

            List<L2PcInstance> allPlayers = new ArrayList<L2PcInstance>();
            if (_debug)
            {
                allPlayers.add(player);
            }
            else
            {
                allPlayers.addAll(Config.TRASKEN_MIN_PLAYERS > Config.MAX_MEMBERS_IN_PARTY ?
                        player.getParty().getCommandChannel().getMembers() :
                        player.getParty().getCommandChannel() != null ?
                                player.getParty().getCommandChannel().getMembers() :
                                player.getParty().getPartyMembers());
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
                enterPlayer.teleToLocation(_enterCords, true);
            }

            startQuestTimer("stage_1_start", 60000, null, player);

            Log.fine(getName() + ": [" + template_id + "] instance started: " + instanceId + " created by player: " +
                    player.getName());
            return;
        }
    }

    public static void main(String[] args)
    {
        new Trasken(-1, _qn, "instances/GrandBosses");
    }
}
