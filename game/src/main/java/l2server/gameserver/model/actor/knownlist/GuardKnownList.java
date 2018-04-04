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

import l2server.Config;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.GuardInstance;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.model.actor.instance.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuardKnownList extends AttackableKnownList {
	private static Logger log = LoggerFactory.getLogger(GuardKnownList.class.getName());



	public GuardKnownList(GuardInstance activeChar) {
		super(activeChar);
	}

	@Override
	public boolean addKnownObject(WorldObject object) {
		if (!super.addKnownObject(object)) {
			return false;
		}

		if (object instanceof Player) {
			// Check if the object added is a Player that owns Karma
			if (((Player) object).getReputation() < 0) {
				if (Config.DEBUG) {
					log.debug(getActiveChar().getObjectId() + ": PK " + object.getObjectId() + " entered scan range");
				}

				// Set the GuardInstance Intention to AI_INTENTION_ACTIVE
				if (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE) {
					getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
				}
			}
		} else if (Config.GUARD_ATTACK_AGGRO_MOB && getActiveChar().isInActiveRegion() && object instanceof MonsterInstance) {
			// Check if the object added is an aggressive MonsterInstance
			if (((MonsterInstance) object).isAggressive()) {
				if (Config.DEBUG) {
					log.debug(getActiveChar().getObjectId() + ": Aggressive mob " + object.getObjectId() + " entered scan range");
				}

				// Set the GuardInstance Intention to AI_INTENTION_ACTIVE
				if (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE) {
					getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
				}
			}
		} else if (object instanceof Npc && ((Npc) object).getClan() != null &&
				((Npc) object).getClan().equalsIgnoreCase(getActiveChar().getEnemyClan())) {
			if (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE) {
				getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			}
		}

		return true;
	}

	@Override
	protected boolean removeKnownObject(WorldObject object, boolean forget) {
		if (!super.removeKnownObject(object, forget)) {
			return false;
		}

		// Check if the aggroList of the GuardInstance is Empty
		if (getActiveChar().noTarget()) {
			//removeAllKnownObjects();

			// Set the GuardInstance to AI_INTENTION_IDLE
			if (getActiveChar().hasAI()) {
				getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
			}
		}

		return true;
	}

	@Override
	public final GuardInstance getActiveChar() {
		return (GuardInstance) super.getActiveChar();
	}
}
