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

import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.instance.DefenderInstance;
import l2server.gameserver.model.actor.instance.DoorInstance;
import l2server.gameserver.model.actor.instance.Player;

public class DoorKnownList extends CharKnownList {
	// =========================================================
	// Data Field

	// =========================================================
	// Constructor
	public DoorKnownList(DoorInstance activeChar) {
		super(activeChar);
	}

	// =========================================================
	// Method - Public

	// =========================================================
	// Method - Private

	// =========================================================
	// Property - Public
	@Override
	public final DoorInstance getActiveChar() {
		return (DoorInstance) super.getActiveChar();
	}

	@Override
	public int getDistanceToForgetObject(WorldObject object) {
		if (object instanceof DefenderInstance) {
			return 800;
		}
		if (!(object instanceof Player)) {
			return 0;
		}

		return 4000;
	}

	@Override
	public int getDistanceToWatchObject(WorldObject object) {
		if (object instanceof DefenderInstance) {
			return 600;
		}
		if (!(object instanceof Player)) {
			return 0;
		}
		return 3000;
	}
}
