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

import l2tserver.Config;
import l2tserver.gameserver.GeoData;
import l2tserver.gameserver.ai.CtrlEvent;
import l2tserver.gameserver.ai.CtrlIntention;
import l2tserver.gameserver.model.L2CharPosition;
import l2tserver.gameserver.model.L2Effect;
import l2tserver.gameserver.model.Location;
import l2tserver.gameserver.model.actor.instance.L2DefenderInstance;
import l2tserver.gameserver.model.actor.instance.L2FortCommanderInstance;
import l2tserver.gameserver.model.actor.instance.L2NpcInstance;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.model.actor.instance.L2PetInstance;
import l2tserver.gameserver.model.actor.instance.L2SiegeFlagInstance;
import l2tserver.gameserver.model.actor.instance.L2SiegeSummonInstance;
import l2tserver.gameserver.stats.Env;
import l2tserver.gameserver.stats.VisualEffect;
import l2tserver.gameserver.templates.skills.L2AbnormalType;
import l2tserver.gameserver.templates.skills.L2EffectTemplate;
import l2tserver.gameserver.templates.skills.L2EffectType;

/**
 * @author littlecrow
 * 
 *		 Implementation of the Fear Effect
 */
public class EffectLove extends L2Effect
{
	public EffectLove(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}
	
	/**
	 * 
	 * @see l2tserver.gameserver.model.L2Abnormal#getType()
	 */
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.LOVE;
	}

	@Override
	public L2AbnormalType getAbnormalType()
	{
		return L2AbnormalType.LOVE;
	}
	
	/**
	 * 
	 * @see l2tserver.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		// Fear skills cannot be used in event
		if (getEffector() instanceof L2PcInstance
				&& ((L2PcInstance)getEffector()).isInEvent())
			return false;
		
		if (getEffected() instanceof L2NpcInstance
				|| getEffected() instanceof L2DefenderInstance
				|| getEffected() instanceof L2FortCommanderInstance
				|| getEffected() instanceof L2SiegeFlagInstance
				|| getEffected() instanceof L2SiegeSummonInstance)
			return false;
		
		if (!getEffected().isInLove())
		{
			if (getEffected().isCastingNow() && getEffected().canAbortCast())
				getEffected().abortCast();
			
			getEffected().startLove();
			getEffected().startVisualEffect(VisualEffect.TARGET_HEART);
			onActionTime();
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @see l2tserver.gameserver.model.L2Abnormal#onExit()
	 */
	@Override
	public void onExit()
	{
		getEffected().stopVisualEffect(VisualEffect.TARGET_HEART);
		getEffected().stopLove(false);
		getEffected().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, getEffector());
	}
	
	/**
	 * 
	 * @see l2tserver.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		int posX = getEffector().getX();
		int posY = getEffector().getY();
		int posZ = getEffector().getZ();
		
		if (Config.GEODATA > 0)
		{
			Location destiny = GeoData.getInstance().moveCheck(getEffected().getX(), getEffected().getY(), getEffected().getZ(), posX, posY, posZ, getEffected().getInstanceId());
			posX = destiny.getX();
			posY = destiny.getY();
		}
		
		if (!(getEffected() instanceof L2PetInstance))
			getEffected().setRunning();
		
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(posX, posY, posZ, 0));
		return true;
	}
}
