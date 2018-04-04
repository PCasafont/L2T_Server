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

import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.StartRotation;

/**
 * This class ...
 *
 * @version $Revision: 1.1.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class StartRotating extends L2GameClientPacket {

	private int degree;
	private int side;

	@Override
	protected void readImpl() {
		degree = readD();
		side = readD();
	}

	@Override
	protected void runImpl() {
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}

		final StartRotation br;
		if (activeChar.isInAirShip() && activeChar.getAirShip().isCaptain(activeChar)) {
			br = new StartRotation(activeChar.getAirShip().getObjectId(), degree, side, 0);
			activeChar.getAirShip().broadcastPacket(br);
		} else {
			br = new StartRotation(activeChar.getObjectId(), degree, side, 0);
			activeChar.broadcastPacket(br);
		}
	}
}
