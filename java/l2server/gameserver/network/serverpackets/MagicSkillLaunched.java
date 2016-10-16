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

import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.actor.L2Character;

/**
 * sample
 * <p>
 * 0000: 8e  d8 a8 10 48  10 04 00 00  01 00 00 00  01 00 00	....H...........
 * 0010: 00  d8 a8 10 48									 ....H
 * <p>
 * <p>
 * format   ddddd d
 *
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class MagicSkillLaunched extends L2GameServerPacket
{
	private int charObjId;
	private int skillId;
	private int skillLevel;
	private int numberOfTargets;
	private L2Object[] targets;
	private int singleTargetId;

	public MagicSkillLaunched(L2Character cha, int skillId, int skillLevel, L2Object[] targets)
	{
		this.charObjId = cha.getObjectId();
		this.skillId = skillId;
		this.skillLevel = skillLevel;

		if (targets != null)
		{
			this.numberOfTargets = targets.length;
			this.targets = targets;
		}
		else
		{
			this.numberOfTargets = 1;
			this.targets = new L2Object[]{cha};
		}
		this.singleTargetId = 0;
	}

	public MagicSkillLaunched(L2Character cha, int skillId, int skillLevel)
	{
		this.charObjId = cha.getObjectId();
		this.skillId = skillId;
		this.skillLevel = skillLevel;
		this.numberOfTargets = 1;
		this.singleTargetId = cha.getTargetId();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(0x02); // GoD ??? (if 1, party skills cannot be seen)
		writeD(this.charObjId);
		writeD(this.skillId);
		writeD(this.skillLevel);
		writeD(this.numberOfTargets); // also failed or not?
		if (this.singleTargetId != 0 || this.numberOfTargets == 0)
		{
			writeD(this.singleTargetId);
		}
		else
		{
			for (L2Object target : this.targets)
			{
				writeD(target.getObjectId());
			}
		}
	}
}
