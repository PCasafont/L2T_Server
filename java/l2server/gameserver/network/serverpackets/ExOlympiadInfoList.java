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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.model.olympiad.CompetitionType;
import l2server.gameserver.model.olympiad.OlympiadGameManager;
import l2server.gameserver.model.olympiad.OlympiadGameTask;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class ExOlympiadInfoList extends L2GameServerPacket
{
	private List<OlympiadGameTask> _tasks;

	public ExOlympiadInfoList()
	{
		_tasks = new ArrayList<>();
		for (int i = 0; i < 160; i++)
		{
			OlympiadGameTask task = OlympiadGameManager.getInstance().getOlympiadTask(i);
			if (task.isRunning())
			{
				_tasks.add(task);
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_tasks.size());
		writeD(0x00); // This value makes the list be repeated multiple times
		for (OlympiadGameTask task : _tasks)
		{
			if (task == null || task.getGame() == null)
			{
				continue;
			}
			writeD(task.getGame().getGameId());
			writeD(task.getGame().getType() == CompetitionType.NON_CLASSED ? 1 : 2);
			writeD(task.isBattleStarted() ? 2 : task.isGameStarted() ? 1 : 0);
			writeS(task.getGame().getPlayerNames()[0]);
			writeS(task.getGame().getPlayerNames()[1]);
		}
	}

	@Override
	protected final Class<?> getOpCodeClass()
	{
		return ExOlympiadMatchList.class;
	}
}
