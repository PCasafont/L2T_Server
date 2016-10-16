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

import l2server.L2DatabaseFactory;
import l2server.gameserver.GeoData;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.events.chess.ChessEvent;
import l2server.gameserver.handler.IAdminCommandHandler;
import l2server.gameserver.instancemanager.DiscussionManager;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.olympiad.Olympiad;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExStartScenePlayer;
import l2server.gameserver.network.serverpackets.SkillCoolTime;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.util.Util;
import l2server.log.Log;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Pere
 */
public class AdminTenkai implements IAdminCommandHandler
{
	private static List<SpawnInfo> deletedSpawns = new ArrayList<SpawnInfo>();
	private static List<Integer> mobIds = new ArrayList<Integer>();

	private static final String[] ADMIN_COMMANDS = {
			"admin_chess_start",
			"admin_select_daily_event",
			"admin_start_daily_event",
			"admin_stop_daily_event",
			"admin_oly_ban",
			"admin_oly_unban",
			"admin_packet",
			"admin_refresh_skills",
			"admin_disable_global_chat",
			"admin_enable_global_chat",
			"admin_start_votations",
			"admin_end_votations",
			"admin_movie",
			"admin_observe_landrates",
			"admin_start_hh",
			"admin_stop_hh",

			//LT commands
			"admin_loc",
			"admin_devdelete",
			"admin_massdelete",
			"admin_devspawn",
			"admin_massspawn",
			"admin_dump"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_chess_start"))
		{
			ChessEvent.init();
			ChessEvent.startFight(activeChar, (L2PcInstance) activeChar.getTarget());
		}
		else if (command.equals("admin_select_daily_event"))
		{
			//DailyEventsManager.getInstance().selectEvent();
			//activeChar.sendMessage("The next daily event will be the number "+DailyEvent.type);
		}
		else if (command.equals("admin_start_daily_event"))
		{
			//HiddenChests.getInstance().getCurrentEvent().start();
		}
		else if (command.equals("admin_stop_daily_event"))
		{
			//DailyEventsManager.getInstance().getCurrentEvent().start();
		}
		else if (command.startsWith("admin_oly"))
		{
			boolean ban = command.startsWith("admin_oly_ban");
			int length = ban ? 14 : 16;
			if (command.length() > length)
			{
				String playerName = command.substring(length);
				int playerId = 0;
				Connection con = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement =
							con.prepareStatement("SELECT charId FROM characters WHERE char_name LIKE ?");
					statement.setString(1, playerName);
					ResultSet rset = statement.executeQuery();
					if (rset.next())
					{
						playerId = rset.getInt("charId");
					}
					rset.close();
					statement.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					try
					{
						con.close();
					}
					catch (Exception e)
					{
					}
				}
				if (ban)
				{
					Olympiad.getInstance().olyBan(playerId);
					activeChar.sendMessage("Character " + playerName + " banned from olympiad games this month!");
				}
				else
				{
					Olympiad.getInstance().olyUnban(playerId);
					activeChar.sendMessage("Character " + playerName + " unbanned from olympiad games.");
				}
			}
			else if (activeChar.getTarget() instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) activeChar.getTarget();
				if (ban)
				{
					Olympiad.getInstance().olyBan(player.getObjectId());
					player.logout();
					activeChar.sendMessage("Character " + player.getName() + " banned from olympiad games this month!");
				}
				else
				{
					Olympiad.getInstance().olyUnban(player.getObjectId());
					activeChar.sendMessage("Character " + player.getName() + " unbanned from olympiad games.");
				}
			}
			else
			{
				activeChar.sendMessage("Incorrect target.");
			}
		}
		else if (command.startsWith("admin_packet"))
		{
			/*StringTokenizer st = new StringTokenizer(command, " ");
			String token = st.nextToken();
			int type;
			int[] args = new int[50];
			switch (Integer.valueOf(st.nextToken()))
			{
				case 1:
					type = 0xaa;
					break;
				case 2:
					type = 0xa0;
					break;
				case 3:
					type = 0x60;
					break;
				case 4:
					type = 0x63;
					break;
				case 5:
					type = 0x64;
					break;
				case 6:
					type = 0x65;
					break;
				case 7:
					type = 0x66;
					break;
				default:
					type = 0x84;
					break;
			}
			int i = 0;
			while (st.hasMoreTokens())
			{
				token = st.nextToken();
				if (token.equals("id"))
					args[i] = activeChar.getObjectId();
				else
					args[i] = Integer.valueOf(token);
				i++;
			}*/
			//TestPacket tp = new TestPacket(type, args, i);
			//activeChar.sendPacket(tp);
		}
		else if (command.startsWith("admin_refresh_skills"))
		{
			L2PcInstance player = null;
			L2PcInstance[] players = null;

			if (command.length() > 21)
			{
				if (!command.substring(21).equalsIgnoreCase("all"))
				{
					player = L2World.getInstance().getPlayer(command.substring(21));
					if (player == null)
					{
						int radius = Integer.parseInt(command.substring(21));
						players = (L2PcInstance[]) activeChar.getKnownList().getKnownPlayersInRadius(radius).toArray();
					}
				}
				else
				{
					players = (L2PcInstance[]) L2World.getInstance().getAllPlayers().values().toArray();
				}
			}
			else if (activeChar.getTarget() != null && activeChar.getTarget().getActingPlayer() != null)
			{
				player = activeChar.getTarget().getActingPlayer();
			}
			else
			{
				activeChar.sendMessage("Usage: //refresh_skills <playername | radius | all>");
				return false;
			}

			if (player != null)
			{
				for (L2Skill skill : player.getAllSkills())
				{
					player.enableSkill(skill);
				}

				player.sendSkillList();
				player.sendPacket(new SkillCoolTime(player));
				activeChar.sendMessage(player.getName() + "'s skills have been refreshed.");
			}
			else if (players != null)
			{
				for (L2PcInstance p : players)
				{
					for (L2Skill skill : p.getAllSkills())
					{
						p.enableSkill(skill);
					}

					p.sendSkillList();
					p.sendPacket(new SkillCoolTime(p));
				}
				activeChar.sendMessage("All online players' skills have been refreshed.");
			}
		}
		else if (command.startsWith("admin_disable_global_chat"))
		{
			DiscussionManager.getInstance().setGlobalChatDisabled(true);
			activeChar.sendMessage("Global chat disabled.");
		}
		else if (command.startsWith("admin_enable_global_chat"))
		{
			DiscussionManager.getInstance().setGlobalChatDisabled(false);
			activeChar.sendMessage("Global chat enabled again.");
		}
		else if (command.startsWith("admin_start_votations"))
		{
			DiscussionManager.getInstance().startVotations();
			activeChar.sendMessage("Votations started.");
		}
		else if (command.startsWith("admin_end_votations"))
		{
			int[] votes = DiscussionManager.getInstance().endVotations();
			activeChar.sendMessage("Votations ended. Results:");
			for (int i = 0; i < votes.length; i++)
			{
				if (votes[i] > 0)
				{
					activeChar.sendMessage("Option " + i + ": " + votes[i] + " votes.");
				}
			}
		}
		else if (command.startsWith("admin_movie"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			int id = Integer.valueOf(st.nextToken());
			activeChar.setMovieId(id);
			activeChar.broadcastPacket(new ExStartScenePlayer(id));
		}
		else if (command.startsWith("admin_observe_landrates"))
		{
			L2PcInstance target = null;

			if (activeChar.getTarget() != null && activeChar.getTarget() instanceof L2PcInstance)
			{
				target = (L2PcInstance) activeChar.getTarget();

				if (!activeChar.isLandrateObservationActive(target))
				{
					target.registerLandratesObserver(activeChar);
					activeChar.sendMessage("You are now observing " + target.getName() + "'s land rates.");
				}
				else
				{
					target.stopLandrateObservation(activeChar);
					activeChar.sendMessage("You stopped observing " + target.getName() + "'s land rate.");
				}
			}
		}
		else if (command.startsWith("admin_loc"))
		{
			SpawnTable.getInstance().getAllSpawns(0);
			Log.warning(activeChar.getX() + ", " + activeChar.getY() + ", " + activeChar.getZ() + ", " +
					activeChar.getHeading());
		}
		else if (command.startsWith("admin_devdelete"))
		{
			handleDevDelete(activeChar);
		}
		else if (command.equalsIgnoreCase("admin_massdelete"))
		{
			handleDevMassDelete(activeChar);
		}
		else if (command.startsWith("admin_devspawn"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			try
			{
				st.nextToken();
				String id = st.nextToken();
				int respawnTime = 0;
				if (st.hasMoreTokens())
				{
					respawnTime = Integer.parseInt(st.nextToken());
				}

				spawnMonster(activeChar, id, respawnTime, false);
			}
			catch (Exception e)
			{ // Case of wrong or missing monster data
				AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
			}
		}
		else if (command.startsWith("admin_massspawn"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			try
			{
				st.nextToken();
				String id = st.nextToken();
				int rows = Integer.parseInt(st.nextToken());
				int columns = Integer.parseInt(st.nextToken());
				int separation = Integer.parseInt(st.nextToken());
				int width = columns * separation;
				int height = rows * separation;
				double heading = Util.convertHeadingToDegree(activeChar.getHeading()) * Math.PI / 180.0;
				double cos = Math.cos(heading);
				double sin = Math.sin(heading);
				for (int i = 0; i < columns; i++)
				{
					for (int j = 0; j < rows; j++)
					{
						int x = activeChar.getX() + (int) (cos * (-(width / 2 - separation / 2) + separation * i) -
								sin * (-(width / 2 - separation / 2) + separation * j));
						int y = activeChar.getY() + (int) (sin * (-(height / 2 - separation / 2) + separation * i) +
								cos * (-(height / 2 - separation / 2) + separation * j));

						spawnMonster(activeChar, id, 60, true, x, y, activeChar.getZ());
					}
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //massspawn <npcId> <rows> <columns> <separation>");
			}
		}
		else if (command.startsWith("admin_dump"))
		{
			dump();
		}
		return true;
	}

	public static void dump()
	{
		try
		{
			File outputFile = new File("../sql/server/spawnlist_OUTPUT.sql");

			LineNumberReader inputFile =
					new LineNumberReader(new BufferedReader(new FileReader(new File("../sql/server/spawnlist.sql"))));

			PrintWriter writeOutput = new PrintWriter(new FileWriter(outputFile));

			Log.warning("AdminDev: Deleting: ");

			String line = null;

			while ((line = inputFile.readLine()) != null)
			{
				if (line.trim().isEmpty())
				{
					writeOutput.println();
					continue;
				}

				for (SpawnInfo deleted : deletedSpawns)
				{
					if (!line.contains(
							deleted.getId() + "," + deleted.getX() + "," + deleted.getY() + "," + deleted.getZ()))
					{
						writeOutput.println(line);
						deletedSpawns.remove(deleted);
						Log.warning(
								deleted.getId() + "," + deleted.getX() + "," + deleted.getY() + "," + deleted.getZ());
						break;
					}
				}
			}

			inputFile.close();
			writeOutput.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error(e);
		}
		
		/*for (int id : mobIds)
			Log.warning("" + id);*/

		for (SpawnInfo parser : deletedSpawns)
		{
			Log.warning(parser.getId() + "," + parser.getX() + "," + parser.getY() + "," + parser.getZ() + ",0,0,");
		}
	}

	private void handleDevMassDelete(L2PcInstance activeChar)
	{
		Collection<L2Object> objects = activeChar.getKnownList().getKnownObjects().values();

		for (L2Object obj : objects)
		{
			if (obj instanceof L2MonsterInstance && GeoData.getInstance().canSeeTarget(activeChar, obj))
			{
				L2Npc target = (L2Npc) obj;

				if (!mobIds.contains(target.getNpcId()))
				{
					mobIds.add(target.getNpcId());
				}

				L2Spawn spawn = target.getSpawn();

				if (spawn == null)
				{
					continue;
				}

				deletedSpawns.add(new SpawnInfo(target.getNpcId(), spawn.getX(), spawn.getY(), spawn.getZ()));

				target.deleteMe();

				spawn.stopRespawn();

				SpawnTable.getInstance().deleteSpawn(spawn, true);

				activeChar.sendMessage("Deleted " + target.getName() + " from " + target.getObjectId() + ".");
			}
		}
	}

	private void handleDevDelete(L2PcInstance activeChar)
	{
		L2Object obj = activeChar.getTarget();
		if (obj instanceof L2Npc)
		{
			L2Npc target = (L2Npc) obj;

			if (!mobIds.contains(target.getNpcId()))
			{
				mobIds.add(target.getNpcId());
			}

			L2Spawn spawn = target.getSpawn();

			deletedSpawns.add(new SpawnInfo(target.getNpcId(), spawn.getX(), spawn.getY(), spawn.getZ()));

			target.deleteMe();

			spawn.stopRespawn();

			SpawnTable.getInstance().deleteSpawn(spawn, true);

			activeChar.sendMessage("Deleted " + target.getName() + " from " + target.getObjectId() + ".");
		}
		else
		{
			activeChar.sendMessage("Incorrect target.");
		}
	}

	private class SpawnInfo
	{
		private int id = 0;
		private int x = 0;
		private int y = 0;
		private int z = 0;

		public SpawnInfo(int ID, int X, int Y, int Z)
		{
			id = ID;
			x = X;
			y = Y;
			z = Z;
		}

		private int getId()
		{
			return id;
		}

		private int getX()
		{
			return x;
		}

		private int getY()
		{
			return y;
		}

		private int getZ()
		{
			return z;
		}
	}

	private void spawnMonster(L2PcInstance activeChar, String monsterId, int respawnTime, boolean permanent)
	{
		L2Object target = activeChar.getTarget();
		if (target == null)
		{
			target = activeChar;
		}

		spawnMonster(activeChar, monsterId, respawnTime, permanent, target.getX(), target.getY(), target.getZ());
	}

	private void spawnMonster(L2PcInstance activeChar, String monsterId, int respawnTime, boolean permanent, int x, int y, int z)
	{
		L2NpcTemplate template1;
		if (monsterId.matches("[0-9]*"))
		{
			int monsterTemplate = Integer.parseInt(monsterId);
			template1 = NpcTable.getInstance().getTemplate(monsterTemplate);
		}
		else
		{
			monsterId = monsterId.replace('_', ' ');
			template1 = NpcTable.getInstance().getTemplateByName(monsterId);
		}

		try
		{
			L2Spawn spawn = new L2Spawn(template1);
			spawn.setX(x);
			spawn.setY(y);
			spawn.setZ(z);
			spawn.setHeading(activeChar.getHeading());
			spawn.setRespawnDelay(respawnTime);
			if (activeChar.getInstanceId() > 0)
			{
				spawn.setInstanceId(activeChar.getInstanceId());
				permanent = false;
			}
			else
			{
				spawn.setInstanceId(0);
			}

			SpawnTable.getInstance().addNewSpawn(spawn, permanent);
			spawn.startRespawn();
			spawn.doSpawn();

			spawn.getNpc().setDisplayEffect(3);

			if (!permanent)
			{
				spawn.stopRespawn();
			}

			activeChar.sendMessage(
					"Created " + template1.Name + " on " + spawn.getX() + ", " + spawn.getY() + ", " + spawn.getZ());
			//<spawn npcId="80329" x="-185556" y="146669" z="-15314" heading="467" respawn="20" />
			Log.warning("<spawn npcId=\"" + template1.NpcId + "\"  x=\"" + spawn.getX() + "\" y=\"" + spawn.getY() +
					"\" z=\"" + spawn.getZ() + "\" heading=\"" + spawn.getHeading() + "\" respawn=\"20\" />");
			//_log.warning(spawn.getX() + ", " + spawn.getY() + ", " + spawn.getZ() + ", " + spawn.getHeading());
		}
		catch (Exception e)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_CANT_FOUND));
		}
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
