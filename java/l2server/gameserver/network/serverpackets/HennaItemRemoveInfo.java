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

public class HennaItemRemoveInfo extends L2GameServerPacket
{

	private L2PcInstance activeChar;
	private L2Henna henna;

	public HennaItemRemoveInfo(L2Henna henna, L2PcInstance player)
	{
		this.henna = henna;
		this.activeChar = player;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(this.henna.getSymbolId()); //symbol Id
		writeD(this.henna.getDyeId()); //item id of dye
		writeQ(0x00); // total amount of dye require
		writeQ(this.henna.getPrice() / 5); //total amount of aden require to remove symbol
		writeD(1); //able to remove or not 0 is false and 1 is true
		writeQ(this.activeChar.getAdena());
		writeD(this.activeChar.getINT()); //current INT
		writeC(this.activeChar.getINT() - this.henna.getStatINT()); //equip INT
		writeD(this.activeChar.getSTR()); //current STR
		writeC(this.activeChar.getSTR() - this.henna.getStatSTR()); //equip STR
		writeD(this.activeChar.getCON()); //current CON
		writeC(this.activeChar.getCON() - this.henna.getStatCON()); //equip CON
		writeD(this.activeChar.getMEN()); //current MEM
		writeC(this.activeChar.getMEN() - this.henna.getStatMEM()); //equip MEM
		writeD(this.activeChar.getDEX()); //current DEX
		writeC(this.activeChar.getDEX() - this.henna.getStatDEX()); //equip DEX
		writeD(this.activeChar.getWIT()); //current WIT
		writeC(this.activeChar.getWIT() - this.henna.getStatWIT()); //equip WIT
		writeD(this.activeChar.getLUC()); //current LUC
		writeC(this.activeChar.getLUC() - this.henna.getStatLUC()); //equip LUC
		writeD(this.activeChar.getCHA()); //current CHA
		writeC(this.activeChar.getCHA() - this.henna.getStatCHA()); //equip CHA
	}
}
