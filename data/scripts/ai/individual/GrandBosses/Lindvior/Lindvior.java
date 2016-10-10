package ai.individual.GrandBosses.Lindvior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.ScenePlayerDataTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.instancemanager.GrandBossManager;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2GrandBossInstance;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.model.zone.type.L2BossZone;
import l2server.gameserver.network.serverpackets.Earthquake;
import l2server.gameserver.network.serverpackets.EventTrigger;
import l2server.gameserver.network.serverpackets.ExSendUIEvent;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.SocialAction;
import l2server.gameserver.util.Broadcast;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;
import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 * @author Pere
 *         <p>
 *         Lindvior Boss - Normal Mode
 *         <p>
 *         Source:
 *         - http://www.youtube.com/watch?v=QlHvI54oyJo (Retail part 1)
 *         - http://www.youtube.com/watch?v=9GMk6q4rjys (Retail part 2)
 *         - http://www.youtube.com/watch?v=sVFNT8tdagA (Retail part3)
 *         - http://www.youtube.com/watch?v=vKwf8Jx_Qtc (Retail failed boss)
 *         - http://www.youtube.com/watch?v=dG9OMGGg1ao
 */

public class Lindvior extends L2AttackableAIScript
{
    //Quest
    private static final boolean _debug = false;
    private static final String _qn = "Lindvior";

    //Id's
    private static final int _npcEnterId = 33881;
    private static final int _generatorId = 19426;
    private static final int _generatorGuard = 19479;
    private static final int _giantCycloneId = 19427;
    private static final int _cycloneId = 25898;
    private static final int _firstFloorLindvior = 25899;
    private static final int _secondFloorLindvior = 29240;
    private static final int _flyLindvior = 19424;
    private static final int[] _lindviorIds = {_firstFloorLindvior, _secondFloorLindvior, _flyLindvior};
    private static final int[] _lynDracoIds = {25895, 25896, 25897, 29241, 29242, 29243};
    private static final int[] _allMinionIds = {
            _lynDracoIds[0],
            _lynDracoIds[1],
            _lynDracoIds[2],
            _lynDracoIds[3],
            _lynDracoIds[4],
            _lynDracoIds[5],
            _cycloneId,
            _giantCycloneId
    };
    private static final int _flyingLindviorAroundZoneId = 19423;
    private static final int _lindviorCameraId = 19428;
    //private static final int _redCircle					= 19391;
    //private static final int _blueCircle					= 19392;
    private static final double maxGeneratorDamage = 1500000;
    private static final L2BossZone _bossZone = GrandBossManager.getInstance().getZone(45697, -26269, -1409);

    //Effects
    private static final int _allGeneratorsConnectedEffect = 21170110;
    private static final int _redTowerEffect = 21170112;
    private static final int _shieldTowerEffect = 21170100;
    private static final int _generatorEffect_1 = 21170104;
    private static final int _generatorEffect_2 = 21170102;
    private static final int _generatorEffect_3 = 21170106;
    private static final int _generatorEffect_4 = 21170108;
    private static final int _redZoneEffect = 21170120;

    //Cords
    private static final Location _enterCords = new Location(46931, -28813, -1406);

    //Skills
    //private static final L2Skill _rechargePossible 		= SkillTable.getInstance().getInfo(15605, 1);
    //private static final L2Skill _recharge 				= SkillTable.getInstance().getInfo(15606, 1);
    private static final L2Skill _takeOff = SkillTable.getInstance().getInfo(15596, 1);

    //Vars
    private L2Npc _dummyLindvior;
    private L2Npc _lindviorBoss;
    private Location _bossLocation;
    private static long _LastAction;
    private int _bossStage;
    private Map<L2Npc, Double> _manageGenerators = new HashMap<L2Npc, Double>();

    public Lindvior(int questId, String name, String descr)
    {
        super(questId, name, descr);

        addTalkId(_npcEnterId);
        addStartNpc(_npcEnterId);
        addSpawnId(_generatorGuard);
        addFirstTalkId(_generatorId);
        addTalkId(_generatorId);
        addStartNpc(_generatorId);
        addSpawnId(_generatorId);
        addAttackId(_generatorId);

        for (int a : _allMinionIds)
        {
            addAttackId(a);
            addKillId(a);
        }

        for (int a : _lindviorIds)
        {
            addSpellFinishedId(a);
            addAttackId(a);
            addKillId(a);
        }

        //Unlock
        startQuestTimer("unlock_lindvior", GrandBossManager.getInstance()
                .getUnlockTime(_secondFloorLindvior), null, null);
    }

    @Override
    public String onSpawn(L2Npc npc)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onSpawn: " + npc.getName());
        }

        if (npc.getNpcId() == _generatorId)
        {
            npc.disableCoreAI(true);
            npc.setDisplayEffect(1);
            npc.setIsMortal(false);
            npc.setIsInvul(true); //Can't get damage now
        }

        if (npc.getNpcId() == _generatorGuard)
        {
            npc.setIsInvul(true);
        }

        return super.onSpawn(npc);
    }

    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onFirstTalk: " + player.getName());
        }

        if (npc.getNpcId() == _generatorId)
        {
            if (_bossStage == 1)
            {
                return npc.getDisplayEffect() == 1 ? "Generator.html" : "GeneratorDone.html";
            }
            else
            {
                player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, npc);
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

        int npcId = npc.getNpcId();
        if (npcId == _npcEnterId)
        {
            int lindStatus = GrandBossManager.getInstance().getBossStatus(_secondFloorLindvior);

            final List<L2PcInstance> allPlayers = new ArrayList<L2PcInstance>();
            if (lindStatus == GrandBossManager.getInstance().DEAD)
            {
                return "33881-01.html";
            }
            else
            {
                if (!_debug)
                {
                    if (lindStatus == GrandBossManager.getInstance().ALIVE && !InstanceManager.getInstance()
                            .checkInstanceConditions(player, -1, Config.LINDVIOR_MIN_PLAYERS, 200, 95,
                                    Config.MAX_LEVEL))
                    {
                        return null;
                    }
                    else if (lindStatus == GrandBossManager.getInstance().WAITING && !InstanceManager.getInstance()
                            .checkInstanceConditions(player, -1, Config.LINDVIOR_MIN_PLAYERS, 200, 95,
                                    Config.MAX_LEVEL))
                    {
                        return null;
                    }
                    else if (lindStatus == GrandBossManager.getInstance().FIGHTING)
                    {
                        return null;
                    }
                }
            }

            if (lindStatus == GrandBossManager.getInstance().ALIVE)
            {
                GrandBossManager.getInstance()
                        .setBossStatus(_secondFloorLindvior, GrandBossManager.getInstance().WAITING);

                startQuestTimer("stage_1_start", 1000, null, null);
            }

            if (_debug)
            {
                allPlayers.add(player);
            }
            else
            {
                allPlayers.addAll(Config.LINDVIOR_MIN_PLAYERS > Config.MAX_MEMBERS_IN_PARTY || player.getParty()
                        .isInCommandChannel() ? player.getParty().getCommandChannel().getMembers() : player.getParty()
                        .getPartyMembers());
            }

            for (L2PcInstance enterPlayer : allPlayers)
            {
                if (enterPlayer == null)
                {
                    continue;
                }

                _bossZone.allowPlayerEntry(enterPlayer, 7200);

                enterPlayer.sendPacket(new EventTrigger(_redTowerEffect, true));
                enterPlayer.teleToLocation(_enterCords, true);
            }
        }

        return "";
    }

    @Override
    public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onAdvEvent: " + event);
        }

        if (event.equalsIgnoreCase("unlock_lindvior"))
        {
            L2Npc lindvior = addSpawn(_secondFloorLindvior, -105200, -253104, -15264, 32768, false, 0);

            GrandBossManager.getInstance().addBoss((L2GrandBossInstance) lindvior);
            GrandBossManager.getInstance().setBossStatus(_secondFloorLindvior, GrandBossManager.getInstance().ALIVE);

            Broadcast.toAllOnlinePlayers(new Earthquake(45697, -26269, -1409, 20, 10));
        }
        else if (event.equalsIgnoreCase("check_activity_task"))
        {
            if (!GrandBossManager.getInstance().isActive(_secondFloorLindvior, _LastAction))
            {
                notifyEvent("end_lindvior", null, null);
            }
        }
        else if (event.equalsIgnoreCase("stage_1_start"))
        {
            SpawnTable.getInstance().spawnSpecificTable("lindvior_boss");

            _bossStage = 1;

            _bossZone
                    .sendDelayedPacketToZone(5000,
                            new ExShowScreenMessage(14211701, 0, true, 5000)); //You must activate the 4 Generators.

            //Generator guards should broadcast npcStringId 1802366

            _dummyLindvior = addSpawn(_lindviorCameraId, 45259, -27115, -638, 41325, false, 0, false);

            _LastAction = System.currentTimeMillis();

            startQuestTimer("check_activity_task", 60000, null, null, true);
        }
        else if (event.equalsIgnoreCase("stage_1_activate_generator"))
        {
            if (_bossStage == 1)
            {
                if (npc.getDisplayEffect() == 1) //Orange
                {
                    npc.setDisplayEffect(2); //Blue

                    int generatorEffect = _generatorEffect_1;
                    if (Util.checkIfInRange(500, npc.getX(), npc.getY(), npc.getZ(), 45283, -30372, -1405, false))
                    {
                        generatorEffect = _generatorEffect_2;
                    }
                    else if (Util.checkIfInRange(500, npc.getX(), npc.getY(), npc.getZ(), 45283, -23967, -1405, false))
                    {
                        generatorEffect = _generatorEffect_3;
                    }
                    else if (Util.checkIfInRange(500, npc.getX(), npc.getY(), npc.getZ(), 42086, -27179, -1405, false))
                    {
                        generatorEffect = _generatorEffect_4;
                    }

                    _bossZone.broadcastPacket(new EventTrigger(generatorEffect, true));

                    _bossZone.broadcastPacket(new Earthquake(npc.getX(), npc.getY(), npc.getZ(), 10, 10));

                    synchronized (_manageGenerators)
                    {
                        if (_manageGenerators.size() < 3)
                        {
                            _bossZone
                                    .broadcastPacket(new ExShowScreenMessage(14211701, 0, true,
                                            5000)); //You must activate the 4 Generators.
                        }

                        if (!_manageGenerators.containsKey(npc))
                        {
                            _manageGenerators.put(npc, (double) 0);
                        }

                        if (_manageGenerators.size() == 4)
                        {
                            //All generators are active now
                            //Here the center shield should opens (missing) and Lindvior should appear
                            _bossStage = 2;

                            //Spawn the dummy cyclone
                            for (L2Npc generator : _manageGenerators.keySet())
                            {
                                if (generator == null)
                                {
                                    continue;
                                }

                                //Display & invul maybe should be done before the intro movie
                                generator.setDisplayEffect(1);
                                generator.setIsInvul(false); //generator now can get damage
                            }

                            _bossZone.broadcastPacket(new EventTrigger(_allGeneratorsConnectedEffect, true));

                            _bossZone.broadcastPacket(new SocialAction(_dummyLindvior.getObjectId(), 1));

                            _bossZone.broadcastPacket(new EventTrigger(_shieldTowerEffect, true));
                            _bossZone.broadcastPacket(new EventTrigger(_redTowerEffect, false));

                            startQuestTimer("stage_2_intro_start", 6500, npc, null);
                        }
                    }
                }
            }
        }
        else if (event.equalsIgnoreCase("stage_2_intro_start"))
        {
            _bossZone.showVidToZone(76);

            startQuestTimer("stage_2_start", ScenePlayerDataTable.getInstance().getVideoDuration(76) + 200, npc, null);
        }
        else if (event.equalsIgnoreCase("stage_2_start"))
        {
            _dummyLindvior.deleteMe();
            _dummyLindvior = addSpawn(_flyingLindviorAroundZoneId, 45259, -27115, -638, 41325, false, 0, false);

            _bossZone.broadcastPacket(new ExShowScreenMessage(14211702, 0, true, 5000)); //Protect the Generator!
        }
        else if (event.equalsIgnoreCase("stage_2_end"))
        {
            _bossStage = 5;

            _bossLocation = new Location(_lindviorBoss.getX(), _lindviorBoss.getY(), _lindviorBoss.getZ(), _lindviorBoss
                    .getHeading());

            _lindviorBoss.deleteMe();

            startQuestTimer("stage_3_start", 5000, npc, null);
        }
        else if (event.equalsIgnoreCase("stage_3_start"))
        {
            landingLindvior();
        }
        else if (event.equalsIgnoreCase("stage_3_end"))
        {
            _bossStage = 7;

            _bossLocation = new Location(_lindviorBoss.getX(), _lindviorBoss.getY(), _lindviorBoss.getZ(), _lindviorBoss
                    .getHeading());

            _lindviorBoss.deleteMe();

            startQuestTimer("stage_4_start", 5000, npc, null);
        }
        else if (event.equalsIgnoreCase("stage_4_start"))
        {
            landingLindvior();
        }
        else if (event.equalsIgnoreCase("stage_4_end"))
        {
            _bossLocation = new Location(_lindviorBoss.getX(), _lindviorBoss.getY(), _lindviorBoss.getZ(), _lindviorBoss
                    .getHeading());

            _lindviorBoss.deleteMe();

            startQuestTimer("stage_5_start", 5000, npc, null);
        }
        else if (event.equalsIgnoreCase("stage_5_start"))
        {
            landingLindvior();
        }
        else if (event.equalsIgnoreCase("stage_5_end"))
        {
            _bossLocation = new Location(_lindviorBoss.getX(), _lindviorBoss.getY(), _lindviorBoss.getZ(), _lindviorBoss
                    .getHeading());

            _lindviorBoss.deleteMe();

            startQuestTimer("stage_6_start", 5000, npc, null);
        }
        else if (event.equalsIgnoreCase("stage_6_start"))
        {
            landingLindvior();
        }
        else if (event.equalsIgnoreCase("spawn_minion_task"))
        {
            if (_lindviorBoss != null && !_lindviorBoss.isDead())
            {
                if (Rnd.get(100) > 30)
                {
                    if (_bossStage < 9) //Can spawn Lyns at all stages, less at the last one
                    {
                        spawnMinions(_lindviorBoss, Rnd.get(100) > 50 ? _cycloneId : -1, 1000, 20);
                    }
                    else
                    {
                        spawnMinions(_lindviorBoss, _cycloneId, 1000, 20); //Only Cyclones
                    }
                }

                //Big Cyclone only at last stage, always
                if (_bossStage >= 9)
                {
                    spawnMinions(_lindviorBoss, _giantCycloneId, 1, 2);
                }

                //Individual Player Cyclone always
                for (L2PcInstance players : _bossZone.getPlayersInside())
                {
                    if (players == null)
                    {
                        continue;
                    }

                    if (Rnd.get(100) > 40)
                    {
                        spawnMinions(players, _cycloneId, 1, 1);
                    }
                }
            }
        }
        else if (event.equalsIgnoreCase("cancel_timers"))
        {
            QuestTimer activityTimer = getQuestTimer("check_activity_task", null, null);
            if (activityTimer != null)
            {
                activityTimer.cancel();
            }

            QuestTimer spawnMinionTask = getQuestTimer("spawn_minion_task", null, null);
            if (spawnMinionTask != null)
            {
                spawnMinionTask.cancel();
            }
        }
        else if (event.equalsIgnoreCase("end_lindvior"))
        {
            notifyEvent("cancel_timers", null, null);

            _bossZone.oustAllPlayers();

            _manageGenerators.clear();

            _bossStage = 0;

            SpawnTable.getInstance().despawnSpecificTable("lindvior_boss");

            for (L2Npc mob : _bossZone.getNpcsInside())
            {
                if (mob == null)
                {
                    continue;
                }

                mob.getSpawn().stopRespawn();
                mob.deleteMe();
            }

            if (GrandBossManager.getInstance().getBossStatus(_secondFloorLindvior) != GrandBossManager
                    .getInstance().DEAD)
            {
                GrandBossManager.getInstance()
                        .setBossStatus(_secondFloorLindvior, GrandBossManager.getInstance().ALIVE);
            }
        }

        return super.onAdvEvent(event, npc, player);
    }

    private void spawnMinions(L2Character npc, int minionId, int rad, int amount)
    {
        int radius = rad;
        int mobCount = _bossZone.getNpcsInside().size();
        if (mobCount < 70)
        {
            for (int i = 0; i < amount; i++)
            {
                int x = (int) (radius * Math.cos(i * 0.618));
                int y = (int) (radius * Math.sin(i * 0.618));

                L2MonsterInstance minion = (L2MonsterInstance) addSpawn(minionId == -1 ? _lynDracoIds[Rnd
                        .get(_lynDracoIds.length)] : minionId, npc.getX() + x, npc.getY() + y, npc
                        .getZ() + 20, -1, false, 0, true, npc.getInstanceId());
                minion.setIsRunning(true);

                //To be sure
                if (!_bossZone.isInsideZone(minion))
                {
                    minion.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
                }
            }
        }
    }

    @Override
    public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onAttack: " + npc.getName());
        }

        _LastAction = System.currentTimeMillis();

        //Anti BUGGERS
        if (!_bossZone.isInsideZone(attacker)) //Character attacking out of zone
        {
            attacker.doDie(null);

            if (_debug)
            {
                Log.warning(getName() + ": Character: " + attacker.getName() + " attacked: " + npc
                        .getName() + " out of the boss zone!");
            }
        }

        if (!_bossZone.isInsideZone(npc)) //Npc moved out of the zone
        {
            int[] randPoint = _bossZone.getZone().getRandomPoint();
            npc.teleToLocation(randPoint[0], randPoint[1], randPoint[2]);

            if (_debug)
            {
                Log.warning(getName() + ": Character: " + attacker.getName() + " attacked: " + npc
                        .getName() + " wich is out of the boss zone!");
            }
        }

        if (npc.getNpcId() == _generatorId)
        {
            if (_bossStage == 2)
            {
                if (npc.getDisplayEffect() == 1)//Charge is Possible
                {
                    synchronized (_manageGenerators)
                    {
                        if (_manageGenerators.containsKey(npc))
                        {
                            if (_manageGenerators.get(npc) == 0) //First attack
                            {
                                _manageGenerators.put(npc, (double) damage);

                                _bossZone
                                        .broadcastPacket(new ExShowScreenMessage(14211702, 0, true,
                                                5000)); //Protect the Generator!

                                //Spawn Lyn Dracos
                                spawnMinions(npc, -1, 600, 7);
                            }
                            else
                            {
                                //This should be done with one special skill but we will do it with damage....
                                double generatorDamage = _manageGenerators
                                        .get(npc); //Get the current damage from this generator
                                double calcDamage = generatorDamage + damage;

                                if (generatorDamage == maxGeneratorDamage)
                                {
                                    return super.onAttack(npc, attacker, damage, isPet);
                                }
                                else
                                {
                                    if (calcDamage >= maxGeneratorDamage)
                                    {
                                        _manageGenerators.put(npc, maxGeneratorDamage);

                                        npc.broadcastPacket(new ExSendUIEvent(5, 120, 120, 16211701),
                                                1200); //bar with the 100%

                                        _bossZone.broadcastPacket(new ExShowScreenMessage("$s1 has charged the cannon!"
                                                .replace("$s1", attacker
                                                        .getName()), 5000)); //$s1 has charged the cannon!

                                        int count = 0;

                                        for (Entry<L2Npc, Double> generator : _manageGenerators.entrySet())
                                        {
                                            if (generator.getValue() == maxGeneratorDamage)
                                            {
                                                count++;
                                            }
                                        }

                                        if (count == 4)
                                        {
                                            //All generators are charged here
                                            _bossStage = 3;

                                            GrandBossManager.getInstance()
                                                    .setBossStatus(_secondFloorLindvior, GrandBossManager
                                                            .getInstance().FIGHTING);

                                            _dummyLindvior.deleteMe(); //Delete the flying Lindvior

                                            //Lindvior fall to the scenario here
                                            _bossZone.broadcastPacket(new Earthquake(npc.getX(), npc.getY(), npc
                                                    .getZ(), 10, 10));

                                            landingLindvior();

                                            _bossZone
                                                    .sendDelayedPacketToZone(5000,
                                                            new ExShowScreenMessage(14211708, 0, true,
                                                                    5000)); //Lindvior has fallen from the sky!

                                            //Start the minion task
                                            startQuestTimer("spawn_minion_task", 3 * 60000, null, null, true);

                                            //At this point all the start instance npcs are deleted
                                            for (L2Character chara : _bossZone.getCharactersInside().values())
                                            {
                                                if (chara == null || !(chara instanceof L2Npc))
                                                {
                                                    continue;
                                                }

                                                if (((L2Npc) chara).getNpcId() != _firstFloorLindvior)
                                                {
                                                    chara.deleteMe();
                                                }
                                            }

                                            //kick dual box
                                            _bossZone.kickDualBoxes();
                                        }
                                    }
                                    else
                                    {
                                        _manageGenerators.put(npc, calcDamage);

                                        double calc = generatorDamage / maxGeneratorDamage * 120;

                                        npc.broadcastPacket(new ExSendUIEvent(5, (int) calc, 120, 16211701), 1200);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else if (npc.getNpcId() == _firstFloorLindvior)
        {
            if (_bossStage == 3)
            {
                if (npc.getCurrentHp() < npc.getMaxHp() * 0.50) //50%
                {
                    _bossStage = 4;

                    takeOffLindvior();

                    _bossZone.broadcastPacket(new EventTrigger(_redZoneEffect, true));
                    _bossZone.sendDelayedPacketToZone(15000, new EventTrigger(_redZoneEffect, false));
                    _bossZone
                            .broadcastPacket(new ExShowScreenMessage(14211705, 0, true,
                                    5000)); //A fearsome power emanates from Lindvior!

                    startQuestTimer("stage_2_end", 4000, npc, null); //TODO
                }
            }
        }
        else if (npc.getNpcId() == _secondFloorLindvior)
        {
            if (_bossStage == 7)
            {
                if (npc.getCurrentHp() < npc.getMaxHp() * 0.20) //20%
                {
                    _bossStage = 8;

                    takeOffLindvior();

                    startQuestTimer("stage_4_end", 5600, npc, null);
                }
            }
        }
        else if (npc.getNpcId() == _flyLindvior)
        {
            if (_bossStage == 5)
            {
                if (npc.getCurrentHp() < npc.getMaxHp() * 0.30) //30%
                {
                    _bossStage = 6;

                    takeOffLindvior();

                    startQuestTimer("stage_3_end", 5600, npc, null);
                }
            }
            else if (_bossStage == 8)
            {
                if (npc.getCurrentHp() < npc.getMaxHp() * 0.20) //20%
                {
                    _bossStage = 9;

                    takeOffLindvior();

                    startQuestTimer("stage_5_end", 5600, npc, null);
                }
            }
        }

        return super.onAttack(npc, attacker, damage, isPet, skill);
    }

    private void takeOffLindvior()
    {
        switch (_bossStage)
        {
            case 4:
            case 6:
            case 8:
            case 9:
                _lindviorBoss.disableCoreAI(true);
                _lindviorBoss.setIsInvul(true);
                _lindviorBoss.setTarget(_lindviorBoss);

                if (_lindviorBoss.isCastingNow())
                {
                    _lindviorBoss.abortCast();
                }

                _lindviorBoss.doCast(_takeOff);
                break;
        }
    }

    private void landingLindvior()
    {
        switch (_bossStage)
        {
            case 3:
                _lindviorBoss = addSpawn(_firstFloorLindvior, 47180, -26122, -1407, 48490, false, 0, true);
                break;

            case 5:
            case 8:
                _lindviorBoss = addSpawn(_flyLindvior, _bossLocation.getX(), _bossLocation.getY(), _bossLocation
                        .getZ(), _bossLocation.getHeading(), false, 0, true);
                _lindviorBoss.setDisplayEffect(1);
                _lindviorBoss.setCurrentHp(_lindviorBoss.getMaxHp() * (_bossStage == 5 ? 0.85 : 0.40));
                break;

            case 7:
            case 9:
                _lindviorBoss = addSpawn(_secondFloorLindvior, _bossLocation.getX(), _bossLocation.getY(), _bossLocation
                        .getZ(), _bossLocation.getHeading(), false, 0, true);
                _lindviorBoss.setCurrentHp(_lindviorBoss.getMaxHp() * (_bossStage == 7 ? 0.70 : 0.20));
                break;
        }

        _lindviorBoss.setShowSummonAnimation(false);
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onKill: " + npc.getName());
        }

        if (npc.getNpcId() == _secondFloorLindvior)
        {
            GrandBossManager.getInstance().notifyBossKilled(_secondFloorLindvior);

            notifyEvent("cancel_timers", null, null);

            _bossZone.stopWholeZone();

            // Start the zone when the cameras ends
            ThreadPoolManager.getInstance().scheduleAi(new Runnable()
            {
                @Override
                public void run()
                {
                    _bossZone.startWholeZone();
                }
            }, 14000);

            startQuestTimer("unlock_lindvior", GrandBossManager.getInstance()
                    .getUnlockTime(_secondFloorLindvior), null, null);
            startQuestTimer("end_lindvior", 1800000, null, null);
        }

        return super.onKill(npc, player, isPet);
    }

    @Override
    public int getOnKillDelay(int npcId)
    {
        return 0;
    }

    public static void main(String[] args)
    {
        new Lindvior(-1, _qn, "ai/individual/GrandBosses");
    }
}
