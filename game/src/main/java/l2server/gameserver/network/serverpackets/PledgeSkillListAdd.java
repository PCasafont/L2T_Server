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

/**
 * Format: (ch) dd
 *
 * @author -Wooden-
 */
public class PledgeSkillListAdd extends L2GameServerPacket {
	private int id;
	private int lvl;
	
	public PledgeSkillListAdd(int id, int lvl) {
		this.id = id;
		this.lvl = lvl;
	}
	
	@Override
	protected final void writeImpl() {
		writeD(id);
		writeD(lvl);
	}
}
