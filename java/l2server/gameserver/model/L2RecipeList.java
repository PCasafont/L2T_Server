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

import l2server.gameserver.templates.StatsSet;

/**
 * This class describes a Recipe used by Dwarf to craft Item.
 * All L2RecipeList are made of L2RecipeInstance (1 line of the recipe : Item-Quantity needed).<BR><BR>
 */
public class L2RecipeList
{
	/**
	 * The table containing all L2RecipeInstance (1 line of the recipe : Item-Quantity needed) of the L2RecipeList
	 */
	private L2RecipeInstance[] recipes;

	/**
	 * The table containing all L2RecipeStatInstance for the statUse parameter of the L2RecipeList
	 */
	private L2RecipeStatInstance[] statUse;

	/**
	 * The table containing all L2RecipeStatInstance for the altStatChange parameter of the L2RecipeList
	 */
	private L2RecipeStatInstance[] altStatChange;

	/**
	 * The Identifier of the Instance
	 */
	private int id;

	/**
	 * The crafting level needed to use this L2RecipeList
	 */
	private int level;

	/**
	 * The Identifier of the L2RecipeList
	 */
	private int recipeId;

	/**
	 * The name of the L2RecipeList
	 */
	private String recipeName;

	/**
	 * The crafting success rate when using the L2RecipeList
	 */
	private int successRate;

	/**
	 * The Identifier of the Item crafted with this L2RecipeList
	 */
	private int itemId;

	/**
	 * The quantity of Item crafted when using this L2RecipeList
	 */
	private int count;

	/**
	 * The Identifier of the Rare Item crafted with this L2RecipeList
	 */
	private int rareItemId;

	/**
	 * The quantity of Rare Item crafted when using this L2RecipeList
	 */
	private int rareCount;

	/**
	 * The chance of Rare Item crafted when using this L2RecipeList
	 */
	private int rarity;

	/**
	 * If this a common or a dwarven recipe
	 */
	private boolean isDwarvenRecipe;

	/**
	 * Constructor of L2RecipeList (create a new Recipe).<BR><BR>
	 */
	public L2RecipeList(StatsSet set, boolean haveRare)
	{
		this.recipes = new L2RecipeInstance[0];
		this.statUse = new L2RecipeStatInstance[0];
		this.altStatChange = new L2RecipeStatInstance[0];
		this.id = set.getInteger("id");
		this.level = set.getInteger("craftLevel");
		this.recipeId = set.getInteger("recipeId");
		this.recipeName = set.getString("recipeName");
		this.successRate = set.getInteger("successRate");
		this.itemId = set.getInteger("itemId");
		this.count = set.getInteger("count");
		if (haveRare)
		{
			this.rareItemId = set.getInteger("rareItemId");
			this.rareCount = set.getInteger("rareCount");
			this.rarity = set.getInteger("rarity");
		}
		this.isDwarvenRecipe = set.getBool("isDwarvenRecipe");
	}

	/**
	 * Add a L2RecipeInstance to the L2RecipeList (add a line Item-Quantity needed to the Recipe).<BR><BR>
	 */
	public void addRecipe(L2RecipeInstance recipe)
	{
		int len = this.recipes.length;
		L2RecipeInstance[] tmp = new L2RecipeInstance[len + 1];
		System.arraycopy(this.recipes, 0, tmp, 0, len);
		tmp[len] = recipe;
		this.recipes = tmp;
	}

	/**
	 * Add a L2RecipeStatInstance of the statUse parameter to the L2RecipeList.<BR><BR>
	 */
	public void addStatUse(L2RecipeStatInstance statUse)
	{
		int len = this.statUse.length;
		L2RecipeStatInstance[] tmp = new L2RecipeStatInstance[len + 1];
		System.arraycopy(this.statUse, 0, tmp, 0, len);
		tmp[len] = statUse;
		this.statUse = tmp;
	}

	/**
	 * Add a L2RecipeStatInstance of the altStatChange parameter to the L2RecipeList.<BR><BR>
	 */
	public void addAltStatChange(L2RecipeStatInstance statChange)
	{
		int len = this.altStatChange.length;
		L2RecipeStatInstance[] tmp = new L2RecipeStatInstance[len + 1];
		System.arraycopy(this.altStatChange, 0, tmp, 0, len);
		tmp[len] = statChange;
		this.altStatChange = tmp;
	}

	/**
	 * Return the Identifier of the Instance.<BR><BR>
	 */
	public int getId()
	{
		return this.id;
	}

	/**
	 * Return the crafting level needed to use this L2RecipeList.<BR><BR>
	 */
	public int getLevel()
	{
		return this.level;
	}

	/**
	 * Return the Identifier of the L2RecipeList.<BR><BR>
	 */
	public int getRecipeId()
	{
		return this.recipeId;
	}

	/**
	 * Return the name of the L2RecipeList.<BR><BR>
	 */
	public String getRecipeName()
	{
		return this.recipeName;
	}

	/**
	 * Return the crafting success rate when using the L2RecipeList.<BR><BR>
	 */
	public int getSuccessRate()
	{
		return this.successRate;
	}

	/**
	 * Return rue if the Item crafted with this L2RecipeList is consumable (shot, arrow,...).<BR><BR>
	 */
	public boolean isConsumable()
	{
		return this.itemId >= 1463 && this.itemId <= 1467 // Soulshots
				|| this.itemId >= 2509 && this.itemId <= 2514 // Spiritshots
				|| this.itemId >= 3947 && this.itemId <= 3952 // Blessed Spiritshots
				|| this.itemId >= 1341 && this.itemId <= 1345;
	}

	/**
	 * Return the Identifier of the Item crafted with this L2RecipeList.<BR><BR>
	 */
	public int getItemId()
	{
		return this.itemId;
	}

	/**
	 * Return the quantity of Item crafted when using this L2RecipeList.<BR><BR>
	 */
	public int getCount()
	{
		return this.count;
	}

	/**
	 * Return the Identifier of the Rare Item crafted with this L2RecipeList.<BR><BR>
	 */
	public int getRareItemId()
	{
		return this.rareItemId;
	}

	/**
	 * Return the quantity of Rare Item crafted when using this L2RecipeList.<BR><BR>
	 */
	public int getRareCount()
	{
		return this.rareCount;
	}

	/**
	 * Return the chance of Rare Item crafted when using this L2RecipeList.<BR><BR>
	 */
	public int getRarity()
	{
		return this.rarity;
	}

	/**
	 * Return <B>true</B> if this a Dwarven recipe or <B>false</B> if its a Common recipe
	 */
	public boolean isDwarvenRecipe()
	{
		return this.isDwarvenRecipe;
	}

	/**
	 * Return the table containing all L2RecipeInstance (1 line of the recipe : Item-Quantity needed) of the L2RecipeList.<BR><BR>
	 */
	public L2RecipeInstance[] getRecipes()
	{
		return this.recipes;
	}

	/**
	 * Return the table containing all L2RecipeStatInstance of the statUse parameter of the L2RecipeList.<BR><BR>
	 */
	public L2RecipeStatInstance[] getStatUse()
	{
		return this.statUse;
	}

	/**
	 * Return the table containing all L2RecipeStatInstance of the AltStatChange parameter of the L2RecipeList.<BR><BR>
	 */
	public L2RecipeStatInstance[] getAltStatChange()
	{
		return this.altStatChange;
	}
}
