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

package l2server.gameserver;

import java.util.HashMap;
import java.util.Map;

public class ReloadableManager
{
	private Map<String, Reloadable> _reloadables = new HashMap<>();

	public static ReloadableManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void register(String name, Reloadable r)
	{
		_reloadables.put(name, r);
	}

	public String reload(String name)
	{
		Reloadable r = _reloadables.get(name);
		if (r == null)
		{
			return "Couldn't find a reloadable called \"" + name + "\"";
		}

		return r.getReloadMessage(r.reload());
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ReloadableManager _instance = new ReloadableManager();
	}
}
