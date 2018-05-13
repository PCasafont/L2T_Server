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

import l2server.Config;
import l2server.gameserver.events.Curfew;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.CreatureZone;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.zone.SpawnZone;
import l2server.gameserver.network.serverpackets.PlaySound;
import l2server.util.Rnd;

/**
 * A Town zone
 *
 * @author durgus
 */
public class TownZone extends SpawnZone {
	private int townId;
	private int taxById;
	private boolean isPeaceZone;

	public TownZone(int id) {
		super(id);

		taxById = 0;

		// Default not peace zone
		isPeaceZone = false;
	}

	@Override
	public void setParameter(String name, String value) {
		switch (name) {
			case "townId":
				townId = Integer.parseInt(value);
				break;
			case "taxById":
				taxById = Integer.parseInt(value);
				break;
			case "isPeaceZone":
				isPeaceZone = Boolean.parseBoolean(value);
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}

	@Override
	protected void onEnter(Creature character) {
		if (character instanceof Player) {
			// PVP possible during siege, now for siege participants only
			// Could also check if this town is in siege, or if any siege is going on
			if (((Player) character).getSiegeState() != 0 && Config.ZONE_TOWN == 1) {
				return;
			}

			//((Player)character).sendMessage("You entered "+townName);

			/*if (Config.isServer(Config.TENKAI) && Curfew.getInstance().getOnlyPeaceTown() == -1 && isInHostileTown((Player)character))
			{
				((Player)character).setHostileZone(true);
				((Player)character).broadcastReputation();
				((Player)character).sendMessage(40063);
			}*/

			//ThreadPoolManager.getInstance().scheduleGeneral(new MusicTask((Player)character), 2000);
		}

		if (isPeaceZone && Config.ZONE_TOWN != 2 &&
				(Curfew.getInstance().getOnlyPeaceTown() == -1 || Curfew.getInstance().getOnlyPeaceTown() == townId)) {
			character.setInsideZone(CreatureZone.ZONE_PEACE, true);
		}

		character.setInsideZone(CreatureZone.ZONE_TOWN, true);
	}

	@Override
	protected void onExit(Creature character) {
		// TODO: there should be no exit if there was possibly no enter
		if (isPeaceZone) {
			character.setInsideZone(CreatureZone.ZONE_PEACE, false);
		}

		character.setInsideZone(CreatureZone.ZONE_TOWN, false);

		// if (character instanceof Player)
		//((Player)character).sendMessage("You left "+townName);

		/*if (character instanceof Player)
		{
			((Player)character).setHostileZone(false);
			((Player)character).broadcastReputation();
		}*/
	}

	@Override
	public void onDieInside(Creature character, Creature killer) {
	}

	@Override
	public void onReviveInside(Creature character) {
	}

	/**
	 * Returns this zones town id (if any)
	 *
	 */
	public int getTownId() {
		return townId;
	}

	/**
	 * Returns this town zones castle id
	 *
	 */
	public final int getTaxById() {
		return taxById;
	}

	public final boolean isPeaceZone() {
		return isPeaceZone;
	}

	@SuppressWarnings("unused")
	private boolean isInHostileTown(Player player) {
		switch (townId) {
			case 7:
				return player.isAtWarWithCastle(1);
			case 8:
				return player.isAtWarWithCastle(2);
			case 9:
				return player.isAtWarWithCastle(3);
			case 10:
				return player.isAtWarWithCastle(4);
			case 12:
				return player.isAtWarWithCastle(5);
			case 15:
				return player.isAtWarWithCastle(6);
			case 13:
				return player.isAtWarWithCastle(7);
			case 14:
				return player.isAtWarWithCastle(8);
			case 17:
				return player.isAtWarWithCastle(9);
			default:
				return false;
		}
	}

	class MusicTask implements Runnable {
		private Player player;

		public MusicTask(Player player) {
			this.player = player;
		}

		@Override
		public void run() {
			int rnd = Rnd.get(4) + 1;
			player.sendPacket(new PlaySound(1, "CC_0" + rnd, 0, 0, 0, 0, 0));
		}
	}
}
