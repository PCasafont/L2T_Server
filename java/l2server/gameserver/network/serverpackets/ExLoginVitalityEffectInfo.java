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

/**
 * @author Erlando
 */
public class ExLoginVitalityEffectInfo extends L2GameServerPacket
{
	private float expBonus;
	private int vitalityItemsUsed;

	public ExLoginVitalityEffectInfo(int vitPoints, int vitalityItemsUsed)
	{
		if (vitPoints > 0)
		{
			expBonus = Config.VITALITY_MULTIPLIER * 100;
		}
		else
		{
			expBonus = 0;
		}

		this.vitalityItemsUsed = vitalityItemsUsed;
	}

	@Override
	protected final void writeImpl()
	{
		writeD((int) expBonus);
		writeD(vitalityItemsUsed);
	}
}
