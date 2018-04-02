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

package handlers.skillhandlers;

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.SkillType;

/**
 * @author nBd
 */

public class Soul implements ISkillHandler {
	private static final SkillType[] SKILL_IDS = {SkillType.CHARGESOUL};
	
	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(Creature, Skill, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		if (!(activeChar instanceof Player) || activeChar.isAlikeDead()) {
			return;
		}
		
		Player player = (Player) activeChar;
		
		int level = player.getSkillLevelHash(467);
		if (level > 0) {
			Skill soulmastery = SkillTable.getInstance().getInfo(467, level);
			
			if (soulmastery != null) {
				if (player.getSouls() < soulmastery.getNumSouls()) {
					int count = 0;
					
					if (player.getSouls() + skill.getNumSouls() <= soulmastery.getNumSouls()) {
						count = skill.getNumSouls();
					} else {
						count = soulmastery.getNumSouls() - player.getSouls();
					}
					
					player.increaseSouls(count);
				} else {
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SOUL_CANNOT_BE_INCREASED_ANYMORE);
					player.sendPacket(sm);
					return;
				}
			}
		}
	}
	
	/**
	 * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public SkillType[] getSkillIds() {
		return SKILL_IDS;
	}
}
