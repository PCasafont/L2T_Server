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

import l2server.gameserver.model.entity.ClanHall;
import l2server.gameserver.model.entity.ClanHall.ClanHallFunction;

/**
 * @author Steuf
 */
public class AgitDecoInfo extends L2GameServerPacket
{
	private ClanHall clanHall;
	private ClanHallFunction function;

	public AgitDecoInfo(ClanHall ClanHall)
	{
		clanHall = ClanHall;
	}

	/*
	 * Packet send, must be confirmed
		 writeC(0xf7);
		writeD(0); // clanhall id
		writeC(0); // FUNC_RESTORE_HP (Fireplace)
		writeC(0); // FUNC_RESTORE_MP (Carpet)
		writeC(0); // FUNC_RESTORE_MP (Statue)
		writeC(0); // FUNC_RESTORE_EXP (Chandelier)
		writeC(0); // FUNC_TELEPORT (Mirror)
		writeC(0); // Crytal
		writeC(0); // Curtain
		writeC(0); // FUNC_ITEM_CREATE (Magic Curtain)
		writeC(0); // FUNC_SUPPORT
		writeC(0); // FUNC_SUPPORT (Flag)
		writeC(0); // Front Platform
		writeC(0); // FUNC_ITEM_CREATE
		writeD(0);
		writeD(0);
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(clanHall.getId()); // clanhall id
		//FUNC_RESTORE_HP
		function = clanHall.getFunction(ClanHall.FUNC_RESTORE_HP);
		if (function == null || function.getLvl() == 0)
		{
			writeC(0);
		}
		else if (clanHall.getGrade() == 0 && function.getLvl() < 220 ||
				clanHall.getGrade() == 1 && function.getLvl() < 160 ||
				clanHall.getGrade() == 2 && function.getLvl() < 260 ||
				clanHall.getGrade() == 3 && function.getLvl() < 300)
		{
			writeC(1);
		}
		else
		{
			writeC(2);
		}
		//FUNC_RESTORE_MP
		function = clanHall.getFunction(ClanHall.FUNC_RESTORE_MP);
		if (function == null || function.getLvl() == 0)
		{
			writeC(0);
			writeC(0);
		}
		else if ((clanHall.getGrade() == 0 || clanHall.getGrade() == 1) && function.getLvl() < 25 ||
				clanHall.getGrade() == 2 && function.getLvl() < 30 ||
				clanHall.getGrade() == 3 && function.getLvl() < 40)
		{
			writeC(1);
			writeC(1);
		}
		else
		{
			writeC(2);
			writeC(2);
		}
		//FUNC_RESTORE_EXP
		function = clanHall.getFunction(ClanHall.FUNC_RESTORE_EXP);
		if (function == null || function.getLvl() == 0)
		{
			writeC(0);
		}
		else if (clanHall.getGrade() == 0 && function.getLvl() < 25 ||
				clanHall.getGrade() == 1 && function.getLvl() < 30 ||
				clanHall.getGrade() == 2 && function.getLvl() < 40 ||
				clanHall.getGrade() == 3 && function.getLvl() < 50)
		{
			writeC(1);
		}
		else
		{
			writeC(2);
		}
		// FUNC_TELEPORT
		function = clanHall.getFunction(ClanHall.FUNC_TELEPORT);
		if (function == null || function.getLvl() == 0)
		{
			writeC(0);
		}
		else if (function.getLvl() < 2)
		{
			writeC(1);
		}
		else
		{
			writeC(2);
		}
		writeC(0);
		//CURTAINS
		function = clanHall.getFunction(ClanHall.FUNC_DECO_CURTAINS);
		if (function == null || function.getLvl() == 0)
		{
			writeC(0);
		}
		else if (function.getLvl() <= 1)
		{
			writeC(1);
		}
		else
		{
			writeC(2);
		}
		//FUNC_ITEM_CREATE
		function = clanHall.getFunction(ClanHall.FUNC_ITEM_CREATE);
		if (function == null || function.getLvl() == 0)
		{
			writeC(0);
		}
		else if (clanHall.getGrade() == 0 && function.getLvl() < 2 || function.getLvl() < 3)
		{
			writeC(1);
		}
		else
		{
			writeC(2);
		}
		// FUNC_SUPPORT
		function = clanHall.getFunction(ClanHall.FUNC_SUPPORT);
		if (function == null || function.getLvl() == 0)
		{
			writeC(0);
			writeC(0);
		}
		else if (clanHall.getGrade() == 0 && function.getLvl() < 2 ||
				clanHall.getGrade() == 1 && function.getLvl() < 4 ||
				clanHall.getGrade() == 2 && function.getLvl() < 5 ||
				clanHall.getGrade() == 3 && function.getLvl() < 8)
		{
			writeC(1);
			writeC(1);
		}
		else
		{
			writeC(2);
			writeC(2);
		}
		//Front Plateform
		function = clanHall.getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM);
		if (function == null || function.getLvl() == 0)
		{
			writeC(0);
		}
		else if (function.getLvl() <= 1)
		{
			writeC(1);
		}
		else
		{
			writeC(2);
		}
		//FUNC_ITEM_CREATE
		function = clanHall.getFunction(ClanHall.FUNC_ITEM_CREATE);
		if (function == null || function.getLvl() == 0)
		{
			writeC(0);
		}
		else if (clanHall.getGrade() == 0 && function.getLvl() < 2 || function.getLvl() < 3)
		{
			writeC(1);
		}
		else
		{
			writeC(2);
		}
		writeD(0);
		writeD(0);
	}
}
