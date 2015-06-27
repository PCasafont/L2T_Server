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

import l2server.Config;
import l2server.gameserver.model.L2CrystallizeReward;
import l2server.gameserver.templates.item.L2Item;

/**
 *
 * @author Erlandys
 */
public class ExGetCrystalizingEstimation extends L2GameServerPacket
{
	private static final String _S__FE_E0_EXGETCRYSTALIZINGESTIMATION = "[S] FE:E1 ExGetCrystalizingEstimation";

	private L2Item _item;
	private long _crystalCount;

	public ExGetCrystalizingEstimation(L2Item item, long crystalCount)
	{
		_item = item;
		_crystalCount = crystalCount;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xE1);
		
		if (Config.ENABLE_CRYSTALLIZE_REWARDS && _item.getCrystallizeRewards() != null)
		{
			writeD(_item.getCrystallizeRewards().length + 1);
			writeD(_item.getCrystalItemId());
			writeQ(_crystalCount);
			writeF(100.0);
			for (L2CrystallizeReward reward : _item.getCrystallizeRewards())
			{
				writeD(reward.getItemId());
				writeQ(reward.getCount());
				writeF(reward.getChance());
			}
		}
		else
		{
			writeD(1);
			writeD(_item.getCrystalItemId());
			writeQ(_crystalCount);
			writeF(100.0);
		}
	}

	@Override
	public String getType()
	{
		return _S__FE_E0_EXGETCRYSTALIZINGESTIMATION;
	}
	
}
