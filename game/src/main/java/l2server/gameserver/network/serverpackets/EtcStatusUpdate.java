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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.model.actor.CreatureZone;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.templates.skills.EffectType;

/* Packet format: F3 XX000000 YY000000 ZZ000000 */

/**
 * @author Luca Baldi
 */
public class EtcStatusUpdate extends L2GameServerPacket {
	
	private Player activeChar;
	
	public EtcStatusUpdate(Player activeChar) {
		this.activeChar = activeChar;
	}
	
	/**
	 * @see l2server.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl() { // several icons to a separate line (0 = disabled)
		writeC(activeChar.getCharges()); // 1-7 increase force, lvl
		writeC(activeChar.getWeightPenalty()); // 1-4 weight penalty, lvl (1=50%, 2=66.6%, 3=80%, 4=100%)
		writeC(activeChar.getMessageRefusal() || activeChar.isChatBanned() || activeChar.isSilenceMode() ? 1 : 0); // 1 = block all chat
		writeC(activeChar.isInsideZone(CreatureZone.ZONE_DANGERAREA) ? 1 : 0); // 1 = danger area
		writeC(activeChar.getExpertiseWeaponPenalty()); // Weapon Grade Penalty [1-4]
		writeC(activeChar.getExpertiseArmorPenalty()); // Armor Grade Penalty [1-4]
		writeC(activeChar.isAffected(EffectType.CHARMOFCOURAGE.getMask()) ? 1 :
				0); // 1 = charm of courage (allows resurrection on the same spot upon death on the siege battlefield)
		writeC(0x00);//writeD(activeChar.getDeathPenaltyBuffLevel()); // 1-15 death penalty, lvl (combat ability decreased due to death)
		writeC(activeChar.getSouls());
		writeC(0x00); // ???
	}
}
