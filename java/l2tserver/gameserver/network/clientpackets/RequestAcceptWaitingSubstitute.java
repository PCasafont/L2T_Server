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

import l2tserver.gameserver.datatables.SkillTable;
import l2tserver.gameserver.instancemanager.PartySearchManager;
import l2tserver.gameserver.model.L2Party.messageType;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.serverpackets.UserInfo;

/**
 * @author Erlandys
 *
 */
public class RequestAcceptWaitingSubstitute extends L2GameClientPacket
{
	private static final String _C__D0_AB_REQUESTACCEPTWAITINGSUBSTITUTE = "[C] D0:AB RequestAcceptWaitingSubstitute";

	boolean _willJoin;

	@Override
	protected void readImpl()
	{
		_willJoin = readD() == 1;
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (_willJoin)
		{
			if (activeChar.getParty() == null)
				PartySearchManager.getInstance().removeLookingForParty(activeChar);
				if (activeChar.getPlayerForChange() != null)
				{
					L2PcInstance player = activeChar.getPlayerForChange();
					PartySearchManager.getInstance().removeChangeThisPlayer(player);
					if (player.getParty() != null)
					{
						activeChar.joinParty(player.getParty());
						player.sendPacket(new UserInfo(player));
						player.getParty().removePartyMember(player, messageType.Expelled);
						activeChar.setPlayerForChange(null);
						L2PcInstance leader = activeChar.getParty().getLeader();
						if (leader != null)
						{
							activeChar.teleToLocation(leader.getX(), leader.getY(), leader.getZ());
							for (L2PcInstance member : activeChar.getParty().getPartyMembers())
							{
								SkillTable.getInstance().getInfo(14534, 1).getEffects(member, member);
								SkillTable.getInstance().getInfo(14535, 1).getEffects(member, member);
							}
						}
					}
				}
			}
	}

	@Override
	public String getType()
	{
		return _C__D0_AB_REQUESTACCEPTWAITINGSUBSTITUTE;
	}
}
