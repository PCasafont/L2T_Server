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
import l2server.gameserver.model.actor.Trap;

public class TrapKnownList extends CharKnownList {
	public TrapKnownList(Trap activeChar) {
		super(activeChar);
	}

	@Override
	public final Trap getActiveChar() {
		return (Trap) super.getActiveChar();
	}

	@Override
	public int getDistanceToForgetObject(WorldObject object) {
		if (object == getActiveChar().getOwner() || object == getActiveChar().getTarget()) {
			return 6000;
		}

		return 3000;
	}

	@Override
	public int getDistanceToWatchObject(WorldObject object) {
		return 1500;
	}
}
