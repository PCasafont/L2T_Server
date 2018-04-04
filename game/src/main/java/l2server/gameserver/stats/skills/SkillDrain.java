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
import l2server.gameserver.model.Item;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.CubicInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.StatusUpdate.StatusUpdateDisplay;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.skills.SkillTargetType;
import org.slf4j.LoggerFactory;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class SkillDrain extends Skill {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(SkillDrain.class.getName());

	private float absorbPart;
	private int absorbAbs;

	public SkillDrain(StatsSet set) {
		super(set);

		absorbPart = set.getFloat("absorbPart", 0.f);
		absorbAbs = set.getInteger("absorbAbs", 0);
	}

	@Override
	public void useSkill(Creature activeChar, WorldObject[] targets) {
		if (activeChar.isAlikeDead()) {
			return;
		}

		for (Creature target : (Creature[]) targets) {
			if (target.isAlikeDead() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB) {
				continue;
			}

			if (activeChar != target && target.isInvul(activeChar)) {
				continue; // No effect on invulnerable chars unless they cast it themselves.
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
			int curCp = (int) target.getCurrentCp();
			int curHp = (int) target.getCurrentHp();

			/*if (!((Config.isServer(Config.PVP))
					&& activeChar instanceof Player
					&& target instanceof MonsterInstance
					&& ((Player)activeChar).getPvpFlag() > 0))*/
			{
				if (curCp > 0) {
					if (damage < curCp) {
						drain = 0;
					} else {
						drain = damage - curCp;
					}
				} else if (damage > curHp) {
					drain = curHp;
				} else {
					drain = damage;
				}
			}

			double hpAdd = absorbAbs + absorbPart * drain;
			double hp = activeChar.getCurrentHp() + hpAdd > activeChar.getMaxHp() ? activeChar.getMaxHp() : activeChar.getCurrentHp() + hpAdd;

			activeChar.setCurrentHp(hp);

			if (getId() == 1245) {
				int mpAdd = (int) (damage * 0.05);
				double mp = activeChar.getCurrentMp() + mpAdd > activeChar.getMaxMp() ? activeChar.getMaxMp() : activeChar.getCurrentMp() + mpAdd;

				activeChar.setCurrentMp(mp);

				StatusUpdate suhp = new StatusUpdate(activeChar, target, StatusUpdateDisplay.NORMAL);
				suhp.addAttribute(StatusUpdate.CUR_MP, (int) mp);
				activeChar.sendPacket(suhp);
			}

			StatusUpdate suhp = new StatusUpdate(activeChar, target, StatusUpdateDisplay.NORMAL);
			suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
			activeChar.sendPacket(suhp);

			// Check to see if we should damage the target
			if (damage > 0 && (!target.isDead() || getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)) {
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage)) {
					target.breakAttack();
					target.breakCast();
				}

				activeChar.sendDamageMessage(target, damage, mcrit, false, false);

				if (Config.LOG_GAME_DAMAGE && activeChar instanceof Playable && damage > Config.LOG_GAME_DAMAGE_THRESHOLD) {
					LogRecord record = new LogRecord(Level.INFO, "");
					record.setParameters(new Object[]{activeChar, " did damage ", damage, this, " to ", target});
					record.setLoggerName("mdam");
				}

				if (hasEffects() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB) {
					// ignoring vengeance-like reflections
					if ((Formulas.calcSkillReflect(target, this) & Formulas.SKILL_REFLECT_EFFECTS) > 0) {
						//activeChar.stopSkillEffects(getId());
						getEffects(target, activeChar);
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(getId());
						activeChar.sendPacket(sm);
					} else {
						// activate attacked effects, if any
						//target.stopSkillEffects(getId());
						if (Formulas.calcSkillSuccess(activeChar, target, this, shld, ssMul)) {
							getEffects(activeChar, target);
						} else {
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
			if (target.isDead() && getTargetType() == SkillTargetType.TARGET_CORPSE_MOB && target instanceof Npc) {
				((Npc) target).endDecayTask();
			}
		}
		//effect self :]
        /*Abnormal effect = activeChar.getFirstEffect(getId());
		if (effect != null && effect.isSelfEffect())
		{
			//Replace old effect with new one.
			effect.exit();
		}*/
		// cast self effect if any
		getEffectsSelf(activeChar);
	}

	public void useCubicSkill(CubicInstance activeCubic, WorldObject[] targets) {
		if (Config.DEBUG) {
			log.info("SkillDrain: useCubicSkill()");
		}

		for (Creature target : (Creature[]) targets) {
			if (target.isAlikeDead() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB) {
				continue;
			}

			boolean mcrit = Formulas.calcMCrit(activeCubic.getMCriticalHit(target, this));
			byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, this);

			int damage = (int) Formulas.calcMagicDam(activeCubic, target, this, mcrit, shld);
			if (Config.DEBUG) {
				log.info("SkillDrain: useCubicSkill() -> damage = " + damage);
			}

			double hpAdd = absorbAbs + absorbPart * damage;
			Player owner = activeCubic.getOwner();
			double hp = owner.getCurrentHp() + hpAdd > owner.getMaxHp() ? owner.getMaxHp() : owner.getCurrentHp() + hpAdd;

			owner.setCurrentHp(hp);

			StatusUpdate suhp = new StatusUpdate(owner);
			suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
			owner.sendPacket(suhp);

			// Check to see if we should damage the target
			if (damage > 0 && (!target.isDead() || getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)) {
				target.reduceCurrentHp(damage, activeCubic.getOwner(), this);

				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage)) {
					target.breakAttack();
					target.breakCast();
				}
				owner.sendDamageMessage(target, damage, mcrit, false, false);
			}
		}
	}
}
