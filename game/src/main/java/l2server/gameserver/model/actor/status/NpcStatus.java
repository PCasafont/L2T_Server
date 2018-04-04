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

import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.entity.Duel;

public class NpcStatus extends CharStatus {
	public NpcStatus(Npc activeChar) {
		super(activeChar);
	}

	@Override
	public void reduceHp(double value, Creature attacker) {
		reduceHp(value, attacker, true, false, false);
	}

	@Override
	public void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHpConsumption) {
		if (getActiveChar().isDead()) {
			return;
		}

		if (attacker != null) {
			final Player attackerPlayer = attacker.getActingPlayer();
			if (attackerPlayer != null && attackerPlayer.isInDuel()) {
				attackerPlayer.setDuelState(Duel.DUELSTATE_INTERRUPTED);
			}

			// Add attackers to npc's attacker list
			getActiveChar().addAttackerToAttackByList(attacker);
		}

		super.reduceHp(value, attacker, awake, isDOT, isHpConsumption);
	}

	@Override
	public Npc getActiveChar() {
		return (Npc) super.getActiveChar();
	}
}
