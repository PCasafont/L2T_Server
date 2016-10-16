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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.item.L2Henna;

public final class HennaInfo extends L2GameServerPacket
{

	private final L2PcInstance _activeChar;
	private final L2Henna[] _hennas = new L2Henna[4];
	private int _count;

	public HennaInfo(L2PcInstance player)
	{
		_activeChar = player;

		int j = 0;
		for (int i = 0; i < 3; i++)
		{
			L2Henna henna = _activeChar.getHenna(i + 1);
			if (henna != null)
			{
				_hennas[j++] = henna;
			}
		}
		_count = j;
	}

	@Override
	protected final void writeImpl()
	{
		writeH(_activeChar.getHennaStatINT()); //equip INT
		writeH(_activeChar.getHennaStatSTR()); //equip STR
		writeH(_activeChar.getHennaStatCON()); //equip CON
		writeH(_activeChar.getHennaStatMEN()); //equip MEM
		writeH(_activeChar.getHennaStatDEX()); //equip DEX
		writeH(_activeChar.getHennaStatWIT()); //equip WIT
		writeH(_activeChar.getHennaStatLUC()); //equip LUC
		writeH(_activeChar.getHennaStatCHA()); //equip CHA

		writeD(3);
		//writeD(4); // slots?
		writeD(_count); //size
		for (int i = 0; i < _count; i++)
		{
			writeD(_hennas[i].getSymbolId());
			writeD(0x01); // Enabled
		}

		writeD(0x00);
		writeD(0x03);
		writeD(0x00);

		//4rth Slot dye information
		L2Henna dye = _activeChar.getHenna(4);
		if (dye != null)
		{
			writeD(dye.getSymbolId());
			writeD((int) (dye.getExpiryTime() - System.currentTimeMillis()) / 1000); // Seconds
			writeD(0x01);
		}
		else
		{
			writeD(0x00);
			writeD((int) (-System.currentTimeMillis() / 1000)); // Weird, but that's what retail sends
			writeD(0x00);
		}
	}
}
