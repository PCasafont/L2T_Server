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
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Didl
 */
public class TargetClanParty implements ISkillTargetTypeHandler {

	/**
	 *
	 */
	public TargetClanParty() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public WorldObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
		List<Creature> targetList = new ArrayList<Creature>();

		if (activeChar instanceof Playable) {
			int radius = skill.getSkillRadius();

			Player player = null;

			if (activeChar instanceof Summon) {
				player = ((Summon) activeChar).getOwner();
			} else {
				player = (Player) activeChar;
			}

			if (player == null) {
				return null;
			}

			if (player.isInOlympiadMode() || player.isInDuel()) {
				return new Creature[]{player};
			}

			if (!onlyFirst) {
				targetList.add(player);
			} else {
				return new Creature[]{player};
			}

			/* FIXME
			if (activeChar.getPet() != null)
			{
				if (!(activeChar.getPet().isDead()))
					targetList.add(activeChar.getPet());
			}*/

			for (Player tempChar : player.getKnownList().getKnownPlayersInRadius(radius)) {
				if (tempChar == player || tempChar.isDead()) {
					continue;
				}

				if (tempChar.getClan() != null && player.getClan() != null && player.getClan() == tempChar.getClan() ||
						player.isInParty() && player.getParty().isInParty(tempChar)) {

					if (tempChar.getPet() != null) {
						if (Util.checkIfInRange(radius, activeChar, tempChar.getPet(), true)) {
							if (!tempChar.getPet().isDead() && player.checkPvpSkill(tempChar, skill) && !onlyFirst) {
								targetList.add(tempChar.getPet());
							}
						}
					}

					if (!player.checkPvpSkill(tempChar, skill)) {
						continue;
					}

					if (!onlyFirst) {
						targetList.add(tempChar);
					} else {
						return new Creature[]{tempChar};
					}
				}
			}
		}
		return targetList.toArray(new Creature[targetList.size()]);
	}

	@Override
	public Enum<SkillTargetType> getTargetType() {
		return SkillTargetType.TARGET_CLANPARTY;
	}

	public static void main(String[] args) {
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetClanParty());
	}
}
