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

import java.util.ArrayList;
import java.util.List;

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

/**
 * @author LasTravel
 * 
 * 	Kelbim AI
 * 
 *  Source:
 *  		- https://www.youtube.com/watch?v=qVkk2BJoGoU
 */

public class Kelbim extends L2AttackableAIScript
{
	//Quest
	private static final boolean	_debug 	= false;
	private static final String		_qn		= "Kelbim";
	
	//Ids
	private static final int	_npcEnterId 		= 34052;
	private static final int	_teleDevice			= 34053;
	private static final int	_kelbimShout 		= 19597;
	private static final int 	_kelbimId 			= 26124;
	private static final int	_guardianSinistra 	= 26126;
	private static final int	_guardianDestra		= 26127;
	private static final int[]	_kelbimGuardians	= {_guardianSinistra, _guardianDestra};
	private static final int	_kelbimGuard 		= 26129;
	private static final int	_kelbimAltar 		= 26130;
	private static final int[] _kelbimMinions 		= {_guardianSinistra, _guardianDestra, _kelbimGuard};
	private static final int[] _allMobs 			= {_kelbimId, _kelbimMinions[0], _kelbimMinions[1], _kelbimMinions[2], _kelbimAltar};
	private static final L2BossZone	_bossZone 		= GrandBossManager.getInstance().getZone(-55505, 58781, -274);
	private static final Location _enterCords		= new Location(-55386, 58939, -274);
	
	//Skills
	private static final L2Skill _meteorCrash 		= SkillTable.getInstance().getInfo(23692, 1);
	private static final L2Skill _waterDrop 		= SkillTable.getInstance().getInfo(23693, 1);
	private static final L2Skill _tornadoShackle 	= SkillTable.getInstance().getInfo(23694, 1);
	private static final L2Skill _flameThrower 		= SkillTable.getInstance().getInfo(23699, 1);
	private static final L2Skill[] _areaSkills 		= {_meteorCrash, _waterDrop, _tornadoShackle, _flameThrower};
	
	//Vars
	private static L2Npc _kelbimBoss;
	private static long	_lastAction;
	private static int _bossStage;
	private static ArrayList<L2Npc> _minions = new ArrayList<L2Npc>();
	
	public Kelbim(int id, String name, String descr)
	{ 
		super(id, name, descr);
		
		addTalkId(_npcEnterId);
		addStartNpc(_npcEnterId);
		
		addTalkId(_teleDevice);
		addStartNpc(_teleDevice);
		addFirstTalkId(_teleDevice);
		
		for (int i : _allMobs)
		{
			addAttackId(i);
			addKillId(i);
		}
		
		//Unlock
		long unlockTime = GrandBossManager.getInstance().getUnlockTime(_kelbimId);
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
		if (_debug)
			Log.warning(getName() + ": onFirstTalk: " + player.getName());
		
		if (npc.getNpcId() == _teleDevice)
		{
			player.teleToLocation(-55730, 55643, -1954);
		}
		return super.onFirstTalk(npc, player);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (_debug)
			Log.warning(getName() + ": onTalk: " + player.getName());
		
		int npcId = npc.getNpcId();
		if (npcId == _npcEnterId)
		{
			int kelbimStatus = GrandBossManager.getInstance().getBossStatus(_kelbimId);
			final List<L2PcInstance> allPlayers = new ArrayList<L2PcInstance>();
			if (kelbimStatus == GrandBossManager.getInstance().DEAD)
				return "34052-1.htm";
			else
			{
				if (!_debug)
				{	
					if (kelbimStatus == GrandBossManager.getInstance().ALIVE && !InstanceManager.getInstance().checkInstanceConditions(player, 101, Config.KELBIM_MIN_PLAYERS, 200, 95, Config.MAX_LEVEL))
						return null;
					else if (kelbimStatus == GrandBossManager.getInstance().WAITING && !InstanceManager.getInstance().checkInstanceConditions(player, 101, Config.KELBIM_MIN_PLAYERS, 200, 95, Config.MAX_LEVEL))
						return null;
					else if (kelbimStatus == GrandBossManager.getInstance().FIGHTING)
						return null;
				}	
			}
			
			if (kelbimStatus == GrandBossManager.getInstance().ALIVE)
			{
				GrandBossManager.getInstance().setBossStatus(_kelbimId, GrandBossManager.getInstance().WAITING);
				
				startQuestTimer("stage_1_start", 2 * 60000, null, null);
			}
			
			if (_debug)
				allPlayers.add(player);
			else
				allPlayers.addAll(Config.KELBIM_MIN_PLAYERS > Config.MAX_MEMBERS_IN_PARTY ? player.getParty().getCommandChannel().getMembers() : player.getParty().getPartyMembers());
			
			for (L2PcInstance enterPlayer : allPlayers)
			{
				if (enterPlayer == null)
					continue;
				
				_bossZone.allowPlayerEntry(enterPlayer, 30);
				
				enterPlayer.teleToLocation(_enterCords, true);
			}
		}
		return super.onTalk(npc, player);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (_debug)
			Log.warning(getName() +  ": onAdvEvent: " + event);
		
		if (event.equalsIgnoreCase("unlock_kelbim"))
		{
			GrandBossManager.getInstance().setBossStatus(_kelbimId, GrandBossManager.getInstance().ALIVE);
			
			Broadcast.toAllOnlinePlayers(new Earthquake(-55754, 59903, -269, 20, 10));
			
			DoorTable.getInstance().getDoor(18190002).openMe();
			DoorTable.getInstance().getDoor(18190004).openMe();
		}
		else if (event.equalsIgnoreCase("check_activity_task"))
		{
			if (!GrandBossManager.getInstance().isActive(_kelbimId, _lastAction))
				notifyEvent("end_kelbim", null, null);
		}
		else if (event.equalsIgnoreCase("stage_1_start"))
		{
			_bossStage = 1;
			
			GrandBossManager.getInstance().setBossStatus(_kelbimId, GrandBossManager.getInstance().FIGHTING);
			
			_bossZone.broadcastMovie(81);
			
			startQuestTimer("stage_1_kelbim_spawn", ScenePlayerDataTable.getInstance().getVideoDuration(81) + 2000, null, null);
		}
		else if (event.equalsIgnoreCase("stage_1_kelbim_spawn"))
		{
			_kelbimBoss = addSpawn(_kelbimId, -56340, 60801, -269, 54262, false, 0);
			GrandBossManager.getInstance().addBoss((L2GrandBossInstance)_kelbimBoss);
			
			_lastAction = System.currentTimeMillis();
			
			startQuestTimer("check_activity_task", 60000, null, null, true);
			
			startQuestTimer("stage_all_random_area_attack", Rnd.get(2, 3) * 60000, null, null);
		}
		else if (event.equalsIgnoreCase("stage_all_spawn_minions"))
		{
			for (int i = 0; i < Rnd.get((_bossStage * 5) / 2, (_bossStage * 5)); i++)
			{
				L2Npc minion = addSpawn(_kelbimGuard, _kelbimBoss.getX(), _kelbimBoss.getY(), _kelbimBoss.getZ(), 0, true, 0, true, 0);
				minion.setIsRunning(true);
				((L2Attackable)minion).setIsRaidMinion(true);
					
				_minions.add(minion);
			}
				
			for (int i = 0; i < Rnd.get((_bossStage * 2) / 2, (_bossStage * 2)); i++)
			{
				L2Npc minion = addSpawn(_kelbimGuardians[Rnd.get(_kelbimGuardians.length)], _kelbimBoss.getX(), _kelbimBoss.getY(), _kelbimBoss.getZ(), 0, true, 0, true, 0);
				minion.setIsRunning(true);
				((L2Attackable)minion).setIsRaidMinion(true);
					
				_minions.add(minion);
			}
		}
		else if (event.equalsIgnoreCase("stage_all_random_area_attack"))
		{
			if (_bossStage > 0 && _bossStage < 7)
			{
				if (_kelbimBoss.isInCombat())
				{
					L2Skill randomAttackSkill = _areaSkills[Rnd.get(_areaSkills.length)];
					ArrayList<L2Npc> _skillNpcs = new ArrayList<L2Npc>();
					for (L2PcInstance pl : _bossZone.getPlayersInside())
					{
						if (pl == null)
							continue;
						
						if (Rnd.get(100) > 40)
						{
							L2Npc skillMob = addSpawn(_kelbimShout, pl.getX(), pl.getY(), pl.getZ() + 10, 0, true, 60000, false, 0);
							_skillNpcs.add(skillMob);
							
							_minions.add(skillMob);
						}
					}
					
					for (L2Npc skillNpc : _skillNpcs)
					{
						if (skillNpc == null)
							continue;
						
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
				activityTimer.cancel();
		}
		else if (event.equalsIgnoreCase("end_kelbim"))
		{
			_bossStage = 0;
			
			notifyEvent("cancel_timers", null, null);
			
			_bossZone.oustAllPlayers();
			
			if (_kelbimBoss != null)
				_kelbimBoss.deleteMe();
			
			if (!_minions.isEmpty())
			{
				for (L2Npc minion : _minions)
				{
					if (minion == null)
						continue;
					
					minion.deleteMe();	
				}
			}
			
			_minions.clear();
			
			if (GrandBossManager.getInstance().getBossStatus(_kelbimId) != GrandBossManager.getInstance().DEAD)
				GrandBossManager.getInstance().setBossStatus(_kelbimId, GrandBossManager.getInstance().ALIVE);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack (L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (_debug)
			Log.warning(getName() + ": onAttack: " + npc.getName());
		
		if (npc.getNpcId() == _kelbimId)
		{
			_lastAction = System.currentTimeMillis();
			
			if (_bossStage == 1 && npc.getCurrentHp() < npc.getMaxHp() * 0.80)
			{
				_bossStage = 2;
				
				notifyEvent("stage_all_spawn_minions", null, null);
			}
			else if (_bossStage == 2 && npc.getCurrentHp() < npc.getMaxHp() * 0.60)
			{
				_bossStage = 3;
				
				notifyEvent("stage_all_spawn_minions", null, null);
			}
			else if (_bossStage == 3 && npc.getCurrentHp() < npc.getMaxHp() * 0.40)
			{
				_bossStage = 4;
				
				notifyEvent("stage_all_spawn_minions", null, null);
			}
			else if (_bossStage == 4 && npc.getCurrentHp() < npc.getMaxHp() * 0.20)
			{
				_bossStage = 5;
				
				notifyEvent("stage_all_spawn_minions", null, null);
			}
			else if (_bossStage == 5 && npc.getCurrentHp() < npc.getMaxHp() * 0.05)
			{
				_bossStage = 6;
				
				notifyEvent("stage_all_spawn_minions", null, null);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (_debug)
			Log.warning(getName() + ": onKill: " + npc.getName());
		
		if (npc.getNpcId() == _kelbimId)
		{
			_bossStage = 7;
			
			addSpawn(_teleDevice, -54331, 58331, -264, 16292, false, 1800000);
			
			GrandBossManager.getInstance().notifyBossKilled(_kelbimId);
			
			DoorTable.getInstance().getDoor(18190002).closeMe();
			DoorTable.getInstance().getDoor(18190004).closeMe();
			
			notifyEvent("cancel_timers", null, null);
			
			startQuestTimer("unlock_kelbim", GrandBossManager.getInstance().getUnlockTime(_kelbimId), null, null);
			startQuestTimer("end_kelbim", 1800000, null, null);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new Kelbim(-1, _qn, "ai/individual/GrandBosses");
	}
}
