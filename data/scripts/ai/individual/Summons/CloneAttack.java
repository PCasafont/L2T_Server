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

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Attackable.AggroInfo;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2BabyPetInstance;
import l2server.gameserver.model.actor.instance.L2GuardInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectType;
import l2server.log.Log;
import l2server.util.Rnd;
import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 * @author Pere
 * 
 * Clone Attack (skill id: 10532) AI
 */

public class CloneAttack extends L2AttackableAIScript
{
	private static final boolean	_debug				= false;
	private static final int[]		_shadowOfHellIds	= {13302, 13303, 13304, 13305};
	
	public CloneAttack(int id, String name, String descr)
	{
		super(id, name, descr);
		
		for (int i : _shadowOfHellIds)
			addSpawnId(i);
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		if (npc.getOwner() == null)
			return null;
		
		((L2GuardInstance)npc).setCanReturnToSpawnPoint(false);
		npc.setIsInvul(true);
		npc.setIsRunning(true);
		npc.setIsMortal(false);

		CloneAttackAI ai = new CloneAttackAI(npc);
		ai.setSchedule(ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(ai, 1, 1000));
		
		return null;
	}
	
	class CloneAttackAI implements Runnable
	{
		private L2Npc _shadowOfHell;
		private ScheduledFuture<?> _schedule;
		private L2Skill _illusionTrick;
		private L2Skill _illusionStrike;
		private L2Character _tempTarget;
		private L2PcInstance _owner;
		
		protected CloneAttackAI(L2Npc npc)
		{
			_shadowOfHell	= npc;
			_owner = npc.getOwner();
			int cloneAttackLevel = 1;
			if (_owner != null)
				cloneAttackLevel = _owner.getSkillLevelHash(10532);
			_illusionTrick = SkillTable.getInstance().getInfo(10551, cloneAttackLevel);
			_illusionStrike = SkillTable.getInstance().getInfo(10550, cloneAttackLevel);
		}
		
		public void setSchedule(ScheduledFuture<?> schedule)
		{
			_schedule = schedule;
		}
		
		public void run()
		{
			if (_shadowOfHell == null || _shadowOfHell.isDead() || _shadowOfHell.isDecayed() || _shadowOfHell.isAlikeDead() || _owner == null)
			{
				if (_schedule != null)
				{
					_schedule.cancel(true);
					return;
				}
			}
			
			L2Object temp = _owner.getTarget();
			if (temp instanceof L2Character)
			{	
				if (_tempTarget == null || temp != _tempTarget)
					_tempTarget = (L2Character)_owner.getTarget();
			}
			
			L2Character ownerAttackingTarget = _owner.getAI().getAttackTarget();
			if (ownerAttackingTarget == null)
				ownerAttackingTarget = _owner.getAI().getCastTarget();
			L2Character shadowOfHellAttackingTarget = _shadowOfHell.getAI().getAttackTarget();
			if (shadowOfHellAttackingTarget == null)
				shadowOfHellAttackingTarget = _shadowOfHell.getAI().getCastTarget(); 
			
			L2Character target = null;
			if (shadowOfHellAttackingTarget == null && ownerAttackingTarget != null)
				target = ownerAttackingTarget;
			else if (shadowOfHellAttackingTarget != null && ownerAttackingTarget != null && shadowOfHellAttackingTarget != ownerAttackingTarget)
				target = ownerAttackingTarget;
			else if (ownerAttackingTarget == null && shadowOfHellAttackingTarget != null)
				target = shadowOfHellAttackingTarget;
		
			if (target == null && _tempTarget != null)
				target = _tempTarget;
			
			if (target != shadowOfHellAttackingTarget)
			{
				if (_shadowOfHell.isCastingNow())
					_shadowOfHell.abortCast();
				
				if (_shadowOfHell.isAttackingNow())
					_shadowOfHell.abortAttack();
				
				((L2Attackable) _shadowOfHell).clearAggroList();
				_shadowOfHell.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, _owner);
			}	
			
			if (target != null && (target == _owner || target instanceof L2GuardInstance && ((L2GuardInstance)target).getOwner() == _owner))
				target = null;
			
			if (target != null)
			{
				if (target.isAffected(L2EffectType.UNTARGETABLE.getMask()) || 
						target.getFirstEffect(L2AbnormalType.HIDE) != null || 
						target.isAlikeDead())
				{
					if (_shadowOfHell.isCastingNow())
						_shadowOfHell.abortCast();
					
					if (_shadowOfHell.isAttackingNow())
						_shadowOfHell.abortAttack();
					
					((L2Attackable) _shadowOfHell).clearAggroList();
					_shadowOfHell.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _owner);
					return;
				}	
				
				L2PcInstance pTarget = null;
				if (target instanceof L2PcInstance)
					pTarget = (L2PcInstance)target;
				if (target instanceof L2SummonInstance)
					pTarget = ((L2SummonInstance)target).getOwner();
				if (target instanceof L2PetInstance)
					pTarget = ((L2PetInstance)target).getOwner();
				if (target instanceof L2BabyPetInstance)
					pTarget = ((L2BabyPetInstance)target).getOwner();
				
				if (pTarget != null)
				{
					if (!_owner.canAttackCharacter(pTarget) || !pTarget.isAutoAttackable(_owner))
					{
						if (_shadowOfHell.isCastingNow())
							_shadowOfHell.abortCast();
						
						if (_shadowOfHell.isAttackingNow())
							_shadowOfHell.abortAttack();
						
						((L2Attackable) _shadowOfHell).clearAggroList();
						_shadowOfHell.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _owner);
						return;
					}	
				}
			}
			
			if (target != null && target.isInsideRadius(_owner, 600, false, false))
			{
				if (_debug)
					Log.info(getName() + ": Target: " + target.getName());
				
				if (!_shadowOfHell.isInsideRadius(target, _owner.getPhysicalAttackRange(), false, false))
					_shadowOfHell.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(target.getX() + Rnd.get(20), target.getY() + Rnd.get(20), target.getZ(), 0) , null);
				else
				{
					_shadowOfHell.setTarget(target);
					
					int skillRand = Rnd.get(100);
					if (skillRand > 80 && _shadowOfHell.canCastNow(_illusionStrike))
						_shadowOfHell.doCast(_illusionStrike);
					else if (skillRand > 70 && _shadowOfHell.canCastNow(_illusionTrick))	
						_shadowOfHell.doCast(_illusionTrick);
					else
					{
						if (target instanceof L2Attackable)
							((L2Attackable) _shadowOfHell).addDamageHate(target, 100, 99999);
						else
						{
							AggroInfo ai = ((L2Attackable) _shadowOfHell).getAggroList().get(target);
							if (ai == null)
							{
								ai = new AggroInfo(target);
								((L2Attackable) _shadowOfHell).getAggroList().put(target, ai);
							}
							ai.addDamage(100);
							ai.addHate(99999);
						}	
						
						_shadowOfHell.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
						
						if(_debug)
							Log.info(getName() + ": Attacking: " + target.getName());
					}
				}	
			}
			else
			{
				if(_debug)
					Log.info(getName() + ": Target: is null!");
				
				//Follow
				_shadowOfHell.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _owner);
			}
		}
	}
	
	public static void main(String[] args)
	{
		new CloneAttack(-1, "CloneAttack", "ai/individual");
	}
}
