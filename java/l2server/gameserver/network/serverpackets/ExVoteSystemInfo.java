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

import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.RecoBonus;

/**
 * *	@author Gnacik
 * *
 */
public class ExVoteSystemInfo extends L2GameServerPacket
{
	private int _recomLeft;
	private int _recomHave;
	private int _bonusTime;
	private int _bonusVal;
	private int _bonusType;

	public ExVoteSystemInfo(L2PcInstance player)
	{
		_recomLeft = player.getRecomLeft();
		_recomHave = player.getRecomHave();
		_bonusTime = player.getRecomBonusTime();
		_bonusVal = RecoBonus.getRecoBonus(player);
		_bonusType = player.getRecomBonusType();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_recomLeft);
		writeD(_recomHave);
		writeD(_bonusTime);
		writeD(_bonusVal);
		writeD(_bonusType);
	}
}
