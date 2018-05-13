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

package l2server.gameserver.stats.effects;

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.EffectTemplate;
import l2server.gameserver.templates.skills.EffectType;

/**
 * @author Pere
 */
public class EffectStance extends L2Effect {
	private static final int[] STANCE_IDS = {11007, 11008, 11009, 11010};
	private static final int DOUBLE_CASTING_ID = 11068;

	public EffectStance(Env env, EffectTemplate template) {
		super(env, template);
	}

	@Override
	public EffectType getEffectType() {
		return getSkill().getId() == DOUBLE_CASTING_ID ? EffectType.DOUBLE_CASTING : EffectType.NONE;
	}

	@Override
	public boolean onStart() {
		if (!(getEffected() instanceof Player)) {
			return false;
		}

		Player activeChar = (Player) getEffected();

		Skill skill = getSkill();

		if (skill.getId() == DOUBLE_CASTING_ID) {
			activeChar.setElementalStance(activeChar.getElementalStance() + 10);
			for (int stanceId : STANCE_IDS) {
				if (stanceId != STANCE_IDS[0] + activeChar.getElementalStance() % 10 - 1) {
					Skill stance = SkillTable.getInstance().getInfo(stanceId, 1);
					stance.getEffects(activeChar, activeChar);
				}
			}
		} else if (activeChar.getElementalStance() < 10) {
			// Set the player in the current one
			activeChar.setElementalStance(skill.getId() - STANCE_IDS[0] + 1);
			// And stop the other stances
			for (int stanceId : STANCE_IDS) {
				if (stanceId != skill.getId()) {
					activeChar.stopSkillEffects(stanceId);
				}
			}
		}

		return true;
	}

	@Override
	public void onExit() {
		if (!(getEffected() instanceof Player)) {
			return;
		}

		Player activeChar = (Player) getEffected();
		if (getSkill().getId() == DOUBLE_CASTING_ID) {
			activeChar.setElementalStance(activeChar.getElementalStance() % 10);

			for (int stanceId : STANCE_IDS) {
				if (stanceId - STANCE_IDS[0] + 1 != activeChar.getElementalStance()) {
					activeChar.stopSkillEffects(stanceId);
				}
			}

			activeChar.abortCast();
			activeChar.setCastingNow2(false);
		}
	}

	@Override
	public boolean onActionTime() {
		return getSkill().getId() != DOUBLE_CASTING_ID;
	}
}
