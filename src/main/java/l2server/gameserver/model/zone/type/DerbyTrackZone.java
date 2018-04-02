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
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.zone.ZoneType;

/**
 * The Monster Derby Track Zone
 *
 * @author durgus
 */
public class DerbyTrackZone extends ZoneType {
	public DerbyTrackZone(int id) {
		super(id);
	}

	@Override
	protected void onEnter(Creature character) {
		if (character instanceof Playable) {
			character.setInsideZone(Creature.ZONE_MONSTERTRACK, true);
			character.setInsideZone(Creature.ZONE_PEACE, true);
			character.setInsideZone(Creature.ZONE_NOSUMMONFRIEND, true);
		}
	}

	@Override
	protected void onExit(Creature character) {
		if (character instanceof Playable) {
			character.setInsideZone(Creature.ZONE_MONSTERTRACK, false);
			character.setInsideZone(Creature.ZONE_PEACE, false);
			character.setInsideZone(Creature.ZONE_NOSUMMONFRIEND, false);
		}
	}

	@Override
	public void onDieInside(Creature character, Creature killer) {
	}

	@Override
	public void onReviveInside(Creature character) {
	}
}
