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

package l2server.gameserver;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.datatables.OfflineTradersTable;
import l2server.gameserver.events.DamageManager;
import l2server.gameserver.events.LotterySystem;
import l2server.gameserver.instancemanager.*;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.ClanWarManager;
import l2server.gameserver.model.olympiad.HeroesManager;
import l2server.gameserver.model.olympiad.Olympiad;
import l2server.gameserver.network.L2GameClient;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.ServerClose;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Broadcast;
import l2server.log.Log;

import java.util.Collection;
import java.util.logging.Level;

/**
 * This class provides the functions for shutting down and restarting the server
 * It closes all open clientconnections and saves all data.
 *
 * @version $Revision: 1.2.4.5 $ $Date: 2005/03/27 15:29:09 $
 */
public class Shutdown extends Thread
{
	public static final int SIGTERM = 0;
	public static final int GM_SHUTDOWN = 1;
	public static final int GM_RESTART = 2;
	public static final int ABORT = 3;
	private static final String[] MODE_TEXT = {"SIGTERM", "shutting down", "restarting", "aborting"};

	private int _shutdownMode = SIGTERM;
	private static ShutdownTask _task = null;

	private boolean _shuttingDown = false;

	public void startShutdown(L2PcInstance activeChar, int seconds, boolean restart)
	{
		String text = null;
		if (activeChar != null)
		{
			text = "GM: " + activeChar.getName() + " (" + activeChar.getObjectId() + ") issued shutdown command.";
		}

		startShutdown(text, seconds, restart);
	}

	/**
	 * This functions starts a shutdown countdown
	 *
	 * @param seconds seconds until shutdown
	 * @param restart true if the server will restart after shutdown
	 */
	public void startShutdown(String text, int seconds, boolean restart)
	{
		if (restart)
		{
			_shutdownMode = GM_RESTART;
		}
		else
		{
			_shutdownMode = GM_SHUTDOWN;
		}

		if (text != null)
		{
			Log.info(text);
		}
		Log.info(MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds!");

		if (_shutdownMode > 0)
		{
			switch (seconds)
			{
				case 540:
				case 480:
				case 420:
				case 360:
				case 300:
				case 240:
				case 180:
				case 120:
				case 60:
				case 30:
				case 10:
				case 5:
				case 4:
				case 3:
				case 2:
				case 1:
					break;
				default:
					sendServerQuit(seconds, restart);
			}
		}

		if (_task != null)
		{
			_task.abort();
		}

		//		 the main instance should only run for shutdown hook, so we start a new instance
		_task = new ShutdownTask(seconds, restart);
		_task.start();
	}

	/**
	 * This function aborts a running countdown
	 *
	 * @param activeChar GM who issued the abort command
	 */
	public void abort(L2PcInstance activeChar)
	{
		Log.warning("GM: " + activeChar.getName() + " (" + activeChar.getObjectId() + ") issued shutdown ABORT. " +
				MODE_TEXT[_shutdownMode] + " has been stopped!");
		if (_task != null)
		{
			_task.abort();
			Announcements _an = Announcements.getInstance();
			_an.announceToAll("Server aborts " + MODE_TEXT[_shutdownMode] + " and continues normal operation!");
		}
	}

	/**
	 * This function notifies the players and the console log about the coming shutdown
	 *
	 * @param seconds seconds untill shutdown
	 * @param restart true if the server will restart after shutdown
	 */
	private void sendServerQuit(int seconds, boolean restart)
	{
		if (restart)
		{
			Broadcast.toAllOnlinePlayers(new ExShowScreenMessage("Restarting in " + seconds + " seconds", 5000));
			Announcements.getInstance().announceToAll(
					"The server is restarting in " + seconds + " seconds. Find a safe place to log out.");
		}
		else
		{
			SystemMessage sysm =
					SystemMessage.getSystemMessage(SystemMessageId.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS);
			sysm.addNumber(seconds);
			Broadcast.toAllOnlinePlayers(sysm);
		}
	}

	/**
	 * this function is called, when a new thread starts
	 * <p>
	 * if this thread is the thread of getInstance, then this is the shutdown hook
	 * and we save all data and disconnect all clients.
	 * <p>
	 * after this thread ends, the server will completely exit
	 * <p>
	 * if this is not the thread of getInstance, then this is a countdown thread.
	 * we start the countdown, and when we finished it, and it was not aborted,
	 * we tell the shutdown-hook why we call exit, and then call exit
	 * <p>
	 * when the exit status of the server is 1, startServer.sh / startServer.bat
	 * will restart the server.
	 */
	@Override
	public void run()
	{
		_shuttingDown = true;
		try
		{
			if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
			{
				OfflineTradersTable.storeOffliners();
			}
		}
		catch (Throwable t)
		{
			Log.log(Level.WARNING, "Error saving offline shops.", t);
		}

		try
		{
			if (Config.OFFLINE_BUFFERS_ENABLE && Config.OFFLINE_BUFFERS_RESTORE)
			{
				CustomOfflineBuffersManager.getInstance().storeOfflineBuffers();
			}
		}
		catch (Throwable t)
		{
			Log.log(Level.WARNING, "Error saving offline buffers.", t);
		}

		try
		{
			disconnectAllCharacters();
			Log.info("All players disconnected.");
		}
		catch (Throwable t)
		{
			Log.warning("Something went wrong while disconnecting players: " + t.getMessage());
			t.printStackTrace();
		}

		// ensure all services are stopped
		try
		{
			TimeController.getInstance().stopTimer();
		}
		catch (Throwable t)
		{
			Log.warning("Something went wrong while stopping GameTimeController: " + t.getMessage());
			t.printStackTrace();
		}

		// stop all threadpools
		ThreadPoolManager.getInstance().shutdown();

		try
		{
			LoginServerThread.getInstance().interrupt();
		}
		catch (Throwable t)
		{
			Log.warning("Something went wrong while shutting down Login Server connection: " + t.getMessage());
			t.printStackTrace();
		}

		// last byebye, save all data and quit this server
		saveData();

		// saveData sends messages to exit players, so shutdown selector after it
		try
		{
			Server.gameServer.getSelectorThread().shutdown();
		}
		catch (Throwable t)
		{
			Log.warning("Something went wrong while shutting down selector thread: " + t.getMessage());
			t.printStackTrace();
		}

		// commit data, last chance
		try
		{
			L2DatabaseFactory.getInstance().shutdown();
		}
		catch (Throwable t)
		{
			Log.warning("Something went wrong while shutting down DB connection: " + t.getMessage());
			t.printStackTrace();
		}

		// server will quit, when this function ends.
		if (_shutdownMode == GM_RESTART)
		{
			Runtime.getRuntime().halt(2);
		}
		else
		{
			Runtime.getRuntime().halt(0);
		}
	}

	/**
	 * this sends a last byebye, disconnects all players and saves data
	 */
	private void saveData()
	{
		switch (_shutdownMode)
		{
			case SIGTERM:
				Log.info("SIGTERM received. Shutting down NOW!");
				break;
			case GM_SHUTDOWN:
				Log.info("GM shutdown received. Shutting down NOW!");
				break;
			case GM_RESTART:
				Log.info("GM restart received. Restarting NOW!");
				break;
		}

		/*if (Config.ACTIVATE_POSITION_RECORDER)
			Universe.getInstance().implode(true);*/

		SpawnDataManager.getInstance().saveDbSpawnData();
		Log.info("SpawnDataManager: All spawn dynamic data saved");
		GrandBossManager.getInstance().cleanUp();
		Log.info("GrandBossManager: All Grand Boss info saved");
		TradeController.getInstance().dataCountStore();
		Log.info("TradeController: All count Item Saved");
		ItemAuctionManager.getInstance().shutdown();
		Log.info("Item Auctions shut down");
		Olympiad.getInstance().saveOlympiadStatus();
		Log.info("Olympiad System: Data saved");
		HeroesManager.getInstance().shutdown();
		Log.info("Hero System: Data saved");
		ClanTable.getInstance().storeClanScore();
		Log.info("Clan System: Data saved");
		ClanWarManager.getInstance().storeWarData();
		Log.info("Clan War System: Data saved");

		// Save Cursed Weapons data before closing.
		CursedWeaponsManager.getInstance().saveData();
		Log.info("Cursed Weapon data saved");

		// Save all manor data
		CastleManorManager.getInstance().save();
		Log.info("Manor data saved");

		// Save all global (non-player specific) Quest data that needs to persist after reboot
		QuestManager.getInstance().save();
		Log.info("Global Quest data saved");

		// Save all global variables data
		GlobalVariablesManager.getInstance().saveVars();
		Log.info("Global Variables saved");

		//Save items on ground before closing
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().saveInDb();
			ItemsOnGroundManager.getInstance().cleanUp();
			Log.info("ItemsOnGroundManager: All items on ground saved!!");
		}

		if (Config.ENABLE_CUSTOM_DAMAGE_MANAGER)
		{
			DamageManager.getInstance().saveData();
		}

		if (Config.ENABLE_CUSTOM_LOTTERY)
		{
			LotterySystem.getInstance().saveData();
		}
	}

	/**
	 * this disconnects all clients from the server
	 */
	public void disconnectAllCharacters()
	{
		Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers().values();
		//synchronized (L2World.getInstance().getAllPlayers())
		{
			for (L2PcInstance player : pls)
			{
				if (player == null)
				{
					continue;
				}

				// Log out character
				try
				{
					L2GameClient client = player.getClient();
					if (client != null && !client.isDetached())
					{
						client.close(ServerClose.STATIC_PACKET);
						client.setActiveChar(null);
						player.setClient(null);
					}
				}
				catch (Throwable t)
				{
					Log.log(Level.WARNING, "Failed to log out char " + player, t);
				}
			}

			for (L2PcInstance player : pls)
			{
				if (player == null)
				{
					continue;
				}

				// Store character
				try
				{
					player.deleteMe();
				}
				catch (Throwable t)
				{
					Log.log(Level.WARNING, "Failed to store char " + player, t);
				}
			}
		}
	}

	public boolean isShuttingDown()
	{
		return _shuttingDown;
	}

	private class ShutdownTask extends Thread
	{
		private int _secondsShut;

		/**
		 * This creates a countdown instance of Shutdown.
		 *
		 * @param seconds how many seconds until shutdown
		 * @param restart true is the server shall restart after shutdown
		 */
		public ShutdownTask(int seconds, boolean restart)
		{
			if (seconds < 0)
			{
				seconds = 0;
			}
			_secondsShut = seconds;
			if (restart)
			{
				_shutdownMode = GM_RESTART;
			}
			else
			{
				_shutdownMode = GM_SHUTDOWN;
			}
		}

		@Override
		public void run()
		{
			// gm shutdown: send warnings and then call exit to start shutdown sequence
			countdown();
			// last point where logging is operational :(
			Log.warning("GM shutdown countdown is over. " + MODE_TEXT[_shutdownMode] + " NOW!");
			switch (_shutdownMode)
			{
				case GM_SHUTDOWN:
					System.exit(0);
					break;
				case GM_RESTART:
					System.exit(2);
					break;
			}
		}

		/**
		 * set shutdown mode to ABORT
		 */
		private void abort()
		{
			_shutdownMode = ABORT;
		}

		/**
		 * this counts the countdown and reports it to all players
		 * countdown is aborted if mode changes to ABORT
		 */
		private void countdown()
		{
			try
			{
				while (_secondsShut > 0)
				{
					switch (_secondsShut)
					{
						case 540:
							sendServerQuit(540, _shutdownMode == GM_RESTART);
							break;
						case 480:
							sendServerQuit(480, _shutdownMode == GM_RESTART);
							break;
						case 420:
							sendServerQuit(420, _shutdownMode == GM_RESTART);
							break;
						case 360:
							sendServerQuit(360, _shutdownMode == GM_RESTART);
							break;
						case 300:
							sendServerQuit(300, _shutdownMode == GM_RESTART);
							break;
						case 240:
							sendServerQuit(240, _shutdownMode == GM_RESTART);
							break;
						case 180:
							sendServerQuit(180, _shutdownMode == GM_RESTART);
							break;
						case 120:
							sendServerQuit(120, _shutdownMode == GM_RESTART);
							break;
						case 60:
							sendServerQuit(60, _shutdownMode == GM_RESTART);
							break;
						case 30:
							sendServerQuit(30, _shutdownMode == GM_RESTART);
							break;
						case 10:
							sendServerQuit(10, _shutdownMode == GM_RESTART);
							break;
						case 5:
							sendServerQuit(5, _shutdownMode == GM_RESTART);
							break;
						case 4:
							sendServerQuit(4, _shutdownMode == GM_RESTART);
							break;
						case 3:
							sendServerQuit(3, _shutdownMode == GM_RESTART);
							break;
						case 2:
							sendServerQuit(2, _shutdownMode == GM_RESTART);
							break;
						case 1:
							sendServerQuit(1, _shutdownMode == GM_RESTART);
							break;
					}

					_secondsShut--;

					int delay = 1000; //milliseconds
					Thread.sleep(delay);

					if (_shutdownMode == ABORT)
					{
						break;
					}
				}
			}
			catch (InterruptedException e)
			{
				//this will never happen
			}
		}
	}

	/**
	 * get the shutdown-hook instance
	 * the shutdown-hook instance is created by the first call of this function,
	 * but it has to be registrered externaly.
	 *
	 * @return instance of Shutdown, to be used as shutdown hook
	 */
	public static Shutdown getInstance()
	{
		return SingletonHolder._instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final Shutdown _instance = new Shutdown();
	}
}
