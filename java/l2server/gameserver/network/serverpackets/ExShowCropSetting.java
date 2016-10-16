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
import l2server.gameserver.model.L2Manor;
import l2server.gameserver.model.entity.Castle;

import java.util.ArrayList;

/**
 * format(packet 0xFE) ch dd [ddcdcdddddddcddc] c - id h - sub id
 * <p>
 * d - manor id d - size
 * [ d - crop id d - seed level c d - reward 1 id c d - reward 2 id d - next
 * sale limit d d - min crop price d - max crop price d - today buy d - today
 * price c - today reward d - next buy d - next price c - next reward ]
 *
 * @author l3x
 */
public class ExShowCropSetting extends L2GameServerPacket
{

	private int manorId;

	private int count;

	private long[] cropData; // data to send, size:_count*14

	@Override
	public void runImpl()
	{
	}

	public ExShowCropSetting(int manorId)
	{
		this.manorId = manorId;
		Castle c = CastleManager.getInstance().getCastleById(this.manorId);
		ArrayList<Integer> crops = L2Manor.getInstance().getCropsForCastle(this.manorId);
		count = crops.size();
		cropData = new long[count * 14];
		int i = 0;
		for (int cr : crops)
		{
			cropData[i * 14] = cr;
			cropData[i * 14 + 1] = L2Manor.getInstance().getSeedLevelByCrop(cr);
			cropData[i * 14 + 2] = L2Manor.getInstance().getRewardItem(cr, 1);
			cropData[i * 14 + 3] = L2Manor.getInstance().getRewardItem(cr, 2);
			cropData[i * 14 + 4] = L2Manor.getInstance().getCropPuchaseLimit(cr);
			cropData[i * 14 + 5] = 0; // Looks like not used
			cropData[i * 14 + 6] = L2Manor.getInstance().getCropBasicPrice(cr) * 60 / 100;
			cropData[i * 14 + 7] = L2Manor.getInstance().getCropBasicPrice(cr) * 10;
			CropProcure cropPr = c.getCrop(cr, CastleManorManager.PERIOD_CURRENT);
			if (cropPr != null)
			{
				cropData[i * 14 + 8] = cropPr.getStartAmount();
				cropData[i * 14 + 9] = cropPr.getPrice();
				cropData[i * 14 + 10] = cropPr.getReward();
			}
			else
			{
				cropData[i * 14 + 8] = 0;
				cropData[i * 14 + 9] = 0;
				cropData[i * 14 + 10] = 0;
			}
			cropPr = c.getCrop(cr, CastleManorManager.PERIOD_NEXT);
			if (cropPr != null)
			{
				cropData[i * 14 + 11] = cropPr.getStartAmount();
				cropData[i * 14 + 12] = cropPr.getPrice();
				cropData[i * 14 + 13] = cropPr.getReward();
			}
			else
			{
				cropData[i * 14 + 11] = 0;
				cropData[i * 14 + 12] = 0;
				cropData[i * 14 + 13] = 0;
			}
			i++;
		}
	}

	@Override
	public void writeImpl()
	{
		writeD(manorId); // manor id
		writeD(count); // size

		for (int i = 0; i < count; i++)
		{
			writeD((int) cropData[i * 14]); // crop id
			writeD((int) cropData[i * 14 + 1]); // seed level
			writeC(1);
			writeD((int) cropData[i * 14 + 2]); // reward 1 id
			writeC(1);
			writeD((int) cropData[i * 14 + 3]); // reward 2 id

			writeD((int) cropData[i * 14 + 4]); // next sale limit
			writeD((int) cropData[i * 14 + 5]); // ???
			writeD((int) cropData[i * 14 + 6]); // min crop price
			writeD((int) cropData[i * 14 + 7]); // max crop price

			writeQ(cropData[i * 14 + 8]); // today buy
			writeQ(cropData[i * 14 + 9]); // today price
			writeC((int) cropData[i * 14 + 10]); // today reward

			writeQ(cropData[i * 14 + 11]); // next buy
			writeQ(cropData[i * 14 + 12]); // next price
			writeC((int) cropData[i * 14 + 13]); // next reward
		}
	}
}
