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

import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.entity.Fort;

import java.util.List;

/**
 * @author KenM
 */
public class ExShowFortressSiegeInfo extends L2GameServerPacket {
	private int fortId;
	private int size;
	private Fort fort;
	private int csize;
	private int csize2;
	
	public ExShowFortressSiegeInfo(Fort fort) {
		this.fort = fort;
		fortId = fort.getFortId();
		size = fort.getFortSize();
		List<L2Spawn> commanders = fort.getCommanderSpawns();
		if (commanders != null) {
			csize = commanders.size();
		}
		csize2 = fort.getCommanderSpawns().size();
	}

    /*
	  @see l2server.gameserver.network.serverpackets.L2GameServerPacket#getType()
     */
	
	/**
	 * @see l2server.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl() {
		writeD(fortId); // Fortress Id
		writeD(size); // Total Barracks Count
		if (csize > 0) {
			switch (csize) {
				case 3:
					switch (csize2) {
						case 0:
							writeD(0x03);
							break;
						case 1:
							writeD(0x02);
							break;
						case 2:
							writeD(0x01);
							break;
						case 3:
							writeD(0x00);
							break;
					}
					break;
				case 4: // TODO: change 4 to 5 once control room supported
					switch (csize2)
					// TODO: once control room supported, update writeD(0x0x) to support 5th room
					{
						case 0:
							writeD(0x05);
							break;
						case 1:
							writeD(0x04);
							break;
						case 2:
							writeD(0x03);
							break;
						case 3:
							writeD(0x02);
							break;
						case 4:
							writeD(0x01);
							break;
					}
					break;
			}
		} else {
			for (int i = 0; i < size; i++) {
				writeD(0x00);
			}
		}
	}
}
