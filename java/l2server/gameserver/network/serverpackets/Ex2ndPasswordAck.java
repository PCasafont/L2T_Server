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
 * @author mrTJO
 */
public class Ex2ndPasswordAck extends L2GameServerPacket
{

	int response;

	public static int SUCCESS = 0x00;
	public static int WRONG_PATTERN = 0x01;

	public Ex2ndPasswordAck(int response)
	{
		this.response = response;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x00);
		if (response == WRONG_PATTERN)
		{
			writeD(0x01);
		}
		else
		{
			writeD(0x00);
		}
		writeD(0x00);
	}
}
