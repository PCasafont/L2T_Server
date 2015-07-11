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

import java.util.Map;

import l2tserver.gameserver.model.olympiad.HeroInfo;
import l2tserver.gameserver.model.olympiad.HeroesManager;

/**
 * Format: (ch) d [SdSdSdd]
 * d: size
 * [
 * S: hero name
 * d: hero class ID
 * S: hero clan name
 * d: hero clan crest id
 * S: hero ally name
 * d: hero Ally id
 * d: count
 * ]
 * @author -Wooden-
 * Format from KenM
 *
 * Re-written by godson
 *
 */
public class ExHeroList extends L2GameServerPacket
{
	private static final String TYPE = "[S] FE:79 ExHeroList";
	private Map<Integer, HeroInfo> _heroList;
	
	public ExHeroList()
	{
		_heroList = HeroesManager.getInstance().getHeroes();
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x7a);
		writeD(_heroList.size());
		
		for (HeroInfo hero : _heroList.values())
		{
			writeS(hero.getName());
			writeD(hero.getClassId());
			writeS(hero.getClanName());
			writeD(hero.getClanCrest());
			writeS(hero.getAllyName());
			writeD(hero.getAllyCrest());
			writeD(hero.getCount());
		}
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return TYPE;
	}
	
}