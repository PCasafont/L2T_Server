/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.TimeController;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 *
 * @version $Revision: 1.4.2.5.2.6 $ $Date: 2005/03/27 15:29:39 $
 */
public class CharSelected extends L2GameServerPacket
{
	// SdSddddddddddffddddddddddddddddddddddddddddddddddddddddd d
	private L2PcInstance _activeChar;
	private int _sessionId;

	/**
	 */
	public CharSelected(L2PcInstance cha, int sessionId)
	{
		_activeChar = cha;
		_sessionId = sessionId;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected final void writeImpl()
	{
		writeS(_activeChar.getName());
		writeD(_activeChar.getCharId()); // ??
		writeS(_activeChar.getTitle());
		writeD(_sessionId);
		writeD(_activeChar.getClanId());
		writeD(0x00); // ??
		writeD(_activeChar.getAppearance().getSex() ? 1 : 0);
		writeD(_activeChar.getRace().ordinal());
		writeD(_activeChar.getCurrentClass().getId());
		writeD(0x01); // active ??
		writeD(_activeChar.getX());
		writeD(_activeChar.getY());
		writeD(_activeChar.getZ());

		writeF(_activeChar.getCurrentHp());
		writeF(_activeChar.getCurrentMp());
		writeQ(_activeChar.getSp());
		writeQ(_activeChar.getExp());
		writeD(_activeChar.getLevel());
		writeD(_activeChar.getReputation()); // thx evill33t
		writeD(_activeChar.getPkKills());

		writeD(TimeController.getInstance().getGameTime() % (24 * 60)); // "reset" on 24th hour
		writeD(0x00);

		writeD(_activeChar.getCurrentClass().getId());

		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);

		writeB(new byte[64]);
		writeD(0x00);
	}
}
