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

package l2server.gameserver.model.actor.knownlist;

import l2server.gameserver.MonsterRace;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2RaceManagerInstance;
import l2server.gameserver.network.serverpackets.DeleteObject;

public class RaceManagerKnownList extends NpcKnownList
{
	// =========================================================
	// Data Field

	// =========================================================
	// Constructor
	public RaceManagerKnownList(L2RaceManagerInstance activeChar)
	{
		super(activeChar);
	}

	// =========================================================
	// Method - Public
	@Override
	public boolean addKnownObject(L2Object object)
	{
		if (!super.addKnownObject(object))
		{
			return false;
		}

		/* DONT KNOW WHY WE NEED THIS WHEN RACE MANAGER HAS A METHOD THAT BROADCAST TO ITS KNOW PLAYERS
		if (object instanceof L2PcInstance) {
			if (packet != null)
				((L2PcInstance) object).sendPacket(packet);
		}
		 */

		return true;
	}

	@Override
	protected boolean removeKnownObject(L2Object object, boolean forget)
	{
		if (!super.removeKnownObject(object, forget))
		{
			return false;
		}

		if (object instanceof L2PcInstance)
		{
			//Logozo.info("Sending delete monsrac info.");
			DeleteObject obj = null;
			for (int i = 0; i < 8; i++)
			{
				obj = new DeleteObject(MonsterRace.getInstance().getMonsters()[i]);
				object.sendPacket(obj);
			}
		}

		return true;
	}

	// =========================================================
	// Method - Private

	// =========================================================
	// Property - Public
	@Override
	public L2RaceManagerInstance getActiveChar()
	{
		return (L2RaceManagerInstance) super.getActiveChar();
	}
}
