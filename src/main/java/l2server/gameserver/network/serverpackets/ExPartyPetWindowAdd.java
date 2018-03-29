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
	private final L2Summon summon;

	public ExPartyPetWindowAdd(L2Summon summon)
	{
		this.summon = summon;
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
		writeD(summon.getObjectId());
		writeD(summon.getTemplate().TemplateId + 1000000);
		writeD(summon.getSummonType());
		writeD(summon.getOwner().getObjectId());
		writeS(summon.getName());
		writeD((int) summon.getCurrentHp());
		writeD(summon.getMaxVisibleHp());
		writeD((int) summon.getCurrentMp());
		writeD(summon.getMaxMp());
		writeD(summon.getLevel());
	}
}
