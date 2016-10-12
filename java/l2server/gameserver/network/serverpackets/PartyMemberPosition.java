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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.model.L2Party;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zabbix
 */
public class PartyMemberPosition extends L2GameServerPacket
{
	Map<Integer, Location> locations = new HashMap<>();

	public PartyMemberPosition(L2Party party)
	{
		reuse(party);
	}

	public void reuse(L2Party party)
	{
		locations.clear();
		for (L2PcInstance member : party.getPartyMembers())
		{
			if (member == null)
			{
				continue;
			}
			locations.put(member.getObjectId(), new Location(member));
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(locations.size());
		for (Map.Entry<Integer, Location> entry : locations.entrySet())
		{
			Location loc = entry.getValue();
			writeD(entry.getKey());
			writeD(loc.getX());
			writeD(loc.getY());
			writeD(loc.getZ());
		}
	}
}
