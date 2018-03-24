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

package handlers.skillhandlers;

import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.templates.skills.L2SkillType;

/**
 * Class handling the Mana damage skill
 *
 * @author slyce
 */
public class Manadam implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {L2SkillType.MANADAM};

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(l2server.gameserver.model.actor.L2Character, l2server.gameserver.model.L2Skill, l2server.gameserver.model.L2Object[])
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}

		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		double ssMul = L2ItemInstance.CHARGED_NONE;
		if (weaponInst != null)
		{
			if (skill.isMagic())
			{
				ssMul = weaponInst.getChargedSpiritShot();
				weaponInst.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
			else
			{
				ssMul = weaponInst.getChargedSoulShot();
				weaponInst.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
			}
		}
		// If there is no weapon equipped, check for an active summon.
		else if (activeChar instanceof L2Summon)
		{
			L2Summon activeSummon = (L2Summon) activeChar;
			if (skill.isMagic())
			{
				ssMul = activeSummon.getChargedSpiritShot();
				activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
			else
			{
				ssMul = activeSummon.getChargedSoulShot();
				activeSummon.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
			}
		}
		for (L2Character target : (L2Character[]) targets)
		{
			if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_EFFECTS)
			{
				target = activeChar;
			}

			boolean acted = Formulas.calcMagicSuccess(activeChar, target, skill);
			if (target.isInvul(activeChar) || !acted ||
					target.getFaceoffTarget() != null && target.getFaceoffTarget() != activeChar)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MISSED_TARGET));
			}
			else
			{
				if (skill.hasEffects())
				{
					byte shld = Formulas.calcShldUse(activeChar, target, skill);
					//target.stopSkillEffects(skill.getId());
					if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, ssMul))
					{
						skill.getEffects(activeChar, target, new Env(shld, ssMul));
					}
					else
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
						sm.addCharName(target);
						sm.addSkillName(skill);
						activeChar.sendPacket(sm);
					}
				}

				double damage = Formulas.calcManaDam(activeChar, target, skill, ssMul);

				if (Formulas.calcMCrit(activeChar.getMCriticalHit(target, skill)))
				{
					damage *= 3.;
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRITICAL_HIT_MAGIC));
				}

				double mp = damage > target.getCurrentMp() ? target.getCurrentMp() : damage;
				target.reduceCurrentMp(mp);
				if (damage > 0)
				{
					target.stopEffectsOnDamage(true, 1);
				}

				if (target instanceof L2PcInstance)
				{
					StatusUpdate sump = new StatusUpdate(target);
					sump.addAttribute(StatusUpdate.CUR_MP, (int) target.getCurrentMp());
					// [L2J_JP EDIT START - TSL]
					target.sendPacket(sump);

					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MP_HAS_BEEN_DRAINED_BY_C1);
					sm.addCharName(activeChar);
					sm.addNumber((int) mp);
					target.sendPacket(sm);
				}

				if (activeChar instanceof L2PcInstance)
				{
					SystemMessage sm2 =
							SystemMessage.getSystemMessage(SystemMessageId.YOUR_OPPONENTS_MP_WAS_REDUCED_BY_S1);
					sm2.addNumber((int) mp);
					activeChar.sendPacket(sm2);
				}
				// [L2J_JP EDIT END - TSL]
			}
		}

		if (skill.hasSelfEffects())
		{
			L2Abnormal effect = activeChar.getFirstEffect(skill.getId());
			if (effect != null && effect.isSelfEffect())
			{
				//Replace old effect with new one.
				effect.exit();
			}
			// cast self effect if any
			skill.getEffectsSelf(activeChar);
		}
	}

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
