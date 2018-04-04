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
import l2server.gameserver.Ranked1v1;
import l2server.gameserver.events.Elpy;
import l2server.gameserver.events.Ranked2v2;
import l2server.gameserver.model.RandomFight;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * This class ...
 *
 * @version $Revision: 1.9.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class Logout extends L2GameClientPacket {

	protected static final Logger logAccounting = Logger.getLogger("accounting");

	@Override
	protected void readImpl() {

	}

	@Override
	protected void runImpl() {
		// Dont allow leaving if player is fighting
		final Player player = getClient().getActiveChar();

		if (player == null) {
			return;
		}

		if (player.getActiveEnchantItem() != null || player.getActiveEnchantAttrItem() != null) {
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (player.isLocked()) {
			log.warn("Player " + player.getName() + " tried to logout during class change.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (Elpy.elpy.containsKey(player.getObjectId())) {
			Elpy.getInstance().removePlayer(player);
		}
		if (Ranked1v1.players.contains(player)) {
			Ranked1v1.players.remove(player);
		}
		if (Ranked2v2.players.contains(player)) {
			Ranked2v2.players.remove(player);
		}
		if (Ranked2v2.teamOne.contains(player)) {
			Ranked2v2.teamOne.clear();
		}
		if (Ranked2v2.teamTwo.contains(player)) {
			Ranked2v2.teamTwo.clear();
		}
		if (RandomFight.players.contains(player)) {
			player.sendMessage("You can't logout when you are in random fight event.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (AttackStanceTaskManager.getInstance().getAttackStanceTask(player) && !(player.isGM() && Config.GM_RESTART_FIGHTING)) {
			if (Config.DEBUG) {
				log.debug("Player " + player.getName() + " tried to logout while fighting");
			}

			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_LOGOUT_WHILE_FIGHTING));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Remove player from Boss Zone
		player.removeFromBossZone();

		LogRecord record = new LogRecord(Level.INFO, "Disconnected");
		record.setParameters(new Object[]{getClient()});
		logAccounting.log(record);

		player.logout();
	}
}
