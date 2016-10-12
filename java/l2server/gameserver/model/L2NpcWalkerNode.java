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

import l2server.gameserver.network.NpcStringId;

/**
 * @author Rayan RPG, JIV
 * @since 927
 */
public class L2NpcWalkerNode
{
	private int _chatId = 0;
	private int _moveX;
	private int _moveY;
	private int _moveZ;
	private int _delay;
	@SuppressWarnings("unused")
	private NpcStringId _npcString;
	private String _chatText;
	private boolean _running;

	public L2NpcWalkerNode(int moveX, int moveY, int moveZ, int delay, String chatText, boolean running)
	{
		this(moveX, moveY, moveZ, delay, null, chatText, running);
	}

	public L2NpcWalkerNode(int x, int y, int z, int delay, NpcStringId npcString, String chatText, boolean running)
	{
		_moveX = x;
		_moveY = y;
		_moveZ = z;
		_delay = delay;
		_npcString = npcString;
		_chatText = chatText;
		if (_chatText.startsWith("#"))
		{
			_chatId = Integer.parseInt(_chatText.substring(1));
		}
		else if (_chatText.trim().isEmpty())
		{
			_chatText = null;
		}
		_running = running;
	}

	public String getChatText()
	{
		if (_chatId != 0)
		{
			throw new IllegalStateException("Chat id is defined for walker route!");
		}
		return _chatText;
	}

	public int getMoveX()
	{
		return _moveX;
	}

	public int getMoveY()
	{
		return _moveY;
	}

	public int getMoveZ()
	{
		return _moveZ;
	}

	public int getDelay()
	{
		return _delay;
	}

	public boolean getRunning()
	{
		return _running;
	}

	public int getChatId()
	{
		return _chatId;
	}
}
