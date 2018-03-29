/*
 * $Header: IllegalPlayerAction.java, 21/10/2005 23:32:02 luisantonioa Exp $
 *
 * $Author: luisantonioa $ $Date: 21/10/2005 23:32:02 $ $Revision: 1 $ $Log:
 * IllegalPlayerAction.java,v $ Revision 1 21/10/2005 23:32:02 luisantonioa
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

package l2server.gameserver.util;

import l2server.Config;
import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * This class ...
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public final class IllegalPlayerAction implements Runnable
{
	private static Logger logAudit = Logger.getLogger("audit");

	private String message;
	private int punishment;
	private L2PcInstance actor;

	public static final int PUNISH_BROADCAST = 1;
	public static final int PUNISH_KICK = 2;
	public static final int PUNISH_KICKBAN = 3;
	public static final int PUNISH_JAIL = 4;

	public IllegalPlayerAction(L2PcInstance actor, String message, int punishment)
	{
		this.message = message;
		this.punishment = punishment;
		this.actor = actor;

		switch (punishment)
		{
			case PUNISH_KICK:
				actor.sendMessage("You will be kicked for illegal action, GM informed.");
				break;
			case PUNISH_KICKBAN:
				//actor.setAccessLevel(-100);
				actor.setAccountAccesslevel(-100);
				actor.sendMessage("You are banned for illegal action, GM informed.");
				break;
			case PUNISH_JAIL:
				actor.sendMessage("Illegal action performed!");
				actor.sendMessage("You will be teleported to GM Consultation Service area and jailed.");
				break;
		}
	}

	@Override
	public void run()
	{
		if (actor.isGM())
		{
			return;
		}

		LogRecord record = new LogRecord(Level.INFO, "AUDIT:" + message);
		record.setLoggerName("audit");
		record.setParameters(new Object[]{actor, punishment});
		logAudit.log(record);

		Broadcast.toGameMasters(message);

		switch (punishment)
		{
			case PUNISH_BROADCAST:
				return;
			case PUNISH_KICK:
				actor.logout(false);
				break;
			case PUNISH_KICKBAN:
				actor.logout();
				break;
			case PUNISH_JAIL:
				actor.setPunishLevel(L2PcInstance.PunishLevel.JAIL, Config.DEFAULT_PUNISH_PARAM);
				break;
		}
	}
}
