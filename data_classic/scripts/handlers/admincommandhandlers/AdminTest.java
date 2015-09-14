/*
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.bots.BotMode;
import l2server.gameserver.bots.BotType;
import l2server.gameserver.bots.BotsManager;
import l2server.gameserver.bots.controllers.BotController;
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.IAdminCommandHandler;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.base.Experience;
import l2server.gameserver.model.base.Race;
import l2server.gameserver.network.L2GameClient;
import l2server.gameserver.network.L2GameClient.GameClientState;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.CharSelected;
import l2server.gameserver.network.serverpackets.CharSelectionInfo;
import l2server.gameserver.network.serverpackets.ExOlympiadMode;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.network.serverpackets.RestartResponse;
import l2server.log.Log;
import l2server.util.Rnd;


/**
 * This class ...
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class AdminTest implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_stats",
		"admin_skill_test",
		"admin_known",
		"admin_test",
		"admin_do"
	};
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, l2server.gameserver.model.L2PcInstance)
	 */
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		
		st.nextToken();
		
		if (command.equals("admin_stats"))
		{
			for (String line : ThreadPoolManager.getInstance().getStats())
			{
				activeChar.sendMessage(line);
			}
		}
		else if (command.startsWith("admin_skill_test") || command.startsWith("admin_st"))
		{
			try
			{
				int id = Integer.parseInt(st.nextToken());
				if (command.startsWith("admin_skill_test"))
					adminTestSkill(activeChar, id, true);
				else
					adminTestSkill(activeChar, id, false);
			}
			catch (NumberFormatException e)
			{
				activeChar.sendMessage("Command format is //skill_test <ID>");
			}
			catch (NoSuchElementException nsee)
			{
				activeChar.sendMessage("Command format is //skill_test <ID>");
			}
		}
		else if (command.equals("admin_known on"))
		{
			Config.CHECK_KNOWN = true;
		}
		else if (command.equals("admin_known off"))
		{
			Config.CHECK_KNOWN = false;
		}
		else if (command.toLowerCase().startsWith("admin_test"))
		{
			//TradeController.getInstance().reload();
			//activeChar.sendMessage("Shops have been successfully reloaded!");
			BotsManager.getInstance();
		}
		else if (command.startsWith("admin_do"))
		{
			if (!st.hasMoreTokens())
			{
				activeChar.sendMessage("You forgot to tell me what to execute. Ex: onExecute InventoryToMultisell");
				return false;
			}
			
			String secondaryCommand = st.nextToken();
			
			if (secondaryCommand.equals("TeleportAllPlayersToMe"))
			{
				for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
					player.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			}
			else if (secondaryCommand.equals("OlyCamera"))
			{
				if (activeChar.getTarget() instanceof L2PcInstance)
				{
					final L2PcInstance target = (L2PcInstance) activeChar.getTarget();
					
					target.sendPacket(new ExOlympiadMode(3));
				}
				else
					activeChar.sendPacket(new ExOlympiadMode(3));
			}
			else if (secondaryCommand.equals("Login"))
			{
				if (!st.hasMoreTokens())
				{
					activeChar.sendMessage("Specify the name of the character to log into.");
					return false;
				}
				
				final String logIntoCharacterName = st.nextToken();
				
				String charNameToSwitch = "";
				if (st.hasMoreTokens())
				{
					charNameToSwitch = st.nextToken();
				}
				
				L2PcInstance toon = activeChar;
				if (!charNameToSwitch.equals(""))
				{
					toon = L2World.getInstance().getPlayer(charNameToSwitch);
					activeChar.sendMessage("Logging " + toon.getName() + " into " + logIntoCharacterName);
				}
				
				final int charId = CharNameTable.getInstance().getIdByName(logIntoCharacterName);
				
				if (charId == 0)
				{
					activeChar.sendMessage("No character with such name. Try again.");
					return false;
				}
				
				final L2GameClient gameClient = toon.getClient();

				toon.setClient(null);
				
				gameClient.saveCharToDisk();
				
				gameClient.setActiveChar(null);
				
				// return the client to the authed status
				gameClient.setState(GameClientState.AUTHED);
				
				gameClient.sendPacket(RestartResponse.STATIC_PACKET_TRUE);
				
				// send char list
				CharSelectionInfo cl = new CharSelectionInfo(gameClient.getAccountName(), gameClient.getSessionId().playOkID1);
				gameClient.sendPacket(cl);
				gameClient.setCharSelection(cl.getCharInfo());
				
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
					public void run()
					{
						L2PcInstance cha = L2PcInstance.load(charId);
						
						if (cha == null)
						{
							gameClient.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
						
						cha.setClient(gameClient);
						gameClient.setActiveChar(cha);
						
						//BotsManager.getInstance().logPlayer(cha, true);
						
						gameClient.setState(GameClientState.IN_GAME);
						
						CharSelected cs = new CharSelected(cha, gameClient.getSessionId().playOkID1);
						gameClient.sendPacket(cs);
					}
				}, 1000);
			}
			else if (secondaryCommand.equals("CreateBot"))
			{
				if (!st.hasMoreTokens())
				{
					activeChar.sendMessage("Type the ClassId of the character you would like to create.");
					return false;
				}
				
				int classId = -1;
				
				BotType botType = null;
				String botTypeInput = "";
				
				if (st.hasMoreTokens())
				{
					String nextToken = st.nextToken();
					try
					{
						classId = Integer.parseInt(nextToken);
					}
					catch (Exception e)
					{
						botTypeInput = nextToken;
					}
					
					if (!botTypeInput.equals(""))
					{
						try
						{
							botType = BotType.valueOf(botTypeInput);
						}
						catch (Exception e)
						{
							activeChar.sendMessage("Specified BotType is unknown. Try REGULAR or TESTER.");
							return false;
						}
					}
				}
				
				final L2PcInstance player = BotsManager.getInstance().createPlayer(botType, classId);
				
				//. ...
				player.setClassId(classId);
				player.setBaseClass(player.getActiveClass());
				player.broadcastUserInfo();
				
				long allExpToAdd = Experience.getAbsoluteExp(99) - player.getExp();
				
				player.addExpAndSp(allExpToAdd, 999999);
				player.giveAvailableSkills(true);
				player.store();
				
				player.setCurrentMp(player.getMaxMp());
				player.setCurrentHp(player.getMaxHp());
				player.setCurrentCp(player.getMaxCp());
				
				player.getBotController().onEnterWorld(false);
				
				player.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ());
				
				player.getBotController().broadcastDebugMessage("I'm ready!!!");
			}
			else if (secondaryCommand.equals("CreateBots"))
			{
				if (!st.hasMoreTokens())
				{
					activeChar.sendMessage("Type the amount of characters you would like to create.");
					return false;
				}
				
				final int amount = Integer.parseInt(st.nextToken());
				
				int classId = -1;
				
				BotType botType = null;
				String botTypeInput = "";
				
				if (st.hasMoreTokens())
				{
					String nextToken = st.nextToken();
					try
					{
						classId = Integer.parseInt(nextToken);
					}
					catch (Exception e)
					{
						botTypeInput = nextToken;
					}
					
					if (!botTypeInput.equals(""))
					{
						try
						{
							botType = BotType.valueOf(botTypeInput);
						}
						catch (Exception e)
						{
							activeChar.sendMessage("Specified BotType is unknown. Try REGULAR or TESTER.");
							return false;
						}
					}
				}
				
				int createdBots = 0;
				
				while (createdBots++ < amount)
				{
					// If there was no class specified...
					if (classId == -1)
					{
						// We create one (or more, depends on the amount specified) character of EVERY classes.
						for (int i = 0; i < BotsManager.AWAKANED_CLASSES_IDS.length; i++)
						{
							// Not available yet...
							if (!((Boolean) BotsManager.AWAKANED_CLASSES_IDS[i][5]))
								continue;
							
							// We spawn them in line, front of us...
							float headingAngle = (float) (activeChar.getHeading() * Math.PI) / Short.MAX_VALUE;
							
							int x = 0, y = 0, z = 0;
							
							if (botType == BotType.TESTER)
							{
								x = (int) (activeChar.getX() + (i * 50) * (float) Math.cos(headingAngle));
								y = (int) (activeChar.getY() + (i * 50) * (float) Math.sin(headingAngle));
								z = activeChar.getZ() + 1;
							}
							else
							{
								x = activeChar.getX() + Rnd.get(-2000, 2000);
								y = activeChar.getY() + Rnd.get(-2000, 2000);
								z = activeChar.getZ();
							}
							// Create the bot...
							final L2PcInstance player = 
									BotsManager.getInstance().createPlayer(botType, (Integer) BotsManager.AWAKANED_CLASSES_IDS[i][0]);
							
							if (player == null)
								continue;
							
							// Now, get the last class and top level...
							player.setClassId((Integer) BotsManager.AWAKANED_CLASSES_IDS[i][0]);
							player.setBaseClass(player.getActiveClass());
							player.broadcastUserInfo();
							
							// 1 / 2 is a newbie ^_^
							int randomLevel = Rnd.nextBoolean() ? Rnd.get(85, 95) : Rnd.get(0, 4) == 0 ? Rnd.get(96, 99) : 99;
							
							if (player.getRace() == Race.Kamael)
								randomLevel = Rnd.get(96, 99); // No S80 Kamaels. Fuck it.
							
							long allExpToAdd = Experience.getAbsoluteExp(randomLevel) - player.getExp();
							
							player.addExpAndSp(allExpToAdd, 999999999);
							player.giveAvailableSkills(true);
							player.store();
							
							player.setCurrentMp(player.getMaxMp());
							player.setCurrentHp(player.getMaxHp());
							player.setCurrentCp(player.getMaxCp());
							
							//player.setTitle((String) BotsManager.AWAKANED_CLASSES_IDS[i][1]);
							player.broadcastTitleInfo();
							
							// Login. He'll get geared up and stuff.
							player.getBotController().onEnterWorld(false);
							
							player.teleToLocation(x, y, z);
						}
					}
					else
					{
						// We spawn them in line, front of us...
						float headingAngle = (float) (activeChar.getHeading() * Math.PI) / Short.MAX_VALUE;
						
						float x = activeChar.getX() + (createdBots * 50) * (float) Math.cos(headingAngle);
						float y = activeChar.getY() + (createdBots * 50) * (float) Math.sin(headingAngle);
						float z = activeChar.getZ() + 1;
						
						final L2PcInstance player = BotsManager.getInstance().createPlayer(botType, classId);
						
						// Login. He'll get geared up and stuff.
						player.getBotController().onEnterWorld(false);
						
						player.teleToLocation((int) x, (int) y, (int) z);
						
						// Now, get the last class and top level...
						player.setClassId(classId);
						player.setBaseClass(player.getActiveClass());
						player.broadcastUserInfo();
						
						long allExpToAdd = Experience.getAbsoluteExp(99) - player.getExp();
						
						player.addExpAndSp(allExpToAdd, 999999);
						player.giveAvailableSkills(true);
						player.store();
						
						player.setCurrentMp(player.getMaxMp());
						player.setCurrentHp(player.getMaxHp());
						player.setCurrentCp(player.getMaxCp());
						
						player.setTitle("Lalala...");
						player.broadcastTitleInfo();
					}
				}
			}
			else if (secondaryCommand.equals("LoginBot"))
			{
				if (!st.hasMoreTokens())
				{
					activeChar.sendMessage("Mention the name of the bot to login.");
					return false;
				}
				
				final String botName = st.nextToken();
				
				final L2PcInstance player = BotsManager.getInstance().loadPlayer(botName, BotMode.SPAWNED_BY_GM);
				
				player.getBotController().onEnterWorld(false);
				player.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ());
				
				activeChar.sendMessage("Logged in " + player.getName() + ".");
			}
			else if (secondaryCommand.equals("LogoutBot"))
			{
				if (!st.hasMoreTokens())
				{
					activeChar.sendMessage("Mention the name of the bot to logout.");
					return false;
				}
				
				final String botName = st.nextToken();
				
				final L2PcInstance player = L2World.getInstance().getPlayer(botName);
				
				if (player != null && player.isBot())
				{
					player.getBotController().onExitWorld();
				
					activeChar.sendMessage("Logged out " + player.getName() + ".");
				}
			}
			else if (secondaryCommand.equals("LoginBots"))
			{
				if (!st.hasMoreTokens())
				{
					activeChar.sendMessage("You must mentioned a range of bots to load. Ex: //do LoginBots 0 25");
					return false;
				}
				
				int firstId = -1;
				
				try
				{
					firstId = Integer.parseInt(st.nextToken());
				}
				catch (Exception e)
				{
					activeChar.sendMessage("You must mentioned a range of bots to load. Ex: //do LoginBots 0 25");
				}
				
				if (!st.hasMoreTokens())
				{
					activeChar.sendMessage("You must mentioned a range of bots to load. Ex: //do LoginBots 0 25");
					return false;
				}
				
				int secondId = -1;
				
				try
				{
					secondId = Integer.parseInt(st.nextToken());
				}
				catch (Exception e)
				{
					activeChar.sendMessage("You must mentioned a range of bots to load. Ex: //do LoginBots 0 25");
				}
				
				int loadedBots = 0;
				for (int i = firstId; i < secondId; i++ )
				{
					final L2PcInstance player = BotsManager.getInstance().loadPlayer("" + i, BotMode.SPAWNED_BY_GM);
					
					player.getBotController().onEnterWorld(false);
					
					// We spawn them in line, front of us...
					float headingAngle = (float) (activeChar.getHeading() * Math.PI) / Short.MAX_VALUE;
					
					float x = activeChar.getX() + (loadedBots * 50) * (float) Math.cos(headingAngle);
					float y = activeChar.getY() + (loadedBots * 50) * (float) Math.sin(headingAngle);
					float z = activeChar.getZ() + 1;
					
					player.teleToLocation((int) x, (int) y, (int) z);
					
					activeChar.sendMessage("Logged in Bot[" + player.getName() + "].");
					
					loadedBots++;
				}
				
				activeChar.sendMessage(loadedBots + " Bots have been successfully loaded.");
			}
			else if (secondaryCommand.equals("LogoutBots"))
			{
				final Map<Integer, L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers();
				
				for (L2PcInstance p : allPlayers.values())
				{
					if (p == null || !p.isBot())
						continue;

					@SuppressWarnings("unused")
					final BotController b = p.getBotController();
					
					boolean shouldLogout = true;// !(b instanceof YulController) && !(b instanceof SigelController) && !(b instanceof OthellController);
					
					if (!shouldLogout)
						continue;
					
					p.getBotController().onExitWorld();
					p.sendMessage("Logged out " + p.getName() + ".");
				}
			}
			else if (secondaryCommand.equals("StartController"))
			{
				final L2Object target = activeChar.getTarget();
				
				final L2PcInstance toonToControl;
				if (target != null)
				{
					if (!(target instanceof L2PcInstance))
					{
						activeChar.sendMessage("This target cannot be controlled by a bot.");
						return false;
					}
					
					toonToControl = (L2PcInstance) target;
				}
				else
					toonToControl = activeChar;

				toonToControl.setBotController(BotsManager.getInstance().initControllerFor(toonToControl));
				
				toonToControl.getBotController().onEnterWorld(true);
				
				toonToControl.getBotController().setMode(BotMode.SPAWNED_BY_GM);
				
				if (toonToControl == activeChar)
					activeChar.sendMessage("You are now controlled by an AI.");
				else
					activeChar.sendMessage(toonToControl.getName() + " is now controlled by an AI");
			}
			else if (secondaryCommand.equals("StopController"))
			{
				boolean forceSelf = false;
				
				if (st.hasMoreTokens())
					forceSelf = true;
				
				final L2PcInstance toonToControl;
				
				if (forceSelf)
					toonToControl = activeChar;
				else
				{
					final L2Object target = activeChar.getTarget();
					
					if (target != null)
					{
						if (!(target instanceof L2PcInstance))
						{
							activeChar.sendMessage("This target cannot be controlled by a bot.");
							return false;
						}
						
						toonToControl = (L2PcInstance) target;
					}
					else
						toonToControl = activeChar;
				}
				
				final BotController botController = toonToControl.getBotController();
				
				if (botController == null)
				{
					activeChar.sendMessage("There was no Bot Controller running.");
					return false;
				}
				
				toonToControl.getBotController().onExitWorld();
				toonToControl.setBotController(null);
				toonToControl.sendMessage("You are no longer controlled by an AI.");
				
				if (toonToControl == activeChar)
					activeChar.sendMessage("You are no longer controlled by an AI.");
				else
					activeChar.sendMessage(toonToControl.getName() + " is no longer controlled by an AI.");
			}
			else if (secondaryCommand.equals("PrintSkills"))
			{
				final L2Object target = activeChar.getTarget();
				
				L2PcInstance targetedPlayer = null;
				if (!(target instanceof L2PcInstance))
					targetedPlayer = activeChar;
				else
					targetedPlayer = (L2PcInstance) target;
				
				String filePath = "D:/Projects/Dreams Gaming/Lineage II/Goddess of Destruction+/Server Files/MoonLand/Tools/L2_DataTool/dist/data/client/skillname-e.txt";
				
				List<String> allLines = null;
				try
				{
					allLines = Files.readAllLines(Paths.get(filePath), StandardCharsets.ISO_8859_1);
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				String className = (String) BotsManager.getInstance().getClassDataById(targetedPlayer.getActiveClass())[1];
				
				Log.info("// " + className);
				Log.info("// ");
				Log.info("// ");
				Log.info("");
				for (L2Skill s : targetedPlayer.getAllSkills())
				{
					if (!s.isActive())
						continue;
					else if (s.getName().equals("Common Craft") || s.getName().startsWith("Mentor"))
						continue;
					
					final String varName = s.getName().toUpperCase().replace(" ", "_").replace("'", "");

					for (String s2 : allLines)
					{
						final String[] split = s2.split("\t");
						final int skillId = Integer.parseInt(split[0]);
						
						if (skillId != s.getId())
							continue;
						
						final int skillLevel = Integer.parseInt(split[1]);
						
						if (skillLevel != s.getLevel())
							continue;
						
						@SuppressWarnings("unused")
						final String skillName = split[2];
						final String skillDesc = split[3];
						
						Log.info("// " + skillDesc);
						Log.info("// Cooldown: " + (s.getReuseDelay() / 1000) + "s");
					}
					Log.info("private static final int " + varName + " = " + s.getId() + ";");
					Log.info("");
				}
			}
		}
		return true;
	}
	
	/**
	 * @param activeChar
	 * @param id
	 */
	private void adminTestSkill(L2PcInstance activeChar, int id, boolean msu)
	{
		L2Character caster;
		L2Object target = activeChar.getTarget();
		if (!(target instanceof L2Character))
			caster = activeChar;
		else
			caster = (L2Character) target;

		L2Skill _skill = SkillTable.getInstance().getInfo(id, 1);
		if (_skill != null)
		{
			caster.setTarget(activeChar);
			if (msu)
				caster.broadcastPacket(new MagicSkillUse(caster, activeChar, id, 1, _skill.getHitTime(), _skill.getReuseDelay(), _skill.getReuseHashCode(), 0));
			else
				caster.doCast(_skill);
		}
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
	 */
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
