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

import l2server.Config;
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.datatables.PlayerClassTable;
import l2server.gameserver.datatables.PledgeSkillTree;
import l2server.gameserver.events.instanced.EventsManager;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.instancemanager.FortManager;
import l2server.gameserver.instancemanager.FortSiegeManager;
import l2server.gameserver.instancemanager.SiegeManager;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2Clan.SubPledge;
import l2server.gameserver.model.L2ClanMember;
import l2server.gameserver.model.L2PledgeSkillLearn;
import l2server.gameserver.model.base.PlayerClass;
import l2server.gameserver.model.base.Race;
import l2server.gameserver.model.base.SubClass;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.entity.Fort;
import l2server.gameserver.model.entity.Siege;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.StringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class ...
 *
 * @version $Revision: 1.4.2.3.2.8 $ $Date: 2005/03/29 23:15:15 $
 */
public class L2VillageMasterInstance extends L2NpcInstance
{
	//

	/**
	 * @param template
	 */
	public L2VillageMasterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2VillageMasterInstance);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";

		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}

		return "villagemaster/" + pom + ".htm";
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
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

		if (actualCommand.equalsIgnoreCase("create_clan"))
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

			if (player.getClan() == null)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_CLAN_LEADER_CREATE_ALLIANCE));
			}
			else
			{
				player.getClan().createAlly(player, cmdParams);
			}
		}
		else if (actualCommand.equalsIgnoreCase("dissolve_ally"))
		{
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
			if (player.getClan().levelUpClan(player))
			{
				player.broadcastPacket(new MagicSkillUse(player, 5103, 1, 0, 0));
				player.broadcastPacket(new MagicSkillLaunched(player, 5103, 1));
			}
		}
		else if (actualCommand.equalsIgnoreCase("learn_clan_skills"))
		{
			showPledgeSkillList(player);
		}
		else if (command.startsWith("Subclass"))
		{
			if (player.getTemporaryLevel() != 0)
			{
				player.sendMessage("You may not do this on a temporary level.");
				return;
			}

			// Subclasses may not be changed while a skill is in use.
			if (player.isCastingNow() || player.isAllSkillsDisabled())
			{
				player.sendPacket(SystemMessage
						.getSystemMessage(SystemMessageId.SUBCLASS_NO_CHANGE_OR_CREATE_WHILE_SKILL_IN_USE));
				return;
			}

			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());

			if (player.getTransformation() != null)
			{
				html.setFile(player.getHtmlPrefix(), "villagemaster/SubClass_NoTransformed.htm");
				player.sendPacket(html);
				return;
			}

			int cmdChoice = 0;
			int paramOne = 0;
			int paramTwo = 0;

			try
			{
				cmdChoice = Integer.parseInt(command.substring(9, 10).trim());

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
					html.setFile(player.getHtmlPrefix(), getSubClassMenu(player.getRace()));
					break;
				case 1: // Add Subclass - Initial
					// Avoid giving player an option to add a new sub class, if they have three already.
					if (player.getTotalSubClasses() >= maxSubs)
					{
						html.setFile(player.getHtmlPrefix(), getSubClassFail());
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
					if (player.getSubClasses().isEmpty() ||
							EventsManager.getInstance().isPlayerParticipant(player.getObjectId()))
					{
						html.setFile(player.getHtmlPrefix(), "villagemaster/SubClass_ChangeNo.htm");
					}
					else
					{
						final StringBuilder content2 = StringUtil.startAppend(200);

						if (checkVillageMaster(player.getBaseClass()))
						{
							StringUtil.append(content2, "<a action=\"bypass -h npc_%objectId%_Subclass 5 0\">",
									PlayerClassTable.getInstance().getClassNameById(player.getBaseClass()), "</a><br>");
						}

						for (Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext(); )
						{
							SubClass subClass = subList.next();
							if (checkVillageMaster(subClass.getClassDefinition()))
							{
								StringUtil.append(content2, "<a action=\"bypass -h npc_%objectId%_Subclass 5 ",
										String.valueOf(subClass.getClassIndex()), "\">", formatClassForDisplay(
												PlayerClassTable.getInstance().getClassById(subClass.getClassId())),
										"</a><br>");
							}
						}

						if (content2.length() > 0)
						{
							html.setFile(player.getHtmlPrefix(), "villagemaster/SubClass_Change.htm");
							html.replace("%list%", content2.toString());
						}
						else
						{
							html.setFile(player.getHtmlPrefix(), "villagemaster/SubClass_ChangeNotFound.htm");
						}
					}
					break;
				case 3: // Change/Cancel Subclass - Initial
					if (player.getSubClasses() == null || player.getSubClasses().isEmpty())
					{
						html.setFile(player.getHtmlPrefix(), "villagemaster/SubClass_ModifyEmpty.htm");
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
							html.replace("<a action=\"bypass -h npc_%objectId%_Subclass 6 1\">%sub1%</a><br>", "");
						}

						if (player.getSubClasses().containsKey(2))
						{
							html.replace("%sub2%", PlayerClassTable.getInstance()
									.getClassNameById(player.getSubClasses().get(2).getClassId()));
						}
						else
						{
							html.replace("<a action=\"bypass -h npc_%objectId%_Subclass 6 2\">%sub2%</a><br>", "");
						}

						if (player.getSubClasses().containsKey(3))
						{
							html.replace("%sub3%", PlayerClassTable.getInstance()
									.getClassNameById(player.getSubClasses().get(3).getClassId()));
						}
						else
						{
							html.replace("<a action=\"bypass -h npc_%objectId%_Subclass 6 3\">%sub3%</a><br>", "");
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

					/*
					 * If quest checking is enabled, verify if the character has completed the Mimir's Elixir (Path to Subclass)
					 * and Fate's Whisper (A Grade Weapon) quests by checking for instances of their unique reward items.
					 *
					 * If they both exist, remove both unique items and continue with adding the sub-class.
					 */
					if (allowAddition && !Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS)
					{
						allowAddition = checkQuests(player);
					}

					if (allowAddition && isValidNewSubClass(player, paramOne))
					{
						if (!player.addSubClass(paramOne, player.getTotalSubClasses() + 1, 0))
						{
							return;
						}

						player.setActiveClass(player.getTotalSubClasses());

						html.setFile(player.getHtmlPrefix(), "villagemaster/SubClass_AddOk.htm");

						player.sendPacket(
								SystemMessage.getSystemMessage(SystemMessageId.ADD_NEW_SUBCLASS)); // Subclass added.
						player.sendPacket(new ExSubjobInfo(player));
					}
					else
					{
						html.setFile(player.getHtmlPrefix(), getSubClassFail());
					}
					break;
				case 5: // Change Class - Action
					/*
					 * If the character is less than level 75 on any of their previously chosen
					 * classes then disallow them to change to their most recently added sub-class choice.
					 *
					 * Note: paramOne = classIndex
					 */

					if (!player.getFloodProtectors().getSubclass().tryPerformAction("change class"))
					{
						Log.warning("Player " + player.getName() + " has performed a subclass change too fast");
						return;
					}

					if (player.hasIdentityCrisis()) // Cannot switch or change subclasses while identity crisis during
					{
						player.sendMessage("You cannot switch your subclass while Identity crisis are in progress.");
						return;
					}

					if (player.getClassIndex() == paramOne)
					{
						html.setFile(player.getHtmlPrefix(), "villagemaster/SubClass_Current.htm");
						break;
					}

					if (paramOne == 0)
					{
						if (!checkVillageMaster(player.getBaseClass()))
						{
							return;
						}
					}
					else
					{
						try
						{
							if (!checkVillageMaster(player.getSubClasses().get(paramOne).getClassDefinition()))
							{
								return;
							}
						}
						catch (NullPointerException e)
						{
							return;
						}
					}

					player.setActiveClass(paramOne);

					player.sendPacket(SystemMessage
							.getSystemMessage(SystemMessageId.SUBCLASS_TRANSFER_COMPLETED)); // Transfer completed.
					player.sendPacket(new ExSubjobInfo(player));
					return;
				case 6: // Change/Cancel Subclass - Choice
					// validity check
					if (paramOne < 1 || paramOne > maxSubs)
					{
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

					if (!player.getFloodProtectors().getSubclass().tryPerformAction("change class"))
					{
						Log.warning("Player " + player.getName() + " has performed a subclass change too fast");
						return;
					}

					if (player.hasIdentityCrisis()) // Cannot switch or change subclasses while identity crisis during
					{
						player.sendMessage("You cannot switch your subclass while Identity crisis are in progress.");
						return;
					}

					if (!isValidNewSubClass(player, paramTwo))
					{
						return;
					}

					if (player.modifySubClass(paramOne, paramTwo))
					{
						player.abortCast();
						player.stopAllEffectsExceptThoseThatLastThroughDeath(); // all effects from old subclass stopped!
						player.stopCubics();
						player.setActiveClass(paramOne);

						html.setFile(player.getHtmlPrefix(), "villagemaster/SubClass_ModifyOk.htm");
						html.replace("%name%", PlayerClassTable.getInstance().getClassNameById(paramTwo));

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
					if (paramOne < 1 || paramOne > maxSubs || player.getSubClasses().get(paramOne) == null)
					{
						return;
					}

					if (player.getSubClasses().get(paramOne).getClassDefinition().getId() == 136)
					{
						player.sendMessage("You cannot make Judicator be your dual class!");
						return;
					}

					if (player.getSubClasses().get(paramOne).getLevel() < 80)
					{
						player.sendMessage("This subclass is not at level 80!");
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

					player.getSubClasses().get(paramOne).setIsDual(true);
					player.sendPacket(new ExSubjobInfo(player));

					html.setHtml(
							"<html><body>Make Dual Class:<br>Your subclass is now a <font color=\"LEVEL\">dual class</font>.</body></html>");

					//player.sendMessage("Dual class created!"); // Subclass added.
					break;
			}

			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		else
		{
			// this class dont know any other commands, let forward
			// the command to the parent class
			super.onBypassFeedback(player, command);
		}
	}

	protected String getSubClassMenu(Race pRace)
	{
		if (Config.ALT_GAME_SUBCLASS_EVERYWHERE || pRace != Race.Kamael)
		{
			return "villagemaster/SubClass.htm";
		}

		return "villagemaster/SubClass_NoOther.htm";
	}

	protected String getSubClassFail()
	{
		return "villagemaster/SubClass_Fail.htm";
	}

	protected boolean checkQuests(L2PcInstance player)
	{
		// Noble players can add subbclasses without quests
		if (player.isNoble())
		{
			return true;
		}

		QuestState qs = player.getQuestState("234_FatesWhisper");
		if (qs == null || !qs.isCompleted())
		{
			return false;
		}

		qs = player.getQuestState("235_MimirsElixir");
		return !(qs == null || !qs.isCompleted());

	}

	/*
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

				// check for the village master
				if (!checkVillageMaster(pclass.getId()))
				{
					PlayerClassTable.getInstance().getAvailableSubclasses(player, baseClassId).remove(subId);
					continue;
				}

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
		if (!checkVillageMaster(classId))
		{
			return false;
		}

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

	protected boolean checkVillageMasterRace(PlayerClass pclass)
	{
		return true;
	}

	protected boolean checkVillageMasterTeachType(PlayerClass pclass)
	{
		return true;
	}

	/*
	 * Returns true if this classId allowed for master
	 */
	public final boolean checkVillageMaster(int classId)
	{
		return checkVillageMaster(PlayerClassTable.getInstance().getClassById(classId));
	}

	/*
	 * Returns true if this PlayerClass is allowed for master
	 */
	public final boolean checkVillageMaster(PlayerClass pclass)
	{
		if (Config.ALT_GAME_SUBCLASS_EVERYWHERE)
		{
			return true;
		}

		return checkVillageMasterRace(pclass) && checkVillageMasterTeachType(pclass);
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

	private static Iterator<SubClass> iterSubClasses(L2PcInstance player)
	{
		return player.getSubClasses().values().iterator();
	}

	private static void dissolveClan(L2PcInstance player, int clanId)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			return;
		}

		final L2Clan clan = player.getClan();
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
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_SIEGE));
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

	private static void recoverClan(L2PcInstance player, int clanId)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			return;
		}

		final L2Clan clan = player.getClan();
		clan.setDissolvingExpiryTime(0);
		clan.updateClanInDB();
	}

	private static void changeClanLeader(L2PcInstance player, String target)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			return;
		}
		if (player.getName().equalsIgnoreCase(target))
		{
			return;
		}
		/*
		 * Until proper clan leader change support is done, this is a little
		 * exploit fix (leader, while fliying wyvern changes clan leader and the new leader
		 * can ride the wyvern too)
		 * DrHouse
		 */
		if (player.isFlying())
		{
			player.sendMessage("Please, stop flying");
			return;
		}

		final L2Clan clan = player.getClan();
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

		final L2ClanMember member = clan.getClanMember(target);
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

	private static void createSubPledge(L2PcInstance player, String clanName, String leaderName, int pledgeType, int minClanLvl)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			return;
		}

		final L2Clan clan = player.getClan();
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

		final int leaderId = pledgeType != L2Clan.SUBUNIT_ACADEMY ? clan.getClanMember(leaderName).getObjectId() : 0;

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
			final L2ClanMember leaderSubPledge = clan.getClanMember(leaderName);
			final L2PcInstance leaderPlayer = leaderSubPledge.getPlayerInstance();
			if (leaderPlayer != null)
			{
				leaderPlayer.setPledgeClass(leaderSubPledge.calculatePledgeClass(leaderPlayer));
				leaderPlayer.sendPacket(new UserInfo(leaderPlayer));
			}
		}
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

	private static void assignSubPledgeLeader(L2PcInstance player, String clanName, String leaderName)
	{
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

		final L2Clan clan = player.getClan();
		final SubPledge subPledge = player.getClan().getSubPledge(clanName);

		if (null == subPledge || subPledge.getId() == L2Clan.SUBUNIT_ACADEMY)
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

		subPledge.setLeaderId(clan.getClanMember(leaderName).getObjectId());
		clan.updateSubPledgeInDB(subPledge.getId());

		final L2ClanMember leaderSubPledge = clan.getClanMember(leaderName);
		final L2PcInstance leaderPlayer = leaderSubPledge.getPlayerInstance();
		if (leaderPlayer != null)
		{
			leaderPlayer.setPledgeClass(leaderSubPledge.calculatePledgeClass(leaderPlayer));
			leaderPlayer.sendPacket(new UserInfo(leaderPlayer));
		}

		clan.broadcastClanStatus();
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_BEEN_SELECTED_AS_CAPTAIN_OF_S2);
		sm.addString(leaderName);
		sm.addString(clanName);
		clan.broadcastToOnlineMembers(sm);
		sm = null;
	}

	/**
	 * this displays PledgeSkillList to the player.
	 *
	 * @param player
	 */
	public static void showPledgeSkillList(L2PcInstance player)
	{
		if (player.getClan() == null || !player.isClanLeader())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setFile(player.getHtmlPrefix(), "villagemaster/NotClanLeader.htm");
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
				SystemMessage sm =
						SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1);
				if (player.getClan().getLevel() < 5)
				{
					sm.addNumber(5);
				}
				else
				{
					sm.addNumber(player.getClan().getLevel() + 1);
				}
				player.sendPacket(sm);
			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(player.getHtmlPrefix(), "villagemaster/NoMoreSkills.htm");
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
