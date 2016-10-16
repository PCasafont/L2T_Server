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
import lombok.Setter;
/**
 * This class ...
 *
 * @version $Revision: 1.2.4.1 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2TeleportLocation
{
	@Getter private int teleId;
	@Getter @Setter private int locX;
	@Getter @Setter private int locY;
	@Getter @Setter private int locZ;
	@Getter @Setter private int price;
	private boolean forNoble;
	@Getter private int itemId;
	@Getter @Setter private String description;

	/**
	 * @param id
	 */
	public void setTeleId(int id)
	{
		teleId = id;
	}

	/**
	 * @param locX
	 */

	/**
	 * @param locY
	 */

	/**
	 * @param locZ
	 */

	/**
	 * @param price
	 */

	/**
	 * @param val
	 */
	public void setIsForNoble(boolean val)
	{
		forNoble = val;
	}

	/**
	 * @param val
	 */
	public void setItemId(int val)
	{
		itemId = val;
	}

	/**
	 * @return
	 */

	/**
	 * @return
	 */

	/**
	 * @return
	 */

	/**
	 * @return
	 */

	/**
	 * @return
	 */

	/**
	 * @return
	 */
	public boolean getIsForNoble()
	{
		return forNoble;
	}

	/**
	 * @return
	 */


}
