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

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Character.AIAccessor;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.templates.skills.L2SkillType;

/**
 * @author Pere
 */
public class L2AHealerAI extends L2APlayerAI
{
	public L2AHealerAI(AIAccessor accessor)
	{
		super(accessor);
	}

	@Override
	protected int[] getRandomGear()
	{
		return new int[]{
				30262,
				19917,
				19918,
				19919,
				19920,
				19921,
				19464,
				19463,
				19458,
				17623,
				35570,
				34860,
				19462,
				19454,
				35802,
				30317
		};
	}

	@Override
	protected boolean interactWith(L2Character target)
	{
		if (_player.isAlly(target))
		{
			if (target.isDead())
			{
				for (L2Skill skill : _player.getAllSkills())
				{
					if (skill.getSkillType() != L2SkillType.RESURRECT ||
							skill.getTargetType() != L2SkillTargetType.TARGET_ONE)
					{
						continue;
					}

					if (_player.useMagic(skill, true, false))
					{
						break;
					}
				}
			}
			else
			{
				setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
			}
			return true;
		}

		/*if (getIntention() == CtrlIntention.AI_INTENTION_IDLE
				&& (_player.getCurrentMp() > _player.getMaxMp() * 0.7
				|| _player.getCurrentHp() < _player.getMaxHp() * 0.5))
		{
			for (L2Skill skill : _player.getAllSkills())
			{
				if (!skill.isOffensive() || skill.getTargetType() != L2SkillTargetType.TARGET_ONE)
					continue;

				if (_player.useMagic(skill, true, false))
					break;
			}
		}*/
		setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);

		return true;
	}

	@Override
	protected void think()
	{
		super.think();

		if (_player.getParty() == null)
		{
			return;
		}

		L2PcInstance mostHarmed = null;
		L2PcInstance mostDebuffed = null;
		L2PcInstance dead = null;
		int memberCount = 0;
		int leastHealth = 100;
		int totalHealth = 0;
		int maxDebuffs = 0;

		for (L2PcInstance member : _player.getParty().getPartyMembers())
		{
			if (_player.getDistanceSq(member) > 1500 * 1500)
			{
				continue;
			}

			if (member.isDead())
			{
				dead = member;
				continue;
			}

			int health = (int) (member.getCurrentHp() * 100 / member.getMaxHp());
			if (health < leastHealth)
			{
				leastHealth = health;
				mostHarmed = member;
			}

			health = (int) ((member.getCurrentHp() + member.getCurrentCp()) * 100 /
					(member.getMaxHp() + member.getMaxCp()));
			if (health < leastHealth)
			{
				leastHealth = health;
				mostHarmed = member;
			}

			int debuffs = 0;
			for (L2Abnormal e : member.getAllEffects())
			{
				if (e != null && e.getSkill().isDebuff() && e.getSkill().canBeDispeled())
				{
					debuffs++;
				}
			}

			if (debuffs > maxDebuffs)
			{
				maxDebuffs = debuffs;
				mostDebuffed = member;
			}

			totalHealth += health;
			memberCount++;
		}

		//Log.info(meanHealth + " " + leastHealth);

		if (dead != null)
		{
			_player.setTarget(dead);

			for (L2Skill skill : _player.getAllSkills())
			{
				if (skill.getSkillType() != L2SkillType.RESURRECT)
				{
					continue;
				}

				if (_player.useMagic(skill, true, false))
				{
					return;
				}
			}
		}

		if (memberCount == 0)
		{
			return;
		}

		if (mostDebuffed != null &&
				(maxDebuffs > 2 || mostDebuffed.isMovementDisabled() || mostDebuffed.isOutOfControl()))
		{
			_player.setTarget(mostDebuffed);

			for (L2Skill skill : _player.getAllSkills())
			{
				if (skill.getSkillType() != L2SkillType.CANCEL_DEBUFF)
				{
					continue;
				}

				if (_player.useMagic(skill, true, false))
				{
					return;
				}
			}
		}

		int meanHealth = totalHealth / memberCount;

		//Log.info(leastHealth + " " + meanHealth);

		if (meanHealth < 85 || leastHealth < 70)
		{
			_player.setTarget(mostHarmed);

			if (meanHealth < leastHealth + 25 || meanHealth < 65)
			{
				for (L2Skill skill : _player.getAllSkills())
				{
					if (skill.getSkillType() != L2SkillType.HEAL && skill.getSkillType() != L2SkillType.HEAL_STATIC &&
							skill.getSkillType() != L2SkillType.HEAL_PERCENT &&
							skill.getSkillType() != L2SkillType.CHAIN_HEAL &&
							skill.getSkillType() != L2SkillType.OVERHEAL ||
							skill.getTargetType() != L2SkillTargetType.TARGET_PARTY &&
									skill.getTargetType() != L2SkillTargetType.TARGET_PARTY_NOTME &&
									skill.getTargetType() != L2SkillTargetType.TARGET_PARTY_CLAN)
					{
						continue;
					}

					if (_player.useMagic(skill, true, false))
					{
						return;
					}
				}
			}

			if (mostHarmed != null)
			{
				for (L2Skill skill : _player.getAllSkills())
				{
					if (skill.getSkillType() != L2SkillType.HEAL && skill.getSkillType() != L2SkillType.HEAL_STATIC &&
							skill.getSkillType() != L2SkillType.HEAL_PERCENT &&
							skill.getSkillType() != L2SkillType.CHAIN_HEAL &&
							skill.getSkillType() != L2SkillType.OVERHEAL ||
							skill.getTargetType() != L2SkillTargetType.TARGET_ONE &&
									(skill.getTargetType() != L2SkillTargetType.TARGET_SELF || mostHarmed != _player) &&
									(skill.getTargetType() != L2SkillTargetType.TARGET_PARTY_OTHER ||
											mostHarmed == _player) &&
									skill.getTargetType() != L2SkillTargetType.TARGET_PARTY_MEMBER)
					{
						continue;
					}

					if (_player.useMagic(skill, true, false))
					{
						return;
					}
				}
			}
		}
	}

	/*private boolean isHealingSkill(L2Skill skill)
	{
		switch(skill.getSkillType())
		{
			case HEAL:
			case HEAL_STATIC:
			case HEAL_PERCENT:
			case CHAIN_HEAL:
			case OVERHEAL:
				return true;
			default:
				return false;
		}
	}*/
}
