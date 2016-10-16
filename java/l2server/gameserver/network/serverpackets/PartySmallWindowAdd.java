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
	private final L2PcInstance member;
	private final int leaderId;
	private final int distribution;

	public PartySmallWindowAdd(L2PcInstance member, L2Party party)
	{
		this.member = member;
		leaderId = party.getPartyLeaderOID();
		distribution = party.getLootDistribution();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(leaderId); // c3
		writeD(distribution);//writeD(0x04); ?? //c3
		writeD(member.getObjectId());
		writeS(member.getName());
		writeD((int) member.getCurrentCp()); //c4
		writeD(member.getMaxCp()); //c4
		writeD((int) member.getCurrentHp());
		writeD(member.getMaxVisibleHp());
		writeD((int) member.getCurrentMp());
		writeD(member.getMaxMp());
		writeD(member.getVitalityPoints());
		writeC(member.getLevel());
		writeH(member.getCurrentClass().getId());
		/*writeD(this.member.getVitalityPoints());
		writeC(0x01); // ???
		writeC(this.member.getRace().ordinal());
		writeC(PartySearchManager.getInstance().getWannaToChangeThisPlayer(this.member.getObjectId()) ? 0x01 : 0x00); // GoD unknown
		writeD(this.member.getSummons().size() + (this.member.getPet() != null ? 1 : 0));
		for (L2SummonInstance summon : this.member.getSummons())
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
		if (this.member.getPet() != null)
		{
			writeD(this.member.getPet().getObjectId());
			writeD(this.member.getPet().getNpcId() + 1000000);
			writeC(this.member.getPet().getSummonType());
			writeS(this.member.getPet().getName());
			writeD((int)_member.getPet().getCurrentHp());
			writeD(this.member.getPet().getMaxHp());
			writeD((int)_member.getPet().getCurrentMp());
			writeD(this.member.getPet().getMaxMp());
			writeC(this.member.getPet().getLevel());
		}*/
	}
}
