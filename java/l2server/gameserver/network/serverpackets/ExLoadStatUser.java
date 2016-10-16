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

/**
 * @author Xavi
 */
public class ExLoadStatUser extends L2GameServerPacket
{

	L2PcInstance player;

	public ExLoadStatUser(L2PcInstance player)
	{
		this.player = player;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		if (this.player == null)
		{
		}

		/*writeD(MuseumStatistic.values().length);

		for (MuseumStatistic statistic : MuseumStatistic.values())
		{
			boolean isClanStatistic = statistic.toString().toLowerCase().contains("clan");
			int identity = (isClanStatistic && this.player.getClan() != null) ? this.player.getClanId() : this.player.getObjectId();
			long score1 = MuseumManager.getInstance().getStatistic(identity, statistic, true);
			long score2 = MuseumManager.getInstance().getStatistic(identity, statistic, false);

			writeD(statistic.getPId1());
			writeD(statistic.getPId2());
			writeQ(score1);
			writeQ(score2);
		}*/
	}
}
