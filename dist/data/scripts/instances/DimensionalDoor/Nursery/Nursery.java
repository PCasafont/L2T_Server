package instances.DimensionalDoor.Nursery;

import ai.group_template.L2AttackableAIScript;
import instances.DimensionalDoor.DimensionalDoor;
import l2server.Config;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2NpcBufferInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExSendUIEvent;
import l2server.gameserver.network.serverpackets.ExSendUIEventRemove;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;
import l2server.util.Rnd;

/**
 * @author LasTravel
 *         <p>
 *         Source:
 *         - https://www.youtube.com/watch?v=gV0anX2I9LU
 *         - http://www.lineage-realm.com/community/lineage-2-talk/quest-walkthroughs/Nursery-Instance-Guide-Seed-of-Annihilation
 */

public class Nursery extends L2AttackableAIScript
{
    private static final String _qn = "Nursery";

    //Config
    private static final boolean _debug = false;
    private static final int _reuseMinutes = 1440;

    //Ids
    private static final int _instanceTemplateId = 171;
    //private static final int _tissueEnergyCrystal		= 17602;	//Retail reward
    private static final int _tieId = 33152;
    private static final int _maguenId = 19037;
    private static final int[] _energyRegenerationIds = {14228, 14229, 14230};
    private static final int[] _failedCreations = {80329, 80330, 80331, 80332};

    public Nursery(int questId, String name, String descr)
    {
        super(questId, name, descr);

        addTalkId(DimensionalDoor.getNpcManagerId());
        addStartNpc(DimensionalDoor.getNpcManagerId());

        addTalkId(_tieId);
        addStartNpc(_tieId);
        addFirstTalkId(_tieId);

        for (int i : _failedCreations)
        {
            addKillId(i);
        }

        addKillId(_maguenId);
    }

    private class NurseryWorld extends InstanceWorld
    {
        private L2PcInstance instancePlayer;
        private Long enterTime;
        private int energyBuffId;
        private int points;
        private int leakedPoints;
        private boolean isMaguenSpawned;

        private NurseryWorld()
        {
        }
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
        else
        {
            wrld = InstanceManager.getInstance().getPlayerWorld(player);
        }

        if (wrld != null && wrld instanceof NurseryWorld)
        {
            NurseryWorld world = (NurseryWorld) wrld;
            if (npc.getNpcId() == _tieId)
            {
                if (world.status == 0)
                {
                    return "Tie.html";
                }
                else if (world.status == 1)
                {
                    L2Abnormal buff = world.instancePlayer.getFirstEffect(world.energyBuffId);
                    if (buff != null)
                    {
                        buff.exit();

                        if (buff.getSkill() != null)
                        {
                            int skillId = buff.getSkill().getId();
                            int pointsToGive;

                            if (skillId == _energyRegenerationIds[0])
                            {
                                pointsToGive = 40;
                            }
                            else if (skillId == _energyRegenerationIds[1])
                            {
                                pointsToGive = 60;
                            }
                            else
                            {
                                pointsToGive = 80;
                            }

                            world.points += pointsToGive;
                            world.instancePlayer.sendPacket(new ExShowScreenMessage(1, -1, 2, 0, 0, 0, 0, true, 2000, 0,
                                    "Soldier Tie received " + pointsToGive +
                                            " pieces of bio-energy residue.")); //1811146
                        }
                    }
                }
                else if (world.status == 2)
                {
                    return "TieEnd.html";
                }
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

        if (wrld != null && wrld instanceof NurseryWorld)
        {
            NurseryWorld world = (NurseryWorld) wrld;
            if (event.equalsIgnoreCase("stage_1_start"))
            {
                world.status = 1;

                world.instancePlayer = player;

                world.enterTime = System.currentTimeMillis();

                startQuestTimer("stage_all_screen_message", 1000, null, world.instancePlayer); //1sec
                startQuestTimer("stage_last_end", 1800000, null, player); //30min

                for (L2Spawn iSpawn : SpawnTable.getInstance().getSpecificSpawns("nursery"))
                {
                    if (iSpawn == null)
                    {
                        continue;
                    }

                    L2Npc iNpc = addSpawn(iSpawn.getNpcId(), iSpawn.getX(), iSpawn.getY(), iSpawn.getZ(),
                            iSpawn.getHeading(), false, 0, true, world.instanceId);

                    L2Spawn spawn = iNpc.getSpawn();
                    spawn.setRespawnDelay(20);
                    spawn.startRespawn();
                }
            }
            else if (event.equalsIgnoreCase("stage_all_screen_message"))
            {
                if (world.instancePlayer != null && world.instancePlayer.getInstanceId() == world.instanceId)
                {
                    world.instancePlayer.sendPacket(
                            new ExSendUIEvent(3, (int) (System.currentTimeMillis() - world.enterTime) / 1000,
                                    world.points * 60, 1, 2409));
                    startQuestTimer("stage_all_screen_message", 1000, null, world.instancePlayer); //1sec
                }
            }
            else if (event.equalsIgnoreCase("stage_all_maguen_check"))
            {
                if (world.isMaguenSpawned)
                {
                    world.isMaguenSpawned = false;
                }
            }
            else if (event.equalsIgnoreCase("stage_last_end"))
            {
                world.status = 2;

                world.instancePlayer.sendPacket(new ExSendUIEventRemove());
                world.instancePlayer.sendPacket(new ExShowScreenMessage("Now talk with Tie!", 3000));

                for (L2Npc iNpc : InstanceManager.getInstance().getInstance(world.instanceId).getNpcs())
                {
                    if (iNpc == null || iNpc.getNpcId() == _tieId)
                    {
                        continue;
                    }

                    L2Spawn sp = iNpc.getSpawn();
                    if (sp != null)
                    {
                        sp.stopRespawn();
                    }
                    iNpc.deleteMe();
                }
            }
            else if (event.equalsIgnoreCase("exchange_and_leave"))
            {
                if (world.status == 2)
                {
                    world.status = 3;

                    int shinyCoins = DimensionalDoor.getDimensionalDoorRewardRate();
                    if (world.points >= 1 && world.points <= 800)
                    {
                        shinyCoins += Rnd.get(2 * DimensionalDoor.getDimensionalDoorRewardRate(),
                                1 * DimensionalDoor.getDimensionalDoorRewardRate());
                    }
                    else if (world.points >= 801 && world.points <= 1600)
                    {
                        shinyCoins += Rnd.get(4 * DimensionalDoor.getDimensionalDoorRewardRate(),
                                2 * DimensionalDoor.getDimensionalDoorRewardRate());
                    }
                    else if (world.points >= 1601 && world.points <= 2000)
                    {
                        shinyCoins += Rnd.get(5 * DimensionalDoor.getDimensionalDoorRewardRate(),
                                3 * DimensionalDoor.getDimensionalDoorRewardRate());
                    }
                    else if (world.points >= 2001 && world.points <= 2400)
                    {
                        shinyCoins += Rnd.get(7 * DimensionalDoor.getDimensionalDoorRewardRate(),
                                4 * DimensionalDoor.getDimensionalDoorRewardRate());
                    }
                    else if (world.points >= 2401 && world.points <= 2800)
                    {
                        shinyCoins += Rnd.get(9 * DimensionalDoor.getDimensionalDoorRewardRate(),
                                4 * DimensionalDoor.getDimensionalDoorRewardRate());
                    }
                    else if (world.points >= 2801 && world.points <= 3200)
                    {
                        shinyCoins += Rnd.get(11 * DimensionalDoor.getDimensionalDoorRewardRate(),
                                4 * DimensionalDoor.getDimensionalDoorRewardRate());
                    }
                    else if (world.points >= 3201 && world.points <= 3600)
                    {
                        shinyCoins += Rnd.get(13 * DimensionalDoor.getDimensionalDoorRewardRate(),
                                4 * DimensionalDoor.getDimensionalDoorRewardRate());
                    }
                    else if (world.points >= 3601 && world.points <= 4000)
                    {
                        shinyCoins += Rnd.get(15 * DimensionalDoor.getDimensionalDoorRewardRate(),
                                4 * DimensionalDoor.getDimensionalDoorRewardRate());
                    }
                    else if (world.points >= 4001)
                    {
                        shinyCoins += Rnd.get(17 * DimensionalDoor.getDimensionalDoorRewardRate(),
                                4 * DimensionalDoor.getDimensionalDoorRewardRate());
                    }

                    if (world.points > 600)
                    {
                        world.instancePlayer.addItem(_qn, DimensionalDoor.getDimensionalDoorRewardId(), shinyCoins,
                                world.instancePlayer, true);
                    }

                    InstanceManager.getInstance()
                            .setInstanceReuse(world.instanceId, _instanceTemplateId, _reuseMinutes);
                    InstanceManager.getInstance().finishInstance(world.instanceId, true);
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
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onKill: " + npc.getName());
        }

        InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpworld instanceof NurseryWorld)
        {
            NurseryWorld world = (NurseryWorld) tmpworld;

            if (world.status != 1)
            {
                return super.onKill(npc, player, isPet);
            }

            if (npc.getNpcId() >= _failedCreations[0] && npc.getNpcId() <= _failedCreations[3])
            {
                world.points += Rnd.get(1, 10);

                //Maguen
                if (!world.isMaguenSpawned && Rnd.get(100) > 90)
                {
                    world.isMaguenSpawned = true;
                    world.instancePlayer
                            .sendPacket(new ExShowScreenMessage(1801149, 0, true, 2000)); //Maguen appearance!!!

                    L2MonsterInstance maguen = (L2MonsterInstance) addSpawn(_maguenId, world.instancePlayer.getX(),
                            world.instancePlayer.getY(), world.instancePlayer.getZ(), 0, true, 3000, true,
                            world.instanceId); //5seg
                    maguen.setTarget(world.instancePlayer);
                    maguen.addDamageHate(world.instancePlayer, 500, 99999);
                    maguen.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world.instancePlayer);

                    world.leakedPoints = Rnd.get(1, 99);

                    if (world.leakedPoints > world.points)
                    {
                        world.leakedPoints = Rnd.get(1, world.points);
                    }

                    world.points -= world.leakedPoints;
                    world.instancePlayer.sendPacket(new ExShowScreenMessage(1, -1, 2, 0, 0, 0, 0, true, 2000, 0,
                            "Maguen stole " + world.leakedPoints + " pieces of bio-energy residue.")); //1811145

                    startQuestTimer("stage_all_maguen_check", 3000, maguen, null); //1sec
                }
                else if (Rnd.get(120) <= 5)
                {
                    if (world.instancePlayer.getFirstEffect(world.energyBuffId) == null)
                    {
                        world.energyBuffId = _energyRegenerationIds[Rnd.get(_energyRegenerationIds.length)];

                        world.instancePlayer.sendPacket(
                                new ExShowScreenMessage(1811179, 0, true, 2000)); //Received Regeneration Energy!!

                        SkillTable.getInstance().getInfo(world.energyBuffId, 1)
                                .getEffects(world.instancePlayer, world.instancePlayer);
                    }
                }
            }
            else if (npc.getNpcId() == _maguenId)
            {
                if (world.isMaguenSpawned)
                {
                    world.isMaguenSpawned = false;
                    world.points += world.leakedPoints;
                    world.instancePlayer.sendPacket(new ExShowScreenMessage(1, -1, 2, 0, 0, 0, 0, true, 2000, 0,
                            "Maguen gets surprised and gives " + world.leakedPoints +
                                    " pieces of bio-energy residue.")); //1811147
                }
            }
        }
        return "";
    }

    private final synchronized void enterInstance(L2PcInstance player)
    {
        InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
        if (world != null)
        {
            if (!(world instanceof NurseryWorld))
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
                    player.teleToLocation(-185859, 147886, -15315, true);

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
            world = new NurseryWorld();
            world.instanceId = instanceId;
            world.status = 0;

            InstanceManager.getInstance().addWorld(world);

            world.allowed.add(player.getObjectId());

            player.stopAllEffectsExceptThoseThatLastThroughDeath();
            player.setInstanceId(instanceId);
            player.teleToLocation(-185859, 147886, -15315, true);

            L2NpcBufferInstance.giveBasicBuffs(player);

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
        new Nursery(-1, _qn, "instances/DimensionalDoor");
    }
}
