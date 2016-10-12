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

import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.actor.L2Character;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class AbnormalStatusUpdateFromTarget extends L2GameServerPacket
{
	private L2Character _character;
	private List<Effect> _effects;

	private static class Effect
	{
		protected int _skillId;
		protected int _level;
		protected int _comboId;
		protected int _duration;
		protected int _effector;

		public Effect(int pSkillId, int pLevel, int pComboId, int pDuration, int pEffector)
		{
			_skillId = pSkillId;
			_level = pLevel;
			_comboId = pComboId;
			_duration = pDuration;
			_effector = pEffector;
		}
	}

	public AbnormalStatusUpdateFromTarget(L2Character c)
	{
		_character = c;
		_effects = new ArrayList<>();

		for (L2Abnormal e : c.getAllEffects())
		{
			if (e == null || !e.getShowIcon())
			{
				continue;
			}

			switch (e.getType())
			{
				case CHARGE: // handled by EtcStatusUpdate
				case SIGNET_GROUND:
					continue;
			}

			if (e.getInUse())
			{
				e.addIcon(this);
			}
		}
	}

	public void addEffect(int skillId, int level, int comboId, int duration, int effector)
	{
		if (skillId == 2031 || skillId == 2032 || skillId == 2037 || skillId == 26025 || skillId == 26026)
		{
			return;
		}

		_effects.add(new Effect(skillId, level, comboId, duration, effector));
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_character.getObjectId());

		writeH(_effects.size());

		for (Effect temp : _effects)
		{
			writeD(temp._skillId);
			writeD(temp._level);
			writeH(temp._comboId);
			if (temp._duration == -1)
			{
				writeH(-1);
			}
			else
			{
				writeH(temp._duration / 1000 + 1);
			}

			writeD(temp._effector);
		}
	}
}
