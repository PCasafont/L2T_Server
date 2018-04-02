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
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.templates.skills.SkillTargetType;

/**
 * @author nBd
 */
public class TargetEnemySummon implements ISkillTargetTypeHandler {
	@Override
	public WorldObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
		if (target instanceof Summon) {
			Summon targetSummon = (Summon) target;
			if (activeChar instanceof Player && ((Player) activeChar).getPet() != targetSummon && !targetSummon.isDead() &&
					!((Player) activeChar).getSummons().contains(targetSummon) &&
					(targetSummon.getOwner().getPvpFlag() != 0 || targetSummon.getOwner().getReputation() < 0) ||
					targetSummon.getOwner().isInsideZone(Creature.ZONE_PVP) && ((Player) activeChar).isInsideZone(Creature.ZONE_PVP) ||
					targetSummon.getOwner().isInDuel() && ((Player) activeChar).isInDuel() &&
							targetSummon.getOwner().getDuelId() == ((Player) activeChar).getDuelId()) {
				return new Creature[]{targetSummon};
			}
		}

		return null;
	}

	@Override
	public Enum<SkillTargetType> getTargetType() {
		// TODO Auto-generated method stub
		return SkillTargetType.TARGET_ENEMY_SUMMON;
	}

	public static void main(String[] args) {
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetEnemySummon());
	}
}
