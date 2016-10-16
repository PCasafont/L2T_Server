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

	private final L2PcInstance activeChar;
	private final L2Henna[] hennas = new L2Henna[4];
	private int count;

	public HennaInfo(L2PcInstance player)
	{
		this.activeChar = player;

		int j = 0;
		for (int i = 0; i < 3; i++)
		{
			L2Henna henna = this.activeChar.getHenna(i + 1);
			if (henna != null)
			{
				this.hennas[j++] = henna;
			}
		}
		this.count = j;
	}

	@Override
	protected final void writeImpl()
	{
		writeH(this.activeChar.getHennaStatINT()); //equip INT
		writeH(this.activeChar.getHennaStatSTR()); //equip STR
		writeH(this.activeChar.getHennaStatCON()); //equip CON
		writeH(this.activeChar.getHennaStatMEN()); //equip MEM
		writeH(this.activeChar.getHennaStatDEX()); //equip DEX
		writeH(this.activeChar.getHennaStatWIT()); //equip WIT
		writeH(this.activeChar.getHennaStatLUC()); //equip LUC
		writeH(this.activeChar.getHennaStatCHA()); //equip CHA

		writeD(3);
		//writeD(4); // slots?
		writeD(this.count); //size
		for (int i = 0; i < this.count; i++)
		{
			writeD(this.hennas[i].getSymbolId());
			writeD(0x01); // Enabled
		}

		writeD(0x00);
		writeD(0x03);
		writeD(0x00);

		//4rth Slot dye information
		L2Henna dye = this.activeChar.getHenna(4);
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
