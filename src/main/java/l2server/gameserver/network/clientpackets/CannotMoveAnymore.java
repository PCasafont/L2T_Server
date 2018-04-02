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
import l2server.gameserver.ai.CtrlEvent;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.actor.instance.Player;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class CannotMoveAnymore extends L2GameClientPacket {

	private int x;
	private int y;
	private int z;
	private int heading;

	@Override
	protected void readImpl() {
		x = readD();
		y = readD();
		z = readD();
		heading = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getClient().getActiveChar();
		if (player == null) {
			return;
		}

		if (Config.DEBUG) {
			log.debug("client: x:" + x + " y:" + y + " z:" + z + " server x:" + player.getX() + " y:" + player.getY() + " z:" + player.getZ());
		}

		if (player.getAI() != null) {
			player.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED_BLOCKED, new L2CharPosition(x, y, z, heading));
		}
		/*if (player.getParty() != null)
        {
			player.getParty().broadcastToPartyMembers(player, new PartyMemberPosition(player));
		}*/

		// player.stopMove();
		//
		// if (Config.DEBUG)
		// Logozo.fine("client: x:"+x+" y:"+y+" z:"+z+
		// " server x:"+player.getX()+" y:"+player.getZ()+" z:"+player.getZ());
		// StopMove smwl = new StopMove(player);
		// getClient().getActiveChar().sendPacket(smwl);
		// getClient().getActiveChar().broadcastPacket(smwl);
		//
		// StopRotation sr = new StopRotation(getClient().getActiveChar(),
		// heading);
		// getClient().getActiveChar().sendPacket(sr);
		// getClient().getActiveChar().broadcastPacket(sr);
	}
}
