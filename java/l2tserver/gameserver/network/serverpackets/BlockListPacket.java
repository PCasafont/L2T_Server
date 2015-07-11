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

import l2tserver.gameserver.datatables.CharNameTable;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Erlando
 *
 */
public class BlockListPacket extends L2GameServerPacket
{

	private static final String _S__D5_BLOCKLIST = "[S] D5 BlockList";

	private L2PcInstance player;

	public BlockListPacket(L2PcInstance activeChar)
	{
		player = activeChar;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xD5);
		writeD(player.getBlockList().getBlockList().size());
		for (int objId : player.getBlockList().getBlockList())
		{
			writeS(CharNameTable.getInstance().getNameById(objId));
			writeS(player.getBlockMemo(objId));
		}
		
	}

	@Override
	public String getType()
	{
		return _S__D5_BLOCKLIST;
	}
	
}
