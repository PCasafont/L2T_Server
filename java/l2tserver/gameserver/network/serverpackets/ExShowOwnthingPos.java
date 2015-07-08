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


/**
 * Format: (ch) d[dddd]
 *
 * @author  -Gigiikun-
 */
public class ExShowOwnthingPos extends L2GameServerPacket
{
	private static final String _S__FE_93_EXSHOWOWNTHINGPOS = "[S] FE:93 ExShowOwnthingPos";
	
	public ExShowOwnthingPos()
	{
	}
	
	/**
	 * @see l2tserver.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x94);

		// Disabled!
		writeD(0x00);
		/*if (TerritoryWarManager.getInstance().isTWInProgress())
		{
			List<TerritoryWard> territoryWardList = TerritoryWarManager.getInstance().getAllTerritoryWards();
			writeD(territoryWardList.size());
			for (TerritoryWard ward : territoryWardList)
			{
				writeD(ward.getTerritoryId());
				
				if (ward.getNpc() != null)
				{
					writeD(ward.getNpc().getX());
					writeD(ward.getNpc().getY());
					writeD(ward.getNpc().getZ());
				}
				else if (ward.getPlayer() != null)
				{
					writeD(ward.getPlayer().getX());
					writeD(ward.getPlayer().getY());
					writeD(ward.getPlayer().getZ());
				}
				else
				{
					writeD(0);
					writeD(0);
					writeD(0);
				}
			}
		}
		else
		{
			writeD(0);
			//writeD(0);
		}*/
	}
	
	/**
	 * @see l2tserver.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_93_EXSHOWOWNTHINGPOS;
	}
}
