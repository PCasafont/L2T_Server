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
package ai.individual.Summons;

import java.util.concurrent.ScheduledFuture;

import l2tserver.gameserver.GeoData;
import l2tserver.gameserver.ThreadPoolManager;
import l2tserver.gameserver.datatables.SkillTable;
import l2tserver.gameserver.model.L2Party;
import l2tserver.gameserver.model.L2Skill;
import l2tserver.gameserver.model.actor.L2Npc;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 * @author Pere
 * 
 * Summon Protection Stone (skill id: 11359) AI
 */

public class ProtectionStone extends L2AttackableAIScript
{
	private static final int		_protectionStoneId	= 13423;
	private static final int		_arcaneProtectionId = 11360;
	private static final int _summonProtectionStoneId = 11359;
	
	public ProtectionStone(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addSpawnId(_protectionStoneId);
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		npc.disableCoreAI(true);
		
		ProtectionStoneAI ai = new ProtectionStoneAI(npc);
		
		ai.setSchedule(ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(ai, 1000, 5000));
		
		return null;
	}
	
	class ProtectionStoneAI implements Runnable
	{
		private L2Npc _protectionStone;
		private L2PcInstance _owner;
		private ScheduledFuture<?> _schedule = null;
		private  L2Skill _arcaneProtection;
		
		protected ProtectionStoneAI(L2Npc npc)
		{
			_protectionStone	= npc;
			_owner				= npc.getOwner();
			_arcaneProtection = SkillTable.getInstance().getInfo(_arcaneProtectionId, _owner.getSkillLevelHash(_summonProtectionStoneId));
		}
		
		public void setSchedule(ScheduledFuture<?> schedule)
		{
			_schedule = schedule;
		}
		
		public void run()
		{
			if (_protectionStone == null || _protectionStone.isDead() || _protectionStone.isDecayed())
			{
				if (_schedule != null)
				{
					_schedule.cancel(true);
					return;
				}
			}
			
			L2Party party = _owner.getParty();
			if (party != null)
			{
				for (L2PcInstance player : party.getPartyMembers())
				{
					if (player == null || !GeoData.getInstance().canSeeTarget(_protectionStone, player) || !player.isInsideRadius(_protectionStone, _arcaneProtection.getSkillRadius(), true, false))
						continue;
					
					_arcaneProtection.getEffects(_protectionStone, player);
				}
			}
			else
			{
				if (GeoData.getInstance().canSeeTarget(_protectionStone, _owner) && _owner.isInsideRadius(_protectionStone, _arcaneProtection.getSkillRadius(), true, false))
					_arcaneProtection.getEffects(_protectionStone, _owner);
			}
		}
	}
	
	public static void main(String[] args)
	{
		new ProtectionStone(-1, "ProtectionStone", "ai/individual");
	}
}
