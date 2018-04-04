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
 * This class ...
 *
 * @version $Revision: 1.2.4.1 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2TeleportLocation {
	private int teleId;
	private int locX;
	private int locY;
	private int locZ;
	private int price;
	private boolean forNoble;
	private int itemId;
	private String description;
	
	public void setTeleId(int id) {
		teleId = id;
	}
	
	public void setLocX(int locX) {
		this.locX = locX;
	}
	
	public void setLocY(int locY) {
		this.locY = locY;
	}
	
	public void setLocZ(int locZ) {
		this.locZ = locZ;
	}
	
	public void setPrice(int price) {
		this.price = price;
	}
	
	public void setIsForNoble(boolean val) {
		forNoble = val;
	}
	
	public void setItemId(int val) {
		itemId = val;
	}
	
	public int getTeleId() {
		return teleId;
	}
	
	public int getLocX() {
		return locX;
	}
	
	public int getLocY() {
		return locY;
	}
	
	public int getLocZ() {
		return locZ;
	}
	
	public int getPrice() {
		return price;
	}
	
	public boolean getIsForNoble() {
		return forNoble;
	}
	
	public int getItemId() {
		return itemId;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
}
