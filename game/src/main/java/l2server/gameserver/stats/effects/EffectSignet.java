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

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.CreatureZone;
import l2server.gameserver.model.actor.instance.EffectPointInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.skills.SkillSignet;
import l2server.gameserver.stats.skills.SkillSignetCasttime;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectTemplate;

import java.util.ArrayList;

/**
 * @authors Forsaiken, Sami
 */
public class EffectSignet extends L2Effect {
	private Skill skill;
	private EffectPointInstance actor;
	private boolean srcInArena;

	public EffectSignet(Env env, EffectTemplate template) {
		super(env, template);
	}

	@Override
	public AbnormalType getAbnormalType() {
		return AbnormalType.SIGNET_EFFECT;
	}

	/**
	 * @see Abnormal#onStart()
	 */
	@Override
	public boolean onStart() {
		if (getSkill() instanceof SkillSignet) {
			skill = SkillTable.getInstance().getInfo(((SkillSignet) getSkill()).effectId, ((SkillSignet) getSkill()).effectLevel);
		} else if (getSkill() instanceof SkillSignetCasttime) {
			skill = SkillTable.getInstance().getInfo(((SkillSignetCasttime) getSkill()).effectId, getLevel());
		}
		actor = (EffectPointInstance) getEffected();
		srcInArena = getEffector().isInsideZone(CreatureZone.ZONE_PVP) && !getEffector().isInsideZone(CreatureZone.ZONE_SIEGE);
		return true;
	}

	/**
	 * @see Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime() {
		if (skill == null) {
			return true;
		}

		int mpConsume = skill.getMpConsume();

		if (mpConsume > getEffector().getCurrentMp()) {
			getEffector().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
			return false;
		} else {
			getEffector().reduceCurrentMp(mpConsume);
		}

		boolean signetActor = calc() != 0;

		final ArrayList<Creature> targets = new ArrayList<>();
		for (Creature cha : actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius())) {
			if (cha == null) {
				continue;
			}

			if (skill.isOffensive() && !Skill.checkForAreaOffensiveSkills(getEffector(), cha, skill, srcInArena)) {
				continue;
			}

			if (cha instanceof Player) {
				Player player = (Player) cha;
				if (!player.isInsideZone(CreatureZone.ZONE_PVP) && player.getPvpFlag() == 0) {
					continue;
				}
			}

			// there doesn't seem to be a visible effect with MagicSkillLaunched packet...
			if (!signetActor) {
				actor.broadcastPacket(new MagicSkillUse(actor, cha, skill.getId(), skill.getLevelHash(), 0, 0, 0));
			}
			targets.add(cha);
		}

		if (signetActor) {
			//actor.broadcastPacket(new TargetSelected(actor.getObjectId(), actor.getObjectId(), actor.getX(), actor.getY(), actor.getZ()));
			actor.broadcastPacket(new MagicSkillUse(actor, skill.getId(), skill.getLevelHash(), 0, 0));
			//actor.broadcastPacket(new MagicSkillLaunched(actor, skill.getId(), skill.getLevel(), targets.toArray(new Creature[targets.size()])));
		}

		if (!targets.isEmpty()) {
			if (!signetActor) {
				getEffector().callSkill(skill, targets.toArray(new Creature[targets.size()]));
			} else {
				actor.callSkill(skill, targets.toArray(new Creature[targets.size()]));
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
