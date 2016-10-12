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

import l2server.gameserver.model.L2Party;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 *
 * @version $Revision: 1.4.2.1.2.5 $ $Date: 2005/03/27 15:29:57 $
 */
public final class PartySmallWindowAdd extends L2GameServerPacket
{

	private final L2PcInstance _member;
	private final int _leaderId;
	private final int _distribution;

	public PartySmallWindowAdd(L2PcInstance member, L2Party party)
	{
		_member = member;
		_leaderId = party.getPartyLeaderOID();
		_distribution = party.getLootDistribution();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_leaderId); // c3
		writeD(_distribution);//writeD(0x04); ?? //c3
		writeD(_member.getObjectId());
		writeS(_member.getName());
		writeD((int) _member.getCurrentCp()); //c4
		writeD(_member.getMaxCp()); //c4
		writeD((int) _member.getCurrentHp());
		writeD(_member.getMaxVisibleHp());
		writeD((int) _member.getCurrentMp());
		writeD(_member.getMaxMp());
		writeD(_member.getVitalityPoints());
		writeC(_member.getLevel());
		writeH(_member.getCurrentClass().getId());
		/*writeD(_member.getVitalityPoints());
        writeC(0x01); // ???
		writeC(_member.getRace().ordinal());
		writeC(PartySearchManager.getInstance().getWannaToChangeThisPlayer(_member.getObjectId()) ? 0x01 : 0x00); // GoD unknown
		writeD(_member.getSummons().size() + (_member.getPet() != null ? 1 : 0));
		for (L2SummonInstance summon : _member.getSummons())
		{
			writeD(summon.getObjectId());
			writeD(summon.getNpcId() + 1000000);
			writeC(summon.getSummonType());
			writeS(summon.getName());
			writeD((int)summon.getCurrentHp());
			writeD(summon.getMaxHp());
			writeD((int)summon.getCurrentMp());
			writeD(summon.getMaxMp());
			writeC(summon.getLevel());
		}
		if (_member.getPet() != null)
		{
			writeD(_member.getPet().getObjectId());
			writeD(_member.getPet().getNpcId() + 1000000);
			writeC(_member.getPet().getSummonType());
			writeS(_member.getPet().getName());
			writeD((int)_member.getPet().getCurrentHp());
			writeD(_member.getPet().getMaxHp());
			writeD((int)_member.getPet().getCurrentMp());
			writeD(_member.getPet().getMaxMp());
			writeC(_member.getPet().getLevel());
		}*/
	}
}
