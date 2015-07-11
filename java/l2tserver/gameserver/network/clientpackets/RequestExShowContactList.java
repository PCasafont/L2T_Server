/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2tserver.gameserver.network.clientpackets;

import l2tserver.Config;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.serverpackets.ExShowContactList;

/**
 * Format: (ch)
 * 
 * @author mrTJO & UnAfraid
 */
public final class RequestExShowContactList extends L2GameClientPacket
{
	private static final String _C__D0_86_REQUESTEXSHOWCONTACTLIST = "[C] D0:86 RequestExShowContactList";
	
	@Override
	protected void readImpl()
	{
		// trigger packet
	}
	
	@Override
	public void runImpl()
	{
		if (!Config.ALLOW_MAIL)
			return;
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (!activeChar.getContactList().getAllContacts().isEmpty())
			activeChar.sendPacket(new ExShowContactList(activeChar));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_86_REQUESTEXSHOWCONTACTLIST;
	}
}
