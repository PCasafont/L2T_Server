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
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.templates.skills.SkillType;
import l2server.util.Rnd;

public class ShiftTarget implements ISkillHandler {
	private static final SkillType[] SKILL_IDS = {SkillType.SHIFT_TARGET};

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(Creature, Skill, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		if (targets == null) {
			return;
		}
		Creature target = (Creature) targets[0];

		if (activeChar.isAlikeDead() || target == null || !(target instanceof Player)) {
			return;
		}

		Player targetPlayer = (Player) target;
		if (!targetPlayer.isInParty()) {
			return;
		}

		Player otherMember = targetPlayer;
		while (otherMember == targetPlayer) {
			otherMember = targetPlayer.getParty().getPartyMembers().get(Rnd.get(targetPlayer.getParty().getMemberCount()));
		}

		for (Creature obj : activeChar.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius())) {
			if (!(obj instanceof Attackable) || obj.isDead()) {
				continue;
			}

			Attackable hater = (Attackable) obj;
			int hating = hater.getHating(targetPlayer);
			if (hating == 0) {
				continue;
			}

			hater.addDamageHate(otherMember, 0, hating);
			hater.reduceHate(targetPlayer, hating);
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
