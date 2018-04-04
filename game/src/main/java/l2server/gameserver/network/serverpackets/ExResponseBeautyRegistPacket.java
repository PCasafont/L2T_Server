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
public class ExResponseBeautyRegistPacket extends L2GameServerPacket {
	private long adena;
	private long tickets;
	private int type;
	private int hair;
	private int face;
	private int hairColor;
	
	public ExResponseBeautyRegistPacket(long adena, long tickets, int type, int hair, int face, int hairColor) {
		this.adena = adena;
		this.tickets = tickets;
		this.type = type;
		this.hair = hair;
		this.face = face;
		this.hairColor = hairColor;
	}
	
	@Override
	protected final void writeImpl() {
		writeQ(adena);
		writeQ(tickets);
		writeD(0x00); // 1 ? restore to previous : change
		writeD(type);
		writeD(hair);
		writeD(face);
		writeD(hairColor);
	}
}
