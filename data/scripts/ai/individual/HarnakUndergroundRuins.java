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
package ai.individual;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.network.serverpackets.ExSendUIEvent;
import l2server.gameserver.network.serverpackets.ExSendUIEventRemove;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.util.Rnd;
import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 * 
 * Harnak Underground Ruins Spawn System: Normal Mobs <-> Demonic Mobs
 */

public class HarnakUndergroundRuins extends L2AttackableAIScript
{
	private static final int[] 				_normalMobs = {22931, 22932, 22933, 22934, 22935, 22936, 22937,22938, 23349};
	private static Map<L2ZoneType, zoneInfo> _roomInfo 	= new HashMap<L2ZoneType, zoneInfo>(24);
	
	public HarnakUndergroundRuins(int id, String name, String descr)
	{
		super(id, name, descr);
		
		for (int zoneId = 60028; zoneId <= 60051; zoneId ++)
		{
			L2ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
			_roomInfo.put(zone, new zoneInfo());
			
			//Spawn the normal mobs here
			SpawnTable.getInstance().spawnSpecificTable(zone.getName().replace(" ", "_"));
		}
		
		for (int npc : _normalMobs)
		{
			addKillId(npc);
			addSpawnId(npc);
		}
	}

	private static final class zoneInfo
	{
		private int currentPoints 				= 0;
		private int currentMonitorizedDamage	= 0;
		private int zoneStage 					= 0;
		
		private void setZoneStage(int a)
		{
			zoneStage = a;
		}
		
		private void setCurrentPoint(int a)
		{
			currentPoints = a;
		}
		
		private void setMonitorizedDamage(int a)
		{
			currentMonitorizedDamage = a;
		}
		
		private int getZoneStage()
		{
			return zoneStage;
		}
		
		private int getCurrentPoints()
		{
			return currentPoints;
		}
		
		private int getCurrentMonitorizedDamage()
		{
			return currentMonitorizedDamage;
		}
		
		private void reset()
		{
			currentPoints 				= 0;
			currentMonitorizedDamage	= 0;
			zoneStage 					= 0;
		}
	}
	
	private static final class changeZoneStage implements Runnable
	{
		private final L2ZoneType zone;
		
		public changeZoneStage(L2ZoneType a)
		{
			zone = a;
		}
		
		@Override
		public void run()
		{
			try
			{
				zoneInfo currentInfo = _roomInfo.get(zone);
				switch (currentInfo.getZoneStage())
				{
					case 0:
						zone.broadcastPacket(new ExShowScreenMessage(1600064, 3000));	//Monitor the damage for 30 sec.
						break;
						
					case 1:
						zone.broadcastPacket(new ExShowScreenMessage(1600065, 3000));	//25 seconds left!
						break;
						
					case 2:
						zone.broadcastPacket(new ExShowScreenMessage(1600067, 3000));	//20 seconds left!
						break;
						
					case 3:
						zone.broadcastPacket(new ExShowScreenMessage(1600068, 3000));	//15 seconds left!
						break;
						
					case 4:
						zone.broadcastPacket(new ExShowScreenMessage(1600069, 3000));	//10 seconds left!
						break;
						
					case 5:
						zone.broadcastPacket(new ExShowScreenMessage(1600070, 3000));	//5 seconds left!
						break;
						
					case 6:
						//Monitorize damage check
						if (currentInfo.getCurrentMonitorizedDamage() >= 10)
						{
							//Success
							zone.broadcastPacket(new ExShowScreenMessage(1600071, 3000));	//Demonic System will activate.
							
							//change spawns from that zone
							SpawnTable.getInstance().despawnSpecificTable(zone.getName().replace(" ", "_"));
							SpawnTable.getInstance().spawnSpecificTable(zone.getName().replace(" ", "_").concat("_demonic"));
							
							//Zones is demonic now
							zone.broadcastPacket(new ExSendUIEvent(0, 0, 600, 0, 1802319));	//Demonic System Activated
							
							currentInfo.setZoneStage(7);
							
							ThreadPoolManager.getInstance().scheduleGeneral(new changeZoneStage(zone), 600000);	//10min
						}
						else
						{
							//Fail, reset it.
							currentInfo.reset();
							return;
						}
						break;
						
					case 7:
						currentInfo.reset();
						
						zone.broadcastPacket(new ExSendUIEventRemove());
						
						SpawnTable.getInstance().despawnSpecificTable(zone.getName().replace(" ", "_").concat("_demonic"));
						SpawnTable.getInstance().spawnSpecificTable(zone.getName().replace(" ", "_"));
						return;
				}
				
				if (currentInfo.getZoneStage() < 6)
				{
					currentInfo.setZoneStage(currentInfo.getZoneStage() + 1);
					
					ThreadPoolManager.getInstance().scheduleGeneral(new changeZoneStage(zone), 5000);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		for (Entry<L2ZoneType, zoneInfo> currentZone : _roomInfo.entrySet())
		{
			if (currentZone.getKey().isInsideZone(npc))
			{
				zoneInfo currentInfo = currentZone.getValue();
				
				int currentPoints = currentInfo.getCurrentPoints();
				if (currentPoints == 300)	//Zone is already at max points
				{
					if (currentInfo.getZoneStage() < 1)	//Zone is waiting for monitoring stage but not in yet
						return super.onKill(npc, killer, isPet);
					
					//At this point this room is monitoring the damage
					int currentDamage = currentInfo.getCurrentMonitorizedDamage();
					
					int calcDamage = currentDamage + 1;
					if (calcDamage >= 10)
						calcDamage = 10;

					currentInfo.setMonitorizedDamage(calcDamage);
					currentZone.getKey().broadcastPacket(new ExSendUIEvent(5, calcDamage, 10, 1802318));	//Monitor the Damage
					
					return super.onKill(npc, killer, isPet);
				}
				
				int calcPoints = currentPoints + 1;
				if (calcPoints >= 300)
				{
					//At this point the Zone should go to Monitor Damage Stage.
					calcPoints = 300;
					
					ThreadPoolManager.getInstance().scheduleGeneral(new changeZoneStage(currentZone.getKey()), 1000);
				}
				
				currentInfo.setCurrentPoint(calcPoints);
				currentZone.getKey().broadcastPacket(new ExSendUIEvent(5, calcPoints, 300, 1802322));	//Danger Increasing. Danger Increasing.
			}
		}
		
		if (npc.getDisplayEffect() > 0)
		{
			L2MonsterInstance copy = (L2MonsterInstance) addSpawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), 0, true, 0 , false);
			copy.setTarget(killer);
			copy.addDamageHate(killer, 500, 99999);
			copy.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		if (Rnd.get(20) > 15)
			npc.setDisplayEffect(1);
		
		return super.onSpawn(npc);
	}

	@Override
	public int getOnKillDelay(int npcId)
	{
		return 0;
	}
	
	public static void main(String[] args)
	{
		new HarnakUndergroundRuins(-1, "HarnakUndergroundRuins", "ai");
	}
}
