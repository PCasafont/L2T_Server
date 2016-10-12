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
import l2server.gameserver.datatables.PlayerClassTable;
import l2server.gameserver.instancemanager.QuestManager;
import l2server.gameserver.model.base.PlayerClass;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.UserInfo;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.util.StringUtil;

/**
 * This class ...
 *
 * @version $Revision: 1.4.2.1.2.7 $ $Date: 2005/03/27 15:29:32 $
 */
public final class L2ClassMasterInstance extends L2MerchantInstance
{
	/**
	 * @param template
	 */
	public L2ClassMasterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2ClassMasterInstance);
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

		return "classmaster/" + pom + ".htm";
	}

	@Override
	public String getHtmlPath(int npcId, String val)
	{
		String pom = "";
		if (val.isEmpty() || val.equals("0"))
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}

		return "classmaster/" + pom + ".htm";
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
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
		else if (command.startsWith("become_noble"))
		{
			if (!player.isNoble())
			{
				player.setNoble(true);
				player.sendPacket(new UserInfo(player));
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player.getHtmlPrefix(), "classmaster/nobleok.htm");
				player.sendPacket(html);
			}
		}
		else if (command.startsWith("learn_skills"))
		{
			player.giveAvailableSkills(false);
		}
		else if (command.startsWith("increase_clan_level"))
		{
			if (player.getClan() == null || !player.isClanLeader())
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player.getHtmlPrefix(), "classmaster/noclanleader.htm");
				player.sendPacket(html);
			}
			else if (player.getClan().getLevel() >= 5)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player.getHtmlPrefix(), "classmaster/noclanlevel.htm");
				player.sendPacket(html);
			}
			else
			{
				player.getClan().changeLevel(5);
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

		if (!Config.ALLOW_CLASS_MASTERS)
		{
			html.setFile(player.getHtmlPrefix(), "classmaster/disabled.htm");
		}
		else
		{
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
					for (PlayerClass cl : PlayerClassTable.getInstance().getAllClasses())
					{
						if (cl.getId() == 135 && player.getTotalSubClasses() < 2)
						{
							continue;
						}
						if (validateClassId(currentClass, cl) && cl.level() == level)
						{
							StringUtil.append(menu, "<a action=\"bypass -h npc_%objectId%_change_class ",
									String.valueOf(cl.getId()), "\">",
									PlayerClassTable.getInstance().getClassNameById(cl.getId()), "</a><br>");
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
		}

		html.replace("%objectId%", String.valueOf(objectId));
		html.replace("%req_items%", getRequiredItems(level));
		player.sendPacket(html);
	}

	private static boolean checkAndChangeClass(L2PcInstance player, int val)
	{
		final PlayerClass currentClass = player.getCurrentClass();
		if (getMinLevel(currentClass.level()) > player.getLevel() && !Config.ALLOW_ENTIRE_TREE)
		{
			return false;
		}

		if (!validateClassId(currentClass, val))
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
		Quest q = QuestManager.getInstance().getQuest("SkillTransfer");
		if (q != null)
		{
			q.startQuestTimer("givePormanders", 1, null, player);
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
	private static boolean validateClassId(PlayerClass oldC, int val)
	{
		try
		{
			return validateClassId(oldC, PlayerClassTable.getInstance().getClassById(val));
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
	private static boolean validateClassId(PlayerClass oldC, PlayerClass newC)
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

	private static String getRequiredItems(int level)
	{
		return "<tr><td>none</td></tr>";
	}
}
