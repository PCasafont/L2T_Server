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

import java.util.ArrayList;
import java.util.List;

/**
 * This class ...
 *
 * @author godson
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class ExOlympiadSpelledInfo extends L2GameServerPacket
{
	// chdd(dhd)
	private int playerID;
	private List<Effect> effects;

	private static class Effect
	{
		protected int skillId;
		protected int level;
		protected int duration;

		public Effect(int pSkillId, int pLevel, int pDuration)
		{
			skillId = pSkillId;
			level = pLevel;
			duration = pDuration;
		}
	}

	public ExOlympiadSpelledInfo(L2PcInstance player)
	{
		effects = new ArrayList<>();
		playerID = player.getObjectId();
	}

	public void addEffect(int skillId, int level, int duration)
	{
		effects.add(new Effect(skillId, level, duration));
	}

	@Override
	protected final void writeImpl()
	{
		writeD(playerID);
		writeD(effects.size());
		for (Effect temp : effects)
		{
			writeD(temp.skillId);
			writeD(temp.level);
			writeD(0x00); // ???
			writeH(temp.duration / 1000 + 1);
		}
	}
}
