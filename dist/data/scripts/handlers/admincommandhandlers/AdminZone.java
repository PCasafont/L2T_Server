/*
 * $Header: AdminTest.java, 25/07/2005 17:15:21 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 25/07/2005 17:15:21 $
 * $Revision: 1 $
 * $Log: AdminTest.java,v $
 * Revision 1  25/07/2005 17:15:21  luisantonioa
 * Added copyright notice
 *
 *
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

import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.handler.IAdminCommandHandler;
import l2server.gameserver.instancemanager.GrandBossManager;
import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.World;
import l2server.gameserver.model.WorldRegion;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.zone.ZoneType;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.util.StringUtil;

import java.util.StringTokenizer;

/**
 * Small typo fix by Zoey76 24/02/2011
 */
public class AdminZone implements IAdminCommandHandler {
	private static final String[] ADMIN_COMMANDS = {"admin_zone_check", "admin_zone_reload", "admin_zone_visual", "admin_zone_visual_clear"};

	/**
	 * @see l2server.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, Player)
	 */
	@Override
	@SuppressWarnings("deprecation")
	public boolean useAdminCommand(String command, Player activeChar) {
		if (activeChar == null) {
			return false;
		}

		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command

		//String val = "";
		//if (st.countTokens() >= 1) {val = st.nextToken();}

		if (actualCommand.equalsIgnoreCase("admin_zone_check")) {
			showHtml(activeChar);
			activeChar.sendMessage("MapRegion: x:" + MapRegionTable.getInstance().getMapRegionX(activeChar.getX()) + " y:" +
					MapRegionTable.getInstance().getMapRegionY(activeChar.getY()) + " (" +
					MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY()) + ")");
			getGeoRegionXY(activeChar);
			activeChar.sendMessage("Closest Town: " + MapRegionTable.getInstance().getClosestTownName(activeChar));

			Location loc;

			loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.Castle);
			activeChar.sendMessage("TeleToLocation (Castle): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

			loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.ClanHall);
			activeChar.sendMessage("TeleToLocation (ClanHall): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

			loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.SiegeFlag);
			activeChar.sendMessage("TeleToLocation (SiegeFlag): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

			loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.Town);
			activeChar.sendMessage("TeleToLocation (Town): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());
		} else if (actualCommand.equalsIgnoreCase("admin_zone_reload")) {
			ZoneManager.getInstance().reload();
			activeChar.sendMessage("All Zones have been reloaded");
		} else if (actualCommand.equalsIgnoreCase("admin_zone_visual")) {
			String next = st.nextToken();
			if (next.equalsIgnoreCase("world")) {
				for (ZoneType zone : ZoneManager.getInstance().getAllZones()) {
					zone.visualizeZone(activeChar);
				}
				showHtml(activeChar);
			} else if (next.equalsIgnoreCase("all")) {
				for (ZoneType zone : ZoneManager.getInstance().getZones(activeChar)) {
					zone.visualizeZone(activeChar);
				}
				showHtml(activeChar);
			} else {
				int zoneId = Integer.parseInt(next);
				ZoneManager.getInstance().getZoneById(zoneId).visualizeZone(activeChar);
			}
		} else if (actualCommand.equalsIgnoreCase("admin_zone_visual_clear")) {
			//ZoneManager.getInstance().clearDebugItems();
			showHtml(activeChar);
		}
		return true;
	}

	private static void showHtml(Player activeChar) {
		final String htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "admin/zone.htm");
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(htmContent);
		adminReply.replace("%BOSS%", GrandBossManager.getInstance().checkIfInZone(activeChar) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%PEACE%", activeChar.isInsideZone(Creature.ZONE_PEACE) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%PVP%", activeChar.isInsideZone(Creature.ZONE_PVP) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%SIEGE%", activeChar.isInsideZone(Creature.ZONE_SIEGE) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%TOWN%", activeChar.isInsideZone(Creature.ZONE_TOWN) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%CASTLE%", activeChar.isInsideZone(Creature.ZONE_CASTLE) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%FORT%", activeChar.isInsideZone(Creature.ZONE_FORT) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%NOHQ%", activeChar.isInsideZone(Creature.ZONE_NOHQ) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%CLANHALL%", activeChar.isInsideZone(Creature.ZONE_CLANHALL) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%LAND%", activeChar.isInsideZone(Creature.ZONE_LANDING) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%NOLAND%", activeChar.isInsideZone(Creature.ZONE_NOLANDING) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%NOSUMMON%", activeChar.isInsideZone(Creature.ZONE_NOSUMMONFRIEND) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%WATER%", activeChar.isInsideZone(Creature.ZONE_WATER) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%SWAMP%", activeChar.isInsideZone(Creature.ZONE_SWAMP) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%DANGER%", activeChar.isInsideZone(Creature.ZONE_DANGERAREA) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%NOSTORE%", activeChar.isInsideZone(Creature.ZONE_NOSTORE) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%SCRIPT%", activeChar.isInsideZone(Creature.ZONE_SCRIPT) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		StringBuilder zones = new StringBuilder(100);
		WorldRegion region = World.getInstance().getRegion(activeChar.getX(), activeChar.getY());
		for (ZoneType zone : region.getZones()) {
			if (zone.isCharacterInZone(activeChar)) {
				if (zone.getName() != null) {
					StringUtil.append(zones, zone.getName() + (zone.getId() < 300000 ? " (" + String.valueOf(zone.getId()) + ")<br1>" : "<br1>"));
				} else {
					StringUtil.append(zones, String.valueOf(zone.getId()));
				}
				StringUtil.append(zones, " ");
			}
		}
		adminReply.replace("%ZLIST%", zones.toString());
		activeChar.sendPacket(adminReply);
	}

	private static void getGeoRegionXY(Player activeChar) {
		int worldX = activeChar.getX();
		int worldY = activeChar.getY();
		int geoX = (worldX - (-327680) >> 4 >> 11) + 10;
		int geoY = (worldY - (-262144) >> 4 >> 11) + 10;
		activeChar.sendMessage("GeoRegion: " + geoX + "_" + geoY + "");
	}

	/**
	 * @see l2server.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
	 */
	@Override
	public String[] getAdminCommandList() {
		return ADMIN_COMMANDS;
	}
}
