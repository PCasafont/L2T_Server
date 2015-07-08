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
 * Format: (chd) ddd
 * d: Always -1
 * d: Origin Team
 * d: Destination Team
 * 
 * @author mrTJO
 */
public class ExCubeGameChangeTeam extends L2GameServerPacket
{
	private static final String _S__FE_97_05_EXCUBEGAMECHANGETEAM = "[S] FE:97:05 ExCubeGameChangeTeam";
	L2PcInstance _player;
	boolean _fromRedTeam;
	
	/**
	 * Move Player from Team x to Team y
	 * 
	 * @param player Player Instance
	 * @param fromRedTeam Is Player from Red Team?
	 */
	public ExCubeGameChangeTeam(L2PcInstance player, boolean fromRedTeam)
	{
		_player = player;
		_fromRedTeam = fromRedTeam;
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x98);
		writeD(0x05);
		
		writeD(_player.getObjectId());
		writeD(_fromRedTeam ? 0x01 : 0x00);
		writeD(_fromRedTeam ? 0x00 : 0x01);
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_97_05_EXCUBEGAMECHANGETEAM;
	}
	
}