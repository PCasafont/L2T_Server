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

package l2server.gameserver.model.actor.instance;

import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.instancemanager.ClanHallManager;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.entity.ClanHall;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.templates.chars.NpcTemplate;

public class ClanHallTeleporterInstance extends DoormenInstance {
	private boolean init = false;
	private ClanHall clanHall = null;
	
	public ClanHallTeleporterInstance(int objectID, NpcTemplate template) {
		super(objectID, template);
		setInstanceType(InstanceType.L2ClanHallDoormenInstance);
	}
	
	@Override
	public void showChatWindow(Player player) {
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		
		if (getClanHall() != null) {
			L2Clan owner = ClanTable.getInstance().getClan(getClanHall().getOwnerId());
			if (isOwnerClan(player)) {
				html.setFile(player.getHtmlPrefix(), "clanHallDoormen/doormen-tele.htm");
				html.replace("%clanname%", owner.getName());
			} else {
				if (owner != null && owner.getLeader() != null) {
					html.setFile(player.getHtmlPrefix(), "clanHallDoormen/doormen-no.htm");
					html.replace("%leadername%", owner.getLeaderName());
					html.replace("%clanname%", owner.getName());
				} else {
					html.setFile(player.getHtmlPrefix(), "clanHallDoormen/emptyowner.htm");
					html.replace("%hallname%", getClanHall().getName());
				}
			}
		} else {
			return;
		}
		
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	@Override
	protected final void openDoors(Player player, String command) {
		Location loc = getClanHall().getZone().getSpawnLoc();
		
		if (loc != null) {
			player.teleToLocation(loc, false);
			if (player.getPet() != null) {
				player.getPet().teleToLocation(loc, false);
			}
			for (SummonInstance summon : player.getSummons()) {
				summon.teleToLocation(loc, false);
			}
		}
	}
	
	@Override
	protected final void closeDoors(Player player, String command) {
		Location loc = getClanHall().getZone().getChaoticSpawnLoc();
		if (loc != null) {
			player.teleToLocation(loc, false);
			if (player.getPet() != null) {
				player.getPet().teleToLocation(loc, false);
			}
			for (SummonInstance summon : player.getSummons()) {
				summon.teleToLocation(loc, false);
			}
		} else {
			player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			if (player.getPet() != null) {
				player.getPet().teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
			for (SummonInstance summon : player.getSummons()) {
				summon.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
		}
	}
	
	private ClanHall getClanHall() {
		if (!init) {
			synchronized (this) {
				if (!init) {
					clanHall = ClanHallManager.getInstance().getNearbyClanHall(getX(), getY(), 500);
					init = true;
				}
			}
		}
		return clanHall;
	}
	
	@Override
	protected final boolean isOwnerClan(Player player) {
		if (player.getClan() != null && getClanHall() != null) {
			if (player.getClanId() == getClanHall().getOwnerId()) {
				return true;
			}
		}
		return false;
	}
}
