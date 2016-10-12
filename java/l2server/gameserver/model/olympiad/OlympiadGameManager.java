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

package l2server.gameserver.model.olympiad;

import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.zone.type.L2OlympiadStadiumZone;
import l2server.log.Log;

import java.util.Collection;
import java.util.List;

/**
 * @author GodKratos, DS
 */
public class OlympiadGameManager implements Runnable
{

	private volatile boolean _battleStarted = false;
	private final OlympiadGameTask[] _tasks;

	private OlympiadGameManager()
	{
		final Collection<L2OlympiadStadiumZone> zones =
				ZoneManager.getInstance().getAllZones(L2OlympiadStadiumZone.class);
		if (zones == null || zones.isEmpty())
		{
			throw new Error("No olympiad stadium zones defined !");
		}

		_tasks = new OlympiadGameTask[zones.size() * 40];
		int i = 0;
		for (L2OlympiadStadiumZone zone : zones)
		{
			for (int j = 0; j < 40; j++)
			{
				_tasks[j * 4 + i] = new OlympiadGameTask(zone, j * 4 + i);
			}
			i++;
		}

		Log.info("Olympiad System: Loaded " + _tasks.length + " stadium instances.");
	}

	public static OlympiadGameManager getInstance()
	{
		return SingletonHolder._instance;
	}

	protected final boolean isBattleStarted()
	{
		return _battleStarted;
	}

	protected final void startBattle()
	{
		_battleStarted = true;
	}

	@Override
	public final void run()
	{
		if (Olympiad.getInstance().inCompPeriod())
		{
			OlympiadGameTask task;
			AbstractOlympiadGame newGame;

			List<List<Integer>> readyClassed = OlympiadManager.getInstance().hasEnoughRegisteredClassed();
			boolean readyNonClassed = OlympiadManager.getInstance().hasEnoughRegisteredNonClassed();

			if (readyClassed == null)
			{
				for (List<Integer> list : OlympiadManager.getInstance().getRegisteredClassBased().values())
				{
					for (int objId : list)
					{
						L2PcInstance player = L2World.getInstance().getPlayer(objId);
						if (player != null)
						{
							player.sendMessage(
									"Your match may not begin yet because there are not enough participants registered.");
						}
					}
				}
			}
			if (!readyNonClassed)
			{
				for (int objId : OlympiadManager.getInstance().getRegisteredNonClassBased())
				{
					L2PcInstance player = L2World.getInstance().getPlayer(objId);
					if (player != null)
					{
						player.sendMessage(
								"Your match may not begin yet because there are not enough participants registered.");
					}
				}
			}
			if (readyClassed != null || readyNonClassed)
			{
				// set up the games queue
				for (int i = 0; i < _tasks.length; i++)
				{
					task = _tasks[i];
					synchronized (task)
					{
						if (!task.isRunning())
						{
							// WTF was this "fair arena distribution"? Commenting out...
							if (readyClassed != null/* && (i % 2) == 0*/)
							{
								newGame = OlympiadGameClassed.createGame(i, readyClassed);
								if (newGame != null)
								{
									task.attachGame(newGame);
									continue;
								}
								else
								{
									readyClassed = null;
								}
							}
							if (readyNonClassed)
							{
								newGame = OlympiadGameNonClassed
										.createGame(i, OlympiadManager.getInstance().getRegisteredNonClassBased());
								if (newGame != null)
								{
									task.attachGame(newGame);
									continue;
								}
								else
								{
									readyNonClassed = false;
								}
							}
						}
					}

					// stop generating games if no more participants
					if (readyClassed == null && !readyNonClassed)
					{
						break;
					}
				}
			}
		}
		else if (isAllTasksFinished() && _battleStarted)
		{
			OlympiadManager.getInstance().clearRegistered();
			_battleStarted = false;
			Log.info("Olympiad System: All current games finished.");
		}
	}

	public final boolean isAllTasksFinished()
	{
		for (OlympiadGameTask task : _tasks)
		{
			if (task.isRunning())
			{
				return false;
			}
		}
		return true;
	}

	public final OlympiadGameTask getOlympiadTask(int id)
	{
		if (id < 0 || id >= _tasks.length)
		{
			return null;
		}

		return _tasks[id];
	}

	public final int getNumberOfStadiums()
	{
		return _tasks.length;
	}

	public final void notifyCompetitorDamage(L2PcInstance player, int damage)
	{
		if (player == null)
		{
			return;
		}

		final int id = player.getOlympiadGameId();
		if (id < 0 || id >= _tasks.length)
		{
			return;
		}

		final AbstractOlympiadGame game = _tasks[id].getGame();
		if (game != null)
		{
			game.addDamage(player, damage);
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final OlympiadGameManager _instance = new OlympiadGameManager();
	}
}
