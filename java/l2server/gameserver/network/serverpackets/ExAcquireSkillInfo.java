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

import l2server.gameserver.model.L2SkillLearn;
import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.Map;

/**
 * @author Pere
 */
public class ExAcquireSkillInfo extends L2GameServerPacket
{
	private L2SkillLearn skill;
	private L2PcInstance player;

	public ExAcquireSkillInfo(L2SkillLearn skill, L2PcInstance player)
	{
		this.skill = skill;
		this.player = player;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(skill.getId());
		writeD(skill.getLevel());
		writeQ(skill.getSpCost());
		writeH(skill.getMinLevel());
		writeH(skill.getMinDualLevel());
		writeD(skill.getCostItems().size());
		for (int itemId : skill.getCostItems().keySet())
		{
			writeD(itemId);
			writeQ(skill.getCostItems().get(itemId));
		}

		Map<Integer, Integer> costSkills = skill.getCostSkills(player);
		writeD(costSkills.size());
		for (int skillId : costSkills.keySet())
		{
			writeD(skillId);
			writeD(costSkills.get(skillId));
		}
	}
}
