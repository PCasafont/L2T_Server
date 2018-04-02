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

import l2server.gameserver.ai.CreatureAI;
import l2server.gameserver.ai.CtrlEvent;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.RaidBossInstance;

public class MonsterKnownList extends AttackableKnownList {
	public MonsterKnownList(MonsterInstance activeChar) {
		super(activeChar);
	}

	@Override
	public boolean addKnownObject(WorldObject object) {
		if (!super.addKnownObject(object)) {
			return false;
		}

		if (object instanceof Player) {
			Player player = (Player) object;
			if (!player.isGM()) {
				if (player.getPvpFlag() > 0 && getActiveChar() instanceof RaidBossInstance && player.getLevel() > getActiveChar().getLevel() + 8 &&
						getActiveChar().isInsideRadius(object, 500, true, true)) {
					Skill tempSkill = SkillTable.getInstance().getInfo(4515, 1);
					if (tempSkill != null) {
						tempSkill.getEffects(getActiveChar(), player);
					}
				}
			}
		}

		final CreatureAI ai = getActiveChar().getAI(); // force AI creation

		// Set the MonsterInstance Intention to AI_INTENTION_ACTIVE if the state was AI_INTENTION_IDLE
		if (object instanceof Player && ai != null && ai.getIntention() == CtrlIntention.AI_INTENTION_IDLE) {
			ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
		}

		return true;
	}

	@Override
	protected boolean removeKnownObject(WorldObject object, boolean forget) {
		if (!super.removeKnownObject(object, forget)) {
			return false;
		}

		if (!(object instanceof Creature)) {
			return true;
		}

		if (getActiveChar().hasAI()) {
			// Notify the MonsterInstance AI with EVT_FORGET_OBJECT
			getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
		}

		if (getActiveChar().isVisible() && getKnownPlayers().isEmpty() && getKnownSummons().isEmpty()) {
			// Clear the aggroList of the MonsterInstance
			getActiveChar().clearAggroList();

			// Remove all WorldObject from knownObjects and knownPlayer of the MonsterInstance then cancel Attak or Cast and notify AI
			//removeAllKnownObjects();
		}

		return true;
	}

	@Override
	public final MonsterInstance getActiveChar() {
		return (MonsterInstance) super.getActiveChar();
	}
}
