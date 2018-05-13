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

import l2server.gameserver.model.InstanceType;
import l2server.gameserver.datatables.DoorTable;
import l2server.gameserver.datatables.TeleportLocationTable;
import l2server.gameserver.model.L2TeleportLocation;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.templates.chars.NpcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.StringTokenizer;

/**
 * This class ...
 *
 * @version $Revision$ $Date$
 */
public class DoormenInstance extends NpcInstance {
	private static Logger log = LoggerFactory.getLogger(DoormenInstance.class.getName());

	public DoormenInstance(int objectID, NpcTemplate template) {
		super(objectID, template);
		setInstanceType(InstanceType.L2DoormenInstance);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command) {
		if (command.startsWith("Chat")) {
			showChatWindow(player);
			return;
		} else if (command.startsWith("open_doors")) {
			if (isOwnerClan(player)) {
				if (isUnderSiege()) {
					cannotManageDoors(player);
				} else {
					openDoors(player, command);
				}
			}
			return;
		} else if (command.startsWith("close_doors")) {
			if (isOwnerClan(player)) {
				if (isUnderSiege()) {
					cannotManageDoors(player);
				} else {
					closeDoors(player, command);
				}
			}
			return;
		} else if (command.startsWith("tele")) {
			if (isOwnerClan(player)) {
				doTeleport(player, command);
			}
			return;
		}
		super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(Player player) {
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		
		if (!isOwnerClan(player)) {
			html.setFile(player.getHtmlPrefix(), "doormen/" + getTemplate().NpcId + "-no.htm");
		} else {
			html.setFile(player.getHtmlPrefix(), "doormen/" + getTemplate().NpcId + ".htm");
		}
		
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	protected void openDoors(Player player, String command) {
		StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
		st.nextToken();
		
		while (st.hasMoreTokens()) {
			DoorTable.getInstance().getDoor(Integer.parseInt(st.nextToken())).openMe();
		}
	}
	
	protected void closeDoors(Player player, String command) {
		StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
		st.nextToken();
		
		while (st.hasMoreTokens()) {
			DoorTable.getInstance().getDoor(Integer.parseInt(st.nextToken())).closeMe();
		}
	}
	
	protected void cannotManageDoors(Player player) {
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), "doormen/" + getTemplate().NpcId + "-busy.htm");
		player.sendPacket(html);
	}
	
	protected void doTeleport(Player player, String command) {
		final int whereTo = Integer.parseInt(command.substring(5).trim());
		L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(whereTo);
		if (list != null) {
			if (!player.isAlikeDead()) {
				player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), false);
			}
		} else {
			log.warn("No teleport destination with id:" + whereTo);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	protected boolean isOwnerClan(Player player) {
		return true;
	}
	
	protected boolean isUnderSiege() {
		return false;
	}
}
