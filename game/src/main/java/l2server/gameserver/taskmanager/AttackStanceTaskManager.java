/*
 * $HeadURL: $
 *
 * $Author: $ $Date: $ $Revision: $
 *
 *
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
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.CubicInstance;
import l2server.gameserver.model.actor.instance.MobSummonInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.AutoAttackStop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class ...
 *
 * @author Luca Baldi
 * @version $Revision: $ $Date: $
 */
public class AttackStanceTaskManager {
	private static Logger log = LoggerFactory.getLogger(AttackStanceTaskManager.class.getName());

	protected Map<Creature, Long> attackStanceTasks = new ConcurrentHashMap<>();

	private AttackStanceTaskManager() {
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FightModeScheduler(), 0, 1000);
	}

	public static AttackStanceTaskManager getInstance() {
		return SingletonHolder.instance;
	}

	public void addAttackStanceTask(Creature actor) {
		if (actor instanceof Summon) {
			Summon summon = (Summon) actor;
			actor = summon.getOwner();
		}
		if (actor instanceof Player) {
			Player player = (Player) actor;
			player.setFightStanceTime(System.currentTimeMillis());
			player.onCombatStanceStart();
			player.getCubics().values().stream().filter(cubic -> cubic.getId() != CubicInstance.LIFE_CUBIC).forEach(CubicInstance::doAction);

			player.getSummons().stream().filter(summon -> summon instanceof MobSummonInstance).forEach(summon -> summon.unSummon(player));
		}
		attackStanceTasks.put(actor, System.currentTimeMillis());
	}

	public void removeAttackStanceTask(Creature actor) {
		if (actor instanceof Summon) {
			Summon summon = (Summon) actor;
			actor = summon.getOwner();
		}
		if (actor instanceof Player) {
			((Player) actor).onCombatStanceEnd();
		}

		attackStanceTasks.remove(actor);
	}

	public boolean getAttackStanceTask(Creature actor) {
		if (actor instanceof Summon) {
			Summon summon = (Summon) actor;
			actor = summon.getOwner();
		}

		return attackStanceTasks.containsKey(actor);
	}

	private class FightModeScheduler implements Runnable {
		protected FightModeScheduler() {
			// Do nothing
		}

		@Override
		public void run() {
			Long current = System.currentTimeMillis();
			try {
				if (attackStanceTasks != null) {
					synchronized (this) {
						for (Entry<Creature, Long> entry : attackStanceTasks.entrySet()) {
							Creature actor = entry.getKey();
							if (current - entry.getValue() > 15000) {
								actor.broadcastPacket(new AutoAttackStop(actor.getObjectId()));
								if (actor instanceof Player) {
									if (((Player) actor).getPet() != null) {
										((Player) actor).getPet()
												.broadcastPacket(new AutoAttackStop(((Player) actor).getPet().getObjectId()));
									}

									if (((Player) actor).getSummons() != null) {
										((Player) actor).getSummons()
												.stream()
												.filter(summon -> summon != null)
												.forEach(summon -> summon.broadcastPacket(new AutoAttackStop(summon.getObjectId())));
									}
								}
								actor.getAI().setAutoAttacking(false);
								removeAttackStanceTask(actor);
							}
						}
					}
				}
			} catch (Exception e) {
				log.warn("Error in FightModeScheduler: " + e.getMessage(), e);
			}
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final AttackStanceTaskManager instance = new AttackStanceTaskManager();
	}
}
