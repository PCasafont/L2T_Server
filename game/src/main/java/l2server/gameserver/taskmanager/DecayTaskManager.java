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

package l2server.gameserver.taskmanager;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author la2 Lets drink to code!
 */
public class DecayTaskManager {
	
	private static Logger log = LoggerFactory.getLogger(DecayTaskManager.class.getName());
	
	protected Map<Creature, Long> decayTasks = new ConcurrentHashMap<>();

	public static final int RAID_BOSS_DECAY_TIME = 30000;
	public static final int ATTACKABLE_DECAY_TIME = 8500;

	private DecayTaskManager() {
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new DecayScheduler(), 10000, 1000);
	}

	public static DecayTaskManager getInstance() {
		return SingletonHolder.instance;
	}

	public void addDecayTask(Creature actor) {
		decayTasks.put(actor, System.currentTimeMillis());
	}

	public void addDecayTask(Creature actor, int interval) {
		decayTasks.put(actor, System.currentTimeMillis() + interval);
	}

	public void cancelDecayTask(Creature actor) {
		try {
			decayTasks.remove(actor);
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		}
	}

	private class DecayScheduler implements Runnable {
		protected DecayScheduler() {
			// Do nothing
		}

		@Override
		public void run() {
			long current = System.currentTimeMillis();
			int delay;
			try {
				Iterator<Entry<Creature, Long>> it = decayTasks.entrySet().iterator();
				while (it.hasNext()) {
					Entry<Creature, Long> e = it.next();
					Creature actor = e.getKey();
					Long next = e.getValue();
					if (next == null) {
						continue;
					}
					if (actor.isRaid() && !actor.isRaidMinion()) {
						delay = RAID_BOSS_DECAY_TIME;
					} else if (actor instanceof Attackable && (((Attackable) actor).isSpoil() || ((Attackable) actor).isSeeded())) {
						delay = ATTACKABLE_DECAY_TIME * 2;
					} else {
						delay = ATTACKABLE_DECAY_TIME;
					}
					if (current - next > delay) {
						actor.onDecay();
						it.remove();
					}
				}
			} catch (Exception e) {
				log.warn("Error in DecayScheduler: " + e.getMessage(), e);
			}
		}
	}

	@Override
	public String toString() {
		String ret = "============= DecayTask Manager Report ============\r\n";
		ret += "Tasks count: " + decayTasks.size() + "\r\n";
		ret += "Tasks dump:\r\n";

		Long current = System.currentTimeMillis();
		for (Creature actor : decayTasks.keySet()) {
			ret += "Class/Name: " + actor.getClass().getSimpleName() + "/" + actor.getName() + " decay timer: " + (current - decayTasks.get(actor)) +
					"\r\n";
		}

		return ret;
	}

	/**
	 * <u><b><font color="FF0000">Read only</font></b></u>
	 */
	public Map<Creature, Long> getTasks() {
		return decayTasks;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final DecayTaskManager instance = new DecayTaskManager();
	}
}
