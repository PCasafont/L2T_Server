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

import java.util.ArrayList;
import java.util.List;

import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.olympiad.CompetitionType;
import l2server.gameserver.model.olympiad.Olympiad;
import l2server.gameserver.model.olympiad.OlympiadGameManager;
import l2server.gameserver.model.olympiad.OlympiadGameTask;

/**
 * @author Pere
 */
public class ExOlympiadInfoList extends L2GameServerPacket
{
	// chc
	private static final String _S__FE_D4_OLYMPIADINFOLIST = "[S] FE:D4 ExOlympiadInfoList";
	private int _type;
	private List<OlympiadGameTask> _tasks;
	private Object[] _info;
	
	public ExOlympiadInfoList()
	{
		_type = 0;
		_tasks = new ArrayList<OlympiadGameTask>();
		for (int i = 0; i < 160; i++)
		{
			OlympiadGameTask task = OlympiadGameManager.getInstance().getOlympiadTask(i);
			if (task.isRunning())
				_tasks.add(task);
		}
	}
	
	public ExOlympiadInfoList(Object[] info)
	{
		_type = 1;
		_info = info;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0xd5);
		writeD(_type);
		switch (_type)
		{
			case 0:
				writeD(_tasks.size());
				writeD(0x00); // This value makes the list be repeated multiple times
				for (OlympiadGameTask task : _tasks)
				{
					if (task == null || task.getGame() == null)
						continue;
					writeD(task.getGame().getGameId());
					writeD(task.getGame().getType() == CompetitionType.NON_CLASSED ? 1 : 2);
					writeD(task.isBattleStarted() ? 2 : (task.isGameStarted() ? 1 : 0));
					writeS(task.getGame().getPlayerNames()[0]);
					writeS(task.getGame().getPlayerNames()[1]);
				}
				break;
			case 1:
				writeD((Integer)_info[0] < 0 ? 1 : 0); // Victory or Tie
				writeS((String)_info[1]); // Winner
				for (int i = 0; i < 2; i++)
				{
					writeD(i + 1);
					int multiplier = (Integer)_info[0] == i ? 1 : -1;
					int participants = (Integer)_info[2];
					writeD(participants);
					for (int j = 0; j < participants; j++)
					{
						L2PcInstance player = (L2PcInstance)_info[i * participants + j + 4];
						if (player == null)
						{
							writeS("");
							writeS("");
							writeD(0x00);
							writeD(0x00);
							writeD(0x00);
							writeD(0x00);
							writeD(0x00);
						}
						else
						{
							writeS(player.getName());
							writeS(player.getClan() == null ? "" : player.getClan().getName());
							writeD(0x00); // ???
							writeD(player.getCurrentClass().getId());
							writeD(player.getOlyGivenDmg());
							writeD(Olympiad.getInstance().getNobleInfo(player.getObjectId()).getPoints());
							writeD((Integer)_info[3] * multiplier);
						}
					}
				}
		}
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_D4_OLYMPIADINFOLIST;
	}
}
