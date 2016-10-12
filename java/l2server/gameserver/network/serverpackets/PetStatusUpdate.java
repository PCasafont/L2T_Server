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

	private L2Summon _summon;
	private int _maxHp, _maxMp;
	private int _maxFed, _curFed;

	public PetStatusUpdate(L2Summon summon)
	{
		_summon = summon;
		_maxHp = _summon.getMaxVisibleHp();
		_maxMp = _summon.getMaxMp();
		if (_summon instanceof L2PetInstance)
		{
			L2PetInstance pet = (L2PetInstance) _summon;
			_curFed = pet.getCurrentFed(); // how fed it is
			_maxFed = pet.getMaxFed(); //max fed it can be
		}
		else if (_summon instanceof L2SummonInstance)
		{
			L2SummonInstance sum = (L2SummonInstance) _summon;
			_curFed = sum.getTimeRemaining();
			_maxFed = sum.getTotalLifeTime();
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_summon.getSummonType());
		writeD(_summon.getObjectId());
		writeD(_summon.getX());
		writeD(_summon.getY());
		writeD(_summon.getZ());
		writeS("");
		writeD(_curFed);
		writeD(_maxFed);
		writeD((int) _summon.getCurrentHp());
		writeD(_maxHp);
		writeD((int) _summon.getCurrentMp());
		writeD(_maxMp);
		writeD(_summon.getLevel());
		writeQ(_summon.getStat().getExp());
		writeQ(_summon.getExpForThisLevel()); // 0% absolute value
		writeQ(_summon.getExpForNextLevel()); // 100% absolute value
		writeD(0); // ???
	}
}
