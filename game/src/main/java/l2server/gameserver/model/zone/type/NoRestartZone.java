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
import l2server.gameserver.datatables.MapRegionTable.TeleportWhereType;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.zone.ZoneType;

/**
 * A simple no restart zone
 *
 * @author GKR
 */
public class NoRestartZone extends ZoneType {
	private int restartAllowedTime = 0;
	private boolean enabled = true;

	public NoRestartZone(int id) {
		super(id);
	}

	@Override
	public void setParameter(String name, String value) {
		if (name.equalsIgnoreCase("EnabledByDefault")) {
			enabled = Boolean.parseBoolean(value);
		} else if (name.equalsIgnoreCase("restartAllowedTime")) {
			restartAllowedTime = Integer.parseInt(value);
		} else if (name.equalsIgnoreCase("restartTime")) {
			// Do nothing.
		} else if (name.equalsIgnoreCase("defaultStatus")) {
			// Do nothing.
		} else if (name.equalsIgnoreCase("instanceId")) {
			// Do nothing.
		} else {
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(Creature character) {
		if (!enabled) {
			return;
		}

		if (character instanceof Player) {
			character.setInsideZone(Creature.ZONE_NORESTART, true);
			Player player = (Player) character;

			if (player.getZoneRestartLimitTime() > 0 && player.getZoneRestartLimitTime() < System.currentTimeMillis()) {
				ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(player), 2000);
			}
			player.setZoneRestartLimitTime(0);
		}
	}

	@Override
	protected void onExit(Creature character) {
		if (!enabled) {
			return;
		}

		if (character instanceof Player) {
			character.setInsideZone(Creature.ZONE_NORESTART, false);
		}
	}

	@Override
	public void onDieInside(Creature character, Creature killer) {
		// Do nothing.
	}

	@Override
	public void onReviveInside(Creature character) {
		// Do nothing.
	}

	public int getRestartAllowedTime() {
		return restartAllowedTime;
	}

	public void setRestartAllowedTime(int time) {
		restartAllowedTime = time;
	}

	private static class TeleportTask implements Runnable {
		private final Player player;

		public TeleportTask(Player player) {
			this.player = player;
		}

		@Override
		public void run() {
			player.teleToLocation(TeleportWhereType.Town);
		}
	}
}
