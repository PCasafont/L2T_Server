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

package l2server.gameserver.model.actor.stat;

import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2CloneInstance;
import l2server.gameserver.stats.Stats;

public class SummonStat extends PlayableStat
{
	public SummonStat(L2Summon activeChar)
	{
		super(activeChar);
	}

	@Override
	public L2Summon getActiveChar()
	{
		return (L2Summon) super.getActiveChar();
	}

	@Override
	public int getPAtk(L2Character target)
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getPAtk(target);
		}

		if (getActiveChar() instanceof L2CloneInstance)
		{
			return getActiveChar().getOwner().getPAtk(target);
		}

		double ownerBonus = getActiveChar().getOwner().getPAtk(target);
		double percent = getActiveChar().getOwner().calcStat(Stats.OWNER_PATK, 0, null, null);
		ownerBonus *= percent / 100.0;
		return super.getPAtk(target) + (int) ownerBonus;
	}

	@Override
	public int getPDef(L2Character target)
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getPAtkSpd();
		}

		if (getActiveChar() instanceof L2CloneInstance)
		{
			return (int) (getActiveChar().getOwner().getPDef(target) * 0.25);
		}

		double ownerBonus = getActiveChar().getOwner().getPDef(target);
		double percent = getActiveChar().getOwner().calcStat(Stats.OWNER_PDEF, 0, null, null);
		ownerBonus *= percent / 100.0;
		return super.getPDef(target) + (int) ownerBonus;
	}

	@Override
	public int getMAtk(L2Character target, L2Skill skill)
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getMAtk(target, skill);
		}

		if (getActiveChar() instanceof L2CloneInstance)
		{
			return (int) (getActiveChar().getOwner().getMAtk(target, skill) * 0.25);
		}

		double ownerBonus = getActiveChar().getOwner().getMAtk(target, skill);
		double percent = getActiveChar().getOwner().calcStat(Stats.OWNER_MATK, 0, null, null);
		ownerBonus *= percent / 100.0;
		return super.getMAtk(target, skill) + (int) ownerBonus;
	}

	@Override
	public int getMDef(L2Character target, L2Skill skill)
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getMDef(target, skill);
		}

		if (getActiveChar() instanceof L2CloneInstance)
		{
			return (int) (getActiveChar().getOwner().getMDef(target, skill) * 0.25);
		}

		double ownerBonus = getActiveChar().getOwner().getMDef(target, skill);
		double percent = getActiveChar().getOwner().calcStat(Stats.OWNER_MDEF, 0, null, null);
		ownerBonus *= percent / 100.0;
		return super.getMDef(target, skill) + (int) ownerBonus;
	}

	@Override
	public int getMaxVisibleHp()
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getMaxVisibleHp();
		}

		if (getActiveChar() instanceof L2CloneInstance)
		{
			return (int) (getActiveChar().getOwner().getMaxVisibleHp() * 0.25);
		}

		double ownerBonus = getActiveChar().getOwner().getMaxVisibleHp();
		double percent = getActiveChar().getOwner().calcStat(Stats.OWNER_MAXHP, 0, null, null);
		ownerBonus *= percent / 100.0;
		return super.getMaxVisibleHp() + (int) ownerBonus;
	}

	@Override
	public int getMaxMp()
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getMaxMp();
		}

		if (getActiveChar() instanceof L2CloneInstance)
		{
			return (int) (getActiveChar().getOwner().getMaxMp() * 0.25);
		}

		double ownerBonus = getActiveChar().getOwner().getMaxMp();
		double percent = getActiveChar().getOwner().calcStat(Stats.OWNER_MAXMP, 0, null, null);
		ownerBonus *= percent / 100.0;
		return super.getMaxMp() + (int) ownerBonus;
	}

	@Override
	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getCriticalHit(target, skill);
		}

		if (getActiveChar() instanceof L2CloneInstance)
		{
			return getActiveChar().getOwner().getCriticalHit(target, skill);
		}

		double ownerBonus = getActiveChar().getOwner().getCriticalHit(target, skill);
		double percent = getActiveChar().getOwner().calcStat(Stats.OWNER_CRIT, 0, null, null);
		ownerBonus *= percent / 100.0;
		return super.getCriticalHit(target, skill) + (int) ownerBonus;
	}

	@Override
	public double getPCriticalDamage(L2Character target, double damage, L2Skill skill)
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getPCriticalDamage(target, damage, skill);
		}

		double ownerBonus = getActiveChar().getOwner().getPCriticalDamage(target, damage, skill);
		double percent = getActiveChar().getOwner().calcStat(Stats.OWNER_CRIT_DMG, 0, null, null);

		ownerBonus *= percent / 100.0;
		return super.getPCriticalDamage(target, damage + (int) ownerBonus, skill);
	}

	@Override
	public int getPAtkSpd()
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getPAtkSpd();
		}

		if (getActiveChar() instanceof L2CloneInstance)
		{
			return getActiveChar().getOwner().getPAtkSpd();
		}

		double ownerBonus = getActiveChar().getOwner().getPAtkSpd();
		double percent = getActiveChar().getOwner().calcStat(Stats.OWNER_PATKSPD, 0, null, null);
		ownerBonus *= percent / 100.0;
		return super.getPAtkSpd() + (int) ownerBonus;
	}

	@Override
	public int getMAtkSpd()
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getMAtkSpd();
		}

		double ownerBonus = getActiveChar().getOwner().getMAtkSpd();
		double percent = getActiveChar().getOwner().calcStat(Stats.OWNER_MATKSPD, 0, null, null);
		ownerBonus *= percent / 100.0;
		return super.getMAtkSpd() + (int) ownerBonus;
	}

	@Override
	public int getAccuracy()
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getAccuracy();
		}

		if (getActiveChar() instanceof L2CloneInstance)
		{
			return getActiveChar().getOwner().getAccuracy();
		}

		return (int) getActiveChar().getOwner().calcStat(Stats.SERVITOR_ACCURACY, super.getAccuracy(), null, null);
	}

	@Override
	public double getPvPPhysicalDamage(L2Character target)
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getPvPPhysicalDamage(target);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0)
		{
			return getActiveChar().getOwner().getPvPPhysicalDamage(target);
		}

		return 1;
	}

	@Override
	public double getPvPPhysicalSkillDamage(L2Character target)
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getPvPPhysicalSkillDamage(target);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0)
		{
			return getActiveChar().getOwner().getPvPPhysicalSkillDamage(target);
		}

		return 1;
	}

	@Override
	public double getPvPPhysicalDefense(L2Character attacker)
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getPvPPhysicalDefense(attacker);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0)
		{
			return getActiveChar().getOwner().getPvPPhysicalDefense(attacker);
		}

		return 1;
	}

	@Override
	public double getPvPPhysicalSkillDefense(L2Character attacker)
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getPvPPhysicalDefense(attacker);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0)
		{
			return getActiveChar().getOwner().getPvPPhysicalSkillDefense(attacker);
		}

		return 1;
	}

	@Override
	public double getPvPMagicDamage(L2Character target)
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getPvPMagicDamage(target);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0)
		{
			return getActiveChar().getOwner().getPvPMagicDamage(target);
		}

		return 1;
	}

	@Override
	public double getPvPMagicDefense(L2Character attacker)
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getPvPMagicDamage(attacker);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0)
		{
			return getActiveChar().getOwner().getPvPMagicDefense(attacker);
		}

		return 1;
	}

	@Override
	public double getPvEPhysicalSkillDamage(L2Character target)
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getPvPMagicDamage(target);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0)
		{
			return getActiveChar().getOwner().getPvEPhysicalSkillDamage(target);
		}

		return 1;
	}

	@Override
	public double getPvEPhysicalSkillDefense(L2Character attacker)
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getPvPMagicDamage(attacker);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0)
		{
			return getActiveChar().getOwner().getPvEPhysicalSkillDefense(attacker);
		}

		return 1;
	}

	@Override
	public double getPvEPhysicalDamage(L2Character target)
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getPvEPhysicalDamage(target);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0)
		{
			return getActiveChar().getOwner().getPvEPhysicalDamage(target);
		}

		return 1;
	}

	@Override
	public double getPvEPhysicalDefense(L2Character attacker)
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getPvEPhysicalDefense(attacker);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0)
		{
			return getActiveChar().getOwner().getPvEPhysicalDefense(attacker);
		}

		return 1;
	}

	@Override
	public double getPvEMagicDamage(L2Character target)
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getPvEMagicDamage(target);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0)
		{
			return getActiveChar().getOwner().getPvEMagicDamage(target);
		}

		return 1;
	}

	@Override
	public double getPvEMagicDefense(L2Character attacker)
	{
		if (getActiveChar().getOwner() == null)
		{
			return super.getPvEMagicDefense(attacker);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0)
		{
			return getActiveChar().getOwner().getPvEMagicDefense(attacker);
		}

		return 1;
	}
}
