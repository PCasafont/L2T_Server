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

package l2server.gameserver.model;

/**
 * * @author Gnacik
 */
public class L2PremiumItem {
	private int itemId;
	private long count;
	private String sender;

	public L2PremiumItem(int itemid, long count, String sender) {
		this.itemId = itemid;
		this.count = count;
		this.sender = sender;
	}

	public void updateCount(long newcount) {
		count = newcount;
	}

	public int getItemId() {
		return itemId;
	}

	public long getCount() {
		return count;
	}

	public String getSender() {
		return sender;
	}
}
