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

import l2server.gameserver.instancemanager.TownManager;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExManagePartyRoomMember;
import l2server.gameserver.network.serverpackets.SystemMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gnacik
 */
public class PartyMatchRoom {
	private int id;
	private String title;
	private int loot;
	private int location;
	private int minlvl;
	private int maxlvl;
	private int maxmem;
	private final List<L2PcInstance> members = new ArrayList<>();

	public PartyMatchRoom(int id, String title, int loot, int minlvl, int maxlvl, int maxmem, L2PcInstance owner) {
		this.id = id;
		this.title = title;
		this.loot = loot;
		location = TownManager.getClosestLocation(owner);
		this.minlvl = minlvl;
		this.maxlvl = maxlvl;
		this.maxmem = maxmem;
		members.add(owner);
	}

	public List<L2PcInstance> getPartyMembers() {
		return members;
	}

	public void addMember(L2PcInstance player) {
		members.add(player);
	}

	public void deleteMember(L2PcInstance player) {
		if (player != getOwner()) {
			members.remove(player);
			notifyMembersAboutExit(player);
		} else if (members.size() == 1) {
			PartyMatchRoomList.getInstance().deleteRoom(id);
		} else {
			changeLeader(members.get(1));
			deleteMember(player);
		}
	}

	public void notifyMembersAboutExit(L2PcInstance player) {
		for (L2PcInstance member : getPartyMembers()) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEFT_PARTY_ROOM);
			sm.addCharName(player);
			member.sendPacket(sm);
			member.sendPacket(new ExManagePartyRoomMember(player, this, 2));
		}
	}

	public void changeLeader(L2PcInstance newLeader) {
		// Get current leader
		L2PcInstance oldLeader = members.get(0);
		// Remove new leader
		members.remove(newLeader);
		// Move him to first position
		members.set(0, newLeader);
		// Add old leader as normal member
		members.add(oldLeader);
		// Broadcast change
		for (L2PcInstance member : getPartyMembers()) {
			member.sendPacket(new ExManagePartyRoomMember(newLeader, this, 1));
			member.sendPacket(new ExManagePartyRoomMember(oldLeader, this, 1));
			member.sendPacket(SystemMessageId.PARTY_ROOM_LEADER_CHANGED);
		}
	}

	public int getId() {
		return id;
	}

	public int getLootType() {
		return loot;
	}

	public int getMinLvl() {
		return minlvl;
	}

	public int getMaxLvl() {
		return maxlvl;
	}

	public int getLocation() {
		return location;
	}

	public int getMembers() {
		return members.size();
	}

	public int getMaxMembers() {
		return maxmem;
	}

	public String getTitle() {
		return title;
	}

	public L2PcInstance getOwner() {
		return members.get(0);
	}

	/* SET  */

	public void setMinLvl(int minlvl) {
		this.minlvl = minlvl;
	}

	public void setMaxLvl(int maxlvl) {
		this.maxlvl = maxlvl;
	}

	public void setLocation(int loc) {
		location = loc;
	}

	public void setLootType(int loot) {
		this.loot = loot;
	}

	public void setMaxMembers(int maxmem) {
		this.maxmem = maxmem;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
