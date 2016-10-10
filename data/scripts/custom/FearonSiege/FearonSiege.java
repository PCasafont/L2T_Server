package custom.FearonSiege;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import l2server.Config;
import l2server.gameserver.Announcements;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Party;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2GuardInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.Earthquake;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.network.serverpackets.SpecialCamera;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

/**
 * @author LasTravel
 */

@SuppressWarnings("all")
public class FearonSiege extends Quest
{
    //Quest
    private static final boolean _debug = false;
    private static final String _qn = "FearonSiege";

    //Ids & Configs
    private static final boolean _isEventOn = false;
    private static final int _protectionStoneId = 13425;
    private static final int _dummyKainVanHalter = 33979;
    private static final int _dummyInvaderDoor = 19074;
    private static final int _makkumBossId = 26090;
    private static final int[] _invadeMobs = {23477, 23478, 19555}; //Abyssal Shaman, Abyssal Berserker, Abyssal Imp
    private static final int _warriorLeonaId = 33898;
    private static final int _warriorKainId = 33993;
    private static final int _warriorMageSUpId = 19495;
    private static final int _warriorGuard = 33518;
    private static final int _summonTreeId = 27486;
    private static final int _gravityCoreId = 13435;
    //Dummy Effects
    private static final L2Skill _portalEffect1 = SkillTable.getInstance().getInfo(6783, 1);
    private static final L2Skill _portalEffect2 = SkillTable.getInstance().getInfo(6799, 1);
    private static final L2Skill _warriorsSpawnEffect = SkillTable.getInstance().getInfo(6176, 1);
    //Protection Stone AI Skill
    private static final L2Skill _protectionSkill = SkillTable.getInstance().getInfo(14085, 1);
    private static final L2Skill _weakMoment = SkillTable.getInstance().getInfo(14558, 1);
    //Humman Support AI Skills
    private static final int[] _fullBuffsIds = {15129, 15133, 15137};
    private static final L2Skill _buffPresentation = SkillTable.getInstance().getInfo(15368, 6);
    private static final L2Skill _resSkill = SkillTable.getInstance().getInfo(1016, 6);
    private static final L2Skill _healSkill = SkillTable.getInstance().getInfo(11570, 1);
    private static final L2Skill _summonTree = SkillTable.getInstance().getInfo(14902, 1);
    private static final L2Skill _blessingOfTree = SkillTable.getInstance().getInfo(11806, 4);
    private static final L2Skill _ultimateDef = SkillTable.getInstance().getInfo(23451, 3);
    private static final L2Skill _summonCore = SkillTable.getInstance().getInfo(6848, 1);
    private static final int _passiveSkillId = 90003;
    private static final String[] _dummyKainTexts = {
            "They... They are coming...",
            "They will arrive soon...",
            "No one will be safe...",
            "But... The Aden army will be ready to fight!",
            "Hurry up Leona Blackbird please...",
            "Fearon Village is in dangerous..."
    };

    //Vars
    private static int _eventStatus = 0;
    private static int _eventRound = 0;
    private static int _instanceId = 0;
    private static L2Npc _protectionStone = null;
    private static L2Npc _dummyKain = null;
    private static L2Npc _bossMakkum = null;
    //Warrior Npcs
    private static L2Npc _warriorLeona = null;
    private static L2Npc _warriorKain = null;
    private static L2Npc _warriorMageSup = null;
    //Warrior Summons
    private static L2Npc _summonTreeHelper = null;
    private static L2Npc _summonGravityCore = null;
    private static List<L2Npc> _guardArmy = new ArrayList<L2Npc>();
    private static List<L2Npc> _allMinions = new ArrayList<L2Npc>();
    private static List<String> _hwids = new ArrayList<String>();
    private static List<Integer> _playerIds = new ArrayList<Integer>();
    private static Map<Integer, Map<L2Party, Long>> _attackerParty = new HashMap<Integer, Map<L2Party, Long>>();
    private static Map<Integer, Map<Integer, Integer>> _rewardList = new HashMap<Integer, Map<Integer, Integer>>();
    private static Map<Integer, Long> _rewardedInfo = new HashMap<Integer, Long>();

    public FearonSiege(int questId, String name, String descr)
    {
        super(questId, name, descr);

        if (!Config.isServer(Config.TENKAI))
        {
            return;
        }

        _dummyKain = addSpawn(_dummyKainVanHalter, 83789, 148617, -3400, 32402, false, 0);

        addFirstTalkId(_dummyKainVanHalter);
        addFirstTalkId(_warriorLeonaId);
        addFirstTalkId(_warriorKainId);
        addFirstTalkId(_warriorMageSUpId);

        if (!_isEventOn)
        {
            notifyEvent("random_text", null, null);
            startQuestTimer("random_text", 900000, null, null, true);
        }
        else
        {
            addTalkId(_dummyKainVanHalter);
            addStartNpc(_dummyKainVanHalter);

            for (int i : _invadeMobs)
            {
                addAttackId(i);
                addKillId(i);
                addSkillSeeId(i);
            }

            addAttackId(_makkumBossId);
            addKillId(_makkumBossId);
            addSkillSeeId(_makkumBossId);

            for (int i = 1; i <= 20; i++)
            {
                Map<Integer, Integer> rewards = new HashMap<Integer, Integer>();
                rewards.put(4357, 20000 + 500 * i); //Silver Shilen

                if (i >= 5)
                {
                    rewards.put(36513, 5 + i); //Elcyum Powder
                }
                if (i >= 10)
                {
                    rewards.put(4356, 3 + i); //Gold Einhasad
                }
                if (i >= 15)
                {
                    rewards.put(36414, 5 + i); //Dragon Claw
                }

                if (i == 20)
                {
                    rewards.put(4037, 5); //Coin of Luck

                    rewards.put(36417, 1); //Antharas Shaper - Fragment
                    rewards.put(36418, 1); //Antharas Slasher - Fragment
                    rewards.put(36419, 1); //Antharas Thrower - Fragment
                    rewards.put(36420, 1); //Antharas Buster - Fragment
                    rewards.put(36421, 1); //Antharas Cutter - Fragment
                    rewards.put(36422, 1); //Antharas Stormer - Fragment
                    rewards.put(36423, 1); //Antharas Fighter - Fragment
                    rewards.put(36424, 1); //Antharas Avenger - Fragment
                    rewards.put(36425, 1); //Antharas Dual Blunt Weapon - Fragment
                    rewards.put(36426, 1); //Antharas Dualsword - Fragment

                    rewards.put(36427, 1); //Valakas Shaper - Fragment
                    rewards.put(36428, 1); //Valakas Cutter - Fragment
                    rewards.put(36429, 1); //Valakas Slasher - Fragment
                    rewards.put(36430, 1); //Valakas Thrower - Fragment
                    rewards.put(36431, 1); //Valakas Buster - Fragment
                    rewards.put(36432, 1); //Valakas Caster - Fragment
                    rewards.put(36433, 1); //Valakas Retributer - Fragment

                    rewards.put(36434, 1); //Lindvior Shaper - Fragment
                    rewards.put(36435, 1); //Lindvior Thrower - Fragment
                    rewards.put(36436, 1); //Lindvior Slasher - Fragment
                    rewards.put(36437, 1); //Lindvior Caster - Fragment
                    rewards.put(36438, 1); //Lindvior Cutter - Fragment
                    rewards.put(36439, 1); //Lindvior Shooter - Fragment
                    rewards.put(36440, 1); //Lindvior Dual Dagger - Fragment
                }

                _rewardList.put(i, rewards);
            }
        }
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onAdvEvent: " + event);
        }

        if (event.equalsIgnoreCase("random_text"))
        {
            _dummyKain.broadcastPacket(new CreatureSay(_dummyKain.getObjectId(), 1, _dummyKain
                    .getName(), _dummyKainTexts[Rnd.get(_dummyKainTexts.length)]));
        }
        else if (event.equalsIgnoreCase("launch_event"))
        {
            if (player.isGM() && _eventStatus == 0)
            {
                _eventStatus = 1;

                notifyEvent("stage_0_prepare_event", null, null);

                startQuestTimer("end_event", 11400000, null, null);
            }
        }
        else if (event.equalsIgnoreCase("end_event"))
        {
            notifyEvent("cancel_timers", null, null);

            startQuestTimer("restart_variables", 300000, null, null);
        }
        else if (event.equalsIgnoreCase("enter_instance"))
        {
            if (_isEventOn && _eventStatus == 1 && _eventRound < 15 && _instanceId != 0)
            {
                String playerHwid = player.getHWID();
                if (playerHwid != null && !playerHwid.isEmpty() && !_hwids.contains(playerHwid) || _playerIds
                        .contains(player.getObjectId()) && _hwids.contains(playerHwid))
                {
                    _hwids.add(playerHwid);
                    _playerIds.add(player.getObjectId());

                    player.setInstanceId(_instanceId);
                    player.teleToLocation(-78583, 248231, -3303, 56847, true);
                    player.sendPacket(
                            new ExShowScreenMessage("Sarch the Protection Stone out of the Fearon Village!", 5000));
                }
                else
                {
                    player.sendMessage("You can enter only with one character...!");
                }
            }
            else
            {
                player.sendMessage("You can't enter right now...!");
            }
        }
        else if (event.equalsIgnoreCase("restart_variables"))
        {
            _instanceId = 0;
            _eventStatus = 0;
            _eventRound = 0;

            _protectionStone = null;
            _dummyKain = null;
            _bossMakkum = null;
            _warriorLeona = null;
            _warriorKain = null;
            _warriorMageSup = null;
            _summonTreeHelper = null;
            _summonGravityCore = null;

            _allMinions.clear();
            _attackerParty.clear();
            _rewardedInfo.clear();
            _playerIds.clear();
            _hwids.clear();
            _guardArmy.clear();
        }
        else if (event.equalsIgnoreCase("cancel_timers"))
        {
            QuestTimer stoneAi = getQuestTimer("stone_ai", null, null);
            if (stoneAi != null)
            {
                stoneAi.cancel();
            }

            QuestTimer treeAi = getQuestTimer("tree_ai", null, null);
            if (treeAi != null)
            {
                treeAi.cancel();
            }

            QuestTimer leonaAi = getQuestTimer("leona_ai", null, null);
            if (leonaAi != null)
            {
                leonaAi.cancel();
            }

            QuestTimer kainAi = getQuestTimer("kain_ai", null, null);
            if (kainAi != null)
            {
                kainAi.cancel();
            }

            QuestTimer gravityAi = getQuestTimer("gravity_core_ai", null, null);
            if (gravityAi != null)
            {
                gravityAi.cancel();
            }

            QuestTimer magicSupAi = getQuestTimer("ai_magic_sup", null, null);
            if (magicSupAi != null)
            {
                magicSupAi.cancel();
            }

            QuestTimer end_event = getQuestTimer("end_event", null, null);
            if (end_event != null)
            {
                end_event.cancel();
            }
        }
        else if (event.equalsIgnoreCase("kain_ai"))
        {
            _warriorKain.setIsRunning(true);
            _warriorKain.setIsInvul(true);
            ((L2GuardInstance) _warriorKain).setCanReturnToSpawnPoint(false);

            _warriorKain.setTarget(_bossMakkum);
            ((L2GuardInstance) _warriorKain).addDamageHate(_bossMakkum, 500, 9999);
            _warriorKain.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _bossMakkum);
        }
        else if (event.equalsIgnoreCase("leona_ai"))
        {
            _warriorLeona.setIsRunning(true);
            _warriorLeona.setIsInvul(true);
            ((L2GuardInstance) _warriorLeona).setCanReturnToSpawnPoint(false);

            _warriorLeona.setTarget(_bossMakkum);
            ((L2GuardInstance) _warriorLeona).addDamageHate(_bossMakkum, 500, 9999);
            _warriorLeona.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _bossMakkum);
        }
        else if (event.equalsIgnoreCase("stone_ai"))
        {
            /**
             * Protection Stone AI
             *
             * This NPC will cast non-stop one buff to all the players that are inside his activity radius.
             */
            Collection<L2Character> chars = _protectionStone.getKnownList()
                    .getKnownCharactersInRadius(_protectionSkill.getSkillRadius());

            if (chars != null && !chars.isEmpty())
            {
                for (L2Character chara : chars)
                {
                    if (chara == null)
                    {
                        continue;
                    }

                    if (chara.isInsideRadius(_protectionStone, _protectionSkill.getSkillRadius(), false, false))
                    {
                        if (chara instanceof L2Playable)
                        {
                            _protectionSkill.getEffects(chara, chara);
                        }
                        else
                        {
                            if (_eventRound >= 17)
                            {
                                _weakMoment.getEffects(_protectionStone, chara);
                            }
                        }
                    }
                }
            }
        }
        else if (event.equalsIgnoreCase("tree_ai"))
        {
            /**
             * Tree Support AI
             *
             * This NPC will cast non-stop a heal skill to all players around
             */
            if (_summonTreeHelper != null && !_summonTreeHelper.isDecayed())
            {
                Collection<L2Character> chars = _summonTreeHelper.getKnownList()
                        .getKnownCharactersInRadius(_blessingOfTree.getSkillRadius());

                if (chars != null && !chars.isEmpty())
                {
                    for (L2Character chara : chars)
                    {
                        if (chara == null || !chara.isInsideRadius(_summonTreeHelper, _blessingOfTree
                                .getSkillRadius(), false, false) || !(chara instanceof L2Playable))
                        {
                            continue;
                        }

                        _blessingOfTree.getEffects(_summonTreeHelper, chara);
                    }
                }
            }
        }
        else if (event.equalsIgnoreCase("gravity_core_ai"))
        {
            /**
             * Gravity Core Shield Support AI
             *
             * This NPC will cast non-stop a UD skill to all players inside
             */
            if (_summonGravityCore != null && !_summonGravityCore.isDecayed())
            {
                Collection<L2Character> chars = _summonGravityCore.getKnownList()
                        .getKnownCharactersInRadius(_ultimateDef.getSkillRadius());

                if (chars != null && !chars.isEmpty())
                {
                    for (L2Character chara : chars)
                    {
                        if (chara == null || !chara.isInsideRadius(_summonGravityCore, _ultimateDef
                                .getSkillRadius(), false, false) || !(chara instanceof L2Playable))
                        {
                            continue;
                        }

                        _ultimateDef.getEffects(_summonGravityCore, chara);
                    }
                }
            }
        }
        else if (event.equalsIgnoreCase("guard_army_ai"))
        {
            /**
             * Guard Army
             *
             * We will just start the attack to the boss
             */
            for (L2Npc guard : _guardArmy)
            {
                if (guard == null)
                {
                    continue;
                }

                guard.setIsRunning(true);
                guard.setIsInvul(true);
                ((L2GuardInstance) guard).setCanReturnToSpawnPoint(false);

                guard.setTarget(_bossMakkum);
                ((L2GuardInstance) guard).addDamageHate(_bossMakkum, 500, 9999);
                guard.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _bossMakkum);
            }
        }
        else if (event.equalsIgnoreCase("ai_magic_sup"))
        {
            /**
             * Magic Support AI
             *
             * This NPC will give some OP buffs to the players and depends on the amount of players without full hp will:
             * 		- Summon a Tree of Life which will restore players HP
             * 		- Cast a Heal skills to a single target
             * 		- Summon a protective shield that will full-protect all the players inside
             * 		- Res dead players that are inside the ress cast skill range
             */
            final Collection<L2PcInstance> chars = _warriorMageSup.getKnownList().getKnownPlayersInRadius(1600);

            if (chars != null && !chars.isEmpty())
            {
                if (_warriorMageSup.getCurrentMp() < 800)
                {
                    _warriorMageSup.broadcastPacket(new CreatureSay(_warriorMageSup.getObjectId(), 1, _warriorMageSup
                            .getName(), "My mana power are decreasing so fast...!"));
                }

                List<L2PcInstance> fuckedPlayers = new ArrayList<L2PcInstance>();
                List<L2PcInstance> deadPlayers = new ArrayList<L2PcInstance>();
                for (L2PcInstance chara : chars)
                {
                    if (chara == null)
                    {
                        continue;
                    }

                    if (chara.isDead() && _warriorMageSup.isInsideRadius(chara, _resSkill.getCastRange(), false, false))
                    {
                        deadPlayers.add(chara);
                    }
                    else if (chara.getCurrentHp() < chara.getMaxHp() * 0.80)
                    {
                        fuckedPlayers.add(chara);
                    }
                }

                int fuckedCount = fuckedPlayers.size();
                int nextActionTime = 10000; //10sec
                if (deadPlayers.size() > 0)
                {
                    final L2PcInstance target = deadPlayers.get(Rnd.get(deadPlayers.size() - 1));
                    if (target != null && target.isDead() && _warriorMageSup
                            .isInsideRadius(target, _resSkill.getCastRange(), false, false))
                    {
                        _warriorMageSup
                                .broadcastPacket(new CreatureSay(_warriorMageSup.getObjectId(), 1, _warriorMageSup
                                        .getName(), target.getName() + " I'll resurrect you!"));

                        _warriorMageSup.setTarget(target);
                        _warriorMageSup.doCast(_resSkill);

                        nextActionTime += _resSkill.getHitTime() + 2000;
                    }
                }
                else if (fuckedCount > 0 && fuckedCount <= 5) //HEAL
                {
                    L2PcInstance target = fuckedPlayers.get(Rnd.get(fuckedPlayers.size() - 1));

                    if (target != null && _warriorMageSup.getCurrentMp() >= _healSkill.getMpConsume())
                    {
                        _warriorMageSup
                                .broadcastPacket(new CreatureSay(_warriorMageSup.getObjectId(), 1, _warriorMageSup
                                        .getName(), target.getName() + " let me give you a hand!"));

                        _warriorMageSup.setTarget(target);
                        _warriorMageSup.doCast(_healSkill);

                        nextActionTime += _healSkill.getHitTime() + 3000;
                    }
                }
                else if (fuckedCount > 5 && fuckedCount <= 10) //Summon Tree of Life
                {
                    if (_summonTreeHelper == null || _summonTreeHelper.isDecayed() && _warriorMageSup
                            .getCurrentMp() >= _summonTree.getMpConsume()) //Be sure we don't spawn more than one
                    {
                        _warriorMageSup
                                .broadcastPacket(new CreatureSay(_warriorMageSup.getObjectId(), 1, _warriorMageSup
                                        .getName(), "Ahhgrr! This will help us!"));

                        _warriorMageSup.setTarget(_warriorMageSup);
                        _warriorMageSup.doCast(_summonTree);

                        ThreadPoolManager.getInstance().scheduleAi(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                _summonTreeHelper = addSpawn(_summonTreeId, _warriorMageSup.getX(), _warriorMageSup
                                        .getY(), _warriorMageSup.getZ() + 20, 0, true, 30000, true, _instanceId);

                                //We will start that task only one time, then will be running all time while a tree is spawned
                                QuestTimer treeAi = getQuestTimer("tree_ai", null, null);
                                if (treeAi == null)
                                {
                                    startQuestTimer("tree_ai", 4000, null, null, true);
                                }
                            }
                        }, _summonTree.getHitTime() + 1000);

                        nextActionTime += _summonTree.getHitTime() + 5000;
                    }
                }
                else if (fuckedCount > 10) //Protective Gravity Core
                {
                    if (_summonGravityCore == null || _summonGravityCore.isDecayed() && _warriorMageSup
                            .getCurrentMp() >= _summonTree.getMpConsume()) //Be sure we don't spawn more than one
                    {
                        _warriorMageSup
                                .broadcastPacket(new CreatureSay(_warriorMageSup.getObjectId(), 1, _warriorMageSup
                                        .getName(),
                                        "Desperate situations need desperate measures! Come all! Enter enter into that shield!"));

                        _warriorMageSup.setTarget(_warriorMageSup);
                        _warriorMageSup.doCast(_summonCore);

                        ThreadPoolManager.getInstance().scheduleAi(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                _summonGravityCore = addSpawn(_gravityCoreId, _warriorMageSup.getX(), _warriorMageSup
                                        .getY(), _warriorMageSup.getZ() + 20, 0, false, 20000, true, _instanceId);

                                //We will start that task only one time, then will be running all time while a tree is spawned
                                QuestTimer treeAi = getQuestTimer("gravity_core_ai", null, null);
                                if (treeAi == null)
                                {
                                    startQuestTimer("gravity_core_ai", 1000, null, null, true);
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
                    if (_bossMakkum.getCurrentHp() < _bossMakkum.getMaxHp() * 0.50)
                    {
                        buffLevel = 1;
                    }
                    else if (_bossMakkum.getCurrentHp() < _bossMakkum.getMaxHp() * 0.30)
                    {
                        buffLevel = 2;
                    }
                    else if (_bossMakkum.getCurrentHp() < _bossMakkum.getMaxHp() * 0.10)
                    {
                        buffLevel = 3;
                    }

                    final L2Skill buffSkill = SkillTable.getInstance()
                            .getInfo(_fullBuffsIds[Rnd.get(_fullBuffsIds.length)] + buffLevel, 1);
                    if (buffSkill != null && _warriorMageSup.getCurrentMp() >= buffSkill.getMpConsume())
                    {
                        String skillType = buffSkill.getName().split(" ")[2];

                        _warriorMageSup
                                .broadcastPacket(new CreatureSay(_warriorMageSup.getObjectId(), 1, _warriorMageSup
                                        .getName(),
                                        skillType + "s! Come close to me to receive the power of " + skillType +
                                                "s God!"));

                        _warriorMageSup.setTarget(_warriorMageSup);
                        _warriorMageSup.doCast(_buffPresentation);

                        //Cast the buff and delay the next task
                        ThreadPoolManager.getInstance().scheduleAi(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                for (L2PcInstance chara : chars)
                                {
                                    if (chara == null || !chara.isInsideRadius(_warriorMageSup, 150, false, false))
                                    {
                                        continue;
                                    }

                                    buffSkill.getEffects(_warriorMageSup, chara);
                                }
                            }
                        }, _buffPresentation.getHitTime() + 700);

                        nextActionTime += buffSkill.getHitTime() + 9000;
                    }
                }
                startQuestTimer("ai_magic_sup", nextActionTime, null, null);
            }
        }
        else if (event.equalsIgnoreCase("stage_0_prepare_event"))
        {
            InstanceWorld world = null;

            _instanceId = InstanceManager.getInstance().createDynamicInstance(_qn + ".xml");

            world = new InstanceWorld();

            world.instanceId = _instanceId;

            //world.templateId = instanceTemplateId;

            InstanceManager.getInstance().addWorld(world);

            InstanceManager.getInstance().getInstance(_instanceId).setPvPInstance(false);

            InstanceManager.getInstance().getInstance(_instanceId).setPeaceInstance(true);

            Announcements.getInstance()
                    .announceToAll(
                            "The global Fearon Siege Instance now is available through Kain Van Halter located in Giran!");

            Announcements.getInstance().announceToAll("The instance will start on 10 minutes!");

            _protectionStone =
                    addSpawn(_protectionStoneId, -79660, 244954, -3651 + 20, 0, false, 0, false, _instanceId);

            startQuestTimer("stone_ai", 3000, null, null, true);

            startQuestTimer("stage_1_start_event", _debug ? 60000 : 10 * 60000, null, null);
        }
        else if (event.equalsIgnoreCase("stage_1_start_event"))
        {
            InstanceManager.getInstance()
                    .sendPacket(_instanceId, new ExShowScreenMessage("Stay close to the Protection Stone!", 5000));

            InstanceManager.getInstance().sendPacket(_instanceId, new Earthquake(-79660, 244954, -3651, 1, 10));

            for (int i = 0; i < 61; i++)
            {
                int x = (int) (1200 * Math.cos(i * 0.618));
                int y = (int) (1200 * Math.sin(i * 0.618));

                addSpawn(_dummyInvaderDoor, -79660 + x, 244954 + y, -3651 + 20, -1, false, 0, true, _instanceId);
            }

            startQuestTimer("stage_all_spawn_round", 60000, null, null);
        }
        else if (event.equalsIgnoreCase("stage_all_spawn_round"))
        {
            _eventRound++;

            InstanceManager.getInstance()
                    .sendPacket(_instanceId, new Earthquake(153581, 142081, -12741, _eventRound, 10));

            InstanceManager.getInstance()
                    .sendPacket(_instanceId,
                            new ExShowScreenMessage(1, 0, 2, 0, 0, 0, 0, true, 5 * 1000, 0, "Round: " + _eventRound));

            if (_eventRound == 17)
            {
                InstanceManager.getInstance()
                        .sendDelayedPacketToInstance(_instanceId, 6,
                                new ExShowScreenMessage(1, 0, 2, 0, 0, 0, 0, true, 5 * 1000, 0,
                                        "The Protection Stone now can debuff the enemies!"));
            }

            if (_eventRound == 15)
            {
                InstanceManager.getInstance()
                        .sendDelayedPacketToInstance(_instanceId, 6,
                                new ExShowScreenMessage(1, 0, 2, 0, 0, 0, 0, true, 5 * 1000, 0,
                                        "The entrance to the instance is now closed!"));
            }

            L2Skill passiveSkill = SkillTable.getInstance()
                    .getInfo(_passiveSkillId, _eventRound < 20 ? _eventRound : 10);

            if (_eventRound < 20)
            {
                for (int i = 0; i < 61; i++)
                {
                    int x = (int) (1200 * Math.cos(i * 0.618));
                    int y = (int) (1200 * Math.sin(i * 0.618));

                    L2Npc minion = addSpawn(_invadeMobs[Rnd
                                    .get(_invadeMobs.length)], -79660 + x, 244954 + y, -3651 + 20, -1, false, 0, true,
                            _instanceId);
                    minion.setIsRunning(true);
                    minion.addSkill(passiveSkill);
                    minion.setCurrentHpMp(minion.getMaxHp(), minion.getMaxMp());

                    synchronized (_allMinions)
                    {
                        _allMinions.add(minion);
                    }
                }
            }
            else if (_eventRound == 20)
            {
                //BossTime
                _bossMakkum = addSpawn(_makkumBossId, -80015, 244904, -3677, 917, false, 0, true, _instanceId);

                _bossMakkum.addSkill(passiveSkill);
                _bossMakkum.setCurrentHpMp(_bossMakkum.getMaxHp(), _bossMakkum.getMaxMp());

                InstanceManager.getInstance()
                        .sendPacket(_instanceId, new CreatureSay(_bossMakkum.getObjectId(), 1, _bossMakkum
                                .getName(), "It's the time to end with your lives!"));
            }
        }

        return "";
    }

    @Override
    public String onSkillSee(L2Npc npc, L2PcInstance player, L2Skill skill, L2Object[] targets, boolean isPet)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onSkillSee: " + npc.getName());
        }

        //Register indirectly attackers (Healers and Supporters)
        L2Party party = player.getParty();
        if (party != null)
        {
            if (skill.getTargetType() != L2SkillTargetType.TARGET_SELF && (skill.getSkillType().toString()
                    .contains("HEAL") || skill.getSkillType().toString().contains("BUFF")))
            {
                synchronized (_attackerParty)
                {
                    Map<L2Party, Long> registredAttacks = _attackerParty.get(npc.getObjectId());
                    if (registredAttacks != null) //Only if its already registered
                    {
                        if (registredAttacks.containsKey(party))
                        {
                            long newDamage = registredAttacks.get(party);
                            newDamage += skill.getPower();
                            newDamage += skill.getAggroPoints();

                            registredAttacks.put(party, newDamage);
                        }
                        else
                        {
                            long newDamage = 0;
                            newDamage += skill.getPower();
                            newDamage += skill.getAggroPoints();
                            registredAttacks.put(party, newDamage);
                        }
                        _attackerParty.put(npc.getObjectId(), registredAttacks);
                    }
                }
            }
        }

        return super.onSkillSee(npc, player, skill, targets, isPet);
    }

    private static void rewardDetectedAttackers(L2Npc npc)
    {
        if (!_isEventOn || npc.isMinion())
        {
            return;
        }

        if (!npc.isInsideRadius(_protectionStone, 2000, false, false))
        {
            if (_debug)
            {
                Log.warning(_qn + ": Npc: " + npc.getName() + " wont give reward because it's out of the limit!");
            }
            return;
        }

        Map<Integer, Integer> _possibleRewards = _rewardList.get(_eventRound);
        if (_possibleRewards == null)
        {
            return;
        }

		/*if (npc.getNpcId() == _makkumBossId)
		{
			for (int a = 0; a < 3; a++)
			{
				for (Entry<Integer, Integer> i : _possibleRewards.entrySet())
				{
					if (i == null)
						continue;

					int rewardId = i.getKey();
					int count = Rnd.get(i.getValue()) + 1;

					L2ItemInstance dropItem = new L2ItemInstance(IdFactory.getInstance().getNextId(), rewardId);
					if (dropItem != null)
					{
						dropItem.dropMe(npc, npc.getX() + Rnd.get(100), npc.getY() + Rnd.get(100), npc.getZ() + 20);

						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), " drop: " + ItemTable.getInstance().getTemplate(rewardId).getName() + "("+count+")"));

						Log.warning(_qn + ": Npc: " + npc.getName() + " drops: " + ItemTable.getInstance().getTemplate(rewardId).getName() +"("+count+")");

						synchronized(_rewardedInfo)
						{
							Long rewardedAmount = _rewardedInfo.get(rewardId);
							if (rewardedAmount != null)
							{
								rewardedAmount += count;
								_rewardedInfo.put(rewardId, rewardedAmount);
							}
							else
								_rewardedInfo.put(rewardId, (long)count);
						}
					}
				}
			}
		}
		else
		{
			synchronized(_attackerParty)
			{
				Map<L2Party, Long> registredAttacks = _attackerParty.get(npc.getObjectId());
				if (registredAttacks != null)
				{
					long mostDamage = 0;
					L2Party rewardParty = null;
					for (Entry<L2Party, Long> i : registredAttacks.entrySet())
					{
						if (i == null)
							continue;

						if (i.getValue() > mostDamage)
							rewardParty = i.getKey();
					}

					if (rewardParty != null)
					{
						for (L2PcInstance partyMember : rewardParty.getPartyMembers())
						{
							if (partyMember == null || partyMember.getInstanceId() != _instanceId || !partyMember.isInsideRadius(_protectionStone, 2000, false, false))
								continue;

							Integer rndRewardId = (Integer) _possibleRewards.keySet().toArray()[Rnd.nextInt(_possibleRewards.size())];
							int count = Rnd.get(_possibleRewards.get(rndRewardId)) + 1;
							partyMember.addItem(_qn, rndRewardId, count, npc, true);

							//Log.warning(_qn + ": player: " + partyMember.getName() + " rewarded with " + ItemTable.getInstance().getTemplate(rndRewardId).getName() +"("+count+")");

							synchronized(_rewardedInfo)
							{
								Long rewardedAmount = _rewardedInfo.get(rndRewardId);
								if (rewardedAmount != null)
								{
									rewardedAmount += count;
									_rewardedInfo.put(rndRewardId, rewardedAmount);
								}
								else
									_rewardedInfo.put(rndRewardId, (long)count);
							}
						}
					}
				}
			}
		}*/
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onKill: " + npc.getName());
        }

        rewardDetectedAttackers(npc);

        if (Util.contains(_invadeMobs, npc.getNpcId()))
        {
            synchronized (_allMinions)
            {
                if (_allMinions.contains(npc))
                {
                    _allMinions.remove(npc);

                    if (_allMinions.isEmpty())
                    {
                        InstanceManager.getInstance()
                                .sendPacket(_instanceId, new ExShowScreenMessage(1, 0, 2, 0, 0, 0, 0, true, 5 * 1000, 0,
                                        "Next round will start in 15 seconds!"));

                        startQuestTimer("stage_all_spawn_round", 15000, null, null);
                    }
                }
            }
        }
        else if (npc.getNpcId() == _makkumBossId)
        {
            if (_eventStatus == 4)
            {
                _eventStatus = 5;

                notifyEvent("cancel_timers", null, null);

                InstanceManager.getInstance().finishInstance(_instanceId, false);

                startQuestTimer("restart_variables", 300000, null, null);

                //Dump rewards
                for (Entry<Integer, Long> i : _rewardedInfo.entrySet())
                {
                    if (i == null)
                    {
                        continue;
                    }

                    Log.warning(_qn + ": Rewarded in total :" + ItemTable.getInstance().getTemplate(i.getKey())
                            .getName() + "(" + i.getValue() + ")");
                }
            }
        }

        return super.onKill(npc, player, isPet);
    }

    @Override
    public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onAttack: " + npc.getName());
        }

        L2Party party = attacker.getParty();
        if (party != null)
        {
            synchronized (_attackerParty)
            {
                Map<L2Party, Long> registredAttacks = _attackerParty.get(npc.getObjectId());
                if (registredAttacks == null)
                {
                    Map<L2Party, Long> partyAttack = new HashMap<L2Party, Long>();
                    partyAttack.put(party, (long) damage);
                    _attackerParty.put(npc.getObjectId(), partyAttack);
                }
                else
                {
                    if (registredAttacks.containsKey(party))
                    {
                        long newDamage = registredAttacks.get(party);
                        registredAttacks.put(party, newDamage + damage);
                    }
                    else
                    {
                        registredAttacks.put(party, (long) damage);
                    }
                }
            }
        }

        if (npc.getNpcId() == _makkumBossId)
        {
            if (_eventStatus == 1 && npc.getCurrentHp() < npc.getMaxHp() * 0.90)
            {
                _eventStatus = 2;

                InstanceManager.getInstance().stopWholeInstance(_instanceId);

                InstanceManager.getInstance()
                        .sendPacket(_instanceId, new Earthquake(153581, 142081, -12741, _eventRound, 10));

                InstanceManager.getInstance().sendPacket(_instanceId, new SpecialCamera(_protectionStone
                        .getObjectId(), 1000, 0, 150, 0, 16000));

                InstanceManager.getInstance().sendPacket(_instanceId, new MagicSkillUse(_protectionStone, _portalEffect1
                        .getId(), 1, _portalEffect1.getHitTime(), _portalEffect1.getReuseDelay()));

                ThreadPoolManager.getInstance().scheduleAi(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        InstanceManager.getInstance()
                                .sendPacket(_instanceId, new MagicSkillUse(_protectionStone, _portalEffect2
                                        .getId(), 1, _portalEffect2.getHitTime(), _portalEffect2.getReuseDelay()));

                        for (int i = 0; i < 15; i++)
                        {
                            L2Npc guard = addSpawn(_warriorGuard, _protectionStone.getX(), _protectionStone
                                    .getY(), _protectionStone.getZ(), 0, true, 0, true, _instanceId);
                            _guardArmy.add(guard);

                            guard.broadcastPacket(new MagicSkillUse(guard, _warriorsSpawnEffect
                                    .getId(), 1, _warriorsSpawnEffect.getHitTime(), _warriorsSpawnEffect
                                    .getReuseDelay()));
                        }

                        ThreadPoolManager.getInstance().scheduleAi(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                InstanceManager.getInstance().startWholeInstance(_instanceId);

                                InstanceManager.getInstance()
                                        .sendDelayedPacketToInstance(_instanceId, 8,
                                                new ExShowScreenMessage(1, 0, 2, 0, 0, 0, 0, true, 5 * 1000, 0,
                                                        "The Royal Army Guards from the Aden realm has been arrived!"));

                                notifyEvent("guard_army_ai", null, null);
                            }
                        }, 4000);
                    }
                }, 12000);
            }
            else if (_eventStatus == 2 && npc.getCurrentHp() < npc.getMaxHp() * 0.50)
            {
                _eventStatus = 3;

                L2Skill passiveSkill = SkillTable.getInstance().getInfo(_passiveSkillId, 15);
                npc.addSkill(passiveSkill);

                //Supporters
                InstanceManager.getInstance().stopWholeInstance(_instanceId);

                //Some cosmetics
                InstanceManager.getInstance()
                        .sendPacket(_instanceId, new Earthquake(153581, 142081, -12741, _eventRound, 10));

                //SpecialCamera
                InstanceManager.getInstance().sendPacket(_instanceId, new SpecialCamera(_protectionStone
                        .getObjectId(), 1000, 0, 150, 0, 16000));

                //Portal Effects
                InstanceManager.getInstance().sendPacket(_instanceId, new MagicSkillUse(_protectionStone, _portalEffect1
                        .getId(), 1, _portalEffect1.getHitTime(), _portalEffect1.getReuseDelay()));

                ThreadPoolManager.getInstance().scheduleAi(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        InstanceManager.getInstance()
                                .sendPacket(_instanceId, new MagicSkillUse(_protectionStone, _portalEffect2
                                        .getId(), 1, _portalEffect2.getHitTime(), _portalEffect2.getReuseDelay()));

                        _warriorLeona = addSpawn(_warriorLeonaId, _protectionStone.getX(), _protectionStone
                                .getY(), _protectionStone.getZ() + 20, 0, true, 0, true, _instanceId);
                        _warriorKain = addSpawn(_warriorKainId, _protectionStone.getX(), _protectionStone
                                .getY(), _protectionStone.getZ() + 20, 0, true, 0, true, _instanceId);
                        _warriorMageSup = addSpawn(_warriorMageSUpId, _protectionStone.getX(), _protectionStone
                                .getY(), _protectionStone.getZ() + 20, 0, true, 0, true, _instanceId);

                        _warriorLeona.broadcastPacket(new MagicSkillUse(_warriorLeona, _warriorsSpawnEffect
                                .getId(), 1, _warriorsSpawnEffect.getHitTime(), _warriorsSpawnEffect.getReuseDelay()));
                        _warriorKain.broadcastPacket(new MagicSkillUse(_warriorKain, _warriorsSpawnEffect
                                .getId(), 1, _warriorsSpawnEffect.getHitTime(), _warriorsSpawnEffect.getReuseDelay()));
                        _warriorMageSup.broadcastPacket(new MagicSkillUse(_warriorMageSup, _warriorsSpawnEffect
                                .getId(), 1, _warriorsSpawnEffect.getHitTime(), _warriorsSpawnEffect.getReuseDelay()));

                        //Start It back
                        ThreadPoolManager.getInstance().scheduleAi(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                InstanceManager.getInstance().startWholeInstance(_instanceId);

                                notifyEvent("ai_magic_sup", null, null);
                                notifyEvent("kain_ai", null, null);
                                notifyEvent("leona_ai", null, null);

                                notifyEvent("guard_army_ai", null, null);

                                InstanceManager.getInstance()
                                        .sendDelayedPacketToInstance(_instanceId, 3,
                                                new ExShowScreenMessage(1, 0, 2, 0, 0, 0, 0, true, 5 * 1000, 0,
                                                        "The Royal Army Captains from the Aden realm has been arrived!"));
                            }
                        }, 4000);
                    }
                }, 12000);
            }
            else if (_eventStatus == 3 && npc.getCurrentHp() < npc.getMaxHp() * 0.10)
            {
                _eventStatus = 4;

                L2Skill passiveSkill = SkillTable.getInstance().getInfo(_passiveSkillId, 20);
                npc.addSkill(passiveSkill);
            }
        }
        return super.onAttack(npc, attacker, damage, isPet, skill);
    }

    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player)
    {
        if (_isEventOn)
        {
            if (npc.getNpcId() == _dummyKainVanHalter)
            {
                if (player.isGM())
                {
                    return "gmEventPanel.html";
                }
                return "DummyKain.html";
            }
        }
        return super.onFirstTalk(npc, player);
    }

    @Override
    public int getOnKillDelay(int npcId)
    {
        return 0;
    }

    public static void main(String[] args)
    {
        new FearonSiege(-1, _qn, "custom");
    }
}
