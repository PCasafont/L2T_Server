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

import static l2server.gameserver.model.itemcontainer.PcInventory.ADENA_ID;

import l2server.gameserver.model.L2ItemInstance;

import java.util.ArrayList;

/**
 * @author DS
 */
public class PreparedEntry extends MultiSellEntry
{
	private long taxAmount = 0;

	public PreparedEntry(MultiSellEntry template, L2ItemInstance item, boolean applyTaxes, boolean maintainEnchantment, double taxRate)
	{
		entryId = template.getEntryId() * 100000;
		if (maintainEnchantment && item != null)
		{
			entryId += item.getEnchantLevel();
		}

		ItemInfo info = null;
		long adenaAmount = 0;

		ingredients = new ArrayList<>(template.getIngredients().size());
		for (Ingredient ing : template.getIngredients())
		{
			if (ing.getItemId() == ADENA_ID)
			{
				// Tax ingredients added only if taxes enabled
				if (ing.isTaxIngredient())
				{
					// if taxes are to be applied, modify/add the adena count based on the template adena/ancient adena count
					if (applyTaxes)
					{
						taxAmount += Math.round(ing.getItemCount() * taxRate);
					}
				}
				else
				{
					adenaAmount += ing.getItemCount();
				}

			}
			else if (maintainEnchantment && item != null && ing.isArmorOrWeapon())
			{
				info = new ItemInfo(item);
				final Ingredient newIngredient = ing.clone();
				newIngredient.setItemInfo(info);
				ingredients.add(newIngredient);
			}
			else
			{
				ingredients.add(ing);
			}
		}

		// now add the adena, if any.
		adenaAmount += taxAmount; // do not forget tax
		if (adenaAmount > 0)
		{
			ingredients.add(new Ingredient(ADENA_ID, adenaAmount, false, false));
		}

		// now copy products
		products = new ArrayList<>(template.getProducts().size());
		for (Ingredient ing : template.getProducts())
		{
			if (!ing.isStackable())
			{
				stackable = false;
			}

			if (maintainEnchantment && ing.isArmorOrWeapon())
			{
				final Ingredient newProduct = ing.clone();
				newProduct.setItemInfo(info);
				products.add(newProduct);
			}
			else
			{
				products.add(ing);
			}
		}
	}

	@Override
	public final long getTaxAmount()
	{
		return taxAmount;
	}
}
