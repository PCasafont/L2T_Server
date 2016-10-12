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
import l2server.gameserver.instancemanager.AntiFeedManager;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.L2GameClient;
import l2server.gameserver.network.L2GameClient.GameClientState;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.CharSelectionInfo;
import l2server.gameserver.network.serverpackets.RestartResponse;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;
import l2server.log.Log;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * This class ...
 *
 * @version $Revision: 1.11.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestRestart extends L2GameClientPacket
{

	protected static final Logger _logAccounting = Logger.getLogger("accounting");

	@Override
	protected void readImpl()
	{
		// trigger
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();

		if (player == null)
		{
			return;
		}

		if (player.getActiveEnchantItem() != null || player.getActiveEnchantAttrItem() != null)
		{
			sendPacket(RestartResponse.valueOf(false));
			return;
		}

		if (player.isLocked())
		{
			Log.warning("Player " + player.getName() + " tried to restart during class change.");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}

		if (player.getPrivateStoreType() != 0)
		{
			player.sendMessage("Cannot restart while trading");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}

		if (AttackStanceTaskManager.getInstance().getAttackStanceTask(player) &&
				!(player.isGM() && Config.GM_RESTART_FIGHTING))
		{
			if (Config.DEBUG)
			{
				Log.fine("Player " + player.getName() + " tried to logout while fighting.");
			}

			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_RESTART_WHILE_FIGHTING));
			sendPacket(RestartResponse.valueOf(false));
			return;
		}

		// Remove player from Boss Zone
		player.removeFromBossZone();

		final L2GameClient client = getClient();

		LogRecord record = new LogRecord(Level.INFO, "Logged out");
		record.setParameters(new Object[]{client});
		_logAccounting.log(record);

		// detach the client from the char so that the connection isnt closed in the deleteMe
		player.setClient(null);

		player.deleteMe();

		client.setActiveChar(null);
		AntiFeedManager.getInstance().onDisconnect(client);

		// return the client to the authed status
		client.setState(GameClientState.AUTHED);

		sendPacket(RestartResponse.valueOf(true));

		// send char list
		final CharSelectionInfo cl = new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1);
		sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}
}
