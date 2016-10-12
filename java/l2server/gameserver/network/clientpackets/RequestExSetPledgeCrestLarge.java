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
import l2server.gameserver.network.serverpackets.ExSetPledgeEmblemAck;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Format : chdb
 * c (id) 0xD0
 * h (subid) 0x11
 * d data size
 * b raw data (picture i think ;) )
 *
 * @author -Wooden-
 */
public final class RequestExSetPledgeCrestLarge extends L2GameClientPacket
{
	static Logger _log = Logger.getLogger(RequestExSetPledgeCrestLarge.class.getName());

	private int _partId;
	private int _length;
	private byte[] _data;

	@Override
	protected void readImpl()
	{
		_partId = readD(); // sub id?
		@SuppressWarnings("unused") int unk = readH(); // ???
		//System.out.println("i " + subId);
		@SuppressWarnings("unused") int unk2 = readH(); // ???
		//System.out.println("s " + unk);
		_length = readD();
		_data = new byte[_length];
		readB(_data);
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
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

		if (_length <= 0)
		{
			activeChar.sendMessage("File transfer error.");
			return;
		}
		if (_length > 15000)
		{
			activeChar.sendMessage("The insignia file size is greater than 15000 bytes.");
			return;
		}

		boolean updated = false;
		int largeCrestId = -1;
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_REGISTER_CREST) == L2Clan.CP_CL_REGISTER_CREST)
		{
			if (_length == 0 || _data == null)
			{
				if (clan.getLargeCrestId() == 0)
				{
					return;
				}

				largeCrestId = 0;
				activeChar.sendMessage("The insignia has been removed.");
				updated = true;
			}
			else
			{
				if (!activeChar.isGM() && clan.getHasCastle() == 0 && clan.getHasHideout() == 0)
				{
					activeChar.sendMessage(
							"Only a clan that owns a clan hall or a castle can get their emblem displayed on clan related items"); //there is a system message for that but didnt found the id
					return;
				}

				if (_partId == 0)
				{
					largeCrestId = IdFactory.getInstance().getNextId();
					clan.setTempLargeCrestId(largeCrestId);
				}
				else
				{
					largeCrestId = clan.getTempLargeCrestId();
				}

				if (!CrestCache.getInstance().savePledgeCrestLarge(largeCrestId, _partId, _data))
				{
					Log.log(Level.INFO,
							"Error saving large crest for clan " + clan.getName() + " [" + clan.getClanId() + "]");
					return;
				}

				if (_partId == 4)
				{
					activeChar.sendPacket(
							SystemMessage.getSystemMessage(SystemMessageId.CLAN_EMBLEM_WAS_SUCCESSFULLY_REGISTERED));
					activeChar.sendPacket(new ExShowScreenMessage(
							"Please be aware that if your emblem is inappropiate, your\n clan might be deleted and your accounts permanently banned.",
							20000));
					updated = true;
				}
				else
				{
					activeChar.sendPacket(new ExSetPledgeEmblemAck(_partId));
				}
			}
		}

		if (updated && largeCrestId != -1)
		{
			clan.changeLargeCrest(largeCrestId);
		}
	}
}
