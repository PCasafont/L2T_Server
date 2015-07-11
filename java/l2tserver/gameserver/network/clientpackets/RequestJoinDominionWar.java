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

import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.SystemMessageId;
import l2tserver.gameserver.network.serverpackets.SystemMessage;

/**
 *
 * @author Gigiikun
 */
public final class RequestJoinDominionWar extends L2GameClientPacket
{
	private static final String _C__57_RequestJoinDominionWar = "[C] 57 RequestJoinDominionWar";
	// 
	
	@SuppressWarnings("unused")
	private int _territoryId;
	@SuppressWarnings("unused")
	private int _isClan;
	@SuppressWarnings("unused")
	private int _isJoining;
	
	@Override
	protected void readImpl()
	{
		_territoryId = readD();
		_isClan = readD();
		_isJoining = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_TERRITORY_REGISTRATION_PERIOD));
	}
	
	@Override
	public String getType()
	{
		return _C__57_RequestJoinDominionWar;
	}
}
