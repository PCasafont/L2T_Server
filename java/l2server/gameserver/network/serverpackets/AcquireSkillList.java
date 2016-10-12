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

import l2server.gameserver.datatables.SkillTreeTable;
import l2server.gameserver.model.L2SkillLearn;
import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.Map;

/**
 * @author Pere
 */
public final class AcquireSkillList extends L2GameServerPacket
{
	private L2SkillLearn[] _skills;
	private L2PcInstance _player;

	public AcquireSkillList(L2PcInstance player)
	{
		_skills = SkillTreeTable.getInstance().getAvailableClassSkills(player);
		_player = player;
	}

	@Override
	protected final void writeImpl()
	{
		writeH(_skills.length);
		for (L2SkillLearn sk : _skills)
		{
			writeD(sk.getId());
			writeH(sk.getLevel());
			writeH(0); // Skill enchant
			writeQ(sk.getSpCost());
			writeC(sk.getMinLevel());
			writeC(sk.getMinDualLevel());

			writeC(sk.getCostItems().size());
			for (int itemId : sk.getCostItems().keySet())
			{
				writeD(itemId);
				writeQ(sk.getCostItems().get(itemId));
			}

			Map<Integer, Integer> costSkills = sk.getCostSkills(_player);
			writeC(costSkills.size());
			for (int skillId : costSkills.keySet())
			{
				writeD(skillId);
				writeD(costSkills.get(skillId));
			}
		}
	}
}
