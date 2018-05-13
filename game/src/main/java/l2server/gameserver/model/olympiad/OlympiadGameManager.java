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

import l2server.gameserver.datatables.DoorTable;
import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.zone.type.OlympiadStadiumZone;
import l2server.util.loader.annotations.Load;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * @author GodKratos, DS
 */
public class OlympiadGameManager implements Runnable {
	
	private static Logger log = LoggerFactory.getLogger(OlympiadGameManager.class.getName());
	
	
	private volatile boolean battleStarted = false;
	private OlympiadGameTask[] tasks;

	private OlympiadGameManager() {
	}
	
	@Load(dependencies = {Olympiad.class, ZoneManager.class, DoorTable.class})
	public void initialize() {
		final Collection<OlympiadStadiumZone> zones = ZoneManager.getInstance().getAllZones(OlympiadStadiumZone.class);
		if (zones == null || zones.isEmpty()) {
			throw new Error("No olympiad stadium zones defined !");
		}
		
		tasks = new OlympiadGameTask[zones.size() * 40];
		int i = 0;
		for (OlympiadStadiumZone zone : zones) {
			for (int j = 0; j < 40; j++) {
				tasks[j * 4 + i] = new OlympiadGameTask(zone, j * 4 + i);
			}
			i++;
		}
		
		log.info("Loaded " + tasks.length + " stadium instances.");
	}
	
	public static OlympiadGameManager getInstance() {
		return SingletonHolder.instance;
	}

	protected final boolean isBattleStarted() {
		return battleStarted;
	}

	protected final void startBattle() {
		battleStarted = true;
	}

	@Override
	public final void run() {
		if (Olympiad.getInstance().inCompPeriod()) {
			OlympiadGameTask task;
			AbstractOlympiadGame newGame;

			List<List<Integer>> readyClassed = OlympiadManager.getInstance().hasEnoughRegisteredClassed();
			boolean readyNonClassed = OlympiadManager.getInstance().hasEnoughRegisteredNonClassed();

			if (readyClassed == null) {
				for (List<Integer> list : OlympiadManager.getInstance().getRegisteredClassBased().values()) {
					for (int objId : list) {
						Player player = World.getInstance().getPlayer(objId);
						if (player != null) {
							player.sendMessage("Your match may not begin yet because there are not enough participants registered.");
						}
					}
				}
			}
			if (!readyNonClassed) {
				for (int objId : OlympiadManager.getInstance().getRegisteredNonClassBased()) {
					Player player = World.getInstance().getPlayer(objId);
					if (player != null) {
						player.sendMessage("Your match may not begin yet because there are not enough participants registered.");
					}
				}
			}
			if (readyClassed != null || readyNonClassed) {
				// set up the games queue
				for (int i = 0; i < tasks.length; i++) {
					task = tasks[i];
					synchronized (task) {
						if (!task.isRunning()) {
							// WTF was this "fair arena distribution"? Commenting out...
							if (readyClassed != null/* && (i % 2) == 0*/) {
								newGame = OlympiadGameClassed.createGame(i, readyClassed);
								if (newGame != null) {
									task.attachGame(newGame);
									continue;
								} else {
									readyClassed = null;
								}
							}
							if (readyNonClassed) {
								newGame = OlympiadGameNonClassed.createGame(i, OlympiadManager.getInstance().getRegisteredNonClassBased());
								if (newGame != null) {
									task.attachGame(newGame);
									continue;
								} else {
									readyNonClassed = false;
								}
							}
						}
					}

					// stop generating games if no more participants
					if (readyClassed == null && !readyNonClassed) {
						break;
					}
				}
			}
		} else if (isAllTasksFinished() && battleStarted) {
			OlympiadManager.getInstance().clearRegistered();
			battleStarted = false;
			log.info("All current games finished.");
		}
	}

	public final boolean isAllTasksFinished() {
		for (OlympiadGameTask task : tasks) {
			if (task.isRunning()) {
				return false;
			}
		}
		return true;
	}

	public final OlympiadGameTask getOlympiadTask(int id) {
		if (id < 0 || id >= tasks.length) {
			return null;
		}

		return tasks[id];
	}

	public final int getNumberOfStadiums() {
		return tasks.length;
	}

	public final void notifyCompetitorDamage(Player player, int damage) {
		if (player == null) {
			return;
		}

		final int id = player.getOlympiadGameId();
		if (id < 0 || id >= tasks.length) {
			return;
		}

		final AbstractOlympiadGame game = tasks[id].getGame();
		if (game != null) {
			game.addDamage(player, damage);
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final OlympiadGameManager instance = new OlympiadGameManager();
	}
}
