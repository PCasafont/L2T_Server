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

package ai.individual.GrandBosses.Lilith;

import ai.group_template.L2AttackableAIScript;
import l2server.Config;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.MapRegionTable.TeleportWhereType;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.instancemanager.GrandBossManager;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2GrandBossInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.model.zone.type.L2BossZone;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LasTravel
 *         <p>
 *         Lilith AI
 *         <p>
 *         Source:
 *         - https://www.youtube.com/watch?v=H3MuIwUjjD4
 */

public class Lilith extends L2AttackableAIScript
{
    //Quest
    private static final boolean _debug = false;
    private static final String _qn = "Lilith";

    //Id's
    private static final int _lilithId = 25283;
    private static final int _remnant = 19490;
    private static final int _enterCubic = 31118;
    private static final int _exitCubic = 31124;
    private static final int _lilithCubic = 31110;
    private static final int[] _lilithMinions = {25284, 25285};
    private static final int[] _necroMobs = {21178, 21179, 21180, 21181, 21182, 21183, 21184, 21185, 21186};
    private static final L2Skill _remantTele = SkillTable.getInstance().getInfo(23303, 1);
    private static final Location _enterLoc = new Location(-19361, 13504, -4906);
    private static final Location _enterLilithLoc = new Location(184449, -9032, -5499);
    private static final int[] _allMobs = {
            _lilithId,
            _lilithMinions[0],
            _lilithMinions[1],
            _necroMobs[0],
            _necroMobs[1],
            _necroMobs[2],
            _necroMobs[3],
            _necroMobs[4],
            _necroMobs[5],
            _necroMobs[6],
            _necroMobs[7],
            _necroMobs[8],
            _remnant
    };
    private static final L2BossZone _bossZone = GrandBossManager.getInstance().getZone(185062, -9605, -5499);
    private static final L2BossZone _preLilithZone = GrandBossManager.getInstance().getZone(-19105, 13588, -4906);

    //Others
    private static List<L2Npc> _remnants = new ArrayList<L2Npc>();
    private static long _lastAction;
    private static L2Npc _lilithBoss;

    public Lilith(int id, String name, String descr)
    {
        super(id, name, descr);

        addStartNpc(_enterCubic);
        addTalkId(_enterCubic);

        addStartNpc(_exitCubic);
        addTalkId(_exitCubic);

        addStartNpc(_lilithCubic);
        addTalkId(_lilithCubic);

        addSpawnId(_remnant);
        addSpellFinishedId(_remnant);

        for (int i : _allMobs)
        {
            addAttackId(i);
            addKillId(i);
            addSkillSeeId(i);
        }

        //Unlock
        startQuestTimer("unlock_lilith", GrandBossManager.getInstance().getUnlockTime(_lilithId), null, null);
    }

    @Override
    public String onTalk(L2Npc npc, L2PcInstance player)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onTalk: " + player.getName());
        }

        int npcId = npc.getNpcId();

        if (npcId == _enterCubic || npcId == _lilithCubic)
        {
            int _lilithStatus = GrandBossManager.getInstance().getBossStatus(_lilithId);

            final List<L2PcInstance> allPlayers = new ArrayList<L2PcInstance>();

            if (_lilithStatus == GrandBossManager.getInstance().DEAD)
            {
                return "31118-01.html";
            }
            else
            {
                if (!_debug)
                {
                    if (_lilithStatus == GrandBossManager.getInstance().ALIVE && !InstanceManager.getInstance()
                            .checkInstanceConditions(player, -1, Config.LILITH_MIN_PLAYERS, 100, 99, Config.MAX_LEVEL))
                    {
                        return null;
                    }
                    else if (_lilithStatus == GrandBossManager.getInstance().WAITING && !InstanceManager.getInstance()
                            .checkInstanceConditions(player, -1, Config.LILITH_MIN_PLAYERS, 100, 99, Config.MAX_LEVEL))
                    {
                        return null;
                    }

                    if (_lilithStatus == GrandBossManager.getInstance().FIGHTING)
                    {
                        return "31118-01.html";
                    }
                }
            }

            if (_lilithStatus == GrandBossManager.getInstance().ALIVE && npcId == _enterCubic)
            {
                GrandBossManager.getInstance().setBossStatus(_lilithId, GrandBossManager.getInstance().WAITING);

                SpawnTable.getInstance().spawnSpecificTable("pre_lilith");

                _remnants.clear();

                notifyEvent("spawn_remant", null, null);

                _lastAction = System.currentTimeMillis();

                startQuestTimer("check_activity_task", 60000, null, null, true);
            }
            else if (_lilithStatus == GrandBossManager.getInstance().WAITING && npcId == _lilithCubic)
            {
                if (!_remnants.isEmpty())
                {
                    return "";
                }

                GrandBossManager.getInstance().setBossStatus(_lilithId, GrandBossManager.getInstance().FIGHTING);

                //Spawn the rb
                _lilithBoss = addSpawn(_lilithId, 185062, -9605, -5499, 15640, false, 0);

                GrandBossManager.getInstance().addBoss((L2GrandBossInstance) _lilithBoss);

                startQuestTimer("end_lilith", 60 * 60000, null, null); //1h
            }

            if (_debug)
            {
                allPlayers.add(player);
            }
            else
            {
                allPlayers.addAll(Config.LILITH_MIN_PLAYERS > Config.MAX_MEMBERS_IN_PARTY ||
                        player.getParty().isInCommandChannel() ? player.getParty().getCommandChannel().getMembers() :
                        player.getParty().getPartyMembers());
            }

            Location enterLoc = npcId == _enterCubic ? _enterLoc : _enterLilithLoc;
            for (L2PcInstance enterPlayer : allPlayers)
            {
                if (enterPlayer == null)
                {
                    continue;
                }

                if (npcId == _lilithCubic)
                {
                    _bossZone.allowPlayerEntry(enterPlayer, 7200);
                }
                else
                {
                    _preLilithZone.allowPlayerEntry(enterPlayer, 7200);
                }

                enterPlayer.teleToLocation(enterLoc, true);
            }
        }
        else if (npc.getNpcId() == _exitCubic)
        {
            player.teleToLocation(TeleportWhereType.Town);
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

        if (event.equalsIgnoreCase("unlock_lilith"))
        {
            GrandBossManager.getInstance().setBossStatus(_lilithId, GrandBossManager.getInstance().ALIVE);
        }
        else if (event.equalsIgnoreCase("check_activity_task"))
        {
            if (!GrandBossManager.getInstance().isActive(_lilithId, _lastAction))
            {
                notifyEvent("end_lilith", null, null);
            }
        }
        else if (event.equalsIgnoreCase("spawn_remant"))
        {
            List<L2Spawn> spawns = SpawnTable.getInstance()
                    .getSpecificSpawns("pre_lilith"); //Can be moved into a global script var, testing

            L2Spawn randomSpawn = null;

            if (npc == null)
            {
                for (int i = 0; i < 2; i++)
                {
                    randomSpawn = spawns.get(Rnd.get(spawns.size()));
                    if (randomSpawn != null)
                    {
                        L2Npc remnant = addSpawn(_remnant, randomSpawn.getX(), randomSpawn.getY(), randomSpawn.getZ(),
                                randomSpawn.getHeading(), true, 0, false, 0);
                        _remnants.add(remnant);
                    }
                }
            }
            else
            {
                randomSpawn = spawns.get(Rnd.get(spawns.size()));
                if (randomSpawn != null)
                {
                    npc.teleToLocation(randomSpawn.getX(), randomSpawn.getY(), randomSpawn.getZ());
                    npc.setSpawn(randomSpawn);
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

            QuestTimer forceEnd = getQuestTimer("end_lilith", null, null);
            if (forceEnd != null)
            {
                forceEnd.cancel();
            }
        }
        else if (event.equalsIgnoreCase("end_lilith"))
        {
            notifyEvent("cancel_timers", null, null);

            if (_lilithBoss != null)
            {
                _lilithBoss.deleteMe();
            }

            _bossZone.oustAllPlayers();

            _preLilithZone.oustAllPlayers();

            SpawnTable.getInstance().despawnSpecificTable("pre_lilith");

            for (L2Npc remnant : _remnants)
            {
                if (remnant == null)
                {
                    continue;
                }

                remnant.deleteMe();
            }

            if (GrandBossManager.getInstance().getBossStatus(_lilithId) != GrandBossManager.getInstance().DEAD)
            {
                GrandBossManager.getInstance().setBossStatus(_lilithId, GrandBossManager.getInstance().ALIVE);
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

        _lastAction = System.currentTimeMillis();

        if (npc.isMinion() || npc.isRaid())//Lilith and minions
        {
            //Anti BUGGERS
            if (!_bossZone.isInsideZone(attacker)) //Character attacking out of zone
            {
                attacker.doDie(null);

                if (_debug)
                {
                    Log.warning(getName() + ": Character: " + attacker.getName() + " attacked: " + npc.getName() +
                            " out of the boss zone!");
                }
            }

            if (!_bossZone.isInsideZone(npc)) //Npc moved out of the zone
            {
                L2Spawn spawn = npc.getSpawn();

                if (spawn != null)
                {
                    npc.teleToLocation(spawn.getX(), spawn.getY(), spawn.getZ());
                }

                if (_debug)
                {
                    Log.warning(getName() + ": Character: " + attacker.getName() + " attacked: " + npc.getName() +
                            " wich is out of the boss zone!");
                }
            }
        }

        if (npc.getNpcId() == _remnant)
        {
            if (npc.getCurrentHp() < npc.getMaxHp() * 0.30)
            {
                if (!npc.isCastingNow() && Rnd.get(100) > 95)
                {
                    npc.doCast(_remantTele);
                }
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

        if (npc.getNpcId() == _lilithId)
        {
            GrandBossManager.getInstance().notifyBossKilled(_lilithId);

            notifyEvent("cancel_timers", null, null);

            addSpawn(_exitCubic, 185062, -9605, -5499, 15640, false, 900000); //15min

            startQuestTimer("unlock_lilith", GrandBossManager.getInstance().getUnlockTime(_lilithId), null, null);

            startQuestTimer("end_lilith", 900000, null, null);
        }
        else if (npc.getNpcId() == _remnant)
        {
            _remnants.remove(npc);

            if (_remnants.isEmpty())
            {
                addSpawn(_lilithCubic, -19410, 23805, -4903, 62917, false, 60 * 60000, false, 0);
            }
        }

        return super.onKill(npc, killer, isPet);
    }

    @Override
    public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onSpellFinished: " + npc.getName());
        }

        if (npc.getNpcId() == _remnant && _preLilithZone.isInsideZone(npc))
        {
            if (skill == _remantTele)
            {
                notifyEvent("spawn_remant", npc, null);
            }
        }
        return super.onSpellFinished(npc, player, skill);
    }

    @Override
    public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onSkillSee: " + npc.getName());
        }

        if (Util.contains(_lilithMinions, npc.getNpcId()) && Rnd.get(2) == 1)
        {
            if (skill.getSkillType().toString().contains("HEAL"))
            {
                if (!npc.isCastingNow() && npc.getTarget() != npc && npc.getTarget() != caster &&
                        npc.getTarget() != _lilithBoss) //Don't call minions if are healing Lilith
                {
                    ((L2Attackable) npc).clearAggroList();
                    npc.setTarget(caster);
                    ((L2Attackable) npc).addDamageHate(caster, 500, 99999);
                    npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, caster);
                }
            }
        }

        return super.onSkillSee(npc, caster, skill, targets, isPet);
    }

    @Override
    public String onSpawn(L2Npc npc)
    {
        if (_debug)
        {
            Log.warning(getName() + ": onSpawn: " + npc.getName() + ": " + npc.getX() + ", " + npc.getY() + ", " +
                    npc.getZ());
        }

        return super.onSpawn(npc);
    }

    public static void main(String[] args)
    {
        new Lilith(-1, _qn, "ai/individual/GrandBosses");
    }
}
