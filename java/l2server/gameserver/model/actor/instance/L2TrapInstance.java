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

package l2server.gameserver.model.actor.instance;

import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Trap;
import l2server.gameserver.model.olympiad.OlympiadGameManager;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.Quest.TrapAction;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.chars.L2NpcTemplate;

import java.util.ArrayList;
import java.util.List;

public class L2TrapInstance extends L2Trap
{
	private L2PcInstance _owner;
	private int _level;
	private boolean _isInArena = false;
	private final List<Integer> _playersWhoDetectedMe = new ArrayList<>();

	/**
	 * @param objectId
	 * @param template
	 * @param owner
	 */
	public L2TrapInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, int lifeTime, L2Skill skill)
	{
		super(objectId, template, lifeTime, skill);
		setInstanceType(InstanceType.L2TrapInstance);

		setInstanceId(owner.getInstanceId());

		_owner = owner;
		_level = owner.getLevel();
	}

	public L2TrapInstance(int objectId, L2NpcTemplate template, int instanceId, int lifeTime, L2Skill skill)
	{
		super(objectId, template, lifeTime, skill);
		setInstanceType(InstanceType.L2TrapInstance);

		setInstanceId(instanceId);

		_owner = null;
		if (skill != null)
		{
			_level = skill.getLevelHash();
		}
		else
		{
			_level = 1;
		}
	}

	@Override
	public int getLevel()
	{
		return _level;
	}

	@Override
	public L2PcInstance getOwner()
	{
		return _owner;
	}

	@Override
	public L2PcInstance getActingPlayer()
	{
		return _owner;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		_isInArena = isInsideZone(ZONE_PVP) && !isInsideZone(ZONE_SIEGE);
		_playersWhoDetectedMe.clear();
	}

	@Override
	public void deleteMe()
	{
		if (_owner != null)
		{
			_owner.setTrap(null);
			_owner = null;
		}
		super.deleteMe();
	}

	@Override
	public void unSummon()
	{
		if (_owner != null)
		{
			_owner.setTrap(null);
			_owner = null;
		}
		super.unSummon();
	}

	@Override
	public int getKarma()
	{
		return _owner != null ? _owner.getReputation() : 0;
	}

	@Override
	public byte getPvpFlag()
	{
		return _owner != null ? _owner.getPvpFlag() : 0;
	}

	@Override
	public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		if (miss || _owner == null)
		{
			return;
		}

		if (_owner.isInOlympiadMode() && target instanceof L2PcInstance && ((L2PcInstance) target).isInOlympiadMode() &&
				((L2PcInstance) target).getOlympiadGameId() == _owner.getOlympiadGameId())
		{
			OlympiadGameManager.getInstance().notifyCompetitorDamage(getOwner(), damage);
		}

		final SystemMessage sm;

		if (target.isInvul(_owner) && !(target instanceof L2NpcInstance))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.ATTACK_WAS_BLOCKED);
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_GAVE_C2_DAMAGE_OF_S3);
			sm.addCharName(this);
			sm.addCharName(target);
			sm.addNumber(damage);
		}

		_owner.sendPacket(sm);
	}

	@Override
	public boolean canSee(L2Character cha)
	{
		if (cha != null && _playersWhoDetectedMe.contains(cha.getObjectId()))
		{
			return true;
		}

		if (_owner == null || cha == null)
		{
			return false;
		}
		if (cha == _owner)
		{
			return true;
		}

		if (cha instanceof L2PcInstance)
		{
			// observers can't see trap
			if (((L2PcInstance) cha).inObserverMode())
			{
				return false;
			}

			// olympiad competitors can't see trap
			if (_owner.isInOlympiadMode() && ((L2PcInstance) cha).isInOlympiadMode() &&
					((L2PcInstance) cha).getOlympiadSide() != _owner.getOlympiadSide())
			{
				return false;
			}
		}

		if (_isInArena)
		{
			return true;
		}

		return _owner.isInParty() && cha.isInParty() &&
				_owner.getParty().getPartyLeaderOID() == cha.getParty().getPartyLeaderOID();

	}

	@Override
	public void setDetected(L2Character detector)
	{
		if (_isInArena)
		{
			super.setDetected(detector);
			return;
		}
		if (_owner != null && _owner.getPvpFlag() == 0 && _owner.getReputation() == 0)
		{
			return;
		}

		_playersWhoDetectedMe.add(detector.getObjectId());
		if (getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION) != null)
		{
			for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION))
			{
				quest.notifyTrapAction(this, detector, TrapAction.TRAP_DETECTED);
			}
		}
		super.setDetected(detector);
	}

	@Override
	protected boolean checkTarget(L2Character target)
	{
		if (!L2Skill.checkForAreaOffensiveSkills(this, target, getSkill(), _isInArena))
		{
			return false;
		}

		// observers
		if (target instanceof L2PcInstance && ((L2PcInstance) target).inObserverMode())
		{
			return false;
		}

		// olympiad own team and their summons not attacked
		if (_owner != null && _owner.isInOlympiadMode())
		{
			final L2PcInstance player = target.getActingPlayer();
			if (player != null && player.isInOlympiadMode() && player.getOlympiadSide() == _owner.getOlympiadSide())
			{
				return false;
			}
		}

		if (_isInArena)
		{
			return true;
		}

		// trap owned by players not attack non-flagged players
		if (_owner != null)
		{
			final L2PcInstance player = target.getActingPlayer();
			if (target instanceof L2Attackable)
			{
				return true;
			}
			if (player == null || player.getPvpFlag() == 0 && player.getReputation() == 0)
			{
				return false;
			}
		}

		return true;
	}
}
