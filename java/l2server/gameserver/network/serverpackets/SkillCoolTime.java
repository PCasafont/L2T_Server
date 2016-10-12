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

import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance.TimeStamp;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author KenM
 */
public class SkillCoolTime extends L2GameServerPacket
{
	public Collection<TimeStamp> _reuseTimeStamps;

	public SkillCoolTime(L2PcInstance cha)
	{
		_reuseTimeStamps = cha.getReuseTimeStamps();
		Iterator<TimeStamp> iter = _reuseTimeStamps.iterator();
		while (iter.hasNext())
		{
			if (!iter.next().hasNotPassed()) // remove expired timestamps
			{
				iter.remove();
			}
		}
	}

    /*
	  @see l2server.gameserver.network.serverpackets.L2GameServerPacket#getType()
     */

	/**
	 * @see l2server.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(_reuseTimeStamps.size()); // list size
		for (TimeStamp ts : _reuseTimeStamps)
		{
			writeD(ts.getSkillId());
			writeD(0x00);
			writeD((int) ts.getReuse() / 1000);
			writeD((int) ts.getRemaining() / 1000);
		}
	}
}
