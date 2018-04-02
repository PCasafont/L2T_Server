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
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.zone.ZoneType;
import l2server.gameserver.network.serverpackets.NpcInfo;
import l2server.gameserver.network.serverpackets.ServerObjectInfo;

import java.util.Collection;

public class WaterZone extends ZoneType {
	public WaterZone(int id) {
		super(id);
	}

	@Override
	protected void onEnter(Creature character) {
		character.setInsideZone(Creature.ZONE_WATER, true);

		if (character instanceof Player) {
			if (character.isTransformed() && !((Player) character).isCursedWeaponEquipped()) {
				character.stopTransformation(true);
				//((Player) character).untransform();
			}
			// TODO: update to only send speed status when that packet is known
			else {
				((Player) character).broadcastUserInfo();
			}
		} else if (character instanceof Npc) {
			Collection<Player> plrs = character.getKnownList().getKnownPlayers().values();
			//synchronized (character.getKnownList().getKnownPlayers())
			{
				for (Player player : plrs) {
					if (character.getRunSpeed() == 0) {
						player.sendPacket(new ServerObjectInfo((Npc) character, player));
					} else {
						player.sendPacket(new NpcInfo((Npc) character, player));
					}
				}
			}
		}

		/*
		 * if (character instanceof Player) {
		 * ((Player)character).sendMessage("You entered water!"); }
		 */
	}

	@Override
	protected void onExit(Creature character) {
		character.setInsideZone(Creature.ZONE_WATER, false);

		/*if (character instanceof Player)
		{
			((Player)character).sendMessage("You exited water!");
		}*/

		// TODO: update to only send speed status when that packet is known
		if (character instanceof Player) {
			((Player) character).broadcastUserInfo();
		} else if (character instanceof Npc) {
			Collection<Player> plrs = character.getKnownList().getKnownPlayers().values();
			//synchronized (character.getKnownList().getKnownPlayers())
			{
				for (Player player : plrs) {
					if (character.getRunSpeed() == 0) {
						player.sendPacket(new ServerObjectInfo((Npc) character, player));
					} else {
						player.sendPacket(new NpcInfo((Npc) character, player));
					}
				}
			}
		}
	}

	@Override
	public void onDieInside(Creature character, Creature killer) {
	}

	@Override
	public void onReviveInside(Creature character) {
	}

	public int getWaterZ() {
		return getZone().getHighZ();
	}
}
