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

package l2server.gameserver.handler;

import gnu.trove.TIntObjectHashMap;
import l2server.gameserver.templates.item.L2EtcItem;

/**
 * This class manages handlers of items
 *
 * @version $Revision: 1.1.4.3 $ $Date: 2005/03/27 15:30:09 $
 */
public class ItemHandler
{
	private TIntObjectHashMap<IItemHandler> _datatable;

	/**
	 * Create ItemHandler if doesn't exist and returns ItemHandler
	 *
	 * @return ItemHandler
	 */
	public static ItemHandler getInstance()
	{
		return SingletonHolder._instance;
	}

	/**
	 * Returns the number of elements contained in datatable
	 *
	 * @return int : Size of the datatable
	 */
	public int size()
	{
		return _datatable.size();
	}

	/**
	 * Constructor of ItemHandler
	 */
	private ItemHandler()
	{
		_datatable = new TIntObjectHashMap<>();
	}

	/**
	 * Adds handler of item type in <I>datatable</I>.<BR><BR>
	 * <B><I>Concept :</I></U><BR>
	 * This handler is put in <I>datatable</I> Map &lt;String ; IItemHandler &gt; for each ID corresponding to an item type
	 * (existing in classes of package itemhandlers) sets as key of the Map.
	 *
	 * @param handler (IItemHandler)
	 */
	public void registerItemHandler(IItemHandler handler)
	{
		_datatable.put(handler.getClass().getSimpleName().intern().hashCode(), handler);
	}

	/**
	 * Returns the handler of the item
	 *
	 * @return IItemHandler
	 */
	public IItemHandler getItemHandler(L2EtcItem item)
	{
		if (item == null || item.getHandlerName() == null)
		{
			return null;
		}
		return _datatable.get(item.getHandlerName().hashCode());
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ItemHandler _instance = new ItemHandler();
	}
}
