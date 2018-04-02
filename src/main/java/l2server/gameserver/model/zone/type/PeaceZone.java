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

import l2server.gameserver.instancemanager.GMEventManager;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.zone.ZoneType;

/**
 * A peaceful zone
 *
 * @author durgus
 */
public class PeaceZone extends ZoneType {
	boolean enabled;

	public PeaceZone(int id) {
		super(id);

		enabled = true;
	}

	@Override
	protected void onEnter(Creature character) {
		if (!enabled) {
			return;
		}

		if (!GMEventManager.getInstance().onEnterZone(character, this)) {
			return;
		}

		character.setInsideZone(Creature.ZONE_PEACE, true);
	}

	@Override
	protected void onExit(Creature character) {
		character.setInsideZone(Creature.ZONE_PEACE, false);
	}

	@Override
	public void onDieInside(Creature character, Creature killer) {
	}

	@Override
	public void onReviveInside(Creature character) {
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setZoneEnabled(boolean val) {
		enabled = val;

		for (Creature chara : getCharactersInside().values()) {
			if (chara == null) {
				continue;
			}

			if (enabled) {
				onEnter(chara);
			} else {
				onExit(chara);
			}
		}
	}
}
