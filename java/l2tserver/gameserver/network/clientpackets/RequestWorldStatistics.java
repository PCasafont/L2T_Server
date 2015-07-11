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
package l2tserver.gameserver.network.clientpackets;

import l2tserver.gameserver.network.serverpackets.ExLoadStatWorldRank;

/**
 * @author Pere
 */
public final class RequestWorldStatistics extends L2GameClientPacket
{
	private int _pId1;
	private int _pId2;
	
	@Override
	protected void readImpl()
	{
		_pId1 = readD();
		_pId2 = readD();
	}
	
	/**
	 * @see l2tserver.util.network.BaseRecievePacket.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		if (getClient().getActiveChar() == null)
				//|| MuseumStatistic.get(_pId1, _pId2) == null)
			return;
		
		sendPacket(new ExLoadStatWorldRank(_pId1, _pId2));
	}
	
	/**
	 * @see l2tserver.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "RequestWorldStatistics";
	}
}
