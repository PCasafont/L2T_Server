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
import java.util.List;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.1.2.2 $ $Date: 2005/03/27 15:29:33 $
 */
public class L2ManufactureList
{
	private List<L2ManufactureItem> list;
	private boolean confirmed;
	private String manufactureStoreName;

	public L2ManufactureList()
	{
		this.list = new ArrayList<>();
		this.confirmed = false;
	}

	public int size()
	{
		return this.list.size();
	}

	public void setConfirmedTrade(boolean x)
	{
		this.confirmed = x;
	}

	public boolean hasConfirmed()
	{
		return this.confirmed;
	}

	/**
	 */
	public void setStoreName(String manufactureStoreName)
	{
		this.manufactureStoreName = manufactureStoreName;
	}

	/**
	 * @return Returns the this.manufactureStoreName.
	 */
	public String getStoreName()
	{
		return this.manufactureStoreName;
	}

	public void add(L2ManufactureItem item)
	{
		this.list.add(item);
	}

	public List<L2ManufactureItem> getList()
	{
		return this.list;
	}

	public void setList(List<L2ManufactureItem> list)
	{
		this.list = list;
	}
}
