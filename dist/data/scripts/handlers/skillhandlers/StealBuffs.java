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
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.PetInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.SummonInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.SkillType;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class StealBuffs implements ISkillHandler {
	private static final SkillType[] SKILL_IDS = {SkillType.STEAL_BUFF};

	// Resistance given by each buff enchant level
	private final double ENCHANT_BENEFIT = 0.5;

	// Minimum cancellation chance
	private final int MIN_CANCEL_CHANCE = 0;

	// Level difference penalty
	private double PER_LVL_PENALTY = 5;

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(Creature, Skill, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		dischargeShots(activeChar, skill);

		Creature target;
		Abnormal effect;
		int maxNegate = skill.getMaxNegatedEffects();
		double chance = skill.getPower();
		boolean targetWasInOlys = false;

		for (WorldObject obj : targets) {
			if (!(obj instanceof Creature)) {
				continue;
			}

			if (obj instanceof Player) {
				targetWasInOlys = ((Player) obj).isInOlympiadMode();
			} else if (obj instanceof SummonInstance) {
				((SummonInstance) obj).getOwner().isInOlympiadMode();
			} else if (obj instanceof PetInstance) {
				((PetInstance) obj).getOwner().isInOlympiadMode();
			}

			target = (Creature) obj;

			if (target.isDead()) {
				continue;
			}

			if (!(target instanceof Player)) {
				continue;
			}

			Env env;
			int lastSkillId = 0;
			final Abnormal[] effects = target.getAllEffects();
			final List<Abnormal> toSteal = new ArrayList<Abnormal>(maxNegate);

			// Consider caster skill and target level
			chance -= (target.getLevel() - skill.getMagicLevel()) * PER_LVL_PENALTY;
			chance *= Formulas.calcEffectTypeProficiency(activeChar, target, AbnormalType.CANCEL) /
					Formulas.calcEffectTypeResistance(target, AbnormalType.CANCEL);
			if (chance < 0.0) {
				chance = 0.0;
			}

			for (int i = effects.length; --i >= 0; ) // reverse order
			{
				effect = effects[i];
				if (effect == null) {
					continue;
				}

				if (!effect.canBeStolen()) // remove effect if can't be stolen
				{
					effects[i] = null;
					continue;
				}

				// if eff time is smaller than 5 sec, will not be stolen, just to save CPU,
				// avoid synchronization(?) problems and NPEs
				if (effect.getDuration() - effect.getTime() < 5) {
					effects[i] = null;
					continue;
				}

				// first pass - only dances/songs
				if (!effect.getSkill().isDance()) {
					continue;
				}

				if (effect.getSkill().getId() != lastSkillId) {
					lastSkillId = effect.getSkill().getId();
					maxNegate--;
				}

				// Save original rate temporarily
				double tempRate = chance;

				// Reduce land rate depending on effect's enchant level
				if (effect.getEnchantRouteId() > 100) {
					chance -= effect.getEnchantLevel() * ENCHANT_BENEFIT;
				}
				if (chance < MIN_CANCEL_CHANCE) {
					chance = MIN_CANCEL_CHANCE;
				}

				if (Rnd.get(100) < chance) // Tenkai custom - only percentual chance to steal a buff
				{
					toSteal.add(effect);
				}

				// Restore original rate
				chance = tempRate;

				if (maxNegate == 0) {
					break;
				}
			}

			if (maxNegate > 0) // second pass
			{
				lastSkillId = 0;
				for (int i = effects.length; --i >= 0; ) {
					effect = effects[i];
					if (effect == null) {
						continue;
					}

					// second pass - all except dances/songs
					if (effect.getSkill().isDance()) {
						continue;
					}

					if (effect.getSkill().getId() != lastSkillId) {
						lastSkillId = effect.getSkill().getId();
						maxNegate--;
					}

					// Save original rate temporarily
					double tempRate = chance;

					// Reduce land rate depending on effect's enchant level
					if (effect.getLevelHash() > 100) {
						chance -= effect.getLevelHash() % 100 * ENCHANT_BENEFIT;
					}
					if (chance < MIN_CANCEL_CHANCE) {
						chance = MIN_CANCEL_CHANCE;
					}

					if (Rnd.get(100) < chance) // Tenkai custom - only percentual chance to steal a buff
					{
						toSteal.add(effect);
					}

					// Restore original rate
					chance = tempRate;

					if (maxNegate == 0) {
						break;
					}
				}
			}

			if (toSteal.size() == 0) {
				continue;
			}

			// stealing effects
			for (Abnormal eff : toSteal) {
				env = new Env();
				env.player = target;
				env.target = activeChar;
				env.skill = eff.getSkill();
				try {
					effect = eff.getTemplate().getStolenEffect(env, eff);
					if (effect != null) {
						effect.scheduleEffect();
						if (effect.getShowIcon() && activeChar instanceof Player) {
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
							sm.addSkillName(effect);
							activeChar.sendPacket(sm);
						}
					}
					// Finishing stolen effect
					eff.exit();

					// Tenkai custom - Buffs returning
					if (eff.getEffected() instanceof Player) {
						eff.getEffected().getActingPlayer().scheduleEffectRecovery(eff, 15, targetWasInOlys);
					}
				} catch (RuntimeException e) {
					log.warn("Cannot steal effect: " + eff + " Stealer: " + activeChar + " Stolen: " + target, e);
				}
			}

			//Possibility of a lethal strike
			Formulas.calcLethalHit(activeChar, target, skill);
		}

		if (skill.hasSelfEffects()) {
			// Applying self-effects
			effect = activeChar.getFirstEffect(skill.getId());
			if (effect != null && effect.isSelfEffect()) {
				//Replace old effect with new one.
				effect.exit();
			}
			skill.getEffectsSelf(activeChar);
		}
	}

	private void dischargeShots(Creature activeChar, Skill skill) {
		// discharge shots
		final Item weaponInst = activeChar.getActiveWeaponInstance();
		if (weaponInst != null) {
			if (skill.isMagic()) {
				if (weaponInst.getChargedSpiritShot() == Item.CHARGED_BLESSED_SPIRITSHOT) {
					weaponInst.setChargedSpiritShot(Item.CHARGED_NONE);
				} else if (weaponInst.getChargedSpiritShot() == Item.CHARGED_SPIRITSHOT) {
					weaponInst.setChargedSpiritShot(Item.CHARGED_NONE);
				}
			}
		} else if (activeChar instanceof Summon) {
			final Summon activeSummon = (Summon) activeChar;

			if (skill.isMagic()) {
				if (activeSummon.getChargedSpiritShot() == Item.CHARGED_BLESSED_SPIRITSHOT) {
					activeSummon.setChargedSpiritShot(Item.CHARGED_NONE);
				} else if (activeSummon.getChargedSpiritShot() == Item.CHARGED_SPIRITSHOT) {
					activeSummon.setChargedSpiritShot(Item.CHARGED_NONE);
				}
			}
		} else if (activeChar instanceof Npc) {
			((Npc) activeChar).spiritshotcharged = false;
		}
	}

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public SkillType[] getSkillIds() {
		return SKILL_IDS;
	}
}
