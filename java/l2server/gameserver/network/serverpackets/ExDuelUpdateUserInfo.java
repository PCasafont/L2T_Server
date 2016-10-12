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

import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * Format: ch Sddddddddd
 *
 * @author KenM
 */
public class ExDuelUpdateUserInfo extends L2GameServerPacket
{
	private L2PcInstance _activeChar;

	public ExDuelUpdateUserInfo(L2PcInstance cha)
	{
		_activeChar = cha;
	}

	/**
	 */
	@Override
	protected final void writeImpl()
	{
		writeS(_activeChar.getName());
		writeD(_activeChar.getObjectId());
		writeD(_activeChar.getCurrentClass().getId());
		writeD(_activeChar.getLevel());
		writeD((int) _activeChar.getCurrentHp());
		writeD(_activeChar.getMaxVisibleHp());
		writeD((int) _activeChar.getCurrentMp());
		writeD(_activeChar.getMaxMp());
		writeD((int) _activeChar.getCurrentCp());
		writeD(_activeChar.getMaxCp());
	}
}
