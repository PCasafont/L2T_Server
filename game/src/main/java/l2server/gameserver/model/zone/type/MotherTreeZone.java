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
import l2server.gameserver.model.actor.CreatureZone;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.zone.ZoneType;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * A mother-trees zone
 * Basic type zone for Hp, MP regen
 *
 * @author durgus
 */
public class MotherTreeZone extends ZoneType {
	private int enterMsg;
	private int leaveMsg;
	private int mpRegen;
	private int hpRegen;

	public MotherTreeZone(int id) {
		super(id);
	}

	@Override
	public void setParameter(String name, String value) {
		switch (name) {
			case "enterMsgId":
				enterMsg = Integer.valueOf(value);
				break;
			case "leaveMsgId":
				leaveMsg = Integer.valueOf(value);
				break;
			case "MpRegenBonus":
				mpRegen = Integer.valueOf(value);
				break;
			case "HpRegenBonus":
				hpRegen = Integer.valueOf(value);
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}

	@Override
	protected void onEnter(Creature character) {
		if (character instanceof Player) {
			Player player = (Player) character;
			player.setInsideZone(CreatureZone.ZONE_MOTHERTREE, true);
			if (enterMsg != 0) {
				player.sendPacket(SystemMessage.getSystemMessage(enterMsg));
			}
		}
	}

	@Override
	protected void onExit(Creature character) {
		if (character instanceof Player) {
			Player player = (Player) character;
			player.setInsideZone(CreatureZone.ZONE_MOTHERTREE, false);
			if (leaveMsg != 0) {
				player.sendPacket(SystemMessage.getSystemMessage(leaveMsg));
			}
		}
	}

	@Override
	public void onDieInside(Creature character, Creature killer) {
	}

	@Override
	public void onReviveInside(Creature character) {
	}

	/**
	 * @return the mpRegen
	 */
	public int getMpRegenBonus() {
		return mpRegen;
	}

	/**
	 * @return the hpRegen
	 */
	public int getHpRegenBonus() {
		return hpRegen;
	}
}
