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
import l2server.gameserver.model.zone.ZoneType;

/**
 * Zone where store is not allowed.
 *
 * @author fordfrog
 */
public class NoStoreZone extends ZoneType {
	public NoStoreZone(final int id) {
		super(id);
	}

	@Override
	protected void onEnter(final Creature character) {
		if (character instanceof Player) {
			character.setInsideZone(CreatureZone.ZONE_NOSTORE, true);
		}
	}

	@Override
	protected void onExit(final Creature character) {
		if (character instanceof Player) {
			character.setInsideZone(CreatureZone.ZONE_NOSTORE, false);
		}
	}

	@Override
	public void onDieInside(final Creature character, Creature killer) {
	}

	@Override
	public void onReviveInside(final Creature character) {
	}
}
