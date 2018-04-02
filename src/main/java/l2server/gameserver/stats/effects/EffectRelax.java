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

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.EffectTemplate;
import l2server.gameserver.templates.skills.EffectType;

public class EffectRelax extends L2Effect {
	public EffectRelax(Env env, EffectTemplate template) {
		super(env, template);
	}

	/**
	 * @see Abnormal#getType()
	 */
	@Override
	public EffectType getEffectType() {
		return EffectType.RELAXING;
	}

	/**
	 * @see Abnormal#onStart()
	 */
	@Override
	public boolean onStart() {
		if (getEffected() instanceof Player) {
			((Player) getEffected()).sitDown(false);
		} else {
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
		}
		return super.onStart();
	}

	/**
	 * @see Abnormal#onExit()
	 */
	@Override
	public void onExit() {
		super.onExit();
	}

	/**
	 * @see Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime() {
		if (getEffected().isDead()) {
			return false;
		}

		if (getEffected() instanceof Player) {
			if (!((Player) getEffected()).isSitting()) {
				return false;
			}
		}

		if (getEffected().getCurrentHp() + 1 > getEffected().getMaxHp()) {
			if (getSkill().isToggle()) {
				getEffected().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_DEACTIVATED_HP_FULL));
				return false;
			}
		}

		double manaDam = calc();

		if (manaDam > getEffected().getCurrentMp()) {
			if (getSkill().isToggle()) {
				getEffected().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
				return false;
			}
		}

		getEffected().reduceCurrentMp(manaDam);
		return true;
	}
}
