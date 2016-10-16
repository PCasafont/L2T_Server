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
 * Format: (ch)ddd
 */
public class ExVariationResult extends L2GameServerPacket
{
	private int stat12;
	private int stat34;
	private int unk3;

	public ExVariationResult(int unk1, int unk2, int unk3)
	{
		stat12 = unk1;
		stat34 = unk2;
		this.unk3 = unk3;
	}

	/**
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(stat12);
		writeD(stat34);
		writeD(unk3);
	}
}
