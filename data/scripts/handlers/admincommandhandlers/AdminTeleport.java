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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import l2tserver.Config;
import l2tserver.L2DatabaseFactory;
import l2tserver.gameserver.GeoData;
import l2tserver.gameserver.ai.CtrlIntention;
import l2tserver.gameserver.datatables.MapRegionTable;
import l2tserver.gameserver.datatables.NpcTable;
import l2tserver.gameserver.datatables.SpawnTable;
import l2tserver.gameserver.handler.IAdminCommandHandler;
import l2tserver.gameserver.instancemanager.GrandBossManager;
import l2tserver.gameserver.model.L2CharPosition;
import l2tserver.gameserver.model.L2Object;
import l2tserver.gameserver.model.L2Spawn;
import l2tserver.gameserver.model.L2World;
import l2tserver.gameserver.model.Location;
import l2tserver.gameserver.model.actor.L2Npc;
import l2tserver.gameserver.model.actor.instance.L2GrandBossInstance;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.model.actor.instance.L2RaidBossInstance;
import l2tserver.gameserver.model.zone.type.L2BossZone;
import l2tserver.gameserver.network.SystemMessageId;
import l2tserver.gameserver.network.serverpackets.NpcHtmlMessage;
import l2tserver.gameserver.network.serverpackets.SystemMessage;
import l2tserver.gameserver.templates.chars.L2NpcTemplate;
import l2tserver.util.StringUtil;


/**
 * This class handles following admin commands:
 * - show_moves
 * - show_teleport
 * - teleport_to_character
 * - move_to
 * - teleport_character
 *
 * @version $Revision: 1.3.2.6.2.4 $ $Date: 2005/04/11 10:06:06 $
 * con.close() change and small typo fix by Zoey76 24/02/2011
 */
public class AdminTeleport implements IAdminCommandHandler
{
	private static final Logger _log = Logger.getLogger(AdminTeleport.class.getName());
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_show_moves",
		"admin_show_moves_other",
		"admin_show_teleport",
		"admin_teleport_to_character",
		"admin_teleportto",
		"admin_move_to",
		"admin_teleport",
		"admin_teleport_character",
		"admin_recall",
		"admin_walk",
		"teleportto",
		"recall",
		"admin_sendhome",
		"admin_recall_npc",
		"admin_gonorth",
		"admin_gosouth",
		"admin_goeast",
		"admin_gowest",
		"admin_goup",
		"admin_godown",
		"admin_tele",
		"admin_teleto",
		"admin_instant_move"
	};
	
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_teleto"))
		{
			activeChar.setTeleMode(1);
		}
		if (command.equals("admin_instant_move"))
		{
			activeChar.setTeleMode(1);
		}
		if (command.equals("admin_teleto r"))
		{
			activeChar.setTeleMode(2);
		}
		if (command.equals("admin_teleto jump"))
		{
			activeChar.setTeleMode(3);
		}
		if (command.equals("admin_teleto end"))
		{
			activeChar.setTeleMode(0);
		}
		if (command.equals("admin_show_moves"))
		{
			AdminHelpPage.showHelpPage(activeChar, "teleports.htm");
		}
		if (command.equals("admin_show_moves_other"))
		{
			AdminHelpPage.showHelpPage(activeChar, "tele/other.html");
		}
		else if (command.equals("admin_show_teleport"))
		{
			showTeleportCharWindow(activeChar);
		}
		else if (command.equals("admin_recall_npc"))
		{
			recallNPC(activeChar);
		}
		else if (command.equals("admin_teleport_to_character"))
		{
			teleportToCharacter(activeChar, activeChar.getTarget());
		}
		else if (command.startsWith("admin_walk"))
		{
			try
			{
				String val = command.substring(11);
				StringTokenizer st = new StringTokenizer(val);
				String x1 = st.nextToken();
				int x = Integer.parseInt(x1);
				String y1 = st.nextToken();
				int y = Integer.parseInt(y1);
				String z1 = st.nextToken();
				int z = Integer.parseInt(z1);
				L2CharPosition pos = new L2CharPosition(x, y, z, 0);
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
			}
			catch (Exception e)
			{
				if (Config.DEBUG)
					_log.info("admin_walk: " + e);
			}
		}
		else if (command.startsWith("admin_move_to") || command.startsWith("admin_teleport "))
		{
			try
			{
				String val = command.substring(14).trim();
				teleportTo(activeChar, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				//Case of empty or missing coordinates
				AdminHelpPage.showHelpPage(activeChar, "teleports.htm");
			}
			catch (NumberFormatException nfe)
			{
				activeChar.sendMessage("Usage: //move_to <x> <y> <z>");
				AdminHelpPage.showHelpPage(activeChar, "teleports.htm");
			}
		}
		else if (command.startsWith("admin_teleport_character"))
		{
			try
			{
				String val = command.substring(25);
				
				teleportCharacter(activeChar, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				//Case of empty coordinates
				activeChar.sendMessage("Wrong or no Coordinates given.");
				showTeleportCharWindow(activeChar); //back to character teleport
			}
		}
		else if (command.startsWith("admin_teleportto "))
		{
			String targetName = null;
			L2PcInstance player = null;
			try
			{
				targetName = command.substring(17);
				player = L2World.getInstance().getPlayer(targetName);
				if (player == null)
				{
					for (L2PcInstance pl : L2World.getInstance().getAllPlayers().values())
					{
						if (pl != null && pl.getName().equalsIgnoreCase(targetName))
						{
							player = pl;
							break;
						}
					}
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Please provide a player or NPC name as argument!");
			}
			
			if (player != null)
			{
				teleportToCharacter(activeChar, player);
				return true;
			}
			
			// Tenkai custom - if no player can be found, try to find NPC with that name
			for (L2NpcTemplate temp : NpcTable.getInstance().getAllTemplates())
			{
				if (!temp.getName().equalsIgnoreCase(targetName)
						|| temp.getAllSpawns().isEmpty())
					continue;
				
				for (L2Spawn spawn : temp.getAllSpawns())
				{
					if (spawn != null)
					{
						activeChar.teleToLocation(spawn.getX(), spawn.getY(), spawn.getZ());
						return true;
					}
				}
			}
				
			activeChar.sendMessage("Unable to locate NPC or player with that name.");
		}
		else if (command.startsWith("admin_recall "))
		{
			try
			{
				String[] param = command.split(" ");
				if (param.length != 2)
				{
					for (L2PcInstance pl : L2World.getInstance().getAllPlayers().values())
					{
						if (pl != null && pl.getEvent() == null && pl.isInOlympiadMode()
								&& pl.getPrivateStoreType() == 0)
						{
							teleportCharacter(pl, activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar);
							break;
						}
					}
					activeChar.sendMessage("Usage: //recall <playername>");
					return false;
				}
				String targetName = param[1];
				L2PcInstance player = L2World.getInstance().getPlayer(targetName);
				if (player == null)
				{
					for (L2PcInstance pl : L2World.getInstance().getAllPlayers().values())
					{
						if (pl != null && pl.getName().equalsIgnoreCase(targetName))
						{
							player = pl;
							break;
						}
					}
				}
				if (player != null)
				{	
					L2BossZone zone = GrandBossManager.getInstance().getZone(activeChar);
					if (zone != null)
						zone.allowPlayerEntry(player, 30);
					teleportCharacter(player, activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar);
				}	
				else
					changeCharacterPosition(activeChar, targetName);
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_sendhome"))
		{
			L2PcInstance player = null;
			
			if (command.length() > 14)	// Parameter given
				player = L2World.getInstance().getPlayer(command.substring(15));
			else if (activeChar.getTarget() != null && activeChar.getTarget() instanceof L2PcInstance)	// Take targeted player
				player = (L2PcInstance)activeChar.getTarget();
			
			// GM has no one on target and didn't provide name as parameter
			if (player == null)
			{
				activeChar.sendMessage("Please provide an existing name as parameter or have someone on target!");
				return false;
			}
			
			// Determine closest town
			Location loc = MapRegionTable.getInstance().getTeleToLocation(player, MapRegionTable.TeleportWhereType.Town);
			
			// Send chosen player to town
			player.teleToLocation(loc, true);
			player.setInstanceId(0);
		}
		else if (command.equals("admin_tele"))
		{
			showTeleportWindow(activeChar);
		}
		else if (command.startsWith("admin_go"))
		{
			int intVal = 150;
			int x = activeChar.getX(), y = activeChar.getY(), z = activeChar.getZ();
			try
			{
				String val = command.substring(8);
				StringTokenizer st = new StringTokenizer(val);
				String dir = st.nextToken();
				if (st.hasMoreTokens())
					intVal = Integer.parseInt(st.nextToken());
				if (dir.equals("east"))
					x += intVal;
				else if (dir.equals("west"))
					x -= intVal;
				else if (dir.equals("north"))
					y -= intVal;
				else if (dir.equals("south"))
					y += intVal;
				else if (dir.equals("up"))
					z += intVal;
				else if (dir.equals("down"))
					z -= intVal;
				activeChar.teleToLocation(x, y, z, false);
				showTeleportWindow(activeChar);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //go<north|south|east|west|up|down> [offset] (default 150)");
			}
		}
		
		return true;
	}
	
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void teleportTo(L2PcInstance activeChar, String Coords)
	{
		try
		{
			StringTokenizer st = new StringTokenizer(Coords);
			String x1 = st.nextToken();
			int x = Integer.parseInt(x1);
			String y1 = st.nextToken();
			int y = Integer.parseInt(y1);
			int z = 0;
			if (st.hasMoreTokens())
				z = Integer.parseInt(st.nextToken());
			else
				z = GeoData.getInstance().getHeight(x, y, z);
			
			int instId = 0;
			if (st.hasMoreTokens())
				instId = Integer.parseInt(st.nextToken());
			
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			
			if (instId != 0)
				activeChar.setInstanceId(instId);
			
			activeChar.teleToLocation(x, y, z, false);
			
			activeChar.sendMessage("You have been teleported to " + Coords);
		}
		catch (NoSuchElementException nsee)
		{
			activeChar.sendMessage("Wrong or no Coordinates given.");
		}
	}
	
	private void showTeleportWindow(L2PcInstance activeChar)
	{
		AdminHelpPage.showHelpPage(activeChar, "move.htm");
	}
	
	private void showTeleportCharWindow(L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		final String replyMSG = StringUtil.concat(
				"<html><title>Teleport Character</title>" +
				"<body>" +
				"The character you will teleport is ",
				player.getName(),
				"." +
				"<br>" +
				"Co-ordinate x" +
				"<edit var=\"char_cord_x\" width=110>" +
				"Co-ordinate y" +
				"<edit var=\"char_cord_y\" width=110>" +
				"Co-ordinate z" +
				"<edit var=\"char_cord_z\" width=110>" +
				"<button value=\"Teleport\" action=\"bypass -h admin_teleport_character $char_cord_x $char_cord_y $char_cord_z\" width=60 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" +
				"<button value=\"Teleport near you\" action=\"bypass -h admin_teleport_character ",
				String.valueOf(activeChar.getX()),
				" ",
				String.valueOf(activeChar.getY()),
				" ",
				String.valueOf(activeChar.getZ()),
				"\" width=115 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" +
				"<center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>" +
				"</body></html>"
		);
		adminReply.setHtml(replyMSG);
		activeChar.sendPacket(adminReply);
	}
	
	private void teleportCharacter(L2PcInstance activeChar, String Cords)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		
		if (player.getObjectId() == activeChar.getObjectId())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_USE_ON_YOURSELF));
		}
		else
		{
			try
			{
				StringTokenizer st = new StringTokenizer(Cords);
				String x1 = st.nextToken();
				int x = Integer.parseInt(x1);
				String y1 = st.nextToken();
				int y = Integer.parseInt(y1);
				String z1 = st.nextToken();
				int z = Integer.parseInt(z1);
				teleportCharacter(player, x, y, z, null);
			}
			catch (NoSuchElementException nsee)
			{
			}
		}
	}
	
	/**
	 * @param player
	 * @param x
	 * @param y
	 * @param z
	 */
	private void teleportCharacter(L2PcInstance player, int x, int y, int z, L2PcInstance activeChar)
	{
		if (player != null)
		{
			// Check for jail
			if (player.isInJail())
			{
				activeChar.sendMessage("Sorry, player " + player.getName() + " is in Jail.");
			}
			else
			{
				// Set player to same instance as GM teleporting.
				if (activeChar != null && activeChar.getInstanceId() >= 0)
					player.setInstanceId(activeChar.getInstanceId());
				else
					player.setInstanceId(0);
				
				// Information
				activeChar.sendMessage("You have recalled " + player.getName());
				player.sendMessage("Admin is teleporting you.");
				
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				player.teleToLocation(x, y, z, true);
			}
		}
	}
	
	private void teleportToCharacter(L2PcInstance activeChar, L2Object target)
	{
		if (target == null)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		
		if (player.getObjectId() == activeChar.getObjectId())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_USE_ON_YOURSELF));
		}
		else
		{
			// move to targets instance
			activeChar.setInstanceId(target.getInstanceId());
			
			int x = player.getX();
			int y = player.getY();
			int z = player.getZ();
			
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			activeChar.teleToLocation(x, y, z, true);
			
			activeChar.sendMessage("You have teleported to character " + player.getName() + ".");
		}
	}
	
	private void changeCharacterPosition(L2PcInstance activeChar, String name)
	{
		Connection con = null;
		final int x = activeChar.getX();
		final int y = activeChar.getY();
		final int z = activeChar.getZ();
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=? WHERE char_name=?");
			statement.setInt(1, x);
			statement.setInt(2, y);
			statement.setInt(3, z);
			statement.setString(4, name);
			statement.execute();
			int count = statement.getUpdateCount();
			statement.close();
			if (count == 0)
				activeChar.sendMessage("Character not found or position unaltered.");
			else
				activeChar.sendMessage("Player's [" + name + "] position is now set to (" + x + "," + y + "," + z + ").");
		}
		catch (SQLException se)
		{
			activeChar.sendMessage("SQLException while changing offline character's position");
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	private void recallNPC(L2PcInstance activeChar)
	{
		L2Object obj = activeChar.getTarget();
		if (obj instanceof L2Npc && !((L2Npc)obj).isMinion() && !(obj instanceof L2RaidBossInstance) && !(obj instanceof L2GrandBossInstance))
		{
			L2Npc target = (L2Npc) obj;
			
			int monsterTemplate = target.getTemplate().NpcId;
			L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(monsterTemplate);
			if (template1 == null)
			{
				activeChar.sendMessage("Incorrect monster template.");
				_log.warning("ERROR: NPC " + target.getObjectId() + " has a 'null' template.");
				return;
			}
			
			L2Spawn spawn = target.getSpawn();
			if (spawn == null)
			{
				activeChar.sendMessage("Incorrect monster spawn.");
				_log.warning("ERROR: NPC " + target.getObjectId() + " has a 'null' spawn.");
				return;
			}
			int respawnTime = spawn.getRespawnDelay() / 1000;
			
			target.deleteMe();
			spawn.stopRespawn();
			SpawnTable.getInstance().deleteSpawn(spawn, true);
			
			try
			{
				//L2MonsterInstance mob = new L2MonsterInstance(monsterTemplate, template1);
				
				spawn = new L2Spawn(template1);
				//spawn.setCustom(true);
				spawn.setX(activeChar.getX());
				spawn.setY(activeChar.getY());
				spawn.setZ(activeChar.getZ());
				spawn.setHeading(activeChar.getHeading());
				spawn.setRespawnDelay(respawnTime);
				if (activeChar.getInstanceId() >= 0)
					spawn.setInstanceId(activeChar.getInstanceId());
				else
					spawn.setInstanceId(0);
				SpawnTable.getInstance().addNewSpawn(spawn, true);
				spawn.startRespawn();
				spawn.doSpawn();
				
				activeChar.sendMessage("Created " + template1.Name + " on " + target.getObjectId() + ".");
				
				if (Config.DEBUG)
				{
					_log.fine("Spawn at X=" + spawn.getX() + " Y=" + spawn.getY() + " Z=" + spawn.getZ());
					_log.warning("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") moved NPC " + target.getObjectId());
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Target is not in game.");
			}
			
		}
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
		}
	}
	
}
