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

/**
 * @author Pere
 */
public class ExChangePostState extends L2GameServerPacket
{

	private boolean _receivedBoard;
	private int[] _changedMsgIds;
	private int _changeId;

	public ExChangePostState(boolean receivedBoard, int[] changedMsgIds, int changeId)
	{
		_receivedBoard = receivedBoard;
		_changedMsgIds = changedMsgIds;
		_changeId = changeId;
	}

	public ExChangePostState(boolean receivedBoard, int changedMsgId, int changeId)
	{
		_receivedBoard = receivedBoard;
		_changedMsgIds = new int[]{changedMsgId};
		_changeId = changeId;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(_receivedBoard ? 1 : 0);
		writeD(_changedMsgIds.length);
		for (int postId : _changedMsgIds)
		{
			writeD(postId); // postId
			writeD(_changeId); // state
		}
	}
}
