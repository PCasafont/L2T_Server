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
package ai.individual.GrandBosses.Anakim;

import java.util.ArrayList;
import java.util.List;

import l2tserver.Config;
import l2tserver.gameserver.ai.CtrlIntention;
import l2tserver.gameserver.datatables.SkillTable;
import l2tserver.gameserver.datatables.SpawnTable;
import l2tserver.gameserver.datatables.MapRegionTable.TeleportWhereType;
import l2tserver.gameserver.instancemanager.GrandBossManager;
import l2tserver.gameserver.instancemanager.InstanceManager;
import l2tserver.gameserver.model.L2Object;
import l2tserver.gameserver.model.L2Skill;
import l2tserver.gameserver.model.L2Spawn;
import l2tserver.gameserver.model.Location;
import l2tserver.gameserver.model.actor.L2Attackable;
import l2tserver.gameserver.model.actor.L2Npc;
import l2tserver.gameserver.model.actor.instance.L2GrandBossInstance;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.model.quest.QuestTimer;
import l2tserver.gameserver.model.zone.type.L2BossZone;
import l2tserver.gameserver.util.Util;
import l2tserver.log.Log;
import l2tserver.util.Rnd;
import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 * 
 * Anakim AI
 * 
 *  Source:
 * 			- http://www.youtube.com/watch?v=LecymFTJQzQ
 * 			- https://www.youtube.com/watch?v=Vi-bf6p9H8s
 * 			- http://www.youtube.com/watch?v=YkinCX2ppyA
 * 			- http://boards.lineage2.com/showpost.php?p=3386784&postcount=6
 */

public class Anakim extends L2AttackableAIScript
{
	//Quest
	private static final boolean	_debug 	= false;
	private static final String		_qn		= "Anakim";

	//Id's
	private static final int		_anakimId		= 25286;
	private static final int		_remnant		= 19490;
	private static final int		_enterCubic		= 31101;
	private static final int 		_exitCubic		= 31109;
	private static final int		_anakimCubic 	= 31111;
	private static final int[]		_anakimMinions	= {25287, 25288, 25289};
	private static final int[]		_necroMobs		= {21199, 21200, 21201, 21202, 21203, 21204, 21205, 21206, 21207};
	private static final L2Skill	_remantTele		= SkillTable.getInstance().getInfo(23303, 1);
	private static final Location	_enterLoc		= new Location(172420, -17602, -4906);
	private static final Location	_enterAnakimLoc	= new Location(184569, -12134, -5499);
	private static final int[]		_allMobs		= {_anakimId, _anakimMinions[0], _anakimMinions[1], _anakimMinions[2], _necroMobs[0], _necroMobs[1], _necroMobs[2], _necroMobs[3], _necroMobs[4], _necroMobs[5], _necroMobs[6], _necroMobs[7], _necroMobs[8], _remnant};	
	private static final L2BossZone	_bossZone 		= GrandBossManager.getInstance().getZone(185084, -12598, -5499);
	private static final L2BossZone _preAnakimZone 	= GrandBossManager.getInstance().getZone(172679, -17486, -4906);
	
	//Others
	private static List<L2Npc>	_remnants = new ArrayList<L2Npc>();
	private static long		_lastAction;
	private static L2Npc	_anakimBoss;
	
	public Anakim(int id, String name, String descr)
	{ 
		super(id, name, descr);
		
		addStartNpc(_enterCubic);
		addTalkId(_enterCubic);
		
		addStartNpc(_exitCubic);
		addTalkId(_exitCubic);
		
		addStartNpc(_anakimCubic);
		addTalkId(_anakimCubic);
		
		addSpawnId(_remnant);
		addSpellFinishedId(_remnant);
		
		for (int i : _allMobs)
		{
			addAttackId(i);
			addKillId(i);
			addSkillSeeId(i);
		}
		
		//Unlock
		startQuestTimer("unlock_anakim", GrandBossManager.getInstance().getUnlockTime(_anakimId), null, null);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (_debug)
			Log.warning(getName() + ": onTalk: " + player.getName());
		
		int npcId = npc.getNpcId();
		
		if (npcId == _enterCubic || npcId == _anakimCubic)
		{
			int _anakimStatus = GrandBossManager.getInstance().getBossStatus(_anakimId);
			
			final List<L2PcInstance> allPlayers = new ArrayList<L2PcInstance>();
			
			if (_anakimStatus == GrandBossManager.getInstance().DEAD)
				return "31101-01.html";
			else
			{
				if (!_debug)
				{
					if (_anakimStatus == GrandBossManager.getInstance().ALIVE && !InstanceManager.getInstance().checkInstanceConditions(player, -1, Config.ANAKIM_MIN_PLAYERS, 100, 99, 105))
						return null;
					else if (_anakimStatus == GrandBossManager.getInstance().WAITING && !InstanceManager.getInstance().checkInstanceConditions(player, -1, Config.ANAKIM_MIN_PLAYERS, 100, 99, 105))
						return null;
					else if (_anakimStatus == GrandBossManager.getInstance().FIGHTING)
						return "31101-01.html";
				}	
			}
			
			if (_anakimStatus == GrandBossManager.getInstance().ALIVE && npcId == _enterCubic)
			{
				GrandBossManager.getInstance().setBossStatus(_anakimId, GrandBossManager.getInstance().WAITING);
				
				SpawnTable.getInstance().spawnSpecificTable("pre_anakim");
				
				_remnants.clear();
				
				notifyEvent("spawn_remant", null, null);
				
				_lastAction = System.currentTimeMillis();
				
				startQuestTimer("check_activity_task", 60000, null, null, true);
			}
			else if (_anakimStatus == GrandBossManager.getInstance().WAITING && npcId == _anakimCubic)
			{
				if (!_remnants.isEmpty())
					return "";
				
				GrandBossManager.getInstance().setBossStatus(_anakimId, GrandBossManager.getInstance().FIGHTING);
				
				//Spawn the rb
				_anakimBoss = addSpawn(_anakimId, 185080, -12613, -5499, 16550, false, 0);
				
				GrandBossManager.getInstance().addBoss((L2GrandBossInstance) _anakimBoss);
				
				startQuestTimer("end_anakim", 60 * 60000, null, null);	//1h
			}
			
			if (_debug)
				allPlayers.add(player);
			else
				allPlayers.addAll(Config.ANAKIM_MIN_PLAYERS > Config.MAX_MEMBERS_IN_PARTY ? player.getParty().getCommandChannel().getMembers() : player.getParty().getPartyMembers());
			
			Location enterLoc = npcId == _enterCubic ? _enterLoc : _enterAnakimLoc;		
			for (L2PcInstance enterPlayer : allPlayers)
			{
				if (enterPlayer == null)
					continue;
				
				if (npcId == _anakimCubic)
					_bossZone.allowPlayerEntry(enterPlayer, 7200);
				else
					_preAnakimZone.allowPlayerEntry(enterPlayer, 7200);
				
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
			Log.warning(getName() +  ": onAdvEvent: " + event);
		
		if (event.equalsIgnoreCase("unlock_anakim"))
		{
			GrandBossManager.getInstance().setBossStatus(_anakimId, GrandBossManager.getInstance().ALIVE);
		}
		else if (event.equalsIgnoreCase("check_activity_task"))
		{
			if (!GrandBossManager.getInstance().isActive(_anakimId, _lastAction))
				notifyEvent("end_anakim", null, null);
		}
		else if (event.equalsIgnoreCase("spawn_remant"))
		{
			List<L2Spawn> spawns = SpawnTable.getInstance().getSpecificSpawns("pre_anakim");	//Can be moved into a global script var, testing
			
			L2Spawn randomSpawn = null;
			
			if (npc == null)
			{
				for (int i = 0; i < 2; i++)
				{
					randomSpawn = spawns.get(Rnd.get(spawns.size()));
					if (randomSpawn != null)
					{	
						L2Npc remnant = addSpawn(_remnant, randomSpawn.getX(), randomSpawn.getY(), randomSpawn.getZ(), randomSpawn.getHeading(), true, 0, false, 0);
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
				activityTimer.cancel();
			
			QuestTimer forceEnd = getQuestTimer("end_anakim", null, null);
			if (forceEnd != null)
				forceEnd.cancel();
		}
		else if (event.equalsIgnoreCase("end_anakim"))
		{
			notifyEvent("cancel_timers", null, null);
			
			if (_anakimBoss != null)
				_anakimBoss.deleteMe();
			
			_bossZone.oustAllPlayers();
			
			_preAnakimZone.oustAllPlayers();
			
			SpawnTable.getInstance().despawnSpecificTable("pre_anakim");
			
			for (L2Npc remnant : _remnants)
			{
				if (remnant == null)
					continue;
				
				remnant.deleteMe();
			}
			
			if (GrandBossManager.getInstance().getBossStatus(_anakimId) != GrandBossManager.getInstance().DEAD)
				GrandBossManager.getInstance().setBossStatus(_anakimId, GrandBossManager.getInstance().ALIVE);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack (L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (_debug)
			Log.warning(getName() + ": onAttack: " + npc.getName());
		
		_lastAction = System.currentTimeMillis();
		
		if (npc.isMinion() || npc.isRaid())//Anakim and minions
		{	
			//Anti BUGGERS
			if (!_bossZone.isInsideZone(attacker))	//Character attacking out of zone
			{
				attacker.doDie(null);
							
				if (_debug)
					Log.warning(getName() + ": Character: " + attacker.getName() + " attacked: " + npc.getName() + " out of the boss zone!");
			}
						
			if (!_bossZone.isInsideZone(npc))	//Npc moved out of the zone 
			{
				L2Spawn spawn = npc.getSpawn();
						
				if (spawn != null)
					npc.teleToLocation(spawn.getX(), spawn.getY(), spawn.getZ());
							
				if (_debug)
					Log.warning(getName() + ": Character: " + attacker.getName() + " attacked: " + npc.getName() + " wich is out of the boss zone!");
			}
		}
		
		if (npc.getNpcId() == _remnant)
		{
			if (npc.getCurrentHp() < npc.getMaxHp() * 0.30)
			{
				if (!npc.isCastingNow() && Rnd.get(100) > 95)
					npc.doCast(_remantTele);
			}
		}
		
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (_debug)
			Log.warning(getName() + ": onKill: " + npc.getName());
		
		if (npc.getNpcId() == _anakimId)
		{
			GrandBossManager.getInstance().notifyBossKilled(_anakimId);
			
			notifyEvent("cancel_timers", null, null);
			
			addSpawn(_exitCubic, 185082, -12606, -5499, 6133, false, 900000);	//15min
			
			startQuestTimer("unlock_anakim", GrandBossManager.getInstance().getUnlockTime(_anakimId), null, null);
			
			startQuestTimer("end_anakim", 900000, null, null);
		}
		else if (npc.getNpcId() == _remnant)
		{
			_remnants.remove(npc);
			
			if (_remnants.isEmpty())
				addSpawn(_anakimCubic, 183225, -11911, -4897, 32768, false, 60 * 60000, false, 0);
		}
		
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if (_debug)
			Log.warning(getName() + ": onSpellFinished: " + npc.getName());
		
		if (npc.getNpcId() == _remnant  && _preAnakimZone.isInsideZone(npc))
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
			Log.warning(getName() + ": onSkillSee: " + npc.getName());
		
		if (Util.contains(_anakimMinions, npc.getNpcId()) && Rnd.get(2) == 1)
		{
			if (skill.getSkillType().toString().contains("HEAL"))
			{
				if (!npc.isCastingNow() && npc.getTarget() != npc && npc.getTarget() != caster && npc.getTarget() != _anakimBoss)	//Don't call minions if are healing Anakim
				{
					((L2Attackable)npc).clearAggroList();
					npc.setTarget(caster);
					((L2Attackable)npc).addDamageHate(caster, 500, 99999);
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
			Log.warning(getName() + ": onSpawn: " + npc.getName() + ": " + npc.getX() + ", " + npc.getY() + ", " + npc.getZ());
		
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new Anakim(-1, _qn, "ai/individual/GrandBosses");
	}
}
