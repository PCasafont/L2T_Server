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

import l2server.gameserver.instancemanager.TerritoryWarManager;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author JIV
 *
 * Structure: ddddd
 */
public class ExDominionWarStart extends L2GameServerPacket
{
	
	private int _objId;
	private int _terId;
	private boolean _isDisguised;
	
	public ExDominionWarStart(L2PcInstance player)
	{
		_objId = player.getObjectId();
		_terId = TerritoryWarManager.getInstance().getRegisteredTerritoryId(player);
		_isDisguised = TerritoryWarManager.getInstance().isDisguised(_objId);
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(_objId);
		writeD(1); //??
		writeD(_terId);
		writeD(_isDisguised ? 1 : 0);
		writeD(_isDisguised ? _terId : 0);
	}
}
