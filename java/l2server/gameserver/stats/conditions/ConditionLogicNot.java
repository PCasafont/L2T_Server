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
 * The Class ConditionLogicNot.
 *
 * @author mkizub
 */
public class ConditionLogicNot extends Condition
{

	private Condition _condition;

	/**
	 * Instantiates a new condition logic not.
	 *
	 * @param condition the condition
	 */
	public ConditionLogicNot(Condition condition)
	{
		_condition = condition;
		if (getListener() != null)
		{
			_condition.setListener(this);
		}
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
			_condition.setListener(this);
		}
		else
		{
			_condition.setListener(null);
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
		return !_condition.test(env);
	}
}
