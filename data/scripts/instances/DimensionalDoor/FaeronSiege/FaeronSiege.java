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
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author LasTravel
 */
public class FaeronSiege extends Quest
{
    private static final boolean _debug = false;
    private static final String _qn = "FaeronSiege";

    //Ids
    private static final int _instanceTemplateId = 502;
    private static final int _protectionStoneId = 13425;
    private static final int _dummyInvaderDoor = 19074;
    private static final int _passiveSkillId = 90003;
    private static final int _makkumBossId = 26090;
    private static final int _warriorGuard = 33518;
    private static final int _warriorLeonaId = 33898;
    private static final int _warriorKainId = 33993;
    private static final int _warriorMageSUpId = 19495;
    private static final int _gravityCoreId = 13435;
    private static final int[] _invadeMobs = {23477, 23478, 19555}; //Abyssal Shaman, Abyssal Berserker, Abyssal Imp
    private static final int[] _fullBuffsIds = {15129, 15133, 15137};
    //Skills
    private static final L2Skill _protectionSkill = SkillTable.getInstance().getInfo(14085, 1);
    private static final L2Skill _weakMoment = SkillTable.getInstance().getInfo(14558, 1);
    private static final L2Skill _portalEffect1 = SkillTable.getInstance().getInfo(6783, 1);
    private static final L2Skill _portalEffect2 = SkillTable.getInstance().getInfo(6799, 1);
    private static final L2Skill _warriorsSpawnEffect = SkillTable.getInstance().getInfo(6176, 1);
    private static final L2Skill _buffPresentation = SkillTable.getInstance().getInfo(15368, 6);
    private static final L2Skill _resSkill = SkillTable.getInstance().getInfo(1016, 6);
    private static final L2Skill _healSkill = SkillTable.getInstance().getInfo(11570, 1);
    private static final L2Skill _summonTree = SkillTable.getInstance().getInfo(14902, 1);
    private static final L2Skill _ultimateDef = SkillTable.getInstance().getInfo(23451, 3);
    private static final L2Skill _summonCore = SkillTable.getInstance().getInfo(6848, 1);

    public FaeronSiege(int questId, String name, String descr)
    {
        super(questId, name, descr);

        addTalkId(DimensionalDoor.getNpcManagerId());
        addStartNpc(DimensionalDoor.getNpcManagerId());

        addAttackId(_makkumBossId);
        addKillId(_makkumBossId);
        addSkillSeeId(_makkumBossId);

        addFirstTalkId(_warriorGuard);
        addFirstTalkId(_warriorLeonaId);
        addFirstTalkId(_warriorKainId);
        addFirstTalkId(_warriorMageSUpId);
        addFirstTalkId(_gravityCoreId);

        for (int i : _invadeMobs)
        {
            addAttackId(i);
            addKillId(i);
        }
    }

    private class FearonSiegeWorld extends InstanceWorld
    {
        private int _eventRound;
        private L2Npc _warriorLeona;
        private L2Npc _warriorKain;
        private L2Npc _warriorMageSup;
        private L2Npc _protectionStone;
        private L2Npc _summonGravityCore;
        private L2Npc _bossMakkum;
        private List<L2Npc> _allMinions;
        private List<L2Npc> _guardArmy;
        private ArrayList<L2PcInstance> _rewardedPlayers;

        private FearonSiegeWorld()
        {
            _allMinions = new ArrayList<L2Npc>();
            _guardArmy = new ArrayList<L2Npc>();
            _rewardedPlayers = new ArrayList<L2PcInstance>();
        }
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

        if (wrld != null && wrld instanceof FearonSiegeWorld)
        {
            final FearonSiegeWorld world = (FearonSiegeWorld) wrld;
            if (event.equalsIgnoreCase("stage_1_start"))
            {
                if (_debug)
                {
                    world._eventRound = 9;
                }
                world._protectionStone =
                        addSpawn(_protectionStoneId, -79660, 244954, -3651 + 20, 0, false, 0, false, world.instanceId);

                startQuestTimer("stone_ai", 3000, world._protectionStone, null);

                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new ExShowScreenMessage("Sarch the Protection Stone out of the Fearon Village!", 8000));
                InstanceManager.getInstance().sendDelayedPacketToInstance(world.instanceId, 10,
                        new ExShowScreenMessage("Stay close to the Protection Stone!", 5000));

                //Spawn dummy effects
                for (int i = 0; i < 61; i++)
                {
                    int x = (int) (1200 * Math.cos(i * 0.618));
                    int y = (int) (1200 * Math.sin(i * 0.618));
                    addSpawn(_dummyInvaderDoor, -79660 + x, 244954 + y, -3651 + 20, -1, false, 0, true,
                            world.instanceId);
                }

                startQuestTimer("stage_all_spawn_round", _debug ? 60000 : 60000 * 3, world._protectionStone, null);
            }
            else if (event.equalsIgnoreCase("stage_all_spawn_round"))
            {
                world._eventRound++;

                InstanceManager.getInstance()
                        .sendPacket(world.instanceId, new Earthquake(153581, 142081, -12741, world._eventRound, 10));
                InstanceManager.getInstance().sendPacket(world.instanceId,
                        new ExShowScreenMessage(1, 0, 2, 0, 0, 0, 0, true, 5 * 1000, 0, "Round: " + world._eventRound));

                if (world._eventRound == 7)
                {
                    InstanceManager.getInstance().sendDelayedPacketToInstance(world.instanceId, 6,
                            new ExShowScreenMessage(1, 0, 2, 0, 0, 0, 0, true, 5 * 1000, 0,
                                    "The Protection Stone now can debuff the enemies!"));
                }

                L2Skill passiveSkill = SkillTable.getInstance()
                        .getInfo(_passiveSkillId, world._eventRound < 10 ? world._eventRound * 2 : 10);
                if (world._eventRound < 10)
                {
                    for (int i = 0; i < 61; i++)
                    {
                        int x = (int) (1200 * Math.cos(i * 0.618));
                        int y = (int) (1200 * Math.sin(i * 0.618));

                        L2Npc minion =
                                addSpawn(_invadeMobs[Rnd.get(_invadeMobs.length)], -79660 + x, 244954 + y, -3651 + 20,
                                        -1, false, 0, true, world.instanceId);
                        minion.setIsRunning(true);
                        minion.addSkill(passiveSkill);
                        minion.setCurrentHpMp(minion.getMaxHp(), minion.getMaxMp());

                        synchronized (world._allMinions)
                        {
                            world._allMinions.add(minion);
                        }
                    }
                }
                else if (world._eventRound == 10)
                {
                    //BossTime
                    world._bossMakkum =
                            addSpawn(_makkumBossId, -80015, 244904, -3677, 917, false, 0, true, world.instanceId);

                    world._bossMakkum.addSkill(passiveSkill);
                    world._bossMakkum.setCurrentHpMp(world._bossMakkum.getMaxHp(), world._bossMakkum.getMaxMp());

                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new CreatureSay(world._bossMakkum.getObjectId(), 1, world._bossMakkum.getName(),
                                    "It's the time to end with your lives!"));
                }
            }
            else if (event.equalsIgnoreCase("stone_ai"))
            {
                /**
                 * Protection Stone AI
                 * This NPC will cast non-stop one buff to all the players that are inside his activity radius.
                 */
                Collection<L2Character> chars = world._protectionStone.getKnownList()
                        .getKnownCharactersInRadius(_protectionSkill.getSkillRadius());
                if (chars != null && !chars.isEmpty())
                {
                    for (L2Character chara : chars)
                    {
                        if (chara == null)
                        {
                            continue;
                        }

                        if (chara.isInsideRadius(world._protectionStone, _protectionSkill.getSkillRadius(), false,
                                false))
                        {
                            if (chara instanceof L2Playable)
                            {
                                _protectionSkill.getEffects(chara, chara);
                            }
                            else
                            {
                                if (world._eventRound >= 7)
                                {
                                    _weakMoment.getEffects(world._protectionStone, chara);
                                }
                            }
                        }
                    }
                }
                startQuestTimer("stone_ai", 3000, world._protectionStone, null);
            }
            else if (event.equalsIgnoreCase("gravity_core_ai"))
            {
                /**
                 * Gravity Core Shield Support AI
                 * This NPC will cast non-stop a UD skill to all players inside
                 */
                if (world._summonGravityCore != null && !world._summonGravityCore.isDecayed())
                {
                    Collection<L2Character> chars = world._summonGravityCore.getKnownList()
                            .getKnownCharactersInRadius(_ultimateDef.getSkillRadius());
                    if (chars != null && !chars.isEmpty())
                    {
                        for (L2Character chara : chars)
                        {
                            if (chara == null ||
                                    !chara.isInsideRadius(world._summonGravityCore, _ultimateDef.getSkillRadius(),
                                            false, false) || !(chara instanceof L2Playable))
                            {
                                continue;
                            }

                            _ultimateDef.getEffects(world._summonGravityCore, chara);
                        }
                    }
                    startQuestTimer("gravity_core_ai", 1000, world._protectionStone, null);
                }
            }
            else if (event.equalsIgnoreCase("guard_army_ai"))
            {
                /**
                 * Guard Army
                 * We will just start the attack to the boss
                 */
                for (L2Npc guard : world._guardArmy)
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
                        if (guard != world._warriorMageSup)
                        {
                            guard.setTarget(world._bossMakkum);
                            ((L2GuardInstance) guard).addDamageHate(world._bossMakkum, 500, 9999);
                            guard.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world._bossMakkum);
                        }
                    }
                }
            }
            else if (event.equalsIgnoreCase("ai_magic_sup"))
            {
                /**
                 * Magic Support AI
                 * This NPC will give some OP buffs to the players and depends on the amount of players without full HP will:
                 * 		- Cast a Heal skills to a single target
                 * 		- Summon a protective shield that will full-protect all the players inside
                 * 		- Res dead players that are inside the resurrection cast skill range
                 */
                final Collection<L2PcInstance> chars =
                        world._warriorMageSup.getKnownList().getKnownPlayersInRadius(1600);
                if (chars != null && !chars.isEmpty())
                {
                    if (world._warriorMageSup.getCurrentMp() < 800)
                    {
                        world._warriorMageSup.broadcastPacket(
                                new CreatureSay(world._warriorMageSup.getObjectId(), 1, world._warriorMageSup.getName(),
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
                                world._warriorMageSup.isInsideRadius(chara, _resSkill.getCastRange(), false, false))
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
                                world._warriorMageSup.isInsideRadius(target, _resSkill.getCastRange(), false, false) &&
                                !target.isReviveRequested())
                        {
                            world._warriorMageSup.broadcastPacket(
                                    new CreatureSay(world._warriorMageSup.getObjectId(), 1,
                                            world._warriorMageSup.getName(),
                                            target.getName() + " I'll resurrect you!"));

                            world._warriorMageSup.setTarget(target);
                            world._warriorMageSup.doCast(_resSkill);

                            nextActionTime += _resSkill.getHitTime() + 2000;
                        }
                    }
                    else if (fuckedCount > 0 && fuckedCount <= 3) //HEAL
                    {
                        L2PcInstance target = fuckedPlayers.get(Rnd.get(fuckedPlayers.size() - 1));
                        if (target != null && world._warriorMageSup.getCurrentMp() >= _healSkill.getMpConsume())
                        {
                            world._warriorMageSup.broadcastPacket(
                                    new CreatureSay(world._warriorMageSup.getObjectId(), 1,
                                            world._warriorMageSup.getName(),
                                            target.getName() + " let me give you a hand!"));

                            world._warriorMageSup.setTarget(target);
                            world._warriorMageSup.doCast(_healSkill);

                            nextActionTime += _healSkill.getHitTime() + 3000;
                        }
                    }
                    else if (fuckedCount >= 4) //Protective Gravity Core
                    {
                        if (world._summonGravityCore == null || world._summonGravityCore.isDecayed() &&
                                world._warriorMageSup.getCurrentMp() >=
                                        _summonTree.getMpConsume()) //Be sure we don't spawn more than one
                        {
                            world._warriorMageSup.broadcastPacket(
                                    new CreatureSay(world._warriorMageSup.getObjectId(), 1,
                                            world._warriorMageSup.getName(),
                                            "Desperate situations need desperate measures! Come all! Enter enter into that shield!"));

                            world._warriorMageSup.setTarget(world._warriorMageSup);
                            world._warriorMageSup.doCast(_summonCore);

                            ThreadPoolManager.getInstance().scheduleAi(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    world._summonGravityCore = addSpawn(_gravityCoreId, world._warriorMageSup.getX(),
                                            world._warriorMageSup.getY(), world._warriorMageSup.getZ() + 20, 0, false,
                                            20000, true, world.instanceId);

                                    //We will start that task only one time, then will be running all time while a tree is spawned
                                    QuestTimer coreAi = getQuestTimer("gravity_core_ai", null, null);
                                    if (coreAi == null)
                                    {
                                        startQuestTimer("gravity_core_ai", 1000, world._protectionStone, null);
                                    }
                                }
                            }, _summonCore.getHitTime() + 1000);

                            nextActionTime += _summonCore.getHitTime() + 5000;
                        }
                    }
                    else
                    //Give Buffs
                    {
                        int buffLevel = 0;
                        if (world._bossMakkum.getCurrentHp() < world._bossMakkum.getMaxHp() * 0.50)
                        {
                            buffLevel = 1;
                        }
                        else if (world._bossMakkum.getCurrentHp() < world._bossMakkum.getMaxHp() * 0.40)
                        {
                            buffLevel = 2;
                        }
                        else if (world._bossMakkum.getCurrentHp() < world._bossMakkum.getMaxHp() * 0.20)
                        {
                            buffLevel = 3;
                        }

                        final L2Skill buffSkill = SkillTable.getInstance()
                                .getInfo(_fullBuffsIds[Rnd.get(_fullBuffsIds.length)] + buffLevel, 1);
                        if (buffSkill != null && world._warriorMageSup.getCurrentMp() >= buffSkill.getMpConsume())
                        {
                            String skillType = buffSkill.getName().split(" ")[2];

                            world._warriorMageSup.broadcastPacket(
                                    new CreatureSay(world._warriorMageSup.getObjectId(), 1,
                                            world._warriorMageSup.getName(),
                                            skillType + "s! Come close to me to receive the power of " + skillType +
                                                    "s God!"));
                            world._warriorMageSup.setTarget(world._warriorMageSup);
                            world._warriorMageSup.doCast(_buffPresentation);

                            //Cast the buff and delay the next task
                            ThreadPoolManager.getInstance().scheduleAi(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    for (L2PcInstance chara : chars)
                                    {
                                        if (chara == null ||
                                                !chara.isInsideRadius(world._warriorMageSup, 150, false, false))
                                        {
                                            continue;
                                        }

                                        buffSkill.getEffects(world._warriorMageSup, chara);
                                    }
                                }
                            }, _buffPresentation.getHitTime() + 700);

                            nextActionTime += buffSkill.getHitTime() + 9000;
                        }
                    }
                    startQuestTimer("ai_magic_sup", nextActionTime, world._protectionStone, null);
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
        if (_debug)
        {
            Log.warning(getName() + ": onAttack: " + npc.getName());
        }

        final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
        if (tmpWorld instanceof FearonSiegeWorld)
        {
            final FearonSiegeWorld world = (FearonSiegeWorld) tmpWorld;
            if (npc == world._bossMakkum)
            {
                if (world.status == 0 && npc.getCurrentHp() < npc.getMaxHp() * 0.90)
                {
                    world.status = 1;

                    InstanceManager.getInstance().stopWholeInstance(world.instanceId);
                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new Earthquake(153581, 142081, -12741, world._eventRound, 10));
                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new SpecialCamera(world._protectionStone.getObjectId(), 1000, 0, 150, 0, 16000));
                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new MagicSkillUse(world._protectionStone, _portalEffect1.getId(), 1,
                                    _portalEffect1.getHitTime(), _portalEffect1.getReuseDelay()));

                    ThreadPoolManager.getInstance().scheduleAi(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            InstanceManager.getInstance().sendPacket(world.instanceId,
                                    new MagicSkillUse(world._protectionStone, _portalEffect2.getId(), 1,
                                            _portalEffect2.getHitTime(), _portalEffect2.getReuseDelay()));

                            for (int i = 0; i < 15; i++)
                            {
                                L2Npc guard = addSpawn(_warriorGuard, world._protectionStone.getX(),
                                        world._protectionStone.getY(), world._protectionStone.getZ(), 0, true, 0, true,
                                        world.instanceId);
                                world._guardArmy.add(guard);

                                guard.broadcastPacket(new MagicSkillUse(guard, _warriorsSpawnEffect.getId(), 1,
                                        _warriorsSpawnEffect.getHitTime(), _warriorsSpawnEffect.getReuseDelay()));
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

                                    L2Npc randomGuard = world._guardArmy.get(Rnd.get(world._guardArmy.size()));
                                    if (randomGuard != null)
                                    {
                                        InstanceManager.getInstance().sendDelayedPacketToInstance(world.instanceId, 3,
                                                new CreatureSay(randomGuard.getObjectId(), 1, randomGuard.getName(),
                                                        "Our Captains will arrive soon!"));
                                    }

                                    startQuestTimer("guard_army_ai", 3000, world._protectionStone, null);
                                }
                            }, 4000);
                        }
                    }, 12000);
                }
                else if (world.status == 1 && npc.getCurrentHp() < npc.getMaxHp() * 0.50)
                {
                    world.status = 2;

                    L2Skill passiveSkill = SkillTable.getInstance().getInfo(_passiveSkillId, 15);
                    npc.addSkill(passiveSkill);

                    InstanceManager.getInstance().stopWholeInstance(world.instanceId);
                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new Earthquake(153581, 142081, -12741, world._eventRound, 10));
                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new SpecialCamera(world._protectionStone.getObjectId(), 1000, 0, 150, 0, 16000));
                    InstanceManager.getInstance().sendPacket(world.instanceId,
                            new MagicSkillUse(world._protectionStone, _portalEffect1.getId(), 1,
                                    _portalEffect1.getHitTime(), _portalEffect1.getReuseDelay()));

                    ThreadPoolManager.getInstance().scheduleAi(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            InstanceManager.getInstance().sendPacket(world.instanceId,
                                    new MagicSkillUse(world._protectionStone, _portalEffect2.getId(), 1,
                                            _portalEffect2.getHitTime(), _portalEffect2.getReuseDelay()));

                            world._warriorLeona = addSpawn(_warriorLeonaId, world._protectionStone.getX(),
                                    world._protectionStone.getY(), world._protectionStone.getZ() + 20, 0, true, 0, true,
                                    world.instanceId);
                            world._warriorKain = addSpawn(_warriorKainId, world._protectionStone.getX(),
                                    world._protectionStone.getY(), world._protectionStone.getZ() + 20, 0, true, 0, true,
                                    world.instanceId);
                            world._warriorMageSup = addSpawn(_warriorMageSUpId, world._protectionStone.getX(),
                                    world._protectionStone.getY(), world._protectionStone.getZ() + 20, 0, true, 0, true,
                                    world.instanceId);

                            world._warriorLeona.broadcastPacket(
                                    new MagicSkillUse(world._warriorLeona, _warriorsSpawnEffect.getId(), 1,
                                            _warriorsSpawnEffect.getHitTime(), _warriorsSpawnEffect.getReuseDelay()));
                            world._warriorKain.broadcastPacket(
                                    new MagicSkillUse(world._warriorKain, _warriorsSpawnEffect.getId(), 1,
                                            _warriorsSpawnEffect.getHitTime(), _warriorsSpawnEffect.getReuseDelay()));
                            world._warriorMageSup.broadcastPacket(
                                    new MagicSkillUse(world._warriorMageSup, _warriorsSpawnEffect.getId(), 1,
                                            _warriorsSpawnEffect.getHitTime(), _warriorsSpawnEffect.getReuseDelay()));

                            L2Npc randomGuard = world._guardArmy.get(Rnd.get(world._guardArmy.size()));
                            if (randomGuard != null)
                            {
                                InstanceManager.getInstance().sendDelayedPacketToInstance(world.instanceId, 5,
                                        new CreatureSay(randomGuard.getObjectId(), 1, randomGuard.getName(),
                                                "They're here finally!"));
                            }

                            world._guardArmy.add(world._warriorMageSup);
                            world._guardArmy.add(world._warriorLeona);
                            world._guardArmy.add(world._warriorKain);

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
                                            new CreatureSay(world._warriorLeona.getObjectId(), 1,
                                                    world._warriorLeona.getName(), "Keep pushing warriors!"));
                                    InstanceManager.getInstance().sendDelayedPacketToInstance(world.instanceId, 3,
                                            new CreatureSay(world._warriorKain.getObjectId(), 1,
                                                    world._warriorKain.getName(), "C'mon!"));

                                    startQuestTimer("guard_army_ai", 3000, world._protectionStone, null);
                                    startQuestTimer("ai_magic_sup", 6000, world._protectionStone, null);
                                }
                            }, 3000);
                        }
                    }, 12000);
                }
                else if (world.status == 2 && npc.getCurrentHp() < npc.getMaxHp() * 0.10)
                {
                    world.status = 3;

                    L2Skill passiveSkill = SkillTable.getInstance().getInfo(_passiveSkillId, 20);
                    npc.addSkill(passiveSkill);
                }
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
        if (tmpworld instanceof FearonSiegeWorld)
        {
            FearonSiegeWorld world = (FearonSiegeWorld) tmpworld;
            if (Util.contains(_invadeMobs, npc.getNpcId()))
            {
                synchronized (world._allMinions)
                {
                    if (world._allMinions.contains(npc))
                    {
                        world._allMinions.remove(npc);
                        if (world._allMinions.isEmpty())
                        {
                            InstanceManager.getInstance().sendPacket(world.instanceId,
                                    new ExShowScreenMessage(1, 0, 2, 0, 0, 0, 0, true, 5 * 1000, 0,
                                            "Next round will start in 15 seconds!"));

                            startQuestTimer("stage_all_spawn_round", 15000, world._protectionStone, null);
                        }
                    }
                }
            }
            else if (npc.getNpcId() == _makkumBossId)
            {
                if (player.isInParty())
                {
                    for (L2PcInstance pMember : player.getParty().getPartyMembers())
                    {
                        if (pMember == null || pMember.getInstanceId() != world.instanceId)
                        {
                            continue;
                        }

                        if (InstanceManager.getInstance().canGetUniqueReward(pMember, world._rewardedPlayers))
                        {
                            world._rewardedPlayers.add(pMember);
                            pMember.addItem(_qn, DimensionalDoor.getDimensionalDoorRewardId(),
                                    Rnd.get(70 * DimensionalDoor.getDimensionalDoorRewardRate(),
                                            80 * DimensionalDoor.getDimensionalDoorRewardRate()), player, true);
                        }
                        else
                        {
                            pMember.sendMessage("Nice attempt, but you already got a reward!");
                        }
                    }
                }
                InstanceManager.getInstance().setInstanceReuse(world.instanceId, _instanceTemplateId, 6, 30);
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
            if (!_debug && !InstanceManager.getInstance()
                    .checkInstanceConditions(player, _instanceTemplateId, 7, 7, 99, Config.MAX_LEVEL))
            {
                return;
            }

            final int instanceId = InstanceManager.getInstance().createDynamicInstance(_qn + ".xml");
            world = new FearonSiegeWorld();
            world.instanceId = instanceId;
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
                enterPlayer.teleToLocation(-78583, 248231, -3303, 56847, true);
            }

            startQuestTimer("stage_1_start", 1000, null, player);

            Log.fine(getName() + ": instance started: " + instanceId + " created by player: " + player.getName());
            return;
        }
    }

    public static void main(String[] args)
    {
        new FaeronSiege(-1, _qn, "instances/DimensionalDoor");
    }
}
