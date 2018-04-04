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
public class ExPutIntensiveResultForVariationMake extends L2GameServerPacket {
	
	private int refinerItemObjId;
	private int lifestoneItemId;
	private int gemstoneItemId;
	private int gemstoneCount;
	private int unk2;
	
	public ExPutIntensiveResultForVariationMake(int refinerItemObjId, int lifeStoneId, int gemstoneItemId, int gemstoneCount) {
		this.refinerItemObjId = refinerItemObjId;
		lifestoneItemId = lifeStoneId;
		this.gemstoneItemId = gemstoneItemId;
		this.gemstoneCount = gemstoneCount;
		unk2 = 1;
	}
	
	@Override
	protected final void writeImpl() {
		writeD(refinerItemObjId);
		writeD(lifestoneItemId);
		writeD(gemstoneItemId);
		writeQ(gemstoneCount);
		writeD(unk2);
	}
}
