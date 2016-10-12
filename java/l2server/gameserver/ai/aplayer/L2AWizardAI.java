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

package l2server.gameserver.ai.aplayer;

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Character.AIAccessor;
import l2server.gameserver.templates.skills.L2SkillTargetType;

/**
 * @author Pere
 */
public class L2AWizardAI extends L2APlayerAI
{
	public static final int MAGICAL_CHARGE = 11094;
	public static final int MAGICAL_EVASION = 11057;

	//11007 + 3
	public L2AWizardAI(AIAccessor accessor)
	{
		super(accessor);
	}

	@Override
	protected int[] getRandomGear()
	{
		return new int[]{
				30259,
				19709,
				19710,
				19711,
				19712,
				19713,
				18099,
				19464,
				19463,
				19458,
				17623,
				35570,
				34860,
				19462,
				19454,
				35813,
				30314
		};
	}

	@Override
	protected boolean interactWith(L2Character target)
	{
		if (super.interactWith(target))
		{
			return true;
		}

		if (target != null)
		{
			if (_player.isInsideRadius(target, 100, true, true))
			{
				L2Skill skill = _player.getKnownSkill(MAGICAL_EVASION);
				if (skill != null && _player.useMagic(skill, true, false))
				{
					return true;
				}
			}
			else if (!_player.isInsideRadius(target, 1000, true, true))
			{
				L2Skill skill = _player.getKnownSkill(MAGICAL_CHARGE);
				if (skill != null && _player.useMagic(skill, true, false))
				{
					return true;
				}
			}
		}

		for (L2Character attacker : _player.getKnownList().getKnownCharactersInRadius(100))
		{
			if (_player.isEnemy(attacker) && attacker.isAttackingNow() && attacker.getTarget() == _player)
			{
				L2Skill skill = _player.getKnownSkill(MAGICAL_CHARGE);
				if (skill != null && _player.useMagic(skill, true, false))
				{
					return true;
				}

				skill = _player.getKnownSkill(MAGICAL_EVASION);
				if (skill != null && _player.useMagic(skill, true, false))
				{
					return true;
				}
			}
		}

		//if (_player.getCurrentMp() > _player.getMaxMp() * 0.7
		//		|| _player.getCurrentHp() < _player.getMaxHp() * 0.5
		//		|| _player.getTarget() instanceof L2Playable)
		{
			for (L2Skill skill : _player.getAllSkills())
			{
				if (!skill.isOffensive() || skill.getTargetType() != L2SkillTargetType.TARGET_ONE ||
						skill.getHitTime() > 5000)
				{
					continue;
				}

				if (_player.useMagic(skill, true, false))
				{
					break;
				}
			}

			for (L2Skill skill : _player.getAllSkills())
			{
				if (!skill.isStanceSwitch())
				{
					continue;
				}

				_player.setElementalStance(1);

				int stance = _player.getElementalStance();
				if (stance > 4)
				{
					stance = 5;
				}

				L2Skill magic = SkillTable.getInstance().getInfo(skill.getId() + stance, skill.getLevelHash());
				if (magic.getPower() > 150 && magic.getTargetType() == L2SkillTargetType.TARGET_ONE &&
						_player.useMagic(magic, true, false))
				{
					break;
				}
			}
		}

		return true;
	}

	@Override
	protected void think()
	{
		super.think();
	}
}
