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

import l2server.util.Point3D;

import java.util.List;

/**
 * Format: (ch) d[ddddd]
 *
 * @author -Wooden-
 */
public class ExCursedWeaponLocation extends L2GameServerPacket
{
	private List<CursedWeaponInfo> _cursedWeaponInfo;

	public ExCursedWeaponLocation(List<CursedWeaponInfo> cursedWeaponInfo)
	{
		_cursedWeaponInfo = cursedWeaponInfo;
	}

	/**
	 */
	@Override
	protected final void writeImpl()
	{
		if (!_cursedWeaponInfo.isEmpty())
		{
			writeD(_cursedWeaponInfo.size());
			for (CursedWeaponInfo w : _cursedWeaponInfo)
			{
				writeD(w.id);
				writeD(w.activated);

				writeD(w.pos.getX());
				writeD(w.pos.getY());
				writeD(w.pos.getZ());
			}
		}
		else
		{
			writeD(0);
			writeD(0);
		}
	}

	public static class CursedWeaponInfo
	{
		public Point3D pos;
		public int id;
		public int activated; //0 - not activated ? 1 - activated

		public CursedWeaponInfo(Point3D p, int ID, int status)
		{
			pos = p;
			id = ID;
			activated = status;
		}
	}
}
