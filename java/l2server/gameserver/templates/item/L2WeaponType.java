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
 * @author mkizub
 *         <BR>Description of Weapon Type
 */
public enum L2WeaponType implements L2ItemType
{
	SWORD("Sword"),
	BLUNT("Blunt"),
	DAGGER("Dagger"),
	BOW("Bow"),
	POLE("Pole"),
	NONE("None"),
	DUAL("Dual Sword"),
	ETC("Etc"),
	FIST("Fist"),
	DUALFIST("Dual Fist"),
	FISHINGROD("Rod"),
	RAPIER("Rapier"),
	ANCIENTSWORD("Ancient"),
	CROSSBOWK("Crossbow"),
	FLAG("Flag"),
	OWNTHING("Ownthing"),
	DUALDAGGER("Dual Dagger"),
	CROSSBOW("Crossbow"),
	DUALBLUNT("Dual Blunt"),

	// L2J CUSTOM, BACKWARD COMPATIBILITY
	BIGBLUNT("Big Blunt"),
	BIGSWORD("Big Sword");

	private final int _mask;
	private final String _name;

	/**
	 * Constructor of the L2WeaponType.
	 *
	 * @param name : String designating the name of the WeaponType
	 */
	L2WeaponType(String name)
	{
		_mask = 1 << ordinal();
		_name = name;
	}

	/**
	 * Returns the ID of the item after applying the mask.
	 *
	 * @return int : ID of the item
	 */
	@Override
	public int mask()
	{
		return _mask;
	}

	/**
	 * Returns the name of the WeaponType
	 *
	 * @return String
	 */
	@Override
	public String toString()
	{
		return _name;
	}

}
