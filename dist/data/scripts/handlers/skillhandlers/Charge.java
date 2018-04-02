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
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.templates.skills.SkillType;

import java.util.logging.Logger;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.2.2.9 $ $Date: 2005/04/04 19:08:01 $
 */

public class Charge implements ISkillHandler {
	static Logger log = Logger.getLogger(Charge.class.getName());

	/* (non-Javadoc)
	 * @see l2server.gameserver.handler.IItemHandler#useItem(l2server.gameserver.model.Player, l2server.gameserver.model.Item)
	 */
	private static final SkillType[] SKILL_IDS = {/*SkillType.CHARGE*/};

	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {

		for (WorldObject target : targets) {
			if (!(target instanceof Player)) {
				continue;
			}
			skill.getEffects(activeChar, (Player) target);
		}

		// self Effect :]
		if (skill.hasSelfEffects()) {
			final Abnormal effect = activeChar.getFirstEffect(skill.getId());
			if (effect != null && effect.isSelfEffect()) {
				//Replace old effect with new one.
				effect.exit();
			}
			skill.getEffectsSelf(activeChar);
		}
	}

	@Override
	public SkillType[] getSkillIds() {
		return SKILL_IDS;
	}
}
