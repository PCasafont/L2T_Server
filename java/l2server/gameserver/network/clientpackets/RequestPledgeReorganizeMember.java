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
package l2server.gameserver.network.clientpackets;

import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2ClanMember;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * Format: (ch) dSdS
 * @author  -Wooden-
 */
public final class RequestPledgeReorganizeMember extends L2GameClientPacket
{
	private static final String _C__D0_2C_REQUESTPLEDGEREORGANIZEMEMBER = "[C] D0:2C RequestPledgeReorganizeMember";
	
	private int _isMemberSelected;
	private String _memberName;
	private int _newPledgeType;
	private String _selectedMember;
	
	@Override
	protected void readImpl()
	{
		_isMemberSelected = readD();
		_memberName = readS();
		_newPledgeType = readD();
		_selectedMember = readS();
	}
	
	/**
	 * @see l2server.util.network.BaseRecievePacket.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		if (_isMemberSelected == 0)
			return;
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		final L2Clan clan = activeChar.getClan();
		if (clan == null)
			return;
		
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_MANAGE_RANKS) != L2Clan.CP_CL_MANAGE_RANKS)
			return;
		
		final L2ClanMember member1 = clan.getClanMember(_memberName);
		if (member1 == null || member1.getObjectId() == clan.getLeaderId())
			return;
		
		final L2ClanMember member2 = clan.getClanMember(_selectedMember);
		if (member2 == null || member2.getObjectId() == clan.getLeaderId())
			return;
		
		final int oldPledgeType = member1.getPledgeType();
		if (oldPledgeType == _newPledgeType)
			return;
		
		member1.setPledgeType(_newPledgeType);
		member2.setPledgeType(oldPledgeType);
		clan.broadcastClanStatus();
	}
	
	/**
	 * @see l2server.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_2C_REQUESTPLEDGEREORGANIZEMEMBER;
	}
}
