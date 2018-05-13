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

import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.CreatureZone;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.zone.SpawnZone;

/**
 * @author UnAfraid
 */
public class ConditionZone extends SpawnZone {
	private boolean NO_ITEM_DROP = false;
	private boolean NO_BOOKMARK = false;

	public ConditionZone(int id) {
		super(id);
	}

	@Override
	public void setParameter(String name, String value) {
		if (name.equalsIgnoreCase("NoBookmark")) {
			NO_BOOKMARK = Boolean.parseBoolean(value);
		} else if (name.equalsIgnoreCase("NoItemDrop")) {
			NO_ITEM_DROP = Boolean.parseBoolean(value);
		} else {
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(Creature character) {
		if (character instanceof Player) {
			if (NO_BOOKMARK) {
				character.setInsideZone(CreatureZone.ZONE_NOBOOKMARK, true);
			}
			if (NO_ITEM_DROP) {
				character.setInsideZone(CreatureZone.ZONE_NOITEMDROP, true);
			}
		}
	}

	@Override
	protected void onExit(Creature character) {

		if (character instanceof Player) {
			if (NO_BOOKMARK) {
				character.setInsideZone(CreatureZone.ZONE_NOBOOKMARK, false);
			}
			if (NO_ITEM_DROP) {
				character.setInsideZone(CreatureZone.ZONE_NOITEMDROP, false);
			}
		}
	}

	@Override
	public void onDieInside(Creature character, Creature killer) {
	}

	@Override
	public void onReviveInside(Creature character) {
	}
}
