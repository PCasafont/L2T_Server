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
public class ExNoticePostArrived extends L2GameServerPacket
{
	private static final ExNoticePostArrived STATIC_PACKET_TRUE = new ExNoticePostArrived(true);
	private static final ExNoticePostArrived STATIC_PACKET_FALSE = new ExNoticePostArrived(false);

	public static ExNoticePostArrived valueOf(boolean result)
	{
		return result ? STATIC_PACKET_TRUE : STATIC_PACKET_FALSE;
	}

	boolean _showAnim;

	public ExNoticePostArrived(boolean showAnimation)
	{
		_showAnim = showAnimation;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(_showAnim ? 0x01 : 0x00);
	}
}
