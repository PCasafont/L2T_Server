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

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.NpcInstance;
import l2server.gameserver.model.actor.instance.Player;

import java.util.Collection;

public class AttackableKnownList extends NpcKnownList {
	public AttackableKnownList(Attackable activeChar) {
		super(activeChar);
	}

	@Override
	protected boolean removeKnownObject(WorldObject object, boolean forget) {
		if (!super.removeKnownObject(object, forget)) {
			return false;
		}

		// Remove the WorldObject from the aggrolist of the Attackable
		if (object instanceof Creature) {
			getActiveChar().getAggroList().remove(object);
		}
		// Set the Attackable Intention to AI_INTENTION_IDLE
		final Collection<Player> known = getKnownPlayers().values();

		//FIXME: This is a temporary solution && support for Walking Manager
		if (getActiveChar().hasAI() && (known == null || known.isEmpty())) {
			getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
		}

		return true;
	}

	@Override
	public Attackable getActiveChar() {
		return (Attackable) super.getActiveChar();
	}

	@Override
	public int getDistanceToForgetObject(WorldObject object) {
		if (getActiveChar().getAggroList().get(object) != null) {
			return 3000;
		}

		return getDistanceToWatchObject(object) + 200;
	}

	@Override
	public int getDistanceToWatchObject(WorldObject object) {
		if (object instanceof NpcInstance && (((NpcInstance) object).getClan() == null ||
				!((NpcInstance) object).getClan().equalsIgnoreCase(getActiveChar().getEnemyClan())) || !(object instanceof Creature)) {
			return 0;
		}

		if (object instanceof Playable) {
			return object.getKnownList().getDistanceToWatchObject(getActiveObject());
		}

		return Math.max(300, Math.max(getActiveChar().getAggroRange(), Math.max(getActiveChar().getFactionRange(), getActiveChar().getEnemyRange())));
	}
}
