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

/**
 * Author: VISTALL
 *
 */
public class ExSubPledgeSkillAdd extends L2GameServerPacket
{
	private final int _type;
	private final int _skillId;
	private final int _skillLevel;
	
	public ExSubPledgeSkillAdd(int type, int skillId, int skillLevel)
	{
		_type = type;
		_skillId = skillId;
		_skillLevel = skillLevel;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x77);
		writeD(_type);
		writeD(_skillId);
		writeD(_skillLevel);
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[S] FE:76 ExSubPledgeSkillAdd".intern();
	}
	
}
