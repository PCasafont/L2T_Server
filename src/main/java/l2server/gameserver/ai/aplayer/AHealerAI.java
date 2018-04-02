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
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.gameserver.templates.skills.SkillType;

/**
 * @author Pere
 */
public class AHealerAI extends APlayerAI {
	public AHealerAI(Creature creature) {
		super(creature);
	}
	
	@Override
	protected int[] getRandomGear() {
		return new int[]{30262, 19917, 19918, 19919, 19920, 19921, 19464, 19463, 19458, 17623, 35570, 34860, 19462, 19454, 35802, 30317};
	}
	
	@Override
	protected boolean interactWith(Creature target) {
		if (player.isAlly(target)) {
			if (target.isDead()) {
				for (Skill skill : player.getAllSkills()) {
					if (skill.getSkillType() != SkillType.RESURRECT || skill.getTargetType() != SkillTargetType.TARGET_ONE) {
						continue;
					}
					
					if (player.useMagic(skill, true, false)) {
						break;
					}
				}
			} else {
				setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
			}
			return true;
		}

		/*if (getIntention() == CtrlIntention.AI_INTENTION_IDLE
				&& (player.getCurrentMp() > player.getMaxMp() * 0.7
				|| player.getCurrentHp() < player.getMaxHp() * 0.5))
		{
			for (Skill skill : player.getAllSkills())
			{
				if (!skill.isOffensive() || skill.getTargetType() != SkillTargetType.TARGET_ONE)
					continue;

				if (player.useMagic(skill, true, false))
					break;
			}
		}*/
		setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
		
		return true;
	}
	
	@Override
	protected void think() {
		super.think();
		
		if (player.getParty() == null) {
			return;
		}
		
		Player mostHarmed = null;
		Player mostDebuffed = null;
		Player dead = null;
		int memberCount = 0;
		int leastHealth = 100;
		int totalHealth = 0;
		int maxDebuffs = 0;
		
		for (Player member : player.getParty().getPartyMembers()) {
			if (player.getDistanceSq(member) > 1500 * 1500) {
				continue;
			}
			
			if (member.isDead()) {
				dead = member;
				continue;
			}
			
			int health = (int) (member.getCurrentHp() * 100 / member.getMaxHp());
			if (health < leastHealth) {
				leastHealth = health;
				mostHarmed = member;
			}
			
			health = (int) ((member.getCurrentHp() + member.getCurrentCp()) * 100 / (member.getMaxHp() + member.getMaxCp()));
			if (health < leastHealth) {
				leastHealth = health;
				mostHarmed = member;
			}
			
			int debuffs = 0;
			for (Abnormal e : member.getAllEffects()) {
				if (e != null && e.getSkill().isDebuff() && e.getSkill().canBeDispeled()) {
					debuffs++;
				}
			}
			
			if (debuffs > maxDebuffs) {
				maxDebuffs = debuffs;
				mostDebuffed = member;
			}
			
			totalHealth += health;
			memberCount++;
		}
		
		//log.info(meanHealth + " " + leastHealth);
		
		if (dead != null) {
			player.setTarget(dead);
			
			for (Skill skill : player.getAllSkills()) {
				if (skill.getSkillType() != SkillType.RESURRECT) {
					continue;
				}
				
				if (player.useMagic(skill, true, false)) {
					return;
				}
			}
		}
		
		if (memberCount == 0) {
			return;
		}
		
		if (mostDebuffed != null && (maxDebuffs > 2 || mostDebuffed.isMovementDisabled() || mostDebuffed.isOutOfControl())) {
			player.setTarget(mostDebuffed);
			
			for (Skill skill : player.getAllSkills()) {
				if (skill.getSkillType() != SkillType.CANCEL_DEBUFF) {
					continue;
				}
				
				if (player.useMagic(skill, true, false)) {
					return;
				}
			}
		}
		
		int meanHealth = totalHealth / memberCount;
		
		//log.info(leastHealth + " " + meanHealth);
		
		if (meanHealth < 85 || leastHealth < 70) {
			player.setTarget(mostHarmed);
			
			if (meanHealth < leastHealth + 25 || meanHealth < 65) {
				for (Skill skill : player.getAllSkills()) {
					if (skill.getSkillType() != SkillType.HEAL && skill.getSkillType() != SkillType.HEAL_STATIC &&
							skill.getSkillType() != SkillType.HEAL_PERCENT && skill.getSkillType() != SkillType.CHAIN_HEAL &&
							skill.getSkillType() != SkillType.OVERHEAL || skill.getTargetType() != SkillTargetType.TARGET_PARTY &&
							skill.getTargetType() != SkillTargetType.TARGET_PARTY_NOTME &&
							skill.getTargetType() != SkillTargetType.TARGET_PARTY_CLAN) {
						continue;
					}
					
					if (player.useMagic(skill, true, false)) {
						return;
					}
				}
			}
			
			if (mostHarmed != null) {
				for (Skill skill : player.getAllSkills()) {
					if (skill.getSkillType() != SkillType.HEAL && skill.getSkillType() != SkillType.HEAL_STATIC &&
							skill.getSkillType() != SkillType.HEAL_PERCENT && skill.getSkillType() != SkillType.CHAIN_HEAL &&
							skill.getSkillType() != SkillType.OVERHEAL || skill.getTargetType() != SkillTargetType.TARGET_ONE &&
							(skill.getTargetType() != SkillTargetType.TARGET_SELF || mostHarmed != player) &&
							(skill.getTargetType() != SkillTargetType.TARGET_PARTY_OTHER || mostHarmed == player) &&
							skill.getTargetType() != SkillTargetType.TARGET_PARTY_MEMBER) {
						continue;
					}
					
					if (player.useMagic(skill, true, false)) {
						return;
					}
				}
			}
		}
	}

	/*private boolean isHealingSkill(Skill skill)
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
