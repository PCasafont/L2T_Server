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
 *
 * @author -Wooden-
 */
public final class RequestPledgeReorganizeMember extends L2GameClientPacket
{

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
	 */
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		if (_isMemberSelected == 0 && _memberName.length() == 0)
		{
			activeChar.sendMessage("You did not select any member.");
			return;
		}

		final L2Clan clan = activeChar.getClan();
		if (clan == null)
		{
			activeChar.sendMessage("You do not have a clan.");
			return;
		}

		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_MANAGE_RANKS) != L2Clan.CP_CL_MANAGE_RANKS)
		{
			activeChar.sendMessage("You do not have the rights to do this.");
			return;
		}

		final L2ClanMember member1 = clan.getClanMember(_memberName);
		if (member1 == null || member1.getObjectId() == clan.getLeaderId())
		{
			activeChar.sendMessage("The selected member could not be found.");
			return;
		}

		final int oldPledgeType = member1.getPledgeType();
		if (oldPledgeType == _newPledgeType)
		{
			activeChar.sendMessage(member1.getName() + " is already in the selected squad.");
			return;
		}

		member1.setPledgeType(_newPledgeType);
		if (_selectedMember.length() != 0)
		{
			final L2ClanMember member2 = clan.getClanMember(_selectedMember);
			if (member2 == null || member2.getObjectId() == clan.getLeaderId())
			{
				activeChar.sendMessage("You did not select the member to swap " + member1.getName() + " with.");
			}
			else
			{
				member2.setPledgeType(oldPledgeType);
			}
		}

		clan.broadcastClanStatus();
	}
}
