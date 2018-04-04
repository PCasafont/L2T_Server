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

package l2server.gameserver.model.actor.knownlist;

import l2server.gameserver.MonsterRace;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.RaceManagerInstance;
import l2server.gameserver.network.serverpackets.DeleteObject;

public class RaceManagerKnownList extends NpcKnownList {
	// =========================================================
	// Data Field

	// =========================================================
	// Constructor
	public RaceManagerKnownList(RaceManagerInstance activeChar) {
		super(activeChar);
	}

	// =========================================================
	// Method - Public
	@Override
	public boolean addKnownObject(WorldObject object) {
		if (!super.addKnownObject(object)) {
			return false;
		}

		/* DONT KNOW WHY WE NEED THIS WHEN RACE MANAGER HAS A METHOD THAT BROADCAST TO ITS KNOW PLAYERS
		if (object instanceof Player) {
			if (packet != null)
				((Player) object).sendPacket(packet);
		}
		 */

		return true;
	}

	@Override
	protected boolean removeKnownObject(WorldObject object, boolean forget) {
		if (!super.removeKnownObject(object, forget)) {
			return false;
		}

		if (object instanceof Player) {
			//Logozo.info("Sending delete monsrac info.");
			DeleteObject obj = null;
			for (int i = 0; i < 8; i++) {
				obj = new DeleteObject(MonsterRace.getInstance().getMonsters()[i]);
				object.sendPacket(obj);
			}
		}

		return true;
	}

	// =========================================================
	// Method - Private

	// =========================================================
	// Property - Public
	@Override
	public RaceManagerInstance getActiveChar() {
		return (RaceManagerInstance) super.getActiveChar();
	}
}
