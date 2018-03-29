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
public class Ex2ndPasswordVerify extends L2GameServerPacket
{

	public static final int PASSWORD_OK = 0x00;
	public static final int PASSWORD_WRONG = 0x01;
	public static final int PASSWORD_BAN = 0x02;

	int wrongTentatives, mode;

	public Ex2ndPasswordVerify(int mode, int wrongTentatives)
	{
		this.mode = mode;
		this.wrongTentatives = wrongTentatives;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(mode);
		writeD(wrongTentatives);
	}
}
