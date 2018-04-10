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

package l2server.gameserver.model.quest;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;

public class QuestTimer {
	private static Logger log = LoggerFactory.getLogger(QuestTimer.class.getName());

	// =========================================================
	// Schedule Task
	public class ScheduleTimerTask implements Runnable {
		@Override
		public void run() {
			if (!getIsActive()) {
				return;
			}

			try {
				if (!getIsRepeating()) {
					cancel();
				}
				getQuest().notifyEvent(getName(), getNpc(), getPlayer());
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	// =========================================================
	// Data Field
	private boolean isActive = true;
	private String name;
	private Quest quest;
	private Npc npc;
	private Player player;
	private boolean isRepeating;
	private ScheduledFuture<?> schedular;

	// =========================================================
	// Constructor
	public QuestTimer(Quest quest, String name, long time, Npc npc, Player player, boolean repeating) {
		this.name = name;
		this.quest = quest;
		this.player = player;
		this.npc = npc;
		isRepeating = repeating;
		if (repeating) {
			schedular = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ScheduleTimerTask(), time, time); // Prepare auto end task
		} else {
			schedular = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), time); // Prepare auto end task
		}
	}

	public QuestTimer(Quest quest, String name, long time, Npc npc, Player player) {
		this(quest, name, time, npc, player, false);
	}

	public QuestTimer(QuestState qs, String name, long time) {
		this(qs.getQuest(), name, time, null, qs.getPlayer(), false);
	}

	// =========================================================
	// Method - Public
	public void cancel() {
		isActive = false;

		if (schedular != null) {
			schedular.cancel(false);
		}

		getQuest().removeQuestTimer(this);
	}

	/**
	 * public method to compare if this timer matches with the key attributes passed.
	 *
	 * @param quest  : Quest instance to which the timer is attached
	 * @param name   : Name of the timer
	 * @param npc    : Npc instance attached to the desired timer (null if no npc attached)
	 * @param player : Player instance attached to the desired timer (null if no player attached)
	 */
	public boolean isMatch(Quest quest, String name, Npc npc, Player player) {
		if (quest == null || name == null) {
			return false;
		}
		if (quest != getQuest() || name.compareToIgnoreCase(getName()) != 0) {
			return false;
		}
		return npc == getNpc() && player == getPlayer();
	}

	// =========================================================
	// Property - Public
	public final boolean getIsActive() {
		return isActive;
	}

	public final boolean getIsRepeating() {
		return isRepeating;
	}

	public final Quest getQuest() {
		return quest;
	}

	public final String getName() {
		return name;
	}

	public final Npc getNpc() {
		return npc;
	}

	public final Player getPlayer() {
		return player;
	}

	@Override
	public final String toString() {
		return name;
	}
}
