/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package instances.DimensionalDoor.FaeronSiege;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import instances.DimensionalDoor.DimensionalDoor;
import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2GuardInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.Earthquake;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.network.serverpackets.SpecialCamera;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

/**
 * @author LasTravel
 */
public class FaeronSiege extends Quest
{
    private static final boolean debug = false;
    private static final String qn = "FaeronSiege";

    //Ids
    private static final int instanceTemplateId = 502;
    private static final int protectionStoneId = 13425;
    private static final int dummyInvaderDoor = 19074;
    private static final int passiveSkillId = 90003;
    private static final int makkumBossId = 26090;
    private static final int warriorGuard = 33518;
    private static final int warriorLeonaId = 33898;
    private static final int warriorKainId = 33993;
    private static final int warriorMageSUpId = 19495;
    private static final int gravityCoreId = 13435;
    private static final int[] invadeMobs = {23477, 23478, 19555}; //Abyssal Shaman, Abyssal Berserker, Abyssal Imp
    private static final int[] fullBuffsIds = {15129, 15133, 15137};
    //Skills
    private static final L2Skill protectionSkill = SkillTable.getInstance().getInfo(14085, 1);
    private static final L2Skill weakMoment = SkillTable.getInstance().getInfo(14558, 1);
    private static final L2Skill portalEffect1 = SkillTable.getInstance().getInfo(6783, 1);
    private static final L2Skill portalEffect2 = SkillTable.getInstance().getInfo(6799, 1);
    private static final L2Skill warriorsSpawnEffect = SkillTable.getInstance().getInfo(6176, 1);
    private static final L2Skill buffPresentation = SkillTable.getInstance().getInfo(15368, 6);
    private static final L2Skill resSkill = SkillTable.getInstance().getInfo(1016, 6);
    private static final L2Skill healSkill = SkillTable.getInstance().getInfo(11570, 1);
    private static final L2Skill summonTree = SkillTable.getInstance().getInfo(14902, 1);
    private static final L2Skill ultimateDef = SkillTable.getInstance().getInfo(23451, 3);
    private static final L2Skill summonCore = SkillTable.getInstance().getInfo(6848, 1);

    public FaeronSiege(int questId, String name, String descr)
    {
        super(questId, name, descr);

        addTalkId(DimensionalDoor.getNpcManagerId());
        addStartNpc(DimensionalDoor.getNpcManagerId());

        addAttackId(makkumBossId);
        addKillId(makkumBossId);
        addSkillSeeId(makkumBossId);

        addFirstTalkId(warriorGuard);
        addFirstTalkId(warriorLeonaId);
        addFirstTalkId(warriorKainId);
        addFirstTalkId(warriorMageSUpId);
        addFirstTalkId(gravityCoreId);

        for (int i : invadeMobs)
        {
            addAttackId(i);
            addKillId(i);
        }
    }

    private class FearonSiegeWorld extends InstanceWorld
    {
        private int eventRound;
        private L2Npc warriorLeona;
        private L2Npc warriorKain;
        private L2Npc warriorMageSup;
        private L2Npc protectionStone;
        private L2Npc summonGravityCore;
        private L2Npc bossMakkum;
        private List<L2Npc> allMinions;
        private List<L2Npc> guardArmy;
        private ArrayList<L2PcInstance> rewardedPlayers;

        private FearonSiegeWorld()
        {
            allMinions = new ArrayList<L2Npc>();
            guardArmy = new ArrayList<L2Npc>();
            rewardedPlayers = new ArrayList<L2PcInstance>();
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

        if (wrld != null && wrld instanceof FearonSiegeWorld)
        {
            final FearonSiegeWorld world = (FearonSiegeWorld) wrld;
            if (event.equalsIgnoreCase("stage_1_start"))
            {
                if (debug)
                {
                    world.eventRound = 9;
                }
                world.protectionStone =
                        addSpawn(protectionStoneId, -79660, 244954, -3651 + 20, 0, false, 0, false, world.instanceId);

                startQuestTimer("stone_ai", 3000, world.protectionStone, null);

                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new ExShowScreenMessage("Sarch the Protection Stone out of the Fearon Village!", 8000));
                InstanceManager.getInstance().sendDelayedPacketToInstance(world.instanceId, 10,
                        new ExShowScreenMessage("Stay close to the Protection Stone!", 5000));

                //Spawn dummy effects
                for (int i = 0; i < 61; i++)
                {
                    int x = (int) (1200 * Math.cos(i * 0.618));
                    int y = (int) (1200 * Math.sin(i * 0.618));
                    addSpawn(dummyInvaderDoor, -79660 + x, 244954 + y, -3651 + 20, -1, false, 0, true,
                            world.instanceId);
                }

                startQuestTimer("stage_all_spawn_round", debug ? 60000 : 60000 * 3, world.protectionStone, null);
            }
            else if (event.equalsIgnoreCase("stage_all_spawn_round"))
            {
                world.eventRound++;

                InstanceManager.getInstance()
                        .sendPacket(world.instanceId, new Earthquake(153581, 142081, -12741, world.eventRound, 10));
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new ExShowScreenMessage(1, 0, 2, 0, 0, 0, 0, true, 5 * 1000, 0, "Round: " + world.eventRound));

                if (world.eventRound == 7)
                {
                    InstanceManager.getInstance().sendDelayedPacketToInstance(world.instanceId, 6,
                            new ExShowScreenMessage(1, 0, 2, 0, 0, 0, 0, true, 5 * 1000, 0,
                                    "The Protection Stone now can debuff the enemies!"));
                }

                L2Skill passiveSkill = SkillTable.getInstance()
                        .getInfo(passiveSkillId, world.eventRound < 10 ? world.eventRound * 2 : 10);
                if (world.eventRound < 10)
                {
                    for (int i = 0; i < 61; i++)
                    {
                        int x = (int) (1200 * Math.cos(i * 0.618));
                        int y = (int) (1200 * Math.sin(i * 0.618));

                        L2Npc minion =
                                addSpawn(invadeMobs[Rnd.get(invadeMobs.length)], -79660 + x, 244954 + y, -3651 + 20,
                                        -1, false, 0, true, world.instanceId);
                        minion.setIsRunning(true);
                        minion.addSkill(passiveSkill);
                        minion.setCurrentHpMp(minion.getMaxHp(), minion.getMaxMp());

                        synchronized (world.allMinions)
                        {
                            world.allMinions.add(minion);
                        }
                    }
                }
                else if (world.eventRound == 10)
                {
                    //BossTime
                    world.bossMakkum =
                            addSpawn(makkumBossId, -80015, 244904, -3677, 917, false, 0, true, world.instanceId);

                    world.bossMakkum.addSkill(passiveSkill);
                    world.bossMakkum.setCurrentHpMp(world.bossMakkum.getMaxHp(), world.bossMakkum.getMaxMp());

                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new CreatureSay(world.bossMakkum.getObjectId(), 1, world.bossMakkum.getName(),
                                    "It's the time to end with your lives!"));
                }
            }
            else if (event.equalsIgnoreCase("stone_ai"))
            {
                /*
                  Protection Stone AI
                  This NPC will cast non-stop one buff to all the players that are inside his activity radius.
                 */
                Collection<L2Character> chars = world.protectionStone.getKnownList()
                        .getKnownCharactersInRadius(protectionSkill.getSkillRadius());
                if (chars != null && !chars.isEmpty())
                {
                    for (L2Character chara : chars)
                    {
                        if (chara == null)
                        {
                            continue;
                        }

                        if (chara.isInsideRadius(world.protectionStone, protectionSkill.getSkillRadius(), false,
                                false))
                        {
                            if (chara instanceof L2Playable)
                            {
                                protectionSkill.getEffects(chara, chara);
                            }
                            else
                            {
                                if (world.eventRound >= 7)
                                {
                                    weakMoment.getEffects(world.protectionStone, chara);
                                }
                            }
                        }
                    }
                }
                startQuestTimer("stone_ai", 3000, world.protectionStone, null);
            }
            else if (event.equalsIgnoreCase("gravity_core_ai"))
            {
                /*
                  Gravity Core Shield Support AI
                  This NPC will cast non-stop a UD skill to all players inside
                 */
                if (world.summonGravityCore != null && !world.summonGravityCore.isDecayed())
                {
                    Collection<L2Character> chars = world.summonGravityCore.getKnownList()
                            .getKnownCharactersInRadius(ultimateDef.getSkillRadius());
                    if (chars != null && !chars.isEmpty())
                    {
                        for (L2Character chara : chars)
                        {
                            if (chara == null ||
                                    !chara.isInsideRadius(world.summonGravityCore, ultimateDef.getSkillRadius(),
                                            false, false) || !(chara instanceof L2Playable))
                            {
                                continue;
                            }

                            ultimateDef.getEffects(world.summonGravityCore, chara);
                        }
                    }
                    startQuestTimer("gravity_core_ai", 1000, world.protectionStone, null);
                }
            }
            else if (event.equalsIgnoreCase("guard_army_ai"))
            {
                /*
                  Guard Army
                  We will just start the attack to the boss
                 */
                for (L2Npc guard : world.guardArmy)
                {
                    if (guard == null)
                    {
                        continue;
                    }

                    guard.setIsRunning(true);
                    guard.setIsInvul(true);

                    if (guard instanceof L2GuardInstance)
                    {
                        ((L2GuardInstance) guard).setCanReturnToSpawnPoint(false);
                        if (guard != world.warriorMageSup)
                        {
                            guard.setTarget(world.bossMakkum);
                            ((L2GuardInstance) guard).addDamageHate(world.bossMakkum, 500, 9999);
                            guard.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world.bossMakkum);
                        }
                    }
                }
            }
            else if (event.equalsIgnoreCase("ai_magic_sup"))
            {
                /*
                  Magic Support AI
                  This NPC will give some OP buffs to the players and depends on the amount of players without full HP will:
                  		- Cast a Heal skills to a single target
                  		- Summon a protective shield that will full-protect all the players inside
                  		- Res dead players that are inside the resurrection cast skill range
                 */
                final Collection<L2PcInstance> chars =
                        world.warriorMageSup.getKnownList().getKnownPlayersInRadius(1600);
                if (chars != null && !chars.isEmpty())
                {
                    if (world.warriorMageSup.getCurrentMp() < 800)
                    {
                        world.warriorMageSup.broadcastPacket(
                                new CreatureSay(world.warriorMageSup.getObjectId(), 1, world.warriorMageSup.getName(),
                                        "My mana power are decreasing so fast...!"));
                    }

                    List<L2PcInstance> fuckedPlayers = new ArrayList<L2PcInstance>();
                    List<L2PcInstance> deadPlayers = new ArrayList<L2PcInstance>();
                    for (L2PcInstance chara : chars)
                    {
                        if (chara == null)
                        {
                            continue;
                        }

                        if (chara.isDead() &&
                                world.warriorMageSup.isInsideRadius(chara, resSkill.getCastRange(), false, false))
                        {
                            deadPlayers.add(chara);
                        }
                        else if (chara.getCurrentHp() < chara.getMaxHp() * 0.80)
                        {
                            fuckedPlayers.add(chara);
                        }
                    }

                    int fuckedCount = fuckedPlayers.size();
                    int nextActionTime = 10000;
                    if (deadPlayers.size() > 0) //RES
                    {
                        final L2PcInstance target = deadPlayers.get(Rnd.get(deadPlayers.size() - 1));
                        if (target != null && target.isDead() &&
                                world.warriorMageSup.isInsideRadius(target, resSkill.getCastRange(), false, false) &&
                                !target.isReviveRequested())
                        {
                            world.warriorMageSup.broadcastPacket(
                                    new CreatureSay(world.warriorMageSup.getObjectId(), 1,
                                            world.warriorMageSup.getName(),
                                            target.getName() + " I'll resurrect you!"));

                            world.warriorMageSup.setTarget(target);
                            world.warriorMageSup.doCast(resSkill);

                            nextActionTime += resSkill.getHitTime() + 2000;
                        }
                    }
                    else if (fuckedCount > 0 && fuckedCount <= 3) //HEAL
                    {
                        L2PcInstance target = fuckedPlayers.get(Rnd.get(fuckedPlayers.size() - 1));
                        if (target != null && world.warriorMageSup.getCurrentMp() >= healSkill.getMpConsume())
                        {
                            world.warriorMageSup.broadcastPacket(
                                    new CreatureSay(world.warriorMageSup.getObjectId(), 1,
                                            world.warriorMageSup.getName(),
                                            target.getName() + " let me give you a hand!"));

                            world.warriorMageSup.setTarget(target);
                            world.warriorMageSup.doCast(healSkill);

                            nextActionTime += healSkill.getHitTime() + 3000;
                        }
                    }
                    else if (fuckedCount >= 4) //Protective Gravity Core
                    {
                        if (world.summonGravityCore == null || world.summonGravityCore.isDecayed() &&
                                world.warriorMageSup.getCurrentMp() >=
                                        summonTree.getMpConsume()) //Be sure we don't spawn more than one
                        {
                            world.warriorMageSup.broadcastPacket(
                                    new CreatureSay(world.warriorMageSup.getObjectId(), 1,
                                            world.warriorMageSup.getName(),
                                            "Desperate situations need desperate measures! Come all! Enter enter into that shield!"));

                            world.warriorMageSup.setTarget(world.warriorMageSup);
                            world.warriorMageSup.doCast(summonCore);

                            ThreadPoolManager.getInstance().scheduleAi(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    world.summonGravityCore = addSpawn(gravityCoreId, world.warriorMageSup.getX(),
                                            world.warriorMageSup.getY(), world.warriorMageSup.getZ() + 20, 0, false,
                                            20000, true, world.instanceId);

                                    //We will start that task only one time, then will be running all time while a tree is spawned
                                    QuestTimer coreAi = getQuestTimer("gravity_core_ai", null, null);
                                    if (coreAi == null)
                                    {
                                        startQuestTimer("gravity_core_ai", 1000, world.protectionStone, null);
                                    }
                                }
                            }, summonCore.getHitTime() + 1000);

                            nextActionTime += summonCore.getHitTime() + 5000;
                        }
                    }
                    else
                    //Give Buffs
                    {
                        int buffLevel = 0;
                        if (world.bossMakkum.getCurrentHp() < world.bossMakkum.getMaxHp() * 0.50)
                        {
                            buffLevel = 1;
                        }
                        else if (world.bossMakkum.getCurrentHp() < world.bossMakkum.getMaxHp() * 0.40)
                        {
                            buffLevel = 2;
                        }
                        else if (world.bossMakkum.getCurrentHp() < world.bossMakkum.getMaxHp() * 0.20)
                        {
                            buffLevel = 3;
                        }

                        final L2Skill buffSkill = SkillTable.getInstance()
                                .getInfo(fullBuffsIds[Rnd.get(fullBuffsIds.length)] + buffLevel, 1);
                        if (buffSkill != null && world.warriorMageSup.getCurrentMp() >= buffSkill.getMpConsume())
                        {
                            String skillType = buffSkill.getName().split(" ")[2];

                            world.warriorMageSup.broadcastPacket(
                                    new CreatureSay(world.warriorMageSup.getObjectId(), 1,
                                            world.warriorMageSup.getName(),
                                            skillType + "s! Come close to me to receive the power of " + skillType +
                                                    "s God!"));
                            world.warriorMageSup.setTarget(world.warriorMageSup);
                            world.warriorMageSup.doCast(buffPresentation);

                            //Cast the buff and delay the next task
                            ThreadPoolManager.getInstance().scheduleAi(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    for (L2PcInstance chara : chars)
                                    {
                                        if (chara == null ||
                                                !chara.isInsideRadius(world.warriorMageSup, 150, false, false))
                                        {
                                            continue;
                                        }

                                        buffSkill.getEffects(world.warriorMageSup, chara);
                                    }
                                }
                            }, buffPresentation.getHitTime() + 700);

                            nextActionTime += buffSkill.getHitTime() + 9000;
                        }
                    }
                    startQuestTimer("ai_magic_sup", nextActionTime, world.protectionStone, null);
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
    public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
    {
        if (debug)
        {
            Log.warning(getName() + ": onAttack: " + npc.getName());
        }

        final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpWorld instanceof FearonSiegeWorld)
        {
            final FearonSiegeWorld world = (FearonSiegeWorld) tmpWorld;
            if (npc == world.bossMakkum)
            {
                if (world.status == 0 && npc.getCurrentHp() < npc.getMaxHp() * 0.90)
                {
                    world.status = 1;

                    InstanceManager.getInstance().stopWholeInstance(world.instanceId);
                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new Earthquake(153581, 142081, -12741, world.eventRound, 10));
                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new SpecialCamera(world.protectionStone.getObjectId(), 1000, 0, 150, 0, 16000));
                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new MagicSkillUse(world.protectionStone, portalEffect1.getId(), 1,
                                    portalEffect1.getHitTime(), portalEffect1.getReuseDelay()));

                    ThreadPoolManager.getInstance().scheduleAi(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            InstanceManager.getInstance().sendPacket(world.instanceId,
                                    new MagicSkillUse(world.protectionStone, portalEffect2.getId(), 1,
                                            portalEffect2.getHitTime(), portalEffect2.getReuseDelay()));

                            for (int i = 0; i < 15; i++)
                            {
                                L2Npc guard = addSpawn(warriorGuard, world.protectionStone.getX(),
                                        world.protectionStone.getY(), world.protectionStone.getZ(), 0, true, 0, true,
                                        world.instanceId);
                                world.guardArmy.add(guard);

                                guard.broadcastPacket(new MagicSkillUse(guard, warriorsSpawnEffect.getId(), 1,
                                        warriorsSpawnEffect.getHitTime(), warriorsSpawnEffect.getReuseDelay()));
                            }

                            ThreadPoolManager.getInstance().scheduleAi(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    InstanceManager.getInstance().startWholeInstance(world.instanceId);
                                    InstanceManager.getInstance().sendDelayedPacketToInstance(world.instanceId, 3,
                                            new ExShowScreenMessage(1, 0, 2, 0, 0, 0, 0, true, 5 * 1000, 0,
                                                    "The Royal Army Guards from the Aden realm has been arrived!"));

                                    L2Npc randomGuard = world.guardArmy.get(Rnd.get(world.guardArmy.size()));
                                    if (randomGuard != null)
                                    {
                                        InstanceManager.getInstance().sendDelayedPacketToInstance(world.instanceId, 3,
                                                new CreatureSay(randomGuard.getObjectId(), 1, randomGuard.getName(),
                                                        "Our Captains will arrive soon!"));
                                    }

                                    startQuestTimer("guard_army_ai", 3000, world.protectionStone, null);
                                }
                            }, 4000);
                        }
                    }, 12000);
                }
                else if (world.status == 1 && npc.getCurrentHp() < npc.getMaxHp() * 0.50)
                {
                    world.status = 2;

                    L2Skill passiveSkill = SkillTable.getInstance().getInfo(passiveSkillId, 15);
                    npc.addSkill(passiveSkill);

                    InstanceManager.getInstance().stopWholeInstance(world.instanceId);
                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new Earthquake(153581, 142081, -12741, world.eventRound, 10));
                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new SpecialCamera(world.protectionStone.getObjectId(), 1000, 0, 150, 0, 16000));
                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new MagicSkillUse(world.protectionStone, portalEffect1.getId(), 1,
                                    portalEffect1.getHitTime(), portalEffect1.getReuseDelay()));

                    ThreadPoolManager.getInstance().scheduleAi(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            InstanceManager.getInstance().sendPacket(world.instanceId,
                                    new MagicSkillUse(world.protectionStone, portalEffect2.getId(), 1,
                                            portalEffect2.getHitTime(), portalEffect2.getReuseDelay()));

                            world.warriorLeona = addSpawn(warriorLeonaId, world.protectionStone.getX(),
                                    world.protectionStone.getY(), world.protectionStone.getZ() + 20, 0, true, 0, true,
                                    world.instanceId);
                            world.warriorKain = addSpawn(warriorKainId, world.protectionStone.getX(),
                                    world.protectionStone.getY(), world.protectionStone.getZ() + 20, 0, true, 0, true,
                                    world.instanceId);
                            world.warriorMageSup = addSpawn(warriorMageSUpId, world.protectionStone.getX(),
                                    world.protectionStone.getY(), world.protectionStone.getZ() + 20, 0, true, 0, true,
                                    world.instanceId);

                            world.warriorLeona.broadcastPacket(
                                    new MagicSkillUse(world.warriorLeona, warriorsSpawnEffect.getId(), 1,
                                            warriorsSpawnEffect.getHitTime(), warriorsSpawnEffect.getReuseDelay()));
                            world.warriorKain.broadcastPacket(
                                    new MagicSkillUse(world.warriorKain, warriorsSpawnEffect.getId(), 1,
                                            warriorsSpawnEffect.getHitTime(), warriorsSpawnEffect.getReuseDelay()));
                            world.warriorMageSup.broadcastPacket(
                                    new MagicSkillUse(world.warriorMageSup, warriorsSpawnEffect.getId(), 1,
                                            warriorsSpawnEffect.getHitTime(), warriorsSpawnEffect.getReuseDelay()));

                            L2Npc randomGuard = world.guardArmy.get(Rnd.get(world.guardArmy.size()));
                            if (randomGuard != null)
                            {
                                InstanceManager.getInstance().sendDelayedPacketToInstance(world.instanceId, 5,
                                        new CreatureSay(randomGuard.getObjectId(), 1, randomGuard.getName(),
                                                "They're here finally!"));
                            }

                            world.guardArmy.add(world.warriorMageSup);
                            world.guardArmy.add(world.warriorLeona);
                            world.guardArmy.add(world.warriorKain);

                            //Start It back
                            ThreadPoolManager.getInstance().scheduleAi(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    InstanceManager.getInstance().startWholeInstance(world.instanceId);

                                    InstanceManager.getInstance().sendDelayedPacketToInstance(world.instanceId, 3,
                                            new ExShowScreenMessage(1, 0, 2, 0, 0, 0, 0, true, 5 * 1000, 0,
                                                    "The Royal Army Captains from the Aden realm has been arrived!"));

                                    InstanceManager.getInstance().sendDelayedPacketToInstance(world.instanceId, 3,
                                            new CreatureSay(world.warriorLeona.getObjectId(), 1,
                                                    world.warriorLeona.getName(), "Keep pushing warriors!"));
                                    InstanceManager.getInstance().sendDelayedPacketToInstance(world.instanceId, 3,
                                            new CreatureSay(world.warriorKain.getObjectId(), 1,
                                                    world.warriorKain.getName(), "C'mon!"));

                                    startQuestTimer("guard_army_ai", 3000, world.protectionStone, null);
                                    startQuestTimer("ai_magic_sup", 6000, world.protectionStone, null);
                                }
                            }, 3000);
                        }
                    }, 12000);
                }
                else if (world.status == 2 && npc.getCurrentHp() < npc.getMaxHp() * 0.10)
                {
                    world.status = 3;

                    L2Skill passiveSkill = SkillTable.getInstance().getInfo(passiveSkillId, 20);
                    npc.addSkill(passiveSkill);
                }
            }
        }
        return "";
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (debug)
        {
            Log.warning(getName() + ": onKill: " + npc.getName());
        }

        InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpworld instanceof FearonSiegeWorld)
        {
            FearonSiegeWorld world = (FearonSiegeWorld) tmpworld;
            if (Util.contains(invadeMobs, npc.getNpcId()))
            {
                synchronized (world.allMinions)
                {
                    if (world.allMinions.contains(npc))
                    {
                        world.allMinions.remove(npc);
                        if (world.allMinions.isEmpty())
                        {
                            InstanceManager.getInstance().sendPacket(world.instanceId,
                                    new ExShowScreenMessage(1, 0, 2, 0, 0, 0, 0, true, 5 * 1000, 0,
                                            "Next round will start in 15 seconds!"));

                            startQuestTimer("stage_all_spawn_round", 15000, world.protectionStone, null);
                        }
                    }
                }
            }
            else if (npc.getNpcId() == makkumBossId)
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
                            pMember.addItem(qn, DimensionalDoor.getDimensionalDoorRewardId(),
                                    Rnd.get(70 * DimensionalDoor.getDimensionalDoorRewardRate(),
                                            80 * DimensionalDoor.getDimensionalDoorRewardRate()), player, true);
                        }
                        else
                        {
                            pMember.sendMessage("Nice attempt, but you already got a reward!");
                        }
                    }
                }
                InstanceManager.getInstance().setInstanceReuse(world.instanceId, instanceTemplateId, 6, 30);
                InstanceManager.getInstance().finishInstance(world.instanceId, true);
            }
        }
        return super.onKill(npc, player, isPet);
    }

    private final synchronized void enterInstance(L2PcInstance player)
    {
        InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
        if (world != null)
        {
            if (!(world instanceof FearonSiegeWorld))
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
                    player.teleToLocation(-78583, 248231, -3303, 56847, true);
                }
            }
            return;
        }
        else
        {
            if (!debug && !InstanceManager.getInstance()
                    .checkInstanceConditions(player, instanceTemplateId, 7, 7, 99, Config.MAX_LEVEL))
            {
                return;
            }

            final int instanceId = InstanceManager.getInstance().createDynamicInstance(qn + ".xml");
            world = new FearonSiegeWorld();
            world.instanceId = instanceId;
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
                enterPlayer.teleToLocation(-78583, 248231, -3303, 56847, true);
            }

            startQuestTimer("stage_1_start", 1000, null, player);

            Log.fine(getName() + ": instance started: " + instanceId + " created by player: " + player.getName());
            return;
        }
    }

    public static void main(String[] args)
    {
        new FaeronSiege(-1, qn, "instances/DimensionalDoor");
    }
}
