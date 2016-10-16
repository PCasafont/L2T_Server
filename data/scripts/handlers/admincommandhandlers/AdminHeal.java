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

import java.util.Collection;
import java.util.logging.Logger;

import l2server.Config;
import l2server.gameserver.handler.IAdminCommandHandler;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;

/**
 * This class handles following admin commands:
 * - heal = restores HP/MP/CP on target, name or radius
 *
 * @version $Revision: 1.2.4.5 $ $Date: 2005/04/11 10:06:06 $
 *          Small typo fix by Zoey76 24/02/2011
 */
public class AdminHeal implements IAdminCommandHandler
{
	private static Logger log = Logger.getLogger(AdminRes.class.getName());
	private static final String[] ADMIN_COMMANDS = {"admin_heal"};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{

		if (command.equals("admin_heal"))
		{
			handleHeal(activeChar);
		}
		else if (command.startsWith("admin_heal"))
		{
			try
			{
				String healTarget = command.substring(11);
				handleHeal(activeChar, healTarget);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				if (Config.DEVELOPER)
				{
					Log.warning("Heal error: " + e);
				}
				activeChar.sendMessage("Incorrect target/radius specified.");
			}
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void handleHeal(L2PcInstance activeChar)
	{
		handleHeal(activeChar, null);
	}

	private void handleHeal(L2PcInstance activeChar, String player)
	{

		L2Object obj = activeChar.getTarget();
		if (player != null)
		{
			L2PcInstance plyr = L2World.getInstance().getPlayer(player);

			if (plyr != null)
			{
				obj = plyr;
			}
			else
			{
				try
				{
					int radius = Integer.parseInt(player);
					Collection<L2Object> objs = activeChar.getKnownList().getKnownObjects().values();
					//synchronized (activeChar.getKnownList().getKnownObjects())
					{
						for (L2Object object : objs)
						{
							if (object instanceof L2Character)
							{
								L2Character character = (L2Character) object;
								character.setCurrentHpMp(character.getMaxHp(), character.getMaxMp());
								if (object instanceof L2PcInstance)
								{
									character.setCurrentCp(character.getMaxCp());
								}
							}
						}
					}
					activeChar.sendMessage("Healed within " + radius + " unit radius.");
					return;
				}
				catch (NumberFormatException nbe)
				{
				}
			}
		}
		if (obj == null)
		{
			obj = activeChar;
		}
		if (obj instanceof L2Character)
		{
			L2Character target = (L2Character) obj;
			target.setCurrentHpMp(target.getMaxHp(), target.getMaxMp());
			if (target instanceof L2PcInstance)
			{
				target.setCurrentCp(target.getMaxCp());
			}
			if (Config.DEBUG)
			{
				Log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") healed character " +
						target.getName());
			}
		}
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
		}
	}
}
