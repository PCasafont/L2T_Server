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
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.DefenderInstance;
import l2server.gameserver.model.actor.instance.FortCommanderInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.entity.Fort;

public class DefenderKnownList extends AttackableKnownList {
	// =========================================================
	// Data Field

	// =========================================================
	// Constructor
	public DefenderKnownList(DefenderInstance activeChar) {
		super(activeChar);
	}

	// =========================================================
	// Method - Public
	@Override
	public boolean addKnownObject(WorldObject object) {
		if (!super.addKnownObject(object)) {
			return false;
		}

		Castle castle = getActiveChar().getCastle();
		Fort fortress = getActiveChar().getFort();
		// Check if siege is in progress
		if (fortress != null && fortress.getZone().isActive() || castle != null && castle.getZone().isActive()) {
			Player player = null;
			if (object instanceof Player) {
				player = (Player) object;
			} else if (object instanceof Summon) {
				player = ((Summon) object).getOwner();
			}
			int activeSiegeId = fortress != null ? fortress.getFortId() : castle != null ? castle.getCastleId() : 0;

			// Check if player is an enemy of this defender npc
			if (player != null &&
					(player.getSiegeState() == 2 && !player.isRegisteredOnThisSiegeField(activeSiegeId) || player.getSiegeState() == 0)) {
				if (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE) {
					getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
				}
			}
		}
		return true;
	}

	// =========================================================
	// Property - Public
	@Override
	public final DefenderInstance getActiveChar() {
		if (super.getActiveChar() instanceof FortCommanderInstance) {
			return (FortCommanderInstance) super.getActiveChar();
		}

		return (DefenderInstance) super.getActiveChar();
	}
}
