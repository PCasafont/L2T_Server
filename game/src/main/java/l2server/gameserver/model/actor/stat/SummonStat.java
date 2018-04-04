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

import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.CloneInstance;
import l2server.gameserver.stats.Stats;

public class SummonStat extends PlayableStat {
	public SummonStat(Summon activeChar) {
		super(activeChar);
	}

	@Override
	public Summon getActiveChar() {
		return (Summon) super.getActiveChar();
	}

	@Override
	public int getPAtk(Creature target) {
		if (getActiveChar().getOwner() == null) {
			return super.getPAtk(target);
		}

		if (getActiveChar() instanceof CloneInstance) {
			return getActiveChar().getOwner().getPAtk(target);
		}

		double ownerBonus = getActiveChar().getOwner().getPAtk(target);
		double percent = getActiveChar().getOwner().calcStat(Stats.OWNER_PATK, 0, null, null);
		ownerBonus *= percent / 100.0;
		return super.getPAtk(target) + (int) ownerBonus;
	}

	@Override
	public int getPDef(Creature target) {
		if (getActiveChar().getOwner() == null) {
			return super.getPAtkSpd();
		}

		if (getActiveChar() instanceof CloneInstance) {
			return (int) (getActiveChar().getOwner().getPDef(target) * 0.25);
		}

		double ownerBonus = getActiveChar().getOwner().getPDef(target);
		double percent = getActiveChar().getOwner().calcStat(Stats.OWNER_PDEF, 0, null, null);
		ownerBonus *= percent / 100.0;
		return super.getPDef(target) + (int) ownerBonus;
	}

	@Override
	public int getMAtk(Creature target, Skill skill) {
		if (getActiveChar().getOwner() == null) {
			return super.getMAtk(target, skill);
		}

		if (getActiveChar() instanceof CloneInstance) {
			return (int) (getActiveChar().getOwner().getMAtk(target, skill) * 0.25);
		}

		double ownerBonus = getActiveChar().getOwner().getMAtk(target, skill);
		double percent = getActiveChar().getOwner().calcStat(Stats.OWNER_MATK, 0, null, null);
		ownerBonus *= percent / 100.0;
		return super.getMAtk(target, skill) + (int) ownerBonus;
	}

	@Override
	public int getMDef(Creature target, Skill skill) {
		if (getActiveChar().getOwner() == null) {
			return super.getMDef(target, skill);
		}

		if (getActiveChar() instanceof CloneInstance) {
			return (int) (getActiveChar().getOwner().getMDef(target, skill) * 0.25);
		}

		double ownerBonus = getActiveChar().getOwner().getMDef(target, skill);
		double percent = getActiveChar().getOwner().calcStat(Stats.OWNER_MDEF, 0, null, null);
		ownerBonus *= percent / 100.0;
		return super.getMDef(target, skill) + (int) ownerBonus;
	}

	@Override
	public int getMaxVisibleHp() {
		if (getActiveChar().getOwner() == null) {
			return super.getMaxVisibleHp();
		}

		if (getActiveChar() instanceof CloneInstance) {
			return (int) (getActiveChar().getOwner().getMaxVisibleHp() * 0.25);
		}

		double ownerBonus = getActiveChar().getOwner().getMaxVisibleHp();
		double percent = getActiveChar().getOwner().calcStat(Stats.OWNER_MAXHP, 0, null, null);
		ownerBonus *= percent / 100.0;
		return super.getMaxVisibleHp() + (int) ownerBonus;
	}

	@Override
	public int getMaxMp() {
		if (getActiveChar().getOwner() == null) {
			return super.getMaxMp();
		}

		if (getActiveChar() instanceof CloneInstance) {
			return (int) (getActiveChar().getOwner().getMaxMp() * 0.25);
		}

		double ownerBonus = getActiveChar().getOwner().getMaxMp();
		double percent = getActiveChar().getOwner().calcStat(Stats.OWNER_MAXMP, 0, null, null);
		ownerBonus *= percent / 100.0;
		return super.getMaxMp() + (int) ownerBonus;
	}

	@Override
	public int getCriticalHit(Creature target, Skill skill) {
		if (getActiveChar().getOwner() == null) {
			return super.getCriticalHit(target, skill);
		}

		if (getActiveChar() instanceof CloneInstance) {
			return getActiveChar().getOwner().getCriticalHit(target, skill);
		}

		double ownerBonus = getActiveChar().getOwner().getCriticalHit(target, skill);
		double percent = getActiveChar().getOwner().calcStat(Stats.OWNER_CRIT, 0, null, null);
		ownerBonus *= percent / 100.0;
		return super.getCriticalHit(target, skill) + (int) ownerBonus;
	}

	@Override
	public double getPCriticalDamage(Creature target, double damage, Skill skill) {
		if (getActiveChar().getOwner() == null) {
			return super.getPCriticalDamage(target, damage, skill);
		}

		double ownerBonus = getActiveChar().getOwner().getPCriticalDamage(target, damage, skill);
		double percent = getActiveChar().getOwner().calcStat(Stats.OWNER_CRIT_DMG, 0, null, null);

		ownerBonus *= percent / 100.0;
		return super.getPCriticalDamage(target, damage + (int) ownerBonus, skill);
	}

	@Override
	public int getPAtkSpd() {
		if (getActiveChar().getOwner() == null) {
			return super.getPAtkSpd();
		}

		if (getActiveChar() instanceof CloneInstance) {
			return getActiveChar().getOwner().getPAtkSpd();
		}

		double ownerBonus = getActiveChar().getOwner().getPAtkSpd();
		double percent = getActiveChar().getOwner().calcStat(Stats.OWNER_PATKSPD, 0, null, null);
		ownerBonus *= percent / 100.0;
		return super.getPAtkSpd() + (int) ownerBonus;
	}

	@Override
	public int getMAtkSpd() {
		if (getActiveChar().getOwner() == null) {
			return super.getMAtkSpd();
		}

		double ownerBonus = getActiveChar().getOwner().getMAtkSpd();
		double percent = getActiveChar().getOwner().calcStat(Stats.OWNER_MATKSPD, 0, null, null);
		ownerBonus *= percent / 100.0;
		return super.getMAtkSpd() + (int) ownerBonus;
	}

	//Code to give pets physical acc
	@Override
	public int getAccuracy() {
		if (getActiveChar().getOwner() == null) {
			return super.getAccuracy();
		}

		if (getActiveChar() instanceof CloneInstance) {
			return getActiveChar().getOwner().getAccuracy();
		}

		return (int) getActiveChar().getOwner().calcStat(Stats.SERVITOR_ACCURACY, super.getAccuracy(), null, null);
	}

	//code to give pets magic Acc
	@Override
	public int getMAccuracy() {
		if (getActiveChar().getOwner() == null) {
			return super.getMAccuracy();
		}

		if (getActiveChar() instanceof CloneInstance) {
			return getActiveChar().getOwner().getMAccuracy();
		}

		return (int) getActiveChar().getOwner().calcStat(Stats.OWNER_ACCURACY_MAGIC, super.getMAccuracy(), null, null);
	}

	//code to give pets physical evasion
	public int getEvasionRate(Creature target) {
		if (getActiveChar().getOwner() == null) {
			return super.getEvasionRate(target);
		}

		if (getActiveChar() instanceof CloneInstance)

		{
			return getActiveChar().getOwner().getEvasionRate(target);
		}
		return (int) getActiveChar().getOwner().calcStat(Stats.OWNER_P_EVASION_RATE, super.getEvasionRate(target), null, null);
	}

	//code to give pets magic evasion
	public int getMEvasionRate(Creature target) {
		if (getActiveChar().getOwner() == null) {
			return super.getMEvasionRate(target);
		}

		if (getActiveChar() instanceof CloneInstance)

		{
			return getActiveChar().getOwner().getMEvasionRate(target);
		}
		return (int) getActiveChar().getOwner().calcStat(Stats.OWNER_M_EVASION_RATE, super.getMEvasionRate(target), null, null);
	}

	@Override
	public double getPvPPhysicalDamage(Creature target) {
		if (getActiveChar().getOwner() == null) {
			return super.getPvPPhysicalDamage(target);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0) {
			return getActiveChar().getOwner().getPvPPhysicalDamage(target);
		}

		return 1;
	}

	@Override
	public double getPvPPhysicalSkillDamage(Creature target) {
		if (getActiveChar().getOwner() == null) {
			return super.getPvPPhysicalSkillDamage(target);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0) {
			return getActiveChar().getOwner().getPvPPhysicalSkillDamage(target);
		}

		return 1;
	}

	@Override
	public double getPvPPhysicalDefense(Creature attacker) {
		if (getActiveChar().getOwner() == null) {
			return super.getPvPPhysicalDefense(attacker);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0) {
			return getActiveChar().getOwner().getPvPPhysicalDefense(attacker);
		}

		return 1;
	}

	@Override
	public double getPvPPhysicalSkillDefense(Creature attacker) {
		if (getActiveChar().getOwner() == null) {
			return super.getPvPPhysicalDefense(attacker);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0) {
			return getActiveChar().getOwner().getPvPPhysicalSkillDefense(attacker);
		}

		return 1;
	}

	@Override
	public double getPvPMagicDamage(Creature target) {
		if (getActiveChar().getOwner() == null) {
			return super.getPvPMagicDamage(target);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0) {
			return getActiveChar().getOwner().getPvPMagicDamage(target);
		}

		return 1;
	}

	@Override
	public double getPvPMagicDefense(Creature attacker) {
		if (getActiveChar().getOwner() == null) {
			return super.getPvPMagicDamage(attacker);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0) {
			return getActiveChar().getOwner().getPvPMagicDefense(attacker);
		}

		return 1;
	}

	@Override
	public double getPvEPhysicalSkillDamage(Creature target) {
		if (getActiveChar().getOwner() == null) {
			return super.getPvPMagicDamage(target);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0) {
			return getActiveChar().getOwner().getPvEPhysicalSkillDamage(target);
		}

		return 1;
	}

	@Override
	public double getPvEPhysicalSkillDefense(Creature attacker) {
		if (getActiveChar().getOwner() == null) {
			return super.getPvPMagicDamage(attacker);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0) {
			return getActiveChar().getOwner().getPvEPhysicalSkillDefense(attacker);
		}

		return 1;
	}

	@Override
	public double getPvEPhysicalDamage(Creature target) {
		if (getActiveChar().getOwner() == null) {
			return super.getPvEPhysicalDamage(target);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0) {
			return getActiveChar().getOwner().getPvEPhysicalDamage(target);
		}

		return 1;
	}

	@Override
	public double getPvEPhysicalDefense(Creature attacker) {
		if (getActiveChar().getOwner() == null) {
			return super.getPvEPhysicalDefense(attacker);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0) {
			return getActiveChar().getOwner().getPvEPhysicalDefense(attacker);
		}

		return 1;
	}

	@Override
	public double getPvEMagicDamage(Creature target) {
		if (getActiveChar().getOwner() == null) {
			return super.getPvEMagicDamage(target);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0) {
			return getActiveChar().getOwner().getPvEMagicDamage(target);
		}

		return 1;
	}

	@Override
	public double getPvEMagicDefense(Creature attacker) {
		if (getActiveChar().getOwner() == null) {
			return super.getPvEMagicDefense(attacker);
		}

		if (getActiveChar().getOwner().calcStat(Stats.OWNER_PVP_PVE, 0.0, getActiveChar().getOwner(), null) > 0.0) {
			return getActiveChar().getOwner().getPvEMagicDefense(attacker);
		}

		return 1;
	}
}
