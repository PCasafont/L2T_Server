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
 *
 * @author  JIV
 */
public class ExReplyDominionInfo extends L2GameServerPacket
{
	/**
	 * @see l2tserver.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	// 
	@SuppressWarnings("unused")
	private int _warTime = 0;//(int) (TerritoryWarManager.getInstance().getTWStartTimeInMillis() / 1000);
	
	/**
	 * @see l2tserver.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x93);
		
		// Disabled!
		writeD(0x00);
		/*ArrayList<Territory> territoryList = TerritoryWarManager.getInstance().getAllTerritories();
		writeD(territoryList.size()); // Territory Count
		for (Territory t : territoryList)
		{
			writeD(t.getTerritoryId()); // Territory Id
			writeS(CastleManager.getInstance().getCastleById(t.getCastleId()).getName().toLowerCase() + "_dominion"); // territory name
			writeS(t.getOwnerClan().getName());
			writeD(t.getOwnedWardIds().size()); // Emblem Count
			for (int i:t.getOwnedWardIds())
				writeD(i); // Emblem ID - should be in for loop for emblem count
			writeD(_warTime);
		}*/
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:92 ExReplyDominionInfo";
	}
}
