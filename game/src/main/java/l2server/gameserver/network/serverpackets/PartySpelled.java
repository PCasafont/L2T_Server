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

import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.PetInstance;
import l2server.gameserver.model.actor.instance.SummonInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * This class ...
 *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PartySpelled extends L2GameServerPacket {
	private List<Effect> effects;
	private Creature activeChar;
	
	private static class Effect {
		protected int skillId;
		protected int dat;
		protected int duration;
		
		public Effect(int pSkillId, int pDat, int pDuration) {
			skillId = pSkillId;
			dat = pDat;
			duration = pDuration;
		}
	}
	
	public PartySpelled(Creature cha) {
		effects = new ArrayList<>();
		activeChar = cha;
	}
	
	@Override
	protected final void writeImpl() {
		writeD(activeChar instanceof SummonInstance ? 2 : activeChar instanceof PetInstance ? 1 : 0);
		writeD(activeChar.getObjectId());
		writeD(effects.size());
		for (Effect temp : effects) {
			writeD(temp.skillId);
			writeD(temp.dat);
			writeD(0x00);
			writeH(temp.duration / 1000 + 1);
		}
	}
	
	public void addPartySpelledEffect(int skillId, int dat, int duration) {
		effects.add(new Effect(skillId, dat, duration));
	}
}
