/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.util;

import l2server.gameserver.TimeController;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.L2GameClient;
import l2server.log.Log;
import l2server.util.StringUtil;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * Flood protector implementation.
 *
 * @author fordfrog
 */
public final class FloodProtectorAction
{

    /*
	  Logger
     */

	/**
	 * Client for this instance of flood protector.
	 */
	private final L2GameClient client;
	/**
	 * Configuration of this instance of flood protector.
	 */
	private final FloodProtectorConfig config;
	/**
	 * Next game tick when new request is allowed.
	 */
	private volatile int nextGameTick = TimeController.getGameTicks();
	/**
	 * Request counter.
	 */
	private AtomicInteger count = new AtomicInteger(0);
	/**
	 * Flag determining whether exceeding request has been logged.
	 */
	private boolean logged;
	/**
	 * Flag determining whether punishment application is in progress so that we do not apply
	 * punisment multiple times (flooding).
	 */
	private volatile boolean punishmentInProgress;

	/**
	 * Creates new instance of FloodProtectorAction.
	 *
	 * @param config flood protector configuration
	 */
	public FloodProtectorAction(final L2GameClient client, final FloodProtectorConfig config)
	{
		super();
		this.client = client;
		this.config = config;
	}

	/**
	 * Checks whether the request is flood protected or not.
	 *
	 * @param command command issued or short command description
	 * @return true if action is allowed, otherwise false
	 */
	public synchronized boolean tryPerformAction(final String command)
	{
		final int curTick = TimeController.getGameTicks();

		if (curTick < nextGameTick || punishmentInProgress)
		{
			if (config.LOG_FLOODING && !logged && Log.isLoggable(Level.WARNING))
			{
				log(" called command ", command, " ~", String.valueOf(
						(config.FLOOD_PROTECTION_INTERVAL - (nextGameTick - curTick)) *
								TimeController.MILLIS_IN_TICK), " ms after previous command");
				logged = true;
			}

			count.incrementAndGet();

			if (!punishmentInProgress && config.PUNISHMENT_LIMIT > 0 && count.get() >= config.PUNISHMENT_LIMIT &&
					config.PUNISHMENT_TYPE != null)
			{
				punishmentInProgress = true;

				if ("kick".equals(config.PUNISHMENT_TYPE))
				{
					kickPlayer();
				}
				else if ("ban".equals(config.PUNISHMENT_TYPE))
				{
					banAccount();
				}
				else if ("jail".equals(config.PUNISHMENT_TYPE))
				{
					jailChar();
				}

				punishmentInProgress = false;
			}

			return false;
		}

		if (count.get() > 0)
		{
			if (config.LOG_FLOODING && Log.isLoggable(Level.WARNING))
			{
				log(" issued ", String.valueOf(count), " extra requests within ~",
						String.valueOf(config.FLOOD_PROTECTION_INTERVAL * TimeController.MILLIS_IN_TICK), " ms");
			}
		}

		nextGameTick = curTick + config.FLOOD_PROTECTION_INTERVAL;
		logged = false;
		count.set(0);

		return true;
	}

	/**
	 * Kick player from game (close network connection).
	 */
	private void kickPlayer()
	{
		if (client.getActiveChar() != null)
		{
			client.getActiveChar().logout(false);
		}
		else
		{
			client.closeNow();
		}

		if (Log.isLoggable(Level.WARNING))
		{
			log("kicked for flooding");
		}
	}

	/**
	 * Bans char account and logs out the char.
	 */
	private void banAccount()
	{
		if (client.getActiveChar() != null)
		{
			client.getActiveChar().setPunishLevel(L2PcInstance.PunishLevel.ACC, config.PUNISHMENT_TIME);

			if (Log.isLoggable(Level.WARNING))
			{
				log(" banned for flooding ",
						config.PUNISHMENT_TIME <= 0 ? "forever" : "for " + config.PUNISHMENT_TIME + " mins");
			}

			client.getActiveChar().logout();
		}
		else
		{
			log(" unable to ban account: no active player");
		}
	}

	/**
	 * Jails char.
	 */
	private void jailChar()
	{
		if (client.getActiveChar() != null)
		{
			client.getActiveChar().setPunishLevel(L2PcInstance.PunishLevel.JAIL, config.PUNISHMENT_TIME);

			if (Log.isLoggable(Level.WARNING))
			{
				log(" jailed for flooding ",
						config.PUNISHMENT_TIME <= 0 ? "forever" : "for " + config.PUNISHMENT_TIME + " mins");
			}
		}
		else
		{
			log(" unable to jail: no active player");
		}
	}

	private void log(String... lines)
	{
		final StringBuilder output = StringUtil.startAppend(100, config.FLOOD_PROTECTOR_TYPE, ": ");
		String address = null;
		try
		{
			if (!client.isDetached())
			{
				address = client.getConnection().getInetAddress().getHostAddress();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		switch (client.getState())
		{
			case IN_GAME:
				if (client.getActiveChar() != null)
				{
					StringUtil.append(output, client.getActiveChar().getName());
					StringUtil.append(output, "(", String.valueOf(client.getActiveChar().getObjectId()), ") ");
				}
			case AUTHED:
				if (client.getAccountName() != null)
				{
					StringUtil.append(output, client.getAccountName(), " ");
				}
			case CONNECTED:
				if (address != null)
				{
					StringUtil.append(output, address);
				}
				break;
			default:
				throw new IllegalStateException("Missing state on switch");
		}

		StringUtil.append(output, lines);
		Log.warning(output.toString());
	}
}
