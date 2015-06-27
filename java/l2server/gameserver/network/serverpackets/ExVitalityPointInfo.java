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

/**
 * format: d
 * @author  GodKratos
 */
public class ExVitalityPointInfo extends L2GameServerPacket
{
	private static final String _S__FE_A0_EXVITALITYPOINTINFO = "[S] FE:A0 ExVitalityPointInfo";
	private int _vitalityPoints;
	
	public ExVitalityPointInfo(int vitPoints)
	{
		_vitalityPoints = vitPoints;
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0xa1);
		writeD(_vitalityPoints);
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_A0_EXVITALITYPOINTINFO;
	}
}
