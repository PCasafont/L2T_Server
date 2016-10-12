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

import l2server.gameserver.GeoData;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.MyTargetSelected;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.ValidateLocation;
import l2server.gameserver.templates.chars.L2NpcTemplate;

import java.util.ArrayList;
import java.util.List;

public class L2ControlTowerInstance extends L2Npc
{
	private List<L2Spawn> _guards;

	public L2ControlTowerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2ControlTowerInstance);
		setIsInvul(false);
	}

	@Override
	public boolean isAttackable()
	{
		// Attackable during siege by attacker only
		return getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress();
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		// Attackable during siege by attacker only
		return attacker != null && attacker instanceof L2PcInstance && getCastle() != null &&
				getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress() &&
				getCastle().getSiege().checkIsAttacker(((L2PcInstance) attacker).getClan());
	}

	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}

	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if (!canTarget(player))
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
			if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100 &&
					GeoData.getInstance().canSeeTarget(player, this))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (getCastle().getSiege().getIsInProgress())
		{
			getCastle().getSiege().killedCT(this);

			if (_guards != null && !_guards.isEmpty())
			{
				for (L2Spawn spawn : _guards)
				{
					if (spawn == null)
					{
						continue;
					}
					try
					{
						spawn.stopRespawn();
						//spawn.getNpc().doDie(spawn.getNpc());
					}
					catch (Exception ignored)
					{
					}
				}
				_guards.clear();
			}
		}
		return super.doDie(killer);
	}

	public void registerGuard(L2Spawn guard)
	{
		getGuards().add(guard);
	}

	public final List<L2Spawn> getGuards()
	{
		if (_guards == null)
		{
			synchronized (this)
			{
				if (_guards == null)
				{
					_guards = new ArrayList<>();
				}
			}
		}

		return _guards;
	}
}
