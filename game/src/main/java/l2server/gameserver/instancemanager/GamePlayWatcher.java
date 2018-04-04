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
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.SpecialCamera;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;
import l2server.gameserver.util.Util;
import l2server.util.Point3D;
import l2server.util.Rnd;

import java.util.concurrent.ScheduledFuture;

/**
 * @author Pere
 */
public class GamePlayWatcher {
	private static GamePlayWatcher instance;

	public static GamePlayWatcher getInstance() {
		if (instance == null) {
			instance = new GamePlayWatcher();
		}

		return instance;
	}

	public void makeWatcher(Player watcher) {
		WatchTask watchTask = new WatchTask(watcher);
		ScheduledFuture<?> schedule = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(watchTask, 1000L, 1000L);
		watchTask.setSchedule(schedule);
	}

	private class WatchTask implements Runnable {
		private final Player watcher;
		private Player pivot = null;
		private long checkForAnotherPivotTimer = 0L;

		private ScheduledFuture<?> schedule = null;

		public WatchTask(Player watcher) {
			this.watcher = watcher;
		}

		@Override
		public void run() {
			if (!watcher.isOnline() || !watcher.isInWatcherMode()) {
				if (schedule != null) {
					schedule.cancel(false);
				}

				return;
			}

			if (checkForAnotherPivotTimer < System.currentTimeMillis() || pivot == null ||
					!AttackStanceTaskManager.getInstance().getAttackStanceTask(pivot) || pivot.isInsidePeaceZone(pivot) || pivot.isDead()) {
				pivot = null;
				checkForAnotherPivotTimer = System.currentTimeMillis() + 30000L;
			}

			if (pivot == null) {
				int bestCombatPvPCount = 0;
				int bestCombatPvECount = 0;
				for (Player player : World.getInstance().getAllPlayersArray()) {
					if (!AttackStanceTaskManager.getInstance().getAttackStanceTask(player) || player.isInsidePeaceZone(player) || player.isDead() ||
							player.inObserverMode() || player == watcher) {
						continue;
					}

					int flaggedCount = 0;
					int combatPvPCount = 0;
					int combatPvECount = 0;
					for (Creature c : player.getKnownList().getKnownCharacters()) {
						if (Util.checkIfInRange(1000, player, c, false)) {
							if (c instanceof Player) {
								if (((Player) c).getPvpFlag() > 0) {
									flaggedCount++; // That's someone making PvP for sure
								}
								if (AttackStanceTaskManager.getInstance().getAttackStanceTask(c)) {
									if (c.getTarget() instanceof Player) {
										combatPvPCount++; // Less valuable than a flagged player but ok
									} else {
										combatPvECount++; // Target can be null, let's consider it pve
									}
								}
							}
						}
					}

					// FIXME: this ckeck is going to pick farm zones over PvP if lots of farmers get together
					if (flaggedCount > bestCombatPvPCount || combatPvPCount > bestCombatPvPCount ||
							bestCombatPvPCount < 2 && combatPvECount > bestCombatPvECount) {
						pivot = player;
						bestCombatPvPCount = Math.max(combatPvPCount, flaggedCount);
						bestCombatPvECount = combatPvECount;
					}
				}
			}

			//pivot = World.getInstance().getPlayer("pere");

			if (pivot == null || watcher.isTeleporting()) {
				// We stream Gludio when no mini game is going on.
				//if (Config.isServer(Config.FUSION))
				{
					if (!watcher.isTeleporting()) {
						if (!watcher.isInsideRadius(-14504, 123799, 500, false)) {
							watcher.teleToLocation(-14504, 123799, -3114);
						}

						watcher.sendPacket(new SpecialCamera(watcher.getObjectId(), Rnd.get(50, 150), // Distance
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

			if (!Util.checkIfInRange(5000, watcher, pivot, true)) {
				watcher.setInstanceId(pivot.getInstanceId());
				watcher.teleToLocation(pivot.getX(), pivot.getY(), pivot.getZ());
				return;
			}

			if (!Util.checkIfInRange(500, watcher, pivot, true)) {
				watcher.setXYZ(pivot.getX(), pivot.getY(), pivot.getZ());
				watcher.broadcastUserInfo();
			}

			if (pivot.getTarget() == null || pivot.getTarget() == pivot) {
				// 3rd person cam
				watcher.sendPacket(new SpecialCamera(pivot.getObjectId(), Rnd.get(50, 150), // Distance
						0, // Yaw
						Rnd.get(15, 25), // Pitch
						2000, // Time
						30000, // Duration
						0, // Turn
						0, // Rise
						0, // Cinematic-like
						1 // Relative to Object's angle
				));
			} else {
				double yaw = Math.toDegrees(Math.atan2(pivot.getX() - pivot.getTarget().getX(), pivot.getY() - pivot.getTarget().getY())) + 90;
				double angle = 180 - yaw;
				double pitch = 15 + 0.02 * (1000 - Util.calculateDistance(pivot, pivot.getTarget(), false));
				if (pitch < 15) {
					pitch = 15;
				}
				double distance = 250;
				Point3D cameraPos = new Point3D((int) Math.round(pivot.getX() + distance * Math.cos(angle * Math.PI / 180.0)),
						(int) Math.round(pivot.getY() + distance * Math.sin(angle * Math.PI / 180.0) * Math.cos(pitch * Math.PI / 180.0)),
						(int) Math.round(pivot.getZ() + distance * Math.sin(pitch * Math.PI / 180.0)));
				while (!GeoEngine.getInstance().canSeeTarget(pivot, cameraPos)) {
					distance -= 100;

					if (distance < 50) {
						//pivot = null;
						return;
					}

					cameraPos = new Point3D((int) Math.round(pivot.getX() + distance * Math.cos(angle * Math.PI / 180.0)),
							(int) Math.round(pivot.getY() + distance * Math.sin(angle * Math.PI / 180.0) * Math.cos(pitch * Math.PI / 180.0)),
							(int) Math.round(pivot.getZ() + distance * Math.sin(pitch * Math.PI / 180.0)));
				}

				// 3rd person cam
				watcher.sendPacket(new SpecialCamera(pivot.getObjectId(), Rnd.get(50, 150), // Distance
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

		public void setSchedule(ScheduledFuture<?> schedule) {
			this.schedule = schedule;
		}
	}
}
