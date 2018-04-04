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

package l2server.gameserver.model.actor.status;

import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;

public class AttackableStatus extends NpcStatus {
	public AttackableStatus(Attackable activeChar) {
		super(activeChar);
	}

	@Override
	public final void reduceHp(double value, Creature attacker) {
		reduceHp(value, attacker, true, false, false);
	}

	@Override
	public final void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHpConsumption) {
		if (getActiveChar().isDead()) {
			return;
		}

		if (value > 0) {
			if (getActiveChar().isOverhit()) {
				getActiveChar().setOverhitValues(attacker, value);
			} else {
				getActiveChar().overhitEnabled(false);
			}
		} else {
			getActiveChar().overhitEnabled(false);
		}

		super.reduceHp(value, attacker, awake, isDOT, isHpConsumption);

		if (!getActiveChar().isDead())
		// And the attacker's hit didn't kill the mob, clear the over-hit flag
		{
			getActiveChar().overhitEnabled(false);
		}
	}

	@Override
	public Attackable getActiveChar() {
		return (Attackable) super.getActiveChar();
	}
}
