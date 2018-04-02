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

package handlers.targethandlers;

import l2server.gameserver.handler.ISkillTargetTypeHandler;
import l2server.gameserver.handler.SkillTargetTypeHandler;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nBd
 */
public class TargetParty implements ISkillTargetTypeHandler {
	/**
	 */
	@Override
	public WorldObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
		List<Creature> targetList = new ArrayList<Creature>();

		if (onlyFirst) {
			return new Creature[]{activeChar};
		}

		targetList.add(activeChar);

		Player player = null;

		if (activeChar instanceof Summon) {
			player = ((Summon) activeChar).getOwner();
			targetList.add(player);
		} else if (activeChar instanceof Player) {
			player = (Player) activeChar;
			for (Summon summon : ((Player) activeChar).getSummons()) {
				if (!summon.isDead()) {
					targetList.add(summon);
				}
			}
		}

		if (activeChar.getParty() != null) {
			// Get all visible objects in a spherical area near the Creature
			// Get a list of Party Members
			List<Player> partyList = activeChar.getParty().getPartyMembers();

			for (Player partyMember : partyList) {
				if (partyMember == null) {
					continue;
				}
				if (partyMember == player) {
					continue;
				}

				if (!partyMember.isDead() && Util.checkIfInRange(skill.getSkillRadius(), activeChar, partyMember, true)) {
					targetList.add(partyMember);

					if (partyMember.getPet() != null && !partyMember.getPet().isDead()) {
						targetList.add(partyMember.getPet());
					}
				}
			}
		}
		return targetList.toArray(new Creature[targetList.size()]);
	}

	/**
	 */
	@Override
	public Enum<SkillTargetType> getTargetType() {
		return SkillTargetType.TARGET_PARTY;
	}

	public static void main(String[] args) {
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetParty());
	}
}
