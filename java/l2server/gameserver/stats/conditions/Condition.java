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
 * The Class Condition.
 *
 * @author mkizub
 */
public abstract class Condition implements ConditionListener
{
	private ConditionListener _listener;
	private String _msg;
	private int _msgId;
	private boolean _addName = false;
	private boolean _result;

	/**
	 * Sets the message.
	 *
	 * @param msg the new message
	 */
	public final void setMessage(String msg)
	{
		_msg = msg;
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public final String getMessage()
	{
		return _msg;
	}

	/**
	 * Sets the message id.
	 *
	 * @param msgId the new message id
	 */
	public final void setMessageId(int msgId)
	{
		_msgId = msgId;
	}

	/**
	 * Gets the message id.
	 *
	 * @return the message id
	 */
	public final int getMessageId()
	{
		return _msgId;
	}

	/**
	 * Adds the name.
	 */
	public final void addName()
	{
		_addName = true;
	}

	/**
	 * Checks if is adds the name.
	 *
	 * @return true, if is adds the name
	 */
	public final boolean isAddName()
	{
		return _addName;
	}

	/**
	 * Sets the listener.
	 *
	 * @param listener the new listener
	 */
	void setListener(ConditionListener listener)
	{
		_listener = listener;
		notifyChanged();
	}

	/**
	 * Gets the listener.
	 *
	 * @return the listener
	 */
	final ConditionListener getListener()
	{
		return _listener;
	}

	/**
	 * Test.
	 *
	 * @param env the env
	 * @return true, if successful
	 */
	public final boolean test(Env env)
	{
		boolean res = testImpl(env);
		if (_listener != null && res != _result)
		{
			_result = res;
			notifyChanged();
		}
		return res;
	}

	/**
	 * Test impl.
	 *
	 * @param env the env
	 * @return true, if successful
	 */
	abstract boolean testImpl(Env env);

	/* (non-Javadoc)
	 * @see l2server.gameserver.stats.conditions.ConditionListener#notifyChanged()
	 */
	@Override
	public void notifyChanged()
	{
		if (_listener != null)
		{
			_listener.notifyChanged();
		}
	}
}
