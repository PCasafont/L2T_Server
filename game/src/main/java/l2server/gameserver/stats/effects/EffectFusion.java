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
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectTemplate;

/**
 * @author Kerberos
 */
public class EffectFusion extends L2Effect {
	public int effect;
	public int maxEffect;

	public EffectFusion(Env env, EffectTemplate template) {
		super(env, template);
		effect = getSkill().getLevel();
		maxEffect = SkillTable.getInstance().getMaxLevel(getSkill().getId());
	}

	@Override
	public AbnormalType getAbnormalType() {
		return AbnormalType.BUFF;
	}

	/**
	 * @see Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime() {
		return true;
	}

	public void increaseEffect() {
		if (effect < maxEffect) {
			effect++;
			updateBuff();
		}
	}

	public void decreaseForce() {
		effect--;
		if (effect < 1) {
			exit();
		} else {
			updateBuff();
		}
	}

	private void updateBuff() {
		exit();
		SkillTable.getInstance().getInfo(getSkill().getId(), effect).getEffects(getEffector(), getEffected());
	}
}
