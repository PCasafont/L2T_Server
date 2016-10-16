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

package l2server.gameserver.network.clientpackets;

import l2server.gameserver.network.serverpackets.ExLoadStatWorldRank;

/**
 * @author Pere
 */
public final class RequestWorldStatistics extends L2GameClientPacket
{
	private int pId1;
	private int pId2;

	@Override
	protected void readImpl()
	{
		this.pId1 = readD();
		this.pId2 = readD();
	}

	/**
	 */
	@Override
	protected void runImpl()
	{
		if (getClient().getActiveChar() == null)
		//|| MuseumStatistic.get(this.pId1, this.pId2) == null)
		{
			return;
		}

		sendPacket(new ExLoadStatWorldRank(this.pId1, this.pId2));
	}
}
