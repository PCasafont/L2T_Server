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
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Character.AIAccessor;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.templates.skills.L2SkillTargetType;

/**
 * @author Pere
 */
public class L2AArcherAI extends L2APlayerAI
{
	public static final int QUICK_EVASION = 10774;
	public static final int QUICK_CHARGE = 10805;

	public L2AArcherAI(AIAccessor accessor)
	{
		super(accessor);
	}

	@Override
	protected int[] getRandomGear()
	{
		return new int[]{
				30250,
				19912,
				19913,
				19914,
				19915,
				19916,
				19464,
				19463,
				19458,
				17623,
				35570,
				34860,
				19462,
				19454,
				35890,
				30313
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
			if (this.player.isInsideRadius(target, 100, true, true))
			{
				L2Skill skill = this.player.getKnownSkill(QUICK_EVASION);
				if (skill != null && this.player.useMagic(skill, true, false))
				{
					return true;
				}
			}
			else if (!this.player.isInsideRadius(target, 1500, true, true))
			{
				L2Skill skill = this.player.getKnownSkill(QUICK_CHARGE);
				if (skill != null && this.player.useMagic(skill, true, false))
				{
					return true;
				}
			}
		}

		for (L2Character attacker : this.player.getKnownList().getKnownCharactersInRadius(100))
		{
			if (this.player.isEnemy(attacker) && attacker.isAttackingNow() && attacker.getTarget() == this.player)
			{
				L2Skill skill = this.player.getKnownSkill(QUICK_CHARGE);
				if (skill != null && this.player.useMagic(skill, true, false))
				{
					return true;
				}

				skill = this.player.getKnownSkill(QUICK_EVASION);
				if (skill != null && this.player.useMagic(skill, true, false))
				{
					return true;
				}
			}
		}

		if (this.player.getCurrentMp() > player.getMaxMp() * 0.7 || this.player.getCurrentHp() < this.player.getMaxHp() * 0.5 ||
				this.player.getTarget() instanceof L2Playable)
		{
			for (L2Skill skill : this.player.getAllSkills())
			{
				if (!skill.isOffensive() || skill.getTargetType() != L2SkillTargetType.TARGET_ONE)
				{
					continue;
				}

				if (this.player.useMagic(skill, true, false))
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

		L2ItemInstance arrows = this.player.getInventory().getItemByItemId(18550);
		if (arrows == null || arrows.getCount() < 1000)
		{
			this.player.getInventory().addItem("", 18550, 1000, this.player, this.player);
			L2ItemInstance bow = this.player.getActiveWeaponInstance();
			if (bow != null)
			{
				this.player.useEquippableItem(bow, false);
				this.player.useEquippableItem(bow, false);
			}
		}
	}
}
