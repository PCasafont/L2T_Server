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

import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;

/**
 * This class ...
 *
 * @version $Revision: 1.5.2.3.2.5 $ $Date: 2005/03/29 23:15:10 $
 */
public class PetStatusUpdate extends L2GameServerPacket
{

	private L2Summon summon;
	private int maxHp, maxMp;
	private int maxFed, curFed;

	public PetStatusUpdate(L2Summon summon)
	{
		this.summon = summon;
		maxHp = summon.getMaxVisibleHp();
		maxMp = summon.getMaxMp();
		if (summon instanceof L2PetInstance)
		{
			L2PetInstance pet = (L2PetInstance) summon;
			curFed = pet.getCurrentFed(); // how fed it is
			maxFed = pet.getMaxFed(); //max fed it can be
		}
		else if (summon instanceof L2SummonInstance)
		{
			L2SummonInstance sum = (L2SummonInstance) summon;
			curFed = sum.getTimeRemaining();
			maxFed = sum.getTotalLifeTime();
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(summon.getSummonType());
		writeD(summon.getObjectId());
		writeD(summon.getX());
		writeD(summon.getY());
		writeD(summon.getZ());
		writeS("");
		writeD(curFed);
		writeD(maxFed);
		writeD((int) summon.getCurrentHp());
		writeD(maxHp);
		writeD((int) summon.getCurrentMp());
		writeD(maxMp);
		writeD(summon.getLevel());
		writeQ(summon.getStat().getExp());
		writeQ(summon.getExpForThisLevel()); // 0% absolute value
		writeQ(summon.getExpForNextLevel()); // 100% absolute value
		writeD(0); // ???
	}
}
