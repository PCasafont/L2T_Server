package instances.RaidBosses.Teredor;

import java.util.ArrayList;
import java.util.List;

import l2server.Config;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.ai.L2AttackableAI;
import l2server.gameserver.ai.L2NpcWalkerAI;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.L2NpcWalkerNode;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;
import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 *         <p>
 *         Terador Boss - Default mode
 *         <p>
 *         Source:
 *         - http://www.youtube.com/watch?v=fFERpRhW52E
 *         - http://www.youtube.com/watch?v=iNMNTe2L3qU
 *         - http://www.lineage-realm.com/community/lineage-2-talk/quest-walkthroughs/Trajan-Instance-Guide-killing-teredor
 */

public class Teredor extends L2AttackableAIScript
{
    //Quest
    private static final boolean _debug = false;
    private static final String _qn = "Teredor";

    //Id's
    private static final int _templateID = 160;
    private static final int _teredor = 25785;
    private static final int _filaur = 30535;
    private static final int _egg1 = 19023;
    private static final int _egg2 = 18996;
    private static final int _eliteMillipede = 19015;
    private static final int _teredorTransparent1 = 18998;
    private static final int _adventureGuildsman = 33385;
    private static final int[] _eggMinions = {18993, 19016, 19000, 18995, 18994};
    private static final int[] _allMobs = {18993, 19016, 19000, 18995, 18994, 19023, 25785, 18996, 19015, 19024};

    //Skills
    private static final L2Skill _teredorFluInfection = SkillTable.getInstance().getInfo(14113, 1);

    //Spawns
    private static final int[] _adventureSpawn = {177228, -186305, -3800, 339};

    //Others
    private static List<L2NpcWalkerNode> _route = new ArrayList<L2NpcWalkerNode>();

    //Cords
    private static final Location[] _playerEnter = {
            new Location(186933, -173534, -3878),
            new Location(186787, -173618, -3878),
            new Location(186907, -173708, -3878),
            new Location(187048, -173699, -3878),
            new Location(186998, -173579, -3878)
    };

    private static final int[][] _walkRoutes = {
            {177127, -185282, -3804, 19828},
            {177138, -184701, -3804, 16417},
            {176616, -184448, -3796, 29126},
            {176067, -184240, -3804, 28990},
            {176038, -184806, -3804, 48098},
            {175494, -185097, -3804, 37891},
            {175347, -185711, -3804, 46649},
            {175912, -186311, -3804, 57030},
            {176449, -186195, -3804, 1787}
    };

    private class TeredorWorld extends InstanceWorld
    {
        private L2Npc Teredor;
        private boolean bossIsReady;
        private boolean bossIsInPause;
        private L2NpcWalkerAI teredorWalkAI;
        private L2AttackableAI teredorAttackAI;

        public TeredorWorld()
        {
            bossIsReady = true;
            bossIsInPause = true;
        }
    }

    public Teredor(int questId, String name, String descr)
    {
        super(questId, name, descr);

        addTalkId(_adventureGuildsman);
        addTalkId(_filaur);
        addStartNpc(_filaur);
        addSpellFinishedId(_teredor);
        addAggroRangeEnterId(_egg1);
        addAggroRangeEnterId(_egg2);
        addAggroRangeEnterId(_teredorTransparent1);

        for (int id : _allMobs)
        {
            addKillId(id);
            addAttackId(id);
        }

        for (int[] coord : _walkRoutes)
        {
            _route.add(new L2NpcWalkerNode(coord[0], coord[1], coord[2], 0, "", true));
        }
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
            Log.warning(getName() + ": onAdvEvent: Unable to get world.");
            return null;
        }

        if (wrld != null && wrld instanceof TeredorWorld)
        {
            TeredorWorld world = (TeredorWorld) wrld;
            if (npc.getNpcId() == _teredor && skill.getId() == 14112) //Teredor Poison
            {
                for (L2PcInstance players : L2World.getInstance().getAllPlayers().values())
                {
                    if (players != null && players.getInstanceId() == world.instanceId)
                    {
                        addSpawn(_teredorTransparent1, players.getX(), players.getY(), players
                                .getZ(), 0, false, 0, true, world.instanceId);
                    }
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

        if (wrld != null && wrld instanceof TeredorWorld)
        {
            TeredorWorld world = (TeredorWorld) wrld;
            if (event.equalsIgnoreCase("stage_1_spawn_boss"))
            {
                world.Teredor = addSpawn(_teredor, 177228, -186305, -3800, 59352, false, 0, false, world.instanceId);

                startBossWalk(world);
            }
            else if (event.equalsIgnoreCase("stage_all_attack_again"))
            {
                world.bossIsReady = true;
            }
            else if (event.equalsIgnoreCase("stage_all_egg"))
            {
                if (npc != null && !npc.isDead() && npc.getTarget() != null)
                {
                    spawnMinions(world, npc, npc.getTarget().getActingPlayer(), _eggMinions[Rnd
                            .get(_eggMinions.length)], 1);
                }
            }
        }
        else if (event.equalsIgnoreCase("enterInstance"))
        {
            try
            {
                enterInstance(player);
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
            Log.warning(getName() + ": onAttack: " + attacker.getName());
        }

        final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpWorld instanceof TeredorWorld)
        {
            final TeredorWorld world = (TeredorWorld) tmpWorld;
            if (npc.getNpcId() == _teredor)
            {
                if (world.bossIsInPause && world.bossIsReady)
                {
                    world.bossIsInPause = false;

                    stopBossWalk(world);

                    spawnMinions(world, npc, attacker, _eliteMillipede, 3);
                }
                else if (!world.bossIsInPause && world.bossIsReady && (world.status == 0 && npc.getCurrentHp() < npc
                        .getMaxHp() * 0.85 || world.status == 1 && npc.getCurrentHp() < npc.getMaxHp() * 0.50))
                {
                    world.status++;

                    world.bossIsInPause = true;

                    world.bossIsReady = false;

                    startBossWalk(world);

                    startQuestTimer("stage_all_attack_again", 60000, npc, null);
                }
            }
        }
        return super.onAttack(npc, attacker, damage, isPet);
    }

    @Override
    public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onAggroRangeEnter: " + player.getName());
        }

        InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpworld instanceof TeredorWorld)
        {
            if ((npc.getNpcId() == _egg1 || npc.getNpcId() == _egg2) && npc.getDisplayEffect() == 0)
            {
                if (npc.getNpcId() == _egg1)
                {
                    npc.setDisplayEffect(3);
                }
                else if (npc.getNpcId() == _egg2)
                {
                    npc.setDisplayEffect(2);
                }

                npc.setTarget(player);

                //Custom but funny
                if (Rnd.get(100) > 30)
                {
                    npc.doCast(_teredorFluInfection);
                }

                startQuestTimer("stage_all_egg", 5000, npc, null); // 5sec?
            }
            else if (npc.getNpcId() == _teredorTransparent1)
            {
                npc.setTarget(player);
                npc.doCast(_teredorFluInfection);
            }
        }
        return super.onAggroRangeEnter(npc, player, isPet);
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onKill: " + npc.getName());
        }

        InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpworld instanceof TeredorWorld)
        {
            TeredorWorld world = (TeredorWorld) tmpworld;
            if (npc.getNpcId() == _teredor)
            {
                addSpawn(_adventureGuildsman, _adventureSpawn[0], _adventureSpawn[1], _adventureSpawn[2],
                        _adventureSpawn[3], false, 0, false, world.instanceId);

                //Check if any char is moving if yes spawn random millipede's
                for (int objId : world.allowed)
                {
                    L2PcInstance target = L2World.getInstance().getPlayer(objId);

                    if (target != null && target.isOnline() && target.getInstanceId() == world.instanceId && target
                            .isMoving())
                    {
                        spawnMinions(world, npc, target, _eliteMillipede, Rnd.get(6));
                        break;
                    }
                }
                InstanceManager.getInstance().setInstanceReuse(world.instanceId, _templateID, 5);
                InstanceManager.getInstance().finishInstance(world.instanceId, true);
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

        if (npc.getNpcId() == _filaur)
        {
            return "Filaur.html";
        }
        else if (npc.getNpcId() == _adventureGuildsman)
        {
            player.setInstanceId(0);
            player.teleToLocation(85636, -142530, -1336, true);
        }
        return super.onTalk(npc, player);
    }

    private void stopBossWalk(TeredorWorld world)
    {
        world.Teredor.stopMove(null);
        world.teredorWalkAI.cancelTask();
        world.Teredor.setIsInvul(false);
        world.teredorAttackAI = new L2AttackableAI(world.Teredor.new AIAccessor());
        world.Teredor.setAI(world.teredorAttackAI);
    }

    private void startBossWalk(TeredorWorld world)
    {
        if (world.Teredor.isCastingNow())
        {
            world.Teredor.abortCast();
        }

        world.Teredor.setIsInvul(true);
        world.Teredor.setIsRunning(true);
        world.teredorWalkAI = new L2NpcWalkerAI(world.Teredor.new AIAccessor());
        world.Teredor.setAI(world.teredorWalkAI);
        world.teredorWalkAI.initializeRoute(_route, null);
        world.teredorWalkAI.walkToLocation();
    }

    private void spawnMinions(TeredorWorld world, L2Npc npc, L2Character target, int npcId, int count)
    {
        if (!Util.checkIfInRange(700, npc, target, true))
        {
            return;
        }

        for (int id = 0; id < count; id++)
        {
            L2Attackable minion = (L2Attackable) addSpawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc
                    .getHeading(), false, 0, false, world.instanceId);
            minion.setIsRunning(true);

            if (target != null)
            {
                minion.addDamageHate(target, 0, 500);
                minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
            }
        }
    }

    private final synchronized void enterInstance(L2PcInstance player)
    {
        InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
        if (world != null)
        {
            if (!(world instanceof TeredorWorld))
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
                    player.setInstanceId(world.instanceId);
                    player.teleToLocation(186933, -173534, -3878, true);
                }
            }
            return;
        }
        else
        {
            if (!_debug && !InstanceManager.getInstance()
                    .checkInstanceConditions(player, _templateID, Config.TEREDOR_MIN_PLAYERS, 7, 81, 95))
            {
                return;
            }

            final int instanceId = InstanceManager.getInstance().createDynamicInstance(_qn + ".xml");
            world = new TeredorWorld();
            world.instanceId = instanceId;
            world.templateId = _templateID;
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
                enterPlayer.teleToLocation(_playerEnter[Rnd.get(0, _playerEnter.length - 1)], true);
            }

            startQuestTimer("stage_1_spawn_boss", 5000, null, player);

            Log.fine(getName() + ": [" + _templateID + "] instance started: " + instanceId + " created by player: " +
                    player
                            .getName());
            return;
        }
    }

    public static void main(String[] args)
    {
        new Teredor(-1, _qn, "instances/RaidBosses");
    }
}
