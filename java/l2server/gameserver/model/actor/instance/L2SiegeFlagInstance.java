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

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.instancemanager.FortSiegeManager;
import l2server.gameserver.instancemanager.SiegeManager;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2SiegeClan;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.status.SiegeFlagStatus;
import l2server.gameserver.model.entity.Siegable;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.templates.chars.L2NpcTemplate;

public class L2SiegeFlagInstance extends L2Npc
{
	private L2Clan _clan;
	private L2PcInstance _player;
	private Siegable _siege;
	private final boolean _isAdvanced;
	private boolean _canTalk;

	public L2SiegeFlagInstance(L2PcInstance player, int objectId, L2NpcTemplate template, boolean advanced, boolean outPost)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2SiegeFlagInstance);

		_clan = player.getClan();
		_player = player;
		_canTalk = true;
		_siege = SiegeManager.getInstance().getSiege(_player.getX(), _player.getY(), _player.getZ());
		if (_siege == null)
		{
			_siege = FortSiegeManager.getInstance().getSiege(_player.getX(), _player.getY(), _player.getZ());
		}
		if (_clan == null || _siege == null)
		{
			throw new NullPointerException(getClass().getSimpleName() + ": Initialization failed.");
		}
		else
		{
			L2SiegeClan sc = _siege.getAttackerClan(_clan);
			if (sc == null)
			{
				throw new NullPointerException(getClass().getSimpleName() + ": Cannot find siege clan.");
			}
			else
			{
				sc.addFlag(this);
			}
		}
		_isAdvanced = advanced;
		getStatus();
		setIsInvul(false);
	}

	/**
	 * Use L2SiegeFlagInstance(L2PcInstance, int, L2NpcTemplate, boolean) instead
	 */
	@Deprecated
	public L2SiegeFlagInstance(L2PcInstance player, int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		_isAdvanced = false;
	}

	@Override
	public boolean isAttackable()
	{
		return !isInvul();
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return !isInvul(attacker);
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		if (_siege != null && _clan != null)
		{
			L2SiegeClan sc = _siege.getAttackerClan(_clan);
			if (sc != null)
			{
				sc.removeFlag(this);
			}
		}

		return true;
	}

	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}

	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if (player == null || !canTarget(player))
		{
			return;
		}

		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);

			// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);

			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else if (interact)
		{
			if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100)
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
			else
			{
				// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}

	public boolean isAdvancedHeadquarter()
	{
		return _isAdvanced;
	}

	@Override
	public SiegeFlagStatus getStatus()
	{
		return (SiegeFlagStatus) super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		setStatus(new SiegeFlagStatus(this));
	}

	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, L2Skill skill)
	{
		super.reduceCurrentHp(damage, attacker, skill);
		if (canTalk())
		{
			if (getCastle() != null && getCastle().getSiege().getIsInProgress())
			{
				if (_clan != null)
				{
					// send warning to owners of headquarters that theirs base is under attack
					_clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.BASE_UNDER_ATTACK));
					setCanTalk(false);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTalkTask(), 20000);
				}
			}
			else if (getFort() != null && getFort().getSiege().getIsInProgress())
			{
				if (_clan != null)
				{
					// send warning to owners of headquarters that theirs base is under attack
					_clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.BASE_UNDER_ATTACK));
					setCanTalk(false);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTalkTask(), 20000);
				}
			}
		}
	}

	private class ScheduleTalkTask implements Runnable
	{

		public ScheduleTalkTask()
		{
		}

		@Override
		public void run()
		{
			setCanTalk(true);
		}
	}

	void setCanTalk(boolean val)
	{
		_canTalk = val;
	}

	private boolean canTalk()
	{
		return _canTalk;
	}
}
