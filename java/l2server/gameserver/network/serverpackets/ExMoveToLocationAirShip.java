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

import l2server.gameserver.model.actor.L2Character;

public class ExMoveToLocationAirShip extends L2GameServerPacket
{
	private static final String _S__FE_65_EXAIRSHIPMOVETOLOCATION = "[S] FE:65 ExMoveToLocationAirShip";
	
	private final int _objId, _tx, _ty, _tz, _x, _y, _z;
	
	public ExMoveToLocationAirShip(L2Character cha)
	{
		_objId = cha.getObjectId();
		_tx = cha.getXdestination();
		_ty = cha.getYdestination();
		_tz = cha.getZdestination();
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x66);
		
		writeD(_objId);
		writeD(_tx);
		writeD(_ty);
		writeD(_tz);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_65_EXAIRSHIPMOVETOLOCATION;
	}
}