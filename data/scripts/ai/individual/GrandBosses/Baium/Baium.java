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

package ai.individual.GrandBosses.Baium;

import java.util.ArrayList;
import java.util.List;

import ai.group_template.L2AttackableAIScript;
import l2server.Config;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.MapRegionTable.TeleportWhereType;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.instancemanager.GrandBossManager;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2GrandBossInstance;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.model.zone.type.L2BossZone;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.Earthquake;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.PlaySound;
import l2server.gameserver.network.serverpackets.SocialAction;
import l2server.log.Log;
import l2server.util.Rnd;

/**
 * @author LasTravel
 *         <p>
 *         Baium AI (Based on Fulminus work)
 *         <p>
 *         Source:
 *         - http://www.youtube.com/watch?v=xljlWxSQpM0
 */

public class Baium extends L2AttackableAIScript
{
    //Quest
    private static final boolean debug = false;
    private static final String qn = "Baium";

    //Id's
    private static final int liveBaium = 29020;
    private static final int stoneBaium = 29025;
    private static final int archangel = 29021;
    private static final int vortex = 31862;
    private static final int exitCubic = 31842;
    private static final int[] allMobs = {liveBaium, stoneBaium, archangel};
    private static final L2BossZone bossZone = GrandBossManager.getInstance().getZone(113100, 14500, 10077);
    private static final L2Skill baiumGift = SkillTable.getInstance().getInfo(4136, 1);

    //Others
    private static long lastAction;
    private static L2Npc baiumBoss;
    private static L2PcInstance firstAttacker;

    public Baium(int id, String name, String descr)
    {
        super(id, name, descr);

        addStartNpc(this.vortex);
        addTalkId(this.vortex);

        addStartNpc(this.stoneBaium);
        addTalkId(this.stoneBaium);

        addStartNpc(this.exitCubic);
        addTalkId(this.exitCubic);

        addSpawnId(this.archangel);

        for (int i : this.allMobs)
        {
            addAttackId(i);
            addKillId(i);
        }

        //Unlock
        startQuestTimer("unlock_baium", GrandBossManager.getInstance().getUnlockTime(this.liveBaium), null, null);
    }

    @Override
    public String onTalk(L2Npc npc, L2PcInstance player)
    {
        if (this.debug)
        {
            Log.warning(getName() + ": onTalk: " + player.getName());
        }

        if (npc.getNpcId() == this.vortex)
        {
            int baiumStatus = GrandBossManager.getInstance().getBossStatus(this.liveBaium);

            final List<L2PcInstance> allPlayers = new ArrayList<L2PcInstance>();

            if (baiumStatus == GrandBossManager.getInstance().DEAD)
            {
                return "31862-02.html";
            }
            else
            {
                if (!this.debug)
                {
                    int maxLvl = 84;
                    if (Config.isServer(Config.TENKAI_ESTHUS))
                    {
                        maxLvl = Config.MAX_LEVEL;
                    }
                    if (baiumStatus == GrandBossManager.getInstance().ALIVE && !InstanceManager.getInstance()
                            .checkInstanceConditions(player, -1, Config.BAIUM_MIN_PLAYERS, 200, 76, maxLvl))
                    {
                        return null;
                    }
                    else if (baiumStatus == GrandBossManager.getInstance().WAITING && !InstanceManager.getInstance()
                            .checkInstanceConditions(player, -1, Config.BAIUM_MIN_PLAYERS, 200, 76, maxLvl))
                    {
                        return null;
                    }
                    else if (baiumStatus == GrandBossManager.getInstance().FIGHTING)
                    {
                        return "31862-01.html";
                    }
                }
            }

            if (baiumStatus == GrandBossManager.getInstance().ALIVE)
            {
                GrandBossManager.getInstance().setBossStatus(this.liveBaium, GrandBossManager.getInstance().WAITING);

                this.lastAction = System.currentTimeMillis();

                startQuestTimer("check_activity_task", 60000, null, null, true);
            }

            if (this.debug)
            {
                allPlayers.add(player);
            }
            else
            {
                allPlayers.addAll(Config.BAIUM_MIN_PLAYERS > Config.MAX_MEMBERS_IN_PARTY ||
                        player.getParty().isInCommandChannel() ? player.getParty().getCommandChannel().getMembers() :
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

                this.bossZone.allowPlayerEntry(enterPlayer, 7200);

                enterPlayer.teleToLocation(113100, 14500, 10077, true);
            }
        }
        else if (npc.getNpcId() == this.stoneBaium)
        {
            notifyEvent("wake_up_baium", npc, player);
        }
        else if (npc.getNpcId() == this.exitCubic)
        {
            player.teleToLocation(TeleportWhereType.Town);
        }
        return super.onTalk(npc, player);
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        if (this.debug)
        {
            Log.warning(getName() + ": onAdvEvent: " + event);
        }

        if (event.equalsIgnoreCase("unlock_baium"))
        {
            this.baiumBoss = addSpawn(this.stoneBaium, 116033, 17447, 10104, 40188, false, 0);

            GrandBossManager.getInstance().setBossStatus(this.liveBaium, GrandBossManager.getInstance().ALIVE);
        }
        else if (event.equalsIgnoreCase("check_activity_task"))
        {
            if (!GrandBossManager.getInstance().isActive(this.liveBaium, this.lastAction))
            {
                notifyEvent("end_baium", null, null);
            }
        }
        else if (event.equalsIgnoreCase("wake_up_baium"))
        {
            if (GrandBossManager.getInstance().getBossStatus(this.liveBaium) == GrandBossManager.getInstance().WAITING)
            {
                npc.deleteMe();

                GrandBossManager.getInstance().setBossStatus(this.liveBaium, GrandBossManager.getInstance().FIGHTING);

                this.baiumBoss = addSpawn(this.liveBaium, 116033, 17447, 10107, -25348, false, 0);

                this.baiumBoss.setIsInvul(true);

                this.baiumBoss.disableCoreAI(true);

                this.baiumBoss.setRunning();

                this.firstAttacker = player;

                GrandBossManager.getInstance().addBoss((L2GrandBossInstance) this.baiumBoss);

                this.bossZone.sendDelayedPacketToZone(50, new SocialAction(this.baiumBoss.getObjectId(), 2));

                startQuestTimer("wake_up_intro_1", 5000, this.baiumBoss, null);
            }
        }
        else if (event.equalsIgnoreCase("wake_up_intro_1"))
        {
            this.bossZone.broadcastPacket(new Earthquake(this.baiumBoss.getX(), this.baiumBoss.getY(), this.baiumBoss.getZ(), 40, 10));
            this.bossZone.broadcastPacket(new PlaySound("BS02_A"));
            this.bossZone.sendDelayedPacketToZone(8000, new SocialAction(this.baiumBoss.getObjectId(), 3));

            startQuestTimer("baium_spawn_minions", 17000, null, null);
        }
        else if (event.equalsIgnoreCase("baium_spawn_minions"))
        {
            this.baiumBoss.broadcastPacket(new SocialAction(this.baiumBoss.getObjectId(), 1));

            if (!this.firstAttacker.isOnline() || !this.bossZone.isInsideZone(this.firstAttacker)) //Get random one in case
            {
                this.firstAttacker = this.baiumBoss.getKnownList().getKnownPlayers()
                        .get(Rnd.get(this.baiumBoss.getKnownList().getKnownPlayers().size())); //if is empty...
            }

            if (!this.firstAttacker.isInsideRadius(this.baiumBoss, this.baiumGift.getEffectRange(), false, false))
            {
                this.firstAttacker.teleToLocation(115910, 17337, 10105);
            }

            if (this.firstAttacker != null)
            {
                this.baiumBoss.setTarget(this.firstAttacker);

                this.baiumBoss.doCast(this.baiumGift);

                this.baiumBoss.broadcastPacket(new CreatureSay(this.baiumBoss.getObjectId(), 0, this.baiumBoss.getName(),
                        this.firstAttacker.getName() + ", How dare you wake me! Now you shall die!"));
            }

            for (L2PcInstance players : this.bossZone.getPlayersInside())
            {
                if (players == null || !players.isHero())
                {
                    continue;
                }

                this.bossZone.broadcastPacket(new ExShowScreenMessage(
                        "Not even the gods themselves could touch me. But you, $s1, you dare challenge me?! Ignorant mortal!"
                                .replace("$1", players.getName()), 4000));//1000521
            }

            SpawnTable.getInstance().spawnSpecificTable("baium_minions");

            startQuestTimer("minions_attack_task", 60000, null, null, true);

            this.baiumBoss.setIsInvul(false);

            this.baiumBoss.disableCoreAI(false);
        }
        else if (event.equalsIgnoreCase("minions_attack_task"))
        {
            //Let's do it simple, minions should attack baium & players, by default due the enemy clan attacks almost all time baium instead of players so call this each time..
            List<L2PcInstance> insidePlayers = this.bossZone.getPlayersInside();
            L2Character target = null;

            if (insidePlayers != null && !insidePlayers.isEmpty())
            {
                for (L2Npc zoneMob : this.bossZone.getNpcsInside())
                {
                    if (!(zoneMob instanceof L2MonsterInstance))
                    {
                        continue;
                    }

                    if (zoneMob.getTarget() != null) //Only if default core ai are doing some shit
                    {
                        if (zoneMob.getTarget() == this.baiumBoss)
                        {
                            target = insidePlayers.get(Rnd.get(insidePlayers.size()));
                        }
                        else
                        {
                            //Lets use that code to take a lil look into the baiums target, if baim is attacking a minion set a random player as a target
                            if (zoneMob == this.baiumBoss && zoneMob.getTarget() instanceof L2MonsterInstance)
                            {
                                target = insidePlayers.get(Rnd.get(insidePlayers.size()));
                            }
                            else
                            {
                                target = this.baiumBoss;
                            }
                        }
                        if (target != null)
                        {
                            ((L2Attackable) zoneMob).getAggroList().clear();
                            zoneMob.setTarget(target);
                            ((L2MonsterInstance) zoneMob).addDamageHate(target, 500, 99999);
                            zoneMob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
                        }
                    }
                }
            }
        }
        else if (event.equalsIgnoreCase("cancel_timers"))
        {
            QuestTimer activityTimer = getQuestTimer("check_activity_task", null, null);
            if (activityTimer != null)
            {
                activityTimer.cancel();
            }

            QuestTimer minionsTimer = getQuestTimer("minions_attack_task", null, null);
            if (minionsTimer != null)
            {
                minionsTimer.cancel();
            }
        }
        else if (event.equalsIgnoreCase("end_baium"))
        {
            notifyEvent("cancel_timers", null, null);

            this.bossZone.oustAllPlayers();

            if (this.baiumBoss != null)
            {
                this.baiumBoss.deleteMe();
            }

            SpawnTable.getInstance().despawnSpecificTable("baium_minions");

            if (GrandBossManager.getInstance().getBossStatus(this.liveBaium) != GrandBossManager.getInstance().DEAD)
            {
                GrandBossManager.getInstance().setBossStatus(this.liveBaium, GrandBossManager.getInstance().ALIVE);
                this.baiumBoss = addSpawn(this.stoneBaium, 116033, 17447, 10104, 40188, false, 0);
            }
        }
        else if (event.equalsIgnoreCase("31862-03.html"))
        {
            return event;
        }

        return super.onAdvEvent(event, npc, player);
    }

    @Override
    public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
    {
        if (this.debug)
        {
            Log.warning(getName() + ": onAttack: " + npc.getName());
        }

        this.lastAction = System.currentTimeMillis();

        //Anti BUGGERS
        if (!this.bossZone.isInsideZone(attacker)) //Character attacking out of zone
        {
            attacker.doDie(null);

            if (this.debug)
            {
                Log.warning(getName() + ": Character: " + attacker.getName() + " attacked: " + npc.getName() +
                        " out of the boss zone!");
            }
        }

        if (!this.bossZone.isInsideZone(npc)) //Npc moved out of the zone
        {
            L2Spawn spawn = npc.getSpawn();

            if (spawn != null)
            {
                npc.teleToLocation(spawn.getX(), spawn.getY(), spawn.getZ());
            }

            if (this.debug)
            {
                Log.warning(getName() + ": Character: " + attacker.getName() + " attacked: " + npc.getName() +
                        " wich is out of the boss zone!");
            }
        }

        return super.onAttack(npc, attacker, damage, isPet);
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
    {
        if (this.debug)
        {
            Log.warning(getName() + ": onKill: " + npc.getName());
        }

        if (npc.getNpcId() == this.liveBaium)
        {
            GrandBossManager.getInstance().notifyBossKilled(this.liveBaium);

            notifyEvent("cancel_timers", null, null);

            SpawnTable.getInstance().despawnSpecificTable("baium_minions");

            this.bossZone.broadcastPacket(new PlaySound("BS01"));

            addSpawn(this.exitCubic, 115017, 15549, 10090, 0, false, 600000); //10min

            startQuestTimer("unlock_baium", GrandBossManager.getInstance().getUnlockTime(this.liveBaium), null, null);

            startQuestTimer("end_baium", 900000, null, null);
        }

        return super.onKill(npc, killer, isPet);
    }

    @Override
    public String onSpawn(L2Npc npc)
    {
        if (this.debug)
        {
            Log.warning(getName() + ": onSpawn: " + npc.getName());
        }

        npc.setIsRunning(true);
        ((L2Attackable) npc).setIsRaidMinion(true);

        return super.onSpawn(npc);
    }

    public static void main(String[] args)
    {
        new Baium(-1, qn, "ai/individual/GrandBosses");
    }
}
