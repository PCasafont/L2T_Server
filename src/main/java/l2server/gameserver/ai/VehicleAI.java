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

package l2server.gameserver.ai;

import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Vehicle;

public abstract class VehicleAI extends CreatureAI {
	/**
	 * @author DS
	 * Simple AI for vehicles
	 */
	public VehicleAI(Vehicle creature) {
		super(creature);
	}

	@Override
	protected void onIntentionAttack(Creature target) {
	}

	@Override
	protected void onIntentionCast(Skill skill, WorldObject target) {
	}

	@Override
	protected void onIntentionFollow(Creature target) {
	}

	@Override
	protected void onIntentionPickUp(WorldObject item) {
	}

	@Override
	protected void onIntentionInteract(WorldObject object) {
	}

	@Override
	protected void onEvtAttacked(Creature attacker) {
	}

	@Override
	protected void onEvtAggression(Creature target, int aggro) {
	}

	@Override
	protected void onEvtStunned(Creature attacker) {
	}

	@Override
	protected void onEvtSleeping(Creature attacker) {
	}

	@Override
	protected void onEvtRooted(Creature attacker) {
	}

	@Override
	protected void onEvtForgetObject(WorldObject object) {
	}

	@Override
	protected void onEvtCancel() {
	}

	@Override
	protected void onEvtDead() {
	}

	@Override
	protected void onEvtFakeDeath() {
	}

	@Override
	protected void onEvtFinishCasting() {
	}

	@Override
	protected void clientActionFailed() {
	}

	@Override
	protected void moveToPawn(WorldObject pawn, int offset) {
	}

	@Override
	protected void clientStoppedMoving() {
	}
}
