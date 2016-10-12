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

package l2server.gameserver.model.entity;

import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Gnacik
 */
public final class RecoBonus
{
	private static final int[][] _recoBonus = {
			{25, 50, 50, 50, 50, 50, 50, 50, 50, 50},
			{16, 33, 50, 50, 50, 50, 50, 50, 50, 50},
			{12, 25, 37, 50, 50, 50, 50, 50, 50, 50},
			{10, 20, 30, 40, 50, 50, 50, 50, 50, 50},
			{8, 16, 25, 33, 41, 50, 50, 50, 50, 50},
			{7, 14, 21, 28, 35, 42, 50, 50, 50, 50},
			{6, 12, 18, 25, 31, 37, 43, 50, 50, 50},
			{5, 11, 16, 22, 27, 33, 38, 44, 50, 50},
			{5, 10, 15, 20, 25, 30, 35, 40, 45, 50},
			{5, 10, 15, 20, 25, 30, 35, 40, 45, 50}
	};

	public static int getRecoBonus(L2PcInstance activeChar)
	{
		if (activeChar != null && activeChar.isOnline())
		{
			if (activeChar.getRecomHave() == 0)
			{
				return 0;
			}

			int lvl = (int) Math.min(Math.ceil(activeChar.getLevel() / 10), 9);
			int exp = (int) Math.ceil((Math.min(100, activeChar.getRecomHave()) - 1) / 10);

			return _recoBonus[lvl][exp];
		}
		return 0;
	}

	public static double getRecoMultiplier(L2PcInstance activeChar)
	{
		double _multiplier = 1;

		int bonus = getRecoBonus(activeChar);
		if (bonus > 0)
		{
			_multiplier = 1 + bonus / 100;
		}

		if (_multiplier < 1)
		{
			_multiplier = 1;
		}

		return _multiplier;
	}
}
