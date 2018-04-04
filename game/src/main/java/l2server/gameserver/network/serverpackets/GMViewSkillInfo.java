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

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.instance.Player;

public class GMViewSkillInfo extends L2GameServerPacket {
	private Player activeChar;
	private Skill[] skills;
	
	public GMViewSkillInfo(Player cha) {
		activeChar = cha;
		skills = activeChar.getAllSkills();
		if (skills.length == 0) {
			skills = new Skill[0];
		}
	}
	
	@Override
	protected final void writeImpl() {
		writeS(activeChar.getName());
		writeD(skills.length);
		
		boolean isDisabled = false;
		if (activeChar.getClan() != null) {
			isDisabled = activeChar.getClan().getReputationScore() < 0;
		}
		
		for (Skill skill : skills) {
			writeD(skill.isPassive() ? 1 : 0);
			writeD(skill.getLevelHash());
			writeD(skill.getId());
			writeD(-1); // GoD ???
			writeC(isDisabled && skill.isClanSkill() ? 1 : 0);
			writeC(SkillTable.getInstance().isEnchantable(skill.getId()) ? 1 : 0);
		}
	}
}
