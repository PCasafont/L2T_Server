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

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;

import l2tserver.gameserver.GeoData;
import l2tserver.gameserver.ThreadPoolManager;
import l2tserver.gameserver.datatables.SkillTable;
import l2tserver.gameserver.model.L2Skill;
import l2tserver.gameserver.model.actor.L2Summon;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 * @author Pere
 * 
 * Summon Clan Guardian (skill id: 19008) AI
 */

public class ClanGuardian extends L2AttackableAIScript
{
	private static final int		_clanGuardian			= 15053;
	private static final L2Skill 	_clanGuardianRecovery	= SkillTable.getInstance().getInfo(19018, 1);
	
	public ClanGuardian(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addSpawnId(_clanGuardian);
	}

	@Override
	public final String onSpawn(L2Summon npc)
	{
		ClanGuardianAI ai = new ClanGuardianAI(npc);
		
		ai.setSchedule(ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(ai, 5000, 10000));
		
		return null;
	}
	
	class ClanGuardianAI implements Runnable
	{
		private L2Summon _clanGuardian;
		private L2PcInstance _owner;
		private ScheduledFuture<?> _schedule = null;
		
		protected ClanGuardianAI(L2Summon npc)
		{
			_clanGuardian	= npc;
			_owner			= npc.getOwner();
		}
		
		public void setSchedule(ScheduledFuture<?> schedule)
		{
			_schedule = schedule;
		}
		
		public void run()
		{
			if (_clanGuardian == null || _clanGuardian.isDead() || !_owner.getSummons().contains(_clanGuardian))
			{
				if (_schedule != null)
				{
					_schedule.cancel(true);
					return;
				}
			}
			
			Collection<L2PcInstance> _players = _clanGuardian.getKnownList().getKnownPlayersInRadius(500);
			
			for (L2PcInstance player : _players)
			{
				if (isValidTarget(player, _clanGuardian))
				{	
					_clanGuardian.setTarget(player);
					_clanGuardian.doCast(_clanGuardianRecovery);
				}	
			}
		}
	}
	
	private boolean isValidTarget(L2PcInstance target, L2Summon summon)
	{
		if (target == null || summon == null)
			return false;
		
		if (summon.isDead() || target.isDead())
			return false;
		
		if (target.isInvul(summon.getOwner()))
			return false;
		
		if (target.getClan() != summon.getOwner().getClan())
			return false;
		
		if (!GeoData.getInstance().canSeeTarget(summon, target))
			return false;
		
		if (!summon.isInsideRadius(target, 500, true, false))
			return false;
		
		if (target.getCurrentHp() == target.getMaxHp() && target.getCurrentCp() == target.getMaxCp() && target.getCurrentMp() == target.getMaxMp())
			return false;
		
		return true;
	}
	
	public static void main(String[] args)
	{
		new ClanGuardian(-1, "ClanGuardian", "ai/individual");
	}
}
