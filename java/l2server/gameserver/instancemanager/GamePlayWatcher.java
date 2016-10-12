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

package l2server.gameserver.instancemanager;

import l2server.gameserver.GeoEngine;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.SpecialCamera;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;
import l2server.gameserver.util.Util;
import l2server.util.Point3D;
import l2server.util.Rnd;

import java.util.concurrent.ScheduledFuture;

/**
 * @author Pere
 */
public class GamePlayWatcher
{
	private static GamePlayWatcher _instance;

	public static GamePlayWatcher getInstance()
	{
		if (_instance == null)
		{
			_instance = new GamePlayWatcher();
		}

		return _instance;
	}

	public void makeWatcher(L2PcInstance watcher)
	{
		WatchTask watchTask = new WatchTask(watcher);
		ScheduledFuture<?> schedule =
				ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(watchTask, 1000L, 1000L);
		watchTask.setSchedule(schedule);
	}

	private class WatchTask implements Runnable
	{
		private final L2PcInstance _watcher;
		private L2PcInstance _pivot = null;
		private long _checkForAnotherPivotTimer = 0L;

		private ScheduledFuture<?> _schedule = null;

		public WatchTask(L2PcInstance watcher)
		{
			_watcher = watcher;
		}

		@Override
		public void run()
		{
			if (!_watcher.isOnline() || !_watcher.isInWatcherMode())
			{
				if (_schedule != null)
				{
					_schedule.cancel(false);
				}

				return;
			}

			if (_checkForAnotherPivotTimer < System.currentTimeMillis() || _pivot == null ||
					!AttackStanceTaskManager.getInstance().getAttackStanceTask(_pivot) ||
					_pivot.isInsidePeaceZone(_pivot) || _pivot.isDead())
			{
				_pivot = null;
				_checkForAnotherPivotTimer = System.currentTimeMillis() + 30000L;
			}

			if (_pivot == null)
			{
				int bestCombatPvPCount = 0;
				int bestCombatPvECount = 0;
				for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
				{
					if (!AttackStanceTaskManager.getInstance().getAttackStanceTask(player) ||
							player.isInsidePeaceZone(player) || player.isDead() || player.inObserverMode() ||
							player == _watcher)
					{
						continue;
					}

					int flaggedCount = 0;
					int combatPvPCount = 0;
					int combatPvECount = 0;
					for (L2Character c : player.getKnownList().getKnownCharacters())
					{
						if (Util.checkIfInRange(1000, player, c, false))
						{
							if (c instanceof L2PcInstance)
							{
								if (((L2PcInstance) c).getPvpFlag() > 0)
								{
									flaggedCount++; // That's someone making PvP for sure
								}
								if (AttackStanceTaskManager.getInstance().getAttackStanceTask(c))
								{
									if (c.getTarget() instanceof L2PcInstance)
									{
										combatPvPCount++; // Less valuable than a flagged player but ok
									}
									else
									{
										combatPvECount++; // Target can be null, let's consider it pve
									}
								}
							}
						}
					}

					// FIXME: this ckeck is going to pick farm zones over PvP if lots of farmers get together
					if (flaggedCount > bestCombatPvPCount || combatPvPCount > bestCombatPvPCount ||
							bestCombatPvPCount < 2 && combatPvECount > bestCombatPvECount)
					{
						_pivot = player;
						bestCombatPvPCount = Math.max(combatPvPCount, flaggedCount);
						bestCombatPvECount = combatPvECount;
					}
				}
			}

			//_pivot = L2World.getInstance().getPlayer("pere");

			if (_pivot == null || _watcher.isTeleporting())
			{
				// We stream Gludio when no mini game is going on.
				//if (Config.isServer(Config.FUSION))
				{
					if (!_watcher.isTeleporting())
					{
						if (!_watcher.isInsideRadius(-14504, 123799, 500, false))
						{
							_watcher.teleToLocation(-14504, 123799, -3114);
						}

						_watcher.sendPacket(new SpecialCamera(_watcher.getObjectId(), Rnd.get(50, 150), // Distance
								(int) (System.currentTimeMillis() / 100), // Yaw
								Rnd.get(15, 25), // Pitch
								2000, // Time
								5000, // Duration
								0, // Turn
								0, // Rise
								0, // Cinematic-like
								1 // Relative to Object's angle
						));
					}
				}

				return;
			}

			if (!Util.checkIfInRange(5000, _watcher, _pivot, true))
			{
				_watcher.setInstanceId(_pivot.getInstanceId());
				_watcher.teleToLocation(_pivot.getX(), _pivot.getY(), _pivot.getZ());
				return;
			}

			if (!Util.checkIfInRange(500, _watcher, _pivot, true))
			{
				_watcher.setXYZ(_pivot.getX(), _pivot.getY(), _pivot.getZ());
				_watcher.broadcastUserInfo();
			}

			if (_pivot.getTarget() == null || _pivot.getTarget() == _pivot)
			{
				// 3rd person cam
				_watcher.sendPacket(new SpecialCamera(_pivot.getObjectId(), Rnd.get(50, 150), // Distance
						0, // Yaw
						Rnd.get(15, 25), // Pitch
						2000, // Time
						30000, // Duration
						0, // Turn
						0, // Rise
						0, // Cinematic-like
						1 // Relative to Object's angle
				));
			}
			else
			{
				double yaw = Math.toDegrees(Math.atan2(_pivot.getX() - _pivot.getTarget().getX(),
						_pivot.getY() - _pivot.getTarget().getY())) + 90;
				double angle = 180 - yaw;
				double pitch = 15 + 0.02 * (1000 - Util.calculateDistance(_pivot, _pivot.getTarget(), false));
				if (pitch < 15)
				{
					pitch = 15;
				}
				double distance = 250;
				Point3D cameraPos =
						new Point3D((int) Math.round(_pivot.getX() + distance * Math.cos(angle * Math.PI / 180.0)),
								(int) Math.round(_pivot.getY() + distance * Math.sin(angle * Math.PI / 180.0) *
										Math.cos(pitch * Math.PI / 180.0)),
								(int) Math.round(_pivot.getZ() + distance * Math.sin(pitch * Math.PI / 180.0)));
				while (!GeoEngine.getInstance().canSeeTarget(_pivot, cameraPos))
				{
					distance -= 100;

					if (distance < 50)
					{
						//_pivot = null;
						return;
					}

					cameraPos =
							new Point3D((int) Math.round(_pivot.getX() + distance * Math.cos(angle * Math.PI / 180.0)),
									(int) Math.round(_pivot.getY() + distance * Math.sin(angle * Math.PI / 180.0) *
											Math.cos(pitch * Math.PI / 180.0)),
									(int) Math.round(_pivot.getZ() + distance * Math.sin(pitch * Math.PI / 180.0)));
				}

				// 3rd person cam
				_watcher.sendPacket(new SpecialCamera(_pivot.getObjectId(), Rnd.get(50, 150), // Distance
						(int) Math.round(yaw), // Yaw
						Rnd.get(15, 25), // Pitch
						2000, // Time
						30000, // Duration
						0, // Turn
						0, // Rise
						0, // Cinematic-like
						0 // Relative to Object's angle
				));
			}
		}

		public void setSchedule(ScheduledFuture<?> schedule)
		{
			_schedule = schedule;
		}
	}
}
