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
		this.maxHp = this.summon.getMaxVisibleHp();
		this.maxMp = this.summon.getMaxMp();
		if (this.summon instanceof L2PetInstance)
		{
			L2PetInstance pet = (L2PetInstance) this.summon;
			this.curFed = pet.getCurrentFed(); // how fed it is
			this.maxFed = pet.getMaxFed(); //max fed it can be
		}
		else if (this.summon instanceof L2SummonInstance)
		{
			L2SummonInstance sum = (L2SummonInstance) this.summon;
			this.curFed = sum.getTimeRemaining();
			this.maxFed = sum.getTotalLifeTime();
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(this.summon.getSummonType());
		writeD(this.summon.getObjectId());
		writeD(this.summon.getX());
		writeD(this.summon.getY());
		writeD(this.summon.getZ());
		writeS("");
		writeD(this.curFed);
		writeD(this.maxFed);
		writeD((int) this.summon.getCurrentHp());
		writeD(this.maxHp);
		writeD((int) this.summon.getCurrentMp());
		writeD(this.maxMp);
		writeD(this.summon.getLevel());
		writeQ(this.summon.getStat().getExp());
		writeQ(this.summon.getExpForThisLevel()); // 0% absolute value
		writeQ(this.summon.getExpForNextLevel()); // 100% absolute value
		writeD(0); // ???
	}
}
