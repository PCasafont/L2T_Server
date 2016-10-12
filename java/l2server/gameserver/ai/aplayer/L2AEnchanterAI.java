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
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.templates.skills.L2SkillType;

/**
 * @author Pere
 */
public class L2AEnchanterAI extends L2APlayerAI
{
	private static final int[] SONATAS = {11529, 11530, 11532};
	private static final int ASSAULT_RUSH = 11508;

	public L2AEnchanterAI(AIAccessor accessor)
	{
		super(accessor);
	}

	@Override
	protected int[] getRandomGear()
	{
		return new int[]{
				30267,
				19698,
				19699,
				19700,
				19701,
				19702,
				19464,
				19463,
				19458,
				17623,
				35570,
				34860,
				19462,
				19454,
				35846,
				30316
		};
	}

	@Override
	protected boolean interactWith(L2Character target)
	{
		if (super.interactWith(target))
		{
			return true;
		}

		if (_player.getCurrentMp() > _player.getMaxMp() * 0.7 || _player.getCurrentHp() < _player.getMaxHp() * 0.5 ||
				_player.getTarget() instanceof L2Playable)
		{
			// First, let's try to Rush
			if (target != null && 600 - _player.getDistanceSq(target) > 100)
			{
				L2Skill skill = _player.getKnownSkill(ASSAULT_RUSH);

				if (skill != null)
				{
					_player.useMagic(skill, true, false);
				}
			}

			// Then, let's attack!
			for (L2Skill skill : _player.getAllSkills())
			{
				if (!skill.isOffensive() || skill.getTargetType() != L2SkillTargetType.TARGET_ONE)
				{
					continue;
				}

				if (_player.useMagic(skill, true, false))
				{
					break;
				}
			}
		}

		setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);

		return true;
	}

	private boolean checkBuffs(L2PcInstance partner)
	{
		if (partner.isDead())
		{
			return false;
		}

		// Check the sonatas
		for (int sonata : SONATAS)
		{
			boolean hasBuff = false;
			for (L2Abnormal e : partner.getAllEffects())
			{
				if (e.getSkill().getId() == sonata /*&& e.getTime() > 30*/)
				{
					hasBuff = true;
					break;
				}
			}

			if (!hasBuff)
			{
				L2Skill skill = _player.getKnownSkill(sonata);
				if (skill != null && _player.useMagic(skill, true, false))
				{
					return false;
				}
			}
		}

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

		int memberCount = 0;
		L2PcInstance mostHarmed = null;
		int leastHealth = 100;
		int totalHealth = 0;
		for (L2PcInstance member : _player.getParty().getPartyMembers())
		{
			if (_player.getDistanceSq(member) > 1000 * 1000)
			{
				continue;
			}

			checkBuffs(member);

			int health = (int) (member.getCurrentHp() * 100 / member.getMaxHp());
			if (health < leastHealth)
			{
				leastHealth = health;
				mostHarmed = member;
			}

			totalHealth += health;
			memberCount++;
		}

		int meanHealth = totalHealth / memberCount;

		if (meanHealth < 80 || leastHealth < 60)
		{
			_player.setTarget(mostHarmed);

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
					break;
				}
			}
		}
	}
}
