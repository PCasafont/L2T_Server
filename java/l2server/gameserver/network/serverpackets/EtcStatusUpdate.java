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

import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.skills.L2EffectType;

/* Packet format: F3 XX000000 YY000000 ZZ000000 */

/**
 * @author Luca Baldi
 */
public class EtcStatusUpdate extends L2GameServerPacket
{

	private L2PcInstance _activeChar;

	public EtcStatusUpdate(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
	}

	/**
	 * @see l2server.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{ // several icons to a separate line (0 = disabled)
		writeC(_activeChar.getCharges()); // 1-7 increase force, lvl
		writeC(_activeChar.getWeightPenalty()); // 1-4 weight penalty, lvl (1=50%, 2=66.6%, 3=80%, 4=100%)
		writeC(_activeChar.getMessageRefusal() || _activeChar.isChatBanned() || _activeChar.isSilenceMode() ? 1 :
				0); // 1 = block all chat
		writeC(_activeChar.isInsideZone(L2Character.ZONE_DANGERAREA) ? 1 : 0); // 1 = danger area
		writeC(_activeChar.getExpertiseWeaponPenalty()); // Weapon Grade Penalty [1-4]
		writeC(_activeChar.getExpertiseArmorPenalty()); // Armor Grade Penalty [1-4]
		writeC(_activeChar.isAffected(L2EffectType.CHARMOFCOURAGE.getMask()) ? 1 :
				0); // 1 = charm of courage (allows resurrection on the same spot upon death on the siege battlefield)
		writeC(0x00);//writeD(_activeChar.getDeathPenaltyBuffLevel()); // 1-15 death penalty, lvl (combat ability decreased due to death)
		writeC(_activeChar.getSouls());
		writeC(0x00); // ???
	}
}
