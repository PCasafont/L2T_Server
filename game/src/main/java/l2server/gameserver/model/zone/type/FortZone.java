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

import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.CreatureZone;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.zone.SpawnZone;

/**
 * A castle zone
 *
 * @author durgus
 */
public class FortZone extends SpawnZone {
	private int fortId;

	public FortZone(int id) {
		super(id);
	}

	@Override
	public void setParameter(String name, String value) {
		if (name.equals("fortId")) {
			fortId = Integer.parseInt(value);
		} else {
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(Creature character) {
		character.setInsideZone(CreatureZone.ZONE_FORT, true);
	}

	@Override
	protected void onExit(Creature character) {
		character.setInsideZone(CreatureZone.ZONE_FORT, false);
	}

	@Override
	public void onDieInside(Creature character, Creature killer) {

	}

	@Override
	public void onReviveInside(Creature character) {
	}

	public void updateZoneStatusForCharactersInside() {
	}

	/**
	 * Removes all foreigners from the fort
	 *
	 */
	public void banishForeigners(L2Clan owningClan) {
		for (Creature temp : characterList.values()) {
			if (!(temp instanceof Player)) {
				continue;
			}
			if (((Player) temp).getClan() == owningClan) {
				continue;
			}

			temp.teleToLocation(MapRegionTable.TeleportWhereType.Town); // TODO: shouldnt be town, its outside of fort
		}
	}

	public int getFortId() {
		return fortId;
	}
}
