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

import l2server.gameserver.model.Item;
import l2server.gameserver.model.actor.instance.Player;

/**
 * @author Erlandys
 */
public class ExResponseCommissionInfo extends L2GameServerPacket {
	
	@SuppressWarnings("unused")
	private Player player;
	@SuppressWarnings("unused")
	private Item item;
	private boolean success;
	
	public ExResponseCommissionInfo(Player player, int itemOID, boolean success) {
		this.player = player;
		item = player.getInventory().getItemByObjectId(itemOID);
		this.success = success;
	}
	
	@Override
	protected final void writeImpl() {
		writeD(success ? 0x01 : 0x00);
		writeD(0x00); // ItemID
		writeD(0x00); // TODO: Price
		writeQ(0x00); // TODO: Count
		writeD(0x00); // TODO: Duration
		writeD(-1); // TODO: Unknown
		writeD(0x00); // TODO: Unknown
	}
}
