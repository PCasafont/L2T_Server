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
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.SkillType;

/**
 * @author ZaKax
 */

public class Detection implements ISkillHandler {
	private static final SkillType[] SKILL_IDS = {SkillType.DETECTION};

	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		final boolean hasParty;
		final boolean hasClan;
		final boolean hasAlly;
		final Player player = activeChar.getActingPlayer();
		if (player != null) {
			hasParty = player.isInParty();
			hasClan = player.getClanId() > 0;
			hasAlly = player.getAllyId() > 0;
		} else {
			hasParty = false;
			hasClan = false;
			hasAlly = false;
		}

		for (Player target : activeChar.getKnownList().getKnownPlayersInRadius(skill.getSkillRadius())) {
			if (target != null && target.getAppearance().getInvisible()) {
				if (hasParty && target.getParty() != null && player.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID()) {
					continue;
				}
				if (hasClan && player.getClanId() == target.getClanId()) {
					continue;
				}
				if (hasAlly && player.getAllyId() == target.getAllyId()) {
					continue;
				}

				Abnormal eHide = target.getFirstEffect(AbnormalType.HIDE);
				if (eHide != null) {
					eHide.exit();
				}
			}
		}
	}

	@Override
	public SkillType[] getSkillIds() {
		return SKILL_IDS;
	}
}
