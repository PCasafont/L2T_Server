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

package ai.individual.GrandBosses.Kelbim;

import l2server.Config;
import l2server.gameserver.datatables.DoorTable;
import l2server.gameserver.datatables.ScenePlayerDataTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.GrandBossManager;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2GrandBossInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.model.zone.type.L2BossZone;
import l2server.gameserver.network.serverpackets.Earthquake;
import l2server.gameserver.util.Broadcast;
import l2server.log.Log;
import l2server.util.Rnd;

import ai.group_template.L2AttackableAIScript;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LasTravel
 *         <p>
 *         Kelbim AI
 *         <p>
 *         Source:
 *         - https://www.youtube.com/watch?v=qVkk2BJoGoU
 */

public class Kelbim extends L2AttackableAIScript
{
    //Quest
    private static final boolean debug = false;
    private static final String qn = "Kelbim";

    //Ids
    private static final int npcEnterId = 34052;
    private static final int teleDevice = 34053;
    private static final int kelbimShout = 19597;
    private static final int kelbimId = 26124;
    private static final int guardianSinistra = 26126;
    private static final int guardianDestra = 26127;
    private static final int[] kelbimGuardians = {guardianSinistra, guardianDestra};
    private static final int kelbimGuard = 26129;
    private static final int kelbimAltar = 26130;
    private static final int[] kelbimMinions = {guardianSinistra, guardianDestra, kelbimGuard};
    private static final int[] allMobs =
            {kelbimId, kelbimMinions[0], kelbimMinions[1], kelbimMinions[2], kelbimAltar};
    private static final L2BossZone bossZone = GrandBossManager.getInstance().getZone(-55505, 58781, -274);
    private static final Location enterCords = new Location(-55386, 58939, -274);

    //Skills
    private static final L2Skill meteorCrash = SkillTable.getInstance().getInfo(23692, 1);
    private static final L2Skill waterDrop = SkillTable.getInstance().getInfo(23693, 1);
    private static final L2Skill tornadoShackle = SkillTable.getInstance().getInfo(23694, 1);
    private static final L2Skill flameThrower = SkillTable.getInstance().getInfo(23699, 1);
    private static final L2Skill[] areaSkills = {meteorCrash, waterDrop, tornadoShackle, flameThrower};

    //Vars
    private static L2Npc kelbimBoss;
    private static long lastAction;
    private static int bossStage;
    private static ArrayList<L2Npc> minions = new ArrayList<L2Npc>();

    public Kelbim(int id, String name, String descr)
    {
        super(id, name, descr);

        addTalkId(npcEnterId);
        addStartNpc(npcEnterId);

        addTalkId(teleDevice);
        addStartNpc(teleDevice);
        addFirstTalkId(teleDevice);

        for (int i : allMobs)
        {
            addAttackId(i);
            addKillId(i);
        }

        //Unlock
        long unlockTime = GrandBossManager.getInstance().getUnlockTime(kelbimId);
        startQuestTimer("unlock_kelbim", unlockTime, null, null);
        if (unlockTime == 1)
        {
            DoorTable.getInstance().getDoor(18190002).openMe();
            DoorTable.getInstance().getDoor(18190004).openMe();
        }
    }

    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player)
    {
        if (debug)
        {
            Log.warning(getName() + ": onFirstTalk: " + player.getName());
        }

        if (npc.getNpcId() == teleDevice)
        {
            player.teleToLocation(-55730, 55643, -1954);
        }
        return super.onFirstTalk(npc, player);
    }

    @Override
    public String onTalk(L2Npc npc, L2PcInstance player)
    {
        if (debug)
        {
            Log.warning(getName() + ": onTalk: " + player.getName());
        }

        int npcId = npc.getNpcId();
        if (npcId == npcEnterId)
        {
            int kelbimStatus = GrandBossManager.getInstance().getBossStatus(kelbimId);
            final List<L2PcInstance> allPlayers = new ArrayList<L2PcInstance>();
            if (kelbimStatus == GrandBossManager.getInstance().DEAD)
            {
                return "34052-1.htm";
            }
            else
            {
                if (!debug)
                {
                    if (kelbimStatus == GrandBossManager.getInstance().ALIVE && !InstanceManager.getInstance()
                            .checkInstanceConditions(player, 101, Config.KELBIM_MIN_PLAYERS, 200, 95, Config.MAX_LEVEL))
                    {
                        return null;
                    }
                    else if (kelbimStatus == GrandBossManager.getInstance().WAITING && !InstanceManager.getInstance()
                            .checkInstanceConditions(player, 101, Config.KELBIM_MIN_PLAYERS, 200, 95, Config.MAX_LEVEL))
                    {
                        return null;
                    }
                    else if (kelbimStatus == GrandBossManager.getInstance().FIGHTING)
                    {
                        return null;
                    }
                }
            }

            if (kelbimStatus == GrandBossManager.getInstance().ALIVE)
            {
                GrandBossManager.getInstance().setBossStatus(kelbimId, GrandBossManager.getInstance().WAITING);

                startQuestTimer("stage_1_start", 2 * 60000, null, null);
            }

            if (debug)
            {
                allPlayers.add(player);
            }
            else
            {
                allPlayers.addAll(Config.KELBIM_MIN_PLAYERS > Config.MAX_MEMBERS_IN_PARTY ||
                        player.getParty().isInCommandChannel() ? player.getParty().getCommandChannel().getMembers() :
                        player.getParty().getPartyMembers());
            }

            for (L2PcInstance enterPlayer : allPlayers)
            {
                if (enterPlayer == null)
                {
                    continue;
                }

                bossZone.allowPlayerEntry(enterPlayer, 7200);

                enterPlayer.teleToLocation(enterCords, true);
            }
        }
        return super.onTalk(npc, player);
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        if (debug)
        {
            Log.warning(getName() + ": onAdvEvent: " + event);
        }

        if (event.equalsIgnoreCase("unlock_kelbim"))
        {
            GrandBossManager.getInstance().setBossStatus(kelbimId, GrandBossManager.getInstance().ALIVE);

            Broadcast.toAllOnlinePlayers(new Earthquake(-55754, 59903, -269, 20, 10));

            DoorTable.getInstance().getDoor(18190002).openMe();
            DoorTable.getInstance().getDoor(18190004).openMe();
        }
        else if (event.equalsIgnoreCase("check_activity_task"))
        {
            if (!GrandBossManager.getInstance().isActive(kelbimId, lastAction))
            {
                notifyEvent("end_kelbim", null, null);
            }
        }
        else if (event.equalsIgnoreCase("stage_1_start"))
        {
            bossStage = 1;

            GrandBossManager.getInstance().setBossStatus(kelbimId, GrandBossManager.getInstance().FIGHTING);

            bossZone.broadcastMovie(81);

            startQuestTimer("stage_1_kelbim_spawn", ScenePlayerDataTable.getInstance().getVideoDuration(81) + 2000,
                    null, null);
        }
        else if (event.equalsIgnoreCase("stage_1_kelbim_spawn"))
        {
            kelbimBoss = addSpawn(kelbimId, -56340, 60801, -269, 54262, false, 0);
            GrandBossManager.getInstance().addBoss((L2GrandBossInstance) kelbimBoss);

            lastAction = System.currentTimeMillis();

            startQuestTimer("check_activity_task", 60000, null, null, true);

            startQuestTimer("stage_all_random_area_attack", Rnd.get(2, 3) * 60000, null, null);
        }
        else if (event.equalsIgnoreCase("stage_all_spawn_minions"))
        {
            for (int i = 0; i < Rnd.get(bossStage * 5 / 2, bossStage * 5); i++)
            {
                L2Npc minion =
                        addSpawn(kelbimGuard, kelbimBoss.getX(), kelbimBoss.getY(), kelbimBoss.getZ(), 0, true, 0,
                                true, 0);
                minion.setIsRunning(true);
                ((L2Attackable) minion).setIsRaidMinion(true);

                minions.add(minion);
            }

            for (int i = 0; i < Rnd.get(bossStage * 2 / 2, bossStage * 2); i++)
            {
                L2Npc minion = addSpawn(kelbimGuardians[Rnd.get(kelbimGuardians.length)], kelbimBoss.getX(),
                        kelbimBoss.getY(), kelbimBoss.getZ(), 0, true, 0, true, 0);
                minion.setIsRunning(true);
                ((L2Attackable) minion).setIsRaidMinion(true);

                minions.add(minion);
            }
        }
        else if (event.equalsIgnoreCase("stage_all_random_area_attack"))
        {
            if (bossStage > 0 && bossStage < 7)
            {
                if (kelbimBoss.isInCombat())
                {
                    L2Skill randomAttackSkill = areaSkills[Rnd.get(areaSkills.length)];
                    ArrayList<L2Npc> skillNpcs = new ArrayList<L2Npc>();
                    for (L2PcInstance pl : bossZone.getPlayersInside())
                    {
                        if (pl == null)
                        {
                            continue;
                        }

                        if (Rnd.get(100) > 40)
                        {
                            L2Npc skillMob =
                                    addSpawn(kelbimShout, pl.getX(), pl.getY(), pl.getZ() + 10, 0, true, 60000, false,
                                            0);
                            skillNpcs.add(skillMob);

                            minions.add(skillMob);
                        }
                    }

                    for (L2Npc skillNpc : skillNpcs)
                    {
                        if (skillNpc == null)
                        {
                            continue;
                        }

                        skillNpc.doCast(randomAttackSkill);
                    }
                }
                startQuestTimer("stage_all_random_area_attack", Rnd.get(1, 2) * 60000, null, null);
            }
        }
        else if (event.equalsIgnoreCase("cancel_timers"))
        {
            QuestTimer activityTimer = getQuestTimer("check_activity_task", null, null);
            if (activityTimer != null)
            {
                activityTimer.cancel();
            }
        }
        else if (event.equalsIgnoreCase("end_kelbim"))
        {
            bossStage = 0;

            notifyEvent("cancel_timers", null, null);

            bossZone.oustAllPlayers();

            if (kelbimBoss != null)
            {
                kelbimBoss.deleteMe();
            }

            if (!minions.isEmpty())
            {
                for (L2Npc minion : minions)
                {
                    if (minion == null)
                    {
                        continue;
                    }

                    minion.deleteMe();
                }
            }

            minions.clear();

            if (GrandBossManager.getInstance().getBossStatus(kelbimId) != GrandBossManager.getInstance().DEAD)
            {
                GrandBossManager.getInstance().setBossStatus(kelbimId, GrandBossManager.getInstance().ALIVE);
            }
        }
        return super.onAdvEvent(event, npc, player);
    }

    @Override
    public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
    {
        if (debug)
        {
            Log.warning(getName() + ": onAttack: " + npc.getName());
        }

        if (npc.getNpcId() == kelbimId)
        {
            lastAction = System.currentTimeMillis();

            if (bossStage == 1 && npc.getCurrentHp() < npc.getMaxHp() * 0.80)
            {
                bossStage = 2;

                notifyEvent("stage_all_spawn_minions", null, null);
            }
            else if (bossStage == 2 && npc.getCurrentHp() < npc.getMaxHp() * 0.60)
            {
                bossStage = 3;

                notifyEvent("stage_all_spawn_minions", null, null);
            }
            else if (bossStage == 3 && npc.getCurrentHp() < npc.getMaxHp() * 0.40)
            {
                bossStage = 4;

                notifyEvent("stage_all_spawn_minions", null, null);
            }
            else if (bossStage == 4 && npc.getCurrentHp() < npc.getMaxHp() * 0.20)
            {
                bossStage = 5;

                notifyEvent("stage_all_spawn_minions", null, null);
            }
            else if (bossStage == 5 && npc.getCurrentHp() < npc.getMaxHp() * 0.05)
            {
                bossStage = 6;

                notifyEvent("stage_all_spawn_minions", null, null);
            }
        }
        return super.onAttack(npc, attacker, damage, isPet);
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
    {
        if (debug)
        {
            Log.warning(getName() + ": onKill: " + npc.getName());
        }

        if (npc.getNpcId() == kelbimId)
        {
            bossStage = 7;

            addSpawn(teleDevice, -54331, 58331, -264, 16292, false, 1800000);

            GrandBossManager.getInstance().notifyBossKilled(kelbimId);

            DoorTable.getInstance().getDoor(18190002).closeMe();
            DoorTable.getInstance().getDoor(18190004).closeMe();

            notifyEvent("cancel_timers", null, null);

            startQuestTimer("unlock_kelbim", GrandBossManager.getInstance().getUnlockTime(kelbimId), null, null);
            startQuestTimer("end_kelbim", 1800000, null, null);
        }
        return super.onKill(npc, killer, isPet);
    }

    public static void main(String[] args)
    {
        new Kelbim(-1, qn, "ai/individual/GrandBosses");
    }
}
