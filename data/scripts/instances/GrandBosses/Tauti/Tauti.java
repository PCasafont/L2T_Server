package instances.GrandBosses.Tauti;

import java.util.ArrayList;
import java.util.List;

import ai.group_template.L2AttackableAIScript;
import l2server.Config;
import l2server.gameserver.datatables.ScenePlayerDataTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2DoorInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.NpcSay;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

/**
 * @author LasTravel
 *         <p>
 *         Tauti Boss - Normal/Extreme Mode
 *         <p>
 *         Source:
 *         - http://www.lineage2.com/en/game/patch-notes/tauti/tauti-seed-of-hellfire/tauti.php
 *         - http://www.youtube.com/watch?v=pwuveKuGCdQ&feature=plcp
 *         - http://www.youtube.com/watch?v=Kah5D4UH2i8&feature=plcp
 *         - http://www.youtube.com/watch?v=VPRbtUhJPBc&feature=plcp
 *         - http://www.youtube.com/watch?v=9W-Jo25tKec
 *         - http://www.youtube.com/watch?v=WhySCtr9h5E&feature=related (Fail)
 *         - http://www.youtube.com/watch?v=spzaHg5gu2s&feature=plcp
 */

public class Tauti extends L2AttackableAIScript
{
    //Quest
    private static final boolean debug = false;
    private static final String qn = "Tauti";

    //Id's
    private static final int fakeLeapTauti = 29239;
    private static final int zahakId = 19287;
    private static final int[] allMobs = {29234, 29237, 29237, 29233, 29236, 19266};
    private static final int[] enterNpcs = {33671, 33669, 80001}; //Aku, Sizrak
    private static final int[] templates = {218, 219};

    //Skills
    private static final L2Skill leapAtkUp = SkillTable.getInstance().getInfo(16036, 1);
    private static final L2Skill leapAtkDown = SkillTable.getInstance().getInfo(16037, 1);

    //Cords
    private static final Location[] enterCords = {
            new Location(-149190, 210051, -10202),
            new Location(-149187, 209811, -10202),
            new Location(-148795, 209815, -10202),
            new Location(-148789, 210081, -10202),
            new Location(-148995, 209877, -10202)
    };

    //Spawns
    private static final int[][] zahakExtremeCords = {
            {-147580, 213653, -10056, 53698},
            {-148034, 213214, -10056, 61229},
            {-148038, 212574, -10056, 4019},
            {-147589, 212110, -10056, 12426},
            {-146941, 212114, -10056, 20521},
            {-146488, 212574, -10056, 28677},
            {-146490, 213216, -10056, 36842},
            {-146944, 213668, -10056, 44997}
    };

    private static final int[][] zahakEasyeCords = {
            {-147580, 213653, -10056, 53698},
            {-148038, 212574, -10056, 4019},
            {-146941, 212114, -10056, 20521},
            {-146490, 213216, -10056, 36842}
    };

    private class TautiWorld extends InstanceWorld
    {
        private L2Npc Tauti;
        private int TautiId;
        private int TautiAxeId;
        private boolean isHardMode;
        private List<L2Npc> fakeTautis;

        public TautiWorld()
        {
            this.fakeTautis = new ArrayList<L2Npc>();
            isHardMode = false;
        }
    }

    public Tauti(int questId, String name, String descr)
    {
        super(questId, name, descr);

        for (int id : this.allMobs)
        {
            addAttackId(id);
            addKillId(id);
            addSpellFinishedId(id);
        }

        addSpellFinishedId(this.fakeLeapTauti);

        for (int id : this.enterNpcs)
        {
            addTalkId(id);
            addStartNpc(id);
        }
    }

    @Override
    public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
    {
        if (this.debug)
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

        if (wrld != null && wrld instanceof TautiWorld)
        {
            TautiWorld world = (TautiWorld) wrld;
            if (npc.getNpcId() == world.TautiId)
            {
                switch (skill.getId())
                {
                    case 15200: //Tauti Ultra Whirlwind
                        if (!world.isHardMode)
                        {
                            InstanceManager.getInstance().sendPacket(world.instanceId,
                                    new ExShowScreenMessage(1801783, 0, true,
                                            5000)); //You rat-like creatures! Taste my attack!
                        }
                        break;

                    case 15202: //Tauti Ultra Typhoon
                        if (!world.isHardMode)
                        {
                            InstanceManager.getInstance().sendPacket(world.instanceId,
                                    new ExShowScreenMessage(1801784, 0, true,
                                            5000)); //Do you think you are safe outside? Feel my strength!
                        }
                        break;
                }
            }
            else if (npc.getNpcId() == this.fakeLeapTauti)
            {
                if (skill == this.leapAtkDown)
                {
                    npc.deleteMe();
                }
            }
        }
        return super.onSpellFinished(npc, player, skill);
    }

    @Override
    public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        if (this.debug)
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

        if (wrld != null && wrld instanceof TautiWorld)
        {
            TautiWorld world = (TautiWorld) wrld;
            if (event.equalsIgnoreCase("stage_1_open_doors"))
            {
                for (L2DoorInstance door : InstanceManager.getInstance().getInstance(world.instanceId).getDoors())
                {
                    door.openMe();
                }
                startQuestTimer("stage_1_intro", this.debug ? 60000 : 5 * 60000, null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_intro"))
            {
                for (L2DoorInstance door : InstanceManager.getInstance().getInstance(world.instanceId).getDoors())
                {
                    door.closeMe();
                }

                //kick buggers
                ArrayList<Integer> allowedPlayers = new ArrayList<Integer>(world.allowed);
                for (int objId : allowedPlayers)
                {
                    L2PcInstance pl = L2World.getInstance().getPlayer(objId);
                    if (pl != null && pl.isOnline() && pl.getInstanceId() == world.instanceId)
                    {
                        if (pl.getY() < 210980)
                        {
                            world.allowed.remove((Integer) pl.getObjectId());
                            pl.logout(true);
                        }
                    }
                }

                InstanceManager.getInstance().showVidToInstance(69, world.instanceId);

                startQuestTimer("stage_1_spawn_boss", ScenePlayerDataTable.getInstance().getVideoDuration(69) + 2000,
                        null, player);
            }
            else if (event.equalsIgnoreCase("stage_1_spawn_boss"))
            {
                world.Tauti =
                        addSpawn(world.TautiId, -147265, 212902, -10056, 49314, false, 0, false, world.instanceId);
                world.Tauti.setIsRunning(true);
                world.Tauti.setIsMortal(false);

                startQuestTimer("stage_all_leap_attack_up", 60000, world.Tauti, null);
            }
            else if (event.equalsIgnoreCase("stage_all_leap_attack_up"))
            {
                if (world.status < 3)
                {
                    int nextLeap = Rnd.get(1, 1) * 60000;
                    if (world.Tauti.isCastingNow())
                    {
                        nextLeap += 5000;
                    }
                    else
                    {
                        if (world.Tauti.isInCombat())
                        {
                            nextLeap += this.leapAtkUp.getHitTime();
                            nextLeap += this.leapAtkDown.getHitTime();

                            world.Tauti.setTarget(world.Tauti);
                            world.Tauti.doCast(this.leapAtkUp);

                            if (world.isHardMode)
                            {
                                world.fakeTautis.clear();
                                for (int i = 1; i <= Rnd.get(1, 3); i++)
                                {
                                    int randObjId = world.allowed.get(Rnd.get(world.allowed.size()));
                                    L2PcInstance target = L2World.getInstance().getPlayer(randObjId);
                                    if (target == null)
                                    {
                                        continue;
                                    }

                                    L2Npc dummyTauti =
                                            addSpawn(this.fakeLeapTauti, target.getX(), target.getY(), target.getZ(), -1,
                                                    false, 0, false, world.instanceId);
                                    world.fakeTautis.add(dummyTauti);
                                }
                                startQuestTimer("stage_all_leap_attack_down_clones", 3000, world.Tauti, null);
                            }
                            startQuestTimer("stage_all_leap_attack_down", this.leapAtkUp.getHitTime(), world.Tauti, null);
                        }
                    }
                    startQuestTimer("stage_all_leap_attack_up", nextLeap, world.Tauti, null);
                }
            }
            else if (event.equalsIgnoreCase("stage_all_leap_attack_down_clones"))
            {
                for (L2Npc fake : world.fakeTautis)
                {
                    fake.setTarget(fake);
                    fake.doCast(this.leapAtkDown);
                }
            }
            else if (event.equalsIgnoreCase("stage_all_leap_attack_down"))
            {
                if (world.status < 3)
                {
                    world.Tauti.setTarget(world.Tauti);
                    world.Tauti.doCast(this.leapAtkDown);
                }
            }
            else if (event.equalsIgnoreCase("stage_2_spawn_axe"))
            {
                world.Tauti =
                        addSpawn(world.TautiAxeId, -147265, 212902, -10056, 49314, false, 0, false, world.instanceId);
                world.Tauti.setCurrentHp(world.Tauti.getMaxHp() / 3);
                world.Tauti.setIsRunning(true);
            }
            else if (event.equalsIgnoreCase("stage_last_finish"))
            {
                Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
                if (inst != null)
                {
                    inst.setDuration(300000);
                }
            }
            else if (event.equalsIgnoreCase("stage_all_unspawn_zahaks"))
            {
                for (L2Npc minion : InstanceManager.getInstance().getInstance(world.instanceId).getNpcs())
                {
                    if (minion == null)
                    {
                        continue;
                    }

                    if (minion.getNpcId() == this.zahakId)
                    {
                        minion.doDie(null);
                    }
                }
            }
        }

        if (npc != null && Util.contains(this.enterNpcs, npc.getNpcId()) && Util.isDigit(event) &&
                Util.contains(this.templates, Integer.valueOf(event)))
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
        if (this.debug)
        {
            Log.warning(getName() + ": onAttack: " + npc.getName());
        }

        final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpWorld instanceof TautiWorld)
        {
            final TautiWorld world = (TautiWorld) tmpWorld;
            if (npc.getNpcId() == world.TautiId)
            {
                if (world.status == 0 && npc.getCurrentHp() < npc.getMaxHp() * 0.70 ||
                        world.status == 1 && npc.getCurrentHp() < npc.getMaxHp() * 0.20)
                {
                    world.status++;

                    for (int[] loc : world.isHardMode ? this.zahakExtremeCords : this.zahakEasyeCords)
                    {
                        L2Npc minion =
                                addSpawn(this.zahakId, loc[0], loc[1], loc[2], loc[3], false, 0, false, world.instanceId);
                        minion.setIsInvul(true);
                        minion.setIsImmobilized(true);
                        minion.broadcastPacket(new NpcSay(minion.getObjectId(), 0, minion.getNpcId(),
                                1801650)); //Lord Tauti, receive my Petra and be strengthened. Then, defeat these feeble wretches
                    }

                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new ExShowScreenMessage(1801649, 0, true, 5000)); //Jahak is infusing its Petra to Tauti.

                    startQuestTimer("stage_all_unspawn_zahaks", 20000, npc, null);
                }
                else if (world.status == 2 && npc.getCurrentHp() < npc.getMaxHp() * 0.10) //10%
                {
                    world.status = 3;

                    InstanceManager.getInstance().despawnAll(world.instanceId);
                    InstanceManager.getInstance().showVidToInstance(71, world.instanceId);

                    startQuestTimer("stage_2_spawn_axe", ScenePlayerDataTable.getInstance().getVideoDuration(71) + 2000,
                            npc, null);
                }
            }
        }
        return super.onAttack(npc, attacker, damage, isPet);
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (this.debug)
        {
            Log.warning(getName() + ": onKill: " + npc.getName());
        }

        InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpworld instanceof TautiWorld)
        {
            TautiWorld world = (TautiWorld) tmpworld;
            if (npc.getNpcId() == world.TautiAxeId)
            {
                world.Tauti.deleteMe();

                InstanceManager.getInstance().showVidToInstance(72, world.instanceId);
                InstanceManager.getInstance().setInstanceReuse(world.instanceId, world.templateId,
                        world.templateId == this.templates[0] ? false : true);
                InstanceManager.getInstance().finishInstance(world.instanceId, true);
            }
        }
        return super.onKill(npc, player, isPet);
    }

    @Override
    public final String onTalk(L2Npc npc, L2PcInstance player)
    {
        if (this.debug)
        {
            Log.warning(getName() + ": onTalk: " + player.getName());
        }

        int npcId = npc.getNpcId();
        if (npcId == this.enterNpcs[1] || npcId == this.enterNpcs[2])
        {
            return "Easy.html";
        }
        else if (npcId == this.enterNpcs[0])
        {
            return "Hard.html";
        }

        return super.onTalk(npc, player);
    }

    private void setupIDs(TautiWorld world, int template_id)
    {
        if (template_id == 219) //extreme mode
        {
            world.TautiId = 29234;
            world.TautiAxeId = 29237;
            world.isHardMode = true;
        }
        else
        //218
        {
            world.TautiId = 29233;
            world.TautiAxeId = 29236;
        }
    }

    private final synchronized void enterInstance(L2PcInstance player, int template_id)
    {
        InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
        if (world != null)
        {
            if (!(world instanceof TautiWorld))
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
                    player.teleToLocation(-147250, 211617, -10043);
                }
            }
            return;
        }
        else
        {
            int minPlayers = template_id == 218 ? Config.TAUTI_MIN_PLAYERS / 2 : Config.TAUTI_MIN_PLAYERS;
            if (!this.debug && !InstanceManager.getInstance()
                    .checkInstanceConditions(player, template_id, minPlayers, 35, 92, Config.MAX_LEVEL))
            {
                return;
            }

            final int instanceId = InstanceManager.getInstance().createDynamicInstance(this.qn + ".xml");
            world = new TautiWorld();
            world.instanceId = instanceId;
            world.templateId = template_id;
            world.status = 0;

            InstanceManager.getInstance().addWorld(world);

            setupIDs((TautiWorld) world, template_id);

            List<L2PcInstance> allPlayers = new ArrayList<L2PcInstance>();
            if (this.debug)
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
                enterPlayer.teleToLocation(this.enterCords[Rnd.get(0, this.enterCords.length - 1)], true);
            }

            startQuestTimer("stage_1_open_doors", 5000, null, player);

            Log.fine(getName() + ": [" + template_id + "] instance started: " + instanceId + " created by player: " +
                    player.getName());
            return;
        }
    }

    public static void main(String[] args)
    {
        new Tauti(-1, qn, "instances/GrandBosses");
    }
}
