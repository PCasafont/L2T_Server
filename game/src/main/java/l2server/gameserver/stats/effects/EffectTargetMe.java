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
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.SiegeSummonInstance;
import l2server.gameserver.network.serverpackets.MyTargetSelected;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectTemplate;

/**
 * @author -Nemesiss-
 */
public class EffectTargetMe extends L2Effect {
	public EffectTargetMe(Env env, EffectTemplate template) {
		super(env, template);
	}

	@Override
	public AbnormalType getAbnormalType() {
		return AbnormalType.DEBUFF;
	}

	/**
	 * @see Abnormal#onStart()
	 */
	@Override
	public boolean onStart() {
		if (getEffected() instanceof Playable) {
			if (getEffected() instanceof SiegeSummonInstance) {
				return false;
			}

			if (getEffected() instanceof Player && ((Player) getEffected()).isCastingProtected()) {
				return false;
			}

			if (getEffected().getTarget() != getEffector()) {
				// Target is different
				getEffected().abortAttack();
				getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				getEffected().setTarget(getEffector());
				if (getEffected() instanceof Player) {
					getEffected().sendPacket(new MyTargetSelected(getEffector().getObjectId(), 0));
				}
			}

			if (getAbnormal().getTemplate().getDuration() > 0) {
				((Playable) getEffected()).setLockedTarget(getEffector());
			}

			return true;
		} else if (getEffected() instanceof Attackable && !getEffected().isRaid()) {
			return true;
		}

		return false;
	}

	/**
	 * @see Abnormal#onExit()
	 */
	@Override
	public void onExit() {
		if (getEffected() instanceof Playable) {
			((Playable) getEffected()).setLockedTarget(null);
		}
	}

	/**
	 * @see Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime() {
		// nothing
		return false;
	}
}
