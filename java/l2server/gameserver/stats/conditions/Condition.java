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
	private ConditionListener listener;
	private String msg;
	private int msgId;
	private boolean addName = false;
	private boolean result;

	/**
	 * Sets the message.
	 *
	 * @param msg the new message
	 */
	public final void setMessage(String msg)
	{
		this.msg = msg;
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public final String getMessage()
	{
		return msg;
	}

	/**
	 * Sets the message id.
	 *
	 * @param msgId the new message id
	 */
	public final void setMessageId(int msgId)
	{
		this.msgId = msgId;
	}

	/**
	 * Gets the message id.
	 *
	 * @return the message id
	 */
	public final int getMessageId()
	{
		return msgId;
	}

	/**
	 * Adds the name.
	 */
	public final void addName()
	{
		addName = true;
	}

	/**
	 * Checks if is adds the name.
	 *
	 * @return true, if is adds the name
	 */
	public final boolean isAddName()
	{
		return addName;
	}

	/**
	 * Sets the listener.
	 *
	 * @param listener the new listener
	 */
	void setListener(ConditionListener listener)
	{
		this.listener = listener;
		notifyChanged();
	}

	/**
	 * Gets the listener.
	 *
	 * @return the listener
	 */
	final ConditionListener getListener()
	{
		return listener;
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
		if (listener != null && res != result)
		{
			result = res;
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
		if (listener != null)
		{
			listener.notifyChanged();
		}
	}
}
