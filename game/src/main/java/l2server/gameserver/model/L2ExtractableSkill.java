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

import java.util.ArrayList;

/**
 * @author Zoey76
 */
public class L2ExtractableSkill {
	private final long hash;
	private final ArrayList<L2ExtractableProductItem> product;
	
	public L2ExtractableSkill(long hash, ArrayList<L2ExtractableProductItem> products) {
		this.hash = hash;
		product = products;
	}
	
	public long getSkillHash() {
		return hash;
	}
	
	public ArrayList<L2ExtractableProductItem> getProductItemsArray() {
		return product;
	}
}
