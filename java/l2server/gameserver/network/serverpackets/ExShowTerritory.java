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

import l2server.gameserver.model.L2Territory;
import l2server.gameserver.model.L2Territory.Point;

/**
 * @author Pere
 *         It crashes the client on Ertheia!!
 */
public final class ExShowTerritory extends L2GameServerPacket
{
	private final L2Territory territory;

	public ExShowTerritory(L2Territory territory)
	{
		this.territory = territory;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(this.territory.getMinZ());
		writeD(this.territory.getMaxZ());
		writeD(this.territory.getPoints().size());
		for (Point p : this.territory.getPoints())
		{
			writeD(p.x);
			writeD(p.y);
		}
	}
}
