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

import l2server.gameserver.instancemanager.CursedWeaponsManager;
import l2server.gameserver.model.CursedWeapon;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.network.serverpackets.ExCursedWeaponLocation;
import l2server.gameserver.network.serverpackets.ExCursedWeaponLocation.CursedWeaponInfo;
import l2server.util.Point3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Format: (ch)
 *
 * @author -Wooden-
 */
public final class RequestCursedWeaponLocation extends L2GameClientPacket
{

	@Override
	protected void readImpl()
	{
		//nothing to read it's just a trigger
	}

	/**
	 */
	@Override
	protected void runImpl()
	{
		L2Character activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		List<CursedWeaponInfo> list = new ArrayList<>();
		for (CursedWeapon cw : CursedWeaponsManager.getInstance().getCursedWeapons())
		{
			if (!cw.isActive())
			{
				continue;
			}

			Point3D pos = cw.getWorldPosition();
			if (pos != null)
			{
				list.add(new CursedWeaponInfo(pos, cw.getItemId(), cw.isActivated() ? 1 : 0));
			}
		}

		//send the ExCursedWeaponLocation
		if (!list.isEmpty())
		{
			activeChar.sendPacket(new ExCursedWeaponLocation(list));
		}
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
