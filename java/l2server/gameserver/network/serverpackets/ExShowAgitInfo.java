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

import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.instancemanager.ClanHallManager;
import l2server.gameserver.model.entity.ClanHall;

import java.util.Map;

/**
 * @author KenM
 */
public class ExShowAgitInfo extends L2GameServerPacket
{

    /*
	  @see l2server.gameserver.network.serverpackets.L2GameServerPacket#getType()
     */

	/**
	 * @see l2server.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		Map<Integer, ClanHall> clannhalls = ClanHallManager.getInstance().getAllClanHalls();
		writeD(clannhalls.size());
		for (ClanHall ch : clannhalls.values())
		{
			writeD(ch.getId());
			writeS(ch.getOwnerId() <= 0 ? "" :
					ClanTable.getInstance().getClan(ch.getOwnerId()).getName()); // owner clan name
			writeS(ch.getOwnerId() <= 0 ? "" :
					ClanTable.getInstance().getClan(ch.getOwnerId()).getLeaderName()); // leader name
			writeD(ch.getGrade() > 0 ? 0x00 : 0x01); // 0 - auction  1 - war clanhall  2 - ETC (rainbow spring clanhall)
		}
	}
}
