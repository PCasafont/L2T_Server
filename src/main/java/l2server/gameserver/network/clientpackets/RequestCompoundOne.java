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

import l2server.gameserver.datatables.CompoundTable;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.ExCompoundOneFail;
import l2server.gameserver.network.serverpackets.ExCompoundOneOK;
import l2server.gameserver.network.serverpackets.ExCompoundTwoFail;

/**
 * @author Pere
 */
public final class RequestCompoundOne extends L2GameClientPacket {
	private int objId;

	@Override
	protected void readImpl() {
		objId = readD();
	}

	@Override
	protected void runImpl() {
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}

		Item compoundItem = activeChar.getInventory().getItemByObjectId(objId);
		if (compoundItem == null) {
			sendPacket(new ExCompoundTwoFail());
			return;
		}

		if (activeChar.getCompoundItem2() != null &&
				(activeChar.getCompoundItem2() == compoundItem || activeChar.getCompoundItem2().getItemId() != compoundItem.getItemId())) {
			sendPacket(new ExCompoundOneFail());
			return;
		}

		if (!CompoundTable.getInstance().isCombinable(compoundItem.getItemId())) {
			sendPacket(new ExCompoundOneFail());
			return;
		}

		activeChar.setCompoundItem1(compoundItem);
		sendPacket(new ExCompoundOneOK());
	}
}
