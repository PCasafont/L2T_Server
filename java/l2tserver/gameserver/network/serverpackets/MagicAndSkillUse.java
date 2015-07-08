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

import l2tserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Pere
 */
public final class MagicAndSkillUse extends L2GameServerPacket
{
	private int _objId;
	
	public MagicAndSkillUse(L2PcInstance player)
	{
		_objId = player.getObjectId();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x40);
		writeD(_objId);
		writeD(133560);
		writeD(730502);
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "ExUnk";
	}
}
