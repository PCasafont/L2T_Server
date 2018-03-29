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

	private int addedPower;
	private int totalPower;
	private int succeeded;
	private int failed;

	public ExAttributeEnchantResult(int addedPower, int totalPower, int succeeded, int failed)
	{
		this.addedPower = addedPower;
		this.totalPower = totalPower;
		this.succeeded = succeeded;
		this.failed = failed;
	}

	@Override
	protected final void writeImpl()
	{
		//writeD(result);
		writeD(0);
		writeC(0);
		writeH(4); // ???
		writeH(addedPower);
		writeH(totalPower);
		writeH(succeeded); // Successful stones
		writeH(failed); // Failed stones
	}
}
