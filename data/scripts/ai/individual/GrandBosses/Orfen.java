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

package ai.individual.GrandBosses;

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.GrandBossManager;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2GrandBossInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.model.zone.type.L2BossZone;
import l2server.gameserver.network.serverpackets.NpcSay;
import l2server.gameserver.network.serverpackets.PlaySound;
import l2server.log.Log;
import l2server.util.Rnd;
import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 *
 * Orfen AI (Based on Emperorc work)
 */

public class Orfen extends L2AttackableAIScript
{
	//Quest
	private static final boolean _debug = false;
	private static final String _qn = "Orfen";
	
	//Id's
	private static final int _orfenId = 29014;
	private static final int[] _textIds = { 1000028, 1000029, 1000030, 1000031 };
	private static final L2Skill _paralysis = SkillTable.getInstance().getInfo(4064, 1);
	private static final L2BossZone _bossZone = GrandBossManager.getInstance().getZone(43728, 17220, -4342);
	private static final Location[] _orfenLocs = { new Location(43728, 17220, -4342), new Location(55024, 17368, -5412), new Location(53504, 21248, -5486), new Location(53248, 24576, -5262) };
	
	//Others
	private L2Npc _orfenBoss;
	private static long _LastAction;
	private boolean _isTeleported;
	
	public Orfen(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addAttackId(_orfenId);
		addKillId(_orfenId);
		addSkillSeeId(_orfenId);
		
		//Unlock
		startQuestTimer("unlock_orfen", GrandBossManager.getInstance().getUnlockTime(_orfenId), null, null);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (_debug)
			Log.warning(getName() + ": onAdvEvent: " + event);
		
		if (event.equalsIgnoreCase("unlock_orfen"))
		{
			int rnd = Rnd.get(10);
			Location orfenLoc = null;
			
			if (rnd < 4)
				orfenLoc = _orfenLocs[1];
			else if (rnd < 7)
				orfenLoc = _orfenLocs[2];
			else
				orfenLoc = _orfenLocs[3];
			
			_orfenBoss = addSpawn(_orfenId, orfenLoc.getX(), orfenLoc.getY(), orfenLoc.getZ(), 0, false, 0);
			
			GrandBossManager.getInstance().addBoss((L2GrandBossInstance) _orfenBoss);
			
			GrandBossManager.getInstance().setBossStatus(_orfenId, GrandBossManager.getInstance().ALIVE);
			
			_orfenBoss.broadcastPacket(new PlaySound(1, "BS01_A", 1, _orfenBoss.getObjectId(), _orfenBoss.getX(), _orfenBoss.getY(), _orfenBoss.getZ()));
		}
		else if (event.equalsIgnoreCase("check_orfen_location"))
		{
			//Check the boss location and minion loc
			if ((_isTeleported && (_orfenBoss.getCurrentHp() > (_orfenBoss.getMaxHp() * 0.95))) || (!_bossZone.isInsideZone(_orfenBoss) && !_isTeleported))
			{
				setSpawnPoint(Rnd.get(3) + 1);
				_isTeleported = false;
			}
			else if (_isTeleported && !_bossZone.isInsideZone(_orfenBoss))
				setSpawnPoint(0);
		}
		else if (event.equalsIgnoreCase("check_activity_task"))
		{
			if (!GrandBossManager.getInstance().isActive(_orfenId, _LastAction))
				notifyEvent("end_orfen", null, null);
		}
		else if (event.equalsIgnoreCase("end_orfen"))
		{
			QuestTimer activityTimer = getQuestTimer("check_activity_task", null, null);
			if (activityTimer != null)
				activityTimer.cancel();
			
			QuestTimer checkOrfenLoc = getQuestTimer("check_orfen_location", null, null);
			if (checkOrfenLoc != null)
				checkOrfenLoc.cancel();
			
			if (GrandBossManager.getInstance().getBossStatus(_orfenId) != GrandBossManager.getInstance().DEAD)
				GrandBossManager.getInstance().setBossStatus(_orfenId, GrandBossManager.getInstance().ALIVE);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	public void setSpawnPoint(int index)
	{
		Location loc = _orfenLocs[index];
		
		((L2Attackable) _orfenBoss).clearAggroList();
		
		_orfenBoss.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
		
		L2Spawn spawn = _orfenBoss.getSpawn();
		spawn.setX(loc.getX());
		spawn.setY(loc.getY());
		spawn.setZ(loc.getZ());
		
		_orfenBoss.teleToLocation(loc.getX(), loc.getY(), loc.getZ());
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (_debug)
			Log.warning(getName() + ": onAttack: " + npc.getName());
		
		_LastAction = System.currentTimeMillis();
		
		if (GrandBossManager.getInstance().getBossStatus(_orfenId) == GrandBossManager.getInstance().ALIVE)
		{
			GrandBossManager.getInstance().setBossStatus(_orfenId, GrandBossManager.getInstance().FIGHTING);
			
			startQuestTimer("check_activity_task", 60000, null, null, true);
			
			startQuestTimer("check_orfen_location", 10000, null, null, true);
		}
		
		if (npc.getNpcId() == _orfenId)
		{
			if (!_isTeleported && ((npc.getCurrentHp() - damage) < (npc.getMaxHp() / 2)))
			{
				_isTeleported = true;
				setSpawnPoint(0);
			}
			else if (npc.isInsideRadius(attacker, 1000, false, false) && !npc.isInsideRadius(attacker, 300, false, false) && (Rnd.get(10) == 0))
			{
				attacker.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
				
				NpcSay packet = new NpcSay(npc.getObjectId(), 0, _orfenId, _textIds[Rnd.get(3)]);
				packet.addStringParameter(attacker.getName().toString());
				npc.broadcastPacket(packet);
				
				npc.setTarget(attacker);
				npc.doCast(_paralysis);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (_debug)
			Log.warning(getName() + ": onKill: " + npc.getName());
		
		if (npc.getNpcId() == _orfenId)
		{
			_orfenBoss.broadcastPacket(new PlaySound(1, "BS02_D", 1, _orfenBoss.getObjectId(), _orfenBoss.getX(), _orfenBoss.getY(), _orfenBoss.getZ()));
			
			GrandBossManager.getInstance().notifyBossKilled(_orfenId);
			
			notifyEvent("end_orfen", null, null);
			
			startQuestTimer("unlock_orfen", GrandBossManager.getInstance().getUnlockTime(_orfenId), null, null);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (npc.getNpcId() == _orfenId)
		{
			L2Character originalCaster = isPet ? caster.getPet() : caster;
			if ((skill.getAggroPoints() > 0) && (Rnd.get(5) == 0) && npc.isInsideRadius(originalCaster, 1000, false, false))
			{
				NpcSay packet = new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), _textIds[Rnd.get(_textIds.length)]);
				packet.addStringParameter(caster.getName().toString());
				npc.broadcastPacket(packet);
				
				originalCaster.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
				
				npc.setTarget(originalCaster);
				npc.doCast(_paralysis);
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	public static void main(String[] args)
	{
		new Orfen(-1, _qn, "ai/individual/GrandBosses");
	}
}
