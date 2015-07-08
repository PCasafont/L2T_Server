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
package l2tserver.gameserver.network.serverpackets;

import java.util.List;

import l2tserver.gameserver.datatables.ClanTable;
import l2tserver.gameserver.instancemanager.CastleManager;
import l2tserver.gameserver.model.entity.Castle;
import l2tserver.log.Log;

/**
 *
 * @author  KenM
 */
public class ExShowCastleInfo extends L2GameServerPacket
{
	
	/**
	 * @see l2tserver.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	
	@Override
	public String getType()
	{
		return "[S] FE:14 ExShowCastleInfo";
	}
	
	/**
	 * @see l2tserver.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x14);
		List<Castle> castles = CastleManager.getInstance().getCastles();
		writeD(castles.size());
		for (Castle castle : castles)
		{
			writeD(castle.getCastleId());
			if (castle.getOwnerId() > 0)
			{
				if (ClanTable.getInstance().getClan(castle.getOwnerId()) != null)
					writeS(ClanTable.getInstance().getClan(castle.getOwnerId()).getName());
				else
				{
					Log.warning("Castle owner with no name! Castle: " + castle.getName() + " has an OwnerId = " + castle.getOwnerId() + " who does not have a  name!");
					writeS("");
				}
			}
			else
				writeS("");
			writeD(castle.getTaxPercent());
			writeD((int)(castle.getSiege().getSiegeDate().getTimeInMillis()/1000));
		}
	}
	
}
