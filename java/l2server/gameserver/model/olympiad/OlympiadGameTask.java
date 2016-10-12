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

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.instance.L2DoorInstance;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.model.zone.type.L2OlympiadStadiumZone;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author DS
 */
public final class OlympiadGameTask implements Runnable
{
	protected static final long BATTLE_PERIOD = Config.ALT_OLY_BATTLE; // 6 mins

	public static final int[] TELEPORT_TO_ARENA = {120, 60, 30, 15, 10, 5, 4, 3, 2, 1, 0};
	public static final int[] BATTLE_START_TIME_FIRST = {60, 50, 40, 30, 20, 10, 0};
	public static final int[] BATTLE_START_TIME_SECOND = {10, 5, 4, 3, 2, 1, 0};
	public static final int[] TELEPORT_TO_TOWN = {40, 30, 20, 10, 5, 4, 3, 2, 1, 0};

	private final L2OlympiadStadiumZone _zone;
	private AbstractOlympiadGame _game;
	private GameState _state = GameState.IDLE;
	private boolean _needAnnounce = false;
	private int _countDown = 0;

	private final List<L2DoorInstance> _doors;
	private final List<L2Spawn> _buffers;

	private enum GameState
	{
		BEGIN,
		TELEPORT_TO_ARENA,
		GAME_STARTED,
		BATTLE_COUNTDOWN_FIRST,
		BATTLE_COUNTDOWN_SECOND,
		BATTLE_STARTED,
		BATTLE_IN_PROGRESS,
		GAME_STOPPED,
		TELEPORT_TO_TOWN,
		CLEANUP,
		IDLE
	}

	public OlympiadGameTask(L2OlympiadStadiumZone zone, int id)
	{
		_zone = zone;
		zone.registerTask(this, id);
		InstanceManager.getInstance().createInstance(id + Olympiad.BASE_INSTANCE_ID);
		Instance instance = InstanceManager.getInstance().getInstance(id + Olympiad.BASE_INSTANCE_ID);
		_buffers = new ArrayList<>(2);
		_doors = new ArrayList<>(2);

		StatsSet set = new StatsSet();
		int door1Id = 17100001 + id % 4 * 100;
		int door2Id = 17100002 + id % 4 * 100;
		instance.addDoor(door1Id, set);
		instance.addDoor(door2Id, set);
		_doors.add(instance.getDoor(door1Id));
		_doors.add(instance.getDoor(door2Id));

		try
		{
			L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(36402);
			L2Spawn bufferSpawn = new L2Spawn(tmpl);

			bufferSpawn.setX(zone.getSpawns().get(6).getX());
			bufferSpawn.setY(zone.getSpawns().get(6).getY());
			bufferSpawn.setZ(zone.getSpawns().get(6).getZ());
			bufferSpawn.setHeading(zone.getSpawns().get(6).getHeading());
			bufferSpawn.setInstanceId(id + Olympiad.BASE_INSTANCE_ID);

			bufferSpawn.startRespawn();
			bufferSpawn.doSpawn();

			SpawnTable.getInstance().addNewSpawn(bufferSpawn, false);
			_buffers.add(0, bufferSpawn);

			bufferSpawn = new L2Spawn(tmpl);

			bufferSpawn.setX(zone.getSpawns().get(7).getX());
			bufferSpawn.setY(zone.getSpawns().get(7).getY());
			bufferSpawn.setZ(zone.getSpawns().get(7).getZ());
			bufferSpawn.setHeading(zone.getSpawns().get(7).getHeading());
			bufferSpawn.setInstanceId(id + Olympiad.BASE_INSTANCE_ID);

			bufferSpawn.startRespawn();
			bufferSpawn.doSpawn();

			SpawnTable.getInstance().addNewSpawn(bufferSpawn, false);
			_buffers.add(1, bufferSpawn);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public final boolean isRunning()
	{
		return _state != GameState.IDLE;
	}

	public final boolean isGameStarted()
	{
		return _state.ordinal() >= GameState.GAME_STARTED.ordinal() && _state.ordinal() <= GameState.CLEANUP.ordinal();
	}

	public final boolean isBattleStarted()
	{
		return _state == GameState.BATTLE_IN_PROGRESS;
	}

	public final boolean isBattleFinished()
	{
		return _state == GameState.TELEPORT_TO_TOWN;
	}

	public final boolean needAnnounce()
	{
		if (_needAnnounce)
		{
			_needAnnounce = false;
			return true;
		}
		else
		{
			return false;
		}
	}

	public final L2OlympiadStadiumZone getZone()
	{
		return _zone;
	}

	public final AbstractOlympiadGame getGame()
	{
		return _game;
	}

	public final void attachGame(AbstractOlympiadGame game)
	{
		if (game != null && _state != GameState.IDLE)
		{
			Log.log(Level.WARNING, "Attempt to overwrite non-finished game in state " + _state);
			return;
		}

		_game = game;
		_state = GameState.BEGIN;
		_needAnnounce = false;
		ThreadPoolManager.getInstance().executeTask(this);
	}

	@Override
	public final void run()
	{
		try
		{
			int delay = 1; // schedule next call after 1s
			switch (_state)
			{
				// Game created
				case BEGIN:
				{
					_state = GameState.TELEPORT_TO_ARENA;
					_countDown = Config.ALT_OLY_WAIT_TIME;
					break;
				}
				// Teleport to arena countdown
				case TELEPORT_TO_ARENA:
				{
					if (_countDown > 0)
					{
						SystemMessage sm = SystemMessage
								.getSystemMessage(SystemMessageId.YOU_WILL_ENTER_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S);
						sm.addNumber(_countDown);
						_game.broadcastPacketToParticipants(sm);
					}

					delay = getDelay(TELEPORT_TO_ARENA);
					if (_countDown <= 0)
					{
						_state = GameState.GAME_STARTED;
					}
					break;
				}
				// Game start, port players to arena
				case GAME_STARTED:
				{
					if (!startGame())
					{
						_state = GameState.GAME_STOPPED;
						break;
					}

					_state = GameState.BATTLE_COUNTDOWN_FIRST;
					_countDown = BATTLE_START_TIME_FIRST[0];
					delay = 5;
					break;
				}
				// Battle start countdown, first part (60-10)
				case BATTLE_COUNTDOWN_FIRST:
				{
					if (_countDown > 0)
					{
						SystemMessage sm =
								SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_WILL_START_IN_S1_SECOND_S);
						sm.addNumber(_countDown);
						_game.broadcastPacket(sm, _zone);
					}

					delay = getDelay(BATTLE_START_TIME_FIRST);
					if (_countDown <= 0)
					{
						openingDoors();

						_state = GameState.BATTLE_COUNTDOWN_SECOND;
						_countDown = BATTLE_START_TIME_SECOND[0];
						delay = getDelay(BATTLE_START_TIME_SECOND);
					}

					break;
				}
				// Battle start countdown, second part (10-0)
				case BATTLE_COUNTDOWN_SECOND:
				{
					if (_countDown > 0)
					{
						SystemMessage sm =
								SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_WILL_START_IN_S1_SECOND_S);
						sm.addNumber(_countDown);
						_game.broadcastPacket(sm, _zone);
					}

					delay = getDelay(BATTLE_START_TIME_SECOND);
					if (_countDown <= 0)
					{
						_state = GameState.BATTLE_STARTED;
					}

					break;
				}
				// Beginning of the battle
				case BATTLE_STARTED:
				{
					_countDown = 0;
					_state = GameState.BATTLE_IN_PROGRESS; // set state first, used in zone update
					if (!startBattle())
					{
						_state = GameState.GAME_STOPPED;
					}

					break;
				}
				// Checks during battle
				case BATTLE_IN_PROGRESS:
				{
					_countDown += 1000;
					if (checkBattle() || _countDown > Config.ALT_OLY_BATTLE)
					{
						_state = GameState.GAME_STOPPED;
					}

					break;
				}
				// End of the battle
				case GAME_STOPPED:
				{
					_state = GameState.TELEPORT_TO_TOWN;
					_countDown = TELEPORT_TO_TOWN[0];
					stopGame();
					delay = getDelay(TELEPORT_TO_TOWN);
					break;
				}
				// Teleport to town countdown
				case TELEPORT_TO_TOWN:
				{
					if (_countDown > 0)
					{
						SystemMessage sm =
								SystemMessage.getSystemMessage(SystemMessageId.YOU_WILL_BE_MOVED_TO_TOWN_IN_S1_SECONDS);
						sm.addNumber(_countDown);
						_game.broadcastPacketToParticipants(sm);
					}

					delay = getDelay(TELEPORT_TO_TOWN);
					if (_countDown <= 0)
					{
						_state = GameState.CLEANUP;
					}

					break;
				}
				// Removals
				case CLEANUP:
				{
					cleanupGame();
					_state = GameState.IDLE;
					_game = null;
					return;
				}
			}
			ThreadPoolManager.getInstance().scheduleGeneral(this, delay * 1000);
		}
		catch (Exception e)
		{
			switch (_state)
			{
				case GAME_STOPPED:
				case TELEPORT_TO_TOWN:
				case CLEANUP:
				case IDLE:
				{
					Log.log(Level.WARNING, "Unable to return players back in town, exception: " + e.getMessage());
					_state = GameState.IDLE;
					_game = null;
					return;
				}
			}

			Log.log(Level.WARNING, "Exception in " + _state + ", trying to port players back: " + e.getMessage(), e);
			_state = GameState.GAME_STOPPED;
			ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
		}
	}

	private int getDelay(int[] times)
	{
		int time;
		for (int i = 0; i < times.length - 1; i++)
		{
			time = times[i];
			if (time >= _countDown)
			{
				continue;
			}

			final int delay = _countDown - time;
			_countDown = time;
			return delay;
		}
		// should not happens
		_countDown = -1;
		return 1;
	}

	/**
	 * Second stage: check for defaulted, port players to arena, announce game.
	 * Returns true if no participants defaulted.
	 */
	private boolean startGame()
	{
		try
		{
			// Checking for opponents and teleporting to arena
			if (_game.checkDefaulted())
			{
				return false;
			}

			closeDoors();
			if (_game.needBuffers())
			{
				spawnBuffers();
			}

			if (!_game.portPlayersToArena(_zone.getSpawns()))
			{
				return false;
			}

			_game.removals();
			_needAnnounce = true;
			OlympiadGameManager.getInstance().startBattle(); // inform manager
			return true;
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, e.getMessage(), e);
		}
		return false;
	}

	/**
	 * Third stage: open doors.
	 */
	private void openingDoors()
	{
		try
		{
			_game.resetDamage();
			openDoors();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, e.getMessage(), e);
		}
	}

	/**
	 * Fourth stage: last checks, remove buffers, start competition itself.
	 * Returns true if all participants online and ready on the stadium.
	 */
	private boolean startBattle()
	{
		try
		{
			if (_game.needBuffers())
			{
				deleteBuffers();
			}

			if (_game.checkBattleStatus() && _game.makeCompetitionStart())
			{
				// game successfully started
				_game.broadcastOlympiadInfo(_zone);
				_game.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.STARTS_THE_GAME), _zone);
				_zone.updateZoneStatusForCharactersInside(_game.getGameId());
				return true;
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, e.getMessage(), e);
		}
		return false;
	}

	/**
	 * Fifth stage: battle is running, returns true if winner found.
	 */
	private boolean checkBattle()
	{
		try
		{
			return _game.haveWinner();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, e.getMessage(), e);
		}

		return true;
	}

	/**
	 * Sixth stage: winner's validations
	 */
	private void stopGame()
	{
		try
		{
			_game.validateWinner(_zone);
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, e.getMessage(), e);
		}

		try
		{
			_zone.updateZoneStatusForCharactersInside(_game.getGameId());
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, e.getMessage(), e);
		}

		try
		{
			_game.cleanEffects();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, e.getMessage(), e);
		}
	}

	/**
	 * Seventh stage: game cleanup (port players back, closing doors, etc)
	 */
	private void cleanupGame()
	{
		try
		{
			_game.playersStatusBack();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, e.getMessage(), e);
		}

		try
		{
			_game.portPlayersBack();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, e.getMessage(), e);
		}

		try
		{
			_game.clearPlayers();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, e.getMessage(), e);
		}

		try
		{
			closeDoors();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, e.getMessage(), e);
		}
	}

	public void openDoors()
	{
		for (L2DoorInstance door : _doors)
		{
			if (door != null && !door.getOpen())
			{
				door.openMe();
			}
		}
	}

	public void closeDoors()
	{
		for (L2DoorInstance door : _doors)
		{
			if (door != null && door.getOpen())
			{
				door.closeMe();
			}
		}
	}

	public void spawnBuffers()
	{
		for (L2Spawn spawn : _buffers)
		{
			spawn.startRespawn();
			spawn.doSpawn();
			spawn.stopRespawn();
		}
	}

	public void deleteBuffers()
	{
		for (L2Spawn spawn : _buffers)
		{
			if (spawn.getNpc() != null && spawn.getNpc().isVisible())
			{
				spawn.getNpc().deleteMe();
			}
		}
	}

	public List<L2DoorInstance> getDoors()
	{
		return _doors;
	}

	public List<L2Spawn> getBuffers()
	{
		return _buffers;
	}
}
