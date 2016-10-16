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

public class HennaItemDrawInfo extends L2GameServerPacket
{

	private L2PcInstance activeChar;
	private L2Henna henna;

	public HennaItemDrawInfo(L2Henna henna, L2PcInstance player)
	{
		this.henna = henna;
		activeChar = player;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(henna.getSymbolId()); //symbol Id
		writeD(henna.getDyeId()); //item id of dye
		writeQ(henna.getAmountDyeRequire()); // total amount of dye require
		writeQ(henna.getPrice()); //total amount of aden require to draw symbol
		writeD(1); //able to draw or not 0 is false and 1 is true
		writeQ(activeChar.getAdena());
		writeD(activeChar.getINT()); //current INT
		writeC(activeChar.getINT() + henna.getStatINT()); //equip INT
		writeD(activeChar.getSTR()); //current STR
		writeC(activeChar.getSTR() + henna.getStatSTR()); //equip STR
		writeD(activeChar.getCON()); //current CON
		writeC(activeChar.getCON() + henna.getStatCON()); //equip CON
		writeD(activeChar.getMEN()); //current MEM
		writeC(activeChar.getMEN() + henna.getStatMEM()); //equip MEM
		writeD(activeChar.getDEX()); //current DEX
		writeC(activeChar.getDEX() + henna.getStatDEX()); //equip DEX
		writeD(activeChar.getWIT()); //current WIT
		writeC(activeChar.getWIT() + henna.getStatWIT()); //equip WIT
		writeD(activeChar.getLUC()); //current LUC
		writeC(activeChar.getLUC() + henna.getStatLUC()); //equip LUC
		writeD(activeChar.getCHA()); //current CHA
		writeC(activeChar.getCHA() + henna.getStatCHA()); //equip CHA
	}
}
