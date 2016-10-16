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
	private L2PcInstance activeChar;
	private int sessionId;

	/**
	 */
	public CharSelected(L2PcInstance cha, int sessionId)
	{
		this.activeChar = cha;
		this.sessionId = sessionId;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected final void writeImpl()
	{
		writeS(this.activeChar.getName());
		writeD(this.activeChar.getCharId()); // ??
		writeS(this.activeChar.getTitle());
		writeD(this.sessionId);
		writeD(this.activeChar.getClanId());
		writeD(0x00); // ??
		writeD(this.activeChar.getAppearance().getSex() ? 1 : 0);
		writeD(this.activeChar.getRace().ordinal());
		writeD(this.activeChar.getCurrentClass().getId());
		writeD(0x01); // active ??
		writeD(this.activeChar.getX());
		writeD(this.activeChar.getY());
		writeD(this.activeChar.getZ());

		writeF(this.activeChar.getCurrentHp());
		writeF(this.activeChar.getCurrentMp());
		writeQ(this.activeChar.getSp());
		writeQ(this.activeChar.getExp());
		writeD(this.activeChar.getLevel());
		writeD(this.activeChar.getReputation()); // thx evill33t
		writeD(this.activeChar.getPkKills());

		writeD(TimeController.getInstance().getGameTime() % (24 * 60)); // "reset" on 24th hour
		writeD(0x00);

		writeD(this.activeChar.getCurrentClass().getId());

		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);

		writeB(new byte[64]);
		writeD(0x00);
	}
}
