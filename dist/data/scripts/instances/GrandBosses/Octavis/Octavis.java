package instances.GrandBosses.Octavis;

import l2server.Config;
import l2server.gameserver.ai.L2NpcWalkerAI;
import l2server.gameserver.datatables.ScenePlayerDataTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.L2NpcWalkerNode;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2DoorInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

import ai.group_template.L2AttackableAIScript;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LasTravel
 * @author Pere
 *         <p>
 *         Octavis Boss - Normal/Extreme Mode
 *         <p>
 *         Source:
 *         - http://www.youtube.com/watch?v=nu2WMeW9pJ0&feature=related
 *         - http://www.youtube.com/watch?v=N4PXCfIRHzA&feature=related
 *         - http://www.youtube.com/watch?v=CgSr7qtj1T4
 *         - http://www.youtube.com/watch?v=OELyergKzVY
 *         - 23088 healers disable
 */

public class Octavis extends L2AttackableAIScript
{
    //Quest
    private static final boolean _debug = false;
    private static final String _qn = "Octavis";

    //Ids
    private static final int _lydia = 32892;
    private static final int _lydiaInner = 33882;
    private static final int _octavisCrystal = 37505;
    private static final int _heroOfTheArena = 23089;
    private static final int _octavisInfluenceDummy = 18984;
    private static final int[] _templates = {180, 181};
    private static final int[] _minionDoorIds = {26210101, 26210102, 26210103, 26210104, 26210105, 26210106};
    private static final int[] _allMobs =
            {29209, 29211, 29212, 29210, 23086, 23088, 23146, 29191, 29193, 29194, 29192, 22928, 22930, 23144};
    private static final int[] _octavisInfluenceIds = {14279, 14280, 14281};

    //Skills
    private static final L2Skill _heroArrowShaft = SkillTable.getInstance().getInfo(14285, 1);
    private static final L2Skill _heroInfluence1 = SkillTable.getInstance().getInfo(14028, 1); //Stage 1
    //private static final L2Skill	_heroInfluence2		= SkillTable.getInstance().getInfo(14029, 1);	//Last stage
    private static final L2Skill _lionsAttack = SkillTable.getInstance().getInfo(14024, 1);
    private static final L2Skill _octavisObedience = SkillTable.getInstance().getInfo(14282, 1);

    //Cords
    private static final Location[] _enterCords = {
            new Location(210646, 118783, -9996),
            new Location(210898, 119152, -9996),
            new Location(210503, 119168, -9996),
            new Location(211026, 118601, -9996),
            new Location(210275, 118595, -9996)
    };

    //Spawns
    private static final int[][] _gladiatorSpawns = {
            {206515, 122206, -9975, 49151},
            {207869, 122211, -9975, 44315},
            {208825, 121253, -9975, 37421},
            {208822, 119898, -9975, 29865},
            {207868, 118945, -9976, 18939},
            {206513, 118935, -9975, 12266}
    };

    private static final int[][] _beastSpawns = {
            //x,y,z,heading
            {206638, 119234, -10015, 12291},
            {207748, 119238, -10015, 20329},
            {208549, 120014, -10015, 28508},
            {208531, 121132, -10015, 36123},
            {207748, 121917, -10015, 45054},
            {206633, 121917, -10015, 53212}
    };

    private static final int[][] _curatorSpawns = {
            //x,y,z,heading
            {206911, 121248, -10008, 53085},
            {207472, 121252, -10012, 45202},
            {207865, 120855, -10012, 36893},
            {207869, 120295, -10008, 28432},
            {207473, 119896, -10012, 20544},
            {206911, 119898, -10008, 12461},
            {206517, 120296, -10008, 3845},
            {206507, 120860, -10008, 61612}
    };

    private static final int[][] _heroOfTheArenaSpawns = {
            //x,y,z,heading
            {207579, 120506, -10015, 32767},
            {207514, 120347, -10014, 25521},
            {207419, 120251, -10014, 24175},
            {207259, 120189, -10015, 16383},
            {207126, 120186, -10015, 16383},
            {206965, 120252, -10015, 7744},
            {206870, 120348, -10015, 7866},
            {206803, 120508, -10015, 65188},
            {206811, 120642, -10015, 64987},
            {206871, 120805, -10015, 56909},
            {207126, 120960, -10015, 48007},
            {207261, 120956, -10015, 49015},
            {207415, 120891, -10015, 41553},
            {207514, 120798, -10015, 40429},
            {207572, 120641, -10015, 32977}
    };

    //Others
    private static final int[][] _walkRoutes = {
            {208006, 120929, -10008},
            //1
            {207546, 121391, -10008},
            {206860, 121394, -10008},
            {206374, 120902, -10008},
            {206405, 120215, -10008},
            {206858, 119745, -10008},
            {207530, 119756, -10008},
            {208013, 120228, -10008}
    };

    private class OctavisWorld extends InstanceWorld
    {
        private ArrayList<L2PcInstance> rewardedPlayers;
        private ArrayList<L2Npc> curatorMinions;
        private ArrayList<L2Npc> allMinions;
        private L2NpcWalkerAI lionsAI;
        private L2Npc Dummy;
        private L2Npc octavisBoss;
        private L2Npc octavisLions;
        private int octavisLionsId;
        private int firstOctavisId;
        private int secondOctavisId;
        private int lastOctavisId;
        private int octavisGladiatorId; //1
        private int octavisCuratorId; //3
        private int beastOfTheArena; //2
        private boolean isHardMode;

        public OctavisWorld()
        {
            isHardMode = false;
            rewardedPlayers = new ArrayList<L2PcInstance>();
            curatorMinions = new ArrayList<L2Npc>();
            allMinions = new ArrayList<L2Npc>();
        }
    }

    public Octavis(int questId, String name, String descr)
    {
        super(questId, name, descr);

        addTalkId(_lydia);
        addStartNpc(_lydia);
        addFirstTalkId(_lydiaInner);
        addTalkId(_lydiaInner);
        addStartNpc(_lydiaInner);

        for (int id : _allMobs)
        {
            addAttackId(id);
            addKillId(id);
            addSpellFinishedId(id);
        }
    }

    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onFirstTalk: " + player);
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
            Log.warning(getName() + ": onFirstTalk: Unable to get world.");
            return null;
        }

        if (wrld != null && wrld instanceof OctavisWorld)
        {
            if (npc.getNpcId() == _lydiaInner)
            {
                return "LydiaInner.html";
            }
        }
        return super.onFirstTalk(npc, player);
    }

    @Override
    public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onSpellFinished: " + skill.getName());
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
            Log.warning(getName() + ": onSpellFinished: Unable to get world.");
            return null;
        }

        if (wrld != null && wrld instanceof OctavisWorld)
        {
            OctavisWorld world = (OctavisWorld) wrld;
            if (npc.getNpcId() == world.firstOctavisId)
            {
                if (skill == _heroArrowShaft)
                {
                    world.octavisLions.setIsRunning(true);
                    world.octavisBoss.setIsRunning(true);
                    world.lionsAI.walkToLocation();

                    world.octavisBoss.getAI().startFollow(world.octavisLions, 100);

                    //Dummy Attack
                    int playerId = world.allowed.get(Rnd.get(world.allowed.size()));
                    world.octavisBoss.setTarget(world.octavisBoss);

                    L2PcInstance target = L2World.getInstance().getPlayer(playerId);
                    if (_debug)
                    {
                        Log.warning(getName() + ": Target: " + target.getName());
                    }

                    world.Dummy.setTarget(target);
                    world.Dummy.doCast(_heroInfluence1);
                }
            }
            else if (npc.getNpcId() == world.lastOctavisId)
            {
                if (Util.contains(_octavisInfluenceIds, skill.getId()))
                {
                    for (L2Npc curator : world.curatorMinions)
                    {
                        if (curator == null || curator.isDead())
                        {
                            continue;
                        }

                        curator.disableCoreAI(true);
                        ((L2Attackable) curator).clearAggroList();
                        curator.setTarget(world.octavisBoss);
                        curator.doCast(_octavisObedience);
                    }
                }
            }
            else if (npc.getNpcId() == world.octavisCuratorId)
            {
                if (npc.isCoreAIDisabled())
                {
                    npc.disableCoreAI(false);
                }
            }
        }
        return super.onSpellFinished(npc, player, skill);
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

        if (wrld != null && wrld instanceof OctavisWorld)
        {
            OctavisWorld world = (OctavisWorld) wrld;
            if (event.equalsIgnoreCase("stage_1_open_doors"))
            {
                for (L2DoorInstance door : InstanceManager.getInstance().getInstance(world.instanceId).getDoors())
                {
                    door.openMe();
                }
                startQuestTimer("stage_1_intro", _debug ? 60000 : 5 * 60000, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro"))
            {
                for (L2DoorInstance door : InstanceManager.getInstance().getInstance(world.instanceId).getDoors())
                {
                    door.closeMe();
                }

                //kick buggers
                /*for (int objId : world.allowed)
                {
					L2PcInstance pl = L2World.getInstance().getPlayer(objId);
					if (pl != null && pl.isOnline() && pl.getInstanceId() == world.instanceId)
					{
						if (pl != null && pl.getY()  < 145039)
						{
							world.allowed.remove((Integer)pl.getObjectId());
							pl.logout(true);
						}
					}
				}*/

                InstanceManager.getInstance().showVidToInstance(35, world.instanceId);

                startQuestTimer("stage_1_spawnboss", ScenePlayerDataTable.getInstance().getVideoDuration(35) + 2000,
                        null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_spawnboss"))
            {
                world.octavisLions = addSpawn(world.octavisLionsId, 207187, 120575, -10006, 65463, false, 0, false,
                        world.instanceId);
                world.octavisLions.setIsRunning(true);
                world.octavisLions.setIsMortal(false); //Lions cant die
                world.octavisBoss = addSpawn(world.firstOctavisId, 206990, 120575, -10006, 65004, false, 0, false,
                        world.instanceId);
                world.octavisBoss.disableCoreAI(true);
                world.octavisBoss.setIsInvul(true);
                world.octavisBoss.setIsRunning(true);
                world.octavisBoss.getAI().startFollow(world.octavisLions, 105);

                world.Dummy = addSpawn(_octavisInfluenceDummy, 207190, 120568, -10011, 47671, false, 0, false,
                        world.instanceId);
            }
            else if (event.equalsIgnoreCase("stage_1_start_run"))
            {
                world.lionsAI = new L2NpcWalkerAI(world.octavisLions);

                world.octavisLions.setAI(world.lionsAI);

                List<L2NpcWalkerNode> route = new ArrayList<L2NpcWalkerNode>();
                for (int[] coord : _walkRoutes)
                {
                    route.add(new L2NpcWalkerNode(coord[0], coord[1], coord[2], 0, "", true));
                }

                world.lionsAI.initializeRoute(route, null);
                world.lionsAI.walkToLocation();
            }
            else if (event.equalsIgnoreCase("stage_1_start_octavis_attack"))
            {
                //Cancel the task
                if (world.status != 1 || world.octavisLions == null || world.octavisBoss == null)
                {
                    QuestTimer timer = getQuestTimer("stage_1_start_octavis_attack", npc, null);
                    if (timer != null)
                    {
                        timer.cancel();
                        return super.onAdvEvent(event, npc, player);
                    }
                }

                world.octavisLions.stopMove(null);

                for (L2PcInstance target : world.octavisLions.getKnownList().getKnownPlayersInRadius(150))
                {
                    if (target == null)
                    {
                        continue;
                    }

                    world.octavisLions.setTarget(target);
                    world.octavisLions.doCast(_lionsAttack);

                    break;
                }

                world.octavisBoss.getAI().stopFollow();
                world.octavisBoss.getSpawn().setX(world.octavisBoss.getX());
                world.octavisBoss.getSpawn().setY(world.octavisBoss.getY());
                world.octavisBoss.getSpawn().setZ(world.octavisBoss.getZ());
                world.octavisBoss.doCast(_heroArrowShaft);
            }
            else if (event.equalsIgnoreCase("stage_2_spawnboss"))
            {
                world.status = 3;

                world.octavisBoss =
                        addSpawn(world.secondOctavisId, 207187, 120576, -10008, 52, false, 0, false, world.instanceId);
            }
            else if (event.equalsIgnoreCase("stage_2_spawnMinions"))
            {
                if (world.status == 4)
                {
                    for (int[] id : _beastSpawns)
                    {
                        L2Npc minion = addSpawn(world.beastOfTheArena, id[0], id[1], id[2], id[3], false, 0, true,
                                world.instanceId);
                        world.allMinions.add(minion);
                        minion.setIsRunning(true);

                        L2Spawn spawn = minion.getSpawn();
                        spawn.setRespawnDelay(20);
                        spawn.startRespawn();
                    }

                    for (int[] id : _gladiatorSpawns)
                    {
                        L2Npc minion = addSpawn(world.octavisGladiatorId, id[0], id[1], id[2], id[3], false, 0, true,
                                world.instanceId);
                        world.allMinions.add(minion);
                        minion.setIsRunning(true);

                        L2Spawn spawn = minion.getSpawn();
                        spawn.setRespawnDelay(20);
                        spawn.startRespawn();
                    }

                    for (int doorid : _minionDoorIds)
                    {
                        InstanceManager.getInstance().getInstance(world.instanceId).getDoor(doorid).openMe();
                    }
                }
            }
            else if (event.equalsIgnoreCase("stage_2_spawnHeros"))
            {
                if (world.status == 4)
                {
                    for (int[] id : _heroOfTheArenaSpawns)
                    {
                        L2Npc minion =
                                addSpawn(_heroOfTheArena, id[0], id[1], id[2], id[3], false, 0, true, world.instanceId);
                        world.allMinions.add(minion);
                        minion.setIsRunning(true);

                        L2Spawn spawn = minion.getSpawn();
                        spawn.setRespawnDelay(20);
                        spawn.startRespawn();
                    }
                }
            }
            else if (event.equalsIgnoreCase("stage_last_spawnboss"))
            {
                world.status = 6;

                world.octavisBoss =
                        addSpawn(world.lastOctavisId, 207187, 120575, -10008, 65457, false, 0, false, world.instanceId);
            }
            else if (event.equalsIgnoreCase("stage_last_spawnCurators"))
            {
                for (int[] id : _curatorSpawns)
                {
                    L2Npc minion = addSpawn(world.octavisCuratorId, id[0], id[1], id[2], id[3], false, 0, true,
                            world.instanceId);
                    world.allMinions.add(minion);
                    world.curatorMinions.add(minion);
                    minion.setIsRunning(true);
                    L2Spawn spawn = minion.getSpawn();
                    spawn.setRespawnDelay(20);
                    spawn.startRespawn();
                }
            }
            else if (event.equalsIgnoreCase("stage_last_lydia_spawn"))
            {
                InstanceManager.getInstance().sendDelayedPacketToInstance(world.instanceId, 5,
                        new ExShowScreenMessage(1802377, 0, true, 5000));

                addSpawn(_lydiaInner, 207194, 120574, -10010, 60699, false, 0, false, world.instanceId);
            }
            else if (event.equalsIgnoreCase("tryGetReward"))
            {
                synchronized (world.rewardedPlayers)
                {
                    if (InstanceManager.getInstance().canGetUniqueReward(player, world.rewardedPlayers))
                    {
                        world.rewardedPlayers.add(player);

                        player.addItem(_qn, _octavisCrystal, 1, npc, true);
                    }
                    else
                    {
                        player.sendMessage("Nice attempt, but you already got a reward!");
                    }
                }
            }
        }
        if (npc != null && npc.getNpcId() == _lydia && Util.isDigit(event) &&
                Util.contains(_templates, Integer.valueOf(event)))
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
        if (tmpWorld instanceof OctavisWorld)
        {
            final OctavisWorld world = (OctavisWorld) tmpWorld;

            if (npc.getNpcId() == world.octavisLionsId)
            {
                if (world.status == 0)
                {
                    world.status = 1;

                    startQuestTimer("stage_1_start_run", 1000, npc, null);
                    startQuestTimer("stage_1_start_octavis_attack", 30000, npc, null, true);
                }

                if (npc.getCurrentHp() < npc.getMaxHp() * 0.85 && world.octavisBoss.getDisplayEffect() == 0)
                {
                    world.octavisBoss.setDisplayEffect(1);
                }
                else if (npc.getCurrentHp() < npc.getMaxHp() * 0.75 && world.octavisBoss.getDisplayEffect() == 1)
                {
                    world.octavisBoss.setDisplayEffect(2);
                }
                else if (npc.getCurrentHp() < npc.getMaxHp() * 0.65 && world.octavisBoss.getDisplayEffect() == 2)
                {
                    world.octavisBoss.setDisplayEffect(3);
                }
                else if (npc.getCurrentHp() < npc.getMaxHp() * 0.55 && world.octavisBoss.getDisplayEffect() == 3)
                {
                    world.octavisBoss.setDisplayEffect(4);
                }
                else if (npc.getCurrentHp() < npc.getMaxHp() * 0.50 && world.octavisBoss.getDisplayEffect() == 4)
                {
                    world.octavisBoss.setDisplayEffect(5);
                }
            }
            else if (npc.getNpcId() == world.firstOctavisId)
            {
                if (world.status == 1)
                {
                    if (npc.getCurrentHp() < npc.getMaxHp() * 0.15)
                    {
                        world.status = 2;

                        world.octavisBoss.deleteMe();
                        world.octavisLions.deleteMe();

                        InstanceManager.getInstance().showVidToInstance(36, world.instanceId);

                        startQuestTimer("stage_2_spawnboss",
                                ScenePlayerDataTable.getInstance().getVideoDuration(36) + 2000, npc, null);
                    }

                    if (world.octavisLions.getCurrentHp() < world.octavisLions.getMaxHp() * 0.50)
                    {
                        npc.setIsInvul(false);
                    }
                    else
                    {
                        npc.setIsInvul(true);
                    }
                }
            }
            else if (npc.getNpcId() == world.secondOctavisId)
            {
                if (world.status == 3)
                {
                    world.status = 4;

                    startQuestTimer("stage_2_spawnMinions", 10000, npc, null);

                    if (world.isHardMode)
                    {
                        startQuestTimer("stage_2_spawnHeros", 40000, npc, null);
                    }
                }
                else if (world.status == 4 && npc.getNpcId() == world.secondOctavisId &&
                        npc.getCurrentHp() < npc.getMaxHp() * 0.15)
                {
                    world.status = 5;

                    InstanceManager.getInstance().despawnAll(world.instanceId);
                    InstanceManager.getInstance().showVidToInstance(37, world.instanceId);

                    startQuestTimer("stage_last_spawnboss",
                            ScenePlayerDataTable.getInstance().getVideoDuration(37) + 2000, npc, null);
                }
            }
            else if (npc.getNpcId() == world.lastOctavisId)
            {
                if (world.status == 6)
                {
                    world.status = 7;

                    startQuestTimer("stage_last_spawnCurators", 10000, npc, null);
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
        if (tmpworld instanceof OctavisWorld)
        {
            OctavisWorld world = (OctavisWorld) tmpworld;
            if (npc.getNpcId() == world.lastOctavisId)
            {
                InstanceManager.getInstance().despawnAll(world.instanceId);
                InstanceManager.getInstance().showVidToInstance(38, world.instanceId);
                InstanceManager.getInstance().setInstanceReuse(world.instanceId, world.templateId,
                        world.templateId == _templates[0] ? false : true);
                InstanceManager.getInstance().finishInstance(world.instanceId, false);

                if (world.isHardMode)
                {
                    startQuestTimer("stage_last_lydia_spawn", ScenePlayerDataTable.getInstance().getVideoDuration(38),
                            npc, null);
                }
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
        if (npcId == _lydia)
        {
            return "Lydia.html";
        }

        return "";
    }

    private void setupIDs(OctavisWorld world, int template_id)
    {
        if (template_id == 181) //extreme mode
        {
            world.firstOctavisId = 29209;
            world.secondOctavisId = 29211;
            world.lastOctavisId = 29212;
            world.octavisLionsId = 29210;
            world.octavisGladiatorId = 23086;
            world.octavisCuratorId = 23088;
            world.beastOfTheArena = 23087;
            world.isHardMode = true;
        }
        else
        //180
        {
            world.firstOctavisId = 29191;
            world.secondOctavisId = 29193;
            world.lastOctavisId = 29194;
            world.octavisLionsId = 29192;
            world.octavisGladiatorId = 22928;
            world.octavisCuratorId = 22930;
            world.beastOfTheArena = 22929;
        }
    }

    private final synchronized void enterInstance(L2PcInstance player, int template_id)
    {
        InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
        if (world != null)
        {
            if (!(world instanceof OctavisWorld))
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
                    player.teleToLocation(208195, 120574, -10015);
                }
            }
            return;
        }
        else
        {
            int minPlayers = template_id == 181 ? Config.OCTAVIS_MIN_PLAYERS : Config.OCTAVIS_MIN_PLAYERS / 2;
            int maxLevel = template_id == 181 ? Config.MAX_LEVEL : 99;
            if (!_debug && !InstanceManager.getInstance()
                    .checkInstanceConditions(player, template_id, minPlayers, 49, 92, maxLevel))
            {
                return;
            }

            final int instanceId = InstanceManager.getInstance().createDynamicInstance(_qn + ".xml");
            world = new OctavisWorld();
            world.instanceId = instanceId;
            world.templateId = template_id;
            world.status = 0;

            InstanceManager.getInstance().addWorld(world);

            setupIDs((OctavisWorld) world, template_id);

            List<L2PcInstance> allPlayers = new ArrayList<L2PcInstance>();
            if (_debug)
            {
                allPlayers.add(player);
            }
            else
            {
                allPlayers.addAll(minPlayers > Config.MAX_MEMBERS_IN_PARTY ?
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
                enterPlayer.teleToLocation(_enterCords[Rnd.get(0, _enterCords.length - 1)], true);
            }

            startQuestTimer("stage_1_open_doors", 3000, null, player);

            Log.fine(getName() + ": [" + template_id + "] instance started: " + instanceId + " created by player: " +
                    player.getName());
            return;
        }
    }

    public static void main(String[] args)
    {
        new Octavis(-1, _qn, "instances/GrandBosses");
    }
}
