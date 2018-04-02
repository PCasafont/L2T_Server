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

import l2server.gameserver.model.actor.instance.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Format: (ch)d[S]
 * d: Number of Contacts
 * [
 * S: Character Name
 * ]
 *
 * @author UnAfraid & mrTJO
 */
public class ExShowContactList extends L2GameServerPacket {
	private final List<String> contacts;
	
	public ExShowContactList(Player player) {
		contacts = new ArrayList<>();
		contacts.addAll(player.getContactList().getAllContacts());
	}
	
	@Override
	protected final void writeImpl() {
		writeD(contacts.size());
		for (String name : contacts) {
			writeS(name);
		}
	}
}
