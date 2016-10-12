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

package l2server.gameserver.stats.skills;

import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.templates.StatsSet;

public class L2SkillElemental extends L2Skill
{

	private final int[] _seeds;
	private final boolean _seedAny;

	public L2SkillElemental(StatsSet set)
	{
		super(set);

		_seeds = new int[3];
		_seeds[0] = set.getInteger("seed1", 0);
		_seeds[1] = set.getInteger("seed2", 0);
		_seeds[2] = set.getInteger("seed3", 0);

		_seedAny = set.getInteger("seed_any", 0) == 1;
	}

	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}

		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

		if (activeChar instanceof L2PcInstance)
		{
			if (weaponInst == null)
			{
				activeChar.sendMessage("You must equip your weapon before casting a spell.");
				return;
			}
		}

		double ssMul = L2ItemInstance.CHARGED_NONE;
		if (weaponInst != null)
		{
			ssMul = weaponInst.getChargedSpiritShot();
			weaponInst.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
		}
		else if (activeChar instanceof L2Summon)
		{
			L2Summon activeSummon = (L2Summon) activeChar;
			ssMul = activeSummon.getChargedSpiritShot();
			activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
		}

		for (L2Character target : (L2Character[]) targets)
		{
			if (target.isAlikeDead())
			{
				continue;
			}

			boolean charged = true;
			if (!_seedAny)
			{
				for (int _seed : _seeds)
				{
					if (_seed != 0)
					{
						L2Abnormal e = target.getFirstEffect(_seed);
						if (e == null || !e.getInUse())
						{
							charged = false;
							break;
						}
					}
				}
			}
			else
			{
				charged = false;
				for (int _seed : _seeds)
				{
					if (_seed != 0)
					{
						L2Abnormal e = target.getFirstEffect(_seed);
						if (e != null && e.getInUse())
						{
							charged = true;
							break;
						}
					}
				}
			}
			if (!charged)
			{
				activeChar.sendMessage("Target is not charged by elements.");
				continue;
			}

			boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, this));
			byte shld = Formulas.calcShldUse(activeChar, target, this);

			int damage = (int) Formulas.calcMagicDam(activeChar, target, this, shld, ssMul, mcrit);

			if (damage > 0)
			{
				target.reduceCurrentHp(damage, activeChar, this);

				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}

				activeChar.sendDamageMessage(target, damage, false, false, false);
			}

			// activate attacked effects, if any
			//target.stopSkillEffects(getId());
			if (damage > 1)
			{
				getEffects(activeChar, target, new Env(shld, ssMul));
			}
		}
	}
}
