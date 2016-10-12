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

import l2server.gameserver.datatables.AbilityTable;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Pere
 */
public class ExAcquireAPSkillList extends L2GameServerPacket
{
	private L2PcInstance _player;
	private boolean _success;

	public ExAcquireAPSkillList(L2PcInstance player, boolean success)
	{
		_player = player;
		_success = success;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_success ? 0x01 : 0x00);

		writeQ(AbilityTable.getInstance().getAdenaCostForReset());
		writeQ(AbilityTable.getInstance().getSpCostPerPoint(_player.getAbilityPoints()));
		writeD(AbilityTable.getInstance().getMaxPoints());
		writeD(_player.getAbilityPoints());
		writeD(_player.getSpentAbilityPoints());

		writeD(_player.getAbilities().size());
		for (int skillId : _player.getAbilities().keys())
		{
			writeD(skillId);
			writeD(_player.getAbilities().get(skillId));
		}

		writeD(0x01);
	}
}
