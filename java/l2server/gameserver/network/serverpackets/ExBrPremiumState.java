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
 * format: dc
 * @author  GodKratos
 */
public class ExBrPremiumState extends L2GameServerPacket
{
	private static final String TYPE = "[S] FE:CD ExBrPremiumState";
	private int _objId;
	private int _state;
	
	public ExBrPremiumState(int id, int state)
	{
		_objId = id;
		_state = state;
	}
	
	/**
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0xda);
		writeD(_objId);
		writeC(_state);
	}
	
	/**
	 * @see l2server.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return TYPE;
	}
}
