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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.instancemanager.TownManager;
import l2server.gameserver.model.PartyMatchWaitingList;
import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.ArrayList;

/**
 * @author Gnacik
 */
public class ExListPartyMatchingWaitingRoom extends L2GameServerPacket {
	@SuppressWarnings("unused")
	private final L2PcInstance activeChar;
	@SuppressWarnings("unused")
	private int page;
	private int minlvl;
	private int maxlvl;
	@SuppressWarnings("unused")
	private int mode;
	private ArrayList<L2PcInstance> members;
	
	public ExListPartyMatchingWaitingRoom(L2PcInstance player, int page, int minlvl, int maxlvl, int mode) {
		activeChar = player;
		this.page = page;
		this.minlvl = minlvl;
		this.maxlvl = maxlvl;
		this.mode = mode;
		members = new ArrayList<>();
		for (L2PcInstance cha : PartyMatchWaitingList.getInstance().getPlayers()) {
			if (cha == null) {
				continue;
			}
			
			if (!cha.isPartyWaiting()) {
				PartyMatchWaitingList.getInstance().removePlayer(cha);
				continue;
			}
			
			if (cha.getLevel() < minlvl || cha.getLevel() > maxlvl) {
				continue;
			}
			
			members.add(cha);
		}
	}
	
	@Override
	protected final void writeImpl() {
		/*if (mode == 0)
        {
			writeD(0);
			writeD(0);
			return;
		}*/
		
		writeD(members.size());
		writeD(members.size());
		for (L2PcInstance member : members) {
			writeS(member.getName());
			writeD(member.getClassId());
			writeD(member.getLevel());
			writeD(TownManager.getClosestLocation(member));
			writeD(0x00); // ???
		}
	}
}
