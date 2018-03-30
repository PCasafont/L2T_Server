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

import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.skills.L2SkillType;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.5.2.4 $ $Date: 2005/04/03 15:55:03 $
 */

public class ChangeHairAccessory implements ISkillHandler {
	private static final L2SkillType[] SKILL_IDS = {L2SkillType.CHANGE_HAIR_ACCESSORY};

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(l2server.gameserver.model.actor.L2Character, l2server.gameserver.model.L2Skill, l2server.gameserver.model.L2Object[])
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets) {
		if (!(activeChar instanceof L2PcInstance)) {
			return;
		}

		L2PcInstance player = (L2PcInstance) activeChar;
		player.setShowHat(!player.isShowingHat());

		/*
		if (player.isShowingHat())
			player.sendMessage(4168); // Hair accessories will be displayed from now on.
		else
			player.sendMessage(4167); // Hair accessories will no longer be displayed.
		 */
		player.broadcastUserInfo();
	}

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public L2SkillType[] getSkillIds() {
		return SKILL_IDS;
	}
}
