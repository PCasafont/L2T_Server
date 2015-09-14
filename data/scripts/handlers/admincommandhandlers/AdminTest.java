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

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.IAdminCommandHandler;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ExOlympiadMode;
import l2server.gameserver.network.serverpackets.MagicSkillUse;

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
				caster.broadcastPacket(new MagicSkillUse(caster, activeChar, id, 1, _skill.getHitTime(), _skill.getReuseDelay(), _skill.getReuseHashCode(), 0, 0));
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
