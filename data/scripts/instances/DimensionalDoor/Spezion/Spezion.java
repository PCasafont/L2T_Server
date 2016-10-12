package instances.DimensionalDoor.Spezion;

import ai.group_template.L2AttackableAIScript;
import instances.DimensionalDoor.DimensionalDoor;
import l2server.Config;
import l2server.gameserver.GeoData;
import l2server.gameserver.datatables.ScenePlayerDataTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.NicknameChanged;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.VisualEffect;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LasTravel
 *         <p>
 *         Spezion Boss - Normal/Extreme Mode
 *         <p>
 *         Source:
 *         - http://www.youtube.com/watch?v=RausqeXJ_rs&feature=player_embedded
 *         - http://www.youtube.com/watch?v=nLVx8PPcXy8&feature=player_embedded
 */

public class Spezion extends L2AttackableAIScript
{
    //Quest
    private static final String _qn = "Spezion";
    private static final boolean _debug = false;

    //Ids
    private static final int _giantCannonball = 17611;
    private static final int _fakeSpezion = 25868;
    private static final int[] _cannonIds = {32939, 32940, 32941, 32942};
    private static final int[] _allMobs = {25779, 25780, 25781, 25782, 25867, 25872, 25873, 25874};
    private static final Location _enterCords = new Location(175475, 145044, -11897);
    private static final L2Skill _cannonBlast = SkillTable.getInstance().getInfo(14175, 1);

    public Spezion(int questId, String name, String descr)
    {
        super(questId, name, descr);

        addTalkId(DimensionalDoor.getNpcManagerId());
        addStartNpc(DimensionalDoor.getNpcManagerId());

        for (int a : _cannonIds)
        {
            addTalkId(a);
            addStartNpc(a);
            addSpellFinishedId(a);
        }

        for (int a : _allMobs)
        {
            addAttackId(a);
            addKillId(a);
            addSpawnId(a);
        }
    }

    private class PrisonOfDarknessWorld extends InstanceWorld
    {
        private boolean isHardMode;
        private L2Npc spezionBoss;
        private int spezionId;
        private int[] spezionGuards;
        private List<L2Npc> minions;
        private List<L2Npc> fakeMonsters;
        private ArrayList<L2PcInstance> rewardedPlayers;

        private PrisonOfDarknessWorld()
        {
            isHardMode = false;
            spezionGuards = new int[3];
            minions = new ArrayList<>();
            fakeMonsters = new ArrayList<>();
            rewardedPlayers = new ArrayList<>();
        }
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onKill: " + npc.getName());
        }

        InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpworld instanceof PrisonOfDarknessWorld)
        {
            PrisonOfDarknessWorld world = (PrisonOfDarknessWorld) tmpworld;
            if (Util.contains(world.spezionGuards, npc.getNpcId()))
            {
                synchronized (world.minions)
                {
                    if (world.minions.contains(npc))
                    {
                        world.minions.remove(npc);
                    }
                }
            }
            else if (npc == world.spezionBoss)
            {
                int maxReward = world.isHardMode ? 20 : 10;
                if (player.isInParty())
                {
                    for (L2PcInstance pMember : player.getParty().getPartyMembers())
                    {
                        if (pMember == null || pMember.getInstanceId() != world.instanceId)
                        {
                            continue;
                        }

                        //Reward
                        if (InstanceManager.getInstance().canGetUniqueReward(pMember, world.rewardedPlayers))
                        {
                            world.rewardedPlayers.add(pMember);
                            pMember.addItem(_qn, DimensionalDoor.getDimensionalDoorRewardId(),
                                    Rnd.get(7 * DimensionalDoor.getDimensionalDoorRewardRate(),
                                            maxReward * DimensionalDoor.getDimensionalDoorRewardRate()), player, true);
                        }
                        else
                        {
                            pMember.sendMessage("Nice attempt, but you already got a reward!");
                        }
                    }
                }

                InstanceManager.getInstance().setInstanceReuse(world.instanceId, world.isHardMode ? 196 : 159, 6, 30);
                InstanceManager.getInstance().finishInstance(world.instanceId, true);
                InstanceManager.getInstance().showVidToInstance(54, world.instanceId);
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
        if (npcId == DimensionalDoor.getNpcManagerId())
        {
            return _qn + ".html";
        }
        else if (Util.contains(_cannonIds, npc.getNpcId()))
        {
            if (npc.getInstanceId() != 0)
            {
                notifyEvent("stage_all_cannon", npc, player);
            }
        }

        return "";
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

        if (wrld != null && wrld instanceof PrisonOfDarknessWorld)
        {
            PrisonOfDarknessWorld world = (PrisonOfDarknessWorld) wrld;
            if (Util.contains(_cannonIds, npc.getNpcId()))
            {
                if (skill == _cannonBlast)
                {
                    npc.setTitle("Empty Cannon");
                    npc.broadcastPacket(new NicknameChanged(npc));

                    world.spezionBoss.setDisplayEffect(2);
                    world.spezionBoss.setIsInvul(false);
                    world.spezionBoss.stopVisualEffect(VisualEffect.S_INVINCIBLE);

                    if (world.isHardMode) //Delete the fake shits
                    {
                        for (L2Npc fakeMonster : world.fakeMonsters)
                        {
                            fakeMonster.deleteMe();
                        }
                        world.fakeMonsters.clear();
                    }
                    startQuestTimer("stage_all_spezion_back", 60000, npc, null);
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

        if (wrld != null && wrld instanceof PrisonOfDarknessWorld)
        {
            PrisonOfDarknessWorld world = (PrisonOfDarknessWorld) wrld;
            if (event.equalsIgnoreCase("stage_1_start"))
            {
                InstanceManager.getInstance().showVidToInstance(53, world.instanceId);

                startQuestTimer("stage_1_spawns", ScenePlayerDataTable.getInstance().getVideoDuration(53) + 2000, null,
                        player);
            }
            else if (event.equalsIgnoreCase("stage_1_spawns"))
            {
                world.spezionBoss =
                        addSpawn(world.spezionId, 175474, 143502, -11769, 15397, true, 0, false, world.instanceId);
                world.spezionBoss.setIsInvul(true);
                world.spezionBoss.startVisualEffect(VisualEffect.S_INVINCIBLE);

                if (world.isHardMode)
                {
                    notifyEvent("stage_all_spawn_fake_spezions", world.spezionBoss, null);
                }

                InstanceManager.getInstance().sendPacket(world.instanceId, new ExShowScreenMessage(1811153, 5000));

                startQuestTimer("stage_all_spawn_minions", 10000, world.spezionBoss, null);
            }
            else if (event.equalsIgnoreCase("stage_all_spawn_fake_spezions"))
            {
                //Fake Invul Spezions
                for (int i = 0; i < 3; i++)
                {
                    L2Npc fakeSpezion = addSpawn(_fakeSpezion, world.spezionBoss.getX(), world.spezionBoss.getY(),
                            world.spezionBoss.getZ(), 0, true, 0, false, world.instanceId);
                    world.fakeMonsters.add(fakeSpezion);
                    fakeSpezion.setIsInvul(true);
                    fakeSpezion.startVisualEffect(VisualEffect.S_INVINCIBLE);
                }

                //It's Invul minions
                List<L2Npc> toIterate = new ArrayList<>(world.fakeMonsters);
                for (L2Npc fakeSpezion : toIterate)
                {
                    for (int a = 1; a < 3; a++)
                    {
                        for (int b : world.spezionGuards)
                        {
                            L2Npc newGuard = addSpawn(b, fakeSpezion.getX(), fakeSpezion.getY(), fakeSpezion.getZ(),
                                    fakeSpezion.getHeading(), true, 0, false, world.instanceId);
                            world.fakeMonsters.add(newGuard);
                            ((L2MonsterInstance) newGuard).setIsRaidMinion(true);
                            newGuard.setIsRunning(true);
                            newGuard.setIsInvul(true);
                        }
                    }
                }
            }
            else if (event.equalsIgnoreCase("stage_all_spawn_minions"))
            {
                if (world.spezionBoss != null && !world.spezionBoss.isDead())
                {
                    if (world.spezionBoss.getDisplayEffect() != 2)
                    {
                        synchronized (world.minions)
                        {
                            if (world.minions.isEmpty())
                            {
                                for (int a = 1; a < 3; a++)
                                {
                                    for (int b : world.spezionGuards)
                                    {
                                        L2Npc newGuard = addSpawn(b, world.spezionBoss.getX(), world.spezionBoss.getY(),
                                                world.spezionBoss.getZ(), world.spezionBoss.getHeading(), true, 0,
                                                false, world.instanceId);
                                        world.minions.add(newGuard);
                                        ((L2MonsterInstance) newGuard).setIsRaidMinion(true);
                                        newGuard.setIsRunning(true);
                                    }
                                }
                            }
                        }
                    }

                    startQuestTimer("stage_all_spawn_minions", 5000, world.spezionBoss, null);
                }
            }
            else if (event.equalsIgnoreCase("stage_all_spezion_back"))
            {
                if (world.spezionBoss != null && !world.spezionBoss.isDead())
                {
                    world.spezionBoss.setDisplayEffect(3);
                    world.spezionBoss.setIsInvul(true);
                    world.spezionBoss.startVisualEffect(VisualEffect.S_INVINCIBLE);

                    if (world.isHardMode)
                    {
                        notifyEvent("stage_all_spawn_fake_spezions", world.spezionBoss, null);
                    }
                }
            }
            else if (event.equalsIgnoreCase("stage_all_cannon"))
            {
                if (world.spezionBoss == null || world.spezionBoss.isDead() ||
                        world.spezionBoss.getDisplayEffect() == 2)
                {
                    return "";
                }

                long cannonBallCount = player.getInventory().getInventoryItemCount(_giantCannonball, 0);
                if (cannonBallCount == 0)
                {
                    return npc.getTemplate().TemplateId + "-1.html";
                }

                if (!Util.checkIfInRange(800, npc, world.spezionBoss, false) &&
                        Util.checkIfInRange(1300, npc, world.spezionBoss, false) &&
                        GeoData.getInstance().canSeeTarget(npc, world.spezionBoss))
                {
                    player.destroyItemByItemId(_qn, _giantCannonball, 1, npc, true);
                    player.getInventory().destroyItem(_qn, _giantCannonball, 1, player, player);

                    npc.setTitle("Loading Cannon");
                    npc.broadcastPacket(new NicknameChanged(npc));
                    npc.setTarget(world.spezionBoss);
                    npc.doCast(_cannonBlast);
                }

                if (_debug)
                {
                    Log.warning(getName() + ": Range: " +
                            Util.calculateDistance(npc.getX(), npc.getY(), world.spezionBoss.getX(),
                                    world.spezionBoss.getY()));
                }
            }
        }
        else if (event.startsWith("enterToInstance_"))
        {
            try
            {
                enterInstance(player, Integer.valueOf(event.replace("enterToInstance_", "")));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return null;
    }

    private final synchronized void enterInstance(L2PcInstance player, int template_id)
    {
        InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
        if (world != null)
        {
            if (!(world instanceof PrisonOfDarknessWorld))
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
                    player.deleteAllItemsById(_giantCannonball);
                    player.setInstanceId(world.instanceId);
                    player.teleToLocation(175373, 144292, -11818);
                }
            }

            return;
        }
        else
        {
            if (!_debug && !InstanceManager.getInstance()
                    .checkInstanceConditions(player, template_id, 7, 7, 92, Config.MAX_LEVEL))
            {
                return;
            }

            final int instanceId = InstanceManager.getInstance().createDynamicInstance(_qn + ".xml");
            world = new PrisonOfDarknessWorld();
            world.instanceId = instanceId;
            world.status = 0;

            InstanceManager.getInstance().addWorld(world);

            setupIDs((PrisonOfDarknessWorld) world, template_id);

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

                enterPlayer.deleteAllItemsById(_giantCannonball);
                enterPlayer.setInstanceId(instanceId);
                enterPlayer.teleToLocation(_enterCords, true);
            }

            startQuestTimer("stage_1_start", 60000, null, player);

            Log.fine(getName() + ":  instance started: " + instanceId + " created by player: " + player.getName());
            return;
        }
    }

    private void setupIDs(PrisonOfDarknessWorld world, int template_id)
    {
        if (template_id == 159) //Easy Mode
        {
            world.spezionId = 25779;
            for (int a = 1; a < 4; a++)
            {
                world.spezionGuards[a - 1] = world.spezionId + a;
            }
        }
        else
        //196 Extreme
        {
            world.isHardMode = true;
            world.spezionId = 25867;
            for (int a = 1; a < 4; a++)
            {
                world.spezionGuards[a - 1] = world.spezionId + 4 + a;
            }
        }
    }

    public static void main(String[] args)
    {
        new Spezion(-1, _qn, "instances/DimensionalDoor");
    }
}
