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

import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.EffectPointInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectTemplate;

/**
 * @authors Forsaiken, Sami
 */
public class EffectSignetNoise extends L2Effect {
	private EffectPointInstance actor;
	
	public EffectSignetNoise(Env env, EffectTemplate template) {
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
		
		if (!(getEffector() instanceof Player)) {
			return false;
		}
		
		Player caster = (Player) getEffector();
		
		for (Creature target : actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius())) {
			if (target == null || target == caster) {
				continue;
			}
			
			if (target instanceof Player) {
				Player player = (Player) target;
				if (!player.isInsideZone(Creature.ZONE_PVP) && player.getPvpFlag() == 0) {
					continue;
				}
			}
			
			if (caster.canAttackCharacter(target)) {
				Abnormal[] effects = target.getAllEffects();
				if (effects != null) {
					for (Abnormal effect : effects) {
						if (effect.getSkill().isDance()) {
							effect.exit();
						}
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
