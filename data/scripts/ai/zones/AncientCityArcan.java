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

package ai.zones;

import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.network.serverpackets.Earthquake;
import l2server.gameserver.network.serverpackets.EventTrigger;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * @author LasTravel
 *
 * Source:
 * 			- http://l2wiki.com/Ancient_City_Arcan
 */

public class AncientCityArcan extends Quest
{
	private static final int _blueEffectId = 262001;
	private static final int _redEffectId = 262003;
	private static int _currentEffect = _blueEffectId;
	private static final int _ancientCityArcanId = 60010;
	private static final L2ZoneType _ancientCityZone = ZoneManager.getInstance().getZoneById(_ancientCityArcanId);
	
	public AncientCityArcan(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addEnterZoneId(_ancientCityArcanId);
		
		startQuestTimer("ancient_city_arcan_change", 1800000, null, null, true);
	}
	
	@Override
	public final String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (character instanceof L2PcInstance)
		{
			character.broadcastPacket(new EventTrigger(_currentEffect == _blueEffectId ? _blueEffectId : _redEffectId, false));
			character.broadcastPacket(new EventTrigger(_currentEffect, true));
		}
		return null;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("ancient_city_arcan_change"))
		{
			int deleteEffect = 0;
			int messageId = 0;
			
			if (_currentEffect == _blueEffectId)
			{
				_currentEffect = _redEffectId;
				deleteEffect = _blueEffectId;
				messageId = 8888108;
				
				SpawnTable.getInstance().spawnSpecificTable("ancient_city_arcan");
			}
			else
			{
				_currentEffect = _blueEffectId;
				deleteEffect = _redEffectId;
				messageId = 8888107;
				
				SpawnTable.getInstance().despawnSpecificTable("ancient_city_arcan");
			}
			
			_ancientCityZone.broadcastPacket(new EventTrigger(deleteEffect, false));
			_ancientCityZone.broadcastPacket(new EventTrigger(_currentEffect, true));
			_ancientCityZone.broadcastPacket(new ExShowScreenMessage(messageId, 0, true, 5000));
			_ancientCityZone.broadcastPacket(new Earthquake(207382, 89370, -1123, 5, 10));
		}
		return "";
	}
	
	public static void main(String[] args)
	{
		new AncientCityArcan(-1, "AncientCityArcan", "ai");
	}
}
