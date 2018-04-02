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

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.DefenderInstance;
import l2server.gameserver.model.actor.instance.DoorInstance;

/**
 * @author mkizub
 */
public class DoorAI extends CreatureAI {
	public DoorAI(DoorInstance creature) {
		super(creature);
	}

	// rather stupid AI... well,  it's for doors :D
	@Override
	protected void onIntentionIdle() {
	}

	@Override
	protected void onIntentionActive() {
	}

	@Override
	protected void onIntentionRest() {
	}

	@Override
	protected void onIntentionAttack(Creature target) {
	}

	@Override
	protected void onIntentionCast(Skill skill, WorldObject target) {
	}

	@Override
	protected void onIntentionMoveTo(L2CharPosition destination) {
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
	protected void onEvtThink() {
	}

	@Override
	protected void onEvtAttacked(Creature attacker) {
		DoorInstance me = (DoorInstance) actor;
		ThreadPoolManager.getInstance().executeTask(new onEventAttackedDoorTask(me, attacker));
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
	protected void onEvtReadyToAct() {
	}

	@Override
	protected void onEvtUserCmd(Object arg0, Object arg1) {
	}

	@Override
	protected void onEvtArrived() {
	}

	@Override
	protected void onEvtArrivedRevalidate() {
	}

	@Override
	protected void onEvtArrivedBlocked(L2CharPosition blocked_at_pos) {
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

	private class onEventAttackedDoorTask implements Runnable {
		private DoorInstance door;
		private Creature attacker;

		public onEventAttackedDoorTask(DoorInstance door, Creature attacker) {
			this.door = door;
			this.attacker = attacker;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			for (DefenderInstance guard : door.getKnownDefenders()) {
				if (actor.isInsideRadius(guard, guard.getFactionRange(), false, true) && Math.abs(attacker.getZ() - guard.getZ()) < 200) {
					guard.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 15);
				}
			}
		}
	}
}
