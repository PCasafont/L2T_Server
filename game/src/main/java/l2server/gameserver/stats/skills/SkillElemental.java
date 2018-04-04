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

import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.templates.StatsSet;

public class SkillElemental extends Skill {

	private final int[] seeds;
	private final boolean seedAny;

	public SkillElemental(StatsSet set) {
		super(set);

		seeds = new int[3];
		seeds[0] = set.getInteger("seed1", 0);
		seeds[1] = set.getInteger("seed2", 0);
		seeds[2] = set.getInteger("seed3", 0);

		seedAny = set.getInteger("seed_any", 0) == 1;
	}

	@Override
	public void useSkill(Creature activeChar, WorldObject[] targets) {
		if (activeChar.isAlikeDead()) {
			return;
		}

		Item weaponInst = activeChar.getActiveWeaponInstance();

		if (activeChar instanceof Player) {
			if (weaponInst == null) {
				activeChar.sendMessage("You must equip your weapon before casting a spell.");
				return;
			}
		}

		double ssMul = Item.CHARGED_NONE;
		if (weaponInst != null) {
			ssMul = weaponInst.getChargedSpiritShot();
			weaponInst.setChargedSpiritShot(Item.CHARGED_NONE);
		} else if (activeChar instanceof Summon) {
			Summon activeSummon = (Summon) activeChar;
			ssMul = activeSummon.getChargedSpiritShot();
			activeSummon.setChargedSpiritShot(Item.CHARGED_NONE);
		}

		for (Creature target : (Creature[]) targets) {
			if (target.isAlikeDead()) {
				continue;
			}

			boolean charged = true;
			if (!seedAny) {
				for (int seed : seeds) {
					if (seed != 0) {
						Abnormal e = target.getFirstEffect(seed);
						if (e == null || !e.getInUse()) {
							charged = false;
							break;
						}
					}
				}
			} else {
				charged = false;
				for (int seed : seeds) {
					if (seed != 0) {
						Abnormal e = target.getFirstEffect(seed);
						if (e != null && e.getInUse()) {
							charged = true;
							break;
						}
					}
				}
			}
			if (!charged) {
				activeChar.sendMessage("Target is not charged by elements.");
				continue;
			}

			boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, this));
			byte shld = Formulas.calcShldUse(activeChar, target, this);

			int damage = (int) Formulas.calcMagicDam(activeChar, target, this, shld, ssMul, mcrit);

			if (damage > 0) {
				target.reduceCurrentHp(damage, activeChar, this);

				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage)) {
					target.breakAttack();
					target.breakCast();
				}

				activeChar.sendDamageMessage(target, damage, false, false, false);
			}

			// activate attacked effects, if any
			//target.stopSkillEffects(getId());
			if (damage > 1) {
				getEffects(activeChar, target, new Env(shld, ssMul));
			}
		}
	}
}
