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

/*
  @author Forsaiken
 */

package l2server.gameserver.stats.effects;

import l2server.gameserver.ai.CtrlEvent;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.actor.*;
import l2server.gameserver.model.actor.instance.EffectPointInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.MagicSkillLaunched;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.stats.skills.SkillSignetCasttime;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.gameserver.templates.item.WeaponType;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectTemplate;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.util.Point3D;

import java.util.ArrayList;

public class EffectSignetPDam extends L2Effect {
	private EffectPointInstance actor;

	public EffectSignetPDam(Env env, EffectTemplate template) {
		super(env, template);
	}

	@Override
	public AbnormalType getAbnormalType() {
		return AbnormalType.SIGNET_GROUND;
	}

	/**
	 * @see Abnormal#onStart()
	 */
	@Override
	public boolean onStart() {
		NpcTemplate template;
		if (getSkill() instanceof SkillSignetCasttime) {
			template = NpcTable.getInstance().getTemplate(((SkillSignetCasttime) getSkill()).effectNpcId);
		} else {
			return false;
		}

		EffectPointInstance effectPoint = new EffectPointInstance(IdFactory.getInstance().getNextId(), template, getEffector());
		effectPoint.setCurrentHp(effectPoint.getMaxHp());
		effectPoint.setCurrentMp(effectPoint.getMaxMp());
		//World.getInstance().storeObject(effectPoint);

		int x = getEffector().getX();
		int y = getEffector().getY();
		int z = getEffector().getZ();

		if (getSkill().getTargetType() == SkillTargetType.TARGET_GROUND) {
			Point3D wordPosition = getEffector().getSkillCastPosition();

			if (wordPosition != null) {
				x = wordPosition.getX();
				y = wordPosition.getY();
				z = wordPosition.getZ();
			}
		}
		effectPoint.setIsInvul(true);
		effectPoint.spawnMe(x, y, z);

		actor = effectPoint;
		return true;
	}

	/**
	 * @see Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime() {
		if (getAbnormal().getCount() >= getAbnormal().getTotalCount() - 2) {
			return true; // do nothing first 2 times
		}
		int mpConsume = getSkill().getMpConsume();

		Player caster = (Player) getEffector();

		Item weapon = caster.getActiveWeaponInstance();
		double soul = Item.CHARGED_NONE;
		if (weapon != null && weapon.getItemType() != WeaponType.DAGGER) {
			soul = weapon.getChargedSoulShot();
		}

		ArrayList<Creature> targets = new ArrayList<>();

		for (Creature cha : actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius())) {
			if (cha == null || cha == caster) {
				continue;
			}

			if (cha instanceof Player) {
				Player player = (Player) cha;
				if (!player.isInsideZone(CreatureZone.ZONE_PVP) && player.getPvpFlag() == 0) {
					continue;
				}
			}

			if (cha instanceof Attackable || cha instanceof Playable) {
				if (cha.isAlikeDead()) {
					continue;
				}

				if (mpConsume > caster.getCurrentMp()) {
					caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
					return false;
				} else {
					caster.reduceCurrentMp(mpConsume);
				}

				if (cha instanceof Playable) {
					if (caster.canAttackCharacter(cha)) {
						targets.add(cha);
						caster.updatePvPStatus(cha);
					}
				} else {
					targets.add(cha);
				}
			}
		}

		if (!targets.isEmpty()) {
			caster.broadcastPacket(new MagicSkillLaunched(caster,
					getSkill().getId(),
					getSkill().getLevelHash(),
					targets.toArray(new Creature[targets.size()])));
			for (Creature target : targets) {
				boolean pcrit = Formulas.calcMCrit(caster.getCriticalHit(target, getSkill()));
				byte shld = Formulas.calcShldUse(caster, target, getSkill());
				int pdam = (int) Formulas.calcPhysSkillDam(caster, target, getSkill(), shld, pcrit, false, soul);

				if (target instanceof Summon) {
					target.broadcastStatusUpdate();
				}

				if (pdam > 0) {
					if (!target.isRaid() && Formulas.calcAtkBreak(target, pdam)) {
						target.breakAttack();
						target.breakCast();
					}
					caster.sendDamageMessage(target, pdam, pcrit, false, false);
					target.reduceCurrentHp(pdam, caster, getSkill());
				}
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, caster);
			}
		}
		return true;
	}

	/**
	 * @see Abnormal#onExit()
	 */
	@Override
	public void onExit() {
		if (actor != null) {
			actor.deleteMe();
		}
	}
}
