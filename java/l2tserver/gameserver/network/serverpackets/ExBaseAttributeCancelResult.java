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
 * @author JIV
 *
 */
public class ExBaseAttributeCancelResult extends L2GameServerPacket
{
	private static final String TYPE = "[S] FE:75 ExBaseAttributeCancelResult";
	
	private int _objId;
	private byte _attribute;
	
	public ExBaseAttributeCancelResult(int objId, byte attribute)
	{
		_objId = objId;
		_attribute = attribute;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x76);
		writeD(1); // result
		writeD(_objId);
		writeD(_attribute);
	}
	
	@Override
	public String getType()
	{
		return TYPE;
	}
}
