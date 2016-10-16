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
	private boolean receivedBoard;
	private int[] changedMsgIds;
	private int changeId;

	public ExChangePostState(boolean receivedBoard, int[] changedMsgIds, int changeId)
	{
		this.receivedBoard = receivedBoard;
		this.changedMsgIds = changedMsgIds;
		this.changeId = changeId;
	}

	public ExChangePostState(boolean receivedBoard, int changedMsgId, int changeId)
	{
		this.receivedBoard = receivedBoard;
		changedMsgIds = new int[]{changedMsgId};
		this.changeId = changeId;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(receivedBoard ? 1 : 0);
		writeD(changedMsgIds.length);
		for (int postId : changedMsgIds)
		{
			writeD(postId); // postId
			writeD(changeId); // state
		}
	}
}
