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
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectTemplate;
import l2server.gameserver.templates.skills.EffectType;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author littlecrow
 * <p>
 * Implementation of the Confusion Effect
 */
public class EffectConfusion extends L2Effect {
	public EffectConfusion(Env env, EffectTemplate template) {
		super(env, template);
	}

	/**
	 * @see Abnormal#getType()
	 */
	@Override
	public EffectType getEffectType() {
		return EffectType.CONFUSION;
	}

	@Override
	public AbnormalType getAbnormalType() {
		return AbnormalType.CONFUSION;
	}

	/**
	 * @see Abnormal#onStart()
	 */
	@Override
	public boolean onStart() {
		getEffected().startConfused();
		onActionTime();
		return true;
	}

	/**
	 * @see Abnormal#onExit()
	 */
	@Override
	public void onExit() {
		getEffected().stopConfused(getAbnormal());
	}

	/**
	 * @see Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime() {
		List<Creature> targetList = new ArrayList<>();

		// Getting the possible targets

		Collection<WorldObject> objs = getEffected().getKnownList().getKnownObjects().values();
		// synchronized (getEffected().getKnownList().getKnownObjects())
		{
			for (WorldObject obj : objs) {
				if (obj instanceof Creature && obj != getEffected()) {
					targetList.add((Creature) obj);
				}
			}
		}
		// if there is no target, exit function
		if (targetList.isEmpty()) {
			return true;
		}

		// Choosing randomly a new target
		int nextTargetIdx = Rnd.nextInt(targetList.size());
		WorldObject target = targetList.get(nextTargetIdx);

		// Attacking the target
		getEffected().setTarget(target);
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);

		return true;
	}
}
