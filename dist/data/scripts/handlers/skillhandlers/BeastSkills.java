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
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.TamedBeastInstance;
import l2server.gameserver.templates.skills.SkillType;

/**
 * @author _drunk_
 */
public class BeastSkills implements ISkillHandler {
	// private static Logger log = Logger.getLogger(BeastSkills.class.getName());
	private static final SkillType[] SKILL_IDS =
			{SkillType.BEAST_FEED, SkillType.BEAST_RELEASE, SkillType.BEAST_RELEASE_ALL, SkillType.BEAST_SKILL, SkillType.BEAST_ACCOMPANY};

	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		if (!(activeChar instanceof Player)) {
			return;
		}

		SkillType type = skill.getSkillType();
		Player player = activeChar.getActingPlayer();
		WorldObject target = player.getTarget();

		switch (type) {
			case BEAST_FEED:
				WorldObject[] targetList = skill.getTargetList(activeChar);

				if (targetList == null) {
					return;
				}

				// This is just a dummy skill handler for the golden food and crystal food skills,
				// since the AI responce onSkillUse handles the rest.
				break;
			case BEAST_RELEASE:
				if (target != null && target instanceof TamedBeastInstance) {
					((TamedBeastInstance) target).deleteMe();
				}
				break;
			case BEAST_RELEASE_ALL:
				if (player.getTrainedBeasts() != null) {
					for (TamedBeastInstance beast : player.getTrainedBeasts()) {
						beast.deleteMe();
					}
				}
				break;
			case BEAST_ACCOMPANY:
				// Unknown effect now
				break;
			case BEAST_SKILL:
				if (target != null && target instanceof TamedBeastInstance) {
					((TamedBeastInstance) target).castBeastSkills();
				}
				break;
			default:
		}
	}

	@Override
	public SkillType[] getSkillIds() {
		return SKILL_IDS;
	}
}
