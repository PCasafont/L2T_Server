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
package ai.individual.NervaPrison;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2DoorInstance;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.zone.L2ZoneType;
import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 * 
 * Prison AI
 * 
 * Source:
 * 			- http://l2wiki.com/Raiders_Crossroads
 * 			- https://4gameforum.com/showthread.php?t=23180
 */

public class NervaPrison extends L2AttackableAIScript
{
	private static final String _qn			= "NervaPrison";
	private static final int	_doorNpc	= 19459;
	private static final int	_kaysen		= 19458;
	private static final int	_nervaKey	= 36665;
	private static final int	_kaiser		= 23329;
	private static final Map<L2ZoneType, List<L2DoorInstance>> _prisons = new HashMap<L2ZoneType, List<L2DoorInstance>>();

	public NervaPrison(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addTalkId(_doorNpc);
		addStartNpc(_doorNpc);
		addFirstTalkId(_kaysen);
		addKillId(_kaiser);
		addSpawnId(_kaysen);
		
		for (int i = 60052; i <= 60059; i++)
		{
			List<L2DoorInstance> doors = new ArrayList<L2DoorInstance>(2);
			L2ZoneType zone = ZoneManager.getInstance().getZoneById(i);
			for (L2Character door : zone.getCharactersInside().values())
			{
				if (door instanceof L2DoorInstance)
					doors.add((L2DoorInstance) door);
			}
			
			_prisons.put(zone, doors);
		}
	}

	@Override
	public String onTalk(L2Npc npc,L2PcInstance player)
	{
		if (npc.getNpcId() == _doorNpc)
		{
			if (!player.destroyItemByItemId(_qn, _nervaKey, 1, player, true))
				return "19459-1.html";
			
			for (Entry<L2ZoneType, List<L2DoorInstance>> currentZone : _prisons.entrySet())
			{
				if (currentZone.getKey().isInsideZone(npc))
				{
					for (L2DoorInstance door : currentZone.getValue())
					{
						if (door.getOpen())
							return super.onFirstTalk(npc, player);	//Cheating
					}
						
					for (L2DoorInstance door : currentZone.getValue())
					{
						door.openMe();
					}
				}
			}
			npc.deleteMe();
		}
		return super.onTalk(npc, player);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (!npc.isDead() && npc.isInsideRadius(player, 50, false, false))
		{
			for (Entry<L2ZoneType, List<L2DoorInstance>> currentZone : _prisons.entrySet())
			{
				if (currentZone.getKey().isInsideZone(npc))
				{
					for (L2DoorInstance door : currentZone.getValue())
					{
						if (!door.getOpen())
							return super.onFirstTalk(npc, player);	//Cheating
					}
					
					for (L2DoorInstance door : currentZone.getValue())
					{
						door.closeMe();
					}
				}
			}
			
			npc.deleteMe();
				
			L2MonsterInstance kaiser = (L2MonsterInstance) addSpawn(_kaiser, npc.getX(), npc.getY(), npc.getZ(), 0, false, 180000, false);	//3min
			kaiser.setTarget(player);
			kaiser.addDamageHate(player, 500, 99999);
			kaiser.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
		else
		{
			return "19458.html";
		}
		return super.onFirstTalk(npc, player);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		for (Entry<L2ZoneType, List<L2DoorInstance>> currentZone : _prisons.entrySet())
		{
			if (currentZone.getKey().isInsideZone(npc))
			{
				for (L2DoorInstance door : currentZone.getValue())
					door.openMe();
			}
		}
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		for (Entry<L2ZoneType, List<L2DoorInstance>> currentZone : _prisons.entrySet())
		{
			if (currentZone.getKey().isInsideZone(npc))
			{
				for (L2DoorInstance door : currentZone.getValue())
				{
					door.closeMe();
				}
				
				//Kick players inside...
				for (L2Character chara : currentZone.getKey().getCharactersInside().values())
				{
					if (chara == null)
						continue;
					
					if (chara instanceof L2Playable)
						chara.teleToLocation(npc.getX(), npc.getY() + 500, npc.getZ());
				}
			}
		}
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new NervaPrison(-1, _qn, "ai/individual");
	}
}
