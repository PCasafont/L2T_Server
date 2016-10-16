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

import l2server.gameserver.model.L2Object.InstanceType;

import java.util.HashMap;
import java.util.Map;

public class ActionHandler
{
	private Map<InstanceType, IActionHandler> actions;
	private Map<InstanceType, IActionHandler> actionsShift;

	public static ActionHandler getInstance()
	{
		return SingletonHolder.instance;
	}

	private ActionHandler()
	{
		actions = new HashMap<>();
		actionsShift = new HashMap<>();
	}

	public void registerActionHandler(IActionHandler handler)
	{
		actions.put(handler.getInstanceType(), handler);
	}

	public void registerActionShiftHandler(IActionHandler handler)
	{
		actionsShift.put(handler.getInstanceType(), handler);
	}

	public IActionHandler getActionHandler(InstanceType iType)
	{
		IActionHandler result = null;
		for (InstanceType t = iType; t != null; t = t.getParent())
		{
			result = actions.get(t);
			if (result != null)
			{
				break;
			}
		}
		return result;
	}

	public IActionHandler getActionShiftHandler(InstanceType iType)
	{
		IActionHandler result = null;
		for (InstanceType t = iType; t != null; t = t.getParent())
		{
			result = actionsShift.get(t);
			if (result != null)
			{
				break;
			}
		}
		return result;
	}

	public int size()
	{
		return actions.size();
	}

	public int sizeShift()
	{
		return actionsShift.size();
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ActionHandler instance = new ActionHandler();
	}
}
