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

import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.StatusUpdate.StatusUpdateDisplay;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectTemplate;

public class EffectMDamOverTime extends L2Effect
{
	public EffectMDamOverTime(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2AbnormalType getAbnormalType()
	{
		return L2AbnormalType.DEBUFF;
	}

	@Override
	public boolean onStart()
	{
		return !getEffected().isDead();

	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
		{
			return false;
		}

		double ssMul = L2ItemInstance.CHARGED_NONE;
		L2ItemInstance weaponInst = getEffector().getActiveWeaponInstance();
		if (weaponInst != null)
		{
			ssMul = weaponInst.getChargedSpiritShot();
			weaponInst.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
		}

		boolean mcrit = Formulas.calcMCrit(getEffector().getMCriticalHit(getEffected(), getSkill()));
		double damage = Formulas.calcMagicDam(getEffector(), getEffected(), getSkill(), (byte) 0, ssMul, mcrit);
		return dealDamage(damage);
	}

	private boolean dealDamage(double damage)
	{
		if (damage >= getEffected().getCurrentHp() - 1)
		{
			if (getSkill().isToggle())
			{
				getEffected().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_HP));
				return false;
			}

			// For DOT skills that will not kill effected player.
			if (!getSkill().killByDOT())
			{
				// Fix for players dying by DOTs if HP < 1 since reduceCurrentHP method will kill them
				if (getEffected().getCurrentHp() <= 1)
				{
					return true;
				}

				damage = getEffected().getCurrentHp() - 1;
			}
		}

		// Exile
		boolean dmgSelf = getSkill().getId() == 11273 || getSkill().getId() == 11296;

		getEffected().reduceCurrentHpByDOT(damage, dmgSelf ? getEffected() : getEffector(), getSkill());

		// If the skill is also drain, heal the effector with the damage
		if (getSkill().absorbDOT())
		{
			double hp = getEffector().getCurrentHp();
			double maxhp = getEffector().getMaxHp();
			hp += damage;
			if (hp > maxhp)
			{
				hp = maxhp;
			}

			getEffector().setCurrentHp(hp);
			StatusUpdate suhp = new StatusUpdate(getEffector(), getEffector(), StatusUpdateDisplay.NORMAL);
			suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
			getEffector().sendPacket(suhp);
		}

		if (getEffector() instanceof L2PcInstance && getSkill().getId() == 11260) // Mark of Void
		{
			double heal = damage * (getEffected().getActingPlayer() == null ? 0.5 : 0.75);
			double hp = getEffector().getCurrentHp();
			double maxhp = getEffector().getMaxHp();
			hp += heal;
			if (hp > maxhp)
			{
				hp = maxhp;
			}
			double mp = getEffector().getCurrentMp();
			double maxmp = getEffector().getMaxMp();
			mp += heal;
			if (mp > maxmp)
			{
				mp = maxmp;
			}

			getEffector().setCurrentHp(hp);
			getEffector().setCurrentMp(mp);
			StatusUpdate su = new StatusUpdate(getEffector());
			su.addAttribute(StatusUpdate.CUR_HP, (int) hp);
			su.addAttribute(StatusUpdate.CUR_MP, (int) mp);
			getEffector().sendPacket(su);

			for (L2SummonInstance summon : ((L2PcInstance) getEffector()).getSummons())
			{
				if (summon == null)
				{
					continue;
				}
				hp = summon.getCurrentHp();
				maxhp = summon.getMaxHp();
				hp += heal;
				if (hp > maxhp)
				{
					hp = maxhp;
				}
				mp = summon.getCurrentMp();
				maxmp = summon.getMaxMp();
				mp += heal;
				if (mp > maxmp)
				{
					mp = maxmp;
				}

				summon.setCurrentHp(hp);
				summon.setCurrentMp(mp);
				su = new StatusUpdate(summon);
				su.addAttribute(StatusUpdate.CUR_HP, (int) hp);
				su.addAttribute(StatusUpdate.CUR_MP, (int) mp);
				getEffector().sendPacket(su);
			}
		}

		return true;
	}
}
