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

package l2server.gameserver.templates.chars;

import l2server.gameserver.datatables.PlayerStatDataTable;
import l2server.gameserver.model.base.Race;
import l2server.gameserver.templates.StatsSet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mkizub
 */
public class L2PcTemplate extends L2CharTemplate
{
	public final Race race;
	public final boolean isMage;
	public final int startingClassId;

	public final double fCollisionHeightFemale;
	public final double fCollisionRadiusFemale;

	private List<PcTemplateItem> _items = new ArrayList<>();
	private List<Integer> _skillIds = new ArrayList<>();

	public L2PcTemplate(StatsSet set)
	{
		super(set);
		race = Race.values()[set.getInteger("raceId")];
		isMage = set.getBool("isMage");
		startingClassId = set.getInteger("startingClassId");

		fCollisionRadiusFemale = set.getDouble("collisionRadiusFemale");
		fCollisionHeightFemale = set.getDouble("collisionHeightFemale");
	}

	public int getId()
	{
		return race.ordinal() * 2 + (isMage ? 1 : 0);
	}

	/**
	 * Adds starter equipment
	 */
	public void addItem(int itemId, int amount, boolean equipped)
	{
		if (amount == 1 || !equipped)
		{
			_items.add(new PcTemplateItem(itemId, amount, equipped));
		}
		else
		{
			for (int i = 0; i < amount; i++)
			{
				_items.add(new PcTemplateItem(itemId, 1, equipped));
			}
		}
	}

	/**
	 * @return itemIds of all the starter equipment
	 */
	public List<PcTemplateItem> getItems()
	{
		return _items;
	}

	public static final class PcTemplateItem
	{
		private final int _itemId;
		private final int _amount;
		private final boolean _equipped;

		/**
		 * @param amount
		 * @param itemId
		 */
		public PcTemplateItem(int itemId, int amount, boolean equipped)
		{
			_itemId = itemId;
			_amount = amount;
			_equipped = equipped;
		}

		/**
		 * @return Returns the itemId.
		 */
		public int getItemId()
		{
			return _itemId;
		}

		/**
		 * @return Returns the amount.
		 */
		public int getAmount()
		{
			return _amount;
		}

		/**
		 * @return Returns the if the item should be equipped after char creation.
		 */
		public boolean isEquipped()
		{
			return _equipped;
		}
	}

	public void addSkill(int id)
	{
		_skillIds.add(id);
	}

	public List<Integer> getSkillIds()
	{
		return _skillIds;
	}

	public final int getFallHeight()
	{
		return 333;
	}

	@Override
	public float getBaseHpReg(int level)
	{
		return PlayerStatDataTable.getInstance().getHpRegen(level);
	}

	@Override
	public float getBaseMpReg(int level)
	{
		return PlayerStatDataTable.getInstance().getMpRegen(level);
	}

	@Override
	public float getBaseCpReg(int level)
	{
		return PlayerStatDataTable.getInstance().getCpRegen(level);
	}
}
