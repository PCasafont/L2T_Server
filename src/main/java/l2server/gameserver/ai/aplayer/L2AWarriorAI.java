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
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.skills.L2SkillTargetType;

/**
 * @author Pere, Soul
 */
public class L2AWarriorAI extends L2APlayerAI
{
	private static final int BEAR_CRY = 10291; // + P. Atk. and P.Critical Damage by 30%.
	private static final int OGRE_CRY = 10292;
	// + P. Atk., P. Def. And M. Def. by 35% and P. Critical Damage and Max HP by 10%.
	private static final int PUMA_CRY = 10293;
	// + P. Accuracy by 10 and Atk. Spd. by 20%, and decreases Critical Damage received by 40%.
	private static final int RABBIT_CRY = 10294; // + Atk. Spd. by 30%, Speed by 35% and P. Evasion by 15.
	private static final int HAWK_CRY = 10295; // + P. Accuracy by 8, Critical Rate by 120 and Critical Damage by 30%.

	private static final int HURRICANE_RUSH = 10267;

	public L2AWarriorAI(L2Character creature)
	{
		super(creature);
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
				35809,
				30311
		};
	}

	@Override
	protected boolean interactWith(L2Character target)
	{
		if (super.interactWith(target))
		{
			return true;
		}

		/*
		 * Decide what totem is better according to situation.
		 * Basic reasoning:
		 *
		 * IDLE or without totem buff: BEAR
		 * Under regular PVP (CP is decreasing): PUMA
		 * Under heavy PVP (HP is decreasing): OGRE
		 * Target is fleeing: RABBIT
		 * Target is Healer : HAWK
		 */
		L2Skill feralCry = null;

		if (getIntention() == CtrlIntention.AI_INTENTION_IDLE || !hasAbnormalType(player, "possession"))
		{
			// On IDLE or if there's no totem on, renew basic Feral Cry (Bear)
			// If there's any totem, just ignore this check.
			feralCry = player.getKnownSkill(BEAR_CRY);
		}
		else if (player.getCurrentCp() < player.getMaxCp() * 0.8)
		{
			// Under first contact, switch to Puma
			feralCry = player.getKnownSkill(PUMA_CRY);
		}
		else if (player.getCurrentHp() < player.getMaxHp() * 0.99)
		{
			// PVP is getting serious, change to Ogre
			feralCry = player.getKnownSkill(OGRE_CRY);
		}
		else if (player.isInsideRadius(target, 1000, true, true))
		{
			// Target is fleeing, let's use Rabbit to catch him
			feralCry = player.getKnownSkill(RABBIT_CRY);
		}
		else if (target instanceof L2PcInstance && ((L2PcInstance) target).getClassId() == 146)
		{
			// Target is a healer, change to Hawk
			feralCry = player.getKnownSkill(HAWK_CRY);
		}

		if (feralCry != null && player.useMagic(feralCry, true, false))
		{
			return true;
		}

		// Now, time to use skillz
		if (player.getCurrentMp() > player.getMaxMp() * 0.7 || player.getCurrentHp() < player.getMaxHp() * 0.5 ||
				player.getTarget() instanceof L2Playable)
		{
			// First, let's try to Rush
			if (target != null && 600 - player.getDistanceSq(target) > 100)
			{
				L2Skill skill = player.getKnownSkill(HURRICANE_RUSH);

				if (skill != null)
				{
					player.useMagic(skill, true, false);
				}
			}

			// Then, let's attack!
			for (L2Skill skill : player.getAllSkills())
			{
				if (!skill.isOffensive() || skill.getTargetType() != L2SkillTargetType.TARGET_ONE)
				{
					continue;
				}

				if (player.useMagic(skill, true, false))
				{
					break;
				}
			}
		}

		setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);

		return true;
	}

	@Override
	protected void think()
	{
		super.think();
	}
}
