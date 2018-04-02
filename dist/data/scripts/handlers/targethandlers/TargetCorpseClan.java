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
import l2server.gameserver.model.L2ClanMember;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.Summon;
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
public class TargetCorpseClan implements ISkillTargetTypeHandler {
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

			L2Clan clan = player.getClan();

			if (player.isInOlympiadMode()) {
				return new Creature[]{player};
			}

			if (clan != null) {
				// Get all visible objects in a spheric area near the Creature
				// Get Clan Members
				for (L2ClanMember member : clan.getMembers()) {
					Player newTarget = member.getPlayerInstance();

					if (newTarget == null || newTarget == player) {
						continue;
					}

					if (player.isInDuel() &&
							(player.getDuelId() != newTarget.getDuelId() || player.getParty() != null && !player.getParty().isInParty(newTarget))) {
						continue;
					}

					if (!newTarget.isDead()) {
						continue;
					}

					if (skill.getSkillType() == SkillType.RESURRECT) {
						// check target is not in a active siege zone
						if (newTarget.isInsideZone(Creature.ZONE_SIEGE)) {
							continue;
						}
					}

					if (!Util.checkIfInRange(radius, activeChar, newTarget, true)) {
						continue;
					}

					// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
					if (!player.checkPvpSkill(newTarget, skill)) {
						continue;
					}

					if (!onlyFirst) {
						targetList.add(newTarget);
					} else {
						return new Creature[]{newTarget};
					}
				}
			}
		} else if (activeChar instanceof Npc) {
			// for buff purposes, returns one unbuffed friendly mob nearby or mob itself?
			Npc npc = (Npc) activeChar;
			Collection<WorldObject> objs = activeChar.getKnownList().getKnownObjects().values();
			//synchronized (activeChar.getKnownList().getKnownObjects())
			{
				for (WorldObject newTarget : objs) {
					if (newTarget instanceof Npc && ((Npc) newTarget).getFactionId() == npc.getFactionId()) {
						if (!Util.checkIfInRange(skill.getCastRange(), activeChar, newTarget, true)) {
							continue;
						}
						if (((Npc) newTarget).getFirstEffect(skill) != null) {
							targetList.add((Npc) newTarget);
							break;
						}
					}
				}
			}
			if (targetList.isEmpty()) {
				targetList.add(activeChar);
			}
		}

		return targetList.toArray(new Creature[targetList.size()]);
	}

	@Override
	public Enum<SkillTargetType> getTargetType() {
		// TODO Auto-generated method stub
		return SkillTargetType.TARGET_CORPSE_CLAN;
	}

	public static void main(String[] args) {
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetCorpseClan());
	}
}
