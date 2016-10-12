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

	private L2PcInstance _activeChar;
	private L2Henna _henna;

	public HennaItemRemoveInfo(L2Henna henna, L2PcInstance player)
	{
		_henna = henna;
		_activeChar = player;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_henna.getSymbolId()); //symbol Id
		writeD(_henna.getDyeId()); //item id of dye
		writeQ(0x00); // total amount of dye require
		writeQ(_henna.getPrice() / 5); //total amount of aden require to remove symbol
		writeD(1); //able to remove or not 0 is false and 1 is true
		writeQ(_activeChar.getAdena());
		writeD(_activeChar.getINT()); //current INT
		writeC(_activeChar.getINT() - _henna.getStatINT()); //equip INT
		writeD(_activeChar.getSTR()); //current STR
		writeC(_activeChar.getSTR() - _henna.getStatSTR()); //equip STR
		writeD(_activeChar.getCON()); //current CON
		writeC(_activeChar.getCON() - _henna.getStatCON()); //equip CON
		writeD(_activeChar.getMEN()); //current MEM
		writeC(_activeChar.getMEN() - _henna.getStatMEM()); //equip MEM
		writeD(_activeChar.getDEX()); //current DEX
		writeC(_activeChar.getDEX() - _henna.getStatDEX()); //equip DEX
		writeD(_activeChar.getWIT()); //current WIT
		writeC(_activeChar.getWIT() - _henna.getStatWIT()); //equip WIT
		writeD(_activeChar.getLUC()); //current LUC
		writeC(_activeChar.getLUC() - _henna.getStatLUC()); //equip LUC
		writeD(_activeChar.getCHA()); //current CHA
		writeC(_activeChar.getCHA() - _henna.getStatCHA()); //equip CHA
	}
}
