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
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.StatusUpdate.StatusUpdateDisplay;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.util.Util;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class L2SkillContinuousDrain extends L2Skill
{
	private static final Logger _logDamage = Logger.getLogger("damage");

	private float _absorbPart;
	private int _absorbTime;

	public L2SkillContinuousDrain(StatsSet set)
	{
		super(set);

		_absorbPart = set.getFloat("absorbPart", 0.0f) / 100;
		_absorbTime = set.getInteger("absorbTime", 0);
	}

	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		ThreadPoolManager.getInstance().scheduleEffect(new DrainTask(activeChar, targets), 1000);
	}

	private class DrainTask implements Runnable
	{
		private int _count = _absorbTime;
		L2Character _activeChar;
		L2Object[] _targets;

		public DrainTask(L2Character activeChar, L2Object[] targets)
		{
			_activeChar = activeChar;
			_targets = targets;
		}

		@Override
		public void run()
		{
			if (!drain(_activeChar, _targets))
			{
				return;
			}

			if (_count > 0)
			{
				ThreadPoolManager.getInstance().scheduleEffect(this, 1000);
			}

			_count--;
		}
	}

	public boolean drain(L2Character activeChar, L2Object[] targets)
	{
		if (activeChar.isAlikeDead() || !activeChar.isCastingNow() || activeChar.getLastSkillCast() != this)
		{
			return false;
		}

		if (!Util.contains(targets, activeChar.getTarget()))
		{
			activeChar.abortCast();
			return false;
		}

		for (L2Character target : (L2Character[]) targets)
		{
			if (target.isAlikeDead() && getTargetType() != L2SkillTargetType.TARGET_CORPSE_MOB)
			{
				return false;
			}

			if (activeChar != target && target.isInvul(activeChar))
			{
				return false; // No effect on invulnerable chars unless they cast it themselves.
			}

			if (activeChar.getDistanceSq(target) > 1400 * 1400)
			{
				return false; // Too far away to continue draining
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

			int _drain = 0;
			//int _cp = (int)target.getCurrentCp();
			int _hp = (int) target.getCurrentHp();

			if (!(Config.isServer(Config.TENKAI) && activeChar instanceof L2PcInstance &&
					target instanceof L2MonsterInstance && ((L2PcInstance) activeChar).getPvpFlag() > 0))
			{
				/*if (_cp > 0)
                {
					if (damage < _cp)
						_drain = 0;
					else
						_drain = damage - _cp;
				}
				else */
				if (damage > _hp)
				{
					_drain = _hp;
				}
				else
				{
					_drain = damage;
				}
			}

			double hpAdd = _absorbPart * _drain;
			double hp = activeChar.getCurrentHp() + hpAdd > activeChar.getMaxHp() ? activeChar.getMaxHp() :
					activeChar.getCurrentHp() + hpAdd;

			activeChar.setCurrentHp(hp);

			StatusUpdate suhp = new StatusUpdate(activeChar, null, StatusUpdateDisplay.NORMAL);
			suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
			activeChar.sendPacket(suhp);

			// Check to see if we should damage the target
			if (damage > 0 && (!target.isDead() || getTargetType() != L2SkillTargetType.TARGET_CORPSE_MOB))
			{
				activeChar.sendDamageMessage(target, damage, mcrit, false, false);

				if (Config.LOG_GAME_DAMAGE && activeChar instanceof L2Playable &&
						damage > Config.LOG_GAME_DAMAGE_THRESHOLD)
				{
					LogRecord record = new LogRecord(Level.INFO, "");
					record.setParameters(new Object[]{activeChar, " did damage ", damage, this, " to ", target});
					record.setLoggerName("mdam");
					_logDamage.log(record);
				}

				target.reduceCurrentHp(damage, activeChar, this);
			}

			// Check to see if we should do the decay right after the cast
			if (target.isDead() && getTargetType() == L2SkillTargetType.TARGET_CORPSE_MOB && target instanceof L2Npc)
			{
				((L2Npc) target).endDecayTask();
			}

			if (activeChar instanceof L2PcInstance)
			{
				((L2PcInstance) activeChar).rechargeAutoSoulShot(false, true, false);
			}
		}

		return true;
	}
}
