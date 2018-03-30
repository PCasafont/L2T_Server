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

package l2server.gameserver.model;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.clientpackets.L2GameClientPacket;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * This class manages requests (transactions) between two L2PcInstance.
 *
 * @author kriau
 */
public class L2Request {
	private static final int REQUEST_TIMEOUT = 15; //in secs

	protected L2PcInstance player;
	protected L2PcInstance partner;
	protected boolean isRequestor;
	protected boolean isAnswerer;
	protected L2GameClientPacket requestPacket;

	public L2Request(L2PcInstance player) {
		this.player = player;
	}

	protected void clear() {
		partner = null;
		requestPacket = null;
		isRequestor = false;
		isAnswerer = false;
	}

	/**
	 * Set the L2PcInstance member of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR><BR>
	 */
	private synchronized void setPartner(L2PcInstance partner) {
		this.partner = partner;
	}

	/**
	 * Return the L2PcInstance member of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR><BR>
	 */
	public L2PcInstance getPartner() {
		return partner;
	}

	/**
	 * Set the packet incomed from requestor.<BR><BR>
	 */
	private synchronized void setRequestPacket(L2GameClientPacket packet) {
		requestPacket = packet;
	}

	/**
	 * Return the packet originally incomed from requestor.<BR><BR>
	 */
	public L2GameClientPacket getRequestPacket() {
		return requestPacket;
	}

	/**
	 * Checks if request can be made and in success case puts both PC on request state.<BR><BR>
	 */
	public synchronized boolean setRequest(L2PcInstance partner, L2GameClientPacket packet) {
		if (partner == null) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET));
			return false;
		}
		if (partner.getRequest().isProcessingRequest()) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
			sm.addString(partner.getName());
			player.sendPacket(sm);
			sm = null;
			return false;
		}
		if (isProcessingRequest()) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WAITING_FOR_ANOTHER_REPLY));
			return false;
		}

		this.partner = partner;
		requestPacket = packet;
		setOnRequestTimer(true);
		partner.getRequest().setPartner(player);
		partner.getRequest().setRequestPacket(packet);
		partner.getRequest().setOnRequestTimer(false);
		return true;
	}

	private void setOnRequestTimer(boolean isRequestor) {
		this.isRequestor = isRequestor;
		isAnswerer = !isRequestor;
		ThreadPoolManager.getInstance().scheduleGeneral(this::clear, REQUEST_TIMEOUT * 1000);
	}

	/**
	 * Clears PC request state. Should be called after answer packet receive.<BR><BR>
	 */
	public void onRequestResponse() {
		if (partner != null) {
			partner.getRequest().clear();
		}
		clear();
	}

	/**
	 * Return True if a transaction is in progress.<BR><BR>
	 */
	public boolean isProcessingRequest() {
		return partner != null;
	}
}
