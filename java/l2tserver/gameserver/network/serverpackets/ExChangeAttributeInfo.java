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
 * @author Erlandys
 */
public class ExChangeAttributeInfo extends L2GameServerPacket
{
	
	private static final String _S__FE_118_EXCHANGEATTRIBUTEINFO = "[S] FE:118 ExChangeAttributeInfo";

	private int itemOID;
	private int attributeOID;
	private int attributes;

	public ExChangeAttributeInfo(int _attributeOID, int _itemOID, int _attribute)
	{
		itemOID = _itemOID;
		attributeOID = _attributeOID;
		attributes = _attribute;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x119);
		writeD(attributeOID);
		writeD(attributes);
		writeD(itemOID);
		
	}

	@Override
	public String getType()
	{
		return _S__FE_118_EXCHANGEATTRIBUTEINFO;
	}
	
}
