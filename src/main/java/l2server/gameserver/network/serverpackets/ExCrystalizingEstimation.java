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
import l2server.gameserver.templates.item.ItemTemplate;

/**
 * @author Erlandys
 */
public class ExCrystalizingEstimation extends L2GameServerPacket {
	
	private ItemTemplate item;
	private long crystalCount;
	
	public ExCrystalizingEstimation(ItemTemplate item, long crystalCount) {
		this.item = item;
		this.crystalCount = crystalCount;
	}
	
	@Override
	protected final void writeImpl() {
		if (Config.ENABLE_CRYSTALLIZE_REWARDS && item.getCrystallizeRewards() != null) {
			writeD(item.getCrystallizeRewards().length + 1);
			writeD(item.getCrystalItemId());
			writeQ(crystalCount);
			writeF(100.0);
			for (L2CrystallizeReward reward : item.getCrystallizeRewards()) {
				writeD(reward.getItemId());
				writeQ(reward.getCount());
				writeF(reward.getChance());
			}
		} else {
			writeD(1);
			writeD(item.getCrystalItemId());
			writeQ(crystalCount);
			writeF(100.0);
		}
	}
}
