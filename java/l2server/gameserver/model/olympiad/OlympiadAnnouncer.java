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

package l2server.gameserver.model.olympiad;

import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.NpcSay;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DS
 */
public final class OlympiadAnnouncer implements Runnable
{
	private static final int OLY_MANAGER = 31688;

	private List<L2Spawn> _managers = new ArrayList<>();
	private int _currentStadium = 0;

	public OlympiadAnnouncer()
	{
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable())
		{
			if (spawn != null && spawn.getNpcId() == OLY_MANAGER)
			{
				_managers.add(spawn);
			}
		}
	}

	@Override
	public void run()
	{
		OlympiadGameTask task;
		for (int i = OlympiadGameManager.getInstance().getNumberOfStadiums(); --i >= 0; _currentStadium++)
		{
			if (_currentStadium >= OlympiadGameManager.getInstance().getNumberOfStadiums())
			{
				_currentStadium = 0;
			}

			task = OlympiadGameManager.getInstance().getOlympiadTask(_currentStadium);
			if (task != null && task.getGame() != null && task.needAnnounce())
			{
				int msg;
				final String arenaId = String.valueOf(task.getGame().getGameId() + 1);
				switch (task.getGame().getType())
				{
					case NON_CLASSED:
						// msg = "Olympiad class-free individual match is going to begin in Arena " + arenaId + " in a moment.";
						msg = 1300166;
						break;
					case CLASSED:
						// msg = "Olympiad class-specific individual match is going to begin in Arena " + arenaId + " in a moment.";
						msg = 1300167;
						break;
					default:
						continue;
				}

				L2Npc manager;
				NpcSay packet;
				for (L2Spawn spawn : _managers)
				{
					manager = spawn.getNpc();
					if (manager != null)
					{
						packet = new NpcSay(manager.getObjectId(), Say2.ALL_NOT_RECORDED, manager.getNpcId(), msg);
						packet.addStringParameter(arenaId);
						manager.broadcastPacket(packet);
					}
				}
				break;
			}
		}
	}
}
