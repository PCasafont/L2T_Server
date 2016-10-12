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

public class ExAttributeEnchantResult extends L2GameServerPacket
{

	private int _addedPower;
	private int _totalPower;
	private int _succeeded;
	private int _failed;

	public ExAttributeEnchantResult(int addedPower, int totalPower, int succeeded, int failed)
	{
		_addedPower = addedPower;
		_totalPower = totalPower;
		_succeeded = succeeded;
		_failed = failed;
	}

	@Override
	protected final void writeImpl()
	{
		//writeD(_result);
		writeD(0);
		writeC(0);
		writeH(4); // ???
		writeH(_addedPower);
		writeH(_totalPower);
		writeH(_succeeded); // Successful stones
		writeH(_failed); // Failed stones
	}
}
