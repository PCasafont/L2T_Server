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

package l2server.gameserver.model.multisell;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DS
 */
public class MultiSellEntry
{
	protected int entryId;
	protected boolean stackable = true;

	protected List<Ingredient> products;
	protected List<Ingredient> ingredients;

	public MultiSellEntry(int entryId)
	{
		this.entryId = entryId;
		this.products = new ArrayList<>();
		this.ingredients = new ArrayList<>();
	}

	/**
	 * This constructor used in PreparedEntry only
	 * ArrayLists not created
	 */
	protected MultiSellEntry()
	{
	}

	public final void setEntryId(int id)
	{
		this.entryId = id;
	}

	public final int getEntryId()
	{
		return this.entryId;
	}

	public final void addProduct(Ingredient product)
	{
		this.products.add(product);

		if (!product.isStackable())
		{
			this.stackable = false;
		}
	}

	public final List<Ingredient> getProducts()
	{
		return this.products;
	}

	public final void addIngredient(Ingredient ingredient)
	{
		this.ingredients.add(ingredient);
	}

	public final List<Ingredient> getIngredients()
	{
		return this.ingredients;
	}

	public final boolean isStackable()
	{
		return this.stackable;
	}

	public long getTaxAmount()
	{
		return 0;
	}
}
