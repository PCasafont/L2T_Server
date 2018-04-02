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
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.zone.ZoneType;

/**
 * A landing zone
 *
 * @author Kerberos
 */
public class LandingZone extends ZoneType {
	public LandingZone(int id) {
		super(id);
	}

	@Override
	protected void onEnter(Creature character) {
		if (character instanceof Player) {
			character.setInsideZone(Creature.ZONE_LANDING, true);
		}
	}

	@Override
	protected void onExit(Creature character) {
		if (character instanceof Player) {
			character.setInsideZone(Creature.ZONE_LANDING, false);
		}
	}

	/**
	 */
	@Override
	public void onDieInside(Creature character, Creature killer) {
	}

	/**
	 * @see ZoneType#onReviveInside(Creature)
	 */
	@Override
	public void onReviveInside(Creature character) {
	}
}
