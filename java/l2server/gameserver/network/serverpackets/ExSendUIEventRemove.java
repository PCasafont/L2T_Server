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

public class ExSendUIEventRemove extends L2GameServerPacket
{
	private static final String _S__FE_8E_EXSENDUIEVENTREMOVE = "[S] FE:8E ExSendUIEventRemove";
	private int _uiType;
	
	public ExSendUIEventRemove()
	{
		_uiType = 1;
	}
	
	@Override
	protected void writeImpl()
	{
		if (getClient() == null || getClient().getActiveChar() == null)
		{
			return;
		}
		
		writeC(0xFE);
		writeH(0x8f);
		writeD(getClient().getActiveChar().getObjectId());
		writeD(_uiType);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_8E_EXSENDUIEVENTREMOVE;
	}
}