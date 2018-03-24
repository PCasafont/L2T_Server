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

import l2server.Config;
import l2server.gameserver.Announcements;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.model.zone.type.L2PeaceZone;
import l2server.gameserver.model.zone.type.L2SiegeZone;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.EventTrigger;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Broadcast;
import l2server.util.Rnd;

/**
 * @author LasTravel
 *         <p>
 *         Source:
 *         - http://l2wiki.com/Gainak
 */

public class Gainak extends Quest
{
	private static final int _siegeEffect = 20140700;
	private static boolean _isInSiege = false;
	private static final int _gainakPeaceZoneId = 60018;
	private static final int _gainakSiegeZoneId = 60019;
	private static final L2PeaceZone _gainakPeaceZone =
			ZoneManager.getInstance().getZoneById(_gainakPeaceZoneId, L2PeaceZone.class);
	private static final L2SiegeZone _gainakSiegeZone =
			ZoneManager.getInstance().getZoneById(_gainakSiegeZoneId, L2SiegeZone.class);

	public Gainak(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addDieZoneId(_gainakSiegeZoneId);
		addEnterZoneId(_gainakSiegeZoneId);

		startQuestTimer("gainak_change", getTimeBetweenSieges() * 60000, null, null);
	}

	private final int getTimeBetweenSieges()
	{
		if (Config.isServer(Config.TENKAI))
		{
			return Rnd.get(120, 180); // 2 to 3 hours.
		}

		return 150;
	}

	private final int getSiegeDuration()
	{
		return 30;
	}

	@Override
	public final String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (character instanceof L2PcInstance)
		{
			if (_isInSiege)
			{
				character.broadcastPacket(new EventTrigger(_siegeEffect, true));
			}
		}
		return null;
	}

	@Override
	public String onDieZone(L2Character character, L2Character killer, L2ZoneType zone)
	{
		if (_isInSiege)
		{
			L2PcInstance player = killer.getActingPlayer();
			if (player != null)
			{
				player.increasePvpKills(character);
			}
		}
		return null;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("gainak_change"))
		{
			if (_isInSiege)
			{
				SpawnTable.getInstance().despawnSpecificTable("gainak_siege");

				_isInSiege = false;

				_gainakPeaceZone.setZoneEnabled(true);
				_gainakSiegeZone.setIsActive(false);
				_gainakSiegeZone.updateZoneStatusForCharactersInside();
				_gainakPeaceZone.broadcastPacket(new EventTrigger(_siegeEffect, false));
				_gainakPeaceZone.broadcastPacket(new ExShowScreenMessage(1600066, 0, true, 5000));

				startQuestTimer("gainak_change", getTimeBetweenSieges() * 60000, null, null);

				SystemMessage s = SystemMessage.getSystemMessage(SystemMessageId.LIGHT_BLUE_DOWNSTAIRS_S1);
				s.addString("Gainak is now in peace.");
				Announcements.getInstance().announceToAll(s);
			}
			else
			{
				SpawnTable.getInstance().spawnSpecificTable("gainak_siege");

				_isInSiege = true;

				_gainakPeaceZone.setZoneEnabled(false);
				_gainakSiegeZone.setIsActive(true);
				_gainakSiegeZone.updateZoneStatusForCharactersInside();
				_gainakSiegeZone.broadcastPacket(new EventTrigger(_siegeEffect, true));
				_gainakSiegeZone.broadcastPacket(new ExShowScreenMessage(1600063, 0, true, 5000));

				startQuestTimer("gainak_change", getSiegeDuration() * 60000, null, null);

				SystemMessage s = SystemMessage.getSystemMessage(SystemMessageId.LIGHT_BLUE_DOWNSTAIRS_S1);
				s.addString("Gainak is now under siege.");
				Announcements.getInstance().announceToAll(s);

				if (Config.isServer(Config.TENKAI_LEGACY))
				{
					ExShowScreenMessage essm = new ExShowScreenMessage("Gainak is now under siege!", 5000);
					Broadcast.toAllOnlinePlayers(essm);
				}

				ZoneManager.getInstance().getZoneByName("Gainak Siege Peace Zone", L2PeaceZone.class)
						.setZoneEnabled(false);
			}
		}
		return "";
	}

	public static void main(String[] args)
	{
		new Gainak(-1, "Gainak", "ai");
	}
}
