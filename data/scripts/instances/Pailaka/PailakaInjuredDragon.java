package instances.Pailaka;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import l2server.gameserver.ai.CtrlEvent;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.quest.State;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SpecialCamera;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

public class PailakaInjuredDragon extends Quest
{
    private static final String qn = "144_PailakaInjuredDragon";

    private static final int MIN_LEVEL = 73;
    private static final int MAX_LEVEL = 77;
    private static final int MAX_SUMMON_LEVEL = 80;
    private static final int EXIT_TIME = 5;
    private static final int INSTANCE_ID = 45;
    private static final int[] TELEPORT = {125757, -40928, -3736};

    // NO EXIT ZONES
    private static final Map<Integer, int[]> NOEXIT_ZONES = new HashMap<Integer, int[]>();

    static
    {
        NOEXIT_ZONES.put(200001, new int[]{123167, -45743, -3023});
        NOEXIT_ZONES.put(200002, new int[]{117783, -46398, -2560});
        NOEXIT_ZONES.put(200003, new int[]{116791, -51556, -2584});
        NOEXIT_ZONES.put(200004, new int[]{117993, -52505, -2480});
        NOEXIT_ZONES.put(200005, new int[]{113226, -44080, -2776});
        NOEXIT_ZONES.put(200006, new int[]{107916, -46716, -2008});
        NOEXIT_ZONES.put(200007, new int[]{118341, -55951, -2280});
        NOEXIT_ZONES.put(200008, new int[]{110127, -41562, -2332});
    }

    ;

    // NPCS
    private static final int KETRA_ORC_SHAMAN = 32499;
    private static final int KETRA_ORC_SUPPORTER = 32502;
    private static final int KETRA_ORC_SUPPORTER2 = 32512;
    private static final int KETRA_ORC_INTELIGENCE_OFFICER = 32509;

    // WALL MOBS
    private static final int VARKA_SILENOS_RECRUIT = 18635;
    private static final int VARKA_SILENOS_FOOTMAN = 18636;
    private static final int VARKA_SILENOS_WARRIOR = 18642;
    private static final int VARKA_SILENOS_OFFICER = 18646;
    private static final int VARKAS_COMMANDER = 18654;
    private static final int VARKA_ELITE_GUARD = 18653;
    private static final int VARKA_SILENOS_GREAT_MAGUS = 18649;
    private static final int VARKA_SILENOS_GENERAL = 18650;
    private static final int VARKA_SILENOS_HEAD_GUARD = 18655;
    private static final int PROPHET_GUARD = 18657;
    private static final int VARKAS_PROPHET = 18659;

    // EXTRA WALL SILENOS
    private static final int VARKA_SILENOS_MEDIUM = 18644;
    private static final int VARKA_SILENOS_PRIEST = 18641;
    private static final int VARKA_SILENOS_SHAMAN = 18640;
    private static final int VARKA_SILENOS_SEER = 18648;
    private static final int VARKA_SILENOS_MAGNUS = 18645;
    private static final int DISCIPLE_OF_PROPHET = 18658;
    private static final int VARKA_HEAD_MAGUS = 18656;
    private static final int VARKA_SILENOS_GREAT_SEER = 18652;

    // NORMAL MOBS
    private static final int ANTYLOPE_1 = 18637;
    private static final int ANTYLOPE_2 = 18643;
    private static final int ANTYLOPE_3 = 18651;
    private static final int FLAVA = 18647;

    // BOSS
    private static final int LATANA = 18660;

    // ITEMS
    private static final int SPEAR = 13052;
    private static final int ENCHSPEAR = 13053;
    private static final int LASTSPEAR = 13054;
    private static final int STAGE1 = 13056;
    private static final int STAGE2 = 13057;

    private static final int SHIELD_POTION = 13032;
    private static final int HEAL_POTION = 13033;

    // Rewards
    private static final int PSHIRT = 13296;
    private static final int SCROLL_OF_ESCAPE = 736;

    private static int buff_counter = 5;
    private static boolean _hasDoneAnimation = false;

    // Arrays
    private static final int[] NPCS =
            {KETRA_ORC_SHAMAN, KETRA_ORC_SUPPORTER, KETRA_ORC_INTELIGENCE_OFFICER, KETRA_ORC_SUPPORTER2};

    private static final int[] WALL_MONSTERS = {
            // 1st Row Mobs
            VARKA_SILENOS_FOOTMAN,
            VARKA_SILENOS_WARRIOR,
            VARKA_SILENOS_OFFICER,
            VARKAS_COMMANDER,
            VARKA_SILENOS_RECRUIT,
            PROPHET_GUARD,
            VARKA_ELITE_GUARD,
            VARKA_SILENOS_GREAT_MAGUS,
            VARKA_SILENOS_GENERAL,
            VARKA_SILENOS_HEAD_GUARD,
            PROPHET_GUARD,
            VARKAS_PROPHET,

            // 2nd Row Mobs
            DISCIPLE_OF_PROPHET,
            VARKA_HEAD_MAGUS,
            VARKA_SILENOS_GREAT_SEER,
            VARKA_SILENOS_SHAMAN,
            VARKA_SILENOS_MAGNUS,
            VARKA_SILENOS_SEER,
            VARKA_SILENOS_MEDIUM,
            VARKA_SILENOS_PRIEST
    };

    private static final int[] OTHER_MONSTERS = {ANTYLOPE_1, ANTYLOPE_2, ANTYLOPE_3, FLAVA};

    private static final int[] ITEMS = {SPEAR, ENCHSPEAR, LASTSPEAR, STAGE1, STAGE2, SHIELD_POTION, HEAL_POTION};

    private static final int[][] BUFFS = {
            {4357, 2}, // Haste Lv2
            {4342, 2}, // Wind Walk Lv2
            {4356, 3}, // Empower Lv3
            {4355, 3}, // Acumen Lv3
            {4351, 6}, // Concentration Lv6
            {4345, 3}, // Might Lv3
            {4358, 3}, // Guidance Lv3
            {4359, 3}, // Focus Lv3
            {4360, 3}, // Death Wisper Lv3
            {4352, 2}, // Berserker Spirit Lv2
            {4354, 4}, // Vampiric Rage Lv4
            {4347, 6} // Blessed Body Lv6
    };

    private static final ArrayList<PailakaDrop> DROPLIST = new ArrayList<PailakaDrop>();

    static
    {
        DROPLIST.add(new PailakaDrop(HEAL_POTION, 80));
        DROPLIST.add(new PailakaDrop(SHIELD_POTION, 30));
    }

    ;

    private static final int[][] HP_HERBS_DROPLIST = {
            // itemId, count, chance
            {8601, 1, 40}, {8600, 1, 70}
    };

    private static final int[][] MP_HERBS_DROPLIST = {
            // itemId, count, chance
            {8604, 1, 40}, {8603, 1, 70}
    };

    private static final void dropHerb(L2Npc mob, L2PcInstance player, int[][] drop)
    {
        final int chance = Rnd.get(100);
        for (int[] element : drop)
        {
            if (chance < element[2])
            {
                ((L2MonsterInstance) mob).dropItem(player, element[0], element[1]);
                return;
            }
        }
    }

    private static final void dropItem(L2Npc mob, L2PcInstance player)
    {
        // To make random drops, we shuffle the droplist every time its used
        Collections.shuffle(DROPLIST);
        for (PailakaDrop pd : DROPLIST)
        {
            if (Rnd.get(100) < pd.getChance())
            {
                ((L2MonsterInstance) mob).dropItem(player, pd.getItemID(), Rnd.get(1, 6));
                return;
            }
        }
    }

    private static void giveBuff(L2Npc npc, L2PcInstance player, int skillId, int level)
    {
        npc.setTarget(player);
        npc.doCast(SkillTable.getInstance().getInfo(skillId, level));
        buff_counter--;
        return;
    }

    private static final void teleportPlayer(L2Playable player, int[] coords, int instanceId)
    {
        player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        player.setInstanceId(instanceId);
        player.teleToLocation(coords[0], coords[1], coords[2], true);
    }

    private final synchronized void enterInstance(L2PcInstance player, boolean isNewQuest)
    {
        // Check for existing instances for this player
        InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
        if (world != null)
        {
            if (world.templateId != INSTANCE_ID)
            {
                player.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
                return;
            }

            final Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
            if (inst != null)
            {
                //  - Check max summon levels
                checkMaxSummonLevel(player);
                teleportPlayer(player, TELEPORT, world.instanceId);
            }
        }
        else
        {
            if (!isNewQuest)
            {
                final QuestState st = player.getQuestState(qn);
                st.unset("cond");
                st.exitQuest(true);
                player.sendMessage("Your instance has ended so your quest has been canceled. Talk to me again");
                return;
            }

            final int instanceId = InstanceManager.getInstance().createDynamicInstance("PailakaInjuredDragon.xml");

            world = new InstanceWorld();
            world.instanceId = instanceId;
            world.templateId = INSTANCE_ID;
            InstanceManager.getInstance().addWorld(world);

            //  - Check max summon levels
            checkMaxSummonLevel(player);

            world.allowed.add(player.getObjectId());
            teleportPlayer(player, TELEPORT, instanceId);
        }
    }

    //  - Checks if the summon or pet that the player has can be used
    private final void checkMaxSummonLevel(L2PcInstance player)
    {
        final L2PetInstance pet = player.getPet();
        if (pet != null)
        {
            if (pet.getLevel() > MAX_SUMMON_LEVEL)
            {
                pet.unSummon(player);
            }
        }
    }

    @Override
    public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        final QuestState st = player.getQuestState(qn);
        if (st == null)
        {
            return getNoQuestMsg(player);
        }

        final int cond = st.getInt("cond");
        if (event.equalsIgnoreCase("enter"))
        {
            if (player.getLevel() < MIN_LEVEL)
            {
                return "32499-no.htm";
            }
            if (player.getLevel() > MAX_LEVEL)
            {
                return "32499-no.htm";
            }
            if (cond < 2)
            {
                return "32499-no.htm";
            }
            enterInstance(player, cond == 2);
            return null;
        }
        else if (event.equalsIgnoreCase("32499-02.htm")) // Shouldn't be 32499-04.htm ???
        {
            if (cond == 0)
            {
                st.set("cond", "1");
                st.setState(State.STARTED);
                st.playSound("ItemSound.quest_accept");
            }
        }
        else if (event.equalsIgnoreCase("32499-05.htm"))
        {
            if (cond == 1)
            {
                st.set("cond", "2");
                st.playSound("ItemSound.quest_accept"); // double quest accept ???
            }
        }
        else if (event.equalsIgnoreCase("32502-05.htm"))
        {
            if (cond == 2)
            {
                st.set("cond", "3");
                if (!st.hasQuestItems(SPEAR))
                {
                    st.giveItems(SPEAR, 1);
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        }
        else if (event.equalsIgnoreCase("32509-02.htm"))
        {
            switch (cond)
            {
                case 2:
                case 3:
                    return "32509-07.htm";
                case 4:
                    st.set("cond", "5");
                    st.takeItems(SPEAR, 1);
                    st.takeItems(STAGE1, 1);
                    st.giveItems(ENCHSPEAR, 1);
                    st.playSound("ItemSound.quest_itemget");
                    return "32509-02.htm";
                case 5:
                    return "32509-01.htm";
                case 6:
                    st.set("cond", "7");
                    st.takeItems(ENCHSPEAR, 1);
                    st.takeItems(STAGE2, 1);
                    st.giveItems(LASTSPEAR, 1);
                    st.playSound("ItemSound.quest_itemget");

                    // Spawns Latana
                    addSpawn(LATANA, 105732, -41787, -1782, 35742, false, 0, false, npc.getInstanceId());
                    return "32509-03.htm";
                case 7:
                    return "32509-03.htm";
                default:
                    break;
            }
        }
        else if (event.equalsIgnoreCase("32509-06.htm"))
        {
            if (buff_counter < 1)
            {
                return "32509-05.htm";
            }
        }
        else if (event.equalsIgnoreCase("32512-02.htm"))
        {
            st.unset("cond");
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);

            Instance inst = InstanceManager.getInstance().getInstance(npc.getInstanceId());
            inst.setDuration(EXIT_TIME * 60000);
            inst.setEmptyDestroyTime(0);

            //if (inst.containsPlayer(player.getObjectId()))
            {
                player.setVitalityPoints(20000, true);
                st.addExpAndSp(28000000, 2850000);
                st.giveItems(SCROLL_OF_ESCAPE, 1);
                st.giveItems(PSHIRT, 1);
            }
        }
        else if (event.startsWith("buff"))
        {
            if (buff_counter > 0)
            {
                final int nr = Integer.parseInt(event.split("buff")[1]);
                giveBuff(npc, player, BUFFS[nr - 1][0], BUFFS[nr - 1][1]);
                return "32509-06.htm";
            }
            else
            {
                return "32509-05.htm";
            }
        }
        else if (event.equalsIgnoreCase("latana_animation"))
        {
            _hasDoneAnimation = true;

            npc.abortAttack();
            npc.abortCast();
            npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
            player.abortAttack();
            player.abortCast();
            player.stopMove(null);
            player.setTarget(null);
            if (player.getPet() != null)
            {
                player.getPet().abortAttack();
                player.getPet().abortCast();
                player.getPet().stopMove(null);
                player.getPet().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
            }
            for (L2SummonInstance summon : player.getSummons())
            {
                summon.abortAttack();
                summon.abortCast();
                summon.stopMove(null);
                summon.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
            }

            player.sendPacket(new SpecialCamera(npc.getObjectId(), 200, 0, 0, 1000, 11000, 1, 0, 1, 0));
            startQuestTimer("latana_animation2", 1000, npc, player);
            return null;
        }
        else if (event.equalsIgnoreCase("latana_animation2"))
        {
            npc.doCast(SkillTable.getInstance().getInfo(5759, 1));
            npc.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player);
            return null;
        }
        return event;
    }

    @Override
    public final String onTalk(L2Npc npc, L2PcInstance player)
    {
        final QuestState st = player.getQuestState(qn);
        if (st == null)
        {
            return getNoQuestMsg(player);
        }

        final int cond = st.getInt("cond");
        switch (npc.getNpcId())
        {
            case KETRA_ORC_SHAMAN:
                switch (st.getState())
                {
                    case State.CREATED:
                        if (player.getLevel() < MIN_LEVEL)
                        {
                            return "32499-no.htm";
                        }
                        if (player.getLevel() > MAX_LEVEL)
                        {
                            return "32499-no.htm";
                        }
                        return "32499-00.htm";
                    case State.STARTED:
                        if (player.getLevel() < MIN_LEVEL)
                        {
                            return "32499-no.htm";
                        }
                        if (player.getLevel() > MAX_LEVEL)
                        {
                            return "32499-no.htm";
                        }
                        if (cond > 1)
                        {
                            return "32499-06.htm";
                        }
                    case State.COMPLETED:
                        return "32499-completed.htm";
                    default:
                        return "32499-no.htm";
                }
            case KETRA_ORC_SUPPORTER:
                if (cond > 2)
                {
                    return "32502-05.htm";
                }
                else
                {
                    return "32502-00.htm";
                }
            case KETRA_ORC_INTELIGENCE_OFFICER:
                return "32509-00.htm";
            case KETRA_ORC_SUPPORTER2:
                if (st.getState() == State.COMPLETED)
                {
                    return "32512-03.htm";
                }
                else if (cond == 8)
                {
                    return "32512-01.htm";
                }
        }

        return getNoQuestMsg(player);
    }

    @Override
    public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        QuestState st = player.getQuestState(qn);
        if (st == null || st.getState() != State.STARTED)
        {
            return null;
        }

		/* There are lots of mobs walls, and item get is random, it could happen that you dont get the item
         * until the last wall, and there's 4 different silenos groups. 1 enchant comes only from group 2
		 * and the 2nd comes from group 4. Chances, lets say 20% of getting the enchant when killing
		 * the right mob
		 * When you kill a mob wall, another mage type appears behind. If all mobs from the front are killed
		 * then the ones that are behind are despawned. Also this mobs should be damaged, like with 30% of
		 * max HP, because they should be easy to kill
		 */
        final int cond = st.getInt("cond");
        switch (npc.getNpcId())
        {
            case VARKA_SILENOS_FOOTMAN:
            case VARKA_SILENOS_RECRUIT:
                dropHerb(npc, player, HP_HERBS_DROPLIST);
                if (cond == 3 && st.hasQuestItems(SPEAR) && !st.hasQuestItems(STAGE1) && Rnd.get(100) < 5)
                {
                    st.set("cond", "4");
                    st.giveItems(STAGE1, 1);
                    st.playSound("ItemSound.quest_itemget");
                }

                // Spawns Mage Type silenos behind the one that was killed
                spawnMageBehind(npc, player, VARKA_SILENOS_MEDIUM);

                // Check if all the first row have been killed. Despawn mages
                checkIfLastInWall(npc);
                break;
            case VARKA_SILENOS_WARRIOR:
                dropHerb(npc, player, HP_HERBS_DROPLIST);
                if (cond == 3 && st.hasQuestItems(SPEAR) && !st.hasQuestItems(STAGE1) && Rnd.get(100) < 10)
                {
                    st.set("cond", "4");
                    st.giveItems(STAGE1, 1);
                    st.playSound("ItemSound.quest_itemget");
                }

                // Spawns Mage Type silenos behind the one that was killed
                spawnMageBehind(npc, player, VARKA_SILENOS_PRIEST);

                // Check if all the first row have been killed. Despawn mages
                checkIfLastInWall(npc);
                break;
            case VARKA_ELITE_GUARD:
                dropHerb(npc, player, HP_HERBS_DROPLIST);
                if (cond == 3 && st.hasQuestItems(SPEAR) && !st.hasQuestItems(STAGE1) && Rnd.get(100) < 15)
                {
                    st.set("cond", "4");
                    st.giveItems(STAGE1, 1);
                    st.playSound("ItemSound.quest_itemget");
                }

                // Spawns Mage Type silenos behind the one that was killed
                spawnMageBehind(npc, player, VARKA_SILENOS_SHAMAN);

                // Check if all the first row have been killed. Despawn mages
                checkIfLastInWall(npc);
                break;
            case VARKAS_COMMANDER:
            case VARKA_SILENOS_OFFICER:
                dropHerb(npc, player, HP_HERBS_DROPLIST);
                if (cond == 3 && st.hasQuestItems(SPEAR) && !st.hasQuestItems(STAGE1) && Rnd.get(100) < 25)
                {
                    st.set("cond", "4");
                    st.giveItems(STAGE1, 1);
                    st.playSound("ItemSound.quest_itemget");
                }

                // Spawns Mage Type silenos behind the one that was killed
                spawnMageBehind(npc, player, VARKA_SILENOS_SEER);

                // Check if all the first row have been killed. Despawn mages
                checkIfLastInWall(npc);
                break;
            case VARKA_SILENOS_GREAT_MAGUS:
            case VARKA_SILENOS_GENERAL:
                dropHerb(npc, player, HP_HERBS_DROPLIST);
                if (cond == 5 && st.hasQuestItems(ENCHSPEAR) && !st.hasQuestItems(STAGE2) && Rnd.get(100) < 5)
                {
                    st.set("cond", "6");
                    st.giveItems(STAGE2, 1);
                    st.playSound("ItemSound.quest_itemget");
                }

                // Spawns Mage Type silenos behind the one that was killed
                spawnMageBehind(npc, player, VARKA_SILENOS_MAGNUS);

                // Check if all the first row have been killed. Despawn mages
                checkIfLastInWall(npc);
                break;
            case VARKAS_PROPHET:
                dropHerb(npc, player, HP_HERBS_DROPLIST);
                if (cond == 5 && st.hasQuestItems(ENCHSPEAR) && !st.hasQuestItems(STAGE2) && Rnd.get(100) < 10)
                {
                    st.set("cond", "6");
                    st.giveItems(STAGE2, 1);
                    st.playSound("ItemSound.quest_itemget");
                }

                // Spawns Mage Type silenos behind the one that was killed
                spawnMageBehind(npc, player, DISCIPLE_OF_PROPHET);

                // Check if all the first row have been killed. Despawn mages
                checkIfLastInWall(npc);
                break;
            case VARKA_SILENOS_HEAD_GUARD:
                dropHerb(npc, player, HP_HERBS_DROPLIST);
                if (cond == 5 && st.hasQuestItems(ENCHSPEAR) && !st.hasQuestItems(STAGE2) && Rnd.get(100) < 20)
                {
                    st.set("cond", "6");
                    st.giveItems(STAGE2, 1);
                    st.playSound("ItemSound.quest_itemget");
                }

                // Spawns Mage Type silenos behind the one that was killed
                spawnMageBehind(npc, player, VARKA_HEAD_MAGUS);

                // Check if all the first row have been killed. Despawn mages
                checkIfLastInWall(npc);
                break;
            case PROPHET_GUARD:
                dropHerb(npc, player, HP_HERBS_DROPLIST);
                if (cond == 5 && st.hasQuestItems(ENCHSPEAR) && !st.hasQuestItems(STAGE2) && Rnd.get(100) < 25)
                {
                    st.set("cond", "6");
                    st.giveItems(STAGE2, 1);
                    st.playSound("ItemSound.quest_itemget");
                }

                // Spawns Mage Type silenos behind the one that was killed
                spawnMageBehind(npc, player, VARKA_SILENOS_GREAT_SEER);

                // Check if all the first row have been killed. Despawn mages
                checkIfLastInWall(npc);
                break;
            case LATANA:
                st.set("cond", "8");
                st.playSound("ItemSound.quest_middle");

                // Spawns Ketra Orc Supporter
                addSpawn(KETRA_ORC_SUPPORTER2, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, false,
                        npc.getInstanceId());
                break;
            case ANTYLOPE_1:
            case ANTYLOPE_2:
            case ANTYLOPE_3:
            case FLAVA:
                dropItem(npc, player);
                break;
            default:
                // hardcoded herb drops
                dropHerb(npc, player, HP_HERBS_DROPLIST);
                dropHerb(npc, player, MP_HERBS_DROPLIST);
                break;
        }
        return super.onKill(npc, player, isPet);
    }

    // Spawns Mage Type silenos behind the one that was killed. Aggro against the player that kill the mob
    private final void spawnMageBehind(L2Npc npc, L2PcInstance player, int mageId)
    {
        final double rads = Math.toRadians(Util.convertHeadingToDegree(npc.getSpawn().getHeading()) + 180);
        final int mageX = (int) (npc.getX() + 150 * Math.cos(rads));
        final int mageY = (int) (npc.getY() + 150 * Math.sin(rads));
        final L2Npc mageBack = addSpawn(mageId, mageX, mageY, npc.getZ(), npc.getSpawn().getHeading(), false, 0, true,
                npc.getInstanceId());
        mageBack.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, player, 1000);
    }

    /* This function will check if there is other mob alive in this wall of mobs. If all mobs in the first row are dead
     * then despawn the second row mobs, the mages
     */
    private final void checkIfLastInWall(L2Npc npc)
    {
        final Collection<L2Character> knowns = npc.getKnownList().getKnownCharactersInRadius(700);
        for (L2Character npcs : knowns)
        {
            if (!(npcs instanceof L2Npc))
            {
                continue;
            }

            if (npcs.isDead())
            {
                continue;
            }

            final L2Npc knownNpc = (L2Npc) npcs;

            switch (npc.getNpcId())
            {
                case VARKA_SILENOS_FOOTMAN:
                case VARKA_SILENOS_RECRUIT:
                case VARKA_SILENOS_WARRIOR:
                    switch (knownNpc.getNpcId())
                    {
                        case VARKA_SILENOS_FOOTMAN:
                        case VARKA_SILENOS_RECRUIT:
                        case VARKA_SILENOS_WARRIOR:
                            return;
                    }
                    break;
                case VARKA_ELITE_GUARD:
                case VARKAS_COMMANDER:
                case VARKA_SILENOS_OFFICER:
                    switch (knownNpc.getNpcId())
                    {
                        case VARKA_ELITE_GUARD:
                        case VARKAS_COMMANDER:
                        case VARKA_SILENOS_OFFICER:
                            return;
                    }
                    break;
                case VARKA_SILENOS_GREAT_MAGUS:
                case VARKA_SILENOS_GENERAL:
                case VARKAS_PROPHET:
                    switch (knownNpc.getNpcId())
                    {
                        case VARKA_SILENOS_GREAT_MAGUS:
                        case VARKA_SILENOS_GENERAL:
                        case VARKAS_PROPHET:
                            return;
                    }
                    break;
                case VARKA_SILENOS_HEAD_GUARD:
                case PROPHET_GUARD:
                    switch (knownNpc.getNpcId())
                    {
                        case VARKA_SILENOS_HEAD_GUARD:
                        case PROPHET_GUARD:
                            return;
                    }
                    break;
            }
        }

        // We didnt find any mob on the first row alive, so despawn the second row mobs
        for (L2Character npcs : knowns)
        {
            if (!(npcs instanceof L2Npc))
            {
                continue;
            }

            if (npcs.isDead())
            {
                continue;
            }

            final L2Npc knownNpc = (L2Npc) npcs;

            switch (npc.getNpcId())
            {
                case VARKA_SILENOS_FOOTMAN:
                case VARKA_SILENOS_RECRUIT:
                case VARKA_SILENOS_WARRIOR:
                    switch (knownNpc.getNpcId())
                    {
                        case VARKA_SILENOS_MEDIUM:
                        case VARKA_SILENOS_PRIEST:
                            knownNpc.abortCast();
                            knownNpc.deleteMe();
                            break;
                    }
                    break;
                case VARKA_ELITE_GUARD:
                case VARKAS_COMMANDER:
                case VARKA_SILENOS_OFFICER:
                    switch (knownNpc.getNpcId())
                    {
                        case VARKA_SILENOS_SHAMAN:
                        case VARKA_SILENOS_SEER:
                            knownNpc.abortCast();
                            knownNpc.deleteMe();
                            break;
                    }
                    break;
                case VARKA_SILENOS_GREAT_MAGUS:
                case VARKA_SILENOS_GENERAL:
                case VARKAS_PROPHET:
                    switch (knownNpc.getNpcId())
                    {
                        case VARKA_SILENOS_MAGNUS:
                        case DISCIPLE_OF_PROPHET:
                            knownNpc.abortCast();
                            knownNpc.deleteMe();
                            break;
                    }
                    break;
                case VARKA_SILENOS_HEAD_GUARD:
                case PROPHET_GUARD:
                    switch (knownNpc.getNpcId())
                    {
                        case VARKA_HEAD_MAGUS:
                        case VARKA_SILENOS_GREAT_SEER:
                            knownNpc.abortCast();
                            knownNpc.deleteMe();
                            break;
                    }
                    break;
            }
        }
    }

    @Override
    public final String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        final QuestState st = player.getQuestState(qn);
        if (st == null || st.getState() != State.STARTED)
        {
            return null;
        }

        if (isPet)
        {
            return null;
        }

        // If enter on aggro range of Latana, start animation
        switch (npc.getNpcId())
        {
            case LATANA:
                // Start Latana's Animation
                if (!_hasDoneAnimation)
                {
                    startQuestTimer("latana_animation", 500, npc, player);
                    return null;
                }
                break;
        }
        return super.onAggroRangeEnter(npc, player, isPet);
    }

    @Override
    public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
    {
        if (attacker == null)
        {
            return super.onAttack(npc, attacker, damage, isPet);
        }

        // If enter on aggro range of Latana, start animation
        switch (npc.getNpcId())
        {
            case LATANA:
                // Start Latana's Animation
                if (!_hasDoneAnimation)
                {
                    final QuestState st = attacker.getQuestState(qn);
                    if (st == null || st.getState() != State.STARTED)
                    {
                        return super.onAttack(npc, attacker, damage, isPet);
                    }

                    startQuestTimer("latana_animation", 500, npc, attacker);
                    return null;
                }
                break;
        }

        return super.onAttack(npc, attacker, damage, isPet);
    }

    @Override
    public String onSpawn(L2Npc npc)
    {
        if (npc instanceof L2MonsterInstance)
        {
            for (int mobId : WALL_MONSTERS)
            {
				/* Every monster on pailaka should be Aggresive and Active, with the same clan, also
				 * wall mobs cannot move, they all use magic from far, and if you get in combat range
				 * they hit
				 */
                if (mobId == npc.getNpcId())
                {
                    final L2MonsterInstance monster = (L2MonsterInstance) npc;
                    //monster.setIsAggresiveOverride(900);
                    //monster.setClanOverride("pailaka_clan");
                    monster.setIsImmobilized(true);
                    break;
                }
            }
        }
        return super.onSpawn(npc);
    }

    @Override
    public String onExitZone(L2Character character, L2ZoneType zone)
    {
        /**
         if (character instanceof L2PcInstance
         && !character.isDead()
         && !character.isTeleporting()
         && ((L2PcInstance)character).isOnline())
         {
         InstanceWorld world = InstanceManager.getInstance().getWorld(character.getInstanceId());
         if (world != null && world.templateId == INSTANCE_ID)
         ThreadPoolManager.getInstance().scheduleGeneral(new Teleport(character, world.instanceId), 1000);
         }
         */
        if (character instanceof L2Playable && !character.isDead() && !character.isTeleporting() &&
                character.getActingPlayer().isOnline())
        {
            InstanceWorld world = InstanceManager.getInstance().getWorld(character.getInstanceId());
            if (world != null && world.templateId == INSTANCE_ID)
            {
                //  - If a player wants to go by a mob wall without kill it, he will be returned back to a spawn point
                final int[] zoneTeleport = NOEXIT_ZONES.get(zone.getId());
                if (zoneTeleport != null)
                {
                    final Collection<L2Character> knowns = character.getKnownList().getKnownCharactersInRadius(700);
                    for (L2Character npcs : knowns)
                    {
                        if (!(npcs instanceof L2Npc))
                        {
                            continue;
                        }

                        if (npcs.isDead())
                        {
                            continue;
                        }

                        teleportPlayer(character.getActingPlayer(), zoneTeleport, world.instanceId);
                        break;
                    }
                }
            }
        }

        return super.onExitZone(character, zone);
    }

    static final class Teleport implements Runnable
    {
        private final L2Character _char;
        private final int _instanceId;

        public Teleport(L2Character c, int id)
        {
            _char = c;
            _instanceId = id;
        }

        @Override
        public void run()
        {
            try
            {
                teleportPlayer((L2PcInstance) _char, TELEPORT, _instanceId);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private static class PailakaDrop
    {
        private final int _itemId;
        private final int _chance;

        public PailakaDrop(int itemId, int chance)
        {
            _itemId = itemId;
            _chance = chance;
        }

        public int getItemID()
        {
            return _itemId;
        }

        public int getChance()
        {
            return _chance;
        }
    }

    public PailakaInjuredDragon(int questId, String name, String descr)
    {
        super(questId, name, descr);
        addStartNpc(KETRA_ORC_SHAMAN);
        for (int npcId : NPCS)
        {
            addTalkId(npcId);
        }

        addKillId(LATANA);
        for (int mobId : OTHER_MONSTERS)
        {
            addKillId(mobId);
        }

        addAggroRangeEnterId(LATANA);
        // Add aggro acting on main mobs
        for (int mobId : WALL_MONSTERS)
        {
            addSpawnId(mobId);
            addKillId(mobId);
        }

        addAttackId(LATANA);

        // Add all no exit zones for mob walls
        for (int zoneid : NOEXIT_ZONES.keySet())
        {
            addExitZoneId(zoneid);
        }

        //addExitZoneId(ZONE);
        questItemIds = ITEMS;
    }

    public static void main(String[] args)
    {
        new PailakaInjuredDragon(144, qn, "Pailaka - Injured Dragon");
    }
}
