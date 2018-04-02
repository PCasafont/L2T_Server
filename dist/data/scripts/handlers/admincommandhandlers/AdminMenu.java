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

package handlers.admincommandhandlers;

import l2server.Config;
import l2server.gameserver.datatables.AdminCommandAccessRights;
import l2server.gameserver.handler.AdminCommandHandler;
import l2server.gameserver.handler.IAdminCommandHandler;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.World;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

import java.util.StringTokenizer;

/**
 * This class handles following admin commands:
 * - handles every admin menu command
 *
 * @version $Revision: 1.3.2.6.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminMenu implements IAdminCommandHandler {

	private static final String[] ADMIN_COMMANDS =
			{"admin_char_manage", "admin_teleport_character_to_menu", "admin_recall_char_menu", "admin_recall_party_menu", "admin_recall_clan_menu",
					"admin_goto_char_menu", "admin_kick_menu", "admin_kill_menu", "admin_ban_menu", "admin_unban_menu"};

	@Override
	public boolean useAdminCommand(String command, Player activeChar) {
		if (command.equals("admin_char_manage")) {
			showMainPage(activeChar);
		} else if (command.startsWith("admin_teleport_character_to_menu")) {
			String[] data = command.split(" ");
			if (data.length == 5) {
				String playerName = data[1];
				Player player = World.getInstance().getPlayer(playerName);
				if (player != null) {
					teleportCharacter(player,
							Integer.parseInt(data[2]),
							Integer.parseInt(data[3]),
							Integer.parseInt(data[4]),
							activeChar,
							"Admin is teleporting you.");
				}
			}
			showMainPage(activeChar);
		} else if (command.startsWith("admin_recall_char_menu")) {
			try {
				String targetName = command.substring(23);
				Player player = World.getInstance().getPlayer(targetName);
				teleportCharacter(player, activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar, "Admin is teleporting you.");
			} catch (StringIndexOutOfBoundsException e) {
			}
		} else if (command.startsWith("admin_recall_party_menu")) {
			int x = activeChar.getX(), y = activeChar.getY(), z = activeChar.getZ();
			try {
				String targetName = command.substring(24);
				Player player = World.getInstance().getPlayer(targetName);
				if (player == null) {
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
					return true;
				}
				if (!player.isInParty()) {
					activeChar.sendMessage("Player is not in party.");
					teleportCharacter(player, x, y, z, activeChar, "Admin is teleporting you.");
					return true;
				}
				for (Player pm : player.getParty().getPartyMembers()) {
					teleportCharacter(pm, x, y, z, activeChar, "Your party is being teleported by an Admin.");
				}
			} catch (Exception e) {
				log.warn("", e);
			}
		} else if (command.startsWith("admin_recall_clan_menu")) {
			int x = activeChar.getX(), y = activeChar.getY(), z = activeChar.getZ();
			try {
				String targetName = command.substring(23);
				Player player = World.getInstance().getPlayer(targetName);
				if (player == null) {
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
					return true;
				}
				L2Clan clan = player.getClan();
				if (clan == null) {
					activeChar.sendMessage("Player is not in a clan.");
					teleportCharacter(player, x, y, z, activeChar, "Admin is teleporting you.");
					return true;
				}
				Player[] members = clan.getOnlineMembers(0);
				for (Player member : members) {
					teleportCharacter(member, x, y, z, activeChar, "Your clan is being teleported by an Admin.");
				}
			} catch (Exception e) {
				log.warn("", e);
			}
		} else if (command.startsWith("admin_goto_char_menu")) {
			try {
				String targetName = command.substring(21);
				Player player = World.getInstance().getPlayer(targetName);
				activeChar.setInstanceId(player.getInstanceId());
				teleportToCharacter(activeChar, player);
			} catch (StringIndexOutOfBoundsException e) {
			}
		} else if (command.equals("admin_kill_menu")) {
			handleKill(activeChar);
		} else if (command.startsWith("admin_kick_menu")) {
			StringTokenizer st = new StringTokenizer(command);
			if (st.countTokens() > 1) {
				st.nextToken();
				String player = st.nextToken();
				Player plyr = World.getInstance().getPlayer(player);
				String text;
				if (plyr != null) {
					plyr.logout();
					text = "You kicked " + plyr.getName() + " from the game.";
				} else {
					text = "Player " + player + " was not found in the game.";
				}
				activeChar.sendMessage(text);
			}
			showMainPage(activeChar);
		} else if (command.startsWith("admin_ban_menu")) {
			StringTokenizer st = new StringTokenizer(command);
			if (st.countTokens() > 1) {
				String subCommand = "admin_ban_char";
				if (!AdminCommandAccessRights.getInstance().hasAccess(subCommand, activeChar.getAccessLevel())) {
					activeChar.sendMessage("You don't have the access right to use this command!");
					log.warn("Character " + activeChar.getName() + " tryed to use admin command " + subCommand + ", but have no access to it!");
					return false;
				}
				IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler(subCommand);
				ach.useAdminCommand(subCommand + command.substring(14), activeChar);
			}
			showMainPage(activeChar);
		} else if (command.startsWith("admin_unban_menu")) {
			StringTokenizer st = new StringTokenizer(command);
			if (st.countTokens() > 1) {
				String subCommand = "admin_unban_char";
				if (!AdminCommandAccessRights.getInstance().hasAccess(subCommand, activeChar.getAccessLevel())) {
					activeChar.sendMessage("You don't have the access right to use this command!");
					log.warn("Character " + activeChar.getName() + " tryed to use admin command " + subCommand + ", but have no access to it!");
					return false;
				}
				IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler(subCommand);
				ach.useAdminCommand(subCommand + command.substring(16), activeChar);
			}
			showMainPage(activeChar);
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList() {
		return ADMIN_COMMANDS;
	}

	private void handleKill(Player activeChar) {
		handleKill(activeChar, null);
	}

	private void handleKill(Player activeChar, String player) {
		WorldObject obj = activeChar.getTarget();
		Creature target = (Creature) obj;
		String filename = "main_menu.htm";
		if (player != null) {
			Player plyr = World.getInstance().getPlayer(player);
			if (plyr != null) {
				target = plyr;
			}
			activeChar.sendMessage("You killed " + plyr.getName());
		}
		if (target != null) {
			if (target instanceof Player) {
				target.reduceCurrentHp(target.getMaxHp() + target.getMaxCp() + 1, activeChar, null);
				filename = "charmanage.htm";
			} else if (Config.L2JMOD_CHAMPION_ENABLE && target.isChampion()) {
				target.reduceCurrentHp(target.getMaxHp() * Config.L2JMOD_CHAMPION_HP + 1, activeChar, null);
			} else {
				target.reduceCurrentHp(target.getMaxHp() + 1, activeChar, null);
			}
		} else {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
		}
		AdminHelpPage.showHelpPage(activeChar, filename);
	}

	private void teleportCharacter(Player player, int x, int y, int z, Player activeChar, String message) {
		if (player != null) {
			player.sendMessage(message);
			player.teleToLocation(x, y, z, true);
		}
		showMainPage(activeChar);
	}

	private void teleportToCharacter(Player activeChar, WorldObject target) {
		Player player = null;
		if (target instanceof Player) {
			player = (Player) target;
		} else {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		if (player.getObjectId() == activeChar.getObjectId()) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_USE_ON_YOURSELF));
		} else {
			activeChar.setInstanceId(player.getInstanceId());
			activeChar.teleToLocation(player.getX(), player.getY(), player.getZ(), true);
			activeChar.sendMessage("You're teleporting yourself to character " + player.getName());
		}
		showMainPage(activeChar);
	}

	private void showMainPage(Player activeChar) {
		AdminHelpPage.showHelpPage(activeChar, "charmanage.htm");
	}
}
