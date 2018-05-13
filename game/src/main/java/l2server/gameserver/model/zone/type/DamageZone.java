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

package l2server.gameserver.model.zone.type;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.model.InstanceType;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.zone.ZoneType;

import java.util.Collection;
import java.util.concurrent.Future;

/**
 * A damage zone
 *
 * @author durgus
 */
public class DamageZone extends ZoneType {
	private int damageHPPerSec;
	private int damageMPPerSec;
	private Future<?> task;

	private int castleId;
	private Castle castle;

	private int startTask;
	private int reuseTask;

	private boolean enabled;

	public DamageZone(int id) {
		super(id);

		// Setup default damage
		damageHPPerSec = 200;
		damageMPPerSec = 0;

		// Setup default start / reuse time
		startTask = 10;
		reuseTask = 5000;

		// no castle by default
		castleId = 0;
		castle = null;

		enabled = true;

		setTargetType(InstanceType.L2Playable); // default only playabale
	}

	@Override
	public void setParameter(String name, String value) {
		if (name.equalsIgnoreCase("dmgHPSec")) {
			damageHPPerSec = Integer.parseInt(value);
		} else if (name.equalsIgnoreCase("dmgMPSec")) {
			damageMPPerSec = Integer.parseInt(value);
		} else if (name.equalsIgnoreCase("castleId")) {
			castleId = Integer.parseInt(value);
		} else if (name.equalsIgnoreCase("initialDelay")) {
			startTask = Integer.parseInt(value);
		} else if (name.equalsIgnoreCase("reuse")) {
			reuseTask = Integer.parseInt(value);
		} else if (name.equalsIgnoreCase("enabled")) {
			enabled = Boolean.parseBoolean(value);
		} else {
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(Creature character) {
		if (task == null && (damageHPPerSec != 0 || damageMPPerSec != 0)) {
			Player player = character.getActingPlayer();
			if (getCastle() != null) // Castle zone
			{
				if (!(getCastle().getSiege().getIsInProgress() && player != null && player.getSiegeState() != 2)) // Siege and no defender
				{
					return;
				}
			}
			synchronized (this) {
				if (task == null) {
					task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ApplyDamage(this), startTask, reuseTask);
				}
			}
		}
	}

	@Override
	protected void onExit(Creature character) {
		if (characterList.isEmpty() && task != null) {
			stopTask();
		}
	}

	protected Collection<Creature> getCharacterList() {
		return characterList.values();
	}

	protected int getHPDamagePerSecond() {
		return damageHPPerSec;
	}

	protected int getMPDamagePerSecond() {
		return damageMPPerSec;
	}

	protected void stopTask() {
		if (task != null) {
			task.cancel(false);
			task = null;
		}
	}

	private Castle getCastle() {
		if (castleId > 0 && castle == null) {
			castle = CastleManager.getInstance().getCastleById(castleId);
		}

		return castle;
	}

	class ApplyDamage implements Runnable {
		private final DamageZone dmgZone;
		private final Castle castle;

		ApplyDamage(DamageZone zone) {
			dmgZone = zone;
			castle = zone.getCastle();
		}

		@Override
		public void run() {
			boolean siege = false;

			if (castle != null) {
				siege = castle.getSiege().getIsInProgress();
				// castle zones active only during siege
				if (!siege) {
					dmgZone.stopTask();
					return;
				}
			}

			if (!enabled) {
				return;
			}

			for (Creature temp : dmgZone.getCharacterList()) {
				if (temp != null && !temp.isDead()) {
					if (siege) {
						// during siege defenders not affected
						final Player player = temp.getActingPlayer();
						if (player != null && player.isInSiege() && player.getSiegeState() == 2) {
							continue;
						}
					}

					if (getHPDamagePerSecond() != 0) {
						temp.reduceCurrentHp(dmgZone.getHPDamagePerSecond(), null, null);
					}
					if (getMPDamagePerSecond() != 0) {
						temp.reduceCurrentMp(dmgZone.getMPDamagePerSecond());
					}
				}
			}
		}
	}

	public void setEnabled(boolean state) {
		enabled = state;
	}

	@Override
	public void onDieInside(Creature character, Creature killer) {
	}

	@Override
	public void onReviveInside(Creature character) {
	}
}
