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
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.network.serverpackets.ExCursedWeaponList;

import java.util.ArrayList;
import java.util.List;

/**
 * Format: (ch)
 *
 * @author -Wooden-
 */
public class RequestCursedWeaponList extends L2GameClientPacket
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

		//send a ExCursedWeaponList :p
		List<Integer> list = new ArrayList<>();
		for (int id : CursedWeaponsManager.getInstance().getCursedWeaponsIds())
		{
			list.add(id);
		}

		activeChar.sendPacket(new ExCursedWeaponList(list));
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
