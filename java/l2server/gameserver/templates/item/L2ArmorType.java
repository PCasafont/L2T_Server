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

package l2server.gameserver.templates.item;

/**
 * Description of Armor Type
 */

public enum L2ArmorType implements L2ItemType
{
	NONE("None"), LIGHT("Light"), HEAVY("Heavy"), MAGIC("Magic"), SIGIL("Sigil"),

	//L2J CUSTOM
	SHIELD("Shield");

	final int _mask;
	final String _name;

	/**
	 * Constructor of the L2ArmorType.
	 *
	 * @param name : String designating the name of the ArmorType
	 */
	L2ArmorType(String name)
	{
		_mask = 1 << ordinal() + L2WeaponType.values().length;
		_name = name;
	}

	/**
	 * Returns the ID of the ArmorType after applying a mask.
	 *
	 * @return int : ID of the ArmorType after mask
	 */
	@Override
	public int mask()
	{
		return _mask;
	}

	/**
	 * Returns the name of the ArmorType
	 *
	 * @return String
	 */
	@Override
	public String toString()
	{
		return _name;
	}
}
