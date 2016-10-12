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

import java.util.ArrayList;
import java.util.List;

/**
 * MagicEffectIcons
 * format   h (dhd)
 *
 * @version $Revision: 1.3.2.1.2.6 $ $Date: 2005/04/05 19:41:08 $
 */
public class AbnormalStatusUpdate extends L2GameServerPacket
{
	private List<Effect> _effects;

	private static class Effect
	{
		protected int _skillId;
		protected int _level;
		protected int _comboId;
		protected int _duration;

		public Effect(int pSkillId, int pLevel, int pComboId, int pDuration)
		{
			_skillId = pSkillId;
			_level = pLevel;
			_comboId = pComboId;
			_duration = pDuration;
		}
	}

	public AbnormalStatusUpdate()
	{
		_effects = new ArrayList<>();
	}

	public void addEffect(int skillId, int level, int comboId, int duration)
	{
		if (skillId == 2031 || skillId == 2032 || skillId == 2037 || skillId == 26025 || skillId == 26026)
		{
			return;
		}
		_effects.add(new Effect(skillId, level, comboId, duration));
	}

	@Override
	protected final void writeImpl()
	{
		writeH(_effects.size());

		for (Effect temp : _effects)
		{
			writeD(temp._skillId);
			writeD(temp._level);

			writeD(temp._comboId);

			if (temp._duration == -1)
			{
				writeH(-1);
			}
			else
			{
				writeH(temp._duration / 1000 + 1);
			}
		}
	}
}
