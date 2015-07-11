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
package l2tserver.gameserver.network.serverpackets;

import l2tserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * 
 * @author Erlandys
 */
public class ExResponseCommissionBuyItem extends L2GameServerPacket
{
	private static final String _S__FE_F8_EXRESPONSECOMMISSIONBUYITEM = "[S] FE:F8 ExResponseCommissionBuyItem";

	@SuppressWarnings("unused")
	private L2PcInstance _player;

	public ExResponseCommissionBuyItem(L2PcInstance player)
	{
		_player = player;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xF8);
		writeD(1); // unk
		writeD(0); // unk
		writeD(58); // Item ID
		writeQ(1); // count
	}
	
	@Override
	public String getType()
	{
		return _S__FE_F8_EXRESPONSECOMMISSIONBUYITEM;
	}
}
