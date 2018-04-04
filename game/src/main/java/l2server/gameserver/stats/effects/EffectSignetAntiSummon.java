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

import l2server.gameserver.ai.CtrlEvent;
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.EffectPointInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.SummonInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectTemplate;

/**
 * @author Forsaiken
 */
public class EffectSignetAntiSummon extends L2Effect {
	private EffectPointInstance actor;

	public EffectSignetAntiSummon(Env env, EffectTemplate template) {
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
		if (!(getEffector() instanceof Player)) {
			return false;
		}

		actor = (EffectPointInstance) getEffected();
		return true;
	}

	/**
	 * @see Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime() {
		if (getAbnormal().getCount() == getAbnormal().getTotalCount() - 1) {
			return true; // do nothing first time
		}

		int mpConsume = getSkill().getMpConsume();

		Player caster = (Player) getEffector();

		for (Creature cha : actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius())) {
			if (cha == null) {
				continue;
			}

			if (cha instanceof Player) {
				Player player = (Player) cha;
				if (!player.isInsideZone(Creature.ZONE_PVP) && player.getPvpFlag() == 0) {
					continue;
				}
			}

			if (cha instanceof Playable) {
				if (caster.canAttackCharacter(cha)) {
					Player owner = null;
					if (cha instanceof Summon) {
						owner = ((Summon) cha).getOwner();
					} else {
						owner = (Player) cha;
					}

					if (owner != null && (owner.getPet() != null || !owner.getSummons().isEmpty())) {
						if (mpConsume > getEffector().getCurrentMp()) {
							getEffector().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
							return false;
						} else {
							getEffector().reduceCurrentMp(mpConsume);
						}

						if (owner.getPet() != null) {
							owner.getPet().unSummon(owner);
						}
						for (SummonInstance summon : owner.getSummons()) {
							summon.unSummon(owner);
						}
						owner.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, getEffector());
					}
				}
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
