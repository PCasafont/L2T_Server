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

import java.util.Map.Entry;

import l2tserver.gameserver.model.L2CommandChannel;
import l2tserver.gameserver.model.L2Effect;
import l2tserver.gameserver.model.L2Party;
import l2tserver.gameserver.model.actor.L2Attackable;
import l2tserver.gameserver.model.actor.L2Character;
import l2tserver.gameserver.model.actor.L2Attackable.AggroInfo;
import l2tserver.gameserver.model.actor.instance.L2MonsterInstance;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.stats.Env;
import l2tserver.gameserver.templates.skills.L2EffectTemplate;

public class EffectKillMonster extends L2Effect
{
	public EffectKillMonster(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}
	
	/**
	 * 
	 * @see l2tserver.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		if (getEffected() == null || !(getEffected() instanceof L2MonsterInstance) || getEffected().isRaid())
			return false;

		L2PcInstance player = getEffector().getActingPlayer();
		if (player == null)
			return false;
		
		L2Party playerParty = player.getParty();
		L2CommandChannel playerCommand = playerParty != null ? playerParty.getCommandChannel() : null;
		
		boolean shouldFlag = false;
		if (getEffected() instanceof L2Attackable)
		{
			L2Attackable mob = (L2Attackable)getEffected();
			if (mob != null)
			{
				AggroInfo maxDam = null;
				for (Entry<L2Character, AggroInfo> i : mob.getAggroList().entrySet())
				{
					if (i == null || i.getValue() == null || i.getValue().getAttacker() == null)
						continue;
					
					L2PcInstance attacker = i.getValue().getAttacker().getActingPlayer();
					if (attacker == null)
						continue;
					
					L2Party attackerParty = attacker.getParty();
					L2CommandChannel attackerCommand = attackerParty != null ? attackerParty.getCommandChannel() : null;
					
					if (playerParty != null && attackerParty != null && 
							((playerParty.getPartyLeaderOID() == attackerParty.getPartyLeaderOID()) || 
									(playerCommand != null && attackerCommand != null && playerCommand.getMembers().contains(attacker))))
						continue;
					
					if (maxDam == null || i.getValue().getDamage() > maxDam.getDamage())
						maxDam = i.getValue();
				}
				
				if (maxDam != null && maxDam.getAttacker() != player)
					shouldFlag = true;
			}
		}
		
		getEffected().reduceCurrentHp(getEffected().getMaxHp() + 1, getEffector(), null);
		
		if (shouldFlag)
			player.updatePvPStatus();
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
}
