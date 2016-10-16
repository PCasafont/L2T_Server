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
import l2server.gameserver.instancemanager.CastleManorManager.SeedProduction;
import l2server.gameserver.model.L2Manor;
import l2server.gameserver.model.entity.Castle;

import java.util.ArrayList;

/**
 * format(packet 0xFE) ch dd [ddcdcdddddddd] c - id h - sub id
 * <p>
 * d - manor id d - size
 * [ d - seed id d - level c d - reward 1 id c d - reward 2 id d - next sale
 * limit d - price for castle to produce 1 d - min seed price d - max seed price
 * d - today sales d - today price d - next sales d - next price ]
 *
 * @author l3x
 */
public class ExShowSeedSetting extends L2GameServerPacket
{

	private int manorId;

	private int count;

	private long[] seedData; // data to send, size:_count*12

	@Override
	public void runImpl()
	{
	}

	public ExShowSeedSetting(int manorId)
	{
		this.manorId = manorId;
		Castle c = CastleManager.getInstance().getCastleById(this.manorId);
		ArrayList<Integer> seeds = L2Manor.getInstance().getSeedsForCastle(this.manorId);
		this.count = seeds.size();
		this.seedData = new long[this.count * 12];
		int i = 0;
		for (int s : seeds)
		{
			this.seedData[i * 12] = s;
			this.seedData[i * 12 + 1] = L2Manor.getInstance().getSeedLevel(s);
			this.seedData[i * 12 + 2] = L2Manor.getInstance().getRewardItemBySeed(s, 1);
			this.seedData[i * 12 + 3] = L2Manor.getInstance().getRewardItemBySeed(s, 2);
			this.seedData[i * 12 + 4] = L2Manor.getInstance().getSeedSaleLimit(s);
			this.seedData[i * 12 + 5] = L2Manor.getInstance().getSeedBuyPrice(s);
			this.seedData[i * 12 + 6] = L2Manor.getInstance().getSeedBasicPrice(s) * 60 / 100;
			this.seedData[i * 12 + 7] = L2Manor.getInstance().getSeedBasicPrice(s) * 10;
			SeedProduction seedPr = c.getSeed(s, CastleManorManager.PERIOD_CURRENT);
			if (seedPr != null)
			{
				this.seedData[i * 12 + 8] = seedPr.getStartProduce();
				this.seedData[i * 12 + 9] = seedPr.getPrice();
			}
			else
			{
				this.seedData[i * 12 + 8] = 0;
				this.seedData[i * 12 + 9] = 0;
			}
			seedPr = c.getSeed(s, CastleManorManager.PERIOD_NEXT);
			if (seedPr != null)
			{
				this.seedData[i * 12 + 10] = seedPr.getStartProduce();
				this.seedData[i * 12 + 11] = seedPr.getPrice();
			}
			else
			{
				this.seedData[i * 12 + 10] = 0;
				this.seedData[i * 12 + 11] = 0;
			}
			i++;
		}
	}

	@Override
	public void writeImpl()
	{
		writeD(this.manorId); // manor id
		writeD(this.count); // size

		for (int i = 0; i < this.count; i++)
		{
			writeD((int) this.seedData[i * 12]); // seed id
			writeD((int) this.seedData[i * 12 + 1]); // level
			writeC(1);
			writeD((int) this.seedData[i * 12 + 2]); // reward 1 id
			writeC(1);
			writeD((int) this.seedData[i * 12 + 3]); // reward 2 id

			writeD((int) this.seedData[i * 12 + 4]); // next sale limit
			writeD((int) this.seedData[i * 12 + 5]); // price for castle to produce 1
			writeD((int) this.seedData[i * 12 + 6]); // min seed price
			writeD((int) this.seedData[i * 12 + 7]); // max seed price

			writeQ(this.seedData[i * 12 + 8]); // today sales
			writeQ(this.seedData[i * 12 + 9]); // today price
			writeQ(this.seedData[i * 12 + 10]); // next sales
			writeQ(this.seedData[i * 12 + 11]); // next price
		}
	}
}
