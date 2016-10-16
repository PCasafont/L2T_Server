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

import lombok.Getter;

/**
 * This class describes a RecipeList componant (1 line of the recipe : Item-Quantity needed).<BR><BR>
 */
public class L2RecipeInstance
{
	/**
	 * The Identifier of the item needed in the L2RecipeInstance
	 */
	@Getter private int itemId;

	/**
	 * The item quantity needed in the L2RecipeInstance
	 */
	@Getter private int quantity;

	/**
	 * Constructor of L2RecipeInstance (create a new line in a RecipeList).<BR><BR>
	 */
	public L2RecipeInstance(int itemId, int quantity)
	{
		this.itemId = itemId;
		this.quantity = quantity;
	}
}
