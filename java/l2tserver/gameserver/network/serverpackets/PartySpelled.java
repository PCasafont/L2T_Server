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
package l2tserver.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import l2tserver.gameserver.model.actor.L2Character;
import l2tserver.gameserver.model.actor.instance.L2PetInstance;
import l2tserver.gameserver.model.actor.instance.L2SummonInstance;

/**
 * This class ...
 *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PartySpelled extends L2GameServerPacket
{
	private static final String _S__EE_PartySpelled = "[S] f4 PartySpelled";
	private List<Effect> _effects;
	private L2Character _activeChar;
	
	private static class Effect
	{
		protected int _skillId;
		protected int _dat;
		protected int _duration;
		
		public Effect(int pSkillId, int pDat, int pDuration)
		{
			_skillId = pSkillId;
			_dat = pDat;
			_duration = pDuration;
		}
	}
	
	public PartySpelled(L2Character cha)
	{
		_effects = new ArrayList<Effect>();
		_activeChar = cha;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xf4);
		writeD(_activeChar instanceof L2SummonInstance ? 2 : _activeChar instanceof L2PetInstance ? 1 : 0);
		writeD(_activeChar.getObjectId());
		writeD(_effects.size());
		for (Effect temp : _effects)
		{
			writeD(temp._skillId);
			writeD(temp._dat);
			writeD(0x00);
			writeH(temp._duration / 1000);
		}
	}
	
	public void addPartySpelledEffect(int skillId, int dat, int duration)
	{
		_effects.add(new Effect(skillId, dat, duration));
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__EE_PartySpelled;
	}
}
