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
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.gameserver.templates.skills.SkillType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author One
 */
public class TargetCorpseAlly implements ISkillTargetTypeHandler {
	/**
	 */
	@Override
	public WorldObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
		List<Creature> targetList = new ArrayList<Creature>();

		if (activeChar instanceof Player) {
			int radius = skill.getSkillRadius();
			Player player = (Player) activeChar;
			L2Clan clan = player.getClan();

			if (player.isInOlympiadMode()) {
				return new Creature[]{player};
			}

			if (clan != null) {
				// Get all visible objects in a spherical area near the Creature
				// Get Clan Members
				Collection<WorldObject> objs = activeChar.getKnownList().getKnownObjects().values();
				//synchronized (activeChar.getKnownList().getKnownObjects())
				{
					for (WorldObject newTarget : objs) {
						if (!(newTarget instanceof Player)) {
							continue;
						}
						if ((((Player) newTarget).getAllyId() == 0 || ((Player) newTarget).getAllyId() != player.getAllyId()) &&
								(((Player) newTarget).getClan() == null || ((Player) newTarget).getClanId() != player.getClanId())) {
							continue;
						}
						if (player.isInDuel() && (player.getDuelId() != ((Player) newTarget).getDuelId() ||
								player.getParty() != null && !player.getParty().isInParty(newTarget))) {
							continue;
						}

						if (!((Player) newTarget).isDead()) {
							continue;
						}

						if (skill.getSkillType() == SkillType.RESURRECT) {
							// check target is not in a active siege
							// zone
							if (((Player) newTarget).isInsideZone(Creature.ZONE_SIEGE) && ((Player) newTarget).getSiegeState() == 0) {
								continue;
							}
						}

						if (!Util.checkIfInRange(radius, activeChar, newTarget, true)) {
							continue;
						}

						// Don't add this target if this is a Pc->Pc pvp
						// casting and pvp condition not met
						if (!player.checkPvpSkill(newTarget, skill)) {
							continue;
						}

						if (!onlyFirst) {
							targetList.add((Creature) newTarget);
						} else {
							return new Creature[]{(Creature) newTarget};
						}
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
		return SkillTargetType.TARGET_CORPSE_ALLY;
	}

	public static void main(String[] args) {
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetCorpseAlly());
	}
}
