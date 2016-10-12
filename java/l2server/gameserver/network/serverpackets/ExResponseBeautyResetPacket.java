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
 * @author Pere
 */
public class ExResponseBeautyResetPacket extends L2GameServerPacket
{
	private long _adena;
	private long _tickets;
	private int _hair;
	private int _face;
	private int _hairColor;

	public ExResponseBeautyResetPacket(long adena, long tickets, int hair, int face, int hairColor)
	{
		_adena = adena;
		_tickets = tickets;
		_hair = hair;
		_face = face;
		_hairColor = hairColor;
	}

	@Override
	protected final void writeImpl()
	{
		writeQ(_adena);
		writeQ(_tickets);
		writeD(_hair);
		writeD(_face);
		writeD(_hairColor);
	}
}
