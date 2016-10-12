package l2server.gameserver.model.actor.instance;

import l2server.Config;
import l2server.gameserver.Announcements;
import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.datatables.*;
import l2server.gameserver.events.instanced.EventsManager;
import l2server.gameserver.instancemanager.*;
import l2server.gameserver.model.*;
import l2server.gameserver.model.L2Clan.SubPledge;
import l2server.gameserver.model.base.Experience;
import l2server.gameserver.model.base.PlayerClass;
import l2server.gameserver.model.base.Race;
import l2server.gameserver.model.base.SubClass;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.entity.Fort;
import l2server.gameserver.model.entity.Siege;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.network.serverpackets.SortedWareHouseWithdrawalList.WarehouseListType;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.item.L2Henna;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.StringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Pere
 */
public final class L2ServicesNpcInstance extends L2NpcInstance
{
	/**
	 * @param template
	 */
	public L2ServicesNpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean isWarehouse()
	{
		return true;
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		// lil check to prevent enchant exploit
		if (player.getActiveEnchantItem() != null)
		{
			//Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " trying to use enchant exploit, ban this player!", IllegalPlayerAction.PUNISH_KICK);
			return;
		}

		String param[] = command.split("_");
		String[] commandStr = command.split(" ");
		String actualCommand = commandStr[0]; // Get actual command

		String cmdParams = "";
		String cmdParams2 = "";

		if (commandStr.length >= 2)
		{
			cmdParams = commandStr[1];
		}
		if (commandStr.length >= 3)
		{
			cmdParams2 = commandStr[2];
		}

		if (command.startsWith("1stClass"))
		{
			showHtmlMenu(player, getObjectId(), 1);
		}
		else if (command.startsWith("2ndClass"))
		{
			showHtmlMenu(player, getObjectId(), 2);
		}
		else if (command.startsWith("3rdClass"))
		{
			showHtmlMenu(player, getObjectId(), 3);
		}
		else if (command.startsWith("change_class"))
		{
			int val = Integer.parseInt(command.substring(13));

			if (checkAndChangeClass(player, val))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player.getHtmlPrefix(), "classmaster/ok.htm");
				html.replace("%name%", PlayerClassTable.getInstance().getClassNameById(val));
				player.sendPacket(html);
			}
		}
		else if (command.startsWith("classmaster"))
		{
			if (Config.DEBUG)
			{
				Log.fine("ClassMaster activated");
			}

			PlayerClass cl = player.getCurrentClass();

			int lvl = PlayerClassTable.getInstance().getClassById(cl.getId()).getLevel();
			switch (lvl)
			{
				case 1:
					showHtmlMenu(player, getObjectId(), 1);
					break;
				case 20:
					showHtmlMenu(player, getObjectId(), 2);
					break;
				case 40:
					showHtmlMenu(player, getObjectId(), 3);
					break;
				default:
					showHtmlMenu(player, getObjectId(), 4);
			}
		}
		else if (command.equals("Draw"))
		{
			HennaEquipList hel = new HennaEquipList(player, player.getCurrentClass().getAllowedDyes());
			player.sendPacket(hel);
		}
		else if (command.equals("RemoveList"))
		{
			showRemoveChat(player);
		}
		else if (command.startsWith("Remove "))
		{
			int slot = Integer.parseInt(command.substring(7));
			player.removeHenna(slot);
		}
		else if (actualCommand.equalsIgnoreCase("create_clan"))
		{
			if (cmdParams.isEmpty())
			{
				return;
			}

			ClanTable.getInstance().createClan(player, cmdParams);
		}
		else if (actualCommand.equalsIgnoreCase("create_academy"))
		{
			if (cmdParams.isEmpty())
			{
				return;
			}

			createSubPledge(player, cmdParams, null, L2Clan.SUBUNIT_ACADEMY, 5);
		}
		else if (actualCommand.equalsIgnoreCase("rename_pledge"))
		{
			if (cmdParams.isEmpty() || cmdParams2.isEmpty())
			{
				return;
			}

			renameSubPledge(player, Integer.valueOf(cmdParams), cmdParams2);
		}
		else if (actualCommand.equalsIgnoreCase("create_royal"))
		{
			if (cmdParams.isEmpty())
			{
				return;
			}

			createSubPledge(player, cmdParams, cmdParams2, L2Clan.SUBUNIT_ROYAL1, 6);
		}
		else if (actualCommand.equalsIgnoreCase("create_knight"))
		{
			if (cmdParams.isEmpty())
			{
				return;
			}

			createSubPledge(player, cmdParams, cmdParams2, L2Clan.SUBUNIT_KNIGHT1, 7);
		}
		else if (actualCommand.equalsIgnoreCase("assign_subpl_leader"))
		{
			if (cmdParams.isEmpty())
			{
				return;
			}

			assignSubPledgeLeader(player, cmdParams, cmdParams2);
		}
		else if (actualCommand.equalsIgnoreCase("create_ally"))
		{
			if (cmdParams.isEmpty())
			{
				return;
			}

			if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_CLAN_LEADER_CREATE_ALLIANCE));
				return;
			}
			player.getClan().createAlly(player, cmdParams);
		}
		else if (actualCommand.equalsIgnoreCase("dissolve_ally"))
		{
			if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER));
				return;
			}
			player.getClan().dissolveAlly(player);
		}
		else if (actualCommand.equalsIgnoreCase("dissolve_clan"))
		{
			dissolveClan(player, player.getClanId());
		}
		else if (actualCommand.equalsIgnoreCase("change_clan_leader"))
		{
			if (cmdParams.isEmpty())
			{
				return;
			}

			changeClanLeader(player, cmdParams);
		}
		else if (actualCommand.equalsIgnoreCase("recover_clan"))
		{
			recoverClan(player, player.getClanId());
		}
		else if (actualCommand.equalsIgnoreCase("increase_clan_level"))
		{
			if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
				return;
			}
			player.getClan().levelUpClan(player);
		}
		else if (actualCommand.equalsIgnoreCase("learn_clan_skills"))
		{
			showPledgeSkillList(player);
		}
		else if (command.startsWith("Subclass"))
		{
			if (EventsManager.getInstance().isPlayerParticipant(player.getObjectId()))
			{
				player.sendMessage("You can't change sub classes if you are joined in an event.");
				return;
			}

			int cmdChoice = Integer.parseInt(command.substring(9, 10).trim());

			// Subclasses may not be changed while a skill is in use.
			if (player.isCastingNow() || player.isAllSkillsDisabled())
			{
				player.sendPacket(SystemMessage
						.getSystemMessage(SystemMessageId.SUBCLASS_NO_CHANGE_OR_CREATE_WHILE_SKILL_IN_USE));
				return;
			}

			String content = "<html><body>";
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());

			int paramOne = 0;
			int paramTwo = 0;

			try
			{
				int endIndex = command.indexOf(' ', 11);
				if (endIndex == -1)
				{
					endIndex = command.length();
				}

				paramOne = Integer.parseInt(command.substring(11, endIndex).trim());
				if (command.length() > endIndex)
				{
					paramTwo = Integer.parseInt(command.substring(endIndex).trim());
				}
			}
			catch (Exception ignored)
			{
			}

			int maxSubs = Config.MAX_SUBCLASS;
			if (player.getRace() == Race.Ertheia)
			{
				maxSubs = 1;
			}
			switch (cmdChoice)
			{
				case 0: // Subclass change menu
					html.setFile(player.getHtmlPrefix(), "villagemaster/SubClass.htm");
					break;
				case 1: // Add Subclass - Initial
					// Avoid giving player an option to add a new sub class, if they have three already.
					if (player.getTotalSubClasses() >= maxSubs)
					{
						html.setFile(player.getHtmlPrefix(), "villagemaster/SubClass_Fail.htm");
						break;
					}

					html.setFile(player.getHtmlPrefix(), "villagemaster/SubClass_Add.htm");
					final StringBuilder content1 = StringUtil.startAppend(200);
					List<Integer> subsAvailable = getAvailableSubClasses(player);

					if (subsAvailable != null && !subsAvailable.isEmpty())
					{
						for (int subClassId : subsAvailable)
						{
							PlayerClass subClass = PlayerClassTable.getInstance().getClassById(subClassId);
							StringUtil.append(content1, "<a action=\"bypass -h npc_%objectId%_Subclass 4 ",
									String.valueOf(subClass.getId()), "\" msg=\"1268;", formatClassForDisplay(subClass),
									"\">", formatClassForDisplay(subClass), "</a><br>");
						}
					}
					else
					{
						// TODO: Retail message
						player.sendMessage("There are no sub classes available at this time.");
						return;
					}
					html.replace("%list%", content1.toString());
					break;
				case 2: // Change Class - Initial
					content += "Change Subclass:<br>";

					final int baseClassId = player.getBaseClass();

					if (player.getSubClasses().isEmpty())
					{
						content += "You can't change sub classes when you don't have a sub class to begin with.<br>";
					}
					else
					{
						content += "Which class would you like to switch to?<br>";

						if (baseClassId == player.getActiveClass())
						{
							content += PlayerClassTable.getInstance().getClassNameById(baseClassId) +
									"&nbsp;<font color=\"LEVEL\">(Base Class)</font><br><br>";
						}
						else
						{
							content += "<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 5 0\">" +
									PlayerClassTable.getInstance().getClassNameById(baseClassId) + "</a>&nbsp;" +
									"<font color=\"LEVEL\">(Base Class)</font><br><br>";
						}

						for (Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext(); )
						{
							SubClass subClass = subList.next();
							int subClassId = subClass.getClassId();

							if (subClassId == player.getActiveClass())
							{
								content += PlayerClassTable.getInstance().getClassNameById(subClassId) + "<br>";
							}
							else
							{
								content += "<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 5 " +
										subClass.getClassIndex() + "\">" +
										PlayerClassTable.getInstance().getClassNameById(subClassId) + "</a><br>";
							}
						}
					}
					break;
				case 3: // Change/Cancel Subclass - Initial
					if (player.getSubClasses() == null || player.getSubClasses().isEmpty())
					{
						html.setFile(player.getHtmlPrefix(), "villagemaster/SubClass_ModifyEmpty.htm");
						break;
					}

					if (player.getTemporaryLevel() != 0)
					{
						player.sendMessage("You are not allowed to do that while on a temporary level.");
						break;
					}

					// custom value
					if (player.getTotalSubClasses() > 3)
					{
						html.setFile(player.getHtmlPrefix(), "villagemaster/SubClass_ModifyCustom.htm");
						final StringBuilder content3 = StringUtil.startAppend(200);
						int classIndex = 1;

						for (Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext(); )
						{
							SubClass subClass = subList.next();

							StringUtil.append(content3, "Sub-class ", String.valueOf(classIndex++), "<br>",
									"<a action=\"bypass -h npc_%objectId%_Subclass 6 ",
									String.valueOf(subClass.getClassIndex()), "\">",
									PlayerClassTable.getInstance().getClassNameById(subClass.getClassId()), "</a><br>");
						}
						html.replace("%list%", content3.toString());
					}
					else
					{
						// retail html contain only 3 subclasses
						html.setFile(player.getHtmlPrefix(), "villagemaster/SubClass_Modify.htm");
						if (player.getSubClasses().containsKey(1))
						{
							html.replace("%sub1%", PlayerClassTable.getInstance()
									.getClassNameById(player.getSubClasses().get(1).getClassId()));
						}
						else
						{
							html.replace(
									"Sub-class 1<br>\n<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h npc_%objectId%_Subclass 6 1\">%sub1%</Button>",
									"");
						}

						if (player.getSubClasses().containsKey(2))
						{
							html.replace("%sub2%", PlayerClassTable.getInstance()
									.getClassNameById(player.getSubClasses().get(2).getClassId()));
						}
						else
						{
							html.replace(
									"Sub-class 2<br>\n<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h npc_%objectId%_Subclass 6 2\">%sub2%</Button>",
									"");
						}

						if (player.getSubClasses().containsKey(3))
						{
							html.replace("%sub3%", PlayerClassTable.getInstance()
									.getClassNameById(player.getSubClasses().get(3).getClassId()));
						}
						else
						{
							html.replace(
									"Sub-class 3<br>\n<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h npc_%objectId%_Subclass 6 3\">%sub3%</Button>",
									"");
						}
					}
					break;
				case 4: // Add Subclass - Action (Subclass 4 x[x])
					/*
                     * If the character is less than level 75 on any of their previously chosen
					 * classes then disallow them to change to their most recently added sub-class choice.
					 */

					if (!player.getFloodProtectors().getSubclass().tryPerformAction("add subclass"))
					{
						Log.warning("Player " + player.getName() + " has performed a subclass change too fast");
						return;
					}

					if (player.getTemporaryLevel() != 0)
					{
						player.sendMessage("You are not allowed to do that while on a temporary level.");
						return;
					}

					boolean allowAddition = true;

					if (player.getTotalSubClasses() >= maxSubs)
					{
						allowAddition = false;
					}

					if (player.getLevel() < 75 && !(player.getRace() != Race.Ertheia || player.getLevel() >= 85))
					{
						allowAddition = false;
					}

					if (allowAddition)
					{
						if (!player.getSubClasses().isEmpty())
						{
							for (Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext(); )
							{
								SubClass subClass = subList.next();

								if (subClass.getLevel() < 75)
								{
									allowAddition = false;
									break;
								}
							}
						}
					}

					if (allowAddition && isValidNewSubClass(player, paramOne))
					{
						if (!player.addSubClass(paramOne, player.getTotalSubClasses() + 1, 0))
						{
							return;
						}

						player.setActiveClass(player.getTotalSubClasses());

						if (Config.isServer(Config.TENKAI_ESTHUS))
						{
							player.giveAvailableSkills(true);
							player.sendSkillList();
						}

						html.setFile(player.getHtmlPrefix(), "villagemaster/SubClass_AddOk.htm");

						player.sendPacket(
								SystemMessage.getSystemMessage(SystemMessageId.ADD_NEW_SUBCLASS)); // Subclass added.
						player.sendPacket(new ExSubjobInfo(player));
					}
					else
					{
						html.setFile(player.getHtmlPrefix(), "villagemaster/SubClass_Fail.htm");
					}
					break;
				case 5: // Change Class - Action
                    /*
					 * If the character is less than level 75 on any of their previously chosen
					 * classes then disallow them to change to their most recently added sub-class choice.
					 *
					 * Note: paramOne = classIndex
					 */

					if (!player.getFloodProtectors().getSubclass().tryPerformAction("add subclass"))
					{
						Log.warning("Player " + player.getName() + " has performed a subclass change too fast");
						return;
					}

					if (player.hasIdentityCrisis()) // Cannot switch or change subclasses while identity crisis during
					{
						content += "You cannot switch your subclass while Identity crisis are in progress.<br>";
						break;
					}

					if (player.getTemporaryLevel() != 0)
					{
						player.sendMessage("You are not allowed to do that while on a temporary level.");
						return;
					}

					player.setActiveClass(paramOne);

					content += "Change Subclass:<br>Your active sub class is now a <font color=\"LEVEL\">" +
							PlayerClassTable.getInstance().getClassNameById(player.getActiveClass()) + "</font>.";

					player.sendPacket(SystemMessage
							.getSystemMessage(SystemMessageId.SUBCLASS_TRANSFER_COMPLETED)); // Transfer completed.
					player.sendPacket(new ExSubjobInfo(player));
					break;
				case 6: // Change/Cancel Subclass - Choice
					// validity check
					if (paramOne < 1 || paramOne > maxSubs)
					{
						return;
					}

					if (player.getTemporaryLevel() != 0)
					{
						player.sendMessage("You are not allowed to do that while on a temporary level.");
						return;
					}

					subsAvailable = getAvailableSubClasses(player);

					// another validity check
					if (subsAvailable == null || subsAvailable.isEmpty())
					{
						// TODO: Retail message
						player.sendMessage("There are no sub classes available at this time.");
						return;
					}

					final StringBuilder content6 = StringUtil.startAppend(200);

					for (int subClassId : subsAvailable)
					{
						PlayerClass subClass = PlayerClassTable.getInstance().getClassById(subClassId);
						StringUtil.append(content6, "<a action=\"bypass -h npc_%objectId%_Subclass 7 ",
								String.valueOf(paramOne), " ", String.valueOf(subClass.getId()), "\" msg=\"1445;",
								"\">", formatClassForDisplay(subClass), "</a><br>");
					}

					switch (paramOne)
					{
						case 1:
							html.setFile(player.getHtmlPrefix(), "villagemaster/SubClass_ModifyChoice1.htm");
							break;
						case 2:
							html.setFile(player.getHtmlPrefix(), "villagemaster/SubClass_ModifyChoice2.htm");
							break;
						case 3:
							html.setFile(player.getHtmlPrefix(), "villagemaster/SubClass_ModifyChoice3.htm");
							break;
						default:
							html.setFile(player.getHtmlPrefix(), "villagemaster/SubClass_ModifyChoice.htm");
					}
					html.replace("%list%", content6.toString());
					break;
				case 7: // Change Subclass - Action
					/*
					 * Warning: the information about this subclass will be removed from the
					 * subclass list even if false!
					 */

					if (!player.getFloodProtectors().getSubclass().tryPerformAction("add subclass"))
					{
						Log.warning("Player " + player.getName() + " has performed a subclass change too fast");
						return;
					}

					if (player.hasIdentityCrisis()) // Cannot switch or change subclasses while identity crisis during
					{
						content += "You cannot switch your subclass while Identity crisis are in progress.<br>";
						break;
					}

					if (player.getTemporaryLevel() != 0)
					{
						player.sendMessage("You are not allowed to do that while on a temporary level.");
						return;
					}

					if (player.modifySubClass(paramOne, paramTwo))
					{
						player.stopAllEffects(); // all effects from old subclass stopped!
						player.setActiveClass(paramOne);

						content += "Change Subclass:<br>Your sub class has been changed to <font color=\"LEVEL\">" +
								PlayerClassTable.getInstance().getClassNameById(paramTwo) + "</font>.";

						if (Config.isServer(Config.TENKAI_ESTHUS))
						{
							player.giveAvailableSkills(true);
							player.sendSkillList();
						}

						player.sendPacket(
								SystemMessage.getSystemMessage(SystemMessageId.ADD_NEW_SUBCLASS)); // Subclass added.
						player.sendPacket(new ExSubjobInfo(player));
					}
					else
					{
						/*
						 * This isn't good! modifySubClass() removed subclass from memory
						 * we must update _classIndex! Else IndexOutOfBoundsException can turn
						 * up some place down the line along with other seemingly unrelated
						 * problems.
						 */
						player.setActiveClass(0); // Also updates _classIndex plus switching _classid to baseclass.

						player.sendMessage(
								"The sub class could not be added, you have been reverted to your base class.");
						return;
					}
					break;
				case 8: // Make Dual Class - Initial
					if (player.getSubClasses() == null || player.getSubClasses().isEmpty())
					{
						player.sendMessage("You don't have any subclass!");
						return;
					}

					if (player.getTemporaryLevel() != 0)
					{
						player.sendMessage("You are not allowed to do that while on a temporary level.");
						return;
					}

					// retail html contain only 3 subclasses
					html.setFile(player.getHtmlPrefix(), "villagemaster/SubClass_MakeDual.htm");
					if (player.getSubClasses().containsKey(1))
					{
						html.replace("%sub1%", PlayerClassTable.getInstance()
								.getClassNameById(player.getSubClasses().get(1).getClassId()));
					}
					else
					{
						html.replace("<a action=\"bypass -h npc_%objectId%_Subclass 9 1\">%sub1%</a><br>", "");
					}

					if (player.getSubClasses().containsKey(2))
					{
						html.replace("%sub2%", PlayerClassTable.getInstance()
								.getClassNameById(player.getSubClasses().get(2).getClassId()));
					}
					else
					{
						html.replace("<a action=\"bypass -h npc_%objectId%_Subclass 9 2\">%sub2%</a><br>", "");
					}

					if (player.getSubClasses().containsKey(3))
					{
						html.replace("%sub3%", PlayerClassTable.getInstance()
								.getClassNameById(player.getSubClasses().get(3).getClassId()));
					}
					else
					{
						html.replace("<a action=\"bypass -h npc_%objectId%_Subclass 9 3\">%sub3%</a><br>", "");
					}
					break;
				case 9: // Make Dual Class - Action
					if (paramOne < 1 || paramOne > maxSubs)
					{
						return;
					}

					SubClass subClass = player.getSubClasses().get(paramOne);
					if (subClass == null)
					{
						return;
					}

					if (subClass.getClassDefinition().getId() == 136)
					{
						player.sendMessage("You cannot make Judicator be your dual class!");
						return;
					}

					if (subClass.getLevel() < 80)
					{
						player.sendMessage("This subclass is not at level 80!");
						return;
					}

					if (player.getTemporaryLevel() != 0)
					{
						player.sendMessage("You are not allowed to do that while on a temporary level.");
						return;
					}

					boolean hasDual = false;
					for (SubClass sub : player.getSubClasses().values())
					{
						if (sub.isDual())
						{
							hasDual = true;
							break;
						}
					}

					if (hasDual)
					{
						player.sendMessage("You already have a dual class!");
						return;
					}

					subClass.setIsDual(true);
					if (Config.STARTING_LEVEL > subClass.getLevel())
					{
						byte level = Config.STARTING_LEVEL;
						if (level > subClass.getMaxLevel())
						{
							level = subClass.getMaxLevel();
						}

						subClass.setLevel(level);
						subClass.setExp(Experience.getAbsoluteExp(level));
						player.broadcastUserInfo();
					}

					player.sendPacket(new ExSubjobInfo(player));

					content += "Make Dual Class:<br>Your subclass is now a <font color=\"LEVEL\">dual class</font>.";

					player.sendMessage("Dual class created!"); // Subclass added.
					break;
			}

			content += "</body></html>";

			// If the content is greater than for a basic blank page,
			// then assume no external HTML file was assigned.
			if (content.length() > 26)
			{
				html.setHtml(content);
			}

			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		else if (command.startsWith("WithdrawP"))
		{
			if (Config.L2JMOD_ENABLE_WAREHOUSESORTING_PRIVATE)
			{
				String htmFile = "mods/WhSortedP.htm";
				String htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmFile);
				if (htmContent != null)
				{
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(npcHtmlMessage);
				}
				else
				{
					Log.warning("Missing htm: " + htmFile + " !");
				}
			}
			else
			{
				showRetrieveWindow(player);
			}
		}
		else if (command.startsWith("WithdrawSortedP"))
		{
			if (param.length > 2)
			{
				showRetrieveWindow(player, WarehouseListType.valueOf(param[1]),
						SortedWareHouseWithdrawalList.getOrder(param[2]));
			}
			else if (param.length > 1)
			{
				showRetrieveWindow(player, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.A2Z);
			}
			else
			{
				showRetrieveWindow(player, WarehouseListType.ALL, SortedWareHouseWithdrawalList.A2Z);
			}
		}
		else if (command.equals("DepositP"))
		{
			showDepositWindow(player);
		}
		else if (command.startsWith("WithdrawC"))
		{
			if (Config.L2JMOD_ENABLE_WAREHOUSESORTING_CLAN)
			{
				String htmFile = "mods/WhSortedC.htm";
				String htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmFile);
				if (htmContent != null)
				{
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(npcHtmlMessage);
				}
				else
				{
					Log.warning("Missing htm: " + htmFile + " !");
				}
			}
			else
			{
				showWithdrawWindowClan(player);
			}
		}
		else if (command.startsWith("WithdrawSortedC"))
		{
			if (param.length > 2)
			{
				showWithdrawWindowClan(player, WarehouseListType.valueOf(param[1]),
						SortedWareHouseWithdrawalList.getOrder(param[2]));
			}
			else if (param.length > 1)
			{
				showWithdrawWindowClan(player, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.A2Z);
			}
			else
			{
				showWithdrawWindowClan(player, WarehouseListType.ALL, SortedWareHouseWithdrawalList.A2Z);
			}
		}
		else if (command.equals("DepositC"))
		{
			showDepositWindowClan(player);
		}
		else if (command.startsWith("FishSkillList"))
		{
			showSkillList(player);
		}
		else if (command.startsWith("AbandonCastle"))
		{
			L2Clan clan = player.getClan();
			if (clan == null)
			{
				player.sendMessage("You don't have a clan!");
				return;
			}

			if (!player.isClanLeader())
			{
				player.sendMessage("You are not the clan leader from your clan!");
				return;
			}

			Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
			if (castle == null)
			{
				player.sendMessage("Your clan doesn't have a castle!");
				return;
			}

			Siege siege = CastleManager.getInstance().getCastleByOwner(clan).getSiege();
			if (siege != null && siege.getIsInProgress())
			{
				player.sendMessage("This function can't be used while in siege!");
				return;
			}

			castle.removeOwner(clan);
			Announcements.getInstance()
					.announceToAll(clan.getName() + " has abandoned " + castle.getName() + " castle!");
			player.sendMessage("The castle has been abandoned!");
		}
		else if (command.startsWith("ChangeCastleTendency"))
		{
			L2Clan clan = player.getClan();
			if (clan == null)
			{
				player.sendMessage("You don't have a clan!");
				return;
			}

			if (!player.isClanLeader())
			{
				player.sendMessage("You are not the clan leader from your clan!");
				return;
			}

			Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
			if (castle == null)
			{
				player.sendMessage("Your clan doesn't have a castle!");
				return;
			}

			Siege siege = CastleManager.getInstance().getCastleByOwner(clan).getSiege();
			if (siege != null && siege.getIsInProgress())
			{
				player.sendMessage("This function can't be used while in siege!");
				return;
			}

			String varFormat = castle.getName() + "_tendency";
			String value = GlobalVariablesManager.getInstance().getStoredVariable(varFormat);

			long _currTime = System.currentTimeMillis();
			long _reuseTime = value == null ? 0 : Long.parseLong(value);
			if (_currTime > _reuseTime)
			{
				if (castle.getTendency() == Castle.TENDENCY_LIGHT)
				{
					castle.setTendency(Castle.TENDENCY_DARKNESS);
				}
				else
				{
					castle.setTendency(Castle.TENDENCY_LIGHT);
				}

				clan.checkTendency();
				player.sendMessage("The castle tendency has been changed!");

				GlobalVariablesManager.getInstance()
						.storeVariable(varFormat, Long.toString(604800000 + System.currentTimeMillis()));
			}
			else
			{
				player.sendMessage("This function can be used only one time per week!");
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	private static void showHtmlMenu(L2PcInstance player, int objectId, int level)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(objectId);
		if (player.getRace() == Race.Ertheia && level == 1)
		{
			level = 2;
		}

		final PlayerClass currentClass = player.getCurrentClass();
		if (currentClass.level() >= level)
		{
			html.setFile(player.getHtmlPrefix(), "classmaster/nomore.htm");
		}
		else
		{
			final int minLevel = getMinLevel(currentClass.level());
			if (player.getLevel() >= minLevel || Config.ALLOW_ENTIRE_TREE)
			{
				final StringBuilder menu = new StringBuilder(100);
				for (PlayerClass cid : PlayerClassTable.getInstance().getAllClasses())
				{
					if (cid.getRace() == null)
					{
						continue;
					}
					else if (cid.getRace() == Race.Ertheia && player.getLevel() < 40)
					{
						continue;
					}

					if (validateClass(currentClass, cid) && cid.level() == level)
					{
						if (cid.getId() != 135) // 135 = Inspector (male + female) - prohibiting Judicator as main class
						{
							StringUtil.append(menu, "<a action=\"bypass -h npc_%objectId%_change_class ",
									String.valueOf(cid.getId()), "\">",
									PlayerClassTable.getInstance().getClassNameById(cid.getId()), "</a><br>");
						}
					}
				}

				if (menu.length() > 0)
				{
					html.setFile(player.getHtmlPrefix(), "classmaster/template.htm");
					html.replace("%name%", PlayerClassTable.getInstance().getClassNameById(currentClass.getId()));
					html.replace("%menu%", menu.toString());
				}
				else
				{
					html.setFile(player.getHtmlPrefix(), "classmaster/comebacklater.htm");
					html.replace("%level%", String.valueOf(getMinLevel(level - 1)));
				}
			}
			else
			{
				if (minLevel < Integer.MAX_VALUE)
				{
					html.setFile(player.getHtmlPrefix(), "classmaster/comebacklater.htm");
					html.replace("%level%", String.valueOf(minLevel));
				}
				else
				{
					html.setFile(player.getHtmlPrefix(), "classmaster/nomore.htm");
				}
			}
		}

		html.replace("%objectId%", String.valueOf(objectId));
		player.sendPacket(html);
	}

	private static boolean checkAndChangeClass(L2PcInstance player, int val)
	{
		final PlayerClass currentClassId = player.getCurrentClass();
		if (getMinLevel(currentClassId.level()) > player.getLevel() && !Config.ALLOW_ENTIRE_TREE)
		{
			return false;
		}

		if (!validateClass(currentClassId, val))
		{
			return false;
		}

		player.setClassId(val);

		if (player.isSubClassActive())
		{
			player.getSubClasses().get(player.getClassIndex()).setClassId(player.getActiveClass());
		}
		else
		{
			player.setBaseClass(player.getActiveClass());
			player.addRaceSkills();
		}
		player.broadcastUserInfo();

		return true;
	}

	/**
	 * Returns minimum player level required for next class transfer
	 *
	 * @param level - current skillId level (0 - start, 1 - first, etc)
	 */
	private static int getMinLevel(int level)
	{
		switch (level)
		{
			case 0:
				return 20;
			case 1:
				return 40;
			case 2:
				return 76;
			case 3:
				return 85;
			default:
				return Integer.MAX_VALUE;
		}
	}

	/**
	 * Returns true if class change is possible
	 *
	 * @param oldC current player ClassId
	 * @param val  new class index
	 * @return
	 */
	private static boolean validateClass(PlayerClass oldC, int val)
	{
		try
		{
			return validateClass(oldC, PlayerClassTable.getInstance().getClassById(val));
		}
		catch (Exception e)
		{
			// possible ArrayOutOfBoundsException
		}
		return false;
	}

	/**
	 * Returns true if class change is possible
	 *
	 * @param oldC current player ClassId
	 * @param newC new ClassId
	 * @return true if class change is possible
	 */
	private static boolean validateClass(PlayerClass oldC, PlayerClass newC)
	{
		if (newC == null)
		{
			return false;
		}

		if (oldC.equals(newC.getParent()))
		{
			return true;
		}

		return Config.ALLOW_ENTIRE_TREE && newC.childOf(oldC);

	}

	private Iterator<SubClass> iterSubClasses(L2PcInstance player)
	{
		return player.getSubClasses().values().iterator();
	}/*
		* Returns list of available subclasses
		* Base class and already used subclasses removed
		*/

	private List<Integer> getAvailableSubClasses(L2PcInstance player)
	{
		// get player base class
		final int currentBaseId = player.getBaseClass();
		final PlayerClass baseCID = PlayerClassTable.getInstance().getClassById(currentBaseId);

		// we need 2nd occupation ID
		final int baseClassId;
		if (baseCID.level() > 2)
		{
			baseClassId = baseCID.getParent().getId();
		}
		else
		{
			baseClassId = currentBaseId;
		}

        /*
          If the race of your main class is Elf or Dark Elf,
          you may not select each class as a subclass to the other class.

          If the race of your main class is Kamael, you may not subclass any other race
          If the race of your main class is NOT Kamael, you may not subclass any Kamael class

          You may not select Overlord and Warsmith class as a subclass.

          You may not select a similar class as the subclass.
          The occupations classified as similar classes are as follows:

          Treasure Hunter, Plainswalker and Abyss Walker
          Hawkeye, Silver Ranger and Phantom Ranger
          Paladin, Dark Avenger, Temple Knight and Shillien Knight
          Warlocks, Elemental Summoner and Phantom Summoner
          Elder and Shillien Elder
          Swordsinger and Bladedancer
          Sorcerer, Spellsinger and Spellhowler

          Also, Kamael have a special, hidden 4 subclass, the inspector, which can
          only be taken if you have already completed the other two Kamael subclasses

         */
		List<Integer> availSubs = PlayerClassTable.getInstance().getAvailableSubclasses(player, baseClassId);

		if (availSubs != null && !availSubs.isEmpty())
		{
			List<Integer> toIterate = new ArrayList<>(availSubs);
			for (Integer subId : toIterate)
			{
				PlayerClass pclass = PlayerClassTable.getInstance().getClassById(subId);

				// scan for already used subclasses
				int availClassId = pclass.getId();
				PlayerClass cid = PlayerClassTable.getInstance().getClassById(availClassId);
				for (Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext(); )
				{
					SubClass prevSubClass = subList.next();
					PlayerClass subClassId = PlayerClassTable.getInstance().getClassById(prevSubClass.getClassId());

					if (subClassId.equalsOrChildOf(cid))
					{
						availSubs.remove(subId);
						break;
					}
				}
			}
		}

		return availSubs;
	}

	/*
	 * Check new subclass classId for validity
	 * (villagemaster race/type, is not contains in previous subclasses,
	 * is contains in allowed subclasses)
	 * Base class not added into allowed subclasses.
	 */
	private boolean isValidNewSubClass(L2PcInstance player, int classId)
	{
		final PlayerClass cid = PlayerClassTable.getInstance().getClassById(classId);
		for (Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext(); )
		{
			SubClass sub = subList.next();
			PlayerClass subClass = PlayerClassTable.getInstance().getClassById(sub.getClassId());

			if (subClass.equalsOrChildOf(cid))
			{
				return false;
			}
		}

		// get player base class
		final int currentBaseId = player.getBaseClass();
		final PlayerClass baseCID = PlayerClassTable.getInstance().getClassById(currentBaseId);

		// we need 2nd occupation ID
		final int baseClassId;
		if (baseCID.level() > 2)
		{
			baseClassId = baseCID.getParent().getId();
		}
		else
		{
			baseClassId = currentBaseId;
		}

		List<Integer> availSubs = PlayerClassTable.getInstance().getAvailableSubclasses(player, baseClassId);
		if (availSubs == null || availSubs.isEmpty())
		{
			return false;
		}

		boolean found = false;
		for (int subId : availSubs)
		{
			PlayerClass pclass = PlayerClassTable.getInstance().getClassById(subId);
			if (pclass.getId() == classId)
			{
				found = true;
				break;
			}
		}

		return found;
	}

	private static String formatClassForDisplay(PlayerClass className)
	{
		String classNameStr = className.getName();
		char[] charArray = classNameStr.toCharArray();

		for (int i = 1; i < charArray.length; i++)
		{
			if (Character.isUpperCase(charArray[i]))
			{
				classNameStr = classNameStr.substring(0, i) + " " + classNameStr.substring(i);
			}
		}

		return classNameStr;
	}

	private void showRemoveChat(L2PcInstance player)
	{
		String html1 = "<html><body>" + "Select symbol you would like to remove:<br><br>";
		boolean hasHennas = false;

		for (int i = 1; i <= 4; i++)
		{
			L2Henna henna = player.getHenna(i);
			if (henna != null)
			{
				hasHennas = true;
				html1 += "<a action=\"bypass -h npc_%objectId%_Remove " + i + "\">" + henna.getName() + "</a><br>";
			}
		}
		if (!hasHennas)
		{
			html1 += "You don't have any symbol to remove!";
		}
		html1 += "</body></html>";
		insertObjectIdAndShowChatWindow(player, html1);
	}

	public void showPledgeSkillList(L2PcInstance player)
	{
		if (Config.DEBUG)
		{
			Log.fine("PledgeSkillList activated on: " + getObjectId());
		}
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		if (player.getClan() == null || !player.isClanLeader())
		{
			String s = "<html><body>" + "<br><br>You're not qualified to learn Clan skills." + "</body></html>";
			html.setHtml(s);
			player.sendPacket(html);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2PledgeSkillLearn[] skills = PledgeSkillTree.getInstance().getAvailableSkills(player);
		ExAcquireSkillList asl = new ExAcquireSkillList(ExAcquireSkillList.SkillType.Clan);
		int counts = 0;

		for (L2PledgeSkillLearn s : skills)
		{
			int cost = s.getRepCost();
			counts++;

			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
		}

		if (counts == 0)
		{

			if (player.getClan().getLevel() < 8)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
				player.sendPacket(sm);
			}
			else
			{
				String s = "<html><body>" + "You've learned all skills available for your Clan.<br>" + "</body></html>";
				html.setHtml(s);
				player.sendPacket(html);
			}
		}
		else
		{
			player.sendPacket(asl);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	private static void renameSubPledge(L2PcInstance player, int pledgeType, String pledgeName)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			return;
		}

		final L2Clan clan = player.getClan();
		final SubPledge subPledge = player.getClan().getSubPledge(pledgeType);

		if (subPledge == null)
		{
			player.sendMessage("Pledge don't exists.");
			return;
		}
		if (!Util.isAlphaNumeric(pledgeName) || 2 > pledgeName.length())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_NAME_INCORRECT));
			return;
		}
		if (pledgeName.length() > 16)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_NAME_TOO_LONG));
			return;
		}

		subPledge.setName(pledgeName);
		clan.updateSubPledgeInDB(subPledge.getId());
		clan.broadcastClanStatus();
		player.sendMessage("Pledge name changed.");
	}

	public void assignSubPledgeLeader(L2PcInstance player, String clanName, String leaderName)
	{
		if (Config.DEBUG)
		{
			Log.fine(player.getObjectId() + " (" + player.getName() + ") requested to assign sub clan" + clanName +
					"leader " + "(" + leaderName + ")");
		}

		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			return;
		}

		if (leaderName.length() > 16)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NAMING_CHARNAME_UP_TO_16CHARS));
			return;
		}

		if (player.getName().equals(leaderName))
		{
			player.sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED));
			return;
		}

		L2Clan clan = player.getClan();
		SubPledge subPledge = player.getClan().getSubPledge(clanName);

		if (null == subPledge)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_NAME_INCORRECT));
			return;
		}
		if (subPledge.getId() == L2Clan.SUBUNIT_ACADEMY)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_NAME_INCORRECT));
			return;
		}

		if (clan.getClanMember(leaderName) == null || clan.getClanMember(leaderName).getPledgeType() != 0)
		{
			if (subPledge.getId() >= L2Clan.SUBUNIT_KNIGHT1)
			{
				player.sendPacket(SystemMessage
						.getSystemMessage(SystemMessageId.CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED));
			}
			else if (subPledge.getId() >= L2Clan.SUBUNIT_ROYAL1)
			{
				player.sendPacket(
						SystemMessage.getSystemMessage(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED));
			}
			return;
		}

		int leaderId = clan.getClanMember(leaderName).getObjectId();

		subPledge.setLeaderId(leaderId);
		clan.updateSubPledgeInDB(subPledge.getId());
		L2ClanMember leaderSubPledge = clan.getClanMember(leaderName);
		if (leaderSubPledge.getPlayerInstance() != null)
		{
			leaderSubPledge.getPlayerInstance()
					.setPledgeClass(leaderSubPledge.calculatePledgeClass(leaderSubPledge.getPlayerInstance()));
			leaderSubPledge.getPlayerInstance().sendPacket(new UserInfo(leaderSubPledge.getPlayerInstance()));
		}
		clan.broadcastClanStatus();
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_BEEN_SELECTED_AS_CAPTAIN_OF_S2);
		sm.addString(leaderName);
		sm.addString(clanName);
		clan.broadcastToOnlineMembers(sm);
		sm = null;
	}

	public void createSubPledge(L2PcInstance player, String clanName, String leaderName, int pledgeType, int minClanLvl)
	{
		if (Config.DEBUG)
		{
			Log.fine(player.getObjectId() + " (" + player.getName() + ") requested sub clan creation from " +
					getObjectId() + " (" + getName() + ")");
		}

		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			return;
		}

		L2Clan clan = player.getClan();
		if (clan.getLevel() < minClanLvl)
		{
			if (pledgeType == L2Clan.SUBUNIT_ACADEMY)
			{
				player.sendPacket(SystemMessage
						.getSystemMessage(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN_ACADEMY));
			}
			else
			{
				player.sendPacket(SystemMessage
						.getSystemMessage(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_MILITARY_UNIT));
			}
			return;
		}
		if (!Util.isAlphaNumeric(clanName) || 2 > clanName.length())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_NAME_INCORRECT));
			return;
		}
		if (clanName.length() > 16)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_NAME_TOO_LONG));
			return;
		}
		for (L2Clan tempClan : ClanTable.getInstance().getClans())
		{
			if (tempClan.getSubPledge(clanName) != null)
			{
				if (pledgeType == L2Clan.SUBUNIT_ACADEMY)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_EXISTS);
					sm.addString(clanName);
					player.sendPacket(sm);
					sm = null;
				}
				else
				{
					player.sendPacket(SystemMessage
							.getSystemMessage(SystemMessageId.ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME));
				}
				return;
			}
		}

		if (pledgeType != L2Clan.SUBUNIT_ACADEMY)
		{
			if (clan.getClanMember(leaderName) == null || clan.getClanMember(leaderName).getPledgeType() != 0)
			{
				if (pledgeType >= L2Clan.SUBUNIT_KNIGHT1)
				{
					player.sendPacket(SystemMessage
							.getSystemMessage(SystemMessageId.CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED));
				}
				else if (pledgeType >= L2Clan.SUBUNIT_ROYAL1)
				{
					player.sendPacket(
							SystemMessage.getSystemMessage(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED));
				}
				return;
			}
		}

		int leaderId = pledgeType != L2Clan.SUBUNIT_ACADEMY ? clan.getClanMember(leaderName).getObjectId() : 0;

		if (clan.createSubPledge(player, pledgeType, leaderId, clanName) == null)
		{
			return;
		}

		SystemMessage sm;
		if (pledgeType == L2Clan.SUBUNIT_ACADEMY)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.THE_S1S_CLAN_ACADEMY_HAS_BEEN_CREATED);
			sm.addString(player.getClan().getName());
		}
		else if (pledgeType >= L2Clan.SUBUNIT_KNIGHT1)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.THE_KNIGHTS_OF_S1_HAVE_BEEN_CREATED);
			sm.addString(player.getClan().getName());
		}
		else if (pledgeType >= L2Clan.SUBUNIT_ROYAL1)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.THE_ROYAL_GUARD_OF_S1_HAVE_BEEN_CREATED);
			sm.addString(player.getClan().getName());
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_CREATED);
		}

		player.sendPacket(sm);
		if (pledgeType != L2Clan.SUBUNIT_ACADEMY)
		{
			L2ClanMember leaderSubPledge = clan.getClanMember(leaderName);
			if (leaderSubPledge.getPlayerInstance() == null)
			{
				return;
			}
			leaderSubPledge.getPlayerInstance()
					.setPledgeClass(leaderSubPledge.calculatePledgeClass(leaderSubPledge.getPlayerInstance()));
			leaderSubPledge.getPlayerInstance().sendPacket(new UserInfo(leaderSubPledge.getPlayerInstance()));
		}
	}

	public void changeClanLeader(L2PcInstance player, String target)
	{
		if (Config.DEBUG)
		{
			Log.fine(player.getObjectId() + " (" + player.getName() + ") requested change a clan leader from " +
					getObjectId() + " (" + getName() + ")");
		}

		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			return;
		}
		if (player.getName().equalsIgnoreCase(target))
		{
			return;
		}
		L2Clan clan = player.getClan();
		if (clan == null)
		{
			return;
		}
		//Restrict change the clan leader while clan is in siege
		for (Siege sg : SiegeManager.getInstance().getSieges())
		{
			if (sg.getIsInProgress() && (sg.checkIsAttacker(clan) || sg.checkIsDefender(clan)))
			{
				player.sendMessage("You can't change the clan leader while in siege!");
				return;
			}
		}

		L2ClanMember member = clan.getClanMember(target);
		if (member == null)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DOES_NOT_EXIST);
			sm.addString(target);
			player.sendPacket(sm);
			sm = null;
			return;
		}
		if (!member.isOnline())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INVITED_USER_NOT_ONLINE));
			return;
		}
		clan.setNewLeader(member);
	}

	public void recoverClan(L2PcInstance player, int clanId)
	{
		if (Config.DEBUG)
		{
			Log.fine(player.getObjectId() + " (" + player.getName() + ") requested recover a clan from " +
					getObjectId() + " (" + getName() + ")");
		}

		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			return;
		}
		L2Clan clan = player.getClan();

		clan.setDissolvingExpiryTime(0);
		clan.updateClanInDB();
	}

	public void dissolveClan(L2PcInstance player, int clanId)
	{
		if (Config.DEBUG)
		{
			Log.fine(player.getObjectId() + " (" + player.getName() + ") requested dissolve a clan from " +
					getObjectId() + " (" + getName() + ")");
		}

		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			return;
		}
		L2Clan clan = player.getClan();
		if (clan.getAllyId() != 0)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISPERSE_THE_CLANS_IN_ALLY));
			return;
		}
		if (clan.isAtWar())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_WAR));
			return;
		}
		if (clan.getHasCastle() != 0 || clan.getHasHideout() != 0 || clan.getHasFort() != 0)
		{
			player.sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISSOLVE_WHILE_OWNING_CLAN_HALL_OR_CASTLE));
			return;
		}
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			if (SiegeManager.getInstance().checkIsRegistered(clan, castle.getCastleId()))
			{
				player.sendPacket(SystemMessage
						.getSystemMessage(SystemMessageId.CANNOT_DISSOLVE_CAUSE_CLAN_WILL_PARTICIPATE_IN_CASTLE_SIEGE));
				return;
			}
		}
		for (Fort fort : FortManager.getInstance().getForts())
		{
			if (FortSiegeManager.getInstance().checkIsRegistered(clan, fort.getFortId()))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_SIEGE));
				return;
			}
		}
		if (player.isInsideZone(L2PcInstance.ZONE_SIEGE))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_SIEGE));
			return;
		}
		if (clan.getDissolvingExpiryTime() > System.currentTimeMillis())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DISSOLUTION_IN_PROGRESS));
			return;
		}

		clan.setDissolvingExpiryTime(
				System.currentTimeMillis() + Config.ALT_CLAN_DISSOLVE_DAYS * 86400000L); //24*60*60*1000 = 86400000
		clan.updateClanInDB();

		ClanTable.getInstance().scheduleRemoveClan(clan.getClanId());

		// The clan leader should take the XP penalty of a full death.
		player.deathPenalty(false, false, false, false);
	}

	private void showRetrieveWindow(L2PcInstance player, WarehouseListType itemtype, byte sortorder)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());

		if (player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH));
			return;
		}

		if (Config.DEBUG)
		{
			Log.fine("Showing stored items");
		}

		player.sendPacket(
				new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE, itemtype, sortorder));
	}

	private void showRetrieveWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());

		if (player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH));
			return;
		}

		if (Config.DEBUG)
		{
			Log.fine("Showing stored items");
		}
		player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE));
	}

	private void showDepositWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());
		player.tempInventoryDisable();
		if (Config.DEBUG)
		{
			Log.fine("Showing items to deposit");
		}

		player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.PRIVATE));
	}

	private void showDepositWindowClan(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		if (player.getClan() != null)
		{
			if (player.getClan().getLevel() == 0)
			{
				player.sendPacket(
						SystemMessage.getSystemMessage(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE));
			}
			else
			{
				player.setActiveWarehouse(player.getClan().getWarehouse());
				player.tempInventoryDisable();
				if (Config.DEBUG)
				{
					Log.fine("Showing items to deposit - clan");
				}

				WareHouseDepositList dl = new WareHouseDepositList(player, WareHouseDepositList.CLAN);
				player.sendPacket(dl);
			}
		}
	}

	private void showWithdrawWindowClan(L2PcInstance player, WarehouseListType itemtype, byte sortorder)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		if ((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) != L2Clan.CP_CL_VIEW_WAREHOUSE)
		{
			player.sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE));
		}
		else
		{
			if (player.getClan().getLevel() == 0)
			{
				player.sendPacket(
						SystemMessage.getSystemMessage(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE));
			}
			else
			{
				player.setActiveWarehouse(player.getClan().getWarehouse());
				if (Config.DEBUG)
				{
					Log.fine("Showing items to deposit - clan");
				}
				player.sendPacket(
						new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN, itemtype, sortorder));
			}
		}
	}

	private void showWithdrawWindowClan(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		if ((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) != L2Clan.CP_CL_VIEW_WAREHOUSE)
		{
			player.sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE));
		}
		else
		{
			if (player.getClan().getLevel() == 0)
			{
				player.sendPacket(
						SystemMessage.getSystemMessage(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE));
			}
			else
			{
				player.setActiveWarehouse(player.getClan().getWarehouse());
				if (Config.DEBUG)
				{
					Log.fine("Showing items to deposit - clan");
				}
				player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN));
			}
		}
	}

	public void showSkillList(L2PcInstance player)
	{
		if (player.isTransformed())
		{
			return;
		}

		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player);
		ExAcquireSkillList asl = new ExAcquireSkillList(ExAcquireSkillList.SkillType.Fishing);

		int counts = 0;

		for (L2SkillLearn s : skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

			if (sk == null)
			{
				continue;
			}

			counts++;
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getSpCost(), 1);
		}

		if (counts == 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			int minlevel = SkillTreeTable.getInstance().getMinLevelForNewSkill(player);

			if (minlevel > 0)
			{
				// No more skills to learn, come back when you level.
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
				sm.addNumber(minlevel);
				player.sendPacket(sm);
			}
			else
			{
				String s = "<html><head><body>" + "You've learned all skills.<br>" + "</body></html>";
				html.setHtml(s);
				player.sendPacket(html);
			}
		}
		else
		{
			player.sendPacket(asl);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
