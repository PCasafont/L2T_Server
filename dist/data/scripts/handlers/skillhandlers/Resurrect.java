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
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.PetInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.taskmanager.DecayTaskManager;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.gameserver.templates.skills.SkillType;

import java.util.ArrayList;
import java.util.List;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.5.2.4 $ $Date: 2005/04/03 15:55:03 $
 */

public class Resurrect implements ISkillHandler {
	private static final SkillType[] SKILL_IDS = {SkillType.RESURRECT};

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(Creature, Skill, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		Player player = null;
		if (activeChar instanceof Player) {
			player = (Player) activeChar;
		}

		if (player != null && player.isInOlympiadMode() && player.isOlympiadStart()) {
			return;
		}

		Player targetPlayer;
		List<Creature> targetToRes = new ArrayList<Creature>();

		for (Creature target : (Creature[]) targets) {
			if (target instanceof Player) {
				targetPlayer = (Player) target;

				// Check for same party or for same clan, if target is for clan.
				if (skill.getTargetType() == SkillTargetType.TARGET_CORPSE_CLAN) {
					if (player.getClanId() != targetPlayer.getClanId()) {
						continue;
					}
				}

				if (targetPlayer.isPlayingEvent()) {
					continue;
				}

				if (target.calcStat(Stats.BLOCK_RESURRECTION, 0, target, skill) > 0) {
					continue;
				}
			}
			if (target.isVisible()) {
				targetToRes.add(target);
			}
		}

		if (targetToRes.isEmpty()) {
			activeChar.abortCast();
			return;
		}

		for (Creature cha : targetToRes) {
			if (activeChar instanceof Player) {
				if (cha instanceof Player) {
					((Player) cha).reviveRequest((Player) activeChar, skill, false);
				} else if (cha instanceof PetInstance) {
					((PetInstance) cha).getOwner().reviveRequest((Player) activeChar, skill, true);
				}
			} else {
				DecayTaskManager.getInstance().cancelDecayTask(cha);
				cha.doRevive(Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), activeChar));
			}
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
