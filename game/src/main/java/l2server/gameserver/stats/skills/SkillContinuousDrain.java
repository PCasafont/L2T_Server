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
import l2server.gameserver.model.Item;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.StatusUpdate.StatusUpdateDisplay;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.gameserver.util.Util;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class SkillContinuousDrain extends Skill {
	private static final Logger logDamage = Logger.getLogger("damage");

	private float absorbPart;
	private int absorbTime;

	public SkillContinuousDrain(StatsSet set) {
		super(set);

		absorbPart = set.getFloat("absorbPart", 0.0f) / 100;
		absorbTime = set.getInteger("absorbTime", 0);
	}

	@Override
	public void useSkill(Creature activeChar, WorldObject[] targets) {
		ThreadPoolManager.getInstance().scheduleEffect(new DrainTask(activeChar, targets), 1000);
	}

	private class DrainTask implements Runnable {
		private int count = absorbTime;
		Creature activeChar;
		WorldObject[] targets;

		public DrainTask(Creature activeChar, WorldObject[] targets) {
			this.activeChar = activeChar;
			this.targets = targets;
		}

		@Override
		public void run() {
			if (!drain(activeChar, targets)) {
				return;
			}

			if (count > 0) {
				ThreadPoolManager.getInstance().scheduleEffect(this, 1000);
			}

			count--;
		}
	}

	public boolean drain(Creature activeChar, WorldObject[] targets) {
		if (activeChar.isAlikeDead() || !activeChar.isCastingNow() || activeChar.getLastSkillCast() != this) {
			return false;
		}

		if (!Util.contains(targets, activeChar.getTarget())) {
			activeChar.abortCast();
			return false;
		}

		for (Creature target : (Creature[]) targets) {
			if (target.isAlikeDead() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB) {
				return false;
			}

			if (activeChar != target && target.isInvul(activeChar)) {
				return false; // No effect on invulnerable chars unless they cast it themselves.
			}

			if (activeChar.getDistanceSq(target) > 1400 * 1400) {
				return false; // Too far away to continue draining
			}

			double ssMul = Item.CHARGED_NONE;
			Item weaponInst = activeChar.getActiveWeaponInstance();
			if (weaponInst != null) {
				ssMul = weaponInst.getChargedSpiritShot();
				weaponInst.setChargedSpiritShot(Item.CHARGED_NONE);
			} else if (activeChar instanceof Summon) {
				Summon activeSummon = (Summon) activeChar;
				ssMul = activeSummon.getChargedSpiritShot();
				activeSummon.setChargedSpiritShot(Item.CHARGED_NONE);
			}

			boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, this));
			byte shld = Formulas.calcShldUse(activeChar, target, this);
			int damage = (int) Formulas.calcMagicDam(activeChar, target, this, shld, ssMul, mcrit);

			int drain = 0;
			//int _cp = (int)target.getCurrentCp();
			int _hp = (int) target.getCurrentHp();

			if (!(Config.isServer(Config.TENKAI) && activeChar instanceof Player && target instanceof MonsterInstance &&
					((Player) activeChar).getPvpFlag() > 0)) {
				/*if (cp > 0)
                {
					if (damage < cp)
						drain = 0;
					else
						drain = damage - cp;
				}
				else */
				if (damage > _hp) {
					drain = _hp;
				} else {
					drain = damage;
				}
			}

			double hpAdd = absorbPart * drain;
			double hp = activeChar.getCurrentHp() + hpAdd > activeChar.getMaxHp() ? activeChar.getMaxHp() : activeChar.getCurrentHp() + hpAdd;

			activeChar.setCurrentHp(hp);

			StatusUpdate suhp = new StatusUpdate(activeChar, null, StatusUpdateDisplay.NORMAL);
			suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
			activeChar.sendPacket(suhp);

			// Check to see if we should damage the target
			if (damage > 0 && (!target.isDead() || getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)) {
				activeChar.sendDamageMessage(target, damage, mcrit, false, false);

				if (Config.LOG_GAME_DAMAGE && activeChar instanceof Playable && damage > Config.LOG_GAME_DAMAGE_THRESHOLD) {
					LogRecord record = new LogRecord(Level.INFO, "");
					record.setParameters(new Object[]{activeChar, " did damage ", damage, this, " to ", target});
					record.setLoggerName("mdam");
					logDamage.log(record);
				}

				target.reduceCurrentHp(damage, activeChar, this);
			}

			// Check to see if we should do the decay right after the cast
			if (target.isDead() && getTargetType() == SkillTargetType.TARGET_CORPSE_MOB && target instanceof Npc) {
				((Npc) target).endDecayTask();
			}

			if (activeChar instanceof Player) {
				((Player) activeChar).rechargeAutoSoulShot(false, true, false);
			}
		}

		return true;
	}
}
