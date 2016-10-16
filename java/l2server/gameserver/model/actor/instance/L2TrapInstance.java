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
	private L2PcInstance owner;
	private int level;
	private boolean isInArena = false;
	private final List<Integer> playersWhoDetectedMe = new ArrayList<>();

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

		this.owner = owner;
		this.level = owner.getLevel();
	}

	public L2TrapInstance(int objectId, L2NpcTemplate template, int instanceId, int lifeTime, L2Skill skill)
	{
		super(objectId, template, lifeTime, skill);
		setInstanceType(InstanceType.L2TrapInstance);

		setInstanceId(instanceId);

		this.owner = null;
		if (skill != null)
		{
			this.level = skill.getLevelHash();
		}
		else
		{
			this.level = 1;
		}
	}

	@Override
	public int getLevel()
	{
		return this.level;
	}

	@Override
	public L2PcInstance getOwner()
	{
		return this.owner;
	}

	@Override
	public L2PcInstance getActingPlayer()
	{
		return this.owner;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		this.isInArena = isInsideZone(ZONE_PVP) && !isInsideZone(ZONE_SIEGE);
		this.playersWhoDetectedMe.clear();
	}

	@Override
	public void deleteMe()
	{
		if (this.owner != null)
		{
			this.owner.setTrap(null);
			this.owner = null;
		}
		super.deleteMe();
	}

	@Override
	public void unSummon()
	{
		if (this.owner != null)
		{
			this.owner.setTrap(null);
			this.owner = null;
		}
		super.unSummon();
	}

	@Override
	public int getKarma()
	{
		return this.owner != null ? this.owner.getReputation() : 0;
	}

	@Override
	public byte getPvpFlag()
	{
		return this.owner != null ? this.owner.getPvpFlag() : 0;
	}

	@Override
	public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		if (miss || this.owner == null)
		{
			return;
		}

		if (this.owner.isInOlympiadMode() && target instanceof L2PcInstance && ((L2PcInstance) target).isInOlympiadMode() &&
				((L2PcInstance) target).getOlympiadGameId() == this.owner.getOlympiadGameId())
		{
			OlympiadGameManager.getInstance().notifyCompetitorDamage(getOwner(), damage);
		}

		final SystemMessage sm;

		if (target.isInvul(this.owner) && !(target instanceof L2NpcInstance))
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

		this.owner.sendPacket(sm);
	}

	@Override
	public boolean canSee(L2Character cha)
	{
		if (cha != null && this.playersWhoDetectedMe.contains(cha.getObjectId()))
		{
			return true;
		}

		if (this.owner == null || cha == null)
		{
			return false;
		}
		if (cha == this.owner)
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
			if (this.owner.isInOlympiadMode() && ((L2PcInstance) cha).isInOlympiadMode() &&
					((L2PcInstance) cha).getOlympiadSide() != this.owner.getOlympiadSide())
			{
				return false;
			}
		}

		if (this.isInArena)
		{
			return true;
		}

		return this.owner.isInParty() && cha.isInParty() &&
				this.owner.getParty().getPartyLeaderOID() == cha.getParty().getPartyLeaderOID();

	}

	@Override
	public void setDetected(L2Character detector)
	{
		if (this.isInArena)
		{
			super.setDetected(detector);
			return;
		}
		if (this.owner != null && this.owner.getPvpFlag() == 0 && this.owner.getReputation() == 0)
		{
			return;
		}

		this.playersWhoDetectedMe.add(detector.getObjectId());
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
		if (!L2Skill.checkForAreaOffensiveSkills(this, target, getSkill(), this.isInArena))
		{
			return false;
		}

		// observers
		if (target instanceof L2PcInstance && ((L2PcInstance) target).inObserverMode())
		{
			return false;
		}

		// olympiad own team and their summons not attacked
		if (this.owner != null && this.owner.isInOlympiadMode())
		{
			final L2PcInstance player = target.getActingPlayer();
			if (player != null && player.isInOlympiadMode() && player.getOlympiadSide() == this.owner.getOlympiadSide())
			{
				return false;
			}
		}

		if (this.isInArena)
		{
			return true;
		}

		// trap owned by players not attack non-flagged players
		if (this.owner != null)
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
