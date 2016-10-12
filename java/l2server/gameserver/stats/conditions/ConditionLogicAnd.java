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

package l2server.gameserver.stats.conditions;

import l2server.gameserver.stats.Env;

/**
 * The Class ConditionLogicAnd.
 *
 * @author mkizub
 */
public class ConditionLogicAnd extends Condition
{

	private static Condition[] _emptyConditions = new Condition[0];
	public Condition[] conditions = _emptyConditions;

	/**
	 * Instantiates a new condition logic and.
	 */
	public ConditionLogicAnd()
	{
		super();
	}

	/**
	 * Adds the.
	 *
	 * @param condition the condition
	 */
	public void add(Condition condition)
	{
		if (condition == null)
		{
			return;
		}
		if (getListener() != null)
		{
			condition.setListener(this);
		}
		final int len = conditions.length;
		final Condition[] tmp = new Condition[len + 1];
		System.arraycopy(conditions, 0, tmp, 0, len);
		tmp[len] = condition;
		conditions = tmp;
	}

	/**
	 * Sets the listener.
	 *
	 * @param listener the new listener
	 * @see l2server.gameserver.stats.conditions.Condition#setListener(l2server.gameserver.stats.conditions.ConditionListener)
	 */
	@Override
	void setListener(ConditionListener listener)
	{
		if (listener != null)
		{
			for (Condition c : conditions)
			{
				c.setListener(this);
			}
		}
		else
		{
			for (Condition c : conditions)
			{
				c.setListener(null);
			}
		}
		super.setListener(listener);
	}

	/**
	 * Test impl.
	 *
	 * @param env the env
	 * @return true, if successful
	 * @see l2server.gameserver.stats.conditions.Condition#testImpl(l2server.gameserver.stats.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		for (Condition c : conditions)
		{
			if (!c.test(env))
			{
				return false;
			}
		}
		return true;
	}
}
