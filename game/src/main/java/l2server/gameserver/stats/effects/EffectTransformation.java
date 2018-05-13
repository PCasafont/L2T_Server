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

import l2server.gameserver.instancemanager.TransformationManager;
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.actor.CreatureZone;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectTemplate;

/**
 * @author nBd
 */
public class EffectTransformation extends L2Effect {
	public EffectTransformation(Env env, EffectTemplate template) {
		super(env, template);
	}
	
	// Special constructor to steal this effect
	public EffectTransformation(Env env, L2Effect effect) {
		super(env, effect);
	}
	
	@Override
	public AbnormalType getAbnormalType() {
		return AbnormalType.MUTATE;
	}
	
	/**
	 * @see Abnormal#onStart()
	 */
	@Override
	public boolean onStart() {
		if (!(getEffected() instanceof Player)) {
			return false;
		}
		
		Player trg = (Player) getEffected();
		if (trg == null) {
			return false;
		}
		
		if (trg.isAlikeDead() || trg.isCursedWeaponEquipped()) {
			return false;
		}
		
		if (getEffector() == trg && trg.getTransformation() != null) {
			trg.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN));
			return false;
		}
		
		//LasTravel: Avoiding use mounts for enter to castles
		if (TransformationManager.getInstance().isMountable(getSkill().getTransformId()) && trg.isInsideZone(CreatureZone.ZONE_CASTLE)) {
			trg.getFirstEffect(getSkill().getId()).exit();
			return false;
		}
		
		TransformationManager.getInstance().transformPlayer(getSkill().getTransformId(), trg);
		return true;
	}
	
	/**
	 * @see Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime() {
		return false;
	}
	
	@Override
	public void onExit() {
		getEffected().stopTransformation(false);
	}
}
