/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.instancemanager.PartySearchManager;
import l2server.gameserver.model.L2Party;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;

/**
 * sample 63 01 00 00 00 count
 * <p>
 * c1 b2 e0 4a object id 54 00 75 00 65 00 73 00 64 00 61 00 79 00 00 00 name 5a 01 00 00 hp 5a 01
 * 00 00 hp max 89 00 00 00 mp 89 00 00 00 mp max 0e 00 00 00 level 12 00 00 00 class 00 00 00 00 01
 * 00 00 00
 * <p>
 * <p>
 * format d (dSdddddddd)
 *
 * @version $Revision: 1.6.2.1.2.5 $ $Date: 2005/03/27 15:29:57 $
 */
public final class PartySmallWindowAll extends L2GameServerPacket
{
	private L2Party _party;
	private L2PcInstance _exclude;
	private int _dist, _LeaderOID;

	public PartySmallWindowAll(L2PcInstance exclude, L2Party party)
	{
		_exclude = exclude;
		_party = party;
		_LeaderOID = _party.getPartyLeaderOID();
		_dist = _party.getLootDistribution();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_LeaderOID);
		writeC(_dist);
		writeC(_party.getMemberCount() - 1);

		for (L2PcInstance member : _party.getPartyMembers())
		{
			if (member != null && member != _exclude)
			{
				writeD(member.getObjectId());
				writeS(member.getName());

				writeD((int) member.getCurrentCp()); // c4
				writeD(member.getMaxCp()); // c4

				writeD((int) member.getCurrentHp());
				writeD(member.getMaxVisibleHp());
				writeD((int) member.getCurrentMp());
				writeD(member.getMaxMp());
				writeD(member.getVitalityPoints());
				writeC(member.getLevel());
				writeH(member.getCurrentClass().getId());
				writeC(0x01); // ???
				writeC(member.getRace().ordinal());
				writeC(PartySearchManager.getInstance().getWannaToChangeThisPlayer(member.getObjectId()) ? 0x01 :
						0x00); // GoD unknown
				writeD(member.getSummons().size() + (member.getPet() != null ? 1 : 0));
				for (L2SummonInstance summon : member.getSummons())
				{
					writeD(summon.getObjectId());
					writeD(summon.getNpcId() + 1000000);
					writeC(summon.getSummonType());
					writeS(summon.getName());
					writeD((int) summon.getCurrentHp());
					writeD(summon.getMaxHp());
					writeD((int) summon.getCurrentMp());
					writeD(summon.getMaxMp());
					writeC(summon.getLevel());
				}
				if (member.getPet() != null)
				{
					writeD(member.getPet().getObjectId());
					writeD(member.getPet().getNpcId() + 1000000);
					writeC(member.getPet().getSummonType());
					writeS(member.getPet().getName());
					writeD((int) member.getPet().getCurrentHp());
					writeD(member.getPet().getMaxHp());
					writeD((int) member.getPet().getCurrentMp());
					writeD(member.getPet().getMaxMp());
					writeC(member.getPet().getLevel());
				}
			}
		}
	}
}
