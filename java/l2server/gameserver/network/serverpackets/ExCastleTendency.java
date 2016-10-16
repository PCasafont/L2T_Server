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
public class ExCastleTendency extends L2GameServerPacket
{
	private int castleId;
	private int tendency;

	public ExCastleTendency(int castleId, int tendency)
	{
		this.castleId = castleId;
		this.tendency = tendency;
	}

	/**
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(this.castleId);
		writeD(this.tendency);
	}
}
