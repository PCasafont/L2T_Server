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

import l2server.gameserver.instancemanager.GMEventManager;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.zone.SpawnZone;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * An arena
 *
 * @author durgus
 */
public class ArenaZone extends SpawnZone {

	public ArenaZone(int id) {
		super(id);
	}

	@Override
	protected void onEnter(Creature character) {

		if (!GMEventManager.getInstance().onEnterZone(character, this)) {
			return;
		}

		character.setInsideZone(Creature.ZONE_PVP, true);
		character.setInsideZone(Creature.ZONE_NOSUMMONFRIEND, true);

		if (character instanceof Player) {
			if (!character.isInsideZone(Creature.ZONE_PVP)) {
				character.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
			}
		}
	}

	@Override
	protected void onExit(Creature character) {
		character.setInsideZone(Creature.ZONE_PVP, false);
		character.setInsideZone(Creature.ZONE_NOSUMMONFRIEND, false);

		if (character instanceof Player) {
			if (!character.isInsideZone(Creature.ZONE_PVP)) {
				character.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
			}
		}
	}

	@Override
	public void onDieInside(Creature character, Creature killer) {
	}

	@Override
	public void onReviveInside(Creature character) {
	}
}
