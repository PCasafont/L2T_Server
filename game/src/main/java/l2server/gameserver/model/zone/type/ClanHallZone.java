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
import l2server.gameserver.instancemanager.ClanHallManager;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.entity.ClanHall;
import l2server.gameserver.model.zone.SpawnZone;
import l2server.gameserver.network.serverpackets.AgitDecoInfo;

/**
 * A clan hall zone
 *
 * @author durgus
 */
public class ClanHallZone extends SpawnZone {
	private int clanHallId;

	public ClanHallZone(int id) {
		super(id);
	}

	@Override
	public void setParameter(String name, String value) {
		if (name.equals("clanHallId")) {
			clanHallId = Integer.parseInt(value);
			// Register self to the correct clan hall
			ClanHall ch = ClanHallManager.getInstance().getClanHallById(clanHallId);
			if (ch != null) {
				ch.setZone(this);
			}
		} else {
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(Creature character) {
		if (character instanceof Player) {
			// Set as in clan hall
			character.setInsideZone(Creature.ZONE_CLANHALL, true);

			ClanHall clanHall = ClanHallManager.getInstance().getClanHallById(clanHallId);
			if (clanHall == null) {
				return;
			}

			// Send decoration packet
			AgitDecoInfo deco = new AgitDecoInfo(clanHall);
			character.sendPacket(deco);
		} else if (character instanceof Attackable && ((Attackable) character).getMostHated() != null) {
			((Attackable) character).escape("Do you want to kidnap me in this dirty clan hall? No, thanks :)");
			((Attackable) character).getMostHated().reduceCurrentHp(100000, character, null);
		}
	}

	@Override
	protected void onExit(Creature character) {
		if (character instanceof Player) {
			// Unset clanhall zone
			character.setInsideZone(Creature.ZONE_CLANHALL, false);
		}
	}

	@Override
	public void onDieInside(Creature character, Creature killer) {
	}

	@Override
	public void onReviveInside(Creature character) {
	}

	/**
	 * Removes all foreigners from the clan hall
	 *
	 */
	public void banishForeigners(int owningClanId) {
		for (Creature temp : characterList.values()) {
			if (!(temp instanceof Player)) {
				continue;
			}
			if (((Player) temp).getClanId() == owningClanId) {
				continue;
			}

			temp.teleToLocation(MapRegionTable.TeleportWhereType.Town);
		}
	}

	/**
	 * @return the clanHallId
	 */
	public int getClanHallId() {
		return clanHallId;
	}
}
