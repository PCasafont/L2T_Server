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

import l2tserver.gameserver.model.actor.L2Character;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;

/**
 *
 * @author  devScarlet
 */
public class NicknameChanged extends L2GameServerPacket
{
	private static final String _S__CC_TITLE_UPDATE = "[S] cc NicknameChanged";
	private String _title;
	private int _objectId;
	
	public NicknameChanged(L2PcInstance cha)
	{
		_objectId = cha.getObjectId();
		_title = cha.getTitle();
	}
	
	public NicknameChanged(L2Character cha)
	{
		_objectId = cha.getObjectId();
		_title = cha.getTitle();
	}
	
	public NicknameChanged(final int objectId, final String title)
	{
		_objectId = objectId;
		_title = title;
	}
	
	/**
	 * @see l2tserver.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xcc);
		writeD(_objectId);
		writeS(_title);
	}
	
	/**
	 * @see l2tserver.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__CC_TITLE_UPDATE;
	}
	
}
