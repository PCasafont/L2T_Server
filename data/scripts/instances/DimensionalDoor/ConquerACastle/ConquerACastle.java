package instances.DimensionalDoor.ConquerACastle;

import ai.group_template.L2AttackableAIScript;
import instances.DimensionalDoor.DimensionalDoor;
import l2server.Config;
import l2server.gameserver.GeoData;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2DoorInstance;
import l2server.gameserver.model.actor.instance.L2GrandBossInstance;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.ExCastleTendency;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.VisualEffect;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LasTravel
 */

public class ConquerACastle extends L2AttackableAIScript
{
    private static final String _qn = "ConquerACastle";

    //Config
    private static final boolean _debug = false;

    //Ids
    private static final int _instanceTemplateId = 501;
    private static final int _dummyFlagId = 80201;
    private static final int _helperGolem = 80213;
    private static final int _helperDwarf = 80214;
    private static final int _crystalOfProtection = 80202;
    private static final int _shilensProtector = 80210;
    private static final int[] _raidMinions = {80208, 80209, 80211, 80212};
    private static final L2Skill _wildCannon = SkillTable.getInstance().getInfo(4230, 1);
    private static final L2Skill _dummyCast = SkillTable.getInstance().getInfo(299, 1);
    private static final Location[] _enterCords = {
            new Location(-14968, 117129, -3217, 14944), //Gludio
            new Location(19343, 152802, -3269, 6867), //Dion
            new Location(107211, 145573, -3381, 47854), //Giran
            new Location(75423, 40413, -3201, 13435), //Oren
            new Location(147486, 19492, -1980, 46384), //Aden
            new Location(117402, 241715, -1354, 59468), //Innadril
            new Location(154456, -51715, -3001, 38507), //Goddard
            new Location(27413, -49123, -1324, 64462), //Rune
            new Location(75955, -144889, -1301, 26139) //Schuttgart
    };
    private static final int[][] _finalBossSpawn = {
            {-18111, 109141, -2498, 16383}, //Gludio
            {22072, 160599, -2692, 48811}, //Dion
            {116770, 145097, -2565, 32767}, //Giran
            {82861, 37192, -2292, 32371}, //Oren
            {147456, 4024, -310, 16193}, //Aden
            {116023, 249351, -788, 49056}, //Innadril
            {147466, -48786, -2279, 16735}, //Goddard
            {10918, -49151, -538, 39}, //Rune
            {77550, -152874, -547, 16325} //Schuttgart
    };

    public ConquerACastle(int questId, String name, String descr)
    {
        super(questId, name, descr);

        addTalkId(DimensionalDoor.getNpcManagerId());
        addStartNpc(DimensionalDoor.getNpcManagerId());
        addSpawnId(_crystalOfProtection);
        addKillId(_shilensProtector);
        addKillId(_crystalOfProtection);
        addTalkId(_helperDwarf);
        addSpellFinishedId(_helperDwarf);
        addStartNpc(_helperDwarf);
        addFirstTalkId(_helperDwarf);
        addSpellFinishedId(_helperGolem);
        addSpawnId(_dummyFlagId); //Flag

        for (int i : _raidMinions)
        {
            addSpawnId(i);
        }
    }

    private class ConquerACastleWorld extends InstanceWorld
    {
        private int castleId;
        private L2Npc finalBoss;
        private L2Npc dwarf;
        private L2Npc golem;
        private boolean isGolemAttacking;
        private ArrayList<L2PcInstance> rewardedPlayers;

        private ConquerACastleWorld()
        {
            isGolemAttacking = false;
            rewardedPlayers = new ArrayList<>();
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
            Log.warning(getName() + ": onSpellFinished: Unable to get world.");
            return null;
        }

        if (wrld != null && wrld instanceof ConquerACastleWorld)
        {
            ConquerACastleWorld world = (ConquerACastleWorld) wrld;
            if (npc.getNpcId() == _helperDwarf)
            {
                if (skill.getId() == 299)
                {
                    world.golem =
                            addSpawn(_helperGolem, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0, false,
                                    npc.getInstanceId());

                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new CreatureSay(npc.getObjectId(), Say2.SHOUT, "Dwarf Soldier",
                                    "C'mon golem! Destroy these doors!"));

                    attackDoor(world);
                }
            }
            else if (npc.getNpcId() == _helperGolem)
            {
                if (skill == _wildCannon)
                {
                    attackDoor(world);
                }
            }
        }

        return super.onSpellFinished(npc, player, skill);
    }

    private static void attackDoor(ConquerACastleWorld world)
    {
        L2DoorInstance door = getClosestDoor(world);
        if (door == null)
        {
            world.isGolemAttacking = false;
            world.golem.deleteMe();

            InstanceManager.getInstance().sendPacket(world.instanceId,
                    new CreatureSay(world.dwarf.getObjectId(), Say2.SHOUT, "Dwarf Soldier",
                            "My golem can't see more doors, please guide me to the next position!"));

            return;
        }

        world.golem.setTarget(door);
        world.golem.doCast(_wildCannon);
    }

    @Override
    public final String onSpawn(L2Npc npc)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onSpawn: " + npc.getName());
        }

        final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpWorld instanceof ConquerACastleWorld)
        {
            ConquerACastleWorld world = (ConquerACastleWorld) tmpWorld;
            if (Util.contains(_raidMinions, npc.getNpcId()))
            {
                if (world.status == 0)
                {
                    npc.setIsParalyzed(true);
                    npc.setIsInvul(true);
                    npc.startVisualEffect(VisualEffect.HOLD_2);
                }
            }
        }

        if (npc.getNpcId() == _crystalOfProtection)
        {
            npc.disableCoreAI(true);
        }
        else if (npc.getNpcId() == _dummyFlagId)
        {
            addSpawn(_helperDwarf, npc.getX(), npc.getY(), npc.getZ() + 50, npc.getHeading(), true, 0, false,
                    npc.getInstanceId());
        }

        return super.onSpawn(npc);
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onKill: " + npc.getName());
        }

        InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpworld instanceof ConquerACastleWorld)
        {
            ConquerACastleWorld world = (ConquerACastleWorld) tmpworld;
            if (npc.getNpcId() == _crystalOfProtection)
            {
                world.status++;

                if (_debug)
                {
                    Log.warning(getName() + ": world status: " + world.status);
                }

                if (world.status == 6 && world.castleId != 9 && world.castleId != 7 ||
                        world.status == 7 && (world.castleId == 9 || world.castleId == 7))
                {
                    world.finalBoss.stopVisualEffect(VisualEffect.HOLD_2);
                    world.finalBoss.setIsInvul(false);
                    world.finalBoss.setIsParalyzed(false);

                    for (L2MonsterInstance minion : ((L2GrandBossInstance) world.finalBoss).getMinionList()
                            .getSpawnedMinions())
                    {
                        if (minion == null)
                        {
                            continue;
                        }

                        minion.stopVisualEffect(VisualEffect.HOLD_2);
                        minion.setIsInvul(false);
                        minion.setIsParalyzed(false);
                    }

                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new CreatureSay(world.finalBoss.getObjectId(), 1, "Shilen's Protector",
                                    "It's the time to die fucking warriors!"));
                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new ExShowScreenMessage("All the seals has been destroyed, the boss is now available!",
                                    6000));
                }
                else
                {
                    String value = "";
                    switch (world.status)
                    {
                        case 1:
                            value = "first";
                            break;
                        case 2:
                            value = "second";
                            break;
                        case 3:
                            value = "third";
                            break;
                        case 4:
                            value = "fourth";
                            break;
                        case 5:
                            value = "fifth";
                            break;
                        case 6:
                            value = "sixth";
                            break;
                        case 7:
                            value = "seventh";
                            break;
                    }

                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new ExShowScreenMessage("The " + value + " seal has been destroyed!", 6000));
                }
            }
            else if (npc.getNpcId() == _shilensProtector)
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
                                    Rnd.get(8 * DimensionalDoor.getDimensionalDoorRewardRate(),
                                            15 * DimensionalDoor.getDimensionalDoorRewardRate()), player, true);
                        }
                        else
                        {
                            pMember.sendMessage("Nice attempt, but you already got a reward!");
                        }
                    }
                }
                InstanceManager.getInstance().setInstanceReuse(world.instanceId, _instanceTemplateId, 6, 30);
                InstanceManager.getInstance().finishInstance(world.instanceId, true);

                //Restore the server tendency for this players
                for (Castle castle : CastleManager.getInstance().getCastles())
                {
                    InstanceManager.getInstance()
                            .sendPacket(world.instanceId, new ExCastleTendency(world.castleId, castle.getTendency()));
                }
            }
            else if (npc.getNpcId() == _helperGolem)
            {
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new CreatureSay(npc.getObjectId(), 1, "Dwarf Soldier", "Please, protect my golem!"));

                world.isGolemAttacking = false;
            }
        }

        return super.onKill(npc, player, isPet);
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

        if (wrld != null && wrld instanceof ConquerACastleWorld)
        {
            ConquerACastleWorld world = (ConquerACastleWorld) wrld;

            if (event.equalsIgnoreCase("stage_1_start"))
            {
                //Spawn the boos
                world.finalBoss = addSpawn(_shilensProtector, _finalBossSpawn[world.castleId - 1][0],
                        _finalBossSpawn[world.castleId - 1][1], _finalBossSpawn[world.castleId - 1][2],
                        _finalBossSpawn[world.castleId - 1][3], false, 0, false, world.instanceId);
                world.finalBoss.setIsParalyzed(true);
                world.finalBoss.setIsInvul(true);
                world.finalBoss.startVisualEffect(VisualEffect.HOLD_2);

                InstanceManager.getInstance().sendDelayedPacketToInstance(world.instanceId, 15, new ExShowScreenMessage(
                        "Welcome to the castle of " +
                                CastleManager.getInstance().getCastleById(world.castleId).getName() + "!", 6000));
                InstanceManager.getInstance().sendDelayedPacketToInstance(world.instanceId, 31,
                        new ExShowScreenMessage("Infiltrate to the Castle and destroy the Crystals as fast you can!",
                                6000));
                InstanceManager.getInstance().sendPacket(world.instanceId, new ExCastleTendency(world.castleId, 0));
            }
            else if (event.startsWith("stage_all_dwarf_help"))
            {
                if (player.getParty() == null || player.getParty().getLeader() != player)
                {
                    player.sendMessage("Only the party leader can use this option!");
                    return "";
                }

                if (world.golem != null && world.golem.isDead() ||
                        world.isGolemAttacking && world.golem == null && !world.dwarf.isCastingNow())
                {
                    world.isGolemAttacking = false;
                }

                if (world.isGolemAttacking)
                {
                    player.sendMessage("Can't use this option now!");
                    return "";
                }

                if (world.dwarf == null)
                {
                    world.dwarf = npc;
                    world.dwarf.setIsRunning(true);
                }

                if (event.endsWith("follow"))
                {
                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new CreatureSay(npc.getObjectId(), 1, "Dwarf Soldier",
                                    (player.getAppearance().getSex() ? "Mrs. " : "Mr. ") + player.getName() +
                                            "! I'll follow you!"));

                    world.dwarf.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player);

                    return "";
                }
                else if (event.endsWith("attack"))
                {
                    L2Object door = getClosestDoor(world);
                    if (door == null)
                    {
                        InstanceManager.getInstance().sendPacket(world.instanceId,
                                new CreatureSay(npc.getObjectId(), 1, "Dwarf Soldier",
                                        (player.getAppearance().getSex() ? "Mrs. " : "Mr. ") + player.getName() +
                                                "! I don't see any door in my range, please guide me to the correct position!"));
                        return "";
                    }

                    world.dwarf.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
                    world.isGolemAttacking = true;

                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new CreatureSay(npc.getObjectId(), 1, "Dwarf Soldier",
                                    (player.getAppearance().getSex() ? "Mrs. " : "Mr. ") + player.getName() +
                                            "! I'll make my best golem, give me few minutes!"));

                    npc.setIsCastingNow(true);
                    npc.doCast(_dummyCast);
                }
            }
        }
        else if (event.equalsIgnoreCase("enterToInstance"))
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
        else if (event.equalsIgnoreCase("dwarf_main"))
        {
            return "dwarfHelp.html";
        }

        return null;
    }

    private static L2DoorInstance getClosestDoor(ConquerACastleWorld world)
    {
        L2DoorInstance door = null;

        double distSq = 1000000000;
        for (L2DoorInstance instanceDoor : InstanceManager.getInstance().getInstance(world.instanceId).getDoors())
        {
            if (instanceDoor == null || instanceDoor.isDead() || instanceDoor.isWall() ||
                    instanceDoor.getCastle().getCastleId() != world.castleId)
            {
                continue;
            }

            if (!world.golem.isInsideRadius(instanceDoor, _wildCannon.getCastRange() - 100, true, true) ||
                    !GeoData.getInstance().canSeeTarget(world.golem, instanceDoor))
            {
                continue;
            }

            //Check if is damaged
            if (instanceDoor.getCurrentHp() != instanceDoor.getMaxHp())
            {
                door = instanceDoor;
                break;
            }

            double tempDistSq = instanceDoor.getDistanceSq(world.golem);
            if (tempDistSq < distSq)
            {
                distSq = tempDistSq;
                door = instanceDoor;
            }
        }

        return door;
    }

    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onFirstTalk: " + player);
        }

        if (npc.getNpcId() == _helperDwarf)
        {
            return "dwarfHelp.html";
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
        if (npcId == DimensionalDoor.getNpcManagerId())
        {
            return _qn + ".html";
        }

        return "";
    }

    private final synchronized void enterInstance(L2PcInstance player)
    {
        InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
        if (world != null)
        {
            if (!(world instanceof ConquerACastleWorld))
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
                    ConquerACastleWorld wrld = (ConquerACastleWorld) world;
                    player.setInstanceId(world.instanceId);
                    player.teleToLocation(_enterCords[wrld.castleId - 1], true);
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

            int castleId = Rnd.get(1, 9);

            final int instanceId = InstanceManager.getInstance().createDynamicInstance(
                    _qn + "/" + CastleManager.getInstance().getCastleById(castleId).getName() + ".xml");

            world = new ConquerACastleWorld();
            world.instanceId = instanceId;
            world.status = 0;

            InstanceManager.getInstance().addWorld(world);

            ConquerACastleWorld wrld = (ConquerACastleWorld) world;

            wrld.castleId = castleId;

            List<L2PcInstance> allPlayers = new ArrayList<>();
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
                enterPlayer.teleToLocation(_enterCords[castleId - 1], true);
            }

            startQuestTimer("stage_1_start", 1, null, player);

            Log.fine(getName() + ":  instance started: " + instanceId + " created by player: " + player.getName());

            return;
        }
    }

    public static void main(String[] args)
    {
        new ConquerACastle(-1, _qn, "instances/DimensionalDoor");
    }
}
