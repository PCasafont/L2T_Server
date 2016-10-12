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

import l2server.gameserver.model.L2Manor;

import java.util.ArrayList;

/**
 * format(packet 0xFE) ch cd [ddddcdcd] c - id h - sub id
 * <p>
 * c d - size
 * [ d - level d - seed price d - seed level d - crop price c d - reward 1 id c
 * d - reward 2 id ]
 *
 * @author l3x
 */
public class ExShowManorDefaultInfo extends L2GameServerPacket
{

	private ArrayList<Integer> _crops = null;

	public ExShowManorDefaultInfo()
	{
		_crops = L2Manor.getInstance().getAllCrops();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0);
		writeD(_crops.size());
		for (int cropId : _crops)
		{
			writeD(cropId); // crop Id
			writeD(L2Manor.getInstance().getSeedLevelByCrop(cropId)); // level
			writeD(L2Manor.getInstance().getSeedBasicPriceByCrop(cropId)); // seed
			// price
			writeD(L2Manor.getInstance().getCropBasicPrice(cropId)); // crop
			// price
			writeC(1); // rewrad 1 Type
			writeD(L2Manor.getInstance().getRewardItem(cropId, 1)); // Rewrad 1
			// Type Item
			// Id
			writeC(1); // rewrad 2 Type
			writeD(L2Manor.getInstance().getRewardItem(cropId, 2)); // Rewrad 2
			// Type Item
			// Id
		}
	}
}
