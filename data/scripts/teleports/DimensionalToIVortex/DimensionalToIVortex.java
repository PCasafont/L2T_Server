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
package teleports.DimensionalToIVortex;

import java.util.HashMap;
import java.util.Map;

import l2tserver.gameserver.model.Location;
import l2tserver.gameserver.model.actor.L2Npc;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.model.quest.Quest;


/**
 * @author LasTravel
 * 
 *  Dimensional Vortex Npc teleports and Dimensional Stone Sellers
 *
 */
public class DimensionalToIVortex extends Quest
{
	private static final String	_qn 					= "DimensionalToIVortex";
	private static final int[]	_dimensionalVortexNpcs	= {30952, 30953, 30954};
	private static final int[]	_dimensionalStoneNpcs	= {30949, 30950, 30951};
	private static final int	_greenDimensionalStone	= 4401;
	private static final int	_blueDimensionalStone	= 4402;
	private static final int	_redDimensionalStone	= 4403;
	private static Map<Integer, Location> _teleports	= new HashMap<Integer, Location>(10);
	
	public DimensionalToIVortex(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		for (int npcId : _dimensionalVortexNpcs)
		{
			addStartNpc(npcId);
			
			addTalkId(npcId);
			
			addFirstTalkId(npcId);
		}
		
		for (int npcId : _dimensionalStoneNpcs)
		{
			addStartNpc(npcId);
			
			addTalkId(npcId);
		}
		
		_teleports.put(1, new Location(114679, 13436, -5101));
		_teleports.put(2, new Location(114665, 12697, -3609));
		_teleports.put(3, new Location(111249, 16031, -2127));
		_teleports.put(4, new Location(114605, 19371, -645));
		_teleports.put(5, new Location(117996, 16103, 843));
		_teleports.put(6, new Location(114743, 19707, 1947));
		_teleports.put(7, new Location(114552, 12354, 2957));
		_teleports.put(8, new Location(110963, 16147, 3967));
		_teleports.put(9, new Location(117356, 18462, 4977));
		_teleports.put(10, new Location(118250, 15858, 5897));
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.getNpcId() + ".html";
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.getNpcId() + ".html";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.startsWith("buy"))
		{
			int stoneId = 0;
			
			if (event.equalsIgnoreCase("buyGreenStone"))
			{
				stoneId = _greenDimensionalStone;
			}
			else if (event.equalsIgnoreCase("buyBlueStone"))
			{
				stoneId = _blueDimensionalStone;
			}
			else
			{
				stoneId = _redDimensionalStone;
			}
			
			if (player.destroyItemByItemId(_qn, 57, 10000, player, true))
				player.addItem(_qn, stoneId, 1, npc, true);
		}
		else
		{
			int teleportId = Integer.valueOf(event);
			
			int stoneId = 0;
			
			if (teleportId >= 1 && teleportId <= 3)
			{	
				stoneId = _greenDimensionalStone;
			}	
			else if (teleportId >= 4  && teleportId <= 6)
			{	
				stoneId = _blueDimensionalStone;
			}	
			else
			{	
				stoneId = _redDimensionalStone;
			}
			
			if (!player.destroyItemByItemId(_qn, stoneId, 1, player, true))
				return "no.html";
			else
				player.teleToLocation(_teleports.get(teleportId), true);
		}
		
		return super.onAdvEvent(event, npc, player);
	}

	public static void main(String[] args)
	{
		new DimensionalToIVortex(-1, _qn, "teleports");
	}
}
