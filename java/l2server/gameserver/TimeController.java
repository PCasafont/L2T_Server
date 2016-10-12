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
import l2server.gameserver.ai.CtrlEvent;
import l2server.gameserver.instancemanager.DayNightSpawnManager;
import l2server.gameserver.model.actor.L2Character;
import l2server.log.Log;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Removed TimerThread watcher [DrHouse]
 *
 * @version $Date: 2010/02/02 22:43:00 $
 */
public class TimeController
{
	public static final int TICKS_PER_SECOND = 10; // not able to change this without checking through code
	public static final int MILLIS_IN_TICK = 1000 / TICKS_PER_SECOND;
	public static final int IG_DAYS_PER_DAY = 6;
	public static final int MILLIS_PER_IG_DAY = 1000 * 60 * 60 * 24 / IG_DAYS_PER_DAY;
	public static final int SECONDS_PER_IG_DAY = MILLIS_PER_IG_DAY / 1000;
	public static final int MINUTES_PER_IG_DAY = SECONDS_PER_IG_DAY / 60;
	public static final int TICKS_PER_IG_DAY = SECONDS_PER_IG_DAY * TICKS_PER_SECOND;
	public static final int TICKS_SUN_STATE_CHANGE = TICKS_PER_IG_DAY / 4;

	protected static int _gameTicks;
	protected static long _gameStartTime;
	protected static boolean _isNight = false;
	protected static boolean _interruptRequest = false;

	private static final ConcurrentHashMap<Integer, L2Character> _movingObjects = new ConcurrentHashMap<>();

	protected static TimerThread _timer;

	/**
	 * one ingame day is 240 real minutes
	 */
	public static TimeController getInstance()
	{
		return SingletonHolder._instance;
	}

	private TimeController()
	{
		_gameStartTime = System.currentTimeMillis() - 3600000; // offset so that the server starts a day begin
		_gameTicks = 3600000 / MILLIS_IN_TICK; // offset so that the server starts a day begin

		_timer = new TimerThread();
		_timer.start();

		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new BroadcastSunState(), 0, 600000);
	}

	public boolean isNowNight()
	{
		return _isNight;
	}

	public int getGameTime()
	{
		return _gameTicks / (TICKS_PER_SECOND * 10);
	}

	public static int getGameTicks()
	{
		return _gameTicks;
	}

	/**
	 * Add a L2Character to movingObjects of GameTimeController.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All L2Character in movement are identified in <B>movingObjects</B> of GameTimeController.<BR><BR>
	 *
	 * @param cha The L2Character to add to movingObjects of GameTimeController
	 */
	public void registerMovingObject(L2Character cha)
	{
		if (cha == null)
		{
			return;
		}

		_movingObjects.putIfAbsent(cha.getObjectId(), cha);
	}

	/**
	 * Move all L2Characters contained in movingObjects of GameTimeController.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All L2Character in movement are identified in <B>movingObjects</B> of GameTimeController.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Update the position of each L2Character </li>
	 * <li>If movement is finished, the L2Character is removed from movingObjects </li>
	 * <li>Create a task to update the _knownObject and _knowPlayers of each L2Character that finished its movement and of their already known L2Object then notify AI with EVT_ARRIVED </li><BR><BR>
	 */
	protected void moveObjects()
	{
		// Go throw the table containing L2Character in movement
		Iterator<Map.Entry<Integer, L2Character>> it = _movingObjects.entrySet().iterator();
		while (it.hasNext())
		{
			// If movement is finished, the L2Character is removed from
			// movingObjects and added to the ArrayList ended
			L2Character ch = it.next().getValue();
			if (ch.updatePosition(_gameTicks))
			{
				it.remove();
				ThreadPoolManager.getInstance().executeTask(new MovingObjectArrived(ch));
			}
		}
	}

	public void stopTimer()
	{
		_interruptRequest = true;
		_timer.interrupt();
	}

	class TimerThread extends Thread
	{
		public TimerThread()
		{
			super("GameTimeController");
			setDaemon(true);
			setPriority(MAX_PRIORITY);
		}

		@Override
		public void run()
		{
			int oldTicks;
			long runtime;
			int sleepTime;

			for (; ; )
			{
				try
				{
					oldTicks = _gameTicks; // save old ticks value to avoid moving objects 2x in same tick
					runtime = System.currentTimeMillis() - _gameStartTime; // from server boot to now

					_gameTicks = (int) (runtime / MILLIS_IN_TICK); // new ticks value (ticks now)

					if (oldTicks != _gameTicks)
					{
						moveObjects(); // Runs possibly too often
					}

					runtime = System.currentTimeMillis() - _gameStartTime - runtime;

					// calculate sleep time... time needed to next tick minus time it takes to call moveObjects()
					sleepTime = 1 + MILLIS_IN_TICK - (int) runtime % MILLIS_IN_TICK;

					//Logozo.finest("TICK: "+_gameTicks);

					if (sleepTime > 0)
					{
						Thread.sleep(sleepTime);
					}
				}
				catch (InterruptedException ie)
				{
					if (_interruptRequest)
					{
						return;
					}

					Log.log(Level.WARNING, "", ie);
				}
				catch (Exception e)
				{
					Log.log(Level.WARNING, "", e);
				}
			}
		}
	}

	/**
	 * Update the _knownObject and _knowPlayers of each L2Character that finished its movement and of their already known L2Object then notify AI with EVT_ARRIVED.<BR><BR>
	 */
	private static class MovingObjectArrived implements Runnable
	{
		private final L2Character _ended;

		MovingObjectArrived(L2Character ended)
		{
			_ended = ended;
		}

		@Override
		public void run()
		{
			try
			{
				if (_ended.hasAI()) // AI could be just disabled due to region turn off
				{
					if (Config.MOVE_BASED_KNOWNLIST)
					{
						_ended.getKnownList().findObjects();
					}
					_ended.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
				}
			}
			catch (NullPointerException e)
			{
				Log.log(Level.WARNING, "", e);
			}
		}
	}

	/**
	 */
	class BroadcastSunState implements Runnable
	{
		int h;
		boolean tempIsNight;

		@Override
		public void run()
		{
			h = getGameTime() / 60 % 24; // Time in hour
			tempIsNight = h < 6;

			if (tempIsNight != _isNight)
			{ // If diff day/night state
				_isNight = tempIsNight; // Set current day/night varible to value of temp varible
				DayNightSpawnManager.getInstance().notifyChangeMode();
			}
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final TimeController _instance = new TimeController();
	}
}
