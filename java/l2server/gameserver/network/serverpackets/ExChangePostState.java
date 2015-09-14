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
	private static final String _S__FE_B3_EXCHANGEPOSTSTATE = "[S] B3 ExChangePostState";
	
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
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0xb4);
		writeD(_receivedBoard ? 1 : 0);
		writeD(_changedMsgIds.length);
		for (int postId : _changedMsgIds)
		{
			writeD(postId); // postId
			writeD(_changeId); // state
		}
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_B3_EXCHANGEPOSTSTATE;
	}
}