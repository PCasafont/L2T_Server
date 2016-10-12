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

import l2server.Config;
import l2server.gameserver.model.BlockList;
import l2server.gameserver.model.L2Party;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2ApInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.AskJoinParty;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;

/**
 * sample
 * 29
 * 42 00 00 10
 * 01 00 00 00
 * <p>
 * format  cdd
 *
 * @version $Revision: 1.7.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestJoinParty extends L2GameClientPacket
{

	private String _name;
	private int _itemDistribution;

	@Override
	protected void readImpl()
	{
		_name = readS();
		_itemDistribution = readD();
		if (_itemDistribution < 0)
		{
			_itemDistribution = 0;
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance requestor = getClient().getActiveChar();
		L2PcInstance target = L2World.getInstance().getPlayer(_name);

		if (requestor == null)
		{
			return;
		}

		if (target == null)
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FIRST_SELECT_USER_TO_INVITE_TO_PARTY));
			return;
		}

		if (Config.isServer(Config.TENKAI) && target.getAppearance().getInvisible() && !requestor.isGM())
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return;
		}

		// LasTravel
		if (target.getIsRefusingRequests())
		{
			requestor.sendMessage("Your target have the requests blocked!");
			return;
		}

		if (requestor.getIsInsideGMEvent() && !target.getIsInsideGMEvent() ||
				!requestor.getIsInsideGMEvent() && target.getIsInsideGMEvent())
		{
			return;
		}

		if (!requestor.isGM())
		{
			if (requestor.isPlayingEvent() && requestor.getEvent() == target.getEvent() &&
					requestor.getEvent().getConfig().isAllVsAll())
			{
				requestor.sendMessage("You cannot make parties on this event!");
				return;
			}
		}

		if (target.isInParty() && !requestor.isGM())
		{
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_IN_PARTY);
			msg.addString(target.getName());
			requestor.sendPacket(msg);
			return;
		}

		if (BlockList.isBlocked(target, requestor) && !requestor.isGM())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
			sm.addCharName(target);
			requestor.sendPacket(sm);
			return;
		}

		if (target == requestor)
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET));
			return;
		}

		if (target.isCursedWeaponEquipped() || requestor.isCursedWeaponEquipped())
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}

		if (!requestor.isGM() && (target.isInJail() || requestor.isInJail()))
		{
			requestor.sendMessage("Player is in Jail");
			return;
		}
		/*
        if (target.getClient() == null || target.getClient().isDetached())
		{
			requestor.sendMessage("Player is in offline mode.");
			return;
		}*/

		if (target instanceof L2ApInstance)
		{
			requestor.sendMessage("You can't invite an artificial player.");
			return;
		}

		if (target.isInOlympiadMode() || requestor.isInOlympiadMode())
		{
			if (target.isInOlympiadMode() != requestor.isInOlympiadMode() ||
					target.getOlympiadGameId() != requestor.getOlympiadGameId() ||
					target.getOlympiadSide() != requestor.getOlympiadSide())
			{
				return;
			}
		}

		SystemMessage info = SystemMessage.getSystemMessage(SystemMessageId.C1_INVITED_TO_PARTY);
		info.addCharName(target);
		requestor.sendPacket(info);

		if (!requestor.isInParty()) //Asker has no party
		{
			createNewParty(target, requestor);
		}
		else
		//Asker is in party
		{
			addTargetToParty(target, requestor);
		}
	}

	/**
	 * @param target
	 * @param requestor
	 */
	private void addTargetToParty(L2PcInstance target, L2PcInstance requestor)
	{
		SystemMessage msg;
		L2Party party = requestor.getParty();

		if (party == null)
		{
			return;
		}

		if (requestor.isGM() && !target.isInParty())
		{
			target.joinParty(requestor.getParty());
			return;
		}

		// summary of ppl already in party and ppl that get invitation
		if (!party.isLeader(requestor))
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_LEADER_CAN_INVITE));
			return;
		}
		if (party.getMemberCount() >= Config.MAX_MEMBERS_IN_PARTY)
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTY_FULL));
			return;
		}
		if (party.getPendingInvitation() && !party.isInvitationRequestExpired())
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WAITING_FOR_ANOTHER_REPLY));
			return;
		}

		if (!target.isProcessingRequest())
		{
			requestor.onTransactionRequest(target);
			// in case a leader change has happened, use party's mode
			target.sendPacket(new AskJoinParty(requestor.getName(), party.getLootDistribution()));
			party.setPendingInvitation(true);

			if (Config.DEBUG)
			{
				Log.fine("sent out a party invitation to:" + target.getName());
			}
		}
		else
		{
			msg = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
			msg.addString(target.getName());
			requestor.sendPacket(msg);

			if (Config.DEBUG)
			{
				Log.warning(requestor.getName() + " already received a party invitation");
			}
		}
		msg = null;
	}

	/**
	 * @param target
	 * @param requestor
	 */
	private void createNewParty(L2PcInstance target, L2PcInstance requestor)
	{
		if (requestor.isGM())
		{
			if (!target.isInParty())
			{
				requestor.setParty(new L2Party(requestor, _itemDistribution));
				target.joinParty(requestor.getParty());
			}
			else if (target.getParty().getMemberCount() < Config.MAX_MEMBERS_IN_PARTY)
			{
				requestor.joinParty(target.getParty());
			}

			return;
		}

		if (!target.isProcessingRequest())
		{
			requestor.setParty(new L2Party(requestor, _itemDistribution));

			requestor.onTransactionRequest(target);
			target.sendPacket(new AskJoinParty(requestor.getName(), _itemDistribution));
			requestor.getParty().setPendingInvitation(true);

			if (Config.DEBUG)
			{
				Log.fine("sent out a party invitation to:" + target.getName());
			}
		}
		else
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WAITING_FOR_ANOTHER_REPLY));

			if (Config.DEBUG)
			{
				Log.warning(requestor.getName() + " already received a party invitation");
			}
		}
	}
}
