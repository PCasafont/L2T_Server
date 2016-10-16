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

import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * This class ...
 *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PartySpelled extends L2GameServerPacket
{
	private List<Effect> effects;
	private L2Character activeChar;

	private static class Effect
	{
		protected int skillId;
		protected int dat;
		protected int duration;

		public Effect(int pSkillId, int pDat, int pDuration)
		{
			this.skillId = pSkillId;
			this.dat = pDat;
			this.duration = pDuration;
		}
	}

	public PartySpelled(L2Character cha)
	{
		this.effects = new ArrayList<>();
		this.activeChar = cha;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(this.activeChar instanceof L2SummonInstance ? 2 : this.activeChar instanceof L2PetInstance ? 1 : 0);
		writeD(this.activeChar.getObjectId());
		writeD(this.effects.size());
		for (Effect temp : this.effects)
		{
			writeD(temp.skillId);
			writeD(temp.dat);
			writeD(0x00);
			writeH(temp.duration / 1000 + 1);
		}
	}

	public void addPartySpelledEffect(int skillId, int dat, int duration)
	{
		this.effects.add(new Effect(skillId, dat, duration));
	}
}
