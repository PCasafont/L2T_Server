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

package l2server.gameserver.stats.effects;

import l2server.Config;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.templates.skills.L2EffectTemplate;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.gameserver.util.Util;

public class EffectTreeOfLife extends L2Effect
{
	public EffectTreeOfLife(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	// Special constructor to steal this effect
	public EffectTreeOfLife(Env env, L2Effect effect)
	{
		super(env, effect);
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		double hp = getSkill().getPower();
		double cp = 0;

		L2Character target = getEffected();

		L2Character activeChar = getEffector();

		if (target == null || target.isDead() || target.isInvul(activeChar) ||
				!Util.checkIfInRange(600, activeChar, target, true))
		{
			return false;
		}

		// No healing from others for player in duels
		if (Config.isServer(Config.TENKAI) && target instanceof L2PcInstance && target.getActingPlayer().isInDuel() &&
				target.getObjectId() != activeChar.getObjectId())
		{
			return false;
		}

		if (target != activeChar)
		{
			// Player holding a cursed weapon can't be healed and can't heal
			if (target instanceof L2PcInstance && ((L2PcInstance) target).isCursedWeaponEquipped())
			{
				return false;
			}
			else if (activeChar instanceof L2PcInstance && ((L2PcInstance) activeChar).isCursedWeaponEquipped())
			{
				return false;
			}

			// Nor all vs all event player
			if (activeChar instanceof L2PcInstance && ((L2PcInstance) activeChar).isPlayingEvent() &&
					((L2PcInstance) activeChar).getEvent().getConfig().isAllVsAll())
			{
				return false;
			}
		}

		// Healer proficiency (since CT1)
		hp = activeChar.calcStat(Stats.HEAL_PROFICIENCY, hp, null, null);

		// Extra bonus (since CT1.5)
		if (getSkill().isPotion())
		{
			hp += target.calcStat(Stats.HEAL_STATIC_BONUS, 0, null, null);
		}

		// Heal critic, since CT2.3 Gracia Final
		if (getSkill().getSkillType() == L2SkillType.HEAL && !getSkill().isPotion() &&
				Formulas.calcMCrit(activeChar.getMCriticalHit(target, getSkill())))
		{
			hp *= 3;
		}

		// from CT2 u will receive exact HP, u can't go over it, if u have full HP and u get HP buff, u will receive 0HP restored message
		// Soul: but from GoD onwards that "overheal" factor is converted into CP by some Areoe Healer skills
		if (target.getCurrentHp() + hp >= target.getMaxHp())
		{
			cp = hp + target.getMaxHp() - target.getCurrentHp();

			if (target.getCurrentCp() + cp >= target.getMaxCp())
			{
				cp = target.getMaxCp() - target.getCurrentCp();
			}

			hp = target.getMaxHp() - target.getCurrentHp();
		}

		if (hp < 0)
		{
			hp = 0;
		}

		target.setCurrentHp(hp + target.getCurrentHp());
		StatusUpdate su = new StatusUpdate(target);
		su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
		target.sendPacket(su);

		if (cp < 0)
		{
			cp = 0;
		}

		if (cp > 0) // TODO: needs retail confirmation, but technically correct
		{
			target.setCurrentCp(cp + target.getCurrentCp());
			su = new StatusUpdate(target);
			su.addAttribute(StatusUpdate.CUR_CP, (int) target.getCurrentCp());
			target.sendPacket(su);
		}

		if (target instanceof L2PcInstance)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_C1);
			sm.addString(activeChar.getName());
			sm.addNumber((int) hp);
			sm.addHpChange(target.getObjectId(), activeChar.getObjectId(), (int) hp);
			target.sendPacket(sm);

			sm = SystemMessage.getSystemMessage(SystemMessageId.S2_CP_WILL_BE_RESTORED_BY_C1);
			sm.addString(activeChar.getName());
			sm.addNumber((int) cp);
			target.sendPacket(sm);
		}
		return true;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		return true;
	}
}
