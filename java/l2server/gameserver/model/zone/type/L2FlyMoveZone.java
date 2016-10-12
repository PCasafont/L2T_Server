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

package l2server.gameserver.model.zone.type;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.PlayerClassTable;
import l2server.gameserver.model.L2FlyMove;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExNotifyFlyMoveStart;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Pere
 */
public class L2FlyMoveZone extends L2ZoneType
{
	private L2FlyMove _flyMove;

	public L2FlyMoveZone(int id)
	{
		super(id + 70500);
	}

	public void setFlyMove(L2FlyMove move)
	{
		_flyMove = move;
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (!(character instanceof L2PcInstance))
		{
			return;
		}

		L2PcInstance player = (L2PcInstance) character;

		if (PlayerClassTable.getInstance().getClassById(player.getBaseClass()).getLevel() < 85 ||
				player.getReputation() < 0 || player.isMounted() || player.isTransformed())
		{
			return;
		}

		if (!player.getSummons().isEmpty())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_USE_SAYUNE_WITH_PET));
			return;
		}

		player.setFlyMove(_flyMove);

		ThreadPoolManager.getInstance().scheduleGeneral(new FlyMoveStartSendTask((L2PcInstance) character), 10L);
	}

	@Override
	protected void onExit(L2Character character)
	{
	}

	@Override
	public void onDieInside(L2Character character, L2Character killer)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}

	private class FlyMoveStartSendTask implements Runnable
	{
		L2PcInstance _player;

		public FlyMoveStartSendTask(L2PcInstance player)
		{
			_player = player;
		}

		@Override
		public void run()
		{
			if (!isCharacterInZone(_player))
			{
				return;
			}

			if (!(_player.isPerformingFlyMove() && _player.isChoosingFlyMove()))
			{
				_player.sendPacket(new ExNotifyFlyMoveStart());
			}

			ThreadPoolManager.getInstance().scheduleGeneral(this, 1000L);
		}
	}
}
