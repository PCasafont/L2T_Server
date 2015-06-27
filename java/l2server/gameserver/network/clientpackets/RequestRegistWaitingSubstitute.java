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

import l2server.gameserver.instancemanager.PartySearchManager;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ExRegistWaitingSubstituteOk;

/**
 * @author Erlandys
 *
 */
public class RequestRegistWaitingSubstitute extends L2GameClientPacket
{
	private static final String _C__D0_AA_REQUESTREGISTWAITINGSUBSTITUTE = "[C] D0:AA RequestRegistWaitingSubstitute";

	int _id = 0;

	@Override
	protected void readImpl()
	{
		_id = readD();
	}

	@Override
	protected void runImpl()
	{
		PartySearchManager psm = PartySearchManager.getInstance();
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar.getParty() != null)
			return;

		if (activeChar.isSearchingForParty())
			activeChar.closeWaitingSubstitute();
		else
		{

			L2PcInstance changingPlayer = psm.getWannaToChangeThisPlayer(activeChar.getLevel(), activeChar.getClassId());
			if (changingPlayer != null)
			{
				changingPlayer.getParty().getLeader().sendMessage(activeChar.getName() + " meets the requirements to change " + changingPlayer.getName() + "."); // Not retail thing, need something to do with that......
				activeChar.sendPacket(new ExRegistWaitingSubstituteOk(activeChar.getClassId(), changingPlayer));
				activeChar.setPlayerForChange(changingPlayer);
				psm.removeLookingForParty(activeChar);
				return;
			}
			activeChar.showWaitingSubstitute();
		}
	}

	@Override
	public String getType()
	{
		return _C__D0_AA_REQUESTREGISTWAITINGSUBSTITUTE;
	}
}
