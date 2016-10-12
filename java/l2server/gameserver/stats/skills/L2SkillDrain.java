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

import l2server.Config;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2CubicInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.StatusUpdate.StatusUpdateDisplay;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.log.Log;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class L2SkillDrain extends L2Skill
{
	private static final Logger _logDamage = Logger.getLogger("damage");

	private float _absorbPart;
	private int _absorbAbs;

	public L2SkillDrain(StatsSet set)
	{
		super(set);

		_absorbPart = set.getFloat("absorbPart", 0.f);
		_absorbAbs = set.getInteger("absorbAbs", 0);
	}

	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}

		for (L2Character target : (L2Character[]) targets)
		{
			if (target.isAlikeDead() && getTargetType() != L2SkillTargetType.TARGET_CORPSE_MOB)
			{
				continue;
			}

			if (activeChar != target && target.isInvul(activeChar))
			{
				continue; // No effect on invulnerable chars unless they cast it themselves.
			}

			double ssMul = L2ItemInstance.CHARGED_NONE;
			L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
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

			boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, this));
			byte shld = Formulas.calcShldUse(activeChar, target, this);
			int damage = (int) Formulas.calcMagicDam(activeChar, target, this, shld, ssMul, mcrit);

			int drain = 0;
			int curCp = (int) target.getCurrentCp();
			int curHp = (int) target.getCurrentHp();

			/*if (!((Config.isServer(Config.PVP))
					&& activeChar instanceof L2PcInstance
					&& target instanceof L2MonsterInstance
					&& ((L2PcInstance)activeChar).getPvpFlag() > 0))*/
			{
				if (curCp > 0)
				{
					if (damage < curCp)
					{
						drain = 0;
					}
					else
					{
						drain = damage - curCp;
					}
				}
				else if (damage > curHp)
				{
					drain = curHp;
				}
				else
				{
					drain = damage;
				}
			}

			double hpAdd = _absorbAbs + _absorbPart * drain;
			double hp = activeChar.getCurrentHp() + hpAdd > activeChar.getMaxHp() ? activeChar.getMaxHp() :
					activeChar.getCurrentHp() + hpAdd;

			activeChar.setCurrentHp(hp);

			if (getId() == 1245)
			{
				int mpAdd = (int) (damage * 0.05);
				double mp = activeChar.getCurrentMp() + mpAdd > activeChar.getMaxMp() ? activeChar.getMaxMp() :
						activeChar.getCurrentMp() + mpAdd;

				activeChar.setCurrentMp(mp);

				StatusUpdate suhp = new StatusUpdate(activeChar, target, StatusUpdateDisplay.NORMAL);
				suhp.addAttribute(StatusUpdate.CUR_MP, (int) mp);
				activeChar.sendPacket(suhp);
			}

			StatusUpdate suhp = new StatusUpdate(activeChar, target, StatusUpdateDisplay.NORMAL);
			suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
			activeChar.sendPacket(suhp);

			// Check to see if we should damage the target
			if (damage > 0 && (!target.isDead() || getTargetType() != L2SkillTargetType.TARGET_CORPSE_MOB))
			{
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}

				activeChar.sendDamageMessage(target, damage, mcrit, false, false);

				if (Config.LOG_GAME_DAMAGE && activeChar instanceof L2Playable &&
						damage > Config.LOG_GAME_DAMAGE_THRESHOLD)
				{
					LogRecord record = new LogRecord(Level.INFO, "");
					record.setParameters(new Object[]{activeChar, " did damage ", damage, this, " to ", target});
					record.setLoggerName("mdam");
					_logDamage.log(record);
				}

				if (hasEffects() && getTargetType() != L2SkillTargetType.TARGET_CORPSE_MOB)
				{
					// ignoring vengeance-like reflections
					if ((Formulas.calcSkillReflect(target, this) & Formulas.SKILL_REFLECT_EFFECTS) > 0)
					{
						//activeChar.stopSkillEffects(getId());
						getEffects(target, activeChar);
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(getId());
						activeChar.sendPacket(sm);
					}
					else
					{
						// activate attacked effects, if any
						//target.stopSkillEffects(getId());
						if (Formulas.calcSkillSuccess(activeChar, target, this, shld, ssMul))
						{
							getEffects(activeChar, target);
						}
						else
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
							sm.addCharName(target);
							sm.addSkillName(this);
							activeChar.sendPacket(sm);
						}
					}
				}

				target.reduceCurrentHp(damage, activeChar, this);
			}

			// Check to see if we should do the decay right after the cast
			if (target.isDead() && getTargetType() == L2SkillTargetType.TARGET_CORPSE_MOB && target instanceof L2Npc)
			{
				((L2Npc) target).endDecayTask();
			}
		}
		//effect self :]
        /*L2Abnormal effect = activeChar.getFirstEffect(getId());
		if (effect != null && effect.isSelfEffect())
		{
			//Replace old effect with new one.
			effect.exit();
		}*/
		// cast self effect if any
		getEffectsSelf(activeChar);
	}

	public void useCubicSkill(L2CubicInstance activeCubic, L2Object[] targets)
	{
		if (Config.DEBUG)
		{
			Log.info("L2SkillDrain: useCubicSkill()");
		}

		for (L2Character target : (L2Character[]) targets)
		{
			if (target.isAlikeDead() && getTargetType() != L2SkillTargetType.TARGET_CORPSE_MOB)
			{
				continue;
			}

			boolean mcrit = Formulas.calcMCrit(activeCubic.getMCriticalHit(target, this));
			byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, this);

			int damage = (int) Formulas.calcMagicDam(activeCubic, target, this, mcrit, shld);
			if (Config.DEBUG)
			{
				Log.info("L2SkillDrain: useCubicSkill() -> damage = " + damage);
			}

			double hpAdd = _absorbAbs + _absorbPart * damage;
			L2PcInstance owner = activeCubic.getOwner();
			double hp =
					owner.getCurrentHp() + hpAdd > owner.getMaxHp() ? owner.getMaxHp() : owner.getCurrentHp() + hpAdd;

			owner.setCurrentHp(hp);

			StatusUpdate suhp = new StatusUpdate(owner);
			suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
			owner.sendPacket(suhp);

			// Check to see if we should damage the target
			if (damage > 0 && (!target.isDead() || getTargetType() != L2SkillTargetType.TARGET_CORPSE_MOB))
			{
				target.reduceCurrentHp(damage, activeCubic.getOwner(), this);

				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				owner.sendDamageMessage(target, damage, mcrit, false, false);
			}
		}
	}
}
