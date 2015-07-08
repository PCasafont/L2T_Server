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

package l2tserver.gameserver.stats.effects;

import l2tserver.gameserver.instancemanager.TransformationManager;
import l2tserver.gameserver.model.L2Effect;
import l2tserver.gameserver.model.actor.L2Character;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.SystemMessageId;
import l2tserver.gameserver.network.serverpackets.SystemMessage;
import l2tserver.gameserver.stats.Env;
import l2tserver.gameserver.templates.skills.L2AbnormalType;
import l2tserver.gameserver.templates.skills.L2EffectTemplate;

/**
 * 
 * @author nBd
 */
public class EffectTransformation extends L2Effect
{
	public EffectTransformation(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}
	
	// Special constructor to steal this effect
	public EffectTransformation(Env env, L2Effect effect)
	{
		super(env, effect);
	}

	@Override
	public L2AbnormalType getAbnormalType()
	{
		return L2AbnormalType.MUTATE;
	}
	
	/**
	 * 
	 * @see l2tserver.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		if (!(getEffected() instanceof L2PcInstance))
			return false;
		
		L2PcInstance trg = (L2PcInstance) getEffected();
		if (trg == null)
			return false;
		
		if (trg.isAlikeDead() || trg.isCursedWeaponEquipped())
			return false;
		
		if (getEffector() == trg && trg.getTransformation() != null)
		{
			trg.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN));
			return false;
		}
		
		//LasTravel: Avoiding use mounts for enter to castles
		if (TransformationManager.getInstance().isMountable(getSkill().getTransformId()) && trg.isInsideZone(L2Character.ZONE_CASTLE))
		{
			trg.getFirstEffect(getSkill().getId()).exit();
			return false;
		}
		
		TransformationManager.getInstance().transformPlayer(getSkill().getTransformId(), trg);
		return true;
	}
	
	/**
	 * 
	 * @see l2tserver.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopTransformation(false);
	}
}
