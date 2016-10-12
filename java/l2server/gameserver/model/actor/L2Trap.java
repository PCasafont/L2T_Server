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

package l2server.gameserver.model.actor;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.knownlist.TrapKnownList;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.Quest.TrapAction;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.gameserver.network.serverpackets.NpcInfo;
import l2server.gameserver.network.serverpackets.SocialAction;
import l2server.gameserver.taskmanager.DecayTaskManager;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.item.L2Weapon;
import l2server.log.Log;

import java.util.Collection;
import java.util.logging.Level;

/**
 * @author nBd
 */
public class L2Trap extends L2Character
{
	protected static final int TICK = 1000; // 1s

	private boolean _isTriggered;
	private final L2Skill _skill;
	private final int _lifeTime;
	private int _timeRemaining;
	private boolean _hasLifeTime;

	/**
	 * @param objectId
	 * @param template
	 */
	public L2Trap(int objectId, L2NpcTemplate template, int lifeTime, L2Skill skill)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2Trap);
		setName(template.Name);
		setIsInvul(false);

		_isTriggered = false;
		_skill = skill;
		_hasLifeTime = true;
		if (lifeTime != 0)
		{
			_lifeTime = lifeTime;
		}
		else
		{
			_lifeTime = 30000;
		}
		_timeRemaining = _lifeTime;
		if (lifeTime < 0)
		{
			_hasLifeTime = false;
		}

		if (skill != null)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new TrapTask(), TICK);
		}
	}

	/**
	 * @see l2server.gameserver.model.actor.L2Character#getKnownList()
	 */
	@Override
	public TrapKnownList getKnownList()
	{
		return (TrapKnownList) super.getKnownList();
	}

	@Override
	public void initKnownList()
	{
		setKnownList(new TrapKnownList(this));
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return !canSee(attacker);
	}

	/**
	 *
	 *
	 */
	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
	}

	/**
	 * @see l2server.gameserver.model.actor.L2Character#onDecay()
	 */
	@Override
	public void onDecay()
	{
		deleteMe();
	}

	/**
	 * @return
	 */
	public final int getNpcId()
	{
		return getTemplate().NpcId;
	}

	/**
	 * @see l2server.gameserver.model.actor.L2Character#doDie(l2server.gameserver.model.actor.L2Character)
	 */
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}

		DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}

	/**
	 */
	@Override
	public void deleteMe()
	{
		decayMe();
		getKnownList().removeAllKnownObjects();
		super.deleteMe();
	}

	/**
	 */
	public synchronized void unSummon()
	{
		if (isVisible() && !isDead())
		{
			if (getWorldRegion() != null)
			{
				getWorldRegion().removeFromZones(this);
			}

			deleteMe();
		}
	}

	/**
	 * @see l2server.gameserver.model.actor.L2Character#getActiveWeaponInstance()
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	/**
	 * @see l2server.gameserver.model.actor.L2Character#getActiveWeaponItem()
	 */
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}

	/**
	 * @see l2server.gameserver.model.actor.L2Character#getLevel()
	 */
	@Override
	public int getLevel()
	{
		return getTemplate().Level;
	}

	/**
	 * @see l2server.gameserver.model.actor.L2Character#getTemplate()
	 */
	@Override
	public L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) super.getTemplate();
	}

	/**
	 * @see l2server.gameserver.model.actor.L2Character#getSecondaryWeaponInstance()
	 */
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	/**
	 * @see l2server.gameserver.model.actor.L2Character#getSecondaryWeaponItem()
	 */
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	/**
	 * @see l2server.gameserver.model.actor.L2Character#updateAbnormalEffect()
	 */
	@Override
	public void updateAbnormalEffect()
	{

	}

	public L2Skill getSkill()
	{
		return _skill;
	}

	public L2PcInstance getOwner()
	{
		return null;
	}

	public int getKarma()
	{
		return 0;
	}

	public byte getPvpFlag()
	{
		return 0;
	}

	/**
	 * Checks is triggered
	 *
	 * @return True if trap is triggered.
	 */
	public boolean isTriggered()
	{
		return _isTriggered;
	}

	/**
	 * Checks trap visibility
	 *
	 * @param cha - checked character
	 * @return True if character can see trap
	 */
	public boolean canSee(L2Character cha)
	{
		return false;
	}

	/**
	 * Reveal trap to the detector (if possible)
	 *
	 * @param detector
	 */
	public void setDetected(L2Character detector)
	{
		detector.sendPacket(new NpcInfo(this));
	}

	/**
	 * Check if target can trigger trap
	 *
	 * @param target
	 * @return
	 */
	protected boolean checkTarget(L2Character target)
	{
		return getOwner().isAbleToCastOnTarget(target, null, true);
	}

	private class TrapTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (!_isTriggered)
				{
					if (_hasLifeTime)
					{
						_timeRemaining -= TICK;
						if (_timeRemaining < _lifeTime - 15000)
						{
							SocialAction sa = new SocialAction(getObjectId(), 2);
							broadcastPacket(sa);
						}
						if (_timeRemaining < 0)
						{
							switch (getSkill().getTargetType())
							{
								case TARGET_AURA:
								case TARGET_FRONT_AURA:
								case TARGET_BEHIND_AURA:
								case TARGET_AROUND_CASTER:
									trigger(L2Trap.this);
									break;
								default:
									unSummon();
							}
							return;
						}
					}

					for (L2Character target : getKnownList().getKnownCharactersInRadius(_skill.getSkillRadius()))
					{
						if (target == getOwner())
						{
							continue;
						}

						if (!getOwner().isAbleToCastOnTarget(target, _skill, false))
						{
							continue;
						}

						trigger(target);
						return;
					}

					ThreadPoolManager.getInstance().scheduleGeneral(new TrapTask(), TICK);
				}
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "", e);
				unSummon();
			}
		}
	}

	/**
	 * Trigger trap
	 *
	 * @param target
	 */
	public void trigger(L2Character target)
	{
		_isTriggered = true;
		broadcastPacket(new NpcInfo(this));
		setTarget(target);

		if (getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION) != null)
		{
			for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION))
			{
				quest.notifyTrapAction(this, target, TrapAction.TRAP_TRIGGERED);
			}
		}

		ThreadPoolManager.getInstance().scheduleGeneral(new TriggerTask(), 300);
	}

	private class TriggerTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				doCast(_skill);
				ThreadPoolManager.getInstance().scheduleGeneral(new UnsummonTask(), _skill.getHitTime() + 300);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				unSummon();
			}
		}
	}

	private class UnsummonTask implements Runnable
	{
		@Override
		public void run()
		{
			unSummon();
		}
	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if (_isTriggered || canSee(activeChar))
		{
			activeChar.sendPacket(new NpcInfo(this));
		}
	}

	@Override
	public void broadcastPacket(L2GameServerPacket mov)
	{
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if (player != null && (_isTriggered || canSee(player)))
			{
				player.sendPacket(mov);
			}
		}
	}

	@Override
	public void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist)
	{
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if (player == null)
			{
				continue;
			}
			if (isInsideRadius(player, radiusInKnownlist, false, false))
			{
				if (_isTriggered || canSee(player))
				{
					player.sendPacket(mov);
				}
			}
		}
	}
}
