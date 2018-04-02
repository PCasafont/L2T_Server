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

import l2server.gameserver.ai.CtrlEvent;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.instancemanager.DuelManager;
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.*;
import l2server.gameserver.model.actor.instance.ClanHallManagerInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.SummonInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.templates.skills.SkillBehaviorType;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.gameserver.templates.skills.SkillType;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.2.2.9 $ $Date: 2005/04/03 15:55:04 $
 */

public class Continuous implements ISkillHandler {
	//private static Logger log = Logger.getLogger(Continuous.class.getName());

	private static final SkillType[] SKILL_IDS =
			{SkillType.BUFF, SkillType.DEBUFF, SkillType.CONT, SkillType.CONTINUOUS_DEBUFF, SkillType.UNDEAD_DEFENSE, SkillType.AGGDEBUFF,
			 SkillType.FUSION};

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(Creature, Skill, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		boolean acted = true;

		Player player = null;
		if (activeChar instanceof Player) {
			player = (Player) activeChar;
		}

		if (skill.getEffectId() != 0) {
			Skill sk = SkillTable.getInstance().getInfo(skill.getEffectId(), skill.getEffectLvl() == 0 ? 1 : skill.getEffectLvl());

			if (sk != null) {
				skill = sk;
			}
		}

		for (WorldObject obj : targets) {
			if (!(obj instanceof Creature)) {
				continue;
			}

			Creature target = (Creature) obj;
			byte shld = 0;
			double ssMul = Item.CHARGED_NONE;

			Creature attacker = activeChar;
			if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_EFFECTS) {
				target = activeChar;
				attacker = target;
			}

			// Player holding a cursed weapon can't be buffed and can't buff
			if (skill.getSkillType() == SkillType.BUFF && !(activeChar instanceof ClanHallManagerInstance)) {
				if (target != attacker) {
					if (target instanceof Player) {
						Player trg = (Player) target;
						if (trg.isCursedWeaponEquipped()) {
							continue;
						}
						// Avoiding block checker players get buffed from outside
						else if (trg.getBlockCheckerArena() != -1) {
							continue;
						}
					} else if (player != null && player.isCursedWeaponEquipped()) {
						continue;
					}
				}
			}

			if (skill.isOffensive() || skill.isDebuff()) {
				Item weaponInst = activeChar.getActiveWeaponInstance();
				if (weaponInst != null) {
					if (skill.isMagic()) {
						ssMul = weaponInst.getChargedSpiritShot();
						if (skill.getId() != 1020) // vitalize
						{
							weaponInst.setChargedSpiritShot(Item.CHARGED_NONE);
						}
					} else {
						ssMul = weaponInst.getChargedSoulShot();
						if (skill.getId() != 1020) // vitalize
						{
							weaponInst.setChargedSoulShot(Item.CHARGED_NONE);
						}
					}
				}
				// If there is no weapon equipped, check for an active summon.
				else if (activeChar instanceof Summon) {
					Summon activeSummon = (Summon) activeChar;
					if (skill.isMagic()) {
						ssMul = activeSummon.getChargedSpiritShot();
						activeSummon.setChargedSpiritShot(Item.CHARGED_NONE);
					} else {
						ssMul = activeSummon.getChargedSoulShot();
						activeSummon.setChargedSoulShot(Item.CHARGED_NONE);
					}
				} else if (activeChar instanceof Npc) {
					if (skill.isMagic()) {
						ssMul = ((Npc) activeChar).soulshotcharged ? Item.CHARGED_SOULSHOT : Item.CHARGED_NONE;
						((Npc) activeChar).soulshotcharged = false;
					} else {
						ssMul = ((Npc) activeChar).spiritshotcharged ? Item.CHARGED_SPIRITSHOT : Item.CHARGED_NONE;
						((Npc) activeChar).spiritshotcharged = false;
					}
				}

				shld = Formulas.calcShldUse(attacker, target, skill);
				acted = true;//Formulas.calcSkillSuccess(activeChar, target, skill, shld, 1.0);
			}

			if (acted) {
				if (skill.isToggle()) {
					Abnormal[] effects = target.getAllEffects();
					if (effects != null) {
						for (Abnormal e : effects) {
							if (e != null) {
								if (e.getSkill().getId() == skill.getId()) {
									e.exit();
									return;
								}
							}
						}
					}
				}

				// if this is a debuff let the duel manager know about it
				// so the debuff can be removed after the duel
				// (player & target must be in the same duel)
				Abnormal[] effects = skill.getEffects(attacker, target, new Env(shld, ssMul));
				if (target instanceof Player && ((Player) target).isInDuel() &&
						(skill.isDebuff() || skill.getSkillType() == SkillType.BUFF) && player != null &&
						player.getDuelId() == ((Player) target).getDuelId()) {
					DuelManager dm = DuelManager.getInstance();
					for (Abnormal buff : effects) {
						if (buff != null) {
							dm.onBuff((Player) target, buff);
						}
					}
				}

				// Give the buff to our pets if possible
				if (target instanceof Player && effects.length > 0 &&
						(effects[0].canBeShared() || skill.getTargetType() == SkillTargetType.TARGET_SELF) && !skill.isToggle() &&
						skill.canBeSharedWithSummon()) {
					for (SummonInstance summon : ((Player) target).getSummons()) {
						skill.getEffects(attacker, summon, new Env(shld, ssMul));
					}
				}

				if (skill.getSkillType() == SkillType.AGGDEBUFF && effects.length > 0) {
					if (target instanceof Attackable) {
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, (int) skill.getPower());
					} else if (target instanceof Playable) {
						if (target.getTarget() == attacker) {
							target.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, activeChar);
						} else {
							target.setTarget(attacker);
						}
					}
				}

				if (effects.length == 0 && skill.getSkillBehavior() != SkillBehaviorType.FRIENDLY) {
					acted = false;
				}
			} else {
				attacker.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED));
			}

			if (skill.getSkillType() == SkillType.CONTINUOUS_DEBUFF && !acted) {
				activeChar.abortCast();
			}

			// Possibility of a lethal strike
			Formulas.calcLethalHit(attacker, target, skill);
		}

		// self Effect
		if (skill.hasSelfEffects()) {
			final Abnormal effect = activeChar.getFirstEffect(skill.getId());
			if (effect != null && effect.isSelfEffect()) {
				//Replace old effect with new one.
				effect.exit();
			}
			skill.getEffectsSelf(activeChar);
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
