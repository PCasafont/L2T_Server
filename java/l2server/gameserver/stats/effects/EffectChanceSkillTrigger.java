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

import l2server.gameserver.model.ChanceCondition;
import l2server.gameserver.model.IChanceSkillTrigger;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2EffectTemplate;

public class EffectChanceSkillTrigger extends L2Effect implements IChanceSkillTrigger
{
	private final int triggeredId;
	private final int triggeredLevel;
	private final int triggeredEnchantRoute;
	private final int triggeredEnchantLevel;
	private final ChanceCondition chanceCondition;

	public EffectChanceSkillTrigger(Env env, L2EffectTemplate template)
	{
		super(env, template);

		triggeredId = template.triggeredId;
		triggeredLevel = template.triggeredLevel;
		triggeredEnchantRoute = template.triggeredEnchantRoute;
		triggeredEnchantLevel = template.triggeredEnchantLevel;
		chanceCondition = template.chanceCondition;
	}

	// Special constructor to steal this effect
	public EffectChanceSkillTrigger(Env env, L2Effect effect)
	{
		super(env, effect);

		triggeredId = effect.getTemplate().triggeredId;
		triggeredLevel = effect.getTemplate().triggeredLevel;
		triggeredEnchantRoute = effect.getTemplate().triggeredEnchantRoute;
		triggeredEnchantLevel = effect.getTemplate().triggeredEnchantLevel;
		chanceCondition = effect.getTemplate().chanceCondition;
	}

	@Override
	protected boolean effectCanBeStolen()
	{
		return true;
	}

	@Override
	public boolean onStart()
	{
		getEffected().addChanceTrigger(this);
		getEffected().onStartChanceEffect(getSkill(), getSkill().getElement());
		return super.onStart();
	}

	@Override
	public boolean onActionTime()
	{
		L2Abnormal activeEffect = getEffected().getFirstEffect(triggeredId);
		if (activeEffect != null)
		{
			if (activeEffect.getLevel() == triggeredLevel &&
					activeEffect.getEnchantRouteId() == triggeredEnchantRoute &&
					activeEffect.getEnchantLevel() == triggeredEnchantLevel)
			{
				return true;
			}
		}

		if (getSkill().isToggle())
		{
			int dam = (int) calc();
			double manaDam = dam % 1000;
			if (manaDam > getEffected().getCurrentMp())
			{
				getEffected().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
				return false;
			}

			getEffected().reduceCurrentMp(manaDam);

			double hpDam = dam / 1000;
			if (hpDam > getEffected().getCurrentHp() - 1)
			{
				getEffected().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_HP));
				return false;
			}

			getEffected().reduceCurrentHpByDOT(hpDam, getEffected(), getSkill());
		}

		getEffected().onActionTimeChanceEffect(getSkill(), getSkill().getElement());
		return true;
	}

	@Override
	public void onExit()
	{
		// trigger only if effect in use and successfully ticked to the end
		if (getAbnormal().getInUse() && getAbnormal().getCount() == 0)
		{
			getEffected().onExitChanceEffect(getSkill(), getSkill().getElement());
		}
		getEffected().removeChanceEffect(this);
		super.onExit();
	}

	@Override
	public int getTriggeredChanceId()
	{
		return triggeredId;
	}

	@Override
	public int getTriggeredChanceLevel()
	{
		return triggeredLevel;
	}

	@Override
	public int getTriggeredChanceEnchantRoute()
	{
		return triggeredEnchantRoute;
	}

	@Override
	public int getTriggeredChanceEnchantLevel()
	{
		return triggeredEnchantLevel;
	}

	@Override
	public boolean triggersChanceSkill()
	{
		return triggeredId > 1;
	}

	@Override
	public ChanceCondition getTriggeredChanceCondition()
	{
		return chanceCondition;
	}
}
