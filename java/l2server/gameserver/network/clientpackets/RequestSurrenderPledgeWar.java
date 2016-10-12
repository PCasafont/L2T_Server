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

import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.ClanWarManager;
import l2server.gameserver.model.entity.ClanWarManager.ClanWar;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;

public final class RequestSurrenderPledgeWar extends L2GameClientPacket
{
	private String _pledgeName;

	@Override
	protected void readImpl()
	{
		_pledgeName = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		L2Clan playerClan = activeChar.getClan();
		if (playerClan == null)
		{
			return;
		}

		L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
		if (clan == null)
		{
			activeChar.sendMessage("No such clan.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		Log.info("RequestSurrenderPledgeWar by " + getClient().getActiveChar().getClan().getName() + " with " +
				_pledgeName);

		if (!playerClan.isAtWarWith(clan.getClanId()))
		{
			activeChar.sendMessage("You aren't at war with this clan.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN);
		msg.addString(_pledgeName);
		activeChar.sendPacket(msg);
		msg = null;
		activeChar.deathPenalty(false, false, false, false);
		ClanWar war = ClanWarManager.getInstance().getWar(playerClan, clan);
		war.setLoser(playerClan);
		war.setWinner(clan);
		war.stop();
	}
}
