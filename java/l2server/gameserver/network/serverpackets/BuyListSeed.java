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

import l2server.gameserver.instancemanager.CastleManorManager.SeedProduction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Format: c ddh[hdddhhd]
 * c - id (0xE8)
 * <p>
 * d - money
 * d - manor id
 * h - size
 * [
 * h - item type 1
 * d - object id
 * d - item id
 * d - count
 * h - item type 2
 * h
 * d - price
 * ]
 *
 * @author l3x
 */

public final class BuyListSeed extends L2GameServerPacket
{

	private int manorId;
	private List<Seed> list = null;
	private long money;

	public BuyListSeed(long currentMoney, int castleId, List<SeedProduction> seeds)
	{
		this.money = currentMoney;
		this.manorId = castleId;

		if (seeds != null && seeds.size() > 0)
		{
			this.list = new ArrayList<>();
			this.list.addAll(seeds.stream().filter(s -> s.getCanProduce() > 0 && s.getPrice() > 0)
					.map(s -> new Seed(s.getId(), s.getCanProduce(), s.getPrice())).collect(Collectors.toList()));
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeQ(this.money); // current money
		writeD(this.manorId); // manor id

		if (this.list != null && this.list.size() > 0)
		{
			writeH(this.list.size()); // list length
			for (Seed s : this.list)
			{
				writeD(s.itemId);
				writeD(s.itemId);
				writeD(0x00);
				writeQ(s.count); // item count
				writeH(0x05); // Custom Type 2
				writeH(0x00); // Custom Type 1
				writeH(0x00); // Equipped
				writeD(0x00); // Body Part
				writeH(0x00); // Enchant
				writeH(0x00); // Custom Type
				writeD(0x00); // Augment
				writeD(-1); // Mana
				writeD(-9999); // Time
				writeH(0x00); // Element Type
				writeH(0x00); // Element Power
				for (byte i = 0; i < 6; i++)
				{
					writeH(0x00);
				}
				// Enchant Effects
				writeH(0x00);
				writeH(0x00);
				writeH(0x00);
				writeQ(s.price); // price
			}
			this.list.clear();
		}
		else
		{
			writeH(0x00);
		}
	}

	private static class Seed
	{
		public final int itemId;
		public final long count;
		public final long price;

		public Seed(int itemId, long count, long price)
		{
			this.itemId = itemId;
			this.count = count;
			this.price = price;
		}
	}
}
