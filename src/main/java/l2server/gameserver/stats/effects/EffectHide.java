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

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.events.instanced.EventInstance.EventType;
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.DeleteObject;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.VisualEffect;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectTemplate;

/**
 * @author ZaKaX - nBd
 */
public class EffectHide extends L2Effect {
	public EffectHide(Env env, EffectTemplate template) {
		super(env, template);
	}

	public EffectHide(Env env, L2Effect effect) {
		super(env, effect);
	}

	@Override
	public AbnormalType getAbnormalType() {
		return AbnormalType.HIDE;
	}

	/**
	 * @see Abnormal#onStart()
	 */
	@Override
	public boolean onStart() {
		if (getEffected() instanceof Player && !((Player) getEffected()).isCombatFlagEquipped() &&
				!(((Player) getEffected()).isPlayingEvent() && (((Player) getEffected()).getEvent().isType(EventType.Survival) ||
						((Player) getEffected()).getEvent().isType(EventType.TeamSurvival)))) {
			Player activeChar = (Player) getEffected();
			activeChar.getAppearance().setInvisible();
			activeChar.startVisualEffect(VisualEffect.STEALTH);

			if (activeChar.getAI().getNextIntention() != null &&
					activeChar.getAI().getNextIntention().getCtrlIntention() == CtrlIntention.AI_INTENTION_ATTACK) {
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}

			L2GameServerPacket del = new DeleteObject(activeChar);
			for (Creature target : activeChar.getKnownList().getKnownCharacters()) {
				if (target != null &&
						(target.getTarget() == activeChar || target.getAI() != null && target.getAI().getAttackTarget() == activeChar)) {
					target.setTarget(null);
					target.abortAttack();
					target.abortCast();
					target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				}

				boolean inSameParty = false;
				if (target.getParty() != null) {
					inSameParty = target.getParty() == activeChar.getParty();
				}

				if (target instanceof Player && !target.isGM() && !inSameParty) {
					target.sendPacket(del);
				}
			}

			for (Creature target : activeChar.getStatus().getStatusListener()) {
				if (target != null &&
						(target.getTarget() == activeChar || target.getAI() != null && target.getAI().getAttackTarget() == activeChar)) {
					target.setTarget(null);
					target.abortAttack();
					target.abortCast();
					target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				}

				boolean inSameParty = false;
				if (target.getParty() != null) {
					inSameParty = target.getParty() == activeChar.getParty();
				}

				if (target instanceof Player && !target.isGM() && !inSameParty) {
					target.sendPacket(del);
				}
			}

			activeChar.broadcastUserInfo();
		}

		return true;
	}

	/**
	 * @see Abnormal#onExit()
	 */
	@Override
	public void onExit() {
		// Avoid other abnormals like this one to be removed
		for (Abnormal abnormal : getEffected().getAllEffects()) {
			if (abnormal.getSkill().getId() != getSkill().getId() && abnormal.getType() == getAbnormalType()) {
				return;
			}
		}

		if (getEffected() instanceof Player) {
			Player activeChar = (Player) getEffected();
			if (!activeChar.inObserverMode()) {
				activeChar.getAppearance().setVisible();
			}
			activeChar.stopVisualEffect(VisualEffect.STEALTH);
		}
	}

	/**
	 * @see Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime() {
		return !(getEffected() instanceof Player && ((Player) getEffected()).isCombatFlagEquipped());
	}
}
