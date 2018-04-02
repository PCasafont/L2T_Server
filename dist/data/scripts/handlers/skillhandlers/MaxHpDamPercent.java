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
import l2server.gameserver.model.Item;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.SkillType;

public class MaxHpDamPercent implements ISkillHandler {
	private static final SkillType[] SKILL_IDS = {SkillType.MAXHPDAMPERCENT};

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(Creature, Skill, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		if (activeChar.isAlikeDead()) {
			return;
		}

		for (Creature target : (Creature[]) targets) {
			if (target.isRaid() || target.isDead() || target.isAlikeDead() ||
					target.getFaceoffTarget() != null && target.getFaceoffTarget() != activeChar) {
				continue;
			}

			int damage = (int) (target.getMaxHp() * (skill.getPower() / 100));

			skill.getEffects(activeChar, target, new Env((byte) 0, Item.CHARGED_NONE));

			activeChar.sendDamageMessage(target, damage, false, false, false);

			target.reduceCurrentHp(damage, activeChar, skill);
		}

		if (skill.isSuicideAttack()) {
			activeChar.doDie(activeChar);
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
