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

package ai.individual.GrandBosses.AntharasOpenWorld;

import l2server.Config;
import l2server.gameserver.GeoData;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.instancemanager.GrandBossManager;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2GrandBossInstance;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.model.zone.type.L2BossZone;
import l2server.gameserver.network.serverpackets.Earthquake;
import l2server.gameserver.network.serverpackets.ExSendUIEvent;
import l2server.gameserver.network.serverpackets.ExSendUIEventRemove;
import l2server.gameserver.network.serverpackets.PlaySound;
import l2server.gameserver.network.serverpackets.SpecialCamera;
import l2server.gameserver.util.Broadcast;
import l2server.log.Log;
import l2server.util.Rnd;

import ai.group_template.L2AttackableAIScript;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author LasTravel
 *         <p>
 *         Open World Antharas AI (Based on SANDMAN work)
 */

public class AntharasOpenWorld extends L2AttackableAIScript
{
    //Quest
    private static final boolean _debug = false;
    private static final String _qn = "AntharasOpenWorld";

    //Id's
        private static final int _maxMinions = 30;
    private static final int _behemothDragon = 29069;
    private static final int _taraskDragon = 29225;
    private static final int[] _dragonBombers = {29070, 29076};
    private static final int _antharasId = 29068;
    private static final int[] _allMobIds =
            {_behemothDragon, _taraskDragon, _dragonBombers[0], _dragonBombers[1], _antharasId};
    private static final int _heartOfWarding = 13001;
    private static final int _teleportCubic = 31859;
    private static final L2BossZone _bossZone = GrandBossManager.getInstance().getZone(179700, 113800, -7709);

    //Others
    private static final List<L2Npc> _allMonsters = new ArrayList<L2Npc>();
    private static L2Npc _antharasBoss;
    private static long _LastAction;

    public AntharasOpenWorld(int id, String name, String descr)
    {
        super(id, name, descr);

        addTalkId(_teleportCubic);
        addStartNpc(_teleportCubic);

        addTalkId(_heartOfWarding);
        addStartNpc(_heartOfWarding);

        for (int i : _allMobIds)
        {
            addKillId(i);
            addAttackId(i);
        }

        addEnterZoneId(_bossZone.getId());
        addExitZoneId(_bossZone.getId());

        //Unlock
        startQuestTimer("unlock_antharas", GrandBossManager.getInstance().getUnlockTime(_antharasId), null, null);
    }

    @Override
    public String onTalk(L2Npc npc, L2PcInstance player)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onTalk: " + player.getName());
        }

        if (npc.getNpcId() == _heartOfWarding)
        {
            int anthyStatus = GrandBossManager.getInstance().getBossStatus(_antharasId);

            final List<L2PcInstance> allPlayers = new ArrayList<L2PcInstance>();

            if (anthyStatus == GrandBossManager.getInstance().DEAD)
            {
                return "13001-01.html";
            }
            else
            {
                if (!_debug)
                {
                    if (anthyStatus == GrandBossManager.getInstance().ALIVE && !InstanceManager.getInstance()
                            .checkInstanceConditions(player, -1, Config.ANTHARAS_MIN_PLAYERS, 200, 95,
                                    Config.MAX_LEVEL))
                    {
                        return null;
                    }
                    else if (anthyStatus == GrandBossManager.getInstance().WAITING && !InstanceManager.getInstance()
                            .checkInstanceConditions(player, -1, Config.ANTHARAS_MIN_PLAYERS, 200, 95,
                                    Config.MAX_LEVEL))
                    {
                        return null;
                    }
                    else if (anthyStatus == GrandBossManager.getInstance().FIGHTING)
                    {
                        return "13001-02.html";
                    }
                }
            }

            if (anthyStatus == GrandBossManager.getInstance().ALIVE)
            {
                GrandBossManager.getInstance().setBossStatus(_antharasId, GrandBossManager.getInstance().WAITING);

                _LastAction = System.currentTimeMillis();

                startQuestTimer("antharas_spawn_task_1", _debug ? 60000 : Config.ANTHARAS_WAIT_TIME * 60000, null,
                        null);
            }

            if (_debug)
            {
                allPlayers.add(player);
            }
            else
            {
                allPlayers.addAll(Config.ANTHARAS_MIN_PLAYERS > Config.MAX_MEMBERS_IN_PARTY ||
                        player.getParty().isInCommandChannel() ? player.getParty().getCommandChannel().getMembers() :
                        player.getParty().getPartyMembers());
            }

            for (L2PcInstance enterPlayer : allPlayers)
            {
                if (enterPlayer == null)
                {
                    continue;
                }

                _bossZone.allowPlayerEntry(enterPlayer, 7200);

                enterPlayer.teleToLocation(179700 + Rnd.get(700), 113800 + Rnd.get(2100), -7709);
            }
        }
        else if (npc.getNpcId() == _teleportCubic)
        {
            player.teleToLocation(79800 + Rnd.get(600), 151200 + Rnd.get(1100), -3534);
        }
        return super.onTalk(npc, player);
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onAdvEvent: " + event);
        }

        if (event.equalsIgnoreCase("unlock_antharas"))
        {
            GrandBossManager.getInstance().setBossStatus(_antharasId, GrandBossManager.getInstance().ALIVE);

            Broadcast.toAllOnlinePlayers(new Earthquake(185708, 114298, -8221, 20, 10));
        }
        else if (event.equalsIgnoreCase("antharas_spawn_task_1"))
        {
            //Block all players
            _bossZone.stopWholeZone();

            _antharasBoss = addSpawn(_antharasId, 181323, 114850, -7623, 32542, false, 120 * 2 * 60000);

            _allMonsters.add(_antharasBoss);

            GrandBossManager.getInstance().addBoss((L2GrandBossInstance) _antharasBoss);

            _antharasBoss.setIsImmobilized(true);

            GrandBossManager.getInstance().setBossStatus(_antharasId, GrandBossManager.getInstance().FIGHTING);

            _LastAction = System.currentTimeMillis();

            //Cameras
            _bossZone.sendDelayedPacketToZone(16,
                    new SpecialCamera(_antharasBoss.getObjectId(), 700, 13, -19, 0, 20000, 0, 0, 1, 0));
            _bossZone.sendDelayedPacketToZone(3016,
                    new SpecialCamera(_antharasBoss.getObjectId(), 700, 13, 0, 6000, 20000, 0, 0, 1, 0));
            _bossZone.sendDelayedPacketToZone(13016,
                    new SpecialCamera(_antharasBoss.getObjectId(), 3700, 0, -3, 0, 10000, 0, 0, 1, 0));
            _bossZone.sendDelayedPacketToZone(13216,
                    new SpecialCamera(_antharasBoss.getObjectId(), 1100, 0, -3, 22000, 30000, 0, 0, 1, 0));
            _bossZone.sendDelayedPacketToZone(24016,
                    new SpecialCamera(_antharasBoss.getObjectId(), 1100, 0, -3, 300, 7000, 0, 0, 1, 0));

            startQuestTimer("antharas_spawn_task_7", 25916, null, null);
            startQuestTimer("antharas_last_spawn_task", 25916, null, null);
            startQuestTimer("check_activity_task", 60000, null, null, true);
            startQuestTimer("spawn_minion_task", 5 * 60000, null, null, true);
        }
        else if (event.equalsIgnoreCase("antharas_spawn_task_7"))
        {
            _antharasBoss.abortCast();

            _antharasBoss.setIsImmobilized(false);

            startQuestTimer("antharas_move_random", 500, null, null);
        }
        else if (event.equalsIgnoreCase("antharas_move_random"))
        {
            //UnBlock all players
            _bossZone.startWholeZone();

            L2CharPosition pos = new L2CharPosition(Rnd.get(175000, 178500), Rnd.get(112400, 116000), -7707, 0);

            _antharasBoss.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);

            //kick dual box
            _bossZone.kickDualBoxes();
        }
        else if (event.equalsIgnoreCase("check_activity_task"))
        {
            if (!GrandBossManager.getInstance().isActive(_antharasId, _LastAction))
            {
                notifyEvent("end_antharas", null, null);
            }
        }
        else if (event.equalsIgnoreCase("cancel_timers"))
        {
            QuestTimer activityTimer = getQuestTimer("check_activity_task", null, null);
            if (activityTimer != null)
            {
                activityTimer.cancel();
            }

            QuestTimer spawnMinionTask = getQuestTimer("spawn_minion_task", null, null);
            if (spawnMinionTask != null)
            {
                spawnMinionTask.cancel();
            }
        }
        else if (event.equalsIgnoreCase("end_antharas"))
        {
            notifyEvent("cancel_timers", null, null);

            _bossZone.oustAllPlayers();

            for (L2Npc mob : _allMonsters)
            {
                mob.getSpawn().stopRespawn();
                mob.deleteMe();
            }
            _allMonsters.clear();

            if (GrandBossManager.getInstance().getBossStatus(_antharasId) != GrandBossManager.getInstance().DEAD)
            {
                GrandBossManager.getInstance().setBossStatus(_antharasId, GrandBossManager.getInstance().ALIVE);
            }
        }
        else if (event.equalsIgnoreCase("spawn_minion_task"))
        {
            if (_antharasBoss != null && !_antharasBoss.isDead())
            {
                List<Integer> minionsToSpawn = new ArrayList<Integer>();
                for (int i = 1; i <= 5; i++)
                {
                    if (_allMonsters.size() < _maxMinions)
                    {
                        minionsToSpawn.add(_dragonBombers[Rnd.get(_dragonBombers.length)]);
                    }
                }

                if (_allMonsters.size() < _maxMinions)
                {
                    minionsToSpawn.add(_taraskDragon);
                }

                if (_allMonsters.size() < _maxMinions)
                {
                    minionsToSpawn.add(_behemothDragon);
                }

                for (int i = 0; i < minionsToSpawn.size(); i++)
                {
                    int tried = 0;
                    boolean notFound = true;
                    int x = 175000;
                    int y = 112400;
                    int dt = (_antharasBoss.getX() - x) * (_antharasBoss.getX() - x) +
                            (_antharasBoss.getY() - y) * (_antharasBoss.getY() - y);

                    while (tried++ < 25 && notFound)
                    {
                        int rx = Rnd.get(175000, 179900);
                        int ry = Rnd.get(112400, 116000);
                        int rdt = (_antharasBoss.getX() - rx) * (_antharasBoss.getX() - rx) +
                                (_antharasBoss.getY() - ry) * (_antharasBoss.getY() - ry);

                        if (GeoData.getInstance()
                                .canSeeTarget(_antharasBoss.getX(), _antharasBoss.getY(), -7704, rx, ry, -7704))
                        {
                            if (rdt < dt)
                            {
                                x = rx;
                                y = ry;
                                dt = rdt;

                                if (rdt <= 900000)
                                {
                                    notFound = false;
                                }
                            }
                        }
                    }

                    L2Npc minion = addSpawn(minionsToSpawn.get(i), x, y, -7704, 0, true, 120 * 2 * 60000);
                    minion.setIsRunning(true);
                    _allMonsters.add(minion);
                }
            }
        }
        return super.onAdvEvent(event, npc, player);
    }

    @Override
    public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onAttack: " + npc.getName());
        }

        if (npc.getNpcId() == _antharasId)
        {
            _LastAction = System.currentTimeMillis();

            if (GrandBossManager.getInstance().getBossStatus(_antharasId) != GrandBossManager.getInstance().FIGHTING)
            {
                _bossZone.oustAllPlayers();
            }
        }
        return super.onAttack(npc, attacker, damage, isPet);
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onKill: " + npc.getName());
        }

        if (npc.getNpcId() == _antharasId)
        {
            GrandBossManager.getInstance().notifyBossKilled(_antharasId);

            notifyEvent("cancel_timers", null, null);

            _bossZone.broadcastPacket(
                    new PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));

            addSpawn(_teleportCubic, 177615, 114941, -7709, 0, false, 600000); //10min

            startQuestTimer("unlock_antharas", GrandBossManager.getInstance().getUnlockTime(_antharasId), null, null);

            startQuestTimer("end_antharas", 900000, null, null);
        }
        else if (npc.getNpcId() == _behemothDragon)
        {
            int countHPHerb = Rnd.get(6, 18);
            int countMPHerb = Rnd.get(6, 18);

            for (int i = 0; i < countHPHerb; i++)
            {
                ((L2MonsterInstance) npc).dropItem(killer, 8602, 1);
            }

            for (int i = 0; i < countMPHerb; i++)
            {
                ((L2MonsterInstance) npc).dropItem(killer, 8605, 1);
            }
        }

        if (_allMonsters.contains(npc))
        {
            _allMonsters.remove(npc);
        }

        return super.onKill(npc, killer, isPet);
    }

    @Override
    public final String onEnterZone(L2Character character, L2ZoneType zone)
    {
        if (character instanceof L2PcInstance)
        {
            if (GrandBossManager.getInstance().getBossStatus(_antharasId) == GrandBossManager.getInstance().WAITING)
            {
                character.sendPacket(new ExSendUIEvent(0, 0, (int) TimeUnit.MILLISECONDS
                        .toSeconds(_LastAction + Config.ANTHARAS_WAIT_TIME * 60000 - System.currentTimeMillis()), 0,
                        "Antharas is coming..."));
            }
        }
        return null;
    }

    @Override
    public final String onExitZone(L2Character character, L2ZoneType zone)
    {
        if (character instanceof L2PcInstance)
        {
            if (GrandBossManager.getInstance().getBossStatus(_antharasId) == GrandBossManager.getInstance().WAITING)
            {
                character.sendPacket(new ExSendUIEventRemove());
            }
        }
        return null;
    }

    public static void main(String[] args)
    {
        new AntharasOpenWorld(-1, _qn, "ai/individual/GrandBosses");
    }
}
