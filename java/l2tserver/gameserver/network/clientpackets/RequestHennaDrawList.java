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
package l2tserver.gameserver.network.clientpackets;

import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.serverpackets.HennaEquipList;

/**
 * RequestHennaList - 0xba
 *
 * @author Tempy
 */
public final class RequestHennaDrawList extends L2GameClientPacket
{
	private static final String _C__BA_RequestHennaDrawList = "[C] ba RequestHennaDrawList";
	
	// This is just a trigger packet...
	@SuppressWarnings("unused")
	private int _unknown;
	
	@Override
	protected void readImpl()
	{
		_unknown = readD(); // ??
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		activeChar.sendPacket(new HennaEquipList(activeChar, activeChar.getCurrentClass().getAllowedDyes()));
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__BA_RequestHennaDrawList;
	}
}