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

package handlers.bypasshandlers;

import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.datatables.PlayerClassTable;
import l2server.gameserver.datatables.SkillTreeTable;
import l2server.gameserver.handler.IBypassHandler;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.base.PlayerClass;
import l2server.gameserver.model.olympiad.HeroesManager;
import l2server.gameserver.model.olympiad.Olympiad;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.ArrayList;
import java.util.List;

public class SpecializeClass implements IBypassHandler
{
	private static final String[] COMMANDS = {"SpecializeClass"};

	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Npc target)
	{
		if (target == null || activeChar == null || activeChar.getCurrentClass().getLevel() != 85)
		{
			activeChar.sendMessage("You must be on an awakened class to do this.");
			return false;
		}

		if (activeChar.getTemporaryLevel() != 0)
		{
			activeChar.sendMessage("You can't do that while on a temporary level.");
			return false;
		}

		boolean hasDeprecatedClass = activeChar.getClassId() >= 139 && activeChar.getClassId() <= 145;

		if (command.length() < 16)
		{
			String html = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "classmaster/specialize.htm");
			String buttons = "";
			List<PlayerClass> classes = new ArrayList<PlayerClass>();
			for (PlayerClass cl1 : PlayerClassTable.getInstance().getAllClasses())
			{
				if (cl1.getLevel() == 76 && cl1.getAwakeningClassId() == activeChar.getClassId())
				{
					for (PlayerClass cl2 : PlayerClassTable.getInstance().getAllClasses())
					{
						if (cl2.getParent() == cl1)
						{
							classes.add(cl2);
						}
					}
				}
			}

			if (classes.isEmpty())
			{
				for (PlayerClass cl : PlayerClassTable.getInstance().getAllClasses())
				{
					if (cl.getLevel() == 85 && cl.getParent() != null)
					{
						if (cl.getParent().getAwakeningClassId() == activeChar.getClassId())
						{
							classes.add(cl);
						}
						else if (activeChar.getCurrentClass().getParent() != null &&
								cl.getParent().getAwakeningClassId() ==
										activeChar.getCurrentClass().getParent().getAwakeningClassId())
						{
							classes.add(cl);
						}
					}
				}
			}

			if (activeChar.getClassId() == activeChar.getBaseClass())
			{
				/*if (activeChar.isHero())
                {
					activeChar.sendPacket(new ExShowScreenMessage("You cannot use this option while you're a hero!", 6000));
					return false;
				}
				else if (Olympiad.getInstance().getNobleInfo(activeChar.getObjectId()) != null)
					activeChar.sendPacket(new ExShowScreenMessage("WARNING: If you use this option, your olympiad and hero records will be reset!", 6000));*/
				if (activeChar.isHero() || Olympiad.getInstance().getNobleInfo(activeChar.getObjectId()) != null)
				{
					activeChar.sendPacket(new ExShowScreenMessage(
							"You cannot use this option while you're involved in the Grand Olympiads!", 6000));
					return false;
				}
			}

			for (PlayerClass cl : classes)
			{
				buttons += "<button value=\"" + cl.getName() + "\" action=\"bypass -h npc_%objectId%_SpecializeClass " +
						cl.getId() +
						"\" width=\"200\" height=\"31\" back=\"L2UI_CT1.HtmlWnd_DF_Awake_Down\" fore=\"L2UI_CT1.HtmlWnd_DF_Awake\"><br>";
			}

			html = html.replace("%classButtons%", buttons);
			NpcHtmlMessage packet = new NpcHtmlMessage(target.getObjectId());
			packet.setHtml(html);
			packet.replace("%objectId%", String.valueOf(target.getObjectId()));

			activeChar.sendPacket(packet);
		}
		else
		{
			try
			{
				if (activeChar.getClassId() == activeChar.getBaseClass() && activeChar.isHero())
				{
					return false;
				}

				int classId = Integer.parseInt(command.substring(16));
				PlayerClass prev = activeChar.getCurrentClass();
				if (!hasDeprecatedClass && prev.getLevel() == 85)
				{
					if (!activeChar.isSubClassActive())
					{
						if (!activeChar.destroyItemByItemId("Specialize Class", 36949, 1, activeChar, true))
						{
							return false;
						}
					}
					else
					{
						if (!activeChar.destroyItemByItemId("Specialize Class", 37494, 1, activeChar, true))
						{
							return false;
						}
					}
				}

				if (activeChar.getClassId() == activeChar.getBaseClass() &&
						(activeChar.isHero() || Olympiad.getInstance().getNobleInfo(activeChar.getObjectId()) != null))
				{
					Olympiad.getInstance().removeNoble(activeChar.getObjectId());
					HeroesManager.getInstance().removeHero(activeChar.getObjectId());
				}

				activeChar.setClassId(classId);
				if (!activeChar.isSubClassActive())
				{
					activeChar.setBaseClass(classId);
				}

				for (L2Skill skill : activeChar.getAllSkills())
				{
					if (!SkillTreeTable.getInstance().isSkillAllowed(activeChar, skill))
					{
						activeChar.removeSkill(skill, true);
					}
				}

				activeChar.sendSkillList();

				activeChar.store();
				activeChar.broadcastUserInfo();
			}
			catch (StringIndexOutOfBoundsException e)
			{
				e.printStackTrace();
			}
		}

		return true;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
