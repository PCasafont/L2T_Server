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
	private int chatId = 0;
	private int moveX;
	private int moveY;
	private int moveZ;
	private int delay;
	@SuppressWarnings("unused")
	private NpcStringId npcString;
	private String chatText;
	private boolean running;

	public L2NpcWalkerNode(int moveX, int moveY, int moveZ, int delay, String chatText, boolean running)
	{
		this(moveX, moveY, moveZ, delay, null, chatText, running);
	}

	public L2NpcWalkerNode(int x, int y, int z, int delay, NpcStringId npcString, String chatText, boolean running)
	{
		this.moveX = x;
		this.moveY = y;
		this.moveZ = z;
		this.delay = delay;
		this.npcString = npcString;
		this.chatText = chatText;
		if (this.chatText.startsWith("#"))
		{
			this.chatId = Integer.parseInt(this.chatText.substring(1));
		}
		else if (this.chatText.trim().isEmpty())
		{
			this.chatText = null;
		}
		this.running = running;
	}

	public String getChatText()
	{
		if (this.chatId != 0)
		{
			throw new IllegalStateException("Chat id is defined for walker route!");
		}
		return this.chatText;
	}

	public int getMoveX()
	{
		return this.moveX;
	}

	public int getMoveY()
	{
		return this.moveY;
	}

	public int getMoveZ()
	{
		return this.moveZ;
	}

	public int getDelay()
	{
		return this.delay;
	}

	public boolean getRunning()
	{
		return this.running;
	}

	public int getChatId()
	{
		return this.chatId;
	}
}
