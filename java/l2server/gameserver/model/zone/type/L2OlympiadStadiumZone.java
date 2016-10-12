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
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2DoorInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.model.olympiad.Olympiad;
import l2server.gameserver.model.olympiad.OlympiadGameTask;
import l2server.gameserver.model.zone.L2SpawnZone;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExOlympiadMatchEnd;
import l2server.gameserver.network.serverpackets.ExOlympiadUserInfo;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.gameserver.network.serverpackets.SystemMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * An olympiad stadium
 *
 * @author durgus, DS
 */
public class L2OlympiadStadiumZone extends L2SpawnZone
{
	private final List<OlympiadGameTask> _instances;

	public L2OlympiadStadiumZone(int id)
	{
		super(id);
		_instances = new ArrayList<>(40);
	}

	public final void registerTask(OlympiadGameTask task, int id)
	{
		_instances.add(id / 4, task);
	}

	public final void broadcastStatusUpdate(L2PcInstance player)
	{
		final ExOlympiadUserInfo packet = new ExOlympiadUserInfo(player);
		for (L2Character character : _characterList.values())
		{
			if (character instanceof L2PcInstance && character.getInstanceId() == player.getInstanceId())
			{
				if (((L2PcInstance) character).inObserverMode() ||
						((L2PcInstance) character).getOlympiadSide() != player.getOlympiadSide())
				{
					character.sendPacket(packet);
				}
			}
		}
	}

	public final void broadcastPacketToObservers(L2GameServerPacket packet, int gameId)
	{
		for (L2Character character : _characterList.values())
		{
			if (character instanceof L2PcInstance && ((L2PcInstance) character).inObserverMode() &&
					character.getInstanceId() - Olympiad.BASE_INSTANCE_ID == gameId)
			{
				character.sendPacket(packet);
			}
		}
	}

	@Override
	protected final void onEnter(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, true);

		int instanceIndex = (character.getInstanceId() - Olympiad.BASE_INSTANCE_ID) / 4;
		if (instanceIndex < 0 || instanceIndex >= _instances.size())
		{
			return;
		}

		OlympiadGameTask task = _instances.get(instanceIndex);
		if (task == null)
		{
			return;
		}

		if (task.isBattleStarted())
		{
			character.setInsideZone(L2Character.ZONE_PVP, true);
			if (character instanceof L2PcInstance)
			{
				character.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
				task.getGame().sendOlympiadInfo(character);
			}
		}

		if (character instanceof L2Playable)
		{
			final L2PcInstance player = character.getActingPlayer();
			if (player != null)
			{
				// only participants, observers and GMs allowed
				if (!player.isGM() && !player.isInOlympiadMode() && !player.inObserverMode())
				{
					ThreadPoolManager.getInstance().executeTask(new KickPlayer(player));
				}
			}
		}
	}

	@Override
	protected final void onExit(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, false);

		int instanceIndex = (character.getInstanceId() - Olympiad.BASE_INSTANCE_ID) / 4;
		if (instanceIndex < 0 || instanceIndex >= _instances.size())
		{
			return;
		}

		OlympiadGameTask task = _instances.get(instanceIndex);
		if (task.isBattleStarted())
		{
			character.setInsideZone(L2Character.ZONE_PVP, false);
			if (character instanceof L2PcInstance)
			{
				character.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
				character.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET);
			}
		}

		if (character instanceof L2DoorInstance)
		{
			task.getDoors().remove(character);
		}
	}

	public final void updateZoneStatusForCharactersInside(int gameId)
	{
		int instanceIndex = gameId / 4;
		if (instanceIndex < 0 || instanceIndex >= _instances.size())
		{
			return;
		}

		OlympiadGameTask task = _instances.get(instanceIndex);
		if (task == null)
		{
			return;
		}

		final boolean battleStarted = task.isBattleStarted();
		final SystemMessage sm;
		if (battleStarted)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE);
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.LEFT_COMBAT_ZONE);
		}

		for (L2Character character : _characterList.values())
		{
			if (character == null || character.getInstanceId() - Olympiad.BASE_INSTANCE_ID != gameId)
			{
				continue;
			}

			if (battleStarted)
			{
				character.setInsideZone(L2Character.ZONE_PVP, true);
				if (character instanceof L2PcInstance)
				{
					character.sendPacket(sm);
				}
			}
			else
			{
				character.setInsideZone(L2Character.ZONE_PVP, false);
				if (character instanceof L2PcInstance)
				{
					character.sendPacket(sm);
					character.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET);
				}
			}
		}
	}

	@Override
	public void onDieInside(L2Character character, L2Character killer)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}

	private static final class KickPlayer implements Runnable
	{
		private L2PcInstance _player;

		public KickPlayer(L2PcInstance player)
		{
			_player = player;
		}

		@Override
		public void run()
		{
			if (_player != null)
			{
				final L2Summon pet = _player.getPet();
				if (pet != null)
				{
					pet.unSummon(_player);
				}
				for (L2SummonInstance summon : _player.getSummons())
				{
					summon.unSummon(_player);
				}

				_player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
				_player = null;
			}
		}
	}
}
