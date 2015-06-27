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
 * This class ...
 *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public final class RestartResponse extends L2GameServerPacket
{
	private static final String _S__74_RESTARTRESPONSE = "[S] 71 RestartResponse";
	public static final RestartResponse STATIC_PACKET_TRUE = new RestartResponse(true);
	private static final RestartResponse STATIC_PACKET_FALSE = new RestartResponse(false);
	
	public static final RestartResponse valueOf(boolean result)
	{
		return result ? STATIC_PACKET_TRUE : STATIC_PACKET_FALSE;
	}
	
	private boolean _result;
	
	public RestartResponse(boolean result)
	{
		_result = result;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x71);
		writeD(_result ? 1 : 0);
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__74_RESTARTRESPONSE;
	}
}
