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
import l2server.util.Rnd;

import java.util.ArrayList;

/**
 * A castle teleporter zone
 * used for Mass Gatekeepers
 *
 * @author Kerberos
 */
public class CastleTeleportZone extends ZoneType {
	private int[] spawnLoc;
	private int castleId;

	public CastleTeleportZone(int id) {
		super(id);

		spawnLoc = new int[5];
	}

	@Override
	public void setParameter(String name, String value) {
		switch (name) {
			case "castleId":
				castleId = Integer.parseInt(value);
				break;
			case "spawnMinX":
				spawnLoc[0] = Integer.parseInt(value);
				break;
			case "spawnMaxX":
				spawnLoc[1] = Integer.parseInt(value);
				break;
			case "spawnMinY":
				spawnLoc[2] = Integer.parseInt(value);
				break;
			case "spawnMaxY":
				spawnLoc[3] = Integer.parseInt(value);
				break;
			case "spawnZ":
				spawnLoc[4] = Integer.parseInt(value);
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}

	@Override
	protected void onEnter(Creature character) {
		character.setInsideZone(CreatureZone.ZONE_NOSUMMONFRIEND, true);
	}

	@Override
	protected void onExit(Creature character) {
		character.setInsideZone(CreatureZone.ZONE_NOSUMMONFRIEND, false);
	}

	@Override
	public void onDieInside(Creature character, Creature killer) {
	}

	@Override
	public void onReviveInside(Creature character) {
	}

	/**
	 * Returns all players within this zone
	 *
	 */
	public ArrayList<Player> getAllPlayers() {
		ArrayList<Player> players = new ArrayList<>();

		for (Creature temp : characterList.values()) {
			if (temp instanceof Player) {
				players.add((Player) temp);
			}
		}

		return players;
	}

	@Override
	public void oustAllPlayers() {
		if (characterList == null) {
			return;
		}
		if (characterList.isEmpty()) {
			return;
		}
		for (Creature character : characterList.values()) {
			if (character == null) {
				continue;
			}
			if (character instanceof Player) {
				Player player = (Player) character;
				if (player.isOnline()) {
					player.teleToLocation(Rnd.get(spawnLoc[0], spawnLoc[1]), Rnd.get(spawnLoc[2], spawnLoc[3]), spawnLoc[4]);
				}
			}
		}
	}

	public int getCastleId() {
		return castleId;
	}

	/**
	 * Get the spawn locations
	 *
	 */
	public int[] getSpawn() {
		return spawnLoc;
	}
}
