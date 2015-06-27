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
import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 * 
 * Baium AI (Based on Fulminus work)
 * 
 *  Source:
 * 			- http://www.youtube.com/watch?v=xljlWxSQpM0
 */

public class Baium extends L2AttackableAIScript
{
	//Quest
	private static final boolean	_debug 	= false;
	private static final String		_qn		= "Baium";

	//Id's
	private static final int		_liveBaium	= 29020;
	private static final int		_stoneBaium	= 29025;
	private static final int 		_archangel	= 29021;
	private static final int		_vortex		= 31862;
	private static final int 		_exitCubic	= 31842;
	private static final int[]		_allMobs	= {_liveBaium, _stoneBaium, _archangel};
	private static final L2BossZone	_bossZone 	= GrandBossManager.getInstance().getZone(113100, 14500, 10077);
	private static final L2Skill	_baiumGift	= SkillTable.getInstance().getInfo(4136, 1);
	
	//Others
	private static long		_lastAction;
	private static L2Npc	_baiumBoss;
	private static L2PcInstance _firstAttacker;
	
	public Baium(int id, String name, String descr)
	{ 
		super(id, name, descr);
		
		addStartNpc(_vortex);
		addTalkId(_vortex);
		
		addStartNpc(_stoneBaium);
		addTalkId(_stoneBaium);
		
		addStartNpc(_exitCubic);
		addTalkId(_exitCubic);
		
		addSpawnId(_archangel);
		
		for (int i : _allMobs)
		{
			addAttackId(i);
			addKillId(i);
		}
		
		//Unlock
		startQuestTimer("unlock_baium", GrandBossManager.getInstance().getUnlockTime(_liveBaium), null, null);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (_debug)
			Log.warning(getName() + ": onTalk: " + player.getName());
		
		if (npc.getNpcId() == _vortex)
		{
			int baiumStatus = GrandBossManager.getInstance().getBossStatus(_liveBaium);
			
			final List<L2PcInstance> allPlayers = new ArrayList<L2PcInstance>();
			
			if (baiumStatus == GrandBossManager.getInstance().DEAD)
				return "31862-02.html";
			else
			{
				if (!_debug)
				{
					if (baiumStatus == GrandBossManager.getInstance().ALIVE && !InstanceManager.getInstance().checkInstanceConditions(player, -1, Config.BAIUM_MIN_PLAYERS, 200, 76, 84))
						return null;
					else if (baiumStatus == GrandBossManager.getInstance().WAITING && !InstanceManager.getInstance().checkInstanceConditions(player, -1, Config.BAIUM_MIN_PLAYERS, 200, 76, 84))
						return null;
					else if (baiumStatus == GrandBossManager.getInstance().FIGHTING)
						return "31862-01.html";
				}	
			}
			
			if (baiumStatus == GrandBossManager.getInstance().ALIVE)
			{
				GrandBossManager.getInstance().setBossStatus(_liveBaium, GrandBossManager.getInstance().WAITING);
				
				_lastAction = System.currentTimeMillis();
				
				startQuestTimer("check_activity_task", 60000, null, null, true);
			}
			
			if (_debug)
				allPlayers.add(player);
			else
				allPlayers.addAll(Config.BAIUM_MIN_PLAYERS > Config.MAX_MEMBERS_IN_PARTY ? player.getParty().getCommandChannel().getMembers() : player.getParty().getCommandChannel() != null ? player.getParty().getCommandChannel().getMembers() : player.getParty().getPartyMembers());
			
			for (L2PcInstance enterPlayer : allPlayers)
			{
				if (enterPlayer == null)
					continue;
				
				_bossZone.allowPlayerEntry(enterPlayer, 30);
				
				enterPlayer.teleToLocation(113100, 14500, 10077, true);
			}
		}
		else if (npc.getNpcId() == _stoneBaium)
		{
			notifyEvent("wake_up_baium", npc, player);
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
		
		if (event.equalsIgnoreCase("unlock_baium"))
		{
			_baiumBoss = addSpawn(_stoneBaium, 116033, 17447, 10104, 40188, false, 0);
			
			GrandBossManager.getInstance().setBossStatus(_liveBaium, GrandBossManager.getInstance().ALIVE);
		}
		else if (event.equalsIgnoreCase("check_activity_task"))
		{
			if (!GrandBossManager.getInstance().isActive(_liveBaium, _lastAction))
				notifyEvent("end_baium", null, null);
		}
		else if (event.equalsIgnoreCase("wake_up_baium"))
		{
			if (GrandBossManager.getInstance().getBossStatus(_liveBaium) == GrandBossManager.getInstance().WAITING)
			{
				npc.deleteMe();
				
				GrandBossManager.getInstance().setBossStatus(_liveBaium, GrandBossManager.getInstance().FIGHTING);
				
				_baiumBoss = addSpawn(_liveBaium, 116033, 17447, 10107, -25348, false, 0);
			
				_baiumBoss.setIsInvul(true);
				
				_baiumBoss.disableCoreAI(true);
				
				_baiumBoss.setRunning();
				
				_firstAttacker = player;
				
				GrandBossManager.getInstance().addBoss((L2GrandBossInstance) _baiumBoss);
				
				_bossZone.sendDelayedPacketToZone(50, new SocialAction(_baiumBoss.getObjectId(), 2));
				
				startQuestTimer("wake_up_intro_1", 5000, _baiumBoss, null);
			}
		}
		else if (event.equalsIgnoreCase("wake_up_intro_1"))
		{
			_bossZone.broadcastPacket(new Earthquake(_baiumBoss.getX(), _baiumBoss.getY(), _baiumBoss.getZ(), 40, 10));
			_bossZone.broadcastPacket(new PlaySound("BS02_A"));
			_bossZone.sendDelayedPacketToZone(8000, new SocialAction(_baiumBoss.getObjectId(), 3));
			
			startQuestTimer("baium_spawn_minions", 17000, null, null);
		}
		else if (event.equalsIgnoreCase("baium_spawn_minions"))
		{
			_baiumBoss.broadcastPacket(new SocialAction(_baiumBoss.getObjectId(), 1));
			
			if (!_firstAttacker.isOnline() || !_bossZone.isInsideZone(_firstAttacker))	//Get random one in case
				_firstAttacker = _baiumBoss.getKnownList().getKnownPlayers().get(Rnd.get(_baiumBoss.getKnownList().getKnownPlayers().size()));	//if is empty...
			
			if (!_firstAttacker.isInsideRadius(_baiumBoss, _baiumGift.getEffectRange(), false, false))
				_firstAttacker.teleToLocation(115910, 17337, 10105);
			
			if (_firstAttacker != null)
			{	
				_baiumBoss.setTarget(_firstAttacker);
			
				_baiumBoss.doCast(_baiumGift);
			
				_baiumBoss.broadcastPacket(new CreatureSay(_baiumBoss.getObjectId(), 0, _baiumBoss.getName(), _firstAttacker.getName() + ", How dare you wake me! Now you shall die!"));
			}
			
			for (L2PcInstance players : _bossZone.getPlayersInside())
			{
				if (players == null || !players.isHero())
					continue;
				
				_bossZone.broadcastPacket(new ExShowScreenMessage("Not even the gods themselves could touch me. But you, $s1, you dare challenge me?! Ignorant mortal!".replace("$1", players.getName()), 4000));//1000521
			}
			
			SpawnTable.getInstance().spawnSpecificTable("baium_minions");
			
			startQuestTimer("minions_attack_task", 60000, null, null, true);
			
			_baiumBoss.setIsInvul(false);
			
			_baiumBoss.disableCoreAI(false);
		}
		else if (event.equalsIgnoreCase("minions_attack_task"))
		{
			//Let's do it simple, minions should attack baium & players, by default due the enemy clan attacks almost all time baium instead of players so call this each time..
			List<L2PcInstance> insidePlayers = _bossZone.getPlayersInside();
			L2Character target = null;
			
			if (insidePlayers != null && !insidePlayers.isEmpty())
			{
				for (L2Npc zoneMob : _bossZone.getNpcsInside())
				{
					if (!(zoneMob instanceof L2MonsterInstance))
						continue;
					
					if (zoneMob.getTarget() != null)	//Only if default core ai are doing some shit
					{
						if (zoneMob.getTarget() == _baiumBoss)
							target = insidePlayers.get(Rnd.get(insidePlayers.size()));
						else
						{	
							//Lets use that code to take a lil look into the baiums target, if baim is attacking a minion set a random player as a target
							if (zoneMob == _baiumBoss && zoneMob.getTarget() instanceof L2MonsterInstance)
								target = insidePlayers.get(Rnd.get(insidePlayers.size()));
							else
								target = _baiumBoss;
						}
						if (target != null)
						{
							((L2Attackable)zoneMob).getAggroList().clear();
							zoneMob.setTarget(target);
							((L2MonsterInstance)zoneMob).addDamageHate(target, 500, 99999);
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
				activityTimer.cancel();
			
			QuestTimer minionsTimer = getQuestTimer("minions_attack_task", null, null);
			if (minionsTimer != null)
				minionsTimer.cancel();
		}
		else if (event.equalsIgnoreCase("end_baium"))
		{
			notifyEvent("cancel_timers", null, null);
			
			_bossZone.oustAllPlayers();
			
			if (_baiumBoss != null)
				_baiumBoss.deleteMe();
			
			SpawnTable.getInstance().despawnSpecificTable("baium_minions");
			
			if (GrandBossManager.getInstance().getBossStatus(_liveBaium) != GrandBossManager.getInstance().DEAD)
			{
				GrandBossManager.getInstance().setBossStatus(_liveBaium, GrandBossManager.getInstance().ALIVE);
				_baiumBoss = addSpawn(_stoneBaium, 116033, 17447, 10104, 40188, false, 0);
			}	
		}
		else if (event.equalsIgnoreCase("31862-03.html"))
		{
			return event;
		}
		
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack (L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (_debug)
			Log.warning(getName() + ": onAttack: " + npc.getName());
		
		_lastAction = System.currentTimeMillis();
		
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
		
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (_debug)
			Log.warning(getName() + ": onKill: " + npc.getName());
		
		if (npc.getNpcId() == _liveBaium)
		{
			GrandBossManager.getInstance().notifyBossKilled(_liveBaium);
			
			notifyEvent("cancel_timers", null, null);
			
			SpawnTable.getInstance().despawnSpecificTable("baium_minions");
			
			_bossZone.broadcastPacket(new PlaySound("BS01"));
			
			addSpawn(_exitCubic, 115017, 15549, 10090, 0, false, 600000);	//10min
			
			startQuestTimer("unlock_baium", GrandBossManager.getInstance().getUnlockTime(_liveBaium), null, null);
			
			startQuestTimer("end_baium", 900000, null, null);
		}
		
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if (_debug)
			Log.warning(getName() + ": onSpawn: " + npc.getName());
		
		npc.setIsRunning(true);
		((L2Attackable)npc).setIsRaidMinion(true);
		
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new Baium(-1, _qn, "ai/individual/GrandBosses");
	}
}
