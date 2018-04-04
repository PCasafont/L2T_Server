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
import l2server.gameserver.model.zone.ZoneType;

/**
 * A fishing zone
 *
 * @author durgus
 */
public class FishingZone extends ZoneType {
	public FishingZone(int id) {
		super(id);
	}

	@Override
	protected void onEnter(Creature character) {
	}

	@Override
	protected void onExit(Creature character) {
	}

	@Override
	public void onDieInside(Creature character, Creature killer) {
	}

	@Override
	public void onReviveInside(Creature character) {
	}

	/* getWaterZ() this added function returns the Z value for the water surface.
	 * In effect this simply returns the upper Z value of the zone. This required
	 * some modification of L2ZoneForm, and zone form extentions.
	 */
	public int getWaterZ() {
		return getZone().getHighZ();
	}
}
