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

import l2server.Config;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectTemplate;
import l2server.gameserver.templates.skills.L2EffectType;
import l2server.util.Rnd;

import java.util.ArrayList;

/**
 * @author Pere
 */
public class EffectCancel extends L2Effect
{
	public EffectCancel(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2AbnormalType getAbnormalType()
	{
		return L2AbnormalType.CANCEL;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		L2Character caster = getEffector();
		L2Skill skill = getSkill();

		int minNegate = skill.getMinNegatedEffects(); // Skill cancels at least this amount of buffs
		int maxNegate = skill.getMaxNegatedEffects(); // Skill cancels up to this amount of buffs
		double rate =
				calc(); // After cancelling the min amount, this is the chance of each additional buff to be cancelled

		// Only apply cancellation effect to players
		if (!(getEffected() instanceof L2PcInstance))
		{
			return false;
		}

		L2PcInstance target = (L2PcInstance) getEffected();

		// No effect on dead targets
		if (target.isDead())
		{
			return false;
		}

		// Reference to the collection of target's buffs and debuffs
		final L2Abnormal[] effects = target.getAllEffects();

		// Filter buff-type effects out of the effect collection
		ArrayList<L2Abnormal> removableBuffs = new ArrayList<>();
		for (L2Abnormal effect : effects)
		{
			if (effect.canBeStolen() || effect.getEffectMask() == L2EffectType.INVINCIBLE.getMask())
			{
				removableBuffs.add(effect);
			}
		}

		for (int i = 0; i < maxNegate; i++)
		{
			if (removableBuffs.isEmpty())
			{
				break;
			}

			// Get a random buff index for cancellation try
			int candidate = Rnd.get(removableBuffs.size());

			// More detailed .landrates feedback considering enchanted buffs
			if (caster instanceof L2PcInstance && i > minNegate && caster.getActingPlayer().isLandRates())
			{
				caster.sendMessage(
						"Attempted to remove " + removableBuffs.get(candidate).getSkill().getName() + " with " + rate +
								"% chance.");
			}

			// Give it a try with rate% chance
			if (i < minNegate || Rnd.get(100) < rate)
			{
				L2Abnormal buff = removableBuffs.get(candidate);
				if (buff == null)
				{
					continue;
				}

				buff.getEffected().onExitChanceEffect(buff.getSkill(), buff.getSkill().getElement());
				buff.exit();

				// Tenkai custom: recover buffs 1 minute after they're cancelled!
				if (Config.isServer(Config.TENKAI) && !Config.isServer(Config.TENKAI_ESTHUS) &&
						buff.getEffected() instanceof L2PcInstance)
				{
					target.scheduleEffectRecovery(buff, 60, target.isInOlympiadMode());
				}

				if (caster instanceof L2PcInstance && caster.getActingPlayer().isLandRates())
				{
					caster.sendMessage("Attempt to remove " + buff.getSkill().getName() + " succeeded.");
				}

				// Remove the reference to the cancelled buffs from the collection to not try same again
				removableBuffs.remove(candidate);
			}
		}

		return true;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
