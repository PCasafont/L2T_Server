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
 * @author KenM
 */
public final class ExPartyPetWindowAdd extends L2GameServerPacket
{
	private final L2Summon _summon;

	public ExPartyPetWindowAdd(L2Summon summon)
	{
		_summon = summon;
	}

    /*
	  @see l2server.gameserver.network.serverpackets.L2GameServerPacket#getType()
     */

	/**
	 * @see l2server.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(_summon.getObjectId());
		writeD(_summon.getTemplate().TemplateId + 1000000);
		writeD(_summon.getSummonType());
		writeD(_summon.getOwner().getObjectId());
		writeS(_summon.getName());
		writeD((int) _summon.getCurrentHp());
		writeD(_summon.getMaxVisibleHp());
		writeD((int) _summon.getCurrentMp());
		writeD(_summon.getMaxMp());
		writeD(_summon.getLevel());
	}
}
