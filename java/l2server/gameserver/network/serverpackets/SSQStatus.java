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

import l2server.gameserver.network.SystemMessageId;

/**
 * Seven Signs Record Update
 * <p>
 * packet type id 0xf5
 * format:
 * <p>
 * c cc	(Page Num = 1 -> 4, period)
 * <p>
 * 1: [ddd cc dd ddd c ddd c]
 * 2: [hc [cd (dc (S))]
 * 3: [ccc (cccc)]
 * 4: [(cchh)]
 *
 * @author Tempy
 *         CT 2.3 [dddccQQQQQcQQQc.cdQc....Qc..ccc.cccc....ccd]
 * @editor shansoft
 */
public class SSQStatus extends L2GameServerPacket
{
	private int _page;

	public SSQStatus(int objectId, int recordPage)
	{
		_page = recordPage;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_page);
		writeC(0); // current period?

		switch (_page)
		{
			case 1:
				// [ddd cc dd ddd c ddd c]

				writeD(0);
				writeD(SystemMessageId.INITIAL_PERIOD.getId());
				writeD(SystemMessageId.UNTIL_TODAY_6PM.getId());

				writeC(0);
				writeC(0);

				writeQ(0); // Seal Stones Turned-In
				writeQ(0); // Ancient Adena to Collect

				/* DUSK */
				writeQ(0); // Seal Stone Score
				writeQ(0); // Festival Score
				writeQ(0); // Total Score

				writeC(0); // Dusk %

				/* DAWN */
				writeQ(0); // Seal Stone Score
				writeQ(0); // Festival Score
				writeQ(0); // Total Score

				writeC(0); // Dawn %
				break;
			case 2:
				// c cc hc [cd (dc (S))]
				writeH(1);

				writeC(5); // Total number of festivals

				for (int i = 0; i < 5; i++)
				{
					writeC(i + 1); // Current client-side festival ID
					writeD(0);

					// Dusk Score \\
					writeQ(0);

					writeC(0);

					// Dawn Score \\
					writeQ(0);

					writeC(0);
				}
				break;
			case 3:
				// c cc [ccc (cccc)]
				writeC(10); // Minimum limit for winning cabal to retain their seal
				writeC(35); // Minimum limit for winning cabal to claim a seal
				writeC(3); // Total number of seals

				for (int i = 1; i < 4; i++)
				{
					writeC(i);
					writeC(0);

					writeC(0);
					writeC(0);
				}
				break;
			case 4:
				// c cc [cc (ccD)] CT 2.3 update
				writeC(0); // Overall predicted winner
				writeC(3); // Total number of seals

				for (int i = 1; i < 4; i++)
				{
					writeC(i);

					writeC(0);
					writeD(SystemMessageId.COMPETITION_TIE_SEAL_NOT_AWARDED.getId());
				}
				break;
		}
	}
}
