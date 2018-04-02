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

import l2server.gameserver.RecipeController;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.SkillType;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.2.2.4 $ $Date: 2005/04/06 16:13:48 $
 */

public class Craft implements ISkillHandler {
	private static final SkillType[] SKILL_IDS = {SkillType.COMMON_CRAFT, SkillType.DWARVEN_CRAFT};
	
	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(Creature, Skill, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		if (!(activeChar instanceof Player)) {
			return;
		}
		
		Player player = (Player) activeChar;
		
		if (player.getPrivateStoreType() != 0) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_CREATED_WHILE_ENGAGED_IN_TRADING));
			return;
		}
		RecipeController.getInstance().requestBookOpen(player, skill.getSkillType() == SkillType.DWARVEN_CRAFT ? true : false);
	}
	
	/**
	 * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public SkillType[] getSkillIds() {
		return SKILL_IDS;
	}
}
