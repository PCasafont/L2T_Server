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

import l2server.Config;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.instancemanager.CastleManorManager;
import l2server.gameserver.instancemanager.CastleManorManager.CropProcure;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.instance.CastleChamberlainInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.List;

import static l2server.gameserver.model.actor.Npc.DEFAULT_INTERACTION_DISTANCE;
import static l2server.gameserver.model.itemcontainer.PcInventory.MAX_ADENA;

/**
 * Format: (ch) dd [dddc]
 * d - manor id
 * d - size
 * [
 * d - crop id
 * d - sales
 * d - price
 * c - reward type
 * ]
 *
 * @author l3x
 */
public class RequestSetCrop extends L2GameClientPacket {
	//

	private static final int BATCH_LENGTH = 21; // length of the one item

	private int manorId;
	private Crop[] items = null;

	@Override
	protected void readImpl() {
		manorId = readD();
		int count = readD();
		if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != buf.remaining()) {
			return;
		}

		items = new Crop[count];
		for (int i = 0; i < count; i++) {
			int itemId = readD();
			long sales = readQ();
			long price = readQ();
			int type = readC();
			if (itemId < 1 || sales < 0 || price < 0) {
				items = null;
				return;
			}
			items[i] = new Crop(itemId, sales, price, type);
		}
	}

	@Override
	protected void runImpl() {
		if (items == null) {
			return;
		}

		Player player = getClient().getActiveChar();
		// check player privileges
		if (player == null || player.getClan() == null || (player.getClanPrivileges() & L2Clan.CP_CS_MANOR_ADMIN) == 0) {
			return;
		}

		// check castle owner
		Castle currentCastle = CastleManager.getInstance().getCastleById(manorId);
		if (currentCastle.getOwnerId() != player.getClanId()) {
			return;
		}

		WorldObject manager = player.getTarget();

		if (!(manager instanceof CastleChamberlainInstance)) {
			manager = player.getLastFolkNPC();
		}

		if (!(manager instanceof CastleChamberlainInstance)) {
			return;
		}

		if (((CastleChamberlainInstance) manager).getCastle() != currentCastle) {
			return;
		}

		if (!player.isInsideRadius(manager, DEFAULT_INTERACTION_DISTANCE, true, false)) {
			return;
		}

		List<CropProcure> crops = new ArrayList<>(items.length);
		for (Crop i : items) {
			CropProcure s = i.getCrop();
			if (s == null) {
				Util.handleIllegalPlayerAction(player,
						"Warning!! Character " + player.getName() + " of account " + player.getAccountName() +
								" tried to overflow while setting manor.",
						Config.DEFAULT_PUNISH);
				return;
			}
			crops.add(s);
		}

		currentCastle.setCropProcure(crops, CastleManorManager.PERIOD_NEXT);
		if (Config.ALT_MANOR_SAVE_ALL_ACTIONS) {
			currentCastle.saveCropData(CastleManorManager.PERIOD_NEXT);
		}
	}

	private static class Crop {
		private final int itemId;
		private final long sales;
		private final long price;
		private final int type;

		public Crop(int id, long s, long p, int t) {
			itemId = id;
			sales = s;
			price = p;
			type = t;
		}

		public CropProcure getCrop() {
			if (sales != 0 && MAX_ADENA / sales < price) {
				return null;
			}

			return CastleManorManager.getInstance().getNewCropProcure(itemId, sales, type, price, sales);
		}
	}
}
