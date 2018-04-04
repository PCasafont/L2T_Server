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
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectTemplate;
import l2server.gameserver.templates.skills.EffectType;

/**
 * @author mkizub
 */
public class EffectUntargetable extends L2Effect {
	public EffectUntargetable(Env env, EffectTemplate template) {
		super(env, template);
	}
	
	/**
	 * @see Abnormal#getType()
	 */
	@Override
	public EffectType getEffectType() {
		return EffectType.UNTARGETABLE;
	}
	
	@Override
	public AbnormalType getAbnormalType() {
		return AbnormalType.BUFF;
	}
	
	/**
	 * @see Abnormal#onStart()
	 */
	@Override
	public boolean onStart() {
		if (getEffected() instanceof Player && ((Player) getEffected()).isCombatFlagEquipped()) {
			return false;
		}
		
		for (Creature target : getEffected().getKnownList().getKnownCharacters()) {
			if (target != null && target != getEffected()) {
				if (target.getActingPlayer() != null && getEffected().getActingPlayer() != null && target.getActingPlayer().getParty() != null &&
						target.getActingPlayer().getParty() == getEffected().getActingPlayer().getParty()) {
					continue;
				}
				
				if (target.getTarget() == getEffected() || target.getAI() != null && target.getAI().getAttackTarget() == getEffected()) {
					target.setTarget(null);
					target.abortAttack();
					// It should not abort the cast
					//target.abortCast();
					target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					
					if (target instanceof Attackable) {
						((Attackable) target).reduceHate(getEffected(), ((Attackable) target).getHating(getEffected()));
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
	}
	
	/**
	 * @see Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime() {
		return true;
	}
}
