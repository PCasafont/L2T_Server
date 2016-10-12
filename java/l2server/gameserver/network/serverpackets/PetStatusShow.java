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

import l2server.gameserver.model.actor.L2Summon;

/**
 * This class ...
 *
 * @author Yme
 * @version $Revision: 1.3.2.2.2.4 $ $Date: 2005/03/29 23:15:10 $
 */
public class PetStatusShow extends L2GameServerPacket
{
	private int _summonType;
	private int _summonId;

	public PetStatusShow(L2Summon summon)
	{
		_summonType = summon.getSummonType();
		_summonId = summon.getObjectId();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_summonType);
		writeD(_summonId);
	}
}
