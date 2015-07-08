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
public class ExResponseCommissionDelete extends L2GameServerPacket
{
	private static final String _S__FE_F5_EXRESPONSECOMMISSIONDELETE = "[S] FE:F5 ExResponseCommissionDelete";

	private boolean _success;

	public ExResponseCommissionDelete(boolean success)
	{
		_success = success;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xd3);
		writeD(_success ? 0x01 : 0x00);
	}

	@Override
	public String getType()
	{
		return _S__FE_F5_EXRESPONSECOMMISSIONDELETE;
	}
}
