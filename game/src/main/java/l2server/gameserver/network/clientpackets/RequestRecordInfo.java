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

package l2server.gameserver.network.clientpackets;

import l2server.gameserver.TaskPriority;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.SpawnItem;
import l2server.gameserver.network.serverpackets.UserInfo;

import java.util.Collection;

public class RequestRecordInfo extends L2GameClientPacket {
	/**
	 * urgent messages, execute immediatly
	 */
	public TaskPriority getPriority() {
		return TaskPriority.PR_NORMAL;
	}

	@Override
	protected void readImpl() {
		// trigger
	}

	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();

		if (activeChar == null) {
			return;
		}

		activeChar.sendPacket(new UserInfo(activeChar));

		Collection<WorldObject> objs = activeChar.getKnownList().getKnownObjects().values();
		//synchronized (activeChar.getKnownList().getKnownObjects())
		{
			for (WorldObject object : objs) {
				if (object.getPoly().isMorphed() && object.getPoly().getPolyType().equals("item")) {
					activeChar.sendPacket(new SpawnItem(object));
				} else {
					object.sendInfo(activeChar);

					if (object instanceof Creature) {
						// Update the state of the Creature object client
						// side by sending Server->Client packet
						// MoveToPawn/CharMoveToLocation and AutoAttackStart to
						// the Player
						Creature obj = (Creature) object;
						if (obj.getAI() != null) {
							obj.getAI().describeStateToPlayer(activeChar);
						}
					}
				}
			}
		}
	}
}
