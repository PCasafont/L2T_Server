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
		this.clanHall = ClanHall;
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
		writeD(this.clanHall.getId()); // clanhall id
		//FUNC_RESTORE_HP
		this.function = this.clanHall.getFunction(ClanHall.FUNC_RESTORE_HP);
		if (this.function == null || this.function.getLvl() == 0)
		{
			writeC(0);
		}
		else if (this.clanHall.getGrade() == 0 && this.function.getLvl() < 220 ||
				this.clanHall.getGrade() == 1 && this.function.getLvl() < 160 ||
				this.clanHall.getGrade() == 2 && this.function.getLvl() < 260 ||
				this.clanHall.getGrade() == 3 && this.function.getLvl() < 300)
		{
			writeC(1);
		}
		else
		{
			writeC(2);
		}
		//FUNC_RESTORE_MP
		this.function = this.clanHall.getFunction(ClanHall.FUNC_RESTORE_MP);
		if (this.function == null || this.function.getLvl() == 0)
		{
			writeC(0);
			writeC(0);
		}
		else if ((this.clanHall.getGrade() == 0 || this.clanHall.getGrade() == 1) && this.function.getLvl() < 25 ||
				this.clanHall.getGrade() == 2 && this.function.getLvl() < 30 ||
				this.clanHall.getGrade() == 3 && this.function.getLvl() < 40)
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
		this.function = this.clanHall.getFunction(ClanHall.FUNC_RESTORE_EXP);
		if (this.function == null || this.function.getLvl() == 0)
		{
			writeC(0);
		}
		else if (this.clanHall.getGrade() == 0 && this.function.getLvl() < 25 ||
				this.clanHall.getGrade() == 1 && this.function.getLvl() < 30 ||
				this.clanHall.getGrade() == 2 && this.function.getLvl() < 40 ||
				this.clanHall.getGrade() == 3 && this.function.getLvl() < 50)
		{
			writeC(1);
		}
		else
		{
			writeC(2);
		}
		// FUNC_TELEPORT
		this.function = this.clanHall.getFunction(ClanHall.FUNC_TELEPORT);
		if (this.function == null || this.function.getLvl() == 0)
		{
			writeC(0);
		}
		else if (this.function.getLvl() < 2)
		{
			writeC(1);
		}
		else
		{
			writeC(2);
		}
		writeC(0);
		//CURTAINS
		this.function = this.clanHall.getFunction(ClanHall.FUNC_DECO_CURTAINS);
		if (this.function == null || this.function.getLvl() == 0)
		{
			writeC(0);
		}
		else if (this.function.getLvl() <= 1)
		{
			writeC(1);
		}
		else
		{
			writeC(2);
		}
		//FUNC_ITEM_CREATE
		this.function = this.clanHall.getFunction(ClanHall.FUNC_ITEM_CREATE);
		if (this.function == null || this.function.getLvl() == 0)
		{
			writeC(0);
		}
		else if (this.clanHall.getGrade() == 0 && this.function.getLvl() < 2 || this.function.getLvl() < 3)
		{
			writeC(1);
		}
		else
		{
			writeC(2);
		}
		// FUNC_SUPPORT
		this.function = this.clanHall.getFunction(ClanHall.FUNC_SUPPORT);
		if (this.function == null || this.function.getLvl() == 0)
		{
			writeC(0);
			writeC(0);
		}
		else if (this.clanHall.getGrade() == 0 && this.function.getLvl() < 2 ||
				this.clanHall.getGrade() == 1 && this.function.getLvl() < 4 ||
				this.clanHall.getGrade() == 2 && this.function.getLvl() < 5 ||
				this.clanHall.getGrade() == 3 && this.function.getLvl() < 8)
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
		this.function = this.clanHall.getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM);
		if (this.function == null || this.function.getLvl() == 0)
		{
			writeC(0);
		}
		else if (this.function.getLvl() <= 1)
		{
			writeC(1);
		}
		else
		{
			writeC(2);
		}
		//FUNC_ITEM_CREATE
		this.function = this.clanHall.getFunction(ClanHall.FUNC_ITEM_CREATE);
		if (this.function == null || this.function.getLvl() == 0)
		{
			writeC(0);
		}
		else if (this.clanHall.getGrade() == 0 && this.function.getLvl() < 2 || this.function.getLvl() < 3)
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
