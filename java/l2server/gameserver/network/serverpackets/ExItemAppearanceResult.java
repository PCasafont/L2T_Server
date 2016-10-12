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

import l2server.gameserver.model.L2ItemInstance;

/**
 * @author Pere
 */
public class ExItemAppearanceResult extends L2GameServerPacket
{
	private int _answer;
	L2ItemInstance _result;

	public ExItemAppearanceResult(int answer, L2ItemInstance result)
	{
		_answer = answer;
		_result = result;
	}

	/**
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(_answer);
		if (_answer == 1)
		{
			writeD(_result.getItemId());
			writeD(0x00); // GoD ???
			writeD(0x00); // GoD ???
			writeD(0x00); // GoD ???
			writeD(0x00); // GoD ???
			writeD(_result.getAppearance());
		}
	}
}
