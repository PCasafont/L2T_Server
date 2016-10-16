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

import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.instancemanager.CastleManorManager;
import l2server.gameserver.instancemanager.CastleManorManager.CropProcure;
import l2server.gameserver.model.entity.Castle;

import java.util.HashMap;

/**
 * format(packet 0xFE) ch dd [dddc] c - id h - sub id
 * <p>
 * d - crop id d - size
 * [ d - manor name d - buy residual d - buy price c - reward type ]
 *
 * @author l3x
 */
public class ExShowProcureCropDetail extends L2GameServerPacket
{
	private int cropId;

	private HashMap<Integer, CropProcure> castleCrops;

	public ExShowProcureCropDetail(int cropId)
	{
		this.cropId = cropId;
		castleCrops = new HashMap<>();

		for (Castle c : CastleManager.getInstance().getCastles())
		{
			CropProcure cropItem = c.getCrop(this.cropId, CastleManorManager.PERIOD_CURRENT);
			if (cropItem != null && cropItem.getAmount() > 0)
			{
				castleCrops.put(c.getCastleId(), cropItem);
			}
		}
	}

	@Override
	public void runImpl()
	{
	}

	@Override
	protected final void writeImpl()
	{
		writeD(cropId); // crop id
		writeD(castleCrops.size()); // size

		for (int manorId : castleCrops.keySet())
		{
			CropProcure crop = castleCrops.get(manorId);
			writeD(manorId); // manor name
			writeQ(crop.getAmount()); // buy residual
			writeQ(crop.getPrice()); // buy price
			writeC(crop.getReward()); // reward type
		}
	}
}
