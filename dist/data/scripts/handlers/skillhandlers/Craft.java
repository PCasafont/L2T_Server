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
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.L2SkillType;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.2.2.4 $ $Date: 2005/04/06 16:13:48 $
 */

public class Craft implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {L2SkillType.COMMON_CRAFT, L2SkillType.DWARVEN_CRAFT};

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(l2server.gameserver.model.actor.L2Character, l2server.gameserver.model.L2Skill, l2server.gameserver.model.L2Object[])
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance))
		{
			return;
		}

		L2PcInstance player = (L2PcInstance) activeChar;

		if (player.getPrivateStoreType() != 0)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_CREATED_WHILE_ENGAGED_IN_TRADING));
			return;
		}
		RecipeController.getInstance()
				.requestBookOpen(player, skill.getSkillType() == L2SkillType.DWARVEN_CRAFT ? true : false);
	}

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
