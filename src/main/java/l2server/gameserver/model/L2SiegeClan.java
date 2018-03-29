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

package l2server.gameserver.model;

import l2server.gameserver.model.actor.L2Npc;

import java.util.ArrayList;
import java.util.List;

public class L2SiegeClan
{
	// ==========================================================================================
	// Instance
	// ===============================================================
	// Data Field
	private int clanId = 0;
	private List<L2Npc> flag = new ArrayList<>();
	private int numFlagsAdded = 0;
	private SiegeClanType type;

	public enum SiegeClanType
	{
		OWNER, DEFENDER, ATTACKER, DEFENDER_PENDING
	}

	// =========================================================
	// Constructor

	public L2SiegeClan(int clanId, SiegeClanType type)
	{
		this.clanId = clanId;
		this.type = type;
	}

	// =========================================================
	// Method - Public
	public int getNumFlags()
	{
		return numFlagsAdded;
	}

	public void addFlag(L2Npc flag)
	{
		numFlagsAdded++;
		getFlag().add(flag);
	}

	public boolean removeFlag(L2Npc flag)
	{
		if (flag == null)
		{
			return false;
		}
		boolean ret = getFlag().remove(flag);
		//check if null objects or duplicates remain in the list.
		//for some reason, this might be happening sometimes...
		// delete false duplicates: if this flag got deleted, delete its copies too.
		if (ret)
		{
			while (getFlag().remove(flag))
			{
			}
		}

		flag.deleteMe();
		numFlagsAdded--;
		return ret;
	}

	public void removeFlags()
	{
		for (L2Npc flag : getFlag())
		{
			removeFlag(flag);
		}
	}

	// =========================================================
	// Property
	public final int getClanId()
	{
		return clanId;
	}

	public final List<L2Npc> getFlag()
	{
		if (flag == null)
		{
			flag = new ArrayList<>();
		}
		return flag;
	}

	public SiegeClanType getType()
	{
		return type;
	}

	public void setType(SiegeClanType setType)
	{
		type = setType;
	}
}
