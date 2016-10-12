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

import l2server.gameserver.model.quest.QuestState;

/**
 * @author Pere
 */
public class ExQuestNpcLogList extends L2GameServerPacket
{

	private QuestState _state;

	public ExQuestNpcLogList(QuestState st)
	{
		_state = st;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_state.getQuest().getQuestIntId());
		writeC(_state.getNpcLogs().size());
		for (int npcId : _state.getNpcLogs().keys())
		{
			writeD(npcId + 1000000);
			writeC(0x00); // ???
			writeD(_state.getNpcLogs().get(npcId));
		}
	}
}
