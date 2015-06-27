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
 * 
 * @author mrTJO
 */
public class Ex2ndPasswordVerify extends L2GameServerPacket
{
	private static final String _S__FE_10A_EX2NDPASSWORDVERIFYPACKET = "[S] FE:10A Ex2NDPasswordVerifyPacket";
	
	public static final int PASSWORD_OK = 0x00;
	public static final int PASSWORD_WRONG = 0x01;
	public static final int PASSWORD_BAN = 0x02;
	
	int _wrongTentatives, _mode;
	
	public Ex2ndPasswordVerify(int mode, int wrongTentatives)
	{
		_mode = mode;
		_wrongTentatives = wrongTentatives;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x106);
		writeD(_mode);
		writeD(_wrongTentatives);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_10A_EX2NDPASSWORDVERIFYPACKET;
	}
}
