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

import l2server.gameserver.cache.CrestCache;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client packet for setting/deleting clan crest.
 */
public final class RequestSetPledgeCrest extends L2GameClientPacket
{
	static Logger _log = Logger.getLogger(RequestSetPledgeCrest.class.getName());

	private int _length;
	private byte[] _data;

	@Override
	protected void readImpl()
	{
		_length = readD();
		if (_length > 256)
		{
			return;
		}

		_data = new byte[_length];
		readB(_data);
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		L2Clan clan = activeChar.getClan();
		if (clan == null)
		{
			return;
		}

		if (clan.getDissolvingExpiryTime() > System.currentTimeMillis())
		{
			activeChar.sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.CANNOT_SET_CREST_WHILE_DISSOLUTION_IN_PROGRESS));
			return;
		}

		if (_length < 0)
		{
			activeChar.sendMessage("File transfer error.");
			return;
		}
		if (_length > 256)
		{
			activeChar.sendMessage("The clan crest file size was too big (max 256 bytes).");
			return;
		}
		boolean updated = false;
		int crestId = -1;
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_REGISTER_CREST) == L2Clan.CP_CL_REGISTER_CREST)
		{
			if (_length == 0 || _data.length == 0)
			{
				if (clan.getCrestId() == 0)
				{
					return;
				}

				crestId = 0;
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_CREST_HAS_BEEN_DELETED));
				updated = true;
			}
			else
			{
				if (clan.getLevel() < 3)
				{
					activeChar
							.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_LVL_3_NEEDED_TO_SET_CREST));
					return;
				}

				crestId = IdFactory.getInstance().getNextId();
				if (!CrestCache.getInstance().savePledgeCrest(crestId, _data))
				{
					Log.log(Level.INFO,
							"Error saving crest for clan " + clan.getName() + " [" + clan.getClanId() + "]");
					return;
				}
				updated = true;
			}
		}
		if (updated && crestId != -1)
		{
			clan.changeClanCrest(crestId);
		}

		activeChar.sendMessage("Clan crest set succesfully.");
	}
}
