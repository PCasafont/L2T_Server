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
 * Format: ch dddd
 *
 * @author KenM
 */
public class ExUseSharedGroupItem extends L2GameServerPacket {
	private int itemId, grpId, remainedTime, totalTime;
	
	public ExUseSharedGroupItem(int itemId, int grpId, int remainedTime, int totalTime) {
		this.itemId = itemId;
		this.grpId = grpId;
		this.remainedTime = remainedTime / 1000;
		this.totalTime = totalTime / 1000;
	}
	
	@Override
	protected final void writeImpl() {
		writeD(itemId);
		writeD(grpId);
		writeD(remainedTime);
		writeD(totalTime);
	}
}
