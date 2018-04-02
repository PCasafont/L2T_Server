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
 * Format: (ch)ddddd
 */
public class ExPutCommissionResultForVariationMake extends L2GameServerPacket {
	
	private int gemstoneObjId;
	private int itemId;
	private long gemstoneCount;
	private int unk1;
	private int unk2;
	private int unk3;
	
	public ExPutCommissionResultForVariationMake(int gemstoneObjId, long count, int itemId) {
		this.gemstoneObjId = gemstoneObjId;
		this.itemId = itemId;
		gemstoneCount = count;
		unk1 = 0;
		unk2 = 0;
		unk3 = 1;
	}
	
	@Override
	protected final void writeImpl() {
		writeD(gemstoneObjId);
		writeD(itemId);
		writeQ(gemstoneCount);
		writeD(unk1);
		writeD(unk2);
		writeD(unk3);
	}
}
