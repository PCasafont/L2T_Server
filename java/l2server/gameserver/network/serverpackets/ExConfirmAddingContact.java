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
 * Format: (ch)Sd
 * S: Character Name
 * d: Status
 * 
 * @author mrTJO & UnAfraid
 */
public class ExConfirmAddingContact extends L2GameServerPacket
{
	private static final String _S__FE_D2_EXCONFIRMADDINGCONTACT = "[S] FE:D2 ExConfirmAddingContact";
	private final String _charName;
	private final boolean _added;
	
	public ExConfirmAddingContact(String charName, boolean added)
	{
		_charName = charName;
		_added = added;
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xD3);
		writeS(_charName);
		writeD(_added ? 0x01 : 0x00);
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_D2_EXCONFIRMADDINGCONTACT;
	}
	
}
