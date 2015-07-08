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

import l2tserver.gameserver.instancemanager.PartySearchManager;
import l2tserver.gameserver.model.L2World;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.SystemMessageId;
import l2tserver.gameserver.network.serverpackets.PartySmallWindowUpdate;
import l2tserver.gameserver.network.serverpackets.SystemMessage;
import l2tserver.gameserver.network.serverpackets.UserInfo;

/**
 * @author Erlandys
 *
 */
public class RequestDeletePartySubstitute extends L2GameClientPacket
{
	private static final String _C__D0_A9_REQUESTDELETEPARTYSUBSTITUTE = "[C] D0:A9 RequestDeletePartySubstitute";

	int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance leader = getClient().getActiveChar();
		if (leader == null)
			return;
		PartySearchManager psm = PartySearchManager.getInstance();
		SystemMessage sm1;
		if (L2World.getInstance().getPlayer(_objectId) != null)
		{
			L2PcInstance activeChar = L2World.getInstance().getPlayer(_objectId);
			if (psm.getWannaToChangeThisPlayer(activeChar.getLevel(), activeChar.getClassId()) != null)
			{
				sm1 = SystemMessage.getSystemMessage(SystemMessageId.STOPPED_LOOKING_FOR_A_PLAYER_WHO_WILL_REPLACE_S1);
				sm1.addCharName(activeChar);
				leader.sendPacket(sm1);
				psm.removeChangeThisPlayer(activeChar);
			}
	
			leader.getParty().broadcastToPartyMembers(activeChar, new PartySmallWindowUpdate(activeChar));
			activeChar.sendPacket(new UserInfo(activeChar));
		}
	}

	@Override
	public String getType()
	{
		return _C__D0_A9_REQUESTDELETEPARTYSUBSTITUTE;
	}
}
