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

import l2server.gameserver.cache.CrestCache;
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.AllyCrest;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client packet for setting ally crest.
 */
public final class RequestSetAllyCrest extends L2GameClientPacket {
	static Logger log = Logger.getLogger(RequestSetAllyCrest.class.getName());
	
	private int length;
	private byte[] data;
	
	@Override
	protected void readImpl() {
		length = readD();
		if (length > 192) {
			return;
		}
		
		data = new byte[length];
		readB(data);
	}
	
	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}
		
		if (length < 0) {
			activeChar.sendMessage("File transfer error.");
			return;
		}
		if (length > 192) {
			activeChar.sendMessage("The ally crest file size was too big (max 192 bytes).");
			return;
		}
		
		if (activeChar.getAllyId() != 0) {
			L2Clan leaderclan = ClanTable.getInstance().getClan(activeChar.getAllyId());
			
			if (activeChar.getClanId() != leaderclan.getClanId() || !activeChar.isClanLeader()) {
				return;
			}
			
			boolean remove = false;
			if (length == 0 || data.length == 0) {
				remove = true;
			}
			
			int newId = 0;
			if (!remove) {
				newId = IdFactory.getInstance().getNextId();
			}
			
			if (!remove && !CrestCache.getInstance().saveAllyCrest(newId, data)) {
				log.info("Error saving crest for ally " + leaderclan.getAllyName() + " [" + leaderclan.getAllyId() + "]");
				return;
			}
			
			leaderclan.changeAllyCrest(newId, false);
			
			activeChar.sendPacket(new AllyCrest(newId));
		}
	}
}
