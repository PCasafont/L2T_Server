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
	private L2PcInstance activeChar;

	public ExDuelUpdateUserInfo(L2PcInstance cha)
	{
		this.activeChar = cha;
	}

	/**
	 */
	@Override
	protected final void writeImpl()
	{
		writeS(this.activeChar.getName());
		writeD(this.activeChar.getObjectId());
		writeD(this.activeChar.getCurrentClass().getId());
		writeD(this.activeChar.getLevel());
		writeD((int) this.activeChar.getCurrentHp());
		writeD(this.activeChar.getMaxVisibleHp());
		writeD((int) this.activeChar.getCurrentMp());
		writeD(this.activeChar.getMaxMp());
		writeD((int) this.activeChar.getCurrentCp());
		writeD(this.activeChar.getMaxCp());
	}
}
