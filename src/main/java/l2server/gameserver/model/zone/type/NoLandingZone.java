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

package l2server.gameserver.model.zone.type;

import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.zone.ZoneType;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * A no landing zone
 *
 * @author durgus
 */
public class NoLandingZone extends ZoneType {
	private int dismountDelay = 5;

	public NoLandingZone(int id) {
		super(id);
	}

	@Override
	protected void onEnter(Creature character) {
		if (character instanceof Player) {
			character.setInsideZone(Creature.ZONE_NOLANDING, true);
			if (((Player) character).getMountType() == 2) {
				character.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN));
				((Player) character).enteredNoLanding(dismountDelay);
			}
		}
	}

	@Override
	protected void onExit(Creature character) {
		if (character instanceof Player) {
			character.setInsideZone(Creature.ZONE_NOLANDING, false);
			if (((Player) character).getMountType() == 2) {
				((Player) character).exitedNoLanding();
			}
		}
	}

	@Override
	public void onDieInside(Creature character, Creature killer) {
	}

	@Override
	public void onReviveInside(Creature character) {
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.model.zone.ZoneType#setParameter(java.lang.String, java.lang.String)
	 */
	@Override
	public void setParameter(String name, String value) {
		if (name.equals("dismountDelay")) {
			dismountDelay = Integer.parseInt(value);
		} else {
			super.setParameter(name, value);
		}
	}
}
