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

import java.util.ArrayList;
import java.util.List;

/**
 * <code>
 * sample
 * <p>
 * a4
 * 4d000000 01000000 98030000 			Attack Aura, level 1, sp cost
 * 01000000 							number of requirements
 * 05000000 47040000 0100000 000000000	   1 x spellbook advanced ATTACK												 .
 * </code>
 * <p>
 * format   dddd d (ddQd)
 *
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class AcquireSkillInfo extends L2GameServerPacket
{
	private List<Req> _reqs;
	private int _id, _level, _spCost, _mode;

	private static class Req
	{
		public int itemId;
		public int count;
		public int type;
		public int unk;

		public Req(int pType, int pItemId, int pCount, int pUnk)
		{
			itemId = pItemId;
			type = pType;
			count = pCount;
			unk = pUnk;
		}
	}

	public AcquireSkillInfo(int id, int level, int spCost, int mode)
	{
		_reqs = new ArrayList<>();
		_id = id;
		_level = level;
		_spCost = spCost;
		_mode = mode;
	}

	public void addRequirement(int type, int id, int count, int unk)
	{
		_reqs.add(new Req(type, id, count, unk));
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_id);
		writeD(_level);
		writeQ(_spCost);
		writeD(_mode); //c4

		writeD(_reqs.size());

		for (Req temp : _reqs)
		{
			writeD(temp.type);
			writeD(temp.itemId);
			writeQ(temp.count);
			writeD(temp.unk);
		}
	}
}
