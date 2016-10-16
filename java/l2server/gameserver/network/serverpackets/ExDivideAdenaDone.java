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
public class ExDivideAdenaDone extends L2GameServerPacket
{
	private int friendCount;
	private long adena;
	private long totalAdena;
	private String sender;

	public ExDivideAdenaDone(int friendCount, long adena, long totalAdena, String sender)
	{
		this.friendCount = friendCount;
		this.adena = adena;
		this.totalAdena = totalAdena;
		this.sender = sender;
	}

	@Override
	protected final void writeImpl()
	{
		writeH(0x00);
		writeD(this.friendCount); // Friend count
		writeQ(this.adena); // Your adena
		writeQ(this.totalAdena); // Total adena

		writeS(this.sender); // Sender name
	}
}
