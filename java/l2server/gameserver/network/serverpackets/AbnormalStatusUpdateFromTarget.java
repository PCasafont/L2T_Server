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
	private L2Character character;
	private List<Effect> effects;

	private static class Effect
	{
		protected int skillId;
		protected int level;
		protected int comboId;
		protected int duration;
		protected int effector;

		public Effect(int pSkillId, int pLevel, int pComboId, int pDuration, int pEffector)
		{
			skillId = pSkillId;
			level = pLevel;
			comboId = pComboId;
			duration = pDuration;
			effector = pEffector;
		}
	}

	public AbnormalStatusUpdateFromTarget(L2Character c)
	{
		character = c;
		effects = new ArrayList<>();

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

			if (e.isInUse())
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

		effects.add(new Effect(skillId, level, comboId, duration, effector));
	}

	@Override
	protected final void writeImpl()
	{
		writeD(character.getObjectId());

		writeH(effects.size());

		for (Effect temp : effects)
		{
			writeD(temp.skillId);
			writeD(temp.level);
			writeH(temp.comboId);
			if (temp.duration == -1)
			{
				writeH(-1);
			}
			else
			{
				writeH(temp.duration / 1000 + 1);
			}

			writeD(temp.effector);
		}
	}
}
