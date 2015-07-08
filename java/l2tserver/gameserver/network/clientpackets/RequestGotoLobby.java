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
package l2tserver.gameserver.network.clientpackets;

import l2tserver.gameserver.network.L2GameClient;
import l2tserver.gameserver.network.serverpackets.CharSelectionInfo;

/**
 * (ch)
 * @author  KenM
 */
public class RequestGotoLobby extends L2GameClientPacket
{
	private static final String _C__D0_38_REQUESTGOTOLOBBY = "[C] D0:38 RequestGotoLobby";
	
	/**
	 * @see l2tserver.gameserver.network.clientpackets.L2GameClientPacket#readImpl()
	 */
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	/**
	 * @see l2tserver.gameserver.network.clientpackets.L2GameClientPacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2GameClient client = this.getClient();
		client.sendPacket(new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1));
	}
	
	/**
	 * @see l2tserver.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_38_REQUESTGOTOLOBBY;
	}
}
