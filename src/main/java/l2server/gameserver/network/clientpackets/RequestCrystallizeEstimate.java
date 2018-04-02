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

package l2server.gameserver.network.clientpackets;

import l2server.gameserver.model.Item;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.ExCrystalizingEstimation;

/**
 * @author Erlandys
 */
public final class RequestCrystallizeEstimate extends L2GameClientPacket {
	
	private int itemObjId;
	@SuppressWarnings("unused")
	private long itemCount;
	
	@Override
	protected void readImpl() {
		itemObjId = readD();
		itemCount = readQ(); // For some use???
	}
	
	@Override
	protected void runImpl() {
		Player player = getClient().getActiveChar();
		if (player == null) {
			return;
		}
		
		Item item = player.getInventory().getItemByObjectId(itemObjId);
		if (item != null) {
			sendPacket(new ExCrystalizingEstimation(item.getItem(), item.getCrystalCount()));
		}
	}
}
