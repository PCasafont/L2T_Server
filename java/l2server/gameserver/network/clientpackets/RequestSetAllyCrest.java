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
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.AllyCrest;
import l2server.log.Log;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client packet for setting ally crest.
 */
public final class RequestSetAllyCrest extends L2GameClientPacket
{
	static Logger _log = Logger.getLogger(RequestSetAllyCrest.class.getName());

	private int _length;
	private byte[] _data;

	@Override
	protected void readImpl()
	{
		_length = readD();
		if (_length > 192)
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

		if (_length < 0)
		{
			activeChar.sendMessage("File transfer error.");
			return;
		}
		if (_length > 192)
		{
			activeChar.sendMessage("The ally crest file size was too big (max 192 bytes).");
			return;
		}

		if (activeChar.getAllyId() != 0)
		{
			L2Clan leaderclan = ClanTable.getInstance().getClan(activeChar.getAllyId());

			if (activeChar.getClanId() != leaderclan.getClanId() || !activeChar.isClanLeader())
			{
				return;
			}

			boolean remove = false;
			if (_length == 0 || _data.length == 0)
			{
				remove = true;
			}

			int newId = 0;
			if (!remove)
			{
				newId = IdFactory.getInstance().getNextId();
			}

			if (!remove && !CrestCache.getInstance().saveAllyCrest(newId, _data))
			{
				Log.log(Level.INFO,
						"Error saving crest for ally " + leaderclan.getAllyName() + " [" + leaderclan.getAllyId() +
								"]");
				return;
			}

			leaderclan.changeAllyCrest(newId, false);

			activeChar.sendPacket(new AllyCrest(newId));
		}
	}
}
